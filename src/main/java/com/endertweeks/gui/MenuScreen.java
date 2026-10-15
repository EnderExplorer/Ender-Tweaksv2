package com.endertweeks.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.IntConsumer;

import org.lwjgl.glfw.GLFW;

import com.endertweeks.core.Config;
import com.endertweeks.core.Feature;
import com.endertweeks.core.Features;
import com.endertweeks.core.Friends;
import com.endertweeks.core.Keys;
import com.endertweeks.render.Draw;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/**
 * Right Ctrl menu with a sidebar. Each sidebar tab is a game mode (Sword, Crystal, Mace, Cart, Visuals).
 * Left-click a module = toggle, right-click = settings + keybind.
 * Immediate-mode: every frame we rebuild the list of clickable rectangles.
 */
public class MenuScreen extends Screen {
	private static final int ACCENT = 0xFF8A4FFF;
	private static final String[] TABS = {"Sword", "Crystal", "Mace", "Cart", "Visuals", "Misc", "Friends"};
	private static final Map<String, String> SUBTITLE = Map.of(
			"Sword", "Sword PvP",
			"Crystal", "Crystal / anchor PvP",
			"Mace", "Mace PvP",
			"Cart", "Cart PvP",
			"Visuals", "ESP and visuals",
			"Misc", "Movement and utility",
			"Friends", "Players the modules ignore");
	private static final Map<String, String> PLANNED = Map.of();

	private record Hit(int x, int y, int w, int h, IntConsumer click) {}

	private final List<Hit> hits = new ArrayList<>();
	private String tab = "Sword";
	private Feature selected;
	private Feature capturing;
	private Feature.Text typing;
	private final StringBuilder friendInput = new StringBuilder();
	private boolean friendFocus;

	public MenuScreen() {
		super(Component.literal("Ender Tweeks"));
	}

	@Override
	public boolean isPauseScreen() { return false; }

	@Override
	public void onClose() {
		Config.save();
		super.onClose();
	}

	// ---------------------------------------------------------------- drawing
	private void button(GuiGraphics g, int mx, int my, int x, int y, int w, int h, String label, int bg, IntConsumer click) {
		boolean hover = mx >= x && mx < x + w && my >= y && my < y + h;
		g.fill(x, y, x + w, y + h, hover ? (bg | 0xFF000000) + 0x00181818 : bg);
		Draw.rect(g, x, y, x + w, y + h, 0xFF000000 | (hover ? 0x00B28CFF : 0x00403858));
		g.drawString(font, label, x + (w - font.width(label)) / 2, y + (h - 8) / 2, 0xFFFFFFFF, true);
		hits.add(new Hit(x, y, w, h, click));
	}

	@Override
	public void render(GuiGraphics g, int mx, int my, float delta) {
		hits.clear();
		g.fill(0, 0, width, height, 0x88000000);

		int pw = 470, ph = 310, sw = 96;
		int px = Math.max(0, (width - pw) / 2), py = Math.max(0, (height - ph) / 2);
		g.fill(px, py, px + pw, py + ph, 0xF0100E1A);
		g.fill(px, py, px + sw, py + ph, 0xFF0A0812);
		Draw.rect(g, px, py, px + pw, py + ph, ACCENT);

		// sidebar
		g.drawString(font, "ENDER", px + 12, py + 10, 0xFFB28CFF, true);
		g.drawString(font, "TWEEKS", px + 12, py + 21, 0xFFFFFFFF, true);
		int ty = py + 44;
		for (String name : TABS) {
			if (name.equals("Misc")) ty += 6;
			final String t = name;
			boolean sel = tab.equals(name);
			boolean hover = mx >= px + 6 && mx < px + sw - 6 && my >= ty && my < ty + 22;
			g.fill(px + 6, ty, px + sw - 6, ty + 22, sel ? 0xFF2B1F55 : hover ? 0xFF1C1634 : 0x00000000);
			if (sel) g.fill(px + 6, ty, px + 9, ty + 22, ACCENT);
			g.drawString(font, name, px + 16, ty + 7, sel ? 0xFFFFFFFF : 0xFFB0A8C8, true);
			hits.add(new Hit(px + 6, ty, sw - 12, 22, b -> {
				tab = t;
				selected = null; capturing = null; typing = null; friendFocus = false;
			}));
			ty += 25;
		}

		int cx = px + sw + 12, cw = pw - sw - 24;
		g.drawString(font, tab.toUpperCase(), cx, py + 10, 0xFFFFFFFF, true);
		g.drawString(font, SUBTITLE.getOrDefault(tab, ""), cx, py + 22, 0xFF9A9AB0, true);
		g.fill(cx, py + 34, cx + cw, py + 35, 0xFF403858);

		if (tab.equals("Friends")) drawFriends(g, mx, my, cx, py, cw, ph);
		else drawModules(g, mx, my, cx, py, cw, ph);
	}

	private void drawModules(GuiGraphics g, int mx, int my, int cx, int py, int cw, int ph) {
		List<Feature> list = Features.in(tab);
		int gap = 6, bw = (cw - gap) / 2, bh = 22;
		int startY = py + 42;
		String hoverDesc = null;

		for (int i = 0; i < list.size(); i++) {
			Feature f = list.get(i);
			int x = cx + (i % 2) * (bw + gap);
			int y = startY + (i / 2) * (bh + 4);
			int bg = f.enabled ? 0xFF2E7D32 : f.action ? 0xFF2F3E6B : 0xFF2A2740;
			button(g, mx, my, x, y, bw, bh, f.name + (f.action ? "  [key]" : ""), bg, b -> {
				if (b == 0) f.toggle();
				else if (b == 1) { selected = f; capturing = null; typing = null; }
			});
			if (f == selected) Draw.rect(g, x - 1, y - 1, x + bw + 1, y + bh + 1, ACCENT);
			if (mx >= x && mx < x + bw && my >= y && my < y + bh) hoverDesc = f.description;
		}

		int rows = (list.size() + 1) / 2;
		int sy = startY + rows * (bh + 4) + 6;

		String planned = PLANNED.get(tab);
		if (planned != null) {
			g.drawString(font, planned, cx, sy, 0xFF9A9AB0, true);
			sy += 14;
		}
		g.fill(cx, sy - 3, cx + cw, sy - 2, 0xFF403858);

		if (selected != null && selected.category.equals(tab)) {
			g.drawString(font, selected.name + " settings", cx, sy + 3, 0xFFFFFFFF, true);
			int y = sy + 17;

			String keyLabel = capturing == selected ? "press a key... (Esc = none)" : "Keybind: " + Keys.name(selected.key);
			button(g, mx, my, cx, y, 190, 18, keyLabel, 0xFF2A2740, b -> {
				if (b == 0 || b == 1) { capturing = selected; typing = null; }
			});
			button(g, mx, my, cx + 194, y, 50, 18, "Clear", 0xFF402A2A, b -> selected.key = -1);
			if (selected.action) g.drawString(font, "Runs on keybind", cx + 252, y + 5, 0xFF9A9AB0, true);
			y += 22;

			for (Feature.Setting s : selected.settings) {
				if (s instanceof Feature.Num n) {
					button(g, mx, my, cx, y, 190, 18, n.name + ": " + n.value + "  (L + / R -)", 0xFF2A2740, b -> {
						if (b == 0) n.nudge(1); else if (b == 1) n.nudge(-1);
					});
				} else if (s instanceof Feature.Bool bo) {
					button(g, mx, my, cx, y, 190, 18, bo.name + ": " + (bo.value ? "ON" : "OFF"), bo.value ? 0xFF2E7D32 : 0xFF2A2740, b -> bo.value = !bo.value);
				} else if (s instanceof Feature.Text t) {
					String shown = t.name + ": " + t.value + (typing == t && (System.currentTimeMillis() / 500) % 2 == 0 ? "_" : "");
					button(g, mx, my, cx, y, cw, 18, shown, typing == t ? 0xFF4B2E99 : 0xFF2A2740, b -> { typing = t; capturing = null; });
				}
				y += 22;
			}
		} else if (!list.isEmpty()) {
			g.drawString(font, "Right-click a module for its keybind and settings.", cx, sy + 4, 0xFF9A9AB0, true);
		}

		String footer = hoverDesc != null ? hoverDesc : "Left-click: toggle  |  Right-click: settings  |  Right Ctrl: close";
		g.drawString(font, footer, cx, py + ph - 14, 0xFF9A9AB0, true);
	}

	private void drawFriends(GuiGraphics g, int mx, int my, int cx, int py, int cw, int ph) {
		int y = py + 44;
		g.drawString(font, "Add a friend by username:", cx, y, 0xFFFFFFFF, true);
		y += 12;

		int fw = 200;
		g.fill(cx, y, cx + fw, y + 18, friendFocus ? 0xFF2B2150 : 0xFF1A1830);
		Draw.rect(g, cx, y, cx + fw, y + 18, friendFocus ? ACCENT : 0xFF403858);
		String txt = friendInput + (friendFocus && (System.currentTimeMillis() / 500) % 2 == 0 ? "_" : "");
		g.drawString(font, txt, cx + 4, y + 5, 0xFFFFFFFF, true);
		hits.add(new Hit(cx, y, fw, 18, b -> friendFocus = true));
		button(g, mx, my, cx + fw + 6, y, 60, 18, "Add", 0xFF2E7D32, b -> addFriend());

		y += 28;
		List<String> list = Friends.list();
		g.drawString(font, "Friends (" + list.size() + ") - middle-click a player in game to add / remove", cx, y, 0xFF9A9AB0, true);
		y += 14;

		int shown = 0;
		for (String name : list) {
			if (shown++ >= 8) break;
			g.fill(cx, y, cx + 250, y + 18, 0xFF1A1830);
			g.drawString(font, name, cx + 6, y + 5, 0xFF55FF55, true);
			button(g, mx, my, cx + 254, y, 70, 18, "Remove", 0xFF402A2A, b -> Friends.remove(name));
			y += 21;
		}
		if (list.size() > 8) g.drawString(font, "+" + (list.size() - 8) + " more", cx, y + 2, 0xFF9A9AB0, true);
	}

	private void addFriend() {
		if (friendInput.length() > 0) {
			Friends.add(friendInput.toString());
			friendInput.setLength(0);
			Config.save();
		}
	}

	// ---------------------------------------------------------------- input
	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		double mx = event.x(), my = event.y();
		int button = event.button();
		typing = null;
		friendFocus = false;
		capturing = null;

		for (int i = hits.size() - 1; i >= 0; i--) {
			Hit h = hits.get(i);
			if (mx >= h.x() && mx < h.x() + h.w() && my >= h.y() && my < h.y() + h.h()) {
				h.click().accept(button);
				return true;
			}
		}
		return true;
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		int k = event.key();

		if (capturing != null) {
			if (k == GLFW.GLFW_KEY_ESCAPE) capturing.key = -1;
			else if (k != GLFW.GLFW_KEY_RIGHT_CONTROL) capturing.key = k;
			capturing = null;
			return true;
		}
		if (typing != null) {
			if (k == GLFW.GLFW_KEY_ESCAPE || k == GLFW.GLFW_KEY_ENTER || k == GLFW.GLFW_KEY_KP_ENTER) typing = null;
			else if (k == GLFW.GLFW_KEY_BACKSPACE && !typing.value.isEmpty()) typing.value = typing.value.substring(0, typing.value.length() - 1);
			return true;
		}
		if (friendFocus) {
			if (k == GLFW.GLFW_KEY_ESCAPE) friendFocus = false;
			else if (k == GLFW.GLFW_KEY_ENTER || k == GLFW.GLFW_KEY_KP_ENTER) addFriend();
			else if (k == GLFW.GLFW_KEY_BACKSPACE && friendInput.length() > 0) friendInput.setLength(friendInput.length() - 1);
			return true;
		}
		if (k == GLFW.GLFW_KEY_RIGHT_CONTROL || k == GLFW.GLFW_KEY_ESCAPE) {
			onClose();
			return true;
		}
		return super.keyPressed(event);
	}

	@Override
	public boolean charTyped(CharacterEvent event) {
		int cp = event.codepoint();
		if (cp < 32 || cp == 127) return false;
		if (typing != null) {
			if (typing.value.length() < 60) typing.value += new String(Character.toChars(cp));
			return true;
		}
		if (friendFocus) {
			if ((Character.isLetterOrDigit(cp) || cp == '_') && friendInput.length() < 16) friendInput.appendCodePoint(cp);
			return true;
		}
		return false;
	}
}

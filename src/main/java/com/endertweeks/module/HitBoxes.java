package com.endertweeks.module;

import com.endertweeks.core.Feature;
import com.endertweeks.core.Friends;
import com.endertweeks.render.Draw;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

public class HitBoxes extends Feature {
	/** Read by EntityPickRadiusMixin. */
	public static volatile boolean active = false;
	public static volatile float size = 0.3f;

	private final Num sizeSetting = add(new Num("Size", 0.0, 1.0, 0.05, 0.3));
	private final Bool render = add(new Bool("Render", true));

	public HitBoxes() {
		super("Hit Boxes", "Makes other players easier to aim at (bigger target box). Render shows the box.", false);
	}

	@Override
	public void onEnable() { active = true; size = (float) sizeSetting.value; }

	@Override
	public void onDisable() { active = false; }

	@Override
	public void onTick() { size = (float) sizeSetting.value; }

	@Override
	public void onHud(GuiGraphics g, float partial) {
		if (!render.value) return;
		for (Player p : mc.level.players()) {
			if (p == mc.player || p.isSpectator() || Friends.is(p)) continue;
			AABB box = p.getBoundingBox().move(p.getPosition(partial).subtract(p.position())).inflate(size);
			Draw.box(g, box, 0xFFFFA500);
		}
	}
}

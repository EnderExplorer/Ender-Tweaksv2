package com.endertweeks.module;

import com.endertweeks.core.Feature;
import com.endertweeks.core.Util;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ShieldBreaker extends Feature {
	private final Num delay = add(new Num("Delay", 0, 10, 1, 0));
	private int back = -1;
	private int waited;

	public ShieldBreaker() {
		super("Shield Breaker", "If your sword is ready and the target raises a shield, swaps to an axe and hits it.", false);
	}

	@Override
	public void onTick() {
		LocalPlayer p = mc.player;
		if (p == null || mc.screen != null) return;

		Player target = Util.crosshairPlayer();
		if (target != null && target.isBlocking()) {
			ItemStack held = p.getMainHandItem();
			if (!held.is(ItemTags.AXES)) {
				if (!held.is(ItemTags.SWORDS) || p.getAttackStrengthScale(0f) < 1f) return;
				int axe = Util.findHotbar(s -> s.is(ItemTags.AXES));
				if (axe < 0) return;
				back = Util.selected();
				Util.select(axe);
				waited = 0;
				return;
			}
			if (p.getAttackStrengthScale(0f) >= 0.9f && waited++ >= delay.value) {
				Util.attack(target);
				waited = 0;
			}
			return;
		}

		waited = 0;
		if (back != -1) {
			Util.select(back);
			back = -1;
		}
	}

	@Override
	public void onDisable() {
		if (back != -1 && mc.player != null) Util.select(back);
		back = -1;
	}
}

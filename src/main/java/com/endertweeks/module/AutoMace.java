package com.endertweeks.module;

import com.endertweeks.core.Feature;
import com.endertweeks.core.Util;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;

public class AutoMace extends Feature {
	private final Num minFall = add(new Num("Min fall", 1.5, 10, 0.5, 2.0));
	private final Bool autoSwap = add(new Bool("Auto swap", true));
	private int back = -1;

	public AutoMace() {
		super("Auto Mace", "While falling onto a player you aim at: swaps to your mace and smashes.", false);
	}

	@Override
	public void onTick() {
		if (mc.player == null || mc.screen != null) return;
		Player t = Util.crosshairPlayer();

		if (t != null && !mc.player.onGround() && mc.player.fallDistance >= minFall.value) {
			if (!mc.player.getMainHandItem().is(Items.MACE)) {
				if (!autoSwap.value) return;
				int s = Util.findHotbar(Items.MACE);
				if (s < 0) return;
				back = Util.selected();
				Util.select(s);
				return;
			}
			if (mc.player.getAttackStrengthScale(0f) >= 1f) Util.attack(t);
		} else if (back != -1 && (mc.player.onGround() || t == null)) {
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

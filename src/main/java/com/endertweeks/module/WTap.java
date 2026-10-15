package com.endertweeks.module;

import com.endertweeks.core.Feature;
import com.endertweeks.core.Util;

import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;

public class WTap extends Feature {
	private final Num delay = add(new Num("Delay", 0, 5, 1, 0));
	private final Num hold = add(new Num("Hold", 1, 6, 1, 2));
	private int wait = -1;
	private int holding;

	public WTap() {
		super("W Tap", "Releases W for a few ticks after each hit so your sprint resets. Delay / Hold are in ticks.", false);
		AttackEntityCallback.EVENT.register((player, level, hand, entity, hit) -> {
			if (enabled && player == mc.player && entity instanceof LivingEntity && player.isSprinting()) {
				wait = (int) delay.value;
				holding = (int) hold.value;
			}
			return InteractionResult.PASS;
		});
	}

	@Override
	public void onTick() {
		if (mc.player == null || wait < 0) return;
		if (wait > 0) { wait--; return; }
		mc.options.keyUp.setDown(false);
		if (--holding <= 0) {
			wait = -1;
			Util.restoreKey(mc.options.keyUp);
		}
	}

	@Override
	public void onDisable() {
		if (wait >= 0 && mc.player != null) Util.restoreKey(mc.options.keyUp);
		wait = -1;
	}
}

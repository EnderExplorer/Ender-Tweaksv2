package com.endertweeks.module;

import com.endertweeks.core.Feature;
import com.endertweeks.core.Steps;
import com.endertweeks.core.Util;

import net.minecraft.world.item.Items;

public class AutoPearlCatch extends Feature {
	private final Num delay = add(new Num("Delay", 0, 10, 1, 1));
	private final Steps steps = new Steps();

	public AutoPearlCatch() {
		super("Auto Pearl Catch", "Keybind: throws a pearl straight up, then a wind charge up after it. Delay in ticks.", true);
	}

	@Override
	public void onTick() {
		steps.tick((int) delay.value);
	}

	@Override
	public void onKey() {
		if (mc.player == null || steps.busy()) return;
		int pearl = Util.findHotbar(Items.ENDER_PEARL);
		int wind = Util.findHotbar(Items.WIND_CHARGE);
		if (pearl < 0 || wind < 0) { Util.msg("Need an ender pearl and a wind charge in your hotbar"); return; }

		final int oldSlot = Util.selected();
		final float yaw = mc.player.getYRot(), pitch = mc.player.getXRot();

		steps.add(() -> { Util.select(pearl); Util.setRot(yaw, -90f); Util.useHeld(); });
		steps.add(() -> { Util.select(wind); Util.setRot(yaw, -90f); Util.useHeld(); });
		steps.add(() -> { Util.setRot(yaw, pitch); Util.select(oldSlot); });
	}
}

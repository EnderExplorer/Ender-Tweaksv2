package com.endertweeks.module;

import com.endertweeks.core.Feature;
import com.endertweeks.core.Steps;
import com.endertweeks.core.Util;

import net.minecraft.world.item.Items;

public class ShortWindCharge extends Feature {
	private final Num delay = add(new Num("Delay", 0, 10, 1, 2));
	private final Bool autoJump = add(new Bool("Auto jump", true));
	private final Steps steps = new Steps();

	public ShortWindCharge() {
		super("Short Wind Charge", "Keybind: jump and fire a wind charge at your feet to launch upward. Delay in ticks.", true);
	}

	@Override
	public void onTick() {
		steps.tick((int) delay.value);
	}

	@Override
	public void onKey() {
		if (mc.player == null || steps.busy()) return;
		int wind = Util.findHotbar(Items.WIND_CHARGE);
		if (wind < 0) { Util.msg("Need a wind charge in your hotbar"); return; }

		final int oldSlot = Util.selected();
		final float yaw = mc.player.getYRot(), pitch = mc.player.getXRot();
		final boolean jump = autoJump.value;

		steps.add(() -> { if (jump) mc.options.keyJump.setDown(true); });
		steps.add(() -> { Util.select(wind); Util.setRot(yaw, 90f); Util.useHeld(); });
		steps.add(() -> {
			if (jump) Util.restoreKey(mc.options.keyJump);
			Util.setRot(yaw, pitch);
			Util.select(oldSlot);
		});
	}
}

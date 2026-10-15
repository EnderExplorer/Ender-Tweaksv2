package com.endertweeks.module;

import com.endertweeks.core.Feature;
import com.endertweeks.core.Steps;
import com.endertweeks.core.Util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class AutoBowCart extends Feature {
	private final Num charge = add(new Num("Charge ticks", 3, 20, 1, 3));
	private final Num aim = add(new Num("Aim up (deg)", 30, 90, 5, 80));
	private final Steps steps = new Steps();

	public AutoBowCart() {
		super("Auto Bow Cart", "Keybind: shoots an arrow up with a short charge, then places a rail + TNT minecart where it lands.", true);
	}

	@Override
	public void onTick() {
		steps.tick();
	}

	/** Simulates the arrow and returns the block it lands on (null if it never lands). */
	private BlockHitResult simulate(float yaw, float pitch, int chargeTicks) {
		double f = chargeTicks / 20.0;
		f = (f * f + f * 2.0) / 3.0;
		if (f < 0.1) return null;                 // too weak, a bow will not even fire
		if (f > 1.0) f = 1.0;
		double speed = f * 3.0;

		double yr = Math.toRadians(yaw), pr = Math.toRadians(pitch);
		Vec3 v = new Vec3(-Math.sin(yr) * Math.cos(pr), -Math.sin(pr), Math.cos(yr) * Math.cos(pr)).scale(speed);
		Vec3 pos = mc.player.getEyePosition().add(0, -0.1, 0);

		for (int i = 0; i < 300; i++) {
			Vec3 next = pos.add(v);
			BlockHitResult hit = mc.level.clip(new ClipContext(pos, next, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, mc.player));
			if (hit.getType() == HitResult.Type.BLOCK) return hit;
			pos = next;
			v = v.scale(0.99).add(0, -0.05, 0);
		}
		return null;
	}

	@Override
	public void onKey() {
		if (mc.player == null || mc.level == null || steps.busy()) return;
		int bow = Util.findHotbar(Items.BOW);
		int rail = Util.findHotbar(Items.RAIL);
		int cart = Util.findHotbar(Items.TNT_MINECART);
		if (bow < 0 || rail < 0 || cart < 0) { Util.msg("Need a bow, rails and a TNT minecart in your hotbar"); return; }

		final float yaw = mc.player.getYRot(), oldPitch = mc.player.getXRot();
		final float pitch = (float) -aim.value;
		final int ticks = (int) charge.value;

		BlockHitResult land = simulate(yaw, pitch, ticks);
		if (land == null || land.getDirection() != Direction.UP) { Util.msg("Arrow would not land on flat ground"); return; }
		final BlockPos ground = land.getBlockPos();
		if (mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf(ground)) > 4.5 * 4.5) { Util.msg("Landing spot is out of reach"); return; }

		final int oldSlot = Util.selected();
		final BlockPos railPos = ground.above();

		steps.add(() -> { Util.select(bow); Util.setRot(yaw, pitch); mc.options.keyUse.setDown(true); });
		for (int i = 1; i < ticks; i++) steps.add(() -> {});
		steps.add(() -> Util.restoreKey(mc.options.keyUse)); // letting go fires the arrow
		steps.add(() -> { Util.select(rail); Util.useOnBlock(ground, Direction.UP); });
		steps.add(() -> { Util.select(cart); Util.useOnBlock(railPos, Direction.UP); });
		steps.add(() -> { Util.setRot(yaw, oldPitch); Util.select(oldSlot); });
	}
}

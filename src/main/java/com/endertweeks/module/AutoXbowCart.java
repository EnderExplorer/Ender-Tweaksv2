package com.endertweeks.module;

import com.endertweeks.core.Feature;
import com.endertweeks.core.Steps;
import com.endertweeks.core.Util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class AutoXbowCart extends Feature {
	private final Num delay = add(new Num("Delay", 0, 10, 1, 1));
	private final Steps steps = new Steps();

	public AutoXbowCart() {
		super("Auto XBow Cart", "Keybind: rail + TNT minecart on the ground you look at, flint & steel in front, then a crossbow shot through the fire.", true);
	}

	@Override
	public void onTick() {
		steps.tick((int) delay.value);
	}

	private static boolean loaded(ItemStack s) {
		ChargedProjectiles cp = s.get(DataComponents.CHARGED_PROJECTILES);
		return cp != null && !cp.isEmpty();
	}

	@Override
	public void onKey() {
		if (mc.player == null || mc.level == null || steps.busy()) return;
		if (!(mc.hitResult instanceof BlockHitResult hit) || hit.getType() != HitResult.Type.BLOCK || hit.getDirection() != Direction.UP) {
			Util.msg("Look at the top of a block");
			return;
		}

		int rail = Util.findHotbar(Items.RAIL);
		int cart = Util.findHotbar(Items.TNT_MINECART);
		int flint = Util.findHotbar(Items.FLINT_AND_STEEL);
		int xbow = Util.findHotbar(s -> s.is(Items.CROSSBOW) && loaded(s));
		if (rail < 0 || cart < 0 || flint < 0 || xbow < 0) {
			Util.msg("Need rails, TNT minecart, flint & steel and a LOADED crossbow in your hotbar");
			return;
		}

		final BlockPos ground = hit.getBlockPos();
		final BlockPos railPos = ground.above();
		Direction toward = Direction.getApproximateNearest(mc.player.getX() - railPos.getX() - 0.5, 0, mc.player.getZ() - railPos.getZ() - 0.5);
		final BlockPos fireGround = railPos.relative(toward).below();
		final Vec3 cartCenter = Vec3.atCenterOf(railPos);

		final int oldSlot = Util.selected();
		final float yaw = mc.player.getYRot(), pitch = mc.player.getXRot();

		steps.add(() -> { Util.select(rail); Util.useOnBlock(ground, Direction.UP); });
		steps.add(() -> { Util.select(cart); Util.useOnBlock(railPos, Direction.UP); });
		steps.add(() -> { Util.select(flint); Util.useOnBlock(fireGround, Direction.UP); });
		steps.add(() -> { Util.select(xbow); Util.lookAt(cartCenter); Util.useHeld(); });
		steps.add(() -> { Util.setRot(yaw, pitch); Util.select(oldSlot); });
	}
}

package com.endertweeks.module;

import com.endertweeks.core.Feature;
import com.endertweeks.core.Util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class AutoCrystal extends Feature {
	private final Num delay = add(new Num("Delay", 0, 10, 1, 1));
	private final Bool place = add(new Bool("Place", true));
	private final Bool explode = add(new Bool("Explode", true));
	private int timer;

	public AutoCrystal() {
		super("Auto Crystal", "Places and explodes end crystals on the obsidian/bedrock you look at. Delay in ticks.", false);
	}

	@Override
	public void onTick() {
		if (mc.player == null || mc.level == null || mc.screen != null) return;
		if (timer > 0) { timer--; return; }

		if (explode.value && mc.hitResult instanceof EntityHitResult r && r.getEntity().getType() == EntityType.END_CRYSTAL) {
			Util.attack(r.getEntity());
			timer = (int) delay.value;
			return;
		}

		if (place.value && mc.player.getMainHandItem().is(Items.END_CRYSTAL)
				&& mc.hitResult instanceof BlockHitResult b && b.getType() == HitResult.Type.BLOCK
				&& b.getDirection() == Direction.UP) {
			BlockPos pos = b.getBlockPos();
			BlockState st = mc.level.getBlockState(pos);
			if ((st.is(Blocks.OBSIDIAN) || st.is(Blocks.BEDROCK)) && mc.level.getBlockState(pos.above()).isAir()) {
				mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, b);
				mc.player.swing(InteractionHand.MAIN_HAND);
				timer = (int) delay.value;
			}
		}
	}
}

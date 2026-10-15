package com.endertweeks.module;

import com.endertweeks.core.Feature;
import com.endertweeks.core.Friends;
import com.endertweeks.core.Steps;
import com.endertweeks.core.Util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;

public class AutoHitCrystal extends Feature {
	private final Num delay = add(new Num("Delay", 0, 10, 1, 1));
	private final Steps steps = new Steps();

	public AutoHitCrystal() {
		super("Auto Hit Crystal", "Keybind: obsidian next to the target's feet, crystal on it, then hit it. Delay in ticks.", true);
	}

	@Override
	public void onTick() {
		steps.tick((int) delay.value);
	}

	private Player findTarget() {
		Player t = Util.crosshairPlayer();
		if (t != null) return t;
		Player best = null;
		double bd = 25;
		for (Player p : mc.level.players()) {
			if (p == mc.player || p.isSpectator() || Friends.is(p)) continue;
			double d = mc.player.distanceToSqr(p);
			if (d < bd) { bd = d; best = p; }
		}
		return best;
	}

	@Override
	public void onKey() {
		if (mc.player == null || mc.level == null || steps.busy()) return;
		Player t = findTarget();
		if (t == null) { Util.msg("No target nearby"); return; }

		int obs = Util.findHotbar(Items.OBSIDIAN);
		int cry = Util.findHotbar(Items.END_CRYSTAL);
		if (obs < 0 || cry < 0) { Util.msg("Need obsidian and end crystals in your hotbar"); return; }

		Direction side = Direction.getApproximateNearest(mc.player.getX() - t.getX(), 0, mc.player.getZ() - t.getZ());
		final BlockPos spot = t.blockPosition().relative(side);
		if (mc.level.getBlockState(spot.below()).isAir() || !mc.level.getBlockState(spot).isAir()) {
			Util.msg("No room next to the target");
			return;
		}

		steps.add(() -> { Util.select(obs); Util.useOnBlock(spot.below(), Direction.UP); });
		steps.add(() -> { Util.select(cry); Util.useOnBlock(spot, Direction.UP); });
		steps.add(() -> {
			for (EndCrystal c : mc.level.getEntitiesOfClass(EndCrystal.class, new AABB(spot.above()).inflate(1.0))) {
				Util.attack(c);
				break;
			}
		});
	}
}

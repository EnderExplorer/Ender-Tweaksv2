package com.endertweeks.module;

import com.endertweeks.core.Feature;
import com.endertweeks.core.Friends;
import com.endertweeks.core.Util;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class AimAssist extends Feature {
	private final Num speed = add(new Num("Speed", 1, 30, 1, 6));
	private final Num fov = add(new Num("FOV", 10, 180, 5, 60));
	private final Num range = add(new Num("Range", 3, 8, 0.5, 4.5));
	private final Bool predict = add(new Bool("Predictions", true));
	private final Bool sticky = add(new Bool("Sticky", true));
	private final Bool weaponOnly = add(new Bool("Weapon only", true));
	private final Bool attackOnly = add(new Bool("Only while attacking", true));
	private Player current;

	public AimAssist() {
		super("Aim Assist", "Smoothly pulls your aim toward the nearest player inside the FOV.", false);
	}

	private boolean valid(LocalPlayer me, Player t) {
		return t != me && t.isAlive() && !t.isSpectator() && !Friends.is(t) && me.distanceTo(t) <= range.value;
	}

	@Override
	public void onTick() {
		LocalPlayer p = mc.player;
		if (p == null || mc.level == null || mc.screen != null) return;
		if (weaponOnly.value && !(p.getMainHandItem().is(ItemTags.SWORDS) || p.getMainHandItem().is(ItemTags.AXES))) return;
		if (attackOnly.value && !mc.options.keyAttack.isDown()) return;

		Player target = null;
		if (sticky.value && current != null && valid(p, current)) target = current;

		if (target == null) {
			double best = fov.value / 2.0;
			for (Player t : mc.level.players()) {
				if (!valid(p, t)) continue;
				float[] r = Util.rotsTo(t.position().add(0, t.getBbHeight() * 0.7, 0));
				double ang = Math.hypot(Mth.wrapDegrees(r[0] - p.getYRot()), r[1] - p.getXRot());
				if (ang <= best) { best = ang; target = t; }
			}
		}
		current = target;
		if (target == null) return;

		Vec3 aim = target.position().add(0, target.getBbHeight() * 0.7, 0);
		if (predict.value) aim = aim.add((target.getX() - target.xo) * 3, 0, (target.getZ() - target.zo) * 3);

		float[] r = Util.rotsTo(aim);
		float s = (float) speed.value;
		float dy = Mth.clamp(Mth.wrapDegrees(r[0] - p.getYRot()), -s, s);
		float dp = Mth.clamp(r[1] - p.getXRot(), -s, s);
		Util.setRot(p.getYRot() + dy, p.getXRot() + dp);
	}

	@Override
	public void onDisable() { current = null; }
}

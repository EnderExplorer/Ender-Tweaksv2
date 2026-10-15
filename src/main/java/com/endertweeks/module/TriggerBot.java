package com.endertweeks.module;

import com.endertweeks.core.Feature;
import com.endertweeks.core.Features;
import com.endertweeks.core.Friends;
import com.endertweeks.core.Util;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;

public class TriggerBot extends Feature {
	private final Num delay = add(new Num("Delay", 0, 10, 1, 0));
	private final Bool crits = add(new Bool("Crits", false));
	private final Bool mobs = add(new Bool("Hit mobs", false));
	private int ready;
	private boolean jumped;

	public TriggerBot() {
		super("Trigger Bot", "Hits what you aim at when holding a sword/axe and the cooldown is ready. Delay is in ticks.", false);
	}

	@Override
	public void onTick() {
		LocalPlayer p = mc.player;
		if (p == null) return;
		if (jumped) { Util.restoreKey(mc.options.keyJump); jumped = false; }
		if (mc.screen != null || p.isUsingItem()) return;

		ItemStack held = p.getMainHandItem();
		if (!(held.is(ItemTags.SWORDS) || held.is(ItemTags.AXES))) return;
		if (!(mc.hitResult instanceof EntityHitResult r)) { ready = 0; return; }

		Entity e = r.getEntity();
		if (!e.isAlive()) return;

		if (e instanceof Player target) {
			if (target == p || target.isSpectator() || Friends.is(target)) return;
			ShieldBreaker sb = Features.get(ShieldBreaker.class);
			if (sb != null && sb.enabled && target.isBlocking()) return;
		} else if (!(mobs.value && e instanceof LivingEntity)) {
			return;
		}

		if (p.getAttackStrengthScale(0f) < 1f) { ready = 0; return; }

		if (crits.value) {
			if (p.onGround()) {
				mc.options.keyJump.setDown(true); // jump now, hit on the way down
				jumped = true;
				return;
			}
			if (!(p.fallDistance > 0 && !p.isInWater() && !p.onClimbable())) return;
		}

		if (ready++ < delay.value) return;
		ready = 0;
		Util.attack(e);
	}

	@Override
	public void onDisable() {
		if (jumped && mc.player != null) Util.restoreKey(mc.options.keyJump);
		jumped = false;
	}
}

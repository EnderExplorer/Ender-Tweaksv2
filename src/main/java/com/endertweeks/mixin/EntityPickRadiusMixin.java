package com.endertweeks.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.endertweeks.core.Friends;
import com.endertweeks.module.HitBoxes;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

@Mixin(Entity.class)
public abstract class EntityPickRadiusMixin {
	@Inject(method = "getPickRadius", at = @At("HEAD"), cancellable = true)
	private void endertweeks$bigger(CallbackInfoReturnable<Float> cir) {
		if (!HitBoxes.active) return;
		Entity self = (Entity) (Object) this;
		if (self instanceof Player p && p != Minecraft.getInstance().player && !Friends.is(p)) {
			cir.setReturnValue(HitBoxes.size);
		}
	}
}

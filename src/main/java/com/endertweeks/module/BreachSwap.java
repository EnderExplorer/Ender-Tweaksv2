package com.endertweeks.module;

import com.endertweeks.core.Feature;
import com.endertweeks.core.Util;

import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;

public class BreachSwap extends Feature {
	private final Bool requireBreach = add(new Bool("Require Breach", true));
	private boolean pending;

	public BreachSwap() {
		super("Breach Swap", "After you hit with a sword, swaps to your Breach mace.", false);
		AttackEntityCallback.EVENT.register((player, level, hand, entity, hit) -> {
			if (enabled && player == mc.player && entity instanceof LivingEntity && player.getMainHandItem().is(ItemTags.SWORDS)) {
				pending = true;
			}
			return InteractionResult.PASS;
		});
	}

	private boolean hasBreach(ItemStack s) {
		return s.getEnchantments().keySet().stream().anyMatch(h -> h.is(Enchantments.BREACH));
	}

	@Override
	public void onTick() {
		if (!pending || mc.player == null) return;
		pending = false;
		int slot = Util.findHotbar(s -> s.is(Items.MACE) && (!requireBreach.value || hasBreach(s)));
		if (slot >= 0) Util.select(slot);
	}
}

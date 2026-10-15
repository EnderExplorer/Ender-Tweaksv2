package com.endertweeks.module;

import com.endertweeks.core.Feature;

import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.Items;

public class AutoTotem extends Feature {
	private final Num delay = add(new Num("Delay", 0, 10, 1, 0));
	private int wait;

	public AutoTotem() {
		super("Auto Totem", "Refills your offhand with a totem from your inventory as soon as it is empty. Delay in ticks.", false);
	}

	@Override
	public void onTick() {
		if (mc.player == null || mc.gameMode == null) return;
		if (mc.screen != null && !(mc.screen instanceof InventoryScreen)) return;
		if (wait > 0) { wait--; return; }
		if (mc.player.getOffhandItem().is(Items.TOTEM_OF_UNDYING)) return;

		for (int i = 0; i < 36; i++) {
			if (!mc.player.getInventory().getItem(i).is(Items.TOTEM_OF_UNDYING)) continue;
			int menuSlot = i < 9 ? 36 + i : i; // hotbar slots are 36-44 in the inventory menu
			mc.gameMode.handleInventoryMouseClick(mc.player.inventoryMenu.containerId, menuSlot, 40, ClickType.SWAP, mc.player);
			wait = (int) delay.value + 1;
			return;
		}
	}
}

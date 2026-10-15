package com.endertweeks.core;

import java.util.ArrayList;
import java.util.List;

import com.endertweeks.module.*;

public final class Features {
	public static final List<Feature> ALL = new ArrayList<>();

	private Features() {}

	private static void reg(String category, Feature f) {
		f.category = category;
		ALL.add(f);
	}

	public static void init() {
		// Sword
		reg("Sword", new TriggerBot());
		reg("Sword", new ShieldBreaker());
		reg("Sword", new WTap());
		reg("Sword", new AimAssist());
		reg("Sword", new HitBoxes());
		// Crystal
		reg("Crystal", new AutoCrystal());
		reg("Crystal", new AutoHitCrystal());
		reg("Crystal", new AutoTotem());
		reg("Crystal", new AnchorHelper());
		reg("Crystal", new SafeAnchor());
		reg("Crystal", new WebDrainer());
		reg("Crystal", new WebPlacer());
		reg("Crystal", new HoverTotem());
		// Mace
		reg("Mace", new AutoMace());
		reg("Mace", new AutoPearlCatch());
		reg("Mace", new BreachSwap());
		reg("Mace", new ShortWindCharge());
		// Cart
		reg("Cart", new AutoBowCart());
		reg("Cart", new AutoXbowCart());
		// Visuals
		reg("Visuals", new Tracers());
		reg("Visuals", new NameTags());
		reg("Visuals", new BlockEsp());
		reg("Visuals", new Xray());
		reg("Visuals", new StorageEsp());
		reg("Visuals", new Logouts());
		// Misc
		reg("Misc", new Flight());
		reg("Misc", new FastBreak());
	}

	public static List<Feature> in(String category) {
		List<Feature> out = new ArrayList<>();
		for (Feature f : ALL) {
			if (f.category.equals(category)) out.add(f);
		}
		return out;
	}

	@SuppressWarnings("unchecked")
	public static <T extends Feature> T get(Class<T> type) {
		for (Feature f : ALL) {
			if (type.isInstance(f)) return (T) f;
		}
		return null;
	}
}

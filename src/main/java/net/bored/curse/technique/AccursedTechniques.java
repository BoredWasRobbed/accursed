package net.bored.curse.technique;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import net.bored.curse.CurseTechnique;
import net.minecraft.nbt.CompoundTag;

public final class AccursedTechniques {
	private static final Map<String, Supplier<CurseTechnique>> TECHNIQUES = new HashMap<>();

	private AccursedTechniques() {
	}

	public static void registerDefaults() {
		register("shrine", ShrineTechnique::new);
	}

	public static void register(String id, Supplier<CurseTechnique> factory) {
		TECHNIQUES.put(id, factory);
	}

	public static Optional<CurseTechnique> create(String id) {
		Supplier<CurseTechnique> factory = TECHNIQUES.get(id);
		return factory == null ? Optional.empty() : Optional.of(factory.get());
	}

	public static Optional<CurseTechnique> load(CompoundTag tag) {
		return create(tag.getString("id"));
	}
}

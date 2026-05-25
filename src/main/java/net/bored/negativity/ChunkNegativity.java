package net.bored.negativity;

import net.minecraft.nbt.CompoundTag;

public class ChunkNegativity {
	private double value;
	private long lastUpdated;

	public ChunkNegativity(double value, long lastUpdated) {
		this.value = value;
		this.lastUpdated = lastUpdated;
	}

	public double value(long gameTime) {
		long elapsed = Math.max(0L, gameTime - lastUpdated);
		return Math.max(0.0D, value - elapsed * 0.002D);
	}

	public void add(double amount, long gameTime) {
		value = Math.min(100.0D, value(gameTime) + amount);
		lastUpdated = gameTime;
	}

	public CompoundTag save(long key) {
		CompoundTag tag = new CompoundTag();
		tag.putLong("chunk", key);
		tag.putDouble("value", value);
		tag.putLong("last_updated", lastUpdated);
		return tag;
	}

	public static ChunkNegativity load(CompoundTag tag) {
		return new ChunkNegativity(tag.getDouble("value"), tag.getLong("last_updated"));
	}
}

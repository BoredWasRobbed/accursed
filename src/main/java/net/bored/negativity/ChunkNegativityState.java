package net.bored.negativity;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;

public class ChunkNegativityState extends SavedData {
	private static final String DATA_NAME = "accursed_chunk_negativity";
	private static final Factory<ChunkNegativityState> FACTORY = new Factory<>(ChunkNegativityState::new, ChunkNegativityState::load, DataFixTypes.LEVEL);
	private final Map<Long, ChunkNegativity> chunks = new HashMap<>();

	public static ChunkNegativityState get(ServerLevel world) {
		return world.getDataStorage().computeIfAbsent(FACTORY, DATA_NAME);
	}

	public void add(BlockPosLike pos, double amount, long gameTime) {
		long key = ChunkPos.asLong(pos.blockX(), pos.blockZ());
		chunks.computeIfAbsent(key, ignored -> new ChunkNegativity(0.0D, gameTime)).add(amount, gameTime);
		setDirty();
	}

	public double valueAt(BlockPosLike pos, long gameTime) {
		ChunkNegativity negativity = chunks.get(ChunkPos.asLong(pos.blockX(), pos.blockZ()));
		return negativity == null ? 0.0D : negativity.value(gameTime);
	}

	public void prune(long gameTime) {
		Iterator<Map.Entry<Long, ChunkNegativity>> iterator = chunks.entrySet().iterator();
		while (iterator.hasNext()) {
			if (iterator.next().getValue().value(gameTime) <= 0.01D) {
				iterator.remove();
				setDirty();
			}
		}
	}

	@Override
	public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
		ListTag chunkList = new ListTag();
		for (Map.Entry<Long, ChunkNegativity> entry : chunks.entrySet()) {
			chunkList.add(entry.getValue().save(entry.getKey()));
		}
		tag.put("chunks", chunkList);
		return tag;
	}

	private static ChunkNegativityState load(CompoundTag tag, HolderLookup.Provider registries) {
		ChunkNegativityState state = new ChunkNegativityState();
		ListTag chunkList = tag.getList("chunks", Tag.TAG_COMPOUND);
		for (int i = 0; i < chunkList.size(); i++) {
			CompoundTag chunkTag = chunkList.getCompound(i);
			state.chunks.put(chunkTag.getLong("chunk"), ChunkNegativity.load(chunkTag));
		}
		return state;
	}

	public interface BlockPosLike {
		int blockX();

		int blockZ();
	}
}

package net.bored.curse;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.bored.network.AccursedNetworking;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;

public class CursePlayerState extends SavedData {
	private static final String DATA_NAME = "accursed_players";
	private static final Factory<CursePlayerState> FACTORY = new Factory<>(CursePlayerState::new, CursePlayerState::load, DataFixTypes.PLAYER);
	private final Map<UUID, CursePlayerData> players = new HashMap<>();

	public static void registerEvents() {
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			CursePlayerData data = get(server).get(handler.player);
			AccursedNetworking.sync(handler.player, data);
		});

		ServerTickEvents.END_SERVER_TICK.register(server -> {
			CursePlayerState state = get(server);
			for (ServerPlayer player : server.getPlayerList().getPlayers()) {
				CursePlayerData data = state.get(player);
				data.tick(player);
				if (player.tickCount % 20 == 0) {
					AccursedNetworking.sync(player, data);
				}
			}
			state.setDirty();
		});
	}

	public static CursePlayerState get(MinecraftServer server) {
		return server.overworld().getDataStorage().computeIfAbsent(FACTORY, DATA_NAME);
	}

	public CursePlayerData get(ServerPlayer player) {
		return players.computeIfAbsent(player.getUUID(), CursePlayerData::new);
	}

	public void update(ServerPlayer player, CursePlayerData data) {
		players.put(player.getUUID(), data);
		setDirty();
	}

	@Override
	public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
		ListTag playerList = new ListTag();
		for (CursePlayerData data : players.values()) {
			playerList.add(data.save());
		}
		tag.put("players", playerList);
		return tag;
	}

	private static CursePlayerState load(CompoundTag tag, HolderLookup.Provider registries) {
		CursePlayerState state = new CursePlayerState();
		ListTag playerList = tag.getList("players", Tag.TAG_COMPOUND);
		for (int i = 0; i < playerList.size(); i++) {
			CursePlayerData data = CursePlayerData.load(playerList.getCompound(i));
			state.players.put(data.playerId(), data);
		}
		return state;
	}
}

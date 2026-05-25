package net.bored.network;

import java.util.ArrayList;
import java.util.List;
import net.bored.Accursed;
import net.bored.client.AbilityView;
import net.bored.client.ClientAbilityState;
import net.bored.curse.CurseAbility;
import net.bored.curse.CursePlayerData;
import net.bored.curse.CursePlayerState;
import net.bored.curse.CurseTechnique;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public final class AccursedNetworking {
	private static boolean payloadsRegistered;

	private AccursedNetworking() {
	}

	public static void registerPayloads() {
		if (payloadsRegistered) {
			return;
		}

		PayloadTypeRegistry.playC2S().register(UseAbilityPayload.TYPE, UseAbilityPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(SelectAbilityPayload.TYPE, SelectAbilityPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(AbilityStatePayload.TYPE, AbilityStatePayload.CODEC);
		payloadsRegistered = true;
	}

	public static void registerServer() {
		registerPayloads();

		ServerPlayNetworking.registerGlobalReceiver(UseAbilityPayload.TYPE, (payload, context) -> {
			ServerPlayer player = context.player();
			CursePlayerState state = CursePlayerState.get(context.server());
			CursePlayerData data = state.get(player);
			data.useSelectedAbility(player);
			state.update(player, data);
			sync(player, data);
		});

		ServerPlayNetworking.registerGlobalReceiver(SelectAbilityPayload.TYPE, (payload, context) -> {
			ServerPlayer player = context.player();
			CursePlayerState state = CursePlayerState.get(context.server());
			CursePlayerData data = state.get(player);
			data.selectAbility(payload.index());
			state.update(player, data);
			sync(player, data);
		});
	}

	public static void registerClient() {
		registerPayloads();

		ClientPlayNetworking.registerGlobalReceiver(AbilityStatePayload.TYPE, (payload, context) -> context.client().execute(() -> {
			long tick = context.client().level == null ? 0L : context.client().level.getGameTime();
			ClientAbilityState.updateFromServer(payload.cursedEnergy(), payload.maxCursedEnergy(), payload.selectedIndex(), payload.abilities(), tick);
		}));
	}

	public static void sendUseAbility() {
		ClientPlayNetworking.send(new UseAbilityPayload());
	}

	public static void sendSelectAbility(int index) {
		ClientPlayNetworking.send(new SelectAbilityPayload(index));
	}

	public static void sync(ServerPlayer player, CursePlayerData data) {
		List<AbilityView> abilityViews = new ArrayList<>();
		long gameTime = player.serverLevel().getGameTime();
		for (CurseTechnique technique : data.techniques()) {
			for (CurseAbility ability : technique.abilities()) {
				abilityViews.add(new AbilityView(technique.id(), ability.id(), ability.displayName(), data.remainingCooldown(ability, gameTime), ability.cooldownTicks()));
			}
		}

		ServerPlayNetworking.send(player, new AbilityStatePayload(data.cursedEnergy(), data.maxCursedEnergy(), data.selectedAbilityIndex(), abilityViews));
	}

	private static ResourceLocation id(String path) {
		return ResourceLocation.fromNamespaceAndPath(Accursed.MOD_ID, path);
	}

	public record UseAbilityPayload() implements CustomPacketPayload {
		public static final Type<UseAbilityPayload> TYPE = new Type<>(id("use_ability"));
		public static final StreamCodec<RegistryFriendlyByteBuf, UseAbilityPayload> CODEC = StreamCodec.unit(new UseAbilityPayload());

		@Override
		public Type<? extends CustomPacketPayload> type() {
			return TYPE;
		}
	}

	public record SelectAbilityPayload(int index) implements CustomPacketPayload {
		public static final Type<SelectAbilityPayload> TYPE = new Type<>(id("select_ability"));
		public static final StreamCodec<RegistryFriendlyByteBuf, SelectAbilityPayload> CODEC = StreamCodec.ofMember(SelectAbilityPayload::write, SelectAbilityPayload::read);

		private void write(RegistryFriendlyByteBuf buf) {
			buf.writeVarInt(index);
		}

		private static SelectAbilityPayload read(RegistryFriendlyByteBuf buf) {
			return new SelectAbilityPayload(buf.readVarInt());
		}

		@Override
		public Type<? extends CustomPacketPayload> type() {
			return TYPE;
		}
	}

	public record AbilityStatePayload(double cursedEnergy, double maxCursedEnergy, int selectedIndex, List<AbilityView> abilities) implements CustomPacketPayload {
		public static final Type<AbilityStatePayload> TYPE = new Type<>(id("ability_state"));
		public static final StreamCodec<RegistryFriendlyByteBuf, AbilityStatePayload> CODEC = StreamCodec.ofMember(AbilityStatePayload::write, AbilityStatePayload::read);

		private void write(RegistryFriendlyByteBuf buf) {
			buf.writeDouble(cursedEnergy);
			buf.writeDouble(maxCursedEnergy);
			buf.writeVarInt(selectedIndex);
			buf.writeVarInt(abilities.size());
			for (AbilityView ability : abilities) {
				buf.writeUtf(ability.techniqueId());
				buf.writeUtf(ability.abilityId());
				buf.writeUtf(ability.name());
				buf.writeVarInt(ability.remainingCooldownTicks());
				buf.writeVarInt(ability.cooldownTicks());
			}
		}

		private static AbilityStatePayload read(RegistryFriendlyByteBuf buf) {
			double cursedEnergy = buf.readDouble();
			double maxCursedEnergy = buf.readDouble();
			int selectedIndex = buf.readVarInt();
			int abilityCount = buf.readVarInt();
			List<AbilityView> abilities = new ArrayList<>(abilityCount);
			for (int i = 0; i < abilityCount; i++) {
				abilities.add(new AbilityView(buf.readUtf(), buf.readUtf(), buf.readUtf(), buf.readVarInt(), buf.readVarInt()));
			}
			return new AbilityStatePayload(cursedEnergy, maxCursedEnergy, selectedIndex, abilities);
		}

		@Override
		public Type<? extends CustomPacketPayload> type() {
			return TYPE;
		}
	}
}

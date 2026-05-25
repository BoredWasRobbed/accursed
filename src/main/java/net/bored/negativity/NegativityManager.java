package net.bored.negativity;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.phys.AABB;

public final class NegativityManager {
	private NegativityManager() {
	}

	public static void registerEvents() {
		ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
			if (entity.level() instanceof ServerLevel world && isPassiveOrCreature(entity)) {
				addNegativity(world, entity.blockPosition(), 12.0D);
			}
		});

		ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
			if (entity instanceof LivingEntity living && entity instanceof Enemy) {
				amplifyHostile(living, world);
			}
		});

		ServerTickEvents.END_WORLD_TICK.register(world -> {
			if (world.getGameTime() % 100L == 0L) {
				ChunkNegativityState.get(world).prune(world.getGameTime());
				refreshNearbyHostiles(world);
			}
		});
	}

	public static void recordBurning(ServerLevel world, BlockPos pos, double amount) {
		addNegativity(world, pos, amount);
	}

	private static void addNegativity(ServerLevel world, BlockPos pos, double amount) {
		ChunkNegativityState.get(world).add(new BlockLike(pos), amount, world.getGameTime());
	}

	private static boolean isPassiveOrCreature(LivingEntity entity) {
		MobCategory category = entity.getType().getCategory();
		return category == MobCategory.CREATURE || category == MobCategory.AMBIENT || category == MobCategory.WATER_CREATURE || category == MobCategory.WATER_AMBIENT;
	}

	private static void refreshNearbyHostiles(ServerLevel world) {
		for (var player : world.players()) {
			AABB area = player.getBoundingBox().inflate(96.0D);
			for (Entity entity : world.getEntities(player, area, candidate -> candidate instanceof LivingEntity && candidate instanceof Enemy)) {
				if (entity instanceof LivingEntity living) {
					amplifyHostile(living, world);
				}
			}
		}
	}

	private static void amplifyHostile(LivingEntity entity, ServerLevel world) {
		double negativity = ChunkNegativityState.get(world).valueAt(new BlockLike(entity.blockPosition()), world.getGameTime());
		if (negativity <= 2.0D) {
			return;
		}

		int amplifier = Math.min(2, (int) (negativity / 28.0D));
		entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 140, amplifier, true, false, true));
		entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 140, Math.max(0, amplifier - 1), true, false, true));
		if (negativity >= 45.0D) {
			entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 140, 0, true, false, true));
		}
	}

	private record BlockLike(BlockPos pos) implements ChunkNegativityState.BlockPosLike {
		@Override
		public int blockX() {
			return pos.getX();
		}

		@Override
		public int blockZ() {
			return pos.getZ();
		}
	}
}

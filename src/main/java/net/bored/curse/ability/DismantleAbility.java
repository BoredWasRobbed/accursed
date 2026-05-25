package net.bored.curse.ability;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.bored.curse.AbilityUseContext;
import net.bored.curse.CurseAbility;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class DismantleAbility implements CurseAbility {
	private static final double RANGE = 18.0D;
	private static final double STEP = 0.35D;
	private static final float ENTITY_DAMAGE = 9.0F;
	private static final float BLOCK_CUT_BUDGET = 26.0F;

	@Override
	public String id() {
		return "dismantle";
	}

	@Override
	public String displayName() {
		return "Dismantle";
	}

	@Override
	public int cooldownTicks() {
		return 60;
	}

	@Override
	public double cursedEnergyCost() {
		return 30.0D;
	}

	@Override
	public boolean activate(AbilityUseContext context) {
		ServerPlayer player = context.player();
		ServerLevel world = player.serverLevel();
		Vec3 start = player.getEyePosition();
		Vec3 direction = player.getLookAngle().normalize();
		Vec3 end = start.add(direction.scale(RANGE));
		Set<BlockPos> visitedBlocks = new HashSet<>();
		float remainingBudget = BLOCK_CUT_BUDGET;

		for (double distance = 0.0D; distance <= RANGE; distance += STEP) {
			Vec3 point = start.add(direction.scale(distance));
			BlockPos pos = BlockPos.containing(point);
			if (!visitedBlocks.add(pos)) {
				continue;
			}

			BlockState state = world.getBlockState(pos);
			if (state.isAir()) {
				continue;
			}

			float hardness = state.getDestroySpeed(world, pos);
			if (hardness < 0.0F) {
				break;
			}

			float cost = Math.max(0.5F, hardness * 1.8F + 0.75F);
			if (remainingBudget < cost) {
				break;
			}

			remainingBudget -= cost;
			world.destroyBlock(pos, true, player, 512);
		}

		AABB slashBounds = new AABB(start, end).inflate(1.15D);
		List<Entity> targets = world.getEntities(player, slashBounds, entity -> entity instanceof LivingEntity && entity.isAlive());
		for (Entity entity : targets) {
			if (entity instanceof LivingEntity living && intersectsSlash(living, start, direction)) {
				living.hurt(world.damageSources().playerAttack(player), ENTITY_DAMAGE);
			}
		}

		return true;
	}

	private static boolean intersectsSlash(LivingEntity entity, Vec3 start, Vec3 direction) {
		Vec3 toTarget = entity.position().add(0.0D, entity.getBbHeight() * 0.5D, 0.0D).add(start.scale(-1.0D));
		double along = toTarget.dot(direction);
		if (along < 0.0D || along > RANGE) {
			return false;
		}

		Vec3 closest = start.add(direction.scale(along));
		return entity.getBoundingBox().inflate(0.25D).contains(closest);
	}
}

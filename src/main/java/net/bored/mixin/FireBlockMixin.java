package net.bored.mixin;

import net.bored.negativity.NegativityManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FireBlock.class)
public class FireBlockMixin {
	@Inject(method = "tick", at = @At("HEAD"))
	private void accursed$recordFireTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random, CallbackInfo info) {
		NegativityManager.recordBurning(world, pos, 0.4D);
	}

	@Inject(method = "onPlace", at = @At("HEAD"))
	private void accursed$recordFirePlaced(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean notify, CallbackInfo info) {
		if (world instanceof ServerLevel serverLevel) {
			NegativityManager.recordBurning(serverLevel, pos, 2.0D);
		}
	}
}

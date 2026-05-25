package net.bored.mixin;

import net.bored.client.ClientAbilityState;
import net.bored.client.input.AbilityKeybinds;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {
	@Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
	private void accursed$cycleAbility(long window, double horizontal, double vertical, CallbackInfo info) {
		if (vertical != 0.0D && AbilityKeybinds.shouldHandleAbilityScroll()) {
			ClientAbilityState.cycle(vertical > 0.0D ? -1 : 1);
			info.cancel();
		}
	}
}

package net.bored.client.input;

import com.mojang.blaze3d.platform.InputConstants;
import net.bored.client.ClientAbilityState;
import net.bored.network.AccursedNetworking;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public final class AbilityKeybinds {
	private static final String CATEGORY = "key.category.accursed";
	private static KeyMapping toggleDisplay;
	private static KeyMapping cycleModifier;
	private static KeyMapping useAbility;

	private AbilityKeybinds() {
	}

	public static void register() {
		toggleDisplay = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.accursed.toggle_abilities", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_V, CATEGORY));
		cycleModifier = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.accursed.cycle_modifier", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_ALT, CATEGORY));
		useAbility = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.accursed.use_ability", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_G, CATEGORY));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (toggleDisplay.consumeClick()) {
				ClientAbilityState.toggleVisible();
			}

			while (useAbility.consumeClick()) {
				if (ClientAbilityState.visible()) {
					AccursedNetworking.sendUseAbility();
				}
			}
		});
	}

	public static boolean shouldHandleAbilityScroll() {
		return ClientAbilityState.visible() && cycleModifier != null && cycleModifier.isDown();
	}
}

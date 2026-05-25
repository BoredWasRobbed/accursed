package net.bored.client;

import net.bored.client.hud.AbilityHud;
import net.bored.client.input.AbilityKeybinds;
import net.bored.network.AccursedNetworking;
import net.fabricmc.api.ClientModInitializer;

public class AccursedClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientAbilityState.reset();
		AccursedNetworking.registerClient();
		AbilityKeybinds.register();
		AbilityHud.register();
	}
}

package net.bored;

import net.fabricmc.api.ModInitializer;
import net.bored.curse.CursePlayerState;
import net.bored.curse.technique.AccursedTechniques;
import net.bored.negativity.NegativityManager;
import net.bored.network.AccursedNetworking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Accursed implements ModInitializer {
	public static final String MOD_ID = "accursed";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		AccursedTechniques.registerDefaults();
		CursePlayerState.registerEvents();
		NegativityManager.registerEvents();
		AccursedNetworking.registerServer();

		LOGGER.info("Accursed systems initialized.");
	}
}

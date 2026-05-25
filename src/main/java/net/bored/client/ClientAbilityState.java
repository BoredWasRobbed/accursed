package net.bored.client;

import java.util.ArrayList;
import java.util.List;
import net.bored.network.AccursedNetworking;

public final class ClientAbilityState {
	private static final List<AbilityView> ABILITIES = new ArrayList<>();
	private static boolean visible;
	private static int selectedIndex;
	private static double cursedEnergy;
	private static double maxCursedEnergy = 100.0D;
	private static long syncClientTick;

	private ClientAbilityState() {
	}

	public static void reset() {
		ABILITIES.clear();
		ABILITIES.add(new AbilityView("shrine", "dismantle", "Dismantle", 0, 60));
		visible = false;
		selectedIndex = 0;
		cursedEnergy = 100.0D;
		maxCursedEnergy = 100.0D;
		syncClientTick = 0L;
	}

	public static void updateFromServer(double energy, double maxEnergy, int selected, List<AbilityView> abilities, long clientTick) {
		ABILITIES.clear();
		ABILITIES.addAll(abilities);
		cursedEnergy = energy;
		maxCursedEnergy = Math.max(1.0D, maxEnergy);
		selectedIndex = clampSelected(selected);
		syncClientTick = clientTick;
	}

	public static boolean visible() {
		return visible;
	}

	public static void toggleVisible() {
		visible = !visible;
	}

	public static List<AbilityView> abilities() {
		return ABILITIES;
	}

	public static int selectedIndex() {
		return selectedIndex;
	}

	public static double cursedEnergy() {
		return cursedEnergy;
	}

	public static double maxCursedEnergy() {
		return maxCursedEnergy;
	}

	public static void cycle(int direction) {
		if (ABILITIES.isEmpty()) {
			return;
		}

		selectedIndex = Math.floorMod(selectedIndex + direction, ABILITIES.size());
		AccursedNetworking.sendSelectAbility(selectedIndex);
	}

	public static void select(int index) {
		selectedIndex = clampSelected(index);
	}

	private static int clampSelected(int selected) {
		if (ABILITIES.isEmpty()) {
			return 0;
		}

		return Math.max(0, Math.min(selected, ABILITIES.size() - 1));
	}

	public static int remainingCooldownTicks(AbilityView ability, long currentClientTick) {
		int elapsed = (int) Math.max(0L, currentClientTick - syncClientTick);
		return Math.max(0, ability.remainingCooldownTicks() - elapsed);
	}
}

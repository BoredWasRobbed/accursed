package net.bored.curse;

public interface CurseAbility {
	String id();

	String displayName();

	int cooldownTicks();

	double cursedEnergyCost();

	boolean activate(AbilityUseContext context);
}

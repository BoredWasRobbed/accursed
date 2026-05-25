package net.bored.client;

public record AbilityView(String techniqueId, String abilityId, String name, int remainingCooldownTicks, int cooldownTicks) {
}

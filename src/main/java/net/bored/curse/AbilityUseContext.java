package net.bored.curse;

import net.minecraft.server.level.ServerPlayer;

public record AbilityUseContext(ServerPlayer player, CursePlayerData data, long gameTime) {
}

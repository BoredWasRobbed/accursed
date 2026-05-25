package net.bored.client.hud;

import java.util.List;
import net.bored.client.AbilityView;
import net.bored.client.ClientAbilityState;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public final class AbilityHud {
	private AbilityHud() {
	}

	public static void register() {
		HudRenderCallback.EVENT.register(AbilityHud::render);
	}

	private static void render(GuiGraphics graphics, DeltaTracker tickCounter) {
		Minecraft minecraft = Minecraft.getInstance();
		if (!ClientAbilityState.visible() || minecraft.player == null || minecraft.screen != null) {
			return;
		}

		List<AbilityView> abilities = ClientAbilityState.abilities();
		if (abilities.isEmpty()) {
			return;
		}

		long tick = minecraft.level == null ? 0L : minecraft.level.getGameTime();
		int selected = ClientAbilityState.selectedIndex();
		int centerY = graphics.guiHeight() / 2;
		int x = 16;
		int slotHeight = 32;
		int width = 132;
		int count = abilities.size();

		for (int offset = -2; offset <= 2; offset++) {
			int index = Math.floorMod(selected + offset, count);
			AbilityView ability = abilities.get(index);
			int y = centerY + offset * slotHeight - 15;
			float scale = offset == 0 ? 1.0F : 0.84F;
			int alpha = offset == 0 ? 0xDD : 0x88;
			int bg = (alpha << 24) | (offset == 0 ? 0x30251A : 0x171717);
			int accent = offset == 0 ? 0xFFE8B15A : 0xFF735F42;

			int rowWidth = (int) (width * scale);
			int rowX = x + (width - rowWidth) / 2 + Math.abs(offset) * 5;
			graphics.fill(rowX, y, rowX + rowWidth, y + 25, bg);
			graphics.fill(rowX, y, rowX + 3, y + 25, accent);
			graphics.drawString(minecraft.font, ability.name(), rowX + 10, y + 5, offset == 0 ? 0xFFF7E8D1 : 0xFFCABBA5, false);

			int remaining = ClientAbilityState.remainingCooldownTicks(ability, tick);
			if (remaining > 0) {
				float progress = ability.cooldownTicks() <= 0 ? 1.0F : (float) remaining / (float) ability.cooldownTicks();
				int cooldownWidth = Math.max(1, (int) ((rowWidth - 13) * progress));
				graphics.fill(rowX + 10, y + 18, rowX + 10 + cooldownWidth, y + 21, 0xFF6AA6FF);
				graphics.drawString(minecraft.font, formatSeconds(remaining), rowX + rowWidth - 30, y + 5, 0xFFBFD7FF, false);
			} else if (offset == 0) {
				graphics.fill(rowX + 10, y + 18, rowX + rowWidth - 4, y + 21, 0xFF8BE28B);
			}
		}

		int barY = centerY + 92;
		graphics.fill(x, barY, x + width, barY + 7, 0xAA16120F);
		int energyWidth = (int) (width * (ClientAbilityState.cursedEnergy() / ClientAbilityState.maxCursedEnergy()));
		graphics.fill(x, barY, x + energyWidth, barY + 7, 0xFF6C5CE7);
		graphics.drawString(minecraft.font, "CE " + (int) ClientAbilityState.cursedEnergy() + "/" + (int) ClientAbilityState.maxCursedEnergy(), x, barY + 10, 0xFFE9E0FF, false);
	}

	private static String formatSeconds(int ticks) {
		return String.format("%.1fs", ticks / 20.0F);
	}
}

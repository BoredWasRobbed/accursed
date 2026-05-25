package net.bored.curse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.bored.curse.technique.AccursedTechniques;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;

public class CursePlayerData {
	private final UUID playerId;
	private double cursedEnergy = 100.0D;
	private double maxCursedEnergy = 100.0D;
	private int selectedAbilityIndex;
	private final List<CurseTechnique> techniques = new ArrayList<>();
	private final Map<String, Long> cooldownEnds = new HashMap<>();

	public CursePlayerData(UUID playerId) {
		this.playerId = playerId;
		ensureDefaults();
	}

	public UUID playerId() {
		return playerId;
	}

	public double cursedEnergy() {
		return cursedEnergy;
	}

	public double maxCursedEnergy() {
		return maxCursedEnergy;
	}

	public int selectedAbilityIndex() {
		return selectedAbilityIndex;
	}

	public List<CurseTechnique> techniques() {
		return techniques;
	}

	public List<CurseAbility> abilities() {
		List<CurseAbility> abilities = new ArrayList<>();
		for (CurseTechnique technique : techniques) {
			abilities.addAll(technique.abilities());
		}
		return abilities;
	}

	public void selectAbility(int index) {
		List<CurseAbility> abilities = abilities();
		if (abilities.isEmpty()) {
			selectedAbilityIndex = 0;
			return;
		}

		selectedAbilityIndex = Math.max(0, Math.min(index, abilities.size() - 1));
	}

	public boolean useSelectedAbility(ServerPlayer player) {
		List<CurseAbility> abilities = abilities();
		if (abilities.isEmpty()) {
			return false;
		}

		selectAbility(selectedAbilityIndex);
		CurseAbility ability = abilities.get(selectedAbilityIndex);
		long gameTime = player.serverLevel().getGameTime();
		if (remainingCooldown(ability, gameTime) > 0 || cursedEnergy < ability.cursedEnergyCost()) {
			return false;
		}

		boolean activated = ability.activate(new AbilityUseContext(player, this, gameTime));
		if (activated) {
			cursedEnergy -= ability.cursedEnergyCost();
			cooldownEnds.put(ability.id(), gameTime + ability.cooldownTicks());
			return true;
		}

		return false;
	}

	public int remainingCooldown(CurseAbility ability, long gameTime) {
		return (int) Math.max(0L, cooldownEnds.getOrDefault(ability.id(), 0L) - gameTime);
	}

	public void tick(ServerPlayer player) {
		if (player.tickCount % 20 == 0 && cursedEnergy < maxCursedEnergy) {
			cursedEnergy = Math.min(maxCursedEnergy, cursedEnergy + 2.0D);
		}
	}

	public void ensureDefaults() {
		if (techniques.isEmpty()) {
			AccursedTechniques.create("shrine").ifPresent(techniques::add);
		}
	}

	public CompoundTag save() {
		CompoundTag tag = new CompoundTag();
		tag.putUUID("player_id", playerId);
		tag.putDouble("cursed_energy", cursedEnergy);
		tag.putDouble("max_cursed_energy", maxCursedEnergy);
		tag.putInt("selected_ability", selectedAbilityIndex);

		ListTag techniqueList = new ListTag();
		for (CurseTechnique technique : techniques) {
			techniqueList.add(technique.save());
		}
		tag.put("techniques", techniqueList);

		ListTag cooldownList = new ListTag();
		for (Map.Entry<String, Long> cooldown : cooldownEnds.entrySet()) {
			CompoundTag cooldownTag = new CompoundTag();
			cooldownTag.putString("ability", cooldown.getKey());
			cooldownTag.putLong("ends_at", cooldown.getValue());
			cooldownList.add(cooldownTag);
		}
		tag.put("cooldowns", cooldownList);

		return tag;
	}

	public static CursePlayerData load(CompoundTag tag) {
		UUID id = tag.hasUUID("player_id") ? tag.getUUID("player_id") : UUID.randomUUID();
		CursePlayerData data = new CursePlayerData(id);
		data.cursedEnergy = tag.contains("cursed_energy") ? tag.getDouble("cursed_energy") : data.cursedEnergy;
		data.maxCursedEnergy = tag.contains("max_cursed_energy") ? tag.getDouble("max_cursed_energy") : data.maxCursedEnergy;
		data.selectedAbilityIndex = tag.getInt("selected_ability");
		data.techniques.clear();

		ListTag techniqueList = tag.getList("techniques", Tag.TAG_COMPOUND);
		for (int i = 0; i < techniqueList.size(); i++) {
			CompoundTag techniqueTag = techniqueList.getCompound(i);
			Optional<CurseTechnique> technique = AccursedTechniques.load(techniqueTag);
			technique.ifPresent(data.techniques::add);
		}

		ListTag cooldownList = tag.getList("cooldowns", Tag.TAG_COMPOUND);
		for (int i = 0; i < cooldownList.size(); i++) {
			CompoundTag cooldownTag = cooldownList.getCompound(i);
			data.cooldownEnds.put(cooldownTag.getString("ability"), cooldownTag.getLong("ends_at"));
		}

		data.ensureDefaults();
		data.selectAbility(data.selectedAbilityIndex);
		return data;
	}
}

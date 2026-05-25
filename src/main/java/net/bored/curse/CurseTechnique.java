package net.bored.curse;

import java.util.List;
import net.minecraft.nbt.CompoundTag;

public interface CurseTechnique {
	String id();

	String displayName();

	List<CurseAbility> abilities();

	CurseTechnique copy();

	default CompoundTag save() {
		CompoundTag tag = new CompoundTag();
		tag.putString("id", id());
		return tag;
	}
}

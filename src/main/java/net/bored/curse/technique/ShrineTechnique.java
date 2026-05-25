package net.bored.curse.technique;

import java.util.List;
import net.bored.curse.CurseAbility;
import net.bored.curse.CurseTechnique;
import net.bored.curse.ability.DismantleAbility;

public class ShrineTechnique implements CurseTechnique {
	private final List<CurseAbility> abilities;

	public ShrineTechnique() {
		this.abilities = List.of(new DismantleAbility());
	}

	@Override
	public String id() {
		return "shrine";
	}

	@Override
	public String displayName() {
		return "Shrine";
	}

	@Override
	public List<CurseAbility> abilities() {
		return abilities;
	}

	@Override
	public CurseTechnique copy() {
		return new ShrineTechnique();
	}
}

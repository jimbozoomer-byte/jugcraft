package io.github.jimbozoomer.jugcraft.concordance.effect;

import org.jspecify.annotations.Nullable;

/**
 * How a creature bears harmful control (movement and status effects): the same for every source. Damage is not
 * changed here; it already passes through armour, enchantments and Spell Power's school resistances on its damage
 * type, and taking it off again here would count those twice.
 * <ul>
 * <li>{@link #NORMAL}: the effect as it is.</li>
 * <li>{@link #RESISTANT} ({@code #jugcraft:concordance/resistant}): harmful pushes at half strength, harmful statuses
 * for half the time; anything halved to nothing is resisted.</li>
 * <li>{@link #IMMUNE} ({@code #jugcraft:concordance/immune}, and players in creative or spectator mode for every
 * harmful effect): harmful control does nothing.</li>
 * </ul>
 * Helpful effects are never reduced.
 */
public enum Tolerance {
	NORMAL,
	RESISTANT,
	IMMUNE;

	/** Whether this tolerance concerns the effect at all: harmful control only. */
	public static boolean governs(EffectSpec spec) {
		return spec.intent() == Intent.HARMFUL && (spec.kind() == EffectKind.MOVEMENT || spec.kind() == EffectKind.STATUS);
	}

	/**
	 * The effect as this creature takes it, or null if it does nothing to them. Effects this tolerance does not
	 * govern pass unchanged.
	 */
	public EffectSpec.@Nullable Adjusted adjust(EffectSpec spec) {
		if (!governs(spec) || this == NORMAL) {
			return new EffectSpec.Adjusted(spec, false);
		}
		if (this == IMMUNE) {
			return null;
		}
		EffectSpec halved = spec.kind() == EffectKind.MOVEMENT ? spec.withMagnitude(spec.magnitude() / 2)
				: spec.withDuration(spec.duration() / 2);
		boolean nothingLeft = spec.kind() == EffectKind.MOVEMENT ? halved.magnitude() <= 0 : halved.duration() <= 0;
		return nothingLeft ? null : new EffectSpec.Adjusted(halved, true);
	}
}

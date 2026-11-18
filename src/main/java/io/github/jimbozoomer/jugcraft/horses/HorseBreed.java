package io.github.jimbozoomer.jugcraft.horses;

import java.util.List;

/**
 * The horse breeds and the Pegasus line (docs/features/horse-breeds.md): what each one is good and bad at, as bounded
 * changes to a horse's own rolled health, speed and jump and to existing attributes. Pure data: no Minecraft classes, so
 * the rules can be tested outside the game (HorseLineageTests).
 *
 * <p>Multipliers apply to the horse's vanilla-style rolled base value (never to inherited totals, so they cannot stack
 * over generations); bonuses add to vanilla's attribute (step height 1.0, safe fall 6). {@code cargoRows} is the breed's
 * own saddlebag size in rows of nine; {@code roadBonus} the extra speed on road blocks ({@code jugcraft:horse_roads});
 * {@code recovery} the health a calm horse regains every {@link HorseLineage#RECOVERY_TICKS} ticks.
 *
 * <table>
 * <caption>Breed traits</caption>
 * <tr><th>Breed</th><th>Health</th><th>Speed</th><th>Jump</th><th>Step</th><th>Safe fall</th><th>Knockback</th><th>Armour</th><th>Bags</th><th>Also</th></tr>
 * <tr><td>Icelandic</td><td>x1</td><td>x0.85</td><td>x0.9</td><td>+0.25</td><td>+2</td><td>0</td><td>0</td><td>-</td><td>walks on powder snow; no freezing (entity tags)</td></tr>
 * <tr><td>Mongolian</td><td>x1.15</td><td>x1</td><td>x0.95</td><td>+0.1</td><td>+1</td><td>0</td><td>0</td><td>-</td><td>recovers 1 health in 10 s when calm; no freezing</td></tr>
 * <tr><td>Shire</td><td>x1.2</td><td>x0.75</td><td>x0.7</td><td>0</td><td>0</td><td>+0.5</td><td>0</td><td>3 rows</td><td></td></tr>
 * <tr><td>Giant Finnhorse</td><td>x1.5</td><td>x1.1</td><td>x0.8</td><td>+0.25</td><td>0</td><td>+0.8</td><td>+4</td><td>1 row</td><td>the largest hitbox</td></tr>
 * <tr><td>Friesian</td><td>x0.85</td><td>x1.15</td><td>x1.2</td><td>0</td><td>0</td><td>0</td><td>0</td><td>-</td><td>+10% speed on roads</td></tr>
 * <tr><td>Pegasus</td><td colspan="7">its own fixed stats (Pegasus.HEALTH, SPEED, JUMP); no breed modifiers</td><td>1 row</td><td>flies</td></tr>
 * </table>
 */
public enum HorseBreed {
	ICELANDIC("icelandic_horse", 1.00, 0.85, 0.90, 0.25, 2.0, 0.0, 0.0, 0, 0.0, 0.0, false),
	MONGOLIAN("mongolian_horse", 1.15, 1.00, 0.95, 0.10, 1.0, 0.0, 0.0, 0, 0.0, 1.0, false),
	SHIRE("shire_horse", 1.20, 0.75, 0.70, 0.0, 0.0, 0.5, 0.0, 3, 0.0, 0.0, true),
	GIANT_FINNHORSE("giant_finnhorse", 1.50, 1.10, 0.80, 0.25, 0.0, 0.8, 4.0, 1, 0.0, 0.0, true),
	FRIESIAN("friesian_horse", 0.85, 1.15, 1.20, 0.0, 0.0, 0.0, 0.0, 0, 0.10, 0.0, false),
	/** The flying line: pairs only with itself (HorseLineage.mayPair); its stats are Pegasus's own, not modifiers. */
	PEGASUS("pegasus", 1.0, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1, 0.0, 0.0, true);

	/** The five ground breeds, in the order ancestry shares are kept. */
	public static final List<HorseBreed> GROUND = List.of(ICELANDIC, MONGOLIAN, SHIRE, GIANT_FINNHORSE, FRIESIAN);

	private final String id;
	public final double health;
	public final double speed;
	public final double jump;
	public final double step;
	public final double safeFall;
	public final double knockback;
	public final double armor;
	public final int cargoRows;
	public final double roadBonus;
	public final double recovery;
	/** Whether the breed's tack has saddlebags drawn for it (so it can be fitted with them at all). */
	public final boolean carriesBags;

	HorseBreed(String id, double health, double speed, double jump, double step, double safeFall, double knockback,
			double armor, int cargoRows, double roadBonus, double recovery, boolean carriesBags) {
		this.id = id;
		this.health = health;
		this.speed = speed;
		this.jump = jump;
		this.step = step;
		this.safeFall = safeFall;
		this.knockback = knockback;
		this.armor = armor;
		this.cargoRows = cargoRows;
		this.roadBonus = roadBonus;
		this.recovery = recovery;
		this.carriesBags = carriesBags;
	}

	/** The entity id path ({@code jugcraft:<id>}), also the key of its saved ancestry share. */
	public String id() {
		return id;
	}

	/** Only the Pegasus line flies; a ground breed never does, whatever its ancestry. */
	public boolean flies() {
		return this == PEGASUS;
	}

	public static HorseBreed byId(String id) {
		for (HorseBreed breed : values()) {
			if (breed.id.equals(id)) {
				return breed;
			}
		}
		return null;
	}
}

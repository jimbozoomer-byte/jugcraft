package io.github.jimbozoomer.jugcraft.lineage;

/**
 * The four traits a line carries (docs/features/star-bred-lines.md). Each is two copies, one from each parent, each
 * graded 0 to 5; the trait's number is the two added together, 0 to 10, and 4 or below is exactly vanilla.
 */
public enum Trait {
	/** How much the animal gives: more meat or wool, eggs that come as two. */
	YIELD("yield"),
	/** Health, speed and how soon its babies grow up. */
	VIGOUR("vigour"),
	/** How soon it may breed again. */
	FERTILITY("fertility"),
	/** What harms it no longer: thorns, frost, falls, drowning, burning, lightning. */
	HARDINESS("hardiness");

	public final String id;

	Trait(String id) {
		this.id = id;
	}

	public String key() {
		return "lineage.jugcraft.trait." + id;
	}
}

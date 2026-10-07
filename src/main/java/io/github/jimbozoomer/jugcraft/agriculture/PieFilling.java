package io.github.jimbozoomer.jugcraft.agriculture;

import org.jspecify.annotations.Nullable;

/**
 * The pies a Hearth Oven bakes, each from its filling (and pastry and sugar): apple, pumpkin cream, cranberry, sweet potato,
 * chestnut, chocolate, the sweet berry cheesecake, and the orchards' peach and lemon meringue pies. Each comes as a raw pie
 * ({@code raw_<pie>}), a baked pie placed like a cake ({@code <pie>}, eaten or cut a slice at a time) and a slice
 * ({@code <pie>_slice}); {@code <pie>} is {@code <id>_pie} but for the cheesecake and the lemon meringue pie. {@link #color} is the filling's, for the oven's renderer. The Hearth Oven saves a
 * filling by its place in this list, so new fillings go at the end.
 */
public enum PieFilling {
	APPLE("apple", "apple_pie", 4, 0.6F, 0xC89A48),
	PUMPKIN_CREAM("pumpkin_cream", "pumpkin_cream_pie", 4, 0.6F, 0xE0822A),
	CRANBERRY("cranberry", "cranberry_pie", 3, 0.6F, 0xA01C34),
	SWEET_POTATO("sweet_potato", "sweet_potato_pie", 4, 0.7F, 0xD8682A),
	CHESTNUT("chestnut", "chestnut_pie", 5, 0.7F, 0x6A3E1E),
	CHOCOLATE("chocolate", "chocolate_pie", 4, 0.6F, 0x5A3220),
	SWEET_BERRY("sweet_berry", "sweet_berry_cheesecake", 4, 0.6F, 0xB0283C),
	PEACH("peach", "peach_pie", 4, 0.6F, 0xE8904A),
	LEMON("lemon", "lemon_meringue_pie", 4, 0.6F, 0xF0D040);

	public final String id;
	/** The baked pie's ID; the raw pie and the slice are named from it. */
	public final String pie;
	/** What a slice gives: hunger and its saturation modifier. */
	public final int nutrition;
	public final float saturation;
	public final int color;

	PieFilling(String id, String pie, int nutrition, float saturation, int color) {
		this.id = id;
		this.pie = pie;
		this.nutrition = nutrition;
		this.saturation = saturation;
		this.color = color;
	}

	public String rawPie() {
		return "raw_" + pie;
	}

	public String pie() {
		return pie;
	}

	public String slice() {
		return pie + "_slice";
	}

	/** The filling whose raw pie {@code item} is, or null. */
	public static @Nullable PieFilling ofRaw(String item) {
		for (PieFilling filling : values()) {
			if (filling.rawPie().equals(item)) {
				return filling;
			}
		}
		return null;
	}
}

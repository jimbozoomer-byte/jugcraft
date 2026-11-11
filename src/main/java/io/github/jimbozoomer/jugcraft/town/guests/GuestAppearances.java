package io.github.jimbozoomer.jugcraft.town.guests;

import io.github.jimbozoomer.jugcraft.town.CustomNpcs;
import io.github.jimbozoomer.jugcraft.town.Townsfolk;
import java.util.List;
import java.util.Map;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

/** Character-inspired player skins authored in tools/guest_skins.py. Pending identities remain unavailable. */
public final class GuestAppearances {
	/** The note shown wherever a placeholder appears, and its fallback text. */
	public static final String NOTE_KEY = "message.jugcraft.guest.appearance.placeholder";
	public static final String NOTE = "placeholder appearance, reference pending";

	/** The town's civilian skins guests may wear as placeholders, in the order they were handed out. */
	public static final List<String> PLACEHOLDER_SKINS = List.of("townsfolk_1", "townsfolk_2", "townsfolk_3", "townsfolk_4",
			"townsfolk_5", "florist", "baker", "innkeeper", "shopkeeper_green", "shopkeeper_brown", "decorator_green",
			"decorator_orange");

	private static final Map<String, String> CHOSEN = Map.ofEntries(
			Map.entry("emiru", "guest_emiru"),
			Map.entry("lauren_chen", "guest_lauren_chen"),
			Map.entry("brittany_venti", "guest_brittany_venti"),
			Map.entry("aoc", "guest_aoc"),
			Map.entry("sarah_stock", "guest_sarah_stock"),
			Map.entry("pokimane", "guest_pokimane"),
			Map.entry("power", "guest_power"),
			Map.entry("makima", "guest_makima"),
			Map.entry("reze", "guest_reze"),
			Map.entry("asa_mitaka", "guest_asa_mitaka"),
			Map.entry("himeno", "guest_himeno"),
			Map.entry("tokai_teio", "guest_tokai_teio"),
			Map.entry("agnes_tachyon", "guest_agnes_tachyon"),
			Map.entry("gold_ship", "guest_gold_ship"),
			Map.entry("oguri_cap", "guest_oguri_cap"),
			Map.entry("special_week", "guest_special_week"),
			Map.entry("rei_ayanami", "guest_rei_ayanami"),
			Map.entry("asuka_langley", "guest_asuka_langley"),
			Map.entry("misato_katsuragi", "guest_misato_katsuragi"),
			Map.entry("frieren", "guest_frieren"),
			Map.entry("rem", "guest_rem"),
			Map.entry("megumin", "guest_megumin"),
			Map.entry("zero_two", "guest_zero_two"),
			Map.entry("rin_ishtar", "guest_rin_ishtar"),
			Map.entry("tifa_lockhart", "guest_tifa_lockhart"),
			Map.entry("yorha_2b", "guest_yorha_2b"),
			Map.entry("chun_li", "guest_chun_li"),
			Map.entry("ada_wong", "guest_ada_wong"),
			Map.entry("lara_croft", "guest_lara_croft"),
			Map.entry("kafka", "guest_kafka"),
			Map.entry("miyabi", "guest_miyabi"),
			Map.entry("nicole_demara", "guest_nicole_demara"));

	// Historical assignment order: do not derive this from the mutable current roster.
	private static final List<String> LEGACY_ORDER = List.of(
			"emiru",
			"lauren_chen",
			"brittany_venti",
			"aoc",
			"sarah_stock",
			"pokimane",
			"power",
			"makima",
			"reze",
			"asa_mitaka",
			"himeno",
			"tokai_teio",
			"agnes_tachyon",
			"gold_ship",
			"oguri_cap",
			"special_week",
			"rei_ayanami",
			"asuka_langley",
			"misato_katsuragi",
			"frieren",
			"rem",
			"megumin",
			"zero_two",
			"rin_ishtar",
			"tifa_lockhart",
			"yorha_2b",
			"chun_li",
			"ada_wong",
			"lara_croft",
			"kafka",
			"miyabi",
			"nicole_demara");

	/** Exact former stand-in, used only to migrate this identity. */
	public static @Nullable String legacySkin(String guest) {
		int index = LEGACY_ORDER.indexOf(guest);
		return index < 0 ? null : PLACEHOLDER_SKINS.get(index % PLACEHOLDER_SKINS.size());
	}

	/** Packaged, named skins; also exposed to the existing operator skin picker. */
	public static java.util.Set<String> skins() {
		return java.util.Set.copyOf(CHOSEN.values());
	}

	/**
	 * A guest's look: the town skin it wears and whether that is authored character art rather than a town stand-in. Visual approval is separate.
	 */
	public record Appearance(String guest, String townSkin, boolean finalArt) {
		/** A stand-in look, not the character's. */
		public boolean placeholder() {
			return !finalArt;
		}

		/** What players are told about it. */
		public Component note() {
			return finalArt ? Component.translatableWithFallback("message.jugcraft.guest.appearance.final", "character-inspired skin")
					: GuestAppearances.note();
		}
	}

	private GuestAppearances() {
	}

	/** The guest's appearance; null for an unknown or pending guest (it cannot be placed). */
	public static @Nullable Appearance of(@Nullable String guest) {
		GuestRoster.Guest entry = GuestRoster.get(guest);
		if (entry == null || !entry.available()) {
			return null;
		}
		String skin = CHOSEN.get(entry.id());
		return new Appearance(entry.id(), skin != null ? skin : fallback(entry.id()), skin != null);
	}

	/** A guest not in the table: a skin from {@link #PLACEHOLDER_SKINS} picked by its id, the same every time. */
	static String fallback(String guest) {
		return PLACEHOLDER_SKINS.get(Math.floorMod(guest.hashCode(), PLACEHOLDER_SKINS.size()));
	}

	/** "placeholder appearance, reference pending". */
	public static Component note() {
		return Component.translatableWithFallback(NOTE_KEY, NOTE);
	}

	/**
	 * Dresses a placed guest in its placeholder (server). False, leaving the town's default look, for a guest with no
	 * appearance or a skin the town does not have.
	 */
	public static boolean apply(Townsfolk npc, String guest) {
		Appearance appearance = of(guest);
		return appearance != null && CustomNpcs.setTownSkin(npc, appearance.townSkin());
	}
}

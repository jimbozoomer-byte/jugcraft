package io.github.jimbozoomer.jugcraft.town.guests;

import io.github.jimbozoomer.jugcraft.town.CustomNpcs;
import io.github.jimbozoomer.jugcraft.town.Townsfolk;
import java.util.List;
import java.util.Map;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

/**
 * How each town guest looks (worker C). No reference art or named skin was supplied for any guest (the asset audit,
 * docs/audits/jze-guest-appearances.md), so every available guest wears a labelled placeholder: one of the town's own
 * civilian skins ({@link CustomNpcs#townSkins()}, all original), chosen once per guest and kept, varied so neighbours in
 * the roster look different. A placeholder is never presented as the character: the guest's menu and
 * {@code /guests roster} say "placeholder appearance, reference pending", and {@link Appearance#finalArt()} is false
 * for every guest until real, approved art exists.
 *
 * <p>Guard uniforms and the town's named office-holders (mayor, priest, banker) are left out, so a guest is never
 * mistaken for a guard or an official. Pending guests have no appearance: they cannot be placed. Pure data, safe on
 * either side; only {@link #apply} touches an entity (server).
 */
public final class GuestAppearances {
	/** The note shown wherever a placeholder appears, and its fallback text. */
	public static final String NOTE_KEY = "message.jugcraft.guest.appearance.placeholder";
	public static final String NOTE = "placeholder appearance, reference pending";

	/** The town's civilian skins guests may wear as placeholders, in the order they were handed out. */
	public static final List<String> PLACEHOLDER_SKINS = List.of("townsfolk_1", "townsfolk_2", "townsfolk_3", "townsfolk_4",
			"townsfolk_5", "florist", "baker", "innkeeper", "shopkeeper_green", "shopkeeper_brown", "decorator_green",
			"decorator_orange");

	/**
	 * Each available guest's placeholder, fixed here so it never changes when the roster grows (handed out in roster
	 * order over {@link #PLACEHOLDER_SKINS} on 11 November 2026). A guest missing from this table (one that becomes
	 * available later) gets {@link #fallback}.
	 */
	private static final Map<String, String> CHOSEN = Map.ofEntries(
			Map.entry("emiru", "townsfolk_1"),
			Map.entry("lauren_chen", "townsfolk_2"),
			Map.entry("brittany_venti", "townsfolk_3"),
			Map.entry("aoc", "townsfolk_4"),
			Map.entry("sarah_stock", "townsfolk_5"),
			Map.entry("pokimane", "florist"),
			Map.entry("power", "baker"),
			Map.entry("makima", "innkeeper"),
			Map.entry("reze", "shopkeeper_green"),
			Map.entry("asa_mitaka", "shopkeeper_brown"),
			Map.entry("himeno", "decorator_green"),
			Map.entry("tokai_teio", "decorator_orange"),
			Map.entry("agnes_tachyon", "townsfolk_1"),
			Map.entry("gold_ship", "townsfolk_2"),
			Map.entry("oguri_cap", "townsfolk_3"),
			Map.entry("special_week", "townsfolk_4"),
			Map.entry("rei_ayanami", "townsfolk_5"),
			Map.entry("asuka_langley", "florist"),
			Map.entry("misato_katsuragi", "baker"),
			Map.entry("frieren", "innkeeper"),
			Map.entry("rem", "shopkeeper_green"),
			Map.entry("megumin", "shopkeeper_brown"),
			Map.entry("zero_two", "decorator_green"),
			Map.entry("rin_ishtar", "decorator_orange"),
			Map.entry("tifa_lockhart", "townsfolk_1"),
			Map.entry("yorha_2b", "townsfolk_2"),
			Map.entry("chun_li", "townsfolk_3"),
			Map.entry("ada_wong", "townsfolk_4"),
			Map.entry("lara_croft", "townsfolk_5"),
			Map.entry("kafka", "florist"),
			Map.entry("miyabi", "baker"),
			Map.entry("nicole_demara", "innkeeper"));

	/**
	 * A guest's look: the town skin it wears and whether that is final, approved art. {@code finalArt} is false for every
	 * guest now: each is a labelled placeholder.
	 */
	public record Appearance(String guest, String townSkin, boolean finalArt) {
		/** A stand-in look, not the character's. */
		public boolean placeholder() {
			return !finalArt;
		}

		/** What players are told about it. */
		public Component note() {
			return finalArt ? Component.translatableWithFallback("message.jugcraft.guest.appearance.final", "final appearance")
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
		return new Appearance(entry.id(), skin != null ? skin : fallback(entry.id()), false);
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

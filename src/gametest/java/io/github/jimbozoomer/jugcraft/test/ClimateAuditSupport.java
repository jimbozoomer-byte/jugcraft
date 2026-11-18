package io.github.jimbozoomer.jugcraft.test;

import com.google.gson.JsonObject;
import io.github.jimbozoomer.jugcraft.Jugcraft;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

/**
 * The climate audit's tag names, role rules and JSON helpers (worker D), written from {@code CONTRACT.md} sections 2-3
 * and Amendments 1-4, NOT from the climate core's code, so the audit tests check A's and B's classes against an
 * independent reading of the contract. The band geometry is {@link ClimateAuditGeometry}.
 */
final class ClimateAuditSupport {
	static final String FRIGID = ClimateAuditGeometry.FRIGID;
	static final String HOT = ClimateAuditGeometry.HOT;
	static final String TEMPERATE = ClimateAuditGeometry.TEMPERATE;
	static final String CENTRAL = ClimateAuditGeometry.CENTRAL;
	static final String PRESERVE = "preserve";
	/** The banded zones that have pools. */
	static final String[] ZONES = {FRIGID, HOT, TEMPERATE};
	/** The remapped categories (each zone has one pool per category); "preserve" is never remapped. */
	static final String[] CATEGORIES = {"land", "ocean", "deep_ocean", "river", "beach", "mountain"};
	/** Every category tag, preserve included. */
	static final String[] ALL_CATEGORIES = {"land", "ocean", "deep_ocean", "river", "beach", "mountain", PRESERVE};
	/** Category families whose topology must never be crossed by a remap (Amendment 1, A2 and A4). */
	static final Map<String, String> FAMILY = Map.of("land", "land", "mountain", "land", "ocean", "sea", "deep_ocean", "sea", "river", "river",
			"beach", "beach", PRESERVE, PRESERVE);
	/** The Overworld's sea level and the surface Y sampled for weather. */
	static final int SEA_LEVEL = 63;

	private ClimateAuditSupport() {
	}

	/** {@code #jugcraft:climate/<zone>/<category>}: a zone's pool for a category (contract section 2). */
	static TagKey<Biome> pool(String zone, String category) {
		return TagKey.create(Registries.BIOME, Jugcraft.id("climate/" + zone + "/" + category));
	}

	/** {@code #jugcraft:climate/category/<category>}: the explicit category tags (Amendment 1, A4). */
	static TagKey<Biome> category(String category) {
		return TagKey.create(Registries.BIOME, Jugcraft.id("climate/category/" + category));
	}

	static TagKey<Biome> biomeTag(String id) {
		return TagKey.create(Registries.BIOME, Identifier.parse(id));
	}

	/** Every category tag this biome carries (the audit requires exactly one). */
	static List<String> categoriesOf(Holder<Biome> biome) {
		List<String> out = new ArrayList<>();
		for (String c : ALL_CATEGORIES) {
			if (biome.is(category(c))) {
				out.add(c);
			}
		}
		return out;
	}

	static boolean preserved(Holder<Biome> biome) {
		return biome.is(category(PRESERVE));
	}

	/** Whether {@code got} is a legal remap of {@code base} in a banded zone: a member of that zone's pool for one of the base's categories. */
	static boolean inRolePool(Holder<Biome> got, Holder<Biome> base, String zone) {
		for (String c : categoriesOf(base)) {
			if (!c.equals(PRESERVE) && got.is(pool(zone, c))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * The answer the contract requires in {@code zone} for {@code base}: unchanged in the central zone and for preserved
	 * (cave and underground) biomes at any Y; otherwise a member of the zone's pool for the base's own category. Returns
	 * null when it holds, else a reason.
	 */
	static String violation(String zone, Holder<Biome> base, Holder<Biome> got) {
		List<String> categories = categoriesOf(base);
		if (categories.isEmpty()) {
			return "base biome " + id(base) + " has no climate category";
		}
		if (zone.equals(CENTRAL) || categories.contains(PRESERVE)) {
			return got.equals(base) ? null : (zone.equals(CENTRAL) ? "central" : "preserved") + " base " + id(base) + " became " + id(got);
		}
		return inRolePool(got, base, zone) ? null : id(base) + " " + categories + " became " + id(got) + ", not in the " + zone + " pool of its category";
	}

	static String id(Holder<Biome> biome) {
		return biome.unwrapKey().map(key -> key.identifier().toString()).orElse("<direct>");
	}

	/**
	 * The climate core's zone, normalized to the contract's names: a zone enum or record printing FRIGID (or a north/south
	 * frigid variant), HOT, TEMPERATE or CENTRAL. Anything else fails the test loudly rather than being guessed.
	 */
	static String zoneName(Object zone) {
		String name = String.valueOf(zone).toUpperCase(Locale.ROOT);
		if (name.contains("FRIGID") || name.contains("ARCTIC") || name.contains("POLAR")) return FRIGID;
		if (name.contains("TEMPERATE")) return TEMPERATE;
		if (name.contains("HOT") || name.contains("TROPIC")) return HOT;
		if (name.contains("CENTRAL") || name.contains("IDENTITY") || name.equals("NONE")) return CENTRAL;
		throw new AssertionError("Unrecognized climate zone " + zone);
	}

	/** The profile JSON (contract section 3); pools and salt omitted when default, as the editor writes it. */
	static JsonObject json(ClimateAuditGeometry.Geometry g) {
		JsonObject climate = new JsonObject();
		climate.addProperty("schema", 1);
		JsonObject anchor = new JsonObject();
		anchor.addProperty("mode", "fixed");
		anchor.addProperty("x", g.anchorX());
		anchor.addProperty("z", g.anchorZ());
		climate.add("anchor", anchor);
		climate.addProperty("north_frigid", g.north());
		climate.addProperty("south_hot_start", g.hotStart());
		climate.addProperty("south_hot_end", g.hotEnd());
		climate.addProperty("south_frigid", g.southFrigid());
		climate.addProperty("transition", g.transition());
		if (g.salt() != 0) {
			climate.addProperty("salt", g.salt());
		}
		return climate;
	}

	/** See {@link ClimateAuditGeometry#mutableStatics}. */
	static List<String> mutableStatics(Class<?> type) {
		return ClimateAuditGeometry.mutableStatics(type);
	}
}

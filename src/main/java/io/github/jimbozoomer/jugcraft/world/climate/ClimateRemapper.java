package io.github.jimbozoomer.jugcraft.world.climate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;

/**
 * Turns a base biome (what the wrapped source answered) into the biome of its climate belt. Built once per biome
 * source, after the biome tags are bound, from that source's own {@link ClimateProfile}; immutable afterwards, with no
 * cache, {@code Random} or thread-local state, so an answer depends only on its arguments.
 *
 * <ul>
 * <li>CENTRAL: the base, unchanged, before any classification or climate sampling.</li>
 * <li>PRESERVE (caves and underground biomes, by explicit category tag): the base, unchanged, at any height.</li>
 * <li>A base already in its zone's pool for its category: unchanged (a desert in the hot belt stays that desert).</li>
 * <li>Otherwise a member of that pool ({@link ClimateSelection}): oceans stay oceans, rivers rivers, beaches beaches,
 * deep oceans deep, mountains mountains; surface specials such as mushroom fields and islands are land.</li>
 * <li>A base in no category is refused with an exception rather than kept silently (which would break the strict
 * frigid belts) or guessed as land. {@link #requireCategorized} lets a source refuse such a base up front.</li>
 * </ul>
 *
 * The zone of a quart is {@link ClimateBands#quartZone}, which keeps the frigid limits strict at block level.
 */
public final class ClimateRemapper {
	private record Pool(List<Holder<Biome>> members, Set<Holder<Biome>> set) {
	}

	private final ClimateGeometry geometry;
	private final Map<ClimateZone, Map<ClimateCategory, Pool>> pools;
	private final Set<Holder<Biome>> possible;
	/**
	 * {@code String.hashCode} of each known biome's registered name (every pool member and every base biome given to
	 * the checking constructor), by holder identity, so {@link #remap} allocates no string for them. Filled in the
	 * constructor only and read-only afterwards; the values are exactly what {@link #nameHash} would compute.
	 */
	private final Map<Holder<Biome>, Integer> nameHashes;

	/**
	 * The remapper of a profile whose pool tags are bound.
	 *
	 * @throws IllegalStateException if any of the eighteen pools is empty or its tag is not bound yet; the message
	 *         lists every such pool
	 */
	public ClimateRemapper(ClimateProfile profile) {
		this(profile, List.of(), false);
	}

	/**
	 * A remapper that also refuses, up front, any of the base source's biomes that is in no category tag. The
	 * preferred form for a biome source: an unexpected base biome then fails when the source is built, never during
	 * generation.
	 *
	 * @throws IllegalStateException as {@link #ClimateRemapper(ClimateProfile)}, or naming the uncategorized biomes
	 */
	public ClimateRemapper(ClimateProfile profile, Collection<? extends Holder<Biome>> baseBiomes) {
		this(profile, baseBiomes, true);
	}

	private ClimateRemapper(ClimateProfile profile, Collection<? extends Holder<Biome>> baseBiomes, boolean checkBase) {
		this.geometry = profile.geometry();
		List<String> problems = new ArrayList<>();
		Map<ClimateZone, Map<ClimateCategory, Pool>> built = new EnumMap<>(ClimateZone.class);
		Set<Holder<Biome>> all = new LinkedHashSet<>();
		for (ClimateZone zone : ClimateZone.BANDED) {
			Map<ClimateCategory, Pool> zonePools = new EnumMap<>(ClimateCategory.class);
			for (ClimateCategory category : ClimateCategory.POOLED) {
				HolderSet<Biome> set = profile.pools().get(zone, category);
				List<Holder<Biome>> members;
				try {
					members = set.stream().toList();
				} catch (IllegalStateException unbound) {
					problems.add(zone.id() + "/" + category.id() + " (" + describe(set) + ") is not bound: " + unbound.getMessage());
					continue;
				}
				if (members.isEmpty()) {
					problems.add(zone.id() + "/" + category.id() + " (" + describe(set) + ") is empty");
					continue;
				}
				zonePools.put(category, new Pool(members, Collections.unmodifiableSet(new LinkedHashSet<>(members))));
				all.addAll(members);
			}
			built.put(zone, Collections.unmodifiableMap(zonePools));
		}
		if (!problems.isEmpty()) {
			throw new IllegalStateException("Climate pools must each hold at least one biome: " + String.join("; ", problems));
		}
		if (checkBase) {
			requireCategorized(baseBiomes);
		}
		this.pools = Collections.unmodifiableMap(built);
		this.possible = Collections.unmodifiableSet(all);
		Map<Holder<Biome>, Integer> hashes = new IdentityHashMap<>();
		for (Holder<Biome> biome : all) {
			hashes.put(biome, biome.getRegisteredName().hashCode());
		}
		for (Holder<Biome> biome : baseBiomes) {
			hashes.put(biome, biome.getRegisteredName().hashCode());
		}
		this.nameHashes = Collections.unmodifiableMap(hashes);
	}

	/**
	 * The biome at a quart whose base source answered {@code base}, with that source's climate sampler (used only for
	 * the humidity that picks a pool member, and only outside CENTRAL; null reads as neutral humidity).
	 *
	 * @throws IllegalStateException if the base biome is outside CENTRAL and in no category tag
	 */
	public Holder<Biome> remap(Holder<Biome> base, int quartX, int quartY, int quartZ, Climate.Sampler sampler) {
		ClimateZone zone = ClimateBands.quartZone(geometry, quartX, quartZ);
		if (zone == ClimateZone.CENTRAL) {
			return base;
		}
		ClimateCategory category = ClimateCategories.classify(base);
		if (category == null) {
			throw new IllegalStateException("Climate bands: biome " + base.getRegisteredName()
					+ " is in no #jugcraft:climate/category/* tag; add it to the tag for its role");
		}
		if (category == ClimateCategory.PRESERVE) {
			return base;
		}
		Pool pool = pools.get(zone).get(category);
		if (pool.set().contains(base)) {
			return base;
		}
		int size = pool.members().size();
		if (size == 1) {
			return pool.members().getFirst();
		}
		double humidity = humidity(sampler, quartX, quartY, quartZ);
		int baseHash = nameHash(base);
		return pool.members().get(ClimateSelection.index(size, humidity, baseHash, geometry.salt(), zone.ordinal(), category.ordinal()));
	}

	/**
	 * {@code String.hashCode} of the biome's registered name: precomputed for known biomes, computed (allocating the
	 * name) for any other. {@code String.hashCode} is specified, so the value, and every choice made from it, is the
	 * same on every JVM; never an object's or record's own hash.
	 */
	int nameHash(Holder<Biome> biome) {
		Integer known = nameHashes.get(biome);
		return known != null ? known : biome.getRegisteredName().hashCode();
	}

	/** Every member of every pool, in zone, category and pool order (contract API). Callers add their base's own. */
	public Set<Holder<Biome>> possibleBiomes() {
		return possible;
	}

	public ClimateGeometry geometry() {
		return geometry;
	}

	/** The resolved members of one pool, in order. */
	public List<Holder<Biome>> members(ClimateZone zone, ClimateCategory category) {
		Pool pool = pools.containsKey(zone) ? pools.get(zone).get(category) : null;
		if (pool == null) {
			throw new IllegalArgumentException(zone.id() + "/" + category.id() + " has no pool");
		}
		return pool.members();
	}

	/**
	 * Refuses biomes that are in no category tag, naming each, so a source with an unexpected biome fails when it is
	 * built instead of mid-generation.
	 */
	public static void requireCategorized(Collection<? extends Holder<Biome>> biomes) {
		List<Holder<Biome>> missing = ClimateCategories.uncategorized(biomes);
		if (!missing.isEmpty()) {
			throw new IllegalStateException("Climate bands: these biomes are in no #jugcraft:climate/category/* tag: "
					+ String.join(", ", missing.stream().map(Holder::getRegisteredName).toList()));
		}
	}

	/**
	 * The vanilla humidity at a quart, unquantized; null sampler: neutral humidity. This uses the sampler's whole-point
	 * {@code sample}, the form this code base already uses on 26.3 (AdamOndiAhman), although it evaluates all six
	 * climate parameters. 26.3 replaced the density-function API ({@code DensitySampler}, {@code SamplerContext}), so a
	 * humidity-only evaluation needs that API's unverified form; it is left as a measured optimisation for later.
	 */
	static double humidity(Climate.Sampler sampler, int quartX, int quartY, int quartZ) {
		if (sampler == null) {
			return 0;
		}
		return Climate.unquantizeCoord(sampler.sample(quartX, quartY, quartZ).humidity());
	}

	private static String describe(HolderSet<Biome> set) {
		return set.unwrapKey().map(tag -> "#" + tag.location()).orElse("a list");
	}
}

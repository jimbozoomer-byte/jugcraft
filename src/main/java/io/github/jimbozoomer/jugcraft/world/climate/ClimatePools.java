package io.github.jimbozoomer.jugcraft.world.climate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import io.github.jimbozoomer.jugcraft.Jugcraft;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

/**
 * The biome pools of the three banded zones: for each of FRIGID, HOT and TEMPERATE, one {@code HolderSet<Biome>} per
 * pooled category (land, ocean, deep_ocean, river, beach, mountain), eighteen in all. Each defaults to the tag
 * {@code #jugcraft:climate/<zone>/<category>}; a profile may override any of them with a tag or a list of biome IDs.
 *
 * <p>Every pool must be non-empty (contract amendment 2): an explicitly empty list or a missing tag fails decoding,
 * and {@link ClimateRemapper} refuses a pool that is empty or still unbound when it is built. There is no fallback to
 * another category or to the base biome. Immutable.
 */
public final class ClimatePools {
	/** A pool: a biome tag ({@code "#ns:path"}), one biome ID, or a list of biome IDs. */
	public static final Codec<HolderSet<Biome>> POOL = Biome.LIST_CODEC;
	private static final Codec<ClimateZone> ZONE_KEY = Codec.STRING.comapFlatMap(id -> {
		ClimateZone zone = ClimateZone.banded(id);
		return zone != null ? DataResult.success(zone) : DataResult.error(() -> "Unknown climate pool zone \"" + id + "\" (frigid, hot or temperate)");
	}, ClimateZone::id);
	private static final Codec<ClimateCategory> CATEGORY_KEY = Codec.STRING.comapFlatMap(id -> {
		ClimateCategory category = ClimateCategory.pooled(id);
		return category != null ? DataResult.success(category)
				: DataResult.error(() -> "Unknown climate pool category \"" + id + "\" (land, ocean, deep_ocean, river, beach or mountain)");
	}, ClimateCategory::id);
	/** The serialized {@code pools} object: only the overridden pools, zone by zone. */
	public static final Codec<Map<ClimateZone, Map<ClimateCategory, HolderSet<Biome>>>> OVERRIDES =
			Codec.unboundedMap(ZONE_KEY, Codec.unboundedMap(CATEGORY_KEY, POOL));

	private final Map<ClimateZone, Map<ClimateCategory, HolderSet<Biome>>> sets;

	private ClimatePools(Map<ClimateZone, Map<ClimateCategory, HolderSet<Biome>>> sets) {
		this.sets = sets;
	}

	/** The default tag of a pool: {@code #jugcraft:climate/<zone>/<category>}. */
	public static TagKey<Biome> defaultTag(ClimateZone zone, ClimateCategory category) {
		if (!zone.remapped() || !category.pooled()) {
			throw new IllegalArgumentException(zone.id() + "/" + category.id() + " has no pool");
		}
		return TagKey.create(Registries.BIOME, Jugcraft.id("climate/" + zone.id() + "/" + category.id()));
	}

	/** Every pool at its default tag, from a biome lookup that knows the tags. */
	public static ClimatePools defaults(HolderGetter<Biome> biomes) {
		return resolve(biomes, Map.of()).getOrThrow();
	}

	/**
	 * The pools: each override as given, every other pool at its default tag. Fails if an override is an explicitly
	 * empty list, or a default tag is unknown to the lookup. A tag's members may not be bound yet while data packs
	 * load, so a tag's emptiness is checked when a {@link ClimateRemapper} is built, not here.
	 */
	public static DataResult<ClimatePools> resolve(HolderGetter<Biome> biomes,
			Map<ClimateZone, Map<ClimateCategory, HolderSet<Biome>>> overrides) {
		List<String> problems = new ArrayList<>();
		Map<ClimateZone, Map<ClimateCategory, HolderSet<Biome>>> out = new EnumMap<>(ClimateZone.class);
		for (ClimateZone zone : ClimateZone.BANDED) {
			Map<ClimateCategory, HolderSet<Biome>> given = overrides.getOrDefault(zone, Map.of());
			Map<ClimateCategory, HolderSet<Biome>> zonePools = new EnumMap<>(ClimateCategory.class);
			for (ClimateCategory category : ClimateCategory.POOLED) {
				HolderSet<Biome> set = given.get(category);
				if (set == null) {
					TagKey<Biome> tag = defaultTag(zone, category);
					Optional<? extends HolderSet<Biome>> named = biomes == null ? Optional.empty() : biomes.get(tag);
					if (named.isEmpty()) {
						problems.add("pools." + zone.id() + "." + category.id() + ": default tag #" + tag.location() + " is missing");
						continue;
					}
					set = named.get();
				} else if (set.unwrapKey().isEmpty() && set.size() == 0) {
					problems.add("pools." + zone.id() + "." + category.id() + " is an empty list; every pool needs at least one biome");
					continue;
				}
				zonePools.put(category, set);
			}
			out.put(zone, Collections.unmodifiableMap(zonePools));
		}
		if (!problems.isEmpty()) {
			return DataResult.error(() -> String.join("; ", problems));
		}
		return DataResult.success(new ClimatePools(Collections.unmodifiableMap(out)));
	}

	/** The pool of a banded zone and pooled category. */
	public HolderSet<Biome> get(ClimateZone zone, ClimateCategory category) {
		Map<ClimateCategory, HolderSet<Biome>> zonePools = sets.get(zone);
		HolderSet<Biome> set = zonePools == null ? null : zonePools.get(category);
		if (set == null) {
			throw new IllegalArgumentException(zone.id() + "/" + category.id() + " has no pool");
		}
		return set;
	}

	/** Whether this pool is its default tag (and so is left out of the serialized profile). */
	public boolean isDefault(ClimateZone zone, ClimateCategory category) {
		return get(zone, category).unwrapKey().filter(defaultTag(zone, category)::equals).isPresent();
	}

	/** Equal when every pool names the same tag, or lists the same biomes in the same order. */
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof ClimatePools that)) {
			return false;
		}
		for (ClimateZone zone : ClimateZone.BANDED) {
			for (ClimateCategory category : ClimateCategory.POOLED) {
				if (!get(zone, category).unwrap().equals(that.get(zone, category).unwrap())) {
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public int hashCode() {
		int hash = 1;
		for (ClimateZone zone : ClimateZone.BANDED) {
			for (ClimateCategory category : ClimateCategory.POOLED) {
				hash = 31 * hash + get(zone, category).unwrap().hashCode();
			}
		}
		return hash;
	}

	/** The pools that differ from their default tags, for the serialized {@code pools} object (empty if none). */
	public Map<ClimateZone, Map<ClimateCategory, HolderSet<Biome>>> overrides() {
		Map<ClimateZone, Map<ClimateCategory, HolderSet<Biome>>> out = new EnumMap<>(ClimateZone.class);
		for (ClimateZone zone : ClimateZone.BANDED) {
			Map<ClimateCategory, HolderSet<Biome>> zonePools = new EnumMap<>(ClimateCategory.class);
			for (ClimateCategory category : ClimateCategory.POOLED) {
				if (!isDefault(zone, category)) {
					zonePools.put(category, get(zone, category));
				}
			}
			if (!zonePools.isEmpty()) {
				out.put(zone, zonePools);
			}
		}
		return out;
	}
}

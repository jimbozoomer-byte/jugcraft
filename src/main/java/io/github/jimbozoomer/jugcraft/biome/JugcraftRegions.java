package io.github.jimbozoomer.jugcraft.biome;

import com.mojang.datafixers.util.Pair;
import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.config.JugcraftConfig;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;

/**
 * Jugcraft regions (docs/features/biome-regions.md, tools/biomes.py): the Overworld is divided into irregular cells
 * about {@code biomes.region_size} blocks across, and {@code biomes.region_share} of them use the Jugcraft layout:
 * vanilla's climate table with {@link #RULES} applied (a vanilla biome, in the given climate bands, becomes a Jugcraft
 * biome). The rest stay vanilla. Terrain does not depend on biomes, so a region's border is a change of biome only.
 *
 * <p>How: {@code mixin/OverworldBiomeBuilderMixin} records the Jugcraft layout as the builder fills vanilla's table
 * ({@link #recorder}), and lists every Jugcraft biome in vanilla's table at {@link #UNREACHABLE}, a climate no place
 * has, so world generation knows its features and structures. {@code mixin/MultiNoiseBiomeSourceMixin} then answers
 * biome lookups in Jugcraft regions from the Jugcraft layout ({@link Source}). Which cells are Jugcraft regions comes
 * from the world seed, through the climate sampler every lookup carries, so a seed always makes the same world.
 * {@code biomes.enabled=false} turns regions off for new chunks; the biomes stay registered.
 */
public final class JugcraftRegions {
	public static final String FEATURE = "biomes";
	/** Defaults of the region options. Keep in sync with REGIONS in tools/biomes.py. */
	public static final int SIZE = 1024;
	public static final double SHARE = 0.5;
	/** Vanilla's climate band edges (OverworldBiomeBuilder). Keep in sync with tools/biomes.py. */
	private static final float[] TEMPERATURE_BANDS = {-0.45F, -0.15F, 0.2F, 0.55F};
	private static final float[] HUMIDITY_BANDS = {-0.35F, -0.1F, 0.1F, 0.3F};
	/** Where the Jugcraft biomes sit in vanilla's table: further from any place's climate than vanilla's own biomes. */
	public static final Climate.ParameterPoint UNREACHABLE = Climate.parameters(1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F);

	/** In the Jugcraft layout, entries of {@code replaces} in these temperature and humidity bands become {@code biome}. */
	public record Rule(ResourceKey<Biome> replaces, int minTemperature, int maxTemperature, int minHumidity, int maxHumidity,
			ResourceKey<Biome> biome) {
		boolean matches(Pair<Climate.ParameterPoint, ResourceKey<Biome>> entry) {
			if (!entry.getSecond().equals(replaces)) {
				return false;
			}
			int temperature = band(entry.getFirst().temperature(), TEMPERATURE_BANDS);
			int humidity = band(entry.getFirst().humidity(), HUMIDITY_BANDS);
			return temperature >= minTemperature && temperature <= maxTemperature && humidity >= minHumidity && humidity <= maxHumidity;
		}
	}

	/** Batch 1, the seasonal forests. Keep in sync with RULES in tools/biomes.py. */
	public static final List<Rule> RULES = List.of(
			rule(Biomes.TAIGA, 1, 1, 0, 4, "coniferous_forest"),
			rule(Biomes.SNOWY_TAIGA, 0, 0, 0, 4, "snowy_coniferous_forest"),
			rule(Biomes.FOREST, 1, 1, 0, 4, "maple_woods"),
			rule(Biomes.FOREST, 2, 2, 0, 4, "seasonal_forest"),
			rule(Biomes.BIRCH_FOREST, 0, 4, 0, 4, "aspen_glade"),
			rule(Biomes.OLD_GROWTH_BIRCH_FOREST, 0, 4, 0, 4, "aspen_glade"),
			rule(Biomes.PLAINS, 1, 1, 0, 0, "dead_forest"),
			rule(Biomes.PLAINS, 1, 1, 1, 1, "tundra"),
			rule(Biomes.SNOWY_PLAINS, 0, 0, 2, 2, "snowy_forest"),
			rule(Biomes.SNOWY_PLAINS, 0, 0, 1, 1, "muskeg"));

	private static volatile List<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> layout;
	private static volatile int cellQuarts = SIZE / 4;
	private static volatile int share = (int) (SHARE * 65536);

	private JugcraftRegions() {
	}

	private static Rule rule(ResourceKey<Biome> replaces, int minT, int maxT, int minH, int maxH, String biome) {
		return new Rule(replaces, minT, maxT, minH, maxH, ResourceKey.create(Registries.BIOME, Jugcraft.id(biome)));
	}

	/** Reads the region options (after the config has loaded). */
	public static void register() {
		int size = SIZE;
		double fraction = SHARE;
		try {
			size = Math.max(256, Math.min(8192, Integer.parseInt(JugcraftConfig.textOption("biomes.region_size").trim())));
		} catch (NumberFormatException e) {
			Jugcraft.LOGGER.warn("biomes.region_size is not a number of blocks; using {}", SIZE);
		}
		try {
			fraction = Math.max(0.0, Math.min(1.0, Double.parseDouble(JugcraftConfig.textOption("biomes.region_share").trim())));
		} catch (NumberFormatException e) {
			Jugcraft.LOGGER.warn("biomes.region_share is not a fraction from 0 to 1; using {}", SHARE);
		}
		cellQuarts = size / 4;
		share = (int) Math.round(fraction * 65536);
	}

	public static boolean enabled() {
		return JugcraftConfig.isFeatureEnabled(FEATURE);
	}

	/** The Jugcraft biomes the rules place, in rule order. */
	public static Set<ResourceKey<Biome>> biomes() {
		Set<ResourceKey<Biome>> out = new LinkedHashSet<>();
		for (Rule rule : RULES) {
			out.add(rule.biome());
		}
		return out;
	}

	/** The band (0 to 4) where the middle of a climate parameter's range falls. */
	static int band(Climate.Parameter parameter, float[] edges) {
		long middle = (parameter.min() + parameter.max()) / 2;
		int band = 0;
		for (float edge : edges) {
			if (middle >= Climate.quantizeCoord(edge)) {
				band++;
			}
		}
		return band;
	}

	/** The biome a vanilla table entry has in the Jugcraft layout. */
	public static ResourceKey<Biome> regional(Pair<Climate.ParameterPoint, ResourceKey<Biome>> entry) {
		for (Rule rule : RULES) {
			if (rule.matches(entry)) {
				return rule.biome();
			}
		}
		return entry.getSecond();
	}

	/** The Jugcraft layout last recorded from the Overworld biome builder (null: none yet, or regions are off). */
	public static List<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> layout() {
		return layout;
	}

	/** A consumer for the Overworld biome builder that passes entries on and records the Jugcraft layout; null if off. */
	public static Recorder recorder(Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> vanilla) {
		return enabled() ? new Recorder(vanilla) : null;
	}

	/** Records the Jugcraft layout while vanilla's table is built. */
	public static final class Recorder implements Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> {
		private final Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> vanilla;
		private final List<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> recorded = new ArrayList<>();

		private Recorder(Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> vanilla) {
			this.vanilla = vanilla;
		}

		@Override
		public void accept(Pair<Climate.ParameterPoint, ResourceKey<Biome>> entry) {
			vanilla.accept(entry);
			recorded.add(Pair.of(entry.getFirst(), regional(entry)));
		}

		/** Lists the Jugcraft biomes in vanilla's table where no climate reaches them, and publishes the layout. */
		public void finish() {
			for (ResourceKey<Biome> biome : biomes()) {
				vanilla.accept(Pair.of(UNREACHABLE, biome));
			}
			layout = List.copyOf(recorded);
		}
	}

	/** The Jugcraft layout with this biome source's own holders; null if it is not an Overworld with the Jugcraft biomes. */
	public static Climate.ParameterList<Holder<Biome>> layoutFor(Set<Holder<Biome>> possible) {
		List<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> table = layout;
		if (table == null) {
			return null;
		}
		Map<ResourceKey<Biome>, Holder<Biome>> byKey = new HashMap<>();
		for (Holder<Biome> holder : possible) {
			holder.unwrapKey().ifPresent(key -> byKey.put(key, holder));
		}
		if (!byKey.keySet().containsAll(biomes())) {
			return null;
		}
		List<Pair<Climate.ParameterPoint, Holder<Biome>>> values = new ArrayList<>(table.size());
		for (Pair<Climate.ParameterPoint, ResourceKey<Biome>> entry : table) {
			Holder<Biome> holder = byKey.get(entry.getSecond());
			if (holder == null) {
				return null;
			}
			values.add(Pair.of(entry.getFirst(), holder));
		}
		return new Climate.ParameterList<>(values);
	}

	/** A number that differs from seed to seed: the climate at two fixed places. */
	public static long fingerprint(Climate.Sampler sampler) {
		long out = 0x5DEECE66DL;
		for (Climate.TargetPoint point : new Climate.TargetPoint[] {sampler.sample(0, 0, 0), sampler.sample(1024, 0, -1024)}) {
			for (long value : new long[] {point.temperature(), point.humidity(), point.continentalness(), point.erosion(),
					point.depth(), point.weirdness()}) {
				out = mix(out ^ value);
			}
		}
		return out;
	}

	/** Whether the place at quart coordinates (block / 4) is in a Jugcraft region, for a seed's {@link #fingerprint}. */
	public static boolean isJugcraft(long fingerprint, int quartX, int quartZ) {
		int cell = cellQuarts;
		int cx = Math.floorDiv(quartX, cell);
		int cz = Math.floorDiv(quartZ, cell);
		long best = Long.MAX_VALUE;
		long chosen = 0;
		// The nearest of the jittered cell centres around: irregular cells (a Voronoi diagram).
		for (int dx = -1; dx <= 1; dx++) {
			for (int dz = -1; dz <= 1; dz++) {
				long hash = mix(fingerprint ^ mix((cx + dx) * 0x9E3779B97F4A7C15L ^ (cz + dz) * 0xC2B2AE3D27D4EB4FL));
				long px = (long) (cx + dx) * cell + ((hash & 0xFFFF) * cell >>> 16);
				long pz = (long) (cz + dz) * cell + (((hash >>> 16) & 0xFFFF) * cell >>> 16);
				long distance = (quartX - px) * (quartX - px) + (quartZ - pz) * (quartZ - pz);
				if (distance < best) {
					best = distance;
					chosen = hash;
				}
			}
		}
		return ((chosen >>> 32) & 0xFFFF) < share;
	}

	private static long mix(long value) {
		value ^= value >>> 33;
		value *= 0xFF51AFD7ED558CCDL;
		value ^= value >>> 33;
		value *= 0xC4CEB9FE1A85EC53L;
		return value ^ value >>> 33;
	}

	/** One biome source's region state: its Jugcraft layout and its sampler's fingerprint (held by the mixin). */
	public static final class Source {
		private volatile Climate.ParameterList<Holder<Biome>> regional;
		private volatile boolean checked;
		private volatile Fingerprint fingerprint;

		private record Fingerprint(Climate.Sampler sampler, long value) {
		}

		/** The biome at quart coordinates if they lie in a Jugcraft region of this source; null to let vanilla answer. */
		public Holder<Biome> biome(BiomeSource source, int x, int y, int z, Climate.Sampler sampler) {
			if (!enabled()) {
				return null;
			}
			if (!checked) {
				regional = layoutFor(source.possibleBiomes());
				checked = true;
			}
			Climate.ParameterList<Holder<Biome>> list = regional;
			if (list == null) {
				return null;
			}
			Fingerprint print = fingerprint;
			if (print == null || print.sampler() != sampler) {
				print = new Fingerprint(sampler, JugcraftRegions.fingerprint(sampler));
				fingerprint = print;
			}
			return isJugcraft(print.value(), x, z) ? list.findValue(sampler.sample(x, y, z)) : null;
		}
	}
}

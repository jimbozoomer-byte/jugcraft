package io.github.jimbozoomer.jugcraft.world;

import com.mojang.datafixers.util.Pair;
import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.biome.JugcraftRegions;
import io.github.jimbozoomer.jugcraft.config.JugcraftConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLevelEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import org.jspecify.annotations.Nullable;

/**
 * Adam-ondi-Ahman (docs/features/adam-ondi-ahman.md, tools/adam_ondi_ahman.py): quiet river valleys of prairie grass,
 * each with a knoll of three stacked stone altars at its heart, the Ancient Altar on the top one.
 *
 * <p>Where: one valley near the start of every new world ({@link #START_MIN} to {@link #START_MAX} blocks from it,
 * chosen once on the world's first start, after Alpine Spawn and the town, and saved in {@link AdamOndiAhmanState}),
 * and elsewhere one candidate in {@code adam_ondi_ahman.chance} of the cells {@code adam_ondi_ahman.spacing} blocks
 * square, inset {@link #INSET} blocks from its edges, which is a valley if its land is suitable ({@link #suitable}).
 * Both are read from the land's noise (the climate and the generator's heights), so choosing generates nothing, and a
 * seed always makes the same rare valleys.
 *
 * <p>How: a valley is a site (a chunk corner) and the land within about {@link #RADIUS} blocks of it. Biome lookups
 * there answer the valley ({@link #override}, from {@code mixin/MultiNoiseBiomeSourceMixin}), except rivers, oceans,
 * beaches and anything below the surface ({@link #DEPTH}); the biome is listed in vanilla's Overworld table where no
 * climate reaches it ({@link #addToOverworld}), so world generation knows its features. The knoll is the structure
 * {@code jugcraft:altar_knoll}, a vanilla jigsaw structure of one template, placed in exactly the site chunks by
 * {@link AdamOndiAhmanPlacement}. Terrain does not depend on biomes; the knoll blends itself into the land.
 *
 * <p>The state is the loaded Overworld's (seed, noise, generator and start valley), set as it loads, before any chunk
 * generates, and cleared as it unloads. {@code adam_ondi_ahman.enabled=false} stops new valleys and knolls; the biome,
 * the structure and the altar stay registered, so old chunks keep them.
 */
public final class AdamOndiAhman {
	public static final String FEATURE = "adam_ondi_ahman";
	public static final ResourceKey<Biome> BIOME = ResourceKey.create(Registries.BIOME, Jugcraft.id("adam_ondi_ahman"));
	public static final ResourceKey<Structure> KNOLL = ResourceKey.create(Registries.STRUCTURE, Jugcraft.id("altar_knoll"));
	/** Defaults of the rare valleys' settings, and their bounds. Keep in sync with tools/adam_ondi_ahman.py. */
	public static final int SPACING = 3072;
	public static final int MIN_SPACING = 1024;
	public static final int MAX_SPACING = 16384;
	public static final double CHANCE = 0.5;
	/** A valley's reach from its site, wavering by up to {@link #WOBBLE} round its edge. */
	public static final int RADIUS = 176;
	public static final int WOBBLE = 32;
	/** How far inside its cell a rare valley's site is, so the valley never reaches into the next cell. */
	public static final int INSET = RADIUS + WOBBLE + 64;
	/** The valley near the start: rings this far from the start, {@link #START_STEP} apart, every {@link #START_ANGLE} degrees. */
	public static final int START_MIN = 800;
	public static final int START_MAX = 1600;
	public static final int START_STEP = 100;
	public static final int START_ANGLE = 15;
	/** Never this near the origin, where vanilla's own spawn search may already have generated chunks. */
	public static final int ORIGIN_CLEARANCE = 768;
	/** Suitable climate: inland, not the mountains, temperate, and not a river's bed (peaks and valleys). */
	public static final float CONTINENTALNESS = -0.11F;
	public static final float EROSION = -0.375F;
	public static final float TEMPERATURE_MIN = -0.45F;
	public static final float TEMPERATURE_MAX = 0.55F;
	public static final float HUMIDITY_MIN = -0.35F;
	public static final float HUMIDITY_MAX = 0.3F;
	public static final float PEAKS_VALLEYS = -0.2F;
	/** Suitable ground: dry at a 3 x 3 grid of samples this far apart, within these heights at its middle, this even. */
	public static final int REACH = 40;
	public static final int GROUND_MIN = 65;
	public static final int GROUND_MAX = 110;
	public static final int MAX_SLOPE = 10;
	/** Lookups deeper than this (vanilla's depth parameter, where its cave biomes begin) keep their own biome. */
	public static final float DEPTH = 0.2F;
	/**
	 * The knoll's own ground: this near its site (a square), the valley answers even over a river, so the knoll's
	 * structure, which checks the biome at its template's middle, always finds the valley.
	 */
	public static final int KNOLL_GROUND = 64;
	/** No chunk may be loaded this near a start valley's site when it is chosen (its radius and vanilla's margin). */
	public static final int CLEAR_OF_LOADED = RADIUS + WOBBLE + 192;

	public static Block ANCIENT_ALTAR;

	private static final Identifier AFTER_TOWN = Jugcraft.id("adam_ondi_ahman_after_town");
	private static volatile int spacing = SPACING;
	private static volatile int share = (int) Math.round(CHANCE * 65536);
	private static volatile @Nullable World world;

	private AdamOndiAhman() {
	}

	/** A valley's site (a chunk's north-west corner) and the number its edge wavers by. */
	public record Site(int x, int z, long hash) {
		public BlockPos pos() {
			return new BlockPos(x, 0, z);
		}

		public static Site at(int x, int z, long print) {
			return new Site(x, z, mix(print ^ mix(x * 0x9E3779B97F4A7C15L ^ z * 0xC2B2AE3D27D4EB4FL)));
		}
	}

	/** The loaded Overworld's state. */
	private static final class World {
		final long print;
		final BiomeSource source;
		final Climate.Sampler sampler;
		final ChunkGenerator generator;
		final LevelHeightAccessor heights;
		final RandomState random;
		final int spacing;
		final int share;
		final Map<Long, Boolean> suitable = new ConcurrentHashMap<>();
		volatile @Nullable Site start;
		volatile @Nullable Holder<Biome> valley;
		volatile boolean looked;

		World(ServerLevel level, int spacing, int share) {
			this.print = fingerprint(level.getSeed());
			this.generator = level.getChunkSource().getGenerator();
			this.source = generator.getBiomeSource();
			this.random = level.getChunkSource().randomState();
			this.sampler = random.sampler();
			this.heights = level;
			this.spacing = spacing;
			this.share = share;
		}

		@Nullable Holder<Biome> valley() {
			if (!looked) {
				valley = source.possibleBiomes().stream().filter(holder -> holder.is(BIOME)).findFirst().orElse(null);
				looked = true;
			}
			return valley;
		}
	}

	public static void register() {
		ResourceKey<Block> altarKey = ResourceKey.create(Registries.BLOCK, Jugcraft.id("ancient_altar"));
		// Like an end portal frame: it cannot be broken or blown up, or pushed; its loot table is empty.
		ANCIENT_ALTAR = Registry.register(BuiltInRegistries.BLOCK, altarKey, new AncientAltarBlock(BlockBehaviour.Properties.of()
				.mapColor(MapColor.STONE).sound(SoundType.STONE).strength(-1.0F, 3600000.0F).pushReaction(PushReaction.IMMOVEABLE)
				.setId(altarKey)));
		Registry.register(BuiltInRegistries.STRUCTURE_PLACEMENT, Jugcraft.id("adam_ondi_ahman"), AdamOndiAhmanPlacement.CODEC);
		int cells = SPACING;
		double fraction = CHANCE;
		try {
			cells = Math.max(MIN_SPACING, Math.min(MAX_SPACING, Integer.parseInt(JugcraftConfig.textOption("adam_ondi_ahman.spacing").trim())));
		} catch (NumberFormatException e) {
			Jugcraft.LOGGER.warn("adam_ondi_ahman.spacing is not a number of blocks; using {}", SPACING);
		}
		try {
			fraction = Math.max(0.0, Math.min(1.0, Double.parseDouble(JugcraftConfig.textOption("adam_ondi_ahman.chance").trim())));
		} catch (NumberFormatException e) {
			Jugcraft.LOGGER.warn("adam_ondi_ahman.chance is not a fraction from 0 to 1; using {}", CHANCE);
		}
		spacing = cells;
		share = (int) Math.round(fraction * 65536);
		ServerLevelEvents.LOAD.register((server, level) -> {
			if (level.dimension() == Level.OVERWORLD) {
				load(level);
			}
		});
		ServerLevelEvents.UNLOAD.register((server, level) -> {
			if (level.dimension() == Level.OVERWORLD) {
				world = null;
			}
		});
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> world = null);
		// After the world spawn has settled (Alpine Spawn) and the town has been placed (town/TownPlanner).
		ServerLifecycleEvents.SERVER_STARTED.addPhaseOrdering(Event.DEFAULT_PHASE, AFTER_TOWN);
		ServerLifecycleEvents.SERVER_STARTED.addPhaseOrdering(Jugcraft.id("town_after_spawn"), AFTER_TOWN);
		ServerLifecycleEvents.SERVER_STARTED.register(AFTER_TOWN, AdamOndiAhman::chooseStart);
	}

	public static boolean enabled() {
		return JugcraftConfig.isFeatureEnabled(FEATURE);
	}

	/** Takes up the Overworld as it loads, before any chunk of it generates: its noise, and its start valley if it has one. */
	static void load(ServerLevel level) {
		if (!enabled() || !(level.getChunkSource().getGenerator().getBiomeSource() instanceof MultiNoiseBiomeSource)) {
			world = null;
			return;
		}
		World loaded = new World(level, spacing, share);
		BlockPos start = AdamOndiAhmanState.get(level).start();
		if (start != null) {
			loaded.start = Site.at(start.getX(), start.getZ(), loaded.print);
		}
		world = loaded;
	}

	/** Lists the valley's biome in vanilla's Overworld table where no climate reaches it (from the biome builder). */
	public static void addToOverworld(Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> biomes) {
		if (enabled()) {
			biomes.accept(Pair.of(JugcraftRegions.UNREACHABLE, BIOME));
		}
	}

	/**
	 * Chooses the valley near a new world's start, once, on its first start: the nearest suitable site in rings round
	 * the start, clear of the origin and of every chunk already loaded. Saved, chosen or not.
	 */
	private static void chooseStart(MinecraftServer server) {
		ServerLevel level = server.overworld();
		World loaded = world;
		AdamOndiAhmanState state = AdamOndiAhmanState.get(level);
		if (loaded == null || state.chosen() || level.getGameTime() != 0L
				|| !JugcraftConfig.textOption("adam_ondi_ahman.start").trim().equalsIgnoreCase("on")) {
			return;
		}
		BlockPos start = level.getRespawnData().pos();
		Site site = findStart(level, loaded, start);
		state.choose(site == null ? null : site.pos());
		loaded.start = site;
		if (site == null) {
			Jugcraft.LOGGER.info("Adam-ondi-Ahman: no suitable land {}-{} blocks from the start; no valley near it", START_MIN, START_MAX);
		} else {
			Jugcraft.LOGGER.info("Adam-ondi-Ahman: the valley near the start is at {} {}, {} blocks from it", site.x(), site.z(),
					(int) Math.hypot(site.x() - start.getX(), site.z() - start.getZ()));
		}
	}

	private static @Nullable Site findStart(ServerLevel level, World loaded, BlockPos start) {
		int turn = (int) Math.floorMod(loaded.print, 360L);
		for (int r = START_MIN; r <= START_MAX; r += START_STEP) {
			for (int a = 0; a < 360; a += START_ANGLE) {
				double angle = Math.toRadians(turn + a);
				int x = Math.floorDiv(start.getX() + (int) Math.round(r * Math.cos(angle)), 16) * 16;
				int z = Math.floorDiv(start.getZ() + (int) Math.round(r * Math.sin(angle)), 16) * 16;
				if (Math.hypot(x, z) < ORIGIN_CLEARANCE || !suitable(loaded, x, z) || !clearOfLoaded(level, x, z)) {
					continue;
				}
				return Site.at(x, z, loaded.print);
			}
		}
		return null;
	}

	/** Whether no chunk within {@link #CLEAR_OF_LOADED} blocks of (x, z) is loaded (so none of them was made without the valley). */
	private static boolean clearOfLoaded(ServerLevel level, int x, int z) {
		for (int dx = -CLEAR_OF_LOADED; dx <= CLEAR_OF_LOADED; dx += 64) {
			for (int dz = -CLEAR_OF_LOADED; dz <= CLEAR_OF_LOADED; dz += 64) {
				if (level.getChunkSource().getChunkNow((x + dx) >> 4, (z + dz) >> 4) != null) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Whether a knoll may stand at (x, z), from the land's noise alone: the climate there ({@link #climateSuits}), and the
	 * ground dry at a 3 x 3 grid of samples {@link #REACH} apart, {@link #GROUND_MIN} to {@link #GROUND_MAX} high at its
	 * middle, and within {@link #MAX_SLOPE} blocks from highest to lowest.
	 */
	private static boolean suitable(World loaded, int x, int z) {
		if (!climateSuits(loaded.sampler.sample(x >> 2, 64 >> 2, z >> 2))) {
			return false;
		}
		int low = Integer.MAX_VALUE;
		int high = Integer.MIN_VALUE;
		int middle = 0;
		for (int i = -1; i <= 1; i++) {
			for (int j = -1; j <= 1; j++) {
				int sx = x + i * REACH;
				int sz = z + j * REACH;
				int floor = loaded.generator.getBaseHeight(sx, sz, Heightmap.Types.OCEAN_FLOOR_WG, loaded.heights, loaded.random);
				int surface = loaded.generator.getBaseHeight(sx, sz, Heightmap.Types.WORLD_SURFACE_WG, loaded.heights, loaded.random);
				if (surface > floor) {
					return false;
				}
				low = Math.min(low, floor);
				high = Math.max(high, floor);
				if (i == 0 && j == 0) {
					middle = floor;
				}
			}
		}
		return middle >= GROUND_MIN && middle <= GROUND_MAX && high - low <= MAX_SLOPE;
	}

	/** Whether a climate suits a knoll: inland, not the mountains, temperate, and not a river's bed. */
	public static boolean climateSuits(Climate.TargetPoint target) {
		if (target.continentalness() < Climate.quantizeCoord(CONTINENTALNESS) || target.erosion() < Climate.quantizeCoord(EROSION)
				|| target.temperature() < Climate.quantizeCoord(TEMPERATURE_MIN) || target.temperature() >= Climate.quantizeCoord(TEMPERATURE_MAX)
				|| target.humidity() < Climate.quantizeCoord(HUMIDITY_MIN) || target.humidity() >= Climate.quantizeCoord(HUMIDITY_MAX)) {
			return false;
		}
		return peaksAndValleys(target.weirdness() / 10000.0F) >= PEAKS_VALLEYS;
	}

	/** Vanilla's peaks and valleys from weirdness: -1 in a river's valley, 1 on the peaks. */
	public static float peaksAndValleys(float weirdness) {
		return 1.0F - 3.0F * Math.abs(Math.abs(weirdness) - 2.0F / 3.0F);
	}

	private static boolean suitableCached(World loaded, Site site) {
		long key = (long) site.x() << 32 ^ (site.z() & 0xFFFFFFFFL);
		Boolean known = loaded.suitable.get(key);
		if (known == null) {
			// Worked out from the noise alone, so two threads that race here reach the same answer.
			known = suitable(loaded, site.x(), site.z());
			loaded.suitable.put(key, known);
		}
		return known;
	}

	/** The number the rare valleys are drawn from, for a world seed. */
	public static long fingerprint(long seed) {
		return mix(seed ^ 0x41444F4E49313833L);
	}

	/**
	 * The rare valley's candidate site in the cell (cellX, cellZ) of cells {@code cells} blocks square, for a seed's
	 * {@link #fingerprint}; null in the cells (all but {@code share} / 65536 of them) that have none. A candidate is a
	 * chunk's corner, at least {@link #INSET} blocks inside its cell. Whether its land suits is another matter.
	 */
	public static @Nullable Site candidate(long print, int cellX, int cellZ, int cells, int share) {
		long hash = mix(print ^ mix(cellX * 0xC2B2AE3D27D4EB4FL ^ cellZ * 0x9E3779B97F4A7C15L ^ 0x56414C4CL));
		if (((hash >>> 32) & 0xFFFF) >= share) {
			return null;
		}
		int span = cells - 2 * INSET;
		int x = cellX * cells + INSET + (int) ((hash & 0xFFFF) * span >>> 16);
		int z = cellZ * cells + INSET + (int) (((hash >>> 16) & 0xFFFF) * span >>> 16);
		return new Site(Math.floorDiv(x, 16) * 16, Math.floorDiv(z, 16) * 16, hash);
	}

	/** Whether (x, z) lies in the valley round a site: within {@link #RADIUS}, give or take its wavering edge. */
	public static boolean inValley(Site site, int x, int z) {
		long dx = x - site.x();
		long dz = z - site.z();
		long distance = dx * dx + dz * dz;
		if (distance > (long) (RADIUS + WOBBLE) * (RADIUS + WOBBLE)) {
			return false;
		}
		if (distance <= (long) (RADIUS - WOBBLE) * (RADIUS - WOBBLE)) {
			return true;
		}
		double angle = Math.atan2(dz, dx);
		double phase3 = ((site.hash() >>> 40) & 0xFFF) / 4096.0 * 2.0 * Math.PI;
		double phase5 = ((site.hash() >>> 52) & 0xFFF) / 4096.0 * 2.0 * Math.PI;
		double edge = RADIUS + WOBBLE * (0.6 * Math.sin(3.0 * angle + phase3) + 0.4 * Math.sin(5.0 * angle + phase5));
		return distance <= edge * edge;
	}

	/** The valley's site whose land (x, z) is in, in the loaded Overworld; null where there is none. */
	private static @Nullable Site siteAt(World loaded, int x, int z) {
		Site start = loaded.start;
		if (start != null && inValley(start, x, z)) {
			return start;
		}
		Site site = candidate(loaded.print, Math.floorDiv(x, loaded.spacing), Math.floorDiv(z, loaded.spacing), loaded.spacing, loaded.share);
		if (site == null || !inValley(site, x, z) || near(start, site) || !suitableCached(loaded, site)) {
			return null;
		}
		return site;
	}

	/** Whether a rare valley would touch the valley near the start, which then takes its place. */
	private static boolean near(@Nullable Site start, Site site) {
		if (start == null) {
			return false;
		}
		long reach = 2L * (RADIUS + WOBBLE) + 64;
		long dx = site.x() - start.x();
		long dz = site.z() - start.z();
		return dx * dx + dz * dz < reach * reach;
	}

	/**
	 * The biome at quart coordinates ({@code quartX}, {@code quartZ}) of {@code source} for the climate {@code target},
	 * given the answer {@code biome} it already has: the valley where the place is in one, near the surface and not a
	 * river, ocean or beach (but always on the knoll's own ground, {@link #KNOLL_GROUND}); otherwise {@code biome}. Only
	 * the loaded Overworld's source is answered.
	 */
	public static Holder<Biome> override(BiomeSource source, int quartX, int quartZ, Climate.TargetPoint target, Holder<Biome> biome) {
		World loaded = world;
		if (loaded == null || source != loaded.source || target.depth() >= Climate.quantizeCoord(DEPTH)) {
			return biome;
		}
		int x = quartX << 2;
		int z = quartZ << 2;
		Site site = siteAt(loaded, x, z);
		if (site == null) {
			return biome;
		}
		boolean knoll = Math.abs(x - site.x()) <= KNOLL_GROUND && Math.abs(z - site.z()) <= KNOLL_GROUND;
		if (!knoll && (biome.is(BiomeTags.IS_RIVER) || biome.is(BiomeTags.IS_OCEAN) || biome.is(BiomeTags.IS_BEACH))) {
			return biome;
		}
		Holder<Biome> valley = loaded.valley();
		return valley != null ? valley : biome;
	}

	/** Whether the chunk (chunkX, chunkZ) of the loaded Overworld holds a valley's site, where its knoll stands. */
	public static boolean isSiteChunk(int chunkX, int chunkZ) {
		World loaded = world;
		if (loaded == null || !enabled()) {
			return false;
		}
		Site start = loaded.start;
		if (start != null && start.x() >> 4 == chunkX && start.z() >> 4 == chunkZ) {
			return true;
		}
		int x = chunkX << 4;
		int z = chunkZ << 4;
		Site site = candidate(loaded.print, Math.floorDiv(x, loaded.spacing), Math.floorDiv(z, loaded.spacing), loaded.spacing, loaded.share);
		return site != null && site.x() >> 4 == chunkX && site.z() >> 4 == chunkZ && !near(start, site) && suitableCached(loaded, site);
	}

	/** The loaded Overworld's valley near the start; null if it has none (or no Overworld is loaded). */
	public static @Nullable Site startSite() {
		World loaded = world;
		return loaded == null ? null : loaded.start;
	}

	/**
	 * The rare valleys whose sites lie within {@code radius} blocks of (x, z) in the loaded Overworld, suitable land only
	 * (for tests and logs). Reads the noise only.
	 */
	public static List<Site> rareSitesNear(int x, int z, int radius) {
		World loaded = world;
		List<Site> out = new ArrayList<>();
		if (loaded == null) {
			return out;
		}
		for (int cx = Math.floorDiv(x - radius, loaded.spacing); cx <= Math.floorDiv(x + radius, loaded.spacing); cx++) {
			for (int cz = Math.floorDiv(z - radius, loaded.spacing); cz <= Math.floorDiv(z + radius, loaded.spacing); cz++) {
				Site site = candidate(loaded.print, cx, cz, loaded.spacing, loaded.share);
				if (site != null && Math.hypot(site.x() - x, site.z() - z) <= radius && !near(loaded.start, site) && suitableCached(loaded, site)) {
					out.add(site);
				}
			}
		}
		return out;
	}

	/** The rare valleys' settings in force: cell size in blocks, and the share of cells with a candidate (of 65536). */
	public static int spacing() {
		return spacing;
	}

	public static int share() {
		return share;
	}

	private static long mix(long value) {
		value ^= value >>> 33;
		value *= 0xFF51AFD7ED558CCDL;
		value ^= value >>> 33;
		value *= 0xC4CEB9FE1A85EC53L;
		return value ^ value >>> 33;
	}
}

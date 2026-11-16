package io.github.jimbozoomer.jugcraft.test;

import static io.github.jimbozoomer.jugcraft.test.ClimateAuditSupport.*;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.test.ClimateAuditGeometry.Blend;
import io.github.jimbozoomer.jugcraft.test.ClimateAuditGeometry.Geometry;
import io.github.jimbozoomer.jugcraft.world.AdamOndiAhman;
import io.github.jimbozoomer.jugcraft.world.climate.ClimateBiomeSource;
import io.github.jimbozoomer.jugcraft.world.climate.ClimateProfile;
import io.github.jimbozoomer.jugcraft.world.design.DesignBiomeSource;
import io.github.jimbozoomer.jugcraft.world.design.DesignGrid;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.fabric.api.client.gametest.v1.world.TestWorldSave;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.presets.WorldPresets;

/**
 * Worker D's independent climate audit in real worlds. Three new worlds share one seed
 * and one authored design (flat land at 72 over 4,096 blocks round the spawn (320, -192), with paint in the central
 * zone and inside the hot belt), and differ only in their climate:
 * <ol>
 * <li><b>Climate:</b> the design carries the default profile, anchored at the design spawn (Amendment 3).</li>
 * <li><b>Plain:</b> the same design without climate: the control for "unchanged" and for terrain.</li>
 * <li><b>Other:</b> the same design without its own climate, over a {@code jugcraft:climate} base with a different
 * profile and anchor (spawn (-1000, 1500), odd T 97, salt 99): the wrapper in real generation, and a second profile.</li>
 * </ol>
 * The climate world is then reopened. Biomes are read back from FULL generated chunks (as saved) at every band, at the
 * strict limits, in transitions, at real oceans, deep oceans, rivers, beaches and mountains found in each belt, at cave
 * biomes above and below Y0, and on painted cells; each is judged by the audit's own oracle against the base answer
 * at the same quart. Terrain: the noise columns (blocks and fluids before decoration) and their heights are identical
 * with and without climate; deep water stays water or ice after decoration. The Nether and End answer identically
 * with and without climate. Reopening keeps the profile and the stored biomes, and chunks first generated after it,
 * in reverse order, equal what the first session's resolver predicted for them.
 */
public class ClimateBandsAuditClientGameTests implements FabricClientGameTest {
	private static final String SEED = "jugcraft-climate-audit";
	private static final int SPAWN_X = ClimateBandsAuditGameTests.SPAWN_X;
	private static final int SPAWN_Z = ClimateBandsAuditGameTests.SPAWN_Z;
	static final Geometry CLIMATE = Geometry.defaults(SPAWN_X, SPAWN_Z);
	static final int OTHER_SPAWN_X = -1000;
	static final int OTHER_SPAWN_Z = 1500;
	static final Geometry OTHER = new Geometry(OTHER_SPAWN_X, OTHER_SPAWN_Z, 1200, 700, 1500, 2900, 97, 99);
	/** Columns of the band rows: outside the authored grid, so the terrain (oceans, rivers) is the seed's own. */
	private static final int[] BAND_XS = {-24_000, -2_600, 3_100, 24_000};
	/** Columns first generated after reopening. */
	private static final int[] AFTER_XS = {-41_000, 41_000};
	/** South distances of the band rows: every belt, the strict limits exactly, inside every transition, far out. */
	private static final long[] BAND_ROWS = {-12_000, -2_600, -2_504, -2_500, -2_400, -1_500, 1_000, 2_000, 3_000, 4_000, 4_800, 5_400, 5_500, 5_504, 12_000};
	/** Block heights sampled in each generated chunk (quarts of): deep caves, Y0, the surface band and the sky. */
	private static final int[] HEIGHTS = {-56, -40, -24, -4, 0, 8, 24, 40, 48, 56, 64, 72, 96, 140, 200};
	/** The scan for real water, shore and cave biomes: rows per belt (south distance) and the x range. */
	private static final Map<String, long[]> SCAN_ROWS = new LinkedHashMap<>();

	static {
		SCAN_ROWS.put(FRIGID + " north", new long[]{-2_700, -3_600, -7_000});
		SCAN_ROWS.put(HOT, new long[]{3_000, 3_700, 2_600});
		SCAN_ROWS.put(TEMPERATE, new long[]{4_300, 4_900});
		SCAN_ROWS.put(FRIGID + " south", new long[]{5_800, 7_500});
	}

	enum Variant { CLIMATE, PLAIN, OTHER }

	/** A place to generate: a chunk, and why (for messages). */
	record Site(int x, int z, String why) {
	}

	@Override
	public void runTest(ClientGameTestContext context) {
		Map<String, String> climateStored = new ConcurrentHashMap<>();
		Map<String, String> climateBase = new ConcurrentHashMap<>();
		Map<String, String> topology = new ConcurrentHashMap<>();
		Map<String, String> otherDims = new ConcurrentHashMap<>();
		Map<String, String> predicted = new ConcurrentHashMap<>();
		AtomicReference<List<Site>> sites = new AtomicReference<>();
		java.util.concurrent.atomic.AtomicInteger cavesAboveY0 = new java.util.concurrent.atomic.AtomicInteger();
		final TestWorldSave save;
		try (TestSingleplayerContext world = create(context, Variant.CLIMATE)) {
			world.getServer().runOnServer(server -> {
				ServerLevel level = server.overworld();
				DesignBiomeSource design = verify(level, Variant.CLIMATE);
				List<Site> all = new ArrayList<>(bandSites(BAND_XS));
				all.addAll(gridSites());
				all.addAll(foundSites(level, design.base()));
				sites.set(List.copyOf(all));
				Jugcraft.LOGGER.info("[climate-audit] {} sites to generate in the climate world", all.size());
			});
			for (List<Site> batch : batches(sites.get())) {
				world.getServer().runOnServer(server -> {
					ServerLevel level = server.overworld();
					DesignBiomeSource design = (DesignBiomeSource) level.getChunkSource().getGenerator().getBiomeSource();
					for (Site site : batch) {
						topologyOf(level, site, topology, null);
						cavesAboveY0.addAndGet(sample(level, design, design.base(), CLIMATE, site, climateStored, climateBase));
					}
				});
				context.waitTicks(5);
			}
			world.getServer().runOnServer(server -> {
				ServerLevel level = server.overworld();
				DesignBiomeSource design = (DesignBiomeSource) level.getChunkSource().getGenerator().getBiomeSource();
				strictSweep(level, design.base(), CLIMATE);
				blockLevelStrictness(level);
				transitions(level, design.base(), CLIMATE);
				otherDimensions(server, otherDims, null);
				// Predict the chunks first generated after reopening, from this session's resolver (nothing generated).
				for (Site site : bandSites(AFTER_XS)) predict(level, site, predicted);
				// B2: cave biomes the base answers at or above Y0 inside the belts were kept, in generated chunks.
				check(cavesAboveY0.get() > 0, "No preserved cave biome at or above Y0 inside a belt was sampled; the scan found none");
				Jugcraft.LOGGER.info("[climate-audit] climate world: {} stored samples ({} caves at or above Y0 in belts), {} topology columns, {} predicted",
						climateStored.size(), cavesAboveY0.get(), topology.size() / 2, predicted.size());
			});
			save = world.getWorldSave();
		}
		try (TestSingleplayerContext world = create(context, Variant.PLAIN)) {
			world.getServer().runOnServer(server -> {
                var plain = verify(server.overworld(), Variant.PLAIN);
                // Normalize only the control's inherited nearest-neighbour tie handling, not its biome mapping.
                // This matches the opt-in world's base before comparing terrain and the climate transformation.
                ((io.github.jimbozoomer.jugcraft.world.climate.StableBiomeLookup.Source) plain.base()).jugcraft$stabilizeClimateLookup();
            });
			for (List<Site> batch : batches(sites.get())) {
				world.getServer().runOnServer(server -> {
					ServerLevel level = server.overworld();
					DesignBiomeSource design = (DesignBiomeSource) level.getChunkSource().getGenerator().getBiomeSource();
					for (Site site : batch) {
						topologyOf(level, site, null, topology);
						samplePlain(level, design, site, climateBase);
					}
				});
				context.waitTicks(5);
			}
			world.getServer().runOnServer(server -> otherDimensions(server, null, otherDims));
		}
		try (TestSingleplayerContext world = create(context, Variant.OTHER)) {
			world.getServer().runOnServer(server -> verify(server.overworld(), Variant.OTHER));
			List<Site> otherSites = new ArrayList<>(bandSites(new int[]{-2_600, 24_000}));
			for (long s : new long[]{-1_300, -1_150, -1_000, 500, 1_100, 2_000, 2_800, 2_900, 3_500}) {
				otherSites.add(new Site(-24_000, Math.toIntExact(OTHER.anchorZ() + s), "other profile s " + s));
			}
			for (List<Site> batch : batches(otherSites)) {
				world.getServer().runOnServer(server -> {
					ServerLevel level = server.overworld();
					DesignBiomeSource design = (DesignBiomeSource) level.getChunkSource().getGenerator().getBiomeSource();
					BiomeSource seeded = ((ClimateBiomeSource) design.base()).base();
					Map<String, String> stored = new ConcurrentHashMap<>(), base = new ConcurrentHashMap<>();
					for (Site site : batch) sample(level, design, seeded, OTHER, site, stored, base);
					// The same seed answers the same base biomes in every world: only the profile differs.
					base.forEach((key, biome) -> {
						String first = climateBase.get(key);
						check(first == null || first.equals(biome), "The seeded base differs between worlds at " + key + ": " + first + " vs " + biome);
					});
					// Isolation: where the two profiles disagree, this world follows its own (checked in sample), not the other's.
					int disagree = 0;
					for (Site site : batch) {
						if (!OTHER.allowedForQuart(site.z() >> 2).equals(CLIMATE.allowedForQuart(site.z() >> 2))) disagree++;
					}
					Jugcraft.LOGGER.info("[climate-audit] other profile: {} samples, {} of {} sites where the two profiles disagree", stored.size(),
							disagree, batch.size());
				});
				context.waitTicks(5);
			}
			world.getServer().runOnServer(server -> strictSweep(server.overworld(),
					((ClimateBiomeSource) ((DesignBiomeSource) server.overworld().getChunkSource().getGenerator().getBiomeSource()).base()).base(), OTHER));
		}
		try (TestSingleplayerContext world = save.open()) {
			world.getServer().runOnServer(server -> {
				ServerLevel level = server.overworld();
				DesignBiomeSource design = verify(level, Variant.CLIMATE);
				// Stored biomes came back from disk unchanged.
				Map<String, String> again = new ConcurrentHashMap<>(), base = new ConcurrentHashMap<>();
				for (Site site : sites.get()) sample(level, design, design.base(), CLIMATE, site, again, base);
				check(again.equals(climateStored), "Stored biomes changed on reopening: " + difference(climateStored, again));
				// Chunks first generated now, in reverse order, equal the first session's prediction: no dependence on the
				// run, on instance state or on the order chunks are generated in, after two other worlds ran in between.
				List<Site> after = new ArrayList<>(bandSites(AFTER_XS));
				java.util.Collections.reverse(after);
				Map<String, String> fresh = new ConcurrentHashMap<>();
				for (Site site : after) sample(level, design, design.base(), CLIMATE, site, fresh, new ConcurrentHashMap<>());
				Map<String, String> wanted = new TreeMap<>();
				fresh.keySet().forEach(key -> wanted.put(key, predicted.get(key)));
				check(new TreeMap<>(fresh).equals(wanted), "Chunks generated after reopening differ from the first session's answers: "
						+ difference(wanted, fresh));
				otherDimensions(server, null, otherDims);
				Jugcraft.LOGGER.info("[climate-audit] reopened: profile, {} stored samples and {} reverse-order new samples agree", again.size(), fresh.size());
			});
		}
	}

	// -----------------------------------------------------------------------------------------------------------------
	// Worlds

	private static TestSingleplayerContext create(ClientGameTestContext context, Variant variant) {
		return context.worldBuilder().adjustSettings(creator -> {
			creator.setGameMode(WorldCreationUiState.SelectedGameMode.CREATIVE);
			creator.setWorldType(new WorldCreationUiState.WorldTypeEntry(creator.getSettings().worldgenLoadContext()
					.lookupOrThrow(Registries.WORLD_PRESET).getOrThrow(WorldPresets.NORMAL)));
			creator.setSeed(SEED);
			creator.updateDimensions((registries, dimensions) -> dimensions.replaceOverworldGenerator(registries, generator(registries, variant)));
		}).create();
	}

	/** The audit design's generator for a variant, round-tripped through the codec level.dat uses, before any chunk exists. */
	static ChunkGenerator generator(HolderLookup.Provider registries, Variant variant) {
		RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, registries);
		DesignGrid grid = ClimateBandsAuditGameTests.grid();
		var original = registries.lookupOrThrow(Registries.NOISE_SETTINGS).getOrThrow(NoiseGeneratorSettings.OVERWORLD).value();
		JsonObject settings = NoiseGeneratorSettings.DIRECT_CODEC.encodeStart(ops, original).getOrThrow().getAsJsonObject();
		JsonObject router = settings.getAsJsonObject("noise_router");
		JsonObject land = new JsonObject();
		land.addProperty("type", "jugcraft:design_density");
		land.add("grid", DesignGrid.CODEC.encodeStart(ops, grid).getOrThrow());
		land.add("input", router.get("final_density"));
		JsonObject surface = new JsonObject();
		surface.addProperty("type", "jugcraft:design_density");
		surface.addProperty("surface", true);
		surface.add("grid", land.get("grid"));
		surface.add("input", router.get("chunk_surface_level"));
		router.add("final_density", land);
		router.add("chunk_surface_level", surface);
		if (settings.has("aquifers")) settings.getAsJsonObject("aquifers").add("surface_level", surface);
		NoiseGeneratorSettings noise = NoiseGeneratorSettings.DIRECT_CODEC.parse(ops, settings).getOrThrow();
		JsonObject seeded = new JsonObject();
		seeded.addProperty("type", "minecraft:multi_noise");
		seeded.addProperty("preset", "minecraft:overworld");
		JsonElement base = variant == Variant.OTHER ? climateWrapper(seeded, json(OTHER)) : seeded;
		var biomes = registries.lookupOrThrow(Registries.BIOME);
		BlockPos spawn = variant == Variant.OTHER ? new BlockPos(OTHER_SPAWN_X, 72, OTHER_SPAWN_Z) : new BlockPos(SPAWN_X, 72, SPAWN_Z);
		var plainDesign = new DesignBiomeSource(BiomeSource.CODEC.parse(ops, base).getOrThrow(), grid,
				List.of(biomes.getOrThrow(Biomes.DESERT), biomes.getOrThrow(Biomes.SNOWY_PLAINS)), ClimateBandsAuditGameTests.cells(grid), spawn,
				java.util.Optional.empty());
		JsonObject design = BiomeSource.CODEC.encodeStart(ops, plainDesign).getOrThrow().getAsJsonObject();
		if (variant == Variant.CLIMATE) design.add("climate", json(CLIMATE));
		BiomeSource source = BiomeSource.CODEC.parse(ops, design).getOrThrow();
		ChunkGenerator generator = new NoiseBasedChunkGenerator(source, Holder.direct(noise));
		return ChunkGenerator.CODEC.parse(ops, ChunkGenerator.CODEC.encodeStart(ops, generator).getOrThrow()).getOrThrow();
	}

	private static JsonObject climateWrapper(JsonElement base, JsonObject climate) {
		JsonObject source = new JsonObject();
		source.addProperty("type", "jugcraft:climate");
		source.add("base", base);
		source.add("climate", climate);
		return source;
	}

	/** The world's generator and spawn as the variant requires; the Nether and End untouched; no Adam-ondi-Ahman (a design). */
	private static DesignBiomeSource verify(ServerLevel level, Variant variant) {
		var ops = RegistryOps.create(JsonOps.INSTANCE, level.registryAccess());
		BiomeSource source = level.getChunkSource().getGenerator().getBiomeSource();
		check(source instanceof DesignBiomeSource, "The designed biome source is missing: " + source);
		DesignBiomeSource design = (DesignBiomeSource) source;
		JsonObject json = BiomeSource.CODEC.encodeStart(ops, design).getOrThrow().getAsJsonObject();
		switch (variant) {
			case CLIMATE -> {
				check(design.climate().isPresent(), "The design lost its climate");
				JsonElement profile = ClimateProfile.CODEC.encodeStart(ops, design.climate().get()).getOrThrow();
				check(profile.equals(json(CLIMATE)), "The saved profile changed: " + profile);
				check(design.climate().get().anchorX() == SPAWN_X && design.climate().get().anchorZ() == SPAWN_Z, "The anchor moved");
			}
			case PLAIN -> check(design.climate().isEmpty() && !json.toString().contains("climate"), "The plain design gained climate: " + json);
			case OTHER -> {
				check(design.climate().isEmpty() && design.base() instanceof ClimateBiomeSource, "The other world must band through its base");
				JsonElement profile = ClimateProfile.CODEC.encodeStart(ops, ((ClimateBiomeSource) design.base()).climate()).getOrThrow();
				check(profile.equals(json(OTHER)), "The other profile changed: " + profile);
			}
		}
		BlockPos spawn = level.getRespawnData().pos();
		int x = variant == Variant.OTHER ? OTHER_SPAWN_X : SPAWN_X, z = variant == Variant.OTHER ? OTHER_SPAWN_Z : SPAWN_Z;
		check(spawn.getX() == x && spawn.getZ() == z, variant + ": spawn did not use the design: " + spawn);
		check(AdamOndiAhman.startSite() == null, variant + ": Adam-ondi-Ahman takes up no designed world (instanceof MultiNoiseBiomeSource gate)");
		MinecraftServer server = level.getServer();
		JsonElement nether = BiomeSource.CODEC.encodeStart(ops, server.getLevel(Level.NETHER).getChunkSource().getGenerator().getBiomeSource()).getOrThrow();
		JsonElement end = BiomeSource.CODEC.encodeStart(ops, server.getLevel(Level.END).getChunkSource().getGenerator().getBiomeSource()).getOrThrow();
		check(nether.equals(com.google.gson.JsonParser.parseString("{\"type\":\"minecraft:multi_noise\",\"preset\":\"minecraft:nether\"}")),
				"The Nether's source changed: " + nether);
		check(end.equals(com.google.gson.JsonParser.parseString("{\"type\":\"minecraft:the_end\"}")), "The End's source changed: " + end);
		return design;
	}

	// -----------------------------------------------------------------------------------------------------------------
	// Sites

	private static List<Site> bandSites(int[] xs) {
		List<Site> out = new ArrayList<>();
		for (long s : BAND_ROWS) for (int x : xs) out.add(new Site(x, CLIMATE.z(s), "band s " + s));
		return out;
	}

	/** Painted cells in the hot belt and round the spawn, and unpainted grid land in the hot belt. */
	private static List<Site> gridSites() {
		List<Site> out = new ArrayList<>();
		for (int dx : new int[]{-600, 0, 600}) for (long s : new long[]{2_250, 2_450, 2_650}) out.add(new Site(SPAWN_X + dx, CLIMATE.z(s), "painted hot s " + s));
		for (int dx : new int[]{-1_200, 1_200}) for (long s : new long[]{2_300, 2_600}) out.add(new Site(SPAWN_X + dx, CLIMATE.z(s), "unpainted grid hot s " + s));
		out.add(new Site(SPAWN_X, SPAWN_Z, "painted central at the spawn"));
		out.add(new Site(SPAWN_X + 256, SPAWN_Z - 256, "painted central"));
		return out;
	}

	/**
	 * Real oceans, deep oceans, rivers, beaches, mountains and land in every belt, caves above and below Y0, and any
	 * surface special (mushroom fields, islands), found from the seeded base answer outside the authored grid.
	 */
	private static List<Site> foundSites(ServerLevel level, BiomeSource seeded) {
		BiomeResolver base = seeded.createUncachedResolver(level.getChunkSource().randomState());
		DesignGrid grid = ClimateBandsAuditGameTests.grid();
		List<Site> out = new ArrayList<>();
		Map<String, Integer> found = new LinkedHashMap<>();
		List<String> missing = new ArrayList<>();
		for (var belt : SCAN_ROWS.entrySet()) {
			for (long s : belt.getValue()) {
				int z = CLIMATE.z(s);
				for (int x = -60_000; x <= 60_000; x += 48) {
					if (grid.weight(x, z) > 0) continue;
					Holder<Biome> surface = base.getNoiseBiome(x >> 2, 16, z >> 2);
					List<String> categories = categoriesOf(surface);
					String c = categories.isEmpty() ? "uncategorized" : categories.getFirst();
					String key = belt.getKey() + " " + c;
					if (!c.equals(PRESERVE) && found.merge(key, 1, Integer::sum) == 1) out.add(new Site(x, z, key + " (" + id(surface) + ")"));
					if (surface.is(Biomes.MUSHROOM_FIELDS) || surface.is(Jugcraft.id("flower_isle")) || surface.is(Jugcraft.id("elder_vale"))
							|| surface.is(Jugcraft.id("toadstool_field"))) {
						if (found.merge("special", 1, Integer::sum) <= 2) out.add(new Site(x, z, belt.getKey() + " surface special " + id(surface)));
					}
					if (x % 768 == 0) {
						for (int qy : new int[]{2, 6, 10, 14, 18, 24}) {
							if (preserved(base.getNoiseBiome(x >> 2, qy, z >> 2)) && found.merge(belt.getKey() + " cave above Y0", 1, Integer::sum) == 1) {
								out.add(new Site(x, z, belt.getKey() + " cave at y " + qy * 4));
							}
						}
						if (preserved(base.getNoiseBiome(x >> 2, -10, z >> 2)) && found.merge(belt.getKey() + " cave below Y0", 1, Integer::sum) == 1) {
							out.add(new Site(x, z, belt.getKey() + " cave at y -40"));
						}
					}
				}
			}
			for (String needed : List.of("ocean", "deep_ocean", "river", "beach", "land", "cave above Y0", "cave below Y0")) {
				if (!found.containsKey(belt.getKey() + " " + needed)) missing.add(belt.getKey() + " " + needed);
			}
		}
		Jugcraft.LOGGER.info("[climate-audit] found in the seed: {}", found);
		check(!found.keySet().stream().anyMatch(k -> k.endsWith("uncategorized")), "The seeded Overworld answered an uncategorized biome: " + found);
		check(missing.isEmpty(), "Seed " + SEED + " has none of " + missing + " in the scanned rows; choose another seed (a test design limit, not a climate fault)");
		return out;
	}

	/** Small batches, so the server ticks between them and unloads the forced samples. */
	private static List<List<Site>> batches(List<Site> sites) {
		List<List<Site>> out = new ArrayList<>();
		for (int i = 0; i < sites.size(); i += 12) out.add(sites.subList(i, Math.min(sites.size(), i + 12)));
		return out;
	}

	// -----------------------------------------------------------------------------------------------------------------
	// Checks

	private static String key(int qx, int qy, int qz) {
		return qx + " " + qy + " " + qz;
	}

	private static boolean painted(int qx, int qy, int qz) {
		DesignGrid grid = ClimateBandsAuditGameTests.grid();
		int bx = qx * 4, bz = qz * 4;
		return qy >= 0 && grid.weight(bx, bz) > 0 && ClimateBandsAuditGameTests.cells(grid).get(grid.cell(bx, bz)) > 0;
	}

	private static Holder<Biome> palette(ServerLevel level, int qx, int qz) {
		DesignGrid grid = ClimateBandsAuditGameTests.grid();
		int id = ClimateBandsAuditGameTests.cells(grid).get(grid.cell(qx * 4, qz * 4));
		return level.registryAccess().lookupOrThrow(Registries.BIOME).getOrThrow(id == 1 ? Biomes.DESERT : Biomes.SNOWY_PLAINS);
	}

	/**
	 * Generates the site's chunk (FULL, as saved) and judges every stored quart at {@link #HEIGHTS} against the oracle:
	 * paint unchanged; otherwise the profile's zone(s) for the quart applied to the seeded base answer there. The
	 * stored biome must also equal the world source's uncached answer (generation and /locate agree).
	 */
	private static int sample(ServerLevel level, DesignBiomeSource design, BiomeSource seeded, Geometry profile, Site site,
			Map<String, String> stored, Map<String, String> bases) {
		int cavesAboveY0 = 0;
		ChunkAccess chunk = level.getChunk(site.x() >> 4, site.z() >> 4);
		BiomeResolver base = seeded.createUncachedResolver(level.getChunkSource().randomState());
		int qx0 = (site.x() >> 4) << 2, qz0 = (site.z() >> 4) << 2;
		for (int dx = 0; dx < 4; dx++) {
			for (int dz = 0; dz < 4; dz++) {
				int qx = qx0 + dx, qz = qz0 + dz;
				for (int y : HEIGHTS) {
					int qy = y >> 2;
					Holder<Biome> got = chunk.getNoiseBiome(qx, qy, qz);
					Holder<Biome> was = base.getNoiseBiome(qx, qy, qz);
					String where = site.why() + " at quart " + key(qx, qy, qz) + " (block " + qx * 4 + " " + y + " " + qz * 4 + ")";
					if (painted(qx, qy, qz)) {
						check(got.equals(palette(level, qx, qz)), "A painted cell changed: " + id(got) + " " + where);
					} else {
						Set<String> zones = profile.allowedForQuart(qz);
						String why = null;
						for (String zone : zones) {
							why = violation(zone, was, got);
							if (why == null) break;
						}
						check(why == null, why + " " + where + ", zones " + zones);
						if (y >= 0 && preserved(was) && !zones.contains(CENTRAL)) cavesAboveY0++;
					}
					Holder<Biome> uncached = level.getUncachedNoiseBiome(qx, qy, qz);
					check(uncached.equals(got), "Generation stored " + id(got) + " but the source answers " + id(uncached) + " " + where);
					stored.put(key(qx, qy, qz), id(got));
					bases.put(key(qx, qy, qz), id(was));
				}
			}
		}
		return cavesAboveY0;
	}

	/** The plain design: paint, else exactly the seeded base, which is the same in both worlds. */
	private static void samplePlain(ServerLevel level, DesignBiomeSource design, Site site, Map<String, String> climateBase) {
		ChunkAccess chunk = level.getChunk(site.x() >> 4, site.z() >> 4);
		BiomeResolver base = design.base().createUncachedResolver(level.getChunkSource().randomState());
		int qx0 = (site.x() >> 4) << 2, qz0 = (site.z() >> 4) << 2;
		for (int dx = 0; dx < 4; dx++) {
			for (int dz = 0; dz < 4; dz++) {
				int qx = qx0 + dx, qz = qz0 + dz;
				for (int y : HEIGHTS) {
					int qy = y >> 2;
					Holder<Biome> got = chunk.getNoiseBiome(qx, qy, qz);
					Holder<Biome> was = base.getNoiseBiome(qx, qy, qz);
					String where = site.why() + " at quart " + key(qx, qy, qz);
					check(got.equals(painted(qx, qy, qz) ? palette(level, qx, qz) : was), "A design without climate changed: " + id(got) + " " + where);
					String first = climateBase.get(key(qx, qy, qz));
					check(first == null || first.equals(id(was)), "The seeded base differs from the climate world's at " + where + ": " + first + " vs " + id(was));
				}
			}
		}
	}

	/** Records the next session's chunks as this session's resolver answers them, without generating anything. */
	private static void predict(ServerLevel level, Site site, Map<String, String> into) {
		int qx0 = (site.x() >> 4) << 2, qz0 = (site.z() >> 4) << 2;
		for (int dx = 0; dx < 4; dx++) for (int dz = 0; dz < 4; dz++) for (int y : HEIGHTS) {
			into.put(key(qx0 + dx, y >> 2, qz0 + dz), id(level.getUncachedNoiseBiome(qx0 + dx, y >> 2, qz0 + dz)));
		}
	}

	/**
	 * Terrain is never changed by climate. The noise column (blocks and fluids before surface rules and decoration) and
	 * the generation heightmaps are compared exactly between the climate and plain worlds; after decoration, deep water
	 * (noise floor 55 or lower) is water or ice in both, at sea level. Decoration differs by biome on purpose (ice,
	 * snow, sand, icebergs, coral, lakes), so it is not compared block for block. Records into {@code first}, compares
	 * against {@code against}.
	 */
	private static void topologyOf(ServerLevel level, Site site, Map<String, String> first, Map<String, String> against) {
		ChunkGenerator generator = level.getChunkSource().getGenerator();
		var random = level.getChunkSource().randomState();
		int[][] columns = {{8, 8}, {1, 14}, {14, 3}};
		for (int[] c : columns) {
			int x = (site.x() & ~15) + c[0], z = (site.z() & ~15) + c[1];
			var column = generator.getBaseColumn(x, z, level, random);
			StringBuilder runs = new StringBuilder();
			char previous = 0;
			int length = 0;
			for (int y = level.getMinY(); y <= level.getMaxY(); y++) {
				BlockState state = column.getBlock(y);
				char kind = state.getFluidState().is(FluidTags.WATER) ? 'W' : state.getFluidState().is(FluidTags.LAVA) ? 'L' : state.isAir() ? 'A' : 'S';
				if (kind != previous && length > 0) {
					runs.append(previous).append(length);
					length = 0;
				}
				previous = kind;
				length++;
			}
			runs.append(previous).append(length);
			int floor = generator.getBaseHeight(x, z, Heightmap.Types.OCEAN_FLOOR_WG, level, random);
			int top = generator.getBaseHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG, level, random);
			level.getChunk(x >> 4, z >> 4);
			BlockState sea = level.getBlockState(new BlockPos(x, SEA_LEVEL - 1, z));
			boolean wet = sea.getFluidState().is(FluidTags.WATER) || sea.is(BlockTags.ICE) || sea.is(Blocks.SNOW_BLOCK);
			String record = runs + " floor " + floor + " top " + top;
			String key = x + " " + z;
			if (first != null) {
				first.put(key, record);
				first.put(key + " wet", floor <= 55 ? String.valueOf(wet) : "shallow");
			}
			if (against != null) {
				String climate = against.get(key);
				check(climate == null || climate.equals(record), "Climate changed the terrain at " + key + " (" + site.why() + "): " + climate + " vs " + record);
				String climateWet = against.get(key + " wet");
				if (climateWet != null && !climateWet.equals("shallow") && floor <= 55) {
					check(climateWet.equals(String.valueOf(wet)), "Deep water at " + key + " (" + site.why() + ") is " + (wet ? "" : "not ")
							+ "water or ice without climate but " + climateWet + " with it");
				}
			}
		}
	}

	/**
	 * Strict frigid through the world's own source, densely: every quart that any block at or beyond a frigid limit
	 * reads is a frigid pool member of the base's role, or an unchanged cave biome; 80,000 blocks east to west.
	 */
	private static void strictSweep(ServerLevel level, BiomeSource seeded, Geometry profile) {
		BiomeResolver base = seeded.createUncachedResolver(level.getChunkSource().randomState());
		int checked = 0;
		for (long s : new long[]{-(long) profile.north(), -(long) profile.north() - 1, -(long) profile.north() - 4, -(long) profile.north() - 100,
				-(long) profile.north() - 100_000, profile.southFrigid(), profile.southFrigid() + 1L, profile.southFrigid() + 4L, profile.southFrigid() + 3_500L,
				profile.southFrigid() + 100_000L}) {
			int qz = Math.floorDiv(profile.z(s), 4);
			check(profile.allowedForQuart(qz).equals(Set.of(FRIGID)), "Audit row s " + s + " is not strict");
			for (int x = -40_000; x <= 40_000; x += 160) {
				if (painted(x >> 2, 16, qz)) continue;
				for (int qy : new int[]{-12, 0, 8, 16, 20, 32}) {
					Holder<Biome> got = level.getUncachedNoiseBiome(x >> 2, qy, qz);
					String why = violation(FRIGID, base.getNoiseBiome(x >> 2, qy, qz), got);
					check(why == null, "Not strictly frigid at s " + s + " x " + x + " y " + qy * 4 + ": " + why);
					checked++;
				}
			}
		}
		Jugcraft.LOGGER.info("[climate-audit] strict sweep: {} quarts at and beyond the frigid limits of {}", checked, profile);
	}

	/** At block level (Minecraft's fuzzed biome lookup over the stored quarts), no block at or beyond a limit shows a non-frigid biome. */
	private static void blockLevelStrictness(ServerLevel level) {
		int checked = 0;
		for (long limit : new long[]{-(long) CLIMATE.north(), CLIMATE.southFrigid()}) {
			for (int x : BAND_XS) {
				for (int dz = 0; dz < 24; dz++) {
					long s = limit < 0 ? limit - dz : limit + dz;
					BlockPos at = new BlockPos(x + dz % 16, 64, CLIMATE.z(s));
					level.getChunk(at.getX() >> 4, at.getZ() >> 4);
					for (int y : new int[]{-30, 64, 90}) {
						Holder<Biome> got = level.getBiome(at.atY(y));
						boolean frigid = preserved(got);
						for (String c : CATEGORIES) frigid |= got.is(pool(FRIGID, c));
						check(frigid, "Block " + at.getX() + " " + y + " " + at.getZ() + " at s " + s + " shows " + id(got) + ", not a frigid biome");
						checked++;
					}
				}
			}
		}
		Jugcraft.LOGGER.info("[climate-audit] block-level strictness: {} blocks at and beyond the limits", checked);
	}

	/**
	 * Every quart of every blend takes one of its two adjacent zones, and both actually occur (a transition, on the inner
	 * side of each frigid limit, centred on the hot edges); a quart outside the blends takes its one zone.
	 */
	private static void transitions(ServerLevel level, BiomeSource seeded, Geometry profile) {
		BiomeResolver base = seeded.createUncachedResolver(level.getChunkSource().randomState());
		for (Blend blend : profile.blends()) {
			String inner = blend.name().equals("north frigid") ? CENTRAL : blend.name().equals("central to hot") ? CENTRAL
					: blend.name().equals("hot to temperate") ? HOT : TEMPERATE;
			int innerSeen = 0, outerSeen = 0, samples = 0;
			for (long s = blend.from() - 48; s < blend.to() + 48; s += 4) {
				int qz = Math.floorDiv(profile.z(s), 4);
				Set<String> zones = profile.allowedForQuart(qz);
				for (int i = 0; i < 24; i++) {
					int x = -92_000 + i * 8_009;
					if (painted(x >> 2, 16, qz)) continue;
					Holder<Biome> was = base.getNoiseBiome(x >> 2, 16, qz);
					Holder<Biome> got = level.getUncachedNoiseBiome(x >> 2, 16, qz);
					boolean asInner = violation(inner, was, got) == null, asOuter = violation(blend.outer(), was, got) == null;
					check((zones.contains(inner) && asInner) || (zones.contains(blend.outer()) && asOuter),
							blend.name() + ": " + id(was) + " became " + id(got) + " at s " + s + " x " + x + ", zones " + zones);
					if (zones.size() == 2 && !preserved(was)) {
						if (asInner && !asOuter) innerSeen++;
						if (asOuter && !asInner) outerSeen++;
					}
					samples++;
				}
			}
			check(innerSeen > 0 && outerSeen > 0, blend.name() + ": the blend shows no transition (inner-only " + innerSeen + ", outer-only " + outerSeen + ")");
			Jugcraft.LOGGER.info("[climate-audit] {} blend: {} quarts, {} plainly inner, {} plainly outer", blend.name(), samples, innerSeen, outerSeen);
		}
	}

	/** The Nether and End answer the same with and without climate (records into {@code first}, compares against {@code against}). */
	private static void otherDimensions(MinecraftServer server, Map<String, String> first, Map<String, String> against) {
		for (var dimension : List.of(Level.NETHER, Level.END)) {
			ServerLevel level = server.getLevel(dimension);
			ChunkAccess chunk = level.getChunk(1_000 >> 4, -2_000 >> 4);
			for (int x = -8_000; x <= 8_000; x += 1_000) {
				for (int z = -8_000; z <= 8_000; z += 1_000) {
					for (int qy : new int[]{4, 16, 24}) {
						String key = dimension.identifier() + " " + key(x >> 2, qy, z >> 2);
						String got = id(level.getUncachedNoiseBiome(x >> 2, qy, z >> 2));
						if (first != null) first.put(key, got);
						if (against != null) check(got.equals(against.get(key)), "The " + dimension.identifier() + " changed at " + key + ": " + got);
					}
				}
			}
			String stored = dimension.identifier() + " stored " + id(chunk.getNoiseBiome(1_000 >> 2, 16, -2_000 >> 2));
			if (first != null) first.put(dimension.identifier() + " stored", stored);
			if (against != null) check(stored.equals(against.get(dimension.identifier() + " stored")), "The " + dimension.identifier() + " stored a different biome: " + stored);
		}
	}

	private static String difference(Map<String, String> expected, Map<String, String> got) {
		List<String> out = new ArrayList<>();
		for (var entry : new TreeMap<>(expected).entrySet()) {
			if (!entry.getValue().equals(got.get(entry.getKey()))) out.add(entry.getKey() + ": " + entry.getValue() + " vs " + got.get(entry.getKey()));
			if (out.size() >= 8) break;
		}
		if (out.isEmpty()) out.add("sizes " + expected.size() + " vs " + got.size());
		return String.valueOf(out);
	}

	private static void check(boolean pass, String message) {
		if (!pass) throw new AssertionError(message);
	}
}

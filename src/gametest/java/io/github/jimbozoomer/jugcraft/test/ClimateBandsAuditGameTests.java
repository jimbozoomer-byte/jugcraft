package io.github.jimbozoomer.jugcraft.test;

import static io.github.jimbozoomer.jugcraft.test.ClimateAuditSupport.*;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.biome.JugcraftRegions;
import io.github.jimbozoomer.jugcraft.menagerie.CaveWorkshop;
import io.github.jimbozoomer.jugcraft.menagerie.LandscapeBiomes;
import io.github.jimbozoomer.jugcraft.season.JugcraftSeasons;
import io.github.jimbozoomer.jugcraft.test.ClimateAuditGeometry.Blend;
import io.github.jimbozoomer.jugcraft.test.ClimateAuditGeometry.Geometry;
import io.github.jimbozoomer.jugcraft.world.AdamOndiAhman;
import io.github.jimbozoomer.jugcraft.world.AlpineSpawn;
import io.github.jimbozoomer.jugcraft.world.climate.ClimateBands;
import io.github.jimbozoomer.jugcraft.world.climate.ClimateBiomeSource;
import io.github.jimbozoomer.jugcraft.world.climate.ClimateProfile;
import io.github.jimbozoomer.jugcraft.world.climate.ClimateRemapper;
import io.github.jimbozoomer.jugcraft.world.design.DesignBiomeSource;
import io.github.jimbozoomer.jugcraft.world.design.DesignGrid;
import io.github.jimbozoomer.jugcraft.world.design.WorldDesigner;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;

/**
 * Worker D's independent audit of the climate bands (CONTRACT.md and Amendments 1-4), as server game tests: written,
 * not run in Minecraft. Every expectation comes from the audit's own oracle ({@link ClimateAuditGeometry},
 * {@link ClimateAuditSupport}) and the repository's own biome metadata, not from the climate core's code. Real chunk
 * generation, save/reopen, Nether/End and multi-world checks are {@link ClimateBandsAuditClientGameTests}.
 *
 * <p>Covered here: the profile codec in JSON and NBT, at every one of Amendment 4's eleven rule boundaries (the 30M
 * anchor and odd T included) and with fractional or out-of-range numbers; old design JSON and old level settings
 * unchanged; the design anchor must be the design spawn; double application refused at any depth; every Overworld
 * biome in exactly one category consistent with its real metadata; every pool non-empty, in role and honest about
 * its weather (seasons tags, snow, rain); possibleBiomes; the core's zones against the oracle (strict limits, inner-side
 * blends, the ramp, coherent patches, salt); caves kept at any Y; two profiles in one server; no shared statics;
 * order-independent answers; the catalog's climate capability.
 */
public class ClimateBandsAuditGameTests {
	/**
	 * Frigid pool members accepted although they rain at sea level, each a documented exception (strict frigid is about
	 * biomes, and these are vanilla's frozen oceans), and nothing else (coordinator decisions; an owner decision later).
	 * Vanilla's deep frozen ocean is 0.5 with the frozen modifier, so it rains everywhere at sea level; vanilla's frozen
	 * ocean is 0 with the same modifier, whose 0.2 patches rain and leave the water open. Any other member that rains
	 * anywhere fails.
	 */
	static final Set<String> RAINS_IN_FRIGID = Set.of("minecraft:deep_frozen_ocean", "minecraft:frozen_ocean");
	/** A designed world's spawn and so its anchor (Amendment 3): deliberately not the origin. */
	static final int SPAWN_X = 320;
	static final int SPAWN_Z = -192;

	private static RegistryOps<JsonElement> ops(GameTestHelper h) {
		return RegistryOps.create(JsonOps.INSTANCE, h.getLevel().registryAccess());
	}

	private static Registry<Biome> biomes(GameTestHelper h) {
		return h.getLevel().registryAccess().lookupOrThrow(Registries.BIOME);
	}

	private static DataResult<ClimateProfile> parse(GameTestHelper h, JsonElement json) {
		return ClimateProfile.CODEC.parse(ops(h), json);
	}

	private static ClimateProfile profile(GameTestHelper h, Geometry g) {
		return parse(h, json(g)).getOrThrow();
	}

	private static String zone(ClimateProfile p, int x, int z) {
		return zoneName(ClimateBands.zone(p, x, z));
	}

	/** {"type":"minecraft:multi_noise","preset":"minecraft:overworld"}, decoded with this server's registries. */
	private static JsonObject overworldJson() {
		JsonObject base = new JsonObject();
		base.addProperty("type", "minecraft:multi_noise");
		base.addProperty("preset", "minecraft:overworld");
		return base;
	}

	private static BiomeSource decode(GameTestHelper h, JsonElement json) {
		return BiomeSource.CODEC.parse(ops(h), json).getOrThrow();
	}

	private static JsonObject climateSource(JsonElement base, JsonObject climate) {
		JsonObject source = new JsonObject();
		source.addProperty("type", "jugcraft:climate");
		source.add("base", base);
		source.add("climate", climate);
		return source;
	}

	private static JsonObject fixed(String biome) {
		JsonObject source = new JsonObject();
		source.addProperty("type", "minecraft:fixed");
		source.addProperty("biome", biome);
		return source;
	}

	private static BiomeResolver resolver(GameTestHelper h, BiomeSource source) {
		return source.createUncachedResolver(h.getLevel().getChunkSource().randomState());
	}

	/** The audit's design: flat land at 72 over [-1728, 2368] x [-1216, 2880], step 64, spawn (320, -192). */
	static DesignGrid grid() {
		return new DesignGrid(SPAWN_X - 2048, SPAWN_Z - 1024, 64, 65, Collections.nCopies(65 * 65, 72f));
	}

	/** Painted cells: palette 1 (desert) in the central zone round the spawn, palette 2 (snowy plains) inside the hot belt. */
	static List<Integer> cells(DesignGrid grid) {
		List<Integer> cells = new ArrayList<>();
		for (int row = 0; row < grid.width(); row++) {
			for (int col = 0; col < grid.width(); col++) {
				int x = grid.originX() + col * grid.step(), z = grid.originZ() + row * grid.step();
				long s = (long) z - SPAWN_Z;
				cells.add(Math.abs(x - SPAWN_X) <= 512 && s >= -512 && s <= 512 ? 1
						: Math.abs(x - SPAWN_X) <= 768 && s >= 2200 && s <= 2700 ? 2 : 0);
			}
		}
		return cells;
	}

	/** The audit design over {@code base}, without climate, as JSON exactly as the pre-climate codec writes it. */
	static JsonObject designJson(GameTestHelper h, BiomeSource base) {
		var biomes = biomes(h);
		DesignGrid grid = grid();
		var design = new DesignBiomeSource(base, grid, List.of(biomes.getOrThrow(Biomes.DESERT), biomes.getOrThrow(Biomes.SNOWY_PLAINS)),
				cells(grid), new BlockPos(SPAWN_X, 72, SPAWN_Z), Optional.empty());
		return BiomeSource.CODEC.encodeStart(ops(h), design).getOrThrow().getAsJsonObject();
	}

	/** The old overlay, re-implemented from the pre-climate DesignBiomeSource: paint at Y0 and above, else the base. */
	static Holder<Biome> oldOverlay(DesignGrid grid, List<Integer> cells, List<Holder<Biome>> palette, BiomeResolver base, int qx, int qy, int qz) {
		int bx = qx * 4, bz = qz * 4;
		if (qy >= 0 && grid.weight(bx, bz) > 0) {
			int id = cells.get(grid.cell(bx, bz));
			if (id > 0) return palette.get(id - 1);
		}
		return base.getNoiseBiome(qx, qy, qz);
	}

	// ---------------------------------------------------------------------------------------------------------------
	// Codec

	/** A profile survives JSON and NBT (as level.dat stores it); omitted salt and pools stay omitted; values exact. */
	@GameTest
	public void profileRoundTripsInJsonAndNbt(GameTestHelper h) {
		var ops = ops(h);
		var nbt = h.getLevel().registryAccess().createSerializationContext(NbtOps.INSTANCE);
		for (Geometry g : List.of(Geometry.defaults(SPAWN_X, SPAWN_Z), Geometry.defaults(-30_000_000, 30_000_000).withTransition(257).withSalt(-7),
				new Geometry(5, -5, 1, 1, 2, 3, 0, Integer.MIN_VALUE), new Geometry(0, 0, 1_000_000, 2000, 4000, 1_000_000, 1024, Integer.MAX_VALUE))) {
			JsonObject input = json(g);
			ClimateProfile p = parse(h, input).getOrThrow();
			h.assertTrue(p.anchorX() == g.anchorX() && p.anchorZ() == g.anchorZ(), "Anchor changed on decoding " + input);
			JsonElement encoded = ClimateProfile.CODEC.encodeStart(ops, p).getOrThrow();
			h.assertTrue(encoded.equals(input), "A profile with default pools must re-encode as written: " + input + " vs " + encoded);
			Tag tag = ClimateProfile.CODEC.encodeStart(nbt, p).getOrThrow();
			ClimateProfile fromNbt = ClimateProfile.CODEC.parse(nbt, tag).getOrThrow();
			h.assertTrue(ClimateProfile.CODEC.encodeStart(ops, fromNbt).getOrThrow().equals(input), "NBT round trip changed " + input);
			h.assertTrue(ClimateProfile.CODEC.encodeStart(nbt, fromNbt).getOrThrow().equals(tag), "NBT re-encoding is not stable for " + input);
			for (int z = g.anchorZ() - 7000; z <= g.anchorZ() + 7000 && z > Integer.MIN_VALUE + 8000; z += 61) {
				h.assertTrue(zone(p, 13, z).equals(zone(fromNbt, 13, z)), "Zones changed through NBT at z " + z);
			}
		}
		// Explicit pools naming the default tags decode to the same bands and pools, and normalize to the short form.
		JsonObject explicit = json(Geometry.defaults(SPAWN_X, SPAWN_Z));
		JsonObject pools = new JsonObject();
		for (String zone : ZONES) {
			JsonObject members = new JsonObject();
			for (String c : CATEGORIES) members.addProperty(c, "#jugcraft:climate/" + zone + "/" + c);
			pools.add(zone, members);
		}
		explicit.add("pools", pools);
		ClimateProfile p = parse(h, explicit).getOrThrow();
		h.assertTrue(ClimateProfile.CODEC.encodeStart(ops, p).getOrThrow().equals(json(Geometry.defaults(SPAWN_X, SPAWN_Z))),
				"Explicit default pools must normalize to the omitted form");
		// A list pool survives as a list.
		JsonObject listed = json(Geometry.defaults(SPAWN_X, SPAWN_Z));
		JsonObject frigid = new JsonObject();
		JsonArray land = new JsonArray();
		land.add("minecraft:snowy_plains");
		land.add("minecraft:ice_spikes");
		frigid.add("land", land);
		JsonObject listPools = new JsonObject();
		listPools.add("frigid", frigid);
		listed.add("pools", listPools);
		ClimateProfile withList = parse(h, listed).getOrThrow();
		JsonElement again = ClimateProfile.CODEC.encodeStart(ops, withList).getOrThrow();
		h.assertTrue(parse(h, again).isSuccess() && again.getAsJsonObject().has("pools"), "A list pool must survive encoding: " + again);
		var members = new ClimateRemapper(withList).members(io.github.jimbozoomer.jugcraft.world.climate.ClimateZone.FRIGID,
				io.github.jimbozoomer.jugcraft.world.climate.ClimateCategory.LAND);
		h.assertTrue(members.size() == 2 && members.getFirst().is(Biomes.SNOWY_PLAINS), "A list pool must keep its members in order: " + members);
		h.succeed();
	}

	/** Amendment 4: the codec accepts and refuses exactly at each of the eleven rules' boundaries (odd T and 30M anchors). */
	@GameTest
	public void profileCodecEnforcesEveryRuleAtItsBoundary(GameTestHelper h) {
		Map<String, Geometry> cases = new LinkedHashMap<>();
		Geometry d = Geometry.defaults(0, 0);
		cases.put("defaults", d);
		// Rule 3: anchors.
		for (int a : new int[]{30_000_000, -30_000_000, 30_000_001, -30_000_001, Integer.MAX_VALUE, Integer.MIN_VALUE}) {
			cases.put("anchor x " + a, d.withAnchor(a, 0));
			cases.put("anchor z " + a, d.withAnchor(0, a));
		}
		// Rule 4: thresholds within [1, 1,000,000].
		cases.put("all thresholds at 1-3, T 0", new Geometry(0, 0, 1, 1, 2, 3, 0, 0));
		cases.put("north 0", new Geometry(0, 0, 0, 1, 2, 3, 0, 0));
		cases.put("hot start 0", new Geometry(0, 0, 1, 0, 2, 3, 0, 0));
		cases.put("south frigid 1,000,000", new Geometry(0, 0, 1_000_000, 2000, 4000, 1_000_000, 256, 0));
		cases.put("north 1,000,001", new Geometry(0, 0, 1_000_001, 2000, 4000, 5500, 256, 0));
		cases.put("south frigid 1,000,001", new Geometry(0, 0, 2500, 2000, 4000, 1_000_001, 256, 0));
		// Rule 5: T within [0, 1024].
		cases.put("T 0", d.withTransition(0));
		cases.put("T 1024", new Geometry(0, 0, 2500, 2000, 4000, 6000, 1024, 0));
		cases.put("T 1025", new Geometry(0, 0, 2500, 2000, 4000, 7000, 1025, 0));
		cases.put("T -1", d.withTransition(-1));
		cases.put("defaults with T 1024 (rule 11)", d.withTransition(1024));
		// Rule 6: any int salt.
		cases.put("salt min", d.withSalt(Integer.MIN_VALUE));
		cases.put("salt max", d.withSalt(Integer.MAX_VALUE));
		// Rule 7: north > T.
		cases.put("north == T", new Geometry(0, 0, 256, 2000, 4000, 5500, 256, 0));
		cases.put("north == T + 1", new Geometry(0, 0, 257, 2000, 4000, 5500, 256, 0));
		// Rule 8: strict ordering.
		cases.put("hot start == hot end", new Geometry(0, 0, 2500, 4000, 4000, 5500, 0, 0));
		cases.put("hot end == south frigid", new Geometry(0, 0, 2500, 2000, 5500, 5500, 0, 0));
		cases.put("hot start > hot end", new Geometry(0, 0, 2500, 4001, 4000, 5500, 0, 0));
		// Rule 9: 2*hot start > T, even and odd T.
		cases.put("T 256, hot start 128", new Geometry(0, 0, 2500, 128, 4000, 5500, 256, 0));
		cases.put("T 256, hot start 129", new Geometry(0, 0, 2500, 129, 4000, 5500, 256, 0));
		cases.put("T 257, hot start 128", new Geometry(0, 0, 2500, 128, 4000, 5500, 257, 0));
		cases.put("T 257, hot start 129", new Geometry(0, 0, 2500, 129, 4000, 5500, 257, 0));
		// Rule 10: hot end - hot start >= T.
		cases.put("hot belt == T", new Geometry(0, 0, 2500, 2000, 2256, 5500, 256, 0));
		cases.put("hot belt == T - 1", new Geometry(0, 0, 2500, 2000, 2255, 5500, 256, 0));
		cases.put("odd T, hot belt == T", new Geometry(0, 0, 2500, 2000, 2257, 5500, 257, 0));
		cases.put("odd T, hot belt == T - 1", new Geometry(0, 0, 2500, 2000, 2256, 5500, 257, 0));
		// Rule 11: 2*(south frigid - hot end) >= 3T.
		cases.put("temperate 384, T 256", new Geometry(0, 0, 2500, 2000, 4000, 4384, 256, 0));
		cases.put("temperate 383, T 256", new Geometry(0, 0, 2500, 2000, 4000, 4383, 256, 0));
		cases.put("temperate 386, T 257", new Geometry(0, 0, 2500, 2000, 4000, 4386, 257, 0));
		cases.put("temperate 385, T 257", new Geometry(0, 0, 2500, 2000, 4000, 4385, 257, 0));
		for (var entry : cases.entrySet()) {
			Geometry g = entry.getValue();
			DataResult<ClimateProfile> result = parse(h, json(g));
			h.assertTrue(result.isSuccess() == g.valid(), "Rule boundary '" + entry.getKey() + "': the oracle says " + (g.valid() ? "valid" : "invalid")
					+ ", the codec " + result.error().map(e -> "refused: " + e.message()).orElse("accepted"));
		}
		// Rules 1 and 2, and the required fields (only salt and pools may be omitted).
		JsonObject good = json(d);
		for (String field : List.of("schema", "anchor", "north_frigid", "south_hot_start", "south_hot_end", "south_frigid", "transition")) {
			JsonObject missing = good.deepCopy();
			missing.remove(field);
			h.assertTrue(parse(h, missing).isError(), "A profile without " + field + " must be refused");
		}
		for (int schema : new int[]{0, 2, -1}) {
			JsonObject bad = good.deepCopy();
			bad.addProperty("schema", schema);
			h.assertTrue(parse(h, bad).isError(), "Schema " + schema + " must be refused");
		}
		for (String mode : List.of("climate_spawn", "spawn", "FIXED", "")) {
			JsonObject bad = good.deepCopy();
			bad.getAsJsonObject("anchor").addProperty("mode", mode);
			h.assertTrue(parse(h, bad).isError(), "Anchor mode '" + mode + "' must be refused");
		}
		JsonObject noMode = good.deepCopy();
		noMode.getAsJsonObject("anchor").remove("mode");
		h.assertTrue(parse(h, noMode).isError(), "An anchor without a mode must be refused");
		h.succeed();
	}

	/** "Integers, no rounding" (Amendment 4): JSON numbers with a fraction or beyond 32 bits are refused, never truncated or wrapped. */
	@GameTest
	public void profileCodecRefusesFractionsAndWrappedIntegers(GameTestHelper h) {
		JsonObject good = json(Geometry.defaults(0, 0));
		h.assertTrue(parse(h, good).isSuccess(), "The defaults must decode");
		record Bad(String field, Number value) { }
		for (Bad bad : List.of(new Bad("north_frigid", 2500.5), new Bad("transition", 256.25), new Bad("south_frigid", 5500.000001),
				new Bad("salt", 0.5), new Bad("salt", 2_147_483_648L), new Bad("salt", -2_147_483_649L), new Bad("north_frigid", 4_294_969_796L))) {
			JsonObject json = good.deepCopy();
			json.add(bad.field(), new JsonPrimitive(bad.value()));
			h.assertTrue(parse(h, json).isError(), bad.field() + " " + bad.value() + " must be refused, not rounded or wrapped");
		}
		for (Number anchor : List.<Number>of(0.5, -1.5, 4_294_967_301L)) {
			JsonObject json = good.deepCopy();
			json.getAsJsonObject("anchor").add("x", new JsonPrimitive(anchor));
			h.assertTrue(parse(h, json).isError(), "anchor x " + anchor + " must be refused, not rounded or wrapped");
		}
		// Whole numbers written as decimals are integers in both JSON and the editor's JavaScript: accepted.
		JsonObject whole = good.deepCopy();
		whole.add("north_frigid", new JsonPrimitive(2500.0));
		h.assertTrue(parse(h, whole).isSuccess(), "2500.0 is the integer 2500 in JSON and must be accepted, as the editor does");
		h.succeed();
	}

	/** Designs written before climate decode with it off and re-encode identically, in JSON and in NBT level settings. */
	@GameTest
	public void oldDesignJsonAndLevelSettingsDecodeUnchanged(GameTestHelper h) {
		var ops = ops(h);
		var nbt = h.getLevel().registryAccess().createSerializationContext(NbtOps.INSTANCE);
		// Hand-written in the pre-climate format (WorldDesigner's exporter writes this shape): no climate field.
		String heights = String.join(",", Collections.nCopies(25, "80"));
		String cellsText = String.join(",", Collections.nCopies(12, "1")) + "," + String.join(",", Collections.nCopies(13, "0"));
		JsonObject old = JsonParser.parseString("{\"type\":\"jugcraft:design\",\"schema\":1,\"base\":{\"type\":\"minecraft:multi_noise\","
				+ "\"preset\":\"minecraft:overworld\"},\"grid\":{\"origin_x\":-64,\"origin_z\":-64,\"step\":32,\"width\":5,\"heights\":[" + heights
				+ "]},\"palette\":[\"minecraft:desert\"],\"cells\":[" + cellsText + "],\"spawn\":[0,80,0]}").getAsJsonObject();
		BiomeSource decoded = decode(h, old);
		h.assertTrue(decoded instanceof DesignBiomeSource design && design.climate().isEmpty(), "Old design JSON must decode with climate off");
		JsonElement reencoded = BiomeSource.CODEC.encodeStart(ops, decoded).getOrThrow();
		h.assertTrue(reencoded.equals(old), "Old design JSON must re-encode unchanged: " + old + " vs " + reencoded);
		// The pre-climate answers, everywhere: paint at Y0 and above, the seeded multi-noise base elsewhere.
		var biomes = biomes(h);
		DesignGrid grid = new DesignGrid(-64, -64, 32, 5, Collections.nCopies(25, 80f));
		List<Integer> cells = new ArrayList<>(Collections.nCopies(12, 1));
		cells.addAll(Collections.nCopies(13, 0));
		BiomeResolver base = resolver(h, decode(h, overworldJson()));
		BiomeResolver design = resolver(h, decoded);
		List<Holder<Biome>> palette = List.of(biomes.getOrThrow(Biomes.DESERT));
		for (int qx = -40; qx <= 40; qx += 3) {
			for (int qz = -40; qz <= 40; qz += 3) {
				for (int qy : new int[]{-16, -1, 0, 10, 20, 40}) {
					Holder<Biome> expected = oldOverlay(grid, cells, palette, base, qx, qy, qz);
					h.assertTrue(design.getNoiseBiome(qx, qy, qz).equals(expected), "Old design answer changed at quart " + qx + " " + qy + " " + qz);
				}
			}
		}
		Set<Holder<Biome>> possible = new HashSet<>(decode(h, overworldJson()).possibleBiomes());
		possible.add(biomes.getOrThrow(Biomes.DESERT));
		h.assertTrue(new HashSet<>(decoded.possibleBiomes()).equals(possible), "Old design possibleBiomes must be the base's and the palette's only");
		// Level settings: a noise generator holding the old design, through NBT as level.dat keeps it.
		var settings = h.getLevel().registryAccess().lookupOrThrow(Registries.NOISE_SETTINGS).getOrThrow(NoiseGeneratorSettings.OVERWORLD);
		ChunkGenerator generator = new NoiseBasedChunkGenerator(decoded, settings);
		Tag saved = ChunkGenerator.CODEC.encodeStart(nbt, generator).getOrThrow();
		h.assertTrue(!saved.toString().contains("climate"), "Old level settings must not gain a climate field: " + saved);
		ChunkGenerator reopened = ChunkGenerator.CODEC.parse(nbt, saved).getOrThrow();
		h.assertTrue(reopened.getBiomeSource() instanceof DesignBiomeSource again && again.climate().isEmpty(), "Old level settings must reopen without climate");
		h.assertTrue(ChunkGenerator.CODEC.encodeStart(nbt, reopened).getOrThrow().equals(saved), "Old level settings must re-save byte for byte");
		// And a pre-climate jugcraft:design wrapped by nothing else, saved in JSON level settings, is still not banded.
		h.assertTrue(!ChunkGenerator.CODEC.encodeStart(ops, reopened).getOrThrow().toString().contains("climate"), "No climate appears in JSON settings");
		h.succeed();
	}

	/** Amendment 3: a design's climate anchor is the design spawn x/z, exactly; any other anchor is refused. */
	@GameTest
	public void designClimateAnchorMustBeTheDesignSpawn(GameTestHelper h) {
		var base = new FixedBiomeSource(biomes(h).getOrThrow(Biomes.PLAINS));
		JsonObject design = designJson(h, base);
		JsonObject at = design.deepCopy();
		at.add("climate", json(Geometry.defaults(SPAWN_X, SPAWN_Z)));
		h.assertTrue(BiomeSource.CODEC.parse(ops(h), at).isSuccess(), "The anchor at the design spawn must be accepted");
		// The spawn's Y is not part of the anchor.
		JsonObject high = at.deepCopy();
		JsonArray spawn = new JsonArray();
		spawn.add(SPAWN_X);
		spawn.add(200);
		spawn.add(SPAWN_Z);
		high.add("spawn", spawn);
		h.assertTrue(BiomeSource.CODEC.parse(ops(h), high).isSuccess(), "The spawn's height must not matter to the anchor");
		for (int[] off : new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}, {-SPAWN_X, -SPAWN_Z}, {64, 64}}) {
			JsonObject moved = design.deepCopy();
			moved.add("climate", json(Geometry.defaults(SPAWN_X + off[0], SPAWN_Z + off[1])));
			h.assertTrue(BiomeSource.CODEC.parse(ops(h), moved).isError(), "An anchor " + off[0] + " " + off[1] + " away from the spawn must be refused");
		}
		// A town is not the anchor unless it is the spawn (Amendment 3, C3).
		JsonObject town = design.deepCopy();
		JsonArray townPos = new JsonArray();
		townPos.add(SPAWN_X + 512);
		townPos.add(72);
		townPos.add(SPAWN_Z + 512);
		town.add("town", townPos);
		JsonObject atTown = town.deepCopy();
		atTown.add("climate", json(Geometry.defaults(SPAWN_X + 512, SPAWN_Z + 512)));
		h.assertTrue(BiomeSource.CODEC.parse(ops(h), atTown).isError(), "An anchor at the town centre, not the spawn, must be refused");
		JsonObject atSpawnWithTown = town.deepCopy();
		atSpawnWithTown.add("climate", json(Geometry.defaults(SPAWN_X, SPAWN_Z)));
		h.assertTrue(BiomeSource.CODEC.parse(ops(h), atSpawnWithTown).isSuccess(), "A design with a town keeps its anchor at the spawn");
		h.succeed();
	}

	/** Bands are applied once: refused at every depth of nesting, while single application through any path is accepted. */
	@GameTest
	public void doubleApplicationIsRefusedAtAnyDepth(GameTestHelper h) {
		JsonObject climate = json(Geometry.defaults(SPAWN_X, SPAWN_Z));
		JsonObject plains = fixed("minecraft:plains");
		JsonObject banded = climateSource(plains, climate.deepCopy());
		JsonObject plainDesign = designJson(h, decode(h, plains));
		JsonObject designOverBanded = plainDesign.deepCopy();
		designOverBanded.add("base", banded.deepCopy());
		JsonObject climateDesign = plainDesign.deepCopy();
		climateDesign.add("climate", climate.deepCopy());
		Map<String, JsonObject> refused = new LinkedHashMap<>();
		refused.put("climate over climate", climateSource(banded.deepCopy(), climate.deepCopy()));
		refused.put("climate over a climate design", climateSource(climateDesign.deepCopy(), climate.deepCopy()));
		refused.put("climate over a plain design over climate", climateSource(designOverBanded.deepCopy(), climate.deepCopy()));
		JsonObject climateDesignOverBanded = designOverBanded.deepCopy();
		climateDesignOverBanded.add("climate", climate.deepCopy());
		refused.put("climate design over climate", climateDesignOverBanded);
		JsonObject climateDesignOverDesignOverBanded = plainDesign.deepCopy();
		climateDesignOverDesignOverBanded.add("base", designOverBanded.deepCopy());
		climateDesignOverDesignOverBanded.add("climate", climate.deepCopy());
		refused.put("climate design over a plain design over climate", climateDesignOverDesignOverBanded);
		JsonObject climateDesignOverClimateDesign = climateDesign.deepCopy();
		climateDesignOverClimateDesign.add("base", climateDesign.deepCopy());
		refused.put("climate design over a climate design", climateDesignOverClimateDesign);
		for (var entry : refused.entrySet()) {
			h.assertTrue(BiomeSource.CODEC.parse(ops(h), entry.getValue()).isError(), "Double application must be refused: " + entry.getKey());
		}
		// Single application is not over-refused.
		h.assertTrue(BiomeSource.CODEC.parse(ops(h), banded).isSuccess(), "climate over a plain base must decode");
		h.assertTrue(BiomeSource.CODEC.parse(ops(h), climateDesign).isSuccess(), "a climate design over a plain base must decode");
		h.assertTrue(BiomeSource.CODEC.parse(ops(h), designOverBanded).isSuccess(), "a plain design over a climate base must decode (bands once)");
		JsonObject climateDesignOverPlainDesign = plainDesign.deepCopy();
		climateDesignOverPlainDesign.add("base", plainDesign.deepCopy());
		climateDesignOverPlainDesign.add("climate", climate.deepCopy());
		h.assertTrue(BiomeSource.CODEC.parse(ops(h), climateDesignOverPlainDesign).isSuccess(), "a climate design over a plain design must decode");
		// The plain design over a climate base: paint wins, the unpainted fallback is banded once.
		BiomeResolver once = resolver(h, decode(h, designOverBanded));
		ClimateProfile p = profile(h, Geometry.defaults(SPAWN_X, SPAWN_Z));
		Holder<Biome> plainsHolder = biomes(h).getOrThrow(Biomes.PLAINS);
		for (int x : new int[]{-4000, 3000, 9000}) {
			int z = SPAWN_Z - 3000;
			Holder<Biome> got = once.getNoiseBiome(x >> 2, 16, z >> 2);
			h.assertTrue(got.is(pool(FRIGID, "land")) && !got.equals(plainsHolder), "Unpainted frigid land must be banded once at " + x + ": " + id(got));
		}
		h.assertTrue(once.getNoiseBiome(SPAWN_X >> 2, 18, SPAWN_Z >> 2).is(Biomes.DESERT), "Paint must win over a climate base");
		h.assertTrue(zone(p, 0, SPAWN_Z - 3000).equals(FRIGID), "Sanity: 3000 north of the anchor is frigid");
		h.succeed();
	}

	// ---------------------------------------------------------------------------------------------------------------
	// Categories, pools and weather

	/** The Overworld biomes the bands can be asked about: everything the seeded Overworld can answer, plus Jugcraft's lists. */
	private static Set<Holder<Biome>> overworld(GameTestHelper h) {
		var biomes = biomes(h);
		Set<Holder<Biome>> out = new LinkedHashSet<>(decode(h, overworldJson()).possibleBiomes());
		// AlpineSpawn replaces vanilla meadow in the seeded table; that does not make meadow a dimensional biome.
		biomes.get(net.minecraft.tags.BiomeTags.IS_OVERWORLD).ifPresent(set -> set.forEach(out::add));
		List<ResourceKey<Biome>> keys = new ArrayList<>(JugcraftRegions.biomes());
		keys.addAll(LandscapeBiomes.overworld());
		keys.add(AlpineSpawn.BIOME);
		keys.add(AdamOndiAhman.BIOME);
		for (ResourceKey<Biome> key : keys) biomes.get(key).ifPresent(out::add);
		return out;
	}

	private static String family(Holder<Biome> biome) {
		List<String> categories = categoriesOf(biome);
		return categories.size() == 1 ? FAMILY.get(categories.getFirst()) : String.valueOf(categories);
	}

	/**
	 * Amendment 1, A4, and Amendment 2, B2: every Overworld biome is in exactly one category tag, and that category
	 * agrees with the repository's real metadata (vanilla role tags, cave lists, Landscape sources, region rules).
	 */
	@GameTest
	public void everyOverworldBiomeHasOneCategoryFromRealMetadata(GameTestHelper h) {
		var biomes = biomes(h);
		Set<Holder<Biome>> overworld = overworld(h);
		h.assertTrue(overworld.size() > 150, "Too few Overworld biomes found (" + overworld.size() + "): the audit's list is wrong");
		List<String> problems = new ArrayList<>();
		for (Holder<Biome> b : overworld) {
			List<String> categories = categoriesOf(b);
			if (categories.size() != 1) problems.add(id(b) + " has categories " + categories);
		}
		h.assertTrue(problems.isEmpty(), "Every Overworld biome needs exactly one climate category (no default to land): " + problems);
		Map<String, String> expected = new LinkedHashMap<>();
		// Vanilla role tags.
		for (Holder<Biome> b : overworld) {
			if (b.is(BiomeTags.IS_DEEP_OCEAN)) expected.put(id(b), "sea");
			else if (b.is(BiomeTags.IS_OCEAN)) expected.put(id(b), "sea");
			else if (b.is(BiomeTags.IS_RIVER)) expected.put(id(b), "river");
			else if (b.is(BiomeTags.IS_BEACH)) expected.put(id(b), "beach");
			if (b.is(biomeTag("c:is_cave")) || b.is(biomeTag("c:is_underground"))) expected.put(id(b), PRESERVE);
		}
		for (ResourceKey<Biome> cave : List.of(Biomes.DRIPSTONE_CAVES, Biomes.LUSH_CAVES, Biomes.DEEP_DARK)) expected.put(cave.identifier().toString(), PRESERVE);
		for (String cave : CaveWorkshop.CAVES) expected.put(Jugcraft.id("cavern_" + cave).toString(), PRESERVE);
		// The Landscape collection's own sources (LandscapeBiomes.Entry's cave/water/shore rules, restated).
		for (LandscapeBiomes.Entry e : LandscapeBiomes.ENTRIES) {
			String s = e.source();
			if (s.startsWith("end_") || Set.of("erupting_inferno", "visceral_heap", "withered_abyss", "undergrowth", "crystalline_chasm").contains(s)) continue;
			String key = e.key().identifier().toString();
			if (s.startsWith("cavern_") || s.startsWith("cave/") || s.equals("glowing_grotto") || s.equals("spider_nest")) expected.put(key, PRESERVE);
			else if (s.contains("river")) expected.put(key, "river");
			else if (s.contains("ocean")) expected.put(key, "sea");
			else if (s.contains("beach")) expected.put(key, "beach");
		}
		// Region rules: a Jugcraft biome takes the role of the vanilla biome it replaces; surface specials replacing
		// mushroom fields are land (Amendment 1, A3).
		Map<String, Set<String>> replaced = new HashMap<>();
		for (JugcraftRegions.Rule rule : JugcraftRegions.rules()) {
			Holder<Biome> vanilla = biomes.get(rule.replaces()).orElse(null);
			if (vanilla == null) continue;
			String role = rule.replaces().equals(Biomes.MUSHROOM_FIELDS) ? "land" : family(vanilla);
			replaced.computeIfAbsent(rule.biome().identifier().toString(), k -> new HashSet<>()).add(role);
		}
		replaced.forEach((biome, roles) -> {
			if (roles.size() == 1 && !expected.containsKey(biome)) expected.put(biome, roles.iterator().next());
		});
		List<String> mismatches = new ArrayList<>();
		expected.forEach((biome, role) -> biomes.get(ResourceKey.create(Registries.BIOME, Identifier.parse(biome))).ifPresent(holder -> {
			if (overworld.contains(holder) && !role.equals(family(holder))) mismatches.add(biome + " is " + categoriesOf(holder) + ", metadata says " + role);
		}));
		h.assertTrue(mismatches.isEmpty(), "Climate categories disagree with the real biome metadata: " + mismatches);
		// Surface specials are never preserved (A3), so strict frigid has no surface exceptions.
		for (String special : List.of("minecraft:mushroom_fields", "jugcraft:flower_isle", "jugcraft:elder_vale", "jugcraft:toadstool_field",
				"jugcraft:landscape_alpha_islands")) {
			biomes.get(ResourceKey.create(Registries.BIOME, Identifier.parse(special))).ifPresent(holder -> h.assertTrue(
					!preserved(holder) && "land".equals(family(holder)), special + " must be land, remapped in its role, not " + categoriesOf(holder)));
		}
		Jugcraft.LOGGER.info("[climate-audit] {} Overworld biomes in one category each; {} metadata facts agree", overworld.size(), expected.size());
		h.succeed();
	}

	/**
	 * Amendment 1, A2, and Amendment 2, B3: every zone x category pool is non-empty, holds Overworld biomes of its own
	 * role (never a cave), and its weather matches the zone: frigid never rains at sea level anywhere sampled, apart from
	 * the documented {@link #RAINS_IN_FRIGID}, so a cold, dry member without precipitation (cold desert) passes:
	 * the check is "no rain", not "snowfall"; hot never gets winter snow or seasons and is always warm enough to rain;
	 * temperate is warm enough to rain at sea level and neither frozen nor desert-hot. Seasonal flags come from the same
	 * tags the season code reads. Every frigid member's actual values are logged.
	 */
	@GameTest
	public void poolsAreFullInRoleAndHonestAboutTheWeather(GameTestHelper h) {
		var biomes = biomes(h);
		Set<Holder<Biome>> overworld = overworld(h);
		List<String> problems = new ArrayList<>();
		List<String> notes = new ArrayList<>();
		List<String> frigid = new ArrayList<>();
		List<BlockPos> places = new ArrayList<>();
		for (int i = 0; i < 8; i++) for (int j = 0; j < 8; j++) places.add(new BlockPos(-7000 + i * 2003, SEA_LEVEL + 1, -7000 + j * 1999));
		for (String zone : ZONES) {
			for (String c : CATEGORIES) {
				var set = biomes.get(pool(zone, c));
				if (set.isEmpty() || set.get().size() == 0) {
					problems.add(zone + "/" + c + " is empty or missing");
					continue;
				}
				for (Holder<Biome> b : set.get()) {
					String where = zone + "/" + c + " " + id(b);
					if (!overworld.contains(b)) problems.add(where + " is not an Overworld biome");
					if (preserved(b)) problems.add(where + " is a preserved (cave) biome");
					if (!FAMILY.get(c).equals(family(b))) problems.add(where + " has role " + categoriesOf(b) + ", not " + c);
					Biome biome = b.value();
					// Places warm enough to rain at sea level (vanilla's own test: temperature, including the frozen modifier's
					// patches, at least 0.15); precipitation is reported separately, so a cold, dry biome never "rains".
					int rains = 0;
					for (BlockPos at : places) if (biome.warmEnoughToRain(at, SEA_LEVEL)) rains++;
					int cold = places.size() - rains;
					boolean seasons = b.is(JugcraftSeasons.HAS_SEASONS), winter = b.is(JugcraftSeasons.HAS_WINTER_SNOW);
					float t = biome.getBaseTemperature();
					switch (zone) {
						case FRIGID -> {
							frigid.add(String.format(java.util.Locale.ROOT, "%s %s: temperature %.2f, warm enough to rain at sea level at %d/%d places, precipitation %b",
									c, id(b), t, rains, places.size(), biome.hasPrecipitation()));
							if (RAINS_IN_FRIGID.contains(id(b))) {
								// Documented exceptions (coordinator): kept as vanilla's arctic biomes, an owner decision later.
								notes.add(where + " rains at sea level at " + rains + "/" + places.size() + " places: documented exception");
							} else if (rains > 0) {
								problems.add(where + " is frigid but rains at sea level at " + rains + "/" + places.size() + " places (temperature " + t + ")");
							}
							if (!biome.hasPrecipitation()) notes.add(where + " is cold and dry: no precipitation, so no rain and no snowfall (accepted)");
							if (seasons) problems.add(where + " is frigid but has seasons (#jugcraft:has_seasons)");
						}
						case HOT -> {
							if (winter) problems.add(where + " is hot but gets winter snow (#jugcraft:has_winter_snow)");
							if (cold > 0) problems.add(where + " is hot but too cold to rain at sea level at " + cold + " places (temperature " + t + ")");
							if ((c.equals("land") || c.equals("mountain")) && seasons) problems.add(where + " is hot land but has seasons");
							if ((c.equals("land") || c.equals("mountain")) && t < 0.8F) problems.add(where + " is hot land at temperature " + t);
						}
						default -> {
							if (cold > 0 && !c.equals("mountain")) problems.add(where + " is temperate but too cold to rain at sea level at " + cold + " places (temperature " + t + ")");
							if (t > 1.0F) problems.add(where + " is temperate but desert-hot (temperature " + t + ")");
							if (c.equals("land") && !seasons) notes.add(where + " is temperate land without seasons");
						}
					}
				}
			}
		}
		// The actual values of every frigid pool member, for the review (coordinator: report them).
		frigid.forEach(line -> Jugcraft.LOGGER.info("[climate-audit] frigid pool weather: {}", line));
		Jugcraft.LOGGER.info("[climate-audit] pool weather notes: {}", notes);
		h.assertTrue(problems.isEmpty(), "Climate pools: " + problems);
		h.succeed();
	}

	/** possibleBiomes: the remapper lists exactly the pools' members; both sources add every member to their base's own. */
	@GameTest
	public void possibleBiomesHoldEveryPoolMemberAndTheBase(GameTestHelper h) {
		var biomes = biomes(h);
		Set<Holder<Biome>> members = new HashSet<>();
		for (String zone : ZONES) for (String c : CATEGORIES) biomes.get(pool(zone, c)).ifPresent(set -> set.forEach(members::add));
		ClimateProfile p = profile(h, Geometry.defaults(SPAWN_X, SPAWN_Z));
		h.assertTrue(new HashSet<>(new ClimateRemapper(p).possibleBiomes()).equals(members), "The remapper must list exactly the pool members");
		BiomeSource overworld = decode(h, overworldJson());
		BiomeSource banded = decode(h, climateSource(overworldJson(), json(Geometry.defaults(SPAWN_X, SPAWN_Z))));
		JsonObject designWithClimate = designJson(h, overworld);
		designWithClimate.add("climate", json(Geometry.defaults(SPAWN_X, SPAWN_Z)));
		BiomeSource design = decode(h, designWithClimate);
		for (BiomeSource source : List.of(banded, design)) {
			h.assertTrue(source.possibleBiomes().containsAll(members), source.getClass().getSimpleName() + " is missing pool members in possibleBiomes");
			h.assertTrue(source.possibleBiomes().containsAll(overworld.possibleBiomes()),
					source.getClass().getSimpleName() + " must keep every base biome (Jugcraft's regions list theirs there)");
		}
		h.assertTrue(design.possibleBiomes().contains(biomes.getOrThrow(Biomes.SNOWY_PLAINS)) && design.possibleBiomes().contains(biomes.getOrThrow(Biomes.DESERT)),
				"A climate design must keep its palette in possibleBiomes");
		h.succeed();
	}

	// ---------------------------------------------------------------------------------------------------------------
	// Band geometry and remapping

	/**
	 * The core's zones against the oracle: exact outside the blends and one of the two adjacent zones inside them; strict
	 * frigid at and beyond 2500 N and 5500 S of the anchor for any x; transitions on the inner side only; defaults, odd
	 * T, T 0, T 1024 and 30M anchors. Inside the default blends the outer zone's share rises from rare to common, patches
	 * are coherent (not per-quart salt and pepper) and the salt changes only the blends.
	 */
	@GameTest
	public void bandsMatchTheContractGeometry(GameTestHelper h) {
		List<Geometry> geometries = List.of(Geometry.defaults(SPAWN_X, SPAWN_Z), Geometry.defaults(SPAWN_X, SPAWN_Z).withTransition(257),
				Geometry.defaults(SPAWN_X, SPAWN_Z).withTransition(0), new Geometry(0, 0, 2500, 2000, 4000, 6000, 1024, 3),
				Geometry.defaults(-30_000_000, 30_000_000), Geometry.defaults(30_000_000, -30_000_000), new Geometry(7, 9, 300, 200, 500, 900, 100, 0));
		int[] xs = {-29_999_999, -1_000_003, -1, 0, SPAWN_X, 65_537, 29_999_999};
		for (Geometry g : geometries) {
			ClimateProfile p = profile(h, g);
			for (long s = -(long) g.north() - 600; s <= (long) g.southFrigid() + 600; s++) {
				long z = g.anchorZ() + s;
				if (z < Integer.MIN_VALUE || z > Integer.MAX_VALUE) continue;
				Set<String> allowed = g.allowed((int) z);
				for (int x : xs) {
					String got = zone(p, x, (int) z);
					if (!allowed.contains(got)) {
						h.fail("Zone " + got + " at s " + s + " (x " + x + ") is not one the contract allows there, " + allowed + ", for " + g);
					}
				}
			}
			// Strict at and beyond both limits, far out.
			for (long s : new long[]{-(long) g.north(), -(long) g.north() - 1, -(long) g.north() - 100_000, g.southFrigid(), g.southFrigid() + 1L, g.southFrigid() + 100_000L}) {
				long z = g.anchorZ() + s;
				if (z < Integer.MIN_VALUE || z > Integer.MAX_VALUE) continue;
				for (int x : xs) h.assertTrue(zone(p, x, (int) z).equals(FRIGID), "Not strictly frigid at s " + s + " x " + x + " for " + g);
			}
			// The anchor row is central for every x.
			for (int x : xs) h.assertTrue(zone(p, x, g.anchorZ()).equals(CENTRAL), "The anchor row must be central for " + g);
		}
		// The ramp and coherence of the default blends.
		Geometry d = Geometry.defaults(SPAWN_X, SPAWN_Z);
		ClimateProfile p = profile(h, d);
		ClimateProfile salted = profile(h, d.withSalt(918_273));
		int saltChanges = 0;
		for (Blend blend : d.blends()) {
			double[] share = new double[4];
			double[] stated = new double[4];
			int[] count = new int[4];
			for (long s = blend.from(); s < blend.to(); s++) {
				int z = d.z(s);
				double progress = blend.progress(s);
				int quarter = Math.min(3, (int) (progress * 4));
				for (int i = 0; i < 600; i++) {
					stated[quarter] += progress * progress * (3 - 2 * progress);
					int x = -1_000_000 + i * 3331;
					String got = zone(p, x, z);
					if (got.equals(blend.outer())) share[quarter]++;
					count[quarter]++;
					if (!got.equals(zone(salted, x, z))) saltChanges++;
				}
			}
			for (int q = 0; q < 4; q++) {
				share[q] /= count[q];
				stated[q] /= count[q];
			}
			h.assertTrue(share[0] <= 0.3 && share[3] >= 0.7 && share[0] < share[1] && share[1] < share[2] && share[2] < share[3],
					blend.name() + ": the outer zone's share must ramp from rare to common across the blend, not " + java.util.Arrays.toString(share));
			// Uniformity (contract section 1: the probability ramps by smoothstep across the whole band): the realized share
			// per quarter is the stated smoothstep's, so the visible transition is T wide, not a narrower middle.
			for (int q = 0; q < 4; q++) {
				h.assertTrue(Math.abs(share[q] - stated[q]) <= 0.1, blend.name() + ": quarter " + q + " has outer share " + share[q]
						+ " where the smoothstep ramp gives " + stated[q] + ": the blend is not uniform across its width");
			}
			int z = d.z((blend.from() + blend.to()) / 2);
			int changes = 0;
			String previous = zone(p, -65_536, z);
			for (int x = -65_532; x < 65_536; x += 4) {
				String got = zone(p, x, z);
				if (!got.equals(previous)) changes++;
				previous = got;
			}
			double rate = changes / (131_072 / 4.0);
			h.assertTrue(rate < 0.2, blend.name() + ": neighbouring quarts disagree " + rate + " of the time: per-quart salt and pepper, not coherent patches");
			Jugcraft.LOGGER.info("[climate-audit] {} blend: outer share by quarter {}, quart neighbours differ {}", blend.name(), java.util.Arrays.toString(share), rate);
		}
		h.assertTrue(saltChanges > 0, "The salt must change the blend fringe somewhere");
		for (long s = -6000; s <= 7000; s += 7) {
			int z = d.z(s);
			if (d.certain(z) == null) continue;
			for (int x : xs) h.assertTrue(zone(p, x, z).equals(zone(salted, x, z)), "The salt must not move a pure belt (s " + s + ")");
		}
		h.succeed();
	}

	/**
	 * Remapping over every Overworld base biome: central unchanged, preserved (cave) biomes unchanged at every Y in every
	 * zone (B2: by ID, never by a Y test), everything else into its own role's pool, surface specials included; and no
	 * default to land for a biome without a category.
	 */
	@GameTest
	public void remapKeepsRolesAndCavesAtAnyY(GameTestHelper h) {
		var biomes = biomes(h);
		JsonObject climate = json(Geometry.defaults(SPAWN_X, SPAWN_Z));
		Geometry g = Geometry.defaults(SPAWN_X, SPAWN_Z);
		Map<String, Integer> rows = new LinkedHashMap<>();
		rows.put(CENTRAL, SPAWN_Z + 1000);
		rows.put(FRIGID, SPAWN_Z - 2500);
		rows.put(FRIGID + " far north", SPAWN_Z - 40_000);
		rows.put(HOT, SPAWN_Z + 3000);
		rows.put(TEMPERATE, SPAWN_Z + 4800);
		rows.put(FRIGID + " south", SPAWN_Z + 5500);
		int checked = 0;
		for (Holder<Biome> base : overworld(h)) {
			BiomeResolver resolver = resolver(h, decode(h, climateSource(fixed(id(base)), climate.deepCopy())));
			for (var row : rows.entrySet()) {
				String zone = g.certain(row.getValue());
				h.assertTrue(zone != null && row.getKey().startsWith(zone), "Audit row " + row.getKey() + " is not pure");
				for (int x : new int[]{-123_456, SPAWN_X, 98_765}) {
					for (int qy : new int[]{-16, -1, 0, 10, 16, 30, 60}) {
						Holder<Biome> got = resolver.getNoiseBiome(x >> 2, qy, row.getValue() >> 2);
						String why = violation(zone, base, got);
						h.assertTrue(why == null, why + " at x " + x + " y " + qy * 4 + " in " + row.getKey());
						checked++;
					}
				}
			}
		}
		// Every preserved biome, including those only a tag names (c:is_cave), stays at every Y.
		var preserve = biomes.get(category(PRESERVE)).orElseThrow();
		h.assertTrue(preserve.size() >= 25, "The preserve set lists too few cave biomes: " + preserve.size());
		for (Holder<Biome> cave : preserve) {
			BiomeResolver resolver = resolver(h, decode(h, climateSource(fixed(id(cave)), climate.deepCopy())));
			for (int z : rows.values()) for (int qy = -16; qy <= 79; qy += 5) {
				h.assertTrue(resolver.getNoiseBiome(17, qy, z >> 2).equals(cave), id(cave) + " must stay at y " + qy * 4 + " z " + z);
			}
		}
		// No default to land: a biome in no category is refused or kept, never turned into frigid land.
		Holder<Biome> theVoid = biomes.getOrThrow(Biomes.THE_VOID);
		if (categoriesOf(theVoid).isEmpty()) {
			Holder<Biome> got = null;
			try {
				got = resolver(h, decode(h, climateSource(fixed("minecraft:the_void"), climate.deepCopy()))).getNoiseBiome(0, 16, (SPAWN_Z - 3000) >> 2);
			} catch (RuntimeException refused) {
				// Refused loudly: allowed.
			}
			h.assertTrue(got == null || got.equals(theVoid), "An uncategorized biome must never default to a pool: " + (got == null ? "" : id(got)));
		}
		Jugcraft.LOGGER.info("[climate-audit] {} remap answers kept their roles; {} preserved biomes stayed at every Y", checked, preserve.size());
		h.succeed();
	}

	/** Amendment 2, A2: an empty, unknown or unbound pool fails loudly, never falling back to the base or another category. */
	@GameTest
	public void emptyOrUnknownPoolsFailLoudly(GameTestHelper h) {
		for (String zone : ZONES) {
			for (String c : CATEGORIES) {
				for (JsonElement bad : List.of(new JsonArray(), new JsonPrimitive("#jugcraft:climate/audit_missing_" + zone + "_" + c))) {
					JsonObject json = json(Geometry.defaults(SPAWN_X, SPAWN_Z));
					JsonObject zonePools = new JsonObject();
					zonePools.add(c, bad);
					JsonObject pools = new JsonObject();
					pools.add(zone, zonePools);
					json.add("pools", pools);
					DataResult<ClimateProfile> parsed = parse(h, json);
					if (parsed.isError()) continue;
					boolean refused = false;
					try {
						new ClimateRemapper(parsed.getOrThrow());
					} catch (IllegalStateException expected) {
						refused = true;
					}
					h.assertTrue(refused, zone + "/" + c + " = " + bad + " was accepted by both the codec and the remapper");
				}
			}
		}
		// Unknown zone or category keys are not silently ignored.
		for (String[] key : new String[][]{{"central", "land"}, {"frigid", "cave"}, {"arctic", "land"}}) {
			JsonObject json = json(Geometry.defaults(SPAWN_X, SPAWN_Z));
			JsonObject zonePools = new JsonObject();
			zonePools.addProperty(key[1], "minecraft:plains");
			JsonObject pools = new JsonObject();
			pools.add(key[0], zonePools);
			json.add("pools", pools);
			h.assertTrue(parse(h, json).isError(), "Pool key " + key[0] + "/" + key[1] + " must be refused");
		}
		h.succeed();
	}

	// ---------------------------------------------------------------------------------------------------------------
	// Isolation and determinism

	/** Two profiles in one server never leak into each other, and the climate classes hold no shared mutable statics. */
	@GameTest
	public void twoProfilesInOneServerStayApartWithNoStatics(GameTestHelper h) {
		Geometry one = Geometry.defaults(SPAWN_X, SPAWN_Z);
		Geometry two = new Geometry(-4000, 6000, 1200, 700, 1500, 2900, 97, 99);
		List<int[]> places = new ArrayList<>();
		for (int z = -12_000; z <= 16_000; z += 37) places.add(new int[]{(z * 7919) % 20_000, z});
		Supplier<BiomeResolver> first = () -> resolver(h, decode(h, climateSource(overworldJson(), json(one))));
		Supplier<BiomeResolver> second = () -> resolver(h, decode(h, climateSource(overworldJson(), json(two))));
		Map<Integer, Holder<Biome>> aloneOne = new HashMap<>(), aloneTwo = new HashMap<>();
		BiomeResolver r1 = first.get();
		for (int i = 0; i < places.size(); i++) aloneOne.put(i, r1.getNoiseBiome(places.get(i)[0] >> 2, 16, places.get(i)[1] >> 2));
		BiomeResolver r2 = second.get();
		for (int i = 0; i < places.size(); i++) aloneTwo.put(i, r2.getNoiseBiome(places.get(i)[0] >> 2, 16, places.get(i)[1] >> 2));
		BiomeResolver a = first.get(), b = second.get();
		int differ = 0;
		ClimateProfile p1 = profile(h, one), p2 = profile(h, two);
		for (int i = places.size() - 1; i >= 0; i--) {
			int[] at = places.get(i);
			Holder<Biome> x2 = b.getNoiseBiome(at[0] >> 2, 16, at[1] >> 2), x1 = a.getNoiseBiome(at[0] >> 2, 16, at[1] >> 2);
			h.assertTrue(x1.equals(aloneOne.get(i)) && x2.equals(aloneTwo.get(i)), "Interleaving two profiles changed an answer at " + at[0] + " " + at[1]);
			if (!zone(p1, at[0], at[1]).equals(zone(p2, at[0], at[1]))) differ++;
		}
		h.assertTrue(differ > places.size() / 4, "The two audit profiles should disagree on many places (" + differ + ")");
		List<String> statics = new ArrayList<>();
		for (String name : List.of("ClimateProfile", "ClimateBands", "ClimateRemapper", "ClimateBiomeSource", "ClimateLayer", "ClimateZone",
				"ClimateCategory", "ClimateCategories", "ClimatePools", "ClimateGeometry", "ClimateAnchor", "ClimateNoise", "ClimateRules",
				"ClimateSelection", "StableBiomeLookup")) {
			try {
				statics.addAll(mutableStatics(Class.forName("io.github.jimbozoomer.jugcraft.world.climate." + name)));
			} catch (ClassNotFoundException absent) {
				Jugcraft.LOGGER.info("[climate-audit] no class {} to scan", name);
			}
		}
		statics.addAll(mutableStatics(DesignBiomeSource.class));
		h.assertTrue(statics.isEmpty(), "Climate classes must hold no shared mutable static state: " + statics);
		// Pre-existing, documented (Amendment 2, B1): the region engine's statics, logged for the review only.
		Jugcraft.LOGGER.info("[climate-audit] pre-existing statics (not climate's): JugcraftRegions {}, AdamOndiAhman {}",
				mutableStatics(JugcraftRegions.class), mutableStatics(AdamOndiAhman.class));
		h.succeed();
	}

	/** Answers depend only on the position: fresh instances, forward, reverse and scattered orders agree. */
	@GameTest
	public void answersDoNotDependOnOrderOrInstance(GameTestHelper h) {
		JsonObject banded = climateSource(overworldJson(), json(Geometry.defaults(SPAWN_X, SPAWN_Z)));
		JsonObject design = designJson(h, decode(h, overworldJson()));
		design.add("climate", json(Geometry.defaults(SPAWN_X, SPAWN_Z)));
		for (JsonObject source : List.of(banded, design)) {
			List<int[]> quarts = new ArrayList<>();
			for (int z = -12_000; z <= 9_000; z += 23) for (int x : new int[]{-50_000, 333, 77_777}) quarts.add(new int[]{x >> 2, (z % 7 + 3) * 8, z >> 2});
			BiomeResolver forward = resolver(h, decode(h, source));
			List<Holder<Biome>> expected = new ArrayList<>();
			for (int[] q : quarts) expected.add(forward.getNoiseBiome(q[0], q[1], q[2]));
			BiomeResolver reverse = resolver(h, decode(h, source));
			for (int i = quarts.size() - 1; i >= 0; i--) {
				int[] q = quarts.get(i);
				h.assertTrue(reverse.getNoiseBiome(q[0], q[1], q[2]).equals(expected.get(i)), "Reverse order changed quart " + java.util.Arrays.toString(q));
			}
			BiomeResolver scattered = resolver(h, decode(h, source));
			for (int k = 0; k < quarts.size(); k++) {
				int i = (int) ((k * 2_654_435_761L) % quarts.size());
				int[] q = quarts.get(i);
				h.assertTrue(scattered.getNoiseBiome(q[0], q[1], q[2]).equals(expected.get(i)), "Scattered order changed quart " + java.util.Arrays.toString(q));
			}
		}
		h.succeed();
	}

	/** Amendment 4: the exported catalog advertises climate schema 1, alongside its existing contents. */
	@GameTest
	public void catalogAdvertisesClimateSchemaOne(GameTestHelper h) {
		JsonObject catalog = WorldDesigner.catalog(h.getLevel());
		h.assertTrue(catalog.has("climate") && catalog.getAsJsonObject("climate").get("schema").getAsInt() == 1,
				"The catalog must report climate.schema == 1: " + catalog.get("climate"));
		for (String key : List.of("schema", "minecraft", "packFormat", "townSize", "biomes", "blocks", "structures", "noiseSettings", "biomeSource")) {
			h.assertTrue(catalog.has(key), "The catalog lost its " + key);
		}
		h.assertTrue(catalog.get("schema").getAsInt() == 1, "The catalog's own schema must stay 1");
		h.succeed();
	}
}

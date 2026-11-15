package io.github.jimbozoomer.jugcraft.test;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.JsonOps;
import io.github.jimbozoomer.jugcraft.season.JugcraftSeasons;
import io.github.jimbozoomer.jugcraft.world.climate.*;
import java.util.List;
import java.util.function.Consumer;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterLists;

/**
 * The climate bands' core (world/climate) against the real registries: the profile codec and its eleven rules, the
 * category tags covering every biome of the real Overworld source, the eighteen pools bound and fitting their zones
 * (temperature, precipitation and the seasons tags), and the remapper's roles, caves and strict limits. Generation in
 * a real world is the runtime's and the audit's ({@code ClimateRuntimeGameTests}, {@code ClimateDesignClientGameTests}).
 */
public class ClimateCoreGameTests {
	/** The profile the World Designer exports (salt written out, no pools). */
	private static final String EXPORTED = "{\"schema\":1,\"anchor\":{\"mode\":\"fixed\",\"x\":-128,\"z\":256},\"north_frigid\":2500,"
			+ "\"south_hot_start\":2000,\"south_hot_end\":4000,\"south_frigid\":5500,\"transition\":256,\"salt\":0}";

	private static RegistryOps<JsonElement> ops(GameTestHelper h) {
		return RegistryOps.create(JsonOps.INSTANCE, h.getLevel().registryAccess());
	}

	private static JsonObject exported() {
		return JsonParser.parseString(EXPORTED).getAsJsonObject();
	}

	private static ClimateRemapper defaults(GameTestHelper h) {
		return new ClimateRemapper(ClimateProfile.CODEC.parse(ops(h), exported()).getOrThrow());
	}

	private static boolean refused(GameTestHelper h, Consumer<JsonObject> change) {
		JsonObject json = exported();
		change.accept(json);
		return ClimateProfile.CODEC.parse(ops(h), json).error().isPresent();
	}

	@GameTest
	public void profileCodecRoundTripsAndOmitsDefaults(GameTestHelper h) {
		var ops = ops(h);
		ClimateProfile profile = ClimateProfile.CODEC.parse(ops, exported()).getOrThrow();
		h.assertTrue(profile.anchorX() == -128 && profile.anchorZ() == 256 && profile.salt() == 0, "Anchor and salt decode");
		h.assertTrue(profile.geometry().equals(new ClimateGeometry(new ClimateAnchor(-128, 256), 2500, 2000, 4000, 5500, 256, 0)),
				"The exported geometry decodes exactly");
		for (ClimateZone zone : ClimateZone.BANDED) {
			for (ClimateCategory category : ClimateCategory.POOLED) {
				h.assertTrue(profile.pools().isDefault(zone, category), zone.id() + "/" + category.id() + " defaults to its tag");
			}
		}
		JsonObject encoded = ClimateProfile.CODEC.encodeStart(ops, profile).getOrThrow().getAsJsonObject();
		h.assertTrue(!encoded.has("salt") && !encoded.has("pools"), "Salt 0 and default pools are left out: " + encoded);
		JsonObject without = exported();
		without.remove("salt");
		h.assertTrue(encoded.equals(ClimateProfile.CODEC.encodeStart(ops, ClimateProfile.CODEC.parse(ops, without).getOrThrow()).getOrThrow()),
				"Salt may be omitted (default 0)");
		ClimateProfile again = ClimateProfile.CODEC.parse(ops, encoded).getOrThrow();
		h.assertTrue(again.geometry().equals(profile.geometry()) && again.equals(profile), "The profile round-trips");
		// One override survives, alone; salt survives when not 0.
		JsonObject custom = exported();
		custom.addProperty("salt", -7);
		JsonObject pools = new JsonObject(), hot = new JsonObject();
		hot.add("beach", JsonParser.parseString("[\"minecraft:beach\"]"));
		pools.add("hot", hot);
		custom.add("pools", pools);
		ClimateProfile overridden = ClimateProfile.CODEC.parse(ops, custom).getOrThrow();
		JsonObject reencoded = ClimateProfile.CODEC.encodeStart(ops, overridden).getOrThrow().getAsJsonObject();
		h.assertTrue(reencoded.get("salt").getAsInt() == -7 && reencoded.getAsJsonObject("pools").size() == 1
				&& reencoded.getAsJsonObject("pools").getAsJsonObject("hot").size() == 1, "Only the override is written: " + reencoded);
		h.assertTrue(reencoded.equals(ClimateProfile.CODEC.encodeStart(ops, ClimateProfile.CODEC.parse(ops, reencoded).getOrThrow()).getOrThrow()),
				"An overridden profile re-encodes identically");
		h.assertTrue(new ClimateRemapper(overridden).members(ClimateZone.HOT, ClimateCategory.BEACH).size() == 1, "The override is used");
		h.succeed();
	}

	@GameTest
	public void profileCodecRefusesEveryBrokenRule(GameTestHelper h) {
		h.assertTrue(refused(h, j -> j.addProperty("schema", 2)), "rule 1");
		h.assertTrue(refused(h, j -> j.getAsJsonObject("anchor").addProperty("mode", "climate_spawn")), "rule 2: climate_spawn is removed");
		h.assertTrue(refused(h, j -> j.getAsJsonObject("anchor").addProperty("x", 30_000_001)), "rule 3");
		h.assertTrue(!refused(h, j -> j.getAsJsonObject("anchor").addProperty("z", -30_000_000)), "rule 3 boundary");
		h.assertTrue(refused(h, j -> j.addProperty("north_frigid", 1_000_001)), "rule 4");
		h.assertTrue(refused(h, j -> j.addProperty("transition", 1025)), "rule 5");
		h.assertTrue(refused(h, j -> j.addProperty("salt", 2_147_483_648L)), "rule 6: salt is 32-bit");
		h.assertTrue(refused(h, j -> j.addProperty("north_frigid", 256)), "rule 7");
		h.assertTrue(refused(h, j -> j.addProperty("south_hot_end", 2000)), "rule 8");
		h.assertTrue(refused(h, j -> j.addProperty("south_hot_start", 128)), "rule 9");
		h.assertTrue(refused(h, j -> j.addProperty("south_hot_end", 2255)), "rule 10");
		h.assertTrue(!refused(h, j -> j.addProperty("south_hot_end", 2256)), "rule 10 boundary");
		h.assertTrue(refused(h, j -> j.addProperty("south_frigid", 4383)), "rule 11");
		h.assertTrue(!refused(h, j -> j.addProperty("south_frigid", 4384)), "rule 11 boundary");
		h.assertTrue(refused(h, j -> j.addProperty("north_frigid", 2500.5)), "Fractions are refused, never rounded");
		h.assertTrue(!refused(h, j -> j.add("north_frigid", new JsonPrimitive(2500.0))), "A whole number written with a point is an integer");
		h.assertTrue(refused(h, j -> j.remove("transition")), "Transition is required");
		h.assertTrue(refused(h, j -> j.add("pools", JsonParser.parseString("{\"frigid\":{\"land\":[]}}"))), "An empty pool list is refused");
		h.assertTrue(refused(h, j -> j.add("pools", JsonParser.parseString("{\"frigid\":{\"land\":\"#jugcraft:climate/no_such_pool\"}}"))),
				"A missing pool tag is refused");
		h.assertTrue(refused(h, j -> j.add("pools", JsonParser.parseString("{\"central\":{\"land\":\"minecraft:plains\"}}"))),
				"Central has no pools");
		h.assertTrue(refused(h, j -> j.add("pools", JsonParser.parseString("{\"hot\":{\"preserve\":\"minecraft:plains\"}}"))),
				"Preserve has no pool");
		h.succeed();
	}

	/** Contract amendment 2, B3: every pool is bound, non-empty, and really fits its zone. */
	@GameTest
	public void poolsAreBoundAndFitTheirZones(GameTestHelper h) {
		ClimateRemapper remapper = defaults(h);
		BlockPos sea = new BlockPos(0, h.getLevel().getSeaLevel(), 0);
		List<ResourceKey<Biome>> frozenModifier = List.of(Biomes.FROZEN_OCEAN, Biomes.DEEP_FROZEN_OCEAN);
		for (ClimateZone zone : ClimateZone.BANDED) {
			for (ClimateCategory category : ClimateCategory.POOLED) {
				List<Holder<Biome>> members = remapper.members(zone, category);
				h.assertTrue(!members.isEmpty(), zone.id() + "/" + category.id() + " is empty");
				boolean water = category == ClimateCategory.OCEAN || category == ClimateCategory.DEEP_OCEAN || category == ClimateCategory.RIVER;
				for (Holder<Biome> b : members) {
					String name = zone.id() + "/" + category.id() + " " + b.getRegisteredName();
					float t = b.value().getBaseTemperature();
					boolean seasons = b.is(JugcraftSeasons.HAS_SEASONS), winter = b.is(JugcraftSeasons.HAS_WINTER_SNOW);
					h.assertTrue(remapper.possibleBiomes().contains(b), name + " is missing from possibleBiomes");
					ClimateCategory own = ClimateCategories.classify(b);
					h.assertTrue(own != null && own.pooled(), name + " is uncategorized or a cave");
					switch (zone) {
						case FRIGID -> h.assertTrue((t < 0.15F || frozenModifier.stream().anyMatch(b::is)) && !seasons && !winter,
								name + " is not always frozen: temperature " + t + ", seasons " + seasons + ", winter snow " + winter);
						case HOT -> h.assertTrue(t >= (water ? 0.5F : 0.8F) && !seasons && !winter,
								name + " is not hot: temperature " + t + ", seasons " + seasons + ", winter snow " + winter);
						case TEMPERATE -> h.assertTrue(t >= 0.15F && t <= 0.9F
										&& b.value().getPrecipitationAt(sea, h.getLevel().getSeaLevel()) == Biome.Precipitation.RAIN,
								name + " is not temperate: temperature " + t);
						default -> h.fail("No pools in central");
					}
				}
			}
		}
		// Pools keep their tags' order (driest first), which the humidity groups rely on.
		h.assertTrue(remapper.members(ClimateZone.HOT, ClimateCategory.BEACH).stream().map(Holder::getRegisteredName).toList()
				.equals(List.of("jugcraft:dune_beach", "minecraft:beach", "jugcraft:overgrown_beach")), "Pool order follows the tag");
		h.succeed();
	}

	/** Contract amendment 1, A4: every biome the real Overworld source can return has exactly one category. */
	@GameTest
	public void everyOverworldBiomeHasOneCategory(GameTestHelper h) {
		var preset = h.getLevel().registryAccess().lookupOrThrow(Registries.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST)
				.getOrThrow(MultiNoiseBiomeSourceParameterLists.OVERWORLD);
		var possible = MultiNoiseBiomeSource.createFromPreset(preset).possibleBiomes();
		ClimateRemapper.requireCategorized(possible);
		for (Holder<Biome> b : possible) {
			long count = ClimateCategory.PRECEDENCE.stream().filter(c -> b.is(ClimateCategories.tag(c))).count();
			h.assertTrue(count == 1, b.getRegisteredName() + " is in " + count + " category tags");
		}
		h.assertTrue(possible.size() >= 250, "The Overworld lists Jugcraft's biomes too: " + possible.size());
		boolean refused = false;
		try {
			ClimateRemapper.requireCategorized(List.of(h.getLevel().registryAccess().lookupOrThrow(Registries.BIOME).getOrThrow(Biomes.NETHER_WASTES)));
		} catch (IllegalStateException expected) {
			refused = true;
		}
		h.assertTrue(refused, "A biome in no category is refused");
		h.succeed();
	}

	@GameTest
	public void remapKeepsRolesCavesAndStrictLimits(GameTestHelper h) {
		var biomes = h.getLevel().registryAccess().lookupOrThrow(Registries.BIOME);
		ClimateRemapper remapper = defaults(h);
		Climate.Sampler sampler = h.getLevel().getChunkSource().randomState()
				.createClimateSampler(net.minecraft.world.level.levelgen.densityfunction.SamplerContext.EMPTY_UNCACHED);
		int ax = -128, az = 256;
		record Probe(ClimateZone zone, int z) {
		}
		List<Probe> probes = List.of(new Probe(ClimateZone.FRIGID, az - 3000), new Probe(ClimateZone.HOT, az + 3000),
				new Probe(ClimateZone.TEMPERATE, az + 4800), new Probe(ClimateZone.FRIGID, az + 6000));
		record Role(ResourceKey<Biome> base, ClimateCategory category) {
		}
		for (Role role : List.of(new Role(Biomes.OCEAN, ClimateCategory.OCEAN), new Role(Biomes.DEEP_OCEAN, ClimateCategory.DEEP_OCEAN),
				new Role(Biomes.RIVER, ClimateCategory.RIVER), new Role(Biomes.BEACH, ClimateCategory.BEACH),
				new Role(Biomes.JAGGED_PEAKS, ClimateCategory.MOUNTAIN), new Role(Biomes.PLAINS, ClimateCategory.LAND),
				new Role(Biomes.MUSHROOM_FIELDS, ClimateCategory.LAND))) {
			Holder<Biome> base = biomes.getOrThrow(role.base());
			for (Probe probe : probes) {
				for (int x = -5000; x <= 5000; x += 500) {
					Holder<Biome> got = remapper.remap(base, x >> 2, 16, probe.z() >> 2, sampler);
					h.assertTrue(remapper.members(probe.zone(), role.category()).contains(got),
							role.base().identifier() + " at " + x + " " + probe.z() + " became " + got.getRegisteredName());
				}
			}
			h.assertTrue(remapper.remap(base, ax >> 2, 16, az >> 2, sampler) == base, "Central keeps the base instance");
		}
		for (ResourceKey<Biome> cave : List.of(Biomes.DRIPSTONE_CAVES, Biomes.LUSH_CAVES, Biomes.DEEP_DARK)) {
			Holder<Biome> base = biomes.getOrThrow(cave);
			for (Probe probe : probes) {
				for (int y : new int[]{-16, 0, 16, 40}) {
					h.assertTrue(remapper.remap(base, 0, y, probe.z() >> 2, sampler) == base, cave.identifier() + " stays at quart y " + y);
				}
			}
		}
		// Strict: every quart that a block at or beyond either limit can read is frigid land, whatever the base.
		Holder<Biome> plains = biomes.getOrThrow(Biomes.PLAINS);
		for (int x = -20_000; x <= 20_000; x += 1999) {
			for (int z = az + 5500; z < az + 5600; z++) {
				for (int q : new int[]{(z - 2) >> 2, ((z - 2) >> 2) + 1}) {
					h.assertTrue(remapper.members(ClimateZone.FRIGID, ClimateCategory.LAND).contains(remapper.remap(plains, x >> 2, 16, q, sampler)),
							"South limit strict at block z " + z);
				}
			}
			for (int z = az - 2500; z > az - 2600; z--) {
				for (int q : new int[]{(z - 2) >> 2, ((z - 2) >> 2) + 1}) {
					h.assertTrue(remapper.members(ClimateZone.FRIGID, ClimateCategory.LAND).contains(remapper.remap(plains, x >> 2, 16, q, sampler)),
							"North limit strict at block z " + z);
				}
			}
		}
		// A biome in no category: unchanged in central, refused in a belt.
		Holder<Biome> nether = biomes.getOrThrow(Biomes.NETHER_WASTES);
		h.assertTrue(remapper.remap(nether, 0, 16, az >> 2, sampler) == nether, "Central never looks at categories");
		boolean refused = false;
		try {
			remapper.remap(nether, 0, 16, (az + 3000) >> 2, sampler);
		} catch (IllegalStateException expected) {
			refused = true;
		}
		h.assertTrue(refused, "An uncategorized base is refused in a belt, never kept silently");
		// The checking constructor precomputes name hashes for the base's biomes: it must answer exactly as the plain one,
		// for every Overworld base biome, in every belt.
		var preset = h.getLevel().registryAccess().lookupOrThrow(Registries.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST)
				.getOrThrow(MultiNoiseBiomeSourceParameterLists.OVERWORLD);
		var overworld = MultiNoiseBiomeSource.createFromPreset(preset).possibleBiomes();
		ClimateRemapper checked = new ClimateRemapper(ClimateProfile.CODEC.parse(ops(h), exported()).getOrThrow(), overworld);
		for (Holder<Biome> base : overworld) {
			for (Probe probe : probes) {
				for (int x = -3000; x <= 3000; x += 1500) {
					h.assertTrue(checked.remap(base, x >> 2, 16, probe.z() >> 2, sampler) == remapper.remap(base, x >> 2, 16, probe.z() >> 2, sampler),
							"Precomputed name hashes change no answer: " + base.getRegisteredName());
				}
			}
		}
		// Deterministic: a second remapper of the same profile answers the same, in any order.
		ClimateRemapper twin = defaults(h);
		for (int i = 0; i < 400; i++) {
			int x = (i * 7919) % 20000 - 10000, z = az + 1800 + (i * 104729) % 4000;
			h.assertTrue(twin.remap(plains, x >> 2, 16, z >> 2, sampler) == remapper.remap(plains, x >> 2, 16, z >> 2, sampler),
					"Same place, same answer");
		}
		h.succeed();
	}
}

package io.github.jimbozoomer.jugcraft.test;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.world.climate.ClimateBiomeSource;
import io.github.jimbozoomer.jugcraft.world.climate.ClimateCategories;
import io.github.jimbozoomer.jugcraft.world.climate.ClimateLayer;
import io.github.jimbozoomer.jugcraft.world.climate.ClimateProfile;
import io.github.jimbozoomer.jugcraft.world.design.DesignBiomeSource;
import io.github.jimbozoomer.jugcraft.world.design.DesignGrid;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.FixedBiomeSource;

/**
 * The climate bands' runtime (world/climate, the design source's {@code climate} field): codecs, the old design format,
 * painted cells winning, pools in {@code possibleBiomes}, refusals, and the bands as the resolvers answer them. Real
 * generation and save/reopen are {@link ClimateDesignClientGameTests}.
 *
 * <p>The test profile is small, so its bands fall on and near a 1,024-block design: anchor (-128, 256), the design's
 * spawn; north frigid 256, hot 128 to 320, south frigid 480, transitions 64. North is -Z, s = z - 256:
 * frigid z &le; 0; central 64 &lt; z &lt; 352; hot 416 &lt; z &lt; 544; temperate 608 &lt; z &lt; 672; frigid z &ge; 736.
 */
public class ClimateRuntimeGameTests {
    @GameTest
    public void climate_lookup_ties_ignore_previous_queries(GameTestHelper helper) {
        var left = net.minecraft.world.level.biome.Climate.parameters(-1, 0, 0, 0, 0, 0, 0);
        var right = net.minecraft.world.level.biome.Climate.parameters(1, 0, 0, 0, 0, 0, 0);
        var table = new net.minecraft.world.level.biome.Climate.ParameterList<>(List.of(
                com.mojang.datafixers.util.Pair.of(left, "left"), com.mojang.datafixers.util.Pair.of(right, "right")));
        var center = net.minecraft.world.level.biome.Climate.target(0, 0, 0, 0, 0, 0);
        var atLeft = net.minecraft.world.level.biome.Climate.target(-1, 0, 0, 0, 0, 0);
        var atRight = net.minecraft.world.level.biome.Climate.target(1, 0, 0, 0, 0, 0);
        table.findValue(atLeft);
        String oldLeft = table.findValue(center);
        table.findValue(atRight);
        String oldRight = table.findValue(center);
        helper.assertTrue(!oldLeft.equals(oldRight), "Regression must reproduce vanilla's history-dependent tie");
        table.findValue(atLeft);
        String stable = io.github.jimbozoomer.jugcraft.world.climate.StableBiomeLookup.find(table, center);
        for (int i = 0; i < 100; i++) {
            table.findValue((i & 1) == 0 ? atLeft : atRight);
            helper.assertTrue(stable.equals(io.github.jimbozoomer.jugcraft.world.climate.StableBiomeLookup.find(table, center)),
                    "Climate lookup tie changed after an unrelated lookup");
        }
        helper.succeed();
    }

    static final String[] ZONES = {"frigid", "hot", "temperate"};
    static final String[] CATEGORIES = {"land", "ocean", "deep_ocean", "river", "beach", "mountain"};
    static final int ANCHOR_X = -128;
    static final int ANCHOR_Z = 256;
    /** Block z inside each pure zone of the test profile. */
    static final int FRIGID_NORTH_Z = -100;
    static final int CENTRAL_Z = 200;
    static final int HOT_Z = 480;
    static final int TEMPERATE_Z = 640;
    static final int FRIGID_SOUTH_Z = 800;

    static TagKey<Biome> pool(String zone, String category) {
        return TagKey.create(Registries.BIOME, Jugcraft.id("climate/" + zone + "/" + category));
    }

    /** The zone the test profile gives block z; null in a transition (no position here is tested there). */
    static String zone(int z) {
        if (z <= 0 || z >= 736) return "frigid";
        if (z > 64 && z < 352) return "central";
        if (z > 416 && z < 544) return "hot";
        if (z > 608 && z < 672) return "temperate";
        return null;
    }

    /** The climate category the remapper itself gives a biome (its category tag, in the core's precedence); null if none. */
    static String categoryOf(Holder<Biome> biome) {
        var category = ClimateCategories.classify(biome);
        return category == null ? null : category.id();
    }

    static boolean inZone(Holder<Biome> biome, String zone) {
        for (String c : CATEGORIES) if (biome.is(pool(zone, c))) return true;
        return false;
    }

    static JsonObject profileJson(int anchorX, int anchorZ, int north, int hotStart, int hotEnd, int southFrigid, int transition) {
        JsonObject climate = new JsonObject();
        climate.addProperty("schema", 1);
        JsonObject anchor = new JsonObject();
        anchor.addProperty("mode", "fixed"); anchor.addProperty("x", anchorX); anchor.addProperty("z", anchorZ);
        climate.add("anchor", anchor);
        climate.addProperty("north_frigid", north);
        climate.addProperty("south_hot_start", hotStart);
        climate.addProperty("south_hot_end", hotEnd);
        climate.addProperty("south_frigid", southFrigid);
        climate.addProperty("transition", transition);
        return climate;
    }

    static JsonObject testProfileJson() {
        return profileJson(ANCHOR_X, ANCHOR_Z, 256, 128, 320, 480, 64);
    }

    static ClimateProfile profile(RegistryOps<JsonElement> ops, JsonObject json) {
        return ClimateProfile.CODEC.parse(ops, json).getOrThrow();
    }

    /** The test design: flat land at 80 over -512..512; columns west of x=0 painted desert, east of it unpainted. */
    static DesignGrid grid() {
        return new DesignGrid(-512, -512, 32, 33, Collections.nCopies(33 * 33, 80f));
    }

    static List<Integer> westPainted() {
        var cells = new ArrayList<Integer>();
        for (int z = 0; z < 33; z++) for (int x = 0; x < 33; x++) cells.add(x < 16 ? 1 : 0);
        return cells;
    }

    static DesignBiomeSource design(BiomeSource base, HolderLookup.RegistryLookup<Biome> biomes, Optional<ClimateProfile> climate) {
        return new DesignBiomeSource(base, grid(), List.of(biomes.getOrThrow(Biomes.DESERT)), westPainted(),
                new BlockPos(ANCHOR_X, 0, ANCHOR_Z), Optional.empty(), climate);
    }

    static Holder<Biome> at(BiomeResolver resolver, int blockX, int blockY, int blockZ) {
        return resolver.getNoiseBiome(blockX >> 2, blockY >> 2, blockZ >> 2);
    }

    static String id(Holder<Biome> biome) {
        return biome.unwrapKey().map(key -> key.identifier().toString()).orElse("<direct>");
    }

    private static RegistryOps<JsonElement> ops(GameTestHelper h) {
        return RegistryOps.create(JsonOps.INSTANCE, h.getLevel().registryAccess());
    }

    private static HolderLookup.RegistryLookup<Biome> biomes(GameTestHelper h) {
        return h.getLevel().registryAccess().lookupOrThrow(Registries.BIOME);
    }

    private static BiomeResolver resolver(GameTestHelper h, BiomeSource source) {
        return source.createUncachedResolver(h.getLevel().getChunkSource().randomState());
    }

    /** A design written before climate existed decodes with climate off, re-encodes identically and answers as before. */
    @GameTest
    public void oldDesignJsonIsUnchanged(GameTestHelper h) {
        var biomes = biomes(h);
        var plains = biomes.getOrThrow(Biomes.PLAINS);
        var ops = ops(h);
        var old = new DesignBiomeSource(new FixedBiomeSource(plains), grid(), List.of(biomes.getOrThrow(Biomes.DESERT)), westPainted(),
                new BlockPos(ANCHOR_X, 0, ANCHOR_Z), Optional.empty());
        JsonElement json = BiomeSource.CODEC.encodeStart(ops, old).getOrThrow();
        h.assertTrue(!json.getAsJsonObject().has("climate"), "A design without climate must not write a climate field");
        BiomeSource decoded = BiomeSource.CODEC.parse(ops, json).getOrThrow();
        h.assertTrue(decoded instanceof DesignBiomeSource d && d.climate().isEmpty() && !ClimateLayer.applies(d),
                "Old design JSON must load with climate disabled");
        JsonElement again = BiomeSource.CODEC.encodeStart(ops, decoded).getOrThrow();
        h.assertTrue(json.toString().equals(again.toString()), "Old design JSON must re-encode byte for byte: " + json + " vs " + again);
        Set<Holder<Biome>> expected = Set.of(plains, biomes.getOrThrow(Biomes.DESERT));
        h.assertTrue(new HashSet<>(decoded.possibleBiomes()).equals(expected), "Old designs must list only base and palette biomes");
        var resolver = resolver(h, decoded);
        h.assertTrue(at(resolver, -200, 80, FRIGID_NORTH_Z).is(Biomes.DESERT), "Paint must still answer");
        for (int z : new int[]{FRIGID_NORTH_Z, CENTRAL_Z, HOT_Z, TEMPERATE_Z, FRIGID_SOUTH_Z, -4000, 4000}) {
            h.assertTrue(at(resolver, 200, 80, z).is(Biomes.PLAINS) && at(resolver, 4000, 80, z).is(Biomes.PLAINS),
                    "Without climate the fallback must be the base source's answer at z " + z);
        }
        h.succeed();
    }

    /** A climate design round-trips; painted cells win in every band; unpainted and outside land follows the bands. */
    @GameTest
    public void climateDesignRoundTripsAndPaintWins(GameTestHelper h) {
        var biomes = biomes(h);
        var ops = ops(h);
        var source = design(new FixedBiomeSource(biomes.getOrThrow(Biomes.PLAINS)), biomes, Optional.of(profile(ops, testProfileJson())));
        JsonElement json = BiomeSource.CODEC.encodeStart(ops, source).getOrThrow();
        h.assertTrue(json.getAsJsonObject().has("climate"), "The climate field must be written");
        BiomeSource decoded = BiomeSource.CODEC.parse(ops, json).getOrThrow();
        h.assertTrue(decoded instanceof DesignBiomeSource d && d.climate().isPresent() && ClimateLayer.applies(d), "Climate must survive decoding");
        h.assertTrue(json.toString().equals(BiomeSource.CODEC.encodeStart(ops, decoded).getOrThrow().toString()), "Climate design must re-encode identically");
        var resolver = resolver(h, decoded);
        // Painted cells win, even in strict frigid: x -200 is painted desert.
        for (int z : new int[]{FRIGID_NORTH_Z, -480, CENTRAL_Z, HOT_Z}) {
            h.assertTrue(at(resolver, -200, 80, z).is(Biomes.DESERT), "A painted cell was changed by the climate at z " + z);
        }
        // Unpainted cells (x 200) and outside the design (x 4000, far east and west) follow the bands.
        for (int x : new int[]{200, 4000, -4000, 1_000_000, -1_000_000}) {
            h.assertTrue(at(resolver, x, 80, CENTRAL_Z).is(Biomes.PLAINS), "Central must be the unchanged base at x " + x);
            for (int z : new int[]{FRIGID_NORTH_Z, -3000, FRIGID_SOUTH_Z, 3000}) {
                h.assertTrue(at(resolver, x, 80, z).is(pool("frigid", "land")), "Not frigid land at " + x + " " + z + ": " + id(at(resolver, x, 80, z)));
            }
            h.assertTrue(at(resolver, x, 80, HOT_Z).is(pool("hot", "land")), "Not hot land at x " + x);
            h.assertTrue(at(resolver, x, 80, TEMPERATE_Z).is(pool("temperate", "land")), "Not temperate land at x " + x);
        }
        // The anchor, not the origin: from the origin z -100 is n = 100, central; from the anchor it is n = 356, frigid.
        h.assertTrue(!at(resolver, 200, 80, -100).is(Biomes.PLAINS), "Bands must be measured from the anchor, not the origin");
        // Below Y0 the paint never applied: the fallback (here land) follows the bands there too.
        h.assertTrue(at(resolver, -200, -20, FRIGID_NORTH_Z).is(pool("frigid", "land")), "Below the paint the fallback must follow the bands");
        h.succeed();
    }

    /** Oceans, rivers, beaches, deep oceans and mountains stay in their role; caves stay at any depth; surface specials move. */
    @GameTest
    public void categoriesStayInRoleAndCavesStay(GameTestHelper h) {
        var biomes = biomes(h);
        var ops = ops(h);
        var profile = profile(ops, testProfileJson());
        record Role(ResourceKey<Biome> base, String category) { }
        for (Role role : List.of(new Role(Biomes.OCEAN, "ocean"), new Role(Biomes.DEEP_OCEAN, "deep_ocean"), new Role(Biomes.RIVER, "river"),
                new Role(Biomes.BEACH, "beach"), new Role(Biomes.JAGGED_PEAKS, "mountain"), new Role(Biomes.MUSHROOM_FIELDS, "land"))) {
            var resolver = resolver(h, design(new FixedBiomeSource(biomes.getOrThrow(role.base())), biomes, Optional.of(profile)));
            for (String zone : ZONES) {
                int z = zone.equals("frigid") ? FRIGID_NORTH_Z : zone.equals("hot") ? HOT_Z : TEMPERATE_Z;
                Holder<Biome> got = at(resolver, 200, 60, z);
                h.assertTrue(got.is(pool(zone, role.category())), role.base().identifier() + " in " + zone + " became " + id(got));
            }
        }
        for (ResourceKey<Biome> cave : List.of(Biomes.DRIPSTONE_CAVES, Biomes.LUSH_CAVES, Biomes.DEEP_DARK)) {
            var resolver = resolver(h, design(new FixedBiomeSource(biomes.getOrThrow(cave)), biomes, Optional.of(profile)));
            for (int z : new int[]{FRIGID_NORTH_Z, HOT_Z, TEMPERATE_Z, FRIGID_SOUTH_Z}) {
                for (int y : new int[]{-40, 20, 90}) {
                    h.assertTrue(at(resolver, 200, y, z).is(cave), cave.identifier() + " must stay at y " + y + " z " + z);
                }
            }
        }
        h.succeed();
    }

    /** possibleBiomes holds the base's, the palette's and every member of every (non-empty) pool, for both sources. */
    @GameTest
    public void possibleBiomesHoldEveryPool(GameTestHelper h) {
        var biomes = biomes(h);
        var ops = ops(h);
        var profile = profile(ops, testProfileJson());
        var plains = biomes.getOrThrow(Biomes.PLAINS);
        Set<Holder<Biome>> required = new HashSet<>(List.of(plains, biomes.getOrThrow(Biomes.DESERT)));
        for (String zone : ZONES) {
            for (String c : CATEGORIES) {
                var members = biomes.getOrThrow(pool(zone, c));
                h.assertTrue(members.size() > 0, "Pool " + zone + "/" + c + " must not be empty");
                members.stream().forEach(required::add);
            }
        }
        var designed = design(new FixedBiomeSource(plains), biomes, Optional.of(profile));
        h.assertTrue(designed.possibleBiomes().containsAll(required), "A climate design must list every pool member");
        var banded = new ClimateBiomeSource(new FixedBiomeSource(plains), profile);
        required.remove(biomes.getOrThrow(Biomes.DESERT));
        h.assertTrue(banded.possibleBiomes().containsAll(required), "A climate source must list every pool member and its base's");
        h.succeed();
    }

    /** jugcraft:climate round-trips and answers by band; double bands, a design base and a wrong anchor are refused. */
    @GameTest
    public void climateSourceRoundTripsAndRefusals(GameTestHelper h) {
        var biomes = biomes(h);
        var ops = ops(h);
        var profileJson = testProfileJson();
        var profile = profile(ops, profileJson);
        var plains = new FixedBiomeSource(biomes.getOrThrow(Biomes.PLAINS));
        var banded = new ClimateBiomeSource(plains, profile);
        JsonElement json = BiomeSource.CODEC.encodeStart(ops, banded).getOrThrow();
        h.assertTrue(json.getAsJsonObject().get("type").getAsString().equals("jugcraft:climate"), "Climate source must register as jugcraft:climate");
        BiomeSource decoded = BiomeSource.CODEC.parse(ops, json).getOrThrow();
        h.assertTrue(decoded instanceof ClimateBiomeSource && json.toString().equals(BiomeSource.CODEC.encodeStart(ops, decoded).getOrThrow().toString()),
                "Climate source must round-trip");
        var resolver = resolver(h, decoded);
        h.assertTrue(at(resolver, 0, 80, CENTRAL_Z).is(Biomes.PLAINS) && at(resolver, 0, 80, FRIGID_NORTH_Z).is(pool("frigid", "land"))
                && at(resolver, 0, 80, HOT_Z).is(pool("hot", "land")), "Climate source must answer by band");

        JsonObject nested = new JsonObject();
        nested.addProperty("type", "jugcraft:climate"); nested.add("base", json); nested.add("climate", profileJson.deepCopy());
        h.assertTrue(BiomeSource.CODEC.parse(ops, nested).error().isPresent(), "Climate over climate must be refused");
        h.assertTrue(BiomeSource.CODEC.encodeStart(ops, new ClimateBiomeSource(banded, profile)).error().isPresent(), "Climate over climate must not encode");

        JsonElement plainDesign = BiomeSource.CODEC.encodeStart(ops, design(plains, biomes, Optional.empty())).getOrThrow();
        JsonObject overDesign = new JsonObject();
        overDesign.addProperty("type", "jugcraft:climate"); overDesign.add("base", plainDesign); overDesign.add("climate", profileJson.deepCopy());
        h.assertTrue(BiomeSource.CODEC.parse(ops, overDesign).error().isPresent(), "Climate over a design must be refused: paint must win");

        JsonObject designOverClimate = BiomeSource.CODEC.encodeStart(ops, design(plains, biomes, Optional.of(profile))).getOrThrow().getAsJsonObject();
        designOverClimate.add("base", json);
        h.assertTrue(BiomeSource.CODEC.parse(ops, designOverClimate).error().isPresent(), "A climate design over a climate source must be refused");

        JsonObject moved = BiomeSource.CODEC.encodeStart(ops, design(plains, biomes, Optional.of(profile))).getOrThrow().getAsJsonObject();
        moved.getAsJsonObject("climate").getAsJsonObject("anchor").addProperty("x", ANCHOR_X + 16);
        h.assertTrue(BiomeSource.CODEC.parse(ops, moved).error().isPresent(), "A design climate anchor away from the design spawn must be refused");
        h.succeed();
    }

    /** An empty or unknown pool fails loudly: when decoded, or when a resolver is made. Never a silent base fallback. */
    @GameTest
    public void emptyOrUnknownPoolFailsLoudly(GameTestHelper h) {
        var biomes = biomes(h);
        var ops = ops(h);
        var plains = new FixedBiomeSource(biomes.getOrThrow(Biomes.PLAINS));
        for (JsonElement bad : List.<JsonElement>of(new JsonArray(), new com.google.gson.JsonPrimitive("#jugcraft:climate/no_such_pool"))) {
            JsonObject json = testProfileJson();
            JsonObject pools = new JsonObject();
            for (String zone : ZONES) {
                JsonObject members = new JsonObject();
                for (String c : CATEGORIES) members.addProperty(c, "#jugcraft:climate/" + zone + "/" + c);
                pools.add(zone, members);
            }
            pools.getAsJsonObject("frigid").add("land", bad);
            json.add("pools", pools);
            var parsed = ClimateProfile.CODEC.parse(ops, json);
            if (parsed.error().isPresent()) continue;
            var source = design(plains, biomes, Optional.of(parsed.getOrThrow()));
            boolean refused;
            try {
                resolver(h, source).getNoiseBiome(50, 20, FRIGID_NORTH_Z >> 2);
                refused = false;
            } catch (IllegalStateException expected) {
                refused = true;
            }
            h.assertTrue(refused, "A climate profile with frigid land " + bad + " must be refused, not answered");
        }
        h.succeed();
    }

    /**
     * A base biome in no category tag (here one from another dimension, as another mod's or a data pack's would be) is
     * refused, by name, when a resolver is made: before the bands answer anything, never part-way through a chunk. The
     * palette is not checked, since painted cells are never remapped; without climate the same base is untouched.
     */
    @GameTest
    public void uncategorizedBaseIsRefusedAtResolverConstruction(GameTestHelper h) {
        var biomes = biomes(h);
        var profile = profile(ops(h), testProfileJson());
        Holder<Biome> stray = biomes.listElements().filter(b -> ClimateCategories.classify(b) == null).findFirst().orElse(null);
        h.assertTrue(stray != null, "No uncategorized biome is registered to test with (the Nether's should be)");
        var strayBase = new FixedBiomeSource(stray);
        for (BiomeSource source : List.<BiomeSource>of(design(strayBase, biomes, Optional.of(profile)), new ClimateBiomeSource(strayBase, profile))) {
            String refusal = null;
            try {
                resolver(h, source);
            } catch (IllegalStateException expected) {
                refusal = expected.getMessage();
            }
            h.assertTrue(refusal != null && refusal.contains(stray.getRegisteredName()),
                    "An uncategorized base " + id(stray) + " must be refused, by name, when the resolver is made: " + refusal);
        }
        h.assertTrue(at(resolver(h, design(strayBase, biomes, Optional.empty())), 200, 80, FRIGID_NORTH_Z).equals(stray),
                "Without climate an uncategorized base must answer unchanged");
        var paintedStray = new DesignBiomeSource(new FixedBiomeSource(biomes.getOrThrow(Biomes.PLAINS)), grid(), List.of(stray), westPainted(),
                new BlockPos(ANCHOR_X, 0, ANCHOR_Z), Optional.empty(), Optional.of(profile));
        var resolver = resolver(h, paintedStray);
        h.assertTrue(at(resolver, -200, 80, FRIGID_NORTH_Z).equals(stray), "An uncategorized painted biome must be accepted and kept");
        h.assertTrue(at(resolver, 200, 80, FRIGID_NORTH_Z).is(pool("frigid", "land")), "The categorized fallback must still follow the bands");
        h.succeed();
    }

    /** Climate is opt-in: no climate preset ships yet, and this test world's ordinary generator applies no bands. */
    @GameTest
    public void climateIsOptIn(GameTestHelper h) {
        var presets = h.getLevel().registryAccess().lookupOrThrow(Registries.WORLD_PRESET);
        h.assertTrue(presets.get(ResourceKey.create(Registries.WORLD_PRESET, Jugcraft.id("climate_bands"))).isEmpty(),
                "The procedural climate preset is deferred and must not be registered");
        var source = h.getLevel().getChunkSource().getGenerator().getBiomeSource();
        h.assertTrue(!ClimateLayer.applies(source) && !(source instanceof ClimateBiomeSource), "An ordinary world must not gain climate bands");
        h.succeed();
    }
}

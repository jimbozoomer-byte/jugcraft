package io.github.jimbozoomer.jugcraft.test;

import com.mojang.datafixers.util.Pair;
import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.season.JugcraftSeasons;
import io.github.jimbozoomer.jugcraft.world.AlpineSpawn;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.StructureTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterList;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterLists;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Alpine Spawn: its place in the Overworld climate table, its tags and seasons, and its villages. */
public class AlpineGameTests {
	private static final Logger LOGGER = LoggerFactory.getLogger("jugcraft-test");

	private static Holder<MultiNoiseBiomeSourceParameterList> overworldPreset(ServerLevel level) {
		return level.registryAccess().lookupOrThrow(Registries.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST)
				.getOrThrow(MultiNoiseBiomeSourceParameterLists.OVERWORLD);
	}

	/** Every cool meadow became Alpine Spawn; temperate meadows are still meadows. */
	@GameTest
	public void alpineSpawnTakesOverCoolMeadows(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		List<Pair<Climate.ParameterPoint, Holder<Biome>>> table = overworldPreset(level).value().parameters().values();
		long alpine = table.stream().filter(entry -> entry.getSecond().is(AlpineSpawn.BIOME)).count();
		long coolMeadows = table.stream().filter(entry -> entry.getSecond().is(Biomes.MEADOW)
				&& entry.getFirst().temperature().max() <= Climate.quantizeCoord(AlpineSpawn.COOL_MAX)).count();
		long meadows = table.stream().filter(entry -> entry.getSecond().is(Biomes.MEADOW)).count();
		LOGGER.info("Overworld climate table: {} entries, {} Alpine Spawn, {} meadows left, {} cool meadows left",
				table.size(), alpine, meadows, coolMeadows);
		helper.assertTrue(alpine > 0, "The Overworld climate table has no Alpine Spawn");
		helper.assertTrue(coolMeadows == 0, coolMeadows + " cool meadows are left");
		helper.assertTrue(meadows > 0, "Temperate meadows are gone too");
		Climate.ParameterPoint cool = Climate.parameters(Climate.Parameter.span(-0.45F, -0.15F), Climate.Parameter.span(-1.0F, 1.0F),
				Climate.Parameter.span(0.0F, 1.0F), Climate.Parameter.span(-1.0F, 1.0F), Climate.Parameter.point(0.0F),
				Climate.Parameter.span(-1.0F, 1.0F), 0.0F);
		Climate.ParameterPoint temperate = Climate.parameters(Climate.Parameter.span(-0.15F, 0.2F), Climate.Parameter.span(-1.0F, 1.0F),
				Climate.Parameter.span(0.0F, 1.0F), Climate.Parameter.span(-1.0F, 1.0F), Climate.Parameter.point(0.0F),
				Climate.Parameter.span(-1.0F, 1.0F), 0.0F);
		helper.assertTrue(AlpineSpawn.replaces(Pair.of(cool, Biomes.MEADOW)), "A cool meadow is not replaced");
		helper.assertTrue(!AlpineSpawn.replaces(Pair.of(temperate, Biomes.MEADOW)), "A temperate meadow is replaced");
		helper.assertTrue(!AlpineSpawn.replaces(Pair.of(cool, Biomes.FOREST)), "A cool forest is replaced");
		helper.succeed();
	}

	@GameTest
	public void alpineSpawnHasSeasonsAndMountainTags(GameTestHelper helper) {
		Registry<Biome> biomes = helper.getLevel().registryAccess().lookupOrThrow(Registries.BIOME);
		Holder<Biome> alpine = biomes.getOrThrow(AlpineSpawn.BIOME);
		helper.assertTrue(alpine.is(JugcraftSeasons.HAS_SEASONS), "Alpine Spawn has no seasonal colours");
		helper.assertTrue(alpine.is(JugcraftSeasons.HAS_WINTER_SNOW), "Alpine Spawn has no winter snow");
		helper.assertTrue(alpine.is(BiomeTags.IS_OVERWORLD) && alpine.is(BiomeTags.IS_MOUNTAIN), "Alpine Spawn is not an Overworld mountain biome");
		helper.assertTrue(alpine.value().getBaseTemperature() < biomes.getOrThrow(Biomes.MEADOW).value().getBaseTemperature(),
				"Alpine Spawn is not cooler than a meadow");
		helper.succeed();
	}

	/** Alpine villages: a village structure in Alpine Spawn only, on a grid much tighter than vanilla's. */
	@GameTest
	public void alpineVillagesAreCommon(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		var structure = level.registryAccess().lookupOrThrow(Registries.STRUCTURE)
				.getOrThrow(ResourceKey.create(Registries.STRUCTURE, Jugcraft.id("village_alpine")));
		helper.assertTrue(structure.is(StructureTags.VILLAGE), "The alpine village is not in #minecraft:village");
		helper.assertTrue(structure.value().biomes().contains(level.registryAccess().lookupOrThrow(Registries.BIOME).getOrThrow(AlpineSpawn.BIOME)),
				"The alpine village does not generate in Alpine Spawn");
		StructureSet set = level.registryAccess().lookupOrThrow(Registries.STRUCTURE_SET)
				.getOrThrow(ResourceKey.create(Registries.STRUCTURE_SET, Jugcraft.id("alpine_villages"))).value();
		helper.assertTrue(set.placement() instanceof RandomSpreadStructurePlacement spread && spread.spacing() < 34,
				"Alpine villages are no closer together than vanilla's: " + set.placement());
		helper.succeed();
	}
}

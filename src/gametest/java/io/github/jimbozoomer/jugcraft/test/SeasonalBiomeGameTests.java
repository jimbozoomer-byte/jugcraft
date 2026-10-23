package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.agriculture.Squirrels;
import io.github.jimbozoomer.jugcraft.agriculture.Turkeys;
import io.github.jimbozoomer.jugcraft.agriculture.Werewolves;
import io.github.jimbozoomer.jugcraft.lair.HagStoneRite;
import io.github.jimbozoomer.jugcraft.season.JugcraftSeasons;
import io.github.jimbozoomer.jugcraft.season.SeasonalKinds;
import io.github.jimbozoomer.jugcraft.world.AlpineSpawn;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBiomeTags;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

/**
 * The seasonal biome audit (docs/features/seasonal-biome-audit.md, tools/seasonal_kinds.py): the newer biomes take the
 * seasons and winter snow by their climate, read as their kinds for the seasonal and fall systems, and reach the fall's
 * creatures and the Hag Stone; the dry lands and the always-frozen lands stay as they were.
 */
public class SeasonalBiomeGameTests {
	@GameTest
	public void newerBiomesTakeTheSeasonsByTheirKinds(GameTestHelper helper) {
		Registry<Biome> biomes = helper.getLevel().registryAccess().lookupOrThrow(Registries.BIOME);
		Holder<Biome> maple = landscape(biomes, "maple_woods");
		Holder<Biome> pumpkins = landscape(biomes, "pumpkin_patch");
		Holder<Biome> canyon = landscape(biomes, "desert_canyon");
		Holder<Biome> wintry = landscape(biomes, "wintry_forest");
		for (Holder<Biome> seasonal : List.of(maple, pumpkins)) {
			helper.assertTrue(seasonal.is(JugcraftSeasons.HAS_SEASONS), name(seasonal) + " has no seasons");
			helper.assertTrue(seasonal.is(JugcraftSeasons.HAS_WINTER_SNOW), name(seasonal) + " has no winter snow");
			helper.assertTrue(SeasonalKinds.is(seasonal, ConventionalBiomeTags.IS_FOREST), name(seasonal) + " is not a forest to the seasons");
		}
		helper.assertTrue(SeasonalKinds.is(pumpkins, ConventionalBiomeTags.IS_SPOOKY), "The pumpkin patch is not spooky to the seasons");
		helper.assertTrue(!SeasonalKinds.is(maple, ConventionalBiomeTags.IS_SPOOKY), "The maple woods are spooky to the seasons");
		// The dry lands and the always-frozen lands look the same all year.
		for (Holder<Biome> fixed : List.of(canyon, wintry)) {
			helper.assertTrue(!fixed.is(JugcraftSeasons.HAS_SEASONS) && !fixed.is(JugcraftSeasons.HAS_WINTER_SNOW), name(fixed) + " has seasons");
		}
		helper.assertTrue(SeasonalKinds.is(wintry, ConventionalBiomeTags.IS_SNOWY), "The wintry forest is not snowy to the seasons");
		for (TagKey<Biome> kind : List.of(ConventionalBiomeTags.IS_FOREST, ConventionalBiomeTags.IS_PLAINS, ConventionalBiomeTags.IS_SPOOKY,
				ConventionalBiomeTags.IS_SWAMP)) {
			helper.assertTrue(!SeasonalKinds.is(canyon, kind), "The desert canyon is " + kind + " to the seasons");
		}
		// Vanilla's biomes still read through the conventional tags themselves.
		helper.assertTrue(SeasonalKinds.is(biomes.getOrThrow(Biomes.SWAMP), ConventionalBiomeTags.IS_SWAMP), "A swamp is not a swamp to the seasons");
		helper.assertTrue(!SeasonalKinds.is(biomes.getOrThrow(Biomes.DESERT), ConventionalBiomeTags.IS_SWAMP), "A desert is a swamp to the seasons");
		boolean refused = false;
		try {
			SeasonalKinds.of(ConventionalBiomeTags.IS_DESERT);
		} catch (IllegalArgumentException expected) {
			refused = true;
		}
		helper.assertTrue(refused, "A conventional tag the seasons do not read has a seasonal kind");
		helper.succeed();
	}

	@GameTest
	public void newerBiomesReachTheFallsCreatures(GameTestHelper helper) {
		Registry<Biome> biomes = helper.getLevel().registryAccess().lookupOrThrow(Registries.BIOME);
		Holder<Biome> maple = landscape(biomes, "maple_woods");
		Holder<Biome> canyon = landscape(biomes, "desert_canyon");
		Holder<Biome> alpine = biomes.getOrThrow(AlpineSpawn.BIOME);
		in(helper, maple, Werewolves.HAUNTS);
		in(helper, landscape(biomes, "siberian_taiga"), Werewolves.HAUNTS);
		in(helper, landscape(biomes, "wintry_forest"), Werewolves.SNOW_HAUNTS);
		in(helper, landscape(biomes, "ominous_woods"), Werewolves.SHADOW_HAUNTS);
		in(helper, maple, Squirrels.HABITAT);
		in(helper, landscape(biomes, "pasture"), Turkeys.HABITAT);
		in(helper, alpine, Turkeys.HABITAT);
		in(helper, landscape(biomes, "bog"), HagStoneRite.LANDS);
		// No werewolves at the start, and none of these in the desert.
		helper.assertTrue(!alpine.is(Werewolves.HAUNTS), "Werewolves haunt the Alpine Spawn");
		for (TagKey<Biome> tag : List.of(Werewolves.HAUNTS, Werewolves.SNOW_HAUNTS, Werewolves.SHADOW_HAUNTS, Squirrels.HABITAT, Turkeys.HABITAT,
				HagStoneRite.LANDS)) {
			helper.assertTrue(!canyon.is(tag), "The desert canyon is in " + tag);
		}
		helper.succeed();
	}

	private static Holder<Biome> landscape(Registry<Biome> biomes, String name) {
		return biomes.getOrThrow(ResourceKey.create(Registries.BIOME, Jugcraft.id("landscape_" + name)));
	}

	private static void in(GameTestHelper helper, Holder<Biome> biome, TagKey<Biome> tag) {
		helper.assertTrue(biome.is(tag), name(biome) + " is not in " + tag);
	}

	private static String name(Holder<Biome> biome) {
		return biome.unwrapKey().map(key -> key.identifier().toString()).orElse("a biome");
	}
}

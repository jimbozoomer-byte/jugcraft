package io.github.jimbozoomer.jugcraft.test;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import io.github.jimbozoomer.jugcraft.Jugcraft;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;

/** The lairs (docs/features/witching-season.md, the lairs' shared rules). */
public class LairGameTests {
	/**
	 * Logs vanilla 26.3's own dimension types, level stems and void biome as JSON, so the lairs' dimension files follow
	 * the running game's format (there is no other copy of it in this repository).
	 */
	@GameTest
	public void vanillaDimensionFormats(GameTestHelper helper) {
		RegistryAccess access = helper.getLevel().getServer().registryAccess();
		RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, access);
		Registry<DimensionType> types = access.lookupOrThrow(Registries.DIMENSION_TYPE);
		for (ResourceKey<DimensionType> key : List.of(BuiltinDimensionTypes.OVERWORLD, BuiltinDimensionTypes.NETHER, BuiltinDimensionTypes.END)) {
			Jugcraft.LOGGER.info("[lair-probe] dimension_type {} = {}", key, DimensionType.DIRECT_CODEC.encodeStart(ops, types.getValueOrThrow(key)));
		}
		Registry<LevelStem> stems = access.lookupOrThrow(Registries.LEVEL_STEM);
		for (ResourceKey<LevelStem> key : List.of(LevelStem.OVERWORLD, LevelStem.END)) {
			stems.getOptional(key).ifPresent(stem -> Jugcraft.LOGGER.info("[lair-probe] dimension {} = {}", key, LevelStem.CODEC.encodeStart(ops, stem)));
		}
		Registry<Biome> biomes = access.lookupOrThrow(Registries.BIOME);
		for (ResourceKey<Biome> key : List.of(Biomes.THE_VOID, Biomes.THE_END)) {
			Jugcraft.LOGGER.info("[lair-probe] biome {} = {}", key, Biome.DIRECT_CODEC.encodeStart(ops, biomes.getValueOrThrow(key)));
		}
		helper.succeed();
	}
}

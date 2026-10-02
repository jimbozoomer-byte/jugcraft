package io.github.jimbozoomer.jugcraft.world;

import com.mojang.datafixers.util.Pair;
import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.config.JugcraftConfig;
import java.util.Locale;
import java.util.function.Consumer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.StructureTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * Alpine Spawn: a large, cool alpine meadow on mountain plateaus, where new worlds start. The biome is data
 * (data/jugcraft/worldgen, generated from tools/alpine.py); this class places it and moves the world spawn into it.
 *
 * <p>Placement: vanilla's cool meadows (temperature band at most {@link #COOL_MAX}) become Alpine Spawn in the
 * Overworld climate table, through {@code mixin/OverworldBiomeBuilderMixin}; temperate meadows stay meadows.
 *
 * <p>The start: on a new world's first start (game time 0) the server looks for the nearest Alpine Spawn within
 * {@link #SEARCH_RADIUS} blocks of the origin and moves the world spawn there, onto a village when one stands in the
 * biome within {@link #VILLAGE_CHUNKS} chunks. Alpine villages are common (their own tight grid, tools/alpine.py).
 * {@code alpine_spawn.enabled=false} stops new generation; {@code alpine_spawn.start=off} keeps vanilla's spawn.
 */
public final class AlpineSpawn {
	public static final String FEATURE = "alpine_spawn";
	public static final ResourceKey<Biome> BIOME = ResourceKey.create(Registries.BIOME, Jugcraft.id("alpine_spawn"));
	/** Meadows whose temperature band reaches no higher than this become Alpine Spawn (vanilla's cool band). */
	public static final float COOL_MAX = -0.15F;
	public static final int SEARCH_RADIUS = 6400;
	public static final int SEARCH_STEP = 64;
	public static final int VILLAGE_CHUNKS = 24;
	/** The start must have Alpine Spawn this far away on all four sides, so the spawn's scatter stays in the biome. */
	public static final int MARGIN = 48;
	/** How far from the biome's nearest edge to look for such a start. */
	public static final int INSIDE_SEARCH = 512;

	private AlpineSpawn() {
	}

	public static void register() {
		ServerLifecycleEvents.SERVER_STARTED.register(AlpineSpawn::moveWorldSpawn);
	}

	/** Wraps the Overworld biome builder's output so cool meadows come out as Alpine Spawn (unless switched off). */
	public static Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> wrap(Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> biomes) {
		if (!JugcraftConfig.isFeatureEnabled(FEATURE)) {
			return biomes;
		}
		return entry -> biomes.accept(replaces(entry) ? Pair.of(entry.getFirst(), BIOME) : entry);
	}

	/** Whether a climate entry is a cool meadow, which Alpine Spawn takes over. */
	public static boolean replaces(Pair<Climate.ParameterPoint, ResourceKey<Biome>> entry) {
		return entry.getSecond().equals(Biomes.MEADOW) && entry.getFirst().temperature().max() <= Climate.quantizeCoord(COOL_MAX);
	}

	private static void moveWorldSpawn(MinecraftServer server) {
		ServerLevel level = server.overworld();
		if (!JugcraftConfig.isFeatureEnabled(FEATURE) || !JugcraftConfig.textOption("alpine_spawn.start").trim().equalsIgnoreCase("on")
				|| level.getGameTime() != 0L || !(level.getChunkSource().getGenerator().getBiomeSource() instanceof MultiNoiseBiomeSource)) {
			return;
		}
		BlockPos start = findStart(level);
		if (start == null) {
			Jugcraft.LOGGER.info("Alpine Spawn: none within {} blocks of the origin; the world spawn stays vanilla's", SEARCH_RADIUS);
			return;
		}
		server.getCommands().performPrefixedCommand(server.createCommandSourceStack().withSuppressedOutput(),
				String.format(Locale.ROOT, "setworldspawn %d %d %d", start.getX(), start.getY(), start.getZ()));
		Jugcraft.LOGGER.info("Alpine Spawn: the world starts at {} {} {}", start.getX(), start.getY(), start.getZ());
	}

	/**
	 * Where a new world should start: on the surface at the nearest alpine village, or the nearest Alpine Spawn if no
	 * village stands in the biome near it; null if there is no Alpine Spawn within {@link #SEARCH_RADIUS} blocks.
	 */
	public static BlockPos findStart(ServerLevel level) {
		Pair<BlockPos, Holder<Biome>> found = level.findClosestBiome3d(biome -> biome.is(BIOME), new BlockPos(0, 96, 0),
				SEARCH_RADIUS, SEARCH_STEP, 64);
		if (found == null) {
			return null;
		}
		BlockPos target = inside(level, found.getFirst());
		BlockPos village = level.findNearestMapStructure(StructureTags.VILLAGE, target, VILLAGE_CHUNKS, false);
		if (village != null && level.getBiome(village).is(BIOME)) {
			Jugcraft.LOGGER.info("Alpine Spawn: a village stands at {} {}", village.getX(), village.getZ());
			target = village;
		}
		level.getChunk(target.getX() >> 4, target.getZ() >> 4);
		int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, target.getX(), target.getZ());
		return new BlockPos(target.getX(), y, target.getZ());
	}

	/**
	 * The nearest point to {@code edge} (within {@link #INSIDE_SEARCH} blocks, in steps of 16) that has Alpine Spawn
	 * under it and {@link #MARGIN} blocks away on all four sides; {@code edge} itself if there is none. Biomes are read
	 * from the generator, so no chunk is generated.
	 */
	private static BlockPos inside(ServerLevel level, BlockPos edge) {
		for (int radius = 0; radius <= INSIDE_SEARCH; radius += 16) {
			for (int dx = -radius; dx <= radius; dx += 16) {
				for (int dz = -radius; dz <= radius; dz += 16) {
					if (Math.max(Math.abs(dx), Math.abs(dz)) != radius) {
						continue;
					}
					BlockPos pos = edge.offset(dx, 0, dz);
					if (alpine(level, pos) && alpine(level, pos.east(MARGIN)) && alpine(level, pos.west(MARGIN))
							&& alpine(level, pos.north(MARGIN)) && alpine(level, pos.south(MARGIN))) {
						return pos;
					}
				}
			}
		}
		return edge;
	}

	private static boolean alpine(ServerLevel level, BlockPos pos) {
		return level.getBiome(pos).is(BIOME);
	}
}

package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.agriculture.JugcraftAgriculture;
import io.github.jimbozoomer.jugcraft.agriculture.SeasonalLeavesBlock;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;

/** Natural biome stops, repeated shapes, water depths, seasonal crowns and complete timber samples. */
public class TreeBatchFourBCClientGameTests extends TreeBatchTwoClientGameTests {
	@Override protected List<String> shapes() { return TreeBatchFourBCGameTests.SHAPES; }
	@Override protected List<String> biomes() { return List.of("bayou", "lush_swamp", "swamp_woods", "webwood", "flower_isle"); }
	@Override protected String worldName() { return "Cypress and Magnolia Inspection"; }
	@Override protected String exportName() { return "tree-batch-4bc-preview"; }
	@Override protected String screenshotPrefix() { return "jugcraft_tree4bc_"; }
	@Override protected boolean includeSubmergedColumns() { return true; }

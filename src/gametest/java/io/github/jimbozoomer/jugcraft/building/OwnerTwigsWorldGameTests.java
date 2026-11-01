package io.github.jimbozoomer.jugcraft.building;

import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.material.Fluids;

/** Exercise the actual feature and native fluid API, not a same-named test property. */
public final class OwnerTwigsWorldGameTests {
    private record Patch(String kind, String block, int shellRoll) { }
    private static final List<Patch> PATCHES = List.of(
            new Patch("patch_twig", "twig", 0),
            new Patch("patch_pebble", "pebble", 0),
            new Patch("patch_seashell", "opaline_seashell", 0),
            new Patch("patch_seashell", "tangerine_seashell", 20),
            new Patch("patch_seashell", "roseate_seashell", 40),
            new Patch("patch_seashell", "bronzed_seashell", 55));

    private static boolean place(GameTestHelper h, BlockPos pos, Patch patch) {
        var random = new LegacyRandomSource(0) {
            @Override public int nextInt(int bound) { return bound == 63 ? patch.shellRoll() : 0; }
        };
        return new OwnerTwigsWorld.Natural(patch.kind()).place(h.getLevel(), h.getLevel().getChunkSource().getGenerator(), random, pos);
    }

    @GameTest(maxTicks = 20)
    public void naturalTwigsPebblesAndEveryShellPreserveDryAndWetPlacement(GameTestHelper h) {
        var level = h.getLevel();
        var pos = h.absolutePos(new BlockPos(1, 2, 1));
        level.setBlock(pos.below(), Blocks.STONE.defaultBlockState(), 2);
        for (var patch : PATCHES) for (boolean water : new boolean[]{false, true}) {
            level.setBlock(pos, (water ? Blocks.WATER : Blocks.AIR).defaultBlockState(), 2);
            h.assertTrue(place(h, pos, patch), "Natural patch places " + patch.block() + "; water=" + water);
            var state = level.getBlockState(pos);
            h.assertTrue(state.is(OwnerTwigs.block(patch.block())), "Selected patch keeps its intended block");
            h.assertTrue(state.hasProperty(BlockStateProperties.WATERLOGGED), "Registered decor uses the native waterlogging property");
            h.assertTrue(state.getValue(BlockStateProperties.WATERLOGGED) == water, "Natural patch preserves dry/source-water state");
            h.assertTrue(state.getFluidState().is(Fluids.WATER) == water, "Actual fluid contents agree with the placed state");
        }
        h.succeed();
    }

    @GameTest(maxTicks = 20)
    public void naturalPatchesKeepOccupiedAndUnsupportedPositionsUnchanged(GameTestHelper h) {
        var level = h.getLevel();
        var pos = h.absolutePos(new BlockPos(1, 2, 1));
        for (var patch : PATCHES) {
            level.setBlock(pos.below(), Blocks.STONE.defaultBlockState(), 2);
            level.setBlock(pos, Blocks.COBBLESTONE.defaultBlockState(), 2);
            h.assertTrue(!place(h, pos, patch) && level.getBlockState(pos).is(Blocks.COBBLESTONE), "Patch does not replace an occupied block");
            for (var support : List.of(Blocks.AIR, Blocks.OAK_LEAVES)) for (var old : List.of(Blocks.AIR, Blocks.WATER)) {
                level.setBlock(pos.below(), support.defaultBlockState(), 2);
                level.setBlock(pos, old.defaultBlockState(), 2);
                h.assertTrue(!place(h, pos, patch), "Patch requires valid floor support: " + patch.block());
                h.assertTrue(level.getBlockState(pos).is(old), "Rejected patch preserves existing air or water");
            }
        }
        h.succeed();
    }

    @GameTest(maxTicks = 20)
    public void generatedDecorSupportsNativeWaterPickupAndRefill(GameTestHelper h) {
        var level = h.getLevel();
        var pos = h.absolutePos(new BlockPos(1, 2, 1));
        level.setBlock(pos.below(), Blocks.STONE.defaultBlockState(), 2);
        for (var patch : PATCHES) {
            level.setBlock(pos, Blocks.WATER.defaultBlockState(), 2);
            h.assertTrue(place(h, pos, patch), "Wet natural patch places before fluid interaction");
            var block = (SimpleWaterloggedBlock)OwnerTwigs.block(patch.block());
            var bucket = block.pickupBlock(null, level, pos, level.getBlockState(pos));
            h.assertTrue(bucket.is(Items.WATER_BUCKET), "Native pickup retrieves the contained source water");
            h.assertTrue(level.getBlockState(pos).is(OwnerTwigs.block(patch.block())) && level.getFluidState(pos).isEmpty(), "Pickup keeps the decor and dries it");
            h.assertTrue(block.pickupBlock(null, level, pos, level.getBlockState(pos)).isEmpty(), "Dry decor cannot duplicate water");
            h.assertTrue(block.placeLiquid(level, pos, level.getBlockState(pos), Fluids.WATER.getSource(false)), "Native refill restores water");
            h.assertTrue(level.getFluidState(pos).is(Fluids.WATER), "Refill restores actual fluid contents");
            h.assertTrue(!block.placeLiquid(level, pos, level.getBlockState(pos), Fluids.WATER.getSource(false)), "Already full decor refuses another source");
        }
        h.succeed();
    }
}

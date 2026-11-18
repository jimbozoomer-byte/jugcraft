package io.github.jimbozoomer.jugcraft.agriculture;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import java.util.Map;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/** Full-size lamps with one protected footprint, a solid foot, and open pavement under the hanging lantern. */
public abstract class PeppermintLamps extends MultiDecorationBlock {
    public static final String LANE = "peppermint_lane_lamp";
    public static final String BOULEVARD = "peppermint_boulevard_lamp";

    private PeppermintLamps(Properties properties) { super(properties); }

    public static void register() {
        for (String name : new String[] {LANE, BOULEVARD}) {
            var blockKey = ResourceKey.create(Registries.BLOCK, Jugcraft.id(name));
            var properties = Properties.ofFullCopy(Blocks.LANTERN).noOcclusion().strength(1.5F)
                    .pushReaction(PushReaction.IMMOVEABLE).lightLevel(PeppermintLamps::light).setId(blockKey);
            var block = Registry.register(BuiltInRegistries.BLOCK, blockKey,
                    name.equals(LANE) ? new Lane(properties) : new Boulevard(properties));
            var itemKey = ResourceKey.create(Registries.ITEM, blockKey.identifier());
            var item = Registry.register(BuiltInRegistries.ITEM, itemKey,
                    new BlockItem(block, new Item.Properties().setId(itemKey).useBlockDescriptionPrefix()));
            CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(output -> output.accept(item));
        }
        // Removing the footing drops the whole lamp, including a lantern which may reach into the next claim.
        PlayerBlockBreakEvents.BEFORE.register((level, player, pos, state, be) -> {
            var above = level.getBlockState(pos.above());
            return !(above.getBlock() instanceof PeppermintLamps lamp) || lamp.part(above) != MASTER
                    || lamp.mayChangeWhole(level, pos.above(), above.getValue(FACING), player);
        });
    }

    private static int light(BlockState state) {
        var lamp = (PeppermintLamps) state.getBlock();
        // The glass is in the lower of the two overhanging cells on both models.
        return state.getValue(LIT) && lamp.part(state) == lamp.cells().length - 2 ? 15 : 0;
    }

    @Override public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        var level = context.getLevel();
        var foot = context.getClickedPos();
        var facing = context.getHorizontalDirection().getOpposite();
        var base = defaultBlockState().setValue(FACING, facing);
        if (!base.canSurvive(level, foot)) return null;
        for (int i = 0; i < cells().length; i++) {
            BlockPos pos = partPos(foot, facing, i);
            if (!level.hasChunkAt(pos) || level.isOutsideBuildHeight(pos) || !level.getWorldBorder().isWithinBounds(pos)
                    || !level.getFluidState(pos).isEmpty() || !level.getBlockState(pos).canBeReplaced(BlockPlaceContext.at(context, pos, Direction.UP))
                    || !level.isUnobstructed(base.setValue(partProperty(), i), pos, CollisionContext.empty())
                    || context.getPlayer() != null && !context.getPlayer().mayUseItemAt(pos, Direction.UP, context.getItemInHand())) return null;
        }
        return super.getStateForPlacement(context);
    }

    @Override protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return part(state) != MASTER || level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP);
    }

    @Override protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos,
            Direction direction, BlockPos neighborPos, BlockState neighbor, RandomSource random) {
        if (direction == Direction.DOWN && !state.canSurvive(level, pos)) return Blocks.AIR.defaultBlockState();
        return super.updateShape(state, level, ticks, pos, direction, neighborPos, neighbor, random);
    }

    private static final class Lane extends PeppermintLamps {
        private static final int[][] CELLS = {{0,0}, {0,1}, {0,2}, {1,1}, {1,2}};
        private static final IntegerProperty PART = IntegerProperty.create("part", 0, CELLS.length - 1);
        private static final Map<Direction,VoxelShape[]> SHAPES = shapesOf(CELLS,
                new double[]{19,0,3,29,7,13}, new double[]{21,7,5,27,39,11},
                new double[]{18,36,5,25,45,11}, new double[]{12,41,5,21,47,11},
                new double[]{7,37,5,14,45,11}, new double[]{6,33,6,11,40,10},
                new double[]{3,20,3,13,33,13});
        private Lane(Properties properties) { super(properties); }
        @Override public int[][] cells() { return CELLS; }
        @Override public IntegerProperty partProperty() { return PART; }
        @Override protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
            return SHAPES.get(state.getValue(FACING))[part(state)];
        }
    }

    private static final class Boulevard extends PeppermintLamps {
        private static final int[][] CELLS = {{0,0}, {0,1}, {0,2}, {0,3}, {0,4}, {1,3}, {1,4}};
        private static final IntegerProperty PART = IntegerProperty.create("part", 0, CELLS.length - 1);
        private static final Map<Direction,VoxelShape[]> SHAPES = shapesOf(CELLS,
                new double[]{17,0,1,31,12,15}, new double[]{20,12,4,28,71,12},
                new double[]{18,46,2,30,57,14}, new double[]{18,68,4,26,77,12},
                new double[]{11,73,4,22,80,12}, new double[]{5,68,4,14,77,12},
                new double[]{5,63,5,11,72,11}, new double[]{2,48,2,14,64,14});
        private Boulevard(Properties properties) { super(properties); }
        @Override public int[][] cells() { return CELLS; }
        @Override public IntegerProperty partProperty() { return PART; }
        @Override protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
            return SHAPES.get(state.getValue(FACING))[part(state)];
        }
    }
}


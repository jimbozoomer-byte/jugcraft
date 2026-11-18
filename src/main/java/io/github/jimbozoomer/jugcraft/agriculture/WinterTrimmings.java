package io.github.jimbozoomer.jugcraft.agriculture;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public final class WinterTrimmings {
    // Two pixels per square. The asset generator draws the model from these same heights.
    private static final String[] DRIFT = {
        "00000000", "01100000", "12211000", "23432110",
        "24554321", "12345532", "00122321", "00000110"
    };
    private static final String[] PILE = {
        "00110000", "01232100", "13565411", "24798632",
        "23688742", "12456631", "01123310", "00011000"
    };

    private WinterTrimmings() {}

    public static void register() {
        register("wind_carved_snowdrift", Blocks.SNOW_BLOCK, p -> new Snowbank(p, DRIFT));
        register("shovelled_snow_pile", Blocks.SNOW_BLOCK, p -> new Snowbank(p, PILE));
        register("eave_icicle_fringe", Blocks.PACKED_ICE,
                p -> new HarvestTownHangingBlock(p.noCollision(), new double[]{0, 1, 3, 16, 16, 10}));
    }

    private static void register(String path, Block material, Function<BlockBehaviour.Properties, Block> factory) {
        var id = Jugcraft.id(path);
        var blockKey = ResourceKey.create(Registries.BLOCK, id);
        var props = BlockBehaviour.Properties.ofFullCopy(material).strength(.25F).noOcclusion()
                .pushReaction(PushReaction.POPPED).setId(blockKey);
        var block = Registry.register(BuiltInRegistries.BLOCK, blockKey, factory.apply(props));
        var itemKey = ResourceKey.create(Registries.ITEM, id);
        var item = Registry.register(BuiltInRegistries.ITEM, itemKey,
                new BlockItem(block, new Item.Properties().setId(itemKey).useBlockDescriptionPrefix()));
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.NATURAL_BLOCKS).register(out -> out.accept(item));
    }

    private static final class Snowbank extends FacingDecorationBlock {
        private final Map<Direction, VoxelShape> surface = new EnumMap<>(Direction.class);

        Snowbank(Properties properties, String[] heights) {
            super(properties, new double[]{0, 0, 0, 16, 1, 16});
            var strips = new ArrayList<double[]>();
            for (int z = 0; z < heights.length; z++) {
                String row = heights[z];
                for (int x = 0; x < row.length();) {
                    int end = x + 1;
                    while (end < row.length() && row.charAt(x) == row.charAt(end)) end++;
                    int height = row.charAt(x) - '0';
                    if (height > 0) strips.add(new double[]{2*x, 0, 2*z, 2*end, height, 2*z+2});
                    x = end;
                }
            }
            var boxes = strips.toArray(double[][]::new);
            for (Direction facing : Direction.Plane.HORIZONTAL) surface.put(facing, LongDecorationBlock.shape(boxes, facing));
        }

        @Override protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
            return surface.get(state.getValue(FACING));
        }

        @Override protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
            return Block.canSupportCenter(level, pos.below(), Direction.UP);
        }

        @Override public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
            BlockState state = super.getStateForPlacement(context);
            return state != null && state.canSurvive(context.getLevel(), context.getClickedPos()) ? state : null;
        }

        @Override protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos,
                Direction direction, BlockPos neighborPos, BlockState neighbor, RandomSource random) {
            if (direction == Direction.DOWN && !state.canSurvive(level, pos)) return Blocks.AIR.defaultBlockState();
            return super.updateShape(state, level, ticks, pos, direction, neighborPos, neighbor, random);
        }
    }
}

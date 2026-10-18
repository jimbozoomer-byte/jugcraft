package io.github.jimbozoomer.jugcraft.agriculture;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/** One capped tap per rooted rubber trunk; one minute per fill step, no block entity or catch-up. */
public final class TreeTapBlock extends Block {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final IntegerProperty LATEX = IntegerProperty.create("latex", 0, 4);
    public static final int INTERVAL = 1200;
    public TreeTapBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH).setValue(LATEX, 0));
    }
    @Override protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case NORTH -> box(4, 1, 8, 12, 12, 16);
            case SOUTH -> box(4, 1, 0, 12, 12, 8);
            case WEST -> box(8, 1, 4, 16, 12, 12);
            default -> box(0, 1, 4, 8, 12, 12);
        };
    }
    private static boolean livingLog(BlockState state) {
        return state.getBlock() instanceof RubberLogBlock && state.getValue(RubberLogBlock.NATURAL)
                && state.getValue(RotatedPillarBlock.AXIS) == Direction.Axis.Y;
    }
    public static boolean productive(LevelReader level, BlockPos pos, Direction facing) {
        BlockPos trunk = pos.relative(facing.getOpposite());
        if (!livingLog(level.getBlockState(trunk))) return false;
        BlockPos base = trunk;
        for (int n = 0; n < 16 && livingLog(level.getBlockState(base.below())); n++) base = base.below();
        if (trunk.getY() - base.getY() > 2 || !level.getBlockState(base.below()).is(BlockTags.DIRT)) return false;
        BlockPos top = base;
        for (int n = 0; n < 16 && livingLog(level.getBlockState(top.above())); n++) top = top.above();
        if (top.getY() - base.getY() < 7) return false;
        // At most 12 tap positions; duplicate taps cannot multiply one tree's production.
        for (int y = 0; y <= 2; y++) for (Direction side : Direction.Plane.HORIZONTAL) {
            BlockPos other = base.above(y).relative(side);
            BlockState tap = level.getBlockState(other);
            if (!other.equals(pos) && tap.getBlock() instanceof TreeTapBlock && tap.getValue(FACING) == side) return false;
        }
        int leaves = 0;
        for (BlockPos at : BlockPos.betweenClosed(top.offset(-2, -2, -2), top.offset(2, 2, 2))) {
            BlockState leaf = level.getBlockState(at);
            if (leaf.is(JugcraftAgriculture.block("rubber_leaves")) && !leaf.getValue(LeavesBlock.PERSISTENT)
                    && leaf.getValue(LeavesBlock.DISTANCE) < 7) leaves++;
        }
        return leaves >= 8;
    }
    @Override public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction side = context.getClickedFace();
        if (!side.getAxis().isHorizontal()) return null;
        return productive(context.getLevel(), context.getClickedPos(), side) ? defaultBlockState().setValue(FACING, side) : null;
    }
    @Override protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return livingLog(level.getBlockState(pos.relative(state.getValue(FACING).getOpposite())));
    }
    @Override protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks,
            BlockPos pos, Direction direction, BlockPos other, BlockState neighbor, RandomSource random) {
        if (!state.canSurvive(level, pos)) return Blocks.AIR.defaultBlockState();
        return super.updateShape(state, level, ticks, pos, direction, other, neighbor, random);
    }
    @Override protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState old, boolean moved) {
        if (!level.isClientSide() && !old.is(this)) level.scheduleTick(pos, this, INTERVAL);
    }
    @Override public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        BlockState current = level.getBlockState(pos);
        if (!current.is(this)) return;
        if (current.getValue(LATEX) < 4 && productive(level, pos, current.getValue(FACING))) {
            current = current.setValue(LATEX, current.getValue(LATEX) + 1);
            level.setBlock(pos, current, Block.UPDATE_CLIENTS);
        }
        if (current.getValue(LATEX) < 4) level.scheduleTick(pos, this, INTERVAL);
    }
    @Override protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        BlockState current = level.getBlockState(pos);
        if (!stack.is(Items.GLASS_BOTTLE) || !current.is(this) || current.getValue(LATEX) != 4) return InteractionResult.PASS;
        if (!level.isClientSide()) {
            level.setBlock(pos, current.setValue(LATEX, 0), Block.UPDATE_CLIENTS);
            stack.consume(1, player);
            var bottle = new ItemStack(JugcraftAgriculture.item("latex_bottle"));
            if (!player.getInventory().add(bottle)) Block.popResource(level, pos, bottle);
            level.scheduleTick(pos, this, INTERVAL);
        }
        return InteractionResult.SUCCESS;
    }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(FACING, LATEX); }
}

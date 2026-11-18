package io.github.jimbozoomer.jugcraft.town;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/** Three lengths of brass tube. Each stand remembers its pitch for redstone performances. */
public final class GalleryChimesBlock extends Block {
    public static final IntegerProperty TONE = IntegerProperty.create("tone", 0, 2);
    private static final float[] PITCHES = { 1.0F, 1.2599F, 1.4983F };
    private static final String[] NOTES = { "C", "E", "G" };
    private static final VoxelShape WIDE = Block.box(1, 0, 4, 15, 15, 12);
    private static final VoxelShape DEEP = Block.box(4, 0, 1, 12, 15, 15);

    GalleryChimesBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH)
                .setValue(TONE, 0).setValue(BlockStateProperties.POWERED, false));
    }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.HORIZONTAL_FACING, TONE, BlockStateProperties.POWERED);
    }
    @Override public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, context.getHorizontalDirection().getOpposite())
                .setValue(BlockStateProperties.POWERED, context.getLevel().hasNeighborSignal(context.getClickedPos()));
    }
    @Override protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos at, CollisionContext context) {
        return state.getValue(BlockStateProperties.HORIZONTAL_FACING).getAxis() == Direction.Axis.Z ? WIDE : DEEP;
    }
    @Override protected void neighborChanged(BlockState state, Level level, BlockPos at, Block source, @Nullable Orientation orientation, boolean moved) {
        if (!(level instanceof ServerLevel server)) return;
        boolean powered = level.hasNeighborSignal(at);
        if (powered != state.getValue(BlockStateProperties.POWERED)) {
            level.setBlock(at, state.setValue(BlockStateProperties.POWERED, powered), Block.UPDATE_CLIENTS);
            if (powered) strike(server, at, state.getValue(TONE));
        }
    }
    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos at, Player player, BlockHitResult hit) {
        if (level instanceof ServerLevel server) {
            if (player.isShiftKeyDown()) {
                state = state.cycle(TONE);
                level.setBlock(at, state, Block.UPDATE_CLIENTS);
                player.sendOverlayMessage(Component.translatable("message.jugcraft.gallery_chimes.tune", NOTES[state.getValue(TONE)]));
            }
            strike(server, at, state.getValue(TONE));
        }
        return InteractionResult.SUCCESS;
    }
    private static void strike(ServerLevel level, BlockPos at, int tone) {
        level.playSound(null, at, SoundEvents.NOTE_BLOCK_CHIME.value(), SoundSource.BLOCKS, 1.1F, PITCHES[tone]);
        level.sendParticles(ParticleTypes.NOTE, at.getX() + 0.5, at.getY() + 1.1, at.getZ() + 0.5, 1, 0.0, 0.0, 0.0, 0.0);
    }
}

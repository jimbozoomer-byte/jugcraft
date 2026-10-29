package io.github.jimbozoomer.jugcraft.agriculture;

import io.github.jimbozoomer.jugcraft.energy.EnergyConnectable;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/**
 * The Animatronic Show Controller (Clockwork Animatronics, part 6; tools/clockwork.py SHOW_CONTROLLER): a riveted
 * gunmetal steel control cabinet on the JE network that drives every clockwork figure near it in time, without shafts
 * ({@link ShowControllerBlockEntity}). Use it with an empty hand to throw its knife switch ({@link #ENABLED}). While its
 * show runs ({@link #RUNNING}) its beacon is lit and it gives light {@value #LIGHT}; {@link #CAST} is how many of its
 * lamps are lit, one for each figure cued, up to {@value #LAMPS}. Its model is tools/clockwork_data.py
 * show_controller(), its moving parts drawn by client/ShowControllerRenderer. Cables connect to it.
 */
public class ShowControllerBlock extends BaseEntityBlock implements EnergyConnectable {
	public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
	public static final BooleanProperty ENABLED = BooleanProperty.create("enabled");
	public static final BooleanProperty RUNNING = BooleanProperty.create("running");
	/** Its amber lamps. */
	public static final int LAMPS = 6;
	public static final IntegerProperty CAST = IntegerProperty.create("cast", 0, LAMPS);
	public static final int LIGHT = 4;
	/** Ticks its programme drum takes to turn once round while the show runs, and its show gauge's needle to swing. */
	public static final int DRUM_TICKS = 160;
	public static final int SWEEP_TICKS = 40;
	/** The cabinet, 87 cm wide and 50 cm deep (by the way its front faces). */
	private static final Map<Direction, VoxelShape> SHAPES = Map.of(
			Direction.NORTH, Block.box(1.0, 0.0, 4.0, 15.0, 16.0, 12.0), Direction.SOUTH, Block.box(1.0, 0.0, 4.0, 15.0, 16.0, 12.0),
			Direction.EAST, Block.box(4.0, 0.0, 1.0, 12.0, 16.0, 15.0), Direction.WEST, Block.box(4.0, 0.0, 1.0, 12.0, 16.0, 15.0));

	public ShowControllerBlock(Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(ENABLED, false).setValue(RUNNING, false)
				.setValue(CAST, 0));
	}

	public static int light(BlockState state) {
		return state.getValue(RUNNING) ? LIGHT : 0;
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPES.get(state.getValue(FACING));
	}

	@Override
	public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
		return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
	}

	/** Throws its knife switch: the show on or off. */
	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (!(level instanceof ServerLevel)) {
			return InteractionResult.SUCCESS;
		}
		boolean on = !state.getValue(ENABLED);
		level.setBlock(pos, state.setValue(ENABLED, on), Block.UPDATE_ALL);
		level.playSound(null, pos, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.6F, on ? 0.9F : 0.7F);
		player.sendOverlayMessage(Component.translatable(on ? "message.jugcraft.show_controller.on" : "message.jugcraft.show_controller.off"));
		return InteractionResult.SUCCESS;
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new ShowControllerBlockEntity(pos, state);
	}

	@Override
	public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		if (level.isClientSide() || type != JugcraftAgriculture.SHOW_CONTROLLER_ENTITY) {
			return null;
		}
		return (tickLevel, pos, tickState, entity) -> ((ShowControllerBlockEntity) entity).serverTick((ServerLevel) tickLevel, pos, tickState);
	}

	@Override
	protected BlockState rotate(BlockState state, Rotation rotation) {
		return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
	}

	@Override
	protected BlockState mirror(BlockState state, Mirror mirror) {
		return state.rotate(mirror.getRotation(state.getValue(FACING)));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, ENABLED, RUNNING, CAST);
	}
}

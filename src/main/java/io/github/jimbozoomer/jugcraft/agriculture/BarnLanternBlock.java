package io.github.jimbozoomer.jugcraft.agriculture;

import java.util.EnumMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * The Barn Lantern (Barn & Harvest Tools, part 5; tools/barn_tools.py LANTERN): a hurricane lantern hung by its bail
 * from an iron bracket on a wall ({@link WallDecorationBlock}: it comes off when the wall goes). Placed lit
 * ({@link #LIT}, light {@value #LIGHT}); use it with an empty hand to put it out or light it again. Its model is
 * tools/barn_tools_data.py lantern(), the flame drawn only while it is lit.
 */
public class BarnLanternBlock extends WallDecorationBlock {
	public static final int LIGHT = 15;
	public static final BooleanProperty LIT = BlockStateProperties.LIT;
	/** The plate, the bracket's arm and the lantern on its hook, facing north (the wall behind, to the south). */
	private static final double[][] BOXES = {{6.5, 11.0, 15.25, 9.5, 15.0, 16.0}, {7.5, 13.5, 7.5, 8.5, 14.5, 15.25}, {6.25, 5.5, 6.25, 9.75, 13.5, 9.75}};
	private static final Map<Direction, VoxelShape> SHAPES = new EnumMap<>(Direction.class);

	static {
		for (Direction facing : Direction.Plane.HORIZONTAL) {
			SHAPES.put(facing, LongDecorationBlock.shape(BOXES, facing));
		}
	}

	public BarnLanternBlock(Properties properties) {
		super(properties, 10.0, 5.5, 15.0);
		registerDefaultState(defaultBlockState().setValue(LIT, true));
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPES.get(state.getValue(FACING));
	}

	/** Only an empty hand lights it or puts it out; anything else does what it would on any block. */
	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
			BlockHitResult hit) {
		return stack.isEmpty() ? InteractionResult.TRY_WITH_EMPTY_HAND : InteractionResult.PASS;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (!level.isClientSide()) {
			boolean lit = !state.getValue(LIT);
			level.setBlock(pos, state.setValue(LIT, lit), Block.UPDATE_ALL);
			level.playSound(null, pos, lit ? SoundEvents.FLINTANDSTEEL_USE : SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.6F, 1.0F);
			level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(LIT);
	}
}

package io.github.jimbozoomer.jugcraft.agriculture;

import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/**
 * A market display of the Pumpkin Patch Market (decor set 1, part 2; tools/pumpkin_patch.py DISPLAYS): a real-life-sized
 * prop of several blocks, placed and broken as one ({@link MultiDecorationBlock}), that shows the produce put on it. Use a
 * piece of produce ({@link PumpkinCrateBlock#PRODUCE}, the Pumpkin Crate's) on any of its blocks to set it on the next free
 * spot ({@link #spots}); use it with an empty hand to take the last one back. Its first block keeps what it holds
 * ({@link ProduceDisplayBlockEntity}); breaking it spills them. It has no lanterns ({@link #LIT} stays on).
 */
public abstract class ProduceDisplayBlock extends MultiDecorationBlock implements EntityBlock {
	/** How far each piece is turned about the vertical, by its spot's index, round and round (tools/pumpkin_patch.py TURNS). */
	public static final float[] TURNS = {8.0F, -12.0F, 20.0F, -4.0F, 14.0F, -18.0F, 2.0F};

	protected ProduceDisplayBlock(Properties properties) {
		super(properties);
	}

	/** Where its pieces stand, in pixels in its frame (tools/pumpkin_patch.py DISPLAYS), filled in this order. */
	public abstract float[][] spots();

	/** How big each piece is drawn, in blocks across. */
	public abstract float scale();

	/** Each part's shape for each facing. */
	protected abstract Map<Direction, VoxelShape[]> shapes();

	/** The part shapes of a prop made of several boxes {x0, y0, z0, x1, y1, z1} in its frame, each part's pieces joined. */
	protected static Map<Direction, VoxelShape[]> shapesOf(int[][] cells, double[]... boxes) {
		Map<Direction, VoxelShape[]> out = partShapes(boxes[0], cells);
		for (int i = 1; i < boxes.length; i++) {
			Map<Direction, VoxelShape[]> more = partShapes(boxes[i], cells);
			for (Map.Entry<Direction, VoxelShape[]> entry : out.entrySet()) {
				VoxelShape[] shapes = entry.getValue();
				for (int part = 0; part < shapes.length; part++) {
					shapes[part] = Shapes.or(shapes[part], more.get(entry.getKey())[part]);
				}
			}
		}
		return out;
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return shapes().get(state.getValue(FACING))[part(state)];
	}

	/** The display's records, on its first block, from any of its blocks. */
	public @Nullable ProduceDisplayBlockEntity display(Level level, BlockPos pos, BlockState state) {
		return level.getBlockEntity(masterPos(pos, state)) instanceof ProduceDisplayBlockEntity display ? display : null;
	}

	/** Sets a piece of produce on the next free spot. */
	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
			BlockHitResult hit) {
		if (!stack.is(PumpkinCrateBlock.PRODUCE)) {
			// Only an empty hand goes on to take a piece back; anything else does what it would on any block.
			return stack.isEmpty() ? InteractionResult.TRY_WITH_EMPTY_HAND : InteractionResult.PASS;
		}
		ProduceDisplayBlockEntity display = display(level, pos, state);
		if (display == null) {
			return InteractionResult.PASS;
		}
		if (display.isFull()) {
			// (not PASS or FAIL: either would set the pumpkin down on the display)
			if (!level.isClientSide()) {
				player.sendOverlayMessage(Component.translatable("message.jugcraft.produce_display.full"));
			}
			return InteractionResult.CONSUME;
		}
		if (!level.isClientSide()) {
			display.add(stack.split(1));
			level.playSound(null, pos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 0.8F, 0.9F);
			level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
		}
		return InteractionResult.SUCCESS;
	}

	/** Takes the last piece back (it has no lanterns to light). */
	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		ProduceDisplayBlockEntity display = display(level, pos, state);
		if (display == null || display.isEmpty()) {
			return InteractionResult.PASS;
		}
		if (!level.isClientSide()) {
			ItemStack taken = display.takeLast();
			if (!player.getInventory().add(taken)) {
				Block.popResource(level, pos.above(), taken);
			}
			level.playSound(null, pos, SoundEvents.WOOD_HIT, SoundSource.BLOCKS, 0.8F, 1.1F);
			level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return part(state) == MASTER ? new ProduceDisplayBlockEntity(pos, state) : null;
	}
}

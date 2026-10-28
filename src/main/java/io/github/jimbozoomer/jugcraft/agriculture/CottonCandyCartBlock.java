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
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/**
 * The Cotton-Candy Cart (Haunted Midway, part 1; tools/haunted_midway.py COTTON_CANDY): a violet lacquered cart on two
 * spoked wheels, one block across and two up ({@link MultiDecorationBlock}), a steel spinning bowl on its counter and a
 * striped canopy above. It is placed switched off ({@link #LIT} false); an empty hand switches its motor on or off.
 * While it runs, its spinner head spins (client/CottonCandyCartRenderer, from the blockstate's {@link #RUNNER} model,
 * which a placed cart never takes) and its heater band glows. Sugar used on it while it runs is spun into floss on the
 * bowl's wall ({@link #FLOSS_LEVEL}, on every block), one serving a sugar, up to {@value #FLOSS}; Paper winds a serving
 * onto a cone, a Cotton Candy. Broken, it gives back a sugar for each serving left (its loot table). Its model is
 * tools/haunted_midway_data.py cotton_candy_cart().
 */
public class CottonCandyCartBlock extends MultiDecorationBlock implements EntityBlock {
	public static final int FLOSS = 3;
	/** How fast its spinner head turns while it runs, in degrees a tick, about the bowl's middle (pixels, x and z). */
	public static final float SPIN = 18.0F;
	public static final float BOWL_X = 8.0F;
	public static final float BOWL_Z = 8.25F;
	/** Its blocks: 1 across, 2 up, 1 deep (tools/haunted_midway.py COTTON_CANDY["size"]). */
	private static final int[][] CELLS = box(1, 2, 1);
	public static final IntegerProperty PART = IntegerProperty.create("part", 0, CELLS.length - 1);
	public static final IntegerProperty FLOSS_LEVEL = IntegerProperty.create("floss", 0, FLOSS);
	public static final BooleanProperty RUNNER = BooleanProperty.create("runner");
	/** The cabinet on its wheels, the bowl, the two posts and the canopy. */
	private static final Map<Direction, VoxelShape[]> SHAPES = shapesOf(CELLS, new double[] {0.5, 0.0, 3.5, 15.5, 12.75, 15.25},
			new double[] {3.75, 12.75, 4.0, 12.25, 15.5, 12.5}, new double[] {2.5, 12.75, 12.5, 3.25, 25.5, 13.25},
			new double[] {12.75, 12.75, 12.5, 13.5, 25.5, 13.25}, new double[] {0.5, 25.5, 0.5, 15.5, 31.0, 15.5});

	public CottonCandyCartBlock(Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState().setValue(LIT, false).setValue(FLOSS_LEVEL, 0).setValue(RUNNER, false));
	}

	@Override
	public int[][] cells() {
		return CELLS;
	}

	@Override
	public IntegerProperty partProperty() {
		return PART;
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPES.get(state.getValue(FACING))[part(state)];
	}

	/** Where its spinner head has turned to at {@code time} (ticks, with the partial tick), in degrees: still while off. */
	public static float spin(boolean running, float time) {
		return running ? (time * SPIN) % 360.0F : 0.0F;
	}

	/** Sets {@code property} to {@code value} on every block of the cart whose first block is at {@code master}. */
	private <T extends Comparable<T>> void setAll(Level level, BlockPos master, Direction facing, Property<T> property, T value) {
		for (int part = 0; part < CELLS.length; part++) {
			BlockPos at = partPos(master, facing, part);
			BlockState there = level.getBlockState(at);
			if (there.is(this) && part(there) == part && there.getValue(FACING) == facing) {
				level.setBlock(at, there.setValue(property, value), Block.UPDATE_ALL);
			}
		}
	}

	/** Sugar is spun into floss while it runs; Paper winds a serving of floss onto a cone. */
	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
			BlockHitResult hit) {
		boolean sugar = stack.is(Items.SUGAR);
		boolean paper = stack.is(Items.PAPER);
		if (!sugar && !paper) {
			return stack.isEmpty() ? InteractionResult.TRY_WITH_EMPTY_HAND : InteractionResult.PASS;
		}
		if (level.isClientSide()) {
			return InteractionResult.SUCCESS;
		}
		int floss = state.getValue(FLOSS_LEVEL);
		String refusal = sugar ? (!state.getValue(LIT) ? "off" : floss >= FLOSS ? "full" : null) : floss == 0 ? "empty" : null;
		if (refusal != null) {
			player.sendOverlayMessage(Component.translatable("message.jugcraft.cotton_candy_cart." + refusal));
			return InteractionResult.CONSUME;
		}
		setAll(level, masterPos(pos, state), state.getValue(FACING), FLOSS_LEVEL, floss + (sugar ? 1 : -1));
		stack.consume(1, player);
		if (paper) {
			ItemStack candy = new ItemStack(JugcraftAgriculture.item("cotton_candy"));
			if (!player.getInventory().add(candy)) {
				Block.popResource(level, pos, candy);
			}
		}
		level.playSound(null, pos, sugar ? SoundEvents.WOOL_PLACE : SoundEvents.WOOL_HIT, SoundSource.BLOCKS, 0.8F, sugar ? 1.4F : 1.0F);
		level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
		return InteractionResult.SUCCESS;
	}

	/** An empty hand switches its motor on or off, on every block. */
	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (!level.isClientSide()) {
			boolean running = !state.getValue(LIT);
			setAll(level, masterPos(pos, state), state.getValue(FACING), LIT, running);
			level.playSound(null, pos, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.6F, running ? 0.8F : 0.6F);
			level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return part(state) == MASTER ? new DecorationBlockEntity(JugcraftAgriculture.COTTON_CANDY_CART_ENTITY, pos, state) : null;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(FLOSS_LEVEL, RUNNER);
	}
}

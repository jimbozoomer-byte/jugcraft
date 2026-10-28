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
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/**
 * The Ticket Booth (Haunted Midway, part 2; tools/haunted_midway.py TICKET_BOOTH): a violet kiosk one block across and
 * two up ({@link MultiDecorationBlock}), its ticket window glazed over a counter, with a brass ticket dispenser on the
 * counter, a roll of paper on top and a crank at its side. Paper used on it loads the roll, {@value #PER_SHEET} tickets a
 * sheet, up to {@value #ROLL} ({@link #ROLL_LEFT}, on both blocks); an empty hand turns the crank once round and prints a
 * Midway Ticket. The turn is a block event its block entity marks, for client/TicketBoothRenderer to turn the
 * blockstate's {@link #RUNNER} model (the crank) about its axle over {@value #CRANK_TICKS} ticks; the crank turns once
 * at a time. A ticket is a round's admission at the midway's games, not money. Broken, the booth gives back a sheet for
 * each {@value #PER_SHEET} tickets left on its roll (its loot table). Its model is tools/haunted_midway_data.py
 * ticket_booth().
 */
public class TicketBoothBlock extends MultiDecorationBlock implements EntityBlock {
	public static final int ROLL = 32;
	public static final int PER_SHEET = 2;
	public static final int CRANK_TICKS = 10;
	/** The crank's axle, along the booth's width (pixels: height and depth, facing north). */
	public static final float AXLE_Y = 16.5F;
	public static final float AXLE_Z = 2.1F;
	/** Its blocks: 1 across, 2 up, 1 deep (tools/haunted_midway.py TICKET_BOOTH["size"]). */
	private static final int[][] CELLS = box(1, 2, 1);
	private static final int CRANK = 1;
	public static final IntegerProperty PART = IntegerProperty.create("part", 0, CELLS.length - 1);
	public static final IntegerProperty ROLL_LEFT = IntegerProperty.create("roll", 0, ROLL);
	public static final BooleanProperty RUNNER = BooleanProperty.create("runner");
	/** The kiosk and its counter, the dispenser and its crank, and the roof and crest. */
	private static final Map<Direction, VoxelShape[]> SHAPES = shapesOf(CELLS, new double[] {0.5, 0.0, 1.0, 15.5, 26.0, 15.25},
			new double[] {5.0, 14.75, 1.25, 12.25, 19.5, 3.0}, new double[] {0.5, 26.0, 2.25, 15.5, 31.75, 15.5});

	public TicketBoothBlock(Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState().setValue(LIT, false).setValue(ROLL_LEFT, 0).setValue(RUNNER, false));
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

	/**
	 * How far round the crank has turned {@code since} ticks (with the partial tick) after it was last turned, in degrees:
	 * once round, easing in and out, over {@value #CRANK_TICKS} ticks; at rest otherwise.
	 */
	public static float crank(float since) {
		if (since < 0.0F || since >= CRANK_TICKS) {
			return 0.0F;
		}
		return 180.0F * (float) (1.0 - Math.cos(since / CRANK_TICKS * Math.PI));
	}

	/** Sets the roll to {@code left} on every block of the booth whose first block is at {@code master}. */
	private void setRoll(Level level, BlockPos master, Direction facing, int left) {
		for (int part = 0; part < CELLS.length; part++) {
			BlockPos at = partPos(master, facing, part);
			BlockState there = level.getBlockState(at);
			if (there.is(this) && part(there) == part && there.getValue(FACING) == facing) {
				level.setBlock(at, there.setValue(ROLL_LEFT, left), Block.UPDATE_ALL);
			}
		}
	}

	/** Paper loads the roll; anything else does what it would on any block. */
	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
			BlockHitResult hit) {
		if (!stack.is(Items.PAPER)) {
			return stack.isEmpty() ? InteractionResult.TRY_WITH_EMPTY_HAND : InteractionResult.PASS;
		}
		if (level.isClientSide()) {
			return InteractionResult.SUCCESS;
		}
		int left = state.getValue(ROLL_LEFT);
		if (left + PER_SHEET > ROLL) {
			player.sendOverlayMessage(Component.translatable("message.jugcraft.ticket_booth.full"));
			return InteractionResult.CONSUME;
		}
		setRoll(level, masterPos(pos, state), state.getValue(FACING), left + PER_SHEET);
		stack.consume(1, player);
		level.playSound(null, pos, SoundEvents.BOOK_PAGE_TURN, SoundSource.BLOCKS, 0.8F, 1.0F);
		level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
		return InteractionResult.SUCCESS;
	}

	/** An empty hand turns the crank and prints a ticket, if there is paper on the roll and the crank is at rest. */
	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		int left = state.getValue(ROLL_LEFT);
		if (left == 0) {
			if (!level.isClientSide()) {
				player.sendOverlayMessage(Component.translatable("message.jugcraft.ticket_booth.empty"));
			}
			return InteractionResult.PASS;
		}
		if (level.isClientSide()) {
			return InteractionResult.SUCCESS;
		}
		BlockPos master = masterPos(pos, state);
		if (!(level.getBlockEntity(master) instanceof DecorationBlockEntity booth)
				|| booth.marked() != Long.MIN_VALUE && level.getGameTime() - booth.marked() < CRANK_TICKS) {
			return InteractionResult.CONSUME;
		}
		setRoll(level, master, state.getValue(FACING), left - 1);
		// Marked here at once, so a second turn in the same tick waits; the block event tells the clients.
		booth.mark(level.getGameTime());
		ItemStack ticket = new ItemStack(JugcraftAgriculture.item("midway_ticket"));
		if (!player.getInventory().add(ticket)) {
			Block.popResource(level, pos, ticket);
		}
		level.blockEvent(master, this, CRANK, 0);
		level.playSound(null, pos, SoundEvents.CHAIN_HIT, SoundSource.BLOCKS, 0.6F, 1.4F);
		level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
		return InteractionResult.SUCCESS;
	}

	/** A turn of the crank: its block entity marks the time, for the crank's turn (and so the next waits for it). */
	@Override
	protected boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int param) {
		super.triggerEvent(state, level, pos, id, param);
		if (id == CRANK && level.getBlockEntity(pos) instanceof DecorationBlockEntity booth) {
			booth.mark(level.getGameTime());
			return true;
		}
		return false;
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return part(state) == MASTER ? new DecorationBlockEntity(JugcraftAgriculture.TICKET_BOOTH_ENTITY, pos, state) : null;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(ROLL_LEFT, RUNNER);
	}
}

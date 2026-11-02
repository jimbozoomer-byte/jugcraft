package io.github.jimbozoomer.jugcraft.agriculture;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * The Candy Corn Dispenser (Halloween Party Table, part 1; tools/party_table.py DISPENSER): a jumbo gumball machine
 * 86 cm high, its glass globe 41 cm across on a black cast-iron pedestal banded in orange, a brass crank and a brass
 * flap over its chute on its front. It stands facing whoever places it ({@link FacingDecorationBlock}). Candy corn used
 * on it pours in, as much as fits, up to {@value #CAPACITY} pieces ({@link CandyCornDispenserBlockEntity}); its globe
 * shows how full it is ({@link #FILL}). An empty hand turns its crank once round in {@value #CRANK_TICKS} ticks, and as
 * it comes round one piece drops out of its chute, the flap swinging out ({@link #crank}, {@link #flap};
 * client/CandyCornDispenserRenderer). It turns again only once it has come round; an empty one only rattles. Its model
 * is tools/party_table_data.py candy_corn_dispenser().
 */
public class CandyCornDispenserBlock extends FacingDecorationBlock implements EntityBlock {
	public static final int CAPACITY = 64;
	public static final int LEVELS = 4;
	public static final int CRANK_TICKS = 20;
	/** The crank's axle (pixels: height and depth, facing north), about which it turns once round. */
	public static final float CRANK_Y = 3.75F;
	public static final float CRANK_Z = 4.75F;
	/** The flap's swing out (degrees) and back (ticks), about its top (pixels: height and depth). */
	public static final float FLAP = 55.0F;
	public static final int FLAP_TICKS = 12;
	public static final float HINGE_Y = 3.0F;
	public static final float HINGE_Z = 4.75F;
	/** How full the globe looks: 0 empty, 1 to {@value #LEVELS}. */
	public static final IntegerProperty FILL = IntegerProperty.create("fill", 0, LEVELS);
	/** 0 for the dispenser; 1 its crank and 2 its flap, for the renderer. */
	public static final IntegerProperty RUNNER = IntegerProperty.create("runner", 0, 2);
	private static final int CRANK = 1;

	public CandyCornDispenserBlock(Properties properties) {
		super(properties, new double[] {4.5, 0.0, 4.5, 11.5, 13.75, 11.5});
		registerDefaultState(defaultBlockState().setValue(FILL, 0).setValue(RUNNER, 0));
	}

	/** The fill level for {@code count} pieces held. */
	public static int fill(int count) {
		return count <= 0 ? 0 : Math.min(LEVELS, 1 + (count - 1) * LEVELS / CAPACITY);
	}

	/** The crank's turn {@code since} ticks after it was set turning, degrees clockwise as its front is seen: once round, easing in and out. */
	public static float crank(float since) {
		if (since < 0.0F || since >= CRANK_TICKS) {
			return 0.0F;
		}
		float t = since / CRANK_TICKS;
		return 360.0F * t * t * (3.0F - 2.0F * t);
	}

	/** The flap's swing out, degrees, {@code since} ticks after the crank was set turning: out and back as the piece drops, once it has come round. */
	public static float flap(float since) {
		float t = (since - CRANK_TICKS) / FLAP_TICKS;
		return t <= 0.0F || t >= 1.0F ? 0.0F : FLAP * Mth.sin((float) Math.PI * t);
	}

	/** Where its chute lets a piece out (facing north: in front of the flap, at its foot). */
	public static Vec3 chute(BlockPos pos, Direction facing) {
		double out = 0.5 - 4.0 / 16.0;
		return new Vec3(pos.getX() + 0.5 + facing.getStepX() * out, pos.getY() + 1.75 / 16.0, pos.getZ() + 0.5 + facing.getStepZ() * out);
	}

	/** Candy corn pours in, as much as fits; anything else is an empty hand. */
	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
			BlockHitResult hit) {
		if (!stack.is(JugcraftAgriculture.item("candy_corn"))) {
			return InteractionResult.TRY_WITH_EMPTY_HAND;
		}
		if (level instanceof ServerLevel server && server.getBlockEntity(pos) instanceof CandyCornDispenserBlockEntity dispenser) {
			if (dispenser.pour(stack) == 0) {
				return InteractionResult.FAIL;
			}
			server.setBlock(pos, state.setValue(FILL, fill(dispenser.count())), Block.UPDATE_ALL);
			server.playSound(null, pos, SoundEvents.BUNDLE_INSERT, SoundSource.BLOCKS, 0.8F, 0.9F);
			server.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
		}
		return InteractionResult.SUCCESS;
	}

	/** An empty hand turns the crank, if it isn't turning; an empty one only rattles. */
	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (level instanceof ServerLevel server && server.getBlockEntity(pos) instanceof CandyCornDispenserBlockEntity dispenser) {
			long last = dispenser.marked();
			if (last != Long.MIN_VALUE && server.getGameTime() - last < CRANK_TICKS) {
				return InteractionResult.SUCCESS;
			}
			if (dispenser.count() <= 0) {
				server.playSound(null, pos, SoundEvents.DISPENSER_FAIL, SoundSource.BLOCKS, 0.6F, 1.4F);
				return InteractionResult.SUCCESS;
			}
			server.playSound(null, pos, SoundEvents.GRINDSTONE_USE, SoundSource.BLOCKS, 0.5F, 1.6F);
			dispenser.mark(server.getGameTime()); // At once, so a second use this tick finds it turning.
			server.blockEvent(pos, this, CRANK, 0);
			server.scheduleTick(pos, this, CRANK_TICKS);
			server.gameEvent(player, GameEvent.BLOCK_ACTIVATE, pos);
		}
		return InteractionResult.SUCCESS;
	}

	/** The crank is set turning: its block entity marks the time, on every side, for the renderer (and for when it may turn again). */
	@Override
	protected boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int param) {
		super.triggerEvent(state, level, pos, id, param);
		if (id != CRANK || !(level.getBlockEntity(pos) instanceof CandyCornDispenserBlockEntity dispenser)) {
			return false;
		}
		dispenser.mark(level.getGameTime());
		return true;
	}

	/** The crank has come round: one piece drops out of the chute. */
	@Override
	protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		if (!(level.getBlockEntity(pos) instanceof CandyCornDispenserBlockEntity dispenser) || !dispenser.take()) {
			return;
		}
		level.setBlock(pos, state.setValue(FILL, fill(dispenser.count())), Block.UPDATE_ALL);
		Direction facing = state.getValue(FACING);
		Vec3 at = chute(pos, facing);
		ItemEntity piece = new ItemEntity(level, at.x, at.y, at.z, new ItemStack(JugcraftAgriculture.item("candy_corn")),
				facing.getStepX() * 0.08, 0.05, facing.getStepZ() * 0.08);
		piece.setDefaultPickUpDelay();
		level.addFreshEntity(piece);
		level.playSound(null, pos, SoundEvents.BUNDLE_REMOVE_ONE, SoundSource.BLOCKS, 0.7F, 1.2F);
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new CandyCornDispenserBlockEntity(pos, state);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(FILL, RUNNER);
	}
}

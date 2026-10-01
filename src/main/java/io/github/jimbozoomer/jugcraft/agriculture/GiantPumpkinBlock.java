package io.github.jimbozoomer.jugcraft.agriculture;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealSource;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

/**
 * A giant pumpkin: a cube of {@link #SIZE} blocks a side (1, then 2, then 3) grown from a
 * {@link GiantPumpkinVineBlock}. Every block of it is this block; {@link #PART} says where in the cube it
 * is, counted from the lowest north-west corner (the master, part 0, which alone has the
 * {@link GiantPumpkinBlockEntity} and ticks). Breaking any block breaks the whole pumpkin and drops once
 * (the loot table reads the size). Full grown, each side can be carved as one 48x48 face, and a torch
 * inside lights every block of it ({@link #LIGHT}).
 */
public class GiantPumpkinBlock extends BaseEntityBlock implements BonemealableBlock {
	public static final int MAX_SIZE = 3;
	public static final IntegerProperty SIZE = IntegerProperty.create("size", 1, MAX_SIZE);
	public static final IntegerProperty PART = IntegerProperty.create("part", 0, MAX_SIZE * MAX_SIZE * MAX_SIZE - 1);
	/** The light every block gives: the carving's glow while a torch is inside, else 0. */
	public static final IntegerProperty LIGHT = IntegerProperty.create("light", 0, 15);

	public GiantPumpkinBlock(Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(SIZE, 1).setValue(PART, 0).setValue(LIGHT, 0));
	}

	public static int light(BlockState state) {
		return state.getValue(LIGHT);
	}

	/** The part number of the block at {@code (dx, dy, dz)} from the master of a pumpkin {@code size} blocks wide. */
	public static int part(int size, int dx, int dy, int dz) {
		return dx + size * (dz + size * dy);
	}

	/** The offset of a part from its master: {@code {dx, dy, dz}}. */
	public static int[] offset(int size, int part) {
		return new int[] {part % size, part / (size * size), (part / size) % size};
	}

	/** Where the master of the giant pumpkin this block belongs to is. */
	public static BlockPos masterPos(BlockPos pos, BlockState state) {
		int[] offset = offset(state.getValue(SIZE), state.getValue(PART));
		return pos.offset(-offset[0], -offset[1], -offset[2]);
	}

	public static @Nullable GiantPumpkinBlockEntity master(Level level, BlockPos pos, BlockState state) {
		return level.getBlockEntity(masterPos(pos, state)) instanceof GiantPumpkinBlockEntity master ? master : null;
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return state.getValue(PART) == 0 ? new GiantPumpkinBlockEntity(pos, state) : null;
	}

	/** Only the master ticks: it grows the pumpkin, then puts on weight. */
	@Override
	protected boolean isRandomlyTicking(BlockState state) {
		return state.getValue(PART) == 0;
	}

	@Override
	protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		if (level.getBlockEntity(pos) instanceof GiantPumpkinBlockEntity master) {
			master.tick(level, random);
		}
	}

	@Override
	protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
		super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
		if (level.getBlockState(pos).is(this)) {
			return; // Grown or lit, not removed.
		}
		// The block that was broken dropped the harvest; remove the rest of the pumpkin without drops.
		int size = state.getValue(SIZE);
		BlockPos master = masterPos(pos, state);
		for (int part = 0; part < size * size * size; part++) {
			int[] offset = offset(size, part);
			BlockPos partPos = master.offset(offset[0], offset[1], offset[2]);
			BlockState other = level.getBlockState(partPos);
			if (!partPos.equals(pos) && other.is(this) && other.getValue(SIZE) == size && other.getValue(PART) == part) {
				level.removeBlock(partPos, false);
			}
		}
	}

	/** A torch lights a full-grown, carved giant pumpkin; every block of it then glows. */
	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
			InteractionHand hand, BlockHitResult hit) {
		GiantPumpkinBlockEntity master = master(level, pos, state);
		if (!stack.is(Items.TORCH) || master == null || master.lit() || master.glow() == 0) {
			return stack.isEmpty() ? InteractionResult.TRY_WITH_EMPTY_HAND : InteractionResult.PASS;
		}
		if (!level.isClientSide()) {
			master.setLit(true);
			stack.consume(1, player);
			level.playSound(null, pos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 1.0F, 0.9F);
			level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
		}
		return InteractionResult.SUCCESS;
	}

	/** An empty hand takes the torch back out. */
	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		GiantPumpkinBlockEntity master = master(level, pos, state);
		if (master == null || !master.lit()) {
			return InteractionResult.PASS;
		}
		if (!level.isClientSide()) {
			master.setLit(false);
			ItemStack torch = new ItemStack(Items.TORCH);
			if (!player.getInventory().add(torch)) {
				Block.popResourceFromFace(level, pos, hit.getDirection(), torch);
			}
			level.playSound(null, pos, SoundEvents.WOOD_BREAK, SoundSource.BLOCKS, 0.8F, 0.9F);
			level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
		}
		return InteractionResult.SUCCESS;
	}

	/**
	 * Bone meal feeds a giant pumpkin its vine still holds; once cut from the vine, carved or at its heaviest,
	 * it has no effect.
	 */
	@Override
	public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state, BonemealSource source) {
		return level.getBlockEntity(masterPos(pos, state)) instanceof GiantPumpkinBlockEntity master && master.canGrow() && master.attached(level);
	}

	@Override
	public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state, BonemealSource source) {
		return true;
	}

	@Override
	public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state, BonemealSource source) {
		GiantPumpkinBlockEntity master = master(level, pos, state);
		if (master != null) {
			master.feed(level, GiantPumpkinBlockEntity.BONE_MEAL_POINTS, random);
		}
	}

	@Override
	protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
		return new ItemStack(JugcraftAgriculture.item("giant_pumpkin_seeds"));
	}

	/** Turning one block of a pumpkin alone would tear it apart, so structures leave it as it is. */
	@Override
	protected BlockState rotate(BlockState state, Rotation rotation) {
		return state;
	}

	@Override
	protected BlockState mirror(BlockState state, Mirror mirror) {
		return state;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(SIZE, PART, LIGHT);
	}

	/** Under the Harvest Moon a lit giant pumpkin throws off sparks from its top. */
	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		if (HarvestMoon.clientActive && state.getValue(LIGHT) > 0 && random.nextInt(6) == 0) {
			level.addParticle(random.nextBoolean() ? ParticleTypes.SMALL_FLAME : ParticleTypes.WAX_ON, pos.getX() + random.nextDouble(),
					pos.getY() + 1.0, pos.getZ() + random.nextDouble(), 0.0, 0.04, 0.0);
		}
	}
}

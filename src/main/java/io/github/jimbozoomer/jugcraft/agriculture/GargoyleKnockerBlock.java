package io.github.jimbozoomer.jugcraft.agriculture;

import java.util.EnumMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/**
 * The Gargoyle Door Knocker (Front-Yard Haunt Scenes, part 3; tools/front_yard.py KNOCKER): a bronze gargoyle's head on
 * its back plate, a ring in its jaws and a stud below for the ring to strike. It hangs on the side of a block or on
 * either face of a door, as the wreaths do (it is a kind of {@link AutumnWreathBlock}, and a {@link DoorWreath} on a
 * door). An empty hand lifts its ring and raps {@value #RAPS} times, {@value #RAP_TICKS} ticks apart, the gargoyle's
 * eyes glowing while it raps ({@link #ring}, {@link #eyes}: client/GargoyleKnockerRenderer on a wall,
 * client/DoorWreathRenderer on a door). With a Candy Bag in hand it knocks for trick-or-treat
 * ({@link TrickOrTreat#knockWith}) at its own door, or at a wooden door by the wall it hangs on. It is haunted: now and
 * then it raps by itself (one random tick in {@value #HAUNT} on a wall, one tick in {@value #DOOR_CHANCE} on a door).
 * Its blockstate's {@link #RUNNER} models 1 and 2 are the ring and the eyes. Its model is tools/front_yard_data.py
 * gargoyle_knocker().
 */
public class GargoyleKnockerBlock extends AutumnWreathBlock implements EntityBlock {
	public static final int RAPS = 3;
	public static final int RAP_TICKS = 6;
	/** How far the ring swings out to rap, in degrees, about its pivot in the jaws (pixels: height and depth, facing north). */
	public static final float SWING = 40.0F;
	public static final float PIVOT_Y = 7.75F;
	public static final float PIVOT_Z = 15.0F;
	/** One random tick in this many on a wall, and one tick in {@value #DOOR_CHANCE} on a door, it raps by itself. */
	public static final int HAUNT = 3;
	public static final int DOOR_CHANCE = 3000;
	/** How long its eyes glow after its last rap (ticks). */
	public static final int GLOW_AFTER = 10;
	/** 0 for the knocker; 1 its ring and 2 its eyes, for the renderer. */
	public static final IntegerProperty RUNNER = IntegerProperty.create("runner", 0, 2);
	private static final int KNOCK = 1;
	/** The plate, the head and the ring (pixels, facing north: on the north side of the block behind it). */
	private static final double[] BOX = {6.0, 4.0, 13.75, 10.0, 12.75, 16.0};
	private static final Map<Direction, VoxelShape> SHAPES = new EnumMap<>(Direction.class);

	static {
		for (Direction facing : Direction.Plane.HORIZONTAL) {
			SHAPES.put(facing, LongDecorationBlock.shape(new double[][] {BOX}, facing));
		}
	}

	public GargoyleKnockerBlock(Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState().setValue(RUNNER, 0));
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPES.get(state.getValue(FACING));
	}

	/** How far its ring stands out, in degrees, {@code since} ticks after it began to knock: lifted and let fall each rap, striking at each rap's end. */
	public static float ring(float since) {
		if (since < 0.0F || since >= RAPS * RAP_TICKS) {
			return 0.0F;
		}
		return SWING * (float) Math.sin(Math.PI * (since % RAP_TICKS) / RAP_TICKS);
	}

	/** Whether its eyes glow {@code since} ticks after it began to knock: while it raps, and a little after. */
	public static boolean eyes(float since) {
		return since >= 0.0F && since < RAPS * RAP_TICKS + GLOW_AFTER;
	}

	/** Whether it is still rapping {@code since} ticks after it began, so a new knock waits. */
	public static boolean rapping(long since) {
		return since >= 0 && since <= RAPS * RAP_TICKS;
	}

	@Override
	public boolean takesMums() {
		return false;
	}

	@Override
	public double[] doorBox() {
		return new double[] {2.25, 4.0, 13.0};
	}

	/** Ticks from game time {@code at} (a knock, or {@link Long#MIN_VALUE} for none yet) to {@code now}: a long while for none. */
	public static long since(long now, long at) {
		return at == Long.MIN_VALUE ? Long.MAX_VALUE : now - at;
	}

	private static long since(Level level, @Nullable BlockEntity entity) {
		return entity instanceof DecorationBlockEntity watch ? since(level.getGameTime(), watch.marked()) : Long.MAX_VALUE;
	}

	/** It begins to knock: the ring lifts, and raps {@link #RAP_TICKS} later and again (its scheduled ticks). */
	private void knock(ServerLevel level, BlockPos pos) {
		if (rapping(since(level, level.getBlockEntity(pos)))) {
			return;
		}
		level.blockEvent(pos, this, KNOCK, 0);
		level.scheduleTick(pos, this, RAP_TICKS);
	}

	static void rap(Level level, Vec3 at) {
		level.playSound(null, at.x, at.y, at.z, SoundEvents.ANVIL_LAND, SoundSource.BLOCKS, 0.25F, 1.9F);
	}

	/** An empty hand knocks. */
	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (level instanceof ServerLevel server) {
			knock(server, pos);
			level.gameEvent(player, GameEvent.BLOCK_ACTIVATE, pos);
		}
		return InteractionResult.SUCCESS;
	}

	/** A Candy Bag knocks for trick-or-treat at a wooden door by it; a mum does nothing; anything else is an empty hand. */
	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
			BlockHitResult hit) {
		if (TrickOrTreat.isCandyBag(stack)) {
			BlockPos door = doorBy(level, pos, state.getValue(FACING));
			if (level instanceof ServerLevel server) {
				knock(server, pos);
			}
			return door == null ? InteractionResult.SUCCESS : TrickOrTreat.knockWith(player, stack, door);
		}
		return Mums.of(stack) != null ? InteractionResult.PASS : InteractionResult.TRY_WITH_EMPTY_HAND;
	}

	/** The wooden door by the knocker at {@code pos}: beside it or the block it hangs on, at its height or a block below. */
	static @Nullable BlockPos doorBy(BlockGetter level, BlockPos pos, Direction facing) {
		BlockPos behind = pos.relative(facing.getOpposite());
		for (int down = 0; down <= 1; down++) {
			for (BlockPos near : new BlockPos[] {pos.relative(facing.getClockWise()), pos.relative(facing.getCounterClockWise()),
					behind.relative(facing.getClockWise()), behind.relative(facing.getCounterClockWise())}) {
				BlockPos at = near.below(down);
				BlockState there = level.getBlockState(at);
				if (there.getBlock() instanceof DoorBlock && DoorBlock.isWoodenDoor(there)) {
					return at;
				}
			}
		}
		return null;
	}

	/** On a door: a Candy Bag knocks for trick-or-treat at that door, and an empty hand knocks; sneaking, it lets the door be. */
	@Override
	public InteractionResult useOnDoor(DoorWreath ornament, Player player, InteractionHand hand) {
		ItemStack held = player.getItemInHand(hand);
		if (player.isSecondaryUseActive() || !(held.isEmpty() || TrickOrTreat.isCandyBag(held))) {
			return InteractionResult.PASS;
		}
		if (!player.level().isClientSide() && !rapping(since(player.level().getGameTime(), ornament.knockedAt()))) {
			ornament.knock();
		}
		return TrickOrTreat.isCandyBag(held) ? TrickOrTreat.knockWith(player, held, ornament.getPos()) : InteractionResult.SUCCESS;
	}

	/** On a door, it raps on time after each knock, and now and then knocks by itself. */
	@Override
	public void doorTick(ServerLevel level, DoorWreath ornament) {
		long since = since(level.getGameTime(), ornament.knockedAt());
		if (since > 0 && since <= RAPS * RAP_TICKS && since % RAP_TICKS == 0) {
			rap(level, ornament.position());
		} else if (!rapping(since) && level.getRandom().nextInt(DOOR_CHANCE) == 0) {
			ornament.knock();
		}
	}

	/** Its raps, {@link #RAP_TICKS} apart; but first, against a door, it moves onto the door as a wreath does. */
	@Override
	protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		if (level.getBlockState(pos.relative(state.getValue(FACING).getOpposite())).getBlock() instanceof DoorBlock) {
			super.tick(state, level, pos, random);
			return;
		}
		long since = since(level, level.getBlockEntity(pos));
		if (since > 0 && since <= RAPS * RAP_TICKS) {
			rap(level, Vec3.atCenterOf(pos));
			if (since + RAP_TICKS <= RAPS * RAP_TICKS) {
				level.scheduleTick(pos, this, RAP_TICKS);
			}
		}
	}

	/** Now and then it knocks by itself. */
	@Override
	protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		if (random.nextInt(HAUNT) == 0) {
			knock(level, pos);
		}
	}

	/** A knock: its block entity marks the time, on every side, for the renderer and its raps. */
	@Override
	protected boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int param) {
		super.triggerEvent(state, level, pos, id, param);
		if (id != KNOCK || !(level.getBlockEntity(pos) instanceof DecorationBlockEntity watch)) {
			return false;
		}
		watch.mark(level.getGameTime());
		return true;
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new DecorationBlockEntity(JugcraftAgriculture.GARGOYLE_KNOCKER_ENTITY, pos, state);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(RUNNER);
	}
}

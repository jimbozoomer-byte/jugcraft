package io.github.jimbozoomer.jugcraft.agriculture;

import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
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
 * The Caramel-Corn Wagon (Haunted Midway, part 1; tools/haunted_midway.py POPCORN_WAGON): an old popcorn wagon on two big
 * spoked wheels, one block across and two up ({@link MultiDecorationBlock}), a glass case on its cabinet with a steel
 * kettle hung in it, a roof with a crest board and a jack-o'-lantern on top. It is placed out ({@link #LIT} false);
 * flint and steel or a fire charge lights it and a shovel puts it out. Lit, its case lamp and lantern glow, its upper
 * block giving light {@value #LIGHT}. Corn kernels used on it while it is lit go in the kettle, one at a time
 * ({@link #POPPING}): after {@value #POP_TICKS} ticks they pop, the kettle tips forward to spill them (a block event its
 * block entity marks, for client/PopcornWagonRenderer to tip the blockstate's {@link #RUNNER} model, the kettle) and the
 * case holds one more Popcorn ({@link #POPCORN}, on every block), as smelting a kernel gives, up to {@value #CAPACITY}.
 * An empty hand takes a Popcorn out; Caramel coats two into a Caramel Popcorn, as the crafting recipe does. Broken, it
 * gives back its popcorn and a kernel still in its kettle (its loot table). It burns no fuel. Its model is
 * tools/haunted_midway_data.py popcorn_wagon().
 */
public class PopcornWagonBlock extends MultiDecorationBlock implements EntityBlock {
	public static final int CAPACITY = 8;
	public static final int POP_TICKS = 60;
	public static final int LIGHT = 10;
	/** How far the kettle tips forward to spill its popcorn, in degrees, and over how many ticks it tips and comes back. */
	public static final float TIP = 40.0F;
	public static final int TIP_TICKS = 20;
	/** The kettle's pivot, where it tips (pixels: height and depth in the wagon's first block, facing north). */
	public static final float PIVOT_Y = 21.0F;
	public static final float PIVOT_Z = 8.0F;
	/** Its blocks: 1 across, 2 up, 1 deep (tools/haunted_midway.py POPCORN_WAGON["size"]); the lamp and lantern are part 1's. */
	private static final int[][] CELLS = box(1, 2, 1);
	public static final int TOP = 1;
	private static final int DUMP = 1;
	public static final IntegerProperty PART = IntegerProperty.create("part", 0, CELLS.length - 1);
	public static final IntegerProperty POPCORN = IntegerProperty.create("popcorn", 0, CAPACITY);
	public static final BooleanProperty POPPING = BooleanProperty.create("popping");
	public static final BooleanProperty RUNNER = BooleanProperty.create("runner");
	private static final TagKey<Item> IGNITERS = TagKey.create(Registries.ITEM, Identifier.withDefaultNamespace("creeper_igniters"));
	/** The cabinet on its wheels, the glass case and the roof with its crest and lantern. */
	private static final Map<Direction, VoxelShape[]> SHAPES = shapesOf(CELLS, new double[] {0.0, 0.0, 3.25, 16.0, 13.75, 15.25},
			new double[] {1.75, 13.75, 3.5, 14.25, 24.5, 12.5}, new double[] {1.0, 24.5, 2.75, 15.0, 26.75, 13.25},
			new double[] {3.5, 26.75, 1.75, 12.5, 31.5, 4.25});

	public PopcornWagonBlock(Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState().setValue(LIT, false).setValue(POPCORN, 0).setValue(POPPING, false).setValue(RUNNER, false));
	}

	@Override
	public int[][] cells() {
		return CELLS;
	}

	@Override
	public IntegerProperty partProperty() {
		return PART;
	}

	/** The light it gives: its upper block (the case lamp and the lantern), while lit. */
	public static int light(BlockState state) {
		return state.getValue(LIT) && state.getValue(PART) == TOP ? LIGHT : 0;
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPES.get(state.getValue(FACING))[part(state)];
	}

	/**
	 * How far forward the kettle is tipped {@code since} ticks (with the partial tick) after it last spilled, in degrees:
	 * out and back smoothly over {@value #TIP_TICKS} ticks, then upright.
	 */
	public static float tip(float since) {
		if (since < 0.0F || since >= TIP_TICKS) {
			return 0.0F;
		}
		return TIP * (float) (1.0 - Math.cos(since / TIP_TICKS * Math.PI * 2.0)) / 2.0F;
	}

	/** Sets {@code property} to {@code value} on every block of the wagon whose first block is at {@code master}. */
	private <T extends Comparable<T>> void setAll(Level level, BlockPos master, Direction facing, Property<T> property, T value) {
		for (int part = 0; part < CELLS.length; part++) {
			BlockPos at = partPos(master, facing, part);
			BlockState there = level.getBlockState(at);
			if (there.is(this) && part(there) == part && there.getValue(FACING) == facing) {
				level.setBlock(at, there.setValue(property, value), Block.UPDATE_ALL);
			}
		}
		if (level.getBlockEntity(master) instanceof DecorationBlockEntity wagon) local.peepo.CompanionReadiness.invalidate(wagon);
	}

	private static void give(Level level, BlockPos pos, Player player, ItemStack stack) {
		if (!player.getInventory().add(stack)) {
			Block.popResource(level, pos, stack);
		}
	}

	/**
	 * Flint and steel or a fire charge lights it and a shovel puts it out; a corn kernel goes in the lit kettle to pop;
	 * Caramel coats two popcorn.
	 */
	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
			BlockHitResult hit) {
		boolean lighter = stack.is(IGNITERS);
		boolean shovel = stack.is(ItemTags.SHOVELS);
		boolean kernel = stack.is(JugcraftAgriculture.item("corn_kernels"));
		boolean caramel = stack.is(JugcraftAgriculture.item("caramel"));
		if (!lighter && !shovel && !kernel && !caramel) {
			return stack.isEmpty() ? InteractionResult.TRY_WITH_EMPTY_HAND : InteractionResult.PASS;
		}
		boolean lit = state.getValue(LIT);
		if ((lighter && lit) || (shovel && !lit)) {
			return InteractionResult.PASS;
		}
		if (level.isClientSide()) {
			return InteractionResult.SUCCESS;
		}
		BlockPos master = masterPos(pos, state);
		Direction facing = state.getValue(FACING);
		int popcorn = state.getValue(POPCORN);
		String refusal = kernel ? (!lit ? "light" : state.getValue(POPPING) ? "popping" : popcorn >= CAPACITY ? "full" : null)
				: caramel && popcorn < 2 ? "two" : null;
		if (refusal != null) {
			player.sendOverlayMessage(Component.translatable("message.jugcraft.popcorn_wagon." + refusal));
			return InteractionResult.CONSUME;
		}
		if (lighter || shovel) {
			setAll(level, master, facing, LIT, lighter);
			if (lighter) {
				boolean worn = stack.isDamageableItem();
				level.playSound(null, pos, worn ? SoundEvents.FLINTANDSTEEL_USE : SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 1.0F,
						level.getRandom().nextFloat() * 0.4F + 0.8F);
				if (worn) {
					stack.hurtAndBreak(1, player, hand);
				} else {
					stack.consume(1, player);
				}
			} else {
				level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.6F, 1.0F);
			}
		} else if (kernel) {
			stack.consume(1, player);
			setAll(level, master, facing, POPPING, true);
			level.scheduleTick(master, this, POP_TICKS);
			level.playSound(null, pos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 0.4F, 1.6F);
		} else {
			stack.consume(1, player);
			setAll(level, master, facing, POPCORN, popcorn - 2);
			give(level, pos, player, new ItemStack(JugcraftAgriculture.item("caramel_popcorn")));
			level.playSound(null, pos, SoundEvents.HONEY_BLOCK_PLACE, SoundSource.BLOCKS, 0.8F, 1.0F);
		}
		level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
		return InteractionResult.SUCCESS;
	}

	/** An empty hand takes a Popcorn out of the case. */
	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (!level.isClientSide() && player.isShiftKeyDown() && level.getBlockEntity(masterPos(pos,state)) instanceof DecorationBlockEntity wagon
			&& wagon.confection != null && wagon.confection.collect(player)) return InteractionResult.SUCCESS;
		int popcorn = state.getValue(POPCORN);
		if (popcorn == 0) {
			if (!state.getValue(LIT) && !level.isClientSide()) {
				player.sendOverlayMessage(Component.translatable("message.jugcraft.popcorn_wagon.light"));
			}
			return InteractionResult.PASS;
		}
		if (!level.isClientSide()) {
			setAll(level, masterPos(pos, state), state.getValue(FACING), POPCORN, popcorn - 1);
			give(level, pos, player, new ItemStack(JugcraftAgriculture.item("popcorn")));
			level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.4F, 1.4F);
			level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
		}
		return InteractionResult.SUCCESS;
	}

	@Override protected boolean isPathfindable(BlockState state, net.minecraft.world.level.pathfinder.PathComputationType type) { return false; }

	/** The kernel in the kettle has popped: the kettle spills it into the case (its first block's scheduled tick). */
	@Override
	protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		if (part(state) != MASTER || !state.getValue(POPPING)) {
			return;
		}
		Direction facing = state.getValue(FACING);
		setAll(level, pos, facing, POPPING, false);
		setAll(level, pos, facing, POPCORN, Math.min(CAPACITY, state.getValue(POPCORN) + 1));
		level.blockEvent(pos, this, DUMP, 0);
		level.playSound(null, pos.above(), SoundEvents.BUBBLE_COLUMN_BUBBLE_POP, SoundSource.BLOCKS, 0.8F, 1.8F);
	}

	/** A spill: its block entity marks the time, for the kettle's tip. */
	@Override
	protected boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int param) {
		super.triggerEvent(state, level, pos, id, param);
		if (id == DUMP && level.getBlockEntity(pos) instanceof DecorationBlockEntity wagon) {
			wagon.mark(level.getGameTime());
			return true;
		}
		return false;
	}

	/** While a kernel pops, it crackles in the kettle and a few white flakes jump in the case (each client's own, near it). */
	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		if (!state.getValue(POPPING) || part(state) != MASTER) {
			return;
		}
		if (random.nextInt(3) == 0) {
			level.playLocalSound(pos.getX() + 0.5, pos.getY() + 1.3, pos.getZ() + 0.5, SoundEvents.BUBBLE_COLUMN_BUBBLE_POP, SoundSource.BLOCKS,
					0.5F, 1.6F + random.nextFloat() * 0.4F, false);
		}
		level.addParticle(ParticleTypes.WHITE_ASH, pos.getX() + 0.35 + random.nextDouble() * 0.3, pos.getY() + 1.25,
				pos.getZ() + 0.35 + random.nextDouble() * 0.3, 0.0, 0.05, 0.0);
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return part(state) == MASTER ? new DecorationBlockEntity(JugcraftAgriculture.POPCORN_WAGON_ENTITY, pos, state) : null;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(POPCORN, POPPING, RUNNER);
	}
}

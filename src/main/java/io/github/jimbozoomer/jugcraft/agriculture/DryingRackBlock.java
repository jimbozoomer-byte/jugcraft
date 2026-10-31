package io.github.jimbozoomer.jugcraft.agriculture;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import java.util.Locale;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Prediction;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
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
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/**
 * The Drying Rack (Barn & Harvest Tools, part 3; tools/barn_tools.py RACK): a freestanding frame, two uprights on
 * splayed feet joined by two rails, one block across and two up, with two hooks on each rail. Use herb sprigs
 * ({@link #HERBS}) or peppers on it to hang a bunch of up to {@value #BUNCH} on the first free hook. A bunch dries in
 * {@value #DRY_TICKS} ticks: herbs into Dried Herbs and peppers into Dried Chili, one for one. Use it with an empty hand
 * to take down the first dried bunch, or the last one hung if none has dried. Each block shows its rail's two bunches
 * ({@link #LEFT}, {@link #RIGHT}, as seen from the front), fresh or dried; a scheduled tick turns them dry. Its bunches are
 * kept by a {@link DryingRackBlockEntity} on its first block. Its model is tools/barn_tools_data.py rack().
 */
public class DryingRackBlock extends MultiDecorationBlock implements EntityBlock {
	public static final int SLOTS = 4;
	public static final int BUNCH = 4;
	public static final int DRY_TICKS = 2400;
	public static final TagKey<Item> HERBS = TagKey.create(Registries.ITEM, Jugcraft.id("herbs"));
	/** Its blocks: 1 across, 2 up, 1 deep (tools/barn_tools.py RACK["size"]). */
	private static final int[][] CELLS = box(1, 2, 1);
	public static final IntegerProperty PART = IntegerProperty.create("part", 0, CELLS.length - 1);
	public static final EnumProperty<Bunch> LEFT = EnumProperty.create("left", Bunch.class);
	public static final EnumProperty<Bunch> RIGHT = EnumProperty.create("right", Bunch.class);
	private static final Map<Direction, VoxelShape[]> SHAPES = shapesOf(CELLS, new double[] {1.0, 0.0, 3.0, 15.0, 31.0, 13.0});

	/** What hangs on a hook, as drawn. */
	public enum Bunch implements StringRepresentable {
		EMPTY, HERBS, DRIED_HERBS, CHILI, DRIED_CHILI;

		public static Bunch of(ItemStack bunch, boolean dried) {
			if (bunch.isEmpty()) {
				return EMPTY;
			}
			boolean herbs = bunch.is(DryingRackBlock.HERBS);
			return herbs ? (dried ? DRIED_HERBS : Bunch.HERBS) : (dried ? DRIED_CHILI : CHILI);
		}

		@Override
		public String getSerializedName() {
			return name().toLowerCase(Locale.ROOT);
		}
	}

	public DryingRackBlock(Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState().setValue(LEFT, Bunch.EMPTY).setValue(RIGHT, Bunch.EMPTY));
	}

	/** Whether {@code stack} hangs on the rack: herb sprigs or peppers. */
	public static boolean hangs(ItemStack stack) {
		return !stack.isEmpty() && (stack.is(HERBS) || stack.is(JugcraftAgriculture.item("pepper")));
	}

	/** A dried bunch's yield: as many Dried Herbs or Dried Chilies as it had sprigs or peppers. */
	public static ItemStack driedForm(ItemStack bunch) {
		return new ItemStack(JugcraftAgriculture.item(bunch.is(HERBS) ? "dried_herbs" : "dried_chili"), bunch.getCount());
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
	@Override
	protected boolean isPathfindable(BlockState state, net.minecraft.world.level.pathfinder.PathComputationType type) {
		return false;
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return part(state) == MASTER ? new DryingRackBlockEntity(pos, state) : null;
	}

	private @Nullable DryingRackBlockEntity rack(Level level, BlockPos pos, BlockState state) {
		return level.getBlockEntity(masterPos(pos, state)) instanceof DryingRackBlockEntity rack ? rack : null;
	}

	/** Hangs herbs or peppers; anything else does what it would on any block, and an empty hand takes a bunch down. */
	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
			BlockHitResult hit) {
		if (!hangs(stack)) {
			return stack.isEmpty() ? InteractionResult.TRY_WITH_EMPTY_HAND : InteractionResult.PASS;
		}
		DryingRackBlockEntity rack = rack(level, pos, state);
		if (level.isClientSide() || rack == null) {
			return InteractionResult.SUCCESS;
		}
		if (rack.hang(stack, level.getGameTime()) < 0) {
			player.sendOverlayMessage(Component.translatable("message.jugcraft.drying_rack.full"));
			return InteractionResult.CONSUME;
		}
		BlockPos master = masterPos(pos, state);
		show(level, master);
		level.scheduleTick(master, this, DRY_TICKS);
		level.playSound(null, pos, SoundEvents.AZALEA_LEAVES_PLACE, SoundSource.BLOCKS, 0.7F, 1.0F);
		level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
		return InteractionResult.SUCCESS;
	}

	/** Takes down the first dried bunch, or the last one hung. */
	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		DryingRackBlockEntity rack = rack(level, pos, state);
		if (level.isClientSide() || rack == null) {
			return InteractionResult.SUCCESS;
		}
		ItemStack taken = rack.takeDown(level.getGameTime());
		if (taken.isEmpty()) {
			return InteractionResult.PASS;
		}
		player.getInventory().placeItemBackInInventory(taken, Prediction.SERVER_ONLY);
		BlockPos master = masterPos(pos, state);
		show(level, master);
		level.playSound(null, pos, SoundEvents.AZALEA_LEAVES_BREAK, SoundSource.BLOCKS, 0.7F, 1.2F);
		level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
		return InteractionResult.SUCCESS;
	}

	/** A bunch has dried: shows it, and waits for the next still drying. */
	@Override
	protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		if (part(state) != MASTER || !(level.getBlockEntity(pos) instanceof DryingRackBlockEntity rack)) {
			return;
		}
		show(level, pos);
		long next = rack.untilNextDry(level.getGameTime());
		if (next > 0) {
			level.scheduleTick(pos, this, (int) next);
		}
	}

	/** Shows each block's rail's two bunches of the rack whose first block is at {@code master}, fresh or dried, as they are now. */
	public void show(Level level, BlockPos master) {
		BlockState masterState = level.getBlockState(master);
		if (!masterState.is(this) || !(level.getBlockEntity(master) instanceof DryingRackBlockEntity rack)) {
			return;
		}
		long now = level.getGameTime();
		Direction facing = masterState.getValue(FACING);
		for (int part = 0; part < CELLS.length; part++) {
			BlockPos at = partPos(master, facing, part);
			BlockState there = level.getBlockState(at);
			if (there.is(this) && part(there) == part && there.getValue(FACING) == facing) {
				BlockState shown = there.setValue(RIGHT, rack.shown(2 * part, now)).setValue(LEFT, rack.shown(2 * part + 1, now));
				if (shown != there) {
					level.setBlock(at, shown, Block.UPDATE_CLIENTS);
				}
			}
		}
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(LEFT, RIGHT);
	}
}

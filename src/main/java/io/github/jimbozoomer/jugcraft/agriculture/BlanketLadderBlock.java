package io.github.jimbozoomer.jugcraft.agriculture;

import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/**
 * The Blanket Ladder (Cozy Fall Hearth, part 4; tools/hearth.py LADDER): a wooden ladder 1.75 m high leaning on a wall,
 * one block across and two up ({@link MultiDecorationBlock}), its feet out from the wall and its top against it, with
 * five turned rungs. It leans on the wall the player aims at (its upper block needs a sturdy wall behind it) and comes off
 * when the wall goes. Use a wool carpet of any colour on it to fold it over a rung as a throw, the top rung first, up to
 * {@value #RUNGS} ({@link BlanketLadderBlockEntity}, on its first block); an empty hand takes back the one hung last. Each
 * is drawn by client/BlanketLadderRenderer in its carpet's colour. Broken, it gives them back.
 */
public class BlanketLadderBlock extends MultiDecorationBlock implements EntityBlock {
	public static final TagKey<Item> CARPETS = TagKey.create(Registries.ITEM, Identifier.withDefaultNamespace("wool_carpets"));
	public static final int RUNGS = 3;
	/** Its blocks: 1 across, 2 up, 1 deep (tools/hearth.py LADDER["size"]); its top leans on the wall from part 1. */
	private static final int[][] CELLS = box(1, 2, 1);
	private static final int TOP = 1;
	public static final IntegerProperty PART = IntegerProperty.create("part", 0, CELLS.length - 1);
	private static final Map<Direction, VoxelShape[]> SHAPES = shapesOf(CELLS, new double[] {2.5, 0.0, 3.0, 13.5, 29.0, 16.0});

	public BlanketLadderBlock(Properties properties) {
		super(properties);
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

	/** Whether the top's block at {@code topPos}, facing {@code facing}, has a sturdy wall behind it to lean on. */
	private static boolean leaning(LevelReader level, BlockPos topPos, Direction facing) {
		BlockPos wall = topPos.relative(facing.getOpposite());
		return level.getBlockState(wall).isFaceSturdy(level, wall, facing);
	}

	/** Leant on the wall the player aims at (or the nearest they look towards), the block above free. */
	@Override
	public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockState placed = super.getStateForPlacement(context);
		if (placed == null) {
			return null;
		}
		for (Direction direction : context.getNearestLookingDirections()) {
			if (direction.getAxis().isHorizontal()) {
				Direction facing = direction.getOpposite();
				if (leaning(context.getLevel(), partPos(context.getClickedPos(), facing, TOP), facing)) {
					return placed.setValue(FACING, facing);
				}
			}
		}
		return null;
	}

	@Override
	protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		return part(state) != TOP || leaning(level, pos, state.getValue(FACING));
	}

	/** Its wall gone, it falls, and the rest with it. */
	@Override
	protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction direction,
			BlockPos neighborPos, BlockState neighbor, RandomSource random) {
		return direction == state.getValue(FACING).getOpposite() && !state.canSurvive(level, pos) ? Blocks.AIR.defaultBlockState()
				: super.updateShape(state, level, ticks, pos, direction, neighborPos, neighbor, random);
	}

	/** A carpet is hung over the next free rung; anything else does what it would on any block. */
	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
			BlockHitResult hit) {
		if (!stack.is(CARPETS)) {
			return stack.isEmpty() ? InteractionResult.TRY_WITH_EMPTY_HAND : InteractionResult.PASS;
		}
		if (!level.isClientSide() && level.getBlockEntity(masterPos(pos, state)) instanceof BlanketLadderBlockEntity ladder) {
			if (!ladder.hang(stack)) {
				player.sendOverlayMessage(Component.translatable("message.jugcraft.blanket_ladder.full"));
				return InteractionResult.SUCCESS;
			}
			stack.consume(1, player);
			level.playSound(null, pos, SoundEvents.WOOL_PLACE, SoundSource.BLOCKS, 0.8F, 1.0F);
			level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
		}
		return InteractionResult.SUCCESS;
	}

	/** An empty hand takes back the throw hung last. */
	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (!(level.getBlockEntity(masterPos(pos, state)) instanceof BlanketLadderBlockEntity ladder) || ladder.throwsOn().isEmpty()) {
			return InteractionResult.PASS;
		}
		if (!level.isClientSide()) {
			ItemStack taken = ladder.takeLast();
			if (!player.getInventory().add(taken)) {
				Block.popResource(level, pos, taken);
			}
			level.playSound(null, pos, SoundEvents.WOOL_HIT, SoundSource.BLOCKS, 0.8F, 1.0F);
			level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return part(state) == MASTER ? new BlanketLadderBlockEntity(pos, state) : null;
	}
}

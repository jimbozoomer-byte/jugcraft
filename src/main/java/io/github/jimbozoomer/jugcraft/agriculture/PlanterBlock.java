package io.github.jimbozoomer.jugcraft.agriculture;

import java.util.EnumMap;
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
import net.minecraft.world.item.BlockItem;
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
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/**
 * A planter of the fall garden (Fall Garden & Porch, part 2; tools/fall_garden.py PLANTERS): the Window Box on a wall,
 * the Half-Barrel and the Galvanized Tub on the floor. Use a small flower ({@code #minecraft:small_flowers}) on it to
 * plant it in the next free spot ({@link Planter#spots}), up to as many as it has; use it with an empty hand to take back
 * the one planted last. What grows in it is kept by its {@link PlanterBlockEntity}, on the server, and drawn by
 * client/PlanterRenderer from each flower's own block model; breaking it gives them back. Its own model is
 * tools/fall_garden_data.py's.
 */
public class PlanterBlock extends HorizontalDirectionalBlock implements EntityBlock {
	/**
	 * A planter's make, drawn facing north (a wall planter's wall behind it, to the south), in pixels: its shape's boxes
	 * {x0, y0, z0, x1, y1, z1}, its spots {x, z}, the soil's height, how big a flower stands in it, and whether it hangs on
	 * a wall.
	 */
	public record Planter(double[][] boxes, double[][] spots, double soil, float scale, boolean onWall) {
	}

	public static final Planter WINDOW_BOX = new Planter(new double[][] {{0.5, 0.0, 12.0, 15.5, 4.0, 16.0}},
			new double[][] {{3.0, 14.0}, {8.0, 14.0}, {13.0, 14.0}}, 3.5, 0.5F, true);
	public static final Planter HALF_BARREL = new Planter(new double[][] {{3.0, 0.0, 4.0, 13.0, 7.25, 12.0}, {4.0, 0.0, 3.0, 12.0, 7.25, 13.0}},
			new double[][] {{8.0, 8.0}}, 6.5, 0.9F, false);
	public static final Planter GALVANIZED_TUB = new Planter(new double[][] {{2.0, 0.0, 5.0, 14.0, 4.5, 11.0}, {3.0, 0.0, 4.0, 13.0, 4.5, 12.0}},
			new double[][] {{5.5, 8.0}, {10.5, 8.0}}, 3.75, 0.65F, false);

	/** What grows in a planter: vanilla's small flowers tag (26.3 has no ItemTags field for it). */
	public static final TagKey<Item> SMALL_FLOWERS = TagKey.create(Registries.ITEM, Identifier.withDefaultNamespace("small_flowers"));

	private final Planter planter;
	private final Map<Direction, VoxelShape> shapes = new EnumMap<>(Direction.class);

	public PlanterBlock(Properties properties, Planter planter) {
		super(properties);
		this.planter = planter;
		for (Direction facing : Direction.Plane.HORIZONTAL) {
			shapes.put(facing, LongDecorationBlock.shape(planter.boxes(), facing));
		}
		registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
	}

	public Planter planter() {
		return planter;
	}

	/** Whether {@code stack} can be planted in a planter: a small flower that is a block. */
	public static boolean plantable(ItemStack stack) {
		return stack.is(SMALL_FLOWERS) && stack.getItem() instanceof BlockItem;
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return shapes.get(state.getValue(FACING));
	}

	/** A wall planter hangs on the wall the player aims at; the others stand facing the player. */
	@Override
	public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
		if (!planter.onWall()) {
			return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
		}
		for (Direction direction : context.getNearestLookingDirections()) {
			if (direction.getAxis().isHorizontal()) {
				BlockState state = defaultBlockState().setValue(FACING, direction.getOpposite());
				if (state.canSurvive(context.getLevel(), context.getClickedPos())) {
					return state;
				}
			}
		}
		return null;
	}

	@Override
	protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		if (!planter.onWall()) {
			return true;
		}
		Direction facing = state.getValue(FACING);
		BlockPos wall = pos.relative(facing.getOpposite());
		return level.getBlockState(wall).isFaceSturdy(level, wall, facing);
	}

	@Override
	protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction direction,
			BlockPos neighborPos, BlockState neighbor, RandomSource random) {
		return planter.onWall() && direction == state.getValue(FACING).getOpposite() && !state.canSurvive(level, pos)
				? Blocks.AIR.defaultBlockState() : super.updateShape(state, level, ticks, pos, direction, neighborPos, neighbor, random);
	}

	/** Plants a small flower in the next free spot; anything else but an empty hand does what it would on any block. */
	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
			BlockHitResult hit) {
		if (!plantable(stack)) {
			return stack.isEmpty() ? InteractionResult.TRY_WITH_EMPTY_HAND : InteractionResult.PASS;
		}
		if (level.isClientSide()) {
			return InteractionResult.SUCCESS;
		}
		if (!(level.getBlockEntity(pos) instanceof PlanterBlockEntity planted)) {
			return InteractionResult.PASS;
		}
		if (!planted.plant(stack)) {
			// (not PASS or FAIL: either would go on to place the flower in front of it)
			player.sendOverlayMessage(Component.translatable("message.jugcraft.planter.full"));
			return InteractionResult.CONSUME;
		}
		if (!player.getAbilities().instabuild) {
			stack.shrink(1);
		}
		level.playSound(null, pos, SoundEvents.ROOTED_DIRT_PLACE, SoundSource.BLOCKS, 0.8F, 1.0F);
		level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
		return InteractionResult.SUCCESS;
	}

	/** Takes back the flower planted last. */
	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (!(level.getBlockEntity(pos) instanceof PlanterBlockEntity planted) || planted.plants().isEmpty()) {
			return InteractionResult.PASS;
		}
		if (level.isClientSide()) {
			return InteractionResult.SUCCESS;
		}
		ItemStack taken = planted.takeLast();
		if (!player.getInventory().add(taken)) {
			Block.popResource(level, pos.above(), taken);
		}
		level.playSound(null, pos, SoundEvents.ROOTED_DIRT_BREAK, SoundSource.BLOCKS, 0.8F, 1.0F);
		level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
		return InteractionResult.SUCCESS;
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new PlanterBlockEntity(pos, state);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}
}

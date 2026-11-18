package io.github.jimbozoomer.jugcraft.agriculture;

import io.github.jimbozoomer.jugcraft.finance.LandRegistry;
import io.github.jimbozoomer.jugcraft.town.TownProtection;
import java.util.Map;
import java.util.function.BiConsumer;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/**
 * The Harvest Photo Frame Backdrop (Thanksgiving festival toys, tools/thanksgiving_toys.py BACKDROP;
 * docs/features/thanksgiving-toys.md): a broad timber picture frame dressed for the harvest, {@value #ACROSS} blocks
 * across and {@value #UP} up, whose open middle (two blocks wide and two high, above a low sill) two people stand in
 * for a picture. Its {@value #PARTS} blocks are placed and broken as one ({@link MultiDecorationBlock}): placing it
 * needs every block it fills free (it never replaces anything) and, for the player placing it, every one of them theirs
 * to build on (claims and towns), or nothing is placed; breaking any block takes the whole frame down and drops it once,
 * from its first block. The middle two blocks of the middle row are not part of it, so nothing there, seen or unseen,
 * stands in anyone's way; the sill's outline is its own two pixels. Using it switches the string of bulbs under its top
 * rail on and off (light {@value #LIGHT} on the two middle blocks of the top rail while lit).
 *
 * <p>Since breaking one block brings down all ten, the frame is protected as a whole wherever any one of its blocks is
 * (a land claim made after it was placed, a town, an operator region): a player who may not build at every one of its
 * blocks can neither break any of them nor make progress breaking one, in survival or in creative
 * ({@link #register}'s before-break check, {@link #getDestroyProgress}); and an explosion that reaches an unprotected
 * block of it leaves the whole frame alone while any block of it is shielded ({@link #onExplosionHit}), as claimed and
 * town blocks themselves are. Its blocks count as solid (FestivalToys gives them forceSolidOn), so flowing water or lava
 * and an emptied bucket cannot wash its thin top rail or sill away and bring the rest down with them. Pistons cannot move
 * it (IMMOVEABLE) and fire cannot burn it (it is not flammable).
 */
public class FestivalToyBackdropBlock extends MultiDecorationBlock {
	public static final int ACROSS = 4;
	public static final int UP = 3;
	public static final int LIGHT = 10;
	/** Each part's {right, up}: the left post from the bottom up, the top rail, the right post from the top down, the sill. */
	private static final int[][] CELLS = {{0, 0}, {0, 1}, {0, 2}, {1, 2}, {2, 2}, {3, 2}, {3, 1}, {3, 0}, {1, 0}, {2, 0}};
	public static final int PARTS = 10;
	/** The parts carrying the string of bulbs. */
	public static final int BULBS_LEFT = 3;
	public static final int BULBS_RIGHT = 4;
	public static final IntegerProperty PART = IntegerProperty.create("part", 0, PARTS - 1);
	/** Its outline in its frame (pixels, facing north): the posts with their feet, the top rail, the sill, the harvest at each foot. */
	private static final Map<Direction, VoxelShape[]> SHAPES = shapesOf(CELLS, new double[] {8.5, 0.0, 2.0, 16.0, 48.0, 14.5},
			new double[] {48.0, 0.0, 2.0, 55.5, 48.0, 14.5}, new double[] {8.5, 40.0, 4.0, 55.5, 48.0, 10.5},
			new double[] {16.0, 0.0, 5.0, 48.0, 2.0, 9.5}, new double[] {0.5, 0.0, 1.5, 8.5, 9.5, 13.0},
			new double[] {55.5, 0.0, 2.5, 63.5, 11.5, 13.5});

	public FestivalToyBackdropBlock(Properties properties) {
		super(properties);
	}

	/**
	 * The before-break check (registered once, at start): a player who may not build at every block of a frame cannot
	 * break any block of it, in survival or creative. Operators in creative and players trusted on every block are let
	 * through, as LandRegistry and TownProtection decide.
	 */
	public static void register() {
		PlayerBlockBreakEvents.BEFORE.register((level, player, pos, state, blockEntity) -> {
			if (state.getBlock() instanceof FestivalToyBackdropBlock frame && !frame.mayChangeAll(level, pos, state, player)) {
				frame.tellProtected(level, pos, state, player);
				return false;
			}
			return true;
		});
	}

	/** Where every block of the frame {@code pos} belongs to is, whether or not each is still there. */
	private BlockPos[] partsOf(BlockPos pos, BlockState state) {
		BlockPos master = masterPos(pos, state);
		Direction facing = state.getValue(FACING);
		BlockPos[] out = new BlockPos[CELLS.length];
		for (int part = 0; part < CELLS.length; part++) {
			out[part] = partPos(master, facing, part);
		}
		return out;
	}

	/** Whether {@code player} may build at every block of the frame that {@code pos} belongs to (decided on the server). */
	public boolean mayChangeAll(Level level, BlockPos pos, BlockState state, Player player) {
		if (level.isClientSide()) {
			return true;
		}
		for (BlockPos at : partsOf(pos, state)) {
			if (!LandRegistry.mayBuild(player, level, at) || TownProtection.denies(player, level, at)) {
				return false;
			}
		}
		return true;
	}

	/** Whether any block of the frame is shielded from explosions: claimed land, a town or an operator region. */
	public boolean anyPartShielded(Level level, BlockPos pos, BlockState state) {
		for (BlockPos at : partsOf(pos, state)) {
			if (TownProtection.shieldsBlock(level, at) || LandRegistry.isClaimed(level, at)) {
				return true;
			}
		}
		return false;
	}

	/** Says why the frame cannot be broken: whose land one of its blocks stands on, or that it is in a protected town. */
	private void tellProtected(Level level, BlockPos pos, BlockState state, Player player) {
		for (BlockPos at : partsOf(pos, state)) {
			if (!LandRegistry.mayBuild(player, level, at)) {
				player.sendOverlayMessage(Component.literal("This land belongs to " + LandRegistry.ownerAt(level, at) + "."));
				return;
			}
		}
		player.sendOverlayMessage(Component.translatable("message.jugcraft.town.protected"));
	}

	/** A player who may not build at every block of it makes no progress breaking any of them. */
	@Override
	protected float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
		if (level instanceof Level world && !mayChangeAll(world, pos, state, player)) {
			return 0.0F;
		}
		return super.getDestroyProgress(state, player, level, pos);
	}

	/**
	 * An explosion takes the frame only where none of its blocks is shielded; while one is, the whole frame stays (an
	 * explosion cannot reach a claimed or town block of it by breaking an unprotected one).
	 */
	@Override
	protected void onExplosionHit(BlockState state, ServerLevel level, BlockPos pos, Explosion explosion, BiConsumer<ItemStack, BlockPos> drops) {
		if (anyPartShielded(level, pos, state)) {
			return;
		}
		super.onExplosionHit(state, level, pos, explosion, drops);
	}

	/** The light a part gives: {@value #LIGHT} on the bulbs' two blocks while lit. */
	public static int light(BlockState state) {
		int part = state.getValue(PART);
		return state.getValue(LIT) && (part == BULBS_LEFT || part == BULBS_RIGHT) ? LIGHT : 0;
	}

	@Override
	public int[][] cells() {
		return CELLS;
	}

	@Override
	public IntegerProperty partProperty() {
		return PART;
	}

	/**
	 * As every MultiDecorationBlock, placed only where every block it fills is free; and, for a player, only where every
	 * one of them is theirs to build on, so a frame placed in their own land never reaches into a neighbour's claim or a
	 * protected town.
	 */
	@Override
	public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockState placed = super.getStateForPlacement(context);
		Player player = context.getPlayer();
		if (placed == null || player == null) {
			return placed;
		}
		Level level = context.getLevel();
		for (int part = 0; part < CELLS.length; part++) {
			BlockPos at = partPos(context.getClickedPos(), placed.getValue(FACING), part);
			if (!LandRegistry.mayBuild(player, level, at) || TownProtection.denies(player, level, at)) {
				return null;
			}
		}
		return placed;
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPES.get(state.getValue(FACING))[state.getValue(PART)];
	}
}

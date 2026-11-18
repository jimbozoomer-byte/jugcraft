package io.github.jimbozoomer.jugcraft.agriculture;

import io.github.jimbozoomer.jugcraft.finance.LandRegistry;
import io.github.jimbozoomer.jugcraft.town.TownProtection;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/**
 * The Timber Harvest Arch (Thanksgiving harvest town details; tools/thanksgiving_town.py ARCH): a farm gateway of hewn oak,
 * {@value #WIDTH} blocks across and {@value #HEIGHT} up. Two posts on fieldstone plinths, dressed with tied corn stalks,
 * hung ears of ornamental corn and a pumpkin at each foot, carry a pegged beam on knee braces, with a wheat sheaf and two
 * pumpkins on top and swags of autumn leaves along its front. Its model is tools/thanksgiving_town_data.py
 * timber_harvest_arch(), cut into its parts' models.
 *
 * <p>Placed and broken as one ({@link MultiDecorationBlock}), from the block aimed at (its left post, as the player sees
 * it) to the right. The opening between the posts, {@value #OPENING} blocks across and {@value #CLEAR} up, has no block
 * at all, so nothing in it can be bumped into; only the posts and the beam above the opening have blocks. Placing it is
 * all or nothing: it is refused, and nothing is changed, unless every one of its blocks is free (replaceable, with no
 * water or other fluid in it), loaded, clear of entities and a block the placer may build at (land claims and town
 * protection), and both posts stand on firm ground. Breaking any of its blocks brings the whole arch down, and it drops
 * once (its loot is on its first block only). A survival player who may not build at every one of its blocks cannot
 * break it. It has nothing to use: no lanterns ({@link #LIT} stays on), no storage.
 */
public class HarvestTownArchBlock extends MultiDecorationBlock {
	public static final int WIDTH = 5;
	public static final int HEIGHT = 4;
	public static final int OPENING = 3;
	public static final int CLEAR = 3;
	/** Its blocks: the left post from the bottom up, the beam from left to right, then the right post from the top down. */
	private static final int[][] CELLS = {{0, 0}, {0, 1}, {0, 2}, {0, 3}, {1, 3}, {2, 3}, {3, 3}, {4, 3}, {4, 2}, {4, 1}, {4, 0}};
	public static final IntegerProperty PART = IntegerProperty.create("part", 0, CELLS.length - 1);
	/** Its solid boxes in its frame (tools/thanksgiving_town.py ARCH["shapes"]): plinths, posts and corn, beam, crest. */
	private static final Map<Direction, VoxelShape[]> SHAPES = shapesOf(CELLS, new double[] {0.5, 0.0, 0.0, 12.0, 4.5, 12.0},
			new double[] {0.5, 4.5, 3.0, 11.0, 50.0, 11.0}, new double[] {68.0, 0.0, 0.0, 79.5, 4.5, 12.0},
			new double[] {69.0, 4.5, 3.0, 79.5, 50.0, 11.0}, new double[] {0.5, 50.0, 3.5, 79.5, 57.0, 12.0},
			new double[] {31.0, 57.0, 5.0, 49.0, 64.0, 11.0});

	public HarvestTownArchBlock(Properties properties) {
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

	/**
	 * Every block it needs must be free, as {@link MultiDecorationBlock} asks, and more: no fluid in it (it never takes the
	 * place of water), loaded, clear of entities, the placer's to build on, and firm ground under both posts. Otherwise it
	 * is refused and nothing changes.
	 */
	@Override
	public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockState state = super.getStateForPlacement(context);
		if (state == null) {
			return null;
		}
		Level level = context.getLevel();
		Player player = context.getPlayer();
		Direction facing = state.getValue(FACING);
		BlockPos master = context.getClickedPos();
		for (int part = 0; part < CELLS.length; part++) {
			BlockPos pos = partPos(master, facing, part);
			if (!level.hasChunkAt(pos) || !level.getFluidState(pos).isEmpty()
					|| !level.getBlockState(pos).canBeReplaced(BlockPlaceContext.at(context, pos, Direction.UP))
					|| !level.isUnobstructed(state.setValue(PART, part), pos, CollisionContext.empty())
					|| player != null && !mayChange(player, level, pos, context)) {
				return null;
			}
			if (CELLS[part][1] == 0) {
				BlockPos ground = pos.below();
				if (!level.getBlockState(ground).isFaceSturdy(level, ground, Direction.UP)) {
					return null;
				}
			}
		}
		return state;
	}

	private static boolean mayChange(Player player, Level level, BlockPos pos, BlockPlaceContext context) {
		return player.mayUseItemAt(pos, Direction.UP, context.getItemInHand())
				&& (level.isClientSide() || LandRegistry.mayBuild(player, level, pos) && !TownProtection.denies(player, level, pos));
	}

	/** Whether {@code player} may build at every block of the arch that {@code pos} belongs to (decided on the server). */
	public boolean mayChangeAll(Level level, BlockPos pos, BlockState state, Player player) {
		if (level.isClientSide()) {
			return true;
		}
		BlockPos master = masterPos(pos, state);
		Direction facing = state.getValue(FACING);
		for (int part = 0; part < CELLS.length; part++) {
			BlockPos at = partPos(master, facing, part);
			if (!LandRegistry.mayBuild(player, level, at) || TownProtection.denies(player, level, at)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Breaking one block brings down all of them, so a player who may not build at every one of them makes no progress
	 * breaking any: a claim or town that holds one of its blocks protects the whole arch.
	 */
	@Override
	protected float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
		if (level instanceof Level world && !mayChangeAll(world, pos, state, player)) {
			return 0.0F;
		}
		return super.getDestroyProgress(state, player, level, pos);
	}

	/** It has nothing to use. */
	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		return InteractionResult.PASS;
	}
}

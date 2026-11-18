package io.github.jimbozoomer.jugcraft.agriculture;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;

/**
 * The High-Backed Oak Settle (Thanksgiving homestead, tools/thanksgiving_homestead.py PROPS, kind "settle"): a
 * {@link HomesteadFurnitureBlock} two blocks across and two up, its two lower blocks each a seat ({@link Seat}). Using
 * either block of a half with an empty hand sits the player on that half's squab, at {@link #SEAT} above the floor; one
 * sitter a seat, and a sneaking player does not sit. Each half is one seat whichever of its blocks is used: its lower
 * block is its canonical cell ({@link #seatCell}), where a player's Seat stands and a companion's reservation is kept
 * (CompanionSeats), so a player and a companion, or two companions, never share a squab. Broken from any block, it
 * stands its sitters up and forgets any companion's reservation at once. Like every other seat, using it goes through
 * the normal block-use path, so land and town protection apply (in a protected town it is a public seat: TownProtection
 * runs its empty-hand use).
 */
public class HomesteadSettleBlock extends HomesteadFurnitureBlock implements Seat.Sittable {
	/** Where the sitter sits, in blocks above the floor: the top of the squab (tools/thanksgiving_homestead.py "seat"). */
	public static final double SEAT = 8.5 / 16.0;

	public HomesteadSettleBlock(Properties properties, double[]... boxes) {
		super(properties, boxes);
	}

	/**
	 * Above a lower block's base, the squab's top; an upper block's seat is the one below it, so anything seating at an
	 * upper block (a companion choosing seats by {@link Seat.Sittable}) sits on the squab, not on the back.
	 */
	@Override
	public double seatHeight(BlockState state) {
		return lower(part(state)) ? SEAT : SEAT - 1.0;
	}

	/** The lower block of the half {@code pos} belongs to: the seat. */
	public BlockPos seatPos(BlockPos pos, BlockState state) {
		return partPos(masterPos(pos, state), state.getValue(FACING), part(state) % 2);
	}

	/** Either block of a half seats at its lower block. */
	@Override
	public BlockPos seatCell(BlockGetter level, BlockPos pos, BlockState state) {
		return state.is(this) ? seatPos(pos, state) : pos;
	}

	/** Broken (from any of its blocks), each seat stands its sitter up and is forgotten by the companions at once. */
	@Override
	protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
		super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
		if (lower(part(state)) && !level.getBlockState(pos).is(this)) {
			for (Seat seat : level.getEntitiesOfClass(Seat.class, new AABB(pos))) {
				seat.ejectPassengers();
				seat.discard();
			}
			local.peepo.CompanionSeats.release(level, pos);
		}
	}

	/** Sits the player on the seat of the half they used. */
	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (player.isSecondaryUseActive()) {
			return InteractionResult.PASS;
		}
		BlockPos seat = seatPos(pos, state);
		BlockState seated = level.getBlockState(seat);
		if (!seated.is(this) || !lower(part(seated))) {
			return InteractionResult.PASS;
		}
		if (level instanceof ServerLevel server && !Seat.sit(server, seat, seated, player)) {
			return InteractionResult.PASS;
		}
		return InteractionResult.SUCCESS;
	}
}

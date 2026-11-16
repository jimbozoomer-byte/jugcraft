package io.github.jimbozoomer.jugcraft.town.guests;

import io.github.jimbozoomer.jugcraft.boardgames.TableBlock;
import io.github.jimbozoomer.jugcraft.concordance.Authority;
import io.github.jimbozoomer.jugcraft.finance.LandRegistry;
import io.github.jimbozoomer.jugcraft.town.JugcraftTown;
import io.github.jimbozoomer.jugcraft.town.TownProtection;
import io.github.jimbozoomer.jugcraft.town.Townsfolk;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;

/**
 * Where an outing happens (worker C): a stand spot for the guest, found near the inviting player in loaded chunks only,
 * nearest first. Nothing here changes, places or opens anything, and nothing loads a chunk: every block is read only
 * after {@link #loaded} says its chunk is already loaded.
 *
 * <ul>
 * <li><b>Fishing</b>: a spot on the bank beside open surface water (a water source with nothing above it, and at least
 * {@link #OPEN_WATER} such sources in the 3x3 round it), at the spot's level or one below.</li>
 * <li><b>Fair</b>: open ground: a spot with at least {@link #OPEN_GROUND} of its four neighbours standable too, at least
 * {@link #FAIR_MIN} blocks from the player.</li>
 * <li><b>Board game</b>: a spot beside a table-like block (a board-game table, or any block whose id ends in
 * {@code table}); otherwise the player's own spot. The table is never used or opened.</li>
 * </ul>
 * A spot is {@link #standable} (a sturdy, safe floor and two empty, dry, safe blocks over it) and free (no other entity in
 * it, and not within {@link #APART} of another guest's activity anchor). Every block it uses (the spot, its head and
 * floor, the water or the table) must be {@link #open} for the inviting player: protected for neither party. The last
 * question, {@link Authority#mayChange}, is asked at most {@link #AUTHORITY_CALLS} times a search.
 */
final class GuestOutingPlaces {
	/** How far from the player's feet a place is looked for: sideways, and up or down. */
	static final int RADIUS = 12, HEIGHT = 4;
	/** Surface water sources in the 3x3 round the water fished (itself included) that make it open water. */
	static final int OPEN_WATER = 3;
	/** Standable neighbours (of four) that make a fair spot open ground. */
	static final int OPEN_GROUND = 3;
	/** A fair spot is at least this many blocks from the player. */
	static final int FAIR_MIN = 2;
	/** At most this many final {@link Authority#mayChange} questions per search. */
	static final int AUTHORITY_CALLS = 16;
	/** A place is never this near (blocks) another guest's activity anchor. */
	static final double APART = 1.5;

	/** Offsets within a radius, nearest first (then by height, x, z, so the choice is the same every time). */
	private static final Map<Integer, List<BlockPos>> OFFSETS = new ConcurrentHashMap<>();

	private GuestOutingPlaces() {
	}

	/** Where {@code kind} could happen near {@code player} for {@code npc}, or null when nowhere near will do. */
	static @Nullable BlockPos find(GuestOutings.Kind kind, ServerLevel level, ServerPlayer player, Townsfolk npc, int radius) {
		Search search = new Search(level, player, npc);
		BlockPos origin = player.blockPosition();
		for (BlockPos offset : offsets(radius)) {
			if (search.exhausted()) {
				return null;
			}
			BlockPos feet = origin.offset(offset);
			BlockPos found = switch (kind) {
				case FISHING -> fishing(search, feet);
				case FAIR -> fair(search, feet, origin);
				case BOARD_GAME -> boardGame(search, feet);
				case VISIT_HOME -> null;
			};
			if (found != null) {
				return found;
			}
		}
		// A board game needs no table: the guest comes to the player's own spot.
		if (kind == GuestOutings.Kind.BOARD_GAME && standable(level, origin) && search.free(origin) && search.place(origin)) {
			return origin;
		}
		return null;
	}

	private static @Nullable BlockPos fishing(Search search, BlockPos feet) {
		if (!standable(search.level, feet)) {
			return null;
		}
		for (Direction side : Direction.Plane.HORIZONTAL) {
			BlockPos beside = feet.relative(side);
			for (BlockPos water : List.of(beside, beside.below())) {
				if (openWater(search.level, water) && search.free(feet) && search.place(feet, water)) {
					return feet;
				}
			}
		}
		return null;
	}

	private static @Nullable BlockPos fair(Search search, BlockPos feet, BlockPos origin) {
		if (feet.distSqr(origin) < FAIR_MIN * FAIR_MIN || !standable(search.level, feet)) {
			return null;
		}
		int open = 0;
		for (Direction side : Direction.Plane.HORIZONTAL) {
			if (standable(search.level, feet.relative(side))) {
				open++;
			}
		}
		return open >= OPEN_GROUND && search.free(feet) && search.place(feet) ? feet : null;
	}

	private static @Nullable BlockPos boardGame(Search search, BlockPos feet) {
		if (!standable(search.level, feet)) {
			return null;
		}
		for (Direction side : Direction.Plane.HORIZONTAL) {
			BlockPos table = feet.relative(side);
			if (loaded(search.level, table) && tableLike(search.level.getBlockState(table)) && search.free(feet) && search.place(feet, table)) {
				return feet;
			}
		}
		return null;
	}

	/** A board-game table, or any block whose id ends in "table" (crafting, cartography, smithing tables, furniture). */
	static boolean tableLike(BlockState state) {
		return state.getBlock() instanceof TableBlock || BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath().endsWith("table");
	}

	/** A water source with nothing solid or wet above it. */
	static boolean surfaceWater(ServerLevel level, BlockPos pos) {
		if (!loaded(level, pos) || !loaded(level, pos.above())) {
			return false;
		}
		FluidState fluid = level.getFluidState(pos);
		BlockState above = level.getBlockState(pos.above());
		return fluid.is(FluidTags.WATER) && fluid.isSource() && above.getFluidState().isEmpty()
				&& above.getCollisionShape(level, pos.above()).isEmpty();
	}

	/** Surface water with at least {@link #OPEN_WATER} surface sources in the 3x3 round it. */
	static boolean openWater(ServerLevel level, BlockPos pos) {
		if (!surfaceWater(level, pos)) {
			return false;
		}
		int count = 0;
		for (int dx = -1; dx <= 1; dx++) {
			for (int dz = -1; dz <= 1; dz++) {
				if (surfaceWater(level, pos.offset(dx, 0, dz))) {
					count++;
				}
			}
		}
		return count >= OPEN_WATER;
	}

	/** In the world and its border, and in a chunk that is already loaded (never loads one). */
	static boolean loaded(ServerLevel level, BlockPos pos) {
		return level.isInWorldBounds(pos) && level.getWorldBorder().isWithinBounds(pos) && level.isLoaded(pos);
	}

	/**
	 * Whether a guest could stand with its feet in {@code feet}: all three blocks loaded; a floor whose top face is sturdy,
	 * dry and not dangerous; feet and head blocks without collision, fluid or danger (fire, berry bushes, powder snow,
	 * wither roses, cobwebs). A player-sized body centred there then touches only those two blocks.
	 */
	static boolean standable(ServerLevel level, BlockPos feet) {
		BlockPos floor = feet.below();
		BlockPos head = feet.above();
		if (!loaded(level, floor) || !loaded(level, feet) || !loaded(level, head)) {
			return false;
		}
		BlockState ground = level.getBlockState(floor);
		if (!ground.isFaceSturdy(level, floor, Direction.UP) || !ground.getFluidState().isEmpty()
				|| JugcraftTown.TOWNSFOLK.isBlockDangerous(ground) || ground.getBlock() instanceof TableBlock) {
			return false;
		}
		for (BlockPos cell : List.of(feet, head)) {
			BlockState state = level.getBlockState(cell);
			if (!state.getCollisionShape(level, cell).isEmpty() || !state.getFluidState().isEmpty()
					|| JugcraftTown.TOWNSFOLK.isBlockDangerous(state) || state.is(Blocks.COBWEB)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Free to stand in: no entity in the spot's two blocks but those named (the inviting player, the guest), and no other
	 * guest's activity anchor within {@link #APART}.
	 */
	static boolean free(ServerLevel level, BlockPos feet, @Nullable Entity player, @Nullable Entity guest) {
		AABB space = new AABB(feet.getX(), feet.getY(), feet.getZ(), feet.getX() + 1, feet.getY() + 2, feet.getZ() + 1);
		if (!level.getEntities((Entity) null, space, e -> e != player && e != guest && !e.isSpectator() && !e.isRemoved()).isEmpty()) {
			return false;
		}
		for (GuestActivity.Activity other : GuestActivity.get(level.getServer()).all().values()) {
			if (other.dimension().equals(level.dimension()) && other.anchor().closerThan(feet, APART)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Protected for neither party: the block is shielded by no town or operator region (whoever asks: no creative
	 * exemption), the player may build in any claim there ({@link LandRegistry#mayBuildFor}: their own, trusted, or
	 * unclaimed; no admins' bypass), and spawn protection lets them act there. Cheap: hash lookups only.
	 */
	static boolean open(ServerLevel level, ServerPlayer player, BlockPos pos) {
		return !TownProtection.shieldsBlock(level, pos) && LandRegistry.mayBuildFor(player.getUUID(), level, pos) && level.mayInteract(player, pos);
	}

	/** Offsets within {@code radius} sideways and {@link #HEIGHT} up or down, nearest first. */
	private static List<BlockPos> offsets(int radius) {
		return OFFSETS.computeIfAbsent(radius, r -> {
			List<BlockPos> all = new ArrayList<>();
			for (int dy = -HEIGHT; dy <= HEIGHT; dy++) {
				for (int dx = -r; dx <= r; dx++) {
					for (int dz = -r; dz <= r; dz++) {
						all.add(new BlockPos(dx, dy, dz));
					}
				}
			}
			all.sort(Comparator.<BlockPos>comparingInt(p -> p.getX() * p.getX() + p.getY() * p.getY() + p.getZ() * p.getZ())
					.thenComparingInt(p -> Math.abs(p.getY())).thenComparingInt(BlockPos::getY)
					.thenComparingInt(BlockPos::getX).thenComparingInt(BlockPos::getZ));
			return List.copyOf(all);
		});
	}

	/** One search: what it is for, the anchors other activities hold, and how many final questions it has asked. */
	private static final class Search {
		final ServerLevel level;
		final ServerPlayer player;
		final Townsfolk npc;
		int asked;

		Search(ServerLevel level, ServerPlayer player, Townsfolk npc) {
			this.level = level;
			this.player = player;
			this.npc = npc;
		}

		boolean exhausted() {
			return asked >= AUTHORITY_CALLS;
		}

		boolean free(BlockPos feet) {
			return GuestOutingPlaces.free(level, feet, player, npc);
		}

		/**
		 * Whether the spot and every block it uses are open for the player; then, as the last question, whether the
		 * player could change the spot's own block by hand ({@link Authority#mayChange}). Nothing is changed.
		 */
		boolean place(BlockPos feet, BlockPos... uses) {
			List<BlockPos> cells = new ArrayList<>(List.of(feet.below(), feet, feet.above()));
			cells.addAll(List.of(uses));
			for (BlockPos cell : cells) {
				if (!open(level, player, cell)) {
					return false;
				}
			}
			asked++;
			return Authority.mayChange(level, player, feet);
		}
	}
}

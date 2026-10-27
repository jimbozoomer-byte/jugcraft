package io.github.jimbozoomer.jugcraft.lair;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Where the Church Bridge's parts lie (docs/features/church-bridge.md), relative to its template's corner: the numbers of
 * tools/church_bridge.py, checked against it by tools/check_mod_data.py. An instance's part is at
 * {@code Lair.CHURCH_BRIDGE.origin(slot)} offset by these.
 */
public final class ChurchBridge {
	/** The floor's top layer, and where anyone on it stands. */
	public static final int SOIL = 4;
	public static final int STAND = 5;
	/** The hollow's middle (x and z, block corners), where the Horseman's leash is measured from (part 2), and its radius. */
	public static final double CENTRE_X = 36.5;
	public static final double CENTRE_Z = 50.5;
	public static final double HOLLOW = 20.5;
	/** The wood road's columns (inclusive), from the bridge's south end to the Grey Mist. */
	public static final int ROAD_WEST = 35;
	public static final int ROAD_EAST = 37;
	public static final int ROAD_NORTH = 29;
	public static final int ROAD_SOUTH = 82;
	/** The covered bridge over the brook: its rows and its walls' columns (inclusive). */
	public static final int BRIDGE_NORTH = 22;
	public static final int BRIDGE_SOUTH = 28;
	public static final int BRIDGE_WEST = 34;
	public static final int BRIDGE_EAST = 38;
	/** The brook's rows (inclusive). */
	public static final int BROOK_NORTH = 24;
	public static final int BROOK_SOUTH = 26;
	/** The Soul Braziers round the hollow, on the floor, in the order of {@link Brazier}. */
	public static final BlockPos[] BRAZIERS = {new BlockPos(41, 5, 38), new BlockPos(48, 5, 45), new BlockPos(48, 5, 55),
			new BlockPos(41, 5, 62), new BlockPos(31, 5, 62), new BlockPos(24, 5, 55), new BlockPos(24, 5, 45), new BlockPos(31, 5, 38)};
	/** Where the Horseman waits (part 2): mounted at the bridge's south foot, facing south over the hollow. */
	public static final double WAIT_X = 36.5;
	public static final double WAIT_Y = 5.0;
	public static final double WAIT_Z = 31.5;
	public static final float WAIT_YAW = 0.0F;
	/** The Grey Mist across the road's end, the way home: three columns, three high from the road. */
	public static final int EXIT_WEST = 35;
	public static final int EXIT_EAST = 37;
	public static final int EXIT_Z = 82;
	/** The Grey Mist that opens on the covered bridge when the Horseman falls (part 2): across its deck, over the brook. */
	public static final int BRIDGE_EXIT_WEST = 35;
	public static final int BRIDGE_EXIT_EAST = 37;
	public static final int BRIDGE_EXIT_Z = 25;
	/** The template's footprint (x and z), where everyone in an instance is. */
	public static final int SIZE_X = 72;
	public static final int SIZE_Z = 86;

	/**
	 * The eight braziers, clockwise round the hollow from north-north-east, each named for its compass point (NORTH_EAST:
	 * north-north-east, EAST_NORTH: east-north-east; tools/church_bridge.py BRAZIER_NAMES).
	 */
	public enum Brazier {
		NORTH_EAST, EAST_NORTH, EAST_SOUTH, SOUTH_EAST, SOUTH_WEST, WEST_SOUTH, WEST_NORTH, NORTH_WEST;

		public String title() {
			return name().toLowerCase(Locale.ROOT);
		}

		/** The brazier's name in words ("the north-north-east brazier"). */
		public Component display() {
			return Component.translatable("lair.jugcraft.church_bridge.brazier." + title());
		}

		/** The brazier in the instance at {@code origin}. */
		public BlockPos pos(BlockPos origin) {
			return origin.offset(BRAZIERS[ordinal()]);
		}

		/** Its neighbour clockwise round the ring. */
		public Brazier next() {
			return values()[(ordinal() + 1) % values().length];
		}

		/** Its neighbour anticlockwise round the ring. */
		public Brazier previous() {
			return values()[(ordinal() + values().length - 1) % values().length];
		}

		/** How far (flat) it stands from (x, z), in the template. */
		public double distance(double x, double z) {
			BlockPos at = BRAZIERS[ordinal()];
			return Math.hypot(at.getX() + 0.5 - x, at.getZ() + 0.5 - z);
		}
	}

	private ChurchBridge() {
	}

	/** The hollow's middle, on its floor, from an instance's origin. */
	public static Vec3 centre() {
		return new Vec3(CENTRE_X, STAND, CENTRE_Z);
	}

	/** Where the Horseman waits. */
	public static Vec3 waiting() {
		return new Vec3(WAIT_X, WAIT_Y, WAIT_Z);
	}

	/** Whether (x, z) (in the template) is in the hollow, the arena (its ragged edge aside). */
	public static boolean inHollow(double x, double z) {
		return Math.hypot(x - CENTRE_X, z - CENTRE_Z) <= HOLLOW;
	}

	/** Whether (x, z) (in the template) is on the wood road. */
	public static boolean onRoad(double x, double z) {
		return x >= ROAD_WEST && x < ROAD_EAST + 1 && z >= ROAD_NORTH && z < ROAD_SOUTH + 1;
	}

	/** Whether (x, z) (in the template) is on the covered bridge, walls and all. */
	public static boolean onBridge(double x, double z) {
		return x >= BRIDGE_WEST && x < BRIDGE_EAST + 1 && z >= BRIDGE_NORTH && z < BRIDGE_SOUTH + 1;
	}

	/** Whether {@code brazier} burns in the instance at {@code origin}. */
	public static boolean lit(Level level, BlockPos origin, Brazier brazier) {
		BlockState state = level.getBlockState(brazier.pos(origin));
		return state.getBlock() instanceof LairBrazierBlock && state.getValue(LairBrazierBlock.LIT);
	}

	/** The braziers burning in the instance at {@code origin}. */
	public static List<Brazier> lit(Level level, BlockPos origin) {
		List<Brazier> out = new ArrayList<>();
		for (Brazier brazier : Brazier.values()) {
			if (lit(level, origin, brazier)) {
				out.add(brazier);
			}
		}
		return out;
	}

	/**
	 * The run of {@code count} braziers nearest (x, z) (in the template): the nearest, then on round the ring toward
	 * whichever of its two neighbours is nearer.
	 */
	public static List<Brazier> run(double x, double z, int count) {
		Brazier first = Brazier.NORTH_EAST;
		for (Brazier brazier : Brazier.values()) {
			if (brazier.distance(x, z) < first.distance(x, z)) {
				first = brazier;
			}
		}
		boolean clockwise = first.next().distance(x, z) <= first.previous().distance(x, z);
		List<Brazier> out = new ArrayList<>();
		Brazier at = first;
		for (int i = 0; i < Math.min(count, Brazier.values().length); i++) {
			out.add(at);
			at = clockwise ? at.next() : at.previous();
		}
		return out;
	}

	/** Whether (x, z) (in the template) is inside the template's footprint. */
	public static boolean inside(double x, double z) {
		return x >= 0.0 && x < SIZE_X && z >= 0.0 && z < SIZE_Z;
	}

	/** The cells of the Grey Mist that opens on the bridge: across its deck, three high. */
	public static List<BlockPos> bridgeExitCells() {
		List<BlockPos> out = new ArrayList<>();
		for (int y = STAND; y <= STAND + 2; y++) {
			for (int x = BRIDGE_EXIT_WEST; x <= BRIDGE_EXIT_EAST; x++) {
				out.add(new BlockPos(x, y, BRIDGE_EXIT_Z));
			}
		}
		return out;
	}

	/** The cells of the Grey Mist at the road's end. */
	public static List<BlockPos> exitCells() {
		List<BlockPos> out = new ArrayList<>();
		for (int y = STAND; y <= STAND + 2; y++) {
			for (int x = EXIT_WEST; x <= EXIT_EAST; x++) {
				out.add(new BlockPos(x, y, EXIT_Z));
			}
		}
		return out;
	}
}

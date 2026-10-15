package io.github.jimbozoomer.jugcraft.lair;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Where the Foundry Trench's parts lie (docs/features/foundry-trench.md), relative to its template's corner: the numbers
 * of tools/foundry_trench.py, checked against it by tools/check_mod_data.py. An instance's part is at
 * {@code Lair.FOUNDRY_TRENCH.origin(slot)} offset by these.
 */
public final class FoundryTrench {
	/** The pit's paving, where anyone in the pit stands, and the ground round it (anyone on it stands a block higher). */
	public static final int PAVE = 4;
	public static final int STAND = 5;
	public static final int GROUND = 12;
	/** Where anyone in the fire trench and in the dugout stands. */
	public static final int TRENCH_STAND = 11;
	public static final int DUGOUT_STAND = 9;
	/** The pit's open cells, inclusive: west to east (x) and north to south (z). */
	public static final int PIT_WEST = 12;
	public static final int PIT_EAST = 58;
	public static final int PIT_NORTH = 16;
	public static final int PIT_SOUTH = 56;
	/** The pit's middle (x and z, block corners): where the Dreadnought's leash is measured from. */
	public static final double CENTRE_X = 35.5;
	public static final double CENTRE_Z = 36.5;
	/** Each pylon's breaker, crown and ground terminal, in the order of {@link Pylon}. */
	public static final BlockPos[] BREAKERS = {new BlockPos(18, 5, 36), new BlockPos(53, 5, 36), new BlockPos(35, 5, 51)};
	public static final BlockPos[] CROWNS = {new BlockPos(16, 15, 36), new BlockPos(55, 15, 36), new BlockPos(35, 15, 53)};
	public static final BlockPos[] TERMINALS = {new BlockPos(19, 4, 36), new BlockPos(52, 4, 36), new BlockPos(35, 4, 50)};
	/**
	 * The lane from the pit's centre out to each pylon's terminal, in the order of {@link Pylon}: its open cells inclusive
	 * (x0, x1, z0, z1), three wide.
	 */
	public static final int[][] LANES = {{20, 34, 35, 37}, {36, 51, 35, 37}, {34, 36, 37, 49}};
	/** The dugout's telephone, on the map table. */
	public static final BlockPos TELEPHONE = new BlockPos(25, 10, 64);
	/** Where the Dreadnought waits in his assembly gantry, facing south over the pit. */
	public static final double WAIT_X = 35.5;
	public static final double WAIT_Y = 5.0;
	public static final double WAIT_Z = 9.5;
	/** Where the Grey Mist opens in the gantry when the Dreadnought falls: three columns, two high from the floor. */
	public static final int EXIT_WEST = 34;
	public static final int EXIT_EAST = 36;
	public static final int EXIT_Z = 6;

	/** The three pylons, west, east and south (tools/lairs.py PYLON_NAMES). */
	public enum Pylon {
		WEST, EAST, SOUTH;

		public String title() {
			return name().toLowerCase(java.util.Locale.ROOT);
		}

		/** The pylon's name in words ("the west pylon"). */
		public Component display() {
			return Component.translatable("lair.jugcraft.foundry_trench.pylon." + title());
		}

		public BlockPos breaker(BlockPos origin) {
			return origin.offset(BREAKERS[ordinal()]);
		}

		public BlockPos crown(BlockPos origin) {
			return origin.offset(CROWNS[ordinal()]);
		}

		public BlockPos terminal(BlockPos origin) {
			return origin.offset(TERMINALS[ordinal()]);
		}
	}

	private FoundryTrench() {
	}

	/** The pit's middle, on its floor, from an instance's origin. */
	public static Vec3 centre() {
		return new Vec3(CENTRE_X, STAND, CENTRE_Z);
	}

	/** Where the Dreadnought waits in his gantry. */
	public static Vec3 waiting() {
		return new Vec3(WAIT_X, WAIT_Y, WAIT_Z);
	}

	/** Whether (x, z) (in the template) is one of the pit's open cells. */
	public static boolean inPit(double x, double z) {
		return x >= PIT_WEST && x < PIT_EAST + 1 && z >= PIT_NORTH && z < PIT_SOUTH + 1;
	}

	/** How far (x, z) (in the template) is from the lane to {@code pylon} (0 on it). */
	public static double fromLane(Pylon pylon, double x, double z) {
		int[] lane = LANES[pylon.ordinal()];
		double dx = Math.max(0.0, Math.max(lane[0] - x, x - (lane[1] + 1)));
		double dz = Math.max(0.0, Math.max(lane[2] - z, z - (lane[3] + 1)));
		return Math.hypot(dx, dz);
	}

	/** The pylon whose breaker is at {@code pos} in the instance at {@code origin}, or null. */
	public static @Nullable Pylon breakerAt(BlockPos origin, BlockPos pos) {
		for (Pylon pylon : Pylon.values()) {
			if (pylon.breaker(origin).equals(pos)) {
				return pylon;
			}
		}
		return null;
	}

	/** The cells of the Grey Mist that opens in the gantry when the Dreadnought falls. */
	public static List<BlockPos> exitCells() {
		List<BlockPos> out = new ArrayList<>();
		for (int y = STAND; y <= STAND + 1; y++) {
			for (int x = EXIT_WEST; x <= EXIT_EAST; x++) {
				out.add(new BlockPos(x, y, EXIT_Z));
			}
		}
		return out;
	}
}

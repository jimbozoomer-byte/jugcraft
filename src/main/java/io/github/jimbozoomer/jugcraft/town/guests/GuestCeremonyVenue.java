package io.github.jimbozoomer.jugcraft.town.guests;

import io.github.jimbozoomer.jugcraft.town.TownState;
import io.github.jimbozoomer.jugcraft.town.Townsfolk;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/**
 * Where a guest ceremony is held (worker D): the spawn town's Church of Notch and its Priest of Notch, both already
 * loaded. Read only: nothing here loads a chunk, sets a block or touches an entity. Every block read is inside the church
 * plot after every chunk under the plot has been found loaded ({@link #loaded}), and the priest is looked for only among
 * loaded entities.
 *
 * <p><b>The plot</b> is the Church of Notch's whole plot from {@code tools/town_living.py} ({@code CHURCH}: town x 78 to
 * 114, z 35 to 87, the building, its steps and the owner's slot machines), from {@link #BELOW} under the town's ground to
 * {@link #ABOVE} over it, at the town's origin ({@link TownState#origin}, in the overworld). The priest is a loaded
 * {@link Townsfolk} with role {@code priest} standing in the plot (the town's own resident, not a custom NPC or guest). The
 * anchor where the guest waits is a standable cell 2 to 3 blocks from the priest (or the priest's own cell when none is
 * free); the guest walks near it with its usual goals.
 */
public final class GuestCeremonyVenue {
	/** The church plot in town coordinates (tools/town_living.py CHURCH), inclusive. */
	public static final int X0 = 78, Z0 = 35, X1 = 114, Z1 = 87;
	/** How far the plot reaches below and above the town's ground. */
	public static final int BELOW = 2, ABOVE = 48;

	/** Why there is no venue now. */
	public enum Refusal {
		NO_TOWN, NOT_LOADED, NO_PRIEST
	}

	/** A loaded venue: its level, the church plot, the priest standing in it, and where the guest waits. */
	public record Venue(ServerLevel level, AABB church, Townsfolk priest, BlockPos anchor) {
	}

	/** The venue, or why there is none. */
	public record Found(@Nullable Venue venue, @Nullable Refusal refusal) {
	}

	/** A church plot: a level and a box. */
	record Plot(ResourceKey<Level> dimension, AABB box) {
	}

	/**
	 * Game tests only: a stand-in plot per test guest, so a test builds its own small church in its own structure. Guests
	 * not named here use the spawn town's church. Never filled in play.
	 */
	static final Map<String, Plot> TEST_PLOTS = new ConcurrentHashMap<>();

	private GuestCeremonyVenue() {
	}

	/** The venue for a ceremony with {@code guest}, or why there is none. Reads only loaded chunks and entities. */
	public static Found find(MinecraftServer server, String guest) {
		Plot plot = plot(server, guest);
		ServerLevel level = plot == null ? null : server.getLevel(plot.dimension());
		if (plot == null || level == null) {
			return new Found(null, Refusal.NO_TOWN);
		}
		if (!loaded(level, plot.box())) {
			return new Found(null, Refusal.NOT_LOADED);
		}
		Townsfolk priest = priest(level, plot.box());
		if (priest == null) {
			return new Found(null, Refusal.NO_PRIEST);
		}
		BlockPos anchor = anchor(level, plot.box(), priest.blockPosition());
		if (!level.isPositionEntityTicking(anchor)) {
			return new Found(null, Refusal.NOT_LOADED);   // loaded at its edge only: nobody could walk there
		}
		return new Found(new Venue(level, plot.box(), priest, anchor), null);
	}

	/** The church plot for {@code guest}: a test's stand-in, else the spawn town's; null when there is no town. */
	static @Nullable Plot plot(MinecraftServer server, String guest) {
		Plot test = TEST_PLOTS.get(guest);
		if (test != null) {
			return test;
		}
		BlockPos origin = TownState.get(server).origin();
		if (origin == null) {
			return null;
		}
		return new Plot(Level.OVERWORLD, new AABB(origin.getX() + X0, origin.getY() - BELOW, origin.getZ() + Z0,
				origin.getX() + X1 + 1, origin.getY() + ABOVE, origin.getZ() + Z1 + 1));
	}

	/** Whether every chunk under {@code box} is loaded now (asked without loading any). */
	public static boolean loaded(ServerLevel level, AABB box) {
		int x0 = Mth.floor(box.minX) >> 4, x1 = (Mth.ceil(box.maxX) - 1) >> 4;
		int z0 = Mth.floor(box.minZ) >> 4, z1 = (Mth.ceil(box.maxZ) - 1) >> 4;
		for (int x = x0; x <= x1; x++) {
			for (int z = z0; z <= z1; z++) {
				if (!level.hasChunk(x, z)) {
					return false;
				}
			}
		}
		return true;
	}

	/** The town's priest standing in {@code box} among loaded entities, or null. */
	static @Nullable Townsfolk priest(ServerLevel level, AABB box) {
		List<Townsfolk> found = level.getEntitiesOfClass(Townsfolk.class, box, GuestCeremonyVenue::isPriest);
		return found.stream().min(Comparator.comparing(Townsfolk::getUUID)).orElse(null);
	}

	/** The town's own Priest of Notch: alive, role {@code priest}, not a custom NPC or a guest. */
	static boolean isPriest(Townsfolk npc) {
		return npc.isAlive() && !npc.isRemoved() && "priest".equals(npc.role()) && npc.custom() == null && npc.guest() == null;
	}

	/**
	 * A standable cell 2 to 3 blocks across from {@code priest}, inside {@code box}, nearest that distance and the priest's
	 * own height; the priest's cell when none is. The caller has found every chunk under the box loaded.
	 */
	static BlockPos anchor(ServerLevel level, AABB box, BlockPos priest) {
		BlockPos best = null;
		int bestScore = Integer.MAX_VALUE;
		for (int dy = -1; dy <= 1; dy++) {
			for (int dx = -3; dx <= 3; dx++) {
				for (int dz = -3; dz <= 3; dz++) {
					int across = dx * dx + dz * dz;
					if (across < 4 || across > 9) {
						continue;
					}
					BlockPos feet = priest.offset(dx, dy, dz);
					if (!box.contains(Vec3.atCenterOf(feet)) || !box.contains(Vec3.atCenterOf(feet.below()))
							|| !box.contains(Vec3.atCenterOf(feet.above())) || !standable(level, feet)) {
						continue;
					}
					int score = across + 8 * Math.abs(dy);
					if (score < bestScore) {
						bestScore = score;
						best = feet.immutable();
					}
				}
			}
		}
		return best != null ? best : priest.immutable();
	}

	/** Room for a player-sized body: a sturdy floor (or a thin cover such as carpet), and nothing solid at feet or head. */
	static boolean standable(ServerLevel level, BlockPos feet) {
		BlockState at = level.getBlockState(feet);
		VoxelShape shape = at.getCollisionShape(level, feet);
		boolean floor = shape.isEmpty() ? level.getBlockState(feet.below()).isFaceSturdy(level, feet.below(), Direction.UP)
				: shape.max(Direction.Axis.Y) <= 0.25;
		BlockPos head = feet.above();
		return floor && at.getFluidState().isEmpty() && level.getBlockState(head).getCollisionShape(level, head).isEmpty();
	}
}

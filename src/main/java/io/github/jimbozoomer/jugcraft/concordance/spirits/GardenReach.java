package io.github.jimbozoomer.jugcraft.concordance.spirits;

import io.github.jimbozoomer.jugcraft.concordance.LeyPylonBlockEntity;
import io.github.jimbozoomer.jugcraft.concordance.courier.Couriers;
import io.github.jimbozoomer.jugcraft.concordance.garden.GleanerBlockEntity;
import io.github.jimbozoomer.jugcraft.concordance.garden.VerdantBedBlockEntity;
import io.github.jimbozoomer.jugcraft.concordance.garden.VerdantHeartBlockEntity;
import io.github.jimbozoomer.jugcraft.concordance.resource.BoundWill;
import io.github.jimbozoomer.jugcraft.concordance.worker.WorkerParser;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.UUID;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jspecify.annotations.Nullable;

/**
 * Concordance garden cohort, GC-03 (GC-P2, GB12): the bed index a garden spirit takes its targets from, one for each
 * presence (its service memory, PactSpiritEntity.serviceMemory; never saved). It holds the places of the awake Verdant Beds in
 * its pact's area (the cube of the service's radius round the perch, as Pacts.serves reads it) whose keeper it indexes, at
 * most {@value #MAX_BEDS}, nearest the perch first (Garden.nearest's order: distance, then y, x and z), and reads them again
 * at most every {@value #REFRESH_TICKS} ticks. A refresh reads only the block-entity maps of the chunks the area overlaps
 * that are loaded now (CourierWork.reserve's scan: at most 9 chunks, the radius never past WorkerParser.MAX_RADIUS); it never
 * builds or sorts the area's positions (Garden.nearest built 867 at reach 8), never loads a chunk and never samples a
 * habitat. Each read resolves the places again, in loaded chunks only, so a bed broken or asleep since is gone at once and a
 * new one waits for the next refresh.
 * <p>
 * GC-05 (GC-P45, GC-P54): in the same pass, an index made with a keeper rule ({@link #devices}) also holds the awake Verdant
 * Hearts and Gleaners whose keeper that rule keeps and the Ley Pylons such a keeper or no one owns, at most
 * {@value #MAX_DEVICES} of each, nearest first, read back the same way. An index of beds alone ({@link #of}) holds none.
 * GC-07 (GC-P82, GC-P83): an index made with stores ({@link #withStores}) also holds, in the same pass, the plain
 * containers touching its centre (the 26 cells round the perch; CourierWork.plain, decided on the block entity in
 * hand), at most {@value #MAX_STORES}, nearest first, read back the same way ({@link #stores(ServerLevel, long)}).
 */
public final class GardenReach {
	/** The most beds one index holds. Keep equal to tools/concordance_garden_pacts.py REACH. */
	public static final int MAX_BEDS = 64;
	/** GC-05: the most Hearts, the most Gleaners and the most Ley Pylons one index holds, each. Keep equal to tools/concordance_garden_pacts.py REACH. */
	public static final int MAX_DEVICES = 16;
	/** GC-07: the most plain containers touching the perch one index holds (Foras's stores). Keep equal to tools/concordance_garden_pacts.py REACH. */
	public static final int MAX_STORES = 16;
	/** How long an index serves before it is read again (ticks). Keep equal to tools/concordance_garden_pacts.py REACH. */
	public static final int REFRESH_TICKS = 200;
	/** An index of beds alone keeps no keeper's devices. */
	private static final Predicate<@Nullable UUID> NO_DEVICES = keeper -> false;

	private final BlockPos centre;
	private final int radius;
	private final Predicate<VerdantBedBlockEntity> indexes;
	private final Predicate<@Nullable UUID> keeps;
	private final boolean stores;
	private List<BlockPos> beds = List.of();
	private List<BlockPos> hearts = List.of();
	private List<BlockPos> gleaners = List.of();
	private List<BlockPos> pylons = List.of();
	private List<BlockPos> containers = List.of();
	private long refreshedAt = Long.MIN_VALUE;
	private int refreshes;
	private int scanned;

	/** An index of the cube of {@code radius} (at most WorkerParser.MAX_RADIUS) round {@code centre}, of the awake beds {@code indexes} accepts. */
	public GardenReach(BlockPos centre, int radius, Predicate<VerdantBedBlockEntity> indexes) {
		this(centre, radius, indexes, NO_DEVICES);
	}

	/**
	 * GC-05: an index of the same cube that also holds the awake Hearts and Gleaners whose keeper {@code keeps} accepts (it
	 * is asked with null for a device nobody keeps) and the Ley Pylons owned by one it accepts or by no one.
	 */
	public GardenReach(BlockPos centre, int radius, Predicate<VerdantBedBlockEntity> indexes, Predicate<@Nullable UUID> keeps) {
		this(centre, radius, indexes, keeps, false);
	}

	/** The index of the cube of {@code radius} round {@code centre}, holding the plain containers touching it when {@code stores}. */
	private GardenReach(BlockPos centre, int radius, Predicate<VerdantBedBlockEntity> indexes, Predicate<@Nullable UUID> keeps, boolean stores) {
		this.centre = centre.immutable();
		this.radius = Math.clamp(radius, 0, WorkerParser.MAX_RADIUS);
		this.indexes = indexes;
		this.keeps = keeps;
		this.stores = stores;
	}

	/**
	 * {@code record}'s index: its perch's area, indexing the beds whose keeper its pact benefits at each refresh (GC-P32;
	 * GardenPacts.target asks again at each change). The pact is seated with a service: its presence's decide saw to it.
	 */
	public static GardenReach of(BoundWill record) {
		BoundWill.Pact pact = record.pact();
		return new GardenReach(Couriers.pos(pact.place()), pact.service().radius(), bed -> GardenPacts.benefits(record, bed.keeper()));
	}

	/**
	 * GC-05: {@code record}'s device index: its perch's area, no beds, the devices of the keepers its pact benefits at each
	 * refresh (and the pylons no one owns). GardenPacts.target asks again at each change.
	 */
	public static GardenReach devices(BoundWill record) {
		BoundWill.Pact pact = record.pact();
		return new GardenReach(Couriers.pos(pact.place()), pact.service().radius(), bed -> false, keeper -> GardenPacts.benefits(record, keeper));
	}

	/**
	 * GC-07: {@code record}'s store index: its perch's area, indexing the beds whose keeper its pact benefits (as {@link #of})
	 * and the plain containers touching the perch; no devices. GardenPacts.target asks again at each change.
	 */
	public static GardenReach withStores(BoundWill record) {
		BoundWill.Pact pact = record.pact();
		return new GardenReach(Couriers.pos(pact.place()), pact.service().radius(), bed -> GardenPacts.benefits(record, bed.keeper()), NO_DEVICES, true);
	}

	/** GC-07: an index of the cube of {@code radius} round {@code centre}, of the awake beds {@code indexes} accepts and the plain containers touching {@code centre}. */
	public static GardenReach withStores(BlockPos centre, int radius, Predicate<VerdantBedBlockEntity> indexes) {
		return new GardenReach(centre, radius, indexes, NO_DEVICES, true);
	}

	/** Whether this index is still {@code record}'s area (its perch and radius), so a service memory may keep it. */
	public boolean covers(BoundWill record) {
		BoundWill.Pact pact = record.pact();
		return pact != null && pact.place() != null && pact.service() != null && centre.equals(Couriers.pos(pact.place()))
				&& radius == Math.clamp(pact.service().radius(), 0, WorkerParser.MAX_RADIUS);
	}

	/**
	 * The indexed beds that are still there, loaded and awake, nearest first; read again first when the index is
	 * {@value #REFRESH_TICKS} ticks old at game time {@code now} or more, or the clock ran back. Server thread only.
	 */
	public List<VerdantBedBlockEntity> beds(ServerLevel level, long now) {
		fresh(level, now);
		List<VerdantBedBlockEntity> out = new ArrayList<>(beds.size());
		for (BlockPos pos : beds) {
			if (level.isLoaded(pos) && level.getBlockEntity(pos) instanceof VerdantBedBlockEntity bed && !bed.isRemoved() && bed.awake()) {
				out.add(bed);
			}
		}
		return out;
	}

	/** GC-05: the indexed Hearts still there, loaded and awake, nearest first (refreshed as {@link #beds} is). Server thread only. */
	public List<VerdantHeartBlockEntity> hearts(ServerLevel level, long now) {
		fresh(level, now);
		List<VerdantHeartBlockEntity> out = new ArrayList<>(hearts.size());
		for (BlockPos pos : hearts) {
			if (level.isLoaded(pos) && level.getBlockEntity(pos) instanceof VerdantHeartBlockEntity heart && !heart.isRemoved() && heart.awake()) {
				out.add(heart);
			}
		}
		return out;
	}

	/** GC-05: the indexed Gleaners still there, loaded and awake, nearest first. Server thread only. */
	public List<GleanerBlockEntity> gleaners(ServerLevel level, long now) {
		fresh(level, now);
		List<GleanerBlockEntity> out = new ArrayList<>(gleaners.size());
		for (BlockPos pos : gleaners) {
			if (level.isLoaded(pos) && level.getBlockEntity(pos) instanceof GleanerBlockEntity gleaner && !gleaner.isRemoved() && gleaner.awake()) {
				out.add(gleaner);
			}
		}
		return out;
	}

	/** GC-05: the indexed Ley Pylons still there and loaded, nearest first. Server thread only. */
	public List<LeyPylonBlockEntity> pylons(ServerLevel level, long now) {
		fresh(level, now);
		List<LeyPylonBlockEntity> out = new ArrayList<>(pylons.size());
		for (BlockPos pos : pylons) {
			if (level.isLoaded(pos) && level.getBlockEntity(pos) instanceof LeyPylonBlockEntity pylon && !pylon.isRemoved()) {
				out.add(pylon);
			}
		}
		return out;
	}

	/** GC-07: the indexed plain containers touching the perch still there, loaded and plain, nearest first. Server thread only. */
	public List<BlockPos> stores(ServerLevel level, long now) {
		fresh(level, now);
		List<BlockPos> out = new ArrayList<>(containers.size());
		for (BlockPos pos : containers) {
			if (level.isLoaded(pos) && CourierWork.container(level, pos) != null) {
				out.add(pos);
			}
		}
		return out;
	}

	/** Forgets the index, so the next read refreshes it. */
	public void forget() {
		refreshedAt = Long.MIN_VALUE;
		beds = List.of();
		hearts = List.of();
		gleaners = List.of();
		pylons = List.of();
		containers = List.of();
	}

	/** How many times it has been read from the chunks (tests and the load card). */
	public int refreshes() {
		return refreshes;
	}

	/** How many block entities its last refresh looked at (tests and the load card). */
	public int scanned() {
		return scanned;
	}

	/** The order an index keeps (Garden.nearest's): nearest {@code centre} first, then the lowest, then by x and by z. */
	public static Comparator<BlockPos> nearest(BlockPos centre) {
		return Comparator.<BlockPos>comparingDouble(pos -> pos.distSqr(centre)).thenComparingInt(BlockPos::getY)
				.thenComparingInt(BlockPos::getX).thenComparingInt(BlockPos::getZ);
	}

	/** Reads the chunks again when the index is REFRESH_TICKS old at game time {@code now}, never read, or the clock ran back. */
	private void fresh(ServerLevel level, long now) {
		if (refreshedAt == Long.MIN_VALUE || now < refreshedAt || now - refreshedAt >= REFRESH_TICKS) {
			refresh(level, now);
		}
	}

	private void refresh(ServerLevel level, long now) {
		Comparator<BlockPos> order = nearest(centre);
		// The farthest first, so the 65th nearest is the one let go: O(n log MAX_BEDS), whatever the area holds.
		PriorityQueue<BlockPos> kept = new PriorityQueue<>(MAX_BEDS + 1, order.reversed());
		// GC-05: each device kind likewise, at most MAX_DEVICES each, in the same pass (an index of beds alone keeps none).
		boolean devices = keeps != NO_DEVICES;
		PriorityQueue<BlockPos> keptHearts = new PriorityQueue<>(MAX_DEVICES + 1, order.reversed());
		PriorityQueue<BlockPos> keptGleaners = new PriorityQueue<>(MAX_DEVICES + 1, order.reversed());
		PriorityQueue<BlockPos> keptPylons = new PriorityQueue<>(MAX_DEVICES + 1, order.reversed());
		// GC-07: the plain containers touching the perch, in the same pass, at most MAX_STORES, decided on the entity in hand
		// (reading the level here could change the very map being walked: LevelChunk.getBlockEntity).
		PriorityQueue<BlockPos> keptStores = new PriorityQueue<>(MAX_STORES + 1, order.reversed());
		int examined = 0;
		for (int cx = (centre.getX() - radius) >> 4; cx <= (centre.getX() + radius) >> 4; cx++) {
			for (int cz = (centre.getZ() - radius) >> 4; cz <= (centre.getZ() + radius) >> 4; cz++) {
				LevelChunk chunk = level.getChunkSource().getChunkNow(cx, cz);
				if (chunk == null) {
					continue;
				}
				for (BlockEntity entity : chunk.getBlockEntities().values()) {
					examined++;
					BlockPos pos = entity.getBlockPos();
					if (entity instanceof VerdantBedBlockEntity bed && !bed.isRemoved() && bed.awake() && within(pos) && indexes.test(bed)) {
						kept.add(pos);
						if (kept.size() > MAX_BEDS) {
							kept.poll();
						}
					} else if (devices && !entity.isRemoved() && within(pos)) {
						if (entity instanceof VerdantHeartBlockEntity heart && heart.awake() && keeps.test(heart.keeper())) {
							keptHearts.add(pos);
							if (keptHearts.size() > MAX_DEVICES) {
								keptHearts.poll();
							}
						} else if (entity instanceof GleanerBlockEntity gleaner && gleaner.awake() && keeps.test(gleaner.keeper())) {
							keptGleaners.add(pos);
							if (keptGleaners.size() > MAX_DEVICES) {
								keptGleaners.poll();
							}
						} else if (entity instanceof LeyPylonBlockEntity pylon && (pylon.owner() == null || keeps.test(pylon.owner()))) {
							keptPylons.add(pos);
							if (keptPylons.size() > MAX_DEVICES) {
								keptPylons.poll();
							}
						}
					}
					if (stores && !entity.isRemoved() && touching(pos) && CourierWork.plain(entity)) {
						keptStores.add(pos);
						if (keptStores.size() > MAX_STORES) {
							keptStores.poll();
						}
					}
				}
			}
		}
		beds = sorted(kept, order);
		hearts = sorted(keptHearts, order);
		gleaners = sorted(keptGleaners, order);
		pylons = sorted(keptPylons, order);
		containers = sorted(keptStores, order);
		refreshedAt = now;
		refreshes++;
		scanned = examined;
	}

	private static List<BlockPos> sorted(PriorityQueue<BlockPos> kept, Comparator<BlockPos> order) {
		List<BlockPos> out = new ArrayList<>(kept);
		out.sort(order);
		return List.copyOf(out);
	}

	private boolean within(BlockPos pos) {
		return Math.abs(pos.getX() - centre.getX()) <= radius && Math.abs(pos.getY() - centre.getY()) <= radius
				&& Math.abs(pos.getZ() - centre.getZ()) <= radius;
	}

	/** GC-P83: one of the 26 cells touching the centre (the perch), never the centre itself. */
	private boolean touching(BlockPos pos) {
		return !pos.equals(centre) && Math.abs(pos.getX() - centre.getX()) <= 1 && Math.abs(pos.getY() - centre.getY()) <= 1
				&& Math.abs(pos.getZ() - centre.getZ()) <= 1;
	}
}

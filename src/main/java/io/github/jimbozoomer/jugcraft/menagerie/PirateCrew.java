package io.github.jimbozoomer.jugcraft.menagerie;

import io.github.jimbozoomer.jugcraft.menagerie.CrewedVessel.Role;
import io.github.jimbozoomer.jugcraft.menagerie.CrewedVessel.Station;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * The crew of a moving pirate ship ({@link CrewedVessel}; docs/features/pirate-ship-crews.md): plunderers seated at
 * the ship's stations as its passengers. The ship calls these from its own overrides; everything else (steering,
 * gunnery, boarding, going back aboard) runs in the plunderer's goals.
 *
 * <p>STUB (first commit): the public signatures below are final; bodies marked STUB return the safe empty answer until
 * the plunderer's ship link lands in the next commit.
 */
public final class PirateCrew {
	/**
	 * The standard ten stations, in this index order: 0 helm, 1 lookout, 2-5 gunners (port fore, port aft, starboard
	 * fore, starboard aft), 6-9 deck crew (port fore, starboard fore, port aft, starboard aft). Local frame as in
	 * {@link CrewedVessel}: x to port, y up from the waterline, z to the bow. The deck is at y 1.0.
	 */
	public static final List<Station> STATIONS = List.of(
			new Station(Role.HELM, new Vec3(0.0, 1.0, -6.0), 0.0F, 0.0F, new Vec3(0.0, 1.0, -6.0)),
			new Station(Role.LOOKOUT, new Vec3(0.0, 9.5, 1.0), 0.0F, 180.0F, new Vec3(0.0, 9.5, 1.0)),
			new Station(Role.GUNNER, new Vec3(2.0, 1.0, 2.5), -90.0F, 45.0F, new Vec3(3.2, 1.6, 2.5)),
			new Station(Role.GUNNER, new Vec3(2.0, 1.0, -2.0), -90.0F, 45.0F, new Vec3(3.2, 1.6, -2.0)),
			new Station(Role.GUNNER, new Vec3(-2.0, 1.0, 2.5), 90.0F, 45.0F, new Vec3(-3.2, 1.6, 2.5)),
			new Station(Role.GUNNER, new Vec3(-2.0, 1.0, -2.0), 90.0F, 45.0F, new Vec3(-3.2, 1.6, -2.0)),
			new Station(Role.DECK, new Vec3(1.0, 1.0, 5.5), 0.0F, 0.0F, new Vec3(1.0, 1.0, 5.5)),
			new Station(Role.DECK, new Vec3(-1.0, 1.0, 5.5), 0.0F, 0.0F, new Vec3(-1.0, 1.0, 5.5)),
			new Station(Role.DECK, new Vec3(1.0, 1.0, -3.5), 0.0F, 0.0F, new Vec3(1.0, 1.0, -3.5)),
			new Station(Role.DECK, new Vec3(-1.0, 1.0, -3.5), 0.0F, 0.0F, new Vec3(-1.0, 1.0, -3.5)));
	/** The standard walkable deck, local frame: beam 5 by length 15, from the deck up two blocks. */
	public static final AABB LOCAL_DECK = new AABB(-2.5, 1.0, -7.5, 2.5, 3.0, 7.5);

	private PirateCrew() {
	}

	/**
	 * Where {@code passenger} sits on {@code vessel}: its station's seat turned by the ship's yaw
	 * ({@code seat.yRot(-vessel.getYRot() * Mth.DEG_TO_RAD)}), an offset from the ship's position in world axes, not yet
	 * added to it, the same as {@code Zeppelin}, {@code Landship} and {@code RaiderCrew.seat} return. The ship returns
	 * this unchanged from {@code getPassengerAttachmentPoint}, or its own default when it is null (not one of its crew).
	 * Both sides: the crew member's station is synced.
	 */
	public static @Nullable Vec3 seat(Entity vessel, Entity passenger) {
		return null; // STUB until the ship link lands
	}

	/** Whether {@code passenger} may ride {@code vessel}: only its own crew, one to a station. For {@code canAddPassenger}. */
	public static boolean mayBoard(Entity vessel, Entity passenger) {
		return false; // STUB until the ship link lands
	}

	/** Whether a living crew member holds the helm. The ship moves under its own steering only while this is true. */
	public static boolean helmManned(Entity vessel) {
		return false; // STUB until the ship link lands
	}

	/**
	 * Crews a new ship: one plunderer for each station, linked to it and seated as its passenger. Call once, on the
	 * server, before the ship is in the world; the caller then adds it with {@code addFreshEntityWithPassengers}, so
	 * each crew member is added once, saved inside the ship and never duplicated. Returns how many were seated; a ship
	 * that already carries crew gets none.
	 */
	public static int provision(ServerLevel level, Entity vessel) {
		return 0; // STUB until the ship link lands
	}

	/** Whether the ship's crew are all gone: none aboard and none of its own still alive nearby (loaded entities only). */
	public static boolean defeated(Entity vessel) {
		return false; // STUB until the ship link lands
	}

	/** {@link #LOCAL_DECK} turned with the ship: the world box that encloses it (exact at multiples of 90°). */
	public static AABB deckBounds(Entity vessel) {
		Vec3 a = new Vec3(LOCAL_DECK.minX, LOCAL_DECK.minY, LOCAL_DECK.minZ).yRot(-vessel.getYRot() * Mth.DEG_TO_RAD);
		Vec3 b = new Vec3(LOCAL_DECK.maxX, LOCAL_DECK.maxY, LOCAL_DECK.maxZ).yRot(-vessel.getYRot() * Mth.DEG_TO_RAD);
		Vec3 c = new Vec3(LOCAL_DECK.minX, LOCAL_DECK.minY, LOCAL_DECK.maxZ).yRot(-vessel.getYRot() * Mth.DEG_TO_RAD);
		Vec3 d = new Vec3(LOCAL_DECK.maxX, LOCAL_DECK.maxY, LOCAL_DECK.minZ).yRot(-vessel.getYRot() * Mth.DEG_TO_RAD);
		double minX = Math.min(Math.min(a.x, b.x), Math.min(c.x, d.x)), maxX = Math.max(Math.max(a.x, b.x), Math.max(c.x, d.x));
		double minZ = Math.min(Math.min(a.z, b.z), Math.min(c.z, d.z)), maxZ = Math.max(Math.max(a.z, b.z), Math.max(c.z, d.z));
		return new AABB(minX, LOCAL_DECK.minY, minZ, maxX, LOCAL_DECK.maxY, maxZ).move(vessel.position());
	}
}

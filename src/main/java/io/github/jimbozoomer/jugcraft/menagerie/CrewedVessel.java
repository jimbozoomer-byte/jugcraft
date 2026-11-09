package io.github.jimbozoomer.jugcraft.menagerie;

import java.util.List;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * What a moving pirate ship exposes to its crew (docs/features/pirate-ship-crews.md). The ship is an entity that
 * implements this; {@link PirateCrew} seats, steers and fights its crew of plunderers through it.
 *
 * <p>Frame of every local offset: ship-local blocks from the entity position, x to port (the ship's left), y up from
 * the waterline, z to the bow. Offsets turn with the ship as {@code local.yRot(-getYRot() * Mth.DEG_TO_RAD)}, the
 * convention of {@code Zeppelin}, {@code Landship} and {@code RaiderCrew}. A station's facing is a Minecraft yaw
 * relative to the ship's: 0 the bow, +90 starboard, -90 port; its arc is the half-width either side of the facing.
 *
 * <p>Crew ride the ship as passengers, so the game saves them inside the ship and brings them back with it. The ship
 * saves only {@link #heaveTo()} beyond what vanilla saves for a vehicle.
 */
public interface CrewedVessel {
	/** What a station is for. */
	enum Role { HELM, GUNNER, DECK, LOOKOUT }

	/**
	 * A place on the ship. {@code seat} is where a rider's position goes (the game's sitting pose puts the hips about
	 * 0.75 above it); {@code muzzle} is where a gunner's shot leaves (the seat for other roles).
	 */
	record Station(Role role, Vec3 seat, float facing, float arc, Vec3 muzzle) {
	}

	/** The ship's stations, constant for its type; {@link PirateCrew#STATIONS} is the standard ten. */
	List<Station> stations();

	/** The walkable deck in world space this tick, for boarding ({@link PirateCrew#deckBounds} turns the standard one). */
	AABB deckBounds();

	/** Whether the ship is sinking or wrecked: its crew abandon it and fight on as ordinary plunderers. */
	boolean sunk();

	/** Whether the crew have stopped the ship (boarding). Saved by the ship. */
	boolean heaveTo();

	/** The crew stop the ship to fight a boarding action, and let it go again after. */
	void setHeaveTo(boolean stop);
}

package io.github.jimbozoomer.jugcraft.menagerie;

import io.github.jimbozoomer.jugcraft.menagerie.CrewedVessel.Station;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;

/**
 * Who stands on a pirate ship's deck (docs/features/pirate-ships.md): an API for other features (the Naval Cutlass's
 * boarding bonus), with no dependency on them. A ship's deck is four {@link PirateShipDeck} squares that entities stand
 * on without riding anything, so "afloat" checks that look for water or a boat miss a fight on a pirate deck; this
 * answers it.
 */
public final class PirateDecks {
	private PirateDecks() {
	}

	/**
	 * Whether {@code entity}'s feet are in real, supported contact with a pirate ship's deck: on the ground, within a
	 * pixel of a deck square's top ({@link PirateDeckPlan#atDeckHeight}), and over the ship's walkable deck at its yaw
	 * ({@link PirateDeckPlan#over}). A ship's own crew riding at a deck-level station (the helm, the guns, the deck
	 * hands) stand on its deck too; its lookout, in the main top, does not.
	 *
	 * <p>False for an entity in the water beside the hull, on a shore or dock beside the ship, on a block or anything
	 * else above the deck, in the rigging or the main top, in the air or mid-jump, inside the hull below the deck, or
	 * riding anything else (a boat). Cost: one loaded-entity query of a small box round the entity's feet, only when
	 * called; it loads no chunk and keeps no state.
	 */
	public static boolean standingOnDeck(LivingEntity entity) {
		if (entity == null || entity.isRemoved()) {
			return false;
		}
		if (entity.isPassenger()) {
			if (entity.getVehicle() instanceof PirateShip ship && entity instanceof OwnerPlunderer crew) {
				Station station = PirateCrew.station(ship, crew);
				return station != null && PirateDeckPlan.atDeckHeight(station.seat().y);
			}
			return false;
		}
		if (!entity.onGround()) {
			return false;
		}
		AABB box = entity.getBoundingBox();
		AABB feet = new AABB(box.minX, box.minY - PirateDeckPlan.TOLERANCE, box.minZ, box.maxX, box.minY + PirateDeckPlan.TOLERANCE, box.maxZ);
		for (PirateShipDeck deck : entity.level().getEntitiesOfClass(PirateShipDeck.class, feet)) {
			PirateShip ship = deck.ship();
			if (ship == null) {
				continue;
			}
			if (Math.abs(box.minY - deck.getBoundingBox().maxY) <= PirateDeckPlan.TOLERANCE
					&& PirateDeckPlan.standing(entity.getX() - ship.getX(), box.minY - ship.getY(), entity.getZ() - ship.getZ(), ship.getYRot(), true)) {
				return true;
			}
		}
		return false;
	}
}

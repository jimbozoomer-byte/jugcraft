package io.github.jimbozoomer.jugcraft.menagerie;

import com.mojang.authlib.GameProfile;
import io.github.jimbozoomer.jugcraft.artillery.Ballistics;
import io.github.jimbozoomer.jugcraft.concordance.Authority;
import io.github.jimbozoomer.jugcraft.menagerie.CrewedVessel.Role;
import io.github.jimbozoomer.jugcraft.menagerie.CrewedVessel.Station;
import io.github.jimbozoomer.jugcraft.town.Town;
import io.github.jimbozoomer.jugcraft.walker.WalkerAim;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * The crew of a moving pirate ship ({@link CrewedVessel}; docs/features/pirate-ship-crews.md): plunderers seated at
 * the ship's stations as its passengers. The ship calls these from its own overrides; everything else (steering,
 * gunnery, boarding, going back aboard) runs in the plunderer's goals.
 *
 * <ul>
 * <li><b>Stations.</b> Each crew member is a {@code supps_plunderer} linked to its ship (saved {@code Ship},
 * {@code ShipStation}) and riding it at its station, so the game saves it inside the ship and never duplicates it.
 * Plunderers with no link (the static galleons') behave exactly as before.</li>
 * <li><b>Gunnery.</b> Gunners lay their broadside cannon with {@link Ballistics#solve} and fire only when the shell's
 * stepped flight ({@link WalkerAim#flight}) reaches the target through loaded chunks. Each shot spends one gunpowder
 * and one cannonball from the gunner's own finite supplies; the cannonball is damage-only.</li>
 * <li><b>Boarding.</b> A player who comes within {@value #BOARDING_REACH} blocks of the deck in a vehicle, or onto the
 * deck, makes the helm heave to; the deck crew then go over to fight hand to hand. With nobody to fight for
 * {@value #RELEASE_TICKS} ticks the helm lets the ship go and the crew go back to their stations.</li>
 * <li><b>Limits.</b> The crew fight players only: never villagers or townsfolk, and nowhere a claim or a town's
 * protection refuses an unprivileged pirate identity. Nothing here reads or loads an unloaded chunk.</li>
 * </ul>
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
	/** The cannon boat's muzzle speed (OwnerCannonBoat.fireCrew) and the cannonball's own gravity (OwnerCannonball). */
	static final double SHOT_SPEED = 1.8;
	static final double SHOT_GRAVITY = 0.035;
	/** Horizontal reach of a ship's gun, in blocks, as the cannon boat's. */
	static final double MIN_RANGE = 5.0;
	static final double MAX_RANGE = 40.0;
	/** Elevation limits of a broadside gun, degrees up. */
	static final float MIN_PITCH = -10.0F;
	static final float MAX_PITCH = 25.0F;
	/** Ticks between a gunner's shots (the plunderer's own gun cooldown). */
	static final int COOLDOWN = 80;
	/** A gunner's finite supplies when its ship is crewed; never refilled. */
	static final int GUNPOWDER = 8;
	static final int CANNONBALLS = 8;
	/** How close (blocks) a shot must pass the target's middle to be fired at all. */
	static final double SHOT_MARGIN = 2.0;
	/** How far from its ship a crew member off it still counts as its crew for {@link #defeated}, in blocks. */
	static final double CREW_REACH = 64.0;
	/** How near the deck, in blocks, a player must come for the crew to heave to and board. */
	static final double BOARDING_REACH = 12.0;
	/** Ticks with nobody to fight before the helm lets the ship go again. */
	static final int RELEASE_TICKS = 200;
	/** Ticks a crew member off its ship waits, with the ship's last place loaded and no ship there, before giving it up. */
	static final int GONE_TICKS = 100;
	/** How far a crew member's head turns from its station's facing (RaiderCrew's). */
	static final float HEAD_TURN = 75.0F;
	/** The unprivileged identity whose protection answers say where the crew may fight (as Authority's Tiny TNT). */
	private static final GameProfile STAND_IN = new GameProfile(new UUID(0L, 0x5049524154L), "[Pirate crew]");

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
		Station station = station(vessel, passenger);
		return station == null ? null : station.seat().yRot(-vessel.getYRot() * Mth.DEG_TO_RAD);
	}

	/** Whether {@code passenger} may ride {@code vessel}: only its own crew, one to a station. For {@code canAddPassenger}. */
	public static boolean mayBoard(Entity vessel, Entity passenger) {
		if (!(passenger instanceof OwnerPlunderer crew) || !vessel.getUUID().equals(crew.shipLink())
				|| station(vessel, passenger) == null) {
			return false;
		}
		for (Entity other : vessel.getPassengers()) {
			if (other != passenger && other instanceof OwnerPlunderer aboard && aboard.shipStation() == crew.shipStation()) {
				return false;
			}
		}
		return true;
	}

	/** Whether a living crew member holds the helm. The ship moves under its own steering only while this is true. */
	public static boolean helmManned(Entity vessel) {
		for (Entity passenger : vessel.getPassengers()) {
			Station station = station(vessel, passenger);
			if (station != null && station.role() == Role.HELM && passenger.isAlive()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Crews a new ship: one plunderer for each station, linked to it and seated as its passenger. Call once, on the
	 * server, before the ship is in the world; the caller then adds it with {@code addFreshEntityWithPassengers}, so
	 * each crew member is added once, saved inside the ship and never duplicated. Returns how many were seated; a ship
	 * that already carries crew gets none.
	 */
	public static int provision(ServerLevel level, Entity vessel) {
		if (!(vessel instanceof CrewedVessel ship)) {
			return 0;
		}
		for (Entity passenger : vessel.getPassengers()) {
			if (passenger instanceof OwnerPlunderer) {
				return 0;
			}
		}
		Item cannonball = OwnerSupplementaries.ITEMS.get("cannonball");
		int seated = 0;
		for (int index = 0; index < ship.stations().size(); index++) {
			OwnerPlunderer crew = OwnerPlunderer.TYPE.create(level, EntitySpawnReason.STRUCTURE);
			if (crew == null) {
				continue;
			}
			crew.snapTo(vessel.getX(), vessel.getY(), vessel.getZ(), vessel.getYRot(), 0.0F);
			crew.finalizeSpawn(level, level.getCurrentDifficultyAt(vessel.blockPosition()), EntitySpawnReason.STRUCTURE, null);
			crew.setPersistenceRequired();
			crew.link(vessel.getUUID(), index);
			crew.sawShip(vessel.blockPosition());
			if (ship.stations().get(index).role() == Role.GUNNER && cannonball != null) {
				crew.getInventory().addItem(new ItemStack(Items.GUNPOWDER, GUNPOWDER));
				crew.getInventory().addItem(new ItemStack(cannonball, CANNONBALLS));
			}
			if (crew.startRiding(vessel, true, true)) {
				seated++;
			} else {
				crew.discard();
			}
		}
		return seated;
	}

	/**
	 * The ship's loot trigger: whether its crew are all gone, with none aboard and none of its own alive within
	 * {@value #CREW_REACH} blocks. Server side. Cheap enough to call every tick: the entity search runs only once no crew
	 * member is aboard, and reads loaded entities only. It answers false, not yet known, while any chunk in that reach is
	 * unloaded, so a ship just loaded from disk (its passengers already restored) cannot count a crew member as gone merely
	 * because that crew member's own chunk has not loaded yet. The ship rolls its hold once when this first becomes true;
	 * the crew code rolls, gives and drops no hold loot.
	 */
	public static boolean defeated(Entity vessel) {
		for (Entity passenger : vessel.getPassengers()) {
			if (passenger instanceof OwnerPlunderer crew && crew.isAlive() && vessel.getUUID().equals(crew.shipLink())) {
				return false;
			}
		}
		AABB reach = vessel.getBoundingBox().inflate(CREW_REACH);
		if (!(vessel.level() instanceof ServerLevel level) || !WalkerAim.loadedIn(level, reach)) {
			return false;
		}
		return level.getEntitiesOfClass(OwnerPlunderer.class, reach,
				crew -> crew.isAlive() && vessel.getUUID().equals(crew.shipLink())).isEmpty();
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
	/** The station {@code passenger} holds on {@code vessel}, or null if it is not crew there. Both sides (synced). */
	static @Nullable Station station(Entity vessel, Entity passenger) {
		if (!(vessel instanceof CrewedVessel ship) || !(passenger instanceof OwnerPlunderer crew)) {
			return null;
		}
		int index = crew.shipStation();
		return index >= 0 && index < ship.stations().size() ? ship.stations().get(index) : null;
	}

	/** A local point on the ship in world space now: position + local turned by -yaw. */
	static Vec3 world(Entity vessel, Vec3 local) {
		return vessel.position().add(local.yRot(-vessel.getYRot() * Mth.DEG_TO_RAD));
	}

	/**
	 * Each tick, on both sides: a crew member at its station faces the station's way, its head free to turn
	 * {@value #HEAD_TURN} degrees (RaiderCrew.face, plus the station's facing).
	 */
	static void face(OwnerPlunderer crew) {
		Entity vessel = crew.getVehicle();
		Station station = vessel == null ? null : station(vessel, crew);
		if (station == null) {
			return;
		}
		float body = vessel.getYRot() + station.facing();
		crew.yBodyRot = body;
		crew.yBodyRotO = vessel.yRotO + station.facing();
		crew.setYRot(body);
		crew.setYHeadRot(body + Mth.clamp(Mth.wrapDegrees(crew.getYHeadRot() - body), -HEAD_TURN, HEAD_TURN));
	}

	/** Whether the crew may fight at {@code pos}: loaded, outside a town's protection, and not refused by any claim. */
	static boolean mayFight(ServerLevel level, BlockPos pos) {
		if (!level.hasChunkAt(pos) || Town.isProtected(level, pos)) {
			return false;
		}
		return Authority.mayChange(level, FakePlayer.get(level, STAND_IN), pos);
	}

	/** Whether a linked crew member may fight {@code target}: a survival or adventure player, both of them where fighting is allowed. */
	static boolean fair(OwnerPlunderer crew, LivingEntity target) {
		return target instanceof Player player && !player.isCreative() && !player.isSpectator()
				&& crew.level() instanceof ServerLevel level && mayFight(level, crew.blockPosition())
				&& mayFight(level, target.blockPosition());
	}

	/** Whether {@code target} is within boarding reach of the ship's deck. */
	static boolean near(CrewedVessel ship, Entity target) {
		return ship.deckBounds().inflate(BOARDING_REACH).contains(target.position());
	}

	/**
	 * Lays and fires a gunner's cannon at {@code target}, if it is in the station's arc and reach and the shell's
	 * stepped flight passes within {@value #SHOT_MARGIN} blocks of it through loaded chunks only. Spends one gunpowder
	 * and one cannonball from the gunner's supplies. The cannonball is damage-only (OwnerCannonball).
	 */
	static boolean fire(ServerLevel level, Entity vessel, Station station, OwnerPlunderer gunner, LivingEntity target) {
		if (station.role() != Role.GUNNER || !(target instanceof Player)) {
			return false;
		}
		Vec3 muzzle = world(vessel, station.muzzle());
		Vec3 aim = target.getBoundingBox().getCenter();
		Vec3 delta = aim.subtract(muzzle);
		double distance = delta.horizontalDistance();
		if (distance < MIN_RANGE || distance > MAX_RANGE) {
			return false;
		}
		float yaw = (float) Math.toDegrees(Math.atan2(-delta.x, delta.z));
		if (Math.abs(Mth.wrapDegrees(yaw - (vessel.getYRot() + station.facing()))) > station.arc()) {
			return false;
		}
		Float pitch = Ballistics.solve(SHOT_SPEED, SHOT_GRAVITY, distance, delta.y, false, MIN_PITCH, MAX_PITCH);
		if (pitch == null) {
			return false;
		}
		Vec3 velocity = Vec3.directionFromRotation(-pitch, yaw).scale(SHOT_SPEED);
		if (!reaches(WalkerAim.flight(level, muzzle, velocity, SHOT_GRAVITY, gunner), aim)) {
			return false;
		}
		SimpleContainer supplies = gunner.getInventory();
		int powder = slot(supplies, Items.GUNPOWDER);
		int ball = slot(supplies, OwnerSupplementaries.ITEMS.get("cannonball"));
		if (powder < 0 || ball < 0) {
			return false;
		}
		OwnerCannonball shot = new OwnerCannonball(OwnerCannon.BALL, level);
		shot.setOwner(gunner);
		shot.setPos(muzzle);
		shot.setDeltaMovement(velocity);
		// The stepped flight above already cleared the path from the muzzle; the hull itself must not stop the shot.
		if (!WalkerAim.loadedIn(level, shot.getBoundingBox()) || !level.addFreshEntity(shot)) {
			return false;
		}
		supplies.getItem(powder).shrink(1);
		supplies.getItem(ball).shrink(1);
		supplies.setChanged();
		level.playSound(null, muzzle.x, muzzle.y, muzzle.z, OwnerSupplementaries.SOUNDS.get("cannon_fire"), SoundSource.HOSTILE, 2.5F, 1.0F);
		level.sendParticles(ParticleTypes.LARGE_SMOKE, muzzle.x, muzzle.y, muzzle.z, 8, 0.1, 0.1, 0.1, 0.05);
		return true;
	}

	/** Whether a followed flight passes within {@value #SHOT_MARGIN} blocks of {@code aim} before it ends. */
	static boolean reaches(WalkerAim.Flight flight, Vec3 aim) {
		for (Vec3 point : flight.points()) {
			if (point.distanceToSqr(aim) <= SHOT_MARGIN * SHOT_MARGIN) {
				return true;
			}
		}
		return false;
	}

	private static int slot(SimpleContainer container, @Nullable Item item) {
		for (int index = 0; item != null && index < container.getContainerSize(); index++) {
			if (container.getItem(index).is(item)) {
				return index;
			}
		}
		return -1;
	}

	/**
	 * A crew member at its station: faces it, and by its role steers the boarding (helm), fires its cannon (gunner) or
	 * goes over the side to fight (deck crew, once the ship has hove to). A sinking ship's crew abandon it.
	 */
	static final class StationGoal extends Goal {
		private final OwnerPlunderer crew;
		private int quiet;

		StationGoal(OwnerPlunderer crew) {
			this.crew = crew;
			setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
		}

		@Override
		public boolean canUse() {
			Entity vessel = crew.getVehicle();
			return crew.shipLink() != null && vessel != null && crew.shipLink().equals(vessel.getUUID()) && station(vessel, crew) != null;
		}

		@Override
		public boolean requiresUpdateEveryTick() {
			return true;
		}

		@Override
		public void tick() {
			Entity vessel = crew.getVehicle();
			if (!(crew.level() instanceof ServerLevel level) || !(vessel instanceof CrewedVessel ship)) {
				return;
			}
			if (ship.sunk()) {
				crew.stopRiding();
				crew.unlink();
				return;
			}
			Station station = station(vessel, crew);
			if (station == null) {
				return;
			}
			if (crew.tickCount % 20 == 0) {
				crew.sawShip(vessel.blockPosition());
			}
			crew.getNavigation().stop();
			LivingEntity target = crew.getTarget();
			boolean fair = target != null && target.isAlive() && crew.canAttack(target);
			if (fair) {
				crew.getLookControl().setLookAt(target, 30.0F, 30.0F);
			}
			switch (station.role()) {
				case HELM -> {
					if (fair && near(ship, target) && (target.isPassenger() || ship.deckBounds().contains(target.position()))) {
						quiet = 0;
						if (!ship.heaveTo()) {
							ship.setHeaveTo(true);
						}
					} else if (ship.heaveTo() && ++quiet >= RELEASE_TICKS) {
						quiet = 0;
						ship.setHeaveTo(false);
					}
				}
				case GUNNER -> {
					if (fair && crew.gunCooldown() == 0 && fire(level, vessel, station, crew, target)) {
						crew.gunCooldown(COOLDOWN);
					}
				}
				case DECK -> {
					if (fair && (ship.heaveTo() || !helmManned(vessel)) && near(ship, target)) {
						crew.sawShip(vessel.blockPosition());
						crew.stopRiding();
					}
				}
				case LOOKOUT -> {
				}
			}
		}
	}

	/**
	 * A crew member off its ship with nobody to fight goes back aboard to its own station. It looks its ship up among
	 * loaded entities only: while the ship's last place is in an unloaded chunk it waits; once that place is loaded and
	 * the ship is not there for {@value #GONE_TICKS} ticks, or the ship has sunk, it gives the ship up and fights on as
	 * an ordinary plunderer.
	 */
	static final class ReturnGoal extends Goal {
		private final OwnerPlunderer crew;
		private int gone;

		ReturnGoal(OwnerPlunderer crew) {
			this.crew = crew;
			setFlags(EnumSet.of(Flag.MOVE));
		}

		@Override
		public boolean canUse() {
			if (crew.shipLink() == null || crew.isPassenger()) {
				return false;
			}
			LivingEntity target = crew.getTarget();
			return target == null || !target.isAlive() || !crew.canAttack(target);
		}

		@Override
		public void stop() {
			crew.getNavigation().stop();
		}

		@Override
		public void tick() {
			if (!(crew.level() instanceof ServerLevel level) || crew.tickCount % 20 != 0 || crew.shipLink() == null) {
				return;
			}
			Entity found = level.getEntity(crew.shipLink());
			if (!(found instanceof CrewedVessel ship) || found.isRemoved()) {
				BlockPos last = crew.lastShipPos();
				if (last != null && !level.hasChunkAt(last)) {
					gone = 0; // Its ship may be out there in an unloaded chunk: wait, without loading anything.
				} else if ((gone += 20) >= GONE_TICKS) {
					crew.unlink();
				}
				return;
			}
			gone = 0;
			if (ship.sunk()) {
				crew.unlink();
				return;
			}
			crew.sawShip(found.blockPosition());
			Station station = station(found, crew);
			if (station == null) {
				crew.unlink();
				return;
			}
			Vec3 seat = world(found, station.seat());
			if (ship.deckBounds().inflate(3.0).contains(crew.position())) {
				if (mayBoard(found, crew)) {
					crew.getNavigation().stop();
					crew.startRiding(found, true, true);
				}
			} else if (WalkerAim.loadedIn(level, new AABB(crew.position(), seat).inflate(1.0))) {
				crew.getNavigation().moveTo(seat.x, seat.y, seat.z, 1.0);
			}
		}
	}
}

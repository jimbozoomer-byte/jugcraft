package io.github.jimbozoomer.jugcraft.menagerie;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.SmoothFlight;
import io.github.jimbozoomer.jugcraft.walker.WalkerAim;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LinearInterpolationHandler;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * A moving pirate ship (docs/features/pirate-ships.md): a 6 x 16 brig that sails a bounded patrol round the place it was
 * raised, crewed by plunderers at its ten stations ({@link PirateCrew}; docs/features/pirate-ship-crews.md). The server
 * moves it; clients ease it ({@link SmoothFlight}).
 *
 * <ul>
 * <li><b>Sailing.</b> Within {@value #PATROL_RADIUS} blocks of its anchor, waypoint to waypoint, only while a crew member
 * holds the helm, the crew have not hove to, it is afloat and no player stands on its deck; and each step (turn and move)
 * only where the hull's whole swept volume is clear ({@link PirateWaters#sweep}), in loaded water. It never loads a
 * chunk: where the way is unloaded it waits.</li>
 * <li><b>Deck.</b> Four {@link PirateShipDeck} squares along the keel, one block tall from the waterline, so the deck is
 * at y 1.0 and players and boarders stand on it.</li>
 * <li><b>Hold.</b> {@value #HOLD_SLOTS} slots, empty until the crew are beaten ({@link PirateCrew#defeated}); then rolled
 * once from the galleon chest's loot table and never again, saved with the ship. Only a ship the spawner crewed rolls.</li>
 * <li><b>Damage.</b> Players (by hand, arrows or cannonballs, where they may build) and other pirate ships' crews
 * harm it; its own crew never do; {@value #HEALTH} points sink it. A sinking ship spills its
 * hold where it lies, settles for {@value #SINK_TICKS} ticks and is gone. A beaten ship whose hold is empty and which no
 * player has come near for {@value #DERELICT_TICKS} ticks sinks on its own.</li>
 * </ul>
 */
public class PirateShip extends Entity implements CrewedVessel {
	/** The entity box: a square under the middle of the hull (the deck squares do the standing; this one never collides). */
	public static final float WIDTH = 6.0F;
	public static final float HEIGHT = 1.0F;
	/** Blocks from the anchor the ship may sail. */
	public static final double PATROL_RADIUS = 64.0;
	/** Waypoints are at least this far from the anchor. */
	public static final double MIN_LEG = 16.0;
	/** Top speed (blocks a tick), the most it gains or loses in a tick, and the most it turns (degrees a tick). */
	public static final double SPEED = 0.12;
	public static final double ACCELERATION = 0.004;
	public static final float TURN = 1.0F;
	/** Ticks spent on one waypoint before choosing another. */
	public static final int LEG_TICKS = 2_400;
	public static final int HOLD_SLOTS = 27;
	public static final float HEALTH = 300.0F;
	public static final int SINK_TICKS = 200;
	/** How far a sinking ship settles each tick, at most. */
	public static final double SINK_STEP = 0.03;
	public static final int DERELICT_TICKS = 6_000;
	/** Players closer than this keep a beaten ship from sinking on its own. */
	public static final double DERELICT_WATCH = 48.0;
	/** Ticks between a natural ship's reports to {@link PirateSeas}. */
	public static final int REPORT_TICKS = 100;
	/** The hold's loot: the static galleon's own chest table, read and never changed. */
	public static final ResourceKey<LootTable> LOOT = ResourceKey.create(Registries.LOOT_TABLE, Jugcraft.id("supps_loot/galleon/chest"));

	private static final EntityDataAccessor<Boolean> HEAVE_TO = SynchedEntityData.defineId(PirateShip.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Boolean> SUNK = SynchedEntityData.defineId(PirateShip.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Boolean> BEATEN = SynchedEntityData.defineId(PirateShip.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Float> RUDDER = SynchedEntityData.defineId(PirateShip.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Float> HEADWAY = SynchedEntityData.defineId(PirateShip.class, EntityDataSerializers.FLOAT);

	private final SimpleContainer hold = new SimpleContainer(HOLD_SLOTS) {
		@Override
		public boolean stillValid(Player player) {
			return !isRemoved() && !sunk() && player.distanceToSqr(PirateShip.this) < 144.0;
		}
	};
	private final List<PirateShipDeck> decks = new ArrayList<>();
	private Vec3 anchor = Vec3.ZERO;
	/** Whether {@link #anchor} has been set: by {@link #launch}, from its save, or else from where it first ticks. */
	private boolean anchored;
	private @Nullable Vec3 waypoint;
	private boolean natural;
	private boolean crewed;
	private boolean lootRolled;
	private float damage;
	private int sinking;
	private int idle;
	private int legTicks;
	private double speed;
	/** Whether this natural ship has asked {@link PirateSeas} since it was loaded (not saved: every load asks again). */
	private boolean ledgerChecked;
	/** For game tests only, never saved: stands in for the crew's answer to "is the helm manned" and "are they beaten". */
	@Nullable Boolean testHelm;
	@Nullable Boolean testBeaten;

	public PirateShip(EntityType<? extends PirateShip> type, Level level) {
		super(type, level);
	}

	// ------------------------------------------------------------------------------------------------- CrewedVessel

	@Override
	public List<Station> stations() {
		return PirateCrew.STATIONS;
	}

	@Override
	public AABB deckBounds() {
		return PirateCrew.deckBounds(this);
	}

	@Override
	public boolean sunk() {
		return entityData.get(SUNK);
	}

	@Override
	public boolean heaveTo() {
		return entityData.get(HEAVE_TO);
	}

	@Override
	public void setHeaveTo(boolean stop) {
		entityData.set(HEAVE_TO, stop);
	}

	// ------------------------------------------------------------------------------------------------- state

	/** Sets the anchor its patrol keeps to, and whether {@link PirateSeas} raised it. Before it is added. */
	public void launch(Vec3 anchor, boolean natural) {
		this.anchor = anchor;
		this.anchored = true;
		this.natural = natural;
	}

	/** Whether the spawner seated a crew (only such a ship rolls its hold). */
	public void setCrewed(boolean crewed) {
		this.crewed = crewed;
	}

	public Vec3 anchor() {
		return anchor;
	}

	public @Nullable Vec3 waypoint() {
		return waypoint;
	}

	/** Sets the next waypoint (game tests; the patrol chooses its own). */
	void setWaypoint(@Nullable Vec3 waypoint) {
		this.waypoint = waypoint;
		legTicks = 0;
	}

	public boolean natural() {
		return natural;
	}

	public boolean crewed() {
		return crewed;
	}

	/** Whether the crew are beaten and the hold has been rolled (synced: the client lowers the flag). */
	public boolean beaten() {
		return entityData.get(BEATEN);
	}

	public boolean lootRolled() {
		return lootRolled;
	}

	public SimpleContainer hold() {
		return hold;
	}

	public double speed() {
		return speed;
	}

	/** The rudder, -1 to 1 (synced, for the wheel and rudder). */
	public float rudder() {
		return entityData.get(RUDDER);
	}

	/** Headway, 0 to 1 of top speed (synced, for the sails). */
	public float headway() {
		return entityData.get(HEADWAY);
	}

	/** Its deck squares (server side), for game tests. */
	public List<PirateShipDeck> decks() {
		return List.copyOf(decks);
	}

	boolean helmManned() {
		return testHelm != null ? testHelm : PirateCrew.helmManned(this);
	}

	boolean crewBeaten() {
		return testBeaten != null ? testBeaten : PirateCrew.defeated(this);
	}

	/** Whether it may make way this tick. */
	public boolean mayMove() {
		if (sunk() || heaveTo() || !helmManned()) {
			return false;
		}
		// Boarders and visitors on the deck: the ship lies still while anyone stands aboard.
		return level().getEntitiesOfClass(Player.class, deckBounds().inflate(0.5), Player::isAlive).isEmpty();
	}

	// ------------------------------------------------------------------------------------------------- ticking

	@Override
	public LinearInterpolationHandler createInterpolationHandler() {
		return SmoothFlight.handler(this);
	}

	@Override
	public void tick() {
		super.tick();
		if (!(level() instanceof ServerLevel level)) {
			return;
		}
		if (!anchored) {
			// Added without launch (a /summon, say, with no anchor saved): its patrol keeps to where it first floats.
			anchor = position();
			anchored = true;
		}
		if (sunk()) {
			sink(level);
			return;
		}
		if (natural && (!ledgerChecked || tickCount % REPORT_TICKS == 0)) {
			ledgerChecked = true;
			if (!PirateSeas.get(level).seen(getUUID(), PirateSeas.region(anchor.x, anchor.z), BlockPos.containing(anchor), lootRolled,
					level.getGameTime())) {
				vanish();
				return;
			}
		}
		if (tickCount % 20 == 0 && mayRollHold()) {
			rollHold(level);
		}
		if (mayMove()) {
			sail(level);
		} else {
			speed = 0.0;
			entityData.set(RUDDER, 0.0F);
		}
		entityData.set(HEADWAY, (float) (speed / SPEED));
		placeDecks(level);
		if (lootRolled && tickCount % 20 == 0) {
			watchDerelict(level);
		}
	}

	/**
	 * One tick of the patrol: steer for the waypoint and make way. The step (the turn and the move together) is taken
	 * only if the hull's whole swept volume is clear ({@link PirateWaters#sweep}): nothing solid, protected, unloaded or
	 * outside the border where any part of the hull, stern included, would pass. Otherwise it stops and chooses again.
	 */
	private void sail(ServerLevel level) {
		if (waypoint == null || ++legTicks > LEG_TICKS) {
			waypoint = chooseWaypoint(level, getRandom());
			legTicks = 0;
			if (waypoint == null) {
				speed = 0.0;
				entityData.set(RUDDER, 0.0F);
				return;
			}
		}
		float wanted = (float) Math.toDegrees(Math.atan2(-(waypoint.x - getX()), waypoint.z - getZ()));
		float diff = Mth.wrapDegrees(wanted - getYRot());
		float turn = Mth.clamp(diff, -TURN, TURN);
		float yaw = getYRot() + turn;
		double target = Math.abs(diff) < 30.0F ? SPEED : SPEED / 3.0;
		double nextSpeed = speed + Mth.clamp(target - speed, -ACCELERATION, ACCELERATION);
		Vec3 next = position().add(heading(yaw).scale(nextSpeed));
		if (next.subtract(anchor).horizontalDistance() > PATROL_RADIUS || !PirateWaters.sweep(level, position(), getYRot(), next, yaw)) {
			// Something in the hull's way (ahead, beside or astern), unloaded or protected water, or the end of its beat.
			speed = 0.0;
			entityData.set(RUDDER, 0.0F);
			waypoint = null;
			return;
		}
		speed = nextSpeed;
		setYRot(yaw);
		entityData.set(RUDDER, turn / TURN);
		setPos(next);
		if (next.subtract(waypoint).horizontalDistanceSqr() < 9.0) {
			waypoint = null;
		}
	}

	/**
	 * A waypoint {@value #MIN_LEG}-{@value #PATROL_RADIUS} blocks from the anchor, in clear water, with every chunk on the
	 * straight way there loaded; or null this tick (one candidate a tick, so a crowded sea costs little).
	 */
	@Nullable Vec3 chooseWaypoint(ServerLevel level, RandomSource random) {
		double angle = random.nextDouble() * Math.PI * 2.0;
		double distance = MIN_LEG + random.nextDouble() * (PATROL_RADIUS - MIN_LEG);
		Vec3 candidate = new Vec3(anchor.x + Math.cos(angle) * distance, anchor.y, anchor.z + Math.sin(angle) * distance);
		float yaw = (float) Math.toDegrees(Math.atan2(-(candidate.x - getX()), candidate.z - getZ()));
		if (!WalkerAim.loadedAlong(level, position(), candidate) || !PirateWaters.clear(level, candidate, yaw)) {
			return null;
		}
		return candidate;
	}

	/** Its heading: yaw 0 sails +z, the convention of every Jugcraft vehicle. */
	public Vec3 heading() {
		return heading(getYRot());
	}

	static Vec3 heading(float yaw) {
		double radians = Math.toRadians(yaw);
		return new Vec3(-Math.sin(radians), 0.0, Math.cos(radians));
	}

	/** Keeps the four deck squares under the hull; makes them again after a load or if one went. */
	private void placeDecks(ServerLevel level) {
		decks.removeIf(deck -> deck.isRemoved() || deck.ship() != this);
		if (decks.size() != PirateShipDeck.KEEL.length) {
			decks.forEach(Entity::discard);
			decks.clear();
			for (int i = 0; i < PirateShipDeck.KEEL.length; i++) {
				PirateShipDeck deck = PirateShips.DECK.create(level, EntitySpawnReason.EVENT);
				if (deck == null) {
					return;
				}
				deck.attach(this);
				moveDeck(deck, i);
				if (level.addFreshEntity(deck)) {
					decks.add(deck);
				}
			}
			return;
		}
		for (int i = 0; i < decks.size(); i++) {
			moveDeck(decks.get(i), i);
		}
	}

	private void moveDeck(PirateShipDeck deck, int index) {
		Vec3 at = position().add(new Vec3(0.0, 0.0, PirateShipDeck.KEEL[index]).yRot(-getYRot() * Mth.DEG_TO_RAD));
		deck.setPos(at);
		deck.setYRot(getYRot());
	}

	/** Whether the hold should be rolled now: a ship the spawner crewed, not yet rolled, its crew beaten. */
	boolean mayRollHold() {
		return crewed && !lootRolled && crewBeaten();
	}

	/** The hold's loot seed: fixed by the ship's identity, so the one roll it ever makes is reproducible. */
	long lootSeed() {
		long seed = getUUID().getMostSignificantBits() ^ getUUID().getLeastSignificantBits();
		return seed == 0 ? 1 : seed;
	}

	/** Fills {@code into} from {@link #LOOT} as the hold is filled (game tests compare against it). */
	static void fillHold(ServerLevel level, Vec3 origin, long seed, net.minecraft.world.Container into) {
		LootParams params = new LootParams.Builder(level).withParameter(LootContextParams.ORIGIN, origin)
				.create(LootContextParamSets.CHEST);
		level.getServer().reloadableRegistries().getLootTable(LOOT).fill(into, params, seed);
	}

	/** The crew are beaten: the hold is rolled once from {@link #LOOT}, and the ship lies still for its boarders. */
	void rollHold(ServerLevel level) {
		if (lootRolled) {
			return;
		}
		fillHold(level, position(), lootSeed(), hold);
		lootRolled = true;
		entityData.set(BEATEN, true);
		setHeaveTo(true);
		if (natural) {
			PirateSeas.get(level).defeated(getUUID(), level.getGameTime());
		}
	}

	/** A beaten ship with an empty hold that nobody has come near for a while sinks on its own. */
	private void watchDerelict(ServerLevel level) {
		boolean watched = level.getNearestPlayer(this, DERELICT_WATCH) != null;
		if (!hold.isEmpty() || watched) {
			idle = 0;
		} else if ((idle += 20) >= DERELICT_TICKS) {
			beginSinking(level);
		}
	}

	/**
	 * A stale copy of a ship already removed for good (its id is tombstoned: an older chunk save, a restored backup): it
	 * and its crew go quietly, with no loot rolled and nothing spilt, so the removed ship never sails twice.
	 */
	private void vanish() {
		for (Entity passenger : getPassengers()) {
			passenger.discard();
		}
		discard();
	}

	/** Holed below the waterline: the crew abandon it ({@link CrewedVessel#sunk}), and the hold spills where it lies. */
	void beginSinking(ServerLevel level) {
		if (sunk()) {
			return;
		}
		entityData.set(SUNK, true);
		setHeaveTo(true);
		speed = 0.0;
		Containers.dropContents(level, this, hold);
		hold.clearContent();
		level.playSound(null, getX(), getY(), getZ(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.NEUTRAL, 1.5F, 0.6F);
	}

	/**
	 * One tick of settling: down {@value #SINK_STEP} a tick, but never below the hull's checked depth (the waterline row
	 * the anchor floats in, less {@link PirateWaters#DEPTH}), and only while the way down is open water or air, loaded,
	 * and unprotected ({@link PirateWaters#maySink}): it never settles into the seabed, a block or an operator region.
	 */
	private void sink(ServerLevel level) {
		ejectPassengers();
		double next = getY() - SINK_STEP;
		if (PirateWaters.maySink(level, position(), getYRot(), next, Mth.floor(anchor.y))) {
			setPos(getX(), next, getZ());
		}
		placeDecks(level);
		if (++sinking >= SINK_TICKS) {
			discard();
		}
	}

	// ------------------------------------------------------------------------------------------------- riders

	@Override
	protected boolean canAddPassenger(Entity passenger) {
		return !sunk() && PirateCrew.mayBoard(this, passenger);
	}

	@Override
	protected Vec3 getPassengerAttachmentPoint(Entity passenger, EntityDimensions dimensions, float scale) {
		Vec3 seat = PirateCrew.seat(this, passenger);
		return seat != null ? seat : new Vec3(0.0, 1.0, 0.0);
	}

	// ------------------------------------------------------------------------------------------------- players

	/** Use: a beaten ship's hold opens; a crewed one's does not. */
	@Override
	public InteractionResult interact(Player player, InteractionHand hand, Vec3 location) {
		if (sunk()) {
			return InteractionResult.PASS;
		}
		if (level().isClientSide()) {
			return InteractionResult.SUCCESS;
		}
		if (!lootRolled) {
			player.sendOverlayMessage(Component.translatable("message.jugcraft.pirate_ship.crewed"));
			return InteractionResult.SUCCESS;
		}
		player.openMenu(new SimpleMenuProvider((id, inventory, p) -> ChestMenu.threeRows(id, inventory, hold),
				Component.translatable("container.jugcraft.pirate_ship_hold")));
		return InteractionResult.SUCCESS;
	}

	/** Pistons never move it: it goes only where its own checks let it ({@link PirateWaters#sweep}). */
	@Override
	public PushReaction getPistonPushReaction() {
		return PushReaction.IGNORE_ENTITY;
	}

	/** The entity box is not hit; its deck squares are, and pass the blow here. */
	@Override
	public boolean isPickable() {
		return false;
	}

	/**
	 * Hull damage, on the server, judged by the shared protection rules at the ship and at the point of impact (where the
	 * shot struck, for a projectile), so a claim, town or operator region laid over a ship that is already there shields it:
	 * <ul>
	 * <li>from a player (by hand, arrows or cannonballs): only where that player may build (land claims; town and operator
	 * protection), as for a Zeppelin;</li>
	 * <li>from another pirate ship's crew (cannon fire in a sea fight): only where the unprivileged pirate identity
	 * ({@link io.github.jimbozoomer.jugcraft.concordance.Authority#pirateCrewActor}) may change things, the identity its crew
	 * already fight under, with no claim or town exemption;</li>
	 * <li>from its own crew, a galleon plunderer or anything else: never.</li>
	 * </ul>
	 * An operator in creative striking it by hand (where they may build) removes it outright.
	 */
	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
		if (isRemoved() || sunk() || amount <= 0 || !Float.isFinite(amount)) {
			return false;
		}
		Entity attacker = source.getEntity();
		List<BlockPos> struck = new ArrayList<>(List.of(blockPosition()));
		if (source.getDirectEntity() != null && source.getDirectEntity() != attacker) {
			struck.add(source.getDirectEntity().blockPosition());
		}
		if (attacker instanceof Player player) {
			for (BlockPos at : struck) {
				if (!io.github.jimbozoomer.jugcraft.finance.LandRegistry.mayBuild(player, level, at)
						|| io.github.jimbozoomer.jugcraft.town.TownProtection.denies(player, level, at)) {
					return false;
				}
			}
			if (player.getAbilities().instabuild && source.getDirectEntity() == player) {
				Containers.dropContents(level, this, hold);
				hold.clearContent();
				discard();
				return true;
			}
		} else if (attacker instanceof OwnerPlunderer crew && crew.shipLink() != null && !crew.shipLink().equals(getUUID())) {
			Player pirates = io.github.jimbozoomer.jugcraft.concordance.Authority.pirateCrewActor(level);
			for (BlockPos at : struck) {
				if (!io.github.jimbozoomer.jugcraft.concordance.Authority.mayChange(level, pirates, at)) {
					return false;
				}
			}
		} else {
			return false;
		}
		damage += amount;
		if (damage >= HEALTH) {
			beginSinking(level);
		}
		return true;
	}

	public float damage() {
		return damage;
	}

	@Override
	public void remove(RemovalReason reason) {
		if (level() instanceof ServerLevel level) {
			decks.forEach(Entity::discard);
			decks.clear();
			if (reason.shouldDestroy()) {
				// Gone for good: its crew's roster goes (they fight on unlinked), and a natural ship's record is tombstoned.
				PirateCrew.forget(level, getUUID());
				if (natural) {
					PirateSeas.get(level).gone(getUUID(), level.getGameTime());
				}
			}
		}
		super.remove(reason);
	}

	// ------------------------------------------------------------------------------------------------- saving

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		builder.define(HEAVE_TO, false);
		builder.define(SUNK, false);
		builder.define(BEATEN, false);
		builder.define(RUDDER, 0.0F);
		builder.define(HEADWAY, 0.0F);
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		// Only an anchor that has been set: a ship saved before its first tick (summoned, never ticked) anchors where it
		// is next placed, not at the world's origin.
		if (anchored) {
			output.putDouble("AnchorX", anchor.x);
			output.putDouble("AnchorY", anchor.y);
			output.putDouble("AnchorZ", anchor.z);
		}
		output.putBoolean("HasWaypoint", waypoint != null);
		if (waypoint != null) {
			output.putDouble("WaypointX", waypoint.x);
			output.putDouble("WaypointZ", waypoint.z);
		}
		output.putBoolean("Natural", natural);
		output.putBoolean("Crewed", crewed);
		output.putBoolean("LootRolled", lootRolled);
		output.putBoolean("HeaveTo", heaveTo());
		output.putBoolean("Sunk", sunk());
		output.putFloat("Damage", damage);
		output.putInt("Sinking", sinking);
		output.putInt("Idle", idle);
		NonNullList<ItemStack> items = NonNullList.withSize(HOLD_SLOTS, ItemStack.EMPTY);
		for (int i = 0; i < HOLD_SLOTS; i++) {
			items.set(i, hold.getItem(i));
		}
		ContainerHelper.saveAllItems(output, items);
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		double anchorX = input.getDoubleOr("AnchorX", Double.NaN);
		anchored = !Double.isNaN(anchorX);
		// Without a saved anchor (a /summon reads its tag before it is placed), the first tick anchors it where it floats.
		anchor = anchored ? new Vec3(anchorX, input.getDoubleOr("AnchorY", getY()), input.getDoubleOr("AnchorZ", getZ())) : Vec3.ZERO;
		waypoint = input.getBooleanOr("HasWaypoint", false)
				? new Vec3(input.getDoubleOr("WaypointX", anchor.x), anchor.y, input.getDoubleOr("WaypointZ", anchor.z)) : null;
		natural = input.getBooleanOr("Natural", false);
		crewed = input.getBooleanOr("Crewed", false);
		lootRolled = input.getBooleanOr("LootRolled", false);
		entityData.set(BEATEN, lootRolled);
		entityData.set(HEAVE_TO, input.getBooleanOr("HeaveTo", false));
		entityData.set(SUNK, input.getBooleanOr("Sunk", false));
		damage = Mth.clamp(input.getFloatOr("Damage", 0.0F), 0.0F, HEALTH);
		sinking = Mth.clamp(input.getIntOr("Sinking", 0), 0, SINK_TICKS);
		idle = Math.max(0, input.getIntOr("Idle", 0));
		NonNullList<ItemStack> items = NonNullList.withSize(HOLD_SLOTS, ItemStack.EMPTY);
		ContainerHelper.loadAllItems(input, items);
		for (int i = 0; i < HOLD_SLOTS; i++) {
			hold.setItem(i, items.get(i));
		}
	}
}

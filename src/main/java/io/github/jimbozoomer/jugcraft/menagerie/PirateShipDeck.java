package io.github.jimbozoomer.jugcraft.menagerie;

import io.github.jimbozoomer.jugcraft.SmoothFlight;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.LinearInterpolationHandler;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * One stretch of a pirate ship's deck (docs/features/pirate-ships.md): a square, one block tall from the waterline to the
 * deck at y 1.0, that players, boarders and crew stand on. An entity's own box is a square that cannot turn, so the ship's
 * deck is six of these along its keel ({@link PirateDeckPlan}: 5-block squares amidships, smaller ones following the fine
 * bow), moved with it every tick; each square's size comes from its index (synced). They are never saved: the ship makes
 * them again when it loads. Hitting or using one hits or uses its ship.
 */
public class PirateShipDeck extends Entity {
	/** The widest square (the type's size), and each square's centre along the keel (ship-local z), bow last. */
	public static final float WIDTH = 5.0F;
	public static final double[] KEEL = PirateDeckPlan.SQUARE_CENTRES;
	private static final EntityDataAccessor<Integer> SHIP = SynchedEntityData.defineId(PirateShipDeck.class, EntityDataSerializers.INT);
	/** Which square of the deck this is (its size and place), synced so clients size it alike. */
	private static final EntityDataAccessor<Integer> INDEX = SynchedEntityData.defineId(PirateShipDeck.class, EntityDataSerializers.INT);

	public PirateShipDeck(EntityType<? extends PirateShipDeck> type, Level level) {
		super(type, level);
	}

	/** Belongs to {@code ship} as its square {@code index} (before it is added to the level). */
	void attach(PirateShip ship, int index) {
		entityData.set(SHIP, ship.getId());
		entityData.set(INDEX, index);
		refreshDimensions();
	}

	/** Which square of the deck this is. */
	public int index() {
		return entityData.get(INDEX);
	}

	/** The square's side, from its index (PirateDeckPlan.SQUARE_WIDTHS); the widest before the index is known. */
	public static float width(int index) {
		return index >= 0 && index < PirateDeckPlan.SQUARE_WIDTHS.length ? (float) PirateDeckPlan.SQUARE_WIDTHS[index] : WIDTH;
	}

	@Override
	public EntityDimensions getDimensions(Pose pose) {
		// Called from Entity's constructor too, before the synced data exists (as JimtaSpiderPart guards).
		return EntityDimensions.scalable(width(entityData == null ? -1 : index()), 1.0F);
	}

	@Override
	public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
		super.onSyncedDataUpdated(key);
		if (key == INDEX) {
			refreshDimensions();
		}
	}

	/** Its ship, on either side, or null once the ship is gone. */
	public @Nullable PirateShip ship() {
		return level().getEntity(entityData.get(SHIP)) instanceof PirateShip ship && !ship.isRemoved() ? ship : null;
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		builder.define(SHIP, -1);
		builder.define(INDEX, -1);
	}

	@Override
	public void tick() {
		super.tick();
		if (!level().isClientSide() && ship() == null) {
			discard();
		}
	}

	@Override
	public LinearInterpolationHandler createInterpolationHandler() {
		return SmoothFlight.handler(this);
	}

	/** Players, boarders and loose items stand on it; its ship and the other squares do not. */
	@Override
	public boolean canBeCollidedWith(Entity other) {
		return !isRemoved() && !(other instanceof PirateShipDeck) && !(other instanceof PirateShip);
	}

	@Override
	public boolean canCollideWith(Entity other) {
		return false;
	}

	@Override
	public boolean isPushable() {
		return false;
	}

	/** Pistons never move it (its ship sets it in place every tick). */
	@Override
	public PushReaction getPistonPushReaction() {
		return PushReaction.IGNORE_ENTITY;
	}

	@Override
	public boolean isPickable() {
		return !isRemoved();
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
		PirateShip ship = ship();
		return ship != null && ship.hurtServer(level, source, amount);
	}

	@Override
	public InteractionResult interact(Player player, InteractionHand hand, Vec3 location) {
		PirateShip ship = ship();
		return ship == null ? InteractionResult.PASS : ship.interact(player, hand, location);
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
	}
}

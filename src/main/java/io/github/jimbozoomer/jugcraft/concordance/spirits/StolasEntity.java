package io.github.jimbozoomer.jugcraft.concordance.spirits;

import com.geckolib.animation.RawAnimation;
import io.github.jimbozoomer.jugcraft.concordance.Authority;
import io.github.jimbozoomer.jugcraft.concordance.worker.PactDefinition;
import io.github.jimbozoomer.jugcraft.concordance.worker.Pacts;
import io.github.jimbozoomer.jugcraft.concordance.worker.Status;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/**
 * Concordance Chunk D, D-04 (D-S7): Stolas, a worker (here in spirits/, so check_workers' bans on chunk loading,
 * dimension changes and brain memories cover him; MISC, so transport crates refuse him). While a conversation is open at
 * his perch and nothing is kept there he is a visitor, bound to no one and on no roster; he leaves when he has neither a
 * pact nor a live conversation (declined, closed, timed out, its player gone, a restart), with the Concordance on or off:
 * a visitor is saved with his chunk (his perch only) and leaves at his first think after it loads. Once a pact is sealed
 * he is its one presence; he leaves when the pact is ended, kept elsewhere or answered by another. Only damage that
 * bypasses invulnerability harms him (Styx's rule); if he is lost all the same, the pact is not: its holder calls him
 * again at no cost. He keeps to his perch. Provisional body (D-S5, unit 2): the owner's Flashjack rig under his name;
 * D-06 gives him his own.
 */
public class StolasEntity extends WorkerEntity<StolasEntity> implements AgreementHolder.Presence {
	/** His pacts' counterpart (the pact definition's id). */
	public static final String COUNTERPART = PactDefinition.STOLAS;
	// ---------------------------------------------------------------- his provisional body (D-S5, unit 2)
	/** Perched and serving, or hearing a visitor: the owner's Flashjack settling onto its feet, held there. */
	private static final RawAnimation PERCHED = RawAnimation.begin().thenPlayAndHold("idle.sitting");
	/** Off duty (suspended, held for upkeep, his holder away, the Concordance off): standing, breathing. */
	private static final RawAnimation GROUNDED = RawAnimation.begin().thenLoop("idle.ground");
	/** Astir (an offering due; travelling or working, which he never is in unit 2): wings beating. */
	private static final RawAnimation ASTIR = RawAnimation.begin().thenLoop("idle.flapping");

	private @Nullable BlockPos perch;
	private @Nullable UUID record;

	public StolasEntity(EntityType<? extends PathfinderMob> type, Level level) {
		super(type, level);
		setNoGravity(true);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 20.0).add(Attributes.MOVEMENT_SPEED, 0.0).add(Attributes.FOLLOW_RANGE, 8.0);
	}

	@Override
	public String kind() {
		return "pact";
	}

	/**
	 * Stolas joins his holder's roster only when his pact is sealed, he is called again or the pact is seated
	 * (AgreementHolder's note); a change of status never puts one replaced, or loaded while the Concordance is off, back
	 * on it (unit 1's unit review fix 2, as for the Gathering Shade; applied at the merge of unit 2).
	 */
	@Override
	protected boolean joinsRosterOnStatus() {
		return false;
	}

	public @Nullable BlockPos perch() {
		return perch;
	}

	public void setPerch(BlockPos at) {
		perch = at.immutable();
	}

	/** The Bound Will he serves (null for a visitor, and for a presence until it first thinks). */
	public @Nullable UUID record() {
		return record;
	}

	@Override
	public @Nullable BlockPos seat() {
		return perch;
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
		return source.is(DamageTypeTags.BYPASSES_INVULNERABILITY) && super.hurtServer(level, source, amount);
	}

	@Override
	public boolean isPushable() {
		return false;
	}

	@Override
	public Status think(ServerLevel level, long time, long gameTime) {
		Status next = decide(level, gameTime);
		if (!isRemoved()) {
			setStatus(next);
		}
		return next;
	}

	private Status decide(ServerLevel level, long gameTime) {
		if (owner == null && record == null && !awaited(level, gameTime)) {
			// A visitor no conversation waits on leaves whether or not the Concordance is on: he holds no record, and one
			// saved with his chunk is no perch's visitor when it loads (the unit review).
			leave(level, null, blockPosition());
			return Status.FINISHED;
		}
		if (!Workers.enabled()) {
			stop();
			return Status.DISABLED;
		}
		if (perch == null) {
			leave(level, null, blockPosition());
			return Status.FINISHED;
		}
		if (!level.isLoaded(perch)) {
			stop();
			return Status.DESTINATION_UNLOADED;
		}
		PactPerchBlockEntity home = level.getBlockEntity(perch) instanceof PactPerchBlockEntity found ? found : null;
		if (home == null) {
			leave(level, null, perch);
			return Status.FINISHED;
		}
		stay();
		if (record == null) {
			if (owner != null && home.holder().agreement() != null && getUUID().equals(home.holder().spirit())) {
				record = home.holder().agreement().record();
			} else if (owner == null && getUUID().equals(home.visitor())
					&& PactSessions.live(level.getServer(), GlobalPos.of(level.dimension(), perch), gameTime)) {
				return Status.IDLE;
			} else {
				leave(level, home, perch);
				return Status.FINISHED;
			}
		}
		AgreementHolder.Standing standing = home.holder().verify(level, gameTime);
		if (standing == AgreementHolder.Standing.UNREADABLE) {
			stop();
			return Status.OUTSIDE_AGREEMENT;
		}
		if (standing != AgreementHolder.Standing.VALID || !getUUID().equals(home.holder().spirit())
				|| !record.equals(home.holder().agreement().record())) {
			leave(level, home, perch);
			return Status.FINISHED;
		}
		// Its pact's state as its Bound Will says (D-S3, D-S9): suspended by its holder, an offering due, or held for upkeep.
		Status state = home.holder().state(level);
		if (state != null) {
			return state;
		}
		return Authority.presentNear(level, owner, perch, Pacts.REACH) == null ? Status.OWNER_OFFLINE : Status.IDLE;
	}

	/** Whether a conversation still waits on this visitor: his perch loaded, he its visitor, a conversation there live. */
	private boolean awaited(ServerLevel level, long gameTime) {
		return perch != null && level.isLoaded(perch) && level.getBlockEntity(perch) instanceof PactPerchBlockEntity home
				&& getUUID().equals(home.visitor()) && PactSessions.live(level.getServer(), GlobalPos.of(level.dimension(), perch), gameTime);
	}

	/** Pushed or led off his perch, he is back on it (within his own level, check_workers). */
	private void stay() {
		double x = perch.getX() + 0.5, y = perch.getY() + 1.0, z = perch.getZ() + 0.5;
		if (distanceToSqr(x, y, z) > 1.0) {
			teleportTo(x, y, z);
		}
	}

	@Override
	public void leave(ServerLevel level, AgreementHolder.@Nullable Holds home, BlockPos at) {
		if (isRemoved()) {
			return;
		}
		if (owner != null) {
			WorkerRoster.of(level.getServer()).remove(owner, getUUID());
		}
		discard();
	}

	/** Killed or discarded: struck off his holder's roster, which is how the perch knows he is lost (D-S18's rule). */
	@Override
	public void remove(RemovalReason reason) {
		if (reason.shouldDestroy() && owner != null && level() instanceof ServerLevel server) {
			WorkerRoster.of(server.getServer()).remove(owner, getUUID());
		}
		super.remove(reason);
	}

	@Override
	protected RawAnimation animation(Status status) {
		return switch (status) {
			case TRAVELLING, RETURNING, WORKING, UPKEEP_DUE -> ASTIR;
			case SUSPENDED, UPKEEP_HELD, OWNER_OFFLINE, OTHER_DIMENSION, OUTSIDE_AGREEMENT, DESTINATION_UNLOADED, FINISHED, DISABLED -> GROUNDED;
			default -> PERCHED;
		};
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		if (perch != null) {
			output.store("perch", BlockPos.CODEC, perch);
		}
		if (record != null) {
			output.store("record", UUIDUtil.CODEC, record);
		}
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		perch = input.read("perch", BlockPos.CODEC).orElse(null);
		record = input.read("record", UUIDUtil.CODEC).orElse(null);
	}
}

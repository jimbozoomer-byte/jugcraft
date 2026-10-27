package io.github.jimbozoomer.jugcraft.creatures.herobrine;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Herobrine (docs/features/herobrine.md): a figure who stands still, stares at the one player he came for, and is gone
 * the moment anyone comes within {@value #VANISH_DISTANCE} blocks, stares back too long, or hits him. He never attacks,
 * breaks a block, carries a name tag or is saved with the world: {@link HerobrineEncounters} places him for one
 * encounter and he removes himself. While he stands he notes every attentive player who really sees him (on their
 * screen, nothing in the way, for {@value #SEEN_TICKS} ticks); when he goes, the encounter ends with those witnesses.
 */
public final class Herobrine extends PathfinderMob {
	/** A distant encounter ends when any player comes this close, in blocks. */
	public static final double VANISH_DISTANCE = 12.0;
	/** A distant encounter ends after the player has looked straight at him for this many ticks. */
	public static final int STARE_TICKS = 40;
	/** The longest a distant encounter lasts, in ticks. */
	public static final int DISTANT_TICKS = 600;
	/** The longest a close encounter lasts, in ticks: time to turn round. */
	public static final int CLOSE_TICKS = 60;
	/** A close encounter ends once the player has looked at him this many ticks: long enough to know what they saw. */
	public static final int CLOSE_STARE_TICKS = 12;
	/** A player has seen him once he has been on their screen, unobstructed, for this many ticks. */
	public static final int SEEN_TICKS = 10;
	/** Players further than this cannot see him, and his target going further ends the encounter, in blocks. */
	public static final double WITNESS_RANGE = 96.0;
	/** How near the middle of a player's view he must be to count as on their screen: the cosine of the angle. */
	private static final double ON_SCREEN_COSINE = 0.6;
	/** How directly a player must look at him to count as staring: the cosine of the angle off their aim. */
	private static final double DISTANT_STARE_COSINE = 0.985;
	private static final double CLOSE_STARE_COSINE = 0.85;

	/** What kind of encounter he is here for: to be glimpsed at a distance, or to stand right behind someone. */
	public enum Kind {
		DISTANT, CLOSE
	}

	/** Why he went. */
	public enum Outcome {
		/** His player looked straight at him long enough. */
		STARED_DOWN,
		/** A player came within {@value #VANISH_DISTANCE} blocks of a distant encounter. */
		APPROACHED,
		/** A player hit him. */
		HIT,
		/** His time ran out. */
		EXPIRED,
		/** His player logged out, died, left the dimension or went out of range. */
		TARGET_LOST,
		/** He was removed some other way: his chunk unloaded, the server stopped, or a command. */
		REMOVED
	}

	private Kind kind = Kind.DISTANT;
	private @Nullable UUID target;
	private UUID encounter = UUID.randomUUID();
	private int life = DISTANT_TICKS;
	private int stared;
	private @Nullable Outcome outcome;
	private final Map<UUID, Integer> watching = new HashMap<>();
	private final Set<UUID> witnesses = new LinkedHashSet<>();

	public Herobrine(EntityType<? extends Herobrine> type, Level level) {
		super(type, level);
		setNoAi(true);
	}

	public static AttributeSupplier.Builder attributes() {
		return PathfinderMob.createMobAttributes().add(Attributes.MAX_HEALTH, 20.0).add(Attributes.MOVEMENT_SPEED, 0.0)
				.add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
	}

	/** Sends him for {@code player} as encounter {@code id}, and turns him their way. */
	public void haunt(Kind kind, Player player, UUID id) {
		this.kind = kind;
		target = player.getUUID();
		encounter = id;
		life = kind == Kind.CLOSE ? CLOSE_TICKS : DISTANT_TICKS;
		stared = 0;
		face(player);
	}

	public Kind kind() {
		return kind;
	}

	public @Nullable UUID target() {
		return target;
	}

	public UUID encounter() {
		return encounter;
	}

	/** Every player who has really seen him so far, in the order they did. */
	public Set<UUID> witnesses() {
		return Collections.unmodifiableSet(witnesses);
	}

	@Override
	public void tick() {
		super.tick();
		if (!(level() instanceof ServerLevel level) || isRemoved()) {
			return;
		}
		Player player = target == null ? null : level.getPlayerByUUID(target);
		if (player == null || !player.isAlive() || player.isSpectator() || distanceToSqr(player) > WITNESS_RANGE * WITNESS_RANGE) {
			end(level, Outcome.TARGET_LOST);
			return;
		}
		face(player);
		noteWitnesses(level);
		if (kind == Kind.DISTANT && someoneClose(level, position())) {
			end(level, Outcome.APPROACHED);
			return;
		}
		stared = isStaredAtBy(player) ? stared + 1 : 0;
		if (stared >= (kind == Kind.CLOSE ? CLOSE_STARE_TICKS : STARE_TICKS)) {
			end(level, Outcome.STARED_DOWN);
		} else if (--life <= 0) {
			end(level, Outcome.EXPIRED);
		}
	}

	private void noteWitnesses(ServerLevel level) {
		for (Player player : level.getEntitiesOfClass(Player.class, getBoundingBox().inflate(WITNESS_RANGE),
				p -> !p.isSpectator() && p.isAlive())) {
			// Only someone paying attention can see him: not idle, asleep, in a menu or in danger.
			if (witnesses.contains(player.getUUID()) || !(player instanceof ServerPlayer watcher)
					|| !HerobrineEncounters.attentive(watcher, level.getServer().getTickCount()) || !isOnScreenOf(player)) {
				continue;
			}
			int ticks = watching.merge(player.getUUID(), 1, Integer::sum);
			if (ticks >= SEEN_TICKS) {
				witness(player);
			}
		}
	}

	private void witness(Player player) {
		if (witnesses.add(player.getUUID())) {
			HerobrineEncounters.seen(this, player.getUUID(), kind);
		}
	}

	/** Whether a player (not spectating) is within {@value #VANISH_DISTANCE} blocks of {@code at}. */
	static boolean someoneClose(ServerLevel level, Vec3 at) {
		return !level.getEntitiesOfClass(Player.class, new AABB(at, at).inflate(VANISH_DISTANCE),
				p -> !p.isSpectator() && p.distanceToSqr(at) < VANISH_DISTANCE * VANISH_DISTANCE).isEmpty();
	}

	/** Whether he is on {@code player}'s screen, in range, with nothing in the way. */
	public boolean isOnScreenOf(Player player) {
		return within(player, ON_SCREEN_COSINE) && distanceToSqr(player) <= WITNESS_RANGE * WITNESS_RANGE && player.hasLineOfSight(this);
	}

	/** Whether {@code player} is looking straight at him, with nothing in the way. */
	public boolean isStaredAtBy(Player player) {
		return within(player, kind == Kind.CLOSE ? CLOSE_STARE_COSINE : DISTANT_STARE_COSINE) && player.hasLineOfSight(this);
	}

	private boolean within(Player player, double cosine) {
		Vec3 toHim = getEyePosition().subtract(player.getEyePosition());
		double length = toHim.length();
		return length < 1.0E-4 || player.getViewVector(1.0F).dot(toHim.scale(1.0 / length)) > cosine;
	}

	private void face(Player player) {
		double dx = player.getX() - getX();
		double dz = player.getZ() - getZ();
		double dy = player.getEyeY() - getEyeY();
		float yaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90.0F;
		float pitch = (float) -Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)));
		setYRot(yaw);
		yBodyRot = yaw;
		setYHeadRot(yaw);
		setXRot(pitch);
	}

	/** Ends the encounter: a wisp of smoke where he stood, and he is gone. */
	public void end(ServerLevel level, Outcome why) {
		if (outcome == null) {
			outcome = why;
		}
		level.sendParticles(ParticleTypes.SMOKE, getX(), getY() + 1.0, getZ(), 6, 0.2, 0.5, 0.2, 0.0);
		discard();
	}

	@Override
	public void remove(RemovalReason reason) {
		if (level() instanceof ServerLevel && !isRemoved()) {
			HerobrineEncounters.ended(this, outcome == null ? Outcome.REMOVED : outcome);
		}
		super.remove(reason);
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
		if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
			return super.hurtServer(level, source, amount);
		}
		// A blow or an arrow from a player meets nothing: he is gone, and nobody is hurt. Whoever struck him saw him.
		if (source.getEntity() instanceof Player player) {
			witness(player);
			end(level, Outcome.HIT);
		}
		return false;
	}

	@Override
	public boolean isPushable() {
		return false;
	}

	@Override
	public boolean removeWhenFarAway(double distance) {
		return false;
	}
}

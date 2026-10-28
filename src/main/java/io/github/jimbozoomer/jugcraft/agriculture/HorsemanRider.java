package io.github.jimbozoomer.jugcraft.agriculture;

import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * The Headless Horseman as he is seen riding in the Halloween season (docs/features/horseman-sightings.md): sent by
 * {@link HorsemanSightings} at one player, he gallops at them out of the dark, swings half round them
 * {@value #CIRCLE_RADIUS} blocks off, flings a flaming pumpkin at their feet that only frightens
 * ({@link FlamingPumpkin#harmless}), and rides off, gone in a puff of smoke {@value #LEAVE_TICKS} ticks later. A
 * sighting, not a fight: he passes through whatever stands in his way, cannot be struck, pushed or hurt, harms nobody,
 * breaks nothing and is never saved. Everyone near when he throws has seen him ({@link HorsemanSightings#witnessed}).
 * If the player he rides at leaves, dies or is gone from the overworld, he rides off at once. The summoned Horseman,
 * who fights, is {@link HeadlessHorseman}; the two share a body.
 */
public class HorsemanRider extends Mob {
	public static final double APPROACH_SPEED = 0.55;
	public static final int APPROACH_TICKS = 80;
	public static final double CIRCLE_RADIUS = 6.0;
	public static final int CIRCLE_TICKS = 30;
	public static final int THROW_AT = 12;
	public static final double LEAVE_SPEED = 0.6;
	public static final int LEAVE_TICKS = 50;
	public static final int MAX_TICKS = 240;

	/** The parts of his ride. */
	public enum Phase {
		APPROACH, CIRCLE, LEAVE
	}

	private @Nullable UUID quarry;
	private Phase phase = Phase.APPROACH;
	private int phaseTicks;
	/** Where he is about the player as he begins to circle (radians), and which way round he goes (1 or -1). */
	private double circleFrom;
	private int circleWay = 1;
	private Vec3 leaveWay = new Vec3(0.0, 0.0, 1.0);
	private boolean thrown;

	public HorsemanRider(EntityType<? extends HorsemanRider> type, Level level) {
		super(type, level);
		setNoGravity(true);
		noPhysics = true;
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 20.0).add(Attributes.MOVEMENT_SPEED, 0.3);
	}

	/** Sets him at {@code from}, facing {@code player}, whom he rides at. */
	public void rideAt(ServerPlayer player, Vec3 from) {
		quarry = player.getUUID();
		snapTo(from.x, from.y, from.z, 0.0F, 0.0F);
		face(player.position().subtract(from));
	}

	public Phase phase() {
		return phase;
	}

	/** Whether he has flung his pumpkin yet. */
	public boolean thrown() {
		return thrown;
	}

	public @Nullable UUID quarry() {
		return quarry;
	}

	@Override
	protected void customServerAiStep(ServerLevel level) {
		super.customServerAiStep(level);
		phaseTicks++;
		if (tickCount > MAX_TICKS) {
			vanish(level);
			return;
		}
		ServerPlayer player = quarry == null ? null : level.getServer().getPlayerList().getPlayer(quarry);
		if (phase != Phase.LEAVE && (player == null || !player.isAlive() || player.level() != level || player.isSpectator())) {
			leave(level, facing());
		}
		switch (phase) {
			case APPROACH -> approach(level, player);
			case CIRCLE -> circle(level, player);
			case LEAVE -> {
				gallop(level, leaveWay, LEAVE_SPEED);
				if (phaseTicks >= LEAVE_TICKS) {
					vanish(level);
					return;
				}
			}
		}
		if (tickCount % 5 == 0) {
			level.playSound(null, getX(), getY(), getZ(), SoundEvents.HORSE_GALLOP, SoundSource.HOSTILE, 1.5F, 0.8F);
		}
	}

	/** He gallops straight at the player, the fog thickening round them, until he is {@value #CIRCLE_RADIUS} blocks off. */
	private void approach(ServerLevel level, ServerPlayer player) {
		Vec3 to = new Vec3(player.getX() - getX(), 0.0, player.getZ() - getZ());
		double distance = to.length();
		if (distance <= CIRCLE_RADIUS + 0.5 || phaseTicks > APPROACH_TICKS) {
			phase = Phase.CIRCLE;
			phaseTicks = 0;
			circleFrom = Math.atan2(getZ() - player.getZ(), getX() - player.getX());
			circleWay = getRandom().nextBoolean() ? 1 : -1;
			return;
		}
		gallop(level, to, Math.min(APPROACH_SPEED, distance - CIRCLE_RADIUS));
		if (phaseTicks % 4 == 0) {
			for (int i = 0; i < 6; i++) {
				double angle = getRandom().nextDouble() * Math.PI * 2.0;
				double reach = 3.0 + getRandom().nextDouble() * 5.0;
				level.sendParticles(ParticleTypes.CLOUD, player.getX() + Math.cos(angle) * reach, player.getY() + 0.3,
						player.getZ() + Math.sin(angle) * reach, 1, 0.3, 0.1, 0.3, 0.0);
			}
		}
	}

	/** He swings half round the player, flings his pumpkin at their feet on the way, then rides off away from them. */
	private void circle(ServerLevel level, ServerPlayer player) {
		double angle = circleFrom + circleWay * Math.PI * Math.min(1.0, phaseTicks / (double) CIRCLE_TICKS);
		Vec3 goal = new Vec3(player.getX() + Math.cos(angle) * CIRCLE_RADIUS, getY(), player.getZ() + Math.sin(angle) * CIRCLE_RADIUS);
		Vec3 step = new Vec3(goal.x - getX(), 0.0, goal.z - getZ());
		gallop(level, step, step.length());
		if (phaseTicks == THROW_AT) {
			fling(level, player);
		}
		if (phaseTicks >= CIRCLE_TICKS) {
			leave(level, new Vec3(getX() - player.getX(), 0.0, getZ() - player.getZ()));
		}
	}

	/** He flings a frightening pumpkin at the player's feet, cackling; everyone near has now seen him. */
	private void fling(ServerLevel level, ServerPlayer player) {
		FlamingPumpkin pumpkin = FlamingPumpkin.harmless(level, this);
		double dx = player.getX() - getX();
		double dz = player.getZ() - getZ();
		double dy = player.getY() - pumpkin.getY();
		double flat = Math.sqrt(dx * dx + dz * dz);
		pumpkin.shoot(dx, dy + flat * 0.2, dz, 0.9F, 0.0F);
		level.addFreshEntity(pumpkin);
		thrown = true;
		level.playSound(null, getX(), getY(), getZ(), SoundEvents.FIRECHARGE_USE, SoundSource.HOSTILE, 1.0F, 0.7F);
		level.playSound(null, getX(), getY(), getZ(), SoundEvents.WITCH_CELEBRATE, SoundSource.HOSTILE, 2.0F, 0.5F);
		HorsemanSightings.witnessed(level, player.position());
	}

	private void leave(ServerLevel level, Vec3 way) {
		phase = Phase.LEAVE;
		phaseTicks = 0;
		leaveWay = way.lengthSqr() < 1.0E-4 ? facing() : way;
		level.playSound(null, getX(), getY(), getZ(), SoundEvents.SKELETON_HORSE_AMBIENT, SoundSource.HOSTILE, 2.0F, 0.6F);
	}

	/** Gone in a puff of smoke, with a last cackle, to everyone near. */
	public void vanish(ServerLevel level) {
		level.sendParticles(ParticleTypes.LARGE_SMOKE, getX(), getY() + 1.0, getZ(), 40, 0.8, 1.0, 0.8, 0.02);
		level.playSound(null, getX(), getY(), getZ(), SoundEvents.WITCH_CELEBRATE, SoundSource.HOSTILE, 2.0F, 0.6F);
		for (ServerPlayer player : level.getPlayers(player -> player.distanceToSqr(this) < HorsemanSightings.WITNESS_RANGE
				* HorsemanSightings.WITNESS_RANGE)) {
			player.sendSystemMessage(Component.translatable("message.jugcraft.horseman.sighting_gone"));
		}
		discard();
	}

	/** He gallops {@code speed} blocks a tick along {@code way}, over the ground, through whatever stands in it. */
	private void gallop(ServerLevel level, Vec3 way, double speed) {
		Vec3 flat = new Vec3(way.x, 0.0, way.z);
		if (flat.lengthSqr() < 1.0E-6) {
			setDeltaMovement(Vec3.ZERO);
			return;
		}
		flat = flat.normalize().scale(speed);
		double x = getX() + flat.x;
		double z = getZ() + flat.z;
		BlockPos column = BlockPos.containing(x, getY(), z);
		double ground = level.hasChunkAt(column) ? level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, column.getX(), column.getZ()) : getY();
		setDeltaMovement(flat.x, Math.max(-1.0, Math.min(1.0, ground - getY())), flat.z);
		face(flat);
	}

	/** The way he faces, flat. */
	public Vec3 facing() {
		double yaw = Math.toRadians(getYRot());
		return new Vec3(-Math.sin(yaw), 0.0, Math.cos(yaw));
	}

	private void face(Vec3 way) {
		if (way.x * way.x + way.z * way.z > 1.0E-4) {
			float yaw = (float) (Math.toDegrees(Math.atan2(way.z, way.x)) - 90.0);
			setYRot(yaw);
			setYBodyRot(yaw);
			setYHeadRot(yaw);
		}
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
		return false;
	}

	@Override
	public boolean isPickable() {
		return false;
	}

	@Override
	public boolean isPushable() {
		return false;
	}

	@Override
	protected void doPush(Entity entity) {
	}

	@Override
	public boolean canBeLeashed() {
		return false;
	}

	@Override
	public boolean removeWhenFarAway(double distance) {
		return false;
	}

	@Override
	public boolean shouldDropExperience() {
		return false;
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.SKELETON_HORSE_AMBIENT;
	}
}

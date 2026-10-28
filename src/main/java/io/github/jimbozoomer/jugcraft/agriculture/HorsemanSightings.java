package io.github.jimbozoomer.jugcraft.agriculture;

import com.mojang.serialization.Codec;
import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.config.JugcraftConfig;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Sightings of the Headless Horseman in the Halloween season (docs/features/horseman-sightings.md): a few times a
 * season every player sees him ride out of the dark at them ({@link HorsemanRider}), and nobody comes to harm.
 *
 * <p>A player is due a sighting on their first night of the season, then once {@value #MIN_GAP_DAYS} to
 * {@value #MAX_GAP_DAYS} days (the server's dates, as the season's) have passed since the last one, and again on
 * Halloween itself ({@link HalloweenSeason#harvestMoon()}) if they have not yet seen him that day. Every
 * {@value #CHECK_TICKS} ticks, on a night that is passing (the overworld's clock moving: never while time stands still),
 * each player due who is out under the open sky in the overworld, has been in the game {@value #SETTLE_TICKS} ticks or
 * more and is not asleep is sent him {@value #START_CHANCE} of the time, so he comes at an unforeseen moment; never
 * with another rider or the summoned Horseman within {@value #ONE_AT_A_TIME} blocks. He comes from a spot
 * {@value #START_DISTANCE} blocks off, in front of them (within 70 degrees of where they look, in sight if any such
 * spot is), on loaded ground no more than {@value #CLIMB} blocks above or below them; never through an unloaded chunk.
 * Everyone within {@value #WITNESS_RANGE} blocks when he flings his pumpkin has seen him: their record is today, and they
 * earn Sleepy Hollow. A player's record ({@link #SEEN}, the day they last saw him) is kept with them, through death.
 *
 * <p>The server's {@code halloween.horseman_sightings} ({@code on} or {@code off}) and the {@code agriculture} switch
 * turn them off; the season's own dates and {@code halloween.mode} bound them as every Halloween event is bound.
 */
public final class HorsemanSightings {
	public static final int CHECK_TICKS = 20;
	public static final int SETTLE_TICKS = 600;
	public static final float START_CHANCE = 0.02F;
	public static final int MIN_GAP_DAYS = 2;
	public static final int MAX_GAP_DAYS = 3;
	public static final int ONE_AT_A_TIME = 128;
	public static final int WITNESS_RANGE = 48;
	public static final double START_DISTANCE = 26.0;
	public static final int CLIMB = 10;
	public static final String OPTION = "halloween.horseman_sightings";
	public static final String ADVANCEMENT = "sleepy_hollow";
	/** The record of a player who has never seen him. */
	public static final long NEVER = -1L;
	/** The bearings (degrees from where the player looks) he may come from, the most in sight first. */
	private static final int[] BEARINGS = {0, 35, -35, 70, -70};
	/** The day (epoch day, on the season's clock) each player last saw him. */
	public static AttachmentType<Long> SEEN;
	private static long lastClock = Long.MIN_VALUE;

	private HorsemanSightings() {
	}

	static void register() {
		SEEN = AttachmentRegistry.<Long>builder().persistent(Codec.LONG).copyOnDeath().buildAndRegister(Jugcraft.id("horseman_seen"));
		ServerTickEvents.END_SERVER_TICK.register(HorsemanSightings::tick);
	}

	private static void tick(MinecraftServer server) {
		if (server.getTickCount() % CHECK_TICKS != 0) {
			return;
		}
		ServerLevel level = server.overworld();
		long clock = level.getOverworldClockTime();
		boolean passing = clock != lastClock;
		lastClock = clock;
		if (!passing || !Wisps.night(clock) || !enabled()) {
			return;
		}
		LocalDate today = HalloweenSeason.date();
		long halloween = HalloweenSeason.harvestMoon().atYear(today.getYear()).toEpochDay();
		for (ServerPlayer player : List.copyOf(level.players())) {
			if (ready(level, player) && due(seen(player), today.toEpochDay(), halloween, player.getUUID())
					&& level.getRandom().nextFloat() < START_CHANCE && !riding(level, player.position())) {
				send(level, player);
			}
		}
	}

	/** Whether sightings are on: the {@code agriculture} switch and {@code halloween.horseman_sightings}. */
	public static boolean enabled() {
		return JugcraftConfig.isFeatureEnabled(JugcraftAgriculture.FEATURE) && !"off".equalsIgnoreCase(JugcraftConfig.textOption(OPTION));
	}

	/** Whether {@code player} may be sent him now: out under the open sky in the overworld, settled in and awake. */
	public static boolean ready(ServerLevel level, ServerPlayer player) {
		return player.level() == level && player.isAlive() && !player.isSpectator() && !player.isSleeping()
				&& player.tickCount >= SETTLE_TICKS && level.canSeeSky(player.blockPosition());
	}

	/** The day {@code player} last saw him (epoch day), or {@link #NEVER}. */
	public static long seen(ServerPlayer player) {
		return player.getAttachedOrElse(SEEN, NEVER);
	}

	/**
	 * Whether a player who last saw him on {@code last} (epoch day, or {@link #NEVER}) is due a sighting {@code today}:
	 * never yet (or the clock was set back), {@link #gap} days or more since, or Halloween ({@code halloween}) and not
	 * yet today.
	 */
	public static boolean due(long last, long today, long halloween, UUID player) {
		if (last == NEVER || last > today) {
			return true;
		}
		if (today == halloween && last != today) {
			return true;
		}
		return today - last >= gap(player, last);
	}

	/** The days a player waits after a sighting on {@code last}: from {@value #MIN_GAP_DAYS} to {@value #MAX_GAP_DAYS}, fixed for that sighting. */
	public static int gap(UUID player, long last) {
		return MIN_GAP_DAYS + Math.floorMod(Objects.hash(player, last), MAX_GAP_DAYS - MIN_GAP_DAYS + 1);
	}

	/** Whether a rider, or the summoned Horseman, is about within {@value #ONE_AT_A_TIME} blocks of {@code at}. */
	public static boolean riding(ServerLevel level, Vec3 at) {
		AABB near = new AABB(at, at).inflate(ONE_AT_A_TIME);
		return !level.getEntitiesOfClass(HorsemanRider.class, near, Entity::isAlive).isEmpty()
				|| !level.getEntitiesOfClass(HeadlessHorseman.class, near, Entity::isAlive).isEmpty();
	}

	/** Sends him riding at {@code player} from in front of them, if there is ground for him to come from; returns him, or null. */
	public static @Nullable HorsemanRider send(ServerLevel level, ServerPlayer player) {
		Vec3 from = start(level, player);
		HorsemanRider rider = from == null ? null : JugcraftAgriculture.HORSEMAN_RIDER.create(level, EntitySpawnReason.EVENT);
		if (rider == null) {
			return null;
		}
		rider.rideAt(player, from);
		level.addFreshEntity(rider);
		player.sendOverlayMessage(Component.translatable("message.jugcraft.horseman.hoofbeats"));
		level.playSound(null, from.x, from.y, from.z, SoundEvents.WITCH_CELEBRATE, SoundSource.HOSTILE, 3.0F, 0.5F);
		level.playSound(null, from.x, from.y, from.z, SoundEvents.SKELETON_HORSE_AMBIENT, SoundSource.HOSTILE, 3.0F, 0.7F);
		return rider;
	}

	/**
	 * Where he comes from: {@value #START_DISTANCE} blocks off, on one of the bearings in front of the player, on loaded
	 * ground that is not water and within {@value #CLIMB} blocks of their height; the first in their sight, else the first.
	 */
	public static @Nullable Vec3 start(ServerLevel level, ServerPlayer player) {
		Vec3 eye = player.getEyePosition();
		Vec3 first = null;
		for (int bearing : BEARINGS) {
			double yaw = Math.toRadians(player.getYRot() + bearing);
			double x = player.getX() - Math.sin(yaw) * START_DISTANCE;
			double z = player.getZ() + Math.cos(yaw) * START_DISTANCE;
			BlockPos column = BlockPos.containing(x, player.getY(), z);
			if (!level.isPositionEntityTicking(column)) {
				continue;
			}
			int ground = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, column.getX(), column.getZ());
			if (Math.abs(ground - player.getY()) > CLIMB || !level.getFluidState(new BlockPos(column.getX(), ground - 1, column.getZ())).isEmpty()) {
				continue;
			}
			Vec3 spot = new Vec3(x, ground, z);
			if (level.clip(new ClipContext(eye, spot.add(0.0, 2.0, 0.0), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player)).getType()
					== HitResult.Type.MISS) {
				return spot;
			}
			if (first == null) {
				first = spot;
			}
		}
		return first;
	}

	/** Everyone within {@value #WITNESS_RANGE} blocks of {@code at} has seen him today, and earns Sleepy Hollow. */
	public static void witnessed(ServerLevel level, Vec3 at) {
		long today = HalloweenSeason.date().toEpochDay();
		for (ServerPlayer player : level.getPlayers(player -> !player.isSpectator() && player.distanceToSqr(at) < WITNESS_RANGE * WITNESS_RANGE)) {
			player.setAttached(SEEN, today);
			TrickOrTreat.award(player, ADVANCEMENT);
		}
	}
}

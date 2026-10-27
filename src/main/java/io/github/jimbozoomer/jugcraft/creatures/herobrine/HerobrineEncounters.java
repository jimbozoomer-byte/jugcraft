package io.github.jimbozoomer.jugcraft.creatures.herobrine;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.config.JugcraftConfig;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Herobrine's encounters (docs/features/herobrine.md): the entity type, the live tick, placing him and reporting what
 * happened. Once a second, with {@code herobrine.haunting} on, the installed {@link HerobrineDirector} (the schedule) is
 * told every player's state and names the encounters to attempt; each is placed or fails, and the director hears who
 * really saw him and how it ended. This class keeps no schedule of its own. He only ever appears in loaded chunks, on
 * ground that is already there, and changes nothing in the world: no block, no item, no health.
 */
public final class HerobrineEncounters {
	public static final String ID = "herobrine";
	/** A player who has just joined or respawned is left alone this long, in ticks. */
	public static final int GRACE_TICKS = 1200;
	/** A player who has neither moved nor looked round for this long is idle, in ticks. */
	public static final int IDLE_TICKS = 1800;
	/** A player hurt by a mob this recently is not calm, in ticks. */
	public static final int RECENT_HURT_TICKS = 200;
	/** A player below this much health is not calm. */
	public static final float LOW_HEALTH = 6.0F;
	/** How far behind the player a close encounter stands, in blocks. */
	public static final double CLOSE_DISTANCE = 1.6;
	/** How long a close encounter's Darkness lasts, in ticks. */
	public static final int CLOSE_DARKNESS_TICKS = 60;
	/** How far away a distant encounter stands from a player under the open sky, in blocks. */
	public static final int OUTDOOR_NEAR = 28;
	public static final int OUTDOOR_FAR = 52;
	/** How far away a distant encounter stands from a player underground, in blocks. */
	public static final int UNDERGROUND_NEAR = 14;
	public static final int UNDERGROUND_FAR = 28;
	/** How far above or below the player a distant encounter may stand, in blocks. */
	public static final int MAX_RISE = 24;
	/** How many places are tried for one distant encounter; the best is used. */
	public static final int SPOT_ATTEMPTS = 16;
	/** How far behind the player a follower starts (and stays), in blocks. */
	public static final int FOLLOW_NEAR = 18;
	public static final int FOLLOW_FAR = 22;
	/** The shortest straight tunnel he waits at the end of, and how far down one is looked, in blocks. */
	public static final int TUNNEL_MIN = 14;
	public static final int TUNNEL_MAX = 48;
	/** The chance an underground player's distant encounter is tried down a tunnel first. */
	public static final double TUNNEL_CHANCE = 0.6;
	/** The chances a distant encounter is a follower or a second look (otherwise a watcher). */
	public static final double FOLLOWER_CHANCE = 0.25;
	public static final double SECOND_LOOK_CHANCE = 0.25;
	/** Other players within this many blocks may share a sighting, and the place is chosen for them too. */
	public static final int AUDIENCE_RANGE = 48;
	/** How many other players a place is judged for. */
	public static final int MAX_AUDIENCE = 4;

	public static EntityType<Herobrine> HEROBRINE;

	/** What {@link #attempt} did: the Herobrine placed, or why none was. */
	public record Attempt(@Nullable Herobrine herobrine, HerobrineDirector.@Nullable Failure failure) {
		public boolean placed() {
			return herobrine != null;
		}
	}

	private record Live(HerobrineDirector.Request request, Herobrine herobrine) {
	}

	private record Pose(double x, double y, double z, float yRot, float xRot, long movedAt) {
	}

	private static HerobrineDirector director = HerobrineDirector.NONE;
	private static final Map<UUID, Live> LIVE = new HashMap<>();
	private static final Map<UUID, UUID> BY_PLAYER = new HashMap<>();
	private static final Map<UUID, Pose> POSES = new HashMap<>();
	/** Players who have seen him at a distance since the server started: only they may be given a close encounter. */
	private static final Set<UUID> INTRODUCED = new HashSet<>();

	private HerobrineEncounters() {
	}

	public static void register() {
		ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, Jugcraft.id(ID));
		HEROBRINE = Registry.register(BuiltInRegistries.ENTITY_TYPE, key, EntityType.Builder.<Herobrine>of(Herobrine::new, MobCategory.MISC)
				.sized(0.6F, 1.95F).eyeHeight(1.62F).fireImmune().noLootTable().noSave().clientTrackingRange(10).build(key));
		FabricDefaultAttributeRegistry.register(HEROBRINE, Herobrine.attributes());
		ServerTickEvents.END_SERVER_TICK.register(HerobrineEncounters::tick);
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> reset());
		CommandRegistrationCallback.EVENT.register((dispatcher, access, environment) -> commands(dispatcher));
	}

	/** Installs the schedule ({@link HerobrineDirector#NONE} to remove it). */
	public static void setDirector(HerobrineDirector schedule) {
		director = schedule;
	}

	public static HerobrineDirector director() {
		return director;
	}

	/** Whether the haunting runs ({@code herobrine.haunting}, on unless set to off). Operators' commands work either way. */
	public static boolean enabled() {
		return !"off".equalsIgnoreCase(JugcraftConfig.textOption("herobrine.haunting").trim());
	}

	private static void tick(MinecraftServer server) {
		long now = server.getTickCount();
		if (now % 20 != 0) {
			return;
		}
		List<HerobrineDirector.Candidate> candidates = new ArrayList<>();
		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			boolean active = active(player, now);
			candidates.add(new HerobrineDirector.Candidate(player, active, active && calm(player)));
		}
		POSES.keySet().retainAll(candidates.stream().map(c -> c.player().getUUID()).toList());
		if (!enabled()) {
			return;
		}
		for (HerobrineDirector.Request request : director.due(server, List.copyOf(candidates))) {
			ServerPlayer player = server.getPlayerList().getPlayer(request.player());
			if (player == null) {
				director.failed(request, HerobrineDirector.Failure.TARGET_GONE);
			} else {
				attempt(player, request);
			}
		}
	}

	/**
	 * Whether {@code player} is actively playing: in the Overworld, alive, not spectating or asleep, a minute past joining
	 * or respawning ({@value #GRACE_TICKS} ticks; game-test mock players never tick, so never qualify), and not idle.
	 * Call once a second with the server's tick count: it also notes whether they have moved or looked round.
	 */
	public static boolean active(ServerPlayer player, long now) {
		Pose last = POSES.get(player.getUUID());
		boolean moved = last == null || Math.abs(last.x() - player.getX()) + Math.abs(last.y() - player.getY())
				+ Math.abs(last.z() - player.getZ()) > 0.05 || Math.abs(Mth.wrapDegrees(last.yRot() - player.getYRot())) > 1.0F
				|| Math.abs(last.xRot() - player.getXRot()) > 1.0F;
		Pose pose = new Pose(player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot(), moved ? now : last.movedAt());
		POSES.put(player.getUUID(), pose);
		return player.tickCount > GRACE_TICKS && player.isAlive() && !player.isSpectator() && !player.isSleeping()
				&& player.level().dimension() == Level.OVERWORLD && now - pose.movedAt() <= IDLE_TICKS;
	}

	/**
	 * Whether {@code player} is safe for a close encounter: no menu open, not dying or badly hurt, not hurt by a mob in
	 * the last {@value #RECENT_HURT_TICKS} ticks, and not burning, in lava, falling or short of air.
	 */
	public static boolean calm(ServerPlayer player) {
		return player.containerMenu == player.inventoryMenu && !player.isDeadOrDying() && player.getHealth() >= LOW_HEALTH
				&& (player.getLastHurtByMob() == null || player.tickCount - player.getLastHurtByMobTimestamp() > RECENT_HURT_TICKS)
				&& !player.isInLava() && !player.isOnFire() && player.fallDistance <= 3 && player.getAirSupply() >= 150;
	}

	/**
	 * Whether {@code player} can count as having seen him: calm, awake, and not idle (moved or looked round within
	 * {@value #IDLE_TICKS} ticks of {@code now}, the server's tick count). A camera that merely faces him is not enough.
	 */
	public static boolean attentive(ServerPlayer player, long now) {
		Pose pose = POSES.get(player.getUUID());
		return calm(player) && !player.isSleeping() && (pose == null || now - pose.movedAt() <= IDLE_TICKS);
	}

	/**
	 * Tries {@code request} for {@code player}, tells the director whether he was placed, and returns what happened. A
	 * distant encounter picks its own {@link Herobrine.Variant}: down a dark tunnel when the player is underground and
	 * one runs behind them, otherwise a follower, a second look or a watcher, falling back to a watcher. Unless an
	 * operator forced it, a close encounter needs a calm player who has already seen him at a distance since the server
	 * started, so nobody's first sight of him is a jump-scare.
	 */
	public static Attempt attempt(ServerPlayer player, HerobrineDirector.Request request) {
		return attempt(player, request, () -> request.kind() == Herobrine.Kind.CLOSE ? behind(player) : distant(player));
	}

	/** Tries {@code request} as one particular variant only (an operator's choice, and tests). */
	public static Attempt attemptVariant(ServerPlayer player, HerobrineDirector.Request request, Herobrine.Variant variant) {
		return attempt(player, request, () -> variant.kind != request.kind() ? null
				: variant == Herobrine.Variant.BEHIND ? behind(player) : distant(player, variant));
	}

	/**
	 * Places him exactly at {@code at} for {@code request}, with the same checks, bookkeeping and reports as
	 * {@link #attempt}: for encounters whose place is chosen elsewhere (and for tests). Fails with
	 * {@link HerobrineDirector.Failure#NO_VISIBLE_SPOT} if there is no room to stand there.
	 */
	public static Attempt attemptAt(ServerPlayer player, HerobrineDirector.Request request, Vec3 at, Herobrine.Variant variant) {
		return attempt(player, request, () -> variant.kind == request.kind() && standable(player.level(), BlockPos.containing(at))
				? spawn(player.level(), at, variant, player) : null);
	}

	/** {@link #attemptAt} as a watcher (distant) or behind (close). */
	public static Attempt attemptAt(ServerPlayer player, HerobrineDirector.Request request, Vec3 at) {
		return attemptAt(player, request, at, request.kind() == Herobrine.Kind.CLOSE ? Herobrine.Variant.BEHIND : Herobrine.Variant.WATCHER);
	}

	private static Attempt attempt(ServerPlayer player, HerobrineDirector.Request request, Supplier<@Nullable Herobrine> placement) {
		HerobrineDirector.Failure failure = null;
		Herobrine herobrine = null;
		if (!player.isAlive() || player.isSpectator() || player.level().dimension() != Level.OVERWORLD) {
			failure = HerobrineDirector.Failure.TARGET_GONE;
		} else if (BY_PLAYER.containsKey(player.getUUID())) {
			failure = HerobrineDirector.Failure.ALREADY_HAUNTED;
		} else if (request.kind() == Herobrine.Kind.CLOSE && request.reservation() != HerobrineDirector.FORCED
				&& (!calm(player) || !INTRODUCED.contains(player.getUUID()))) {
			failure = HerobrineDirector.Failure.TARGET_UNSAFE;
		} else {
			herobrine = placement.get();
			if (herobrine == null) {
				failure = HerobrineDirector.Failure.NO_VISIBLE_SPOT;
			}
		}
		if (herobrine == null) {
			director.failed(request, failure);
			return new Attempt(null, failure);
		}
		LIVE.put(herobrine.encounter(), new Live(request, herobrine));
		BY_PLAYER.put(player.getUUID(), herobrine.encounter());
		director.placed(request, herobrine.encounter());
		return new Attempt(herobrine, null);
	}

	/** Called by {@link Herobrine} the first time {@code player} really sees him. */
	static void seen(Herobrine herobrine, UUID player, Herobrine.Kind kind) {
		if (kind == Herobrine.Kind.DISTANT) {
			INTRODUCED.add(player);
		}
		Live live = LIVE.get(herobrine.encounter());
		if (live != null) {
			director.seen(live.request(), herobrine.encounter(), player);
		}
	}

	/** Called by {@link Herobrine} as he is removed, however that happens. */
	static void ended(Herobrine herobrine, Herobrine.Outcome outcome) {
		Live live = LIVE.remove(herobrine.encounter());
		if (live == null) {
			return;
		}
		BY_PLAYER.remove(live.request().player(), herobrine.encounter());
		director.ended(live.request(), herobrine.encounter(), outcome, herobrine.witnesses());
	}

	/** Encounters under way. */
	public static int live() {
		return LIVE.size();
	}

	/** Whether {@code player} is under the open sky (otherwise underground, indoors or under trees). */
	private static boolean outdoors(ServerPlayer player) {
		return player.level().canSeeSky(player.blockPosition().above());
	}

	private static @Nullable Herobrine distant(ServerPlayer player) {
		RandomSource random = player.getRandom();
		List<Herobrine.Variant> tries = new ArrayList<>();
		if (!outdoors(player) && random.nextDouble() < TUNNEL_CHANCE) {
			tries.add(Herobrine.Variant.TUNNEL);
		}
		double roll = random.nextDouble();
		// With other players about, he shows himself to the group rather than following one of them.
		if (roll < FOLLOWER_CHANCE && audience(player).isEmpty()) {
			tries.add(Herobrine.Variant.FOLLOWER);
		} else if (roll < FOLLOWER_CHANCE + SECOND_LOOK_CHANCE) {
			tries.add(Herobrine.Variant.SECOND_LOOK);
		}
		tries.add(Herobrine.Variant.WATCHER);
		for (Herobrine.Variant variant : tries) {
			Herobrine herobrine = distant(player, variant);
			if (herobrine != null) {
				return herobrine;
			}
		}
		return null;
	}

	/** Places a distant encounter of {@code variant}, or returns null if it has no place here. */
	private static @Nullable Herobrine distant(ServerPlayer player, Herobrine.Variant variant) {
		ServerLevel level = player.level();
		if (!level.hasChunkAt(player.blockPosition())) {
			return null;
		}
		boolean outdoors = outdoors(player);
		int near = outdoors ? OUTDOOR_NEAR : UNDERGROUND_NEAR;
		int far = outdoors ? OUTDOOR_FAR : UNDERGROUND_FAR;
		Vec3 spot = switch (variant) {
			case TUNNEL -> tunnel(level, player);
			case FOLLOWER -> spot(level, player, FOLLOW_NEAR, FOLLOW_FAR, outdoors, 150, 180, List.of());
			case WATCHER, SECOND_LOOK -> spot(level, player, near, far, outdoors, 20, 75, audience(player));
			case BEHIND -> null;
		};
		return spot == null ? null : spawn(level, spot, variant, player);
	}

	/** Other attentive players near {@code player}, who might see him too (at most {@value #MAX_AUDIENCE}). */
	private static List<ServerPlayer> audience(ServerPlayer player) {
		ServerLevel level = player.level();
		long now = level.getServer().getTickCount();
		return level.getEntitiesOfClass(ServerPlayer.class, player.getBoundingBox().inflate(AUDIENCE_RANGE),
				p -> p != player && !p.isSpectator() && attentive(p, now)).stream().limit(MAX_AUDIENCE).toList();
	}

	/**
	 * A place {@code near} to {@code far} blocks from {@code player}, 20° to 75° off where they face: on the surface, or
	 * (underground) within three blocks of their height. It must be loaded, have room to stand, no player within
	 * {@value Herobrine#VANISH_DISTANCE} blocks, and a clear line from the player's eyes to his. Null if none of
	 * {@value #SPOT_ATTEMPTS} tries finds one.
	 */
	public static @Nullable Vec3 spot(ServerLevel level, ServerPlayer player, int near, int far, boolean surface) {
		return spot(level, player, near, far, surface, 20, 75, List.of());
	}

	/**
	 * The best of {@value #SPOT_ATTEMPTS} tries at a place {@code near} to {@code far} blocks from {@code player},
	 * {@code fromDegrees} to {@code toDegrees} off where they face, either side; each must be loaded, have room to stand,
	 * no player within {@value Herobrine#VANISH_DISTANCE} blocks and a clear line from the player's eyes. Better is higher
	 * than the player (a ridge), open to the sky (a clearing), and in clear view of more of {@code others}.
	 */
	public static @Nullable Vec3 spot(ServerLevel level, ServerPlayer player, int near, int far, boolean surface, int fromDegrees,
			int toDegrees, List<ServerPlayer> others) {
		RandomSource random = player.getRandom();
		Vec3 eye = player.getEyePosition();
		Vec3 best = null;
		int bestScore = -1;
		for (int attempt = 0; attempt < SPOT_ATTEMPTS; attempt++) {
			double offset = fromDegrees + random.nextDouble() * (toDegrees - fromDegrees);
			double angle = Math.toRadians(player.getYRot() + (random.nextBoolean() ? 1 : -1) * offset);
			double distance = near + random.nextDouble() * (far - near);
			int x = Mth.floor(player.getX() - Math.sin(angle) * distance);
			int z = Mth.floor(player.getZ() + Math.cos(angle) * distance);
			BlockPos feet = surface ? surfaceFeet(level, x, z) : caveFeet(level, x, player.getBlockY(), z);
			if (feet == null || Math.abs(feet.getY() - player.getBlockY()) > MAX_RISE) {
				continue;
			}
			Vec3 at = Vec3.atBottomCenterOf(feet);
			if (Herobrine.someoneClose(level, at) || !inClearView(level, eye, at, player)) {
				continue;
			}
			int score = (feet.getY() >= player.getBlockY() + 3 ? 2 : 0) + (surface && level.canSeeSky(feet.above()) ? 1 : 0);
			for (ServerPlayer other : others) {
				score += inClearView(level, other.getEyePosition(), at, other) ? 3 : 0;
			}
			if (score > bestScore) {
				best = at;
				bestScore = score;
			}
		}
		return best;
	}

	private static boolean inClearView(ServerLevel level, Vec3 eye, Vec3 feet, ServerPlayer viewer) {
		return level.clip(new ClipContext(eye, feet.add(0.0, 1.62, 0.0), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, viewer))
				.getType() == HitResult.Type.MISS;
	}

	/**
	 * The far end of the longest straight, covered, walkable run from {@code player} in a direction they are not facing,
	 * at least {@value #TUNNEL_MIN} blocks long (at most {@value #TUNNEL_MAX} are walked), with no player near it and a
	 * clear line down it. Null if there is none: a tunnel's eyes only appear in a tunnel.
	 */
	public static @Nullable Vec3 tunnel(ServerLevel level, ServerPlayer player) {
		BlockPos start = player.blockPosition();
		Vec3 best = null;
		int bestLength = TUNNEL_MIN - 1;
		for (Direction direction : Direction.Plane.HORIZONTAL) {
			if (direction == player.getDirection()) {
				continue;
			}
			int length = 0;
			for (int step = 1; step <= TUNNEL_MAX; step++) {
				BlockPos pos = start.relative(direction, step);
				if (!standable(level, pos) || level.canSeeSky(pos)) {
					break;
				}
				length = step;
			}
			if (length <= bestLength) {
				continue;
			}
			Vec3 at = Vec3.atBottomCenterOf(start.relative(direction, length));
			if (!Herobrine.someoneClose(level, at) && inClearView(level, player.getEyePosition(), at, player)) {
				best = at;
				bestLength = length;
			}
		}
		return best;
	}

	/** Where a second look moves him: another place the player could see, at least 6 blocks from {@code from}. */
	static @Nullable Vec3 relocation(ServerLevel level, ServerPlayer player, Vec3 from) {
		boolean outdoors = outdoors(player);
		for (int tries = 0; tries < 2; tries++) {
			Vec3 spot = spot(level, player, outdoors ? OUTDOOR_NEAR : UNDERGROUND_NEAR, outdoors ? OUTDOOR_FAR : UNDERGROUND_FAR,
					outdoors, 20, 75, List.of());
			if (spot != null && spot.distanceToSqr(from) >= 36.0) {
				return spot;
			}
		}
		return null;
	}

	private static @Nullable BlockPos surfaceFeet(ServerLevel level, int x, int z) {
		if (!level.hasChunkAt(new BlockPos(x, 0, z))) {
			return null;
		}
		BlockPos feet = new BlockPos(x, level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z), z);
		return standable(level, feet) ? feet : null;
	}

	private static @Nullable BlockPos caveFeet(ServerLevel level, int x, int y, int z) {
		for (int dy = 3; dy >= -3; dy--) {
			BlockPos feet = new BlockPos(x, y + dy, z);
			if (standable(level, feet)) {
				return feet;
			}
		}
		return null;
	}

	/** Room to stand at {@code feet}: two clear, dry blocks over a solid top, all in loaded chunks. */
	public static boolean standable(ServerLevel level, BlockPos feet) {
		if (!level.hasChunkAt(feet)) {
			return false;
		}
		BlockPos below = feet.below();
		return clear(level, feet) && clear(level, feet.above()) && level.getBlockState(below).isFaceSturdy(level, below, Direction.UP);
	}

	private static boolean clear(ServerLevel level, BlockPos pos) {
		BlockState state = level.getBlockState(pos);
		return (state.isAir() || state.canBeReplaced()) && state.getCollisionShape(level, pos).isEmpty() && state.getFluidState().isEmpty();
	}

	/**
	 * Stands Herobrine right behind {@code player} ({@value #CLOSE_DISTANCE} blocks), with a moment of Darkness and a
	 * heartbeat only they hear. Null if there is no room behind them. It never hurts: Darkness only dims the screen.
	 */
	private static @Nullable Herobrine behind(ServerPlayer player) {
		ServerLevel level = player.level();
		double yaw = Math.toRadians(player.getYRot());
		BlockPos feet = BlockPos.containing(player.getX() + Math.sin(yaw) * CLOSE_DISTANCE, player.getY(),
				player.getZ() - Math.cos(yaw) * CLOSE_DISTANCE);
		if (!standable(level, feet)) {
			return null;
		}
		Herobrine herobrine = spawn(level, Vec3.atBottomCenterOf(feet), Herobrine.Variant.BEHIND, player);
		if (herobrine == null) {
			return null;
		}
		player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, CLOSE_DARKNESS_TICKS, 0));
		long seed = level.getRandom().nextLong();
		player.connection.send(new ClientboundSoundPacket(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.WARDEN_HEARTBEAT),
				SoundSource.HOSTILE, herobrine.getX(), herobrine.getEyeY(), herobrine.getZ(), 1.0F, 0.7F, seed));
		player.connection.send(new ClientboundSoundPacket(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.SOUL_ESCAPE.value()),
				SoundSource.HOSTILE, herobrine.getX(), herobrine.getEyeY(), herobrine.getZ(), 1.0F, 0.5F, seed));
		return herobrine;
	}

	private static @Nullable Herobrine spawn(ServerLevel level, Vec3 at, Herobrine.Variant variant, ServerPlayer player) {
		Herobrine herobrine = HEROBRINE.create(level, EntitySpawnReason.EVENT);
		if (herobrine == null) {
			return null;
		}
		herobrine.snapTo(at.x, at.y, at.z, 0.0F, 0.0F);
		herobrine.haunt(variant, player, UUID.randomUUID());
		return level.addFreshEntity(herobrine) ? herobrine : null;
	}

	/** Forgets every encounter and player state (the server stopped). The installed director stays. */
	public static void reset() {
		LIVE.clear();
		BY_PLAYER.clear();
		POSES.clear();
		INTRODUCED.clear();
	}

	private static void commands(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("jugcraft").then(Commands.literal("herobrine")
				.requires(source -> Commands.LEVEL_GAMEMASTERS.check(source.permissions()))
				.then(Commands.literal("status").executes(context -> status(context.getSource())))
				.then(Commands.literal("sighting").then(Commands.argument("players", EntityArgument.players())
						.executes(context -> force(context.getSource(), EntityArgument.getPlayers(context, "players"), null))
						.then(Commands.argument("variant", StringArgumentType.word())
								.suggests((context, builder) -> SharedSuggestionProvider.suggest(Arrays.stream(Herobrine.Variant.values())
										.filter(v -> v.kind == Herobrine.Kind.DISTANT).map(v -> v.name().toLowerCase(Locale.ROOT)), builder))
								.executes(context -> force(context.getSource(), EntityArgument.getPlayers(context, "players"),
										StringArgumentType.getString(context, "variant"))))))
				.then(Commands.literal("scare").then(Commands.argument("players", EntityArgument.players())
						.executes(context -> force(context.getSource(), EntityArgument.getPlayers(context, "players"), "behind"))))));
	}

	private static int status(CommandSourceStack source) {
		if (!enabled()) {
			source.sendSuccess(() -> Component.translatable("commands.jugcraft.herobrine.status.off"), false);
		} else {
			int players = source.getServer().getPlayerList().getPlayers().size();
			source.sendSuccess(() -> Component.translatable("commands.jugcraft.herobrine.status.on", live(), players), false);
		}
		if (director == HerobrineDirector.NONE) {
			source.sendSuccess(() -> Component.translatable("commands.jugcraft.herobrine.status.no_schedule"), false);
		}
		return live();
	}

	/** Forces an encounter for each of {@code players}: a distant one of any variant ({@code null}), or the named one. */
	private static int force(CommandSourceStack source, Collection<ServerPlayer> players, @Nullable String name) {
		Herobrine.Variant variant = null;
		if (name != null) {
			try {
				variant = Herobrine.Variant.valueOf(name.toUpperCase(Locale.ROOT));
			} catch (IllegalArgumentException e) {
				source.sendFailure(Component.translatable("commands.jugcraft.herobrine.unknown_variant", name));
				return 0;
			}
		}
		Herobrine.Kind kind = variant == null ? Herobrine.Kind.DISTANT : variant.kind;
		int placed = 0;
		for (ServerPlayer player : players) {
			HerobrineDirector.Request request = new HerobrineDirector.Request(player.getUUID(), kind, HerobrineDirector.FORCED);
			Attempt attempt = variant == null ? attempt(player, request) : attemptVariant(player, request, variant);
			if (attempt.placed()) {
				placed++;
				source.sendSuccess(() -> Component.translatable("commands.jugcraft.herobrine." + (kind == Herobrine.Kind.CLOSE ? "scare" : "sighting"),
						player.getDisplayName()), false);
			} else {
				source.sendFailure(Component.translatable("commands.jugcraft.herobrine.failed."
						+ attempt.failure().name().toLowerCase(Locale.ROOT), player.getDisplayName()));
			}
		}
		return placed;
	}
}

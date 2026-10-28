package io.github.jimbozoomer.jugcraft.agriculture;

import com.mojang.serialization.Codec;
import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.config.JugcraftConfig;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.BossEvent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Graveyard Rising (docs/features/graveyard-rising.md), every night of the Halloween season: a wave defence at a
 * graveyard, raised by ringing a bell beside it.
 *
 * <p>In the Overworld, at night, not in peaceful, using a bell with at least {@value #MIN_GRAVES} graves (block tag
 * {@code #jugcraft:graves}, a headstone of several blocks counted once) within {@value #RADIUS} blocks across and
 * {@value #HEIGHT} up or down stirs them; using it again {@value #CONFIRM_MIN_TICKS} to {@value #CONFIRM_TICKS} ticks
 * later wakes the dead. A player looks for graves at most once every {@value #RING_COOLDOWN} ticks. After
 * {@value #FIRST_WAVE_TICKS} ticks the dead climb out of the graves in {@link #WAVES}, {@value #WAVE_GAP_TICKS} ticks
 * apart, with {@value #EXTRA_PER_PLAYER} more of {@link #EXTRA} for each player taking part after the first (at most
 * {@value #MAX_EXTRA} a wave). Taking part: alive, not spectating, within {@value #RANGE} blocks of the bell.
 *
 * <p>The rising fails, and the dead sink back, at dawn, with nobody taking part for {@value #ABANDON_TICKS} ticks, when
 * the graves have room for fewer than half a wave, or when switched off. One of the risen straying more than
 * {@value #LEASH} blocks from the bell sinks back and another climbs out in its place. Clear the last wave and each
 * player taking part who has not been rewarded tonight ({@link #RESTED}, the night they last were) gets the gift loot
 * table {@value #TABLE} and the advancement Graveyard Shift. At most {@value #MAX_RISINGS} risings at once, none within
 * {@value #APART} blocks of another, and each bell once a night. Everything is decided on the server; risings are not
 * saved, and any of the risen left in the world by a restart sink back when they load.
 *
 * <p>The server's {@code halloween.graveyard_rising} ({@code on} or {@code off}) and the {@code agriculture} switch turn
 * it off; the season's own dates and {@code halloween.mode} bound it as every Halloween event is bound.
 */
public final class GraveyardRising {
	public static final int MIN_GRAVES = 6;
	public static final int RADIUS = 16;
	public static final int HEIGHT = 6;
	public static final int CONFIRM_MIN_TICKS = 20;
	public static final int CONFIRM_TICKS = 200;
	public static final int RING_COOLDOWN = 20;
	public static final int FIRST_WAVE_TICKS = 60;
	public static final int WAVE_GAP_TICKS = 100;
	public static final int RANGE = 48;
	public static final int ABANDON_TICKS = 600;
	public static final int LEASH = 40;
	public static final int CHECK_TICKS = 20;
	public static final int EXTRA_PER_PLAYER = 1;
	public static final int MAX_EXTRA = 4;
	public static final int MAX_RISINGS = 4;
	public static final int APART = 64;
	public static final String OPTION = "halloween.graveyard_rising";
	public static final String TAG = "jugcraft.risen";
	public static final String ADVANCEMENT = "graveyard_shift";
	public static final String TABLE = "gameplay/graveyard_rising";
	public static final String EXTRA = "minecraft:zombie";
	public static final List<String> ARCHERS = List.of("minecraft:skeleton", "minecraft:stray");
	public static final List<List<Rise>> WAVES = List.of(
			List.of(new Rise("minecraft:zombie", 4)),
			List.of(new Rise("minecraft:zombie", 3), new Rise("minecraft:skeleton", 3)),
			List.of(new Rise("minecraft:zombie", 3), new Rise("minecraft:skeleton", 2), new Rise("minecraft:husk", 2), new Rise("minecraft:stray", 1)));
	public static final TagKey<Block> GRAVES = TagKey.create(Registries.BLOCK, Jugcraft.id("graves"));
	public static final ResourceKey<LootTable> LOOT = ResourceKey.create(Registries.LOOT_TABLE, Jugcraft.id(TABLE));
	/** The record of a player never rewarded. */
	public static final long NEVER = -1L;
	/** The night (overworld day number) each player was last rewarded by a rising. */
	public static AttachmentType<Long> RESTED;

	/** What ringing a bell did. The first six say nothing to the player: the bell is just a bell. */
	public enum Result {
		WAIT, DISABLED, OUT_OF_SEASON, WRONG_PLACE, NO_GRAVEYARD, SPECTATOR,
		TOO_FEW_GRAVES, DAYTIME, PEACEFUL, ALREADY_RISEN, NEARBY, TOO_MANY, STIRRED, RISEN
	}

	public enum State {
		RISING, WON, FAILED
	}

	/** {@code count} of the entity {@code entity} climbing out in a wave. */
	public record Rise(String entity, int count) {
	}

	private record Stir(ResourceKey<Level> dimension, BlockPos bell, long at) {
	}

	private static final List<Rising> RISINGS = new ArrayList<>();
	/** Every one of the risen still walking, so any other with the tag (left by a restart) sinks back when it loads. */
	private static final Set<UUID> RISEN = new HashSet<>();
	/** The bells whose graves have risen, with the night: "dimension pos night". */
	private static final Set<String> RISEN_TONIGHT = new HashSet<>();
	private static final Map<UUID, Stir> STIRRED = new HashMap<>();
	private static final Map<UUID, Long> LOOKED = new HashMap<>();

	private GraveyardRising() {
	}

	static void register() {
		RESTED = AttachmentRegistry.<Long>builder().persistent(Codec.LONG).copyOnDeath().buildAndRegister(Jugcraft.id("graveyard_rested"));
		ServerTickEvents.END_SERVER_TICK.register(GraveyardRising::tick);
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> forget());
		ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> {
			if (entity.entityTags().contains(TAG) && !RISEN.contains(entity.getUUID())) {
				entity.discard();
			}
		});
		UseBlockCallback.EVENT.register((player, level, hand, hit) -> {
			if (hand == InteractionHand.MAIN_HAND && player instanceof ServerPlayer rung && level.getBlockState(hit.getBlockPos()).is(Blocks.BELL)) {
				ring(rung, hit.getBlockPos());
			}
			return InteractionResult.PASS; // The bell rings as ever.
		});
	}

	private static void forget() {
		RISINGS.clear();
		RISEN.clear();
		RISEN_TONIGHT.clear();
		STIRRED.clear();
		LOOKED.clear();
	}

	private static void tick(MinecraftServer server) {
		if (RISINGS.isEmpty() || server.getTickCount() % CHECK_TICKS != 0) {
			return;
		}
		long dayTime = server.overworld().getOverworldClockTime();
		for (Rising rising : List.copyOf(RISINGS)) {
			ServerLevel level = server.getLevel(rising.dimension);
			if (level == null || rising.step(level, level.getGameTime(), dayTime) != State.RISING) {
				RISINGS.remove(rising);
			}
		}
	}

	/** Whether risings are on: the {@code agriculture} switch and {@code halloween.graveyard_rising}. */
	public static boolean enabled() {
		return JugcraftConfig.isFeatureEnabled(JugcraftAgriculture.FEATURE) && !"off".equalsIgnoreCase(JugcraftConfig.textOption(OPTION));
	}

	/** Whether the dead may be about at {@code dayTime} on the overworld clock: dusk to dawn. */
	public static boolean night(long dayTime) {
		long hour = Math.floorMod(dayTime, TrickOrTreat.DAY);
		return hour >= HarvestMoon.DUSK && hour < HarvestMoon.DAWN;
	}

	/** The night {@code dayTime} falls in: the overworld's day number. */
	public static long tonight(long dayTime) {
		return Math.floorDiv(dayTime, TrickOrTreat.DAY);
	}

	/** The night {@code player} was last rewarded by a rising, or {@link #NEVER}. */
	public static long rested(ServerPlayer player) {
		return player.getAttachedOrElse(RESTED, NEVER);
	}

	/** The risings under way. */
	public static List<Rising> risings() {
		return List.copyOf(RISINGS);
	}

	/** {@code player} rings the bell at {@code bell}. */
	public static Result ring(ServerPlayer player, BlockPos bell) {
		return ring(player, bell, player.level().getOverworldClockTime());
	}

	/** {@code player} rings the bell at {@code bell} as if the overworld clock read {@code dayTime}; tells them what it did. */
	public static Result ring(ServerPlayer player, BlockPos bell, long dayTime) {
		ServerLevel level = player.level();
		long now = level.getGameTime();
		if (!enabled()) {
			return Result.DISABLED;
		}
		if (!HalloweenSeason.active()) {
			return Result.OUT_OF_SEASON;
		}
		if (level.dimension() != Level.OVERWORLD) {
			return Result.WRONG_PLACE;
		}
		if (player.isSpectator()) {
			return Result.SPECTATOR;
		}
		Long looked = LOOKED.get(player.getUUID());
		if (looked != null && now >= looked && now - looked < RING_COOLDOWN) {
			return Result.WAIT;
		}
		LOOKED.put(player.getUUID(), now);
		List<BlockPos> graves = graves(level, bell);
		if (graves.isEmpty()) {
			return Result.NO_GRAVEYARD;
		}
		Result result = graves.size() < MIN_GRAVES ? Result.TOO_FEW_GRAVES
				: !night(dayTime) ? Result.DAYTIME
				: level.getDifficulty() == Difficulty.PEACEFUL ? Result.PEACEFUL
				: RISEN_TONIGHT.contains(key(level, bell, tonight(dayTime))) ? Result.ALREADY_RISEN
				: RISINGS.stream().anyMatch(rising -> rising.state == State.RISING && rising.dimension == level.dimension()
						&& rising.bell.closerThan(bell, APART)) ? Result.NEARBY
				: RISINGS.stream().filter(rising -> rising.state == State.RISING).count() >= MAX_RISINGS ? Result.TOO_MANY
				: null;
		if (result != null) {
			player.sendOverlayMessage(Component.translatable("message.jugcraft.graveyard_rising." + result.name().toLowerCase(),
					graves.size(), MIN_GRAVES));
			return result;
		}
		Stir stir = STIRRED.remove(player.getUUID());
		if (stir == null || stir.dimension != level.dimension() || !stir.bell.equals(bell) || now - stir.at < CONFIRM_MIN_TICKS
				|| now - stir.at > CONFIRM_TICKS) {
			STIRRED.put(player.getUUID(), new Stir(level.dimension(), bell.immutable(), now));
			player.sendOverlayMessage(Component.translatable("message.jugcraft.graveyard_rising.stirred"));
			level.playSound(null, bell, SoundEvents.ROOTED_DIRT_BREAK, SoundSource.BLOCKS, 1.0F, 0.5F);
			return Result.STIRRED;
		}
		Rising rising = begin(level, bell, graves, now, dayTime);
		RISINGS.add(rising);
		RISEN_TONIGHT.add(key(level, bell, rising.night));
		for (ServerPlayer near : rising.party(level)) {
			near.sendSystemMessage(Component.translatable("message.jugcraft.graveyard_rising.risen"));
		}
		level.playSound(null, bell, SoundEvents.BELL_RESONATE, SoundSource.BLOCKS, 2.0F, 0.6F);
		return Result.RISEN;
	}

	private static String key(ServerLevel level, BlockPos bell, long night) {
		return level.dimension().identifier() + " " + bell.asLong() + " " + night;
	}

	/**
	 * The graves within reach of {@code bell}: every block in {@code #jugcraft:graves} within {@value #RADIUS} blocks
	 * across and {@value #HEIGHT} up or down, in loaded chunks, a headstone counted by its first part only. Reads at
	 * most 33 x 13 x 33 blocks.
	 */
	public static List<BlockPos> graves(ServerLevel level, BlockPos bell) {
		List<BlockPos> found = new ArrayList<>();
		for (BlockPos pos : BlockPos.betweenClosed(bell.offset(-RADIUS, -HEIGHT, -RADIUS), bell.offset(RADIUS, HEIGHT, RADIUS))) {
			if (!level.isLoaded(pos)) {
				continue;
			}
			BlockState state = level.getBlockState(pos);
			if (state.is(GRAVES) && (!state.hasProperty(HeadstoneBlock.PART) || state.getValue(HeadstoneBlock.PART) == 0)) {
				found.add(pos.immutable());
			}
		}
		return found;
	}

	/** How many climb out in wave {@code wave} (from 0) with {@code players} taking part. */
	public static int count(int wave, int players) {
		int total = 0;
		for (Rise rise : WAVES.get(wave)) {
			total += rise.count();
		}
		return total + extra(players);
	}

	private static int extra(int players) {
		return Math.min(MAX_EXTRA, EXTRA_PER_PLAYER * Math.max(0, players - 1));
	}

	/**
	 * A rising at the graveyard round {@code bell}, its graves {@code graves}, begun at game time {@code now} on the night
	 * of {@code dayTime}; not under way until the server ticks it (risings rung up are), or a caller calls {@link Rising#step}.
	 */
	public static Rising begin(ServerLevel level, BlockPos bell, List<BlockPos> graves, long now, long dayTime) {
		return new Rising(level.dimension(), bell.immutable(), List.copyOf(graves), tonight(dayTime), now);
	}

	/** One graveyard's rising. */
	public static final class Rising {
		private final ResourceKey<Level> dimension;
		private final BlockPos bell;
		private final List<BlockPos> graves;
		private final long night;
		private final List<UUID> risen = new ArrayList<>();
		private final ServerBossEvent bar = new ServerBossEvent(UUID.randomUUID(),
				Component.translatable("event.jugcraft.graveyard_rising.wave", 1, WAVES.size()), BossEvent.BossBarColor.PURPLE,
				BossEvent.BossBarOverlay.PROGRESS);
		private State state = State.RISING;
		private int wave;
		private boolean raised;
		private int raisedCount;
		private long nextWaveAt;
		private long lastSeen;

		private Rising(ResourceKey<Level> dimension, BlockPos bell, List<BlockPos> graves, long night, long now) {
			this.dimension = dimension;
			this.bell = bell;
			this.graves = graves;
			this.night = night;
			this.nextWaveAt = now + FIRST_WAVE_TICKS;
			this.lastSeen = now;
		}

		public BlockPos bell() {
			return bell;
		}

		public State state() {
			return state;
		}

		/** The wave climbing out or next to (from 0). */
		public int wave() {
			return wave;
		}

		/** The risen still walking. */
		public List<UUID> risen() {
			return List.copyOf(risen);
		}

		/** The players taking part: alive, not spectating, within {@value #RANGE} blocks of the bell. */
		public List<ServerPlayer> party(ServerLevel level) {
			Vec3 centre = Vec3.atCenterOf(bell);
			return level.players().stream().filter(player -> player.isAlive() && !player.isSpectator()
					&& player.position().closerThan(centre, RANGE)).toList();
		}

		/** Moves the rising on to game time {@code now}, with the overworld clock reading {@code dayTime}; returns its state. */
		public State step(ServerLevel level, long now, long dayTime) {
			if (state != State.RISING) {
				return state;
			}
			List<ServerPlayer> party = party(level);
			if (!enabled()) {
				return fail(level, party, "failed");
			}
			if (!night(dayTime)) {
				return fail(level, party, "dawn");
			}
			for (ServerPlayer shown : List.copyOf(bar.getPlayers())) {
				if (!party.contains(shown)) {
					bar.removePlayer(shown);
				}
			}
			party.forEach(bar::addPlayer);
			if (!party.isEmpty()) {
				lastSeen = now;
			} else if (now - lastSeen > ABANDON_TICKS) {
				return fail(level, party, "failed");
			}
			Vec3 centre = Vec3.atCenterOf(bell);
			for (UUID id : List.copyOf(risen)) {
				Entity mob = level.getEntity(id);
				if (mob == null || !mob.isAlive()) {
					forget(id);
				} else if (!mob.position().closerThan(centre, LEASH)) {
					// Too far from its grave: it sinks back, and another climbs out in its place.
					String type = BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType()).toString();
					sink(level, mob);
					forget(id);
					raise(level, type);
				}
			}
			if (!raised) {
				if (now >= nextWaveAt) {
					int wanted = count(wave, party.size());
					for (Rise rise : WAVES.get(wave)) {
						for (int i = 0; i < rise.count(); i++) {
							raise(level, rise.entity());
						}
					}
					for (int i = 0; i < extra(party.size()); i++) {
						raise(level, EXTRA);
					}
					if (risen.size() * 2 < wanted) {
						return fail(level, party, "blocked");
					}
					raised = true;
					raisedCount = risen.size();
					for (ServerPlayer player : party) {
						player.sendOverlayMessage(Component.translatable("message.jugcraft.graveyard_rising.wave", wave + 1, WAVES.size()));
					}
				}
			} else if (risen.isEmpty()) {
				if (wave + 1 >= WAVES.size()) {
					return win(level, party);
				}
				wave++;
				raised = false;
				nextWaveAt = now + WAVE_GAP_TICKS;
			}
			bar.setName(Component.translatable("event.jugcraft.graveyard_rising.wave", wave + 1, WAVES.size()));
			bar.setProgress(raised && raisedCount > 0 ? Math.min(1.0F, risen.size() / (float) raisedCount) : 1.0F);
			return state;
		}

		/** Ends the rising at once, as a failure: the dead sink back. */
		public State stop(ServerLevel level) {
			return state == State.RISING ? fail(level, party(level), "failed") : state;
		}

		private void forget(UUID id) {
			risen.remove(id);
			RISEN.remove(id);
		}

		/** One of {@code id} climbs out beside a random grave with room; returns it, or null if none has room. */
		private @Nullable Mob raise(ServerLevel level, String id) {
			BlockPos at = null;
			for (int tries = 0; tries < 4 && at == null && !graves.isEmpty(); tries++) {
				at = room(level, graves.get(level.getRandom().nextInt(graves.size())));
			}
			EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.parse(id));
			if (at == null || !(type.create(level, EntitySpawnReason.EVENT) instanceof Mob mob)) {
				return null;
			}
			mob.snapTo(at.getX() + 0.5, at.getY(), at.getZ() + 0.5, level.getRandom().nextFloat() * 360.0F, 0.0F);
			if (ARCHERS.contains(id)) {
				mob.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
			}
			mob.setPersistenceRequired();
			mob.addTag(TAG);
			RISEN.add(mob.getUUID());
			risen.add(mob.getUUID());
			level.addFreshEntity(mob);
			level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.ROOTED_DIRT.defaultBlockState()),
					mob.getX(), mob.getY() + 0.2, mob.getZ(), 24, 0.35, 0.1, 0.35, 0.1);
			level.sendParticles(ParticleTypes.SOUL, mob.getX(), mob.getY() + 0.5, mob.getZ(), 4, 0.3, 0.4, 0.3, 0.02);
			level.playSound(null, at, SoundEvents.ROOTED_DIRT_BREAK, SoundSource.HOSTILE, 1.0F, 0.6F);
			return mob;
		}

		/** Where one of the risen can stand beside {@code grave} (in front, behind, to either side, or on top), or null. */
		private static @Nullable BlockPos room(ServerLevel level, BlockPos grave) {
			for (BlockPos beside : List.of(grave.north(), grave.south(), grave.east(), grave.west(), grave.above())) {
				for (BlockPos at : List.of(beside, beside.below(), beside.above())) {
					if (standable(level, at)) {
						return at;
					}
				}
			}
			return null;
		}

		private static boolean standable(ServerLevel level, BlockPos at) {
			return level.isLoaded(at) && level.getFluidState(at).isEmpty()
					&& level.getBlockState(at).getCollisionShape(level, at).isEmpty()
					&& level.getBlockState(at.above()).getCollisionShape(level, at.above()).isEmpty()
					&& !level.getBlockState(at.below()).getCollisionShape(level, at.below()).isEmpty();
		}

		private static void sink(ServerLevel level, Entity mob) {
			level.sendParticles(ParticleTypes.LARGE_SMOKE, mob.getX(), mob.getY() + 0.8, mob.getZ(), 8, 0.3, 0.5, 0.3, 0.02);
			level.sendParticles(ParticleTypes.SOUL, mob.getX(), mob.getY() + 0.8, mob.getZ(), 3, 0.3, 0.5, 0.3, 0.02);
			mob.discard();
		}

		private State win(ServerLevel level, List<ServerPlayer> party) {
			state = State.WON;
			LootTable table = level.getServer().reloadableRegistries().getLootTable(LOOT);
			for (ServerPlayer player : party) {
				if (rested(player) == night) {
					player.sendSystemMessage(Component.translatable("message.jugcraft.graveyard_rising.rested_already"));
					continue;
				}
				player.setAttached(RESTED, night);
				LootParams params = new LootParams.Builder(level).withParameter(LootContextParams.ORIGIN, player.position())
						.withParameter(LootContextParams.THIS_ENTITY, player).create(LootContextParamSets.GIFT);
				for (ItemStack stack : table.getRandomItems(params)) {
					if (!player.getInventory().add(stack)) {
						Block.popResource(level, player.blockPosition(), stack);
					}
				}
				TrickOrTreat.award(player, ADVANCEMENT);
				player.sendSystemMessage(Component.translatable("message.jugcraft.graveyard_rising.won"));
			}
			level.playSound(null, bell, SoundEvents.BELL_RESONATE, SoundSource.BLOCKS, 2.0F, 1.2F);
			end();
			return state;
		}

		private State fail(ServerLevel level, List<ServerPlayer> party, String why) {
			state = State.FAILED;
			for (UUID id : List.copyOf(risen)) {
				Entity mob = level.getEntity(id);
				if (mob != null) {
					sink(level, mob);
				}
				forget(id);
			}
			for (ServerPlayer player : party) {
				player.sendSystemMessage(Component.translatable("message.jugcraft.graveyard_rising." + why));
			}
			end();
			return state;
		}

		private void end() {
			for (UUID id : risen) {
				RISEN.remove(id);
			}
			risen.clear();
			bar.removeAllPlayers();
		}
	}
}

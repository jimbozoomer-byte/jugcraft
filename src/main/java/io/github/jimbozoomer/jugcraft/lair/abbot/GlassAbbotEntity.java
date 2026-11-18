package io.github.jimbozoomer.jugcraft.lair.abbot;

import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.LoopType;
import com.geckolib.util.GeckoLibUtil;
import io.github.jimbozoomer.jugcraft.concordance.ConcordanceProgress;
import io.github.jimbozoomer.jugcraft.concordance.JugcraftConcordance;
import io.github.jimbozoomer.jugcraft.concordance.rules.ResearchState;
import io.github.jimbozoomer.jugcraft.concordance.strata.Brace;
import io.github.jimbozoomer.jugcraft.config.JugcraftConfig;
import io.github.jimbozoomer.jugcraft.lair.ChapelMirrorBlock;
import io.github.jimbozoomer.jugcraft.lair.Encounter;
import io.github.jimbozoomer.jugcraft.lair.GlassChapel;
import io.github.jimbozoomer.jugcraft.lair.GlassChapel.Mirror;
import io.github.jimbozoomer.jugcraft.lair.JugcraftLairs;
import io.github.jimbozoomer.jugcraft.lair.Lair;
import io.github.jimbozoomer.jugcraft.lair.LairBosses;
import io.github.jimbozoomer.jugcraft.lair.LairExitBlock;
import io.github.jimbozoomer.jugcraft.lair.LairInstance;
import io.github.jimbozoomer.jugcraft.lair.LairRewards;
import io.github.jimbozoomer.jugcraft.lair.Lairs;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * The Glass Abbot (docs/features/glass-abbot.md): the Glass Chapel's boss, a tall faceless figure in a monk's habit of grey
 * wool under a mantle of leaded glass, a round rose window glowing where his face should be. He kneels at his lectern in
 * the apse, and wakes when someone who may fight him steps into the nave or strikes him: he rises as the rose window
 * floods with light, unhurt, his health scaled for the party. He keeps to the nave.
 *
 * <p>His mantle: while it is whole, blows and shots do {@value #MANTLE} of their damage. His rule, the Beam: every
 * {@link #beamEvery()} ticks he marks two of the chapel's three mirrors (their frames burn gold, the bar names them);
 * {@value #MARK_TICKS} ticks later he returns to his place and gathers the sun for {@value #GATHER_TICKS}; then his beam
 * leaves his mantle aimed at one of the two and goes where the mirrors send it, worked out by {@link GlassChapel#trace}
 * from their angles alone. Sent home off both, his mantle cracks: for {@value #CRACK_TICKS} ticks he kneels dazed and
 * every blow does {@value #CRACKED_MULTIPLIER} of its damage. Lost, it burns along its path ({@value #BEAM_DAMAGE}) and the
 * mantle holds. Whoever turned a marked mirror into place for a beam sent home has done a deed of his fight.
 *
 * <p>Vespers: the Shard Fan and the Prism Line. At {@value #VEIL_AT} of his health the Veil: he folds his hands, unhurt,
 * and his Glass Images stand up; from then he veils one of his marked mirrors for {@value #VEILED_TICKS} ticks, turns a
 * mirror himself every {@value #TURN_EVERY} ticks, and calls images again. Below {@value #BREAKING_AT}, the Breaking: his
 * beam comes oftener and his images are three. Only he casts the rose window's patterned shadow.
 *
 * <p>Every attack has a wind-up you can read, a strike and a recovery ({@link Attack}); the numbers are here, in one place,
 * and in tools/glass_abbot.py. Falls and drowning never harm him. Each participant ({@link Encounter}: who hurt him or his
 * images, healed or warded another player in his fight, or turned a mirror for his light to come home) is paid when he
 * falls ({@link AbbotLoot}, through {@link LairRewards}). Left alone he returns to his lectern and kneels, healed: his
 * images fade, the mirrors turn back to rest, and who took part is forgotten. Bound to a lair instance he vanishes with
 * it; one with no instance (a test's) keeps to the chapel laid out from the origin it was given. The only blocks he
 * changes are the mirrors' marks, veils and turns, and the Grey Mist he opens in the apse when he falls.
 */
public class GlassAbbotEntity extends Monster implements GeoEntity {
	// ---------------------------------------------------------------- numbers (tools/glass_abbot.py)
	public static final float HEALTH = 340.0F;
	public static final double ARMOR = 6.0;
	public static final double LEASH = 26.0;
	public static final double PARTY_STEP = 0.5;
	public static final double PARTY_MAX = 2.5;
	public static final int WAKE_TICKS = 50;
	public static final int ABANDON_TICKS = 200;
	public static final int GLOBAL_COOLDOWN = 20;
	public static final double SPEED = 0.12;
	public static final double KEEP_DISTANCE = 6.0;
	public static final double STEP = 1.0;
	public static final float VEIL_AT = 0.5F;
	public static final float BREAKING_AT = 0.2F;
	public static final int VEIL_TICKS = 40;
	public static final float MANTLE = 0.25F;
	public static final float CRACKED_MULTIPLIER = 1.25F;
	public static final int CRACK_TICKS = 160;
	public static final int BEAM_FIRST = 160;
	public static final int BEAM_EVERY = 400;
	public static final int BEAM_EVERY_VEIL = 360;
	public static final int BEAM_EVERY_BREAKING = 300;
	public static final int BEAM_REST = 40;
	public static final int MARK_TICKS = 200;
	public static final int GATHER_TICKS = 60;
	public static final int BEAM_SHOW = 20;
	public static final float BEAM_DAMAGE = 6.0F;
	public static final double BEAM_WIDTH = 1.0;
	public static final int VEILED_TICKS = 80;
	public static final int TURN_EVERY = 240;
	public static final int TURN_TELL = 30;
	public static final int TURN_GRACE = 100;
	public static final int SHARDS = 3;
	public static final double SHARD_SPREAD = 15.0;
	public static final double SHARD_SPEED = 0.9;
	public static final int SHARD_LIFE = 30;
	public static final float SHARD_DAMAGE = 6.0F;
	public static final double SHARD_PUSH = 0.6;
	public static final double SHARD_HIT = 0.6;
	public static final double PRISM_LENGTH = 18.0;
	public static final double PRISM_WIDTH = 0.75;
	public static final float PRISM_DAMAGE = 8.0F;
	public static final int IMAGES_VEIL = 2;
	public static final int IMAGES_BREAKING = 3;
	public static final double IMAGE_APART = 4.0;
	public static final int IMAGE_TICKS = 200;
	public static final int PREVIEW_EVERY = 10;
	public static final int EXPERIENCE = 300;
	/** The research whose understanding shows the beam's path ahead (Radiance's First Light). */
	public static final String FIRST_LIGHT = "jugcraft:first_light";
	/** How high over the floor his beam, his shards and his line run. */
	private static final double LIGHT_HEIGHT = 1.4;
	private static final DustParticleOptions GOLD = new DustParticleOptions(0xFFD86A, 1.4F);
	private static final DustParticleOptions ROSE = new DustParticleOptions(0xE87A98, 1.0F);
	private static final DustParticleOptions SHADE = new DustParticleOptions(0x1C1E24, 1.6F);
	private static final DustParticleOptions SHARD = new DustParticleOptions(0xD8EEF8, 1.0F);

	/**
	 * His attacks: wind-up, the active part and recovery in ticks, the cooldown, the target's nearest and farthest
	 * distance, and the phases he uses it in (Vespers; the Veil and the Breaking). The wind-up is what players read; the
	 * strike lands as it ends. The Beam is his rule, on its own clock.
	 */
	public enum Attack {
		SHARD_FAN(16, 1, 10, 100, 0.0, 20.0, true, true),
		PRISM_LINE(20, 4, 12, 140, 0.0, 18.0, true, true),
		FALSE_IMAGES(30, 1, 10, 400, 0.0, 30.0, false, true);

		public final int windup;
		public final int active;
		public final int recovery;
		public final int cooldown;
		public final double near;
		public final double far;
		public final boolean vespers;
		public final boolean veil;

		Attack(int windup, int active, int recovery, int cooldown, double near, double far, boolean vespers, boolean veil) {
			this.windup = windup;
			this.active = active;
			this.recovery = recovery;
			this.cooldown = cooldown;
			this.near = near;
			this.far = far;
			this.vespers = vespers;
			this.veil = veil;
		}

		public int length() {
			return windup + active + recovery;
		}
	}

	/** The fight's phases: kneeling, waking, Vespers, the Veil falling, the Veil, the Breaking. */
	public enum Phase {
		WAITING, WAKING, VESPERS, VEILING, VEIL, BREAKING
	}

	/** Where his beam stands: none under way, two mirrors marked, gathering the sun, or his mantle cracked. */
	public enum Beam {
		NONE, MARKED, GATHER, CRACKED
	}

	/** What his body is doing, synced for its animations. */
	public enum Action {
		PRAY, WAKE, IDLE, SHARD_WINDUP, SHARD, PRISM_WINDUP, PRISM, IMAGES_WINDUP, IMAGES, GATHER, DAZED, VEIL, TURN
	}

	/** A shard in flight: where it is, its motion a tick, and the ticks it has left. */
	public record Shard(Vec3 at, Vec3 motion, int life) {
	}

	private static final EntityDataAccessor<Integer> ACTION = SynchedEntityData.defineId(GlassAbbotEntity.class, EntityDataSerializers.INT);
	static final RawAnimation[] BODY = new RawAnimation[Action.values().length];
	static final RawAnimation WALK = RawAnimation.begin().thenLoop("animation.glass_abbot.walk");
	static final RawAnimation DEATH = RawAnimation.begin().thenPlayAndHold("animation.glass_abbot.death");

	static {
		String prefix = "animation.glass_abbot.";
		BODY[Action.PRAY.ordinal()] = RawAnimation.begin().thenLoop(prefix + "pray");
		BODY[Action.WAKE.ordinal()] = RawAnimation.begin().thenPlayAndHold(prefix + "wake");
		BODY[Action.IDLE.ordinal()] = RawAnimation.begin().thenLoop(prefix + "idle");
		BODY[Action.SHARD_WINDUP.ordinal()] = RawAnimation.begin().thenPlayAndHold(prefix + "shard_windup");
		BODY[Action.SHARD.ordinal()] = RawAnimation.begin().then(prefix + "shard", LoopType.PLAY_ONCE).thenLoop(prefix + "idle");
		BODY[Action.PRISM_WINDUP.ordinal()] = RawAnimation.begin().thenPlayAndHold(prefix + "prism_windup");
		BODY[Action.PRISM.ordinal()] = RawAnimation.begin().then(prefix + "prism", LoopType.PLAY_ONCE).thenLoop(prefix + "idle");
		BODY[Action.IMAGES_WINDUP.ordinal()] = RawAnimation.begin().thenPlayAndHold(prefix + "images_windup");
		BODY[Action.IMAGES.ordinal()] = RawAnimation.begin().then(prefix + "images", LoopType.PLAY_ONCE).thenLoop(prefix + "idle");
		BODY[Action.GATHER.ordinal()] = RawAnimation.begin().thenPlayAndHold(prefix + "gather");
		BODY[Action.DAZED.ordinal()] = RawAnimation.begin().thenLoop(prefix + "dazed");
		BODY[Action.VEIL.ordinal()] = RawAnimation.begin().thenPlayAndHold(prefix + "veil");
		BODY[Action.TURN.ordinal()] = RawAnimation.begin().thenPlayAndHold(prefix + "turn");
	}

	private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
	private final ServerBossEvent bossBar = new ServerBossEvent(UUID.randomUUID(), Component.translatable("entity.jugcraft.glass_abbot"),
			BossEvent.BossBarColor.YELLOW, BossEvent.BossBarOverlay.NOTCHED_10);
	private final Encounter encounter = new Encounter(this, this::centre);
	/** The lair instance he belongs to (null for one put down by a test or a command), and the chapel's origin. */
	private @Nullable UUID instance;
	private int slot = -1;
	private BlockPos origin = BlockPos.ZERO;
	private Phase phase = Phase.WAITING;
	private int phaseTicks;
	private @Nullable Attack attack;
	private int attackTicks;
	private int globalCooldown;
	private final int[] cooldowns = new int[Attack.values().length];
	private @Nullable UUID foe;
	private int abandoned;
	private final List<UUID> images = new ArrayList<>();
	// His rule: the beam under way.
	private Beam beam = Beam.NONE;
	private int beamTicks;
	private int beamTimer = BEAM_FIRST;
	private int beams;
	private @Nullable Mirror markedA;
	private @Nullable Mirror markedB;
	private @Nullable Mirror first;
	private @Nullable Mirror veiled;
	private final Map<Mirror, UUID> turners = new EnumMap<>(Mirror.class);
	private List<Vec3> beamPath = List.of();
	private int beamShow;
	// His own turns of the mirrors, from the Veil.
	private int turnTimer = TURN_EVERY;
	private @Nullable Mirror turning;
	private int turnTell;
	// The attacks under way.
	private final List<Shard> shards = new ArrayList<>();
	private Vec3 prismFrom = Vec3.ZERO;
	private Vec3 prismTo = Vec3.ZERO;
	/** Set when he is loaded mid-fight: he stands at his place and fights on, his mirrors back to rest. */
	private boolean landOnLoad;

	public GlassAbbotEntity(EntityType<? extends GlassAbbotEntity> type, Level level) {
		super(type, level);
		xpReward = 0;
		setPersistenceRequired();
		bossBar.setDarkenScreen(true);
		bossBar.setVisible(false);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, HEALTH).add(Attributes.ARMOR, ARMOR)
				.add(Attributes.KNOCKBACK_RESISTANCE, 0.9).add(Attributes.FOLLOW_RANGE, 48.0).add(Attributes.MOVEMENT_SPEED, 0.3)
				.add(Attributes.ATTACK_DAMAGE, SHARD_DAMAGE).add(Attributes.STEP_HEIGHT, STEP);
	}

	/**
	 * Kneels him at the lectern of {@code instance}'s chapel. Called as the instance is placed. Placing the template puts
	 * back only its own blocks, so the Grey Mist a fallen Abbot opened in the apse before still stands: it goes first.
	 */
	public static GlassAbbotEntity summon(ServerLevel level, LairInstance instance) {
		BlockPos origin = instance.origin();
		for (BlockPos cell : fallExitCells()) {
			BlockPos at = origin.offset(cell);
			if (level.getBlockState(at).is(JugcraftLairs.LAIR_EXIT)) {
				level.setBlock(at, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
			}
		}
		return summon(level, origin, instance);
	}

	/** Kneels him at the lectern of the chapel laid out from {@code origin} (tests pass no instance). */
	public static GlassAbbotEntity summon(ServerLevel level, BlockPos origin, @Nullable LairInstance instance) {
		GlassAbbotEntity abbot = new GlassAbbotEntity(JugcraftAbbot.GLASS_ABBOT, level);
		abbot.origin = origin.immutable();
		if (instance != null) {
			abbot.instance = instance.id;
			abbot.slot = instance.slot;
		}
		Vec3 place = abbot.place();
		abbot.snapTo(place.x, place.y, place.z, 180.0F, 0.0F);
		abbot.face(180.0F);
		level.addFreshEntity(abbot);
		return abbot;
	}

	// ---------------------------------------------------------------- where things are

	/** The Grey Mist he leaves when he falls, in the apse behind his lectern (tools/glass_abbot.py FALL_EXIT). */
	public static final int FALL_EXIT_WEST = 16;
	public static final int FALL_EXIT_EAST = 18;
	public static final int FALL_EXIT_Z = 4;

	/** The cells of the Grey Mist he leaves when he falls, three high from the floor. */
	public static List<BlockPos> fallExitCells() {
		List<BlockPos> out = new ArrayList<>();
		for (int y = GlassChapel.STAND; y < GlassChapel.STAND + 3; y++) {
			for (int x = FALL_EXIT_WEST; x <= FALL_EXIT_EAST; x++) {
				out.add(new BlockPos(x, y, FALL_EXIT_Z));
			}
		}
		return out;
	}

	/** The chapel's middle, on its floor, for the chapel laid out from {@code origin}. */
	public static Vec3 centre(BlockPos origin) {
		return GlassChapel.centre().add(origin.getX(), origin.getY(), origin.getZ());
	}

	public Vec3 centre() {
		return centre(origin);
	}

	public BlockPos origin() {
		return origin;
	}

	/** His place: where he kneels, and where his beam leaves and comes home to. */
	public Vec3 place() {
		return GlassChapel.abbot().add(origin.getX(), origin.getY(), origin.getZ());
	}

	/** The chapel's floor, where he and his foes stand. */
	public double floorY() {
		return origin.getY() + GlassChapel.STAND;
	}

	/** Whether {@code at} (a world position) is in the nave or the apse. */
	public boolean inArena(Vec3 at) {
		return GlassChapel.inArena(at.x - origin.getX(), at.z - origin.getZ());
	}

	/** A point of the template's floor plane (x and z) in the world, at the height his light runs. */
	public Vec3 world(double x, double z) {
		return new Vec3(origin.getX() + x, floorY() + LIGHT_HEIGHT, origin.getZ() + z);
	}

	/** The lower block of {@code mirror}. */
	public BlockPos mirror(Mirror mirror) {
		return mirror.base(origin);
	}

	// ---------------------------------------------------------------- state

	public Phase phase() {
		return phase;
	}

	public Beam beam() {
		return beam;
	}

	public int beamTicks() {
		return beamTicks;
	}

	public int beamTimer() {
		return beamTimer;
	}

	public @Nullable Attack attack() {
		return attack;
	}

	public int attackTicks() {
		return attackTicks;
	}

	public List<Mirror> marked() {
		List<Mirror> out = new ArrayList<>();
		if (markedA != null) {
			out.add(markedA);
		}
		if (markedB != null) {
			out.add(markedB);
		}
		return out;
	}

	public @Nullable Mirror first() {
		return first;
	}

	public @Nullable Mirror veiled() {
		return veiled;
	}

	public @Nullable Mirror turning() {
		return turning;
	}

	public List<Vec3> beamPath() {
		return beamPath;
	}

	public List<Shard> shards() {
		return List.copyOf(shards);
	}

	public List<UUID> images() {
		return List.copyOf(images);
	}

	/** Whether the image {@code id} is one of his. */
	public boolean calls(UUID id) {
		return images.contains(id);
	}

	/** The foe he has now, while they are still near the chapel; else null. */
	public @Nullable Player foe(ServerLevel level) {
		Player held = foe == null ? null : level.getPlayerByUUID(foe);
		return held != null && LairBosses.eligible(held) && held.position().distanceTo(centre()) <= LEASH + 8.0 ? held : null;
	}

	public Encounter encounter() {
		return encounter;
	}

	public Action action() {
		int id = entityData.get(ACTION);
		return id >= 0 && id < Action.values().length ? Action.values()[id] : Action.IDLE;
	}

	private void setAction(Action action) {
		entityData.set(ACTION, action.ordinal());
	}

	public @Nullable UUID instanceId() {
		return instance;
	}

	/** Whether he is fighting (not kneeling, waking or veiling). */
	public boolean fighting() {
		return phase == Phase.VESPERS || phase == Phase.VEIL || phase == Phase.BREAKING;
	}

	/** Whether his mantle is cracked. */
	public boolean cracked() {
		return beam == Beam.CRACKED;
	}

	/** Damage he deals: the attack's own, times {@code lairs.boss_damage} ({@link LairBosses}). */
	public static float damage(float base) {
		return LairBosses.damage(base);
	}

	/** His health for a party of {@code players}: half as much again for each beyond the first, at most 2.5 times. */
	public static double partyScale(int players) {
		return LairBosses.partyScale(players, PARTY_STEP, PARTY_MAX);
	}

	/** How long from one marking of his mirrors to the next in this phase. */
	public int beamEvery() {
		return switch (phase) {
			case VEIL, VEILING -> BEAM_EVERY_VEIL;
			case BREAKING -> BEAM_EVERY_BREAKING;
			default -> BEAM_EVERY;
		};
	}

	/** How many images he calls in this phase. */
	public int imageCap() {
		return phase == Phase.BREAKING ? IMAGES_BREAKING : IMAGES_VEIL;
	}

	// ---------------------------------------------------------------- the server's tick

	@Override
	protected void customServerAiStep(ServerLevel level) {
		super.customServerAiStep(level);
		if (!bound(level)) {
			dismiss(level);
			discard();
			return;
		}
		if (landOnLoad) {
			landOnLoad = false;
			ChapelMirrorBlock.reset(level, origin);
			Vec3 place = place();
			teleportTo(place.x, place.y, place.z);
		}
		if (phase != Phase.WAITING && isAlive() && !encounter.underway()) {
			encounter.begin();  // a fight back from a save keeps a fresh record of who takes part
		}
		resetFallDistance();
		bossBar.setProgress(getHealth() / getMaxHealth());
		prune(level);
		for (int i = 0; i < cooldowns.length; i++) {
			if (cooldowns[i] > 0) {
				cooldowns[i]--;
			}
		}
		flyShards(level);
		showBeam(level);
		phaseTicks++;
		switch (phase) {
			case WAITING -> waiting(level);
			case WAKING -> waking(level);
			case VEILING -> veiling(level);
			case VESPERS, VEIL, BREAKING -> fight(level);
		}
	}

	private boolean bound(ServerLevel level) {
		return instance == null || lairInstance(level) != null;
	}

	private @Nullable LairInstance lairInstance(ServerLevel level) {
		Lair lair = Lair.of(level.dimension());
		LairInstance open = lair == null || instance == null ? null : Lairs.instance(lair, slot);
		return open != null && open.id.equals(instance) ? open : null;
	}

	private void waiting(ServerLevel level) {
		Vec3 place = place();
		if (position().distanceToSqr(place) > 0.01) {
			setPos(place.x, place.y, place.z);
		}
		hold();
		face(180.0F);
		setAction(Action.PRAY);
		if (tickCount % 10 == 0 && intruder(level) != null) {
			wake(level);
		}
	}

	/** Someone who may fight him, standing in the nave or the apse. */
	private @Nullable Player intruder(ServerLevel level) {
		for (Player player : level.players()) {
			if (LairBosses.eligible(player) && inArena(player.position()) && player.getY() > floorY() - 1.0 && player.getY() < floorY() + 6.0) {
				return player;
			}
		}
		return null;
	}

	/** He wakes: his health scaled for the players in the chapel, his bar shown, the rose window flooding with light. */
	public void wake(ServerLevel level) {
		if (phase != Phase.WAITING) {
			return;
		}
		double scale = partyScale(Math.max(1, nearby(level).size()));
		AttributeInstance health = getAttribute(Attributes.MAX_HEALTH);
		if (health != null) {
			health.setBaseValue(HEALTH * scale);
		}
		setHealth(getMaxHealth());
		phase = Phase.WAKING;
		phaseTicks = 0;
		setAction(Action.WAKE);
		bossBar.setVisible(true);
		encounter.begin();
		level.playSound(null, getX(), getY(), getZ(), SoundEvents.BEACON_ACTIVATE, SoundSource.HOSTILE, 3.0F, 0.6F);
		level.playSound(null, getX(), getY(), getZ(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.HOSTILE, 3.0F, 0.5F);
		Vec3 rose = world(GlassChapel.ABBOT_X, GlassChapel.ABBOT_Z - 6.0).add(0.0, 5.0, 0.0);
		level.sendParticles(ParticleTypes.END_ROD, rose.x, rose.y, rose.z, 60, 2.0, 2.0, 0.3, 0.05);
		tell(level, "message.jugcraft.glass_abbot.wakes");
	}

	private void waking(ServerLevel level) {
		hold();
		if (phaseTicks % 6 == 0) {
			level.sendParticles(ParticleTypes.END_ROD, getX(), getY() + 2.4, getZ(), 6, 0.4, 0.6, 0.4, 0.02);
		}
		if (phaseTicks >= WAKE_TICKS) {
			phase = Phase.VESPERS;
			phaseTicks = 0;
			globalCooldown = GLOBAL_COOLDOWN;
			beamTimer = BEAM_FIRST;
			face(0.0F);
			setAction(Action.IDLE);
		}
	}

	/** Players near enough to the chapel to be fighting (or about to). */
	public List<ServerPlayer> nearby(ServerLevel level) {
		List<ServerPlayer> out = new ArrayList<>();
		Vec3 centre = centre();
		for (ServerPlayer player : level.players()) {
			if (LairBosses.eligible(player) && player.position().distanceTo(centre) <= LEASH + 8.0) {
				out.add(player);
			}
		}
		return out;
	}

	// ---------------------------------------------------------------- fighting

	private void fight(ServerLevel level) {
		if (beam != Beam.GATHER && beam != Beam.CRACKED) {
			keepToTheNave(level);
		}
		if (phase == Phase.VESPERS && getHealth() <= getMaxHealth() * VEIL_AT && attack == null && beam != Beam.GATHER && beam != Beam.CRACKED) {
			veil(level);
			return;
		}
		if (phase == Phase.VEIL && getHealth() <= getMaxHealth() * BREAKING_AT) {
			breaking(level);
		}
		Player target = target(level);
		beamTick(level);
		if (phase == Phase.VEIL || phase == Phase.BREAKING) {
			turnTick(level);
			shadow(level);
		}
		if (beam == Beam.GATHER || beam == Beam.CRACKED) {
			hold();
			return;
		}
		if (target == null) {
			breakOff();
			setAction(Action.IDLE);
			move(centre(), SPEED);
			if (++abandoned >= ABANDON_TICKS) {
				reset(level);
			}
			return;
		}
		abandoned = 0;
		if (attack != null) {
			attackTicks++;
			perform(level, target);
			return;
		}
		face(target);
		if (globalCooldown > 0) {
			globalCooldown--;
			approach(target);
			return;
		}
		Attack next = choose(target);
		if (next == null) {
			approach(target);
		} else {
			begin(level, next, target);
		}
	}

	/** Strayed farther than his leash from the chapel's middle, or below its floor, he is set back at his place. */
	private void keepToTheNave(ServerLevel level) {
		Vec3 centre = centre();
		double dx = getX() - centre.x;
		double dz = getZ() - centre.z;
		if (dx * dx + dz * dz <= LEASH * LEASH && getY() > floorY() - 3.0) {
			return;
		}
		level.sendParticles(ParticleTypes.END_ROD, getX(), getY() + 1.2, getZ(), 20, 0.6, 1.0, 0.6, 0.05);
		breakOff();
		Vec3 back = place();
		teleportTo(back.x, back.y, back.z);
		level.sendParticles(ParticleTypes.END_ROD, getX(), getY() + 1.2, getZ(), 20, 0.6, 1.0, 0.6, 0.05);
	}

	/** His target: the one he has while they are still near the chapel, else the nearest who may fight him. */
	private @Nullable Player target(ServerLevel level) {
		Player held = foe(level);
		if (held != null) {
			return held;
		}
		Player nearest = null;
		double best = Double.MAX_VALUE;
		for (ServerPlayer player : nearby(level)) {
			double distance = player.distanceToSqr(this);
			if (distance < best) {
				best = distance;
				nearest = player;
			}
		}
		foe = nearest == null ? null : nearest.getUUID();
		return nearest;
	}

	/** Keeps about {@value #KEEP_DISTANCE} blocks from {@code target}: closer in when far, back when near. */
	private void approach(Player target) {
		Vec3 to = target.position().subtract(position());
		double flat = Math.sqrt(to.x * to.x + to.z * to.z);
		Vec3 goal = position();
		if (flat > KEEP_DISTANCE + 2.0) {
			goal = new Vec3(target.getX() - to.x / flat * KEEP_DISTANCE, getY(), target.getZ() - to.z / flat * KEEP_DISTANCE);
		} else if (flat < KEEP_DISTANCE - 2.0 && flat > 0.01) {
			goal = new Vec3(getX() - to.x / flat * 2.0, getY(), getZ() - to.z / flat * 2.0);
		}
		move(goal, SPEED);
	}

	private void move(Vec3 goal, double speed) {
		Vec3 to = new Vec3(goal.x - getX(), 0.0, goal.z - getZ());
		double length = to.length();
		Vec3 step = length < 0.05 ? Vec3.ZERO : to.scale(Math.min(speed, length) / length);
		setDeltaMovement(step.x, Math.min(getDeltaMovement().y, 0.0), step.z);
	}

	private void hold() {
		setDeltaMovement(0.0, Math.min(getDeltaMovement().y, 0.0), 0.0);
	}

	private void face(Entity target) {
		face(target.position().subtract(position()));
	}

	private void face(Vec3 way) {
		if (way.x * way.x + way.z * way.z > 1.0E-4) {
			face((float) (Math.toDegrees(Math.atan2(way.z, way.x)) - 90.0));
		}
	}

	private void face(float yaw) {
		setYRot(yaw);
		setYBodyRot(yaw);
		setYHeadRot(yaw);
	}

	/** The way he faces, flat. */
	public Vec3 facing() {
		double yaw = Math.toRadians(getYRot());
		return new Vec3(-Math.sin(yaw), 0.0, Math.cos(yaw));
	}

	/** An attack he may begin on {@code target} now, or null. */
	private @Nullable Attack choose(Player target) {
		double distance = distanceTo(target);
		List<Attack> ready = new ArrayList<>();
		for (Attack candidate : Attack.values()) {
			boolean allowed = phase == Phase.VESPERS ? candidate.vespers : candidate.veil;
			if (!allowed || cooldowns[candidate.ordinal()] > 0 || distance < candidate.near || distance > candidate.far) {
				continue;
			}
			if (candidate == Attack.FALSE_IMAGES && images.size() >= imageCap()) {
				continue;
			}
			ready.add(candidate);
		}
		return ready.isEmpty() ? null : ready.get(getRandom().nextInt(ready.size()));
	}

	/** Starts {@code next} on {@code target}: its wind-up begins. */
	public void begin(ServerLevel level, Attack next, Player target) {
		breakOff();
		attack = next;
		attackTicks = 0;
		foe = target.getUUID();
		cooldowns[next.ordinal()] = next.cooldown;
		hold();
		face(target);
		switch (next) {
			case SHARD_FAN -> {
				setAction(Action.SHARD_WINDUP);
				level.playSound(null, getX(), getY(), getZ(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.HOSTILE, 2.0F, 1.4F);
			}
			case PRISM_LINE -> {
				setAction(Action.PRISM_WINDUP);
				markPrism(target.position());
				level.playSound(null, getX(), getY(), getZ(), SoundEvents.BEACON_POWER_SELECT, SoundSource.HOSTILE, 2.0F, 1.6F);
			}
			case FALSE_IMAGES -> {
				setAction(Action.IMAGES_WINDUP);
				level.playSound(null, getX(), getY(), getZ(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.HOSTILE, 2.5F, 0.8F);
			}
		}
	}

	private void perform(ServerLevel level, Player target) {
		Attack current = attack;
		if (current == null) {
			return;
		}
		int t = attackTicks;
		hold();
		switch (current) {
			case SHARD_FAN -> {
				if (t < current.windup) {
					face(target);
					if (t % 4 == 0) {
						Vec3 hand = hand();
						level.sendParticles(ParticleTypes.END_ROD, hand.x, hand.y + 0.6, hand.z, 3, 0.2, 0.1, 0.2, 0.0);
					}
				} else if (t == current.windup) {
					setAction(Action.SHARD);
					shardFan(level, target.position().add(0.0, 1.0, 0.0));
				}
			}
			case PRISM_LINE -> {
				if (t < current.windup) {
					if (t % 3 == 0) {
						drawPrism(level, false);
					}
				} else if (t == current.windup) {
					setAction(Action.PRISM);
					lance(level);
				} else if (t < current.windup + current.active) {
					drawPrism(level, true);
				}
			}
			case FALSE_IMAGES -> {
				if (t == current.windup) {
					setAction(Action.IMAGES);
					callImages(level);
				}
			}
		}
		if (attack == current && attackTicks >= current.length()) {
			attack = null;
			attackTicks = 0;
			globalCooldown = GLOBAL_COOLDOWN;
			setAction(Action.IDLE);
		}
	}

	/** The attack under way is broken off before its end. */
	private void breakOff() {
		attack = null;
		attackTicks = 0;
	}

	/** Where his raised hand is, a little over his right shoulder. */
	private Vec3 hand() {
		Vec3 facing = facing();
		return position().add(-facing.z * 0.5, 2.4, facing.x * 0.5);
	}

	/** Sets {@code player} moving at {@code motion}, and tells their client (a player's own motion is theirs to move). */
	static void push(ServerPlayer player, Vec3 motion) {
		player.setDeltaMovement(motion);
		player.connection.send(new ClientboundSetEntityMotionPacket(player));
	}

	// ---------------------------------------------------------------- the Shard Fan

	/** Looses {@value #SHARDS} shards from his hand at {@code at}, {@value #SHARD_SPREAD} degrees apart. */
	public void shardFan(ServerLevel level, Vec3 at) {
		Vec3 from = hand();
		Vec3 way = at.subtract(from);
		if (way.lengthSqr() < 1.0E-6) {
			way = facing();
		}
		way = way.normalize();
		for (int i = 0; i < SHARDS; i++) {
			double turn = Math.toRadians((i - (SHARDS - 1) / 2.0) * SHARD_SPREAD);
			Vec3 motion = new Vec3(way.x * Math.cos(turn) - way.z * Math.sin(turn), way.y, way.x * Math.sin(turn) + way.z * Math.cos(turn))
					.normalize().scale(SHARD_SPEED);
			shards.add(new Shard(from, motion, SHARD_LIFE));
		}
		level.playSound(null, from.x, from.y, from.z, SoundEvents.GLASS_BREAK, SoundSource.HOSTILE, 1.5F, 1.4F);
	}

	/** Moves every shard on a tick: a block stops one; a player it passes within {@value #SHARD_HIT} of is struck. */
	public int flyShards(ServerLevel level) {
		int hit = 0;
		List<Shard> next = new ArrayList<>();
		for (Shard shard : shards) {
			Vec3 from = shard.at();
			Vec3 to = from.add(shard.motion());
			HitResult block = level.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
			if (block.getType() != HitResult.Type.MISS) {
				to = block.getLocation();
			}
			ServerPlayer struck = null;
			for (ServerPlayer player : nearby(level)) {
				if (player.getBoundingBox().inflate(SHARD_HIT).clip(from, to).isPresent()) {
					struck = player;
					break;
				}
			}
			level.sendParticles(SHARD, to.x, to.y, to.z, 1, 0.0, 0.0, 0.0, 0.0);
			if (struck != null) {
				struck.hurtServer(level, damageSources().mobAttack(this), damage(SHARD_DAMAGE));
				double share = Brace.spend(struck);
				Vec3 way = shard.motion().normalize();
				push(struck, struck.getDeltaMovement().add(way.x * SHARD_PUSH * share, 0.1 * share, way.z * SHARD_PUSH * share));
				level.sendParticles(ParticleTypes.CRIT, to.x, to.y, to.z, 6, 0.2, 0.2, 0.2, 0.1);
				hit++;
				continue;
			}
			if (block.getType() != HitResult.Type.MISS || shard.life() <= 1) {
				level.sendParticles(ParticleTypes.END_ROD, to.x, to.y, to.z, 3, 0.1, 0.1, 0.1, 0.02);
				continue;
			}
			next.add(new Shard(to, shard.motion(), shard.life() - 1));
		}
		shards.clear();
		shards.addAll(next);
		return hit;
	}

	// ---------------------------------------------------------------- the Prism Line

	/** Marks his line: from him toward {@code at}, {@value #PRISM_LENGTH} long, on the floor. */
	public void markPrism(Vec3 at) {
		Vec3 way = new Vec3(at.x - getX(), 0.0, at.z - getZ());
		if (way.lengthSqr() < 1.0E-6) {
			way = facing();
		}
		way = way.normalize();
		prismFrom = new Vec3(getX(), floorY(), getZ());
		prismTo = prismFrom.add(way.scale(PRISM_LENGTH));
	}

	public Vec3 prismFrom() {
		return prismFrom;
	}

	public Vec3 prismTo() {
		return prismTo;
	}

	private void drawPrism(ServerLevel level, boolean lit) {
		double length = prismFrom.distanceTo(prismTo);
		for (double d = 0.5; d <= length; d += 0.75) {
			Vec3 at = prismFrom.add(prismTo.subtract(prismFrom).scale(d / length));
			level.sendParticles(lit ? ParticleTypes.END_ROD : GOLD, at.x, at.y + (lit ? 1.0 : 0.1), at.z, 1, 0.05, lit ? 0.4 : 0.0, 0.05, 0.0);
		}
	}

	/** The lance down his line: {@value #PRISM_DAMAGE} to whoever stands within {@value #PRISM_WIDTH} of it. Returns how many. */
	public int lance(ServerLevel level) {
		int hit = 0;
		for (ServerPlayer player : nearby(level)) {
			Vec3 at = new Vec3(player.getX(), prismFrom.y, player.getZ());
			if (flatDistance(at, prismFrom, prismTo) <= PRISM_WIDTH && player.getY() > floorY() - 0.5 && player.getY() < floorY() + 2.5) {
				if (player.hurtServer(level, damageSources().mobAttack(this), damage(PRISM_DAMAGE))) {
					hit++;
				}
			}
		}
		level.playSound(null, getX(), getY(), getZ(), SoundEvents.BEACON_DEACTIVATE, SoundSource.HOSTILE, 2.0F, 1.8F);
		return hit;
	}

	/** How far {@code p} is from the segment from {@code a} to {@code b}, on the floor's plane. */
	public static double flatDistance(Vec3 p, Vec3 a, Vec3 b) {
		double dx = b.x - a.x;
		double dz = b.z - a.z;
		double length = dx * dx + dz * dz;
		double t = length < 1.0E-9 ? 0.0 : Math.max(0.0, Math.min(1.0, ((p.x - a.x) * dx + (p.z - a.z) * dz) / length));
		return Math.hypot(p.x - a.x - t * dx, p.z - a.z - t * dz);
	}

	// ---------------------------------------------------------------- his images

	/** Calls his Glass Images round him, up to his cap, and steps among them, so his foes lose him. Returns how many came. */
	public int callImages(ServerLevel level) {
		int called = 0;
		List<Vec3> spots = new ArrayList<>();
		spots.add(position());
		while (images.size() < imageCap()) {
			double angle = getRandom().nextDouble() * Math.PI * 2.0;
			Vec3 at = settle(level, position().add(Math.cos(angle) * IMAGE_APART, 0.0, Math.sin(angle) * IMAGE_APART));
			GlassImageEntity image = GlassImageEntity.spawn(level, this, at);
			images.add(image.getUUID());
			spots.add(at);
			called++;
		}
		// He steps into one of the places, and an image into his.
		if (called > 0) {
			int pick = getRandom().nextInt(spots.size());
			Vec3 mine = spots.get(pick);
			if (pick > 0 && level.getEntity(images.get(pick - 1)) instanceof GlassImageEntity swapped) {
				swapped.teleportTo(getX(), getY(), getZ());
			}
			teleportTo(mine.x, mine.y, mine.z);
		}
		level.sendParticles(ParticleTypes.END_ROD, getX(), getY() + 1.4, getZ(), 30, 2.0, 1.0, 2.0, 0.05);
		level.playSound(null, getX(), getY(), getZ(), SoundEvents.AMETHYST_BLOCK_BREAK, SoundSource.HOSTILE, 2.0F, 0.7F);
		return called;
	}

	/** {@code at} kept in the nave, on its floor. */
	public Vec3 settle(ServerLevel level, Vec3 at) {
		Vec3 centre = centre();
		double x = Math.max(origin.getX() + GlassChapel.NAVE_WEST + 1.0, Math.min(origin.getX() + GlassChapel.NAVE_EAST, at.x));
		double z = Math.max(origin.getZ() + GlassChapel.NAVE_NORTH + 1.0, Math.min(origin.getZ() + GlassChapel.NAVE_SOUTH, at.z));
		BlockPos cell = BlockPos.containing(x, floorY(), z);
		if (!level.getBlockState(cell).isAir() || !level.getBlockState(cell.above()).isAir()) {
			return new Vec3(centre.x, floorY(), centre.z);
		}
		return new Vec3(x, floorY(), z);
	}

	/** The rose window's tracery in shade on the floor under him, while his images stand: only he casts it. */
	private void shadow(ServerLevel level) {
		if (images.isEmpty() || tickCount % 5 != 0) {
			return;
		}
		double y = floorY() + 0.1;
		for (int i = 0; i < 8; i++) {
			double angle = Math.PI * i / 4.0;
			level.sendParticles(SHADE, getX() + Math.cos(angle) * 0.9, y, getZ() + Math.sin(angle) * 0.9, 1, 0.0, 0.0, 0.0, 0.0);
			level.sendParticles(SHADE, getX() + Math.cos(angle) * 0.45, y, getZ() + Math.sin(angle) * 0.45, 1, 0.0, 0.0, 0.0, 0.0);
		}
		level.sendParticles(SHADE, getX(), y, getZ(), 1, 0.0, 0.0, 0.0, 0.0);
	}

	private void prune(ServerLevel level) {
		images.removeIf(id -> !(level.getEntity(id) instanceof GlassImageEntity image) || !image.isAlive());
	}

	/** His images fade and his shards break. */
	private void dismiss(ServerLevel level) {
		for (UUID id : List.copyOf(images)) {
			if (level.getEntity(id) instanceof GlassImageEntity image) {
				image.fade(level);
			}
		}
		images.clear();
		shards.clear();
	}

	// ---------------------------------------------------------------- his rule: the Beam

	private void beamTick(ServerLevel level) {
		switch (beam) {
			case NONE -> {
				if (--beamTimer <= 0 && attack == null) {
					mark(level);
				}
			}
			case MARKED -> {
				beamTicks++;
				if (veiled != null && beamTicks == VEILED_TICKS) {
					ChapelMirrorBlock.setVeiled(level, mirror(veiled), false);
					veiled = null;
				}
				if (beamTicks % PREVIEW_EVERY == 0) {
					preview(level);
				}
				if (beamTicks % 10 == 0) {
					for (Mirror mirror : marked()) {
						BlockPos base = mirror(mirror);
						level.sendParticles(GOLD, base.getX() + 0.5, base.getY() + 2.1, base.getZ() + 0.5, 4, 0.3, 0.2, 0.3, 0.0);
					}
				}
				if (beamTicks >= MARK_TICKS) {
					gather(level);
				}
			}
			case GATHER -> {
				beamTicks++;
				if (beamTicks % 4 == 0) {
					Vec3 rose = world(GlassChapel.ABBOT_X, GlassChapel.ABBOT_Z - 6.0).add(0.0, 5.0, 0.0);
					Vec3 down = position().add(0.0, 2.2, 0.0).subtract(rose);
					for (int i = 0; i < 6; i++) {
						Vec3 at = rose.add(down.scale(getRandom().nextDouble()));
						level.sendParticles(ParticleTypes.END_ROD, at.x, at.y, at.z, 1, 0.1, 0.1, 0.1, 0.0);
					}
				}
				if (beamTicks >= GATHER_TICKS) {
					fire(level);
				}
			}
			case CRACKED -> {
				beamTicks++;
				if (beamTicks % 10 == 0) {
					level.sendParticles(ParticleTypes.CRIT, getX(), getY() + 1.6, getZ(), 4, 0.5, 0.5, 0.5, 0.05);
				}
				if (beamTicks >= CRACK_TICKS) {
					endBeam(true);
					setAction(Action.IDLE);
				}
			}
		}
	}

	/** Marks two mirrors for his beam (a pair other than the last), the beam aimed at one of them; from the Veil, one veiled. */
	public void mark(ServerLevel level) {
		List<Mirror[]> pairs = new ArrayList<>();
		Mirror[] all = Mirror.values();
		for (int i = 0; i < all.length; i++) {
			for (int j = i + 1; j < all.length; j++) {
				if (!(all[i] == markedA && all[j] == markedB)) {
					pairs.add(new Mirror[] {all[i], all[j]});
				}
			}
		}
		Mirror[] pair = pairs.get(getRandom().nextInt(pairs.size()));
		mark(level, pair[0], pair[1], getRandom().nextBoolean() ? pair[0] : pair[1]);
	}

	/** Marks {@code a} and {@code b} for his beam, aimed at {@code aim} (a test names them). */
	public void mark(ServerLevel level, Mirror a, Mirror b, Mirror aim) {
		markedA = a;
		markedB = b;
		first = aim;
		turners.clear();
		beam = Beam.MARKED;
		beamTicks = 0;
		ChapelMirrorBlock.setMarked(level, mirror(a), true);
		ChapelMirrorBlock.setMarked(level, mirror(b), true);
		if (phase == Phase.VEIL || phase == Phase.BREAKING) {
			veiled = getRandom().nextBoolean() ? a : b;
			ChapelMirrorBlock.setVeiled(level, mirror(veiled), true);
		}
		level.playSound(null, getX(), getY(), getZ(), SoundEvents.BELL_RESONATE, SoundSource.HOSTILE, 2.0F, 1.2F);
		Vec3 centre = centre();
		for (ServerPlayer player : level.players()) {
			if (player.position().distanceTo(centre) <= LEASH + 32.0) {
				player.sendSystemMessage(Component.translatable("message.jugcraft.glass_abbot.marked", a.display(), b.display()));
			}
		}
	}

	/**
	 * Radiance's preview: to each player in his fight who understands First Light (and only to them), the path the beam
	 * would take now, traced faintly from mirror to mirror.
	 */
	private void preview(ServerLevel level) {
		if (markedA == null || markedB == null || first == null || !JugcraftConfig.isFeatureEnabled(JugcraftConcordance.FEATURE)) {
			return;
		}
		List<ServerPlayer> readers = new ArrayList<>();
		for (ServerPlayer player : nearby(level)) {
			if (ConcordanceProgress.knowledge(player).state(FIRST_LIGHT).atLeast(ResearchState.UNDERSTOOD)) {
				readers.add(player);
			}
		}
		if (readers.isEmpty()) {
			return;
		}
		List<Vec3> path = path(GlassChapel.trace(GlassChapel.stops(level, origin), marked(), first));
		for (ServerPlayer reader : readers) {
			trail(level, path, ROSE, reader);
		}
	}

	/** He returns to his place and gathers the sun. */
	private void gather(ServerLevel level) {
		beam = Beam.GATHER;
		beamTicks = 0;
		breakOff();
		level.sendParticles(ParticleTypes.END_ROD, getX(), getY() + 1.2, getZ(), 20, 0.4, 1.0, 0.4, 0.05);
		Vec3 place = place();
		teleportTo(place.x, place.y, place.z);
		face(0.0F);
		setAction(Action.GATHER);
		level.sendParticles(ParticleTypes.END_ROD, getX(), getY() + 1.2, getZ(), 20, 0.4, 1.0, 0.4, 0.05);
		level.playSound(null, getX(), getY(), getZ(), SoundEvents.BEACON_AMBIENT, SoundSource.HOSTILE, 3.0F, 1.2F);
		tell(level, "message.jugcraft.glass_abbot.gather");
	}

	/**
	 * The beam leaves his mantle: worked out from the mirrors as they stand. Sent home off both marked mirrors, his mantle
	 * cracks, and whoever turned each into place has done a deed of his fight; lost, it burns along its path. Either way
	 * the mirrors' marks go out. Returns whether it came home.
	 */
	public boolean fire(ServerLevel level) {
		Mirror a = markedA;
		Mirror b = markedB;
		Mirror aim = first;
		if (a == null || b == null || aim == null) {
			endBeam(false);
			return false;
		}
		GlassChapel.Beam traced = GlassChapel.trace(GlassChapel.stops(level, origin), List.of(a, b), aim);
		boolean home = traced.home() && traced.struck().contains(a) && traced.struck().contains(b);
		beamPath = path(traced);
		beamShow = BEAM_SHOW;
		beams++;
		unmark(level);
		level.playSound(null, getX(), getY(), getZ(), SoundEvents.BEACON_POWER_SELECT, SoundSource.HOSTILE, 3.0F, 0.8F);
		if (home) {
			beam = Beam.CRACKED;
			beamTicks = 0;
			setAction(Action.DAZED);
			for (Map.Entry<Mirror, UUID> turned : turners.entrySet()) {
				if ((turned.getKey() == a || turned.getKey() == b) && level.getPlayerByUUID(turned.getValue()) instanceof ServerPlayer player
						&& LairBosses.eligible(player)) {
					encounter.deed(player, "beam " + beams + " " + turned.getKey().getSerializedName());
				}
			}
			level.playSound(null, getX(), getY(), getZ(), SoundEvents.GLASS_BREAK, SoundSource.HOSTILE, 3.0F, 0.6F);
			level.sendParticles(ParticleTypes.CRIT, getX(), getY() + 1.6, getZ(), 30, 0.6, 0.8, 0.6, 0.2);
			tell(level, "message.jugcraft.glass_abbot.home");
		} else {
			burn(level, beamPath);
			tell(level, "message.jugcraft.glass_abbot.lost");
			endBeam(false);
		}
		turners.clear();
		return home;
	}

	/** The marks and the veil go out of his mirrors. */
	private void unmark(ServerLevel level) {
		for (Mirror mirror : Mirror.values()) {
			ChapelMirrorBlock.setMarked(level, mirror(mirror), false);
			ChapelMirrorBlock.setVeiled(level, mirror(mirror), false);
		}
		veiled = null;
	}

	/** The beam is over: the next marking comes {@link #beamEvery()} after the last began, and never sooner than {@value #BEAM_REST}. */
	private void endBeam(boolean cracked) {
		beam = Beam.NONE;
		beamTicks = 0;
		int spent = MARK_TICKS + GATHER_TICKS + (cracked ? CRACK_TICKS : 0);
		beamTimer = Math.max(BEAM_REST, beamEvery() - spent);
	}

	/** A traced beam's points, in the world, at the height his light runs. */
	public List<Vec3> path(GlassChapel.Beam traced) {
		List<Vec3> out = new ArrayList<>();
		for (double[] point : traced.points()) {
			out.add(world(point[0], point[1]));
		}
		return out;
	}

	/** A lost beam burns along its whole path: {@value #BEAM_DAMAGE} to everyone within {@value #BEAM_WIDTH} of it, once. */
	public int burn(ServerLevel level, List<Vec3> path) {
		int hit = 0;
		Set<UUID> burned = new HashSet<>();
		for (ServerPlayer player : nearby(level)) {
			if (player.getY() < floorY() - 0.5 || player.getY() > floorY() + 2.5) {
				continue;
			}
			Vec3 at = new Vec3(player.getX(), 0.0, player.getZ());
			for (int i = 1; i < path.size(); i++) {
				if (flatDistance(at, path.get(i - 1), path.get(i)) <= BEAM_WIDTH && burned.add(player.getUUID())) {
					if (player.hurtServer(level, damageSources().mobAttack(this), damage(BEAM_DAMAGE))) {
						hit++;
					}
					player.igniteForSeconds(2.0F);
				}
			}
		}
		return hit;
	}

	private void showBeam(ServerLevel level) {
		if (beamShow <= 0) {
			return;
		}
		beamShow--;
		trail(level, beamPath, beam == Beam.CRACKED ? GOLD : ParticleTypes.END_ROD, null);
	}

	/** Particles along {@code path}, to everyone or to {@code only}. */
	private static void trail(ServerLevel level, List<Vec3> path, ParticleOptions particle, @Nullable ServerPlayer only) {
		for (int i = 1; i < path.size(); i++) {
			Vec3 from = path.get(i - 1);
			Vec3 to = path.get(i);
			double length = from.distanceTo(to);
			for (double d = 0.0; d <= length; d += 0.6) {
				Vec3 at = from.add(to.subtract(from).scale(length < 1.0E-6 ? 0.0 : d / length));
				if (only != null) {
					level.sendParticles(only, particle, true, false, at.x, at.y, at.z, 1, 0.0, 0.0, 0.0, 0.0);
				} else {
					level.sendParticles(particle, at.x, at.y, at.z, 1, 0.02, 0.02, 0.02, 0.0);
				}
			}
		}
	}

	/** A player turned a mirror of his chapel: if it is marked for his beam and now at the pair's stop, theirs is the turn. */
	public void turned(ServerPlayer player, Mirror mirror, int stop) {
		if ((beam != Beam.MARKED && beam != Beam.GATHER) || (mirror != markedA && mirror != markedB) || markedA == null || markedB == null) {
			return;
		}
		Mirror other = mirror == markedA ? markedB : markedA;
		if (stop == mirror.stopFor(other)) {
			turners.put(mirror, player.getUUID());
		} else {
			turners.remove(mirror);
		}
	}

	// ---------------------------------------------------------------- his own turns of the mirrors, from the Veil

	private void turnTick(ServerLevel level) {
		if (turning != null) {
			BlockPos base = mirror(turning);
			if (turnTell % 6 == 0) {
				level.sendParticles(ParticleTypes.END_ROD, base.getX() + 0.5, base.getY() + 1.2, base.getZ() + 0.5, 6, 0.3, 0.8, 0.3, 0.02);
				level.playSound(null, base, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.HOSTILE, 1.5F, 0.9F);
			}
			if (--turnTell <= 0) {
				int stop = ChapelMirrorBlock.turn(level, base, null);
				if (stop >= 0 && (turning == markedA || turning == markedB)) {
					turners.remove(turning);
				}
				Vec3 centre = centre();
				for (ServerPlayer player : level.players()) {
					if (player.position().distanceTo(centre) <= LEASH + 32.0) {
						player.sendSystemMessage(Component.translatable("message.jugcraft.glass_abbot.turned", turning.display()));
					}
				}
				turning = null;
				turnTimer = TURN_EVERY;
			}
			return;
		}
		if (--turnTimer > 0) {
			return;
		}
		boolean late = beam == Beam.MARKED && MARK_TICKS - beamTicks <= TURN_GRACE + TURN_TELL;
		if (beam == Beam.GATHER || beam == Beam.CRACKED || late) {
			turnTimer = 20;
			return;
		}
		turning = Mirror.values()[getRandom().nextInt(Mirror.values().length)];
		turnTell = TURN_TELL;
	}

	// ---------------------------------------------------------------- the Veil and the Breaking

	/** The Veil: he folds his hands, unhurt, the windows dim, and his images stand up. */
	public void veil(ServerLevel level) {
		breakOff();
		phase = Phase.VEILING;
		phaseTicks = 0;
		setAction(Action.VEIL);
		bossBar.setName(barName());
		level.playSound(null, getX(), getY(), getZ(), SoundEvents.BEACON_DEACTIVATE, SoundSource.HOSTILE, 3.0F, 0.6F);
		tell(level, "message.jugcraft.glass_abbot.veil");
	}

	private void veiling(ServerLevel level) {
		hold();
		beamTick(level);
		if (phaseTicks % 8 == 0) {
			level.sendParticles(SHADE, getX(), getY() + 1.4, getZ(), 10, 1.2, 1.0, 1.2, 0.0);
		}
		if (phaseTicks >= VEIL_TICKS) {
			phase = Phase.VEIL;
			phaseTicks = 0;
			turnTimer = TURN_EVERY;
			bossBar.setName(barName());
			callImages(level);
			setAction(Action.IDLE);
		}
	}

	/** The Breaking: his beam comes oftener, and his images are three. */
	public void breaking(ServerLevel level) {
		phase = Phase.BREAKING;
		phaseTicks = 0;
		bossBar.setName(barName());
		tell(level, "message.jugcraft.glass_abbot.breaking");
	}

	private Component barName() {
		return switch (phase) {
			case VEIL, VEILING -> Component.translatable("message.jugcraft.glass_abbot.veil_bar");
			case BREAKING -> Component.translatable("message.jugcraft.glass_abbot.breaking_bar");
			default -> Component.translatable("entity.jugcraft.glass_abbot");
		};
	}

	// ---------------------------------------------------------------- who took part

	/** Whoever hurt him or his images has taken part. */
	public void took(ServerPlayer player, float amount) {
		encounter.damage(player, amount);
	}

	/** His participants still near the chapel: everyone who qualified in his fight and is alive within reach. */
	public List<ServerPlayer> participants(ServerLevel level) {
		List<ServerPlayer> out = new ArrayList<>();
		Vec3 centre = centre();
		for (UUID id : encounter.qualified()) {
			ServerPlayer player = level.getServer().getPlayerList().getPlayer(id);
			if (player != null && player.isAlive() && player.level() == level && player.position().distanceTo(centre) <= Encounter.REACH) {
				out.add(player);
			}
		}
		return out;
	}

	// ---------------------------------------------------------------- abandoned, and defeated

	/** Left alone: he returns to his lectern and kneels, healed; his images fade, the mirrors turn back to rest. */
	public void reset(ServerLevel level) {
		dismiss(level);
		ChapelMirrorBlock.reset(level, origin);
		phase = Phase.WAITING;
		phaseTicks = 0;
		attack = null;
		attackTicks = 0;
		abandoned = 0;
		beam = Beam.NONE;
		beamTicks = 0;
		beamTimer = BEAM_FIRST;
		markedA = null;
		markedB = null;
		first = null;
		veiled = null;
		turning = null;
		turners.clear();
		beamShow = 0;
		foe = null;
		encounter.abandon();
		Arrays.fill(cooldowns, 0);
		AttributeInstance health = getAttribute(Attributes.MAX_HEALTH);
		if (health != null) {
			health.setBaseValue(HEALTH);
		}
		setHealth(getMaxHealth());
		Vec3 place = place();
		teleportTo(place.x, place.y, place.z);
		face(180.0F);
		setAction(Action.PRAY);
		bossBar.setName(Component.translatable("entity.jugcraft.glass_abbot"));
		bossBar.setVisible(false);
		tell(level, "message.jugcraft.glass_abbot.reset");
	}

	@Override
	public void die(DamageSource source) {
		super.die(source);
		if (level() instanceof ServerLevel level) {
			// A killing blow gets here inside super.hurtServer, before hurtServer's own took(): its striker has taken part.
			if (Encounter.striker(source) instanceof ServerPlayer player) {
				took(player, Float.MIN_VALUE);
			}
			defeated(level);
		}
	}

	/**
	 * He has fallen: his images fade and his mirrors turn back to rest, each participant is paid, his lair's instance ends
	 * (it closes once everyone has left), his mantle shatters into light, and the Grey Mist opens in the apse.
	 */
	private void defeated(ServerLevel level) {
		if (!encounter.claim()) {
			return;
		}
		dismiss(level);
		ChapelMirrorBlock.reset(level, origin);
		LairRewards.pay(level, AbbotLoot.BOSS, encounter, position(), centre(), EXPERIENCE);
		LairInstance open = lairInstance(level);
		if (open != null) {
			Lairs.end(open);
		}
		BlockState mist = JugcraftLairs.LAIR_EXIT.defaultBlockState().setValue(LairExitBlock.AXIS, Direction.Axis.X);
		for (BlockPos cell : fallExitCells()) {
			BlockPos at = origin.offset(cell);
			if (level.getBlockState(at).isAir()) {
				level.setBlock(at, mist, Block.UPDATE_ALL);
			}
		}
		level.sendParticles(ParticleTypes.END_ROD, getX(), getY() + 1.4, getZ(), 80, 1.0, 1.5, 1.0, 0.15);
		level.playSound(null, getX(), getY(), getZ(), SoundEvents.GLASS_BREAK, SoundSource.HOSTILE, 4.0F, 0.5F);
		bossBar.setVisible(false);
		tell(level, "message.jugcraft.glass_abbot.defeated");
	}

	/** Tells everyone near the chapel. */
	void tell(ServerLevel level, String key) {
		Vec3 centre = centre();
		for (ServerPlayer player : level.players()) {
			if (player.position().distanceTo(centre) <= LEASH + 32.0) {
				player.sendSystemMessage(Component.translatable(key));
			}
		}
	}

	// ---------------------------------------------------------------- his mantle, and what he is immune to

	/**
	 * Blows on him: kneeling, one from a player who may fight him wakes him and does nothing; waking or veiling, nothing; his
	 * mantle whole, {@value #MANTLE} of it; cracked, {@value #CRACKED_MULTIPLIER} of it.
	 */
	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
		if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
			return super.hurtServer(level, source, amount);
		}
		if (phase == Phase.WAITING) {
			if (source.getEntity() instanceof Player player && LairBosses.eligible(player)) {
				wake(level);
			}
			return false;
		}
		if (phase == Phase.WAKING || phase == Phase.VEILING) {
			return false;
		}
		float dealt = amount * mantle();
		if (beam != Beam.CRACKED) {
			level.sendParticles(SHARD, getX(), getY() + 1.6, getZ(), 6, 0.4, 0.4, 0.4, 0.05);
			level.playSound(null, getX(), getY(), getZ(), SoundEvents.GLASS_HIT, SoundSource.HOSTILE, 1.0F, 1.4F);
			if (source.getEntity() instanceof ServerPlayer player && tickCount % 40 < 2) {
				player.sendOverlayMessage(Component.translatable("message.jugcraft.glass_abbot.mantle"));
			}
		}
		boolean hurt = super.hurtServer(level, source, dealt);
		if (hurt && Encounter.striker(source) instanceof ServerPlayer player) {
			took(player, dealt);
		}
		return hurt;
	}

	/** The share of a blow his mantle lets through now. */
	public float mantle() {
		return beam == Beam.CRACKED ? CRACKED_MULTIPLIER : MANTLE;
	}

	@Override
	public boolean causeFallDamage(double distance, float multiplier, DamageSource source) {
		return false;
	}

	@Override
	public boolean canBreatheUnderwater() {
		return true;
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
	protected boolean canRide(Entity vehicle) {
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
	public void startSeenByPlayer(ServerPlayer player) {
		super.startSeenByPlayer(player);
		bossBar.addPlayer(player);
	}

	@Override
	public void stopSeenByPlayer(ServerPlayer player) {
		super.stopSeenByPlayer(player);
		bossBar.removePlayer(player);
	}

	@Override
	protected @Nullable SoundEvent getAmbientSound() {
		return phase == Phase.WAITING ? null : SoundEvents.AMETHYST_BLOCK_RESONATE;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return SoundEvents.GLASS_HIT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.GLASS_BREAK;
	}

	@Override
	public float getVoicePitch() {
		return 0.6F;
	}

	// ---------------------------------------------------------------- saving and syncing

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(ACTION, Action.PRAY.ordinal());
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		if (instance != null) {
			output.store("instance", UUIDUtil.CODEC, instance);
			output.putInt("slot", slot);
		}
		output.store("origin", BlockPos.CODEC, origin);
		output.putString("phase", phase.name());
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		instance = input.read("instance", UUIDUtil.CODEC).orElse(null);
		slot = input.getIntOr("slot", -1);
		origin = input.read("origin", BlockPos.CODEC).orElse(BlockPos.containing(getX() - GlassChapel.ABBOT_X,
				getY() - GlassChapel.STAND, getZ() - GlassChapel.ABBOT_Z));
		try {
			phase = Phase.valueOf(input.getStringOr("phase", Phase.WAITING.name()));
		} catch (IllegalArgumentException e) {
			phase = Phase.WAITING;
		}
		// A passing moment is not resumed: waking, he fights on; veiling, the Veil has fallen. His beam is never saved, so
		// his mirrors go back to rest as he loads.
		if (phase == Phase.WAKING) {
			phase = Phase.VESPERS;
		} else if (phase == Phase.VEILING) {
			phase = Phase.VEIL;
		}
		landOnLoad = phase != Phase.WAITING;
		beam = Beam.NONE;
		beamTimer = BEAM_FIRST;
		setAction(phase == Phase.WAITING ? Action.PRAY : Action.IDLE);
		bossBar.setVisible(phase != Phase.WAITING);
		bossBar.setName(barName());
	}

	// ---------------------------------------------------------------- his animations

	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
		controllers.add(new AnimationController<GlassAbbotEntity>("body", 4, test -> {
			GlassAbbotEntity abbot = test.animatable();
			if (abbot.isDeadOrDying()) {
				return test.setAndContinue(DEATH);
			}
			Action action = abbot.action();
			if (action == Action.IDLE && test.isMoving()) {
				return test.setAndContinue(WALK);
			}
			return test.setAndContinue(BODY[action.ordinal()]);
		}));
	}

	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache() {
		return cache;
	}
}

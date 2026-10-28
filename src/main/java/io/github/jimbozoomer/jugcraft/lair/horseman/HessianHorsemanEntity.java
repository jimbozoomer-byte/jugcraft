package io.github.jimbozoomer.jugcraft.lair.horseman;

import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.LoopType;
import com.geckolib.util.GeckoLibUtil;
import io.github.jimbozoomer.jugcraft.lair.ChurchBridge;
import io.github.jimbozoomer.jugcraft.lair.ChurchBridge.Brazier;
import io.github.jimbozoomer.jugcraft.lair.Encounter;
import io.github.jimbozoomer.jugcraft.lair.JugcraftLairs;
import io.github.jimbozoomer.jugcraft.lair.Lair;
import io.github.jimbozoomer.jugcraft.lair.LairBosses;
import io.github.jimbozoomer.jugcraft.lair.LairBrazierBlock;
import io.github.jimbozoomer.jugcraft.lair.LairExitBlock;
import io.github.jimbozoomer.jugcraft.lair.LairInstance;
import io.github.jimbozoomer.jugcraft.lair.LairRewards;
import io.github.jimbozoomer.jugcraft.lair.Lairs;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * The Headless Horseman in his lair (docs/features/hessian-horseman.md): the Church Bridge's boss, the Hessian trooper
 * whose head was carried off by a cannonball, on his black horse, his cloak streaming, a jack-o'-lantern on his saddle
 * where his head should be. Out of the Halloween season he waits mounted at the bridge's foot, his lantern dark, and
 * wakes when someone who may fight him steps off the wood road into the hollow, or strikes him: his horse rears (unhurt),
 * his health scaled for the party, his lantern bursts into flame, and he laughs. He keeps to the hollow, and never
 * crosses the bridge: his horse balks at it, but he throws.
 *
 * <p>His rule, the Dark Road: every {@value #SNUFF_EVERY} ticks ({@value #CHARGE_EVERY} in the Headless Charge,
 * {@value #LAST_EVERY} in the Last Ride) he wheels and lifts his lantern ({@value #SNUFF_TICKS} ticks) and snuffs
 * {@value #SNUFFED} of the hollow's eight Soul Braziers ({@value #LAST_SNUFFED} in the Last Ride): the one nearest the
 * player he hunts, and on round the ring. Any player lights a dark one by using it ({@link LairBrazierBlock}). While
 * {@value #HIDDEN_DARK} or more are dark he is hidden in the dark, and every blow does {@value #HIDDEN} times its damage.
 * Every brazier of a snuffing lit again within {@value #RELIGHT_TICKS} ticks, he is revealed: his lantern flares white,
 * his horse rears ({@value #REVEAL_STAGGER} ticks), and for {@value #REVEALED_TICKS} ticks every blow does
 * {@value #REVEALED} times its damage. At {@value #WAKES_AT} of his health, the Hollow Wakes: he rides back to the bridge's
 * foot and lifts his lantern high (unhurt), every brazier gutters and {@value #WAKES_SNUFFED} go dark; the Headless Charge
 * follows, with the Head Hurl and the Phantom Riders besides. Below {@value #LAST_AT}, the Last Ride.
 *
 * <p>Every attack has a wind-up you can read, a strike and a recovery ({@link Attack}); the numbers are here and in
 * tools/hessian_horseman.py. Poison, Wither, drowning, knockback and falls never harm or move him. Each player who
 * qualified in his fight ({@link Encounter}: hurt him, healed or warded others, or lit a brazier of a snuffing that revealed
 * him) is paid when he falls ({@link HorsemanLoot}, through lair/LairRewards). Left alone he rides back to the bridge's
 * foot, healed; his riders fade, his braziers burn again, and who hurt him is forgotten. Bound to a lair instance he
 * vanishes with it; one with no instance (a test's) keeps to the hollow laid out from the origin he was given. The only
 * blocks he changes are the braziers and, on his fall, the Grey Mist on the bridge.
 */
public class HessianHorsemanEntity extends Monster implements GeoEntity {
	// ---------------------------------------------------------------- numbers (tools/hessian_horseman.py)
	public static final float HEALTH = 420.0F;
	public static final double ARMOR = 8.0;
	public static final double LEASH = 24.0;
	public static final double PARTY_STEP = 0.5;
	public static final double PARTY_MAX = 2.5;
	public static final int WAKE_TICKS = 50;
	public static final int ABANDON_TICKS = 200;
	public static final int GLOBAL_COOLDOWN = 16;
	public static final double SPEED = 0.24;
	public static final double KEEP_DISTANCE = 3.0;
	public static final double STEP = 1.6;
	public static final float WAKES_AT = 0.5F;
	public static final float LAST_AT = 0.25F;
	public static final int RIDE_BACK_TICKS = 30;
	public static final int WAKES_TICKS = 40;
	public static final int WAKES_SNUFFED = 2;
	public static final int SNUFF_EVERY = 480;
	public static final int CHARGE_EVERY = 360;
	public static final int LAST_EVERY = 300;
	public static final int SNUFF_TICKS = 24;
	public static final int SNUFFED = 2;
	public static final int LAST_SNUFFED = 3;
	public static final int RELIGHT_TICKS = 200;
	public static final int HIDDEN_DARK = 4;
	public static final float HIDDEN = 0.5F;
	public static final int REVEAL_STAGGER = 40;
	public static final int REVEALED_TICKS = 160;
	public static final float REVEALED = 1.5F;
	public static final float PASS_DAMAGE = 9.0F;
	public static final double PASS_LENGTH = 16.0;
	public static final double PASS_HALF_WIDTH = 1.2;
	public static final double PASS_THROW = 0.9;
	public static final float TOSS_DAMAGE = 7.0F;
	public static final double TOSS_SPLASH = 3.0;
	public static final int TOSS_FIRE_SECONDS = 3;
	public static final double TOSS_RANGE = 20.0;
	public static final int TOSS_FLIGHT = 12;
	public static final float STOMP_DAMAGE = 6.0F;
	public static final double STOMP_REACH = 3.5;
	public static final float HURL_DAMAGE = 12.0F;
	public static final double HURL_SPLASH = 2.5;
	public static final int HURL_FIRE_SECONDS = 4;
	public static final double HURL_RANGE = 30.0;
	public static final int HURL_FLIGHT = 16;
	public static final int RIDERS = 2;
	public static final float RIDER_DAMAGE = 6.0F;
	public static final double RIDER_HALF_WIDTH = 1.2;
	public static final int EXPERIENCE = 350;
	/** How often (at most) he says his horse balks at the bridge. */
	private static final int BALK_EVERY = 200;

	/**
	 * His attacks: wind-up, the active part and recovery in ticks, the cooldown, the target's nearest and farthest distance,
	 * and the phases he uses it in (the Ride; the Headless Charge and the Last Ride). The wind-up is what players read; the
	 * strike lands as it ends. The Sabre Pass's active ticks are his gallop, the Pumpkin Toss's and the Head Hurl's their
	 * flight, and the Phantom Riders' the riders' gallop.
	 */
	public enum Attack {
		SABRE_PASS(20, 20, 14, 100, 4.0, 16.0, true, true),
		PUMPKIN_TOSS(20, 12, 10, 90, 5.0, 20.0, true, true),
		REARING_STOMP(16, 1, 14, 60, 0.0, 3.5, true, true),
		HEAD_HURL(30, 16, 14, 200, 6.0, 30.0, false, true),
		PHANTOM_RIDERS(30, 24, 12, 260, 0.0, 40.0, false, true);

		public final int windup;
		public final int active;
		public final int recovery;
		public final int cooldown;
		public final double near;
		public final double far;
		public final boolean ride;
		public final boolean charge;

		Attack(int windup, int active, int recovery, int cooldown, double near, double far, boolean ride, boolean charge) {
			this.windup = windup;
			this.active = active;
			this.recovery = recovery;
			this.cooldown = cooldown;
			this.near = near;
			this.far = far;
			this.ride = ride;
			this.charge = charge;
		}

		public int length() {
			return windup + active + recovery;
		}

		/** Whether he can still use it on a foe on the bridge, which his horse balks at: what he throws. */
		public boolean thrown() {
			return this == PUMPKIN_TOSS || this == HEAD_HURL;
		}
	}

	/** The fight's phases. */
	public enum Phase {
		WAITING, WAKING, RIDE, HOLLOW_WAKES, CHARGE, LAST
	}

	/** What he and his horse are doing, synced for their animations. */
	public enum Action {
		WAIT, REAR_WAKE, IDLE, PASS_WINDUP, PASS, TOSS_WINDUP, TOSS, STOMP_WINDUP, STOMP, SNUFF, HURL_WINDUP, HURL, RIDERS_WINDUP,
		RIDERS, STAGGER, LIFT
	}

	/** His lantern, and the jack-o'-lantern on his saddle, synced: dark (waiting, fallen), lit, or flaring white (revealed). */
	public enum Lantern {
		DARK, LIT, WHITE
	}

	/** What he holds up in his left hand, synced: his lantern, a pumpkin from his saddle, or his lantern head. */
	public enum Held {
		NONE, PUMPKIN, HEAD
	}

	private static final EntityDataAccessor<Integer> ACTION = SynchedEntityData.defineId(HessianHorsemanEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> LANTERN = SynchedEntityData.defineId(HessianHorsemanEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> HELD = SynchedEntityData.defineId(HessianHorsemanEntity.class, EntityDataSerializers.INT);
	static final RawAnimation[] BODY = new RawAnimation[Action.values().length];
	static final RawAnimation[] LANTERNS = new RawAnimation[Lantern.values().length];
	static final RawAnimation WALK = RawAnimation.begin().thenLoop("animation.hessian_horseman.walk");
	static final RawAnimation DEATH = RawAnimation.begin().thenPlayAndHold("animation.hessian_horseman.death");
	static final RawAnimation[] HELDS = new RawAnimation[Held.values().length];

	static {
		String prefix = "animation.hessian_horseman.";
		BODY[Action.WAIT.ordinal()] = RawAnimation.begin().thenLoop(prefix + "wait");
		BODY[Action.REAR_WAKE.ordinal()] = RawAnimation.begin().thenPlayAndHold(prefix + "rear_wake");
		BODY[Action.IDLE.ordinal()] = RawAnimation.begin().thenLoop(prefix + "idle");
		BODY[Action.PASS_WINDUP.ordinal()] = RawAnimation.begin().thenPlayAndHold(prefix + "pass_windup");
		BODY[Action.PASS.ordinal()] = RawAnimation.begin().thenLoop(prefix + "pass");
		BODY[Action.TOSS_WINDUP.ordinal()] = RawAnimation.begin().thenPlayAndHold(prefix + "toss_windup");
		BODY[Action.TOSS.ordinal()] = RawAnimation.begin().then(prefix + "toss", LoopType.PLAY_ONCE).thenLoop(prefix + "idle");
		BODY[Action.STOMP_WINDUP.ordinal()] = RawAnimation.begin().thenPlayAndHold(prefix + "stomp_windup");
		BODY[Action.STOMP.ordinal()] = RawAnimation.begin().then(prefix + "stomp", LoopType.PLAY_ONCE).thenLoop(prefix + "idle");
		BODY[Action.SNUFF.ordinal()] = RawAnimation.begin().thenPlayAndHold(prefix + "snuff");
		BODY[Action.HURL_WINDUP.ordinal()] = RawAnimation.begin().thenPlayAndHold(prefix + "hurl_windup");
		BODY[Action.HURL.ordinal()] = RawAnimation.begin().then(prefix + "hurl", LoopType.PLAY_ONCE).thenLoop(prefix + "idle");
		BODY[Action.RIDERS_WINDUP.ordinal()] = RawAnimation.begin().thenPlayAndHold(prefix + "riders_windup");
		BODY[Action.RIDERS.ordinal()] = RawAnimation.begin().thenLoop(prefix + "riders");
		BODY[Action.STAGGER.ordinal()] = RawAnimation.begin().thenLoop(prefix + "stagger");
		BODY[Action.LIFT.ordinal()] = RawAnimation.begin().thenPlayAndHold(prefix + "lift");
		LANTERNS[Lantern.DARK.ordinal()] = RawAnimation.begin().thenLoop(prefix + "lantern_dark");
		LANTERNS[Lantern.LIT.ordinal()] = RawAnimation.begin().thenLoop(prefix + "lantern_lit");
		LANTERNS[Lantern.WHITE.ordinal()] = RawAnimation.begin().thenLoop(prefix + "lantern_white");
		HELDS[Held.NONE.ordinal()] = RawAnimation.begin().thenLoop(prefix + "held_none");
		HELDS[Held.PUMPKIN.ordinal()] = RawAnimation.begin().thenLoop(prefix + "held_pumpkin");
		HELDS[Held.HEAD.ordinal()] = RawAnimation.begin().thenLoop(prefix + "held_head");
	}

	private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
	private final ServerBossEvent bossBar = new ServerBossEvent(UUID.randomUUID(), Component.translatable("entity.jugcraft.hessian_horseman"),
			BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.NOTCHED_10);
	/** The lair instance he belongs to (null for one put down by a test or a command), and the hollow's origin. */
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
	private int staggerTicks;
	private int balked;
	/** Who takes part in this fight, and how (Concordance Chunk A); not saved. */
	private final Encounter encounter = new Encounter(this, this::centre);
	// His rule, the Dark Road.
	private int snuffIn = SNUFF_EVERY;
	/** Ticks into his snuffing's tell (-1 when he is not snuffing), and the braziers it will put out. */
	private int snuffTicks = -1;
	private final List<Brazier> snuffing = new ArrayList<>();
	/** The braziers of the last snuffing, the ticks left to light them all again, and who lit them. */
	private final List<Brazier> snuffed = new ArrayList<>();
	private int relightTicks;
	private final Set<UUID> lighters = new HashSet<>();
	/** How many snuffings he has made this fight (each one's deeds are named by it). */
	private int snuffings;
	private int revealedTicks;
	// His attacks' marks: where the toss or the hurl lands, the pass's line, and the riders' lines' crossing and way.
	private Vec3 mark = Vec3.ZERO;
	private Vec3 lineFrom = Vec3.ZERO;
	private Vec3 lineWay = new Vec3(0.0, 0.0, 1.0);
	private double lineLength;
	private final Set<UUID> passed = new HashSet<>();
	private final List<UUID> riders = new ArrayList<>();
	private final List<UUID> thrown = new ArrayList<>();
	// His ride back to the bridge's foot at the Hollow Wakes, through anything.
	private boolean riding;
	private Vec3 rideFrom = Vec3.ZERO;
	private int rideTicks;
	/** Set when he is loaded fighting: his braziers burn again (the snuffing is not saved). */
	private boolean lightOnLoad;

	public HessianHorsemanEntity(EntityType<? extends HessianHorsemanEntity> type, Level level) {
		super(type, level);
		xpReward = 0;
		setPersistenceRequired();
		bossBar.setDarkenScreen(true);
		bossBar.setVisible(false);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, HEALTH).add(Attributes.ARMOR, ARMOR)
				.add(Attributes.KNOCKBACK_RESISTANCE, 1.0).add(Attributes.FOLLOW_RANGE, 48.0).add(Attributes.MOVEMENT_SPEED, 0.3)
				.add(Attributes.ATTACK_DAMAGE, PASS_DAMAGE).add(Attributes.STEP_HEIGHT, STEP);
	}

	/**
	 * Sets him waiting at the bridge's foot of {@code instance}'s hollow. Called as the instance is placed. Placing the
	 * template puts back only its own blocks, so the Grey Mist a fallen Horseman opened on the bridge before still stands:
	 * it goes first.
	 */
	public static HessianHorsemanEntity summon(ServerLevel level, LairInstance instance) {
		BlockPos origin = instance.origin();
		for (BlockPos cell : ChurchBridge.bridgeExitCells()) {
			BlockPos at = cell.offset(origin);
			if (level.getBlockState(at).is(JugcraftLairs.LAIR_EXIT)) {
				level.setBlock(at, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
			}
		}
		return summon(level, origin, instance);
	}

	/** Sets him waiting at the bridge's foot of the hollow laid out from {@code origin} (tests pass no instance). */
	public static HessianHorsemanEntity summon(ServerLevel level, BlockPos origin, @Nullable LairInstance instance) {
		HessianHorsemanEntity horseman = new HessianHorsemanEntity(JugcraftHorseman.HESSIAN_HORSEMAN, level);
		horseman.origin = origin.immutable();
		if (instance != null) {
			horseman.instance = instance.id;
			horseman.slot = instance.slot;
		}
		Vec3 waiting = horseman.waiting();
		horseman.snapTo(waiting.x, waiting.y, waiting.z, ChurchBridge.WAIT_YAW, 0.0F);
		horseman.face(ChurchBridge.WAIT_YAW);
		level.addFreshEntity(horseman);
		return horseman;
	}

	// ---------------------------------------------------------------- where things are

	/** The hollow's middle, on its floor, for the hollow laid out from {@code origin}. */
	public static Vec3 centre(BlockPos origin) {
		return ChurchBridge.centre().add(origin.getX(), origin.getY(), origin.getZ());
	}

	public Vec3 centre() {
		return centre(origin);
	}

	public BlockPos origin() {
		return origin;
	}

	/** Where he waits, mounted at the bridge's foot. */
	public Vec3 waiting() {
		Vec3 wait = ChurchBridge.waiting();
		return new Vec3(origin.getX() + wait.x, origin.getY() + wait.y, origin.getZ() + wait.z);
	}

	/** The hollow's floor, where he and his foes stand. */
	public double floorY() {
		return origin.getY() + ChurchBridge.STAND;
	}

	private boolean atFloor(Entity entity) {
		double y = entity.getY() - origin.getY();
		return y >= ChurchBridge.STAND - 2 && y <= ChurchBridge.STAND + 8;
	}

	/** Whether {@code entity} is in the hollow, where he fights. */
	public boolean inHollow(Entity entity) {
		return ChurchBridge.inHollow(entity.getX() - origin.getX(), entity.getZ() - origin.getZ()) && atFloor(entity);
	}

	/** Whether {@code entity} is on the covered bridge, which his horse balks at. */
	public boolean onBridge(Entity entity) {
		return ChurchBridge.onBridge(entity.getX() - origin.getX(), entity.getZ() - origin.getZ()) && atFloor(entity);
	}

	/** Whether {@code entity} is on the wood road (where it runs through the hollow too). */
	public boolean onRoad(Entity entity) {
		return ChurchBridge.onRoad(entity.getX() - origin.getX(), entity.getZ() - origin.getZ()) && atFloor(entity);
	}

	/** Whether he can reach {@code player} with something: in the hollow, or on the bridge, where he throws. */
	public boolean inReach(Player player) {
		return inHollow(player) || onBridge(player);
	}

	// ---------------------------------------------------------------- state

	public Phase phase() {
		return phase;
	}

	public @Nullable Attack attack() {
		return attack;
	}

	public int attackTicks() {
		return attackTicks;
	}

	public int staggerTicks() {
		return staggerTicks;
	}

	public int snuffIn() {
		return snuffIn;
	}

	/** Sets the ticks to his next snuffing (for tests). */
	public void snuffIn(int ticks) {
		snuffIn = ticks;
	}

	/** The braziers he is about to snuff, while he lifts his lantern. */
	public List<Brazier> snuffing() {
		return snuffTicks >= 0 ? List.copyOf(snuffing) : List.of();
	}

	/** The braziers of his last snuffing while they can still reveal him (empty: none). */
	public List<Brazier> snuffed() {
		return relightTicks > 0 ? List.copyOf(snuffed) : List.of();
	}

	public int relightTicks() {
		return relightTicks;
	}

	/** Sets the ticks left to light his last snuffing's braziers (for tests). */
	public void relightTicks(int ticks) {
		relightTicks = ticks;
	}

	public int revealedTicks() {
		return revealedTicks;
	}

	/** Whether he is revealed: every blow bites deeper. */
	public boolean revealed() {
		return revealedTicks > 0;
	}

	/** Whether he is hidden in the dark: {@value #HIDDEN_DARK} braziers or more are dark, and he is not revealed. */
	public boolean hidden() {
		return !revealed() && dark() >= HIDDEN_DARK;
	}

	/** How many of the hollow's braziers are dark. */
	public int dark() {
		return Brazier.values().length - ChurchBridge.lit(level(), origin).size();
	}

	/** Whether he is up and about (not waiting). */
	public boolean awake() {
		return phase != Phase.WAITING;
	}

	/** Whether he is fighting (not waiting, waking or at the Hollow Wakes). */
	public boolean fighting() {
		return phase == Phase.RIDE || phase == Phase.CHARGE || phase == Phase.LAST;
	}

	public List<UUID> riders() {
		return List.copyOf(riders);
	}

	/** Where the Pumpkin Toss or the Head Hurl lands, or where the Phantom Riders' lines cross. */
	public Vec3 mark() {
		return mark;
	}

	/** The Sabre Pass's line (or the first rider's): where it starts, its way (flat, of length 1) and its length. */
	public Vec3 lineFrom() {
		return lineFrom;
	}

	public Vec3 lineWay() {
		return lineWay;
	}

	public double lineLength() {
		return lineLength;
	}

	/** What his bar says. */
	public Component bar() {
		return bossBar.getName();
	}

	public Action action() {
		int id = entityData.get(ACTION);
		return id >= 0 && id < Action.values().length ? Action.values()[id] : Action.IDLE;
	}

	private void setAction(Action action) {
		entityData.set(ACTION, action.ordinal());
	}

	/** His lantern (synced). */
	public Lantern lantern() {
		int id = entityData.get(LANTERN);
		return id >= 0 && id < Lantern.values().length ? Lantern.values()[id] : Lantern.DARK;
	}

	private void setLantern(Lantern lantern) {
		entityData.set(LANTERN, lantern.ordinal());
	}

	/** What he holds up (synced). */
	public Held held() {
		int id = entityData.get(HELD);
		return id >= 0 && id < Held.values().length ? Held.values()[id] : Held.NONE;
	}

	private void setHeld(Held held) {
		entityData.set(HELD, held.ordinal());
	}

	/** Whether he holds a pumpkin or his lantern head up to throw. */
	public boolean holding() {
		return held() != Held.NONE;
	}

	public @Nullable UUID instanceId() {
		return instance;
	}

	/** Damage he deals: the attack's own, times {@code lairs.boss_damage} ({@link LairBosses}). */
	public static float damage(float base) {
		return LairBosses.damage(base);
	}

	/** His health for a party of {@code players}: half as much again for each beyond the first, at most 2.5 times. */
	public static double partyScale(int players) {
		return LairBosses.partyScale(players, PARTY_STEP, PARTY_MAX);
	}

	/** The ticks between his snuffings in this phase. */
	public int snuffEvery() {
		return phase == Phase.LAST ? LAST_EVERY : phase == Phase.CHARGE || phase == Phase.HOLLOW_WAKES ? CHARGE_EVERY : SNUFF_EVERY;
	}

	/** How many braziers he snuffs in this phase. */
	public int snuffs() {
		return phase == Phase.LAST ? LAST_SNUFFED : SNUFFED;
	}

	/** What every blow on him is multiplied by now: revealed, hidden in the dark, or neither. */
	public float exposure() {
		return revealed() ? REVEALED : hidden() ? HIDDEN : 1.0F;
	}

	/** A vanilla sound by its id, or {@code fallback} if this game has none so named. */
	static SoundEvent sound(String id, SoundEvent fallback) {
		return BuiltInRegistries.SOUND_EVENT.getOptional(Identifier.withDefaultNamespace(id)).orElse(fallback);
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
		if (lightOnLoad) {
			lightOnLoad = false;
			lightAll(level);
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
		if (balked > 0) {
			balked--;
		}
		phaseTicks++;
		if (riding) {
			tickRide(level);
		}
		if (fighting() || phase == Phase.HOLLOW_WAKES) {
			tickDarkRoad(level);
		}
		switch (phase) {
			case WAITING -> waiting(level);
			case WAKING -> waking(level);
			case HOLLOW_WAKES -> hollowWaking(level);
			case RIDE, CHARGE, LAST -> fight(level);
		}
	}

	/** Whether his instance (if he has one) is still open. */
	private boolean bound(ServerLevel level) {
		return instance == null || lairInstance(level) != null;
	}

	private @Nullable LairInstance lairInstance(ServerLevel level) {
		Lair lair = Lair.of(level.dimension());
		LairInstance open = lair == null || instance == null ? null : Lairs.instance(lair, slot);
		return open != null && open.id.equals(instance) ? open : null;
	}

	private void waiting(ServerLevel level) {
		Vec3 waiting = waiting();
		setDeltaMovement(0.0, -0.3, 0.0);
		if (position().distanceToSqr(waiting) > 0.25) {
			teleportTo(waiting.x, waiting.y, waiting.z);
		}
		face(ChurchBridge.WAIT_YAW);
		setAction(Action.WAIT);
		if (tickCount % 10 == 0 && intruder(level) != null) {
			wake(level);
		}
	}

	/** Someone who may fight him, off the wood road into the hollow. */
	private @Nullable Player intruder(ServerLevel level) {
		for (Player player : level.players()) {
			if (LairBosses.eligible(player) && inHollow(player) && !onRoad(player)) {
				return player;
			}
		}
		return null;
	}

	/** His horse rears: his health scaled for the players about, his bar shown, his lantern bursting into flame. */
	public void wake(ServerLevel level) {
		if (phase != Phase.WAITING) {
			return;
		}
		double scale = partyScale(Math.max(1, present(level).size()));
		AttributeInstance health = getAttribute(Attributes.MAX_HEALTH);
		if (health != null) {
			health.setBaseValue(HEALTH * scale);
		}
		setHealth(getMaxHealth());
		phase = Phase.WAKING;
		phaseTicks = 0;
		setAction(Action.REAR_WAKE);
		setLantern(Lantern.LIT);
		encounter.begin();
		bossBar.setName(barName());
		bossBar.setVisible(true);
		snuffIn = SNUFF_EVERY;
		level.playSound(null, getX(), getY(), getZ(), SoundEvents.SKELETON_HORSE_AMBIENT, SoundSource.HOSTILE, 3.0F, 0.6F);
		level.playSound(null, getX(), getY(), getZ(), SoundEvents.WITCH_CELEBRATE, SoundSource.HOSTILE, 3.0F, 0.5F);
		level.sendParticles(ParticleTypes.FLAME, getX(), getY() + 2.0, getZ(), 24, 0.3, 0.3, 0.3, 0.04);
		tell(level, "message.jugcraft.hessian_horseman.wakes");
	}

	/** Waking: his horse rears, and he rides out into the Ride. */
	private void waking(ServerLevel level) {
		setDeltaMovement(0.0, -0.3, 0.0);
		if (phaseTicks >= WAKE_TICKS) {
			phase = Phase.RIDE;
			phaseTicks = 0;
			globalCooldown = GLOBAL_COOLDOWN;
			setAction(Action.IDLE);
		}
	}

	/** Players anywhere in his lair who may fight him: the party he scales for. */
	public List<ServerPlayer> present(ServerLevel level) {
		List<ServerPlayer> out = new ArrayList<>();
		for (ServerPlayer player : level.players()) {
			if (LairBosses.eligible(player) && ChurchBridge.inside(player.getX() - origin.getX(), player.getZ() - origin.getZ())
					&& atFloor(player)) {
				out.add(player);
			}
		}
		return out;
	}

	/** Players who may fight him in the hollow: those he hunts, and those his blows can reach. */
	public List<ServerPlayer> nearby(ServerLevel level) {
		List<ServerPlayer> out = new ArrayList<>();
		for (ServerPlayer player : level.players()) {
			if (LairBosses.eligible(player) && inHollow(player)) {
				out.add(player);
			}
		}
		return out;
	}

	/** Players who may fight him on the bridge, where only what he throws reaches. */
	public List<ServerPlayer> bridged(ServerLevel level) {
		List<ServerPlayer> out = new ArrayList<>();
		for (ServerPlayer player : level.players()) {
			if (LairBosses.eligible(player) && onBridge(player)) {
				out.add(player);
			}
		}
		return out;
	}

	private @Nullable Player nearest(List<ServerPlayer> among) {
		Player nearest = null;
		double best = Double.MAX_VALUE;
		for (ServerPlayer player : among) {
			double distance = player.distanceToSqr(this);
			if (distance < best) {
				best = distance;
				nearest = player;
			}
		}
		return nearest;
	}

	// ---------------------------------------------------------------- his rule: the Dark Road

	/** Starts his snuffing's tell on {@code target}: he wheels and lifts his lantern over the braziers nearest them. */
	public void beginSnuff(ServerLevel level, Player target, int count) {
		snuffing.clear();
		snuffing.addAll(ChurchBridge.run(target.getX() - origin.getX(), target.getZ() - origin.getZ(), count));
		snuffTicks = 0;
		snuffIn = snuffEvery();
		hold();
		face(target);
		setAction(Action.SNUFF);
		level.playSound(null, getX(), getY(), getZ(), SoundEvents.SKELETON_HORSE_AMBIENT, SoundSource.HOSTILE, 2.5F, 0.5F);
	}

	/** His snuffing's tell, a tick on: when it is over, the braziers go dark. */
	private void snuffingTell(ServerLevel level) {
		hold();
		if (snuffTicks % 4 == 0) {
			for (Brazier brazier : snuffing) {
				Vec3 at = Vec3.atBottomCenterOf(brazier.pos(origin));
				level.sendParticles(ParticleTypes.SMOKE, at.x, at.y + 1.0, at.z, 3, 0.2, 0.2, 0.2, 0.01);
			}
		}
		if (++snuffTicks >= SNUFF_TICKS) {
			snuff(level, List.copyOf(snuffing));
			snuffTicks = -1;
			snuffing.clear();
			setAction(Action.IDLE);
			globalCooldown = GLOBAL_COOLDOWN;
		}
	}

	/**
	 * He snuffs {@code braziers}: each goes dark, and the ticks to light them all again begin. Any snuffing still open is
	 * over (its braziers stay as they are).
	 */
	public void snuff(ServerLevel level, List<Brazier> braziers) {
		snuffings++;
		snuffed.clear();
		snuffed.addAll(braziers);
		relightTicks = RELIGHT_TICKS;
		lighters.clear();
		for (Brazier brazier : braziers) {
			BlockPos pos = brazier.pos(origin);
			BlockState state = level.getBlockState(pos);
			if (state.getBlock() instanceof LairBrazierBlock && state.getValue(LairBrazierBlock.LIT)) {
				level.setBlock(pos, state.setValue(LairBrazierBlock.LIT, false), Block.UPDATE_ALL);
			}
			Vec3 at = Vec3.atBottomCenterOf(pos);
			level.sendParticles(ParticleTypes.LARGE_SMOKE, at.x, at.y + 0.9, at.z, 12, 0.25, 0.3, 0.25, 0.02);
			level.sendParticles(ParticleTypes.SOUL, at.x, at.y + 0.9, at.z, 6, 0.2, 0.3, 0.2, 0.03);
			level.playSound(null, at.x, at.y, at.z, sound("block.fire.extinguish", SoundEvents.FIRECHARGE_USE), SoundSource.HOSTILE, 1.5F, 0.6F);
		}
		bossBar.setName(barName());
		if (braziers.size() >= 3) {
			tell(level, "message.jugcraft.hessian_horseman.snuff_three", braziers.get(0).display(), braziers.get(1).display(),
					braziers.get(2).display());
		} else if (braziers.size() == 2) {
			tell(level, "message.jugcraft.hessian_horseman.snuff", braziers.get(0).display(), braziers.get(1).display());
		}
	}

	/** The Dark Road, a tick on: his snuffing's time to light, and the time he stays revealed, run down. */
	private void tickDarkRoad(ServerLevel level) {
		if (relightTicks > 0) {
			relightTicks--;
		}
		if (revealedTicks > 0 && --revealedTicks == 0) {
			setLantern(Lantern.LIT);
			bossBar.setName(barName());
		}
		if (tickCount % 20 == 0) {
			bossBar.setName(barName());
		}
	}

	/**
	 * A player lit the brazier at {@code pos}: if it is one of his last snuffing's, they are counted, and if they are all
	 * lit again in time he is revealed. Returns whether it was one of his.
	 */
	public boolean brazierLit(ServerLevel level, BlockPos pos, ServerPlayer player) {
		Brazier lit = null;
		for (Brazier brazier : snuffed) {
			if (brazier.pos(origin).equals(pos)) {
				lit = brazier;
			}
		}
		if (lit == null || relightTicks <= 0) {
			return false;
		}
		lighters.add(player.getUUID());
		int burning = 0;
		for (Brazier brazier : snuffed) {
			if (ChurchBridge.lit(level, origin, brazier)) {
				burning++;
			}
		}
		player.sendOverlayMessage(Component.translatable("message.jugcraft.hessian_horseman.relit", burning, snuffed.size()));
		if (burning >= snuffed.size()) {
			reveal(level);
		}
		return true;
	}

	/**
	 * Every brazier of his snuffing burns again in time: his lantern flares white, his horse rears, and every blow bites
	 * deeper for {@value #REVEALED_TICKS} ticks. Those who lit them have taken part.
	 */
	public void reveal(ServerLevel level) {
		relightTicks = 0;
		revealedTicks = REVEALED_TICKS;
		if (fighting()) {
			stagger(REVEAL_STAGGER);
			snuffTicks = -1;
			snuffing.clear();
		}
		setLantern(Lantern.WHITE);
		bossBar.setName(barName());
		tell(level, "message.jugcraft.hessian_horseman.revealed");
		for (UUID id : lighters) {
			ServerPlayer lighter = level.getServer().getPlayerList().getPlayer(id);
			if (lighter != null) {
				encounter.deed(lighter, "revealed:" + snuffings + ":" + id);
			}
		}
		lighters.clear();
		level.playSound(null, getX(), getY(), getZ(), sound("entity.horse.angry", SoundEvents.SKELETON_HORSE_AMBIENT), SoundSource.HOSTILE, 3.0F, 0.6F);
		level.sendParticles(ParticleTypes.END_ROD, getX(), getY() + 2.2, getZ(), 30, 0.4, 0.4, 0.4, 0.06);
	}

	/** Every brazier burns again, and no snuffing is open. */
	public void lightAll(ServerLevel level) {
		for (Brazier brazier : Brazier.values()) {
			BlockPos pos = brazier.pos(origin);
			BlockState state = level.getBlockState(pos);
			if (state.getBlock() instanceof LairBrazierBlock && !state.getValue(LairBrazierBlock.LIT)) {
				level.setBlock(pos, state.setValue(LairBrazierBlock.LIT, true), Block.UPDATE_ALL);
			}
		}
		snuffed.clear();
		snuffing.clear();
		snuffTicks = -1;
		relightTicks = 0;
		lighters.clear();
	}

	private Component barName() {
		if (phase == Phase.WAITING || phase == Phase.WAKING) {
			return Component.translatable("entity.jugcraft.hessian_horseman");
		}
		if (revealed()) {
			return Component.translatable("message.jugcraft.hessian_horseman.revealed_bar");
		}
		if (hidden()) {
			return Component.translatable("message.jugcraft.hessian_horseman.dark_bar");
		}
		return switch (phase) {
			case HOLLOW_WAKES, CHARGE -> Component.translatable("message.jugcraft.hessian_horseman.charge_bar");
			case LAST -> Component.translatable("message.jugcraft.hessian_horseman.last_bar");
			default -> Component.translatable("entity.jugcraft.hessian_horseman");
		};
	}

	// ---------------------------------------------------------------- fighting

	private void fight(ServerLevel level) {
		Player target = target(level);
		if (attack != null) {
			if (target == null && attack != Attack.SABRE_PASS && attack != Attack.PHANTOM_RIDERS) {
				endAttack(level);
			} else {
				attackTicks++;
				perform(level, target);
				return;
			}
		}
		if (snuffTicks >= 0) {
			snuffingTell(level);
			return;
		}
		if (staggerTicks > 0) {
			staggerTicks--;
			hold();
			if (staggerTicks == 0) {
				setAction(Action.IDLE);
				globalCooldown = GLOBAL_COOLDOWN;
			}
			return;
		}
		keepToTheHollow(level);
		if (phase == Phase.RIDE && getHealth() <= getMaxHealth() * WAKES_AT) {
			hollowWakes(level);
			return;
		}
		if (phase == Phase.CHARGE && getHealth() <= getMaxHealth() * LAST_AT) {
			lastRide(level);
		}
		if (target == null) {
			setAction(Action.IDLE);
			move(waiting(), SPEED);
			if (++abandoned >= ABANDON_TICKS) {
				reset(level);
			}
			return;
		}
		abandoned = 0;
		if (snuffIn > 0) {
			snuffIn--;
		}
		if (snuffIn <= 0) {
			beginSnuff(level, target, snuffs());
			return;
		}
		face(target);
		boolean balking = onBridge(target);
		if (balking && balked <= 0 && position().distanceTo(waiting()) < 4.0) {
			balked = BALK_EVERY;
			tell(level, "message.jugcraft.hessian_horseman.balks");
			level.playSound(null, getX(), getY(), getZ(), sound("entity.horse.angry", SoundEvents.SKELETON_HORSE_AMBIENT), SoundSource.HOSTILE, 2.0F, 0.8F);
		}
		if (globalCooldown > 0) {
			globalCooldown--;
			approach(target, balking);
			return;
		}
		Attack next = choose(target, balking);
		if (next == null) {
			approach(target, balking);
		} else {
			begin(level, next, target);
		}
	}

	/** Strayed farther than his leash from the hollow's middle, or fallen below its floor, he is set back at the bridge's foot. */
	private void keepToTheHollow(ServerLevel level) {
		Vec3 centre = centre();
		double dx = getX() - centre.x;
		double dz = getZ() - centre.z;
		if (dx * dx + dz * dz <= LEASH * LEASH && getY() > centre.y - 3.0 && !onBridge(this)) {
			return;
		}
		level.sendParticles(ParticleTypes.CLOUD, getX(), getY() + 1.5, getZ(), 20, 0.8, 1.0, 0.8, 0.02);
		endAttack(level);
		Vec3 back = waiting();
		teleportTo(back.x, back.y, back.z);
		level.sendParticles(ParticleTypes.CLOUD, getX(), getY() + 1.5, getZ(), 20, 0.8, 1.0, 0.8, 0.02);
	}

	/**
	 * His target: the one he has while they are still in his reach (the hollow, or the bridge), else the nearest who may
	 * fight him in the hollow, else on the bridge.
	 */
	private @Nullable Player target(ServerLevel level) {
		if (foe != null) {
			Player held = level.getPlayerByUUID(foe);
			if (held != null && LairBosses.eligible(held) && inReach(held)) {
				return held;
			}
		}
		Player nearest = nearest(nearby(level));
		if (nearest == null) {
			nearest = nearest(bridged(level));
		}
		foe = nearest == null ? null : nearest.getUUID();
		return nearest;
	}

	/** His foe now, if he has one. */
	public @Nullable Player foe(ServerLevel level) {
		return foe == null ? null : level.getPlayerByUUID(foe);
	}

	/** Trots toward a point {@value #KEEP_DISTANCE} blocks short of {@code target}; to the bridge's foot if they are on it. */
	private void approach(Player target, boolean balking) {
		if (balking) {
			move(waiting(), SPEED);
			return;
		}
		Vec3 to = target.position().subtract(position());
		double flat = Math.sqrt(to.x * to.x + to.z * to.z);
		Vec3 goal = flat > KEEP_DISTANCE + 0.5
				? new Vec3(target.getX() - to.x / flat * KEEP_DISTANCE, getY(), target.getZ() - to.z / flat * KEEP_DISTANCE)
				: position();
		move(goal, SPEED);
	}

	/** Trots toward {@code goal} (never out of the hollow, nor onto the bridge) at {@code speed}, pressed down onto the floor. */
	private void move(Vec3 goal, double speed) {
		if (noPhysics) {
			setDeltaMovement(Vec3.ZERO);
			return;
		}
		goal = inside(goal, 1.0);
		Vec3 to = new Vec3(goal.x - getX(), 0.0, goal.z - getZ());
		double length = to.length();
		Vec3 step = length < 0.05 ? Vec3.ZERO : to.scale(Math.min(speed, length) / length);
		setDeltaMovement(step.x, -0.3, step.z);
	}

	/** {@code at}, brought inside the hollow by {@code margin} if it lies outside. */
	private Vec3 inside(Vec3 at, double margin) {
		Vec3 centre = centre();
		Vec3 out = new Vec3(at.x - centre.x, 0.0, at.z - centre.z);
		double reach = ChurchBridge.HOLLOW - margin;
		if (out.length() > reach) {
			out = out.normalize().scale(reach);
			return new Vec3(centre.x + out.x, at.y, centre.z + out.z);
		}
		return at;
	}

	/** Holds still where he is, pressed down onto what he stands on. */
	private void hold() {
		setDeltaMovement(noPhysics ? Vec3.ZERO : new Vec3(0.0, -0.3, 0.0));
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

	/** His saddle, where a pumpkin is lifted from: over his horse's withers, and up. */
	public Vec3 hand() {
		return position().add(facing().scale(0.6)).add(0.0, 3.0, 0.0);
	}

	/** Picks an attack he can use now on {@code target} (only what he throws if they are on the bridge), or null. */
	private @Nullable Attack choose(Player target, boolean balking) {
		double distance = target.distanceTo(this);
		List<Attack> ready = new ArrayList<>();
		for (Attack candidate : Attack.values()) {
			boolean phaseAllows = phase == Phase.RIDE ? candidate.ride : candidate.charge;
			if (!phaseAllows || cooldowns[candidate.ordinal()] > 0 || distance < candidate.near || distance > candidate.far
					|| balking && !candidate.thrown()) {
				continue;
			}
			ready.add(candidate);
			if (candidate == Attack.SABRE_PASS) {
				ready.add(candidate);  // his sabre comes most often
			}
		}
		return ready.isEmpty() ? null : ready.get(getRandom().nextInt(ready.size()));
	}

	/** Starts {@code next} on {@code target}: its wind-up begins, and what it aims at is fixed. */
	public void begin(ServerLevel level, Attack next, Player target) {
		attack = next;
		attackTicks = 0;
		foe = target.getUUID();
		cooldowns[next.ordinal()] = next.cooldown;
		hold();
		face(target);
		switch (next) {
			case SABRE_PASS -> {
				setAction(Action.PASS_WINDUP);
				passLine(target);
				passed.clear();
				level.playSound(null, getX(), getY(), getZ(), SoundEvents.HORSE_GALLOP, SoundSource.HOSTILE, 2.0F, 1.4F);
			}
			case PUMPKIN_TOSS -> {
				setAction(Action.TOSS_WINDUP);
				setHeld(Held.PUMPKIN);
				mark = markAt(target, TOSS_RANGE);
				level.playSound(null, getX(), getY(), getZ(), sound("entity.blaze.shoot", SoundEvents.FIRECHARGE_USE), SoundSource.HOSTILE, 1.5F, 0.6F);
			}
			case REARING_STOMP -> {
				setAction(Action.STOMP_WINDUP);
				level.playSound(null, getX(), getY(), getZ(), sound("entity.horse.angry", SoundEvents.SKELETON_HORSE_AMBIENT), SoundSource.HOSTILE,
						2.5F, 0.6F);
			}
			case HEAD_HURL -> {
				setAction(Action.HURL_WINDUP);
				setHeld(Held.HEAD);
				Player far = hurlTarget(level, target);
				mark = markAt(far, HURL_RANGE);
				level.playSound(null, getX(), getY(), getZ(), SoundEvents.WITCH_CELEBRATE, SoundSource.HOSTILE, 2.5F, 0.6F);
			}
			case PHANTOM_RIDERS -> {
				setAction(Action.RIDERS_WINDUP);
				mark = new Vec3(target.getX(), floorY(), target.getZ());
				double bearing = getRandom().nextDouble() * Math.PI;
				lineWay = new Vec3(Math.cos(bearing), 0.0, Math.sin(bearing));
				level.playSound(null, getX(), getY(), getZ(), SoundEvents.SKELETON_HORSE_AMBIENT, SoundSource.HOSTILE, 3.0F, 0.4F);
			}
		}
	}

	/** Where something thrown at {@code target} lands: where they stand, or {@code range} toward them, on the floor. */
	private Vec3 markAt(Player target, double range) {
		Vec3 to = target.position().subtract(position());
		double flat = Math.hypot(to.x, to.z);
		Vec3 at = flat <= range ? target.position() : position().add(to.x / flat * range, 0.0, to.z / flat * range);
		return new Vec3(at.x, onBridge(target) ? target.getY() : floorY(), at.z);
	}

	/** Whom the Head Hurl is thrown at: anyone on the bridge first (the farthest of them), else the farthest in the hollow. */
	public Player hurlTarget(ServerLevel level, Player fallback) {
		List<ServerPlayer> among = bridged(level);
		if (among.isEmpty()) {
			among = nearby(level);
		}
		Player farthest = fallback;
		double best = -1.0;
		for (ServerPlayer player : among) {
			double distance = player.distanceToSqr(this);
			if (distance > best) {
				best = distance;
				farthest = player;
			}
		}
		return farthest;
	}

	/** Fixes the Sabre Pass's line: from him through {@code target}'s place, {@value #PASS_LENGTH} long, but never out of the hollow. */
	private void passLine(Player target) {
		Vec3 to = new Vec3(target.getX() - getX(), 0.0, target.getZ() - getZ());
		lineWay = to.lengthSqr() < 1.0E-4 ? facing() : to.normalize();
		lineFrom = new Vec3(getX(), floorY(), getZ());
		double inside = 0.0;
		while (inside < PASS_LENGTH && inHollowAt(lineFrom.x + lineWay.x * (inside + 0.5), lineFrom.z + lineWay.z * (inside + 0.5), 1.0)) {
			inside += 0.5;
		}
		lineLength = Math.max(1.0, inside);
	}

	/** Whether (x, z) (in the world) is in the hollow, {@code margin} in from its edge, and off the bridge. */
	private boolean inHollowAt(double x, double z, double margin) {
		Vec3 centre = centre();
		return Math.hypot(x - centre.x, z - centre.z) <= ChurchBridge.HOLLOW - margin
				&& !ChurchBridge.onBridge(x - origin.getX(), z - origin.getZ());
	}

	/**
	 * A chord of the hollow through {@code through} along {@code way}: its two ends, where the hollow's edge (less a block)
	 * crosses it.
	 */
	public Vec3[] chord(Vec3 through, Vec3 way) {
		Vec3 centre = centre();
		double radius = ChurchBridge.HOLLOW - 1.0;
		double px = through.x - centre.x;
		double pz = through.z - centre.z;
		double b = px * way.x + pz * way.z;
		double c = px * px + pz * pz - radius * radius;
		double root = Math.sqrt(Math.max(0.0, b * b - c));
		double back = -b - root;
		double ahead = -b + root;
		return new Vec3[] {new Vec3(through.x + way.x * back, floorY(), through.z + way.z * back),
				new Vec3(through.x + way.x * ahead, floorY(), through.z + way.z * ahead)};
	}

	/** The Phantom Riders' lines: two chords crossing at right angles where his foe stood. */
	public List<Vec3[]> riderLines() {
		Vec3 cross = new Vec3(-lineWay.z, 0.0, lineWay.x);
		return List.of(chord(mark, lineWay), chord(mark, cross));
	}

	private void perform(ServerLevel level, @Nullable Player target) {
		Attack current = attack;
		if (current == null) {
			return;
		}
		int t = attackTicks;
		hold();
		switch (current) {
			case SABRE_PASS -> {
				if (t < current.windup) {
					face(lineWay);
					if (t % 3 == 0) {
						drawLine(level, lineFrom, lineWay, lineLength, ParticleTypes.CRIT, 0.6, PASS_HALF_WIDTH);
					}
					if (t % 5 == 0) {
						level.playSound(null, getX(), getY(), getZ(), SoundEvents.HORSE_GALLOP, SoundSource.HOSTILE, 1.5F, 1.0F + t * 0.03F);
					}
				} else if (t == current.windup) {
					setAction(Action.PASS);
					noPhysics = true;
				}
				if (t >= current.windup && t <= current.windup + current.active) {
					gallop(level, (t - current.windup) / (double) current.active);
				}
				if (t == current.windup + current.active) {
					noPhysics = false;
					setAction(Action.IDLE);
				}
			}
			case PUMPKIN_TOSS -> {
				if (t < current.windup) {
					face(mark.subtract(position()));
					if (t % 2 == 0) {
						drawRing(level, mark, TOSS_SPLASH, ParticleTypes.SMALL_FLAME);
					}
				} else if (t == current.windup) {
					setAction(Action.TOSS);
					setHeld(Held.NONE);
					throwAt(level, JugcraftHorseman.HESSIAN_PUMPKIN, TOSS_FLIGHT);
				}
			}
			case REARING_STOMP -> {
				if (t < current.windup) {
					if (target != null) {
						face(target);
					}
				} else if (t == current.windup) {
					setAction(Action.STOMP);
					rearingStomp(level);
				}
			}
			case HEAD_HURL -> {
				if (t < current.windup) {
					face(mark.subtract(position()));
					if (t % 2 == 0) {
						level.sendParticles(ParticleTypes.FLAME, mark.x, mark.y + 0.2 + getRandom().nextDouble() * 3.0, mark.z, 3, 0.15, 0.4, 0.15, 0.01);
						drawRing(level, mark, HURL_SPLASH, ParticleTypes.FLAME);
					}
				} else if (t == current.windup) {
					setAction(Action.HURL);
					setHeld(Held.NONE);
					throwAt(level, JugcraftHorseman.HESSIAN_HEAD, HURL_FLIGHT);
				}
			}
			case PHANTOM_RIDERS -> {
				if (t < current.windup) {
					if (t % 3 == 0) {
						for (Vec3[] line : riderLines()) {
							Vec3 way = line[1].subtract(line[0]);
							drawLine(level, line[0], way.normalize(), way.length(), ParticleTypes.SOUL_FIRE_FLAME, 0.5, 0.3);
						}
					}
				} else if (t == current.windup) {
					setAction(Action.RIDERS);
					phantomRiders(level, current.active);
				}
			}
		}
		if (attack == current && attackTicks >= current.length()) {
			endAttack(level);
		}
	}

	/** The Sabre Pass, {@code share} of the way along his line: he is there, and rides through whoever he reaches. */
	private void gallop(ServerLevel level, double share) {
		double along = lineLength * Math.min(1.0, share);
		Vec3 at = lineFrom.add(lineWay.scale(along));
		setPos(at.x, at.y, at.z);
		face(lineWay);
		for (ServerPlayer player : nearby(level)) {
			if (passed.contains(player.getUUID())) {
				continue;
			}
			Vec3 to = new Vec3(player.getX() - lineFrom.x, 0.0, player.getZ() - lineFrom.z);
			double ahead = to.x * lineWay.x + to.z * lineWay.z;
			double across = to.x * lineWay.z - to.z * lineWay.x;
			if (ahead < -0.5 || ahead > along + 0.5 || Math.abs(across) > PASS_HALF_WIDTH + player.getBbWidth() / 2.0
					|| Math.abs(player.getY() - lineFrom.y) > 3.0) {
				continue;
			}
			passed.add(player.getUUID());
			if (player.hurtServer(level, damageSources().mobAttack(this), damage(PASS_DAMAGE))) {
				// Thrown aside, off his line, on the side they stood.
				double side = across >= 0.0 ? 1.0 : -1.0;
				push(player, new Vec3(lineWay.z * side * PASS_THROW, 0.35, -lineWay.x * side * PASS_THROW));
			}
		}
		if (level.getGameTime() % 3 == 0) {
			level.sendParticles(ParticleTypes.SWEEP_ATTACK, getX(), getY() + 1.6, getZ(), 1, 0.3, 0.2, 0.3, 0.0);
			level.playSound(null, getX(), getY(), getZ(), SoundEvents.HORSE_GALLOP, SoundSource.HOSTILE, 2.0F, 1.2F);
		}
	}

	/** How many players his last Sabre Pass rode through. */
	public int passedThrough() {
		return passed.size();
	}

	/** He throws what he holds from his saddle onto the mark; it lands {@code flight} ticks later. */
	private void throwAt(ServerLevel level, EntityType<HessianPumpkinEntity> kind, int flight) {
		HessianPumpkinEntity thrownOne = HessianPumpkinEntity.toss(level, kind, this, hand(), mark, flight);
		thrown.add(thrownOne.getUUID());
	}

	/** The Phantom Riders: a ghostly rider at the start of each line, galloping down it over {@code ticks}. */
	public List<PhantomRiderEntity> phantomRiders(ServerLevel level, int ticks) {
		prune(level);
		List<PhantomRiderEntity> out = new ArrayList<>();
		for (Vec3[] line : riderLines()) {
			if (riders.size() >= RIDERS) {
				break;
			}
			PhantomRiderEntity rider = PhantomRiderEntity.gallop(level, this, line[0], line[1], ticks);
			riders.add(rider.getUUID());
			out.add(rider);
		}
		return out;
	}

	/** The attack under way is over or spoiled: his hands are empty, and he is on the ground. */
	private void endAttack(ServerLevel level) {
		if (attack == null) {
			return;
		}
		attack = null;
		attackTicks = 0;
		globalCooldown = GLOBAL_COOLDOWN;
		setHeld(Held.NONE);
		noPhysics = false;
		Action action = action();
		if (action == Action.PASS || action == Action.RIDERS || action.name().endsWith("_WINDUP")) {
			setAction(Action.IDLE);
		}
	}

	/** A ring of {@code particle} round {@code at}, {@code radius} across. */
	private static void drawRing(ServerLevel level, Vec3 at, double radius, ParticleOptions particle) {
		for (int i = 0; i < 16; i++) {
			double rad = Math.toRadians(22.5 * i);
			level.sendParticles(particle, at.x + Math.cos(rad) * radius, at.y + 0.1, at.z + Math.sin(rad) * radius, 1, 0.0, 0.02, 0.0, 0.0);
		}
	}

	/** {@code particle} along a line from {@code from} along {@code way}, {@code halfWidth} either side, about {@code density} a block. */
	private void drawLine(ServerLevel level, Vec3 from, Vec3 way, double length, ParticleOptions particle, double density, double halfWidth) {
		int count = Math.max(1, (int) Math.round(length * density));
		for (int i = 0; i < count; i++) {
			double along = getRandom().nextDouble() * length;
			double across = (getRandom().nextDouble() * 2.0 - 1.0) * halfWidth;
			double x = from.x + way.x * along + way.z * across;
			double z = from.z + way.z * along - way.x * across;
			level.sendParticles(particle, x, from.y + 0.2 + getRandom().nextDouble() * 0.6, z, 1, 0.0, 0.0, 0.0, 0.0);
		}
	}

	/** Rearing Stomp: every player within {@value #STOMP_REACH} blocks takes {@value #STOMP_DAMAGE}. Returns how many it hit. */
	public int rearingStomp(ServerLevel level) {
		int hit = 0;
		for (ServerPlayer player : nearby(level)) {
			Vec3 to = player.position().subtract(position());
			if (Math.hypot(to.x, to.z) > STOMP_REACH + player.getBbWidth() / 2.0 || Math.abs(to.y) > 3.0) {
				continue;
			}
			if (player.hurtServer(level, damageSources().mobAttack(this), damage(STOMP_DAMAGE))) {
				hit++;
			}
		}
		level.sendParticles(ParticleTypes.CLOUD, getX(), getY() + 0.1, getZ(), 24, STOMP_REACH / 2.0, 0.05, STOMP_REACH / 2.0, 0.02);
		level.playSound(null, getX(), getY(), getZ(), SoundEvents.RAVAGER_STEP, SoundSource.HOSTILE, 2.5F, 0.6F);
		return hit;
	}

	/** He reels: any attack under way is spoiled, and he does nothing for {@code ticks}. */
	public void stagger(int ticks) {
		attack = null;
		attackTicks = 0;
		noPhysics = false;
		setHeld(Held.NONE);
		staggerTicks = Math.max(staggerTicks, ticks);
		setAction(Action.STAGGER);
	}

	/** Sets {@code player} moving at {@code motion}, and tells their client (a player's own motion is theirs to move). */
	static void push(ServerPlayer player, Vec3 motion) {
		player.setDeltaMovement(motion);
		player.connection.send(new ClientboundSetEntityMotionPacket(player));
	}

	// ---------------------------------------------------------------- the Hollow Wakes, and the Last Ride

	/** The Hollow Wakes: he rides back to the bridge's foot, unhurt, and lifts his lantern high. */
	public void hollowWakes(ServerLevel level) {
		endAttack(level);
		staggerTicks = 0;
		snuffTicks = -1;
		snuffing.clear();
		phase = Phase.HOLLOW_WAKES;
		phaseTicks = 0;
		setAction(Action.IDLE);
		riding = true;
		rideFrom = position();
		rideTicks = 0;
		noPhysics = true;
		bossBar.setName(barName());
		level.playSound(null, getX(), getY(), getZ(), SoundEvents.HORSE_GALLOP, SoundSource.HOSTILE, 3.0F, 0.8F);
	}

	private void tickRide(ServerLevel level) {
		rideTicks++;
		double share = Math.min(1.0, rideTicks / (double) RIDE_BACK_TICKS);
		Vec3 to = waiting();
		Vec3 along = rideFrom.add(to.subtract(rideFrom).scale(share));
		setDeltaMovement(Vec3.ZERO);
		setPos(along.x, along.y, along.z);
		face(to.subtract(rideFrom));
		if (share >= 1.0) {
			riding = false;
			noPhysics = false;
			face(ChurchBridge.WAIT_YAW);
			phaseTicks = 0;
			setAction(Action.LIFT);
			level.playSound(null, getX(), getY(), getZ(), SoundEvents.WITCH_CELEBRATE, SoundSource.HOSTILE, 4.0F, 0.4F);
			tell(level, "message.jugcraft.hessian_horseman.hollow_wakes");
		}
	}

	/** The Hollow Wakes, a tick on: his lantern held high; when it is over every brazier gutters, two go dark, and he charges. */
	private void hollowWaking(ServerLevel level) {
		if (riding) {
			return;
		}
		setDeltaMovement(0.0, -0.3, 0.0);
		if (phaseTicks % 4 == 0) {
			level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, getX(), getY() + 3.4, getZ(), 4, 0.3, 0.3, 0.3, 0.03);
			for (Brazier brazier : Brazier.values()) {
				Vec3 at = Vec3.atBottomCenterOf(brazier.pos(origin));
				level.sendParticles(ParticleTypes.SMOKE, at.x, at.y + 1.0, at.z, 2, 0.2, 0.2, 0.2, 0.01);
			}
		}
		if (phaseTicks >= WAKES_TICKS) {
			phase = Phase.CHARGE;
			phaseTicks = 0;
			snuffIn = Math.min(snuffIn, CHARGE_EVERY);
			globalCooldown = GLOBAL_COOLDOWN;
			setAction(Action.IDLE);
			Player target = target(level);
			Vec3 toward = target != null ? target.position() : centre();
			snuff(level, ChurchBridge.run(toward.x - origin.getX(), toward.z - origin.getZ(), WAKES_SNUFFED));
			bossBar.setName(barName());
		}
	}

	/** The Last Ride: the dark closes in faster, three braziers at a time. */
	public void lastRide(ServerLevel level) {
		phase = Phase.LAST;
		phaseTicks = 0;
		snuffIn = Math.min(snuffIn, LAST_EVERY);
		bossBar.setName(barName());
		tell(level, "message.jugcraft.hessian_horseman.last");
	}

	// ---------------------------------------------------------------- his riders and what he throws

	/** Forgets the faded riders and the landed pumpkins. */
	private void prune(ServerLevel level) {
		riders.removeIf(id -> !(level.getEntity(id) instanceof PhantomRiderEntity rider) || !rider.isAlive());
		thrown.removeIf(id -> !(level.getEntity(id) instanceof HessianPumpkinEntity pumpkin) || !pumpkin.isAlive());
	}

	/** Whoever hurt him, or whose tamed creature did, has taken part. */
	public void took(ServerPlayer player, float amount) {
		encounter.damage(player, amount);
	}

	/** This fight: who took part and how. */
	public Encounter encounter() {
		return encounter;
	}

	/**
	 * His participants still near his hollow: everyone who qualified in his fight ({@link Encounter}) and is alive within
	 * {@value Encounter#REACH} blocks.
	 */
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

	/** Left alone: he rides back to the bridge's foot, healed; his riders fade, his braziers burn again, and who hurt him is forgotten. */
	public void reset(ServerLevel level) {
		dismiss(level);
		lightAll(level);
		phase = Phase.WAITING;
		phaseTicks = 0;
		attack = null;
		abandoned = 0;
		staggerTicks = 0;
		revealedTicks = 0;
		foe = null;
		riding = false;
		noPhysics = false;
		snuffIn = SNUFF_EVERY;
		encounter.abandon();
		Arrays.fill(cooldowns, 0);
		setHealth(getMaxHealth());
		setHeld(Held.NONE);
		setLantern(Lantern.DARK);
		Vec3 waiting = waiting();
		teleportTo(waiting.x, waiting.y, waiting.z);
		face(ChurchBridge.WAIT_YAW);
		setAction(Action.WAIT);
		bossBar.setName(Component.translatable("entity.jugcraft.hessian_horseman"));
		bossBar.setVisible(false);
		tell(level, "message.jugcraft.hessian_horseman.reset");
	}

	/** His riders fade, and whatever he has thrown burns out. */
	private void dismiss(ServerLevel level) {
		for (UUID id : List.copyOf(riders)) {
			if (level.getEntity(id) instanceof PhantomRiderEntity rider) {
				rider.fade(level);
			}
		}
		riders.clear();
		for (UUID id : List.copyOf(thrown)) {
			if (level.getEntity(id) instanceof HessianPumpkinEntity pumpkin) {
				pumpkin.discard();
			}
		}
		thrown.clear();
	}

	@Override
	public void die(DamageSource source) {
		super.die(source);
		if (level() instanceof ServerLevel level) {
			// A killing blow gets here inside super.hurtServer, before hurtServer's own took(): its striker (or the owner of
			// the tamed creature that struck) has taken part before his fall is paid. Any amount qualifies.
			if (Encounter.striker(source) instanceof ServerPlayer player) {
				took(player, Float.MIN_VALUE);
			}
			defeated(level);
		}
	}

	/**
	 * He has fallen: his riders fade, his braziers burn again, each participant is paid, his lair's instance ends (it closes
	 * once everyone has left), and Grey Mist opens on the bridge.
	 */
	private void defeated(ServerLevel level) {
		// However often his fall is reported, it is answered once: one reward each (Concordance Chunk A).
		if (!encounter.claim()) {
			return;
		}
		dismiss(level);
		lightAll(level);
		LairRewards.pay(level, HorsemanLoot.BOSS, encounter, position(), centre(), EXPERIENCE);
		setLantern(Lantern.DARK);
		level.playSound(null, getX(), getY(), getZ(), sound("block.fire.extinguish", SoundEvents.FIRECHARGE_USE), SoundSource.HOSTILE, 4.0F, 0.5F);
		level.playSound(null, getX(), getY(), getZ(), SoundEvents.SKELETON_HORSE_AMBIENT, SoundSource.HOSTILE, 3.0F, 0.4F);
		LairInstance open = lairInstance(level);
		if (open != null) {
			Lairs.end(open);
			BlockState mist = JugcraftLairs.LAIR_EXIT.defaultBlockState().setValue(LairExitBlock.AXIS, Direction.Axis.X);
			for (BlockPos cell : ChurchBridge.bridgeExitCells()) {
				level.setBlock(cell.offset(origin), mist, Block.UPDATE_ALL);
			}
		}
		bossBar.setVisible(false);
		tell(level, "message.jugcraft.hessian_horseman.defeated");
	}

	/** Tells everyone near his hollow. */
	void tell(ServerLevel level, String key, Object... args) {
		Vec3 centre = centre();
		for (ServerPlayer player : level.players()) {
			if (player.position().distanceTo(centre) <= 64.0) {
				player.sendSystemMessage(Component.translatable(key, args));
			}
		}
	}

	// ---------------------------------------------------------------- what he is immune to, and the dark he hides in

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
		if (phase == Phase.WAKING || phase == Phase.HOLLOW_WAKES) {
			return false;
		}
		float dealt = amount * exposure();
		boolean hurt = super.hurtServer(level, source, dealt);
		if (hurt && Encounter.striker(source) instanceof ServerPlayer player) {
			took(player, dealt);
		}
		return hurt;
	}

	@Override
	public boolean canBeAffected(MobEffectInstance effect) {
		return !effect.is(MobEffects.WITHER) && !effect.is(MobEffects.POISON) && super.canBeAffected(effect);
	}

	@Override
	public boolean canBreatheUnderwater() {
		return true;
	}

	@Override
	public boolean causeFallDamage(double distance, float multiplier, DamageSource source) {
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
		return phase == Phase.WAITING ? null : SoundEvents.SKELETON_HORSE_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return sound("entity.skeleton_horse.hurt", SoundEvents.SKELETON_HORSE_AMBIENT);
	}

	@Override
	protected SoundEvent getDeathSound() {
		return sound("entity.skeleton_horse.death", SoundEvents.SKELETON_HORSE_AMBIENT);
	}

	@Override
	public float getVoicePitch() {
		return 0.6F;
	}

	// ---------------------------------------------------------------- saving and syncing

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(ACTION, Action.WAIT.ordinal());
		builder.define(LANTERN, Lantern.DARK.ordinal());
		builder.define(HELD, Held.NONE.ordinal());
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
		origin = input.read("origin", BlockPos.CODEC).orElse(BlockPos.containing(getX() - ChurchBridge.WAIT_X, getY() - ChurchBridge.WAIT_Y,
				getZ() - ChurchBridge.WAIT_Z));
		try {
			phase = Phase.valueOf(input.getStringOr("phase", Phase.WAITING.name()));
		} catch (IllegalArgumentException e) {
			phase = Phase.WAITING;
		}
		if (phase == Phase.WAKING || phase == Phase.HOLLOW_WAKES) {
			// A passing moment is not resumed: he fights on.
			phase = phase == Phase.WAKING ? Phase.RIDE : Phase.CHARGE;
		}
		// His snuffing, his attacks and his riders are not saved: loaded fighting, his braziers burn again.
		lightOnLoad = phase != Phase.WAITING;
		entityData.set(LANTERN, (phase == Phase.WAITING ? Lantern.DARK : Lantern.LIT).ordinal());
		entityData.set(HELD, Held.NONE.ordinal());
		entityData.set(ACTION, (phase == Phase.WAITING ? Action.WAIT : Action.IDLE).ordinal());
		bossBar.setVisible(phase != Phase.WAITING);
		bossBar.setName(barName());
	}

	// ---------------------------------------------------------------- his animations

	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
		controllers.add(new AnimationController<HessianHorsemanEntity>("body", 3, test -> {
			HessianHorsemanEntity horseman = test.animatable();
			if (horseman.isDeadOrDying()) {
				return test.setAndContinue(DEATH);
			}
			Action action = horseman.action();
			return test.setAndContinue(action == Action.IDLE && test.isMoving() ? WALK : BODY[action.ordinal()]);
		}));
		controllers.add(new AnimationController<HessianHorsemanEntity>("lantern", 2, test -> test.setAndContinue(
				test.animatable().isDeadOrDying() ? LANTERNS[Lantern.DARK.ordinal()] : LANTERNS[test.animatable().lantern().ordinal()])));
		controllers.add(new AnimationController<HessianHorsemanEntity>("held", 0,
				test -> test.setAndContinue(HELDS[test.animatable().held().ordinal()])));
	}

	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache() {
		return cache;
	}
}

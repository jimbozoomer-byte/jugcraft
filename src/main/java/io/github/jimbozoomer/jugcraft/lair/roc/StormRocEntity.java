package io.github.jimbozoomer.jugcraft.lair.roc;

import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.LoopType;
import com.geckolib.util.GeckoLibUtil;
import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.lair.CrownOfThunder;
import io.github.jimbozoomer.jugcraft.lair.CrownOfThunder.Rod;
import io.github.jimbozoomer.jugcraft.lair.JugcraftLairs;
import io.github.jimbozoomer.jugcraft.lair.Lair;
import io.github.jimbozoomer.jugcraft.lair.LairBosses;
import io.github.jimbozoomer.jugcraft.lair.LairExitBlock;
import io.github.jimbozoomer.jugcraft.lair.LairInstance;
import io.github.jimbozoomer.jugcraft.lair.Lairs;
import io.github.jimbozoomer.jugcraft.lair.dreadnought.GroundingStrap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
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
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LightningBolt;
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * The Storm Roc (docs/features/storm-roc.md): the Crown of Thunder's boss, an eagle as big as a cart, slate-grey with a
 * storm-blue sheen, his flight feathers tipped white like lightning, a hooked beak of dark horn and eyes that burn
 * electric blue. He perches in his nest on the crag north of the summit, and wakes when someone who may fight him steps
 * onto the summit or strikes him: he rises screeching as lightning splits the sky and glides down to the summit, unhurt,
 * his health scaled for the party. He keeps to the summit, and no flight of his lasts longer than {@value #FLIGHT_MAX}
 * ticks before he lands.
 *
 * <p>His rule: the Storm Rods. Every {@link #diveEvery()} ticks he dives. For {@value #CHARGE_TICKS} ticks a Storm Rod
 * charges (the bar names it) while he climbs over the summit's edge; for {@value #LANE_TICKS} a line of sparks runs from him
 * through his marked foe and follows them; then it holds still and he dives down it: {@value #DIVE_DAMAGE}, and a shove
 * aside, to everyone within {@value #DIVE_WIDTH} of his path. If his lane passes within {@value #GROUND_RADIUS} of the
 * charged rod, the rod grounds him as he passes it: he crashes there and lies stunned for {@value #GROUNDED_TICKS}
 * ticks, every blow doing {@value #GROUNDED_MULTIPLIER} times as much, and cannot fly.
 *
 * <p>The Gathering Storm: the Wing Buffet and the Lightning Marks. At {@value #SQUALL_AT} of his health the Squall Rises:
 * he climbs and circles screaming, unhurt, and his fledglings leave the nest. The Squall: Wind Strips, more fledglings,
 * and dives oftener. Below {@value #EYE_AT}, the Eye of the Storm: oftener still, and five Lightning Marks.
 *
 * <p>Every attack has a wind-up you can read, a strike and a recovery ({@link Attack}); the numbers are here, in one place,
 * and in tools/storm_roc.py. Lightning, electric arcs and falls never harm him. Each participant (whoever hurt him or his
 * fledglings, or stood by the rod that grounded him) gets their own loot ({@link RocLoot}). Left alone he flies back to his
 * nest, healed: his fledglings return to it, the rods go quiet, the wind drops, and who hurt him is forgotten. Bound to a
 * lair instance he vanishes with it; one with no instance (a test's) keeps to the summit laid out from the origin it was
 * given. The only blocks he changes are the Storm Rods' charge, and the Grey Mist he opens at the foot of his crag when he
 * falls.
 */
public class StormRocEntity extends Monster implements GeoEntity {
	// ---------------------------------------------------------------- numbers (tools/storm_roc.py)
	public static final float HEALTH = 400.0F;
	public static final double ARMOR = 8.0;
	public static final double LEASH = 30.0;
	public static final double PARTY_STEP = 0.5;
	public static final double PARTY_MAX = 2.5;
	public static final int WAKE_TICKS = 50;
	public static final int ABANDON_TICKS = 200;
	public static final int GLOBAL_COOLDOWN = 20;
	public static final double SPEED = 0.16;
	public static final double KEEP_DISTANCE = 2.5;
	public static final double STEP = 1.0;
	public static final int FLIGHT_MAX = 120;
	public static final float SQUALL_AT = 0.5F;
	public static final float EYE_AT = 0.2F;
	public static final int SQUALL_TICKS = 60;
	public static final double SQUALL_HEIGHT = 9.0;
	public static final double SQUALL_CIRCLE = 8.0;
	public static final int DIVE_FIRST = 200;
	public static final int DIVE_EVERY = 320;
	public static final int DIVE_EVERY_SQUALL = 280;
	public static final int DIVE_EVERY_EYE = 240;
	public static final int CHARGE_TICKS = 40;
	public static final int LANE_TICKS = 40;
	public static final double PERCH_OUT = 20.0;
	public static final double PERCH_UP = 8.0;
	public static final double PERCH_SPREAD = 0.0;
	public static final double DIVE_SPEED = 1.25;
	public static final double DIVE_OVERSHOOT = 5.0;
	public static final float DIVE_DAMAGE = 12.0F;
	public static final double DIVE_WIDTH = 1.5;
	public static final double DIVE_SHOVE = 1.0;
	public static final double GROUND_RADIUS = 2.5;
	public static final int GROUNDED_TICKS = 120;
	public static final float GROUNDED_MULTIPLIER = 1.5F;
	/** The sprint his rule is fair to (blocks a second): the audit walks the summit at it. */
	public static final double SPRINT = 5.6;
	public static final float BUFFET_DAMAGE = 7.0F;
	public static final double BUFFET_DEPTH = 6.0;
	public static final double BUFFET_ANGLE = 50.0;
	public static final double BUFFET_THROW = 1.1;
	public static final int MARKS = 3;
	public static final int EYE_MARKS = 5;
	public static final double MARK_RADIUS = 1.5;
	public static final double MARK_APART = 6.0;
	public static final float MARK_DAMAGE = 8.0F;
	public static final int STRIPS = 2;
	public static final double STRIP_WIDTH = 3.0;
	public static final double WIND_PUSH = 0.08;
	public static final double LEE = 3.0;
	public static final int FLEDGLING_CAP = 3;
	public static final int EXPERIENCE = 300;
	/** How far in from the summit's edge he lands, and looks for a place to stand. */
	private static final double LAND_IN = 2.0;
	/** How far from a charged rod's middle he lies once it grounds him (clear of its plinth). */
	private static final double GROUND_CLEAR = 2.8;
	private static final DustParticleOptions STORM_BLUE = new DustParticleOptions(0x5A9AFF, 1.2F);
	private static final DustParticleOptions WIND_WHITE = new DustParticleOptions(0xDCEBFF, 1.0F);

	/**
	 * His attacks: wind-up, the active part and recovery in ticks, the cooldown, the target's nearest and farthest
	 * distance, and the phases he uses it in (the Gathering Storm; the Squall and the Eye of the Storm). The wind-up is what
	 * players read; the strike lands as it ends. The Storm Dive is his rule, on its own clock.
	 */
	public enum Attack {
		WING_BUFFET(20, 2, 14, 80, 0.0, 5.5, true, true),
		LIGHTNING_MARKS(30, 1, 16, 160, 0.0, 40.0, true, true),
		WIND_STRIPS(30, 80, 10, 300, 0.0, 40.0, false, true),
		FLEDGLING_CALL(20, 1, 10, 600, 0.0, 40.0, false, true);

		public final int windup;
		public final int active;
		public final int recovery;
		public final int cooldown;
		public final double near;
		public final double far;
		public final boolean gathering;
		public final boolean squall;

		Attack(int windup, int active, int recovery, int cooldown, double near, double far, boolean gathering, boolean squall) {
			this.windup = windup;
			this.active = active;
			this.recovery = recovery;
			this.cooldown = cooldown;
			this.near = near;
			this.far = far;
			this.gathering = gathering;
			this.squall = squall;
		}

		public int length() {
			return windup + active + recovery;
		}
	}

	/** The fight's phases: waiting in the nest, waking, the Gathering Storm, the Squall Rising, the Squall, the Eye. */
	public enum Phase {
		WAITING, WAKING, GATHERING, RISING, SQUALL, EYE
	}

	/** Where his dive stands: none under way, a rod charging, his lane marked, diving, or grounded by the rod. */
	public enum Dive {
		NONE, CHARGE, LANE, DIVING, GROUNDED
	}

	/** What his body is doing, synced for its animations. */
	public enum Action {
		PERCH(false), WAKE(true), IDLE(false), BUFFET_WINDUP(true), BUFFET(true), SCREECH(true), STRIKE(true), CALL(false),
		STRIPS_WINDUP(true), STRIPS(true), FLY(true), HOVER(true), DIVE(true), GROUNDED(true), CIRCLE(true);

		/** Whether his wings are spread for it. */
		public final boolean spread;

		Action(boolean spread) {
			this.spread = spread;
		}
	}

	private static final EntityDataAccessor<Integer> ACTION = SynchedEntityData.defineId(StormRocEntity.class, EntityDataSerializers.INT);
	static final RawAnimation[] BODY = new RawAnimation[Action.values().length];
	static final RawAnimation WALK = RawAnimation.begin().thenLoop("animation.storm_roc.walk");
	static final RawAnimation DEATH = RawAnimation.begin().thenPlayAndHold("animation.storm_roc.death");
	static final RawAnimation WINGS_FOLDED = RawAnimation.begin().thenLoop("animation.storm_roc.wings_folded");
	static final RawAnimation WINGS_SPREAD = RawAnimation.begin().thenLoop("animation.storm_roc.wings_spread");

	static {
		String prefix = "animation.storm_roc.";
		BODY[Action.PERCH.ordinal()] = RawAnimation.begin().thenLoop(prefix + "perch");
		BODY[Action.WAKE.ordinal()] = RawAnimation.begin().thenPlayAndHold(prefix + "wake");
		BODY[Action.IDLE.ordinal()] = RawAnimation.begin().thenLoop(prefix + "idle");
		BODY[Action.BUFFET_WINDUP.ordinal()] = RawAnimation.begin().thenPlayAndHold(prefix + "buffet_windup");
		BODY[Action.BUFFET.ordinal()] = RawAnimation.begin().then(prefix + "buffet", LoopType.PLAY_ONCE).thenLoop(prefix + "idle");
		BODY[Action.SCREECH.ordinal()] = RawAnimation.begin().thenPlayAndHold(prefix + "screech");
		BODY[Action.STRIKE.ordinal()] = RawAnimation.begin().then(prefix + "strike", LoopType.PLAY_ONCE).thenLoop(prefix + "idle");
		BODY[Action.CALL.ordinal()] = RawAnimation.begin().thenPlayAndHold(prefix + "call");
		BODY[Action.STRIPS_WINDUP.ordinal()] = RawAnimation.begin().thenPlayAndHold(prefix + "strips_windup");
		BODY[Action.STRIPS.ordinal()] = RawAnimation.begin().thenLoop(prefix + "strips");
		BODY[Action.FLY.ordinal()] = RawAnimation.begin().thenLoop(prefix + "fly");
		BODY[Action.HOVER.ordinal()] = RawAnimation.begin().thenLoop(prefix + "hover");
		BODY[Action.DIVE.ordinal()] = RawAnimation.begin().thenLoop(prefix + "dive");
		BODY[Action.GROUNDED.ordinal()] = RawAnimation.begin().thenLoop(prefix + "grounded");
		BODY[Action.CIRCLE.ordinal()] = RawAnimation.begin().thenLoop(prefix + "circle");
	}

	private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
	private final ServerBossEvent bossBar = new ServerBossEvent(UUID.randomUUID(), Component.translatable("entity.jugcraft.storm_roc"),
			BossEvent.BossBarColor.BLUE, BossEvent.BossBarOverlay.NOTCHED_10);
	/** The lair instance he belongs to (null for one put down by a test or a command), and the Crown's origin. */
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
	private final Map<UUID, Float> damageBy = new HashMap<>();
	/** Players who stood by the rod that grounded him: they take part too. */
	private final Set<UUID> helpers = new HashSet<>();
	private final List<UUID> fledglings = new ArrayList<>();
	// His rule: the dive under way.
	private Dive dive = Dive.NONE;
	private int diveTicks;
	private int diveTimer = DIVE_FIRST;
	private @Nullable Rod charged;
	private @Nullable Rod lastCharged;
	private Vec3 perch = Vec3.ZERO;
	private Vec3 flightFrom = Vec3.ZERO;
	/** The lane: from the perch, through where his foe stood when it held (on the floor), on to where he lands. */
	private Vec3 laneTo = Vec3.ZERO;
	private Vec3 laneEnd = Vec3.ZERO;
	/** How far along the lane (flat) he has come, and where (flat) the charged rod grounds him, or -1 if it will not. */
	private double along;
	private double groundAt = -1.0;
	private final Set<UUID> diveHit = new HashSet<>();
	// The attacks under way.
	private final List<Vec3> marks = new ArrayList<>();
	private Vec3 wind = new Vec3(1.0, 0.0, 0.0);
	private final List<Vec3> strips = new ArrayList<>();
	private Vec3 circleFrom = Vec3.ZERO;
	/** Set when he is loaded in a passing moment (waking, rising, in flight): he lands on the summit and fights on. */
	private boolean landOnLoad;

	public StormRocEntity(EntityType<? extends StormRocEntity> type, Level level) {
		super(type, level);
		xpReward = 0;
		setPersistenceRequired();
		bossBar.setDarkenScreen(true);
		bossBar.setVisible(false);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, HEALTH).add(Attributes.ARMOR, ARMOR)
				.add(Attributes.KNOCKBACK_RESISTANCE, 0.9).add(Attributes.FOLLOW_RANGE, 48.0).add(Attributes.MOVEMENT_SPEED, 0.3)
				.add(Attributes.ATTACK_DAMAGE, DIVE_DAMAGE).add(Attributes.STEP_HEIGHT, STEP);
	}

	/**
	 * Sets him in the nest of {@code instance}'s Crown. Called as the instance is placed. Placing the template puts back
	 * only its own blocks, so the Grey Mist a fallen Roc opened at the foot of his crag before still stands: it goes first.
	 */
	public static StormRocEntity summon(ServerLevel level, LairInstance instance) {
		BlockPos origin = instance.origin();
		for (BlockPos cell : CrownOfThunder.fallExitCells()) {
			BlockPos at = origin.offset(cell);
			if (level.getBlockState(at).is(JugcraftLairs.LAIR_EXIT)) {
				level.setBlock(at, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
			}
		}
		return summon(level, origin, instance);
	}

	/** Sets him in the nest of the Crown laid out from {@code origin} (tests pass no instance). */
	public static StormRocEntity summon(ServerLevel level, BlockPos origin, @Nullable LairInstance instance) {
		StormRocEntity roc = new StormRocEntity(JugcraftRoc.STORM_ROC, level);
		roc.origin = origin.immutable();
		if (instance != null) {
			roc.instance = instance.id;
			roc.slot = instance.slot;
		}
		Vec3 waiting = roc.waiting();
		roc.snapTo(waiting.x, waiting.y, waiting.z, 0.0F, 0.0F);
		roc.face(0.0F);
		roc.noPhysics = true;
		roc.setNoGravity(true);
		level.addFreshEntity(roc);
		return roc;
	}

	// ---------------------------------------------------------------- where things are

	/** The summit's middle, on its floor, for the Crown laid out from {@code origin}. */
	public static Vec3 centre(BlockPos origin) {
		return CrownOfThunder.centre().add(origin.getX(), origin.getY(), origin.getZ());
	}

	public Vec3 centre() {
		return centre(origin);
	}

	public BlockPos origin() {
		return origin;
	}

	/** Where he waits in his nest. */
	public Vec3 waiting() {
		return CrownOfThunder.waiting().add(origin.getX(), origin.getY(), origin.getZ());
	}

	/** Where he comes down on the summit as he wakes. */
	public Vec3 woke() {
		return new Vec3(origin.getX() + CrownOfThunder.WOKE_X, floorY(), origin.getZ() + CrownOfThunder.WOKE_Z);
	}

	/** The summit's floor, where he and his foes stand. */
	public double floorY() {
		return origin.getY() + CrownOfThunder.STAND;
	}

	/** The middle of {@code rod} (flat, on the floor). */
	public Vec3 rod(Rod rod) {
		return new Vec3(origin.getX() + rod.x(), floorY(), origin.getZ() + rod.z());
	}

	/** Whether {@code at} (a world position) is over the summit. */
	public boolean onSummit(Vec3 at) {
		return CrownOfThunder.onSummit(at.x - origin.getX(), at.z - origin.getZ());
	}

	/** {@code at} on the floor, drawn in toward the summit's middle until it lies {@code in} inside its edge. */
	public Vec3 inside(Vec3 at, double in) {
		Vec3 centre = centre();
		Vec3 flat = new Vec3(at.x - centre.x, 0.0, at.z - centre.z);
		double most = CrownOfThunder.SUMMIT - in;
		if (flat.length() > most) {
			flat = flat.scale(most / flat.length());
		}
		return new Vec3(centre.x + flat.x, floorY(), centre.z + flat.z);
	}

	// ---------------------------------------------------------------- state

	public Phase phase() {
		return phase;
	}

	public Dive dive() {
		return dive;
	}

	public @Nullable Rod charged() {
		return charged;
	}

	public @Nullable Attack attack() {
		return attack;
	}

	public int attackTicks() {
		return attackTicks;
	}

	public int diveTimer() {
		return diveTimer;
	}

	public Vec3 perch() {
		return perch;
	}

	public Vec3 laneEnd() {
		return laneEnd;
	}

	public double groundAt() {
		return groundAt;
	}

	public List<Vec3> marks() {
		return List.copyOf(marks);
	}

	public List<Vec3> strips() {
		return List.copyOf(strips);
	}

	public Vec3 wind() {
		return wind;
	}

	public List<UUID> fledglings() {
		return List.copyOf(fledglings);
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

	/** Whether he is fighting (not waiting, waking or rising into the Squall). */
	public boolean fighting() {
		return phase == Phase.GATHERING || phase == Phase.SQUALL || phase == Phase.EYE;
	}

	/** Whether he is in the air: waking, rising into the Squall, or on the wing for a dive. */
	public boolean flying() {
		return phase == Phase.WAKING || phase == Phase.RISING || dive == Dive.CHARGE || dive == Dive.LANE || dive == Dive.DIVING;
	}

	/** Damage he deals: the attack's own, times {@code lairs.boss_damage} ({@link LairBosses}). */
	public static float damage(float base) {
		return LairBosses.damage(base);
	}

	/** His health for a party of {@code players}: half as much again for each beyond the first, at most 2.5 times. */
	public static double partyScale(int players) {
		return LairBosses.partyScale(players, PARTY_STEP, PARTY_MAX);
	}

	/** How long between his dives in this phase. */
	public int diveEvery() {
		return switch (phase) {
			case SQUALL, RISING -> DIVE_EVERY_SQUALL;
			case EYE -> DIVE_EVERY_EYE;
			default -> DIVE_EVERY;
		};
	}

	/** How many Lightning Marks he lays in this phase. */
	public int markCount() {
		return phase == Phase.EYE ? EYE_MARKS : MARKS;
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
			Vec3 woke = woke();
			teleportTo(woke.x, woke.y, woke.z);
			walking();
		}
		resetFallDistance();
		bossBar.setProgress(getHealth() / getMaxHealth());
		prune(level);
		for (int i = 0; i < cooldowns.length; i++) {
			if (cooldowns[i] > 0) {
				cooldowns[i]--;
			}
		}
		phaseTicks++;
		switch (phase) {
			case WAITING -> waiting(level);
			case WAKING -> waking(level);
			case RISING -> rising(level);
			case GATHERING, SQUALL, EYE -> fight(level);
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

	/** On foot: he walks and falls as anything does. */
	private void walking() {
		noPhysics = false;
		setNoGravity(false);
	}

	/** On the wing: he goes where he is put. */
	private void airborne() {
		noPhysics = true;
		setNoGravity(true);
		setDeltaMovement(Vec3.ZERO);
	}

	private void waiting(ServerLevel level) {
		airborne();
		Vec3 waiting = waiting();
		if (position().distanceToSqr(waiting) > 0.01) {
			setPos(waiting.x, waiting.y, waiting.z);
		}
		face(0.0F);
		setAction(Action.PERCH);
		if (tickCount % 10 == 0 && intruder(level) != null) {
			wake(level);
		}
	}

	/** Someone who may fight him, standing on the summit. */
	private @Nullable Player intruder(ServerLevel level) {
		for (Player player : level.players()) {
			if (LairBosses.eligible(player) && onSummit(player.position()) && player.getY() > floorY() - 1.0 && player.getY() < floorY() + 6.0) {
				return player;
			}
		}
		return null;
	}

	/** He wakes: his health scaled for the players near the summit, his bar shown, lightning splitting the sky. */
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
		flightFrom = position();
		setAction(Action.WAKE);
		bossBar.setVisible(true);
		level.playSound(null, getX(), getY(), getZ(), cry(), SoundSource.HOSTILE, 4.0F, 0.5F);
		bolt(level, waiting().add(5.0, -4.0, 6.0));
		tell(level, "message.jugcraft.storm_roc.wakes");
	}

	/** Waking: he rises off the nest screeching, then glides down to the summit in an arc. */
	private void waking(ServerLevel level) {
		airborne();
		double share = Math.min(1.0, phaseTicks / (double) WAKE_TICKS);
		Vec3 to = woke();
		Vec3 line = flightFrom.add(to.subtract(flightFrom).scale(share));
		setPos(line.x, line.y + 3.0 * Math.sin(Math.PI * share), line.z);
		face(to.subtract(flightFrom));
		if (phaseTicks % 8 == 0) {
			level.playSound(null, getX(), getY(), getZ(), SoundEvents.ENDER_DRAGON_FLAP, SoundSource.HOSTILE, 1.5F, 1.2F);
		}
		if (phaseTicks >= WAKE_TICKS) {
			Vec3 land = settle(level, to);
			teleportTo(land.x, land.y, land.z);
			walking();
			phase = Phase.GATHERING;
			phaseTicks = 0;
			globalCooldown = GLOBAL_COOLDOWN;
			diveTimer = DIVE_FIRST;
			setAction(Action.IDLE);
		}
	}

	/** Players near enough to the summit to be fighting (or about to). */
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
		if (dive == Dive.NONE || dive == Dive.GROUNDED) {
			keepToTheSummit(level);
		}
		if (phase == Phase.GATHERING && getHealth() <= getMaxHealth() * SQUALL_AT && dive == Dive.NONE && attack == null) {
			squall(level);
			return;
		}
		if (phase == Phase.SQUALL && getHealth() <= getMaxHealth() * EYE_AT) {
			eye(level);
		}
		Player target = target(level);
		if (dive != Dive.NONE) {
			diveTick(level, target);
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
		if (--diveTimer <= 0) {
			dive(level, target, nextRod(), (getRandom().nextDouble() * 2.0 - 1.0) * PERCH_SPREAD);
			return;
		}
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

	/** Strayed farther than his leash from the summit's middle, or fallen below its floor, he is set back on it. */
	private void keepToTheSummit(ServerLevel level) {
		Vec3 centre = centre();
		double dx = getX() - centre.x;
		double dz = getZ() - centre.z;
		if (dx * dx + dz * dz <= LEASH * LEASH && getY() > floorY() - 3.0) {
			return;
		}
		level.sendParticles(ParticleTypes.CLOUD, getX(), getY() + 1.0, getZ(), 20, 0.8, 0.8, 0.8, 0.05);
		breakOff();
		if (dive == Dive.GROUNDED) {
			dive = Dive.NONE;
		}
		Vec3 back = settle(level, woke());
		teleportTo(back.x, back.y, back.z);
		walking();
		level.sendParticles(ParticleTypes.CLOUD, getX(), getY() + 1.0, getZ(), 20, 0.8, 0.8, 0.8, 0.05);
	}

	/** His target: the one he has while they are still near the summit, else the nearest who may fight him. */
	private @Nullable Player target(ServerLevel level) {
		Vec3 centre = centre();
		if (foe != null) {
			Player held = level.getPlayerByUUID(foe);
			if (held != null && LairBosses.eligible(held) && held.position().distanceTo(centre) <= LEASH + 8.0) {
				return held;
			}
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

	/** His foe now, if he has one. */
	public @Nullable Player foe(ServerLevel level) {
		return foe == null ? null : level.getPlayerByUUID(foe);
	}

	/** Stalks toward a point {@value #KEEP_DISTANCE} blocks short of {@code target}. */
	private void approach(Player target) {
		Vec3 to = target.position().subtract(position());
		double flat = Math.sqrt(to.x * to.x + to.z * to.z);
		Vec3 goal = flat > KEEP_DISTANCE + 0.5
				? new Vec3(target.getX() - to.x / flat * KEEP_DISTANCE, getY(), target.getZ() - to.z / flat * KEEP_DISTANCE)
				: position();
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

	/** The way to {@code to}, flat and of length one (or his facing, if {@code to} is straight up). */
	private Vec3 flat(Vec3 to) {
		Vec3 flat = new Vec3(to.x, 0.0, to.z);
		return flat.lengthSqr() < 1.0E-6 ? facing() : flat.normalize();
	}

	/** Picks an attack he can use now on {@code target}, or null. */
	private @Nullable Attack choose(Player target) {
		double distance = target.distanceTo(this);
		List<Attack> ready = new ArrayList<>();
		for (Attack candidate : Attack.values()) {
			boolean phaseAllows = phase == Phase.GATHERING ? candidate.gathering : candidate.squall;
			if (!phaseAllows || cooldowns[candidate.ordinal()] > 0 || distance < candidate.near || distance > candidate.far) {
				continue;
			}
			if (candidate == Attack.FLEDGLING_CALL && fledglings.size() >= FLEDGLING_CAP) {
				continue;
			}
			ready.add(candidate);
			if (candidate == Attack.WING_BUFFET) {
				ready.add(candidate);  // close in, his wings come most often
			}
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
			case WING_BUFFET -> {
				setAction(Action.BUFFET_WINDUP);
				level.playSound(null, getX(), getY(), getZ(), SoundEvents.ENDER_DRAGON_FLAP, SoundSource.HOSTILE, 2.0F, 0.6F);
			}
			case LIGHTNING_MARKS -> {
				setAction(Action.SCREECH);
				markLightning(level, target);
				level.playSound(null, getX(), getY(), getZ(), cry(), SoundSource.HOSTILE, 3.0F, 0.7F);
			}
			case WIND_STRIPS -> {
				setAction(Action.STRIPS_WINDUP);
				markStrips(level, target);
				level.playSound(null, getX(), getY(), getZ(), SoundEvents.BREEZE_IDLE_AIR, SoundSource.HOSTILE, 2.5F, 0.5F);
			}
			case FLEDGLING_CALL -> {
				setAction(Action.CALL);
				level.playSound(null, getX(), getY(), getZ(), cry(), SoundSource.HOSTILE, 3.0F, 0.9F);
				tell(level, "message.jugcraft.storm_roc.fledglings");
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
			case WING_BUFFET -> {
				if (t < current.windup) {
					face(target);
				} else if (t == current.windup) {
					setAction(Action.BUFFET);
					buffet(level);
				}
			}
			case LIGHTNING_MARKS -> {
				if (t < current.windup) {
					if (t % 4 == 0) {
						for (Vec3 mark : marks) {
							ring(level, mark);
						}
					}
				} else if (t == current.windup) {
					setAction(Action.STRIKE);
					strikeMarks(level);
				}
			}
			case WIND_STRIPS -> {
				if (t < current.windup + current.active) {
					if (t % 4 == 0) {
						arrows(level);
					}
					if (t == current.windup) {
						setAction(Action.STRIPS);
						level.playSound(null, getX(), getY(), getZ(), SoundEvents.BREEZE_IDLE_AIR, SoundSource.HOSTILE, 3.0F, 0.4F);
					}
					if (t >= current.windup) {
						blow(level);
					}
				} else if (t == current.windup + current.active) {
					strips.clear();
					setAction(Action.IDLE);
				}
			}
			case FLEDGLING_CALL -> {
				if (t == current.windup) {
					callFledglings(level);
				}
			}
		}
		if (attack == current && attackTicks >= current.length()) {
			attack = null;
			attackTicks = 0;
			globalCooldown = GLOBAL_COOLDOWN;
			marks.clear();
			strips.clear();
			setAction(Action.IDLE);
		}
	}

	/** The attack under way is broken off before its end (his foe gone, set back on the summit, a dive begun). */
	private void breakOff() {
		attack = null;
		attackTicks = 0;
		marks.clear();
		strips.clear();
	}

	/** Sets {@code player} moving at {@code motion}, and tells their client (a player's own motion is theirs to move). */
	static void push(ServerPlayer player, Vec3 motion) {
		player.setDeltaMovement(motion);
		player.connection.send(new ClientboundSetEntityMotionPacket(player));
	}

	/**
	 * Wing Buffet: a gust in a cone before him, {@value #BUFFET_DEPTH} deep and {@value #BUFFET_ANGLE} degrees either side
	 * of where he faces: {@value #BUFFET_DAMAGE}, and thrown back. Returns how many it hurt.
	 */
	public int buffet(ServerLevel level) {
		Vec3 facing = facing();
		int hit = 0;
		for (ServerPlayer player : nearby(level)) {
			Vec3 to = player.position().subtract(position());
			Vec3 flat = new Vec3(to.x, 0.0, to.z);
			double distance = flat.length();
			if (distance > BUFFET_DEPTH + player.getBbWidth() / 2.0 || Math.abs(to.y) > 3.0) {
				continue;
			}
			Vec3 way = distance < 1.0E-3 ? facing : flat.scale(1.0 / distance);
			if (distance > 1.0 && Math.toDegrees(Math.acos(Math.max(-1.0, Math.min(1.0, way.dot(facing))))) > BUFFET_ANGLE) {
				continue;
			}
			if (player.hurtServer(level, damageSources().mobAttack(this), damage(BUFFET_DAMAGE))) {
				hit++;
				push(player, way.scale(BUFFET_THROW).add(0.0, 0.35, 0.0));
			}
		}
		for (int i = 0; i < 16; i++) {
			double a = Math.toRadians((getRandom().nextDouble() * 2.0 - 1.0) * BUFFET_ANGLE);
			Vec3 way = new Vec3(facing.x * Math.cos(a) - facing.z * Math.sin(a), 0.0, facing.x * Math.sin(a) + facing.z * Math.cos(a));
			Vec3 at = position().add(way.scale(1.5)).add(0.0, 1.0, 0.0);
			level.sendParticles(ParticleTypes.CLOUD, at.x, at.y, at.z, 0, way.x, 0.05, way.z, 0.6);
		}
		level.playSound(null, getX(), getY(), getZ(), SoundEvents.ENDER_DRAGON_FLAP, SoundSource.HOSTILE, 3.0F, 0.5F);
		return hit;
	}

	// ---------------------------------------------------------------- Lightning Marks

	/**
	 * The Lightning Marks: {@link #markCount()} rings, at least {@value #MARK_APART} apart: the first under his foe, the rest
	 * under the other players near the summit, then on open summit.
	 */
	public void markLightning(ServerLevel level, Player target) {
		marks.clear();
		int count = markCount();
		addMark(new Vec3(target.getX(), floorY(), target.getZ()));
		for (ServerPlayer player : nearby(level)) {
			if (marks.size() >= count) {
				break;
			}
			if (onSummit(player.position())) {
				addMark(new Vec3(player.getX(), floorY(), player.getZ()));
			}
		}
		Vec3 centre = centre();
		for (int tries = 0; tries < 60 && marks.size() < count; tries++) {
			double a = getRandom().nextDouble() * Math.PI * 2.0;
			double r = Math.sqrt(getRandom().nextDouble()) * (CrownOfThunder.SUMMIT - LAND_IN);
			addMark(new Vec3(centre.x + Math.cos(a) * r, floorY(), centre.z + Math.sin(a) * r));
		}
	}

	private void addMark(Vec3 at) {
		for (Vec3 mark : marks) {
			if (Math.hypot(mark.x - at.x, mark.z - at.z) < MARK_APART) {
				return;
			}
		}
		marks.add(at);
	}

	/** Sets the Lightning Marks, as their wind-up would (for tests). */
	public void marks(List<Vec3> at) {
		marks.clear();
		marks.addAll(at);
	}

	/** A crackling ring on the floor at {@code mark}. */
	private void ring(ServerLevel level, Vec3 mark) {
		for (int i = 0; i < 10; i++) {
			double a = Math.PI * 2.0 * i / 10;
			level.sendParticles(STORM_BLUE, mark.x + Math.cos(a) * MARK_RADIUS, mark.y + 0.1, mark.z + Math.sin(a) * MARK_RADIUS, 1, 0.0, 0.0, 0.0, 0.0);
		}
		level.sendParticles(ParticleTypes.ELECTRIC_SPARK, mark.x, mark.y + 0.2, mark.z, 2, 0.5, 0.05, 0.5, 0.05);
	}

	/**
	 * The bolts strike the marks: {@value #MARK_DAMAGE} of electric harm (which the Grounding Strap halves) to each player
	 * within {@value #MARK_RADIUS} of one (once). Returns how many it hurt.
	 */
	public int strikeMarks(ServerLevel level) {
		Set<UUID> struck = new HashSet<>();
		for (Vec3 mark : marks) {
			bolt(level, mark);
			for (ServerPlayer player : nearby(level)) {
				double flat = Math.hypot(player.getX() - mark.x, player.getZ() - mark.z);
				if (struck.contains(player.getUUID()) || flat > MARK_RADIUS + player.getBbWidth() / 2.0 || Math.abs(player.getY() - mark.y) > 2.5) {
					continue;
				}
				if (player.hurtServer(level, GroundingStrap.arc(level, this, this), damage(MARK_DAMAGE))) {
					struck.add(player.getUUID());
				}
			}
		}
		return struck.size();
	}

	/** Lightning that only shows: a flash and a thunderclap at {@code at}, hurting nobody, lighting nothing. */
	private static void bolt(ServerLevel level, Vec3 at) {
		LightningBolt bolt = EntityTypes.LIGHTNING_BOLT.create(level, EntitySpawnReason.TRIGGERED);
		if (bolt != null) {
			bolt.setVisualOnly(true);
			bolt.snapTo(at);
			level.addFreshEntity(bolt);
		}
	}

	// ---------------------------------------------------------------- Wind Strips

	/**
	 * The Wind Strips: the wind's way, and {@value #STRIPS} strips {@value #STRIP_WIDTH} wide along it, the first through his
	 * foe and the next through another player clear of it, or beside it.
	 */
	public void markStrips(ServerLevel level, Player target) {
		double a = getRandom().nextDouble() * Math.PI * 2.0;
		Vec3 way = new Vec3(Math.cos(a), 0.0, Math.sin(a));
		List<Vec3> through = new ArrayList<>();
		through.add(target.position());
		Vec3 across = new Vec3(-way.z, 0.0, way.x);
		for (ServerPlayer player : nearby(level)) {
			if (through.size() >= STRIPS) {
				break;
			}
			if (Math.abs(player.position().subtract(target.position()).dot(across)) >= STRIP_WIDTH * 2.0) {
				through.add(player.position());
			}
		}
		while (through.size() < STRIPS) {
			double side = (getRandom().nextBoolean() ? 1.0 : -1.0) * (STRIP_WIDTH * 2.0 + getRandom().nextDouble() * 4.0);
			through.add(target.position().add(across.scale(side * through.size())));
		}
		strips(way, through);
	}

	/** Sets the wind's way and the strips' lines, as their wind-up would (for tests). */
	public void strips(Vec3 way, List<Vec3> through) {
		wind = flat(way);
		strips.clear();
		for (Vec3 at : through) {
			strips.add(new Vec3(at.x, floorY(), at.z));
		}
	}

	/** Whether (x, z) lies in a strip, {@code margin} counted besides its half width. */
	public boolean inStrip(double x, double z, double margin) {
		Vec3 across = new Vec3(-wind.z, 0.0, wind.x);
		for (Vec3 line : strips) {
			if (Math.abs((x - line.x) * across.x + (z - line.z) * across.z) <= STRIP_WIDTH / 2.0 + margin) {
				return true;
			}
		}
		return false;
	}

	/** The arrows on the floor along each strip, pointing the way the wind will blow, a chevron every three blocks. */
	private void arrows(ServerLevel level) {
		Vec3 across = new Vec3(-wind.z, 0.0, wind.x);
		for (Vec3 line : strips) {
			for (double s = -CrownOfThunder.SUMMIT * 2.0; s <= CrownOfThunder.SUMMIT * 2.0; s += 3.0) {
				Vec3 at = line.add(wind.scale(s));
				if (!onSummit(at)) {
					continue;
				}
				for (double k = -0.6; k <= 0.61; k += 0.3) {
					Vec3 point = at.add(wind.scale(0.5 - Math.abs(k))).add(across.scale(k));
					level.sendParticles(WIND_WHITE, point.x, floorY() + 0.1, point.z, 1, 0.0, 0.0, 0.0, 0.0);
				}
			}
		}
	}

	/**
	 * One tick of the wind along the strips: everyone in one, and not sheltered (rock within {@value #LEE} of them upwind:
	 * a boulder), is pushed {@value #WIND_PUSH} the way its arrows point. Returns how many it pushed.
	 */
	public int blow(ServerLevel level) {
		int pushed = 0;
		for (ServerPlayer player : nearby(level)) {
			if (!inStrip(player.getX(), player.getZ(), player.getBbWidth() / 2.0) || player.getY() > floorY() + 4.0 || sheltered(level, player, wind)) {
				continue;
			}
			push(player, player.getDeltaMovement().add(wind.scale(WIND_PUSH)));
			pushed++;
		}
		if (attackTicks % 2 == 0) {
			for (Vec3 line : strips) {
				double s = (getRandom().nextDouble() * 2.0 - 1.0) * CrownOfThunder.SUMMIT;
				Vec3 at = line.add(wind.scale(s)).add(0.0, 0.4 + getRandom().nextDouble() * 1.5, 0.0);
				level.sendParticles(ParticleTypes.CLOUD, at.x, at.y, at.z, 0, wind.x, 0.0, wind.z, 0.5);
			}
		}
		return pushed;
	}

	/** Whether {@code player} is sheltered from a wind blowing {@code way}: rock stands within {@value #LEE} upwind of their chest. */
	public boolean sheltered(ServerLevel level, Player player, Vec3 way) {
		Vec3 chest = player.position().add(0.0, 0.6, 0.0);
		Vec3 upwind = chest.subtract(way.scale(LEE));
		return level.clip(new ClipContext(chest, upwind, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player)).getType() == HitResult.Type.BLOCK;
	}

	// ---------------------------------------------------------------- his fledglings

	/** He screams to the nest: fledglings leave it until he has {@value #FLEDGLING_CAP}. Returns how many came. */
	public int callFledglings(ServerLevel level) {
		int came = 0;
		Vec3 nest = waiting();
		while (fledglings.size() < FLEDGLING_CAP) {
			Vec3 at = nest.add((getRandom().nextDouble() - 0.5) * 3.0, 0.5, (getRandom().nextDouble() - 0.5) * 3.0);
			fledglings.add(GaleFledglingEntity.rise(level, this, at).getUUID());
			came++;
		}
		return came;
	}

	/** Forgets what is gone. */
	private void prune(ServerLevel level) {
		fledglings.removeIf(id -> !(level.getEntity(id) instanceof GaleFledglingEntity fledgling) || !fledgling.isAlive());
	}

	/** Whoever hurt him or his fledglings has taken part. */
	public void took(ServerPlayer player, float amount) {
		damageBy.merge(player.getUUID(), amount, Float::sum);
	}

	/** His participants: everyone who hurt him or his fledglings, or stood by the rod that grounded him, still near the summit. */
	public List<ServerPlayer> participants(ServerLevel level) {
		List<ServerPlayer> out = new ArrayList<>();
		Vec3 centre = centre();
		for (ServerPlayer player : level.players()) {
			boolean part = damageBy.getOrDefault(player.getUUID(), 0.0F) > 0.0F || helpers.contains(player.getUUID());
			if (player.isAlive() && part && player.position().distanceTo(centre) <= 64.0) {
				out.add(player);
			}
		}
		return out;
	}

	// ---------------------------------------------------------------- his rule: the Storm Rods

	/** A rod to charge: any but the last. */
	private Rod nextRod() {
		List<Rod> choices = new ArrayList<>(Arrays.asList(Rod.values()));
		choices.remove(lastCharged);
		return choices.get(getRandom().nextInt(choices.size()));
	}

	/**
	 * Where he dives from for {@code rod}: over the summit's edge, {@value #PERCH_OUT} from its middle and {@value #PERCH_UP}
	 * over its floor, {@code offset} degrees round from the rod's bearing.
	 */
	public Vec3 perch(Rod rod, double offset) {
		Vec3 centre = centre();
		Vec3 at = rod(rod);
		double bearing = Math.atan2(at.x - centre.x, -(at.z - centre.z)) + Math.toRadians(offset);
		return new Vec3(centre.x + Math.sin(bearing) * PERCH_OUT, floorY() + PERCH_UP, centre.z - Math.cos(bearing) * PERCH_OUT);
	}

	/**
	 * He begins a dive at {@code target}: {@code rod} charges and the bar names it, and he climbs to where he dives from,
	 * {@code offset} degrees round from the rod's bearing.
	 */
	public void dive(ServerLevel level, Player target, Rod rod, double offset) {
		breakOff();
		foe = target.getUUID();
		charged = rod;
		lastCharged = rod;
		CrownOfThunder.charge(level, origin, rod, true);
		perch = perch(rod, offset);
		flightFrom = position();
		dive = Dive.CHARGE;
		diveTicks = 0;
		groundAt = -1.0;
		diveHit.clear();
		airborne();
		setAction(Action.FLY);
		bossBar.setName(Component.translatable("message.jugcraft.storm_roc.charged." + rod.title()));
		level.playSound(null, getX(), getY(), getZ(), cry(), SoundSource.HOSTILE, 3.0F, 0.6F);
		Vec3 centre = centre();
		for (ServerPlayer player : level.players()) {
			if (player.position().distanceTo(centre) <= LEASH + 32.0) {
				player.sendSystemMessage(Component.translatable("message.jugcraft.storm_roc.charge_tell", rod.display()));
			}
		}
	}

	/** His dive ticks on: climbing as the rod charges, holding as his lane follows his foe, diving, grounded. */
	private void diveTick(ServerLevel level, @Nullable Player target) {
		diveTicks++;
		switch (dive) {
			case CHARGE -> {
				double share = Math.min(1.0, diveTicks / (double) CHARGE_TICKS);
				double eased = share * share * (3.0 - 2.0 * share);
				Vec3 line = flightFrom.add(perch.subtract(flightFrom).scale(eased));
				setPos(line.x, line.y + 2.0 * Math.sin(Math.PI * share), line.z);
				face(perch.subtract(flightFrom));
				if (diveTicks % 8 == 0) {
					level.playSound(null, getX(), getY(), getZ(), SoundEvents.ENDER_DRAGON_FLAP, SoundSource.HOSTILE, 1.5F, 1.0F);
				}
				if (target == null) {
					land(level, false);
				} else if (diveTicks >= CHARGE_TICKS) {
					dive = Dive.LANE;
					diveTicks = 0;
					setAction(Action.HOVER);
					if (target instanceof ServerPlayer marked) {
						marked.sendSystemMessage(Component.translatable("message.jugcraft.storm_roc.marked"));
					}
				}
			}
			case LANE -> {
				setPos(perch.x, perch.y + 0.3 * Math.sin(diveTicks * 0.4), perch.z);
				if (target == null) {
					land(level, false);
					return;
				}
				face(target);
				lane(target.position());
				if (diveTicks % 2 == 0) {
					sparks(level);
				}
				if (diveTicks >= LANE_TICKS) {
					hold(level);
				}
			}
			case DIVING -> diving(level);
			case GROUNDED -> {
				hold();
				setAction(Action.GROUNDED);
				if (diveTicks % 10 == 0) {
					level.sendParticles(ParticleTypes.ELECTRIC_SPARK, getX(), getY() + 1.0, getZ(), 6, 1.2, 0.4, 1.2, 0.1);
				}
				if (diveTicks >= GROUNDED_TICKS) {
					dive = Dive.NONE;
					diveTicks = 0;
					diveTimer = diveEvery();
					globalCooldown = GLOBAL_COOLDOWN;
					setAction(Action.IDLE);
					level.playSound(null, getX(), getY(), getZ(), cry(), SoundSource.HOSTILE, 2.5F, 0.7F);
				}
			}
			default -> {
			}
		}
	}

	/** His lane, from where he dives from, through {@code through} (a point on the summit, or one beyond it drawn onto it). */
	public void lane(Vec3 through) {
		laneTo = inside(through, 1.0);
		Vec3 way = flat(laneTo.subtract(perch));
		Vec3 end = laneTo.add(way.scale(DIVE_OVERSHOOT));
		// He lands on the summit: the lane ends where it would leave it.
		double most = flatDistance(perch, laneTo) + DIVE_OVERSHOOT;
		for (double s = flatDistance(perch, laneTo); s <= most; s += 0.25) {
			Vec3 at = new Vec3(perch.x, floorY(), perch.z).add(way.scale(s));
			if (!onSummit(at) || at.distanceTo(centre()) > CrownOfThunder.SUMMIT - LAND_IN) {
				break;
			}
			end = at;
		}
		laneEnd = end;
	}

	/** Lays his lane as its holding would (for tests): through {@code through}. */
	public void laneThrough(Vec3 through) {
		lane(through);
	}

	private static double flatDistance(Vec3 a, Vec3 b) {
		return Math.hypot(b.x - a.x, b.z - a.z);
	}

	/** The line of sparks down his lane, from him through his foe to where he would land. */
	private void sparks(ServerLevel level) {
		Vec3 from = position().add(0.0, 1.0, 0.0);
		Vec3 to = laneEnd.add(0.0, 0.3, 0.0);
		double length = from.distanceTo(to);
		for (double s = 0.0; s <= length; s += 0.7) {
			Vec3 at = from.add(to.subtract(from).scale(s / length));
			level.sendParticles(ParticleTypes.ELECTRIC_SPARK, at.x, at.y, at.z, 1, 0.0, 0.0, 0.0, 0.0);
		}
	}

	/**
	 * His lane holds still and he dives: where along it (flat) the charged rod would ground him is fixed now, if the lane
	 * passes within {@value #GROUND_RADIUS} of it before its end.
	 */
	public void hold(ServerLevel level) {
		groundAt = charged == null ? -1.0 : groundingAt(perch, laneEnd, rod(charged));
		along = 0.0;
		dive = Dive.DIVING;
		diveTicks = 0;
		diveHit.clear();
		setAction(Action.DIVE);
		level.playSound(null, getX(), getY(), getZ(), cry(), SoundSource.HOSTILE, 4.0F, 0.8F);
	}

	/**
	 * Where along a lane from {@code from} to {@code end} (flat) it passes nearest {@code rod}, if within
	 * {@value #GROUND_RADIUS} of it and before the lane's end; else -1.
	 */
	public static double groundingAt(Vec3 from, Vec3 end, Vec3 rod) {
		Vec3 way = new Vec3(end.x - from.x, 0.0, end.z - from.z);
		double length = way.length();
		if (length < 1.0E-6) {
			return -1.0;
		}
		Vec3 unit = way.scale(1.0 / length);
		Vec3 to = new Vec3(rod.x - from.x, 0.0, rod.z - from.z);
		double s = to.dot(unit);
		double off = to.subtract(unit.scale(s)).length();
		return s > 0.0 && s <= length && off <= GROUND_RADIUS ? s : -1.0;
	}

	/** Diving: down his lane at {@value #DIVE_SPEED} a tick, striking whoever is near his path, until grounded or landed. */
	private void diving(ServerLevel level) {
		Vec3 start = new Vec3(perch.x, floorY(), perch.z);
		Vec3 way = flat(laneEnd.subtract(start));
		double toFoe = flatDistance(start, laneTo);
		double length = flatDistance(start, laneEnd);
		Vec3 before = position();
		along = Math.min(length, along + DIVE_SPEED);
		boolean grounding = groundAt >= 0.0 && along >= groundAt;
		double s = grounding ? groundAt : along;
		double height = s >= toFoe || toFoe < 1.0E-6 ? 0.0 : PERCH_UP * (1.0 - s / toFoe);
		Vec3 at = start.add(way.scale(s)).add(0.0, height, 0.0);
		setPos(at.x, at.y, at.z);
		face(way);
		strikeAlong(level, before, at);
		if (grounding) {
			ground(level);
		} else if (along >= length || diveTicks > FLIGHT_MAX) {
			land(level, true);
		}
	}

	/** His path from {@code from} to {@code to} this tick: {@value #DIVE_DAMAGE} and a shove aside to everyone near it (once a dive). */
	public int strikeAlong(ServerLevel level, Vec3 from, Vec3 to) {
		Vec3 seg = to.subtract(from);
		double length = seg.length();
		int hit = 0;
		for (ServerPlayer player : nearby(level)) {
			if (diveHit.contains(player.getUUID())) {
				continue;
			}
			Vec3 body = player.position().add(0.0, player.getBbHeight() / 2.0, 0.0);
			double t = length < 1.0E-6 ? 0.0 : Math.max(0.0, Math.min(1.0, body.subtract(from).dot(seg) / (length * length)));
			Vec3 nearest = from.add(seg.scale(t)).add(0.0, 1.0, 0.0);
			Vec3 off = body.subtract(nearest);
			double flatOff = Math.hypot(off.x, off.z);
			if (flatOff > DIVE_WIDTH + player.getBbWidth() / 2.0 || Math.abs(off.y) > 2.0) {
				continue;
			}
			if (player.hurtServer(level, damageSources().mobAttack(this), damage(DIVE_DAMAGE))) {
				diveHit.add(player.getUUID());
				hit++;
				Vec3 aside = flatOff < 1.0E-3 ? new Vec3(-seg.z, 0.0, seg.x) : new Vec3(off.x, 0.0, off.z);
				Vec3 shove = aside.lengthSqr() < 1.0E-6 ? facing() : aside.normalize();
				push(player, shove.scale(DIVE_SHOVE).add(0.0, 0.4, 0.0));
			}
		}
		return hit;
	}

	/**
	 * The charged rod grounds him: a bolt arcs from it into him, he crashes on the summit clear of its plinth, and lies
	 * stunned; whoever stands by the rod takes part.
	 */
	public void ground(ServerLevel level) {
		Rod rod = charged;
		Vec3 here = new Vec3(getX(), floorY(), getZ());
		if (rod != null) {
			Vec3 middle = rod(rod);
			Vec3 away = new Vec3(here.x - middle.x, 0.0, here.z - middle.z);
			if (away.length() < GROUND_CLEAR) {
				Vec3 out = away.lengthSqr() < 1.0E-6 ? facing() : away.normalize();
				here = middle.add(out.scale(GROUND_CLEAR));
			}
			Vec3 tip = middle.add(0.0, CrownOfThunder.ROD_HEIGHT + 1.0, 0.0);
			Vec3 me = position().add(0.0, 1.0, 0.0);
			for (double s = 0.0; s <= 1.0; s += 0.08) {
				Vec3 at = tip.add(me.subtract(tip).scale(s));
				level.sendParticles(ParticleTypes.ELECTRIC_SPARK, at.x, at.y, at.z, 2, 0.05, 0.05, 0.05, 0.02);
			}
			for (ServerPlayer player : nearby(level)) {
				if (Math.hypot(player.getX() - middle.x, player.getZ() - middle.z) <= GROUND_RADIUS + 1.5) {
					helpers.add(player.getUUID());
				}
			}
			CrownOfThunder.charge(level, origin, rod, false);
		}
		bolt(level, position());
		charged = null;
		Vec3 land = settle(level, inside(here, LAND_IN));
		teleportTo(land.x, land.y, land.z);
		walking();
		dive = Dive.GROUNDED;
		diveTicks = 0;
		setAction(Action.GROUNDED);
		bossBar.setName(barName());
		level.playSound(null, getX(), getY(), getZ(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 2.0F, 0.6F);
		tell(level, "message.jugcraft.storm_roc.grounded");
	}

	/** He lands where his flight ends (on the summit), the charged rod discharging harmlessly if he dove past it. */
	private void land(ServerLevel level, boolean dove) {
		if (charged != null) {
			Vec3 middle = rod(charged);
			level.sendParticles(ParticleTypes.ELECTRIC_SPARK, middle.x, middle.y + CrownOfThunder.ROD_HEIGHT, middle.z, 20, 0.3, 0.6, 0.3, 0.2);
			CrownOfThunder.charge(level, origin, charged, false);
			if (dove) {
				tell(level, "message.jugcraft.storm_roc.discharged");
			}
		}
		charged = null;
		Vec3 land = settle(level, inside(position(), LAND_IN));
		teleportTo(land.x, land.y, land.z);
		walking();
		dive = Dive.NONE;
		diveTicks = 0;
		diveTimer = diveEvery();
		globalCooldown = GLOBAL_COOLDOWN;
		setAction(Action.IDLE);
		bossBar.setName(barName());
	}

	/** A place to stand near {@code want} on the summit's floor: his body clear of rock, rods and boulders. */
	public Vec3 settle(ServerLevel level, Vec3 want) {
		Vec3 base = inside(want, LAND_IN);
		for (double r = 0.0; r <= 5.0; r += 0.5) {
			int steps = r == 0.0 ? 1 : (int) Math.ceil(r * 4);
			for (int i = 0; i < steps; i++) {
				double a = Math.PI * 2.0 * i / steps;
				Vec3 at = new Vec3(base.x + Math.cos(a) * r, floorY(), base.z + Math.sin(a) * r);
				AABB body = getType().getDimensions().makeBoundingBox(at);
				if (onSummit(at) && level.noCollision(this, body)) {
					return at;
				}
			}
		}
		return woke();
	}

	// ---------------------------------------------------------------- the Squall, the Eye

	/** The Squall Rises: he climbs and circles screaming for {@value #SQUALL_TICKS} ticks, unhurt, and his fledglings leave the nest. */
	public void squall(ServerLevel level) {
		breakOff();
		phase = Phase.RISING;
		phaseTicks = 0;
		circleFrom = position();
		airborne();
		setAction(Action.CIRCLE);
		level.playSound(null, getX(), getY(), getZ(), cry(), SoundSource.HOSTILE, 4.0F, 0.5F);
		callFledglings(level);
		tell(level, "message.jugcraft.storm_roc.squall");
	}

	private void rising(ServerLevel level) {
		airborne();
		Vec3 centre = centre();
		double start = Math.atan2(circleFrom.z - centre.z, circleFrom.x - centre.x);
		double a = start + Math.PI * 2.0 * phaseTicks / (double) SQUALL_TICKS;
		Vec3 circling = centre.add(Math.cos(a) * SQUALL_CIRCLE, SQUALL_HEIGHT, Math.sin(a) * SQUALL_CIRCLE);
		Vec3 at;
		if (phaseTicks <= 15) {
			at = circleFrom.add(circling.subtract(circleFrom).scale(phaseTicks / 15.0));
		} else if (phaseTicks >= SQUALL_TICKS - 15) {
			Vec3 down = centre.add(Math.cos(a) * SQUALL_CIRCLE * 0.5, 0.0, Math.sin(a) * SQUALL_CIRCLE * 0.5);
			at = circling.add(down.subtract(circling).scale((phaseTicks - (SQUALL_TICKS - 15)) / 15.0));
		} else {
			at = circling;
		}
		Vec3 before = position();
		setPos(at.x, at.y, at.z);
		face(at.subtract(before));
		if (phaseTicks % 10 == 0) {
			level.playSound(null, getX(), getY(), getZ(), SoundEvents.ENDER_DRAGON_FLAP, SoundSource.HOSTILE, 2.0F, 0.8F);
			level.sendParticles(ParticleTypes.CLOUD, centre.x, centre.y + 1.0, centre.z, 20, 8.0, 0.5, 8.0, 0.2);
		}
		if (phaseTicks >= SQUALL_TICKS) {
			Vec3 land = settle(level, position());
			teleportTo(land.x, land.y, land.z);
			walking();
			phase = Phase.SQUALL;
			phaseTicks = 0;
			globalCooldown = GLOBAL_COOLDOWN;
			diveTimer = Math.min(diveTimer, DIVE_EVERY_SQUALL);
			setAction(Action.IDLE);
			bossBar.setName(barName());
		}
	}

	/** The Eye of the Storm: he dives oftener, and marks five rings. */
	public void eye(ServerLevel level) {
		phase = Phase.EYE;
		phaseTicks = 0;
		diveTimer = Math.min(diveTimer, DIVE_EVERY_EYE);
		if (charged == null) {
			bossBar.setName(barName());
		}
		level.playSound(null, getX(), getY(), getZ(), cry(), SoundSource.HOSTILE, 4.0F, 0.4F);
		tell(level, "message.jugcraft.storm_roc.eye");
	}

	/** The bar's own name now: his, the Squall's or the Eye's. */
	private Component barName() {
		return switch (phase) {
			case SQUALL -> Component.translatable("message.jugcraft.storm_roc.squall_bar");
			case EYE -> Component.translatable("message.jugcraft.storm_roc.eye_bar");
			default -> Component.translatable("entity.jugcraft.storm_roc");
		};
	}

	// ---------------------------------------------------------------- abandoned, and defeated

	/**
	 * Left alone: he flies back to his nest, healed; his fledglings return to it, the rods go quiet, the wind drops, and who
	 * hurt him is forgotten.
	 */
	public void reset(ServerLevel level) {
		dismiss(level);
		phase = Phase.WAITING;
		phaseTicks = 0;
		attack = null;
		attackTicks = 0;
		abandoned = 0;
		dive = Dive.NONE;
		diveTicks = 0;
		diveTimer = DIVE_FIRST;
		foe = null;
		damageBy.clear();
		helpers.clear();
		marks.clear();
		strips.clear();
		Arrays.fill(cooldowns, 0);
		AttributeInstance health = getAttribute(Attributes.MAX_HEALTH);
		if (health != null) {
			health.setBaseValue(HEALTH);
		}
		setHealth(getMaxHealth());
		airborne();
		Vec3 waiting = waiting();
		teleportTo(waiting.x, waiting.y, waiting.z);
		face(0.0F);
		setAction(Action.PERCH);
		bossBar.setName(Component.translatable("entity.jugcraft.storm_roc"));
		bossBar.setVisible(false);
		tell(level, "message.jugcraft.storm_roc.reset");
	}

	/** His fledglings fly back to the nest, and every rod goes quiet. */
	private void dismiss(ServerLevel level) {
		for (UUID id : List.copyOf(fledglings)) {
			if (level.getEntity(id) instanceof GaleFledglingEntity fledgling) {
				fledgling.leave(level);
			}
		}
		fledglings.clear();
		for (Rod rod : Rod.values()) {
			CrownOfThunder.charge(level, origin, rod, false);
		}
		charged = null;
	}

	@Override
	public void die(DamageSource source) {
		super.die(source);
		walking();
		if (level() instanceof ServerLevel level) {
			defeated(level);
		}
	}

	/**
	 * He has fallen: his fledglings fly off and the rods go quiet, each participant gets their loot, his lair's instance
	 * ends (it closes once everyone has left), a last thunderclap, and the Grey Mist opens at the foot of his crag: the way
	 * home.
	 */
	private void defeated(ServerLevel level) {
		dismiss(level);
		dive = Dive.NONE;
		RocLoot.reward(level, this, participants(level));
		LairInstance open = lairInstance(level);
		if (open != null) {
			Lairs.end(open);
		}
		BlockState mist = JugcraftLairs.LAIR_EXIT.defaultBlockState().setValue(LairExitBlock.AXIS, Direction.Axis.X);
		for (BlockPos cell : CrownOfThunder.fallExitCells()) {
			BlockPos at = origin.offset(cell);
			if (level.getBlockState(at).isAir()) {
				level.setBlock(at, mist, Block.UPDATE_ALL);
			}
		}
		bolt(level, position());
		bossBar.setVisible(false);
		tell(level, "message.jugcraft.storm_roc.defeated");
	}

	/** Tells everyone near the summit. */
	void tell(ServerLevel level, String key) {
		Vec3 centre = centre();
		for (ServerPlayer player : level.players()) {
			if (player.position().distanceTo(centre) <= LEASH + 32.0) {
				player.sendSystemMessage(Component.translatable(key));
			}
		}
	}

	/** His cry: the owner's bald eagle's (art/owner-library), pitched down for his size; a phantom's if it is not there. */
	static SoundEvent cry() {
		return BuiltInRegistries.SOUND_EVENT.getOptional(Jugcraft.id("beast.bald_eagle_idle")).orElse(SoundEvents.PHANTOM_AMBIENT);
	}

	// ---------------------------------------------------------------- what he is immune to, and his grounding

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
		if (phase == Phase.WAKING || phase == Phase.RISING || source.is(DamageTypeTags.IS_LIGHTNING) || source.is(GroundingStrap.ELECTRIC)) {
			return false;
		}
		float dealt = dive == Dive.GROUNDED ? amount * GROUNDED_MULTIPLIER : amount;
		boolean hurt = super.hurtServer(level, source, dealt);
		if (hurt && source.getEntity() instanceof ServerPlayer player) {
			took(player, dealt);
		}
		return hurt;
	}

	@Override
	public boolean canBeAffected(MobEffectInstance effect) {
		return !effect.is(MobEffects.LEVITATION) && super.canBeAffected(effect);
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
		return phase == Phase.WAITING ? null : cry();
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return BuiltInRegistries.SOUND_EVENT.getOptional(Jugcraft.id("beast.bald_eagle_hurt")).orElse(SoundEvents.PHANTOM_HURT);
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.PHANTOM_DEATH;
	}

	@Override
	public float getVoicePitch() {
		return 0.5F;
	}

	// ---------------------------------------------------------------- saving and syncing

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(ACTION, Action.PERCH.ordinal());
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
		origin = input.read("origin", BlockPos.CODEC).orElse(BlockPos.containing(getX() - CrownOfThunder.WAIT_X,
				getY() - CrownOfThunder.WAIT_Y, getZ() - CrownOfThunder.WAIT_Z));
		try {
			phase = Phase.valueOf(input.getStringOr("phase", Phase.WAITING.name()));
		} catch (IllegalArgumentException e) {
			phase = Phase.WAITING;
		}
		// A passing moment is not resumed: waking, he comes down to the summit; rising, the Squall begins; any flight ends
		// on the summit (his dive is never saved, so the rods are calm).
		if (phase == Phase.WAKING) {
			phase = Phase.GATHERING;
		} else if (phase == Phase.RISING) {
			phase = Phase.SQUALL;
		}
		landOnLoad = phase != Phase.WAITING;
		dive = Dive.NONE;
		if (phase == Phase.WAITING) {
			noPhysics = true;
			setNoGravity(true);
		}
		setAction(phase == Phase.WAITING ? Action.PERCH : Action.IDLE);
		bossBar.setVisible(phase != Phase.WAITING);
		bossBar.setName(barName());
	}

	// ---------------------------------------------------------------- his animations

	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
		controllers.add(new AnimationController<StormRocEntity>("body", 4, test -> {
			StormRocEntity roc = test.animatable();
			if (roc.isDeadOrDying()) {
				return test.setAndContinue(DEATH);
			}
			Action action = roc.action();
			if (action == Action.IDLE && test.isMoving()) {
				return test.setAndContinue(WALK);
			}
			return test.setAndContinue(BODY[action.ordinal()]);
		}));
		controllers.add(new AnimationController<StormRocEntity>("wings", 3,
				test -> test.setAndContinue(test.animatable().isDeadOrDying() || test.animatable().action().spread ? WINGS_SPREAD : WINGS_FOLDED)));
	}

	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache() {
		return cache;
	}
}

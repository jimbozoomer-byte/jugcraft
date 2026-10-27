package io.github.jimbozoomer.jugcraft.concordance.spirits;

import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.PlayState;
import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.concordance.Authority;
import io.github.jimbozoomer.jugcraft.concordance.resource.BoundWill;
import io.github.jimbozoomer.jugcraft.concordance.worker.Pacts;
import io.github.jimbozoomer.jugcraft.concordance.worker.Status;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.ToLongFunction;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Registry;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Concordance garden cohort, GC-01 (SG2; docs/features/concordance-garden-cohort.md): what every pact spirit is, taken from
 * Stolas (Concordance Chunk D, D-04, D-06) without changing him. A worker (here in spirits/, so check_workers' bans on chunk
 * loading, dimension changes and brain memories cover it; MISC, so transport crates refuse it). While a conversation is
 * open at its perch and nothing is kept there it is a visitor, bound to no one and on no roster; it leaves when it has
 * neither a pact nor a live conversation (declined, closed, timed out, its player gone, a restart), with the Concordance
 * on or off: a visitor is saved with its chunk (its perch only) and leaves at its first think after it loads. Once a pact
 * is sealed it is its one presence; it leaves when the pact is ended, kept elsewhere or answered by another. Only damage
 * that bypasses invulnerability harms it; if it is lost all the same, the pact is not: its holder calls it again at no
 * cost. It keeps to its perch.
 * <p>
 * Its pact's service (PactServices, by the record's work) takes one autonomous step a think, and only after {@link #decide}
 * has returned IDLE for a presence with a record: its UUID and record verified against its perch, its pact's state read,
 * its holder present near (Authority.presentNear within Pacts.REACH). So a visitor, a copy, a duplicate, a suspended,
 * held or unattended pact never works. Stolas's service takes none.
 * <p>
 * Its body (its subclass's {@link Body}): the worker's main controller plays its loops for what the server synced; its
 * gestures play on a controller of their own over it, each fired by the server through {@link #gesture} (GeckoLib sends it
 * to those watching it then; one who starts watching later sees its state, never a replay). It arrives when it is
 * summoned to a perch; when it leaves it makes its departing gesture and is gone that gesture's length of game time
 * later: struck off its holder's roster at once, never saved meanwhile, discarded by its own tick or, where its chunk no
 * longer ticks it, by the server's sweep ({@link #registerSweep}, one for every spirit).
 * <p>
 * Its cues: each gesture and its listening is fired by the server's action, once, with its sound (jugcraft:<spirit>.<cue>)
 * and subtitle, at most one of a kind every {@link #CUE_TICKS} and none while the Concordance is off but its departure.
 * Saved: {@code perch} and {@code record}, here and nowhere else (a subclass saves nothing of its own).
 */
public abstract class PactSpiritEntity<T extends PactSpiritEntity<T>> extends WorkerEntity<T> implements AgreementHolder.Presence {
	/** The controller a spirit's gestures play on, registered after the worker's own "main" one, so a gesture's bones win while it plays. */
	public static final String GESTURE_CONTROLLER = "gesture";
	/**
	 * The least time between two of a spirit's cues of one kind (tools/concordance_pacts.py CUE_TICKS): a player's cues come
	 * from rate-gated actions; this bounds what any number of players can make it do (08:145-155).
	 */
	public static final int CUE_TICKS = 10;
	/** How many recent cues it keeps for reading (tests); never saved. */
	private static final int SHOWN = 8;
	/** Whether a conversation is open at its perch (the server's; never saved). */
	private static final EntityDataAccessor<Boolean> LISTENING = SynchedEntityData.defineId(PactSpiritEntity.class, EntityDataSerializers.BOOLEAN);
	/** Whether it is leaving (the server's, set with its departing gesture; never saved): the client hides one it never saw go. */
	private static final EntityDataAccessor<Boolean> DEPARTING = SynchedEntityData.defineId(PactSpiritEntity.class, EntityDataSerializers.BOOLEAN);
	/** Every spirit's sound events by path ("stolas.arrive"), registered once each (Workers.register). */
	private static final Map<String, SoundEvent> SOUNDS = new HashMap<>();
	/**
	 * Every spirit leaving now, by identity, for the server's sweep: it ends a departure on time where its own ticks do not
	 * (its chunk tracked but not ticking, or unloaded around it, since a departing spirit is never saved and so never
	 * unloaded with it). Memory only, emptied when the server stops.
	 */
	private static final Set<PactSpiritEntity<?>> LEAVING = Collections.newSetFromMap(new IdentityHashMap<>());

	/** One of a spirit's gestures: its name on {@link #GESTURE_CONTROLLER}, its length in ticks and its clip (a subclass's enum). */
	public interface Clip {
		String id();

		int ticks();

		RawAnimation animation();
	}

	/**
	 * A spirit's body as the base reads it (tools/concordance_pacts.py SPIRITS): its sound path (jugcraft:<spirit>.<cue>), its
	 * cues in SOUND_EVENTS' order (each gesture's and "listen"), its gestures, and the ones it arrives and departs with.
	 */
	public record Body(String spirit, List<String> cues, List<? extends Clip> gestures, Clip arrive, Clip depart) {
		public Body {
			cues = List.copyOf(cues);
			gestures = List.copyOf(gestures);
		}
	}

	private final Body body;
	private @Nullable BlockPos perch;
	private @Nullable UUID record;
	/**
	 * The game time its departure ends at; 0 while it is not leaving. Never saved: a departing spirit is not written to the
	 * world. Its own tick or the server's sweep discards it once it is reached, whether or not its chunk ticks it.
	 */
	private long departAt;
	/** When each of its cues last showed (game time, by its Body's cues' index); never saved. */
	private final long[] cued;
	/** Its recent cues, oldest first; never saved. */
	private final ArrayDeque<String> shown = new ArrayDeque<>(SHOWN);
	/** GC-02: what a service's step has it look at (a bed, a reservoir), and until when (game time); memory only, never saved. */
	private @Nullable BlockPos working;
	private long workingUntil;

	protected PactSpiritEntity(EntityType<? extends PathfinderMob> type, Level level, Body body) {
		super(type, level);
		this.body = body;
		this.cued = new long[body.cues().size()];
		setNoGravity(true);
		Arrays.fill(cued, Long.MIN_VALUE / 2);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 20.0).add(Attributes.MOVEMENT_SPEED, 0.0).add(Attributes.FOLLOW_RANGE, 8.0);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(LISTENING, false);
		builder.define(DEPARTING, false);
	}

	/** Every pact presence's roster kind (a saved string, Chunk D's): one kind for every spirit, never one a spirit. */
	@Override
	public final String kind() {
		return "pact";
	}

	/**
	 * A spirit joins its holder's roster only when its pact is sealed, it is called again or the pact is seated
	 * (AgreementHolder's note); a change of status never puts one replaced, or loaded while the Concordance is off, back on
	 * it (unit 1's unit review fix 2, as for the Gathering Shade).
	 */
	@Override
	protected boolean joinsRosterOnStatus() {
		return false;
	}

	/** Its pacts' counterpart: its own entity id (a pact definition's id is its spirit's entity id, Chunk D). */
	public String counterpart() {
		return BuiltInRegistries.ENTITY_TYPE.getKey(getType()).toString();
	}

	/** Its body's table (its subclass's). */
	public Body body() {
		return body;
	}

	public @Nullable BlockPos perch() {
		return perch;
	}

	public void setPerch(BlockPos at) {
		perch = at.immutable();
	}

	/** The Bound Will it serves (null for a visitor, and for a presence until it first thinks). */
	public @Nullable UUID record() {
		return record;
	}

	@Override
	public @Nullable BlockPos seat() {
		return perch;
	}

	/** Whether a conversation is open at its perch (both sides: the client draws it listening). */
	public boolean listening() {
		return entityData.get(LISTENING);
	}

	/** Whether it is leaving (the server's): its departing gesture is playing and it will be gone when it ends. */
	public boolean departing() {
		return departAt != 0;
	}

	/** Whether it is leaving, as synced (both sides): the client draws it only where it saw its departing gesture fired. */
	public boolean leaving() {
		return entityData.get(DEPARTING);
	}

	/** Gone, or going: removed, or departing. No perch, roster, pact or test counts it from then on. */
	public boolean gone() {
		return isRemoved() || departAt != 0;
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
		if (next == Status.IDLE && owner != null && record != null && !gone()) {
			// GC-01 (SG1): its pact's service may take one step, only now: a presence verified at its own perch, its pact
			// serving and its holder near.
			next = serve(level, time, gameTime);
		}
		if (!gone()) {
			// Leaving, it keeps its last status, as it did when it left at once: no sign over it, no roster entry touched.
			setStatus(next);
			// D-06: it listens while a conversation is open at its perch (its screen open, its time not past).
			entityData.set(LISTENING, perch != null && next != Status.DISABLED
					&& PactSessions.live(level.getServer(), GlobalPos.of(level.dimension(), perch), gameTime));
			// GC-02 (GC-S6): whom it faces, once a think: its work, its speaker, or no one (its body keeps its facing).
			face(level, gameTime, next);
		}
		return next;
	}

	private Status decide(ServerLevel level, long gameTime) {
		if (owner == null && record == null && !awaited(level, gameTime)) {
			// A visitor no conversation waits on leaves whether or not the Concordance is on: it holds no record, and one
			// saved with its chunk is no perch's visitor when it loads (the unit review).
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

	/**
	 * GC-01 (SG1): its pact's service's one autonomous step (PactService.work: at most one change; Stolas's takes none), for
	 * the record its perch keeps; IDLE when its work is one this version does not know.
	 */
	private Status serve(ServerLevel level, long time, long gameTime) {
		if (!(level.getBlockEntity(perch) instanceof PactPerchBlockEntity home)) {
			return Status.IDLE;
		}
		BoundWill bound = home.holder().record(level);
		BoundWill.Service terms = bound == null || bound.pact() == null ? null : bound.pact().service();
		PactService service = terms == null ? null : PactServices.get(terms.work());
		return service == null ? Status.IDLE : service.work(level, this, bound, home.holder(), time, gameTime);
	}

	/** Whether a conversation still waits on this visitor: its perch loaded, it its visitor, a conversation there live. */
	private boolean awaited(ServerLevel level, long gameTime) {
		return perch != null && level.isLoaded(perch) && level.getBlockEntity(perch) instanceof PactPerchBlockEntity home
				&& getUUID().equals(home.visitor()) && PactSessions.live(level.getServer(), GlobalPos.of(level.dimension(), perch), gameTime);
	}

	/** Pushed or led off its perch, it is back on it (within its own level, check_workers). Every unit-1 spirit keeps its station on the perch (GC-P11). */
	private void stay() {
		double x = perch.getX() + 0.5, y = perch.getY() + 1.0, z = perch.getZ() + 0.5;
		if (distanceToSqr(x, y, z) > 1.0) {
			teleportTo(x, y, z);
		}
	}

	@Override
	public void leave(ServerLevel level, AgreementHolder.@Nullable Holds home, BlockPos at) {
		if (gone()) {
			return;
		}
		if (owner != null) {
			WorkerRoster.of(level.getServer()).remove(owner, getUUID());
		}
		// D-06: it makes its departing gesture, and is discarded when it ends (customServerAiStep, or the server's sweep where
		// its chunk no longer ticks it); it listens no more, and one who starts watching it now sees it gone (the unit review).
		departAt = level.getGameTime() + body.depart().ticks();
		LEAVING.add(this);
		entityData.set(LISTENING, false);
		entityData.set(DEPARTING, true);
		stop();
		gesture(body.depart());
	}

	/** Departing, it only waits for its going; otherwise it thinks as every worker does. */
	@Override
	protected void customServerAiStep(ServerLevel level) {
		if (departAt != 0) {
			departed(level.getGameTime());
			return;
		}
		super.customServerAiStep(level);
	}

	/**
	 * Departing, it is not written to the world: a chunk saved or unloaded, or a server stopped, mid-departure leaves it out.
	 * An unsaved entity is not unloaded with its chunk, so the server's sweep ({@link #registerSweep}) discards it on time,
	 * ticking or not.
	 */
	@Override
	public boolean shouldBeSaved() {
		return departAt == 0 && super.shouldBeSaved();
	}

	/** Whether its departure is over by {@code gameTime}; if it is, it is discarded. */
	private boolean departed(long gameTime) {
		if (departAt == 0 || gameTime < departAt) {
			return false;
		}
		discard();
		return true;
	}

	/**
	 * Registers, once (Workers.register), the server's sweep at the end of every tick: each departure of every spirit ends on
	 * time, at its own level's game time, its entity ticking or not.
	 */
	public static void registerSweep() {
		ServerTickEvents.END_SERVER_TICK.register(server -> sweep(spirit -> spirit.level().getGameTime()));
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> LEAVING.clear());
	}

	/** The sweep at game time {@code gameTime}, as if the server's ran then (tests: no entity ticks meanwhile). */
	public static void sweep(long gameTime) {
		sweep(spirit -> gameTime);
	}

	/** Each spirit leaving whose departure is over by {@code time} is discarded, and one already removed is dropped. */
	private static void sweep(ToLongFunction<PactSpiritEntity<?>> time) {
		if (LEAVING.isEmpty()) {
			return;
		}
		for (PactSpiritEntity<?> spirit : List.copyOf(LEAVING)) {
			if (spirit.isRemoved() || spirit.departed(time.applyAsLong(spirit))) {
				LEAVING.remove(spirit);
			}
		}
	}

	/** Killed or discarded: struck off its holder's roster, which is how the perch knows it is lost (D-S18's rule). */
	@Override
	public void remove(RemovalReason reason) {
		if (reason.shouldDestroy() && owner != null && level() instanceof ServerLevel server) {
			WorkerRoster.of(server.getServer()).remove(owner, getUUID());
		}
		super.remove(reason);
	}

	/**
	 * Plays one of its gestures for everyone watching it now, with its sound (server side only: GeckoLib sends it to those
	 * tracking it; one who starts watching later sees its state, never a replay). Shown only as its cues allow
	 * ({@link #cueing}): never while the Concordance is off, at most one of a kind every CUE_TICKS; its departure always.
	 * Nothing else follows from it.
	 */
	public void gesture(Clip clip) {
		if (!isRemoved() && level() instanceof ServerLevel level && cueing(level, clip.id(), clip != body.depart())) {
			triggerAnim(GESTURE_CONTROLLER, clip.id());
		}
	}

	/** Registers {@code body}'s sound events once (its subclass's registerSounds, from Workers.register); sounds.json and their subtitles are tools/concordance_pacts.py's. */
	protected static void registerSounds(Body body) {
		for (String id : body.cues()) {
			Identifier key = Jugcraft.id(body.spirit() + "." + id);
			SOUNDS.put(body.spirit() + "." + id, Registry.register(BuiltInRegistries.SOUND_EVENT, key, SoundEvent.createVariableRangeEvent(key)));
		}
	}

	/** Its recent cues, oldest first (server side; never saved: one reloaded or newly watched has none). */
	public List<String> cues() {
		return List.copyOf(shown);
	}

	/**
	 * A conversation opens at its perch (PactPerchBlockEntity.heard): it listens at once (its loop, synced) and is heard to,
	 * unless it has only just arrived, whose arrival carries the moment. Switched off, nothing.
	 */
	void listen() {
		if (gone() || !Workers.enabled() || !(level() instanceof ServerLevel level)) {
			return;
		}
		entityData.set(LISTENING, true);
		// GC-02: it turns to whoever opened the screen at once, not at its next think.
		face(level, level.getGameTime(), status());
		if (level.getGameTime() - cued[body.cues().indexOf(body.arrive().id())] >= CUE_TICKS) {
			cueing(level, "listen", true);
		}
	}

	// ---------------------------------------------------------------- facing (Concordance garden cohort, GC-02; GC-S6)

	/**
	 * GC-02: the yaw that faces {@code to} from {@code from}, in Minecraft's convention (0 south, 90 west, 180 north, -90
	 * east; LookControl.getYRotD's formula), or {@code otherwise} when {@code to} is straight above or below.
	 */
	public static float bearing(Vec3 from, Vec3 to, float otherwise) {
		double dx = to.x - from.x;
		double dz = to.z - from.z;
		if (Math.abs(dx) < 1.0E-5 && Math.abs(dz) < 1.0E-5) {
			return otherwise;
		}
		return Mth.wrapDegrees((float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90.0F);
	}

	/**
	 * GC-02: yaw, head and body set to {@code yaw} at once, before it enters the world (PactPerchBlockEntity's summon):
	 * snapTo sets only the yaw, and each client gives its body the head's yaw it is sent.
	 */
	void turnTo(float yaw) {
		setYRot(yaw);
		setYHeadRot(yaw);
		setYBodyRot(yaw);
	}

	/**
	 * GC-02 (for a garden service's step, GC-04 onward): it looks at {@code target} while its work gesture plays
	 * ({@code ticks} from {@code gameTime}), then at its speaker again if one is live; {@link #face}, later in the same
	 * think, applies it. Public for its game test.
	 */
	public void attendWork(BlockPos target, int ticks, long gameTime) {
		working = target.immutable();
		workingUntil = gameTime + ticks;
	}

	/**
	 * GC-02 (GC-S6, GC-P8): whom it faces. Its work while its gesture plays, else whoever spoke last in a live conversation
	 * at its perch, else no one: the brain's look target is set or erased (WorkerEntity.attend), and its yaw is set to what
	 * it faces, or with no one to its body's own facing, so a hop back onto its station (stay(): teleportTo keeps the yaw),
	 * which a client draws as a move that turns the body to the yaw, never snaps it away from it. Switched off, it faces no
	 * one; a spirit whose day's quota is spent (FINISHED) still faces whoever speaks to it. One session lookup; no scan.
	 */
	private void face(ServerLevel level, long gameTime, Status next) {
		boolean attending = perch != null && next != Status.DISABLED;
		if (attending && working != null && gameTime < workingUntil) {
			attend(working);
			setYRot(bearing(position(), Vec3.atCenterOf(working), getYRot()));
			return;
		}
		working = null;
		ServerPlayer speaker = attending ? PactSessions.speaker(level.getServer(), GlobalPos.of(level.dimension(), perch), gameTime) : null;
		if (speaker != null && speaker.level() == level) {
			attend(speaker);
			setYRot(bearing(position(), speaker.position(), getYRot()));
		} else {
			attend((Entity) null);
			setYRot(yBodyRot);
		}
	}

	/**
	 * Whether cue {@code id} shows now; if so its sound plays at it (entity-attached, NEUTRAL, heard within 16 blocks:
	 * sounds.json) and it is noted. Never while the Concordance is off, and at most one of a kind every CUE_TICKS, when
	 * {@code bounded} (its departure is neither: it happens once, when it leaves).
	 */
	private boolean cueing(ServerLevel level, String id, boolean bounded) {
		int kind = body.cues().indexOf(id);
		long now = level.getGameTime();
		if (kind < 0 || bounded && (!Workers.enabled() || now - cued[kind] < CUE_TICKS)) {
			return false;
		}
		cued[kind] = now;
		SoundEvent sound = SOUNDS.get(body.spirit() + "." + id);
		if (sound != null) {
			level.playSound(null, this, sound, SoundSource.NEUTRAL, 1.0F, 1.0F);
		}
		shown.addLast(id);
		while (shown.size() > SHOWN) {
			shown.removeFirst();
		}
		return true;
	}

	/**
	 * The gesture with which it answers a pact screen's action (PactMenu.receive through PactPerchBlockEntity.react), from its
	 * answer; null for none. Each spirit's own table (Stolas: StolasEntity.reaction).
	 */
	protected abstract @Nullable Clip answer(int action, String reason, boolean done);

	/** The worker's main controller (its loops), then its gestures' over it: triggered only, it draws nothing between them. */
	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
		super.registerControllers(controllers);
		AnimationController<T> gestures = new AnimationController<T>(GESTURE_CONTROLLER, 0, test -> PlayState.STOP);
		for (Clip clip : body.gestures()) {
			gestures.triggerableAnim(clip.id(), clip.animation());
		}
		controllers.add(gestures);
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

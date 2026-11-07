package io.github.jimbozoomer.jugcraft.concordance.spirits;

import com.geckolib.animation.RawAnimation;
import io.github.jimbozoomer.jugcraft.concordance.worker.Status;
import java.util.List;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/**
 * Concordance garden cohort, GC-07 (Dossier 4; docs/features/foras-pact.md): Foras, the thirty-first spirit, a tall stooped
 * elder in a robe of bark and moss who stands beside his perch with the owner's acorn cap on his head. Everything a pact
 * spirit does is PactSpiritEntity's (his visit, his pact's presence, his departure and the sweep, his cues and gestures,
 * his saved perch and record, whom he faces, and his standing station beside the perch: Station.BESIDE, unit 3's GC-P67,
 * with no station code of his own); here are only his own tables (tools/concordance_pacts.py SPIRITS["foras"]): his
 * counterpart, his sign, his sound events, his loops and gestures, and the gesture with which he answers each action. His
 * service is graft keeping (GraftKeeping): a host crop grafted with a donor from the store beside him, one graft a think at
 * most.
 * <p>
 * His two details. (a) His acorn cap tips on its own at his greeting and at each graft, and only then (the cap bone in
 * his arrive and work clips). (b) He counts aloud: after each graft the holder hears how many he has made today against
 * his quota, and the kept page's gauge says it (GraftKeeping.countLine, rows).
 * <p>
 * His body (GC-S12, GC-P92): built by tools/garden_bodies.py, painted from the owner's fir log and Spanish moss, the
 * owner's acorn cap (the second edited part's jims mobs armour) on his head. His main controller plays a {@link Loop} for
 * what the server synced: stooped and breathing while he serves or idles; leaning to his speaker, a hand on his satchel's
 * strap, while a conversation is open at his perch; arms folded behind him, head bowed, while he waits; bent low while
 * his pact is suspended; tapping his satchel while an offering is due. His {@link Gesture}s play over it on the base's
 * gesture controller; his work is fired by his service toward the host he grafts.
 */
public class ForasEntity extends PactSpiritEntity<ForasEntity> {
	/** His pacts' counterpart (the pact definition's id, his entity id). */
	public static final String COUNTERPART = "jugcraft:foras";
	/** His sign: an acorn, what a long keeping starts from (tools/concordance_pacts.py SPIRITS; GC-P91). */
	public static final String SIGN = "jugcraft:acorn";
	/** His sound events, jugcraft:foras.<id>, in tools/concordance_pacts.py SOUND_EVENTS' order: each gesture's and his listening's. */
	public static final List<String> SOUND_IDS = List.of("arrive", "listen", "inspect", "assent", "refuse", "depart", "work");

	/** His loops on the worker's main controller, for what the server synced (SPIRITS["foras"]["body"]["loops"]). */
	public enum Loop {
		IDLE(RawAnimation.begin().thenLoop("animation.foras.idle")),
		LISTEN(RawAnimation.begin().thenLoop("animation.foras.listen")),
		WAITING(RawAnimation.begin().thenLoop("animation.foras.waiting")),
		SUSPENDED(RawAnimation.begin().thenLoop("animation.foras.suspended")),
		ASTIR(RawAnimation.begin().thenLoop("animation.foras.astir"));

		final RawAnimation animation;

		Loop(RawAnimation animation) {
			this.animation = animation;
		}
	}

	/** His gestures: each a one-shot clip on the gesture controller, its length in ticks (SPIRITS["foras"]["body"]["gesture_times"]). */
	public enum Gesture implements PactSpiritEntity.Clip {
		ARRIVE("arrive", 40, RawAnimation.begin().thenPlay("animation.foras.arrive")),
		INSPECT("inspect", 40, RawAnimation.begin().thenPlay("animation.foras.inspect")),
		ASSENT("assent", 36, RawAnimation.begin().thenPlay("animation.foras.assent")),
		REFUSE("refuse", 40, RawAnimation.begin().thenPlay("animation.foras.refuse")),
		DEPART("depart", 52, RawAnimation.begin().thenPlayAndHold("animation.foras.depart")),
		WORK("work", 48, RawAnimation.begin().thenPlay("animation.foras.work"));

		/** Its name on the gesture controller (what the server triggers). */
		public final String id;
		public final int ticks;
		public final RawAnimation animation;

		Gesture(String id, int ticks, RawAnimation animation) {
			this.id = id;
			this.ticks = ticks;
			this.animation = animation;
		}

		/** The clip it plays (its name in foras.animation.json). */
		public String clip() {
			return animation.getAnimationStages().getFirst().animationName();
		}

		@Override
		public String id() {
			return id;
		}

		@Override
		public int ticks() {
			return ticks;
		}

		@Override
		public RawAnimation animation() {
			return animation;
		}
	}

	/** His body for the base: sounds jugcraft:foras.<cue>, his six gestures, arriving and departing with his own, standing beside his perch. */
	static final Body BODY = new Body("foras", SOUND_IDS, List.of(Gesture.values()), Gesture.ARRIVE, Gesture.DEPART, Station.BESIDE);

	public ForasEntity(EntityType<? extends PathfinderMob> type, Level level) {
		super(type, level, BODY);
	}

	/** Registers his sound events once (Workers.register); sounds.json and their subtitles are tools/concordance_pacts.py's. */
	public static void registerSounds() {
		PactSpiritEntity.registerSounds(BODY);
	}

	/**
	 * The gesture with which he answers a pact screen's action, from its answer: an acceptance sealed, assent; refused (no
	 * store, no host, the compact short, not the owner), refuse (he objects); an offering paid ahead, inspect (he weighs it);
	 * a resumption, assent; his matter answered (RESOLVE), assent. None for the menu's own "not offered", hearing his terms
	 * (he listens), an invitation (he arrives), declining and ending (he departs), suspending (his loop shows it) or seating
	 * (he arrives). He has no on-request action, so a recall is never his.
	 */
	public static @Nullable Gesture reaction(int action, String reason, boolean done) {
		if (PactMenu.NOT_OFFERED.equals(reason)) {
			return null;
		}
		return switch (action) {
			case PactRequestPayload.ACCEPT -> done ? Gesture.ASSENT : Gesture.REFUSE;
			case PactRequestPayload.DEPOSIT -> done ? Gesture.INSPECT : null;
			case PactRequestPayload.RESUME -> done ? Gesture.ASSENT : null;
			case PactRequestPayload.RESOLVE -> done ? Gesture.ASSENT : null;
			default -> null;
		};
	}

	@Override
	protected @Nullable Clip answer(int action, String reason, boolean done) {
		return reaction(action, reason, done);
	}

	/**
	 * His loop for {@code status}: listening over all else unless switched off (his day's grafts spent, FINISHED, he still
	 * listens to whoever speaks: SF A-06's rule); waiting whenever he is off his service for a reason that is not his pact's
	 * own state; never a claim of work he is not doing. WORKING, and a matter awaiting its answer (he works on meanwhile,
	 * under the safe course), play his idle; his work gesture plays over it when his service fires it.
	 */
	public static Loop loop(Status status, boolean listening) {
		if (status == Status.DISABLED) {
			return Loop.WAITING;
		}
		if (listening) {
			return Loop.LISTEN;
		}
		return switch (status) {
			case UPKEEP_DUE -> Loop.ASTIR;
			case SUSPENDED, UPKEEP_HELD -> Loop.SUSPENDED;
			case FINISHED, OWNER_OFFLINE, OTHER_DIMENSION, OUTSIDE_AGREEMENT, DESTINATION_UNLOADED, BLOCKED_BY_ACCESS -> Loop.WAITING;
			default -> Loop.IDLE;
		};
	}

	@Override
	protected RawAnimation animation(Status status) {
		return loop(status, listening()).animation;
	}
}

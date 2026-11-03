package io.github.jimbozoomer.jugcraft.concordance.spirits;

import com.geckolib.animation.RawAnimation;
import io.github.jimbozoomer.jugcraft.concordance.worker.PactDefinition;
import io.github.jimbozoomer.jugcraft.concordance.worker.Status;
import java.util.List;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/**
 * Concordance Chunk D, D-04 (D-S7): Stolas, the pact hall's first spirit. Since the garden cohort (GC-01, SG2) everything a
 * perched pact spirit does is PactSpiritEntity's (his visit, his pact's presence, his departure and sweep, his cues and
 * gestures, his saved perch and record), unchanged; here are only his own tables: his counterpart, his sign, his sound
 * events, his loops and gestures (tools/concordance_pacts.py SPIRITS["stolas"]) and the gesture with which he answers each
 * action. His service is Star-reckoning (StarReckoning.SERVICE), which takes no autonomous step.
 * <p>
 * His body (D-06, D-S5): the owner's Flashjack rig in his raven form (tools/stolas_body.py). His main controller plays a
 * {@link Loop} for what the server synced: perched, listening while a conversation is open at his perch, astir while an
 * offering is due, hunched while his pact is suspended, waiting while he is off his service for any other reason; his
 * {@link Gesture}s play on the base's gesture controller over it.
 */
public class StolasEntity extends PactSpiritEntity<StolasEntity> {
	/** His pacts' counterpart (the pact definition's id). */
	public static final String COUNTERPART = PactDefinition.STOLAS;
	/** His sign: the Styx flower named for him, drawn by his name on the pact screen (tools/concordance_pacts.py SPIRITS; GC-01). */
	public static final String SIGN = "jugcraft:stolas_starflower";
	/** His sound events, jugcraft:stolas.<id>, in tools/concordance_pacts.py SOUND_EVENTS' order: each gesture's and his listening's. */
	public static final List<String> SOUND_IDS = List.of("arrive", "listen", "inspect", "assent", "refuse", "depart");

	/** His loops on the worker's main controller, for what the server synced (tools/concordance_pacts.py LOOPS). */
	public enum Loop {
		IDLE(RawAnimation.begin().thenLoop("animation.stolas.idle")),
		LISTEN(RawAnimation.begin().thenLoop("animation.stolas.listen")),
		WAITING(RawAnimation.begin().thenLoop("animation.stolas.waiting")),
		SUSPENDED(RawAnimation.begin().thenLoop("animation.stolas.suspended")),
		ASTIR(RawAnimation.begin().thenLoop("idle.flapping"));

		final RawAnimation animation;

		Loop(RawAnimation animation) {
			this.animation = animation;
		}
	}

	/** His gestures: each a one-shot clip on the gesture controller, its length in ticks (tools/concordance_pacts.py GESTURE_TIMES). */
	public enum Gesture implements PactSpiritEntity.Clip {
		ARRIVE("arrive", 40, RawAnimation.begin().thenPlay("animation.stolas.arrive")),
		INSPECT("inspect", 50, RawAnimation.begin().thenPlay("animation.stolas.inspect")),
		ASSENT("assent", 40, RawAnimation.begin().thenPlay("animation.stolas.assent")),
		REFUSE("refuse", 40, RawAnimation.begin().thenPlay("animation.stolas.refuse")),
		DEPART("depart", 40, RawAnimation.begin().thenPlayAndHold("animation.stolas.depart"));

		/** Its name on the gesture controller (what the server triggers). */
		public final String id;
		public final int ticks;
		public final RawAnimation animation;

		Gesture(String id, int ticks, RawAnimation animation) {
			this.id = id;
			this.ticks = ticks;
			this.animation = animation;
		}

		/** The clip it plays (its name in stolas.animation.json). */
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

	/** His body for the base: sounds jugcraft:stolas.<cue>, his five gestures, arriving and departing with his own. */
	static final Body BODY = new Body("stolas", SOUND_IDS, List.of(Gesture.values()), Gesture.ARRIVE, Gesture.DEPART);

	public StolasEntity(EntityType<? extends PathfinderMob> type, Level level) {
		super(type, level, BODY);
	}

	/** Registers his sound events once (Workers.register); sounds.json and their subtitles are tools/concordance_pacts.py's. */
	public static void registerSounds() {
		PactSpiritEntity.registerSounds(BODY);
	}

	/**
	 * The gesture with which he answers a pact screen's action (PactMenu.receive through PactPerchBlockEntity.react), from its
	 * answer: an acceptance sealed, assent; refused, refuse; a reckoning done, inspect (his astrolabe); refused, refuse; an
	 * offering paid ahead, inspect; a resumption, assent. None for the menu's own "not offered" ("changed" and "too fast"
	 * never reach an action), hearing his terms (he listens), an invitation (he arrives), declining and ending (he departs),
	 * suspending (his loop shows it) or seating (he arrives).
	 */
	public static @Nullable Gesture reaction(int action, String reason, boolean done) {
		if (PactMenu.NOT_OFFERED.equals(reason)) {
			return null;
		}
		return switch (action) {
			case PactRequestPayload.ACCEPT -> done ? Gesture.ASSENT : Gesture.REFUSE;
			case PactRequestPayload.RECALL -> done ? Gesture.INSPECT : Gesture.REFUSE;
			case PactRequestPayload.DEPOSIT -> done ? Gesture.INSPECT : null;
			case PactRequestPayload.RESUME -> done ? Gesture.ASSENT : null;
			default -> null;
		};
	}

	@Override
	protected @Nullable Clip answer(int action, String reason, boolean done) {
		return reaction(action, reason, done);
	}

	/** His loop for {@code status}: listening over all else while he serves; never a claim of work he is not doing. */
	public static Loop loop(Status status, boolean listening) {
		if (status == Status.DISABLED || status == Status.FINISHED) {
			return Loop.WAITING;
		}
		if (listening) {
			return Loop.LISTEN;
		}
		return switch (status) {
			case TRAVELLING, RETURNING, WORKING, UPKEEP_DUE -> Loop.ASTIR;
			case SUSPENDED, UPKEEP_HELD -> Loop.SUSPENDED;
			case OWNER_OFFLINE, OTHER_DIMENSION, OUTSIDE_AGREEMENT, DESTINATION_UNLOADED -> Loop.WAITING;
			default -> Loop.IDLE;
		};
	}

	@Override
	protected RawAnimation animation(Status status) {
		return loop(status, listening()).animation;
	}
}

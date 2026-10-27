package io.github.jimbozoomer.jugcraft.concordance.spirits;

import com.geckolib.animation.RawAnimation;
import io.github.jimbozoomer.jugcraft.concordance.worker.Status;
import java.util.List;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/**
 * Concordance garden cohort, GC-04 (Dossier 1; docs/features/buer-pact.md): Buer, the tenth spirit, rooted on his perch.
 * Everything a perched pact spirit does is PactSpiritEntity's (his visit, his pact's presence, his departure and the sweep,
 * his cues and gestures, his saved perch and record, whom he faces); here are only his own tables (tools/concordance_pacts.py
 * SPIRITS["buer"]): his counterpart, his sign, his sound events, his loops and gestures, and the gesture with which he
 * answers each action. His service is strain tending (StrainTending.SERVICE): by daylight, while his holder is near, one
 * bed's harvest strain eased a think at most, by the bed's own rule.
 * <p>
 * His body (GC-S12): the owner's Decor Blossom, its texture unchanged and its geometry and clips adapted
 * (tools/garden_bodies.py). His main controller plays a {@link Loop} for what the server synced: the owner's own "active"
 * while he serves or idles; open and leaning toward his speaker while a conversation is open at his perch; shut and
 * swaying while he waits (asleep outside his hours, his holder away, switched off); shut and drooping while his pact is
 * suspended; half open and quivering while an offering is due. His {@link Gesture}s play on the base's gesture controller
 * over it; his work is fired by his service on the bed it eases (StrainTending).
 */
public class BuerEntity extends PactSpiritEntity<BuerEntity> {
	/** His pacts' counterpart (the pact definition's id, his entity id). */
	public static final String COUNTERPART = "jugcraft:buer";
	/** His sign: the Mendvetch, the healing crop, drawn by his name on the pact screen (tools/concordance_pacts.py SPIRITS). */
	public static final String SIGN = "jugcraft:mendvetch";
	/** His sound events, jugcraft:buer.<id>, in tools/concordance_pacts.py SOUND_EVENTS' order: each gesture's and his listening's. */
	public static final List<String> SOUND_IDS = List.of("arrive", "listen", "inspect", "assent", "refuse", "depart", "work");

	/** His loops on the worker's main controller, for what the server synced (tools/concordance_pacts.py SPIRITS["buer"]["body"]["loops"]). */
	public enum Loop {
		IDLE(RawAnimation.begin().thenLoop("active")),
		LISTEN(RawAnimation.begin().thenLoop("animation.buer.listen")),
		WAITING(RawAnimation.begin().thenLoop("animation.buer.waiting")),
		SUSPENDED(RawAnimation.begin().thenLoop("animation.buer.suspended")),
		ASTIR(RawAnimation.begin().thenLoop("animation.buer.astir"));

		final RawAnimation animation;

		Loop(RawAnimation animation) {
			this.animation = animation;
		}
	}

	/** His gestures: each a one-shot clip on the gesture controller, its length in ticks (SPIRITS["buer"]["body"]["gesture_times"]). */
	public enum Gesture implements PactSpiritEntity.Clip {
		ARRIVE("arrive", 50, RawAnimation.begin().thenPlay("animation.buer.arrive")),
		INSPECT("inspect", 40, RawAnimation.begin().thenPlay("animation.buer.inspect")),
		ASSENT("assent", 40, RawAnimation.begin().thenPlay("animation.buer.assent")),
		REFUSE("refuse", 40, RawAnimation.begin().thenPlay("animation.buer.refuse")),
		DEPART("depart", 40, RawAnimation.begin().thenPlayAndHold("animation.buer.depart")),
		WORK("work", 60, RawAnimation.begin().thenPlay("animation.buer.work"));

		/** Its name on the gesture controller (what the server triggers). */
		public final String id;
		public final int ticks;
		public final RawAnimation animation;

		Gesture(String id, int ticks, RawAnimation animation) {
			this.id = id;
			this.ticks = ticks;
			this.animation = animation;
		}

		/** The clip it plays (its name in buer.animation.json). */
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

	/** His body for the base: sounds jugcraft:buer.<cue>, his six gestures, arriving and departing with his own. */
	static final Body BODY = new Body("buer", SOUND_IDS, List.of(Gesture.values()), Gesture.ARRIVE, Gesture.DEPART);

	public BuerEntity(EntityType<? extends PathfinderMob> type, Level level) {
		super(type, level, BODY);
	}

	/** Registers his sound events once (Workers.register); sounds.json and their subtitles are tools/concordance_pacts.py's. */
	public static void registerSounds() {
		PactSpiritEntity.registerSounds(BODY);
	}

	/**
	 * The gesture with which he answers a pact screen's action, from its answer: an acceptance sealed, assent; refused (the
	 * compact short, nothing to mend, not the owner), refuse; an offering paid ahead, inspect (he looks it over); a
	 * resumption, assent. None for the menu's own "not offered", hearing his terms (he listens), an invitation (he arrives),
	 * declining and ending (he departs), suspending (his loop shows it) or seating (he arrives). He has no on-request action
	 * (StrainTending offers none), so a recall is never his.
	 */
	public static @Nullable Gesture reaction(int action, String reason, boolean done) {
		if (PactMenu.NOT_OFFERED.equals(reason)) {
			return null;
		}
		return switch (action) {
			case PactRequestPayload.ACCEPT -> done ? Gesture.ASSENT : Gesture.REFUSE;
			case PactRequestPayload.DEPOSIT -> done ? Gesture.INSPECT : null;
			case PactRequestPayload.RESUME -> done ? Gesture.ASSENT : null;
			default -> null;
		};
	}

	@Override
	protected @Nullable Clip answer(int action, String reason, boolean done) {
		return reaction(action, reason, done);
	}

	/**
	 * His loop for {@code status}: listening over all else unless switched off (his day's quota spent, FINISHED, he still
	 * listens to whoever speaks); asleep (shut) whenever he is off his service for a reason that is not his pact's own
	 * state; never a claim of work he is not doing. WORKING plays his idle under the work gesture his service fired.
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

package io.github.jimbozoomer.jugcraft.concordance.spirits;

import com.geckolib.animation.RawAnimation;
import io.github.jimbozoomer.jugcraft.concordance.worker.Status;
import java.util.List;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/**
 * Concordance garden cohort, GC-06 (Dossier 3; docs/features/marax-pact.md): Marax, the twenty-first spirit, a great
 * bull with a calm man's face, and his familiar, who knows the ground. Everything a pact spirit does is PactSpiritEntity's
 * (his visit, his pact's presence, his departure and the sweep, his cues and gestures, his saved perch and record, whom he
 * faces, his station); here are only his tables (tools/concordance_pacts.py SPIRITS["marax"]): his counterpart, his sign,
 * his sound events, his loops and gestures and the gesture with which he answers each action. He stands on the ground
 * beside his perch (Station.BESIDE). His service is habitat reading (HabitatReading.SERVICE), given on request; he takes no
 * autonomous step.
 * <p>
 * His body (GC-S12): built by tools/garden_bodies.py, painted from the owner's mahogany log, Spanish moss and cypress
 * leaves. His main controller plays a {@link Loop} for what the server synced, and by night, idle, he lifts his head to the
 * sky (Dossier 3 item 7a; the client's own sky, cosmetic, never a status). His familiar is a bone group of its own, a root
 * child; when a reading names a bed, the client's renderer flies it there (PactSpiritRenderers, its row's "reach").
 */
public class MaraxEntity extends PactSpiritEntity<MaraxEntity> {
	/** His pacts' counterpart (the pact definition's id, his entity id). */
	public static final String COUNTERPART = "jugcraft:marax";
	/** His sign: a book and quill, the astronomy and liberal sciences of his entry, drawn by his name on the pact screen (GC-P63). */
	public static final String SIGN = "minecraft:writable_book";
	/** His sound events, jugcraft:marax.<id>, in tools/concordance_pacts.py SOUND_EVENTS' order: each gesture's and his listening's. */
	public static final List<String> SOUND_IDS = List.of("arrive", "listen", "inspect", "assent", "refuse", "depart", "work");

	/** His loops on the worker's main controller, for what the server synced (SPIRITS["marax"]["body"]["loops"]). */
	public enum Loop {
		IDLE(RawAnimation.begin().thenLoop("animation.marax.idle")),
		LISTEN(RawAnimation.begin().thenLoop("animation.marax.listen")),
		WAITING(RawAnimation.begin().thenLoop("animation.marax.waiting")),
		SUSPENDED(RawAnimation.begin().thenLoop("animation.marax.suspended")),
		ASTIR(RawAnimation.begin().thenLoop("animation.marax.astir")),
		STARGAZE(RawAnimation.begin().thenLoop("animation.marax.stargaze"));

		final RawAnimation animation;

		Loop(RawAnimation animation) {
			this.animation = animation;
		}
	}

	/** His gestures: each a one-shot clip on the gesture controller, its length in ticks (SPIRITS["marax"]["body"]["gesture_times"]). */
	public enum Gesture implements PactSpiritEntity.Clip {
		ARRIVE("arrive", 40, RawAnimation.begin().thenPlay("animation.marax.arrive")),
		INSPECT("inspect", 40, RawAnimation.begin().thenPlay("animation.marax.inspect")),
		ASSENT("assent", 30, RawAnimation.begin().thenPlay("animation.marax.assent")),
		REFUSE("refuse", 32, RawAnimation.begin().thenPlay("animation.marax.refuse")),
		DEPART("depart", 40, RawAnimation.begin().thenPlayAndHold("animation.marax.depart")),
		WORK("work", 40, RawAnimation.begin().thenPlay("animation.marax.work"));

		/** Its name on the gesture controller (what the server triggers). */
		public final String id;
		public final int ticks;
		public final RawAnimation animation;

		Gesture(String id, int ticks, RawAnimation animation) {
			this.id = id;
			this.ticks = ticks;
			this.animation = animation;
		}

		/** The clip it plays (its name in marax.animation.json). */
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

	/** His body for the base: sounds jugcraft:marax.<cue>, his six gestures, arriving and departing with his own, standing beside his perch. */
	static final Body BODY = new Body("marax", SOUND_IDS, List.of(Gesture.values()), Gesture.ARRIVE, Gesture.DEPART, Station.BESIDE);

	public MaraxEntity(EntityType<? extends PathfinderMob> type, Level level) {
		super(type, level, BODY);
	}

	/** Registers his sound events once (Workers.register); sounds.json and their subtitles are tools/concordance_pacts.py's. */
	public static void registerSounds() {
		PactSpiritEntity.registerSounds(BODY);
	}

	/**
	 * The gesture with which he answers a pact screen's action (PactMenu.receive through PactPerchBlockEntity.react): an
	 * acceptance sealed, assent; refused, refuse; a reading given, work (he watches his familiar go); every crop thriving,
	 * assent; a reading refused, refuse; an offering paid ahead, inspect; a resumption, assent. None for the menu's own "not
	 * offered", hearing his terms (he listens), an invitation (he arrives), declining and ending (he departs), suspending
	 * (his loop shows it), seating (he arrives) or a matter's answer (he raises none).
	 */
	public static @Nullable Gesture reaction(int action, String reason, boolean done) {
		if (PactMenu.NOT_OFFERED.equals(reason)) {
			return null;
		}
		return switch (action) {
			case PactRequestPayload.ACCEPT -> done ? Gesture.ASSENT : Gesture.REFUSE;
			case PactRequestPayload.RECALL -> done ? Gesture.WORK : HabitatReading.NOTHING_TO_CORRECT.equals(reason) ? Gesture.ASSENT : Gesture.REFUSE;
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
	 * His loop for {@code status}: listening over all else but switched off (as Buer's, unit 1 A-06); by {@code night},
	 * idle, his look at the sky; never a claim of work he is not doing.
	 */
	public static Loop loop(Status status, boolean listening, boolean night) {
		if (status == Status.DISABLED) {
			return Loop.WAITING;
		}
		if (listening) {
			return Loop.LISTEN;
		}
		return switch (status) {
			case FINISHED -> Loop.WAITING;
			case UPKEEP_DUE -> Loop.ASTIR;
			case SUSPENDED, UPKEEP_HELD -> Loop.SUSPENDED;
			case OWNER_OFFLINE, OTHER_DIMENSION, OUTSIDE_AGREEMENT, DESTINATION_UNLOADED, BLOCKED_BY_ACCESS -> Loop.WAITING;
			default -> night ? Loop.STARGAZE : Loop.IDLE;
		};
	}

	@Override
	protected RawAnimation animation(Status status) {
		return loop(status, listening(), level().isDarkOutside()).animation;
	}
}

package io.github.jimbozoomer.jugcraft.concordance.spirits;

import com.geckolib.animation.RawAnimation;
import io.github.jimbozoomer.jugcraft.concordance.sign.Sign;
import io.github.jimbozoomer.jugcraft.concordance.sign.Signs;
import io.github.jimbozoomer.jugcraft.concordance.worker.Status;
import java.util.ArrayDeque;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/**
 * Concordance garden cohort, GC-08 (Dossier 5; docs/features/bifrons-pact.md): Bifrons, the forty-sixth spirit, a low
 * mound of roots studded with stones that rises into a lean man of roots while spoken to. Everything a pact spirit does is
 * PactSpiritEntity's (his visit, his pact's presence, his departure and the sweep, his cues and gestures, his saved perch
 * and record, whom he faces, his station); here are only his own tables (tools/concordance_pacts.py SPIRITS["bifrons"]):
 * his counterpart, his sign, his sound events, his loops and gestures, the gesture with which he answers each action, and
 * his measuring marks. He stands on the ground beside his perch (Station.BESIDE). His service is root-running
 * (RootRunning): by night, two nutrients from a full bed for one in a hungry one, measured first.
 * <p>
 * His two shapes (detail a): his main controller plays a {@link Loop} for what the server synced. Low, he breathes as a
 * mound (idle), lies still with his roots drawn in (waiting: by day, his holder away, switched off), sags (suspended) or
 * twitches (astir: an offering due). While a conversation is open at his perch he rises: his listening loop plays his
 * rise once, then his risen pose; when it closes the main controller's blend sinks him back. His {@link Gesture}s play on
 * the base's gesture controller over it.
 * <p>
 * His marks (detail b): before each run his service calls {@link #mark} at the source, then a beat later at the
 * destination: the Concordance's shared preparation sign at the bed ({@link #MARK}: every client draws it in its own
 * particles, as its settings allow), never particles of his own, and nothing shown between the two.
 */
public class BifronsEntity extends PactSpiritEntity<BifronsEntity> {
	/** His pacts' counterpart (the pact definition's id, his entity id). */
	public static final String COUNTERPART = "jugcraft:bifrons";
	/** His sign: hanging roots (tools/concordance_pacts.py SPIRITS; GC-P107). */
	public static final String SIGN = "minecraft:hanging_roots";
	/** His sound events, jugcraft:bifrons.<id>, in tools/concordance_pacts.py SOUND_EVENTS' order: each gesture's and his listening's. */
	public static final List<String> SOUND_IDS = List.of("arrive", "listen", "inspect", "assent", "refuse", "depart", "work");
	/** Detail (b): the sign each bed of a measure shows, a place being readied: glyphs drawn in to it (SPIRITS["bifrons"]["body"]["mark"]). */
	public static final Sign MARK = Sign.GATHER;
	/** How many marks he remembers (a run's two). */
	private static final int MARKS = 2;

	/** His loops on the worker's main controller, for what the server synced (SPIRITS["bifrons"]["body"]["loops"]). */
	public enum Loop {
		IDLE(RawAnimation.begin().thenLoop("animation.bifrons.idle")),
		LISTEN(RawAnimation.begin().thenPlay("animation.bifrons.rise").thenLoop("animation.bifrons.listen")),
		WAITING(RawAnimation.begin().thenLoop("animation.bifrons.waiting")),
		SUSPENDED(RawAnimation.begin().thenLoop("animation.bifrons.suspended")),
		ASTIR(RawAnimation.begin().thenLoop("animation.bifrons.astir"));

		final RawAnimation animation;

		Loop(RawAnimation animation) {
			this.animation = animation;
		}
	}

	/** His gestures: each a one-shot clip on the gesture controller, its length in ticks (SPIRITS["bifrons"]["body"]["gesture_times"]). */
	public enum Gesture implements PactSpiritEntity.Clip {
		ARRIVE("arrive", 40, RawAnimation.begin().thenPlay("animation.bifrons.arrive")),
		INSPECT("inspect", 40, RawAnimation.begin().thenPlay("animation.bifrons.inspect")),
		ASSENT("assent", 30, RawAnimation.begin().thenPlay("animation.bifrons.assent")),
		REFUSE("refuse", 32, RawAnimation.begin().thenPlay("animation.bifrons.refuse")),
		DEPART("depart", 30, RawAnimation.begin().thenPlayAndHold("animation.bifrons.depart")),
		WORK("work", 40, RawAnimation.begin().thenPlay("animation.bifrons.work"));

		/** Its name on the gesture controller (what the server triggers). */
		public final String id;
		public final int ticks;
		public final RawAnimation animation;

		Gesture(String id, int ticks, RawAnimation animation) {
			this.id = id;
			this.ticks = ticks;
			this.animation = animation;
		}

		/** The clip it plays (its name in bifrons.animation.json). */
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

	/** One mark of a measure as he showed it: the game time, the bed, and whether it was a run's source (memory only: tests and evidence read it). */
	public record Mark(long at, BlockPos pos, boolean source) {
	}

	/** His body for the base: sounds jugcraft:bifrons.<cue>, his six gestures, arriving and departing with his own, standing beside his perch. */
	static final Body BODY = new Body("bifrons", SOUND_IDS, List.of(Gesture.values()), Gesture.ARRIVE, Gesture.DEPART, Station.BESIDE);

	/** His last marks, oldest first; never saved. */
	private final ArrayDeque<Mark> marks = new ArrayDeque<>(MARKS);

	public BifronsEntity(EntityType<? extends PathfinderMob> type, Level level) {
		super(type, level, BODY);
	}

	/** Registers his sound events once (Workers.register); sounds.json and their subtitles are tools/concordance_pacts.py's. */
	public static void registerSounds() {
		PactSpiritEntity.registerSounds(BODY);
	}

	/**
	 * The gesture with which he answers a pact screen's action, from its answer: an acceptance sealed, assent; refused
	 * (fewer than two beds, the compact short, not the owner), refuse; an offering paid ahead, inspect (he measures it); a
	 * resumption, assent; his matter answered (RESOLVE), assent. None for the menu's own "not offered", hearing his terms (he
	 * rises), an invitation (he arrives), declining and ending (he departs), suspending (his loop shows it) or seating (he
	 * arrives). He has no on-request action, so a recall is never his.
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
	 * His loop for {@code status}: risen over all else while spoken to, unless switched off (his runs spent, FINISHED, he
	 * still rises for whoever speaks: SF A-06's rule); low and still whenever he is off his service for a reason that is
	 * not his pact's own state (by day among them); never a claim of work he is not doing. WORKING and a matter awaiting its
	 * answer play his idle; his inspect and work gestures play over it when his service fires them.
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

	/**
	 * Detail (b), called by his service at each beat of a measure: the shared preparation sign at the bed at {@code bed}
	 * (Signs.show, once; each client draws it as its settings allow), remembered at game time {@code now} with whether it
	 * was the run's source. Nothing is shown between two beds.
	 */
	public void mark(ServerLevel level, BlockPos bed, boolean source, long now) {
		Signs.show(level, bed, MARK);
		if (marks.size() == MARKS) {
			marks.removeFirst();
		}
		marks.addLast(new Mark(now, bed.immutable(), source));
	}

	/** His last two marks, oldest first; memory only. */
	public List<Mark> marks() {
		return List.copyOf(marks);
	}
}

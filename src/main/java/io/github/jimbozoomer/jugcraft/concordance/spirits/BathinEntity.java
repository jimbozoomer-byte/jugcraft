package io.github.jimbozoomer.jugcraft.concordance.spirits;

import com.geckolib.animation.RawAnimation;
import io.github.jimbozoomer.jugcraft.concordance.sign.Sign;
import io.github.jimbozoomer.jugcraft.concordance.sign.Signs;
import io.github.jimbozoomer.jugcraft.concordance.worker.Status;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/**
 * Concordance garden cohort, GC-05 (Dossier 2; docs/features/bathin-pact.md): Bathin, the eighteenth spirit, coiled on his
 * perch. Everything a perched pact spirit does is PactSpiritEntity's (his visit, his pact's presence, his departure and the
 * sweep, his cues and gestures, his saved perch and record, whom he faces); here are only his own tables
 * (tools/concordance_pacts.py SPIRITS["bathin"]): his counterpart, his sign, his sound events, his loops and gestures, the
 * gesture with which he answers each action, and his two details. His service is Verdance carrying (VerdanceCarrying):
 * Verdance moved at once from his holder's Hearts to a far Gleaner or a pylon away from them, one move a think at most.
 * <p>
 * His two details. He never travels: he is whole on his perch at once and gone from it at once, each time in a ring of dust
 * ({@link #gestured}, the base's hook, with his arriving and departing gestures), and never anywhere between. When he carries,
 * both reservoirs shimmer in the same tick ({@link #shimmer}, called by his service in the step that moved the Verdance)
 * and nothing crosses between them: no sign is ever shown anywhere but at the two ends. Both are the Concordance's shared
 * signs (Signs.show: every client draws them in its own particles, as its settings allow), never particles of his own.
 * <p>
 * His body (GC-S12): built by tools/garden_bodies.py, painted from the owner's willow log and Spanish moss. His main
 * controller plays a {@link Loop} for what the server synced: hovering at rest while he serves or idles; arms folded and
 * head inclined while a conversation is open at his perch; arms folded, head turned away and tail tapping while he waits;
 * slumped while his pact is suspended; drumming and lashing while an offering is due. His {@link Gesture}s play on the
 * base's gesture controller over it; his work is fired by his service at the reservoir it fills.
 */
public class BathinEntity extends PactSpiritEntity<BathinEntity> {
	/** His pacts' counterpart (the pact definition's id, his entity id). */
	public static final String COUNTERPART = "jugcraft:bathin";
	/** His sign: the ender pearl, which goes from one place to another at once (tools/concordance_pacts.py SPIRITS; GC-P58). */
	public static final String SIGN = "minecraft:ender_pearl";
	/** His sound events, jugcraft:bathin.<id>, in tools/concordance_pacts.py SOUND_EVENTS' order: each gesture's and his listening's. */
	public static final List<String> SOUND_IDS = List.of("arrive", "listen", "inspect", "assent", "refuse", "depart", "work");
	/** Detail (a): the sign his ring of dust is shown as, at his foot: a movement's puffs of cloud (SPIRITS["bathin"]["body"]["ring"]). */
	public static final Sign RING = Sign.SHOVE;
	/** Detail (b): the sign each end of a carry shows, where work was done: light rising (SPIRITS["bathin"]["body"]["shimmer"]). */
	public static final Sign SHIMMER = Sign.WORK;

	/** His loops on the worker's main controller, for what the server synced (SPIRITS["bathin"]["body"]["loops"]). */
	public enum Loop {
		IDLE(RawAnimation.begin().thenLoop("animation.bathin.idle")),
		LISTEN(RawAnimation.begin().thenLoop("animation.bathin.listen")),
		WAITING(RawAnimation.begin().thenLoop("animation.bathin.waiting")),
		SUSPENDED(RawAnimation.begin().thenLoop("animation.bathin.suspended")),
		ASTIR(RawAnimation.begin().thenLoop("animation.bathin.astir"));

		final RawAnimation animation;

		Loop(RawAnimation animation) {
			this.animation = animation;
		}
	}

	/** His gestures: each a one-shot clip on the gesture controller, its length in ticks (SPIRITS["bathin"]["body"]["gesture_times"]). */
	public enum Gesture implements PactSpiritEntity.Clip {
		ARRIVE("arrive", 30, RawAnimation.begin().thenPlay("animation.bathin.arrive")),
		INSPECT("inspect", 40, RawAnimation.begin().thenPlay("animation.bathin.inspect")),
		ASSENT("assent", 30, RawAnimation.begin().thenPlay("animation.bathin.assent")),
		REFUSE("refuse", 32, RawAnimation.begin().thenPlay("animation.bathin.refuse")),
		DEPART("depart", 20, RawAnimation.begin().thenPlayAndHold("animation.bathin.depart")),
		WORK("work", 30, RawAnimation.begin().thenPlay("animation.bathin.work"));

		/** Its name on the gesture controller (what the server triggers). */
		public final String id;
		public final int ticks;
		public final RawAnimation animation;

		Gesture(String id, int ticks, RawAnimation animation) {
			this.id = id;
			this.ticks = ticks;
			this.animation = animation;
		}

		/** The clip it plays (its name in bathin.animation.json). */
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

	/** One carry's shimmer as he showed it: the game time and its two reservoirs (memory only: tests and evidence read it). */
	public record Shimmer(long at, BlockPos from, BlockPos to) {
	}

	/** His body for the base: sounds jugcraft:bathin.<cue>, his six gestures, arriving and departing with his own. */
	static final Body BODY = new Body("bathin", SOUND_IDS, List.of(Gesture.values()), Gesture.ARRIVE, Gesture.DEPART);

	/** How many rings of dust he has shown since he was made or loaded; never saved (tests and evidence read it). */
	private int rings;
	/** His last carry's shimmer; never saved. */
	private @Nullable Shimmer shimmered;

	public BathinEntity(EntityType<? extends PathfinderMob> type, Level level) {
		super(type, level, BODY);
	}

	/** Registers his sound events once (Workers.register); sounds.json and their subtitles are tools/concordance_pacts.py's. */
	public static void registerSounds() {
		PactSpiritEntity.registerSounds(BODY);
	}

	/**
	 * The gesture with which he answers a pact screen's action, from its answer: an acceptance sealed, assent; refused
	 * (nothing to carry, the compact short, not the owner), refuse; an offering paid ahead, inspect (he weighs it); a
	 * resumption, assent; his matter answered (RESOLVE), assent. None for the menu's own "not offered", hearing his terms (he
	 * listens), an invitation (he arrives), declining and ending (he departs), suspending (his loop shows it) or seating (he
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
	 * His loop for {@code status}: listening over all else unless switched off (his day's moves spent, FINISHED, he still
	 * listens to whoever speaks: SF A-06's rule); waiting whenever he is off his service for a reason that is not his pact's
	 * own state; never a claim of work he is not doing. WORKING, and a matter awaiting its answer (he works on meanwhile),
	 * play his idle; his work gesture plays over it when his service fires it.
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

	/** Detail (a): his arrival and his departure each show in a ring of dust at his perch, and nowhere else. */
	@Override
	protected void gestured(ServerLevel level, Clip clip) {
		if (clip == Gesture.ARRIVE || clip == Gesture.DEPART) {
			ring(level);
		}
	}

	/** A ring of dust round his foot: the shared {@link #RING} sign there, once (each client draws it as its settings allow). */
	private void ring(ServerLevel level) {
		Signs.show(level, position().add(0.0, 0.1, 0.0), RING, null);
		rings++;
	}

	/**
	 * Detail (b), called by his service in the server step that moved Verdance from {@code from} to {@code to}: both shimmer
	 * now (the shared {@link #SHIMMER} sign at each), and nothing is shown between them (never Sign.FLOW, a sign that
	 * travels: the Verdance is never on the way).
	 */
	public void shimmer(ServerLevel level, BlockPos from, BlockPos to) {
		for (BlockPos end : List.of(from, to)) {
			Signs.show(level, end, SHIMMER);
		}
		shimmered = new Shimmer(level.getGameTime(), from.immutable(), to.immutable());
	}

	/** How many rings of dust he has shown since he was made or loaded; memory only. */
	public int rings() {
		return rings;
	}

	/** His last carry's shimmer, or null; memory only. */
	public @Nullable Shimmer lastShimmer() {
		return shimmered;
	}
}

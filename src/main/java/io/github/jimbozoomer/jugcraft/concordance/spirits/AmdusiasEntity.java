package io.github.jimbozoomer.jugcraft.concordance.spirits;

import com.geckolib.animation.RawAnimation;
import io.github.jimbozoomer.jugcraft.concordance.sign.Sign;
import io.github.jimbozoomer.jugcraft.concordance.sign.Signs;
import io.github.jimbozoomer.jugcraft.concordance.worker.Status;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/**
 * Concordance garden cohort, GC-09 (Dossier 6; docs/features/amdusias-pact.md): Amdusias, the sixty-seventh spirit, a tall
 * white unicorn whose horn is a living branch, standing beside his perch (Station.BESIDE, unit 3's GC-P67, with no station
 * code of his own). Everything a pact spirit does is PactSpiritEntity's (his visit, his pact's presence, his departure and
 * the sweep, his cues and gestures, his saved perch and record, whom he faces, his station); here are only his own tables
 * (tools/concordance_pacts.py SPIRITS["amdusias"]): his counterpart, his sign, his sound events, his loops and gestures,
 * the gesture with which he answers each action, his horn's beat and his tree's sway. His service is the bending canopy
 * (CanopyShelter).
 * <p>
 * His two details. (a) His horn is heard a beat late: the cues his horn sounds ({@link #HORNS}) sound {@link #HORN_BEAT}
 * ticks after their gesture ({@link #cueBeat}, the base's), never with it, subtitled "A distant horn"; his whistles sound
 * with theirs. (b) His tree sways: while he shelters, every CanopyShelter.SWAY_TICKS, and once as he departs (bowing his
 * horn: {@link #gestured}), the shared {@link #SWAY} sign shows at the top of the log beside his perch ({@link #sway}),
 * which every client draws in its own particles, as its settings allow; never particles of his own. Neither changes
 * anything.
 * <p>
 * His body (GC-S12): built by tools/garden_bodies.py, painted from the owner's white petals, origin oak log and leaves, the
 * owner's flower crown his garland. His main controller plays a {@link Loop} for what the server synced; his
 * {@link Gesture}s play on the base's gesture controller over it.
 */
public class AmdusiasEntity extends PactSpiritEntity<AmdusiasEntity> {
	/** His pacts' counterpart (the pact definition's id, his entity id). */
	public static final String COUNTERPART = "jugcraft:amdusias";
	/** His sign: an oak sapling, the tree he will bend (tools/concordance_pacts.py SPIRITS; GC-P132). */
	public static final String SIGN = "minecraft:oak_sapling";
	/** His sound events, jugcraft:amdusias.<id>, in tools/concordance_pacts.py SOUND_EVENTS' order: each gesture's and his listening's. */
	public static final List<String> SOUND_IDS = List.of("arrive", "listen", "inspect", "assent", "refuse", "depart", "work");
	/** Detail (a): how many ticks after its gesture his horn sounds (a beat; less than his departure, Gesture.DEPART). */
	public static final int HORN_BEAT = 20;
	/** The cues his horn sounds (menagerie/pocket/horn_fade); the rest are his whistles, heard with their gestures. */
	public static final Set<String> HORNS = Set.of("arrive", "work", "depart");
	/**
	 * Detail (b): the sign his tree's sway shows at the log's top: work being done (execution), as Bathin's shimmer and a
	 * bed's reading show it; never an effect mark, which says an effect was applied (SPIRITS["amdusias"]["body"]["sway"]).
	 */
	public static final Sign SWAY = Sign.WORK;

	/** His loops on the worker's main controller, for what the server synced (SPIRITS["amdusias"]["body"]["loops"]). */
	public enum Loop {
		IDLE(RawAnimation.begin().thenLoop("animation.amdusias.idle")),
		LISTEN(RawAnimation.begin().thenLoop("animation.amdusias.listen")),
		WAITING(RawAnimation.begin().thenLoop("animation.amdusias.waiting")),
		SUSPENDED(RawAnimation.begin().thenLoop("animation.amdusias.suspended")),
		ASTIR(RawAnimation.begin().thenLoop("animation.amdusias.astir"));

		final RawAnimation animation;

		Loop(RawAnimation animation) {
			this.animation = animation;
		}
	}

	/** His gestures: each a one-shot clip on the gesture controller, its length in ticks (SPIRITS["amdusias"]["body"]["gesture_times"]). */
	public enum Gesture implements PactSpiritEntity.Clip {
		ARRIVE("arrive", 50, RawAnimation.begin().thenPlay("animation.amdusias.arrive")),
		INSPECT("inspect", 40, RawAnimation.begin().thenPlay("animation.amdusias.inspect")),
		ASSENT("assent", 36, RawAnimation.begin().thenPlay("animation.amdusias.assent")),
		REFUSE("refuse", 40, RawAnimation.begin().thenPlay("animation.amdusias.refuse")),
		DEPART("depart", 40, RawAnimation.begin().thenPlayAndHold("animation.amdusias.depart")),
		WORK("work", 60, RawAnimation.begin().thenPlay("animation.amdusias.work"));

		/** Its name on the gesture controller (what the server triggers). */
		public final String id;
		public final int ticks;
		public final RawAnimation animation;

		Gesture(String id, int ticks, RawAnimation animation) {
			this.id = id;
			this.ticks = ticks;
			this.animation = animation;
		}

		/** The clip it plays (its name in amdusias.animation.json). */
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

	/** One sway of his tree as he showed it: the game time and the log (memory only: tests and evidence read it). */
	public record Sway(long at, BlockPos tree) {
	}

	/** His body for the base: sounds jugcraft:amdusias.<cue>, his six gestures, arriving and departing with his own, standing beside his perch. */
	static final Body BODY = new Body("amdusias", SOUND_IDS, List.of(Gesture.values()), Gesture.ARRIVE, Gesture.DEPART, Station.BESIDE);

	/** How many times his tree has swayed since he was made or loaded; never saved. */
	private int sways;
	/** His tree's last sway; never saved. */
	private @Nullable Sway swayed;

	public AmdusiasEntity(EntityType<? extends PathfinderMob> type, Level level) {
		super(type, level, BODY);
	}

	/** Registers his sound events once (Workers.register); sounds.json and their subtitles are tools/concordance_pacts.py's. */
	public static void registerSounds() {
		PactSpiritEntity.registerSounds(BODY);
	}

	/**
	 * The gesture with which he answers a pact screen's action, from its answer: an acceptance sealed, assent; refused (no
	 * tree, no bed, the compact short, not the owner), refuse; an offering paid ahead, inspect; a resumption, assent; his matter
	 * answered (RESOLVE), assent. None for the menu's own "not offered", hearing his terms (he listens), an invitation (he
	 * arrives), declining and ending (he departs), suspending (his loop shows it) or seating (he arrives). He has no on-request
	 * action, so a recall is never his.
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
	 * His loop for {@code status}: listening over all else unless switched off; waiting whenever he is off his service for a
	 * reason that is not his pact's own state (no tree included); never a claim of work he is not doing. WORKING and a matter
	 * awaiting its answer (his canopy stands meanwhile) play his idle; his work gesture plays over it when he bends his tree.
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
			case FINISHED, OWNER_OFFLINE, OTHER_DIMENSION, OUTSIDE_AGREEMENT, DESTINATION_UNLOADED, BLOCKED_BY_ACCESS,
					WAITING_FOR_RESOURCES -> Loop.WAITING;
			default -> Loop.IDLE;
		};
	}

	@Override
	protected RawAnimation animation(Status status) {
		return loop(status, listening()).animation;
	}

	/** Detail (a): his horn's cues sound a beat after their gestures; his whistles with theirs. */
	@Override
	protected int cueBeat(String id) {
		return HORNS.contains(id) ? HORN_BEAT : 0;
	}

	/** Detail (b): as he departs (bowing his horn), the tree beside his perch sways once. A sign only. */
	@Override
	protected void gestured(ServerLevel level, Clip clip) {
		if (clip == Gesture.DEPART) {
			BlockPos tree = CanopyShelter.tree(level, this);
			if (tree != null) {
				sway(level, tree);
			}
		}
	}

	/** His tree sways: the shared {@link #SWAY} sign at the top of the log at {@code tree}, once (each client draws it as its settings allow). Changes nothing. */
	public void sway(ServerLevel level, BlockPos tree) {
		Signs.show(level, tree, SWAY);
		sways++;
		swayed = new Sway(level.getGameTime(), tree.immutable());
	}

	/** How many times his tree has swayed since he was made or loaded; memory only. */
	public int sways() {
		return sways;
	}

	/** His tree's last sway, or null; memory only. */
	public @Nullable Sway lastSway() {
		return swayed;
	}
}

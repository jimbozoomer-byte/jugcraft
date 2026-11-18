package io.github.jimbozoomer.jugcraft.concordance.garden;

import io.github.jimbozoomer.jugcraft.concordance.ecology.Factor;
import io.github.jimbozoomer.jugcraft.concordance.ecology.Fit;
import io.github.jimbozoomer.jugcraft.concordance.ecology.Graft;
import io.github.jimbozoomer.jugcraft.concordance.ecology.Habitat;
import io.github.jimbozoomer.jugcraft.concordance.ecology.Niche;
import io.github.jimbozoomer.jugcraft.concordance.ecology.Organism;
import io.github.jimbozoomer.jugcraft.concordance.ecology.Verdict;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.LevelEvent;
import org.jspecify.annotations.Nullable;

/**
 * Grafting (Concordance Chunk C, C-09, C-P62 to C-P65; the garden cohort, GC-07, GC-P87): the one place a Verdant Bed is
 * grafted. A Greenwarden's gesture (OrganismCropBlock.graftBed: its rate gate, Authority at crop and bed, its messages
 * and the donor it spends) and Foras's graft keeping (spirits/GraftKeeping: every precondition asked as the present
 * holder, then one donor taken from his store in one Fabric transaction) both end in {@link #apply}, after every check
 * passed and the donor is spent. Grafting cannot fail and changes nothing but the bed's graft, its last verdict and the
 * particles at the crop.
 */
public final class Grafting {
	private Grafting() {
	}

	/** Whether {@code bed} carries a graft the rules know (one a bed; one they no longer know is replaced by the next). */
	public static boolean holds(VerdantBedBlockEntity bed) {
		return !bed.graft().isEmpty() && Garden.catalog().graft(bed.graft()) != null;
	}

	/** The first graft by id whose host is the organism {@code host} and whose donor has an item (GC-P84); null when none. */
	public static @Nullable Graft graftFor(String host) {
		Graft found = null;
		for (Graft graft : Garden.catalog().grafts().values()) {
			if (graft.host().equals(host) && donorItem(graft) != null && (found == null || graft.id().compareTo(found.id()) < 0)) {
				found = graft;
			}
		}
		return found;
	}

	/** The item id of {@code graft}'s donor crop (what is worked into its host), or null when the rules know no such item. */
	public static @Nullable String donorItem(Graft graft) {
		Organism donor = Garden.catalog().organism(graft.donor());
		return donor == null ? null : donor.item();
	}

	/**
	 * GC-P84: whether grafting {@code graft} onto {@code bed} would improve its {@code host} in {@code habitat}: the grafted
	 * niche's fit no worse on any factor than the niche the bed gives the host now, and better on one. Reads only.
	 */
	public static boolean gains(VerdantBedBlockEntity bed, Organism host, Graft graft, Habitat habitat) {
		return gains(bed.niche(host), Garden.catalog().niche(host, graft.id()), habitat);
	}

	/** GC-P84 on two niches: {@code grafted} no worse than {@code own} on every factor of {@code habitat}, and better on one. */
	public static boolean gains(Niche own, Niche grafted, Habitat habitat) {
		Verdict before = Verdict.of(own, habitat);
		Verdict after = Verdict.of(grafted, habitat);
		boolean better = false;
		for (Factor factor : Factor.values()) {
			int was = rank(before.fit(factor));
			int now = rank(after.fit(factor));
			if (now > was) {
				return false;
			}
			better |= now < was;
		}
		return better;
	}

	/** How far a fit holds a plant back, in Verdict's own order: ideal 0, tolerable 1, outside its range 2. */
	private static int rank(Fit fit) {
		return fit.outside() ? 2 : fit == Fit.TOLERABLE ? 1 : 0;
	}

	/**
	 * Grafts {@code graft} onto {@code bed} beneath the crop at {@code cropPos}, every precondition having held and its donor
	 * spent: sets the graft (sent and saved), forgets the ungrafted plant's verdict, and shows the growth particles and sound
	 * at the crop. Server only; it cannot fail.
	 */
	public static void apply(ServerLevel level, BlockPos cropPos, VerdantBedBlockEntity bed, Graft graft) {
		bed.setGraft(graft.id());
		// The niche changed: the last verdict spoke for the ungrafted plant (the unit review).
		bed.forgetVerdict();
		level.levelEvent(LevelEvent.PARTICLES_AND_SOUND_PLANT_GROWTH, cropPos, 5);
	}
}

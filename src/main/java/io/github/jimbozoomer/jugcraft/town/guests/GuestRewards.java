package io.github.jimbozoomer.jugcraft.town.guests;

import net.minecraft.server.level.ServerPlayer;

/**
 * The one daily social-reward budget (coordinator's seam): every source of affection other than the ledger's operator
 * repair goes through here, so gifts, quests, chats, errands, rivalries, outings, visits, milestones and meals share
 * one counter per player and guest per day ({@link GuestFavors#DAILY_AFFECTION_CAP}) instead of each granting a cap of
 * its own. Nothing here takes items; a caller takes what it takes first, having checked {@link #left} and
 * {@link GuestLedger#canGainAffection}, so nothing is consumed for a gain that would be refused.
 *
 * <p>One stated exception (owner decision, 12 November): the shared meal of a couple the ledger already has married, at
 * full affection ({@link GuestMilestoneMeals#marriedAtFull}), still takes its one food once a day, grants nothing (this
 * class is not called) and tells the player plainly that no affection is gained. Nothing else consumes for a zero gain;
 * an unmarried guest at full affection is still refused before anything is taken.
 */
public final class GuestRewards {
	private GuestRewards() {
	}

	/** Adds at most {@code amount} affection within today's shared budget and the maximum; returns what was added. */
	public static int grant(ServerPlayer player, String guest, int amount) {
		return GuestFavors.grantShared(player, guest, amount);
	}

	/** What is left of today's shared budget for the player and guest (and of the maximum). */
	public static int left(ServerPlayer player, String guest) {
		return GuestFavors.affectionLeftToday(player, guest);
	}
}

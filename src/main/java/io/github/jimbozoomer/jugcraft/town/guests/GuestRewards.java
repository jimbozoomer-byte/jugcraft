package io.github.jimbozoomer.jugcraft.town.guests;

import net.minecraft.server.level.ServerPlayer;

/**
 * The one daily social-reward budget (coordinator's seam): every source of affection other than the ledger's operator
 * repair goes through here, so gifts, quests, chats, errands, rivalries, outings, visits, milestones and meals share
 * one counter per player and guest per day ({@link GuestFavors#DAILY_AFFECTION_CAP}) instead of each granting a cap of
 * its own. Nothing here takes items; a caller takes what it takes first, having checked {@link #left} and
 * {@link GuestLedger#canGainAffection}, so nothing is ever consumed for a gain that would be refused.
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

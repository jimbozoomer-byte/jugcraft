package io.github.jimbozoomer.jugcraft.town.guests;

import io.github.jimbozoomer.jugcraft.town.Townsfolk;
import net.minecraft.server.level.ServerPlayer;
import org.jspecify.annotations.Nullable;

/**
 * WORKER A IMPLEMENTS (API agreed; placeholder bodies). Proposal and divorce, on the server thread, from
 * {@link GuestMenu} buttons that have already passed its distance and revision checks. Both are two steps: asked for,
 * then explicitly confirmed within {@link GuestBalance#CONFIRM_TICKS}; cancelling, or letting it lapse, changes nothing.
 * The pending request is the player's, kept server side (not in the menu), so reopening a menu neither confirms nor
 * renews it.
 *
 * <ul>
 * <li>A proposal may be asked for and confirmed only while {@link GuestLedger#canMarry} allows it and the player holds
 * {@link GuestBalance#PROPOSAL_COUNT} {@link GuestBalance#PROPOSAL_ITEM}; every check is made again on confirmation, and
 * the item is taken only once the ledger has committed the marriage, so a refused or lapsed proposal costs nothing.</li>
 * <li>Confirmation is for the same guest the request named; a confirmation for another guest is refused.</li>
 * </ul>
 */
public final class GuestMarriage {
	private GuestMarriage() {
	}

	public static GuestResult requestProposal(ServerPlayer player, Townsfolk npc, String guest) {
		throw new UnsupportedOperationException("GuestMarriage: worker A");
	}

	public static GuestResult confirmProposal(ServerPlayer player, Townsfolk npc, String guest) {
		throw new UnsupportedOperationException("GuestMarriage: worker A");
	}

	public static GuestResult cancelProposal(ServerPlayer player) {
		throw new UnsupportedOperationException("GuestMarriage: worker A");
	}

	/** The guest the player has asked to marry and not yet confirmed or cancelled (unexpired), or null. */
	public static @Nullable String proposalPending(ServerPlayer player) {
		throw new UnsupportedOperationException("GuestMarriage: worker A");
	}

	/** Whether a proposal now could be asked for and confirmed (every check, the item included); for the menu. */
	public static boolean mayPropose(ServerPlayer player, String guest) {
		throw new UnsupportedOperationException("GuestMarriage: worker A");
	}

	public static GuestResult requestDivorce(ServerPlayer player, String guest) {
		throw new UnsupportedOperationException("GuestMarriage: worker A");
	}

	public static GuestResult confirmDivorce(ServerPlayer player, String guest) {
		throw new UnsupportedOperationException("GuestMarriage: worker A");
	}

	public static GuestResult cancelDivorce(ServerPlayer player) {
		throw new UnsupportedOperationException("GuestMarriage: worker A");
	}

	/** Whether the player has asked for a divorce and not yet confirmed or cancelled it (unexpired). */
	public static boolean divorcePending(ServerPlayer player) {
		throw new UnsupportedOperationException("GuestMarriage: worker A");
	}
}

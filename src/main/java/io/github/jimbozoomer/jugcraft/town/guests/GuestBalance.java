package io.github.jimbozoomer.jugcraft.town.guests;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

/**
 * The numbers every part shares (first choices for owner review). Gift and quest values are worker B's, in
 * {@link GuestFavors}; this holds only what more than one part reads.
 */
public final class GuestBalance {
	/** Affection runs from 0 to this, per player and guest; at this a romance guest may be proposed to. */
	public static final int MAX_AFFECTION = 1000;
	/** How near (blocks, eye to body) a player must be to the guest for the menu and every action in it. */
	public static final double INTERACT_RANGE = 6.0;
	/** After a divorce neither the player nor the guest may marry for this long: seven real days. */
	public static final long REMARRIAGE_COOLDOWN_MILLIS = 7L * 24 * 60 * 60 * 1000;
	/** A proposal or divorce asked for must be confirmed within this many ticks (30 seconds), or it lapses. */
	public static final int CONFIRM_TICKS = 600;
	/** What a proposal gives, taken only when it is confirmed and the marriage committed: one diamond. */
	public static final Item PROPOSAL_ITEM = Items.DIAMOND;
	public static final int PROPOSAL_COUNT = 1;
	/** Ticks between two actions by one player, across all menus. */
	public static final int ACTION_GAP_TICKS = 4;

	private GuestBalance() {
	}
}

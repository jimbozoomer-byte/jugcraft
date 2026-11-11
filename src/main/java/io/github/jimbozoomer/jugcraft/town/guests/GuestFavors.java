package io.github.jimbozoomer.jugcraft.town.guests;

import io.github.jimbozoomer.jugcraft.town.Townsfolk;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * WORKER B IMPLEMENTS (API agreed; placeholder bodies). Gifts and fetch quests: modest existing survival items, taken
 * from the player's own inventory on the server, exactly once; affection added only through
 * {@link GuestLedger#addAffection}, capped per player and guest per day. No currency is paid or made. Calls come from
 * {@link GuestMenu} buttons that have already passed its distance and revision checks; each re-checks what it takes.
 */
public final class GuestFavors {
	/** A player's fetch quest for a guest, today. */
	public enum QuestState {
		NONE, OFFERED, ACTIVE, DONE
	}

	private GuestFavors() {
	}

	/** Server hooks, if any. Called once from {@link GuestService}. */
	public static void register() {
	}

	/** Gives the guest one of the item in the player's main hand. */
	public static GuestResult gift(ServerPlayer player, Townsfolk npc, String guest) {
		throw new UnsupportedOperationException("GuestFavors: worker B");
	}

	public static GuestResult acceptQuest(ServerPlayer player, Townsfolk npc, String guest) {
		throw new UnsupportedOperationException("GuestFavors: worker B");
	}

	public static GuestResult turnIn(ServerPlayer player, Townsfolk npc, String guest) {
		throw new UnsupportedOperationException("GuestFavors: worker B");
	}

	public static QuestState questState(ServerPlayer player, String guest) {
		throw new UnsupportedOperationException("GuestFavors: worker B");
	}

	/** The item and count today's quest asks for; empty when there is none. */
	public static ItemStack questItem(ServerPlayer player, String guest) {
		throw new UnsupportedOperationException("GuestFavors: worker B");
	}

	/** How many of the quest's item the player carries. */
	public static int carried(ServerPlayer player, String guest) {
		throw new UnsupportedOperationException("GuestFavors: worker B");
	}

	public static int giftsLeft(ServerPlayer player, String guest) {
		throw new UnsupportedOperationException("GuestFavors: worker B");
	}

	public static int affectionLeftToday(ServerPlayer player, String guest) {
		throw new UnsupportedOperationException("GuestFavors: worker B");
	}
}

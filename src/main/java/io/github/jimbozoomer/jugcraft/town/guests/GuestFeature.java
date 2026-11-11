package io.github.jimbozoomer.jugcraft.town.guests;

import io.github.jimbozoomer.jugcraft.town.Townsfolk;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * Something more a player can do with a guest, offered in the guest menu's Activities panel (coordinator's seam). The
 * menu shows each option {@link #available} reports, and sends a click (already checked for distance, the canonical
 * guest and the player's revision) to {@link #act}, which checks everything again itself, as GuestFavors and
 * GuestMarriage do, so a direct call can never do more than a click.
 */
public interface GuestFeature {
	/** Stable id ("personality", "outings", "milestones"); orders nothing and is saved nowhere. */
	String id();

	/** The options, in a fixed order, at most 16; the same on both sides (registered in common code). */
	List<String> options();

	/** The option's button text: translatableWithFallback("screen.jugcraft.guest.option.&lt;id&gt;.&lt;option&gt;", English). */
	Component label(String option);

	/** Whether the option is offered to the player now. Server side, cheap and without side effects (called every sync). */
	boolean available(ServerPlayer player, Townsfolk npc, String guest, String option);

	/** Does it; refuses (changing nothing) unless every check passes again. */
	GuestResult act(ServerPlayer player, Townsfolk npc, String guest, String option);
}

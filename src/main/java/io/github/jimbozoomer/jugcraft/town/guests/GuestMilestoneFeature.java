package io.github.jimbozoomer.jugcraft.town.guests;

import io.github.jimbozoomer.jugcraft.town.Townsfolk;
import java.util.List;
import java.util.Map;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * Worker D's options in the guest menu's Activities panel ({@link GuestFeature} {@value #ID}): the shared meal, the
 * church ceremony's request, confirmation and cancellation, and the two notice settings. {@link #available} only reads;
 * {@link #act} hands each option to the class that checks everything again itself.
 */
final class GuestMilestoneFeature implements GuestFeature {
	static final String ID = "milestones";
	static final String MEAL = "meal", CEREMONY = "ceremony", CEREMONY_CONFIRM = "ceremony_confirm", CEREMONY_CANCEL = "ceremony_cancel",
			NOTICES = "notices_toggle", SHARE = "notices_share";
	static final List<String> OPTIONS = List.of(MEAL, CEREMONY, CEREMONY_CONFIRM, CEREMONY_CANCEL, NOTICES, SHARE);
	private static final Map<String, String> ENGLISH = Map.of(
			MEAL, "Share a meal",
			CEREMONY, "Ask for a church ceremony",
			CEREMONY_CONFIRM, "Confirm the ceremony",
			CEREMONY_CANCEL, "Cancel the ceremony",
			NOTICES, "Town notices on/off",
			SHARE, "Mention me in notices on/off");

	@Override
	public String id() {
		return ID;
	}

	@Override
	public List<String> options() {
		return OPTIONS;
	}

	@Override
	public Component label(String option) {
		return Component.translatableWithFallback("screen.jugcraft.guest.option." + ID + "." + option, ENGLISH.getOrDefault(option, option));
	}

	@Override
	public boolean available(ServerPlayer player, Townsfolk npc, String guest, String option) {
		GuestRoster.Guest entry = GuestRoster.get(guest);
		if (entry == null || !entry.available()) {
			return false;
		}
		return switch (option) {
			case MEAL -> GuestMilestoneMeals.available(player, guest);
			case CEREMONY -> GuestCeremonies.mayRequest(player, guest);
			case CEREMONY_CONFIRM -> GuestCeremonies.pending(player, guest);
			case CEREMONY_CANCEL -> GuestCeremonies.cancellable(player, guest);
			case NOTICES, SHARE -> !GuestMilestoneState.get(player.level().getServer()).frozen(player.getUUID());
			default -> false;
		};
	}

	@Override
	public GuestResult act(ServerPlayer player, Townsfolk npc, String guest, String option) {
		return switch (option) {
			case MEAL -> GuestMilestoneMeals.share(player, npc, guest);
			case CEREMONY -> GuestCeremonies.request(player, npc, guest);
			case CEREMONY_CONFIRM -> GuestCeremonies.confirm(player, npc, guest);
			case CEREMONY_CANCEL -> GuestCeremonies.cancel(player, guest);
			case NOTICES -> GuestNotices.toggle(player, npc, guest);
			case SHARE -> GuestNotices.toggleShare(player, npc, guest);
			default -> GuestResult.refused("message.jugcraft.guest.option.unknown", "That is not something you can do here.");
		};
	}
}

package io.github.jimbozoomer.jugcraft.town.guests;

import io.github.jimbozoomer.jugcraft.town.Townsfolk;
import io.github.jimbozoomer.jugcraft.town.guests.GuestPersonalityState.ErrandState;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * Worker B's options in the guest menu's Activities panel ({@link GuestFeature} "personality", five of the 64):
 * <ul>
 * <li>{@code chat}: {@link GuestDialogue#chat}.</li>
 * <li>{@code errand}: take up today's errand ({@link GuestPersonalityErrands#accept}); offered while it is offered.</li>
 * <li>{@code errand_turn_in}: {@link GuestPersonalityErrands#turnIn}; offered while the errand is taken up.</li>
 * <li>{@code rivalry}: {@link GuestRivalries#start}; offered for a free fictional guest while the player has none
 * running (whether a rival stands nearby is found out on the click).</li>
 * <li>{@code rivalry_cancel}: {@link GuestRivalries#cancel}; offered while the player's rivalry involves this guest.</li>
 * </ul>
 * {@link #available} is read only and cheap (the menu reads it on every sync); {@link #act} goes to entry points that
 * check everything again themselves.
 */
final class GuestPersonalityFeature implements GuestFeature {
	static final GuestPersonalityFeature INSTANCE = new GuestPersonalityFeature();
	static final String ID = "personality";
	static final String CHAT = "chat", ERRAND = "errand", ERRAND_TURN_IN = "errand_turn_in", RIVALRY = "rivalry", RIVALRY_CANCEL = "rivalry_cancel";
	private static final List<String> OPTIONS = List.of(CHAT, ERRAND, ERRAND_TURN_IN, RIVALRY, RIVALRY_CANCEL);

	private GuestPersonalityFeature() {
	}

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
		return switch (option) {
			case CHAT -> Component.translatableWithFallback("screen.jugcraft.guest.option.personality.chat", "Chat");
			case ERRAND -> Component.translatableWithFallback("screen.jugcraft.guest.option.personality.errand", "Ask for an errand");
			case ERRAND_TURN_IN -> Component.translatableWithFallback("screen.jugcraft.guest.option.personality.errand_turn_in", "Hand over the errand");
			case RIVALRY -> Component.translatableWithFallback("screen.jugcraft.guest.option.personality.rivalry", "Start a friendly rivalry");
			case RIVALRY_CANCEL -> Component.translatableWithFallback("screen.jugcraft.guest.option.personality.rivalry_cancel", "Call off the rivalry");
			default -> Component.literal(option);
		};
	}

	@Override
	public boolean available(ServerPlayer player, Townsfolk npc, String guest, String option) {
		if (GuestPersonality.check(player, npc, guest) != null) {
			return false;
		}
		return switch (option) {
			case CHAT -> true;
			case ERRAND -> GuestPersonalityErrands.state(player, guest) == ErrandState.OFFERED;
			case ERRAND_TURN_IN -> GuestPersonalityErrands.state(player, guest) == ErrandState.ACTIVE;
			case RIVALRY -> GuestRivalries.mayOffer(player, guest);
			case RIVALRY_CANCEL -> GuestRivalries.involves(player, guest);
			default -> false;
		};
	}

	@Override
	public GuestResult act(ServerPlayer player, Townsfolk npc, String guest, String option) {
		return switch (option) {
			case CHAT -> GuestDialogue.chat(player, npc, guest);
			case ERRAND -> GuestPersonalityErrands.accept(player, npc, guest);
			case ERRAND_TURN_IN -> GuestPersonalityErrands.turnIn(player, npc, guest);
			case RIVALRY -> GuestRivalries.start(player, npc, guest);
			case RIVALRY_CANCEL -> GuestRivalries.cancel(player, npc, guest);
			default -> GuestResult.refused("message.jugcraft.guest.personality.unknown_option", "That is not something you can do here.");
		};
	}
}

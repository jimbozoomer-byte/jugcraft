package io.github.jimbozoomer.jugcraft.town.guests;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;

/**
 * WORKER C IMPLEMENTS. Placing guests ({@code /guests place}, {@code /guests roster}; operators only): only in loaded
 * chunks, never changing or replacing a block, never inside someone else's protection, through
 * {@code CustomNpcs.create(.., guest)} and {@link GuestLedger#bind}; a pending guest, or one whose canonical entity
 * exists, is refused.
 */
public final class GuestPlacement {
	private GuestPlacement() {
	}

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
	}
}

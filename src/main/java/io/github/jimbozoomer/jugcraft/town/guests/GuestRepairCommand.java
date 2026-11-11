package io.github.jimbozoomer.jugcraft.town.guests;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;

/**
 * WORKER A IMPLEMENTS. Operator repair under {@code /guests} (permission level 2, checked again on the server): the
 * ledger's state for a guest or player, a forced divorce, clearing a cooldown, rebinding a lost guest. Every repair is
 * explicit and logged. Worker C's {@link GuestPlacement} adds its own {@code /guests} subcommands to the same root.
 */
public final class GuestRepairCommand {
	private GuestRepairCommand() {
	}

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
	}
}

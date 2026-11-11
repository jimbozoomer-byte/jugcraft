package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.town.guests.GuestRoster;
import java.util.HashSet;
import java.util.Set;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

/**
 * Town guests (docs/features/jze-town-relationships.md): the roster, placement and the menu's server side (worker C
 * adds to these).
 */
public class GuestRosterGameTests {
	/** Saved ids are unique and well formed; pending guests can be neither placed nor married. */
	@GameTest(maxTicks = 1)
	public void theRosterIsUniqueAndPendingGuestsWait(GameTestHelper helper) {
		Set<String> ids = new HashSet<>();
		for (GuestRoster.Guest guest : GuestRoster.ALL) {
			helper.assertTrue(ids.add(guest.id()), "Repeated guest id " + guest.id());
			helper.assertTrue(GuestRoster.get(guest.id()) == guest, "Look-up by id finds " + guest.id());
			helper.assertTrue(guest.id().matches("[a-z][a-z0-9_]{1,31}"), "Malformed guest id " + guest.id());
			helper.assertTrue(!guest.pending() || (!guest.available() && !guest.mayMarry()), "A pending guest waits: " + guest.id());
		}
		for (String pending : new String[] {"rooni", "remielle", "guest_unnamed", "jakeey_kpop"}) {
			helper.assertTrue(GuestRoster.get(pending) != null && GuestRoster.get(pending).pending(), pending + " is pending");
		}
		helper.succeed();
	}
}

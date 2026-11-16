package io.github.jimbozoomer.jugcraft.town.guests;

/**
 * Game-test guests (coordinator's seam): a synthetic, available guest whose id starts with "test_", never in
 * {@link GuestRoster#ALL}, so tests that bind guests on the shared test server use ids of their own instead of competing
 * for the real ones. Each test class uses its own prefix (test_a_, test_b_, test_c_, test_d_, test_ledger_).
 */
public final class GuestRosterTesting {
	private GuestRosterTesting() {
	}

	/** Registers (once) and returns the id of a synthetic test guest that may or may not marry. */
	public static String guest(String id, boolean mayMarry) {
		GuestRoster.Guest guest = GuestRoster.test(id, mayMarry);
		if (guest.mayMarry() != mayMarry) {
			throw new IllegalStateException("Test guest " + id + " was registered with mayMarry " + guest.mayMarry());
		}
		return guest.id();
	}
}

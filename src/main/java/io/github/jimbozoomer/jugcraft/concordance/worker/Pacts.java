package io.github.jimbozoomer.jugcraft.concordance.worker;

import io.github.jimbozoomer.jugcraft.concordance.resource.BoundWill;
import io.github.jimbozoomer.jugcraft.concordance.resource.BoundWillLedger;
import java.util.UUID;

/**
 * Concordance Chunk D, unit 1 (D-01): the rules every kept agreement shares. Pure; it holds no state: the server says
 * what it sees (who acts, what the ledger holds, how full their roster is). A seal is refused with its first reason,
 * before anything is recorded, spent or called (D-S11). Keep Outcome equal to tools/concordance_workers.py SEAL_OUTCOMES
 * (check_pacts).
 */
public final class Pacts {
	/** Why a pact departed: its holder ended it, or its place was broken. */
	public static final String ENDED = "ended";
	public static final String BROKEN = "broken";

	private Pacts() {
	}

	/**
	 * How a seal went. Ids are stable (append only); each is worded as {@code message.jugcraft.concordance.workers.seal_<id>}
	 * (DISABLED as the switch's).
	 */
	public enum Outcome {
		DONE("done"),
		DISABLED("disabled"),
		NOT_PERMITTED("not_permitted"),
		TAKEN("taken"),
		ALREADY_HELD("already_held"),
		HOLDER_FULL("holder_full"),
		ROSTER_FULL("roster_full"),
		UNAVAILABLE("unavailable"),
		CANNOT_AFFORD("cannot_afford");

		public final String id;

		Outcome(String id) {
			this.id = id;
		}
	}

	/**
	 * The first reason a seal is refused now, in this order, or DONE: the actor may not seal at this place (D-S8); the place
	 * already keeps an agreement (TAKEN, D-S10); they keep one with this counterpart and it allows one (ALREADY_HELD); they
	 * hold BoundWillLedger.MAX_PER_HOLDER Bound Wills (HOLDER_FULL); their roster has no room (ROSTER_FULL, D-S11). DISABLED,
	 * UNAVAILABLE and CANNOT_AFFORD are the server's (the Concordance off; a presence that did not come; a cost not met).
	 */
	public static Outcome refusal(boolean permitted, boolean placeKept, boolean counterpartHeld, int held, boolean rosterFull) {
		if (!permitted) {
			return Outcome.NOT_PERMITTED;
		}
		if (placeKept) {
			return Outcome.TAKEN;
		}
		if (counterpartHeld) {
			return Outcome.ALREADY_HELD;
		}
		if (held >= BoundWillLedger.MAX_PER_HOLDER) {
			return Outcome.HOLDER_FULL;
		}
		return rosterFull ? Outcome.ROSTER_FULL : Outcome.DONE;
	}

	/** Whether {@code holder} keeps a record with {@code counterpart} (namespaces normalised: records before G9 wrote "gathering_shade"). */
	public static boolean holds(BoundWillLedger ledger, UUID holder, String counterpart) {
		String wanted = counterpart(counterpart);
		for (BoundWill record : ledger.heldBy(holder)) {
			if (counterpart(record.counterpart()).equals(wanted)) {
				return true;
			}
		}
		return false;
	}

	/** A counterpart id with its namespace ("jugcraft" when a record from before G9 wrote none). */
	public static String counterpart(String id) {
		return id.indexOf(':') >= 0 ? id : "jugcraft:" + id;
	}
}

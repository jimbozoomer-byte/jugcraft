package io.github.jimbozoomer.jugcraft.concordance.resource;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/**
 * The celestial events a player has drawn Astral Resonance from, so that one alignment never pays twice. Events are
 * named with their time ({@code eventTime}, the game time the alignment began), and only events within the last
 * {@value #RETENTION} ticks can be claimed at all; older entries are forgotten, which keeps the ledger small without
 * ever letting an event pay again.
 */
public record AstralLedger(Map<String, Long> claimed) {
	/** Five in-game days. */
	public static final long RETENTION = 5L * 24000L;
	public static final AstralLedger EMPTY = new AstralLedger(Map.of());

	public AstralLedger {
		claimed = Collections.unmodifiableMap(new TreeMap<>(claimed));
	}

	public enum Outcome {
		GRANTED, ALREADY_CLAIMED, EXPIRED, NOT_YET
	}

	public record Claim(AstralLedger ledger, Outcome outcome) {
	}

	public Claim claim(String event, long eventTime, long now) {
		if (eventTime > now) {
			return new Claim(this, Outcome.NOT_YET);
		}
		if (now - eventTime > RETENTION) {
			return new Claim(this, Outcome.EXPIRED);
		}
		if (claimed.containsKey(event)) {
			return new Claim(this, Outcome.ALREADY_CLAIMED);
		}
		Map<String, Long> copy = new TreeMap<>();
		for (Map.Entry<String, Long> entry : claimed.entrySet()) {
			if (now - entry.getValue() <= RETENTION) {
				copy.put(entry.getKey(), entry.getValue());
			}
		}
		copy.put(event, eventTime);
		return new Claim(new AstralLedger(copy), Outcome.GRANTED);
	}
}

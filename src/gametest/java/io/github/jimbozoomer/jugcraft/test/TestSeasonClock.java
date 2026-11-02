package io.github.jimbozoomer.jugcraft.test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;

/** Scoped season clock for world generation on every test thread. Never shipped with the mod. */
public final class TestSeasonClock implements AutoCloseable {
	private static volatile Clock clock;
	private final Clock previous;

	private TestSeasonClock(Instant instant) {
		previous = clock;
		clock = Clock.fixed(instant, ZoneOffset.UTC);
	}

	public static TestSeasonClock freeze(Instant instant) {
		return new TestSeasonClock(instant);
	}

	public static LocalDate date(ZoneId zone) {
		Clock current = clock;
		return current == null ? LocalDate.now(zone) : LocalDate.now(current.withZone(zone));
	}

	@Override
	public void close() {
		clock = previous;
	}
}

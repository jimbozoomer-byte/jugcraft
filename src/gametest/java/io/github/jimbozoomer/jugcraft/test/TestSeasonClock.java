package io.github.jimbozoomer.jugcraft.test;

import java.time.Instant;
import org.jspecify.annotations.Nullable;

/** One real-date snapshot for a client tour; closed after its worlds finish saving. Not shipped with the mod. */
public final class TestSeasonClock implements AutoCloseable {
	private static volatile @Nullable Instant frozen;
	private final Instant captured;

	private TestSeasonClock(Instant captured) {
		this.captured = captured;
	}

	public static TestSeasonClock freeze() {
		return freezeAt(Instant.now());
	}

	static synchronized TestSeasonClock freezeAt(Instant captured) {
		if (frozen != null) throw new IllegalStateException("A seasonal test clock is already active");
		var scope = new TestSeasonClock(captured);
		frozen = scope.captured;
		return scope;
	}

	public static @Nullable Instant frozenInstant() {
		return frozen;
	}

	@Override
	public void close() {
		synchronized (TestSeasonClock.class) {
			if (frozen == captured) frozen = null;
		}
	}
}

package io.github.jimbozoomer.jugcraft.concordance.rules;

/**
 * Something a player did that research can learn from. The server creates these from what it saw happen (an
 * examination, a finished study, a successful cast); a client can never submit one.
 */
public sealed interface Evidence {
	/** A luminous specimen examined by hand, at the light level where the player stood (0..15). */
	record Examined(String item, int light) implements Evidence {
	}

	/** A specimen read to the end at a study station. */
	record Studied(String item, String station) implements Evidence {
	}

	/** An invocation that took effect in a chunk ({@code chunk} is the packed chunk position). */
	record Invoked(String invocation, long chunk) implements Evidence {
	}
}

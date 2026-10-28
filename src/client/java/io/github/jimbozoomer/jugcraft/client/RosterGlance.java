package io.github.jimbozoomer.jugcraft.client;

import net.minecraft.network.chat.Component;

/**
 * The roster's glances (docs/features/magic-interfaces.md, spec 2.14): a card's one fact of at most four words, from the
 * server's own row (its next obligation, else its task), through the same keyed table as the pact's
 * (screen.jugcraft.roster.glance.&lt;key&gt;, tools/magic_ui.py ROSTER_GLANCES). The card's message keeps every word of the
 * row. Pure formatting; anything without a glance shows its whole line.
 */
final class RosterGlance {
	private RosterGlance() {
	}

	/** A roster line's glance (its next obligation, its task). */
	static PactGlance.Glance of(Component line) {
		return PactGlance.of(line);
	}

	/**
	 * A card's one fact: its next obligation's glance when it has one, else its task's, else the next obligation whole
	 * (the roster's rows say "Nothing is owed" when nothing is).
	 */
	static Component fact(Component next, Component task) {
		PactGlance.Glance owed = of(next);
		if (owed.isShort()) {
			return owed.shown();
		}
		PactGlance.Glance doing = of(task);
		return doing.isShort() ? doing.shown() : next;
	}
}

package io.github.jimbozoomer.jugcraft.client;

import io.github.jimbozoomer.jugcraft.concordance.spirits.RosterStatePayload;
import java.util.List;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.item.ItemStack;

/**
 * The roster's glances (docs/features/magic-interfaces.md, spec 2.14): a card's one fact of at most four words, from the
 * server's own row (its next obligation, else its task), through the same keyed table as the pact's
 * (screen.jugcraft.roster.glance.&lt;key&gt;, tools/magic_ui.py ROSTER_GLANCES). The card's message keeps every word of the
 * row. Pure formatting; anything without a glance shows its whole line. Unit 2 adds a card's name, kind and state (its
 * row's waiting reason, short: a roster sentence's glance, else a worker's or courier's short form, MagicGlance) and the
 * item its fact draws.
 */
final class RosterGlance {
	private static final String ROSTER = PactGlance.SERVER + "roster.";
	/** The offering sentences whose second argument names the offering's tag (Roster.next). */
	private static final List<String> OFFERINGS = List.of("next.offering", "next.at_visit", "next.warned", "next.held");

	/** A card's fact: its words (empty: none) and the items its icon shows in turn (none, one, or an offering tag's). */
	record Fact(Component shown, List<ItemStack> items) {
	}

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

	/**
	 * {@code row}'s fact (as {@link #fact(Component, Component)}) and the items it draws: an offering's tag, a request's own
	 * item for its deliveries, the carried request's item for a courier's task, the work's item for a pact's day of tasks.
	 */
	static Fact fact(RosterStatePayload.Row row, List<RosterStatePayload.Row> rows) {
		Component line = factLine(row);
		PactGlance.Glance glance = of(line);
		return glance.isShort() ? new Fact(glance.shown(), items(row, line, rows)) : new Fact(line, List.of());
	}

	/**
	 * The line {@code row}'s fact is drawn from: its next obligation when that has a glance, else its task when that has one,
	 * else its next obligation (drawn whole). The card's tip carries this line's whole sentence (the unit 3 fixes).
	 */
	static Component factLine(RosterStatePayload.Row row) {
		return of(row.next()).isShort() || !of(row.task()).isShort() ? row.next() : row.task();
	}

	/** A card's name: a request's short name ("Request #3"), else the row's own. */
	static Component name(RosterStatePayload.Row row) {
		return of(row.name()).shown();
	}

	/** A card's kind: a request's short kind ("Courier"), else the row's own. */
	static Component kind(RosterStatePayload.Row row) {
		return of(row.kind()).shown();
	}

	/** A card's state: its waiting reason short (a roster sentence's glance, else a worker's or courier's short form), else {@code word}. */
	static Component state(RosterStatePayload.Row row, Component word) {
		PactGlance.Glance glance = of(row.waiting());
		if (glance.isShort()) {
			return glance.shown();
		}
		Component shortened = MagicGlance.of(row.waiting());
		return shortened != row.waiting() ? shortened : word;
	}

	private static List<ItemStack> items(RosterStatePayload.Row row, Component line, List<RosterStatePayload.Row> rows) {
		if (!(line.getContents() instanceof TranslatableContents contents) || !contents.getKey().startsWith(ROSTER)) {
			return List.of();
		}
		String key = contents.getKey().substring(ROSTER.length());
		Object[] args = contents.getArgs();
		if (OFFERINGS.contains(key) && args.length > 1 && args[1] instanceof Component tag
				&& tag.getContents() instanceof TranslatableContents named && named.getFallback() != null) {
			return MagicStacks.tag(named.getFallback());
		}
		if (key.equals("next.delivered")) {
			return one(PactGlance.item(row.portrait()));
		}
		if ((key.equals("task.carrying") || key.equals("task.claimed")) && args.length > 0 && args[0] instanceof Number number) {
			UUID request = new UUID(0L, number.longValue());
			for (RosterStatePayload.Row other : rows) {
				if (other.id().equals(request)) {
					return one(PactGlance.item(other.portrait()));
				}
			}
			return List.of();
		}
		if (key.equals("next.quota") && row.service().getContents() instanceof TranslatableContents service
				&& service.getKey().startsWith(ROSTER + "service.")) {
			return one(PactGlance.workIcon(service.getKey().substring((ROSTER + "service.").length()), ""));
		}
		return List.of();
	}

	private static List<ItemStack> one(ItemStack item) {
		return item.isEmpty() ? List.of() : List.of(item);
	}
}

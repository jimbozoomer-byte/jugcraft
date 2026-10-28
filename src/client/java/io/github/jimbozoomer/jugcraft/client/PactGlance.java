package io.github.jimbozoomer.jugcraft.client;

import java.util.List;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.jspecify.annotations.Nullable;

/**
 * Glances (docs/features/magic-interfaces.md, spec 1.7): pure formatting of a server sentence into the few words a magic
 * screen draws, with no payload change. The server sends each term, row, blocker and answer as a translatable sentence
 * with its arguments (numbers kept as numbers over the network); a glance is a client key beside it,
 * screen.jugcraft.pact.glance.&lt;server key&gt; (the roster's under screen.jugcraft.roster.glance.), fed the same arguments by
 * position. The whole sentence stays the message, the narration and the tip. Anything unknown (an older server, a new
 * term, a glance naming more arguments than were sent, a bare %s) falls back to the whole sentence. tools/magic_ui.py
 * generates every glance and renders each with the server's real argument order (check_magic_ui); a garden spirit's unit
 * adds its rows there and nothing here.
 */
final class PactGlance {
	/** Where every server sentence the glances read lives. */
	static final String SERVER = "message.jugcraft.concordance.";

	/**
	 * A glance: the state mark it draws (null: none), the words it draws and the whole sentence (its message).
	 *
	 * @param shown the short words, or {@code full} itself when there is no glance for it
	 */
	record Glance(MagicArt.@Nullable Mark mark, Component shown, Component full) {
		/** Whether the words drawn are a glance rather than the whole sentence. */
		boolean isShort() {
			return shown != full;
		}
	}

	/** The mark a glance draws, by the shape of its server key (tools/magic_ui.py MARK_RULES, in that order). */
	private record Rule(String kind, String text, MagicArt.Mark mark) {
		boolean matches(String server) {
			return switch (kind) {
				case "prefix" -> server.startsWith(text);
				case "suffix" -> server.endsWith(text);
				case "contains" -> server.contains(text);
				default -> server.equals(text);
			};
		}
	}

	private static final List<Rule> MARKS = List.of(
			new Rule("prefix", "pact.missing.", MagicArt.Mark.LOCK),
			new Rule("suffix", ".missing", MagicArt.Mark.LOCK),
			new Rule("exact", "pact.seat.offer", MagicArt.Mark.LOCK),
			new Rule("exact", "pact.unavailable", MagicArt.Mark.LOCK),
			new Rule("exact", "pact.row.state.active", MagicArt.Mark.TICK),
			new Rule("exact", "pact.row.state.upkeep_due", MagicArt.Mark.HOURGLASS),
			new Rule("prefix", "pact.row.state.", MagicArt.Mark.MOON),
			new Rule("exact", "pact.row.lost", MagicArt.Mark.MOON),
			new Rule("suffix", ".row.asleep", MagicArt.Mark.MOON),
			new Rule("contains", ".row.overworked", MagicArt.Mark.HOURGLASS),
			new Rule("suffix", "obligation_kept", MagicArt.Mark.TICK),
			new Rule("suffix", "obligation", MagicArt.Mark.HOURGLASS),
			new Rule("contains", ".matter.", MagicArt.Mark.ASK));

	private PactGlance() {
	}

	/** {@code full}'s glance. */
	static Glance of(Component full) {
		return of(full, "");
	}

	/** {@code full}'s glance with {@code suffix}: a second glance of one sentence (the upkeep's cap: "cap"). */
	static Glance of(Component full, String suffix) {
		if (full.getContents() instanceof TranslatableContents contents && contents.getKey().startsWith(SERVER)) {
			String server = contents.getKey().substring(SERVER.length());
			Component shown = shorten(key(server, suffix), contents.getArgs());
			return new Glance(mark(server), shown == null ? full : shown, full);
		}
		return new Glance(null, full, full);
	}

	/**
	 * A chooser card's reason (spec 2.14; at most four words): the server's note for a spirit not ready is
	 * pact.invite.missing (its name, its first missing note), and the reason is that note's card glance, else its glance,
	 * else the note itself.
	 */
	static Component card(Component note) {
		Component reason = note;
		if (note.getContents() instanceof TranslatableContents contents && contents.getKey().equals(SERVER + "pact.invite.missing")
				&& contents.getArgs().length == 2 && contents.getArgs()[1] instanceof Component inner) {
			reason = inner;
		}
		if (reason.getContents() instanceof TranslatableContents contents && contents.getKey().startsWith(SERVER)) {
			Component shown = shorten(cardKey(contents.getKey().substring(SERVER.length())), contents.getArgs());
			if (shown != null) {
				return shown;
			}
		}
		return of(reason).shown();
	}

	/** A glance's key (tools/magic_ui.py glance_key): pact.&lt;rest&gt; and roster.&lt;rest&gt; under their screens, the rest under the pact's. */
	static String key(String server, String suffix) {
		int dot = server.indexOf('.');
		String family = dot < 0 ? server : server.substring(0, dot);
		String rest = dot < 0 ? "" : server.substring(dot + 1);
		String base = switch (family) {
			case "pact" -> "screen.jugcraft.pact.glance." + rest;
			case "roster" -> "screen.jugcraft.roster.glance." + rest;
			default -> "screen.jugcraft.pact.glance." + server;
		};
		return suffix.isEmpty() ? base : base + "." + suffix;
	}

	/** A chooser reason's key (tools/magic_ui.py card_key). */
	static String cardKey(String server) {
		return "screen.jugcraft.pact.glance.card." + (server.startsWith("pact.") ? server.substring("pact.".length()) : server);
	}

	/** The mark the glance of {@code server} draws, or null. */
	static MagicArt.@Nullable Mark mark(String server) {
		for (Rule rule : MARKS) {
			if (rule.matches(server)) {
				return rule.mark();
			}
		}
		return null;
	}

	/** The glance under {@code key} fed {@code args}, or null when there is none or it does not fit them. */
	private static @Nullable Component shorten(String key, Object[] args) {
		Language language = Language.getInstance();
		if (!language.has(key)) {
			return null;
		}
		int top = highest(language.getOrDefault(key));
		return top < 0 || top > args.length ? null : Component.translatable(key, args);
	}

	/**
	 * The highest argument a glance names by position (%n$s), 0 for none; -1 when it uses anything else (a bare %s takes
	 * arguments in order, which a glance must not: critique 3).
	 */
	static int highest(String pattern) {
		int top = 0;
		int i = 0;
		while ((i = pattern.indexOf('%', i)) >= 0) {
			if (i + 1 < pattern.length() && pattern.charAt(i + 1) == '%') {
				i += 2;
				continue;
			}
			int j = i + 1;
			while (j < pattern.length() && Character.isDigit(pattern.charAt(j))) {
				j++;
			}
			if (j == i + 1 || j + 1 >= pattern.length() || pattern.charAt(j) != '$' || pattern.charAt(j + 1) != 's') {
				return -1;
			}
			top = Math.max(top, Integer.parseInt(pattern.substring(i + 1, j)));
			i = j + 2;
		}
		return top;
	}
}

package io.github.jimbozoomer.jugcraft.client;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

/**
 * Glances (docs/features/magic-interfaces.md, spec 1.7): pure formatting of a server sentence into the few words a magic
 * screen draws, with no payload change. The server sends each term, row, blocker and answer as a translatable sentence
 * with its arguments (numbers kept as numbers over the network); a glance is a client key beside it,
 * screen.jugcraft.pact.glance.&lt;server key&gt; (the roster's under screen.jugcraft.roster.glance.), fed the same arguments by
 * position. The whole sentence stays the message, the narration and the tip. Anything unknown (an older server, a new
 * term, a glance naming more arguments than were sent, a bare %s) falls back to the whole sentence. An argument that is
 * itself a sentence with a glance (a first task's act, a research step's description) is drawn by that glance too. The
 * conflicting-agreement diagnostic, the one answer from outside the Concordance's messages, has its glance under
 * "diagnostic.". tools/magic_ui.py generates every glance and renders each with the server's real argument order and with
 * every act and step its nested arguments can be (check_magic_ui). A garden spirit's unit adds its work's row there
 * (WORK_GLANCES: its service line and name, rows, blockers, chooser reason and icon) and its icon's line in
 * {@link #WORK_ICONS} here, which check_magic_ui holds equal.
 */
final class PactGlance {
	/** Where every server sentence the glances read lives. */
	static final String SERVER = "message.jugcraft.concordance.";
	/** The diagnostics a pact answer can be (AgreementHolder's conflicting agreement), read as "diagnostic.&lt;code&gt;". */
	static final String DIAGNOSTIC = "compose.jugcraft.diagnostic.";
	/** A research step's description (pact.missing.step's third argument), drawn by its glance under "step.". */
	private static final String STEP = "advancements.jugcraft.";
	private static final String DESCRIPTION = ".description";

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
			return PactGlance.matches(kind, text, server);
		}
	}

	/**
	 * The icon a page's row draws instead of its mark, by the shape of its server key (tools/magic_ui.py ICON_RULES, in that
	 * order): "work" (the work's own item, {@link #WORK_ICONS}), "glyph:&lt;name&gt;" or "item:&lt;id&gt;".
	 */
	private record IconRule(String kind, String text, String icon) {
		boolean matches(String server) {
			return PactGlance.matches(kind, text, server);
		}
	}

	/** What a row draws before its words: an item, a glyph in its ink, or neither (then its mark, if any). */
	record Icon(ItemStack item, MagicArt.@Nullable Glyph glyph) {
		static final Icon NONE = new Icon(ItemStack.EMPTY, null);

		boolean isNone() {
			return item.isEmpty() && glyph == null;
		}
	}

	private static final List<Rule> MARKS = List.of(
			new Rule("prefix", "pact.missing.", MagicArt.Mark.LOCK),
			new Rule("suffix", ".missing", MagicArt.Mark.LOCK),
			new Rule("contains", ".missing.", MagicArt.Mark.LOCK),
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
			new Rule("contains", ".matter.", MagicArt.Mark.ASK),
			new Rule("exact", "pact.graft_keeping.row.no_donor", MagicArt.Mark.HOURGLASS));

	private static final List<IconRule> ICONS = List.of(
			new IconRule("contains", ".row.overworked", "work"),
			new IconRule("exact", "pact.verdance_carrying.row.poured", "item:jugcraft:ley_pylon"),
			new IconRule("exact", "pact.verdance_carrying.row.moved", "item:jugcraft:gleaner"),
			new IconRule("exact", "pact.verdance_carrying.row.leaving", "item:jugcraft:verdant_heart"),
			new IconRule("exact", "pact.graft_keeping.row.no_donor", "item:jugcraft:dewmoss"),
			new IconRule("exact", "pact.graft_keeping.row.last", "item:jugcraft:sunpetal"),
			new IconRule("suffix", "obligation", "glyph:pin"),
			new IconRule("exact", "pact.ended_here", "glyph:pin"),
			new IconRule("exact", "pact.departed", "glyph:spiral"));

	/**
	 * Each work's own item: its service line's and its "work" rows' icon (tools/magic_ui.py WORK_GLANCES[work]["icon"], one
	 * line here a work, held equal by check_magic_ui); a work not here draws its spirit's sign.
	 */
	static final Map<String, String> WORK_ICONS = Map.of(
			"star_reckoning", "jugcraft:astrolabe",
			"strain_tending", "jugcraft:verdant_bed",
			"verdance_carrying", "jugcraft:verdant_heart",
			"graft_keeping", "jugcraft:dewmoss");

	private PactGlance() {
	}

	/** {@code full}'s glance. */
	static Glance of(Component full) {
		return of(full, "");
	}

	/** {@code full}'s glance with {@code suffix}: a second glance of one sentence (the upkeep's cap: "cap"). */
	static Glance of(Component full, String suffix) {
		String server = server(full);
		if (!server.isEmpty() && full.getContents() instanceof TranslatableContents contents) {
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

	private static boolean matches(String kind, String text, String server) {
		return switch (kind) {
			case "prefix" -> server.startsWith(text);
			case "suffix" -> server.endsWith(text);
			case "contains" -> server.contains(text);
			default -> server.equals(text);
		};
	}

	/**
	 * {@code full}'s server key under message.jugcraft.concordance. (pact.row.next), "diagnostic.&lt;code&gt;" for a
	 * diagnostic, or "" when it is neither.
	 */
	static String server(Component full) {
		if (full.getContents() instanceof TranslatableContents contents) {
			String key = contents.getKey();
			if (key.startsWith(SERVER)) {
				return key.substring(SERVER.length());
			}
			if (key.startsWith(DIAGNOSTIC)) {
				return "diagnostic." + key.substring(DIAGNOSTIC.length());
			}
		}
		return "";
	}

	/**
	 * The glance key an argument's own sentence is drawn by when it is nested in another's (tools/magic_ui.py nested_key): a
	 * server sentence's glance, or a research step description's under screen.jugcraft.pact.glance.step.; null for others.
	 */
	static @Nullable String nestedKey(String key) {
		if (key.startsWith(SERVER)) {
			return key(key.substring(SERVER.length()), "");
		}
		if (key.startsWith(STEP) && key.endsWith(DESCRIPTION)) {
			return "screen.jugcraft.pact.glance.step." + key.substring(STEP.length(), key.length() - DESCRIPTION.length());
		}
		return null;
	}

	/** The icon the row {@code server} draws, for a pact whose work is {@code work} and whose spirit's sign is {@code sign}. */
	static Icon icon(String server, String work, String sign) {
		for (IconRule rule : ICONS) {
			if (rule.matches(server)) {
				if (rule.icon().equals("work")) {
					return new Icon(workIcon(work, sign), null);
				}
				if (rule.icon().startsWith("glyph:")) {
					return new Icon(ItemStack.EMPTY, MagicArt.Glyph.valueOf(rule.icon().substring("glyph:".length()).toUpperCase(Locale.ROOT)));
				}
				return new Icon(item(rule.icon().substring("item:".length())), null);
			}
		}
		return Icon.NONE;
	}

	/** The work's own item ({@link #WORK_ICONS}), else the spirit's sign, else nothing. */
	static ItemStack workIcon(String work, String sign) {
		ItemStack own = item(WORK_ICONS.getOrDefault(work, ""));
		return own.isEmpty() ? item(sign) : own;
	}

	/** The item {@code id} names, or an empty stack. */
	static ItemStack item(String id) {
		Identifier key = id.isEmpty() ? null : Identifier.tryParse(id);
		return key != null && BuiltInRegistries.ITEM.containsKey(key) ? new ItemStack(BuiltInRegistries.ITEM.getValue(key)) : ItemStack.EMPTY;
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

	/**
	 * The glance under {@code key} fed {@code args}, or null when there is none or it does not fit them; each argument that
	 * is a sentence with a glance of its own (nestedKey) is given as that glance.
	 */
	private static @Nullable Component shorten(String key, Object[] args) {
		Language language = Language.getInstance();
		if (!language.has(key)) {
			return null;
		}
		int top = highest(language.getOrDefault(key));
		if (top < 0 || top > args.length) {
			return null;
		}
		Object[] shown = args.clone();
		for (int i = 0; i < shown.length; i++) {
			if (shown[i] instanceof Component part && part.getContents() instanceof TranslatableContents inner) {
				String nested = nestedKey(inner.getKey());
				if (nested != null && language.has(nested)) {
					int needs = highest(language.getOrDefault(nested));
					if (needs >= 0 && needs <= inner.getArgs().length) {
						shown[i] = Component.translatable(nested, inner.getArgs());
					}
				}
			}
		}
		return Component.translatable(key, shown);
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

package io.github.jimbozoomer.jugcraft.client;

import io.github.jimbozoomer.jugcraft.concordance.spirits.PactSpirits;
import java.util.Locale;
import java.util.Map;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

/**
 * Short forms of whole sentences beyond the pact screen's own glances (docs/features/magic-interfaces.md, unit 2): a
 * roster card's state (a row's waiting reason that is a worker's status or a courier's state or note) and Jade's short
 * lines on the pact perch, the Spirit Anchor, the spirits, Verdant Beds and living devices. A sentence's short form is
 * screen.jugcraft.glance.&lt;its own key&gt; (tools/magic_ui.py SENTENCES), fed the sentence's own arguments by position;
 * a sentence without one, or given fewer arguments than its short form names, is shown whole. The whole sentence stays
 * the narration, and Jade's extended view (sneak) shows every line whole. The mark drawn before a line is {@link #MARKS}'
 * (tools/magic_ui.py SENTENCE_MARKS, held equal by check_magic_ui). Pure formatting, and no Jade type here: the Jade
 * adapter (client/compat/JugcraftJadeClient) draws what this gives.
 */
public final class MagicGlance {
	/** Where a sentence's short form lives: this, then the sentence's own key. */
	static final String PREFIX = "screen.jugcraft.glance.";
	private static final String STATUS = "compose.jugcraft.worker.status.";
	private static final String ECOLOGY = "compose.jugcraft.ecology.";
	private static final String JADE = "tooltip.jugcraft.concordance.jade.";

	/** The mark a sentence's line draws, by the sentence's own key (tools/magic_ui.py SENTENCE_MARKS). */
	private static final Map<String, MagicArt.Mark> MARKS = Map.ofEntries(
			Map.entry(STATUS + "awaiting_answer", MagicArt.Mark.ASK),
			Map.entry(STATUS + "blocked_by_access", MagicArt.Mark.CROSS),
			Map.entry(STATUS + "cannot_navigate", MagicArt.Mark.CROSS),
			Map.entry(STATUS + "destination_unloaded", MagicArt.Mark.MOON),
			Map.entry(STATUS + "disabled", MagicArt.Mark.LOCK),
			Map.entry(STATUS + "finished", MagicArt.Mark.TICK),
			Map.entry(STATUS + "following", MagicArt.Mark.TICK),
			Map.entry(STATUS + "full", MagicArt.Mark.HOURGLASS),
			Map.entry(STATUS + "idle", MagicArt.Mark.MOON),
			Map.entry(STATUS + "needs_repair", MagicArt.Mark.HOURGLASS),
			Map.entry(STATUS + "no_energy", MagicArt.Mark.HOURGLASS),
			Map.entry(STATUS + "other_dimension", MagicArt.Mark.MOON),
			Map.entry(STATUS + "outside_agreement", MagicArt.Mark.MOON),
			Map.entry(STATUS + "owner_offline", MagicArt.Mark.MOON),
			Map.entry(STATUS + "returning", MagicArt.Mark.TICK),
			Map.entry(STATUS + "supporting", MagicArt.Mark.TICK),
			Map.entry(STATUS + "suspended", MagicArt.Mark.MOON),
			Map.entry(STATUS + "travelling", MagicArt.Mark.TICK),
			Map.entry(STATUS + "upkeep_due", MagicArt.Mark.HOURGLASS),
			Map.entry(STATUS + "upkeep_held", MagicArt.Mark.MOON),
			Map.entry(STATUS + "waiting_for_resources", MagicArt.Mark.HOURGLASS),
			Map.entry(STATUS + "working", MagicArt.Mark.TICK),
			Map.entry(JADE + "pact.visiting", MagicArt.Mark.ASK),
			Map.entry(JADE + "pact.unreadable", MagicArt.Mark.LOCK),
			Map.entry(JADE + "pact.state.active", MagicArt.Mark.TICK),
			Map.entry(JADE + "pact.state.suspended", MagicArt.Mark.MOON),
			Map.entry(JADE + "pact.state.upkeep_due", MagicArt.Mark.HOURGLASS),
			Map.entry(JADE + "pact.state.upkeep_held", MagicArt.Mark.MOON),
			Map.entry(JADE + "pact.lost", MagicArt.Mark.MOON),
			Map.entry(JADE + "pact.accord", MagicArt.Mark.TICK),
			Map.entry(JADE + "pact.matter.verdance_carrying.two_roads", MagicArt.Mark.ASK),
			Map.entry(JADE + "pact.note.strain_tending.asleep", MagicArt.Mark.MOON),
			Map.entry(JADE + "pact.note.strain_tending.overworked", MagicArt.Mark.HOURGLASS),
			Map.entry(JADE + "pact.note.verdance_carrying.empty", MagicArt.Mark.MOON),
			Map.entry(JADE + "pact.note.verdance_carrying.nowhere", MagicArt.Mark.HOURGLASS),
			Map.entry(JADE + "pact.note.verdance_carrying.two_roads", MagicArt.Mark.ASK),
			// GC-06 unit 3
			Map.entry(JADE + "pact.note.habitat_reading.proud", MagicArt.Mark.HOURGLASS),
			// GC-07 unit 4
			Map.entry(JADE + "pact.matter.graft_keeping.no_gain", MagicArt.Mark.ASK),
			Map.entry(JADE + "pact.note.graft_keeping.no_gain", MagicArt.Mark.ASK),
			Map.entry(JADE + "pact.note.graft_keeping.no_reading", MagicArt.Mark.HOURGLASS),
			Map.entry(JADE + "pact.note.graft_keeping.no_donor", MagicArt.Mark.HOURGLASS),
			Map.entry(JADE + "pact.note.graft_keeping.no_host", MagicArt.Mark.MOON),
			// GC-08 unit 5
			// GC-09 unit 6
			Map.entry("message.jugcraft.concordance.disabled", MagicArt.Mark.LOCK),
			Map.entry("compose.jugcraft.diagnostic.conflicting_agreement", MagicArt.Mark.CROSS),
			Map.entry(JADE + "dormant", MagicArt.Mark.MOON),
			Map.entry(ECOLOGY + "growth.thriving", MagicArt.Mark.TICK),
			Map.entry(ECOLOGY + "growth.tolerating", MagicArt.Mark.HOURGLASS),
			Map.entry(ECOLOGY + "growth.stalled", MagicArt.Mark.CROSS),
			Map.entry(ECOLOGY + "strain", MagicArt.Mark.HOURGLASS),
			Map.entry(ECOLOGY + "short", MagicArt.Mark.HOURGLASS),
			Map.entry(ECOLOGY + "excess", MagicArt.Mark.HOURGLASS),
			Map.entry(ECOLOGY + "tolerable", MagicArt.Mark.HOURGLASS),
			Map.entry(ECOLOGY + "strained.excess", MagicArt.Mark.HOURGLASS),
			Map.entry(ECOLOGY + "strained.tolerable", MagicArt.Mark.HOURGLASS),
			Map.entry(ECOLOGY + "mixed", MagicArt.Mark.TICK),
			Map.entry(ECOLOGY + "status.beds_full", MagicArt.Mark.HOURGLASS),
			Map.entry(ECOLOGY + "status.disabled", MagicArt.Mark.LOCK),
			Map.entry(ECOLOGY + "status.dormant", MagicArt.Mark.MOON),
			Map.entry(ECOLOGY + "status.full", MagicArt.Mark.HOURGLASS),
			Map.entry(ECOLOGY + "status.hungry", MagicArt.Mark.MOON),
			Map.entry(ECOLOGY + "status.idle", MagicArt.Mark.MOON),
			Map.entry(ECOLOGY + "status.keeper_away", MagicArt.Mark.MOON),
			Map.entry(ECOLOGY + "status.no_bed", MagicArt.Mark.CROSS),
			Map.entry(ECOLOGY + "status.no_verdance", MagicArt.Mark.CROSS),
			Map.entry(ECOLOGY + "status.stalled", MagicArt.Mark.CROSS),
			Map.entry(ECOLOGY + "status.starved", MagicArt.Mark.CROSS),
			Map.entry(ECOLOGY + "status.waiting", MagicArt.Mark.MOON),
			Map.entry(ECOLOGY + "status.working", MagicArt.Mark.TICK),
			// Unit 2's review: a departing spirit, and a perch that keeps no agreement.
			Map.entry(JADE + "pact.departing", MagicArt.Mark.MOON),
			Map.entry(JADE + "pact.none", MagicArt.Mark.MOON));

	/** An icon a line draws first: a sprite of the GUI atlas at its own size (a mark, a glyph). */
	public record Icon(Identifier sprite, int width, int height) {
	}

	private MagicGlance() {
	}

	/** {@code full}'s short form, or {@code full} itself when it has none (or it names more arguments than {@code full} has). */
	public static Component of(Component full) {
		if (full.getContents() instanceof TranslatableContents contents) {
			String key = PREFIX + contents.getKey();
			Language language = Language.getInstance();
			if (language.has(key)) {
				int top = PactGlance.highest(language.getOrDefault(key));
				if (top >= 0 && top <= contents.getArgs().length) {
					return Component.translatable(key, contents.getArgs());
				}
			}
		}
		return full;
	}

	/** Whether {@code full} has a short form ({@link #of} gives another sentence). */
	public static boolean isShort(Component full) {
		return of(full) != full;
	}

	/** The mark {@code full}'s line draws, or null (the line draws its own icon, or none). */
	public static @Nullable Icon mark(Component full) {
		MagicArt.Mark mark = markOf(full);
		return mark == null ? null : new Icon(mark.sprite, mark.width, MagicArt.Mark.HEIGHT);
	}

	/** {@link #mark}'s mark itself, for the magic screens (a roster card asked for an answer draws the "?"). */
	static MagicArt.@Nullable Mark markOf(Component full) {
		return full.getContents() instanceof TranslatableContents contents ? MARKS.get(contents.getKey()) : null;
	}

	/** One of the family's glyphs ("calendar", "hourglass", "person", "scroll"...): white, seven rows, at its own width. */
	public static Icon glyph(String name) {
		MagicArt.Glyph glyph = MagicArt.Glyph.valueOf(name.toUpperCase(Locale.ROOT));
		return new Icon(glyph.sprite, glyph.width, MagicArt.Glyph.HEIGHT);
	}

	/** A work's own item (its service line's icon on the pact screen: PactGlance.WORK_ICONS), or an empty stack. */
	public static ItemStack work(String work) {
		return PactGlance.workIcon(work, "");
	}

	/** The sign item of the pact spirit whose entity type is {@code counterpart} (an id), or an empty stack for any other. */
	public static ItemStack sign(String counterpart) {
		for (PactSpirits.Spirit spirit : PactSpirits.all()) {
			if (spirit.counterpart().equals(counterpart)) {
				return PactGlance.item(spirit.sign());
			}
		}
		return ItemStack.EMPTY;
	}
}

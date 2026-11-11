package io.github.jimbozoomer.jugcraft.town.guests;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.Nullable;

/**
 * The requested guests, and only these (owner request, 10 November 2026). Ids are saved in entities and the ledger:
 * never rename or reuse one.
 *
 * <p>Every available guest has the same exclusive companionship mechanics: affection, gifts and quests, one companion
 * at a time. Everything is nonsexual for everyone, and real people get generic, respectful lines only: no invented
 * personal facts, quotes or claims about their lives. One hold: guests whose source work depicts them as minors or
 * school students ({@link Guest#marriage()} false) have companionship but no proposal or marriage. Pending entries
 * (identity or reference unknown) are listed so they are not forgotten; they cannot be placed or befriended until the
 * owner says who they are.
 */
public final class GuestRoster {
	public enum Origin {
		REAL_PERSON, FICTIONAL
	}

	/** One guest: its saved id, shown name, origin, series (fiction), whether still pending, whether it may marry, and why. */
	public record Guest(String id, String name, Origin origin, String series, boolean pending, boolean marriage, String note) {
		/** May be placed and befriended. */
		public boolean available() {
			return !pending;
		}

		/** May be proposed to, at full affection. */
		public boolean mayMarry() {
			return marriage && !pending;
		}
	}

	private static final String REAL = "Real person: generic, respectful lines only; likeness pending reference art.";
	private static final String STUDENT = "Companionship without marriage: depicted as a minor or school student in the source work.";

	public static final List<Guest> ALL = List.of(
			real("emiru", "Emiru", ""),
			real("lauren_chen", "Lauren Chen", ""),
			real("brittany_venti", "Brittany Venti", "Confirmed by the owner."),
			real("aoc", "AOC", ""),
			real("sarah_stock", "Sarah Stock", "Optional non-gory zombie wedding dress appearance (pending art)."),
			real("pokimane", "Pokimane", ""),
			pending("rooni", "Rooni", Origin.REAL_PERSON, "Pending: described only as a slim Black woman; identity and reference unknown."),
			fiction("power", "Power", "Chainsaw Man", ""),
			fiction("makima", "Makima", "Chainsaw Man", ""),
			fiction("reze", "Reze", "Chainsaw Man", ""),
			student("asa_mitaka", "Asa Mitaka", "Chainsaw Man", ""),
			fiction("himeno", "Himeno", "Chainsaw Man", ""),
			student("tokai_teio", "Tokai Teio", "Uma Musume Pretty Derby", "The owner wrote \"Uma Teio\"."),
			student("agnes_tachyon", "Agnes Tachyon", "Uma Musume Pretty Derby", "Confirmed by the owner."),
			student("gold_ship", "Gold Ship", "Uma Musume Pretty Derby", ""),
			student("oguri_cap", "Oguri Cap", "Uma Musume Pretty Derby", ""),
			student("special_week", "Special Week", "Uma Musume Pretty Derby", ""),
			student("rei_ayanami", "Rei Ayanami", "Neon Genesis Evangelion", ""),
			student("asuka_langley", "Asuka Langley", "Neon Genesis Evangelion", ""),
			fiction("misato_katsuragi", "Misato Katsuragi", "Neon Genesis Evangelion", ""),
			fiction("frieren", "Frieren", "Frieren: Beyond Journey's End", ""),
			student("rem", "Rem", "Re:Zero", ""),
			student("megumin", "Megumin", "KonoSuba", ""),
			fiction("zero_two", "Zero Two", "Darling in the Franxx", ""),
			student("rin_ishtar", "Rin Tohsaka / Ishtar", "Fate", "One slot for either; the owner chooses which."),
			fiction("tifa_lockhart", "Tifa Lockhart", "Final Fantasy VII", ""),
			fiction("yorha_2b", "2B", "NieR:Automata", ""),
			fiction("chun_li", "Chun-Li", "Street Fighter", ""),
			fiction("ada_wong", "Ada Wong", "Resident Evil", ""),
			fiction("lara_croft", "Lara Croft", "Tomb Raider", ""),
			fiction("kafka", "Kafka", "Honkai: Star Rail", ""),
			fiction("miyabi", "Hoshimi Miyabi", "Zenless Zone Zero", ""),
			pending("remielle", "Remielle", Origin.FICTIONAL, "Pending: identity and reference unknown."),
			fiction("nicole_demara", "Nicole Demara", "Zenless Zone Zero", ""),
			pending("guest_unnamed", "Unnamed guest", Origin.FICTIONAL, "Pending: an additional guest the owner has not named."),
			pending("jakeey_kpop", "Jakeey's K-pop picks", Origin.REAL_PERSON, "Pending: Jakeey's choices are unknown."));

	private static final Map<String, Guest> BY_ID = new LinkedHashMap<>();

	static {
		for (Guest guest : ALL) {
			if (BY_ID.put(guest.id(), guest) != null || !guest.id().matches("[a-z][a-z0-9_]{1,31}")) {
				throw new IllegalStateException("Bad or repeated guest id " + guest.id());
			}
		}
	}

	private GuestRoster() {
	}

	/** The guest with this id, or null. */
	public static @Nullable Guest get(@Nullable String id) {
		return id == null ? null : BY_ID.get(id);
	}

	private static Guest real(String id, String name, String note) {
		return new Guest(id, name, Origin.REAL_PERSON, "", false, true, note.isEmpty() ? REAL : REAL + " " + note);
	}

	private static Guest fiction(String id, String name, String series, String note) {
		return new Guest(id, name, Origin.FICTIONAL, series, false, true, join("Likeness pending reference art.", note));
	}

	private static Guest student(String id, String name, String series, String note) {
		return new Guest(id, name, Origin.FICTIONAL, series, false, false, join(STUDENT + " Likeness pending reference art.", note));
	}

	private static Guest pending(String id, String name, Origin origin, String note) {
		return new Guest(id, name, origin, "", true, false, note);
	}

	private static String join(String a, String b) {
		return b.isEmpty() ? a : a + " " + b;
	}
}

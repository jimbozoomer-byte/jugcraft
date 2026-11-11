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
 * personal facts, quotes or claims about their lives. Romance is for unambiguous adults only (owner rule, 11 November
 * 2026): guests whose source work depicts them as minors or school students, and guests whose source leaves their age
 * unstated with a youthful presentation, have companionship but no proposal or marriage ({@link Guest#marriage()} false)
 * and no body-shape changes, until an adult identity is resolved; real people keep their own adult identities. Pending entries
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

	private static final String REAL = "Real person: generic, respectful lines only; character-inspired Minecraft skin.";
	private static final String STUDENT = "Companionship without marriage: depicted as a minor or school student in the source work.";
	private static final String UNCERTAIN = "Companionship without marriage: age not stated in the source work; held until an adult identity is resolved.";

	public static final List<Guest> ALL = List.of(
			real("emiru", "Emiru", ""),
			real("lauren_chen", "Lauren Chen", ""),
			real("brittany_venti", "Brittany Venti", "Confirmed by the owner."),
			real("aoc", "AOC", ""),
			real("sarah_stock", "Sarah Stock", "Non-gory zombie wedding dress appearance."),
			real("pokimane", "Pokimane", ""),
			pending("rooni", "Rooni", Origin.REAL_PERSON, "Pending: described only as a slim Black woman; identity and reference unknown."),
			uncertain("power", "Power", "Chainsaw Man"),
			fiction("makima", "Makima", "Chainsaw Man", ""),
			uncertain("reze", "Reze", "Chainsaw Man"),
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
			uncertain("zero_two", "Zero Two", "Darling in the Franxx"),
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

	/** Game-test guests ({@link #test}): never in {@link #ALL}, never made by the mod itself. */
	private static final Map<String, Guest> TEST = new java.util.concurrent.ConcurrentHashMap<>();

	/** The guest with this id, or null. */
	public static @Nullable Guest get(@Nullable String id) {
		return id == null ? null : BY_ID.getOrDefault(id, TEST.get(id));
	}

	/**
	 * Game tests only: a synthetic available guest {@code id} (which must start with "test_"), so tests that bind guests
	 * on the shared test server need not compete for the real ids. The mod never calls this.
	 */
	static Guest test(String id, boolean marriage) {
		if (!id.startsWith("test_") || !id.matches("[a-z][a-z0-9_]{1,31}") || BY_ID.containsKey(id)) {
			throw new IllegalArgumentException("Not a test guest id: " + id);
		}
		return TEST.computeIfAbsent(id, key -> new Guest(key, "Test " + key.substring(5), Origin.FICTIONAL, "Game test", false, marriage,
				"A game-test guest."));
	}

	private static Guest real(String id, String name, String note) {
		return new Guest(id, name, Origin.REAL_PERSON, "", false, true, note.isEmpty() ? REAL : REAL + " " + note);
	}

	private static Guest fiction(String id, String name, String series, String note) {
		return new Guest(id, name, Origin.FICTIONAL, series, false, true, join("Character-inspired Minecraft skin.", note));
	}

	private static Guest student(String id, String name, String series, String note) {
		return new Guest(id, name, Origin.FICTIONAL, series, false, false, join(STUDENT + " Character-inspired Minecraft skin.", note));
	}

	private static Guest uncertain(String id, String name, String series) {
		return new Guest(id, name, Origin.FICTIONAL, series, false, false, UNCERTAIN + " Character-inspired Minecraft skin.");
	}

	private static Guest pending(String id, String name, Origin origin, String note) {
		return new Guest(id, name, origin, "", true, false, note);
	}

	private static String join(String a, String b) {
		return b.isEmpty() ? a : a + " " + b;
	}
}

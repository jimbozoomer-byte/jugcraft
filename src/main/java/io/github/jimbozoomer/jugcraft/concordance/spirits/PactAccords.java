package io.github.jimbozoomer.jugcraft.concordance.spirits;

import io.github.jimbozoomer.jugcraft.concordance.Authority;
import io.github.jimbozoomer.jugcraft.concordance.resource.BoundWill;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.jspecify.annotations.Nullable;

/**
 * Concordance garden cohort, GC-05 (GC-S11; docs/features/bathin-pact.md, "His matter"): a pact's accord, its holder's
 * answer to the matter its spirit raises. The perch keeps it as one optional saved string (PactPerchBlockEntity's
 * "accord": one of its service's accords(), at most {@value #MAX_ACCORD} characters; absent while unanswered; read as
 * unanswered when this version does not know it, and kept as written; gone with the pact it answers, and never on the
 * Bound Will or the spirit). Until it is answered the service keeps to its safe course (its accords' first), and while a
 * choice stands its presence reports AWAITING_ANSWER (the roster's "awaiting your answer"). The holder answers on the kept
 * page (PactMenu's RESOLVE), in person, and may change the answer at any time; nothing is spent, lost or held while a
 * choice waits, and no one else answers. Everything here reads only, but {@link #resolve}.
 */
public final class PactAccords {
	/** The longest accord a perch keeps (a saved string; tools/concordance_pacts.py ACCORD_MAX). */
	public static final int MAX_ACCORD = 16;
	private static final String KEY = "message.jugcraft.concordance.pact.";

	/**
	 * A matter as its kept page and Jade show it: its pact's work, the id of the matter raised now (null when none is and
	 * only an answer is kept), its line, the answers it may be given (the safe course first), the answer kept (or null) and
	 * whether a choice waits (one is raised and none is kept).
	 */
	public record Matter(String work, @Nullable String id, Component line, List<String> answers, @Nullable String answer, boolean awaiting) {
		public Matter {
			answers = List.copyOf(answers);
		}
	}

	private PactAccords() {
	}

	/** {@code record}'s service, or null (no pact, no service, a work this version does not know). */
	static @Nullable PactService service(@Nullable BoundWill record) {
		BoundWill.Service terms = record == null || record.pact() == null ? null : record.pact().service();
		return terms == null ? null : PactServices.get(terms.work());
	}

	/** Whether {@code value} is one of {@code service}'s answers. */
	public static boolean known(@Nullable PactService service, @Nullable String value) {
		return service != null && value != null && service.accords().contains(value);
	}

	/**
	 * The answer kept at {@code perch} for the pact it keeps now, or null: unanswered, one its service does not know, or no
	 * pact counting there (a copy, a record ended or unreadable). Read only: the holder peeked, never verified.
	 */
	public static @Nullable String answer(ServerLevel level, PactPerchBlockEntity perch) {
		String kept = perch.accord();
		if (kept == null || perch.holder().peek(level) != AgreementHolder.Standing.VALID) {
			return null;
		}
		return known(service(perch.holder().record(level)), kept) ? kept : null;
	}

	/** The course {@code service} keeps to at {@code perch}: its holder's answer, else its first accord (the safe course); "" for none. */
	public static String course(ServerLevel level, PactPerchBlockEntity perch, PactService service) {
		List<String> accords = service.accords();
		if (accords.isEmpty()) {
			return "";
		}
		String kept = answer(level, perch);
		return kept != null && accords.contains(kept) ? kept : accords.getFirst();
	}

	/**
	 * The matter at {@code perch} for {@code record}'s pact at game time {@code now}, or null. Shown while its service has
	 * accords and either a choice is raised now (its complication with answers; awaiting while none is kept) or an answer is
	 * kept (then, when none is raised now, in its work's general words: <work>.question). A complication without answers is
	 * a task, never shown here. Its answers are those of its accords the complication names, at most
	 * PactStatePayload.MAX_ANSWERS. Read only and bounded (PactService.complication's contract).
	 */
	public static @Nullable Matter matter(ServerLevel level, PactPerchBlockEntity perch, BoundWill record, long now) {
		PactService service = service(record);
		if (service == null || service.accords().isEmpty()) {
			return null;
		}
		String work = record.pact().service().work();
		String kept = answer(level, perch);
		PactService.Complication raised = service.complication(level, record, perch, now);
		List<String> answers = raised == null ? List.of() : raised.answers().stream().filter(service.accords()::contains).distinct()
				.limit(PactStatePayload.MAX_ANSWERS).toList();
		if (!answers.isEmpty()) {
			return new Matter(work, raised.id(), raised.line(), answers, kept, kept == null);
		}
		if (kept != null) {
			List<String> all = service.accords().stream().limit(PactStatePayload.MAX_ANSWERS).toList();
			return new Matter(work, null, Component.translatable(KEY + work + ".question"), all, kept, false);
		}
		return null;
	}

	/** Whether a choice waits at {@code perch} now: one is raised and none is kept (PactSpiritEntity's AWAITING_ANSWER). Read only. */
	public static boolean awaiting(ServerLevel level, PactPerchBlockEntity perch, BoundWill record, long now) {
		Matter matter = matter(level, perch, record, now);
		return matter != null && matter.awaiting();
	}

	/** An answer's words (message.jugcraft.concordance.pact.<work>.accord.<value>), its id if they are missing. */
	public static Component answerName(String work, String value) {
		return Component.translatableWithFallback(KEY + work + ".accord." + value, value);
	}

	/**
	 * Its holder answers the matter of the pact kept at {@code perch} with {@code value} (PactMenu's RESOLVE): only with
	 * the Concordance on, never a stand-in, only a living player in person who holds the record kept there, only for the
	 * pact of work {@code work} the page showed, and only one of its service's answers. Kept as the perch's accord; true
	 * when it is kept (the same answer again included). Refused with nothing written otherwise. Nothing is spent.
	 */
	public static boolean resolve(ServerLevel level, ServerPlayer player, PactPerchBlockEntity perch, String work, String value, long now) {
		if (!Workers.enabled() || Authority.isStandIn(player) || !player.isAlive()) {
			return false;
		}
		if (perch.holder().verify(level, now) != AgreementHolder.Standing.VALID) {
			return false;
		}
		BoundWill record = perch.holder().record(level);
		BoundWill.Service terms = record == null || record.pact() == null ? null : record.pact().service();
		if (terms == null || !record.holder().equals(player.getUUID()) || !terms.work().equals(work)) {
			return false;
		}
		PactService service = PactServices.get(terms.work());
		if (!known(service, value) || value.length() > MAX_ACCORD) {
			return false;
		}
		perch.keepAccord(value);
		return true;
	}
}

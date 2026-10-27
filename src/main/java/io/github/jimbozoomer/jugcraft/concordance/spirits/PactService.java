package io.github.jimbozoomer.jugcraft.concordance.spirits;

import io.github.jimbozoomer.jugcraft.concordance.resource.BoundWill;
import io.github.jimbozoomer.jugcraft.concordance.worker.PactDefinition;
import io.github.jimbozoomer.jugcraft.concordance.worker.Status;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.jspecify.annotations.Nullable;

/**
 * Concordance garden cohort, GC-01 (SG1): what one pact service does, keyed by the work a pact's record names
 * (BoundWill.Service.work; worker/PactParser.WORK). Each is a small Java class (03:177: no scripting language, no rule
 * interpreter); PactServices registers each once and refuses to start without one for every work the parser accepts. A
 * record whose work has none (a newer save read by an older build) still loads, lists, suspends and ends: its page says
 * its service is not known and offers no service action. Later members land with their first user: the first
 * obligation's act (GC-03), a complication and its accords (GC-05).
 */
public interface PactService {
	/** Why an invitation waits beyond research, ownership and a pact already held: a reason id (one of PactMenu's ANSWERS) and its words. */
	record Missing(String reason, Component note) {
	}

	/** Its work id (one of PactParser.WORK). */
	String work();

	/** Whether it serves its holder alone (PactParser.HOLDER_ONLY: its beneficiaries must be personal). */
	boolean holderOnly();

	/** What it still asks of {@code inviter} at {@code perch} before its spirit will come, in order (Stolas: a star observed); empty when nothing. Reads only. */
	List<Missing> missing(ServerLevel level, ServerPlayer inviter, PactPerchBlockEntity perch, PactDefinition definition);

	/** The choices its on-request action offers the holder now (at most PactStatePayload.MAX_CHOICES), each with its id; empty when it has none. Reads only. */
	List<PactStatePayload.Choice> choices(ServerLevel level, ServerPlayer player, BoundWill record, long now);

	/**
	 * The holder performs it with the choice of id {@code choice}, at world time {@code time} (the screen's RECALL action):
	 * its answer, or null when it has no on-request action. Refused with one reason before anything is spent.
	 */
	PactMenu.@Nullable Answered perform(ServerLevel level, ServerPlayer player, AgreementHolder holder, String choice, long time, long now);

	/** Its rows on the kept page where Chunk D's service row stood (what it does, how much is left today). */
	List<Component> serviceRows(ServerLevel level, ServerPlayer player, BoundWill record, long now);

	/** Its rows on the kept page where Chunk D's first-obligation row stood. */
	List<Component> obligationRows(ServerLevel level, ServerPlayer player, BoundWill record, long now);

	/** The kept page's first row while its holder has suspended it ({@code name}: its spirit's). */
	Component suspendedRow(Component name);

	/**
	 * Concordance garden cohort, GC-03 (GC-P7): the act by hand that is this service's first obligation, which
	 * PactObligations records as the practice jugcraft:garden_pact_service keyed by its spirit; null when it has none there
	 * (Star-reckoning's stays at the observatory: StarReckoning.observed). Each garden service names its own act.
	 */
	default PactObligations.@Nullable Act obligation() {
		return null;
	}

	/**
	 * One autonomous step for {@code spirit}'s pact, run only after its decide returned IDLE (a verified presence at its
	 * perch, its pact serving, its holder near): at most one change; the status it leaves. Default: none (Stolas).
	 */
	default Status work(ServerLevel level, PactSpiritEntity<?> spirit, BoundWill record, AgreementHolder holder, long time, long now) {
		return Status.IDLE;
	}

	/**
	 * Concordance garden cohort, GC-05 (GC-S11): a matter a pact spirit raises in play. Its id (one of its work's matters:
	 * tools/concordance_pacts.py SERVICE_WORDS), the line its kept page asks it in, the block it concerns (or null), and the
	 * answers it may be given: its service's accords() (a choice, kept as the perch's accord, PactAccords), or none (a
	 * task: done in the world, said by the service's own rows, never answered here).
	 */
	record Complication(String id, Component line, @Nullable BlockPos pos, List<String> answers) {
		public Complication {
			answers = List.copyOf(answers);
		}
	}

	/**
	 * GC-05: the one matter its spirit raises now for {@code record}'s pact kept at {@code perch}, at game time {@code now},
	 * or null (default: none; Star-reckoning's, strain tending's). Read only and bounded: it changes no block, record or
	 * entity, takes no sample and loads no chunk, and reads at most what one of its thinks reads (its presence's memory, its
	 * bed index, the loaded block entities they name). Asked by its presence's think (the roster's status), its kept page
	 * and Jade.
	 */
	default @Nullable Complication complication(ServerLevel level, BoundWill record, PactPerchBlockEntity perch, long now) {
		return null;
	}

	/**
	 * GC-05: the answers its choice may be given, the safe course first (what it does until its holder answers): each a
	 * saved string of at most PactAccords.MAX_ACCORD characters, at most PactStatePayload.MAX_ANSWERS of them, closed and
	 * append only (a perch's saved accord names one; check_pacts' GC05_ACCORDS pins them). Empty when its matters are tasks
	 * or it raises none (default).
	 */
	default List<String> accords() {
		return List.of();
	}
}

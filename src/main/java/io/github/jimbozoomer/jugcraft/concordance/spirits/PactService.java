package io.github.jimbozoomer.jugcraft.concordance.spirits;

import io.github.jimbozoomer.jugcraft.concordance.resource.BoundWill;
import io.github.jimbozoomer.jugcraft.concordance.worker.PactDefinition;
import io.github.jimbozoomer.jugcraft.concordance.worker.Status;
import java.util.List;
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
	 * One autonomous step for {@code spirit}'s pact, run only after its decide returned IDLE (a verified presence at its
	 * perch, its pact serving, its holder near): at most one change; the status it leaves. Default: none (Stolas).
	 */
	default Status work(ServerLevel level, PactSpiritEntity<?> spirit, BoundWill record, AgreementHolder holder, long time, long now) {
		return Status.IDLE;
	}
}

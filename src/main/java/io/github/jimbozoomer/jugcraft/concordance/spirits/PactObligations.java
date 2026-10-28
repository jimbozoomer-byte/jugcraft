package io.github.jimbozoomer.jugcraft.concordance.spirits;

import io.github.jimbozoomer.jugcraft.concordance.Authority;
import io.github.jimbozoomer.jugcraft.concordance.ConcordanceProgress;
import io.github.jimbozoomer.jugcraft.concordance.courier.Couriers;
import io.github.jimbozoomer.jugcraft.concordance.logistics.Place;
import io.github.jimbozoomer.jugcraft.concordance.resource.BoundWill;
import io.github.jimbozoomer.jugcraft.concordance.rules.Evidence;
import io.github.jimbozoomer.jugcraft.concordance.rules.Knowledge;
import io.github.jimbozoomer.jugcraft.concordance.rules.ResearchEngine;
import io.github.jimbozoomer.jugcraft.concordance.rules.ResearchState;
import io.github.jimbozoomer.jugcraft.concordance.worker.Pacts;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/**
 * Concordance garden cohort, GC-03 (GC-S9, GC-P7, GC-P31): a garden pact's first obligation, an act the holder does by
 * hand inside the pact's area while it is active. Each garden service names its act (PactService.obligation); the act's
 * one-line hook is in the gesture that does it, on success (tools/concordance_garden_pacts.py HOOKS). It records the
 * practice GardenPacts.ACTIVITY keyed by the spirit, once a player a spirit, which counts toward mastering The Garden's
 * Regard; never Stolas's jugcraft:pact_service (his stays at the observatory: StarReckoning.observed), never the Binding
 * Arts' worker_service, never cultivation (Don'ts 6 and 7). Nothing for a stand-in, with the Concordance off, for a pact
 * suspended, held for upkeep or without its perch, or for an act outside the area.
 */
public final class PactObligations {
	/** The acts by hand a garden service may take as its first obligation. Ids are words (lang), never saved. Append only: unit 2 DEVICE, 3 READ, 4 GRAFT, 5 FEED, 6 HARVEST. Keep equal to tools/concordance_garden_pacts.py ACTS. */
	public enum Act {
		TEND("tend"),
		/** GC-05 (Bathin): an awake living device (a Verdant Heart, Gleaner, Mulch Maw or Habitat Gauge) read or set with an empty hand. */
		DEVICE("device");

		public final String id;

		Act(String id) {
			this.id = id;
		}
	}

	private PactObligations() {
	}

	/**
	 * {@code player} did {@code act} by hand at {@code pos} in {@code level} (its hook, after the gesture succeeded). For each pact
	 * they hold whose service's first obligation is {@code act}, that is active (seated, neither suspended nor held for
	 * upkeep: Pacts.serving, as Stolas's), kept by its perch (not stranded) and whose area, in this level, holds {@code pos}:
	 * records the practice once for that spirit (a pact's hours and day's quota do not matter: the act is the holder's, not
	 * the spirit's; GC-P31). A spirit's obligation already kept is not recorded again, so the Conclave hears it once. At most
	 * 32 records are looked at (BoundWillLedger.MAX_PER_HOLDER). Returns whether it recorded one.
	 */
	public static boolean performed(ServerLevel level, ServerPlayer player, Act act, BlockPos pos) {
		if (!Workers.enabled() || Authority.isStandIn(player)) {
			return false;
		}
		MinecraftServer server = level.getServer();
		Place at = Couriers.place(level, pos);
		boolean recorded = false;
		for (BoundWill record : BoundWills.of(server).ledger().heldBy(player.getUUID())) {
			BoundWill.Pact pact = record.pact();
			BoundWill.Service service = pact == null ? null : pact.service();
			PactService work = service == null ? null : PactServices.get(service.work());
			if (work == null || work.obligation() != act || !Pacts.serving(record) || !pact.place().within(at, service.radius())
					|| AgreementHolder.stranded(server, record)) {
				continue;
			}
			String counterpart = Pacts.counterpart(record.counterpart());
			if (kept(player, counterpart)) {
				continue;
			}
			ResearchEngine.Result result = ConcordanceProgress.record(player, new Evidence.Practiced(GardenPacts.ACTIVITY, counterpart));
			if (result.recorded()) {
				player.sendSystemMessage(Component.translatable("message.jugcraft.concordance.pact.obligation_kept", PactKeeping.name(record.counterpart())));
				recorded = true;
			}
		}
		return recorded;
	}

	/** The evidence key {@code counterpart}'s first obligation records under The Garden's Regard. */
	public static String key(String counterpart) {
		return "practice:" + GardenPacts.ACTIVITY + ":" + Pacts.counterpart(counterpart);
	}

	/** Whether {@code player} has kept {@code counterpart}'s first obligation (its key held), or mastered The Garden's Regard. */
	public static boolean kept(Player player, String counterpart) {
		Knowledge.Progress progress = ConcordanceProgress.knowledge(player).progress(GardenPacts.RESEARCH);
		return progress.state().atLeast(ResearchState.MASTERED) || progress.evidence().containsKey(key(counterpart));
	}

	/** How many different garden spirits' first obligations {@code player} has kept (GardenPacts.MASTERY_SPIRITS once mastered). */
	public static int spiritsKept(Player player) {
		Knowledge.Progress progress = ConcordanceProgress.knowledge(player).progress(GardenPacts.RESEARCH);
		if (progress.state().atLeast(ResearchState.MASTERED)) {
			return Math.max(GardenPacts.MASTERY_SPIRITS, count(progress));
		}
		return count(progress);
	}

	private static int count(Knowledge.Progress progress) {
		String prefix = "practice:" + GardenPacts.ACTIVITY + ":";
		int count = 0;
		for (String key : progress.evidence().keySet()) {
			if (key.startsWith(prefix)) {
				count++;
			}
		}
		return count;
	}

	/**
	 * The kept page's first-obligation row for a garden service whose act is {@code act} (each garden PactService's
	 * obligationRows calls it, so no spirit words its own): what to do, or that it is kept and how far toward mastery. Reads only.
	 */
	public static List<Component> rows(Player player, BoundWill record, Act act) {
		return List.of(kept(player, record.counterpart())
				? Component.translatable("message.jugcraft.concordance.garden_pact.obligation_kept", spiritsKept(player), GardenPacts.MASTERY_SPIRITS)
				: Component.translatable("message.jugcraft.concordance.garden_pact.obligation",
						Component.translatable("message.jugcraft.concordance.garden_pact.act." + act.id)));
	}
}

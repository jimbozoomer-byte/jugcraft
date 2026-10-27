package io.github.jimbozoomer.jugcraft.concordance.spirits;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.concordance.Authority;
import io.github.jimbozoomer.jugcraft.concordance.courier.Couriers;
import io.github.jimbozoomer.jugcraft.concordance.resource.BoundWill;
import io.github.jimbozoomer.jugcraft.concordance.worker.Pacts;
import io.github.jimbozoomer.jugcraft.concordance.worker.Status;
import io.github.jimbozoomer.jugcraft.party.JugcraftParties;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import org.jspecify.annotations.Nullable;

/**
 * Concordance garden cohort (docs/features/concordance-garden-cohort.md), GC-03: what every garden pact shares.
 * <ul>
 * <li>The Garden's Regard ({@value #RESEARCH}, tools/concordance_garden_pacts.py), the cohort's one research: the
 * Spiritbinders' practitioner entry, begun once the Binding Arts and Verdant Husbandry are understood; encountered by
 * examining a garden specimen ({@link #SPECIMENS}), observed by cultivation, understood by a bound worker's service, and
 * mastered by the first obligations of {@value #MASTERY_SPIRITS} different garden spirits ({@value #ACTIVITY}, recorded only
 * by PactObligations). Its gate is practice/garden_pact_service. Nothing here records or grants.</li>
 * <li>Whose land a garden service may touch ({@link #target}, GC-S7): only where its pact serves now, only a beneficiary's,
 * only while its holder is near its perch, and only what that holder could change by hand, asked at each change. No
 * stand-in ever answers: with the holder away the spirit waits, so a service never runs while no upkeep accrues.</li>
 * </ul>
 */
public final class GardenPacts {
	/** The Garden's Regard. Keep equal to tools/concordance_garden_pacts.py REGARD. */
	public static final String RESEARCH = "jugcraft:garden_regard";
	/** The practice a garden pact's first obligation records, keyed by its spirit. Its gate is practice/garden_pact_service. */
	public static final String ACTIVITY = "jugcraft:garden_pact_service";
	/** What a player examines to begin The Garden's Regard: the organism crops' produce and Verdant Chaff. */
	public static final TagKey<Item> SPECIMENS = TagKey.create(Registries.ITEM, Jugcraft.id("garden_regard_specimens"));
	/** How many different garden spirits' first obligations master it (its mastered rule's distinct; GC-S9). */
	public static final int MASTERY_SPIRITS = 3;

	private GardenPacts() {
	}

	/** Whether {@code record}'s pact benefits whoever keeps a block ({@code keeper}; nobody: no): its holder; their party under party. */
	public static boolean benefits(BoundWill record, @Nullable UUID keeper) {
		return keeper != null && Pacts.benefits(record, keeper, JugcraftParties.sameParty(record.holder(), keeper));
	}

	/**
	 * Why {@code record}'s garden service may not change the block at {@code pos} (in {@code level}, kept by {@code keeper}) at
	 * world time {@code time} and game time {@code now}, or null when it may (GC-S7, S3, S7). Called only after the presence's
	 * decide returned IDLE (PactSpiritEntity.think), and at each change, never from a cache. In this order, cheapest first:
	 * its pact does not serve there now (Pacts.serves: no service or no perch, another level or outside its area, outside its
	 * hours: OUTSIDE_AGREEMENT; suspended: SUSPENDED; held for upkeep: UPKEEP_HELD; its day's quota spent: FINISHED); the
	 * keeper is nobody or no beneficiary at this moment (IDLE: skipped); its holder is not online, alive, in this level and
	 * within Pacts.REACH of the perch (OWNER_OFFLINE: the test upkeep uses, never a stand-in, whatever the server's
	 * absent-owner option); its holder could not change the block by hand (BLOCKED_BY_ACCESS: claims, towns, spawn, the world
	 * border, every protection mod). It only reads.
	 */
	public static @Nullable Status target(ServerLevel level, BoundWill record, BlockPos pos, @Nullable UUID keeper, long time, long now) {
		Pacts.Reckoning serves = Pacts.serves(record, Couriers.place(level, pos), time, now);
		if (serves != null) {
			return switch (serves) {
				case SUSPENDED -> Status.SUSPENDED;
				case UPKEEP_HELD -> Status.UPKEEP_HELD;
				case QUOTA_SPENT -> Status.FINISHED;
				default -> Status.OUTSIDE_AGREEMENT;
			};
		}
		if (!benefits(record, keeper)) {
			return Status.IDLE;
		}
		ServerPlayer holder = Authority.presentNear(level, record.holder(), Couriers.pos(record.pact().place()), Pacts.REACH);
		if (holder == null) {
			return Status.OWNER_OFFLINE;
		}
		return Authority.mayChange(level, holder, pos) ? null : Status.BLOCKED_BY_ACCESS;
	}
}

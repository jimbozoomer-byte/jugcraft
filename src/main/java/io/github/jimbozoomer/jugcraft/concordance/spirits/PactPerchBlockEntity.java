package io.github.jimbozoomer.jugcraft.concordance.spirits;

import io.github.jimbozoomer.jugcraft.concordance.ConcordanceData;
import io.github.jimbozoomer.jugcraft.concordance.Saved;
import io.github.jimbozoomer.jugcraft.concordance.resource.BoundWill;
import io.github.jimbozoomer.jugcraft.concordance.worker.PactDefinition;
import io.github.jimbozoomer.jugcraft.concordance.worker.Pacts;
import io.github.jimbozoomer.jugcraft.party.UseMode;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/**
 * Concordance Chunk D, D-04 (D-S6, D-S7): the pact perch. It keeps a pact through an {@link AgreementHolder} (its owner,
 * the terms as sealed here, the presence serving it) and holds no item: the escrow and the terms are the Bound Will's, so
 * no hopper, pipe, explosion or copied block reaches them, and a copy elsewhere is inert. Broken or blown up with the
 * Concordance on, its pact is left without its perch (OnBreak.UNSEAT). Once a second, spread by place, it looks at its
 * pact (PactKeeping.tick: the upkeep, and a lost presence called back while its holder is near). While a conversation is
 * open and nothing is kept here, one visiting Stolas waits on it; the visitor is never saved, so a perch read from its
 * save has none.
 */
public class PactPerchBlockEntity extends BlockEntity implements AgreementHolder.Holds {
	/** How often the perch looks at its pact: once a second, spread by position (tools/concordance_pacts.py SESSION). */
	public static final int LOOK_TICKS = 20;

	private final AgreementHolder holder = new AgreementHolder(this, AgreementHolder.OnBreak.UNSEAT, true);
	private @Nullable UUID visitor;

	public PactPerchBlockEntity(BlockPos pos, BlockState state) {
		super(Workers.PERCH_ENTITY, pos, state);
	}

	@Override
	public AgreementHolder holder() {
		return holder;
	}

	/** The visiting Stolas while a conversation is open here (never saved). */
	public @Nullable UUID visitor() {
		return visitor;
	}

	/** Stolas's offer in force (data/jugcraft/concordance/pact/stolas.json), or null while it did not load. */
	public static @Nullable PactDefinition definition() {
		return ConcordanceData.rules().pacts().get(PactDefinition.STOLAS);
	}

	/** What the perch seals: {@code d}'s terms copied onto the record, one a holder, its owner alone (D-S8), the compact paid by the seal itself (D-S13). */
	public static AgreementHolder.Sealing sealing(PactDefinition d, @Nullable Predicate<ServerPlayer> affords, @Nullable Consumer<ServerPlayer> pay) {
		return new AgreementHolder.Sealing(d.agreement(), d.id(), new BoundWill.Service(d.work(), d.radius(), d.from(), d.to(), d.quota()),
				Pacts.terms(d.offering(), d.count(), d.period(), d.grace(), d.cap()), UseMode.valueOf(d.beneficiaries().toUpperCase(Locale.ROOT)),
				true, false, affords, pay);
	}

	/** An invitation opened here: Stolas arrives, one visitor at a perch, none while a pact is kept here (D-S7). */
	@Nullable StolasEntity welcome(ServerLevel level) {
		if (holder.agreement() != null) {
			return null;
		}
		StolasEntity guest = unbound(level);
		if (guest != null) {
			return guest;
		}
		guest = summon(level, null);
		visitor = guest == null ? null : guest.getUUID();
		return guest;
	}

	/** The pact's presence for {@code who}: the visitor, if one waits here, becomes it (D-S7); otherwise Stolas is called. */
	@Override
	public @Nullable WorkerEntity<?> call(ServerLevel level, UUID who, String counterpart) {
		if (!PactDefinition.STOLAS.equals(Pacts.counterpart(counterpart))) {
			return null;
		}
		StolasEntity guest = unbound(level);
		if (guest != null) {
			visitor = null;
			guest.setOwner(who);
			return guest;
		}
		return summon(level, who);
	}

	private @Nullable StolasEntity unbound(ServerLevel level) {
		return visitor != null && level.getEntity(visitor) instanceof StolasEntity guest && !guest.isRemoved() && guest.owner() == null ? guest : null;
	}

	private @Nullable StolasEntity summon(ServerLevel level, @Nullable UUID who) {
		StolasEntity stolas = Workers.STOLAS.create(level, EntitySpawnReason.MOB_SUMMONED);
		if (stolas == null) {
			return null;
		}
		BlockPos pos = getBlockPos();
		stolas.setOwner(who);
		stolas.setPerch(pos);
		stolas.snapTo(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 0.0F, 0.0F);
		return level.addFreshEntity(stolas) ? stolas : null;
	}

	/** Its ticker (server only): its pact, once a second, spread by position. */
	public static void serverTick(Level level, BlockPos pos, BlockState state, PactPerchBlockEntity perch) {
		if (level instanceof ServerLevel server && Math.floorMod(server.getGameTime() + pos.asLong(), LOOK_TICKS) == 0) {
			PactKeeping.tick(server, perch.holder, pos, server.getGameTime());
		}
	}

	/** Broken or blown up: its pact is left without its perch (never ended), and a visitor leaves. */
	@Override
	public void preRemoveSideEffects(BlockPos pos, BlockState state) {
		if (level instanceof ServerLevel server) {
			holder.broken(server, server.getGameTime());
			StolasEntity guest = unbound(server);
			if (guest != null) {
				guest.leave(server, null, pos);
			}
			visitor = null;
		}
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		holder.load(input);
		visitor = null;
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		Saved.stamp(output, 1);
		holder.save(output);
	}
}

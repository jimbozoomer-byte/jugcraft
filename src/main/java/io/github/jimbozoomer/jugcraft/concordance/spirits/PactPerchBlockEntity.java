package io.github.jimbozoomer.jugcraft.concordance.spirits;

import io.github.jimbozoomer.jugcraft.concordance.ConcordanceData;
import io.github.jimbozoomer.jugcraft.concordance.Saved;
import io.github.jimbozoomer.jugcraft.concordance.resource.BoundWill;
import io.github.jimbozoomer.jugcraft.concordance.worker.PactDefinition;
import io.github.jimbozoomer.jugcraft.concordance.worker.PactParser;
import io.github.jimbozoomer.jugcraft.concordance.worker.Pacts;
import io.github.jimbozoomer.jugcraft.party.UseMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeSet;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Concordance Chunk D, D-04 (D-S6, D-S7): the pact perch. It keeps a pact through an {@link AgreementHolder} (its owner,
 * the terms as sealed here, the presence serving it) and holds no item: the escrow and the terms are the Bound Will's, so
 * no hopper, pipe, explosion or copied block reaches them, and a copy elsewhere is inert. Broken or blown up with the
 * Concordance on, its pact is left without its perch (OnBreak.UNSEAT). Once a second, spread by place, it looks at its
 * pact (PactKeeping.tick: the upkeep, and a lost presence called back while its holder is near). While a conversation is
 * open and nothing is kept here, one visiting spirit waits on it; the visitor is never saved, so a perch read from its
 * save has none. It keeps one more thing of its pact's (GC-05): its holder's answer to its spirit's matter, the accord,
 * saved while that pact is kept here and gone with it.
 */
public class PactPerchBlockEntity extends BlockEntity implements AgreementHolder.Holds {
	/** How often the perch looks at its pact: once a second, spread by position (tools/concordance_pacts.py SESSION). */
	public static final int LOOK_TICKS = 20;

	private final AgreementHolder holder = new AgreementHolder(this, AgreementHolder.OnBreak.UNSEAT, true);
	private @Nullable UUID visitor;
	/**
	 * GC-05 (GC-P42, GC-P46): its pact's accord, its holder's answer to its spirit's matter (PactAccords), as saved; null
	 * while unanswered. Readable and saved only while a pact is kept here; a pact sealed or seated here starts unanswered.
	 */
	private @Nullable String accord;

	public PactPerchBlockEntity(BlockPos pos, BlockState state) {
		super(Workers.PERCH_ENTITY, pos, state);
	}

	@Override
	public AgreementHolder holder() {
		return holder;
	}

	/** The visiting spirit's id while a conversation is open here (never saved). */
	public @Nullable UUID visitor() {
		return visitor;
	}

	/** GC-05: the accord as kept here (raw; PactAccords.answer reads it against its service's answers); null while no pact is kept here. */
	public @Nullable String accord() {
		return holder.agreement() == null ? null : accord;
	}

	/** GC-05: its holder's answer is kept here. PactAccords.resolve alone calls this, after every check of its own. */
	void keepAccord(String value) {
		if (!value.equals(accord)) {
			accord = value;
			setChanged();
		}
	}

	/**
	 * The pact spirit at this perch now (GC-01, SG3): the presence of the pact kept here if it is in the world and perched
	 * here, else the visitor while one waits; null when neither is, or the one there is leaving (PactSpiritEntity.gone). A
	 * copy of this perch names the original's presence, who is perched at the original: never this perch's.
	 */
	public @Nullable PactSpiritEntity<?> presence(ServerLevel level) {
		UUID id = holder.spirit();
		if (id != null && level.getEntity(id) instanceof PactSpiritEntity<?> presence && !presence.gone() && getBlockPos().equals(presence.perch())) {
			return presence;
		}
		return visitor(level);
	}

	/** Stolas at this perch now, as {@link #presence} finds him (Chunk D's view of the perch, kept for his tests and evidence). */
	public @Nullable StolasEntity stolas(ServerLevel level) {
		return presence(level) instanceof StolasEntity stolas ? stolas : null;
	}

	/** The visitor waiting here: bound to no one, not leaving; null when none. */
	public @Nullable PactSpiritEntity<?> visitor(ServerLevel level) {
		return visitor != null && level.getEntity(visitor) instanceof PactSpiritEntity<?> guest && !guest.gone() && guest.owner() == null ? guest : null;
	}

	/** Stolas's offer in force (data/jugcraft/concordance/pact/stolas.json), or null while it did not load (Chunk D's; his tests read it). */
	public static @Nullable PactDefinition definition() {
		return definition(PactDefinition.STOLAS);
	}

	/** {@code counterpart}'s offer in force, or null while it did not load. */
	public static @Nullable PactDefinition definition(String counterpart) {
		return ConcordanceData.rules().pacts().get(Pacts.counterpart(counterpart));
	}

	/**
	 * GC-01 (SG4, GC-P15): the offers a perch makes, in the order the game ships them (PactParser.REQUIRED), then any other
	 * loaded offer by id; only those whose counterpart is a registered pact spirit (PactSpirits), since nothing else could come.
	 */
	public static List<PactDefinition> offered() {
		Map<String, PactDefinition> loaded = ConcordanceData.rules().pacts().pacts();
		List<PactDefinition> out = new ArrayList<>();
		for (String id : PactParser.REQUIRED) {
			PactDefinition offer = loaded.get(id);
			if (offer != null && PactSpirits.knows(id)) {
				out.add(offer);
			}
		}
		for (String id : new TreeSet<>(loaded.keySet())) {
			if (!PactParser.REQUIRED.contains(id) && PactSpirits.knows(id)) {
				out.add(loaded.get(id));
			}
		}
		return List.copyOf(out);
	}

	/** What the perch seals: {@code d}'s terms copied onto the record, one a holder, its owner alone (D-S8), the compact paid by the seal itself (D-S13). */
	public static AgreementHolder.Sealing sealing(PactDefinition d, @Nullable Predicate<ServerPlayer> affords, @Nullable Consumer<ServerPlayer> pay) {
		return new AgreementHolder.Sealing(d.agreement(), d.id(), new BoundWill.Service(d.work(), d.radius(), d.from(), d.to(), d.quota()),
				Pacts.terms(d.offering(), d.count(), d.period(), d.grace(), d.cap()), UseMode.valueOf(d.beneficiaries().toUpperCase(Locale.ROOT)),
				true, false, affords, pay);
	}

	/**
	 * An invitation opened here: {@code counterpart} arrives, one visitor at a perch, none while a pact is kept here (D-S7); a
	 * visitor of another spirit leaves first. GC-02 turns the new one to {@code inviter}.
	 */
	@Nullable PactSpiritEntity<?> welcome(ServerLevel level, ServerPlayer inviter, String counterpart) {
		if (holder.agreement() != null) {
			return null;
		}
		PactSpirits.Spirit spirit = PactSpirits.get(counterpart);
		if (spirit == null) {
			return null;
		}
		PactSpiritEntity<?> guest = visitor(level);
		if (guest != null && guest.getType() == spirit.type()) {
			return guest;
		}
		if (guest != null) {
			guest.leave(level, null, getBlockPos());
		}
		guest = summon(level, spirit, null, inviter);
		visitor = guest == null ? null : guest.getUUID();
		return guest;
	}

	/**
	 * GC-01 (the unit review): {@code counterpart} was chosen but cannot come yet, so a visitor of another spirit still
	 * waiting here (a conversation reopened before its next think) leaves; one of that spirit stays, as welcome keeps it.
	 */
	void dismiss(ServerLevel level, String counterpart) {
		PactSpirits.Spirit spirit = PactSpirits.get(counterpart);
		PactSpiritEntity<?> guest = visitor(level);
		if (guest != null && (spirit == null || guest.getType() != spirit.type())) {
			guest.leave(level, null, getBlockPos());
			visitor = null;
		}
	}

	/** The pact's presence for {@code who}: the visitor of that spirit, if one waits here, becomes it (D-S7); otherwise that spirit is called. */
	@Override
	public @Nullable WorkerEntity<?> call(ServerLevel level, UUID who, String counterpart) {
		// GC-05 (GC-P46): a pact sealed or seated here arrives through this call while nothing is kept here (a recall comes
		// while one is): it starts unanswered, whatever an earlier pact here was told.
		if (holder.agreement() == null && accord != null) {
			accord = null;
			setChanged();
		}
		PactSpirits.Spirit spirit = PactSpirits.get(counterpart);
		if (spirit == null) {
			return null;
		}
		PactSpiritEntity<?> guest = visitor(level);
		if (guest != null && guest.getType() == spirit.type()) {
			visitor = null;
			guest.setOwner(who);
			return guest;
		}
		if (guest != null) {
			guest.leave(level, null, getBlockPos());
			visitor = null;
		}
		return summon(level, spirit, who, level.getServer().getPlayerList().getPlayer(who));
	}

	/** D-06: a conversation opens here: its spirit attends (PactSpiritEntity.listen); at a copy, nobody (presence()). */
	void heard(ServerLevel level) {
		PactSpiritEntity<?> spirit = presence(level);
		if (spirit != null) {
			spirit.listen();
		}
	}

	/** D-06: its spirit answers a screen's action with its gesture and sound (its own table, PactSpiritEntity.answer), fired here, on the server. */
	void react(ServerLevel level, int action, String reason, boolean done) {
		PactSpiritEntity<?> spirit = presence(level);
		PactSpiritEntity.Clip clip = spirit == null ? null : spirit.answer(action, reason, done);
		if (clip != null) {
			spirit.gesture(clip);
		}
	}

	private @Nullable PactSpiritEntity<?> summon(ServerLevel level, PactSpirits.Spirit spirit, @Nullable UUID who, @Nullable Entity facing) {
		PactSpiritEntity<?> summoned = spirit.type().create(level, EntitySpawnReason.MOB_SUMMONED);
		if (summoned == null) {
			return null;
		}
		BlockPos pos = getBlockPos();
		summoned.setOwner(who);
		summoned.setPerch(pos);
		// GC-06 (GC-P67): its station: on the perch, or, a standing body, beside it on the side away from whoever it comes for.
		Vec3 station = summoned.station(level, facing != null && facing.level() == level ? facing.position() : null);
		// GC-02 (GC-S6): it arrives facing whoever it comes for; south (yaw 0, as before) with nobody in this level to face.
		float yaw = facing != null && facing.level() == level ? PactSpiritEntity.bearing(station, facing.position(), 0.0F) : 0.0F;
		summoned.snapTo(station.x, station.y, station.z, yaw, 0.0F);
		summoned.turnTo(yaw);
		if (!level.addFreshEntity(summoned)) {
			return null;
		}
		// D-06: it arrives on the perch. Everyone in range already tracks it (ChunkMap pairs a new entity at once), so all of
		// them see the whole arrival; one who comes later sees it perched.
		summoned.gesture(summoned.body().arrive());
		return summoned;
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
			PactSpiritEntity<?> guest = visitor(server);
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
		// GC-05: an answer is read only with the pact it answers, and only as long as an accord may be; an unknown one is kept
		// as written (PactAccords reads it as unanswered).
		String read = input.getStringOr("accord", "");
		accord = holder.agreement() != null && !read.isEmpty() && read.length() <= PactAccords.MAX_ACCORD ? read : null;
		visitor = null;
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		Saved.stamp(output, 1);
		holder.save(output);
		// GC-05: optional, and only while a pact is kept here; the stamp stays 1 (an older build reads past it).
		if (accord != null && holder.agreement() != null) {
			output.putString("accord", accord);
		}
	}
}

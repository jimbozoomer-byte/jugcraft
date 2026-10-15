package io.github.jimbozoomer.jugcraft.concordance.encyclopedia;

import io.github.jimbozoomer.jugcraft.concordance.Alchemy;
import io.github.jimbozoomer.jugcraft.concordance.CircleAnchorBlockEntity;
import io.github.jimbozoomer.jugcraft.concordance.ComposeText;
import io.github.jimbozoomer.jugcraft.concordance.ConcordanceCommand;
import io.github.jimbozoomer.jugcraft.concordance.ConcordanceData;
import io.github.jimbozoomer.jugcraft.concordance.ConcordanceProgress;
import io.github.jimbozoomer.jugcraft.concordance.CrucibleBlockEntity;
import io.github.jimbozoomer.jugcraft.concordance.JugcraftConcordance;
import io.github.jimbozoomer.jugcraft.concordance.LeyPylonBlockEntity;
import io.github.jimbozoomer.jugcraft.concordance.alchemy.Assay;
import io.github.jimbozoomer.jugcraft.concordance.alchemy.CrucibleInspection;
import io.github.jimbozoomer.jugcraft.concordance.compose.Text;
import io.github.jimbozoomer.jugcraft.concordance.diagnostic.Diagnostic;
import io.github.jimbozoomer.jugcraft.concordance.resource.Conversion;
import io.github.jimbozoomer.jugcraft.concordance.ritual.RitualDefinition;
import io.github.jimbozoomer.jugcraft.concordance.ritual.RitualMachine;
import io.github.jimbozoomer.jugcraft.concordance.rules.Knowledge;
import io.github.jimbozoomer.jugcraft.concordance.rules.ResearchEngine;
import io.github.jimbozoomer.jugcraft.concordance.rules.ResearchState;
import io.github.jimbozoomer.jugcraft.config.JugcraftConfig;
import io.github.jimbozoomer.jugcraft.guide.encyclopedia.EncyclopediaRequestPayload;
import io.github.jimbozoomer.jugcraft.guide.encyclopedia.PageWriter;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

/**
 * The encyclopedia's Inspect page (Concordance Chunk A, A-04 and A-07): shown when the encyclopedia is opened while
 * looking at a Circle Anchor or a Ley Pylon within {@value ConcordanceCommand#CIRCLE_READ_RANGE} blocks, in a loaded
 * chunk, and an Alembic Crucible within the reader's own block reach, at three levels by research and a carried Assay
 * Glass (Concordance Chunk C, C-01). For an anchor it is the anchor's own report, the same lines chat,
 * {@code /jugcraft concordance circle} and Jade show ({@link CircleAnchorBlockEntity#status(ServerLevel, java.util.function.Consumer)},
 * from its cached report: there is no second validator), then the rituals the player knows there; for a pylon, its
 * charge, how it fills and whose rituals it lends to; for a crucible, what anyone sees (heat and what heats it, volume,
 * tank, formula step, why it waits, the risk line), its slots, and with the glass what the glass would tell
 * ({@link CrucibleInspection}). The position is the client's claim, so it is checked here: the same reach as the command
 * gives a player (a crucible: the player's own reach), and never a chunk that is not loaded. It only reads.
 */
public final class InspectPage {
	public static final String PAGE = "inspect";
	/** How far beyond the reader's own block reach a crucible is read: the allowance the server gives a use, never the anchor's 16 (C-P26). */
	public static final double CRUCIBLE_REACH = 1.0;

	private InspectPage() {
	}

	public static void write(ServerPlayer player, EncyclopediaRequestPayload request, PageWriter out) {
		if (!JugcraftConfig.isFeatureEnabled(JugcraftConcordance.FEATURE) || request.looking().isEmpty()) {
			return;
		}
		BlockPos pos = request.looking().get();
		ServerLevel level = player.level();
		double reach = ConcordanceCommand.CIRCLE_READ_RANGE;
		if (!level.isLoaded(pos) || player.distanceToSqr(Vec3.atCenterOf(pos)) > reach * reach) {
			return;
		}
		BlockEntity entity = level.getBlockEntity(pos);
		if (entity instanceof CircleAnchorBlockEntity anchor) {
			anchor(player, level, pos, anchor, out);
		} else if (entity instanceof LeyPylonBlockEntity pylon) {
			pylon(player, pos, pylon, out);
		} else if (entity instanceof CrucibleBlockEntity crucible && player.isWithinBlockInteractionRange(pos, CRUCIBLE_REACH)) {
			crucible(player, level, pos, crucible, out);
		}
	}

	private static void anchor(ServerPlayer player, ServerLevel level, BlockPos pos, CircleAnchorBlockEntity anchor, PageWriter out) {
		out.heading(Component.translatable("encyclopedia.jugcraft.inspect.at", Component.translatable("block.jugcraft.circle_anchor"),
				pos.getX(), pos.getY(), pos.getZ()));
		anchor.status(level, out::fact);
		out.heading(Component.translatable("encyclopedia.jugcraft.inspect.rituals"));
		Knowledge knowledge = ConcordanceProgress.knowledge(player);
		List<RitualDefinition> rituals = ConcordanceData.rules().ritualsAt(CircleAnchorBlockEntity.ANCHOR);
		int unknown = 0;
		for (RitualDefinition ritual : rituals) {
			if (!ResearchEngine.knowsRitual(knowledge, ritual)) {
				unknown++;
				continue;
			}
			out.fact(CircleAnchorBlockEntity.ritualName(ritual.id()), Component.translatable("encyclopedia.jugcraft.inspect.ritual", ritual.participants(),
					ritual.focus(), ritual.steps(), ritual.steps() * RitualMachine.STEP_TICKS / 20, ritual.ley()), List.of());
		}
		if (unknown == rituals.size()) {
			out.fact(Component.translatable("encyclopedia.jugcraft.inspect.no_rituals"));
		}
		if (unknown > 0) {
			out.fact(Component.translatable("encyclopedia.jugcraft.inspect.rituals_unknown", unknown));
		}
		out.codex("circles/lesser_circle", "jugcraft:circle_lore", ResearchState.ENCOUNTERED);
		out.codex("circles/rituals", "jugcraft:circle_lore", ResearchState.UNDERSTOOD);
	}

	private static void pylon(ServerPlayer player, BlockPos pos, LeyPylonBlockEntity pylon, PageWriter out) {
		out.heading(Component.translatable("encyclopedia.jugcraft.inspect.at", Component.translatable("block.jugcraft.ley_pylon"),
				pos.getX(), pos.getY(), pos.getZ()));
		out.fact(pylon.status());
		Conversion conversion = ConcordanceData.rules().conversions().get(LeyPylonBlockEntity.CONVERSION);
		out.fact(conversion == null ? Component.translatable("encyclopedia.jugcraft.inspect.pylon.fill_energy", LeyPylonBlockEntity.JE_PER_LEY)
				: Component.translatable("encyclopedia.jugcraft.inspect.pylon.fill", conversion.fromAmount(), conversion.toAmount(), LeyPylonBlockEntity.JE_PER_LEY));
		out.fact(Component.translatable(pylon.lends(List.of(player.getUUID())) ? "encyclopedia.jugcraft.inspect.pylon.lends"
				: "encyclopedia.jugcraft.inspect.pylon.owned"));
		out.codex("circles/ley_pylon", "jugcraft:circle_lore", ResearchState.ENCOUNTERED);
	}

	/**
	 * An Alembic Crucible (C-01, C-P3): what anyone sees and its slots; the makeup only with an Assay Glass carried, at the
	 * level the glass would tell this reader (Alchemy.glassLevel). Reads the crucible's own reason; changes nothing.
	 */
	private static void crucible(ServerPlayer player, ServerLevel level, BlockPos pos, CrucibleBlockEntity crucible, PageWriter out) {
		out.heading(Component.translatable("encyclopedia.jugcraft.inspect.at", Component.translatable("block.jugcraft.crucible"),
				pos.getX(), pos.getY(), pos.getZ()));
		int reading = FirstLightRoute.carries(player, stack -> stack.is(JugcraftConcordance.ASSAY_GLASS)) ? Alchemy.glassLevel(player) : Assay.SPOON;
		Diagnostic reason = crucible.waiting();
		// Its mixture is not its formula's own batch when it was there before the formula was set (step 0) or the rules
		// refuse the formula: its own harm is then named too, since a dose of it can still be bottled by hand (Assay.risks).
		boolean apart = !crucible.mixture().isEmpty() && (crucible.step() == 0 || crucible.incompatibility() != null);
		CrucibleInspection.State state = new CrucibleInspection.State(crucible.temperature(), crucible.below(level),
				crucible.held(ConcordanceProgress.now(level)), crucible.mixture(), crucible.tankParts(), CrucibleBlockEntity.WATER_PER_BUCKET,
				crucible.program(), crucible.step(), reason == null ? null : reason.text(), apart);
		List<CrucibleInspection.Section> sections = CrucibleInspection.read(state, reading, Alchemy::harmfulStatus, Alchemy.catalog());
		lines(out, sections.getFirst());
		slots(crucible, out);
		if (reading < Assay.GLASS) {
			out.fact(Component.translatable("encyclopedia.jugcraft.inspect.crucible.glass"));
		}
		for (CrucibleInspection.Section section : sections.subList(1, sections.size())) {
			lines(out, section);
		}
		out.codex("alembic/crucible", "jugcraft:alembic_arts", ResearchState.ENCOUNTERED);
		out.codex("alembic/sampling", "jugcraft:alembic_arts", ResearchState.ENCOUNTERED);
		out.codex("alembic/formulas", "jugcraft:alembic_arts", ResearchState.UNDERSTOOD);
	}

	private static void lines(PageWriter out, CrucibleInspection.Section section) {
		if (section.heading() != null) {
			out.heading(ComposeText.show(section.heading()));
		}
		for (Text line : section.lines()) {
			out.fact(ComposeText.show(line));
		}
	}

	/** What waits in its buffer, its bottles or bowls, and what it has made: counts and icons, as anyone could look in. */
	private static void slots(CrucibleBlockEntity crucible, PageWriter out) {
		List<ItemStack> buffer = new ArrayList<>();
		for (int slot = 0; slot < CrucibleBlockEntity.BUFFER_SLOTS; slot++) {
			buffer.add(crucible.getItem(slot));
		}
		slot(out, "encyclopedia.jugcraft.inspect.crucible.buffer", buffer);
		slot(out, "encyclopedia.jugcraft.inspect.crucible.bottles", List.of(crucible.getItem(CrucibleBlockEntity.BOTTLE_SLOT)));
		slot(out, "encyclopedia.jugcraft.inspect.crucible.output", List.of(crucible.getItem(CrucibleBlockEntity.OUTPUT_SLOT)));
	}

	private static void slot(PageWriter out, String key, List<ItemStack> stacks) {
		MutableComponent held = Component.empty();
		List<Item> icons = new ArrayList<>();
		for (ItemStack stack : stacks) {
			if (stack.isEmpty()) {
				continue;
			}
			if (!icons.isEmpty()) {
				held.append(", ");
			}
			held.append(Component.translatable("encyclopedia.jugcraft.inspect.crucible.count", stack.getCount(), stack.getHoverName()));
			icons.add(stack.getItem());
		}
		out.fact(Component.translatable(key, icons.isEmpty() ? Component.translatable("encyclopedia.jugcraft.inspect.crucible.none") : held), null, icons);
	}
}

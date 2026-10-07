package io.github.jimbozoomer.jugcraft.concordance.stages;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.serialization.Codec;
import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.concordance.ConcordanceData;
import io.github.jimbozoomer.jugcraft.concordance.ConcordanceProgress;
import io.github.jimbozoomer.jugcraft.concordance.JugcraftConcordance;
import io.github.jimbozoomer.jugcraft.concordance.conclave.Conclave;
import io.github.jimbozoomer.jugcraft.concordance.conclave.Standing;
import io.github.jimbozoomer.jugcraft.concordance.progression.ProgressionCatalog;
import io.github.jimbozoomer.jugcraft.concordance.progression.ProgressionGraph;
import io.github.jimbozoomer.jugcraft.concordance.progression.Route;
import io.github.jimbozoomer.jugcraft.concordance.progression.StageDefinition;
import io.github.jimbozoomer.jugcraft.concordance.progression.Stages;
import io.github.jimbozoomer.jugcraft.concordance.rules.Definitions;
import io.github.jimbozoomer.jugcraft.concordance.rules.ResearchState;
import io.github.jimbozoomer.jugcraft.concordance.starbound.Starbound;
import io.github.jimbozoomer.jugcraft.config.JugcraftConfig;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.jspecify.annotations.Nullable;

/**
 * A player's stage on the server (roadmap step 24, docs/features/arcane-concordance-progression.md): read from what they
 * know (research), their Conclave rank and projects, and the milestones features record, by the stage definitions'
 * routes ({@link Stages}). A stage once reached is kept ({@link #STAGE}, through death) and announced with its
 * advancement. {@code /jugcraft concordance stage} shows a player's stage and every route to the next with what it still
 * needs; {@code /jugcraft concordance progression} (operators) shows what the progression graph finds wrong, if anything.
 */
public final class StageProgress {
	/** The furthest stage a player has reached (kept through death). */
	public static AttachmentType<String> STAGE;
	/** Milestones features record for a player (roadmap step 25's endgame project, for one), read by stage routes. */
	public static AttachmentType<List<String>> MILESTONES;

	private StageProgress() {
	}

	public static void register() {
		STAGE = AttachmentRegistry.<String>builder().persistent(Codec.STRING).copyOnDeath().buildAndRegister(Jugcraft.id("concordance_stage"));
		MILESTONES = AttachmentRegistry.<List<String>>builder().persistent(Codec.STRING.listOf()).copyOnDeath()
				.buildAndRegister(Jugcraft.id("concordance_milestones"));
		ConcordanceProgress.listen((player, evidence, result) -> {
			if (!result.transitions().isEmpty()) {
				refresh(player);
			}
		});
		Starbound.listen(StageProgress::refresh);
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> refresh(handler.getPlayer()));
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> command(dispatcher));
	}

	public static boolean enabled() {
		return JugcraftConfig.isFeatureEnabled(JugcraftConcordance.FEATURE);
	}

	public static ProgressionCatalog catalog() {
		return ConcordanceData.rules().progression();
	}

	/** What {@code player} has, for the stage rules. */
	public static Stages.Situation situation(ServerPlayer player) {
		Map<String, ResearchState> research = new HashMap<>();
		Map<String, String> traditions = new HashMap<>();
		for (Definitions.Research entry : ConcordanceData.rules().research().values()) {
			research.put(entry.id(), ConcordanceProgress.knowledge(player).state(entry.id()));
			traditions.put(entry.id(), entry.tradition());
		}
		Standing standing = Starbound.standing(player);
		int projects = 0;
		for (String key : standing.awarded().keySet()) {
			if (key.startsWith("project:") && key.endsWith(":complete")) {
				projects++;
			}
		}
		List<String> milestones = player.getAttached(MILESTONES);
		return new Stages.Situation(research, traditions, standing.member() ? Conclave.rank(standing) : null, projects,
				milestones == null ? Set.of() : new HashSet<>(milestones));
	}

	/** Records a milestone for {@code player} (a feature calls this) and looks at their stage again. */
	public static void milestone(ServerPlayer player, String milestone) {
		List<String> held = player.getAttached(MILESTONES);
		if (held == null || !held.contains(milestone)) {
			List<String> next = new java.util.ArrayList<>(held == null ? List.of() : held);
			next.add(milestone);
			player.setAttached(MILESTONES, List.copyOf(next));
		}
		refresh(player);
	}

	/** The furthest stage {@code player} has reached, or null. */
	public static @Nullable StageDefinition stage(ServerPlayer player) {
		String id = player.getAttached(STAGE);
		return id == null ? null : catalog().stages().get(id);
	}

	/**
	 * Looks at {@code player}'s stage again: a stage reached for the first time is kept, its advancement awarded (and every
	 * stage's before it) and announced. A stage is never lost. Tests call this.
	 */
	public static void refresh(ServerPlayer player) {
		if (!enabled()) {
			return;
		}
		StageDefinition reached = Stages.reached(catalog().stages().values(), situation(player));
		StageDefinition held = stage(player);
		if (reached == null || held != null && held.order() >= reached.order()) {
			return;
		}
		player.setAttached(STAGE, reached.id());
		for (StageDefinition stage : catalog().ordered()) {
			if (stage.order() <= reached.order() && (held == null || stage.order() > held.order())) {
				ConcordanceProgress.award(player, Jugcraft.id("concordance_stage_" + stage.id()));
			}
		}
		player.sendSystemMessage(Component.translatable("message.jugcraft.concordance.stage.reached", name(reached.id())));
	}

	public static Component name(String stage) {
		return Component.translatable("compose.jugcraft.stage." + stage);
	}

	/** {@code player}'s stage and, for the next one, every route and what it still needs. */
	public static void describe(ServerPlayer player) {
		StageDefinition held = stage(player);
		if (held == null) {
			player.sendSystemMessage(Component.translatable("message.jugcraft.concordance.stage.none"));
		} else {
			player.sendSystemMessage(Component.translatable("message.jugcraft.concordance.stage.current", name(held.id())));
		}
		StageDefinition next = null;
		for (StageDefinition stage : catalog().ordered()) {
			if (held == null || stage.order() > held.order()) {
				next = stage;
				break;
			}
		}
		if (next == null) {
			player.sendSystemMessage(Component.translatable("message.jugcraft.concordance.stage.last"));
			return;
		}
		Stages.Situation situation = situation(player);
		player.sendSystemMessage(Component.translatable("message.jugcraft.concordance.stage.next", name(next.id())));
		for (Route route : next.routes()) {
			List<String> missing = Stages.missing(route, situation);
			Component text = Component.translatable("compose.jugcraft.stage." + next.id() + "." + route.id());
			player.sendSystemMessage(missing.isEmpty() ? Component.translatable("message.jugcraft.concordance.stage.route_met", text)
					: Component.translatable("message.jugcraft.concordance.stage.route", text, String.join(", ", missing)));
		}
	}

	private static void command(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("jugcraft").then(Commands.literal("concordance")
				.then(Commands.literal("stage").executes(context -> {
					ServerPlayer player = context.getSource().getPlayerOrException();
					refresh(player);
					describe(player);
					return 1;
				}))
				.then(Commands.literal("progression").requires(source -> Commands.LEVEL_GAMEMASTERS.check(source.permissions())).executes(context -> {
					ProgressionGraph graph = catalog().graph();
					List<String> problems = graph == null ? List.of("no graph") : graph.problems();
					if (problems.isEmpty()) {
						context.getSource().sendSuccess(() -> Component.translatable("message.jugcraft.concordance.progression.whole",
								graph.nodes().size()), false);
					}
					for (String problem : problems) {
						context.getSource().sendSuccess(() -> Component.translatable("message.jugcraft.concordance.progression.problem", problem), false);
					}
					return problems.size();
				}))));
	}
}

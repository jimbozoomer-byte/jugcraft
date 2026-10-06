package io.github.jimbozoomer.jugcraft.concordance;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.jimbozoomer.jugcraft.concordance.rules.ConcordanceRules;
import io.github.jimbozoomer.jugcraft.concordance.rules.FocusPool;
import io.github.jimbozoomer.jugcraft.concordance.rules.Knowledge;
import io.github.jimbozoomer.jugcraft.concordance.rules.ResearchEngine;
import io.github.jimbozoomer.jugcraft.concordance.rules.ResearchState;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * {@code /jugcraft concordance}: anyone can see their own research ({@code status}). Operators (permission level 2)
 * can see anyone's, list the problems found in the loaded rules ({@code diagnose}), and for testing and support set a
 * research state ({@code grant}), forget a player's research ({@code reset}) or set their Focus ({@code focus}).
 */
public final class ConcordanceCommand {
	private ConcordanceCommand() {
	}

	static void register() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> register(dispatcher));
	}

	private static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("jugcraft").then(Commands.literal("concordance")
				.then(Commands.literal("status")
						.executes(context -> status(context.getSource(), context.getSource().getPlayerOrException()))
						.then(Commands.argument("player", EntityArgument.player()).requires(ConcordanceCommand::isOperator)
								.executes(context -> status(context.getSource(), EntityArgument.getPlayer(context, "player")))))
				.then(Commands.literal("diagnose").requires(ConcordanceCommand::isOperator)
						.executes(context -> diagnose(context.getSource())))
				.then(Commands.literal("grant").requires(ConcordanceCommand::isOperator)
						.then(Commands.argument("player", EntityArgument.player())
								.then(Commands.argument("research", IdentifierArgument.id())
										.suggests((context, builder) -> {
											ConcordanceData.rules().research().keySet().forEach(builder::suggest);
											return builder.buildFuture();
										})
										.then(Commands.argument("state", StringArgumentType.word())
												.suggests((context, builder) -> {
													for (ResearchState state : ResearchState.values()) {
														builder.suggest(state.id());
													}
													return builder.buildFuture();
												})
												.executes(ConcordanceCommand::grant)))))
				.then(Commands.literal("reset").requires(ConcordanceCommand::isOperator)
						.then(Commands.argument("player", EntityArgument.player())
								.executes(context -> reset(context.getSource(), EntityArgument.getPlayer(context, "player")))))
				.then(Commands.literal("focus").requires(ConcordanceCommand::isOperator)
						.then(Commands.argument("player", EntityArgument.player())
								.then(Commands.argument("amount", IntegerArgumentType.integer(0, FocusPool.MAX))
										.executes(context -> focus(context.getSource(), EntityArgument.getPlayer(context, "player"),
												IntegerArgumentType.getInteger(context, "amount"))))))));
	}

	private static boolean isOperator(CommandSourceStack source) {
		return Commands.LEVEL_GAMEMASTERS.check(source.permissions());
	}

	private static int status(CommandSourceStack source, ServerPlayer player) {
		ConcordanceRules rules = ConcordanceData.rules();
		Knowledge knowledge = ConcordanceProgress.knowledge(player);
		source.sendSuccess(() -> Component.translatable("message.jugcraft.concordance.focus",
				ConcordanceProgress.currentFocus(player), FocusPool.MAX), false);
		for (String research : rules.research().keySet()) {
			StringBuilder line = new StringBuilder(research).append(": ").append(knowledge.state(research).id());
			for (ResearchEngine.Explanation explanation : ResearchEngine.explain(knowledge, rules, research, ConcordanceProgress.TAG_LOOKUP)) {
				line.append(" | ").append(describe(explanation));
			}
			String text = line.toString();
			source.sendSuccess(() -> Component.literal(text), false);
		}
		String invocations = knowledge.invocations().isEmpty() ? "none" : knowledge.invocations().toString();
		source.sendSuccess(() -> Component.literal("Invocations (spell=Focus): " + invocations), false);
		return knowledge.entries().size();
	}

	private static String describe(ResearchEngine.Explanation explanation) {
		return switch (explanation) {
			case ResearchEngine.Explanation.Unknown unknown -> "not defined by the loaded data";
			case ResearchEngine.Explanation.NeedsPrerequisite needs -> "needs " + needs.requirement().research() + " "
					+ needs.requirement().state().id() + " (has " + needs.has().id() + ")";
			case ResearchEngine.Explanation.Complete complete -> "complete";
			case ResearchEngine.Explanation.Next next -> {
				StringBuilder text = new StringBuilder("next ").append(next.state().id()).append(":");
				for (ResearchEngine.RuleProgress rule : next.alternatives()) {
					text.append(' ').append(rule.rule().kind().id).append(' ').append(rule.have()).append('/').append(rule.need());
				}
				yield text.toString();
			}
		};
	}

	private static int diagnose(CommandSourceStack source) {
		ConcordanceRules rules = ConcordanceData.rules();
		source.sendSuccess(() -> Component.literal(String.format(Locale.ROOT,
				"Concordance rules: %d research, %d invocations, %d workings, %d conversions; entry points %s",
				rules.research().size(), rules.invocations().size(), rules.workings().size(),
				rules.conversions().conversions().size(), rules.entryPoints())), false);
		List<String> problems = rules.problems();
		if (problems.isEmpty()) {
			source.sendSuccess(() -> Component.literal("No problems"), false);
		}
		for (String problem : problems) {
			source.sendFailure(Component.literal(problem));
		}
		return problems.size();
	}

	private static int grant(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		ServerPlayer player = EntityArgument.getPlayer(context, "player");
		String research = IdentifierArgument.getId(context, "research").toString();
		ResearchState state = ResearchState.fromId(StringArgumentType.getString(context, "state").toLowerCase(Locale.ROOT));
		if (ConcordanceData.rules().research(research) == null || state == null) {
			context.getSource().sendFailure(Component.literal("Unknown research or state"));
			return 0;
		}
		ConcordanceProgress.grant(player, research, state);
		Map<String, Integer> invocations = ConcordanceProgress.knowledge(player).invocations();
		context.getSource().sendSuccess(() -> Component.literal(player.getName().getString() + ": " + research + " set to "
				+ state.id() + "; invocations " + invocations), true);
		return 1;
	}

	private static int reset(CommandSourceStack source, ServerPlayer player) {
		ConcordanceProgress.reset(player);
		source.sendSuccess(() -> Component.literal(player.getName().getString() + ": Concordance research forgotten"), true);
		return 1;
	}

	private static int focus(CommandSourceStack source, ServerPlayer player, int amount) {
		ConcordanceProgress.setFocus(player, amount);
		source.sendSuccess(() -> Component.literal(player.getName().getString() + ": Focus set to " + amount), true);
		return amount;
	}
}

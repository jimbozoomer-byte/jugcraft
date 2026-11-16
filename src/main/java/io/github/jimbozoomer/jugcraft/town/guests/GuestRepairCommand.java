package io.github.jimbozoomer.jugcraft.town.guests;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.town.Townsfolk;
import java.util.Locale;
import java.util.StringJoiner;
import java.util.TreeMap;
import java.util.UUID;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Operator repair under {@code /guests} (permission level 2, {@link Commands#LEVEL_GAMEMASTERS}, checked on every node
 * and again in every command before it reads or changes anything): the ledger's state for a guest or player, a forced
 * divorce, clearing a cooldown, setting affection, rebinding a lost guest. Every repair is explicit and logged. Worker
 * C's {@link GuestPlacement} adds {@code /guests place} and {@code /guests roster} to the same root; Brigadier merges
 * the two registrations.
 *
 * <pre>
 * /guests state guest &lt;guest&gt;                     /guests state player &lt;player&gt;
 * /guests repair divorce &lt;guest&gt; [true|false]       (false: no cooldowns; the default starts both)
 * /guests repair cooldown clear player &lt;player&gt;     /guests repair cooldown clear guest &lt;guest&gt;
 * /guests repair affection &lt;player&gt; &lt;guest&gt; &lt;0-1000&gt;
 * /guests repair rebind &lt;guest&gt; [force]            (the guest's NPC you look at, else the nearest within 8 blocks)
 * </pre>
 *
 * <p>A player is an online player's name or any player's UUID (offline players by UUID). A rebind is allowed only while
 * the guest is unbound or its canonical entity is lost; {@code force} replaces a canonical entity that is not lost,
 * which is kept as a tombstone, never accepted again, and leaves as a duplicate when it next loads.
 */
public final class GuestRepairCommand {
	/** How far a rebind looks for the guest's NPC: along the operator's view, else around them. */
	static final double REACH = 8.0;

	private GuestRepairCommand() {
	}

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("guests")
				.then(Commands.literal("state").requires(GuestRepairCommand::allowed)
						.then(Commands.literal("guest").then(guestArgument()
								.executes(c -> guestState(c.getSource(), StringArgumentType.getString(c, "guest")))))
						.then(Commands.literal("player").then(playerArgument()
								.executes(c -> playerState(c.getSource(), StringArgumentType.getString(c, "player"))))))
				.then(Commands.literal("repair").requires(GuestRepairCommand::allowed)
						.then(Commands.literal("divorce").then(guestArgument()
								.executes(c -> divorce(c.getSource(), StringArgumentType.getString(c, "guest"), true))
								.then(Commands.argument("cooldowns", BoolArgumentType.bool())
										.executes(c -> divorce(c.getSource(), StringArgumentType.getString(c, "guest"),
												BoolArgumentType.getBool(c, "cooldowns"))))))
						.then(Commands.literal("cooldown").then(Commands.literal("clear")
								.then(Commands.literal("player").then(playerArgument()
										.executes(c -> clearPlayerCooldown(c.getSource(), StringArgumentType.getString(c, "player")))))
								.then(Commands.literal("guest").then(guestArgument()
										.executes(c -> clearGuestCooldown(c.getSource(), StringArgumentType.getString(c, "guest")))))))
						.then(Commands.literal("affection").then(playerArgument().then(guestArgument()
								.then(Commands.argument("value", IntegerArgumentType.integer(0, GuestBalance.MAX_AFFECTION))
										.executes(c -> affection(c.getSource(), StringArgumentType.getString(c, "player"),
												StringArgumentType.getString(c, "guest"), IntegerArgumentType.getInteger(c, "value")))))))
						.then(Commands.literal("rebind").then(guestArgument()
								.executes(c -> rebind(c.getSource(), StringArgumentType.getString(c, "guest"), false))
								.then(Commands.literal("force")
										.executes(c -> rebind(c.getSource(), StringArgumentType.getString(c, "guest"), true)))))));
	}

	private static RequiredArgumentBuilder<CommandSourceStack, String> guestArgument() {
		return Commands.argument("guest", StringArgumentType.word()).suggests((context, builder) -> {
			if (allowed(context.getSource())) {
				String typed = builder.getRemaining().toLowerCase(Locale.ROOT);
				for (GuestRoster.Guest guest : GuestRoster.ALL) {
					if (guest.id().startsWith(typed)) {
						builder.suggest(guest.id());
					}
				}
			}
			return builder.buildFuture();
		});
	}

	private static RequiredArgumentBuilder<CommandSourceStack, String> playerArgument() {
		return Commands.argument("player", StringArgumentType.word()).suggests((context, builder) -> {
			if (allowed(context.getSource())) {
				String typed = builder.getRemaining().toLowerCase(Locale.ROOT);
				for (ServerPlayer player : context.getSource().getServer().getPlayerList().getPlayers()) {
					String name = player.getName().getString();
					if (name.toLowerCase(java.util.Locale.ROOT).startsWith(typed)) {
						builder.suggest(name);
					}
				}
			}
			return builder.buildFuture();
		});
	}

	/** Operators only: permission level 2, as {@code CustomNpcs.isOperator}. Quiet, for the command tree. */
	private static boolean allowed(CommandSourceStack source) {
		return Commands.LEVEL_GAMEMASTERS.check(source.permissions());
	}

	/** The check again, inside each command; a refused source is told why. */
	private static boolean operator(CommandSourceStack source) {
		if (allowed(source)) {
			return true;
		}
		source.sendFailure(Component.translatableWithFallback("message.jugcraft.guest.repair.operator",
				"Only operators can inspect or repair the guest ledger."));
		return false;
	}

	// ---------------------------------------------------------------- the commands (package-private for the tests)

	static int guestState(CommandSourceStack source, String guest) {
		if (!operator(source)) {
			return 0;
		}
		GuestRoster.Guest entry = guest(source, guest);
		if (entry == null) {
			return 0;
		}
		MinecraftServer server = source.getServer();
		GuestLedger ledger = GuestLedger.get(server);
		UUID canonical = ledger.canonical(guest);
		String identity = canonical == null ? "unbound" : canonical + (ledger.missing(guest) ? " (missing)" : "");
		GuestLedger.Place seen = ledger.lastSeen(guest);
		String where = seen == null ? "never" : seen.pos().toShortString() + " in " + seen.dimension().identifier();
		UUID spouse = ledger.spouseOf(guest);
		long cooldown = ledger.cooldownMillis(guest);
		String status = ledger.unreadable(guest) ? "its saved record cannot be read: frozen" : entry.pending() ? "pending"
				: entry.mayMarry() ? "available, may marry" : "available, companionship only";
		ok(source, "state.guest", "%s (%s, %s): entity %s, last seen %s; spouse %s; cooldown %s; %s replaced entities kept; affection held by %s players.",
				entry.name(), guest, status, identity, where, spouse == null ? "none" : label(server, spouse),
				cooldown <= 0 ? "none" : GuestMarriage.duration(cooldown), ledger.replaced(guest).size(), ledger.admirers(guest));
		return 1;
	}

	static int playerState(CommandSourceStack source, String player) {
		if (!operator(source)) {
			return 0;
		}
		UUID id = player(source, player);
		if (id == null) {
			return 0;
		}
		MinecraftServer server = source.getServer();
		GuestLedger ledger = GuestLedger.get(server);
		String spouse = ledger.spouseOf(id);
		long cooldown = ledger.cooldownMillis(id);
		StringJoiner affection = new StringJoiner(", ");
		new TreeMap<>(ledger.affections(id)).forEach((guest, value) -> affection.add(guest + " " + value));
		String proposal = GuestMarriage.proposalPending(server, id);
		String divorce = GuestMarriage.divorcePending(server, id);
		ok(source, "state.player", "%s%s: spouse %s; cooldown %s; affection: %s; pending: %s.", label(server, id),
				ledger.damaged(id) ? " (damaged saved data concerns them: refused marriage until repaired)" : "",
				spouse == null ? "none" : spouse, cooldown <= 0 ? "none" : GuestMarriage.duration(cooldown),
				affection.length() == 0 ? "none" : affection.toString(),
				proposal != null ? "proposal to " + proposal : divorce != null ? "divorce from " + divorce : "nothing");
		return 1;
	}

	static int divorce(CommandSourceStack source, String guest, boolean cooldowns) {
		if (!operator(source) || guest(source, guest) == null) {
			return 0;
		}
		MinecraftServer server = source.getServer();
		UUID player = GuestLedger.get(server).forceDivorce(guest, cooldowns);
		if (player == null) {
			return fail(source, "not_married", "%s is not married.", guest);
		}
		Jugcraft.LOGGER.info("[guests] Repair by {}: forced divorce of {} and player {}, cooldowns {}", source.getTextName(), guest, player,
				cooldowns ? "started" : "not started");
		ok(source, "divorce", "%s and %s are divorced (%s).", guest, label(server, player),
				cooldowns ? "both may marry again in " + GuestMarriage.duration(GuestBalance.REMARRIAGE_COOLDOWN_MILLIS) : "no cooldowns");
		return 1;
	}

	static int clearPlayerCooldown(CommandSourceStack source, String player) {
		if (!operator(source)) {
			return 0;
		}
		UUID id = player(source, player);
		if (id == null) {
			return 0;
		}
		MinecraftServer server = source.getServer();
		if (!GuestLedger.get(server).clearCooldown(id)) {
			return fail(source, "no_cooldown", "%s has no cooldown.", label(server, id));
		}
		Jugcraft.LOGGER.info("[guests] Repair by {}: cleared player {}'s remarriage cooldown", source.getTextName(), id);
		ok(source, "cooldown_cleared", "%s's cooldown is cleared.", label(server, id));
		return 1;
	}

	static int clearGuestCooldown(CommandSourceStack source, String guest) {
		if (!operator(source) || guest(source, guest) == null) {
			return 0;
		}
		if (!GuestLedger.get(source.getServer()).clearCooldown(guest)) {
			return fail(source, "no_cooldown", "%s has no cooldown.", guest);
		}
		Jugcraft.LOGGER.info("[guests] Repair by {}: cleared {}'s remarriage cooldown", source.getTextName(), guest);
		ok(source, "cooldown_cleared", "%s's cooldown is cleared.", guest);
		return 1;
	}

	static int affection(CommandSourceStack source, String player, String guest, int value) {
		if (!operator(source)) {
			return 0;
		}
		UUID id = player(source, player);
		GuestRoster.Guest entry = id == null ? null : guest(source, guest);
		if (entry == null) {
			return 0;
		}
		if (!entry.available()) {
			return fail(source, "pending", "%s is pending: nobody can befriend them yet.", guest);
		}
		MinecraftServer server = source.getServer();
		GuestLedger ledger = GuestLedger.get(server);
		if (ledger.unreadable(id)) {
			return fail(source, "unreadable_player", "%s's saved record cannot be read; it is kept as written. Repair the town_guests file first.",
					label(server, id));
		}
		int before = ledger.affection(id, guest);
		int after = ledger.setAffection(id, guest, value);
		Jugcraft.LOGGER.info("[guests] Repair by {}: player {}'s affection for {} set from {} to {}", source.getTextName(), id, guest, before, after);
		ok(source, "affection", "%s's affection for %s is now %s (was %s).", label(server, id), guest, after, before);
		return 1;
	}

	static int rebind(CommandSourceStack source, String guest, boolean force) {
		if (!operator(source)) {
			return 0;
		}
		GuestRoster.Guest entry = guest(source, guest);
		if (entry == null) {
			return 0;
		}
		if (!entry.available()) {
			return fail(source, "pending", "%s is pending: nobody can befriend them yet.", guest);
		}
		GuestLedger ledger = GuestLedger.get(source.getServer());
		UUID old = ledger.canonical(guest);
		boolean lost = ledger.missing(guest);
		if (old != null && !lost && !force) {
			return fail(source, "rebind.not_lost",
					"%s's entity %s is not lost. Add force to replace it anyway (it then leaves as a duplicate when it next loads).", guest, old);
		}
		Townsfolk npc = target(source, guest, old);
		if (npc == null) {
			return fail(source, "rebind.no_target",
					"No NPC carrying the guest %s in sight or within %s blocks (other than its canonical entity).", guest, (int) REACH);
		}
		boolean forced = old != null && !lost;
		boolean bound = forced ? ledger.forceBind(guest, npc.getUUID()) : ledger.bind(guest, npc.getUUID());
		if (!bound) {
			return fail(source, "rebind.refused",
					"That NPC cannot become %s: it was replaced before, is another guest's, or too many replaced entities may still exist.", guest);
		}
		ledger.seen(guest, npc.getUUID(), npc.level().dimension(), npc.blockPosition());
		Jugcraft.LOGGER.info("[guests] Repair by {}: {} rebound to entity {}{}", source.getTextName(), guest, npc.getUUID(),
				old == null ? " (it was unbound)" : " in place of " + old + (forced ? " (forced; the old one is a tombstone)" : " (lost)"));
		ok(source, "rebind", "%s is now the NPC at %s.", guest, npc.blockPosition().toShortString());
		return 1;
	}

	// ---------------------------------------------------------------- helpers

	/** The NPC carrying {@code guest} (other than its canonical entity) that the source looks at, else the nearest within reach. */
	static @Nullable Townsfolk target(CommandSourceStack source, String guest, @Nullable UUID canonical) {
		Entity viewer = source.getEntity();
		if (viewer != null) {
			Vec3 eye = viewer.getEyePosition();
			Vec3 end = eye.add(viewer.getViewVector(1.0F).scale(REACH));
			Townsfolk best = null;
			double bestDistance = Double.MAX_VALUE;
			for (Townsfolk npc : viewer.level().getEntitiesOfClass(Townsfolk.class, new AABB(eye, end).inflate(1.0),
					npc -> fits(npc, guest, canonical))) {
				var hit = npc.getBoundingBox().inflate(0.3).clip(eye, end);
				if (hit.isPresent() && viewer instanceof LivingEntity living && living.hasLineOfSight(npc)) {
					double distance = hit.get().distanceToSqr(eye);
					if (distance < bestDistance) {
						bestDistance = distance;
						best = npc;
					}
				}
			}
			if (best != null) {
				return best;
			}
		}
		ServerLevel level = source.getLevel();
		Vec3 at = source.getPosition();
		Townsfolk nearest = null;
		double best = REACH * REACH;
		for (Townsfolk npc : level.getEntitiesOfClass(Townsfolk.class, AABB.ofSize(at, 2 * REACH, 2 * REACH, 2 * REACH),
				npc -> fits(npc, guest, canonical))) {
			double distance = npc.position().distanceToSqr(at);
			if (distance <= best) {
				best = distance;
				nearest = npc;
			}
		}
		return nearest;
	}

	private static boolean fits(Townsfolk npc, String guest, @Nullable UUID canonical) {
		return guest.equals(npc.guest()) && npc.isAlive() && !npc.isRemoved() && !npc.getUUID().equals(canonical);
	}

	/** The roster entry, or null (and the source told). */
	private static GuestRoster.@Nullable Guest guest(CommandSourceStack source, String guest) {
		GuestRoster.Guest entry = GuestRoster.get(guest);
		if (entry == null) {
			fail(source, "unknown_guest", "There is no guest called %s.", guest);
		}
		return entry;
	}

	/** An online player's name, or any player's UUID; null (and the source told) when it is neither. */
	private static @Nullable UUID player(CommandSourceStack source, String text) {
		try {
			return UUID.fromString(text);
		} catch (IllegalArgumentException notUuid) {
			// a name, then
		}
		for (ServerPlayer online : source.getServer().getPlayerList().getPlayers()) {
			if (online.getName().getString().equalsIgnoreCase(text)) {
				return online.getUUID();
			}
		}
		fail(source, "unknown_player", "No online player is called %s; give an offline player's UUID.", text);
		return null;
	}

	/** An online player's name with their UUID, or the UUID alone. */
	private static String label(MinecraftServer server, UUID player) {
		ServerPlayer online = server.getPlayerList().getPlayer(player);
		return online == null ? player.toString() : online.getName().getString() + " (" + player + ")";
	}

	private static void ok(CommandSourceStack source, String key, String english, Object... args) {
		Component message = Component.translatableWithFallback("message.jugcraft.guest.repair." + key, english, args);
		source.sendSuccess(() -> message, true);
	}

	private static int fail(CommandSourceStack source, String key, String english, Object... args) {
		source.sendFailure(Component.translatableWithFallback("message.jugcraft.guest.repair." + key, english, args));
		return 0;
	}
}

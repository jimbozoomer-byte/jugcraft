package io.github.jimbozoomer.jugcraft.town.guests;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import io.github.jimbozoomer.jugcraft.finance.LandRegistry;
import io.github.jimbozoomer.jugcraft.town.CustomNpcs;
import io.github.jimbozoomer.jugcraft.town.JugcraftTown;
import io.github.jimbozoomer.jugcraft.town.TownProtection;
import io.github.jimbozoomer.jugcraft.town.Townsfolk;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Placing town guests (worker C): {@code /guests place <id> [pos] [yaw]} (yaw in whole degrees, -180 to 180) and
 * {@code /guests roster}, operators only (permission level 2, checked by the command tree and again here on the server). Worker A's
 * {@link GuestRepairCommand} adds {@code /guests ledger} and {@code /guests repair} to the same root; Brigadier merges
 * the two trees.
 *
 * <p>A guest is placed as a custom NPC ({@code CustomNpcs.create(.., guest)}) and then bound as the guest's canonical
 * entity in the {@link GuestLedger}. Placing never changes, moves or replaces a block and never loads a chunk. It is
 * refused, with nothing changed, when:
 * <ul>
 * <li>the source is not an operator, or the id is not in the roster or is still pending;</li>
 * <li>the guest already has a canonical entity in the ledger, loaded or not, unless the ledger records it as lost
 * ({@link GuestLedger#missing}): one entity per identity;</li>
 * <li>any block the body would stand in is outside the world or its border, or in a chunk that is not loaded;</li>
 * <li>any of those blocks is in a land claim the placing operator may not build in ({@link LandRegistry#mayBuildFor},
 * without the admins' bypass: a player's claim is theirs, and the console may place in no claim at all), or is
 * protected by the town or an operator region ({@link TownProtection}): only an operator in creative mode, whom the
 * town already lets change it ({@link TownProtection#exempt}), may place a guest there; the console and command blocks
 * may not;</li>
 * <li>anything other than a player or an ordinary mob is in the spot (the blocks the body stands in, so an item frame
 * on their wall counts): item frames, armor stands, paintings, displays, minecarts, boats, dropped items and other
 * NPCs are owners' things and are never pushed aside;</li>
 * <li>the body would intersect a block (or anything else that collides).</li>
 * </ul>
 * If the ledger refuses the binding, the new entity is removed again and the operator is told. A placed guest wears its
 * placeholder ({@link GuestAppearances}), labelled as such.
 *
 * <p>Placing guests at free spots of the spawn town automatically is not offered: an operator stands where the guest
 * should be (or names the block) and places it there.
 */
public final class GuestPlacement {
	/** Keeps the body's far faces out of the next block (a body exactly one block tall stands in one block). */
	private static final double EPSILON = 1.0E-4;

	/** Why a guest was not placed. */
	enum Refusal {
		NOT_OPERATOR, UNKNOWN, PENDING, PLACED, OUTSIDE, UNLOADED, CLAIMED, PROTECTED, OCCUPIED, BLOCKED, FAILED, BIND_REFUSED
	}

	/** What a placement did: the new guest (null when refused), why not, and what the operator is told. */
	record Outcome(@Nullable Townsfolk npc, @Nullable Refusal refusal, Component message) {
		boolean placed() {
			return npc != null;
		}
	}

	/** Where a guest is, as far as the ledger knows. */
	enum Status {
		UNPLACED, PLACED, MISSING
	}

	private GuestPlacement() {
	}

	/** Adds {@code place} and {@code roster} under {@code /guests}. Called once from {@link GuestService}. */
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("guests")
				.then(Commands.literal("place").requires(CustomNpcs::isOperator)
						.then(guestId()
								.executes(c -> place(c.getSource(), StringArgumentType.getString(c, "id"), c.getSource().getPosition(),
										c.getSource().getRotation().y))
								.then(Commands.argument("pos", BlockPosArgument.blockPos())
										.executes(c -> place(c.getSource(), StringArgumentType.getString(c, "id"),
												Vec3.atBottomCenterOf(BlockPosArgument.getLoadedBlockPos(c, "pos")), c.getSource().getRotation().y))
										.then(Commands.argument("yaw", IntegerArgumentType.integer(-180, 180))
												.executes(c -> place(c.getSource(), StringArgumentType.getString(c, "id"),
														Vec3.atBottomCenterOf(BlockPosArgument.getLoadedBlockPos(c, "pos")),
														(float) IntegerArgumentType.getInteger(c, "yaw")))))))
				.then(Commands.literal("roster").requires(CustomNpcs::isOperator).executes(c -> roster(c.getSource()))));
	}

	private static RequiredArgumentBuilder<CommandSourceStack, String> guestId() {
		return Commands.argument("id", StringArgumentType.word()).suggests((context, builder) -> {
			if (CustomNpcs.isOperator(context.getSource())) {
				for (GuestRoster.Guest guest : GuestRoster.ALL) {
					if (guest.available()) {
						builder.suggest(guest.id());
					}
				}
			}
			return builder.buildFuture();
		});
	}

	// ---------------------------------------------------------------- placing

	private static int place(CommandSourceStack source, String id, Vec3 at, float yaw) {
		Outcome outcome = place(source, id, source.getLevel(), at, yaw);
		if (!outcome.placed()) {
			source.sendFailure(outcome.message());
			return 0;
		}
		source.sendSuccess(outcome::message, true);
		return 1;
	}

	/**
	 * Places the guest {@code id} standing at {@code at} in {@code level}, facing {@code yaw}, for {@code source}, or says
	 * why not, with nothing changed. Server thread.
	 */
	static Outcome place(CommandSourceStack source, String id, ServerLevel level, Vec3 at, float yaw) {
		if (!CustomNpcs.isOperator(source)) {
			return refused(Refusal.NOT_OPERATOR, "operator", "Only operators can place town guests.");
		}
		GuestRoster.Guest guest = GuestRoster.get(id);
		if (guest == null) {
			return refused(Refusal.UNKNOWN, "unknown", "There is no town guest called %s. See /guests roster.", id);
		}
		if (!guest.available()) {
			return refused(Refusal.PENDING, "pending", "%s is pending: the owner has not said who they are yet, so they cannot be placed.",
					guest.name());
		}
		MinecraftServer server = level.getServer();
		GuestLedger ledger = GuestLedger.get(server);
		if (status(ledger, guest.id()) == Status.PLACED) {
			return refused(Refusal.PLACED, "placed",
					"%s is already placed (%s). A guest can be placed again only once the ledger records it as lost.",
					guest.name(), where(ledger.lastSeen(guest.id())));
		}

		// The body as CustomNpcs.create would stand it, and the blocks it would stand in.
		Townsfolk probe = JugcraftTown.TOWNSFOLK.create(level, EntitySpawnReason.COMMAND);
		if (probe == null) {
			return refused(Refusal.FAILED, "failed", "The guest could not be made there. Nothing was changed.");
		}
		probe.snapTo(at.x, at.y, at.z, yaw, 0);
		AABB body = probe.getBoundingBox();
		BlockPos low = BlockPos.containing(body.minX, body.minY, body.minZ);
		BlockPos high = BlockPos.containing(body.maxX - EPSILON, body.maxY - EPSILON, body.maxZ - EPSILON);
		for (BlockPos cell : BlockPos.betweenClosed(low, high)) {
			if (!level.isInWorldBounds(cell) || !level.getWorldBorder().isWithinBounds(cell)) {
				return refused(Refusal.OUTSIDE, "outside", "That spot is outside the world or its border.");
			}
			if (!level.isLoaded(cell)) {
				return refused(Refusal.UNLOADED, "unloaded", "That spot is not loaded. Go there first: placing never loads a chunk.");
			}
		}
		Entity placer = source.getEntity();
		UUID operator = placer == null ? null : placer.getUUID();
		for (BlockPos cell : BlockPos.betweenClosed(low, high)) {
			if (!LandRegistry.mayBuildFor(operator, level, cell)) {
				return refused(Refusal.CLAIMED, "claimed", "That spot is in land claimed by %s. Place guests only where you may build.",
						LandRegistry.ownerAt(level, cell.immutable()));
			}
			if (placer instanceof Player player ? TownProtection.denies(player, level, cell) : TownProtection.shieldsBlock(level, cell)) {
				return refused(Refusal.PROTECTED, "protected",
						"That spot is protected by the town or an operator region. Only an operator in creative mode may place a guest there.");
			}
		}
		AABB spot = new AABB(low.getX(), low.getY(), low.getZ(), high.getX() + 1, high.getY() + 1, high.getZ() + 1).deflate(EPSILON);
		List<Entity> inTheWay = level.getEntities((Entity) null, spot, GuestPlacement::ownerObject);
		if (!inTheWay.isEmpty()) {
			return refused(Refusal.OCCUPIED, "occupied", "%s is in that spot. Placing a guest never moves anything: choose a clear spot.",
					inTheWay.getFirst().getDisplayName());
		}
		if (!level.noCollision(probe)) {
			return refused(Refusal.BLOCKED, "blocked",
					"There is no room there: a guest needs a clear spot the size of a player, and placing never changes a block.");
		}

		Townsfolk npc = CustomNpcs.create(level, at, yaw, guest.name(), CustomNpcs.NPC, operator, guest.id());
		if (npc == null || npc.custom() == null) {
			return refused(Refusal.FAILED, "failed", "The guest could not be made there. Nothing was changed.");
		}
		if (!ledger.bind(guest.id(), npc.getUUID())) {
			CustomNpcs.remove(server, npc.custom().id());
			return refused(Refusal.BIND_REFUSED, "bind_refused",
					"The guest ledger refused the new %s (another entity holds the identity), so it was removed again.", guest.name());
		}
		GuestAppearances.apply(npc, guest.id());
		GuestAppearances.Appearance look = GuestAppearances.of(guest.id());
		Component done = Component.translatableWithFallback("message.jugcraft.guest.place.done",
				"%s (%s) now stands at %s as NPC #%s: %s (town skin %s).", guest.name(), guest.id(),
				npc.blockPosition().toShortString() + " in " + level.dimension().identifier(), npc.custom().id(),
				look == null ? GuestAppearances.note() : look.note(), npc.skin());
		return new Outcome(npc, null, done);
	}

	/**
	 * Something placing a guest must never push aside: anything but a player or an ordinary mob, other NPCs (townsfolk,
	 * custom NPCs and guests) included.
	 */
	static boolean ownerObject(Entity entity) {
		if (entity.isRemoved() || entity.isSpectator() || entity instanceof Player) {
			return false;
		}
		return !(entity instanceof Mob) || entity instanceof Townsfolk;
	}

	private static Outcome refused(Refusal refusal, String key, String english, Object... args) {
		return new Outcome(null, refusal, Component.translatableWithFallback("message.jugcraft.guest.place." + key, english, args));
	}

	// ---------------------------------------------------------------- the roster

	private static int roster(CommandSourceStack source) {
		if (!CustomNpcs.isOperator(source)) {
			source.sendFailure(Component.translatableWithFallback("message.jugcraft.guest.roster.operator",
					"Only operators can read the guest roster."));
			return 0;
		}
		for (Component line : rosterLines(source.getServer())) {
			source.sendSuccess(() -> line, false);
		}
		return GuestRoster.ALL.size();
	}

	/**
	 * What {@code /guests roster} says: a summary, then one line per roster entry with its id, name, whether it is
	 * pending, whether it is placed, missing (lost) or unplaced, whether it may marry, and its appearance.
	 */
	static List<Component> rosterLines(MinecraftServer server) {
		GuestLedger ledger = GuestLedger.get(server);
		List<Component> lines = new ArrayList<>();
		int available = 0, pending = 0, placed = 0, missing = 0;
		for (GuestRoster.Guest guest : GuestRoster.ALL) {
			Status status = status(ledger, guest.id());
			if (guest.available()) {
				available++;
			} else {
				pending++;
			}
			if (status == Status.PLACED) {
				placed++;
			} else if (status == Status.MISSING) {
				missing++;
			}
			lines.add(line(ledger, guest, status));
		}
		lines.add(0, Component.translatableWithFallback("message.jugcraft.guest.roster.header",
				"Town guests: %s available, %s pending; %s placed, %s missing.", available, pending, placed, missing));
		return lines;
	}

	/** Placed (a canonical entity the ledger has not lost), missing (lost and not replaced) or unplaced (never bound). */
	static Status status(GuestLedger ledger, String guest) {
		return ledger.canonical(guest) == null ? Status.UNPLACED : ledger.missing(guest) ? Status.MISSING : Status.PLACED;
	}

	private static Component line(GuestLedger ledger, GuestRoster.Guest guest, Status status) {
		Component availability = guest.pending() ? text("pending", "pending") : text("available", "available");
		Component place = switch (status) {
			case UNPLACED -> text("unplaced", "unplaced");
			case PLACED -> text("placed", "placed (%s)", where(ledger.lastSeen(guest.id())));
			case MISSING -> text("missing", "missing: lost (%s)", where(ledger.lastSeen(guest.id())));
		};
		Component marriage = guest.mayMarry() ? text("may_marry", "may marry")
				: guest.pending() ? text("no_marriage_pending", "no marriage while pending")
				: text("companionship", "companionship only, no marriage");
		GuestAppearances.Appearance look = GuestAppearances.of(guest.id());
		Component appearance = look == null ? text("no_appearance", "no appearance until the owner confirms who they are")
				: text("appearance", "%s (town skin %s)", look.note(), look.townSkin());
		return Component.translatableWithFallback("message.jugcraft.guest.roster.line", "%s, %s: %s, %s, %s; %s", guest.id(), guest.name(),
				availability, place, marriage, appearance);
	}

	private static Component text(String key, String english, Object... args) {
		return Component.translatableWithFallback("message.jugcraft.guest.roster." + key, english, args);
	}

	private static Component where(GuestLedger.@Nullable Place place) {
		return place == null ? Component.translatableWithFallback("message.jugcraft.guest.roster.nowhere", "not seen yet")
				: Component.translatableWithFallback("message.jugcraft.guest.roster.where", "last seen at %s in %s", place.pos().toShortString(),
						place.dimension().identifier().toString());
	}
}

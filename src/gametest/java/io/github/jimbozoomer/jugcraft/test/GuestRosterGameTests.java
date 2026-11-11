package io.github.jimbozoomer.jugcraft.test;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.jimbozoomer.jugcraft.finance.LandRegistry;
import io.github.jimbozoomer.jugcraft.finance.Ledger;
import io.github.jimbozoomer.jugcraft.town.CustomNpcs;
import io.github.jimbozoomer.jugcraft.town.TownProtection;
import io.github.jimbozoomer.jugcraft.town.Townsfolk;
import io.github.jimbozoomer.jugcraft.town.guests.GuestAppearances;
import io.github.jimbozoomer.jugcraft.town.guests.GuestLedger;
import io.github.jimbozoomer.jugcraft.town.guests.GuestMenu;
import io.github.jimbozoomer.jugcraft.town.guests.GuestPlacementTesting;
import io.github.jimbozoomer.jugcraft.town.guests.GuestRoster;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Town guests (docs/features/jze-town-relationships.md), worker C's server tests: the roster, the placeholder
 * appearances, operator placement and its refusals, {@code /guests roster}, and the guest menu's server side (validity
 * and stale clicks across a reopened menu).
 *
 * <p>Written for the integrated build. The first two tests stand alone. Every other test reads or writes the guest
 * ledger, so it needs worker A's {@code GuestLedger}; the two menu tests also need worker B's {@code GuestFavors} and
 * worker A's {@code GuestMarriage}, which the menu's synced values read whenever it is opened or clicked. Guests are
 * picked from the end of the roster, only when free to place, and removed again (lost, so placeable again) when each
 * test ends; {@link #placementRefusesAndChangesNothing} leaves one guest bound to an entity that never loads.
 */
public class GuestRosterGameTests {
	/** Saved ids are unique and well formed; pending guests can be neither placed nor married. */
	@GameTest(maxTicks = 1)
	public void theRosterIsUniqueAndPendingGuestsWait(GameTestHelper helper) {
		Set<String> ids = new HashSet<>();
		for (GuestRoster.Guest guest : GuestRoster.ALL) {
			helper.assertTrue(ids.add(guest.id()), "Repeated guest id " + guest.id());
			helper.assertTrue(GuestRoster.get(guest.id()) == guest, "Look-up by id finds " + guest.id());
			helper.assertTrue(guest.id().matches("[a-z][a-z0-9_]{1,31}"), "Malformed guest id " + guest.id());
			helper.assertTrue(!guest.pending() || (!guest.available() && !guest.mayMarry()), "A pending guest waits: " + guest.id());
		}
		for (String pending : new String[] {"rooni", "remielle", "guest_unnamed", "jakeey_kpop"}) {
			helper.assertTrue(GuestRoster.get(pending) != null && GuestRoster.get(pending).pending(), pending + " is pending");
		}
		helper.succeed();
	}

	/**
	 * Every available guest has a labelled placeholder from the town's own civilian skins, never final art, varied so
	 * that neighbours in the roster differ; pending guests have none.
	 */
	@GameTest(maxTicks = 1)
	public void everyAppearanceIsALabelledPlaceholderFromTheTownsSkins(GameTestHelper helper) {
		Set<String> townSkins = CustomNpcs.townSkins();
		Set<String> used = new HashSet<>();
		GuestAppearances.Appearance previous = null;
		for (GuestRoster.Guest guest : GuestRoster.ALL) {
			GuestAppearances.Appearance look = GuestAppearances.of(guest.id());
			if (guest.pending()) {
				helper.assertTrue(look == null, guest.id() + " is pending and has no appearance");
				continue;
			}
			helper.assertTrue(look != null && look.guest().equals(guest.id()), guest.id() + " has an appearance");
			helper.assertTrue(!look.finalArt() && look.placeholder(), guest.id() + " is a placeholder, not final art");
			helper.assertTrue(townSkins.contains(look.townSkin()) && GuestAppearances.PLACEHOLDER_SKINS.contains(look.townSkin()),
					guest.id() + " wears one of the town's civilian skins: " + look.townSkin());
			helper.assertTrue(look.note().getString().equals(GuestAppearances.NOTE), guest.id() + " is labelled: " + look.note().getString());
			helper.assertTrue(previous == null || !previous.townSkin().equals(look.townSkin()),
					guest.id() + " looks different from the guest before it in the roster");
			used.add(look.townSkin());
			previous = look;
		}
		for (String skin : GuestAppearances.PLACEHOLDER_SKINS) {
			helper.assertTrue(townSkins.contains(skin), "The town has the placeholder skin " + skin);
			helper.assertTrue(!skin.startsWith("guard") && !Set.of("mayor", "priest", "banker").contains(skin), skin + " is a civilian skin");
		}
		helper.assertTrue(used.size() == GuestAppearances.PLACEHOLDER_SKINS.size(), "Every placeholder skin is in use: " + used);
		helper.assertTrue(GuestAppearances.PLACEHOLDER_SKINS.contains(GuestPlacementTesting.fallbackSkin("a_future_guest"))
				&& GuestPlacementTesting.fallbackSkin("a_future_guest").equals(GuestPlacementTesting.fallbackSkin("a_future_guest")),
				"A guest added later gets a stable placeholder too");
		helper.succeed();
	}

	/**
	 * {@code /guests place <id> <pos> <yaw>} by an operator makes one guest NPC there, bound in the ledger and wearing its
	 * placeholder, and changes no block. While it stands, the guest cannot be placed again; once it is lost (removed for
	 * good) the ledger keeps the identity and an operator may place it again. Needs worker A's ledger.
	 */
	@GameTest(maxTicks = 20)
	public void anOperatorPlacesAGuestAndNoBlockChanges(GameTestHelper h) throws CommandSyntaxException {
		floor(h);
		ServerLevel level = h.getLevel();
		MinecraftServer server = level.getServer();
		String id = GuestPlacementTesting.freeGuest(server, List.of());
		h.assertTrue(id != null, "A guest is free to place");
		GuestRoster.Guest guest = GuestRoster.get(id);
		Map<BlockPos, BlockState> before = blocks(h);
		BlockPos at = h.absolutePos(new BlockPos(3, 2, 3));
		int placed = server.getCommands().getDispatcher().execute(
				"guests place " + id + " " + at.getX() + " " + at.getY() + " " + at.getZ() + " 90", console(h));
		List<Townsfolk> made = guests(h);
		h.assertTrue(placed == 1 && made.size() == 1, "The operator's /guests place makes one guest: " + made.size());
		Townsfolk npc = made.getFirst();
		try {
			h.assertTrue(npc.custom() != null && id.equals(npc.guest()) && npc.getName().getString().equals(guest.name()),
					"A custom NPC carrying the guest's id and name");
			h.assertTrue(npc.blockPosition().equals(at) && Math.abs(Mth.wrapDegrees(npc.getYRot() - 90.0F)) < 1.0F,
					"Standing on the named block, facing the named way");
			GuestLedger ledger = GuestLedger.get(server);
			h.assertTrue(npc.getUUID().equals(ledger.canonical(id)) && !ledger.missing(id)
					&& GuestPlacementTesting.status(server, id).equals("PLACED"), "Bound as the guest's canonical entity");
			GuestAppearances.Appearance look = GuestAppearances.of(id);
			h.assertTrue(look != null && look.placeholder() && npc.skin().equals(look.townSkin()), "Wearing its labelled placeholder");
			h.assertTrue(before.equals(blocks(h)), "Placing changed no block");

			GuestPlacementTesting.Placed again = GuestPlacementTesting.place(console(h), id, level, centre(h, 5, 5), 0);
			h.assertTrue(again.npc() == null && again.refusal().equals("PLACED") && guests(h).size() == 1,
					"A guest already standing cannot be placed again: " + again.refusal());
		} finally {
			remove(server, npc);
		}
		h.assertTrue(GuestPlacementTesting.status(server, id).equals("MISSING"), "Removed for good, the guest is lost, not freed");
		GuestPlacementTesting.Placed replaced = GuestPlacementTesting.place(console(h), id, level, centre(h, 5, 5), 0);
		try {
			h.assertTrue(replaced.npc() != null && replaced.npc().getUUID().equals(GuestLedger.get(server).canonical(id))
					&& !replaced.npc().getUUID().equals(npc.getUUID()), "A lost guest may be placed again: " + replaced.refusal());
		} finally {
			remove(server, replaced.npc());
		}
		h.assertTrue(before.equals(blocks(h)), "Still no block changed");
		h.succeed();
	}

	/**
	 * Every refusal, with nothing made, moved or changed: a pending or unknown id, a guest whose canonical entity is not
	 * loaded, an unloaded chunk, a block in the way, an armor stand in the spot, an item frame on the spot's wall, someone
	 * else's land claim, an operator's protected region, and a player who is not an operator (who cannot even reach the
	 * command). Needs worker A's ledger.
	 */
	@GameTest(maxTicks = 20)
	public void placementRefusesAndChangesNothing(GameTestHelper h) throws CommandSyntaxException {
		floor(h);
		ServerLevel level = h.getLevel();
		MinecraftServer server = level.getServer();
		CommandDispatcher<CommandSourceStack> dispatcher = server.getCommands().getDispatcher();
		CommandSourceStack console = console(h);
		// Fixtures: a block in the way at (1, 2, 1), an armor stand at (5, 2, 1), an item frame on a wall at (2, 2, 6).
		h.setBlock(new BlockPos(1, 2, 1), Blocks.STONE);
		Vec3 standing = centre(h, 5, 1);
		ArmorStand stand = new ArmorStand(level, standing.x, standing.y, standing.z);
		h.assertTrue(level.addFreshEntity(stand), "The armor stand is in the world");
		BlockPos framed = h.absolutePos(new BlockPos(2, 2, 6));
		level.setBlockAndUpdate(framed.west(), Blocks.STONE.defaultBlockState());
		ItemFrame frame = new ItemFrame(level, framed, Direction.EAST);
		h.assertTrue(level.addFreshEntity(frame), "The item frame hangs on the wall");
		Vec3 standAt = stand.position();
		Vec3 frameAt = frame.position();
		h.assertTrue(GuestPlacementTesting.ownerObject(stand) && GuestPlacementTesting.ownerObject(frame), "Both are owners' things");
		Map<BlockPos, BlockState> before = blocks(h);
		ServerPlayer visitor = TestPlayers.survivor(h);
		Vec3 aside = centre(h, 6, 6);
		visitor.snapTo(aside.x, aside.y, aside.z, 0, 0);
		String id = GuestPlacementTesting.freeGuest(server, List.of());
		h.assertTrue(id != null, "A guest is free to place");
		try {
			refused(h, console, "rooni", centre(h, 3, 3), "PENDING");
			refused(h, console, "nobody_at_all", centre(h, 3, 3), "UNKNOWN");
			refused(h, console, id, centre(h, 1, 1), "BLOCKED");
			refused(h, console, id, centre(h, 5, 1), "OCCUPIED");
			refused(h, console, id, Vec3.atBottomCenterOf(framed), "OCCUPIED");
			refused(h, console, id, centre(h, 3, 3).add(100_000, 0, 0), "UNLOADED");

			// Not an operator: refused by the command tree and again on the server.
			CommandSourceStack personal = visitor.createCommandSourceStack().withSuppressedOutput();
			h.assertFalse(CustomNpcs.isOperator(personal), "The survivor is not an operator (precondition)");
			refused(h, personal, id, centre(h, 3, 3), "NOT_OPERATOR");
			h.assertFalse(dispatcher.getRoot().getChild("guests").canUse(personal), "A player who is not an operator cannot use /guests");
			boolean thrown = false;
			try {
				dispatcher.execute("guests place " + id, personal.withPosition(centre(h, 3, 3)));
			} catch (CommandSyntaxException expected) {
				thrown = true;
			}
			h.assertTrue(thrown && guests(h).isEmpty(), "/guests place by a player who is not an operator does nothing");

			// Someone else's land claim, for the console and for an operator standing in for the survivor.
			BlockPos spot = h.absolutePos(new BlockPos(3, 2, 3));
			long key = LandRegistry.keyOf(spot);
			LandRegistry land = LandRegistry.get(server);
			var book = Ledger.get(server).book();
			h.assertFalse(LandRegistry.isClaimed(level, spot), "The test's chunk is unclaimed (precondition)");
			h.assertTrue(land.assign(book, key, book.account(UUID.randomUUID(), "GuestClaimOwner"), 0).ok(), "A stranger's claim");
			try {
				refused(h, console, id, centre(h, 3, 3), "CLAIMED");
				refused(h, operatorAs(server, visitor, level), id, centre(h, 3, 3), "CLAIMED");
			} finally {
				land.abandon(visitor, book, key, true, 0);
			}
			h.assertFalse(LandRegistry.isClaimed(level, spot), "The claim fixture is gone");

			// An operator's protected region over the spot (shared with the town's protection).
			String region = "guest_test_" + Integer.toHexString(UUID.randomUUID().hashCode() & 0xFFFFFF);
			CommandSourceStack regionOperator = server.createCommandSourceStack().withEntity(visitor).withSuppressedOutput();
			visitor.snapTo(spot.getX() + 0.5, spot.getY(), spot.getZ() + 0.5, 0, 0);
			dispatcher.execute("protect pos1", regionOperator);
			visitor.snapTo(spot.getX() + 0.5, spot.getY() + 2, spot.getZ() + 0.5, 0, 0);
			dispatcher.execute("protect pos2", regionOperator);
			dispatcher.execute("protect create " + region, regionOperator);
			visitor.snapTo(aside.x, aside.y, aside.z, 0, 0);
			try {
				h.assertTrue(TownProtection.shieldsBlock(level, spot), "The region protects the spot (precondition)");
				refused(h, console, id, centre(h, 3, 3), "PROTECTED");
				refused(h, operatorAs(server, visitor, level), id, centre(h, 3, 3), "PROTECTED");
			} finally {
				dispatcher.execute("protect remove " + region, regionOperator);
			}
			h.assertFalse(TownProtection.shieldsBlock(level, spot), "The region fixture is gone");

			// Already placed, its canonical entity not loaded anywhere (as when its chunk is unloaded).
			String held = GuestPlacementTesting.freeGuest(server, List.of(id));
			h.assertTrue(held != null && GuestLedger.get(server).bind(held, UUID.randomUUID()), "The ledger holds an unloaded entity for " + held);
			refused(h, console, held, centre(h, 3, 3), "PLACED");
			h.assertTrue(GuestPlacementTesting.status(server, held).equals("PLACED"), "It stays placed");

			h.assertTrue(guests(h).isEmpty(), "No guest was made");
			h.assertTrue(stand.isAlive() && stand.position().equals(standAt) && frame.isAlive() && frame.position().equals(frameAt),
					"The armor stand and item frame were not moved");
			h.assertTrue(before.equals(blocks(h)), "No block changed");
		} finally {
			stand.discard();
			frame.discard();
		}
		h.succeed();
	}

	/**
	 * {@code /guests roster}: a summary, then every entry with its id, name, pending or available, placed, missing or
	 * unplaced, whether it may marry, and its appearance (always "placeholder appearance, reference pending"); operators
	 * only. Needs worker A's ledger.
	 */
	@GameTest(maxTicks = 20)
	public void theRosterCommandListsEveryEntry(GameTestHelper h) throws CommandSyntaxException {
		floor(h);
		ServerLevel level = h.getLevel();
		MinecraftServer server = level.getServer();
		String id = GuestPlacementTesting.freeGuest(server, List.of());
		h.assertTrue(id != null, "A guest is free to place");
		GuestPlacementTesting.Placed placed = GuestPlacementTesting.place(console(h), id, level, centre(h, 3, 3), 0);
		h.assertTrue(placed.npc() != null, "Placed: " + placed.refusal());
		try {
			List<Component> lines = GuestPlacementTesting.rosterLines(server);
			h.assertTrue(lines.size() == GuestRoster.ALL.size() + 1, "A header and a line per entry: " + lines.size());
			long pending = GuestRoster.ALL.stream().filter(GuestRoster.Guest::pending).count();
			String header = lines.getFirst().getString();
			h.assertTrue(header.contains((GuestRoster.ALL.size() - pending) + " available") && header.contains(pending + " pending"),
					"The header counts the entries: " + header);
			for (int i = 0; i < GuestRoster.ALL.size(); i++) {
				GuestRoster.Guest guest = GuestRoster.ALL.get(i);
				String line = lines.get(i + 1).getString();
				h.assertTrue(line.startsWith(guest.id() + ", " + guest.name() + ": "), "The line names the guest: " + line);
				if (guest.pending()) {
					h.assertTrue(line.contains(": pending, unplaced, no marriage while pending; no appearance"), "Pending: " + line);
				} else {
					GuestAppearances.Appearance look = GuestAppearances.of(guest.id());
					h.assertTrue(line.contains(": available, ") && line.contains(GuestAppearances.NOTE + " (town skin " + look.townSkin() + ")"),
							"Available, with its labelled placeholder: " + line);
					h.assertTrue(line.contains(guest.mayMarry() ? ", may marry;" : ", companionship only, no marriage;"),
							"Whether it may marry: " + line);
				}
				String status = GuestPlacementTesting.status(server, guest.id());
				h.assertTrue(line.contains(switch (status) {
					case "PLACED" -> ", placed (";
					case "MISSING" -> ", missing: lost (";
					default -> ", unplaced,";
				}), "Where it is (" + status + "): " + line);
			}
			String mine = lines.get(GuestRoster.ALL.indexOf(GuestRoster.get(id)) + 1).getString();
			h.assertTrue(mine.contains(", placed ("), "The guest just placed is listed as placed: " + mine);

			CommandDispatcher<CommandSourceStack> dispatcher = server.getCommands().getDispatcher();
			h.assertTrue(dispatcher.execute("guests roster", console(h)) == GuestRoster.ALL.size(), "/guests roster answers for every entry");
			ServerPlayer visitor = TestPlayers.survivor(h);
			h.assertFalse(dispatcher.getRoot().getChild("guests").canUse(visitor.createCommandSourceStack()),
					"A player who is not an operator cannot read the roster");
		} finally {
			remove(server, placed.npc());
		}
		h.succeed();
	}

	/**
	 * The guest menu's server side, with a real survival player: valid beside the guest, invalid beyond
	 * {@code INTERACT_RANGE} and valid again on coming back, and invalid for a second, unbound copy of the guest (not its
	 * canonical entity). Needs workers A and B: opening a menu syncs values from the ledger, favors and marriage.
	 */
	@GameTest(maxTicks = 20)
	public void theMenuIsValidOnlyBesideTheCanonicalGuest(GameTestHelper h) {
		floor(h);
		ServerLevel level = h.getLevel();
		MinecraftServer server = level.getServer();
		String id = GuestPlacementTesting.freeGuest(server, List.of());
		h.assertTrue(id != null, "A guest is free to place");
		GuestPlacementTesting.Placed placed = GuestPlacementTesting.place(console(h), id, level, centre(h, 1, 1), 0);
		Townsfolk npc = placed.npc();
		h.assertTrue(npc != null, "Placed: " + placed.refusal());
		Townsfolk copy = null;
		ServerPlayer player = TestPlayers.survivor(h);
		try {
			Vec3 beside = centre(h, 3, 1);
			player.snapTo(beside.x, beside.y, beside.z, 0, 0);
			GuestMenu.open(player, npc, id);
			h.assertTrue(player.containerMenu instanceof GuestMenu, "Talking opens the guest menu");
			GuestMenu menu = (GuestMenu) player.containerMenu;
			h.assertTrue(menu.stillValid(player), "Valid beside the guest");

			player.snapTo(beside.x + 8, beside.y, beside.z, 0, 0);
			h.assertFalse(menu.stillValid(player), "Invalid beyond the interaction range");
			player.snapTo(beside.x, beside.y, beside.z, 0, 0);
			h.assertTrue(menu.stillValid(player), "Valid again beside it");

			copy = CustomNpcs.create(level, centre(h, 3, 3), 0, GuestRoster.get(id).name(), CustomNpcs.NPC, null, id);
			h.assertTrue(copy != null && id.equals(copy.guest()) && !copy.getUUID().equals(GuestLedger.get(server).canonical(id)),
					"A second, unbound copy of the guest (precondition)");
			GuestMenu.open(player, copy, id);
			h.assertTrue(player.containerMenu instanceof GuestMenu other && other != menu && !other.stillValid(player),
					"A menu on a copy that is not the canonical entity is invalid");
			player.closeContainer();
		} finally {
			remove(server, copy);
			remove(server, npc);
		}
		h.succeed();
	}

	/**
	 * Stale clicks after a reopen: opening the menu again moves the player's revision on, so a button id from the
	 * earlier view is refused by {@code clickMenuButton} (on the new menu and the old one) without being acted on; a
	 * current click passes the revision check, and sending it a second time is refused. Needs workers A and B.
	 */
	@GameTest(maxTicks = 20)
	public void aReopenedMenuRefusesTheOldButton(GameTestHelper h) {
		floor(h);
		ServerLevel level = h.getLevel();
		MinecraftServer server = level.getServer();
		String id = GuestPlacementTesting.freeGuest(server, List.of());
		h.assertTrue(id != null, "A guest is free to place");
		GuestPlacementTesting.Placed placed = GuestPlacementTesting.place(console(h), id, level, centre(h, 1, 1), 0);
		Townsfolk npc = placed.npc();
		h.assertTrue(npc != null, "Placed: " + placed.refusal());
		ServerPlayer player = TestPlayers.survivor(h);
		try {
			Vec3 beside = centre(h, 3, 1);
			player.snapTo(beside.x, beside.y, beside.z, 0, 0);
			GuestMenu.open(player, npc, id);
			GuestMenu first = (GuestMenu) player.containerMenu;
			int seen = first.revision();
			int stale = GuestMenu.button(GuestMenu.Action.PROPOSE_CANCEL, seen);

			GuestMenu.open(player, npc, id);
			h.assertTrue(player.containerMenu instanceof GuestMenu && player.containerMenu != first, "Reopened");
			GuestMenu second = (GuestMenu) player.containerMenu;
			int now = second.revision();
			h.assertTrue(now != seen, "Opening the menu again moved the revision on");
			h.assertFalse(second.clickMenuButton(player, stale), "The earlier view's button is refused by the new menu");
			h.assertFalse(first.clickMenuButton(player, stale), "and by the old one");
			h.assertTrue(second.revision() == now, "Refused before anything was done: the revision did not move");

			int current = GuestMenu.button(GuestMenu.Action.PROPOSE_CANCEL, now);
			second.clickMenuButton(player, current);
			int after = second.revision();
			h.assertTrue(after != now, "A current click passes the revision check (cancelling nothing changes nothing)");
			h.assertFalse(second.clickMenuButton(player, current), "The same click sent again is refused");
			h.assertTrue(second.revision() == after, "and is not acted on");
			player.closeContainer();
		} finally {
			remove(server, npc);
		}
		h.succeed();
	}

	// ---------------------------------------------------------------- helpers

	private static void refused(GameTestHelper h, CommandSourceStack source, String id, Vec3 at, String expected) {
		GuestPlacementTesting.Placed result = GuestPlacementTesting.place(source, id, h.getLevel(), at, 0);
		if (result.npc() != null) {
			remove(h.getLevel().getServer(), result.npc());
		}
		h.assertTrue(result.npc() == null && result.refusal().equals(expected),
				id + " at " + at + ": expected " + expected + ", got " + result.refusal() + " (" + result.message().getString() + ")");
	}

	private static Vec3 centre(GameTestHelper h, int x, int z) {
		BlockPos at = h.absolutePos(new BlockPos(x, 2, z));
		return new Vec3(at.getX() + 0.5, at.getY(), at.getZ() + 0.5);
	}

	/** The console, an operator, in the test's level. */
	private static CommandSourceStack console(GameTestHelper h) {
		return h.getLevel().getServer().createCommandSourceStack().withSuppressedOutput().withLevel(h.getLevel()).withPosition(centre(h, 1, 1))
				.withRotation(new Vec2(0, 0));
	}

	/** An operator's command source acting as {@code player} (whose own claims, trust and game mode count). */
	private static CommandSourceStack operatorAs(MinecraftServer server, ServerPlayer player, ServerLevel level) {
		return server.createCommandSourceStack().withEntity(player).withLevel(level).withSuppressedOutput();
	}

	private static AABB area(GameTestHelper h) {
		return new AABB(Vec3.atCenterOf(h.absolutePos(BlockPos.ZERO)), Vec3.atCenterOf(h.absolutePos(new BlockPos(7, 5, 7)))).inflate(1);
	}

	/** Town guests standing in the test. */
	private static List<Townsfolk> guests(GameTestHelper h) {
		return h.getLevel().getEntitiesOfClass(Townsfolk.class, area(h), npc -> npc.guest() != null && !npc.isRemoved());
	}

	private static Map<BlockPos, BlockState> blocks(GameTestHelper h) {
		Map<BlockPos, BlockState> states = new HashMap<>();
		for (BlockPos pos : BlockPos.betweenClosed(h.absolutePos(BlockPos.ZERO), h.absolutePos(new BlockPos(7, 5, 7)))) {
			states.put(pos.immutable(), h.getLevel().getBlockState(pos));
		}
		return states;
	}

	private static void remove(MinecraftServer server, @Nullable Townsfolk npc) {
		if (npc != null && npc.custom() != null && !npc.isRemoved()) {
			CustomNpcs.remove(server, npc.custom().id());
		}
	}

	private static void floor(GameTestHelper h) {
		for (int x = 0; x < 8; x++) {
			for (int z = 0; z < 8; z++) {
				h.setBlock(new BlockPos(x, 1, z), Blocks.STONE);
			}
		}
	}
}

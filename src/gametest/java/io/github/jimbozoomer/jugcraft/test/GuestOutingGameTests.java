package io.github.jimbozoomer.jugcraft.test;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.jimbozoomer.jugcraft.finance.LandRegistry;
import io.github.jimbozoomer.jugcraft.finance.Ledger;
import io.github.jimbozoomer.jugcraft.town.CustomNpcs;
import io.github.jimbozoomer.jugcraft.town.TownProtection;
import io.github.jimbozoomer.jugcraft.town.Townsfolk;
import io.github.jimbozoomer.jugcraft.town.guests.GuestActivity;
import io.github.jimbozoomer.jugcraft.town.guests.GuestFavorTesting;
import io.github.jimbozoomer.jugcraft.town.guests.GuestFavors;
import io.github.jimbozoomer.jugcraft.town.guests.GuestFeature;
import io.github.jimbozoomer.jugcraft.town.guests.GuestLedger;
import io.github.jimbozoomer.jugcraft.town.guests.GuestOutingTesting;
import io.github.jimbozoomer.jugcraft.town.guests.GuestOutings;
import io.github.jimbozoomer.jugcraft.town.guests.GuestResult;
import io.github.jimbozoomer.jugcraft.town.guests.GuestRewards;
import io.github.jimbozoomer.jugcraft.town.guests.GuestRoster;
import io.github.jimbozoomer.jugcraft.town.guests.GuestRosterTesting;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * JZE guest expansion (docs/plans/jze-guest-expansion-nov11.md, docs/plans/jze-guest-outings-design.md): worker C's
 * server tests for outings and home visits. Registered by the coordinator.
 *
 * <p>Every test uses fresh survival players and synthetic guests of its own ({@code test_c_*}, through
 * {@link GuestRosterTesting}, never one of the real roster ids), placed on an 8x8 stone floor and bound in the ledger as
 * their canonical entities; they are discarded when the test ends (which ends anything still holding them). Time is
 * moved only through {@link GuestOutingTesting#step}, one guest at a time, at a game time the test chooses; days through
 * {@link GuestFavorTesting#pinDay}. Searches that could reach a neighbouring test's area use a narrow radius. Each test
 * runs within one tick, so no guest walks during it: arrival is set up by where the guest already stands.
 */
public class GuestOutingGameTests {
	/** The favors day pinned for the reward tests: far from the overworld's and from worker B's. */
	private static final long DAY = 9_000L;

	/** An invitation is refused while the guest is busy, while the player already holds a guest, and out of range. */
	@GameTest(maxTicks = 20)
	public void anInvitationIsRefusedWhenTheGuestIsBusyOrThePlayerOutOfRange(GameTestHelper h) {
		floor(h);
		MinecraftServer server = h.getLevel().getServer();
		String guest = GuestRosterTesting.guest("test_c_busy", false);
		String other = GuestRosterTesting.guest("test_c_busy_other", false);
		List<Townsfolk> made = new ArrayList<>();
		ServerPlayer player = visitor(h, 7, 7);
		GuestActivity activities = GuestActivity.get(server);
		try {
			Townsfolk npc = place(h, guest, 1, 1, made);
			Townsfolk second = place(h, other, 5, 1, made);
			GuestFeature outings = GuestOutingTesting.feature();

			h.assertFalse(outings.available(player, npc, guest, "invite_fair"), "Not offered out of range");
			GuestResult far = outings.act(player, npc, guest, "invite_fair");
			h.assertFalse(far.done() || activities.current(guest) != null, "Refused out of range, nothing held: " + far.message().getString());

			move(player, h, 3, 3);
			activities.begin(guest, new GuestActivity.Activity("ceremony", UUID.randomUUID(), GuestOutingTesting.now(server),
					h.getLevel().dimension(), npc.blockPosition()));
			try {
				h.assertFalse(outings.available(player, npc, guest, "invite_fair"), "Not offered while another activity holds the guest");
				GuestResult busy = outings.act(player, npc, guest, "invite_fair");
				h.assertFalse(busy.done(), "Refused while the guest is busy: " + busy.message().getString());
				h.assertTrue(activities.current(guest) != null && activities.current(guest).kind().equals("ceremony"),
						"The other activity still holds the guest, untouched");
			} finally {
				activities.end(guest, "ceremony");
			}

			GuestResult first = GuestOutingTesting.invite(player, second, other, "fair", 3);
			h.assertTrue(first.done(), "An outing with the second guest begins: " + first.message().getString());
			h.assertFalse(outings.available(player, npc, guest, "invite_fair"), "Not offered while the player holds another guest");
			GuestResult twice = outings.act(player, npc, guest, "invite_fair");
			h.assertFalse(twice.done() || activities.current(guest) != null, "Refused: one guest at a time: " + twice.message().getString());

			h.assertTrue(outings.act(player, second, other, "end_outing").done(), "The first outing ends");
			GuestResult now = GuestOutingTesting.invite(player, npc, guest, "fair", 3);
			h.assertTrue(now.done() && activities.current(guest) != null, "Free again, the invitation is accepted: " + now.message().getString());
			h.succeed();
		} finally {
			clear(made);
		}
	}

	/** An outing begins at a fishing spot by the water, holds the guest there, ends on request, and changes no block. */
	@GameTest(maxTicks = 20)
	public void anOutingBeginsAndEndsAndChangesNoBlock(GameTestHelper h) {
		floor(h);
		water(h);
		MinecraftServer server = h.getLevel().getServer();
		String guest = GuestRosterTesting.guest("test_c_begin", false);
		List<Townsfolk> made = new ArrayList<>();
		ServerPlayer player = visitor(h, 3, 3);
		GuestActivity activities = GuestActivity.get(server);
		try {
			Townsfolk npc = place(h, guest, 1, 1, made);
			GuestFeature outings = GuestOutingTesting.feature();
			Map<BlockPos, BlockState> before = blocks(h);
			h.assertTrue(outings.available(player, npc, guest, "invite_fishing"), "Fishing is offered beside the guest");
			h.assertFalse(outings.available(player, npc, guest, "end_outing"), "Nothing to end yet");

			GuestResult began = outings.act(player, npc, guest, "invite_fishing");
			h.assertTrue(began.done(), "The fishing outing begins: " + began.message().getString());
			GuestActivity.Activity held = activities.current(guest);
			h.assertTrue(held != null && held.kind().equals(GuestOutings.OUTING) && held.player().equals(player.getUUID())
					&& held.dimension().equals(h.getLevel().dimension()), "One outing activity holds the guest for this player");
			List<BlockPos> banks = List.of(at(h, 4, 2), at(h, 4, 3), at(h, 7, 2), at(h, 7, 3), at(h, 5, 1), at(h, 6, 1), at(h, 5, 4), at(h, 6, 4));
			h.assertTrue(banks.contains(held.anchor()), "The place is on the bank beside the open water: " + held.anchor());
			h.assertTrue(npc.anchor().equals(held.anchor()) && npc.range() == GuestActivity.RANGE, "The guest's goals now lead to the place");
			h.assertTrue(GuestOutingTesting.tracked(guest), "The outing is tracked");
			h.assertTrue(outings.available(player, npc, guest, "end_outing") && !outings.available(player, npc, guest, "invite_fair"),
					"Ending is offered; another invitation is not");

			GuestResult ended = outings.act(player, npc, guest, "end_outing");
			h.assertTrue(ended.done(), "The player ends it: " + ended.message().getString());
			h.assertTrue(activities.current(guest) == null && !GuestOutingTesting.tracked(guest), "Nothing holds the guest any more");
			h.assertTrue(npc.anchor().equals(npc.home()), "The guest's anchor is its post again");
			h.assertFalse(outings.act(player, npc, guest, "end_outing").done(), "Ending it again does nothing");
			h.assertTrue(before.equals(blocks(h)), "No block changed");
			h.succeed();
		} finally {
			clear(made);
			unwater(h);
		}
	}

	/**
	 * Ending sends the guest home: its anchor is the post again and its walk is watched; stuck, it is put at its post,
	 * but never while the post is blocked.
	 */
	@GameTest(maxTicks = 20)
	public void cancellingReturnsTheGuestToItsPost(GameTestHelper h) {
		floor(h);
		MinecraftServer server = h.getLevel().getServer();
		String guest = GuestRosterTesting.guest("test_c_return", false);
		List<Townsfolk> made = new ArrayList<>();
		ServerPlayer player = visitor(h, 4, 5);
		try {
			Townsfolk npc = place(h, guest, 1, 1, made);
			BlockPos post = npc.home();
			h.assertTrue(post.equals(at(h, 1, 1)), "The guest's post is where it was placed");
			h.assertTrue(GuestOutingTesting.begin(player, npc, guest, "board_game", at(h, 6, 6)).done(), "A board game begins at (6, 6)");
			Vec3 there = centre(h, 6, 6);
			npc.teleportTo(there.x, there.y, there.z);   // as if it had walked there (the test's own fixture)

			h.assertTrue(GuestOutingTesting.feature().act(player, npc, guest, "end_outing").done(), "The player ends it beside the guest");
			h.assertTrue(GuestActivity.get(server).current(guest) == null && npc.anchor().equals(post), "Its anchor is its post again");
			h.assertTrue(GuestOutingTesting.returning(guest), "Its walk home is watched");

			h.setBlock(new BlockPos(1, 2, 1), Blocks.STONE);   // the post blocked
			long now = GuestOutingTesting.now(server);
			int wait = GuestOutingTesting.returnStuckTicks();
			GuestOutingTesting.step(server, guest, now + 1);
			GuestOutingTesting.step(server, guest, now + 1 + wait);
			h.assertTrue(npc.blockPosition().equals(at(h, 6, 6)) && GuestOutingTesting.returning(guest),
					"Stuck, but never put into a block: it stays and is still watched");

			h.setBlock(new BlockPos(1, 2, 1), Blocks.AIR);
			GuestOutingTesting.step(server, guest, now + 2 + 2L * wait);
			h.assertTrue(npc.blockPosition().equals(post), "With the post clear and loaded, the stuck guest is put at its post: " + npc.blockPosition());
			h.assertFalse(GuestOutingTesting.returning(guest), "Home: no longer watched");
			h.succeed();
		} finally {
			clear(made);
		}
	}

	/** Logging out ends the outing, and its guest heads home. */
	@GameTest(maxTicks = 20)
	public void loggingOutEndsTheOuting(GameTestHelper h) {
		floor(h);
		MinecraftServer server = h.getLevel().getServer();
		String guest = GuestRosterTesting.guest("test_c_logout", false);
		List<Townsfolk> made = new ArrayList<>();
		ServerPlayer player = visitor(h, 3, 3);
		try {
			Townsfolk npc = place(h, guest, 1, 1, made);
			h.assertTrue(GuestOutingTesting.invite(player, npc, guest, "fair", 3).done(), "A fair outing begins");
			TestPlayers.leave(player);
			GuestOutingTesting.step(server, guest, GuestOutingTesting.now(server));
			h.assertTrue(GuestActivity.get(server).current(guest) == null && !GuestOutingTesting.tracked(guest),
					"The player left the game: the activity is ended");
			h.assertTrue(npc.anchor().equals(npc.home()), "The guest heads back to its post");
			h.succeed();
		} finally {
			clear(made);
		}
	}

	/**
	 * Chunk unload: a place that is not loaded ends the outing, and so does the guest's own unload, whose walk home is
	 * then watched when it loads again. Restart: the state's codec keeps visit spots and the day's outings, and recovery
	 * ends outing and visit activities nothing tracks (as after a restart) or older than the bound, leaving a live outing
	 * and other features' activities alone.
	 */
	@GameTest(maxTicks = 20)
	public void unloadingAndRestartingEndOutingsAndSendGuestsHome(GameTestHelper h) {
		floor(h);
		ServerLevel level = h.getLevel();
		MinecraftServer server = level.getServer();
		String far = GuestRosterTesting.guest("test_c_unload_far", false);
		String away = GuestRosterTesting.guest("test_c_unload_self", false);
		String stale = GuestRosterTesting.guest("test_c_restart_stale", false);
		String orphan = GuestRosterTesting.guest("test_c_restart_orphan", false);
		String live = GuestRosterTesting.guest("test_c_restart_live", false);
		String ceremony = GuestRosterTesting.guest("test_c_restart_other", false);
		List<Townsfolk> made = new ArrayList<>();
		ServerPlayer player = visitor(h, 3, 3);
		ServerPlayer friend = visitor(h, 3, 5);
		GuestActivity activities = GuestActivity.get(server);
		try {
			// The place's chunk is not loaded (an outing begun just before it unloaded).
			Townsfolk farNpc = place(h, far, 1, 1, made);
			h.assertTrue(GuestOutingTesting.begin(player, farNpc, far, "fair", at(h, 3, 3).offset(100_000, 0, 0)).done(), "Begun (precondition)");
			GuestOutingTesting.step(server, far, GuestOutingTesting.now(server));
			h.assertTrue(activities.current(far) == null && !GuestOutingTesting.tracked(far), "An unloaded place ends the outing");
			h.assertTrue(farNpc.anchor().equals(farNpc.home()), "and the guest heads back to its post");

			// The guest's own entity unloads with its chunk.
			Townsfolk awayNpc = place(h, away, 5, 1, made);
			h.assertTrue(GuestOutingTesting.invite(player, awayNpc, away, "fair", 3).done(), "A second outing begins");
			GuestOutingTesting.unloaded(level, awayNpc);
			h.assertTrue(activities.current(away) == null && !GuestOutingTesting.tracked(away), "The guest's unload ends the outing");
			h.assertTrue(GuestOutingTesting.returnsOnLoad(away) && !GuestOutingTesting.returning(away), "Its walk home is watched when it loads");
			GuestOutingTesting.loaded(level, awayNpc);
			h.assertTrue(GuestOutingTesting.returning(away) && !GuestOutingTesting.returnsOnLoad(away), "Loaded again, its walk home is watched");

			// The per-player state survives a save and load through its codec.
			UUID me = player.getUUID();
			h.assertTrue(GuestOutingTesting.setSpot(player).done(), "A visit spot on open, unclaimed ground (precondition)");
			GuestOutingTesting.markDone(player, far, "fishing");
			BlockPos spot = GuestOutingTesting.spot(server, me);
			Tag saved = GuestOutingTesting.save(server);
			GuestOutingTesting.forget(server, me);
			h.assertTrue(GuestOutingTesting.spot(server, me) == null && !GuestOutingTesting.done(player, far, "fishing"), "Forgotten (precondition)");
			GuestOutingTesting.load(server, saved, me);
			h.assertTrue(at(h, 3, 3).equals(spot) && at(h, 3, 3).equals(GuestOutingTesting.spot(server, me)), "The visit spot is read back");
			h.assertTrue(GuestOutingTesting.done(player, far, "fishing") && !GuestOutingTesting.done(player, far, "fair"),
					"The day's outings are read back, and only those");

			// Restart recovery: saved activities nothing tracks any more.
			Townsfolk staleNpc = place(h, stale, 1, 5, made);
			Townsfolk orphanNpc = place(h, orphan, 1, 7, made);
			Townsfolk liveNpc = place(h, live, 5, 5, made);
			Townsfolk otherNpc = place(h, ceremony, 7, 7, made);
			long now = GuestOutingTesting.now(server);
			h.assertTrue(GuestOutingTesting.invite(friend, liveNpc, live, "fair", 3).done(), "A live outing (another player's)");
			h.assertTrue(activities.begin(stale, new GuestActivity.Activity(GuestOutings.OUTING, UUID.randomUUID(),
					now - GuestOutingTesting.outingBound() - 1, level.dimension(), at(h, 2, 5))), "A stale outing from before a restart");
			h.assertTrue(activities.begin(orphan, new GuestActivity.Activity(GuestOutings.VISIT, UUID.randomUUID(), now, level.dimension(),
					at(h, 2, 7))), "A recent visit nothing tracks (as after a restart)");
			h.assertTrue(activities.begin(ceremony, new GuestActivity.Activity("ceremony", UUID.randomUUID(), now - 100_000L, level.dimension(),
					at(h, 6, 7))), "Another feature's old activity");
			try {
				h.assertTrue(staleNpc.anchor().equals(at(h, 2, 5)) && orphanNpc.anchor().equals(at(h, 2, 7)), "Both guests are held (precondition)");
				int ended = GuestOutingTesting.recover(server);
				h.assertTrue(ended >= 2 && activities.current(stale) == null && activities.current(orphan) == null,
						"Recovery ends both: " + ended);
				h.assertTrue(staleNpc.anchor().equals(staleNpc.home()) && orphanNpc.anchor().equals(orphanNpc.home()),
						"and both guests head back to their posts");
				h.assertTrue(GuestOutingTesting.returning(stale) && GuestOutingTesting.returning(orphan), "Their walks home are watched");
				h.assertTrue(activities.current(live) != null && GuestOutingTesting.tracked(live), "A live, tracked outing is left alone");
				h.assertTrue(activities.current(ceremony) != null && activities.current(ceremony).kind().equals("ceremony"),
						"Another feature's activity is never touched");
				h.assertTrue(otherNpc.anchor().equals(at(h, 6, 7)), "and still holds its guest");
			} finally {
				activities.end(ceremony, "ceremony");
			}
			h.succeed();
		} finally {
			clear(made);
			GuestOutingTesting.forget(server, player.getUUID());
		}
	}

	/** A guest that makes no progress towards the place for the stuck window: the outing ends and it heads home. */
	@GameTest(maxTicks = 20)
	public void aStuckOutingEndsAndTheGuestHeadsHome(GameTestHelper h) {
		floor(h);
		MinecraftServer server = h.getLevel().getServer();
		String guest = GuestRosterTesting.guest("test_c_stuck", false);
		List<Townsfolk> made = new ArrayList<>();
		ServerPlayer player = visitor(h, 2, 2);
		try {
			Townsfolk npc = place(h, guest, 1, 1, made);
			h.assertTrue(GuestOutingTesting.begin(player, npc, guest, "fair", at(h, 7, 6)).done(), "A fair outing begins 7.8 blocks away");
			long now = GuestOutingTesting.now(server);
			GuestOutingTesting.step(server, guest, now + 20);
			h.assertTrue(GuestOutingTesting.tracked(guest), "Still on the way after one step");
			GuestOutingTesting.step(server, guest, now + 20 + GuestOutingTesting.stuckTicks() - 1);
			h.assertTrue(GuestOutingTesting.tracked(guest), "Not stuck before the window is up");
			GuestOutingTesting.step(server, guest, now + 20 + GuestOutingTesting.stuckTicks());
			h.assertTrue(GuestActivity.get(server).current(guest) == null && !GuestOutingTesting.tracked(guest),
					"No progress for the whole window: the outing ends");
			h.assertTrue(npc.anchor().equals(npc.home()), "and the guest heads back to its post");
			h.assertTrue(npc.blockPosition().equals(at(h, 1, 1)), "It was never moved by the outing");
			h.succeed();
		} finally {
			clear(made);
		}
	}

	/**
	 * Home visit spots: refused in someone else's claim and in an operator's protected region (which shares the town's
	 * protection), allowed in the player's own claim, also by {@code /guests visit spot}; a visit then holds the guest at
	 * the spot and ends on request. No block changes.
	 */
	@GameTest(maxTicks = 20)
	public void visitSpotsFollowClaimsAndProtection(GameTestHelper h) throws CommandSyntaxException {
		floor(h);
		ServerLevel level = h.getLevel();
		MinecraftServer server = level.getServer();
		String guest = GuestRosterTesting.guest("test_c_visit", false);
		List<Townsfolk> made = new ArrayList<>();
		ServerPlayer player = visitor(h, 3, 3);
		UUID me = player.getUUID();
		BlockPos spot = at(h, 3, 3);
		long key = LandRegistry.keyOf(spot);
		LandRegistry land = LandRegistry.get(server);
		var book = Ledger.get(server).book();
		CommandDispatcher<CommandSourceStack> dispatcher = server.getCommands().getDispatcher();
		try {
			Townsfolk npc = place(h, guest, 1, 1, made);
			Map<BlockPos, BlockState> before = blocks(h);
			h.assertFalse(LandRegistry.isClaimed(level, spot) || TownProtection.shieldsBlock(level, spot), "Open ground (precondition)");

			// Someone else's claim.
			h.assertTrue(land.assign(book, key, book.account(UUID.randomUUID(), "OutingNeighbour"), 0).ok(), "A stranger's claim");
			try {
				GuestResult claimed = GuestOutingTesting.setSpot(player);
				h.assertFalse(claimed.done(), "Refused in someone else's claim: " + claimed.message().getString());
				h.assertTrue(GuestOutingTesting.spot(server, me) == null, "and no spot is saved");
			} finally {
				land.abandon(player, book, key, true, 0);
			}

			// An operator's protected region (the town's own protection asks the same question).
			String region = "outing_test_" + Integer.toHexString(UUID.randomUUID().hashCode() & 0xFFFFFF);
			CommandSourceStack regionOperator = server.createCommandSourceStack().withEntity(player).withSuppressedOutput();
			move(player, h, 3, 3);
			dispatcher.execute("protect pos1", regionOperator);
			player.snapTo(spot.getX() + 0.5, spot.getY() + 2, spot.getZ() + 0.5, 0, 0);
			dispatcher.execute("protect pos2", regionOperator);
			dispatcher.execute("protect create " + region, regionOperator);
			move(player, h, 3, 3);
			try {
				h.assertTrue(TownProtection.shieldsBlock(level, spot), "The region protects the spot (precondition)");
				GuestResult shielded = GuestOutingTesting.setSpot(player);
				h.assertFalse(shielded.done(), "Refused in a protected region: " + shielded.message().getString());
				h.assertTrue(GuestOutingTesting.spot(server, me) == null, "and no spot is saved");
			} finally {
				dispatcher.execute("protect remove " + region, regionOperator);
			}
			h.assertFalse(TownProtection.shieldsBlock(level, spot), "The region fixture is gone");

			// The player's own claim.
			h.assertTrue(land.assign(book, key, book.account(me, "OutingHost"), 0).ok(), "The player's own claim");
			try {
				int set = dispatcher.execute("guests visit spot", player.createCommandSourceStack().withSuppressedOutput());
				h.assertTrue(set == 1 && spot.equals(GuestOutingTesting.spot(server, me)),
						"/guests visit spot by the player (no operator) saves the spot in their own claim");
				GuestResult again = GuestOutingTesting.setSpot(player);
				h.assertTrue(again.done() && spot.equals(GuestOutingTesting.spot(server, me)), "So does the menu's set_visit_spot");

				GuestFeature outings = GuestOutingTesting.feature();
				h.assertTrue(outings.available(player, npc, guest, "invite_home"), "Inviting home is offered once a spot is set");
				GuestResult visit = outings.act(player, npc, guest, "invite_home");
				h.assertTrue(visit.done(), "The guest is invited home: " + visit.message().getString());
				GuestActivity.Activity held = GuestActivity.get(server).current(guest);
				h.assertTrue(held != null && held.kind().equals(GuestOutings.VISIT) && held.anchor().equals(spot) && npc.anchor().equals(spot),
						"A visit holds the guest at the spot");
				h.assertTrue(outings.act(player, npc, guest, "end_visit").done() && GuestActivity.get(server).current(guest) == null,
						"and ends on request");
			} finally {
				land.abandon(player, book, key, true, 0);
			}
			h.assertTrue(before.equals(blocks(h)), "No block changed");
			h.succeed();
		} finally {
			clear(made);
			GuestOutingTesting.forget(server, me);
		}
	}

	/**
	 * A completed outing adds its affection once, through the shared daily budget; the same kind is refused for the rest
	 * of the day and offered again the next; near the budget's end it adds only what is left.
	 */
	@GameTest(maxTicks = 20)
	public void rewardsAreBoundedByTheSharedBudgetAndOncePerKindADay(GameTestHelper h) {
		floor(h);
		MinecraftServer server = h.getLevel().getServer();
		String guest = GuestRosterTesting.guest("test_c_reward", false);
		List<Townsfolk> made = new ArrayList<>();
		ServerPlayer player = visitor(h, 3, 3);
		UUID me = player.getUUID();
		GuestFavorTesting.pinDay(player, DAY);
		try {
			Townsfolk npc = place(h, guest, 1, 1, made);
			GuestLedger ledger = GuestLedger.get(server);
			GuestFeature outings = GuestOutingTesting.feature();
			int cap = GuestFavors.DAILY_AFFECTION_CAP;
			h.assertTrue(ledger.affection(me, guest) == 0 && GuestRewards.left(player, guest) == cap, "A fresh player and guest (precondition)");

			h.assertTrue(GuestOutingTesting.invite(player, npc, guest, "fair", 3).done(), "A fair outing begins");
			long start = GuestOutingTesting.now(server);
			int fair = GuestOutingTesting.reward("fair");
			GuestOutingTesting.step(server, guest, start + GuestOutingTesting.duration("fair"));
			h.assertTrue(GuestActivity.get(server).current(guest) == null && !GuestOutingTesting.tracked(guest), "At its duration it completes");
			h.assertTrue(ledger.affection(me, guest) == fair && GuestRewards.left(player, guest) == cap - fair,
					"Its affection is added once, from the shared budget: " + ledger.affection(me, guest));
			h.assertTrue(GuestOutingTesting.done(player, guest, "fair"), "The fair is done for today");
			h.assertFalse(outings.available(player, npc, guest, "invite_fair"), "and not offered again today");
			h.assertFalse(GuestOutingTesting.invite(player, npc, guest, "fair", 3).done(), "nor accepted again today");
			h.assertTrue(ledger.affection(me, guest) == fair, "and nothing more was added");

			// Five left of today's budget: a board game adds five, not its whole reward.
			GuestFavorTesting.gainedToday(player, guest, cap - 5);
			h.assertTrue(GuestRewards.left(player, guest) == 5, "Five left today (precondition)");
			h.assertTrue(GuestOutingTesting.invite(player, npc, guest, "board_game", 3).done(), "A board game begins");
			GuestOutingTesting.step(server, guest, GuestOutingTesting.now(server) + GuestOutingTesting.duration("board_game"));
			h.assertTrue(ledger.affection(me, guest) == fair + 5 && GuestRewards.left(player, guest) == 0,
					"Only what was left of the shared budget is added: " + ledger.affection(me, guest));

			// The next day the fair may be had again.
			GuestFavorTesting.pinDay(player, DAY + 1);
			h.assertTrue(outings.available(player, npc, guest, "invite_fair"), "The next day the fair is offered again");
			h.assertTrue(GuestOutingTesting.invite(player, npc, guest, "fair", 3).done(), "and accepted");
			h.succeed();
		} finally {
			clear(made);
			GuestFavorTesting.unpin(me);
		}
	}

	/** No idle farming: staying away for the outing earns nothing (and may be tried again); going too far ends it. */
	@GameTest(maxTicks = 20)
	public void leavingOrIdlingAwayEarnsNothing(GameTestHelper h) {
		floor(h);
		MinecraftServer server = h.getLevel().getServer();
		String guest = GuestRosterTesting.guest("test_c_idle", false);
		List<Townsfolk> made = new ArrayList<>();
		ServerPlayer player = visitor(h, 3, 3);
		UUID me = player.getUUID();
		GuestFavorTesting.pinDay(player, DAY);
		try {
			Townsfolk npc = place(h, guest, 1, 1, made);
			GuestLedger ledger = GuestLedger.get(server);
			h.assertTrue(GuestOutingTesting.invite(player, npc, guest, "fair", 3).done(), "A fair outing begins");
			Vec3 aside = centre(h, 3, 3).add(20, 0, 0);
			player.snapTo(aside.x, aside.y, aside.z, 0, 0);   // beyond together range, short of leaving
			GuestOutingTesting.step(server, guest, GuestOutingTesting.now(server) + GuestOutingTesting.duration("fair"));
			h.assertTrue(GuestActivity.get(server).current(guest) == null && GuestOutingTesting.together(guest) == -1, "It ran its time");
			h.assertTrue(ledger.affection(me, guest) == 0 && !GuestOutingTesting.done(player, guest, "fair"),
					"Away the whole time: no affection, and not counted as done");

			move(player, h, 3, 3);
			h.assertTrue(GuestOutingTesting.invite(player, npc, guest, "fair", 3).done(), "It may be tried again");
			Vec3 gone = centre(h, 3, 3).add(40, 0, 0);
			player.snapTo(gone.x, gone.y, gone.z, 0, 0);
			GuestOutingTesting.step(server, guest, GuestOutingTesting.now(server) + 20);
			h.assertTrue(GuestActivity.get(server).current(guest) == null && ledger.affection(me, guest) == 0,
					"Too far from the guest: the outing ends at once, with nothing earned");
			h.assertTrue(npc.anchor().equals(npc.home()), "and the guest heads back to its post");
			h.succeed();
		} finally {
			clear(made);
			GuestFavorTesting.unpin(me);
		}
	}

	/**
	 * Every entry point checks for itself: a copy of the guest that is not its canonical entity, the wrong guest id, a
	 * pending or unknown guest, a spectator, a player out of range, a guest kept at a fixed post, and another player
	 * ending someone else's outing. Nothing is held by any refusal.
	 */
	@GameTest(maxTicks = 20)
	public void everyEntryPointChecksTheGuestAndThePlayer(GameTestHelper h) {
		floor(h);
		ServerLevel level = h.getLevel();
		MinecraftServer server = level.getServer();
		String guest = GuestRosterTesting.guest("test_c_checks", false);
		String other = GuestRosterTesting.guest("test_c_checks_other", false);
		List<Townsfolk> made = new ArrayList<>();
		ServerPlayer player = visitor(h, 3, 3);
		ServerPlayer stranger = visitor(h, 3, 1);
		GuestActivity activities = GuestActivity.get(server);
		GuestFeature outings = GuestOutingTesting.feature();
		Townsfolk copy = null;
		try {
			Townsfolk npc = place(h, guest, 1, 1, made);
			place(h, other, 6, 6, made);

			copy = CustomNpcs.create(level, centre(h, 2, 5), 0, GuestRoster.get(guest).name(), CustomNpcs.NPC, null, guest);
			h.assertTrue(copy != null && guest.equals(copy.guest()) && !copy.getUUID().equals(GuestLedger.get(server).canonical(guest)),
					"An unbound copy of the guest (precondition)");
			h.assertFalse(outings.available(player, copy, guest, "invite_fair") || outings.act(player, copy, guest, "invite_fair").done(),
					"A copy that is not the canonical entity is neither offered nor accepted");
			h.assertFalse(outings.act(player, copy, guest, "set_visit_spot").done(), "nor sets a visit spot");
			h.assertFalse(outings.act(player, npc, other, "invite_fair").done(), "An invitation is only for the guest standing there");
			h.assertFalse(outings.act(player, npc, "rooni", "invite_fair").done(), "A pending guest is refused");
			h.assertFalse(outings.act(player, npc, "no_such_guest", "invite_fair").done(), "An unknown guest is refused");
			h.assertFalse(outings.act(player, npc, guest, "no_such_option").done(), "An unknown option is refused");

			player.setGameMode(GameType.SPECTATOR);
			h.assertFalse(outings.available(player, npc, guest, "invite_fair") || outings.act(player, npc, guest, "invite_fair").done(),
					"A spectator is refused");
			player.setGameMode(GameType.SURVIVAL);
			move(player, h, 7, 7);
			h.assertFalse(outings.act(player, npc, guest, "invite_fair").done(), "Out of range is refused");
			move(player, h, 3, 3);

			CustomNpcs.setWander(npc, 0);
			h.assertFalse(outings.act(player, npc, guest, "invite_fair").done(), "A guest kept at a fixed post is refused");
			CustomNpcs.setWander(npc, 2);
			h.assertTrue(activities.current(guest) == null && activities.current(other) == null, "No refusal held anything");

			h.assertTrue(GuestOutingTesting.invite(player, npc, guest, "fair", 3).done(), "The real invitation is accepted");
			h.assertFalse(outings.available(stranger, npc, guest, "end_outing") || outings.act(stranger, npc, guest, "end_outing").done(),
					"Another player cannot end it");
			h.assertTrue(activities.current(guest) != null && activities.current(guest).player().equals(player.getUUID()), "It goes on");
			h.succeed();
		} finally {
			if (copy != null && !copy.isRemoved()) {
				copy.discard();
			}
			clear(made);
		}
	}

	// ---------------------------------------------------------------- helpers

	/** A fresh survival player at (x, 2, z) of the test. */
	private static ServerPlayer visitor(GameTestHelper h, int x, int z) {
		ServerPlayer player = TestPlayers.survivor(h);
		move(player, h, x, z);
		return player;
	}

	private static void move(ServerPlayer player, GameTestHelper h, int x, int z) {
		Vec3 at = centre(h, x, z);
		player.snapTo(at.x, at.y, at.z, 90.0F, 0.0F);
	}

	/** The guest {@code guest} at (x, 2, z) of the test, its post there, bound in the ledger as its canonical entity. */
	private static Townsfolk place(GameTestHelper h, String guest, int x, int z, List<Townsfolk> made) {
		GuestRoster.Guest entry = GuestRoster.get(guest);
		Townsfolk npc = CustomNpcs.create(h.getLevel(), centre(h, x, z), 0.0F, entry != null ? entry.name() : guest, CustomNpcs.NPC, null, guest);
		h.assertTrue(npc != null && guest.equals(npc.guest()), "An entity for " + guest + " stands in the test");
		made.add(npc);
		GuestLedger ledger = GuestLedger.get(h.getLevel().getServer());
		boolean bound = ledger.bind(guest, npc.getUUID()) && npc.getUUID().equals(ledger.canonical(guest));
		h.assertTrue(bound, "The test's " + guest + " is its canonical entity");
		return npc;
	}

	/** Discards the test's guests (which ends anything still holding them, and leaves each lost in the ledger). */
	private static void clear(List<Townsfolk> made) {
		for (Townsfolk npc : made) {
			if (!npc.isRemoved()) {
				npc.discard();
			}
		}
		made.clear();
	}

	/** A 2x2 pond of water sources at (5..6, 1, 2..3), stone beneath, in the test's floor. */
	private static void water(GameTestHelper h) {
		for (int x = 5; x <= 6; x++) {
			for (int z = 2; z <= 3; z++) {
				h.setBlock(new BlockPos(x, 0, z), Blocks.STONE);
				h.setBlock(new BlockPos(x, 1, z), Blocks.WATER);
			}
		}
	}

	private static void unwater(GameTestHelper h) {
		for (int x = 5; x <= 6; x++) {
			for (int z = 2; z <= 3; z++) {
				h.setBlock(new BlockPos(x, 1, z), Blocks.STONE);
			}
		}
	}

	/** The absolute block at (x, 2, z) of the test: where a body stands on the floor. */
	private static BlockPos at(GameTestHelper h, int x, int z) {
		return h.absolutePos(new BlockPos(x, 2, z));
	}

	private static Vec3 centre(GameTestHelper h, int x, int z) {
		return Vec3.atBottomCenterOf(at(h, x, z));
	}

	private static Map<BlockPos, BlockState> blocks(GameTestHelper h) {
		Map<BlockPos, BlockState> states = new HashMap<>();
		for (BlockPos pos : BlockPos.betweenClosed(h.absolutePos(BlockPos.ZERO), h.absolutePos(new BlockPos(7, 5, 7)))) {
			states.put(pos.immutable(), h.getLevel().getBlockState(pos));
		}
		return states;
	}

	private static void floor(GameTestHelper h) {
		for (int x = 0; x < 8; x++) {
			for (int z = 0; z < 8; z++) {
				h.setBlock(new BlockPos(x, 1, z), Blocks.STONE);
			}
		}
	}
}

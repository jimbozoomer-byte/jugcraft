package io.github.jimbozoomer.jugcraft.test;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.jimbozoomer.jugcraft.town.CustomNpcs;
import io.github.jimbozoomer.jugcraft.town.JugcraftTown;
import io.github.jimbozoomer.jugcraft.town.Townsfolk;
import io.github.jimbozoomer.jugcraft.town.guests.GuestBalance;
import io.github.jimbozoomer.jugcraft.town.guests.GuestFavors;
import io.github.jimbozoomer.jugcraft.town.guests.GuestLedger;
import io.github.jimbozoomer.jugcraft.town.guests.GuestLedger.Claim;
import io.github.jimbozoomer.jugcraft.town.guests.GuestLedger.Refusal;
import io.github.jimbozoomer.jugcraft.town.guests.GuestLedgerTesting;
import io.github.jimbozoomer.jugcraft.town.guests.GuestMarriage;
import io.github.jimbozoomer.jugcraft.town.guests.GuestMenu;
import io.github.jimbozoomer.jugcraft.town.guests.GuestResult;
import io.github.jimbozoomer.jugcraft.town.guests.GuestRoster;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

/**
 * Town guests (docs/features/jze-town-relationships.md), worker A's tests: the ledger: identity and duplicates, affection, proposal and divorce (two-step), cooldowns across restart and a backward clock, operator repair, save and reload, simultaneous proposals, cross-menu replay.
 * Registered in fabric.mod.json by the shared API; worker A adds the tests.
 *
 * <p>Every test that uses the server's ledger binds only the guest ids reserved for these tests (game tests share one
 * server and run side by side): emiru, lauren_chen, brittany_venti, aoc, sarah_stock, pokimane and power, which may marry;
 * asa_mitaka and rei_ayanami, which may not; pending ids never bind. Each test owns its ids and forgets them first
 * ({@link GuestLedgerTesting#reset}). Tests of the ledger alone use ledgers of their own ({@link GuestLedgerTesting#fresh}),
 * with the wall clock swapped only inside {@link GuestLedgerTesting#withClock}.
 *
 * <p>Only the menu part of {@link #oneMarriagePerGuestAndPerPlayerAndNoReplayThroughTheMenu} reads {@link GuestFavors}
 * (the menu's data sync does): it needs worker B's favors merged and says so if they are still the placeholders.
 */
public class GuestLedgerGameTests {
	private static final int MAX = GuestBalance.MAX_AFFECTION;
	private static final long DAY = 24L * 60 * 60 * 1000;
	private static final long WEEK = GuestBalance.REMARRIAGE_COOLDOWN_MILLIS;
	/** A wall-clock reading for the swapped clock (any positive one does). */
	private static final long T0 = 1_800_000_000_000L;

	// ---------------------------------------------------------------- identity

	@GameTest(maxTicks = 20)
	public void theLedgerKeepsOneIdentityPerGuest(GameTestHelper h) {
		GuestLedger ledger = GuestLedgerTesting.fresh();
		String guest = "power";
		UUID first = UUID.randomUUID(), second = UUID.randomUUID(), third = UUID.randomUUID();
		h.assertTrue(ledger.canonical(guest) == null && ledger.verify(guest, first) == Claim.CANONICAL && first.equals(ledger.canonical(guest)),
				"The first entity to ask binds an unbound guest");
		h.assertTrue(ledger.verify(guest, second) == Claim.DUPLICATE && ledger.verify(guest, first) == Claim.CANONICAL,
				"Any other entity is a duplicate, and the first stays canonical");
		h.assertTrue(!ledger.bind(guest, second) && first.equals(ledger.canonical(guest)), "A bind is refused while the canonical entity is not lost");
		h.assertTrue(ledger.bind(guest, first) && first.equals(ledger.canonical(guest)), "Binding the canonical entity again changes nothing");
		h.assertTrue(ledger.verify("rooni", third) == Claim.UNKNOWN_GUEST && !ledger.bind("rooni", third) && ledger.canonical("rooni") == null,
				"A pending guest is never bound");
		h.assertTrue(ledger.verify("not_a_guest", third) == Claim.UNKNOWN_GUEST && !ledger.bind("not_a_guest", third),
				"Nor is an id that is not in the roster");
		h.assertTrue(!ledger.bind("aoc", first) && ledger.verify("aoc", first) == Claim.DUPLICATE && ledger.canonical("aoc") == null,
				"One entity is never two guests");

		int before = GuestLedgerTesting.changes(ledger);
		ledger.seen(guest, first, Level.OVERWORLD, new BlockPos(1, 64, 1));
		int once = GuestLedgerTesting.changes(ledger);
		ledger.seen(guest, first, Level.OVERWORLD, new BlockPos(1, 64, 1));
		ledger.seen(guest, second, Level.OVERWORLD, new BlockPos(9, 64, 9));
		h.assertTrue(once == before + 1 && GuestLedgerTesting.changes(ledger) == once && new BlockPos(1, 64, 1).equals(ledger.lastSeen(guest).pos()),
				"seen counts a change only when the canonical entity's block changes, and ignores a duplicate");
		ledger.seen(guest, first, Level.NETHER, new BlockPos(1, 64, 1));
		h.assertTrue(GuestLedgerTesting.changes(ledger) == once + 1 && Level.NETHER.equals(ledger.lastSeen(guest).dimension()), "or its dimension");

		// Lost keeps everything; only a bind replaces it, and the entity it replaces is never accepted again.
		UUID fan = UUID.randomUUID(), other = UUID.randomUUID();
		h.assertTrue(ledger.addAffection(fan, guest, MAX) == MAX && ledger.marry(fan, guest) && ledger.addAffection(other, guest, MAX) == MAX,
				"(married, and another player at full affection)");
		ledger.lost(guest, second);
		h.assertTrue(!ledger.missing(guest), "A duplicate's removal frees nothing");
		ledger.lost(guest, first);
		h.assertTrue(ledger.missing(guest) && first.equals(ledger.canonical(guest)) && fan.equals(ledger.spouseOf(guest))
				&& guest.equals(ledger.spouseOf(fan)) && ledger.affection(fan, guest) == MAX && ledger.canMarry(other, guest) == Refusal.GUEST_MARRIED,
				"A lost guest keeps its reservation, its spouse and its affection");
		h.assertTrue(ledger.verify(guest, second) == Claim.DUPLICATE, "Lost, it is still bound: another entity asking is a duplicate");
		h.assertTrue(ledger.bind(guest, second) && second.equals(ledger.canonical(guest)) && !ledger.missing(guest) && fan.equals(ledger.spouseOf(guest)),
				"A bind replaces a lost canonical entity, and the spouse stays the guest's");
		h.assertTrue(ledger.verify(guest, first) == Claim.DUPLICATE && !ledger.bind(guest, first) && !GuestLedgerTesting.forceBind(ledger, guest, first),
				"The replaced entity is never accepted again");

		// Forced replacements are bounded; only a tombstone whose entity was seen destroyed is dropped to make room.
		h.assertTrue(GuestLedgerTesting.forceBind(ledger, guest, third) && third.equals(ledger.canonical(guest))
				&& GuestLedgerTesting.replaced(ledger, guest).equals(List.of(first, second)), "A forced bind replaces a live canonical entity, which is kept");
		int max = GuestLedgerTesting.maxReplaced();
		while (GuestLedgerTesting.replaced(ledger, guest).size() < max) {
			h.assertTrue(GuestLedgerTesting.forceBind(ledger, guest, UUID.randomUUID()), "(filling the tombstones)");
		}
		UUID extra = UUID.randomUUID(), refused = UUID.randomUUID();
		List<UUID> kept = GuestLedgerTesting.replaced(ledger, guest);
		h.assertTrue(GuestLedgerTesting.forceBind(ledger, guest, extra) && !GuestLedgerTesting.replaced(ledger, guest).contains(first)
				&& GuestLedgerTesting.replaced(ledger, guest).contains(second) && GuestLedgerTesting.replaced(ledger, guest).size() == max
				&& kept.contains(first), "At the bound, the one tombstone seen destroyed (the lost entity) makes room");
		h.assertTrue(!GuestLedgerTesting.forceBind(ledger, guest, refused) && extra.equals(ledger.canonical(guest)),
				"With none seen destroyed, a further rebind is refused rather than drop one that may still exist");
		ledger.lost(guest, second);
		h.assertTrue(GuestLedgerTesting.forceBind(ledger, guest, refused) && !GuestLedgerTesting.replaced(ledger, guest).contains(second)
				&& refused.equals(ledger.canonical(guest)), "Once a replaced entity is seen destroyed, its tombstone makes room");

		// Affection, and whether it can be gained.
		UUID friend = UUID.randomUUID();
		h.assertTrue(ledger.canGainAffection(friend, guest) && ledger.addAffection(friend, guest, -50) == 0 && ledger.addAffection(friend, guest, 5000) == MAX
				&& !ledger.canGainAffection(friend, guest) && ledger.addAffection(friend, guest, 1) == MAX,
				"Affection stays within 0 and the maximum, and canGainAffection says when a gain would change it");
		h.assertTrue(!ledger.canGainAffection(friend, "rooni") && ledger.addAffection(friend, "rooni", 10) == 0
				&& !ledger.canGainAffection(friend, "not_a_guest") && ledger.addAffection(friend, "not_a_guest", 10) == 0,
				"A pending or unknown guest gains none");
		h.assertTrue(ledger.canGainAffection(friend, "asa_mitaka") && ledger.addAffection(friend, "asa_mitaka", 10) == 10,
				"A guest without marriage is still befriended");
		GuestLedgerTesting.damage(ledger, "aoc");
		h.assertTrue(!ledger.canGainAffection(friend, "aoc") && ledger.addAffection(friend, "aoc", 10) == 0, "A guest whose entry is damaged gains none");
		GuestLedgerTesting.damage(ledger, friend);
		h.assertTrue(!ledger.canGainAffection(friend, "emiru") && ledger.addAffection(friend, "emiru", 10) == 0,
				"nor does a player whose own entry is damaged");
		h.succeed();
	}

	@GameTest(maxTicks = 200)
	public void duplicatesLeaveAndALostGuestKeepsEverything(GameTestHelper h) {
		floor(h);
		ServerLevel level = h.getLevel();
		MinecraftServer server = level.getServer();
		String twin = "asa_mitaka", lost = "emiru";
		GuestLedgerTesting.reset(server, twin, lost);
		GuestLedger ledger = GuestLedger.get(server);

		// Duplicates: a copy loaded from the canonical NPC's save under a new UUID, and a second placement.
		Townsfolk canonical = placeBound(h, ledger, twin, 1, 1);
		Townsfolk copy = loaded(level, saved(level, canonical));
		copy.setUUID(UUID.randomUUID());
		h.assertTrue(twin.equals(copy.guest()) && level.addFreshEntity(copy), "A copy of the canonical NPC's save loads under a new UUID");
		Townsfolk second = place(h, twin, 1, 4);
		h.assertTrue(ledger.verify(twin, copy.getUUID()) == Claim.DUPLICATE && ledger.verify(twin, second.getUUID()) == Claim.DUPLICATE
				&& ledger.verify(twin, canonical.getUUID()) == Claim.CANONICAL, "Both are duplicates of the canonical NPC");

		// Lost: the canonical NPC killed (and later a replacement removed by /npc remove).
		Townsfolk first = placeBound(h, ledger, lost, 5, 1);
		UUID firstId = first.getUUID();
		CompoundTag firstSave = saved(level, first);   // as its chunk on disk holds it
		UUID fan = UUID.randomUUID(), friend = UUID.randomUUID(), stranger = UUID.randomUUID();
		ledger.addAffection(fan, lost, MAX);
		ledger.addAffection(friend, lost, 250);
		ledger.addAffection(stranger, lost, MAX);
		h.assertTrue(ledger.marry(fan, lost), "(married)");
		first.kill(level);

		Townsfolk[] made = new Townsfolk[2];
		h.runAfterDelay(30, () -> {
			h.assertTrue(copy.isRemoved() && second.isRemoved(), "Both duplicates have left");
			h.assertTrue(!canonical.isRemoved() && canonical.getUUID().equals(ledger.canonical(twin)) && !ledger.missing(twin), "The canonical NPC stays");

			h.assertTrue(first.isRemoved() && ledger.missing(lost) && firstId.equals(ledger.canonical(lost)),
					"Killed, the canonical NPC is lost: the guest is missing and keeps its reservation");
			h.assertTrue(fan.equals(ledger.spouseOf(lost)) && lost.equals(ledger.spouseOf(fan)) && ledger.affection(fan, lost) == MAX
					&& ledger.affection(friend, lost) == 250, "and its spouse and its affection");
			h.assertTrue(ledger.canMarry(stranger, lost) == Refusal.GUEST_MARRIED, "The marriage is the guest's, not the entity's");
			h.assertTrue(ledger.verify(lost, UUID.randomUUID()) == Claim.DUPLICATE, "No other entity takes a lost guest by asking");

			Townsfolk replacement = place(h, lost, 5, 4);
			h.assertTrue(ledger.bind(lost, replacement.getUUID()) && replacement.getUUID().equals(ledger.canonical(lost)) && !ledger.missing(lost)
					&& fan.equals(ledger.spouseOf(lost)), "A placement's bind accepts a new entity; the spouse stays");
			h.assertTrue(ledger.verify(lost, firstId) == Claim.DUPLICATE && !ledger.bind(lost, firstId), "The old UUID is refused");

			server.getCommands().performPrefixedCommand(server.createCommandSourceStack().withSuppressedOutput(), "npc remove " + replacement.custom().id());
			h.assertTrue(replacement.isRemoved() && ledger.missing(lost) && replacement.getUUID().equals(ledger.canonical(lost))
					&& fan.equals(ledger.spouseOf(lost)) && ledger.affection(fan, lost) == MAX && ledger.affection(friend, lost) == 250,
					"/npc remove: the same; lost, with its reservation, spouse and affection");
			Townsfolk third = place(h, lost, 2, 5);
			h.assertTrue(ledger.bind(lost, third.getUUID()) && !ledger.bind(lost, firstId) && !ledger.bind(lost, replacement.getUUID()),
					"A bind replaces it again, and neither replaced UUID is accepted");

			Townsfolk returned = loaded(level, firstSave);
			h.assertTrue(firstId.equals(returned.getUUID()) && lost.equals(returned.guest()) && level.addFreshEntity(returned),
					"The first NPC's old save loads again under its old UUID");
			made[0] = third;
			made[1] = returned;
		});
		h.runAfterDelay(90, () -> {
			Townsfolk third = made[0], returned = made[1];
			h.assertTrue(returned.isRemoved() && !third.isRemoved() && third.getUUID().equals(ledger.canonical(lost)),
					"The old NPC is a duplicate and leaves; the new one stays");
			h.assertTrue(ledger.verify(lost, firstId) == Claim.DUPLICATE && !ledger.bind(lost, firstId) && fan.equals(ledger.spouseOf(lost)),
					"The old UUID stays refused, and the marriage stands");
			remove(server, canonical, third);
			h.succeed();
		});
	}

	// ---------------------------------------------------------------- proposals

	@GameTest(maxTicks = 60)
	public void oneMarriagePerGuestAndPerPlayerAndNoReplayThroughTheMenu(GameTestHelper h) {
		floor(h);
		MinecraftServer server = h.getLevel().getServer();
		String contested = "aoc", other = "brittany_venti";
		GuestLedgerTesting.reset(server, contested, other);
		GuestLedger ledger = GuestLedger.get(server);
		Townsfolk aoc = placeBound(h, ledger, contested, 2, 2);
		Townsfolk brittany = placeBound(h, ledger, other, 5, 5);

		// Two players confirm for one guest in the same tick: exactly one marriage.
		ServerPlayer one = visitor(h, 3, 2, 1), two = visitor(h, 2, 3, 1);
		ledger.addAffection(one.getUUID(), contested, MAX);
		ledger.addAffection(two.getUUID(), contested, MAX);
		h.assertTrue(GuestMarriage.requestProposal(one, aoc, contested).done() && GuestMarriage.requestProposal(two, aoc, contested).done(),
				"Both may ask");
		GuestResult first = GuestMarriage.confirmProposal(one, aoc, contested);
		GuestResult second = GuestMarriage.confirmProposal(two, aoc, contested);
		h.assertTrue(first.done() && !second.done(), "In the same tick the first confirmation marries them and the second is refused");
		h.assertTrue(one.getUUID().equals(ledger.spouseOf(contested)) && contested.equals(ledger.spouseOf(one.getUUID()))
				&& ledger.spouseOf(two.getUUID()) == null, "One spouse for the guest");
		h.assertTrue(diamonds(one) == 0 && diamonds(two) == 1, "Only the marriage made takes a diamond");
		h.assertTrue(one.getUUID().equals(GuestLedgerTesting.forceDivorce(ledger, contested, false)) && ledger.cooldownMillis(contested) == 0,
				"(an operator's divorce without cooldowns frees the guest for the next part)");

		// One player proposes to two guests: one marriage.
		ServerPlayer three = visitor(h, 4, 3, 2);
		ledger.addAffection(three.getUUID(), contested, MAX);
		ledger.addAffection(three.getUUID(), other, MAX);
		h.assertTrue(GuestMarriage.requestProposal(three, aoc, contested).done(), "Asks the first");
		h.assertTrue(!GuestMarriage.requestProposal(three, brittany, other).done() && contested.equals(GuestMarriage.proposalPending(three)),
				"A second guest cannot be asked while a proposal waits");
		h.assertTrue(!GuestMarriage.confirmProposal(three, brittany, other).done() && contested.equals(GuestMarriage.proposalPending(three))
				&& ledger.spouseOf(other) == null, "A confirmation at the other guest is refused; the request stays where it was");
		h.assertTrue(GuestMarriage.confirmProposal(three, aoc, contested).done() && contested.equals(ledger.spouseOf(three.getUUID())),
				"The first is confirmed");
		h.assertTrue(!GuestMarriage.requestProposal(three, brittany, other).done() && !GuestMarriage.mayPropose(three, other)
				&& ledger.canMarry(three.getUUID(), other) == Refusal.PLAYER_MARRIED && ledger.spouseOf(other) == null && diamonds(three) == 1,
				"The second guest is refused: one spouse per player");

		// The same at the ledger itself.
		GuestLedger alone = GuestLedgerTesting.fresh();
		UUID a = UUID.randomUUID(), b = UUID.randomUUID();
		h.assertTrue(alone.bind("pokimane", UUID.randomUUID()) && alone.bind("power", UUID.randomUUID()), "(two guests bound)");
		for (UUID player : List.of(a, b)) {
			alone.addAffection(player, "pokimane", MAX);
			alone.addAffection(player, "power", MAX);
		}
		h.assertTrue(alone.marry(a, "pokimane") && !alone.marry(b, "pokimane") && !alone.marry(a, "power") && alone.marry(b, "power")
				&& b.equals(alone.spouseOf("power")) && "pokimane".equals(alone.spouseOf(a)), "marry commits one spouse per guest and per player");

		// Replay after reopening, through the real menu.
		ServerPlayer four = visitor(h, 5, 4, 1);
		ledger.addAffection(four.getUUID(), other, MAX);
		boolean favors = true;
		try {
			GuestFavors.giftsLeft(four, other);
		} catch (UnsupportedOperationException placeholder) {
			favors = false;
		}
		h.assertTrue(favors, "The menu's data sync reads GuestFavors: this part needs worker B's GuestFavors merged");
		GuestMenu.open(four, brittany, other);
		h.assertTrue(four.containerMenu instanceof GuestMenu, "The guest's menu opens");
		GuestMenu menu = (GuestMenu) four.containerMenu;
		int revision = menu.get(GuestMenu.REVISION);
		int propose = GuestMenu.button(GuestMenu.Action.PROPOSE, revision);
		h.assertTrue(menu.clickMenuButton(four, propose), "PROPOSE carrying the current revision is accepted");
		long expiry = GuestLedgerTesting.proposalExpiry(four);
		h.assertTrue(other.equals(GuestMarriage.proposalPending(four)) && expiry > 0 && diamonds(four) == 1, "Asked; nothing taken");
		h.assertTrue(!menu.clickMenuButton(four, propose) && GuestLedgerTesting.proposalExpiry(four) == expiry,
				"The same id replayed is refused and renews nothing");
		four.closeContainer();
		GuestMenu.open(four, brittany, other);
		h.assertTrue(four.containerMenu instanceof GuestMenu && four.containerMenu != menu, "The menu opens again");
		GuestMenu reopened = (GuestMenu) four.containerMenu;
		h.assertTrue(!reopened.clickMenuButton(four, propose), "The old PROPOSE id replayed after reopening is refused");
		h.assertTrue(!reopened.clickMenuButton(four, GuestMenu.button(GuestMenu.Action.PROPOSE_CONFIRM, revision)),
				"and so is a confirmation carrying the old revision");
		h.assertTrue(GuestLedgerTesting.proposalExpiry(four) == expiry && other.equals(GuestMarriage.proposalPending(four))
				&& ledger.spouseOf(four.getUUID()) == null && diamonds(four) == 1, "Reopening neither confirmed nor renewed the pending proposal");
		h.runAfterDelay(GuestBalance.ACTION_GAP_TICKS + 2, () -> {
			h.assertTrue(four.containerMenu == reopened, "The menu is still open");
			int confirm = GuestMenu.button(GuestMenu.Action.PROPOSE_CONFIRM, reopened.get(GuestMenu.REVISION));
			h.assertTrue(GuestLedgerTesting.proposalExpiry(four) == expiry && reopened.clickMenuButton(four, confirm),
					"The same request, confirmed at the current revision");
			h.assertTrue(other.equals(ledger.spouseOf(four.getUUID())) && diamonds(four) == 0, "marries them and takes one diamond");
			h.assertTrue(!reopened.clickMenuButton(four, confirm) && diamonds(four) == 0, "The confirmation replayed is refused");
			four.closeContainer();
			remove(server, aoc, brittany);
			h.succeed();
		});
	}

	@GameTest(maxTicks = 20)
	public void proposalRulesAndTheDiamond(GameTestHelper h) {
		floor(h);
		MinecraftServer server = h.getLevel().getServer();
		String sarah = "sarah_stock", poki = "pokimane", student = "rei_ayanami", pending = "remielle";
		GuestLedgerTesting.reset(server, sarah, poki);
		GuestLedger ledger = GuestLedger.get(server);
		GuestRoster.Guest studentEntry = GuestRoster.get(student), pendingEntry = GuestRoster.get(pending);
		h.assertTrue(studentEntry != null && studentEntry.available() && !studentEntry.mayMarry() && pendingEntry != null && pendingEntry.pending(),
				"(the roster: a guest without marriage, and a pending one)");
		Townsfolk sarahNpc = placeBound(h, ledger, sarah, 2, 2);
		Townsfolk pokiNpc = placeBound(h, ledger, poki, 6, 6);
		ServerPlayer player = visitor(h, 3, 2, 3);
		UUID me = player.getUUID();
		Vec3 beside = player.position();

		// Full affection is required, and the diamond to ask.
		ledger.addAffection(me, sarah, MAX - 1);
		h.assertTrue(ledger.canMarry(me, sarah) == Refusal.NOT_FULL_AFFECTION && !GuestMarriage.mayPropose(player, sarah)
				&& !GuestMarriage.requestProposal(player, sarahNpc, sarah).done() && GuestMarriage.proposalPending(player) == null,
				"One short of full affection, a proposal is refused");
		h.assertTrue(ledger.canGainAffection(me, sarah) && ledger.addAffection(me, sarah, 1) == MAX && !ledger.canGainAffection(me, sarah),
				"Affection reaches the maximum, and no more can be gained");
		h.assertTrue(ledger.canMarry(me, sarah) == null && GuestMarriage.mayPropose(player, sarah), "At full affection, with a diamond, one may propose");
		player.getInventory().setItem(0, ItemStack.EMPTY);
		h.assertTrue(!GuestMarriage.mayPropose(player, sarah) && !GuestMarriage.requestProposal(player, sarahNpc, sarah).done()
				&& GuestMarriage.proposalPending(player) == null, "Without a diamond nothing is asked");
		player.getInventory().setItem(0, new ItemStack(Items.DIAMOND, 3));

		// A guest without marriage, and a pending guest.
		h.assertTrue(ledger.canGainAffection(me, student) && ledger.addAffection(me, student, MAX) == MAX, "A guest without marriage is befriended");
		h.assertTrue(ledger.canMarry(me, student) == Refusal.NO_MARRIAGE && !GuestMarriage.mayPropose(player, student)
				&& !GuestMarriage.requestProposal(player, sarahNpc, student).done(), "but never proposed to, even at full affection");
		h.assertTrue(!ledger.canGainAffection(me, pending) && ledger.addAffection(me, pending, 100) == 0
				&& ledger.canMarry(me, pending) == Refusal.NO_MARRIAGE && !GuestMarriage.mayPropose(player, pending),
				"A pending guest can be neither befriended nor proposed to");
		h.assertTrue(diamonds(player) == 3 && GuestMarriage.proposalPending(player) == null, "None of that asked or took anything");

		// The entry points check for themselves: out of reach, a spectator, a duplicate NPC, a mismatched guest id.
		ledger.addAffection(me, poki, MAX);
		Townsfolk twin = place(h, sarah, 2, 5);   // a second placement: a duplicate (it leaves by itself a moment later)
		int changes = GuestLedgerTesting.changes(ledger);
		moveTo(player, beside.add(9, 0, 0));
		GuestResult far = GuestMarriage.requestProposal(player, sarahNpc, sarah);
		moveTo(player, beside);
		player.setGameMode(GameType.SPECTATOR);
		GuestResult spectating = GuestMarriage.requestProposal(player, sarahNpc, sarah);
		player.setGameMode(GameType.SURVIVAL);
		GuestResult duplicate = GuestMarriage.requestProposal(player, twin, sarah);
		GuestResult duplicateConfirm = GuestMarriage.confirmProposal(player, twin, sarah);
		GuestResult mismatched = GuestMarriage.requestProposal(player, pokiNpc, sarah);
		GuestResult mismatchedConfirm = GuestMarriage.confirmProposal(player, sarahNpc, poki);
		h.assertTrue(!far.done() && !spectating.done() && !duplicate.done() && !duplicateConfirm.done() && !mismatched.done() && !mismatchedConfirm.done(),
				"Out of reach, as a spectator, at a duplicate NPC or naming another guest: refused");
		h.assertTrue(GuestLedgerTesting.changes(ledger) == changes && GuestMarriage.proposalPending(player) == null && ledger.spouseOf(me) == null
				&& ledger.spouseOf(sarah) == null && diamonds(player) == 3 && ledger.affection(me, sarah) == MAX,
				"and nothing was taken or changed");

		// No request: nothing to confirm.
		h.assertTrue(!GuestMarriage.confirmProposal(player, sarahNpc, sarah).done() && diamonds(player) == 3, "Nothing asked, nothing confirmed");
		// A request takes nothing; a confirmation from out of reach, or at another guest, is refused and keeps the request.
		h.assertTrue(GuestMarriage.requestProposal(player, sarahNpc, sarah).done() && sarah.equals(GuestMarriage.proposalPending(player))
				&& diamonds(player) == 3, "A request takes nothing");
		moveTo(player, beside.add(9, 0, 0));
		h.assertTrue(!GuestMarriage.confirmProposal(player, sarahNpc, sarah).done() && sarah.equals(GuestMarriage.proposalPending(player))
				&& diamonds(player) == 3, "From out of reach the confirmation is refused and the request kept");
		moveTo(player, centre(h, 5, 6));
		h.assertTrue(!GuestMarriage.confirmProposal(player, pokiNpc, poki).done() && sarah.equals(GuestMarriage.proposalPending(player))
				&& ledger.spouseOf(poki) == null && diamonds(player) == 3, "A confirmation for another guest is refused");
		moveTo(player, beside);
		// The diamond gone before confirming: refused, the request used up, nobody married.
		player.getInventory().setItem(0, ItemStack.EMPTY);
		h.assertTrue(!GuestMarriage.confirmProposal(player, sarahNpc, sarah).done() && GuestMarriage.proposalPending(player) == null
				&& ledger.spouseOf(me) == null && ledger.spouseOf(sarah) == null, "Without the diamond at confirmation: refused");
		player.getInventory().setItem(0, new ItemStack(Items.DIAMOND, 3));
		// The guest marries someone else in between: refused, nothing taken.
		h.assertTrue(GuestMarriage.requestProposal(player, sarahNpc, sarah).done(), "Asked again");
		ServerPlayer rival = visitor(h, 2, 3, 1);
		ledger.addAffection(rival.getUUID(), sarah, MAX);
		h.assertTrue(GuestMarriage.requestProposal(rival, sarahNpc, sarah).done() && GuestMarriage.confirmProposal(rival, sarahNpc, sarah).done()
				&& diamonds(rival) == 0, "Another player marries the guest meanwhile");
		h.assertTrue(!GuestMarriage.confirmProposal(player, sarahNpc, sarah).done() && diamonds(player) == 3 && ledger.spouseOf(me) == null
				&& rival.getUUID().equals(ledger.spouseOf(sarah)) && GuestMarriage.proposalPending(player) == null,
				"The first player's confirmation is refused and takes nothing");
		// A confirmed proposal takes exactly one diamond, exactly once.
		moveTo(player, centre(h, 5, 6));
		h.assertTrue(GuestMarriage.requestProposal(player, pokiNpc, poki).done() && GuestMarriage.confirmProposal(player, pokiNpc, poki).done(),
				"A proposal asked and confirmed");
		h.assertTrue(poki.equals(ledger.spouseOf(me)) && me.equals(ledger.spouseOf(poki)) && diamonds(player) == 2, "marries them and takes one diamond");
		h.assertTrue(!GuestMarriage.confirmProposal(player, pokiNpc, poki).done() && diamonds(player) == 2, "Confirming again takes nothing more");
		remove(server, sarahNpc, pokiNpc, twin);
		h.succeed();
	}

	@GameTest(maxTicks = GuestBalance.CONFIRM_TICKS + 100)
	public void lapsedAndRewoundRequestsNeverComeBack(GameTestHelper h) {
		floor(h);
		MinecraftServer server = h.getLevel().getServer();
		String guest = "lauren_chen";
		GuestLedgerTesting.reset(server, guest);
		GuestLedger ledger = GuestLedger.get(server);
		Townsfolk npc = placeBound(h, ledger, guest, 2, 2);
		ServerPlayer player = visitor(h, 3, 2, 1);
		ledger.addAffection(player.getUUID(), guest, MAX);
		long asked = server.overworld().getGameTime();

		// Game time moved back before the asking drops the request.
		h.assertTrue(GuestMarriage.requestProposal(player, npc, guest).done(), "Asked");
		GuestLedgerTesting.withGameTime(s -> asked - 5, () -> {
			h.assertTrue(GuestMarriage.proposalPending(player) == null, "Game time moved back before the asking: the request is gone");
			h.assertTrue(!GuestMarriage.confirmProposal(player, npc, guest).done() && diamonds(player) == 1, "and cannot be confirmed");
		});
		h.assertTrue(GuestLedgerTesting.realTime(), "(the real game time is back)");
		h.assertTrue(GuestMarriage.proposalPending(player) == null, "nor does it come back with the time");
		// A lapse the server tick noted stays a lapse when game time moves back, even unread.
		h.assertTrue(GuestMarriage.requestProposal(player, npc, guest).done(), "Asked again");
		AtomicLong time = new AtomicLong(asked + GuestBalance.CONFIRM_TICKS + 5);
		GuestLedgerTesting.withGameTime(s -> time.get(), () -> {
			GuestLedgerTesting.observe(server);
			time.set(asked + 10);
			h.assertTrue(GuestMarriage.proposalPending(player) == null && !GuestMarriage.confirmProposal(player, npc, guest).done(),
					"A lapsed request does not come back when game time moves back");
		});
		h.assertTrue(GuestLedgerTesting.realTime(), "(the real game time is back)");
		h.assertTrue(ledger.spouseOf(player.getUUID()) == null && diamonds(player) == 1, "Neither was confirmed or took anything");
		// Cancelled.
		h.assertTrue(GuestMarriage.requestProposal(player, npc, guest).done() && GuestMarriage.cancelProposal(player).done()
				&& GuestMarriage.proposalPending(player) == null && !GuestMarriage.confirmProposal(player, npc, guest).done()
				&& !GuestMarriage.cancelProposal(player).done(), "A cancelled proposal cannot be confirmed, and there is nothing left to cancel");
		// Lapsed after CONFIRM_TICKS.
		h.assertTrue(GuestMarriage.requestProposal(player, npc, guest).done(), "Asked once more");
		long expiry = GuestLedgerTesting.proposalExpiry(player);
		h.assertTrue(expiry == server.overworld().getGameTime() + GuestBalance.CONFIRM_TICKS, "It lapses CONFIRM_TICKS later");
		h.runAfterDelay(GuestBalance.CONFIRM_TICKS - 20, () -> h.assertTrue(guest.equals(GuestMarriage.proposalPending(player))
				&& GuestLedgerTesting.proposalExpiry(player) == expiry, "Still waiting shortly before it lapses"));
		h.runAfterDelay(GuestBalance.CONFIRM_TICKS + 2, () -> {
			h.assertTrue(GuestMarriage.proposalPending(player) == null, "Lapsed");
			h.assertTrue(!GuestMarriage.confirmProposal(player, npc, guest).done() && diamonds(player) == 1 && ledger.spouseOf(player.getUUID()) == null,
					"A lapsed proposal cannot be confirmed and costs nothing");
			remove(server, npc);
			h.succeed();
		});
	}

	// ---------------------------------------------------------------- divorce, cooldowns and repair

	@GameTest(maxTicks = 100)
	public void divorceCooldownsAndOperatorRepair(GameTestHelper h) throws CommandSyntaxException {
		floor(h);
		ServerLevel level = h.getLevel();
		MinecraftServer server = level.getServer();
		String guest = "power", rebound = "rei_ayanami";
		GuestLedgerTesting.reset(server, guest, rebound);
		GuestLedger ledger = GuestLedger.get(server);
		Townsfolk npc = placeBound(h, ledger, guest, 1, 1);
		ServerPlayer player = visitor(h, 2, 1, 2);
		UUID me = player.getUUID();
		ledger.addAffection(me, guest, MAX);
		h.assertTrue(GuestMarriage.requestProposal(player, npc, guest).done() && GuestMarriage.confirmProposal(player, npc, guest).done()
				&& guest.equals(ledger.spouseOf(me)), "(married)");

		// Divorce in two steps.
		h.assertTrue(!GuestMarriage.requestDivorce(player, "aoc").done() && !GuestMarriage.divorcePending(player), "Only from one's own spouse");
		h.assertTrue(GuestMarriage.requestDivorce(player, guest).done() && GuestMarriage.divorcePending(player) && guest.equals(ledger.spouseOf(me)),
				"Asked: still married");
		h.assertTrue(GuestMarriage.cancelDivorce(player).done() && !GuestMarriage.divorcePending(player)
				&& !GuestMarriage.confirmDivorce(player, guest).done() && guest.equals(ledger.spouseOf(me)), "Cancelled: nothing to confirm, still married");
		long asked = server.overworld().getGameTime();
		h.assertTrue(GuestMarriage.requestDivorce(player, guest).done(), "Asked again");
		GuestLedgerTesting.withGameTime(s -> asked - 5, () -> h.assertTrue(!GuestMarriage.divorcePending(player)
				&& !GuestMarriage.confirmDivorce(player, guest).done() && guest.equals(ledger.spouseOf(me)), "Game time moved back drops a divorce request"));
		h.assertTrue(GuestLedgerTesting.realTime(), "(the real game time is back)");
		h.assertTrue(!GuestMarriage.divorcePending(player), "and it does not come back");
		h.assertTrue(GuestMarriage.requestDivorce(player, guest).done() && GuestMarriage.confirmDivorce(player, guest).done(), "Asked and confirmed");
		h.assertTrue(ledger.spouseOf(me) == null && ledger.spouseOf(guest) == null, "Divorced: both free");
		long mine = ledger.cooldownMillis(me), theirs = ledger.cooldownMillis(guest);
		h.assertTrue(mine > WEEK - 60_000 && mine <= WEEK && theirs > WEEK - 60_000 && theirs <= WEEK,
				"Each has a cooldown of seven real days: " + mine + ", " + theirs);
		ServerPlayer other = visitor(h, 1, 2, 1);
		ledger.addAffection(other.getUUID(), guest, MAX);
		h.assertTrue(ledger.canMarry(me, guest) == Refusal.PLAYER_COOLDOWN && ledger.canMarry(other.getUUID(), guest) == Refusal.GUEST_COOLDOWN,
				"Neither may marry meanwhile");
		h.assertTrue(!GuestMarriage.requestProposal(player, npc, guest).done() && !GuestMarriage.requestProposal(other, npc, guest).done()
				&& diamonds(player) == 1 && diamonds(other) == 1, "nor even ask");

		// Operator repair: a non-operator is refused everywhere; each repair works.
		CommandDispatcher<CommandSourceStack> dispatcher = server.getCommands().getDispatcher();
		CommandSourceStack stranger = player.createCommandSourceStack().withSuppressedOutput();
		CommandSourceStack operator = server.createCommandSourceStack().withSuppressedOutput().withLevel(level).withPosition(centre(h, 6, 6))
				.withRotation(new Vec2(0, 0));
		String fan = me.toString();
		h.assertFalse(CustomNpcs.isOperator(stranger), "(the survivor is no operator)");
		h.assertTrue(!parses(dispatcher, "guests repair cooldown clear player " + fan, stranger) && !parses(dispatcher, "guests state guest " + guest, stranger),
				"A non-operator cannot run /guests at all");
		h.assertTrue(GuestLedgerTesting.repairPlayerCooldown(stranger, fan) == 0 && GuestLedgerTesting.repairGuestCooldown(stranger, guest) == 0
				&& GuestLedgerTesting.repairAffection(stranger, fan, guest, 0) == 0 && GuestLedgerTesting.repairDivorce(stranger, guest, false) == 0
				&& GuestLedgerTesting.repairRebind(stranger, rebound, true) == 0 && GuestLedgerTesting.guestState(stranger, guest) == 0
				&& GuestLedgerTesting.playerState(stranger, fan) == 0, "and every command refuses a non-operator by itself");
		h.assertTrue(ledger.cooldownMillis(me) > 0 && ledger.cooldownMillis(guest) > 0 && ledger.affection(me, guest) == MAX, "so nothing changed");
		h.assertTrue(dispatcher.execute("guests state guest " + guest, operator) == 1 && dispatcher.execute("guests state player " + fan, operator) == 1,
				"An operator reads a guest's and a player's state");
		h.assertTrue(dispatcher.execute("guests repair cooldown clear player " + fan, operator) == 1 && ledger.cooldownMillis(me) == 0
				&& ledger.cooldownMillis(guest) > 0, "clears a player's cooldown");
		h.assertTrue(dispatcher.execute("guests repair cooldown clear guest " + guest, operator) == 1 && ledger.cooldownMillis(guest) == 0
				&& ledger.canMarry(me, guest) == null, "and a guest's");
		h.assertTrue(dispatcher.execute("guests repair affection " + fan + " " + guest + " 400", operator) == 1 && ledger.affection(me, guest) == 400,
				"sets affection");
		h.assertTrue(!parses(dispatcher, "guests repair affection " + fan + " " + guest + " 1001", operator) && ledger.affection(me, guest) == 400,
				"only within 0 to 1000");
		h.assertTrue(dispatcher.execute("guests repair affection " + fan + " " + guest + " 1000", operator) == 1 && ledger.marry(me, guest),
				"(married again)");
		h.assertTrue(dispatcher.execute("guests repair divorce " + guest + " false", operator) == 1 && ledger.spouseOf(me) == null
				&& ledger.cooldownMillis(me) == 0 && ledger.cooldownMillis(guest) == 0, "forces a divorce without cooldowns");
		h.assertTrue(ledger.marry(me, guest) && dispatcher.execute("guests repair divorce " + guest, operator) == 1 && ledger.spouseOf(guest) == null
				&& ledger.cooldownMillis(me) > WEEK - 60_000 && ledger.cooldownMillis(guest) > WEEK - 60_000, "or, by default, with both cooldowns");
		h.assertTrue(dispatcher.execute("guests repair divorce " + guest, operator) == 0, "Nothing to divorce: refused");

		// Damaged entries, through the seams the combined regression uses.
		UUID damaged = UUID.randomUUID();
		GuestLedgerTesting.damagePlayer(server, damaged);
		h.assertTrue(!ledger.canGainAffection(damaged, guest) && ledger.addAffection(damaged, guest, 10) == 0
				&& ledger.canMarry(damaged, guest) == Refusal.PLAYER_COOLDOWN, "A damaged player entry: no affection, no marriage");
		h.assertTrue(dispatcher.execute("guests repair affection " + damaged + " " + guest + " 500", operator) == 0 && ledger.affection(damaged, guest) == 0,
				"and the repair command will not write over it");
		h.assertTrue(dispatcher.execute("guests repair cooldown clear guest " + guest, operator) == 1 && ledger.marry(other.getUUID(), guest),
				"(the guest married to another player)");
		GuestLedgerTesting.damageGuest(server, guest);
		h.assertTrue(ledger.canMarry(me, guest) == Refusal.NO_MARRIAGE && !ledger.canGainAffection(me, guest)
				&& ledger.verify(guest, npc.getUUID()) == Claim.UNKNOWN_GUEST && ledger.canMarry(other.getUUID(), "aoc") == Refusal.PLAYER_MARRIED,
				"A damaged guest entry: frozen, and the spouse it names counts as married");
		GuestLedgerTesting.reset(server, guest);
		h.assertTrue(ledger.canMarry(other.getUUID(), "aoc") == Refusal.NOT_FULL_AFFECTION, "(reset undoes the damage)");

		// Rebind: refused while the canonical NPC lives unless forced; a lost guest is rebound without force.
		Townsfolk held = placeBound(h, ledger, rebound, 1, 6);
		Townsfolk stand = place(h, rebound, 6, 5);   // beside the operator at (6, 6)
		h.assertTrue(dispatcher.execute("guests repair rebind " + rebound, operator) == 0 && held.getUUID().equals(ledger.canonical(rebound)),
				"Not lost: a rebind is refused without force");
		h.assertTrue(dispatcher.execute("guests repair rebind " + rebound + " force", operator) == 1 && stand.getUUID().equals(ledger.canonical(rebound))
				&& GuestLedgerTesting.replaced(ledger, rebound).contains(held.getUUID()), "With force the NPC beside the operator becomes the guest");
		h.runAfterDelay(40, () -> {
			h.assertTrue(held.isRemoved() && !ledger.bind(rebound, held.getUUID()), "The replaced NPC leaves as a duplicate and is never accepted again");
			CustomNpcs.remove(server, stand.custom().id());
			h.assertTrue(ledger.missing(rebound), "The new one removed: lost");
			Townsfolk fresh = place(h, rebound, 5, 6);
			h.assertTrue(GuestLedgerTesting.repairRebind(operator, rebound, false) == 1 && fresh.getUUID().equals(ledger.canonical(rebound))
					&& !ledger.missing(rebound), "A lost guest is rebound without force");
			h.assertTrue(GuestLedgerTesting.repairRebind(operator, rebound, false) == 0, "Nothing lost now: a second rebind is refused");
			remove(server, npc, fresh);
			h.succeed();
		});
	}

	@GameTest(maxTicks = 20)
	public void cooldownsCountRealTimeOnlyOnce(GameTestHelper h) {
		AtomicLong clock = new AtomicLong(T0);
		GuestLedgerTesting.withClock(clock::get, () -> {
			GuestLedger ledger = GuestLedgerTesting.fresh();
			String guest = "power";
			UUID player = UUID.randomUUID();
			h.assertTrue(ledger.bind(guest, UUID.randomUUID()) && ledger.addAffection(player, guest, MAX) == MAX && ledger.marry(player, guest)
					&& guest.equals(ledger.divorce(player)), "(married and divorced at T0)");
			h.assertTrue(left(ledger, player, guest) == WEEK, "Seven real days each");
			clock.set(T0 + DAY);
			h.assertTrue(left(ledger, player, guest) == WEEK - DAY, "A day later, six left");
			clock.set(T0 - 2 * DAY);
			h.assertTrue(left(ledger, player, guest) == WEEK - DAY, "The clock moved back: nothing taken off");
			clock.set(T0 + DAY);
			h.assertTrue(left(ledger, player, guest) == WEEK - DAY, "and restored to where it was: nothing credited twice");
			clock.set(T0 + 2 * DAY);
			h.assertTrue(left(ledger, player, guest) == WEEK - 2 * DAY, "Time beyond the highest reading counts, once");
			clock.set(T0);
			h.assertTrue(left(ledger, player, guest) == WEEK - 2 * DAY, "Moved back again: nothing");
			GuestLedger reloaded = GuestLedgerTesting.load(GuestLedgerTesting.save(ledger));
			h.assertTrue(left(reloaded, player, guest) == WEEK - 2 * DAY, "Saved and loaded while the clock is behind: the same");
			clock.set(T0 + 2 * DAY);
			h.assertTrue(left(reloaded, player, guest) == WEEK - 2 * DAY, "and restored after the reload: nothing credited twice");
			Tag saved = GuestLedgerTesting.save(reloaded);
			clock.set(T0 + 3 * DAY);   // a day of downtime
			GuestLedger restarted = GuestLedgerTesting.load(saved);
			h.assertTrue(left(restarted, player, guest) == WEEK - 3 * DAY, "Downtime after the save is credited from the saved high-water mark");
			h.assertTrue(restarted.canMarry(player, guest) == Refusal.PLAYER_COOLDOWN, "and the cooldown still holds");
			clock.set(T0 + WEEK + DAY);
			h.assertTrue(restarted.cooldownMillis(player) == 0 && restarted.cooldownMillis(guest) == 0 && restarted.canMarry(player, guest) == null
					&& restarted.marry(player, guest), "Forward past the deadline frees them both");
		});
		h.assertTrue(GuestLedgerTesting.realTime(), "(the real clock is back)");
		h.succeed();
	}

	// ---------------------------------------------------------------- saving

	@GameTest(maxTicks = 20)
	public void theLedgerSavesAndLoadsEverything(GameTestHelper h) {
		AtomicLong clock = new AtomicLong(T0);
		GuestLedgerTesting.withClock(clock::get, () -> {
			GuestLedger ledger = GuestLedgerTesting.fresh();
			UUID powerNpc = UUID.randomUUID(), aocNpc = UUID.randomUUID(), emiruOld = UUID.randomUUID(), emiruNpc = UUID.randomUUID();
			UUID wed = UUID.randomUUID(), fond = UUID.randomUUID(), parted = UUID.randomUUID();
			h.assertTrue(ledger.bind("power", powerNpc) && ledger.bind("aoc", aocNpc) && ledger.bind("emiru", emiruOld)
					&& GuestLedgerTesting.forceBind(ledger, "emiru", emiruNpc), "(bound, one replaced)");
			ledger.seen("power", powerNpc, Level.OVERWORLD, new BlockPos(10, 64, -3));
			ledger.seen("aoc", aocNpc, Level.NETHER, new BlockPos(-7, 40, 2));
			ledger.lost("aoc", aocNpc);
			ledger.addAffection(wed, "power", MAX);
			ledger.addAffection(wed, "aoc", 300);
			ledger.addAffection(fond, "power", 999);
			ledger.addAffection(fond, "asa_mitaka", 120);
			ledger.addAffection(parted, "emiru", MAX);
			h.assertTrue(ledger.marry(wed, "power") && ledger.marry(parted, "emiru") && "emiru".equals(ledger.divorce(parted)), "(one married, one divorced)");
			clock.set(T0 + DAY);
			h.assertTrue(ledger.cooldownMillis(parted) == WEEK - DAY, "(a day into the cooldown)");

			Tag saved = GuestLedgerTesting.save(ledger);
			GuestLedger loaded = GuestLedgerTesting.load(saved);
			h.assertTrue(powerNpc.equals(loaded.canonical("power")) && !loaded.missing("power")
					&& new GuestLedger.Place(Level.OVERWORLD, new BlockPos(10, 64, -3)).equals(loaded.lastSeen("power")), "Identity and last place come back");
			h.assertTrue(aocNpc.equals(loaded.canonical("aoc")) && loaded.missing("aoc") && Level.NETHER.equals(loaded.lastSeen("aoc").dimension()),
					"A lost guest stays lost, last seen in its dimension");
			h.assertTrue(emiruNpc.equals(loaded.canonical("emiru")) && GuestLedgerTesting.replaced(loaded, "emiru").equals(List.of(emiruOld))
					&& loaded.verify("emiru", emiruOld) == Claim.DUPLICATE && !loaded.bind("emiru", emiruOld), "A replaced entity stays refused");
			h.assertTrue(loaded.affection(wed, "power") == MAX && loaded.affection(wed, "aoc") == 300 && loaded.affection(fond, "power") == 999
					&& loaded.affection(fond, "asa_mitaka") == 120 && loaded.affection(parted, "emiru") == MAX, "Affection comes back");
			h.assertTrue("power".equals(loaded.spouseOf(wed)) && wed.equals(loaded.spouseOf("power")) && loaded.spouseOf(parted) == null
					&& loaded.spouseOf("emiru") == null, "Marriages come back, both ways");
			h.assertTrue(loaded.cooldownMillis(parted) == WEEK - DAY && loaded.cooldownMillis("emiru") == WEEK - DAY, "Cooldowns come back");
			h.assertTrue(loaded.canMarry(fond, "power") == Refusal.NOT_FULL_AFFECTION && loaded.canMarry(parted, "emiru") == Refusal.PLAYER_COOLDOWN,
					"and the rules hold on the loaded ledger");
			h.assertTrue(GuestLedgerTesting.save(loaded).equals(saved), "Saving the loaded ledger writes exactly what was read");
		});
		h.assertTrue(GuestLedgerTesting.realTime(), "(the real clock is back)");
		h.succeed();
	}

	@GameTest(maxTicks = 20)
	public void unreadableEntriesRefuseOnlyTheirParties(GameTestHelper h) {
		AtomicLong clock = new AtomicLong(T0);
		GuestLedgerTesting.withClock(clock::get, () -> {
			GuestLedger ledger = GuestLedgerTesting.fresh();
			for (String guest : List.of("power", "aoc", "emiru", "pokimane")) {
				h.assertTrue(ledger.bind(guest, UUID.randomUUID()), "(" + guest + " bound)");
			}
			// kept: married to power, whose entry stays readable. named: married to aoc, whose entry will be damaged with its
			// spouse still legible. unread: affection and a cooldown in an entry that will be damaged. free: concerned by nothing.
			UUID kept = UUID.randomUUID(), named = UUID.randomUUID(), unread = UUID.randomUUID(), free = UUID.randomUUID();
			ledger.addAffection(kept, "power", MAX);
			ledger.addAffection(named, "aoc", MAX);
			ledger.addAffection(named, "emiru", MAX);
			ledger.addAffection(unread, "pokimane", MAX);
			ledger.addAffection(unread, "emiru", MAX);
			ledger.addAffection(free, "emiru", MAX);
			ledger.addAffection(free, "aoc", MAX);
			h.assertTrue(ledger.marry(kept, "power") && ledger.marry(named, "aoc") && ledger.marry(unread, "pokimane") && ledger.divorce(unread) != null,
					"(the marriages and the cooldown)");

			JsonElement json = GuestLedgerTesting.saveJson(ledger);
			JsonObject data = json.getAsJsonObject().getAsJsonObject("data");
			JsonObject aoc = data.getAsJsonObject("guests").getAsJsonObject("aoc");
			aoc.addProperty("cooldown_millis", "not a number");
			data.getAsJsonObject("players").add(unread.toString(), new JsonPrimitive("damaged"));
			JsonElement damagedAoc = aoc.deepCopy();
			GuestLedger loaded = GuestLedgerTesting.loadJson(json);

			h.assertTrue(GuestLedgerTesting.unreadable(loaded, "aoc") && GuestLedgerTesting.unreadable(loaded, unread)
					&& !GuestLedgerTesting.unreadable(loaded, "power") && !GuestLedgerTesting.unreadable(loaded, kept), "Exactly the two damaged entries are unreadable");
			h.assertTrue(loaded.canMarry(free, "aoc") == Refusal.NO_MARRIAGE && !loaded.marry(free, "aoc") && loaded.spouseOf("aoc") == null
					&& loaded.verify("aoc", UUID.randomUUID()) == Claim.UNKNOWN_GUEST && !loaded.bind("aoc", UUID.randomUUID()),
					"The damaged guest is frozen: no second spouse, no new identity");
			h.assertTrue(loaded.canMarry(named, "emiru") == Refusal.PLAYER_MARRIED && !loaded.marry(named, "emiru") && GuestLedgerTesting.damaged(loaded, named),
					"Its spouse, named in the damaged entry, counts as married");
			h.assertTrue(loaded.canMarry(unread, "emiru") == Refusal.PLAYER_COOLDOWN && !loaded.marry(unread, "emiru"),
					"A player whose own entry is unreadable is refused marriage");
			h.assertTrue(!loaded.canGainAffection(unread, "emiru") && loaded.addAffection(unread, "emiru", 10) == 0
					&& GuestLedgerTesting.setAffection(loaded, unread, "emiru", 500) == 0 && !loaded.canGainAffection(free, "aoc")
					&& loaded.addAffection(free, "aoc", -10) == 0, "and gains no affection; nor does anyone with the frozen guest");
			h.assertTrue("power".equals(loaded.spouseOf(kept)) && loaded.canMarry(free, "emiru") == null && loaded.marry(free, "emiru"),
					"Everyone else is unaffected");
			JsonObject again = GuestLedgerTesting.saveJson(loaded).getAsJsonObject().getAsJsonObject("data");
			h.assertTrue(damagedAoc.equals(again.getAsJsonObject("guests").get("aoc"))
					&& new JsonPrimitive("damaged").equals(again.getAsJsonObject("players").get(unread.toString())),
					"Both damaged entries are written back exactly as they were read");

			// Damage that names nobody who can be told refuses every marriage.
			JsonElement blind = GuestLedgerTesting.saveJson(ledger);
			blind.getAsJsonObject().getAsJsonObject("data").getAsJsonObject("guests").getAsJsonObject("aoc").addProperty("spouse", 12);
			GuestLedger unknownSpouse = GuestLedgerTesting.loadJson(blind);
			h.assertTrue(unknownSpouse.canMarry(free, "emiru") == Refusal.PLAYER_MARRIED && !unknownSpouse.marry(free, "emiru"),
					"A damaged entry whose spouse cannot be read refuses every marriage");
			JsonElement nameless = GuestLedgerTesting.saveJson(ledger);
			nameless.getAsJsonObject().getAsJsonObject("data").getAsJsonObject("players").add("not-a-player", new JsonObject());
			GuestLedger unknownPlayer = GuestLedgerTesting.loadJson(nameless);
			h.assertTrue(unknownPlayer.canMarry(free, "emiru") == Refusal.PLAYER_COOLDOWN && !unknownPlayer.marry(free, "emiru"),
					"and so does a damaged player entry that names nobody");
			UUID newcomer = UUID.randomUUID();
			h.assertTrue(!unknownPlayer.canGainAffection(free, "pokimane") && unknownPlayer.addAffection(free, "pokimane", 10) == 0
					&& GuestLedgerTesting.setAffection(unknownPlayer, free, "pokimane", 500) == 0 && unknownPlayer.affection(free, "pokimane") == 0
					&& unknownPlayer.addAffection(free, "emiru", -100) == MAX && unknownPlayer.affection(free, "emiru") == MAX,
					"An unreadable player entry that names nobody might be anyone's: no player's affection changes");
			h.assertTrue(!unknownPlayer.canGainAffection(newcomer, "emiru") && unknownPlayer.addAffection(newcomer, "emiru", 10) == 0
					&& GuestLedgerTesting.setAffection(unknownPlayer, newcomer, "emiru", 10) == 0 && unknownPlayer.affection(newcomer, "emiru") == 0,
					"not even a newcomer's");
			h.assertTrue(new JsonObject().equals(GuestLedgerTesting.saveJson(unknownPlayer).getAsJsonObject().getAsJsonObject("data")
					.getAsJsonObject("players").get("not-a-player")), "and the nameless entry is written back as it was read");

			// The test seams damage a ledger in the same way, and the damage reads back from a save as damage.
			GuestLedgerTesting.damage(ledger, "aoc");
			GuestLedgerTesting.damage(ledger, unread);
			h.assertTrue(ledger.canMarry(free, "aoc") == Refusal.NO_MARRIAGE && ledger.canMarry(named, "emiru") == Refusal.PLAYER_MARRIED
					&& ledger.canMarry(unread, "emiru") == Refusal.PLAYER_COOLDOWN && !ledger.canGainAffection(unread, "emiru")
					&& ledger.canMarry(free, "emiru") == null, "The seams leave the same state as a damaged file");
			GuestLedger reread = GuestLedgerTesting.load(GuestLedgerTesting.save(ledger));
			h.assertTrue(GuestLedgerTesting.unreadable(reread, "aoc") && GuestLedgerTesting.unreadable(reread, unread)
					&& reread.canMarry(named, "emiru") == Refusal.PLAYER_MARRIED && reread.canMarry(free, "emiru") == null,
					"and it stays damaged across a save and load");
		});
		h.assertTrue(GuestLedgerTesting.realTime(), "(the real clock is back)");
		h.succeed();
	}

	// ---------------------------------------------------------------- helpers

	private static void floor(GameTestHelper h) {
		for (int x = 0; x < 8; x++) {
			for (int z = 0; z < 8; z++) {
				h.setBlock(new BlockPos(x, 1, z), Blocks.STONE);
			}
		}
	}

	private static Vec3 centre(GameTestHelper h, int x, int z) {
		BlockPos at = h.absolutePos(new BlockPos(x, 2, z));
		return new Vec3(at.getX() + 0.5, at.getY(), at.getZ() + 0.5);
	}

	/** A guest NPC placed as a placement makes it (not yet bound). */
	private static Townsfolk place(GameTestHelper h, String guest, int x, int z) {
		GuestRoster.Guest entry = GuestRoster.get(guest);
		Townsfolk npc = CustomNpcs.create(h.getLevel(), centre(h, x, z), 0, entry == null ? guest : entry.name(), CustomNpcs.NPC, null, guest);
		h.assertTrue(npc != null && guest.equals(npc.guest()), "A guest NPC for " + guest + " stands at " + x + ", " + z);
		return npc;
	}

	private static Townsfolk placeBound(GameTestHelper h, GuestLedger ledger, String guest, int x, int z) {
		Townsfolk npc = place(h, guest, x, z);
		h.assertTrue(ledger.bind(guest, npc.getUUID()), guest + " is bound to its placed NPC");
		return npc;
	}

	/** A survival player standing at (x, 2, z) of the test with this many diamonds. */
	private static ServerPlayer visitor(GameTestHelper h, int x, int z, int diamonds) {
		ServerPlayer player = TestPlayers.survivor(h);
		moveTo(player, centre(h, x, z));
		if (diamonds > 0) {
			player.getInventory().setItem(0, new ItemStack(Items.DIAMOND, diamonds));
		}
		return player;
	}

	private static void moveTo(ServerPlayer player, Vec3 at) {
		player.snapTo(at.x, at.y, at.z, 0, 0);
	}

	private static int diamonds(ServerPlayer player) {
		return player.getInventory().countItem(Items.DIAMOND);
	}

	private static CompoundTag saved(ServerLevel level, Townsfolk npc) {
		TagValueOutput out = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
		npc.saveWithoutId(out);
		return out.buildResult();
	}

	private static Townsfolk loaded(ServerLevel level, CompoundTag save) {
		Townsfolk npc = JugcraftTown.TOWNSFOLK.create(level, EntitySpawnReason.LOAD);
		npc.load(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), save));
		return npc;
	}

	/** Both cooldowns' milliseconds when they agree, else -1. */
	private static long left(GuestLedger ledger, UUID player, String guest) {
		long mine = ledger.cooldownMillis(player), theirs = ledger.cooldownMillis(guest);
		return mine == theirs ? mine : -1;
	}

	private static boolean parses(CommandDispatcher<CommandSourceStack> dispatcher, String command, CommandSourceStack source) {
		try {
			dispatcher.execute(command, source);
			return true;
		} catch (CommandSyntaxException refused) {
			return false;
		}
	}

	private static void remove(MinecraftServer server, Townsfolk... npcs) {
		for (Townsfolk npc : npcs) {
			if (npc != null && !npc.isRemoved() && npc.custom() != null) {
				CustomNpcs.remove(server, npc.custom().id());
			}
		}
	}
}

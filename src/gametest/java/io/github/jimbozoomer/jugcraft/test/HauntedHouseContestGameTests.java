package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.agriculture.HalloweenSeason;
import io.github.jimbozoomer.jugcraft.agriculture.HarvestScaleBlockEntity;
import io.github.jimbozoomer.jugcraft.agriculture.HauntedHouseContest;
import io.github.jimbozoomer.jugcraft.agriculture.JugcraftAgriculture;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/**
 * The Haunted House contest (docs/features/haunted-house-contest.md). A house is scored high above the test's own
 * area, where no other test builds, and every block placed there is cleared before the method returns. The season is
 * switched on only inside a test and switched back before it returns. Prizes are tested on a contest of a long-past
 * year, so this Halloween's is left alone.
 */
public final class HauntedHouseContestGameTests {
	/** How high above their areas the tests build, each at its own height so neither counts the other's props. */
	private static final int ALOFT = 60;
	private static final int ALOFT_JUDGED = 90;

	/** Builds props round {@code door} (relative, high up): six cobwebs, two carved pumpkins, a jack o'lantern; returns them. */
	private static List<BlockPos> build(GameTestHelper helper, BlockPos door) {
		ServerLevel level = helper.getLevel();
		List<BlockPos> placed = new ArrayList<>();
		for (int i = 0; i < 6; i++) {
			placed.add(place(level, door.offset(i - 3, 2, 2), Blocks.COBWEB));
		}
		placed.add(place(level, door.offset(-2, 0, -1), Blocks.CARVED_PUMPKIN));
		placed.add(place(level, door.offset(2, 0, -1), Blocks.CARVED_PUMPKIN));
		placed.add(place(level, door.offset(0, 3, 0), Blocks.JACK_O_LANTERN));
		placed.add(place(level, door.offset(0, -1, 0), Blocks.STONE)); // Not a prop.
		return placed;
	}

	private static BlockPos place(ServerLevel level, BlockPos at, Block block) {
		level.setBlock(at, block.defaultBlockState(), Block.UPDATE_CLIENTS);
		return at;
	}

	private static void clear(ServerLevel level, List<BlockPos> placed) {
		for (BlockPos at : placed) {
			level.setBlock(at, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
		}
	}

	/** A point a prop, at most four of a kind, and two more a kind; plain blocks count for nothing. */
	@GameTest
	public void theChildrenCountThePropsByTheDoor(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos door = helper.absolutePos(new BlockPos(4, 2 + ALOFT, 4));
		HauntedHouseContest.Score bare = HauntedHouseContest.score(level, door);
		helper.assertTrue(bare.points() == 0, "Nothing up here to start with: " + bare);
		List<BlockPos> placed = build(helper, door);
		try {
			HauntedHouseContest.Score score = HauntedHouseContest.score(level, door);
			// Cobwebs 6 (counted 4) + carved pumpkins 2 + a jack o'lantern 1 = 7 props of 3 kinds: 7 + 3 x 2 = 13.
			helper.assertTrue(score.props() == 7 && score.kinds() == 3 && score.points() == 13, "Scored " + score);
			placed.add(place(level, door.offset(HauntedHouseContest.REACH + 1, 0, 0), Blocks.SKELETON_SKULL));
			helper.assertTrue(HauntedHouseContest.score(level, door).equals(score), "A prop out of reach doesn't count");
		} finally {
			clear(level, placed);
		}
		helper.succeed();
	}

	/** Judging keeps an owner's best of the season, and only an owner on the server, in the season, is judged. */
	@GameTest
	public void anOwnersBestScoreIsTheirEntry(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer owner = helper.makeMockServerPlayerInLevel();
		BlockPos door = helper.absolutePos(new BlockPos(4, 2 + ALOFT_JUDGED, 4));
		List<BlockPos> placed = build(helper, door);
		HalloweenSeason.setMode(HalloweenSeason.Mode.OFF);
		try {
			helper.assertTrue(HauntedHouseContest.judge(level, door, owner.getUUID()) == null, "Nobody is judged out of season");
			HalloweenSeason.setMode(HalloweenSeason.Mode.ON);
			helper.assertTrue(HauntedHouseContest.judge(level, door, null) == null, "A bowl with no owner judges nobody");
			HauntedHouseContest.Score first = HauntedHouseContest.judge(level, door, owner.getUUID());
			HauntedHouseContest.Contest contest = HauntedHouseContest.data(level.getServer()).get(HalloweenSeason.year());
			helper.assertTrue(first != null && contest != null && contest.best(owner.getUUID()) == first.points(), "The first judging is the entry");
			// Without the jack o'lantern and a carved pumpkin: 5 props of 2 kinds, 9 points.
			level.setBlock(placed.get(6), Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
			level.setBlock(placed.get(8), Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
			HauntedHouseContest.Score worse = HauntedHouseContest.judge(level, door, owner.getUUID());
			helper.assertTrue(worse != null && worse.points() < first.points() && contest.best(owner.getUUID()) == first.points(),
					"A worse night keeps the best");
			// With two soul lanterns and two skeleton skulls more: 9 props of 4 kinds, 17 points.
			placed.add(place(level, door.offset(0, 1, 3), Blocks.SOUL_LANTERN));
			placed.add(place(level, door.offset(1, 1, 3), Blocks.SOUL_LANTERN));
			placed.add(place(level, door.offset(-1, 1, 3), Blocks.SKELETON_SKULL));
			placed.add(place(level, door.offset(-2, 1, 3), Blocks.SKELETON_SKULL));
			HauntedHouseContest.Score better = HauntedHouseContest.judge(level, door, owner.getUUID());
			helper.assertTrue(better != null && better.points() > first.points() && contest.best(owner.getUUID()) == better.points(),
					"A better night raises it");
			AdvancementHolder judged = level.getServer().getAdvancements().get(Jugcraft.id(HauntedHouseContest.ADVANCEMENT));
			helper.assertTrue(judged != null && owner.getAdvancements().getOrStartProgress(judged).isDone(), "Haunted House is earned");
		} finally {
			HalloweenSeason.reset();
			clear(level, placed);
		}
		helper.succeed();
	}

	/** Once a season is over its three best houses win the ribbons, once; first place earns Scariest House on the Block. */
	@GameTest
	public void theBestHousesWinRibbonsOnce(GameTestHelper helper) {
		MinecraftServer server = helper.getLevel().getServer();
		int year = 1999;
		HauntedHouseContest.Contest contest = HauntedHouseContest.data(server).contest(year);
		helper.assertTrue(!contest.awarded(), "The test's long-past contest is new");
		List<ServerPlayer> owners = new ArrayList<>();
		int[] scores = {30, 50, 10, 40};
		for (int score : scores) {
			ServerPlayer owner = helper.makeMockServerPlayerInLevel();
			owners.add(owner);
			contest.judged(owner.getUUID(), owner.getName().getString(), score);
		}
		List<HauntedHouseContest.Standing> standings = contest.standings();
		helper.assertTrue(standings.get(0).best() == 50 && standings.get(3).best() == 10, "Best first: " + standings);
		HauntedHouseContest.check(server);
		helper.assertTrue(contest.awarded(), "A past contest is awarded");
		int[] place = {2, 0, -1, 1}; // Where each owner above came: third, first, none, second.
		for (int i = 0; i < owners.size(); i++) {
			ServerPlayer owner = owners.get(i);
			for (int ribbon = 0; ribbon < HarvestScaleBlockEntity.RIBBONS.size(); ribbon++) {
				Item item = JugcraftAgriculture.item(HarvestScaleBlockEntity.RIBBONS.get(ribbon));
				int count = owner.getInventory().countItem(item);
				helper.assertTrue(count == (place[i] == ribbon ? 1 : 0), "Owner " + i + " holds " + count + " of ribbon " + ribbon);
			}
		}
		AdvancementHolder winner = server.getAdvancements().get(Jugcraft.id(HauntedHouseContest.WINNER_ADVANCEMENT));
		helper.assertTrue(winner != null && owners.get(1).getAdvancements().getOrStartProgress(winner).isDone()
				&& !owners.get(3).getAdvancements().getOrStartProgress(winner).isDone(), "Only first place is the Scariest House on the Block");
		HauntedHouseContest.check(server);
		Item first = JugcraftAgriculture.item(HarvestScaleBlockEntity.RIBBONS.get(0));
		helper.assertTrue(owners.get(1).getInventory().countItem(first) == 1, "A contest is awarded once");
		helper.succeed();
	}
}

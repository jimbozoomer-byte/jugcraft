package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.creatures.herobrine.HerobrineMarkPlans;
import io.github.jimbozoomer.jugcraft.creatures.herobrine.HerobrineMarks;
import io.github.jimbozoomer.jugcraft.finance.FinanceBook;
import io.github.jimbozoomer.jugcraft.finance.LandRegistry;
import io.github.jimbozoomer.jugcraft.finance.Ledger;
import java.util.List;
import java.util.UUID;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;

/**
 * Herobrine's marks (docs/features/herobrine.md): a shrine is built in wild land, writes its sign, and is undone cleanly;
 * a player's build nearby, player-placed leaves, or a claim keep a mark away; a grove takes only leaves the world grew
 * and leaves the logs; a tunnel digs natural stone and stops short of ore; and undo leaves a block a player changed
 * as they left it. Everything stands on a raised stone platform so the margin a mark checks stays in the test arena.
 */
public class HerobrineMarksGameTests {
	private static final String ARENA = "jugcraft-test:tatterlace_arena";
	private static final int GROUND = 3;
	private static final String[] FIRST = {"COUNT THE", "WINDOWS", "", ""};

	private static void platform(GameTestHelper helper) {
		for (int x = 0; x < 48; x++) {
			for (int z = 0; z < 48; z++) {
				helper.setBlock(new BlockPos(x, GROUND, z), Blocks.STONE);
			}
		}
	}

	private static BlockPos feet(GameTestHelper helper, int x, int z) {
		return helper.absolutePos(new BlockPos(x, GROUND + 1, z));
	}

	private static List<String> message() {
		return List.of(FIRST);
	}

	@GameTest(structure = ARENA, padding = 48, maxTicks = 60)
	public void aShrineIsBuiltInTheWildAndUndoneCleanly(GameTestHelper helper) {
		platform(helper);
		ServerLevel level = helper.getLevel();
		BlockPos feet = feet(helper, 24, 24);
		HerobrineMarkPlans.Plan plan = HerobrineMarkPlans.shrine(level, feet, feet(helper, 24, 8), message());
		helper.assertTrue(plan != null, "Open, untouched ground should take a shrine");
		String id = HerobrineMarks.start(level, plan);
		helper.assertTrue(id != null, "The shrine should start building");
		helper.runAfterDelay(20, () -> {
			helper.assertTrue(level.getBlockState(feet).is(Blocks.MOSSY_COBBLESTONE), "The shrine's base should be built");
			helper.assertTrue(level.getBlockState(feet.above()).is(Blocks.NETHERRACK), "The shrine's heart should be netherrack");
			helper.assertTrue(level.getBlockState(feet.above().north()).is(Blocks.REDSTONE_TORCH), "Redstone torches should stand round it");
			BlockPos sign = plan.signs().keySet().iterator().next();
			helper.assertTrue(level.getBlockEntity(sign) instanceof SignBlockEntity entity
					&& entity.getText(net.minecraft.world.level.block.entity.SignTextSlot.FRONT).getMessages(false).getFirst().getString().equals(FIRST[0]), "Its sign should carry the message");
			HerobrineMarks.Undo undo = HerobrineMarks.undo(level, id);
			helper.assertTrue(undo != null && undo.kept() == 0 && undo.waiting() == 0, "The whole shrine should come back: " + undo);
			helper.assertTrue(level.getBlockState(feet).isAir() && level.getBlockState(feet.above()).isAir() && level.getBlockState(sign).isAir(),
					"Undo should leave the ground as it was");
			helper.assertTrue(!HerobrineMarks.get(level.getServer()).marks().containsKey(id), "An undone mark leaves no record");
			helper.succeed();
		});
	}

	@GameTest(structure = ARENA, padding = 48, maxTicks = 60)
	public void undoLeavesWhatAPlayerChangedAlone(GameTestHelper helper) {
		platform(helper);
		ServerLevel level = helper.getLevel();
		BlockPos feet = feet(helper, 24, 24);
		HerobrineMarkPlans.Plan plan = HerobrineMarkPlans.pyramid(level, feet, false, feet(helper, 24, 8), message());
		helper.assertTrue(plan != null, "Open ground should take a mossy pyramid");
		String id = HerobrineMarks.start(level, plan);
		helper.runAfterDelay(30, () -> {
			helper.assertTrue(level.getBlockState(feet.above(2)).is(Blocks.MOSSY_COBBLESTONE), "The pyramid should be built");
			BlockPos changed = feet.east(2).south(2);
			level.setBlockAndUpdate(changed, Blocks.OAK_PLANKS.defaultBlockState());
			HerobrineMarks.Undo undo = HerobrineMarks.undo(level, id);
			helper.assertTrue(undo != null && undo.kept() == 1, "Exactly the changed block should be kept: " + undo);
			helper.assertTrue(level.getBlockState(changed).is(Blocks.OAK_PLANKS), "The player's block should stay");
			helper.assertTrue(level.getBlockState(feet).isAir() && level.getBlockState(feet.above(2)).isAir(), "The rest should be undone");
			helper.succeed();
		});
	}

	@GameTest(structure = ARENA, padding = 48, maxTicks = 20)
	public void aPlayersBuildKeepsHimAway(GameTestHelper helper) {
		platform(helper);
		ServerLevel level = helper.getLevel();
		BlockPos feet = feet(helper, 24, 24);
		level.setBlockAndUpdate(feet.east(5), Blocks.OAK_PLANKS.defaultBlockState());
		helper.assertTrue(HerobrineMarkPlans.shrine(level, feet, feet(helper, 24, 8), message()) == null,
				"Planks within the margin should keep a shrine away");
		helper.assertTrue(HerobrineMarkPlans.sign(level, feet, feet(helper, 24, 8), message()) == null,
				"Even a sign should stay away from a build");
		helper.succeed();
	}

	@GameTest(structure = ARENA, padding = 48, maxTicks = 20)
	public void aClaimKeepsHimAway(GameTestHelper helper) {
		platform(helper);
		ServerLevel level = helper.getLevel();
		BlockPos feet = feet(helper, 24, 24);
		ServerPlayer admin = helper.makeMockServerPlayerInLevel();
		LandRegistry land = LandRegistry.get(level.getServer());
		FinanceBook book = Ledger.get(level.getServer()).book();
		long key = LandRegistry.keyOf(feet);
		helper.assertTrue(land.assign(book, key, book.account(UUID.randomUUID(), "HerobrineTestOwner"), 0).ok(), "The claim fixture");
		try {
			helper.assertTrue(HerobrineMarkPlans.shrine(level, feet, feet(helper, 24, 8), message()) == null,
					"A claimed chunk should take no mark");
		} finally {
			land.abandon(admin, book, key, true, 0);
		}
		helper.assertTrue(HerobrineMarkPlans.shrine(level, feet, feet(helper, 24, 8), message()) != null,
				"Once unclaimed, the same wild ground takes a shrine");
		helper.succeed();
	}

	private static void tree(GameTestHelper helper, int x, int z, boolean placedByHand) {
		for (int y = GROUND + 1; y <= GROUND + 5; y++) {
			helper.setBlock(new BlockPos(x, y, z), Blocks.OAK_LOG);
		}
		for (int dx = -2; dx <= 2; dx++) {
			for (int dz = -2; dz <= 2; dz++) {
				for (int y = GROUND + 3; y <= GROUND + 6; y++) {
					if ((dx != 0 || dz != 0 || y > GROUND + 5)) {
						helper.setBlock(new BlockPos(x + dx, y, z + dz), Blocks.OAK_LEAVES.defaultBlockState()
								.setValue(LeavesBlock.PERSISTENT, placedByHand).setValue(LeavesBlock.DISTANCE, 1));
					}
				}
			}
		}
	}

	@GameTest(structure = ARENA, padding = 48, maxTicks = 140)
	public void aGroveTakesOnlyTheLeavesAndLeavesTheLogs(GameTestHelper helper) {
		platform(helper);
		ServerLevel level = helper.getLevel();
		tree(helper, 24, 24, false);
		BlockPos feet = feet(helper, 22, 22);
		HerobrineMarkPlans.Plan plan = HerobrineMarkPlans.grove(level, feet, feet(helper, 22, 8), message(), level.getRandom());
		helper.assertTrue(plan != null, "A wild tree should make a grove");
		String id = HerobrineMarks.start(level, plan);
		helper.assertTrue(id != null, "The grove should start");
		helper.runAfterDelay(100, () -> {
			int leaves = 0;
			for (BlockPos pos : BlockPos.betweenClosed(helper.absolutePos(new BlockPos(22, GROUND + 3, 22)),
					helper.absolutePos(new BlockPos(26, GROUND + 6, 26)))) {
				leaves += level.getBlockState(pos).is(Blocks.OAK_LEAVES) ? 1 : 0;
			}
			helper.assertTrue(leaves == 0, leaves + " leaves are left on the bare tree");
			helper.assertTrue(level.getBlockState(helper.absolutePos(new BlockPos(24, GROUND + 4, 24))).is(Blocks.OAK_LOG), "The trunk should stand");
			HerobrineMarks.undo(level, id);
			helper.succeed();
		});
	}

	@GameTest(structure = ARENA, padding = 48, maxTicks = 20)
	public void leavesAPlayerPlacedKeepAGroveAway(GameTestHelper helper) {
		platform(helper);
		ServerLevel level = helper.getLevel();
		tree(helper, 24, 24, true);
		BlockPos feet = feet(helper, 22, 22);
		helper.assertTrue(HerobrineMarkPlans.grove(level, feet, feet(helper, 22, 8), message(), level.getRandom()) == null,
				"A tree a player dressed in leaves is a build, not a grove");
		helper.succeed();
	}

	@GameTest(structure = ARENA, padding = 48, maxTicks = 80)
	public void aTunnelDigsNaturalStoneAndStopsShortOfOre(GameTestHelper helper) {
		platform(helper);
		ServerLevel level = helper.getLevel();
		for (int x = 14; x <= 34; x++) {
			for (int y = GROUND + 1; y <= GROUND + 5; y++) {
				for (int z = 24; z <= 44; z++) {
					helper.setBlock(new BlockPos(x, y, z), Blocks.STONE);
				}
			}
		}
		helper.setBlock(new BlockPos(24, GROUND + 2, 33), Blocks.DIAMOND_ORE);
		BlockPos feet = feet(helper, 24, 22);
		HerobrineMarkPlans.Plan plan = HerobrineMarkPlans.tunnel(level, feet, Direction.SOUTH, feet(helper, 24, 8), message());
		helper.assertTrue(plan != null, "A stone hillside should take a tunnel");
		String id = HerobrineMarks.start(level, plan);
		helper.runAfterDelay(40, () -> {
			helper.assertTrue(level.getBlockState(helper.absolutePos(new BlockPos(24, GROUND + 1, 26))).isAir()
					&& level.getBlockState(helper.absolutePos(new BlockPos(23, GROUND + 2, 26))).isAir(), "The tunnel should be dug two wide and two high");
			helper.assertTrue(level.getBlockState(helper.absolutePos(new BlockPos(24, GROUND + 2, 33))).is(Blocks.DIAMOND_ORE), "The ore should be left");
			helper.assertTrue(!level.getBlockState(helper.absolutePos(new BlockPos(24, GROUND + 1, 33))).isAir(), "The tunnel should stop before the ore");
			helper.assertTrue(level.getBlockState(helper.absolutePos(new BlockPos(24, GROUND + 3, 26))).is(Blocks.STONE), "The roof should stay");
			HerobrineMarks.Undo undo = HerobrineMarks.undo(level, id);
			helper.assertTrue(undo != null && undo.kept() == 0, "The tunnel should fill back in: " + undo);
			helper.assertTrue(level.getBlockState(helper.absolutePos(new BlockPos(24, GROUND + 1, 26))).is(Blocks.STONE), "Undo should restore the stone");
			helper.succeed();
		});
	}
}

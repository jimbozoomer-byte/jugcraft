package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.concordance.rime.Frostward;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;

/**
 * Concordance Chunk B, unit 4 (docs/features/arcane-concordance-winter.md), as far as a game-test server shows it. B-08,
 * Winter Brine's Frostward: a creature under it gathers no frost in powder snow, frost it had before is gone at its next
 * tick, and the status keeps its time through a save. (The brew itself is in {@link ConcordanceAlchemyGameTests}, and
 * the Yeti King's chill passing a Frostward drinker by in {@link YetiKingGameTests}.) B-10's travel kit and Frost-boon
 * practice join this class with them.
 */
public class ConcordanceWinterGameTests {
	/** A stone floor under the first seven by three blocks, so a creature in powder snow stands on it. */
	private static void floor(GameTestHelper helper) {
		for (int x = 0; x <= 6; x++) {
			for (int z = 0; z <= 2; z++) {
				helper.setBlock(new BlockPos(x, 1, z), Blocks.STONE);
			}
		}
	}

	/**
	 * Frostward clears frost in its own effect tick, whatever gives it: a pig standing in powder snow gathers frost, one
	 * under Frostward beside it ends each tick with at most the one tick of frost powder snow has just added (never frozen
	 * through), and a pig on stone frozen 100 ticks' worth before is clear (melting alone, two a tick, would leave it 20).
	 * Saved and loaded, the warded pig's Frostward keeps its time and level.
	 */
	@GameTest(maxTicks = 60)
	public void frostwardClearsFrostAndKeepsItsTime(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		floor(helper);
		helper.setBlock(new BlockPos(1, 2, 1), Blocks.POWDER_SNOW);
		helper.setBlock(new BlockPos(3, 2, 1), Blocks.POWDER_SNOW);
		Mob bare = helper.spawnWithNoFreeWill(EntityTypes.PIG, new BlockPos(1, 2, 1));
		Mob warded = helper.spawnWithNoFreeWill(EntityTypes.PIG, new BlockPos(3, 2, 1));
		warded.addEffect(new MobEffectInstance(Frostward.FROSTWARD, 1200));
		Mob thawing = helper.spawnWithNoFreeWill(EntityTypes.PIG, new BlockPos(5, 2, 1));
		thawing.setTicksFrozen(100);
		thawing.addEffect(new MobEffectInstance(Frostward.FROSTWARD, 1200));
		helper.runAfterDelay(40, () -> {
			helper.assertTrue(bare.getTicksFrozen() >= 30, "A bare pig in powder snow gathers frost: " + bare.getTicksFrozen());
			helper.assertTrue(warded.getTicksFrozen() <= 1 && !warded.isFullyFrozen(),
					"Under Frostward it gathers none: " + warded.getTicksFrozen());
			helper.assertTrue(thawing.getTicksFrozen() == 0, "Frost gathered before is cleared: " + thawing.getTicksFrozen());
			TagValueOutput out = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
			warded.saveWithoutId(out);
			Mob copy = EntityTypes.PIG.create(level, EntitySpawnReason.COMMAND);
			copy.load(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), out.buildResult()));
			MobEffectInstance before = warded.getEffect(Frostward.FROSTWARD);
			MobEffectInstance after = copy.getEffect(Frostward.FROSTWARD);
			helper.assertTrue(before != null && after != null && after.getDuration() == before.getDuration()
					&& after.getAmplifier() == before.getAmplifier(), "Frostward keeps its time through a save: " + before + " / " + after);
			copy.discard();
			helper.succeed();
		});
	}
}

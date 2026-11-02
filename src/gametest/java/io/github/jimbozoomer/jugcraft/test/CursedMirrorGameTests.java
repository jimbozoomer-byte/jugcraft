package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.curse.CursedMirror;
import io.github.jimbozoomer.jugcraft.curse.CursedMirrorBlock;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * In-game tests for the Cursed Mirror (docs/features/cursed-mirror.md): a survival player who breaks it is cursed with
 * Bad Luck for seven days and the mirror drops; a creative break curses nobody; and a break by no player (the world
 * itself) curses nobody but still drops the mirror.
 */
public class CursedMirrorGameTests {
	private static final BlockPos AT = new BlockPos(2, 2, 2);

	private static void floor(GameTestHelper helper) {
		for (int x = 0; x <= 4; x++) {
			for (int z = 0; z <= 4; z++) {
				helper.setBlock(new BlockPos(x, 1, z), Blocks.STONE);
			}
		}
		helper.setBlock(AT, CursedMirror.BLOCK.defaultBlockState());
	}

	private static ServerPlayer player(GameTestHelper helper, GameType mode) {
		// Vanilla's mock permanently reports creative, even after setGameMode.
		ServerPlayer player = TestPlayers.survivor(helper);
		player.setGameMode(mode);
		helper.assertTrue(player.isCreative() == (mode == GameType.CREATIVE), "The breaker has the requested game mode");
		BlockPos absolute = helper.absolutePos(new BlockPos(2, 2, 0));
		player.setPos(absolute.getX() + 0.5, absolute.getY(), absolute.getZ() + 0.5);
		return player;
	}

	private static int dropped(GameTestHelper helper) {
		return helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(helper.absolutePos(AT)).inflate(3.0))
				.stream().filter(entity -> entity.getItem().is(CursedMirror.ITEM)).mapToInt(entity -> entity.getItem().getCount()).sum();
	}

	@GameTest
	public void breakingCursesThePlayer(GameTestHelper helper) {
		floor(helper);
		ServerLevel level = helper.getLevel();
		BlockPos absolute = helper.absolutePos(AT);
		ServerPlayer breaker = player(helper, GameType.SURVIVAL);
		BlockState state = level.getBlockState(absolute);
		((CursedMirrorBlock) state.getBlock()).playerWillDestroy(level, absolute, state, breaker);
		level.destroyBlock(absolute, true);
		helper.assertTrue(breaker.hasEffect(MobEffects.UNLUCK), "The breaker is cursed with Bad Luck");
		int left = breaker.getEffect(MobEffects.UNLUCK).getDuration();
		helper.assertTrue(left > CursedMirrorBlock.CURSE_TICKS - 100 && left <= CursedMirrorBlock.CURSE_TICKS,
				"The curse lasts about seven days, not " + left + " ticks");
		helper.assertTrue(dropped(helper) == 1, "The broken mirror drops once");
		helper.succeed();
	}

	@GameTest
	public void creativeBreakDoesNotCurse(GameTestHelper helper) {
		floor(helper);
		ServerLevel level = helper.getLevel();
		BlockPos absolute = helper.absolutePos(AT);
		ServerPlayer breaker = player(helper, GameType.CREATIVE);
		BlockState state = level.getBlockState(absolute);
		((CursedMirrorBlock) state.getBlock()).playerWillDestroy(level, absolute, state, breaker);
		helper.assertFalse(breaker.hasEffect(MobEffects.UNLUCK), "A creative break does not curse");
		helper.succeed();
	}

	@GameTest
	public void aNonPlayerBreakDoesNotCurse(GameTestHelper helper) {
		floor(helper);
		ServerLevel level = helper.getLevel();
		BlockPos absolute = helper.absolutePos(AT);
		ServerPlayer bystander = player(helper, GameType.SURVIVAL);
		level.destroyBlock(absolute, true);
		helper.assertFalse(bystander.hasEffect(MobEffects.UNLUCK), "Breaking it with no player curses nobody");
		helper.assertTrue(dropped(helper) == 1, "It still drops once");
		helper.succeed();
	}
}

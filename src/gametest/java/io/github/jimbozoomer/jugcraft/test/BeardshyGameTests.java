package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.season.Beardshy;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.trading.Merchant;

/** Beardshy (docs/features/beardshy.md): shy of standing players, talks to crouching ones, never trades or fights. */
public class BeardshyGameTests {
	@GameTest
	public void beardshyShiesFromStandingPlayersButNotCrouchingOnes(GameTestHelper helper) {
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		helper.assertTrue(Beardshy.startles(player), "A standing player did not startle Beardshy");
		player.setShiftKeyDown(true);
		helper.assertFalse(Beardshy.startles(player), "A crouching player startled Beardshy");
		helper.succeed();
	}

	@GameTest
	public void beardshyTalksWithACooldownAndNeverTrades(GameTestHelper helper) {
		Beardshy shy = helper.spawnWithNoFreeWill(Beardshy.TYPE, new BlockPos(1, 2, 1));
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		player.setShiftKeyDown(true);
		helper.assertTrue(shy.speak(player), "Beardshy said nothing to a crouching player");
		helper.assertFalse(shy.speak(player), "Beardshy spoke again before his cooldown");
		helper.assertFalse(shy instanceof Merchant, "Beardshy trades");
		helper.assertTrue(shy.isPersistenceRequired(), "Beardshy can despawn");
		helper.runAfterDelay(Beardshy.TALK_COOLDOWN, () -> {
			helper.assertTrue(shy.speak(player), "Beardshy did not speak again after his cooldown");
			helper.succeed();
		});
	}

	@GameTest(maxTicks = 40)
	public void beardshyApologisesAndNeverFightsBack(GameTestHelper helper) {
		Beardshy shy = helper.spawnWithNoFreeWill(Beardshy.TYPE, new BlockPos(1, 2, 1));
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		ServerLevel level = helper.getLevel();
		helper.assertTrue(shy.hurtServer(level, level.damageSources().playerAttack(player), 1.0F), "A player's blow did not land on Beardshy");
		helper.runAfterDelay(20, () -> {
			helper.assertTrue(shy.isAlive() && shy.getTarget() == null, "Beardshy fought back");
			helper.succeed();
		});
	}
}

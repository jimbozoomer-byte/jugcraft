package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.lair.vesperine.GraveThrallEntity;
import io.github.jimbozoomer.jugcraft.lair.vesperine.GriefBoltEntity;
import io.github.jimbozoomer.jugcraft.lair.vesperine.JugcraftVesperine;
import io.github.jimbozoomer.jugcraft.lair.vesperine.ReaperSkullEntity;
import io.github.jimbozoomer.jugcraft.lair.vesperine.VesperineEntity;
import io.github.jimbozoomer.jugcraft.lair.vesperine.VesperineLoot;
import io.github.jimbozoomer.jugcraft.weapons.HarvestBoon;
import java.util.List;
import java.util.UUID;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;

/**
 * Vesperine, the Last Reaper (docs/features/vesperine.md), as far as a game-test server can show her: one put down on
 * her own (with no lair instance) sits until a player's blow wakes her, her skulls guard her, her arc strikes in front
 * and not behind, a struck Grief Bolt turns, her servants go with her, she goes back to her seat when left alone, her
 * loot is each participant's own, and the Vesper Scythe's Harvest heals on a kill. Her fight in the Hollow Acre itself
 * (the wards, the moon, the exit) runs in a real world, in {@link VesperineClientGameTests}.
 */
public class VesperineGameTests {
	private static final BlockPos SEAT = new BlockPos(4, 2, 4);

	private static VesperineEntity seated(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		Vec3 at = Vec3.atBottomCenterOf(helper.absolutePos(SEAT));
		return VesperineEntity.summon(level, at, 0.0F, at, null);
	}

	private static ServerPlayer player(GameTestHelper helper, double x, double z) {
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		player.setGameMode(GameType.SURVIVAL);
		Vec3 at = Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(0, 2, 0))).add(x, 0.0, z);
		player.snapTo(at.x, at.y, at.z, 0.0F, 0.0F);
		return player;
	}

	private static Item item(String id) {
		return BuiltInRegistries.ITEM.getValue(Jugcraft.id(id));
	}

	/** Seated, she takes no harm; a player's blow wakes her, her bar shows, and both skulls are at her shoulders. */
	@GameTest
	public void aBlowWakesHerAndDoesNoHarm(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		VesperineEntity vesperine = seated(helper);
		ServerPlayer player = player(helper, 4.5, 7.5);
		helper.assertTrue(vesperine.phase() == VesperineEntity.Phase.SEATED, "She waits seated");
		helper.assertTrue(vesperine.skullsAlive(level) == 2, "Dirge and Requiem are with her");
		float before = vesperine.getHealth();
		helper.assertFalse(vesperine.hurtServer(level, level.damageSources().playerAttack(player), 10.0F), "A seated reaper takes no harm");
		helper.assertTrue(vesperine.getHealth() == before, "Her health is untouched");
		helper.assertTrue(vesperine.phase() == VesperineEntity.Phase.RISING, "The blow wakes her: she rises");
		for (ReaperSkullEntity skull : vesperine.skulls(level)) {
			helper.assertTrue(skull.position().distanceTo(vesperine.shoulder(skull.left())) < 0.5, "A skull floats at her shoulder");
		}
		helper.succeed();
	}

	/** While both skulls live she takes half a blow; with one, all of it; her health grows with the party, within bounds. */
	@GameTest(maxTicks = 40)
	public void herSkullsGuardHer(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		VesperineEntity vesperine = seated(helper);
		helper.assertTrue(vesperine.guards() == 2 && vesperine.taken(10.0F) == 5.0F, "Both skulls: half damage");
		helper.assertTrue(VesperineEntity.partyScale(1) == 1.0 && VesperineEntity.partyScale(2) == 1.5
				&& VesperineEntity.partyScale(4) == 2.5 && VesperineEntity.partyScale(9) == 2.5, "Her health scales with the party, at most 2.5 times");
		helper.assertTrue(VesperineEntity.slamDamage(0.0) == 16.0F && VesperineEntity.slamDamage(5.0) == 8.0F
				&& VesperineEntity.slamDamage(10.0) == 0.0F, "The slam falls off to nothing at 10 blocks");
		vesperine.skulls(level).getFirst().discard();
		helper.runAfterDelay(3, () -> {
			helper.assertTrue(vesperine.guards() == 1, "One skull left");
			helper.assertTrue(vesperine.taken(10.0F) == 10.0F, "One skull: she takes the whole blow");
			helper.succeed();
		});
	}

	/** The Reaping Arc strikes a player in front of her (14 damage and Wither) and not one directly behind. */
	@GameTest(maxTicks = 80)
	public void theReapingArcStrikesInFrontNotBehind(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		VesperineEntity vesperine = seated(helper);
		ServerPlayer front = player(helper, 4.5, 7.0);
		ServerPlayer behind = player(helper, 4.5, 2.0);
		vesperine.wake(level);
		helper.runAfterDelay(VesperineEntity.RISE_TICKS + 2, () -> {
			vesperine.setYRot(0.0F);  // facing south, towards the one in front
			int hit = vesperine.reapingArc(level);
			helper.assertTrue(hit == 1, "The arc struck " + hit + " players, not 1");
			helper.assertTrue(front.getHealth() < front.getMaxHealth() && front.hasEffect(MobEffects.WITHER), "The one in front is reaped and withers");
			helper.assertTrue(behind.getHealth() == behind.getMaxHealth(), "The one behind her is spared");
			helper.succeed();
		});
	}

	/** A Grief Bolt struck by a player turns back towards its skull; a turned bolt cannot be struck again. */
	@GameTest
	public void aStruckGriefBoltTurns(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		VesperineEntity vesperine = seated(helper);
		ServerPlayer player = player(helper, 4.5, 7.5);
		ReaperSkullEntity skull = vesperine.skulls(level).getFirst();
		GriefBoltEntity bolt = GriefBoltEntity.loose(level, skull, player);
		helper.assertFalse(bolt.reflected(), "A loosed bolt flies at the player");
		helper.assertTrue(bolt.hurtServer(level, level.damageSources().playerAttack(player), 1.0F), "A player's blow turns it");
		helper.assertTrue(bolt.reflected(), "It flies back");
		helper.assertFalse(bolt.hurtServer(level, level.damageSources().playerAttack(player), 1.0F), "It turns only once");
		bolt.discard();
		helper.succeed();
	}

	/** Her Grave Call brings up three thralls; when she falls they crumble, and her skulls go with her. */
	@GameTest(maxTicks = 160)
	public void herServantsGoWithHer(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		VesperineEntity vesperine = seated(helper);
		ServerPlayer player = player(helper, 4.5, 7.5);
		player.addEffect(new net.minecraft.world.effect.MobEffectInstance(MobEffects.RESISTANCE, 400, 4));
		vesperine.wake(level);
		helper.runAfterDelay(VesperineEntity.RISE_TICKS + 2, () -> {
			vesperine.begin(level, VesperineEntity.Attack.GRAVE_CALL, player);
			helper.runAfterDelay(VesperineEntity.Attack.GRAVE_CALL.windup + 4, () -> {
				List<UUID> thralls = vesperine.thralls();
				helper.assertTrue(thralls.size() == VesperineEntity.CALL_THRALLS, thralls.size() + " thralls rose, not " + VesperineEntity.CALL_THRALLS);
				List<ReaperSkullEntity> skulls = vesperine.skulls(level);
				vesperine.hurtServer(level, level.damageSources().genericKill(), Float.MAX_VALUE);
				helper.runAfterDelay(3, () -> {
					for (UUID id : thralls) {
						helper.assertTrue(!(level.getEntity(id) instanceof GraveThrallEntity thrall) || !thrall.isAlive(), "A thrall outlived her");
					}
					for (ReaperSkullEntity skull : skulls) {
						helper.assertTrue(skull.isRemoved() || !skull.isAlive(), "A skull outlived her");
					}
					helper.succeed();
				});
			});
		});
	}

	/** Left alone after she rises, she goes back to her seat, healed. */
	@GameTest(maxTicks = 320)
	public void leftAloneSheReturnsToHerSeat(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		VesperineEntity vesperine = seated(helper);
		Vec3 seat = vesperine.position();
		vesperine.wake(level);
		helper.runAfterDelay(VesperineEntity.RISE_TICKS + 2, () -> {
			vesperine.setHealth(vesperine.getMaxHealth() * 0.6F);
			helper.runAfterDelay(VesperineEntity.ABANDON_TICKS + 10, () -> {
				helper.assertTrue(vesperine.phase() == VesperineEntity.Phase.SEATED, "With nobody to fight she sits again, not " + vesperine.phase());
				helper.assertTrue(vesperine.getHealth() == vesperine.getMaxHealth(), "She is healed");
				helper.assertTrue(vesperine.position().distanceTo(seat) < 0.5, "She is back on her seat");
				helper.succeed();
			});
		});
	}

	/**
	 * Her loot is each participant's own: Reaper's Shade, and on a first kill always the Vesper Scythe; afterwards The
	 * Last Harvest is theirs, and a later roll no longer counts as a first kill.
	 */
	@GameTest
	public void herLootIsEachParticipantsOwn(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		VesperineEntity vesperine = seated(helper);
		ServerPlayer player = player(helper, 4.5, 7.5);
		helper.assertTrue(VesperineLoot.firstKill(player), "A newcomer has not yet won The Last Harvest");
		List<ItemStack> loot = VesperineLoot.roll(level, vesperine, player);
		int shades = loot.stream().filter(stack -> stack.is(JugcraftVesperine.REAPER_SHADE)).mapToInt(ItemStack::getCount).sum();
		helper.assertTrue(shades >= 3 && shades <= 6, shades + " Reaper's Shade, not 3 to 6");
		helper.assertTrue(loot.stream().anyMatch(stack -> stack.is(item("vesper_scythe"))), "A first kill always brings the Vesper Scythe");
		VesperineLoot.reward(level, vesperine, List.of(player));
		helper.assertTrue(player.getInventory().countItem(JugcraftVesperine.REAPER_SHADE) >= 3, "The loot went into their inventory");
		helper.assertFalse(VesperineLoot.firstKill(player), "The Last Harvest is theirs now");
		vesperine.discard();
		helper.succeed();
	}

	/** The Vesper Scythe's Harvest: a kill with it heals its wielder two hearts. */
	@GameTest
	public void aKillWithTheVesperScytheHeals(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer player = player(helper, 4.5, 4.5);
		ItemStack scythe = new ItemStack(item("vesper_scythe"));
		player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, scythe);
		player.setHealth(10.0F);
		Mob zombie = helper.spawnWithNoFreeWill(EntityTypes.ZOMBIE, new BlockPos(5, 2, 4));
		zombie.hurtServer(level, level.damageSources().playerAttack(player), 1000.0F);
		scythe.getItem().hurtEnemy(scythe, zombie, player);
		helper.assertTrue(player.getHealth() == 10.0F + HarvestBoon.HEAL, "A kill heals " + HarvestBoon.HEAL + ", health is " + player.getHealth());
		helper.assertFalse(HarvestBoon.charged(player), "One kill does not yet charge the crescent");
		helper.succeed();
	}
}

package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.guns.GunShots;
import io.github.jimbozoomer.jugcraft.guns.JugcraftGuns;
import java.util.Map;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;

/**
 * In-game tests for the guns' recoil push (the recoil-push follow-up, docs/features/guns.md): the guns whose shot pushes
 * their shooter back, at the owner's strengths (their playerKnockBack), and how the push is given. Shooters face south
 * (+z), so a push back is toward -z.
 */
public class RecoilPushGameTests {
	/**
	 * The guns whose shot pushes their shooter back are the owner's 27 built guns with player knockback, at the owner's
	 * strengths, the Flatboat Gun's (the Big Bore's) 2.5 the strongest. A gun without any, the Kreuzer Pistol, has none.
	 */
	@GameTest
	public void kicksAreTheOwners(GameTestHelper helper) {
		helper.assertTrue(JugcraftGuns.KICK.equals(Map.ofEntries(Map.entry("thunderpipe", 0.45F), Map.entry("coach_gun", 0.25F), Map.entry("bellmouth", 0.3F),
				Map.entry("bulldog_pistol", 0.8F), Map.entry("breacher", 0.15F), Map.entry("trench_lobber", 0.2F),
				Map.entry("stoker", 0.7F), Map.entry("bronco_smg", 0.2F), Map.entry("sledge", 0.35F), Map.entry("skylark_rifle", 0.2F),
				Map.entry("bullfrog", 0.2F), Map.entry("mule", 0.35F), Map.entry("fowler", 0.34F), Map.entry("culverin", 0.35F),
				Map.entry("reverb", 0.15F), Map.entry("tusker", 0.15F), Map.entry("crackling", 0.5F), Map.entry("borer", 0.25F),
				Map.entry("goshawk_revolver", 2.25F), Map.entry("waystation_shotgun", 0.35F), Map.entry("flatboat_gun", 2.5F),
				Map.entry("fencepost_thumper", 0.3F), Map.entry("holler_pistol", 0.25F), Map.entry("holler_carbine", 0.15F),
				Map.entry("navvy_shotgun", 0.1F), Map.entry("impetus_shotgun", 0.25F), Map.entry("auger_shotgun", 0.1F))),
				"The guns that push their shooter back are not the owner's: " + JugcraftGuns.KICK);
		helper.assertTrue(JugcraftGuns.kick(JugcraftGuns.GUNS.get("kreuzer_pistol")) == 0.0F, "The Kreuzer Pistol pushes its shooter back");
		helper.succeed();
	}

	/**
	 * Fired at a pig three blocks off, the Impetus Shotgun's shot pushes its shooter back at the owner's 0.25 blocks a tick
	 * times the level share of their look (they look a little down at the pig), and no other way; a Kreuzer Pistol's shot
	 * leaves its shooter as they were.
	 */
	@GameTest(structure = GunsGameTests.ARENA, maxTicks = 40)
	public void shotsPushTheirShooterBack(GameTestHelper helper) {
		GunsGameTests.floor(helper);
		Mob pig = NetherGunsGameTests.sturdyPig(helper, new BlockPos(1, 2, 4));
		Mob other = NetherGunsGameTests.sturdyPig(helper, new BlockPos(7, 2, 4));
		ServerPlayer shotgun = NetherGunsGameTests.shooter(helper, "impetus_shotgun", new BlockPos(1, 2, 1), pig);
		ServerPlayer pistol = NetherGunsGameTests.shooter(helper, "kreuzer_pistol", new BlockPos(7, 2, 1), other);
		shotgun.setDeltaMovement(Vec3.ZERO);
		pistol.setDeltaMovement(Vec3.ZERO);
		Vec3 look = shotgun.getLookAngle();
		double back = 0.25 * Math.sqrt(look.x * look.x + look.z * look.z);
		helper.assertTrue(GunShots.fire(shotgun) && GunShots.fire(pistol), "The Impetus Shotgun or the Kreuzer Pistol did not fire");
		Vec3 kicked = shotgun.getDeltaMovement();
		helper.assertTrue(back > 0.2 && Math.abs(kicked.z + back) < 1.0E-6 && Math.abs(kicked.x) < 1.0E-6 && Math.abs(kicked.y) < 1.0E-6,
				"The Impetus Shotgun's shot moved its shooter " + kicked + ", not " + back + " back");
		helper.assertTrue(pistol.getDeltaMovement().lengthSqr() < 1.0E-12, "The Kreuzer Pistol's shot moved its shooter " + pistol.getDeltaMovement());
		helper.succeed();
	}

	/**
	 * A push tops its shooter's backward speed up to its strength and no further: a second push of the same strength at
	 * once adds nothing, and a weaker one nothing either. Looking straight down there is no push (none up or down); looking
	 * halfway down, half of it. Half knockback resistance takes half of it off.
	 */
	@GameTest
	public void kickTopsUpAndNeverLifts(GameTestHelper helper) {
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		player.setGameMode(GameType.SURVIVAL);
		player.setYRot(0.0F);
		player.setXRot(0.0F);
		player.setDeltaMovement(Vec3.ZERO);
		GunShots.kick(player, 0.8F);
		helper.assertTrue(Math.abs(player.getDeltaMovement().z + 0.8) < 1.0E-6, "A push of 0.8 moved its shooter " + player.getDeltaMovement());
		GunShots.kick(player, 0.8F);
		GunShots.kick(player, 0.3F);
		helper.assertTrue(Math.abs(player.getDeltaMovement().z + 0.8) < 1.0E-6, "Pushes on top of a push drove its shooter on: " + player.getDeltaMovement());
		player.setDeltaMovement(Vec3.ZERO);
		player.setXRot(90.0F);
		GunShots.kick(player, 2.5F);
		helper.assertTrue(player.getDeltaMovement().lengthSqr() < 1.0E-12, "A shot straight down moved its shooter " + player.getDeltaMovement());
		player.setXRot(60.0F);
		GunShots.kick(player, 2.0F);
		helper.assertTrue(Math.abs(player.getDeltaMovement().z + 1.0) < 1.0E-4 && Math.abs(player.getDeltaMovement().y) < 1.0E-12,
				"A shot looking 60 degrees down moved its shooter " + player.getDeltaMovement() + ", not 1.0 back");
		player.setDeltaMovement(Vec3.ZERO);
		player.setXRot(0.0F);
		player.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(0.5);
		GunShots.kick(player, 0.8F);
		helper.assertTrue(Math.abs(player.getDeltaMovement().z + 0.4) < 1.0E-6, "Half knockback resistance left a push of " + player.getDeltaMovement());
		helper.succeed();
	}
}

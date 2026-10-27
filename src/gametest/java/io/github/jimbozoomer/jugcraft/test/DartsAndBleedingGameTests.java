package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.guns.GunShots;
import io.github.jimbozoomer.jugcraft.guns.JugcraftGuns;
import java.util.List;
import java.util.Map;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;

/**
 * In-game tests for darts and bleeding (docs/features/guns.md, the darts-and-bleeding follow-up): the Quill Rifle fires
 * the owner's darts, which fall over their flight, and some guns' hits make what they hit bleed, through its armour, as
 * the owner's data has them. Shooters face south (+z).
 */
public class DartsAndBleedingGameTests {
	/**
	 * The Dart is registered, with its recipe and its spent case, and the Quill Rifle fires it. The guns that may make a
	 * creature bleed are the owner's, at the owner's chances and lengths (their "lacerated"): the Picket Rifle's, the
	 * Spikedriver's, the Harrow's and the Quill Rifle's, and (the hit-effects follow-up) the Cloudburst Gun's and the
	 * Caisson Pistol's. The Bleeding effect is registered.
	 */
	@GameTest
	public void dartsAndBleedingAreTheOwners(GameTestHelper helper) {
		helper.assertTrue(JugcraftGuns.ROUNDS.get(JugcraftGuns.DART) != null && JugcraftGuns.CASINGS.containsKey(JugcraftGuns.DART),
				"The Dart is not a round that leaves a spent case");
		helper.assertTrue(helper.getLevel().recipeAccess().byKey(ResourceKey.create(Registries.RECIPE, Jugcraft.id(JugcraftGuns.DART))).isPresent(),
				"The Dart's recipe does not load");
		helper.assertTrue(JugcraftGuns.SPECS.get("quill_rifle").ammo().equals(JugcraftGuns.DART), "The Quill Rifle does not fire darts");
		helper.assertTrue(JugcraftGuns.BLEEDS.equals(Map.of("picket_rifle", new JugcraftGuns.Bleed(0.3F, 300), "spikedriver",
				new JugcraftGuns.Bleed(0.6F, 300), "harrow", new JugcraftGuns.Bleed(0.3F, 300), "quill_rifle", new JugcraftGuns.Bleed(0.4F, 300),
				"cloudburst_gun", new JugcraftGuns.Bleed(0.3F, 300), "caisson_pistol", new JugcraftGuns.Bleed(0.5F, 200))),
				"The guns that make a creature bleed are not the owner's: " + JugcraftGuns.BLEEDS);
		helper.assertTrue(JugcraftGuns.BLEEDING != null && JugcraftGuns.BLEEDING.isBound(), "Bleeding is not registered");
		helper.succeed();
	}

	/**
	 * A bullet flies straight to the end of its range in one leg. A dart fired level flies a tick's flight a leg, five
	 * blocks the first without falling and each after falling 0.05 of a block a tick faster: after six legs, 30 blocks
	 * on, it has fallen 0.75 of a block; at the end of 64 blocks of flight it has fallen between 3.6 and 3.9.
	 */
	@GameTest
	public void dartsFallAsTheyFly(GameTestHelper helper) {
		Vec3 eye = new Vec3(0.0, 64.0, 0.0);
		Vec3 south = new Vec3(0.0, 0.0, 1.0);
		List<Vec3> bullet = GunShots.flight(eye, south, 64.0, false);
		helper.assertTrue(bullet.size() == 2 && bullet.get(1).distanceTo(new Vec3(0.0, 64.0, 64.0)) < 1.0E-9,
				"A bullet does not fly straight: " + bullet);
		List<Vec3> dart = GunShots.flight(eye, south, 64.0, true);
		helper.assertTrue(dart.get(1).distanceTo(new Vec3(0.0, 64.0, 5.0)) < 1.0E-9 && dart.get(6).distanceTo(new Vec3(0.0, 63.25, 30.0)) < 1.0E-9,
				"A dart's flight does not fall as an arrow's: " + dart);
		double flown = 0.0;
		for (int leg = 1; leg < dart.size(); leg++) {
			flown += dart.get(leg).distanceTo(dart.get(leg - 1));
		}
		Vec3 end = dart.getLast();
		helper.assertTrue(Math.abs(flown - 64.0) < 1.0E-6 && end.y < 64.0 - 3.6 && end.y > 64.0 - 3.9,
				"A dart flew " + flown + " blocks and ended at " + end);
		helper.succeed();
	}

	/**
	 * A sturdy pig in 20 points of armour, bleeding: it loses one point after the effect's first 40 ticks' turn comes,
	 * the whole point through its armour, to the bleeding damage type, and no more before the next.
	 */
	@GameTest(structure = GunsGameTests.ARENA, maxTicks = 80)
	public void bleedingGoesThroughArmour(GameTestHelper helper) {
		GunsGameTests.floor(helper);
		Mob pig = NetherGunsGameTests.sturdyPig(helper, new BlockPos(4, 2, 4));
		pig.getAttribute(Attributes.ARMOR).setBaseValue(20.0);
		pig.addEffect(new MobEffectInstance(JugcraftGuns.BLEEDING, 100, 0));
		helper.runAfterDelay(50, () -> {
			helper.assertTrue(Math.abs(pig.getHealth() - (200.0F - JugcraftGuns.BLEED_DAMAGE)) < 1.0E-3F,
					"Bleeding through 20 points of armour left the pig " + pig.getHealth() + ", not " + (200.0F - JugcraftGuns.BLEED_DAMAGE));
			helper.assertTrue(pig.getLastDamageSource() != null && pig.getLastDamageSource().is(JugcraftGuns.BLEED_TYPE),
					"The pig was last hurt by " + pig.getLastDamageSource() + ", not bleeding");
			helper.succeed();
		});
	}

	/**
	 * The Quill Rifle, fired 30 times at a sturdy pig three blocks off by a shooter in creative (no darts spent), makes it
	 * bleed: each hit's chance is 0.4, so all 30 miss it about once in four million.
	 */
	@GameTest(structure = GunsGameTests.ARENA, maxTicks = 200)
	public void quillDartsMakeTheirMarkBleed(GameTestHelper helper) {
		GunsGameTests.floor(helper);
		Mob pig = NetherGunsGameTests.sturdyPig(helper, new BlockPos(1, 2, 4));
		ServerPlayer shooter = GunsGameTests.shooter(helper, "quill_rifle", 0, pig, GameType.CREATIVE);
		for (int shot = 0; shot < 30; shot++) {
			helper.runAfterDelay(1 + 5L * shot, () -> GunShots.fire(shooter));
		}
		helper.runAfterDelay(160, () -> {
			helper.assertTrue(pig.getHealth() < 200.0F - 25.0F, "The Quill Rifle's darts did not land: the pig has " + pig.getHealth());
			helper.assertTrue(pig.hasEffect(JugcraftGuns.BLEEDING), "Thirty of the Quill Rifle's darts did not make the pig bleed");
			helper.succeed();
		});
	}
}

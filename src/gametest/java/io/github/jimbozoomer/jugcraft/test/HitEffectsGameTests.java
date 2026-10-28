package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.guns.GunShots;
import io.github.jimbozoomer.jugcraft.guns.JugcraftGuns;
import java.util.Map;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;

/**
 * In-game tests for the guns' hit effects (the hit-effects follow-up, docs/features/guns.md), through the server's entry
 * points as {@link GunsGameTests} does: the owner's slowness and darkness on the guns built, and the bleeding an arc now
 * carries. Shooters face south (+z); the creatures stand on pillars, level with the shooter's eye, so that a level shot
 * passes through them all.
 */
public class HitEffectsGameTests {
	/**
	 * The guns whose hits may give a vanilla effect are the owner's, at the owner's chances, lengths and levels: slowness
	 * from the Linesman's and the Stormlock Rifle's arcs and the Geode Cutter's beam, darkness at its second level from
	 * the Lament Rifle's bullets, and (the levitation follow-up) levitation from the End guns' bullets. No other gun's
	 * hits give one.
	 */
	@GameTest
	public void hitEffectsAreTheOwners(GameTestHelper helper) {
		helper.assertTrue(JugcraftGuns.HIT_EFFECTS.equals(Map.of("linesman", new JugcraftGuns.HitEffect("slowness", 0.4F, 200, 0),
				"stormlock_rifle", new JugcraftGuns.HitEffect("slowness", 0.4F, 200, 0), "geode_cutter", new JugcraftGuns.HitEffect("slowness", 0.75F, 100, 0),
				"lament_rifle", new JugcraftGuns.HitEffect("darkness", 0.25F, 100, 1), "chitin_smg", new JugcraftGuns.HitEffect("levitation", 0.3F, 100, 0),
				"hermit_rifle", new JugcraftGuns.HitEffect("levitation", 0.3F, 100, 0), "borer", new JugcraftGuns.HitEffect("levitation", 0.23F, 100, 0))),
				"The hit effects are not the owner's: " + JugcraftGuns.HIT_EFFECTS);
		helper.assertTrue(JugcraftGuns.HIT_EFFECTS.get("linesman").holder().equals(MobEffects.SLOWNESS)
				&& JugcraftGuns.HIT_EFFECTS.get("lament_rifle").holder().equals(MobEffects.DARKNESS)
				&& JugcraftGuns.HIT_EFFECTS.get("borer").holder().equals(MobEffects.LEVITATION), "A hit effect names the wrong vanilla effect");
		helper.assertTrue(JugcraftGuns.shot(JugcraftGuns.GUNS.get("linesman")).equals(JugcraftGuns.ARC)
				&& JugcraftGuns.shot(JugcraftGuns.GUNS.get("stormlock_rifle")).equals(JugcraftGuns.ARC)
				&& JugcraftGuns.shot(JugcraftGuns.GUNS.get("geode_cutter")).equals(JugcraftGuns.BEAM)
				&& JugcraftGuns.shot(JugcraftGuns.GUNS.get("lament_rifle")).equals("bullet"), "A gun with a hit effect fires the wrong thing");
		helper.assertTrue(JugcraftGuns.hitEffect(JugcraftGuns.GUNS.get("picket_rifle")) == null
				&& JugcraftGuns.hitEffect(JugcraftGuns.GUNS.get("caisson_pistol")) == null, "A gun without a hit effect has one");
		helper.succeed();
	}

	/**
	 * {@link GunShots#afflict} gives the effect at its chance, for its ticks and at its amplifier: a sure effect always
	 * lands, at the level and length it names; one of no chance and a gun with none never do. The Lament Rifle's lands
	 * just when the shooter's next roll is under its chance, as the same roll from a twin of the shooter's random shows.
	 * The twins' seeds are drawn from a seeded source: the first roll of sources seeded 0, 1, 2 and on is nearly the same
	 * (about 0.73 for each of the first forty), so near seeds would never roll under a quarter.
	 */
	@GameTest(structure = GunsGameTests.ARENA)
	public void afflictGivesItsChance(GameTestHelper helper) {
		GunsGameTests.floor(helper);
		Mob sure = NetherGunsGameTests.sturdyPig(helper, new BlockPos(2, 2, 4));
		Mob never = NetherGunsGameTests.sturdyPig(helper, new BlockPos(4, 2, 4));
		ServerPlayer shooter = GunsGameTests.shooter(helper, "lament_rifle", 1, sure, GameType.SURVIVAL);
		GunShots.afflict(sure, new JugcraftGuns.HitEffect("darkness", 1.0F, 100, 1), shooter, RandomSource.create(1L));
		helper.assertTrue(sure.hasEffect(MobEffects.DARKNESS) && sure.getEffect(MobEffects.DARKNESS).getAmplifier() == 1
				&& sure.getEffect(MobEffects.DARKNESS).getDuration() == 100, "A sure darkness did not land at level 2 for 100 ticks");
		GunShots.afflict(never, new JugcraftGuns.HitEffect("slowness", 0.0F, 100, 0), shooter, RandomSource.create(1L));
		GunShots.afflict(never, null, shooter, RandomSource.create(1L));
		helper.assertTrue(!never.hasEffect(MobEffects.SLOWNESS) && !never.hasEffect(MobEffects.DARKNESS), "An effect of no chance landed");
		JugcraftGuns.HitEffect lament = JugcraftGuns.HIT_EFFECTS.get("lament_rifle");
		boolean landed = false;
		boolean missed = false;
		RandomSource seeds = RandomSource.create(42L);
		for (int draw = 0; draw < 40 && !(landed && missed); draw++) {
			long seed = seeds.nextLong();
			boolean under = RandomSource.create(seed).nextFloat() < lament.chance();
			if (under ? landed : missed) {
				continue;
			}
			Mob pig = NetherGunsGameTests.sturdyPig(helper, new BlockPos(under ? 6 : 8, 2, 4));
			GunShots.afflict(pig, lament, shooter, RandomSource.create(seed));
			helper.assertTrue(pig.hasEffect(MobEffects.DARKNESS) == under,
					"The Lament Rifle's darkness " + (under ? "missed on" : "landed on") + " a roll " + (under ? "under" : "over") + " its chance");
			landed |= under;
			missed |= !under;
		}
		helper.assertTrue(landed && missed, "Forty seeds did not give both a roll under the Lament Rifle's chance and one over it");
		helper.succeed();
	}

	/**
	 * The Geode Cutter, fired 12 times through two sturdy pigs in a line by a shooter in creative (no charges spent), hurts
	 * and slows them both, at the first level and for at most 100 ticks: each hit's chance is 0.75, so a pig that 12 hits
	 * all miss is about one in seventeen million. A bedrock block behind them stops the beam, uncut.
	 */
	@GameTest(structure = GunsGameTests.ARENA, maxTicks = 60)
	public void geodeCutterBeamSlows(GameTestHelper helper) {
		GunsGameTests.floor(helper);
		Mob near = raised(helper, new BlockPos(1, 2, 3));
		Mob far = raised(helper, new BlockPos(1, 2, 5));
		for (int x = 0; x <= 2; x++) {
			for (int y = 2; y <= 4; y++) {
				helper.setBlock(new BlockPos(x, y, 7), Blocks.BEDROCK);
			}
		}
		ServerPlayer shooter = GunsGameTests.shooter(helper, "geode_cutter", 0, near, GameType.CREATIVE);
		shooter.setXRot(0.0F);
		int every = JugcraftGuns.SPECS.get("geode_cutter").interval() + 1;
		for (int shot = 0; shot < 12; shot++) {
			helper.runAfterDelay(1 + (long) every * shot, () -> GunShots.fire(shooter));
		}
		helper.runAfterDelay(2 + every * 12L, () -> {
			for (Mob pig : new Mob[] {near, far}) {
				helper.assertTrue(pig.getHealth() < 200.0F, "The Geode Cutter's beam did not reach a pig: it has " + pig.getHealth());
				helper.assertTrue(pig.hasEffect(MobEffects.SLOWNESS), "Twelve of the Geode Cutter's hits did not slow a pig");
				helper.assertTrue(pig.getEffect(MobEffects.SLOWNESS).getAmplifier() == 0 && pig.getEffect(MobEffects.SLOWNESS).getDuration() <= 100,
						"The Geode Cutter's slowness is not the first level for at most 100 ticks");
			}
			helper.assertBlockPresent(Blocks.BEDROCK, new BlockPos(1, 3, 7));
			helper.succeed();
		});
	}

	/**
	 * The Linesman, fired 30 times at a sturdy pig by a shooter in creative, slows it: each hit's chance is 0.4, so all 30
	 * miss it about once in four million.
	 */
	@GameTest(structure = GunsGameTests.ARENA, maxTicks = 240)
	public void linesmanArcSlows(GameTestHelper helper) {
		GunsGameTests.floor(helper);
		Mob pig = raised(helper, new BlockPos(1, 2, 4));
		ServerPlayer shooter = GunsGameTests.shooter(helper, "linesman", 0, pig, GameType.CREATIVE);
		int every = JugcraftGuns.SPECS.get("linesman").interval() + 1;
		for (int shot = 0; shot < 30; shot++) {
			helper.runAfterDelay(1 + (long) every * shot, () -> GunShots.fire(shooter));
		}
		helper.runAfterDelay(2 + every * 30L, () -> {
			helper.assertTrue(pig.getHealth() < 200.0F, "The Linesman's arcs did not land: the pig has " + pig.getHealth());
			helper.assertTrue(pig.hasEffect(MobEffects.SLOWNESS), "Thirty of the Linesman's arcs did not slow the pig");
			helper.succeed();
		});
	}

	/**
	 * The Caisson Pistol, fired 20 times by a shooter in creative at a sturdy pig with two more in a row beside it, two
	 * blocks apart, strikes all three (its arc leaps on twice) and makes all three bleed: each creature the arc hurts has
	 * the chance 0.5, so a pig that 20 hits all miss is about one in a million.
	 */
	@GameTest(structure = GunsGameTests.ARENA, maxTicks = 200)
	public void caissonArcBleeds(GameTestHelper helper) {
		GunsGameTests.floor(helper);
		Mob mark = raised(helper, new BlockPos(1, 2, 4));
		Mob next = raised(helper, new BlockPos(3, 2, 4));
		Mob last = raised(helper, new BlockPos(5, 2, 4));
		ServerPlayer shooter = GunsGameTests.shooter(helper, "caisson_pistol", 0, mark, GameType.CREATIVE);
		int every = JugcraftGuns.SPECS.get("caisson_pistol").interval() + 1;
		for (int shot = 0; shot < 20; shot++) {
			helper.runAfterDelay(1 + (long) every * shot, () -> GunShots.fire(shooter));
		}
		helper.runAfterDelay(2 + every * 20L, () -> {
			for (Mob pig : new Mob[] {mark, next, last}) {
				helper.assertTrue(pig.getHealth() < 200.0F, "The Caisson Pistol's arc did not reach a pig: it has " + pig.getHealth());
				helper.assertTrue(pig.hasEffect(JugcraftGuns.BLEEDING), "Twenty of the Caisson Pistol's arcs did not make a pig bleed");
			}
			helper.succeed();
		});
	}

	/** A still, sturdy pig of 200 health on a stone pillar, so it stands level with a shooter's eye. */
	private static Mob raised(GameTestHelper helper, BlockPos pillar) {
		helper.setBlock(pillar, Blocks.STONE);
		return NetherGunsGameTests.sturdyPig(helper, pillar.above());
	}
}

package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.guns.GunItem;
import io.github.jimbozoomer.jugcraft.guns.GunShots;
import io.github.jimbozoomer.jugcraft.guns.GunSpec;
import io.github.jimbozoomer.jugcraft.guns.JugcraftGuns;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;

/**
 * In-game tests for slice 15E's guns (docs/features/guns.md), the owner's last three diamond-steel guns, through the
 * server's entry points as {@link DiamondSteelGunsIVGameTests} does: the Maelstrom Gun, an automatic gun of microjets
 * loaded three at a time; the Ermine Rifle, a lever-action beam rifle; and the Geode Cutter, a cutting beam that pierces
 * armour. Each is held in both hands. Shooters face south (+z).
 */
public class DiamondSteelGunsVGameTests {
	/** The slice's guns, in the order the creative tab shows them. */
	static final List<String> GUNS = List.of("maelstrom_gun", "ermine_rifle", "geode_cutter");

	/**
	 * Each is registered with its numbers and its recipe loads, held in both hands. The Maelstrom Gun fires microjets, a
	 * new round that leaves a case, as long as the trigger is held, twenty-four loaded three a step, and takes no
	 * attachment. The Ermine Rifle fires a beam a pull from Energy Cells, fifteen a magazine, reaching short of the Lantern
	 * Musket, and takes the owner's stocks, grips, bayonets and scopes. The Geode Cutter fires a beam as long as the
	 * trigger is held, fifty a magazine, piercing four points of armour, and takes no attachment.
	 */
	@GameTest
	public void diamondSteelGunsVAreRegistered(GameTestHelper helper) {
		for (String name : GUNS) {
			GunSpec spec = JugcraftGuns.SPECS.get(name);
			GunItem gun = JugcraftGuns.GUNS.get(name);
			helper.assertTrue(spec != null && gun != null && gun.spec() == spec, name + " is not registered with its numbers");
			helper.assertTrue(!JugcraftGuns.ignites(gun) && !JugcraftGuns.levitates(gun) && JugcraftGuns.bleed(gun) == null
					&& JugcraftGuns.burst(gun) == null && JugcraftGuns.knockback(gun) == 0.0F, name + " sets alight, lifts, bleeds, bursts or knocks back");
			helper.assertTrue(!JugcraftGuns.ONE_HANDED.contains(name) && !JugcraftGuns.CARBINES.contains(name),
					name + " is not held in both hands");
			helper.assertTrue(helper.getLevel().recipeAccess().byKey(ResourceKey.create(Registries.RECIPE, Jugcraft.id(name))).isPresent(),
					"The " + name + " recipe does not load");
		}
		GunItem maelstrom = JugcraftGuns.GUNS.get("maelstrom_gun");
		GunSpec maelstromSpec = maelstrom.spec();
		helper.assertTrue(maelstromSpec.ammo().equals("microjet") && JugcraftGuns.shot(maelstrom).equals("bullet") && maelstromSpec.auto()
				&& maelstromSpec.byShell() && maelstromSpec.capacity() == 24 && JugcraftGuns.loadStep(maelstrom) == 3,
				"The Maelstrom Gun is not an automatic gun of twenty-four microjets, loaded three a step");
		helper.assertTrue(JugcraftGuns.ROUNDS.get("microjet") != null && JugcraftGuns.CASING_AMMO.contains("microjet")
				&& helper.getLevel().recipeAccess().byKey(ResourceKey.create(Registries.RECIPE, Jugcraft.id("microjet"))).isPresent(),
				"The Microjet is not a round with a case and a recipe");
		helper.assertTrue(maelstromSpec.reloadTicks(24, 3) == maelstromSpec.shellStart() + 8 * maelstromSpec.shellEach() + maelstromSpec.shellFinish()
				&& maelstromSpec.reloadTicks(7, 3) == maelstromSpec.reloadTicks(9, 3), "Loading three a step does not take a step's time for each three");
		GunItem ermine = JugcraftGuns.GUNS.get("ermine_rifle");
		GunSpec ermineSpec = ermine.spec();
		helper.assertTrue(JugcraftGuns.shot(ermine).equals(JugcraftGuns.BEAM) && JugcraftGuns.ammo(ermineSpec) == JugcraftGuns.ENERGY_CELL
				&& JugcraftGuns.charge(ermine) == 400 && !ermineSpec.auto() && ermineSpec.capacity() == 15 && !ermineSpec.byShell()
				&& JugcraftGuns.piercing(ermine) == 0.0F, "The Ermine Rifle is not a beam a pull on Energy Cells, fifteen a magazine");
		helper.assertTrue(ermineSpec.range() < JugcraftGuns.SPECS.get("lantern_musket").range(),
				"The Ermine Rifle reaches as far as the Lantern Musket");
		List<String> ermineTakes = JugcraftGuns.ACCEPTS.get("ermine_rifle");
		helper.assertTrue(ermineTakes.equals(JugcraftGuns.ACCEPTS.get("auger_rifle")), "The Ermine Rifle takes " + ermineTakes);
		GunItem geode = JugcraftGuns.GUNS.get("geode_cutter");
		GunSpec geodeSpec = geode.spec();
		helper.assertTrue(JugcraftGuns.shot(geode).equals(JugcraftGuns.BEAM) && JugcraftGuns.ammo(geodeSpec) == JugcraftGuns.ENERGY_CELL
				&& JugcraftGuns.charge(geode) == 120 && geodeSpec.auto() && geodeSpec.capacity() == 50 && JugcraftGuns.piercing(geode) == 4.0F,
				"The Geode Cutter is not an automatic beam on Energy Cells, fifty a magazine, piercing four points of armour");
		helper.assertTrue(!JugcraftGuns.ACCEPTS.containsKey("maelstrom_gun") && !JugcraftGuns.ACCEPTS.containsKey("geode_cutter"),
				"The Maelstrom Gun or the Geode Cutter takes an attachment");
		helper.succeed();
	}

	/**
	 * An empty Maelstrom Gun with seven microjets loads three at its first step's time and not before, six at its second,
	 * and the last one at its third; the reload then ends, none are left, and a shot lands its damage on the pig.
	 */
	@GameTest(structure = GunsGameTests.ARENA, maxTicks = 120)
	public void maelstromGunLoadsThreeAStep(GameTestHelper helper) {
		GunsGameTests.floor(helper);
		Mob pig = NetherGunsGameTests.sturdyPig(helper, new BlockPos(1, 2, 4));
		GunSpec spec = JugcraftGuns.SPECS.get("maelstrom_gun");
		Item microjets = JugcraftGuns.ROUNDS.get("microjet");
		ServerPlayer shooter = GunsGameTests.shooter(helper, "maelstrom_gun", 0, pig, GameType.SURVIVAL);
		shooter.getInventory().setItem(1, new ItemStack(microjets, 7));
		helper.assertTrue(GunShots.reload(shooter), "The empty Maelstrom Gun did not start to reload");
		int first = spec.shellStart() + spec.shellEach();
		helper.runAfterDelay(first - 2, () -> helper.assertTrue(GunItem.loaded(shooter.getMainHandItem()) == 0,
				"The Maelstrom Gun loaded before its first step's time"));
		helper.runAfterDelay(first + 1, () -> helper.assertTrue(GunItem.loaded(shooter.getMainHandItem()) == 3,
				"The Maelstrom Gun had " + GunItem.loaded(shooter.getMainHandItem()) + " microjets in after its first step, not 3"));
		helper.runAfterDelay(first + spec.shellEach() + 1, () -> helper.assertTrue(GunItem.loaded(shooter.getMainHandItem()) == 6,
				"The Maelstrom Gun had " + GunItem.loaded(shooter.getMainHandItem()) + " microjets in after its second step, not 6"));
		helper.runAfterDelay(spec.reloadTicks(7, 3) + 2, () -> {
			helper.assertTrue(GunItem.loaded(shooter.getMainHandItem()) == 7, "The Maelstrom Gun holds "
					+ GunItem.loaded(shooter.getMainHandItem()) + " microjets after its reload, not 7");
			helper.assertTrue(GunShots.count(shooter.getInventory(), microjets) == 0, "The reload left microjets in the inventory");
			helper.assertFalse(GunShots.reloading(shooter), "The Maelstrom Gun was still reloading with nothing left to load");
			helper.assertTrue(GunShots.fire(shooter), "The loaded Maelstrom Gun did not fire");
			helper.assertTrue(Math.abs(200.0F - pig.getHealth() - spec.damage()) < 1.0E-3F,
					"The microjet took " + (200.0F - pig.getHealth()) + ", not " + spec.damage());
			helper.succeed();
		});
	}

	/**
	 * Four pigs in armour of 10 points. The Geode Cutter's beam hits one and the Ermine Rifle's another; the other two
	 * take the same damage as plain zaps. The Geode Cutter's beam took more than its zap, through the armour, and no more
	 * than its damage; the Ermine Rifle's took what its zap did. Each spends a round.
	 */
	@GameTest(structure = GunsGameTests.ARENA, maxTicks = 40)
	public void geodeCutterBeamPiercesArmour(GameTestHelper helper) {
		GunsGameTests.floor(helper);
		Mob cut = armouredPig(helper, new BlockPos(1, 2, 4));
		Mob shot = armouredPig(helper, new BlockPos(5, 2, 4));
		Mob cutZapped = armouredPig(helper, new BlockPos(9, 2, 4));
		Mob shotZapped = armouredPig(helper, new BlockPos(13, 2, 4));
		ServerPlayer cutter = NetherGunsGameTests.shooter(helper, "geode_cutter", new BlockPos(1, 2, 1), cut);
		ServerPlayer rifle = NetherGunsGameTests.shooter(helper, "ermine_rifle", new BlockPos(5, 2, 1), shot);
		helper.assertTrue(GunShots.fire(cutter) && GunShots.fire(rifle), "The Geode Cutter or the Ermine Rifle did not fire");
		helper.assertTrue(GunItem.loaded(cutter.getMainHandItem()) == 0 && GunItem.loaded(rifle.getMainHandItem()) == 0,
				"A beam did not spend its round");
		DamageSource zap = new DamageSource(helper.getLevel().registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE)
				.getOrThrow(JugcraftGuns.ZAP_DAMAGE), cutter, cutter);
		float cutDamage = JugcraftGuns.SPECS.get("geode_cutter").damage();
		float shotDamage = JugcraftGuns.SPECS.get("ermine_rifle").damage();
		cutZapped.hurtServer(helper.getLevel(), zap, cutDamage);
		shotZapped.hurtServer(helper.getLevel(), zap, shotDamage);
		float pierced = 200.0F - cut.getHealth();
		float stopped = 200.0F - cutZapped.getHealth();
		helper.assertTrue(pierced > stopped + 0.3F && pierced <= cutDamage + 1.0E-3F,
				"Through 10 points of armour the Geode Cutter's beam took " + pierced + ", a plain zap of " + cutDamage + " took " + stopped);
		float rifleTook = 200.0F - shot.getHealth();
		float rifleZap = 200.0F - shotZapped.getHealth();
		helper.assertTrue(rifleTook > 0.0F && Math.abs(rifleTook - rifleZap) < 1.0E-3F,
				"The Ermine Rifle's beam took " + rifleTook + " through 10 points of armour, a plain zap of " + shotDamage + " took " + rifleZap);
		helper.succeed();
	}

	/** A sturdy pig of 200 health in 10 points of armour. */
	private static Mob armouredPig(GameTestHelper helper, BlockPos at) {
		Mob pig = NetherGunsGameTests.sturdyPig(helper, at);
		pig.getAttribute(Attributes.ARMOR).setBaseValue(10.0);
		return pig;
	}
}

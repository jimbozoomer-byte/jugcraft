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
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;

/**
 * In-game tests for slice 15D's guns (docs/features/guns.md), three more of the owner's diamond-steel guns, through the
 * server's entry points as {@link DiamondSteelGunsIIIGameTests} does: the Auger Rifle, two barrels loaded a ball at a
 * time; the Auger Shotgun, the same made over to fire shot shells; and the Tumbler Rifle, which fires a burst of three to
 * a pull. Each is held in both hands. Shooters face south (+z).
 */
public class DiamondSteelGunsIVGameTests {
	/** The slice's guns, in the order the creative tab shows them. */
	static final List<String> GUNS = List.of("auger_rifle", "auger_shotgun", "tumbler_rifle");

	/**
	 * Each is registered with its numbers and its recipe loads, held in both hands, firing plain bullets. The Auger Rifle
	 * is two paper cartridges loaded a round at a time; the Auger Shotgun two buckshot shells, eight pellets a barrel that
	 * stray as far aimed as from the hip, knocking back what they hit; both take the owner's stocks, grips, bayonets and
	 * scopes, and no barrel attachment or magazine. The Tumbler Rifle is twenty-one rifle rounds, a burst of three to a
	 * pull, piercing three points of armour, and takes what the Quill Rifle takes and the barrel attachments.
	 */
	@GameTest
	public void diamondSteelGunsIVAreRegistered(GameTestHelper helper) {
		for (String name : GUNS) {
			GunSpec spec = JugcraftGuns.SPECS.get(name);
			GunItem gun = JugcraftGuns.GUNS.get(name);
			helper.assertTrue(spec != null && gun != null && gun.spec() == spec, name + " is not registered with its numbers");
			helper.assertTrue(JugcraftGuns.shot(gun).equals("bullet") && !JugcraftGuns.ignites(gun) && !JugcraftGuns.levitates(gun)
					&& JugcraftGuns.bleed(gun) == null, name + " does not fire plain bullets");
			helper.assertTrue(!JugcraftGuns.ONE_HANDED.contains(name) && !JugcraftGuns.CARBINES.contains(name),
					name + " is not held in both hands");
			helper.assertTrue(!spec.auto() && !JugcraftGuns.SILENCED.contains(name), name + " is not a gun of one pull a shot or burst");
			helper.assertTrue(helper.getLevel().recipeAccess().byKey(ResourceKey.create(Registries.RECIPE, Jugcraft.id(name))).isPresent(),
					"The " + name + " recipe does not load");
		}
		GunSpec auger = JugcraftGuns.SPECS.get("auger_rifle");
		helper.assertTrue(auger.ammo().equals("paper_cartridge") && auger.capacity() == 2 && auger.byShell() && auger.pellets() == 1,
				"The Auger Rifle is not two paper cartridges loaded a round at a time");
		GunSpec shotgun = JugcraftGuns.SPECS.get("auger_shotgun");
		helper.assertTrue(shotgun.ammo().equals("buckshot_shell") && shotgun.capacity() == 2 && shotgun.byShell()
				&& shotgun.pellets() == 8 && shotgun.aimSpread() == shotgun.hipSpread(),
				"The Auger Shotgun is not two buckshot shells loaded a shell at a time, eight pellets as wide aimed as from the hip");
		helper.assertTrue(JugcraftGuns.knockback(JugcraftGuns.GUNS.get("auger_shotgun")) == 0.65F
				&& JugcraftGuns.knockback(JugcraftGuns.GUNS.get("auger_rifle")) == 0.0F, "The Auger Shotgun does not knock back 0.65 further");
		for (String name : List.of("auger_rifle", "auger_shotgun")) {
			List<String> takes = JugcraftGuns.ACCEPTS.get(name);
			helper.assertTrue(takes.containsAll(List.of("light_stock", "weighted_stock", "wooden_stock", "light_grip", "vertical_grip",
					"iron_bayonet", "long_scope", "reflex_sight")) && takes.stream().noneMatch(BrassSidearmsGameTests.BARREL::contains)
					&& !takes.contains("extended_magazine"), "The " + name + " takes " + takes);
			helper.assertTrue(JugcraftGuns.burst(JugcraftGuns.GUNS.get(name)) == null, "The " + name + " fires a burst");
		}
		GunSpec tumbler = JugcraftGuns.SPECS.get("tumbler_rifle");
		helper.assertTrue(tumbler.ammo().equals("rifle_round") && tumbler.capacity() == 21 && !tumbler.byShell() && tumbler.interval() == 3,
				"The Tumbler Rifle is not a magazine of twenty-one rifle rounds, every third tick");
		JugcraftGuns.Burst burst = JugcraftGuns.burst(JugcraftGuns.GUNS.get("tumbler_rifle"));
		helper.assertTrue(burst != null && burst.rounds() == 3 && burst.rest() == 5 && burst.cycle(tumbler) == 14,
				"The Tumbler Rifle does not fire a burst of three, resting 5 ticks");
		helper.assertTrue(JugcraftGuns.piercing(JugcraftGuns.GUNS.get("tumbler_rifle")) == 3.0F, "The Tumbler Rifle does not pierce three points of armour");
		List<String> tumblerTakes = JugcraftGuns.ACCEPTS.get("tumbler_rifle");
		helper.assertTrue(tumblerTakes.containsAll(JugcraftGuns.ACCEPTS.get("quill_rifle")) && tumblerTakes.containsAll(BrassSidearmsGameTests.BARREL),
				"The Tumbler Rifle takes " + tumblerTakes);
		helper.succeed();
	}

	/** An empty Auger Rifle with three paper cartridges loads one, then the other, at its rounds' times, and leaves one. */
	@GameTest(structure = GunsGameTests.ARENA, maxTicks = 120)
	public void augerRifleLoadsARoundAtATime(GameTestHelper helper) {
		loadsARoundAtATime(helper, "auger_rifle");
	}

	/** An empty Auger Shotgun with three buckshot shells loads one, then the other, at its shells' times, and leaves one. */
	@GameTest(structure = GunsGameTests.ARENA, maxTicks = 120)
	public void augerShotgunLoadsAShellAtATime(GameTestHelper helper) {
		loadsARoundAtATime(helper, "auger_shotgun");
	}

	/**
	 * The Auger Shotgun at a pig two blocks off: a pull lands its pellets as one hit of a whole number of 3.5-point
	 * pellets, up to all eight, and spends a shell; the second barrel fires on the next pull.
	 */
	@GameTest(structure = GunsGameTests.ARENA, maxTicks = 60)
	public void augerShotgunFiresBothBarrels(GameTestHelper helper) {
		GunsGameTests.floor(helper);
		Mob pig = NetherGunsGameTests.sturdyPig(helper, new BlockPos(1, 2, 3));
		GunSpec spec = JugcraftGuns.SPECS.get("auger_shotgun");
		ServerPlayer shooter = GunsGameTests.shooter(helper, "auger_shotgun", 2, pig, GameType.SURVIVAL);
		helper.assertTrue(GunShots.fire(shooter), "The loaded Auger Shotgun did not fire");
		float took = 200.0F - pig.getHealth();
		float pellets = took / spec.damage();
		helper.assertTrue(took > 0.0F && pellets <= spec.pellets() + 1.0E-3F && Math.abs(pellets - Math.round(pellets)) < 1.0E-3F,
				"The shot took " + took + ", not a whole number of " + spec.damage() + "-point pellets up to " + spec.pellets());
		helper.runAfterDelay(spec.interval() + 1, () -> {
			helper.assertTrue(GunShots.fire(shooter), "The Auger Shotgun's second barrel did not fire");
			helper.assertTrue(GunItem.loaded(shooter.getMainHandItem()) == 0, "Two pulls did not empty both barrels");
			helper.assertFalse(GunShots.fire(shooter), "The empty Auger Shotgun fired");
			helper.succeed();
		});
	}

	/**
	 * The Tumbler Rifle fires a burst of three, a shot every third tick; a fourth shot at its interval is refused, and
	 * once the burst's rest has passed the next burst fires whole. Each shot spends a round.
	 */
	@GameTest(structure = GunsGameTests.ARENA, maxTicks = 80)
	public void tumblerRifleFiresInBursts(GameTestHelper helper) {
		GunsGameTests.floor(helper);
		Mob pig = NetherGunsGameTests.sturdyPig(helper, new BlockPos(1, 2, 6));
		GunSpec spec = JugcraftGuns.SPECS.get("tumbler_rifle");
		JugcraftGuns.Burst burst = JugcraftGuns.burst(JugcraftGuns.GUNS.get("tumbler_rifle"));
		ServerPlayer shooter = GunsGameTests.shooter(helper, "tumbler_rifle", 21, pig, GameType.SURVIVAL);
		for (int shot = 0; shot < burst.rounds(); shot++) {
			int n = shot;
			helper.runAfterDelay(1 + spec.interval() * shot, () -> helper.assertTrue(GunShots.fire(shooter),
					"The Tumbler Rifle did not fire shot " + (n + 1) + " of its burst"));
		}
		helper.runAfterDelay(1 + spec.interval() * burst.rounds(), () -> helper.assertFalse(GunShots.fire(shooter),
				"The Tumbler Rifle fired a fourth shot before its burst's rest had passed"));
		int next = 1 + burst.cycle(spec);
		for (int shot = 0; shot < burst.rounds(); shot++) {
			int n = shot;
			helper.runAfterDelay(next + spec.interval() * shot, () -> helper.assertTrue(GunShots.fire(shooter),
					"The Tumbler Rifle did not fire shot " + (n + 1) + " of its second burst"));
		}
		helper.runAfterDelay(next + spec.interval() * burst.rounds(), () -> {
			helper.assertTrue(GunItem.loaded(shooter.getMainHandItem()) == 21 - 2 * burst.rounds(),
					"Two bursts left " + GunItem.loaded(shooter.getMainHandItem()) + " rounds, not " + (21 - 2 * burst.rounds()));
			helper.succeed();
		});
	}

	/** An empty gun loaded a round at a time, with three of its rounds, loads one, then the other, and leaves one. */
	private static void loadsARoundAtATime(GameTestHelper helper, String name) {
		GunsGameTests.floor(helper);
		Mob pig = NetherGunsGameTests.sturdyPig(helper, new BlockPos(1, 2, 4));
		GunSpec spec = JugcraftGuns.SPECS.get(name);
		Item rounds = JugcraftGuns.ROUNDS.get(spec.ammo());
		ServerPlayer shooter = GunsGameTests.shooter(helper, name, 0, pig, GameType.SURVIVAL);
		shooter.getInventory().setItem(1, new ItemStack(rounds, 3));
		helper.assertTrue(GunShots.reload(shooter), "The empty " + name + " did not start to reload");
		helper.runAfterDelay(spec.shellStart() + spec.shellEach() + 1, () -> helper.assertTrue(GunItem.loaded(shooter.getMainHandItem()) == 1,
				"The " + name + " had not one round in after its first round's time"));
		helper.runAfterDelay(spec.reloadTicks(spec.capacity()) + 2, () -> {
			helper.assertTrue(GunItem.loaded(shooter.getMainHandItem()) == 2, "The " + name + " holds "
					+ GunItem.loaded(shooter.getMainHandItem()) + " rounds after its reload, not 2");
			helper.assertTrue(GunShots.count(shooter.getInventory(), rounds) == 1, "The reload did not leave one round in the inventory");
			helper.assertFalse(GunShots.reloading(shooter), "The " + name + " was still reloading with both barrels full");
			helper.assertTrue(GunShots.fire(shooter), "The loaded " + name + " did not fire");
			helper.succeed();
		});
	}
}

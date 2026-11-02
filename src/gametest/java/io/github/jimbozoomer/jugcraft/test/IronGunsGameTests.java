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
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;

/**
 * In-game tests for slice 16A's guns (docs/features/guns.md), three iron guns from the owner's 23 October upload, through
 * the server's entry points as {@link DiamondSteelGunsIVGameTests} does: the Hush Pistol, silenced the length of its
 * barrel and held in one hand; the Salvo Rifle, which fires a burst of three to a pull; and the Highball Repeater, the
 * Highball Rifle made over into a repeater loaded a round at a time. Shooters face south (+z).
 */
public class IronGunsGameTests {
	/** The slice's guns, in the order the creative tab shows them. */
	static final List<String> GUNS = List.of("hush_pistol", "salvo_rifle", "highball_repeater");

	/**
	 * Each is registered with its numbers and its recipe loads, firing plain bullets a pull at a time (or a burst), none
	 * knocking back further. The Hush Pistol is eight light rounds from a magazine, held in one hand (not a carbine with a
	 * stock), silenced, taking only the magazines. The Salvo Rifle is eighteen rifle rounds, a burst of three to a pull
	 * resting 5 ticks, held in both hands and taking what the Tumbler Rifle takes. The Highball Repeater is six rifle
	 * rounds loaded a round at a time, as true from the hip as aimed, piercing two points of armour, held in both hands and
	 * taking the barrel attachments, stocks, grips, bayonets and scopes but no magazine.
	 */
	@GameTest
	public void ironGunsAreRegistered(GameTestHelper helper) {
		for (String name : GUNS) {
			GunSpec spec = JugcraftGuns.SPECS.get(name);
			GunItem gun = JugcraftGuns.GUNS.get(name);
			helper.assertTrue(spec != null && gun != null && gun.spec() == spec, name + " is not registered with its numbers");
			helper.assertTrue(JugcraftGuns.shot(gun).equals("bullet") && !JugcraftGuns.ignites(gun) && !JugcraftGuns.levitates(gun)
					&& JugcraftGuns.bleed(gun) == null && JugcraftGuns.knockback(gun) == 0.0F, name + " does not fire plain bullets");
			helper.assertTrue(!spec.auto(), name + " fires as long as the trigger is held");
			helper.assertTrue(helper.getLevel().recipeAccess().byKey(ResourceKey.create(Registries.RECIPE, Jugcraft.id(name))).isPresent(),
					"The " + name + " recipe does not load");
		}
		GunSpec hush = JugcraftGuns.SPECS.get("hush_pistol");
		helper.assertTrue(hush.ammo().equals("light_round") && hush.capacity() == 8 && !hush.byShell(),
				"The Hush Pistol is not a magazine of eight light rounds");
		helper.assertTrue(JugcraftGuns.ONE_HANDED.contains("hush_pistol") && !JugcraftGuns.CARBINES.contains("hush_pistol")
				&& JugcraftGuns.SILENCED.contains("hush_pistol"), "The Hush Pistol is not a silenced pistol held in one hand");
		helper.assertTrue(JugcraftGuns.ACCEPTS.get("hush_pistol").equals(List.of("extended_magazine", "speed_magazine")),
				"The Hush Pistol takes " + JugcraftGuns.ACCEPTS.get("hush_pistol"));
		GunSpec salvo = JugcraftGuns.SPECS.get("salvo_rifle");
		helper.assertTrue(salvo.ammo().equals("rifle_round") && salvo.capacity() == 18 && !salvo.byShell() && salvo.interval() == 3,
				"The Salvo Rifle is not a magazine of eighteen rifle rounds, every third tick");
		JugcraftGuns.Burst burst = JugcraftGuns.burst(JugcraftGuns.GUNS.get("salvo_rifle"));
		helper.assertTrue(burst != null && burst.rounds() == 3 && burst.rest() == 5 && burst.cycle(salvo) == 14,
				"The Salvo Rifle does not fire a burst of three, resting 5 ticks");
		helper.assertTrue(JugcraftGuns.ACCEPTS.get("salvo_rifle").equals(JugcraftGuns.ACCEPTS.get("tumbler_rifle")),
				"The Salvo Rifle takes " + JugcraftGuns.ACCEPTS.get("salvo_rifle"));
		GunSpec repeater = JugcraftGuns.SPECS.get("highball_repeater");
		helper.assertTrue(repeater.ammo().equals("rifle_round") && repeater.capacity() == 6 && repeater.byShell()
				&& repeater.aimSpread() == repeater.hipSpread(),
				"The Highball Repeater is not six rifle rounds loaded a round at a time, as true from the hip as aimed");
		helper.assertTrue(JugcraftGuns.piercing(JugcraftGuns.GUNS.get("highball_repeater")) == 2.0F,
				"The Highball Repeater does not pierce two points of armour");
		List<String> repeaterTakes = JugcraftGuns.ACCEPTS.get("highball_repeater");
		helper.assertTrue(repeaterTakes.containsAll(BrassSidearmsGameTests.BARREL) && repeaterTakes.containsAll(BrassSidearmsGameTests.STOCKS)
				&& repeaterTakes.containsAll(FrontierGunsIVGameTests.BAYONETS) && repeaterTakes.containsAll(BrassSidearmsGameTests.SIGHTS)
				&& repeaterTakes.containsAll(List.of("light_grip", "tactical_grip")) && !repeaterTakes.contains("extended_magazine"),
				"The Highball Repeater takes " + repeaterTakes);
		for (String name : List.of("salvo_rifle", "highball_repeater")) {
			helper.assertTrue(!JugcraftGuns.ONE_HANDED.contains(name) && !JugcraftGuns.SILENCED.contains(name),
					"The " + name + " is held in one hand, or silenced");
		}
		helper.succeed();
	}

	/**
	 * The Hush Pistol at a pig four blocks off hits it for its damage and spends a round; with a Warden Pistol in the
	 * other hand it is one of two guns at once.
	 */
	@GameTest(structure = GunsGameTests.ARENA, maxTicks = 40)
	public void hushPistolFiresFromOneHand(GameTestHelper helper) {
		GunsGameTests.floor(helper);
		Mob pig = NetherGunsGameTests.sturdyPig(helper, new BlockPos(1, 2, 5));
		GunSpec spec = JugcraftGuns.SPECS.get("hush_pistol");
		ServerPlayer shooter = GunsGameTests.shooter(helper, "hush_pistol", 8, pig, GameType.SURVIVAL);
		helper.assertTrue(GunShots.fire(shooter), "The loaded Hush Pistol did not fire");
		helper.assertTrue(Math.abs(200.0F - pig.getHealth() - spec.damage()) < 1.0E-3F,
				"The Hush Pistol's shot took " + (200.0F - pig.getHealth()) + ", not " + spec.damage());
		helper.assertTrue(GunItem.loaded(shooter.getMainHandItem()) == 7, "The shot did not spend one of eight rounds");
		ItemStack warden = new ItemStack(JugcraftGuns.GUNS.get("warden_pistol"));
		shooter.setItemInHand(InteractionHand.OFF_HAND, warden);
		helper.assertTrue(GunItem.dual(shooter), "The Hush Pistol and a Warden Pistol are not two guns at once");
		helper.succeed();
	}

	/**
	 * The Salvo Rifle fires a burst of three, a shot every third tick; a fourth shot at its interval is refused, and once
	 * the burst's rest has passed the next burst fires whole. Each shot spends a round.
	 */
	@GameTest(structure = GunsGameTests.ARENA, maxTicks = 80)
	public void salvoRifleFiresInBursts(GameTestHelper helper) {
		GunsGameTests.floor(helper);
		Mob pig = NetherGunsGameTests.sturdyPig(helper, new BlockPos(1, 2, 6));
		GunSpec spec = JugcraftGuns.SPECS.get("salvo_rifle");
		JugcraftGuns.Burst burst = JugcraftGuns.burst(JugcraftGuns.GUNS.get("salvo_rifle"));
		ServerPlayer shooter = GunsGameTests.shooter(helper, "salvo_rifle", 18, pig, GameType.SURVIVAL);
		for (int shot = 0; shot < burst.rounds(); shot++) {
			int n = shot;
			helper.runAfterDelay(1 + spec.interval() * shot, () -> helper.assertTrue(GunShots.fire(shooter),
					"The Salvo Rifle did not fire shot " + (n + 1) + " of its burst"));
		}
		helper.runAfterDelay(1 + spec.interval() * burst.rounds(), () -> helper.assertFalse(GunShots.fire(shooter),
				"The Salvo Rifle fired a fourth shot before its burst's rest had passed"));
		int next = 1 + burst.cycle(spec);
		for (int shot = 0; shot < burst.rounds(); shot++) {
			int n = shot;
			helper.runAfterDelay(next + spec.interval() * shot, () -> helper.assertTrue(GunShots.fire(shooter),
					"The Salvo Rifle did not fire shot " + (n + 1) + " of its second burst"));
		}
		helper.runAfterDelay(next + spec.interval() * burst.rounds(), () -> {
			helper.assertTrue(GunItem.loaded(shooter.getMainHandItem()) == 18 - 2 * burst.rounds(),
					"Two bursts left " + GunItem.loaded(shooter.getMainHandItem()) + " rounds, not " + (18 - 2 * burst.rounds()));
			helper.succeed();
		});
	}

	/**
	 * An empty Highball Repeater with three rifle rounds loads one at its first round's time and all three by its reload's
	 * time for three, leaving none in the inventory and the reload over; a shot then fires.
	 */
	@GameTest(structure = GunsGameTests.ARENA, maxTicks = 120)
	public void highballRepeaterLoadsARoundAtATime(GameTestHelper helper) {
		GunsGameTests.floor(helper);
		Mob pig = NetherGunsGameTests.sturdyPig(helper, new BlockPos(1, 2, 4));
		GunSpec spec = JugcraftGuns.SPECS.get("highball_repeater");
		Item rounds = JugcraftGuns.ROUNDS.get(spec.ammo());
		ServerPlayer shooter = GunsGameTests.shooter(helper, "highball_repeater", 0, pig, GameType.SURVIVAL);
		shooter.getInventory().setItem(1, new ItemStack(rounds, 3));
		helper.assertTrue(GunShots.reload(shooter), "The empty Highball Repeater did not start to reload");
		helper.runAfterDelay(spec.shellStart() + spec.shellEach() + 1, () -> helper.assertTrue(GunItem.loaded(shooter.getMainHandItem()) == 1,
				"The Highball Repeater had not one round in after its first round's time"));
		helper.runAfterDelay(spec.reloadTicks(3) + 2, () -> {
			helper.assertTrue(GunItem.loaded(shooter.getMainHandItem()) == 3, "The Highball Repeater holds "
					+ GunItem.loaded(shooter.getMainHandItem()) + " rounds after loading three, not 3");
			helper.assertTrue(GunShots.count(shooter.getInventory(), rounds) == 0, "The reload left rounds in the inventory");
			helper.assertFalse(GunShots.reloading(shooter), "The Highball Repeater was still reloading with no rounds left to load");
			helper.assertTrue(GunShots.fire(shooter), "The loaded Highball Repeater did not fire");
			helper.succeed();
		});
	}
}

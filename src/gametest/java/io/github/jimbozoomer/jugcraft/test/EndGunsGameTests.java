package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.guns.GunItem;
import io.github.jimbozoomer.jugcraft.guns.GunShots;
import io.github.jimbozoomer.jugcraft.guns.GunSpec;
import io.github.jimbozoomer.jugcraft.guns.JugcraftGuns;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.GameType;

/**
 * In-game tests for the End guns (slice 12A, docs/features/guns.md), through the server's entry points as
 * {@link GunsGameTests} does: the Chitin SMG, the Hermit Rifle and the Borer are automatics loaded a magazine at a time;
 * each lands its shot on what it aims at and may make it float (at the owner's chances since the levitation follow-up).
 * Shooters face south (+z).
 */
public class EndGunsGameTests {
	/** The End guns, in the order the creative tab shows them. */
	static final List<String> GUNS = List.of("chitin_smg", "hermit_rifle", "borer");
	/** The scopes and the Laser Sight. */
	static final List<String> OPTICS = List.of("long_scope", "medium_scope", "reflex_sight", "laser_sight");

	/**
	 * Each is registered with its numbers and its recipe loads, and fires bullets that may make what they hurt float, its
	 * hit effect levitation; no other gun's do, and none of them sets what it hits alight. The Chitin SMG fires light rounds for as long as the trigger is
	 * held, from a magazine of twenty-four; the Hermit Rifle rifle rounds from twenty; the Borer rifle rounds from forty.
	 * The Chitin SMG takes the scopes and the Laser Sight; the Hermit Rifle the stocks too; the Borer, carried at the hip,
	 * nothing.
	 */
	@GameTest
	public void endGunsAreRegistered(GameTestHelper helper) {
		for (String name : GUNS) {
			GunSpec spec = JugcraftGuns.SPECS.get(name);
			GunItem gun = JugcraftGuns.GUNS.get(name);
			helper.assertTrue(spec != null && gun != null && gun.spec() == spec, name + " is not registered with its numbers");
			helper.assertTrue(JugcraftGuns.shot(gun).equals("bullet") && JugcraftGuns.levitates(gun) && !JugcraftGuns.ignites(gun),
					name + " does not fire bullets that make what they hit float, and only that");
			helper.assertTrue(JugcraftGuns.hitEffect(gun) != null && JugcraftGuns.hitEffect(gun).effect().equals("levitation"),
					name + "'s hit effect is not levitation: " + JugcraftGuns.hitEffect(gun));
			helper.assertTrue(spec.auto() && spec.pellets() == 1 && !spec.byShell() && !JugcraftGuns.ONE_HANDED.contains(name),
					name + " is not an automatic held in both hands, one bullet a shot, loaded a magazine at a time");
			helper.assertTrue(helper.getLevel().recipeAccess().byKey(ResourceKey.create(Registries.RECIPE, Jugcraft.id(name))).isPresent(),
					"The " + name + " recipe does not load");
		}
		helper.assertTrue(JugcraftGuns.LEVITATING.equals(Set.copyOf(GUNS)), "Guns besides the End guns make what they hit float: "
				+ JugcraftGuns.LEVITATING);
		helper.assertTrue(Collections.disjoint(JugcraftGuns.LEVITATING, JugcraftGuns.INCENDIARY),
				"A gun both sets what it hits alight and makes it float");
		GunSpec chitin = JugcraftGuns.SPECS.get("chitin_smg");
		GunSpec hermit = JugcraftGuns.SPECS.get("hermit_rifle");
		GunSpec borer = JugcraftGuns.SPECS.get("borer");
		helper.assertTrue(chitin.ammo().equals("light_round") && chitin.capacity() == 24,
				"The Chitin SMG is not a machine gun of twenty-four light rounds");
		helper.assertTrue(hermit.ammo().equals("rifle_round") && hermit.capacity() == 20, "The Hermit Rifle is not a rifle of twenty rifle rounds");
		helper.assertTrue(borer.ammo().equals("rifle_round") && borer.capacity() == 40, "The Borer is not a machine gun of forty rifle rounds");
		helper.assertTrue(JugcraftGuns.ACCEPTS.get("chitin_smg").equals(OPTICS), "The Chitin SMG takes " + JugcraftGuns.ACCEPTS.get("chitin_smg"));
		List<String> hermitTakes = new ArrayList<>(List.of("light_stock", "weighted_stock", "wooden_stock"));
		hermitTakes.addAll(OPTICS);
		helper.assertTrue(JugcraftGuns.ACCEPTS.get("hermit_rifle").equals(hermitTakes),
				"The Hermit Rifle takes " + JugcraftGuns.ACCEPTS.get("hermit_rifle"));
		helper.assertTrue(JugcraftGuns.ACCEPTS.getOrDefault("borer", List.of()).isEmpty(), "The Borer takes " + JugcraftGuns.ACCEPTS.get("borer"));
		helper.succeed();
	}

	/**
	 * Side by side, each fires its last round at a pig three blocks off and its one shot takes one round's damage; each
	 * then loads from the inventory, nothing in halfway through its reload and full as it ends, two rounds left in the
	 * inventory.
	 */
	@GameTest(structure = GunsGameTests.ARENA, maxTicks = 200)
	public void endGunsFireAndLoad(GameTestHelper helper) {
		NetherGunsGameTests.fireAndLoad(helper, GUNS);
	}

	/**
	 * Each End gun, fired from creative at a sturdy pig until a hit makes it float (at most {@value #SHOTS} shots, its
	 * interval and a tick apart; the Borer's 23% missing sixty times running is about one in seven million), lifts it at
	 * the first level for the owner's 100 ticks, and it rises; the gun fires no more once its pig floats. An Undertone
	 * Rifle's shot (slice 10D) leaves its pig standing. When the spell ends each lifted pig comes down to the floor again.
	 */
	@GameTest(structure = GunsGameTests.ARENA, maxTicks = 600)
	public void endGunsMakeWhatTheyHitFloat(GameTestHelper helper) {
		GunsGameTests.floor(helper);
		List<String> fired = new ArrayList<>(GUNS);
		fired.add("undertone_rifle");
		Mob[] pigs = new Mob[fired.size()];
		double[] floor = new double[fired.size()];
		double[] rose = new double[fired.size()];
		boolean[] lifted = new boolean[fired.size()];
		for (int i = 0; i < fired.size(); i++) {
			String name = fired.get(i);
			Mob pig = NetherGunsGameTests.sturdyPig(helper, new BlockPos(1 + 4 * i, 2, 4));
			pig.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000.0);   // sixty shots and a fall
			pig.setHealth(1000.0F);
			pigs[i] = pig;
			floor[i] = pig.getY();
			ServerPlayer shooter = NetherGunsGameTests.shooter(helper, name, new BlockPos(1 + 4 * i, 2, 1), pig);
			shooter.setGameMode(GameType.CREATIVE);   // no rounds spent
			int every = JugcraftGuns.SPECS.get(name).interval() + 1;
			int shots = JugcraftGuns.LEVITATING.contains(name) ? SHOTS : 1;
			int index = i;
			for (int shot = 0; shot < shots; shot++) {
				helper.runAfterDelay(1 + (long) every * shot, () -> {
					if (lifted[index]) {
						return;
					}
					helper.assertTrue(GunShots.fire(shooter), "The " + name + " did not fire");
					helper.assertTrue(pig.getHealth() < 1000.0F, "The " + name + "'s shot missed its pig");
					lifted[index] = pig.hasEffect(MobEffects.LEVITATION);
					helper.assertTrue(!lifted[index] || pig.getEffect(MobEffects.LEVITATION).getAmplifier() == 0
							&& pig.getEffect(MobEffects.LEVITATION).getDuration() == 100,
							"The " + name + " did not lift its pig at the first level for 100 ticks");
				});
			}
		}
		helper.onEachTick(() -> {
			for (int i = 0; i < pigs.length; i++) {
				rose[i] = Math.max(rose[i], pigs[i].getY() - floor[i]);
			}
		});
		helper.succeedWhen(() -> {
			for (int i = 0; i < fired.size(); i++) {
				String name = fired.get(i);
				if (JugcraftGuns.LEVITATING.contains(name)) {
					helper.assertTrue(lifted[i] && rose[i] > 0.5, "The " + name + "'s pig has not floated up: it rose " + rose[i]);
				} else {
					helper.assertTrue(!lifted[i] && rose[i] < 0.01, "The " + name + "'s pig rose " + rose[i] + " blocks");
				}
				helper.assertTrue(!pigs[i].hasEffect(MobEffects.LEVITATION) && pigs[i].onGround()
						&& Math.abs(pigs[i].getY() - floor[i]) < 0.01, "The " + name + "'s pig is not back on the floor");
			}
		});
	}

	/** How many shots each End gun has to make its pig float. */
	private static final int SHOTS = 60;
}

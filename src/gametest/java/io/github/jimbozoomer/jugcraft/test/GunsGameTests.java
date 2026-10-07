package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.guns.GunItem;
import io.github.jimbozoomer.jugcraft.guns.GunShots;
import io.github.jimbozoomer.jugcraft.guns.GunSpec;
import io.github.jimbozoomer.jugcraft.guns.JugcraftGuns;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

/**
 * In-game tests for the guns (slices 1 and 2, docs/features/guns.md), worked through the server's own entry points (the ones
 * the trigger and reload payloads call): GunShots.fire and GunShots.reload. A shot hurts what it is aimed at by the
 * gun's damage and spends a round; an empty gun, an empty hand and a too-fast trigger do not fire; a creative player
 * spends nothing; a magazine reload takes the gun's reload time and loads from the inventory; a shell-at-a-time reload
 * loads one round per shell's time, and a shot or a switch of item cuts it short; a shotgun's pellets land together;
 * bullets count as projectiles; the iron set's pistol and SMG land their damage, and the Haymaker loads only the shells
 * there are. Shooters face south (+z) and aim at their target's middle.
 */
public class GunsGameTests {
	private static final String ARENA = "jugcraft-test:arms_arena";

	/** Every gun and round is registered, each gun as a GunItem with its numbers, and every gun's round exists. */
	@GameTest
	public void everyGunIsRegistered(GameTestHelper helper) {
		JugcraftGuns.SPECS.forEach((name, spec) -> {
			helper.assertTrue(JugcraftGuns.GUNS.get(name) != null && JugcraftGuns.GUNS.get(name).spec() == spec,
					name + " is not registered with its numbers");
			helper.assertTrue(JugcraftGuns.ammo(spec) != null, name + " fires " + spec.ammo() + ", which is not registered");
			helper.assertTrue(new ItemStack(JugcraftGuns.GUNS.get(name)).getMaxStackSize() == 1, name + " stacks");
		});
		helper.assertTrue(JugcraftGuns.GUNS.size() == 6 && JugcraftGuns.ROUNDS.size() == 3, "Not six guns and three rounds");
		helper.succeed();
	}

	/** A Patchwork Carbine's shot hurts the pig it is aimed at by the carbine's damage and spends a round. */
	@GameTest(structure = ARENA, maxTicks = 20)
	public void carbineShotHurtsWhatItAimsAt(GameTestHelper helper) {
		floor(helper);
		Mob pig = pig(helper, new BlockPos(1, 2, 8));
		ServerPlayer shooter = shooter(helper, "patchwork_carbine", 10, pig, GameType.SURVIVAL);
		float max = pig.getHealth();
		helper.assertTrue(GunShots.fire(shooter), "The loaded carbine did not fire");
		GunSpec spec = JugcraftGuns.SPECS.get("patchwork_carbine");
		helper.assertTrue(Math.abs(max - pig.getHealth() - spec.damage()) < 1.0E-3F,
				"The shot took " + (max - pig.getHealth()) + " from the pig, not " + spec.damage());
		helper.assertTrue(GunItem.loaded(shooter.getMainHandItem()) == 9, "The shot did not spend one round");
		helper.succeed();
	}

	/** An empty gun does not fire, and nothing but a gun fires. */
	@GameTest(structure = ARENA, maxTicks = 20)
	public void emptyGunAndEmptyHandDoNotFire(GameTestHelper helper) {
		floor(helper);
		Mob pig = pig(helper, new BlockPos(1, 2, 8));
		ServerPlayer shooter = shooter(helper, "patchwork_carbine", 0, pig, GameType.SURVIVAL);
		float max = pig.getHealth();
		helper.assertFalse(GunShots.fire(shooter), "The empty carbine fired");
		shooter.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.STICK));
		helper.assertFalse(GunShots.fire(shooter), "A stick fired");
		helper.assertTrue(pig.getHealth() == max, "The pig was hurt");
		helper.succeed();
	}

	/**
	 * The trigger keeps the gun's rate: two shots may be banked (both land, despite the hit cooldown), a third at once is
	 * refused, and one interval later it fires.
	 */
	@GameTest(structure = ARENA, maxTicks = 40)
	public void triggerKeepsTheRate(GameTestHelper helper) {
		floor(helper);
		// Three blocks off, so even the Midge's hip spread (4 degrees) keeps every bullet on the pig.
		Mob pig = pig(helper, new BlockPos(1, 2, 4));
		pig.getAttribute(Attributes.MAX_HEALTH).setBaseValue(200.0);
		pig.setHealth(200.0F);
		ServerPlayer shooter = shooter(helper, "rust_midge", 20, pig, GameType.SURVIVAL);
		helper.assertTrue(GunShots.fire(shooter) && GunShots.fire(shooter), "The first two shots were refused");
		helper.assertFalse(GunShots.fire(shooter), "A third shot in the same tick fired");
		// Both shots land: a bullet passes the half second a creature is shielded after a hit.
		float damage = JugcraftGuns.SPECS.get("rust_midge").damage();
		helper.assertTrue(Math.abs(200.0F - pig.getHealth() - 2 * damage) < 1.0E-3F,
				"Two quick shots took " + (200.0F - pig.getHealth()) + ", not " + 2 * damage);
		int interval = JugcraftGuns.SPECS.get("rust_midge").interval();
		helper.runAfterDelay(interval, () -> {
			helper.assertTrue(GunShots.fire(shooter), "The trigger did not fire again after its interval");
			helper.assertTrue(GunItem.loaded(shooter.getMainHandItem()) == 17, "Three shots did not spend three rounds");
			helper.succeed();
		});
	}

	/** A creative player fires an empty gun and spends nothing. */
	@GameTest(structure = ARENA, maxTicks = 20)
	public void creativeSpendsNothing(GameTestHelper helper) {
		floor(helper);
		Mob pig = pig(helper, new BlockPos(1, 2, 8));
		ServerPlayer shooter = shooter(helper, "patchwork_carbine", 0, pig, GameType.CREATIVE);
		float max = pig.getHealth();
		helper.assertTrue(GunShots.fire(shooter), "The creative player's carbine did not fire");
		helper.assertTrue(pig.getHealth() < max, "The creative shot missed");
		helper.assertTrue(GunItem.loaded(shooter.getMainHandItem()) == 0, "The creative shot spent a round");
		helper.succeed();
	}

	/** A magazine reload takes the gun's reload time, then fills it from the inventory's rounds. */
	@GameTest(structure = ARENA, maxTicks = 80)
	public void magazineReloadLoadsFromTheInventory(GameTestHelper helper) {
		floor(helper);
		Mob pig = pig(helper, new BlockPos(1, 2, 8));
		ServerPlayer shooter = shooter(helper, "patchwork_carbine", 2, pig, GameType.SURVIVAL);
		shooter.getInventory().add(new ItemStack(JugcraftGuns.ROUNDS.get("rifle_round"), 15));
		helper.assertTrue(GunShots.reload(shooter), "The reload did not start");
		helper.assertFalse(GunShots.reload(shooter), "A second reload started during the first");
		int ticks = JugcraftGuns.SPECS.get("patchwork_carbine").reload();
		helper.runAfterDelay(ticks - 2, () -> helper.assertTrue(GunItem.loaded(shooter.getMainHandItem()) == 2,
				"The carbine loaded before its reload time was up"));
		helper.runAfterDelay(ticks + 2, () -> {
			helper.assertTrue(GunItem.loaded(shooter.getMainHandItem()) == 10, "The carbine is not full after its reload");
			helper.assertTrue(GunShots.count(shooter.getInventory(), JugcraftGuns.ROUNDS.get("rifle_round")) == 7,
					"The reload did not take 8 rounds from the inventory");
			helper.assertFalse(GunShots.reloading(shooter), "Still reloading after the reload");
			helper.succeed();
		});
	}

	/** With no rounds to load, a reload does not start; a full gun does not reload. */
	@GameTest(structure = ARENA, maxTicks = 20)
	public void reloadNeedsRoundsAndRoom(GameTestHelper helper) {
		floor(helper);
		Mob pig = pig(helper, new BlockPos(1, 2, 8));
		ServerPlayer shooter = shooter(helper, "rust_midge", 5, pig, GameType.SURVIVAL);
		helper.assertFalse(GunShots.reload(shooter), "A reload started with no rounds to load");
		shooter.getInventory().add(new ItemStack(JugcraftGuns.ROUNDS.get("light_round"), 64));
		GunItem.setLoaded(shooter.getMainHandItem(), 20);
		helper.assertFalse(GunShots.reload(shooter), "A full gun started a reload");
		helper.succeed();
	}

	/** The Thunderpipe loads a shell after each shell's time, one at a time, and closes after the last. */
	@GameTest(structure = ARENA, maxTicks = 80)
	public void shellReloadLoadsOneAtATime(GameTestHelper helper) {
		floor(helper);
		Mob pig = pig(helper, new BlockPos(1, 2, 8));
		ServerPlayer shooter = shooter(helper, "thunderpipe", 0, pig, GameType.SURVIVAL);
		shooter.getInventory().add(new ItemStack(JugcraftGuns.ROUNDS.get("buckshot_shell"), 5));
		GunSpec spec = JugcraftGuns.SPECS.get("thunderpipe");
		helper.assertTrue(GunShots.reload(shooter), "The reload did not start");
		helper.runAfterDelay(spec.shellStart() + spec.shellEach() + 1, () -> helper.assertTrue(
				GunItem.loaded(shooter.getMainHandItem()) == 1, "One shell's time in, not one shell loaded"));
		helper.runAfterDelay(spec.reloadTicks(2) + 2, () -> {
			helper.assertTrue(GunItem.loaded(shooter.getMainHandItem()) == 2, "Both barrels are not loaded");
			helper.assertTrue(GunShots.count(shooter.getInventory(), JugcraftGuns.ROUNDS.get("buckshot_shell")) == 3,
					"The reload did not take two shells");
			helper.assertFalse(GunShots.reloading(shooter), "Still reloading after the last shell");
			helper.succeed();
		});
	}

	/** A shot cuts a shell-at-a-time reload short, and fires what is loaded. */
	@GameTest(structure = ARENA, maxTicks = 40)
	public void shotCutsAShellReloadShort(GameTestHelper helper) {
		floor(helper);
		Mob pig = pig(helper, new BlockPos(1, 2, 8));
		ServerPlayer shooter = shooter(helper, "thunderpipe", 1, pig, GameType.SURVIVAL);
		shooter.getInventory().add(new ItemStack(JugcraftGuns.ROUNDS.get("buckshot_shell"), 5));
		helper.assertTrue(GunShots.reload(shooter), "The reload did not start");
		helper.runAfterDelay(2, () -> {
			helper.assertTrue(GunShots.fire(shooter), "The loaded Thunderpipe did not fire during its reload");
			helper.assertFalse(GunShots.reloading(shooter), "The shot did not cut the reload short");
			helper.assertTrue(GunItem.loaded(shooter.getMainHandItem()) == 0, "The shot did not spend the loaded shell");
			helper.succeed();
		});
	}

	/** Switching to another item stops a reload: the gun is not loaded, and no rounds are taken. */
	@GameTest(structure = ARENA, maxTicks = 80)
	public void switchingAwayStopsAReload(GameTestHelper helper) {
		floor(helper);
		Mob pig = pig(helper, new BlockPos(1, 2, 8));
		ServerPlayer shooter = shooter(helper, "rust_midge", 0, pig, GameType.SURVIVAL);
		shooter.getInventory().add(new ItemStack(JugcraftGuns.ROUNDS.get("light_round"), 30));
		helper.assertTrue(GunShots.reload(shooter), "The reload did not start");
		ItemStack gun = shooter.getMainHandItem();
		shooter.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.STICK));
		helper.runAfterDelay(JugcraftGuns.SPECS.get("rust_midge").reload() + 2, () -> {
			helper.assertFalse(GunShots.reloading(shooter), "The reload went on after switching away");
			helper.assertTrue(GunItem.loaded(gun) == 0, "The gun was loaded after switching away");
			helper.assertTrue(GunShots.count(shooter.getInventory(), JugcraftGuns.ROUNDS.get("light_round")) == 30,
					"Rounds were taken after switching away");
			helper.succeed();
		});
	}

	/** At close range a Thunderpipe's pellets land together: far more than one pellet's damage in one shot. */
	@GameTest(structure = ARENA, maxTicks = 20)
	public void pelletsLandTogether(GameTestHelper helper) {
		floor(helper);
		Mob pig = pig(helper, new BlockPos(1, 2, 4));
		pig.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100.0);
		pig.setHealth(100.0F);
		ServerPlayer shooter = shooter(helper, "thunderpipe", 2, pig, GameType.SURVIVAL);
		helper.assertTrue(GunShots.fire(shooter), "The Thunderpipe did not fire");
		GunSpec spec = JugcraftGuns.SPECS.get("thunderpipe");
		float taken = 100.0F - pig.getHealth();
		helper.assertTrue(taken >= spec.damage() * (spec.pellets() - 2),
				"At close range the pellets took only " + taken + " (one pellet is " + spec.damage() + ")");
		helper.succeed();
	}

	/**
	 * The iron set's Warden Pistol and Riveter SMG each land one shot's damage on what they aim at and spend a round (one
	 * shooter, the second gun after the first's interval).
	 */
	@GameTest(structure = ARENA, maxTicks = 40)
	public void ironSetShotsLand(GameTestHelper helper) {
		floor(helper);
		Mob pig = pig(helper, new BlockPos(1, 2, 4));
		pig.getAttribute(Attributes.MAX_HEALTH).setBaseValue(200.0);
		pig.setHealth(200.0F);
		GunSpec pistol = JugcraftGuns.SPECS.get("warden_pistol");
		GunSpec smg = JugcraftGuns.SPECS.get("riveter_smg");
		ServerPlayer shooter = shooter(helper, "warden_pistol", pistol.capacity(), pig, GameType.SURVIVAL);
		helper.assertTrue(GunShots.fire(shooter), "The loaded Warden Pistol did not fire");
		helper.assertTrue(Math.abs(200.0F - pig.getHealth() - pistol.damage()) < 1.0E-3F,
				"The pistol took " + (200.0F - pig.getHealth()) + ", not " + pistol.damage());
		helper.assertTrue(GunItem.loaded(shooter.getMainHandItem()) == pistol.capacity() - 1, "The pistol did not spend one round");
		helper.runAfterDelay(pistol.interval() + 1, () -> {
			ItemStack stack = new ItemStack(JugcraftGuns.GUNS.get("riveter_smg"));
			GunItem.setLoaded(stack, smg.capacity());
			shooter.setItemInHand(InteractionHand.MAIN_HAND, stack);
			helper.assertTrue(GunShots.fire(shooter), "The loaded Riveter SMG did not fire");
			float expected = pistol.damage() + smg.damage();
			helper.assertTrue(Math.abs(200.0F - pig.getHealth() - expected) < 1.0E-3F,
					"After the SMG the pig had lost " + (200.0F - pig.getHealth()) + ", not " + expected);
			helper.assertTrue(GunItem.loaded(shooter.getMainHandItem()) == smg.capacity() - 1, "The SMG did not spend one round");
			helper.succeed();
		});
	}

	/**
	 * The Haymaker loads a shell at a time and stops at the shells there are (two of its five), then its pellets land
	 * together at close range.
	 */
	@GameTest(structure = ARENA, maxTicks = 120)
	public void haymakerLoadsTheShellsThereAre(GameTestHelper helper) {
		floor(helper);
		Mob pig = pig(helper, new BlockPos(1, 2, 4));
		pig.getAttribute(Attributes.MAX_HEALTH).setBaseValue(200.0);
		pig.setHealth(200.0F);
		ServerPlayer shooter = shooter(helper, "haymaker", 0, pig, GameType.SURVIVAL);
		shooter.getInventory().add(new ItemStack(JugcraftGuns.ROUNDS.get("buckshot_shell"), 2));
		GunSpec spec = JugcraftGuns.SPECS.get("haymaker");
		helper.assertTrue(spec.byShell(), "The Haymaker does not load a shell at a time");
		helper.assertTrue(GunShots.reload(shooter), "The reload did not start");
		helper.runAfterDelay(spec.reloadTicks(2) + 2, () -> {
			helper.assertTrue(GunItem.loaded(shooter.getMainHandItem()) == 2, "The Haymaker did not load both shells");
			helper.assertTrue(GunShots.count(shooter.getInventory(), JugcraftGuns.ROUNDS.get("buckshot_shell")) == 0,
					"Shells were left in the inventory");
			helper.assertFalse(GunShots.reloading(shooter), "Still reloading with no shells left");
			helper.assertTrue(GunShots.fire(shooter), "The loaded Haymaker did not fire");
			float taken = 200.0F - pig.getHealth();
			helper.assertTrue(taken >= spec.damage() * (spec.pellets() - 2),
					"At close range the pellets took only " + taken + " (one pellet is " + spec.damage() + ")");
			helper.succeed();
		});
	}

	/** A bullet is a projectile (Projectile Protection guards against it). */
	@GameTest
	public void bulletsAreProjectiles(GameTestHelper helper) {
		helper.assertTrue(helper.getLevel().registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(JugcraftGuns.BULLET)
				.is(DamageTypeTags.IS_PROJECTILE), "The bullet damage type is not a projectile");
		helper.succeed();
	}

	/** A mock player holding a gun with this many rounds loaded, at the arena's (1, 2, 1), aimed at the target's middle. */
	private static ServerPlayer shooter(GameTestHelper helper, String gun, int loaded, Mob target, GameType mode) {
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		player.setGameMode(mode);
		BlockPos at = helper.absolutePos(new BlockPos(1, 2, 1));
		player.setPos(at.getX() + 0.5, at.getY(), at.getZ() + 0.5);
		Vec3 aim = target.getBoundingBox().getCenter().subtract(player.getEyePosition());
		player.setYRot(0.0F);
		player.setYHeadRot(0.0F);
		player.setXRot((float) Math.toDegrees(Math.atan2(-aim.y, Math.sqrt(aim.x * aim.x + aim.z * aim.z))));
		ItemStack stack = new ItemStack(JugcraftGuns.GUNS.get(gun));
		GunItem.setLoaded(stack, loaded);
		player.setItemInHand(InteractionHand.MAIN_HAND, stack);
		return player;
	}

	private static void floor(GameTestHelper helper) {
		for (int x = 0; x <= 15; x++) {
			for (int z = 0; z <= 15; z++) {
				helper.setBlock(new BlockPos(x, 1, z), Blocks.STONE);
				for (int y = 2; y <= 4; y++) {
					helper.setBlock(new BlockPos(x, y, z), Blocks.AIR);
				}
			}
		}
	}

	/**
	 * A pig that cannot walk off or be knocked back: a hit pushes a creature away (and up, once it stands on the
	 * ground), so a later shot along the first aim could pass under it.
	 */
	private static Mob pig(GameTestHelper helper, BlockPos pos) {
		@SuppressWarnings("unchecked")
		EntityType<Mob> type = (EntityType<Mob>) BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.withDefaultNamespace("pig"));
		Mob pig = helper.spawnWithNoFreeWill(type, pos);
		pig.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.0);
		pig.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(1.0);
		return pig;
	}
}

package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.agriculture.Disguises;
import io.github.jimbozoomer.jugcraft.config.JugcraftConfig;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

/** Costume disguises (docs/features/costume-disguises.md): who is fooled, by what, and when it stops. */
public final class DisguiseGameTests {
	private static ItemStack costume(String name) {
		return new ItemStack(BuiltInRegistries.ITEM.getValue(Jugcraft.id(name)));
	}

	/** A zombie never picks a player in Mummy Wraps as its target, and picks one without them. */
	@GameTest
	public void mummyWrapsFoolTheWalkingDead(GameTestHelper helper) {
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		var zombie = helper.spawn(EntityTypes.ZOMBIE, 2, 2, 2);
		zombie.setTarget(player);
		helper.assertTrue(zombie.getTarget() == player, "Without a costume the zombie should target the player");
		zombie.setTarget(null);
		player.setItemSlot(EquipmentSlot.HEAD, costume("mummy_wraps"));
		helper.assertTrue("walking_dead".equals(Disguises.disguise(player)), "Mummy Wraps should disguise the player as the walking dead");
		zombie.setTarget(player);
		helper.assertTrue(zombie.getTarget() == null, "A zombie should take a player in Mummy Wraps for one of its own");
		helper.succeed();
	}

	/** Hurting a fooled monster ends the disguise for it: it fights back. */
	@GameTest
	public void aStruckMonsterFightsBack(GameTestHelper helper) {
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		player.setItemSlot(EquipmentSlot.HEAD, costume("mummy_wraps"));
		var zombie = helper.spawn(EntityTypes.ZOMBIE, 2, 2, 2);
		zombie.hurtServer(helper.getLevel(), helper.getLevel().damageSources().playerAttack(player), 1.0F);
		helper.assertTrue(zombie.getLastHurtByMob() == player, "The zombie should know who hurt it");
		zombie.setTarget(player);
		helper.assertTrue(zombie.getTarget() == player, "A zombie the player has hurt should fight back, costume or not");
		helper.succeed();
	}

	/** Each costume fools only its own kind: cat ears fool a creeper, not a skeleton. */
	@GameTest
	public void aCostumeFoolsOnlyItsKind(GameTestHelper helper) {
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		player.setItemSlot(EquipmentSlot.HEAD, costume("cat_ears_and_tail"));
		var creeper = helper.spawn(EntityTypes.CREEPER, 2, 2, 2);
		var skeleton = helper.spawn(EntityTypes.SKELETON, 4, 2, 2);
		creeper.setTarget(player);
		skeleton.setTarget(player);
		helper.assertTrue(creeper.getTarget() == null, "A creeper should take a player in cat ears for a cat");
		helper.assertTrue(skeleton.getTarget() == player, "Cat ears should not fool a skeleton");
		player.setItemSlot(EquipmentSlot.HEAD, costume("skeleton_suit"));
		skeleton.setTarget(null);
		skeleton.setTarget(player);
		helper.assertTrue(skeleton.getTarget() == null, "A skeleton should take a player in a Skeleton Suit for one of its own");
		helper.succeed();
	}

	/** The server's costumes.disguises off turns disguises off. */
	@GameTest
	public void disguisesCanBeTurnedOff(GameTestHelper helper) {
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		player.setItemSlot(EquipmentSlot.HEAD, costume("mummy_wraps"));
		var zombie = helper.spawn(EntityTypes.ZOMBIE, 2, 2, 2);
		JugcraftConfig.setTextOption(Disguises.OPTION, "off");
		try {
			zombie.setTarget(player);
			helper.assertTrue(zombie.getTarget() == player, "With disguises off the zombie should target the player");
		} finally {
			JugcraftConfig.setTextOption(Disguises.OPTION, "on");
		}
		helper.succeed();
	}
}

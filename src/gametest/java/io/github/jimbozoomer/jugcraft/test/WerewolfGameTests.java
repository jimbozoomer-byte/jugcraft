package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.agriculture.JugcraftAgriculture;
import io.github.jimbozoomer.jugcraft.agriculture.Werewolf;
import io.github.jimbozoomer.jugcraft.agriculture.Werewolves;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.LootTable;

/**
 * In-game tests for full-moon werewolves (fall addition 23): silver (the dagger in hand, a silver arrow) does two and a
 * half times its damage, anything else half, and slaying one with silver earns Silver Lining; silver stops it healing;
 * it is gone soon after it finds it isn't a full-moon night; wolfsbane in hand or near wards someone off, and a warded
 * target is dropped and left alone (a player earns Not Tonight); no more come near a player than the cap; and the data
 * loads.
 */
public class WerewolfGameTests {
	private static final double EPSILON = 1.0E-4;

	private static Item item(String id) {
		return JugcraftAgriculture.item(id);
	}

	private static ServerPlayer player(GameTestHelper helper, BlockPos standAt, ItemStack held) {
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		player.setGameMode(GameType.SURVIVAL);
		BlockPos absolute = helper.absolutePos(standAt);
		player.setPos(absolute.getX() + 0.5, absolute.getY(), absolute.getZ() + 0.5);
		player.setItemInHand(InteractionHand.MAIN_HAND, held);
		return player;
	}

	@SuppressWarnings("unchecked")
	private static EntityType<Villager> villager() {
		return (EntityType<Villager>) BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.withDefaultNamespace("villager"));
	}

	private static boolean earned(ServerPlayer player, String id) {
		AdvancementHolder advancement = player.level().getServer().getAdvancements().get(Jugcraft.id(id));
		return advancement != null && player.getAdvancements().getOrStartProgress(advancement).isDone();
	}

	/** A werewolf with no armour, so damage taken is damage dealt. */
	private static Werewolf werewolf(GameTestHelper helper, BlockPos at) {
		Werewolf werewolf = helper.spawn(JugcraftAgriculture.WEREWOLF, at);
		werewolf.getAttribute(Attributes.ARMOR).setBaseValue(0.0);
		werewolf.setNoAi(true);
		return werewolf;
	}

	private static float lost(Werewolf werewolf) {
		return werewolf.getMaxHealth() - werewolf.getHealth();
	}

	private static void floor(GameTestHelper helper) {
		for (int x = 0; x <= 7; x++) {
			for (int z = 0; z <= 7; z++) {
				helper.setBlock(new BlockPos(x, 1, z), Blocks.GRASS_BLOCK);
			}
		}
	}

	/**
	 * Eight damage from an iron sword does four to a werewolf, from the silver dagger twenty; a plain arrow four, a silver
	 * arrow twenty. Silver keeps it from healing. Slain with silver, it earns Silver Lining.
	 */
	@GameTest
	public void silverHurtsWerewolves(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		floor(helper);
		ServerPlayer hunter = player(helper, new BlockPos(1, 2, 1), new ItemStack(Items.IRON_SWORD));
		Werewolf ironStruck = werewolf(helper, new BlockPos(2, 2, 4));
		ironStruck.hurtServer(level, level.damageSources().playerAttack(hunter), 8.0F);
		helper.assertTrue(Math.abs(lost(ironStruck) - 8.0F * Werewolf.HIDE_FACTOR) < EPSILON, "Iron does half its damage, not " + lost(ironStruck));
		helper.assertTrue(!ironStruck.silverWounded(), "Iron doesn't stop it healing");

		hunter.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(item("silver_dagger")));
		Werewolf silverStruck = werewolf(helper, new BlockPos(4, 2, 4));
		silverStruck.hurtServer(level, level.damageSources().playerAttack(hunter), 8.0F);
		helper.assertTrue(Math.abs(lost(silverStruck) - 8.0F * Werewolf.SILVER_FACTOR) < EPSILON, "Silver does two and a half times, not "
				+ lost(silverStruck));
		helper.assertTrue(silverStruck.silverWounded(), "Silver stops it healing");

		Werewolf shot = werewolf(helper, new BlockPos(6, 2, 4));
		Arrow plain = new Arrow(level, hunter, new ItemStack(Items.ARROW), new ItemStack(Items.BOW));
		shot.hurtServer(level, level.damageSources().arrow(plain, hunter), 8.0F);
		helper.assertTrue(Math.abs(lost(shot) - 8.0F * Werewolf.HIDE_FACTOR) < EPSILON, "A plain arrow does half");
		Werewolf silverShot = werewolf(helper, new BlockPos(6, 2, 6));
		Arrow silver = new Arrow(level, hunter, new ItemStack(item(Werewolves.SILVER_ARROW)), new ItemStack(Items.BOW));
		silverShot.hurtServer(level, level.damageSources().arrow(silver, hunter), 8.0F);
		helper.assertTrue(Math.abs(lost(silverShot) - 8.0F * Werewolf.SILVER_FACTOR) < EPSILON, "A silver arrow does two and a half times");

		silverStruck.hurtServer(level, level.damageSources().playerAttack(hunter), 100.0F);
		helper.assertTrue(silverStruck.isDeadOrDying(), "Silver slays it");
		helper.assertTrue(earned(hunter, "silver_lining"), "Silver Lining is earned");
		helper.succeed();
	}

	/** A werewolf finds within a second whether it is a full-moon night; if it isn't, it is gone. */
	@GameTest(maxTicks = 80)
	public void werewolvesTurnBackWhenTheNightEnds(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		floor(helper);
		boolean fullMoon = Werewolves.fullMoon(level);
		Werewolf werewolf = helper.spawn(JugcraftAgriculture.WEREWOLF, new BlockPos(3, 2, 3));
		werewolf.setNoAi(true);
		helper.runAfterDelay(45, () -> {
			helper.assertTrue(werewolf.isRemoved() != fullMoon, fullMoon ? "On a full-moon night it stays" : "By day it is gone");
			helper.succeed();
		});
	}

	/**
	 * A player with wolfsbane in hand is warded, as is one standing near planted wolfsbane, and not one further off; a
	 * werewolf keeps hunting a villager with nothing, and drops one holding a sprig and leaves them alone; a warded
	 * player is left alone and earns Not Tonight.
	 */
	@GameTest
	public void wolfsbaneWardsWerewolvesOff(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		floor(helper);
		ServerPlayer holder = player(helper, new BlockPos(1, 2, 1), new ItemStack(Werewolves.wolfsbane()));
		helper.assertTrue(Werewolves.warded(level, holder), "Wolfsbane in hand wards");
		ServerPlayer bare = player(helper, new BlockPos(7, 2, 7), ItemStack.EMPTY);
		helper.assertTrue(!Werewolves.warded(level, bare), "An empty hand with no wolfsbane near doesn't");
		helper.setBlock(new BlockPos(0, 2, 7), JugcraftAgriculture.block(Werewolves.WOLFSBANE));
		helper.assertTrue(Werewolves.wardNear(level, helper.absolutePos(new BlockPos(5, 2, 7))), "Planted wolfsbane wards five blocks off");
		helper.assertTrue(!Werewolves.wardNear(level, helper.absolutePos(new BlockPos(0, 2, 7)).east(Werewolves.WARD_REACH + 1)),
				"but not beyond its reach");

		// A villager can be hunted at once, where a player who has only just appeared can't be yet. Without AI, so only
		// its own ward check (not a target goal) changes its target.
		Villager unwarded = helper.spawnWithNoFreeWill(villager(), new BlockPos(7, 2, 5));
		Villager sprigged = helper.spawnWithNoFreeWill(villager(), new BlockPos(1, 2, 3));
		sprigged.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Werewolves.wolfsbane()));
		Werewolf werewolf = werewolf(helper, new BlockPos(3, 2, 3));
		werewolf.setTarget(unwarded);
		helper.assertTrue(werewolf.getTarget() == unwarded, "It hunts a villager");
		helper.assertTrue(!werewolf.checkWard(level) && werewolf.getTarget() == unwarded, "It keeps hunting someone unwarded");
		werewolf.setTarget(sprigged);
		helper.assertTrue(werewolf.checkWard(level), "Its ward check finds wolfsbane on its target");
		helper.assertTrue(werewolf.getTarget() == null && werewolf.shuns(sprigged) && !werewolf.canAttack(sprigged),
				"It drops a warded target and leaves them alone");
		werewolf.wardedOff(holder);
		helper.assertTrue(werewolf.shuns(holder) && !werewolf.canAttack(holder), "It leaves a warded player alone");
		helper.assertTrue(earned(holder, "wolfsbane_ward"), "Not Tonight is earned");
		helper.succeed();
	}

	/** No more werewolves come near a player than the cap allows. */
	@GameTest
	public void werewolvesKeepToTheirCap(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		floor(helper);
		for (int n = 0; n < Werewolves.NEAR_CAP; n++) {
			werewolf(helper, new BlockPos(2 + 2 * n, 2, 2));
		}
		helper.assertTrue(Werewolves.trySpawn(level, helper.absolutePos(new BlockPos(3, 2, 3)), level.getRandom()) == 0, "None come past the cap");
		helper.assertTrue(Werewolves.ground(level, helper.absolutePos(new BlockPos(3, 2, 3)).getX(), helper.absolutePos(new BlockPos(3, 2, 3)).getZ())
				== null || level.getBiome(helper.absolutePos(new BlockPos(3, 2, 3))).is(Werewolves.HAUNTS), "Only werewolf country is ground for one");
		helper.succeed();
	}

	/** The silver dagger, silver arrows, the rug and leather from the pelt have recipes; the drops, flower and tags load. */
	@GameTest
	public void werewolfDataLoads(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		for (String id : List.of("silver_dagger", Werewolves.SILVER_ARROW, "werewolf_rug", "leather_from_werewolf_pelt")) {
			helper.assertTrue(level.recipeAccess().byKey(ResourceKey.create(Registries.RECIPE, Jugcraft.id(id))).isPresent(), id + " has a recipe");
		}
		for (String table : List.of("entities/werewolf", "blocks/wolfsbane", "blocks/potted_wolfsbane", "blocks/werewolf_rug")) {
			helper.assertTrue(level.getServer().reloadableRegistries().getLootTable(ResourceKey.create(Registries.LOOT_TABLE, Jugcraft.id(table)))
					!= LootTable.EMPTY, table + " loads");
		}
		for (String id : List.of("silver_lining", "wolfsbane_ward")) {
			helper.assertTrue(level.getServer().getAdvancements().get(Jugcraft.id(id)) != null, id + " loads");
		}
		helper.assertTrue(new ItemStack(item(Werewolves.SILVER_ARROW)).is(ItemTags.ARROWS), "A silver arrow is an arrow to a bow");
		helper.assertTrue(new ItemStack(item("silver_dagger")).is(Werewolf.SILVER_WEAPONS), "The dagger is a silver weapon");
		helper.succeed();
	}
}

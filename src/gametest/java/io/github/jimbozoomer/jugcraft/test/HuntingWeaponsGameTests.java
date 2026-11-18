package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.weapons.*;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;

/** Real survival throws and bow shots; shared Arms and claim suites cover the inherited damage permissions. */
public class HuntingWeaponsGameTests {

	@GameTest
	public void hatchetChopsAndSpearKeepsItsReach(GameTestHelper h) {
		ItemStack hatchet = new ItemStack(HuntingWeapons.STONE_HEADED_HATCHET);
		ItemStack spear = new ItemStack(HuntingWeapons.HUNTING_SPEAR);
		h.assertTrue(hatchet.getDestroySpeed(Blocks.OAK_LOG.defaultBlockState())
				== new ItemStack(Items.STONE_AXE).getDestroySpeed(Blocks.OAK_LOG.defaultBlockState()), "Hatchet chops at stone-axe speed");
		h.assertTrue(spear.get(DataComponents.ATTACK_RANGE).maxReach() == 3.5F, "Spear reaches 3.5 blocks");
		h.assertTrue(hatchet.getMaxDamage() == 131 && spear.getMaxDamage() == 131, "Stone-tier wear");
		h.succeed();
	}

	@GameTest(structure = "jugcraft-test:arms_arena", padding = 48, maxTicks = 70)
	public void survivalThrowsCarryTheActualNamedItemAndReturnItOnce(GameTestHelper h) {
		for (int x = 0; x < 16; x++) for (int z = 0; z < 16; z++) {
			h.setBlock(new BlockPos(x, 1, z), Blocks.STONE);
			for (int y = 2; y < 7; y++) h.setBlock(new BlockPos(x, y, z), Blocks.AIR);
		}
		var items = List.of(HuntingWeapons.STONE_HEADED_HATCHET, HuntingWeapons.HUNTING_SPEAR);
		for (int i = 0; i < items.size(); i++) {
			var item = (ThrownArmItem) items.get(i);
			ServerPlayer player = player(h, new BlockPos(3 + 6*i, 2, 1));
			ItemStack stack = new ItemStack(item);
			stack.set(DataComponents.CUSTOM_NAME, net.minecraft.network.chat.Component.literal("Heirloom " + i));
			stack.setDamageValue(9);
			player.setItemInHand(InteractionHand.MAIN_HAND, stack);
			player.setXRot(12);
			player.startUsingItem(InteractionHand.MAIN_HAND);
			h.assertFalse(item.releaseUsing(stack, h.getLevel(), player, stack.getUseDuration(player) - item.thrown().wind() + 1),
					"A short wind must not consume a weapon");
			h.assertTrue(item.releaseUsing(stack, h.getLevel(), player, stack.getUseDuration(player) - item.thrown().wind()), "Full wind throws");
			h.assertTrue(player.getMainHandItem().isEmpty(), "Survival throw removes hand item");
			var flying = h.getLevel().getEntitiesOfClass(ThrownArm.class, new AABB(player.blockPosition()).inflate(2), a -> a.getOwner() == player);
			h.assertTrue(flying.size() == 1 && flying.getFirst().getItem().is(item), "Projectile carries the actual hunting weapon");
			h.assertTrue(flying.getFirst().thrown() == item.thrown(), "Projectile keeps custom flight and damage");
		}
		AABB area = new AABB(net.minecraft.world.phys.Vec3.atLowerCornerOf(h.absolutePos(BlockPos.ZERO)), net.minecraft.world.phys.Vec3.atLowerCornerOf(h.absolutePos(new BlockPos(16, 8, 18))));
		h.succeedWhen(() -> {
			for (int i = 0; i < items.size(); i++) {
				var item = items.get(i);
				var drops = h.getLevel().getEntitiesOfClass(ItemEntity.class, area, e -> e.getItem().is(item));
				h.assertTrue(drops.size() == 1 && drops.getFirst().getItem().getCount() == 1, "Exactly one recovered weapon");
				ItemStack returned = drops.getFirst().getItem();
				h.assertTrue(returned.getDamageValue() == 9 + JugcraftArms.THROW_WEAR
						&& returned.getHoverName().getString().equals("Heirloom " + i), "Wear and name survive throw");
			}
		});
	}

	@GameTest
	public void bowConsumesOneArrowAtItsOwnDrawAndCannotShootEmpty(GameTestHelper h) {
		ServerPlayer player = player(h, new BlockPos(2, 3, 2));
		ArmBowItem item = (ArmBowItem) HuntingWeapons.WOODLAND_HUNTING_BOW;
		ItemStack bow = new ItemStack(item);
		player.setItemInHand(InteractionHand.MAIN_HAND, bow);
		player.startUsingItem(InteractionHand.MAIN_HAND);
		h.assertFalse(item.releaseUsing(bow, h.getLevel(), player, bow.getUseDuration(player) - 17), "No arrow means no shot");
		player.getInventory().add(new ItemStack(Items.ARROW, 2));
		h.assertTrue(item.releaseUsing(bow, h.getLevel(), player, bow.getUseDuration(player) - 17), "17-tick draw fires");
		var arrows = h.getLevel().getEntitiesOfClass(AbstractArrow.class, new AABB(player.blockPosition()).inflate(2), a -> a.getOwner() == player);
		h.assertTrue(arrows.size() == 1 && arrows.getFirst().isCritArrow(), "One fully drawn arrow");
		h.assertTrue(Math.abs(arrows.getFirst().getDeltaMovement().length() - 2.4) < .1, "Short bow arrow speed");
		h.assertTrue(player.getInventory().countItem(Items.ARROW) == 1 && bow.getDamageValue() == 1, "One arrow and one durability per shot");
		h.succeed();
	}

	private static ServerPlayer player(GameTestHelper h, BlockPos relative) {
		ServerPlayer p = h.makeMockServerPlayerInLevel();
		p.setGameMode(GameType.SURVIVAL);
		BlockPos at = h.absolutePos(relative);
		p.snapTo(at.getX() + .5, at.getY(), at.getZ() + .5, 0, 0);
		return p;
	}
}

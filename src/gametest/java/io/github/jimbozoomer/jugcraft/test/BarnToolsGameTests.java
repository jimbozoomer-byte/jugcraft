package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.agriculture.JugcraftAgriculture;
import io.github.jimbozoomer.jugcraft.agriculture.SeedPouchItem;
import io.github.jimbozoomer.jugcraft.agriculture.WateringCanItem;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Barn & Harvest Tools, part 1 (decor set 3; docs/features/barn-and-harvest-tools.md): the Watering Can fills at a water
 * source, pours on farmland to wet the square round it and no further, spends a pour each time, waits its cooldown and
 * does nothing dry; the Seed Pouch gathers the seeds a player carries (and nothing else), sows the empty farmland round
 * the farmland it is used on with its first kind first, and empties back when sneak-used; the Steel Sickle harvests 7 by 7.
 */
public class BarnToolsGameTests {
	private static Item item(String id) {
		return JugcraftAgriculture.item(id);
	}

	private static BlockHitResult hit(GameTestHelper helper, BlockPos pos, Direction side) {
		BlockPos absolute = helper.absolutePos(pos);
		return new BlockHitResult(Vec3.atCenterOf(absolute).relative(side, 0.5), side, absolute, false);
	}

	private static InteractionResult useOn(GameTestHelper helper, ServerPlayer player, BlockPos pos) {
		return player.gameMode.useItemOn(player, helper.getLevel(), player.getMainHandItem(), InteractionHand.MAIN_HAND, hit(helper, pos, Direction.UP));
	}

	private static InteractionResult useInAir(GameTestHelper helper, ServerPlayer player) {
		return player.gameMode.useItem(player, helper.getLevel(), player.getMainHandItem(), InteractionHand.MAIN_HAND);
	}

	/** A survival player standing at {@code at}, holding {@code held}. */
	private static ServerPlayer standing(GameTestHelper helper, BlockPos at, ItemStack held) {
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		player.setGameMode(GameType.SURVIVAL);
		BlockPos absolute = helper.absolutePos(at);
		player.setPos(absolute.getX() + 0.5, absolute.getY(), absolute.getZ() + 0.5);
		player.setItemInHand(InteractionHand.MAIN_HAND, held);
		return player;
	}

	/** Dry farmland in the square of {@code radius} round {@code center} (on the floor, y 1). */
	private static void field(GameTestHelper helper, BlockPos center, int radius) {
		for (int x = -radius; x <= radius; x++) {
			for (int z = -radius; z <= radius; z++) {
				helper.setBlock(center.offset(x, 0, z), Blocks.FARMLAND.defaultBlockState().setValue(BlockStateProperties.MOISTURE, 0));
			}
		}
	}

	private static int moisture(GameTestHelper helper, BlockPos pos) {
		return helper.getBlockState(pos).getValue(BlockStateProperties.MOISTURE);
	}

	@GameTest(maxTicks = 40)
	public void theCanFillsAndWaters(GameTestHelper helper) {
		ItemStack can = new ItemStack(item("watering_can"));
		// Filled at a water source the player looks down at.
		helper.setBlock(new BlockPos(1, 1, 1), Blocks.WATER);
		ServerPlayer player = standing(helper, new BlockPos(1, 2, 1), can);
		player.setXRot(90.0F);
		helper.assertTrue(useInAir(helper, player).consumesAction() && WateringCanItem.water(player.getMainHandItem()) == WateringCanItem.CAPACITY,
				"It fills at a water source: " + WateringCanItem.water(player.getMainHandItem()));
		// A pour on the middle of a field wets the square round it and no further.
		BlockPos center = new BlockPos(4, 1, 4);
		field(helper, center, 2);
		player = standing(helper, new BlockPos(4, 2, 1), player.getMainHandItem());
		helper.assertTrue(useOn(helper, player, center).consumesAction(), "It pours on farmland");
		for (int x = -2; x <= 2; x++) {
			for (int z = -2; z <= 2; z++) {
				boolean inside = Math.abs(x) <= WateringCanItem.RADIUS && Math.abs(z) <= WateringCanItem.RADIUS;
				BlockPos at = center.offset(x, 0, z);
				helper.assertTrue(moisture(helper, at) == (inside ? 7 : 0), "Farmland at " + at + " should be " + (inside ? "wet" : "dry") + ": " + moisture(helper, at));
			}
		}
		ItemStack held = player.getMainHandItem();
		helper.assertTrue(WateringCanItem.water(held) == WateringCanItem.CAPACITY - 1, "A pour spends one: " + WateringCanItem.water(held));
		helper.assertTrue(player.getCooldowns().isOnCooldown(held), "It waits its cooldown after a pour");
		// While it waits, another use pours nothing.
		BlockPos corner = center.offset(2, 0, 2);
		useOn(helper, player, corner);
		helper.assertTrue(moisture(helper, corner) == 0 && WateringCanItem.water(held) == WateringCanItem.CAPACITY - 1, "It doesn't pour while it waits");
		// An empty can does nothing (in the hand of a player with no cooldown).
		held.set(JugcraftAgriculture.WATERING_CAN_WATER, 0);
		player = standing(helper, new BlockPos(4, 2, 1), held);
		helper.assertFalse(useOn(helper, player, corner).consumesAction(), "An empty can doesn't pour");
		helper.assertTrue(moisture(helper, corner) == 0, "An empty can wets nothing");
		// A pour on a crop waters the farmland under it.
		held.set(JugcraftAgriculture.WATERING_CAN_WATER, 1);
		BlockPos crop = corner.above();
		helper.setBlock(crop, ((CropBlock) JugcraftAgriculture.block("bean_crop")).getStateForAge(0));
		helper.assertTrue(useOn(helper, player, crop).consumesAction() && moisture(helper, corner) == 7, "A pour on a crop wets its farmland");
		helper.succeed();
	}

	@GameTest(maxTicks = 40)
	public void thePouchGathersSowsAndEmpties(GameTestHelper helper) {
		ServerPlayer player = standing(helper, new BlockPos(4, 2, 0), new ItemStack(item("seed_pouch")));
		player.getInventory().add(new ItemStack(item("flax_seeds"), 10));
		player.getInventory().add(new ItemStack(item("beans"), 5));
		player.getInventory().add(new ItemStack(Items.DIRT, 3));
		// Used in the air it gathers the seeds, and only the seeds.
		helper.assertTrue(useInAir(helper, player).consumesAction(), "It gathers seeds");
		List<ItemStack> held = SeedPouchItem.contents(player.getMainHandItem());
		helper.assertTrue(held.size() == 2 && held.get(0).is(item("flax_seeds")) && held.get(0).getCount() == 10
				&& held.get(1).is(item("beans")) && held.get(1).getCount() == 5, "It holds the flax seeds and the beans: " + held);
		helper.assertTrue(player.getInventory().countItem(item("flax_seeds")) == 0 && player.getInventory().countItem(item("beans")) == 0
				&& player.getInventory().countItem(Items.DIRT) == 3, "The seeds left the inventory, the dirt didn't");
		// Used on farmland it sows the empty farmland round it, first kind first, a seed a block.
		BlockPos center = new BlockPos(4, 1, 4);
		field(helper, center, 1);
		helper.setBlock(center.offset(1, 1, 1), ((CropBlock) JugcraftAgriculture.block("bean_crop")).getStateForAge(3));
		helper.assertTrue(useOn(helper, player, center).consumesAction(), "It sows");
		Block flax = JugcraftAgriculture.block("flax_crop");
		for (int x = -1; x <= 1; x++) {
			for (int z = -1; z <= 1; z++) {
				BlockState above = helper.getBlockState(center.offset(x, 1, z));
				if (x == 1 && z == 1) {
					helper.assertTrue(above.getValue(CropBlock.AGE) == 3, "A growing crop is left alone: " + above);
				} else {
					helper.assertTrue(above.is(flax), "Flax is sown at " + center.offset(x, 1, z) + ": " + above);
				}
			}
		}
		held = SeedPouchItem.contents(player.getMainHandItem());
		helper.assertTrue(held.size() == 2 && held.get(0).getCount() == 2 && held.get(1).getCount() == 5, "Eight flax seeds are spent: " + held);
		// Nothing left to sow: it does nothing.
		helper.assertFalse(useOn(helper, player, center).consumesAction(), "A sown field takes no more");
		// Sneak-used in the air it empties back into the inventory.
		player.setShiftKeyDown(true);
		helper.assertTrue(useInAir(helper, player).consumesAction(), "It empties");
		helper.assertTrue(SeedPouchItem.contents(player.getMainHandItem()).isEmpty() && player.getInventory().countItem(item("flax_seeds")) == 2
				&& player.getInventory().countItem(item("beans")) == 5, "Its seeds are back in the inventory");
		helper.succeed();
	}

	@GameTest(maxTicks = 40)
	public void theSteelSickleReapsSevenBySeven(GameTestHelper helper) {
		Block beans = JugcraftAgriculture.block("bean_crop");
		for (int x = 0; x <= 7; x++) {
			helper.setBlock(new BlockPos(x, 1, 3), Blocks.FARMLAND.defaultBlockState().setValue(BlockStateProperties.MOISTURE, 7));
			helper.setBlock(new BlockPos(x, 2, 3), ((CropBlock) beans).getStateForAge(7));
		}
		ServerPlayer player = standing(helper, new BlockPos(3, 2, 0), new ItemStack(item("steel_sickle")));
		helper.assertTrue(useOn(helper, player, new BlockPos(3, 2, 3)).consumesAction(), "It reaps");
		for (int x = 0; x <= 7; x++) {
			BlockState state = helper.getBlockState(new BlockPos(x, 2, 3));
			int age = x <= 6 ? 0 : 7;
			helper.assertTrue(state.is(beans) && state.getValue(CropBlock.AGE) == age, "Beans at x=" + x + " should be age " + age + ": " + state);
		}
		helper.assertTrue(player.getMainHandItem().getDamageValue() == 1, "It wears 1 a use");
		helper.succeed();
	}
}

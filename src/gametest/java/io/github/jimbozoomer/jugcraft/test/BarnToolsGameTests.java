package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.agriculture.ButterChurnBlock;
import io.github.jimbozoomer.jugcraft.agriculture.ButterChurnBlockEntity;
import io.github.jimbozoomer.jugcraft.agriculture.CornCribBlock;
import io.github.jimbozoomer.jugcraft.agriculture.DryingRackBlock;
import io.github.jimbozoomer.jugcraft.agriculture.DryingRackBlockEntity;
import io.github.jimbozoomer.jugcraft.agriculture.FruitBinBlock;
import io.github.jimbozoomer.jugcraft.agriculture.FruitStoreBlockEntity;
import io.github.jimbozoomer.jugcraft.agriculture.JugcraftAgriculture;
import io.github.jimbozoomer.jugcraft.agriculture.QuernBlock;
import io.github.jimbozoomer.jugcraft.agriculture.QuernBlockEntity;
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
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Barn & Harvest Tools, part 1 (decor set 3; docs/features/barn-and-harvest-tools.md): the Watering Can fills at a water
 * source, pours on farmland to wet the square round it and no further, spends a pour each time, waits its cooldown and
 * does nothing dry; the Seed Pouch gathers the seeds a player carries (and nothing else), sows the empty farmland round
 * the farmland it is used on with its first kind first, and empties back when sneak-used; the Steel Sickle harvests 7 by 7.
 * Part 2: the Quern takes grain and refuses anything else, grinds a piece every two turns (wheat into flour, a dried
 * chili into two paprika), won't turn again before a turn is done, gives its grain back to a sneaking empty hand and
 * spills it when broken; the Butter Churn takes a bucket of milk and gives the bucket back, refuses a second, churns
 * butter on the eighth stroke, won't stroke again before a stroke is done, and spills its milk when broken. Part 3: the
 * Drying Rack stands whole, hangs bunches of four peppers and herbs on its hooks and refuses anything else, shows them,
 * gives back a fresh bunch, waits to show them dry and gives them back dried, and gives back its bunches when broken; the
 * Corn Crib stands whole, takes corn up to its capacity and nothing else, shows it on every block, gives back a stack and
 * spills its corn when broken.
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

	// ---------------------------------------------------------------- part 2

	private static int dropped(GameTestHelper helper, Item item) {
		return helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(helper.absolutePos(new BlockPos(3, 2, 3))).inflate(4.0),
				entity -> entity.getItem().is(item)).stream().mapToInt(entity -> entity.getItem().getCount()).sum();
	}

	@GameTest(maxTicks = 60)
	public void theQuernGrindsWheat(GameTestHelper helper) {
		BlockPos pos = new BlockPos(3, 2, 3);
		helper.setBlock(pos.below(), Blocks.STONE);
		helper.setBlock(pos, JugcraftAgriculture.block("quern").defaultBlockState());
		QuernBlockEntity quern = helper.getBlockEntity(pos, QuernBlockEntity.class);
		ServerPlayer player = standing(helper, new BlockPos(3, 2, 1), new ItemStack(Items.STICK));
		useOn(helper, player, pos);
		helper.assertTrue(player.getMainHandItem().getCount() == 1 && quern.grain().isEmpty(), "A stick isn't taken");
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.WHEAT, 20));
		useOn(helper, player, pos);
		helper.assertTrue(quern.grain().getCount() == QuernBlock.CAPACITY && player.getMainHandItem().getCount() == 20 - QuernBlock.CAPACITY
				&& helper.getBlockState(pos).getValue(QuernBlock.LOADED), "Sixteen wheat go in and show: " + quern.grain());
		player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
		useOn(helper, player, pos);
		useOn(helper, player, pos);
		helper.assertTrue(quern.turns() == 1 && quern.grain().getCount() == QuernBlock.CAPACITY, "A turn waits for the last: " + quern.turns());
		// (a tick to spare, so the next turn is surely due)
		helper.runAfterDelay(QuernBlock.SPIN_TICKS + 1, () -> {
			useOn(helper, player, pos);
			helper.assertTrue(quern.grain().getCount() == QuernBlock.CAPACITY - 1, "Two turns grind one wheat: " + quern.grain());
			helper.runAfterDelay(3, () -> {
				helper.assertTrue(dropped(helper, JugcraftAgriculture.item("flour")) == 1, "It falls out as a flour");
				// A dried chili grinds into two paprika, as the mortar grinds it.
				QuernBlockEntity spices = new QuernBlockEntity(helper.absolutePos(pos.east()), helper.getBlockState(pos));
				spices.add(new ItemStack(JugcraftAgriculture.item("dried_chili")));
				spices.turn(0L);
				ItemStack paprika = spices.turn(QuernBlock.SPIN_TICKS);
				helper.assertTrue(paprika.is(JugcraftAgriculture.item("paprika")) && paprika.getCount() == 2, "A dried chili gives two paprika: " + paprika);
				// Sneaking, an empty hand takes the grain back.
				player.setShiftKeyDown(true);
				useOn(helper, player, pos);
				helper.assertTrue(quern.grain().isEmpty() && player.getInventory().countItem(Items.WHEAT) == QuernBlock.CAPACITY - 1
						&& !helper.getBlockState(pos).getValue(QuernBlock.LOADED), "Its grain comes back");
				// Broken with grain in, it spills it.
				quern.add(new ItemStack(Items.WHEAT, 5));
				helper.getLevel().destroyBlock(helper.absolutePos(pos), true);
				helper.runAfterDelay(3, () -> {
					helper.assertTrue(dropped(helper, Items.WHEAT) == 5 && dropped(helper, JugcraftAgriculture.item("quern")) == 1,
							"Broken, it spills its wheat and drops itself");
					helper.succeed();
				});
			});
		});
	}

	@GameTest(maxTicks = 20 + ButterChurnBlock.STROKES * (ButterChurnBlock.STROKE_TICKS + 1) + 40)
	public void theChurnMakesButter(GameTestHelper helper) {
		BlockPos pos = new BlockPos(3, 2, 3);
		helper.setBlock(pos.below(), Blocks.STONE);
		helper.setBlock(pos, JugcraftAgriculture.block("butter_churn").defaultBlockState());
		ButterChurnBlockEntity churn = helper.getBlockEntity(pos, ButterChurnBlockEntity.class);
		ServerPlayer player = standing(helper, new BlockPos(3, 2, 1), new ItemStack(Items.MILK_BUCKET));
		useOn(helper, player, pos);
		helper.assertTrue(churn.milk() && player.getMainHandItem().is(Items.BUCKET) && helper.getBlockState(pos).getValue(ButterChurnBlock.FULL),
				"The milk goes in and the bucket comes back");
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.MILK_BUCKET));
		useOn(helper, player, pos);
		helper.assertTrue(player.getMainHandItem().is(Items.MILK_BUCKET), "A second bucket isn't taken");
		player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
		useOn(helper, player, pos);
		useOn(helper, player, pos);
		helper.assertTrue(churn.strokes() == 1, "A stroke waits for the last: " + churn.strokes());
		// Each later stroke a tick after the last is done.
		for (int stroke = 1; stroke < ButterChurnBlock.STROKES; stroke++) {
			int at = stroke;
			helper.runAfterDelay(stroke * (ButterChurnBlock.STROKE_TICKS + 1), () -> {
				useOn(helper, player, pos);
				helper.assertTrue(churn.strokes() == (at + 1) % ButterChurnBlock.STROKES, "Stroke " + (at + 1) + ": " + churn.strokes());
			});
		}
		helper.runAfterDelay(ButterChurnBlock.STROKES * (ButterChurnBlock.STROKE_TICKS + 1) + 3, () -> {
			helper.assertTrue(dropped(helper, JugcraftAgriculture.item("butter")) == ButterChurnBlock.BUTTER && !churn.milk()
					&& !helper.getBlockState(pos).getValue(ButterChurnBlock.FULL), "The eighth stroke churns two butter");
			// Broken with milk in, the milk is spilt: nothing comes out but the churn.
			churn.fill();
			helper.getLevel().destroyBlock(helper.absolutePos(pos), true);
			helper.runAfterDelay(3, () -> {
				helper.assertTrue(dropped(helper, JugcraftAgriculture.item("butter_churn")) == 1 && dropped(helper, Items.MILK_BUCKET) == 0
						&& dropped(helper, Items.BUCKET) == 0, "Broken, it drops itself and spills its milk");
				helper.succeed();
			});
		});
	}

	// ---------------------------------------------------------------- part 3

	/** Places the prop in the player's hand on the floor at {@code at}, as they would (facing them: north). */
	private static void place(GameTestHelper helper, ServerPlayer player, BlockPos at) {
		player.getMainHandItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit(helper, at.below(), Direction.UP)));
	}

	private static void stone(GameTestHelper helper) {
		for (int x = 0; x < 8; x++) {
			for (int z = 0; z < 8; z++) {
				helper.setBlock(new BlockPos(x, 1, z), Blocks.STONE);
			}
		}
	}

	@GameTest(maxTicks = 40)
	public void theRackDriesItsBunches(GameTestHelper helper) {
		stone(helper);
		BlockPos pos = new BlockPos(3, 2, 3);
		ServerPlayer player = standing(helper, new BlockPos(3, 2, 1), new ItemStack(item("drying_rack")));
		place(helper, player, pos);
		helper.assertTrue(helper.getBlockState(pos).is(JugcraftAgriculture.block("drying_rack"))
				&& helper.getBlockState(pos.above()).is(JugcraftAgriculture.block("drying_rack")), "It stands whole, two blocks up");
		DryingRackBlockEntity rack = helper.getBlockEntity(pos, DryingRackBlockEntity.class);
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.STICK));
		useOn(helper, player, pos);
		helper.assertTrue(rack.bunch(0).isEmpty() && player.getMainHandItem().getCount() == 1, "A stick doesn't hang");
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(item("pepper"), 6));
		useOn(helper, player, pos);
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(item("sage"), 4));
		useOn(helper, player, pos.above());
		helper.assertTrue(rack.bunch(0).getCount() == DryingRackBlock.BUNCH && rack.bunch(1).is(item("sage")),
				"A bunch of four peppers and one of sage hang: " + rack.bunch(0) + ", " + rack.bunch(1));
		helper.assertTrue(helper.getBlockState(pos).getValue(DryingRackBlock.RIGHT) == DryingRackBlock.Bunch.CHILI
				&& helper.getBlockState(pos).getValue(DryingRackBlock.LEFT) == DryingRackBlock.Bunch.HERBS, "They show on the lower rail");
		helper.assertTrue(helper.getLevel().getBlockTicks().hasScheduledTick(helper.absolutePos(pos), JugcraftAgriculture.block("drying_rack")),
				"It waits to show them dry");
		// Before they dry, an empty hand takes down the last hung, fresh.
		player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
		useOn(helper, player, pos);
		helper.assertTrue(player.getInventory().countItem(item("sage")) == 4 && rack.bunch(1).isEmpty()
				&& helper.getBlockState(pos).getValue(DryingRackBlock.LEFT) == DryingRackBlock.Bunch.EMPTY, "The fresh sage comes back down");
		// Once dry, the peppers come down as dried chili, one for one.
		long now = helper.getLevel().getGameTime();
		helper.assertTrue(!rack.dried(0, now) && rack.dried(0, now + DryingRackBlock.DRY_TICKS), "They dry in their time");
		ItemStack dried = rack.takeDown(now + DryingRackBlock.DRY_TICKS);
		helper.assertTrue(dried.is(item("dried_chili")) && dried.getCount() == DryingRackBlock.BUNCH, "Four dried chilies: " + dried);
		// Broken, it gives back what hangs on it.
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(item("pepper"), 2));
		useOn(helper, player, pos);
		helper.getLevel().destroyBlock(helper.absolutePos(pos.above()), true);
		helper.runAfterDelay(3, () -> {
			helper.assertTrue(dropped(helper, item("pepper")) == 2 && dropped(helper, item("drying_rack")) == 1,
					"Broken, it gives back its peppers and drops once");
			helper.succeed();
		});
	}

	@GameTest(maxTicks = 40)
	public void theCribKeepsCorn(GameTestHelper helper) {
		stone(helper);
		BlockPos pos = new BlockPos(4, 2, 3);
		ServerPlayer player = standing(helper, new BlockPos(4, 2, 1), new ItemStack(item("corn_crib")));
		place(helper, player, pos);
		CornCribBlock crib = (CornCribBlock) JugcraftAgriculture.block("corn_crib");
		BlockPos master = helper.absolutePos(pos);
		for (int part = 0; part < crib.cells().length; part++) {
			helper.assertTrue(helper.getLevel().getBlockState(crib.partPos(master, Direction.NORTH, part)).is(crib), "Its block " + part + " is there");
		}
		FruitStoreBlockEntity store = helper.getBlockEntity(pos, FruitStoreBlockEntity.class);
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.APPLE, 10));
		useOn(helper, player, pos);
		helper.assertTrue(store.count() == 0 && player.getMainHandItem().getCount() == 10, "Apples don't go in");
		for (int i = 0; i < 5; i++) {
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(item("corn"), 60));
			useOn(helper, player, pos.above());
		}
		helper.assertTrue(store.count() == CornCribBlock.CAPACITY, "It takes corn up to its capacity: " + store.count());
		for (int part = 0; part < crib.cells().length; part++) {
			BlockState there = helper.getLevel().getBlockState(crib.partPos(master, Direction.NORTH, part));
			helper.assertTrue(there.getValue(FruitBinBlock.FILL) == FruitBinBlock.STAGES, "Full, shown on block " + part + ": " + there);
		}
		player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
		useOn(helper, player, pos);
		helper.assertTrue(store.count() < CornCribBlock.CAPACITY && player.getInventory().countItem(item("corn")) > 0, "An empty hand takes a stack back");
		int left = store.count();
		helper.getLevel().destroyBlock(crib.partPos(master, Direction.NORTH, 3), true);
		helper.runAfterDelay(3, () -> {
			helper.assertTrue(dropped(helper, item("corn")) == left && dropped(helper, item("corn_crib")) == 1,
					"Broken, it spills its corn and drops once: " + dropped(helper, item("corn")) + " of " + left);
			helper.succeed();
		});
	}
}

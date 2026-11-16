package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.agriculture.JugcraftAgriculture;
import io.github.jimbozoomer.jugcraft.agriculture.PlacedDishBlock;
import io.github.jimbozoomer.jugcraft.agriculture.ThanksgivingDishes;
import io.github.jimbozoomer.jugcraft.finance.LandRegistry;
import io.github.jimbozoomer.jugcraft.finance.Ledger;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Thanksgiving side dishes (tools/thanksgiving_dishes.py; docs/features/thanksgiving-dishes.md): each of the six is a
 * bowl food of its table's hunger and saturation, one to a stack, that gives its bowl back when eaten; a sneaking player
 * sets it down as its dish, facing them, and an empty hand takes it back (exactly as the menu's dishes, MenuGameTests);
 * broken, a set-down dish drops its food once; in a private claim an outsider can neither set one down, take one, nor
 * break one, while the claim's owner can; and every recipe and loot table loads.
 *
 * <p>Written for the headless server game tests; they have not been run here (no Minecraft or Fabric toolchain in the
 * environment these were written in). Each scheduled step is its own Runnable (GameTestInfo keys scheduled callbacks
 * by instance).
 */
public class ThanksgivingDishGameTests {
	@GameTest(maxTicks = 40)
	public void townVisitorsCannotPocketDisplayedMeals(GameTestHelper helper) throws Exception {
		floor(helper);
		ServerLevel level = helper.getLevel();
		var server = level.getServer();
		var dispatch = server.getCommands().getDispatcher();
		ServerPlayer warden = TestPlayers.mock(helper);
		var op = server.createCommandSourceStack().withEntity(warden).withSuppressedOutput();
		String region = "dishes_" + warden.getUUID().toString().replace("-", "");
		BlockPos low = helper.absolutePos(BlockPos.ZERO), high = helper.absolutePos(new BlockPos(7, 7, 7));
		warden.snapTo(low.getX(), low.getY(), low.getZ(), 0, 0);
		dispatch.execute("protect pos1", op);
		warden.snapTo(high.getX(), high.getY(), high.getZ(), 0, 0);
		dispatch.execute("protect pos2", op);
		dispatch.execute("protect create " + region, op);
		ServerPlayer visitor = player(helper, new BlockPos(3, 2, 6), ItemStack.EMPTY);
		try {
			for (int i = 0; i < ThanksgivingDishes.DISHES.size(); i++) {
				String id = ThanksgivingDishes.DISHES.get(i).id();
				BlockPos relative = new BlockPos(1+i, 2, 3), at = helper.absolutePos(relative);
				level.setBlockAndUpdate(at, block(id).defaultBlockState());
				helper.assertTrue(io.github.jimbozoomer.jugcraft.town.TownProtection.denies(visitor, level, at), "Fixture protected");
				use(helper, visitor, relative);
				helper.assertTrue(level.getBlockState(at).is(block(id)) && visitor.getMainHandItem().isEmpty(),
						"A visitor cannot take the displayed " + id);
			}
		} finally {
			dispatch.execute("protect remove " + region, op);
		}
		helper.succeed();
	}

	@GameTest(maxTicks = 20)
	public void everyDishIsABowlFoodThatGivesItsBowlBack(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		floor(helper);
		ServerPlayer player = player(helper, new BlockPos(3, 2, 6), ItemStack.EMPTY);
		for (ThanksgivingDishes.Dish dish : ThanksgivingDishes.DISHES) {
			ItemStack stack = new ItemStack(item(dish.id()));
			helper.assertTrue(stack.is(net.minecraft.tags.TagKey.create(Registries.ITEM, net.minecraft.resources.Identifier.parse("c:foods"))), dish.id() + " belongs in the shared food tag");
			FoodProperties food = stack.get(DataComponents.FOOD);
			FoodProperties table = new FoodProperties.Builder().nutrition(dish.nutrition()).saturationModifier(dish.saturation()).build();
			helper.assertTrue(food != null && food.nutrition() == dish.nutrition() && Math.abs(food.saturation() - table.saturation()) < 1.0E-4F,
					dish.id() + " restores its table's hunger and saturation: " + food);
			helper.assertTrue(stack.getMaxStackSize() == 1, dish.id() + " does not stack");
			helper.assertTrue(block(dish.id()) instanceof PlacedDishBlock placed && placed.dish() == item(dish.id())
					&& placed.dishShape() == dish.shape(), dish.id() + " sets down as itself, in its table's outline");
			player.getFoodData().setFoodLevel(2);
			ItemStack left = new ItemStack(item(dish.id())).finishUsingItem(level, player);
			helper.assertTrue(left.is(Items.BOWL) && player.getFoodData().getFoodLevel() == Math.min(20, 2 + dish.nutrition()),
					dish.id() + " is eaten, leaving its bowl: " + left + ", food " + player.getFoodData().getFoodLevel());
		}
		helper.succeed();
	}

	/**
	 * Every dish, held: not sneaking, it is not set down; sneaking, it is set down on the stone facing the player, and used
	 * up. Holding bread, using it leaves it; an empty hand takes it back. Set down again and broken, it drops its food once.
	 */
	@GameTest(maxTicks = 40)
	public void everyDishSetsDownFacingThePlayerAndIsTakenBack(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		floor(helper);
		ServerPlayer player = player(helper, new BlockPos(3, 2, 6), ItemStack.EMPTY);
		for (int i = 0; i < ThanksgivingDishes.DISHES.size(); i++) {
			ThanksgivingDishes.Dish dish = ThanksgivingDishes.DISHES.get(i);
			BlockPos ground = new BlockPos(1 + i, 1, 3);
			BlockPos at = ground.above();
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(item(dish.id())));
			useTop(helper, player, ground);
			helper.assertTrue(helper.getBlockState(at).isAir() && player.getMainHandItem().is(item(dish.id())),
					"Not sneaking, " + dish.id() + " is not set down");
			player.setShiftKeyDown(true);
			useTop(helper, player, ground);
			player.setShiftKeyDown(false);
			helper.assertTrue(helper.getBlockState(at).is(block(dish.id())) && player.getMainHandItem().isEmpty()
					&& helper.getBlockState(at).getValue(PlacedDishBlock.FACING) == Direction.SOUTH,
					"Sneaking, " + dish.id() + " is set down facing the player, and used up");
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BREAD));
			use(helper, player, at);
			helper.assertTrue(helper.getBlockState(at).is(block(dish.id())) && player.getMainHandItem().is(Items.BREAD),
					"Holding bread, " + dish.id() + " stays");
			player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
			use(helper, player, at);
			helper.assertTrue(helper.getBlockState(at).isAir() && player.getMainHandItem().is(item(dish.id())) && player.getMainHandItem().getCount() == 1,
					"An empty hand takes " + dish.id() + " back, one dish");
			player.setShiftKeyDown(true);
			useTop(helper, player, ground);
			player.setShiftKeyDown(false);
			helper.assertTrue(helper.getBlockState(at).is(block(dish.id())), dish.id() + " is set down again");
			level.destroyBlock(helper.absolutePos(at), true);
			helper.assertTrue(helper.getBlockState(at).isAir(), dish.id() + " breaks");
		}
		helper.runAfterDelay(2, () -> {
			for (ThanksgivingDishes.Dish dish : ThanksgivingDishes.DISHES) {
				helper.assertTrue(dropped(helper, item(dish.id())) == 1, "Broken, " + dish.id() + " drops its food once: " + dropped(helper, item(dish.id())));
			}
			helper.succeed();
		});
	}

	/**
	 * In a private claim the owner sets a dish down and takes it back; an outsider can neither set one down there, take the
	 * owner's, nor break it.
	 */
	@GameTest(maxTicks = 40)
	public void claimsKeepTheDishesToTheirMembers(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer owner = TestPlayers.survivor(helper);
		ServerPlayer stranger = TestPlayers.survivor(helper);
		BlockPos base = helper.absolutePos(new BlockPos(22528, 3, 22528));
		BlockPos origin = new BlockPos(Math.floorDiv(base.getX(), 16) * 16 + 4, base.getY(), Math.floorDiv(base.getZ(), 16) * 16 + 4);
		LandRegistry land = LandRegistry.get(level.getServer());
		var book = Ledger.get(level.getServer()).book();
		long key = LandRegistry.keyOf(origin);
		String carrots = ThanksgivingDishes.DISHES.get(0).id();
		String crisp = ThanksgivingDishes.DISHES.get(3).id();
		try {
			book.account(stranger.getUUID(), "DishVisitor");
			land.assign(book, key, book.account(owner.getUUID(), "DishOwner"), 0);
			for (int x = -2; x <= 6; x++) {
				for (int z = -4; z <= 3; z++) {
					level.setBlockAndUpdate(origin.offset(x, -1, z), Blocks.STONE.defaultBlockState());
					level.setBlockAndUpdate(origin.offset(x, 0, z), Blocks.AIR.defaultBlockState());
				}
			}
			BlockPos dish = origin, bare = origin.east(3);
			owner.snapTo(origin.getX() + 0.5, origin.getY(), origin.getZ() - 2.5, 0.0F, 0.0F);
			owner.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(item(carrots)));
			owner.setShiftKeyDown(true);
			useTopAt(owner, level, dish.below());
			owner.setShiftKeyDown(false);
			helper.assertTrue(level.getBlockState(dish).is(block(carrots)), "The claim's owner sets a dish down");

			stranger.snapTo(bare.getX() + 0.5, bare.getY(), bare.getZ() - 2.5, 0.0F, 0.0F);
			stranger.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(item(crisp)));
			stranger.setShiftKeyDown(true);
			useTopAt(stranger, level, bare.below());
			stranger.setShiftKeyDown(false);
			helper.assertTrue(level.getBlockState(bare).isAir() && stranger.getMainHandItem().is(item(crisp)),
					"An outsider cannot set a dish down on claimed land, and keeps it");
			stranger.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
			useAt(stranger, level, dish);
			helper.assertTrue(level.getBlockState(dish).is(block(carrots)) && stranger.getMainHandItem().isEmpty()
					&& stranger.getInventory().countItem(item(carrots)) == 0, "Nor take the owner's dish");
			helper.assertTrue(!stranger.gameMode.destroyBlock(dish) && level.getBlockState(dish).is(block(carrots)), "Nor break it");

			owner.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
			useAt(owner, level, dish);
			helper.assertTrue(level.getBlockState(dish).isAir() && owner.getMainHandItem().is(item(carrots)), "Its owner takes it back");
		} finally {
			land.abandon(owner, book, key, true, 0);
		}
		helper.succeed();
	}

	@GameTest(maxTicks = 20)
	public void everyRecipeAndLootTableLoads(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		for (ThanksgivingDishes.Dish dish : ThanksgivingDishes.DISHES) {
			helper.assertTrue(level.recipeAccess().byKey(ResourceKey.create(Registries.RECIPE, Jugcraft.id(dish.id()))).isPresent(),
					dish.id() + " has a recipe");
			helper.assertTrue(level.getServer().reloadableRegistries().getLootTable(ResourceKey.create(Registries.LOOT_TABLE,
					Jugcraft.id("blocks/" + dish.id()))) != LootTable.EMPTY, dish.id() + "'s loot loads");
		}
		helper.succeed();
	}

	// ---------------------------------------------------------------- helpers

	private static Item item(String id) {
		return JugcraftAgriculture.item(id);
	}

	private static Block block(String id) {
		return JugcraftAgriculture.block(id);
	}

	private static void floor(GameTestHelper helper) {
		for (int x = 0; x <= 7; x++) {
			for (int z = 0; z <= 7; z++) {
				helper.setBlock(new BlockPos(x, 1, z), Blocks.STONE);
			}
		}
	}

	/** A survival player standing at {@code standAt} (relative), looking north, holding {@code held}. */
	private static ServerPlayer player(GameTestHelper helper, BlockPos standAt, ItemStack held) {
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		player.setGameMode(GameType.SURVIVAL);
		BlockPos absolute = helper.absolutePos(standAt);
		player.setPos(absolute.getX() + 0.5, absolute.getY(), absolute.getZ() + 0.5);
		player.setYRot(180.0F);
		player.setItemInHand(InteractionHand.MAIN_HAND, held);
		return player;
	}

	/** Uses what the player holds on the top of {@code pos} (relative), through the server's normal use path. */
	private static void useTop(GameTestHelper helper, ServerPlayer player, BlockPos pos) {
		useTopAt(player, helper.getLevel(), helper.absolutePos(pos));
	}

	/** Uses what the player holds on the top of {@code absolute}, through the server's normal use path (claims apply). */
	private static void useTopAt(ServerPlayer player, ServerLevel level, BlockPos absolute) {
		BlockHitResult hit = new BlockHitResult(new Vec3(absolute.getX() + 0.5, absolute.getY() + 1.0, absolute.getZ() + 0.5), Direction.UP,
				absolute, false);
		player.gameMode.useItemOn(player, level, player.getMainHandItem(), InteractionHand.MAIN_HAND, hit);
	}

	/** Uses what the player holds on the set-down dish at {@code pos} (relative). */
	private static void use(GameTestHelper helper, ServerPlayer player, BlockPos pos) {
		useAt(player, helper.getLevel(), helper.absolutePos(pos));
	}

	/** Uses what the player holds on the block at {@code absolute} itself, through the server's normal use path. */
	private static void useAt(ServerPlayer player, ServerLevel level, BlockPos absolute) {
		BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(absolute), Direction.UP, absolute, false);
		player.gameMode.useItemOn(player, level, player.getMainHandItem(), InteractionHand.MAIN_HAND, hit);
	}

	private static int dropped(GameTestHelper helper, Item item) {
		AABB area = new AABB(helper.absolutePos(BlockPos.ZERO)).expandTowards(8, 8, 8).inflate(2.0);
		return helper.getLevel().getEntitiesOfClass(ItemEntity.class, area, entity -> entity.getItem().is(item)).stream()
				.mapToInt(entity -> entity.getItem().getCount()).sum();
	}
}

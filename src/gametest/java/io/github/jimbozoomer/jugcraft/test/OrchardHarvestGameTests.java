package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.agriculture.AppleBinBlock;
import io.github.jimbozoomer.jugcraft.agriculture.AppleCrateBlock;
import io.github.jimbozoomer.jugcraft.agriculture.CaramelAppleStandBlock;
import io.github.jimbozoomer.jugcraft.agriculture.CaramelAppleStandBlockEntity;
import io.github.jimbozoomer.jugcraft.agriculture.FruitBinBlock;
import io.github.jimbozoomer.jugcraft.agriculture.FruitStore;
import io.github.jimbozoomer.jugcraft.agriculture.FruitStoreBlockEntity;
import io.github.jimbozoomer.jugcraft.agriculture.JugcraftAgriculture;
import io.github.jimbozoomer.jugcraft.agriculture.MultiDecorationBlock;
import io.github.jimbozoomer.jugcraft.agriculture.OrchardHandcartBlock;
import io.github.jimbozoomer.jugcraft.agriculture.PickingBagBlock;
import io.github.jimbozoomer.jugcraft.agriculture.WindfallApplesBlock;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.ProblemReporter;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Orchard & Apple Harvest, part 1 (decor set 2; docs/features/orchard-apple-harvest.md): the Orchard Ladder stands whole
 * where it is placed, facing its maker, climbable in every block, and breaks whole, dropping once; the Apple Bin takes
 * fruit from any of its blocks up to its capacity, showing its fill and the fruit on top on every block, refuses what
 * is not fruit, gives back the last stack to an empty hand, remembers what it holds and spills it when broken; the
 * Picking Bag hangs only on a wall, takes what fits, shows it, and comes off spilling its fruit when the wall goes. Part 2:
 * apple crates stack three to a block and drop each crate; windfall apples gather four to a block, are walked through,
 * won't go on nothing and give back their apples when their ground goes; the Orchard Handcart holds two stacks and spills them.
 * Part 3: the Caramel Apple Stand won't dip without caramel and sticks, takes them in, dips an apple for an apple, a stick
 * and a serving, lights while it has caramel, lets the apple set before it is taken, and gives everything back when broken.
 */
public class OrchardHarvestGameTests {
	private static Item item(String id) {
		return JugcraftAgriculture.item(id);
	}

	private static Block block(String id) {
		return JugcraftAgriculture.block(id);
	}

	private static void floor(GameTestHelper helper) {
		for (int x = 0; x < 8; x++) {
			for (int z = 0; z < 8; z++) {
				helper.setBlock(new BlockPos(x, 1, z), Blocks.STONE);
			}
		}
	}

	private static BlockHitResult hit(GameTestHelper helper, BlockPos pos, Direction side) {
		BlockPos absolute = helper.absolutePos(pos);
		return new BlockHitResult(Vec3.atCenterOf(absolute).relative(side, 0.5), side, absolute, false);
	}

	private static InteractionResult use(GameTestHelper helper, ServerPlayer player, BlockPos pos, Direction side) {
		return player.gameMode.useItemOn(player, helper.getLevel(), player.getMainHandItem(), InteractionHand.MAIN_HAND, hit(helper, pos, side));
	}

	/** A survival player standing at {@code at}, looking south (yaw 0), holding {@code held}. */
	private static ServerPlayer standing(GameTestHelper helper, BlockPos at, ItemStack held) {
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		player.setGameMode(GameType.SURVIVAL);
		BlockPos absolute = helper.absolutePos(at);
		player.setPos(absolute.getX() + 0.5, absolute.getY(), absolute.getZ() + 0.5);
		player.setYRot(0.0F);
		player.setItemInHand(InteractionHand.MAIN_HAND, held);
		return player;
	}

	private static int dropped(GameTestHelper helper, Item item) {
		return helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(helper.absolutePos(new BlockPos(4, 2, 4))).inflate(8.0),
				entity -> entity.getItem().is(item)).stream().mapToInt(entity -> entity.getItem().getCount()).sum();
	}

	/** Places the prop in the player's hand on the floor at {@code at}, as they would. */
	private static void place(GameTestHelper helper, ServerPlayer player, BlockPos at) {
		player.getMainHandItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit(helper, at.below(), Direction.UP)));
	}

	/** Every block of the prop at {@code first} is there, facing north. */
	private static void assertWhole(GameTestHelper helper, MultiDecorationBlock block, BlockPos first, int blocks, String id) {
		ServerLevel level = helper.getLevel();
		helper.assertTrue(block.cells().length == blocks, id + " should be " + blocks + " blocks, is " + block.cells().length);
		for (int part = 0; part < blocks; part++) {
			BlockState there = level.getBlockState(block.partPos(first, Direction.NORTH, part));
			helper.assertTrue(there.is(block) && block.part(there) == part && there.getValue(MultiDecorationBlock.FACING) == Direction.NORTH,
					id + " is missing its block " + part + ": " + there);
		}
	}

	@GameTest(maxTicks = 40)
	public void theLadderStandsAndClimbs(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		floor(helper);
		BlockPos master = new BlockPos(3, 2, 2);
		ServerPlayer player = standing(helper, master.north(), new ItemStack(item("orchard_ladder")));
		place(helper, player, master);
		MultiDecorationBlock ladder = (MultiDecorationBlock) block("orchard_ladder");
		BlockPos first = helper.absolutePos(master);
		assertWhole(helper, ladder, first, 6, "orchard_ladder");
		for (int part = 0; part < 6; part++) {
			BlockPos at = ladder.partPos(first, Direction.NORTH, part);
			helper.assertTrue(level.getBlockState(at).is(BlockTags.CLIMBABLE) && !level.getBlockState(at).getShape(level, at).isEmpty(),
					"Each block of the ladder is climbable and has its piece of the ladder");
		}
		// Its rails stand towards the back of its front blocks: there is room in front of them to climb.
		helper.assertTrue(level.getBlockState(first).getShape(level, first).min(Direction.Axis.Z) >= 8.0 / 16,
				"There should be room in front of the rails, inside the ladder's block");
		player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
		BlockState before = level.getBlockState(first);
		use(helper, player, master, Direction.NORTH);
		helper.assertTrue(level.getBlockState(first) == before, "Use does nothing to it");
		level.destroyBlock(ladder.partPos(first, Direction.NORTH, 5), true);
		for (int part = 0; part < 6; part++) {
			helper.assertFalse(level.getBlockState(ladder.partPos(first, Direction.NORTH, part)).is(ladder), "It breaks whole");
		}
		helper.runAfterDelay(3, () -> {
			helper.assertTrue(dropped(helper, item("orchard_ladder")) == 1, "It drops itself once");
			helper.succeed();
		});
	}

	@GameTest(maxTicks = 40)
	public void theBinHoldsFourStacks(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		floor(helper);
		BlockPos master = new BlockPos(4, 2, 2);
		ServerPlayer player = standing(helper, master.north(), new ItemStack(item("apple_bin")));
		place(helper, player, master);
		AppleBinBlock bin = (AppleBinBlock) block("apple_bin");
		BlockPos first = helper.absolutePos(master);
		assertWhole(helper, bin, first, 4, "apple_bin");
		FruitStoreBlockEntity store = bin.store(level, first, level.getBlockState(first));
		helper.assertTrue(store != null && store.capacity() == AppleBinBlock.CAPACITY, "Its first block keeps its fruit, up to 256");
		BlockPos far = new BlockPos(3, 2, 3);
		// Apples through its far block: a stack fills it a quarter, shown on every block.
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.APPLE, 64));
		use(helper, player, far, Direction.UP);
		helper.assertTrue(store.count() == 64 && player.getMainHandItem().isEmpty(), "A stack of apples goes in: " + store.count());
		for (int part = 0; part < 4; part++) {
			BlockState there = level.getBlockState(bin.partPos(first, Direction.NORTH, part));
			helper.assertTrue(there.getValue(FruitBinBlock.FILL) == 1 && there.getValue(FruitStore.KIND) == FruitStore.Kind.APPLE,
					"Every block shows it a quarter full of apples: " + there);
		}
		// Pears on top: half full, pears showing.
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(item("pear"), 64));
		use(helper, player, master, Direction.UP);
		BlockState state = level.getBlockState(first);
		helper.assertTrue(store.count() == 128 && state.getValue(FruitBinBlock.FILL) == 2 && state.getValue(FruitStore.KIND) == FruitStore.Kind.PEAR,
				"Pears go in on top: " + state);
		// What is not fruit is not taken.
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.STICK, 8));
		use(helper, player, master, Direction.UP);
		helper.assertTrue(store.count() == 128 && player.getMainHandItem().getCount() == 8, "A stick isn't taken");
		// An empty hand takes back the last stack: the pears.
		player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
		use(helper, player, far, Direction.UP);
		helper.assertTrue(store.count() == 64 && player.getInventory().countItem(item("pear")) == 64
				&& level.getBlockState(first).getValue(FruitStore.KIND) == FruitStore.Kind.APPLE, "An empty hand takes back the pears");
		// Filled to the top, it takes no more.
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.APPLE, 64));
		for (int i = 0; i < 3; i++) {
			use(helper, player, master, Direction.UP);
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.APPLE, 64));
		}
		helper.assertTrue(store.count() == AppleBinBlock.CAPACITY && level.getBlockState(first).getValue(FruitBinBlock.FILL) == FruitBinBlock.STAGES,
				"Four stacks fill it: " + store.count());
		use(helper, player, master, Direction.UP);
		helper.assertTrue(store.count() == AppleBinBlock.CAPACITY && player.getMainHandItem().getCount() == 64, "Full, it takes no more");
		// It remembers what it holds.
		CompoundTag saved = store.saveWithoutMetadata(level.registryAccess());
		FruitStoreBlockEntity copy = new FruitStoreBlockEntity(first, level.getBlockState(first));
		copy.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), saved));
		helper.assertTrue(copy.count() == AppleBinBlock.CAPACITY, "It remembers what it holds: " + copy.count());
		// Broken at any block, it all goes, dropping itself once and spilling its fruit.
		level.destroyBlock(bin.partPos(first, Direction.NORTH, 3), true);
		helper.runAfterDelay(3, () -> {
			helper.assertTrue(dropped(helper, item("apple_bin")) == 1 && dropped(helper, Items.APPLE) == AppleBinBlock.CAPACITY,
					"It drops once and spills its apples: " + dropped(helper, item("apple_bin")) + ", " + dropped(helper, Items.APPLE));
			helper.succeed();
		});
	}

	@GameTest(maxTicks = 40)
	public void theBagHangsOnAWall(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		floor(helper);
		BlockPos wall = new BlockPos(3, 3, 4);
		helper.setBlock(wall, Blocks.OAK_PLANKS);
		BlockPos bagAt = wall.north();
		ServerPlayer player = standing(helper, new BlockPos(3, 2, 1), new ItemStack(item("picking_bag"), 2));
		// Not in mid-air: placed on the floor's top, with no wall behind it, it won't go.
		player.getMainHandItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit(helper, new BlockPos(6, 1, 6), Direction.UP)));
		helper.assertBlockNotPresent(block("picking_bag"), new BlockPos(6, 2, 6));
		player.getMainHandItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit(helper, wall, Direction.NORTH)));
		helper.assertBlockPresent(block("picking_bag"), bagAt);
		BlockState state = helper.getBlockState(bagAt);
		helper.assertTrue(state.getValue(PickingBagBlock.FACING) == Direction.NORTH, "It hangs facing out from the wall");
		FruitStoreBlockEntity store = helper.getBlockEntity(bagAt, FruitStoreBlockEntity.class);
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.APPLE, 20));
		use(helper, player, bagAt, Direction.NORTH);
		helper.assertTrue(store.count() == PickingBagBlock.CAPACITY && player.getMainHandItem().getCount() == 4
				&& helper.getBlockState(bagAt).getValue(PickingBagBlock.FILL) == PickingBagBlock.STAGES, "It takes the sixteen that fit and shows full");
		player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
		use(helper, player, bagAt, Direction.NORTH);
		helper.assertTrue(store.count() == 0 && helper.getBlockState(bagAt).getValue(PickingBagBlock.FILL) == 0,
				"An empty hand takes them back, and it shows empty");
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.APPLE, 5));
		use(helper, player, bagAt, Direction.NORTH);
		helper.assertTrue(helper.getBlockState(bagAt).getValue(PickingBagBlock.FILL) == 1, "Five show it partly full");
		helper.setBlock(wall, Blocks.AIR);
		helper.assertBlockNotPresent(block("picking_bag"), bagAt);
		helper.runAfterDelay(3, () -> {
			helper.assertTrue(dropped(helper, item("picking_bag")) == 1 && dropped(helper, Items.APPLE) == 5,
					"Without its wall it comes off, spilling its apples: " + dropped(helper, item("picking_bag")) + ", " + dropped(helper, Items.APPLE));
			helper.succeed();
		});
	}

	// ---------------------------------------------------------------- part 2

	private static void useOn(GameTestHelper helper, ServerPlayer player, BlockPos pos, Direction side) {
		player.getMainHandItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit(helper, pos, side)));
	}

	@GameTest(maxTicks = 40)
	public void cratesStackThreeHigh(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		floor(helper);
		BlockPos pos = new BlockPos(3, 2, 3);
		ServerPlayer player = standing(helper, new BlockPos(3, 2, 1), new ItemStack(item("red_apple_crate"), 5));
		useOn(helper, player, pos.below(), Direction.UP);
		useOn(helper, player, pos, Direction.UP);
		useOn(helper, player, pos, Direction.UP);
		BlockState state = helper.getBlockState(pos);
		BlockPos absolute = helper.absolutePos(pos);
		helper.assertTrue(state.getValue(AppleCrateBlock.CRATES) == AppleCrateBlock.MAX && state.getValue(AppleCrateBlock.FACING) == Direction.NORTH
				&& player.getMainHandItem().getCount() == 2, "Three crates stack in the block, facing the player: " + state);
		helper.assertTrue(Math.abs(state.getShape(level, absolute).max(Direction.Axis.Y) - 3 * AppleCrateBlock.CRATE_HEIGHT / 16) < 1e-6,
				"The stack's shape is three crates high");
		// A fourth goes on top, in the block above.
		useOn(helper, player, pos, Direction.UP);
		helper.assertTrue(helper.getBlockState(pos.above()).is(block("red_apple_crate")) && helper.getBlockState(pos.above()).getValue(AppleCrateBlock.CRATES) == 1,
				"A fourth crate starts a stack in the block above");
		// Another kind doesn't join the stack.
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(item("golden_apple_crate")));
		useOn(helper, player, pos.above(), Direction.UP);
		helper.assertTrue(helper.getBlockState(pos.above()).getValue(AppleCrateBlock.CRATES) == 1, "A golden crate doesn't join a red stack");
		level.destroyBlock(absolute, true);
		helper.runAfterDelay(3, () -> {
			helper.assertTrue(dropped(helper, item("red_apple_crate")) == 3, "Broken, the stack drops its three crates: " + dropped(helper, item("red_apple_crate")));
			helper.succeed();
		});
	}

	@GameTest(maxTicks = 40)
	public void windfallApplesLieInTheGrass(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		floor(helper);
		BlockPos pos = new BlockPos(3, 2, 3);
		ServerPlayer player = standing(helper, new BlockPos(3, 2, 1), new ItemStack(item("windfall_apples"), 5));
		useOn(helper, player, pos.below(), Direction.UP);
		for (int i = 0; i < 4; i++) {
			useOn(helper, player, pos, Direction.UP);
		}
		BlockState state = helper.getBlockState(pos);
		BlockPos absolute = helper.absolutePos(pos);
		helper.assertTrue(state.getValue(WindfallApplesBlock.APPLES) == WindfallApplesBlock.MAX && player.getMainHandItem().getCount() == 1,
				"Four gather in a block, and a fifth won't go on them: " + state + ", " + player.getMainHandItem().getCount() + " left");
		helper.assertTrue(state.getCollisionShape(level, absolute).isEmpty(), "They are walked through");
		helper.setBlock(pos.below(), Blocks.AIR);
		helper.assertBlockNotPresent(block("windfall_apples"), pos);
		helper.runAfterDelay(3, () -> {
			helper.assertTrue(dropped(helper, Items.APPLE) == 4, "Without their ground they come off, giving back four apples: " + dropped(helper, Items.APPLE));
			helper.succeed();
		});
	}

	@GameTest(maxTicks = 40)
	public void theHandcartHoldsTwoStacks(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		floor(helper);
		BlockPos master = new BlockPos(3, 2, 2);
		ServerPlayer player = standing(helper, master.north(), new ItemStack(item("orchard_handcart")));
		place(helper, player, master);
		OrchardHandcartBlock cart = (OrchardHandcartBlock) block("orchard_handcart");
		BlockPos first = helper.absolutePos(master);
		assertWhole(helper, cart, first, 2, "orchard_handcart");
		FruitStoreBlockEntity store = cart.store(level, first, level.getBlockState(first));
		helper.assertTrue(store != null && store.capacity() == OrchardHandcartBlock.CAPACITY, "Its first block keeps its fruit, up to 128");
		BlockPos back = new BlockPos(3, 2, 3);
		for (int i = 0; i < 3; i++) {
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.APPLE, 64));
			use(helper, player, back, Direction.UP);
		}
		helper.assertTrue(store.count() == OrchardHandcartBlock.CAPACITY && player.getMainHandItem().getCount() == 64,
				"Two stacks fill it, and a third isn't taken: " + store.count());
		for (int part = 0; part < 2; part++) {
			BlockState there = level.getBlockState(cart.partPos(first, Direction.NORTH, part));
			helper.assertTrue(there.getValue(FruitBinBlock.FILL) == FruitBinBlock.STAGES, "Both its blocks show it full: " + there);
		}
		level.destroyBlock(cart.partPos(first, Direction.NORTH, 1), true);
		helper.runAfterDelay(3, () -> {
			helper.assertTrue(dropped(helper, item("orchard_handcart")) == 1 && dropped(helper, Items.APPLE) == OrchardHandcartBlock.CAPACITY,
					"It drops once and spills its apples: " + dropped(helper, item("orchard_handcart")) + ", " + dropped(helper, Items.APPLE));
			helper.succeed();
		});
	}

	// ---------------------------------------------------------------- part 3

	@GameTest(maxTicks = CaramelAppleStandBlock.COOL_TICKS + 100)
	public void theStandDipsCaramelApples(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		floor(helper);
		BlockPos master = new BlockPos(4, 2, 2);
		ServerPlayer player = standing(helper, master.north(), new ItemStack(item("caramel_apple_stand")));
		place(helper, player, master);
		CaramelAppleStandBlock stand = (CaramelAppleStandBlock) block("caramel_apple_stand");
		BlockPos first = helper.absolutePos(master);
		assertWhole(helper, stand, first, 2, "caramel_apple_stand");
		CaramelAppleStandBlockEntity stock = stand.stock(level, first, level.getBlockState(first));
		helper.assertTrue(stock != null, "Its first block keeps its stock");
		// With nothing in the pot, an apple isn't dipped.
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.APPLE, 3));
		use(helper, player, master, Direction.UP);
		helper.assertTrue(stock.apples() == 0 && player.getMainHandItem().getCount() == 3, "No caramel, no dipping");
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(item("caramel"), 2));
		use(helper, player, master, Direction.UP);
		use(helper, player, master, Direction.UP);
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.STICK, 1));
		use(helper, player, new BlockPos(3, 2, 2), Direction.UP);
		helper.assertTrue(stock.caramel() == 2 && stock.sticks() == 1, "Caramel fills the pot and a stick the jar, from either block");
		BlockState state = level.getBlockState(first);
		helper.assertTrue(state.getValue(CaramelAppleStandBlock.POT) == 1 && state.getValue(CaramelAppleStandBlock.JAR) == 1
				&& state.getLightEmission() == CaramelAppleStandBlock.LIGHT, "It shows its caramel and sticks and the burner lights it: " + state);
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.APPLE, 3));
		use(helper, player, master, Direction.UP);
		helper.assertTrue(stock.apples() == 1 && stock.caramel() == 1 && stock.sticks() == 0 && player.getMainHandItem().getCount() == 2,
				"An apple, a stick and a serving make a caramel apple on the tray");
		use(helper, player, master, Direction.UP);
		helper.assertTrue(stock.apples() == 1 && player.getMainHandItem().getCount() == 2, "No sticks, no dipping");
		helper.assertTrue(level.getBlockState(stand.partPos(first, Direction.NORTH, 1)).getValue(CaramelAppleStandBlock.RACK_APPLES) == 1,
				"Both its blocks show the apple on the tray");
		// Not yet set: an empty hand takes nothing.
		player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
		use(helper, player, master, Direction.UP);
		helper.assertTrue(stock.apples() == 1 && player.getInventory().countItem(item("caramel_apple")) == 0, "It must set before it is taken");
		helper.runAfterDelay(CaramelAppleStandBlock.COOL_TICKS + 1, () -> {
			use(helper, player, master, Direction.UP);
			helper.assertTrue(stock.apples() == 0 && player.getInventory().countItem(item("caramel_apple")) == 1
					&& level.getBlockState(first).getValue(CaramelAppleStandBlock.RACK_APPLES) == 0, "Set, an empty hand takes it");
			level.destroyBlock(stand.partPos(first, Direction.NORTH, 1), true);
			helper.runAfterDelay(3, () -> {
				helper.assertTrue(dropped(helper, item("caramel_apple_stand")) == 1 && dropped(helper, item("caramel")) == 1,
						"Broken, it drops once and gives back its caramel: " + dropped(helper, item("caramel_apple_stand")) + ", "
								+ dropped(helper, item("caramel")));
				helper.succeed();
			});
		});
	}
}

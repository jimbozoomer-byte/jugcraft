package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.agriculture.CottonCandyCartBlock;
import io.github.jimbozoomer.jugcraft.agriculture.DecorationBlockEntity;
import io.github.jimbozoomer.jugcraft.agriculture.JugcraftAgriculture;
import io.github.jimbozoomer.jugcraft.agriculture.MultiDecorationBlock;
import io.github.jimbozoomer.jugcraft.agriculture.PopcornWagonBlock;
import java.util.function.Predicate;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Haunted Midway, part 1 (decor set 6; docs/features/haunted-midway.md): the Cotton-Candy Cart stands two blocks high,
 * facing the player, switched off; sugar isn't spun while it is off; an empty hand switches it on, on both blocks; three
 * sugars are spun into floss and a fourth isn't; Paper winds a serving onto a cone, a Cotton Candy, two times; switched
 * off, broken at its canopy it comes away whole, giving back itself and a sugar for the serving left. The Caramel-Corn
 * Wagon stands two blocks high, out; a kernel isn't taken while it is out; flint and steel lights both blocks, wearing the
 * flint, its upper block alone giving light {@value PopcornWagonBlock#LIGHT}; a kernel goes in the kettle and a second
 * isn't taken while it pops; after {@value PopcornWagonBlock#POP_TICKS} ticks it has popped into the case, the kettle's
 * spill marked; an empty hand takes the Popcorn; Caramel isn't spent on fewer than two; two more kernels pop and Caramel
 * coats the two into a Caramel Popcorn; a shovel puts it out. Broken while popping with three in its case, it gives back
 * itself, the three Popcorn and the kernel.
 */
public class HauntedMidwayGameTests {
	private static Item item(String id) {
		return JugcraftAgriculture.item(id);
	}

	private static void floor(GameTestHelper helper) {
		for (int x = 0; x < 8; x++) {
			for (int z = 0; z < 8; z++) {
				helper.setBlock(new BlockPos(x, 1, z), Blocks.STONE);
			}
		}
	}

	private static ServerPlayer standing(GameTestHelper helper, BlockPos at) {
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		player.setGameMode(GameType.SURVIVAL);
		BlockPos absolute = helper.absolutePos(at);
		player.setPos(absolute.getX() + 0.5, absolute.getY(), absolute.getZ() + 0.5);
		return player;
	}

	/** Uses {@code held} on the block at {@code absolute} (an absolute position) from its front. */
	private static void use(ServerPlayer player, ServerLevel level, BlockPos absolute, ItemStack held) {
		player.setItemInHand(InteractionHand.MAIN_HAND, held);
		player.gameMode.useItemOn(player, level, player.getMainHandItem(), InteractionHand.MAIN_HAND,
				new BlockHitResult(Vec3.atCenterOf(absolute).relative(Direction.NORTH, 0.5), Direction.NORTH, absolute, false));
	}

	/** Places {@code held} from the player's hand on the top of the block below {@code pos} (relative). */
	private static void place(ServerPlayer player, GameTestHelper helper, BlockPos pos, ItemStack held) {
		player.setItemInHand(InteractionHand.MAIN_HAND, held);
		BlockPos below = helper.absolutePos(pos.below());
		player.getMainHandItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
				new BlockHitResult(Vec3.atCenterOf(below).relative(Direction.UP, 0.5), Direction.UP, below, false)));
	}

	/** Whether both blocks of the two-high stall at {@code master} are {@code block}, in their parts, and pass {@code test}. */
	private static boolean both(ServerLevel level, MultiDecorationBlock block, BlockPos master, Predicate<BlockState> test) {
		for (int part = 0; part < block.cells().length; part++) {
			BlockState state = level.getBlockState(block.partPos(master, Direction.NORTH, part));
			if (!state.is(block) || block.part(state) != part || !test.test(state)) {
				return false;
			}
		}
		return true;
	}

	private static int dropped(GameTestHelper helper, Item item) {
		return helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(helper.absolutePos(new BlockPos(3, 3, 4))).inflate(5.0),
				entity -> entity.getItem().is(item)).stream().mapToInt(entity -> entity.getItem().getCount()).sum();
	}

	@GameTest(maxTicks = 40)
	public void theCottonCandyCartSpinsSugarIntoCandy(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		floor(helper);
		CottonCandyCartBlock cart = (CottonCandyCartBlock) JugcraftAgriculture.block("cotton_candy_cart");
		BlockPos pos = new BlockPos(3, 2, 4);
		BlockPos master = helper.absolutePos(pos);
		ServerPlayer player = standing(helper, new BlockPos(3, 2, 1));
		place(player, helper, pos, new ItemStack(item("cotton_candy_cart")));
		helper.assertTrue(both(level, cart, master, state -> state.getValue(MultiDecorationBlock.FACING) == Direction.NORTH
				&& !state.getValue(MultiDecorationBlock.LIT) && state.getValue(CottonCandyCartBlock.FLOSS_LEVEL) == 0),
				"It stands two blocks high, facing the player, switched off and empty");
		ItemStack sugar = new ItemStack(Items.SUGAR, 4);
		use(player, level, master, sugar);
		helper.assertTrue(sugar.getCount() == 4, "Sugar isn't spun while it is off");
		use(player, level, master.above(), ItemStack.EMPTY);
		helper.assertTrue(both(level, cart, master, state -> state.getValue(MultiDecorationBlock.LIT)), "An empty hand switches it on");
		for (int i = 0; i < 4; i++) {
			use(player, level, master, sugar);
		}
		helper.assertTrue(both(level, cart, master, state -> state.getValue(CottonCandyCartBlock.FLOSS_LEVEL) == CottonCandyCartBlock.FLOSS)
				&& sugar.getCount() == 1, "Three sugars are spun into floss and a fourth isn't");
		ItemStack paper = new ItemStack(Items.PAPER, 2);
		use(player, level, master, paper);
		use(player, level, master.above(), paper);
		helper.assertTrue(both(level, cart, master, state -> state.getValue(CottonCandyCartBlock.FLOSS_LEVEL) == 1) && paper.isEmpty()
				&& player.getInventory().countItem(item("cotton_candy")) == 2, "Paper winds a serving onto a cone, two times");
		use(player, level, master, ItemStack.EMPTY);
		helper.assertTrue(both(level, cart, master, state -> !state.getValue(MultiDecorationBlock.LIT)), "An empty hand switches it off");
		level.destroyBlock(master.above(), true);
		helper.runAfterDelay(3, () -> {
			helper.assertTrue(level.getBlockState(master).isAir() && level.getBlockState(master.above()).isAir()
					&& dropped(helper, item("cotton_candy_cart")) == 1 && dropped(helper, Items.SUGAR) == 1,
					"Broken at its canopy it comes away whole, giving back itself and a sugar for the serving left");
			helper.succeed();
		});
	}

	@GameTest(maxTicks = 260)
	public void thePopcornWagonPopsKernels(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		floor(helper);
		PopcornWagonBlock wagon = (PopcornWagonBlock) JugcraftAgriculture.block("popcorn_wagon");
		BlockPos pos = new BlockPos(3, 2, 4);
		BlockPos master = helper.absolutePos(pos);
		ServerPlayer player = standing(helper, new BlockPos(3, 2, 1));
		place(player, helper, pos, new ItemStack(item("popcorn_wagon")));
		helper.assertTrue(both(level, wagon, master, state -> !state.getValue(MultiDecorationBlock.LIT) && state.getValue(PopcornWagonBlock.POPCORN) == 0),
				"It stands two blocks high, out and empty");
		ItemStack kernels = new ItemStack(item("corn_kernels"), 4);
		use(player, level, master, kernels);
		helper.assertTrue(kernels.getCount() == 4, "A kernel isn't taken while it is out");
		ItemStack flint = new ItemStack(Items.FLINT_AND_STEEL);
		use(player, level, master.above(), flint);
		helper.assertTrue(both(level, wagon, master, state -> state.getValue(MultiDecorationBlock.LIT)) && flint.getDamageValue() == 1,
				"Flint and steel lights both blocks, wearing the flint");
		helper.assertTrue(level.getBlockState(master).getLightEmission() == 0
				&& level.getBlockState(master.above()).getLightEmission() == PopcornWagonBlock.LIGHT, "Its upper block alone gives light");
		use(player, level, master, kernels);
		use(player, level, master, kernels);
		helper.assertTrue(both(level, wagon, master, state -> state.getValue(PopcornWagonBlock.POPPING)) && kernels.getCount() == 3,
				"A kernel goes in the kettle, and a second isn't taken while it pops");
		ItemStack caramel = new ItemStack(item("caramel"), 2);
		helper.runAfterDelay(PopcornWagonBlock.POP_TICKS + 2, () -> {
			helper.assertTrue(both(level, wagon, master, state -> state.getValue(PopcornWagonBlock.POPCORN) == 1
					&& !state.getValue(PopcornWagonBlock.POPPING)), "It has popped into the case");
			helper.assertTrue(level.getBlockEntity(master) instanceof DecorationBlockEntity spill && spill.marked() != Long.MIN_VALUE,
					"The kettle's spill is marked");
			use(player, level, master.above(), ItemStack.EMPTY);
			helper.assertTrue(both(level, wagon, master, state -> state.getValue(PopcornWagonBlock.POPCORN) == 0)
					&& player.getInventory().countItem(item("popcorn")) == 1, "An empty hand takes the Popcorn");
			use(player, level, master, caramel);
			helper.assertTrue(caramel.getCount() == 2, "Caramel isn't spent on fewer than two");
			use(player, level, master, kernels);
		});
		helper.runAfterDelay(2 * (PopcornWagonBlock.POP_TICKS + 2), () -> use(player, level, master, kernels));
		helper.runAfterDelay(3 * (PopcornWagonBlock.POP_TICKS + 2), () -> {
			helper.assertTrue(both(level, wagon, master, state -> state.getValue(PopcornWagonBlock.POPCORN) == 2) && kernels.getCount() == 1,
					"Two more kernels have popped");
			use(player, level, master, caramel);
			helper.assertTrue(both(level, wagon, master, state -> state.getValue(PopcornWagonBlock.POPCORN) == 0) && caramel.getCount() == 1
					&& player.getInventory().countItem(item("caramel_popcorn")) == 1, "Caramel coats the two into a Caramel Popcorn");
			use(player, level, master, new ItemStack(Items.IRON_SHOVEL));
			helper.assertTrue(both(level, wagon, master, state -> !state.getValue(MultiDecorationBlock.LIT)), "A shovel puts it out");
			helper.succeed();
		});
	}

	@GameTest(maxTicks = 40)
	public void thePopcornWagonGivesBackWhatItHolds(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		floor(helper);
		PopcornWagonBlock wagon = (PopcornWagonBlock) JugcraftAgriculture.block("popcorn_wagon");
		BlockPos master = helper.absolutePos(new BlockPos(3, 2, 4));
		BlockState placed = wagon.defaultBlockState().setValue(MultiDecorationBlock.FACING, Direction.NORTH);
		level.setBlockAndUpdate(master, placed);
		wagon.setPlacedBy(level, master, placed, null, new ItemStack(wagon));
		for (int part = 0; part < wagon.cells().length; part++) {
			BlockPos at = wagon.partPos(master, Direction.NORTH, part);
			level.setBlock(at, level.getBlockState(at).setValue(PopcornWagonBlock.POPCORN, 3).setValue(PopcornWagonBlock.POPPING, true),
					Block.UPDATE_CLIENTS);
		}
		level.destroyBlock(master, true);
		helper.runAfterDelay(3, () -> {
			helper.assertTrue(level.getBlockState(master).isAir() && level.getBlockState(master.above()).isAir()
					&& dropped(helper, item("popcorn_wagon")) == 1 && dropped(helper, item("popcorn")) == 3
					&& dropped(helper, item("corn_kernels")) == 1,
					"Broken while popping with three in its case, it gives back itself, the three Popcorn and the kernel");
			helper.succeed();
		});
	}
}

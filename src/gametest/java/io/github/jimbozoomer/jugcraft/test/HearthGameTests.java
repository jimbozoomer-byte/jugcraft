package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.agriculture.JugcraftAgriculture;
import io.github.jimbozoomer.jugcraft.agriculture.MultiDecorationBlock;
import io.github.jimbozoomer.jugcraft.agriculture.StoneFireplaceBlock;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Cozy Fall Hearth, part 1 (decor set 5; docs/features/cozy-fall-hearth.md): the Stone Fireplace won't stand where any of
 * its nine blocks is taken and stands whole, three across and three up, where there is room; flint and steel lights every
 * block, wearing the flint, its firebox alone giving light {@value StoneFireplaceBlock#LIGHT}; a shovel puts it out. A Mantel Garland drapes every block and is spent; a second isn't; sneaking with an empty hand
 * takes it back. Broken at its firebox, lit and dressed, it all comes away, dropping the fireplace and its garland once.
 */
public class HearthGameTests {
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

	private static boolean all(ServerLevel level, StoneFireplaceBlock fireplace, BlockPos master, java.util.function.Predicate<BlockState> test) {
		for (int part = 0; part < fireplace.cells().length; part++) {
			BlockState state = level.getBlockState(fireplace.partPos(master, Direction.NORTH, part));
			if (!state.is(fireplace) || fireplace.part(state) != part || !test.test(state)) {
				return false;
			}
		}
		return true;
	}

	private static int dropped(GameTestHelper helper, Item item) {
		return helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(helper.absolutePos(new BlockPos(4, 3, 3))).inflate(5.0),
				entity -> entity.getItem().is(item)).stream().mapToInt(entity -> entity.getItem().getCount()).sum();
	}

	@GameTest(maxTicks = 40)
	public void theFireplaceLightsAndTakesItsGarland(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		floor(helper);
		StoneFireplaceBlock fireplace = (StoneFireplaceBlock) JugcraftAgriculture.block("stone_fireplace");
		BlockPos pos = new BlockPos(5, 2, 4);
		BlockPos master = helper.absolutePos(pos);
		ServerPlayer player = standing(helper, new BlockPos(4, 2, 1));
		// Its top corner (part 8, two to the player's right and two up) is taken: it won't stand.
		BlockPos corner = fireplace.partPos(master, Direction.NORTH, 8);
		level.setBlockAndUpdate(corner, Blocks.OAK_PLANKS.defaultBlockState());
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(item("stone_fireplace")));
		player.getMainHandItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
				new BlockHitResult(Vec3.atCenterOf(master.below()).relative(Direction.UP, 0.5), Direction.UP, master.below(), false)));
		helper.assertBlockNotPresent(fireplace, pos);
		level.setBlockAndUpdate(corner, Blocks.AIR.defaultBlockState());
		player.getMainHandItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
				new BlockHitResult(Vec3.atCenterOf(master.below()).relative(Direction.UP, 0.5), Direction.UP, master.below(), false)));
		helper.assertTrue(all(level, fireplace, master, state -> state.getValue(MultiDecorationBlock.FACING) == Direction.NORTH
				&& !state.getValue(MultiDecorationBlock.LIT) && !state.getValue(StoneFireplaceBlock.GARLAND)),
				"It stands whole, three across and three up, facing the player, out and bare");
		BlockPos box = fireplace.partPos(master, Direction.NORTH, StoneFireplaceBlock.FIREBOX);
		ItemStack flint = new ItemStack(Items.FLINT_AND_STEEL);
		use(player, level, box, flint);
		helper.assertTrue(all(level, fireplace, master, state -> state.getValue(MultiDecorationBlock.LIT)) && flint.getDamageValue() == 1,
				"Flint and steel lights every block, wearing the flint");
		for (int part = 0; part < fireplace.cells().length; part++) {
			int light = level.getBlockState(fireplace.partPos(master, Direction.NORTH, part)).getLightEmission();
			helper.assertTrue(light == (part == StoneFireplaceBlock.FIREBOX ? StoneFireplaceBlock.LIGHT : 0),
					"Its firebox alone gives light: block " + part + " gives " + light);
		}
		use(player, level, master.above(2), new ItemStack(Items.IRON_SHOVEL));
		helper.assertTrue(all(level, fireplace, master, state -> !state.getValue(MultiDecorationBlock.LIT)), "A shovel puts it out");
		ItemStack garlands = new ItemStack(item("mantel_garland"), 2);
		use(player, level, fireplace.partPos(master, Direction.NORTH, 4), garlands);
		helper.assertTrue(all(level, fireplace, master, state -> state.getValue(StoneFireplaceBlock.GARLAND)) && garlands.getCount() == 1,
				"A garland drapes every block and is spent");
		use(player, level, master, garlands);
		helper.assertTrue(garlands.getCount() == 1, "A second isn't");
		player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
		player.setShiftKeyDown(true);
		use(player, level, master, ItemStack.EMPTY);
		player.setShiftKeyDown(false);
		helper.assertTrue(all(level, fireplace, master, state -> !state.getValue(StoneFireplaceBlock.GARLAND))
				&& player.getInventory().countItem(item("mantel_garland")) == 1, "Sneaking with an empty hand takes it back");
		helper.succeed();
	}

	@GameTest(maxTicks = 40)
	public void theFireplaceComesAwayWhole(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		floor(helper);
		StoneFireplaceBlock fireplace = (StoneFireplaceBlock) JugcraftAgriculture.block("stone_fireplace");
		BlockPos master = helper.absolutePos(new BlockPos(5, 2, 4));
		BlockState placed = fireplace.defaultBlockState().setValue(MultiDecorationBlock.FACING, Direction.NORTH);
		level.setBlockAndUpdate(master, placed);
		fireplace.setPlacedBy(level, master, placed, null, new ItemStack(fireplace));
		ServerPlayer player = standing(helper, new BlockPos(4, 2, 1));
		BlockPos box = fireplace.partPos(master, Direction.NORTH, StoneFireplaceBlock.FIREBOX);
		use(player, level, box, new ItemStack(Items.FIRE_CHARGE));
		use(player, level, box, new ItemStack(item("mantel_garland")));
		helper.assertTrue(all(level, fireplace, master, state -> state.getValue(MultiDecorationBlock.LIT) && state.getValue(StoneFireplaceBlock.GARLAND)),
				"A fire charge lights it and a garland dresses it");
		level.destroyBlock(box, true);
		helper.runAfterDelay(3, () -> {
			for (int part = 0; part < fireplace.cells().length; part++) {
				helper.assertTrue(level.getBlockState(fireplace.partPos(master, Direction.NORTH, part)).isAir(), "Its block " + part + " is gone");
			}
			helper.assertTrue(dropped(helper, item("stone_fireplace")) == 1 && dropped(helper, item("mantel_garland")) == 1,
					"Broken at its firebox it comes away whole, dropping itself and its garland once");
			helper.succeed();
		});
	}
}

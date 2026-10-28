package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.agriculture.CookingPotBlockEntity;
import io.github.jimbozoomer.jugcraft.agriculture.FacingDecorationBlock;
import io.github.jimbozoomer.jugcraft.agriculture.JugcraftAgriculture;
import io.github.jimbozoomer.jugcraft.agriculture.MultiDecorationBlock;
import io.github.jimbozoomer.jugcraft.agriculture.StoneFireplaceBlock;
import io.github.jimbozoomer.jugcraft.agriculture.StovetopBlock;
import io.github.jimbozoomer.jugcraft.agriculture.WoodstoveBlock;
import java.util.List;
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
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Cozy Fall Hearth, part 1 (decor set 5; docs/features/cozy-fall-hearth.md): the Stone Fireplace won't stand where any of
 * its nine blocks is taken and stands whole, three across and three up, where there is room; flint and steel lights every
 * block, wearing the flint, its firebox alone giving light {@value StoneFireplaceBlock#LIGHT}; a shovel puts it out. A Mantel Garland drapes every block and is spent; a second isn't; sneaking with an empty hand
 * takes it back. Broken at its firebox, lit and dressed, it all comes away, dropping the fireplace and its garland once.
 * Part 2: the Woodstove stands facing the player, out; a Tea Kettle and a Simmer Pot stand on it cold; lit, it gives
 * light and heats them, and a Cooking Pot counts it as heat; a shovel puts it out and the kettle cools; broken, the
 * kettle comes off it and both drop. A kettle stands on stone, cold; a simmer pot on a lit campfire simmers and stops
 * when the fire goes out; with its floor gone, the kettle comes off.
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

	/** Places {@code held} from the player's hand on the top of the block below {@code pos} (relative). */
	private static void place(ServerPlayer player, GameTestHelper helper, BlockPos pos, ItemStack held) {
		player.setItemInHand(InteractionHand.MAIN_HAND, held);
		BlockPos below = helper.absolutePos(pos.below());
		player.getMainHandItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
				new BlockHitResult(Vec3.atCenterOf(below).relative(Direction.UP, 0.5), Direction.UP, below, false)));
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

	// ---------------------------------------------------------------- part 2

	@GameTest(maxTicks = 40)
	public void theWoodstoveHeatsWhatStandsOnIt(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		floor(helper);
		BlockPos stovePos = new BlockPos(2, 2, 4);
		BlockPos potStovePos = new BlockPos(5, 2, 4);
		ServerPlayer player = standing(helper, new BlockPos(3, 2, 1));
		for (BlockPos at : List.of(stovePos, potStovePos)) {
			place(player, helper, at, new ItemStack(item("woodstove")));
		}
		BlockState stove = helper.getBlockState(stovePos);
		helper.assertTrue(stove.is(JugcraftAgriculture.block("woodstove")) && stove.getValue(FacingDecorationBlock.FACING) == Direction.NORTH
				&& !stove.getValue(WoodstoveBlock.LIT), "It stands facing the player, out");
		place(player, helper, stovePos.above(), new ItemStack(item("tea_kettle")));
		place(player, helper, potStovePos.above(), new ItemStack(item("simmer_pot")));
		BlockPos kettle = helper.absolutePos(stovePos.above());
		BlockPos pot = helper.absolutePos(potStovePos.above());
		helper.assertTrue(level.getBlockState(kettle).is(JugcraftAgriculture.block("tea_kettle")) && !level.getBlockState(kettle).getValue(StovetopBlock.HEATED)
				&& level.getBlockState(pot).is(JugcraftAgriculture.block("simmer_pot")) && !level.getBlockState(pot).getValue(StovetopBlock.HEATED),
				"The kettle and the pot stand on the stoves, cold");
		ItemStack flint = new ItemStack(Items.FLINT_AND_STEEL);
		use(player, level, helper.absolutePos(stovePos), flint);
		use(player, level, helper.absolutePos(potStovePos), flint);
		helper.assertTrue(helper.getBlockState(stovePos).getValue(WoodstoveBlock.LIT) && flint.getDamageValue() == 2
				&& helper.getBlockState(stovePos).getLightEmission() == WoodstoveBlock.LIGHT, "Flint and steel lights them, and they give light");
		helper.assertTrue(level.getBlockState(kettle).getValue(StovetopBlock.HEATED) && level.getBlockState(pot).getValue(StovetopBlock.HEATED),
				"Lit, they heat the kettle and the pot");
		use(player, level, helper.absolutePos(stovePos), new ItemStack(Items.IRON_SHOVEL));
		helper.assertTrue(!helper.getBlockState(stovePos).getValue(WoodstoveBlock.LIT) && !level.getBlockState(kettle).getValue(StovetopBlock.HEATED),
				"A shovel puts it out, and the kettle cools");
		// A Cooking Pot counts it as heat, as it counts a campfire.
		level.setBlockAndUpdate(pot, Blocks.AIR.defaultBlockState());
		level.setBlockAndUpdate(pot, JugcraftAgriculture.block("cooking_pot").defaultBlockState());
		helper.assertTrue(CookingPotBlockEntity.isHeated(level, pot), "A Cooking Pot cooks on a lit woodstove");
		helper.getLevel().destroyBlock(helper.absolutePos(stovePos), true);
		helper.runAfterDelay(3, () -> {
			helper.assertTrue(level.getBlockState(kettle).isAir() && dropped(helper, item("tea_kettle")) == 1 && dropped(helper, item("woodstove")) == 1,
					"The stove broken, the kettle comes off it, and both drop");
			helper.succeed();
		});
	}

	@GameTest(maxTicks = 40)
	public void theKettleStandsOnAFloorOrAFire(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		floor(helper);
		ServerPlayer player = standing(helper, new BlockPos(3, 2, 1));
		BlockPos onStone = new BlockPos(2, 2, 3);
		place(player, helper, onStone, new ItemStack(item("tea_kettle")));
		helper.assertTrue(helper.getBlockState(onStone).is(JugcraftAgriculture.block("tea_kettle")) && !helper.getBlockState(onStone).getValue(StovetopBlock.HEATED),
				"It stands on stone, cold");
		BlockPos fire = new BlockPos(5, 2, 3);
		helper.setBlock(fire, Blocks.CAMPFIRE);
		place(player, helper, fire.above(), new ItemStack(item("simmer_pot")));
		helper.assertTrue(helper.getBlockState(fire.above()).getValue(StovetopBlock.HEATED), "On a lit campfire it simmers");
		helper.setBlock(fire, Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, false));
		helper.assertFalse(helper.getBlockState(fire.above()).getValue(StovetopBlock.HEATED), "Its fire out, it stops");
		helper.setBlock(onStone.below(), Blocks.AIR);
		helper.runAfterDelay(3, () -> {
			helper.assertTrue(helper.getBlockState(onStone).isAir() && dropped(helper, item("tea_kettle")) == 1, "Its floor gone, the kettle comes off");
			helper.succeed();
		});
	}
}

package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.agriculture.JugcraftAgriculture;
import io.github.jimbozoomer.jugcraft.agriculture.WoodlandCraftBlock;
import io.github.jimbozoomer.jugcraft.agriculture.WoodlandHarvestCrafts;
import io.github.jimbozoomer.jugcraft.agriculture.WoodlandRushMatBlock;
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
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Woodland harvest crafts (tools/thanksgiving_woodland.py; docs/features/thanksgiving-woodland-crafts.md). Written for the
 * server game tests; not run here (no Minecraft or Fabric toolchain in the environment that wrote them). Every floor piece
 * is placed from its item facing whoever placed it, dry unless placed in water, and drops itself once; it takes water,
 * needs a block under it and goes when that goes; using one with an empty hand changes nothing (decoration only). Rush
 * mats laid side by side join, only their outer edges left bound, take the player's direction for their rushes, split
 * when one is taken up, are a pixel thick to walk on, and go when the block under them goes. Each test checks its states
 * straight after the change it makes (no scheduled callbacks).
 */
public class WoodlandHarvestCraftsGameTests {
	@GameTest(maxTicks = 40)
	public void everyFloorCraftFacesItsPlacerAndDropsItself(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		floor(helper);
		BlockPos at = helper.absolutePos(new BlockPos(3, 2, 3));
		ServerPlayer player = standing(helper, new BlockPos(3, 2, 1));
		for (String id : WoodlandHarvestCrafts.FLOOR) {
			place(player, helper, new BlockPos(3, 2, 3), new ItemStack(item(id)));
			BlockState placed = level.getBlockState(at);
			helper.assertTrue(placed.is(block(id)) && placed.getValue(WoodlandCraftBlock.FACING) == Direction.NORTH
					&& !placed.getValue(WoodlandCraftBlock.WATERLOGGED), id + " stands where it was placed, facing the player, dry: " + placed);
			helper.assertTrue(player.getMainHandItem().isEmpty(), id + " is used up placing it");
			level.destroyBlock(at, true);
			helper.assertTrue(level.getBlockState(at).isAir() && dropped(helper, item(id)) == 1, id + ", broken, drops itself once");
			clearDrops(helper);
		}
		helper.succeed();
	}

	@GameTest(maxTicks = 40)
	public void floorCraftsTakeWaterAndNeedTheirSupport(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		floor(helper);
		ServerPlayer player = standing(helper, new BlockPos(3, 2, 1));
		for (String id : WoodlandHarvestCrafts.FLOOR) {
			BlockPos pos = new BlockPos(3, 2, 3);
			BlockPos at = helper.absolutePos(pos);
			helper.setBlock(pos, Blocks.WATER.defaultBlockState());
			place(player, helper, pos, new ItemStack(item(id)));
			BlockState placed = level.getBlockState(at);
			helper.assertTrue(placed.is(block(id)) && placed.getValue(WoodlandCraftBlock.WATERLOGGED)
					&& level.getFluidState(at).getType() == Fluids.WATER, id + " placed in water holds the water: " + placed);
			helper.assertTrue(!placed.canSurvive(level, at.above(2)), id + " cannot stand on air");
			helper.setBlock(pos.below(), Blocks.AIR);
			helper.assertTrue(!level.getBlockState(at).is(block(id)), id + " goes when the block under it goes");
			clearDrops(helper);
			helper.setBlock(pos, Blocks.AIR);
			helper.setBlock(pos.below(), Blocks.STONE);
		}
		helper.succeed();
	}

	@GameTest(maxTicks = 40)
	public void usingACraftChangesNothing(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		floor(helper);
		ServerPlayer player = standing(helper, new BlockPos(3, 2, 1));
		BlockPos at = helper.absolutePos(new BlockPos(3, 2, 3));
		for (String id : WoodlandHarvestCrafts.FLOOR) {
			BlockState state = block(id).defaultBlockState();
			helper.setBlock(new BlockPos(3, 2, 3), state);
			use(player, level, at, ItemStack.EMPTY);
			helper.assertTrue(level.getBlockState(at) == state && player.getInventory().isEmpty(),
					id + " is decoration: an empty hand changes nothing and gives nothing");
		}
		helper.succeed();
	}

	@GameTest(maxTicks = 40)
	public void rushMatsJoinIntoOneMat(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		floor(helper);
		Block mat = block(WoodlandHarvestCrafts.RUSH_FLOOR_MAT);
		// An L of mats: (2,3) (3,3) (4,3) and (2,4); set from their neighbours, as laying them by hand would.
		BlockPos[] laid = {new BlockPos(2, 2, 3), new BlockPos(3, 2, 3), new BlockPos(4, 2, 3), new BlockPos(2, 2, 4)};
		for (BlockPos pos : laid) {
			helper.setBlock(pos, mat.defaultBlockState());
		}
		for (BlockPos pos : laid) {
			helper.setBlock(pos, Block.updateFromNeighbourShapes(helper.getBlockState(pos), level, helper.absolutePos(pos)));
		}
		BlockState corner = helper.getBlockState(new BlockPos(2, 2, 3));
		BlockState middle = helper.getBlockState(new BlockPos(3, 2, 3));
		BlockState end = helper.getBlockState(new BlockPos(4, 2, 3));
		BlockState foot = helper.getBlockState(new BlockPos(2, 2, 4));
		helper.assertTrue(corner.getValue(WoodlandRushMatBlock.EAST) && corner.getValue(WoodlandRushMatBlock.SOUTH)
				&& !corner.getValue(WoodlandRushMatBlock.WEST) && !corner.getValue(WoodlandRushMatBlock.NORTH), "The corner joins east and south only: " + corner);
		helper.assertTrue(middle.getValue(WoodlandRushMatBlock.EAST) && middle.getValue(WoodlandRushMatBlock.WEST)
				&& !middle.getValue(WoodlandRushMatBlock.NORTH) && !middle.getValue(WoodlandRushMatBlock.SOUTH), "The middle joins both ends: " + middle);
		helper.assertTrue(end.getValue(WoodlandRushMatBlock.WEST) && !end.getValue(WoodlandRushMatBlock.EAST), "The end is bound on its outside: " + end);
		helper.assertTrue(foot.getValue(WoodlandRushMatBlock.NORTH) && !foot.getValue(WoodlandRushMatBlock.SOUTH), "The foot joins the corner: " + foot);
		helper.assertTrue(middle.getCollisionShape(level, helper.absolutePos(new BlockPos(3, 2, 3))).max(Direction.Axis.Y) == 1.0 / 16.0,
				"A mat is a pixel thick, walked on like a carpet");
		helper.setBlock(new BlockPos(3, 2, 3), Blocks.AIR);
		helper.assertTrue(!helper.getBlockState(new BlockPos(2, 2, 3)).getValue(WoodlandRushMatBlock.EAST)
				&& !helper.getBlockState(new BlockPos(4, 2, 3)).getValue(WoodlandRushMatBlock.WEST), "Taking up the middle splits them");
		helper.setBlock(new BlockPos(2, 1, 4), Blocks.AIR);
		helper.assertTrue(!helper.getBlockState(new BlockPos(2, 2, 4)).is(mat), "A mat goes when the block under it goes");
		helper.assertTrue(!helper.getBlockState(new BlockPos(2, 2, 3)).getValue(WoodlandRushMatBlock.SOUTH), "and its neighbour is bound again");
		helper.succeed();
	}

	@GameTest(maxTicks = 40)
	public void aLaidMatTakesThePlayersDirectionAndDropsItself(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		floor(helper);
		ServerPlayer player = standing(helper, new BlockPos(3, 2, 1));
		BlockPos at = helper.absolutePos(new BlockPos(3, 2, 3));
		place(player, helper, new BlockPos(3, 2, 3), new ItemStack(item(WoodlandHarvestCrafts.RUSH_FLOOR_MAT)));
		BlockState laid = level.getBlockState(at);
		helper.assertTrue(laid.is(block(WoodlandHarvestCrafts.RUSH_FLOOR_MAT))
				&& laid.getValue(WoodlandRushMatBlock.AXIS) == player.getDirection().getAxis(), "Its rushes run the way the player faced: " + laid);
		level.destroyBlock(at, true);
		helper.assertTrue(dropped(helper, item(WoodlandHarvestCrafts.RUSH_FLOOR_MAT)) == 1, "Broken, it drops itself once");
		helper.succeed();
	}

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

	private static int dropped(GameTestHelper helper, Item item) {
		return helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(helper.absolutePos(new BlockPos(3, 3, 4))).inflate(5.0),
				entity -> entity.getItem().is(item)).stream().mapToInt(entity -> entity.getItem().getCount()).sum();
	}

	private static void clearDrops(GameTestHelper helper) {
		helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(helper.absolutePos(new BlockPos(3, 3, 4))).inflate(5.0))
				.forEach(ItemEntity::discard);
	}
}

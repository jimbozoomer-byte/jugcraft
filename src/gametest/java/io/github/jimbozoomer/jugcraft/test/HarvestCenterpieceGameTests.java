package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.agriculture.HarvestCenterpieceBlock;
import io.github.jimbozoomer.jugcraft.agriculture.JugcraftAgriculture;
import io.github.jimbozoomer.jugcraft.agriculture.MultiDecorationBlock;
import java.util.HashSet;
import java.util.Set;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * The Harvest Centerpiece (tools/harvest_festival.py CENTERPIECE; Thanksgiving phase 6): five by three by five blocks
 * placed and broken as one, with no block entity, its lantern blocks lit and put out together, and the same cells and
 * one drop whether a player places it or a town scene sets its 75 parts block by block.
 */
public class HarvestCenterpieceGameTests {
	private static final String ID = "harvest_centerpiece";
	private static final int ACROSS = 5, UP = 3, DEEP = 5, PARTS = ACROSS * UP * DEEP;

	private static HarvestCenterpieceBlock block() {
		return (HarvestCenterpieceBlock) JugcraftAgriculture.block(ID);
	}

	private static Item item() {
		return JugcraftAgriculture.item(ID);
	}

	private static void floor(GameTestHelper helper) {
		for (int x = 0; x <= 7; x++) {
			for (int z = 0; z <= 7; z++) {
				helper.setBlock(new BlockPos(x, 1, z), Blocks.STONE);
			}
		}
	}

	private static ServerPlayer player(GameTestHelper helper, BlockPos standAt, ItemStack held) {
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		player.setGameMode(GameType.SURVIVAL);
		BlockPos absolute = helper.absolutePos(standAt);
		player.setPos(absolute.getX() + 0.5, absolute.getY(), absolute.getZ() + 0.5);
		player.setItemInHand(InteractionHand.MAIN_HAND, held);
		return player;
	}

	private static BlockHitResult hit(GameTestHelper helper, BlockPos pos, Direction side) {
		BlockPos absolute = helper.absolutePos(pos);
		return new BlockHitResult(Vec3.atCenterOf(absolute).relative(side, 0.5), side, absolute, false);
	}

	private static InteractionResult place(GameTestHelper helper, ServerPlayer player, BlockPos on) {
		return player.getMainHandItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit(helper, on, Direction.UP)));
	}

	private static InteractionResult use(GameTestHelper helper, ServerPlayer player, BlockPos pos) {
		return player.gameMode.useItemOn(player, helper.getLevel(), player.getMainHandItem(), InteractionHand.MAIN_HAND,
				hit(helper, pos, Direction.UP));
	}

	private static int dropped(GameTestHelper helper) {
		AABB area = new AABB(helper.absolutePos(BlockPos.ZERO)).expandTowards(8, 8, 8).inflate(2.0);
		return helper.getLevel().getEntitiesOfClass(ItemEntity.class, area, entity -> entity.getItem().is(item())).stream()
				.mapToInt(entity -> entity.getItem().getCount()).sum();
	}

	/** Where part {@code part} of a centerpiece with its root at {@code root} facing south (placed by a player looking north) is. */
	private static BlockPos southFacing(BlockPos root, int part) {
		int right = part % ACROSS, up = part / ACROSS % UP, away = part / (ACROSS * UP);
		return root.offset(right, up, -away); // right is east, away is north
	}

	/**
	 * Placed by a player looking north from the block in front of it, it fills five across to their right (east), three
	 * up and five away (north), every part facing them; it won't go where a block is in its way, and nothing is used up
	 * then; no part has a block entity; its lantern blocks (and only those) give light while lit; using any block puts
	 * them all out and lights them again; breaking its far top corner breaks it all and drops it once.
	 */
	@GameTest(maxTicks = 40)
	public void theCenterpieceStandsAsOneAndLightsItsLanterns(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		floor(helper);
		BlockPos root = new BlockPos(1, 2, 6);
		ServerPlayer player = player(helper, new BlockPos(3, 2, 7), new ItemStack(item(), 1));
		player.setYRot(180.0F); // looking north: their right is east
		BlockPos blocker = southFacing(root, PARTS - 1);
		helper.setBlock(blocker, Blocks.STONE);
		InteractionResult refused = place(helper, player, root.below());
		helper.assertTrue(!refused.consumesAction() && player.getMainHandItem().getCount() == 1,
				"A block in its way refuses it, using nothing: " + refused);
		helper.assertBlockNotPresent(block(), root);
		helper.setBlock(blocker, Blocks.AIR);
		InteractionResult placed = place(helper, player, root.below());
		helper.assertTrue(placed.consumesAction() && player.getMainHandItem().isEmpty(), "Placed, it uses the item: " + placed);
		for (int part = 0; part < PARTS; part++) {
			BlockPos at = southFacing(root, part);
			BlockState state = helper.getBlockState(at);
			helper.assertTrue(state.is(block()) && state.getValue(HarvestCenterpieceBlock.PART) == part
					&& state.getValue(MultiDecorationBlock.FACING) == Direction.SOUTH && state.getValue(MultiDecorationBlock.LIT),
					"Part " + part + " at " + at + " is " + state);
			helper.assertTrue(level.getBlockEntity(helper.absolutePos(at)) == null, "Part " + part + " has a block entity");
			int light = state.getLightEmission();
			helper.assertTrue(light == (HarvestCenterpieceBlock.LANTERNS.contains(part) ? HarvestCenterpieceBlock.LIGHT : 0),
					"Part " + part + " gives light " + light);
		}
		player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
		use(helper, player, southFacing(root, 2));
		for (int part = 0; part < PARTS; part++) {
			BlockState state = helper.getBlockState(southFacing(root, part));
			helper.assertTrue(!state.getValue(MultiDecorationBlock.LIT) && state.getLightEmission() == 0, "Used, part " + part + " goes out");
		}
		use(helper, player, southFacing(root, 62));
		for (int part : HarvestCenterpieceBlock.LANTERNS) {
			helper.assertTrue(helper.getBlockState(southFacing(root, part)).getLightEmission() == HarvestCenterpieceBlock.LIGHT,
					"Used again, lantern block " + part + " is lit");
		}
		level.destroyBlock(helper.absolutePos(southFacing(root, PARTS - 1)), true);
		helper.runAfterDelay(3, () -> {
			for (int part = 0; part < PARTS; part++) {
				helper.assertBlockNotPresent(block(), southFacing(root, part));
			}
			helper.assertTrue(dropped(helper) == 1, "Breaking a corner breaks it all, dropping it once, not " + dropped(helper));
			helper.succeed();
		});
	}

	/**
	 * Set block by block as a town scene does (every part's state at partPos, no setPlacedBy), in each facing, inside the
	 * same 5 x 5 floor: the 75 cells are distinct and inside it, each finds the root as its master, and breaking the root
	 * first drops it once and takes every other part away without a drop.
	 */
	@GameTest(maxTicks = 40)
	public void aSceneSetsItCellByCellInEveryFacing(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		floor(helper);
		HarvestCenterpieceBlock block = block();
		Direction[] facings = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
		BlockPos[] roots = {new BlockPos(5, 2, 1), new BlockPos(5, 2, 5), new BlockPos(1, 2, 5), new BlockPos(1, 2, 1)};
		for (int round = 0; round < facings.length; round++) {
			Direction facing = facings[round];
			BlockPos root = helper.absolutePos(roots[round]);
			Set<BlockPos> cells = new HashSet<>();
			for (int part = 0; part < PARTS; part++) {
				BlockPos at = block.partPos(root, facing, part);
				BlockPos relative = helper.relativePos(at);
				helper.assertTrue(relative.getX() >= 1 && relative.getX() <= 5 && relative.getZ() >= 1 && relative.getZ() <= 5
						&& relative.getY() >= 2 && relative.getY() <= 4, facing + " part " + part + " leaves the floor at " + relative);
				helper.assertTrue(cells.add(at), facing + " part " + part + " shares a cell");
				level.setBlock(at, block.defaultBlockState().setValue(MultiDecorationBlock.FACING, facing)
						.setValue(MultiDecorationBlock.LIT, true).setValue(HarvestCenterpieceBlock.PART, part), 3);
			}
			for (BlockPos at : cells) {
				BlockState state = level.getBlockState(at);
				helper.assertTrue(state.is(block) && block.masterPos(at, state).equals(root), facing + ": " + at + " does not find its root");
			}
			level.destroyBlock(root, true);
			for (BlockPos at : cells) {
				helper.assertTrue(!level.getBlockState(at).is(block), facing + ": " + at + " was left behind by the root");
			}
			helper.assertTrue(dropped(helper) == round + 1, facing + ": breaking the root first drops it once, not "
					+ (dropped(helper) - round));
		}
		helper.succeed();
	}
}

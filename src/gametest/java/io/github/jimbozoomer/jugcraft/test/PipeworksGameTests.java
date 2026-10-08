package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.building.Pipeworks;
import io.github.jimbozoomer.jugcraft.building.PipeworksBlock;
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
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/** Pipeworks (docs/features/pipeworks.md): the props stand as one and go as one. */
public class PipeworksGameTests {
	private static PipeworksBlock block(String id) {
		return Pipeworks.BLOCKS.get(id);
	}

	private static Item item(String id) {
		return block(id).asItem();
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

	private static void place(GameTestHelper helper, ServerPlayer player, BlockPos pos, Direction side) {
		player.getMainHandItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit(helper, pos, side)));
	}

	private static int dropped(GameTestHelper helper, Item item) {
		AABB area = new AABB(helper.absolutePos(BlockPos.ZERO)).expandTowards(8, 8, 8).inflate(2.0);
		List<ItemEntity> drops = helper.getLevel().getEntitiesOfClass(ItemEntity.class, area, entity -> entity.getItem().is(item));
		return drops.stream().mapToInt(entity -> entity.getItem().getCount()).sum();
	}

	/**
	 * A horizontal tank (two wide, two tall, four long) goes from the block aimed at to the player's right, up and away
	 * from them, facing them, every block of it the tank with its own part; it won't go where a block is in its way; it
	 * has something to stand on in every block; breaking a far corner breaks it all and drops one tank.
	 */
	@GameTest(maxTicks = 40)
	public void theTankStandsAsOne(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		floor(helper);
		PipeworksBlock tank = block("horizontal_tank");
		ServerPlayer player = player(helper, new BlockPos(2, 2, 0), new ItemStack(item("horizontal_tank"), 2));
		player.setYRot(0.0F); // looking south: their right is west (-x), away is south (+z)
		helper.setBlock(new BlockPos(1, 2, 3), Blocks.STONE);
		place(helper, player, new BlockPos(2, 1, 2), Direction.UP);
		helper.assertBlockNotPresent(tank, new BlockPos(2, 2, 2));
		helper.setBlock(new BlockPos(1, 2, 3), Blocks.AIR);
		place(helper, player, new BlockPos(2, 1, 2), Direction.UP);
		for (int part = 0; part < tank.parts(); part++) {
			int[] cell = tank.cell(part);
			BlockPos pos = new BlockPos(2 - cell[0], 2 + cell[1], 2 + cell[2]);
			BlockState state = helper.getBlockState(pos);
			helper.assertTrue(state.is(tank) && state.getValue(PipeworksBlock.PART) == part
					&& state.getValue(PipeworksBlock.FACING) == Direction.NORTH, "Part " + part + " is where it should be: " + state);
			helper.assertTrue(!state.getShape(level, helper.absolutePos(pos)).isEmpty(), "and has a shape to stand on");
		}
		helper.assertTrue(player.getMainHandItem().getCount() == 1, "One tank was used");
		level.destroyBlock(helper.absolutePos(new BlockPos(1, 3, 5)), true);
		helper.runAfterDelay(3, () -> {
			for (int part = 0; part < tank.parts(); part++) {
				int[] cell = tank.cell(part);
				helper.assertBlockNotPresent(tank, new BlockPos(2 - cell[0], 2 + cell[1], 2 + cell[2]));
			}
			helper.assertTrue(dropped(helper, item("horizontal_tank")) == 1, "Breaking a corner breaks it all, dropping it once");
			helper.succeed();
		});
	}

	/** Every prop places in the open and fills exactly its size; a one-block stub is just itself. */
	@GameTest(maxTicks = 40)
	public void everyPropFillsItsSize(GameTestHelper helper) {
		floor(helper);
		ServerPlayer player = player(helper, new BlockPos(7, 2, 0), ItemStack.EMPTY);
		player.setYRot(0.0F);
		for (String id : Pipeworks.BLOCKS.keySet()) {
			PipeworksBlock prop = block(id);
			if (prop.across() > 7 || prop.away() > 6) {
				continue;
			}
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(item(id)));
			place(helper, player, new BlockPos(6, 1, 1), Direction.UP);
			int filled = 0;
			for (int x = 0; x <= 7; x++) {
				for (int y = 2; y <= 7; y++) {
					for (int z = 0; z <= 7; z++) {
						if (helper.getBlockState(new BlockPos(x, y, z)).is(prop)) {
							filled++;
						}
					}
				}
			}
			helper.assertTrue(filled == prop.parts(), id + " fills " + filled + " blocks, not " + prop.parts());
			player.setGameMode(GameType.CREATIVE);
			helper.getLevel().destroyBlock(helper.absolutePos(new BlockPos(6, 2, 1)), false);
			player.setGameMode(GameType.SURVIVAL);
			for (int x = 0; x <= 7; x++) {
				for (int y = 2; y <= 7; y++) {
					for (int z = 0; z <= 7; z++) {
						helper.setBlock(new BlockPos(x, y, z), Blocks.AIR);
					}
				}
			}
		}
		helper.succeed();
	}
}

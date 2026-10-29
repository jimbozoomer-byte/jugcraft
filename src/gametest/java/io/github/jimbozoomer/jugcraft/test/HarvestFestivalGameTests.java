package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.agriculture.GiantCornucopiaBlock;
import io.github.jimbozoomer.jugcraft.agriculture.JugcraftAgriculture;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
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
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * The Harvest Festival Grounds (decor set 10; docs/features/harvest-festival-grounds.md). Part 1: the Giant Cornucopia
 * stands whole where it is placed, three blocks across, two up and two deep, facing the player, each block with its piece
 * of the horn's shape; use does nothing to it; broken at its tail, it all goes and drops itself once; and its recipe and
 * loot load.
 */
public class HarvestFestivalGameTests {
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

	private static int dropped(GameTestHelper helper, Item item) {
		return helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(helper.absolutePos(new BlockPos(4, 2, 4))).inflate(8.0),
				entity -> entity.getItem().is(item)).stream().mapToInt(entity -> entity.getItem().getCount()).sum();
	}

	@GameTest(maxTicks = 40)
	public void theGiantCornucopiaStandsWhole(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		floor(helper);
		BlockPos master = new BlockPos(2, 2, 2);
		Item item = JugcraftAgriculture.item("giant_cornucopia");
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		player.setGameMode(GameType.SURVIVAL);
		BlockPos stand = helper.absolutePos(master.north());
		player.setPos(stand.getX() + 0.5, stand.getY(), stand.getZ() + 0.5);
		player.setYRot(0.0F);
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(item));
		player.getMainHandItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit(helper, master.below(), Direction.UP)));
		GiantCornucopiaBlock block = (GiantCornucopiaBlock) JugcraftAgriculture.block("giant_cornucopia");
		BlockPos first = helper.absolutePos(master);
		helper.assertTrue(level.getBlockState(first).is(block) && level.getBlockState(first).getValue(GiantCornucopiaBlock.FACING) == Direction.NORTH,
				"It stands where it was placed, facing the player");
		helper.assertTrue(block.cells().length == 12 && block.across() == 3 && block.deep() == 2, "It is three by two by two blocks");
		for (int part = 0; part < 12; part++) {
			BlockPos at = block.partPos(first, Direction.NORTH, part);
			BlockState there = level.getBlockState(at);
			helper.assertTrue(there.is(block) && block.part(there) == part, "It is missing its block " + part + ": " + there);
		}
		player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
		BlockState before = level.getBlockState(first);
		player.gameMode.useItemOn(player, level, player.getMainHandItem(), InteractionHand.MAIN_HAND, hit(helper, master, Direction.NORTH));
		helper.assertTrue(level.getBlockState(first) == before, "Use does nothing to it");
		// Its tail curls up over its first block's back (the east end, its mouth to the west).
		level.destroyBlock(block.partPos(first, Direction.NORTH, 6), true);
		for (int part = 0; part < 12; part++) {
			helper.assertFalse(level.getBlockState(block.partPos(first, Direction.NORTH, part)).is(block), "It breaks whole");
		}
		helper.runAfterDelay(3, () -> {
			helper.assertTrue(dropped(helper, item) == 1, "It drops itself once");
			helper.succeed();
		});
	}

	@GameTest
	public void harvestFestivalDataLoads(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		for (String id : new String[] {"giant_cornucopia"}) {
			helper.assertTrue(level.recipeAccess().byKey(ResourceKey.create(Registries.RECIPE, Jugcraft.id(id))).isPresent(), id + " has a recipe");
			helper.assertTrue(level.getServer().reloadableRegistries().getLootTable(ResourceKey.create(Registries.LOOT_TABLE,
					Jugcraft.id("blocks/" + id))) != LootTable.EMPTY, id + "'s loot loads");
		}
		helper.succeed();
	}
}

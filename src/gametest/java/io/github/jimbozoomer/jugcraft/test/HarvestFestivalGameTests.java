package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.agriculture.FacingDecorationBlock;
import io.github.jimbozoomer.jugcraft.agriculture.GiantCornucopiaBlock;
import io.github.jimbozoomer.jugcraft.agriculture.JugcraftAgriculture;
import io.github.jimbozoomer.jugcraft.agriculture.PrizeRibbonBoardBlock;
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
import net.minecraft.world.item.Items;
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
 * loot load. Part 2: each show vegetable stands facing the player and drops itself; the Prize Ribbon Board stands two
 * blocks tall, hangs each prize ribbon on its own hook (both halves), refuses a second on a taken hook and anything that
 * is not a ribbon, gives back the ribbon nearest where an empty hand aims, does nothing for a player who may not build,
 * and broken drops itself and the ribbons hanging on it; and their recipes and loot load.
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

	/** Places {@code id} from the player's hand on the block below {@code at}, the player looking south: it faces north. */
	private static void place(GameTestHelper helper, ServerPlayer player, String id, BlockPos at) {
		player.setYRot(0.0F);
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(JugcraftAgriculture.item(id)));
		player.getMainHandItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit(helper, at.below(), Direction.UP)));
	}

	private static ServerPlayer player(GameTestHelper helper, BlockPos at) {
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		player.setGameMode(GameType.SURVIVAL);
		BlockPos stand = helper.absolutePos(at);
		player.setPos(stand.getX() + 0.5, stand.getY(), stand.getZ() + 0.5);
		return player;
	}

	/** Each show vegetable stands where it is placed, facing the player; broken, it drops itself once. */
	@GameTest(maxTicks = 40)
	public void theShowVegetablesStandAndDropThemselves(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		floor(helper);
		String[] ids = {"giant_cabbage", "giant_marrow", "giant_turnip", "giant_carrot"};
		for (int i = 0; i < ids.length; i++) {
			BlockPos at = new BlockPos(1 + 2 * i, 2, 4);
			place(helper, player(helper, at.north()), ids[i], at);
			BlockState state = level.getBlockState(helper.absolutePos(at));
			helper.assertTrue(state.is(JugcraftAgriculture.block(ids[i])) && state.getValue(FacingDecorationBlock.FACING) == Direction.NORTH,
					ids[i] + " stands where it was placed, facing the player: " + state);
			level.destroyBlock(helper.absolutePos(at), true);
		}
		helper.runAfterDelay(3, () -> {
			for (String id : ids) {
				helper.assertTrue(dropped(helper, JugcraftAgriculture.item(id)) == 1, id + " drops itself once");
			}
			helper.succeed();
		});
	}

	/** Where a player aims at hook {@code hook} (0 to 2, from the left as they face it) on a north-facing board's face. */
	private static BlockHitResult aim(GameTestHelper helper, BlockPos upper, int hook) {
		BlockPos at = helper.absolutePos(upper);
		double x = at.getX() + 1.0 - (hook + 0.5) / 3.0;
		return new BlockHitResult(new Vec3(x, at.getY() + 0.25, at.getZ() + 6.5 / 16.0), Direction.NORTH, at, false);
	}

	private static void use(ServerPlayer player, ServerLevel level, BlockHitResult hit) {
		player.gameMode.useItemOn(player, level, player.getMainHandItem(), InteractionHand.MAIN_HAND, hit);
	}

	/**
	 * The Prize Ribbon Board stands two blocks tall facing the player; a First Prize Ribbon hangs on its first hook (on both
	 * halves) wherever it is aimed, using it up, and a second one is refused, kept; an apple hangs on nothing; a player who
	 * may not build hangs nothing; a Third hangs on the third hook; an empty hand aimed at the third hook takes back the
	 * Third, and aimed there again, the nearest ribbon left, the First; broken at its top with a First hanging, it drops
	 * itself and the First.
	 */
	@GameTest(maxTicks = 40)
	public void thePrizeRibbonBoardHangsRibbons(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		floor(helper);
		BlockPos lower = new BlockPos(3, 2, 3);
		BlockPos upper = lower.above();
		ServerPlayer player = player(helper, lower.north());
		place(helper, player, "prize_ribbon_board", lower);
		PrizeRibbonBoardBlock board = (PrizeRibbonBoardBlock) JugcraftAgriculture.block("prize_ribbon_board");
		BlockState placed = level.getBlockState(helper.absolutePos(lower));
		helper.assertTrue(placed.is(board) && placed.getValue(PrizeRibbonBoardBlock.FACING) == Direction.NORTH
				&& level.getBlockState(helper.absolutePos(upper)).is(board), "It stands two blocks tall, facing the player");
		Item first = JugcraftAgriculture.item("first_prize_ribbon");
		Item third = JugcraftAgriculture.item("third_prize_ribbon");
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(first, 2));
		use(player, level, aim(helper, upper, 2));
		for (BlockPos half : new BlockPos[] {lower, upper}) {
			BlockState state = level.getBlockState(helper.absolutePos(half));
			helper.assertTrue(state.getValue(PrizeRibbonBoardBlock.FIRST) && !state.getValue(PrizeRibbonBoardBlock.SECOND)
					&& !state.getValue(PrizeRibbonBoardBlock.THIRD), "The First hangs on its own hook, on both halves: " + state);
		}
		helper.assertTrue(player.getMainHandItem().getCount() == 1, "Hanging it uses it up");
		use(player, level, aim(helper, upper, 0));
		helper.assertTrue(player.getMainHandItem().getCount() == 1, "A second First is refused and kept");
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.APPLE));
		BlockState before = level.getBlockState(helper.absolutePos(lower));
		use(player, level, aim(helper, upper, 1));
		helper.assertTrue(player.getMainHandItem().getCount() == 1 && level.getBlockState(helper.absolutePos(lower)) == before,
				"An apple hangs on nothing");
		player.setGameMode(GameType.ADVENTURE);
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(third));
		use(player, level, aim(helper, upper, 2));
		helper.assertTrue(!level.getBlockState(helper.absolutePos(upper)).getValue(PrizeRibbonBoardBlock.THIRD)
				&& player.getMainHandItem().getCount() == 1, "A player who may not build hangs nothing");
		player.setGameMode(GameType.SURVIVAL);
		use(player, level, aim(helper, upper, 2));
		helper.assertTrue(level.getBlockState(helper.absolutePos(lower)).getValue(PrizeRibbonBoardBlock.THIRD) && player.getMainHandItem().isEmpty(),
				"The Third hangs on the third hook");
		use(player, level, aim(helper, upper, 2));
		helper.assertTrue(!level.getBlockState(helper.absolutePos(upper)).getValue(PrizeRibbonBoardBlock.THIRD)
				&& level.getBlockState(helper.absolutePos(upper)).getValue(PrizeRibbonBoardBlock.FIRST) && player.getInventory().countItem(third) == 1,
				"An empty hand aimed at the third hook takes back the Third, and only it");
		player.getInventory().clearContent();
		use(player, level, aim(helper, upper, 2));
		helper.assertTrue(!level.getBlockState(helper.absolutePos(upper)).getValue(PrizeRibbonBoardBlock.FIRST) && player.getInventory().countItem(first) == 1,
				"Aimed at the empty third hook, an empty hand takes back the nearest ribbon, the First");
		player.getInventory().clearContent();
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(first));
		use(player, level, aim(helper, upper, 0));
		helper.assertTrue(level.getBlockState(helper.absolutePos(lower)).getValue(PrizeRibbonBoardBlock.FIRST), "The First hangs again");
		player.getInventory().clearContent();
		level.destroyBlock(helper.absolutePos(upper), true);
		helper.assertTrue(level.getBlockState(helper.absolutePos(lower)).isAir() && level.getBlockState(helper.absolutePos(upper)).isAir(),
				"It breaks whole");
		helper.runAfterDelay(3, () -> {
			helper.assertTrue(dropped(helper, JugcraftAgriculture.item("prize_ribbon_board")) == 1, "It drops itself once");
			helper.assertTrue(dropped(helper, first) == 1, "It drops the First that hung on it");
			helper.assertTrue(dropped(helper, third) == 0, "It drops no ribbon that wasn't hanging on it");
			helper.succeed();
		});
	}

	@GameTest
	public void harvestFestivalDataLoads(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		for (String id : new String[] {"giant_cornucopia", "giant_cabbage", "giant_marrow", "giant_turnip", "giant_carrot", "prize_ribbon_board"}) {
			helper.assertTrue(level.recipeAccess().byKey(ResourceKey.create(Registries.RECIPE, Jugcraft.id(id))).isPresent(), id + " has a recipe");
			helper.assertTrue(level.getServer().reloadableRegistries().getLootTable(ResourceKey.create(Registries.LOOT_TABLE,
					Jugcraft.id("blocks/" + id))) != LootTable.EMPTY, id + "'s loot loads");
		}
		helper.succeed();
	}
}

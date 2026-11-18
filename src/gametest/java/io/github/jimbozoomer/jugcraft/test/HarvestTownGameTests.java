package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.agriculture.CandleCenterpieceBlock;
import io.github.jimbozoomer.jugcraft.agriculture.FacingDecorationBlock;
import io.github.jimbozoomer.jugcraft.agriculture.HarvestTownArchBlock;
import io.github.jimbozoomer.jugcraft.agriculture.HarvestTownDecor;
import io.github.jimbozoomer.jugcraft.agriculture.HarvestTownHangingBlock;
import io.github.jimbozoomer.jugcraft.agriculture.JugcraftAgriculture;
import io.github.jimbozoomer.jugcraft.agriculture.MultiDecorationBlock;
import io.github.jimbozoomer.jugcraft.agriculture.PlanterBlock;
import io.github.jimbozoomer.jugcraft.agriculture.PlanterBlockEntity;
import io.github.jimbozoomer.jugcraft.finance.FinanceBook;
import io.github.jimbozoomer.jugcraft.finance.LandRegistry;
import io.github.jimbozoomer.jugcraft.finance.Ledger;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Thanksgiving harvest town details (tools/thanksgiving_town.py; docs/features/thanksgiving-town-details.md): every floor
 * piece faces whoever placed it and, broken, drops itself once; the chafing dish's burner lights with flint and steel,
 * gives its light, and an empty hand snuffs it; the crock rack hangs only under a block, its outline turns with it, and it
 * falls when its block goes; the Timber Harvest Arch goes up whole with nothing in its opening, refuses rather than
 * overwrite a block, a fluid or its unsupported foot, comes down whole from any of its blocks dropping once, and a claim
 * holding any of its blocks keeps others from placing or breaking it; and the planters take a Warty Gourd
 * (#jugcraft:planter_harvest) as they take small flowers, but not wheat.
 */
public class HarvestTownGameTests {
	/** The floor pieces (tools/thanksgiving_town.py PROPS, kinds "floor" and "candles"). */
	private static final List<String> FLOOR = List.of("folded_linen_stack", "pumpkin_carving_bench", "copper_chafing_dish");
	private static final String ARCH = "timber_harvest_arch";

	@GameTest(maxTicks = 40)
	public void everyFloorPieceFacesItsPlacerAndDropsItself(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		floor(helper);
		BlockPos at = helper.absolutePos(new BlockPos(3, 2, 3));
		ServerPlayer player = standing(helper, new BlockPos(3, 2, 1));
		for (String id : FLOOR) {
			place(player, helper, new BlockPos(3, 2, 3), new ItemStack(item(id)));
			BlockState placed = level.getBlockState(at);
			helper.assertTrue(placed.is(block(id)) && placed.getValue(FacingDecorationBlock.FACING) == Direction.NORTH,
					id + " stands where it was placed, facing the player: " + placed);
			helper.assertTrue(player.getMainHandItem().isEmpty(), id + " is used up placing it");
			helper.assertTrue(placed.getShape(level, at).bounds().getYsize() < 1.0, id + " has its own outline, not a full block");
			level.destroyBlock(at, true);
			helper.assertTrue(level.getBlockState(at).isAir() && dropped(helper, item(id)) == 1, id + ", broken, drops itself once");
			clearDrops(helper);
		}
		helper.succeed();
	}

	@GameTest(maxTicks = 40)
	public void theChafingDishBurnerLightsAndSnuffs(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		floor(helper);
		BlockPos at = helper.absolutePos(new BlockPos(3, 2, 3));
		ServerPlayer player = standing(helper, new BlockPos(3, 2, 1));
		place(player, helper, new BlockPos(3, 2, 3), new ItemStack(item("copper_chafing_dish")));
		helper.assertTrue(!level.getBlockState(at).getValue(CandleCenterpieceBlock.LIT) && level.getBlockState(at).getLightEmission() == 0,
				"It is placed with its burner out, giving no light");
		use(player, level, at, new ItemStack(Items.FLINT_AND_STEEL));
		helper.assertTrue(level.getBlockState(at).getValue(CandleCenterpieceBlock.LIT)
				&& level.getBlockState(at).getLightEmission() == HarvestTownDecor.CHAFING_DISH_LIGHT, "Flint and steel lights its burner");
		use(player, level, at, ItemStack.EMPTY);
		helper.assertTrue(!level.getBlockState(at).getValue(CandleCenterpieceBlock.LIT), "An empty hand snuffs it");
		helper.succeed();
	}

	@GameTest(maxTicks = 40)
	public void theCrockRackHangsUnderABlockAndFallsWithIt(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		floor(helper);
		Block rack = block("hanging_crock_rack");
		helper.assertTrue(rack instanceof HarvestTownHangingBlock, "It is a hanging decoration");
		ServerPlayer player = standing(helper, new BlockPos(3, 2, 1));
		// Nothing to hang from: used on the floor, it is refused and kept.
		place(player, helper, new BlockPos(5, 2, 3), new ItemStack(item("hanging_crock_rack")));
		helper.assertBlockNotPresent(rack, new BlockPos(5, 2, 3));
		helper.assertTrue(player.getMainHandItem().getCount() == 1, "Refused, it is kept");
		// Under a block it hangs, facing the player.
		helper.setBlock(new BlockPos(3, 5, 3), Blocks.STONE);
		BlockPos below = helper.absolutePos(new BlockPos(3, 5, 3));
		player.getMainHandItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
				new BlockHitResult(Vec3.atCenterOf(below).relative(Direction.DOWN, 0.5), Direction.DOWN, below, false)));
		BlockState hung = helper.getBlockState(new BlockPos(3, 4, 3));
		helper.assertTrue(hung.is(rack) && hung.getValue(HarvestTownHangingBlock.FACING) == Direction.NORTH, "It hangs under the block: " + hung);
		// Its outline is longer across than deep, and turns with it.
		VoxelShape north = hung.getShape(level, below.below());
		VoxelShape east = hung.setValue(HarvestTownHangingBlock.FACING, Direction.EAST).getShape(level, below.below());
		helper.assertTrue(north.bounds().getXsize() > north.bounds().getZsize() && east.bounds().getZsize() > east.bounds().getXsize(),
				"Its outline turns with the way it faces");
		helper.assertTrue(hung.getCollisionShape(level, below.below()).isEmpty(), "Nothing bumps into it");
		// Its block goes, and it falls.
		helper.setBlock(new BlockPos(3, 5, 3), Blocks.AIR);
		helper.assertBlockNotPresent(rack, new BlockPos(3, 4, 3));
		helper.succeed();
	}

	@GameTest(maxTicks = 60)
	public void theArchGoesUpWholeWithAClearOpening(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		floor(helper);
		ServerPlayer player = facingNorth(helper, new BlockPos(3, 2, 7), new ItemStack(item(ARCH), 2));
		place(player, helper, new BlockPos(1, 2, 4), player.getMainHandItem());
		HarvestTownArchBlock arch = (HarvestTownArchBlock) block(ARCH);
		int[][] cells = arch.cells();
		helper.assertTrue(cells.length == 11, "It is eleven blocks: two posts of three and a beam of five");
		for (int part = 0; part < cells.length; part++) {
			// Facing south (the player looks north): their right is east.
			BlockState state = helper.getBlockState(new BlockPos(1 + cells[part][0], 2 + cells[part][1], 4));
			helper.assertTrue(state.is(arch) && state.getValue(HarvestTownArchBlock.PART) == part
					&& state.getValue(MultiDecorationBlock.FACING) == Direction.SOUTH, "Part " + part + " is where it should be: " + state);
		}
		helper.assertTrue(player.getMainHandItem().getCount() == 1, "Placing it uses one");
		for (int x = 2; x <= 4; x++) {
			for (int y = 2; y <= 4; y++) {
				helper.assertTrue(helper.getBlockState(new BlockPos(x, y, 4)).isAir(), "The opening is empty at " + x + ", " + y);
			}
		}
		for (int x = 2; x <= 4; x++) {
			BlockPos beam = new BlockPos(x, 5, 4);
			VoxelShape over = helper.getBlockState(beam).getCollisionShape(level, helper.absolutePos(beam));
			helper.assertTrue(!over.isEmpty() && over.min(Direction.Axis.Y) >= 2.0 / 16.0 - 1e-6, "The beam over the opening starts above it");
		}
		helper.succeed();
	}

	@GameTest(maxTicks = 80)
	public void theArchGoesUpInEveryFacingWithAWalkableOpening(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		floor(helper);
		HarvestTownArchBlock arch = (HarvestTownArchBlock) block(ARCH);
		// The player's turn, the facing it gives the arch (towards the player), and a first block that keeps it in the area.
		float[] turns = {0.0F, 90.0F, 180.0F, -90.0F};
		Direction[] facings = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
		BlockPos[] firsts = {new BlockPos(6, 2, 4), new BlockPos(4, 2, 6), new BlockPos(1, 2, 4), new BlockPos(4, 2, 1)};
		for (int i = 0; i < 4; i++) {
			ServerPlayer player = standing(helper, new BlockPos(0, 2, 7));
			player.setYRot(turns[i]);
			place(player, helper, firsts[i], new ItemStack(item(ARCH)));
			BlockPos master = helper.absolutePos(firsts[i]);
			for (int part = 0; part < arch.cells().length; part++) {
				BlockState state = level.getBlockState(arch.partPos(master, facings[i], part));
				helper.assertTrue(state.is(arch) && state.getValue(HarvestTownArchBlock.PART) == part
						&& state.getValue(MultiDecorationBlock.FACING) == facings[i], "Facing " + facings[i] + ", part " + part + ": " + state);
			}
			Direction across = facings[i].getCounterClockWise();
			for (int right = 1; right <= HarvestTownArchBlock.OPENING; right++) {
				for (int up = 0; up < HarvestTownArchBlock.CLEAR; up++) {
					helper.assertTrue(level.getBlockState(master.relative(across, right).above(up)).isAir(),
							"Facing " + facings[i] + ", the opening is empty " + right + " across and " + up + " up");
				}
				// A player-sized box (0.6 wide, 1.8 high) stands anywhere in the opening without touching the arch.
				BlockPos foot = master.relative(across, right);
				helper.assertTrue(level.noCollision(new AABB(foot.getX() + 0.2, foot.getY(), foot.getZ() + 0.2, foot.getX() + 0.8,
						foot.getY() + 1.8, foot.getZ() + 0.8)), "Facing " + facings[i] + ", a player fits through the opening");
			}
			level.removeBlock(master, false);
			for (int part = 0; part < arch.cells().length; part++) {
				helper.assertTrue(!level.getBlockState(arch.partPos(master, facings[i], part)).is(arch), "Taken down whole, facing " + facings[i]);
			}
		}
		helper.succeed();
	}

	@GameTest(maxTicks = 60)
	public void theArchRefusesRatherThanOverwrite(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		floor(helper);
		ServerPlayer player = facingNorth(helper, new BlockPos(3, 2, 7), new ItemStack(item(ARCH)));
		// A block in the far post's place.
		helper.setBlock(new BlockPos(5, 3, 4), Blocks.OAK_PLANKS);
		place(player, helper, new BlockPos(1, 2, 4), player.getMainHandItem());
		assertNothingPlaced(helper, player, "a block in its way");
		helper.assertTrue(helper.getBlockState(new BlockPos(5, 3, 4)).is(Blocks.OAK_PLANKS), "The block in its way is untouched");
		helper.setBlock(new BlockPos(5, 3, 4), Blocks.AIR);
		// Water where a post would stand.
		helper.setBlock(new BlockPos(5, 2, 4), Blocks.WATER.defaultBlockState());
		place(player, helper, new BlockPos(1, 2, 4), player.getMainHandItem());
		assertNothingPlaced(helper, player, "water in its way");
		helper.assertTrue(!level.getFluidState(helper.absolutePos(new BlockPos(5, 2, 4))).isEmpty(), "The water is untouched");
		helper.setBlock(new BlockPos(5, 2, 4), Blocks.AIR);
		// No ground under the far post.
		helper.setBlock(new BlockPos(5, 1, 4), Blocks.AIR);
		place(player, helper, new BlockPos(1, 2, 4), player.getMainHandItem());
		assertNothingPlaced(helper, player, "no ground under a post");
		helper.succeed();
	}

	@GameTest(maxTicks = 80)
	public void theArchComesDownWholeFromAnyBlock(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		floor(helper);
		HarvestTownArchBlock arch = (HarvestTownArchBlock) block(ARCH);
		int[][] cells = arch.cells();
		// Broken at its first block, the middle of its beam, the far post's foot; and by a player at the near post's top.
		for (int part : new int[] {0, 5, 10, 2}) {
			ServerPlayer player = facingNorth(helper, new BlockPos(3, 2, 7), new ItemStack(item(ARCH)));
			place(player, helper, new BlockPos(1, 2, 4), player.getMainHandItem());
			BlockPos broken = helper.absolutePos(new BlockPos(1 + cells[part][0], 2 + cells[part][1], 4));
			helper.assertTrue(level.getBlockState(broken).is(arch), "It stood before part " + part + " was broken");
			if (part == 2) {
				player.gameMode.destroyBlock(broken);
			} else {
				level.destroyBlock(broken, true);
			}
			for (int[] cell : cells) {
				helper.assertTrue(!helper.getBlockState(new BlockPos(1 + cell[0], 2 + cell[1], 4)).is(arch),
						"Breaking part " + part + " takes every block of it down, no orphan left");
			}
			helper.assertTrue(dropped(helper, item(ARCH)) == 1, "Breaking part " + part + " drops one arch");
			clearDrops(helper);
		}
		helper.succeed();
	}

	@GameTest(maxTicks = 60)
	public void aClaimProtectsEveryBlockOfTheArch(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		floor(helper);
		ServerPlayer builder = facingNorth(helper, new BlockPos(3, 2, 7), new ItemStack(item(ARCH)));
		ServerPlayer neighbour = helper.makeMockServerPlayerInLevel();
		LandRegistry land = LandRegistry.get(level.getServer());
		FinanceBook book = Ledger.get(level.getServer()).book();
		long key = LandRegistry.keyOf(helper.absolutePos(new BlockPos(5, 2, 4)));
		try {
			helper.assertTrue(land.assign(book, key, book.account(neighbour.getUUID(), "ArchNeighbour"), 0).ok(), "The far post's ground is claimed");
			place(builder, helper, new BlockPos(1, 2, 4), builder.getMainHandItem());
			assertNothingPlaced(helper, builder, "a block of it in someone else's claim");
			land.abandon(neighbour, book, key, true, 0);
			place(builder, helper, new BlockPos(1, 2, 4), builder.getMainHandItem());
			BlockPos first = helper.absolutePos(new BlockPos(1, 2, 4));
			BlockState state = level.getBlockState(first);
			helper.assertTrue(state.is(block(ARCH)), "Unclaimed, it goes up");
			helper.assertTrue(state.getDestroyProgress(builder, level, first) > 0.0F, "Its builder can break it");
			helper.assertTrue(land.assign(book, key, book.account(neighbour.getUUID(), "ArchNeighbour"), 0).ok(), "The far post's ground is claimed again");
			helper.assertTrue(state.getDestroyProgress(builder, level, first) == 0.0F,
					"Once any block of it is in someone else's claim, a non-member makes no progress breaking even its unclaimed blocks");
		} finally {
			land.abandon(neighbour, book, key, true, 0);
		}
		helper.succeed();
	}

	@GameTest(maxTicks = 40)
	public void thePlantersTakeAWartyGourdButNotWheat(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		floor(helper);
		helper.assertTrue(PlanterBlock.plantable(new ItemStack(item("warty_gourd"))), "#jugcraft:planter_harvest is loaded and holds the Warty Gourd");
		helper.assertTrue(PlanterBlock.plantable(new ItemStack(Items.POPPY)) && PlanterBlock.plantable(new ItemStack(item("aster"))),
				"Small flowers are still plantable, as before");
		helper.assertTrue(!PlanterBlock.plantable(new ItemStack(Items.WHEAT)), "Wheat is no planter's");
		helper.setBlock(new BlockPos(3, 2, 4), Blocks.STONE);
		helper.setBlock(new BlockPos(3, 2, 3), block("window_box").defaultBlockState().setValue(PlanterBlock.FACING, Direction.NORTH));
		BlockPos box = helper.absolutePos(new BlockPos(3, 2, 3));
		ServerPlayer player = standing(helper, new BlockPos(3, 2, 1));
		PlanterBlockEntity planted = helper.getBlockEntity(new BlockPos(3, 2, 3), PlanterBlockEntity.class);
		use(player, level, box, new ItemStack(item("aster"), 2));
		use(player, level, box, new ItemStack(item("warty_gourd"), 2));
		helper.assertTrue(planted.plants().size() == 2 && planted.plants().get(1).is(item("warty_gourd"))
				&& player.getMainHandItem().getCount() == 1, "A Warty Gourd goes into the Window Box beside an aster, one used");
		use(player, level, box, new ItemStack(Items.WHEAT));
		helper.assertTrue(planted.plants().size() == 2 && player.getMainHandItem().getCount() == 1, "Wheat does not");
		// Saved and loaded, the box keeps the aster and the gourd in their spots.
		var saved = planted.saveWithoutMetadata(level.registryAccess());
		PlanterBlockEntity copy = new PlanterBlockEntity(box, helper.getBlockState(new BlockPos(3, 2, 3)));
		copy.setLevel(level);
		copy.loadWithComponents(net.minecraft.world.level.storage.TagValueInput.create(net.minecraft.util.ProblemReporter.DISCARDING,
				level.registryAccess(), saved));
		helper.assertTrue(copy.plants().size() == 2 && copy.plants().get(0).is(item("aster")) && copy.plants().get(1).is(item("warty_gourd")),
				"Saved and loaded, what grows in it is kept: " + copy.plants());
		use(player, level, box, new ItemStack(Items.POPPY));
		helper.assertTrue(planted.plants().size() == 3, "Small flowers still go in beside it");
		player.getInventory().clearContent();
		use(player, level, box, ItemStack.EMPTY);
		// The returned poppy occupies the selected hotbar slot. Select an
		// empty hand instead of deleting the poppy in the use helper.
		player.getInventory().setSelectedSlot(1);
		use(player, level, box, ItemStack.EMPTY);
		helper.assertTrue(planted.plants().size() == 1 && carried(player, item("warty_gourd")) == 1 && carried(player, Items.POPPY) == 1,
				"An empty hand takes them back, the last planted first, the gourd too");
		helper.succeed();
	}

	private static void assertNothingPlaced(GameTestHelper helper, ServerPlayer player, String why) {
		for (int x = 1; x <= 5; x++) {
			for (int y = 2; y <= 5; y++) {
				helper.assertTrue(!helper.getBlockState(new BlockPos(x, y, 4)).is(block(ARCH)), "With " + why + ", no block of it is placed");
			}
		}
		helper.assertTrue(player.getMainHandItem().is(item(ARCH)), "With " + why + ", the arch is kept");
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

	/** A player at {@code at} looking north (their right is east), holding {@code held}. */
	private static ServerPlayer facingNorth(GameTestHelper helper, BlockPos at, ItemStack held) {
		ServerPlayer player = standing(helper, at);
		player.setYRot(180.0F);
		player.setItemInHand(InteractionHand.MAIN_HAND, held);
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
		return helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(helper.absolutePos(new BlockPos(3, 3, 4))).inflate(6.0),
				entity -> entity.getItem().is(item)).stream().mapToInt(entity -> entity.getItem().getCount()).sum();
	}

	private static void clearDrops(GameTestHelper helper) {
		helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(helper.absolutePos(new BlockPos(3, 3, 4))).inflate(6.0))
				.forEach(ItemEntity::discard);
	}

	private static int carried(ServerPlayer player, Item item) {
		int count = 0;
		for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
			if (player.getInventory().getItem(slot).is(item)) {
				count += player.getInventory().getItem(slot).getCount();
			}
		}
		return count;
	}
}

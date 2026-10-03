package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.agriculture.JugcraftAgriculture;
import io.github.jimbozoomer.jugcraft.diagonal.DiagonalConnections;
import io.github.jimbozoomer.jugcraft.diagonal.DiagonalConnections.Diagonal;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CrossCollisionBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Diagonal connections (docs/features/diagonal-connections.md): fences, panes and bars a diagonal step apart join across
 * an open corner, from either end; a straight connection or a block in the corner keeps them apart; breaking one lets
 * the other go; only kinds that join straight join diagonally; rotation and mirroring turn the diagonals; the arms
 * show in the outline and the collision; and every fence and bars block has the properties, starting false.
 */
public class DiagonalConnectionsGameTests {
	private static final Logger LOGGER = LoggerFactory.getLogger("jugcraft-diagonal-tests");
	/** A block and the one a north-east step from it (x + 1, z - 1). */
	private static final BlockPos FIRST = new BlockPos(2, 1, 3);
	private static final BlockPos NORTH_EAST = new BlockPos(3, 1, 2);
	/** The two blocks between them: north of the first (west of the other) and east of it (south of the other). */
	private static final BlockPos NORTH_CORNER = new BlockPos(2, 1, 2);
	private static final BlockPos EAST_CORNER = new BlockPos(3, 1, 3);

	private static void floor(GameTestHelper helper) {
		for (int x = 0; x <= 6; x++) {
			for (int z = 0; z <= 6; z++) {
				helper.setBlock(new BlockPos(x, 0, z), Blocks.STONE);
			}
		}
	}

	private static ServerPlayer player(GameTestHelper helper) {
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		player.setGameMode(GameType.SURVIVAL);
		BlockPos absolute = helper.absolutePos(new BlockPos(0, 1, 6));
		player.setPos(absolute.getX() + 0.5, absolute.getY(), absolute.getZ() + 0.5);
		return player;
	}

	/** The player places `item` on the floor at `pos`, clicking the top of the block below, as a player does. */
	private static void place(GameTestHelper helper, ServerPlayer player, Item item, BlockPos pos) {
		ItemStack stack = new ItemStack(item);
		player.setItemInHand(InteractionHand.MAIN_HAND, stack);
		BlockPos below = helper.absolutePos(pos.below());
		player.gameMode.useItemOn(player, helper.getLevel(), stack, InteractionHand.MAIN_HAND,
				new BlockHitResult(Vec3.atCenterOf(below).relative(Direction.UP, 0.5), Direction.UP, below, false));
	}

	private static void set(GameTestHelper helper, BlockPos pos, Block block) {
		helper.setBlock(pos, block.defaultBlockState());
	}

	private static String describe(BlockState state) {
		List<String> joined = new ArrayList<>();
		for (Direction direction : CrossCollisionBlock.PROPERTY_BY_DIRECTION.keySet()) {
			if (state.getValue(CrossCollisionBlock.PROPERTY_BY_DIRECTION.get(direction))) {
				joined.add(direction.getName());
			}
		}
		for (Diagonal diagonal : Diagonal.ALL) {
			if (state.getValue(diagonal.property)) {
				joined.add(diagonal.property.getName());
			}
		}
		return BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString() + joined;
	}

	/** Whether the first block and its north-east neighbour are joined to each other and to nothing else diagonally. */
	private static boolean joinedPair(GameTestHelper helper) {
		BlockState first = helper.getBlockState(FIRST);
		BlockState other = helper.getBlockState(NORTH_EAST);
		return DiagonalConnections.mask(first) == 1 << Diagonal.NORTH_EAST.ordinal()
				&& DiagonalConnections.mask(other) == 1 << Diagonal.SOUTH_WEST.ordinal();
	}

	private static boolean contains(VoxelShape shape, double x, double y, double z) {
		return shape.toAabbs().stream().anyMatch(box -> box.contains(x, y, z));
	}

	/**
	 * A player places two oak fences a diagonal step apart. The second one's placement joins it to the first, and the
	 * first is told and joins back. Its outline and collision reach its north-east corner and not the others.
	 */
	@GameTest
	public void placedFencesJoinDiagonally(GameTestHelper helper) {
		floor(helper);
		ServerPlayer player = player(helper);
		place(helper, player, Items.OAK_FENCE, FIRST);
		place(helper, player, Items.OAK_FENCE, NORTH_EAST);
		BlockState first = helper.getBlockState(FIRST);
		BlockState other = helper.getBlockState(NORTH_EAST);
		LOGGER.info("Placed diagonally: {} and {}", describe(first), describe(other));
		helper.assertTrue(first.is(Blocks.OAK_FENCE) && other.is(Blocks.OAK_FENCE), "Both fences are placed");
		helper.assertTrue(joinedPair(helper), "The fences join diagonally: " + describe(first) + ", " + describe(other));

		ServerLevel level = helper.getLevel();
		BlockPos absolute = helper.absolutePos(FIRST);
		VoxelShape outline = first.getShape(level, absolute);
		VoxelShape collision = first.getCollisionShape(level, absolute);
		helper.assertTrue(contains(outline, 0.9, 0.6, 0.1) && contains(collision, 0.9, 1.2, 0.1),
				"The outline and collision reach the north-east corner: " + outline.bounds() + ", " + collision.bounds());
		helper.assertTrue(!contains(outline, 0.1, 0.6, 0.9) && !contains(collision, 0.1, 0.6, 0.9) && !contains(collision, 0.1, 0.6, 0.1),
				"No arm reaches the other corners");
		helper.succeed();
	}

	/** A block in the corner joins both fences straight and keeps them apart; taken away, they join diagonally again. */
	@GameTest
	public void aBlockInTheCornerKeepsThemApart(GameTestHelper helper) {
		floor(helper);
		set(helper, FIRST, Blocks.OAK_FENCE);
		set(helper, NORTH_EAST, Blocks.OAK_FENCE);
		helper.assertTrue(joinedPair(helper), "Set side by side diagonally, the fences join: " + describe(helper.getBlockState(FIRST)));
		set(helper, NORTH_CORNER, Blocks.STONE);
		BlockState first = helper.getBlockState(FIRST);
		BlockState other = helper.getBlockState(NORTH_EAST);
		helper.assertTrue(first.getValue(CrossCollisionBlock.NORTH) && other.getValue(CrossCollisionBlock.WEST)
				&& DiagonalConnections.mask(first) == 0 && DiagonalConnections.mask(other) == 0,
				"With stone in the corner both join it and not each other: " + describe(first) + ", " + describe(other));
		set(helper, NORTH_CORNER, Blocks.AIR);
		helper.assertTrue(joinedPair(helper), "With the corner clear again they rejoin: " + describe(helper.getBlockState(FIRST)) + ", "
				+ describe(helper.getBlockState(NORTH_EAST)));
		helper.succeed();
	}

	/** A fence joined straight around the corner does not also join diagonally, so arms never cross a corner twice. */
	@GameTest
	public void aStraightCornerComesFirst(GameTestHelper helper) {
		floor(helper);
		ServerPlayer player = player(helper);
		place(helper, player, Items.SPRUCE_FENCE, FIRST);
		place(helper, player, Items.SPRUCE_FENCE, EAST_CORNER);
		place(helper, player, Items.SPRUCE_FENCE, NORTH_EAST);
		BlockState first = helper.getBlockState(FIRST);
		BlockState corner = helper.getBlockState(EAST_CORNER);
		BlockState other = helper.getBlockState(NORTH_EAST);
		helper.assertTrue(first.getValue(CrossCollisionBlock.EAST) && corner.getValue(CrossCollisionBlock.WEST)
				&& corner.getValue(CrossCollisionBlock.NORTH) && other.getValue(CrossCollisionBlock.SOUTH),
				"The fences join straight around the corner: " + describe(first) + ", " + describe(corner) + ", " + describe(other));
		helper.assertTrue(DiagonalConnections.mask(first) == 0 && DiagonalConnections.mask(other) == 0 && DiagonalConnections.mask(corner) == 0,
				"and not diagonally");
		helper.succeed();
	}

	/** Breaking one of a joined pair lets the other go. */
	@GameTest
	public void breakingOneLetsTheOtherGo(GameTestHelper helper) {
		floor(helper);
		set(helper, FIRST, Blocks.IRON_BARS);
		set(helper, NORTH_EAST, Blocks.IRON_BARS);
		helper.assertTrue(joinedPair(helper), "Iron bars join diagonally: " + describe(helper.getBlockState(FIRST)));
		helper.destroyBlock(NORTH_EAST);
		helper.assertTrue(DiagonalConnections.mask(helper.getBlockState(FIRST)) == 0,
				"Broken, its partner lets go: " + describe(helper.getBlockState(FIRST)));
		helper.succeed();
	}

	/** Kinds that join straight join diagonally, and no others. */
	@GameTest
	public void onlyKindsThatJoinStraightJoinDiagonally(GameTestHelper helper) {
		floor(helper);
		Block cemetery = JugcraftAgriculture.block("cemetery_fence");
		Block aspen = BuiltInRegistries.BLOCK.getValue(Jugcraft.id("aspen_fence"));
		Block copperBars = BuiltInRegistries.BLOCK.getValue(Identifier.withDefaultNamespace("copper_bars"));
		Block redPane = BuiltInRegistries.BLOCK.getValue(Identifier.withDefaultNamespace("red_stained_glass_pane"));
		Object[][] pairs = {
				{Blocks.OAK_FENCE, Blocks.SPRUCE_FENCE, true}, {Blocks.OAK_FENCE, aspen, true}, {Blocks.OAK_FENCE, Blocks.NETHER_BRICK_FENCE, false},
				{cemetery, cemetery, true}, {cemetery, Blocks.OAK_FENCE, false}, {Blocks.GLASS_PANE, Blocks.IRON_BARS, true},
				{redPane, copperBars, true}, {Blocks.IRON_BARS, Blocks.OAK_FENCE, false},
				{Blocks.BAMBOO_FENCE, Blocks.BAMBOO_FENCE, true}};
		List<String> wrong = new ArrayList<>();
		for (Object[] pair : pairs) {
			set(helper, FIRST, Blocks.AIR);
			set(helper, NORTH_EAST, Blocks.AIR);
			set(helper, FIRST, (Block) pair[0]);
			set(helper, NORTH_EAST, (Block) pair[1]);
			boolean joined = joinedPair(helper);
			LOGGER.info("{} and {}: {}", describe(helper.getBlockState(FIRST)), describe(helper.getBlockState(NORTH_EAST)), joined);
			if (joined != (boolean) pair[2]) {
				wrong.add(describe(helper.getBlockState(FIRST)) + " / " + describe(helper.getBlockState(NORTH_EAST)) + " should join: " + pair[2]);
			}
		}
		helper.assertTrue(wrong.isEmpty(), "Pairs joined wrongly: " + wrong);
		helper.succeed();
	}

	/** Rotating and mirroring a block (structures, the structure block) turn its diagonals with it. */
	@GameTest
	public void rotationAndMirroringTurnTheDiagonals(GameTestHelper helper) {
		BlockState northEast = Blocks.OAK_FENCE.defaultBlockState().setValue(DiagonalConnections.NORTH_EAST, true);
		Object[][] cases = {
				{northEast.rotate(Rotation.CLOCKWISE_90), Diagonal.SOUTH_EAST}, {northEast.rotate(Rotation.CLOCKWISE_180), Diagonal.SOUTH_WEST},
				{northEast.rotate(Rotation.COUNTERCLOCKWISE_90), Diagonal.NORTH_WEST}, {northEast.mirror(Mirror.FRONT_BACK), Diagonal.NORTH_WEST},
				{northEast.mirror(Mirror.LEFT_RIGHT), Diagonal.SOUTH_EAST}};
		for (Object[] check : cases) {
			BlockState state = (BlockState) check[0];
			Diagonal wanted = (Diagonal) check[1];
			helper.assertTrue(DiagonalConnections.mask(state) == 1 << wanted.ordinal(), describe(state) + " should have only " + wanted);
		}
		helper.succeed();
	}

	/**
	 * Every fence and bars block has the four properties and starts with them false; every block in the tag is one
	 * (vanilla's 14 fences, 17 panes and 9 bars, Jugcraft's 14 fences: 54).
	 */
	@GameTest
	public void everyFenceAndBarsBlockHasDiagonals(GameTestHelper helper) {
		List<String> problems = new ArrayList<>();
		int tagged = 0;
		for (Block block : BuiltInRegistries.BLOCK) {
			BlockState state = block.defaultBlockState();
			boolean crossing = block instanceof FenceBlock || block instanceof IronBarsBlock;
			if (crossing && (!DiagonalConnections.hasDiagonals(state) || DiagonalConnections.mask(state) != 0)) {
				problems.add(BuiltInRegistries.BLOCK.getKey(block) + " lacks diagonals or starts joined");
			}
			if (state.is(DiagonalConnections.TAG)) {
				tagged++;
				if (!crossing) {
					problems.add(BuiltInRegistries.BLOCK.getKey(block) + " is tagged but is no fence or bars");
				}
			}
		}
		LOGGER.info("{} blocks join diagonally; problems: {}", tagged, problems);
		helper.assertTrue(problems.isEmpty(), "Problems: " + problems);
		helper.assertTrue(tagged == 54, "54 blocks join diagonally, not " + tagged);
		helper.succeed();
	}
}

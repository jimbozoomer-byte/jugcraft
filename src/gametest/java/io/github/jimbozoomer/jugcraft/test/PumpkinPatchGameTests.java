package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.agriculture.AttachedGourdStemBlock;
import io.github.jimbozoomer.jugcraft.agriculture.GourdStemBlock;
import io.github.jimbozoomer.jugcraft.agriculture.JugcraftAgriculture;
import io.github.jimbozoomer.jugcraft.agriculture.ProduceDisplayBlock;
import io.github.jimbozoomer.jugcraft.agriculture.ProduceDisplayBlockEntity;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;

/**
 * The Pumpkin Patch Market's squashes and gourds (decor set 1, part 1; docs/features/pumpkin-patch-market.md): each seed
 * plants its stem on farmland, each grown stem puts its own gourd beside it and bends towards it, each gourd drops itself,
 * is smaller than a block (light and faces pass it) and counts in the Crowfield's harvest, and each has its wild patch.
 * Part 2's displays (the Pumpkin Wheelbarrow, Wagon and Pyramid) stand whole where they are placed, take produce on any of
 * their blocks one piece a spot up to their spots, refuse what is not produce, give the last piece back to an empty hand,
 * remember what they hold, and break whole, spilling it.
 */
public class PumpkinPatchGameTests {
	/** The six, as tools/pumpkin_patch.py VARIETIES lists them. */
	private static final String[] VARIETIES = {"turban_squash", "blue_hubbard_squash", "sweet_dumpling_squash", "long_island_cheese_pumpkin",
			"spoon_gourd", "swan_gourd"};
	private static final BlockPos SOIL = new BlockPos(2, 1, 2);
	private static final BlockPos CROP = SOIL.above();
	private static final TagKey<Block> CROWFIELD_HARVEST = TagKey.create(Registries.BLOCK, Jugcraft.id("crowfield_harvest"));

	private static Item item(String id) {
		return JugcraftAgriculture.item(id);
	}

	private static Block block(String id) {
		return JugcraftAgriculture.block(id);
	}

	private static void farmland(GameTestHelper helper, BlockPos pos) {
		helper.setBlock(pos, Blocks.FARMLAND.defaultBlockState().setValue(BlockStateProperties.MOISTURE, 7));
	}

	/** Each variety's seeds plant its stem on farmland, using one seed. */
	@GameTest
	public void eachSeedPlantsItsStem(GameTestHelper helper) {
		BlockPos absolute = helper.absolutePos(SOIL);
		BlockHitResult top = new BlockHitResult(Vec3.atCenterOf(absolute).add(0.0, 0.5, 0.0), Direction.UP, absolute, false);
		for (String variety : VARIETIES) {
			farmland(helper, SOIL);
			helper.setBlock(CROP, Blocks.AIR);
			Player player = helper.makeMockPlayer(GameType.SURVIVAL);
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(item(variety + "_seeds"), 2));
			helper.useBlock(SOIL, player, top);
			helper.assertBlockPresent(block(variety + "_stem"), CROP);
			helper.assertTrue(player.getMainHandItem().getCount() == 1, variety + "_seeds should use one seed to plant");
		}
		helper.succeed();
	}

	/**
	 * A grown stem of each variety puts that variety on the grass beside it, facing away, and bends towards it; the gourd
	 * drops itself, is smaller than a block, and counts in the Crowfield's harvest.
	 */
	@GameTest
	public void eachStemGrowsItsOwnGourd(GameTestHelper helper) {
		for (String variety : VARIETIES) {
			farmland(helper, SOIL);
			helper.setBlock(SOIL.east(), Blocks.GRASS_BLOCK);
			helper.setBlock(CROP.east(), Blocks.AIR);
			GourdStemBlock stem = (GourdStemBlock) block(variety + "_stem");
			helper.setBlock(CROP, stem.defaultBlockState().setValue(GourdStemBlock.AGE, GourdStemBlock.MAX_AGE));
			helper.assertTrue(stem.growGourd(helper.getLevel(), helper.absolutePos(CROP), Direction.EAST), variety + "'s stem should grow a gourd");
			BlockState gourd = helper.getBlockState(CROP.east());
			helper.assertTrue(gourd.is(block(variety)) && gourd.getValue(HorizontalDirectionalBlock.FACING) == Direction.EAST,
					"Expected " + variety + " facing east, found " + gourd);
			BlockState attached = helper.getBlockState(CROP);
			helper.assertTrue(attached.is(block("attached_" + variety + "_stem")) && attached.getValue(AttachedGourdStemBlock.FACING) == Direction.EAST,
					"Expected " + variety + "'s stem bent east, found " + attached);
			boolean drops = Block.getDrops(gourd, helper.getLevel(), helper.absolutePos(CROP.east()), null).stream()
					.anyMatch(drop -> drop.is(item(variety)));
			helper.assertTrue(drops, variety + " should drop itself");
			helper.assertFalse(gourd.getShape(helper.getLevel(), helper.absolutePos(CROP.east())).equals(Shapes.block()),
					variety + " should be smaller than a block");
			helper.assertTrue(gourd.is(CROWFIELD_HARVEST), variety + " should count in the Crowfield's harvest");
		}
		helper.succeed();
	}

	/** Each variety's wild patch is placed in the world's generation. */
	@GameTest
	public void eachWildPatchLoads(GameTestHelper helper) {
		var placed = helper.getLevel().registryAccess().lookupOrThrow(Registries.PLACED_FEATURE);
		for (String variety : VARIETIES) {
			ResourceKey<PlacedFeature> key = ResourceKey.create(Registries.PLACED_FEATURE, Jugcraft.id("patch_" + variety));
			helper.assertTrue(placed.get(key).isPresent(), "Missing placed feature " + key);
		}
		helper.succeed();
	}

	// ---------------------------------------------------------------- part 2: the displays

	private static void floor(GameTestHelper helper) {
		for (int x = 0; x < 8; x++) {
			for (int z = 0; z < 8; z++) {
				helper.setBlock(new BlockPos(x, 1, z), Blocks.STONE);
			}
		}
	}

	/** Places display {@code id} as a player standing north of {@code at} and looking south would: it faces them. */
	private static ProduceDisplayBlockEntity place(GameTestHelper helper, ServerPlayer player, String id, BlockPos at) {
		player.setYRot(0.0F);
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(item(id)));
		BlockPos below = helper.absolutePos(at.below());
		BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(below).relative(Direction.UP, 0.5), Direction.UP, below, false);
		player.getMainHandItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit));
		return helper.getLevel().getBlockEntity(helper.absolutePos(at)) instanceof ProduceDisplayBlockEntity display ? display : null;
	}

	private static int dropped(GameTestHelper helper, Item item) {
		return helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(helper.absolutePos(new BlockPos(4, 2, 4))).inflate(8.0)).stream()
				.filter(entity -> entity.getItem().is(item)).mapToInt(entity -> entity.getItem().getCount()).sum();
	}

	/** The checks every display passes: see the class's description. */
	private static void checkDisplay(GameTestHelper helper, String id, BlockPos master, int blocks, int spots) {
		ServerLevel level = helper.getLevel();
		floor(helper);
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		player.setGameMode(GameType.SURVIVAL);
		BlockPos stand = helper.absolutePos(master.north());
		player.setPos(stand.getX() + 0.5, stand.getY(), stand.getZ() + 0.5);
		ProduceDisplayBlockEntity display = place(helper, player, id, master);
		helper.assertTrue(display != null, id + " should stand where it was placed, its records on its first block");
		ProduceDisplayBlock block = (ProduceDisplayBlock) block(id);
		BlockPos first = helper.absolutePos(master);
		BlockState state = level.getBlockState(first);
		helper.assertTrue(state.getValue(ProduceDisplayBlock.FACING) == Direction.NORTH, id + " should face the player who placed it");
		helper.assertTrue(block.cells().length == blocks && display.capacity() == spots, id + " should be " + blocks + " blocks with " + spots
				+ " spots, is " + block.cells().length + " with " + display.capacity());
		for (int part = 0; part < blocks; part++) {
			BlockState there = level.getBlockState(block.partPos(first, Direction.NORTH, part));
			helper.assertTrue(there.is(block) && block.part(there) == part, id + " is missing its block " + part + ": " + there);
		}
		// Produce goes on through its farthest block, a piece a spot, until it is full; then no more is taken.
		BlockPos far = block.partPos(first, Direction.NORTH, blocks - 1);
		BlockHitResult hitFar = new BlockHitResult(Vec3.atCenterOf(far), Direction.UP, far, false);
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.PUMPKIN, spots + 1));
		for (int i = 0; i < spots; i++) {
			level.getBlockState(far).useItemOn(player.getMainHandItem(), level, player, InteractionHand.MAIN_HAND, hitFar);
		}
		helper.assertTrue(display.produce().size() == spots && player.getMainHandItem().getCount() == 1,
				id + " should take one piece a spot: holds " + display.produce().size() + ", " + player.getMainHandItem().getCount() + " left");
		level.getBlockState(far).useItemOn(player.getMainHandItem(), level, player, InteractionHand.MAIN_HAND, hitFar);
		helper.assertTrue(display.produce().size() == spots && player.getMainHandItem().getCount() == 1, id + " should take no more when full");
		// What is not produce is not taken, nor does it take a piece back (used as a player uses it, so an empty-hand use
		// that follows a block's refusal would show).
		display.takeLast();
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.STICK, 4));
		player.gameMode.useItemOn(player, level, player.getMainHandItem(), InteractionHand.MAIN_HAND, hitFar);
		helper.assertTrue(display.produce().size() == spots - 1 && player.getMainHandItem().getCount() == 4,
				id + " should neither take a stick nor give a piece back for one: holds " + display.produce().size());
		// An empty hand takes the last piece back.
		player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
		InteractionResult taken = level.getBlockState(far).useWithoutItem(level, player, hitFar);
		helper.assertTrue(taken.consumesAction() && display.produce().size() == spots - 2 && player.getInventory().countItem(Items.PUMPKIN) == 1,
				id + " should give the last piece back to an empty hand");
		// It remembers what it holds.
		CompoundTag saved = display.saveWithoutMetadata(level.registryAccess());
		ProduceDisplayBlockEntity copy = new ProduceDisplayBlockEntity(first, state);
		copy.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), saved));
		helper.assertTrue(copy.produce().size() == spots - 2 && copy.produce().getFirst().is(Items.PUMPKIN), id + " should remember what it holds");
		// Broken at any block, it all goes, dropping itself once and spilling what it held.
		level.destroyBlock(far, true);
		for (int part = 0; part < blocks; part++) {
			helper.assertFalse(level.getBlockState(block.partPos(first, Direction.NORTH, part)).is(block), id + " should break whole");
		}
		helper.assertTrue(dropped(helper, item(id)) == 1 && dropped(helper, Items.PUMPKIN) == spots - 2,
				id + " should drop once and spill its " + (spots - 2) + " pumpkins: " + dropped(helper, item(id)) + ", " + dropped(helper, Items.PUMPKIN));
	}

	@GameTest
	public void theWheelbarrowHoldsFive(GameTestHelper helper) {
		checkDisplay(helper, "pumpkin_wheelbarrow", new BlockPos(4, 2, 2), 2, 5);
		helper.succeed();
	}

	@GameTest
	public void theWagonHoldsTwentyEight(GameTestHelper helper) {
		checkDisplay(helper, "pumpkin_wagon", new BlockPos(5, 2, 2), 16, 28);
		helper.succeed();
	}

	@GameTest
	public void thePyramidHoldsTwentyFive(GameTestHelper helper) {
		checkDisplay(helper, "pumpkin_pyramid", new BlockPos(6, 2, 2), 9, 25);
		helper.succeed();
	}
}

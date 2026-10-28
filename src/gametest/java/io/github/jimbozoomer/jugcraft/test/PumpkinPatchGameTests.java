package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.agriculture.AttachedGourdStemBlock;
import io.github.jimbozoomer.jugcraft.agriculture.ChalkboardSignBlock;
import io.github.jimbozoomer.jugcraft.agriculture.ChalkboardSignBlockEntity;
import io.github.jimbozoomer.jugcraft.agriculture.GiantWartyGourdBlock;
import io.github.jimbozoomer.jugcraft.agriculture.GourdStemBlock;
import io.github.jimbozoomer.jugcraft.agriculture.JugcraftAgriculture;
import io.github.jimbozoomer.jugcraft.agriculture.ProduceDisplayBlock;
import io.github.jimbozoomer.jugcraft.agriculture.ProduceDisplayBlockEntity;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
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
import net.minecraft.world.item.component.BlockItemStateProperties;
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
 * remember what they hold, and break whole, spilling it. Part 3: the Chalkboard Sign faces the player, chalks the market's
 * words in turn and a named Name Tag's name, only for a player who may build, and keeps its words when broken; Pumpkin
 * Vine Cover lies flat, is walked through, won't go where nothing holds it up and comes off when its support goes; the
 * Giant Warty Gourd stands whole, two by three by two, and breaks whole, dropping itself once.
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

	private static List<ItemEntity> drops(GameTestHelper helper, Item item) {
		return helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(helper.absolutePos(new BlockPos(4, 2, 4))).inflate(8.0),
				entity -> entity.getItem().is(item));
	}

	private static int dropped(GameTestHelper helper, Item item) {
		return drops(helper, item).stream().mapToInt(entity -> entity.getItem().getCount()).sum();
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
		// What is not produce is not taken.
		display.takeLast();
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.STICK, 4));
		level.getBlockState(far).useItemOn(player.getMainHandItem(), level, player, InteractionHand.MAIN_HAND, hitFar);
		helper.assertTrue(display.produce().size() == spots - 1 && player.getMainHandItem().getCount() == 4, id + " should not take a stick");
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

	// ---------------------------------------------------------------- part 3

	private static BlockHitResult hit(GameTestHelper helper, BlockPos pos, Direction side) {
		BlockPos absolute = helper.absolutePos(pos);
		return new BlockHitResult(Vec3.atCenterOf(absolute).relative(side, 0.5), side, absolute, false);
	}

	private static InteractionResult use(GameTestHelper helper, ServerPlayer player, BlockPos pos, Direction side) {
		return player.gameMode.useItemOn(player, helper.getLevel(), player.getMainHandItem(), InteractionHand.MAIN_HAND, hit(helper, pos, side));
	}

	private static ServerPlayer standing(GameTestHelper helper, BlockPos at, ItemStack held) {
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		player.setGameMode(GameType.SURVIVAL);
		BlockPos absolute = helper.absolutePos(at);
		player.setPos(absolute.getX() + 0.5, absolute.getY(), absolute.getZ() + 0.5);
		player.setItemInHand(InteractionHand.MAIN_HAND, held);
		return player;
	}

	/**
	 * A Chalkboard Sign faces the player, saying PUMPKINS; use chalks the next words; a player who may not build changes
	 * nothing; a Name Tag named in an anvil chalks its name, and isn't used up; an unnamed one chalks nothing; with its own
	 * words, use changes nothing and sneak-use wipes them; broken, it keeps its words, its own as its name.
	 */
	@GameTest(maxTicks = 40)
	public void theChalkboardTakesWords(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		floor(helper);
		ServerPlayer player = standing(helper, new BlockPos(3, 2, 6), new ItemStack(item("chalkboard_sign")));
		player.setYRot(180.0F);
		BlockPos pos = new BlockPos(3, 2, 3);
		player.getMainHandItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit(helper, pos.below(), Direction.UP)));
		helper.assertTrue(helper.getBlockState(pos).getValue(ChalkboardSignBlock.FACING) == Direction.SOUTH
				&& helper.getBlockState(pos).getValue(ChalkboardSignBlock.WORDS) == ChalkboardSignBlock.Words.PUMPKINS, "It faces the player, saying PUMPKINS");
		ChalkboardSignBlockEntity sign = helper.getBlockEntity(pos, ChalkboardSignBlockEntity.class);
		player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
		use(helper, player, pos, Direction.SOUTH);
		use(helper, player, pos, Direction.SOUTH);
		helper.assertTrue(helper.getBlockState(pos).getValue(ChalkboardSignBlock.WORDS) == ChalkboardSignBlock.Words.U_PICK, "Use chalks the next words");
		player.setGameMode(GameType.ADVENTURE);
		use(helper, player, pos, Direction.SOUTH);
		helper.assertTrue(helper.getBlockState(pos).getValue(ChalkboardSignBlock.WORDS) == ChalkboardSignBlock.Words.U_PICK,
				"A player who may not build changes nothing");
		player.setGameMode(GameType.SURVIVAL);
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.NAME_TAG));
		use(helper, player, pos, Direction.SOUTH);
		helper.assertTrue(sign.text().isEmpty(), "An unnamed tag chalks nothing");
		ItemStack tag = new ItemStack(Items.NAME_TAG);
		tag.set(DataComponents.CUSTOM_NAME, Component.literal("Hayrides at Noon"));
		player.setItemInHand(InteractionHand.MAIN_HAND, tag);
		use(helper, player, pos, Direction.SOUTH);
		helper.assertTrue(sign.text().equals("Hayrides at Noon") && player.getMainHandItem().getCount() == 1, "A named tag chalks its name, kept");
		player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
		use(helper, player, pos, Direction.SOUTH);
		helper.assertTrue(helper.getBlockState(pos).getValue(ChalkboardSignBlock.WORDS) == ChalkboardSignBlock.Words.U_PICK
				&& sign.text().equals("Hayrides at Noon"), "With its own words, use changes nothing");
		player.setShiftKeyDown(true);
		use(helper, player, pos, Direction.SOUTH);
		player.setShiftKeyDown(false);
		helper.assertTrue(sign.text().isEmpty(), "Sneak-use wipes them off");
		sign.paint("Cider Inside");
		level.destroyBlock(helper.absolutePos(pos), true);
		helper.runAfterDelay(3, () -> {
			List<ItemEntity> signs = drops(helper, item("chalkboard_sign"));
			helper.assertTrue(signs.size() == 1, "Broken, it drops once");
			ItemStack stack = signs.get(0).getItem();
			Component name = stack.get(DataComponents.CUSTOM_NAME);
			BlockItemStateProperties state = stack.get(DataComponents.BLOCK_STATE);
			helper.assertTrue(name != null && name.getString().equals("Cider Inside"), "with its own words as its name: " + name);
			helper.assertTrue(state != null && state.apply(block("chalkboard_sign").defaultBlockState()).getValue(ChalkboardSignBlock.WORDS)
					== ChalkboardSignBlock.Words.U_PICK, "and the market's words it showed: " + state);
			helper.succeed();
		});
	}

	/**
	 * Pumpkin Vine Cover lies flat on the ground (a pixel high, walked through); it won't go where nothing holds it up;
	 * when the block under it goes, it comes off as an item.
	 */
	@GameTest(maxTicks = 40)
	public void theVineCoverLiesOnTheGround(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		floor(helper);
		helper.setBlock(new BlockPos(3, 2, 3), Blocks.STONE);
		helper.setBlock(new BlockPos(3, 3, 3), Blocks.STONE);
		ServerPlayer player = standing(helper, new BlockPos(3, 2, 6), new ItemStack(item("pumpkin_vine_cover"), 4));
		BlockPos ground = new BlockPos(5, 2, 3);
		player.getMainHandItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit(helper, ground.below(), Direction.UP)));
		helper.assertBlockPresent(block("pumpkin_vine_cover"), ground);
		BlockPos absolute = helper.absolutePos(ground);
		BlockState cover = level.getBlockState(absolute);
		helper.assertTrue(cover.getCollisionShape(level, absolute).isEmpty() && cover.getShape(level, absolute).max(Direction.Axis.Y) <= 1.0 / 16 + 1e-6,
				"It lies a pixel high and is walked through");
		// Against the side of the stone pillar's top, with nothing under that spot, it won't go.
		player.getMainHandItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit(helper, new BlockPos(3, 3, 3), Direction.EAST)));
		helper.assertBlockNotPresent(block("pumpkin_vine_cover"), new BlockPos(4, 3, 3));
		helper.setBlock(ground.below(), Blocks.AIR);
		helper.assertBlockNotPresent(block("pumpkin_vine_cover"), ground);
		helper.runAfterDelay(3, () -> {
			helper.assertTrue(dropped(helper, item("pumpkin_vine_cover")) == 1,
					"Without the ground under it, it comes off as an item");
			helper.succeed();
		});
	}

	/**
	 * The Giant Warty Gourd stands whole where it is placed, two blocks across, three up and two deep from the block aimed
	 * at, facing the player, each block with its piece of the gourd's shape; use does nothing to it; broken at its top, it
	 * all goes and drops itself once.
	 */
	@GameTest(maxTicks = 40)
	public void theGiantWartyGourdStandsWhole(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		floor(helper);
		BlockPos master = new BlockPos(4, 2, 2);
		ServerPlayer player = standing(helper, master.north(), new ItemStack(item("giant_warty_gourd")));
		player.setYRot(0.0F);
		player.getMainHandItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit(helper, master.below(), Direction.UP)));
		GiantWartyGourdBlock block = (GiantWartyGourdBlock) block("giant_warty_gourd");
		BlockPos first = helper.absolutePos(master);
		helper.assertTrue(level.getBlockState(first).is(block) && level.getBlockState(first).getValue(GiantWartyGourdBlock.FACING) == Direction.NORTH,
				"It stands where it was placed, facing the player");
		helper.assertTrue(block.cells().length == 12 && block.across() == 2 && block.deep() == 2, "It is two by three by two blocks");
		for (int part = 0; part < 12; part++) {
			BlockPos at = block.partPos(first, Direction.NORTH, part);
			BlockState there = level.getBlockState(at);
			helper.assertTrue(there.is(block) && block.part(there) == part && !there.getShape(level, at).isEmpty(),
					"It is missing its block " + part + ", or that block its piece of the gourd: " + there);
		}
		player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
		BlockState before = level.getBlockState(first);
		use(helper, player, master, Direction.NORTH);
		helper.assertTrue(level.getBlockState(first) == before, "Use does nothing to it");
		BlockPos top = block.partPos(first, Direction.NORTH, 11);
		level.destroyBlock(top, true);
		for (int part = 0; part < 12; part++) {
			helper.assertFalse(level.getBlockState(block.partPos(first, Direction.NORTH, part)).is(block), "It breaks whole");
		}
		helper.runAfterDelay(3, () -> {
			helper.assertTrue(dropped(helper, item("giant_warty_gourd")) == 1,
					"It drops itself once");
			helper.succeed();
		});
	}
}

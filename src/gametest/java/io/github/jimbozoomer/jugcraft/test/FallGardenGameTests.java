package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.agriculture.FacingDecorationBlock;
import io.github.jimbozoomer.jugcraft.agriculture.JugcraftAgriculture;
import io.github.jimbozoomer.jugcraft.agriculture.PlanterBlock;
import io.github.jimbozoomer.jugcraft.agriculture.PlanterBlockEntity;
import io.github.jimbozoomer.jugcraft.agriculture.PorchSwingBlock;
import io.github.jimbozoomer.jugcraft.agriculture.Seat;
import io.github.jimbozoomer.jugcraft.agriculture.StringLightHookBlock;
import io.github.jimbozoomer.jugcraft.agriculture.StringLightHookBlockEntity;
import io.github.jimbozoomer.jugcraft.agriculture.StringLightsItem;
import io.github.jimbozoomer.jugcraft.agriculture.WallDecorationBlock;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Fall Garden & Porch, part 1 (decor set 4; docs/features/fall-garden-and-porch.md): every fall plant has its block and
 * item; the asters, stonecrop and ornamental kale and cabbage are small flowers with potted forms; pampas grass is a tall
 * flower and the dried sunflower a two-block plant; their dye recipes and the garden plants' recipes load; each stands on
 * grass; and bittersweet holds to a wall, a hand taking none of it and shears one for each face. Part 2: the Window Box
 * won't hang without a wall and hangs on one facing out; it takes three small flowers and refuses a fourth and anything
 * that isn't one; an empty hand takes back the last planted; it comes off with its wall, giving back its flowers. The
 * Half-Barrel and the Galvanized Tub stand facing the player and take one and two; broken, they give their flowers back.
 * Part 3: the Porch Swing won't hang without a ceiling, hangs whole from one, seats two (one on each seat) and comes down
 * whole with its ceiling, dropping once; the Corn Dolly and the Bittersweet Wreath hang on a wall and come off with it;
 * the Wheat Stook and the Quilt Rail stand facing the player and drop themselves. Part 4: Café String Lights strung
 * between two hooks light nothing while dark; lit, they put a lamp in each empty spot along them (never over a block
 * there), put their lamps out when they go dark, light again, and put them out when a hook is broken.
 */
public class FallGardenGameTests {
	private static final TagKey<Item> SMALL_FLOWERS = TagKey.create(Registries.ITEM, Identifier.withDefaultNamespace("small_flowers"));
	private static final TagKey<Item> FLOWER_TAG = TagKey.create(Registries.ITEM, Identifier.withDefaultNamespace("flowers"));
	private static final List<String> FLOWERS = List.of("aster", "sedum", "ornamental_kale", "ornamental_cabbage");
	private static final List<String> PLANTS = List.of("aster", "sedum", "ornamental_kale", "ornamental_cabbage", "pampas_grass",
			"dried_sunflower", "bittersweet");

	private static Block block(String id) {
		return JugcraftAgriculture.block(id);
	}

	private static Item item(String id) {
		return JugcraftAgriculture.item(id);
	}

	@GameTest
	public void fallPlantsAreRegisteredAndStand(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		for (int x = 0; x < 8; x++) {
			for (int z = 0; z < 8; z++) {
				helper.setBlock(new BlockPos(x, 1, z), Blocks.GRASS_BLOCK);
			}
		}
		for (String plant : PLANTS) {
			helper.assertTrue(item(plant) instanceof BlockItem held && held.getBlock() == block(plant), plant + " has its own item");
		}
		for (String flower : FLOWERS) {
			helper.assertTrue(new ItemStack(item(flower)).is(SMALL_FLOWERS), flower + " is a small flower");
			helper.assertTrue(block("potted_" + flower) instanceof FlowerPotBlock pot && pot.getPotted() == block(flower), flower + " can be potted");
		}
		helper.assertTrue(new ItemStack(item("pampas_grass")).is(FLOWER_TAG) && block("pampas_grass") instanceof DoublePlantBlock,
				"Pampas grass is a tall flower");
		helper.assertTrue(block("dried_sunflower") instanceof DoublePlantBlock, "The dried sunflower stands two blocks");
		for (String recipe : List.of("purple_dye_from_aster", "pink_dye_from_sedum", "magenta_dye_from_ornamental_kale",
				"white_dye_from_ornamental_cabbage", "white_dye_from_pampas_grass", "ornamental_kale", "ornamental_cabbage", "dried_sunflower")) {
			helper.assertTrue(level.getServer().getRecipeManager().byKey(ResourceKey.create(Registries.RECIPE, Jugcraft.id(recipe))).isPresent(),
					recipe + " loads");
		}
		BlockPos at = helper.absolutePos(new BlockPos(1, 2, 1));
		for (String plant : List.of("aster", "sedum", "ornamental_kale", "ornamental_cabbage", "pampas_grass", "dried_sunflower")) {
			helper.assertTrue(block(plant).defaultBlockState().canSurvive(level, at), plant + " stands on grass");
		}
		helper.succeed();
	}

	@GameTest
	public void bittersweetClimbsAWall(GameTestHelper helper) {
		Block vine = block("bittersweet");
		for (int y = 1; y < 5; y++) {
			for (int x = 1; x < 6; x++) {
				helper.setBlock(new BlockPos(x, y, 2), Blocks.OAK_LOG);
			}
		}
		BlockPos at = new BlockPos(3, 2, 3);
		BlockState state = vine.defaultBlockState().setValue(MultifaceBlock.getFaceProperty(Direction.NORTH), true);
		helper.setBlock(at, state);
		helper.assertTrue(helper.getBlockState(at).is(vine), "Bittersweet holds to a wall");
		ServerLevel level = helper.getLevel();
		BlockPos absolute = helper.absolutePos(at);
		List<ItemStack> byHand = Block.getDrops(level.getBlockState(absolute), level, absolute, null, null, ItemStack.EMPTY);
		List<ItemStack> sheared = Block.getDrops(level.getBlockState(absolute), level, absolute, null, null, new ItemStack(Items.SHEARS));
		helper.assertTrue(byHand.stream().noneMatch(s -> s.is(item("bittersweet"))), "A hand takes none");
		helper.assertTrue(sheared.stream().filter(s -> s.is(item("bittersweet"))).mapToInt(ItemStack::getCount).sum() == 1,
				"Shears take one for its face");
		helper.succeed();
	}

	// ---------------------------------------------------------------- part 2

	private static BlockHitResult hit(GameTestHelper helper, BlockPos pos, Direction side) {
		BlockPos absolute = helper.absolutePos(pos);
		return new BlockHitResult(Vec3.atCenterOf(absolute).relative(side, 0.5), side, absolute, false);
	}

	private static ServerPlayer standing(GameTestHelper helper, BlockPos at, ItemStack held) {
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		player.setGameMode(GameType.SURVIVAL);
		BlockPos absolute = helper.absolutePos(at);
		player.setPos(absolute.getX() + 0.5, absolute.getY(), absolute.getZ() + 0.5);
		player.setItemInHand(InteractionHand.MAIN_HAND, held);
		return player;
	}

	private static InteractionResult use(GameTestHelper helper, ServerPlayer player, BlockPos pos, ItemStack held) {
		player.setItemInHand(InteractionHand.MAIN_HAND, held);
		return player.gameMode.useItemOn(player, helper.getLevel(), player.getMainHandItem(), InteractionHand.MAIN_HAND, hit(helper, pos, Direction.UP));
	}

	private static int dropped(GameTestHelper helper, Item item) {
		return helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(helper.absolutePos(new BlockPos(3, 2, 3))).inflate(5.0),
				entity -> entity.getItem().is(item)).stream().mapToInt(entity -> entity.getItem().getCount()).sum();
	}

	@GameTest(maxTicks = 40)
	public void theWindowBoxTakesThreeFlowers(GameTestHelper helper) {
		for (int x = 0; x < 8; x++) {
			for (int z = 0; z < 8; z++) {
				helper.setBlock(new BlockPos(x, 1, z), Blocks.STONE);
			}
		}
		BlockPos wall = new BlockPos(3, 3, 4);
		BlockPos pos = wall.north();
		Block box = block("window_box");
		ServerPlayer player = standing(helper, new BlockPos(3, 2, 1), new ItemStack(item("window_box")));
		player.getMainHandItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit(helper, pos.below(), Direction.UP)));
		helper.assertBlockNotPresent(box, pos);
		helper.setBlock(wall, Blocks.OAK_PLANKS);
		player.getMainHandItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit(helper, wall, Direction.NORTH)));
		BlockState state = helper.getBlockState(pos);
		helper.assertTrue(state.is(box) && state.getValue(PlanterBlock.FACING) == Direction.NORTH, "It hangs on the wall facing out: " + state);
		PlanterBlockEntity planted = helper.getBlockEntity(pos, PlanterBlockEntity.class);
		helper.assertFalse(use(helper, player, pos, new ItemStack(Items.STICK)).consumesAction(), "A stick isn't planted");
		for (Item flower : List.of(item("aster"), Items.POPPY, Items.DANDELION)) {
			use(helper, player, pos, new ItemStack(flower, 2));
			helper.assertTrue(player.getMainHandItem().getCount() == 1, "Planting spends one " + flower);
		}
		helper.assertTrue(planted.plants().size() == 3 && planted.plants().get(0).is(item("aster")), "Three flowers grow in it");
		ItemStack fourth = new ItemStack(Items.CORNFLOWER, 2);
		helper.assertTrue(use(helper, player, pos, fourth).consumesAction() && fourth.getCount() == 2 && planted.plants().size() == 3,
				"A fourth isn't planted, nor placed in front of it");
		use(helper, player, pos, ItemStack.EMPTY);
		helper.assertTrue(planted.plants().size() == 2 && player.getInventory().countItem(Items.DANDELION) == 1,
				"An empty hand takes back the last planted");
		helper.setBlock(wall, Blocks.AIR);
		helper.runAfterDelay(3, () -> {
			helper.assertTrue(helper.getBlockState(pos).isAir() && dropped(helper, item("window_box")) == 1
					&& dropped(helper, item("aster")) == 1 && dropped(helper, Items.POPPY) == 1, "Without its wall it comes off, giving back its flowers");
			helper.succeed();
		});
	}

	@GameTest(maxTicks = 40)
	public void theBarrelAndTubTakeTheirFlowers(GameTestHelper helper) {
		for (int x = 0; x < 8; x++) {
			for (int z = 0; z < 8; z++) {
				helper.setBlock(new BlockPos(x, 1, z), Blocks.STONE);
			}
		}
		BlockPos barrelPos = new BlockPos(2, 2, 3);
		BlockPos tubPos = new BlockPos(5, 2, 3);
		ServerPlayer player = standing(helper, new BlockPos(3, 2, 1), new ItemStack(item("half_barrel_planter")));
		player.getMainHandItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit(helper, barrelPos.below(), Direction.UP)));
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(item("galvanized_tub_planter")));
		player.getMainHandItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit(helper, tubPos.below(), Direction.UP)));
		helper.assertTrue(helper.getBlockState(barrelPos).is(block("half_barrel_planter"))
				&& helper.getBlockState(barrelPos).getValue(PlanterBlock.FACING) == Direction.NORTH
				&& helper.getBlockState(tubPos).is(block("galvanized_tub_planter")), "Both stand on the floor facing the player");
		PlanterBlockEntity barrel = helper.getBlockEntity(barrelPos, PlanterBlockEntity.class);
		PlanterBlockEntity tub = helper.getBlockEntity(tubPos, PlanterBlockEntity.class);
		use(helper, player, barrelPos, new ItemStack(item("ornamental_kale")));
		use(helper, player, barrelPos, new ItemStack(item("sedum")));
		for (int i = 0; i < 3; i++) {
			use(helper, player, tubPos, new ItemStack(item("aster")));
		}
		helper.assertTrue(barrel.plants().size() == 1 && barrel.plants().get(0).is(item("ornamental_kale")) && tub.plants().size() == 2,
				"The barrel takes one, the tub two: " + barrel.plants() + ", " + tub.plants());
		helper.getLevel().destroyBlock(helper.absolutePos(barrelPos), true);
		helper.getLevel().destroyBlock(helper.absolutePos(tubPos), true);
		helper.runAfterDelay(3, () -> {
			helper.assertTrue(dropped(helper, item("ornamental_kale")) == 1 && dropped(helper, item("aster")) == 2
					&& dropped(helper, item("half_barrel_planter")) == 1 && dropped(helper, item("galvanized_tub_planter")) == 1,
					"Broken, they give back their flowers and themselves");
			helper.succeed();
		});
	}

	// ---------------------------------------------------------------- part 3

	private static void floor(GameTestHelper helper) {
		for (int x = 0; x < 8; x++) {
			for (int z = 0; z < 8; z++) {
				helper.setBlock(new BlockPos(x, 1, z), Blocks.STONE);
			}
		}
	}

	@GameTest(maxTicks = 40)
	public void theSwingHangsAndSeatsTwo(GameTestHelper helper) {
		floor(helper);
		BlockPos pos = new BlockPos(4, 2, 3);
		PorchSwingBlock swing = (PorchSwingBlock) block("porch_swing");
		ServerPlayer player = standing(helper, new BlockPos(4, 2, 1), new ItemStack(item("porch_swing")));
		player.getMainHandItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit(helper, pos.below(), Direction.UP)));
		helper.assertBlockNotPresent(swing, pos);
		BlockPos master = helper.absolutePos(pos);
		for (int part = 2; part < swing.cells().length; part++) {
			helper.getLevel().setBlockAndUpdate(swing.partPos(master, Direction.NORTH, part).above(), Blocks.OAK_PLANKS.defaultBlockState());
		}
		player.getMainHandItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit(helper, pos.below(), Direction.UP)));
		for (int part = 0; part < swing.cells().length; part++) {
			helper.assertTrue(helper.getLevel().getBlockState(swing.partPos(master, Direction.NORTH, part)).is(swing), "Its block " + part + " is there");
		}
		BlockPos second = swing.partPos(master, Direction.NORTH, 1);
		player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
		player.gameMode.useItemOn(player, helper.getLevel(), ItemStack.EMPTY, InteractionHand.MAIN_HAND,
				new BlockHitResult(Vec3.atCenterOf(master), Direction.UP, master, false));
		ServerPlayer friend = standing(helper, new BlockPos(3, 2, 1), ItemStack.EMPTY);
		friend.gameMode.useItemOn(friend, helper.getLevel(), ItemStack.EMPTY, InteractionHand.MAIN_HAND,
				new BlockHitResult(Vec3.atCenterOf(second.above()), Direction.NORTH, second.above(), false));
		helper.assertTrue(player.isPassenger() && friend.isPassenger() && Seat.at(helper.getLevel(), master).size() == 1
				&& Seat.at(helper.getLevel(), second).size() == 1, "Two sit on it, one on each seat (the second from its chains above)");
		player.stopRiding();
		friend.stopRiding();
		helper.getLevel().setBlockAndUpdate(swing.partPos(master, Direction.NORTH, 3).above(), Blocks.AIR.defaultBlockState());
		helper.runAfterDelay(3, () -> {
			helper.assertTrue(helper.getLevel().getBlockState(master).isAir() && dropped(helper, item("porch_swing")) == 1,
					"Without its ceiling it comes down whole, dropping once");
			helper.succeed();
		});
	}

	@GameTest(maxTicks = 40)
	public void thePorchDecorationsStand(GameTestHelper helper) {
		floor(helper);
		BlockPos wall = new BlockPos(3, 3, 5);
		helper.setBlock(wall, Blocks.OAK_PLANKS);
		helper.setBlock(wall.east(), Blocks.OAK_PLANKS);
		ServerPlayer player = standing(helper, new BlockPos(3, 2, 1), new ItemStack(item("corn_dolly")));
		player.getMainHandItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit(helper, wall, Direction.NORTH)));
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(item("bittersweet_wreath")));
		player.getMainHandItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit(helper, wall.east(), Direction.NORTH)));
		helper.assertTrue(helper.getBlockState(wall.north()).is(block("corn_dolly")) && helper.getBlockState(wall.east().north()).is(block("bittersweet_wreath"))
				&& helper.getBlockState(wall.north()).getValue(WallDecorationBlock.FACING) == Direction.NORTH, "The dolly and the wreath hang on the wall");
		BlockPos stookPos = new BlockPos(2, 2, 3);
		BlockPos railPos = new BlockPos(5, 2, 3);
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(item("wheat_stook")));
		player.getMainHandItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit(helper, stookPos.below(), Direction.UP)));
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(item("quilt_rail")));
		player.getMainHandItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit(helper, railPos.below(), Direction.UP)));
		helper.assertTrue(helper.getBlockState(stookPos).is(block("wheat_stook")) && helper.getBlockState(railPos).is(block("quilt_rail"))
				&& helper.getBlockState(railPos).getValue(FacingDecorationBlock.FACING) == Direction.NORTH, "The stook and the rail stand facing the player");
		helper.setBlock(wall, Blocks.AIR);
		helper.setBlock(wall.east(), Blocks.AIR);
		helper.getLevel().destroyBlock(helper.absolutePos(stookPos), true);
		helper.getLevel().destroyBlock(helper.absolutePos(railPos), true);
		helper.runAfterDelay(3, () -> {
			helper.assertTrue(dropped(helper, item("corn_dolly")) == 1 && dropped(helper, item("bittersweet_wreath")) == 1
					&& dropped(helper, item("wheat_stook")) == 1 && dropped(helper, item("quilt_rail")) == 1,
					"Without their wall the dolly and the wreath come off; broken, the stook and the rail drop themselves");
			helper.succeed();
		});
	}

	// ---------------------------------------------------------------- part 4

	@GameTest(maxTicks = 40)
	public void cafeLightsLightAlongTheirLength(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		floor(helper);
		// Two hooks on posts, so the strand sags through the air between them.
		BlockPos a = new BlockPos(1, 4, 3);
		BlockPos b = new BlockPos(7, 4, 3);
		for (BlockPos hook : List.of(a, b)) {
			helper.setBlock(hook.below(), Blocks.STONE);
			helper.setBlock(hook.below(2), Blocks.STONE);
			helper.setBlock(hook, block("string_light_hook").defaultBlockState().setValue(StringLightHookBlock.FACE, AttachFace.FLOOR)
					.setValue(StringLightHookBlock.FACING, Direction.NORTH));
		}
		helper.assertTrue(StringLightsItem.string(level, helper.absolutePos(a), helper.absolutePos(b), StringLightHookBlockEntity.Strand.CAFE)
				== StringLightsItem.Result.STRUNG, "Strung");
		StringLightHookBlockEntity hook = helper.getBlockEntity(a, StringLightHookBlockEntity.class);
		List<BlockPos> spots = hook.lampSpots();
		helper.assertTrue(spots.size() >= 2, "It has spots along it for its lamps: " + spots);
		BlockPos built = spots.get(0);
		level.setBlockAndUpdate(built, Blocks.OAK_PLANKS.defaultBlockState());
		hook.update(level);
		helper.assertTrue(hook.lamps().isEmpty(), "Dark, it lights nothing along it");
		helper.setBlock(a.north(), Blocks.REDSTONE_BLOCK);
		hook.update(level);
		helper.assertTrue(hook.lamps().size() == spots.size() - 1 && level.getBlockState(built).is(Blocks.OAK_PLANKS), "Lit, it lights each empty spot, not one built in: "
				+ hook.lamps());
		for (BlockPos lamp : hook.lamps()) {
			BlockState there = level.getBlockState(lamp);
			helper.assertTrue(there.is(Blocks.LIGHT) && there.getValue(LightBlock.LEVEL) == StringLightHookBlockEntity.LAMP_LIGHT, "A lamp at " + lamp);
		}
		List<BlockPos> lit = hook.lamps();
		helper.setBlock(a.north(), Blocks.AIR);
		hook.update(level);
		helper.assertTrue(hook.lamps().isEmpty() && lit.stream().allMatch(pos -> level.getBlockState(pos).isAir()), "Dark again, its lamps go out");
		helper.setBlock(a.north(), Blocks.REDSTONE_BLOCK);
		hook.update(level);
		List<BlockPos> again = hook.lamps();
		helper.assertTrue(!again.isEmpty(), "And light again");
		level.destroyBlock(helper.absolutePos(a), true);
		helper.assertTrue(again.stream().allMatch(pos -> level.getBlockState(pos).isAir()), "Its hook broken, its lamps go out");
		helper.runAfterDelay(3, () -> {
			helper.assertTrue(dropped(helper, item("cafe_string_lights")) == 1, "and its strand drops");
			helper.succeed();
		});
	}
}

package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.agriculture.JugcraftAgriculture;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Fall Garden & Porch, part 1 (decor set 4; docs/features/fall-garden-and-porch.md): every fall plant has its block and
 * item; the asters, stonecrop and ornamental kale and cabbage are small flowers with potted forms; pampas grass is a tall
 * flower and the dried sunflower a two-block plant; their dye recipes and the garden plants' recipes load; each stands on
 * grass; and bittersweet holds to a wall, a hand taking none of it and shears one for each face.
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
}

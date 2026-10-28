package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.agriculture.AttachedGourdStemBlock;
import io.github.jimbozoomer.jugcraft.agriculture.GourdStemBlock;
import io.github.jimbozoomer.jugcraft.agriculture.JugcraftAgriculture;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;

/**
 * The Pumpkin Patch Market's squashes and gourds (decor set 1, part 1; docs/features/pumpkin-patch-market.md): each seed
 * plants its stem on farmland, each grown stem puts its own gourd beside it and bends towards it, each gourd drops itself,
 * is smaller than a block (light and faces pass it) and counts in the Crowfield's harvest, and each has its wild patch.
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
}

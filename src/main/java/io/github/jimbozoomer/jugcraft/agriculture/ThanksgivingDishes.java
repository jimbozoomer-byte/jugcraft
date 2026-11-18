package io.github.jimbozoomer.jugcraft.agriculture;

import io.github.jimbozoomer.jugcraft.agriculture.PlacedDishBlock.DishShape;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.resources.Identifier;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

/**
 * The Thanksgiving side dishes (Thanksgiving batch 2; tools/thanksgiving_dishes.py; docs/features/thanksgiving-dishes.md):
 * six placed feast dishes, each a food eaten like the menu's plated meals (a bowl food that gives the bowl back, one to a
 * stack) that a sneaking player sets down on a block as its sculpted dish, facing them, and takes back with an empty hand
 * ({@link PlacedDishBlock}, exactly as {@link MenuDishes}'s dishes). The dish block shares its food's ID and has no item of
 * its own: it drops that food once when broken and gives it back when taken, so setting it down is never a gain.
 */
public final class ThanksgivingDishes {
	/** A dish: its ID, its food (hunger, saturation modifier) and how it stands set down. */
	public record Dish(String id, int nutrition, float saturation, DishShape shape) {
	}

	/** The six dishes, in the order of tools/thanksgiving_dishes.py DISHES (its numbers; the check compares them). */
	public static final List<Dish> DISHES = List.of(
			new Dish("glazed_carrots", 9, 0.7F, DishShape.PLATE),
			new Dish("roasted_root_vegetables", 10, 0.7F, DishShape.PLATE),
			new Dish("scalloped_potatoes", 9, 0.7F, DishShape.PLATE),
			new Dish("apple_cranberry_crisp", 8, 0.6F, DishShape.PLATE),
			new Dish("stuffed_mushrooms", 7, 0.6F, DishShape.PLATE),
			new Dish("skillet_corn_cakes", 9, 0.7F, DishShape.PLATE));

	/** Registers a block as JugcraftAgriculture.registerBlock does. */
	@FunctionalInterface
	public interface BlockRegistrar {
		Block register(String id, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties properties);
	}

	/** Registers an item in the agriculture food tab, as JugcraftAgriculture.registerItem does. */
	@FunctionalInterface
	public interface ItemRegistrar {
		Item register(String id, Function<Item.Properties, Item> factory, Item.Properties properties);
	}

	private ThanksgivingDishes() {
	}

	/**
	 * Registers each dish's food and its placed block (the block named as the food, as the menu's are), and sets them down
	 * in {@code setDownPhase}, the use-block phase the menu's dishes and the pie use.
	 */
	public static void register(BlockRegistrar blocks, ItemRegistrar items, Identifier setDownPhase) {
		Map<Item, PlacedDishBlock> placed = new HashMap<>();
		for (Dish dish : DISHES) {
			FoodProperties food = new FoodProperties.Builder().nutrition(dish.nutrition()).saturationModifier(dish.saturation()).build();
			Item item = items.register(dish.id(), Item::new, new Item.Properties().food(food).usingConvertsTo(Items.BOWL).stacksTo(1));
			PlacedDishBlock block = (PlacedDishBlock) blocks.register(dish.id(), props -> new PlacedDishBlock(item, dish.shape(), props),
					BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_WHITE).strength(0.3F).sound(SoundType.WOOL).noOcclusion()
							.pushReaction(PushReaction.POPPED));
			placed.put(item, block);
		}
		UseBlockCallback.EVENT.register(setDownPhase, (player, level, hand, hit) -> PlacedDishBlock.setDown(placed, player, level, hand, hit));
	}
}

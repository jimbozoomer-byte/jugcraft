package io.github.jimbozoomer.jugcraft.agriculture;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import java.util.List;
import java.util.function.Function;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

/**
 * The Thanksgiving festival toys and lively props (group D of the Thanksgiving package; tools/thanksgiving_toys.py,
 * docs/features/thanksgiving-toys.md): the Corn Husk Dolls, Turkey Pull Toy, Spinning Top, Mayflower Model Ship,
 * Harvest Photo Frame Backdrop, Tabletop Corn Toss Set, Prize Pumpkin Display and Harvest Percussion Set. They are
 * decorations first, craftable from the start and kept all year; nothing here follows the calendar or asks for
 * research, and none of them stores items, pays out or trades. The shapes and numbers are the table's
 * (tools/thanksgiving_toys_check.py compares them).
 *
 * <p>JugcraftAgriculture registers them through {@link #register}, handing over its own block and item registration so
 * they share its registries, lookups and building tab.
 */
public final class FestivalToys {
	public static final String CORN_HUSK_DOLLS = "corn_husk_dolls";
	public static final String TURKEY_PULL_TOY = "turkey_pull_toy";
	public static final String SPINNING_TOP = "spinning_top";
	public static final String MAYFLOWER_MODEL_SHIP = "mayflower_model_ship";
	public static final String HARVEST_PHOTO_BACKDROP = "harvest_photo_backdrop";
	public static final String CORN_TOSS_SET = "corn_toss_set";
	public static final String PRIZE_PUMPKIN_DISPLAY = "prize_pumpkin_display";
	public static final String HARVEST_PERCUSSION_SET = "harvest_percussion_set";
	/** Every piece, in the table's order. */
	public static final List<String> ALL = List.of(CORN_HUSK_DOLLS, TURKEY_PULL_TOY, SPINNING_TOP, MAYFLOWER_MODEL_SHIP,
			HARVEST_PHOTO_BACKDROP, CORN_TOSS_SET, PRIZE_PUMPKIN_DISPLAY, HARVEST_PERCUSSION_SET);
	/** The pieces whose items carry a hint of what using them does (item.jugcraft.<id>.tooltip). */
	public static final List<String> HINTED = List.of(SPINNING_TOP, HARVEST_PHOTO_BACKDROP, CORN_TOSS_SET, HARVEST_PERCUSSION_SET);

	/** The Corn Toss Set's score keeper (scores only: it holds no items). */
	public static BlockEntityType<FestivalToyCornTossBlockEntity> CORN_TOSS_ENTITY;

	/** Registers a block as JugcraftAgriculture.registerBlock does. */
	@FunctionalInterface
	public interface BlockRegistrar {
		Block register(String id, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties properties);
	}

	/** Registers an item in the agriculture building tab, as JugcraftAgriculture.registerItem does. */
	@FunctionalInterface
	public interface ItemRegistrar {
		Item register(String id, Function<Item.Properties, Item> factory, Item.Properties properties);
	}

	private FestivalToys() {
	}

	public static void register(BlockRegistrar blocks, ItemRegistrar items) {
		Block dolls = blocks.register(CORN_HUSK_DOLLS, props -> new FacingDecorationBlock(props, new double[] {1.0, 0.0, 3.0, 15.0, 14.0, 13.5}),
				toy(MapColor.SAND, SoundType.GRASS, 0.3F).ignitedByLava());
		Block turkey = blocks.register(TURKEY_PULL_TOY, props -> new FacingDecorationBlock(props, new double[] {2.0, 0.0, 0.0, 14.0, 16.0, 14.5}),
				toy(MapColor.TERRACOTTA_ORANGE, SoundType.WOOD, 0.6F).ignitedByLava());
		Block top = blocks.register(SPINNING_TOP, props -> new FestivalToySpinningTopBlock(props, new double[] {2.5, 0.0, 3.5, 13.0, 11.0, 12.5}),
				toy(MapColor.COLOR_RED, SoundType.WOOD, 0.4F).ignitedByLava());
		Block ship = blocks.register(MAYFLOWER_MODEL_SHIP, props -> new FacingDecorationBlock(props, new double[] {0.5, 0.0, 0.5, 15.5, 16.0, 15.5}),
				toy(MapColor.WOOD, SoundType.WOOD, 0.6F).ignitedByLava());
		// forceSolidOn: its thin top rail and sill count as solid, so fluids and buckets cannot wash a block of it away.
		Block backdrop = blocks.register(HARVEST_PHOTO_BACKDROP, FestivalToyBackdropBlock::new, BlockBehaviour.Properties.of()
				.mapColor(MapColor.COLOR_YELLOW).strength(1.5F).sound(SoundType.WOOD).noOcclusion().ignitedByLava().forceSolidOn()
				.lightLevel(FestivalToyBackdropBlock::light).pushReaction(PushReaction.IMMOVEABLE));
		FestivalToyBackdropBlock.register();
		Block toss = blocks.register(CORN_TOSS_SET, props -> new FestivalToyCornTossBlock(props, new double[] {0.5, 0.0, 2.5, 15.5, 7.5, 14.5}),
				toy(MapColor.WOOD, SoundType.WOOD, 0.6F).ignitedByLava());
		Block pumpkin = blocks.register(PRIZE_PUMPKIN_DISPLAY, props -> new FacingDecorationBlock(props, new double[] {1.0, 0.0, 1.0, 15.0, 14.5, 14.5}),
				toy(MapColor.COLOR_ORANGE, SoundType.WOOD, 0.8F).ignitedByLava());
		Block percussion = blocks.register(HARVEST_PERCUSSION_SET, props -> new FestivalToyPercussionBlock(props, new double[] {0.5, 0.0, 0.5, 15.5, 14.0, 14.0}),
				toy(MapColor.WOOD, SoundType.WOOD, 0.6F).ignitedByLava());
		CORN_TOSS_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Jugcraft.id(CORN_TOSS_SET),
				FabricBlockEntityTypeBuilder.create(FestivalToyCornTossBlockEntity::new, toss).build());
		for (Block block : List.of(dolls, turkey, top, ship, backdrop, toss, pumpkin, percussion)) {
			String id = BuiltInRegistries.BLOCK.getKey(block).getPath();
			items.register(id, props -> HINTED.contains(id) ? new FestivalToyBlockItem(block, props, id) : new net.minecraft.world.item.BlockItem(block, props),
					new Item.Properties().useBlockDescriptionPrefix());
		}
	}

	/** A small toy or prop: noOcclusion, popped off by a piston (the Thanksgiving decorations' properties). */
	private static BlockBehaviour.Properties toy(MapColor color, SoundType sound, float strength) {
		return BlockBehaviour.Properties.of().mapColor(color).strength(strength).sound(sound).noOcclusion().pushReaction(PushReaction.POPPED);
	}
}

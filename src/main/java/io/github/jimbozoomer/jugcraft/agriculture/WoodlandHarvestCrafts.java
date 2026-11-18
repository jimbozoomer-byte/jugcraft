package io.github.jimbozoomer.jugcraft.agriculture;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

/**
 * The woodland harvest crafts of the Thanksgiving package (tools/thanksgiving_woodland.py;
 * docs/features/thanksgiving-woodland-crafts.md): everyday harvest and craft decorations inspired by Wampanoag and wider
 * Northeastern Woodlands craft forms (inspired game decor, not historical reconstructions). Each floor piece is a
 * {@link WoodlandCraftBlock} of the table's shape; the Rush Floor Mat is a {@link WoodlandRushMatBlock}. They are plain
 * decorations: none stores, crafts, cooks or grinds anything, none follows the calendar and none asks for research.
 * JugcraftAgriculture registers them through its own registration helper and gives each its item in the building tab.
 */
public final class WoodlandHarvestCrafts {
	/** The floor pieces (tools/thanksgiving_woodland.py PROPS, kind "floor"). */
	public static final List<String> FLOOR = List.of("lidded_twined_basket", "twined_pack_basket", "birch_bark_box", "carved_bowls_and_spoons",
			"incised_earthenware_pot", "corn_drying_frame", "wooden_corn_mortar");
	public static final String RUSH_FLOOR_MAT = "rush_floor_mat";

	/** How a block is registered: JugcraftAgriculture's own helper (its ID, factory and properties). */
	@FunctionalInterface
	public interface Registrar {
		Block register(String id, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties properties);
	}

	private WoodlandHarvestCrafts() {
	}

	/** Registers every piece through {@code blocks}, returning them in the table's order. */
	public static List<Block> register(Registrar blocks) {
		List<Block> out = new ArrayList<>();
		out.add(blocks.register("lidded_twined_basket", props -> new WoodlandCraftBlock(props, new double[] {2.0, 0.0, 2.0, 14.0, 13.0, 14.0}),
				properties(MapColor.SAND, SoundType.GRASS, 0.5F).ignitedByLava()));
		out.add(blocks.register("twined_pack_basket", props -> new WoodlandCraftBlock(props, new double[] {3.0, 0.0, 4.5, 13.0, 16.0, 13.5}),
				properties(MapColor.SAND, SoundType.GRASS, 0.5F).ignitedByLava()));
		out.add(blocks.register("birch_bark_box", props -> new WoodlandCraftBlock(props, new double[] {1.5, 0.0, 3.0, 14.5, 8.0, 13.0}),
				properties(MapColor.QUARTZ, SoundType.WOOD, 0.5F).ignitedByLava()));
		out.add(blocks.register(RUSH_FLOOR_MAT, WoodlandRushMatBlock::new,
				properties(MapColor.SAND, SoundType.GRASS, 0.2F).ignitedByLava()));
		out.add(blocks.register("carved_bowls_and_spoons", props -> new WoodlandCraftBlock(props, new double[] {0.5, 0.0, 0.0, 15.5, 5.5, 15.0}),
				properties(MapColor.WOOD, SoundType.WOOD, 0.5F).ignitedByLava()));
		out.add(blocks.register("incised_earthenware_pot", props -> new WoodlandCraftBlock(props, new double[] {0.0, 0.0, 0.0, 16.0, 15.5, 16.0}),
				properties(MapColor.TERRACOTTA_BROWN, SoundType.DECORATED_POT, 0.6F)));
		out.add(blocks.register("corn_drying_frame", props -> new WoodlandCraftBlock(props, new double[] {0.5, 0.0, 5.5, 15.5, 16.0, 10.5}),
				properties(MapColor.COLOR_YELLOW, SoundType.WOOD, 0.5F).ignitedByLava()));
		out.add(blocks.register("wooden_corn_mortar", props -> new WoodlandCraftBlock(props, new double[] {1.0, 0.0, 0.0, 14.0, 16.0, 15.0}),
				properties(MapColor.WOOD, SoundType.WOOD, 1.0F).ignitedByLava()));
		return out;
	}

	/** A small decoration: noOcclusion, popped off by a piston (as JugcraftAgriculture's decorations). */
	private static BlockBehaviour.Properties properties(MapColor color, SoundType sound, float strength) {
		return BlockBehaviour.Properties.of().mapColor(color).strength(strength).sound(sound).noOcclusion().pushReaction(PushReaction.POPPED);
	}
}

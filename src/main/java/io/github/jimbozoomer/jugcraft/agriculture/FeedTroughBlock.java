package io.github.jimbozoomer.jugcraft.agriculture;

import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/**
 * The Feed Trough (Barn & Harvest Tools, part 5; tools/barn_tools.py TROUGH): a plank trough on legs, two blocks long.
 * It keeps up to {@value #CAPACITY} of what horses eat ({@link #FEED}: hay bales, wheat, apples, carrots and the rest),
 * put in and taken back as the Apple Bin keeps fruit ({@link FruitBinBlock}, {@link FruitStore}), the hay in it rising
 * through {@link FruitBinBlock#STAGES} stages as it fills. Its feed is kept by its own block entity type (a
 * {@link FruitStoreBlockEntity}). Its model is tools/barn_tools_data.py trough().
 */
public class FeedTroughBlock extends FruitBinBlock {
	public static final int CAPACITY = 128;
	public static final TagKey<Item> FEED = TagKey.create(Registries.ITEM, Identifier.withDefaultNamespace("horse_food"));
	/** Its blocks: 2 across, 1 up, 1 deep (tools/barn_tools.py TROUGH["size"]). */
	private static final int[][] CELLS = box(2, 1, 1);
	public static final IntegerProperty PART = IntegerProperty.create("part", 0, CELLS.length - 1);
	private static final Map<Direction, VoxelShape[]> SHAPES = shapesOf(CELLS, new double[] {1.0, 0.0, 4.0, 31.0, 9.5, 12.0});

	public FeedTroughBlock(Properties properties) {
		super(properties);
	}

	@Override
	public int[][] cells() {
		return CELLS;
	}

	@Override
	public IntegerProperty partProperty() {
		return PART;
	}

	@Override
	public int capacity() {
		return CAPACITY;
	}

	@Override
	public TagKey<Item> takes() {
		return FEED;
	}

	@Override
	protected Map<Direction, VoxelShape[]> shapes() {
		return SHAPES;
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return part(state) == MASTER ? new FruitStoreBlockEntity(JugcraftAgriculture.FEED_TROUGH_ENTITY, pos, state) : null;
	}
}

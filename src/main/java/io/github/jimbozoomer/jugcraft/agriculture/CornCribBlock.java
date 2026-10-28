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
 * The Corn Crib (Barn & Harvest Tools, part 3; tools/barn_tools.py CRIB): a slatted crib on posts under a tin gable
 * roof, two blocks across, two up and one deep. It keeps up to {@value #CAPACITY} ears of corn ({@link #CORN}), put in
 * and taken back as the Apple Bin keeps fruit ({@link FruitBinBlock}, {@link FruitStore}), and the ears show through its
 * slats, rising through {@link FruitBinBlock#STAGES} stages as it fills. Its corn is kept by its own block entity type
 * (a {@link FruitStoreBlockEntity}). Its model is tools/barn_tools_data.py crib().
 */
public class CornCribBlock extends FruitBinBlock {
	public static final int CAPACITY = 256;
	public static final TagKey<Item> CORN = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "crops/corn"));
	/** Its blocks: 2 across, 2 up, 1 deep (tools/barn_tools.py CRIB["size"]). */
	private static final int[][] CELLS = box(2, 2, 1);
	public static final IntegerProperty PART = IntegerProperty.create("part", 0, CELLS.length - 1);
	private static final Map<Direction, VoxelShape[]> SHAPES = shapesOf(CELLS, new double[] {1.0, 0.0, 2.0, 31.0, 29.0, 14.0},
			new double[] {0.0, 29.0, 0.0, 32.0, 32.0, 16.0});

	public CornCribBlock(Properties properties) {
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
		return CORN;
	}

	@Override
	protected Map<Direction, VoxelShape[]> shapes() {
		return SHAPES;
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return part(state) == MASTER ? new FruitStoreBlockEntity(JugcraftAgriculture.CORN_CRIB_ENTITY, pos, state) : null;
	}
}

package io.github.jimbozoomer.jugcraft.agriculture;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealSource;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * The vine of a giant pumpkin, grown from Giant Pumpkin Seeds on farmland like a pumpkin stem but
 * {@link #GROWTH_TIME} times slower ({@link CropGrowth}). Full grown, it sets one small fruit on a free side
 * on ground fruit can lie on, and becomes an {@link AttachedGiantPumpkinVineBlock}; the fruit then grows
 * into a {@link GiantPumpkinBlock} as long as the vine holds it. Unlike a pumpkin stem it bears one fruit
 * at a time and nothing more until that is harvested.
 */
public class GiantPumpkinVineBlock extends VegetationBlock implements BonemealableBlock {
	public static final int MAX_AGE = 7;
	public static final float GROWTH_TIME = 1.5F;
	public static final IntegerProperty AGE = BlockStateProperties.AGE_7;
	private static final VoxelShape[] SHAPES = new VoxelShape[MAX_AGE + 1];

	static {
		for (int age = 0; age <= MAX_AGE; age++) {
			SHAPES[age] = Block.box(6.0, 0.0, 6.0, 10.0, 2.0 + age * 2.0, 10.0);
		}
	}

	public GiantPumpkinVineBlock(Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(AGE, 0));
	}

	@Override
	protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
		return state.is(BlockTags.SUPPORTS_STEM_CROPS);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPES[state.getValue(AGE)];
	}

	@Override
	protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		if (level.getRawBrightness(pos, 0) < CropGrowth.MIN_GROWTH_LIGHT
				|| random.nextInt(CropGrowth.chanceDivisor(CropGrowth.speed(level, pos, false), GROWTH_TIME)) != 0) {
			return;
		}
		int age = state.getValue(AGE);
		if (age < MAX_AGE) {
			level.setBlock(pos, state.setValue(AGE, age + 1), Block.UPDATE_CLIENTS);
		} else {
			growFruit(level, pos, Direction.Plane.HORIZONTAL.getRandomDirection(random));
		}
	}

	/** Sets a giant pumpkin's first fruit on {@code side} of a full-grown vine, if there is room. Returns whether it did. */
	public boolean growFruit(Level level, BlockPos pos, Direction side) {
		BlockState state = level.getBlockState(pos);
		BlockPos target = pos.relative(side);
		if (!state.is(this) || state.getValue(AGE) < MAX_AGE || !level.getBlockState(target).isAir()
				|| !level.getBlockState(target.below()).is(BlockTags.SUPPORTS_STEM_FRUIT)) {
			return false;
		}
		level.setBlockAndUpdate(target, JugcraftAgriculture.block("giant_pumpkin").defaultBlockState());
		if (level.getBlockEntity(target) instanceof GiantPumpkinBlockEntity fruit) {
			fruit.plant(target, side);
		}
		level.setBlockAndUpdate(pos, JugcraftAgriculture.block("attached_giant_pumpkin_vine").defaultBlockState()
				.setValue(HorizontalDirectionalBlock.FACING, side));
		return true;
	}

	@Override
	protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
		return new ItemStack(JugcraftAgriculture.item("giant_pumpkin_seeds"));
	}

	@Override
	public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state, BonemealSource source) {
		return state.getValue(AGE) < MAX_AGE;
	}

	@Override
	public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state, BonemealSource source) {
		return true;
	}

	@Override
	public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state, BonemealSource source) {
		int age = Math.min(MAX_AGE, state.getValue(AGE) + 1 + random.nextInt(3));
		level.setBlock(pos, state.setValue(AGE, age), Block.UPDATE_CLIENTS);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(AGE);
	}
}

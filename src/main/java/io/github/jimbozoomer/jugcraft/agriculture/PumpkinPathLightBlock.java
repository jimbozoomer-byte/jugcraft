package io.github.jimbozoomer.jugcraft.agriculture;

import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/**
 * The Pumpkin Path Light (Front-Yard Haunt Scenes, part 1; tools/front_yard.py PATH_LIGHT): a shepherd's hook of black
 * wrought iron 1.3 m high, one block across and two up ({@link MultiDecorationBlock}), a little jack-o'-lantern hanging
 * on a chain from its crook. An empty hand lights it or puts it out ({@link #LIT}, placed lit); lit, its lower block gives
 * light {@value #LIGHT} and its carved face glows. The lantern sways on its chain by {@link #sway} (client/
 * PumpkinPathLightRenderer, from the blockstate's {@link #RUNNER} models 1 and 2: the lantern and its glow), each hook in
 * its own time. Its model is tools/front_yard_data.py pumpkin_path_light().
 */
public class PumpkinPathLightBlock extends MultiDecorationBlock implements EntityBlock {
	public static final int LIGHT = 10;
	public static final float SWAY = 6.0F;
	public static final int PERIOD = 90;
	/** The crook's tip, where the chain hangs from (pixels: height and depth, facing north). */
	public static final float PIVOT_Y = 19.0F;
	public static final float PIVOT_Z = 3.5F;
	/** Its blocks: 1 across, 2 up, 1 deep (tools/front_yard.py PATH_LIGHT["size"]). */
	private static final int[][] CELLS = box(1, 2, 1);
	public static final IntegerProperty PART = IntegerProperty.create("part", 0, CELLS.length - 1);
	/** 0 for the hook; 1 its lantern and 2 its glow, for the renderer. */
	public static final IntegerProperty RUNNER = IntegerProperty.create("runner", 0, 2);
	/** The rod, the crook and the lantern. */
	private static final Map<Direction, VoxelShape[]> SHAPES = shapesOf(CELLS, new double[] {7.5, 0.0, 7.5, 8.5, 20.0, 8.5},
			new double[] {7.5, 19.0, 3.0, 8.5, 21.5, 8.5}, new double[] {5.75, 10.0, 1.5, 10.25, 15.75, 5.5});

	public PumpkinPathLightBlock(Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState().setValue(RUNNER, 0));
	}

	@Override
	public int[][] cells() {
		return CELLS;
	}

	@Override
	public IntegerProperty partProperty() {
		return PART;
	}

	/** The light it gives: from its lower block, where the lantern hangs, while it is lit. */
	public static int light(BlockState state) {
		return state.getValue(LIT) && state.getValue(PART) == MASTER ? LIGHT : 0;
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPES.get(state.getValue(FACING))[part(state)];
	}

	/**
	 * How far the lantern has swung on its chain at game time {@code time}, in degrees: to and fro along the path, up to
	 * {@value #SWAY} degrees once in {@value #PERIOD} ticks ({@code across} false), or a little from side to side, slower
	 * ({@code across} true), each hook in its own time ({@code seed}).
	 */
	public static float sway(float time, int seed, boolean across) {
		double phase = Math.floorMod(seed, PERIOD * 13);
		if (across) {
			return (float) (0.4 * SWAY * Math.sin(2.0 * Math.PI * (time + phase * 0.7) / (PERIOD * 1.3)));
		}
		return (float) (SWAY * Math.sin(2.0 * Math.PI * (time + phase) / PERIOD));
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return part(state) == MASTER ? new DecorationBlockEntity(JugcraftAgriculture.PUMPKIN_PATH_LIGHT_ENTITY, pos, state) : null;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(RUNNER);
	}
}

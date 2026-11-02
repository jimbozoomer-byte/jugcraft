package io.github.jimbozoomer.jugcraft.agriculture;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Mason-Jar Candles (Cozy Fall Hearth, part 5; tools/hearth.py JAR_CANDLE): vanilla's candle, one to four in a block, lit
 * and put out and added to as vanilla's are, but each a wax candle poured into a half-gallon mason jar with a twine bow
 * round its neck. {@link #LAYOUT} gives each jar's centre in pixels for each count; every jar is {@value #JAR_WIDTH}
 * pixels across and {@value #JAR_HEIGHT} high, its candle's flame standing at {@value #FLAME_Y} pixels, inside the jar.
 */
public class MasonJarCandleBlock extends CandleBlock {
	public static final double JAR_WIDTH = 1.75;
	public static final double JAR_HEIGHT = 3.5;
	public static final double FLAME_Y = 2.75;
	/** For one to four jars: each jar's {centre x, centre z} in pixels (tools/hearth.py JAR_CANDLE["layout"]). */
	public static final double[][][] LAYOUT = {
			{{8.0, 8.0}},
			{{6.75, 7.5}, {9.25, 8.75}},
			{{6.5, 6.75}, {9.5, 7.25}, {8.0, 9.75}},
			{{6.5, 6.5}, {9.5, 6.5}, {6.5, 9.5}, {9.5, 9.5}}};
	private static final List<List<Vec3>> FLAMES = new ArrayList<>();
	private static final VoxelShape[] SHAPES = new VoxelShape[LAYOUT.length];

	static {
		for (int i = 0; i < LAYOUT.length; i++) {
			List<Vec3> flames = new ArrayList<>();
			VoxelShape shape = Shapes.empty();
			double r = JAR_WIDTH / 2;
			for (double[] c : LAYOUT[i]) {
				flames.add(new Vec3(c[0] / 16, FLAME_Y / 16, c[1] / 16));
				shape = Shapes.or(shape, Block.box(c[0] - r, 0.0, c[1] - r, c[0] + r, JAR_HEIGHT, c[1] + r));
			}
			FLAMES.add(List.copyOf(flames));
			SHAPES[i] = shape.optimize();
		}
	}

	public MasonJarCandleBlock(Properties properties) {
		super(properties);
	}

	/** Where the flames of {@code count} jars stand in the block (fractions of a block). */
	public static List<Vec3> flames(int count) {
		return FLAMES.get(count - 1);
	}

	@Override
	protected Iterable<Vec3> getParticleOffsets(BlockState state) {
		return flames(state.getValue(CANDLES));
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPES[state.getValue(CANDLES) - 1];
	}
}

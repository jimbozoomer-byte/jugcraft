package io.github.jimbozoomer.jugcraft.agriculture;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The Beware Doormat (Front-Yard Haunt Scenes, part 3; tools/front_yard.py DOORMAT): a coir doormat 75 cm by 50 cm, a
 * black border round a skull and crossbones, read the right way up by whoever it faces. It lies on whatever is under it
 * and goes if that does. Now and then (one step in {@value #HEARTBEAT}) whoever treads on it hears a heart beating under
 * the boards. Its model is tools/front_yard_data.py beware_doormat().
 */
public class BewareDoormatBlock extends FacingDecorationBlock {
	public static final int HEARTBEAT = 8;
	/** Its mat, 12 pixels across, 8 deep and half a pixel thick, facing north. */
	private static final double[] BOX = {2.0, 0.0, 4.0, 14.0, 0.5, 12.0};

	public BewareDoormatBlock(Properties properties) {
		super(properties, BOX);
	}

	@Override
	protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		return !level.isEmptyBlock(pos.below());
	}

	@Override
	protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction direction,
			BlockPos neighborPos, BlockState neighbor, RandomSource random) {
		return direction == Direction.DOWN && !state.canSurvive(level, pos) ? Blocks.AIR.defaultBlockState() : state;
	}

	/** One step in {@value #HEARTBEAT} across it, a heart beats under the boards. */
	@Override
	public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
		if (level instanceof ServerLevel server && server.getRandom().nextInt(HEARTBEAT) == 0
				&& Math.abs(entity.getX() - entity.xOld) + Math.abs(entity.getZ() - entity.zOld) > 0.01) {
			server.playSound(null, pos, SoundEvents.WARDEN_HEARTBEAT, SoundSource.BLOCKS, 0.7F, 0.9F + server.getRandom().nextFloat() * 0.1F);
		}
		super.stepOn(level, pos, state, entity);
	}
}

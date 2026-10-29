package io.github.jimbozoomer.jugcraft.agriculture;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The Ghost Train's watch on its doorways ({@link GhostTrainBlock}, on its first block). Every
 * {@value GhostTrainBlock#LOOK_TICKS} ticks the server looks for a minecart in or by each doorway, and the block opens
 * and shuts that doorway's doors; this keeps when a cart was last seen at each, and (on every side, from the block's
 * events) when each doorway's doors last opened or shut and when its ghost last sprang up ({@link #marked}), for
 * client/GhostTrainRenderer. Nothing is saved: after a restart its doors shut until the next cart comes.
 */
public class GhostTrainBlockEntity extends DecorationBlockEntity {
	private final long[] seen = {Long.MIN_VALUE, Long.MIN_VALUE};
	private final long[] changed = {Long.MIN_VALUE, Long.MIN_VALUE};

	public GhostTrainBlockEntity(BlockPos pos, BlockState state) {
		super(JugcraftAgriculture.GHOST_TRAIN_ENTITY, pos, state);
	}

	/** A cart is at doorway {@code door} (0 the IN, 1 the OUT) at game time {@code time}. */
	public void seen(int door, long time) {
		seen[door] = time;
	}

	/** When a cart was last at doorway {@code door}, or {@link Long#MIN_VALUE}. */
	public long lastSeen(int door) {
		return seen[door];
	}

	/** Doorway {@code door}'s doors opened or shut at game time {@code time}. */
	public void changed(int door, long time) {
		changed[door] = time;
	}

	/** When doorway {@code door}'s doors last opened or shut, or {@link Long#MIN_VALUE}. */
	public long changedAt(int door) {
		return changed[door];
	}

	/** The server's look, from its ticker. */
	public void serverTick(ServerLevel level) {
		if (level.getGameTime() % GhostTrainBlock.LOOK_TICKS == 0 && getBlockState().getBlock() instanceof GhostTrainBlock train) {
			train.look(level, worldPosition, getBlockState(), this);
		}
	}
}

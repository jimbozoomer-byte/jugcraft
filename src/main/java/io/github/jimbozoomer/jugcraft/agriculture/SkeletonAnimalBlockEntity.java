package io.github.jimbozoomer.jugcraft.agriculture;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A skeleton animal's moving part ({@link SkeletonAnimalBlock}), on each player's client: whether they are near it and
 * since when (for the dog's wag), and where the crow's head is turning (its last snap, from where to where and when), for
 * client/SkeletonAnimalRenderer. It is each player's own sight of it, so nothing is sent or saved.
 */
public class SkeletonAnimalBlockEntity extends DecorationBlockEntity {
	private boolean near;
	private long changed = Long.MIN_VALUE;
	private long snap = Long.MIN_VALUE;
	private float from;
	private float to;
	private float snapped;

	public SkeletonAnimalBlockEntity(BlockPos pos, BlockState state) {
		super(JugcraftAgriculture.SKELETON_ANIMAL_ENTITY, pos, state);
	}

	/** The player is {@code near} it (or not) at game time {@code time}; returns since when they have been, or a long while. */
	public long look(boolean near, long time) {
		if (near != this.near) {
			this.near = near;
			changed = time;
		}
		return changed == Long.MIN_VALUE ? time - 1000L : changed;
	}

	/**
	 * The crow's head at game time {@code now}, in snap {@code snap}: at a new snap it turns from where it is to
	 * {@code target} ({@link SkeletonAnimalBlock#snapping}); the first time, it is already there.
	 */
	public float head(long snap, float target, float now) {
		if (snap != this.snap) {
			from = this.snap == Long.MIN_VALUE ? target : SkeletonAnimalBlock.snapping(from, to, now - snapped);
			to = target;
			snapped = now;
			this.snap = snap;
		}
		return SkeletonAnimalBlock.snapping(from, to, now - snapped);
	}
}

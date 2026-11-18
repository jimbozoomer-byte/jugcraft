package io.github.jimbozoomer.jugcraft.agriculture;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * The Tabletop Corn Toss Set's score for the round in play ({@link FestivalToyCornTossBlock}): rust's and cream's
 * points, saved with the world so a round survives a restart, and the game time of the last toss (not saved), for the
 * set's cooldown. It holds no items and gives nothing: the score is only told to the players nearby.
 */
public class FestivalToyCornTossBlockEntity extends BlockEntity {
	/** The most a side can score in a round: every bag in the hole. */
	public static final int MAX_SCORE = FestivalToyCornTossBlock.BAGS * FestivalToyCornTossBlock.HOLE_POINTS;
	private int rust;
	private int cream;
	private long lastToss = Long.MIN_VALUE;

	public FestivalToyCornTossBlockEntity(BlockPos pos, BlockState state) {
		super(FestivalToys.CORN_TOSS_ENTITY, pos, state);
	}

	public int rust() {
		return rust;
	}

	public int cream() {
		return cream;
	}

	/** Whether a toss may be made at game time {@code now}: none within the cooldown of the last. */
	public boolean ready(long now) {
		return lastToss == Long.MIN_VALUE || now - lastToss >= FestivalToyCornTossBlock.COOLDOWN || now < lastToss;
	}

	/** Records a toss at {@code now} by {@code side} ({@code rust} or not) for {@code points}. */
	public void scored(boolean rustSide, int points, long now) {
		if (rustSide) {
			rust = Math.min(MAX_SCORE, rust + points);
		} else {
			cream = Math.min(MAX_SCORE, cream + points);
		}
		lastToss = now;
		setChanged();
	}

	/** A new round: both scores back to nothing. */
	public void reset() {
		rust = 0;
		cream = 0;
		lastToss = Long.MIN_VALUE;
		setChanged();
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		rust = Math.clamp(input.getIntOr("rust", 0), 0, MAX_SCORE);
		cream = Math.clamp(input.getIntOr("cream", 0), 0, MAX_SCORE);
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putInt("rust", rust);
		output.putInt("cream", cream);
	}
}

package io.github.jimbozoomer.jugcraft.agriculture;

import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/**
 * A Whack-a-Zombie round ({@link WhackAZombieBlock}, on its first block): who paid for it, when it ends and which hole a
 * head last came up from (so the next comes up from another). Saved with the world, so a round survives a restart; the
 * heads and the score are the block's state. It marks the time a head came up, for client/WhackAZombieRenderer to raise it.
 */
public class WhackAZombieBlockEntity extends DecorationBlockEntity {
	private @Nullable UUID player;
	private long ends;
	private int last = -1;

	public WhackAZombieBlockEntity(BlockPos pos, BlockState state) {
		super(JugcraftAgriculture.WHACK_A_ZOMBIE_ENTITY, pos, state);
	}

	/** Starts a round for {@code payer}, ending at game time {@code ends}. */
	public void start(UUID payer, long ends) {
		this.player = payer;
		this.ends = ends;
		this.last = -1;
		setChanged();
	}

	/** Ends the round. */
	public void clear() {
		player = null;
		last = -1;
		setChanged();
	}

	/** Who paid for the round, or null when there is none. */
	public @Nullable UUID player() {
		return player;
	}

	/** The game time the round ends. */
	public long ends() {
		return ends;
	}

	/** The hole a head last came up from, or -1. */
	public int last() {
		return last;
	}

	/** A head came up from {@code hole}. */
	public void popped(int hole) {
		last = hole;
		setChanged();
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		player = input.read("player", UUIDUtil.CODEC).orElse(null);
		ends = input.getLongOr("ends", 0L);
		last = input.getIntOr("last", -1);
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		if (player != null) {
			output.store("player", UUIDUtil.CODEC, player);
		}
		output.putLong("ends", ends);
		output.putInt("last", last);
	}
}

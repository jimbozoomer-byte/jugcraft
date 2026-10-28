package io.github.jimbozoomer.jugcraft.agriculture;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The Haunted Mailbox's haunting, on its first block ({@link HauntedMailboxBlock}): on each player's client, whether they
 * are near it and since when, for client/HauntedMailboxRenderer to open its door and show its eyes. It is each player's own
 * sight of it, so nothing is sent or saved.
 */
public class HauntedMailboxBlockEntity extends DecorationBlockEntity {
	private boolean near;
	private long changed = Long.MIN_VALUE;

	public HauntedMailboxBlockEntity(BlockPos pos, BlockState state) {
		super(JugcraftAgriculture.HAUNTED_MAILBOX_ENTITY, pos, state);
	}

	/** The player is {@code near} it (or not) at game time {@code time}; returns since when they have been, or a long while. */
	public long look(boolean near, long time) {
		if (near != this.near) {
			this.near = near;
			changed = time;
		}
		return changed == Long.MIN_VALUE ? time - 1000L : changed;
	}

	public boolean near() {
		return near;
	}
}

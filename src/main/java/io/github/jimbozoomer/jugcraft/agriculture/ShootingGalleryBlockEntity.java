package io.github.jimbozoomer.jugcraft.agriculture;

import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/**
 * A Shooting Gallery round ({@link ShootingGalleryBlock}, on its first block): who paid for it, when it ends and which of
 * its targets are knocked down (a bit for each, rail by rail). Saved with the world and sent to clients, which draw the
 * knocked targets lying flat.
 */
public class ShootingGalleryBlockEntity extends BlockEntity {
	private @Nullable UUID player;
	private long ends;
	private int knocked;

	public ShootingGalleryBlockEntity(BlockPos pos, BlockState state) {
		super(JugcraftAgriculture.SHOOTING_GALLERY_ENTITY, pos, state);
	}

	/** Starts a round for {@code payer}, ending at game time {@code ends}, every target standing. */
	public void start(UUID payer, long ends) {
		this.player = payer;
		this.ends = ends;
		this.knocked = 0;
		changed();
	}

	/** Ends the round: every target stands again. */
	public void clear() {
		player = null;
		knocked = 0;
		changed();
	}

	public @Nullable UUID player() {
		return player;
	}

	public long ends() {
		return ends;
	}

	/** Whether target {@code target} (rail by rail, from 0) is down. */
	public boolean isDown(int target) {
		return (knocked & (1 << target)) != 0;
	}

	/** How many targets are down. */
	public int down() {
		return Integer.bitCount(knocked);
	}

	/** Knocks target {@code target} down; returns whether it was standing. */
	public boolean knock(int target) {
		if (isDown(target)) {
			return false;
		}
		knocked |= 1 << target;
		changed();
		return true;
	}

	private void changed() {
		setChanged();
		if (level != null && !level.isClientSide()) {
			BlockState state = getBlockState();
			level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_CLIENTS);
		}
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		player = input.read("player", UUIDUtil.CODEC).orElse(null);
		ends = input.getLongOr("ends", 0L);
		knocked = input.getIntOr("knocked", 0) & ((1 << ShootingGalleryBlock.TARGETS) - 1);
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		if (player != null) {
			output.store("player", UUIDUtil.CODEC, player);
		}
		output.putLong("ends", ends);
		output.putInt("knocked", knocked);
	}

	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		return saveCustomOnly(registries);
	}
}

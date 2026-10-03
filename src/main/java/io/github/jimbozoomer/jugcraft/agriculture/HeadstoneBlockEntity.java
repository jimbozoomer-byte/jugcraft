package io.github.jimbozoomer.jugcraft.agriculture;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * A headstone's {@link Epitaph}, on the block that holds it (part 0 of a {@link HeadstoneBlock}). It goes with the item
 * when the headstone is broken (data component {@code jugcraft:epitaph}) and comes back when it is placed again; a
 * headstone renamed in an anvil is placed with its name as the first line. Clients get it to draw.
 */
public class HeadstoneBlockEntity extends BlockEntity {
	private Epitaph epitaph = Epitaph.BLANK;

	public HeadstoneBlockEntity(BlockPos pos, BlockState state) {
		super(JugcraftAgriculture.HEADSTONE_ENTITY, pos, state);
	}

	public Epitaph epitaph() {
		return epitaph;
	}

	/** Cuts {@code cut} into the stone, replacing what was there, and sends it to clients. */
	public void engrave(Epitaph cut) {
		epitaph = cut;
		setChanged();
		if (level != null) {
			BlockState state = getBlockState();
			level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_CLIENTS);
		}
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		epitaph = input.read("epitaph", Epitaph.CODEC).orElse(Epitaph.BLANK);
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		if (!epitaph.isBlank()) {
			output.store("epitaph", Epitaph.CODEC, epitaph);
		}
	}

	@Override
	protected void applyImplicitComponents(DataComponentGetter components) {
		super.applyImplicitComponents(components);
		Epitaph carried = components.get(JugcraftAgriculture.EPITAPH);
		Component name = components.get(DataComponents.CUSTOM_NAME);
		if (carried != null) {
			epitaph = carried;
		} else if (name != null) {
			epitaph = Epitaph.BLANK.withFirstLine(name.getString());
		}
	}

	@Override
	protected void collectImplicitComponents(DataComponentMap.Builder components) {
		super.collectImplicitComponents(components);
		if (!epitaph.isBlank()) {
			components.set(JugcraftAgriculture.EPITAPH, epitaph);
		}
	}

	@Override
	public void removeComponentsFromTag(ValueOutput output) {
		super.removeComponentsFromTag(output);
		output.discard("epitaph");
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

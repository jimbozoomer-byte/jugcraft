package io.github.jimbozoomer.jugcraft.tools;

import io.github.jimbozoomer.jugcraft.energy.SimpleEnergyStorage;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.*;

/** Two persistent charging bays. Legacy inventory slot zero keeps its saved identity. */
public class ChargingStationBlockEntity extends BlockEntity implements Container, MenuProvider {
    public static final int SLOTS = 2;
    public static final long CAPACITY = 100_000, INPUT = 1_024, CHARGE_RATE = 512;
    private static final int SYNC_INTERVAL = 20;
    final SimpleEnergyStorage energy = new SimpleEnergyStorage(CAPACITY, INPUT, 0, this::setChanged);
    private final NonNullList<ItemStack> items = NonNullList.withSize(SLOTS, ItemStack.EMPTY);

    public ChargingStationBlockEntity(BlockPos pos, BlockState state) { super(JugcraftTools.CHARGING_STATION_ENTITY, pos, state); }
    public SimpleEnergyStorage energy() { return energy; }
    public ItemStack tool() { return tool(0); }
    public ItemStack tool(int slot) { return items.get(slot); }
    public void setTool(ItemStack stack) { setTool(0, stack); }
    public void setTool(int slot, ItemStack stack) { setItem(slot, stack); }

    void serverTick(ServerLevel level, BlockPos pos, BlockState state) {
        long budget = Math.min(CHARGE_RATE, energy.getAmount()), given = 0;
        int first = (int) (level.getGameTime() & 1);
        // A fair first pass, then reuse any share a full/empty bay did not need.
        for (int pass = 0; pass < 2 && given < budget; pass++) for (int n = 0; n < SLOTS && given < budget; n++) {
            long request = pass == 0 ? (budget - given) / (SLOTS - n) : budget - given;
            given += Chargeable.charge(items.get((first + n) % SLOTS), request);
        }
        if (given > 0) {
            energy.setAmount(energy.getAmount() - given);
            setChanged();
            if (level.getGameTime() % SYNC_INTERVAL == 0) sync();
        }
        boolean charging = given > 0;
        if (state.getValue(ChargingStationBlock.LIT) != charging) {
            level.setBlock(pos, state.setValue(ChargingStationBlock.LIT, charging), Block.UPDATE_CLIENTS);
            var upper = level.getBlockState(pos.above());
            if (upper.is(JugcraftTools.CHARGING_STATION)) level.setBlock(pos.above(), upper.setValue(ChargingStationBlock.LIT, charging), Block.UPDATE_CLIENTS);
        }
    }

    private void sync() {
        setChanged();
        if (level != null && !isRemoved()) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
    }
    @Override public int getContainerSize() { return SLOTS; }
    @Override public int getMaxStackSize() { return 1; }
    @Override public boolean isEmpty() { return items.stream().allMatch(ItemStack::isEmpty); }
    @Override public ItemStack getItem(int slot) { return items.get(slot); }
    @Override public ItemStack removeItem(int slot, int count) {
        var result = ContainerHelper.removeItem(items, slot, count);
        if (!result.isEmpty()) sync();
        return result;
    }
    @Override public ItemStack removeItemNoUpdate(int slot) { return ContainerHelper.takeItem(items, slot); }
    @Override public void setItem(int slot, ItemStack stack) { items.set(slot, stack); stack.limitSize(1); sync(); }
    @Override public void clearContent() { items.clear(); sync(); }
    @Override public boolean canPlaceItem(int slot, ItemStack stack) { return stack.getItem() instanceof Chargeable c && c.baseCapacity() > 0; }
    @Override public boolean stillValid(Player player) {
        return !isRemoved() && player.isAlive() && !player.isSpectator() && player.level() == level
            && level.getBlockEntity(worldPosition) == this && player.distanceToSqr(worldPosition.getX()+.5, worldPosition.getY()+.5, worldPosition.getZ()+.5) <= 64
            && level instanceof ServerLevel server && player.mayBuild()
            && io.github.jimbozoomer.jugcraft.concordance.Authority.mayChange(server, player, worldPosition)
            && io.github.jimbozoomer.jugcraft.concordance.Authority.mayChange(server, player, worldPosition.above());
    }
    @Override public Component getDisplayName() { return Component.translatable("block.jugcraft.charging_station"); }
    @Override public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) { return new ChargingStationMenu(id, inventory, this); }
    @Override public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (level != null && !level.isClientSide()) for (int i=0; i<SLOTS; i++) Block.popResource(level, pos, ContainerHelper.takeItem(items,i));
        items.clear();
    }
    @Override public Packet<ClientGamePacketListener> getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider registries) { return saveWithoutMetadata(registries); }
    @Override protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input); energy.setAmount(input.getLong("energy").orElse(0L));
        items.clear(); ContainerHelper.loadAllItems(input, items);
    }
    @Override protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output); output.putLong("energy", energy.getAmount()); ContainerHelper.saveAllItems(output, items);
    }
}

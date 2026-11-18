package io.github.jimbozoomer.jugcraft.agriculture;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** Eight physical piles around the tree. Each slot holds the exact stack wrapped by a player. */
public final class FirGifts extends BlockEntity {
    public static final int PILES = 8;
    private final NonNullList<ItemStack> slots = NonNullList.withSize(PILES * 2, ItemStack.EMPTY);

    public FirGifts(BlockPos pos, BlockState state) {
        super(HarbourGiftFir.GIFT_ENTITY, pos, state);
    }

    public boolean hasGifts() { return slots.stream().anyMatch(stack -> !stack.isEmpty()); }

    public int count(int pile) {
        return (slots.get(pile * 2).isEmpty() ? 0 : 1) + (slots.get(pile * 2 + 1).isEmpty() ? 0 : 1);
    }

    /** A trunk click looks for the least full pile when giving or the fullest when taking. */
    private int select(int requested, boolean giving) {
        if (requested >= PILES || requested < -1) return -1;
        if (requested >= 0) return requested;
        int best = 0;
        for (int i = 1; i < PILES; i++) {
            if (giving ? count(i) < count(best) : count(i) > count(best)) best = i;
        }
        return best;
    }

    public boolean deposit(ItemStack offered, int requested) {
        if (offered.isEmpty()) return false;
        int pile = select(requested, true);
        if (pile < 0) return false;
        for (int slot = pile * 2; slot <= pile * 2 + 1; slot++) {
            if (slots.get(slot).isEmpty()) {
                slots.set(slot, offered.split(offered.getCount()));
                changed(pile);
                return true;
            }
        }
        return false;
    }

    public ItemStack collect(int requested) {
        int pile = select(requested, false);
        if (pile < 0) return ItemStack.EMPTY;
        for (int slot = pile * 2 + 1; slot >= pile * 2; slot--) {
            if (!slots.get(slot).isEmpty()) {
                ItemStack gift = slots.set(slot, ItemStack.EMPTY);
                changed(pile);
                return gift;
            }
        }
        return ItemStack.EMPTY;
    }

    private void changed(int pile) {
        setChanged();
        if (level == null || level.isClientSide()) return;
        var facing = getBlockState().getValue(MultiDecorationBlock.FACING);
        var fir = (HarbourGiftFir) getBlockState().getBlock();
        var at = fir.partPos(worldPosition, facing, pile + 1);
        BlockState state = level.getBlockState(at);
        if (state.is(fir) && fir.part(state) == pile + 1
            && state.getValue(MultiDecorationBlock.FACING) == facing)
            level.setBlock(at, state.setValue(HarbourGiftFir.PARCELS, count(pile)), Block.UPDATE_CLIENTS);
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState oldState) {
        super.preRemoveSideEffects(pos, oldState);
        if (level == null || level.isClientSide()) return;
        Containers.dropContents(level, pos, slots);
        slots.clear();
    }

    @Override protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, slots);
    }

    @Override protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        slots.clear();
        ContainerHelper.loadAllItems(input, slots);
    }
}

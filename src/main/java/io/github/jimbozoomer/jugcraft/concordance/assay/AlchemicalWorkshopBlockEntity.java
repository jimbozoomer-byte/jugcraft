package io.github.jimbozoomer.jugcraft.concordance.assay;

import io.github.jimbozoomer.jugcraft.concordance.Authority;
import io.github.jimbozoomer.jugcraft.concordance.Saved;
import io.github.jimbozoomer.jugcraft.concordance.equivalence.Assay;
import io.github.jimbozoomer.jugcraft.concordance.equivalence.Material;
import io.github.jimbozoomer.jugcraft.party.JugcraftParties;
import java.util.UUID;
import net.minecraft.core.*;
import net.minecraft.core.component.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.*;
import org.jspecify.annotations.Nullable;

/** One staggered pulse per second, only loaded neighbours, bounded inventory and conserved transfers. */
public final class AlchemicalWorkshopBlockEntity extends BlockEntity implements WorldlyContainer {
    public static final int STAR = 0, SAMPLE = 1, INPUT_FIRST = 2, INPUT_END = 5, OUTPUT_FIRST = 5, SLOTS = 11;
    private static final int[] INPUTS = {2, 3, 4}, OUTPUTS = {5, 6, 7, 8, 9, 10}, NONE = {};
    public static final int IDLE = 0, PAUSED = 1, OWNER_AWAY = 2, DENIED = 3, RESEARCH = 4, DISABLED = 5,
            UNBALANCED = 6, NO_SUN = 7, FULL = 8, COLLECTING = 9, NO_SAMPLE = 10, INVALID_SAMPLE = 11,
            OUTPUT_FULL = 12, WAITING = 13, FORMING = 14, TOO_COSTLY = 15;
    private NonNullList<ItemStack> items = NonNullList.withSize(SLOTS, ItemStack.EMPTY);
    private @Nullable UUID owner;
    private int essence, status, sunlight;
    private boolean paused;

    public AlchemicalWorkshopBlockEntity(BlockPos pos, BlockState state) { super(AlchemicalWorkshops.ENTITY, pos, state); }
    public boolean collector() { return ((AlchemicalWorkshopBlock)getBlockState().getBlock()).collector; }
    public int tier() { return ((AlchemicalWorkshopBlock)getBlockState().getBlock()).tier; }
    public int capacity() { return (collector() ? AlchemicalWorkshops.COLLECTOR_CAPACITY : AlchemicalWorkshops.CONDENSER_CAPACITY)[tier()-1]; }
    public int rate() { return collector() ? AlchemicalWorkshops.COLLECTOR_RATE[tier()-1] : tier() == 1 ? 1 : 8; }
    public int transferRate() { return collector() ? 64 << (2*(tier()-1)) : tier() == 1 ? 4096 : 32768; }
    public int essence() { return essence; }
    public int status() { return status; }
    public int sunlight() { return sunlight; }
    public boolean paused() { return paused; }
    public @Nullable UUID owner() { return owner; }
    public void setEssence(int value) { essence = Math.clamp(value, 0, capacity()); setChanged(); }
    public void setPaused(boolean value) {
        paused = value; status = value ? PAUSED : IDLE;
        if (level instanceof ServerLevel server && getBlockState().getValue(AlchemicalWorkshopBlock.ACTIVE))
            server.setBlock(worldPosition, getBlockState().setValue(AlchemicalWorkshopBlock.ACTIVE, false), Block.UPDATE_CLIENTS);
        setChanged();
    }
    public void placedBy(ServerPlayer player) { owner = player.getUUID(); setChanged(); }
    private boolean related(UUID by) { return owner != null && (owner.equals(by) || JugcraftParties.sameParty(owner, by)); }
    public boolean mayUse(ServerPlayer player) {
        return related(player.getUUID()) && !player.isSpectator() && player.isAlive()
                && level instanceof ServerLevel server && Authority.mayChange(server, player, worldPosition);
    }
    @Override public boolean stillValid(Player player) {
        return level != null && level.getBlockEntity(worldPosition) == this && player.distanceToSqr(net.minecraft.world.phys.Vec3.atCenterOf(worldPosition)) <= 64
                && (!(player instanceof ServerPlayer server) || mayUse(server));
    }
    public static @Nullable Material material(ItemStack stack) {
        return stack.isEmpty() || !Assaying.eligibility(stack).isEmpty() ? null
                : Assaying.catalog().material(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
    }
    public long cost() { Material m = material(items.get(SAMPLE)); return m == null || !m.formable() ? 0 : Assay.form(m, 1); }
    private int outputSlot() {
        ItemStack sample = items.get(SAMPLE);
        for (int i = OUTPUT_FIRST; i < SLOTS; i++) {
            ItemStack out = items.get(i);
            if (!out.isEmpty() && ItemStack.isSameItemSameComponents(out, sample) && out.getCount() < out.getMaxStackSize()) return i;
        }
        for (int i = OUTPUT_FIRST; i < SLOTS; i++) if (items.get(i).isEmpty()) return i;
        return -1;
    }
    /** Recomputed before any intake. A blocked output or invalid sample consumes nothing. */
    private int targetStatus() {
        if (items.get(SAMPLE).isEmpty()) return NO_SAMPLE;
        long cost = cost();
        if (cost <= 0) return INVALID_SAMPLE;
        if (cost > capacity()) return TOO_COSTLY;
        if (outputSlot() < 0) return OUTPUT_FULL;
        return WAITING;
    }
    private int gate(@Nullable ServerPlayer player) {
        if (!Assaying.enabled()) return DISABLED;
        if (paused || (level != null && level.hasNeighborSignal(worldPosition))) return PAUSED;
        if (player == null || owner == null || !owner.equals(player.getUUID()) || !player.isAlive() || player.isSpectator()) return OWNER_AWAY;
        if (!Assaying.knows(player)) return RESEARCH;
        if (!(level instanceof ServerLevel server) || !Assaying.problems(server).isEmpty()) return UNBALANCED;
        return Authority.mayChange(server, player, worldPosition) ? IDLE : DENIED;
    }
    public static void tick(Level level, BlockPos pos, BlockState state, AlchemicalWorkshopBlockEntity be) {
        if (level instanceof ServerLevel server && Math.floorMod(level.getGameTime() + pos.asLong(), 20) == 0)
            be.pulse(Authority.present(server, be.owner));
    }
    /** Actual server pulse; public for deterministic server tests, never called from a client request. */
    public void pulse(@Nullable ServerPlayer player) {
        if (!(level instanceof ServerLevel server)) return;
        status = gate(player); boolean worked = false;
        if (status == IDLE) {
            if (collector()) {
                sunlight = server.dimensionType().hasSkyLight() && server.canSeeSky(worldPosition.above()) ? Math.clamp(server.getBrightness(LightLayer.SKY, worldPosition.above()) - server.getSkyDarken(), 0, 15) : 0;
                int made = sunlight >= 12 ? Math.min(rate(), capacity() - essence) : 0;
                essence += made; worked = made > 0;
                // Adjacent condensers first, then a docked star. All transfers debit exactly what arrived.
                int budget = Math.min(essence, transferRate());
                for (Direction direction : Direction.values()) {
                    BlockPos to = worldPosition.relative(direction);
                    if (budget <= 0 || !server.hasChunkAt(to)) continue;
                    if (server.getBlockEntity(to) instanceof AlchemicalWorkshopBlockEntity other && !other.collector()
                            && other.related(owner) && other.gate(owner.equals(other.owner) ? player : Authority.present(server, other.owner)) == IDLE
                            && other.targetStatus() == WAITING && Authority.mayChange(server, player, to)) {
                        int moved = Math.min(budget, other.capacity() - other.essence);
                        other.setEssence(other.essence + moved); essence -= moved; budget -= moved; worked |= moved > 0;
                    }
                }
                ItemStack star = items.get(STAR);
                int moved = Math.min(budget, KleinStars.capacity(star) - KleinStars.stored(star));
                if (moved > 0) { KleinStars.setStored(star, KleinStars.stored(star) + moved); essence -= moved; worked = true; }
                status = worked ? COLLECTING : essence == capacity() ? FULL : sunlight < 12 ? NO_SUN : IDLE;
            } else {
                status = targetStatus();
                if (status == WAITING) {
                    ItemStack star = items.get(STAR);
                    int moved = Math.min(Math.min(KleinStars.stored(star), capacity() - essence), transferRate());
                    if (moved > 0) { KleinStars.setStored(star, KleinStars.stored(star) - moved); essence += moved; }
                    // Deliberate fuel slots only: never dissolve the sample, star, outputs or a player's inventory.
                    int budget = tier() == 1 ? 8 : 64;
                    for (int i = INPUT_FIRST; i < INPUT_END && budget > 0; i++) {
                        ItemStack input = items.get(i); Material m = material(input);
                        if (m == null || !m.dissolvable()) continue;
                        int n = Math.min(budget, input.getCount());
                        while (n > 0 && Assay.dissolve(m, n) > capacity() - essence) n--;
                        long gain = Assay.dissolve(m, n);
                        if (gain <= 0) continue;
                        input.shrink(n); budget -= n; essence += (int)gain;
                    }
                    long cost = cost();
                    for (int i = 0; i < rate() && essence >= cost; i++) {
                        int slot = outputSlot(); if (slot < 0) break;
                        ItemStack out = items.get(slot);
                        if (out.isEmpty()) items.set(slot, new ItemStack(items.get(SAMPLE).getItem())); else out.grow(1);
                        essence -= (int)cost; worked = true;
                    }
                    status = worked ? FORMING : WAITING;
                }
            }
        }
        boolean wasActive = getBlockState().getValue(AlchemicalWorkshopBlock.ACTIVE);
        if (wasActive != worked) server.setBlock(worldPosition, getBlockState().setValue(AlchemicalWorkshopBlock.ACTIVE, worked), Block.UPDATE_CLIENTS);
        setChanged();
    }

    // Hoppers cannot touch the star or retained sample. Native sided-container adapters also respect these checks.
    private boolean automationAllowed(@Nullable Direction side) {
        if (!(level instanceof ServerLevel server) || collector()) return false;
        ServerPlayer player = Authority.present(server, owner);
        if (gate(player) != IDLE) return false;
        BlockPos neighbour = side == null ? worldPosition : worldPosition.relative(side);
        return server.hasChunkAt(neighbour) && Authority.mayChange(server, player, neighbour);
    }
    @Override public int[] getSlotsForFace(Direction side) { return collector() ? NONE : side == Direction.DOWN ? OUTPUTS : INPUTS; }
    @Override public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction side) {
        return slot >= INPUT_FIRST && slot < INPUT_END && side != Direction.DOWN && automationAllowed(side) && canPlaceItem(slot, stack);
    }
    @Override public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        return slot >= OUTPUT_FIRST && side == Direction.DOWN && automationAllowed(side);
    }
    @Override public boolean canPlaceItem(int slot, ItemStack stack) {
        if (slot == STAR) return KleinStars.capacity(stack) > 0;
        if (collector()) return false;
        Material m = material(stack);
        return slot == SAMPLE ? m != null && m.formable() : slot >= INPUT_FIRST && slot < INPUT_END && m != null && m.dissolvable();
    }
    @Override public int getContainerSize() { return collector() ? 1 : SLOTS; }
    @Override public boolean isEmpty() { return items.stream().allMatch(ItemStack::isEmpty); }
    @Override public ItemStack getItem(int slot) { return items.get(slot); }
    @Override public ItemStack removeItem(int slot, int count) { ItemStack s = ContainerHelper.removeItem(items, slot, count); if (!s.isEmpty()) setChanged(); return s; }
    @Override public ItemStack removeItemNoUpdate(int slot) { return ContainerHelper.takeItem(items, slot); }
    @Override public void setItem(int slot, ItemStack stack) { items.set(slot, stack); setChanged(); }
    @Override public void clearContent() { items.clear(); setChanged(); }
    @Override public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (level instanceof ServerLevel server) { Containers.dropContents(server, pos, this); clearContent(); }
    }
    @Override protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input); items = NonNullList.withSize(SLOTS, ItemStack.EMPTY); ContainerHelper.loadAllItems(input, items);
        owner = input.read("owner", UUIDUtil.CODEC).orElse(null);
        essence = Math.clamp(input.getIntOr("essence", 0), 0, capacity()); paused = input.getBooleanOr("paused", false);
    }
    @Override protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output); Saved.stamp(output, 1); ContainerHelper.saveAllItems(output, items);
        if (owner != null) output.store("owner", UUIDUtil.CODEC, owner);
        output.putInt("essence", essence); output.putBoolean("paused", paused);
    }
    @Override protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components); components.set(AlchemicalWorkshops.ESSENCE, essence);
    }
    @Override protected void applyImplicitComponents(DataComponentGetter components) {
        super.applyImplicitComponents(components); essence = Math.clamp(components.getOrDefault(AlchemicalWorkshops.ESSENCE, 0), 0, capacity());
    }
}

package io.github.jimbozoomer.jugcraft.nouveau;

import com.geckolib.animation.RawAnimation;
import io.github.jimbozoomer.jugcraft.agriculture.ScarecrowBlock;
import io.github.jimbozoomer.jugcraft.concordance.Authority;
import io.github.jimbozoomer.jugcraft.concordance.RateGate;
import io.github.jimbozoomer.jugcraft.concordance.spirits.ClockworkPorterEntity.Route;
import io.github.jimbozoomer.jugcraft.concordance.spirits.WorkerEntity;
import io.github.jimbozoomer.jugcraft.concordance.spirits.WorkerRoster;
import io.github.jimbozoomer.jugcraft.concordance.worker.Status;
import io.github.jimbozoomer.jugcraft.party.JugcraftParties;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.*;
import org.jspecify.annotations.Nullable;

/** A real walking courier. Cargo is removed at pickup, saved once on this entity and deposited only at arrival. */
public final class Starbuncle extends WorkerEntity<Starbuncle> {
    public static final int RANGE = 32, LOAD = 16, MAX_PER_OWNER = 8;
    public static final String[] COLORS = {"white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray",
        "light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black"};
    private static final EntityDataAccessor<Integer> COLOR = SynchedEntityData.defineId(Starbuncle.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<ItemStack> CARGO_DISPLAY = SynchedEntityData.defineId(Starbuncle.class, EntityDataSerializers.ITEM_STACK);
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle"), RUN = RawAnimation.begin().thenLoop("run");
    private @Nullable Route route;
    private ItemStack cargo = ItemStack.EMPTY;
    public Starbuncle(EntityType<? extends PathfinderMob> type, Level level) { super(type, level); }
    public static AttributeSupplier.Builder attributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 16).add(Attributes.MOVEMENT_SPEED, .3).add(Attributes.FOLLOW_RANGE, 32);
    }
    @Override public String kind() { return "starbuncle"; }
    @Override protected void defineSynchedData(SynchedEntityData.Builder b) { super.defineSynchedData(b); b.define(COLOR, 4); b.define(CARGO_DISPLAY, ItemStack.EMPTY); }
    public int color() { return Math.clamp(entityData.get(COLOR), 0, COLORS.length - 1); }
    public void color(int value) { entityData.set(COLOR, Math.clamp(value, 0, COLORS.length - 1)); }
    public @Nullable Route route() { return route; }
    public ItemStack cargo() { return cargo.copy(); }
    /** Display-only copy. Server transfers never read this synced item. */
    public ItemStack cargoDisplay() { return entityData.get(CARGO_DISPLAY).copy(); }
    private void syncCargo() { entityData.set(CARGO_DISPLAY, cargo.copy()); }
    public boolean mayServe(Player player) {
        return owner != null && (owner.equals(player.getUUID()) || JugcraftParties.sameParty(owner, player.getUUID()))
            && player.isAlive() && !player.isSpectator() && player.level() == level();
    }
    private static boolean valid(Route r, ServerLevel level) {
        return r.dimension().equals(level.dimension().identifier().toString()) && !r.source().equals(r.target())
            && r.source().distSqr(r.target()) <= RANGE * RANGE && level.isInWorldBounds(r.source()) && level.isInWorldBounds(r.target());
    }
    /** Refuse rerouting in flight; callers cannot abandon or duplicate an undelivered stack. */
    public boolean assign(ServerPlayer player, Route next) {
        if (!(level() instanceof ServerLevel level) || !JugNouveau.enabled() || !mayServe(player) || !cargo.isEmpty()
            || !valid(next, level) || distanceToSqr(player) > RANGE * RANGE
            || NouveauStorage.accessible(level, player, next.source()) == null
            || NouveauStorage.accessible(level, player, next.target()) == null) return false;
        route = next; arrived(); setStatus(Status.IDLE); return true;
    }
    @Override public Status think(ServerLevel level, long time, long gameTime) {
        Status result = decide(level, gameTime);
        syncCargo();
        if (result != Status.TRAVELLING) stop();
        setStatus(result); return result;
    }
    private Status decide(ServerLevel level, long now) {
        if (!JugNouveau.enabled()) return Status.DISABLED;
        ServerPlayer actor = Authority.answering(level, owner);
        if (actor == null) return Status.OWNER_OFFLINE;
        if (route == null) return Status.IDLE;
        if (!valid(route, level)) return Status.OTHER_DIMENSION;
        BlockPos target = cargo.isEmpty() ? route.source() : route.target();
        if (!level.isLoaded(target)) return Status.DESTINATION_UNLOADED;
        Container chest = NouveauStorage.accessible(level, actor, target);
        if (chest == null) return Status.BLOCKED_BY_ACCESS;
        if (!near(centre(target), 2)) return walkTo(centre(target), 1.0F, 1, now) ? Status.TRAVELLING : Status.CANNOT_NAVIGATE;
        arrived();
        if (cargo.isEmpty()) {
            // Only the linked 27-slot chest half/barrel is searched, once per decision.
            for (int slot = 0; slot < chest.getContainerSize(); slot++) {
                ItemStack stack = chest.getItem(slot);
                if (stack.isEmpty()) continue;
                cargo = chest.removeItem(slot, Math.min(LOAD, stack.getCount()));
                chest.setChanged(); return Status.WORKING;
            }
            return Status.WAITING_FOR_RESOURCES;
        }
        for (int slot = 0; slot < chest.getContainerSize() && !cargo.isEmpty(); slot++) {
            if (!chest.canPlaceItem(slot, cargo)) continue;
            ItemStack in = chest.getItem(slot);
            int limit = Math.min(chest.getMaxStackSize(), cargo.getMaxStackSize());
            if (in.isEmpty()) chest.setItem(slot, cargo.split(Math.min(limit, cargo.getCount())));
            else if (ItemStack.isSameItemSameComponents(in, cargo) && in.getCount() < limit) {
                int count = Math.min(limit - in.getCount(), cargo.getCount()); in.grow(count); cargo.shrink(count);
            }
        }
        chest.setChanged();
        return cargo.isEmpty() ? Status.FINISHED : Status.FULL;
    }
    @Override protected RawAnimation animation(Status status) { return status == Status.TRAVELLING ? RUN : IDLE; }
    @Override protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        var held = player.getItemInHand(hand);
        if (held.is(JugNouveau.WAND)) return held.getItem().interactLivingEntity(held, player, this, hand);
        if (!(player instanceof ServerPlayer server)) return InteractionResult.SUCCESS;
        if (!mayServe(player) || !Authority.mayChange(server.level(), player, blockPosition())) return InteractionResult.FAIL;
        DyeColor dye = ScarecrowBlock.dyeColor(held);
        if (dye != null && RateGate.allow(server, "starbuncle", 4)) {
            int index = java.util.Arrays.asList(COLORS).indexOf(dye.getName());
            if (index >= 0 && color() != index) { color(index); held.consume(1, server); }
            return InteractionResult.SUCCESS;
        }
        if (held.isEmpty() && player.isShiftKeyDown()) {
            if (!RateGate.allow(server, "starbuncle", 4)) return InteractionResult.FAIL;
            // Destruction returns cargo and a color-preserving charm exactly once.
            discard(); return InteractionResult.SUCCESS;
        }
        server.sendOverlayMessage(Component.translatable("message.jugcraft.starbuncle.status", describe(), cargo.getCount(),
            route == null ? "—" : route.source().toShortString(), route == null ? "—" : route.target().toShortString()));
        return InteractionResult.SUCCESS;
    }
    @Override public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (source.getEntity() instanceof Player p && (!mayServe(p) || !Authority.mayChange(level, p, blockPosition()))) return false;
        return super.hurtServer(level, source, amount);
    }
    @Override public void remove(RemovalReason reason) {
        if (reason.shouldDestroy() && !isRemoved() && level() instanceof ServerLevel level) {
            if (!cargo.isEmpty()) { spawnAtLocation(level, cargo); cargo = ItemStack.EMPTY; }
            if (owner != null) {
                var charm = new ItemStack(JugNouveau.STARBUNCLE_CHARM); charm.set(JugNouveau.CHARM_COLOR, color());
                spawnAtLocation(level, charm); WorkerRoster.of(level.getServer()).remove(owner, getUUID()); owner = null;
            }
        }
        super.remove(reason);
    }
    @Override protected void addAdditionalSaveData(ValueOutput out) {
        super.addAdditionalSaveData(out); out.putInt("color", color());
        if (route != null) out.store("route", Route.CODEC, route);
        out.store("cargo", ItemStack.OPTIONAL_CODEC, cargo);
    }
    @Override protected void readAdditionalSaveData(ValueInput in) {
        super.readAdditionalSaveData(in); color(in.getIntOr("color", 4)); route = in.read("route", Route.CODEC).orElse(null);
        cargo = in.read("cargo", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        syncCargo();
    }
}

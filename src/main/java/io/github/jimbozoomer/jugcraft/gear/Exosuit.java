package io.github.jimbozoomer.jugcraft.gear;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.tools.Chargeable;
import io.github.jimbozoomer.jugcraft.tools.EquipmentSettings;
import java.util.Map;
import java.util.WeakHashMap;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.phys.Vec3;

/** Independent, use-billed exosuit powers. Paid shield charge lives on the chestplate. */
public final class Exosuit {
    public static final long CAPACITY = 500_000;
    public static final long NIGHT_VISION_PER_TICK = 2;
    public static final int SHIELD_POINTS = 8;
    public static final int SHIELD_INTERVAL = 10;
    public static final int SHIELD_DELAY = 60;
    public static final long SHIELD_PER_POINT = 1_000;
    public static final long SPEED_PER_TICK = 1;
    public static final double SPEED_BONUS = 0.3;
    /** A damage point is half a heart: 100 JE/heart = 50 JE/damage point. */
    public static final long FALL_PER_POINT = 50;
    public static final double STEP_BONUS = 0.5;
    public static final int NIGHT_VISION_DURATION = 260;
    public static final int NIGHT_VISION_REFRESH = 220;
    public static final int DARK_BELOW = 8;
    public static final Identifier SHIELD = Jugcraft.id("exosuit_shield");
    public static final Identifier SPEED = Jugcraft.id("exosuit_speed");
    public static final Identifier STEP = Jugcraft.id("exosuit_step");
    public static final Identifier FALL = Jugcraft.id("exosuit_fall");
    private static final Map<ServerPlayer, Vec3> POSITIONS = new WeakHashMap<>();
    private static final Map<ServerPlayer, ShieldState> SHIELDS = new WeakHashMap<>();
    private static final class ShieldState {
        final ItemStack stack; float own; float total; float externalMax; long damagedAt = Long.MIN_VALUE / 2;
        ShieldState(ItemStack stack) { this.stack = stack; own = EquipmentSettings.number(stack, "shield", 0) / 1000F; }
    }
    private Exosuit() {}
    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> server.getPlayerList().getPlayers().forEach(Exosuit::tick));
        ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, base, taken, blocked) -> {
            if (entity instanceof ServerPlayer player) {
                ShieldState state = SHIELDS.get(player);
                if (state != null) { float before = state.own; reconcile(player, state); if (!blocked && (taken > 0 || state.own < before)) state.damagedAt = player.level().getGameTime(); }
            }
        });
    }
    public static void tick(ServerPlayer player) {
        if (!player.isAlive() || player.isSpectator()) { detach(player); return; }
        io.github.jimbozoomer.jugcraft.tools.PoweredNightVision.tick(player);
        chestplate(player, player.getItemBySlot(EquipmentSlot.CHEST));
        ItemStack legs = player.getItemBySlot(EquipmentSlot.LEGS);
        var input = player.getLastClientInput();
        boolean intent = input.forward() != input.backward() || input.left() != input.right();
        boolean eligible = worn(legs, ArmorType.LEGGINGS) && Chargeable.energy(legs) >= SPEED_PER_TICK
            && !player.isPassenger() && !player.isSwimming() && !player.isFallFlying() && !player.getAbilities().flying;
        Vec3 previous = POSITIONS.put(player, player.position());
        double moved = previous == null ? 0 : player.position().subtract(previous).horizontalDistanceSqr();
        boolean wasBoosted = player.getAttribute(Attributes.MOVEMENT_SPEED).hasModifier(SPEED);
        if (eligible && wasBoosted && intent && moved > 0.000001 && moved < 1 && player.hurtTime == 0) Chargeable.drain(legs, SPEED_PER_TICK);
        modifier(player, Attributes.MOVEMENT_SPEED, SPEED, eligible && Chargeable.energy(legs) >= SPEED_PER_TICK ? SPEED_BONUS : 0, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        ItemStack feet = player.getItemBySlot(EquipmentSlot.FEET);
        boolean boots = worn(feet, ArmorType.BOOTS) && Chargeable.energy(feet) > 0;
        modifier(player, Attributes.STEP_HEIGHT, STEP, boots ? STEP_BONUS : 0, AttributeModifier.Operation.ADD_VALUE);
        // Remove the old permanent immunity so the damage path can price each real landing once.
        modifier(player, Attributes.FALL_DAMAGE_MULTIPLIER, FALL, 0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    }
    /** Called after ordinary armor/enchantment/effect mitigation, before vanilla absorption. */
    public static float cushion(LivingEntity entity, DamageSource source, float damage) {
        if (!(entity instanceof ServerPlayer player) || !source.is(DamageTypes.FALL) || damage <= 0 || !Float.isFinite(damage)) return damage;
        ItemStack feet = player.getItemBySlot(EquipmentSlot.FEET);
        if (!worn(feet, ArmorType.BOOTS)) return damage;
        long available = Chargeable.energy(feet);
        float prevented = Math.min(damage, available / (float) FALL_PER_POINT);
        long cost = Math.min(available, (long) Math.ceil(prevented * FALL_PER_POINT));
        return cost > 0 && Chargeable.drain(feet, cost) ? Math.max(0, damage - prevented) : damage;
    }
    private static void save(ShieldState state) { EquipmentSettings.setNumber(state.stack, "shield", Math.round(state.own * 1000)); }
    private static void reconcile(ServerPlayer player, ShieldState state) {
        float now = player.getAbsorptionAmount();
        float externalMax = Math.max(0, player.getMaxAbsorption() - (player.getAttribute(Attributes.MAX_ABSORPTION).hasModifier(SHIELD) ? SHIELD_POINTS : 0));
        // Expiring external absorption can clamp the shared pool; those lost potion points were not shield damage.
        float externalExpiry = Math.max(0, state.externalMax - externalMax);
        state.own = Math.clamp(state.own - Math.max(0, state.total - now - externalExpiry), 0, now);
        state.externalMax = externalMax; state.total = now; save(state);
    }
    private static void detach(ServerPlayer player) {
        detachShield(player);
        modifier(player, Attributes.MOVEMENT_SPEED, SPEED, 0, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        modifier(player, Attributes.STEP_HEIGHT, STEP, 0, AttributeModifier.Operation.ADD_VALUE);
        io.github.jimbozoomer.jugcraft.tools.PoweredNightVision.clear(player);
        POSITIONS.remove(player);
    }
    private static void detachShield(ServerPlayer player) {
        ShieldState old = SHIELDS.remove(player);
        if (old != null) { reconcile(player, old); player.setAbsorptionAmount(Math.max(0, player.getAbsorptionAmount() - old.own)); }
        modifier(player, Attributes.MAX_ABSORPTION, SHIELD, 0, AttributeModifier.Operation.ADD_VALUE);
    }

    private static void chestplate(ServerPlayer player, ItemStack chest) {
        ShieldState state = SHIELDS.get(player);
        if (state != null && state.stack != chest) { detachShield(player); state = null; }
        if (!worn(chest, ArmorType.CHESTPLATE)) return;
        modifier(player, Attributes.MAX_ABSORPTION, SHIELD, Chargeable.energy(chest) >= SHIELD_PER_POINT || EquipmentSettings.number(chest, "shield", 0) > 0 ? SHIELD_POINTS : 0, AttributeModifier.Operation.ADD_VALUE);
        if (state == null) {
            state = new ShieldState(chest); state.own = Math.clamp(state.own, 0, SHIELD_POINTS);
            player.setAbsorptionAmount(player.getAbsorptionAmount() + state.own); state.total = player.getAbsorptionAmount();
            state.externalMax = Math.max(0, player.getMaxAbsorption() - SHIELD_POINTS);
            SHIELDS.put(player, state);
        }
        reconcile(player, state);
        long now = player.level().getGameTime();
        if (now % SHIELD_INTERVAL == 0 && now - state.damagedAt >= SHIELD_DELAY && state.own < SHIELD_POINTS
            && player.getAbsorptionAmount() < player.getMaxAbsorption()) {
            float gain = Math.min(1, Math.min(SHIELD_POINTS - state.own, player.getMaxAbsorption() - player.getAbsorptionAmount()));
            if (!Chargeable.drain(chest, (long) Math.ceil(gain * SHIELD_PER_POINT))) return;
            state.own += gain; player.setAbsorptionAmount(player.getAbsorptionAmount() + gain); state.total = player.getAbsorptionAmount(); save(state);
        }
    }
    public static boolean worn(ItemStack stack, ArmorType type) { return stack.getItem() instanceof ExosuitItem exosuit && exosuit.type() == type; }
    private static void modifier(ServerPlayer player, Holder<Attribute> attribute, Identifier id, double amount, AttributeModifier.Operation operation) {
        AttributeInstance instance = player.getAttribute(attribute); if (instance == null) return;
        if (amount == 0) instance.removeModifier(id);
        else if (!instance.hasModifier(id)) instance.addTransientModifier(new AttributeModifier(id, amount, operation));
    }
}

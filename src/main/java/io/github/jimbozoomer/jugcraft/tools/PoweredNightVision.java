package io.github.jimbozoomer.jugcraft.tools;

import io.github.jimbozoomer.jugcraft.gear.Exosuit;
import io.github.jimbozoomer.jugcraft.gear.ExosuitItem;
import io.github.jimbozoomer.jugcraft.jimtafacts.industry.IndustrialGear;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.ArmorType;

/** One owner for powered helmet vision, called once by the existing exosuit/player equipment tick. */
public final class PoweredNightVision {
    // Effect refresh is cosmetic; the industrial payment buys a separate 160 active-tick budget.
    // Both effect thresholds stay above vanilla's 200-tick night-vision flashing window.
    public static final int INDUSTRIAL_DURATION = 400, INDUSTRIAL_REFRESH = 240;
    public static final int INDUSTRIAL_PAID_TICKS = 160;
    public static final long INDUSTRIAL_COST = 200;
    private static final Map<ServerPlayer, State> STATES = new WeakHashMap<>();
    private static final class State {
        ItemStack helmet;
        MobEffectInstance effect;
        int duration;
        int industrialTicks;
        long lastIndustrialTick = Long.MIN_VALUE;
        State(ItemStack helmet, int duration) { this.helmet = helmet; this.duration = duration; }
    }
    private PoweredNightVision() {}

    private static boolean owns(ServerPlayer player, State state) {
        var effect = player.getEffect(MobEffects.NIGHT_VISION);
        return state != null && effect != null && effect == state.effect && effect.isAmbient()
            && !effect.isVisible() && effect.showIcon() && effect.getAmplifier() == 0
            && effect.getDuration() <= state.duration;
    }
    public static void clear(ServerPlayer player) {
        release(player, STATES.get(player));
    }
    private static void release(ServerPlayer player, State state) {
        if (owns(player, state)) player.removeEffect(MobEffects.NIGHT_VISION);
        if (state != null) state.effect = null;
    }
    public static void tick(ServerPlayer player) {
        ItemStack head = player.getItemBySlot(EquipmentSlot.HEAD);
        boolean exo = head.getItem() instanceof ExosuitItem item && item.type() == ArmorType.HELMET;
        boolean enabled = exo ? EquipmentSettings.enabled(head, "night_vision") : IndustrialGear.providesNightVision(head);
        State state = STATES.get(player);
        // Inventory/charging adapters may replace a worn stack with an equivalent copy.
        // Neither a copy nor switching helmet kind discards already-paid industrial time.
        if (state != null) {
            if (!ItemStack.isSameItem(state.helmet, head)) release(player, state);
            state.helmet = head;
        }
        if (!player.isAlive() || player.isSpectator() || !enabled
                || player.level().getMaxLocalRawBrightness(BlockPos.containing(player.getEyePosition())) >= Exosuit.DARK_BELOW) {
            clear(player); return;
        }
        var effect = player.getEffect(MobEffects.NIGHT_VISION);
        // Potions and other providers keep their effects and never pay helmet energy.
        if (effect != null && !owns(player, state)) {
            if (state != null) state.effect = null;
            return;
        }
        int duration = exo ? Exosuit.NIGHT_VISION_DURATION : INDUSTRIAL_DURATION;
        int refresh = exo ? Exosuit.NIGHT_VISION_REFRESH : INDUSTRIAL_REFRESH;
        if (state == null) { state = new State(head, duration); STATES.put(player, state); }
        state.duration = duration;
        if (exo) {
            if (!Chargeable.drain(head, Exosuit.NIGHT_VISION_PER_TICK)) { release(player, state); return; }
        } else {
            long tick = player.level().getServer().overworld().getGameTime();
            if (state.lastIndustrialTick != tick) {
                if (state.industrialTicks == 0) {
                    if (!Chargeable.drain(head, INDUSTRIAL_COST)) { release(player, state); return; }
                    state.industrialTicks = INDUSTRIAL_PAID_TICKS;
                }
                state.industrialTicks--;
                state.lastIndustrialTick = tick;
            }
        }
        if (effect == null || effect.getDuration() < refresh) {
            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, duration, 0, true, false, true));
            state.effect = player.getEffect(MobEffects.NIGHT_VISION);
        }
    }
}

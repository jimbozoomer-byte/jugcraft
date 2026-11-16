package io.github.jimbozoomer.jugcraft.tools;

import io.github.jimbozoomer.jugcraft.gear.Exosuit;
import io.github.jimbozoomer.jugcraft.jimtafacts.industry.IndustrialGear;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.ArmorType;

/**
 * The one owner of powered helmets' night vision: the exosuit helmets, and the industrial goggles, nano and quantum
 * helmets. {@link Exosuit#tick} calls it once for every player each server tick (docs/features/powered-night-vision.md).
 * An exosuit helmet gives it while its {@code night_vision} {@link EquipmentSettings} is on (the default), an industrial
 * one while {@link IndustrialGear#providesNightVision} says so; both only in the dark and only while charged.
 * <p>
 * A player has one night vision effect at most. Vanilla's {@code MobEffectInstance.update} merges any new night vision
 * into that same instance in place: a longer one takes over its duration, a non-ambient one clears its ambient flag, and
 * its particles and icon flags follow the newcomer. So the instance the helmet once added can come to hold a potion's,
 * a beacon's or an accessory's vision, and its identity alone cannot prove it is still the helmet's. The helmet owns
 * the effect only while it is the very instance the helmet last added, still has the helmet's shape (ambient, no
 * particles, an icon, level I, a finite duration) and has only counted down since the helmet last saw it. Anything that
 * merges in breaks one of these and the effect stops being the helmet's for good: it is never removed, refreshed or
 * paid for again. The helmet otherwise leaves other night vision alone and pays nothing while it is on, and gives its
 * own again once that has run out.
 */
public final class PoweredNightVision {
    /**
     * Industrial vision is refreshed to 400 ticks when below 240, so it never reaches vanilla's 200-tick flashing
     * window. Each 200 JE payment buys 160 ticks in which the helmet actually supplies vision; those ticks are counted
     * per player and per game tick, separately from the effect's refresh, and pausing (light, toggling, taking the
     * helmet off) does not lose them. The exosuit keeps its own 260/220 schedule and 2 JE a tick.
     */
    public static final int INDUSTRIAL_DURATION = 400, INDUSTRIAL_REFRESH = 240;
    public static final int INDUSTRIAL_PAID_TICKS = 160;
    public static final long INDUSTRIAL_COST = 200;
    private static final Map<ServerPlayer, State> STATES = new WeakHashMap<>();

    private static final class State {
        Item helmet;
        /** The instance the helmet last added, by identity, or null once it is gone or no longer the helmet's. */
        MobEffectInstance effect;
        /** That instance's duration when the helmet last added or checked it. Unmerged, it can only count down. */
        int seen;
        int industrialTicks;
        long lastIndustrialTick = Long.MIN_VALUE;

        State(Item helmet) {
            this.helmet = helmet;
        }
    }

    private PoweredNightVision() {
    }

    /**
     * Whether {@code current}, the player's night vision, is still exactly the helmet's own effect. The moment it is
     * not (another instance, or the helmet's with something merged into it) the helmet forgets it for good.
     */
    private static boolean owns(MobEffectInstance current, State state) {
        if (state == null || state.effect == null) {
            return false;
        }
        if (current == state.effect && current.isAmbient() && !current.isVisible() && current.showIcon()
                && current.getAmplifier() == 0 && !current.isInfiniteDuration() && current.getDuration() <= state.seen) {
            state.seen = current.getDuration();
            return true;
        }
        state.effect = null;
        return false;
    }

    /** Takes away the helmet's own night vision now, if the player still has it, and never anyone else's. */
    public static void clear(ServerPlayer player) {
        release(player, STATES.get(player));
    }

    private static void release(ServerPlayer player, State state) {
        if (state == null) {
            return;
        }
        MobEffectInstance current = player.getEffect(MobEffects.NIGHT_VISION);
        if (current != null && owns(current, state)) {
            player.removeEffect(MobEffects.NIGHT_VISION);
        }
        state.effect = null;
    }

    public static void tick(ServerPlayer player) {
        ItemStack head = player.getItemBySlot(EquipmentSlot.HEAD);
        boolean exo = Exosuit.worn(head, ArmorType.HELMET);
        boolean enabled = exo ? EquipmentSettings.enabled(head, "night_vision") : IndustrialGear.providesNightVision(head);
        State state = STATES.get(player);
        // A different helmet hands over: the old one's effect goes and the new one gives its own. A copy of the same
        // helmet (charging and inventory sync replace worn stacks) is not a different helmet.
        if (state != null && state.helmet != head.getItem()) {
            release(player, state);
            state.helmet = head.getItem();
        }
        if (!player.isAlive() || player.isSpectator() || !enabled
                || player.level().getMaxLocalRawBrightness(BlockPos.containing(player.getEyePosition())) >= Exosuit.DARK_BELOW) {
            release(player, state);
            return;
        }
        MobEffectInstance current = player.getEffect(MobEffects.NIGHT_VISION);
        if (current != null && !owns(current, state)) {
            // Another source's vision (a potion, a beacon, an accessory, or one merged into the helmet's): left alone, unpaid.
            return;
        }
        if (state == null) {
            state = new State(head.getItem());
            STATES.put(player, state);
        }
        if (exo) {
            if (!Chargeable.drain(head, Exosuit.NIGHT_VISION_PER_TICK)) {
                release(player, state);
                return;
            }
        } else {
            long tick = player.level().getServer().overworld().getGameTime();
            if (state.lastIndustrialTick != tick) {
                if (state.industrialTicks == 0) {
                    if (!Chargeable.drain(head, INDUSTRIAL_COST)) {
                        release(player, state);
                        return;
                    }
                    state.industrialTicks = INDUSTRIAL_PAID_TICKS;
                }
                state.industrialTicks--;
                state.lastIndustrialTick = tick;
            }
        }
        int refresh = exo ? Exosuit.NIGHT_VISION_REFRESH : INDUSTRIAL_REFRESH;
        if (current == null || current.getDuration() < refresh) {
            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, exo ? Exosuit.NIGHT_VISION_DURATION : INDUSTRIAL_DURATION,
                    0, true, false, true));
            state.effect = player.getEffect(MobEffects.NIGHT_VISION);
            state.seen = state.effect == null ? 0 : state.effect.getDuration();
        }
    }
}

package io.github.jimbozoomer.jugcraft.test;

import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.world.entity.LivingEntity;

/** Opt-in observation of native server equip-sound decisions; never included in the runtime mod. */
public final class EquipmentAudioProbe {
    private static final Map<LivingEntity, Integer> HEARD = new WeakHashMap<>();
    public static void start(LivingEntity entity) { HEARD.put(entity, 0); }
    public static int count(LivingEntity entity) { return HEARD.getOrDefault(entity, 0); }
    public static void stop(LivingEntity entity) { HEARD.remove(entity); }
    public static void record(LivingEntity entity) { HEARD.computeIfPresent(entity, (e, n) -> n + 1); }
}

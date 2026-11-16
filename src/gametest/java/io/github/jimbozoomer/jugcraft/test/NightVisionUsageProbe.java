package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.tools.Chargeable;
import io.github.jimbozoomer.jugcraft.gear.Exosuit;
import io.github.jimbozoomer.jugcraft.tools.PoweredNightVision;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/** Test-only metering at actual server tick boundaries, after the production equipment callbacks. */
public final class NightVisionUsageProbe implements ModInitializer {
    private static final Map<UUID, Meter> METERS = new ConcurrentHashMap<>();
    private static final class Meter {
        final String item;
        final boolean pause;
        boolean pending = true;
        int ticks;
        int activeTicks;
        volatile boolean done;
        String error;
        Meter(String item, boolean pause) { this.item = item; this.pause = pause; }
    }
    public static void begin(ServerPlayer player, String item) {
        begin(player,item,false);
    }
    public static void begin(ServerPlayer player, String item, boolean pause) {
        player.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);PoweredNightVision.clear(player);
        METERS.put(player.getUUID(), new Meter(item,pause));
    }
    public static boolean finished(UUID player) { var m=METERS.get(player);return m!=null&&m.done; }
    public static void verify(ServerPlayer player) {
        var m=METERS.remove(player.getUUID());
        if(m==null||!m.done||m.error!=null)throw new AssertionError(m==null?"Missing night vision meter":m.error);
    }
    @Override public void onInitialize() {
        ServerTickEvents.START_SERVER_TICK.register(server->{
            for(var entry:METERS.entrySet()){
                var m=entry.getValue();if(m.done)continue;var p=server.getPlayerList().getPlayer(entry.getKey());if(p==null)continue;
                if(m.pending){
                    var head=new ItemStack(BuiltInRegistries.ITEM.getValue(Jugcraft.id(m.item)));Chargeable.setEnergy(head,10000);
                    EquipmentAudioProbe.start(p);p.setItemSlot(EquipmentSlot.HEAD,head);m.pending=false;
                }
                if(MockPlayerChannels.isMock(p))p.doTick();
                // Real armor adapters can publish fresh stack objects while synchronizing components.
                p.setItemSlot(EquipmentSlot.HEAD,p.getItemBySlot(EquipmentSlot.HEAD).copy());
                if(m.pause)CustomData.update(DataComponents.CUSTOM_DATA,p.getItemBySlot(EquipmentSlot.HEAD),t->t.putBoolean("JimtaGearOff",m.ticks%2==1));
                if(m.ticks%31==30)p.removeEffect(MobEffects.NIGHT_VISION);
            }
        });
        ServerTickEvents.END_SERVER_TICK.register(server->{
            for(var entry:METERS.entrySet()){
                var m=entry.getValue();if(m.done||m.pending)continue;var p=server.getPlayerList().getPlayer(entry.getKey());if(p==null)continue;
                boolean enabled=!m.pause||m.ticks%2==0;
                m.ticks++;if(enabled)m.activeTicks++;
                Exosuit.tick(p); // A duplicate callback must not consume another paid tick.
                long expected=10000-200L*((m.activeTicks+159)/160);
                long actual=Chargeable.energy(p.getItemBySlot(EquipmentSlot.HEAD));var effect=p.getEffect(MobEffects.NIGHT_VISION);
                if(actual!=expected)m.error=m.item+" drained "+(10000-actual)+" JE in "+m.ticks+" server ticks; expected "+(10000-expected);
                else if(enabled&&(effect==null||effect.getDuration()<=200)||!enabled&&effect!=null)m.error="Missing/flashing or disabled metered vision: "+m.item;
                else if(!m.pause&&EquipmentAudioProbe.count(p)!=1)m.error="Metered updates replayed equip audio: "+m.item;
                if(m.error!=null||m.ticks==640){
                    p.setItemSlot(EquipmentSlot.HEAD,ItemStack.EMPTY);PoweredNightVision.clear(p);EquipmentAudioProbe.stop(p);
                    Jugcraft.LOGGER.info("[night-vision-meter] {}: {} ticks, {} JE used, {}",m.item,m.ticks,10000-actual,m.error==null?"passed":m.error);
                    m.done=true;
                }
            }
        });
    }
}

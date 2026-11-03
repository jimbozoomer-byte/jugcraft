package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.tools.Chargeable;
import java.util.List;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

/** Actual client/server vision synchronization and native server sound dispatch while equipment works. */
public final class PoweredArmorClientGameTests implements FabricClientGameTest {
    @Override public void runTest(ClientGameTestContext context) {
        double gamma=context.computeOnClient(c->c.options.gamma().get());
        try(var world=context.worldBuilder().create()) {
            world.getConnection().waitForChunksRender();var server=world.getServer();
            server.runCommand("gamemode survival @p");server.runCommand("time set noon");server.runCommand("weather clear");
            server.runCommand("fill -6 98 -6 6 105 6 minecraft:stone hollow");
            server.runCommand("fill -5 99 -5 5 99 5 minecraft:smooth_stone");
            server.runCommand("fill -4 100 5 -2 103 5 minecraft:gold_ore");
            server.runCommand("fill 0 100 5 2 103 5 minecraft:copper_ore");
            server.runCommand("tp @p 0.5 100 0.5 0 0");
            context.getInput().resizeWindow(1000,700);
            context.runOnClient(c->{c.options.gamma().set(0.0);if(!c.gui.hud.isHidden())c.gui.hud.toggle();});
            context.waitTicks(30);context.takeScreenshot("powered_armor_cave_without_vision");
            for(String id:List.of("jimta_industrial_night_vision_goggles","jimta_nano_helmet","jimta_quantum_helmet","exosuit_helmet","ronin_exosuit_helmet")) {
                boolean metered=id.startsWith("jimta_");
                server.runOnServer(s->{
                    var p=s.getPlayerList().getPlayers().getFirst();
                    if(p.level().getMaxLocalRawBrightness(net.minecraft.core.BlockPos.containing(p.getEyePosition()))>=8)throw new AssertionError("Cave is not dark");
                    if(metered){NightVisionUsageProbe.begin(p,id);return;}
                    EquipmentAudioProbe.start(p);
                    var stack=new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("jugcraft",id)));
                    Chargeable.setEnergy(stack,10000);p.setItemSlot(EquipmentSlot.HEAD,stack);
                });
                context.waitFor(c->c.player.hasEffect(MobEffects.NIGHT_VISION)
                    &&c.player.getEffect(MobEffects.NIGHT_VISION).getDuration()>200
                    &&BuiltInRegistries.ITEM.getKey(c.player.getItemBySlot(EquipmentSlot.HEAD).getItem()).getPath().equals(id),150);
                context.waitTicks(200);
                context.runOnClient(c->{if(c.player.getEffect(MobEffects.NIGHT_VISION)==null||c.player.getEffect(MobEffects.NIGHT_VISION).getDuration()<=200)throw new AssertionError("Missing/flashing client vision: "+id);});
                context.takeScreenshot("powered_armor_vision_"+id);
                if(metered){
                    context.waitFor(c->NightVisionUsageProbe.finished(c.player.getUUID()),800);
                    server.runOnServer(s->NightVisionUsageProbe.verify(s.getPlayerList().getPlayers().getFirst()));
                    context.waitFor(c->!c.player.hasEffect(MobEffects.NIGHT_VISION),100);
                    continue;
                }
                server.runOnServer(s->{var p=s.getPlayerList().getPlayers().getFirst();
                    if(EquipmentAudioProbe.count(p)!=1)throw new AssertionError("Repeated native equip sound while using "+id+": "+EquipmentAudioProbe.count(p));
                    if(Chargeable.energy(p.getItemBySlot(EquipmentSlot.HEAD))>=10000)throw new AssertionError("Vision never paid energy");
                });
                server.runOnServer(s->s.getPlayerList().getPlayers().getFirst().setItemSlot(EquipmentSlot.HEAD,ItemStack.EMPTY));
                context.waitFor(c->!c.player.hasEffect(MobEffects.NIGHT_VISION),100);
            }
            server.runOnServer(s->{var p=s.getPlayerList().getPlayers().getFirst();
                for(String id:List.of("exosuit_chestplate","exosuit_leggings","exosuit_boots")){
                    var stack=new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("jugcraft",id)));Chargeable.setEnergy(stack,10000);
                    p.setItemSlot(stack.get(DataComponents.EQUIPPABLE).slot(),stack);
                }
                EquipmentAudioProbe.start(p);
            });
            context.waitTicks(20);context.getInput().holdKey(o->o.keyUp);
            try{context.waitTicks(10);}finally{context.getInput().releaseKey(o->o.keyUp);}
            context.waitTicks(90);
            server.runOnServer(s->{var p=s.getPlayerList().getPlayers().getFirst();
                var feet=p.getItemBySlot(EquipmentSlot.FEET);long before=Chargeable.energy(feet);
                p.hurtServer(p.level(),p.damageSources().fall(),4);
                if(Chargeable.energy(feet)!=before-200)throw new AssertionError("Fall protection did not pay its exact energy cost");
                if(Chargeable.energy(p.getItemBySlot(EquipmentSlot.LEGS))>=10000)throw new AssertionError("Movement did not use leggings");
                if(Chargeable.energy(p.getItemBySlot(EquipmentSlot.CHEST))>=10000)throw new AssertionError("Shield did not regenerate");
                for(var slot:List.of(EquipmentSlot.CHEST,EquipmentSlot.LEGS,EquipmentSlot.FEET))Chargeable.charge(p.getItemBySlot(slot),100);
            });
            context.waitTicks(30);
            server.runOnServer(s->{var p=s.getPlayerList().getPlayers().getFirst();
                if(EquipmentAudioProbe.count(p)!=0)throw new AssertionError("Resource updates replayed suit equip audio: "+EquipmentAudioProbe.count(p));
                EquipmentAudioProbe.stop(p);
            });
        }finally{context.runOnClient(c->{c.options.gamma().set(gamma);if(c.gui.hud.isHidden())c.gui.hud.toggle();});}
    }
}

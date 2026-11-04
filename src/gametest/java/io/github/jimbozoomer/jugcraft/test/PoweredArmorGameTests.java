package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.gear.*;
import io.github.jimbozoomer.jugcraft.tools.*;
import io.github.jimbozoomer.jugcraft.fluid.*;
import io.github.jimbozoomer.jugcraft.jimtafacts.industry.*;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;

/** Native equip-sound callbacks, complete powered armor families, and real status-effect ticking. */
public final class PoweredArmorGameTests {
    private static ItemStack industrial(String name, long energy) {
        var s = new ItemStack(IndustrialGear.ITEMS.get("jimta_" + name)); Chargeable.setEnergy(s, energy); return s;
    }
    private static ServerPlayer player(GameTestHelper h) {
        var p = TestPlayers.survivor(h); p.doTick(); return p;
    }
    private static void sound(GameTestHelper h, ServerPlayer p, ItemStack before, ItemStack after, int expected) {
        EquipmentAudioProbe.start(p);
        var eq = after.get(DataComponents.EQUIPPABLE);
        p.onEquipItem(eq.slot(), before, after);
        h.assertTrue(EquipmentAudioProbe.count(p) == expected, "Native equip sound count for " + after.getItem() + ": " + EquipmentAudioProbe.count(p));
        EquipmentAudioProbe.stop(p);
    }
    @GameTest public void everyRechargeableWearableUpdatesQuietlyButStillSynchronizes(GameTestHelper h) {
        var p = player(h); int checked = 0;
        for (var item : BuiltInRegistries.ITEM) {
            var stack = new ItemStack(item); var eq = stack.get(DataComponents.EQUIPPABLE);
            if (!(item instanceof Chargeable) || eq == null) continue;
            Chargeable.setEnergy(stack, 1000); var before = stack.copy(); Chargeable.drain(stack, 100);
            sound(h, p, before, stack, 0);
            h.assertTrue(p.equipmentHasChanged(before, stack), "Quiet charge updates must still synchronize");
            sound(h, p, ItemStack.EMPTY, stack, 1);
            Chargeable.charge(stack, 50); sound(h, p, before, stack, 0); checked++;
        }
        h.assertTrue(checked >= 20, "Powered equipment audit missed armor/pack families: " + checked); h.succeed();
    }
    @GameTest public void shieldAndLowChargeWarningsDoNotReequipButRealChangesDo(GameTestHelper h) {
        var p=player(h);var chest=new ItemStack(JugcraftExosuit.piece(ExosuitItem.Style.VANGUARD,ArmorType.CHESTPLATE));
        var before=chest.copy();EquipmentSettings.setNumber(chest,"shield",4000);EquipmentSettings.setNumber(chest,"fuel_warning",1);
        sound(h,p,before,chest,0);
        before=chest.copy();chest.set(DataComponents.CUSTOM_NAME,Component.literal("Named armor"));sound(h,p,before,chest,1);
        var other=new ItemStack(JugcraftExosuit.piece(ExosuitItem.Style.RONIN,ArmorType.CHESTPLATE));sound(h,p,chest,other,1);
        var iron=new ItemStack(Items.IRON_HELMET);before=iron.copy();iron.setDamageValue(1);sound(h,p,before,iron,1);h.succeed();
    }
    @GameTest public void fuelAndFoamPacksAlsoUpdateQuietly(GameTestHelper h) {
        var p=player(h);var fuel=new ItemStack(IndustrialGear.ITEMS.get("jimta_biogas_jetpack"));
        var before=fuel.copy();var fluid=BuiltInRegistries.FLUID.getValue(io.github.jimbozoomer.jugcraft.Jugcraft.id("biogas"));
        fuel.set(JugcraftFluids.STORED_FLUID,new StoredFluid(FluidVariant.of(fluid),1000));sound(h,p,before,fuel,0);
        var foam=new ItemStack(IndustrialGear.ITEMS.get("jimta_construction_foam_pack"));
        h.assertTrue(!foam.isEmpty(),"Foam pack fixture must exist");before=foam.copy();
        CustomData.update(DataComponents.CUSTOM_DATA,foam,t->t.putInt("JimtaFoam",300));sound(h,p,before,foam,0);h.succeed();
    }
    private static ServerPlayer dark(GameTestHelper h) {
        for(int x=0;x<=6;x++)for(int y=0;y<=5;y++)for(int z=0;z<=6;z++)
            h.setBlock(new BlockPos(x,y,z),x==0||x==6||y==0||y==5||z==0||z==6?Blocks.STONE:Blocks.AIR);
        var p=player(h);p.setPos(net.minecraft.world.phys.Vec3.atBottomCenterOf(h.absolutePos(new BlockPos(3,1,3))));return p;
    }
    private static void timedVision(GameTestHelper h, boolean replaceAndRefresh) {
        String[] names={"industrial_night_vision_goggles","nano_helmet","quantum_helmet"};
        ServerPlayer[] players={dark(h),player(h),player(h)};
        int[][] positions={{2,1,2},{4,1,2},{3,1,4}};
        for(int i=0;i<3;i++)players[i].setPos(net.minecraft.world.phys.Vec3.atBottomCenterOf(h.absolutePos(new BlockPos(positions[i][0],1,positions[i][2]))));
        long[] started={-1};
        h.runAfterDelay(20,()->{
            started[0]=h.getLevel().getGameTime();
            for(int i=0;i<3;i++){players[i].setItemSlot(EquipmentSlot.HEAD,industrial(names[i],10000));Exosuit.tick(players[i]);}
        });
        h.onEachTick(()->{
            if(started[0]<0)return;
            long elapsed=h.getLevel().getGameTime()-started[0]+1;
            for(int i=0;i<3;i++){
                var p=players[i];p.doTick();
                if(replaceAndRefresh){p.setItemSlot(EquipmentSlot.HEAD,p.getItemBySlot(EquipmentSlot.HEAD).copy());if(elapsed%17==0)p.removeEffect(MobEffects.NIGHT_VISION);}
                Exosuit.tick(p);IndustrialGear.tick(p);
                if(replaceAndRefresh)Exosuit.tick(p); // Repeated callbacks in one real server tick must not double bill.
                var effect=p.getEffect(MobEffects.NIGHT_VISION);
                h.assertTrue(effect!=null&&effect.getDuration()>200,"Missing/flashing vision from "+names[i]+" at "+elapsed);
                long expected=10000-200*((elapsed+159)/160);
                h.assertTrue(Chargeable.energy(p.getItemBySlot(EquipmentSlot.HEAD))==expected,"Incorrect timed night vision rate for "+names[i]+" at "+elapsed);
            }
            if(elapsed>=600){
                for(var p:players){p.setItemSlot(EquipmentSlot.HEAD,ItemStack.EMPTY);Exosuit.tick(p);h.assertTrue(!p.hasEffect(MobEffects.NIGHT_VISION),"Unequipped helmet leaked vision");}
                h.succeed();
            }
        });
    }
    @GameTest(maxTicks=680) public void gogglesNanoAndQuantumHaveSustainedNonFlashingVision(GameTestHelper h) { timedVision(h,false); }
    @GameTest(maxTicks=680) public void copiesAndEffectRestartsCannotRebuyPaidVision(GameTestHelper h) { timedVision(h,true); }
    @GameTest(maxTicks=80) public void visionHandlesChargeTogglePotionsAndHelmetHandover(GameTestHelper h) {
        var p=dark(h);
        h.runAfterDelay(20,()->{
            var head=industrial("industrial_night_vision_goggles",199);p.setItemSlot(EquipmentSlot.HEAD,head);Exosuit.tick(p);
            h.assertTrue(!p.hasEffect(MobEffects.NIGHT_VISION)&&Chargeable.energy(head)==199,"Unpaid goggles vision");
            Chargeable.setEnergy(head,1000);Exosuit.tick(p);h.assertTrue(p.hasEffect(MobEffects.NIGHT_VISION),"Charged goggles failed");
            CustomData.update(DataComponents.CUSTOM_DATA,head,t->t.putBoolean("JimtaGearOff",true));Exosuit.tick(p);
            h.assertTrue(!p.hasEffect(MobEffects.NIGHT_VISION),"Disabled goggles retained vision");
            CustomData.update(DataComponents.CUSTOM_DATA,head,t->t.putBoolean("JimtaGearOff",false));Exosuit.tick(p);
            p.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION,1200));long energy=Chargeable.energy(head);Exosuit.tick(p);
            h.assertTrue(Chargeable.energy(head)==energy,"Potion vision billed goggles");p.setItemSlot(EquipmentSlot.HEAD,ItemStack.EMPTY);Exosuit.tick(p);
            h.assertTrue(p.hasEffect(MobEffects.NIGHT_VISION),"Removing goggles erased potion");p.removeEffect(MobEffects.NIGHT_VISION);
            for(var style:ExosuitItem.Style.values()){
                var exo=new ItemStack(JugcraftExosuit.piece(style,ArmorType.HELMET));Chargeable.setEnergy(exo,1000);p.setItemSlot(EquipmentSlot.HEAD,exo);Exosuit.tick(p);
                h.assertTrue(p.hasEffect(MobEffects.NIGHT_VISION)&&Chargeable.energy(exo)==998,"Exosuit handover failed");
                head=industrial("industrial_night_vision_goggles",1000);p.setItemSlot(EquipmentSlot.HEAD,head);Exosuit.tick(p);
                h.assertTrue(p.getEffect(MobEffects.NIGHT_VISION).getDuration()==400&&Chargeable.energy(head)==1000,"Goggles handover lost already-paid time");
            }
            p.setGameMode(GameType.SPECTATOR);Exosuit.tick(p);h.assertTrue(!p.hasEffect(MobEffects.NIGHT_VISION),"Spectator retained powered vision");h.succeed();
        });
    }
    @GameTest(maxTicks=240) public void depletedGogglesStopAtNextPaidRefresh(GameTestHelper h) {
        var p=dark(h);long[] started={-1};h.runAfterDelay(20,()->{
            started[0]=h.getLevel().getGameTime();p.setItemSlot(EquipmentSlot.HEAD,industrial("industrial_night_vision_goggles",200));Exosuit.tick(p);
        });
        h.onEachTick(()->{
            if(started[0]<0)return;p.doTick();Exosuit.tick(p);
            long elapsed=h.getLevel().getGameTime()-started[0]+1;
            if(elapsed<=160)h.assertTrue(p.hasEffect(MobEffects.NIGHT_VISION),"Paid goggles stopped early");
            else{h.assertTrue(Chargeable.energy(p.getItemBySlot(EquipmentSlot.HEAD))==0&&!p.hasEffect(MobEffects.NIGHT_VISION),"Flat goggles supplied unpaid vision");h.succeed();}
        });
    }
    @GameTest(maxTicks=700) public void pausingVisionKeepsItsUnusedPaidTime(GameTestHelper h) {
        var p=dark(h);boolean[] started={false};h.runAfterDelay(20,()->{NightVisionUsageProbe.begin(p,"jimta_industrial_night_vision_goggles",true);started[0]=true;});
        h.onEachTick(()->{
            if(started[0]&&NightVisionUsageProbe.finished(p.getUUID())){NightVisionUsageProbe.verify(p);h.succeed();}
        });
    }
}

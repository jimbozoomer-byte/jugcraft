package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.concordance.RateGate;
import io.github.jimbozoomer.jugcraft.concordance.assay.*;
import io.github.jimbozoomer.jugcraft.concordance.equivalence.Assay;
import java.util.*;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Unit;
import net.minecraft.world.*;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.Blocks;

/** Universal prices and real transaction/ownership boundaries, including the former default-diamond regression. */
public class AlchemyPlayabilityGameTests {
    private static ServerPlayer player(GameTestHelper h){return ConnectedAlchemyGameTests.player(h);}
    private static ItemStack item(String id){return new ItemStack(BuiltInRegistries.ITEM.getValue(io.github.jimbozoomer.jugcraft.Jugcraft.id(id)));}
    private static void press(net.minecraft.world.inventory.AbstractContainerMenu m,ServerPlayer p,int action){RateGate.forget(p.getUUID());m.clickMenuButton(p,action);}
    private static AlchemyPatternMenu tablet(ServerPlayer p){return new AlchemyPatternMenu(1,p.getInventory(),()->true,ItemStack.EMPTY);}
    @GameTest(maxTicks=20)public void everyRegisteredDefaultItemHasPositiveValue(GameTestHelper h){
        int checked=0;for(var item:BuiltInRegistries.ITEM){if(item==Items.AIR)continue;var m=AlchemicalWorkshopBlockEntity.material(new ItemStack(item));
            h.assertTrue(m!=null&&m.formable()&&m.dissolvable()&&Assay.form(m,1)>0,"Positive default-item value: "+BuiltInRegistries.ITEM.getKey(item));checked++;}
        h.assertTrue(checked>3000,"Full registry, not the historical 45-item whitelist");h.succeed();
    }
    @GameTest(maxTicks=20)public void familiarValuesAndNoFormationSurcharge(GameTestHelper h){
        var expected=Map.of(Items.DIAMOND,8192L,Items.IRON_INGOT,256L,Items.GOLD_INGOT,2048L,Items.COAL,128L,Items.OAK_LOG,32L,Items.OAK_PLANKS,8L);
        expected.forEach((item,value)->{var m=AlchemicalWorkshopBlockEntity.material(new ItemStack(item));h.assertTrue(Assay.form(m,1)==value&&Assay.dissolve(m,1)==value,"Reference value and no surcharge: "+item);});h.succeed();
    }
    @GameTest(maxTicks=20)public void dissolveIsRequiredAndConfirmationBindsExactStack(GameTestHelper h){
        var p=player(h);var m=tablet(p);m.getSlot(0).set(new ItemStack(Items.DIAMOND,2));press(m,p,0);
        h.assertTrue(AlchemyPatterns.known(p).isEmpty()&&m.confirming(),"Preview cannot teach a retained sample");
        m.getSlot(0).getItem().shrink(1);press(m,p,0);h.assertTrue(AlchemyPatterns.known(p).isEmpty()&&m.getSlot(0).getItem().getCount()==1,"Changed input needs a fresh confirmation");
        press(m,p,0);h.assertTrue(AlchemyPatterns.known(p).contains("minecraft:diamond")&&!m.getSlot(0).hasItem()&&Assaying.balance(p)==18192,"Consume exactly once and learn");h.succeed();
    }
    @GameTest(maxTicks=20)public void learnedBulkWithdrawalPaysWalletThenStars(GameTestHelper h){
        var p=player(h);var m=tablet(p);m.getSlot(0).set(new ItemStack(Items.IRON_INGOT));press(m,p,0);press(m,p,0);
        Assaying.setBalance(p,100);var star=item("klein_star_6");KleinStars.setStored(star,5000);p.getInventory().setItem(4,star);
        press(m,p,7);press(m,p,1);h.assertTrue(m.getSlot(1).getItem().getCount()==16&&Assaying.balance(p)==0&&KleinStars.stored(star)==1004,"16 iron cost exactly 4096 across wallet and star");
        m.getSlot(1).set(ItemStack.EMPTY);press(m,p,9);press(m,p,1);h.assertTrue(m.getSlot(1).getItem().getCount()==3&&KleinStars.stored(star)==236,"Max creates only affordable items");h.succeed();
    }
    @GameTest(maxTicks=20)public void learnedBulkFitsOutputWithoutSpillingOrOvercharging(GameTestHelper h){
        var p=player(h);var m=tablet(p);m.getSlot(0).set(new ItemStack(Items.IRON_INGOT));press(m,p,0);press(m,p,0);m.getSlot(1).set(new ItemStack(Items.IRON_INGOT,63));
        long before=AlchemyPower.available(p);press(m,p,8);press(m,p,1);h.assertTrue(m.getSlot(1).getItem().getCount()==64&&AlchemyPower.available(p)==before-256,"Only the one output space is purchased");
        press(m,p,1);h.assertTrue(AlchemyPower.available(p)==before-256,"Full output cannot debit again");h.succeed();
    }
    @GameTest(maxTicks=20)public void storedContentsAndChargeCannotBeDestroyedByDissolving(GameTestHelper h){
        var p=player(h);var m=tablet(p);var bag=item("white_alchemical_bag");bag.set(DataComponents.CONTAINER,ItemContainerContents.fromItems(List.of(new ItemStack(Items.DIAMOND,64))));
        m.getSlot(0).set(bag);press(m,p,0);press(m,p,0);h.assertTrue(m.inputStatus()==2&&m.getSlot(0).getItem()==bag&&AlchemyPatterns.known(p).isEmpty(),"Filled bag retained with explicit reason");
        var star=item("klein_star_1");KleinStars.setStored(star,100);m.getSlot(0).set(star);press(m,p,0);h.assertTrue(!m.canDissolve()&&KleinStars.stored(star)==100,"Charged star never silently loses stored essence");h.succeed();
    }
    @GameTest(maxTicks=20)public void diamondDensityDefaultWorksAndRespectsReserve(GameTestHelper h){
        var p=player(h);Assaying.setBalance(p,8192);var gem=item("gem_of_eternal_density");gem.set(AlchemicalGear.ACTIVE,Unit.INSTANCE);gem.set(AlchemyAutomation.FILTER,List.of("minecraft:coal"));gem.set(AlchemyAutomation.RESERVE,1024);gem.set(AlchemyAutomation.BUDGET,4);
        var c=new SimpleContainer(4);c.setItem(0,gem);c.setItem(1,new ItemStack(Items.COAL,16));
        AlchemyAutomation.convert(p,c,gem,0,4);h.assertTrue(c.getItem(1).getCount()==12&&c.getItem(2).isEmpty(),"Only four inputs; reserve blocks early output");
        AlchemyAutomation.convert(p,c,gem,0,4);h.assertTrue(c.getItem(2).is(Items.DIAMOND)&&Assaying.balance(p)==1024,"Default diamond output works at 8192 and preserves reserve");h.succeed();
    }
    @GameTest(maxTicks=20)public void densityCanSpendSavedEssenceWithoutNewInput(GameTestHelper h){
        var p=player(h);Assaying.setBalance(p,8192);var gem=item("gem_of_eternal_density");gem.set(AlchemicalGear.ACTIVE,Unit.INSTANCE);gem.set(AlchemyAutomation.FILTER,List.of("minecraft:coal"));var c=new SimpleContainer(3);c.setItem(0,gem);
        h.assertTrue(AlchemyAutomation.convert(p,c,gem,0,3)&&c.getItem(1).is(Items.DIAMOND)&&Assaying.balance(p)==0,"Saved essence is usable on subsequent pulses");h.succeed();
    }
    @GameTest(maxTicks=20)public void storageConsolidationPreservesIdentityAndContents(GameTestHelper h){
        var p=player(h);var pos=new BlockPos(2,3,2);h.setBlock(pos,AlchemyStorage.BLOCKS.get("alchemical_chest"));var be=h.getBlockEntity(pos,AlchemyStorageBlockEntity.class);be.placedBy(p);
        be.setItem(0,new ItemStack(Items.COAL,30));be.setItem(7,new ItemStack(Items.COAL,40));var named=new ItemStack(Items.COAL,5);named.set(DataComponents.CUSTOM_NAME,net.minecraft.network.chat.Component.literal("Keep"));be.setItem(8,named);
        var m=new AlchemyStorageMenu(1,p.getInventory(),be);press(m,p,1);h.assertTrue(be.getItem(0).getCount()==64&&be.getItem(1).getCount()==6&&be.getItem(2).has(DataComponents.CUSTOM_NAME)&&be.getItem(2).getCount()==5,"Consolidates exact identities only");m.removed(p);h.succeed();
    }
    @GameTest(maxTicks=20)public void offlineFactoryUsesOwnBufferAndStillHonorsClaims(GameTestHelper h){
        var p=player(h);var pos=new BlockPos(2,3,2);h.setBlock(pos,AlchemicalWorkshops.BLOCKS.get("condenser_mk1"));var be=h.getBlockEntity(pos,AlchemicalWorkshopBlockEntity.class);be.placedBy(p);be.setItem(1,new ItemStack(Items.IRON_INGOT));be.setEssence(1024);
        h.getLevel().getServer().getPlayerList().remove(p);be.pulse(null);
        h.assertTrue(be.getItem(5).is(Items.IRON_INGOT)&&be.essence()==768,"Offline loaded condenser spends only its saved buffer");
        try(var claims=TestClaims.open()){claims.block(be.getBlockPos(),UUID.randomUUID());be.pulse(null);h.assertTrue(be.essence()==768&&be.getItem(5).getCount()==1,"Offline actor cannot bypass current claims");}h.succeed();
    }
    @GameTest(maxTicks=20)public void offlineAuthorizationSurvivesSaveAndReload(GameTestHelper h){
        var p=player(h);var pos=new BlockPos(2,3,2);h.setBlock(pos,AlchemyMachines.BLOCKS.get("red_matter_furnace"));var be=h.getBlockEntity(pos,AlchemyMachinesBlockEntity.class);be.placedBy(p);be.setEssence(64);be.setItem(1,new ItemStack(Items.RAW_IRON));
        var saved=be.saveWithoutMetadata(h.getLevel().registryAccess());be.loadWithComponents(net.minecraft.world.level.storage.TagValueInput.create(net.minecraft.util.ProblemReporter.DISCARDING,h.getLevel().registryAccess(),saved));
        h.getLevel().getServer().getPlayerList().remove(p);be.pulse(null);h.assertTrue(be.getItem(4).is(Items.IRON_INGOT)&&be.essence()==48,"Persisted authorized furnace works offline without touching a wallet");h.succeed();
    }
    @GameTest(maxTicks=20)public void eyeAreaIsBoundedAndMenuOwned(GameTestHelper h){
        var p=player(h);var eye=item("mercurial_eye");p.setItemInHand(InteractionHand.MAIN_HAND,eye);var m=new AlchemyPatternMenu(1,p.getInventory(),()->p.getMainHandItem()==eye,eye);
        press(m,p,10);h.assertTrue(m.area()==5,"3 to 5 area");press(m,p,10);h.assertTrue(m.area()==1,"5 wraps to 1");
        var other=player(h);press(m,other,10);h.assertTrue(m.area()==1,"Foreign player cannot change eye settings");h.succeed();
    }
    @GameTest(maxTicks=20)public void fractionalItemCanBeLearnedWithoutInventingEssence(GameTestHelper h){
        var p=player(h);var m=tablet(p);var slab=new ItemStack(Items.STONE_SLAB);var material=AlchemicalWorkshopBlockEntity.material(slab);
        h.assertTrue(Assay.dissolve(material,1)==0&&Assay.form(material,1)==1,"Half-essence slab retains rational value");
        m.getSlot(0).set(slab);long before=Assaying.balance(p);press(m,p,0);press(m,p,0);
        h.assertTrue(!m.getSlot(0).hasItem()&&AlchemyPatterns.known(p).contains("minecraft:stone_slab")&&Assaying.balance(p)==before,"Actual low-value input can teach without creating a free essence unit");
        press(m,p,7);press(m,p,1);h.assertTrue(m.getSlot(1).getItem().getCount()==16&&Assaying.balance(p)==before-8,"Bulk transaction rounds once, without surcharge");h.succeed();
    }
    @GameTest(maxTicks=20)public void upgradedGearHasRealPriceAndFitsCondenserAfterReload(GameTestHelper h){
        var p=player(h);var pos=new BlockPos(2,3,2);h.setBlock(pos,AlchemicalWorkshops.BLOCKS.get("condenser_mk2"));
        var be=h.getBlockEntity(pos,AlchemicalWorkshopBlockEntity.class);be.placedBy(p);var target=item("gem_chestplate");
        long price=Assay.form(AlchemicalWorkshopBlockEntity.material(target),1);
        h.assertTrue(price>262144&&price<be.capacity(),"Smithing-based Gem gear is valuable and fits the expanded Mk II buffer");
        be.setItem(1,target);be.setEssence((int)price);
        var saved=be.saveWithoutMetadata(h.getLevel().registryAccess());be.loadWithComponents(net.minecraft.world.level.storage.TagValueInput.create(net.minecraft.util.ProblemReporter.DISCARDING,h.getLevel().registryAccess(),saved));
        be.pulse(p);h.assertTrue(be.getItem(5).is(target.getItem())&&be.essence()==0,"Reload preserves enough essence for one exact-price upgraded item");h.succeed();
    }
}

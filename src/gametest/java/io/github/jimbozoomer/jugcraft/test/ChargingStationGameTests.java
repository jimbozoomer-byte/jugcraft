package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.tools.*;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.*;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.*;

/** Actual menus, persistence, cable charging and item conservation for the two-bay station. */
public final class ChargingStationGameTests {
    private static ChargingStationBlockEntity station(GameTestHelper h) {
        var pos=new BlockPos(2,2,2);var state=JugcraftTools.CHARGING_STATION.defaultBlockState();
        h.setBlock(pos.below(),Blocks.STONE);h.setBlock(pos,state);h.setBlock(pos.above(),state.setValue(ChargingStationBlock.HALF,DoubleBlockHalf.UPPER));
        return h.getBlockEntity(pos,ChargingStationBlockEntity.class);
    }
    private static ServerPlayer player(GameTestHelper h) {
        h.setBlock(new BlockPos(1,1,1),Blocks.STONE);var p=h.makeMockServerPlayerInLevel();p.setGameMode(GameType.SURVIVAL);
        p.connection.handleAcceptPlayerLoad(new net.minecraft.network.protocol.game.ServerboundPlayerLoadedPacket());
        p.setPos(Vec3.atCenterOf(h.absolutePos(new BlockPos(1,2,1))));return p;
    }
    private static ChargingStationMenu open(ServerPlayer p,ChargingStationBlockEntity be) { p.openMenu(be);return (ChargingStationMenu)p.containerMenu; }
    private static int button(int bay,ToolUpgrades.Kind kind,boolean remove) { return ChargingStationMenu.button(bay,kind,remove); }
    private static void tick(GameTestHelper h,ChargingStationBlockEntity be) {
        var state=be.getBlockState();var ticker=((ChargingStationBlock)state.getBlock()).getTicker(h.getLevel(),state,JugcraftTools.CHARGING_STATION_ENTITY);
        ticker.tick(h.getLevel(),be.getBlockPos(),state,be);
    }
    @GameTest public void bothHalvesOpenWithHeldItemsWithoutConsumingThem(GameTestHelper h) {
        var be=station(h);var p=player(h);var held=new ItemStack(JugcraftTools.MODULES.get(ToolUpgrades.Kind.CAPACITY),2);p.setItemInHand(InteractionHand.MAIN_HAND,held);
        for(var pos:new BlockPos[]{be.getBlockPos(),be.getBlockPos().above()}) {
            p.closeContainer();p.gameMode.useItemOn(p,h.getLevel(),held,InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(pos),Direction.NORTH,pos,false));
            h.assertTrue(p.containerMenu instanceof ChargingStationMenu&&p.containerMenu.slots.size()==38,"Either half must open the two-slot menu");
            h.assertTrue(held.getCount()==2&&be.isEmpty(),"Opening the menu consumed or inserted a held item");
        }h.succeed();
    }
    @GameTest public void chargeIsSharedAndUnusedSharesAreReused(GameTestHelper h) {
        var be=station(h);be.setTool(new ItemStack(JugcraftTools.MINING_DRILL));be.setTool(1,new ItemStack(JugcraftTools.CHAINSAW));be.energy().setAmount(2000);tick(h,be);
        h.assertTrue(Chargeable.energy(be.tool(0))==256&&Chargeable.energy(be.tool(1))==256&&be.energy().getAmount()==1488,"Two active bays must fairly share 512 JE");
        Chargeable.setEnergy(be.tool(1),Chargeable.capacity(be.tool(1)));long first=Chargeable.energy(be.tool());tick(h,be);
        h.assertTrue(Chargeable.energy(be.tool())==first+512&&be.energy().getAmount()==976,"Full bay wasted the other bay's charging share");
        be.energy().setAmount(1);first=Chargeable.energy(be.tool());tick(h,be);
        h.assertTrue(Chargeable.energy(be.tool())==first+1&&be.energy().getAmount()==0,"Low buffer created or lost charge");h.succeed();
    }
    @GameTest public void modulesFitBothBaysAndReturnWithoutASpareModule(GameTestHelper h) {
        var be=station(h);var p=player(h);be.setTool(new ItemStack(JugcraftTools.MINING_DRILL));be.setTool(1,new ItemStack(JugcraftTools.CHAINSAW));
        p.getInventory().setItem(9,new ItemStack(JugcraftTools.MODULES.get(ToolUpgrades.Kind.CAPACITY),2));var menu=open(p,be);
        for(int bay=0;bay<2;bay++)h.assertTrue(menu.clickMenuButton(p,button(bay,ToolUpgrades.Kind.CAPACITY,false)),"Inventory module did not fit bay "+bay);
        h.assertTrue(p.getInventory().getItem(9).isEmpty()&&Chargeable.capacity(be.tool())==500000&&Chargeable.capacity(be.tool(1))==500000,"Capacity fit did not conserve modules");
        h.assertTrue(menu.clickMenuButton(p,button(1,ToolUpgrades.Kind.CAPACITY,true)),"Removal required a spare module");
        h.assertTrue(menu.getCarried().is(JugcraftTools.MODULES.get(ToolUpgrades.Kind.CAPACITY))&&Chargeable.capacity(be.tool(1))==250000,"Module not returned to cursor");
        h.assertTrue(menu.clickMenuButton(p,button(0,ToolUpgrades.Kind.CAPACITY,false))&&menu.getCarried().isEmpty(),"Fitting from cursor failed");h.succeed();
    }
    @GameTest public void capacityRemovalCannotDiscardEnergy(GameTestHelper h) {
        var be=station(h);var p=player(h);var tool=new ItemStack(JugcraftTools.MINING_DRILL);ToolUpgrades.fit(h.getLevel(),tool,ToolUpgrades.Kind.CAPACITY);Chargeable.setEnergy(tool,400000);be.setTool(tool);
        var menu=open(p,be);h.assertTrue(!menu.clickMenuButton(p,button(0,ToolUpgrades.Kind.CAPACITY,true))&&menu.status()==6,"Overfull capacity removal succeeded");
        h.assertTrue(Chargeable.energy(be.tool())==400000&&Chargeable.capacity(be.tool())==500000&&menu.getCarried().isEmpty(),"Failed removal changed charge or created a module");h.succeed();
    }
    @GameTest public void fullInventoryRemovalIsAtomicAndCannotBeRepeated(GameTestHelper h) {
        var be=station(h);var p=player(h);var tool=new ItemStack(JugcraftTools.MINING_DRILL);ToolUpgrades.fit(h.getLevel(),tool,ToolUpgrades.Kind.OVERCLOCK);be.setTool(tool);
        for(int i=0;i<36;i++)p.getInventory().setItem(i,new ItemStack(Items.COBBLESTONE,64));var menu=open(p,be);menu.setCarried(new ItemStack(Items.DIRT,64));
        h.assertTrue(!menu.clickMenuButton(p,button(0,ToolUpgrades.Kind.OVERCLOCK,true))&&menu.status()==ChargingStationMenu.NO_ROOM,"Full inventory removal discarded a module");
        h.assertTrue(ToolUpgrades.level(be.tool(),ToolUpgrades.Kind.OVERCLOCK)==1,"Failed removal consumed the installed level");menu.setCarried(ItemStack.EMPTY);
        h.assertTrue(menu.clickMenuButton(p,button(0,ToolUpgrades.Kind.OVERCLOCK,true)),"Empty cursor did not accept module");
        h.assertTrue(!menu.clickMenuButton(p,button(0,ToolUpgrades.Kind.OVERCLOCK,true))&&menu.getCarried().getCount()==1,"Repeated removal duplicated module");h.succeed();
    }
    @GameTest public void ordinaryEnchantmentsCannotBecomeModules(GameTestHelper h) {
        var be=station(h);var p=player(h);var tool=new ItemStack(JugcraftTools.MINING_DRILL);
        tool.enchant(h.getLevel().registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT).getOrThrow(Enchantments.FORTUNE),2);be.setTool(tool);var menu=open(p,be);
        h.assertTrue(ToolUpgrades.installed(h.getLevel(),be.tool(),ToolUpgrades.Kind.FORTUNE)==2&&!menu.clickMenuButton(p,button(0,ToolUpgrades.Kind.FORTUNE,true)),"Natural Fortune produced a recoverable module");
        menu.setCarried(new ItemStack(JugcraftTools.MODULES.get(ToolUpgrades.Kind.SILK_TOUCH)));
        h.assertTrue(!menu.clickMenuButton(p,button(0,ToolUpgrades.Kind.SILK_TOUCH,false))&&menu.getCarried().getCount()==1,"Conflicting module was consumed");
        menu.setCarried(new ItemStack(JugcraftTools.MODULES.get(ToolUpgrades.Kind.FORTUNE)));
        h.assertTrue(menu.clickMenuButton(p,button(0,ToolUpgrades.Kind.FORTUNE,false))&&menu.clickMenuButton(p,button(0,ToolUpgrades.Kind.FORTUNE,true)),"New fitted enchantment could not be recovered");
        h.assertTrue(ToolUpgrades.installed(h.getLevel(),be.tool(),ToolUpgrades.Kind.FORTUNE)==2&&menu.getCarried().getCount()==1,"Recovery damaged original enchantment");h.succeed();
    }
    @GameTest public void legacySlotAndTwoItemSaveRoundTripKeepComponents(GameTestHelper h) {
        var be=station(h);var drill=new ItemStack(JugcraftTools.MINING_DRILL);drill.set(DataComponents.CUSTOM_NAME,Component.literal("Saved Drill"));Chargeable.setEnergy(drill,12345);ToolUpgrades.fit(h.getLevel(),drill,ToolUpgrades.Kind.CAPACITY);
        var old=TagValueOutput.createWithContext(ProblemReporter.DISCARDING,h.getLevel().registryAccess());old.putLong("energy",34567);ContainerHelper.saveAllItems(old,NonNullList.of(ItemStack.EMPTY,drill));
        be.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING,h.getLevel().registryAccess(),old.buildResult()));
        h.assertTrue(be.tool().getHoverName().getString().equals("Saved Drill")&&Chargeable.energy(be.tool())==12345&&be.tool(1).isEmpty()&&be.energy().getAmount()==34567,"Legacy slot was lost");
        be.setTool(1,new ItemStack(JugcraftTools.CHAINSAW));var saved=be.saveWithoutMetadata(h.getLevel().registryAccess());var copy=new ChargingStationBlockEntity(be.getBlockPos(),be.getBlockState());
        copy.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING,h.getLevel().registryAccess(),saved));
        h.assertTrue(copy.tool(1).is(JugcraftTools.CHAINSAW)&&ItemStack.isSameItemSameComponents(copy.tool(),drill)&&copy.energy().getAmount()==34567,"Two-slot saved inventory changed components");h.succeed();
    }
    private static void drops(GameTestHelper h,boolean upper) {
        var be=station(h);be.setTool(new ItemStack(JugcraftTools.MINING_DRILL));be.setTool(1,new ItemStack(JugcraftTools.CHAINSAW));h.getLevel().destroyBlock(upper?be.getBlockPos().above():be.getBlockPos(),true);
        var drops=h.getEntities(EntityTypes.ITEM);long drill=drops.stream().filter(e->e.getItem().is(JugcraftTools.MINING_DRILL)).mapToInt(e->e.getItem().getCount()).sum();
        long saw=drops.stream().filter(e->e.getItem().is(JugcraftTools.CHAINSAW)).mapToInt(e->e.getItem().getCount()).sum();
        h.assertTrue(drill==1&&saw==1,"Breaking either half must drop both items exactly once");h.succeed();
    }
    @GameTest public void lowerBreakDropsBothItemsOnce(GameTestHelper h) { drops(h,false); }
    @GameTest public void upperBreakDropsBothItemsOnce(GameTestHelper h) { drops(h,true); }
    @GameTest public void permissionsDistanceAndInvalidButtonsBlockMutation(GameTestHelper h) {
        var be=station(h);var p=player(h);be.setTool(new ItemStack(JugcraftTools.MINING_DRILL));p.getInventory().setItem(9,new ItemStack(JugcraftTools.MODULES.get(ToolUpgrades.Kind.CAPACITY)));var menu=open(p,be);
        h.assertTrue(!menu.clickMenuButton(p,-1)&&!menu.clickMenuButton(p,ToolUpgrades.Kind.values().length*4),"Unbounded button accepted");
        try(var claim=TestClaims.open().block(be.getBlockPos().above(),java.util.UUID.randomUUID())) {
            h.assertTrue(!menu.stillValid(p)&&!menu.clickMenuButton(p,button(0,ToolUpgrades.Kind.CAPACITY,false)),"Upper-half protection did not revoke access");
            menu.clicked(0,0,ContainerInput.PICKUP,p);h.assertTrue(menu.getCarried().isEmpty()&&!be.tool().isEmpty(),"Protected slot interaction succeeded");
        }
        p.setPos(p.getX()+40,p.getY(),p.getZ());h.assertTrue(!menu.stillValid(p)&&menu.quickMoveStack(p,0).isEmpty(),"Remote inventory transfer succeeded");
        h.assertTrue(ToolUpgrades.level(be.tool(),ToolUpgrades.Kind.CAPACITY)==0&&p.getInventory().getItem(9).getCount()==1,"Denied operation changed inventory");h.succeed();
    }
    @GameTest public void simultaneousMenusCannotDuplicateModules(GameTestHelper h) {
        var be=station(h);var a=player(h);var b=player(h);be.setTool(new ItemStack(JugcraftTools.MINING_DRILL));a.getInventory().setItem(9,new ItemStack(JugcraftTools.MODULES.get(ToolUpgrades.Kind.CAPACITY)));
        var first=open(a,be);var second=open(b,be);
        h.assertTrue(first.clickMenuButton(a,button(0,ToolUpgrades.Kind.CAPACITY,false)),"First viewer failed to fit module");
        h.assertTrue(second.clickMenuButton(b,button(0,ToolUpgrades.Kind.CAPACITY,true)),"Second viewer could not recover current level");
        h.assertTrue(!first.clickMenuButton(a,button(0,ToolUpgrades.Kind.CAPACITY,true))&&second.getCarried().getCount()==1&&a.getInventory().getItem(9).isEmpty(),"Stale view duplicated module");h.succeed();
    }
    @GameTest public void hundredThousandBufferPersistsAndSyncsAcrossSixteenBitBoundary(GameTestHelper h) {
        var be=station(h);be.energy().setAmount(100000);
        h.assertTrue(be.energy().getAmount()==100000,"Station did not accept its new full buffer");
        var saved=be.saveWithoutMetadata(h.getLevel().registryAccess());var copy=new ChargingStationBlockEntity(be.getBlockPos(),be.getBlockState());
        copy.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING,h.getLevel().registryAccess(),saved));
        h.assertTrue(copy.energy().getAmount()==100000,"Saving truncated the new buffer");
        var menu=new ChargingStationMenu(1,player(h).getInventory());
        for(long amount:new long[]{50000,65535,65536,99999,100000}) {
            menu.setData(0,(short)(amount&65535));menu.setData(1,(short)(amount>>>16));
            h.assertTrue(menu.energy()==amount,"Signed menu words truncated "+amount+" to "+menu.energy());
        }h.succeed();
    }

}

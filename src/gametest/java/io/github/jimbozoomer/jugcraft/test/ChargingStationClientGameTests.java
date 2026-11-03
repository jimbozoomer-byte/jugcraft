package io.github.jimbozoomer.jugcraft.test;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.jimbozoomer.jugcraft.client.ChargingStationScreen;
import io.github.jimbozoomer.jugcraft.tools.*;
import java.util.concurrent.atomic.AtomicReference;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.*;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.*;

/** Native inventory clicks and real widget packets, rendered at the smallest supported GUI size. */
public final class ChargingStationClientGameTests implements FabricClientGameTest {
    private static Button control(net.minecraft.client.Minecraft mc,int bay,ToolUpgrades.Kind kind,boolean remove) {
        var buttons=mc.gui.screen().children().stream().filter(Button.class::isInstance).map(Button.class::cast)
            .filter(b->b.getMessage().getString().equals("+")||b.getMessage().getString().equals("-")).toList();
        if(buttons.size()!=ToolUpgrades.Kind.values().length*4)throw new AssertionError("Expected every module control");
        return buttons.get(ChargingStationMenu.button(bay,kind,remove));
    }
    private static void press(ClientGameTestContext c,int bay,ToolUpgrades.Kind kind,boolean remove) {
        c.waitFor(mc->control(mc,bay,kind,remove).active,100);
        c.runOnClient(mc->control(mc,bay,kind,remove).onPress(new MouseButtonInfo(InputConstants.MOUSE_BUTTON_LEFT,0)));
    }
    @Override public void runTest(ClientGameTestContext c) {
        try(var world=c.worldBuilder().create()) {
            world.getConnection().waitForChunksRender();c.getInput().resizeWindow(640,480);
            c.runOnClient(mc->{mc.options.guiScale().set(2);mc.resizeGui();});
            world.getServer().runCommand("gamemode survival @a");
            var position=new AtomicReference<BlockPos>();
            world.getServer().runOnServer(server->{
                var p=server.getPlayerList().getPlayers().getFirst();var pos=p.blockPosition().offset(2,0,0);position.set(pos);
                var state=JugcraftTools.CHARGING_STATION.defaultBlockState();p.level().setBlockAndUpdate(pos,state);
                p.level().setBlockAndUpdate(pos.above(),state.setValue(ChargingStationBlock.HALF,DoubleBlockHalf.UPPER));
                ((ChargingStationBlockEntity)p.level().getBlockEntity(pos)).energy().setAmount(100000);
                var drill=new ItemStack(JugcraftTools.MINING_DRILL);drill.set(DataComponents.CUSTOM_NAME,Component.literal("Workshop Drill"));
                p.getInventory().setItem(9,drill);p.getInventory().setItem(10,new ItemStack(JugcraftTools.CHAINSAW));
                p.getInventory().setItem(11,new ItemStack(JugcraftTools.MODULES.get(ToolUpgrades.Kind.CAPACITY),2));
                p.getInventory().setItem(12,new ItemStack(JugcraftTools.MODULES.get(ToolUpgrades.Kind.OVERCLOCK)));
                p.getInventory().setItem(13,new ItemStack(JugcraftTools.MODULES.get(ToolUpgrades.Kind.EFFICIENCY),3));
            });
            c.waitTicks(4);
            c.runOnClient(mc->mc.gameMode.useItemOn(mc.player,InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(position.get().above()),Direction.WEST,position.get().above(),false)));
            c.waitForScreen(ChargingStationScreen.class);
            c.waitFor(mc->((ChargingStationMenu)mc.player.containerMenu).energy()==100000);
            c.runOnClient(mc->mc.gui.toastManager().clear());c.waitTicks(2);
            c.takeScreenshot("jugcraft_charging_station_full_100k");
            c.waitFor(mc->mc.player.containerMenu.getSlot(2).getItem().is(JugcraftTools.MINING_DRILL));
            c.runOnClient(mc->{int id=mc.player.containerMenu.containerId;mc.gameMode.handleContainerInput(id,2,0,ContainerInput.QUICK_MOVE,mc.player);});
            c.waitFor(mc->mc.player.containerMenu.getSlot(0).getItem().is(JugcraftTools.MINING_DRILL));
            c.runOnClient(mc->mc.gameMode.handleContainerInput(mc.player.containerMenu.containerId,3,0,ContainerInput.QUICK_MOVE,mc.player));
            c.waitFor(mc->mc.player.containerMenu.getSlot(1).getItem().is(JugcraftTools.CHAINSAW));
            press(c,0,ToolUpgrades.Kind.CAPACITY,false);
            c.waitFor(mc->ToolUpgrades.level(mc.player.containerMenu.getSlot(0).getItem(),ToolUpgrades.Kind.CAPACITY)==1);
            press(c,1,ToolUpgrades.Kind.OVERCLOCK,false);
            c.waitFor(mc->ToolUpgrades.level(mc.player.containerMenu.getSlot(1).getItem(),ToolUpgrades.Kind.OVERCLOCK)==1);
            press(c,0,ToolUpgrades.Kind.EFFICIENCY,false);
            c.waitFor(mc->ToolUpgrades.level(mc.player.containerMenu.getSlot(0).getItem(),ToolUpgrades.Kind.EFFICIENCY)==1);
            press(c,0,ToolUpgrades.Kind.EFFICIENCY,false);
            c.waitFor(mc->ToolUpgrades.level(mc.player.containerMenu.getSlot(0).getItem(),ToolUpgrades.Kind.EFFICIENCY)==2);
            press(c,1,ToolUpgrades.Kind.EFFICIENCY,false);
            c.waitFor(mc->ToolUpgrades.level(mc.player.containerMenu.getSlot(1).getItem(),ToolUpgrades.Kind.EFFICIENCY)==1);
            c.waitTicks(4);
            c.runOnClient(mc->{var screen=(ChargingStationScreen)mc.gui.screen();var panel=screen.panelArea();
                if(panel.left()<0||panel.top()<0||panel.right()>mc.getWindow().getGuiScaledWidth()||panel.bottom()>mc.getWindow().getGuiScaledHeight())throw new AssertionError("Two-bay screen does not fit 320x240 GUI");
                for(var widget:mc.gui.screen().children())if(widget instanceof Button b&&(b.getY()<panel.top()||b.getY()+b.getHeight()>panel.top()+134||b.getX()<panel.left()||b.getX()+b.getWidth()>panel.right()))throw new AssertionError("Module row overlaps the footer or panel bounds");
                var d=mc.player.containerMenu.getSlot(0).getItem();var saw=mc.player.containerMenu.getSlot(1).getItem();
                if(((PoweredToolItem)d.getItem()).energyPerBlock(d)!=42||((PoweredToolItem)saw.getItem()).energyPerBlock(saw)!=51)throw new AssertionError("Efficiency component did not synchronize");
                if(Chargeable.energy(mc.player.containerMenu.getSlot(0).getItem())<=0||Chargeable.energy(mc.player.containerMenu.getSlot(1).getItem())<=0)throw new AssertionError("Both bays did not charge through real synchronization");
            });
            c.runOnClient(mc->mc.gui.toastManager().clear());c.waitTicks(2);
            c.takeScreenshot("jugcraft_charging_station_two_bays");
            press(c,0,ToolUpgrades.Kind.CAPACITY,true);
            c.waitFor(mc->ToolUpgrades.level(mc.player.containerMenu.getSlot(0).getItem(),ToolUpgrades.Kind.CAPACITY)==0&&mc.player.containerMenu.getCarried().is(JugcraftTools.MODULES.get(ToolUpgrades.Kind.CAPACITY)));
            world.getServer().runOnServer(server->{
                var p=server.getPlayerList().getPlayers().getFirst();var be=(ChargingStationBlockEntity)p.level().getBlockEntity(position.get());
                if(Chargeable.energy(be.tool())+Chargeable.energy(be.tool(1))+be.energy().getAmount()!=100000)throw new AssertionError("Charging or modules created/lost JE");
                if(!p.getInventory().getItem(13).isEmpty()||p.getInventory().getItem(11).getCount()!=1||!p.getInventory().getItem(12).isEmpty()||p.containerMenu.getCarried().getCount()!=1)throw new AssertionError("Widget packets did not conserve modules");
            });
            c.runOnClient(mc->mc.gui.toastManager().clear());c.waitTicks(2);
            c.takeScreenshot("jugcraft_charging_station_module_recovered");
            press(c,0,ToolUpgrades.Kind.EFFICIENCY,true);
            c.waitFor(mc->ToolUpgrades.level(mc.player.containerMenu.getSlot(0).getItem(),ToolUpgrades.Kind.EFFICIENCY)==1);
            c.waitFor(mc->mc.player.getInventory().contains(new ItemStack(JugcraftTools.MODULES.get(ToolUpgrades.Kind.EFFICIENCY))));
            c.runOnClient(mc->mc.player.closeContainer());c.waitFor(mc->mc.gui.screen()==null);
            c.runOnClient(mc->mc.gameMode.useItemOn(mc.player,InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(position.get()),Direction.WEST,position.get(),false)));
            c.waitForScreen(ChargingStationScreen.class);c.waitFor(mc->mc.player.containerMenu.getSlot(0).getItem().is(JugcraftTools.MINING_DRILL)&&mc.player.containerMenu.getSlot(1).getItem().is(JugcraftTools.CHAINSAW));
        }
    }
}

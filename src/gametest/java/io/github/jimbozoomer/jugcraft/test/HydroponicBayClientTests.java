package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.client.MachineScreen;
import io.github.jimbozoomer.jugcraft.machine.*;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.SimpleContainerData;

/** Render the empty-tank guidance and a filled nutrient gauge, independently of transfer tests. */
public final class HydroponicBayClientTests implements FabricClientGameTest {
    @Override public void runTest(ClientGameTestContext context) {
        try(var world=context.worldBuilder().create()) {
            world.getConnection().waitForChunksRender();
            for(int amount:new int[]{0,4000}) {
                context.setScreen(()->{
                    var inventory=Minecraft.getInstance().player.getInventory();
                    var kind=MachineKind.HYDROPONIC_BAY;
                    var data=new SimpleContainerData(MachineBlockEntity.DATA_COUNT);
                    data.set(MachineBlockEntity.DATA_ENERGY_LOW,10000);
                    data.set(MachineBlockEntity.DATA_CAPACITY_LOW,16000);
                    data.set(MachineBlockEntity.DATA_TANK,amount);
                    var menu=new MachineMenu(JugcraftMachines.menuType(kind),kind,0,inventory,
                        new SimpleContainer(kind.containerSize()),data);
                    return new MachineScreen(menu,inventory,Component.literal("Hydroponic Bay"));
                });
                context.waitTicks(8);
                context.takeScreenshot("hydroponic_nutrient_"+amount);
                double[] cursor=context.computeOnClient(client->{
                    var panel=((MachineScreen)client.gui.screen()).panelArea();
                    return new double[]{(panel.left()+32)/net.minecraft.client.MouseHandler.getScaledXPos(client.getWindow(),1.0),
                        (panel.top()+42)/net.minecraft.client.MouseHandler.getScaledYPos(client.getWindow(),1.0)};
                });
                context.getInput().setCursorPos(cursor[0],cursor[1]);
                context.waitTicks(4);
                context.takeScreenshot("hydroponic_nutrient_tooltip_"+amount);
            }
            context.setScreen(()->null);
        }
    }
}

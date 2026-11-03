package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.client.ProspectorScreen;
import io.github.jimbozoomer.jugcraft.prospecting.*;
import java.util.List;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;

/** Rendered client coverage of the shared scanner screen and both location precisions. */
public final class PoweredEquipmentClientGameTests implements FabricClientGameTest {
    @Override public void runTest(ClientGameTestContext context) {
        try(var world=context.worldBuilder().create()) {
            world.getConnection().waitForChunksRender();
            context.getInput().resizeWindow(640,480);
            context.runOnClient(client->{client.options.guiScale().set(2);client.resizeGui();});
            for(int type=1;type<=2;type++) {
                final int instrument=type;
                world.getServer().runOnServer(server->{
                    var player=server.getPlayerList().getPlayers().getFirst();
                    var item=io.github.jimbozoomer.jugcraft.jimtafacts.industry.IndustrialGear.ITEMS.get(instrument==1?"jimta_ore_scanner":"jimta_advanced_ore_scanner");
                    var stack=new net.minecraft.world.item.ItemStack(item);
                    io.github.jimbozoomer.jugcraft.tools.Chargeable.setEnergy(stack,10_000);
                    player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,stack);
                    var pos=player.blockPosition().offset(10,-4,0);
                    pos=new BlockPos(pos.getX()&~1,pos.getY(),pos.getZ()&~1);
                    player.level().setBlockAndUpdate(pos,net.minecraft.world.level.block.Blocks.DIAMOND_ORE.defaultBlockState());
                    item.use(player.level(),player,net.minecraft.world.InteractionHand.MAIN_HAND);
                });
                context.waitFor(client->client.gui.screen() instanceof ProspectorScreen,500);
                context.runOnClient(client->{
                    var screen=(ProspectorScreen)client.gui.screen();
                    var panel=screen.panelArea();
                    if(panel.left()<0||panel.top()<0||panel.right()>client.getWindow().getGuiScaledWidth()||panel.bottom()>client.getWindow().getGuiScaledHeight())throw new AssertionError("Scanner panel does not fit the smallest supported GUI");
                    var result=screen.snapshot();
                    if(result.instrument()!=instrument||result.coverage()<=0)throw new AssertionError("Scanner packet metadata lost");
                    var diamond=result.readings().stream().filter(r->r.icon().getPath().equals("diamond_ore")).findFirst().orElseThrow();
                    if(diamond.distanceLow()<0||instrument==1&&!diamond.direction().isEmpty()||instrument==2&&diamond.direction().isEmpty())throw new AssertionError("Scanner location precision incorrect");
                });
                context.waitTicks(30);
                context.takeScreenshot(type==1?"jugcraft_ore_scanner_distance":"jugcraft_advanced_scanner_bearing");
                world.getServer().runOnServer(server->{
                    var stack=server.getPlayerList().getPlayers().getFirst().getMainHandItem();
                    long expected=instrument==1?9500:9000;
                    if(io.github.jimbozoomer.jugcraft.tools.Chargeable.energy(stack)!=expected)throw new AssertionError("Incorrect scanner charge");
                });
                context.setScreen(()->null);
            }
            world.getServer().runCommand("tp @p ~ ~ ~ 0 0");
            world.getServer().runOnServer(server->{
                var p=server.getPlayerList().getPlayers().getFirst();
                var wall=BlockPos.containing(p.getX(),p.getEyeY(),p.getZ()+3);
                for(int x=-2;x<=2;x++)for(int y=-2;y<=2;y++)p.level().setBlockAndUpdate(wall.offset(x,y,0),net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
                var drill=new net.minecraft.world.item.ItemStack(io.github.jimbozoomer.jugcraft.tools.JugcraftTools.MINING_DRILL);
                io.github.jimbozoomer.jugcraft.tools.Chargeable.setEnergy(drill,2000);
                drill.set(io.github.jimbozoomer.jugcraft.tools.JugcraftTools.RANGE,1);
                drill.set(io.github.jimbozoomer.jugcraft.tools.JugcraftTools.DRILL_MODE,io.github.jimbozoomer.jugcraft.tools.MiningDrillItem.AREA_LARGE);
                p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,drill);
            });
            context.waitFor(client->io.github.jimbozoomer.jugcraft.client.PoweredToolPreview.targetCount()==25,100);
            context.waitTicks(4);context.takeScreenshot("jugcraft_drill_bulk_target_preview");
            world.getServer().runOnServer(server->{
                if(io.github.jimbozoomer.jugcraft.tools.Chargeable.energy(server.getPlayerList().getPlayers().getFirst().getMainHandItem())!=2000)throw new AssertionError("A cosmetic preview spent tool energy");
            });
        }
    }
}

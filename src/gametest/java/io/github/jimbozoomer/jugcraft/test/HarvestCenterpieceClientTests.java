package io.github.jimbozoomer.jugcraft.test;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import io.github.jimbozoomer.jugcraft.town.TownData;

/** Review the exact versioned town scene in a disposable world; no personal profile or city mutation. */
public final class HarvestCenterpieceClientTests implements FabricClientGameTest {
    @Override public void runTest(ClientGameTestContext context) {
        try(var world=context.worldBuilder().adjustSettings(c->c.setGameMode(WorldCreationUiState.SelectedGameMode.CREATIVE)).create()) {
            world.getConnection().waitForChunksRender();
            var origin=context.computeOnClient(c->c.player.blockPosition());
            var server=world.getServer();
            server.runCommand("gamerule minecraft:send_command_feedback false");
            server.runCommand("time set noon");server.runCommand("weather clear");
            server.runOnServer(s->{
                var scene=TownData.get().centerpiece.get("harvest_v2");
                if(scene==null || scene.size()!=75) throw new AssertionError("Final harvest scene is not installed");
                var level=s.overworld();
                for(int x=-8;x<=15;x++)for(int z=-10;z<=15;z++) {
                    level.setBlock(origin.offset(x,0,z),Blocks.STONE_BRICKS.defaultBlockState(),3);
                    for(int y=1;y<=12;y++)level.removeBlock(origin.offset(x,y,z),false);
                }
                scene.forEach((slot,text)->{
                    var state=TownData.parse(text);
                    if(state.isAir())throw new AssertionError("Unregistered harvest part: "+text);
                    level.setBlock(origin.offset(slot%7,slot/49+1,slot/7%7),state,3);
                });
            });
            context.getInput().pressKey(com.mojang.blaze3d.platform.InputConstants.KEY_F1);
            for(int angle=0;angle<2;angle++) {
                int x=angle==0?-4:10;
                server.runCommand("setblock %d %d %d minecraft:barrier".formatted(origin.getX()+x,origin.getY()+4,origin.getZ()-6));
                server.runCommand("tp @p %s %s %s %s 20".formatted(origin.getX()+x+.5,origin.getY()+5,origin.getZ()-5.5,angle==0?-38:38));
                context.waitTicks(20);world.getConnection().waitForChunksRender();
                context.takeScreenshot("harvest_centerpiece_day_"+angle);
            }
            server.runCommand("time set midnight");context.waitTicks(20);
            context.takeScreenshot("harvest_centerpiece_night");
        }
    }
}

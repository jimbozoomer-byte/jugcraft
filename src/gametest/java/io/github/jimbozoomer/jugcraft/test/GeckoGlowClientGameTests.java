package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.lair.tyrant.JugcraftTyrant;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.block.Blocks;

/** Exercises GeckoLib's actual auto-glowing layer, including shader shadow passes, in a disposable world. */
public class GeckoGlowClientGameTests implements FabricClientGameTest {
    @Override public void runTest(ClientGameTestContext context) {
        try(var world=context.worldBuilder().create()) {
            var server=world.getServer();
            world.getConnection().waitForChunksRender();
            server.runCommand("tick freeze"); // Keep these arena minions alive without an arena owner.
            server.runCommand("weather clear");
            server.runCommand("time set noon");
            server.runOnServer(s->{
                var level=s.overworld();var player=s.getPlayerList().getPlayers().getFirst();
                BlockPos at=player.blockPosition().atY(180);
                for(int x=-10;x<=10;x++)for(int z=-5;z<=14;z++)
                    level.setBlockAndUpdate(at.offset(x,-1,z),Blocks.STONE.defaultBlockState());
                for(int i=-1;i<=1;i++) {
                    var mob=JugcraftTyrant.CINDERLING.create(level,EntitySpawnReason.COMMAND);
                    if(mob==null)throw new AssertionError("Cinderling registration unavailable");
                    mob.setNoAi(true);mob.setNoGravity(true);
                    mob.snapTo(at.getX()+i*3+.5,at.getY(),at.getZ()+.5,0,0);
                    level.addFreshEntity(mob);
                }
                player.teleportTo(at.getX()+.5,at.getY(),at.getZ()+7.5);
                player.setYRot(180);player.setXRot(8);
            });
            context.runOnClient(c->{c.player.setYRot(180);c.player.setXRot(8);if(!c.gui.hud.isHidden())c.gui.hud.toggle();});
            world.getConnection().waitForChunksRender();context.waitTicks(40);
            context.runOnClient(c->{
                int count=0;
                for(var entity:c.level.entitiesForRendering())if(entity.getType()==JugcraftTyrant.CINDERLING)count++;
                if(count!=3)throw new AssertionError("The glow gallery must render all three Cinderlings, got "+count);
            });
            context.takeScreenshot("gecko_glow_day");
            server.runCommand("time set midnight");context.waitTicks(40);
            context.takeScreenshot("gecko_glow_night");
        }
    }
}

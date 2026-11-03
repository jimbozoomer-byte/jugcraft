package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.client.PoweredToolPreview;
import io.github.jimbozoomer.jugcraft.tools.*;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.core.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;

/** Real ray-picked faces at angles that defeat the old pitch heuristic, then native client mining. */
public final class DrillFaceClientGameTests implements FabricClientGameTest {
    private static final BlockPos ROOT=new BlockPos(0,110,0);

    @Override public void runTest(ClientGameTestContext context) {
        try(var world=context.worldBuilder().create()) {
            world.getConnection().waitForChunksRender();
            world.getServer().runCommand("gamemode creative @p");
            for(int reach=1;reach<=2;reach++)for(Direction face:Direction.values()) {
                final int radius=reach;
                world.getServer().runOnServer(server->{
                    var p=server.getPlayerList().getPlayers().getFirst();
                    p.getAbilities().flying=true;p.onUpdateAbilities();
                    for(int x=-3;x<=3;x++)for(int y=-3;y<=3;y++)for(int z=-3;z<=3;z++)
                        p.level().setBlockAndUpdate(ROOT.offset(x,y,z),Blocks.AIR.defaultBlockState());
                    for(int x=-radius;x<=radius;x++)for(int y=-radius;y<=radius;y++)for(int z=-radius;z<=radius;z++) {
                        boolean plane=switch(face.getAxis()){case X->x==0;case Y->y==0;case Z->z==0;};
                        if(plane)p.level().setBlockAndUpdate(ROOT.offset(x,y,z),Blocks.STONE.defaultBlockState());
                    }
                    var tool=new ItemStack(JugcraftTools.MINING_DRILL);
                    tool.set(JugcraftTools.RANGE,radius==2?1:0);
                    tool.set(JugcraftTools.DRILL_MODE,radius==2?MiningDrillItem.AREA_LARGE:MiningDrillItem.AREA);
                    Chargeable.setEnergy(tool,10000);p.setItemInHand(InteractionHand.MAIN_HAND,tool);
                });
                // Top/bottom at shallow pitch; walls at steep pitch. The hit face, not pitch, defines the plane.
                Vec3 normal=new Vec3(face.getStepX(),face.getStepY(),face.getStepZ());
                Vec3 eye=Vec3.atCenterOf(ROOT).add(normal.scale(2.2)).add(face.getAxis()==Direction.Axis.Y?new Vec3(0,0,3):new Vec3(0,3,0));
                Vec3 aim=Vec3.atCenterOf(ROOT).add(normal.scale(.5));var delta=aim.subtract(eye);
                double yaw=Math.toDegrees(Math.atan2(-delta.x,delta.z));
                double pitch=-Math.toDegrees(Math.atan2(delta.y,Math.sqrt(delta.x*delta.x+delta.z*delta.z)));
                world.getServer().runCommand("tp @p "+eye.x+" "+(eye.y-1.62)+" "+eye.z+" "+yaw+" "+pitch);
                context.waitFor(client->client.hitResult instanceof BlockHitResult hit&&hit.getBlockPos().equals(ROOT)
                    &&hit.getDirection()==face&&PoweredToolPreview.targetCount()==(2*radius+1)*(2*radius+1)
                    &&PoweredToolPreview.targets().stream().allMatch(pos->switch(face.getAxis()) {
                        case X->pos.getX()==ROOT.getX();case Y->pos.getY()==ROOT.getY();case Z->pos.getZ()==ROOT.getZ();
                    }),100);
                context.runOnClient(client->{
                    for(var pos:PoweredToolPreview.targets()) {
                        boolean plane=switch(face.getAxis()){case X->pos.getX()==ROOT.getX();case Y->pos.getY()==ROOT.getY();case Z->pos.getZ()==ROOT.getZ();};
                        if(!plane)throw new AssertionError("Preview uses wrong plane for "+face);
                        if(Math.abs(pos.getX()-ROOT.getX())>radius||Math.abs(pos.getY()-ROOT.getY())>radius||Math.abs(pos.getZ()-ROOT.getZ())>radius)
                            throw new AssertionError("Preview extends beyond selected size");
                    }
                });
                if(reach==1&&face==Direction.UP)context.takeScreenshot("jugcraft_drill_floor_face_preview");
                if(reach==2&&face==Direction.NORTH)context.takeScreenshot("jugcraft_drill_wall_face_preview");
            }
            // Keep the last wall: a survival start, cancel, and restart must update the latched face.
            world.getServer().runOnServer(server->{
                var level=server.overworld();
                for(int x=2;x<=4;x++)for(int z=-1;z<=1;z++)level.setBlockAndUpdate(new BlockPos(x,108,z),Blocks.STONE.defaultBlockState());
            });
            world.getServer().runCommand("gamemode survival @p");
            context.waitFor(client->!client.player.isCreative(),100);
            context.runOnClient(client->{
                if(!client.gameMode.startDestroyBlock(ROOT,Direction.EAST))throw new AssertionError("Client start refused");
                var input=(DrillMiningContext)client.gameMode;
                if(input.jugcraft$drillFace(ROOT)!=Direction.EAST)throw new AssertionError("Client did not latch START");
                client.gameMode.startDestroyBlock(ROOT,Direction.UP);
                if(input.jugcraft$drillFace(ROOT)!=Direction.EAST)throw new AssertionError("Continuing the same break rotated START's face");
                client.gameMode.stopDestroyBlock();
                if(input.jugcraft$drillFace(ROOT)!=null)throw new AssertionError("Client cancel retained face");
            });
            // Aim naturally at the wall before using the actual attack key and packet/timing path.
            world.getServer().runCommand("tp @p 3.5 109 0.5 90 0");
            context.waitFor(client->Math.abs(client.player.getX()-3.5)<.05&&Math.abs(client.player.getY()-109)<.05
                &&client.hitResult instanceof BlockHitResult hit&&hit.getBlockPos().equals(ROOT)&&hit.getDirection()==Direction.EAST,100);
            context.waitTicks(3);
            context.getInput().holdKey(o->o.keyAttack);
            try {context.waitFor(client->client.level.getBlockState(ROOT).isAir(),100);}
            finally {context.getInput().releaseKey(o->o.keyAttack);}
            world.getServer().runOnServer(server->{
                var p=server.getPlayerList().getPlayers().getFirst();int air=0;
                for(int y=-2;y<=2;y++)for(int z=-2;z<=2;z++)if(p.level().getBlockState(ROOT.offset(0,y,z)).isAir())air++;
                System.out.println("Drill client mining evidence: "+air+"/25 wall blocks cleared; "+Chargeable.energy(p.getMainHandItem())+" JE remaining; root="+p.level().getBlockState(ROOT));
            });
            context.waitFor(client->Chargeable.energy(client.player.getMainHandItem())==8500,100);
            world.getServer().runOnServer(server->{
                var p=server.getPlayerList().getPlayers().getFirst();
                for(int y=-2;y<=2;y++)for(int z=-2;z<=2;z++)
                    if(!p.level().getBlockState(ROOT.offset(0,y,z)).isAir())throw new AssertionError("Client preview and real mining differed");
                if(Chargeable.energy(p.getMainHandItem())!=8500)throw new AssertionError("Client mining did not bill 25 blocks");
            });
        }
    }
}

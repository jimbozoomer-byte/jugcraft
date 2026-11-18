package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.client.PoweredToolPreview;
import io.github.jimbozoomer.jugcraft.tools.*;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;

/** Ray-picked previews plus native attack packets against a tree, leaf-decorated timber and a treehouse. */
public final class ChainsawTreeClientGameTests implements FabricClientGameTest {
    private static final BlockPos ROOT=new BlockPos(0,100,0);
    @Override public void runTest(ClientGameTestContext context) {
        try(var world=context.worldBuilder().create()) {
            world.getConnection().waitForChunksRender();
            world.getServer().runCommand("gamemode survival @p");
            for(int type=0;type<4;type++) {
                final int kind=type, count=type==0?6:1, height=type==3?3:5;
                world.getServer().runOnServer(server->{
                    var p=server.getPlayerList().getPlayers().getFirst();var level=p.level();
                    for(var at:BlockPos.betweenClosed(ROOT.offset(-3,0,-3),ROOT.offset(3,7,3)))level.setBlockAndUpdate(at,Blocks.AIR.defaultBlockState());
                    level.setBlockAndUpdate(ROOT.below(),Blocks.DIRT.defaultBlockState());
                    for(int y=0;y<height;y++)level.setBlockAndUpdate(ROOT.above(y),Blocks.OAK_LOG.defaultBlockState());
                    if(kind!=3)level.setBlockAndUpdate(ROOT.offset(1,3,1),Blocks.OAK_LOG.defaultBlockState());
                    for(int x=-2;x<=2;x++)for(int y=height-2;y<=height;y++)for(int z=-2;z<=2;z++) {
                        var at=ROOT.offset(x,y,z);
                        if(!level.getBlockState(at).is(BlockTags.LOGS))level.setBlockAndUpdate(at,Blocks.OAK_LEAVES.defaultBlockState()
                            .setValue(LeavesBlock.PERSISTENT,kind==1).setValue(LeavesBlock.DISTANCE,1));
                    }
                    if(kind==2)level.setBlockAndUpdate(ROOT.offset(1,2,0),Blocks.OAK_PLANKS.defaultBlockState());
                    if(kind==3)level.setBlockAndUpdate(ROOT.above().east(),Blocks.OAK_LOG.defaultBlockState().setValue(BlockStateProperties.AXIS,net.minecraft.core.Direction.Axis.X));
                    for(int x=-1;x<=1;x++)for(int z=3;z<=5;z++)level.setBlockAndUpdate(new BlockPos(x,99,z),Blocks.STONE.defaultBlockState());
                    var saw=new ItemStack(JugcraftTools.CHAINSAW);Chargeable.setEnergy(saw,1000);p.setItemInHand(InteractionHand.MAIN_HAND,saw);
                });
                world.getServer().runCommand("tp @p 0.5 100 4.5 180 "+(kind==3?23.5:15));
                context.waitFor(client->Math.abs(client.player.getX()-.5)<.05&&Math.abs(client.player.getY()-100)<.05
                    &&client.hitResult instanceof BlockHitResult hit&&hit.getBlockPos().equals(ROOT)
                    &&client.player.getMainHandItem().is(JugcraftTools.CHAINSAW)&&Chargeable.energy(client.player.getMainHandItem())==1000
                    &&client.level.getBlockState(ROOT.above(height-1).north()).hasProperty(LeavesBlock.PERSISTENT)
                    &&client.level.getBlockState(ROOT.above(height-1).north()).getValue(LeavesBlock.PERSISTENT)==(kind==1)
                    &&(kind!=2||client.level.getBlockState(ROOT.offset(1,2,0)).is(Blocks.OAK_PLANKS))
                    &&(kind!=3||client.level.getBlockState(ROOT.above().east()).is(Blocks.OAK_LOG))
                    &&PoweredToolPreview.targetCount()==count,100);
                context.waitTicks(3);
                context.runOnClient(client->{
                    if(kind!=0&&!PoweredToolPreview.targets().equals(java.util.List.of(ROOT)))throw new AssertionError("Building has a bulk outline");
                    client.gui.toastManager().clear();
                });
                context.getInput().pressKey(o->o.keyToggleGui);context.waitTicks(2);
                context.takeScreenshot(new String[]{"jugcraft_chainsaw_natural_tree","jugcraft_chainsaw_decorative_timber","jugcraft_chainsaw_treehouse","jugcraft_chainsaw_low_beam"}[kind]);
                context.getInput().pressKey(o->o.keyToggleGui);context.waitTicks(2);
                context.getInput().holdKey(o->o.keyAttack);
                try {context.waitFor(client->client.level.getBlockState(ROOT).isAir()&&Chargeable.energy(client.player.getMainHandItem())==1000-40L*count,100);}
                finally{context.getInput().releaseKey(o->o.keyAttack);}
                world.getServer().runOnServer(server->{
                    var p=server.getPlayerList().getPlayers().getFirst();var level=p.level();
                    for(int y=1;y<height;y++)if(level.getBlockState(ROOT.above(y)).isAir()!=(kind==0))throw new AssertionError("Tree/building cut differs from preview");
                    if(kind!=3&&level.getBlockState(ROOT.offset(1,3,1)).isAir()!=(kind==0))throw new AssertionError("Branch/building timber was cut incorrectly");
                    if(kind==3&&!level.getBlockState(ROOT.above().east()).is(Blocks.OAK_LOG))throw new AssertionError("Low beam removed");
                    if(kind==2&&!level.getBlockState(ROOT.offset(1,2,0)).is(Blocks.OAK_PLANKS))throw new AssertionError("Treehouse plank removed");
                    if(Chargeable.energy(p.getMainHandItem())!=1000-40L*count)throw new AssertionError("Incorrect real-client JE debit");
                });
            }
        }
    }
}

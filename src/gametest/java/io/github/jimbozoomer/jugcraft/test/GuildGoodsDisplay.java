package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.agriculture.FoodServings;
import io.github.jimbozoomer.jugcraft.town.DeepsteadWares;
import io.github.jimbozoomer.jugcraft.town.TownShops;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.data.AtlasIds;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;

/** A counter display of each entire sale catalogue using the actual client models and textures. */
final class GuildGoodsDisplay {
    private static Block displayBlock(TownShops.Offer offer) {
        return offer.itemType() instanceof BlockItem item ? item.getBlock() : FoodServings.entries().get(offer.itemType());
    }

    static void capture(ClientGameTestContext context,TestSingleplayerContext world) {
        var server=world.getServer();
        var catalogs=List.of(DeepsteadWares.STONEWRIGHT,DeepsteadWares.COPPER_KETTLE);
        server.runCommand("forceload add 432 400 460 416");
        server.runCommand("tp @p 437 100 410 180 7");
        server.waitFor(s->s.overworld().isPositionEntityTicking(new BlockPos(456,100,406)),600);
        context.runOnClient(mc->{
            var models=mc.getModelManager().getBlockStateModelSet();
            var missing=mc.getAtlasManager().getAtlasOrThrow(AtlasIds.BLOCKS).missingSprite();
            for(String key:catalogs) for(var offer:TownShops.get().shops.get(key).sells()) {
                var stack=offer.stack();
                if(stack.isEmpty()) throw new AssertionError("Missing shop item "+offer.item());
                for(var mode:List.of(net.minecraft.world.item.ItemDisplayContext.GUI,
                        net.minecraft.world.item.ItemDisplayContext.FIRST_PERSON_RIGHT_HAND,
                        net.minecraft.world.item.ItemDisplayContext.FIXED)) {
                    var state=new net.minecraft.client.renderer.item.ItemStackRenderState();
                    mc.getItemModelResolver().updateForTopItem(state,stack,mode,mc.level,mc.player,0);
                    if(state.isEmpty()) throw new AssertionError("Empty "+mode+" model for "+offer.item());
                }
                var block=displayBlock(offer);
                if(block==null) continue;
                for(var bs:block.getStateDefinition().getPossibleStates()) {
                    var model=models.get(bs);
                    if(model==models.missingModel()) throw new AssertionError("Missing guild model "+bs);
                    var parts=new ArrayList<BlockStateModelPart>();
                    model.collectParts(RandomSource.create(4),parts);
                    for(var part:parts) for(int i=-1;i<6;i++)
                        for(var quad:part.getQuads(i<0?null:Direction.values()[i]))
                            if(quad.materialInfo().sprite()==missing) throw new AssertionError("Missing guild texture "+bs);
                }
            }
        });
        for(int bay=0;bay<catalogs.size();bay++) {
            int x=434+bay*16;
            String guild=catalogs.get(bay);
            String backdrop=bay==0?"polished_deepslate":"dark_oak_planks";
            server.runCommand("fill "+(x-2)+" 99 399 "+(x+7)+" 105 409 "+backdrop+" hollow");
            server.runCommand("fill "+(x-2)+" 100 409 "+(x+7)+" 104 409 air");
            server.runCommand("fill "+(x-1)+" 100 402 "+(x+6)+" 100 403 spruce_planks");
            server.runCommand("fill "+(x-1)+" 101 400 "+(x+6)+" 103 400 "+backdrop);
            server.runCommand("fill "+(x-1)+" 104 402 "+(x+6)+" 104 402 minecraft:light[level=15]");
            server.runCommand("fill "+(x-1)+" 102 405 "+(x+6)+" 102 405 minecraft:light[level=15]");
            boolean mason=bay==0;
            server.runOnServer(s->{
                var level=s.overworld();
                int front=0,back=0;
                for(var offer:TownShops.get().shops.get(guild).sells()) {
                    var block=displayBlock(offer);
                    if(block==null) {
                        var frame=new ItemFrame(level,new BlockPos(x+1+back++,103,401),Direction.SOUTH);
                        frame.setItem(offer.stack().copyWithCount(1),false);
                        if(!level.addFreshEntity(frame)) throw new AssertionError("Could not hang "+offer.item());
                    } else {
                        boolean rear=!mason && offer.itemType() instanceof BlockItem;
                        var pos=rear?new BlockPos(x+1+back++,102,401):new BlockPos(x+front++,101,403);
                        if(rear) level.setBlockAndUpdate(pos.below(),net.minecraft.world.level.block.Blocks.POLISHED_DEEPSLATE.defaultBlockState());
                        var placed=block.defaultBlockState();
                        var facing=net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING;
                        if(placed.hasProperty(facing)) placed=placed.setValue(facing,Direction.SOUTH);
                        level.setBlockAndUpdate(pos,placed);
                    }
                }
                if(front!=6 || back!=4) throw new AssertionError("Incomplete "+guild+" counter display");
            });
            server.runCommand("tp @p "+(x+3)+" 102.1 407.7 180 18");
            context.waitTicks(45); world.getConnection().waitForChunksRender(false,600);
            context.takeScreenshot("wares_"+guild);
            for(int half=0;half<2;half++) {
                // Read the carving, grain and plated ingredients at the distance of a player at the counter.
                double centre=x+1.5+half*3;
                server.runCommand("tp @p "+centre+" 101.25 405.65 180 25");
                context.waitTicks(12);
                context.takeScreenshot("wares_"+guild+"_counter_"+half);
            }
            if(mason) {
                server.runCommand("tp @p "+(x+3)+" 102.25 403.85 180 8");
                context.waitTicks(12);
                context.takeScreenshot("wares_guild_instruments");
            }
        }
        server.runOnServer(s->{
            var inventory=s.getPlayerList().getPlayers().getFirst().getInventory();
            int slot=9;
            for(String guild:catalogs) for(var offer:TownShops.get().shops.get(guild).sells())
                inventory.setItem(slot++,offer.stack().copyWithCount(1));
        });
        context.waitTicks(8);
        context.setScreen(()->new net.minecraft.client.gui.screens.inventory.InventoryScreen(
                net.minecraft.client.Minecraft.getInstance().player));
        context.waitTicks(8);
        context.takeScreenshot("wares_guild_inventory");
        context.setScreen(()->null);
    }
}

package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.town.*;
import io.github.jimbozoomer.jugcraft.client.CommissionScreen;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.*;

/** Uses an isolated test world, never the owner's launcher instance. */
public class LivingCityClientGameTests implements FabricClientGameTest {
    @Override public void runTest(ClientGameTestContext context){
        try(var world=context.worldBuilder().create()){
            world.getConnection().waitForChunksRender();var server=world.getServer();
            server.runOnServer(s->{
                var player=s.getPlayerList().getPlayers().getFirst();var pos=player.blockPosition().east(2);
                player.level().setBlockAndUpdate(pos,JugcraftTown.COMMISSION_BOARD.defaultBlockState());
                JugcraftTown.COMMISSION_BOARD.setPlacedBy(player.level(),pos,JugcraftTown.COMMISSION_BOARD.defaultBlockState(),player,new ItemStack(JugcraftTown.COMMISSION_BOARD));
                player.getInventory().setItem(0,new ItemStack(Items.WHEAT,32));
                CommissionMenu.open(player,pos);
            });
            context.waitTicks(15);
            context.runOnClient(c->{if(!(c.gui.screen() instanceof CommissionScreen))throw new AssertionError("Commission screen did not open");
                c.gameMode.handleInventoryButtonClick(c.player.containerMenu.containerId,0);});
            context.waitTicks(10);
            context.runOnClient(c->{var menu=(CommissionMenu)c.player.containerMenu;
                if(menu.progress(0)!=32||menu.paid(0))throw new AssertionError("Delivery preview did not synchronize");});
            context.takeScreenshot("living_city_commission_ready");
            context.runOnClient(c->c.gameMode.handleInventoryButtonClick(c.player.containerMenu.containerId,4));
            context.waitTicks(10);
            context.runOnClient(c->{var menu=(CommissionMenu)c.player.containerMenu;
                if(!menu.paid(0)||menu.balance()<1)throw new AssertionError("Reward did not synchronize");});
            context.takeScreenshot("living_city_commission_collected");
            context.runOnClient(c->c.player.closeContainer());

            // Real synchronized resident poses in a disposable gallery; the server suite tests pathfinding separately.
            long[] clock={6000};
            var residents=new java.util.ArrayList<Townsfolk>();
            server.runCommand("time set noon");
            server.runCommand("weather clear");
            server.runOnServer(s->{
                var player=s.getPlayerList().getPlayers().getFirst();var level=s.overworld();
                var centre=player.blockPosition().atY(150);
                for(int x=-12;x<=12;x++)for(int z=-2;z<=20;z++)
                    level.setBlockAndUpdate(centre.offset(x,-1,z),net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
                String[] jobs={"gardener","sweeper","busker","porter","innkeeper","watch"};
                for(int i=0;i<jobs.length;i++){
                    String job=jobs[i];var at=centre.offset(-10+i*4,0,0);
                    var routine=new TownRoutine.Places(job,"fountain",at,at.south(5),at,at.south(5));
                    var resident=new Townsfolk(JugcraftTown.TOWNSFOLK,level){
                        @Override protected long scheduleTime(){return clock[0];}
                        @Override protected TownRoutine.Places places(){return routine;}
                        @Override public int placeRange(){return 0;}
                    };
                    resident.setUp(new TownData.Spot(-1,job.equals("watch")?"guard":job.equals("innkeeper")?"innkeeper":"townsfolk",null,job,"townsfolk_1",BlockPos.ZERO),at);
                    resident.snapTo(at.getX()+.5,at.getY(),at.getZ()+.5,0,0);
                    level.addFreshEntity(resident);residents.add(resident);
                }
                player.teleportTo(centre.getX()+.5,centre.getY(),centre.getZ()+18.5);
                player.setYRot(180);player.setXRot(0);
            });
            context.runOnClient(c->{c.player.setYRot(180);c.player.setXRot(0);if(!c.gui.hud.isHidden())c.gui.hud.toggle();});
            world.getConnection().waitForChunksRender();
            context.waitTicks(80);
            server.runOnServer(s->{for(var resident:residents)
                if(resident.activity()==TownRoutine.Activity.NONE)throw new AssertionError("Work pose missing: "+resident.getName().getString());});
            context.takeScreenshot("living_city_daytime_activities");
            server.runOnServer(s->clock[0]=13000);
            context.waitTicks(60);
            context.takeScreenshot("living_city_evening_activities");
            server.runOnServer(s->clock[0]=19000);
            context.waitTicks(60);
            server.runOnServer(s->{for(var resident:residents)
                if(!resident.isGuard()&&resident.activity()!=TownRoutine.Activity.DOZE)throw new AssertionError("Night rest pose missing");});
            context.takeScreenshot("living_city_night_rest");
            server.runOnServer(s->{
                var player=s.getPlayerList().getPlayers().getFirst();var keeper=residents.get(4);
                player.teleportTo(keeper.getX(),keeper.getY(),keeper.getZ()+2);
                keeper.talkTo(player,net.minecraft.world.InteractionHand.MAIN_HAND);
            });
            context.waitTicks(15);
            context.runOnClient(c->{
                if(c.gui.hud.isHidden())c.gui.hud.toggle();
                if(!(c.player.containerMenu instanceof ShopMenu shop)||!shop.purchases().isEmpty()
                    ||shop.sales().size()!=TownShops.get().essentialsShop().essentials().size())
                    throw new AssertionError("Night essentials counter did not synchronize");
            });
            context.takeScreenshot("living_city_night_essentials");
            context.runOnClient(c->c.player.closeContainer());
            server.runOnServer(s->residents.forEach(Townsfolk::discard));
            // Keep each saved seasonal state while checking its actual rendered foliage.
            server.runCommand("gamerule minecraft:random_tick_speed 0");
            server.runOnServer(s->{
                var player=s.getPlayerList().getPlayers().getFirst();var level=s.overworld();
                var at=player.blockPosition().offset(0,0,10);
                var leaves=io.github.jimbozoomer.jugcraft.agriculture.JugcraftAgriculture.block("aspen_leaves")
                    .defaultBlockState().setValue(net.minecraft.world.level.block.LeavesBlock.PERSISTENT,true);
                var seasons=io.github.jimbozoomer.jugcraft.agriculture.SeasonalLeavesBlock.Foliage.values();
                for(int x=-2;x<13;x++)for(int z=-2;z<11;z++)
                    level.setBlockAndUpdate(at.offset(x,-1,z),net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
                // onPlace applies today's season; set the gallery state AFTER that first placement.
                for(int i=0;i<3;i++)for(int x=0;x<2;x++)for(int y=0;y<3;y++){
                    var pos=at.offset(i*4+x,y,0);
                    level.setBlockAndUpdate(pos,leaves);
                    level.setBlockAndUpdate(pos,leaves.setValue(
                        io.github.jimbozoomer.jugcraft.agriculture.SeasonalLeavesBlock.SEASON,seasons[i]));
                    if(level.getBlockState(pos).getValue(io.github.jimbozoomer.jugcraft.agriculture.SeasonalLeavesBlock.SEASON)!=seasons[i])
                        throw new AssertionError("Aspen gallery did not retain its explicit seasonal state");
                }
                player.teleportTo(at.getX()+4.5,at.getY()+1,at.getZ()+8.5);
                player.setYRot(180);player.setXRot(0);
            });
            context.runOnClient(c->{c.player.setYRot(180);c.player.setXRot(0);if(!c.gui.hud.isHidden())c.gui.hud.toggle();});
            world.getConnection().waitForChunksRender();context.waitTicks(10);
            context.takeScreenshot("living_city_aspen_all_seasons");
            context.runOnClient(c->{if(c.gui.hud.isHidden())c.gui.hud.toggle();});
        }
    }
}

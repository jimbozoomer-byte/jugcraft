package io.github.jimbozoomer.jugcraft.test;
import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.client.*;
import io.github.jimbozoomer.jugcraft.chemistry.PetroFluids;
import io.github.jimbozoomer.jugcraft.energy.SimpleEnergyStorage;
import io.github.jimbozoomer.jugcraft.machine.*;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluids;
import io.github.jimbozoomer.jugcraft.concordance.ConcordanceProgress;
import io.github.jimbozoomer.jugcraft.concordance.rules.ResearchState;

public class EncyclopediaClientGameTests implements FabricClientGameTest {
    @Override public void runTest(ClientGameTestContext context){
        try(var world=context.worldBuilder().adjustSettings(c->c.setGameMode(WorldCreationUiState.SelectedGameMode.CREATIVE)).create()){
            world.getConnection().waitForChunksRender();var server=world.getServer();
            context.getInput().pressKey(o->EncyclopediaClient.key());
            context.waitFor(c->c.gui.screen() instanceof EncyclopediaScreen);
            context.waitFor(c->EncyclopediaClient.progress("jugcraft:encyclopedia_hydrochloric_acid_bucket")!=null);
            context.takeScreenshot("encyclopedia_home");
            for(String group:java.util.List.of("Technology","Magic","Goals")){
                context.clickScreenButton(group);context.waitTicks(2);context.takeScreenshot("encyclopedia_"+group.toLowerCase());
            }
            context.runOnClient(c->{var s=(EncyclopediaScreen)c.gui.screen();s.searchFor("sulfur trioxide");if(s.resultCount()<1)throw new AssertionError("Acid search missing");});
            context.takeScreenshot("encyclopedia_acid_search");
            context.runOnClient(c->((EncyclopediaScreen)c.gui.screen()).openEntry("guide/acid"));
            context.waitFor(c->c.gui.screen() instanceof HandbookScreen);context.takeScreenshot("encyclopedia_acid_guide");
            context.runOnClient(c->c.gui.screen().onClose());context.clickScreenButton("Routes");
            context.waitFor(c->c.gui.screen() instanceof EncyclopediaRoutesScreen);context.takeScreenshot("encyclopedia_technology_routes");
            context.clickScreenButton("Magic");context.takeScreenshot("encyclopedia_magic_routes");
            context.clickScreenButton("Back");
            server.runOnServer(s->{var p=s.getPlayerList().getPlayers().getFirst();ConcordanceProgress.grant(p,"jugcraft:first_light",ResearchState.UNDERSTOOD);p.getAdvancements().award(s.getAdvancements().get(Jugcraft.id("encyclopedia_hydrochloric_acid_bucket")),"obtain");});
            context.waitTicks(25);context.runOnClient(c->EncyclopediaClient.request());
            context.waitFor(c->EncyclopediaClient.progress("jugcraft:encyclopedia_hydrochloric_acid_bucket").done());
            context.clickScreenButton("Goals");context.takeScreenshot("encyclopedia_goals_updated");
            context.runOnClient(c->c.gui.screen().onClose());
            server.runCommand("gamemode survival @p");
            context.waitFor(c->!c.player.isCreative());
            context.setScreen(()->new InventoryScreen(net.minecraft.client.Minecraft.getInstance().player));
            context.clickScreenButton("Jugcraft guide");context.waitFor(c->c.gui.screen() instanceof EncyclopediaScreen);
            context.clickScreenButton("Done");context.waitFor(c->c.gui.screen() instanceof InventoryScreen);
            context.setScreen(()->null);
            server.runCommand("gamemode creative @p");context.waitFor(c->c.player.isCreative());
            context.setScreen(()->new InventoryScreen(net.minecraft.client.Minecraft.getInstance().player));
            context.waitFor(c->c.gui.screen() instanceof net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen);
            context.clickScreenButton("Jugcraft guide");context.waitFor(c->c.gui.screen() instanceof EncyclopediaScreen);
            context.clickScreenButton("Done");context.waitFor(c->c.gui.screen() instanceof net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen);
            context.setScreen(()->null);
            for(int scale:java.util.List.of(2,3)){
                context.runOnClient(c->{c.options.guiScale().set(scale);c.resizeGui();EncyclopediaClient.open(null);});
                context.takeScreenshot("encyclopedia_scale_"+scale);context.clickScreenButton("Done");
            }
            // A separate saved test world with two stocked reactors and supplies, never an existing player save.
            BlockPos spawn=context.computeOnClient(c->c.player.blockPosition());
            server.runCommand("time set noon");server.runCommand("weather clear");
            server.runCommand("fill %d %d %d %d %d %d minecraft:smooth_stone".formatted(spawn.getX()-4,spawn.getY()-1,spawn.getZ()-6,spawn.getX()+12,spawn.getY()-1,spawn.getZ()+3));
            server.runCommand("fill %d %d %d %d %d %d minecraft:air".formatted(spawn.getX()-4,spawn.getY(),spawn.getZ()-6,spawn.getX()+12,spawn.getY()+5,spawn.getZ()+3));
            server.runOnServer(s->{
                var level=s.overworld();var block=JugcraftMachines.MACHINES.get(MachineKind.CHEMICAL_REACTOR);
                for(int i=0;i<2;i++){
                    BlockPos at=spawn.offset(i*6,0,-3);var state=block.defaultBlockState().setValue(MachineBlock.FACING,Direction.SOUTH);
                    level.setBlock(at,state,Block.UPDATE_ALL);block.setPlacedBy(level,at,state,null,ItemStack.EMPTY);
                    var m=(MachineBlockEntity)level.getBlockEntity(at);m.tanks().input(0).fill(Fluids.WATER,4000);
                    m.tanks().input(1).fill(i==0?PetroFluids.SULFUR_TRIOXIDE.fluid():PetroFluids.HYDROGEN_CHLORIDE.fluid(),4000);
                    if(i==0)m.tanks().input(2).fill(PetroFluids.SULFURIC_ACID.source(),1000);
                    ((SimpleEnergyStorage)m.energyFor(null)).setAmount(30000);
                    var battery=JugcraftMachines.MACHINES.get(MachineKind.BATTERY_BOX);BlockPos bp=at.offset(0,0,3);
                    var bs=battery.defaultBlockState().setValue(MachineBlock.FACING,Direction.NORTH);level.setBlock(bp,bs,Block.UPDATE_ALL);
                    ((SimpleEnergyStorage)((MachineBlockEntity)level.getBlockEntity(bp)).energyFor(null)).setAmount(200000);
                    level.setBlock(at.offset(0,0,2),JugcraftMachines.COPPER_CABLE.defaultBlockState(),Block.UPDATE_ALL);
                }
                var p=s.getPlayerList().getPlayers().getFirst();p.openMenu((MachineBlockEntity)level.getBlockEntity(spawn.offset(0,0,-3)));
                Jugcraft.LOGGER.info("Acid Encyclopedia playtest world: {}",s.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT));
            });
            context.waitFor(c->c.gui.screen() instanceof MachineScreen);context.waitTicks(10);context.takeScreenshot("acid_reactor_tanks");
            context.setScreen(()->null);context.waitTicks(5);
        }
    }
}

package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.chemistry.PetroFluids;
import io.github.jimbozoomer.jugcraft.chemistry.PetroItems;
import io.github.jimbozoomer.jugcraft.client.FormMachineScreen;
import io.github.jimbozoomer.jugcraft.energy.SimpleEnergyStorage;
import io.github.jimbozoomer.jugcraft.machine.MachineBlock;
import io.github.jimbozoomer.jugcraft.machine.form.FormMachineBlock;
import io.github.jimbozoomer.jugcraft.machine.form.FormMachineBlockEntity;
import io.github.jimbozoomer.jugcraft.machine.form.IndustrialForms;
import java.util.List;
import java.util.Locale;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.material.Fluids;

/** Actual native models and saved local showroom. Test closure is separate from the owner's later manual session. */
public class StarterChemistryClientGameTests implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext world = context.worldBuilder()
                .adjustSettings(c -> c.setGameMode(WorldCreationUiState.SelectedGameMode.CREATIVE)).create()) {
            world.getConnection().waitForChunksRender();
            var server = world.getServer();
            BlockPos spawn = context.computeOnClient(c -> BlockPos.containing(c.player.position()));
            int y = spawn.getY();
            int x = spawn.getX();
            int z = spawn.getZ();
            server.runCommand("time set noon");
            server.runCommand("weather clear");
            server.runCommand("gamerule minecraft:advance_time false");
            server.runCommand("gamerule minecraft:spawn_mobs false");
            server.runCommand("gamerule minecraft:send_command_feedback false");
            server.runCommand("fill %d %d %d %d %d %d minecraft:air".formatted(x-12,y,z-14,x+46,y+11,z+14));
            server.runCommand("fill %d %d %d %d %d %d minecraft:smooth_stone".formatted(x-12,y-2,z-14,x+46,y-1,z+14));
            List<FormMachineBlock> blocks = List.of(IndustrialForms.COAL_GASIFIER, IndustrialForms.GAS_CLEANING_RACK,
                    IndustrialForms.CHEMICAL_INFUSER, IndustrialForms.CHEMICAL_OXIDIZER, IndustrialForms.GAS_BURNING_GENERATOR);
            for (int i = 0; i < blocks.size(); i++) {
                final int which = i;
                BlockPos pos = spawn.offset(i*9,0,-4);
                FormMachineBlock block = blocks.get(i);
                server.runOnServer(s -> {
                    var level = s.overworld();
                    var state = block.defaultBlockState().setValue(MachineBlock.FACING, Direction.SOUTH);
                    level.setBlock(pos,state,Block.UPDATE_ALL);
                    block.setPlacedBy(level,pos,state,null,ItemStack.EMPTY);
                    stock(level,pos,which);
                });
            }
            context.runOnClient(c -> { if (!c.gui.hud.isHidden()) c.gui.hud.toggle(); });
            for (int i=0;i<blocks.size();i++) {
                final int which=i;
                BlockPos pos=spawn.offset(i*9,0,-4);
                double cx=pos.getX()+blocks.get(i).form().width()/2.0;
                double cz=pos.getZ()+1-blocks.get(i).form().depth()/2.0;
                double cy=y+blocks.get(i).form().height()/2.0;
                server.runOnServer(s -> { stock(s.overworld(),pos,which); machine(s.overworld(),pos).togglePause(); });
                double dx=blocks.get(i).form().height()>=4?5:3.2;
                double dz=blocks.get(i).form().height()>=4?7:4.8;
                int cameraY=y+(blocks.get(i).form().height()>=4?2:1);
                context.waitTicks(80); // Includes the light hold and the longest rotor spin-down.
                shoot(context,world,cx-dx,cameraY,cz+dz,cx,cy,cz,"starter_"+blocks.get(i).form().id().getPath()+"_idle");
                float idle=angle(context,pos);
                context.waitTicks(7);
                if(Math.abs(angle(context,pos)-idle)>.01f) throw new AssertionError("Paused machine still rotates: "+blocks.get(i).form().id());
                server.runOnServer(s -> machine(s.overworld(),pos).togglePause());
                for (int view=0;view<4;view++) {
                    server.runOnServer(s -> stock(s.overworld(),pos,which));
                    double[][] at={{cx-dx,cz+dz},{cx+dx,cz+dz},{cx+dx,cz-dz},{cx-dx,cz-dz}};
                    shoot(context,world,at[view][0],cameraY,at[view][1],cx,cy,cz,"starter_"+blocks.get(i).form().id().getPath()+"_working_"+view);
                    if(view==0) {
                        float moving=angle(context,pos);
                        context.waitTicks(7);
                        if(Math.abs(angle(context,pos)-moving)<.1f) throw new AssertionError("Working machine has no movement: "+blocks.get(i).form().id());
                    }
                }
            }
            // Open the Infuser's real server menu. The water switch and protected catalyst sit below its terminal.
            BlockPos infuser=spawn.offset(18,0,-4);
            server.runCommand("tp @p %d %d %d 180 0".formatted(infuser.getX(),y,infuser.getZ()+3));
            context.waitTicks(3);
            server.runOnServer(s -> {
                var m=machine(s.overworld(),infuser);
                s.getPlayerList().getPlayers().getFirst().openMenu(m);
            });
            context.waitForScreen(FormMachineScreen.class);
            context.waitTicks(5);
            context.takeScreenshot("starter_infuser_screen");
            context.setScreen(() -> null);
            // End with a stocked, paused factory so the owner can start each machine when ready.
            server.runOnServer(s -> {
                for(int i=0;i<blocks.size();i++) {
                    BlockPos pos=spawn.offset(i*9,0,-4);
                    var m=machine(s.overworld(),pos);
                    for(int tank=0;tank<m.form().tanks().inputTanks().size();tank++) {
                        var input=m.tanks().input(tank);
                        input.drain(input.millibuckets());
                    }
                    m.cancel();
                    stock(s.overworld(),pos,i);
                    if(!m.paused()) m.togglePause();
                    // Ordinary charged battery and copper cable, so the owner has sustained test power.
                    var port=m.form().ports().stream().filter(p -> p.kind().energy()).findFirst().orElseThrow();
                    BlockPos at=m.form().cellPos(pos,Direction.SOUTH,m.form().cellIndex(port.column(),port.row(),port.layer()));
                    Direction side=port.side().world(Direction.SOUTH);
                    var battery=io.github.jimbozoomer.jugcraft.machine.JugcraftMachines.MACHINES.get(io.github.jimbozoomer.jugcraft.machine.MachineKind.BATTERY_BOX);
                    var batteryState=battery.defaultBlockState().setValue(MachineBlock.FACING, m.form().generator()?side:side.getOpposite());
                    BlockPos batteryPos=at.relative(side,2);
                    s.overworld().setBlock(batteryPos,batteryState,Block.UPDATE_ALL);
                    battery.setPlacedBy(s.overworld(),batteryPos,batteryState,null,ItemStack.EMPTY);
                    s.overworld().setBlock(at.relative(side),io.github.jimbozoomer.jugcraft.machine.JugcraftMachines.COPPER_CABLE.defaultBlockState(),Block.UPDATE_ALL);
                    if(s.overworld().getBlockEntity(batteryPos) instanceof io.github.jimbozoomer.jugcraft.machine.MachineBlockEntity box) {
                        var energy=io.github.jimbozoomer.jugcraft.energy.EnergyStorage.SIDED.find(s.overworld(),batteryPos,null);
                        if(energy instanceof SimpleEnergyStorage store) store.setAmount(m.form().generator()?0:store.getCapacity());
                    }
                }
                BlockPos chest=spawn.offset(18,0,3);
                s.overworld().setBlock(chest,Blocks.CHEST.defaultBlockState(),Block.UPDATE_ALL);
                if(s.overworld().getBlockEntity(chest) instanceof ChestBlockEntity box) {
                    int slot=0;
                    for(var block:blocks) box.setItem(slot++,new ItemStack(block));
                    box.setItem(slot++,new ItemStack(PetroItems.NICKEL_CATALYST_BED,4));
                    box.setItem(slot++,new ItemStack(PetroItems.VANADIUM_CONTACT_BED,4));
                    box.setItem(slot++,new ItemStack(Items.COAL,64));
                    box.setItem(slot++,new ItemStack(Items.CHARCOAL,64));
                    box.setItem(slot++,new ItemStack(Items.WATER_BUCKET));
                    box.setItem(slot++,new ItemStack(Items.GLASS,64));
                    for(var gas:List.of(PetroFluids.CARBON_MONOXIDE,PetroFluids.CARBON_DIOXIDE,PetroFluids.HYDROGEN,PetroFluids.CHLORINE,
                            PetroFluids.OXYGEN,PetroFluids.DIRTY_SYNTHESIS_GAS,PetroFluids.METHANE,PetroFluids.SULFUR_DIOXIDE,PetroFluids.SULFUR_TRIOXIDE,PetroFluids.HYDROGEN_CHLORIDE)) {
                        box.setItem(slot++,io.github.jimbozoomer.jugcraft.fluid.GasCylinderItem.filled(new ItemStack(io.github.jimbozoomer.jugcraft.fluid.JugcraftFluids.GAS_CYLINDER),
                                net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant.of(gas.fluid()),8000L*io.github.jimbozoomer.jugcraft.fluid.FluidNetworks.DROPLETS_PER_MB));
                    }
                    box.setItem(slot++,new ItemStack(io.github.jimbozoomer.jugcraft.fluid.JugcraftFluids.GAS_CYLINDER));
                    box.setItem(slot++,new ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(io.github.jimbozoomer.jugcraft.Jugcraft.id("sulfur_dust")),64));
                    box.setItem(slot,new ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(io.github.jimbozoomer.jugcraft.Jugcraft.id("coke")),64));
                }
                io.github.jimbozoomer.jugcraft.Jugcraft.LOGGER.info("Starter chemistry manual-test world: {}",s.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT));
            });
            server.runCommand("fill %d %d %d %d %d %d minecraft:air replace minecraft:barrier".formatted(x-12,y,z-14,x+46,y+11,z+14));
            context.runOnClient(c -> { if(c.gui.hud.isHidden()) c.gui.hud.toggle(); });
            server.runCommand("setworldspawn %d %d %d".formatted(x+18,y,z+8));
            server.runCommand("tp @p %d %d %d 180 0".formatted(x+18,y,z+8));
            context.waitTicks(10);
        }
    }
    private static FormMachineBlockEntity machine(ServerLevel level,BlockPos p) { return (FormMachineBlockEntity)level.getBlockEntity(p); }
    private static float angle(ClientGameTestContext context,BlockPos pos) {
        return context.computeOnClient(c -> io.github.jimbozoomer.jugcraft.client.MachineRotors.extract(c.level.getBlockEntity(pos),0).getFirst().angle());
    }
    private static void stock(ServerLevel level,BlockPos p,int index) {
        var m=machine(level,p);
        ((SimpleEnergyStorage)m.energy()).setAmount(m.energy().getCapacity());
        for(int i=0;i<m.form().tanks().outputTanks().size();i++) m.tanks().output(i).drain(m.tanks().output(i).millibuckets());
        if(m.form().itemOutputs()>0) m.container().setItem(m.form().firstOutputSlot(),ItemStack.EMPTY);
        switch(index) {
            case 0 -> { m.container().setItem(0,new ItemStack(Items.COAL,16)); topUp(m.tanks().input(0),Fluids.WATER); }
            case 1 -> { topUp(m.tanks().input(0),PetroFluids.DIRTY_SYNTHESIS_GAS.fluid()); topUp(m.tanks().input(1),Fluids.WATER); }
            case 2 -> { m.container().setItem(m.form().firstSocketSlot(),new ItemStack(PetroItems.NICKEL_CATALYST_BED)); topUp(m.tanks().input(0),PetroFluids.CARBON_MONOXIDE.fluid()); topUp(m.tanks().input(1),PetroFluids.HYDROGEN.fluid()); }
            case 3 -> { m.container().setItem(0,new ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(io.github.jimbozoomer.jugcraft.Jugcraft.id("sulfur_dust")),16)); topUp(m.tanks().input(0),PetroFluids.OXYGEN.fluid()); }
            case 4 -> { ((SimpleEnergyStorage)m.energy()).setAmount(0); topUp(m.tanks().input(0),PetroFluids.METHANE.fluid()); }
        }
    }
    private static void topUp(io.github.jimbozoomer.jugcraft.chemistry.FluidTank tank,net.minecraft.world.level.material.Fluid fluid) {
        int needed=Math.min(2000,tank.capacityMb())-tank.millibuckets();
        if(needed<0 || !tank.fits(fluid,Math.max(0,needed))) throw new AssertionError("Invalid showroom tank contents");
        if(needed>0) tank.fill(fluid,needed);
    }
    private static void shoot(ClientGameTestContext c,TestSingleplayerContext w,double x,int y,double z,double tx,double ty,double tz,String name) {
        w.getServer().runCommand("setblock %d %d %d minecraft:barrier".formatted((int)Math.floor(x),y-1,(int)Math.floor(z)));
        w.getServer().runCommand(String.format(Locale.ROOT,"tp @p %.2f %d %.2f facing %.2f %.2f %.2f",x,y,z,tx,ty-1.62,tz));
        c.waitTicks(15);
        w.getConnection().waitForChunksRender();
        c.takeScreenshot(name);
    }
}

package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.client.MachineScreen;
import io.github.jimbozoomer.jugcraft.machine.*;
import java.util.List;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;

/** Rendered network-synchronized three-output reactor and all seven new materials/tools. */
public class MineralSandProcessingClientGameTests implements FabricClientGameTest {
    @Override public void runTest(ClientGameTestContext context) {
        try (var world = context.worldBuilder()
                .adjustSettings(c -> c.setGameMode(WorldCreationUiState.SelectedGameMode.CREATIVE)).create()) {
            world.getConnection().waitForChunksRender();
            var server = world.getServer();
            var origin = context.computeOnClient(c -> BlockPos.containing(c.player.position()));
            var at = new BlockPos(origin.getX(), 85, origin.getZ());
            server.runCommand("time set noon");
            server.runCommand("weather clear");
            server.runCommand("gamerule minecraft:spawn_mobs false");
            server.runCommand("fill %d %d %d %d %d %d minecraft:air".formatted(at.getX()-3,84,at.getZ()-3,at.getX()+6,90,at.getZ()+6));
            server.runCommand("fill %d %d %d %d %d %d minecraft:smooth_stone".formatted(at.getX()-3,84,at.getZ()-3,at.getX()+6,84,at.getZ()+6));
            server.runCommand("tp @p %d %d %d 180 0".formatted(at.getX(),85,at.getZ()+3));
            server.runOnServer(s -> {
                var level = s.overworld();
                var block = JugcraftMachines.MACHINES.get(MachineKind.CHEMICAL_REACTOR);
                var state = block.defaultBlockState().setValue(MachineBlock.FACING, Direction.SOUTH);
                level.setBlock(at, state, Block.UPDATE_ALL);
                block.setPlacedBy(level, at, state, null, ItemStack.EMPTY);
                var machine = (MachineBlockEntity)level.getBlockEntity(at);
                machine.setItem(2, stack("neodymium_oxide", 1));
                machine.setItem(3, stack("cerium_oxide", 2));
                machine.setItem(4, stack("yttrium_oxide", 1));
                s.getPlayerList().getPlayers().getFirst().openMenu(machine);
            });
            context.waitForScreen(MachineScreen.class);
            context.waitTicks(8);
            context.runOnClient(c -> {
                if (!(c.player.containerMenu instanceof MachineMenu menu)) throw new AssertionError("Native reactor menu missing");
                for (int i = 0; i < 3; i++) {
                    var slot = menu.slots.get(2+i);
                    if (!slot.getItem().is(stack(List.of("neodymium_oxide","cerium_oxide","yttrium_oxide").get(i),1).getItem()))
                        throw new AssertionError("Coproduct did not synchronize: " + i);
                    if (slot.x < 0 || slot.x + 16 > 176 || slot.y < 0 || slot.y + 16 > 82)
                        throw new AssertionError("Coproduct slot outside native processing panel");
                    if (slot.mayPlace(stack("garnet_grit", 1))) throw new AssertionError("Output slot accepts player feed");
                    for (int j = 0; j < i; j++) {
                        var other = menu.slots.get(2+j);
                        if (slot.x < other.x+16 && slot.x+16 > other.x && slot.y < other.y+16 && slot.y+16 > other.y)
                            throw new AssertionError("Coproduct output slots overlap");
                    }
                }
                if (menu.slots.get(3).getItem().getCount()!=2) throw new AssertionError("Successful cerium roll quantity changed in sync");
            });
            context.takeScreenshot("mineral_sands_reactor_three_coproducts");
            context.setScreen(() -> null);
            server.runOnServer(s -> {
                var pos = at.offset(3,0,0);
                s.overworld().setBlock(pos, Blocks.CHEST.defaultBlockState(), Block.UPDATE_ALL);
                var chest = (ChestBlockEntity)s.overworld().getBlockEntity(pos);
                var ids = List.of("neodymium_oxide","cerium_oxide","yttrium_oxide","garnet_grit",
                    "cerium_polishing_head","garnet_abrasive_head","garnet_sanding_block");
                for (int i=0;i<ids.size();i++) chest.setItem(i,stack(ids.get(i),1));
                s.getPlayerList().getPlayers().getFirst().openMenu(chest);
            });
            context.waitTicks(8);
            context.takeScreenshot("mineral_sands_materials_and_reusable_tools");
        }
    }

    private static ItemStack stack(String id, int count) {
        return new ItemStack(BuiltInRegistries.ITEM.getValue(Jugcraft.id(id)), count);
    }
}

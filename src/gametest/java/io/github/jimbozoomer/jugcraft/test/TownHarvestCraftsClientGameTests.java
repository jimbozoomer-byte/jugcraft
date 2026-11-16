package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.town.TownBuilder;
import io.github.jimbozoomer.jugcraft.town.TownData;
import io.github.jimbozoomer.jugcraft.town.TownFeast;
import io.github.jimbozoomer.jugcraft.town.TownHarvestCrafts;
import java.time.LocalDate;
import java.util.Locale;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** Photographs the actual town-blueprint neighbourhoods, not generic display rows, in a disposable world. */
public final class TownHarvestCraftsClientGameTests implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        boolean hidden = context.computeOnClient(c -> c.gui.hud.isHidden());
        try (TestSingleplayerContext world = context.worldBuilder().adjustSettings(c ->
                c.setGameMode(WorldCreationUiState.SelectedGameMode.CREATIVE)).create()) {
            world.getConnection().waitForChunksRender();
            var server = world.getServer();
            BlockPos origin = context.computeOnClient(c -> c.player.blockPosition().offset(-96, -1, -96));
            server.runCommand("gamerule minecraft:send_command_feedback false");
            server.runCommand("gamerule minecraft:advance_time false");
            server.runCommand("gamerule minecraft:spawn_mobs false");
            server.runCommand("weather clear");
            server.runCommand("time set noon");
            server.runCommand("gamemode spectator @a");
            server.runOnServer(mc -> {
                var level = mc.overworld();
                var data = TownData.get();
                // An exact rectangular excerpt of town, including its church, inn and main square.
                // Nothing from the owner's saved city is read or modified.
                for (int x = 70; x <= 124; x++) for (int z = 60; z <= 132; z++) for (int y = 0; y <= 42; y++) {
                    int index = data.index(x, y, z);
                    TownBuilder.set(level, origin.offset(x, y, z), index == TownData.KEEP ? Blocks.AIR.defaultBlockState() : data.state(index));
                }
                TownFeast.Baseline baseline = new TownFeast.Baseline() {
                    public BlockState expected(BlockPos p) {
                        int index = data.index(p.getX(), p.getY(), p.getZ());
                        return index == TownData.KEEP ? null : data.state(index);
                    }
                    public boolean preserved(BlockPos p) { return false; }
                };
                var ledger = new TownHarvestCrafts.Ledger();
                var result = TownHarvestCrafts.run(level, origin, "harvest", LocalDate.of(2026, 11, 14),
                        baseline, ledger, TownHarvestCrafts.LAYOUT, false);
                if (ledger.entries().stream().filter(e -> e.status().equals("placed")).count() != TownHarvestCrafts.LAYOUT.size()) {
                    throw new AssertionError("Town harvest scenes did not all fit: " + result);
                }
            });
            context.runOnClient(c -> { if (!c.gui.hud.isHidden()) c.gui.hud.toggle(); });
            context.waitTicks(30);
            shoot(context, world, origin, 96, 20, 110, 180, 32, "harvest_town_neighbourhood_overview");
            shoot(context, world, origin, 80, 2, 85, 180, 15, "harvest_town_woodland_front");
            shoot(context, world, origin, 76, 2, 84, 225, 18, "harvest_town_woodland_side");
            shoot(context, world, origin, 113, 2, 88, 180, 8, "harvest_town_photo_lawn");
            shoot(context, world, origin, 79, 2, 126, 180, 10, "harvest_town_inn_rest");
            shoot(context, world, origin, 106, 2, 110, 0, 16, "harvest_town_craft_corner");
            // Stage the public lit appearance; interaction itself is covered by FestivalToyGameTests.
            server.runOnServer(mc -> {
                for (var scene : TownHarvestCrafts.LAYOUT) for (var cell : TownHarvestCrafts.cells(scene, origin)) {
                    BlockState state = mc.overworld().getBlockState(cell.pos());
                    var lit = net.minecraft.world.level.block.state.properties.BlockStateProperties.LIT;
                    if (state.hasProperty(lit)) mc.overworld().setBlockAndUpdate(cell.pos(), state.setValue(lit, true));
                }
            });
            server.runCommand("time set midnight");
            shoot(context, world, origin, 113, 2, 88, 180, 8, "harvest_town_photo_night");
        } finally {
            context.runOnClient(c -> { if (c.gui.hud.isHidden() != hidden) c.gui.hud.toggle(); });
        }
    }

    private static void shoot(ClientGameTestContext context, TestSingleplayerContext world, BlockPos origin,
            double x, double y, double z, float yaw, float pitch, String name) {
        world.getServer().runCommand(String.format(Locale.ROOT, "tp @a %.2f %.2f %.2f %.1f %.1f",
                origin.getX() + x + 0.5, origin.getY() + y, origin.getZ() + z + 0.5, yaw, pitch));
        context.waitTicks(15);
        world.getConnection().waitForChunksRender();
        context.takeScreenshot(name);
    }
}

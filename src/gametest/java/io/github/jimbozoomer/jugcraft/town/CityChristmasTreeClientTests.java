package io.github.jimbozoomer.jugcraft.town;

import io.github.jimbozoomer.jugcraft.agriculture.FirGifts;
import java.util.Locale;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.presets.WorldPresets;

/** Exercise the installed town blueprint and the actual seasonal migration in a disposable market square. */
public final class CityChristmasTreeClientTests implements FabricClientGameTest {
    private static void view(TestServerContext server, BlockPos root, double x, double y, double z, double targetHeight) {
        double yaw = Math.toDegrees(Math.atan2(x, -z));
        double pitch = Math.toDegrees(Math.atan2(y + 1.62 - targetHeight, Math.hypot(x, z)));
        server.runCommand(String.format(Locale.ROOT, "tp @p %.2f %.2f %.2f %.2f %.2f",
            root.getX()+.5+x, root.getY()+y, root.getZ()+.5+z, yaw, pitch));
    }

    @Override public void runTest(ClientGameTestContext context) {
        boolean hidden = context.computeOnClient(c -> c.gui.hud.isHidden());
        try (var world = context.worldBuilder().adjustSettings(c -> {
            c.setGameMode(WorldCreationUiState.SelectedGameMode.CREATIVE);
            c.setWorldType(new WorldCreationUiState.WorldTypeEntry(c.getSettings().worldgenLoadContext()
                .lookupOrThrow(Registries.WORLD_PRESET).getOrThrow(WorldPresets.FLAT)));
        }).create()) {
            var server = world.getServer();
            var at = new BlockPos(912, 80, 912);
            var root = at.offset(104, 1, 104);
            var site = TownData.get().sites.stream().filter(s -> s.kind().equals("centerpiece")).findFirst().orElseThrow();
            server.runCommand("gamemode spectator @p");
            server.runCommand("time set noon");
            server.runCommand("weather clear");
            server.runCommand("gamerule minecraft:advance_time false");
            server.runCommand("forceload add 976 976 1056 1056");
            view(server, root, -29, 13, 6, 14);
            world.getConnection().waitForChunksRender();
            boolean decoratedBeforeBuild = server.computeOnServer(s -> {
                var level = s.overworld();
                var state = TownState.get(level);
                state.place(at);
                var data = TownData.get();
                // Draw the streets, neighbouring roofs and plinth from the same volume used by TownBuilder.
                for (int x=61; x<=146; x++) for (int z=60; z<=146; z++) for (int y=-1; y<=40; y++) {
                    int id = data.index(x,y,z);
                    if (id > 1) TownBuilder.set(level,at.offset(x,y,z),data.state(id));
                }
                TownDecor.force("harvest");
                // Loading a chunk alone does not mean the town has finished building its streets.
                state.setTheme("harvest",level.getGameTime());
                TownDecor.apply(level,site.index());
                boolean premature = !state.siteTheme(site.index()).isEmpty();
                for (int[] cell : site.blocks())
                    state.markBuilt(net.minecraft.world.level.ChunkPos.containing(at.offset(cell[0],cell[1],cell[2])).pack());
                state.setTheme("harvest",level.getGameTime());
                TownDecor.apply(level,site.index());
                if (!state.siteTheme(site.index()).equals("harvest_v2")) throw new AssertionError("No existing harvest display");
                TownDecor.force("december");
                state.setTheme("december",level.getGameTime());
                var tip = root.above(30);
                level.setBlockAndUpdate(tip,Blocks.DIAMOND_BLOCK.defaultBlockState());
                TownDecor.apply(level,site.index());
                if (!level.getBlockState(tip).is(Blocks.DIAMOND_BLOCK) || !state.siteTheme(site.index()).equals("harvest_v2"))
                    throw new AssertionError("Player construction was overwritten or the square was partially replaced");
                level.setBlockAndUpdate(tip,Blocks.OCHRE_FROGLIGHT.defaultBlockState());
                TownDecor.apply(level,site.index());
                if (!state.siteTheme(site.index()).equals("harvest_v2")) throw new AssertionError("Adopted a player's matching light");
                level.removeBlock(tip,false);
                TownDecor.apply(level,site.index());
                if (!(level.getBlockEntity(root) instanceof FirGifts gifts) || !level.getBlockState(tip).is(Blocks.OCHRE_FROGLIGHT))
                    throw new AssertionError("City fir failed to replace the existing harvest display");
                for (int pile=0; pile<8; pile++)
                    if (!gifts.deposit(new ItemStack(Items.COOKIE,pile+1),pile)) throw new AssertionError("Missing gift pile");
                return premature;
            });
            if (!hidden) context.getInput().pressKey(com.mojang.blaze3d.platform.InputConstants.KEY_F1);
            context.waitTicks(25);
            world.getConnection().waitForChunksRender();
            context.takeScreenshot("city_christmas_fir_square");
            server.runCommand("time set 13000");
            view(server, root, -24, 4, -18, 14);
            context.waitTicks(25);
            world.getConnection().waitForChunksRender();
            context.takeScreenshot("city_christmas_fir_lights");
            view(server, root, 6, 1, 7, 1.5);
            context.waitTicks(15);
            world.getConnection().waitForChunksRender();
            context.takeScreenshot("city_christmas_gift_piles");
            server.runOnServer(s -> {
                var level = s.overworld();
                var state = TownState.get(level);
                TownDecor.force("winter");
                state.setTheme("winter",level.getGameTime());
                TownDecor.apply(level,site.index());
                if (!(level.getBlockEntity(root) instanceof FirGifts gifts) || !state.siteTheme(site.index()).equals("december_v2"))
                    throw new AssertionError("The city cleared unclaimed gifts");
                for (int pile=0; pile<8; pile++)
                    if (gifts.collect(pile).getCount()!=pile+1) throw new AssertionError("Lost or duplicated a gift");
                TownDecor.apply(level,site.index());
                if (!state.siteTheme(site.index()).equals("winter") || !level.getBlockState(root.above(30)).isAir())
                    throw new AssertionError("Winter display never replaced the empty tree");
            });
            if (decoratedBeforeBuild) throw new AssertionError("Square ran ahead of town construction");
        } finally {
            TownDecor.force(null);
            if (context.computeOnClient(c -> c.gui.hud.isHidden()) != hidden)
                context.getInput().pressKey(com.mojang.blaze3d.platform.InputConstants.KEY_F1);
        }
    }
}

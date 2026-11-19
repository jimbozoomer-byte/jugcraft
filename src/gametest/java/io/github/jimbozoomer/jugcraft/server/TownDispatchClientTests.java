package io.github.jimbozoomer.jugcraft.server;

import io.github.jimbozoomer.jugcraft.client.TownDispatchScreen;
import io.github.jimbozoomer.jugcraft.test.InProcessServerConfigs;
import io.github.jimbozoomer.jugcraft.test.TestLoginBulletin;
import java.util.Properties;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.presets.WorldPresets;

/** Real dedicated joins, dismissal, reopening, reward acknowledgement and a second login with the same save. */
public final class TownDispatchClientTests implements FabricClientGameTest {
    @Override public void runTest(ClientGameTestContext c) {
        TestLoginBulletin.inspect(true);
        try {
            exerciseBulletin(c);
        } finally {
            TestLoginBulletin.inspect(false);
        }
    }

    private void exerciseBulletin(ClientGameTestContext c) {
        var properties = new Properties();
        properties.setProperty("server-ip", "127.0.0.1");
        properties.setProperty("motd", "Welcome to the West Gate");
        properties.setProperty("online-mode", "false");
        properties.setProperty("view-distance", "3");
        try (var server = c.worldBuilder().adjustSettings(s -> {
            s.setWorldType(new WorldCreationUiState.WorldTypeEntry(s.getSettings().worldgenLoadContext()
                    .lookupOrThrow(Registries.WORLD_PRESET).getOrThrow(WorldPresets.FLAT)));
            s.setGenerateStructures(false);
        }).createServer(properties)) {
            try (var connection = server.connect()) {
                c.waitForScreen(TownDispatchScreen.class);
                c.waitTicks(35);
                String first = c.computeOnClient(mc -> mc.gui.screen().getNarrationMessage().getString());
                if (!first.contains("West Gate") || first.contains("There are no new notices") || first.contains("Candidate status"))
                    throw new AssertionError("Missing server MOTD or packaged player notes: " + first);
                c.runOnClient(mc -> mc.gui.toastManager().clear());
                c.takeScreenshot("town_dispatch_join");
                c.clickScreenButton("›");
                if (first.equals(c.computeOnClient(mc -> mc.gui.screen().getNarrationMessage().getString()))) throw new AssertionError("News page did not change");
                c.clickScreenButton("×");
                c.waitForScreen(null);
                c.waitTicks(30);
                c.runOnClient(mc -> mc.player.connection.sendCommand("bulletin"));
                c.waitForScreen(TownDispatchScreen.class);
                c.clickScreenButton("Open parcel");
                c.waitFor(mc -> hasCollected(mc.gui.screen()), 200);
                c.takeScreenshot("town_dispatch_opened");
                c.clickScreenButton("×");
                c.waitForScreen(null);
            }
            InProcessServerConfigs.beforeReconnect(server);
            try (var again = server.connect()) {
                c.waitForScreen(TownDispatchScreen.class);
                if (!c.computeOnClient(mc -> hasCollected(mc.gui.screen())))
                    throw new AssertionError("Reconnect must retain today's opened parcel");
                c.getInput().pressKey(com.mojang.blaze3d.platform.InputConstants.KEY_ESCAPE);
                c.waitForScreen(null);
            }
        }
    }
    private static boolean hasCollected(net.minecraft.client.gui.screens.Screen screen) {
        return screen instanceof TownDispatchScreen && screen.children().stream().anyMatch(w ->
                w instanceof Button button && !button.active && button.getMessage().getString().equals("Collected"));
    }
}


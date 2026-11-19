package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.client.TownDispatchScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

/** Perform the normal bulletin dismissal on dedicated test joins before driving other in-game controls. */
public final class TestLoginBulletin implements ClientModInitializer {
    private static volatile boolean inspectBulletin;

    /** The bulletin's own rendered test exercises its buttons and dismisses it itself. */
    public static void inspect(boolean inspect) {
        inspectBulletin = inspect;
    }

    @Override public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!inspectBulletin && client.gui.screen() instanceof TownDispatchScreen bulletin) {
                bulletin.onClose();
            }
        });
    }
}

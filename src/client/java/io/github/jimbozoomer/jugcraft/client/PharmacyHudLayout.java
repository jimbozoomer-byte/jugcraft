package io.github.jimbozoomer.jugcraft.client;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;

/** Keeps only our held-dose status above stacked native health/armour rows. */
public final class PharmacyHudLayout {
    private PharmacyHudLayout() { }

    public static boolean isDoseStatus(Component text) {
        return text != null && text.getContents() instanceof TranslatableContents translation
                && (translation.getKey().equals("message.jugcraft.drug.ready")
                || translation.getKey().equals("message.jugcraft.drug.status"));
    }

    public static float anchorY(float original, int height, float maximumHealth, float displayedHealth,
                                float absorption, boolean armour, boolean forecast) {
        int rows = Math.max(1, (int) Math.ceil((Math.max(maximumHealth, displayedHealth)
                + Math.ceil(absorption)) / 20.0));
        int spacing = Math.max(10 - (rows - 2), 3);
        int nativeTop = height - 39 - (rows - 1) * spacing - (armour ? 10 : 0);
        // Vanilla draws this text at anchor-4 through anchor+5. Leave two pixels around the upper row.
        if (original + 7 < nativeTop) return original;
        float anchor = Math.min(original, nativeTop - 8);
        // When moved above the native HUD, also clear our held-dose forecast at height-94.
        if (forecast) anchor = Math.min(anchor, height - 102);
        return Math.max(6, anchor);
    }
}

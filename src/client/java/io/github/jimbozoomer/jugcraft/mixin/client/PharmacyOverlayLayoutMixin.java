package io.github.jimbozoomer.jugcraft.mixin.client;

import io.github.jimbozoomer.jugcraft.client.PharmacyHudLayout;
import io.github.jimbozoomer.jugcraft.client.StrainClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Hud;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/** Preserve every native HUD row and unrelated action-bar message; move our status only when it crowds them. */
@Mixin(Hud.class)
abstract class PharmacyOverlayLayoutMixin {
    @Shadow private Component overlayMessageString;
    @Shadow private int displayHealth;

    @ModifyArg(method = "extractOverlayMessage", at = @At(value = "INVOKE",
            target = "Lorg/joml/Matrix3x2fStack;translate(FF)Lorg/joml/Matrix3x2f;"), index = 1, require = 1)
    private float jugcraft$clearStackedHealth(float y) {
        var client = Minecraft.getInstance();
        var player = client.player;
        if (player == null || client.gameMode == null || !client.gameMode.canHurtPlayer()
                || !PharmacyHudLayout.isDoseStatus(overlayMessageString)) return y;
        var strain = StrainClient.state();
        return PharmacyHudLayout.anchorY(y, client.getWindow().getGuiScaledHeight(), player.getMaxHealth(),
                Math.max(displayHealth, player.getHealth()), player.getAbsorptionAmount(), player.getArmorValue() > 0,
                strain.tenths() > 0 && strain.nextCost() > 0);
    }
}

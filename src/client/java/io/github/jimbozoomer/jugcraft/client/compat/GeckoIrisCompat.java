package io.github.jimbozoomer.jugcraft.client.compat;

import com.geckolib.renderer.layer.builtin.AutoGlowingGeoLayer;
import io.github.jimbozoomer.jugcraft.Jugcraft;
import net.irisshaders.iris.api.v0.IrisApi;
import net.irisshaders.iris.api.v0.IrisProgram;
import net.irisshaders.iris.api.v0.IrisShadowProgram;
import net.minecraft.resources.Identifier;

/** Optional Iris adapter for GeckoLib 5.5.7's Windows-only emissive pipeline.
 * Retains GeckoLib's masks, animation, blending and depth setup; only supplies the missing shader associations.
 * This class is loaded only behind the Iris mod-presence guard in JugcraftClient.
 */
public final class GeckoIrisCompat {
    private static boolean registered;

    private GeckoIrisCompat() {}

    public static void register() {
        if (registered) return;
        // Public accessor initializes the real shared pipeline. No geometry is drawn here.
        var pipeline = AutoGlowingGeoLayer.EmissiveRenderType.getRenderType(
            Jugcraft.id("textures/entity/cinderling_glowmask.png"), false, false, false).pipeline();
        if (!pipeline.getLocation().equals(Identifier.fromNamespaceAndPath("geckolib", "pipeline/emissive"))) {
            // GeckoLib uses vanilla eyes on other platforms: leave Iris's vanilla mappings alone.
            registered = true;
            return;
        }
        var iris = IrisApi.getInstance();
        try {
            iris.assignPipeline(pipeline, IrisProgram.EMISSIVE_ENTITIES);
        } catch (IllegalStateException alreadyAssigned) {
            // The pinned Iris API rejects duplicate assignments. Preserve another adapter's existing mapping.
            Jugcraft.LOGGER.info("Keeping the existing Iris mapping for GeckoLib emissive rendering: {}", alreadyAssigned.getMessage());
        }
        try {
            iris.assignPipelineShadow(pipeline, IrisShadowProgram.SHADOW_ENTITIES);
        } catch (IllegalStateException alreadyAssigned) {
            Jugcraft.LOGGER.info("Keeping the existing Iris shadow mapping for GeckoLib emissive rendering: {}", alreadyAssigned.getMessage());
        }
        registered = true;
        Jugcraft.LOGGER.info("GeckoLib emissive pipeline registered with Iris for entity and shadow rendering");
    }
}

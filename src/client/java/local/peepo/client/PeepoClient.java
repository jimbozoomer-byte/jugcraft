package local.peepo.client;
import local.peepo.PeepoMod;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.minecraft.client.model.geom.ModelLayerLocation;

public final class PeepoClient implements ClientModInitializer {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(PeepoMod.id("peepo"), "main");
    @Override public void onInitializeClient() {
        AssignmentPreview.initialize();
        net.minecraft.client.gui.screens.MenuScreens.register(local.peepo.CompanionMenu.TYPE,CompanionScreen::new);
        net.minecraft.client.gui.screens.MenuScreens.register(local.peepo.GeneratorWheel.MENU,WheelScreen::new);
        net.minecraft.client.renderer.blockentity.BlockEntityRenderers.register(local.peepo.GeneratorWheel.ENTITY,WheelRenderer::new);
        ModelLayerRegistry.registerModelLayer(LAYER, PeepoGeometry::create);
        EntityRendererRegistry.register(PeepoMod.PEEPO, PeepoRenderer::new);
        EntityRendererRegistry.register(PeepoMod.JUGHEAD, PeepoRenderer::new);
        EntityRendererRegistry.register(PeepoMod.LEGACY_JUGHEAD, PeepoRenderer::new);
    }
}

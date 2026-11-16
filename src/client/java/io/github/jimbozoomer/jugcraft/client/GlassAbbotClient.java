package io.github.jimbozoomer.jugcraft.client;

import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.layer.builtin.AutoGlowingGeoLayer;
import io.github.jimbozoomer.jugcraft.lair.abbot.GlassAbbotEntity;
import io.github.jimbozoomer.jugcraft.lair.abbot.GlassImageEntity;
import io.github.jimbozoomer.jugcraft.lair.abbot.JugcraftAbbot;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

/**
 * The Glass Abbot and his fight on the client (docs/features/glass-abbot.md): GeckoLib draws him and his Glass Images from
 * his one model, playing the clip for what the server synced, their glowmask (the rose window where his face should be,
 * and a glint in each pane of his mantle) drawn full bright. His beam, his shards, his Prism Line, the beam's path that
 * First Light shows ahead and the rose window's shadow under him are the server's particles. Nothing here decides anything.
 */
final class GlassAbbotClient {
	/** His shadow, as wide as his robe's hem. */
	static final float SHADOW = 0.7F;

	private GlassAbbotClient() {
	}

	static void register() {
		EntityRendererRegistry.register(JugcraftAbbot.GLASS_ABBOT, context -> new GeoEntityRenderer<GlassAbbotEntity, EntityRenderState>(
				context, JugcraftAbbot.GLASS_ABBOT) {
			{
				shadowRadius = SHADOW;
			}
		}.withRenderLayer(AutoGlowingGeoLayer::new));
		EntityRendererRegistry.register(JugcraftAbbot.GLASS_IMAGE, context -> new GeoEntityRenderer<GlassImageEntity, EntityRenderState>(
				context, JugcraftAbbot.GLASS_IMAGE) {
			{
				shadowRadius = SHADOW;
			}
		}.withRenderLayer(AutoGlowingGeoLayer::new));
	}
}

package io.github.jimbozoomer.jugcraft.client;

import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.layer.builtin.AutoGlowingGeoLayer;
import io.github.jimbozoomer.jugcraft.lair.roc.GaleFledglingEntity;
import io.github.jimbozoomer.jugcraft.lair.roc.JugcraftRoc;
import io.github.jimbozoomer.jugcraft.lair.roc.StormRocEntity;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.phys.AABB;

/**
 * The Storm Roc and his fight on the client (docs/features/storm-roc.md): GeckoLib draws him and his Gale Fledglings from
 * their own models, playing the clips for what the server synced (his body's clip, and whether his wings are folded or
 * spread), their glowmasks (his eyes and the lightning-white tips of his flight feathers; the fledglings' eyes) drawn full
 * bright. His spread wings reach well past his body, so he is kept drawn while any of him can be on screen. His lane's
 * sparks, the rings of his Lightning Marks and the wind's arrows are the server's particles. Nothing here decides anything.
 */
final class StormRocClient {
	/** His shadow, wide as his folded wings. */
	static final float SHADOW = 1.3F;

	private StormRocClient() {
	}

	static void register() {
		EntityRendererRegistry.register(JugcraftRoc.STORM_ROC, context -> new GeoEntityRenderer<StormRocEntity, EntityRenderState>(
				context, JugcraftRoc.STORM_ROC) {
			{
				shadowRadius = SHADOW;
			}

			@Override
			protected AABB getBoundingBoxForCulling(StormRocEntity roc, float partialTick) {
				return roc.getBoundingBox().inflate(2.5, 1.0, 2.5);
			}
		}.withRenderLayer(AutoGlowingGeoLayer::new));
		EntityRendererRegistry.register(JugcraftRoc.GALE_FLEDGLING, context -> new GeoEntityRenderer<GaleFledglingEntity, EntityRenderState>(
				context, JugcraftRoc.GALE_FLEDGLING) {
			@Override
			protected AABB getBoundingBoxForCulling(GaleFledglingEntity fledgling, float partialTick) {
				return fledgling.getBoundingBox().inflate(1.0, 0.5, 1.0);
			}
		}.withRenderLayer(AutoGlowingGeoLayer::new));
	}
}

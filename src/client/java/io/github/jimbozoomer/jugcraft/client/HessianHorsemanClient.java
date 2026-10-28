package io.github.jimbozoomer.jugcraft.client;

import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.layer.builtin.AutoGlowingGeoLayer;
import io.github.jimbozoomer.jugcraft.lair.horseman.HessianHorsemanEntity;
import io.github.jimbozoomer.jugcraft.lair.horseman.HessianPumpkinEntity;
import io.github.jimbozoomer.jugcraft.lair.horseman.JugcraftHorseman;
import io.github.jimbozoomer.jugcraft.lair.horseman.PhantomRiderEntity;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.phys.AABB;

/**
 * The Headless Horseman's lair fight on the client (docs/features/hessian-horseman.md): GeckoLib draws him on his horse,
 * his phantom riders, the pumpkin he tosses and the lantern head he hurls from their own models, playing the clips for
 * what the server synced, with their glowmasks (his horse's eyes, the embers in his collar, his lantern's fire and the
 * jack-o'-lanterns' faces, orange or white; the phantoms' blue fire, all of them) drawn full bright. His horse is longer
 * than his box, and when it rears it reaches far above it, so he is kept drawn while any of him can be on screen. The
 * snuffed braziers' smoke, the ember rings, the pillar of flame and the riders' blue lines are the server's particles.
 * Nothing here decides anything.
 */
final class HessianHorsemanClient {
	/** His shadow, as long as his horse. */
	static final float SHADOW = 1.2F;

	private HessianHorsemanClient() {
	}

	static void register() {
		EntityRendererRegistry.register(JugcraftHorseman.HESSIAN_HORSEMAN, context -> new GeoEntityRenderer<HessianHorsemanEntity, EntityRenderState>(
				context, JugcraftHorseman.HESSIAN_HORSEMAN) {
			{
				shadowRadius = SHADOW;
			}

			@Override
			protected AABB getBoundingBoxForCulling(HessianHorsemanEntity horseman, float partialTick) {
				return horseman.getBoundingBox().inflate(2.0, 1.5, 2.0);
			}
		}.withRenderLayer(AutoGlowingGeoLayer::new));
		EntityRendererRegistry.register(JugcraftHorseman.PHANTOM_RIDER, context -> new GeoEntityRenderer<PhantomRiderEntity, EntityRenderState>(
				context, JugcraftHorseman.PHANTOM_RIDER) {
			@Override
			protected AABB getBoundingBoxForCulling(PhantomRiderEntity rider, float partialTick) {
				return rider.getBoundingBox().inflate(2.0, 1.5, 2.0);
			}
		}.withRenderLayer(AutoGlowingGeoLayer::new));
		EntityRendererRegistry.register(JugcraftHorseman.HESSIAN_PUMPKIN, context -> new GeoEntityRenderer<HessianPumpkinEntity, EntityRenderState>(
				context, JugcraftHorseman.HESSIAN_PUMPKIN).withRenderLayer(AutoGlowingGeoLayer::new));
		EntityRendererRegistry.register(JugcraftHorseman.HESSIAN_HEAD, context -> new GeoEntityRenderer<HessianPumpkinEntity, EntityRenderState>(
				context, JugcraftHorseman.HESSIAN_HEAD).withRenderLayer(AutoGlowingGeoLayer::new));
	}
}

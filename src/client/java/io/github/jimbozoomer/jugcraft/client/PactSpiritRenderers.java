package io.github.jimbozoomer.jugcraft.client;

import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.layer.builtin.AutoGlowingGeoLayer;
import io.github.jimbozoomer.jugcraft.concordance.spirits.PactSpiritEntity;
import io.github.jimbozoomer.jugcraft.concordance.spirits.Workers;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.AABB;

/**
 * Concordance garden cohort, GC-01 (SG11): every pact spirit's renderer from one row (tools/concordance_pacts.py SPIRITS
 * "body"."render"): GeckoLib draws it from its own model at its scale, its glowmask lit when it has one; its culling box
 * reaches as far as its gestures carry it; one leaving is drawn only where its departing gesture was fired, so one who
 * began watching it after sees it already gone; one frozen by a tick freeze is drawn as of its last tick from the frame the
 * freeze arrives. Chunk D's Stolas renderer (WorkerClient), unchanged in what it draws, is the first row. Nothing here
 * decides anything.
 */
final class PactSpiritRenderers {
	/** One spirit's drawing: its scale, its shadow's radius, its culling box widened sideways and below and reaching above, its glowmask. */
	record Row(float scale, float shadow, double wide, double low, double above, boolean glow) {
	}

	private PactSpiritRenderers() {
	}

	static void register() {
		// Stolas (D-S5, D-06): the owner's Flashjack rig drawn larger, his spread wings and his arrival from above inside his box.
		register(Workers.STOLAS, new Row(1.4F, 0.4F, 1.2, 0.1, 2.0, true));
		// Buer (GC-04): the Decor Blossom drawn 2.2 times its block size (the bud 1.4 blocks across, open about 1.7 across its
		// sides); his culling box reaches 1 block below for his sinking departure, 0.5 above for his raised pistil; his pistil lit.
		register(Workers.BUER, new Row(2.2F, 0.6F, 0.4, 1.0, 0.5, true));
	}

	static <T extends PactSpiritEntity<T>> void register(EntityType<T> type, Row row) {
		EntityRendererRegistry.register(type, context -> {
			GeoEntityRenderer<T, EntityRenderState> renderer = new GeoEntityRenderer<T, EntityRenderState>(context, type) {
				{
					shadowRadius = row.shadow();
				}

				@Override
				protected AABB getBoundingBoxForCulling(T spirit, float partialTick) {
					// Its gestures reach past its box (Stolas's spread wings, his arrival from above and his leaving upwards): it
					// stays drawn while any of it can be on screen.
					return spirit.getBoundingBox().inflate(row.wide(), row.low(), row.wide()).expandTowards(0.0, row.above(), 0.0);
				}

				@Override
				public boolean shouldRender(T spirit, Frustum culler, double camX, double camY, double camZ, float partialTicks) {
					// Leaving, it is drawn only where its departing gesture was fired: one who began watching it after sees it
					// already gone, never perched until it vanishes (Chunk D's unit review).
					return (!spirit.leaving() || departs(spirit)) && super.shouldRender(spirit, culler, camX, camY, camZ, partialTicks);
				}

				@Override
				public void extractRenderState(T spirit, EntityRenderState state, float partialTick) {
					// A frozen spirit is drawn as of its last tick, as vanilla draws one once a tick freeze reaches this client's
					// timer. On the frame the freeze arrives vanilla still passes the running partial tick though the spirit is no
					// longer ticked, so its age steps back and GeckoLib rewinds a finished gesture to its last frame, drawn over its
					// loop for as long as the world stays frozen.
					super.extractRenderState(spirit, state, spirit.level().tickRateManager().isEntityFrozen(spirit) ? 1.0F : partialTick);
				}
			};
			if (row.glow()) {
				renderer.withRenderLayer(AutoGlowingGeoLayer::new);
			}
			return renderer.withScale(row.scale());
		});
	}

	/**
	 * Whether this client was sent its departing gesture: its gesture controller was triggered with it, and keeps it once it
	 * has played (it holds its last frame). Read from the trigger, not from the clip drawn, since a clip starts only when it
	 * is drawn.
	 */
	static boolean departs(PactSpiritEntity<?> spirit) {
		AnimatableManager<PactSpiritEntity<?>> manager = spirit.getAnimatableInstanceCache().getManagerForId(spirit.getId());
		AnimationController<?> gestures = manager.getAnimationControllers().get(PactSpiritEntity.GESTURE_CONTROLLER);
		return gestures != null && spirit.body().depart().animation().equals(gestures.getCurrentRawAnimation());
	}
}

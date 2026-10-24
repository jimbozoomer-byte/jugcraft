package io.github.jimbozoomer.jugcraft.client;

import com.geckolib.renderer.GeoEntityRenderer;
import io.github.jimbozoomer.jugcraft.concordance.dreaming.DreamWispEntity;
import io.github.jimbozoomer.jugcraft.concordance.dreaming.Dreaming;
import io.github.jimbozoomer.jugcraft.concordance.spirits.ClockworkPorterEntity;
import io.github.jimbozoomer.jugcraft.concordance.spirits.GatheringShadeEntity;
import io.github.jimbozoomer.jugcraft.concordance.spirits.HearthlingEntity;
import io.github.jimbozoomer.jugcraft.concordance.spirits.StolasEntity;
import io.github.jimbozoomer.jugcraft.concordance.spirits.Workers;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.phys.AABB;

/**
 * Familiars, spirits and constructs on the client (roadmap step 17): GeckoLib draws each from its own model and plays
 * the clip for the status the server synced. Nothing here decides what a worker does. Dream wisps (step 22) are drawn
 * here too: they only drift. So is Stolas (Concordance Chunk D): in unit 2 the owner's Flashjack rig under his name,
 * drawn larger (D-S5).
 */
final class WorkerClient {
	/** Stolas's provisional body, larger than the owner's Flashjack rig it is (tools/concordance_pacts.py BODY_SCALE; D-S5). */
	static final float STOLAS_SCALE = 1.4F;

	private WorkerClient() {
	}

	static void register() {
		EntityRendererRegistry.register(Workers.HEARTHLING,
				context -> new GeoEntityRenderer<HearthlingEntity, EntityRenderState>(context, Workers.HEARTHLING));
		EntityRendererRegistry.register(Workers.GATHERING_SHADE,
				context -> new GeoEntityRenderer<GatheringShadeEntity, EntityRenderState>(context, Workers.GATHERING_SHADE));
		EntityRendererRegistry.register(Workers.CLOCKWORK_PORTER,
				context -> new GeoEntityRenderer<ClockworkPorterEntity, EntityRenderState>(context, Workers.CLOCKWORK_PORTER));
		EntityRendererRegistry.register(Dreaming.WISP, context -> new GeoEntityRenderer<DreamWispEntity, EntityRenderState>(context, Dreaming.WISP));
		EntityRendererRegistry.register(Workers.STOLAS, context -> new GeoEntityRenderer<StolasEntity, EntityRenderState>(context, Workers.STOLAS) {
			{
				shadowRadius = 0.4F;
			}

			@Override
			protected AABB getBoundingBoxForCulling(StolasEntity stolas, float partialTick) {
				// His beak and tail reach past his box: he stays drawn while any of him can be on screen.
				return stolas.getBoundingBox().inflate(0.4, 0.1, 0.4);
			}
		}.withScale(STOLAS_SCALE));
	}
}

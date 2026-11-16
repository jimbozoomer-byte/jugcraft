package io.github.jimbozoomer.jugcraft.client;

import com.geckolib.renderer.GeoEntityRenderer;
import io.github.jimbozoomer.jugcraft.concordance.dreaming.DreamWispEntity;
import io.github.jimbozoomer.jugcraft.concordance.dreaming.Dreaming;
import io.github.jimbozoomer.jugcraft.concordance.spirits.ClockworkPorterEntity;
import io.github.jimbozoomer.jugcraft.concordance.spirits.GatheringShadeEntity;
import io.github.jimbozoomer.jugcraft.concordance.spirits.HearthlingEntity;
import io.github.jimbozoomer.jugcraft.concordance.spirits.Workers;
import io.github.jimbozoomer.jugcraft.concordance.spirits.WorkshopHandEntity;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

/**
 * Familiars, spirits and constructs on the client (roadmap step 17): GeckoLib draws each from its own model and plays
 * the clip for the status the server synced. Nothing here decides what a worker does. Dream wisps (step 22) are drawn
 * here too: they only drift. So are the pact spirits (Concordance Chunk D; the garden cohort's table, PactSpiritRenderers):
 * Stolas first, the owner's Flashjack rig in his raven form, drawn larger with his eyes lit (D-S5, D-06).
 */
final class WorkerClient {
	private WorkerClient() {
	}

	static void register() {
		EntityRendererRegistry.register(Workers.HEARTHLING,
				context -> new GeoEntityRenderer<HearthlingEntity, EntityRenderState>(context, Workers.HEARTHLING));
		EntityRendererRegistry.register(Workers.GATHERING_SHADE,
				context -> new GeoEntityRenderer<GatheringShadeEntity, EntityRenderState>(context, Workers.GATHERING_SHADE));
		EntityRendererRegistry.register(Workers.CLOCKWORK_PORTER,
				context -> new GeoEntityRenderer<ClockworkPorterEntity, EntityRenderState>(context, Workers.CLOCKWORK_PORTER));
		EntityRendererRegistry.register(Workers.WORKSHOP_HAND,
				context -> new GeoEntityRenderer<WorkshopHandEntity, EntityRenderState>(context, Workers.WORKSHOP_HAND));
		EntityRendererRegistry.register(Dreaming.WISP, context -> new GeoEntityRenderer<DreamWispEntity, EntityRenderState>(context, Dreaming.WISP));
		// The pact spirits (Concordance Chunk D's Stolas first): their renderer table (the garden cohort, GC-01).
		PactSpiritRenderers.register();
	}
}

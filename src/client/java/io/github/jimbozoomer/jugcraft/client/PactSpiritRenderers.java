package io.github.jimbozoomer.jugcraft.client;

import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.constant.DataTickets;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.renderer.layer.builtin.AutoGlowingGeoLayer;
import io.github.jimbozoomer.jugcraft.concordance.spirits.PactSpiritEntity;
import io.github.jimbozoomer.jugcraft.concordance.spirits.Workers;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Concordance garden cohort, GC-01 (SG11): every pact spirit's renderer from one row (tools/concordance_pacts.py SPIRITS
 * "body"."render"): GeckoLib draws it from its own model at its scale, its glowmask lit when it has one; its culling box
 * reaches as far as its gestures carry it; one leaving is drawn only where its departing gesture was fired, so one who
 * began watching it after sees it already gone; one frozen by a tick freeze is drawn as of its last tick from the frame the
 * freeze arrives. Chunk D's Stolas renderer (WorkerClient), unchanged in what it draws, is the first row. Nothing here
 * decides anything.
 * <p>
 * GC-06: a row may name a bone its work target draws toward, flown out and back by the client over FLIGHT_TICKS; only
 * drawing, of server state.
 */
final class PactSpiritRenderers {
	/**
	 * One spirit's drawing: its scale, its shadow's radius, its culling box widened sideways and below and reaching above,
	 * its glowmask; GC-06 (GC-P66): the root-child bone its work target draws toward (Marax's familiar), or null.
	 */
	record Row(float scale, float shadow, double wide, double low, double above, boolean glow, @Nullable String reach) {
		/** A row with no reaching bone (every spirit before Marax: their lines stay as they are). */
		Row(float scale, float shadow, double wide, double low, double above, boolean glow) {
			this(scale, shadow, wide, low, above, glow, null);
		}
	}

	/** GC-06: where a row's reaching bone is drawn this frame (its model's drawn frame, blocks), and how far through its flight. */
	private static final DataTicket<Vec3> REACH = DataTicket.create("jugcraft_pact_reach", Vec3.class);
	private static final DataTicket<Float> FLIGHT = DataTicket.create("jugcraft_pact_flight", Float.class);

	private PactSpiritRenderers() {
	}

	static void register() {
		// Stolas (D-S5, D-06): the owner's Flashjack rig drawn larger, his spread wings and his arrival from above inside his box.
		register(Workers.STOLAS, new Row(1.4F, 0.4F, 1.2, 0.1, 2.0, true));
		// Buer (GC-04): the Decor Blossom drawn 2.2 times its block size (the bud 1.4 blocks across, open about 1.7 across its
		// sides); his culling box reaches 1 block below for his sinking departure, 0.5 above for his raised pistil; his pistil lit.
		register(Workers.BUER, new Row(2.2F, 0.6F, 0.4, 1.0, 0.5, true));
		// Bathin (GC-05): his built model drawn at its own size; his culling box reaches 0.3 sideways for his shoulders and his
		// pointing arm, 0.1 below for his hover, 0.3 above; his eyes lit.
		register(Workers.BATHIN, new Row(1.0F, 0.5F, 0.3, 0.1, 0.3, true));
		// GC-06 unit 3
		// Marax (GC-06): his built model drawn at its own size; his culling box reaches 0.9 sideways for his length and his
		// walk on leaving, 0.1 below, 0.8 above for his familiar's flight in on arriving; the familiar's eyes lit; his familiar
		// the bone his work target draws toward (GC-P66).
		register(Workers.MARAX, new Row(1.0F, 0.9F, 0.9, 0.1, 0.8, true, "familiar"));
		// GC-07 unit 4
		// GC-08 unit 5
		// GC-09 unit 6
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
					AABB box = spirit.getBoundingBox().inflate(row.wide(), row.low(), row.wide()).expandTowards(0.0, row.above(), 0.0);
					BlockPos target = row.reach() == null ? null : spirit.flown();
					// GC-06 (GC-P62): while its reach is out, the box covers where it is drawn (at most TARGET_REACH away).
					return target == null || spirit.flight(partialTick) <= 0.0F ? box
							: box.minmax(AABB.ofSize(PactSpiritEntity.reach(spirit.getPosition(partialTick), PactSpiritEntity.hover(target)), 1.0, 1.0, 1.0));
				}

				@Override
				public void addRenderData(T spirit, @Nullable Void relatedObject, EntityRenderState state, float partialTick) {
					super.addRenderData(spirit, relatedObject, state, partialTick);
					// GC-06 (GC-P66): filled every frame (the render state is reused), so a reach at rest is never left drawn.
					BlockPos target = row.reach() == null ? null : spirit.flown();
					float flight = target == null ? 0.0F : spirit.flight(partialTick);
					float yaw = state.getOrDefaultGeckolibData(DataTickets.ENTITY_BODY_YAW, 0.0F);
					state.addGeckolibData(FLIGHT, flight);
					state.addGeckolibData(REACH, flight <= 0.0F ? Vec3.ZERO
							: PactSpiritEntity.posed(spirit.getPosition(partialTick), yaw, PactSpiritEntity.hover(target), row.scale()));
				}

				@Override
				public void adjustModelBonesForRender(RenderPassInfo<EntityRenderState> info, BoneSnapshots bones) {
					super.adjustModelBonesForRender(info, bones);
					Float flight = info.getGeckolibData(FLIGHT);
					Vec3 reach = info.getGeckolibData(REACH);
					if (row.reach() == null || flight == null || reach == null || flight <= 0.0F) {
						return;
					}
					// Its reaching bone (a root child) drawn from where its clips put it toward the reach, by its flight: a
					// snapshot's translation is drawn as (-x, y, z) / 16 from the bone's baked pivot (GL BoneSnapshot.java:238).
					info.model().getBone(row.reach()).ifPresent(bone -> bones.ifPresent(row.reach(), snapshot -> {
						snapshot.setTranslateX(Mth.lerp(flight, snapshot.getTranslateX(), -(float) (reach.x * 16.0 - bone.pivotX())));
						snapshot.setTranslateY(Mth.lerp(flight, snapshot.getTranslateY(), (float) (reach.y * 16.0 - bone.pivotY())));
						snapshot.setTranslateZ(Mth.lerp(flight, snapshot.getTranslateZ(), (float) (reach.z * 16.0 - bone.pivotZ())));
					}));
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

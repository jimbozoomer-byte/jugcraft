package io.github.jimbozoomer.jugcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.jimbozoomer.jugcraft.agriculture.HauntedCarousel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Draws the Haunted Carousel (Haunted Midway, part 5) from the quads tools/haunted_midway_data.py writes
 * (assets/jugcraft/haunted_midway_quads.json): its deck, column, poles and canopy turned to its angle about its middle; its
 * bulbs, at full brightness while it is lit; and each mount at its slot round the ring, facing the way it goes, the horses
 * and bats risen up their poles as far as {@link HauntedCarousel#bob} puts them (where its riders sit), and each bat's
 * wings beating by {@link HauntedCarousel#flap}.
 *
 * <p>The entity stands in its middle block, which gives light while it is lit, so the deck and canopy take their light
 * from just over the block, and each mount from where it is.
 */
public class HauntedCarouselRenderer extends EntityRenderer<HauntedCarousel, HauntedCarouselRenderer.State> {
	private static final String[] KINDS = {"haunted_carousel_coach", "haunted_carousel_horse", "haunted_carousel_bat"};
	/** Where a bat's wings are hinged to its body, in pixels from its seat: either side, and down. */
	private static final float WING_ACROSS = 3.0F;
	private static final float WING_DOWN = 3.0F;

	public static final class State extends EntityRenderState {
		float angle;
		boolean lit;
		int light;
		final int[] mountLight = new int[HauntedCarousel.MOUNTS];
	}

	public HauntedCarouselRenderer(EntityRendererProvider.Context context) {
		super(context);
		shadowRadius = 0.0F;
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	/** The whole carousel, not just the entity's small box at its foot. */
	@Override
	protected AABB getBoundingBoxForCulling(HauntedCarousel carousel, float partialTick) {
		return carousel.extent();
	}

	@Override
	public void extractRenderState(HauntedCarousel carousel, State state, float partialTick) {
		super.extractRenderState(carousel, state, partialTick);
		state.angle = carousel.angle(partialTick);
		state.lit = carousel.lit();
		Level level = carousel.level();
		state.light = LightCoordsUtil.getLightCoords(level, carousel.centre().above());
		for (int mount = 0; mount < HauntedCarousel.MOUNTS; mount++) {
			Vec3 at = new Vec3(HauntedCarousel.radius(mount), 0.6, 0.0).yRot(HauntedCarousel.slotAngle(HauntedCarousel.slot(mount), state.angle));
			state.mountLight[mount] = LightCoordsUtil.getLightCoords(level, BlockPos.containing(carousel.position().add(at)));
		}
	}

	@Override
	public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
		QuadModel spin = DecorQuads.get("haunted_carousel_spin");
		QuadModel bulbs = DecorQuads.get("haunted_carousel_bulbs");
		QuadModel left = DecorQuads.get("haunted_carousel_bat_wing_left");
		QuadModel right = DecorQuads.get("haunted_carousel_bat_wing_right");
		pose.pushPose();
		pose.rotateDegrees(Axis.YP, (float) Math.toDegrees(state.angle));
		if (spin != null) {
			spin.submit(pose, collector, state.light);
		}
		if (bulbs != null) {
			bulbs.submit(pose, collector, state.lit ? LightCoordsUtil.FULL_BRIGHT : state.light);
		}
		pose.popPose();
		for (int mount = 0; mount < HauntedCarousel.MOUNTS; mount++) {
			int kind = HauntedCarousel.kind(mount);
			QuadModel model = DecorQuads.get(KINDS[kind]);
			if (model == null) {
				continue;
			}
			// The coach stands on the deck; a horse's or bat's model is drawn from its seat, as high as it has ridden.
			double up = kind == HauntedCarousel.COACH ? HauntedCarousel.DECK : HauntedCarousel.seatHeight(mount, state.angle);
			pose.pushPose();
			pose.rotateDegrees(Axis.YP, (float) Math.toDegrees(HauntedCarousel.slotAngle(HauntedCarousel.slot(mount), state.angle)));
			pose.translate((float) HauntedCarousel.radius(mount), (float) up, 0.0F);
			int light = state.mountLight[mount];
			model.submit(pose, collector, light);
			if (kind == HauntedCarousel.BAT) {
				float flap = HauntedCarousel.flap(mount, state.angle);
				wing(pose, collector, right, WING_ACROSS, flap, light);
				wing(pose, collector, left, -WING_ACROSS, -flap, light);
			}
			pose.popPose();
		}
		super.submit(state, pose, collector, camera);
	}

	/** A bat's wing, hinged at its side ({@code across} pixels out) and raised {@code degrees} about its length. */
	private static void wing(PoseStack pose, SubmitNodeCollector collector, @Nullable QuadModel wing, float across, float degrees, int light) {
		if (wing == null) {
			return;
		}
		pose.pushPose();
		pose.translate(across / 16.0F, -WING_DOWN / 16.0F, 0.0F);
		pose.rotateDegrees(Axis.ZP, degrees);
		wing.submit(pose, collector, light);
		pose.popPose();
	}
}

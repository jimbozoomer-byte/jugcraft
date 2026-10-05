package io.github.jimbozoomer.jugcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.jimbozoomer.jugcraft.agriculture.DecorationBlockEntity;
import io.github.jimbozoomer.jugcraft.agriculture.GiantBeatingHeartBlock;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Draws the Giant's Beating Heart's heart from its first block (decor17_quads.json: giant_heart_cradle,
 * giant_heart_ventricles and giant_heart_atria, modelled in the vat's frame by tools/decor17_data.py), lit by the light
 * in the middle of the vat. On each beat the block gives, its atria swell, and {@value #LUB_DUB_TICKS} ticks later its
 * ventricles (lub-dub), each settling over {@value #DECAY_TICKS} ticks; the great vessels rise up into the pipes from the
 * lid as the atria swell.
 */
public class GiantBeatingHeartRenderer implements BlockEntityRenderer<DecorationBlockEntity, GiantBeatingHeartRenderer.State> {
	/** How it beats: tools/decor17.py GIANT_HEART holds the same numbers, which the audit compares. */
	private static final float LUB_DUB_TICKS = 4.0F;
	private static final float DECAY_TICKS = 8.0F;
	private static final float ATRIA = 0.10F;
	private static final float VENTRICLES = 0.12F;
	/** What each part swells about, in pixels in the vat's frame. */
	private static final float[] ATRIA_ANCHOR = {24.0F, 29.0F, 24.0F};
	private static final float[] VENTRICLE_ANCHOR = {21.0F, 8.0F, 24.0F};
	/** When each heart's last beat began, as this client saw it: {beat was on, game time}. */
	private final Map<DecorationBlockEntity, double[]> beats = new WeakHashMap<>();

	public static final class State extends BlockEntityRenderState {
		Direction facing = Direction.NORTH;
		float atria;
		float ventricles;
		int light;
		boolean whole;
	}

	public GiantBeatingHeartRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	@Override
	public void extractRenderState(DecorationBlockEntity heart, State state, float partialTick, Vec3 camera,
			ModelFeatureRenderer.@Nullable CrumblingOverlay crumbling) {
		BlockEntityRenderState.extractBase(heart, state, crumbling);
		BlockState block = heart.getBlockState();
		Level level = heart.getLevel();
		state.whole = block.getBlock() instanceof GiantBeatingHeartBlock && level != null;
		if (!(block.getBlock() instanceof GiantBeatingHeartBlock giant) || level == null) {
			return;
		}
		state.facing = block.getValue(GiantBeatingHeartBlock.FACING);
		double now = level.getGameTime() + (double) partialTick;
		boolean beat = block.getValue(GiantBeatingHeartBlock.BEAT);
		double[] seen = beats.computeIfAbsent(heart, h -> new double[] {0.0, -100.0});
		if (beat && seen[0] == 0.0) {
			seen[1] = now;
		}
		seen[0] = beat ? 1.0 : 0.0;
		float since = (float) (now - seen[1]);
		state.atria = pulse(since);
		state.ventricles = pulse(since - LUB_DUB_TICKS);
		state.light = LightCoordsUtil.getLightCoords(level, giant.partPos(heart.getBlockPos(), state.facing, GiantBeatingHeartBlock.MIDDLE));
	}

	/** How far through its swell a part is, {@code ticks} after its beat: 1 at the beat, falling to 0. */
	private static float pulse(float ticks) {
		return ticks < 0.0F ? 0.0F : Math.max(0.0F, 1.0F - ticks / DECAY_TICKS);
	}

	@Override
	public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
		if (!state.whole) {
			return;
		}
		pose.pushPose();
		pose.translate(0.5F, 0.0F, 0.5F);
		pose.rotateDegrees(Axis.YP, -RockingChairRenderer.yRotation(state.facing));
		pose.translate(-0.5F, 0.0F, -0.5F);
		// The vat's frame runs across from its far block, to the left of the first (as the placer saw it).
		pose.translate(-(GiantBeatingHeartBlock.SIZE - 1), 0.0F, 0.0F);
		QuadModel cradle = DecorQuads.get("giant_heart_cradle");
		if (cradle != null) {
			cradle.submit(pose, collector, state.light);
		}
		swelling(pose, collector, "giant_heart_ventricles", VENTRICLE_ANCHOR, 1.0F + VENTRICLES * state.ventricles * state.ventricles, state.light);
		swelling(pose, collector, "giant_heart_atria", ATRIA_ANCHOR, 1.0F + ATRIA * state.atria * state.atria, state.light);
		pose.popPose();
	}

	private static void swelling(PoseStack pose, SubmitNodeCollector collector, String name, float[] anchor, float swell, int light) {
		QuadModel part = DecorQuads.get(name);
		if (part == null) {
			return;
		}
		pose.pushPose();
		pose.translate(anchor[0] / 16, anchor[1] / 16, anchor[2] / 16);
		pose.scale(swell, swell, swell);
		pose.translate(-anchor[0] / 16, -anchor[1] / 16, -anchor[2] / 16);
		part.submit(pose, collector, light);
		pose.popPose();
	}

	/** The heart fills a vat three blocks across, past its first block's box. */
	@Override
	public boolean shouldRenderOffScreen() {
		return true;
	}
}

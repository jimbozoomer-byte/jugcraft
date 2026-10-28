package io.github.jimbozoomer.jugcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.agriculture.FacingDecorationBlock;
import io.github.jimbozoomer.jugcraft.agriculture.Knitting;
import io.github.jimbozoomer.jugcraft.agriculture.KnittingBasketBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * The balls of yarn in a Knitting Basket (Cozy Fall Hearth, part 4): each a round ball wound of strands
 * ({@code textures/entity/yarn_ball.png}, made by tools/hearth_data.py yarn_ball() in greys), drawn in its own yarn's
 * colour ({@link Knitting#color}) in its spot in the basket, facing north and turned to the basket's facing.
 */
public class KnittingBasketRenderer implements BlockEntityRenderer<KnittingBasketBlockEntity, KnittingBasketRenderer.State> {
	private static final RenderType YARN = RenderTypes.entityCutout(Jugcraft.id("textures/entity/yarn_ball.png"));
	/**
	 * Where each ball sits in the basket, facing north: the x and z of its middle and the height of its bottom, in pixels;
	 * the two at the back are piled up behind the two in front, on the floor.
	 */
	private static final float[][] SPOTS = {{6.75F, 6.75F, 0.75F}, {9.25F, 6.75F, 0.75F}, {6.75F, 9.25F, 1.5F}, {9.25F, 9.25F, 1.5F}};
	/** Half a ball's width, in pixels. */
	private static final float BALL = 1.25F;

	public static final class State extends BlockEntityRenderState {
		int[] colors = new int[0];
		Direction facing = Direction.NORTH;
	}

	public KnittingBasketRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	@Override
	public void extractRenderState(KnittingBasketBlockEntity basket, State state, float partialTick, Vec3 camera,
			ModelFeatureRenderer.@Nullable CrumblingOverlay crumbling) {
		BlockEntityRenderState.extractBase(basket, state, crumbling);
		state.colors = basket.yarn().stream().mapToInt(Knitting::color).toArray();
		BlockState block = basket.getBlockState();
		state.facing = block.hasProperty(FacingDecorationBlock.FACING) ? block.getValue(FacingDecorationBlock.FACING) : Direction.NORTH;
	}

	@Override
	public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
		if (state.colors.length == 0) {
			return;
		}
		int light = state.lightCoords;
		pose.pushPose();
		pose.translate(0.5F, 0.0F, 0.5F);
		pose.rotateDegrees(Axis.YP, -RockingChairRenderer.yRotation(state.facing));
		pose.translate(-0.5F, 0.0F, -0.5F);
		collector.submitCustomGeometry(pose, YARN, (matrix, buffer) -> {
			for (int i = 0; i < state.colors.length && i < SPOTS.length; i++) {
				ball(buffer, matrix, SPOTS[i][0], SPOTS[i][2], SPOTS[i][1], 0xFF000000 | state.colors[i], light);
			}
		});
		pose.popPose();
	}

	/** A ball as three crossed boxes, each long along one axis and short along the others, so its corners are rounded off. */
	private static void ball(VertexConsumer buffer, PoseStack.Pose matrix, float cx, float y0, float cz, int argb, int light) {
		float cy = y0 + BALL;
		float in = BALL * 0.72F;
		TintedBoxes.box(buffer, matrix, cx - BALL, cy - in, cz - in, cx + BALL, cy + in, cz + in, argb, light);
		TintedBoxes.box(buffer, matrix, cx - in, cy - BALL, cz - in, cx + in, cy + BALL, cz + in, argb, light);
		TintedBoxes.box(buffer, matrix, cx - in, cy - in, cz - BALL, cx + in, cy + in, cz + BALL, argb, light);
	}
}

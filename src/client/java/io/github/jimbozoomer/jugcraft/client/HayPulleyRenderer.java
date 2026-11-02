package io.github.jimbozoomer.jugcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.jimbozoomer.jugcraft.agriculture.HayPulleyBlock;
import io.github.jimbozoomer.jugcraft.agriculture.HayPulleyBlockEntity;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * What moves on a Hay-Loft Pulley (the block model is its plate, beam, brace and pulley block), drawn for a pulley facing
 * north and turned to its facing, each piece the blockstate's own model of it (HayPulleyBlock.PIECE, made by
 * tools/barn_tools_data.py pulley()): the wheel, turning as the rope runs over it; the rope, from the wheel down as many
 * blocks as it hangs, a block's length of rope at a time (the last cut to fit), running between its last length and its
 * new one through a haul, easing in and out; and the hay hook at its end. The rope and hook sway a few degrees about the
 * wheel, slowly, as a hanging rope does.
 */
public class HayPulleyRenderer implements BlockEntityRenderer<HayPulleyBlockEntity, HayPulleyRenderer.State> {
	private static final RenderType BLOCKS = RenderTypes.entityCutout(TextureAtlas.LOCATION_BLOCKS);
	/** The wheel's axle (height and depth, pixels) and radius, where the rope leaves it going down (depth) and from what height. */
	private static final float AXLE_Y = 12.0F;
	private static final float AXLE_Z = 3.0F;
	private static final float RADIUS = 1.5F;
	private static final float ROPE_Z = 1.5F;
	private static final float ROPE_TOP = 12.0F;
	/** The sway: degrees each way, and ticks a sway takes. */
	private static final float SWAY = 3.0F;
	private static final float SWAY_TICKS = 90.0F;

	public static final class State extends BlockEntityRenderState {
		final List<BlockStateModelPart> wheel = new ArrayList<>();
		final List<BlockStateModelPart> rope = new ArrayList<>();
		final List<BlockStateModelPart> hook = new ArrayList<>();
		Direction facing = Direction.NORTH;
		/** How far down the rope hangs now, in blocks; how far it has run since the haul began, in pixels; the sway, degrees. */
		float length = 1.0F;
		float run;
		float sway;
	}

	public HayPulleyRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	private static void piece(BlockState block, HayPulleyBlock.Piece piece, List<BlockStateModelPart> into) {
		into.clear();
		Minecraft.getInstance().getModelManager().getBlockStateModelSet()
				.get(block.setValue(HayPulleyBlock.FACING, Direction.NORTH).setValue(HayPulleyBlock.PIECE, piece))
				.collectParts(RandomSource.create(0L), into);
	}

	@Override
	public void extractRenderState(HayPulleyBlockEntity pulley, State state, float partialTick, Vec3 camera,
			ModelFeatureRenderer.@Nullable CrumblingOverlay crumbling) {
		BlockEntityRenderState.extractBase(pulley, state, crumbling);
		BlockState block = pulley.getBlockState();
		if (!(block.getBlock() instanceof HayPulleyBlock)) {
			state.wheel.clear();
			state.rope.clear();
			state.hook.clear();
			return;
		}
		piece(block, HayPulleyBlock.Piece.WHEEL, state.wheel);
		piece(block, HayPulleyBlock.Piece.ROPE, state.rope);
		piece(block, HayPulleyBlock.Piece.HOOK, state.hook);
		state.facing = block.getValue(HayPulleyBlock.FACING);
		long time = pulley.getLevel() == null ? 0L : pulley.getLevel().getGameTime();
		float eased = (1.0F - Mth.cos(pulley.progress(time, partialTick) * Mth.PI)) / 2.0F;
		int to = block.getValue(HayPulleyBlock.DROP);
		state.length = pulley.from() + (to - pulley.from()) * eased;
		state.run = (state.length - pulley.from()) * 16.0F;
		state.sway = SWAY * Mth.sin((time % (long) SWAY_TICKS + partialTick) / SWAY_TICKS * 2.0F * Mth.PI);
	}

	@Override
	public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
		if (state.wheel.isEmpty()) {
			return;
		}
		int light = state.lightCoords;
		pose.pushPose();
		pose.translate(0.5F, 0.0F, 0.5F);
		pose.rotateDegrees(Axis.YP, -RockingChairRenderer.yRotation(state.facing));
		pose.translate(-0.5F, 0.0F, -0.5F);
		// The wheel, turned by the rope run over it.
		pose.pushPose();
		pose.translate(0.0F, AXLE_Y / 16.0F, AXLE_Z / 16.0F);
		pose.rotateDegrees(Axis.XP, (float) Math.toDegrees(state.run / RADIUS));
		pose.translate(0.0F, -AXLE_Y / 16.0F, -AXLE_Z / 16.0F);
		collector.submitBlockModel(pose, BLOCKS, state.wheel, new int[0], light, OverlayTexture.NO_OVERLAY, 0);
		pose.popPose();
		// The rope and the hook, swaying about where the rope leaves the wheel.
		pose.translate(0.0F, ROPE_TOP / 16.0F, ROPE_Z / 16.0F);
		pose.rotateDegrees(Axis.XP, state.sway);
		pose.translate(0.0F, -ROPE_TOP / 16.0F, -ROPE_Z / 16.0F);
		float top = ROPE_TOP / 16.0F;
		int whole = (int) Math.floor(state.length);
		for (int k = 0; k < whole; k++) {
			pose.pushPose();
			pose.translate(0.0F, top - 1.0F - k, 0.0F);
			collector.submitBlockModel(pose, BLOCKS, state.rope, new int[0], light, OverlayTexture.NO_OVERLAY, 0);
			pose.popPose();
		}
		float rest = state.length - whole;
		if (rest > 0.01F) {
			pose.pushPose();
			pose.translate(0.0F, top - whole - rest, 0.0F);
			pose.scale(1.0F, rest, 1.0F);
			collector.submitBlockModel(pose, BLOCKS, state.rope, new int[0], light, OverlayTexture.NO_OVERLAY, 0);
			pose.popPose();
		}
		pose.translate(0.0F, top - state.length - 1.0F, 0.0F);
		collector.submitBlockModel(pose, BLOCKS, state.hook, new int[0], light, OverlayTexture.NO_OVERLAY, 0);
		pose.popPose();
	}
}

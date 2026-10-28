package io.github.jimbozoomer.jugcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.jimbozoomer.jugcraft.agriculture.HandWorkedBlock;
import io.github.jimbozoomer.jugcraft.agriculture.HandWorkedBlockEntity;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Draws the part of a hand-worked station that moves (Barn & Harvest Tools, part 2): the block's own {@code runner} model
 * (HandWorkedBlock.RUNNER, made by tools/barn_tools_data.py), already turned to the block's facing by its blockstate,
 * moved by {@link Motion} as far through its last stroke as the block entity says. The Quern's upper stone turns once
 * round its middle in a stroke, easing in and out; the Butter Churn's dasher goes down {@value #DASHER_DEPTH} pixels and
 * back up.
 */
public class HandWorkedRenderer<T extends HandWorkedBlockEntity> implements BlockEntityRenderer<T, HandWorkedRenderer.State> {
	/** How far the churn's dasher goes down in a stroke, in pixels. */
	public static final float DASHER_DEPTH = 5.0F;

	/** How the moving part is placed, `progress` (0 to 1) through a stroke. */
	public interface Motion {
		void apply(PoseStack pose, float progress);
	}

	/** Once round the block's upright middle, easing in and out. */
	public static final Motion TURN = (pose, progress) -> {
		float eased = (1.0F - Mth.cos(progress * Mth.PI)) / 2.0F;
		pose.translate(0.5F, 0.0F, 0.5F);
		pose.rotateDegrees(Axis.YP, -360.0F * eased);
		pose.translate(-0.5F, 0.0F, -0.5F);
	};

	/** Down and back up. */
	public static final Motion PLUNGE = (pose, progress) -> pose.translate(0.0F, -DASHER_DEPTH / 16.0F * Mth.sin(progress * Mth.PI), 0.0F);

	private final Motion motion;

	public static final class State extends BlockEntityRenderState {
		final List<BlockStateModelPart> parts = new ArrayList<>();
		float progress = 1.0F;
	}

	public HandWorkedRenderer(BlockEntityRendererProvider.Context context, Motion motion) {
		this.motion = motion;
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	@Override
	public void extractRenderState(T station, State state, float partialTick, Vec3 camera, ModelFeatureRenderer.@Nullable CrumblingOverlay crumbling) {
		BlockEntityRenderState.extractBase(station, state, crumbling);
		state.parts.clear();
		BlockState block = station.getBlockState();
		if (block.getBlock() instanceof HandWorkedBlock) {
			Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(block.setValue(HandWorkedBlock.RUNNER, true))
					.collectParts(RandomSource.create(0L), state.parts);
		}
		long time = station.getLevel() == null ? 0L : station.getLevel().getGameTime();
		state.progress = station.progress(time, partialTick);
	}

	@Override
	public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
		if (state.parts.isEmpty()) {
			return;
		}
		pose.pushPose();
		motion.apply(pose, state.progress);
		collector.submitBlockModel(pose, RenderTypes.entityCutout(TextureAtlas.LOCATION_BLOCKS), state.parts, new int[0], state.lightCoords,
				OverlayTexture.NO_OVERLAY, 0);
		pose.popPose();
	}
}

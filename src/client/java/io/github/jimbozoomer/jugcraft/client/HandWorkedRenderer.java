package io.github.jimbozoomer.jugcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.jimbozoomer.jugcraft.agriculture.HandWorkedBlock;
import io.github.jimbozoomer.jugcraft.agriculture.HandWorkedBlockEntity;
import io.github.jimbozoomer.jugcraft.agriculture.HarvestBellBlock;
import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;
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
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Draws the part of a hand-worked block that moves (Barn & Harvest Tools, parts 2 and 4): the block's own model of it
 * (its blockstate's runner variant, made by tools/barn_tools_data.py), moved by {@link Motion} as far through its last
 * stroke as the block entity says. The Quern's upper stone turns once round its middle in a stroke, easing in and out;
 * the Butter Churn's dasher goes down {@value #DASHER_DEPTH} pixels and back up; the Harvest Bell swings on its axle,
 * the swing dying away.
 */
public class HandWorkedRenderer<T extends HandWorkedBlockEntity> implements BlockEntityRenderer<T, HandWorkedRenderer.State> {
	/** How far the churn's dasher goes down in a stroke, in pixels. */
	public static final float DASHER_DEPTH = 5.0F;

	/** The Harvest Bell's axle in its upper block (pixels: height and depth, facing north), and its first swing's reach. */
	private static final float BELL_AXLE_Y = 13.5F;
	private static final float BELL_AXLE_Z = 8.0F;
	private static final float BELL_SWING = 40.0F;
	private static final float BELL_SWINGS = 2.5F;

	/** How the moving part is placed, {@code progress} (0 to 1) through a stroke, for a block facing {@code facing}. */
	public interface Motion {
		void apply(PoseStack pose, float progress, Direction facing);
	}

	/** Once round the block's upright middle, easing in and out (the runner model is already turned to the facing). */
	public static final Motion TURN = (pose, progress, facing) -> {
		float eased = (1.0F - Mth.cos(progress * Mth.PI)) / 2.0F;
		pose.translate(0.5F, 0.0F, 0.5F);
		pose.rotateDegrees(Axis.YP, -360.0F * eased);
		pose.translate(-0.5F, 0.0F, -0.5F);
	};

	/** Down and back up. */
	public static final Motion PLUNGE = (pose, progress, facing) -> pose.translate(0.0F, -DASHER_DEPTH / 16.0F * Mth.sin(progress * Mth.PI), 0.0F);

	/**
	 * The bell (its model drawn facing north): up into the upper block, turned to the facing, then swung about its axle,
	 * {@value #BELL_SWINGS} swings dying away over the stroke.
	 */
	public static final Motion SWING = (pose, progress, facing) -> {
		float angle = BELL_SWING * (1.0F - progress) * Mth.sin(progress * BELL_SWINGS * 2.0F * Mth.PI);
		pose.translate(0.5F, 1.0F, 0.5F);
		pose.rotateDegrees(Axis.YP, -RockingChairRenderer.yRotation(facing));
		pose.translate(0.0F, BELL_AXLE_Y / 16.0F, BELL_AXLE_Z / 16.0F - 0.5F);
		pose.rotateDegrees(Axis.XP, angle);
		pose.translate(-0.5F, -BELL_AXLE_Y / 16.0F, -BELL_AXLE_Z / 16.0F);
	};

	/** The quern's and the churn's moving part: their own facing's runner variant. */
	public static final UnaryOperator<BlockState> STATION = state -> state.setValue(HandWorkedBlock.RUNNER, true);
	/** The bell: its upper block's runner variant, facing north (SWING turns it). */
	public static final UnaryOperator<BlockState> BELL = state -> state.setValue(HarvestBellBlock.PART, 1).setValue(HarvestBellBlock.RUNNER, true)
			.setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH);

	private final UnaryOperator<BlockState> runner;
	private final Motion motion;

	public static final class State extends BlockEntityRenderState {
		final List<BlockStateModelPart> parts = new ArrayList<>();
		float progress = 1.0F;
		Direction facing = Direction.NORTH;
	}

	/** {@code runner} gives the state whose model is the moving part, from the block entity's block state. */
	public HandWorkedRenderer(BlockEntityRendererProvider.Context context, UnaryOperator<BlockState> runner, Motion motion) {
		this.runner = runner;
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
		if (block.getBlock() instanceof HandWorkedBlock || block.getBlock() instanceof HarvestBellBlock) {
			state.facing = block.getValue(HorizontalDirectionalBlock.FACING);
			Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(runner.apply(block))
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
		motion.apply(pose, state.progress, state.facing);
		collector.submitBlockModel(pose, RenderTypes.entityCutout(TextureAtlas.LOCATION_BLOCKS), state.parts, new int[0], state.lightCoords,
				OverlayTexture.NO_OVERLAY, 0);
		pose.popPose();
	}
}

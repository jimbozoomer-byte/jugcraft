package io.github.jimbozoomer.jugcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.jimbozoomer.jugcraft.agriculture.DecorationBlockEntity;
import io.github.jimbozoomer.jugcraft.agriculture.FacingDecorationBlock;
import io.github.jimbozoomer.jugcraft.agriculture.ChoppingBlockBlock;
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
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * The Chopping Block's hatchet (Cozy Fall Hearth, part 3): its blockstate's runner model (the hatchet, drawn facing north;
 * made by tools/hearth_data.py chopping_block()), turned to the block's facing and, for a moment after each chop the block
 * entity {@link DecorationBlockEntity#marked}, swung up about its grip and back down into the round by
 * {@link ChoppingBlockBlock#swing}.
 */
public class ChoppingBlockRenderer implements BlockEntityRenderer<DecorationBlockEntity, ChoppingBlockRenderer.State> {
	/** The end of the hatchet's handle, where it is held as it swings (pixels: height and depth, facing north). */
	private static final float GRIP_Y = 10.6F;
	private static final float GRIP_Z = 2.46F;

	public static final class State extends BlockEntityRenderState {
		final List<BlockStateModelPart> parts = new ArrayList<>();
		Direction facing = Direction.NORTH;
		float swing;
	}

	public ChoppingBlockRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	@Override
	public void extractRenderState(DecorationBlockEntity entity, State state, float partialTick, Vec3 camera,
			ModelFeatureRenderer.@Nullable CrumblingOverlay crumbling) {
		BlockEntityRenderState.extractBase(entity, state, crumbling);
		state.parts.clear();
		BlockState block = entity.getBlockState();
		Level level = entity.getLevel();
		if (!(block.getBlock() instanceof ChoppingBlockBlock) || level == null) {
			return;
		}
		state.facing = block.getValue(FacingDecorationBlock.FACING);
		Minecraft.getInstance().getModelManager().getBlockStateModelSet()
				.get(block.setValue(ChoppingBlockBlock.RUNNER, true).setValue(FacingDecorationBlock.FACING, Direction.NORTH))
				.collectParts(RandomSource.create(0L), state.parts);
		long marked = entity.marked();
		state.swing = marked == Long.MIN_VALUE ? 0.0F : ChoppingBlockBlock.swing(level.getGameTime() - marked + partialTick);
	}

	@Override
	public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
		if (state.parts.isEmpty()) {
			return;
		}
		pose.pushPose();
		pose.translate(0.5F, 0.0F, 0.5F);
		pose.rotateDegrees(Axis.YP, -RockingChairRenderer.yRotation(state.facing));
		pose.translate(-0.5F, 0.0F, -0.5F);
		pose.translate(0.0F, GRIP_Y / 16.0F, GRIP_Z / 16.0F);
		pose.rotateDegrees(Axis.XP, -state.swing); // Its head up and over.
		pose.translate(0.0F, -GRIP_Y / 16.0F, -GRIP_Z / 16.0F);
		collector.submitBlockModel(pose, RenderTypes.entityCutout(TextureAtlas.LOCATION_BLOCKS), state.parts, new int[0], state.lightCoords,
				OverlayTexture.NO_OVERLAY, 0);
		pose.popPose();
	}
}

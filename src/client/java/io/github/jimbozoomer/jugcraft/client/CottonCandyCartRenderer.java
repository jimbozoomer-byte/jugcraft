package io.github.jimbozoomer.jugcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.jimbozoomer.jugcraft.agriculture.CottonCandyCartBlock;
import io.github.jimbozoomer.jugcraft.agriculture.DecorationBlockEntity;
import io.github.jimbozoomer.jugcraft.agriculture.MultiDecorationBlock;
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
 * The Cotton-Candy Cart's spinner head (Haunted Midway, part 1): its blockstate's runner model (the slotted drum and its
 * cap, drawn facing north; made by tools/haunted_midway_data.py cotton_candy_cart()), turned to the cart's facing and,
 * while its motor runs, spinning about the bowl's middle by {@link CottonCandyCartBlock#spin}.
 */
public class CottonCandyCartRenderer implements BlockEntityRenderer<DecorationBlockEntity, CottonCandyCartRenderer.State> {
	public static final class State extends BlockEntityRenderState {
		final List<BlockStateModelPart> parts = new ArrayList<>();
		Direction facing = Direction.NORTH;
		float spin;
	}

	public CottonCandyCartRenderer(BlockEntityRendererProvider.Context context) {
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
		if (!(block.getBlock() instanceof CottonCandyCartBlock) || level == null) {
			return;
		}
		state.facing = block.getValue(MultiDecorationBlock.FACING);
		Minecraft.getInstance().getModelManager().getBlockStateModelSet()
				.get(block.setValue(CottonCandyCartBlock.RUNNER, true).setValue(MultiDecorationBlock.FACING, Direction.NORTH))
				.collectParts(RandomSource.create(0L), state.parts);
		state.spin = CottonCandyCartBlock.spin(block.getValue(MultiDecorationBlock.LIT), level.getGameTime() + partialTick);
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
		pose.translate(CottonCandyCartBlock.BOWL_X / 16.0F, 0.0F, CottonCandyCartBlock.BOWL_Z / 16.0F);
		pose.rotateDegrees(Axis.YP, state.spin);
		pose.translate(-CottonCandyCartBlock.BOWL_X / 16.0F, 0.0F, -CottonCandyCartBlock.BOWL_Z / 16.0F);
		collector.submitBlockModel(pose, RenderTypes.entityCutout(TextureAtlas.LOCATION_BLOCKS), state.parts, new int[0], state.lightCoords,
				OverlayTexture.NO_OVERLAY, 0);
		pose.popPose();
	}
}

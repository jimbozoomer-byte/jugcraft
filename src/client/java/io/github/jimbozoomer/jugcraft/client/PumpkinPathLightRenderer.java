package io.github.jimbozoomer.jugcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.jimbozoomer.jugcraft.agriculture.DecorationBlockEntity;
import io.github.jimbozoomer.jugcraft.agriculture.MultiDecorationBlock;
import io.github.jimbozoomer.jugcraft.agriculture.PumpkinPathLightBlock;
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
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * The Pumpkin Path Light's lantern (Front-Yard Haunt Scenes, part 1): its blockstate's runner models 1 (the chain, bail
 * and jack-o'-lantern) and 2 (its carved face's glow), drawn facing north (made by tools/front_yard_data.py
 * pumpkin_path_light()) and turned to its facing, swaying about the crook's tip by {@link PumpkinPathLightBlock#sway}:
 * to and fro along the path, and a little from side to side. Lit, the glow is drawn full-bright.
 */
public class PumpkinPathLightRenderer implements BlockEntityRenderer<DecorationBlockEntity, PumpkinPathLightRenderer.State> {
	public static final class State extends BlockEntityRenderState {
		final List<List<BlockStateModelPart>> parts = new ArrayList<>();
		Direction facing = Direction.NORTH;
		boolean lit;
		float along;
		float across;
	}

	public PumpkinPathLightRenderer(BlockEntityRendererProvider.Context context) {
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
		if (!(block.getBlock() instanceof PumpkinPathLightBlock) || level == null) {
			return;
		}
		state.facing = block.getValue(MultiDecorationBlock.FACING);
		state.lit = block.getValue(MultiDecorationBlock.LIT);
		for (int runner = 1; runner <= 2; runner++) {
			List<BlockStateModelPart> parts = new ArrayList<>();
			Minecraft.getInstance().getModelManager().getBlockStateModelSet()
					.get(block.setValue(PumpkinPathLightBlock.RUNNER, runner).setValue(MultiDecorationBlock.FACING, Direction.NORTH))
					.collectParts(RandomSource.create(0L), parts);
			state.parts.add(parts);
		}
		float now = level.getGameTime() + partialTick;
		int seed = entity.getBlockPos().hashCode();
		state.along = PumpkinPathLightBlock.sway(now, seed, false);
		state.across = PumpkinPathLightBlock.sway(now, seed, true);
	}

	@Override
	public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
		if (state.parts.size() < 2) {
			return;
		}
		pose.pushPose();
		pose.translate(0.5F, 0.0F, 0.5F);
		pose.rotateDegrees(Axis.YP, -RockingChairRenderer.yRotation(state.facing));
		pose.translate(-0.5F, 0.0F, -0.5F);
		pose.translate(0.5F, PumpkinPathLightBlock.PIVOT_Y / 16.0F, PumpkinPathLightBlock.PIVOT_Z / 16.0F);
		pose.rotateDegrees(Axis.XP, state.along);
		pose.rotateDegrees(Axis.ZP, state.across);
		pose.translate(-0.5F, -PumpkinPathLightBlock.PIVOT_Y / 16.0F, -PumpkinPathLightBlock.PIVOT_Z / 16.0F);
		collector.submitBlockModel(pose, RenderTypes.entityCutout(TextureAtlas.LOCATION_BLOCKS), state.parts.get(0), new int[0], state.lightCoords,
				OverlayTexture.NO_OVERLAY, 0);
		if (state.lit) {
			collector.submitBlockModel(pose, RenderTypes.entityCutout(TextureAtlas.LOCATION_BLOCKS), state.parts.get(1), new int[0],
					LightCoordsUtil.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0);
		}
		pose.popPose();
	}

	@Override
	public boolean shouldRenderOffScreen() {
		return true; // Its chain hangs from its upper block.
	}
}

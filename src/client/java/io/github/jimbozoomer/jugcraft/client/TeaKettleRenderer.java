package io.github.jimbozoomer.jugcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.jimbozoomer.jugcraft.agriculture.DecorationBlockEntity;
import io.github.jimbozoomer.jugcraft.agriculture.FacingDecorationBlock;
import io.github.jimbozoomer.jugcraft.agriculture.StovetopBlock;
import io.github.jimbozoomer.jugcraft.agriculture.TeaKettleBlock;
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
 * The Tea Kettle's lid (Cozy Fall Hearth, part 2): its blockstate's runner model (the lid and its knob, drawn facing
 * north; made by tools/hearth_data.py kettle()), turned to the kettle's facing and, while the kettle is heated, jumping
 * and tipping on its rim by {@link TeaKettleBlock#rattle}, about the lid's back edge.
 */
public class TeaKettleRenderer implements BlockEntityRenderer<DecorationBlockEntity, TeaKettleRenderer.State> {
	/** The lid's back edge, where it tips (pixels: height and depth, facing north). */
	private static final float HINGE_Y = 4.25F;
	private static final float HINGE_Z = 9.25F;

	public static final class State extends BlockEntityRenderState {
		final List<BlockStateModelPart> parts = new ArrayList<>();
		Direction facing = Direction.NORTH;
		float rattle;
	}

	public TeaKettleRenderer(BlockEntityRendererProvider.Context context) {
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
		if (!(block.getBlock() instanceof TeaKettleBlock) || level == null) {
			return;
		}
		state.facing = block.getValue(FacingDecorationBlock.FACING);
		Minecraft.getInstance().getModelManager().getBlockStateModelSet()
				.get(block.setValue(TeaKettleBlock.RUNNER, true).setValue(FacingDecorationBlock.FACING, Direction.NORTH))
				.collectParts(RandomSource.create(0L), state.parts);
		state.rattle = TeaKettleBlock.rattle(entity.getBlockPos(), block.getValue(StovetopBlock.HEATED), level.getGameTime() + partialTick);
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
		pose.translate(0.0F, state.rattle * TeaKettleBlock.LID_LIFT / 16.0F, 0.0F);
		pose.translate(0.0F, HINGE_Y / 16.0F, HINGE_Z / 16.0F);
		pose.rotateDegrees(Axis.XP, state.rattle * TeaKettleBlock.LID_TIP); // Its front edge up.
		pose.translate(0.0F, -HINGE_Y / 16.0F, -HINGE_Z / 16.0F);
		collector.submitBlockModel(pose, RenderTypes.entityCutout(TextureAtlas.LOCATION_BLOCKS), state.parts, new int[0], state.lightCoords,
				OverlayTexture.NO_OVERLAY, 0);
		pose.popPose();
	}
}

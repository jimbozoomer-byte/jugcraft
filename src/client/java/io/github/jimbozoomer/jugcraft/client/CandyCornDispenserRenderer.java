package io.github.jimbozoomer.jugcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.jimbozoomer.jugcraft.agriculture.CandyCornDispenserBlock;
import io.github.jimbozoomer.jugcraft.agriculture.CandyCornDispenserBlockEntity;
import io.github.jimbozoomer.jugcraft.agriculture.FacingDecorationBlock;
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
 * The Candy Corn Dispenser's crank and flap (Halloween Party Table, part 1): its blockstate's runner models 1 (the crank)
 * and 2 (the flap), drawn facing north (made by tools/party_table_data.py candy_corn_dispenser()) and turned to its
 * facing. The crank turns once round about its axle by {@link CandyCornDispenserBlock#crank} after its block entity's mark;
 * the flap swings out about its top by {@link CandyCornDispenserBlock#flap} as the piece drops.
 */
public class CandyCornDispenserRenderer implements BlockEntityRenderer<CandyCornDispenserBlockEntity, CandyCornDispenserRenderer.State> {
	public static final class State extends BlockEntityRenderState {
		final List<BlockStateModelPart> crank = new ArrayList<>();
		final List<BlockStateModelPart> flap = new ArrayList<>();
		Direction facing = Direction.NORTH;
		float turn;
		float swing;
	}

	public CandyCornDispenserRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	@Override
	public void extractRenderState(CandyCornDispenserBlockEntity entity, State state, float partialTick, Vec3 camera,
			ModelFeatureRenderer.@Nullable CrumblingOverlay crumbling) {
		BlockEntityRenderState.extractBase(entity, state, crumbling);
		state.crank.clear();
		state.flap.clear();
		BlockState block = entity.getBlockState();
		Level level = entity.getLevel();
		if (!(block.getBlock() instanceof CandyCornDispenserBlock) || level == null) {
			return;
		}
		state.facing = block.getValue(FacingDecorationBlock.FACING);
		BlockState north = block.setValue(FacingDecorationBlock.FACING, Direction.NORTH);
		Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(north.setValue(CandyCornDispenserBlock.RUNNER, 1))
				.collectParts(RandomSource.create(0L), state.crank);
		Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(north.setValue(CandyCornDispenserBlock.RUNNER, 2))
				.collectParts(RandomSource.create(0L), state.flap);
		float since = entity.marked() == Long.MIN_VALUE ? -1.0F : level.getGameTime() + partialTick - entity.marked();
		state.turn = CandyCornDispenserBlock.crank(since);
		state.swing = CandyCornDispenserBlock.flap(since);
	}

	@Override
	public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
		if (state.crank.isEmpty() && state.flap.isEmpty()) {
			return;
		}
		pose.pushPose();
		pose.translate(0.5F, 0.0F, 0.5F);
		pose.rotateDegrees(Axis.YP, -RockingChairRenderer.yRotation(state.facing));
		pose.translate(-0.5F, 0.0F, -0.5F);
		// The crank about its axle, which runs out of the front: clockwise as its front is seen.
		pose.pushPose();
		pose.translate(0.5F, CandyCornDispenserBlock.CRANK_Y / 16.0F, CandyCornDispenserBlock.CRANK_Z / 16.0F);
		pose.rotateDegrees(Axis.ZP, state.turn);
		pose.translate(-0.5F, -CandyCornDispenserBlock.CRANK_Y / 16.0F, -CandyCornDispenserBlock.CRANK_Z / 16.0F);
		collector.submitBlockModel(pose, RenderTypes.entityCutout(TextureAtlas.LOCATION_BLOCKS), state.crank, new int[0],
				state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
		pose.popPose();
		// The flap about its top, its foot swinging out toward the front.
		pose.pushPose();
		pose.translate(0.5F, CandyCornDispenserBlock.HINGE_Y / 16.0F, CandyCornDispenserBlock.HINGE_Z / 16.0F);
		pose.rotateDegrees(Axis.XP, state.swing);
		pose.translate(-0.5F, -CandyCornDispenserBlock.HINGE_Y / 16.0F, -CandyCornDispenserBlock.HINGE_Z / 16.0F);
		collector.submitBlockModel(pose, RenderTypes.entityCutout(TextureAtlas.LOCATION_BLOCKS), state.flap, new int[0],
				state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
		pose.popPose();
		pose.popPose();
	}
}

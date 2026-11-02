package io.github.jimbozoomer.jugcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.jimbozoomer.jugcraft.agriculture.MultiDecorationBlock;
import io.github.jimbozoomer.jugcraft.agriculture.ShootingGalleryBlock;
import io.github.jimbozoomer.jugcraft.agriculture.ShootingGalleryBlockEntity;
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
 * The Shooting Gallery's targets (Haunted Midway, part 3): its blockstate's runner models 1 (a tin bat) and 2 (a tin
 * ghost), each drawn facing north on its rail at x 0 of the first block's model (the frame's x 32; made by
 * tools/haunted_midway_data.py shooting_gallery()). Each target is drawn where {@link ShootingGalleryBlock#targetX} puts
 * it, standing, or lying flat back on its rail when the round's block entity says it is down, all turned to the
 * gallery's facing.
 */
public class ShootingGalleryRenderer implements BlockEntityRenderer<ShootingGalleryBlockEntity, ShootingGalleryRenderer.State> {
	/** How far a knocked target lies back, in degrees. */
	private static final float KNOCKED = 80.0F;

	public static final class State extends BlockEntityRenderState {
		final List<List<BlockStateModelPart>> parts = new ArrayList<>();
		Direction facing = Direction.NORTH;
		final float[] x = new float[ShootingGalleryBlock.TARGETS];
		final boolean[] down = new boolean[ShootingGalleryBlock.TARGETS];
	}

	public ShootingGalleryRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	@Override
	public void extractRenderState(ShootingGalleryBlockEntity entity, State state, float partialTick, Vec3 camera,
			ModelFeatureRenderer.@Nullable CrumblingOverlay crumbling) {
		BlockEntityRenderState.extractBase(entity, state, crumbling);
		state.parts.clear();
		BlockState block = entity.getBlockState();
		Level level = entity.getLevel();
		if (!(block.getBlock() instanceof ShootingGalleryBlock) || level == null) {
			return;
		}
		state.facing = block.getValue(MultiDecorationBlock.FACING);
		for (int rail = 0; rail < 2; rail++) {
			List<BlockStateModelPart> parts = new ArrayList<>();
			Minecraft.getInstance().getModelManager().getBlockStateModelSet()
					.get(block.setValue(ShootingGalleryBlock.RUNNER, rail + 1).setValue(MultiDecorationBlock.FACING, Direction.NORTH))
					.collectParts(RandomSource.create(0L), parts);
			state.parts.add(parts);
			for (int k = 0; k < ShootingGalleryBlock.PER_RAIL; k++) {
				int target = rail * ShootingGalleryBlock.PER_RAIL + k;
				state.x[target] = ShootingGalleryBlock.targetX(rail, k, level.getGameTime(), partialTick);
				state.down[target] = entity.isDown(target);
			}
		}
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
		for (int target = 0; target < ShootingGalleryBlock.TARGETS; target++) {
			int rail = target / ShootingGalleryBlock.PER_RAIL;
			float baseY = (ShootingGalleryBlock.RAIL_Y[rail] + 0.5F) / 16.0F;
			float baseZ = ShootingGalleryBlock.RAIL_Z[rail] / 16.0F;
			pose.pushPose();
			pose.translate((state.x[target] - 32.0F) / 16.0F, 0.0F, 0.0F);
			if (state.down[target]) {
				pose.translate(0.0F, baseY, baseZ);
				pose.rotateDegrees(Axis.XP, KNOCKED); // Its top back, away from the counter.
				pose.translate(0.0F, -baseY, -baseZ);
			}
			collector.submitBlockModel(pose, RenderTypes.entityCutout(TextureAtlas.LOCATION_BLOCKS), state.parts.get(rail), new int[0],
					state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
			pose.popPose();
		}
		pose.popPose();
	}

	@Override
	public boolean shouldRenderOffScreen() {
		return true; // Its targets run the whole width of its three blocks.
	}
}

package io.github.jimbozoomer.jugcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.jimbozoomer.jugcraft.agriculture.MultiDecorationBlock;
import io.github.jimbozoomer.jugcraft.agriculture.WhackAZombieBlock;
import io.github.jimbozoomer.jugcraft.agriculture.WhackAZombieBlockEntity;
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
 * Whack-a-Zombie's heads (Haunted Midway, part 2): its blockstate's runner model (a zombie's head on its pole over the
 * first hole, drawn facing north; made by tools/haunted_midway_data.py whack_a_zombie()), moved to whichever hole's head
 * is up ({@link WhackAZombieBlock#HEAD}) and turned to the cabinet's facing, rising out of its hole by
 * {@link WhackAZombieBlock#rise} from when it popped. Nothing is drawn while no head is up.
 */
public class WhackAZombieRenderer implements BlockEntityRenderer<WhackAZombieBlockEntity, WhackAZombieRenderer.State> {
	public static final class State extends BlockEntityRenderState {
		final List<BlockStateModelPart> parts = new ArrayList<>();
		Direction facing = Direction.NORTH;
		int hole = -1;
		float rise;
	}

	public WhackAZombieRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	@Override
	public void extractRenderState(WhackAZombieBlockEntity entity, State state, float partialTick, Vec3 camera,
			ModelFeatureRenderer.@Nullable CrumblingOverlay crumbling) {
		BlockEntityRenderState.extractBase(entity, state, crumbling);
		state.parts.clear();
		BlockState block = entity.getBlockState();
		Level level = entity.getLevel();
		if (!(block.getBlock() instanceof WhackAZombieBlock) || level == null || block.getValue(WhackAZombieBlock.HEAD) == 0) {
			return;
		}
		state.facing = block.getValue(MultiDecorationBlock.FACING);
		state.hole = block.getValue(WhackAZombieBlock.HEAD) - 1;
		Minecraft.getInstance().getModelManager().getBlockStateModelSet()
				.get(block.setValue(WhackAZombieBlock.RUNNER, true).setValue(MultiDecorationBlock.FACING, Direction.NORTH))
				.collectParts(RandomSource.create(0L), state.parts);
		long marked = entity.marked();
		state.rise = marked == Long.MIN_VALUE ? 1.0F : WhackAZombieBlock.rise(level.getGameTime() - marked + partialTick);
	}

	@Override
	public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
		if (state.parts.isEmpty() || state.hole < 0) {
			return;
		}
		pose.pushPose();
		pose.translate(0.5F, 0.0F, 0.5F);
		pose.rotateDegrees(Axis.YP, -RockingChairRenderer.yRotation(state.facing));
		pose.translate(-0.5F, 0.0F, -0.5F);
		float dx = WhackAZombieBlock.HOLES_X[state.hole] - WhackAZombieBlock.HOLES_X[0];
		float dz = WhackAZombieBlock.HOLES_Z[state.hole] - WhackAZombieBlock.HOLES_Z[0];
		float drop = (1.0F - state.rise) * (WhackAZombieBlock.HEAD_PIXELS + 0.5F);
		pose.translate(dx / 16.0F, -drop / 16.0F, dz / 16.0F);
		collector.submitBlockModel(pose, RenderTypes.entityCutout(TextureAtlas.LOCATION_BLOCKS), state.parts, new int[0], state.lightCoords,
				OverlayTexture.NO_OVERLAY, 0);
		pose.popPose();
	}

	@Override
	public boolean shouldRenderOffScreen() {
		return true; // A head rises into its upper block.
	}
}

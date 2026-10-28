package io.github.jimbozoomer.jugcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.jimbozoomer.jugcraft.agriculture.GhostTrainBlock;
import io.github.jimbozoomer.jugcraft.agriculture.GhostTrainBlockEntity;
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
 * The Ghost Train's doors and ghost (Haunted Midway, part 4): its blockstate's runner models 1 and 2 (the IN doorway's
 * left and right door leaves) and 3 (its ghost on a spring), drawn facing north in the first block's own model (the
 * frame's x 64; made by tools/haunted_midway_data.py ghost_train()), turned to the front's facing. Each doorway's leaves
 * swing about their hinges at its sides by {@link GhostTrainBlock#swing}, a quarter turn at most, the IN doors inward and
 * the OUT doors (the IN leaves moved two blocks across) outward; the ghost springs up from behind the IN sign by
 * {@link GhostTrainBlock#spook}, wobbling on its spring.
 */
public class GhostTrainRenderer implements BlockEntityRenderer<GhostTrainBlockEntity, GhostTrainRenderer.State> {
	/** The OUT doorway, two blocks across from the IN, in pixels; and how far the ghost springs. */
	private static final float OUT_SHIFT = -32.0F;
	private static final float SPOOK_RISE = 6.0F;
	/** The ghost's model lies a block under where it springs (a block model stays within -16..32), and its spring's top (pixels, in the model), where it wobbles. */
	private static final float SPOOK_LIFT = 1.0F;
	private static final float SPOOK_FOOT = 25.0F;

	public static final class State extends BlockEntityRenderState {
		final List<List<BlockStateModelPart>> parts = new ArrayList<>();
		Direction facing = Direction.NORTH;
		final float[] open = new float[2];
		float spook;
		float wobble;
	}

	public GhostTrainRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	@Override
	public void extractRenderState(GhostTrainBlockEntity entity, State state, float partialTick, Vec3 camera,
			ModelFeatureRenderer.@Nullable CrumblingOverlay crumbling) {
		BlockEntityRenderState.extractBase(entity, state, crumbling);
		state.parts.clear();
		BlockState block = entity.getBlockState();
		Level level = entity.getLevel();
		if (!(block.getBlock() instanceof GhostTrainBlock) || level == null) {
			return;
		}
		state.facing = block.getValue(MultiDecorationBlock.FACING);
		for (int runner = 1; runner <= 3; runner++) {
			List<BlockStateModelPart> parts = new ArrayList<>();
			Minecraft.getInstance().getModelManager().getBlockStateModelSet()
					.get(block.setValue(GhostTrainBlock.RUNNER, runner).setValue(MultiDecorationBlock.FACING, Direction.NORTH))
					.collectParts(RandomSource.create(0L), parts);
			state.parts.add(parts);
		}
		float now = level.getGameTime() + partialTick;
		for (int door = 0; door < 2; door++) {
			long changed = entity.changedAt(door);
			boolean open = block.getValue(door == 0 ? GhostTrainBlock.IN_OPEN : GhostTrainBlock.OUT_OPEN);
			state.open[door] = GhostTrainBlock.swing(open, changed == Long.MIN_VALUE ? -1.0F : now - changed);
		}
		long marked = entity.marked();
		float since = marked == Long.MIN_VALUE ? -1.0F : now - marked;
		state.spook = GhostTrainBlock.spook(since);
		state.wobble = (float) Math.sin(since * 0.6F) * 12.0F * state.spook;
	}

	@Override
	public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
		if (state.parts.size() < 3) {
			return;
		}
		pose.pushPose();
		pose.translate(0.5F, 0.0F, 0.5F);
		pose.rotateDegrees(Axis.YP, -RockingChairRenderer.yRotation(state.facing));
		pose.translate(-0.5F, 0.0F, -0.5F);
		float leafZ = (GhostTrainBlock.WALL_FROM + GhostTrainBlock.WALL_TO) / 2.0F / 16.0F;
		for (int door = 0; door < 2; door++) {
			// A quarter turn about each hinge: the IN doors' free edges go in (away from the front), the OUT doors' out.
			float turn = 90.0F * state.open[door] * (door == 0 ? 1.0F : -1.0F);
			float shift = door == 0 ? 0.0F : OUT_SHIFT / 16.0F;
			for (int leaf = 0; leaf < 2; leaf++) {
				float hinge = shift + (leaf == 0 ? -16.0F : 0.0F) / 16.0F;
				pose.pushPose();
				pose.translate(hinge, 0.0F, leafZ);
				pose.rotateDegrees(Axis.YP, leaf == 0 ? -turn : turn);
				pose.translate(-hinge, 0.0F, -leafZ);
				pose.translate(shift, 0.0F, 0.0F);
				collector.submitBlockModel(pose, RenderTypes.entityCutout(TextureAtlas.LOCATION_BLOCKS), state.parts.get(leaf), new int[0],
						state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
				pose.popPose();
			}
		}
		if (state.spook > 0.0F) {
			pose.pushPose();
			pose.translate(0.0F, SPOOK_LIFT + (state.spook - 1.0F) * SPOOK_RISE / 16.0F, 0.0F);
			pose.translate(-8.0F / 16.0F, SPOOK_FOOT / 16.0F, 0.0F);
			pose.rotateDegrees(Axis.ZP, state.wobble);
			pose.translate(8.0F / 16.0F, -SPOOK_FOOT / 16.0F, 0.0F);
			collector.submitBlockModel(pose, RenderTypes.entityCutout(TextureAtlas.LOCATION_BLOCKS), state.parts.get(2), new int[0],
					state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
			pose.popPose();
		}
		pose.popPose();
	}

	@Override
	public boolean shouldRenderOffScreen() {
		return true; // Its doors and ghost are across its five blocks.
	}
}

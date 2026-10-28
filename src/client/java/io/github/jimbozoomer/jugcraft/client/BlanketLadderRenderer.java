package io.github.jimbozoomer.jugcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.agriculture.BlanketLadderBlockEntity;
import io.github.jimbozoomer.jugcraft.agriculture.MultiDecorationBlock;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.WoolCarpetBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * The throws on a Blanket Ladder (Cozy Fall Hearth, part 4): each carpet hung on it folded over a rung, the top rung first,
 * a long fold hanging to the front and a short one behind, in a twill weave with a striped border and a fringe
 * ({@code textures/entity/throw_blanket.png}, made by tools/hearth_data.py throw_blanket() in greys) drawn in its carpet's
 * colour. Drawn from the ladder's first block, facing north and turned to its facing; the rungs are the block model's
 * (tools/hearth_data.py blanket_ladder()), so their heights and depths here are its.
 */
public class BlanketLadderRenderer implements BlockEntityRenderer<BlanketLadderBlockEntity, BlanketLadderRenderer.State> {
	private static final RenderType THROW = RenderTypes.entityCutout(Jugcraft.id("textures/entity/throw_blanket.png"));
	/** The rungs a throw hangs from, top first: each one's height and depth from the ladder's first block, in pixels. */
	private static final float[][] RUNGS = {{26.0F, 14.52F}, {20.75F, 12.34F}, {15.5F, 10.17F}};
	/** Each throw is drawn as if its rung were at this height, so its weave, border and fringe fall the same on every rung. */
	private static final float LOCAL_RUNG = 12.0F;
	/** How far its folds hang below the rung, in front and behind, and half its folded width, in pixels. */
	private static final float FRONT_DROP = 7.0F;
	private static final float BACK_DROP = 4.0F;
	private static final float HALF_WIDTH = 3.5F;

	public static final class State extends BlockEntityRenderState {
		int[] colors = new int[0];
		Direction facing = Direction.NORTH;
	}

	public BlanketLadderRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	@Override
	public void extractRenderState(BlanketLadderBlockEntity ladder, State state, float partialTick, Vec3 camera,
			ModelFeatureRenderer.@Nullable CrumblingOverlay crumbling) {
		BlockEntityRenderState.extractBase(ladder, state, crumbling);
		state.colors = ladder.throwsOn().stream().mapToInt(BlanketLadderRenderer::color).toArray();
		BlockState block = ladder.getBlockState();
		state.facing = block.hasProperty(MultiDecorationBlock.FACING) ? block.getValue(MultiDecorationBlock.FACING) : Direction.NORTH;
	}

	/** A carpet's dye colour, opaque. */
	private static int color(ItemStack carpet) {
		return carpet.getItem() instanceof BlockItem item && item.getBlock() instanceof WoolCarpetBlock wool
				? 0xFF000000 | wool.getColor().getTextureDiffuseColor() : 0xFFFFFFFF;
	}

	@Override
	public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
		if (state.colors.length == 0) {
			return;
		}
		int light = state.lightCoords;
		pose.pushPose();
		pose.translate(0.5F, 0.0F, 0.5F);
		pose.rotateDegrees(Axis.YP, -RockingChairRenderer.yRotation(state.facing));
		pose.translate(-0.5F, 0.0F, -0.5F);
		for (int i = 0; i < state.colors.length && i < RUNGS.length; i++) {
			int argb = state.colors[i];
			float z = RUNGS[i][1];
			pose.pushPose();
			pose.translate(0.0F, (RUNGS[i][0] - LOCAL_RUNG) / 16.0F, 0.0F);
			collector.submitCustomGeometry(pose, THROW, (matrix, buffer) -> {
				float x0 = 8.0F - HALF_WIDTH;
				float x1 = 8.0F + HALF_WIDTH;
				TintedBoxes.box(buffer, matrix, x0, LOCAL_RUNG + 0.5F, z - 0.9F, x1, LOCAL_RUNG + 0.9F, z + 0.9F, argb, light);
				TintedBoxes.box(buffer, matrix, x0, LOCAL_RUNG - FRONT_DROP, z - 0.9F, x1, LOCAL_RUNG + 0.5F, z - 0.5F, argb, light);
				TintedBoxes.box(buffer, matrix, x0, LOCAL_RUNG - BACK_DROP, z + 0.5F, x1, LOCAL_RUNG + 0.5F, z + 0.9F, argb, light);
			});
			pose.popPose();
		}
		pose.popPose();
	}

	@Override
	public boolean shouldRenderOffScreen() {
		return true; // It stands two blocks high from its first.
	}
}

package io.github.jimbozoomer.jugcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.jimbozoomer.jugcraft.agriculture.ProduceDisplayBlock;
import io.github.jimbozoomer.jugcraft.agriculture.ProduceDisplayBlockEntity;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Draws the produce on a Pumpkin Patch Market display ({@link ProduceDisplayBlock}): each piece on its spot, standing on
 * the spot's floor, {@link ProduceDisplayBlock#scale} of a block across and turned a little ({@link ProduceDisplayBlock#TURNS}),
 * lit as the block it stands in is. The spots are in the display's frame (pixels, facing north, x across to the placer's
 * left from its far block), drawn from its first block as the Giant's Beating Heart is.
 */
public class ProduceDisplayRenderer implements BlockEntityRenderer<ProduceDisplayBlockEntity, ProduceDisplayRenderer.State> {
	private final ItemModelResolver itemModels;

	public static final class State extends BlockEntityRenderState {
		final List<ItemStackRenderState> produce = new ArrayList<>();
		final List<Integer> lights = new ArrayList<>();
		float[][] spots = new float[0][];
		float scale = 1.0F;
		int across = 1;
		int count;
		Direction facing = Direction.NORTH;
	}

	public ProduceDisplayRenderer(BlockEntityRendererProvider.Context context) {
		this.itemModels = context.itemModelResolver();
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	@Override
	public void extractRenderState(ProduceDisplayBlockEntity display, State state, float partialTick, Vec3 camera,
			ModelFeatureRenderer.@Nullable CrumblingOverlay crumbling) {
		BlockEntityRenderState.extractBase(display, state, crumbling);
		BlockState block = display.getBlockState();
		state.count = 0;
		if (!(block.getBlock() instanceof ProduceDisplayBlock kind)) {
			return;
		}
		state.facing = block.getValue(ProduceDisplayBlock.FACING);
		state.spots = kind.spots();
		state.scale = kind.scale();
		state.across = kind.across();
		List<ItemStack> produce = display.produce();
		state.count = Math.min(produce.size(), state.spots.length);
		while (state.produce.size() < state.count) {
			state.produce.add(new ItemStackRenderState());
			state.lights.add(state.lightCoords);
		}
		Level level = display.getLevel();
		for (int i = 0; i < state.count; i++) {
			itemModels.updateForTopItem(state.produce.get(i), produce.get(i), ItemDisplayContext.FIXED, level, null,
					(int) display.getBlockPos().asLong() + i);
			state.lights.set(i, level == null ? state.lightCoords
					: LightCoordsUtil.getLightCoords(level, blockAt(kind, display.getBlockPos(), state.facing, state.spots[i])));
		}
	}

	/** The block a spot (pixels in the display's frame) lies in, from its first block. */
	private static BlockPos blockAt(ProduceDisplayBlock kind, BlockPos master, Direction facing, float[] spot) {
		int frameAcross = (int) Math.floor(spot[0] / 16.0F);
		int up = (int) Math.floor(spot[1] / 16.0F);
		int away = (int) Math.floor(spot[2] / 16.0F);
		int right = kind.across() - 1 - frameAcross;
		return master.relative(facing.getCounterClockWise(), right).above(up).relative(facing.getOpposite(), away);
	}

	@Override
	public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
		if (state.count == 0) {
			return;
		}
		pose.pushPose();
		pose.translate(0.5F, 0.0F, 0.5F);
		pose.rotateDegrees(Axis.YP, -RockingChairRenderer.yRotation(state.facing));
		pose.translate(-0.5F, 0.0F, -0.5F);
		// The frame runs across from the display's far block, to the left of its first (as the placer saw it).
		pose.translate(-(state.across - 1), 0.0F, 0.0F);
		for (int i = 0; i < state.count; i++) {
			if (state.produce.get(i).isEmpty()) {
				continue;
			}
			float[] spot = state.spots[i];
			pose.pushPose();
			pose.translate(spot[0] / 16.0F, spot[1] / 16.0F + state.scale * 0.25F, spot[2] / 16.0F);
			pose.rotateDegrees(Axis.YP, ProduceDisplayBlock.TURNS[i % ProduceDisplayBlock.TURNS.length]);
			pose.scale(state.scale, state.scale, state.scale);
			state.produce.get(i).submit(pose, collector, state.lights.get(i), OverlayTexture.NO_OVERLAY, 0);
			pose.popPose();
		}
		pose.popPose();
	}

	/** The produce stands over the display's other blocks, past its first block's box. */
	@Override
	public boolean shouldRenderOffScreen() {
		return true;
	}
}

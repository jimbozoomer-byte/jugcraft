package io.github.jimbozoomer.jugcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.jimbozoomer.jugcraft.agriculture.PlanterBlock;
import io.github.jimbozoomer.jugcraft.agriculture.PlanterBlockEntity;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * The flowers growing in a planter (Fall Garden & Porch, part 2): each one's own block model, standing in its spot on the
 * soil ({@link PlanterBlock.Planter}, drawn facing north and turned to the planter's facing), as big as the planter says.
 */
public class PlanterRenderer implements BlockEntityRenderer<PlanterBlockEntity, PlanterRenderer.State> {
	private static final RenderType BLOCKS = RenderTypes.entityCutout(TextureAtlas.LOCATION_BLOCKS);

	public static final class State extends BlockEntityRenderState {
		final List<List<BlockStateModelPart>> plants = new ArrayList<>();
		PlanterBlock.@Nullable Planter planter;
		Direction facing = Direction.NORTH;
	}

	public PlanterRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	@Override
	public void extractRenderState(PlanterBlockEntity planted, State state, float partialTick, Vec3 camera,
			ModelFeatureRenderer.@Nullable CrumblingOverlay crumbling) {
		BlockEntityRenderState.extractBase(planted, state, crumbling);
		state.plants.clear();
		BlockState block = planted.getBlockState();
		if (!(block.getBlock() instanceof PlanterBlock planter)) {
			state.planter = null;
			return;
		}
		state.planter = planter.planter();
		state.facing = block.getValue(PlanterBlock.FACING);
		long seed = planted.getBlockPos().asLong();
		for (ItemStack stack : planted.plants()) {
			List<BlockStateModelPart> parts = new ArrayList<>();
			if (stack.getItem() instanceof BlockItem item) {
				// The plant's own model, a variant picked by where it stands (a bed of them doesn't repeat).
				Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(item.getBlock().defaultBlockState())
						.collectParts(RandomSource.create(seed + state.plants.size()), parts);
			}
			state.plants.add(parts);
		}
	}

	@Override
	public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
		PlanterBlock.Planter planter = state.planter;
		if (planter == null || state.plants.isEmpty()) {
			return;
		}
		pose.pushPose();
		pose.translate(0.5F, 0.0F, 0.5F);
		pose.rotateDegrees(Axis.YP, -RockingChairRenderer.yRotation(state.facing));
		pose.translate(-0.5F, 0.0F, -0.5F);
		for (int i = 0; i < state.plants.size() && i < planter.spots().length; i++) {
			List<BlockStateModelPart> parts = state.plants.get(i);
			if (parts.isEmpty()) {
				continue;
			}
			double[] spot = planter.spots()[i];
			pose.pushPose();
			pose.translate((float) spot[0] / 16.0F, (float) planter.soil() / 16.0F, (float) spot[1] / 16.0F);
			pose.scale(planter.scale(), planter.scale(), planter.scale());
			pose.translate(-0.5F, 0.0F, -0.5F);
			collector.submitBlockModel(pose, BLOCKS, parts, new int[0], state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
			pose.popPose();
		}
		pose.popPose();
	}
}

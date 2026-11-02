package io.github.jimbozoomer.jugcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.jimbozoomer.jugcraft.agriculture.DecorationBlockEntity;
import io.github.jimbozoomer.jugcraft.agriculture.MultiDecorationBlock;
import io.github.jimbozoomer.jugcraft.agriculture.PorchSwingBlock;
import io.github.jimbozoomer.jugcraft.agriculture.Seat;
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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * The Porch Swing swinging (Fall Garden & Porch, part 3): its blockstate's runner model (the seat, back, arms, chains
 * and rings, drawn facing north from its first block; made by tools/fall_garden_data.py swing()), turned to the swing's
 * facing and swung about the line through its two hooks by {@link PorchSwingBlock#swing}: a little in the breeze, more
 * with someone sitting on it.
 */
public class PorchSwingRenderer implements BlockEntityRenderer<DecorationBlockEntity, PorchSwingRenderer.State> {
	/** The hooks' line, from its first block (pixels: height and depth, facing north). */
	private static final float PIVOT_Y = 31.5F;
	private static final float PIVOT_Z = 8.0F;

	public static final class State extends BlockEntityRenderState {
		final List<BlockStateModelPart> parts = new ArrayList<>();
		Direction facing = Direction.NORTH;
		float swing;
	}

	public PorchSwingRenderer(BlockEntityRendererProvider.Context context) {
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
		if (!(block.getBlock() instanceof PorchSwingBlock swing) || level == null) {
			return;
		}
		state.facing = block.getValue(MultiDecorationBlock.FACING);
		Minecraft.getInstance().getModelManager().getBlockStateModelSet()
				.get(block.setValue(PorchSwingBlock.RUNNER, true).setValue(MultiDecorationBlock.FACING, Direction.NORTH))
				.collectParts(RandomSource.create(0L), state.parts);
		BlockPos master = entity.getBlockPos();
		boolean occupied = !Seat.at(level, master).isEmpty() || !Seat.at(level, swing.partPos(master, state.facing, 1)).isEmpty();
		state.swing = PorchSwingBlock.swing(master, occupied, level.getGameTime() + partialTick);
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
		pose.translate(0.0F, PIVOT_Y / 16.0F, PIVOT_Z / 16.0F);
		pose.rotateDegrees(Axis.XP, state.swing);
		pose.translate(0.0F, -PIVOT_Y / 16.0F, -PIVOT_Z / 16.0F);
		collector.submitBlockModel(pose, RenderTypes.entityCutout(TextureAtlas.LOCATION_BLOCKS), state.parts, new int[0], state.lightCoords,
				OverlayTexture.NO_OVERLAY, 0);
		pose.popPose();
	}

	@Override
	public boolean shouldRenderOffScreen() {
		return true; // It hangs across two blocks and two up from its first.
	}
}

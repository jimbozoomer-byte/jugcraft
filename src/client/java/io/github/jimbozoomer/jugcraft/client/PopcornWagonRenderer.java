package io.github.jimbozoomer.jugcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.jimbozoomer.jugcraft.agriculture.DecorationBlockEntity;
import io.github.jimbozoomer.jugcraft.agriculture.MultiDecorationBlock;
import io.github.jimbozoomer.jugcraft.agriculture.PopcornWagonBlock;
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
 * The Caramel-Corn Wagon's kettle (Haunted Midway, part 1): its blockstate's runner model (the kettle, its lid, the
 * pins it hangs by and its dump handle, drawn facing north in the wagon's first block's frame; made by
 * tools/haunted_midway_data.py popcorn_wagon()), turned to the wagon's facing. While a kernel pops in it, it jiggles on
 * its pins ({@link TeaKettleBlock#rattle}, a degree or two); each time it spills, it tips forward about its pivot by
 * {@link PopcornWagonBlock#tip} and back.
 */
public class PopcornWagonRenderer implements BlockEntityRenderer<DecorationBlockEntity, PopcornWagonRenderer.State> {
	/** How far the kettle jiggles while a kernel pops in it, in degrees. */
	private static final float JIGGLE = 2.0F;

	public static final class State extends BlockEntityRenderState {
		final List<BlockStateModelPart> parts = new ArrayList<>();
		Direction facing = Direction.NORTH;
		float tip;
	}

	public PopcornWagonRenderer(BlockEntityRendererProvider.Context context) {
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
		if (!(block.getBlock() instanceof PopcornWagonBlock) || level == null) {
			return;
		}
		state.facing = block.getValue(MultiDecorationBlock.FACING);
		Minecraft.getInstance().getModelManager().getBlockStateModelSet()
				.get(block.setValue(PopcornWagonBlock.RUNNER, true).setValue(MultiDecorationBlock.FACING, Direction.NORTH))
				.collectParts(RandomSource.create(0L), state.parts);
		float time = level.getGameTime() + partialTick;
		long marked = entity.marked();
		float spill = marked == Long.MIN_VALUE ? 0.0F : PopcornWagonBlock.tip(level.getGameTime() - marked + partialTick);
		state.tip = spill + JIGGLE * TeaKettleBlock.rattle(entity.getBlockPos(), block.getValue(PopcornWagonBlock.POPPING), time);
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
		pose.translate(0.0F, PopcornWagonBlock.PIVOT_Y / 16.0F, PopcornWagonBlock.PIVOT_Z / 16.0F);
		pose.rotateDegrees(Axis.XP, -state.tip); // Its front down.
		pose.translate(0.0F, -PopcornWagonBlock.PIVOT_Y / 16.0F, -PopcornWagonBlock.PIVOT_Z / 16.0F);
		collector.submitBlockModel(pose, RenderTypes.entityCutout(TextureAtlas.LOCATION_BLOCKS), state.parts, new int[0], state.lightCoords,
				OverlayTexture.NO_OVERLAY, 0);
		pose.popPose();
	}

	@Override
	public boolean shouldRenderOffScreen() {
		return true; // Its kettle hangs in its upper block.
	}
}

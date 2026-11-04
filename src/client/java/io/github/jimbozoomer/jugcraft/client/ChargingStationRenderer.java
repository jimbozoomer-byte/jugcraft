package io.github.jimbozoomer.jugcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.jimbozoomer.jugcraft.tools.ChargingStationBlock;
import io.github.jimbozoomer.jugcraft.tools.ChargingStationBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/** Draws the tool on a charging station's cradle, upright and side-on, at chest height in front of the cabinet. */
public class ChargingStationRenderer implements BlockEntityRenderer<ChargingStationBlockEntity, ChargingStationRenderer.State> {
	private final ItemModelResolver itemModels;

	public static final class State extends BlockEntityRenderState {
		final ItemStackRenderState[] tools = {new ItemStackRenderState(), new ItemStackRenderState()};
		Direction facing = Direction.NORTH;
	}

	public ChargingStationRenderer(BlockEntityRendererProvider.Context context) {
		this.itemModels = context.itemModelResolver();
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	@Override
	public void extractRenderState(ChargingStationBlockEntity station, State state, float partialTick, Vec3 camera,
			ModelFeatureRenderer.@Nullable CrumblingOverlay crumbling) {
		BlockEntityRenderState.extractBase(station, state, crumbling);
		state.facing = station.getBlockState().getValue(ChargingStationBlock.FACING);
		for(int i=0;i<2;i++) itemModels.updateForTopItem(state.tools[i], station.tool(i), ItemDisplayContext.FIXED, station.getLevel(), null,
                (int) station.getBlockPos().asLong()+i);
	}

	@Override
	public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        boolean both=!state.tools[0].isEmpty()&&!state.tools[1].isEmpty();
        for(int i=0;i<2;i++) {
            if(state.tools[i].isEmpty())continue;
            pose.pushPose();
            pose.translate(.5F,0,.5F);
            pose.rotateDegrees(Axis.YP,-yRotation(state.facing));
            pose.translate(both?(i==0?-.22F:.22F):0F,1.5F,-.32F);
            pose.rotateDegrees(Axis.YP,90);
            float size=both?.8F:1.2F;pose.scale(size,size,size);
            state.tools[i].submit(pose,collector,state.lightCoords,OverlayTexture.NO_OVERLAY,0);
            pose.popPose();
        }
	}

	private static float yRotation(Direction facing) {
		return switch (facing) {
			case EAST -> 90;
			case SOUTH -> 180;
			case WEST -> 270;
			default -> 0;
		};
	}

	@Override
	public boolean shouldRenderOffScreen() {
		return true; // The tool hangs in the upper block, above the block entity's own.
	}
}

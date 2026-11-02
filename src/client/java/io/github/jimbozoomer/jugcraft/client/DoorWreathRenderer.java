package io.github.jimbozoomer.jugcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.jimbozoomer.jugcraft.agriculture.AutumnWreathBlock;
import io.github.jimbozoomer.jugcraft.agriculture.DoorWreath;
import io.github.jimbozoomer.jugcraft.agriculture.GargoyleKnockerBlock;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;

/**
 * Reuses the normal block model and textures; the saved attachment supplies the face and position. A Gargoyle Door
 * Knocker's ring and eyes are drawn as on a wall (client/GargoyleKnockerRenderer), from the attachment's last knock.
 */
public final class DoorWreathRenderer extends EntityRenderer<DoorWreath, DoorWreathRenderer.State> {
	public static final class State extends EntityRenderState {
		Direction face = Direction.NORTH;
		final List<BlockStateModelPart> parts = new ArrayList<>();
		final List<BlockStateModelPart> ring = new ArrayList<>();
		final List<BlockStateModelPart> eyes = new ArrayList<>();
		float since = Float.MAX_VALUE;
	}
	public DoorWreathRenderer(EntityRendererProvider.Context context) { super(context); }
	@Override public State createRenderState() { return new State(); }
	@Override public void extractRenderState(DoorWreath entity, State state, float partialTick) {
		super.extractRenderState(entity, state, partialTick);
		state.x = entity.getX(); state.y = entity.getY(); state.z = entity.getZ();
		state.face = entity.getDirection(); state.parts.clear();
		if (entity.wreathState().getBlock() instanceof AutumnWreathBlock)
			Minecraft.getInstance().getModelManager().getBlockStateModelSet()
					.get(entity.wreathState().setValue(AutumnWreathBlock.FACING, state.face)).collectParts(RandomSource.create(0), state.parts);
		state.ring.clear();
		if (entity.wreathState().getBlock() instanceof GargoyleKnockerBlock) {
			GargoyleKnockerRenderer.collect(entity.wreathState(), state.ring, state.eyes);
			state.since = GargoyleKnockerRenderer.since(entity.knockedAt(), entity.level(), partialTick);
		}
	}
	@Override public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
		pose.pushPose();
		double offset = 0.5 - DoorWreath.THICKNESS / 2;
		pose.translate(-0.5 + state.face.getStepX() * offset, -0.5, -0.5 + state.face.getStepZ() * offset);
		collector.submitBlockModel(pose, RenderTypes.entityCutout(TextureAtlas.LOCATION_BLOCKS), state.parts,
				new int[0], state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
		if (!state.ring.isEmpty()) {
			GargoyleKnockerRenderer.draw(pose, collector, state.ring, state.eyes, state.face, state.since, state.lightCoords);
		}
		pose.popPose();
		super.submit(state, pose, collector, camera);
	}
}

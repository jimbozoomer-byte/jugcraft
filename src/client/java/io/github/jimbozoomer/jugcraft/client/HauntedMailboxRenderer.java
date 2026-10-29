package io.github.jimbozoomer.jugcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.jimbozoomer.jugcraft.agriculture.HauntedMailboxBlock;
import io.github.jimbozoomer.jugcraft.agriculture.HauntedMailboxBlockEntity;
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
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * The Haunted Mailbox's haunting (Front-Yard Haunt Scenes, part 1): its blockstate's runner models 1 (its door) and 2 (its
 * eyes), drawn facing north (made by tools/front_yard_data.py haunted_mailbox()) and turned to its facing. The door
 * swings about its hinge at its foot by {@link HauntedMailboxBlock#lid}: falling open while this player is within
 * {@value HauntedMailboxBlock#STARE} blocks, rattling now and then otherwise. The eyes glow, full-bright, while it is well
 * open, by {@link HauntedMailboxBlock#eyes}. It is this player's own sight of it.
 */
public class HauntedMailboxRenderer implements BlockEntityRenderer<HauntedMailboxBlockEntity, HauntedMailboxRenderer.State> {
	public static final class State extends BlockEntityRenderState {
		final List<List<BlockStateModelPart>> parts = new ArrayList<>();
		Direction facing = Direction.NORTH;
		float lid;
		boolean eyes;
	}

	public HauntedMailboxRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	@Override
	public void extractRenderState(HauntedMailboxBlockEntity entity, State state, float partialTick, Vec3 camera,
			ModelFeatureRenderer.@Nullable CrumblingOverlay crumbling) {
		BlockEntityRenderState.extractBase(entity, state, crumbling);
		state.parts.clear();
		BlockState block = entity.getBlockState();
		Level level = entity.getLevel();
		if (!(block.getBlock() instanceof HauntedMailboxBlock) || level == null) {
			return;
		}
		state.facing = block.getValue(MultiDecorationBlock.FACING);
		for (int runner = 1; runner <= 2; runner++) {
			List<BlockStateModelPart> parts = new ArrayList<>();
			Minecraft.getInstance().getModelManager().getBlockStateModelSet()
					.get(block.setValue(HauntedMailboxBlock.RUNNER, runner).setValue(MultiDecorationBlock.FACING, Direction.NORTH))
					.collectParts(RandomSource.create(0L), parts);
			state.parts.add(parts);
		}
		Player player = Minecraft.getInstance().player;
		Vec3 box = Vec3.atCenterOf(entity.getBlockPos()).add(0.0, 0.7, 0.0);
		boolean near = player != null && !player.isSpectator()
				&& player.position().distanceToSqr(box) < HauntedMailboxBlock.STARE * HauntedMailboxBlock.STARE;
		long time = level.getGameTime();
		long since = entity.look(near, time);
		float now = time + partialTick;
		int seed = entity.getBlockPos().hashCode();
		state.lid = HauntedMailboxBlock.lid(near, now - since, now, seed);
		state.eyes = HauntedMailboxBlock.eyes(state.lid, now, seed);
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
		if (state.eyes) {
			collector.submitBlockModel(pose, RenderTypes.entityCutout(TextureAtlas.LOCATION_BLOCKS), state.parts.get(1), new int[0],
					LightCoordsUtil.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0);
		}
		// The door falls open forward, its top towards the street, about its hinge at its foot.
		pose.translate(0.0F, HauntedMailboxBlock.HINGE_Y / 16.0F, HauntedMailboxBlock.HINGE_Z / 16.0F);
		pose.rotateDegrees(Axis.XP, -state.lid);
		pose.translate(0.0F, -HauntedMailboxBlock.HINGE_Y / 16.0F, -HauntedMailboxBlock.HINGE_Z / 16.0F);
		collector.submitBlockModel(pose, RenderTypes.entityCutout(TextureAtlas.LOCATION_BLOCKS), state.parts.get(0), new int[0], state.lightCoords,
				OverlayTexture.NO_OVERLAY, 0);
		pose.popPose();
	}

	@Override
	public boolean shouldRenderOffScreen() {
		return true; // Its box is in its upper block.
	}
}

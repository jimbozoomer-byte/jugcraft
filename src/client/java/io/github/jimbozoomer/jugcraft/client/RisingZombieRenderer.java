package io.github.jimbozoomer.jugcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.jimbozoomer.jugcraft.agriculture.DecorationBlockEntity;
import io.github.jimbozoomer.jugcraft.agriculture.MultiDecorationBlock;
import io.github.jimbozoomer.jugcraft.agriculture.RisingZombieBlock;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * The Rising Zombie's arms (Front-Yard Haunt Scenes, part 2): its blockstate's runner models 1 (its left arm) and 2 (its
 * right), drawn facing north (made by tools/front_yard_data.py rising_zombie()) and turned to its facing. Each claws to and
 * fro about its shoulder by {@link RisingZombieBlock#claw}, and leans nearer upright by {@link RisingZombieBlock#lurch} the
 * nearer this player is: its own sight of it.
 */
public class RisingZombieRenderer implements BlockEntityRenderer<DecorationBlockEntity, RisingZombieRenderer.State> {
	public static final class State extends BlockEntityRenderState {
		final List<List<BlockStateModelPart>> parts = new ArrayList<>();
		Direction facing = Direction.NORTH;
		final float[] claw = new float[2];
		float lurch;
	}

	public RisingZombieRenderer(BlockEntityRendererProvider.Context context) {
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
		if (!(block.getBlock() instanceof RisingZombieBlock) || level == null) {
			return;
		}
		state.facing = block.getValue(MultiDecorationBlock.FACING);
		for (int runner = 1; runner <= 2; runner++) {
			List<BlockStateModelPart> parts = new ArrayList<>();
			Minecraft.getInstance().getModelManager().getBlockStateModelSet()
					.get(block.setValue(RisingZombieBlock.RUNNER, runner).setValue(MultiDecorationBlock.FACING, Direction.NORTH))
					.collectParts(RandomSource.create(0L), parts);
			state.parts.add(parts);
		}
		float now = level.getGameTime() + partialTick;
		int seed = entity.getBlockPos().hashCode();
		state.claw[0] = RisingZombieBlock.claw(false, now, seed);
		state.claw[1] = RisingZombieBlock.claw(true, now, seed);
		Player player = Minecraft.getInstance().player;
		Vec3 torso = Vec3.atBottomCenterOf(entity.getBlockPos()).add(0.0, 0.6, 0.0);
		state.lurch = player == null || player.isSpectator() ? 0.0F : RisingZombieBlock.lurch(player.position().distanceTo(torso));
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
		for (int arm = 0; arm < 2; arm++) {
			boolean right = arm == 1;
			float x = (right ? RisingZombieBlock.SHOULDER_X : 16.0F - RisingZombieBlock.SHOULDER_X) / 16.0F;
			pose.pushPose();
			pose.translate(x, RisingZombieBlock.SHOULDER_Y / 16.0F, RisingZombieBlock.SHOULDER_Z / 16.0F);
			// It claws forward and back; reaching higher, each arm leans in toward upright (the right's top from the east).
			pose.rotateDegrees(Axis.XP, -state.claw[arm]);
			pose.rotateDegrees(Axis.ZP, right ? state.lurch : -state.lurch);
			pose.translate(-x, -RisingZombieBlock.SHOULDER_Y / 16.0F, -RisingZombieBlock.SHOULDER_Z / 16.0F);
			collector.submitBlockModel(pose, RenderTypes.entityCutout(TextureAtlas.LOCATION_BLOCKS), state.parts.get(arm), new int[0],
					state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
			pose.popPose();
		}
		pose.popPose();
	}

	@Override
	public boolean shouldRenderOffScreen() {
		return true; // Its arms reach up into its upper block and out past its sides.
	}
}

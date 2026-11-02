package io.github.jimbozoomer.jugcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.jimbozoomer.jugcraft.agriculture.CrashedWitchBlock;
import io.github.jimbozoomer.jugcraft.agriculture.DecorationBlockEntity;
import io.github.jimbozoomer.jugcraft.agriculture.WallDecorationBlock;
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
 * The Crashed Witch's legs (Front-Yard Haunt Scenes, part 4): her blockstate's runner models 1 (her left leg) and 2 (her
 * right), drawn facing north (made by tools/front_yard_data.py crashed_witch()) and turned to her facing. Each splays
 * {@value CrashedWitchBlock#SPLAY} degrees out from her hip and kicks up and down by {@link CrashedWitchBlock#kick}: in
 * bursts, the whole time while this player is near, and after she cackles. It is this player's own sight of her.
 */
public class CrashedWitchRenderer implements BlockEntityRenderer<DecorationBlockEntity, CrashedWitchRenderer.State> {
	public static final class State extends BlockEntityRenderState {
		final List<List<BlockStateModelPart>> legs = new ArrayList<>();
		Direction facing = Direction.NORTH;
		final float[] kick = new float[2];
	}

	public CrashedWitchRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	@Override
	public void extractRenderState(DecorationBlockEntity entity, State state, float partialTick, Vec3 camera,
			ModelFeatureRenderer.@Nullable CrumblingOverlay crumbling) {
		BlockEntityRenderState.extractBase(entity, state, crumbling);
		state.legs.clear();
		BlockState block = entity.getBlockState();
		Level level = entity.getLevel();
		if (!(block.getBlock() instanceof CrashedWitchBlock) || level == null) {
			return;
		}
		state.facing = block.getValue(WallDecorationBlock.FACING);
		for (int runner = 1; runner <= 2; runner++) {
			List<BlockStateModelPart> parts = new ArrayList<>();
			Minecraft.getInstance().getModelManager().getBlockStateModelSet()
					.get(block.setValue(CrashedWitchBlock.RUNNER, runner).setValue(WallDecorationBlock.FACING, Direction.NORTH))
					.collectParts(RandomSource.create(0L), parts);
			state.legs.add(parts);
		}
		float now = level.getGameTime() + partialTick;
		int seed = entity.getBlockPos().hashCode();
		Player player = Minecraft.getInstance().player;
		double distance = player == null || player.isSpectator() ? Double.MAX_VALUE : player.position().distanceTo(Vec3.atCenterOf(entity.getBlockPos()));
		float cackled = entity.marked() == Long.MIN_VALUE ? -1.0F : now - entity.marked();
		state.kick[0] = CrashedWitchBlock.kick(false, now, seed, distance, cackled);
		state.kick[1] = CrashedWitchBlock.kick(true, now, seed, distance, cackled);
	}

	@Override
	public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
		if (state.legs.size() < 2) {
			return;
		}
		pose.pushPose();
		pose.translate(0.5F, 0.0F, 0.5F);
		pose.rotateDegrees(Axis.YP, -RockingChairRenderer.yRotation(state.facing));
		pose.translate(-0.5F, 0.0F, -0.5F);
		for (int leg = 0; leg < 2; leg++) {
			boolean right = leg == 1;
			float x = (8.0F + (right ? CrashedWitchBlock.HIP_X : -CrashedWitchBlock.HIP_X)) / 16.0F;
			pose.pushPose();
			pose.translate(x, CrashedWitchBlock.HIP_Y / 16.0F, CrashedWitchBlock.HIP_Z / 16.0F);
			// Each leg splays out to its own side, its foot toward the street, and kicks up and down.
			pose.rotateDegrees(Axis.YP, right ? -CrashedWitchBlock.SPLAY : CrashedWitchBlock.SPLAY);
			pose.rotateDegrees(Axis.XP, state.kick[leg]);
			pose.translate(-x, -CrashedWitchBlock.HIP_Y / 16.0F, -CrashedWitchBlock.HIP_Z / 16.0F);
			collector.submitBlockModel(pose, RenderTypes.entityCutout(TextureAtlas.LOCATION_BLOCKS), state.legs.get(leg), new int[0],
					state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
			pose.popPose();
		}
		pose.popPose();
	}

	@Override
	public boolean shouldRenderOffScreen() {
		return true; // Her legs reach out of her block.
	}
}

package io.github.jimbozoomer.jugcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.jimbozoomer.jugcraft.agriculture.FacingDecorationBlock;
import io.github.jimbozoomer.jugcraft.agriculture.SkeletonAnimalBlock;
import io.github.jimbozoomer.jugcraft.agriculture.SkeletonAnimalBlockEntity;
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
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * A skeleton animal's moving part (Front-Yard Haunt Scenes, part 2): its blockstate's runner model 1, drawn facing north
 * (made by tools/front_yard_data.py) and turned to its facing, about its {@link SkeletonAnimalBlock.Kind#pivot}: the dog's
 * tail swept across the ground by {@link SkeletonAnimalBlock#wag} while this player is near; the cat's tail swayed from
 * side to side by {@link SkeletonAnimalBlock#sway}; the crow's skull turned by {@link SkeletonAnimalBlock#watch} toward this
 * player, in snaps. It is this player's own sight of it.
 */
public class SkeletonAnimalRenderer implements BlockEntityRenderer<SkeletonAnimalBlockEntity, SkeletonAnimalRenderer.State> {
	public static final class State extends BlockEntityRenderState {
		final List<BlockStateModelPart> parts = new ArrayList<>();
		Direction facing = Direction.NORTH;
		SkeletonAnimalBlock.Kind kind = SkeletonAnimalBlock.Kind.DOG;
		float angle;
	}

	public SkeletonAnimalRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	@Override
	public void extractRenderState(SkeletonAnimalBlockEntity entity, State state, float partialTick, Vec3 camera,
			ModelFeatureRenderer.@Nullable CrumblingOverlay crumbling) {
		BlockEntityRenderState.extractBase(entity, state, crumbling);
		state.parts.clear();
		BlockState block = entity.getBlockState();
		Level level = entity.getLevel();
		if (!(block.getBlock() instanceof SkeletonAnimalBlock animal) || level == null) {
			return;
		}
		state.facing = block.getValue(FacingDecorationBlock.FACING);
		state.kind = animal.kind();
		Minecraft.getInstance().getModelManager().getBlockStateModelSet()
				.get(block.setValue(SkeletonAnimalBlock.RUNNER, 1).setValue(FacingDecorationBlock.FACING, Direction.NORTH))
				.collectParts(RandomSource.create(0L), state.parts);
		Player player = Minecraft.getInstance().player;
		Vec3 at = Vec3.atBottomCenterOf(entity.getBlockPos()).add(0.0, 0.3, 0.0);
		double distance = player == null || player.isSpectator() ? Double.MAX_VALUE : player.position().distanceTo(at);
		long time = level.getGameTime();
		float now = time + partialTick;
		int seed = entity.getBlockPos().hashCode();
		switch (state.kind) {
			case DOG -> {
				boolean near = distance < SkeletonAnimalBlock.NEAR;
				state.angle = SkeletonAnimalBlock.wag(near, now - entity.look(near, time), now);
			}
			case CAT -> state.angle = SkeletonAnimalBlock.sway(now, seed);
			case CROW -> {
				boolean near = distance < SkeletonAnimalBlock.WATCH;
				Vec3 toCrow = near ? at.subtract(player.position()) : Vec3.ZERO;
				float toward = Mth.wrapDegrees((float) Math.toDegrees(Math.atan2(-toCrow.x, toCrow.z)) - state.facing.toYRot() + 180.0F);
				long snap = Math.floorDiv(time + Math.floorMod(seed, SkeletonAnimalBlock.SNAP_TICKS), SkeletonAnimalBlock.SNAP_TICKS);
				state.angle = entity.head(snap, SkeletonAnimalBlock.watch(near, toward, snap, seed), now);
			}
		}
	}

	@Override
	public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
		if (state.parts.isEmpty()) {
			return;
		}
		float[] pivot = state.kind.pivot();
		pose.pushPose();
		pose.translate(0.5F, 0.0F, 0.5F);
		pose.rotateDegrees(Axis.YP, -RockingChairRenderer.yRotation(state.facing));
		pose.translate(-0.5F, 0.0F, -0.5F);
		pose.translate(pivot[0] / 16.0F, pivot[1] / 16.0F, pivot[2] / 16.0F);
		// The dog's tail sweeps and the crow's head turns about the upright; the cat's tail sways from side to side.
		pose.rotateDegrees(state.kind == SkeletonAnimalBlock.Kind.CAT ? Axis.ZP : Axis.YP, -state.angle);
		pose.translate(-pivot[0] / 16.0F, -pivot[1] / 16.0F, -pivot[2] / 16.0F);
		collector.submitBlockModel(pose, RenderTypes.entityCutout(TextureAtlas.LOCATION_BLOCKS), state.parts, new int[0], state.lightCoords,
				OverlayTexture.NO_OVERLAY, 0);
		pose.popPose();
	}
}

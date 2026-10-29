package io.github.jimbozoomer.jugcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.jimbozoomer.jugcraft.agriculture.AutumnWreathBlock;
import io.github.jimbozoomer.jugcraft.agriculture.DecorationBlockEntity;
import io.github.jimbozoomer.jugcraft.agriculture.GargoyleKnockerBlock;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * The Gargoyle Door Knocker's ring and eyes (Front-Yard Haunt Scenes, part 3): its blockstate's runner models 1 (the ring)
 * and 2 (the eyes), drawn facing north (made by tools/front_yard_data.py gargoyle_knocker()) and turned to its facing. The
 * ring swings out about its pivot in the jaws by {@link GargoyleKnockerBlock#ring} after each knock, and the eyes glow,
 * full-bright, by {@link GargoyleKnockerBlock#eyes}. On a wall its block entity marks each knock; on a door,
 * client/DoorWreathRenderer draws them the same way ({@link #draw}) from its {@code DoorWreath}'s knock.
 */
public class GargoyleKnockerRenderer implements BlockEntityRenderer<DecorationBlockEntity, GargoyleKnockerRenderer.State> {
	public static final class State extends BlockEntityRenderState {
		final List<BlockStateModelPart> ring = new ArrayList<>();
		final List<BlockStateModelPart> eyes = new ArrayList<>();
		Direction facing = Direction.NORTH;
		float since = Float.MAX_VALUE;
	}

	public GargoyleKnockerRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	/** Collects the knocker's ring and eyes, facing north, from its state {@code block}. */
	static void collect(BlockState block, List<BlockStateModelPart> ring, List<BlockStateModelPart> eyes) {
		ring.clear();
		eyes.clear();
		BlockState north = block.setValue(AutumnWreathBlock.FACING, Direction.NORTH);
		Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(north.setValue(GargoyleKnockerBlock.RUNNER, 1))
				.collectParts(RandomSource.create(0L), ring);
		Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(north.setValue(GargoyleKnockerBlock.RUNNER, 2))
				.collectParts(RandomSource.create(0L), eyes);
	}

	/** Ticks (with the partial tick) since game time {@code knocked}, or a long while for none. */
	static float since(long knocked, Level level, float partialTick) {
		return knocked == Long.MIN_VALUE ? Float.MAX_VALUE : level.getGameTime() - knocked + partialTick;
	}

	@Override
	public void extractRenderState(DecorationBlockEntity entity, State state, float partialTick, Vec3 camera,
			ModelFeatureRenderer.@Nullable CrumblingOverlay crumbling) {
		BlockEntityRenderState.extractBase(entity, state, crumbling);
		state.ring.clear();
		BlockState block = entity.getBlockState();
		Level level = entity.getLevel();
		if (!(block.getBlock() instanceof GargoyleKnockerBlock) || level == null) {
			return;
		}
		state.facing = block.getValue(AutumnWreathBlock.FACING);
		collect(block, state.ring, state.eyes);
		state.since = since(entity.marked(), level, partialTick);
	}

	/**
	 * Draws the ring and eyes in a block's frame already placed in {@code pose}, turned to {@code facing}: the ring swung
	 * out {@link GargoyleKnockerBlock#ring} degrees about its pivot, the eyes full-bright while they glow.
	 */
	static void draw(PoseStack pose, SubmitNodeCollector collector, List<BlockStateModelPart> ring, List<BlockStateModelPart> eyes,
			Direction facing, float since, int light) {
		pose.pushPose();
		pose.translate(0.5F, 0.0F, 0.5F);
		pose.rotateDegrees(Axis.YP, -RockingChairRenderer.yRotation(facing));
		pose.translate(-0.5F, 0.0F, -0.5F);
		if (GargoyleKnockerBlock.eyes(since)) {
			collector.submitBlockModel(pose, RenderTypes.entityCutout(TextureAtlas.LOCATION_BLOCKS), eyes, new int[0], LightCoordsUtil.FULL_BRIGHT,
					OverlayTexture.NO_OVERLAY, 0);
		}
		// The ring's foot swings out from the plate, toward the street, about its pivot in the jaws.
		pose.translate(0.0F, GargoyleKnockerBlock.PIVOT_Y / 16.0F, GargoyleKnockerBlock.PIVOT_Z / 16.0F);
		pose.rotateDegrees(Axis.XP, GargoyleKnockerBlock.ring(since));
		pose.translate(0.0F, -GargoyleKnockerBlock.PIVOT_Y / 16.0F, -GargoyleKnockerBlock.PIVOT_Z / 16.0F);
		collector.submitBlockModel(pose, RenderTypes.entityCutout(TextureAtlas.LOCATION_BLOCKS), ring, new int[0], light, OverlayTexture.NO_OVERLAY, 0);
		pose.popPose();
	}

	@Override
	public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
		if (!state.ring.isEmpty()) {
			draw(pose, collector, state.ring, state.eyes, state.facing, state.since, state.lightCoords);
		}
	}
}

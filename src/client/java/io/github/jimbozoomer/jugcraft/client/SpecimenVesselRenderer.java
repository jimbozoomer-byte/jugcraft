package io.github.jimbozoomer.jugcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.agriculture.DecorationBlockEntity;
import io.github.jimbozoomer.jugcraft.agriculture.SpecimenJarBlock;
import io.github.jimbozoomer.jugcraft.agriculture.SpecimenTankBlock;
import io.github.jimbozoomer.jugcraft.agriculture.SpecimenVesselBlock;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Draws what floats in a Tall Specimen Jar or a Specimen Tank, from its first block: its specimen (decor17_quads.json
 * big_specimen_*, tools/decor17_data.py specimens()), bobbing and turning slowly in the middle of the fluid, lit by the
 * fluid's glow, among bubbles rising from the bottom to the top. The numbers are tools/decor17.py SPECIMEN_VESSELS',
 * which the audit compares.
 */
public class SpecimenVesselRenderer implements BlockEntityRenderer<DecorationBlockEntity, SpecimenVesselRenderer.State> {
	private static final RenderType BUBBLE = RenderTypes.entityCutout(Jugcraft.id("textures/entity/specimen_jar_bubble.png"));
	private static final int GLOW = 0xF000C0;
	private static final int BOB_TICKS = 120;
	/** The Tall Specimen Jar: blocks across, its specimen's middle (pixels in its frame), scale and bob, its bubbles and fluid. */
	private static final int TALL_ACROSS = 1;
	private static final float[] TALL_MIDDLE = {8.0F, 13.5F, 8.0F};
	private static final float TALL_SCALE = 0.55F;
	private static final float TALL_BOB = 1.2F;
	private static final int TALL_BUBBLES = 4;
	private static final float[] TALL_FLUID = {3.5F, 1.5F, 12.5F, 26.0F};
	/** The Specimen Tank, the same. */
	private static final int TANK_ACROSS = 2;
	private static final float[] TANK_MIDDLE = {16.0F, 16.5F, 16.0F};
	private static final float TANK_SCALE = 1.35F;
	private static final float TANK_BOB = 1.5F;
	private static final int TANK_BUBBLES = 7;
	private static final float[] TANK_FLUID = {2.0F, 3.0F, 30.0F, 27.0F};

	public static final class State extends BlockEntityRenderState {
		SpecimenJarBlock.Specimen specimen = SpecimenJarBlock.Specimen.EYE;
		Direction facing = Direction.NORTH;
		boolean tank;
		boolean whole;
		float bob;
		float turn;
		float time;
	}

	public SpecimenVesselRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	@Override
	public void extractRenderState(DecorationBlockEntity vessel, State state, float partialTick, Vec3 camera,
			ModelFeatureRenderer.@Nullable CrumblingOverlay crumbling) {
		BlockEntityRenderState.extractBase(vessel, state, crumbling);
		BlockState block = vessel.getBlockState();
		state.whole = block.getBlock() instanceof SpecimenVesselBlock && vessel.getLevel() != null;
		if (!state.whole) {
			return;
		}
		state.specimen = block.getValue(SpecimenVesselBlock.SPECIMEN);
		state.facing = block.getValue(SpecimenVesselBlock.FACING);
		state.tank = block.getBlock() instanceof SpecimenTankBlock;
		state.time = vessel.getLevel().getGameTime() % 24000 + partialTick;
		float phase = (vessel.getBlockPos().hashCode() & 0xFF) / 256.0F;
		state.bob = (state.tank ? TANK_BOB : TALL_BOB) * Mth.sin((state.time / BOB_TICKS + phase) * Mth.TWO_PI);
		state.turn = (state.time * 0.6F + (vessel.getBlockPos().hashCode() & 0xFF)) % 360.0F;
	}

	@Override
	public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
		if (!state.whole) {
			return;
		}
		int across = state.tank ? TANK_ACROSS : TALL_ACROSS;
		float[] middle = state.tank ? TANK_MIDDLE : TALL_MIDDLE;
		float scale = state.tank ? TANK_SCALE : TALL_SCALE;
		float[] fluid = state.tank ? TANK_FLUID : TALL_FLUID;
		int bubbles = state.tank ? TANK_BUBBLES : TALL_BUBBLES;
		pose.pushPose();
		pose.translate(0.5F, 0.0F, 0.5F);
		pose.rotateDegrees(Axis.YP, -RockingChairRenderer.yRotation(state.facing));
		pose.translate(-0.5F, 0.0F, -0.5F);
		// The vessel's frame runs across from its far block, to the left of the first (as the placer saw it).
		pose.translate(-(across - 1), 0.0F, 0.0F);
		QuadModel specimen = DecorQuads.get("big_specimen_" + state.specimen.getSerializedName());
		if (specimen != null) {
			pose.pushPose();
			pose.translate(middle[0] / 16, (middle[1] + state.bob) / 16, middle[2] / 16);
			pose.rotateDegrees(Axis.YP, state.turn);
			pose.scale(scale, scale, scale);
			specimen.submit(pose, collector, GLOW);
			pose.popPose();
		}
		float time = state.time;
		float size = (state.tank ? 0.8F : 0.6F) / 16;
		collector.submitCustomGeometry(pose, BUBBLE, (matrix, buffer) -> {
			for (int i = 0; i < bubbles; i++) {
				float rise = (time / 50.0F + i * 0.37F) % 1.0F;
				float x = (fluid[0] + 1.0F + (fluid[2] - fluid[0] - 2.0F) * (i + 0.5F) / bubbles) / 16;
				float z = (fluid[0] + 1.5F + (fluid[2] - fluid[0] - 3.0F) * ((i * 5 + 2) % bubbles + 0.5F) / bubbles) / 16;
				float y = (fluid[1] + 0.5F + rise * (fluid[3] - fluid[1] - 1.5F)) / 16;
				DecorDraw.box(buffer, matrix, x, y, z, x + size, y + size, z + size, 0, 0, 1, 1, 0xFFFFFFFF, GLOW, DecorDraw.ALL);
			}
		});
		pose.popPose();
	}

	/** The fluid fills a jar two blocks tall, or a tank two blocks every way, past the first block's box. */
	@Override
	public boolean shouldRenderOffScreen() {
		return true;
	}
}

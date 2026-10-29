package io.github.jimbozoomer.jugcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.jimbozoomer.jugcraft.agriculture.ShowControllerBlock;
import io.github.jimbozoomer.jugcraft.agriculture.ShowControllerBlockEntity;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Draws the Show Controller's moving parts (tools/clockwork_data.py, clockwork_quads.json): its programme drum, turning
 * once every {@link ShowControllerBlock#DRUM_TICKS} ticks about its axle while the show runs; its gauges' needles, the west
 * one showing its cast against its lamps and the east one swinging with the show's pressure, once every
 * {@link ShowControllerBlock#SWEEP_TICKS} ticks; its amber lamps along their row, one lit at full light for each figure
 * cued ({@link ShowControllerBlock#CAST}); its beacon, lit while the show runs; and its knife switch, thrown back when on
 * and forward when off. Stopped, the drum and the show gauge run down over {@link #EASE} ticks. Coordinates are pixels of
 * its block, for a controller facing north (tools/clockwork.py SHOW_CONTROLLER).
 */
public class ShowControllerRenderer implements BlockEntityRenderer<ShowControllerBlockEntity, ShowControllerRenderer.State> {
	/** The programme drum's axle (height, depth). */
	static final float DRUM_Y = 7.0F;
	static final float DRUM_Z = 5.5F;
	/** The gauges' middles (across, height), and how far either way of upright their needles swing. */
	static final float GAUGE_LEFT = 4.5F;
	static final float GAUGE_RIGHT = 11.5F;
	static final float GAUGE_Y = 12.0F;
	static final float NEEDLE_SWEEP = 120.0F;
	/** Pixels from one lamp to the next along their row. */
	static final float LAMP_STEP = 2.0F;
	/** The knife switch's pivot (height, depth), and how far either way of upright it is thrown. */
	static final float LEVER_Y = 9.0F;
	static final float LEVER_Z = 8.0F;
	static final float LEVER_THROW = 30.0F;
	/** Ticks the drum and the show gauge take to come up to speed, or run down. */
	static final float EASE = 10.0F;
	/** A lamp's tint while it is out. */
	private static final int UNLIT = 0xFF403020;
	/** Each controller as this client last left it: {its drum's ticks (eased), its speed (0 to 1), the game time}. */
	private final Map<ShowControllerBlockEntity, double[]> clocks = new WeakHashMap<>();

	public static final class State extends BlockEntityRenderState {
		Direction facing = Direction.NORTH;
		boolean enabled;
		boolean running;
		int cast;
		float drum;
		float pressure;
	}

	public ShowControllerRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	@Override
	public void extractRenderState(ShowControllerBlockEntity controller, State state, float partialTick, Vec3 camera,
			ModelFeatureRenderer.@Nullable CrumblingOverlay crumbling) {
		BlockEntityRenderState.extractBase(controller, state, crumbling);
		BlockState block = controller.getBlockState();
		Level level = controller.getLevel();
		if (!(block.getBlock() instanceof ShowControllerBlock) || level == null) {
			return;
		}
		state.facing = block.getValue(ShowControllerBlock.FACING);
		state.enabled = block.getValue(ShowControllerBlock.ENABLED);
		state.running = block.getValue(ShowControllerBlock.RUNNING);
		state.cast = block.getValue(ShowControllerBlock.CAST);
		double now = level.getGameTime() + (double) partialTick;
		double[] clock = clocks.computeIfAbsent(controller, c -> new double[] {0.0, 0.0, now});
		double elapsed = Math.max(0.0, Math.min(20.0, now - clock[2]));
		double before = clock[1];
		clock[1] = Mth.clamp(clock[1] + (state.running ? 1.0 : -1.0) * elapsed / EASE, 0.0, 1.0);
		clock[0] += elapsed * (before + clock[1]) / 2.0;
		clock[2] = now;
		state.drum = (float) (360.0 * (clock[0] % ShowControllerBlock.DRUM_TICKS) / ShowControllerBlock.DRUM_TICKS);
		state.pressure = (float) (clock[1] * (0.5 - 0.5 * Math.cos(2.0 * Math.PI * clock[0] / ShowControllerBlock.SWEEP_TICKS)));
	}

	/** Turns what follows by {@code degrees} about the x axis through height {@code y} and depth {@code z} (pixels). */
	private static void aboutX(PoseStack pose, float y, float z, float degrees) {
		pose.translate(0.0F, y / 16, z / 16);
		pose.rotateDegrees(Axis.XP, degrees);
		pose.translate(0.0F, -y / 16, -z / 16);
	}

	/** Turns what follows by {@code degrees} about the z axis through across {@code x} and height {@code y} (pixels). */
	private static void aboutZ(PoseStack pose, float x, float y, float degrees) {
		pose.translate(x / 16, y / 16, 0.0F);
		pose.rotateDegrees(Axis.ZP, degrees);
		pose.translate(-x / 16, -y / 16, 0.0F);
	}

	@Override
	public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
		QuadModel drum = DecorQuads.get("show_controller_drum");
		QuadModel needle = DecorQuads.get("show_controller_needle");
		QuadModel lamp = DecorQuads.get("show_controller_lamp");
		QuadModel beacon = DecorQuads.get("show_controller_beacon");
		QuadModel lever = DecorQuads.get("show_controller_lever");
		pose.pushPose();
		pose.translate(0.5F, 0.0F, 0.5F);
		pose.rotateDegrees(Axis.YP, -RockingChairRenderer.yRotation(state.facing));
		pose.translate(-0.5F, 0.0F, -0.5F);
		if (drum != null) {
			pose.pushPose();
			aboutX(pose, DRUM_Y, DRUM_Z, state.drum);
			drum.submit(pose, collector, state.lightCoords);
			pose.popPose();
		}
		if (needle != null) {
			// A turn about z is clockwise seen from the front: from down to the west (low) round to down to the east (high).
			pose.pushPose();
			aboutZ(pose, GAUGE_LEFT, GAUGE_Y, -NEEDLE_SWEEP + 2.0F * NEEDLE_SWEEP * state.cast / ShowControllerBlock.LAMPS);
			needle.submit(pose, collector, state.lightCoords);
			pose.popPose();
			pose.pushPose();
			pose.translate((GAUGE_RIGHT - GAUGE_LEFT) / 16, 0.0F, 0.0F);
			aboutZ(pose, GAUGE_LEFT, GAUGE_Y, -NEEDLE_SWEEP + 2.0F * NEEDLE_SWEEP * state.pressure);
			needle.submit(pose, collector, state.lightCoords);
			pose.popPose();
		}
		if (lamp != null) {
			for (int i = 0; i < ShowControllerBlock.LAMPS; i++) {
				boolean lit = i < state.cast;
				pose.pushPose();
				pose.translate(i * LAMP_STEP / 16, 0.0F, 0.0F);
				lamp.submit(pose, collector, lit ? LightCoordsUtil.FULL_BRIGHT : state.lightCoords, lit ? 0xFFFFFFFF : UNLIT);
				pose.popPose();
			}
		}
		if (beacon != null) {
			beacon.submit(pose, collector, state.running ? LightCoordsUtil.FULL_BRIGHT : state.lightCoords, state.running ? 0xFFFFFFFF : UNLIT);
		}
		if (lever != null) {
			pose.pushPose();
			aboutX(pose, LEVER_Y, LEVER_Z, state.enabled ? LEVER_THROW : -LEVER_THROW); // on: thrown back, its knob toward the back
			lever.submit(pose, collector, state.lightCoords);
			pose.popPose();
		}
		pose.popPose();
	}
}

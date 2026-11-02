package io.github.jimbozoomer.jugcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.jimbozoomer.jugcraft.agriculture.ClockworkRavenBlock;
import io.github.jimbozoomer.jugcraft.agriculture.ClockworkRavenBlockEntity;
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
 * Draws the Clockwork Raven's moving parts (tools/clockwork_data.py, clockwork_quads.json): the drum's flywheel, turning
 * once every {@link ClockworkRavenBlock#FLYWHEEL_TICKS} ticks while it runs; its head, looking slowly from side to side
 * about the upright through its neck; its lower beak, opening for each caw ({@link ClockworkRavenBlock#beak}); its
 * wings, hinged at its shoulders and leaning with its body, beating once with each caw
 * ({@link ClockworkRavenBlock#wings}); and its eyes, at full light while it runs. Stopped, its clockwork runs down over
 * {@link #EASE} ticks: the flywheel and its head stop where they are, and it folds its wings and shuts its beak, and its
 * eyes go dark. The client keeps its own count of its caws from when it saw the raven start, at the server's rate.
 * Coordinates are pixels of the lower block, for a figure facing north (tools/clockwork.py CLOCKWORK_RAVEN).
 */
public class ClockworkRavenRenderer implements BlockEntityRenderer<ClockworkRavenBlockEntity, ClockworkRavenRenderer.State> {
	/** How far its body leans, its breast up, about the x axis through height {@link #BODY_Y} and depth {@link #BODY_Z}. */
	static final float TILT = 22.5F;
	static final float BODY_Y = 24.5F;
	static final float BODY_Z = 8.5F;
	/** The flywheel's axle on the drum's front (across, height). */
	static final float FLYWHEEL_X = 8.0F;
	static final float FLYWHEEL_Y = 2.75F;
	/** The upright through its neck that its head turns about (across, depth), and how far it looks either way. */
	static final float NECK_X = 8.0F;
	static final float NECK_Z = 6.0F;
	static final float LOOK = 45.0F;
	/** Its lower beak's hinge (height, depth), and how far it opens. */
	static final float JAW_Y = 27.0F;
	static final float JAW_Z = 4.75F;
	static final float JAW = 35.0F;
	/** Its shoulders, where its wings are hinged (across, height), and how far a beat raises them. */
	static final float SHOULDER_LEFT = 7.0F;
	static final float SHOULDER_RIGHT = 9.0F;
	static final float SHOULDER_Y = 25.75F;
	static final float FLAP = 100.0F;
	/** Ticks the clockwork takes to come up to speed, or run down. */
	static final float EASE = 10.0F;
	/** The eyes' tint while it is stopped. */
	private static final int UNLIT = 0xFF5A3430;
	/**
	 * Each raven as this client last left it: {its clockwork's ticks (eased, as it comes up to speed and runs down), its
	 * speed (0 to 1), the game time, the ticks it has run since it started (as the server counts them, for its caws)}.
	 */
	private final Map<ClockworkRavenBlockEntity, double[]> clocks = new WeakHashMap<>();

	public static final class State extends BlockEntityRenderState {
		Direction facing = Direction.NORTH;
		boolean lit;
		float wheel;
		float look;
		float jaw;
		float wings;
	}

	public ClockworkRavenRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	@Override
	public void extractRenderState(ClockworkRavenBlockEntity figure, State state, float partialTick, Vec3 camera,
			ModelFeatureRenderer.@Nullable CrumblingOverlay crumbling) {
		BlockEntityRenderState.extractBase(figure, state, crumbling);
		BlockState block = figure.getBlockState();
		Level level = figure.getLevel();
		if (!(block.getBlock() instanceof ClockworkRavenBlock) || level == null) {
			return;
		}
		state.facing = block.getValue(ClockworkRavenBlock.FACING);
		boolean running = block.getValue(ClockworkRavenBlock.RUNNING);
		state.lit = running;
		double now = level.getGameTime() + (double) partialTick;
		double[] clock = clocks.computeIfAbsent(figure, f -> new double[] {0.0, 0.0, now, 0.0});
		double elapsed = Math.max(0.0, Math.min(20.0, now - clock[2]));
		if (running && clock[1] <= 0.0) {
			clock[3] = 0.0; // Started from rest: its caws counted from the start, as the server counts them.
		}
		if (running) {
			clock[3] += elapsed;
		}
		double before = clock[1];
		clock[1] = Mth.clamp(clock[1] + (running ? 1.0 : -1.0) * elapsed / EASE, 0.0, 1.0);
		clock[0] += elapsed * (before + clock[1]) / 2.0;
		clock[2] = now;
		float speed = (float) clock[1];
		state.wheel = (float) (360.0 * (clock[0] % ClockworkRavenBlock.FLYWHEEL_TICKS) / ClockworkRavenBlock.FLYWHEEL_TICKS);
		state.look = (float) (LOOK * Math.sin(2.0 * Math.PI * clock[0] / ClockworkRavenBlock.LOOK_TICKS));
		state.jaw = JAW * ClockworkRavenBlock.beak(clock[3]) * speed;
		state.wings = FLAP * ClockworkRavenBlock.wings(clock[3]) * speed;
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
		QuadModel flywheel = DecorQuads.get("clockwork_raven_flywheel");
		QuadModel head = DecorQuads.get("clockwork_raven_head");
		QuadModel jaw = DecorQuads.get("clockwork_raven_jaw");
		QuadModel eyes = DecorQuads.get("clockwork_raven_eyes");
		QuadModel left = DecorQuads.get("clockwork_raven_left_wing");
		QuadModel right = DecorQuads.get("clockwork_raven_right_wing");
		pose.pushPose();
		pose.translate(0.5F, 0.0F, 0.5F);
		pose.rotateDegrees(Axis.YP, -RockingChairRenderer.yRotation(state.facing));
		pose.translate(-0.5F, 0.0F, -0.5F);
		if (flywheel != null) {
			pose.pushPose();
			aboutZ(pose, FLYWHEEL_X, FLYWHEEL_Y, -state.wheel);
			flywheel.submit(pose, collector, state.lightCoords);
			pose.popPose();
		}
		// The wings lean with its body, and beat about their shoulders: the left out to the west, the right to the east.
		if (left != null) {
			pose.pushPose();
			aboutX(pose, BODY_Y, BODY_Z, TILT);
			aboutZ(pose, SHOULDER_LEFT, SHOULDER_Y, -state.wings);
			left.submit(pose, collector, state.lightCoords);
			pose.popPose();
		}
		if (right != null) {
			pose.pushPose();
			aboutX(pose, BODY_Y, BODY_Z, TILT);
			aboutZ(pose, SHOULDER_RIGHT, SHOULDER_Y, state.wings);
			right.submit(pose, collector, state.lightCoords);
			pose.popPose();
		}
		pose.pushPose();
		pose.translate(NECK_X / 16, 0.0F, NECK_Z / 16);
		pose.rotateDegrees(Axis.YP, state.look);
		pose.translate(-NECK_X / 16, 0.0F, -NECK_Z / 16);
		if (head != null) {
			head.submit(pose, collector, state.lightCoords);
		}
		if (eyes != null) {
			eyes.submit(pose, collector, state.lit ? LightCoordsUtil.FULL_BRIGHT : state.lightCoords, state.lit ? 0xFFFFFFFF : UNLIT);
		}
		aboutX(pose, JAW_Y, JAW_Z, -state.jaw); // the lower beak drops at its tip
		if (jaw != null) {
			jaw.submit(pose, collector, state.lightCoords);
		}
		pose.popPose();
		pose.popPose();
	}

	@Override
	public boolean shouldRenderOffScreen() {
		return true; // It stands two blocks tall, and its wings reach past its block as they beat.
	}
}

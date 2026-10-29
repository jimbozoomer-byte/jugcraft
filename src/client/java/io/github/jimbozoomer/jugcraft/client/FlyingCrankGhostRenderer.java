package io.github.jimbozoomer.jugcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.jimbozoomer.jugcraft.agriculture.FlyingCrankGhostBlock;
import io.github.jimbozoomer.jugcraft.agriculture.FlyingCrankGhostBlockEntity;
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
 * Draws the Flying Crank Ghost's moving parts (tools/clockwork_data.py, clockwork_quads.json): the crank on the
 * gearbox's front, turning once round every {@link FlyingCrankGhostBlock#CRANK_TICKS} ticks while it runs; the ghost
 * hanging from its pin, so riding a circle the pin's size and swaying about the top of its hood as it goes; its arms,
 * each drawn up about its shoulder as the pin swings away from its pulley and let down as it swings back (the two in
 * turn, {@link #pose}); its eyes, at full light while it runs; and its five wires, a wire stretched and turned from each
 * one's end to the other: from its hood up to the pin, from the pin to each pulley, and from each pulley down to its
 * wrist. Stopped, its clockwork runs down over {@link #EASE} ticks and everything comes to rest where it is, and its eyes
 * go dark. Coordinates are pixels of the lower block, for a figure facing north (tools/clockwork.py FLYING_GHOST).
 */
public class FlyingCrankGhostRenderer implements BlockEntityRenderer<FlyingCrankGhostBlockEntity, FlyingCrankGhostRenderer.State> {
	/** The crank's axle (across, height) and how far its pin is from it; the depth of the plane its wires lie in. */
	static final float CRANK_X = 8.0F;
	static final float CRANK_Y = 30.0F;
	static final float PIN = 1.0F;
	static final float WIRE_Z = 6.0F;
	/** The pulleys at the crossbar's ends (across), and their height. */
	static final float PULLEY_LEFT = 1.5F;
	static final float PULLEY_RIGHT = 14.5F;
	static final float PULLEY_Y = 29.0F;
	/** The top of the ghost's hood, where its wire is tied and about which it sways, and how far it sways. */
	static final float HEAD_X = 8.0F;
	static final float HEAD_Y = 26.5F;
	static final float SWAY = 6.0F;
	/** The shoulders its arms turn about (across, height), and how far each wrist, where its wire is tied, hangs below. */
	static final float SHOULDER_LEFT = 5.25F;
	static final float SHOULDER_RIGHT = 10.75F;
	static final float SHOULDER_Y = 21.0F;
	static final float WRIST = 7.0F;
	/** The arms' angle out from hanging straight down at rest, and how much further its wire draws each up. */
	static final float ARMS_REST = 22.5F;
	static final float FLAP = 45.0F;
	/** Ticks the clockwork takes to come up to speed, or run down. */
	static final float EASE = 10.0F;
	/** The eyes' tint while the lamp in its hood is out. */
	private static final int UNLIT = 0xFF505A54;
	/**
	 * Each figure as this client last left it: {its crank's ticks (eased, as it comes up to speed and runs down), its
	 * speed (0 to 1), the game time}.
	 */
	private final Map<FlyingCrankGhostBlockEntity, double[]> clocks = new WeakHashMap<>();

	public static final class State extends BlockEntityRenderState {
		Direction facing = Direction.NORTH;
		boolean lit;
		float crank;
		float dx;
		float dy;
		float sway;
		float leftArm = -ARMS_REST;
		float rightArm = ARMS_REST;
		/** The five wires' ends, {x, y, x, y} (pixels): hood to pin, pin to each pulley, each pulley to its wrist. */
		final float[][] wires = new float[5][4];
	}

	public FlyingCrankGhostRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	@Override
	public void extractRenderState(FlyingCrankGhostBlockEntity figure, State state, float partialTick, Vec3 camera,
			ModelFeatureRenderer.@Nullable CrumblingOverlay crumbling) {
		BlockEntityRenderState.extractBase(figure, state, crumbling);
		BlockState block = figure.getBlockState();
		Level level = figure.getLevel();
		if (!(block.getBlock() instanceof FlyingCrankGhostBlock) || level == null) {
			return;
		}
		state.facing = block.getValue(FlyingCrankGhostBlock.FACING);
		boolean running = block.getValue(FlyingCrankGhostBlock.RUNNING);
		state.lit = running;
		double now = level.getGameTime() + (double) partialTick;
		double[] clock = clocks.computeIfAbsent(figure, f -> new double[] {0.0, 0.0, now});
		double elapsed = Math.max(0.0, Math.min(20.0, now - clock[2]));
		double before = clock[1];
		clock[1] = Mth.clamp(clock[1] + (running ? 1.0 : -1.0) * elapsed / EASE, 0.0, 1.0);
		clock[0] += elapsed * (before + clock[1]) / 2.0;
		clock[2] = now;
		pose(state, (float) (360.0 * (clock[0] % FlyingCrankGhostBlock.CRANK_TICKS) / FlyingCrankGhostBlock.CRANK_TICKS));
	}

	/**
	 * Poses the ghost, its arms and its wires for the crank turned {@code crank} degrees (its pin at the top at 0, going
	 * east): the ghost hangs from the pin, so it is moved as the pin is from the top, and it sways against the pin's swing;
	 * as the pin swings east it draws in the wire over the west pulley, raising the left arm, and lets out the east one,
	 * lowering the right, and the other way as it swings back. An arm's turn is about the z axis from hanging straight
	 * down, the left one's negative (out to the west).
	 */
	static void pose(State state, float crank) {
		double a = Math.toRadians(crank);
		double s = Math.sin(a);
		double c = Math.cos(a);
		state.crank = crank;
		state.dx = (float) (PIN * s);
		state.dy = (float) (PIN * (c - 1.0));
		state.sway = (float) (-SWAY * c);
		state.leftArm = (float) -(ARMS_REST + FLAP * (0.5 + 0.5 * s));
		state.rightArm = (float) (ARMS_REST + FLAP * (0.5 - 0.5 * s));
		float px = (float) (CRANK_X + PIN * s);
		float py = (float) (CRANK_Y + PIN * c);
		set(state.wires[0], HEAD_X + state.dx, HEAD_Y + state.dy, px, py);
		set(state.wires[1], px, py, PULLEY_LEFT, PULLEY_Y);
		set(state.wires[2], px, py, PULLEY_RIGHT, PULLEY_Y);
		float[] left = wrist(state, SHOULDER_LEFT, state.leftArm);
		float[] right = wrist(state, SHOULDER_RIGHT, state.rightArm);
		set(state.wires[3], PULLEY_LEFT, PULLEY_Y, left[0], left[1]);
		set(state.wires[4], PULLEY_RIGHT, PULLEY_Y, right[0], right[1]);
	}

	private static void set(float[] wire, float x0, float y0, float x1, float y1) {
		wire[0] = x0;
		wire[1] = y0;
		wire[2] = x1;
		wire[3] = y1;
	}

	/** Where the wrist of the arm at {@code shoulder}, turned {@code degrees}, is once the ghost is swayed and moved. */
	private static float[] wrist(State state, float shoulder, float degrees) {
		double t = Math.toRadians(degrees);
		double x = shoulder + WRIST * Math.sin(t) - HEAD_X;
		double y = SHOULDER_Y - WRIST * Math.cos(t) - HEAD_Y;
		double w = Math.toRadians(state.sway);
		return new float[] {(float) (HEAD_X + state.dx + x * Math.cos(w) - y * Math.sin(w)),
				(float) (HEAD_Y + state.dy + x * Math.sin(w) + y * Math.cos(w))};
	}

	/** Turns what follows by {@code degrees} about the z axis through across {@code x} and height {@code y} (pixels). */
	private static void about(PoseStack pose, float x, float y, float degrees) {
		pose.translate(x / 16, y / 16, 0.0F);
		pose.rotateDegrees(Axis.ZP, degrees);
		pose.translate(-x / 16, -y / 16, 0.0F);
	}

	@Override
	public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
		QuadModel crank = DecorQuads.get("flying_crank_ghost_crank");
		QuadModel ghost = DecorQuads.get("flying_crank_ghost_ghost");
		QuadModel eyes = DecorQuads.get("flying_crank_ghost_eyes");
		QuadModel left = DecorQuads.get("flying_crank_ghost_left_arm");
		QuadModel right = DecorQuads.get("flying_crank_ghost_right_arm");
		QuadModel wire = DecorQuads.get("flying_crank_ghost_wire");
		pose.pushPose();
		pose.translate(0.5F, 0.0F, 0.5F);
		pose.rotateDegrees(Axis.YP, -RockingChairRenderer.yRotation(state.facing));
		pose.translate(-0.5F, 0.0F, -0.5F);
		if (crank != null) {
			pose.pushPose();
			about(pose, CRANK_X, CRANK_Y, -state.crank); // its pin at the top going east
			crank.submit(pose, collector, state.lightCoords);
			pose.popPose();
		}
		pose.pushPose();
		pose.translate(state.dx / 16, state.dy / 16, 0.0F);
		about(pose, HEAD_X, HEAD_Y, state.sway);
		if (ghost != null) {
			ghost.submit(pose, collector, state.lightCoords);
		}
		if (eyes != null) {
			eyes.submit(pose, collector, state.lit ? LightCoordsUtil.FULL_BRIGHT : state.lightCoords, state.lit ? 0xFFFFFFFF : UNLIT);
		}
		if (left != null) {
			pose.pushPose();
			about(pose, SHOULDER_LEFT, SHOULDER_Y, state.leftArm);
			left.submit(pose, collector, state.lightCoords);
			pose.popPose();
		}
		if (right != null) {
			pose.pushPose();
			about(pose, SHOULDER_RIGHT, SHOULDER_Y, state.rightArm);
			right.submit(pose, collector, state.lightCoords);
			pose.popPose();
		}
		pose.popPose();
		if (wire != null) {
			// The wire is drawn 16 pixels long, straight up from its foot: moved to a wire's one end, turned toward the other
			// and stretched to reach it.
			for (float[] w : state.wires) {
				float dx = w[2] - w[0];
				float dy = w[3] - w[1];
				pose.pushPose();
				pose.translate(w[0] / 16, w[1] / 16, 0.0F);
				pose.rotateDegrees(Axis.ZP, (float) Math.toDegrees(Math.atan2(-dx, dy)));
				pose.scale(1.0F, (float) Math.hypot(dx, dy) / 16, 1.0F);
				wire.submit(pose, collector, state.lightCoords);
				pose.popPose();
			}
		}
		pose.popPose();
	}

	@Override
	public boolean shouldRenderOffScreen() {
		return true; // It stands two blocks tall, and the ghost's arms reach past its block.
	}
}

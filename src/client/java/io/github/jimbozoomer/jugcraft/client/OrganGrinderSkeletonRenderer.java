package io.github.jimbozoomer.jugcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.jimbozoomer.jugcraft.agriculture.OrganGrinderSkeletonBlock;
import io.github.jimbozoomer.jugcraft.agriculture.OrganGrinderSkeletonBlockEntity;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Draws the Organ-Grinder Skeleton's moving parts (tools/clockwork_data.py, clockwork_quads.json): the organ's crank,
 * turning once round every {@link OrganGrinderSkeletonBlock#CRANK_TICKS} ticks about its axle while the figure runs; its
 * right arm, the upper arm swung about the shoulder and the forearm about the elbow so the hand stays on the crank's
 * handle (two bones, the elbow kept back, {@link #arm}); its head, nodding once a bar about its neck; and its jaw, dropping
 * on each note of the melody. Stopped, its clockwork runs down over {@link #EASE} ticks and everything comes to rest
 * where it is. The client keeps its own count of the tune, from when it saw the figure start, at the server's rate (so
 * the jaw keeps time with the notes it hears), and eases only the crank. Coordinates are pixels of
 * the lower block, for a figure facing north (tools/clockwork.py ORGAN_GRINDER).
 */
public class OrganGrinderSkeletonRenderer implements BlockEntityRenderer<OrganGrinderSkeletonBlockEntity, OrganGrinderSkeletonRenderer.State> {
	/** The crank's axle (across, height, depth) and how far its handle is from it. */
	static final float CRANK_X = 10.5F;
	static final float CRANK_Y = 14.0F;
	static final float CRANK_Z = 7.0F;
	static final float HANDLE = 1.5F;
	/** The right shoulder, and the arm's two lengths: shoulder to elbow, elbow to the middle of the hand's grip. */
	static final float SHOULDER_Y = 23.5F;
	static final float SHOULDER_Z = 10.5F;
	static final float UPPER_ARM = 5.25F;
	static final float FOREARM = 7.0F;
	/** The neck the head nods about, and how far; the jaw's hinge, and how far it drops. */
	static final float NECK_Y = 25.0F;
	static final float NECK_Z = 11.0F;
	static final float NOD = 8.0F;
	static final float JAW_Y = 25.0F;
	static final float JAW_Z = 11.5F;
	static final float JAW = 20.0F;
	/** Ticks the clockwork takes to come up to speed, or run down. */
	static final float EASE = 10.0F;
	/**
	 * Each figure as this client last left it: {its crank's ticks (eased, as it comes up to speed and runs down), its speed
	 * (0 to 1), the game time, the ticks it has run since it started (as the server counts them, for its tune)}.
	 */
	private final Map<OrganGrinderSkeletonBlockEntity, double[]> clocks = new WeakHashMap<>();

	public static final class State extends BlockEntityRenderState {
		Direction facing = Direction.NORTH;
		float crank;
		float upperArm;
		float elbowY = SHOULDER_Y - UPPER_ARM;
		float elbowZ = SHOULDER_Z;
		float forearm;
		float nod;
		float jaw;
	}

	public OrganGrinderSkeletonRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	/**
	 * The right arm reaching the crank's handle with the crank turned {@code crank} degrees (its handle at the top at 0,
	 * going back toward the skeleton): {upper arm's turn, elbow height, elbow depth, forearm's turn}. A turn is about the
	 * x axis from hanging straight down, positive taking the arm's foot forward (to the north); the elbow is the one of
	 * the two that lies further back. Out of reach, the arm points straight at the handle.
	 */
	static float[] arm(float crank) {
		double a = Math.toRadians(crank);
		double hy = CRANK_Y + HANDLE * Math.cos(a);
		double hz = CRANK_Z + HANDLE * Math.sin(a);
		double dy = hy - SHOULDER_Y;
		double dz = hz - SHOULDER_Z;
		double reach = Math.min(Math.hypot(dy, dz), UPPER_ARM + FOREARM - 1.0E-4);
		double toward = Math.atan2(dz, dy);
		double bend = Math.acos(Mth.clamp((UPPER_ARM * UPPER_ARM + reach * reach - FOREARM * FOREARM) / (2.0 * UPPER_ARM * reach), -1.0, 1.0));
		double first = toward + bend;
		double second = toward - bend;
		double upper = Math.sin(first) >= Math.sin(second) ? first : second;
		double ey = SHOULDER_Y + UPPER_ARM * Math.cos(upper);
		double ez = SHOULDER_Z + UPPER_ARM * Math.sin(upper);
		double fore = Math.atan2(hz - ez, hy - ey);
		// A part drawn hanging straight down points at pi (measured from up toward the back); turning it by t points it at pi + t.
		return new float[] {(float) Mth.wrapDegrees(Math.toDegrees(upper - Math.PI)), (float) ey, (float) ez,
				(float) Mth.wrapDegrees(Math.toDegrees(fore - Math.PI))};
	}

	@Override
	public void extractRenderState(OrganGrinderSkeletonBlockEntity figure, State state, float partialTick, Vec3 camera,
			ModelFeatureRenderer.@Nullable CrumblingOverlay crumbling) {
		BlockEntityRenderState.extractBase(figure, state, crumbling);
		BlockState block = figure.getBlockState();
		Level level = figure.getLevel();
		if (!(block.getBlock() instanceof OrganGrinderSkeletonBlock) || level == null) {
			return;
		}
		state.facing = block.getValue(OrganGrinderSkeletonBlock.FACING);
		boolean running = block.getValue(OrganGrinderSkeletonBlock.RUNNING);
		double now = level.getGameTime() + (double) partialTick;
		double[] clock = clocks.computeIfAbsent(figure, f -> new double[] {0.0, 0.0, now, 0.0});
		double elapsed = Math.max(0.0, Math.min(20.0, now - clock[2]));
		if (running && clock[1] <= 0.0) {
			clock[3] = 0.0; // Started from rest: the tune from the top, as the organ plays it.
		}
		if (running) {
			clock[3] += elapsed;
		}
		double before = clock[1];
		clock[1] = Mth.clamp(clock[1] + (running ? 1.0 : -1.0) * elapsed / EASE, 0.0, 1.0);
		clock[0] += elapsed * (before + clock[1]) / 2.0;
		clock[2] = now;
		double ticks = clock[3];
		float speed = (float) clock[1];
		state.crank = (float) (360.0 * (clock[0] % OrganGrinderSkeletonBlock.CRANK_TICKS) / OrganGrinderSkeletonBlock.CRANK_TICKS);
		float[] arm = arm(state.crank);
		state.upperArm = arm[0];
		state.elbowY = arm[1];
		state.elbowZ = arm[2];
		state.forearm = arm[3];
		double bar = ticks / (OrganGrinderSkeletonBlock.STEP_TICKS * OrganGrinderSkeletonBlock.STEPS_PER_BAR);
		state.nod = (float) (NOD * speed * (0.5 - 0.5 * Math.cos(2.0 * Math.PI * (bar - Math.floor(bar)))));
		double steps = ticks / OrganGrinderSkeletonBlock.STEP_TICKS;
		int step = (int) Math.floorMod((long) Math.floor(steps), OrganGrinderSkeletonBlock.MELODY.length);
		state.jaw = OrganGrinderSkeletonBlock.MELODY[step] >= 0 ? (float) (JAW * speed * (1.0 - (steps - Math.floor(steps)))) : 0.0F;
	}

	/** Turns what follows by {@code degrees} about the x axis through height {@code y} and depth {@code z} (pixels). */
	private static void about(PoseStack pose, float y, float z, float degrees) {
		pose.translate(0.0F, y / 16, z / 16);
		pose.rotateDegrees(Axis.XP, degrees);
		pose.translate(0.0F, -y / 16, -z / 16);
	}

	@Override
	public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
		QuadModel crank = DecorQuads.get("organ_grinder_skeleton_crank");
		QuadModel upper = DecorQuads.get("organ_grinder_skeleton_upper_arm");
		QuadModel forearm = DecorQuads.get("organ_grinder_skeleton_forearm");
		QuadModel head = DecorQuads.get("organ_grinder_skeleton_head");
		QuadModel jaw = DecorQuads.get("organ_grinder_skeleton_jaw");
		pose.pushPose();
		pose.translate(0.5F, 0.0F, 0.5F);
		pose.rotateDegrees(Axis.YP, -RockingChairRenderer.yRotation(state.facing));
		pose.translate(-0.5F, 0.0F, -0.5F);
		if (crank != null) {
			pose.pushPose();
			about(pose, CRANK_Y, CRANK_Z, state.crank);
			crank.submit(pose, collector, state.lightCoords);
			pose.popPose();
		}
		if (upper != null) {
			pose.pushPose();
			about(pose, SHOULDER_Y, SHOULDER_Z, state.upperArm);
			upper.submit(pose, collector, state.lightCoords);
			pose.popPose();
		}
		if (forearm != null) {
			// Drawn hanging from where the elbow is when the arm hangs straight down; moved to the elbow and turned there.
			pose.pushPose();
			pose.translate(0.0F, state.elbowY / 16, state.elbowZ / 16);
			pose.rotateDegrees(Axis.XP, state.forearm);
			pose.translate(0.0F, -(SHOULDER_Y - UPPER_ARM) / 16, -SHOULDER_Z / 16);
			forearm.submit(pose, collector, state.lightCoords);
			pose.popPose();
		}
		pose.pushPose();
		about(pose, NECK_Y, NECK_Z, -state.nod); // a nod brings the top of the head forward
		if (head != null) {
			head.submit(pose, collector, state.lightCoords);
		}
		about(pose, JAW_Y, JAW_Z, -state.jaw); // the jaw drops at its front
		if (jaw != null) {
			jaw.submit(pose, collector, state.lightCoords);
		}
		pose.popPose();
		pose.popPose();
	}

	@Override
	public boolean shouldRenderOffScreen() {
		return true; // It stands two blocks tall.
	}
}

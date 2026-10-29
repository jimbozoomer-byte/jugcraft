package io.github.jimbozoomer.jugcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.jimbozoomer.jugcraft.agriculture.PumpkinDrummerBlock;
import io.github.jimbozoomer.jugcraft.agriculture.PumpkinDrummerBlockEntity;
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
 * Draws the Pumpkin-Headed Drummer's moving parts (tools/clockwork_data.py, clockwork_quads.json): each forearm, with its
 * glove and mallet, swung back out from its drumhead about the elbow and in again for each of its beats
 * ({@link PumpkinDrummerBlock#stroke}); his head, dipping about his neck in time ({@link PumpkinDrummerBlock#nod}); and
 * his carved face's candlelight, at full light while he runs. Stopped, his clockwork runs down over {@link #EASE} ticks
 * and his mallets and head come to rest, and his face goes dark. The client keeps its own count of the march from when
 * it saw him start, at the server's rate, so the mallets land on the beats it hears. Coordinates are pixels of the lower
 * block, for a figure facing north (tools/clockwork.py PUMPKIN_DRUMMER).
 */
public class PumpkinDrummerRenderer implements BlockEntityRenderer<PumpkinDrummerBlockEntity, PumpkinDrummerRenderer.State> {
	/** The elbows his forearms swing about (across, height), and how far a backswing takes them. */
	static final float ELBOW_LEFT = 4.5F;
	static final float ELBOW_RIGHT = 11.5F;
	static final float ELBOW_Y = 18.5F;
	static final float BACK = 30.0F;
	/** The neck his head dips about (height, depth), and how far. */
	static final float NECK_Y = 24.0F;
	static final float NECK_Z = 10.5F;
	static final float NOD = 6.0F;
	/** Ticks the clockwork takes to come up to speed, or run down. */
	static final float EASE = 10.0F;
	/** The candlelight's tint while he is stopped: the cuts dark. */
	private static final int UNLIT = 0xFF20100A;
	/**
	 * Each drummer as this client last left him: {unused, his speed (0 to 1), the game time, the ticks he has run since he
	 * started (as the server counts them, for his march)}.
	 */
	private final Map<PumpkinDrummerBlockEntity, double[]> clocks = new WeakHashMap<>();

	public static final class State extends BlockEntityRenderState {
		Direction facing = Direction.NORTH;
		boolean lit;
		float leftBack;
		float rightBack;
		float nod;
	}

	public PumpkinDrummerRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	@Override
	public void extractRenderState(PumpkinDrummerBlockEntity figure, State state, float partialTick, Vec3 camera,
			ModelFeatureRenderer.@Nullable CrumblingOverlay crumbling) {
		BlockEntityRenderState.extractBase(figure, state, crumbling);
		BlockState block = figure.getBlockState();
		Level level = figure.getLevel();
		if (!(block.getBlock() instanceof PumpkinDrummerBlock) || level == null) {
			return;
		}
		state.facing = block.getValue(PumpkinDrummerBlock.FACING);
		boolean running = block.getValue(PumpkinDrummerBlock.RUNNING);
		state.lit = running;
		double now = level.getGameTime() + (double) partialTick;
		double[] clock = clocks.computeIfAbsent(figure, f -> new double[] {0.0, 0.0, now, 0.0});
		double elapsed = Math.max(0.0, Math.min(20.0, now - clock[2]));
		if (running && clock[1] <= 0.0) {
			clock[3] = 0.0; // Started from rest: the march from the top, as he beats it.
		}
		if (running) {
			clock[3] += elapsed;
		}
		clock[1] = Mth.clamp(clock[1] + (running ? 1.0 : -1.0) * elapsed / EASE, 0.0, 1.0);
		clock[2] = now;
		float speed = (float) clock[1];
		state.leftBack = BACK * PumpkinDrummerBlock.stroke(clock[3], 'L') * speed;
		state.rightBack = BACK * PumpkinDrummerBlock.stroke(clock[3], 'R') * speed;
		state.nod = NOD * PumpkinDrummerBlock.nod(clock[3]) * speed;
	}

	/** Turns what follows by {@code degrees} about the z axis through across {@code x} and height {@code y} (pixels). */
	private static void aboutZ(PoseStack pose, float x, float y, float degrees) {
		pose.translate(x / 16, y / 16, 0.0F);
		pose.rotateDegrees(Axis.ZP, degrees);
		pose.translate(-x / 16, -y / 16, 0.0F);
	}

	@Override
	public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
		QuadModel left = DecorQuads.get("pumpkin_drummer_left_forearm");
		QuadModel right = DecorQuads.get("pumpkin_drummer_right_forearm");
		QuadModel head = DecorQuads.get("pumpkin_drummer_head");
		QuadModel face = DecorQuads.get("pumpkin_drummer_face");
		pose.pushPose();
		pose.translate(0.5F, 0.0F, 0.5F);
		pose.rotateDegrees(Axis.YP, -RockingChairRenderer.yRotation(state.facing));
		pose.translate(-0.5F, 0.0F, -0.5F);
		// A backswing takes each mallet out from its drumhead: the left's to the west, the right's to the east.
		if (left != null) {
			pose.pushPose();
			aboutZ(pose, ELBOW_LEFT, ELBOW_Y, -state.leftBack);
			left.submit(pose, collector, state.lightCoords);
			pose.popPose();
		}
		if (right != null) {
			pose.pushPose();
			aboutZ(pose, ELBOW_RIGHT, ELBOW_Y, state.rightBack);
			right.submit(pose, collector, state.lightCoords);
			pose.popPose();
		}
		pose.pushPose();
		pose.translate(0.0F, NECK_Y / 16, NECK_Z / 16);
		pose.rotateDegrees(Axis.XP, -state.nod); // a dip brings the top of his head forward
		pose.translate(0.0F, -NECK_Y / 16, -NECK_Z / 16);
		if (head != null) {
			head.submit(pose, collector, state.lightCoords);
		}
		if (face != null) {
			face.submit(pose, collector, state.lit ? LightCoordsUtil.FULL_BRIGHT : state.lightCoords, state.lit ? 0xFFFFFFFF : UNLIT);
		}
		pose.popPose();
		pose.popPose();
	}

	@Override
	public boolean shouldRenderOffScreen() {
		return true; // He stands two blocks tall, and his mallets swing out past his block.
	}
}

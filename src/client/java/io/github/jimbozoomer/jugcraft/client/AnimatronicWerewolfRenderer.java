package io.github.jimbozoomer.jugcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.jimbozoomer.jugcraft.agriculture.AnimatronicWerewolfBlock;
import io.github.jimbozoomer.jugcraft.agriculture.AnimatronicWerewolfBlockEntity;
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
 * Draws the Animatronic Werewolf's moving parts (tools/clockwork_data.py, clockwork_quads.json): its turntable deck and
 * the werewolf on it, turned once round every {@link AnimatronicWerewolfBlock#TURN_TICKS} ticks while it runs; and in
 * each howl ({@link AnimatronicWerewolfBlock#howl}) its arms swung up about its shoulders, its head thrown back about its
 * neck and its jaw dropped about its hinge. Stopped, its turntable runs down over {@link #EASE} ticks and stops where it
 * is, and it lowers its arms. The client keeps its own count of the howls from when it saw the figure start.
 * Coordinates are pixels of the lower block, for a figure facing north (tools/clockwork.py WEREWOLF).
 */
public class AnimatronicWerewolfRenderer implements BlockEntityRenderer<AnimatronicWerewolfBlockEntity, AnimatronicWerewolfRenderer.State> {
	/** The shoulders (height, depth), and the arms' swing from rest to raised (degrees). */
	static final float SHOULDER_Y = 22.13F;
	static final float SHOULDER_Z = 5.09F;
	static final float ARMS_REST = 22.5F;
	static final float ARMS_RAISED = 130.0F;
	/** The neck the head is thrown back about, and how far; the jaw's hinge, and how far it drops. */
	static final float NECK_Y = 24.0F;
	static final float NECK_Z = 6.5F;
	static final float HEAD_BACK = 35.0F;
	static final float JAW_Y = 23.75F;
	static final float JAW_Z = 4.0F;
	static final float JAW = 30.0F;
	/** Ticks the turntable takes to come up to speed, or run down. */
	static final float EASE = 20.0F;
	/**
	 * Each figure as this client last left it: {its turntable's ticks (eased, as it comes up to speed and runs down), its
	 * speed (0 to 1), the game time, the ticks it has run since it started (as the server counts them, for its howls)}.
	 */
	private final Map<AnimatronicWerewolfBlockEntity, double[]> clocks = new WeakHashMap<>();

	public static final class State extends BlockEntityRenderState {
		Direction facing = Direction.NORTH;
		float spin;
		float arms = ARMS_REST;
		float head;
		float jaw;
	}

	public AnimatronicWerewolfRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	@Override
	public void extractRenderState(AnimatronicWerewolfBlockEntity figure, State state, float partialTick, Vec3 camera,
			ModelFeatureRenderer.@Nullable CrumblingOverlay crumbling) {
		BlockEntityRenderState.extractBase(figure, state, crumbling);
		BlockState block = figure.getBlockState();
		Level level = figure.getLevel();
		if (!(block.getBlock() instanceof AnimatronicWerewolfBlock) || level == null) {
			return;
		}
		state.facing = block.getValue(AnimatronicWerewolfBlock.FACING);
		boolean running = block.getValue(AnimatronicWerewolfBlock.RUNNING);
		double now = level.getGameTime() + (double) partialTick;
		double[] clock = clocks.computeIfAbsent(figure, f -> new double[] {0.0, 0.0, now, 0.0});
		double elapsed = Math.max(0.0, Math.min(20.0, now - clock[2]));
		if (running && clock[1] <= 0.0) {
			clock[3] = 0.0; // Started from rest: its howls counted from the start, as the server counts them.
		}
		if (running) {
			clock[3] += elapsed;
		}
		double before = clock[1];
		clock[1] = Mth.clamp(clock[1] + (running ? 1.0 : -1.0) * elapsed / EASE, 0.0, 1.0);
		clock[0] += elapsed * (before + clock[1]) / 2.0;
		clock[2] = now;
		float speed = (float) clock[1];
		state.spin = (float) (360.0 * (clock[0] % AnimatronicWerewolfBlock.TURN_TICKS) / AnimatronicWerewolfBlock.TURN_TICKS);
		float howl = AnimatronicWerewolfBlock.howl(clock[3]) * speed;
		state.arms = ARMS_REST + (ARMS_RAISED - ARMS_REST) * howl;
		state.head = HEAD_BACK * howl;
		state.jaw = JAW * howl;
	}

	/** Turns what follows by {@code degrees} about the x axis through height {@code y} and depth {@code z} (pixels). */
	private static void about(PoseStack pose, float y, float z, float degrees) {
		pose.translate(0.0F, y / 16, z / 16);
		pose.rotateDegrees(Axis.XP, degrees);
		pose.translate(0.0F, -y / 16, -z / 16);
	}

	@Override
	public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
		QuadModel spin = DecorQuads.get("animatronic_werewolf_spin");
		QuadModel left = DecorQuads.get("animatronic_werewolf_left_arm");
		QuadModel right = DecorQuads.get("animatronic_werewolf_right_arm");
		QuadModel head = DecorQuads.get("animatronic_werewolf_head");
		QuadModel jaw = DecorQuads.get("animatronic_werewolf_jaw");
		pose.pushPose();
		pose.translate(0.5F, 0.0F, 0.5F);
		pose.rotateDegrees(Axis.YP, -RockingChairRenderer.yRotation(state.facing) + state.spin);
		pose.translate(-0.5F, 0.0F, -0.5F);
		if (spin != null) {
			spin.submit(pose, collector, state.lightCoords);
		}
		pose.pushPose();
		about(pose, SHOULDER_Y, SHOULDER_Z, state.arms); // swung forward and up from hanging straight down
		if (left != null) {
			left.submit(pose, collector, state.lightCoords);
		}
		if (right != null) {
			right.submit(pose, collector, state.lightCoords);
		}
		pose.popPose();
		pose.pushPose();
		about(pose, NECK_Y, NECK_Z, state.head); // thrown back: the top of the head goes back
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
		return true; // It stands two blocks tall, and its arms reach past its block as it turns.
	}
}

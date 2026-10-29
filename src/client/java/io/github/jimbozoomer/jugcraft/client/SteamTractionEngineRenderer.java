package io.github.jimbozoomer.jugcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.jimbozoomer.jugcraft.agriculture.MultiDecorationBlock;
import io.github.jimbozoomer.jugcraft.agriculture.SteamTractionEngineBlock;
import io.github.jimbozoomer.jugcraft.agriculture.SteamTractionEngineBlockEntity;
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
 * Draws the Steam Traction Engine's motion (tools/harvest_festival_data.py, harvest_festival_quads.json), from its first
 * block, in its frame's pixels (tools/harvest_festival.py ENGINE): its flywheel and crank, turning once every
 * {@link SteamTractionEngineBlock#TURN_TICKS} ticks about the crankshaft while it is fired; its crosshead and piston rod,
 * sliding along the slide bars as the crank drives them; its connecting rod, from the crosshead's pin to the crank's,
 * turned to reach; its governor, spinning on its pillar half as fast again; and its fire, at full light through the fire
 * door while it is fired. Let out, it runs down over {@link #EASE} ticks and stops where it is. The client keeps its
 * own count of the turns.
 */
public class SteamTractionEngineRenderer implements BlockEntityRenderer<SteamTractionEngineBlockEntity, SteamTractionEngineRenderer.State> {
	/** Ticks it takes to come up to speed, or run down. */
	static final float EASE = 40.0F;
	/** How much faster than the flywheel the governor spins. */
	static final float GOVERNOR = 1.5F;
	/** Each engine as this client last left it: {its flywheel's ticks (eased), its speed (0 to 1), the game time}. */
	private final Map<SteamTractionEngineBlockEntity, double[]> clocks = new WeakHashMap<>();

	public static final class State extends BlockEntityRenderState {
		Direction facing = Direction.NORTH;
		float crank;
		float crosshead;
		float rod;
		float governor;
		boolean lit;
	}

	public SteamTractionEngineRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	/**
	 * The motion with the crank turned {@code degrees} from its rest (its pin toward the cylinder): {how far the
	 * crosshead has slid back from its rest, in pixels; the connecting rod's turn about the crosshead's pin, degrees}.
	 */
	static float[] motion(float degrees) {
		double a = Math.toRadians(degrees);
		double pinY = SteamTractionEngineBlock.THROW * Math.sin(a);
		double pinZ = -SteamTractionEngineBlock.THROW * Math.cos(a);
		double little = pinZ - Math.sqrt(SteamTractionEngineBlock.ROD * SteamTractionEngineBlock.ROD - pinY * pinY);
		double rest = -SteamTractionEngineBlock.THROW - SteamTractionEngineBlock.ROD;
		return new float[] {(float) (little - rest), (float) -Math.toDegrees(Math.atan2(pinY, pinZ - little))};
	}

	@Override
	public void extractRenderState(SteamTractionEngineBlockEntity engine, State state, float partialTick, Vec3 camera,
			ModelFeatureRenderer.@Nullable CrumblingOverlay crumbling) {
		BlockEntityRenderState.extractBase(engine, state, crumbling);
		BlockState block = engine.getBlockState();
		Level level = engine.getLevel();
		if (!(block.getBlock() instanceof SteamTractionEngineBlock) || level == null) {
			return;
		}
		state.facing = block.getValue(MultiDecorationBlock.FACING);
		state.lit = block.getValue(MultiDecorationBlock.LIT);
		double now = level.getGameTime() + (double) partialTick;
		double[] clock = clocks.computeIfAbsent(engine, e -> new double[] {0.0, 0.0, now});
		double elapsed = Math.max(0.0, Math.min(20.0, now - clock[2]));
		double before = clock[1];
		clock[1] = Mth.clamp(clock[1] + (state.lit ? 1.0 : -1.0) * elapsed / EASE, 0.0, 1.0);
		clock[0] += elapsed * (before + clock[1]) / 2.0;
		clock[2] = now;
		int turn = SteamTractionEngineBlock.TURN_TICKS;
		state.crank = (float) (360.0 * (clock[0] % turn) / turn);
		state.governor = (float) (360.0 * (clock[0] * GOVERNOR % turn) / turn);
		float[] motion = motion(state.crank);
		state.crosshead = motion[0];
		state.rod = motion[1];
	}

	/** Turns what follows by {@code degrees} about the x axis through height {@code y} and depth {@code z} (pixels). */
	private static void about(PoseStack pose, float y, float z, float degrees) {
		pose.translate(0.0F, y / 16, z / 16);
		pose.rotateDegrees(Axis.XP, degrees);
		pose.translate(0.0F, -y / 16, -z / 16);
	}

	@Override
	public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
		QuadModel flywheel = DecorQuads.get("steam_traction_engine_flywheel");
		QuadModel crank = DecorQuads.get("steam_traction_engine_crank");
		QuadModel rod = DecorQuads.get("steam_traction_engine_rod");
		QuadModel crosshead = DecorQuads.get("steam_traction_engine_crosshead");
		QuadModel governor = DecorQuads.get("steam_traction_engine_governor");
		QuadModel fire = DecorQuads.get("steam_traction_engine_fire");
		float y = SteamTractionEngineBlock.CRANK_Y;
		float z = SteamTractionEngineBlock.CRANK_Z;
		pose.pushPose();
		pose.translate(0.5F, 0.0F, 0.5F);
		pose.rotateDegrees(Axis.YP, -RockingChairRenderer.yRotation(state.facing));
		pose.translate(-0.5F, 0.0F, -0.5F);
		// The frame's origin: the first block is its last 16 pixels across (tools/decor17_data.py frame_cell).
		pose.translate(-2.0F, 0.0F, 0.0F);
		pose.pushPose();
		about(pose, y, z, state.crank);
		if (flywheel != null) {
			flywheel.submit(pose, collector, state.lightCoords);
		}
		if (crank != null) {
			crank.submit(pose, collector, state.lightCoords);
		}
		pose.popPose();
		pose.pushPose();
		pose.translate(0.0F, 0.0F, state.crosshead / 16);
		if (crosshead != null) {
			crosshead.submit(pose, collector, state.lightCoords);
		}
		// The rod drawn level from the crosshead's pin at rest, turned there to reach the crank's pin.
		about(pose, y, z - SteamTractionEngineBlock.THROW - SteamTractionEngineBlock.ROD, state.rod);
		if (rod != null) {
			rod.submit(pose, collector, state.lightCoords);
		}
		pose.popPose();
		if (governor != null) {
			pose.pushPose();
			pose.translate(SteamTractionEngineBlock.GOVERNOR_X / 16, 0.0F, SteamTractionEngineBlock.GOVERNOR_Z / 16);
			pose.rotateDegrees(Axis.YP, state.governor);
			pose.translate(-SteamTractionEngineBlock.GOVERNOR_X / 16, 0.0F, -SteamTractionEngineBlock.GOVERNOR_Z / 16);
			governor.submit(pose, collector, state.lightCoords);
			pose.popPose();
		}
		if (state.lit && fire != null) {
			fire.submit(pose, collector, LightCoordsUtil.FULL_BRIGHT);
		}
		pose.popPose();
	}

	@Override
	public boolean shouldRenderOffScreen() {
		return true; // Its motion is across its 45 blocks, not only its first.
	}
}

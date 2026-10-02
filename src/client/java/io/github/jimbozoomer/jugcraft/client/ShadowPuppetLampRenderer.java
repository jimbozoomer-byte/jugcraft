package io.github.jimbozoomer.jugcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.agriculture.DecorationBlockEntity;
import io.github.jimbozoomer.jugcraft.agriculture.MourningAngelBlock;
import io.github.jimbozoomer.jugcraft.agriculture.ShadowPuppetLampBlock;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Draws the Shadow Puppet Lamp's turning paper shade, three panels round the candle with a bat, a cat and a witch in
 * them (glowing warm while it is lit), and, while it is lit, the shadow each panel throws: along the panel's outward
 * direction to the first solid block within {@link ShadowPuppetLampBlock#RANGE} blocks, a dark shape on that wall,
 * bigger the further it falls, darker at night. Positions here are in blocks from the lamp's block.
 */
public class ShadowPuppetLampRenderer implements BlockEntityRenderer<DecorationBlockEntity, ShadowPuppetLampRenderer.State> {
	private static final String[] DESIGNS = {"bat", "cat", "witch"};
	private static final RenderType[] PAPER = new RenderType[DESIGNS.length];
	private static final RenderType[] SHADOW = new RenderType[DESIGNS.length];
	private static final int FULL_BRIGHT = 0xF000F0;
	/** The shade: how far its panels are from the middle, how wide they are, and their bottom and top (pixels). */
	private static final float RADIUS = 2.6F;
	private static final float HALF_WIDTH = 4.5F;
	private static final float BOTTOM = 2.5F;
	private static final float TOP = 11.0F;
	/** The candle's flame, where the shadows are thrown from (blocks). */
	private static final double FLAME_Y = 0.42;
	private static final double STEP = 0.25;

	static {
		for (int i = 0; i < DESIGNS.length; i++) {
			PAPER[i] = RenderTypes.entityCutout(Jugcraft.id("textures/block/shadow_puppet_lamp_paper_" + DESIGNS[i] + ".png"));
			SHADOW[i] = RenderTypes.entityTranslucent(Jugcraft.id("textures/entity/shadow_puppet_lamp_" + DESIGNS[i] + "_shadow.png"));
		}
	}

	/** A shadow on a wall: which design, its middle, the wall's normal (toward the lamp) and its size, in blocks. */
	private record Shadow(int design, Vec3 middle, Vec3 normal, float size) {
	}

	public static final class State extends BlockEntityRenderState {
		boolean lit;
		boolean night;
		float turn;
		List<Shadow> shadows = new ArrayList<>();
	}

	public ShadowPuppetLampRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	@Override
	public void extractRenderState(DecorationBlockEntity lamp, State state, float partialTick, Vec3 camera,
			ModelFeatureRenderer.@Nullable CrumblingOverlay crumbling) {
		BlockEntityRenderState.extractBase(lamp, state, crumbling);
		BlockState block = lamp.getBlockState();
		Level level = lamp.getLevel();
		state.shadows = new ArrayList<>();
		state.lit = false;
		if (!(block.getBlock() instanceof ShadowPuppetLampBlock) || level == null) {
			return;
		}
		BlockPos pos = lamp.getBlockPos();
		state.lit = block.getValue(ShadowPuppetLampBlock.LIT);
		state.night = MourningAngelBlock.night(level);
		state.turn = ShadowPuppetLampBlock.turn(pos, level.getGameTime() + (double) partialTick);
		if (!state.lit) {
			return;
		}
		Vec3 flame = new Vec3(0.5, FLAME_Y, 0.5);
		for (int i = 0; i < DESIGNS.length; i++) {
			double angle = Math.toRadians(state.turn + i * 120.0);
			Vec3 out = new Vec3(Math.cos(angle), 0.0, Math.sin(angle));
			Shadow shadow = cast(level, pos, flame, out, i);
			if (shadow != null) {
				state.shadows.add(shadow);
			}
		}
	}

	/** Follows {@code out} from the flame to the first solid block, and works out the shadow on its near face. */
	private static @Nullable Shadow cast(Level level, BlockPos lamp, Vec3 flame, Vec3 out, int design) {
		for (double t = 0.6; t <= ShadowPuppetLampBlock.RANGE; t += STEP) {
			Vec3 at = flame.add(out.scale(t));
			BlockPos block = lamp.offset((int) Math.floor(at.x), (int) Math.floor(at.y), (int) Math.floor(at.z));
			if (block.equals(lamp) || !level.getBlockState(block).isCollisionShapeFullBlock(level, block)) {
				continue;
			}
			int bx = block.getX() - lamp.getX();
			int bz = block.getZ() - lamp.getZ();
			double hit;
			Vec3 normal;
			if (Math.abs(out.x) >= Math.abs(out.z)) {
				double plane = out.x > 0 ? bx : bx + 1;
				hit = (plane - flame.x) / out.x;
				normal = new Vec3(-Math.signum(out.x), 0.0, 0.0);
			} else {
				double plane = out.z > 0 ? bz : bz + 1;
				hit = (plane - flame.z) / out.z;
				normal = new Vec3(0.0, 0.0, -Math.signum(out.z));
			}
			if (hit < 0.5) {
				return null;
			}
			Vec3 middle = flame.add(out.scale(hit)).add(0.0, 0.2 + hit * 0.12, 0.0).add(normal.scale(0.02));
			float size = (float) Math.min(2.2, Math.max(0.6, 0.3 + hit * 0.3));
			return new Shadow(design, middle, normal, size);
		}
		return null;
	}

	@Override
	public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
		int paperLight = state.lit ? FULL_BRIGHT : state.lightCoords;
		for (int i = 0; i < DESIGNS.length; i++) {
			double angle = Math.toRadians(state.turn + i * 120.0);
			Vec3 out = new Vec3(Math.cos(angle), 0.0, Math.sin(angle));
			Vec3 middle = new Vec3(0.5, 0.0, 0.5).add(out.scale(RADIUS / 16));
			collector.submitCustomGeometry(pose, PAPER[i], (matrix, buffer) -> {
				panel(buffer, matrix, middle, out, HALF_WIDTH / 16, BOTTOM / 16, TOP / 16, 0xFFFFFFFF, paperLight);
				panel(buffer, matrix, middle, out.reverse(), HALF_WIDTH / 16, BOTTOM / 16, TOP / 16, 0xFFFFFFFF, paperLight);
			});
		}
		int alpha = state.night ? 160 : 80;
		for (Shadow shadow : state.shadows) {
			float half = shadow.size() / 2;
			Vec3 bottom = shadow.middle().subtract(0.0, half, 0.0);
			collector.submitCustomGeometry(pose, SHADOW[shadow.design()], (matrix, buffer) ->
					panel(buffer, matrix, bottom, shadow.normal(), half, 0.0F, shadow.size(), alpha << 24, state.lightCoords));
		}
	}

	/**
	 * A vertical quad facing {@code normal}, {@code halfWidth} either side of {@code middle} and from {@code bottom} to
	 * {@code top} above it, its texture the right way round to someone looking at it.
	 */
	private static void panel(VertexConsumer buffer, PoseStack.Pose matrix, Vec3 middle, Vec3 normal, float halfWidth, float bottom, float top,
			int color, int light) {
		// To someone facing the quad, their right is (-normal) x up.
		float rx = (float) normal.z;
		float rz = (float) -normal.x;
		float x = (float) middle.x;
		float y = (float) middle.y;
		float z = (float) middle.z;
		float[][] corners = {{x + rx * halfWidth, y + bottom, z + rz * halfWidth, 1, 1}, {x + rx * halfWidth, y + top, z + rz * halfWidth, 1, 0},
				{x - rx * halfWidth, y + top, z - rz * halfWidth, 0, 0}, {x - rx * halfWidth, y + bottom, z - rz * halfWidth, 0, 1}};
		DecorDraw.quad(buffer, matrix, corners, (float) normal.x, (float) normal.y, (float) normal.z, color, light);
	}

	@Override
	public boolean shouldRenderOffScreen() {
		return true; // Its shadows fall on walls round the room.
	}
}

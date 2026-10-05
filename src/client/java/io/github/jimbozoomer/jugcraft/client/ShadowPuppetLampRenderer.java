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
 *
 * <p>Each panel is one quad: 26.3's cut-out entity type draws a quad from both sides (seen from inside, the picture is
 * mirrored, as real paper's would be), so a second, back-to-back quad would only fight it. The panels are just wide
 * enough to meet at the corners of their triangle. A shadow lands on the face its ray actually enters (an exact walk of
 * the blocks at the flame's height), and is cut to the run of open wall round where it falls, so it never hangs over a
 * doorway or an outside corner.
 */
public class ShadowPuppetLampRenderer implements BlockEntityRenderer<DecorationBlockEntity, ShadowPuppetLampRenderer.State> {
	private static final String[] DESIGNS = {"bat", "cat", "witch"};
	private static final RenderType[] PAPER = new RenderType[DESIGNS.length];
	private static final RenderType[] SHADOW = new RenderType[DESIGNS.length];
	private static final int FULL_BRIGHT = 0xF000F0;
	/**
	 * The shade: how far its panels are from the middle, how wide they are (half; the panels of a triangle round a
	 * circle of RADIUS meet at RADIUS times the square root of three, and a hair more closes the corners), and their
	 * bottom and top (pixels).
	 */
	private static final float RADIUS = 2.6F;
	private static final float HALF_WIDTH = (float) (RADIUS * Math.sqrt(3.0)) + 0.02F;
	private static final float BOTTOM = 2.5F;
	private static final float TOP = 11.0F;
	/** The candle's flame, where the shadows are thrown from (blocks). */
	private static final double FLAME_Y = 0.42;
	/** How much of a shadow must be left on its wall once it is cut to it, or it is not drawn. */
	private static final float LEAST_SHOWN = 0.35F;

	static {
		for (int i = 0; i < DESIGNS.length; i++) {
			PAPER[i] = RenderTypes.entityCutout(Jugcraft.id("textures/block/shadow_puppet_lamp_paper_" + DESIGNS[i] + ".png"));
			SHADOW[i] = RenderTypes.entityTranslucent(Jugcraft.id("textures/entity/shadow_puppet_lamp_" + DESIGNS[i] + "_shadow.png"));
		}
	}

	/**
	 * A shadow on a wall: which design, its middle, the wall's normal (toward the lamp) and its half size, in blocks; and
	 * the part of it drawn (cut to its wall), from its middle: left and right as someone facing the wall sees them, bottom
	 * and top.
	 */
	private record Shadow(int design, Vec3 middle, Vec3 normal, float half, float left, float right, float bottom, float top) {
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

	/**
	 * Follows {@code out} from the flame to the first solid block at the flame's height, block by block (each block the
	 * ray passes is looked at, and the face it enters by is the face it hits), and works out the shadow on that face.
	 */
	private static @Nullable Shadow cast(Level level, BlockPos lamp, Vec3 flame, Vec3 out, int design) {
		int stepX = out.x > 0 ? 1 : -1;
		int stepZ = out.z > 0 ? 1 : -1;
		double deltaX = Math.abs(out.x) < 1.0E-9 ? Double.MAX_VALUE : Math.abs(1.0 / out.x);
		double deltaZ = Math.abs(out.z) < 1.0E-9 ? Double.MAX_VALUE : Math.abs(1.0 / out.z);
		double nextX = (stepX > 0 ? 1.0 - flame.x : flame.x) * deltaX;
		double nextZ = (stepZ > 0 ? 1.0 - flame.z : flame.z) * deltaZ;
		int bx = 0;
		int bz = 0;
		int by = (int) Math.floor(flame.y);
		while (true) {
			boolean acrossX = nextX < nextZ;
			double hit;
			if (acrossX) {
				hit = nextX;
				bx += stepX;
				nextX += deltaX;
			} else {
				hit = nextZ;
				bz += stepZ;
				nextZ += deltaZ;
			}
			if (hit > ShadowPuppetLampBlock.RANGE) {
				return null;
			}
			BlockPos block = lamp.offset(bx, by, bz);
			if (!full(level, block)) {
				continue;
			}
			if (hit < 0.5) {
				return null;
			}
			Vec3 normal = acrossX ? new Vec3(-stepX, 0.0, 0.0) : new Vec3(0.0, 0.0, -stepZ);
			Vec3 middle = flame.add(out.scale(hit)).add(0.0, 0.2 + hit * 0.12, 0.0).add(normal.scale(0.02));
			float half = (float) Math.min(2.2, Math.max(0.6, 0.3 + hit * 0.3)) / 2;
			return onWall(level, lamp, new BlockPos(bx, by, bz), design, middle, normal, half);
		}
	}

	/**
	 * The shadow centred on {@code middle} on the face of {@code wall} (relative to the lamp) facing {@code normal}, cut
	 * to the open wall round it: from the struck block, along the wall to either side and then up, as far as the blocks
	 * are full and the blocks in front of them are not; or nothing when too little of it would be left.
	 */
	private static @Nullable Shadow onWall(Level level, BlockPos lamp, BlockPos wall, int design, Vec3 middle, Vec3 normal, float half) {
		// Along the wall: "right" as someone facing it sees it, in whole blocks (normal x up, the other way round).
		int rx = (int) normal.z;
		int rz = (int) -normal.x;
		// Where the struck block starts along the wall, from the shadow's middle (blocks): its left-hand edge.
		double across = rx != 0 ? (middle.x - wall.getX()) * rx : (middle.z - wall.getZ()) * rz;
		double start = (rx + rz > 0 ? 0.0 : -1.0) - across;
		double bottom = middle.y - half;
		double top = middle.y + half;
		int least = (int) Math.floor(-half - start);
		int most = (int) Math.floor(half - start - 1.0E-6);
		int lo = 0;
		int hi = 0;
		while (hi < most && open(level, lamp, wall, rx, rz, hi + 1, 0, normal)) {
			hi++;
		}
		while (lo > least && open(level, lamp, wall, rx, rz, lo - 1, 0, normal)) {
			lo--;
		}
		int up = 0;
		int highest = (int) Math.floor(top - wall.getY() - 1.0E-6);
		while (up < highest) {
			boolean row = true;
			for (int i = lo; i <= hi && row; i++) {
				row = open(level, lamp, wall, rx, rz, i, up + 1, normal);
			}
			if (!row) {
				break;
			}
			up++;
		}
		float left = (float) Math.max(-half, start + lo);
		float right = (float) Math.min(half, start + hi + 1);
		float low = (float) (Math.max(bottom, wall.getY()) - middle.y);
		float high = (float) (Math.min(top, wall.getY() + up + 1.0) - middle.y);
		if (right <= left || high <= low || (right - left) * (high - low) < LEAST_SHOWN * 4 * half * half) {
			return null;
		}
		return new Shadow(design, middle, normal, half, left, right, low, high);
	}

	/**
	 * Whether the block {@code along} blocks along the wall and {@code up} up from {@code wall} is open wall: full, with
	 * nothing full in front of it.
	 */
	private static boolean open(Level level, BlockPos lamp, BlockPos wall, int rx, int rz, int along, int up, Vec3 normal) {
		BlockPos block = lamp.offset(wall.getX() + rx * along, wall.getY() + up, wall.getZ() + rz * along);
		return full(level, block) && !full(level, block.offset((int) normal.x, 0, (int) normal.z));
	}

	private static boolean full(Level level, BlockPos block) {
		return level.getBlockState(block).isCollisionShapeFullBlock(level, block);
	}

	@Override
	public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
		int paperLight = state.lit ? FULL_BRIGHT : state.lightCoords;
		for (int i = 0; i < DESIGNS.length; i++) {
			double angle = Math.toRadians(state.turn + i * 120.0);
			Vec3 out = new Vec3(Math.cos(angle), 0.0, Math.sin(angle));
			Vec3 middle = new Vec3(0.5, 0.0, 0.5).add(out.scale(RADIUS / 16));
			collector.submitCustomGeometry(pose, PAPER[i], (matrix, buffer) ->
					panel(buffer, matrix, middle, out, HALF_WIDTH / 16, BOTTOM / 16, TOP / 16, 0xFFFFFFFF, paperLight));
		}
		int alpha = state.night ? 160 : 80;
		for (Shadow shadow : state.shadows) {
			collector.submitCustomGeometry(pose, SHADOW[shadow.design()],
					(matrix, buffer) -> card(buffer, matrix, shadow, alpha << 24, state.lightCoords));
		}
	}

	/** A shadow's quad, the part of it left on its wall, its texture cut to match. */
	private static void card(VertexConsumer buffer, PoseStack.Pose matrix, Shadow shadow, int color, int light) {
		Vec3 normal = shadow.normal();
		float rx = (float) normal.z;
		float rz = (float) -normal.x;
		float x = (float) shadow.middle().x;
		float y = (float) shadow.middle().y;
		float z = (float) shadow.middle().z;
		float size = shadow.half() * 2;
		float u0 = (shadow.left() + shadow.half()) / size;
		float u1 = (shadow.right() + shadow.half()) / size;
		float v0 = (shadow.half() - shadow.top()) / size;
		float v1 = (shadow.half() - shadow.bottom()) / size;
		float[][] corners = {{x + rx * shadow.right(), y + shadow.bottom(), z + rz * shadow.right(), u1, v1},
				{x + rx * shadow.right(), y + shadow.top(), z + rz * shadow.right(), u1, v0},
				{x + rx * shadow.left(), y + shadow.top(), z + rz * shadow.left(), u0, v0},
				{x + rx * shadow.left(), y + shadow.bottom(), z + rz * shadow.left(), u0, v1}};
		DecorDraw.quad(buffer, matrix, corners, (float) normal.x, (float) normal.y, (float) normal.z, color, light);
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

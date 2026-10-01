package io.github.jimbozoomer.jugcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.jimbozoomer.jugcraft.agriculture.CarvedPumpkinBlock;
import io.github.jimbozoomer.jugcraft.agriculture.JugcraftAgriculture;
import io.github.jimbozoomer.jugcraft.agriculture.PumpkinCarving;
import io.github.jimbozoomer.jugcraft.agriculture.ScarecrowBlock;
import io.github.jimbozoomer.jugcraft.agriculture.ScarecrowBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Draws the head a Scarecrow wears on its shoulders, the way an armor stand wears a pumpkin: a pumpkin ten pixels a
 * side (bigger than a head) sitting on the post one pixel below the shirt's collar, its face turned the way the
 * scarecrow faces. A hand-carved head is the plain pumpkin it was carved from with its own carving drawn over it, as
 * {@link CarvedPumpkinRenderer} draws the block, glowing at full brightness when lit.
 */
public class ScarecrowRenderer implements BlockEntityRenderer<ScarecrowBlockEntity, ScarecrowRenderer.State> {
	/** The head's size: an armor stand's pumpkin (a block drawn at 0.625 of a block). */
	private static final float HEAD = 10.0F / 16.0F;
	/** Where the bottom of the head sits in the upper half: a pixel below the top of the shirt (11 pixels up). */
	private static final float BOTTOM = 10.0F / 16.0F;
	/** A block item drawn FIXED is half a block. */
	private static final float FIXED = 0.5F;
	/** How far outside the pumpkin the carving sits, in pumpkin widths. */
	private static final float OUT = 0.004F;

	private final ItemModelResolver itemModels;

	public static final class State extends BlockEntityRenderState {
		final ItemStackRenderState pumpkin = new ItemStackRenderState();
		Direction facing = Direction.NORTH;
		PumpkinCarving carving = PumpkinCarving.BLANK;
		@Nullable RenderType carvingType;
		int carvingLight;
	}

	public ScarecrowRenderer(BlockEntityRendererProvider.Context context) {
		this.itemModels = context.itemModelResolver();
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	@Override
	public void extractRenderState(ScarecrowBlockEntity scarecrow, State state, float partialTick, Vec3 camera,
			ModelFeatureRenderer.@Nullable CrumblingOverlay crumbling) {
		BlockEntityRenderState.extractBase(scarecrow, state, crumbling);
		state.facing = scarecrow.getBlockState().getValue(ScarecrowBlock.FACING);
		state.carvingType = null;
		ItemStack head = scarecrow.head();
		ItemStack pumpkin = head;
		if (head.getItem() instanceof BlockItem item && item.getBlock() instanceof CarvedPumpkinBlock) {
			// The block is modelled as the plain pumpkin with the carving drawn over it: do the same.
			Block plain = JugcraftAgriculture.plainPumpkin(item.getBlock());
			pumpkin = plain == null ? ItemStack.EMPTY : new ItemStack(plain);
			state.carving = head.getOrDefault(JugcraftAgriculture.CARVING, PumpkinCarving.BLANK);
			boolean lit = ScarecrowBlockEntity.lit(head);
			if (!state.carving.isBlank()) {
				state.carvingType = CarvingTextures.get(state.carving, lit);
				state.carvingLight = lit ? LightCoordsUtil.FULL_BRIGHT : state.lightCoords;
			}
		}
		itemModels.updateForTopItem(state.pumpkin, pumpkin, ItemDisplayContext.FIXED, scarecrow.getLevel(), null,
				(int) scarecrow.getBlockPos().asLong());
	}

	@Override
	public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
		pose.pushPose();
		pose.translate(0.5F, BOTTOM + HEAD / 2.0F, 0.5F);
		if (!state.pumpkin.isEmpty()) {
			pose.pushPose();
			pose.rotateDegrees(Axis.YP, -yRotation(state.facing));
			pose.scale(HEAD / FIXED, HEAD / FIXED, HEAD / FIXED);
			state.pumpkin.submit(pose, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
			pose.popPose();
		}
		RenderType type = state.carvingType;
		if (type != null) {
			// The carving's sides are named in the world, so it is drawn unturned over a cube the head's size.
			PumpkinCarving carving = state.carving;
			Direction facing = state.facing;
			int light = state.carvingLight;
			float scale = HEAD * (1.0F + 2.0F * OUT);
			pose.scale(scale, scale, scale);
			pose.translate(-0.5F, -0.5F, -0.5F);
			collector.submitCustomGeometry(pose, type, (matrix, buffer) -> {
				for (int face = 0; face < PumpkinCarving.FACES; face++) {
					if (!carving.isBlank(face)) {
						CarvedPumpkinRenderer.side(buffer, matrix, PumpkinCarving.side(facing, face), face, light);
					}
				}
			});
		}
		pose.popPose();
	}

	/** Degrees a model drawn facing north turns to face {@code facing}. */
	private static float yRotation(Direction facing) {
		return switch (facing) {
			case EAST -> 90;
			case SOUTH -> 180;
			case WEST -> 270;
			default -> 0;
		};
	}
}

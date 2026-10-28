package io.github.jimbozoomer.jugcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.jimbozoomer.jugcraft.agriculture.CandyChuteBlock;
import io.github.jimbozoomer.jugcraft.agriculture.CandyChuteBlockEntity;
import io.github.jimbozoomer.jugcraft.agriculture.MultiDecorationBlock;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * The treats sliding down a Trick-or-Treat Candy Chute (Front-Yard Haunt Scenes, part 5): each drawn small, on the
 * chute's floor, from its funnel to its mouth by {@link CandyChuteBlock#slide}, quickening, tumbling over about the
 * upright as it goes (so a flat item is never only seen edge-on), the chute turned to its facing. It draws what its
 * block entity was last sent.
 */
public class CandyChuteRenderer implements BlockEntityRenderer<CandyChuteBlockEntity, CandyChuteRenderer.State> {
	/** How far above the chute's floor a treat rides (pixels), and how big it is drawn. */
	private static final float RIDE = 1.0F;
	private static final float SCALE = 0.4F;
	/** How far a treat turns about the upright sliding the whole way (degrees), and how far each starts from the last. */
	private static final float TUMBLE = 540.0F;
	private static final float APART = 45.0F;
	private final ItemModelResolver itemModels;

	public static final class State extends BlockEntityRenderState {
		final ItemStackRenderState[] treats = new ItemStackRenderState[CandyChuteBlock.HOLD];
		final float[] along = new float[CandyChuteBlock.HOLD];
		final float[] turn = new float[CandyChuteBlock.HOLD];
		int count;
		Direction facing = Direction.NORTH;

		State() {
			for (int i = 0; i < treats.length; i++) {
				treats[i] = new ItemStackRenderState();
			}
		}
	}

	public CandyChuteRenderer(BlockEntityRendererProvider.Context context) {
		this.itemModels = context.itemModelResolver();
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	@Override
	public void extractRenderState(CandyChuteBlockEntity chute, State state, float partialTick, Vec3 camera,
			ModelFeatureRenderer.@Nullable CrumblingOverlay crumbling) {
		BlockEntityRenderState.extractBase(chute, state, crumbling);
		state.count = 0;
		Level level = chute.getLevel();
		if (level == null || !(chute.getBlockState().getBlock() instanceof CandyChuteBlock)) {
			return;
		}
		state.facing = chute.getBlockState().getValue(MultiDecorationBlock.FACING);
		float now = level.getGameTime() + partialTick;
		for (CandyChuteBlockEntity.Sliding treat : chute.sliding()) {
			if (state.count >= state.treats.length) {
				break;
			}
			itemModels.updateForTopItem(state.treats[state.count], treat.treat(), ItemDisplayContext.GROUND, level, null,
					(int) chute.getBlockPos().asLong() + state.count);
			state.along[state.count] = CandyChuteBlock.slide(now - treat.start());
			state.turn[state.count] = 90.0F + TUMBLE * state.along[state.count] + APART * state.count;
			state.count++;
		}
	}

	@Override
	public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
		if (state.count == 0) {
			return;
		}
		pose.pushPose();
		pose.translate(0.5F, 0.0F, 0.5F);
		pose.rotateDegrees(Axis.YP, -RockingChairRenderer.yRotation(state.facing));
		pose.translate(-0.5F, 0.0F, -0.5F);
		for (int i = 0; i < state.count; i++) {
			float u = state.along[i];
			float y = CandyChuteBlock.FUNNEL_Y + (CandyChuteBlock.MOUTH_Y - CandyChuteBlock.FUNNEL_Y) * u + RIDE;
			float z = CandyChuteBlock.FUNNEL_Z + (CandyChuteBlock.MOUTH_Z - CandyChuteBlock.FUNNEL_Z) * u;
			pose.pushPose();
			pose.translate(0.5F, y / 16.0F, z / 16.0F);
			pose.rotateDegrees(Axis.YP, state.turn[i]);
			pose.scale(SCALE, SCALE, SCALE);
			state.treats[i].submit(pose, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
			pose.popPose();
		}
		pose.popPose();
	}

	@Override
	public boolean shouldRenderOffScreen() {
		return true; // Its funnel is two blocks behind its first block.
	}
}

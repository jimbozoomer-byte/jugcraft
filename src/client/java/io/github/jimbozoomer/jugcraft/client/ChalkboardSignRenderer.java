package io.github.jimbozoomer.jugcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.jimbozoomer.jugcraft.agriculture.ChalkboardSignBlock;
import io.github.jimbozoomer.jugcraft.agriculture.ChalkboardSignBlockEntity;
import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Chalks a Chalkboard Sign's words on the slates of both its boards: its own words if it has any, else the market's
 * words it shows. Long words are wrapped and the text is made as large as fits the slate, up to {@value #MAX_SCALE}
 * blocks a font pixel; lines past the last that fits are left off. Each board leans {@value #LEAN} degrees from upright
 * about the hinge at the top (tools/pumpkin_patch.py CHALKBOARD), so the words lean with it.
 */
public class ChalkboardSignRenderer implements BlockEntityRenderer<ChalkboardSignBlockEntity, ChalkboardSignRenderer.State> {
	private static final int CHALK = 0xFFE8E6DC;
	/** The boards' lean and hinge (pixels: its height and depth in the model, which faces north), as the model has them. */
	private static final float LEAN = 22.5F;
	private static final float HINGE_Y = 15.0F;
	private static final float HINGE_Z = 8.0F;
	/** Each slate's middle: how far below the hinge along its board, and how far out of the hinge's line its face lies. */
	private static final float SLATE_DROP = 7.0F;
	private static final float SLATE_OUT = 0.6F;
	/** The room for words on each slate, in pixels. */
	private static final float WIDTH = 9.0F;
	private static final float HEIGHT = 9.0F;
	private static final float OUT = 0.005F;
	private static final float MAX_SCALE = 1.0F / 32;
	/** The smallest the letters get: the width to wrap at, in font pixels, is the slate's width at this size. */
	private static final float MIN_SCALE = 1.0F / 100;
	private static final int MAX_LINES = 4;

	private final Font font;

	public static final class State extends BlockEntityRenderState {
		List<FormattedCharSequence> lines = List.of();
		Direction facing = Direction.NORTH;
		float scale = MAX_SCALE;
	}

	public ChalkboardSignRenderer(BlockEntityRendererProvider.Context context) {
		font = context.font();
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	@Override
	public void extractRenderState(ChalkboardSignBlockEntity sign, State state, float partialTick, Vec3 camera,
			ModelFeatureRenderer.@Nullable CrumblingOverlay crumbling) {
		BlockEntityRenderState.extractBase(sign, state, crumbling);
		state.lines = List.of();
		BlockState block = sign.getBlockState();
		if (!(block.getBlock() instanceof ChalkboardSignBlock)) {
			return;
		}
		state.facing = block.getValue(ChalkboardSignBlock.FACING);
		FormattedText text = sign.text().isEmpty() ? Component.translatable(block.getValue(ChalkboardSignBlock.WORDS).key()) : FormattedText.of(sign.text());
		List<FormattedCharSequence> lines = font.split(text, Math.round(WIDTH / 16.0F / MIN_SCALE));
		if (lines.size() > MAX_LINES) {
			lines = List.copyOf(lines.subList(0, MAX_LINES));
		}
		int widest = 1;
		for (FormattedCharSequence line : lines) {
			widest = Math.max(widest, font.width(line));
		}
		float byWidth = WIDTH / 16.0F / widest;
		float byHeight = HEIGHT / 16.0F / Math.max(1, lines.size() * font.lineHeight);
		state.scale = Math.min(MAX_SCALE, Math.min(byWidth, byHeight));
		state.lines = lines;
	}

	@Override
	public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
		if (state.lines.isEmpty()) {
			return;
		}
		// The model's frame (it faces north), as its block state turns it.
		pose.pushPose();
		pose.translate(0.5F, 0.0F, 0.5F);
		pose.rotateDegrees(Axis.YP, -RockingChairRenderer.yRotation(state.facing));
		pose.translate(-0.5F, 0.0F, -0.5F);
		// The front board leans out to the north, the back one to the south; the words face out from each.
		for (int side = -1; side <= 1; side += 2) {
			pose.pushPose();
			pose.translate(0.5F, HINGE_Y / 16.0F, HINGE_Z / 16.0F);
			pose.rotateDegrees(Axis.XP, -LEAN * side);
			if (side < 0) {
				pose.rotateDegrees(Axis.YP, 180.0F);
			}
			pose.translate(0.0F, -SLATE_DROP / 16.0F, SLATE_OUT / 16.0F + OUT);
			pose.scale(state.scale, -state.scale, state.scale);
			float top = -state.lines.size() * font.lineHeight / 2.0F + 1.0F;
			for (int i = 0; i < state.lines.size(); i++) {
				FormattedCharSequence line = state.lines.get(i);
				collector.submitText(pose, -font.width(line) / 2.0F, top + i * font.lineHeight, line, false, Font.DisplayMode.POLYGON_OFFSET,
						state.lightCoords, CHALK, 0, 0);
			}
			pose.popPose();
		}
		pose.popPose();
	}
}

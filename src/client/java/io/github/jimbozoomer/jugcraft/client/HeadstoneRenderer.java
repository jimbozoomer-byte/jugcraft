package io.github.jimbozoomer.jugcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.jimbozoomer.jugcraft.agriculture.HeadstoneBlock;
import io.github.jimbozoomer.jugcraft.agriculture.HeadstoneBlockEntity;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Draws a headstone's epitaph cut into its stone, where its {@link HeadstoneBlock.Style} says: on its front, or on the
 * top of a table tomb or ledger stone, read from its foot. Each line is centred and drawn as large as fits the space,
 * up to the style's largest letters; if the lines together are too tall, all shrink alike. The letters take the
 * stone's ink and fade into it as it weathers ({@code tools/graveyard.py}: ink_fade), so a neglected stone grows hard
 * to read until it is scrubbed.
 */
public class HeadstoneRenderer implements BlockEntityRenderer<HeadstoneBlockEntity, HeadstoneRenderer.State> {
	/** How much each stage of weathering fades the letters towards the stone. */
	public static final float[] INK_FADE = {0.0F, 0.18F, 0.36F, 0.52F};
	/** How far in front of the face the letters sit, so they never sink into the stone. */
	private static final float OUT = 0.005F;
	/** The gap between lines, as a share of a line. */
	private static final float LEADING = 0.25F;

	private final Font font;

	public static final class State extends BlockEntityRenderState {
		List<FormattedCharSequence> lines = List.of();
		List<Float> scales = List.of();
		Direction facing = Direction.NORTH;
		HeadstoneBlock.@Nullable Style style;
		int ink;
	}

	public HeadstoneRenderer(BlockEntityRendererProvider.Context context) {
		font = context.font();
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	/** {@code ink} faded towards {@code stone} by {@code amount} (0 to 1), channel by channel. */
	public static int fade(int ink, int stone, float amount) {
		int out = 0xFF000000;
		for (int shift = 0; shift <= 16; shift += 8) {
			int a = (ink >> shift) & 0xFF;
			int b = (stone >> shift) & 0xFF;
			out |= Math.round(a + (b - a) * amount) << shift;
		}
		return out;
	}

	@Override
	public void extractRenderState(HeadstoneBlockEntity stone, State state, float partialTick, Vec3 camera,
			ModelFeatureRenderer.@Nullable CrumblingOverlay crumbling) {
		BlockEntityRenderState.extractBase(stone, state, crumbling);
		state.style = null;
		BlockState block = stone.getBlockState();
		if (stone.epitaph().isBlank() || !(block.getBlock() instanceof HeadstoneBlock headstone)) {
			return;
		}
		HeadstoneBlock.Style style = headstone.style();
		HeadstoneBlock.Text text = style.text;
		state.style = style;
		state.facing = block.getValue(HeadstoneBlock.FACING);
		state.ink = fade(style.stone.ink, style.stone.fadeTo, INK_FADE[HeadstoneBlock.stage(block)]);
		List<FormattedCharSequence> lines = new ArrayList<>();
		List<Float> scales = new ArrayList<>();
		float width = text.width() / 16.0F;
		float total = 0.0F;
		for (String line : stone.epitaph().lines()) {
			List<FormattedCharSequence> split = line.isEmpty() ? List.of() : font.split(FormattedText.of(line), Integer.MAX_VALUE);
			FormattedCharSequence sequence = split.isEmpty() ? FormattedCharSequence.EMPTY : split.get(0);
			int wide = Math.max(1, font.width(sequence));
			float scale = Math.min(text.maxScale(), width / wide);
			lines.add(sequence);
			scales.add(scale);
			total += font.lineHeight * scale * (1.0F + LEADING);
		}
		float room = text.height() / 16.0F;
		if (total > room) {
			float shrink = room / total;
			scales.replaceAll(scale -> scale * shrink);
		}
		state.lines = lines;
		state.scales = scales;
	}

	@Override
	public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
		HeadstoneBlock.Style style = state.style;
		if (style == null || state.lines.isEmpty()) {
			return;
		}
		HeadstoneBlock.Text text = style.text;
		pose.pushPose();
		pose.translate(0.5F, 0.0F, 0.5F);
		// Turn so that +z points out of a north-facing headstone's front (model -z), +x to its left (model -x).
		pose.rotateDegrees(Axis.YP, -state.facing.toYRot());
		float total = 0.0F;
		for (float scale : state.scales) {
			total += font.lineHeight * scale * (1.0F + LEADING);
		}
		if (text.top()) {
			pose.translate(-(text.x() - 8.0F) / 16.0F, text.y() / 16.0F + OUT, -(text.z() - 8.0F) / 16.0F);
			// Lie flat, letters up and the lines reading away from someone standing at the foot.
			pose.rotateDegrees(Axis.XP, -90.0F);
		} else {
			pose.translate(-(text.x() - 8.0F) / 16.0F, text.y() / 16.0F, -(text.z() - 8.0F) / 16.0F + OUT);
		}
		float y = total / 2.0F;
		for (int i = 0; i < state.lines.size(); i++) {
			float scale = state.scales.get(i);
			FormattedCharSequence line = state.lines.get(i);
			float step = font.lineHeight * scale * (1.0F + LEADING);
			pose.pushPose();
			pose.translate(0.0F, y - font.lineHeight * scale * LEADING / 2.0F, 0.0F);
			pose.scale(scale, -scale, scale);
			collector.submitText(pose, -font.width(line) / 2.0F, 0.0F, line, false, Font.DisplayMode.POLYGON_OFFSET, state.lightCoords,
					state.ink, 0, 0);
			pose.popPose();
			y -= step;
		}
		pose.popPose();
	}
}

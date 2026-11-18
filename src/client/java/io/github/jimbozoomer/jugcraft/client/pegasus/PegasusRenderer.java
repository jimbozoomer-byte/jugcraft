package io.github.jimbozoomer.jugcraft.client.pegasus;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.client.horses.BreedHorseModel;
import io.github.jimbozoomer.jugcraft.client.horses.HorseBreedsClient;
import io.github.jimbozoomer.jugcraft.pegasus.Pegasus;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.AbstractHorseRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.phys.AABB;

/**
 * Draws the Pegasus (docs/features/pegasus.md) as vanilla draws a horse (AbstractHorseRenderer: its shadow, baby
 * handling and render state), with the winged model, its original texture, the saddle layer while saddled and its light
 * pack while fitted and saddled (docs/features/horse-breeds.md#saddlebags-and-cargo-conservation). Every
 * observer animates the wings from the entity's synced flight state, smoothed per client tick
 * ({@link Pegasus#wingOpen(float)} and its siblings, interpolated by the partial tick).
 */
public final class PegasusRenderer extends AbstractHorseRenderer<Pegasus, PegasusRenderState, PegasusModel> {
	public static final Identifier TEXTURE = Jugcraft.id("textures/entity/pegasus/pegasus.png");
	public static final Identifier SADDLE_TEXTURE = Jugcraft.id("textures/entity/pegasus/pegasus_saddle.png");
	public static final Identifier PACK_TEXTURE = Jugcraft.id("textures/entity/pegasus/pegasus_saddlebags.png");
	/** Spread wings reach two blocks out each side: keep them drawn while the body is just out of view. */
	private static final double WING_REACH = 2.25;

	public PegasusRenderer(EntityRendererProvider.Context context) {
		super(context, new PegasusModel(context.bakeLayer(PegasusModel.LAYER)), new PegasusModel(context.bakeLayer(PegasusModel.BABY_LAYER)));
		addLayer(new SaddleLayer(this, new PegasusSaddleModel(context.bakeLayer(PegasusSaddleModel.LAYER))));
		addLayer(new PackLayer(this, HorseBreedsClient.pegasusPack(context)));
	}

	@Override
	public PegasusRenderState createRenderState() {
		return new PegasusRenderState();
	}

	@Override
	public Identifier getTextureLocation(PegasusRenderState state) {
		return TEXTURE;
	}

	@Override
	public void extractRenderState(Pegasus pegasus, PegasusRenderState state, float partialTick) {
		super.extractRenderState(pegasus, state, partialTick);
		state.wingOpen = pegasus.wingOpen(partialTick);
		state.wingAmp = pegasus.wingAmp(partialTick);
		state.wingPhase = pegasus.wingPhase(partialTick);
		state.saddled = !pegasus.getItemBySlot(EquipmentSlot.SADDLE).isEmpty();
		state.flying = pegasus.flightActive();
		state.pack = pegasus.packFitted();
	}

	@Override
	protected AABB getBoundingBoxForCulling(Pegasus pegasus, float partialTick) {
		return super.getBoundingBoxForCulling(pegasus, partialTick).inflate(WING_REACH);
	}

	/** The light pack, strapped behind the saddle: drawn while fitted and saddled (it rides on the saddle). */
	private static final class PackLayer extends RenderLayer<PegasusRenderState, PegasusModel> {
		private final BreedHorseModel<PegasusRenderState> pack;

		PackLayer(PegasusRenderer parent, BreedHorseModel<PegasusRenderState> pack) {
			super(parent);
			this.pack = pack;
		}

		@Override
		public void submit(PoseStack pose, SubmitNodeCollector collector, int light, PegasusRenderState state, float yaw, float pitch) {
			if (!state.pack || !state.saddled || state.isInvisible || state.isBaby) {
				return;
			}
			collector.order(1).submitModel(pack, state, pose, RenderTypes.entityCutout(PACK_TEXTURE), light, OverlayTexture.NO_OVERLAY,
					state.outlineColor);
		}
	}

	/** The saddle and bridle, on their own model and texture, while the saddle slot is filled. */
	private static final class SaddleLayer extends RenderLayer<PegasusRenderState, PegasusModel> {
		private final PegasusSaddleModel saddle;

		SaddleLayer(PegasusRenderer parent, PegasusSaddleModel saddle) {
			super(parent);
			this.saddle = saddle;
		}

		@Override
		public void submit(PoseStack pose, SubmitNodeCollector collector, int light, PegasusRenderState state, float yaw, float pitch) {
			if (!state.saddled || state.isInvisible || state.isBaby) {
				return;
			}
			collector.order(1).submitModel(saddle, state, pose, RenderTypes.entityCutout(SADDLE_TEXTURE), light, OverlayTexture.NO_OVERLAY,
					state.outlineColor);
		}
	}
}

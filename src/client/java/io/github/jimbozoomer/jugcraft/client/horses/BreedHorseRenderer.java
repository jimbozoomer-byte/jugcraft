package io.github.jimbozoomer.jugcraft.client.horses;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.horses.BreedHorse;
import io.github.jimbozoomer.jugcraft.horses.HorseBreed;
import java.util.function.Predicate;
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
 * Draws a breed horse (docs/features/horse-breeds.md) as vanilla draws a horse (AbstractHorseRenderer: its shadow, baby
 * handling and render state) with the breed's own model and texture, its saddle and bridle while saddled, and its
 * saddlebags while fitted (Shire and Giant Finnhorse). One renderer class for all five breeds.
 */
public final class BreedHorseRenderer extends AbstractHorseRenderer<BreedHorse, BreedHorseRenderState, BreedHorseModel<BreedHorseRenderState>> {
	private final Identifier texture;
	/** How far the head and tail reach past the hitbox (blocks), so the model is not culled while it is still in view. */
	private final double reach;

	public BreedHorseRenderer(EntityRendererProvider.Context context, HorseBreed breed) {
		super(context, HorseBreedsClient.body(context, breed, HorseBreedsClient.MAIN), HorseBreedsClient.body(context, breed, HorseBreedsClient.BABY));
		texture = Jugcraft.id("textures/entity/horse_breeds/" + breed.id() + ".png");
		reach = breed == HorseBreed.GIANT_FINNHORSE ? 1.5 : breed == HorseBreed.SHIRE ? 0.75 : 0.5;
		addLayer(new TackLayer(this, HorseBreedsClient.saddle(context, breed),
				Jugcraft.id("textures/entity/horse_breeds/" + breed.id() + "_saddle.png"), state -> state.saddled));
		if (breed.carriesBags) {
			addLayer(new TackLayer(this, HorseBreedsClient.body(context, breed, HorseBreedsClient.SADDLEBAGS),
					Jugcraft.id("textures/entity/horse_breeds/" + breed.id() + "_saddlebags.png"), state -> state.bags));
		}
	}

	@Override
	public BreedHorseRenderState createRenderState() {
		return new BreedHorseRenderState();
	}

	@Override
	public Identifier getTextureLocation(BreedHorseRenderState state) {
		return texture;
	}

	@Override
	public void extractRenderState(BreedHorse horse, BreedHorseRenderState state, float partialTick) {
		super.extractRenderState(horse, state, partialTick);
		state.saddled = !horse.getItemBySlot(EquipmentSlot.SADDLE).isEmpty();
		state.bags = horse.bagsFitted();
	}

	@Override
	protected AABB getBoundingBoxForCulling(BreedHorse horse, float partialTick) {
		return super.getBoundingBoxForCulling(horse, partialTick).inflate(reach);
	}

	/** A tack layer: its own model and texture, drawn while {@code shown} (never on a foal or an invisible horse). */
	private static final class TackLayer extends RenderLayer<BreedHorseRenderState, BreedHorseModel<BreedHorseRenderState>> {
		private final BreedHorseModel<BreedHorseRenderState> model;
		private final Identifier texture;
		private final Predicate<BreedHorseRenderState> shown;

		TackLayer(BreedHorseRenderer parent, BreedHorseModel<BreedHorseRenderState> model, Identifier texture,
				Predicate<BreedHorseRenderState> shown) {
			super(parent);
			this.model = model;
			this.texture = texture;
			this.shown = shown;
		}

		@Override
		public void submit(PoseStack pose, SubmitNodeCollector collector, int light, BreedHorseRenderState state, float yaw, float pitch) {
			if (!shown.test(state) || state.isInvisible || state.isBaby) {
				return;
			}
			collector.order(1).submitModel(model, state, pose, RenderTypes.entityCutout(texture), light, OverlayTexture.NO_OVERLAY,
					state.outlineColor);
		}
	}
}

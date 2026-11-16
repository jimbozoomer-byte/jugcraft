package io.github.jimbozoomer.jugcraft.client.horses;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.horses.HorseBreed;
import io.github.jimbozoomer.jugcraft.horses.HorseBreeds;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshTransformer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EquineRenderState;

/**
 * The horse breeds' client side (docs/features/horse-breeds.md): each breed's model layers (adult, foal, saddle and,
 * for the Shire and the Giant Finnhorse, saddlebags), the Pegasus's light pack layer, and the breeds' renderers. Every
 * adult layer is scaled once with MeshTransformer.scaling({@value #ADULT_SCALE}), as vanilla's ModelLayers.HORSE is
 * (Codex's 26.3 evidence); the foal is the same mesh at {@value #BABY_SCALE}, as the Pegasus's is.
 */
public final class HorseBreedsClient {
	public static final float ADULT_SCALE = 1.1F;
	public static final float BABY_SCALE = 0.55F;
	public static final String MAIN = "main";
	public static final String BABY = "baby";
	public static final String SADDLE = "saddle";
	public static final String SADDLEBAGS = "saddlebags";
	/** The Pegasus's light pack (drawn by PegasusRenderer). */
	public static final ModelLayerLocation PEGASUS_PACK = new ModelLayerLocation(Jugcraft.id("pegasus"), SADDLEBAGS);

	private HorseBreedsClient() {
	}

	public static ModelLayerLocation layer(HorseBreed breed, String layer) {
		return new ModelLayerLocation(Jugcraft.id(breed.id()), layer);
	}

	public static void register() {
		for (HorseBreed breed : HorseBreed.GROUND) {
			String id = breed.id();
			ModelLayerRegistry.registerModelLayer(layer(breed, MAIN), () -> definition(id, MAIN, ADULT_SCALE));
			ModelLayerRegistry.registerModelLayer(layer(breed, BABY), () -> definition(id, MAIN, BABY_SCALE));
			ModelLayerRegistry.registerModelLayer(layer(breed, SADDLE), () -> definition(id, SADDLE, ADULT_SCALE));
			if (breed.carriesBags) {
				ModelLayerRegistry.registerModelLayer(layer(breed, SADDLEBAGS), () -> definition(id, SADDLEBAGS, ADULT_SCALE));
			}
			EntityRendererRegistry.register(HorseBreeds.type(breed), context -> new BreedHorseRenderer(context, breed));
		}
		ModelLayerRegistry.registerModelLayer(PEGASUS_PACK, () -> definition("pegasus", SADDLEBAGS, ADULT_SCALE));
	}

	private static LayerDefinition definition(String breed, String layer, float scale) {
		BreedHorseMeshes.Source source = BreedHorseMeshes.source(breed, layer);
		return LayerDefinition.create(source.mesh(), source.textureWidth(), source.textureHeight()).apply(MeshTransformer.scaling(scale));
	}

	/** A breed's body (or saddlebag) model from a layer, with its rig. */
	static <S extends EquineRenderState> BreedHorseModel<S> body(EntityRendererProvider.Context context, HorseBreed breed, String layer) {
		return new BreedHorseModel<>(context.bakeLayer(layer(breed, layer)), BreedHorseMeshes.rig(breed.id()));
	}

	static <S extends EquineRenderState> BreedHorseModel<S> saddle(EntityRendererProvider.Context context, HorseBreed breed) {
		return new BreedSaddleModel<>(context.bakeLayer(layer(breed, SADDLE)), BreedHorseMeshes.rig(breed.id()));
	}

	/** The Pegasus's light pack on vanilla's horse rig (the Pegasus keeps vanilla's bones and animation). */
	public static <S extends EquineRenderState> BreedHorseModel<S> pegasusPack(EntityRendererProvider.Context context) {
		return new BreedHorseModel<>(context.bakeLayer(PEGASUS_PACK), BreedHorseMeshes.rig("pegasus"));
	}
}

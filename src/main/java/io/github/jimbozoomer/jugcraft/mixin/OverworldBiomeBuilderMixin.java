package io.github.jimbozoomer.jugcraft.mixin;

import com.mojang.datafixers.util.Pair;
import io.github.jimbozoomer.jugcraft.world.AlpineSpawn;
import io.github.jimbozoomer.jugcraft.world.PixelHollows;
import java.util.function.Consumer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.OverworldBiomeBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Jugcraft's Overworld biomes in the climate table. Fabric API can add features to existing biomes but has no way to
 * place a new Overworld biome, so this is the one hook needed. Vanilla's meadows come out as Alpine Spawn (the
 * builder's output is wrapped before vanilla adds anything), and so do the cool plateau's forest and taiga (picked
 * where the builder chooses plateau biomes, so lowland forest and taiga are untouched). The Pixel Hollows is appended
 * after vanilla's own.
 */
@Mixin(OverworldBiomeBuilder.class)
public abstract class OverworldBiomeBuilderMixin {
	@ModifyVariable(method = "addBiomes", at = @At("HEAD"), argsOnly = true)
	private Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> jugcraft$alpineSpawn(
			Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> biomes) {
		return AlpineSpawn.wrap(biomes);
	}

	@Inject(method = "pickPlateauBiome", at = @At("HEAD"), cancellable = true)
	private void jugcraft$alpinePlateau(int temperature, int humidity, Climate.Parameter weirdness,
			CallbackInfoReturnable<ResourceKey<Biome>> cir) {
		if (AlpineSpawn.takesPlateau(temperature, humidity)) {
			cir.setReturnValue(AlpineSpawn.BIOME);
		}
	}

	@Inject(method = "addBiomes", at = @At("TAIL"))
	private void jugcraft$addPixelHollows(Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> biomes, CallbackInfo ci) {
		PixelHollows.addToOverworld(biomes);
	}
}

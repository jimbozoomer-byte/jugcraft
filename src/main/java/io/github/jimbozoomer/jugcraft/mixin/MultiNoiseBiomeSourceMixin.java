package io.github.jimbozoomer.jugcraft.mixin;

import io.github.jimbozoomer.jugcraft.biome.JugcraftRegions;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Jugcraft regions: in a Jugcraft region of an Overworld with the Jugcraft biomes, biome lookups answer from the
 * Jugcraft layout ({@link JugcraftRegions}); everywhere else vanilla answers. Every biome lookup of world generation,
 * structures, spawning and /locate comes through here.
 */
@Mixin(MultiNoiseBiomeSource.class)
public abstract class MultiNoiseBiomeSourceMixin {
	@Unique
	private JugcraftRegions.Source jugcraft$regions = new JugcraftRegions.Source();

	@Inject(method = "getNoiseBiome(IIILnet/minecraft/world/level/biome/Climate$Sampler;)Lnet/minecraft/core/Holder;",
			at = @At("HEAD"), cancellable = true)
	private void jugcraft$regions(int x, int y, int z, Climate.Sampler sampler, CallbackInfoReturnable<Holder<Biome>> cir) {
		Holder<Biome> biome = jugcraft$regions.biome((BiomeSource) (Object) this, x, y, z, sampler);
		if (biome != null) {
			cir.setReturnValue(biome);
		}
	}
}

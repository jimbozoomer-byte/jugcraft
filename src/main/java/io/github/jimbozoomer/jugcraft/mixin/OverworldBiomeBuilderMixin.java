package io.github.jimbozoomer.jugcraft.mixin;

import com.mojang.datafixers.util.Pair;
import io.github.jimbozoomer.jugcraft.world.PixelHollows;
import java.util.function.Consumer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.OverworldBiomeBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Adds the Pixel Hollows to the Overworld's climate table. Fabric API can add features to existing biomes but has no
 * way to place a new Overworld biome, so this is the one hook needed; it appends one entry after vanilla's own.
 */
@Mixin(OverworldBiomeBuilder.class)
public abstract class OverworldBiomeBuilderMixin {
	@Inject(method = "addBiomes", at = @At("TAIL"))
	private void jugcraft$addPixelHollows(Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> biomes, CallbackInfo ci) {
		PixelHollows.addToOverworld(biomes);
	}
}

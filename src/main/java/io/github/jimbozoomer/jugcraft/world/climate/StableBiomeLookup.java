package io.github.jimbozoomer.jugcraft.world.climate;

import io.github.jimbozoomer.jugcraft.mixin.ClimateParameterListAccess;
import io.github.jimbozoomer.jugcraft.mixin.ClimateRTreeAccess;
import net.minecraft.world.level.biome.Climate;

/**
 * Opt-in climate worlds need the same base biome during chunk generation, /locate and later sessions. Vanilla's
 * nearest-neighbour tree starts from a per-thread previous winner and retains it on equal fitness. Overlapping
 * climate entries (including Pixel Hollows versus surface biomes) can therefore depend on traversal order.
 * Starting each lookup at the tree root gives its existing tree order a consistent tie-break. The immutable tree
 * and its fast spatial search are retained; no brute-force scan or shared world/profile state is introduced.
 */
public final class StableBiomeLookup {
    private StableBiomeLookup() {}

    public interface Source {
        void jugcraft$stabilizeClimateLookup();
    }

    public static <T> T find(Climate.ParameterList<T> parameters, Climate.TargetPoint target) {
        var tree = ((ClimateParameterListAccess) (Object) parameters).jugcraft$climateIndex();
        ((ClimateRTreeAccess) (Object) tree).jugcraft$lastClimateResult().remove();
        return parameters.findValue(target);
    }
}

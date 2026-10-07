package io.github.jimbozoomer.jugcraft.agriculture;

import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStackTemplate;

/** Server recipe snapshot for the pot's selector. Recipe IDs and counted ingredients are also the future supply contract. */
public record CookingPotPlan(Identifier id, ItemStackTemplate output, List<CookingPotRecipe.Part> ingredients, int time) {
    public static final int LIMIT = 512;
    public static final StreamCodec<RegistryFriendlyByteBuf, CookingPotPlan> CODEC = StreamCodec.composite(
        Identifier.STREAM_CODEC, CookingPotPlan::id,
        ItemStackTemplate.STREAM_CODEC, CookingPotPlan::output,
        CookingPotRecipe.Part.STREAM_CODEC.apply(ByteBufCodecs.list(64)), CookingPotPlan::ingredients,
        ByteBufCodecs.VAR_INT, CookingPotPlan::time, CookingPotPlan::new);
    public record Opening(List<CookingPotPlan> recipes) {
        public static final StreamCodec<RegistryFriendlyByteBuf, Opening> CODEC =
            CookingPotPlan.CODEC.apply(ByteBufCodecs.list(LIMIT)).map(Opening::new, Opening::recipes);
    }
}

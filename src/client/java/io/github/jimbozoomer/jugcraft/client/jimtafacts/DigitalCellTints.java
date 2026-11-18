package io.github.jimbozoomer.jugcraft.client.jimtafacts;

import com.mojang.serialization.MapCodec;
import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.jimtafacts.network.DigitalCellLeds;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.color.item.ItemTintSources;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * Native replacements for the supplied item tint types ae2:storage_cell_state and ae2:portable_cell_color, used by the
 * runtime cell item definitions (tools/jimtafacts_network.py copies the supplied tint lists). The colours and rules are
 * in {@link DigitalCellLeds}; both are opaque, as the supplied sources return ARGB.opaque.
 */
public final class DigitalCellTints {
    private DigitalCellTints() {}

    public static void register() {
        ItemTintSources.ID_MAPPER.put(Jugcraft.id("jimta_storage_cell_state"), CellState.CODEC);
        ItemTintSources.ID_MAPPER.put(Jugcraft.id("jimta_portable_cell_color"), PortableColor.CODEC);
    }

    /** The status LED: green empty, blue holding items, orange no room for a new type, red full, black uncharged. */
    public record CellState() implements ItemTintSource {
        public static final MapCodec<CellState> CODEC = MapCodec.unit(new CellState());
        @Override public int calculate(ItemStack stack, ClientLevel level, LivingEntity entity) {
            return 0xFF000000 | DigitalCellLeds.ledColor(stack);
        }
        @Override public MapCodec<CellState> type() { return CODEC; }
    }

    /** A portable cell's screen: its dyed colour or the supplied light blue; white (untinted) on anything else. */
    public record PortableColor() implements ItemTintSource {
        public static final MapCodec<PortableColor> CODEC = MapCodec.unit(new PortableColor());
        @Override public int calculate(ItemStack stack, ClientLevel level, LivingEntity entity) {
            return DigitalCellLeds.portable(stack) ? 0xFF000000 | DigitalCellLeds.portableColor(stack) : 0xFFFFFFFF;
        }
        @Override public MapCodec<PortableColor> type() { return CODEC; }
    }
}

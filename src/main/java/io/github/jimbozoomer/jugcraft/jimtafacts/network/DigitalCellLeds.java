package io.github.jimbozoomer.jugcraft.jimtafacts.network;

import io.github.jimbozoomer.jugcraft.tools.Chargeable;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;

/**
 * Storage cell status LED and portable cell screen colours, read from a cell's saved data.
 *
 * <p>Values come from the supplied originals in art/owner-library/originals/Oct23/Jimtafacts/applied energentics reborn
 * by jimbo: appeng/api/storage/cells/CellState (ABSENT 0x000000, EMPTY 0x00FF00, NOT_EMPTY 0x00AAFF, TYPES_FULL
 * 0xFFAA00, FULL 0xFF0000), StorageCellStateTintSource (an uncharged powered cell is ABSENT), BasicCellInventory.getStatus
 * (empty, then "can take a new type", then "room for more of the stored types", then full) and AEItems, which gives both
 * portable item and portable fluid cells the default colour 0x80CAFF that AbstractPortableCell.getColor reads through
 * the dyed-colour component. No supplied class is loaded; this is Jugcraft's own code.
 *
 * <p>Jugcraft cells count items (or fluid) up to a capacity and hold at most {@link CellContents#MAX_TYPES} types with
 * no per-type space charge, so "can take a new type" means fewer than 63 types and room left. The drive and ME chest
 * LEDs (DigitalNode) use the same rule.
 */
public final class DigitalCellLeds {
    public static final int ABSENT = 0, EMPTY = 1, NOT_EMPTY = 2, TYPES_FULL = 3, FULL = 4;
    /** Supplied AbstractPortableCell default for portable item and fluid cells (AEItems). */
    public static final int PORTABLE_DEFAULT_COLOR = 0x80CAFF;
    private static final int[] COLORS = {0x000000, 0x00FF00, 0x00AAFF, 0xFFAA00, 0xFF0000};

    private DigitalCellLeds() {}

    /** The supplied CellState colour (RGB, no alpha) of a state. */
    public static int color(int state) {
        return COLORS[Math.clamp(state, ABSENT, FULL)];
    }

    /** The LED state of a cell item: ABSENT while a portable cell is uncharged, otherwise from its saved contents. */
    public static int state(ItemStack stack) {
        if (stack.getItem() instanceof Chargeable && Chargeable.energy(stack) <= 0) return ABSENT;
        if (DigitalCreativeCell.creative(stack)) {
            boolean configured = !stack.getOrDefault(DigitalStorage.CONTENTS, CellContents.EMPTY).entries().isEmpty()
                    || DigitalCreativeCell.fluid(stack);
            return configured ? NOT_EMPTY : EMPTY;
        }
        long used, capacity;
        int types;
        if (stack.getItem() instanceof DigitalStorage.CellItem cell) {
            var contents = stack.getOrDefault(DigitalStorage.CONTENTS, CellContents.EMPTY);
            used = contents.amount(); types = contents.entries().size(); capacity = cell.capacity;
        } else if (stack.getItem() instanceof FluidCells.Cell cell) {
            var contents = stack.getOrDefault(FluidCells.CONTENTS, FluidCells.Contents.EMPTY);
            used = contents.used(); types = contents.entries().size(); capacity = cell.capacity;
        } else {
            return EMPTY; // The supplied tint treats anything without a cell inventory as an empty cell.
        }
        if (used <= 0) return EMPTY;
        if (used >= capacity) return FULL;
        return types >= CellContents.MAX_TYPES ? TYPES_FULL : NOT_EMPTY;
    }

    /** The LED colour (RGB) of a cell item. */
    public static int ledColor(ItemStack stack) {
        return color(state(stack));
    }

    public static boolean portable(ItemStack stack) {
        return stack.getItem() instanceof PortableCells.ItemsCell || stack.getItem() instanceof PortableCells.Fluid;
    }

    /** A portable cell's screen colour (RGB): its dyed colour, else the supplied default. */
    public static int portableColor(ItemStack stack) {
        var dyed = stack.get(DataComponents.DYED_COLOR);
        return dyed == null ? PORTABLE_DEFAULT_COLOR : dyed.rgb() & 0xFFFFFF;
    }
}

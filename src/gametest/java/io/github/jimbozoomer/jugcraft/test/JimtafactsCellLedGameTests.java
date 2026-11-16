package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.jimtafacts.network.*;
import io.github.jimbozoomer.jugcraft.tools.Chargeable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.TagValueInput;

/** Storage cell LED and portable cell screen colours: the supplied AE2 CellState and AEItems values, read from saved data. */
public final class JimtafactsCellLedGameTests {
    private static void expect(GameTestHelper h, ItemStack cell, int state, int rgb, String what) {
        int actual = DigitalCellLeds.state(cell);
        h.assertTrue(actual == state, what + ": state " + state + ", was " + actual);
        h.assertTrue(DigitalCellLeds.ledColor(cell) == rgb, what + ": LED #" + Integer.toHexString(rgb) + ", was #" + Integer.toHexString(DigitalCellLeds.ledColor(cell)));
    }

    private static ItemStack items(String id, List<CellContents.Entry> entries) {
        var cell = new ItemStack(DigitalStorage.ITEMS.get(id));
        if (!entries.isEmpty()) cell.set(DigitalStorage.CONTENTS, new CellContents(entries));
        return cell;
    }

    private static List<CellContents.Entry> types(int count, int each) {
        var entries = new ArrayList<CellContents.Entry>();
        for (int i = 0; i < count; i++) {
            var sample = new ItemStack(Items.PAPER); sample.set(DataComponents.CUSTOM_NAME, Component.literal("type " + i));
            entries.add(new CellContents.Entry(sample, each));
        }
        return entries;
    }

    @GameTest public void itemCellLedFollowsSavedContents(GameTestHelper h) {
        int capacity = ((DigitalStorage.CellItem) DigitalStorage.ITEMS.get("item_storage_cell_1k")).capacity;
        expect(h, items("item_storage_cell_1k", List.of()), DigitalCellLeds.EMPTY, 0x00FF00, "Empty cell");
        expect(h, items("item_storage_cell_1k", List.of(new CellContents.Entry(new ItemStack(Items.DIAMOND), 10))), DigitalCellLeds.NOT_EMPTY, 0x00AAFF, "Cell with items");
        // Jugcraft charges no space per type, so one item short of capacity still takes a new type: blue, as in the drive.
        expect(h, items("item_storage_cell_1k", List.of(new CellContents.Entry(new ItemStack(Items.DIAMOND), capacity - 1))), DigitalCellLeds.NOT_EMPTY, 0x00AAFF, "Nearly full by count");
        // Nearly full in the supplied sense: no room for another type, room for more of the stored ones.
        expect(h, items("item_storage_cell_1k", types(CellContents.MAX_TYPES, 1)), DigitalCellLeds.TYPES_FULL, 0xFFAA00, "All types used");
        expect(h, items("item_storage_cell_1k", List.of(new CellContents.Entry(new ItemStack(Items.DIAMOND), capacity))), DigitalCellLeds.FULL, 0xFF0000, "Full cell");
        expect(h, items("item_storage_cell_256k", List.of(new CellContents.Entry(new ItemStack(Items.DIAMOND), 64))), DigitalCellLeds.NOT_EMPTY, 0x00AAFF, "256k cell with items");
        h.succeed();
    }

    @GameTest public void fluidCellLedFollowsSavedContents(GameTestHelper h) {
        var empty = new ItemStack(DigitalStorage.ITEMS.get("fluid_storage_cell_1k"));
        long capacity = ((FluidCells.Cell) empty.getItem()).capacity;
        expect(h, empty, DigitalCellLeds.EMPTY, 0x00FF00, "Empty fluid cell");
        var some = empty.copy(); some.set(FluidCells.CONTENTS, new FluidCells.Contents(List.of(new FluidCells.Entry(FluidVariant.of(Fluids.WATER), 81_000))));
        expect(h, some, DigitalCellLeds.NOT_EMPTY, 0x00AAFF, "Fluid cell with water");
        var full = empty.copy(); full.set(FluidCells.CONTENTS, new FluidCells.Contents(List.of(new FluidCells.Entry(FluidVariant.of(Fluids.WATER), capacity))));
        expect(h, full, DigitalCellLeds.FULL, 0xFF0000, "Full fluid cell");
        h.succeed();
    }

    @GameTest public void portableCellsShowChargeStateAndScreenColour(GameTestHelper h) {
        for (String id : new String[]{"portable_item_cell_1k", "portable_fluid_cell_64k"}) {
            var cell = new ItemStack(DigitalStorage.ITEMS.get(id));
            h.assertTrue(DigitalCellLeds.portable(cell), id + " is a portable cell");
            expect(h, cell, DigitalCellLeds.ABSENT, 0x000000, id + " uncharged (supplied ABSENT state)");
            Chargeable.setEnergy(cell, 1000);
            expect(h, cell, DigitalCellLeds.EMPTY, 0x00FF00, id + " charged and empty");
            h.assertTrue(DigitalCellLeds.portableColor(cell) == 0x80CAFF, id + " screen uses the supplied default colour");
            cell.set(DataComponents.DYED_COLOR, new DyedItemColor(0x3366CC));
            h.assertTrue(DigitalCellLeds.portableColor(cell) == 0x3366CC, id + " screen uses its dyed colour");
        }
        var charged = new ItemStack(DigitalStorage.ITEMS.get("portable_item_cell_4k")); Chargeable.setEnergy(charged, 1000);
        charged.set(DigitalStorage.CONTENTS, new CellContents(List.of(new CellContents.Entry(new ItemStack(Items.COBBLESTONE), 5))));
        expect(h, charged, DigitalCellLeds.NOT_EMPTY, 0x00AAFF, "Charged portable cell with items");
        h.assertTrue(!DigitalCellLeds.portable(new ItemStack(DigitalStorage.ITEMS.get("item_storage_cell_1k"))), "A plain cell is not portable");
        h.succeed();
    }

    @GameTest public void itemLedAgreesWithTheDriveLed(GameTestHelper h) {
        var pos = new BlockPos(2, 2, 2); h.setBlock(pos, DigitalStorage.BLOCKS.get("drive"));
        var node = h.getBlockEntity(pos, DigitalNode.class); node.owner(UUID.randomUUID());
        int capacity = ((DigitalStorage.CellItem) DigitalStorage.ITEMS.get("item_storage_cell_4k")).capacity;
        var cells = List.of(items("item_storage_cell_4k", List.of()),
                items("item_storage_cell_4k", List.of(new CellContents.Entry(new ItemStack(Items.DIAMOND), 3))),
                items("item_storage_cell_4k", types(CellContents.MAX_TYPES, 2)),
                items("item_storage_cell_4k", List.of(new CellContents.Entry(new ItemStack(Items.DIAMOND), capacity))));
        for (int slot = 0; slot < cells.size(); slot++) node.setItem(slot, cells.get(slot).copy());
        var client = new DigitalNode(h.absolutePos(pos), node.getBlockState());
        client.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, h.getLevel().registryAccess(), node.getUpdateTag(h.getLevel().registryAccess())));
        for (int slot = 0; slot < cells.size(); slot++)
            h.assertTrue(client.visibleCellStatus(slot) == DigitalCellLeds.state(cells.get(slot)), "Drive bay " + slot + " and the held cell show the same state");
        h.succeed();
    }
}

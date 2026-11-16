package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.chemistry.PetroFluids;
import io.github.jimbozoomer.jugcraft.energy.EnergyStorage;
import io.github.jimbozoomer.jugcraft.energy.SimpleEnergyStorage;
import io.github.jimbozoomer.jugcraft.machine.*;
import io.github.jimbozoomer.jugcraft.workshop.WorkshopCatalog;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;

/** Exercises the loaded recipes and real machines, including capacity checks for larger batches. */
public class ComponentYieldGameTests {
    private static final String ARENA = "jugcraft-test:arms_arena";

    private static ItemStack stack(String id, int count) {
        return new ItemStack(BuiltInRegistries.ITEM.getValue(
                net.minecraft.resources.Identifier.parse(id.contains(":") ? id : "jugcraft:" + id)), count);
    }

    private static ItemStack stack(String id) { return stack(id, 1); }

    @GameTest
    public void circuitsRewardEachPreparedIngredientInAnySlot(GameTestHelper h) {
        var workshop = WorkshopCatalog.get(h.getLevel().getServer(), 0);
        for (boolean wired : List.of(false, true)) {
            for (boolean prepared : List.of(false, true)) {
                int count = (wired ? 2 : 1) * (prepared ? 2 : 1);
                var basic = List.of(stack("silicon"), stack(wired ? "copper_wire" : "minecraft:copper_ingot", wired ? 3 : 1),
                        stack(prepared ? "solder_ingot" : "tin_ingot"));
                var advanced = List.of(stack("basic_circuit", 2), stack(wired ? "silver_wire" : "silver_ingot", wired ? 3 : 1),
                        stack(prepared ? "invar_plate" : "invar_ingot"));
                for (int[] order : List.of(new int[]{0,1,2}, new int[]{0,2,1}, new int[]{1,0,2},
                        new int[]{1,2,0}, new int[]{2,0,1}, new int[]{2,1,0})) {
                    checkCircuit(h, basic, order, "basic_circuit", count, 200);
                    checkCircuit(h, advanced, order, "advanced_circuit", count, 300);
                }
                var matches = workshop.entries().stream()
                        .filter(e -> e.recipe().matches(new MachineInput(basic), h.getLevel())).toList();
                h.assertTrue(matches.size() == 1, "One workshop choice per ingredient combination: " + basic);
                ItemStack output = matches.getFirst().recipe().result().create();
                h.assertTrue(output.is(stack("basic_circuit").getItem()) && output.getCount() == count,
                        "Workshop and assembler yields agree: " + output);
                if (wired) {
                    var shortBasic = List.of(basic.get(0), stack("copper_wire", 2), basic.get(2));
                    var shortAdvanced = List.of(advanced.get(0), stack("silver_wire", 2), advanced.get(2));
                    h.assertTrue(MachineRecipes.findMulti(h.getLevel(), MachineKind.CIRCUIT_ASSEMBLER, shortBasic).isEmpty()
                            && MachineRecipes.findMulti(h.getLevel(), MachineKind.CIRCUIT_ASSEMBLER, shortAdvanced).isEmpty(),
                            "Two wires never pay for a three-wire recipe");
                }
            }
        }
        h.assertTrue(workshop.index(Jugcraft.id("starter_workshop/basic_circuit")) >= 0,
                "The original prepared workshop recipe keeps its ID");
        h.succeed();
    }

    private static void checkCircuit(GameTestHelper h, List<ItemStack> ingredients, int[] order,
            String product, int count, int time) {
        var input = List.of(ingredients.get(order[0]), ingredients.get(order[1]), ingredients.get(order[2]));
        var match = MachineRecipes.findMulti(h.getLevel(), MachineKind.CIRCUIT_ASSEMBLER, input)
                .orElseThrow(() -> h.assertionException("No circuit recipe for " + input));
        ItemStack output = match.recipe().output().create();
        h.assertTrue(output.is(stack(product).getItem()) && output.getCount() == count && match.recipe().time() == time,
                "Circuit yield/time: " + output + " from " + input);
        for (int i = 0; i < 3; i++) h.assertTrue(match.take()[i] == input.get(i).getCount(), "Exact ingredient payment");
    }

    @GameTest
    public void inventoryCircuitAndAllThreeMotorTiersCraft(GameTestHelper h) {
        checkCraft(h, 2, 2, List.of(stack("tin_ingot"), ItemStack.EMPTY,
                stack("silicon"), stack("minecraft:copper_ingot")), "basic_circuit", 1);
        checkCraft(h, 3, 3, List.of(stack("iron_plate"), stack("copper_wire"), stack("iron_plate"),
                stack("copper_wire"), stack("iron_shaft"), stack("copper_wire"),
                stack("iron_plate"), stack("copper_cable"), stack("iron_plate")), "electric_motor", 1);
        checkCraft(h, 3, 3, List.of(stack("iron_plate"), stack("copper_wire"), stack("iron_plate"),
                stack("precision_bearing"), stack("precision_shaft"), stack("precision_bearing"),
                stack("copper_cable"), stack("porcelain_insulator"), stack("copper_cable")), "electric_motor", 2);
        checkCraft(h, 3, 3, List.of(stack("precision_bearing"), stack("copper_wire"), stack("precision_bearing"),
                stack("precision_shaft"), stack("neodymium_magnet"), stack("polymer_housing"),
                stack("copper_cable"), stack("porcelain_insulator"), stack("copper_cable")), "electric_motor", 4);
        h.succeed();
    }

    private static void checkCraft(GameTestHelper h, int width, int height, List<ItemStack> slots, String product, int count) {
        var grid = CraftingInput.of(width, height, slots);
        var recipe = h.getLevel().recipeAccess().getRecipeFor(RecipeType.CRAFTING, grid, h.getLevel())
                .orElseThrow(() -> h.assertionException("No crafting recipe for " + slots));
        ItemStack output = recipe.value().assemble(grid);
        h.assertTrue(output.is(stack(product).getItem()) && output.getCount() == count, "Crafted " + output);
    }

    @GameTest(structure = ARENA, maxTicks = 600)
    public void fourCircuitBatchesWaitForSpaceAndConsumeOnce(GameTestHelper h) {
        var basic = place(h, MachineKind.CIRCUIT_ASSEMBLER, new BlockPos(3, 1, 3));
        var advanced = place(h, MachineKind.CIRCUIT_ASSEMBLER, new BlockPos(9, 1, 3));
        basic.setItem(0, stack("silicon")); basic.setItem(1, stack("copper_wire", 3)); basic.setItem(2, stack("solder_ingot"));
        advanced.setItem(0, stack("basic_circuit", 2)); advanced.setItem(1, stack("silver_wire", 3)); advanced.setItem(2, stack("invar_plate"));
        basic.setItem(3, stack("basic_circuit", 61)); advanced.setItem(3, stack("advanced_circuit", 61));
        h.runAfterDelay(40, () -> {
            h.assertTrue(basic.getItem(0).getCount() == 1 && basic.getItem(1).getCount() == 3
                    && basic.getItem(2).getCount() == 1 && basic.getItem(3).getCount() == 61,
                    "A four-basic batch waits without consuming when only three fit");
            h.assertTrue(advanced.getItem(0).getCount() == 2 && advanced.getItem(1).getCount() == 3
                    && advanced.getItem(2).getCount() == 1 && advanced.getItem(3).getCount() == 61,
                    "A four-advanced batch waits without consuming when only three fit");
            basic.setItem(3, ItemStack.EMPTY); advanced.setItem(3, ItemStack.EMPTY);
        });
        h.succeedWhen(() -> {
            for (var machine : List.of(basic, advanced)) {
                h.assertTrue(machine.getItem(3).getCount() == 4, "Exactly four circuits per batch");
                for (int i = 0; i < 3; i++) h.assertTrue(machine.getItem(i).isEmpty(), "Consume each input once");
            }
        });
    }

    @GameTest(structure = ARENA, maxTicks = 400)
    public void nitricAcidDoublesMicrochipsWithTheSameWaferAndTime(GameTestHelper h) {
        var sulfuric = place(h, MachineKind.LITHOGRAPHY_STATION, new BlockPos(3, 1, 3));
        var nitric = place(h, MachineKind.LITHOGRAPHY_STATION, new BlockPos(9, 1, 3));
        sulfuric.tanks().input(0).fill(PetroFluids.SULFURIC_ACID.source(), 100);
        nitric.tanks().input(0).fill(PetroFluids.NITRIC_ACID.source(), 50);
        for (var station : List.of(sulfuric, nitric)) {
            station.setItem(0, stack("silicon_wafer")); station.setItem(1, stack("copper_wire", 2));
        }
        h.succeedWhen(() -> {
            h.assertTrue(sulfuric.getItem(2).is(stack("microchip").getItem()) && sulfuric.getItem(2).getCount() == 4,
                    "Sulfuric keeps four microchips");
            h.assertTrue(nitric.getItem(2).is(stack("microchip").getItem()) && nitric.getItem(2).getCount() == 8,
                    "Nitric makes eight microchips");
            for (var station : List.of(sulfuric, nitric)) {
                h.assertTrue(station.getItem(0).isEmpty() && station.getItem(1).isEmpty()
                        && station.tanks().input(0).millibuckets() == 0, "One wafer, two wires and exactly one acid batch consumed");
            }
        });
    }

    private static MachineBlockEntity place(GameTestHelper h, MachineKind kind, BlockPos pos) {
        var block = (LargeMachineBlock) JugcraftMachines.MACHINES.get(kind);
        h.setBlock(pos, block.defaultBlockState().setValue(MachineBlock.FACING, Direction.NORTH));
        block.setPlacedBy(h.getLevel(), h.absolutePos(pos), h.getBlockState(pos), null, ItemStack.EMPTY);
        var energy = EnergyStorage.SIDED.find(h.getLevel(), h.absolutePos(pos), Direction.UP);
        h.assertTrue(energy instanceof SimpleEnergyStorage, "Machine accepts power");
        ((SimpleEnergyStorage) energy).setAmount(energy.getCapacity());
        return h.getBlockEntity(pos, MachineBlockEntity.class);
    }
}

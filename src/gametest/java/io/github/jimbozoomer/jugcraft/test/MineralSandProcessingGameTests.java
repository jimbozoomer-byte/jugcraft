package io.github.jimbozoomer.jugcraft.test;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.chemistry.FluidRecipe;
import io.github.jimbozoomer.jugcraft.chemistry.FluidRecipes;
import io.github.jimbozoomer.jugcraft.energy.SimpleEnergyStorage;
import io.github.jimbozoomer.jugcraft.machine.*;
import io.github.jimbozoomer.jugcraft.machine.form.*;
import io.github.jimbozoomer.jugcraft.workshop.*;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.TagValueInput;

/** Real recipe-manager, machine, inventory and tool regressions for the four short sand lines. */
public class MineralSandProcessingGameTests {
    private static final String ARENA = "jugcraft-test:arms_arena";
    private static final BlockPos AT = new BlockPos(7, 1, 7);
    private static final String RECOVERY = "sands_rare_earth_separation";
    private static final String[] OXIDES = {"neodymium_oxide", "cerium_oxide", "yttrium_oxide"};
    private static final int[] COUNTS = {1, 2, 1};

    @GameTest(structure = ARENA)
    public void rareEarthRecipeHasThreeIndependentRollsAndAllEightCombinations(GameTestHelper h) {
        var recipe = fluid(h, RECOVERY);
        h.assertTrue(recipe.hasChancedResults() && recipe.results().size() == 3,
            "One native recipe has all three independently chanced products");
        for (int i = 0; i < 3; i++) {
            h.assertTrue(recipe.resultChance(i) == .5, "Each oxide has its own 50% chance");
            h.assertTrue(recipe.results().get(i).create().is(stack(OXIDES[i], 1).getItem())
                && recipe.results().get(i).create().getCount() == COUNTS[i], "Expected oxide and successful-roll quantity");
        }
        int[] totals = new int[4];
        for (int mask = 0; mask < 8; mask++) {
            int outcome = mask;
            int[] draws = {0};
            var result = recipe.rollResults(() -> (outcome & (1 << draws[0]++)) != 0 ? .25 : .75);
            h.assertTrue(draws[0] == 3 && result.size() == 3, "Exactly three independent draws, including failed rolls");
            assertOutputs(h, result, mask);
            totals[Integer.bitCount(mask)]++;
        }
        h.assertTrue(java.util.Arrays.equals(totals, new int[]{1, 3, 3, 1}),
            "Eight equally probable outcomes give 12.5/37.5/37.5/12.5 percent");
        var guaranteed = fluid(h, "titanium_sponge");
        h.assertTrue(!guaranteed.hasChancedResults(), "Old recipes retain guaranteed output");
        guaranteed.rollResults(() -> { throw new AssertionError("Guaranteed result must not draw RNG"); });
        h.succeed();
    }

    @GameTest(structure = ARENA)
    public void everyRareEarthOutcomePaysOnceAcrossReloadAndNeverRerolls(GameTestHelper h) {
        var recipe = fluid(h, RECOVERY);
        for (int mask = 0; mask < 8; mask++) {
            var machine = compact(h, MachineKind.CHEMICAL_REACTOR);
            feed(machine, recipe);
            tick(h, machine, 120);
            h.assertTrue(machine.processingProgress() == 120, "Partial paid batch progresses before reload");
            reload(h, machine);
            tick(h, machine, recipe.time() - 121);
            h.getLevel().getRandom().setSeed(seedFor(mask));
            tick(h, machine, 1);
            assertOutputs(h, outputs(machine), mask);
            h.assertTrue(machine.getItem(0).isEmpty() && machine.getItem(1).isEmpty()
                && machine.tanks().input(0).millibuckets() == 0, "Exactly one mixed-oxide/acid batch is consumed, including all-failure");
            h.assertTrue(machine.energyFor(null).getAmount() == MachineKind.CHEMICAL_REACTOR.capacity
                - (long) recipe.time() * MachineKind.CHEMICAL_REACTOR.usePerTick, "Power is paid once across reload");
            var savedOutputs = outputs(machine).stream().map(ItemStack::copy).toList();
            long remaining = machine.energyFor(null).getAmount();
            reload(h, machine);
            tick(h, machine, 30);
            for (int i = 0; i < 3; i++) h.assertTrue(ItemStack.matches(savedOutputs.get(i), machine.getItem(2 + i)),
                "Saved completed result cannot change or repeat on idle/reload");
            h.assertTrue(machine.energyFor(null).getAmount() == remaining, "Completed batch spends no further power");
            clear(h, machine);
        }
        h.succeed();
    }

    @GameTest(structure = ARENA)
    public void eachPotentialCoproductNeedsRoomBeforeAnyInputPowerOrRandomDraw(GameTestHelper h) {
        var recipe = fluid(h, RECOVERY);
        for (int blocked = 0; blocked < 3; blocked++) {
            var machine = compact(h, MachineKind.CHEMICAL_REACTOR);
            feed(machine, recipe);
            machine.setItem(2 + blocked, blocked == 1 ? stack("cerium_oxide", 63) : new ItemStack(Items.STONE));
            h.getLevel().getRandom().setSeed(331L);
            double next = RandomSource.create(331L).nextDouble();
            tick(h, machine, 25);
            h.assertTrue(machine.processingProgress() == 0 && machine.energyFor(null).getAmount() == machine.kind().capacity,
                "Blocked output " + blocked + " cannot start paid work");
            h.assertTrue(machine.getItem(0).getCount() == 1 && machine.tanks().input(0).millibuckets() == 250,
                "Full coproduct storage preserves every input");
            h.assertTrue(h.getLevel().getRandom().nextDouble() == next, "A blocked batch draws no chance result");
            machine.setItem(2 + blocked, ItemStack.EMPTY);
            tick(h, machine, recipe.time() - 1);
            h.getLevel().getRandom().setSeed(seedFor(7));
            tick(h, machine, 1);
            assertOutputs(h, outputs(machine), 7);
            clear(h, machine);
        }
        h.succeed();
    }

    @GameTest(structure = ARENA)
    public void blockedInFlightRecoveryKeepsPaidProgressAcrossReloadAndFinishesOnce(GameTestHelper h) {
        var recipe = fluid(h, RECOVERY);
        var machine = compact(h, MachineKind.CHEMICAL_REACTOR);
        feed(machine, recipe);
        tick(h, machine, 93);
        long afterPartialWork = machine.energyFor(null).getAmount();
        h.assertTrue(machine.processingProgress() == 93
            && afterPartialWork == machine.kind().capacity - 93L * machine.kind().usePerTick, "Partial work is paid exactly once");
        machine.setItem(3, stack("cerium_oxide", 63)); // Only one free place, but the possible cerium result needs two.
        h.getLevel().getRandom().setSeed(713L);
        double expectedNext = RandomSource.create(713L).nextDouble();
        tick(h, machine, 47);
        h.assertTrue(machine.processingProgress() == 93 && machine.energyFor(null).getAmount() == afterPartialWork,
            "A newly blocked coproduct pauses without resetting already paid progress");
        reload(h, machine);
        tick(h, machine, 29);
        h.assertTrue(machine.processingProgress() == 93 && machine.energyFor(null).getAmount() == afterPartialWork
            && machine.getItem(0).getCount() == 1 && machine.tanks().input(0).millibuckets() == 250,
            "Blocked reload preserves the job, feed, acid and exact paid work");
        h.assertTrue(h.getLevel().getRandom().nextDouble() == expectedNext, "Neither blocking nor reload rolls or rerolls recovery");
        machine.setItem(3, ItemStack.EMPTY);
        tick(h, machine, recipe.time() - 94);
        h.assertTrue(machine.processingProgress() == recipe.time() - 1
            && machine.getItem(0).getCount() == 1, "Only the exact remaining ticks are required after unblocking");
        h.getLevel().getRandom().setSeed(seedFor(5));
        tick(h, machine, 1);
        assertOutputs(h, outputs(machine), 5);
        h.assertTrue(machine.getItem(0).isEmpty() && machine.tanks().input(0).millibuckets() == 0
            && machine.energyFor(null).getAmount() == machine.kind().capacity - (long)recipe.time() * machine.kind().usePerTick,
            "Resumed batch consumes once and costs exactly one complete batch");
        long completedEnergy = machine.energyFor(null).getAmount();
        reload(h, machine);
        tick(h, machine, 20);
        assertOutputs(h, outputs(machine), 5);
        h.assertTrue(machine.energyFor(null).getAmount() == completedEnergy, "Completed recovery cannot repeat after another reload");
        h.succeed();
    }

    @GameTest(structure = ARENA)
    public void changingOnlyFluidRecipeCannotTransferPaidProgressToAnotherJob(GameTestHelper h) {
        var machine = compact(h, MachineKind.CHEMICAL_REACTOR);
        var old = fluid(h, "absorb_hydrogen_chloride");
        var next = fluid(h, "absorb_sulfur_trioxide");
        feed(machine, old);
        tick(h, machine, 40);
        h.assertTrue(machine.processingProgress() == 40, "First fluid-only recipe has paid progress");
        machine.tanks().input(1).drain(old.fluids().get(1).amount());
        machine.tanks().input(1).fill(next.fluids().get(1).fluid(), next.fluids().get(1).amount());
        machine.tanks().input(2).fill(next.fluids().get(2).fluid(), next.fluids().get(2).amount());
        tick(h, machine, 1);
        h.assertTrue(machine.processingProgress() == 1 && machine.tanks().output(0).millibuckets() == 0,
            "A different valid recipe starts from zero even when no solid setItem callback occurs");
        tick(h, machine, next.time() - 1);
        var output = next.fluidResults().getFirst();
        h.assertTrue(machine.tanks().output(0).has(output.fluid(), output.amount())
            && machine.tanks().output(0).millibuckets() == output.amount(), "Only the new job's paid fluid output is produced");
        h.assertTrue(machine.energyFor(null).getAmount() == machine.kind().capacity
            - (40L + next.time()) * machine.kind().usePerTick, "Recipe switch cannot reuse another recipe's paid work");
        h.succeed();
    }

    @GameTest(structure = ARENA)
    public void oldSingleOutputReactorKeepsInputsOutputProgressAndFluidRoles(GameTestHelper h) {
        var machine = compact(h, MachineKind.CHEMICAL_REACTOR);
        var recipe = fluid(h, "titanium_sponge");
        feed(machine, recipe);
        machine.setItem(2, stack("titanium_sponge", 7));
        var old = machine.saveWithoutMetadata(h.getLevel().registryAccess());
        old.remove("item_layout"); // Before this feature, only nonempty slots 0, 1 and 2 existed.
        old.putInt("progress", 60);
        old.putInt("max_progress", recipe.time());
        old.putLong("energy", 25000);
        machine.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, h.getLevel().registryAccess(), old));
        h.assertTrue(machine.getContainerSize() >= 5 && machine.getItem(2).getCount() == 7
            && machine.getItem(3).isEmpty() && machine.getItem(4).isEmpty(), "Old output stays at index 2; new outputs begin empty");
        h.assertTrue(machine.processingProgress() == 60 && machine.getItem(0).is(stack("raw_titanium", 1).getItem())
            && machine.getItem(1).is(stack("coke", 1).getItem()), "Legacy paid progress and both input slots survive");
        tick(h, machine, recipe.time() - 60);
        h.assertTrue(machine.getItem(2).getCount() == 8 && machine.getItem(3).isEmpty() && machine.getItem(4).isEmpty(),
            "Old one-output batch completes exactly once in its original output slot");
        h.assertTrue(machine.energyFor(null).getAmount() == 25000 - (long)(recipe.time() - 60) * machine.kind().usePerTick,
            "Legacy completion charges only the remaining work");
        h.succeed();
    }

    @GameTest(structure = ARENA, maxTicks = 260)
    public void titaniumAndZirconSandReachTheirExistingFinishedMaterials(GameTestHelper h) {
        var raw = formBatch(h, IndustrialForms.MINERAL_CONDITIONER, "sands_titanium_feed");
        h.assertTrue(raw.is(stack("raw_titanium", 1).getItem()) && raw.getCount() == 1, "Titanium sand has a real raw-mineral consumer");
        var reactor = compact(h, MachineKind.CHEMICAL_REACTOR);
        var titanium = fluid(h, "titanium_sponge");
        feed(reactor, titanium);
        reactor.setItem(0, raw);
        tick(h, reactor, titanium.time());
        var sponge = reactor.getItem(2).copy();
        h.assertTrue(sponge.is(stack("titanium_sponge", 1).getItem()), "Sand-derived raw feed makes existing sponge");
        clear(h, reactor);
        var center = AT.relative(Direction.SOUTH);
        for (int x = -1; x <= 1; x++) for (int y = -1; y <= 1; y++) for (int z = -1; z <= 1; z++) {
            var pos = center.offset(x, y, z);
            if (!pos.equals(AT)) h.setBlock(pos, JugcraftMachines.ARC_FURNACE_CASING);
        }
        var arc = compact(h, MachineKind.ARC_FURNACE);
        arc.setItem(0, sponge);
        h.assertTrue(ArcFurnaceStructure.isFormed(h.getLevel(), arc.getBlockPos(), Direction.NORTH,
            JugcraftMachines.ARC_FURNACE_CASING), "The real 3x3x3 furnace casing is complete");
        // Let the real server clock cross the furnace's 20-tick structure check and then pay its 160 work ticks.
        // Advancing a fake local tick loop at one fixed world time would never reliably form this multiblock.
        h.runAtTickTime(200, () -> {
            h.assertTrue(arc.getItem(arc.kind().outputSlot()).is(stack("titanium_ingot", 1).getItem()),
                "Three general-machine steps finish an existing titanium ingot");
            h.assertTrue(arc.energyFor(null).getAmount() == arc.kind().capacity - 160L * arc.kind().usePerTick,
                "The actual formed furnace pays exactly one unboosted titanium melt");
            for (int x = -1; x <= 1; x++) for (int y = -1; y <= 1; y++) for (int z = -1; z <= 1; z++)
                h.getLevel().setBlock(h.absolutePos(center.offset(x, y, z)), Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
            zirconLine(h);
            h.succeed();
        });
    }

    private static void zirconLine(GameTestHelper h) {
        var zircon = fluid(h, "sands_zirconia_powder");
        var reactor = compact(h, MachineKind.CHEMICAL_REACTOR);
        feed(reactor, zircon);
        long initialEnergy = reactor.energyFor(null).getAmount(), suppliedEnergy = 0;
        for (int i = 0; i < zircon.time(); i++) {
            if (reactor.energyFor(null).getAmount() < reactor.kind().usePerTick) {
                suppliedEnergy += reactor.kind().capacity - reactor.energyFor(null).getAmount();
                ((SimpleEnergyStorage)reactor.energyFor(null)).setAmount(reactor.kind().capacity);
            }
            tick(h, reactor, 1);
        }
        h.assertTrue(reactor.getItem(2).is(stack("zirconia_powder", 1).getItem()), "Four zircon sand make existing powder");
        h.assertTrue(initialEnergy + suppliedEnergy - reactor.energyFor(null).getAmount()
            == (long)zircon.time() * reactor.kind().usePerTick, "Zircon's 30,720 JE batch includes every refill above its 30,000 JE buffer");
        clear(h, reactor);
        var blank = formBatch(h, IndustrialForms.ALUMINA_CALCINER, "sands_fire_zirconia_blank");
        h.assertTrue(blank.is(stack("fired_zirconia_blank", 1).getItem()), "General oven combines preparation and firing");
        var inserts = formBatch(h, IndustrialForms.PRECISION_FINISHER, "finish_zirconia_insert");
        h.assertTrue(inserts.is(stack("zirconia_insert", 1).getItem()) && inserts.getCount() == 2,
            "Third zircon step produces useful existing mold/die inputs");
    }

    @GameTest(structure = ARENA)
    public void namedOxidesGarnetAndReusableHeadsHaveRealRestrictedConsumers(GameTestHelper h) throws Exception {
        var mixed = formBatch(h, IndustrialForms.MINERAL_CONDITIONER, "sands_mixed_rare_earth_oxide");
        h.assertTrue(mixed.is(stack("rare_earth_oxide", 1).getItem()), "Rare-earth sand feeds existing mixed oxide");
        var pulverizer = compact(h, MachineKind.PULVERIZER);
        pulverizer.setItem(0, stack("garnet_sand", 1));
        tick(h, pulverizer, 120);
        h.assertTrue(pulverizer.getItem(pulverizer.kind().outputSlot()).is(stack("garnet_grit", 1).getItem()),
            "One general pulverizing step supplies useful abrasive");
        clear(h, pulverizer);
        for (String name : List.of("sands_cerium_polishing_head", "sands_garnet_abrasive_head", "sands_garnet_sanding_block", "sands_efficiency_upgrade"))
            assertCraft(h, name);
        var garnet = formBatch(h, IndustrialForms.PRECISION_FINISHER, "sands_finish_shaft_garnet");
        h.assertTrue(garnet.is(stack("precision_shaft", 1).getItem()), "Garnet tool finishes existing steel shaft");
        var lens = formBatch(h, IndustrialForms.PRECISION_FINISHER, "sands_finish_lens_cerium");
        h.assertTrue(lens.is(stack("polished_lens", 1).getItem()), "Cerium's useful optics consumer is loaded and executable");
        for (var entry : formRecipes(h, IndustrialForms.PRECISION_FINISHER)) {
            var r = entry.recipe();
            if (r.results().stream().anyMatch(s -> s.create().is(stack("zirconia_insert", 1).getItem())
                || s.create().is(stack("alumina_plate", 1).getItem()))) {
                h.assertTrue(r.tool().isPresent() && !r.tool().get().test(stack("garnet_abrasive_head", 1)),
                    "Garnet must not machine hard technical ceramics");
            }
        }
        var smelter = compact(h, MachineKind.ALLOY_SMELTER);
        smelter.setItem(0, stack("neodymium_oxide", 1));
        smelter.setItem(1, stack("ferroboron", 1));
        tick(h, smelter, 200);
        h.assertTrue(smelter.getItem(smelter.kind().outputSlot()).is(stack("neodymium_magnet", 1).getItem())
            && smelter.getItem(smelter.kind().outputSlot()).getCount() == 4, "Named oxide has a real existing-magnet consumer");
        clear(h, smelter);
        for (String name : List.of("sands_smooth_sandstone", "sands_smooth_red_sandstone")) sanding(h, name);
        var kilnBlock = JugcraftWorkshop.BLOCKS[1];
        h.setBlock(AT, kilnBlock.defaultBlockState());
        h.assertTrue(!h.getBlockEntity(AT, WorkshopBlockEntity.class).canPlaceItem(WorkshopBlockEntity.TOOL,
            stack("garnet_sanding_block", 1)), "Sanding block is not an unrelated kiln tool");
        h.succeed();
    }

    private static void sanding(GameTestHelper h, String name) {
        var block = JugcraftWorkshop.BLOCKS[0];
        var state = block.defaultBlockState();
        h.setBlock(AT, state);
        block.setPlacedBy(h.getLevel(), h.absolutePos(AT), state, null, ItemStack.EMPTY);
        var bench = h.getBlockEntity(AT, WorkshopBlockEntity.class);
        h.assertTrue(bench.select(Jugcraft.id("starter_workshop/" + name)), "Sanding recipe selectable at existing bench");
        boolean red = name.contains("red");
        bench.setItem(0, new ItemStack(red ? Items.CUT_RED_SANDSTONE : Items.CUT_SANDSTONE));
        h.assertTrue(bench.canPlaceItem(WorkshopBlockEntity.TOOL, stack("garnet_sanding_block", 1))
            && !bench.canPlaceItem(WorkshopBlockEntity.TOOL, new ItemStack(Items.STONE)),
            "Real workshop slot filter accepts the sanding tool and rejects unrelated items");
        bench.setItem(3, stack("garnet_sanding_block", 1));
        var player = h.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.SURVIVAL);
        player.snapTo(bench.getBlockPos().getX() + .5, bench.getBlockPos().getY(), bench.getBlockPos().getZ() - 1.5, 0, 0);
        player.containerMenu = bench.createMenu(41, player.getInventory(), player);
        h.assertTrue(player.containerMenu.slots.get(WorkshopBlockEntity.TOOL).mayPlace(stack("garnet_sanding_block", 1))
            && !player.containerMenu.slots.get(WorkshopBlockEntity.TOOL).mayPlace(new ItemStack(Items.STONE)),
            "The actual player menu permits installing the sanding block without direct-container bypass");
        for (int i = 0; i < 200; i++) {
            ((WorkshopMenu)player.containerMenu).clickMenuButton(player, WorkshopMenu.PULSE);
            bench.tick(h.getLevel());
        }
        bench.release(player);
        h.assertTrue(bench.getItem(5).is(red ? Items.SMOOTH_RED_SANDSTONE : Items.SMOOTH_SANDSTONE)
            && bench.getItem(5).getCount() == 1 && bench.getItem(0).isEmpty(), "One building block becomes one finished block");
        h.assertTrue(bench.getItem(3).is(stack("garnet_sanding_block", 1).getItem()) && bench.getItem(3).getCount() == 1,
            "Sanding block remains reusable");
        for (int i = block.footprint().size() - 1; i >= 0; i--)
            h.getLevel().setBlock(block.footprint().partPos(bench.getBlockPos(), bench.getBlockState().getValue(WorkshopBlock.FACING), i),
                Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
    }

    private static void assertCraft(GameTestHelper h, String name) throws Exception {
        try (var in = MineralSandProcessingGameTests.class.getResourceAsStream("/data/jugcraft/recipe/" + name + ".json")) {
            if (in == null) throw new AssertionError("Missing crafting consumer " + name);
            var json = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
            var ops = RegistryOps.create(JsonOps.INSTANCE, h.getLevel().registryAccess());
            var slots = new ArrayList<ItemStack>();
            int width = 3, height;
            if (json.has("pattern")) {
                var pattern = json.getAsJsonArray("pattern");
                width = pattern.get(0).getAsString().length(); height = pattern.size();
                for (var row : pattern) for (char symbol : row.getAsString().toCharArray())
                    slots.add(symbol == ' ' ? ItemStack.EMPTY : new ItemStack(Ingredient.CODEC.parse(ops,
                        json.getAsJsonObject("key").get(String.valueOf(symbol))).getOrThrow().items().findFirst().orElseThrow()));
            } else {
                for (var ingredient : json.getAsJsonArray("ingredients"))
                    slots.add(new ItemStack(Ingredient.CODEC.parse(ops, ingredient).getOrThrow().items().findFirst().orElseThrow()));
                height = (slots.size() + 2) / 3;
                while (slots.size() < width * height) slots.add(ItemStack.EMPTY);
            }
            var input = CraftingInput.of(width, height, slots);
            var found = h.getLevel().recipeAccess().getRecipeFor(RecipeType.CRAFTING, input, h.getLevel()).orElseThrow();
            var expected = json.getAsJsonObject("result");
            h.assertTrue(ItemStack.matches(found.value().assemble(input), stack(expected.get("id").getAsString(),
                expected.has("count") ? expected.get("count").getAsInt() : 1)), "Unique intended crafting result: " + name);
        }
    }

    private static void assertOutputs(GameTestHelper h, List<ItemStack> results, int mask) {
        for (int i = 0; i < 3; i++) {
            var actual = results.get(i);
            h.assertTrue((mask & (1 << i)) == 0 ? actual.isEmpty()
                : actual.is(stack(OXIDES[i], 1).getItem()) && actual.getCount() == COUNTS[i], "Independent outcome " + mask + ", oxide " + i);
        }
    }

    private static long seedFor(int mask) {
        for (long seed = 0; seed < 100000; seed++) {
            var random = RandomSource.create(seed);
            int result = 0;
            for (int i = 0; i < 3; i++) if (random.nextDouble() < .5) result |= 1 << i;
            if (result == mask) return seed;
        }
        throw new AssertionError("No seed for independent outcome " + mask);
    }

    private static ItemStack stack(String id, int count) {
        var item = BuiltInRegistries.ITEM.getValue(Identifier.parse(id.contains(":") ? id : "jugcraft:" + id));
        if (item == Items.AIR) throw new AssertionError("Unregistered sand-chain item " + id);
        return new ItemStack(item, count);
    }

    private static FluidRecipe fluid(GameTestHelper h, String name) {
        return FluidRecipes.entries(h.getLevel().getServer(), MachineKind.CHEMICAL_REACTOR).stream()
            .filter(e -> e.id().getPath().endsWith("/" + name)).findFirst().orElseThrow().recipe();
    }

    private static MachineBlockEntity compact(GameTestHelper h, MachineKind kind) {
        var block = JugcraftMachines.MACHINES.get(kind);
        var state = block.defaultBlockState().setValue(MachineBlock.FACING, Direction.NORTH);
        h.setBlock(AT, state);
        block.setPlacedBy(h.getLevel(), h.absolutePos(AT), state, null, ItemStack.EMPTY);
        var machine = h.getBlockEntity(AT, MachineBlockEntity.class);
        ((SimpleEnergyStorage)machine.energyFor(null)).setAmount(kind.capacity);
        return machine;
    }

    private static void feed(MachineBlockEntity machine, FluidRecipe recipe) {
        for (int i = 0; i < recipe.items().size(); i++) machine.setItem(i,
            new ItemStack(recipe.items().get(i).ingredient().items().findFirst().orElseThrow(), recipe.items().get(i).count()));
        for (int i = 0; i < recipe.fluids().size(); i++) machine.tanks().input(i).fill(recipe.fluids().get(i).fluid(), recipe.fluids().get(i).amount());
    }

    private static List<ItemStack> outputs(MachineBlockEntity machine) {
        return List.of(machine.getItem(2), machine.getItem(3), machine.getItem(4));
    }

    private static void tick(GameTestHelper h, MachineBlockEntity machine, int count) {
        for (int i = 0; i < count; i++) machine.serverTick(h.getLevel(), machine.getBlockPos(), machine.getBlockState());
    }

    private static void reload(GameTestHelper h, MachineBlockEntity machine) {
        var save = machine.saveWithoutMetadata(h.getLevel().registryAccess());
        machine.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, h.getLevel().registryAccess(), save));
    }

    private static List<FluidRecipes.Entry> formRecipes(GameTestHelper h, FormMachineBlock block) {
        var state = block.defaultBlockState().setValue(MachineBlock.FACING, Direction.NORTH);
        h.setBlock(AT, state); block.setPlacedBy(h.getLevel(), h.absolutePos(AT), state, null, ItemStack.EMPTY);
        var machine = h.getBlockEntity(AT, FormMachineBlockEntity.class);
        var recipes = machine.recipes(h.getLevel().getServer());
        clear(h, machine);
        return recipes;
    }

    private static ItemStack formBatch(GameTestHelper h, FormMachineBlock block, String name) {
        var state = block.defaultBlockState().setValue(MachineBlock.FACING, Direction.NORTH);
        h.setBlock(AT, state); block.setPlacedBy(h.getLevel(), h.absolutePos(AT), state, null, ItemStack.EMPTY);
        var machine = h.getBlockEntity(AT, FormMachineBlockEntity.class);
        var entry = machine.recipes(h.getLevel().getServer()).stream().filter(e -> e.id().getPath().endsWith("/" + name)).findFirst().orElseThrow();
        var recipe = entry.recipe(); machine.selectRecipe(entry.id());
        for (int i = 0; i < recipe.items().size(); i++) machine.container().setItem(i,
            new ItemStack(recipe.items().get(i).ingredient().items().findFirst().orElseThrow(), recipe.items().get(i).count()));
        for (int i = 0; i < recipe.fluids().size(); i++) machine.tanks().input(i).fill(recipe.fluids().get(i).fluid(), recipe.fluids().get(i).amount());
        recipe.tool().ifPresent(t -> machine.container().setItem(machine.form().firstSocketSlot(),
            new ItemStack(t.items().findFirst().orElseThrow())));
        var tool = recipe.tool().isPresent() ? machine.container().getItem(machine.form().firstSocketSlot()).copy() : ItemStack.EMPTY;
        long initialEnergy = machine.energy().getAmount(), suppliedEnergy = 0;
        long perTick = machine.form().profile().lanePerTick(recipe.energyPerTick() > 0
            ? recipe.energyPerTick() : machine.form().family().usePerTick);
        for (int i = 0; i < recipe.time(); i++) {
            if (machine.energy().getAmount() < perTick) {
                suppliedEnergy += machine.energy().getCapacity() - machine.energy().getAmount();
                ((SimpleEnergyStorage)machine.energy()).setAmount(machine.energy().getCapacity());
            }
            machine.serverTick(h.getLevel(), machine.getBlockPos(), state);
        }
        h.assertTrue(machine.lane(0) == null, "General form completed " + name + ": " + machine.status());
        h.assertTrue(initialEnergy + suppliedEnergy - machine.energy().getAmount() == (long)recipe.time() * perTick,
            "General form pays the exact complete ledger, including refills: " + name);
        var result = machine.container().getItem(machine.form().firstOutputSlot()).copy();
        h.assertTrue(!result.isEmpty(), "Recipe produces usable material: " + name);
        if (!tool.isEmpty()) {
            h.assertTrue(ItemStack.matches(tool, machine.container().getItem(machine.form().firstSocketSlot())), "Installed tool retained: " + name);
            for (var port : machine.form().ports()) if (port.kind().item()) {
                var transfer = machine.itemPort(port);
                h.assertTrue(transfer != null, "Declared item port exists");
                try (var tx = Transaction.openOuter()) {
                    h.assertTrue(transfer.extract(ItemVariant.of(tool), 1, tx) == 0, "Automation cannot extract installed tool");
                }
            }
        }
        clear(h, machine);
        return result;
    }

    private static void clear(GameTestHelper h, FormMachineBlockEntity machine) {
        var form = machine.form(); var facing = machine.getBlockState().getValue(MachineBlock.FACING);
        for (int i = form.footprint().size() - 1; i >= 0; i--)
            h.getLevel().setBlock(form.footprint().partPos(machine.getBlockPos(), facing, i), Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
    }

    private static void clear(GameTestHelper h, MachineBlockEntity machine) {
        var footprint = machine.kind().footprint(); var facing = machine.getBlockState().getValue(MachineBlock.FACING);
        for (int i = footprint.size() - 1; i >= 0; i--)
            h.getLevel().setBlock(footprint.partPos(machine.getBlockPos(), facing, i), Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
    }
}

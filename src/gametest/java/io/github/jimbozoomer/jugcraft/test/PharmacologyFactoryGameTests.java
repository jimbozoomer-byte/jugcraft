package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.chemistry.FluidRecipe;
import io.github.jimbozoomer.jugcraft.chemistry.FluidRecipes;
import io.github.jimbozoomer.jugcraft.energy.EnergyStorage;
import io.github.jimbozoomer.jugcraft.energy.SimpleEnergyStorage;
import io.github.jimbozoomer.jugcraft.machine.MachineBlock;
import io.github.jimbozoomer.jugcraft.machine.MachineKind;
import io.github.jimbozoomer.jugcraft.machine.form.*;
import io.github.jimbozoomer.jugcraft.pharmacology.PharmacyFactory;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.phys.AABB;

/** Actual formed-machine batches, automation boundaries, formula locks and saved production. */
public class PharmacologyFactoryGameTests {
	@GameTest public void everyPharmacyMachineSupportsNativePickaxeRecovery(GameTestHelper h) {
		var pickaxe=new ItemStack(Items.IRON_PICKAXE);
		for(var block:List.of(IndustrialForms.LABORATORY_ANALYZER,IndustrialForms.PHARMACEUTICAL_SEPARATOR,
				IndustrialForms.FORMULATION_PRESS,IndustrialForms.AUTOCLAVE,IndustrialForms.STERILE_FILLER,
				IndustrialForms.MOONSHINE_STILL,IndustrialForms.MASH_CONDITIONING_BARREL,IndustrialForms.RELEASE_COATER)) {
			h.assertTrue(pickaxe.isCorrectToolForDrops(block.defaultBlockState()),"A normal iron pickaxe can recover the complete pharmacy machine: "+BuiltInRegistries.BLOCK.getKey(block));
			h.assertTrue(!ItemStack.EMPTY.isCorrectToolForDrops(block.defaultBlockState()),"Furnace-derived correct-tool requirement remains intact");
		}
		h.succeed();
	}
	private static final String ARENA = "jugcraft-test:arms_arena";
	private static final BlockPos AT = new BlockPos(7, 1, 7);

	private static List<FormMachineBlock> allBlocks() { var result = new java.util.ArrayList<>(blocks()); result.add(IndustrialForms.MASH_CONDITIONING_BARREL); result.add(IndustrialForms.MOONSHINE_STILL); result.add(IndustrialForms.RELEASE_COATER); return result; }

	private static List<FormMachineBlock> blocks() {
		return List.of(IndustrialForms.LABORATORY_ANALYZER, IndustrialForms.PHARMACEUTICAL_SEPARATOR, IndustrialForms.FORMULATION_PRESS,
				IndustrialForms.AUTOCLAVE, IndustrialForms.STERILE_FILLER);
	}

	@GameTest(structure = ARENA)
	public void everyFactoryRecipeConsumesItsInputsOnceAndKeepsItsFormula(GameTestHelper h) {
		int checked = 0;
		for (FormMachineBlock block : allBlocks()) {
			var probe = place(h, block, Direction.NORTH);
			var recipes = probe.recipes(h.getLevel().getServer());
			int expected = block == IndustrialForms.LABORATORY_ANALYZER ? 31 : block == IndustrialForms.PHARMACEUTICAL_SEPARATOR ? 21
					: block == IndustrialForms.FORMULATION_PRESS ? 27 : block == IndustrialForms.STERILE_FILLER ? 8
					: block == IndustrialForms.MOONSHINE_STILL ? 1 : block == IndustrialForms.RELEASE_COATER ? 5
					: block == IndustrialForms.MASH_CONDITIONING_BARREL ? 3 : 2;
			h.assertTrue(recipes.size() == expected, "Authored recipe count for " + block.form().id());
			clear(h, probe);
			for (var entry : recipes) {
				var machine = place(h, block, Direction.NORTH);
				var recipe = entry.recipe();
				feed(machine, recipe);
				long paidEnergy = 0;
				for (int tick = 0; tick < recipe.time(); tick++) {
					// Advanced jobs can require more than one buffer of power, as they do on a powered production line.
					if (!machine.form().ambient() && !machine.form().fuelled() && machine.energy().getAmount() < recipe.energyPerTick())
						((SimpleEnergyStorage) machine.energy()).setAmount(machine.energy().getCapacity());
					long before = machine.energy().getAmount();
					ticks(h, machine, 1);
					paidEnergy += before - machine.energy().getAmount();
				}
				ItemStack result = recipe.results().getFirst().create();
				ItemStack actual = machine.container().getItem(machine.form().firstOutputSlot());
				h.assertTrue(machine.lane(0) == null && actual.is(result.getItem()) && actual.getCount() == result.getCount(),
						"Exact finished batch for " + entry.id());
				h.assertTrue(paidEnergy == (machine.form().ambient() || machine.form().fuelled() ? 0 : (long) recipe.time() * recipe.energyPerTick()), "Exact paid energy for " + entry.id());
				for (int input = 0; input < machine.form().recipeItemInputs(); input++) {
					h.assertTrue(machine.container().getItem(input).isEmpty(), "No double input or bottle remainder " + entry.id());
				}
				for (int tank = 0; tank < machine.form().tanks().inputTanks().size(); tank++) {
					h.assertTrue(machine.tanks().input(tank).millibuckets() == 0, "Every paid fluid consumed once " + entry.id());
				}
				if (recipe.tool().isPresent()) {
					h.assertTrue(recipe.tool().get().test(machine.container().getItem(toolSlot(machine, recipe.tool().get()))), "Reusable formula/process tool survives " + entry.id());
				}
				ticks(h, machine, 3);
				h.assertTrue(machine.container().getItem(machine.form().firstOutputSlot()).getCount() == result.getCount(), "No duplicate output on idle ticks");
				checked++;
				clear(h, machine);
			}
		}
		h.assertTrue(checked == 98, "All ninety-eight item-output pharmacy and beverage recipes ran");
		h.succeed();
	}

	@GameTest(structure = ARENA)
	public void allEightFormsPlaceAndExposeOnlyDeclaredPortsInEveryOrientation(GameTestHelper h) {
		for (FormMachineBlock block : allBlocks()) {
			for (Direction facing : List.of(Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST)) {
				var machine = place(h, block, facing);
				ticks(h, machine, 1);
				h.assertTrue(machine.status().state() != MachineLifecycle.UNFORMED, "Valid physical form " + block.form().id());
				for (int part = 0; part < machine.form().footprint().size(); part++) {
					BlockPos pos = machine.form().footprint().partPos(machine.getBlockPos(), facing, part);
					for (Direction side : Direction.values()) {
						FormPort port = machine.form().port(part, FormSide.of(side, facing));
						h.assertTrue((FluidStorage.SIDED.find(h.getLevel(), pos, side) != null) == (port != null && port.kind().fluid()), "Fluid port " + part + side);
						h.assertTrue((ItemStorage.SIDED.find(h.getLevel(), pos, side) != null) == (port != null && port.kind().item()), "Item port " + part + side);
						h.assertTrue((EnergyStorage.SIDED.find(h.getLevel(), pos, side) != null) == (port != null && port.kind().energy()), "Energy port " + part + side);
					}
				}
				clear(h, machine);
			}
		}
		h.succeed();
	}

	@GameTest(structure = ARENA)
	public void matchingFormulaIsRequiredAndCannotBeStolenDuringWork(GameTestHelper h) {
		var machine = place(h, IndustrialForms.FORMULATION_PRESS, Direction.NORTH);
		var recipe = recipe(machine, h, "pharmacy_batch_paracetamol");
		feed(machine, recipe);
		int socket = machine.form().firstSocketSlot();
		long energy = machine.energy().getAmount();
		machine.container().setItem(socket, ItemStack.EMPTY);
		ticks(h, machine, 1);
		h.assertTrue(machine.status().state() == MachineLifecycle.WAITING_TOOL && machine.lane(0) == null
				&& machine.energy().getAmount() == energy && machine.container().getItem(0).getCount() == 2, "Missing formula spends nothing");
		machine.container().setItem(socket, new ItemStack(PharmacyFactory.item("caffeine_formula_plate")));
		ticks(h, machine, 1);
		h.assertTrue(machine.lane(0) == null && machine.energy().getAmount() == energy, "Another valid formula cannot authorize this medicine");
		machine.container().setItem(socket, new ItemStack(PharmacyFactory.item("paracetamol_formula_plate")));
		ticks(h, machine, 1);
		h.assertTrue(machine.lane(0) != null && machine.socketLocked(0), "Correct formula starts and locks");
		var player = TestPlayers.mock(h);
		var menu = (FormMachineMenu) machine.createMenu(0, player.getInventory(), player);
		h.assertTrue(!menu.getSlot(socket).mayPickup(player), "Screen cannot take an in-use formula");
		for (int part = 0; part < machine.form().footprint().size(); part++) {
			BlockPos pos = machine.form().footprint().partPos(machine.getBlockPos(), Direction.NORTH, part);
			for (Direction side : Direction.values()) {
				var storage = ItemStorage.SIDED.find(h.getLevel(), pos, side);
				if (storage != null) {
					try (Transaction transaction = Transaction.openOuter()) {
						h.assertTrue(storage.extract(ItemVariant.of(PharmacyFactory.item("paracetamol_formula_plate")), 1, transaction) == 0,
							"Automation cannot reach the formula socket");
					}
				}
			}
		}
		ticks(h, machine, recipe.time() - 1);
		h.assertTrue(!machine.socketLocked(0) && machine.container().getItem(socket).getCount() == 1, "Formula unlocks and remains after completion");
		h.succeed();
	}

	@GameTest(structure = ARENA)
	public void fullOutputsBlockEveryStationBeforeAnyInputOrPowerIsSpent(GameTestHelper h) {
		for (FormMachineBlock block : allBlocks()) {
			var machine = place(h, block, Direction.NORTH);
			var recipe = machine.recipes(h.getLevel().getServer()).getFirst().recipe();
			feed(machine, recipe);
			ItemStack full = recipe.results().getFirst().create();
			full.setCount(full.getMaxStackSize());
			machine.container().setItem(machine.form().firstOutputSlot(), full);
			long energy = machine.energy().getAmount();
			int bankedFuel = machine.furnaceTicksRemaining();
			ItemStack fuelStack = machine.form().fuelled() ? machine.container().getItem(machine.form().fuelSlot()).copy() : ItemStack.EMPTY;
			ticks(h, machine, 2);
			h.assertTrue(machine.lane(0) == null && machine.status().state() == MachineLifecycle.OUTPUT_BLOCKED
					&& machine.energy().getAmount() == energy, "Full output stops " + block.form().id());
			if (machine.form().fuelled()) h.assertTrue(machine.furnaceTicksRemaining() == bankedFuel
					&& ItemStack.matches(fuelStack, machine.container().getItem(machine.form().fuelSlot())), "Blocked still spends no banked or item fuel");
			h.assertTrue(machine.container().getItem(0).getCount() == recipe.items().getFirst().count()
					&& (recipe.fluids().isEmpty() || machine.tanks().input(0).millibuckets() == recipe.fluids().getFirst().amount()), "No input lost while blocked");
			clear(h, machine);
		}
		h.succeed();
	}

	@GameTest(structure = ARENA)
	public void savedWorkWaitsWithoutPowerAndFinishesOnlyOnce(GameTestHelper h) {
		for (FormMachineBlock block : blocks()) {
			var machine = place(h, block, Direction.NORTH);
			var recipe = machine.recipes(h.getLevel().getServer()).getFirst().recipe();
			feed(machine, recipe);
			ticks(h, machine, 40);
			var saved = machine.saveWithoutMetadata(h.getLevel().registryAccess());
			machine.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, h.getLevel().registryAccess(), saved));
			int progress = machine.lane(0).progress();
			long energy = machine.energy().getAmount();
			((SimpleEnergyStorage) machine.energy()).setAmount(0);
			ticks(h, machine, 10);
			h.assertTrue(machine.lane(0).progress() == progress, "Saved work waits without power");
			((SimpleEnergyStorage) machine.energy()).setAmount(energy);
			ticks(h, machine, recipe.time() - progress);
			h.assertTrue(machine.lane(0) == null && machine.container().getItem(machine.form().firstOutputSlot()).getCount()
					== recipe.results().getFirst().create().getCount(), "Saved batch finishes with its original output once");
			clear(h, machine);
		}
		h.succeed();
	}

	@GameTest(structure = ARENA)
	public void cancellationReturnsRawInputsAndNeverRefundsPaidPower(GameTestHelper h) {
		for (FormMachineBlock block : allBlocks()) {
			var machine = place(h, block, Direction.NORTH);
			var recipe = machine.recipes(h.getLevel().getServer()).getFirst().recipe();
			feed(machine, recipe);
			ticks(h, machine, 20);
			long energy = machine.energy().getAmount();
			machine.cancel();
			h.assertTrue(machine.lane(0) == null && machine.energy().getAmount() == energy
					&& machine.container().getItem(machine.form().firstOutputSlot()).isEmpty(), "Cancel yields no free power or medicine");
			for (int i = 0; i < recipe.items().size(); i++) {
				h.assertTrue(machine.container().getItem(i).getCount() == recipe.items().get(i).count(), "Original solid inputs refunded");
			}
			for (int i = 0; i < recipe.fluids().size(); i++) {
				h.assertTrue(machine.tanks().input(i).millibuckets() == recipe.fluids().get(i).amount(), "Original fluid inputs refunded");
			}
			clear(h, machine);
		}
		h.succeed();
	}

	@GameTest(structure = ARENA)
	public void breakingARunningPressDropsOneMachineAndUntransformedInputs(GameTestHelper h) {
		var machine = place(h, IndustrialForms.FORMULATION_PRESS, Direction.NORTH);
		feed(machine, recipe(machine, h, "pharmacy_batch_paracetamol"));
		ticks(h, machine, 20);
		BlockPos controller = machine.getBlockPos();
		BlockPos far = machine.form().footprint().partPos(controller, Direction.NORTH, machine.form().footprint().size() - 1);
		h.getLevel().destroyBlock(far, true);
		List<ItemEntity> drops = h.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(controller).inflate(5));
		h.assertTrue(count(drops, IndustrialForms.FORMULATION_PRESS.asItem()) == 1, "One machine drop from a far part");
		h.assertTrue(count(drops, PharmacyFactory.item("pharmaceutical_base")) == 2
				&& count(drops, item("pharmaceutical_starch")) == 2
				&& count(drops, PharmacyFactory.item("paracetamol_formula_plate")) == 1, "Escrow and formula drop exactly once");
		h.assertTrue(count(drops, item("paracetamol")) == 0, "Breaking cannot manufacture medicine");
		h.succeed();
	}

	@GameTest(structure = ARENA)
	public void earlierPartsAndMedicinesReachEveryFactoryStage(GameTestHelper h) {
		var blank = CraftingInput.of(3, 1, List.of(new ItemStack(item("copper_plate")), new ItemStack(Items.PAPER), new ItemStack(Items.GLASS_PANE)));
		var craft = h.getLevel().recipeAccess().getRecipeFor(RecipeType.CRAFTING, blank, h.getLevel()).orElseThrow();
		h.assertTrue(craft.value().assemble(blank).is(PharmacyFactory.item("blank_formula_plate")), "Blank formula uses existing copper/paper/glass");
		for (String kind : List.of("laboratory_analyzer", "pharmaceutical_separator", "formulation_press")) {
			var center = kind.equals("pharmaceutical_separator") ? item("fluid_tank") : kind.equals("formulation_press") ? Items.PISTON : Items.GLASS_PANE;
			var grid = CraftingInput.of(3, 3, List.of(new ItemStack(item("steel_plate")), new ItemStack(center), new ItemStack(item("steel_plate")),
					new ItemStack(item("electric_motor")), new ItemStack(item("basic_circuit")), new ItemStack(item("machine_casing")),
					new ItemStack(item("steel_plate")), new ItemStack(item("copper_cable")), new ItemStack(item("steel_plate"))));
			var construction = h.getLevel().recipeAccess().getRecipeFor(RecipeType.CRAFTING, grid, h.getLevel()).orElseThrow();
			h.assertTrue(construction.value().assemble(grid).is(item(kind)), "Reachable construction with no pharmaceutical prerequisite " + kind);
		}
		var reactor = FluidRecipes.entries(h.getLevel().getServer(), MachineKind.CHEMICAL_REACTOR);
		for (String name : List.of("aspirin", "paracetamol", "stimulant", "pharmacy_base")) {
			h.assertTrue(reactor.stream().anyMatch(entry -> entry.id().equals(Jugcraft.id("chemical_reaction/" + name)) && entry.recipe().legacy()),
					"The compact reactor retains independently reachable " + name);
		}
		var base = reactor.stream().filter(entry -> entry.id().equals(Jugcraft.id("chemical_reaction/pharmacy_base"))).findFirst().orElseThrow().recipe();
		h.assertTrue(base.items().get(1).ingredient().test(new ItemStack(Items.SUGAR))
				&& !base.items().get(1).ingredient().test(new ItemStack(Items.REDSTONE)),
				"Prepared base has a distinct recipe so the earlier Paracetamol receipt cannot intercept it");
		h.succeed();
	}

	@GameTest(structure = ARENA, maxTicks = 250)
	public void aNormallyTickingPressDeliversMedicineThroughItsOutputPort(GameTestHelper h) {
		var machine = place(h, IndustrialForms.FORMULATION_PRESS, Direction.NORTH);
		feed(machine, recipe(machine, h, "pharmacy_batch_caffeine"));
		var port = machine.form().ports().stream().filter(p -> p.name().equals("medicine_out")).findFirst().orElseThrow();
		BlockPos part = machine.form().cellPos(machine.getBlockPos(), Direction.NORTH,
				machine.form().cellIndex(port.column(), port.row(), port.layer()));
		BlockPos storage = part.relative(Direction.NORTH);
		h.getLevel().setBlock(storage, Blocks.CHEST.defaultBlockState(), Block.UPDATE_ALL);
		h.runAfterDelay(200, () -> {
			ChestBlockEntity chest = (ChestBlockEntity) h.getLevel().getBlockEntity(storage);
			int delivered = 0;
			for (int slot = 0; slot < chest.getContainerSize(); slot++) {
				ItemStack stack = chest.getItem(slot);
				if (stack.is(item("caffeine_drink"))) delivered += stack.getCount();
			}
			h.assertTrue(delivered == 8 && machine.container().getItem(machine.form().firstOutputSlot()).isEmpty(),
					"A real world-ticked batch pushes exactly eight drinks into storage");
			h.assertTrue(machine.container().getItem(machine.form().firstSocketSlot()).getCount() == 1, "Automatic delivery leaves the reusable tool");
			h.succeed();
		});
	}

	private static int count(List<ItemEntity> drops, net.minecraft.world.item.Item item) {
		return drops.stream().map(ItemEntity::getItem).filter(stack -> stack.is(item)).mapToInt(ItemStack::getCount).sum();
	}

	private static net.minecraft.world.item.Item item(String id) {
		return BuiltInRegistries.ITEM.getValue(Jugcraft.id(id));
	}

	static FluidRecipe recipe(FormMachineBlockEntity machine, GameTestHelper h, String name) {
		return machine.recipes(h.getLevel().getServer()).stream().filter(entry -> entry.id().getPath().endsWith('/' + name)).findFirst().orElseThrow().recipe();
	}

	static FormMachineBlockEntity place(GameTestHelper h, FormMachineBlock block, Direction facing) {
		var state = block.defaultBlockState().setValue(MachineBlock.FACING, facing);
		h.setBlock(AT, state);
		block.setPlacedBy(h.getLevel(), h.absolutePos(AT), state, null, ItemStack.EMPTY);
		var machine = h.getBlockEntity(AT, FormMachineBlockEntity.class);
		((SimpleEnergyStorage) machine.energy()).setAmount(machine.energy().getCapacity());
		return machine;
	}

	static void feed(FormMachineBlockEntity machine, FluidRecipe recipe) {
		machine.selectRecipe(machine.recipes(machine.getLevel().getServer()).stream().filter(entry -> entry.recipe() == recipe).findFirst().orElseThrow().id());
		for (int i = 0; i < recipe.items().size(); i++) {
			machine.container().setItem(i, new ItemStack(recipe.items().get(i).ingredient().items().findFirst().orElseThrow(), recipe.items().get(i).count()));
		}
		for (int i = 0; i < recipe.fluids().size(); i++) {
			machine.tanks().input(i).fill(recipe.fluids().get(i).fluid(), recipe.fluids().get(i).amount());
		}
		recipe.tool().ifPresent(tool -> machine.container().setItem(toolSlot(machine, tool), new ItemStack(tool.items().findFirst().orElseThrow())));
		if (machine.form().fuelled()) machine.container().setItem(machine.form().fuelSlot(), new ItemStack(Items.COAL));
	}
	static int toolSlot(FormMachineBlockEntity machine, net.minecraft.world.item.crafting.Ingredient tool) {
		ItemStack stack = new ItemStack(tool.items().findFirst().orElseThrow());
		for (int socket = 0; socket < machine.form().sockets().size(); socket++) {
			if (machine.form().sockets().get(socket).accepts(stack)) return machine.form().firstSocketSlot() + socket;
		}
		if (stack.getItem() instanceof io.github.jimbozoomer.jugcraft.pharmacology.PrecisionPharmacy.ProcessModule
				&& machine.form().upgradeSlots() > 0) return machine.form().firstUpgradeSlot();
		throw new AssertionError("No supported tool slot for " + stack + " in " + machine.form().id());
	}

	static void ticks(GameTestHelper h, FormMachineBlockEntity machine, int count) {
		for (int tick = 0; tick < count; tick++) {
			machine.serverTick(h.getLevel(), machine.getBlockPos(), h.getLevel().getBlockState(machine.getBlockPos()));
		}
	}

	static void clear(GameTestHelper h, FormMachineBlockEntity machine) {
		var form = machine.form();
		var facing = machine.getBlockState().getValue(MachineBlock.FACING);
		for (int part = form.footprint().size() - 1; part >= 0; part--) {
			h.getLevel().setBlock(form.footprint().partPos(machine.getBlockPos(), facing, part), Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
		}
	}
}

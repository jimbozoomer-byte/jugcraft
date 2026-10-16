package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.chemistry.FluidRecipes;
import io.github.jimbozoomer.jugcraft.chemistry.FluidTank;
import io.github.jimbozoomer.jugcraft.chemistry.PetroFluids;
import io.github.jimbozoomer.jugcraft.energy.EnergyStorage;
import io.github.jimbozoomer.jugcraft.energy.SimpleEnergyStorage;
import io.github.jimbozoomer.jugcraft.machine.MachineBlock;
import io.github.jimbozoomer.jugcraft.machine.MachineKind;
import io.github.jimbozoomer.jugcraft.machine.form.FormCell;
import io.github.jimbozoomer.jugcraft.machine.form.FormMachineBlockEntity;
import io.github.jimbozoomer.jugcraft.machine.form.FormPort;
import io.github.jimbozoomer.jugcraft.machine.form.FormSide;
import io.github.jimbozoomer.jugcraft.machine.form.IndustrialForms;
import io.github.jimbozoomer.jugcraft.machine.form.MachineForm;
import java.util.List;
import java.util.Optional;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

/**
 * The Reagent Purification Station (docs/features/industrial-reagent-purification.md): it stands whole in every
 * orientation with pipes and cables reaching it only at its four ports; it purifies the Concentrator's concentrated
 * sulfuric acid into electronic-grade acid with the conditioning water used up; every part lights while it works; it is built from a Chemical Reactor and its bill; and
 * a lithography station makes six microchips from a wafer with 50 mB of electronic-grade acid.
 */
public class ReagentPurificationStationGameTests {
	private static final String ARENA = "jugcraft-test:arms_arena";
	private static final int PARTS = 47;
	/** The chemical reactor family's 96 JE/t, the entry profile's full draw. */
	private static final long DRAW = 96;
	private static final int PURIFY_TICKS = 300;
	private static final int PRODUCT = 0;

	/**
	 * In every orientation it fills its 47 blocks and leaves its open positions free, and each block's faces answer
	 * pipes and cables only where the form declares a port. The feed inlet takes concentrated acid, not oxygen; the
	 * water inlet takes water; nothing goes in at the outlet.
	 */
	@GameTest(structure = ARENA)
	public void itStandsWholeAndOnlyItsPortsAnswer(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		MachineForm form = IndustrialForms.REAGENT_PURIFICATION_STATION_FORM;
		helper.assertTrue(form.positions() == 64 && form.footprint().size() == PARTS
				&& IndustrialForms.REAGENT_PURIFICATION_STATION.partProperty().getPossibleValues().size() == PARTS, "47 blocks in a 4x4x4 envelope");
		Direction[] facings = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
		// One copy per orientation, each clear of the others inside the arena.
		BlockPos[] controllers = {new BlockPos(5, 1, 1), new BlockPos(14, 1, 5), new BlockPos(1, 1, 14), new BlockPos(9, 1, 9)};
		for (int i = 0; i < facings.length; i++) {
			Direction facing = facings[i];
			FormMachineBlockEntity machine = place(helper, controllers[i], facing);
			BlockPos controller = machine.getBlockPos();
			for (int part = 0; part < form.footprint().size(); part++) {
				BlockPos at = form.footprint().partPos(controller, facing, part);
				BlockState state = level.getBlockState(at);
				helper.assertTrue(state.is(IndustrialForms.REAGENT_PURIFICATION_STATION) && IndustrialForms.REAGENT_PURIFICATION_STATION.part(state) == part
						&& state.getValue(MachineBlock.FACING) == facing, facing + ": part " + part + " is " + state);
				for (Direction side : Direction.values()) {
					FormPort port = form.port(part, FormSide.of(side, facing));
					boolean fluid = FluidStorage.SIDED.find(level, at, side) != null;
					boolean power = EnergyStorage.SIDED.find(level, at, side) != null;
					helper.assertTrue(fluid == (port != null && port.kind().fluid()) && power == (port != null && port.kind() == FormPort.Kind.ENERGY_IN),
							facing + ": part " + part + " " + side + " answers fluid " + fluid + ", power " + power);
				}
			}
			for (int cell = 0; cell < form.positions(); cell++) {
				if (form.cell(cell) == FormCell.ACCESS) {
					helper.assertTrue(level.getBlockState(form.cellPos(controller, facing, cell)).isAir(), facing + ": open position " + cell + " stays open");
				}
			}
			try (Transaction transaction = Transaction.openOuter()) {
				Storage<FluidVariant> feed = port(helper, machine, "feed_in");
				helper.assertTrue(feed.insert(FluidVariant.of(PetroFluids.OXYGEN.fluid()), FluidConstants.BUCKET, transaction) == 0,
						facing + ": the feed inlet refuses oxygen");
				helper.assertTrue(port(helper, machine, "product_out").insert(FluidVariant.of(PetroFluids.CONCENTRATED_SULFURIC_ACID.source()),
						FluidConstants.BUCKET, transaction) == 0, facing + ": nothing goes in at the outlet");
				helper.assertTrue(feed.insert(FluidVariant.of(PetroFluids.CONCENTRATED_SULFURIC_ACID.source()), FluidConstants.BUCKET, transaction)
						== FluidConstants.BUCKET, facing + ": the feed inlet takes concentrated acid");
				helper.assertTrue(port(helper, machine, "water_in").insert(FluidVariant.of(Fluids.WATER), FluidConstants.BUCKET / 4, transaction) == FluidConstants.BUCKET / 4,
						facing + ": the water inlet takes water");
				transaction.abort();
			}
		}
		helper.succeed();
	}

	/**
	 * A bucket of concentrated acid and a quarter-bucket of water: 300 paid ticks at 96 JE, 28,800 JE, then 900 mB of
	 * electronic-grade acid at the product spout; the water is used up conditioning it.
	 */
	@GameTest(structure = ARENA, maxTicks = 400)
	public void theConcentrateIsPurifiedToElectronicGrade(GameTestHelper helper) {
		FormMachineBlockEntity machine = place(helper, new BlockPos(5, 1, 1), Direction.NORTH);
		long[] supplied = supply(helper, machine);
		insert(helper, port(helper, machine, "feed_in"), PetroFluids.CONCENTRATED_SULFURIC_ACID.source(), 1000);
		insert(helper, port(helper, machine, "water_in"), Fluids.WATER, 250);
		helper.succeedWhen(() -> {
			helper.assertTrue(machine.lane(0) == null && machine.tanks().input(0).millibuckets() == 0, "Still purifying");
			helper.assertTrue(amount(machine.tanks().output(PRODUCT), PetroFluids.ELECTRONIC_GRADE_SULFURIC_ACID.source()) == 900
					&& machine.tanks().input(1).millibuckets() == 0, "900 mB of electronic-grade acid, the water used up");
			long used = used(machine, supplied);
			helper.assertTrue(used == PURIFY_TICKS * DRAW, "Paid " + used + " JE, not " + PURIFY_TICKS * DRAW);
			helper.assertTrue(extract(helper, port(helper, machine, "product_out"), PetroFluids.ELECTRONIC_GRADE_SULFURIC_ACID.source()) == 900,
					"It leaves at the product spout");
		});
	}

	/** While it works every part shows lit, but only the controller gives off light; paused, it goes dark. */
	@GameTest(structure = ARENA, maxTicks = 200)
	public void everyPartLightsWhileItWorks(GameTestHelper helper) {
		FormMachineBlockEntity machine = place(helper, new BlockPos(5, 1, 1), Direction.NORTH);
		supply(helper, machine);
		machine.tanks().input(0).fill(PetroFluids.CONCENTRATED_SULFURIC_ACID.source(), 2000);
		machine.tanks().input(1).fill(Fluids.WATER, 500);
		helper.runAfterDelay(10, () -> {
			assertLit(helper, machine, true);
			machine.togglePause();
		});
		helper.runAfterDelay(15 + FormMachineBlockEntity.LIT_HOLD, () -> {
			assertLit(helper, machine, false);
			helper.succeed();
		});
	}

	/**
	 * It runs its purification recipe and nothing else, is built from a Chemical Reactor, two Steel Tanks, steel
	 * fluid pipe, steel plates and a basic circuit, and its electronic-grade acid makes six microchips from a wafer in
	 * the lithography station, where ordinary acid makes four.
	 */
	@GameTest(structure = ARENA)
	public void itsRecipesBillAndMicrochips(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		FormMachineBlockEntity machine = place(helper, new BlockPos(5, 1, 1), Direction.NORTH);
		List<?> own = machine.recipes(level.getServer()).stream().map(FluidRecipes.Entry::id).sorted().toList();
		helper.assertTrue(own.equals(List.of(Jugcraft.id("chemical_reaction/purification_electronic_grade_sulfuric_acid"))), "Its recipes: " + own);
		RecipeManager.CachedCheck<CraftingInput, CraftingRecipe> crafting = RecipeManager.createCheck(RecipeType.CRAFTING);
		ItemStack plate = stack("steel_plate");
		ItemStack tank = stack("steel_tank");
		CraftingInput bill = CraftingInput.of(3, 3, List.of(plate, stack("steel_fluid_pipe"), plate, tank, stack("chemical_reactor"), tank,
				plate, stack("basic_circuit"), plate));
		Optional<RecipeHolder<CraftingRecipe>> built = crafting.getRecipeFor(bill, level);
		helper.assertTrue(built.isPresent() && built.get().value().assemble(bill).is(IndustrialForms.REAGENT_PURIFICATION_STATION.asItem()),
				"Its bill makes the Reagent Purification Station");
		FluidRecipes.Entry chips = FluidRecipes.entries(level.getServer(), MachineKind.LITHOGRAPHY_STATION).stream()
				.filter(entry -> entry.id().equals(Jugcraft.id("lithography/microchip_electronic_grade"))).findFirst().orElseThrow();
		helper.assertTrue(chips.recipe().fluids().getFirst().amount() == 50
				&& chips.recipe().fluids().getFirst().fluid() == PetroFluids.ELECTRONIC_GRADE_SULFURIC_ACID.source()
				&& chips.recipe().results().getFirst().create().getCount() == 6, "50 mB of electronic-grade acid makes six chips");
		helper.succeed();
	}

	// ------------------------------------------------------------------ helpers

	private static FormMachineBlockEntity place(GameTestHelper helper, BlockPos relative, Direction facing) {
		helper.setBlock(relative, IndustrialForms.REAGENT_PURIFICATION_STATION.defaultBlockState().setValue(MachineBlock.FACING, facing));
		IndustrialForms.REAGENT_PURIFICATION_STATION.setPlacedBy(helper.getLevel(), helper.absolutePos(relative), helper.getBlockState(relative), null,
				ItemStack.EMPTY);
		return helper.getBlockEntity(relative, FormMachineBlockEntity.class);
	}

	private static void assertLit(GameTestHelper helper, FormMachineBlockEntity machine, boolean lit) {
		MachineForm form = machine.form();
		Direction facing = machine.getBlockState().getValue(MachineBlock.FACING);
		for (int part = 0; part < form.footprint().size(); part++) {
			BlockState state = helper.getLevel().getBlockState(form.footprint().partPos(machine.getBlockPos(), facing, part));
			int light = lit && part == 0 ? 13 : 0;
			helper.assertTrue(state.getValue(MachineBlock.LIT) == lit && state.getLightEmission() == light,
					"Part " + part + ": lit " + state.getValue(MachineBlock.LIT) + ", light " + state.getLightEmission() + ", not " + lit + ", " + light);
		}
	}

	private static Storage<FluidVariant> port(GameTestHelper helper, FormMachineBlockEntity machine, String name) {
		MachineForm form = machine.form();
		Direction facing = machine.getBlockState().getValue(MachineBlock.FACING);
		FormPort port = form.ports().stream().filter(p -> p.name().equals(name)).findFirst().orElseThrow();
		BlockPos at = form.cellPos(machine.getBlockPos(), facing, form.cellIndex(port.column(), port.row(), port.layer()));
		Storage<FluidVariant> storage = FluidStorage.SIDED.find(helper.getLevel(), at, port.side().world(facing));
		helper.assertTrue(storage != null, "No storage at port " + name);
		return storage;
	}

	private static long[] supply(GameTestHelper helper, FormMachineBlockEntity machine) {
		SimpleEnergyStorage store = (SimpleEnergyStorage) machine.energy();
		long[] supplied = {0};
		store.setAmount(store.getCapacity());
		helper.onEachTick(() -> {
			supplied[0] += store.getCapacity() - store.getAmount();
			store.setAmount(store.getCapacity());
		});
		return supplied;
	}

	private static long used(FormMachineBlockEntity machine, long[] supplied) {
		return supplied[0] + machine.energy().getCapacity() - machine.energy().getAmount();
	}

	private static void insert(GameTestHelper helper, Storage<FluidVariant> storage, Fluid fluid, int mb) {
		try (Transaction transaction = Transaction.openOuter()) {
			long moved = storage.insert(FluidVariant.of(fluid), mb * FluidConstants.BUCKET / 1000, transaction);
			helper.assertTrue(moved == mb * FluidConstants.BUCKET / 1000, "Only " + moved + " droplets of " + mb + " mB went in");
			transaction.commit();
		}
	}

	private static int extract(GameTestHelper helper, Storage<FluidVariant> storage, Fluid fluid) {
		try (Transaction transaction = Transaction.openOuter()) {
			long moved = storage.extract(FluidVariant.of(fluid), 16 * FluidConstants.BUCKET, transaction);
			transaction.commit();
			return (int) (moved * 1000 / FluidConstants.BUCKET);
		}
	}

	private static int amount(FluidTank tank, Fluid fluid) {
		return !tank.variant.isBlank() && tank.variant.isOf(fluid) ? tank.millibuckets() : 0;
	}

	private static Item item(String path) {
		return BuiltInRegistries.ITEM.getValue(Jugcraft.id(path));
	}

	private static ItemStack stack(String path) {
		return new ItemStack(item(path));
	}
}

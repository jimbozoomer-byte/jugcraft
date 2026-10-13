package io.github.jimbozoomer.jugcraft.machine.form;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.chemistry.FluidFuels;
import io.github.jimbozoomer.jugcraft.machine.MachineBlock;
import io.github.jimbozoomer.jugcraft.machine.MachineKind;
import java.util.List;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * The industrial machine forms the game registers, from the owner's factory plan (docs/features/
 * industrial-machine-foundation.md): package 2's Electrolytic Separator (docs/features/industrial-electrolytic-separator.md)
 * and Gas Burning Generator (docs/features/industrial-gas-burning-generator.md). Each form is also described in
 * tools/industrial_forms.py for its art, recipes and data; tools/check_mod_data.py keeps the two the same. Every form
 * shares the one controller block entity type, {@code jugcraft:machine_form}, so a saved machine keeps its type when
 * more forms join this list.
 */
public final class IndustrialForms {
	/** Splitting water and ordinary brine. */
	public static final String AQUEOUS_ELECTROLYSIS = "jugcraft:aqueous_electrolysis";

	/**
	 * The Electrolytic Separator: the steel-entry form of the Electrolytic Cell family, two wide, two deep and three
	 * tall, every position a block, the controller (its control box) front left on the ground. Water or brine comes in
	 * at the back; oxygen or chlorine, hydrogen and lye leave at the front, with the power socket at the foot of the
	 * right tower. Water leaves the lye tank unused; a full lye tank stops brine until it is emptied.
	 */
	public static final MachineForm ELECTROLYTIC_SEPARATOR_FORM = MachineForm.builder(Jugcraft.id("electrolytic_separator"),
					MachineKind.ELECTROLYTIC_CELL)
			.layer("C#", "##")
			.layer("##", "##")
			.layer("##", "##")
			.profile(OperatingProfile.ENTRY)
			.inputTank("feed")
			.outputTank("anode_gas")
			.outputTank("hydrogen")
			.outputTank("lye")
			.capability(AQUEOUS_ELECTROLYSIS)
			.upgrades()
			.warmup(40)
			.port("feed_in", FormPort.Kind.FLUID_IN, 0, 0, 1, 0, FormSide.BACK)
			.port("anode_gas_out", FormPort.Kind.FLUID_OUT, 0, 1, 0, 2, FormSide.FRONT)
			.port("hydrogen_out", FormPort.Kind.FLUID_OUT, 1, 0, 0, 2, FormSide.FRONT)
			.port("lye_out", FormPort.Kind.FLUID_OUT, 2, 1, 0, 1, FormSide.FRONT)
			.port("power", FormPort.Kind.ENERGY_IN, 0, 1, 0, 0, FormSide.FRONT)
			.build();

	/**
	 * The Gas Burning Generator: the shared hydrogen and methane generator of the starter factory, a form of the
	 * combustion generators' family (the Gas Turbine's energy figures), four wide, five deep and four tall. The skid fills
	 * the ground layer, the engine-generator train the middle columns, the generator housing and the terminal cabinet
	 * the back rows, and the cabinet's top two blocks of the fourth layer; the corners above the skid at the front are
	 * open. The controller is the ignition box at the front left. Fuel comes in low on the right side; power leaves at
	 * the cabinet's socket at the back. It burns only its own fuels, never the turbine's.
	 */
	public static final MachineForm GAS_BURNING_GENERATOR_FORM = MachineForm.builder(Jugcraft.id("gas_burning_generator"),
					MachineKind.GAS_TURBINE)
			.layer("C###", "####", "####", "####", "####")
			.layer(".##.", ".##.", ".##.", "####", "####")
			.layer(".##.", ".##.", ".##.", "####", "####")
			.layer("....", "....", "....", "....", "..##")
			.profile(OperatingProfile.ENTRY)
			.inputTank("fuel")
			.fuel(Jugcraft.id("hydrogen"), FluidFuels.HYDROGEN, FluidFuels.GAS_BURNING_HYDROGEN)
			.fuel(Jugcraft.id("methane"), FluidFuels.METHANE, FluidFuels.GAS_BURNING_METHANE)
			.port("fuel_in", FormPort.Kind.FLUID_IN, 0, 3, 1, 0, FormSide.RIGHT)
			.port("power_out", FormPort.Kind.ENERGY_OUT, 0, 2, 4, 1, FormSide.BACK)
			.build();


	public static final MachineForm CHEMICAL_INFUSER_FORM = MachineForm.builder(Jugcraft.id("chemical_infuser"),
			MachineKind.SYNTHESIS_CONVERTER)
			.layer("C#", "##", "##")
			.layer("##", "##", "##")
			.layer("##", "##", "##")
			.profile(OperatingProfile.ENTRY)
			.inputTank("reagent_a")
			.inputTank("reagent_b")
			.outputTank("product_gas")
			.outputTank("process_water")
			.socket("catalyst_bed", net.minecraft.tags.TagKey.create(Registries.ITEM, Jugcraft.id("infuser_catalysts")))
			.waterDrainOutput(1)
			.capability("jugcraft:starter_gas_infusing")
			.upgrades()
			.warmup(40)
			.port("reagent_a_in", FormPort.Kind.FLUID_IN, 0, 0, 0, 2, FormSide.FRONT)
			.port("reagent_b_in", FormPort.Kind.FLUID_IN, 1, 1, 0, 2, FormSide.FRONT)
			.port("product_out", FormPort.Kind.FLUID_OUT, 0, 0, 2, 1, FormSide.BACK)
			.port("water_out", FormPort.Kind.FLUID_OUT, 1, 1, 2, 0, FormSide.BACK)
			.port("power", FormPort.Kind.ENERGY_IN, 0, 0, 0, 0, FormSide.FRONT)
			.build();

	public static final MachineForm CHEMICAL_OXIDIZER_FORM = MachineForm.builder(Jugcraft.id("chemical_oxidizer"),
			MachineKind.CHEMICAL_REACTOR)
			.layer("C#", "##")
			.layer("##", "##")
			.layer("##", "##")
			.profile(OperatingProfile.ENTRY)
			.inputTank("oxidant")
			.outputTank("product_gas")
			.items(1, 0)
			.capability("jugcraft:starter_gas_oxidizing")
			.upgrades()
			.warmup(40)
			.port("oxygen_in", FormPort.Kind.FLUID_IN, 0, 1, 0, 0, FormSide.FRONT)
			.port("feed_in", FormPort.Kind.ITEM_IN, 0, 0, 1, 2, FormSide.BACK)
			.port("gas_out", FormPort.Kind.FLUID_OUT, 0, 1, 1, 2, FormSide.BACK)
			.port("power", FormPort.Kind.ENERGY_IN, 0, 0, 0, 0, FormSide.FRONT)
			.build();

	public static final MachineForm COAL_GASIFIER_FORM = MachineForm.builder(Jugcraft.id("coal_gasifier"),
			MachineKind.CHEMICAL_REACTOR)
			.layer("C###", "####", "####", "####")
			.layer("####", "####", "####", "####")
			.layer("####", "####", "####", "####")
			.layer("####", "####", "####", "####")
			.layer("####", "####", "####", "####")
			.layer("####", "####", "####", "####")
			.profile(OperatingProfile.ENTRY)
			.inputTank("feed_water")
			.outputTank("dirty_gas")
			.items(1, 0)
			.capability("jugcraft:starter_gasification")
			.upgrades()
			.warmup(40)
			.port("water_in", FormPort.Kind.FLUID_IN, 0, 0, 3, 0, FormSide.BACK)
			.port("carbon_in", FormPort.Kind.ITEM_IN, 0, 0, 1, 3, FormSide.LEFT)
			.port("dirty_gas_out", FormPort.Kind.FLUID_OUT, 0, 3, 3, 5, FormSide.BACK)
			.port("power", FormPort.Kind.ENERGY_IN, 0, 0, 0, 0, FormSide.FRONT)
			.build();

	public static final MachineForm GAS_CLEANING_RACK_FORM = MachineForm.builder(Jugcraft.id("gas_cleaning_rack"),
			MachineKind.CHEMICAL_REACTOR)
			.layer("C##", "###", "###", "###", "###")
			.layer("###", "###", "###", "###", "###")
			.layer("###", "###", "###", "###", "###")
			.layer("###", "###", "###", "###", "###")
			.profile(OperatingProfile.ENTRY)
			.inputTank("dirty_gas")
			.inputTank("wash_water")
			.outputTank("carbon_monoxide")
			.outputTank("hydrogen")
			.items(0, 1)
			.capability("jugcraft:starter_gas_cleaning")
			.upgrades()
			.warmup(40)
			.port("dirty_gas_in", FormPort.Kind.FLUID_IN, 0, 2, 4, 3, FormSide.BACK)
			.port("wash_water_in", FormPort.Kind.FLUID_IN, 1, 0, 0, 0, FormSide.LEFT)
			.port("co_out", FormPort.Kind.FLUID_OUT, 0, 0, 0, 2, FormSide.FRONT)
			.port("hydrogen_out", FormPort.Kind.FLUID_OUT, 1, 1, 0, 2, FormSide.FRONT)
			.port("residue_out", FormPort.Kind.ITEM_OUT, 0, 1, 0, 0, FormSide.FRONT)
			.port("power", FormPort.Kind.ENERGY_IN, 0, 2, 0, 0, FormSide.FRONT)
			.build();

	public static final MachineForm ANAEROBIC_DIGESTER_FORM = MachineForm.builder(Jugcraft.id("anaerobic_digester"),
			MachineKind.CHEMICAL_REACTOR)
			.layer("C#####", "######", "######", "######", "######", "######")
			.layer("######", "######", "######", "######", "######", "######")
			.layer("######", "######", "######", "######", "######", "######")
			.layer("######", "######", "######", "######", "######", "######")
			.layer("######", "######", "######", "######", "######", "######")
			.layer("######", "######", "######", "######", "######", "######")
			.profile(OperatingProfile.EXPANDED)
			.inputTank("digestion_water")
			.outputTank("raw_biogas")
			.outputTank("digestate")
			.items(1, 0)
			.capability("jugcraft:anaerobic_digestion")
			.upgrades()
			.warmup(80)
			.port("biomass_in", FormPort.Kind.ITEM_IN, 0, 0, 1, 2, FormSide.LEFT)
			.port("water_in", FormPort.Kind.FLUID_IN, 0, 5, 5, 0, FormSide.BACK)
			.port("biogas_out", FormPort.Kind.FLUID_OUT, 0, 5, 5, 4, FormSide.BACK)
			.port("digestate_out", FormPort.Kind.FLUID_OUT, 1, 0, 5, 0, FormSide.BACK)
			.port("power", FormPort.Kind.ENERGY_IN, 0, 0, 0, 0, FormSide.FRONT)
			.build();
	public static FormMachineBlock ANAEROBIC_DIGESTER;

	public static final MachineForm BIOGAS_CLEANER_FORM = MachineForm.builder(Jugcraft.id("biogas_cleaner"),
			MachineKind.CHEMICAL_REACTOR)
			.layer("C#", "##", "##")
			.layer("##", "##", "##")
			.layer("##", "##", "##")
			.profile(OperatingProfile.ENTRY)
			.inputTank("raw_biogas")
			.inputTank("cleaning_water")
			.outputTank("clean_biogas")
			.outputTank("wash_digestate")
			.capability("jugcraft:biogas_cleaning")
			.upgrades()
			.warmup(40)
			.port("raw_in", FormPort.Kind.FLUID_IN, 0, 1, 2, 2, FormSide.BACK)
			.port("water_in", FormPort.Kind.FLUID_IN, 1, 0, 2, 0, FormSide.BACK)
			.port("clean_out", FormPort.Kind.FLUID_OUT, 0, 1, 0, 2, FormSide.FRONT)
			.port("wash_out", FormPort.Kind.FLUID_OUT, 1, 1, 0, 0, FormSide.FRONT)
			.port("power", FormPort.Kind.ENERGY_IN, 0, 0, 0, 0, FormSide.FRONT)
			.build();
	public static FormMachineBlock BIOGAS_CLEANER;

	public static final MachineForm DIGESTATE_SEPARATOR_FORM = MachineForm.builder(Jugcraft.id("digestate_separator"),
			MachineKind.CHEMICAL_REACTOR)
			.layer("C#", "##", "##")
			.layer("##", "##", "##")
			.layer("##", "##", "##")
			.profile(OperatingProfile.ENTRY)
			.inputTank("digestate_feed")
			.outputTank("recovered_water")
			.items(0, 1)
			.capability("jugcraft:digestate_separation")
			.upgrades()
			.warmup(40)
			.port("slurry_in", FormPort.Kind.FLUID_IN, 0, 0, 2, 1, FormSide.BACK)
			.port("water_out", FormPort.Kind.FLUID_OUT, 0, 1, 2, 0, FormSide.BACK)
			.port("fiber_out", FormPort.Kind.ITEM_OUT, 0, 1, 0, 0, FormSide.FRONT)
			.port("power", FormPort.Kind.ENERGY_IN, 0, 0, 0, 0, FormSide.FRONT)
			.build();
	public static FormMachineBlock DIGESTATE_SEPARATOR;

	public static final MachineForm BIO_GENERATOR_FORM = MachineForm.builder(Jugcraft.id("bio_generator"),
			MachineKind.GAS_TURBINE)
			.layer("C##", "###", "###", "###")
			.layer("###", "###", "###", "###")
			.layer("###", "###", "###", "###")
			.profile(OperatingProfile.ENTRY)
			.inputTank("fuel")
			.fuel(Jugcraft.id("clean_biogas"), 192, 64)
			.fuel(Jugcraft.id("bioethanol"), 192, 64)
			.port("biofuel_in", FormPort.Kind.FLUID_IN, 0, 2, 1, 0, FormSide.RIGHT)
			.port("liquid_fuel_in", FormPort.Kind.FLUID_IN, 0, 2, 3, 0, FormSide.BACK)
			.port("power_out", FormPort.Kind.ENERGY_OUT, 0, 0, 3, 1, FormSide.BACK)
			.build();
	public static FormMachineBlock BIO_GENERATOR;

	public static FormMachineBlock CHEMICAL_INFUSER;
	public static FormMachineBlock CHEMICAL_OXIDIZER;
	public static FormMachineBlock COAL_GASIFIER;
	public static FormMachineBlock GAS_CLEANING_RACK;
	public static FormMachineBlock ELECTROLYTIC_SEPARATOR;
	public static FormMachineBlock GAS_BURNING_GENERATOR;
	public static BlockEntityType<FormMachineBlockEntity> ENTITY;

	public static final MachineForm MASHING_STATION_FORM = MachineForm.builder(Jugcraft.id("mashing_station"),
			MachineKind.CHEMICAL_REACTOR)
			.layer("C##", "###", "###")
			.layer("###", "###", "###")
			.layer("###", "###", "###")
			.profile(OperatingProfile.ENTRY)
			.inputTank("mash_water")
			.outputTank("crop_mash")
			.items(1, 0)
			.capability("jugcraft:crop_mashing")
			.upgrades()
			.warmup(40)
			.port("power", FormPort.Kind.ENERGY_IN, 0, 0, 0, 0, FormSide.FRONT)
			.port("feed_0", FormPort.Kind.FLUID_IN, 0, 0, 2, 0, FormSide.BACK)
			.port("product_0", FormPort.Kind.FLUID_OUT, 0, 1, 2, 0, FormSide.BACK)
			.port("solid_in_0", FormPort.Kind.ITEM_IN, 0, 2, 0, 1, FormSide.FRONT)
			.build();
	public static FormMachineBlock MASHING_STATION;

	public static final MachineForm INDUSTRIAL_FERMENTER_FORM = MachineForm.builder(Jugcraft.id("industrial_fermenter"),
			MachineKind.CHEMICAL_REACTOR)
			.layer("C##", "###", "###")
			.layer("###", "###", "###")
			.layer("###", "###", "###")
			.layer("###", "###", "###")
			.profile(OperatingProfile.ENTRY)
			.inputTank("crop_mash")
			.outputTank("ethanol_broth")
			.outputTank("fermentation_exhaust")
			.items(0, 0)
			.capability("jugcraft:controlled_fermentation")
			.upgrades()
			.warmup(40)
			.gasVentOutput(1, Jugcraft.id("fermentation_exhaust"))
			.port("power", FormPort.Kind.ENERGY_IN, 0, 0, 0, 0, FormSide.FRONT)
			.port("feed_0", FormPort.Kind.FLUID_IN, 0, 2, 2, 0, FormSide.BACK)
			.port("product_0", FormPort.Kind.FLUID_OUT, 0, 0, 2, 0, FormSide.BACK)
			.port("product_1", FormPort.Kind.FLUID_OUT, 1, 1, 2, 3, FormSide.BACK)
			.build();
	public static FormMachineBlock INDUSTRIAL_FERMENTER;

	public static final MachineForm CHEMICAL_DISTILLATION_STATION_FORM = MachineForm.builder(Jugcraft.id("chemical_distillation_station"),
			MachineKind.DISTILLATION_TOWER)
			.layer("C###", "####", "####", "####")
			.layer("####", "####", "####", "####")
			.layer("####", "####", "####", "####")
			.layer("####", "####", "####", "####")
			.layer("####", "####", "####", "####")
			.layer("####", "####", "####", "####")
			.profile(OperatingProfile.ENTRY)
			.inputTank("ethanol_broth")
			.outputTank("distilled_ethanol")
			.outputTank("stillage")
			.items(0, 0)
			.capability("jugcraft:chemical_distillation")
			.upgrades()
			.warmup(40)
			.port("power", FormPort.Kind.ENERGY_IN, 0, 0, 0, 0, FormSide.FRONT)
			.port("feed_0", FormPort.Kind.FLUID_IN, 0, 2, 3, 1, FormSide.BACK)
			.port("product_0", FormPort.Kind.FLUID_OUT, 0, 0, 3, 1, FormSide.BACK)
			.port("product_1", FormPort.Kind.FLUID_OUT, 1, 2, 3, 0, FormSide.BACK)
			.build();
	public static FormMachineBlock CHEMICAL_DISTILLATION_STATION;

	public static final MachineForm BAUXITE_DIGESTER_FORM = MachineForm.builder(Jugcraft.id("bauxite_digester"),
			MachineKind.CHEMICAL_REACTOR)
			.layer("C##", "###", "###", "###")
			.layer("###", "###", "###", "###")
			.layer("###", "###", "###", "###")
			.profile(OperatingProfile.ENTRY)
			.inputTank("digestion_lye")
			.inputTank("digestion_water")
			.outputTank("aluminate_slurry")
			.items(1, 0)
			.capability("jugcraft:bauxite_digestion")
			.upgrades()
			.warmup(40)
			.port("power", FormPort.Kind.ENERGY_IN, 0, 0, 0, 0, FormSide.FRONT)
			.port("feed_0", FormPort.Kind.FLUID_IN, 0, 0, 3, 1, FormSide.BACK)
			.port("feed_1", FormPort.Kind.FLUID_IN, 1, 1, 3, 1, FormSide.BACK)
			.port("product_0", FormPort.Kind.FLUID_OUT, 0, 2, 3, 0, FormSide.BACK)
			.port("solid_in_0", FormPort.Kind.ITEM_IN, 0, 2, 0, 1, FormSide.FRONT)
			.build();
	public static FormMachineBlock BAUXITE_DIGESTER;

	public static final MachineForm ALUMINATE_FILTER_PRESS_FORM = MachineForm.builder(Jugcraft.id("aluminate_filter_press"),
			MachineKind.CHEMICAL_REACTOR)
			.layer("C##", "###", "###", "###")
			.layer("###", "###", "###", "###")
			.layer("###", "###", "###", "###")
			.profile(OperatingProfile.ENTRY)
			.inputTank("aluminate_slurry")
			.outputTank("recovered_lye")
			.outputTank("recovered_water")
			.items(0, 2)
			.capability("jugcraft:aluminate_recovery")
			.upgrades()
			.warmup(40)
			.port("power", FormPort.Kind.ENERGY_IN, 0, 0, 0, 0, FormSide.FRONT)
			.port("feed_0", FormPort.Kind.FLUID_IN, 0, 1, 3, 1, FormSide.BACK)
			.port("product_0", FormPort.Kind.FLUID_OUT, 0, 0, 3, 0, FormSide.BACK)
			.port("product_1", FormPort.Kind.FLUID_OUT, 1, 1, 3, 0, FormSide.BACK)
			.port("solid_out_0", FormPort.Kind.ITEM_OUT, 0, 2, 3, 1, FormSide.BACK)
			.port("solid_out_1", FormPort.Kind.ITEM_OUT, 1, 2, 3, 2, FormSide.BACK)
			.build();
	public static FormMachineBlock ALUMINATE_FILTER_PRESS;

	public static final MachineForm ALUMINA_CALCINER_FORM = MachineForm.builder(Jugcraft.id("alumina_calciner"),
			MachineKind.CHEMICAL_REACTOR)
			.layer("C##", "###", "###", "###")
			.layer("###", "###", "###", "###")
			.layer("###", "###", "###", "###")
			.profile(OperatingProfile.ENTRY)
			.outputTank("recovered_water")
			.items(1, 1)
			.capability("jugcraft:alumina_calcination")
			.upgrades()
			.warmup(40)
			.port("power", FormPort.Kind.ENERGY_IN, 0, 0, 0, 0, FormSide.FRONT)
			.port("product_0", FormPort.Kind.FLUID_OUT, 0, 0, 3, 1, FormSide.BACK)
			.port("solid_in_0", FormPort.Kind.ITEM_IN, 0, 2, 0, 1, FormSide.FRONT)
			.port("solid_out_0", FormPort.Kind.ITEM_OUT, 0, 2, 3, 1, FormSide.BACK)
			.build();
	public static FormMachineBlock ALUMINA_CALCINER;

	public static final MachineForm ALUMINUM_REDUCTION_CELL_FORM = MachineForm.builder(Jugcraft.id("aluminum_reduction_cell"),
			MachineKind.ELECTROLYTIC_CELL)
			.layer("C##", "###", "###")
			.layer("###", "###", "###")
			.layer("###", "###", "###")
			.layer("###", "###", "###")
			.profile(OperatingProfile.ENTRY)
			.outputTank("carbon_dioxide")
			.items(2, 1)
			.capability("jugcraft:aluminum_reduction")
			.upgrades()
			.warmup(40)
			.gasVentOutput(0, Jugcraft.id("carbon_dioxide"))
			.port("power", FormPort.Kind.ENERGY_IN, 0, 0, 0, 0, FormSide.FRONT)
			.port("product_0", FormPort.Kind.FLUID_OUT, 0, 0, 2, 1, FormSide.BACK)
			.port("solid_in_0", FormPort.Kind.ITEM_IN, 0, 2, 0, 1, FormSide.FRONT)
			.port("solid_in_1", FormPort.Kind.ITEM_IN, 1, 2, 0, 2, FormSide.FRONT)
			.port("solid_out_0", FormPort.Kind.ITEM_OUT, 0, 2, 2, 1, FormSide.BACK)
			.build();
	public static FormMachineBlock ALUMINUM_REDUCTION_CELL;

	public static final MachineForm COASTAL_SEAWATER_INTAKE_FORM = MachineForm.builder(Jugcraft.id("coastal_seawater_intake"), MachineKind.CHEMICAL_REACTOR)
			.layer("C##", "###", "###")
			.layer("###", "###", "###")
			.profile(OperatingProfile.ENTRY)
			.outputTank("seawater")
			.items(0, 0)
			.capability("jugcraft:coastal_pumping")
			.warmup(0)
			.port("power", FormPort.Kind.ENERGY_IN, 0, 0, 0, 0, FormSide.FRONT)
			.port("product_0", FormPort.Kind.FLUID_OUT, 0, 0, 2, 0, FormSide.BACK)
			.build();
	public static FormMachineBlock COASTAL_SEAWATER_INTAKE;

	public static final MachineForm BRINE_CONCENTRATOR_FORM = MachineForm.builder(Jugcraft.id("brine_concentrator"), MachineKind.CHEMICAL_REACTOR)
			.layer("C##", "###", "###")
			.layer("###", "###", "###")
			.layer("###", "###", "###")
			.profile(OperatingProfile.ENTRY)
			.inputTank("seawater")
			.outputTank("magnesium_brine")
			.outputTank("recovered_water")
			.items(0, 0)
			.capability("jugcraft:seawater_concentration")
			.upgrades()
			.warmup(40)
			.port("power", FormPort.Kind.ENERGY_IN, 0, 0, 0, 0, FormSide.FRONT)
			.port("feed_0", FormPort.Kind.FLUID_IN, 0, 0, 2, 1, FormSide.BACK)
			.port("product_0", FormPort.Kind.FLUID_OUT, 0, 0, 2, 0, FormSide.BACK)
			.port("product_1", FormPort.Kind.FLUID_OUT, 1, 1, 2, 0, FormSide.BACK)
			.build();
	public static FormMachineBlock BRINE_CONCENTRATOR;

	public static final MachineForm MINERAL_CONDITIONER_FORM = MachineForm.builder(Jugcraft.id("mineral_conditioner"), MachineKind.CHEMICAL_REACTOR)
			.layer("C##", "###", "###")
			.layer("###", "###", "###")
			.layer("###", "###", "###")
			.profile(OperatingProfile.ENTRY)
			.inputTank("mineral_feed")
			.inputTank("conditioning_reagent")
			.outputTank("conditioned_liquid")
			.outputTank("recovered_water")
			.items(1, 1)
			.capability("jugcraft:mineral_conditioning")
			.upgrades()
			.warmup(40)
			.port("power", FormPort.Kind.ENERGY_IN, 0, 0, 0, 0, FormSide.FRONT)
			.port("feed_0", FormPort.Kind.FLUID_IN, 0, 0, 2, 1, FormSide.BACK)
			.port("feed_1", FormPort.Kind.FLUID_IN, 1, 1, 2, 1, FormSide.BACK)
			.port("product_0", FormPort.Kind.FLUID_OUT, 0, 0, 2, 0, FormSide.BACK)
			.port("product_1", FormPort.Kind.FLUID_OUT, 1, 1, 2, 0, FormSide.BACK)
			.port("solid_in_0", FormPort.Kind.ITEM_IN, 0, 2, 0, 1, FormSide.FRONT)
			.port("solid_out_0", FormPort.Kind.ITEM_OUT, 0, 2, 2, 1, FormSide.BACK)
			.build();
	public static FormMachineBlock MINERAL_CONDITIONER;

	public static final MachineForm CHLORIDE_DRYER_FORM = MachineForm.builder(Jugcraft.id("chloride_dryer"), MachineKind.CHEMICAL_REACTOR)
			.layer("C##", "###", "###")
			.layer("###", "###", "###")
			.layer("###", "###", "###")
			.profile(OperatingProfile.ENTRY)
			.inputTank("chloride_solution")
			.outputTank("recovered_water")
			.items(0, 1)
			.capability("jugcraft:chloride_drying")
			.upgrades()
			.warmup(40)
			.port("power", FormPort.Kind.ENERGY_IN, 0, 0, 0, 0, FormSide.FRONT)
			.port("feed_0", FormPort.Kind.FLUID_IN, 0, 0, 2, 1, FormSide.BACK)
			.port("product_0", FormPort.Kind.FLUID_OUT, 0, 0, 2, 0, FormSide.BACK)
			.port("solid_out_0", FormPort.Kind.ITEM_OUT, 0, 2, 2, 1, FormSide.BACK)
			.build();
	public static FormMachineBlock CHLORIDE_DRYER;

	public static final MachineForm MAGNESIUM_ELECTROLYSIS_CELL_FORM = MachineForm.builder(Jugcraft.id("magnesium_electrolysis_cell"), MachineKind.ELECTROLYTIC_CELL)
			.layer("C##", "###", "###")
			.layer("###", "###", "###")
			.layer("###", "###", "###")
			.profile(OperatingProfile.ENTRY)
			.outputTank("chlorine")
			.items(1, 1)
			.capability("jugcraft:magnesium_electrolysis")
			.upgrades()
			.warmup(40)
			.port("power", FormPort.Kind.ENERGY_IN, 0, 0, 0, 0, FormSide.FRONT)
			.port("product_0", FormPort.Kind.FLUID_OUT, 0, 0, 2, 0, FormSide.BACK)
			.port("solid_in_0", FormPort.Kind.ITEM_IN, 0, 2, 0, 1, FormSide.FRONT)
			.port("solid_out_0", FormPort.Kind.ITEM_OUT, 0, 2, 2, 1, FormSide.BACK)
			.build();
	public static FormMachineBlock MAGNESIUM_ELECTROLYSIS_CELL;

	public static final MachineForm TITANIUM_CHLORINATOR_FORM = MachineForm.builder(Jugcraft.id("titanium_chlorinator"), MachineKind.CHEMICAL_REACTOR)
			.layer("C##", "###", "###", "###")
			.layer("###", "###", "###", "###")
			.layer("###", "###", "###", "###")
			.layer("###", "###", "###", "###")
			.profile(OperatingProfile.ENTRY)
			.inputTank("chlorine")
			.outputTank("crude_chloride")
			.outputTank("carbon_dioxide")
			.items(2, 0)
			.capability("jugcraft:titanium_chlorination")
			.upgrades()
			.warmup(40)
			.gasVentOutput(1, Jugcraft.id("carbon_dioxide"))
			.port("power", FormPort.Kind.ENERGY_IN, 0, 0, 0, 0, FormSide.FRONT)
			.port("feed_0", FormPort.Kind.FLUID_IN, 0, 0, 3, 1, FormSide.BACK)
			.port("product_0", FormPort.Kind.FLUID_OUT, 0, 0, 3, 0, FormSide.BACK)
			.port("product_1", FormPort.Kind.FLUID_OUT, 1, 1, 3, 0, FormSide.BACK)
			.port("solid_in_0", FormPort.Kind.ITEM_IN, 0, 2, 0, 1, FormSide.FRONT)
			.port("solid_in_1", FormPort.Kind.ITEM_IN, 1, 2, 0, 2, FormSide.FRONT)
			.build();
	public static FormMachineBlock TITANIUM_CHLORINATOR;

	public static final MachineForm TITANIUM_REDUCTION_RETORT_FORM = MachineForm.builder(Jugcraft.id("titanium_reduction_retort"), MachineKind.ELECTROLYTIC_CELL)
			.layer("C##", "###", "###", "###")
			.layer("###", "###", "###", "###")
			.layer("###", "###", "###", "###")
			.profile(OperatingProfile.ENTRY)
			.inputTank("purified_chloride")
			.items(1, 2)
			.capability("jugcraft:titanium_reduction")
			.upgrades()
			.warmup(40)
			.port("power", FormPort.Kind.ENERGY_IN, 0, 0, 0, 0, FormSide.FRONT)
			.port("feed_0", FormPort.Kind.FLUID_IN, 0, 0, 3, 1, FormSide.BACK)
			.port("solid_in_0", FormPort.Kind.ITEM_IN, 0, 2, 0, 1, FormSide.FRONT)
			.port("solid_out_0", FormPort.Kind.ITEM_OUT, 0, 2, 3, 1, FormSide.BACK)
			.port("solid_out_1", FormPort.Kind.ITEM_OUT, 1, 2, 3, 2, FormSide.BACK)
			.build();
	public static FormMachineBlock TITANIUM_REDUCTION_RETORT;

	public static final MachineForm POLYMER_MOLDING_PRESS_FORM = MachineForm.builder(Jugcraft.id("polymer_molding_press"), MachineKind.CHEMICAL_REACTOR)
			.layer("C##", "###", "###", "###")
			.layer("###", "###", "###", "###")
			.layer("###", "###", "###", "###")
			.profile(OperatingProfile.ENTRY)
			.items(1, 1)
			.capability("jugcraft:polymer_molding")
			.upgrades()
			.warmup(40)
			.socket("mold", net.minecraft.tags.TagKey.create(Registries.ITEM, Jugcraft.id("polymer_molds")))
			.port("power", FormPort.Kind.ENERGY_IN, 0, 0, 0, 0, FormSide.FRONT)
			.port("solid_in_0", FormPort.Kind.ITEM_IN, 0, 2, 0, 1, FormSide.FRONT)
			.port("solid_out_0", FormPort.Kind.ITEM_OUT, 0, 2, 3, 1, FormSide.BACK)
			.build();
	public static FormMachineBlock POLYMER_MOLDING_PRESS;

	private IndustrialForms() {
	}

	public static void register() {
		ELECTROLYTIC_SEPARATOR = block("electrolytic_separator", ELECTROLYTIC_SEPARATOR_FORM);
		GAS_BURNING_GENERATOR = block("gas_burning_generator", GAS_BURNING_GENERATOR_FORM);
		CHEMICAL_INFUSER = block("chemical_infuser", CHEMICAL_INFUSER_FORM);
		CHEMICAL_OXIDIZER = block("chemical_oxidizer", CHEMICAL_OXIDIZER_FORM);
		COAL_GASIFIER = block("coal_gasifier", COAL_GASIFIER_FORM);
		GAS_CLEANING_RACK = block("gas_cleaning_rack", GAS_CLEANING_RACK_FORM);
		ANAEROBIC_DIGESTER = block("anaerobic_digester", ANAEROBIC_DIGESTER_FORM);
		BIOGAS_CLEANER = block("biogas_cleaner", BIOGAS_CLEANER_FORM);
		DIGESTATE_SEPARATOR = block("digestate_separator", DIGESTATE_SEPARATOR_FORM);
		BIO_GENERATOR = block("bio_generator", BIO_GENERATOR_FORM);
		MASHING_STATION = block("mashing_station", MASHING_STATION_FORM);
		INDUSTRIAL_FERMENTER = block("industrial_fermenter", INDUSTRIAL_FERMENTER_FORM);
		CHEMICAL_DISTILLATION_STATION = block("chemical_distillation_station", CHEMICAL_DISTILLATION_STATION_FORM);
		BAUXITE_DIGESTER = block("bauxite_digester", BAUXITE_DIGESTER_FORM);
		ALUMINATE_FILTER_PRESS = block("aluminate_filter_press", ALUMINATE_FILTER_PRESS_FORM);
		ALUMINA_CALCINER = block("alumina_calciner", ALUMINA_CALCINER_FORM);
		ALUMINUM_REDUCTION_CELL = block("aluminum_reduction_cell", ALUMINUM_REDUCTION_CELL_FORM);
		COASTAL_SEAWATER_INTAKE = block("coastal_seawater_intake", COASTAL_SEAWATER_INTAKE_FORM);
		BRINE_CONCENTRATOR = block("brine_concentrator", BRINE_CONCENTRATOR_FORM);
		MINERAL_CONDITIONER = block("mineral_conditioner", MINERAL_CONDITIONER_FORM);
		CHLORIDE_DRYER = block("chloride_dryer", CHLORIDE_DRYER_FORM);
		MAGNESIUM_ELECTROLYSIS_CELL = block("magnesium_electrolysis_cell", MAGNESIUM_ELECTROLYSIS_CELL_FORM);
		TITANIUM_CHLORINATOR = block("titanium_chlorinator", TITANIUM_CHLORINATOR_FORM);
		TITANIUM_REDUCTION_RETORT = block("titanium_reduction_retort", TITANIUM_REDUCTION_RETORT_FORM);
		POLYMER_MOLDING_PRESS = block("polymer_molding_press", POLYMER_MOLDING_PRESS_FORM);
		List<FormMachineBlock> blocks = List.of(ELECTROLYTIC_SEPARATOR, GAS_BURNING_GENERATOR, CHEMICAL_INFUSER, CHEMICAL_OXIDIZER, COAL_GASIFIER, GAS_CLEANING_RACK, ANAEROBIC_DIGESTER, BIOGAS_CLEANER, DIGESTATE_SEPARATOR, BIO_GENERATOR, MASHING_STATION, INDUSTRIAL_FERMENTER, CHEMICAL_DISTILLATION_STATION, BAUXITE_DIGESTER, ALUMINATE_FILTER_PRESS, ALUMINA_CALCINER, ALUMINUM_REDUCTION_CELL, COASTAL_SEAWATER_INTAKE, BRINE_CONCENTRATOR, MINERAL_CONDITIONER, CHLORIDE_DRYER, MAGNESIUM_ELECTROLYSIS_CELL, TITANIUM_CHLORINATOR, TITANIUM_REDUCTION_RETORT, POLYMER_MOLDING_PRESS);
		ENTITY = JugcraftForms.registerBlocks(Jugcraft.id("machine_form"), blocks.toArray(FormMachineBlock[]::new));
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(output -> blocks.forEach(output::accept));
	}

	private static FormMachineBlock block(String path, MachineForm form) {
		// Like the machines: the furnace's light while running, given off by the controller alone although every part
		// lights its lamps and strips, and no hiding of neighbours' faces by a detailed model.
		BlockBehaviour.Properties properties = BlockBehaviour.Properties.ofFullCopy(Blocks.FURNACE).noOcclusion()
				.lightLevel(state -> state.getValue(MachineBlock.LIT) && state.getBlock() instanceof FormMachineBlock machine
						&& machine.part(state) == 0 ? 13 : 0)
				.setId(ResourceKey.create(Registries.BLOCK, Jugcraft.id(path)));
		FormMachineBlock block = Registry.register(BuiltInRegistries.BLOCK, Jugcraft.id(path), new FormMachineBlock(properties, form));
		ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, Jugcraft.id(path));
		Registry.register(BuiltInRegistries.ITEM, itemKey, new BlockItem(block, new Item.Properties().setId(itemKey).useBlockDescriptionPrefix()));
		return block;
	}
}

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

	public static FormMachineBlock CHEMICAL_INFUSER;
	public static FormMachineBlock CHEMICAL_OXIDIZER;
	public static FormMachineBlock COAL_GASIFIER;
	public static FormMachineBlock GAS_CLEANING_RACK;
	public static FormMachineBlock ELECTROLYTIC_SEPARATOR;
	public static FormMachineBlock GAS_BURNING_GENERATOR;
	public static BlockEntityType<FormMachineBlockEntity> ENTITY;

	private IndustrialForms() {
	}

	public static void register() {
		ELECTROLYTIC_SEPARATOR = block("electrolytic_separator", ELECTROLYTIC_SEPARATOR_FORM);
		GAS_BURNING_GENERATOR = block("gas_burning_generator", GAS_BURNING_GENERATOR_FORM);
		CHEMICAL_INFUSER = block("chemical_infuser", CHEMICAL_INFUSER_FORM);
		CHEMICAL_OXIDIZER = block("chemical_oxidizer", CHEMICAL_OXIDIZER_FORM);
		COAL_GASIFIER = block("coal_gasifier", COAL_GASIFIER_FORM);
		GAS_CLEANING_RACK = block("gas_cleaning_rack", GAS_CLEANING_RACK_FORM);
		List<FormMachineBlock> blocks = List.of(ELECTROLYTIC_SEPARATOR, GAS_BURNING_GENERATOR, CHEMICAL_INFUSER, CHEMICAL_OXIDIZER, COAL_GASIFIER, GAS_CLEANING_RACK);
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

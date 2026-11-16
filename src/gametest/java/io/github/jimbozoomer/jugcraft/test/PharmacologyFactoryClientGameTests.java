package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.client.FormMachineScreen;
import io.github.jimbozoomer.jugcraft.energy.SimpleEnergyStorage;
import io.github.jimbozoomer.jugcraft.machine.MachineBlock;
import io.github.jimbozoomer.jugcraft.machine.form.*;
import io.github.jimbozoomer.jugcraft.pharmacology.PharmacyFactory;
import io.github.jimbozoomer.jugcraft.pharmacology.Pharmacology;
import java.util.List;
import java.util.Locale;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

/** Render the complete three-station pharmacy and use the native synchronized production/tool-status screen. */
public class PharmacologyFactoryClientGameTests implements FabricClientGameTest {
	@Override
	public void runTest(ClientGameTestContext context) {
		try (TestSingleplayerContext world = context.worldBuilder()
				.adjustSettings(settings -> settings.setGameMode(WorldCreationUiState.SelectedGameMode.CREATIVE)).create()) {
			world.getConnection().waitForChunksRender();
			var server = world.getServer();
			BlockPos spawn = context.computeOnClient(client -> client.player.blockPosition());
			server.runCommand("gamerule minecraft:send_command_feedback false");
			server.runCommand("time set noon");
			server.runCommand("weather clear");
			server.runCommand("gamerule minecraft:spawn_mobs false");
			server.runCommand("fill %d %d %d %d %d %d minecraft:air".formatted(spawn.getX() - 8, spawn.getY(), spawn.getZ() - 10,
					spawn.getX() + 25, spawn.getY() + 7, spawn.getZ() + 10));
			server.runCommand("fill %d %d %d %d %d %d minecraft:smooth_stone".formatted(spawn.getX() - 8, spawn.getY() - 2, spawn.getZ() - 10,
					spawn.getX() + 25, spawn.getY() - 1, spawn.getZ() + 10));
			List<FormMachineBlock> blocks = List.of(IndustrialForms.LABORATORY_ANALYZER, IndustrialForms.PHARMACEUTICAL_SEPARATOR, IndustrialForms.FORMULATION_PRESS);
			List<String> recipes = List.of("pharmacy_analyze_caffeine", "pharmacy_coffee_extract", "pharmacy_batch_caffeine");
			for (int i = 0; i < blocks.size(); i++) {
				FormMachineBlock block = blocks.get(i);
				BlockPos pos = spawn.offset(i * 6, 0, -4);
				server.runOnServer(minecraft -> {
					var level = minecraft.overworld();
					var state = block.defaultBlockState().setValue(MachineBlock.FACING, Direction.SOUTH);
					level.setBlock(pos, state, Block.UPDATE_ALL);
					block.setPlacedBy(level, pos, state, null, ItemStack.EMPTY);
					machine(level, pos).togglePause();
				});
			}
			server.runOnServer(minecraft -> {
				var player = minecraft.getPlayerList().getPlayers().getFirst();
				player.getAbilities().flying = true;
				player.onUpdateAbilities();
			});
			context.runOnClient(client -> { if (!client.gui.hud.isHidden()) client.gui.hud.toggle(); });
			for (int i = 0; i < blocks.size(); i++) {
				BlockPos pos = spawn.offset(i * 6, 0, -4);
				String recipe = recipes.get(i);
				server.runOnServer(minecraft -> {
					var machine = machine(minecraft.overworld(), pos);
					stock(machine, recipe);
					machine.togglePause();
				});
				double cx = pos.getX() + blocks.get(i).form().width() / 2.0;
				double cz = pos.getZ() + 1 - blocks.get(i).form().depth() / 2.0;
				double cy = pos.getY() + blocks.get(i).form().height() / 2.0;
				shoot(context, world, cx - 3.8, pos.getY() + 1, cz + 5, cx, cy, cz, "pharmacy_factory_" + blocks.get(i).form().id().getPath() + "_front");
				shoot(context, world, cx + 3.8, pos.getY() + 1, cz - 5, cx, cy, cz, "pharmacy_factory_" + blocks.get(i).form().id().getPath() + "_back");
				server.runOnServer(minecraft -> machine(minecraft.overworld(), pos).togglePause());
			}
			context.runOnClient(client -> { if (client.gui.hud.isHidden()) client.gui.hud.toggle(); });
			BlockPos press = spawn.offset(12, 0, -4);
			server.runCommand("tp @p %d %d %d".formatted(press.getX(), press.getY(), press.getZ() + 3));
			server.runOnServer(minecraft -> {
				var machine = machine(minecraft.overworld(), press);
				machine.cancel();
				stock(machine, "pharmacy_batch_caffeine");
				if (machine.paused()) machine.togglePause();
				minecraft.getPlayerList().getPlayers().getFirst().openMenu(machine);
			});
			context.waitForScreen(FormMachineScreen.class);
			context.waitTicks(5);
			context.takeScreenshot("pharmacy_factory_press_screen");
			context.waitTicks(170);
			server.runOnServer(minecraft -> {
				var machine = machine(minecraft.overworld(), press);
				ItemStack result = machine.container().getItem(machine.form().firstOutputSlot());
				if (!result.is(Pharmacology.CAFFEINE_DRINK) || result.getCount() != 8
						|| machine.container().getItem(machine.form().firstSocketSlot()).getCount() != 1) {
					throw new AssertionError("Rendered pharmacy did not finish one native eight-drink batch and keep its formula");
				}
				stock(machine, "pharmacy_batch_caffeine");
				machine.container().setItem(machine.form().firstSocketSlot(), new ItemStack(PharmacyFactory.item("paracetamol_formula_plate")));
			});
			context.waitTicks(5);
			context.runOnClient(client -> {
				if (!(client.player.containerMenu instanceof FormMachineMenu menu) || menu.status().state() != MachineLifecycle.WAITING_TOOL) {
					throw new AssertionError("The client did not receive the wrong-formula production status");
				}
			});
			context.takeScreenshot("pharmacy_factory_wrong_formula");
			context.setScreen(() -> null);
		}
	}

	private static FormMachineBlockEntity machine(ServerLevel level, BlockPos pos) {
		return (FormMachineBlockEntity) level.getBlockEntity(pos);
	}

	private static void stock(FormMachineBlockEntity machine, String name) {
		if (machine.lane(0) != null) throw new AssertionError("Fixture must cancel before restocking running work");
		for (int slot = 0; slot < machine.container().getContainerSize(); slot++) machine.container().setItem(slot, ItemStack.EMPTY);
		for (int tank = 0; tank < machine.form().tanks().inputTanks().size(); tank++) machine.tanks().input(tank).drain(machine.tanks().input(tank).millibuckets());
		var entry = machine.recipes(machine.getLevel().getServer()).stream().filter(recipe -> recipe.id().getPath().endsWith('/' + name)).findFirst().orElseThrow();
		machine.selectRecipe(entry.id());
		var recipe = entry.recipe();
		for (int i = 0; i < recipe.items().size(); i++) machine.container().setItem(i, new ItemStack(recipe.items().get(i).ingredient().items().findFirst().orElseThrow(), recipe.items().get(i).count()));
		for (int i = 0; i < recipe.fluids().size(); i++) machine.tanks().input(i).fill(recipe.fluids().get(i).fluid(), recipe.fluids().get(i).amount());
		recipe.tool().ifPresent(tool -> machine.container().setItem(machine.form().firstSocketSlot(), new ItemStack(tool.items().findFirst().orElseThrow())));
		((SimpleEnergyStorage) machine.energy()).setAmount(machine.energy().getCapacity());
	}

	private static void shoot(ClientGameTestContext context, TestSingleplayerContext world, double x, int y, double z,
			double targetX, double targetY, double targetZ, String name) {
		world.getServer().runCommand(String.format(Locale.ROOT, "tp @p %.2f %d %.2f facing %.2f %.2f %.2f", x, y, z, targetX, targetY - 1.62, targetZ));
		context.waitTicks(15);
		world.getConnection().waitForChunksRender();
		context.takeScreenshot(name);
	}
}

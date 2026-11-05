package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.client.FormMachineScreen;
import io.github.jimbozoomer.jugcraft.energy.SimpleEnergyStorage;
import io.github.jimbozoomer.jugcraft.machine.MachineBlock;
import io.github.jimbozoomer.jugcraft.machine.form.*;
import io.github.jimbozoomer.jugcraft.pharmacology.SterilePharmacy;
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

/** Render both new forms and complete a real synchronized sterile filling batch. */
public class PharmacologySterileClientGameTests implements FabricClientGameTest {
	@Override public void runTest(ClientGameTestContext context) {
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
					spawn.getX() + 18, spawn.getY() + 7, spawn.getZ() + 10));
			server.runCommand("fill %d %d %d %d %d %d minecraft:smooth_stone".formatted(spawn.getX() - 8, spawn.getY() - 2, spawn.getZ() - 10,
					spawn.getX() + 18, spawn.getY() - 1, spawn.getZ() + 10));
			List<FormMachineBlock> blocks = List.of(IndustrialForms.AUTOCLAVE, IndustrialForms.STERILE_FILLER);
			List<String> recipes = List.of("sterile_vials", "sterile_fill_penicillin");
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
					stock(machine(minecraft.overworld(), pos), recipe);
					machine(minecraft.overworld(), pos).togglePause();
				});
				double cx = pos.getX() + blocks.get(i).form().width() / 2.0;
				double cz = pos.getZ() + 1 - blocks.get(i).form().depth() / 2.0;
				double cy = pos.getY() + blocks.get(i).form().height() / 2.0;
				shoot(context, world, cx - 3.8, pos.getY() + 1, cz + 5, cx, cy, cz, "sterile_" + blocks.get(i).form().id().getPath() + "_front");
				shoot(context, world, cx + 3.8, pos.getY() + 1, cz - 5, cx, cy, cz, "sterile_" + blocks.get(i).form().id().getPath() + "_back");
				server.runOnServer(minecraft -> machine(minecraft.overworld(), pos).togglePause());
			}
			context.runOnClient(client -> { if (client.gui.hud.isHidden()) client.gui.hud.toggle(); });
			BlockPos filler = spawn.offset(6, 0, -4);
			server.runCommand("tp @p %d %d %d".formatted(filler.getX(), filler.getY(), filler.getZ() + 3));
			server.runOnServer(minecraft -> {
				var machine = machine(minecraft.overworld(), filler);
				machine.cancel();
				stock(machine, "sterile_fill_penicillin");
				if (machine.paused()) machine.togglePause();
				minecraft.getPlayerList().getPlayers().getFirst().openMenu(machine);
			});
			context.waitForScreen(FormMachineScreen.class);
			context.waitTicks(5);
			context.takeScreenshot("sterile_filler_screen");
			context.waitTicks(210);
			server.runOnServer(minecraft -> {
				var machine = machine(minecraft.overworld(), filler);
				var result = machine.container().getItem(machine.form().firstOutputSlot());
				if (!result.is(SterilePharmacy.item("penicillin")) || result.getCount() != 4
						|| machine.container().getItem(machine.form().firstSocketSlot()).getCount() != 1) {
					throw new AssertionError("Rendered sterile filler did not finish exactly four vials and retain its formula");
				}
				stock(machine, "sterile_fill_penicillin");
				machine.container().setItem(machine.form().firstSocketSlot(), new ItemStack(SterilePharmacy.item("morphine_formula_plate")));
			});
			context.waitTicks(5);
			context.runOnClient(client -> {
				if (!(client.player.containerMenu instanceof FormMachineMenu menu) || menu.status().state() != MachineLifecycle.WAITING_TOOL) {
					throw new AssertionError("Wrong sterile formula status did not reach the client");
				}
			});
			context.takeScreenshot("sterile_filler_wrong_formula");
			context.setScreen(() -> null);
		}
	}
	private static FormMachineBlockEntity machine(ServerLevel level, BlockPos pos) { return (FormMachineBlockEntity) level.getBlockEntity(pos); }
	private static void stock(FormMachineBlockEntity machine, String name) {
		for (int slot = 0; slot < machine.form().firstUpgradeSlot(); slot++) machine.container().setItem(slot, ItemStack.EMPTY);
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

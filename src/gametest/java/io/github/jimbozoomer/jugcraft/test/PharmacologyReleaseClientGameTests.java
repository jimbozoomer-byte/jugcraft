package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.client.FormMachineScreen;
import io.github.jimbozoomer.jugcraft.energy.SimpleEnergyStorage;
import io.github.jimbozoomer.jugcraft.machine.MachineBlock;
import io.github.jimbozoomer.jugcraft.machine.form.*;
import io.github.jimbozoomer.jugcraft.pharmacology.*;
import java.util.Locale;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

/** Actual world-ticked release coating, synchronized reusable process tooling and both new late-game branches. */
public class PharmacologyReleaseClientGameTests implements FabricClientGameTest {
	@Override public void runTest(ClientGameTestContext context) {
		try (TestSingleplayerContext world = context.worldBuilder()
				.adjustSettings(settings -> settings.setGameMode(WorldCreationUiState.SelectedGameMode.CREATIVE)).create()) {
			world.getConnection().waitForChunksRender(); var server = world.getServer();
			BlockPos spawn = context.computeOnClient(client -> client.player.blockPosition());
			server.runCommand("gamerule minecraft:send_command_feedback false"); server.runCommand("gamerule minecraft:spawn_mobs false");
			server.runCommand("time set noon"); server.runCommand("weather clear");
			server.runCommand("fill %d %d %d %d %d %d minecraft:air".formatted(spawn.getX() - 10, spawn.getY(), spawn.getZ() - 12, spawn.getX() + 22, spawn.getY() + 8, spawn.getZ() + 12));
			server.runCommand("fill %d %d %d %d %d %d minecraft:smooth_stone".formatted(spawn.getX() - 10, spawn.getY() - 2, spawn.getZ() - 12, spawn.getX() + 22, spawn.getY() - 1, spawn.getZ() + 12));
			BlockPos coater = spawn.offset(0, 0, -5), press = spawn.offset(7, 0, -5), barrel = spawn.offset(13, 0, -5);
			server.runOnServer(minecraft -> {
				place(minecraft.overworld(), coater, IndustrialForms.RELEASE_COATER);
				place(minecraft.overworld(), press, IndustrialForms.FORMULATION_PRESS);
				place(minecraft.overworld(), barrel, IndustrialForms.MASH_CONDITIONING_BARREL);
				var player = minecraft.getPlayerList().getPlayers().getFirst(); player.getAbilities().flying = true; player.onUpdateAbilities();
				var machine = machine(minecraft.overworld(), coater); stock(machine, "release_coat_caffeine_xr"); machine.togglePause();
			});
			context.runOnClient(client -> { if (!client.gui.hud.isHidden()) client.gui.hud.toggle(); });
			double cx = coater.getX() + 1.5, cz = coater.getZ() - .5, cy = coater.getY() + 1.5;
			shoot(context, world, cx - 5.5, coater.getY() + 2, cz + 7, cx, cy, cz, "release_coater_front");
			shoot(context, world, cx + 5.5, coater.getY() + 2, cz - 7, cx, cy, cz, "release_coater_back");
			context.runOnClient(client -> { if (client.gui.hud.isHidden()) client.gui.hud.toggle(); });
			server.runCommand("tp @p %d %d %d".formatted(coater.getX(), coater.getY(), coater.getZ() + 4));
			server.runOnServer(minecraft -> {
				var machine = machine(minecraft.overworld(), coater); machine.cancel(); stock(machine, "release_coat_caffeine_xr");
				machine.container().setItem(machine.form().firstSocketSlot(), new ItemStack(PrecisionPharmacy.item("precision_process_module")));
				minecraft.getPlayerList().getPlayers().getFirst().openMenu(machine);
			});
			context.waitForScreen(FormMachineScreen.class); context.waitTicks(5);
			context.runOnClient(client -> {
				if (!(client.player.containerMenu instanceof FormMachineMenu menu) || menu.status().state() != MachineLifecycle.WAITING_TOOL)
					throw new AssertionError("A precision module must not substitute for the release die");
			});
			context.takeScreenshot("release_coater_wrong_tool");
			server.runOnServer(minecraft -> {
				var machine = machine(minecraft.overworld(), coater);
				machine.container().setItem(machine.form().firstSocketSlot(), new ItemStack(ControlledRelease.item("release_control_die")));
			});
			context.waitTicks(10); context.takeScreenshot("release_coater_processing"); context.waitTicks(190);
			// This advanced batch exceeds one buffer: replenish its existing electrical store, then let native ticks finish it.
			server.runOnServer(minecraft -> { var machine = machine(minecraft.overworld(), coater); ((SimpleEnergyStorage) machine.energy()).setAmount(machine.energy().getCapacity()); });
			context.waitTicks(180);
			server.runOnServer(minecraft -> {
				var machine = machine(minecraft.overworld(), coater); var result = machine.container().getItem(machine.form().firstOutputSlot());
				if (!result.is(ControlledRelease.item("caffeine_xr_core")) || result.getCount() != 4 || machine.container().getItem(machine.form().firstSocketSlot()).getCount() != 1)
					throw new AssertionError("World-ticked coating must make exactly four cores and retain the die");
			});
			context.setScreen(() -> null);
			server.runCommand("tp @p %d %d %d".formatted(press.getX(), press.getY(), press.getZ() + 3));
			server.runOnServer(minecraft -> {
				var machine = machine(minecraft.overworld(), press); stock(machine, "release_capsule_shells"); machine.togglePause();
				minecraft.getPlayerList().getPlayers().getFirst().openMenu(machine);
			});
			context.waitForScreen(FormMachineScreen.class); context.waitTicks(5);
			context.runOnClient(client -> {
				var menu = (FormMachineMenu) client.player.containerMenu;
				if (menu.getSlot(menu.form().firstUpgradeSlot()).mayPickup(client.player)) throw new AssertionError("The Press process-tool lock must reach the client");
			});
			context.takeScreenshot("release_press_process_tool"); context.waitTicks(250);
			server.runOnServer(minecraft -> {
				var machine = machine(minecraft.overworld(), press);
				if (!machine.container().getItem(machine.form().firstOutputSlot()).is(ControlledRelease.item("empty_pharmacy_capsule"))
						|| machine.container().getItem(machine.form().firstOutputSlot()).getCount() != 8
						|| machine.container().getItem(machine.form().firstUpgradeSlot()).getCount() != 1) throw new AssertionError("Native Press must produce eight capsule shells and keep its die");
			});
			context.setScreen(() -> null);
			server.runCommand("tp @p %d %d %d".formatted(barrel.getX(), barrel.getY(), barrel.getZ() + 3));
			server.runOnServer(minecraft -> {
				var machine = machine(minecraft.overworld(), barrel); stock(machine, "release_masterwork_conditioning"); machine.togglePause();
				minecraft.getPlayerList().getPlayers().getFirst().openMenu(machine);
			});
			context.waitForScreen(FormMachineScreen.class); context.waitTicks(5); context.takeScreenshot("release_masterwork_conditioning");
			context.setScreen(() -> null);
			server.runOnServer(minecraft -> {
				var player = minecraft.getPlayerList().getPlayers().getFirst();
				player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ControlledRelease.item("fentanyl_patch")));
				Pharmacology.drink(player, ControlledRelease.FENTANYL_PATCH);
				for (int i = 0; i < 30; i++) Pharmacology.tickSecond(player);
			});
			context.waitTicks(5);
			context.runOnClient(client -> {
				if (!client.player.hasEffect(ControlledRelease.FENTANYL_PATCH.marker()) || client.player.getEffect(SterilePharmacy.DROWSINESS).getAmplifier() != 1)
					throw new AssertionError("Patch and its lower Drowsiness level must synchronize");
			});
			context.takeScreenshot("release_patch_effects");
			server.runOnServer(minecraft -> {
				var player = minecraft.getPlayerList().getPlayers().getFirst();
				new ItemStack(SterilePharmacy.item("naloxone")).finishUsingItem(player.level(), player);
				for (int i = 0; i < 180; i++) Pharmacology.tickSecond(player);
				player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ControlledRelease.item("masterwork_reserve")));
				if (!Pharmacology.drink(player, ControlledRelease.MASTERWORK_RESERVE)) throw new AssertionError("Masterwork should become usable after opioid recovery");
				for (int i = 0; i < 20; i++) Pharmacology.tickSecond(player);
			});
			context.waitTicks(5);
			context.runOnClient(client -> {
				if (!client.player.hasEffect(ControlledRelease.MASTERWORK_RESERVE.marker()) || client.player.getEffect(PrecisionPharmacy.INTOXICATION).getAmplifier() != 5)
					throw new AssertionError("Fifth-grade alcohol and its existing Intoxication state must synchronize");
			});
			context.takeScreenshot("release_masterwork_effects");
		}
	}
	private static FormMachineBlockEntity machine(ServerLevel level, BlockPos pos) { return (FormMachineBlockEntity) level.getBlockEntity(pos); }
	private static void place(ServerLevel level, BlockPos pos, FormMachineBlock block) {
		var state = block.defaultBlockState().setValue(MachineBlock.FACING, Direction.SOUTH);
		level.setBlock(pos, state, Block.UPDATE_ALL); block.setPlacedBy(level, pos, state, null, ItemStack.EMPTY); machine(level, pos).togglePause();
	}
	private static void stock(FormMachineBlockEntity machine, String name) {
		for (int i = 0; i < machine.form().containerSize(); i++) machine.container().setItem(i, ItemStack.EMPTY);
		for (int i = 0; i < machine.form().tanks().inputTanks().size(); i++) machine.tanks().input(i).drain(machine.tanks().input(i).millibuckets());
		var recipe = machine.recipes(machine.getLevel().getServer()).stream().filter(entry -> entry.id().getPath().endsWith('/' + name)).findFirst().orElseThrow().recipe();
		PharmacologyFactoryGameTests.feed(machine, recipe); ((SimpleEnergyStorage) machine.energy()).setAmount(machine.energy().getCapacity());
	}
	private static void shoot(ClientGameTestContext context, TestSingleplayerContext world, double x, int y, double z, double tx, double ty, double tz, String name) {
		world.getServer().runCommand(String.format(Locale.ROOT, "tp @p %.2f %d %.2f facing %.2f %.2f %.2f", x, y, z, tx, ty - 1.62, tz));
		context.waitTicks(15); world.getConnection().waitForChunksRender(); context.takeScreenshot(name);
	}
}

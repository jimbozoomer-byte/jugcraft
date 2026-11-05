package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.client.EchoViewClient;
import io.github.jimbozoomer.jugcraft.client.FormMachineScreen;
import io.github.jimbozoomer.jugcraft.machine.MachineBlock;
import io.github.jimbozoomer.jugcraft.machine.form.*;
import io.github.jimbozoomer.jugcraft.pharmacology.*;
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
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

/** Render the early workshop, run its actual fuel job, then exercise the synchronized remote camera. */
public class PharmacologyPrecisionClientGameTests implements FabricClientGameTest {
	@Override public void runTest(ClientGameTestContext context) {
		try (TestSingleplayerContext world = context.worldBuilder()
				.adjustSettings(settings -> settings.setGameMode(WorldCreationUiState.SelectedGameMode.CREATIVE)).create()) {
			world.getConnection().waitForChunksRender();
			var server = world.getServer();
			BlockPos spawn = context.computeOnClient(client -> client.player.blockPosition());
			server.runCommand("gamerule minecraft:send_command_feedback false");
			server.runCommand("time set noon"); server.runCommand("weather clear");
			server.runCommand("gamerule minecraft:spawn_mobs false");
			server.runCommand("fill %d %d %d %d %d %d minecraft:air".formatted(spawn.getX() - 14, spawn.getY(), spawn.getZ() - 14,
					spawn.getX() + 18, spawn.getY() + 8, spawn.getZ() + 14));
			server.runCommand("fill %d %d %d %d %d %d minecraft:smooth_stone".formatted(spawn.getX() - 14, spawn.getY() - 2, spawn.getZ() - 14,
					spawn.getX() + 18, spawn.getY() - 1, spawn.getZ() + 14));
			List<FormMachineBlock> forms = List.of(IndustrialForms.MASH_CONDITIONING_BARREL, IndustrialForms.MOONSHINE_STILL);
			for (int i = 0; i < forms.size(); i++) {
				var block = forms.get(i); var pos = spawn.offset(i * 6, 0, -4);
				server.runOnServer(minecraft -> {
					var level = minecraft.overworld();
					var state = block.defaultBlockState().setValue(MachineBlock.FACING, Direction.SOUTH);
					level.setBlock(pos, state, Block.UPDATE_ALL); block.setPlacedBy(level, pos, state, null, ItemStack.EMPTY);
					machine(level, pos).togglePause();
				});
			}
			server.runOnServer(minecraft -> {
				var player = minecraft.getPlayerList().getPlayers().getFirst();
				player.getAbilities().flying = true; player.onUpdateAbilities();
			});
			context.runOnClient(client -> { if (!client.gui.hud.isHidden()) client.gui.hud.toggle(); });
			for (int i = 0; i < forms.size(); i++) {
				BlockPos pos = spawn.offset(i * 6, 0, -4);
				double cx = pos.getX() + 1, cz = pos.getZ(), cy = pos.getY() + 1;
				String id = forms.get(i).form().id().getPath();
				shoot(context, world, cx - 3.8, pos.getY() + 1, cz + 5, cx, cy, cz, "precision_" + id + "_front");
				shoot(context, world, cx + 3.8, pos.getY() + 1, cz - 5, cx, cy, cz, "precision_" + id + "_back");
			}
			context.runOnClient(client -> { if (client.gui.hud.isHidden()) client.gui.hud.toggle(); });
			BlockPos still = spawn.offset(6, 0, -4);
			server.runCommand("tp @p %d %d %d".formatted(still.getX(), still.getY(), still.getZ() + 3));
			server.runOnServer(minecraft -> {
				var machine = machine(minecraft.overworld(), still);
				var entry = machine.recipes(minecraft).stream().filter(r -> r.id().getPath().endsWith("/beverage_moonshine")).findFirst().orElseThrow();
				machine.selectRecipe(entry.id());
				machine.container().setItem(0, new ItemStack(PrecisionPharmacy.item("fermented_mash_jug")));
				machine.container().setItem(1, new ItemStack(Items.GLASS_BOTTLE, 4));
				if (machine.paused()) machine.togglePause();
				minecraft.getPlayerList().getPlayers().getFirst().openMenu(machine);
			});
			context.waitForScreen(FormMachineScreen.class); context.waitTicks(5);
			context.runOnClient(client -> {
				if (!(client.player.containerMenu instanceof FormMachineMenu menu) || menu.status().state() != MachineLifecycle.WAITING_ENERGY)
					throw new AssertionError("The still must wait for furnace fuel");
			});
			context.takeScreenshot("precision_still_waiting_fuel");
			server.runOnServer(minecraft -> machine(minecraft.overworld(), still).container().setItem(2, new ItemStack(Items.COAL)));
			context.waitTicks(10); context.takeScreenshot("precision_still_working"); context.waitTicks(200);
			server.runOnServer(minecraft -> {
				var machine = machine(minecraft.overworld(), still);
				var output = machine.container().getItem(machine.form().firstOutputSlot());
				if (!output.is(PrecisionPharmacy.item("moonshine")) || output.getCount() != 4
						|| !machine.container().getItem(machine.form().firstOutputSlot() + 1).is(PrecisionPharmacy.item("empty_mash_jug")))
					throw new AssertionError("The rendered still must complete four bottles and return one jug");
			});
			context.setScreen(() -> null); context.waitTicks(5);
			server.runCommand("tp @p %d %d %d 90 0".formatted(spawn.getX(), spawn.getY() + 1, spawn.getZ() + 6));
			context.waitTicks(5);
			context.runOnClient(client -> {
				var eye = client.player.getEyePosition();
				io.github.jimbozoomer.jugcraft.client.NouveauScryingClient.showView(eye);
				if (client.getCameraEntity().getEyePosition(0).distanceToSqr(eye) > .0001)
					throw new AssertionError("The first interpolated view frame must start at the real eye, without a ceiling peek");
				io.github.jimbozoomer.jugcraft.client.NouveauScryingClient.clearView(client);
			});
			var body = context.computeOnClient(client -> client.player.position());
			server.runOnServer(minecraft -> {
				var player = minecraft.getPlayerList().getPlayers().getFirst();
				player.getInventory().setItem(0, new ItemStack(PrecisionPharmacy.item("beverage_culture_pack"), 4));
				if (!Pharmacology.drink(player, PrecisionPharmacy.KETAMINE)) throw new AssertionError("Ketamine should start from an idle inventory");
			});
			context.waitTicks(5);
			context.runOnClient(client -> {
				if (!EchoViewClient.active() || client.getCameraEntity() == client.player) throw new AssertionError("Echo camera must detach from the real body");
			});
			context.getInput().holdKey(options -> options.keyDrop); context.waitTicks(1); context.getInput().releaseKey(options -> options.keyDrop); context.waitTicks(5);
			context.runOnClient(client -> {
				if (client.player.getMainHandItem().getCount() != 4) throw new AssertionError("Rejected drop input must restore the client's predicted inventory immediately");
			});
			var view = context.computeOnClient(client -> client.getCameraEntity().position());
			context.getInput().holdKey(options -> options.keyUp); context.waitTicks(25); context.getInput().releaseKey(options -> options.keyUp);
			context.runOnClient(client -> {
				if (client.getCameraEntity().position().distanceToSqr(view) < 1 || client.player.position().distanceToSqr(body) > .01)
					throw new AssertionError("Echo input must move only the viewport");
			});
			var beforeRight = context.computeOnClient(client -> client.getCameraEntity().position());
			context.getInput().holdKey(options -> options.keyRight); context.waitTicks(10); context.getInput().releaseKey(options -> options.keyRight); context.waitTicks(3);
			context.runOnClient(client -> {
				if (client.getCameraEntity().position().z > beforeRight.z - .5 || client.player.position().distanceToSqr(body) > .01)
					throw new AssertionError("Right strafe must move the viewport north when facing west, without moving the body");
			});
			context.takeScreenshot("precision_echo_view");
			context.getInput().holdKey(options -> options.keyShift); context.waitTicks(5); context.getInput().releaseKey(options -> options.keyShift);
			context.runOnClient(client -> {
				if (EchoViewClient.active() || client.getCameraEntity() != client.player) throw new AssertionError("Sneak must restore the player camera");
			});
			server.runOnServer(minecraft -> {
				var player = minecraft.getPlayerList().getPlayers().getFirst();
				if (EchoView.active(player) || Pharmacology.strain(player) != 40 || player.getMainHandItem().getCount() != 4) throw new AssertionError("Return must keep drug accounting and inventory");
				Pharmacology.drink(player, (DrugDrinkItem.class.cast(PrecisionPharmacy.item("methylphenidate"))).profile());
				for (int i = 0; i < 20; i++) Pharmacology.tickSecond(player);
			});
			server.runCommand("item replace entity @p weapon.mainhand with jugcraft:garrison_rifle");
			context.waitTicks(5); context.getInput().holdKey(options -> options.keyUse); context.waitTicks(10);
			context.runOnClient(client -> {
				if (Math.abs(PrecisionPharmacy.aimSwayMultiplier(client.player) - .75F) > .001
						|| io.github.jimbozoomer.jugcraft.client.guns.GunView.aim(1) < .99F) throw new AssertionError("Precision focus must synchronize while the supported gun renderer is actually aimed");
			});
			context.takeScreenshot("precision_focused_aim"); context.getInput().releaseKey(options -> options.keyUse);
		}
	}
	private static FormMachineBlockEntity machine(ServerLevel level, BlockPos pos) { return (FormMachineBlockEntity) level.getBlockEntity(pos); }
	private static void shoot(ClientGameTestContext context, TestSingleplayerContext world, double x, int y, double z,
			double tx, double ty, double tz, String name) {
		world.getServer().runCommand(String.format(Locale.ROOT, "tp @p %.2f %d %.2f facing %.2f %.2f %.2f", x, y, z, tx, ty - 1.62, tz));
		context.waitTicks(15); world.getConnection().waitForChunksRender(); context.takeScreenshot(name);
	}
}

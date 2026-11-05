package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.pharmacology.Pharmacology;
import io.github.jimbozoomer.jugcraft.weapons.FieldChemistry;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Renders the full early pharmacy, independent buffs, milk recovery and selective charcoal cleanup. */
public class PharmacologyClientGameTests implements FabricClientGameTest {
	@Override
	public void runTest(ClientGameTestContext context) {
		try (TestSingleplayerContext world = context.worldBuilder()
				.adjustSettings(settings -> settings.setGameMode(WorldCreationUiState.SelectedGameMode.SURVIVAL)).create()) {
			world.getConnection().waitForChunksRender();
			var server = world.getServer();
			BlockPos origin = context.computeOnClient(client -> client.player.blockPosition());
			server.runCommand("time set noon");
			server.runCommand("weather clear");
			server.runCommand("fill %d %d %d %d %d %d minecraft:stone".formatted(origin.getX() - 3, origin.getY() - 1,
					origin.getZ() - 3, origin.getX() + 3, origin.getY() - 1, origin.getZ() + 3));
			server.runOnServer(minecraft -> {
				var player = minecraft.getPlayerList().getPlayers().getFirst();
				player.getAbilities().invulnerable = true;
				player.getInventory().setItem(0, new ItemStack(Pharmacology.CAFFEINE_DRINK, 4));
				player.getInventory().setItem(1, new ItemStack(FieldChemistry.ASPIRIN, 4));
				player.getInventory().setItem(2, new ItemStack(Pharmacology.PARACETAMOL_TABLET, 4));
				player.getInventory().setItem(3, new ItemStack(Pharmacology.REHYDRATION_DRINK, 4));
				player.getInventory().setItem(4, new ItemStack(Pharmacology.ACTIVATED_CHARCOAL, 4));
				player.getInventory().setItem(5, new ItemStack(FieldChemistry.STIMULANT, 4));
				player.getInventory().setItem(6, new ItemStack(Pharmacology.PHARMACEUTICAL_STARCH, 8));
				player.getInventory().setItem(7, new ItemStack(Pharmacology.CAFFEINE_CONCENTRATE, 8));
				player.getInventory().setItem(8, new ItemStack(BuiltInRegistries.ITEM.getValue(Jugcraft.id("coffee_beans")), 8));
				for (int slot : new int[] {1, 2, 3}) {
					ItemStack medicine = player.getInventory().getItem(slot);
					player.getInventory().setItem(slot, medicine.finishUsingItem(player.level(), player));
				}
				ItemStack drink = player.getMainHandItem();
				player.setItemInHand(InteractionHand.MAIN_HAND, drink.finishUsingItem(player.level(), player));
			});
			context.waitTicks(225);
			context.runOnClient(client -> {
				if (!client.player.hasEffect(Pharmacology.CAFFEINATED) || !client.player.hasEffect(MobEffects.HASTE)
						|| !client.player.hasEffect(Pharmacology.FORTITUDE) || !client.player.hasEffect(Pharmacology.RECOVERY)
						|| !client.player.hasEffect(Pharmacology.ENDURANCE)) {
					throw new AssertionError("The independent pharmacy buffs did not all reach the client");
				}
				client.gui.setScreen(new InventoryScreen(client.player));
			});
			context.waitTicks(10);
			context.takeScreenshot("jugcraft_early_pharmacy_inventory");
			context.runOnClient(client -> client.gui.setScreen(null));
			server.runOnServer(minecraft -> {
				var player = minecraft.getPlayerList().getPlayers().getFirst();
				new ItemStack(Items.MILK_BUCKET).finishUsingItem(player.level(), player);
			});
			context.waitTicks(25);
			context.runOnClient(client -> {
				if (client.player.hasEffect(Pharmacology.CAFFEINATED) || client.player.hasEffect(Pharmacology.FORTITUDE)
						|| client.player.hasEffect(Pharmacology.RECOVERY) || client.player.hasEffect(Pharmacology.ENDURANCE)) {
					throw new AssertionError("Milk did not clear every pharmacy benefit");
				}
			});
			server.runOnServer(minecraft -> {
				var player = minecraft.getPlayerList().getPlayers().getFirst();
				if (Pharmacology.strain(player) <= 0 || !Pharmacology.mayDrink(player, Pharmacology.CAFFEINE, false)) {
					throw new AssertionError("Milk reset the pharmacy recovery accounting");
				}
				for (int second = 0; second < 121; second++) {
					Pharmacology.tickSecond(player);
				}
				ItemStack stimulant = player.getInventory().getItem(5);
				player.getInventory().setItem(5, stimulant.finishUsingItem(player.level(), player));
			});
			// Real online time verifies the native Speed/Haste pulses and final-quarter redosing together.
			context.waitTicks(700);
			server.runOnServer(minecraft -> {
				var player = minecraft.getPlayerList().getPlayers().getFirst();
				ItemStack stimulant = player.getInventory().getItem(5);
				player.getInventory().setItem(5, stimulant.finishUsingItem(player.level(), player));
			});
			context.waitTicks(5);
			context.runOnClient(client -> {
				if (!client.player.hasEffect(Pharmacology.STIMULATED) || !client.player.hasEffect(Pharmacology.JITTERS)
						|| !client.player.hasEffect(Pharmacology.STIMULANT_APPETITE)) {
					throw new AssertionError("Stimulant benefits and drawbacks did not synchronize");
				}
				client.gui.setScreen(new InventoryScreen(client.player));
			});
			context.waitTicks(5);
			context.takeScreenshot("jugcraft_pharmacy_stimulant_drawbacks");
			context.runOnClient(client -> client.gui.setScreen(null));
			server.runOnServer(minecraft -> {
				var player = minecraft.getPlayerList().getPlayers().getFirst();
				var before = Pharmacology.state(player);
				new ItemStack(Pharmacology.ACTIVATED_CHARCOAL).finishUsingItem(player.level(), player);
				if (!Pharmacology.state(player).equals(before)) {
					throw new AssertionError("Charcoal changed dose accounting");
				}
			});
			context.waitTicks(5);
			context.runOnClient(client -> {
				if (client.player.hasEffect(Pharmacology.JITTERS) || client.player.hasEffect(Pharmacology.STIMULANT_APPETITE)
						|| !client.player.hasEffect(Pharmacology.STIMULATED)) {
					throw new AssertionError("Charcoal did not selectively clear the visible drawbacks");
				}
			});
		}
	}
}

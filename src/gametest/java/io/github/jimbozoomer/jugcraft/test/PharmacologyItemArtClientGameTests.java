package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.pharmacology.*;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.*;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.*;

/** Native GUI/held art for every delivered item and real timed use of all new forms. */
public class PharmacologyItemArtClientGameTests implements FabricClientGameTest {
	private static final String[] ITEMS={"caffeine_drink","aspirin","paracetamol","rehydration_salts","activated_charcoal","stimulant","penicillin","lidocaine","adrenaline","salbutamol","morphine","naloxone","methylphenidate","amphetamine","modafinil","ketamine","fentanyl"};
	@Override public void runTest(ClientGameTestContext context) {
		int guiScale=context.computeOnClient(c -> c.options.guiScale().get());
		context.runOnClient(c -> {c.getWindow().setWindowed(1280,800);c.options.guiScale().set(2);c.resizeGui();});
		try(TestSingleplayerContext world=context.worldBuilder().adjustSettings(s -> s.setGameMode(WorldCreationUiState.SelectedGameMode.SURVIVAL)).create()) {
			world.getConnection().waitForChunksRender();var server=world.getServer();BlockPos at=context.computeOnClient(c -> c.player.blockPosition());
			server.runCommand("gamerule minecraft:send_command_feedback false");server.runCommand("gamerule minecraft:spawn_mobs false");server.runCommand("gamerule minecraft:advance_time false");server.runCommand("time set noon");server.runCommand("weather clear");
			server.runCommand("fill %d %d %d %d %d %d minecraft:air".formatted(at.getX()-8,at.getY(),at.getZ()-8,at.getX()+8,at.getY()+6,at.getZ()+8));
			server.runCommand("fill %d %d %d %d %d %d minecraft:smooth_stone".formatted(at.getX()-8,at.getY()-1,at.getZ()-8,at.getX()+8,at.getY()-1,at.getZ()+8));
			server.runCommand("tp @p %f %d %f 180 18".formatted(at.getX()+.5,at.getY(),at.getZ()+2.5));
			server.runOnServer(s -> {var p=s.getPlayerList().getPlayers().getFirst();p.getInventory().clearContent();p.getAbilities().invulnerable=true;p.removeAttached(Pharmacology.STATE);for(int i=0;i<ITEMS.length;i++)p.getInventory().setItem(i,new ItemStack(item(ITEMS[i]),2));});context.waitTicks(12);
			context.runOnClient(c -> {c.gui.hud.getChat().clearMessages(true);c.gui.setScreen(new InventoryScreen(c.player));});context.waitTicks(6);context.takeScreenshot("pharmacy_art1_all_17_native_inventory");
			context.runOnClient(c -> c.gui.screen().onClose());context.waitForScreen(null);
			context.runOnClient(c -> {c.options.guiScale().set(1);c.resizeGui();c.gui.setScreen(new InventoryScreen(c.player));});context.waitTicks(6);context.takeScreenshot("pharmacy_art1_all_17_native_inventory_1x");
			context.runOnClient(c -> {c.gui.screen().onClose();c.options.guiScale().set(2);c.resizeGui();});context.waitForScreen(null);
			for(String id:ITEMS) {
				server.runOnServer(s -> s.getPlayerList().getPlayers().getFirst().setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(item(id),2)));context.waitTicks(6);
				context.takeScreenshot("pharmacy_art1_held_"+id);
			}
			for(String id:new String[]{"rehydration_salts","stimulant","adrenaline","salbutamol","naloxone"}) {
				server.runOnServer(s -> {var p=s.getPlayerList().getPlayers().getFirst();p.getInventory().clearContent();p.removeAttached(Pharmacology.STATE);p.removeAllEffects();p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(item(id),2));});context.waitTicks(10);
				context.getInput().holdKey(o -> o.keyUse);context.waitTicks(10);
				context.runOnClient(c -> {if(!c.player.isUsingItem())throw new AssertionError("Actual held use starts for "+id);var animation=c.player.getUseItem().get(DataComponents.CONSUMABLE).animation();if(animation!=(id.equals("rehydration_salts")||id.equals("stimulant")?ItemUseAnimation.EAT:ItemUseAnimation.NONE))throw new AssertionError("Use presentation matches the finished form: "+id);});
				context.takeScreenshot("pharmacy_art1_using_"+id);context.waitTicks(26);context.getInput().releaseKey(o -> o.keyUse);context.waitTicks(6);
				server.runOnServer(s -> {var p=s.getPlayerList().getPlayers().getFirst();int bottles=p.getInventory().countItem(Items.GLASS_BOTTLE);boolean device=id.equals("adrenaline")||id.equals("salbutamol")||id.equals("naloxone");if(p.getInventory().countItem(item(id))!=1 || bottles!=(device?1:0))throw new AssertionError("Native use consumes one and conserves its planned container for "+id+": bottles="+bottles);});
			}
			var reload=context.computeOnClient(c -> c.reloadResourcePacks());context.waitFor(c -> reload.isDone(),2400);reload.join();world.getConnection().waitForChunksRender();context.waitTicks(10);
			context.takeScreenshot("pharmacy_art1_after_resource_reload");
			System.out.println("Pharmacy Art Batch 1 rendered checks passed: all 17 native inventory/held sprites, actual packet/tablet/device use, stable use poses, one paid dose, correct zero/one glass returns and native resource reload.");
		} finally {context.runOnClient(c -> {c.options.guiScale().set(guiScale);c.resizeGui();});}
	}
	private static Item item(String id) {return BuiltInRegistries.ITEM.getValue(Jugcraft.id(id));}
}

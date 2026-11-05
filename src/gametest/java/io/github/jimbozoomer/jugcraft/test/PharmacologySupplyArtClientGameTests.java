package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.client.FormMachineScreen;
import io.github.jimbozoomer.jugcraft.energy.SimpleEnergyStorage;
import io.github.jimbozoomer.jugcraft.machine.MachineBlock;
import io.github.jimbozoomer.jugcraft.machine.form.*;
import io.github.jimbozoomer.jugcraft.pharmacology.Pharmacology;
import java.util.*;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.*;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;

/** Native inventory/held/use/frames and a real paper-dose production job for Art Batches 2–4. */
public class PharmacologySupplyArtClientGameTests implements FabricClientGameTest {
	private static final String BATCH1="caffeine_drink aspirin paracetamol rehydration_salts activated_charcoal stimulant penicillin lidocaine adrenaline salbutamol morphine naloxone methylphenidate amphetamine modafinil ketamine fentanyl";
	private static final String BATCH2="amphetamine_xr caffeine_xr dmt fentanyl_patch high_proof_spirits lsd masterwork_reserve mescaline methylphenidate_xr modafinil_xr moonshine psilocybin refined_spirits reserve_whiskey thc";
	private static final String BATCH3="advanced_release_matrix azure_culture_starter beverage_culture_pack botanical_capsule_shell botanical_resin culture_feed dream_folio dreamcap dreamcap_seeds empty_mash_jug empty_pharmacy_capsule fermented_mash_jug hemp_seeds lucid_imprint mash_jug masterwork_conditioning_insert mycelium_catalyst oak_conditioning_insert oneiric_imprint pattern_imprint pharmaceutical_base pharmaceutical_starch pharmacy_patch_backing precision_process_module prism_cactus_fruit prism_cactus_seeds release_control_die release_matrix release_matrix_blend sterile_carrier sterile_vial study_canvas";
	private static final String BATCH4="adrenaline_stock amphetamine_active amphetamine_reagent_blend amphetamine_stock amphetamine_xr_core antibiotic_extract beverage_wash_bucket caffeine_concentrate caffeine_xr_core dmt_active dmt_stock dream_concentrate fentanyl_active fentanyl_patch_stock fentanyl_reagent_blend fentanyl_stock high_proof_spirits_concentrate ketamine_active ketamine_reagent_blend ketamine_stock lidocaine_stock lsd_active lsd_stock masterwork_blend masterwork_reserve_concentrate masterwork_unconditioned_concentrate mescaline_active mescaline_stock methylphenidate_active methylphenidate_reagent_blend methylphenidate_stock methylphenidate_xr_core modafinil_active modafinil_reagent_blend modafinil_stock modafinil_xr_core naloxone_stock oneiric_stock organic_process_stock_bucket perception_substrate prismatic_concentrate psilocybin_active psilocybin_stock redleaf_medical_stock refined_beverage_stock_bucket refined_spirits_concentrate reserve_whiskey_concentrate salbutamol_stock thc_active thc_stock";
	private static final Set<String> BEVERAGES=Set.of("moonshine","refined_spirits","reserve_whiskey","high_proof_spirits","masterwork_reserve");
	@Override public void runTest(ClientGameTestContext context) {
		int scale=context.computeOnClient(c->c.options.guiScale().get());
		context.runOnClient(c->{c.getWindow().setWindowed(1280,800);c.options.guiScale().set(2);c.resizeGui();});
		try(TestSingleplayerContext world=context.worldBuilder().adjustSettings(s->s.setGameMode(WorldCreationUiState.SelectedGameMode.SURVIVAL)).create()) {
			world.getConnection().waitForChunksRender();var server=world.getServer();BlockPos at=context.computeOnClient(c->c.player.blockPosition());
			server.runCommand("gamerule minecraft:send_command_feedback false");server.runCommand("gamerule minecraft:spawn_mobs false");server.runCommand("gamerule minecraft:advance_time false");server.runCommand("time set noon");server.runCommand("weather clear");
			server.runCommand("fill %d %d %d %d %d %d minecraft:air".formatted(at.getX()-8,at.getY(),at.getZ()-8,at.getX()+22,at.getY()+6,at.getZ()+8));
			server.runCommand("fill %d %d %d %d %d %d minecraft:smooth_stone".formatted(at.getX()-8,at.getY()-1,at.getZ()-8,at.getX()+22,at.getY()-1,at.getZ()+8));
			server.runCommand("tp @p %f %d %f 180 18".formatted(at.getX()+.5,at.getY(),at.getZ()+2.5));
			server.runOnServer(s->{var p=s.getPlayerList().getPlayers().getFirst();p.getAbilities().invulnerable=true;p.removeAttached(Pharmacology.STATE);});
			inventory(context,world,"art2_all_15",BATCH2.split(" "));
			inventory(context,world,"art2_all_32_finished_formulations",(BATCH1+" "+BATCH2).split(" "));
			inventory(context,world,"art3_all_32_supplies",BATCH3.split(" "));
			context.runOnClient(c->{c.options.guiScale().set(1);c.resizeGui();});
			inventory(context,world,"art3_supplies_at_gui_scale_1",BATCH3.split(" "));
			context.runOnClient(c->{c.options.guiScale().set(2);c.resizeGui();});
			String[] materials=BATCH4.split(" ");
			inventory(context,world,"art4_materials_1",Arrays.copyOfRange(materials,0,25));
			inventory(context,world,"art4_materials_2",Arrays.copyOfRange(materials,25,50));
			inventory(context,world,"art4_precision_chains","methylphenidate_reagent_blend","methylphenidate_stock","methylphenidate_active","methylphenidate","methylphenidate_xr_core","methylphenidate_xr","amphetamine_reagent_blend","amphetamine_stock","amphetamine_active","amphetamine","amphetamine_xr_core","amphetamine_xr","modafinil_reagent_blend","modafinil_stock","modafinil_active","modafinil","modafinil_xr_core","modafinil_xr","ketamine_reagent_blend","ketamine_stock","ketamine_active","ketamine","fentanyl_reagent_blend","fentanyl_stock","fentanyl_active","fentanyl","fentanyl_patch_stock","fentanyl_patch");
			inventory(context,world,"art4_botanical_and_beverage_chains","botanical_resin","thc_stock","thc_active","thc","dreamcap","dream_concentrate","psilocybin_stock","psilocybin_active","psilocybin","prism_cactus_fruit","prismatic_concentrate","mescaline_stock","mescaline_active","mescaline","perception_substrate","lsd_stock","lsd_active","lsd","oneiric_stock","dmt_stock","dmt_active","dmt","refined_spirits_concentrate","refined_spirits","reserve_whiskey_concentrate","reserve_whiskey","high_proof_spirits_concentrate","high_proof_spirits","masterwork_unconditioned_concentrate","masterwork_reserve_concentrate","masterwork_reserve");
			if(Boolean.getBoolean("jugcraft.pharmacy.artInventoryOnly")) {
				for(String id:new String[]{"organic_process_stock_bucket","beverage_wash_bucket","refined_beverage_stock_bucket"}) {
					server.runOnServer(s->s.getPlayerList().getPlayers().getFirst().setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(item(id))));context.waitTicks(5);context.takeScreenshot("art4_final_held_"+id);
				}
				frames(context,world,at);paperDoseMachine(context,world,at.offset(10,0,-3));
				var refreshed=context.computeOnClient(c->c.reloadResourcePacks());context.waitFor(c->refreshed.isDone(),2400);refreshed.join();world.getConnection().waitForChunksRender();context.waitTicks(10);
				inventory(context,world,"art234_final_after_resource_reload",BATCH2.split(" "));
				System.out.println("Pharmacy Art Batches 2–4 final review passed: unobstructed inventory/chain views, final bucket proportions and held forms, native comparison frames, real paid paper-dose Press job and resource reload.");return;
			}
			for(String id:(BATCH2+" "+BATCH3+" "+BATCH4).split(" ")) {
				server.runOnServer(s->s.getPlayerList().getPlayers().getFirst().setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(item(id),2)));
				context.waitTicks(4);context.takeScreenshot("art234_held_"+id);
			}
			for(String id:BATCH2.split(" ")) {
				server.runOnServer(s->{var p=s.getPlayerList().getPlayers().getFirst();p.getInventory().clearContent();p.removeAttached(Pharmacology.STATE);p.removeAllEffects();p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(item(id),2));});context.waitTicks(8);
				float seconds=context.computeOnClient(c->c.player.getMainHandItem().get(DataComponents.CONSUMABLE).consumeSeconds());
				context.getInput().holdKey(o->o.keyUse);context.waitTicks(5);
				context.runOnClient(c->{if(!c.player.isUsingItem())throw new AssertionError("Native timed use must start: "+id);var animation=c.player.getUseItem().get(DataComponents.CONSUMABLE).animation();if(animation!=(BEVERAGES.contains(id)?ItemUseAnimation.DRINK:id.equals("fentanyl_patch")?ItemUseAnimation.NONE:ItemUseAnimation.EAT))throw new AssertionError("Wrong product pose: "+id);});
				context.takeScreenshot("art2_using_"+id);context.waitTicks((int)Math.ceil(seconds*20)-4);context.getInput().releaseKey(o->o.keyUse);context.waitTicks(5);
				server.runOnServer(s->{var p=s.getPlayerList().getPlayers().getFirst();if(p.getInventory().countItem(item(id))!=1||p.getInventory().countItem(Items.GLASS_BOTTLE)!=(BEVERAGES.contains(id)?1:0)||Pharmacology.strain(p)<=0)throw new AssertionError("One charged dose and its paid container: "+id);});
			}
			server.runOnServer(s->{var p=s.getPlayerList().getPlayers().getFirst();p.removeAttached(Pharmacology.STATE);p.removeAllEffects();p.getInventory().clearContent();});context.waitTicks(10);
			frames(context,world,at);
			paperDoseMachine(context,world,at.offset(10,0,-3));
			var reload=context.computeOnClient(c->c.reloadResourcePacks());context.waitFor(c->reload.isDone(),2400);reload.join();world.getConnection().waitForChunksRender();context.waitTicks(10);
			inventory(context,world,"art234_after_resource_reload",BATCH2.split(" "));
			System.out.println("Pharmacy Art Batches 2–4 passed: all 97 native inventory/held sprites, all 32 finished formulations, 15 real uses with exact dose/return counts, vanilla bucket/bottle frame comparisons, real paper-dose Press job, chain views and resource reload.");
		} finally {context.runOnClient(c->{c.options.guiScale().set(scale);c.resizeGui();if(c.gui.hud.isHidden())c.gui.hud.toggle();});}
	}
	private static Item item(String id) {var key=id.contains(":")?net.minecraft.resources.Identifier.parse(id):Jugcraft.id(id);var result=BuiltInRegistries.ITEM.getValue(key);if(result==Items.AIR)throw new AssertionError("Missing registered item: "+id);return result;}
	private static void inventory(ClientGameTestContext c,TestSingleplayerContext w,String label,String... ids) {
		w.getServer().runOnServer(s->{var p=s.getPlayerList().getPlayers().getFirst();p.getInventory().clearContent();for(int i=0;i<ids.length;i++)p.getInventory().setItem(i,new ItemStack(item(ids[i]),2));});c.waitTicks(8);
		c.runOnClient(mc->{mc.gui.hud.getChat().clearMessages(true);mc.gui.setScreen(new InventoryScreen(mc.player));});c.getInput().setCursorPos(10,10);c.waitTicks(5);c.takeScreenshot(label);c.runOnClient(mc->mc.gui.screen().onClose());c.waitForScreen(null);
	}
	private static void frames(ClientGameTestContext c,TestSingleplayerContext w,BlockPos at) {
		var server=w.getServer();server.runCommand("fill %d %d %d %d %d %d minecraft:spruce_planks".formatted(at.getX(),at.getY(),at.getZ()-4,at.getX()+5,at.getY()+3,at.getZ()-4));
		String[] buckets={"minecraft:bucket","jugcraft:organic_process_stock_bucket","jugcraft:beverage_wash_bucket","jugcraft:refined_beverage_stock_bucket"};
		String[] bottles={"minecraft:potion","jugcraft:moonshine","jugcraft:refined_spirits","jugcraft:reserve_whiskey","jugcraft:high_proof_spirits","jugcraft:masterwork_reserve"};
		for(int i=0;i<buckets.length;i++)server.runCommand("summon minecraft:item_frame %d %d %d {Facing:3b,Fixed:1b,Item:{id:\"%s\",count:1}}".formatted(at.getX()+i,at.getY()+1,at.getZ()-3,buckets[i]));
		for(int i=0;i<bottles.length;i++)server.runCommand("summon minecraft:item_frame %d %d %d {Facing:3b,Fixed:1b,Item:{id:\"%s\",count:1}}".formatted(at.getX()+i,at.getY()+2,at.getZ()-3,bottles[i]));
		server.runCommand("tp @p %f %d %f 180 0".formatted(at.getX()+2.5,at.getY(),at.getZ()+1.5));c.runOnClient(mc->{if(!mc.gui.hud.isHidden())mc.gui.hud.toggle();});c.waitTicks(20);w.getConnection().waitForChunksRender();c.takeScreenshot("art234_vanilla_and_pharmacy_item_frames");c.runOnClient(mc->{if(mc.gui.hud.isHidden())mc.gui.hud.toggle();});
	}
	private static void paperDoseMachine(ClientGameTestContext c,TestSingleplayerContext w,BlockPos pos) {
		var server=w.getServer();var block=IndustrialForms.FORMULATION_PRESS;
		server.runCommand("tp @p %f %d %f 180 0".formatted(pos.getX()+1.5,pos.getY(),pos.getZ()+3.5));
		server.runOnServer(s->{
			var level=s.overworld();var state=block.defaultBlockState().setValue(MachineBlock.FACING,Direction.SOUTH);
			level.setBlock(pos,state,Block.UPDATE_ALL);block.setPlacedBy(level,pos,state,null,ItemStack.EMPTY);
			var m=(FormMachineBlockEntity)level.getBlockEntity(pos);if(!m.paused())m.togglePause();
			var entry=m.recipes(s).stream().filter(e->e.id().getPath().endsWith("/perception_finish_lsd")).findFirst().orElseThrow();
			m.selectRecipe(entry.id());var recipe=entry.recipe();
			for(int i=0;i<recipe.items().size();i++)m.container().setItem(i,new ItemStack(recipe.items().get(i).ingredient().items().findFirst().orElseThrow(),recipe.items().get(i).count()));
			for(int i=0;i<recipe.fluids().size();i++)m.tanks().input(i).fill(recipe.fluids().get(i).fluid(),recipe.fluids().get(i).amount());
			m.container().setItem(m.form().firstSocketSlot(),new ItemStack(item("lsd_formula_plate")));
			((SimpleEnergyStorage)m.energy()).setAmount(m.energy().getCapacity());s.getPlayerList().getPlayers().getFirst().openMenu(m);
		});
		c.waitForScreen(FormMachineScreen.class);c.getInput().setCursorPos(10,10);c.waitTicks(5);c.takeScreenshot("art2_lsd_paid_paper_press_inputs");
		server.runOnServer(s->{var m=(FormMachineBlockEntity)s.overworld().getBlockEntity(pos);if(m.paused())m.togglePause();});c.waitTicks(230);
		server.runOnServer(s->{var m=(FormMachineBlockEntity)s.overworld().getBlockEntity(pos);var output=m.container().getItem(m.form().firstOutputSlot());if(!output.is(item("lsd"))||output.getCount()!=4||m.container().getItem(m.form().firstSocketSlot()).getCount()!=1)throw new AssertionError("Actual Press must finish four paper doses and retain its formula");});c.takeScreenshot("art2_lsd_four_finished_paper_doses");c.runOnClient(mc->mc.gui.screen().onClose());c.waitForScreen(null);
	}
}

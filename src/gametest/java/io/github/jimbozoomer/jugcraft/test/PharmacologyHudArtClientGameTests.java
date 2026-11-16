package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.client.*;
import io.github.jimbozoomer.jugcraft.pharmacology.*;
import java.util.*;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.*;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;

/** Native additive HUD, all status symbols, whole block models and a final catalog contact pass. */
public class PharmacologyHudArtClientGameTests implements FabricClientGameTest {
	private static final String[] EFFECTS=("caffeinated caffeine_jitters fortitude pharmacy_recovery endurance stimulated stimulant_appetite sterile_recovery lidocaine adrenaline salbutamol morphine pharmacy_absorption adrenaline_fatigue opioid_drowsiness methylphenidate amphetamine modafinil ketamine fentanyl moonshine refined_spirits reserve_whiskey high_proof_spirits intoxication precision_focus caffeine_xr methylphenidate_xr amphetamine_xr modafinil_xr fentanyl_patch masterwork_reserve thc psilocybin mescaline lsd dmt botanical_relaxation pattern_interference").split(" ");
	private static final String[] MODELS=("dream_study_plinth laboratory_analyzer pharmaceutical_separator formulation_press autoclave sterile_filler moonshine_still mash_conditioning_barrel release_coater").split(" ");
	// Catalog follows the canonical art-batch assignments, rather than the enormous unrelated creative catalog.
	private static final String[] CATALOG=("caffeine_drink aspirin paracetamol rehydration_salts activated_charcoal stimulant penicillin lidocaine adrenaline salbutamol morphine naloxone methylphenidate amphetamine modafinil ketamine fentanyl amphetamine_xr caffeine_xr dmt fentanyl_patch high_proof_spirits lsd masterwork_reserve mescaline methylphenidate_xr modafinil_xr moonshine psilocybin refined_spirits reserve_whiskey thc advanced_release_matrix azure_culture_starter beverage_culture_pack botanical_capsule_shell botanical_resin culture_feed dream_folio dreamcap dreamcap_seeds empty_mash_jug empty_pharmacy_capsule fermented_mash_jug hemp_seeds lucid_imprint mash_jug masterwork_conditioning_insert mycelium_catalyst oak_conditioning_insert oneiric_imprint pattern_imprint pharmaceutical_base pharmaceutical_starch pharmacy_patch_backing precision_process_module prism_cactus_fruit prism_cactus_seeds release_control_die release_matrix release_matrix_blend sterile_carrier sterile_vial study_canvas adrenaline_stock amphetamine_active amphetamine_reagent_blend amphetamine_stock amphetamine_xr_core antibiotic_extract beverage_wash_bucket caffeine_concentrate caffeine_xr_core dmt_active dmt_stock dream_concentrate fentanyl_active fentanyl_patch_stock fentanyl_reagent_blend fentanyl_stock high_proof_spirits_concentrate ketamine_active ketamine_reagent_blend ketamine_stock lidocaine_stock lsd_active lsd_stock masterwork_blend masterwork_reserve_concentrate masterwork_unconditioned_concentrate mescaline_active mescaline_stock methylphenidate_active methylphenidate_reagent_blend methylphenidate_stock methylphenidate_xr_core modafinil_active modafinil_reagent_blend modafinil_stock modafinil_xr_core naloxone_stock oneiric_stock organic_process_stock_bucket perception_substrate prismatic_concentrate psilocybin_active psilocybin_stock redleaf_medical_stock refined_beverage_stock_bucket refined_spirits_concentrate reserve_whiskey_concentrate salbutamol_stock thc_active thc_stock activated_charcoal_formula_plate adrenaline_formula_plate amphetamine_formula_plate amphetamine_xr_formula_plate aspirin_formula_plate blank_formula_plate caffeine_formula_plate caffeine_xr_formula_plate dmt_formula_plate fentanyl_formula_plate fentanyl_patch_formula_plate high_proof_spirits_formula_plate ketamine_formula_plate lidocaine_formula_plate lsd_formula_plate masterwork_reserve_formula_plate mescaline_formula_plate methylphenidate_formula_plate methylphenidate_xr_formula_plate modafinil_formula_plate modafinil_xr_formula_plate morphine_formula_plate naloxone_formula_plate paracetamol_formula_plate penicillin_formula_plate psilocybin_formula_plate refined_spirits_formula_plate rehydration_salts_formula_plate reserve_whiskey_formula_plate salbutamol_formula_plate stimulant_formula_plate thc_formula_plate").split(" ");
	@Override public void runTest(ClientGameTestContext context) {
		int scale=context.computeOnClient(c->c.options.guiScale().get());
		var visuals=context.computeOnClient(c->ConcordanceClientOptions.drugVisuals());
		context.runOnClient(c->{c.getWindow().setWindowed(1280,800);c.options.guiScale().set(2);c.resizeGui();if(c.gui.hud.isHidden())c.gui.hud.toggle();ConcordanceClientOptions.setDrugVisuals(new DrugVisualOptions(false,75,70,60,70));});
		try(TestSingleplayerContext world=context.worldBuilder().adjustSettings(s->s.setGameMode(WorldCreationUiState.SelectedGameMode.SURVIVAL)).create()) {
			world.getConnection().waitForChunksRender();var server=world.getServer();BlockPos at=context.computeOnClient(c->c.player.blockPosition());
			server.runCommand("gamerule minecraft:send_command_feedback false");server.runCommand("gamerule minecraft:spawn_mobs false");server.runCommand("gamerule minecraft:advance_time false");server.runCommand("time set noon");server.runCommand("weather clear");
			server.runCommand("fill %d %d %d %d %d %d minecraft:air".formatted(at.getX()-12,at.getY(),at.getZ()-10,at.getX()+28,at.getY()+6,at.getZ()+8));
			server.runCommand("fill %d %d %d %d %d %d minecraft:smooth_stone".formatted(at.getX()-12,at.getY()-1,at.getZ()-10,at.getX()+28,at.getY()-1,at.getZ()+8));
			server.runCommand("tp @p %f %d %f 180 18".formatted(at.getX()+.5,at.getY(),at.getZ()+2.5));
			server.runOnServer(s->{var p=s.getPlayerList().getPlayers().getFirst();p.getAbilities().invulnerable=true;p.getInventory().clearContent();});
			boundaries();
			strain(context,world,0);context.waitTicks(30);context.runOnClient(c->c.gui.hud.getChat().clearMessages(true));context.waitTicks(2);Path vanilla=capture(context,"art6_hud_zero_vanilla");
			for(int value:new int[]{650,1000,1010,1250,1260,1500,1510,2000,2010,2250,3000,3500}) {
				strain(context,world,value);Path capture=capture(context,"art6_hud_strain_"+value);
				if(value==650)unchangedVanilla(vanilla,capture);
			}
			server.runOnServer(s->{var p=s.getPlayerList().getPlayers().getFirst();p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(PrecisionPharmacy.item("fentanyl"),2));});
			context.waitFor(c->StrainClient.state().nextCost()==55,100);context.waitTicks(4);capture(context,"art6_hud_held_forecast_above_300");
			strain(context,world,935);capture(context,"art6_hud_forecast_crosses_100");
			server.runOnServer(s->{var p=s.getPlayerList().getPlayers().getFirst();p.setItemSlot(EquipmentSlot.CHEST,new ItemStack(Items.DIAMOND_CHESTPLATE));});context.waitTicks(5);capture(context,"art6_hud_armor_and_selected_title");
			server.runOnServer(s->{var p=s.getPlayerList().getPlayers().getFirst();p.getAttribute(Attributes.MAX_HEALTH).setBaseValue(40);p.setHealth(40);p.getAttribute(Attributes.MAX_ABSORPTION).setBaseValue(12);p.setAbsorptionAmount(12);if(p.getAbsorptionAmount()!=12)throw new AssertionError("Real absorption hearts in native layout fixture");});context.waitTicks(5);capture(context,"art6_hud_extra_hearts_and_armor");
			for(int gui:new int[]{1,3}) {context.runOnClient(c->{c.options.guiScale().set(gui);c.resizeGui();});context.waitTicks(4);capture(context,"art6_hud_stacked_gui_"+gui);}
			context.runOnClient(c->{c.options.guiScale().set(2);c.resizeGui();c.gui.hud.toggle();});context.waitTicks(3);capture(context,"art6_hud_f1_hidden");context.runOnClient(c->c.gui.hud.toggle());
			server.runCommand("gamemode spectator @p");context.waitTicks(5);capture(context,"art6_hud_spectator_hidden");server.runCommand("gamemode creative @p");context.waitTicks(5);capture(context,"art6_hud_creative_hidden");server.runCommand("gamemode survival @p");
			server.runOnServer(s->{var p=s.getPlayerList().getPlayers().getFirst();p.getInventory().clearContent();p.getAttribute(Attributes.MAX_HEALTH).setBaseValue(20);p.setHealth(20);p.setAbsorptionAmount(0);p.getAttribute(Attributes.MAX_ABSORPTION).setBaseValue(0);});
			strain(context,world,650);context.waitTicks(30);capture(context,"art6_hud_65_no_held_item");
			final int before=context.computeOnClient(c->StrainClient.state().tenths());
			server.runOnServer(s->{var p=s.getPlayerList().getPlayers().getFirst();for(int i=0;i<25;i++)Pharmacology.tickSecond(p);});
			context.waitFor(c->StrainClient.state().tenths()<=before-50,100);capture(context,"art6_hud_decay_removes_fill");
			server.runCommand("fill %d %d %d %d %d %d minecraft:water".formatted(at.getX()-2,at.getY(),at.getZ(),at.getX()+2,at.getY()+2,at.getZ()+4));context.waitTicks(6);capture(context,"art6_hud_hunger_underwater_air_clearance");
			server.runCommand("fill %d %d %d %d %d %d minecraft:air".formatted(at.getX()-2,at.getY(),at.getZ(),at.getX()+2,at.getY()+2,at.getZ()+4));server.runOnServer(s->{var p=s.getPlayerList().getPlayers().getFirst();p.setAirSupply(p.getMaxAirSupply());});context.waitTicks(5);

			strain(context,world,0);context.waitTicks(25);
			server.runOnServer(s->s.getPlayerList().getPlayers().getFirst().removeAllEffects());
			for(int start=0;start<EFFECTS.length;start+=6) {
				// Art-only local fixtures let every conditional symbol reach the native effect sidebar.
				// The real server correctly removes orphaned side-effect markers without an active dose;
				// its ledger and conditions are separately tested by the gameplay/use suites.
				final int first=start;clearClientEffects(context);context.runOnClient(mc->{var p=mc.player;for(int i=first;i<Math.min(first+6,EFFECTS.length);i++)p.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(BuiltInRegistries.MOB_EFFECT.getValue(Jugcraft.id(EFFECTS[i]))),6000,0));});
				context.waitTicks(5);context.runOnClient(c->c.gui.setScreen(new InventoryScreen(c.player)));context.getInput().setCursorPos(10,10);context.waitTicks(3);capture(context,"art6_native_effects_"+first);context.runOnClient(c->c.gui.screen().onClose());context.waitForScreen(null);
				context.runOnClient(mc->{if(mc.player.getActiveEffects().size()!=Math.min(6,EFFECTS.length-first))throw new AssertionError("Every assigned effect symbol must be present in its native gallery capture: "+first);});
			}
			clearClientEffects(context);
			server.runOnServer(s->s.getPlayerList().getPlayers().getFirst().removeAllEffects());
			inventory(context,world,"art6_all_nine_whole_block_models",MODELS);
			// Look away from target blocks: the intentional multiblock placement preview is separate
			// from the actual held item model and must not obscure this model review.
			server.runCommand("tp @p %f %d %f 180 -35".formatted(at.getX()+.5,at.getY(),at.getZ()+2.5));context.waitTicks(5);
			for(String id:MODELS) {server.runOnServer(s->s.getPlayerList().getPlayers().getFirst().setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(item(id),2)));context.waitTicks(4);capture(context,"art6_model_held_"+id);}
			frames(context,world,at.offset(-4,0,-3));
			for(int start=0;start<CATALOG.length;start+=36)inventory(context,world,"art6_catalog_"+start,Arrays.copyOfRange(CATALOG,start,Math.min(start+36,CATALOG.length)));
			inventory(context,world,"art6_shared_coffee_crops_and_inputs","coffee_beans","caffeine_drink","caffeine_concentrate","caffeine_xr_core","caffeine_xr","caffeine_formula_plate","caffeine_xr_formula_plate","hemp_seeds","dreamcap","dreamcap_seeds","prism_cactus_fruit","prism_cactus_seeds","dream_study_plinth");
			var reload=context.computeOnClient(c->c.reloadResourcePacks());context.waitFor(c->reload.isDone(),2400);reload.join();world.getConnection().waitForChunksRender();context.waitTicks(10);
			server.runOnServer(s->s.getPlayerList().getPlayers().getFirst().getInventory().clearContent());strain(context,world,2250);capture(context,"art6_hud_after_resource_reload");inventory(context,world,"art6_models_after_resource_reload",MODELS);
			System.out.println("Pharmacy Art Batch 6 passed: ten-pill layered HUD, exact boundaries/overflow, forecast, decay, armor/extra hearts, GUI scales 1/2/3, F1/spectator/creative visibility, all 39 native effect symbols, all 9 whole models, all 146 flat catalog items, shared inputs and native resource reload.");
		} finally {context.runOnClient(c->{ConcordanceClientOptions.setDrugVisuals(visuals);c.options.guiScale().set(scale);c.resizeGui();if(c.gui.hud.isHidden())c.gui.hud.toggle();});}
	}
	private static void boundaries() {
		int[][] cases={{0,1,0},{50,1,2},{1000,1,5},{1001,2,1},{1250,2,5},{1251,3,5},{1500,3,5},{1501,4,5},{2000,4,5},{2001,5,1},{3000,5,5},{Integer.MAX_VALUE,5,5}};
		for(int[] test:cases)if(StrainPillHud.layer(test[0])!=test[1]||StrainPillHud.fillColumns(test[0],0)!=test[2])throw new AssertionError("Strain boundary "+test[0]);
		if(StrainPillHud.fillColumns(650,6)!=2||StrainPillHud.fillColumns(650,7)!=0||StrainPillHud.fillColumns(1000,9)!=5||StrainPillHud.fillColumns(2001,9)!=0||StrainPillHud.fillColumns(3000,9)!=5)throw new AssertionError("Ten-slot capacity / half-pill fill");
		if(StrainPillHud.rowX(640)!=330||StrainPillHud.rowY(400,0,false,false)!=351||StrainPillHud.rowY(400,0,true,false)!=341||StrainPillHud.rowY(400,0,true,true)!=318||StrainPillHud.rowY(400,2,false,false)!=341||StrainPillHud.rowY(400,3,false,false)!=318)throw new AssertionError("Hunger-column alignment, air/mount rows, selected title and action-bar clearance");
		var active=new StrainStatus(1,0,0,0,false,0,0);
		if(StrainPillHud.visible(StrainStatus.EMPTY,true,false,false,true)||!StrainPillHud.visible(active,true,false,false,true)||StrainPillHud.visible(active,false,false,false,true)||StrainPillHud.visible(active,true,true,false,true)||StrainPillHud.visible(active,true,false,true,true)||StrainPillHud.visible(active,true,false,false,false))throw new AssertionError("Visibility matches vanilla survival HUD");
	}
	private static Path capture(ClientGameTestContext c,String name) {c.runOnClient(mc->mc.gui.hud.getChat().clearMessages(true));c.waitTicks(2);return c.takeScreenshot(name);}
	private static void clearClientEffects(ClientGameTestContext c) {c.runOnClient(mc->{for(var effect:List.copyOf(mc.player.getActiveEffects()))mc.player.removeEffect(effect.getEffect());});}

	private static void unchangedVanilla(Path zero,Path strain) {
		try {
			var a=ImageIO.read(zero.toFile());var b=ImageIO.read(strain.toFile());
			if(a.getWidth()!=1280||a.getHeight()!=800)throw new AssertionError("Expected native screenshot dimensions");
			// Same stationary survival player. Allow two channel levels for native vignette interpolation
			// through transparent pixels; added/shifted HUD pixels differ by much more than that.
			for(int[] r:new int[][]{{458,722,162,18},{660,722,162,18},{458,742,364,10},{458,756,364,44}})
				for(int y=r[1];y<r[1]+r[3];y++)for(int x=r[0];x<r[0]+r[2];x++)for(int channel:new int[]{0,8,16})if(Math.abs(((a.getRGB(x,y)>>channel)&255)-((b.getRGB(x,y)>>channel)&255))>2)throw new AssertionError("Strain HUD changed vanilla pixel at "+x+","+y);
			int filled=b.getRGB(664,708)&0xFFFFFF,empty=b.getRGB(792,708)&0xFFFFFF;
			if(filled!=0xFFF3CE||empty!=0x656962)throw new AssertionError("Native ivory fill and empty capsule at their expected coordinates: "+Integer.toHexString(filled)+" / "+Integer.toHexString(empty));
		} catch(java.io.IOException e){throw new AssertionError("Read native HUD screenshots",e);}
	}
	private static void strain(ClientGameTestContext c,TestSingleplayerContext w,int value) {
		w.getServer().runOnServer(s->{var p=s.getPlayerList().getPlayers().getFirst();p.setAttached(Pharmacology.STATE,new DrugState(List.of(),List.of(),value));StrainStatus.sync(p);});
		c.waitFor(mc->StrainClient.state().tenths()<=value&&StrainClient.state().tenths()>=Math.max(0,value-8),100);c.waitTicks(3);
	}
	private static Item item(String id) {var it=BuiltInRegistries.ITEM.getValue(Jugcraft.id(id));if(it==null||it==Items.AIR)throw new AssertionError("Missing art item "+id);return it;}
	private static void inventory(ClientGameTestContext c,TestSingleplayerContext w,String label,String... ids) {
		w.getServer().runOnServer(s->{var p=s.getPlayerList().getPlayers().getFirst();p.getInventory().clearContent();for(int i=0;i<ids.length;i++){var stack=new ItemStack(item(ids[i]));stack.setCount(Math.min(64,stack.getMaxStackSize()));p.getInventory().setItem(i,stack);}});c.waitTicks(6);
		c.runOnClient(mc->{mc.gui.hud.getChat().clearMessages(true);mc.gui.setScreen(new InventoryScreen(mc.player));});c.getInput().setCursorPos(10,10);c.waitTicks(4);capture(c,label);c.runOnClient(mc->mc.gui.screen().onClose());c.waitForScreen(null);
	}
	private static void frames(ClientGameTestContext c,TestSingleplayerContext w,BlockPos at) {
		w.getServer().runOnServer(s->s.getPlayerList().getPlayers().getFirst().getInventory().clearContent());c.waitTicks(4);
		var s=w.getServer();s.runCommand("fill %d %d %d %d %d %d minecraft:spruce_planks".formatted(at.getX(),at.getY(),at.getZ()-1,at.getX()+8,at.getY()+2,at.getZ()-1));
		for(int i=0;i<MODELS.length;i++)s.runCommand("summon minecraft:item_frame %d %d %d {Facing:3b,Fixed:1b,Item:{id:\"jugcraft:%s\",count:1}}".formatted(at.getX()+i,at.getY()+1,at.getZ(),MODELS[i]));
		s.runCommand("tp @p %f %d %f 180 0".formatted(at.getX()+4.5,at.getY(),at.getZ()+6.5));c.runOnClient(mc->{if(!mc.gui.hud.isHidden())mc.gui.hud.toggle();});c.waitTicks(20);w.getConnection().waitForChunksRender();capture(c,"art6_nine_complete_block_item_frames");c.runOnClient(mc->mc.gui.hud.toggle());
	}
}

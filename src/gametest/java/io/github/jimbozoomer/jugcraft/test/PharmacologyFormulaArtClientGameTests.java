package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.client.FormMachineScreen;
import io.github.jimbozoomer.jugcraft.client.MachineRecipeScreen;
import io.github.jimbozoomer.jugcraft.energy.SimpleEnergyStorage;
import io.github.jimbozoomer.jugcraft.machine.MachineBlock;
import io.github.jimbozoomer.jugcraft.machine.form.*;
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

/** Native formula-plate inventory, held/frame views, retained tools and real production on existing stations. */
public class PharmacologyFormulaArtClientGameTests implements FabricClientGameTest {
	private static final String[] PLATES=("activated_charcoal_formula_plate adrenaline_formula_plate amphetamine_formula_plate amphetamine_xr_formula_plate aspirin_formula_plate blank_formula_plate caffeine_formula_plate caffeine_xr_formula_plate dmt_formula_plate fentanyl_formula_plate fentanyl_patch_formula_plate high_proof_spirits_formula_plate ketamine_formula_plate lidocaine_formula_plate lsd_formula_plate masterwork_reserve_formula_plate mescaline_formula_plate methylphenidate_formula_plate methylphenidate_xr_formula_plate modafinil_formula_plate modafinil_xr_formula_plate morphine_formula_plate naloxone_formula_plate paracetamol_formula_plate penicillin_formula_plate psilocybin_formula_plate refined_spirits_formula_plate rehydration_salts_formula_plate reserve_whiskey_formula_plate salbutamol_formula_plate stimulant_formula_plate thc_formula_plate").split(" ");
	@Override public void runTest(ClientGameTestContext context) {
		int scale=context.computeOnClient(c->c.options.guiScale().get());
		context.runOnClient(c->{c.getWindow().setWindowed(1280,800);c.options.guiScale().set(2);c.resizeGui();});
		try(TestSingleplayerContext world=context.worldBuilder().adjustSettings(s->s.setGameMode(WorldCreationUiState.SelectedGameMode.SURVIVAL)).create()) {
			world.getConnection().waitForChunksRender();var server=world.getServer();BlockPos at=context.computeOnClient(c->c.player.blockPosition());
			server.runCommand("gamerule minecraft:send_command_feedback false");server.runCommand("gamerule minecraft:spawn_mobs false");server.runCommand("gamerule minecraft:advance_time false");server.runCommand("time set noon");server.runCommand("weather clear");
			server.runCommand("fill %d %d %d %d %d %d minecraft:air".formatted(at.getX()-12,at.getY(),at.getZ()-10,at.getX()+28,at.getY()+6,at.getZ()+8));
			server.runCommand("fill %d %d %d %d %d %d minecraft:smooth_stone".formatted(at.getX()-12,at.getY()-1,at.getZ()-10,at.getX()+28,at.getY()-1,at.getZ()+8));
			server.runCommand("tp @p %f %d %f 180 18".formatted(at.getX()+.5,at.getY(),at.getZ()+2.5));
			server.runOnServer(s->{var p=s.getPlayerList().getPlayers().getFirst();p.getAbilities().invulnerable=true;for(String id:PLATES)if(new ItemStack(item(id)).has(DataComponents.CONSUMABLE))throw new AssertionError("A formula remains a reusable tool, not a dose: "+id);});
			inventory(context,world,"art5_all_32_formulas",PLATES);
			context.runOnClient(c->{c.options.guiScale().set(1);c.resizeGui();});inventory(context,world,"art5_all_32_formulas_gui_scale_1",PLATES);context.runOnClient(c->{c.options.guiScale().set(2);c.resizeGui();});
			inventory(context,world,"art5_base_xr_and_patch_comparisons","caffeine_formula_plate","caffeine_xr_formula_plate","methylphenidate_formula_plate","methylphenidate_xr_formula_plate","amphetamine_formula_plate","amphetamine_xr_formula_plate","modafinil_formula_plate","modafinil_xr_formula_plate","fentanyl_formula_plate","fentanyl_patch_formula_plate","blank_formula_plate");
			for(String id:PLATES) {
				server.runOnServer(s->s.getPlayerList().getPlayers().getFirst().setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(item(id),2)));context.waitTicks(4);context.takeScreenshot("art5_held_"+id);
			}
			frames(context,world,at.offset(-10,0,-3));
			job(context,world,at.offset(0,0,-4),IndustrialForms.LABORATORY_ANALYZER,"pharmacy_analyze_caffeine",null,"caffeine_formula_plate",1);
			job(context,world,at.offset(5,0,-4),IndustrialForms.FORMULATION_PRESS,"release_finish_caffeine_xr","caffeine_xr_formula_plate","caffeine_xr",4);
			job(context,world,at.offset(10,0,-4),IndustrialForms.STERILE_FILLER,"precision_fill_fentanyl","fentanyl_formula_plate","fentanyl",4);
			job(context,world,at.offset(15,0,-4),IndustrialForms.RELEASE_COATER,"release_coat_fentanyl_patch","release_control_die","fentanyl_patch_stock",4);
			job(context,world,at.offset(20,0,-4),IndustrialForms.FORMULATION_PRESS,"release_finish_fentanyl_patch","fentanyl_patch_formula_plate","fentanyl_patch",4);
			var reload=context.computeOnClient(c->c.reloadResourcePacks());context.waitFor(c->reload.isDone(),2400);reload.join();world.getConnection().waitForChunksRender();context.waitTicks(10);
			inventory(context,world,"art5_all_32_after_resource_reload",PLATES);
			System.out.println("Pharmacy Art Batch 5 passed: all 32 formula plates in native mixed inventory/held views, base/XR/patch frame comparisons, Analyzer/Press/Filler/Coater recipe and tool screens, five real paid jobs with retained tools, and native resource reload.");
		} finally {context.runOnClient(c->{c.options.guiScale().set(scale);c.resizeGui();if(c.gui.hud.isHidden())c.gui.hud.toggle();});}
	}
	private static Item item(String id) {var result=BuiltInRegistries.ITEM.getValue(Jugcraft.id(id));if(result==Items.AIR)throw new AssertionError("Missing formula/product: "+id);return result;}
	private static void inventory(ClientGameTestContext c,TestSingleplayerContext w,String label,String... ids) {
		w.getServer().runOnServer(s->{var p=s.getPlayerList().getPlayers().getFirst();p.getInventory().clearContent();for(int i=0;i<ids.length;i++)p.getInventory().setItem(i,new ItemStack(item(ids[i]),2));});c.waitTicks(8);
		c.runOnClient(mc->{mc.gui.hud.getChat().clearMessages(true);mc.gui.setScreen(new InventoryScreen(mc.player));});c.getInput().setCursorPos(10,10);c.waitTicks(5);c.takeScreenshot(label);c.runOnClient(mc->mc.gui.screen().onClose());c.waitForScreen(null);
	}
	private static void frames(ClientGameTestContext c,TestSingleplayerContext w,BlockPos at) {
		var server=w.getServer();server.runCommand("fill %d %d %d %d %d %d minecraft:spruce_planks".formatted(at.getX(),at.getY(),at.getZ()-1,at.getX()+4,at.getY()+3,at.getZ()-1));
		String[] bases={"caffeine","methylphenidate","amphetamine","modafinil","fentanyl"};
		for(int i=0;i<bases.length;i++) {
			for(int row=0;row<2;row++) {
				String id=bases[i]+(row==0?"":i==4?"_patch":"_xr")+"_formula_plate";
				server.runCommand("summon minecraft:item_frame %d %d %d {Facing:3b,Fixed:1b,Item:{id:\"jugcraft:%s\",count:1}}".formatted(at.getX()+i,at.getY()+1+row,at.getZ(),id));
			}
		}
		server.runCommand("tp @p %f %d %f 180 0".formatted(at.getX()+2.5,at.getY(),at.getZ()+4.5));c.runOnClient(mc->{if(!mc.gui.hud.isHidden())mc.gui.hud.toggle();});c.waitTicks(20);w.getConnection().waitForChunksRender();c.takeScreenshot("art5_base_xr_patch_formula_frames");c.runOnClient(mc->{if(mc.gui.hud.isHidden())mc.gui.hud.toggle();});
	}
	private static void job(ClientGameTestContext c,TestSingleplayerContext w,BlockPos pos,FormMachineBlock block,String recipeName,String expectedTool,String result,int count) {
		var server=w.getServer();server.runCommand("tp @p %f %d %f 180 0".formatted(pos.getX()+1.5,pos.getY(),pos.getZ()+3.5));
		server.runOnServer(s->{
			var level=s.overworld();var state=block.defaultBlockState().setValue(MachineBlock.FACING,Direction.SOUTH);level.setBlock(pos,state,Block.UPDATE_ALL);block.setPlacedBy(level,pos,state,null,ItemStack.EMPTY);
			var m=(FormMachineBlockEntity)level.getBlockEntity(pos);if(!m.paused())m.togglePause();
			var entry=m.recipes(s).stream().filter(e->e.id().getPath().endsWith('/'+recipeName)).findFirst().orElseThrow();m.selectRecipe(entry.id());var recipe=entry.recipe();
			for(int i=0;i<recipe.items().size();i++)m.container().setItem(i,new ItemStack(recipe.items().get(i).ingredient().items().findFirst().orElseThrow(),recipe.items().get(i).count()));
			for(int i=0;i<recipe.fluids().size();i++)m.tanks().input(i).fill(recipe.fluids().get(i).fluid(),recipe.fluids().get(i).amount());
			if(expectedTool!=null) {
				var tool=recipe.tool().orElseThrow();if(!tool.test(new ItemStack(item(expectedTool))))throw new AssertionError("Correct formula/die authority for "+recipeName);
				m.container().setItem(PharmacologyFactoryGameTests.toolSlot(m,tool),new ItemStack(item(expectedTool)));
			} else if(recipe.tool().isPresent())throw new AssertionError("Unexpected tool in base analysis");
			((SimpleEnergyStorage)m.energy()).setAmount(m.energy().getCapacity());s.getPlayerList().getPlayers().getFirst().openMenu(m);
		});
		c.waitForScreen(FormMachineScreen.class);c.getInput().setCursorPos(10,10);c.waitTicks(5);c.takeScreenshot("art5_"+recipeName+"_inputs_and_tool");
		c.runOnClient(mc->((FormMachineScreen)mc.gui.screen()).openRecipes());c.waitForScreen(MachineRecipeScreen.class);c.getInput().setCursorPos(10,10);c.waitTicks(8);c.takeScreenshot("art5_"+recipeName+"_recipe");c.runOnClient(mc->mc.gui.screen().onClose());c.waitForScreen(FormMachineScreen.class);
		int ticks=server.computeOnServer(s->{var m=(FormMachineBlockEntity)s.overworld().getBlockEntity(pos);if(m.paused())m.togglePause();return m.recipes(s).stream().filter(e->e.id().getPath().endsWith('/'+recipeName)).findFirst().orElseThrow().recipe().time()+100;});
		while(ticks>0) {
			server.runOnServer(s->{var m=(FormMachineBlockEntity)s.overworld().getBlockEntity(pos);((SimpleEnergyStorage)m.energy()).setAmount(m.energy().getCapacity());});int step=Math.min(ticks,100);c.waitTicks(step);ticks-=step;
		}
		server.runOnServer(s->{
			var m=(FormMachineBlockEntity)s.overworld().getBlockEntity(pos);var output=m.container().getItem(m.form().firstOutputSlot());
			if(!output.is(item(result))||output.getCount()!=count||m.lane(0)!=null)throw new AssertionError("Exact paid output for "+recipeName);
			for(int i=0;i<m.form().recipeItemInputs();i++)if(!m.container().getItem(i).isEmpty())throw new AssertionError("All inputs paid exactly once: "+recipeName);
			if(expectedTool!=null) {int retained=0;for(int i=0;i<m.container().getContainerSize();i++)if(m.container().getItem(i).is(item(expectedTool)))retained+=m.container().getItem(i).getCount();if(retained!=1)throw new AssertionError("Exactly one retained formula/die: "+recipeName);}
		});
		c.takeScreenshot("art5_"+recipeName+"_finished");c.runOnClient(mc->mc.gui.screen().onClose());c.waitForScreen(null);
	}
}

package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.client.*;
import io.github.jimbozoomer.jugcraft.pharmacology.*;
import java.nio.file.Path;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.*;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.*;

/** Natural active-dose depletion, three-second empty-row grace and zero-strain held-drug behavior. */
public class PharmacologyHudZeroClientGameTests implements FabricClientGameTest {
	@Override public void runTest(ClientGameTestContext context) {
		boolean originalHidden=context.computeOnClient(c->c.gui.hud.isHidden());
		// Reproduce a preceding gallery's F1 state, then require this scenario to
		// establish its own visible HUD and restore the state it inherited.
		context.runOnClient(c->{if(!c.gui.hud.isHidden())c.gui.hud.toggle();});
		try {
		    visibleHudScenario(context);
		    context.runOnClient(c->{if(!c.gui.hud.isHidden())throw new AssertionError("Restore inherited hidden HUD state");});
		} finally {context.runOnClient(c->{if(c.gui.hud.isHidden()!=originalHidden)c.gui.hud.toggle();});}
	}
	private void visibleHudScenario(ClientGameTestContext context) {
		boolean hidden=context.computeOnClient(c->c.gui.hud.isHidden());
		var visuals=context.computeOnClient(c->ConcordanceClientOptions.drugVisuals());int scale=context.computeOnClient(c->c.options.guiScale().get());
		context.runOnClient(c->{c.getWindow().setWindowed(1280,800);c.options.guiScale().set(2);c.resizeGui();if(c.gui.hud.isHidden())c.gui.hud.toggle();ConcordanceClientOptions.setDrugVisuals(new DrugVisualOptions(false,75,70,60,70));});
		try(TestSingleplayerContext world=context.worldBuilder().adjustSettings(s->s.setGameMode(WorldCreationUiState.SelectedGameMode.SURVIVAL)).create()) {
			world.getConnection().waitForChunksRender();var server=world.getServer();BlockPos at=context.computeOnClient(c->c.player.blockPosition());
			server.runCommand("gamerule minecraft:send_command_feedback false");server.runCommand("gamerule minecraft:spawn_mobs false");server.runCommand("time set noon");server.runCommand("weather clear");
			server.runCommand("fill %d %d %d %d %d %d minecraft:smooth_stone".formatted(at.getX()-8,at.getY()-1,at.getZ()-8,at.getX()+8,at.getY()-1,at.getZ()+8));
			server.runCommand("tp @p %f %d %f 180 18".formatted(at.getX()+.5,at.getY(),at.getZ()+.5));
			server.runOnServer(s->{var p=s.getPlayerList().getPlayers().getFirst();p.getAbilities().invulnerable=true;p.getInventory().clearContent();});context.waitTicks(30);
			if(!StrainPillHud.lingering(12999,10000)||StrainPillHud.lingering(13000,10000)||StrainPillHud.lingering(0,-1))throw new AssertionError("Exactly three seconds of presentation grace");
			var heldZero=new StrainStatus(0,0,55,0,true,0,0);
			if(StrainPillHud.visible(heldZero,true,false,false,true)||!StrainPillHud.visible(heldZero,true,false,false,true,true)||StrainPillHud.visible(heldZero,false,false,false,true,true))throw new AssertionError("Zero hides even with a held drug/active dose; grace still respects death");
			pill(capture(context,"art6_zero_initial_hidden"),false);
			server.runOnServer(s->s.getPlayerList().getPlayers().getFirst().setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(PrecisionPharmacy.item("fentanyl"),2)));context.waitFor(c->StrainClient.state().nextCost()==55,100);
			pill(capture(context,"art6_zero_held_drug_stays_hidden"),false);tooltip(context,"+55 strain");
			server.runOnServer(s->{var p=s.getPlayerList().getPlayers().getFirst();new ItemStack(Pharmacology.CAFFEINE_DRINK).finishUsingItem(p.level(),p);});context.waitFor(c->StrainClient.state().tenths()>0&&StrainClient.state().dosing(),100);positivePill(capture(context,"art6_zero_new_positive_strain_reveals_row"));
			// Run the real online decay function until the paid caffeine dose's strain empties.
			// Its benefit must still be active: zero strain does not imply an ended dose.
			server.runOnServer(s->{var p=s.getPlayerList().getPlayers().getFirst();int limit=1200;while(Pharmacology.state(p).strainTenths()>0&&--limit>0)Pharmacology.tickSecond(p);if(limit==0||!Pharmacology.dosing(p)||Pharmacology.state(p).strainTenths()!=0)throw new AssertionError("Real active-dose strain depletion");StrainStatus.sync(p);});
			context.waitFor(c->StrainClient.state().tenths()==0&&StrainClient.state().dosing(),100);long depleted=context.computeOnClient(c->Util.getMillis());
			pill(capture(context,"art6_zero_empty_row_lingers"),true);
			context.waitFor(c->Util.getMillis()>=depleted+1400,2000);
			server.runOnServer(s->s.getPlayerList().getPlayers().getFirst().setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(PrecisionPharmacy.item("methylphenidate"),2)));context.waitFor(c->StrainClient.state().nextCost()==35,100);
			context.waitFor(c->Util.getMillis()>=depleted+3300,2000);
			pill(capture(context,"art6_zero_hidden_after_three_seconds_while_dosing_and_holding"),false);tooltip(context,"+35 strain");
			context.runOnClient(c->{if(!StrainClient.state().dosing()||StrainClient.state().tenths()!=0)throw new AssertionError("Benefits remain while the zero-strain row is hidden");});
			server.runOnServer(s->{var p=s.getPlayerList().getPlayers().getFirst();new ItemStack(Pharmacology.CAFFEINE_DRINK).finishUsingItem(p.level(),p);});context.waitFor(c->StrainClient.state().tenths()>0,100);positivePill(capture(context,"art6_zero_redose_reveals_row_again"));
			System.out.println("Strain zero-HUD checks passed: initially hidden, held-zero hidden with tooltips, real active-dose depletion, empty pills during three-second grace, hidden afterward despite active benefit/held-drug changes, and new strain reveals the row again.");
		} finally {context.runOnClient(c->{ConcordanceClientOptions.setDrugVisuals(visuals);c.options.guiScale().set(scale);c.resizeGui();if(c.gui.hud.isHidden()!=hidden)c.gui.hud.toggle();});}
	}
	private static Path capture(ClientGameTestContext c,String name) {c.runOnClient(mc->mc.gui.hud.getChat().clearMessages(true));c.waitTicks(2);return c.takeScreenshot(name);}
	private static void positivePill(Path path) {
		try {int pixel=ImageIO.read(path.toFile()).getRGB(664,708)&0xFFFFFF;if(pixel!=0xfff3ce)throw new AssertionError("Positive strain must render a filled capsule with HUD enabled, native pixel="+Integer.toHexString(pixel));}catch(java.io.IOException e){throw new AssertionError(e);}
	}
	private static void pill(Path path,boolean emptyVisible) {
		try {int pixel=ImageIO.read(path.toFile()).getRGB(664,708)&0xFFFFFF;if((pixel==0x656962)!=emptyVisible)throw new AssertionError("Expected empty capsule visible="+emptyVisible+", native pixel="+Integer.toHexString(pixel));}catch(java.io.IOException e){throw new AssertionError(e);}
	}
	private static void tooltip(ClientGameTestContext c,String expected) {c.runOnClient(mc->{var lines=new ArrayList<Component>();ItemTooltipCallback.EVENT.invoker().getTooltip(mc.player.getMainHandItem(),Item.TooltipContext.of(mc.level),TooltipFlag.NORMAL,lines);if(lines.stream().noneMatch(x->x.getString().contains(expected)))throw new AssertionError("Zero-strain tooltip retains forecast "+expected);});}
}

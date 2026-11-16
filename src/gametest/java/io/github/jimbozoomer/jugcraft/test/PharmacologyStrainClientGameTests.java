package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.client.*;
import io.github.jimbozoomer.jugcraft.pharmacology.*;
import java.util.ArrayList;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;

/** Real item use past 100, private HUD forecasts, fractional decay, combination tooltips and persistent penalties. */
public class PharmacologyStrainClientGameTests implements FabricClientGameTest {
	@Override public void runTest(ClientGameTestContext context) {
		var visuals=context.computeOnClient(c -> ConcordanceClientOptions.drugVisuals());
		int scale=context.computeOnClient(c -> c.options.guiScale().get());
		context.runOnClient(c -> {c.getWindow().setWindowed(1280,800);c.options.guiScale().set(2);c.resizeGui();});
		try(TestSingleplayerContext world=context.worldBuilder().adjustSettings(s -> s.setGameMode(WorldCreationUiState.SelectedGameMode.SURVIVAL)).create()) {
			world.getConnection().waitForChunksRender();var server=world.getServer();BlockPos at=context.computeOnClient(c -> c.player.blockPosition());
			server.runCommand("gamerule minecraft:send_command_feedback false");server.runCommand("gamerule minecraft:spawn_mobs false");server.runCommand("time set noon");server.runCommand("weather clear");
			server.runCommand("fill %d %d %d %d %d %d minecraft:smooth_stone".formatted(at.getX()-5,at.getY()-1,at.getZ()-5,at.getX()+5,at.getY()-1,at.getZ()+5));
			server.runCommand("tp @p %f %d %f 90 10".formatted(at.getX()+.5,at.getY(),at.getZ()+.5));
			server.runOnServer(s -> {
				var p=s.getPlayerList().getPlayers().getFirst();p.getAbilities().invulnerable=true;p.getInventory().clearContent();
				Pharmacology.drink(p,Pharmacology.CAFFEINE);for(int i=0;i<10;i++)Pharmacology.tickSecond(p);
				var state=Pharmacology.state(p);p.setAttached(Pharmacology.STATE,new DrugState(state.doses(),state.history(),935));
				p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(PrecisionPharmacy.item("methylphenidate"),4));
			});
			context.waitFor(c -> StrainClient.state().tenths()>900 && StrainClient.state().tenths()<1000 && StrainClient.state().nextCost()==35,100);
			context.runOnClient(c -> c.gui.hud.getChat().clearMessages(true));context.takeScreenshot("strain_a_safe_and_next_dose");
			context.getInput().holdKey(o -> o.keyUse);context.waitTicks(22);context.getInput().releaseKey(o -> o.keyUse);context.waitTicks(5);
			server.runOnServer(s -> {
				var p=s.getPlayerList().getPlayers().getFirst();
				if(p.getMainHandItem().getCount()!=3 || !p.hasEffect(PrecisionPharmacy.METHYLPHENIDATE.marker()) || p.hasEffect(Pharmacology.CAFFEINATED))throw new AssertionError("Native use must consume one replacement and end caffeine above 100");
				if(Math.abs(p.getAttributeValue(Attributes.MOVEMENT_SPEED)-.09)>.00001)throw new AssertionError("Overloaded movement penalty must be real during onset");
			});
			context.waitFor(c -> DrugStrain.band(StrainClient.state().tenths())==DrugStrain.Band.OVERLOADED,100);
			context.takeScreenshot("strain_a_overloaded_and_severe_forecast");
			final int before=context.computeOnClient(c -> StrainClient.state().tenths());
			server.runOnServer(s -> {var p=s.getPlayerList().getPlayers().getFirst();for(int i=0;i<20;i++)Pharmacology.tickSecond(p);});
			context.waitFor(c -> StrainClient.state().tenths()<=before-20 && c.player.hasEffect(net.minecraft.world.effect.MobEffects.HASTE),100);
			context.takeScreenshot("strain_a_falling_while_benefit_active");
			server.runOnServer(s -> {
				var p=s.getPlayerList().getPlayers().getFirst();Pharmacology.drink(p,((DrugDrinkItem)PrecisionPharmacy.item("moonshine")).profile());for(int i=0;i<10;i++)Pharmacology.tickSecond(p);
				var state=Pharmacology.state(p);p.setAttached(Pharmacology.STATE,new DrugState(state.doses(),state.history(),2050));
				p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(PrecisionPharmacy.item("fentanyl"),2));
				Pharmacology.tickSecond(p);
			});
			context.waitFor(c -> DrugStrain.band(StrainClient.state().tenths())==DrugStrain.Band.CRITICAL && StrainClient.state().nextCost()==80 && StrainClient.state().extraCost()==25,100);
			context.waitTicks(5);context.takeScreenshot("strain_a_critical_and_combination_forecast");
			context.runOnClient(c -> {
				var lines=new ArrayList<Component>();ItemTooltipCallback.EVENT.invoker().getTooltip(c.player.getMainHandItem(),Item.TooltipContext.of(c.level),TooltipFlag.NORMAL,lines);
				if(lines.stream().noneMatch(x -> x.getString().contains("+25 combination strain")))throw new AssertionError("Hover tooltip must explain combination strain");
				ConcordanceClientOptions.setDrugVisuals(new DrugVisualOptions(false,75,70,60,70));
			});
			context.waitTicks(5);
			server.runOnServer(s -> {
				var p=s.getPlayerList().getPlayers().getFirst();if(Math.abs(p.getAttributeValue(Attributes.MOVEMENT_SPEED)-.07)>.00001 || Math.abs(Pharmacology.aimSpreadMultiplier(p)-1.5F)>.00001)throw new AssertionError("Visual Off must preserve real critical penalties");
				int strain=Pharmacology.state(p).strainTenths();new ItemStack(Items.MILK_BUCKET).finishUsingItem(p.level(),p);
				if(Pharmacology.state(p).strainTenths()!=strain)throw new AssertionError("Milk must not erase strain");
			});
			context.waitFor(c -> !StrainClient.state().dosing() && StrainClient.state().extraCost()==0 && StrainClient.state().nextCost()==55,100);
			context.takeScreenshot("strain_a_milk_keeps_strain_faster_decay");
			final int previous=context.computeOnClient(c -> StrainClient.state().tenths());
			server.runOnServer(s -> {var p=s.getPlayerList().getPlayers().getFirst();for(int i=0;i<10;i++)Pharmacology.tickSecond(p);});
			context.waitFor(c -> StrainClient.state().tenths()<=previous-20,100);
			System.out.println("Strain client checks passed: native replacement beyond 100, forecasts, active/inactive decay, actual penalties, tooltips, visual Off and milk.");
		} finally {context.runOnClient(c -> {ConcordanceClientOptions.setDrugVisuals(visuals);c.options.guiScale().set(scale);c.resizeGui();});}
	}
}

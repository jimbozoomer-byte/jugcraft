package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.client.*;
import io.github.jimbozoomer.jugcraft.pharmacology.*;
import java.util.List;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;

/** Actual synchronized formulations and rendered shared filter, including accessibility and strain combinations. */
public class PharmacologyFocusClientGameTests implements FabricClientGameTest {
	@Override public void runTest(ClientGameTestContext context) {
		var saved=context.computeOnClient(c -> ConcordanceClientOptions.drugVisuals());
		boolean calm=context.computeOnClient(c -> ConcordanceClientOptions.reducedMotion());
		double nativeScale=context.computeOnClient(c -> c.options.screenEffectScale().get());
		int guiScale=context.computeOnClient(c -> c.options.guiScale().get());
		context.runOnClient(c -> {c.getWindow().setWindowed(1280,800);c.options.guiScale().set(2);c.resizeGui();options(DrugVisualOptions.DEFAULT,false);c.options.screenEffectScale().set(1.0);});
		try(TestSingleplayerContext world=context.worldBuilder().adjustSettings(s -> s.setGameMode(WorldCreationUiState.SelectedGameMode.SURVIVAL)).create()) {
			world.getConnection().waitForChunksRender();var server=world.getServer();BlockPos at=context.computeOnClient(c -> c.player.blockPosition());
			server.runCommand("gamerule minecraft:send_command_feedback false");server.runCommand("gamerule minecraft:spawn_mobs false");server.runCommand("gamerule minecraft:advance_time false");server.runCommand("time set noon");server.runCommand("weather clear");
			server.runCommand("fill %d %d %d %d %d %d minecraft:air".formatted(at.getX()-13,at.getY(),at.getZ()-13,at.getX()+13,at.getY()+12,at.getZ()+12));
			server.runCommand("fill %d %d %d %d %d %d minecraft:smooth_stone".formatted(at.getX()-13,at.getY()-2,at.getZ()-13,at.getX()+13,at.getY()-1,at.getZ()+12));
			server.runOnServer(s -> {
				var level=s.overworld();for(int x=-11;x<=11;x++)for(int y=0;y<7;y++)level.setBlockAndUpdate(at.offset(x,y,-8),(y==0||y==6?Blocks.BRICKS:x%4==0?Blocks.OAK_LOG:Blocks.QUARTZ_BLOCK).defaultBlockState());
				for(int x=-10;x<=10;x+=2)level.setBlockAndUpdate(at.offset(x,0,-4),x%4==0?Blocks.OAK_LEAVES.defaultBlockState().setValue(net.minecraft.world.level.block.LeavesBlock.PERSISTENT,true):Blocks.GLOWSTONE.defaultBlockState());
				var p=s.getPlayerList().getPlayers().getFirst();p.getAbilities().invulnerable=true;p.getInventory().clearContent();
			});
			server.runCommand("tp @p %f %d %f 180 0".formatted(at.getX()+.5,at.getY(),at.getZ()+7.5));
			context.waitTicks(30);context.runOnClient(c -> c.gui.hud.getChat().clearMessages(true));context.takeScreenshot("focus_b_00_sober");
			float[] aim=context.computeOnClient(c -> new float[]{c.player.getYRot(),c.player.getXRot()});
			var profiles=List.of(Pharmacology.CAFFEINE,Pharmacology.STIMULANT,PrecisionPharmacy.METHYLPHENIDATE,PrecisionPharmacy.AMPHETAMINE,PrecisionPharmacy.MODAFINIL,SterilePharmacy.ADRENALINE,
					ControlledRelease.CAFFEINE_XR,ControlledRelease.METHYLPHENIDATE_XR,ControlledRelease.AMPHETAMINE_XR,ControlledRelease.MODAFINIL_XR);
			for(var profile:profiles) {
				server.runOnServer(s -> {var p=s.getPlayerList().getPlayers().getFirst();reset(p);});context.waitTicks(60);
				server.runOnServer(s -> {var p=s.getPlayerList().getPlayers().getFirst();var item=net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(io.github.jimbozoomer.jugcraft.Jugcraft.id(profile.id().equals("caffeine")?"caffeine_drink":profile.id()));
					if(!(item instanceof DrugDrinkItem))throw new AssertionError("Expected actual formulation item: "+profile.id());
					new ItemStack(item).finishUsingItem(p.level(),p);
				});
				if(profile.onsetSeconds()>0) {
					context.waitTicks(10);context.runOnClient(c -> {if(StrainClient.state().visuals()!=0 || c.player.getActivePostEffects().contains(DrugVisuals.ALCOHOL))throw new AssertionError("No active visual during onset: "+profile.id());});
					server.runOnServer(s -> {var p=s.getPlayerList().getPlayers().getFirst();for(int i=0;i<profile.onsetSeconds();i++)Pharmacology.tickSecond(p);});
				}
				context.waitFor(c -> (StrainClient.state().visuals()&DrugPresentation.flag(profile))!=0,100);context.waitTicks(65);
				context.waitFor(c -> c.gameRenderer.getAppliedPostEffects().contains(DrugVisuals.ALCOHOL),100);
				context.runOnClient(c -> {
					var f=DrugVisuals.frame();int flag=DrugPresentation.flag(profile);
					if((flag==DrugPresentation.CAFFEINE || flag==DrugPresentation.METHYLPHENIDATE || flag==DrugPresentation.MODAFINIL)?f.focus()<.1F:f.energy()<.1F)throw new AssertionError("Expected distinct active presentation: "+profile.id());
					if(flag==DrugPresentation.METHYLPHENIDATE && f.tone()>=0 || flag==DrugPresentation.CAFFEINE && f.tone()<=0)throw new AssertionError("Cool focus and warm caffeine must differ");
				});
				context.takeScreenshot("focus_b_"+profile.id());
				if(profile==SterilePharmacy.ADRENALINE) {
					float peak=context.computeOnClient(c -> DrugVisuals.frame().energy());
					int cue=context.computeOnClient(c -> StrainClient.state().rushCue());
					server.runOnServer(s -> {var p=s.getPlayerList().getPlayers().getFirst();Pharmacology.drink(p,SterilePharmacy.ADRENALINE);
						if(p.getEffect(SterilePharmacy.ADRENALINE.marker()).getDuration()>34*20)throw new AssertionError("Early replacement must have a shorter native duration from tolerance");});
					context.waitFor(c -> StrainClient.state().rushCue()!=cue,100);context.waitTicks(330);
					context.runOnClient(c -> {if(DrugVisuals.frame().energy()>=peak*.6F || DrugVisuals.frame().energy()<=0)throw new AssertionError("Adrenaline rush settles while its real benefit continues");});context.takeScreenshot("focus_b_adrenaline_settled");
					server.runOnServer(s -> {var p=s.getPlayerList().getPlayers().getFirst();Pharmacology.drink(p,Pharmacology.CAFFEINE);for(int i=0;i<10;i++)Pharmacology.tickSecond(p);});context.waitTicks(45);
					context.runOnClient(c -> {if(DrugVisuals.frame().energy()>=peak*.6F)throw new AssertionError("Adding another family must not restart the Adrenaline rush");});
					server.runOnServer(s -> Pharmacology.drink(s.getPlayerList().getPlayers().getFirst(),SterilePharmacy.ADRENALINE));context.waitTicks(30);
					context.runOnClient(c -> {if(DrugVisuals.frame().energy()<peak*.9F)throw new AssertionError("A real Adrenaline replacement restarts its presentation");});
					server.runOnServer(s -> {var p=s.getPlayerList().getPlayers().getFirst();for(int i=0;i<45;i++)Pharmacology.tickSecond(p);});context.waitTicks(65);
					context.runOnClient(c -> {if(DrugVisuals.frame().fatigue()<=0 || DrugVisuals.frame().energy()!=0)throw new AssertionError("Natural Adrenaline expiry shows static fatigue rather than a lingering rush");});context.takeScreenshot("focus_b_adrenaline_fatigue");
				}
			}
			// Increasing warnings with no active drug: use a test-only pool fixture, then normal synchronization/decay.
			server.runOnServer(s -> reset(s.getPlayerList().getPlayers().getFirst()));context.waitTicks(60);
			float previous=0;
			for(int points:new int[]{110,135,175,230}) {
				server.runOnServer(s -> {var p=s.getPlayerList().getPlayers().getFirst();p.setAttached(Pharmacology.STATE,new DrugState(List.of(),List.of(),points*10));});
				context.waitTicks(65);float current=context.computeOnClient(c -> DrugVisuals.frame().warning());
				if(current<=previous)throw new AssertionError("Warning must increase with strain band");previous=current;
				context.takeScreenshot("focus_b_strain_"+points);
			}
			// Add real alcohol and an actual stimulant replacement: one composed pass, not piled-up filters.
			server.runOnServer(s -> {
				var p=s.getPlayerList().getPlayers().getFirst();Pharmacology.drink(p,ControlledRelease.MASTERWORK_RESERVE);for(int i=0;i<20;i++)Pharmacology.tickSecond(p);
				Pharmacology.drink(p,PrecisionPharmacy.AMPHETAMINE);for(int i=0;i<20;i++)Pharmacology.tickSecond(p);
				Pharmacology.drink(p,PrecisionPharmacy.METHYLPHENIDATE);for(int i=0;i<20;i++)Pharmacology.tickSecond(p);
			});context.waitTicks(125);
			context.runOnClient(c -> {var f=DrugVisuals.frame();if(f.strength()<=0 || f.focus()<=0 || f.warning()<=0 || f.jitter()<=0)throw new AssertionError("Alcohol, focus, critical warning and Jitters compose");if(c.player.getActivePostEffects().stream().filter(DrugVisuals.ALCOHOL::equals).count()!=1)throw new AssertionError("Only one shared post chain");});
			context.takeScreenshot("focus_b_combined");
			for(int i=0;i<12;i++) {context.waitTicks(4);context.takeScreenshot("focus_b_motion_%02d".formatted(i));}
			context.runOnClient(c -> options(DrugVisualOptions.DEFAULT,true));context.waitTicks(5);
			context.runOnClient(c -> {if(DrugVisuals.frame().motion()!=0 || DrugVisuals.frame().warning()<=0)throw new AssertionError("Reduced Motion keeps static warning and removes every animated channel");});context.takeScreenshot("focus_b_reduced_motion");
			context.runOnClient(c -> c.gui.setScreen(new InventoryScreen(c.player)));context.waitTicks(5);
			context.runOnClient(c -> {if(c.player.getActivePostEffects().contains(DrugVisuals.ALCOHOL))throw new AssertionError("Inventory stays sharp");});context.takeScreenshot("focus_b_sharp_inventory");context.runOnClient(c -> c.gui.screen().onClose());context.waitTicks(5);
			context.runOnClient(c -> options(new DrugVisualOptions(false,75,70,60,70),false));context.waitTicks(5);
			context.runOnClient(c -> {if(c.player.getActivePostEffects().contains(DrugVisuals.ALCOHOL))throw new AssertionError("Off removes all world visuals");});context.takeScreenshot("focus_b_visuals_off");
			server.runOnServer(s -> {var p=s.getPlayerList().getPlayers().getFirst();if(Pharmacology.aimSpreadMultiplier(p,true)<1 || !Pharmacology.activeProfile(p,PrecisionPharmacy.METHYLPHENIDATE))throw new AssertionError("Off keeps focus benefits and combined strain drawbacks");});
			context.runOnClient(c -> {options(DrugVisualOptions.DEFAULT,false);c.options.screenEffectScale().set(0.0);});context.waitTicks(5);
			context.runOnClient(c -> {if(c.player.getActivePostEffects().contains(DrugVisuals.ALCOHOL))throw new AssertionError("Native Distortion Effects zero disables all profiles");c.options.screenEffectScale().set(1.0);});
			var reload=context.computeOnClient(c -> c.reloadResourcePacks());context.waitFor(c -> reload.isDone(),2400);reload.join();context.waitTicks(10);
			context.waitFor(c -> c.gameRenderer.getAppliedPostEffects().contains(DrugVisuals.ALCOHOL),100);
			context.waitFor(c -> !c.isPaused() && c.gui.screen()==null && DrugVisuals.frame().warning()>0,100);
			context.runOnClient(c -> {if(Math.abs(net.minecraft.util.Mth.wrapDegrees(c.player.getYRot()-aim[0]))>.001F || Math.abs(c.player.getXRot()-aim[1])>.001F)throw new AssertionError("Cosmetics never turn the camera");});
			server.runOnServer(s -> {var p=s.getPlayerList().getPlayers().getFirst();new ItemStack(Items.MILK_BUCKET).finishUsingItem(p.level(),p);
				if(p.hasEffect(PrecisionPharmacy.INTOXICATION) || p.hasEffect(Pharmacology.JITTERS) || Pharmacology.activeProfile(p,PrecisionPharmacy.METHYLPHENIDATE) || Pharmacology.state(p).strainTenths()<2000)throw new AssertionError("Native milk ends dose markers and keeps critical strain on the server");});
			context.waitFor(c -> StrainClient.state().visuals()==0 && !c.player.hasEffect(PrecisionPharmacy.INTOXICATION) && !c.player.hasEffect(Pharmacology.JITTERS),100);context.waitTicks(65);
			context.takeScreenshot("focus_b_milk_keeps_warning");
			context.runOnClient(c -> {var f=DrugVisuals.frame();if(f.focus()!=0 || f.strength()!=0 || f.jitter()!=0 || f.warning()<=0)throw new AssertionError("Milk ends dose visuals but preserves strain warning: frame="+f+", state="+StrainClient.state()+", paused="+c.isPaused()+", screen="+c.gui.screen());});
			server.runOnServer(s -> reset(s.getPlayerList().getPlayers().getFirst()));context.waitTicks(65);
			context.runOnClient(c -> {if(c.player.getActivePostEffects().contains(DrugVisuals.ALCOHOL))throw new AssertionError("Safe and sober clears native filter");});context.takeScreenshot("focus_b_cleared");
			System.out.println("Batch B rendered checks passed: ten real formulations, onsets, all warning bands, alcohol composition, Jitters, aim, accessibility, menu, Off, reload, milk and cleanup.");
		} finally {context.runOnClient(c -> {options(saved,calm);c.options.screenEffectScale().set(nativeScale);c.options.guiScale().set(guiScale);c.resizeGui();});}
	}
	private static void reset(ServerPlayer p) {new ItemStack(Items.MILK_BUCKET).finishUsingItem(p.level(),p);p.removeAttached(Pharmacology.STATE);p.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);}
	private static void options(DrugVisualOptions options,boolean calm) {ConcordanceClientOptions.setDrugVisuals(options);ConcordanceClientOptions.set(ConcordanceClientOptions.hud(),calm,ConcordanceClientOptions.exactValues(),ConcordanceClientOptions.simpleJournal(),ConcordanceClientOptions.wornTrinkets(),ConcordanceClientOptions.intensity());}
}

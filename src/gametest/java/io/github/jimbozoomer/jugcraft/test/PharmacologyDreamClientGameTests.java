package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.client.*;
import io.github.jimbozoomer.jugcraft.concordance.sign.Presentation;
import io.github.jimbozoomer.jugcraft.pharmacology.*;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;

/** Real dream capsules, private recorded-site guidance, paid native study and shared display controls. */
public class PharmacologyDreamClientGameTests implements FabricClientGameTest {
	@Override public void runTest(ClientGameTestContext context) {
		var saved=context.computeOnClient(c -> ConcordanceClientOptions.drugVisuals());
		boolean calm=context.computeOnClient(c -> ConcordanceClientOptions.reducedMotion());
		var intensity=context.computeOnClient(c -> ConcordanceClientOptions.intensity());
		double nativeScale=context.computeOnClient(c -> c.options.screenEffectScale().get());
		int guiScale=context.computeOnClient(c -> c.options.guiScale().get());
		context.runOnClient(c -> {c.getWindow().setWindowed(1280,800);c.options.guiScale().set(2);c.resizeGui();options(DrugVisualOptions.DEFAULT,false);c.options.screenEffectScale().set(1.0);});
		try(TestSingleplayerContext world=context.worldBuilder().adjustSettings(s -> s.setGameMode(WorldCreationUiState.SelectedGameMode.SURVIVAL)).create()) {
			world.getConnection().waitForChunksRender();var server=world.getServer();BlockPos at=context.computeOnClient(c -> c.player.blockPosition());
			var site=at.offset(0,0,-1);var far=site.offset(30,0,0);var unknown=site.offset(4,0,0);
			server.runCommand("gamerule minecraft:send_command_feedback false");server.runCommand("gamerule minecraft:spawn_mobs false");server.runCommand("gamerule minecraft:advance_time false");server.runCommand("time set noon");server.runCommand("weather clear");
			server.runCommand("fill %d %d %d %d %d %d minecraft:air".formatted(at.getX()-13,at.getY(),at.getZ()-12,at.getX()+34,at.getY()+8,at.getZ()+12));
			server.runCommand("fill %d %d %d %d %d %d minecraft:smooth_stone".formatted(at.getX()-13,at.getY()-2,at.getZ()-12,at.getX()+34,at.getY()-1,at.getZ()+12));
			server.runOnServer(s -> {
				var level=s.overworld();for(int x=-11;x<=11;x++)for(int y=0;y<7;y++)level.setBlockAndUpdate(at.offset(x,y,-8),(y==0||y==6?Blocks.BRICKS:x%4==0?Blocks.OAK_LOG:Blocks.QUARTZ_BLOCK).defaultBlockState());
				for(var pos:java.util.List.of(site,far,unknown))level.setBlockAndUpdate(pos,DreamStudies.PLINTH.defaultBlockState());
				var p=s.getPlayerList().getPlayers().getFirst();p.getAbilities().invulnerable=true;p.getInventory().clearContent();p.removeAllEffects();p.removeAttached(Pharmacology.STATE);
			});
			server.runCommand("tp @p %f %d %f 180 12".formatted(at.getX()+.5,at.getY(),at.getZ()+3.5));context.waitTicks(25);
			context.runOnClient(c -> c.gui.hud.getChat().clearMessages(true));context.takeScreenshot("dream_d_00_sober");
			float[] aim=context.computeOnClient(c -> new float[]{c.player.getYRot(),c.player.getXRot()});
			server.runOnServer(s -> {var p=s.getPlayerList().getPlayers().getFirst();new ItemStack(PsychedelicPharmacy.item("psilocybin")).finishUsingItem(p.level(),p);});
			context.waitTicks(10);context.runOnClient(c -> {if(DreamSenseClient.state().mode()!=0 || c.player.getActivePostEffects().contains(DrugVisuals.ALCOHOL))throw new AssertionError("Psilocybin onset reveals neither site data nor active world filter");});
			server.runOnServer(s -> seconds(s.getPlayerList().getPlayers().getFirst(),10));
			context.waitFor(c -> DreamSenseClient.state().mode()==1 && !DreamSenseClient.state().folio(),120);context.waitTicks(65);
			context.runOnClient(c -> {if(DrugVisuals.frame().psilocybin()<=0 || DreamSenseClient.lastParticles()!=0)throw new AssertionError("Organic identity works without inventing a study target");});context.takeScreenshot("dream_d_psilocybin_needs_folio");
			server.runOnServer(s -> {var p=s.getPlayerList().getPlayers().getFirst();p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(DreamStudies.FOLIO));p.getInventory().setItem(1,new ItemStack(PsychedelicPharmacy.item("study_canvas"),4));});
			context.waitFor(c -> DreamSenseClient.state().folio() && DreamSenseClient.state().sites().isEmpty(),120);context.takeScreenshot("dream_d_unrecorded_sites_stay_hidden");
			server.runOnServer(s -> {
				var p=s.getPlayerList().getPlayers().getFirst();var body=p.position();
				p.setPos(far.getX()+.5,far.getY(),far.getZ()+2.5);if(!DreamStudies.discover(p,far))throw new AssertionError("Far site requires a real physical recording");
				p.setPos(body);if(!DreamStudies.discover(p,site))throw new AssertionError("Near site records from physical reach");
			});
			context.waitFor(c -> DreamSenseClient.state().sites().equals(java.util.List.of(site)) && DreamSenseClient.lastParticles()>0,160);
			context.runOnClient(c -> {if(DreamSenseClient.guidance()==null || DreamSenseClient.state().sites().contains(unknown) || Math.abs(c.player.getYRot()-aim[0])>.001F || Math.abs(c.player.getXRot()-aim[1])>.001F)throw new AssertionError("Only recorded visible plinth is marked; organic motion never turns the camera");});context.takeScreenshot("dream_d_psilocybin_recorded_wisps");
			context.runOnClient(c -> options(DrugVisualOptions.DEFAULT,true));context.waitTicks(25);
			context.runOnClient(c -> {if(DrugVisuals.frame().motion()!=0 || DrugVisuals.frame().psilocybin()<=0 || DreamSenseClient.lastParticles()!=0 || DreamSenseClient.guidance()==null)throw new AssertionError("Reduced Motion keeps static sight and essential guidance, without moving wisps");});context.takeScreenshot("dream_d_psilocybin_reduced_motion");
			context.runOnClient(c -> options(new DrugVisualOptions(false,75,70,60,70),false));context.waitTicks(25);
			context.runOnClient(c -> {if(c.player.getActivePostEffects().contains(DrugVisuals.ALCOHOL) || DreamSenseClient.guidance()==null || DreamSenseClient.lastParticles()!=0)throw new AssertionError("Off removes filter/wisps and keeps authorized navigation");});context.takeScreenshot("dream_d_sight_visuals_off_guidance");
			context.runOnClient(c -> c.gameMode.useItemOn(c.player,InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(site),Direction.SOUTH,site,false)));
			context.waitForScreen(DreamStudyScreen.class);context.waitFor(c -> ((DreamStudyMenu)c.player.containerMenu).ready(),120);
			int[] clues=context.computeOnClient(c -> {var m=(DreamStudyMenu)c.player.containerMenu;return new int[]{m.clue(0),m.clue(1),m.clue(2)};});
			context.takeScreenshot("dream_d_memory_clue_cards_visuals_off");
			context.clickScreenButton("screen.jugcraft.dream_study.glyph_"+((clues[0]+1)%4));context.waitTicks(5);
			context.runOnClient(c -> {if(((DreamStudyMenu)c.player.containerMenu).progress()!=0)throw new AssertionError("Wrong answer resets only the study attempt");});
			server.runOnServer(s -> {var p=s.getPlayerList().getPlayers().getFirst();if(p.getInventory().countItem(PsychedelicPharmacy.item("study_canvas"))!=4)throw new AssertionError("Wrong answer spends no canvas");});
			for(int i=0;i<3;i++) {context.clickScreenButton("screen.jugcraft.dream_study.glyph_"+clues[i]);context.waitTicks(5);if(i==0)context.takeScreenshot("dream_d_memory_next_step_highlight");}
			context.runOnClient(c -> {if(((DreamStudyMenu)c.player.containerMenu).status()!=2)throw new AssertionError("Native clue cards lead to a completed paid Memory lesson");});
			server.runOnServer(s -> {var p=s.getPlayerList().getPlayers().getFirst();if(p.getInventory().countItem(PsychedelicPharmacy.item("study_canvas"))!=3 || p.getInventory().countItem(PsychedelicPharmacy.item("lucid_imprint"))!=1)throw new AssertionError("One real canvas yields one real Lucid Imprint");});context.takeScreenshot("dream_d_memory_paid_reward");
			context.runOnClient(c -> c.gui.screen().onClose());context.waitForScreen(null);context.waitTicks(5);
			context.runOnClient(c -> options(DrugVisualOptions.DEFAULT,false));
			server.runOnServer(s -> {var p=s.getPlayerList().getPlayers().getFirst();new ItemStack(PsychedelicPharmacy.item("mescaline")).finishUsingItem(p.level(),p);});
			context.waitFor(c -> DreamSenseClient.state().mode()==0,100);context.waitTicks(65);
			context.runOnClient(c -> {if(DrugVisuals.frame().psilocybin()!=0 || DreamSenseClient.guidance()!=null)throw new AssertionError("Replacement ends old organic sight during the new onset");});
			server.runOnServer(s -> seconds(s.getPlayerList().getPlayers().getFirst(),15));
			context.waitFor(c -> DreamSenseClient.state().mode()==4,120);context.waitTicks(65);
			context.runOnClient(c -> {if(DrugVisuals.frame().mescaline()<=0 || DrugVisuals.frame().psilocybin()!=0)throw new AssertionError("Mescaline has a distinct crystalline identity");});context.takeScreenshot("dream_d_mescaline_crystalline");
			server.runOnServer(s -> s.overworld().removeBlock(site,false));
			context.waitFor(c -> DreamSenseClient.state().sites().equals(java.util.List.of(far)),120);
			context.runOnClient(c -> {var g=DreamSenseClient.guidance();if(g==null || g.distance()<24 || g.distance()>96 || g.sector()!=2 || DreamSenseClient.state().sites().contains(unknown))throw new AssertionError("Navigation selects the farther recorded site over a nearby unrecorded plinth: "+g);});context.takeScreenshot("dream_d_mescaline_recorded_navigation");
			context.runOnClient(c -> {Vec3 delta=Vec3.atCenterOf(far).subtract(c.player.getEyePosition());c.player.setYRot((float)Math.toDegrees(Math.atan2(-delta.x,delta.z)));options(DrugVisualOptions.DEFAULT,true);});context.waitTicks(25);
			context.runOnClient(c -> {if(DreamSenseClient.guidance().sector()!=0 || DrugVisuals.frame().motion()!=0 || DreamSenseClient.lastParticles()!=0)throw new AssertionError("Relative compass follows view direction and Reduced Motion removes wisps");});context.takeScreenshot("dream_d_mescaline_ahead_reduced_motion");
			context.runOnClient(c -> {options(DrugVisualOptions.DEFAULT,false);c.options.screenEffectScale().set(0.0);});context.waitTicks(25);
			context.runOnClient(c -> {if(c.player.getActivePostEffects().contains(DrugVisuals.ALCOHOL) || DreamSenseClient.guidance()==null || DreamSenseClient.lastParticles()!=0)throw new AssertionError("Native accessibility zero preserves essential recorded-site guidance");c.options.screenEffectScale().set(1.0);});
			var book=server.computeOnServer(s -> DreamStudies.folio(s.getPlayerList().getPlayers().getFirst()).copy());
			server.runOnServer(s -> s.getPlayerList().getPlayers().getFirst().setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY));
			context.waitFor(c -> DreamSenseClient.state().mode()==4 && !DreamSenseClient.state().folio() && DreamSenseClient.guidance()==null,120);context.takeScreenshot("dream_d_mescaline_folio_removed");
			server.runOnServer(s -> s.getPlayerList().getPlayers().getFirst().setItemInHand(InteractionHand.MAIN_HAND,book.copy()));
			context.waitFor(c -> DreamSenseClient.guidance()!=null,120);
			// The full owner asset collection can take over a minute to reload; gameplay deadlines are unchanged.
			var reload=context.computeOnClient(c -> c.reloadResourcePacks());context.waitFor(c -> reload.isDone(),2400);reload.join();
			context.waitFor(c -> !c.isPaused() && c.gui.screen()==null && DrugVisuals.frame().mescaline()>0 && DreamSenseClient.guidance()!=null,160);context.takeScreenshot("dream_d_mescaline_after_reload");
			server.runOnServer(s -> {var p=s.getPlayerList().getPlayers().getFirst();int before=Pharmacology.state(p).strainTenths();new ItemStack(Items.MILK_BUCKET).finishUsingItem(p.level(),p);if(Pharmacology.state(p).strainTenths()!=before)throw new AssertionError("Milk preserves accumulated strain");});
			context.waitFor(c -> DreamSenseClient.state().mode()==0,120);context.waitTicks(65);
			context.runOnClient(c -> {if(DreamSenseClient.guidance()!=null || DreamSenseClient.lastParticles()!=0 || c.player.getActivePostEffects().contains(DrugVisuals.ALCOHOL))throw new AssertionError("Milk removes private dream guidance, wisps and filter");});context.takeScreenshot("dream_d_cleansed");
			System.out.println("Batch D rendered checks passed: real Psilocybin/Mescaline, onsets, folio/recording gates, private wisps, no unseen-site discovery, paid clue cards, controls/Off, navigation direction, folio removal, reload and milk cleanup.");
		} finally {context.runOnClient(c -> {ConcordanceClientOptions.setDrugVisuals(saved);ConcordanceClientOptions.set(ConcordanceClientOptions.hud(),calm,ConcordanceClientOptions.exactValues(),ConcordanceClientOptions.simpleJournal(),ConcordanceClientOptions.wornTrinkets(),intensity);c.options.screenEffectScale().set(nativeScale);c.options.guiScale().set(guiScale);c.resizeGui();});}
	}
	private static void seconds(ServerPlayer p,int n) {for(int i=0;i<n;i++)Pharmacology.tickSecond(p);}
	private static void options(DrugVisualOptions options,boolean calm) {ConcordanceClientOptions.setDrugVisuals(options);ConcordanceClientOptions.set(ConcordanceClientOptions.hud(),calm,ConcordanceClientOptions.exactValues(),ConcordanceClientOptions.simpleJournal(),ConcordanceClientOptions.wornTrinkets(),Presentation.Intensity.FULL);}
}

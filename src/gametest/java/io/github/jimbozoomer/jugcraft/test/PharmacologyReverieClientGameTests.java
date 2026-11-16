package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.client.*;
import io.github.jimbozoomer.jugcraft.concordance.sign.Presentation;
import io.github.jimbozoomer.jugcraft.pharmacology.*;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.*;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;

/** Real advanced formulations, bounded view trails, cosmetic glimpses and paid native pattern reveals. */
public class PharmacologyReverieClientGameTests implements FabricClientGameTest {
	@Override public void runTest(ClientGameTestContext context) {
		var saved=context.computeOnClient(c -> ConcordanceClientOptions.drugVisuals());
		boolean calm=context.computeOnClient(c -> ConcordanceClientOptions.reducedMotion());
		var intensity=context.computeOnClient(c -> ConcordanceClientOptions.intensity());
		double nativeScale=context.computeOnClient(c -> c.options.screenEffectScale().get());
		int guiScale=context.computeOnClient(c -> c.options.guiScale().get());
		context.runOnClient(c -> {c.getWindow().setWindowed(1280,800);c.options.guiScale().set(2);c.resizeGui();options(DrugVisualOptions.DEFAULT,false);c.options.screenEffectScale().set(1.0);});
		try(TestSingleplayerContext world=context.worldBuilder().adjustSettings(s -> s.setGameMode(WorldCreationUiState.SelectedGameMode.SURVIVAL)).create()) {
			world.getConnection().waitForChunksRender();var server=world.getServer();BlockPos at=context.computeOnClient(c -> c.player.blockPosition());var site=at.offset(0,0,-1);
			server.runCommand("gamerule minecraft:send_command_feedback false");server.runCommand("gamerule minecraft:spawn_mobs false");server.runCommand("gamerule minecraft:advance_time false");server.runCommand("time set noon");server.runCommand("weather clear");
			server.runCommand("fill %d %d %d %d %d %d minecraft:air".formatted(at.getX()-13,at.getY(),at.getZ()-12,at.getX()+13,at.getY()+8,at.getZ()+12));
			server.runCommand("fill %d %d %d %d %d %d minecraft:smooth_stone".formatted(at.getX()-13,at.getY()-2,at.getZ()-12,at.getX()+13,at.getY()-1,at.getZ()+12));
			server.runOnServer(s -> {
				var level=s.overworld();for(int x=-11;x<=11;x++)for(int y=0;y<7;y++)level.setBlockAndUpdate(at.offset(x,y,-8),(y==0||y==6?Blocks.BRICKS:x%4==0?Blocks.OAK_LOG:Blocks.QUARTZ_BLOCK).defaultBlockState());
				level.setBlockAndUpdate(site,DreamStudies.PLINTH.defaultBlockState());var p=s.getPlayerList().getPlayers().getFirst();p.getAbilities().invulnerable=true;p.getInventory().clearContent();p.removeAllEffects();p.removeAttached(Pharmacology.STATE);
				p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(DreamStudies.FOLIO));p.getInventory().setItem(1,new ItemStack(PsychedelicPharmacy.item("study_canvas"),3));
			});
			server.runCommand("tp @p %f %d %f 180 12".formatted(at.getX()+.5,at.getY(),at.getZ()+3.5));context.waitTicks(25);
			server.runOnServer(s -> {if(!DreamStudies.discover(s.getPlayerList().getPlayers().getFirst(),site))throw new AssertionError("Advanced sight requires a physically recorded site");});
			context.runOnClient(c -> c.gui.hud.getChat().clearMessages(true));context.takeScreenshot("reverie_e_00_sober");
			Vec3 body=server.computeOnServer(s -> s.getPlayerList().getPlayers().getFirst().position());
			server.runOnServer(s -> {var p=s.getPlayerList().getPlayers().getFirst();new ItemStack(PsychedelicPharmacy.item("lsd")).finishUsingItem(p.level(),p);});
			context.waitTicks(10);context.runOnClient(c -> {if(DrugVisuals.frame().lsd()!=0 || DreamSenseClient.state().mode()!=0)throw new AssertionError("LSD onset reveals neither active patterns nor targets");});
			server.runOnServer(s -> seconds(s.getPlayerList().getPlayers().getFirst(),20));
			context.waitFor(c -> DreamSenseClient.state().mode()==2 && DrugVisuals.frame().lsd()>0,120);context.waitTicks(60);
			context.runOnClient(c -> {var f=DrugVisuals.frame();if(f.dmt()!=0 || f.glimpse()!=0 || trail(f)!=0 || DreamSenseClient.guidance()==null)throw new AssertionError("Still Pattern Sight has geometry and actual authorized guidance without a trail or Deep Reverie");});context.takeScreenshot("reverie_e_lsd_geometry");
			turn(context,2);context.runOnClient(c -> boundedTrail(DrugVisuals.frame()));context.takeScreenshot("reverie_e_lsd_turn_afterimages");
			context.waitTicks(10);context.runOnClient(c -> {if(trail(DrugVisuals.frame())!=0)throw new AssertionError("Short afterimages settle after turning stops");});
			turn(context,2);context.runOnClient(c -> c.player.setYRot(c.player.getYRot()+90));context.waitTicks(2);
			context.runOnClient(c -> {if(trail(DrugVisuals.frame())!=0)throw new AssertionError("Abrupt camera jumps must discard recent afterimages");c.player.setYRot(180);});context.waitTicks(10);
			context.runOnClient(c -> options(DrugVisualOptions.DEFAULT,true));turn(context,2);
			context.runOnClient(c -> {var f=DrugVisuals.frame();if(f.motion()!=0 || f.lsd()<=0 || trail(f)!=0 || DreamSenseClient.guidance()==null)throw new AssertionError("Reduced Motion keeps static Pattern Sight and real clues without trails");});context.takeScreenshot("reverie_e_lsd_reduced_motion");
			context.runOnClient(c -> options(new DrugVisualOptions(true,75,70,60,0),false));turn(context,2);
			context.runOnClient(c -> {if(DrugVisuals.frame().lsd()<=0 || trail(DrugVisuals.frame())!=0)throw new AssertionError("Zero double vision suppresses trails independently of geometric identity");});context.takeScreenshot("reverie_e_lsd_zero_doubling");
			context.runOnClient(c -> options(new DrugVisualOptions(true,75,70,0,70),false));turn(context,2);
			context.runOnClient(c -> {if(DrugVisuals.frame().motion()!=0 || trail(DrugVisuals.frame())!=0)throw new AssertionError("Zero motion suppresses trails and animation");});
			context.runOnClient(c -> {options(new DrugVisualOptions(false,75,70,60,70),false);c.player.setYRot(180);});context.waitTicks(10);
			context.runOnClient(c -> {if(c.player.getActivePostEffects().contains(DrugVisuals.ALCOHOL) || DreamSenseClient.guidance()==null || trail(DrugVisuals.frame())!=0)throw new AssertionError("Off leaves real Pattern Sight information available");});context.takeScreenshot("reverie_e_lsd_visuals_off");
			open(context,site);context.clickScreenButton("screen.jugcraft.dream_study.lesson_1");context.waitTicks(5);
			int[] mirror=context.computeOnClient(c -> {var m=(DreamStudyMenu)c.player.containerMenu;int[] v={m.clue(2),m.clue(1),m.clue(0)};for(int i=0;i<3;i++)if(m.trace(i)!=v[i])throw new AssertionError("Native Mirror cards reveal reverse input order");return v;});
			context.takeScreenshot("reverie_e_mirror_input_reveals_off");
			for(int value:mirror){context.clickScreenButton("screen.jugcraft.dream_study.glyph_"+value);context.waitTicks(5);}
			context.runOnClient(c -> {if(((DreamStudyMenu)c.player.containerMenu).status()!=2)throw new AssertionError("Revealed reverse inputs complete the actual paid native lesson");});
			server.runOnServer(s -> {var p=s.getPlayerList().getPlayers().getFirst();if(p.getInventory().countItem(PsychedelicPharmacy.item("pattern_imprint"))!=1 || p.getInventory().countItem(PsychedelicPharmacy.item("study_canvas"))!=2)throw new AssertionError("Mirror spends one canvas for one real Pattern Imprint");});context.takeScreenshot("reverie_e_mirror_paid_reward");close(context);
			context.runOnClient(c -> options(DrugVisualOptions.DEFAULT,false));
			server.runOnServer(s -> {var p=s.getPlayerList().getPlayers().getFirst();new ItemStack(PsychedelicPharmacy.item("dmt")).finishUsingItem(p.level(),p);});
			context.waitFor(c -> DreamSenseClient.state().mode()==0,120);context.waitTicks(65);
			context.runOnClient(c -> {if(DrugVisuals.frame().lsd()!=0 || DrugVisuals.frame().dmt()!=0 || DrugVisuals.frame().glimpse()!=0)throw new AssertionError("Family replacement removes old geometry before DMT onset completes");});
			server.runOnServer(s -> seconds(s.getPlayerList().getPlayers().getFirst(),20));context.waitFor(c -> DreamSenseClient.state().mode()==3 && DrugVisuals.frame().dmt()>0,120);context.waitTicks(60);
			context.runOnClient(c -> {var f=DrugVisuals.frame();if(f.lsd()!=0 || f.glimpse()<=0 || trail(f)!=0)throw new AssertionError("Deep Reverie has its distinct geometry and Clockwork preview without idle trails");});context.takeScreenshot("reverie_e_dmt_clockwork_geometry");
			turn(context,2);context.runOnClient(c -> boundedTrail(DrugVisuals.frame()));context.takeScreenshot("reverie_e_dmt_turn_afterimages");context.waitTicks(10);
			context.runOnClient(c -> {options(DrugVisualOptions.DEFAULT,true);c.player.setYRot(180);});context.waitTicks(10);
			context.runOnClient(c -> {var f=DrugVisuals.frame();if(f.motion()!=0 || f.glimpse()<=0 || trail(f)!=0 || DreamSenseClient.guidance()==null)throw new AssertionError("Reduced Motion preserves a static Clockwork preview and useful Deep Reverie");});context.takeScreenshot("reverie_e_dmt_static_clockwork");
			context.runOnClient(c -> {options(DrugVisualOptions.DEFAULT,false);c.options.screenEffectScale().set(0.0);});turn(context,2);
			context.runOnClient(c -> {if(c.player.getActivePostEffects().contains(DrugVisuals.ALCOHOL) || DrugVisuals.frame().glimpse()!=0 || trail(DrugVisuals.frame())!=0 || DreamSenseClient.guidance()==null)throw new AssertionError("Native accessibility zero removes cosmetics while preserving authorized study information");});context.takeScreenshot("reverie_e_dmt_native_accessibility_zero");
			context.runOnClient(c -> {c.options.screenEffectScale().set(1.0);options(new DrugVisualOptions(false,75,70,60,70),false);c.player.setYRot(180);});context.waitTicks(10);
			open(context,site);context.clickScreenButton("screen.jugcraft.dream_study.lesson_2");context.waitTicks(5);
			int[] reverie=context.computeOnClient(c -> {var m=(DreamStudyMenu)c.player.containerMenu;int[] v={(m.clue(0)+1)%4,(m.clue(1)+1)%4,(m.clue(2)+1)%4};for(int i=0;i<3;i++)if(m.trace(i)!=v[i])throw new AssertionError("Reverie cards reveal advanced successor inputs");return v;});context.takeScreenshot("reverie_e_reverie_input_reveals_off");
			for(int value:reverie){context.clickScreenButton("screen.jugcraft.dream_study.glyph_"+value);context.waitTicks(5);}
			context.runOnClient(c -> {if(((DreamStudyMenu)c.player.containerMenu).status()!=2)throw new AssertionError("Revealed successor inputs complete the actual native lesson");});
			server.runOnServer(s -> {var p=s.getPlayerList().getPlayers().getFirst();if(p.getInventory().countItem(PsychedelicPharmacy.item("oneiric_imprint"))!=1 || p.getInventory().countItem(PsychedelicPharmacy.item("study_canvas"))!=1 || p.position().distanceToSqr(body)>.001 || !p.level().dimension().equals(Level.OVERWORLD))throw new AssertionError("One real paid imprint, unchanged body and no dimension travel from a cosmetic glimpse");});context.takeScreenshot("reverie_e_reverie_paid_reward");close(context);
			context.runOnClient(c -> options(DrugVisualOptions.DEFAULT,false));context.waitTicks(10);
			server.runOnServer(s -> {var p=s.getPlayerList().getPlayers().getFirst();new ItemStack(Pharmacology.CAFFEINE_DRINK).finishUsingItem(p.level(),p);seconds(p,Pharmacology.CAFFEINE.onsetSeconds());});
			context.waitFor(c -> DrugVisuals.frame().dmt()>0 && DrugVisuals.frame().focus()>0,120);context.takeScreenshot("reverie_e_dmt_and_caffeine");
			var reload=context.computeOnClient(c -> c.reloadResourcePacks());context.waitFor(c -> reload.isDone(),2400);reload.join();
			context.waitFor(c -> !c.isPaused() && c.gui.screen()==null && DrugVisuals.frame().dmt()>0 && DreamSenseClient.guidance()!=null,160);world.getConnection().waitForChunksRender();context.waitTicks(10);context.takeScreenshot("reverie_e_after_reload");
			server.runOnServer(s -> {var p=s.getPlayerList().getPlayers().getFirst();int before=Pharmacology.state(p).strainTenths();new ItemStack(Items.MILK_BUCKET).finishUsingItem(p.level(),p);if(Pharmacology.state(p).strainTenths()!=before)throw new AssertionError("Milk cannot erase accumulated advanced-drug strain");});
			context.waitFor(c -> DreamSenseClient.state().mode()==0,120);context.waitTicks(65);
			context.runOnClient(c -> {var f=DrugVisuals.frame();if(f.lsd()!=0 || f.dmt()!=0 || f.glimpse()!=0 || trail(f)!=0 || DreamSenseClient.guidance()!=null)throw new AssertionError("Cleansing clears advanced geometry, private cues and every afterimage");});context.takeScreenshot("reverie_e_cleansed");
			System.out.println("Batch E rendered checks passed: real LSD/DMT onsets, distinct geometry/Clockwork preview, bounded directional afterimages, idle/jump/menu cleanup, independent controls, native paid Mirror/Reverie input reveals with cosmetics off, unchanged body/dimension, caffeine composition, reload and cleansing.");
		} finally {context.runOnClient(c -> {ConcordanceClientOptions.setDrugVisuals(saved);ConcordanceClientOptions.set(ConcordanceClientOptions.hud(),calm,ConcordanceClientOptions.exactValues(),ConcordanceClientOptions.simpleJournal(),ConcordanceClientOptions.wornTrinkets(),intensity);c.options.screenEffectScale().set(nativeScale);c.options.guiScale().set(guiScale);c.resizeGui();});}
	}
	private static void seconds(ServerPlayer p,int n) {for(int i=0;i<n;i++)Pharmacology.tickSecond(p);}
	private static void turn(ClientGameTestContext c,float step) {for(int i=0;i<6;i++){c.runOnClient(m -> m.player.setYRot(m.player.getYRot()+step));c.waitTicks(1);}}
	private static float trail(DrugVisuals.Frame f) {return Math.abs(f.trailAX())+Math.abs(f.trailAY())+Math.abs(f.trailBX())+Math.abs(f.trailBY())+Math.abs(f.trailCX())+Math.abs(f.trailCY());}
	private static void boundedTrail(DrugVisuals.Frame f) {if(trail(f)<=0 || Math.abs(f.trailAX())>.0181F || Math.abs(f.trailBX())>.0181F || Math.abs(f.trailCX())>.0181F || Math.abs(f.trailAY())>.0181F || Math.abs(f.trailBY())>.0181F || Math.abs(f.trailCY())>.0181F)throw new AssertionError("Actual turning creates a bounded three-sample afterimage: "+f);}
	private static void open(ClientGameTestContext c,BlockPos site) {c.runOnClient(m -> m.gameMode.useItemOn(m.player,InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(site),Direction.SOUTH,site,false)));c.waitForScreen(DreamStudyScreen.class);c.waitFor(m -> ((DreamStudyMenu)m.player.containerMenu).ready(),120);}
	private static void close(ClientGameTestContext c) {c.runOnClient(m -> m.gui.screen().onClose());c.waitForScreen(null);c.waitTicks(5);}
	private static void options(DrugVisualOptions options,boolean calm) {ConcordanceClientOptions.setDrugVisuals(options);ConcordanceClientOptions.set(ConcordanceClientOptions.hud(),calm,ConcordanceClientOptions.exactValues(),ConcordanceClientOptions.simpleJournal(),ConcordanceClientOptions.wornTrinkets(),Presentation.Intensity.FULL);}
}

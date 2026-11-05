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
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;

/** Real calm formulations, shared native shader, essential body HUD, controlled scout input and vulnerable body. */
public class PharmacologyCalmClientGameTests implements FabricClientGameTest {
	@Override public void runTest(ClientGameTestContext context) {
		var saved=context.computeOnClient(c -> ConcordanceClientOptions.drugVisuals());
		boolean calm=context.computeOnClient(c -> ConcordanceClientOptions.reducedMotion());
		double nativeScale=context.computeOnClient(c -> c.options.screenEffectScale().get());
		int guiScale=context.computeOnClient(c -> c.options.guiScale().get());
		context.runOnClient(c -> {c.getWindow().setWindowed(1280,800);c.options.guiScale().set(2);c.resizeGui();options(DrugVisualOptions.DEFAULT,false);c.options.screenEffectScale().set(1.0);});
		try(TestSingleplayerContext world=context.worldBuilder().adjustSettings(s -> s.setGameMode(WorldCreationUiState.SelectedGameMode.SURVIVAL)).create()) {
			world.getConnection().waitForChunksRender();var server=world.getServer();BlockPos at=context.computeOnClient(c -> c.player.blockPosition());
			server.runCommand("gamerule minecraft:send_command_feedback false");server.runCommand("gamerule minecraft:spawn_mobs false");server.runCommand("gamerule minecraft:advance_time false");server.runCommand("time set noon");server.runCommand("weather clear");
			server.runCommand("fill %d %d %d %d %d %d minecraft:air".formatted(at.getX()-28,at.getY(),at.getZ()-12,at.getX()+28,at.getY()+8,at.getZ()+12));
			server.runCommand("fill %d %d %d %d %d %d minecraft:smooth_stone".formatted(at.getX()-28,at.getY()-2,at.getZ()-12,at.getX()+28,at.getY()-1,at.getZ()+12));
			server.runOnServer(s -> {
				var level=s.overworld();for(int x=-11;x<=11;x++)for(int y=0;y<7;y++)level.setBlockAndUpdate(at.offset(x,y,-8),(y==0||y==6?Blocks.BRICKS:x%4==0?Blocks.OAK_LOG:Blocks.QUARTZ_BLOCK).defaultBlockState());
				for(int x=-10;x<=10;x+=2)level.setBlockAndUpdate(at.offset(x,0,-4),x%4==0?Blocks.OAK_LEAVES.defaultBlockState().setValue(net.minecraft.world.level.block.LeavesBlock.PERSISTENT,true):Blocks.GLOWSTONE.defaultBlockState());
				var p=s.getPlayerList().getPlayers().getFirst();p.getAbilities().invulnerable=true;p.getInventory().clearContent();
			});
			server.runCommand("tp @p %f %d %f 180 0".formatted(at.getX()+.5,at.getY(),at.getZ()+7.5));
			context.waitTicks(30);context.runOnClient(c -> c.gui.hud.getChat().clearMessages(true));context.takeScreenshot("calm_c_00_sober");
			server.runOnServer(s -> {var p=s.getPlayerList().getPlayers().getFirst();new ItemStack(SterilePharmacy.item("lidocaine")).finishUsingItem(p.level(),p);seconds(p,5);});
			context.waitFor(c -> c.player.getAbsorptionAmount()==8,100);context.takeScreenshot("calm_c_lidocaine_four_hearts");
			float previous=0;
			for(var profile:List.of(PsychedelicPharmacy.THC,ControlledRelease.FENTANYL_PATCH,SterilePharmacy.MORPHINE,PrecisionPharmacy.FENTANYL)) {
				server.runOnServer(s -> reset(s.getPlayerList().getPlayers().getFirst()));context.waitFor(c -> StrainClient.state().visuals()==0,100);context.waitTicks(65);
				server.runOnServer(s -> {var p=s.getPlayerList().getPlayers().getFirst();new ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(io.github.jimbozoomer.jugcraft.Jugcraft.id(profile.id()))).finishUsingItem(p.level(),p);});
				context.waitTicks(10);context.runOnClient(c -> {if(StrainClient.state().visuals()!=0 || c.player.getActivePostEffects().contains(DrugVisuals.ALCOHOL))throw new AssertionError("No calm presentation before onset: "+profile.id());});
				server.runOnServer(s -> seconds(s.getPlayerList().getPlayers().getFirst(),profile.onsetSeconds()));
				context.waitFor(c -> (StrainClient.state().visuals()&DrugPresentation.flag(profile))!=0,100);context.waitTicks(65);
				context.waitFor(c -> c.gameRenderer.getAppliedPostEffects().contains(DrugVisuals.ALCOHOL),100);
				float amount=context.computeOnClient(c -> profile==PsychedelicPharmacy.THC?DrugVisuals.frame().thc():DrugVisuals.frame().opioid());
				if(amount<=0 || profile!=PsychedelicPharmacy.THC && amount<=previous)throw new AssertionError("Distinct calm strength: "+profile.id());
				if(profile!=PsychedelicPharmacy.THC)previous=amount;
				context.takeScreenshot("calm_c_"+profile.id());
			}
			server.runOnServer(s -> {var p=s.getPlayerList().getPlayers().getFirst();new ItemStack(PsychedelicPharmacy.item("thc")).finishUsingItem(p.level(),p);seconds(p,10);});context.waitTicks(65);
			context.runOnClient(c -> {if(DrugVisuals.frame().thc()<=0 || DrugVisuals.frame().opioid()<=0)throw new AssertionError("THC and opioid channels compose");});context.takeScreenshot("calm_c_thc_and_opioid");
			server.runOnServer(s -> {var p=s.getPlayerList().getPlayers().getFirst();int before=Pharmacology.state(p).strainTenths();new ItemStack(SterilePharmacy.item("naloxone")).finishUsingItem(p.level(),p);
				if(Pharmacology.state(p).strainTenths()!=before || p.hasEffect(SterilePharmacy.DROWSINESS) || !Pharmacology.activeProfile(p,PsychedelicPharmacy.THC))throw new AssertionError("Naloxone ends only opioid benefits and keeps accounting");});
			context.waitFor(c -> (StrainClient.state().visuals()&(DrugPresentation.MORPHINE|DrugPresentation.FENTANYL|DrugPresentation.FENTANYL_PATCH))==0,100);context.waitTicks(65);
			context.runOnClient(c -> {if(DrugVisuals.frame().opioid()!=0 || DrugVisuals.frame().thc()<=0)throw new AssertionError("Naloxone leaves only the other family's visual");});context.takeScreenshot("calm_c_naloxone_keeps_thc");
			context.runOnClient(c -> options(new DrugVisualOptions(false,75,70,60,70),true));context.waitTicks(5);
			context.runOnClient(c -> {if(c.player.getActivePostEffects().contains(DrugVisuals.ALCOHOL))throw new AssertionError("Visual Off removes calm filter");});
			server.runOnServer(s -> {var p=s.getPlayerList().getPlayers().getFirst();if(!p.hasEffect(net.minecraft.world.effect.MobEffects.LUCK) || Math.abs(p.getAttributeValue(Attributes.BLOCK_BREAK_SPEED)-.9)>.00001)throw new AssertionError("Off preserves actual THC mechanics");});
			context.runOnClient(c -> options(DrugVisualOptions.DEFAULT,false));context.waitTicks(5);
			var reload=context.computeOnClient(c -> c.reloadResourcePacks());context.waitFor(c -> reload.isDone(),2400);reload.join();
			context.waitFor(c -> !c.isPaused() && c.gui.screen()==null && DrugVisuals.frame().thc()>0,100);context.takeScreenshot("calm_c_thc_after_reload");
			server.runOnServer(s -> reset(s.getPlayerList().getPlayers().getFirst()));context.waitFor(c -> StrainClient.state().visuals()==0,100);context.waitTicks(65);
			// Actual detached camera and ordinary input, with no changes to the body's real position or inventory.
			var body=context.computeOnClient(c -> c.player.position());
			server.runOnServer(s -> {var p=s.getPlayerList().getPlayers().getFirst();new ItemStack(PrecisionPharmacy.item("ketamine")).finishUsingItem(p.level(),p);p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(PrecisionPharmacy.item("beverage_culture_pack"),4));});
			context.waitFor(c -> EchoViewClient.active() && (StrainClient.state().visuals()&DrugPresentation.KETAMINE)!=0,100);context.waitTicks(65);
			context.runOnClient(c -> {if(c.getCameraEntity()==c.player || DrugVisuals.frame().echo()<=0 || EchoViewClient.guidance()==null)throw new AssertionError("Actual Echo camera gets native filter and essential guidance");});context.takeScreenshot("calm_c_echo_at_body");
			context.getInput().holdKey(o -> o.keyJump);context.waitTicks(20);context.getInput().releaseKey(o -> o.keyJump);context.waitTicks(5);
			context.runOnClient(c -> {var g=EchoViewClient.guidance();if(!g.vertical() || g.elevation()>-1)throw new AssertionError("A body directly below uses a vertical indication instead of a horizontal bearing: "+g);});context.takeScreenshot("calm_c_echo_body_directly_below");
			context.getInput().holdKey(o -> o.keySprint);context.waitTicks(20);context.getInput().releaseKey(o -> o.keySprint);context.waitTicks(5);
			context.getInput().holdKey(o -> o.keyRight);context.waitTicks(110);context.getInput().releaseKey(o -> o.keyRight);context.waitTicks(10);
			context.runOnClient(c -> {var g=EchoViewClient.guidance();if(g==null || g.distance()<=12 || g.distance()>24.05 || g.sector()!=6 || c.player.position().distanceToSqr(body)>.01)throw new AssertionError("Scout passes old range, body stays fixed, indicator points left: "+g);});context.takeScreenshot("calm_c_echo_beyond_old_range");
			context.getInput().holdKey(o -> o.keyJump);context.waitTicks(20);context.getInput().releaseKey(o -> o.keyJump);context.waitTicks(10);
			context.runOnClient(c -> {if(EchoViewClient.guidance().elevation()>-1)throw new AssertionError("Body height is below a raised scouting camera");c.player.setYRot(90);});context.waitTicks(5);
			context.runOnClient(c -> {if(EchoViewClient.guidance().sector()!=0)throw new AssertionError("Turning toward the body updates relative direction");options(DrugVisualOptions.DEFAULT,true);});context.waitTicks(5);
			context.runOnClient(c -> {if(DrugVisuals.frame().motion()!=0 || DrugVisuals.frame().echo()<=0)throw new AssertionError("Reduced Motion keeps static Echo identity");});context.takeScreenshot("calm_c_echo_body_ahead_below_reduced_motion");
			context.runOnClient(c -> options(new DrugVisualOptions(false,75,70,60,70),false));context.waitTicks(5);
			context.runOnClient(c -> {if(c.player.getActivePostEffects().contains(DrugVisuals.ALCOHOL) || !EchoViewClient.active() || EchoViewClient.guidance()==null)throw new AssertionError("Off keeps the real scouting view and body guidance");});context.takeScreenshot("calm_c_echo_visuals_off_body_hud");
			context.runOnClient(c -> {options(DrugVisualOptions.DEFAULT,false);c.options.screenEffectScale().set(0.0);});context.waitTicks(5);
			context.runOnClient(c -> {if(c.player.getActivePostEffects().contains(DrugVisuals.ALCOHOL) || EchoViewClient.guidance()==null)throw new AssertionError("Native accessibility zero preserves essential guidance");c.options.screenEffectScale().set(1.0);});
			context.getInput().holdKey(o -> o.keyDrop);context.waitTicks(1);context.getInput().releaseKey(o -> o.keyDrop);context.waitTicks(5);
			context.runOnClient(c -> {if(c.player.getMainHandItem().getCount()!=4)throw new AssertionError("Remote drop input cannot move body inventory");});
			context.getInput().holdKey(o -> o.keyRight);context.waitTicks(140);context.getInput().releaseKey(o -> o.keyRight);context.waitTicks(8);
			server.runOnServer(s -> {var p=s.getPlayerList().getPlayers().getFirst();double distance=EchoView.position(p).distanceTo(p.getEyePosition());if(distance<23.5 || distance>24.001 || p.position().distanceToSqr(body)>.001 || p.getMainHandItem().getCount()!=4)throw new AssertionError("Server owns the 24-block sphere and body inventory: "+distance);});context.takeScreenshot("calm_c_echo_at_24_block_boundary");
			context.getInput().holdKey(o -> o.keyShift);context.waitTicks(5);context.getInput().releaseKey(o -> o.keyShift);
			context.waitFor(c -> !EchoViewClient.active() && c.getCameraEntity()==c.player,100);
			context.waitFor(c -> EchoViewClient.guidance()==null && DrugVisuals.frame().echo()==0,100);
			context.runOnClient(c -> {if(EchoViewClient.guidance()!=null || DrugVisuals.frame().echo()!=0)throw new AssertionError("Return removes remote view tint and body guidance");});context.takeScreenshot("calm_c_echo_returned");
			server.runOnServer(s -> {var p=s.getPlayerList().getPlayers().getFirst();reset(p);new ItemStack(PrecisionPharmacy.item("ketamine")).finishUsingItem(p.level(),p);});context.waitFor(c -> EchoViewClient.active(),100);context.waitTicks(5);
			server.runOnServer(s -> {var p=s.getPlayerList().getPlayers().getFirst();p.getAbilities().invulnerable=false;p.hasChangedDimension();p.connection.handleAcceptPlayerLoad(new net.minecraft.network.protocol.game.ServerboundPlayerLoadedPacket());p.setInvulnerableTime(0);p.damageCooldownTime=0;p.hurtServer(p.level(),p.damageSources().fall(),2);
				if(EchoView.active(p) || p.getHealth()>=20 || Pharmacology.state(p).strainTenths()==0)throw new AssertionError("Real body damage ends the view and keeps strain");});
			context.waitFor(c -> !EchoViewClient.active() && c.getCameraEntity()==c.player,100);context.waitTicks(65);context.takeScreenshot("calm_c_body_damage_returns_view");
			server.runOnServer(s -> {var p=s.getPlayerList().getPlayers().getFirst();reset(p);new ItemStack(PrecisionPharmacy.item("ketamine")).finishUsingItem(p.level(),p);});context.waitFor(c -> EchoViewClient.active(),100);
			context.runOnClient(c -> c.gui.setScreen(new InventoryScreen(c.player)));context.waitFor(c -> !EchoViewClient.active(),100);
			context.runOnClient(c -> {if(c.player.getActivePostEffects().contains(DrugVisuals.ALCOHOL) || EchoViewClient.guidance()!=null)throw new AssertionError("Opening inventory ends scouting and keeps its interface sharp");});context.takeScreenshot("calm_c_inventory_returns_view");context.runOnClient(c -> c.gui.screen().onClose());
			server.runOnServer(s -> reset(s.getPlayerList().getPlayers().getFirst()));context.waitTicks(65);
			System.out.println("Batch C rendered checks passed: four-heart shield, actual THC/opioid doses and strengths, Naloxone, native reload, Echo filter, 24-block server limit, body direction/height, accessibility/Off, inventory guards, sneak/damage/menu returns and cleanup.");
		} finally {context.getInput().releaseKey(o -> o.keyRight);context.getInput().releaseKey(o -> o.keyJump);context.runOnClient(c -> {options(saved,calm);c.options.screenEffectScale().set(nativeScale);c.options.guiScale().set(guiScale);c.resizeGui();});}
	}
	private static void seconds(ServerPlayer p,int n) {for(int i=0;i<n;i++)Pharmacology.tickSecond(p);}
	private static void reset(ServerPlayer p) {EchoView.cancel(p);new ItemStack(Items.MILK_BUCKET).finishUsingItem(p.level(),p);p.removeAttached(Pharmacology.STATE);p.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);p.setHealth(20);p.setAbsorptionAmount(0);p.getAbilities().invulnerable=true;}
	private static void options(DrugVisualOptions options,boolean calm) {ConcordanceClientOptions.setDrugVisuals(options);ConcordanceClientOptions.set(ConcordanceClientOptions.hud(),calm,ConcordanceClientOptions.exactValues(),ConcordanceClientOptions.simpleJournal(),ConcordanceClientOptions.wornTrinkets(),ConcordanceClientOptions.intensity());}
}

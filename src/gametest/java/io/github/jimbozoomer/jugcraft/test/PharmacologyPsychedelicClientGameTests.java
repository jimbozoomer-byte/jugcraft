package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.client.DreamStudyScreen;
import io.github.jimbozoomer.jugcraft.client.FormMachineScreen;
import io.github.jimbozoomer.jugcraft.energy.SimpleEnergyStorage;
import io.github.jimbozoomer.jugcraft.machine.MachineBlock;
import io.github.jimbozoomer.jugcraft.machine.form.*;
import io.github.jimbozoomer.jugcraft.pharmacology.*;
import java.util.List;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.*;

/** Native block interaction, private clue sync, narrated buttons, real manufacturing and rendered crop/study art. */
public class PharmacologyPsychedelicClientGameTests implements FabricClientGameTest {
	@Override public void runTest(ClientGameTestContext context) {
		try (TestSingleplayerContext world = context.worldBuilder().adjustSettings(s -> s.setGameMode(WorldCreationUiState.SelectedGameMode.CREATIVE)).create()) {
			world.getConnection().waitForChunksRender(); var server = world.getServer();
			BlockPos at = context.computeOnClient(c -> c.player.blockPosition());
			server.runCommand("gamerule minecraft:send_command_feedback false"); server.runCommand("gamerule minecraft:spawn_mobs false");
			server.runCommand("time set noon"); server.runCommand("weather clear");
			server.runCommand("fill %d %d %d %d %d %d minecraft:air".formatted(at.getX() - 8, at.getY(), at.getZ() - 8, at.getX() + 12, at.getY() + 8, at.getZ() + 8));
			server.runCommand("fill %d %d %d %d %d %d minecraft:smooth_stone".formatted(at.getX() - 8, at.getY() - 2, at.getZ() - 8, at.getX() + 12, at.getY() - 1, at.getZ() + 8));
			server.runCommand("tp @p %f %d %f 180 12".formatted(at.getX() + .5, at.getY(), at.getZ() + 3.5));
			server.runOnServer(s -> {
				var level = s.overworld(); level.setBlockAndUpdate(at, DreamStudies.PLINTH.defaultBlockState());
				int i = 0;
				for (var block : PsychedelicPharmacy.CROPS.values()) {
					var pos = at.offset(-3 + i++ * 2, 0, -3); level.setBlockAndUpdate(pos.below(), Blocks.FARMLAND.defaultBlockState());
					level.setBlockAndUpdate(pos, ((CropBlock) block).getStateForAge(7));
				}
				var p = s.getPlayerList().getPlayers().getFirst();
				p.getInventory().clearContent(); p.getInventory().setItem(0, new ItemStack(DreamStudies.FOLIO));
				p.getInventory().setItem(1, new ItemStack(PsychedelicPharmacy.item("study_canvas"), 4));
				p.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(DreamStudies.FOLIO));
			});
			context.waitTicks(15);
			context.runOnClient(c -> c.gui.hud.getChat().clearMessages(true));
			context.getInput().pressKey(o -> o.keyToggleGui); context.waitTicks(2);
			context.takeScreenshot("perception_crops_and_study_plinth");
			context.getInput().pressKey(o -> o.keyToggleGui); context.waitTicks(2);
			open(context, at); context.waitForScreen(DreamStudyScreen.class); context.waitTicks(6);
			context.runOnClient(c -> { var m = (DreamStudyMenu)c.player.containerMenu; if (m.clue(0) != -1 || m.ready()) throw new AssertionError("Sober client must receive no hidden clue"); });
			context.takeScreenshot("dream_study_sober_hidden"); close(context);
			for (var profile : List.of(PsychedelicPharmacy.PSILOCYBIN, PsychedelicPharmacy.LSD, PsychedelicPharmacy.DMT)) {
				server.runOnServer(s -> {
					var p = s.getPlayerList().getPlayers().getFirst();
					// Advance ordinary online recovery between distinct formulations; preserve the history and tolerance.
					if (!Pharmacology.state(p).doses().isEmpty()) { Pharmacology.cancelFamily(p, "psychedelic"); for(int i=0;i<120;i++) Pharmacology.tickSecond(p); }
					if (!Pharmacology.drink(p, profile)) throw new AssertionError("Next legal formulation must start");
					for (int i=0;i<profile.onsetSeconds();i++) Pharmacology.tickSecond(p);
				});
				context.waitTicks(8); open(context, at); context.waitForScreen(DreamStudyScreen.class); context.waitTicks(5);
				int lesson = profile == PsychedelicPharmacy.PSILOCYBIN ? 0 : profile == PsychedelicPharmacy.LSD ? 1 : 2;
				context.clickScreenButton("screen.jugcraft.dream_study.lesson_" + lesson); context.waitTicks(5);
				int[] clues = context.computeOnClient(c -> { var m=(DreamStudyMenu)c.player.containerMenu; if(!m.ready())throw new AssertionError("Authorized client must see this layer");return new int[]{m.clue(0),m.clue(1),m.clue(2)}; });
				context.takeScreenshot("dream_study_" + profile.id() + "_clue");
				for(int i=0;i<3;i++) {
					int answer = lesson == 1 ? clues[2-i] : lesson == 2 ? (clues[i]+1)%4 : clues[i];
					context.clickScreenButton("screen.jugcraft.dream_study.glyph_" + answer); context.waitTicks(5);
				}
				context.runOnClient(c -> { if(((DreamStudyMenu)c.player.containerMenu).status()!=2)throw new AssertionError("Native button sequence must finish the paid lesson"); });
				context.takeScreenshot("dream_study_" + profile.id() + "_completed"); close(context);
			}
			server.runOnServer(s -> {
				var p=s.getPlayerList().getPlayers().getFirst();
				for(String id:List.of("lucid_imprint","pattern_imprint","oneiric_imprint"))
					if(p.getInventory().countItem(PsychedelicPharmacy.item(id))!=1)throw new AssertionError("Each native lesson awards exactly one imprint");
				if(p.getInventory().countItem(PsychedelicPharmacy.item("study_canvas"))!=1 || DreamStudies.sites(DreamStudies.folio(p)).size()!=1)
					throw new AssertionError("Exactly three paid canvases and one discovered persistent site");
				if(!p.level().dimension().equals(net.minecraft.world.level.Level.OVERWORLD) || p.position().distanceToSqr(Vec3.atCenterOf(at))>36)
					throw new AssertionError("DMT study keeps the body in place in the Overworld");
				Pharmacology.cancelFamily(p,"psychedelic");for(int i=0;i<120;i++)Pharmacology.tickSecond(p);
				if(!Pharmacology.drink(p,PsychedelicPharmacy.MESCALINE))throw new AssertionError("Mescaline starts after shared recovery");
				for(int i=0;i<15;i++)Pharmacology.tickSecond(p);
			});
			context.waitTicks(25);
			server.runOnServer(s -> {
				var p=s.getPlayerList().getPlayers().getFirst();
				if(p.containerMenu!=p.inventoryMenu || DreamStudies.resonance(p).isEmpty())throw new AssertionError("Normal screen closure must permit the recorded-site compass pulse");
				for(var block:PsychedelicPharmacy.CROPS.values()) {
					int index=List.copyOf(PsychedelicPharmacy.CROPS.values()).indexOf(block);
					if(!s.overworld().getBlockState(at.offset(-3+index*2,0,-3)).is(block))throw new AssertionError("All three crops must remain planted through the study session");
				}
			});
			context.takeScreenshot("mescaline_recorded_site_resonance");
			BlockPos separator = at.offset(6, 0, 0);
			server.runOnServer(s -> {
				var block=IndustrialForms.PHARMACEUTICAL_SEPARATOR; var state=block.defaultBlockState().setValue(MachineBlock.FACING,Direction.NORTH);
				s.overworld().setBlockAndUpdate(separator,state);block.setPlacedBy(s.overworld(),separator,state,null,ItemStack.EMPTY);
				var machine=(FormMachineBlockEntity)s.overworld().getBlockEntity(separator);
				var job=machine.recipes(s).stream().filter(e->e.id().getPath().endsWith("/perception_verify_psilocybin")).findFirst().orElseThrow();
				PharmacologyFactoryGameTests.feed(machine,job.recipe()); machine.selectRecipe(job.id());
				((SimpleEnergyStorage)machine.energy()).setAmount(machine.energy().getCapacity());
			});
			server.runCommand("tp @p %f %d %f 180 12".formatted(separator.getX()+.5,separator.getY(),separator.getZ()+3.5));
			server.runOnServer(s -> s.getPlayerList().getPlayers().getFirst().openMenu((FormMachineBlockEntity)s.overworld().getBlockEntity(separator)));
			context.waitForScreen(FormMachineScreen.class); context.waitTicks(10); context.takeScreenshot("perception_native_separator_job"); context.waitTicks(200);
			server.runOnServer(s -> {
				var m=(FormMachineBlockEntity)s.overworld().getBlockEntity(separator);var result=m.container().getItem(m.form().firstOutputSlot());
				if(!result.is(PsychedelicPharmacy.item("psilocybin_active"))||result.getCount()!=4)throw new AssertionError("World-ticked Separator must complete exactly four paid active");
			});
			context.takeScreenshot("perception_native_separator_finished"); close(context);
			server.runCommand("gamemode survival @p");
			server.runCommand("tp @p %f %d %f 90 -30".formatted(separator.getX()+.5, separator.getY(), separator.getZ()+3.5));
			server.runOnServer(s -> s.getPlayerList().getPlayers().getFirst().setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(PsychedelicPharmacy.item("thc"), 2)));
			context.waitTicks(8); context.getInput().holdKey(o -> o.keyUse); context.waitTicks(22); context.getInput().releaseKey(o -> o.keyUse);
			server.runOnServer(s -> {
				var p=s.getPlayerList().getPlayers().getFirst();
				if(p.getMainHandItem().getCount()!=1 || !p.hasEffect(PsychedelicPharmacy.THC.marker()) || p.hasEffect(PsychedelicPharmacy.RELAXED))
					throw new AssertionError("Native self-use consumes one capsule and starts onset without an early mining penalty");
				for(int i=0;i<10;i++)Pharmacology.tickSecond(p);
			});
			context.waitTicks(5);
			context.runOnClient(c -> {if(!c.player.hasEffect(net.minecraft.world.effect.MobEffects.LUCK)||!c.player.hasEffect(PsychedelicPharmacy.RELAXED))throw new AssertionError("Native Luck and the drug-only drawback must sync to the client after onset");});
			context.takeScreenshot("thc_native_consumption_and_effects");
		}
	}
	private static void open(ClientGameTestContext context, BlockPos pos) {
		context.runOnClient(c -> c.gameMode.useItemOn(c.player, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(pos), Direction.SOUTH, pos, false)));
	}
	private static void close(ClientGameTestContext context) {
		context.runOnClient(c -> c.gui.screen().onClose());
		context.waitForScreen(null); context.waitTicks(5);
	}
}

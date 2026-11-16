package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.client.*;
import io.github.jimbozoomer.jugcraft.concordance.sign.Presentation;
import io.github.jimbozoomer.jugcraft.pharmacology.*;
import java.util.List;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;

/** Real synchronized alcohol doses, native post-chain compilation, accessibility, cleansing and rendered evidence. */
public class PharmacologyVisualClientGameTests implements FabricClientGameTest {
	@Override public void runTest(ClientGameTestContext context) {
		var loader=net.fabricmc.loader.api.FabricLoader.getInstance();
		System.out.println("Alcohol renderer check: Sodium="+loader.isModLoaded("sodium")+", Iris="+loader.isModLoaded("iris")+", Fusion="+loader.isModLoaded("fusion"));
		var saved = context.computeOnClient(c -> ConcordanceClientOptions.drugVisuals());
		boolean calm = context.computeOnClient(c -> ConcordanceClientOptions.reducedMotion());
		double nativeScale = context.computeOnClient(c -> c.options.screenEffectScale().get());
		try (TestSingleplayerContext world = context.worldBuilder().adjustSettings(s -> s.setGameMode(WorldCreationUiState.SelectedGameMode.SURVIVAL)).create()) {
			world.getConnection().waitForChunksRender(); var server = world.getServer();
			BlockPos at = context.computeOnClient(c -> c.player.blockPosition());
			server.runCommand("gamerule minecraft:send_command_feedback false"); server.runCommand("gamerule minecraft:spawn_mobs false");
			server.runCommand("gamerule minecraft:advance_time false"); server.runCommand("time set noon"); server.runCommand("weather clear");
			server.runCommand("fill %d %d %d %d %d %d minecraft:air".formatted(at.getX()-13, at.getY(), at.getZ()-13, at.getX()+13, at.getY()+12, at.getZ()+12));
			server.runCommand("fill %d %d %d %d %d %d minecraft:smooth_stone".formatted(at.getX()-13, at.getY()-2, at.getZ()-13, at.getX()+13, at.getY()-1, at.getZ()+12));
			server.runOnServer(s -> {
				var level = s.overworld();
				for (int x=-11; x<=11; x++) for(int y=0; y<7; y++) {
					var block = y==0 || y==6 ? Blocks.BRICKS : x%4==0 ? Blocks.OAK_LOG : Blocks.QUARTZ_BLOCK;
					level.setBlockAndUpdate(at.offset(x,y,-8), block.defaultBlockState());
				}
				for(int x : new int[]{-9,-5,3,7}) for(int dx=0; dx<2; dx++) for(int y=2; y<=4; y++)
					level.setBlockAndUpdate(at.offset(x+dx,y,-8), Blocks.GLASS.defaultBlockState());
				for(int x=-10; x<=10; x+=2) {
					level.setBlockAndUpdate(at.offset(x,0,-10), Blocks.GLOWSTONE.defaultBlockState());
					level.setBlockAndUpdate(at.offset(x,0,-4), Blocks.OAK_LEAVES.defaultBlockState());
				}
				level.setBlockAndUpdate(at.offset(-4,0,-1), Blocks.BARREL.defaultBlockState());
				level.setBlockAndUpdate(at.offset(4,0,-1), Blocks.CRAFTING_TABLE.defaultBlockState());
				var player=s.getPlayerList().getPlayers().getFirst(); player.getAbilities().invulnerable=true;
				player.getInventory().clearContent();
			});
			server.runCommand("tp @p %f %d %f 180 0".formatted(at.getX()+.5, at.getY(), at.getZ()+7.5));
			context.runOnClient(c -> { options(DrugVisualOptions.DEFAULT, false); c.options.screenEffectScale().set(1.0); c.gui.hud.getChat().clearMessages(true); });
			context.waitTicks(30); world.getConnection().waitForChunksRender(); context.takeScreenshot("alcohol_00_sober");
			float[] aim = context.computeOnClient(c -> new float[]{c.player.getYRot(), c.player.getXRot()});
			float previous = 0;
			for(String id : List.of("moonshine", "refined_spirits", "reserve_whiskey", "high_proof_spirits", "masterwork_reserve")) {
				Item item = id.equals("masterwork_reserve") ? ControlledRelease.item(id) : PrecisionPharmacy.item(id);
				var profile = ((DrugDrinkItem)item).profile();
				server.runOnServer(s -> {
					var player=s.getPlayerList().getPlayers().getFirst();
					Pharmacology.cancelFamily(player,"intoxicant");
					for(int i=0;i<180;i++) Pharmacology.tickSecond(player);
					var drink=new ItemStack(item); player.setItemInHand(InteractionHand.MAIN_HAND, drink.finishUsingItem(player.level(),player));
					if(!player.hasEffect(profile.marker()) || !player.getMainHandItem().is(Items.GLASS_BOTTLE)) throw new AssertionError("Actual drink must start and return its bottle: "+id);
					player.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
				});
				context.waitTicks(50);
				context.runOnClient(c -> { if(c.player.getActivePostEffects().contains(DrugVisuals.ALCOHOL)) throw new AssertionError("Onset must not show active intoxication"); });
				server.runOnServer(s -> { var p=s.getPlayerList().getPlayers().getFirst(); for(int i=0;i<profile.onsetSeconds();i++) Pharmacology.tickSecond(p); });
				context.waitTicks(125);
				context.waitFor(c -> c.gameRenderer.getAppliedPostEffects().contains(DrugVisuals.ALCOHOL), 100);
				float current=context.computeOnClient(c -> DrugVisuals.frame().strength());
				if(current<=previous)throw new AssertionError("Each stronger alcohol must visibly increase intensity: "+id);
				previous=current; context.takeScreenshot("alcohol_"+id+"_peak");
				if(id.equals("masterwork_reserve")) {
					var times = new java.util.ArrayList<Long>();
					for(int i=0;i<24;i++) { context.waitTicks(3); times.add(context.computeOnClient(c -> c.level.getGameTime())); context.takeScreenshot("alcohol_motion_%02d".formatted(i)); }
					System.out.println("Alcohol motion game ticks: "+times);
				}
			}
			context.runOnClient(c -> {
				if(Math.abs(net.minecraft.util.Mth.wrapDegrees(c.player.getYRot()-aim[0]))>.001F || Math.abs(c.player.getXRot()-aim[1])>.001F)
					throw new AssertionError("Visual sway must never turn the player's aim: before="+aim[0]+","+aim[1]+" after="+c.player.getYRot()+","+c.player.getXRot());
				options(DrugVisualOptions.DEFAULT,true);
			});
			context.waitTicks(5);
			context.runOnClient(c -> { if(DrugVisuals.frame().motion()!=0 || DrugVisuals.frame().strength()<=0)throw new AssertionError("Reduced motion must remove sway and keep static presentation"); });
			context.takeScreenshot("alcohol_reduced_motion");
			double movement=server.computeOnServer(s -> s.getPlayerList().getPlayers().getFirst().getAttributeValue(Attributes.MOVEMENT_SPEED));
			context.runOnClient(c -> options(new DrugVisualOptions(false,75,70,60,70),false)); context.waitTicks(5);
			context.runOnClient(c -> { if(c.player.getActivePostEffects().contains(DrugVisuals.ALCOHOL) || c.gameRenderer.getAppliedPostEffects().contains(DrugVisuals.ALCOHOL))throw new AssertionError("Off must remove the native effect"); });
			server.runOnServer(s -> {
				var p=s.getPlayerList().getPlayers().getFirst();
				if(!p.hasEffect(PrecisionPharmacy.INTOXICATION) || Math.abs(p.getAttributeValue(Attributes.MOVEMENT_SPEED)-movement)>1e-8 || Pharmacology.strain(p)<=0)
					throw new AssertionError("Visual Off must preserve intoxication, movement penalty and strain");
			});
			context.takeScreenshot("alcohol_visuals_off_still_intoxicated");
			context.runOnClient(c -> { options(DrugVisualOptions.DEFAULT,false); c.options.screenEffectScale().set(0.0); }); context.waitTicks(5);
			context.runOnClient(c -> { if(c.player.getActivePostEffects().contains(DrugVisuals.ALCOHOL))throw new AssertionError("Native Distortion Effects Off must be respected"); c.options.screenEffectScale().set(1.0); });
			context.waitTicks(5);
			context.runOnClient(c -> c.gui.setScreen(new InventoryScreen(c.player))); context.waitTicks(5);
			context.runOnClient(c -> { if(c.player.getActivePostEffects().contains(DrugVisuals.ALCOHOL))throw new AssertionError("An open screen must suspend world distortion"); });
			context.takeScreenshot("alcohol_sharp_inventory"); context.runOnClient(c -> c.gui.screen().onClose()); context.waitTicks(5);
			context.runOnClient(c -> c.gui.setScreen(ConcordanceSettingsScreen.create(null))); context.waitTicks(5);
			context.runOnClient(c -> {
				String title=net.minecraft.network.chat.Component.translatable("screen.jugcraft.drug_visuals.title").getString();
				// Cloth tabs extend AbstractButton rather than Button, so Fabric's generic button helper cannot press them.
				var tab=c.gui.screen().children().stream().filter(w -> w instanceof me.shedaniel.clothconfig2.gui.ClothConfigTabButton)
						.map(w -> (me.shedaniel.clothconfig2.gui.ClothConfigTabButton)w).filter(w -> w.getMessage().getString().equals(title)).findFirst().orElseThrow();
				tab.onPress(new net.minecraft.client.input.MouseButtonInfo(0,0));
				if(!((me.shedaniel.clothconfig2.gui.ClothConfigScreen)c.gui.screen()).getSelectedCategory().getString().equals(title))throw new AssertionError("Drug visuals tab must open");
			});
			context.waitTicks(5); context.takeScreenshot("alcohol_display_controls");
			context.runOnClient(c -> c.gui.screen().onClose()); context.waitTicks(5);
			// Let the real active clock reach the lighter sustained phase, then exercise native shader resource reload.
			context.waitTicks(1200);
			context.runOnClient(c -> { if(DrugVisuals.frame().strength()>.36F || DrugVisuals.frame().strength()<.30F)throw new AssertionError("Long doses must settle to a lighter presentation"); });
			context.takeScreenshot("alcohol_masterwork_sustained");
			var reload=context.computeOnClient(c -> c.reloadResourcePacks());
			context.waitFor(c -> reload.isDone(), 2400); reload.join(); context.waitTicks(10);
			context.waitFor(c -> c.gameRenderer.getAppliedPostEffects().contains(DrugVisuals.ALCOHOL), 100);
			server.runOnServer(s -> {var p=s.getPlayerList().getPlayers().getFirst();new ItemStack(Items.MILK_BUCKET).finishUsingItem(p.level(),p);});
			context.waitTicks(60);
			context.runOnClient(c -> { if(c.player.getActivePostEffects().contains(DrugVisuals.ALCOHOL))throw new AssertionError("Milk must end the visual effect"); });
			context.takeScreenshot("alcohol_cleansed");
			System.out.println("Alcohol visual checks passed: all five real doses, onset, grades, native shader, aim, reduced motion, Off, native accessibility, menus, sustained phase, reload and milk.");
		} finally { context.runOnClient(c -> { options(saved,calm); c.options.screenEffectScale().set(nativeScale); }); }
	}
	private static void options(DrugVisualOptions options, boolean calm) {
		ConcordanceClientOptions.setDrugVisuals(options);
		ConcordanceClientOptions.set(ConcordanceClientOptions.hud(),calm,ConcordanceClientOptions.exactValues(),ConcordanceClientOptions.simpleJournal(),ConcordanceClientOptions.wornTrinkets(),ConcordanceClientOptions.intensity());
	}
}

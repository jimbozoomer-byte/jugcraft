package io.github.jimbozoomer.jugcraft.client;

import io.github.jimbozoomer.jugcraft.agriculture.HarvestMoon;
import io.github.jimbozoomer.jugcraft.agriculture.JugcraftAgriculture;
import io.github.jimbozoomer.jugcraft.agriculture.OpenCarvingPayload;
import io.github.jimbozoomer.jugcraft.agriculture.SpookyBurstPayload;
import io.github.jimbozoomer.jugcraft.fluid.JugcraftFluids;
import io.github.jimbozoomer.jugcraft.fluid.StoredFluid;
import io.github.jimbozoomer.jugcraft.guide.EngineersHandbookItem;
import io.github.jimbozoomer.jugcraft.kinetic.JugcraftKinetics;
import io.github.jimbozoomer.jugcraft.logistics.JugcraftLogistics;
import io.github.jimbozoomer.jugcraft.machine.JugcraftMachines;
import io.github.jimbozoomer.jugcraft.machine.MachineKind;
import io.github.jimbozoomer.jugcraft.prospecting.SurveyPayload;
import io.github.jimbozoomer.jugcraft.solar.JugcraftSolar;
import io.github.jimbozoomer.jugcraft.tools.JugcraftTools;
import io.github.jimbozoomer.jugcraft.weapons.JugcraftWeapons;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;

/**
 * Client entrypoint: machine and Cooking Pot menus to their screens, the wind turbine and belt renderers,
 * the handbook to its book, ore surveys to the prospector screen, carved and giant pumpkins to their renderers and carving screen,
 * pumpkin boats to theirs, gravestones to the renderer of their engravings, and Halloween's night creatures,
 * thrown pumpkins and landing markers to theirs; the Harvest Moon's state to the pumpkins' sparks.
 */
public final class JugcraftClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		for (MachineKind kind : MachineKind.values()) {
			MenuScreens.register(JugcraftMachines.menuType(kind), MachineScreen::new);
		}
		MenuScreens.register(JugcraftAgriculture.COOKING_POT_MENU, CookingPotScreen::new);
		BlockEntityRendererRegistry.register(JugcraftMachines.MACHINE_ENTITY, WindTurbineRenderer::new);
		BlockEntityRendererRegistry.register(JugcraftKinetics.BELT_PULLEY_ENTITY, BeltRenderer::new);
		BlockEntityRendererRegistry.register(JugcraftKinetics.SHAFT_ENTITY, KineticRotorRenderer::new);
		BlockEntityRendererRegistry.register(JugcraftLogistics.CONVEYOR_ENTITY, ConveyorRenderer::new);
		BlockEntityRendererRegistry.register(JugcraftTools.CHARGING_STATION_ENTITY, ChargingStationRenderer::new);
		RocketPackClient.register();
		EntityRendererRegistry.register(JugcraftWeapons.GRENADE, ThrownItemRenderer::new);
		PetroFluidsClient.register();
		RocketPackLayer.register();
		GhostSheetLayer.register();
		CostumeLayer.register();
		FacePaintLayer.register();
		BlockEntityRendererRegistry.register(JugcraftKinetics.HAND_CRANK_ENTITY, KineticRotorRenderer::new);
		BlockEntityRendererRegistry.register(JugcraftKinetics.ELECTRIC_MOTOR_ENTITY, KineticRotorRenderer::new);
		BlockEntityRendererRegistry.register(JugcraftKinetics.FLYWHEEL_ENTITY, KineticRotorRenderer::new);
		BlockEntityRendererRegistry.register(JugcraftFluids.TANK_ENTITY, GlassTankRenderer::new);
		BlockEntityRendererRegistry.register(JugcraftSolar.SOLAR_TRACKER_ENTITY, KineticRotorRenderer::new);
		BlockEntityRendererRegistry.register(JugcraftSolar.HELIOSTAT_ENTITY, KineticRotorRenderer::new);
		BlockEntityRendererRegistry.register(JugcraftKinetics.STEAM_ENGINE_ENTITY, KineticRotorRenderer::new);
		ItemTooltipCallback.EVENT.register((stack, context, flag, lines) -> {
			StoredFluid stored = stack.get(JugcraftFluids.STORED_FLUID);
			if (stored != null) {
				lines.add(stored.describe());
			}
		});
		EngineersHandbookItem.openScreen = () -> Minecraft.getInstance().gui.setScreen(new HandbookScreen());
		ClientPlayNetworking.registerGlobalReceiver(SurveyPayload.TYPE,
				(payload, context) -> Minecraft.getInstance().gui.setScreen(new ProspectorScreen(payload.readings())));
		BlockEntityRendererRegistry.register(JugcraftAgriculture.CARVED_PUMPKIN_ENTITY, CarvedPumpkinRenderer::new);
		BlockEntityRendererRegistry.register(JugcraftAgriculture.GIANT_PUMPKIN_ENTITY, GiantPumpkinRenderer::new);
		BlockEntityRendererRegistry.register(JugcraftAgriculture.SCARECROW_ENTITY, ScarecrowRenderer::new);
		BlockEntityRendererRegistry.register(JugcraftAgriculture.GRAVESTONE_ENTITY, GravestoneRenderer::new);
		BlockEntityRendererRegistry.register(JugcraftAgriculture.STRING_LIGHT_HOOK_ENTITY, StringLightsRenderer::new);
		BlockEntityRendererRegistry.register(JugcraftAgriculture.HAUNTED_PORTRAIT_ENTITY, HauntedPortraitRenderer::new);
		BlockEntityRendererRegistry.register(JugcraftAgriculture.FLOATING_CANDLE_ENTITY, FloatingCandleRenderer::new);
		BlockEntityRendererRegistry.register(JugcraftAgriculture.PUMPKIN_CRATE_ENTITY, PumpkinCrateRenderer::new);
		BlockEntityRendererRegistry.register(JugcraftAgriculture.ROCKING_CHAIR_ENTITY, RockingChairRenderer::new);
		BlockEntityRendererRegistry.register(JugcraftAgriculture.LURKING_EYES_ENTITY, LurkingEyesRenderer::new);
		BlockEntityRendererRegistry.register(JugcraftAgriculture.SILHOUETTE_WINDOW_ENTITY, SilhouetteWindowRenderer::new);
		BlockEntityRendererRegistry.register(JugcraftAgriculture.GIANT_FAKE_SPIDER_ENTITY, GiantFakeSpiderRenderer::new);
		BlockEntityRendererRegistry.register(JugcraftAgriculture.HAUNTED_CHANDELIER_ENTITY, HauntedChandelierRenderer::new);
		BlockEntityRendererRegistry.register(JugcraftAgriculture.PIPE_ORGAN_ENTITY, PipeOrganRenderer::new);
		BlockEntityRendererRegistry.register(JugcraftAgriculture.SUIT_OF_ARMOR_ENTITY, SuitOfArmorRenderer::new);
		BlockEntityRendererRegistry.register(JugcraftAgriculture.DUST_SHEET_ENTITY, DustSheetRenderer::new);
		BlockEntityRendererRegistry.register(JugcraftAgriculture.SPIRIT_MIRROR_ENTITY, SpiritMirrorRenderer::new);
		BlockEntityRendererRegistry.register(JugcraftAgriculture.TATTERED_CURTAINS_ENTITY, TatteredCurtainsRenderer::new);
		BlockEntityRendererRegistry.register(JugcraftAgriculture.CREEPY_DOLL_ENTITY, CreepyDollRenderer::new);
		BlockEntityRendererRegistry.register(JugcraftAgriculture.TESLA_COIL_ENTITY, TeslaCoilRenderer::new);
		BlockEntityRendererRegistry.register(JugcraftAgriculture.LAB_TABLE_ENTITY, LabTableRenderer::new);
		BlockEntityRendererRegistry.register(JugcraftAgriculture.SPECIMEN_JAR_ENTITY, SpecimenJarRenderer::new);
		BlockEntityRendererRegistry.register(JugcraftAgriculture.SARCOPHAGUS_ENTITY, MummySarcophagusRenderer::new);
		BlockEntityRendererRegistry.register(JugcraftAgriculture.RAVEN_PERCH_ENTITY, RavenRenderer::new);
		BlockEntityRendererRegistry.register(JugcraftAgriculture.BLACK_CAT_ENTITY, BlackCatRenderer::new);
		BlockEntityRendererRegistry.register(JugcraftAgriculture.INFLATABLE_ENTITY, InflatableRenderer::new);
		BlockEntityRendererRegistry.register(JugcraftAgriculture.PORCH_WITCH_ENTITY, PorchWitchRenderer::new);
		BlockEntityRendererRegistry.register(JugcraftAgriculture.WIND_CHIMES_ENTITY, BoneWindChimesRenderer::new);
		BlockEntityRendererRegistry.register(JugcraftAgriculture.WEATHERVANE_ENTITY, WeathervaneRenderer::new);
		BlockEntityRendererRegistry.register(JugcraftAgriculture.SPOOKY_SIGN_ENTITY, SpookySignRenderer::new);
		BlockEntityRendererRegistry.register(JugcraftAgriculture.GLOW_PAINT_ENTITY, GlowPaintRenderer::new);
		BlockEntityRendererRegistry.register(JugcraftAgriculture.BRAZIER_ENTITY, WitchFireBrazierRenderer::new);
		BlockEntityRendererRegistry.register(JugcraftAgriculture.SHADOW_LAMP_ENTITY, ShadowPuppetLampRenderer::new);
		BlockEntityRendererRegistry.register(JugcraftAgriculture.FLOATING_HAT_ENTITY, FloatingWitchHatRenderer::new);
		BlockEntityRendererRegistry.register(JugcraftAgriculture.JUMP_SCARE_ENTITY, JumpScareTrapRenderer::new);
		BlockEntityRendererRegistry.register(JugcraftAgriculture.BOWLING_SCOREBOARD_ENTITY, BowlingScoreboardRenderer::new);
		BlockEntityRendererRegistry.register(JugcraftAgriculture.DANCE_FLOOR_ENTITY, DanceFloorRenderer::new);
		BlockEntityRendererRegistry.register(JugcraftAgriculture.GHOST_BELL_ENTITY, GhostBellRenderer::new);
		BlockEntityRendererRegistry.register(JugcraftAgriculture.FORTUNE_TABLE_ENTITY, FortuneTellerTableRenderer::new);
		BlockEntityRendererRegistry.register(JugcraftAgriculture.BONFIRE_ENTITY, HalloweenBonfireRenderer::new);
		BlockEntityRendererRegistry.register(JugcraftAgriculture.WAX_POT_ENTITY, WaxPotRenderer::new);
		BlockEntityRendererRegistry.register(JugcraftAgriculture.AURA_CANDLE_ENTITY, AuraCandleRenderer::new);
		BlockEntityRendererRegistry.register(JugcraftAgriculture.CIDER_PRESS_ENTITY, CiderPressRenderer::new);
		BlockEntityRendererRegistry.register(JugcraftAgriculture.CANNING_KETTLE_ENTITY, CanningKettleRenderer::new);
		BlockEntityRendererRegistry.register(JugcraftAgriculture.PANTRY_SHELF_ENTITY, PantryShelfRenderer::new);
		ParticleProviderRegistry.getInstance().register(JugcraftAgriculture.FOG, FogParticle::provider);
		ParticleProviderRegistry.getInstance().register(JugcraftAgriculture.SPOOKY_SPARK, SpookySparkParticle::provider);
		BlockEntityRendererRegistry.register(JugcraftAgriculture.SHOW_LAUNCHER_ENTITY, ShowLauncherRenderer::new);
		EntityRendererRegistry.register(JugcraftAgriculture.SPOOKY_ROCKET, context -> new ThrownItemRenderer<>(context, 1.0F, true));
		EntityRendererRegistry.register(JugcraftAgriculture.SKY_LANTERN, SkyLanternRenderer::new);
		EntityRendererRegistry.register(JugcraftAgriculture.RESTLESS_SPIRIT, RestlessSpiritRenderer::new);
		BlockEntityRendererRegistry.register(JugcraftAgriculture.FEAST_TABLE_ENTITY, FeastTableRenderer::new);
		BlockEntityRendererRegistry.register(JugcraftAgriculture.CANDY_KETTLE_ENTITY, CandyKettleRenderer::new);
		BlockEntityRendererRegistry.register(JugcraftAgriculture.SPINNING_WHEEL_ENTITY, SpinningWheelRenderer::new);
		BlockEntityRendererRegistry.register(JugcraftAgriculture.HEARTH_OVEN_ENTITY, HearthOvenRenderer::new);
		ClientPlayNetworking.registerGlobalReceiver(SpookyBurstPayload.TYPE, (payload, context) -> SpookyBursts.receive(payload));
		ModelLayerRegistry.registerModelLayer(WispModel.LAYER, WispModel::createLayer);
		ModelLayerRegistry.registerModelLayer(CrowModel.LAYER, CrowModel::createLayer);
		ModelLayerRegistry.registerModelLayer(HayGolemModel.LAYER, HayGolemModel::createLayer);
		ModelLayerRegistry.registerModelLayer(HorsemanModel.LAYER, HorsemanModel::createLayer);
		EntityRendererRegistry.register(JugcraftAgriculture.WILL_O_WISP, WispRenderer::new);
		EntityRendererRegistry.register(JugcraftAgriculture.CROW, CrowRenderer::new);
		EntityRendererRegistry.register(JugcraftAgriculture.HAY_GOLEM, HayGolemRenderer::new);
		EntityRendererRegistry.register(JugcraftAgriculture.HEADLESS_HORSEMAN, HorsemanRenderer::new);
		EntityRendererRegistry.register(JugcraftAgriculture.FLYING_PUMPKIN, context -> new ThrownItemRenderer<>(context, 1.5F, false));
		EntityRendererRegistry.register(JugcraftAgriculture.FLAMING_PUMPKIN, context -> new ThrownItemRenderer<>(context, 1.5F, true));
		EntityRendererRegistry.register(JugcraftAgriculture.BOWLING_PUMPKIN, BowlingPumpkinRenderer::new);
		EntityRendererRegistry.register(JugcraftAgriculture.TOILET_PAPER_ROLL, ThrownItemRenderer::new);
		EntityRendererRegistry.register(JugcraftAgriculture.HAUNTED_HAYRIDE, HauntedHayrideRenderer::new);
		EntityRendererRegistry.register(JugcraftAgriculture.THROW_MARKER, ThrowMarkerRenderer::new);
		EntityRendererRegistry.register(JugcraftAgriculture.SEAT, SeatRenderer::new);
		ClientPlayNetworking.registerGlobalReceiver(HarvestMoon.Payload.TYPE, (payload, context) -> HarvestMoon.clientActive = payload.active());
		EntityRendererRegistry.register(JugcraftAgriculture.PUMPKIN_BARGE, PumpkinBoatRenderer::new);
		EntityRendererRegistry.register(JugcraftAgriculture.PUMPKIN_RACER, PumpkinBoatRenderer::new);
		ClientPlayNetworking.registerGlobalReceiver(OpenCarvingPayload.TYPE,
				(payload, context) -> Minecraft.getInstance().gui.setScreen(new CarvingScreen(payload)));
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
			HarvestMoon.clientActive = false;
			client.execute(CarvingTextures::clear);
		});
	}
}

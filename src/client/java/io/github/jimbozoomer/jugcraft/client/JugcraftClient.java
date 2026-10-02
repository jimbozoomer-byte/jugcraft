package io.github.jimbozoomer.jugcraft.client;

import io.github.jimbozoomer.jugcraft.agriculture.HarvestMoon;
import io.github.jimbozoomer.jugcraft.agriculture.JugcraftAgriculture;
import io.github.jimbozoomer.jugcraft.agriculture.OpenCarvingPayload;
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
		ParticleProviderRegistry.getInstance().register(JugcraftAgriculture.FOG, FogParticle::provider);
		ModelLayerRegistry.registerModelLayer(WispModel.LAYER, WispModel::createLayer);
		ModelLayerRegistry.registerModelLayer(HorsemanModel.LAYER, HorsemanModel::createLayer);
		EntityRendererRegistry.register(JugcraftAgriculture.WILL_O_WISP, WispRenderer::new);
		EntityRendererRegistry.register(JugcraftAgriculture.HEADLESS_HORSEMAN, HorsemanRenderer::new);
		EntityRendererRegistry.register(JugcraftAgriculture.FLYING_PUMPKIN, context -> new ThrownItemRenderer<>(context, 1.5F, false));
		EntityRendererRegistry.register(JugcraftAgriculture.FLAMING_PUMPKIN, context -> new ThrownItemRenderer<>(context, 1.5F, true));
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

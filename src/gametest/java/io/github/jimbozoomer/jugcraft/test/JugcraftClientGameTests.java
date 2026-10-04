package io.github.jimbozoomer.jugcraft.test;

import net.minecraft.world.phys.Vec3;
import net.minecraft.server.level.ServerPlayer;
import io.github.jimbozoomer.jugcraft.gear.JugcraftGrapple;
import io.github.jimbozoomer.jugcraft.gear.GrappleHook;
import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.client.HandbookScreen;
import io.github.jimbozoomer.jugcraft.client.MachineScreen;
import io.github.jimbozoomer.jugcraft.client.ProspectorScreen;
import io.github.jimbozoomer.jugcraft.energy.SimpleEnergyStorage;
import io.github.jimbozoomer.jugcraft.fluid.JugcraftFluids;
import io.github.jimbozoomer.jugcraft.kinetic.BeltPulleyBlockEntity;
import io.github.jimbozoomer.jugcraft.kinetic.DynamoBlock;
import io.github.jimbozoomer.jugcraft.kinetic.ElectricMotorBlock;
import io.github.jimbozoomer.jugcraft.kinetic.ElectricMotorBlockEntity;
import io.github.jimbozoomer.jugcraft.kinetic.HandCrankBlock;
import io.github.jimbozoomer.jugcraft.kinetic.JugcraftKinetics;
import io.github.jimbozoomer.jugcraft.kinetic.ShaftBlock;
import io.github.jimbozoomer.jugcraft.kinetic.SteamEngineBlock;
import io.github.jimbozoomer.jugcraft.logistics.ConveyorBlock;
import io.github.jimbozoomer.jugcraft.logistics.ConveyorBlockEntity;
import io.github.jimbozoomer.jugcraft.logistics.ConveyorSlopeBlock;
import io.github.jimbozoomer.jugcraft.logistics.JugcraftLogistics;
import io.github.jimbozoomer.jugcraft.machine.JugcraftMachines;
import io.github.jimbozoomer.jugcraft.machine.LargeMachineBlock;
import io.github.jimbozoomer.jugcraft.machine.MachineBlock;
import io.github.jimbozoomer.jugcraft.machine.MachineBlockEntity;
import io.github.jimbozoomer.jugcraft.machine.MachineKind;
import io.github.jimbozoomer.jugcraft.prospecting.OreSurvey;
import io.github.jimbozoomer.jugcraft.tools.ChargingStationBlock;
import io.github.jimbozoomer.jugcraft.tools.ChargingStationBlockEntity;
import io.github.jimbozoomer.jugcraft.tools.JugcraftTools;
import io.github.jimbozoomer.jugcraft.world.ArcadeCabinetBlock;
import io.github.jimbozoomer.jugcraft.world.PixelHollows;
import io.github.jimbozoomer.jugcraft.world.RetroTrader;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.client.CameraType;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Block;
import io.github.jimbozoomer.jugcraft.chemistry.FoamSprayerItem;
import io.github.jimbozoomer.jugcraft.chemistry.ConstructionChemistry;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

/**
 * Client game tests: a real game client with real rendering (CI runs it with Mesa). It builds a
 * showroom of every machine, opens a machine screen and the Engineer's Handbook, and saves
 * screenshots, so the looks can be checked from actual game renders rather than previews.
 */
public class JugcraftClientGameTests implements FabricClientGameTest {
	@Override
	public void runTest(ClientGameTestContext context) {
		if (GuideScreenshotGameTests.active()) {
			return;
		}
		try (TestSingleplayerContext singleplayer = context.worldBuilder()
				.adjustSettings(creator -> creator.setGameMode(WorldCreationUiState.SelectedGameMode.CREATIVE)).create()) {
			singleplayer.getConnection().waitForChunksRender();
			BlockPos origin = context.computeOnClient(client -> BlockPos.containing(client.player.position()));
			int x = origin.getX();
			int y = origin.getY();
			int z = origin.getZ();
			TestServerContext server = singleplayer.getServer();
			// No command feedback in chat, so it does not cover the screenshots (the rule's name differs across
			// versions; whichever does not exist just fails).
			server.runCommand("gamerule sendCommandFeedback false");
			server.runCommand("gamerule minecraft:send_command_feedback false");
			server.runCommand("time set noon");
			server.runCommand("weather clear");
			// A floor under the showroom and the scenes, with the air above it cleared. Set block by block on the
			// server: a fill command cannot reach chunks that are not loaded yet, and the multi-block row runs far
			// beyond the player.
			int end = x + 20 + largeRowLength() + 8;
			server.runOnServer(minecraft -> clearFloor(minecraft.overworld(), new BlockPos(x - 26, y, z - 10), new BlockPos(end, y + 14, z + 12)));
			server.runOnServer(minecraft -> buildShowroom(minecraft.overworld(), new BlockPos(x, y, z - 5)));

			// Hide the HUD, hand and chat so the screenshots show only the machines. In 26.3 this is the "toggle GUI"
			// key mapping (F1 by default); pressing the raw F1 key code did not reach it.
			context.getInput().pressKey(options -> options.keyToggleGui);

			// One-block machines, facing the camera, in two halves.
			server.runCommand("tp @p %d %d %d 180 25".formatted(x - 4, y, z - 2));
			context.waitTicks(40);
			singleplayer.getConnection().waitForChunksRender();
			context.takeScreenshot("jugcraft_machines_1");
			server.runCommand("tp @p %d %d %d 180 25".formatted(x + 3, y, z - 2));
			context.waitTicks(20);
			singleplayer.getConnection().waitForChunksRender();
			context.takeScreenshot("jugcraft_machines_2");

			// A running kinetic line: steam engine, shafts, a gearbox with a hand crank, a dynamo and a crusher.
			// Its own row behind the multi-block camera (the showroom rows are at z - 5).
			server.runOnServer(minecraft -> buildKineticLine(minecraft.overworld(), new BlockPos(x, y, z + 2)));
			server.runCommand("tp @p %d %d %d 180 35".formatted(x + 2, y + 2, z + 7));
			context.waitTicks(60);
			singleplayer.getConnection().waitForChunksRender();
			context.takeScreenshot("jugcraft_kinetics");

			// An electric motor driving a pulley, belted up to a second pulley that drives a crusher through a shaft.
			server.runOnServer(minecraft -> buildBeltLine(minecraft.overworld(), new BlockPos(x + 12, y, z + 2)));
			server.runCommand("tp @p %d %d %d 180 5".formatted(x + 14, y + 1, z + 7));
			context.waitTicks(60);
			singleplayer.getConnection().waitForChunksRender();
			context.takeScreenshot("jugcraft_belts");

			// A conveyor line with items riding it, a splitter at the end and a chest on each of its outputs.
			server.runOnServer(minecraft -> buildConveyorLine(minecraft.overworld(), new BlockPos(x + 6, y, z + 2)));
			server.runCommand("tp @p %d %d %d 180 40".formatted(x + 8, y + 2, z + 5));
			context.waitTicks(10);
			singleplayer.getConnection().waitForChunksRender();
			context.takeScreenshot("jugcraft_conveyors");

			// Conveyor slopes: items climb onto a raised conveyor and come back down.
			server.runOnServer(minecraft -> buildSlopeLine(minecraft.overworld(), new BlockPos(x + 5, y, z + 4)));
			server.runCommand("tp @p %d %d %d 150 25".formatted(x + 11, y + 2, z + 8));
			context.waitTicks(12);
			singleplayer.getConnection().waitForChunksRender();
			context.takeScreenshot("jugcraft_conveyor_slopes");

			// Powered tools: three charging stations holding the drill, the chainsaw and the rocket pack.
			server.runOnServer(minecraft -> buildToolStations(minecraft.overworld(), new BlockPos(x - 8, y, z + 2)));
			server.runCommand("tp @p %d %d %d 180 10".formatted(x - 6, y, z + 6));
			context.waitTicks(20);
			singleplayer.getConnection().waitForChunksRender();
			context.takeScreenshot("jugcraft_charging_stations");

			// The drill in hand (first person, with the hotbar showing the three tools), then the worn rocket pack
			// from behind.
			server.runCommand("item replace entity @p hotbar.0 with jugcraft:mining_drill");
			server.runCommand("item replace entity @p hotbar.1 with jugcraft:chainsaw");
			server.runCommand("item replace entity @p hotbar.2 with jugcraft:rocket_pack");
			server.runCommand("item replace entity @p armor.chest with jugcraft:rocket_pack");
			server.runCommand("tp @p %d %d %d 180 20".formatted(x - 6, y, z + 5));
			// Show the HUD again (the first hotbar slot, the drill, is selected in a new world).
			context.getInput().pressKey(options -> options.keyToggleGui);
			context.waitTicks(20);
			context.takeScreenshot("jugcraft_drill_in_hand");
			context.runOnClient(client -> client.options.setCameraType(CameraType.THIRD_PERSON_BACK));
			context.waitTicks(20);
			context.takeScreenshot("jugcraft_rocket_pack_worn");
			// Batch 25: kaiserpunk steel armor with a steel paxel in hand, from the front; then steampunk bronze, from the
			// front and from behind (its boiler).
			server.runCommand("item replace entity @p armor.head with jugcraft:steel_helmet");
			server.runCommand("item replace entity @p armor.chest with jugcraft:steel_chestplate");
			server.runCommand("item replace entity @p armor.legs with jugcraft:steel_leggings");
			server.runCommand("item replace entity @p armor.feet with jugcraft:steel_boots");
			server.runCommand("item replace entity @p hotbar.0 with jugcraft:steel_paxel");
			context.runOnClient(client -> client.options.setCameraType(CameraType.THIRD_PERSON_FRONT));
			context.waitTicks(20);
			context.takeScreenshot("jugcraft_steel_armor_worn");
			server.runCommand("item replace entity @p armor.head with jugcraft:bronze_helmet");
			server.runCommand("item replace entity @p armor.chest with jugcraft:bronze_chestplate");
			server.runCommand("item replace entity @p armor.legs with jugcraft:bronze_leggings");
			server.runCommand("item replace entity @p armor.feet with jugcraft:bronze_boots");
			context.waitTicks(20);
			context.takeScreenshot("jugcraft_bronze_armor_worn");
			context.runOnClient(client -> client.options.setCameraType(CameraType.THIRD_PERSON_BACK));
			context.waitTicks(20);
			context.takeScreenshot("jugcraft_bronze_armor_back");
			// Batch 27: scuba mask and tank, free runners and the power katana, from the front; then the sixteen plastic
			// blocks in a wall, two high.
			server.runCommand("item replace entity @p armor.head with jugcraft:scuba_mask");
			server.runCommand("item replace entity @p armor.chest with jugcraft:scuba_tank");
			server.runCommand("item replace entity @p armor.legs with minecraft:air");
			server.runCommand("item replace entity @p armor.feet with jugcraft:free_runners");
			server.runCommand("item replace entity @p hotbar.0 with jugcraft:power_katana");
			context.runOnClient(client -> client.options.setCameraType(CameraType.THIRD_PERSON_FRONT));
			context.waitTicks(20);
			context.takeScreenshot("jugcraft_scuba_gear_worn");
			// Batch 28: the powered exosuit, Vanguard from the front and behind, then Ronin with its katana.
			for (String[] slot : new String[][] {{"head", "helmet"}, {"chest", "chestplate"}, {"legs", "leggings"},
					{"feet", "boots"}}) {
				server.runCommand("item replace entity @p armor.%s with jugcraft:exosuit_%s".formatted(slot[0], slot[1]));
			}
			server.runCommand("item replace entity @p hotbar.0 with jugcraft:power_katana");
			context.waitTicks(20);
			context.takeScreenshot("jugcraft_exosuit_vanguard");
			context.runOnClient(client -> client.options.setCameraType(CameraType.THIRD_PERSON_BACK));
			context.waitTicks(20);
			context.takeScreenshot("jugcraft_exosuit_vanguard_back");
			for (String[] slot : new String[][] {{"head", "helmet"}, {"chest", "chestplate"}, {"legs", "leggings"},
					{"feet", "boots"}}) {
				server.runCommand("item replace entity @p armor.%s with jugcraft:ronin_exosuit_%s".formatted(slot[0], slot[1]));
			}
			server.runCommand("item replace entity @p hotbar.0 with jugcraft:ronin_katana");
			context.runOnClient(client -> client.options.setCameraType(CameraType.THIRD_PERSON_FRONT));
			context.waitTicks(20);
			context.takeScreenshot("jugcraft_exosuit_ronin");
			// Batch 30: the pneumatic grapple in hand, its hook hanging out on its slack line beside the player.
			server.runCommand("item replace entity @p hotbar.0 with jugcraft:pneumatic_grapple");
			server.runOnServer(minecraft -> {
				ServerPlayer owner = minecraft.getPlayerList().getPlayers().get(0);
				Vec3 look = owner.getLookAngle();
				GrappleHook hook = new GrappleHook(minecraft.overworld(), owner, new ItemStack(JugcraftGrapple.PNEUMATIC_GRAPPLE));
				// On the player's right, which the front camera shows on the left, clear of the advancement toasts.
				hook.setPos(owner.getX() + look.x * 1.5 - look.z * 1.6, owner.getY() + 1.4, owner.getZ() + look.z * 1.5 + look.x * 1.6);
				hook.setNoGravity(true);
				hook.setDeltaMovement(Vec3.ZERO);
				minecraft.overworld().addFreshEntity(hook);
			});
			context.waitTicks(10);
			context.takeScreenshot("jugcraft_pneumatic_grapple");
			server.runCommand("kill @e[type=jugcraft:grapple_hook]");
			context.runOnClient(client -> client.options.setCameraType(CameraType.FIRST_PERSON));
			String[] plastics = {"white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray", "light_gray",
					"cyan", "purple", "blue", "brown", "green", "red", "black"};
			for (int i = 0; i < plastics.length; i++) {
				server.runCommand("fill %d %d %d %d %d %d jugcraft:%s_plastic".formatted(x - 14 + i, y, z + 12, x - 14 + i, y + 1,
						z + 12, plastics[i]));
			}
			server.runCommand("tp @p %d %d %d 180 10".formatted(x - 6, y, z + 18));
			context.waitTicks(20);
			singleplayer.getConnection().waitForChunksRender();
			context.takeScreenshot("jugcraft_plastic_blocks");
			// Batch 32: a trench bridged with sprayed construction foam, a foam blob set against a wall, and concrete and
			// blast-proof concrete with their slabs and stairs; the foam sprayer in hand.
			server.runCommand("item replace entity @p hotbar.0 with jugcraft:foam_sprayer");
			server.runOnServer(minecraft -> {
				ServerLevel level = minecraft.overworld();
				BlockPos base = new BlockPos(x - 24, y, z + 7);
				for (int dx = 2; dx <= 4; dx++) {
					for (int dz = -1; dz <= 1; dz++) {
						for (int dy = 1; dy <= 3; dy++) {
							level.setBlockAndUpdate(base.offset(dx, -dy, dz), Blocks.AIR.defaultBlockState());
						}
						level.setBlockAndUpdate(base.offset(dx, -4, dz), Blocks.WATER.defaultBlockState());
					}
				}
				ServerPlayer player = minecraft.getPlayerList().getPlayers().get(0);
				for (BlockPos pos : FoamSprayerItem.fill(level, player, base.offset(3, -1, 0), ConstructionChemistry.SPRAY_BLOCKS)) {
					level.setBlockAndUpdate(pos, ConstructionChemistry.CONSTRUCTION_FOAM.defaultBlockState());
				}
				String[] row = {"concrete", "concrete_slab", "concrete_stairs", "blastproof_concrete", "blastproof_concrete_slab",
						"blastproof_concrete_stairs"};
				for (int i = 0; i < row.length; i++) {
					Block block = BuiltInRegistries.BLOCK.getValue(Jugcraft.id(row[i]));
					level.setBlockAndUpdate(base.offset(6 + i, 0, -1), block.defaultBlockState());
				}
				level.setBlockAndUpdate(base.offset(6, 1, -1), ConstructionChemistry.BLASTPROOF_CONCRETE.defaultBlockState());
				level.setBlockAndUpdate(base.offset(9, 1, -1), ConstructionChemistry.CONSTRUCTION_FOAM.defaultBlockState());
			});
			// Back from the scene and a little to the left, looking slightly down; wait for the advancement toasts to go.
			server.runCommand("tp @p %d %d %d 190 22".formatted(x - 17, y + 1, z + 12));
			context.waitTicks(140);
			singleplayer.getConnection().waitForChunksRender();
			context.takeScreenshot("jugcraft_foam_sprayer");
			context.runOnClient(client -> client.options.setCameraType(CameraType.FIRST_PERSON));
			context.getInput().pressKey(options -> options.keyToggleGui);
			server.runCommand("clear @p");

			// Power gear in the electric look: a line of glowing cables from a solar panel through a battery box, a
			// capacitor bank and a charging station to an electric motor turning a dynamo, and an electric pump.
			server.runOnServer(minecraft -> buildPowerGear(minecraft.overworld(), new BlockPos(x - 24, y, z + 2)));
			server.runCommand("tp @p %d %d %d 180 18".formatted(x - 17, y + 2, z + 9));
			context.waitTicks(40);
			singleplayer.getConnection().waitForChunksRender();
			context.takeScreenshot("jugcraft_power_gear");

			// Batch 37: a control room. Two battery boxes with sensors (red, blue), a logic controller and a relay on a
			// data cable, an alarm sounding, and a 3x2 control monitor showing the channels; the remote in hand.
			server.runCommand("item replace entity @p hotbar.0 with jugcraft:control_remote");
			server.runOnServer(minecraft -> buildControlRoom(minecraft.overworld(), new BlockPos(x - 24, y, z - 10)));
			server.runCommand("tp @p %d %d %d 180 6".formatted(x - 21, y + 1, z - 3));
			context.waitTicks(120);
			singleplayer.getConnection().waitForChunksRender();
			context.takeScreenshot("jugcraft_control_room");
			server.runCommand("clear @p");

			// Rocketry (batches 38-43), where the control room was: a wall of item frames with the rockets, the rocket
			// workshop, cryogenic liquefier and a rocket pad, a zipline strung from the top of the wall, and a powered
			// booster rail line with a minecart; the rocket launcher in hand.
			server.runCommand("fill %d %d %d %d %d %d minecraft:air".formatted(x - 26, y, z - 10, x - 10, y + 6, z + 1));
			server.runOnServer(minecraft -> buildRocketry(minecraft.overworld(), new BlockPos(x - 24, y, z - 10)));
			String[] rockets = {"survey_rocket", "signal_flare", "delivery_rocket", "line_rocket", "he_rocket", "homing_rocket",
					"kerosene_tank", "lox_tank", "rocket_motor", "flight_plan"};
			for (int i = 0; i < rockets.length; i++) {
				server.runCommand("summon minecraft:item_frame %d %d %d {Facing:3b,Fixed:1b,Item:{id:\"jugcraft:%s\",count:1}}"
						.formatted(x - 23 + i % 5 * 2, y + 1 + i / 5, z - 9, rockets[i]));
			}
			server.runCommand("item replace entity @p hotbar.0 with jugcraft:rocket_launcher");
			server.runCommand("tp @p %d %d %d 180 10".formatted(x - 18, y + 1, z - 2));
			context.waitTicks(80);
			singleplayer.getConnection().waitForChunksRender();
			context.takeScreenshot("jugcraft_rocketry");
			server.runCommand("clear @p");

			// Multi-block machines, ten blocks away, in views twelve blocks apart along the row (the wind turbine is
			// nine tall; the oil machines are at the far end).
			int views = (largeRowLength() + 11) / 12;
			for (int view = 0; view < views; view++) {
				server.runCommand("tp @p %d %d %d 180 8".formatted(x + 24 + view * 12, y + 3, z + 5));
				context.waitTicks(20);
				singleplayer.getConnection().waitForChunksRender();
				context.takeScreenshot("jugcraft_multiblocks_" + (view + 1));
			}

			// A machine screen: walk up to the crusher and use it.
			BlockPos crusher = new BlockPos(x - 7 + singleIndex(MachineKind.CRUSHER), y, z - 5);
			server.runCommand("tp @p %d %d %d 180 30".formatted(crusher.getX(), y, z - 3));
			context.waitTicks(10);
			context.getInput().lookAt(crusher);
			context.waitTick();
			context.getInput().pressKey(options -> options.keyUse);
			context.waitForScreen(MachineScreen.class);
			context.takeScreenshot("jugcraft_machine_screen");
			context.setScreen(() -> null);

			// One screen in each of the other two themes (batch 22): the electric battery box and the lab's circuit
			// assembler; and the distillation tower, whose screen holds the most outputs (five tanks and a slot, batch
			// 24).
			for (MachineKind kind : List.of(MachineKind.BATTERY_BOX, MachineKind.CIRCUIT_ASSEMBLER, MachineKind.DISTILLATION_TOWER)) {
				BlockPos machine = kind.isLarge() ? new BlockPos(x + largeOffset(kind), y, z - 5)
						: new BlockPos(x - 7 + singleIndex(kind), y, z - 5);
				server.runCommand("tp @p %d %d %d 180 30".formatted(machine.getX(), y, z - 3));
				context.waitTicks(10);
				context.getInput().lookAt(machine);
				context.waitTick();
				context.getInput().pressKey(options -> options.keyUse);
				context.waitForScreen(MachineScreen.class);
				context.waitTicks(25);
				context.takeScreenshot("jugcraft_machine_screen_" + kind.id);
				context.setScreen(() -> null);
			}

			// The auto-crafter's screen, with a stick pattern in its grid.
			BlockPos crafter = new BlockPos(x - 7 + singleIndex(MachineKind.AUTO_CRAFTER), y, z - 5);
			server.runOnServer(minecraft -> {
				if (minecraft.overworld().getBlockEntity(crafter) instanceof MachineBlockEntity machine) {
					machine.setItem(0, new ItemStack(Items.OAK_PLANKS, 12));
					machine.setItem(3, new ItemStack(Items.OAK_PLANKS, 12));
				}
			});
			server.runCommand("tp @p %d %d %d 180 30".formatted(crafter.getX(), y, z - 3));
			context.waitTicks(10);
			context.getInput().lookAt(crafter);
			context.waitTick();
			context.getInput().pressKey(options -> options.keyUse);
			context.waitForScreen(MachineScreen.class);
			context.waitTicks(20);
			context.takeScreenshot("jugcraft_auto_crafter_screen");
			context.setScreen(() -> null);

			// The Engineer's Handbook: the first page and the crusher's page.
			context.setScreen(HandbookScreen::new);
			context.waitTicks(2);
			context.takeScreenshot("jugcraft_handbook");
			// The Progression chapter's first stage, and a machine page (Processing: the crusher).
			context.setScreen(() -> new HandbookScreen(0, 1));
			context.waitTicks(2);
			context.takeScreenshot("jugcraft_handbook_progression");
			context.setScreen(() -> new HandbookScreen(4, 1));
			context.waitTicks(2);
			context.takeScreenshot("jugcraft_handbook_machine_page");
			context.setScreen(() -> null);

			// The prospector: a real survey of the superflat ground under the showroom (it is only a few
			// blocks deep), with ores buried in its dirt so the screen has readings to show; wait for the valve-tube bars to warm up.
			server.runCommand("fill %d %d %d %d %d %d minecraft:iron_ore".formatted(x - 12, y - 3, z - 12, x + 12, y - 3, z + 12));
			server.runCommand("fill %d %d %d %d %d %d minecraft:copper_ore".formatted(x - 6, y - 2, z - 6, x + 6, y - 2, z + 6));
			server.runCommand("setblock %d %d %d minecraft:diamond_ore".formatted(x + 1, y - 2, z + 1));
			AtomicReference<List<OreSurvey.Reading>> readings = new AtomicReference<>(List.of());
			server.runOnServer(minecraft -> readings.set(OreSurvey.survey(minecraft.overworld(), new BlockPos(x, y, z),
					minecraft.overworld().getRandom())));
			context.setScreen(() -> new ProspectorScreen(readings.get()));
			context.waitTicks(60);
			context.takeScreenshot("jugcraft_prospector");
			context.setScreen(() -> null);

			// Pixel Hollows blocks, then the Retro Trader at his cabinet, on a fresh floor south of everything else.
			int pz = z + 16;
			server.runCommand("fill %d %d %d %d %d %d minecraft:smooth_stone".formatted(x - 12, y - 1, z + 12, x + 64, y - 1, z + 40));
			server.runCommand("fill %d %d %d %d %d %d minecraft:air".formatted(x - 12, y, z + 12, x + 64, y + 14, z + 40));
			server.runOnServer(minecraft -> buildPixelHollowsShowroom(minecraft.overworld(), new BlockPos(x, y, pz)));
			server.runCommand("tp @p %d %d %d 180 25".formatted(x, y + 1, pz + 6));
			context.waitTicks(20);
			singleplayer.getConnection().waitForChunksRender();
			context.takeScreenshot("jugcraft_pixel_hollows_blocks");

			// Block centres as numbers ("%d.5" would put a negative coordinate half a block the wrong way).
			server.runCommand("summon minecraft:villager %s %d %s {NoAI:1b,Silent:1b,Rotation:[0f,0f]}".formatted(x + 10.5, y, pz + 0.5));
			server.runOnServer(minecraft -> makeRetroTraders(minecraft.overworld()));
			server.runCommand("tp @p %s %d %s 180 5".formatted(x + 10.5, y, pz + 3.2));
			context.waitTicks(20);
			singleplayer.getConnection().waitForChunksRender();
			context.takeScreenshot("jugcraft_retro_trader");

			// The Retro Game Shop as a village places it (its floor replaces the ground layer).
			server.runCommand("place template jugcraft:village/plains/retro_game_shop %d %d %d".formatted(x - 8, y - 1, z + 24));
			// A village turns the street jigsaw at the doorstep into its final state (air); a bare template keeps it.
			server.runCommand("setblock %d %d %d minecraft:air".formatted(x - 4, y, z + 31));
			server.runCommand("tp @p %d %d %d 180 8".formatted(x - 4, y + 1, z + 39));
			context.waitTicks(20);
			singleplayer.getConnection().waitForChunksRender();
			context.takeScreenshot("jugcraft_retro_game_shop");

			// The shop in a real village: a plains village generated far from the scenes (as /place structure does, once its
			// area is loaded), seen from the street in front of the shop's door.
			int vx = x + 640;
			int vz = z + 640;
			String villageArea = "%d %d %d %d".formatted(vx - 112, vz - 112, vx + 112, vz + 112);
			server.runCommand("forceload add " + villageArea);
			server.waitFor(minecraft -> areaLoaded(minecraft.overworld(), vx, vz, 112), 1200);
			server.runCommand("place structure minecraft:village_plains %d %d %d".formatted(vx, y, vz));
			server.runCommand(server.computeOnServer(minecraft -> shopCamera(minecraft.overworld(), new BlockPos(vx, y, vz), 112)));
			context.waitTicks(40);
			singleplayer.getConnection().waitForChunksRender();
			context.takeScreenshot("jugcraft_retro_game_shop_village");
			server.runCommand("forceload remove " + villageArea);

			// Inside a Pixel Hollows cave: a carved cavity in one chunk, its biome set to the Pixel Hollows, lined by the
			// biome's own circuitstone feature placed around the walls, with crystals on its floor and ceiling; seen in
			// spectator mode with night vision.
			int cx = Math.floorDiv(x + 48, 16) * 16;
			int cz = Math.floorDiv(z + 16, 16) * 16;
			server.runOnServer(minecraft -> carveCave(minecraft.overworld(), new BlockPos(cx, y, cz)));
			server.runCommand("fillbiome %d %d %d %d %d %d jugcraft:pixel_hollows".formatted(cx, y - 1, cz, cx + 15, y + 12, cz + 15));
			int[][] blobs = {{2, 2, 2}, {13, 3, 3}, {2, 8, 13}, {13, 9, 12}, {8, 0, 8}, {8, 10, 8}, {3, 5, 8}, {13, 6, 8}, {8, 4, 2},
					{8, 5, 15}, {4, 1, 12}, {12, 1, 4}, {5, 9, 4}, {11, 9, 13}};
			for (int[] blob : blobs) {
				server.runCommand("place feature jugcraft:pixel_hollows_lining %d %d %d".formatted(cx + blob[0], y + blob[1], cz + blob[2]));
			}
			server.runOnServer(minecraft -> growCrystals(minecraft.overworld(), new BlockPos(cx, y, cz)));
			server.runCommand("gamemode spectator @p");
			server.runCommand("effect give @p minecraft:night_vision infinite 0 true");
			server.runCommand("tp @p %d %d %d 180 15".formatted(cx + 8, y + 4, cz + 13));
			context.waitTicks(40);
			singleplayer.getConnection().waitForChunksRender();
			context.takeScreenshot("jugcraft_pixel_hollows_cave");
		}
	}

	/**
	 * Circuitstone, polished circuitstone, bricks and a pixel lamp in a row facing the camera, crystal clusters on top
	 * and on a small circuitstone wall, and the arcade cabinet at the end.
	 */
	private static void buildPixelHollowsShowroom(ServerLevel level, BlockPos row) {
		Block[] blocks = {PixelHollows.CIRCUITSTONE, PixelHollows.POLISHED_CIRCUITSTONE, PixelHollows.CIRCUITSTONE_BRICKS, PixelHollows.PIXEL_LAMP};
		BlockState cluster = PixelHollows.PIXEL_CRYSTAL_CLUSTER.defaultBlockState();
		for (int i = 0; i < blocks.length; i++) {
			level.setBlock(row.offset(-6 + i * 2, 0, 0), blocks[i].defaultBlockState(), 3);
		}
		level.setBlock(row.offset(-6, 1, 0), cluster.setValue(AmethystClusterBlock.FACING, Direction.UP), 3);
		for (int dx = 1; dx <= 3; dx++) {
			for (int dy = 0; dy <= 2; dy++) {
				level.setBlock(row.offset(dx, dy, -1), PixelHollows.CIRCUITSTONE.defaultBlockState(), 3);
			}
		}
		level.setBlock(row.offset(1, 1, 0), cluster.setValue(AmethystClusterBlock.FACING, Direction.SOUTH), 3);
		level.setBlock(row.offset(3, 0, 0), cluster.setValue(AmethystClusterBlock.FACING, Direction.SOUTH), 3);
		level.setBlock(row.offset(2, 3, -1), cluster.setValue(AmethystClusterBlock.FACING, Direction.UP), 3);
		BlockState cabinet = RetroTrader.ARCADE_CABINET.defaultBlockState().setValue(ArcadeCabinetBlock.FACING, Direction.SOUTH);
		level.setBlock(row.offset(6, 0, 0), cabinet, 3);
		level.setBlock(row.offset(6, 1, 0), cabinet.setValue(ArcadeCabinetBlock.HALF, DoubleBlockHalf.UPPER), 3);
		level.setBlock(row.offset(11, 0, 0), cabinet, 3);
		level.setBlock(row.offset(11, 1, 0), cabinet.setValue(ArcadeCabinetBlock.HALF, DoubleBlockHalf.UPPER), 3);
	}

	private static boolean areaLoaded(ServerLevel level, int x, int z, int reach) {
		for (int cx = (x - reach) >> 4; cx <= (x + reach) >> 4; cx++) {
			for (int cz = (z - reach) >> 4; cz <= (z + reach) >> 4; cz++) {
				if (!level.getChunkSource().hasChunk(cx, cz)) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * A teleport command that puts the camera on the street in front of the village's Retro Game Shop, looking at its
	 * door: the shop is found by its arcade cabinet, and its door is the nearest oak door to the cabinet.
	 */
	private static String shopCamera(ServerLevel level, BlockPos centre, int reach) {
		BlockPos cabinet = null;
		for (BlockPos pos : BlockPos.betweenClosed(centre.offset(-reach, -12, -reach), centre.offset(reach, 24, reach))) {
			BlockState state = level.getBlockState(pos);
			if (state.is(RetroTrader.ARCADE_CABINET) && state.getValue(ArcadeCabinetBlock.HALF) == DoubleBlockHalf.LOWER) {
				cabinet = pos.immutable();
				break;
			}
		}
		if (cabinet == null) {
			throw new AssertionError("The generated plains village has no Retro Game Shop");
		}
		BlockPos door = null;
		for (BlockPos pos : BlockPos.betweenClosed(cabinet.offset(-10, -2, -10), cabinet.offset(10, 2, 10))) {
			BlockState state = level.getBlockState(pos);
			if (state.is(Blocks.OAK_DOOR) && state.getValue(DoorBlock.HALF) == DoubleBlockHalf.LOWER
					&& (door == null || pos.distSqr(cabinet) < door.distSqr(cabinet))) {
				door = pos.immutable();
			}
		}
		if (door == null) {
			throw new AssertionError("The Retro Game Shop at " + cabinet + " has no door");
		}
		// From above the street, looking down at the doorstep: the storefront and how it meets the village path.
		Direction outside = level.getBlockState(door).getValue(DoorBlock.FACING).getOpposite();
		BlockPos eye = door.relative(outside, 7).above(6);
		BlockPos doorstep = door.relative(outside);
		Jugcraft.LOGGER.info("[pixel-hollows] Retro Game Shop in a generated plains village: cabinet {}, door {}, doorstep {} ({}), beyond {}",
				cabinet, door, doorstep, level.getBlockState(doorstep.below()), level.getBlockState(doorstep.relative(outside).below()));
		return String.format(Locale.ROOT, "tp @p %.1f %d %.1f facing %.1f %.1f %.1f", eye.getX() + 0.5, eye.getY(), eye.getZ() + 0.5,
				doorstep.getX() + 0.5, doorstep.getY() + 1.0, doorstep.getZ() + 0.5);
	}

	/** Gives every motionless (NoAI) villager the Retro Trader profession, at apprentice level. */
	private static void makeRetroTraders(ServerLevel level) {
		var trader = BuiltInRegistries.VILLAGER_PROFESSION.getOrThrow(RetroTrader.PROFESSION);
		for (Villager villager : level.getEntities(EntityTypeTest.forClass(Villager.class), villager -> villager.isNoAi())) {
			villager.setVillagerData(villager.getVillagerData().withProfession(trader).withLevel(2));
		}
	}

	/** Clusters on about one in six of the cave's open floors and ceilings (worldgen places them the same way). */
	private static void growCrystals(ServerLevel level, BlockPos corner) {
		RandomSource random = RandomSource.create(7L);
		for (int dx = 0; dx < 16; dx++) {
			for (int dz = 0; dz < 16; dz++) {
				for (int dy = 0; dy <= 10; dy++) {
					BlockPos pos = corner.offset(dx, dy, dz);
					if (!level.getBlockState(pos).isAir() || random.nextInt(6) != 0) {
						continue;
					}
					Direction facing = level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP) ? Direction.UP
							: level.getBlockState(pos.above()).isFaceSturdy(level, pos.above(), Direction.DOWN) ? Direction.DOWN : null;
					if (facing != null) {
						level.setBlock(pos, PixelHollows.PIXEL_CRYSTAL_CLUSTER.defaultBlockState().setValue(AmethystClusterBlock.FACING, facing), 2);
					}
				}
			}
		}
	}

	/** A deepslate block filling one chunk, 13 high, hollowed into a few joined round chambers. */
	private static void carveCave(ServerLevel level, BlockPos corner) {
		int[][] chambers = {{8, 5, 8, 6}, {4, 4, 11, 4}, {12, 6, 4, 4}, {8, 3, 13, 3}};
		for (int dx = 0; dx < 16; dx++) {
			for (int dy = -1; dy <= 11; dy++) {
				for (int dz = 0; dz < 16; dz++) {
					boolean hollow = false;
					for (int[] c : chambers) {
						int ax = dx - c[0];
						int ay = dy - c[1];
						int az = dz - c[2];
						hollow |= dy >= 0 && dy <= 10 && ax * ax + ay * ay + az * az < c[3] * c[3];
					}
					level.setBlock(corner.offset(dx, dy, dz), hollow ? Blocks.CAVE_AIR.defaultBlockState() : Blocks.DEEPSLATE.defaultBlockState(), 2);
				}
			}
		}
	}

	/**
	 * Engine at {@code start} facing west (its back drives east), two shafts, a gearbox with a hand crank
	 * on top, a dynamo beyond it and a crusher on the gearbox's north side (behind it, seen from the camera).
	 */
	private static void buildKineticLine(ServerLevel level, BlockPos start) {
		level.setBlock(start.below(), Blocks.WATER.defaultBlockState(), 3);
		level.setBlock(start, JugcraftKinetics.STEAM_ENGINE.defaultBlockState().setValue(SteamEngineBlock.FACING, Direction.WEST), 3);
		Storage<ItemVariant> fuel = ItemStorage.SIDED.find(level, start, Direction.UP);
		try (Transaction transaction = Transaction.openOuter()) {
			fuel.insert(ItemVariant.of(Items.COAL), 8, transaction);
			transaction.commit();
		}
		BlockState shaft = JugcraftKinetics.IRON_SHAFT.defaultBlockState().setValue(ShaftBlock.AXIS, Direction.Axis.X);
		level.setBlock(start.east(), shaft, 3);
		level.setBlock(start.east(2), shaft, 3);
		BlockPos gearbox = start.east(3);
		level.setBlock(gearbox, JugcraftKinetics.BRASS_GEARBOX.defaultBlockState(), 3);
		level.setBlock(gearbox.above(), JugcraftKinetics.HAND_CRANK.defaultBlockState().setValue(HandCrankBlock.FACING, Direction.DOWN), 3);
		level.setBlock(gearbox.east(), JugcraftKinetics.DYNAMO.defaultBlockState().setValue(DynamoBlock.FACING, Direction.SOUTH), 3);
		level.setBlock(gearbox.north(), JugcraftMachines.MACHINES.get(MachineKind.CRUSHER).defaultBlockState()
				.setValue(MachineBlock.FACING, Direction.SOUTH), 3);
	}

	/**
	 * A charged electric motor facing east into a pulley; that pulley is belted to one three blocks above it, which
	 * drives a crusher through a shaft. All run along the x axis.
	 */
	private static void buildBeltLine(ServerLevel level, BlockPos start) {
		level.setBlock(start, JugcraftKinetics.ELECTRIC_MOTOR.defaultBlockState().setValue(ElectricMotorBlock.FACING, Direction.EAST), 3);
		if (level.getBlockEntity(start) instanceof ElectricMotorBlockEntity motor) {
			motor.energy().setAmount(ElectricMotorBlockEntity.CAPACITY);
		}
		BlockState pulley = JugcraftKinetics.BELT_PULLEY.defaultBlockState().setValue(ShaftBlock.AXIS, Direction.Axis.X);
		BlockPos lower = start.east();
		BlockPos upper = lower.above(3);
		level.setBlock(lower, pulley, 3);
		level.setBlock(upper, pulley, 3);
		BeltPulleyBlockEntity.connect(level, lower, upper);
		level.setBlock(upper.east(), JugcraftKinetics.IRON_SHAFT.defaultBlockState().setValue(ShaftBlock.AXIS, Direction.Axis.X), 3);
		level.setBlock(upper.east(2), JugcraftMachines.MACHINES.get(MachineKind.CRUSHER).defaultBlockState()
				.setValue(MachineBlock.FACING, Direction.SOUTH), 3);
	}

	/**
	 * Three conveyors running east with items on them, into a splitter with chests on its three outputs; a charged
	 * motor at the back drives them all.
	 */
	private static void buildConveyorLine(ServerLevel level, BlockPos start) {
		ItemStack[] cargo = {new ItemStack(Items.IRON_INGOT), new ItemStack(Items.COBBLESTONE), new ItemStack(Items.COAL),
				new ItemStack(Items.OAK_LOG)};
		for (int i = 1; i <= 4; i++) {
			BlockPos pos = start.east(i);
			level.setBlock(pos, (i == 4 ? JugcraftLogistics.CONVEYOR_SPLITTER : JugcraftLogistics.CONVEYOR).defaultBlockState()
					.setValue(ConveyorBlock.FACING, Direction.EAST), 3);
			if (i < 4 && level.getBlockEntity(pos) instanceof ConveyorBlockEntity conveyor) {
				for (int slot = 0; slot < 3; slot++) {
					conveyor.accept(cargo[(i + slot) % cargo.length].copy(), slot * 0.3F);
				}
			}
		}
		BlockPos splitter = start.east(4);
		for (BlockPos chest : new BlockPos[] {splitter.north(), splitter.east(), splitter.south()}) {
			level.setBlock(chest, Blocks.CHEST.defaultBlockState(), 3);
		}
		level.setBlock(start, JugcraftKinetics.ELECTRIC_MOTOR.defaultBlockState().setValue(ElectricMotorBlock.FACING, Direction.EAST), 3);
		if (level.getBlockEntity(start) instanceof ElectricMotorBlockEntity motor) {
			motor.energy().setAmount(ElectricMotorBlockEntity.CAPACITY);
		}
	}

	/**
	 * A motor driving a conveyor east into an up slope, a raised conveyor, a down slope and a chest, with items on the
	 * first conveyor and both slopes.
	 */
	private static void buildSlopeLine(ServerLevel level, BlockPos start) {
		BlockState flat = JugcraftLogistics.CONVEYOR.defaultBlockState().setValue(ConveyorBlock.FACING, Direction.EAST);
		BlockState slope = JugcraftLogistics.CONVEYOR_SLOPE.defaultBlockState().setValue(ConveyorBlock.FACING, Direction.EAST);
		level.setBlock(start.east(1), flat, 3);
		level.setBlock(start.east(2), slope.setValue(ConveyorSlopeBlock.ASCENDING, true), 3);
		level.setBlock(start.east(3).above(), flat, 3);
		level.setBlock(start.east(4), slope.setValue(ConveyorSlopeBlock.ASCENDING, false), 3);
		level.setBlock(start.east(5), Blocks.CHEST.defaultBlockState(), 3);
		ItemStack[] cargo = {new ItemStack(Items.IRON_INGOT), new ItemStack(Items.OAK_LOG), new ItemStack(Items.COAL)};
		for (BlockPos pos : new BlockPos[] {start.east(1), start.east(2), start.east(3).above(), start.east(4)}) {
			if (level.getBlockEntity(pos) instanceof ConveyorBlockEntity conveyor) {
				for (int i = 0; i < 2; i++) {
					conveyor.accept(cargo[(pos.getX() + i) % cargo.length].copy(), 0.1F + i * 0.45F);
				}
			}
		}
		level.setBlock(start, JugcraftKinetics.ELECTRIC_MOTOR.defaultBlockState().setValue(ElectricMotorBlock.FACING, Direction.EAST), 3);
		if (level.getBlockEntity(start) instanceof ElectricMotorBlockEntity motor) {
			motor.energy().setAmount(ElectricMotorBlockEntity.CAPACITY);
		}
	}

	/** Three charging stations in a row, facing south, holding the mining drill, the chainsaw and the rocket pack. */
	private static void buildToolStations(ServerLevel level, BlockPos start) {
		Item[] tools = {JugcraftTools.MINING_DRILL, JugcraftTools.CHAINSAW, JugcraftTools.ROCKET_PACK};
		for (int i = 0; i < tools.length; i++) {
			BlockPos lower = start.east(i * 2);
			BlockState state = JugcraftTools.CHARGING_STATION.defaultBlockState().setValue(ChargingStationBlock.FACING, Direction.SOUTH);
			level.setBlock(lower, state, 3);
			level.setBlock(lower.above(), state.setValue(ChargingStationBlock.HALF, DoubleBlockHalf.UPPER), 3);
			if (level.getBlockEntity(lower) instanceof ChargingStationBlockEntity station) {
				station.setTool(new ItemStack(tools[i]));
			}
		}
	}

	/** Position of a one-block machine in the showroom row (multi-block machines skipped). */
	private static int singleIndex(MachineKind target) {
		int index = 0;
		for (MachineKind kind : MachineKind.values()) {
			if (kind == target) {
				return index;
			}
			if (!kind.isLarge()) {
				index++;
			}
		}
		throw new IllegalArgumentException(target.id);
	}

	/** Smooth stone one below {@code from}'s level across the area, and air from there up to {@code to}. */
	private static void clearFloor(ServerLevel level, BlockPos from, BlockPos to) {
		BlockState floor = Blocks.SMOOTH_STONE.defaultBlockState();
		BlockState air = Blocks.AIR.defaultBlockState();
		for (int bx = from.getX(); bx <= to.getX(); bx++) {
			for (int bz = from.getZ(); bz <= to.getZ(); bz++) {
				level.setBlock(new BlockPos(bx, from.getY() - 1, bz), floor, 2);
				for (int by = from.getY(); by <= to.getY(); by++) {
					BlockPos pos = new BlockPos(bx, by, bz);
					if (!level.getBlockState(pos).isAir()) {
						level.setBlock(pos, air, 2);
					}
				}
			}
		}
	}

	/** Blocks a multi-block machine takes across its front (it extends to its right, +x when facing south). */
	private static int width(MachineKind kind) {
		return 1 + kind.footprint().offsets().stream().mapToInt(offset -> Math.abs(offset.getX())).max().orElse(0);
	}

	/** How far east of the showroom's origin a multi-block machine's master stands (see buildShowroom). */
	private static int largeOffset(MachineKind target) {
		int offset = 20;
		for (MachineKind kind : MachineKind.values()) {
			if (kind == target) {
				return offset;
			}
			if (kind.isLarge()) {
				offset += width(kind) + 2;
			}
		}
		throw new IllegalArgumentException(target.id);
	}

	/** Length of the multi-block row: each machine's width plus a two-block gap. */
	private static int largeRowLength() {
		int length = 0;
		for (MachineKind kind : MachineKind.values()) {
			if (kind.isLarge()) {
				length += width(kind) + 2;
			}
		}
		return length;
	}

	/**
	 * Power gear in the electric look, along x from {@code start}: a solar panel, a charged battery box, a capacitor
	 * bank and a charging station holding a drill, joined by copper, silver and aluminum cables (one rising over the
	 * bank), then a charged electric motor turning a shaft into a dynamo, and an electric pump. All face south.
	 */
	/** Batch 37: the control room scene, along the back of the cleared floor (base: the west end of its cable). */
	private static void buildControlRoom(ServerLevel level, BlockPos base) {
		BlockState cable = io.github.jimbozoomer.jugcraft.control.JugcraftControl.DATA_CABLE.defaultBlockState();
		for (int dx = 1; dx <= 5; dx++) {
			level.setBlock(base.offset(dx, 1, 0), cable, 3);
		}
		for (int dx = 6; dx <= 9; dx++) {
			level.setBlock(base.offset(dx, 0, 0), cable, 3);
		}
		BlockPos controllerPos = base.offset(6, 1, 0);
		level.setBlock(controllerPos, io.github.jimbozoomer.jugcraft.control.JugcraftControl.LOGIC_CONTROLLER.defaultBlockState()
				.setValue(io.github.jimbozoomer.jugcraft.control.LogicControllerBlock.FACING, Direction.SOUTH), 3);
		var red = net.minecraft.world.item.DyeColor.RED;
		var blue = net.minecraft.world.item.DyeColor.BLUE;
		int[][] batteries = {{7, 70}, {9, 30}};
		for (int i = 0; i < batteries.length; i++) {
			BlockPos battery = base.offset(batteries[i][0], 0, 2);
			level.setBlock(battery, JugcraftMachines.MACHINES.get(MachineKind.BATTERY_BOX).defaultBlockState()
					.setValue(MachineBlock.FACING, Direction.SOUTH), 3);
			if (level.getBlockEntity(battery) instanceof MachineBlockEntity box && box.energyFor(null) instanceof SimpleEnergyStorage energy) {
				energy.setAmount(energy.getCapacity() * batteries[i][1] / 100);
			}
			level.setBlock(battery.north(), io.github.jimbozoomer.jugcraft.control.JugcraftControl.SENSOR.defaultBlockState()
					.setValue(io.github.jimbozoomer.jugcraft.control.SensorBlock.FACING, Direction.NORTH)
					.setValue(io.github.jimbozoomer.jugcraft.control.Channels.CHANNEL, i == 0 ? red : blue), 3);
		}
		level.setBlock(base.offset(8, 0, 1), io.github.jimbozoomer.jugcraft.control.JugcraftControl.RELAY.defaultBlockState()
				.setValue(io.github.jimbozoomer.jugcraft.control.Channels.CHANNEL, net.minecraft.world.item.DyeColor.GREEN), 3);
		level.setBlock(base.offset(1, 2, 0), io.github.jimbozoomer.jugcraft.control.JugcraftControl.ALARM.defaultBlockState()
				.setValue(io.github.jimbozoomer.jugcraft.control.Channels.CHANNEL, net.minecraft.world.item.DyeColor.ORANGE), 3);
		// The monitor last, so its panels find the cable behind them when they form.
		for (int dx = 2; dx <= 4; dx++) {
			for (int dy = 1; dy <= 2; dy++) {
				level.setBlock(base.offset(dx, dy, 1), io.github.jimbozoomer.jugcraft.control.JugcraftControl.CONTROL_MONITOR.defaultBlockState()
						.setValue(io.github.jimbozoomer.jugcraft.control.ControlMonitorBlock.FACING, Direction.SOUTH), 3);
			}
		}
		for (BlockPos pos : BlockPos.betweenClosed(base, base.offset(9, 2, 0))) {
			if (level.getBlockState(pos).is(io.github.jimbozoomer.jugcraft.control.JugcraftControl.DATA_CABLE)) {
				level.setBlock(pos, Block.updateFromNeighbourShapes(level.getBlockState(pos), level, pos), 3);
			}
		}
		if (level.getBlockEntity(controllerPos) instanceof io.github.jimbozoomer.jugcraft.control.LogicControllerBlockEntity controller) {
			controller.setRule(0, true, red, true, 50, net.minecraft.world.item.DyeColor.GREEN, true);
			controller.setRule(1, true, blue, false, 20, net.minecraft.world.item.DyeColor.ORANGE, true);
			controller.evaluate(level, controllerPos);
			controller.toggle(level, net.minecraft.world.item.DyeColor.ORANGE);
		}
	}

	/** Batches 38-43: the rocketry scene (base: the west end of the back wall). */
	private static void buildRocketry(ServerLevel level, BlockPos base) {
		for (int dx = 0; dx <= 10; dx++) {
			for (int dy = 0; dy <= 2; dy++) {
				level.setBlock(base.offset(dx, dy, 0), Blocks.SMOOTH_STONE.defaultBlockState(), 3);
			}
		}
		level.setBlock(base.offset(1, 0, 3), JugcraftMachines.MACHINES.get(MachineKind.ROCKET_WORKSHOP).defaultBlockState()
				.setValue(MachineBlock.FACING, Direction.SOUTH), 3);
		level.setBlock(base.offset(3, 0, 3), JugcraftMachines.MACHINES.get(MachineKind.CRYOGENIC_LIQUEFIER).defaultBlockState()
				.setValue(MachineBlock.FACING, Direction.SOUTH), 3);
		level.setBlock(base.offset(5, 0, 3), io.github.jimbozoomer.jugcraft.rocketry.JugcraftRocketry.ROCKET_PAD.defaultBlockState(), 3);
		// A zipline from the top of the wall down to an anchor near the camera.
		BlockPos top = base.offset(10, 3, 0);
		BlockPos low = base.offset(13, 0, 4);
		BlockState anchor = io.github.jimbozoomer.jugcraft.rocketry.JugcraftRocketry.ZIPLINE_ANCHOR.defaultBlockState();
		level.setBlock(top, anchor, 3);
		level.setBlock(low, anchor, 3);
		io.github.jimbozoomer.jugcraft.rocketry.ZiplineAnchorBlockEntity.connect(level, top, low, null);
		// A powered booster rail line along the front, with a minecart on it.
		BlockState rail = io.github.jimbozoomer.jugcraft.rocketry.JugcraftRocketry.BOOSTER_RAIL.defaultBlockState()
				.setValue(net.minecraft.world.level.block.PoweredRailBlock.SHAPE, net.minecraft.world.level.block.state.properties.RailShape.EAST_WEST);
		for (int dx = 1; dx <= 8; dx++) {
			level.setBlock(base.offset(dx, 0, 5), rail, 3);
		}
		level.setBlock(base.offset(0, 0, 5), Blocks.REDSTONE_BLOCK.defaultBlockState(), 3);
		var cart = net.minecraft.world.entity.EntityTypes.MINECART.create(level, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
		if (cart != null) {
			BlockPos at = base.offset(5, 0, 5);
			cart.snapTo(at.getX() + 0.5, at.getY() + 0.1, at.getZ() + 0.5, 90, 0);
			level.addFreshEntity(cart);
		}
	}

	private static void buildPowerGear(ServerLevel level, BlockPos start) {
		level.setBlock(start, JugcraftMachines.MACHINES.get(MachineKind.SOLAR_PANEL).defaultBlockState()
				.setValue(MachineBlock.FACING, Direction.SOUTH), 3);
		BlockPos battery = start.east(2);
		level.setBlock(battery, JugcraftMachines.MACHINES.get(MachineKind.BATTERY_BOX).defaultBlockState()
				.setValue(MachineBlock.FACING, Direction.SOUTH), 3);
		if (level.getBlockEntity(battery) instanceof MachineBlockEntity box && box.energyFor(null) instanceof SimpleEnergyStorage energy) {
			energy.setAmount(energy.getCapacity());
		}
		BlockPos bank = start.east(4);
		MachineBlock bankBlock = JugcraftMachines.MACHINES.get(MachineKind.CAPACITOR_BANK);
		BlockState bankState = bankBlock.defaultBlockState().setValue(MachineBlock.FACING, Direction.SOUTH);
		level.setBlock(bank, bankState, 3);
		((LargeMachineBlock) bankBlock).setPlacedBy(level, bank, bankState, null, ItemStack.EMPTY);
		BlockPos station = start.east(7);
		BlockState stationState = JugcraftTools.CHARGING_STATION.defaultBlockState().setValue(ChargingStationBlock.FACING, Direction.SOUTH);
		level.setBlock(station, stationState, 3);
		level.setBlock(station.above(), stationState.setValue(ChargingStationBlock.HALF, DoubleBlockHalf.UPPER), 3);
		if (level.getBlockEntity(station) instanceof ChargingStationBlockEntity holder) {
			holder.setTool(new ItemStack(JugcraftTools.MINING_DRILL));
		}
		BlockPos motor = start.east(9);
		level.setBlock(motor, JugcraftKinetics.ELECTRIC_MOTOR.defaultBlockState().setValue(ElectricMotorBlock.FACING, Direction.EAST), 3);
		if (level.getBlockEntity(motor) instanceof ElectricMotorBlockEntity motorEntity) {
			motorEntity.energy().setAmount(ElectricMotorBlockEntity.CAPACITY);
		}
		level.setBlock(motor.east(), JugcraftKinetics.IRON_SHAFT.defaultBlockState().setValue(ShaftBlock.AXIS, Direction.Axis.X), 3);
		level.setBlock(motor.east(2), JugcraftKinetics.DYNAMO.defaultBlockState().setValue(DynamoBlock.FACING, Direction.SOUTH), 3);
		level.setBlock(start.east(13), JugcraftFluids.ELECTRIC_PUMP.defaultBlockState(), 3);
		// Cables last: copper over the battery, silver across the top of the bank, aluminum to the rest.
		List<BlockPos> copper = List.of(start.east(), start.east(3), start.east(3).above(), start.east(3).above(2));
		List<BlockPos> silver = List.of(start.east(4).above(2), start.east(5).above(2), start.east(6).above(2), start.east(6).above());
		List<BlockPos> aluminum = List.of(start.east(6), start.east(8), start.east(12));
		placeCables(level, JugcraftMachines.COPPER_CABLE, copper);
		placeCables(level, JugcraftMachines.SILVER_CABLE, silver);
		placeCables(level, JugcraftMachines.ALUMINUM_CABLE, aluminum);
		for (List<BlockPos> line : List.of(copper, silver, aluminum)) {
			for (BlockPos pos : line) {
				level.setBlock(pos, Block.updateFromNeighbourShapes(level.getBlockState(pos), level, pos), 3);
			}
		}
	}

	private static void placeCables(ServerLevel level, Block cable, List<BlockPos> positions) {
		for (BlockPos pos : positions) {
			level.setBlock(pos, cable.defaultBlockState(), 3);
		}
	}

	/**
	 * One-block machines side by side (in MachineKind order, from x - 7), all facing south towards the
	 * camera; multi-block machines in a second group 20 blocks east, each its own width plus two apart.
	 */
	private static void buildShowroom(ServerLevel level, BlockPos row) {
		int large = 20;
		for (MachineKind kind : MachineKind.values()) {
			MachineBlock block = JugcraftMachines.MACHINES.get(kind);
			BlockState state = block.defaultBlockState().setValue(MachineBlock.FACING, Direction.SOUTH);
			if (kind.isLarge()) {
				BlockPos pos = row.offset(large, 0, 0);
				level.setBlock(pos, state, 3);
				((LargeMachineBlock) block).setPlacedBy(level, pos, state, null, ItemStack.EMPTY);
				large += width(kind) + 2;
			} else {
				level.setBlock(row.offset(-7 + singleIndex(kind), 0, 0), state, 3);
			}
		}
	}
}

package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.client.GunsClient;
import io.github.jimbozoomer.jugcraft.client.guns.GunEffects;
import io.github.jimbozoomer.jugcraft.client.guns.GunPose;
import io.github.jimbozoomer.jugcraft.client.guns.GunView;
import io.github.jimbozoomer.jugcraft.guns.GunItem;
import io.github.jimbozoomer.jugcraft.guns.GunShots;
import io.github.jimbozoomer.jugcraft.guns.JugcraftGuns;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;

/**
 * Client game test for the guns (slice 1), end to end through the real input: each gun drawn in first person, aimed down
 * its sights and fired so at a husk with the attack key (the server lands the shot and spends a round), reloaded with the
 * reload key (part way through and done: the rounds come out of the inventory), and inspected; the Thunderpipe's
 * shell-at-a-time reload part way; each gun that takes attachments held with two sets of them fitted (slice 5), the
 * client seeing a fitted magazine's capacity; each gun held in third person and shown in the inventory with the
 * attachments. Slice 6: each gun narrows the view aimed, shows a muzzle flash fired and throws the spent casings its
 * animations cue; in third person the player is posed holding it, and fires it. Slice 7: a third set of attachments
 * with a bayonet (and the guns whose parts use shared textures), and a stab with the stab key that hurts the husk.
 * Screenshots jugcraft_guns_* (CI job {@code client}).
 */
public class GunsClientGameTests implements FabricClientGameTest {
	@Override
	public void runTest(ClientGameTestContext context) {
		try (TestSingleplayerContext singleplayer = context.worldBuilder()
				.adjustSettings(creator -> creator.setGameMode(WorldCreationUiState.SelectedGameMode.SURVIVAL)).create()) {
			singleplayer.getConnection().waitForChunksRender();
			BlockPos origin = context.computeOnClient(client -> BlockPos.containing(client.player.position()));
			int x = origin.getX();
			int y = origin.getY();
			int z = origin.getZ();
			TestServerContext server = singleplayer.getServer();
			server.runCommand("time set noon");
			server.runCommand("weather clear");
			server.runCommand("gamerule minecraft:send_command_feedback false");
			server.runCommand("fill %d %d %d %d %d %d minecraft:smooth_stone".formatted(x - 8, y - 1, z - 12, x + 8, y - 1, z + 4));
			server.runCommand("fill %d %d %d %d %d %d minecraft:air".formatted(x - 8, y, z - 12, x + 8, y + 6, z + 4));
			server.runCommand("fill %d %d %d %d %d %d minecraft:stone_bricks".formatted(x - 4, y, z - 12, x + 4, y + 4, z - 12));
			// The player looks north at a still husk seven blocks away, in front of a brick wall.
			server.runCommand(String.format(Locale.ROOT, "tp @p %.1f %d %.1f 180 5", x + 0.5, y, z + 0.5));
			server.runCommand(String.format(Locale.ROOT, "summon minecraft:husk %.1f %d %.1f {NoAI:1b,PersistenceRequired:1b,"
					+ "Rotation:[0f,0f],attributes:[{id:\"minecraft:armor\",base:0.0d},{id:\"minecraft:max_health\",base:1000.0d},"
					+ "{id:\"minecraft:knockback_resistance\",base:1.0d}],Health:1000.0f}", x + 0.5, y, z - 6.5));
			context.runOnClient(client -> client.options.setCameraType(CameraType.FIRST_PERSON));

			for (String gun : JugcraftGuns.SPECS.keySet()) {
				String round = JugcraftGuns.SPECS.get(gun).ammo();
				int capacity = JugcraftGuns.SPECS.get(gun).capacity();
				// Each gun starts from the same aim: every shot kicks the view up, and twelve guns' kicks would lift it
				// over the husk.
				server.runCommand(String.format(Locale.ROOT, "tp @p %.1f %d %.1f 180 5", x + 0.5, y, z + 0.5));
				server.runCommand("clear @p");
				server.runCommand("item replace entity @p weapon.mainhand with jugcraft:%s[jugcraft:loaded_rounds=%d]".formatted(gun, capacity));
				server.runCommand("give @p jugcraft:%s 32".formatted(round));
				context.waitTicks(30);
				context.takeScreenshot("jugcraft_guns_" + gun + "_held");

				// Aimed (the aimed spread keeps the shot on the husk), then fired down the sights.
				context.getInput().holdKey(options -> options.keyUse);
				context.waitTicks(10);
				context.takeScreenshot("jugcraft_guns_" + gun + "_aimed");
				// Slice 6: aimed, the view narrows by the gun's zoom (GunFovMixin hands vanilla's modifier to GunView).
				float fovIn = context.computeOnClient(client -> GunView.lastFovIn());
				float fovOut = context.computeOnClient(client -> GunView.lastFovOut());
				Jugcraft.LOGGER.info("[guns] {} aimed: field of view modifier {} -> {}", gun, fovIn, fovOut);
				if (!(fovOut < fovIn * 0.99F)) {
					throw new AssertionError("Aiming the " + gun + " does not narrow the view: modifier " + fovIn + " -> " + fovOut
							+ (Float.isNaN(fovIn) ? " (GunFovMixin never ran: has AbstractClientPlayer.getFieldOfViewModifier moved?)" : ""));
				}
				float before = health(server, x, y, z);
				long flashes = context.computeOnClient(client -> GunEffects.flashes());
				long ejected = context.computeOnClient(client -> GunEffects.ejected());
				// The shot, with its flash (it shows for two ticks).
				context.getInput().pressKey(options -> options.keyAttack);
				context.takeScreenshot("jugcraft_guns_" + gun + "_fired");
				long flashed = context.computeOnClient(client -> GunEffects.flashes()) - flashes;
				Jugcraft.LOGGER.info("[guns] {} fired: {} muzzle flash frames drawn", gun, flashed);
				if (flashed <= 0) {
					throw new AssertionError("The " + gun + " showed no muzzle flash when fired");
				}
				context.waitTicks(11);
				context.getInput().releaseKey(options -> options.keyUse);
				context.waitTicks(5);
				float after = health(server, x, y, z);
				int loaded = server.computeOnServer(minecraft -> GunItem.loaded(player(minecraft).getMainHandItem()));
				Jugcraft.LOGGER.info("[guns] {} fired at a husk: health {} -> {}, rounds {} -> {}", gun, before, after, capacity, loaded);
				if (after >= before || loaded != capacity - 1) {
					throw new AssertionError("The " + gun + " did not hit the husk and spend a round: health " + before + " -> " + after
							+ ", rounds " + capacity + " -> " + loaded);
				}

				context.getInput().pressKey(options -> GunsClient.reloadKey());
				int ticks = JugcraftGuns.SPECS.get(gun).reloadTicks(1);
				context.waitTicks(Math.max(5, ticks / 2));
				context.takeScreenshot("jugcraft_guns_" + gun + "_reloading");
				context.waitTicks(ticks);
				int reloaded = server.computeOnServer(minecraft -> GunItem.loaded(player(minecraft).getMainHandItem()));
				int left = server.computeOnServer(minecraft -> GunShots.count(player(minecraft).getInventory(),
						BuiltInRegistries.ITEM.getValue(Jugcraft.id(round))));
				Jugcraft.LOGGER.info("[guns] {} reloaded: {} rounds loaded, {} {} left", gun, reloaded, left, round);
				if (reloaded != capacity || left != 31) {
					throw new AssertionError("The " + gun + " reload did not load the one round from the inventory: " + reloaded
							+ " loaded, " + left + " left");
				}

				// The owner's animations cue a spent casing (or, for a paper cartridge, a puff from the lock) on the shot or the
				// reload; every gun whose animations have the cue threw one.
				long thrown = context.computeOnClient(client -> GunEffects.ejected()) - ejected;
				boolean cued = context.computeOnClient(client -> client.getResourceManager()
						.getResource(Jugcraft.id("geckolib/animations/item/" + gun + ".animation.json")).map(resource -> {
							try (InputStream in = resource.open()) {
								return new String(in.readAllBytes(), StandardCharsets.UTF_8).contains("\"eject_casing\"");
							} catch (IOException e) {
								return false;
							}
						}).orElse(false));
				Jugcraft.LOGGER.info("[guns] {} fired and reloaded: {} casings thrown (its animations cue them: {})", gun, thrown, cued);
				if (cued && thrown <= 0) {
					throw new AssertionError("The " + gun + "'s animations cue a spent casing, but none was thrown");
				}

				context.getInput().pressKey(options -> GunsClient.inspectKey());
				context.waitTicks(30);
				context.takeScreenshot("jugcraft_guns_" + gun + "_inspect");
				context.waitTicks(100);
			}

			// The Thunderpipe from empty: both shells, part way through the second.
			server.runCommand("item replace entity @p weapon.mainhand with jugcraft:thunderpipe[jugcraft:loaded_rounds=0]");
			context.waitTicks(30);
			context.getInput().pressKey(options -> GunsClient.reloadKey());
			context.waitTicks(JugcraftGuns.SPECS.get("thunderpipe").shellStart() + JugcraftGuns.SPECS.get("thunderpipe").shellEach() + 6);
			context.takeScreenshot("jugcraft_guns_thunderpipe_shell");
			context.waitTicks(40);

			// Attachments: each gun that takes any, with one of each slot it has from two sets, held and aimed.
			List<List<String>> sets = List.of(List.of("silencer", "extended_magazine", "light_stock", "light_grip"),
					List.of("extended_barrel", "speed_magazine", "weighted_stock", "vertical_grip"),
					List.of("muzzle_brake", "wooden_stock", "iron_bayonet"));
			for (String gun : JugcraftGuns.ACCEPTS.keySet()) {
				for (int set = 0; set < sets.size(); set++) {
					List<String> fitted = sets.get(set).stream().filter(JugcraftGuns.ACCEPTS.get(gun)::contains).toList();
					server.runCommand("item replace entity @p weapon.mainhand with jugcraft:%s[jugcraft:loaded_rounds=1,jugcraft:attachments=%s]"
							.formatted(gun, snbt(fitted)));
					context.waitTicks(20);
					context.takeScreenshot("jugcraft_guns_" + gun + "_fitted_" + (set + 1));
					context.getInput().holdKey(options -> options.keyUse);
					context.waitTicks(10);
					context.takeScreenshot("jugcraft_guns_" + gun + "_fitted_" + (set + 1) + "_aimed");
					context.getInput().releaseKey(options -> options.keyUse);
					context.waitTicks(5);
				}
			}
			// Slice 7: the stab key stabs the husk with a Steel Bayonet from two blocks off; the server deals the blow.
			server.runCommand(String.format(Locale.ROOT, "tp @p %.1f %d %.1f 180 10", x + 0.5, y, z - 4.0));
			server.runCommand("item replace entity @p weapon.mainhand with jugcraft:patchwork_carbine[jugcraft:attachments=%s]"
					.formatted(snbt(List.of("steel_bayonet"))));
			context.waitTicks(20);
			float unstabbed = health(server, x, y, z);
			context.getInput().pressKey(options -> GunsClient.stabKey());
			context.takeScreenshot("jugcraft_guns_bayonet_stab");
			context.waitTicks(5);
			float stabbed = health(server, x, y, z);
			Jugcraft.LOGGER.info("[guns] a Steel Bayonet stab: husk health {} -> {}", unstabbed, stabbed);
			if (!(stabbed < unstabbed)) {
				throw new AssertionError("The Steel Bayonet's stab did not hurt the husk: health " + unstabbed + " -> " + stabbed);
			}
			server.runCommand(String.format(Locale.ROOT, "tp @p %.1f %d %.1f 180 5", x + 0.5, y, z + 0.5));

			server.runCommand("item replace entity @p weapon.mainhand with jugcraft:rust_midge[jugcraft:attachments=[\"extended_magazine\"]]");
			context.waitTicks(10);
			int seen = context.computeOnClient(client -> GunItem.spec(client.player.getMainHandItem()).capacity());
			Jugcraft.LOGGER.info("[guns] the client sees a Rust Midge with an Extended Magazine hold {} rounds", seen);
			if (seen != 30) {
				throw new AssertionError("The client sees the Extended Magazine's Rust Midge hold " + seen + " rounds, not 30");
			}

			// Seen from outside (slice 6): the gun arm raised along the look, the other across to the fore-end for a gun held
			// in both hands; then a shot, its flash seen from in front.
			long posed = context.computeOnClient(client -> GunPose.posed());
			context.runOnClient(client -> client.options.setCameraType(CameraType.THIRD_PERSON_FRONT));
			for (String gun : JugcraftGuns.SPECS.keySet()) {
				server.runCommand(String.format(Locale.ROOT, "tp @p %.1f %d %.1f 180 5", x + 0.5, y, z + 0.5));
				server.runCommand("item replace entity @p weapon.mainhand with jugcraft:%s[jugcraft:loaded_rounds=1]".formatted(gun));
				context.waitTicks(20);
				context.takeScreenshot("jugcraft_guns_" + gun + "_third_person");
				context.getInput().pressKey(options -> options.keyAttack);
				context.takeScreenshot("jugcraft_guns_" + gun + "_third_person_fired");
				context.waitTicks(10);
			}
			context.runOnClient(client -> client.options.setCameraType(CameraType.THIRD_PERSON_BACK));
			for (String gun : List.of("longhorn_rifle", "warden_pistol")) {
				server.runCommand("item replace entity @p weapon.mainhand with jugcraft:" + gun);
				context.waitTicks(20);
				context.takeScreenshot("jugcraft_guns_" + gun + "_third_person_back");
			}
			context.runOnClient(client -> client.options.setCameraType(CameraType.FIRST_PERSON));
			long posedFrames = context.computeOnClient(client -> GunPose.posed()) - posed;
			Jugcraft.LOGGER.info("[guns] in third person the player was posed holding a gun for {} frames", posedFrames);
			if (posedFrames <= 0) {
				throw new AssertionError("In third person the player was never posed holding a gun (GunPose)");
			}

			server.runCommand("clear @p");
			for (String gun : JugcraftGuns.SPECS.keySet()) {
				server.runCommand("give @p jugcraft:" + gun);
			}
			for (String round : JugcraftGuns.AMMO) {
				server.runCommand("give @p jugcraft:%s 16".formatted(round));
			}
			for (String attachment : JugcraftGuns.ATTACHMENTS.keySet()) {
				server.runCommand("give @p jugcraft:" + attachment);
			}
			server.runCommand("give @p jugcraft:patchwork_carbine[jugcraft:attachments=%s]".formatted(
					snbt(List.of("baffled_silencer", "extended_magazine", "wooden_stock", "vertical_grip"))));
			context.waitTicks(10);
			context.setScreen(() -> new InventoryScreen(Minecraft.getInstance().player));
			context.waitTicks(10);
			context.takeScreenshot("jugcraft_guns_inventory");
			context.setScreen(() -> null);
		}
	}

	/** A list of attachment ids as SNBT, for an item component in a command. */
	private static String snbt(List<String> ids) {
		return ids.stream().map(id -> "\"" + id + "\"").collect(Collectors.joining(",", "[", "]"));
	}

	private static ServerPlayer player(MinecraftServer minecraft) {
		return minecraft.getPlayerList().getPlayers().getFirst();
	}

	/** The husk's health, on the server. */
	private static float health(TestServerContext server, int x, int y, int z) {
		return server.computeOnServer(minecraft -> minecraft.overworld().getEntitiesOfClass(LivingEntity.class,
				new AABB(x - 4, y - 2, z - 10, x + 5, y + 4, z), entity -> BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).getPath().equals("husk"))
				.stream().findFirst().map(LivingEntity::getHealth).orElse(-1.0F));
	}
}

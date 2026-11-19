package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.horses.BreedDimensions;
import io.github.jimbozoomer.jugcraft.horses.BreedHorse;
import io.github.jimbozoomer.jugcraft.horses.HorseBreed;
import io.github.jimbozoomer.jugcraft.horses.HorseBreeds;
import io.github.jimbozoomer.jugcraft.horses.HorseLineage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.CameraType;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;

/**
 * Client gallery and rider/observer test for the horse breeds (docs/features/horse-breeds.md), on a real rendered
 * client: the five breeds tacked (saddle, bridle, Shire and Giant saddlebags) in a row; walking, rearing and grazing;
 * the player riding each breed (seated at the breed's attachment, compared with a vanilla horse ridden the same way);
 * an observer watching a husk ride each breed (the client's copy carries it at the same height as the server's); and a
 * ridden Shire-looking half Friesian on a road, whose road bonus is the same on the client and the server, after a late
 * re-track and a save and reload. Screenshots {@code jugcraft_horse_breeds_*}. Written for the repository's client
 * suite; not run in this change.
 */
public class HorseBreedsClientGameTests implements FabricClientGameTest {
	private static void check(boolean yes, String why) {
		if (!yes) {
			throw new AssertionError(why);
		}
	}

	/** Every breed, west to east, and how far apart (blocks) they stand: room for the giant's 2.625. */
	private static final List<HorseBreed> ROW = HorseBreed.GROUND;
	private static final int GAP = 4;

	@Override
	public void runTest(ClientGameTestContext context) {
		try (TestSingleplayerContext singleplayer = context.worldBuilder()
				.adjustSettings(creator -> creator.setGameMode(WorldCreationUiState.SelectedGameMode.CREATIVE)).create()) {
			singleplayer.getConnection().waitForChunksRender();
			context.runOnClient(client -> {
				if (!client.gui.hud.isHidden()) {
					client.gui.hud.toggle();
				}
			});
			TestServerContext server = singleplayer.getServer();
			for (String rule : List.of("send_command_feedback", "advance_time", "advance_weather", "spawn_mobs")) {
				server.runCommand("gamerule minecraft:" + rule + " false");
			}
			server.runCommand("time set noon");
			server.runCommand("weather clear");
			BlockPos origin = context.computeOnClient(client -> BlockPos.containing(client.player.position()));
			int x = origin.getX();
			int y = origin.getY();
			int z = origin.getZ();
			server.runCommand("fill %d %d %d %d %d %d minecraft:grass_block".formatted(x - 4, y - 1, z - 14, x + 26, y - 1, z + 14));
			server.runCommand("fill %d %d %d %d %d %d minecraft:air".formatted(x - 4, y, z - 14, x + 26, y + 12, z + 14));
			server.runCommand("fill %d %d %d %d %d %d minecraft:dirt_path".formatted(x - 4, y - 1, z + 8, x + 26, y - 1, z + 10));
			context.waitTicks(10);

			// The tacked row: tamed, saddled, the Shire and the Giant with their bags.
			List<Integer> row = server.computeOnServer(minecraft -> {
				ServerPlayer player = minecraft.getPlayerList().getPlayers().get(0);
				List<Integer> ids = new ArrayList<>();
				for (int i = 0; i < ROW.size(); i++) {
					BreedHorse horse = spawn(player.level(), ROW.get(i), x + i * GAP + 0.5, y, z - 4.5, player);
					horse.setItemSlot(EquipmentSlot.SADDLE, new ItemStack(Items.SADDLE));
					if (ROW.get(i).carriesBags) {
						horse.bags().setFitted(true);
					}
					horse.setNoAi(true);
					ids.add(horse.getId());
				}
				return ids;
			});
			context.waitTicks(20);
			check(context.computeOnClient(client -> {
				for (int id : row) {
					if (!(client.level.getEntity(id) instanceof BreedHorse horse) || horse.getItemBySlot(EquipmentSlot.SADDLE).isEmpty()) {
						return false;
					}
					if (horse.breed().carriesBags && !horse.bagsFitted()) {
						return false;
					}
				}
				return true;
			}), "Every client copy is its breed, saddled, with its bags synced");
			camera(server, x + 8, y + 3, z + 6, 160, 15);
			context.waitTicks(10);
			context.takeScreenshot("jugcraft_horse_breeds_tacked_row");
			camera(server, x + 10, y + 2, z - 13, 0, 10);
			context.waitTicks(10);
			context.takeScreenshot("jugcraft_horse_breeds_tacked_row_back");

			// Walking: each walks a few blocks east on its own (AI back on), photographed mid-stride.
			var startingPositions = server.computeOnServer(minecraft -> {
				var positions = new HashMap<Integer, net.minecraft.world.phys.Vec3>();
				for (int id : row) {
					Mob horse = (Mob) minecraft.overworld().getEntity(id);
					positions.put(id, horse.position());
					horse.setNoAi(false);
					horse.getNavigation().moveTo(horse.getX() + 3, horse.getY(), horse.getZ() + 6, 1.0);
				}
				return positions;
			});
			int moved = 0;
			for (int tick = 0; tick < 80 && moved < 3; tick += 4) {
				context.waitTicks(4);
				moved = server.computeOnServer(minecraft -> (int) row.stream().filter(id ->
						minecraft.overworld().getEntity(id).position().subtract(startingPositions.get(id)).horizontalDistanceSqr() >= 0.25).count());
			}
			check(moved >= 3, moved + " breeds walked at least half a block");
			context.takeScreenshot("jugcraft_horse_breeds_walking");

			// Rearing: an untamed horse that is shown an item it does not eat rears (vanilla Horse.mobInteract -> makeMad).
			List<Integer> wild = server.computeOnServer(minecraft -> {
				ServerPlayer player = minecraft.getPlayerList().getPlayers().get(0);
				List<Integer> ids = new ArrayList<>();
				for (int i = 0; i < ROW.size(); i++) {
					BreedHorse horse = spawn(player.level(), ROW.get(i), x + i * GAP + 0.5, y, z + 4.5, null);
					horse.setNoAi(true);
					ids.add(horse.getId());
				}
				return ids;
			});
			context.waitTicks(10);
			camera(server, x + 8, y + 2, z + 13, 180, 8);
			context.waitTicks(10);
			server.runOnServer(minecraft -> {
				ServerPlayer player = minecraft.getPlayerList().getPlayers().get(0);
				player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.DIRT));
				for (int id : wild) {
					((BreedHorse) minecraft.overworld().getEntity(id)).mobInteract(player, InteractionHand.MAIN_HAND);
				}
				player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
			});
			context.waitTicks(8);
			context.takeScreenshot("jugcraft_horse_breeds_rearing");

			// Grazing: vanilla's own idle (a horse on grass, unridden, lowers its head about once in 15 s).
			server.runOnServer(minecraft -> {
				for (int id : wild) {
					((Mob) minecraft.overworld().getEntity(id)).setNoAi(false);
				}
			});
			boolean grazed = false;
			for (int wait = 0; wait < 90 && !grazed; wait++) {
				context.waitTicks(10);
				grazed = server.computeOnServer(minecraft -> {
					for (int id : wild) {
						if (minecraft.overworld().getEntity(id) instanceof AbstractHorse horse && horse.isEating()) {
							return true;
						}
					}
					return false;
				});
			}
			context.takeScreenshot("jugcraft_horse_breeds_grazing");
			check(grazed, "A breed grazes within 45 seconds on grass");

			// The rider: a vanilla horse first, for the reference seat, then each breed.
			context.runOnClient(client -> client.options.setCameraType(CameraType.THIRD_PERSON_BACK));
			double vanillaSeat = ride(context, server, x, y, z - 10, null);
			for (int i = 0; i < ROW.size(); i++) {
				HorseBreed breed = ROW.get(i);
				double seat = ride(context, server, x + i * GAP, y, z - 10, breed);
				double expected = vanillaSeat + BreedDimensions.of(breed.id()).seat() - BreedDimensions.of("horse").seat();
				check(Math.abs(seat - expected) < 0.02, breed.id() + " seats its rider at its attachment: " + seat + " vs " + expected);
				context.takeScreenshot("jugcraft_horse_breeds_rider_" + breed.id());
				context.runOnClient(client -> client.options.setCameraType(CameraType.THIRD_PERSON_FRONT));
				context.waitTicks(5);
				context.takeScreenshot("jugcraft_horse_breeds_rider_" + breed.id() + "_front");
				context.runOnClient(client -> client.options.setCameraType(CameraType.THIRD_PERSON_BACK));
			}
			server.runOnServer(minecraft -> minecraft.getPlayerList().getPlayers().get(0).stopRiding());
			context.runOnClient(client -> client.options.setCameraType(CameraType.FIRST_PERSON));

			// The observer: this player watches a husk ride each breed; its own copy carries the husk as the server does.
			List<int[]> riders = server.computeOnServer(minecraft -> {
				ServerLevel level = minecraft.overworld();
				List<int[]> pairs = new ArrayList<>();
				for (int id : row) {
					BreedHorse horse = (BreedHorse) level.getEntity(id);
					horse.setNoAi(true);
					Mob husk = EntityTypes.HUSK.create(level, EntitySpawnReason.COMMAND);
					husk.snapTo(horse.getX(), horse.getY(), horse.getZ(), 0.0F, 0.0F);
					husk.setNoAi(true);
					level.addFreshEntity(husk);
					husk.startRiding(horse, true, false);
					pairs.add(new int[] {id, husk.getId()});
				}
				return pairs;
			});
			camera(server, x + 8, y + 3, z + 14, 180, 12);
			context.waitTicks(20);
			for (int[] pair : riders) {
				double serverOffset = server.computeOnServer(minecraft -> minecraft.overworld().getEntity(pair[1]).getY()
						- minecraft.overworld().getEntity(pair[0]).getY());
				String seen = context.computeOnClient(client -> {
					Entity horse = client.level.getEntity(pair[0]);
					Entity husk = client.level.getEntity(pair[1]);
					return husk != null && husk.getVehicle() == horse ? String.valueOf(husk.getY() - horse.getY()) : "unmounted";
				});
				check(!seen.equals("unmounted") && Math.abs(Double.parseDouble(seen) - serverOffset) < 0.05,
						"The observer sees the rider seated as the server does: " + seen + " vs " + serverOffset);
			}
			context.takeScreenshot("jugcraft_horse_breeds_observer");

			// Road speed: a ridden Shire-looking half Friesian on the path. Client and server agree on the bonus, also after the
			// horse is re-tracked late and after a save and reload.
			java.util.UUID crossId = server.computeOnServer(minecraft -> {
				ServerPlayer player = minecraft.getPlayerList().getPlayers().get(0);
				BreedHorse horse = spawn(player.level(), HorseBreed.SHIRE, x + 2.5, y, z + 9.5, player);
				horse.setItemSlot(EquipmentSlot.SADDLE, new ItemStack(Items.SADDLE));
				horse.setLineage(HorseLineage.blend(HorseLineage.pure(HorseBreed.SHIRE), HorseLineage.pure(HorseBreed.FRIESIAN)),
						"shire_horse,friesian_horse", 1);
				horse.applyTraits();
				player.startRiding(horse, true, false);
				return horse.getUUID();
			});
			context.waitTicks(20);
			int cross = server.computeOnServer(minecraft -> minecraft.overworld().getEntity(crossId).getId());
			roadAgrees(context, server, cross, "ridden");
			context.takeScreenshot("jugcraft_horse_breeds_road_cross");
			// Late tracking: the player goes far away and comes back; the horse is sent to the client afresh.
			server.runOnServer(minecraft -> minecraft.getPlayerList().getPlayers().get(0).stopRiding());
			server.runCommand("tp @p %d %d %d".formatted(x + 600, y + 40, z));
			context.waitTicks(40);
			server.runCommand("tp @p %d %d %d".formatted(x + 2, y, z + 12));
			context.waitTicks(40);
			singleplayer.getConnection().waitForChunksRender();
			// Far enough to be untracked (and perhaps unloaded and loaded again): look it up afresh.
			int late = server.computeOnServer(minecraft -> minecraft.overworld().getEntity(crossId).getId());
			check(context.computeOnClient(client -> client.level.getEntity(late) instanceof BreedHorse horse
					&& Math.abs(horse.roadBonus() - 0.05) < 1e-6), "Late tracking: the client's copy has the synced bonus");
			// Save and reload: a fresh copy from the saved data, sent to the client as a new entity.
			int reloaded = server.computeOnServer(minecraft -> {
				ServerLevel level = minecraft.overworld();
				BreedHorse horse = (BreedHorse) level.getEntity(crossId);
				TagValueOutput saved = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
				horse.saveWithoutId(saved);
				horse.discard();
				BreedHorse copy = HorseBreeds.type(HorseBreed.SHIRE).create(level, EntitySpawnReason.LOAD);
				copy.load(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), saved.buildResult()));
				level.addFreshEntity(copy);
				minecraft.getPlayerList().getPlayers().get(0).startRiding(copy, true, false);
				return copy.getId();
			});
			context.waitTicks(20);
			roadAgrees(context, server, reloaded, "reloaded");
			server.runOnServer(minecraft -> minecraft.getPlayerList().getPlayers().get(0).stopRiding());
		}
	}

	private static BreedHorse spawn(ServerLevel level, HorseBreed breed, double x, double y, double z, ServerPlayer owner) {
		EntityType<BreedHorse> type = HorseBreeds.type(breed);
		BreedHorse horse = type.create(level, EntitySpawnReason.COMMAND);
		horse.snapTo(x, y, z, 0.0F, 0.0F);
		horse.finalizeSpawn(level, level.getCurrentDifficultyAt(BlockPos.containing(x, y, z)), EntitySpawnReason.COMMAND, null);
		if (owner != null) {
			horse.tameWithName(owner);
		}
		level.addFreshEntity(horse);
		return horse;
	}

	/** Stands the camera (this player) at (x, y, z) on a barrier, facing yaw/pitch. */
	private static void camera(TestServerContext server, int x, int y, int z, int yaw, int pitch) {
		server.runCommand("setblock %d %d %d minecraft:barrier".formatted(x, y - 1, z));
		server.runCommand(String.format(Locale.ROOT, "tp @p %.1f %d %.1f %d %d", x + 0.5, y, z + 0.5, yaw, pitch));
	}

	/** Seats this player on a fresh saddled horse of {@code breed} (null: a vanilla horse); its client-seen seat height. */
	private static double ride(ClientGameTestContext context, TestServerContext server, int x, int y, int z, HorseBreed breed) {
		int id = server.computeOnServer(minecraft -> {
			ServerPlayer player = minecraft.getPlayerList().getPlayers().get(0);
			player.stopRiding();
			AbstractHorse horse = breed == null ? EntityTypes.HORSE.create(player.level(), EntitySpawnReason.COMMAND)
					: spawn(player.level(), breed, x + 0.5, y, z + 0.5, player);
			if (breed == null) {
				horse.snapTo(x + 0.5, y, z + 0.5, 0.0F, 0.0F);
				horse.tameWithName(player);
				player.level().addFreshEntity(horse);
			}
			horse.setItemSlot(EquipmentSlot.SADDLE, new ItemStack(Items.SADDLE));
			horse.setNoAi(true);
			player.startRiding(horse, true, false);
			return horse.getId();
		});
		context.waitTicks(20);
		return context.computeOnClient(client -> client.player.getY() - client.level.getEntity(id).getY());
	}

	/** The server's and this client's road bonus and road speed modifier agree for the horse. */
	private static void roadAgrees(ClientGameTestContext context, TestServerContext server, int id, String when) {
		double serverBonus = server.computeOnServer(minecraft -> ((BreedHorse) minecraft.overworld().getEntity(id)).roadBonus());
		String client = context.computeOnClient(c -> {
			BreedHorse horse = (BreedHorse) c.level.getEntity(id);
			var speed = horse.getAttribute(Attributes.MOVEMENT_SPEED);
			var road = speed == null ? null : speed.getModifier(BreedHorse.ROAD);
			return horse.roadBonus() + " " + (road == null ? 0.0 : road.amount());
		});
		String[] parts = client.split(" ");
		check(Math.abs(Double.parseDouble(parts[0]) - serverBonus) < 1e-6 && Math.abs(serverBonus - 0.05) < 1e-6,
				when + ": the client's synced bonus is the server's (a half Friesian's 0.05): " + client + " vs " + serverBonus);
		check(Math.abs(Double.parseDouble(parts[1]) - serverBonus) < 1e-6,
				when + ": the rider's client, which moves it, applies exactly that bonus on the road: " + client);
	}
}

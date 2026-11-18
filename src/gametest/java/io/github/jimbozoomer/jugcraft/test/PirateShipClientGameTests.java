package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.client.DecorQuads;
import io.github.jimbozoomer.jugcraft.client.PirateShipRenderer;
import io.github.jimbozoomer.jugcraft.menagerie.CrewedVessel.Station;
import io.github.jimbozoomer.jugcraft.menagerie.OwnerPlunderer;
import io.github.jimbozoomer.jugcraft.menagerie.PirateCrew;
import io.github.jimbozoomer.jugcraft.menagerie.PirateDecks;
import io.github.jimbozoomer.jugcraft.menagerie.PirateMotion;
import io.github.jimbozoomer.jugcraft.menagerie.PirateShip;
import io.github.jimbozoomer.jugcraft.menagerie.PirateShipDeck;
import io.github.jimbozoomer.jugcraft.menagerie.PirateShips;
import io.github.jimbozoomer.jugcraft.menagerie.PirateWaters;
import java.util.List;
import java.util.Locale;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.client.renderer.entity.state.IllagerRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * The real pirate ship as a player sees it (docs/features/pirate-ships.md), crewed by PirateCrew.provision, floating
 * in a pool at yaw 0 and yaw 90: its renderer and every exported part are loaded, its four deck squares are on the
 * client with their tops at the deck, and each of the ten crew stands (no riding pose) with its feet on its station on
 * the real hull and faces its station's way. Screenshots from off the starboard quarter, for review of the hull,
 * rig, crew and their footing.
 */
public class PirateShipClientGameTests implements FabricClientGameTest {
	private static final String[] PARTS = {"pirate_ship_hull", "pirate_ship_yard_main", "pirate_ship_sail_main", "pirate_ship_furl_main",
			"pirate_ship_yard_main_top", "pirate_ship_sail_main_top", "pirate_ship_furl_main_top", "pirate_ship_yard_fore",
			"pirate_ship_sail_fore", "pirate_ship_furl_fore", "pirate_ship_yard_fore_top", "pirate_ship_sail_fore_top",
			"pirate_ship_furl_fore_top", "pirate_ship_jib", "pirate_ship_furl_jib", "pirate_ship_flag", "pirate_ship_wheel",
			"pirate_ship_rudder", "pirate_ship_cannon", "pirate_ship_rope"};

	@Override
	public void runTest(ClientGameTestContext context) {
		try (var world = context.worldBuilder().adjustSettings(
				creator -> creator.setGameMode(WorldCreationUiState.SelectedGameMode.CREATIVE)).create()) {
			var server = world.getServer();
			world.getConnection().waitForChunksRender();
			server.runCommand("gamerule minecraft:send_command_feedback false");
			server.runCommand("time set noon");
			server.runCommand("weather clear");
			context.runOnClient(client -> {
				for (String part : PARTS) {
					if (DecorQuads.get(part) == null) {
						throw new AssertionError("The exported part " + part + " should be loaded from pirate_ship_quads.json");
					}
				}
			});
			BlockPos origin = context.computeOnClient(client -> client.player.blockPosition().offset(0, 6, 0));
			int x = origin.getX(), y = origin.getY(), z = origin.getZ();
			// A pool: stone floor, water y-3..y-1 (its top block y-1), open air above; the ship's waterline in y-1.
			Vec3 at = new Vec3(x + 0.5, y - 1 + PirateWaters.WATERLINE, z + 0.5);
			for (float yaw : new float[] {0.0F, 90.0F}) {
				String heading = yaw == 0.0F ? "yaw_0" : "yaw_90";
				server.runCommand("fill %d %d %d %d %d %d minecraft:stone".formatted(x - 18, y - 4, z - 18, x + 18, y - 4, z + 18));
				server.runCommand("fill %d %d %d %d %d %d minecraft:water".formatted(x - 18, y - 3, z - 18, x + 18, y - 1, z + 18));
				server.runCommand("fill %d %d %d %d %d %d minecraft:air".formatted(x - 18, y, z - 18, x + 18, y + 16, z + 18));
				server.runCommand("fill %d %d %d %d %d %d minecraft:stone".formatted(x + 17, y - 3, z - 17, x + 17, y + 8, z - 17));
				int[] shipId = new int[1];
				server.runOnServer(minecraft -> {
					ServerLevel level = minecraft.overworld();
					for (Entity old : level.getEntitiesOfClass(Entity.class, new AABB(at, at).inflate(40.0),
							e -> e instanceof PirateShip || e instanceof OwnerPlunderer)) {
						old.discard();
					}
					PirateShip ship = PirateShips.SHIP.create(level, EntitySpawnReason.COMMAND);
					ship.snapTo(at.x, at.y, at.z, yaw, 0.0F);
					ship.launch(at, false);
					ship.setCrewed(PirateCrew.provision(level, ship) > 0);
					level.addFreshEntityWithPassengers(ship);
					shipId[0] = ship.getId();
				});
				server.runCommand(String.format(Locale.ROOT, "tp @a %.1f %d %.1f 45 20", x + 17.5, y + 9, z - 16.5));
				context.waitTicks(40);
				context.runOnClient(client -> {
					if (!(client.level.getEntity(shipId[0]) instanceof PirateShip ship)) {
						throw new AssertionError("The pirate ship should be on the client at " + heading);
					}
					if (!(client.getEntityRenderDispatcher().getRenderer(ship) instanceof PirateShipRenderer)) {
						throw new AssertionError("The pirate ship should draw with PirateShipRenderer");
					}
					List<PirateShipDeck> decks = client.level.getEntitiesOfClass(PirateShipDeck.class, ship.getBoundingBox().inflate(12.0),
							deck -> deck.ship() == ship);
					if (decks.size() != PirateShipDeck.KEEL.length) {
						throw new AssertionError("Its " + PirateShipDeck.KEEL.length + " deck squares should be on the client, not " + decks.size());
					}
					for (PirateShipDeck deck : decks) {
						if (Math.abs(deck.getBoundingBox().maxY - (ship.getY() + 1.0)) > 1.0E-3) {
							throw new AssertionError("A deck square's top should be the deck, " + (ship.getY() + 1.0) + ", not " + deck.getBoundingBox().maxY);
						}
					}
					List<OwnerPlunderer> crew = ship.getPassengers().stream().filter(OwnerPlunderer.class::isInstance)
							.map(OwnerPlunderer.class::cast).toList();
					if (crew.size() != PirateCrew.STATIONS.size()) {
						throw new AssertionError("Ten crew should ride the real ship at " + heading + ", not " + crew.size());
					}
					for (OwnerPlunderer member : crew) {
						Station station = PirateCrew.STATIONS.get(member.shipStation());
						// On the drawn deck: the seat carried by this tick's swell and heel (PirateMotion via ship.lean), then turned.
						Vec3 seat = ship.position().add(ship.lean(station.seat()).yRot(-ship.getYRot() * Mth.DEG_TO_RAD));
						if (member.position().distanceTo(seat) > 0.05) {
							throw new AssertionError("Station " + member.shipStation() + " should stand on its seat on the real hull at "
									+ heading + ": " + member.position() + " vs " + seat);
						}
						if (!(client.getEntityRenderDispatcher().extractEntity(member, 0.0F) instanceof IllagerRenderState state) || state.isRiding) {
							throw new AssertionError("Station " + member.shipStation() + " should render standing at " + heading);
						}
						if (Math.abs(Mth.wrapDegrees(member.yBodyRot - (ship.getYRot() + station.facing()))) > 0.5F) {
							throw new AssertionError("Station " + member.shipStation() + " should face its station's way at " + heading
									+ ", but faces " + member.yBodyRot);
						}
					}
				});
				context.takeScreenshot("pirate_ship_crewed_" + heading);
			}
			gallery(context, server, at, x, y, z);
			lateTracking(context, server, at, x, y, z);
			server.runOnServer(minecraft -> {
				for (Entity old : minecraft.overworld().getEntitiesOfClass(Entity.class, new AABB(at, at).inflate(40.0),
						e -> e instanceof PirateShip || e instanceof OwnerPlunderer)) {
					old.discard();
				}
			});
		}
	}

	/**
	 * The runtime gallery (14 November), for the owner's review of the refined art: a small-crewed ship (captain, two
	 * gunners, two deck hands) from its four sides with its sails set; hove to, with its sails furled (the client eases
	 * them in over 40 ticks); turning hard toward a waypoint abeam (rudder, wheel, heel and braced yards); and from its deck,
	 * a player standing on it, whom PirateDecks counts on the deck. Screenshots only, besides those two checks.
	 */
	private static void gallery(ClientGameTestContext context, net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext server,
			Vec3 at, int x, int y, int z) {
		int[] shipId = new int[1];
		server.runOnServer(minecraft -> {
			ServerLevel level = minecraft.overworld();
			for (Entity old : level.getEntitiesOfClass(Entity.class, new AABB(at, at).inflate(40.0),
					e -> e instanceof PirateShip || e instanceof OwnerPlunderer)) {
				old.discard();
			}
			PirateShip ship = PirateShips.SHIP.create(level, EntitySpawnReason.COMMAND);
			ship.snapTo(at.x, at.y, at.z, 0.0F, 0.0F);
			ship.launch(at, false);
			ship.setCrewed(PirateCrew.provision(level, ship, PirateCrew.crew(5)) > 0);
			ship.setCrewFormat(PirateShip.SMALL_CREW);
			level.addFreshEntityWithPassengers(ship);
			shipId[0] = ship.getId();
		});
		context.waitTicks(60);
		String[][] views = {{"port", "%.1f %d %.1f 90 8", "-20", "0"}, {"starboard", "%.1f %d %.1f -90 8", "20", "0"},
				{"bow", "%.1f %d %.1f 180 8", "0", "22"}, {"stern", "%.1f %d %.1f 0 8", "0", "-22"}};
		for (String[] view : views) {
			// x grows to port: the port view stands at +x looking -x (yaw 90); the bow view at +z looking -z (yaw 180).
			double dx = -Double.parseDouble(view[2]), dz = Double.parseDouble(view[3]);
			server.runCommand(String.format(Locale.ROOT, "tp @a " + view[1], at.x + dx, y + 3, at.z + dz));
			context.waitTicks(10);
			context.takeScreenshot("pirate_ship_sails_set_" + view[0]);
		}
		server.runOnServer(minecraft -> ((PirateShip) minecraft.overworld().getEntity(shipId[0])).setHeaveTo(true));
		context.waitTicks(60);
		context.runOnClient(client -> {
			if (!(client.level.getEntity(shipId[0]) instanceof PirateShip ship) || ship.sailsSet() || ship.sails(0.0F) > 0.0F) {
				throw new AssertionError("Hove to, the crew should have furled the sails");
			}
		});
		server.runCommand(String.format(Locale.ROOT, "tp @a %.1f %d %.1f 135 18", at.x + 16.0, y + 8, at.z - 14.0));
		context.waitTicks(10);
		context.takeScreenshot("pirate_ship_sails_furled_quarter");
		server.runOnServer(minecraft -> {
			PirateShip ship = (PirateShip) minecraft.overworld().getEntity(shipId[0]);
			ship.setHeaveTo(false);
			io.github.jimbozoomer.jugcraft.menagerie.PirateShipTesting.waypoint(ship, ship.position().add(10.0, 0.0, 4.0));
		});
		context.waitTicks(70);
		context.takeScreenshot("pirate_ship_turning");
		server.runOnServer(minecraft -> {
			PirateShip ship = (PirateShip) minecraft.overworld().getEntity(shipId[0]);
			ship.setHeaveTo(true);
			Vec3 deck = ship.position().add(new Vec3(0.0, 1.0, 2.0).yRot(-ship.getYRot() * Mth.DEG_TO_RAD));
			minecraft.getPlayerList().getPlayers().getFirst().teleportTo(deck.x, deck.y + 0.2, deck.z);
		});
		context.waitTicks(20);
		boolean onDeck = server.computeOnServer(minecraft -> PirateDecks.standingOnDeck(minecraft.getPlayerList().getPlayers().getFirst()));
		if (!onDeck) {
			throw new AssertionError("A player standing on the deck should count as on it (PirateDecks)");
		}
		context.takeScreenshot("pirate_ship_boarding_deck");
	}

	/**
	 * Late tracking (14 November): a small-crewed ship ages on the server, the player goes out of its tracking range and
	 * comes back, so this client tracks it afresh (its local entity is far younger than the server's). The client's swell
	 * phase must still be the server's (game time plus the ship's UUID offset), its crew stand on the deck it draws, the
	 * captain at the wheel is drawn steering, and PirateDecks counts the captain on the deck. A close screenshot of the
	 * captain at the wheel, for review.
	 */
	private static void lateTracking(ClientGameTestContext context, net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext server,
			Vec3 at, int x, int y, int z) {
		// This case measures client retracking, not server unload/reload (which assigns a new runtime entity ID).
		forceTestPool(server, x, z, true);
		try {
			lateTrackingLoaded(context, server, at, x, y, z);
		} finally {
			forceTestPool(server, x, z, false);
		}
	}

	private static void forceTestPool(net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext server,
			int x, int z, boolean forced) {
		server.runOnServer(minecraft -> {
			for (int cx = Math.floorDiv(x - 32, 16); cx <= Math.floorDiv(x + 32, 16); cx++) {
				for (int cz = Math.floorDiv(z - 32, 16); cz <= Math.floorDiv(z + 32, 16); cz++) {
					minecraft.overworld().setChunkForced(cx, cz, forced);
					if (forced) minecraft.overworld().getChunk(cx, cz);
				}
			}
		});
	}

	private static void lateTrackingLoaded(ClientGameTestContext context, net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext server,
			Vec3 at, int x, int y, int z) {
		int[] shipId = new int[1];
		server.runOnServer(minecraft -> {
			ServerLevel level = minecraft.overworld();
			for (Entity old : level.getEntitiesOfClass(Entity.class, new AABB(at, at).inflate(40.0),
					e -> e instanceof PirateShip || e instanceof OwnerPlunderer)) {
				old.discard();
			}
			PirateShip ship = PirateShips.SHIP.create(level, EntitySpawnReason.COMMAND);
			ship.snapTo(at.x, at.y, at.z, 30.0F, 0.0F);
			ship.launch(at, false);
			ship.setCrewed(PirateCrew.provision(level, ship, PirateCrew.crew(5)) > 0);
			ship.setCrewFormat(PirateShip.SMALL_CREW);
			level.addFreshEntityWithPassengers(ship);
			shipId[0] = ship.getId();
		});
		context.waitTicks(200);
		server.runCommand(String.format(Locale.ROOT, "tp @a %.1f %d %.1f", x + 900.5, y + 40, z + 0.5));
		context.waitTicks(60);
		context.runOnClient(client -> {
			if (client.level.getEntity(shipId[0]) != null) {
				throw new AssertionError("Setup: 900 blocks off, the client should have stopped tracking the ship");
			}
		});
		server.runCommand(String.format(Locale.ROOT, "tp @a %.1f %d %.1f 210 25", x - 2.5, y + 3, z - 9.5));
		context.waitTicks(40);
		float[] onServer = new float[2];
		server.runOnServer(minecraft -> {
			PirateShip ship = (PirateShip) minecraft.overworld().getEntity(shipId[0]);
			onServer[0] = ship.swellPhase(0.0F);
			onServer[1] = ship.tickCount;
		});
		context.runOnClient(client -> {
			if (!(client.level.getEntity(shipId[0]) instanceof PirateShip ship)) {
				throw new AssertionError("The ship should be tracked again");
			}
			if (ship.tickCount > onServer[1] - 150) {
				throw new AssertionError("Setup: the client's ship should be a late, young copy (" + ship.tickCount + " vs " + onServer[1] + ")");
			}
			// The two tick threads are sampled separately: the client can be just behind or ahead of the server.
			// A few ticks of sampling skew is allowed; a fresh local entity-age phase (200+ ticks adrift) is not.
			float lag = PirateMotion.wrap((ship.swellPhase(0.0F) - onServer[0]) * 360.0F / PirateMotion.PERIOD) * PirateMotion.PERIOD / 360.0F;
			if (Math.abs(lag) > 3.5F) {
				throw new AssertionError("A late client's swell phase should be the server's, not " + ship.swellPhase(0.0F) + " vs " + onServer[0]);
			}
			for (Entity passenger : ship.getPassengers()) {
				if (passenger instanceof OwnerPlunderer member) {
					Station station = PirateCrew.STATIONS.get(member.shipStation());
					Vec3 seat = ship.position().add(ship.lean(station.seat()).yRot(-ship.getYRot() * Mth.DEG_TO_RAD));
					if (member.position().distanceTo(seat) > 0.05) {
						throw new AssertionError("Station " + member.shipStation() + " should stand on the drawn deck of a late-tracked ship: "
								+ member.position() + " vs " + seat);
					}
					if (member.shipStation() == PirateCrew.HELM && !PirateDecks.standingOnDeck(member)) {
						throw new AssertionError("The captain at the wheel stands on the deck (PirateDecks)");
					}
				}
			}
		});
		context.takeScreenshot("pirate_ship_captain_at_wheel_late_tracked");
	}
}

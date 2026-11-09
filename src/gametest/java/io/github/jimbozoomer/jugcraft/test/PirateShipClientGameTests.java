package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.client.DecorQuads;
import io.github.jimbozoomer.jugcraft.client.PirateShipRenderer;
import io.github.jimbozoomer.jugcraft.menagerie.CrewedVessel.Station;
import io.github.jimbozoomer.jugcraft.menagerie.OwnerPlunderer;
import io.github.jimbozoomer.jugcraft.menagerie.PirateCrew;
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
	private static final String[] PARTS = {"pirate_ship_hull", "pirate_ship_sail_fore", "pirate_ship_sail_main", "pirate_ship_jib",
			"pirate_ship_flag", "pirate_ship_wheel", "pirate_ship_rudder", "pirate_ship_cannon"};

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
						Vec3 seat = ship.position().add(station.seat().yRot(-ship.getYRot() * Mth.DEG_TO_RAD));
						if (member.position().distanceTo(seat) > 1.0E-3) {
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
			server.runOnServer(minecraft -> {
				for (Entity old : minecraft.overworld().getEntitiesOfClass(Entity.class, new AABB(at, at).inflate(40.0),
						e -> e instanceof PirateShip || e instanceof OwnerPlunderer)) {
					old.discard();
				}
			});
		}
	}
}

package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.menagerie.CrewedVessel;
import io.github.jimbozoomer.jugcraft.menagerie.CrewedVessel.Station;
import io.github.jimbozoomer.jugcraft.menagerie.OwnerPlunderer;
import io.github.jimbozoomer.jugcraft.menagerie.PirateCrew;
import io.github.jimbozoomer.jugcraft.test.TestVessels.StubVessel;
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
 * A pirate ship's crew as a player sees them (docs/features/pirate-ship-crews.md), at yaw 0 and yaw 90: ten plunderers
 * at their stations on the test-only stub vessel ({@link TestVessels}), which draws nothing, so a plank deck, a mast
 * and a crow's-nest slab stand in for the hull. Every crew member must render standing (no riding pose) with its feet
 * at its seat's height and face its station's way, while an unlinked plunderer in an ordinary boat still sits. The
 * screenshots show the stand-in deck only: once the real ship and its renderer (PirateShip, PirateShipRenderer) are in,
 * they should be taken again on the real hull.
 */
public class PirateCrewClientGameTests implements FabricClientGameTest {
	@Override
	public void runTest(ClientGameTestContext context) {
		try (var world = context.worldBuilder().adjustSettings(
				creator -> creator.setGameMode(WorldCreationUiState.SelectedGameMode.CREATIVE)).create()) {
			var server = world.getServer();
			world.getConnection().waitForChunksRender();
			server.runCommand("gamerule minecraft:send_command_feedback false");
			server.runCommand("time set noon");
			server.runCommand("weather clear");
			BlockPos origin = context.computeOnClient(client -> client.player.blockPosition().offset(0, 4, 0));
			int x = origin.getX(), y = origin.getY(), z = origin.getZ();
			// The ship sits at (x + 0.5, y, z + 0.5); the viewer stands on a pillar off its starboard quarter, looking at it.
			Vec3 center = new Vec3(x + 0.5, y, z + 0.5);
			for (float yaw : new float[] {0.0F, 90.0F}) {
				String heading = yaw == 0.0F ? "yaw_0" : "yaw_90";
				server.runCommand("fill %d %d %d %d %d %d minecraft:stone".formatted(x - 14, y - 1, z - 14, x + 14, y - 1, z + 14));
				server.runCommand("fill %d %d %d %d %d %d minecraft:air".formatted(x - 14, y, z - 14, x + 14, y + 14, z + 14));
				server.runCommand("fill %d %d %d %d %d %d minecraft:stone".formatted(x + 13, y, z - 13, x + 13, y + 7, z - 13));
				// The deck (local x -2.5..2.5, z -7.5..7.5, its top at local y 1.0) turned with the ship.
				if (yaw == 0.0F) {
					server.runCommand("fill %d %d %d %d %d %d minecraft:oak_planks".formatted(x - 2, y, z - 7, x + 2, y, z + 7));
				} else {
					server.runCommand("fill %d %d %d %d %d %d minecraft:oak_planks".formatted(x - 7, y, z - 2, x + 7, y, z + 2));
				}
				// The fore mast under the crow's nest (local 0, *, 1), topped by a bottom slab whose top is the lookout's 9.5.
				Vec3 mast = center.add(new Vec3(0.0, 0.0, 1.0).yRot(-yaw * Mth.DEG_TO_RAD));
				BlockPos foot = BlockPos.containing(mast.x, y + 1, mast.z);
				server.runCommand("fill %d %d %d %d %d %d minecraft:oak_fence".formatted(foot.getX(), y + 1, foot.getZ(), foot.getX(), y + 8, foot.getZ()));
				server.runCommand("setblock %d %d %d minecraft:oak_slab[type=bottom]".formatted(foot.getX(), y + 9, foot.getZ()));
				int[] vesselId = new int[1];
				server.runOnServer(minecraft -> {
					ServerLevel level = minecraft.overworld();
					AABB area = new AABB(center, center).inflate(40.0);
					for (Entity old : level.getEntitiesOfClass(Entity.class, area,
							e -> e instanceof StubVessel || e instanceof OwnerPlunderer || e instanceof net.minecraft.world.entity.vehicle.boat.AbstractBoat)) {
						old.discard();
					}
					StubVessel vessel = TestVessels.STUB.create(level, EntitySpawnReason.COMMAND);
					vessel.snapTo(center.x, center.y, center.z, yaw, 0.0F);
					PirateCrew.provision(level, vessel);
					level.addFreshEntityWithPassengers(vessel);
					vesselId[0] = vessel.getId();
				});
				// For contrast, an unlinked plunderer in an ordinary boat on the stone, which keeps the seated pose.
				server.runCommand(String.format(Locale.ROOT,
						"summon minecraft:oak_boat %.1f %d %.1f {Passengers:[{id:\"jugcraft:supps_plunderer\",PersistenceRequired:1b}]}",
						x + 9.5, y, z - 6.5));
				server.runCommand(String.format(Locale.ROOT, "tp @a %.1f %d %.1f 45 20", x + 13.5, y + 8, z - 12.5));
				context.waitTicks(30);
				context.runOnClient(client -> {
					if (!(client.level.getEntity(vesselId[0]) instanceof StubVessel vessel)) {
						throw new AssertionError("The stub ship should be on the client at " + heading);
					}
					List<OwnerPlunderer> crew = vessel.getPassengers().stream().filter(OwnerPlunderer.class::isInstance)
							.map(OwnerPlunderer.class::cast).toList();
					if (crew.size() != 10) {
						throw new AssertionError("Ten crew should ride the ship on the client at " + heading + ", not " + crew.size());
					}
					for (OwnerPlunderer member : crew) {
						int index = member.shipStation();
						if (index < 0 || index >= PirateCrew.STATIONS.size()) {
							throw new AssertionError("A crew member's station should be synced to the client, not " + index);
						}
						Station station = PirateCrew.STATIONS.get(index);
						Vec3 seat = vessel.position().add(station.seat().yRot(-yaw * Mth.DEG_TO_RAD));
						if (member.position().distanceTo(seat) > 1.0E-3) {
							throw new AssertionError("Station " + index + " should stand with its feet on its seat at " + heading + ": "
									+ member.position() + " vs " + seat);
						}
						if (!(client.getEntityRenderDispatcher().extractEntity(member, 0.0F) instanceof IllagerRenderState state) || state.isRiding) {
							throw new AssertionError("Station " + index + " should render standing at " + heading + ", not in a riding pose");
						}
						if (Math.abs(Mth.wrapDegrees(member.yBodyRot - (yaw + station.facing()))) > 0.5F) {
							throw new AssertionError("Station " + index + " should face the ship's yaw plus its facing at " + heading
									+ ", but faces " + member.yBodyRot);
						}
					}
					List<OwnerPlunderer> sitters = client.level.getEntitiesOfClass(OwnerPlunderer.class,
							new AABB(x + 9.5, y, z - 6.5, x + 9.5, y, z - 6.5).inflate(3.0),
							p -> p.getVehicle() != null && !(p.getVehicle() instanceof CrewedVessel));
					if (sitters.size() != 1 || !(client.getEntityRenderDispatcher().extractEntity(sitters.getFirst(), 0.0F)
							instanceof IllagerRenderState state) || !state.isRiding) {
						throw new AssertionError("An unlinked plunderer in a boat should still sit (" + sitters.size() + " found)");
					}
				});
				context.takeScreenshot("pirate_crew_stations_" + heading);
			}
			server.runOnServer(minecraft -> {
				ServerLevel level = minecraft.overworld();
				for (Entity old : level.getEntitiesOfClass(Entity.class, new AABB(center, center).inflate(40.0),
						e -> e instanceof StubVessel || e instanceof OwnerPlunderer)) {
					old.discard();
				}
			});
		}
	}
}

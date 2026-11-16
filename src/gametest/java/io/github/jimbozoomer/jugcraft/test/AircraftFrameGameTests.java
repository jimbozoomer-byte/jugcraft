package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.agriculture.HotAirBalloon;
import io.github.jimbozoomer.jugcraft.agriculture.HotAirBalloonItem;
import io.github.jimbozoomer.jugcraft.aircraft.Aircraft;
import io.github.jimbozoomer.jugcraft.aircraft.AircraftAttitude;
import io.github.jimbozoomer.jugcraft.aircraft.AircraftBullet;
import io.github.jimbozoomer.jugcraft.aircraft.AircraftInput;
import io.github.jimbozoomer.jugcraft.aircraft.AircraftSpec;
import io.github.jimbozoomer.jugcraft.aircraft.BalloonAttitude;
import io.github.jimbozoomer.jugcraft.aircraft.JugcraftAircraft;
import io.github.jimbozoomer.jugcraft.artillery.ArtilleryShell;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * Riders, shots and turrets are where the aircraft is drawn, and a hot-air balloon's envelope leans the way it drifts
 * (November 13; scripts/tests/test_aircraft_frames.py runs the same comparisons without Minecraft).
 *
 * <p>Every aircraft is flown with the real steering and flight code from several headings, turning both ways and
 * climbing and diving, so that it is pitched and banked. Then the riders' positions, the shots the real firing code
 * launches and the turret aim the server syncs are compared with where the client draws the supplied meshes: the
 * renderer's body turn ({@link AircraftAttitude#body}, lifted by the ground offset), the weapon models translated to
 * their mounts, and the renderer's walk over the supplied parts with the render state's variables (as in
 * {@link AircraftBankingGameTests}). The firing tick is flown without the turn key, so the nose does not move between
 * aiming the pilot and the shot.
 */
public class AircraftFrameGameTests {
	private static final float[] YAWS = {0, 90, 180, -90, 37.5F};
	private static final String ARENA = "jugcraft-test:drone_tower";
	/** The rotary cannon's and telescope's barrel node. */
	private static final List<String> BARREL = List.of("yaw", "pitch", "roll");

	private interface Visit {
		void vertex(List<String> path, Matrix4f frame, float[] pixel);
	}

	/** Every group of a supplied model as AircraftRenderer draws it from {@code base}: its frame, by its path of names. */
	private static Map<List<String>, Matrix4f> draw(String model, Matrix4f base, AircraftAttitude.Controls c, Visit visit) {
		var frames = new LinkedHashMap<List<String>, Matrix4f>();
		walk(AircraftBankingGameTests.models().get(model), base, List.of(), c, frames, visit);
		return frames;
	}

	private static void walk(List<AircraftBankingGameTests.Part> parts, Matrix4f parent, List<String> path, AircraftAttitude.Controls c,
			Map<List<String>, Matrix4f> frames, Visit visit) {
		for (var p : parts) {
			float[] at = AircraftBankingGameTests.animated(p, "position", c);
			float[] turn = AircraftBankingGameTests.animated(p, "rotation", c);
			var m = new Matrix4f(parent).translate((p.offset()[0] + at[0]) / 16, (p.offset()[1] + at[1]) / 16, (p.offset()[2] + at[2]) / 16)
					.rotate(AircraftAttitude.part(p.rest(), turn));
			var here = new ArrayList<>(path);
			here.add(p.name());
			frames.putIfAbsent(List.copyOf(here), m);
			if (visit != null) {
				for (var v : p.vertices()) {
					visit.vertex(here, m, v);
				}
			}
			walk(p.children(), m, here, c, frames, visit);
		}
	}

	/** The render state's animation variables for this craft now, as AircraftRenderer.extractRenderState reads them. */
	private static AircraftBankingGameTests.Inputs inputs(Aircraft a) {
		return new AircraftBankingGameTests.Inputs(0, a.animatedControl(1, 1), a.animatedControl(2, 1), a.animatedControl(0, 1), a.getXRot(),
				a.bank(1), a.aimYaw(), a.aimPitch(), a.recoil());
	}

	/** The renderer's model frame, relative to the entity's position: the ground offset, then the body turn. */
	private static Matrix4f body(Aircraft a) {
		return new Matrix4f().translate(0, (float) a.spec().groundOffset(), 0).rotate(AircraftAttitude.body(a.getYRot(), a.getXRot(), a.bank(1)));
	}

	private static Vector3f vec(Vec3 v) {
		return new Vector3f((float) v.x, (float) v.y, (float) v.z);
	}

	private static float degrees(Vector3f a, Vector3f b) {
		return (float) Math.toDegrees(new Vector3f(a).angle(b));
	}

	/** Where the flights start: well above the test structure, so a dive does not reach the ground. */
	private static Vec3 sky(GameTestHelper h) {
		return Vec3.atCenterOf(h.absolutePos(new BlockPos(2, 2, 2))).add(0, 80, 0);
	}

	/**
	 * A new craft flown with the real steering and flight code by {@code pilot}: on the throttle, turning with {@code key}
	 * (+1 the left key, -1 the right key, as AircraftClient sends) and climbing (+1) or diving (-1), for {@code ticks}.
	 */
	private static Aircraft fly(GameTestHelper h, ServerPlayer pilot, AircraftSpec spec, float yaw, int key, int climb, int ticks) {
		var level = h.getLevel();
		var from = sky(h);
		var a = JugcraftAircraft.TYPES.get(spec.name()).create(level, EntitySpawnReason.COMMAND);
		a.snapTo(from.x, from.y, from.z, yaw, 0);
		a.setOwner(pilot.getUUID());
		level.addFreshEntity(a);
		for (int i = 0; i < spec.sockets().size(); i++) {
			if (spec.sockets().get(i).type().equals("boiler")) {
				a.inventory().setItem(i, new ItemStack(Items.COAL, 16));
			}
		}
		pilot.startRiding(a);
		for (int t = 0; t < ticks; t++) {
			a.steer(pilot, new AircraftInput(1, key, climb, false, false));
			a.tickCount++;
			a.tick();
		}
		return a;
	}

	/** One more tick of flight without the turn key (the nose holds still), past every weapon's cooldown, firing or not. */
	private static void hold(Aircraft a, ServerPlayer pilot, int climb, boolean fire) {
		a.tickCount += 25;
		a.steer(pilot, new AircraftInput(1, 0, climb, fire, false));
		a.tick();
	}

	private static String flight(AircraftSpec spec, float yaw, int key, int climb) {
		return spec.name() + " from yaw " + yaw + (key < 0 ? " turning right" : " turning left") + (climb > 0 ? ", climbing" : ", diving");
	}

	/** The craft is pitched and banked, so the checks exercise both. */
	private static void attitude(GameTestHelper h, Aircraft a, int key, String what) {
		h.assertTrue(Math.abs(a.getXRot()) > (a.spec().aeroplane() ? 8 : 2) && a.bank(1) * key < 0,
				"Pitched and banked in flight: " + what + ", pitch " + a.getXRot() + ", bank " + a.bank(1));
	}

	private static List<Integer> weaponSockets(AircraftSpec spec) {
		var out = new ArrayList<Integer>();
		for (int i = 0; i < spec.sockets().size(); i++) {
			if (spec.sockets().get(i).type().equals("weapon")) {
				out.add(i);
			}
		}
		return out;
	}

	private static void equip(Aircraft a, List<Integer> sockets, int socket, String weapon) {
		for (int i : sockets) {
			a.inventory().setItem(i, ItemStack.EMPTY);
		}
		a.inventory().setItem(sockets.get(socket), new ItemStack(JugcraftAircraft.ITEMS.get(weapon)));
	}

	private static void look(ServerPlayer pilot, Aircraft a, float yaw, float pitch) {
		pilot.setYRot(a.getYRot() + yaw);
		pilot.setXRot(Mth.clamp(a.getXRot() + pitch, -85, 85));
	}

	/** The premise of the seat and muzzle fix: vanilla's Vec3.xRot turns the other way from the renderer's pitch. */
	@GameTest
	public void vanillaVec3XRotTurnsTheOtherWayFromTheRenderersPitch(GameTestHelper h) {
		for (float pitch : new float[] {-18, 18, 40}) {
			for (float yaw : YAWS) {
				var view = Vec3.directionFromRotation(pitch, yaw);
				var ahead = new Vec3(0, 0, 1).xRot(-pitch * Mth.DEG_TO_RAD).yRot(-yaw * Mth.DEG_TO_RAD);
				h.assertTrue(view.distanceTo(ahead) < 1e-3, "A point ahead of an entity is (0,0,1).xRot(-pitch).yRot(-yaw): pitch " + pitch);
				var nose = AircraftAttitude.body(yaw, pitch, 0).transform(new Vector3f(0, 0, 1));
				h.assertTrue(nose.distance(vec(view)) < 1e-3F, "The renderer's body turn points the nose along the same view: pitch " + pitch);
				var old = new Vec3(0, 0, 1).xRot(pitch * Mth.DEG_TO_RAD).yRot(-yaw * Mth.DEG_TO_RAD);
				h.assertTrue(old.y * view.y < 0, "The old seat and muzzle transform tipped the nose the other way: pitch " + pitch);
			}
		}
		h.succeed();
	}

	@GameTest
	public void seatsAreWhereTheyAreDrawnInFlight(GameTestHelper h) {
		var pilot = DeathGraveGameTests.player(h);
		var riders = new ArrayList<ServerPlayer>();
		for (int i = 0; i < 3; i++) {
			riders.add(DeathGraveGameTests.player(h));
		}
		int checked = 0;
		for (var spec : AircraftSpec.ALL.values()) {
			for (float yaw : YAWS) {
				for (int key : new int[] {-1, 1}) {
					for (int climb : new int[] {1, -1}) {
						String what = flight(spec, yaw, key, climb);
						var a = fly(h, pilot, spec, yaw, key, climb, 80);
						attitude(h, a, key, what);
						var drawn = body(a);
						for (int count = 1; count <= spec.seats(); count++) {
							if (count > 1) {
								riders.get(count - 2).startRiding(a);
							}
							h.assertTrue(a.getPassengers().size() == count && a.getFirstPassenger() == pilot, "Riders aboard: " + what);
							for (int i = 0; i < count; i++) {
								var seat = spec.seat(i, count);
								var at = new Matrix4f(drawn).transformPosition(new Vector3f((float) seat.x, (float) seat.y, (float) seat.z));
								var riding = vec(a.getPassengerRidingPosition(a.getPassengers().get(i)).subtract(a.position()));
								h.assertTrue(at.distance(riding) < 1e-3F, "Seat " + i + " of " + count + " is where it is drawn: " + what + ", "
										+ at.distance(riding) + " blocks off");
								checked++;
							}
						}
						for (var rider : riders) {
							rider.stopRiding();
						}
						pilot.stopRiding();
						a.discard();
					}
				}
			}
		}
		h.assertTrue(checked > 0, "Seats were checked");
		h.succeed();
	}

	@GameTest
	public void shotsLeaveTheDrawnWeaponsInFlight(GameTestHelper h) {
		var level = h.getLevel();
		var pilot = DeathGraveGameTests.player(h);
		// Creative pilots fire without loading ammunition (Aircraft.ammunition).
		pilot.setGameMode(GameType.CREATIVE);
		// The heavy crossbow's bolt lies on top of its stock (|x| <= 2 px), and its front is its furthest point ahead.
		float[] crossbow = {Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY};
		draw("heavy_crossbow", new Matrix4f(), AircraftBankingGameTests.Inputs.of(0, 0, 0), (path, frame, v) -> {
			if (Math.abs(v[0]) <= 2) {
				crossbow[0] = Math.max(crossbow[0], v[1] / 16);
			}
			crossbow[1] = Math.max(crossbow[1], v[2] / 16);
		});
		int checked = 0;
		for (var spec : AircraftSpec.ALL.values()) {
			var groups = spec.definition().getAsJsonArray("weaponMounts");
			var sockets = weaponSockets(spec);
			if (groups == null || groups.isEmpty() || sockets.isEmpty()) {
				continue;
			}
			for (float yaw : YAWS) {
				for (int key : new int[] {-1, 1}) {
					for (int climb : new int[] {1, -1}) {
						String flight = flight(spec, yaw, key, climb);
						var a = fly(h, pilot, spec, yaw, key, climb, 80);
						attitude(h, a, key, flight);
						for (int s = 0; s < sockets.size() && s < groups.size(); s++) {
							var group = groups.get(s).getAsJsonObject();
							for (String weapon : List.of("rotary_cannon", "heavy_crossbow", "bomb_bay")) {
								String kind = weapon.equals("bomb_bay") ? "DROP" : weapon.equals("heavy_crossbow") ? "FRONT" : "ROTATING";
								var mounts = group.getAsJsonArray(kind);
								if (mounts == null || mounts.isEmpty()) {
									continue;
								}
								String what = weapon + " on mount group " + s + ", " + flight;
								equip(a, sockets, s, weapon);
								if (weapon.equals("rotary_cannon")) {
									look(pilot, a, 35, -10);
								} else {
									look(pilot, a, 0, 0);
								}
								hold(a, pilot, climb, true);
								var box = a.getBoundingBox().inflate(16);
								List<? extends Entity> shots = switch (weapon) {
									case "rotary_cannon" -> level.getEntitiesOfClass(AircraftBullet.class, box, e -> e.getOwner() == pilot);
									case "heavy_crossbow" -> level.getEntitiesOfClass(AbstractArrow.class, box,
											e -> e.getOwner() == pilot && e.entityTags().contains("jugcraft_aircraft_arrow"));
									default -> level.getEntitiesOfClass(ArtilleryShell.class, box, e -> e.getOwner() == pilot);
								};
								h.assertTrue(shots.size() == mounts.size(), "One shot from each mount: " + what + ", " + shots.size() + " of " + mounts.size());
								var lookDirection = vec(pilot.getLookAngle());
								var used = new ArrayList<Entity>();
								for (var element : mounts) {
									var m = element.getAsJsonObject();
									var base = body(a).translate((float) m.get("x").getAsDouble(), (float) m.get("y").getAsDouble(), (float) m.get("z").getAsDouble());
									Vector3f expected;
									Vector3f direction;
									if (weapon.equals("rotary_cannon")) {
										// The drawn barrels' muzzle: the centre of their ends, furthest along the barrel node's +Z.
										var ends = new ArrayList<Vector3f>();
										float[] far = {Float.NEGATIVE_INFINITY};
										var frames = draw(weapon, base, inputs(a), (path, frame, v) -> {
											if (path.size() > BARREL.size() && path.subList(0, BARREL.size()).equals(BARREL)) {
												if (v[2] > far[0] + 1e-4F) {
													far[0] = v[2];
													ends.clear();
												}
												if (v[2] > far[0] - 1e-4F) {
													ends.add(frame.transformPosition(new Vector3f(v[0] / 16, v[1] / 16, v[2] / 16)));
												}
											}
										});
										expected = new Vector3f();
										for (var end : ends) {
											expected.add(end);
										}
										expected.div(ends.size());
										direction = frames.get(BARREL).transformDirection(new Vector3f(0, 0, 1)).normalize();
										h.assertTrue(degrees(direction, lookDirection) < 1, "The drawn cannon points where the pilot looks: " + what + ", "
												+ degrees(direction, lookDirection) + " degrees off");
									} else if (weapon.equals("heavy_crossbow")) {
										// From the drawn bolt line, 1.25 blocks out along the pilot's look.
										var bolt = new Matrix4f(base).transformPosition(new Vector3f(0, crossbow[0], 0));
										expected = new Vector3f(lookDirection).mul(1.25F).add(bolt);
										direction = new Vector3f(lookDirection).add(0, .1F, 0).normalize();
										var forward = new Matrix4f(base).transformDirection(new Vector3f(0, 0, 1)).normalize();
										h.assertTrue(degrees(forward, lookDirection) < 2, "The pilot looks along the drawn crossbow: " + what);
									} else {
										expected = null;
										direction = null;
									}
									Entity shot = null;
									float nearest = Float.MAX_VALUE;
									for (var candidate : shots) {
										if (used.contains(candidate)) {
											continue;
										}
										var at = vec(candidate.position().subtract(a.position()));
										float d = expected != null ? at.distance(expected)
												: at.distance(new Matrix4f(base).transformPosition(new Vector3f()).add(0, -.8F, 0));
										if (d < nearest) {
											nearest = d;
											shot = candidate;
										}
									}
									used.add(shot);
									var at = vec(shot.position().subtract(a.position()));
									var velocity = vec(shot.getDeltaMovement().subtract(a.getDeltaMovement()));
									if (weapon.equals("rotary_cannon")) {
										h.assertTrue(nearest < .03F, "The round leaves the drawn barrels' muzzle: " + what + ", " + nearest + " blocks off");
										h.assertTrue(degrees(velocity, direction) < 3, "The round flies along the drawn barrels: " + what);
									} else if (weapon.equals("heavy_crossbow")) {
										h.assertTrue(nearest < .02F, "The bolt leaves 1.25 blocks out from the drawn bolt line: " + what + ", " + nearest + " off");
										var forward = new Matrix4f(base).transformDirection(new Vector3f(0, 0, 1)).normalize();
										var bolt = new Matrix4f(base).transformPosition(new Vector3f(0, crossbow[0], 0));
										var out = new Vector3f(at).sub(bolt);
										float ahead = out.dot(forward);
										float aside = new Vector3f(out).sub(new Vector3f(forward).mul(ahead)).length();
										h.assertTrue(ahead > crossbow[1] && aside < .06F, "The bolt starts on the drawn bolt line, ahead of the crossbow: " + what
												+ ", " + aside + " aside, " + ahead + " ahead");
										h.assertTrue(degrees(velocity, direction) < .5F, "The bolt flies along the pilot's look, lifted a little: " + what);
									} else {
										// Below the drawn bomb bay, within its outline seen from above, falling with the craft.
										float lowest = Float.MAX_VALUE, minX = Float.MAX_VALUE, maxX = -Float.MAX_VALUE, minZ = Float.MAX_VALUE,
												maxZ = -Float.MAX_VALUE;
										var bay = new ArrayList<Vector3f>();
										draw(weapon, base, inputs(a), (path, frame, v) -> bay.add(frame.transformPosition(new Vector3f(v[0] / 16, v[1] / 16, v[2] / 16))));
										for (var p : bay) {
											lowest = Math.min(lowest, p.y);
											minX = Math.min(minX, p.x);
											maxX = Math.max(maxX, p.x);
											minZ = Math.min(minZ, p.z);
											maxZ = Math.max(maxZ, p.z);
										}
										h.assertTrue(at.y < lowest - .05F && at.x >= minX && at.x <= maxX && at.z >= minZ && at.z <= maxZ,
												"The bomb drops from below the drawn bay: " + what + ", " + (at.y - lowest) + " below its bottom");
										h.assertTrue(velocity.distance(0, -.2F, 0) < 1e-3F, "The bomb falls with the craft: " + what);
									}
									checked++;
								}
								for (var shot : shots) {
									shot.discard();
								}
							}
						}
						pilot.stopRiding();
						a.discard();
					}
				}
			}
		}
		h.assertTrue(checked > 0, "Shots were checked");
		h.succeed();
	}

	@GameTest
	public void turretsPointWhereThePilotLooksInFlight(GameTestHelper h) {
		var pilot = DeathGraveGameTests.player(h);
		float[][] looks = {{35, -10}, {-70, 15}, {150, -20}, {-20, 30}};
		int checked = 0;
		for (var spec : AircraftSpec.ALL.values()) {
			var groups = spec.definition().getAsJsonArray("weaponMounts");
			var sockets = weaponSockets(spec);
			if (groups == null || groups.isEmpty() || sockets.isEmpty() || !groups.get(0).getAsJsonObject().has("ROTATING")) {
				continue;
			}
			var mounts = groups.get(0).getAsJsonObject().getAsJsonArray("ROTATING");
			for (float yaw : YAWS) {
				for (int key : new int[] {-1, 1}) {
					for (int climb : new int[] {1, -1}) {
						String flight = flight(spec, yaw, key, climb);
						var a = fly(h, pilot, spec, yaw, key, climb, 80);
						attitude(h, a, key, flight);
						for (String weapon : List.of("rotary_cannon", "telescope")) {
							equip(a, sockets, 0, weapon);
							for (float[] at : looks) {
								look(pilot, a, at[0], at[1]);
								hold(a, pilot, climb, false);
								String what = weapon + ", looking " + at[0] + " right and " + -at[1] + " up of the nose, " + flight;
								h.assertTrue((a.weapons() & 7) == (weapon.equals("rotary_cannon") ? 1 : 4), "Equipped and synced: " + what);
								var lookDirection = vec(pilot.getLookAngle());
								for (var element : mounts) {
									var m = element.getAsJsonObject();
									var base = body(a).translate((float) m.get("x").getAsDouble(), (float) m.get("y").getAsDouble(), (float) m.get("z").getAsDouble());
									var direction = draw(weapon, base, inputs(a), null).get(BARREL).transformDirection(new Vector3f(0, 0, 1)).normalize();
									h.assertTrue(degrees(direction, lookDirection) < 1, "The drawn " + weapon + " points where the pilot looks: " + what + ", "
											+ degrees(direction, lookDirection) + " degrees off");
									checked++;
								}
							}
						}
						pilot.stopRiding();
						a.discard();
					}
				}
			}
		}
		h.assertTrue(checked > 0, "Turrets were checked");
		h.succeed();
	}

	@GameTest
	public void warshipTurretTurnsAndTiltsTowardTheLook(GameTestHelper h) {
		var pilot = DeathGraveGameTests.player(h);
		var spec = AircraftSpec.ALL.get("warship");
		float[][] looks = {{0, 0}, {40, 0}, {-40, 0}, {0, -25}, {0, 25}, {30, -20}};
		for (float yaw : YAWS) {
			for (int key : new int[] {-1, 1}) {
				String flight = flight(spec, yaw, key, 1);
				var a = fly(h, pilot, spec, yaw, key, 1, 60);
				for (float[] at : looks) {
					look(pilot, a, at[0], at[1]);
					hold(a, pilot, 1, false);
					String what = "looking " + at[0] + " right and " + -at[1] + " up of the nose, " + flight;
					var frames = draw("warship", body(a), inputs(a), null);
					var bow = frames.entrySet().stream().filter(e -> e.getKey().getLast().equals("cross_bow")).findFirst().orElseThrow().getValue();
					// The turret sits in the gondola, which is turned half round: its crossbow points along its own -Z.
					var inverse = AircraftAttitude.body(a.getYRot(), a.getXRot(), a.bank(1)).conjugate();
					var forward = inverse.transform(bow.transformDirection(new Vector3f(0, 0, -1)).normalize());
					var seen = inverse.transform(vec(pilot.getLookAngle()));
					if (at[0] == 0 && at[1] == 0) {
						h.assertTrue(forward.z > .99F, "With the pilot looking ahead the turret's crossbow points ahead: " + what + ", " + forward);
					}
					if (Math.abs(seen.x) > .2F) {
						h.assertTrue(forward.x * seen.x > 0, "The turret turns toward the side the pilot looks: " + what + ", " + forward);
					}
					if (Math.abs(seen.y) > .2F) {
						h.assertTrue(forward.y * seen.y > 0, "The turret tilts the way the pilot looks: " + what + ", " + forward);
					}
				}
				pilot.stopRiding();
				a.discard();
			}
		}
		h.succeed();
	}

	/**
	 * At the turret's travel (November 13 follow-up): Aircraft clamps the synced aim pitch to 80 degrees either way in the
	 * body's frame, and the drawn rotary cannon and telescope read the clamped aim. On pitched and banked craft, with the
	 * pilot looking just inside and past that limit (aim pitches 76, 84 and 88 degrees up and down, at three aim yaws):
	 * <ul>
	 * <li>cannon rounds fly along the drawn barrels and start at their muzzle; inside the limit that is the pilot's look;</li>
	 * <li>the telescope fires nothing, and is drawn on the same clamped aim;</li>
	 * <li>crossbow bolts fly along the pilot's look, lifted 0.1, at any pitch: pinned by design (the feature record says
	 * weapons follow the pilot's aim, and the supplied crossbow has no aim joint, so its drawn bore stays fixed).</li>
	 * </ul>
	 * The firing tick holds the turn key, so the bank holds; each look is classed inside or past the limit from the
	 * attitude the server aimed with.
	 */
	@GameTest
	public void weaponsAtTheTurretTravelLimit(GameTestHelper h) {
		var level = h.getLevel();
		var pilot = DeathGraveGameTests.player(h);
		pilot.setGameMode(GameType.CREATIVE);
		float[] aimPitches = {-88, -84, -76, 76, 84, 88};
		float[] aimYaws = {-120, 0, 45};
		int inside = 0, past = 0;
		for (String name : List.of("biplane", "bamboo_hopper", "quadrocopter")) {
			var spec = AircraftSpec.ALL.get(name);
			var sockets = weaponSockets(spec);
			var group = spec.definition().getAsJsonArray("weaponMounts").get(0).getAsJsonObject();
			for (int key : new int[] {-1, 1}) {
				for (int climb : new int[] {1, -1}) {
					String flight = flight(spec, 37.5F, key, climb);
					var a = fly(h, pilot, spec, 37.5F, key, climb, 80);
					attitude(h, a, key, flight);
					for (String weapon : List.of("rotary_cannon", "telescope", "heavy_crossbow")) {
						equip(a, sockets, 0, weapon);
						var mounts = group.getAsJsonArray(weapon.equals("heavy_crossbow") ? "FRONT" : "ROTATING");
						for (float aimPitch : aimPitches) {
							for (float aimYaw : aimYaws) {
								var world = AircraftAttitude.body(a.getYRot(), a.getXRot(), a.bank(1)).transform(vec(Vec3.directionFromRotation(aimPitch, aimYaw)));
								pilot.setXRot((float) -Math.toDegrees(Math.asin(Mth.clamp(world.y, -1, 1))));
								pilot.setYRot((float) Math.toDegrees(Math.atan2(-world.x, world.z)));
								// Hundreds of manually ticked shots must stay in the loaded
								// fixture. Preserve attitude/velocity, but reset translation;
								// otherwise later projectiles can spawn in inaccessible chunks.
								a.setPos(sky(h));
								a.tickCount += 25;
								a.steer(pilot, new AircraftInput(1, key, climb, true, false));
								a.tick();
								var look = vec(pilot.getLookAngle());
								float wanted = AircraftAttitude.aim(a.getYRot(), a.getXRot(), a.bank(1), pilot.getLookAngle())[1];
								boolean beyond = Math.abs(wanted) > 80;
								String what = weapon + ", aim pitch " + wanted + " (" + (beyond ? "past" : "inside") + " the limit), aim yaw " + aimYaw + ", " + flight;
								h.assertTrue(Math.abs(a.aimPitch() - Mth.clamp(wanted, -80, 80)) < 1e-3F, "The synced aim pitch is clamped to the turret's travel: " + what);
								if (beyond) {
									past++;
								} else {
									inside++;
								}
								var box = a.getBoundingBox().inflate(16);
								var rounds = level.getEntitiesOfClass(AircraftBullet.class, box, e -> e.getOwner() == pilot);
								var bolts = level.getEntitiesOfClass(AbstractArrow.class, box, e -> e.getOwner() == pilot && e.entityTags().contains("jugcraft_aircraft_arrow"));
								List<? extends Entity> shots = weapon.equals("rotary_cannon") ? rounds : bolts;
								if (weapon.equals("telescope")) {
									h.assertTrue(rounds.isEmpty() && bolts.isEmpty(), "The telescope fires nothing: " + what);
								} else {
									h.assertTrue(shots.size() == mounts.size(), "One shot from each mount: " + what + ", " + shots.size() + " of " + mounts.size());
								}
								for (var element : mounts) {
									var m = element.getAsJsonObject();
									var base = body(a).translate((float) m.get("x").getAsDouble(), (float) m.get("y").getAsDouble(), (float) m.get("z").getAsDouble());
									if (weapon.equals("heavy_crossbow")) {
										// Pinned: the bolt follows the look; the drawn crossbow does not turn.
										var bolt = new Vector3f(look).add(0, .1F, 0).normalize();
										var nose = new Matrix4f(base).transformDirection(new Vector3f(0, 0, 1)).normalize();
										for (var shot : shots) {
											var velocity = vec(shot.getDeltaMovement().subtract(a.getDeltaMovement()));
											h.assertTrue(degrees(velocity, bolt) < .5F, "The bolt flies along the pilot's look, lifted 0.1: " + what);
											h.assertTrue(Math.abs(degrees(velocity, nose) - degrees(bolt, nose)) < .5F, "The drawn crossbow stays fixed: " + what);
										}
										continue;
									}
									var frames = draw(weapon, base, inputs(a), null);
									var barrel = frames.get(BARREL);
									var direction = new Matrix4f(barrel).transformDirection(new Vector3f(0, 0, 1)).normalize();
									float off = degrees(direction, look);
									h.assertTrue(beyond ? Math.abs(off - (Math.abs(wanted) - 80)) < .5F : off < 1,
											"The drawn " + weapon + " points at the look, or as near as its travel allows: " + what + ", " + off + " degrees off");
									if (weapon.equals("rotary_cannon")) {
										var muzzle = new Matrix4f(barrel).transformPosition(new Vector3f(0, 0, 1));
										Entity nearest = null;
										float distance = Float.MAX_VALUE;
										for (var shot : shots) {
											float d = vec(shot.position().subtract(a.position())).distance(muzzle);
											if (d < distance) {
												distance = d;
												nearest = shot;
											}
										}
										var velocity = vec(nearest.getDeltaMovement().subtract(a.getDeltaMovement()));
										h.assertTrue(distance < .03F, "The round starts at the drawn barrels' muzzle: " + what + ", " + distance + " blocks off");
										h.assertTrue(degrees(velocity, direction) < 3, "The round flies along the drawn barrels: " + what + ", "
												+ degrees(velocity, direction) + " degrees off");
									}
								}
								for (var shot : rounds) {
									shot.discard();
								}
								for (var shot : bolts) {
									shot.discard();
								}
							}
						}
					}
					pilot.stopRiding();
					a.discard();
				}
			}
		}
		h.assertTrue(inside > 0 && past > 0, "Looks inside and past the turret's travel were tried: " + inside + " inside, " + past + " past");
		h.succeed();
	}

	/** The envelope's top, leaned for a drift, less where it stands without one, as HotAirBalloonRenderer draws it. */
	private static Vector3f lean(float yaw, Vec3 motion) {
		float[] lean = BalloonAttitude.lean(motion.x, motion.z);
		var top = (float) (HotAirBalloon.THROAT + HotAirBalloon.ENVELOPE_HEIGHT);
		var throat = (float) HotAirBalloon.THROAT;
		var leaned = new Matrix4f().translate(0, throat, 0).rotate(BalloonAttitude.envelope(yaw, lean[0], lean[1])).translate(0, -throat, 0)
				.transformPosition(new Vector3f(0, top, 0));
		var still = new Matrix4f().translate(0, throat, 0).rotate(BalloonAttitude.envelope(yaw, 0, 0)).translate(0, -throat, 0)
				.transformPosition(new Vector3f(0, top, 0));
		return leaned.sub(still);
	}

	private static void leansWithTheDrift(GameTestHelper h, HotAirBalloon balloon, String what) {
		var motion = balloon.getDeltaMovement();
		var moved = lean(balloon.getYRot(), motion);
		var across = new Vector3f(moved.x, 0, moved.z);
		var drift = new Vector3f((float) motion.x, 0, (float) motion.z);
		h.assertTrue(across.length() > 1e-3F && degrees(across, drift) < 2,
				"The envelope's top leans the way the balloon drifts: " + what + ", drift " + motion + ", lean " + moved);
	}

	/**
	 * Balloons placed facing five ways, aloft, each set drifting five ways and flown one tick by the real flight code
	 * (the wind aloft pulls the drift a little); then left to drift on the wind for three seconds, with a pilot so they
	 * keep their heat. The renderer's drift lean ({@link BalloonAttitude}, without the slow idle sway it adds) must lean
	 * the envelope's top the way the balloon moves, whichever way its basket faces.
	 */
	@GameTest(structure = ARENA, skyAccess = true, maxTicks = 200)
	public void balloonEnvelopeLeansTheWayItDriftsFromEveryPlacement(GameTestHelper h) {
		var level = h.getLevel();
		double[][] drifts = {{.15, 0}, {-.15, 0}, {0, .15}, {0, -.15}, {.1, -.1}};
		var balloons = new ArrayList<HotAirBalloon>();
		for (int i = 0; i < YAWS.length; i++) {
			var ground = new BlockPos(6 + 8 * i, 0, 22);
			h.setBlock(ground, Blocks.STONE);
			var balloon = HotAirBalloonItem.setUp(level, h.absolutePos(ground), HotAirBalloon.Kind.HARVEST, YAWS[i], 0);
			h.assertTrue(balloon != null, "A balloon is set up facing " + YAWS[i]);
			h.assertTrue(Math.abs(Mth.wrapDegrees(balloon.getYRot() - YAWS[i])) < 1e-3F, "It faces as placed: " + YAWS[i]);
			balloon.setPos(balloon.getX(), balloon.getY() + 6, balloon.getZ());
			balloon.setHeat(.53F);
			for (double[] drift : drifts) {
				balloon.setDeltaMovement(drift[0], 0, drift[1]);
				balloon.tick();
				leansWithTheDrift(h, balloon, "placed facing " + YAWS[i] + ", set drifting " + drift[0] + ", " + drift[1]);
			}
			balloon.setDeltaMovement(Vec3.ZERO);
			DeathGraveGameTests.player(h).startRiding(balloon);
			balloons.add(balloon);
		}
		h.runAfterDelay(60, () -> {
			int drifting = 0;
			for (var balloon : balloons) {
				if (balloon.getDeltaMovement().horizontalDistance() > .005) {
					leansWithTheDrift(h, balloon, "placed facing " + balloon.getYRot() + ", drifting on the wind");
					drifting++;
				}
			}
			h.assertTrue(drifting > 0, "The balloons aloft drift on the wind");
			for (var balloon : balloons) {
				balloon.ejectPassengers();
				balloon.discard();
			}
			h.succeed();
		});
	}
}

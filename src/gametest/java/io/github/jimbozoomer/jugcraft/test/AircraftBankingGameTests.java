package io.github.jimbozoomer.jugcraft.test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.jimbozoomer.jugcraft.aircraft.AircraftAttitude;
import io.github.jimbozoomer.jugcraft.aircraft.AircraftInput;
import io.github.jimbozoomer.jugcraft.aircraft.AircraftSpec;
import io.github.jimbozoomer.jugcraft.aircraft.JugcraftAircraft;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Aircraft bank into their turns: a right turn lowers the right wingtip and a left turn the left one, on all seven
 * supplied aircraft, in every camera view (docs/TODO.md, "Aircraft banking is still reversed").
 *
 * <p>This runs on the server with the real flight code (steering, the yaw it turns, {@code Aircraft.bank}) and the
 * attitude arithmetic the client renderer draws with ({@link AircraftAttitude}: the body turn, each supplied part's
 * turn and the supplied animation expressions), over the supplied meshes in assets/jugcraft/aircraft_models.json.
 * Restated here are only the renderer's walk over the parts (translate by the part's offset in pixels, then turn it)
 * and the camera's basis. No view rolls the camera: vanilla's camera turns by yaw and pitch only, F5 in front adds
 * half a turn, and Jugcraft's AircraftCameraMixin only lengthens the F5 distance.
 */
public class AircraftBankingGameTests {
	/** A supplied part: its offset and rest turn from the owner's file, its animation, its vertices in pixels. */
	private record Part(String name, float[] offset, float[] rest, JsonObject animation, List<float[]> vertices,
			List<Part> children) {
	}

	/** A vertex as drawn: the names of its part and the part's ancestors, and where it lands, in blocks from the entity. */
	private record Point(List<String> path, Vector3f world) {
	}

	/** One frame's animation variables, as the render state supplies them. */
	private record Inputs(float engineRotation, float steering, float climb, float throttle, float bodyPitch,
			float bodyBank, float aimYaw, float aimPitch, float recoil) implements AircraftAttitude.Controls {
		/** Steering is +1 for the left key and -1 for the right key; the engine is held still. */
		static Inputs of(float steering, float pitch, float bank) {
			return new Inputs(0, steering, 0, 1, pitch, bank, 0, 0, 0);
		}
	}

	private static final Vector3f UP = new Vector3f(0, 1, 0);
	private static final float[] YAWS = {0, 90, 180, -90, 37.5F};
	private static final Pattern SIDE = Pattern.compile("(^|_)(left|right)(_|$|\\d)");
	private static Map<String, List<Part>> models;

	private static synchronized Map<String, List<Part>> models() {
		if (models == null) {
			try (var in = AircraftAttitude.class.getResourceAsStream("/assets/jugcraft/aircraft_models.json")) {
				var root = JsonParser.parseReader(new InputStreamReader(Objects.requireNonNull(in, "aircraft_models.json"),
						StandardCharsets.UTF_8)).getAsJsonObject();
				var loaded = new HashMap<String, List<Part>>();
				for (var e : root.entrySet()) {
					loaded.put(e.getKey(), parts(e.getValue().getAsJsonArray()));
				}
				models = loaded;
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}
		return models;
	}

	private static float[] vec(JsonArray a) {
		return new float[] {a.get(0).getAsFloat(), a.get(1).getAsFloat(), a.get(2).getAsFloat()};
	}

	private static List<Part> parts(JsonArray array) {
		var out = new ArrayList<Part>();
		for (var element : array) {
			var d = element.getAsJsonObject();
			var vertices = new ArrayList<float[]>();
			for (var quad : d.getAsJsonArray("quads")) {
				for (var corner : quad.getAsJsonObject().getAsJsonArray("vertices")) {
					vertices.add(vec(corner.getAsJsonArray()));
				}
			}
			out.add(new Part(d.get("name").getAsString(), vec(d.getAsJsonArray("offset")), vec(d.getAsJsonArray("rotation")),
					d.has("animation") ? d.getAsJsonObject("animation") : new JsonObject(), vertices,
					parts(d.getAsJsonArray("children"))));
		}
		return out;
	}

	/** Every vertex of a craft where AircraftRenderer draws it: ground offset and body turn, then each part's offset and turn. */
	private static List<Point> draw(AircraftSpec spec, float yaw, AircraftAttitude.Controls c) {
		var out = new ArrayList<Point>();
		var body = new Matrix4f().translate(0, (float) spec.groundOffset(), 0)
				.rotate(AircraftAttitude.body(yaw, c.bodyPitch(), c.bodyBank()));
		walk(models().get(spec.name()), body, List.of(), c, out);
		return out;
	}

	private static float[] animated(Part p, String key, AircraftAttitude.Controls c) {
		var a = p.animation().getAsJsonObject(key);
		float[] out = {0, 0, 0};
		if (a != null) {
			for (int i = 0; i < 3; i++) {
				String k = List.of("x", "y", "z").get(i);
				if (a.has(k)) {
					out[i] = AircraftAttitude.expression(a.get(k).getAsString(), c);
				}
			}
		}
		return out;
	}

	private static void walk(List<Part> parts, Matrix4f parent, List<String> path, AircraftAttitude.Controls c,
			List<Point> out) {
		for (var p : parts) {
			float[] at = animated(p, "position", c);
			float[] turn = animated(p, "rotation", c);
			var m = new Matrix4f(parent).translate((p.offset()[0] + at[0]) / 16, (p.offset()[1] + at[1]) / 16,
					(p.offset()[2] + at[2]) / 16).rotate(AircraftAttitude.part(p.rest(), turn));
			var here = new ArrayList<>(path);
			here.add(p.name());
			for (var v : p.vertices()) {
				out.add(new Point(here, m.transformPosition(new Vector3f(v[0] / 16, v[1] / 16, v[2] / 16))));
			}
			walk(p.children(), m, here, c, out);
		}
	}

	/** The entity's level heading at a yaw, by Minecraft's own rotation-to-direction arithmetic (yaw 0 is +Z). */
	private static Vector3f heading(float yaw) {
		var v = Vec3.directionFromRotation(0, yaw);
		return new Vector3f((float) v.x, 0, (float) v.z).normalize();
	}

	/** The pilot's right hand: heading cross up (facing +Z, the right is -X). */
	private static Vector3f right(float yaw) {
		return heading(yaw).cross(UP, new Vector3f());
	}

	/** The outermost tenth of the drawn span toward one side: that side's wingtip (or envelope edge). */
	private static int[] tip(List<Point> points, Vector3f side) {
		float far = Float.NEGATIVE_INFINITY, near = Float.POSITIVE_INFINITY;
		for (var p : points) {
			float d = p.world().dot(side);
			far = Math.max(far, d);
			near = Math.min(near, d);
		}
		float cut = far - (far - near) * .1F;
		return IntStream.range(0, points.size()).filter(i -> points.get(i).world().dot(side) >= cut).toArray();
	}

	private static Vector3f mean(List<Point> points, int[] which) {
		var m = new Vector3f();
		for (int i : which) {
			m.add(points.get(i).world());
		}
		return m.div(which.length);
	}

	private static int[] matching(List<Point> points, Predicate<List<String>> part) {
		return IntStream.range(0, points.size()).filter(i -> part.test(points.get(i).path())).toArray();
	}

	@GameTest
	public void rightAndLeftTurnsLowerTheInsideWingtipInEveryView(GameTestHelper h) {
		var world = h.getLevel();
		var pilot = DeathGraveGameTests.player(h);
		var from = pilot.position().add(0, 60, 0);
		for (var spec : AircraftSpec.ALL.values()) {
			for (float start : YAWS) {
				// AircraftClient sends the left key minus the right key: -1 is the right key, +1 the left key.
				for (int key : new int[] {-1, 1}) {
					String what = spec.name() + (key < 0 ? " right" : " left") + " turn from yaw " + start;
					var a = JugcraftAircraft.TYPES.get(spec.name()).create(world, EntitySpawnReason.COMMAND);
					a.snapTo(from.x, from.y, from.z, start, 0);
					a.setOwner(pilot.getUUID());
					world.addFreshEntity(a);
					for (int i = 0; i < spec.sockets().size(); i++) {
						if (spec.sockets().get(i).type().equals("boiler")) {
							a.inventory().setItem(i, new ItemStack(Items.COAL, 16));
						}
					}
					pilot.startRiding(a);
					for (int t = 0; t < 60; t++) {
						a.steer(pilot, new AircraftInput(1, key, 0, false, false));
						a.tickCount++;
						a.tick();
					}
					float yaw = a.getYRot(), pitch = a.getXRot(), turn = a.animatedControl(1, 1), power = a.power(), bank = a.bank(1);
					var look = a.getLookAngle();
					pilot.stopRiding();
					a.discard();

					// The server: the right key raises the yaw, swinging the heading toward the pilot's starting right.
					h.assertTrue(key < 0 ? yaw > start : yaw < start, "Yaw follows the key: " + what + ", yaw " + yaw);
					h.assertTrue(Math.signum(heading(yaw).dot(right(start))) == -key, "Heading swings toward the key: " + what);
					h.assertTrue(new Vector3f((float) look.x, 0, (float) look.z).normalize().dot(heading(yaw)) > .999F,
							"Entity view vector is the heading the renderer turns the nose to: " + what);
					h.assertTrue(power > .5F && turn * key > .5F, "Powered, smoothed steering: " + what);
					h.assertTrue(bank == AircraftAttitude.bank(spec, turn, power) && bank * key < 0,
							"Entity bank uses the shared formula and rolls positive in a right turn: " + what + ", bank " + bank);

					// The drawn craft: the inside wingtip drops and the outside one rises.
					var level = draw(spec, yaw, Inputs.of(0, pitch, 0));
					var banked = draw(spec, yaw, Inputs.of(turn, pitch, bank));
					var starboard = right(yaw);
					int[] rightTip = tip(level, starboard), leftTip = tip(level, new Vector3f(starboard).negate());
					float rightRise = mean(banked, rightTip).y - mean(level, rightTip).y;
					float leftRise = mean(banked, leftTip).y - mean(level, leftTip).y;
					int[] inside = key < 0 ? rightTip : leftTip;
					float insideRise = key < 0 ? rightRise : leftRise, outsideRise = key < 0 ? leftRise : rightRise;
					h.assertTrue(insideRise < -.02F && outsideRise > .02F,
							"Banks into the turn: " + what + "; right tip " + rightRise + ", left tip " + leftRise);

					// Each view: first person and F5 behind look along the pilot's yaw, F5 in front looks back from
					// ahead. On screen the wingtip on the side the nose swings toward must drop.
					var noseSwing = heading(yaw + (key < 0 ? 1 : -1)).sub(heading(yaw));
					for (float view : new float[] {yaw, yaw + 180}) {
						var camera = new Quaternionf().rotationYXZ((float) Math.PI - view * Mth.DEG_TO_RAD, 0, 0);
						var screenUp = camera.transform(new Vector3f(0, 1, 0));
						var screenRight = camera.transform(new Vector3f(1, 0, 0));
						float side = mean(level, inside).dot(screenRight), toward = noseSwing.dot(screenRight);
						float drop = mean(banked, inside).dot(screenUp) - mean(level, inside).dot(screenUp);
						h.assertTrue(Math.signum(side) == Math.signum(toward) && drop < -.02F,
								"On screen the wingtip on the turning side drops: " + what + ", camera yaw " + view);
					}
				}
			}
		}
		h.succeed();
	}

	@GameTest
	public void suppliedAircraftFaceTheirHeadingWithThePortWingOnPlusX(GameTestHelper h) {
		// Minecraft's convention, which the renderer's body turn relies on: yaw 0 faces +Z and yaw 90 faces -X.
		h.assertTrue(heading(0).dot(new Vector3f(0, 0, 1)) > .9999F && heading(90).dot(new Vector3f(-1, 0, 0)) > .9999F,
				"Yaw 0 faces +Z and yaw 90 faces -X");
		for (float yaw : YAWS) {
			var turn = AircraftAttitude.body(yaw, 0, 0);
			h.assertTrue(turn.transform(new Vector3f(0, 0, 1)).dot(heading(yaw)) > .9999F, "Model +Z turns to the heading, yaw " + yaw);
			h.assertTrue(turn.transform(new Vector3f(1, 0, 0)).dot(right(yaw)) < -.9999F, "Model +X turns to the pilot's left, yaw " + yaw);
		}
		for (var spec : AircraftSpec.ALL.values()) {
			String name = spec.name();
			// The owner's seats: the pilot (seat 0) is never behind a passenger, and is ahead of them when there are any.
			var layouts = spec.definition().getAsJsonArray("passengerPositions");
			boolean nose = false;
			for (int count = 2; count <= layouts.size(); count++) {
				for (int i = 1; i < count; i++) {
					double pilot = spec.seat(0, count).z, other = spec.seat(i, count).z;
					h.assertTrue(pilot >= other, "Pilot seat is not behind a passenger: " + name);
					nose |= pilot > other;
				}
			}
			// A single-seat craft's forward-firing mounts are ahead of its pilot.
			if (layouts.size() == 1) {
				for (var mount : spec.definition().getAsJsonArray("weaponMounts")) {
					var front = mount.getAsJsonObject().getAsJsonArray("FRONT");
					if (front == null) {
						continue;
					}
					for (var f : front) {
						h.assertTrue(f.getAsJsonObject().get("z").getAsDouble() > spec.seat(0, 1).z, "Front mount ahead of the pilot: " + name);
						nose = true;
					}
				}
			}
			h.assertTrue(nose, "The owner's seats or front mounts put the nose at +Z: " + name);

			// The owner's part names: parts called left sit at +X and right at -X. The warship's chests name their own
			// left_side/right_side panels from each chest's front, so a strong majority is required, not every one.
			var rest = draw(spec, 0, Inputs.of(0, 0, 0));
			var sides = new HashMap<String, float[]>();
			for (var p : rest) {
				for (int depth = p.path().size(); depth > 0; depth--) {
					var matcher = SIDE.matcher(p.path().get(depth - 1));
					if (matcher.find()) {
						var sum = sides.computeIfAbsent(String.join("/", p.path().subList(0, depth)) + "|" + matcher.group(2), k -> new float[2]);
						sum[0] += p.world().x;
						sum[1]++;
						break;
					}
				}
			}
			int agree = 0, disagree = 0;
			for (var e : sides.entrySet()) {
				float x = e.getValue()[0] / e.getValue()[1];
				boolean left = e.getKey().endsWith("|left");
				if (Math.abs(x) < 1F / 32) {
					continue;
				}
				if (left == x > 0) {
					agree++;
				} else {
					disagree++;
				}
			}
			h.assertTrue(agree >= 4 * disagree, "Parts named left are at +X and right at -X: " + name + " " + agree + "/" + disagree);
			if (Set.of("bamboo_hopper", "cargo_airship", "warship").contains(name)) {
				h.assertTrue(agree > 0, "Named side parts found: " + name);
			}
			// Rudders are at the tail, behind the origin.
			int[] rudders = matching(rest, path -> path.stream().anyMatch(n -> n.equals("rudder") || n.endsWith("_rudder")));
			if (rudders.length > 0) {
				h.assertTrue(mean(rest, rudders).z < 0, "Rudders are aft (-Z): " + name);
			}
		}
		h.succeed();
	}

	@GameTest
	public void warshipEnvelopeAddsToTheBodyBank(GameTestHelper h) {
		var spec = AircraftSpec.ALL.get("warship");
		var envelope = models().get("warship").stream().filter(p -> p.name().equals("envelope")).findFirst().orElseThrow();
		h.assertTrue("variable.balloon_roll".equals(envelope.animation().getAsJsonObject("rotation").get("z").getAsString()),
				"The supplied envelope rolls by balloon_roll");
		float bank = AircraftAttitude.bank(spec, -1, 1);
		for (float yaw : YAWS) {
			var level = draw(spec, yaw, Inputs.of(0, 0, 0));
			var bodyOnly = draw(spec, yaw, Inputs.of(0, 0, bank));
			var full = draw(spec, yaw, Inputs.of(-1, 0, bank));
			int[] rightTip = tip(level, right(yaw));
			float bodyRise = mean(bodyOnly, rightTip).y - mean(level, rightTip).y;
			float fullRise = mean(full, rightTip).y - mean(level, rightTip).y;
			h.assertTrue(bodyRise < -.02F && fullRise < bodyRise - .02F,
					"In a right turn the envelope's own roll lowers its right side further: yaw " + yaw + ", body " + bodyRise + ", with envelope " + fullRise);
		}
		h.succeed();
	}

	@GameTest
	public void ruddersAndControlSticksDeflectTowardTheTurn(GameTestHelper h) {
		int checked = 0;
		for (var spec : AircraftSpec.ALL.values()) {
			for (float yaw : YAWS) {
				for (int key : new int[] {-1, 1}) {
					var level = draw(spec, yaw, Inputs.of(0, 0, 0));
					var steered = draw(spec, yaw, Inputs.of(key, 0, 0));
					var toward = right(yaw).mul(-key);
					var aft = heading(yaw).negate();
					// A rudder's trailing edge swings toward the turn; a control stick's top leans toward it.
					for (String surface : new String[] {"rudder", "controller"}) {
						for (var part : surfaces(level, surface)) {
							int[] all = matching(level, path -> path.contains(part));
							Vector3f direction = surface.equals("rudder") ? aft : UP;
							float far = Float.NEGATIVE_INFINITY;
							for (int i : all) {
								far = Math.max(far, level.get(i).world().dot(direction));
							}
							final float edge = far - 1F / 64;
							int[] end = IntStream.of(all).filter(i -> level.get(i).world().dot(direction) >= edge).toArray();
							float moved = mean(steered, end).sub(mean(level, end)).dot(toward);
							h.assertTrue(moved > .005F, spec.name() + " " + part + " deflects toward the "
									+ (key < 0 ? "right" : "left") + " turn, yaw " + yaw + ": " + moved);
							checked++;
						}
					}
				}
			}
		}
		h.assertTrue(checked > 0, "Supplied rudders and control sticks were found");
		h.succeed();
	}

	/** The names of a craft's parts that are rudders or the turn stick ("controller"). */
	private static List<String> surfaces(List<Point> points, String kind) {
		return points.stream().flatMap(p -> p.path().stream()).distinct()
				.filter(n -> kind.equals("rudder") ? n.equals("rudder") || n.endsWith("_rudder") : n.equals(kind)).toList();
	}
}

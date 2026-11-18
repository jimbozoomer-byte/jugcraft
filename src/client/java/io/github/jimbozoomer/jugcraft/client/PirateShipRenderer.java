package io.github.jimbozoomer.jugcraft.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.menagerie.CrewedVessel;
import io.github.jimbozoomer.jugcraft.menagerie.PirateCrew;
import io.github.jimbozoomer.jugcraft.menagerie.PirateMotion;
import io.github.jimbozoomer.jugcraft.menagerie.PirateShip;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;

/**
 * Draws a moving pirate ship (docs/features/pirate-ships.md) from the quads tools/pirate_ship.py exports
 * (assets/jugcraft/pirate_ship_quads.json, through {@link DecorQuads}) and its rig (assets/jugcraft/pirate_ship_rig.json):
 * <ul>
 * <li>the hull on the swell: the heave, the yaw, the roll (swell and heel) and the pitch, at the phase the server's crew
 * seats use ({@link PirateShip#swellPhase}), so the crew stand on the deck drawn here;</li>
 * <li>four square sails, each three parts about its yard: the yard (with its lifts) braced round its own mast to the
 * wind ({@link PirateMotion#brace}, never past the sail's brace limit), the canvas swayed with the headway and scaled to
 * how far the crew have it set, and the furled bundle growing as it is taken in; the jib set and furled about the
 * bowsprit's tip;</li>
 * <li>the running rigging (sheets and braces) laid as 16-pixel lengths of rope between points that move with the yards
 * and canvas, so each rope is fast at both ends in every pose;</li>
 * <li>the flag streaming downwind from its staff (struck when the ship is beaten), the wheel and rudder turned by the
 * helm, and a gun at each port turned to its broadside.</li>
 * </ul>
 * Frame: ship-local pixels (16 to a block), x to port, y up from the waterline, z to the bow, unturned; the rig file
 * says how each part is placed (its "frame" note), exactly as tools/pirate_ship.py's frames() and rope_ends() do for the
 * checks and previews. A part missing from the export, or a moving part missing from the rig, is not drawn.
 */
public class PirateShipRenderer extends EntityRenderer<PirateShip, PirateShipRenderer.State> {
	private static final String HULL = "pirate_ship_hull";
	private static final String[] SQUARE = {"main", "main_top", "fore", "fore_top"};
	private static final String JIB = "pirate_ship_jib";
	private static final String JIB_FURL = "pirate_ship_furl_jib";
	private static final String FLAG = "pirate_ship_flag";
	private static final String WHEEL = "pirate_ship_wheel";
	private static final String RUDDER = "pirate_ship_rudder";
	private static final String CANNON = "pirate_ship_cannon";
	private static final String ROPE = "pirate_ship_rope";
	/** Degrees the wheel turns at full helm, and the rudder; how far the canvas sways and the flag flutters. */
	private static final float WHEEL_TURN = 120.0F;
	private static final float RUDDER_TURN = 25.0F;
	private static final float SWAY = 1.0F;
	private static final float FLUTTER = 8.0F;
	/** A part scaled to this share or less is not drawn (tools/pirate_ship.py SHOWN). */
	private static final float SHOWN = 0.04F;
	/** Where a furled sail's sheet starts, in its yard's frame (pixels below the yard; tools/pirate_ship.py rope_ends). */
	private static final float FURLED_CLEW_Y = -3.0F;
	/** How far the gun's muzzle face lies forward of its trunnion (pixels; tools/pirate_ship.py). */
	private static final float BARREL = 8.2F;
	private static @Nullable Rig rig;

	public static final class State extends EntityRenderState {
		float yRot;
		float rudder;
		float headway;
		float wind;
		/** The swell's phase (PirateShip.swellPhase), the same as the server's crew seats use. */
		float phase;
		/** How far the sails are set, 0 furled to 1 set (eased on the client). */
		float sails;
		boolean beaten;
	}

	/** One end of a running rope: the frame it moves with ("hull", "yard_X" or "sail_X") and a point in that part's pixels. */
	record End(String frame, float[] point) {
	}

	/** The pivots, each square sail's brace axis and limit, the running ropes, the guns' trunnions and the culling box. */
	record Rig(Map<String, float[]> pivots, Map<String, Float> axes, Map<String, Float> limits, List<End[]> ropes, float[][] cannons,
			@Nullable AABB culling) {
	}

	/** Forgets the rig on every client resource reload (F3+T, a resource pack change), so new pivots are read again. */
	public static void register() {
		net.fabricmc.fabric.api.resource.v1.ResourceLoader.get(net.minecraft.server.packs.PackType.CLIENT_RESOURCES).registerReloadListener(
				Jugcraft.id("pirate_ship_rig"), (net.minecraft.server.packs.resources.ResourceManagerReloadListener) resources -> rig = null);
	}

	public PirateShipRenderer(EntityRendererProvider.Context context) {
		super(context);
		shadowRadius = 0.0F;
	}

	/** The hull, masts and bowsprit reach far past the entity's small box. */
	@Override
	protected AABB getBoundingBoxForCulling(PirateShip ship, float partialTick) {
		AABB local = rig().culling();
		if (local == null) {
			return ship.getBoundingBox().inflate(10.0, 0.0, 10.0).expandTowards(0.0, 16.0, 0.0).expandTowards(0.0, -3.0, 0.0);
		}
		// The rig's box is ship-local: enclose it at any yaw by its horizontal reach.
		double reach = Math.max(Math.max(Math.abs(local.minX), Math.abs(local.maxX)), Math.max(Math.abs(local.minZ), Math.abs(local.maxZ)));
		double reachXZ = Math.sqrt(2.0) * reach;
		return new AABB(ship.getX() - reachXZ, ship.getY() + local.minY, ship.getZ() - reachXZ, ship.getX() + reachXZ,
				ship.getY() + local.maxY, ship.getZ() + reachXZ);
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	@Override
	public void extractRenderState(PirateShip ship, State state, float partialTick) {
		super.extractRenderState(ship, state, partialTick);
		state.yRot = ship.getYRot(partialTick);
		state.rudder = ship.rudder();
		state.headway = ship.headway();
		state.wind = ship.windOffBow();
		state.phase = ship.swellPhase(partialTick);
		state.sails = ship.sails(partialTick);
		state.beaten = ship.beaten();
	}

	@Override
	public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
		Rig rig = rig();
		float age = state.ageInTicks;
		pose.pushPose();
		// The swell's heave, the yaw, then the roll (the swell's and the heel outward in a turn and to leeward of the wind; no
		// sinking list) and the pitch: exactly PirateMotion.lean's order, at the phase the server's seats use (game time and
		// the ship's own offset), so the crew stand on the deck drawn here however late this client began tracking it.
		float phase = state.phase;
		pose.translate(0.0F, (float) PirateMotion.heave(phase), 0.0F);
		pose.rotateDegrees(Axis.YP, -state.yRot);
		pose.rotateDegrees(Axis.ZP, PirateMotion.roll(phase, state.rudder, state.headway, state.wind));
		pose.rotateDegrees(Axis.XP, PirateMotion.pitch(phase));
		draw(HULL, pose, collector, state);
		float set = state.beaten ? 0.0F : Mth.clamp(state.sails, 0.0F, 1.0F);
		float sway = Mth.sin(age * 0.08F) * SWAY * (1.0F + 2.0F * state.headway) * set;
		float brace = PirateMotion.brace(state.wind);
		for (String sail : SQUARE) {
			Frame yard = sailFrame(rig, sail, brace);
			if (yard == null) {
				continue;
			}
			pose.pushPose();
			yard.apply(pose);
			draw("pirate_ship_yard_" + sail, pose, collector, state);
			if (set > SHOWN) {
				pose.pushPose();
				pose.rotateDegrees(Axis.XP, sway);
				pose.scale(1.0F, set, 1.0F);
				draw("pirate_ship_sail_" + sail, pose, collector, state);
				pose.popPose();
			}
			if (1.0F - set > SHOWN) {
				pose.pushPose();
				pose.scale(1.0F - set, 1.0F - set, 1.0F - set);
				draw("pirate_ship_furl_" + sail, pose, collector, state);
				pose.popPose();
			}
			pose.popPose();
		}
		float[] tip = rig.pivots().get(JIB);
		if (tip != null) {
			if (set > SHOWN) {
				drawScaled(JIB, tip, set, pose, collector, state);
			}
			if (1.0F - set > SHOWN) {
				drawScaled(JIB_FURL, tip, 1.0F - set, pose, collector, state);
			}
		}
		if (!state.beaten) {
			// A beaten ship's colours are struck: its flag is not drawn. It streams downwind (aft is -z; the wind off the bow
			// turns it by -wind) and flutters.
			drawAt(FLAG, rig, pose, collector, state, Axis.YP, -state.wind + Mth.sin(age * 0.2F) * FLUTTER);
		}
		drawAt(WHEEL, rig, pose, collector, state, Axis.ZP, state.rudder * WHEEL_TURN);
		// A positive rudder is a turn to starboard (yaw increasing); the blade's trailing edge swings to starboard (-x).
		drawAt(RUDDER, rig, pose, collector, state, Axis.YP, state.rudder * RUDDER_TURN);
		drawCannons(rig, pose, collector, state);
		drawRopes(rig, pose, collector, state, brace, sway, set);
		pose.popPose();
		super.submit(state, pose, collector, camera);
	}

	/** A gun at each port's trunnion, turned to its broadside (-facing about y). */
	private static void drawCannons(Rig rig, PoseStack pose, SubmitNodeCollector collector, State state) {
		QuadModel cannon = DecorQuads.get(CANNON);
		if (cannon == null) {
			return;
		}
		int gun = 0;
		for (CrewedVessel.Station station : PirateCrew.STATIONS) {
			if (station.role() != CrewedVessel.Role.GUNNER) {
				continue;
			}
			float[] at = gun < rig.cannons().length ? rig.cannons()[gun] : trunnion(station);
			gun++;
			pose.pushPose();
			pose.translate(at[0] / 16.0F, at[1] / 16.0F, at[2] / 16.0F);
			// The cannon model points to the bow (+z); a station's facing is a yaw relative to the ship's.
			pose.rotateDegrees(Axis.YP, -station.facing());
			cannon.submit(pose, collector, state.lightCoords);
			pose.popPose();
		}
	}

	/**
	 * The running rigging: for each rope, its two ends placed as their frames are this frame (a furled sail's sheet runs
	 * from under its yard), then 16-pixel lengths of rope laid from one to the other, turned by yaw then pitch, the last
	 * length scaled to what is left.
	 */
	private static void drawRopes(Rig rig, PoseStack pose, SubmitNodeCollector collector, State state, float brace, float sway, float set) {
		QuadModel rope = DecorQuads.get(ROPE);
		if (rope == null) {
			return;
		}
		for (End[] ends : rig.ropes()) {
			float[] a = place(rig, ends[0], brace, sway, set);
			float[] b = place(rig, ends[1], brace, sway, set);
			if (a == null || b == null) {
				continue;
			}
			float dx = b[0] - a[0], dy = b[1] - a[1], dz = b[2] - a[2];
			float length = Mth.sqrt(dx * dx + dy * dy + dz * dz);
			if (length < 1.0E-4F) {
				continue;
			}
			pose.pushPose();
			pose.translate(a[0] / 16.0F, a[1] / 16.0F, a[2] / 16.0F);
			pose.rotateDegrees(Axis.YP, (float) Math.toDegrees(Math.atan2(dx, dz)));
			pose.rotateDegrees(Axis.XP, (float) -Math.toDegrees(Math.asin(Mth.clamp(dy / length, -1.0F, 1.0F))));
			for (float done = 0.0F; done < length - 1.0E-4F; done += 16.0F) {
				pose.pushPose();
				pose.translate(0.0F, 0.0F, done / 16.0F);
				pose.scale(1.0F, 1.0F, Math.min(16.0F, length - done) / 16.0F);
				rope.submit(pose, collector, state.lightCoords);
				pose.popPose();
			}
			pose.popPose();
		}
	}

	/** A rope's end in ship pixels this frame, or null when its frame is not in the rig. */
	private static float[] place(Rig rig, End end, float brace, float sway, float set) {
		String frame = end.frame();
		if ("hull".equals(frame)) {
			return end.point().clone();
		}
		boolean canvas = frame.startsWith("sail_");
		String sail = frame.substring(frame.indexOf('_') + 1); // "sail_main_top" or "yard_main_top": "main_top"
		Frame yard = sailFrame(rig, sail, brace);
		if (yard == null) {
			return null;
		}
		float[] p = end.point();
		if (!canvas) {
			return yard.point(p[0], p[1], p[2]);
		}
		if (set <= SHOWN) {
			return yard.point(p[0], FURLED_CLEW_Y, p[2]);
		}
		// The canvas frame: the yard's, turned about x by the sway, scaled in y by the set.
		double s = Math.toRadians(sway);
		double y = p[1] * set;
		double z = p[2];
		return yard.point(p[0], (float) (y * Math.cos(s) - z * Math.sin(s)), (float) (y * Math.sin(s) + z * Math.cos(s)));
	}

	/**
	 * A square sail's yard frame: to its brace axis at the yard's height, braced round (within the sail's limit), then out
	 * to the yard's middle; or null when the rig lacks the sail.
	 */
	private static @Nullable Frame sailFrame(Rig rig, String sail, float brace) {
		float[] pivot = rig.pivots().get("pirate_ship_yard_" + sail);
		Float axis = rig.axes().get(sail);
		if (pivot == null || axis == null) {
			return null;
		}
		float limit = rig.limits().getOrDefault(sail, PirateMotion.BRACE_MAX);
		return new Frame(pivot, axis, Mth.clamp(brace, -limit, limit));
	}

	/** A yard's placement (pixels): translate (0, pivot y, axis), turn about y, translate (pivot x, 0, pivot z - axis). */
	record Frame(float[] pivot, float axis, float brace) {
		void apply(PoseStack pose) {
			pose.translate(0.0F, pivot[1] / 16.0F, axis / 16.0F);
			pose.rotateDegrees(Axis.YP, brace);
			pose.translate(pivot[0] / 16.0F, 0.0F, (pivot[2] - axis) / 16.0F);
		}

		/** A point of the yard's part (pixels about the pivot) in ship pixels, as {@link #apply} places it. */
		float[] point(float x, float y, float z) {
			double px = x + pivot[0], pz = z + pivot[2] - axis;
			double b = Math.toRadians(brace);
			// Axis.YP: x' = x cos + z sin, z' = z cos - x sin.
			return new float[] {(float) (px * Math.cos(b) + pz * Math.sin(b)), y + pivot[1], (float) (pz * Math.cos(b) - px * Math.sin(b) + axis)};
		}
	}

	/** Without a rig entry: the trunnion behind the station's muzzle by the barrel's length, along its facing. */
	private static float[] trunnion(CrewedVessel.Station station) {
		double turn = -station.facing() * Mth.DEG_TO_RAD;
		return new float[] {(float) (station.muzzle().x * 16.0 - Math.sin(turn) * BARREL), (float) (station.muzzle().y * 16.0),
				(float) (station.muzzle().z * 16.0 - Math.cos(turn) * BARREL)};
	}

	private static void draw(String name, PoseStack pose, SubmitNodeCollector collector, State state) {
		QuadModel model = DecorQuads.get(name);
		if (model != null) {
			model.submit(pose, collector, state.lightCoords);
		}
	}

	/** Draws a part exported about its pivot, scaled about it. */
	private static void drawScaled(String name, float[] pivot, float scale, PoseStack pose, SubmitNodeCollector collector, State state) {
		pose.pushPose();
		pose.translate(pivot[0] / 16.0F, pivot[1] / 16.0F, pivot[2] / 16.0F);
		pose.scale(scale, scale, scale);
		draw(name, pose, collector, state);
		pose.popPose();
	}

	/** Draws a part exported about its pivot: moved to the pivot, turned about {@code axis} by {@code degrees}. */
	private static void drawAt(String name, Rig rig, PoseStack pose, SubmitNodeCollector collector, State state, Axis axis,
			float degrees) {
		float[] pivot = rig.pivots().get(name);
		if (pivot == null) {
			return;
		}
		pose.pushPose();
		pose.translate(pivot[0] / 16.0F, pivot[1] / 16.0F, pivot[2] / 16.0F);
		pose.rotateDegrees(axis, degrees);
		draw(name, pose, collector, state);
		pose.popPose();
	}

	/** The rig, read from assets/jugcraft/pirate_ship_rig.json on first use after each resource reload ({@link #register}). */
	static Rig rig() {
		if (rig == null) {
			rig = load();
		}
		return rig;
	}

	private static Rig load() {
		Map<String, float[]> pivots = new HashMap<>();
		Map<String, Float> axes = new HashMap<>();
		Map<String, Float> limits = new HashMap<>();
		List<End[]> ropes = new ArrayList<>();
		float[][] cannons = new float[0][];
		AABB culling = null;
		Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(Jugcraft.id("pirate_ship_rig.json"));
		if (resource.isEmpty()) {
			Jugcraft.LOGGER.warn("Missing pirate_ship_rig.json: the pirate ship's moving parts are not drawn");
			return new Rig(pivots, axes, limits, ropes, cannons, null);
		}
		try (Reader reader = resource.get().openAsReader()) {
			JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
			if (root.has("pivots")) {
				for (Map.Entry<String, JsonElement> entry : root.getAsJsonObject("pivots").entrySet()) {
					pivots.put(entry.getKey(), floats(entry.getValue().getAsJsonArray(), 3));
				}
			}
			if (root.has("axes")) {
				for (Map.Entry<String, JsonElement> entry : root.getAsJsonObject("axes").entrySet()) {
					axes.put(entry.getKey(), entry.getValue().getAsFloat());
				}
			}
			if (root.has("brace_limits")) {
				for (Map.Entry<String, JsonElement> entry : root.getAsJsonObject("brace_limits").entrySet()) {
					limits.put(entry.getKey(), entry.getValue().getAsFloat());
				}
			}
			if (root.has("ropes")) {
				for (JsonElement element : root.getAsJsonArray("ropes")) {
					JsonObject r = element.getAsJsonObject();
					ropes.add(new End[] {end(r.getAsJsonObject("from")), end(r.getAsJsonObject("to"))});
				}
			}
			if (root.has("cannons")) {
				JsonArray list = root.getAsJsonArray("cannons");
				cannons = new float[list.size()][];
				for (int i = 0; i < list.size(); i++) {
					cannons[i] = floats(list.get(i).getAsJsonArray(), 3);
				}
			}
			if (root.has("culling")) {
				float[] box = floats(root.getAsJsonArray("culling"), 6);
				culling = new AABB(box[0], box[1], box[2], box[3], box[4], box[5]);
			}
		} catch (Exception e) {
			Jugcraft.LOGGER.warn("Could not read pirate_ship_rig.json; the pirate ship's moving parts may not be drawn", e);
		}
		return new Rig(pivots, axes, limits, ropes, cannons, culling);
	}

	private static End end(JsonObject json) {
		return new End(json.get("frame").getAsString(), floats(json.getAsJsonArray("point"), 3));
	}

	private static float[] floats(JsonArray array, int size) {
		if (array.size() != size) {
			throw new IllegalArgumentException("Expected " + size + " numbers, got " + array.size());
		}
		float[] out = new float[size];
		for (int i = 0; i < size; i++) {
			out[i] = array.get(i).getAsFloat();
		}
		return out;
	}
}

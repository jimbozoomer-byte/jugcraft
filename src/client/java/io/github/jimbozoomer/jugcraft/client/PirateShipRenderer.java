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
import io.github.jimbozoomer.jugcraft.menagerie.PirateShip;
import java.io.Reader;
import java.util.HashMap;
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
 * (assets/jugcraft/pirate_ship_quads.json, through {@link DecorQuads}) and the pivots in its rig
 * (assets/jugcraft/pirate_ship_rig.json): the hull turned to the ship's yaw and rocking on the swell, the sails and flag
 * swaying, the wheel and rudder turned by the helm, and a cannon at each gunner station turned to its broadside.
 *
 * <p>Frame: ship-local pixels (16 to a block), x to port, y up from the waterline, z to the bow, unturned; everything
 * is turned by the ship's yaw here, as {@link ZeppelinRenderer} does. A part missing from the export is not drawn.
 */
public class PirateShipRenderer extends EntityRenderer<PirateShip, PirateShipRenderer.State> {
	private static final String HULL = "pirate_ship_hull";
	private static final String[] SAILS = {"pirate_ship_sail_fore", "pirate_ship_sail_main", "pirate_ship_jib"};
	private static final String FLAG = "pirate_ship_flag";
	private static final String WHEEL = "pirate_ship_wheel";
	private static final String RUDDER = "pirate_ship_rudder";
	private static final String CANNON = "pirate_ship_cannon";
	/** Degrees the wheel turns at full helm, and the rudder. */
	private static final float WHEEL_TURN = 120.0F;
	private static final float RUDDER_TURN = 25.0F;
	/** Pivots used where the rig lacks a key (pixels): tools/pirate_ship.py's PIVOTS (ee2c719051). */
	private static final Map<String, float[]> DEFAULT_PIVOTS = Map.of(
			"pirate_ship_sail_fore", new float[] {0, 109.5F, 84.5F},
			"pirate_ship_sail_main", new float[] {0, 135.5F, 21},
			"pirate_ship_jib", new float[] {0, 50, 124},
			FLAG, new float[] {0, 180, 5},
			WHEEL, new float[] {0, 40, -83.2F},
			RUDDER, new float[] {0, 0, -117});
	/** How far the cannon's muzzle face lies forward of its trunnion (pixels; tools/pirate_ship.py). */
	private static final float BARREL = 8.2F;
	private static @Nullable Rig rig;

	public static final class State extends EntityRenderState {
		float yRot;
		float rudder;
		float headway;
		boolean beaten;
		boolean sunk;
	}

	/** The pivots and cannon trunnions read from the rig file, or the defaults. */
	record Rig(Map<String, float[]> pivots, float[][] cannons, @Nullable AABB culling) {
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
		state.beaten = ship.beaten();
		state.sunk = ship.sunk();
	}

	@Override
	public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
		Rig rig = rig();
		float age = state.ageInTicks;
		pose.pushPose();
		pose.rotateDegrees(Axis.YP, -state.yRot);
		// The swell: a slow roll and pitch; a sinking ship lists hard to port (+x: a negative turn about Z).
		pose.rotateDegrees(Axis.ZP, Mth.sin(age * 0.05F) * 1.2F - (state.sunk ? 14.0F : 0.0F));
		pose.rotateDegrees(Axis.XP, Mth.sin(age * 0.037F + 1.0F) * 0.6F);
		draw(HULL, pose, collector, state);
		float sway = Mth.sin(age * 0.08F) * (1.0F + 2.0F * state.headway);
		for (String sail : SAILS) {
			drawAt(sail, rig, pose, collector, state, Axis.XP, state.beaten ? 0.0F : sway);
		}
		if (!state.beaten) {
			// A beaten ship's colours are struck: its flag is not drawn.
			drawAt(FLAG, rig, pose, collector, state, Axis.YP, Mth.sin(age * 0.2F) * 8.0F);
		}
		drawAt(WHEEL, rig, pose, collector, state, Axis.ZP, state.rudder * WHEEL_TURN);
		// A positive rudder is a turn to starboard (yaw increasing); the blade's trailing edge swings to starboard (-x).
		drawAt(RUDDER, rig, pose, collector, state, Axis.YP, state.rudder * RUDDER_TURN);
		QuadModel cannon = DecorQuads.get(CANNON);
		if (cannon != null) {
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
		pose.popPose();
		super.submit(state, pose, collector, camera);
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

	/** Draws a part exported about its pivot: moved to the pivot, turned about {@code axis} by {@code degrees}. */
	private static void drawAt(String name, Rig rig, PoseStack pose, SubmitNodeCollector collector, State state, Axis axis,
			float degrees) {
		QuadModel model = DecorQuads.get(name);
		float[] pivot = rig.pivots().get(name);
		if (model == null || pivot == null) {
			return;
		}
		pose.pushPose();
		pose.translate(pivot[0] / 16.0F, pivot[1] / 16.0F, pivot[2] / 16.0F);
		pose.rotateDegrees(axis, degrees);
		model.submit(pose, collector, state.lightCoords);
		pose.popPose();
	}

	/**
	 * The rig, read from assets/jugcraft/pirate_ship_rig.json on first use after each resource reload ({@link #register});
	 * the defaults where the file or a key is missing.
	 */
	static Rig rig() {
		if (rig == null) {
			rig = load();
		}
		return rig;
	}

	private static Rig load() {
		Map<String, float[]> pivots = new HashMap<>(DEFAULT_PIVOTS);
		float[][] cannons = new float[0][];
		AABB culling = null;
		Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(Jugcraft.id("pirate_ship_rig.json"));
		if (resource.isEmpty()) {
			Jugcraft.LOGGER.warn("Missing pirate_ship_rig.json: the pirate ship's moving parts use default pivots");
			return new Rig(pivots, cannons, null);
		}
		try (Reader reader = resource.get().openAsReader()) {
			JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
			if (root.has("pivots")) {
				for (Map.Entry<String, JsonElement> entry : root.getAsJsonObject("pivots").entrySet()) {
					pivots.put(entry.getKey(), floats(entry.getValue().getAsJsonArray(), 3));
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
			Jugcraft.LOGGER.warn("Could not read pirate_ship_rig.json; using default pivots", e);
		}
		return new Rig(pivots, cannons, culling);
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

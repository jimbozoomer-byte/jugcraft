package io.github.jimbozoomer.jugcraft.client.arms;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.jimbozoomer.jugcraft.Jugcraft;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.WeakHashMap;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityRenderLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * The flail's swinging head (docs/features/arms-restyle.md, "The flail's head swings"): in the hand a flail's model is
 * its handle alone, and its chain and spiked ball are drawn here, link by link, where a little chain simulation puts
 * them each frame, so the ball hangs under gravity, trails the arm and whips round after a blow. Client only; no
 * networking (every client simulates the flails it sees from the swings it already gets).
 *
 * <p>The parts are the flail's own item models (tools/arms_heads.py): a render-only copy of the held stack carries the
 * custom_model_data string {@link #LINK} or {@link #BALL}, which its item definition answers with the link or the ball
 * box model, so they share the flail's texture atlas, its light and its enchantment glint. assets/jugcraft/arms_heads.json
 * gives, per item, where the chain hangs from the handle and the hand poses the handle is drawn with, so the chain is put
 * where the handle's eye is drawn.
 *
 * <p>Frames: in third person (mixin/client/ArmsItemInHandLayerMixin) the model root is kept as the arm starts
 * ({@link #root}), and the head is drawn where the held item is ({@link #submitThirdPerson}); the chain is simulated in
 * the body's upright root frame, with the body's turns and moves taken out of it as it steps, so the ball keeps its
 * swing as the body turns and the entity moves (and the camera, wherever it is, plays no part). On screen (mixin/client/ArmsFirstPersonMixin) it is simulated in the world's axes about the eye, from
 * the camera's pitch and yaw. Anything further than 32 blocks gets the ball hanging still, and a chain is never stepped
 * twice in a frame (the inventory's paper doll draws the chain as the world last left it, turned with the body).
 */
public final class FlailHeads {
	/** The custom_model_data strings that pick a flail's head parts (tools/arms_heads.py LINK_CASE and BALL_CASE). */
	public static final String LINK = "flail_link";
	public static final String BALL = "flail_ball";
	/** An armed entity's flail heads, on its render state for this frame. */
	public static final RenderStateDataKey<Heads> HEADS = RenderStateDataKey.create(() -> "jugcraft:flail_heads");
	/** The hand poses of arms_heads.json, by context: 0 and 1 third person (right, left), 2 and 3 first person. */
	private static final List<String> CONTEXTS = List.of("thirdperson_righthand", "thirdperson_lefthand", "firstperson_righthand",
			"firstperson_lefthand");

	/** Gravity, blocks a tick squared (a little brisker than a falling entity's feel, so a short chain swings lively). */
	static final float GRAVITY = 0.05F;
	/** Share of its speed a link keeps over a tick (air drag and the links' friction): a swing dies down in about 1.5 s. */
	static final float KEEP = 0.92F;
	/** The longest sub-step and the longest frame stepped, in ticks; longer gaps are clamped. */
	static final float SUBSTEP = 0.25F;
	static final float MAX_STEP = 2.0F;
	/** Constraint passes a sub-step. */
	static final int PASSES = 4;
	/** How far the ball moves when a link pulls on it, against the link's own share: the ball is heavy. */
	static final float BALL_SHARE = 0.25F;
	/** Unseen this many ticks, or moved this many blocks (squared) at once, the chain starts again hanging. */
	static final float RESET_TICKS = 20.0F;
	static final double JUMP_SQ = 4.0;
	/** Beyond this (squared, blocks) the ball just hangs. */
	static final double FAR_SQ = 32.0 * 32.0;
	/** The ball keeps this share of its reach clear of the haft. */
	static final float CLEAR = 0.75F;
	/** On screen, the ball's middle keeps this far (blocks) from the eye, so a swing back never fills the view. */
	static final float NEAR = 0.45F;
	private static final float DEG = (float) (Math.PI / 180.0);
	private static final Matrix4f IDENTITY = new Matrix4f();

	private static final Map<Item, Spec> SPECS = new HashMap<>();
	private static final Map<LivingEntity, Heads> ENTITIES = new WeakHashMap<>();
	private static ItemModelResolver resolver;
	private static boolean failed;

	private FlailHeads() {
	}

	// ---------------------------------------------------------------- data

	/** One flail item's head: lengths in sim units are worked out per hand pose (their display scale). */
	static final class Spec {
		int links;
		/** How far each link is turned about its length from the last (radians): a chain's links interlock a quarter
		 * turn apart, a spine's vertebrae all face one way. */
		float twist;
		/** Per hand pose: the eye and the grip in the item's base frame (where the handle is drawn from), blocks. */
		final Vector3f[] anchor = new Vector3f[4];
		final Vector3f[] grip = new Vector3f[4];
		/** Per hand pose: the display scale (the parts are drawn at it), and the link pitch, the lug, the ball's reach and
		 * the haft's radius at that scale, blocks. */
		final float[] scale = new float[4];
		final float[] pitch = new float[4];
		final float[] lug = new float[4];
		final float[] reach = new float[4];
		final float[] haft = new float[4];
	}

	/** Reads assets/jugcraft/arms_heads.json from the mod's resources (once, at client start). */
	public static void load() {
		String path = "/assets/" + Jugcraft.MOD_ID + "/arms_heads.json";
		try (InputStream in = FlailHeads.class.getResourceAsStream(path)) {
			if (in == null) {
				Jugcraft.LOGGER.warn("No flail heads ({}): flails show their handles only", path);
				return;
			}
			JsonObject json = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
			for (String id : json.keySet()) {
				Item item = BuiltInRegistries.ITEM.getValue(Identifier.parse(id));
				if (item == Items.AIR) {
					Jugcraft.LOGGER.warn("Flail heads: no item {}", id);
					continue;
				}
				SPECS.put(item, parse(json.getAsJsonObject(id)));
			}
		} catch (Exception exception) {
			Jugcraft.LOGGER.error("Could not read the flail heads", exception);
		}
		Jugcraft.LOGGER.info("Flail heads: {} items", SPECS.size());
	}

	/** Keeps the item model resolver of the entity renderers (the first person pass has none of its own to hand). */
	public static void register() {
		LivingEntityRenderLayerRegistrationCallback.EVENT.register((type, renderer, helper, context) -> resolver = context.getItemModelResolver());
	}

	private static Spec parse(JsonObject json) {
		Spec spec = new Spec();
		spec.links = json.get("links").getAsInt();
		spec.twist = json.get("twist").getAsFloat() * DEG;
		float[] anchor = floats(json.getAsJsonArray("anchor"));
		float[] grip = floats(json.getAsJsonArray("grip"));
		JsonObject display = json.getAsJsonObject("display");
		Matrix4f d = new Matrix4f();
		for (int c = 0; c < CONTEXTS.size(); c++) {
			JsonObject pose = display.getAsJsonObject(CONTEXTS.get(c));
			float s = display(d, floats(pose.getAsJsonArray("rotation")), floats(pose.getAsJsonArray("translation")),
					floats(pose.getAsJsonArray("scale"))[0], c % 2 == 1);
			spec.anchor[c] = d.transformPosition(new Vector3f(anchor[0] / 16.0F, anchor[1] / 16.0F, anchor[2] / 16.0F));
			spec.grip[c] = d.transformPosition(new Vector3f(grip[0] / 16.0F, grip[1] / 16.0F, grip[2] / 16.0F));
			spec.scale[c] = s;
			spec.pitch[c] = json.get("pitch").getAsFloat() * s / 16.0F;
			spec.lug[c] = json.get("lug").getAsFloat() * s / 16.0F;
			spec.reach[c] = json.get("reach").getAsFloat() * s / 16.0F;
			spec.haft[c] = json.get("haft").getAsFloat() * s / 16.0F;
		}
		return spec;
	}

	/**
	 * A hand pose as the game applies an item model's display transform, then the model's centring: move, turn about x,
	 * y then z, scale, and move the model's middle to the origin. For a left hand the game mirrors the pose it reads
	 * (the x move and the y and z turns change sign), as vanilla's ItemTransform does. Returns the scale.
	 */
	static float display(Matrix4f out, float[] rotation, float[] translation, float scale, boolean leftHand) {
		float flip = leftHand ? -1.0F : 1.0F;
		out.set(IDENTITY).translate(flip * translation[0] / 16.0F, translation[1] / 16.0F, translation[2] / 16.0F)
				.rotateX(rotation[0] * DEG).rotateY(flip * rotation[1] * DEG).rotateZ(flip * rotation[2] * DEG)
				.scale(scale).translate(-0.5F, -0.5F, -0.5F);
		return scale;
	}

	private static float[] floats(JsonArray array) {
		float[] out = new float[array.size()];
		for (int i = 0; i < out.length; i++) {
			out[i] = array.get(i).getAsFloat();
		}
		return out;
	}

	/** For the client game tests: how many flail items' heads were read. */
	public static int loadedItems() {
		return SPECS.size();
	}

	/** Whether an item's head is drawn here. */
	public static boolean swings(ItemStack stack) {
		return SPECS.containsKey(stack.getItem());
	}

	// ---------------------------------------------------------------- the chain

	/** A chain: points from the eye (0) through the links' joints to the ball's centre (last), in its sim frame. */
	static final class Chain {
		final float[] x = new float[(8 + 2) * 3];
		final float[] old = new float[(8 + 2) * 3];
		final float[] length = new float[8 + 1];
		int points;
		boolean ready;
		/** How far from its sim frame's origin the ball must stay (0: anywhere); the eye, on screen. */
		float near;
		Spec spec;
		int context = -1;
		float now;
		float step = SUBSTEP;
		double px;
		double py;
		double pz;
		float yaw;
		final Vector3f lastAnchor = new Vector3f();

		/** Lays the chain hanging straight down from the anchor (sim frame: y up). */
		void hang(Spec spec, int context, Vector3f anchor) {
			this.spec = spec;
			this.context = context;
			points = Math.min(spec.links, 8) + 2;
			for (int i = 0; i < points - 1; i++) {
				length[i] = i < points - 2 ? spec.pitch[context] : spec.lug[context];
			}
			float y = anchor.y;
			for (int i = 0; i < points; i++) {
				if (i > 0) {
					y -= length[i - 1];
				}
				x[i * 3] = anchor.x;
				x[i * 3 + 1] = y;
				x[i * 3 + 2] = anchor.z;
			}
			System.arraycopy(x, 0, old, 0, points * 3);
			lastAnchor.set(anchor);
			step = SUBSTEP;
			ready = true;
		}

		/**
		 * Steps the chain by dt ticks under gravity g (sim units a tick squared, down -y), its eye moving from where it was
		 * to `anchor` over the step, and keeps the ball clear of the haft (grip to anchor). Verlet with sub-steps of at most
		 * {@link #SUBSTEP}; the links only resist stretching; the ball is heavy.
		 */
		void step(float dt, float g, Vector3f anchor, Vector3f grip, float clear) {
			if (dt <= 0.0F) {
				// No time passes (a still chain): only settle it where it hangs.
				settle(anchor.x, anchor.y, anchor.z, grip, clear);
				lastAnchor.set(anchor);
				return;
			}
			int steps = Math.max(1, (int) Math.ceil(dt / SUBSTEP));
			float h = dt / steps;
			float keep = (float) Math.pow(KEEP, h);
			float carry = h / step;   // the last sub-step's motion rescaled to this one's length
			step = h;
			float ax0 = lastAnchor.x;
			float ay0 = lastAnchor.y;
			float az0 = lastAnchor.z;
			for (int k = 1; k <= steps; k++) {
				float f = k / (float) steps;
				for (int i = 1; i < points; i++) {
					int o = i * 3;
					for (int c = 0; c < 3; c++) {
						float v = (x[o + c] - old[o + c]) * carry * keep;
						old[o + c] = x[o + c];
						x[o + c] += v;
					}
					x[o + 1] -= g * h * h;
				}
				carry = 1.0F;
				settle(ax0 + (anchor.x - ax0) * f, ay0 + (anchor.y - ay0) * f, az0 + (anchor.z - az0) * f, grip, clear);
			}
			lastAnchor.set(anchor);
		}

		/** The constraint passes: the eye pinned at (ax, ay, az), each link pulled back to its length, the ball off the haft. */
		private void settle(float ax, float ay, float az, Vector3f grip, float clear) {
			for (int pass = 0; pass < PASSES; pass++) {
				x[0] = ax;
				x[1] = ay;
				x[2] = az;
				for (int i = 0; i < points - 1; i++) {
					pull(i, i + 1 == points - 1 ? BALL_SHARE : 0.5F);
				}
				clearHaft(grip, ax, ay, az, clear);
				clearOrigin();
			}
			// Then inextensible: each point drawn back to its link's length from the one before it (follow the leader), so
			// however fast the arm moves the links never part.
			for (int i = 0; i < points - 1; i++) {
				int a = i * 3;
				int b = a + 3;
				float dx = x[b] - x[a];
				float dy = x[b + 1] - x[a + 1];
				float dz = x[b + 2] - x[a + 2];
				float d = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
				if (d > length[i] && d > 1.0E-6F) {
					float k = length[i] / d;
					x[b] = x[a] + dx * k;
					x[b + 1] = x[a + 1] + dy * k;
					x[b + 2] = x[a + 2] + dz * k;
				}
			}
		}

		/** Pulls points i and i+1 back to at most length[i] apart, point i+1 moving `share` of the way (point i the rest). */
		private void pull(int i, float share) {
			int a = i * 3;
			int b = a + 3;
			float dx = x[b] - x[a];
			float dy = x[b + 1] - x[a + 1];
			float dz = x[b + 2] - x[a + 2];
			float d = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
			if (d <= length[i] || d < 1.0E-6F) {
				return;
			}
			float k = (d - length[i]) / d;
			float mine = i == 0 ? 0.0F : 1.0F - share;
			float theirs = i == 0 ? 1.0F : share;
			x[a] += dx * k * mine;
			x[a + 1] += dy * k * mine;
			x[a + 2] += dz * k * mine;
			x[b] -= dx * k * theirs;
			x[b + 1] -= dy * k * theirs;
			x[b + 2] -= dz * k * theirs;
		}

		/** Pushes the ball out of the haft's capsule (grip to the eye, `clear` round it). */
		private void clearHaft(Vector3f grip, float ax, float ay, float az, float clear) {
			int o = (points - 1) * 3;
			float sx = ax - grip.x;
			float sy = ay - grip.y;
			float sz = az - grip.z;
			float len2 = sx * sx + sy * sy + sz * sz;
			if (len2 < 1.0E-8F) {
				return;
			}
			float t = ((x[o] - grip.x) * sx + (x[o + 1] - grip.y) * sy + (x[o + 2] - grip.z) * sz) / len2;
			t = Math.max(0.0F, Math.min(1.0F, t));
			float cx = grip.x + sx * t;
			float cy = grip.y + sy * t;
			float cz = grip.z + sz * t;
			float dx = x[o] - cx;
			float dy = x[o + 1] - cy;
			float dz = x[o + 2] - cz;
			float d = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
			if (d >= clear || d < 1.0E-6F) {
				return;
			}
			float k = (clear - d) / d;
			x[o] += dx * k;
			x[o + 1] += dy * k;
			x[o + 2] += dz * k;
		}

		/** Keeps the ball `near` from the sim frame's origin (the eye, on screen). */
		private void clearOrigin() {
			if (near <= 0.0F) {
				return;
			}
			int o = (points - 1) * 3;
			float d = (float) Math.sqrt(x[o] * x[o] + x[o + 1] * x[o + 1] + x[o + 2] * x[o + 2]);
			if (d < near && d > 1.0E-6F) {
				float k = near / d;
				x[o] *= k;
				x[o + 1] *= k;
				x[o + 2] *= k;
			}
		}

		/** Turns every point by `radians` about the frame's vertical axis: the frame turned the other way. */
		void turn(float radians) {
			if (radians == 0.0F) {
				return;
			}
			float c = (float) Math.cos(radians);
			float s = (float) Math.sin(radians);
			for (int i = 0; i < points; i++) {
				turn(x, i * 3, c, s);
				turn(old, i * 3, c, s);
			}
			float lx = lastAnchor.x;
			lastAnchor.x = lx * c + lastAnchor.z * s;
			lastAnchor.z = -lx * s + lastAnchor.z * c;
		}

		private static void turn(float[] p, int o, float c, float s) {
			float px = p[o];
			p[o] = px * c + p[o + 2] * s;
			p[o + 2] = -px * s + p[o + 2] * c;
		}

		/** Moves every point by (dx, dy, dz): the frame's origin moved the other way. */
		void shift(float dx, float dy, float dz) {
			for (int i = 0; i < points; i++) {
				int o = i * 3;
				x[o] += dx;
				x[o + 1] += dy;
				x[o + 2] += dz;
				old[o] += dx;
				old[o + 1] += dy;
				old[o + 2] += dz;
			}
			lastAnchor.add(dx, dy, dz);
		}

		boolean finite() {
			for (int i = 0; i < points * 3; i++) {
				if (!Float.isFinite(x[i])) {
					return false;
				}
			}
			return true;
		}
	}

	/**
	 * An entity's flail heads: its render copies, resolved part states and chains, per arm (0 right, 1 left); and this
	 * frame's model root (kept as the arm starts), the body's yaw as the entity was read, whether the body is tilted
	 * (swimming, gliding, lying, spinning, dying: the ball then hangs towards the feet, unstepped), and whether this is
	 * another view of the entity than the world's (the inventory's paper doll, read a whole tick ahead: it draws the chain
	 * as the world left it, on the doll's body, and never steps it).
	 */
	public static final class Heads {
		final Spec[] spec = new Spec[2];
		final ItemStack[] source = new ItemStack[2];
		final ItemStack[] linkCopy = new ItemStack[2];
		final ItemStack[] ballCopy = new ItemStack[2];
		final ItemStackRenderState[] link = {new ItemStackRenderState(), new ItemStackRenderState()};
		final ItemStackRenderState[] ball = {new ItemStackRenderState(), new ItemStackRenderState()};
		final Chain[] chain = {new Chain(), new Chain()};
		final Chain[] still = {new Chain(), new Chain()};
		final Matrix4f root = new Matrix4f();
		float yaw;
		boolean tilted;
		boolean other;
	}

	/** A render copy of a held flail asking for one of its head parts (keeps its enchantments, so the glint shows). */
	private static ItemStack part(ItemStack stack, String which) {
		ItemStack copy = stack.copy();
		copy.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(List.of(), List.of(), List.of(which), List.of()));
		return copy;
	}

	/** Brings an arm's render copies and resolved part states up to date with what it holds. */
	private static void resolve(Heads heads, int arm, ItemStack stack, ItemModelResolver models, LivingEntity entity) {
		if (heads.source[arm] != stack) {
			heads.source[arm] = stack;
			heads.linkCopy[arm] = part(stack, LINK);
			heads.ballCopy[arm] = part(stack, BALL);
		}
		models.updateForTopItem(heads.link[arm], heads.linkCopy[arm], ItemDisplayContext.FIXED, entity.level(), null, entity.getId());
		models.updateForTopItem(heads.ball[arm], heads.ballCopy[arm], ItemDisplayContext.FIXED, entity.level(), null, entity.getId());
	}

	// ---------------------------------------------------------------- third person

	/**
	 * As an armed entity's render state is filled (mixin/client/ArmsRenderStateMixin, after the arms motion): if either
	 * hand holds a flail whose head swings, its head parts are resolved for this frame and the heads put on the state.
	 */
	public static void extract(LivingEntity entity, ArmedEntityRenderState state, ItemModelResolver models, float partialTick) {
		resolver = models;
		ItemStack right = entity.getMainArm() == HumanoidArm.RIGHT ? entity.getMainHandItem() : entity.getOffhandItem();
		ItemStack left = entity.getMainArm() == HumanoidArm.RIGHT ? entity.getOffhandItem() : entity.getMainHandItem();
		Spec specRight = SPECS.get(right.getItem());
		Spec specLeft = SPECS.get(left.getItem());
		if (failed || specRight == null && specLeft == null) {
			if (state.getData(HEADS) != null) {
				state.setData(HEADS, null);
			}
			return;
		}
		Heads heads = ENTITIES.computeIfAbsent(entity, key -> new Heads());
		heads.spec[0] = specRight;
		heads.spec[1] = specLeft;
		if (specRight != null) {
			resolve(heads, 0, right, models, entity);
		}
		if (specLeft != null) {
			resolve(heads, 1, left, models, entity);
		}
		heads.yaw = state.bodyRot;
		heads.other = partialTick >= 1.0F;
		heads.tilted = entity.isVisuallySwimming() || entity.isFallFlying() || entity.isSleeping() || entity.isAutoSpinAttack()
				|| !entity.isAlive();
		state.setData(HEADS, heads);
	}

	/** As the item in hand starts to be drawn (mixin/client/ArmsItemInHandLayerMixin, at its head): the model's root. */
	public static void root(ArmedEntityRenderState state, PoseStack poseStack) {
		Heads heads = state.getData(HEADS);
		if (heads != null) {
			heads.root.set(poseStack.last().pose());
		}
	}

	// Scratch space for the render thread.
	private static final Matrix4f K = new Matrix4f();
	private static final Matrix4f TO_SIM = new Matrix4f();
	private static final Matrix4f FROM_SIM = new Matrix4f();
	private static final Matrix4f PART = new Matrix4f();
	private static final Vector3f ANCHOR = new Vector3f();
	private static final Vector3f GRIP = new Vector3f();
	private static final Vector3f V = new Vector3f();

	/**
	 * Where the held item is drawn in third person (mixin/client/ArmsItemInHandLayerMixin, after the wrist turn): the
	 * chain is stepped (once a frame, in the world's view) and its links and ball drawn from the item's frame.
	 */
	public static void submitThirdPerson(ArmedEntityRenderState state, ItemStack stack, HumanoidArm arm, PoseStack poseStack,
			SubmitNodeCollector collector, int light) {
		Heads heads = state.getData(HEADS);
		int side = arm == HumanoidArm.RIGHT ? 0 : 1;
		if (heads == null || heads.spec[side] == null || !SPECS.containsKey(stack.getItem())) {
			return;
		}
		try {
			Spec spec = heads.spec[side];
			int context = side;
			// Item base frame -> the model root -> the root upright (the model's flip undone: y up); the body's turns are
			// taken out of the chain as it steps (advance).
			Matrix4f base = poseStack.last().pose();
			K.set(heads.root).invert().mul(base, K);
			TO_SIM.set(IDENTITY).rotateZ((float) Math.PI).mul(K, TO_SIM);
			FROM_SIM.set(TO_SIM).invert();
			TO_SIM.transformPosition(ANCHOR.set(spec.anchor[context]));
			TO_SIM.transformPosition(GRIP.set(spec.grip[context]));
			// The root's own scale (a player is drawn at 15/16, a baby at half): gravity and moves in its units.
			float rootScale = (float) Math.sqrt(heads.root.transformDirection(V.set(1.0F, 0.0F, 0.0F)).lengthSquared());
			rootScale = rootScale > 1.0E-4F ? rootScale : 1.0F;
			boolean still = heads.tilted || state.distanceToCameraSq > FAR_SQ;
			Chain chain = still ? heads.still[side] : heads.chain[side];
			float clear = spec.haft[context] + spec.reach[context] * CLEAR;
			if (still) {
				chain.hang(spec, context, ANCHOR);
				chain.step(0.0F, 0.0F, ANCHOR, GRIP, clear);
			} else if (heads.other) {
				// Another view (the doll): the chain as the world left it, unless there is none fit to show.
				if (!chain.ready || chain.spec != spec || chain.context != context || state.ageInTicks - chain.now > RESET_TICKS) {
					chain.hang(spec, context, ANCHOR);
					chain.step(0.0F, 0.0F, ANCHOR, GRIP, clear);
				}
			} else {
				// Stepped once a frame: a second drawing in the same frame finds the chain already at `now`.
				advance(chain, spec, context, state.ageInTicks, state.x, state.y, state.z, heads.yaw, GRAVITY / rootScale, 1.0F / rootScale,
						clear);
			}
			draw(chain, heads.link[side], heads.ball[side], spec.scale[context], poseStack, collector, light);
		} catch (RuntimeException exception) {
			failed = true;
			Jugcraft.LOGGER.error("Flail heads failed; flails show their handles only from now on", exception);
		}
	}

	/**
	 * Steps a chain to `now` (ticks), or starts it hanging. Its sim frame's origin is at (x, y, z) blocks; in third person
	 * the frame is the body's (upright, turned with it by `yaw` degrees, units of the root), on screen the world's axes
	 * about the eye (yaw NaN). What the frame did since the last step is taken out of the chain, so the chain keeps its
	 * place in the world: the move, and a body's turn. A turn of more than a right angle at once is a different view of
	 * the same body (the inventory's doll faces the screen), not a turn: the chain then keeps its place on the body.
	 */
	private static void advance(Chain chain, Spec spec, int context, float now, double x, double y, double z, float yaw, float g,
			float units, float clear) {
		boolean fresh = !chain.ready || chain.spec != spec || chain.context != context || !chain.finite();
		double mx = x - chain.px;
		double my = y - chain.py;
		double mz = z - chain.pz;
		if (!fresh && (now - chain.now > RESET_TICKS || now < chain.now - 1.0F || mx * mx + my * my + mz * mz > JUMP_SQ)) {
			fresh = true;
		}
		if (fresh) {
			chain.hang(spec, context, ANCHOR);
		} else if (now > chain.now) {
			float turn = Float.isNaN(yaw) ? 0.0F : wrapDegrees(yaw - chain.yaw);
			if (Math.abs(turn) < 90.0F) {
				chain.turn(turn * DEG);
			}
			if (!Float.isNaN(yaw)) {
				// The world's move into the body's frame: x and z turned by the body's yaw (the root faces the body's front).
				float c = (float) Math.cos((yaw - 180.0F) * DEG);
				float s = (float) Math.sin((yaw - 180.0F) * DEG);
				double wx = mx;
				mx = wx * c + mz * s;
				mz = -wx * s + mz * c;
			}
			chain.shift((float) -mx * units, (float) -my * units, (float) -mz * units);
			chain.step(Math.min(MAX_STEP, now - chain.now), g, ANCHOR, GRIP, clear);
			if (!chain.finite()) {
				chain.hang(spec, context, ANCHOR);
			}
		}
		if (fresh || now > chain.now) {
			chain.now = now;
			chain.px = x;
			chain.py = y;
			chain.pz = z;
			chain.yaw = yaw;
		}
	}

	private static float wrapDegrees(float degrees) {
		float d = degrees % 360.0F;
		if (d >= 180.0F) {
			d -= 360.0F;
		}
		if (d < -180.0F) {
			d += 360.0F;
		}
		return d;
	}

	/**
	 * Draws a chain's links and ball from the pose stack's frame: each link midway between its joints, along them, each
	 * turned by the head's twist from the last (a quarter, so a chain's links interlock); the ball at the last point, its
	 * lug up the chain and its face (a skull's) the way the haft points.
	 */
	private static void draw(Chain chain, ItemStackRenderState link, ItemStackRenderState ball, float scale, PoseStack poseStack,
			SubmitNodeCollector collector, int light) {
		// The eye is where the handle is drawn now, whatever the step left.
		chain.x[0] = ANCHOR.x;
		chain.x[1] = ANCHOR.y;
		chain.x[2] = ANCHOR.z;
		float roll = (float) Math.atan2(ANCHOR.x - GRIP.x, ANCHOR.z - GRIP.z);
		for (int i = 0; i < chain.points - 1; i++) {
			boolean last = i == chain.points - 2;
			int a = i * 3;
			int b = a + 3;
			float ux = chain.x[a] - chain.x[b];
			float uy = chain.x[a + 1] - chain.x[b + 1];
			float uz = chain.x[a + 2] - chain.x[b + 2];
			float n = (float) Math.sqrt(ux * ux + uy * uy + uz * uz);
			if (n < 1.0E-6F) {
				ux = 0.0F;
				uy = 1.0F;
				uz = 0.0F;
				n = 1.0F;
			}
			ux /= n;
			uy /= n;
			uz /= n;
			// The part's +y (its length, the ball's lug) along u, by the shortest turn from straight up, then rolled.
			float tilt = (float) Math.acos(Math.max(-1.0F, Math.min(1.0F, uy)));
			float heading = (float) Math.atan2(ux, uz);
			float twist = roll + (last ? 0.0F : i * chain.spec.twist);
			float px = last ? chain.x[b] : (chain.x[a] + chain.x[b]) * 0.5F;
			float py = last ? chain.x[b + 1] : (chain.x[a + 1] + chain.x[b + 1]) * 0.5F;
			float pz = last ? chain.x[b + 2] : (chain.x[a + 2] + chain.x[b + 2]) * 0.5F;
			PART.set(FROM_SIM).translate(px, py, pz).rotateY(heading).rotateX(tilt).rotateY(twist - heading).scale(scale);
			ItemStackRenderState part = last ? ball : link;
			if (part.isEmpty()) {
				continue;
			}
			poseStack.pushPose();
			poseStack.mulPose(PART);
			part.submit(poseStack, collector, light, OverlayTexture.NO_OVERLAY, 0);
			poseStack.popPose();
		}
	}

	// ---------------------------------------------------------------- first person

	private static final Chain[] SCREEN = {screenChain(), screenChain()};
	private static final ItemStackRenderState[] SCREEN_LINK = {new ItemStackRenderState(), new ItemStackRenderState()};
	private static final ItemStackRenderState[] SCREEN_BALL = {new ItemStackRenderState(), new ItemStackRenderState()};
	private static final ItemStack[] SCREEN_SOURCE = new ItemStack[2];
	private static final ItemStack[] SCREEN_LINK_COPY = new ItemStack[2];
	private static final ItemStack[] SCREEN_BALL_COPY = new ItemStack[2];
	private static final Matrix4f SCREEN_BASE = new Matrix4f();
	private static final Matrix4f SCREEN_ITEM = new Matrix4f();
	private static Spec screenSpec;
	private static ItemStack screenStack = ItemStack.EMPTY;
	private static int screenSide = -1;
	private static SubmitNodeCollector screenCollector;
	private static int screenLight;
	private static float screenPartial;

	private static Chain screenChain() {
		Chain chain = new Chain();
		chain.near = NEAR;
		return chain;
	}

	/** Before a hand is drawn on screen (mixin/client/ArmsFirstPersonMixin, at its head): keeps the view's frame. */
	public static void beginFirstPerson(ItemStack stack, PoseStack poseStack, SubmitNodeCollector collector, int light,
			float partialTick) {
		screenSide = -1;
		screenSpec = failed ? null : SPECS.get(stack.getItem());
		if (screenSpec == null || resolver == null) {
			screenSpec = null;
			return;
		}
		screenStack = stack;
		screenCollector = collector;
		screenLight = light;
		screenPartial = partialTick;
		SCREEN_BASE.set(poseStack.last().pose());
	}

	/** Where the hand has been placed on screen (after its pose, and again after its swing): the item's frame. */
	public static void firstPersonFrame(PoseStack poseStack, HumanoidArm arm) {
		if (screenSpec == null) {
			return;
		}
		screenSide = arm == HumanoidArm.RIGHT ? 0 : 1;
		SCREEN_ITEM.set(poseStack.last().pose());
	}

	/** After the hand is drawn (at the method's return, the pose stack back where it began): the head, on screen. */
	public static void endFirstPerson(PoseStack poseStack) {
		Spec spec = screenSpec;
		int side = screenSide;
		screenSpec = null;
		screenSide = -1;
		Player player = Minecraft.getInstance().player;
		if (spec == null || side < 0 || player == null) {
			return;
		}
		try {
			int context = 2 + side;
			if (SCREEN_SOURCE[side] != screenStack) {
				SCREEN_SOURCE[side] = screenStack;
				SCREEN_LINK_COPY[side] = part(screenStack, LINK);
				SCREEN_BALL_COPY[side] = part(screenStack, BALL);
			}
			resolver.updateForTopItem(SCREEN_LINK[side], SCREEN_LINK_COPY[side], ItemDisplayContext.FIXED, player.level(), null, player.getId());
			resolver.updateForTopItem(SCREEN_BALL[side], SCREEN_BALL_COPY[side], ItemDisplayContext.FIXED, player.level(), null, player.getId());
			// Item frame -> the view as the hand pass began -> the world's axes about the eye (the camera's pitch and yaw undone).
			float pitch = player.getViewXRot(screenPartial);
			float yaw = player.getYRot(screenPartial);
			K.set(SCREEN_BASE).invert().mul(SCREEN_ITEM, K);
			TO_SIM.set(IDENTITY).rotateY((180.0F - yaw) * DEG).rotateX(-pitch * DEG).mul(K, TO_SIM);
			TO_SIM.transformPosition(ANCHOR.set(spec.anchor[context]));
			TO_SIM.transformPosition(GRIP.set(spec.grip[context]));
			// Drawn from the pose stack as it is now: back to the item's frame, then out to the sim frame.
			FROM_SIM.set(poseStack.last().pose()).invert().mul(SCREEN_ITEM, FROM_SIM);
			PART.set(TO_SIM).invert();
			FROM_SIM.mul(PART, FROM_SIM);
			float clear = spec.haft[context] + spec.reach[context] * CLEAR;
			Vec3 eye = player.getEyePosition(screenPartial);
			advance(SCREEN[side], spec, context, player.tickCount + screenPartial, eye.x, eye.y, eye.z, Float.NaN, GRAVITY, 1.0F, clear);
			draw(SCREEN[side], SCREEN_LINK[side], SCREEN_BALL[side], spec.scale[context], poseStack, screenCollector, screenLight);
		} catch (RuntimeException exception) {
			failed = true;
			Jugcraft.LOGGER.error("Flail heads failed on screen; flails show their handles only from now on", exception);
		}
	}

	// ---------------------------------------------------------------- tests

	/**
	 * For the client game tests: an entity's flail chain in its main arm (third person, the world's view), as the ball's
	 * offset from the eye (sim frame: y up, units of the body's root) and the chain's full length; or "no chain".
	 */
	public static String describe(LivingEntity entity) {
		Heads heads = ENTITIES.get(entity);
		int side = entity.getMainArm() == HumanoidArm.RIGHT ? 0 : 1;
		return heads == null ? "no chain" : describe(heads.chain[side]);
	}

	/** For the client game tests: the on-screen chain of a hand (0 right, 1 left), as {@link #describe(LivingEntity)}. */
	public static String describeFirstPerson(int side) {
		return describe(SCREEN[side]);
	}

	private static String describe(Chain chain) {
		if (!chain.ready) {
			return "no chain";
		}
		int o = (chain.points - 1) * 3;
		float total = 0.0F;
		for (int i = 0; i < chain.points - 1; i++) {
			total += chain.length[i];
		}
		return String.format(Locale.ROOT, "ball %.4f %.4f %.4f length %.4f finite %s", chain.x[o] - chain.x[0],
				chain.x[o + 1] - chain.x[1], chain.x[o + 2] - chain.x[2], total, chain.finite());
	}

	/** For the client game tests: the ball's height below the eye over the chain's length (1 hanging straight down), or NaN. */
	public static float hang(LivingEntity entity) {
		Heads heads = ENTITIES.get(entity);
		int side = entity.getMainArm() == HumanoidArm.RIGHT ? 0 : 1;
		return heads == null ? Float.NaN : hang(heads.chain[side]);
	}

	/** For the client game tests: as {@link #hang(LivingEntity)}, for the on-screen chain of a hand. */
	public static float hangFirstPerson(int side) {
		return hang(SCREEN[side]);
	}

	private static float hang(Chain chain) {
		if (!chain.ready) {
			return Float.NaN;
		}
		float total = 0.0F;
		for (int i = 0; i < chain.points - 1; i++) {
			total += chain.length[i];
		}
		return (chain.x[1] - chain.x[(chain.points - 1) * 3 + 1]) / total;
	}

	/** For the client game tests: whether no point of the chain is further from the eye than the chain is long. */
	public static boolean withinReach(LivingEntity entity) {
		Heads heads = ENTITIES.get(entity);
		int side = entity.getMainArm() == HumanoidArm.RIGHT ? 0 : 1;
		return heads != null && withinReach(heads.chain[side]);
	}

	private static boolean withinReach(Chain chain) {
		float total = 0.0F;
		for (int i = 0; i < chain.points - 1; i++) {
			total += chain.length[i];
		}
		for (int i = 1; i < chain.points; i++) {
			float dx = chain.x[i * 3] - chain.x[0];
			float dy = chain.x[i * 3 + 1] - chain.x[1];
			float dz = chain.x[i * 3 + 2] - chain.x[2];
			if (dx * dx + dy * dy + dz * dz > (total * 1.02F) * (total * 1.02F)) {
				return false;
			}
		}
		return chain.finite();
	}
}

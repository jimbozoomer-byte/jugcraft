package io.github.jimbozoomer.jugcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.artillery.JugcraftArtillery;
import io.github.jimbozoomer.jugcraft.walker.ArmouredWalker;
import io.github.jimbozoomer.jugcraft.walker.DieselWalker;
import io.github.jimbozoomer.jugcraft.walker.HowitzerWalker;
import io.github.jimbozoomer.jugcraft.walker.WalkerAim;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * The walker pilot's cockpit (docs/features/walker-cockpit.md), on the pilot's own client only:
 * <ul>
 * <li><b>Telegraphs</b>, drawn in the world: the shell's arc and where it will burst, with the burst's reach (the
 * Armoured Walker's cannon, from where the pilot looks; the Howitzer Walker's gun, from where its barrel points now,
 * with a marker where the pilot looks so they can watch the gun lay onto it); the reach of the fist, ram or stomp, with
 * every creature it would hit outlined; and the block the Diesel Walker's drill would cut. Each uses the server's own
 * test or aim ({@link WalkerAim}, {@link DieselWalker#attackReaches}), worked out once a tick.</li>
 * <li><b>Gauges</b> above the hotbar: fuel, the hull's wear, the weapon's reload and shells, and whether the fist, ram or
 * stomp is ready.</li>
 * </ul>
 * Presentation only: nothing here decides anything.
 */
public final class WalkerCockpitClient {
	private static final RenderType SEE_THROUGH = RenderTypes.textSeeThrough(Jugcraft.id("textures/misc/blueprint_ghost.png"));
	private static final int READY = 0xC060F070;
	private static final int LOADING = 0xC0FFB030;
	private static final int BLOCKED = 0xC0FF4030;
	private static final int REACH = 0x70FFFFFF;
	private static final int REACH_COOLING = 0x30FFFFFF;
	private static final int STRUCK = 0xD0FF4030;
	private static final int TARGET = 0xD0FFFFFF;
	private static final int DRILL = 0xC0FFB030;
	private static final int TEXT = 0xFFE8DCC0;
	private static final int TEXT_DIM = 0xFF9A9080;
	private static final int TEXT_GOOD = 0xFF7CE07C;
	private static final int TEXT_WARN = 0xFFFFB030;
	private static final int TEXT_BAD = 0xFFFF5040;

	/** What the cockpit shows this tick, or null when the player is not piloting a walker. */
	private static @Nullable View view;

	/** The pilot's view this tick. */
	record View(DieselWalker walker, List<Vec3> arc, @Nullable Vec3 impact, @Nullable Vec3 target, int arcColour, AABB reach,
			boolean attackReady, List<AABB> struck, @Nullable BlockPos drill, String weapon, int weaponColour) {
	}

	private WalkerCockpitClient() {
	}

	static void register() {
		ClientTickEvents.END_CLIENT_TICK.register(WalkerCockpitClient::tick);
		LevelRenderEvents.COLLECT_SUBMITS.register(context -> {
			View shown = view;
			if (shown == null) {
				return;
			}
			Vec3 camera = context.levelState().cameraRenderState.pos;
			PoseStack pose = context.poseStack();
			pose.pushPose();
			pose.translate(-camera.x, -camera.y, -camera.z);
			context.submitNodeCollector().submitCustomGeometry(pose, SEE_THROUGH, (p, consumer) -> draw(shown, p, consumer));
			pose.popPose();
		});
		HudElementRegistry.attachElementAfter(VanillaHudElements.HOTBAR, Jugcraft.id("walker_gauges"), WalkerCockpitClient::gauges);
	}

	/** The walker the local player pilots, or null. */
	static @Nullable DieselWalker piloted(Minecraft client) {
		LocalPlayer player = client.player;
		return player != null && player.getVehicle() instanceof DieselWalker walker && walker.getFirstPassenger() == player ? walker : null;
	}

	private static void tick(Minecraft client) {
		DieselWalker walker = piloted(client);
		LocalPlayer pilot = client.player;
		Level level = client.level;
		if (walker == null || pilot == null || level == null) {
			view = null;
			return;
		}
		Vec3 heading = walker.heading();
		AABB reach = walker.attackArea(heading);
		List<AABB> struck = new ArrayList<>();
		for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, reach.inflate(1.0),
				e -> e != pilot && e.isAlive() && e.getRootVehicle() != walker && walker.attackReaches(e))) {
			struck.add(target.getBoundingBox());
		}
		boolean ready = walker.attackReady();
		List<Vec3> arc = List.of();
		Vec3 impact = null;
		Vec3 target = null;
		BlockPos drill = null;
		int colour = READY;
		String weapon;
		int weaponColour = TEXT_GOOD;
		int shells = pilot.getInventory().countItem(JugcraftArtillery.HEAVY_SHELL_ITEM);
		boolean free = pilot.getAbilities().instabuild;
		if (walker instanceof HowitzerWalker howitzer) {
			WalkerAim.Flight flight = WalkerAim.flight(level, howitzer.muzzle(1.0F),
					howitzer.barrel(1.0F).scale(JugcraftArtillery.HEAVY_SPEED), JugcraftArtillery.HEAVY_GRAVITY, pilot);
			arc = flight.points();
			impact = flight.impact();
			target = WalkerAim.lookedAt(level, pilot, HowitzerWalker.AIM_REACH);
			float reload = Math.min(1.0F, howitzer.sinceShot(1.0F) / HowitzerWalker.GUN_COOLDOWN);
			boolean onTarget = impact != null && target != null && impact.distanceTo(target) <= 2.5;
			if (!free && shells == 0) {
				colour = BLOCKED;
				weapon = Component.translatable("hud.jugcraft.walker.no_shells").getString();
				weaponColour = TEXT_BAD;
			} else if (reload < 1.0F) {
				colour = LOADING;
				weapon = Component.translatable("hud.jugcraft.walker.reloading", percent(reload)).getString();
				weaponColour = TEXT_WARN;
			} else if (target != null && !onTarget) {
				colour = LOADING;
				weapon = Component.translatable("hud.jugcraft.walker.laying").getString();
				weaponColour = TEXT_WARN;
			} else {
				weapon = Component.translatable("hud.jugcraft.walker.on_target").getString();
			}
			if (!free) {
				weapon += "  " + Component.translatable("hud.jugcraft.walker.shells", shells).getString();
			}
		} else if (walker instanceof ArmouredWalker armoured) {
			Vec3 muzzle = armoured.muzzle(1.0F);
			WalkerAim.Flight flight = WalkerAim.flight(level, muzzle, ArmouredWalker.cannonVelocity(level, pilot, muzzle),
					JugcraftArtillery.HEAVY_GRAVITY, pilot);
			arc = flight.points();
			impact = flight.impact();
			float reload = Math.min(1.0F, armoured.sinceShot() / (float) ArmouredWalker.CANNON_COOLDOWN);
			if (!free && shells == 0) {
				colour = BLOCKED;
				weapon = Component.translatable("hud.jugcraft.walker.no_shells").getString();
				weaponColour = TEXT_BAD;
			} else if (reload < 1.0F) {
				colour = LOADING;
				weapon = Component.translatable("hud.jugcraft.walker.reloading", percent(reload)).getString();
				weaponColour = TEXT_WARN;
			} else {
				weapon = Component.translatable("hud.jugcraft.walker.cannon_ready").getString();
			}
			if (!free) {
				weapon += "  " + Component.translatable("hud.jugcraft.walker.shells", shells).getString();
			}
		} else {
			HitResult hit = pilot.pick(DieselWalker.DRILL_REACH, 1.0F, false);
			if (hit instanceof BlockHitResult block && hit.getType() == HitResult.Type.BLOCK) {
				var state = level.getBlockState(block.getBlockPos());
				float hardness = state.getDestroySpeed(level, block.getBlockPos());
				if (!state.isAir() && hardness >= 0 && hardness <= DieselWalker.DRILL_MAX_HARDNESS) {
					drill = block.getBlockPos();
				}
			}
			weapon = Component.translatable(drill != null ? "hud.jugcraft.walker.drill_ready" : "hud.jugcraft.walker.drill_none").getString();
			weaponColour = drill != null ? TEXT_GOOD : TEXT_DIM;
		}
		if (walker.fuelGauge() <= 0.0F) {
			colour = BLOCKED;
		}
		view = new View(walker, arc, impact, target, colour, reach, ready, struck, drill, weapon, weaponColour);
	}

	private static String percent(float share) {
		return Math.round(share * 100) + "%";
	}

	// ---------------------------------------------------------------- the telegraphs

	private static void draw(View view, PoseStack.Pose pose, VertexConsumer consumer) {
		// The arc: a bead every other tick of the flight.
		for (int i = 1; i < view.arc().size() - 1; i += 2) {
			Vec3 at = view.arc().get(i);
			bead(pose, consumer, at, 0.07F, view.arcColour());
		}
		if (view.impact() != null) {
			ring(pose, consumer, view.impact(), JugcraftArtillery.HEAVY_RADIUS, 40, 0.06F, view.arcColour());
			ring(pose, consumer, view.impact(), 0.6, 12, 0.08F, view.arcColour());
		}
		if (view.target() != null) {
			Vec3 t = view.target();
			LinePreview.cube(pose, consumer, (float) t.x - 0.45F, (float) t.y + 0.02F, (float) t.z - 0.03F,
					(float) t.x + 0.45F, (float) t.y + 0.08F, (float) t.z + 0.03F, TARGET);
			LinePreview.cube(pose, consumer, (float) t.x - 0.03F, (float) t.y + 0.02F, (float) t.z - 0.45F,
					(float) t.x + 0.03F, (float) t.y + 0.08F, (float) t.z + 0.45F, TARGET);
		}
		int reach = view.attackReady() ? REACH : REACH_COOLING;
		if (view.walker() instanceof HowitzerWalker) {
			ring(pose, consumer, view.walker().position().add(0, 0.05, 0), HowitzerWalker.STOMP_RADIUS, 36, 0.05F, reach);
		} else {
			footprint(pose, consumer, view.reach(), 0.03F, reach);
		}
		for (AABB box : view.struck()) {
			edges(pose, consumer, box.inflate(0.05), 0.03F, STRUCK);
		}
		if (view.drill() != null) {
			edges(pose, consumer, new AABB(view.drill()).inflate(0.01), 0.025F, DRILL);
		}
	}

	private static void bead(PoseStack.Pose pose, VertexConsumer consumer, Vec3 at, float size, int colour) {
		LinePreview.cube(pose, consumer, (float) at.x - size, (float) at.y - size, (float) at.z - size,
				(float) at.x + size, (float) at.y + size, (float) at.z + size, colour);
	}

	/** A flat ring of beads round {@code centre}, a little over the ground. */
	private static void ring(PoseStack.Pose pose, VertexConsumer consumer, Vec3 centre, double radius, int beads, float size, int colour) {
		for (int i = 0; i < beads; i++) {
			double angle = i * Mth.TWO_PI / beads;
			bead(pose, consumer, centre.add(Math.cos(angle) * radius, 0.1, Math.sin(angle) * radius), size, colour);
		}
	}

	/**
	 * Where a box meets the ground: its four bottom edges, a little over the floor. The fist's and ram's boxes reach
	 * up past the pilot's eye, so their upright edges would cross the view; the footprint shows the reach plainly.
	 */
	private static void footprint(PoseStack.Pose pose, VertexConsumer consumer, AABB box, float t, int colour) {
		float x0 = (float) box.minX, z0 = (float) box.minZ, x1 = (float) box.maxX, z1 = (float) box.maxZ;
		float y = (float) box.minY + 0.06F;
		LinePreview.cube(pose, consumer, x0, y - t, z0 - t, x1, y + t, z0 + t, colour);
		LinePreview.cube(pose, consumer, x0, y - t, z1 - t, x1, y + t, z1 + t, colour);
		LinePreview.cube(pose, consumer, x0 - t, y - t, z0, x0 + t, y + t, z1, colour);
		LinePreview.cube(pose, consumer, x1 - t, y - t, z0, x1 + t, y + t, z1, colour);
	}

	/** The twelve edges of a box, as thin bars. */
	private static void edges(PoseStack.Pose pose, VertexConsumer consumer, AABB box, float t, int colour) {
		float x0 = (float) box.minX, y0 = (float) box.minY, z0 = (float) box.minZ;
		float x1 = (float) box.maxX, y1 = (float) box.maxY, z1 = (float) box.maxZ;
		for (float y : new float[] {y0, y1}) {
			for (float z : new float[] {z0, z1}) {
				LinePreview.cube(pose, consumer, x0, y - t, z - t, x1, y + t, z + t, colour);
			}
			for (float x : new float[] {x0, x1}) {
				LinePreview.cube(pose, consumer, x - t, y - t, z0, x + t, y + t, z1, colour);
			}
		}
		for (float x : new float[] {x0, x1}) {
			for (float z : new float[] {z0, z1}) {
				LinePreview.cube(pose, consumer, x - t, y0, z - t, x + t, y1, z + t, colour);
			}
		}
	}

	// ---------------------------------------------------------------- the gauges

	private static void gauges(GuiGraphicsExtractor graphics, DeltaTracker delta) {
		Minecraft client = Minecraft.getInstance();
		View shown = view;
		if (shown == null) {
			return;
		}
		DieselWalker walker = shown.walker();
		Font font = client.font;
		// Top left, as the aircraft's instruments are: clear of the chat, the hotbar and the status effects.
		int x = 8;
		int y = 30;
		String attackLine = Component.translatable(attackKey(walker) + (shown.attackReady() ? "_ready" : "_cooling")).getString();
		int width = Math.max(132, Math.max(font.width(shown.weapon()), font.width(attackLine)) + 6);
		graphics.fill(x - 3, y - 3, x + width, y + 51, 0xB0101418);
		graphics.text(font, walker.getDisplayName().getString().toUpperCase(Locale.ROOT), x, y, TEXT);
		y += 11;
		float fuel = walker.fuelGauge();
		bar(graphics, font, x, y, Component.translatable("hud.jugcraft.walker.fuel").getString(), fuel,
				fuel > 0.25F ? TEXT_GOOD : fuel > 0.0F ? TEXT_WARN : TEXT_BAD);
		y += 10;
		float hull = 1.0F - walker.wear();
		bar(graphics, font, x, y, Component.translatable("hud.jugcraft.walker.hull").getString(), hull,
				hull > 0.5F ? TEXT_GOOD : hull > 0.25F ? TEXT_WARN : TEXT_BAD);
		y += 11;
		graphics.text(font, shown.weapon(), x, y, shown.weaponColour());
		y += 10;
		graphics.text(font, attackLine, x, y, shown.attackReady() ? TEXT_GOOD : TEXT_DIM);
	}

	private static String attackKey(DieselWalker walker) {
		return walker instanceof HowitzerWalker ? "hud.jugcraft.walker.stomp" : walker instanceof ArmouredWalker
				? "hud.jugcraft.walker.ram" : "hud.jugcraft.walker.fist";
	}

	private static void bar(GuiGraphicsExtractor graphics, Font font, int x, int y, String label, float share, int colour) {
		graphics.text(font, label, x, y, TEXT_DIM);
		int left = x + 30;
		int width = 64;
		graphics.fill(left, y + 1, left + width, y + 7, 0xA0101010);
		graphics.fill(left + 1, y + 2, left + 1 + Math.round((width - 2) * Mth.clamp(share, 0.0F, 1.0F)), y + 6, colour);
		graphics.text(font, percent(share), left + width + 4, y, TEXT);
	}

	/** For tests: the cockpit's view this tick (null when not piloting). */
	public static @Nullable String describe() {
		View shown = view;
		return shown == null ? null : shown.weapon() + " | impact " + shown.impact() + " | struck " + shown.struck().size();
	}

	/** For tests: whether the local pilot's telegraph has a landing point. */
	public static boolean hasImpact() {
		View shown = view;
		return shown != null && shown.impact() != null;
	}
}

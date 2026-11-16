package io.github.jimbozoomer.jugcraft.aircraft;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * How a supplied aircraft is turned when it is drawn: its body's yaw, pitch and bank, and its animated parts. Common
 * code, so that the client renderer and the server game test {@code AircraftBankingGameTests} use the same arithmetic.
 *
 * <p>Conventions, from the supplied meshes and the entity. A model's nose is +Z, its up is +Y and its pilot's left
 * (port) wing is +X: the biplane's propeller is at +Z and its rudder at -Z, the pilot sits ahead of the other seats,
 * and the parts the owner's files name "left" are at +X. The entity's yaw is Minecraft's: 0 faces +Z, 90 faces -X, and
 * a rising yaw is a turn to the pilot's right. The turn input is +1 for the left key and -1 for the right key
 * ({@code AircraftClient}); the server lowers the yaw by it, so the left key turns left.
 */
public final class AircraftAttitude {
	/** The supplied rotary cannon's pivots (rotary_cannon in aircraft_models.json), in pixels: yaw, then pitch, then roll. */
	private static final float[] CANNON_YAW = {0, 9, -5.5F}, CANNON_PITCH = {0, 3, .5F}, CANNON_ROLL = {0, .5F, -.5F};
	/** The rotary cannon's barrels run along its roll pivot's +Z and end 16 pixels (a block) out. */
	public static final double CANNON_BARREL = 1;
	/** The heavy crossbow's bolt lies on top of its stock, 5 pixels up (the supplied original uses 0.3 blocks). */
	public static final double CROSSBOW_BOLT = 5 / 16.0;

	private AircraftAttitude() {
	}

	/** The variables the supplied animation expressions read, for one frame. */
	public interface Controls {
		float engineRotation();

		/** The smoothed turn input: +1 for the left key, -1 for the right key. */
		float steering();

		float climb();

		float throttle();

		/** The body's pitch in degrees, Minecraft's xRot (negative is nose up). */
		float bodyPitch();

		/** The body's roll in degrees, from {@link #bank}. */
		float bodyBank();

		/** Where the pilot looks, in degrees in the body's frame ({@link #aim}): positive is to the pilot's right. */
		float aimYaw();

		/** Where the pilot looks, in degrees in the body's frame ({@link #aim}): negative is up. */
		float aimPitch();

		float recoil();
	}

	/**
	 * The body's roll in degrees for a smoothed turn input and engine power. A positive roll turns the model about its
	 * nose (+Z), which lifts the +X (port) wing. So a left turn (+1) rolls negative and lowers the port wing, and a
	 * right turn (-1) rolls positive and lowers the starboard wing: the craft banks into its turn.
	 */
	public static float bank(AircraftSpec spec, float turn, float power) {
		return -turn * power * (spec.aeroplane() ? 18F : spec.name().equals("warship") ? 2F : 5F);
	}

	/** The warship envelope's extra roll in degrees, about the body's nose, in the same sense as {@link #bank}. */
	public static float balloonRoll(float turn) {
		return -turn * 2;
	}

	/**
	 * Model space to the world, about the entity's position: the yaw about +Y (nose +Z to the entity's heading), then
	 * the pitch about the body's X, then the bank about the body's Z (its nose). The same as the PoseStack calls
	 * {@code rotateDegrees(Axis.YP, -yaw)}, {@code rotateDegrees(Axis.XP, pitch)}, {@code rotateDegrees(Axis.ZP, bank)}.
	 */
	public static Quaternionf body(float yaw, float pitch, float bank) {
		return new Quaternionf().rotationY(-yaw * Mth.DEG_TO_RAD).rotateX(pitch * Mth.DEG_TO_RAD)
				.rotateZ(bank * Mth.DEG_TO_RAD);
	}

	/**
	 * Where a point of the supplied model (blocks, from its origin) is drawn, relative to the entity's position: the
	 * renderer lifts the model by its ground offset, then turns it by {@link #body}. Seats and weapon mounts go through
	 * here, so that riders sit and shots leave where the model is drawn, as the supplied original places them (its
	 * VehicleEntity transforms them by the same Ry(-yaw) Rx(pitch) Rz(roll) matrix it draws with). Vanilla's
	 * {@code Vec3.xRot(a)} is not Rx(a) but Rx(-a), so {@code seat.xRot(pitch).yRot(-yaw)} tipped seats and mounts the
	 * opposite way to the drawn model.
	 */
	public static Vec3 place(AircraftSpec spec, float yaw, float pitch, float bank, double x, double y, double z) {
		return turn(yaw, pitch, bank, x, y, z).add(0, spec.groundOffset(), 0);
	}

	/** An offset or a direction in the model's frame, turned as the body is drawn. */
	public static Vec3 turn(float yaw, float pitch, float bank, double x, double y, double z) {
		Vector3f v = body(yaw, pitch, bank).transform(new Vector3f((float) x, (float) y, (float) z));
		return new Vec3(v.x, v.y, v.z);
	}

	/**
	 * Where the pilot looks, as a mounted turret's yaw and pitch in the body's own frame, in degrees: positive yaw is to
	 * the pilot's right and negative pitch is up, as an entity's rotation. The supplied RotationalManager does the same:
	 * it turns the look into the craft's frame, then takes yaw = -atan2(x, z) and pitch = -atan2(y, horizontal). Plain
	 * differences of the pilot's and the craft's angles miss the look by up to the craft's pitch when the pilot looks
	 * off the nose.
	 */
	public static float[] aim(float yaw, float pitch, float bank, Vec3 look) {
		Vector3f local = body(yaw, pitch, bank).conjugate().transform(new Vector3f((float) look.x, (float) look.y, (float) look.z));
		return new float[] {(float) Math.toDegrees(-Math.atan2(local.x, local.z)), (float) Math.toDegrees(-Math.atan2(local.y, Math.sqrt(local.x * local.x + local.z * local.z)))};
	}

	/**
	 * Where the rotary cannon's bore starts, in its mount's frame (blocks), at an aim: its roll pivot, carried by the
	 * yaw and pitch nodes as the renderer turns them ({@code -variable.yaw} gives Ry(-aimYaw), {@code variable.pitch}
	 * Rx(aimPitch)). The barrels then run {@link #CANNON_BARREL} along the aim.
	 */
	public static Vector3f cannonBore(float aimYaw, float aimPitch) {
		Vector3f roll = new Vector3f(CANNON_ROLL[0], CANNON_ROLL[1], CANNON_ROLL[2]);
		new Quaternionf().rotationX(aimPitch * Mth.DEG_TO_RAD).transform(roll);
		roll.add(CANNON_PITCH[0], CANNON_PITCH[1], CANNON_PITCH[2]);
		new Quaternionf().rotationY(-aimYaw * Mth.DEG_TO_RAD).transform(roll);
		return roll.add(CANNON_YAW[0], CANNON_YAW[1], CANNON_YAW[2]).div(16);
	}

	/**
	 * The direction the rotary cannon's barrels point, in its mount's frame, at an aim: the barrels' +Z turned as the
	 * renderer turns the yaw and pitch nodes, Ry(-aimYaw) Rx(aimPitch). Rounds fly this way, so that past the turret's
	 * travel (Aircraft clamps the synced aim pitch to 80 degrees either way) they still leave along the drawn barrels
	 * rather than along the pilot's look; inside it the two are the same.
	 */
	public static Vector3f cannonDirection(float aimYaw, float aimPitch) {
		return new Quaternionf().rotationY(-aimYaw * Mth.DEG_TO_RAD).rotateX(aimPitch * Mth.DEG_TO_RAD).transform(new Vector3f(0, 0, 1));
	}

	/**
	 * A supplied Blockbench group's turn: its rest rotation plus its animation, each {x, y, z} in degrees, in
	 * Blockbench's order (Rz Ry Rx: about x first, then y, then z).
	 */
	public static Quaternionf part(float[] rest, float[] animated) {
		return new Quaternionf().rotationZ((rest[2] + animated[2]) * Mth.DEG_TO_RAD)
				.rotateY((rest[1] + animated[1]) * Mth.DEG_TO_RAD).rotateX((rest[0] + animated[0]) * Mth.DEG_TO_RAD);
	}

	/** Only the arithmetic forms present in these owner files; never execute model-provided code. */
	public static float expression(String raw, Controls c) {
		String e = raw.replace("variable.", "").replaceAll("\\s+", "");
		if (e.isEmpty()) {
			return 0;
		}
		int sign = 1;
		if (e.startsWith("-")) {
			sign = -1;
			e = e.substring(1);
		}
		String[] parts = e.split("\\*");
		float base = switch (parts[0]) {
			case "engine_rotation" -> c.engineRotation();
			case "pressing_interpolated_x" -> c.steering();
			case "pressing_interpolated_y" -> c.climb();
			case "pressing_interpolated_z" -> c.throttle();
			case "balloon_pitch" -> c.bodyPitch() * .2F;
			case "balloon_roll" -> balloonRoll(c.steering());
			case "turret_cooldown" -> 1 - c.recoil();
			case "(1-turret_cooldown)" -> c.recoil();
			// The rotary cannon and telescope (barrel along +Z): the supplied RotationalManager's yaw and pitch, which
			// their files turn by "-variable.yaw" and "variable.pitch". The warship's turret sits in the gondola, which is
			// turned half round, and the supplied WarshipEntity sets turret_yaw and turret_pitch to minus the aim.
			case "pitch" -> c.aimPitch();
			case "yaw" -> c.aimYaw();
			case "turret_yaw" -> -c.aimYaw();
			case "turret_pitch" -> -c.aimPitch();
			case "roll" -> c.bodyBank();
			case "chest" -> 0;
			default -> {
				try {
					yield Float.parseFloat(parts[0]);
				} catch (NumberFormatException ex) {
					yield 0;
				}
			}
		};
		if (parts.length > 1) {
			base *= Float.parseFloat(parts[1]);
		}
		return sign * base;
	}
}

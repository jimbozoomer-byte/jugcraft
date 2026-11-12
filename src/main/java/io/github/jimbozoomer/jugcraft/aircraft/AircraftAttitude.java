package io.github.jimbozoomer.jugcraft.aircraft;

import net.minecraft.util.Mth;
import org.joml.Quaternionf;

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

		float aimYaw();

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
			case "pitch", "turret_pitch" -> c.aimPitch();
			case "yaw", "turret_yaw" -> -c.aimYaw();
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

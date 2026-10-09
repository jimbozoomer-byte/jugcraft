package io.github.jimbozoomer.jugcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.util.Mth;

/**
 * Draws and animates the Howitzer Walker's parts (the owner's model; tools/howitzer_walker.py, whose curves these
 * are). Walking, the legs swing at the hips and tuck at the knees while the hull bounces on them, squashing as it
 * lands and stretching as it rises, swaying side to side, the gun wobbling a beat behind. Firing, the gun slams back
 * and runs out past its rest with a wobble, the hull rocks back and squats, a cartoon star burst pops and spins at
 * the muzzle and smoke puffs roll out of it. The lamps, burst and puffs are drawn at full brightness.
 */
final class HowitzerWalkerParts {
	/** Joints in pixels: keep in sync with HIPS, KNEE, TRUNNION, BARREL_END, ELEVATION and RECOIL_STROKE in tools/howitzer_walker.py. */
	static final float[][] HIPS = {{11, 42, 0}, {-11, 42, 0}};
	static final float[] KNEE = {0, -20.33, -8.42};
	static final float[] TRUNNION = {-7, 70, 4};
	static final float[] BARREL_END = {0, 1.5, 36};
	static final float ELEVATION = -25;
	static final float RECOIL_STROKE = 6;
	/** The animation's numbers: keep in sync with tools/howitzer_walker.py. */
	static final float STRIDE_RATE = 2.0F;
	static final float LEG_SWING = 24.0F;
	static final float KNEE_BEND = 20.0F;
	static final float BOB = 2.5F;
	static final float SQUASH = 0.06F;
	static final float ROLL = 3.0F;
	static final float PITCH = 2.0F;
	static final float GUN_WOBBLE = 6.0F;
	static final float RECOIL_TICKS = 14.0F;
	static final float KICK = 6.0F;
	static final float SQUAT = 2.0F;
	static final float FLASH_TICKS = 8.0F;
	static final float PUFF_TICKS = 24.0F;
	private static final float WAIST = 38.0F;
	private static final int FULL_BRIGHT = 0xF000F0;

	private HowitzerWalkerParts() {
	}

	/** How far back the gun is, 0 to 1 of RECOIL_STROKE: a slam back, then it runs out past its rest and wobbles. */
	static float recoil(float t) {
		if (t < 0 || t > 40) {
			return 0.0F;
		}
		if (t < 2) {
			return t / 2;
		}
		float u = (t - 2) / (RECOIL_TICKS - 2);
		return (float) (Math.exp(-u * 3) * Math.cos(u * 4.5));
	}

	/** The hull's pitch after firing, degrees: it rocks back, then bounces to rest. */
	static float kick(float t) {
		return t >= 0 && t < 40 ? (float) (KICK * Math.exp(-t / 8) * Math.cos(t * 0.45)) : 0.0F;
	}

	static float squat(float t) {
		return t >= 0 && t < 12 ? SQUAT * Mth.sin(Mth.PI * t / 12) : 0.0F;
	}

	/** The star burst pops up in two ticks and shrinks away by FLASH_TICKS. */
	static float flashScale(float t) {
		if (t < 0 || t >= FLASH_TICKS) {
			return 0.0F;
		}
		return 1.3F * (t < 2 ? t / 2 : 1 - (t - 2) / (FLASH_TICKS - 2));
	}

	/** The puffs grow quickly, drift, and shrink away in their last quarter. */
	static float puffScale(float t) {
		if (t < 0 || t >= PUFF_TICKS) {
			return 0.0F;
		}
		float u = t / PUFF_TICKS;
		float grow = 0.3F + 1.3F * (1 - (1 - u) * (1 - u));
		return grow * (1 - Math.max(0.0F, (u - 0.75F) / 0.25F));
	}

	/**
	 * Draws the walker facing +z in the current pose: {@code stride} blocks walked, {@code gait} 0 standing to 1
	 * striding, {@code sinceShot} ticks since the gun fired.
	 */
	static void draw(String prefix, PoseStack pose, SubmitNodeCollector collector, int light, float stride, float gait, float sinceShot) {
		float phase = stride * STRIDE_RATE;
		float bob = BOB * gait * (0.5F - 0.5F * Mth.cos(2 * phase)) - squat(sinceShot);
		float stretch = SQUASH * gait * Mth.sin(2 * phase);
		float roll = ROLL * gait * Mth.sin(phase);
		float pitch = PITCH * gait * Mth.sin(2 * phase + 0.4F) + kick(sinceShot);
		// The legs stand on the ground; the hull bounces on them, turned and squashed about its waist.
		for (int i = 0; i < HIPS.length; i++) {
			float p = phase + (i == 0 ? 0 : Mth.PI);
			float swing = LEG_SWING * gait * Mth.sin(p);
			float bend = KNEE_BEND * gait * Math.max(0.0F, Mth.sin(p + 0.7F));
			pose.pushPose();
			at(pose, HIPS[i]);
			pose.rotateDegrees(Axis.XP, swing);
			draw(prefix + "_thigh", pose, collector, light);
			at(pose, KNEE);
			pose.rotateDegrees(Axis.XP, bend);
			draw(prefix + "_shin", pose, collector, light);
			pose.popPose();
		}
		pose.pushPose();
		pose.translate(0, (WAIST + bob) / 16.0F, 0);
		pose.rotateDegrees(Axis.XP, pitch);
		pose.rotateDegrees(Axis.ZP, roll);
		pose.scale(1 - stretch / 2, 1 + stretch, 1 - stretch / 2);
		pose.translate(0, -WAIST / 16.0F, 0);
		draw(prefix + "_hull", pose, collector, light);
		draw(prefix + "_lamps", pose, collector, FULL_BRIGHT);
		// The gun on its trunnion: pitched up, wobbling behind the bounce, recoiling along its barrel when it fires.
		pose.pushPose();
		at(pose, TRUNNION);
		pose.rotateDegrees(Axis.XP, ELEVATION + GUN_WOBBLE * gait * Mth.sin(2 * phase + 1.0F));
		pose.translate(0, -0.3F * bob / 16.0F, -recoil(sinceShot) * RECOIL_STROKE / 16.0F);
		draw(prefix + "_gun", pose, collector, light);
		float flash = flashScale(sinceShot);
		if (flash > 0) {
			pose.pushPose();
			at(pose, BARREL_END);
			pose.rotateDegrees(Axis.ZP, 60 * sinceShot / FLASH_TICKS);
			pose.scale(flash, flash, flash);
			draw(prefix + "_flash", pose, collector, FULL_BRIGHT);
			pose.popPose();
		}
		float puff = puffScale(sinceShot);
		if (puff > 0) {
			pose.pushPose();
			at(pose, BARREL_END);
			pose.translate(0, 0.5F * sinceShot / 16.0F, (6 + 1.4F * sinceShot) / 16.0F);
			pose.scale(puff, puff, puff);
			draw(prefix + "_puff", pose, collector, FULL_BRIGHT);
			pose.popPose();
		}
		pose.popPose();
		pose.popPose();
	}

	private static void at(PoseStack pose, float[] pixels) {
		pose.translate(pixels[0] / 16.0F, pixels[1] / 16.0F, pixels[2] / 16.0F);
	}

	private static void draw(String part, PoseStack pose, SubmitNodeCollector collector, int light) {
		QuadModel model = DecorQuads.get(part);
		if (model != null) {
			model.submit(pose, collector, light);
		}
	}
}

package io.github.jimbozoomer.jugcraft.client.guns;

import com.geckolib.animatable.GeoItem;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.LoopType;
import io.github.jimbozoomer.jugcraft.guns.GunItem;
import io.github.jimbozoomer.jugcraft.guns.GunSpec;
import io.github.jimbozoomer.jugcraft.guns.JugcraftGuns;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * The guns' GeckoLib animation controller: the owner's idle loops; draw, shoot, aim_shoot, inspect and the reloads are
 * triggered (by GunsClient for the player's own gun, by the server's GunActionPayload for others'), each played once
 * and back to idle. A gun loaded a shell at a time has one reload per shell count: reload_start, reload_loop that many
 * times, reload_stop. The animations' sound keyframes play the guns' sounds where the gun is.
 */
public final class GunAnimations {
	public static final String CONTROLLER = "main";
	private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
	/** Animation sound keyframes that play another event's sound (tools/guns.py EVENT_SOUNDS: "rustle" is a rustle). */
	public static final Map<String, String> SOUND_ALIASES = Map.of("rustle", "gun_rustle");
	/** The same, per gun: the Thunderpipe's and Haymaker's loops push a shell, not a magazine. */
	public static final Map<String, Map<String, String>> GUN_SOUND_ALIASES = Map.of(
			"thunderpipe", Map.of("reload_mag_in", "shell_in"),
			"haymaker", Map.of("reload_mag_in", "shell_in"));

	private GunAnimations() {
	}

	public static void register(GunItem gun, AnimatableManager.ControllerRegistrar registrar) {
		AnimationController<GunItem> controller = new AnimationController<GunItem>(CONTROLLER, 1, test -> test.setAndContinue(IDLE));
		for (String name : new String[] {"draw", "shoot", "aim_shoot", "inspect"}) {
			controller.triggerableAnim(name, once(name));
		}
		controller.triggerableAnim("idle", IDLE);
		GunSpec spec = gun.spec();
		if (spec.byShell()) {
			for (int shells = 1; shells <= spec.capacity(); shells++) {
				controller.triggerableAnim(reloadName(spec, shells), RawAnimation.begin().then("reload_start", LoopType.PLAY_ONCE)
						.thenPlayXTimes("reload_loop", shells).then("reload_stop", LoopType.PLAY_ONCE).thenLoop("idle"));
			}
		} else {
			controller.triggerableAnim("reload", once("reload"));
		}
		controller.setSoundKeyframeHandler(event -> {
			Integer owner = event.renderState().getGeckolibData(GunRenderer.OWNER);
			play(gun, event.keyframeData().getSound(), owner == null ? -1 : owner);
		});
		registrar.add(controller);
	}

	/** The triggerable reload that loads this many rounds. */
	public static String reloadName(GunSpec spec, int rounds) {
		return spec.byShell() ? "reload_" + Math.max(1, Math.min(spec.capacity(), rounds)) : "reload";
	}

	private static RawAnimation once(String name) {
		return RawAnimation.begin().then(name, LoopType.PLAY_ONCE).thenLoop("idle");
	}

	/** Plays this gun animation on the gun in this entity's main hand (on this client only). */
	public static void trigger(LivingEntity holder, String animation) {
		ItemStack stack = holder.getMainHandItem();
		if (stack.getItem() instanceof GunItem gun) {
			gun.triggerAnim(holder, GeoItem.getId(stack), CONTROLLER, animation);
		}
	}

	/** An animation's sound, at the entity holding the gun (the player's own when unknown). */
	static void play(GunItem gun, String event, int ownerId) {
		Minecraft client = Minecraft.getInstance();
		ClientLevel level = client.level;
		if (level == null) {
			return;
		}
		String name = GUN_SOUND_ALIASES.getOrDefault(gun.name(), Map.of()).getOrDefault(event, SOUND_ALIASES.getOrDefault(event, event));
		if (!JugcraftGuns.SOUND_EVENTS.contains(name)) {
			return;
		}
		Entity at = ownerId >= 0 ? level.getEntity(ownerId) : client.player;
		if (at == null) {
			at = client.player;
		}
		if (at == null) {
			return;
		}
		SoundEvent sound = JugcraftGuns.sound("guns." + name);
		level.playLocalSound(at.getX(), at.getEyeY(), at.getZ(), sound, SoundSource.PLAYERS, 0.8F, 1.0F, false);
	}
}

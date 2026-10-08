package io.github.jimbozoomer.jugcraft.client.guns;

import io.github.jimbozoomer.jugcraft.guns.GunItem;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

/**
 * How each gun looks in use, on the client only (tools/guns.py: ZOOM, two_handed(), FLASH_SIZE, the attachments'
 * "hides_flash" and the scopes' "zoom" and "view"; check_guns in tools/check_mod_data.py keeps them the same).
 */
public final class GunLooks {
	/** Each gun's look, by name. */
	static final Map<String, Look> LOOKS = new HashMap<>();
	/** How big a shot's muzzle flash is, across, in the gun model's pixels, by the round it fires. */
	static final Map<String, Float> FLASH_SIZES = Map.of("light_round", 5.0F, "rifle_round", 7.0F, "buckshot_shell", 8.0F,
			"paper_cartridge", 10.0F);
	/** The attachments that hide the flash: cans over the muzzle. */
	static final List<String> HIDE_FLASH = List.of("silencer", "baffled_silencer");
	/** The scopes (slice 7), by name: how far each narrows the view and what aiming through it shows. */
	static final Map<String, Optic> OPTICS = new HashMap<>();
	/**
	 * How much further from the eye a gun is held aimed than at the hip, in sixteenths of a block (tools/guns.py BUILDS
	 * "eye_relief"); a gun not listed is aimed at its hip's depth. The Garrison Rifle's bolt slides back along its line
	 * of sight as it fires, and at the hip's depth it came past the eye: aimed, each shot filled the screen.
	 */
	static final Map<String, Float> EYE_RELIEF = Map.of("garrison_rifle", 4.0F);
	private static final Look DEFAULT = new Look(true, 1.0F);

	static {
		LOOKS.put("rust_midge", new Look(true, 0.9F));
		LOOKS.put("patchwork_carbine", new Look(true, 0.82F));
		LOOKS.put("thunderpipe", new Look(true, 0.92F));
		LOOKS.put("warden_pistol", new Look(false, 0.9F));
		LOOKS.put("riveter_smg", new Look(true, 0.88F));
		LOOKS.put("haymaker", new Look(false, 0.92F));
		LOOKS.put("longhorn_rifle", new Look(true, 0.75F));
		LOOKS.put("drover_rifle", new Look(true, 0.8F));
		LOOKS.put("coach_gun", new Look(true, 0.9F));
		LOOKS.put("duelling_pistol", new Look(false, 0.9F));
		LOOKS.put("line_musket", new Look(true, 0.82F));
		LOOKS.put("bellmouth", new Look(true, 0.92F));
		LOOKS.put("bulldog_pistol", new Look(false, 0.9F));
		LOOKS.put("marshal_revolver", new Look(false, 0.85F));
		LOOKS.put("sapper_revolver", new Look(false, 0.9F));
		LOOKS.put("sentry_pistol", new Look(false, 0.9F));
		LOOKS.put("garrison_rifle", new Look(true, 0.85F));
		LOOKS.put("breacher", new Look(true, 0.92F));
		OPTICS.put("long_scope", new Optic(0.3F, "long_scope_reticle2", "scope_vignette", null));
		OPTICS.put("medium_scope", new Optic(0.5F, "long_scope_reticle2", "scope_vignette", null));
		OPTICS.put("reflex_sight", new Optic(0.85F, null, null, "red_dot_reticle"));
	}

	private GunLooks() {
	}

	/** The gun's look; a gun not listed is held in both hands and does not zoom. */
	public static Look of(String gun) {
		return LOOKS.getOrDefault(gun, DEFAULT);
	}

	/** The scope fitted to this gun, or null. */
	public static @Nullable Optic optic(ItemStack stack) {
		String fitted = GunItem.inSlot(stack, "optic");
		return fitted == null ? null : OPTICS.get(fitted);
	}

	/**
	 * @param twoHanded held in both hands: seen from outside, both arms come up to it ({@link GunPose})
	 * @param zoom      the field of view is multiplied by this aimed down the sights (GunFovMixin)
	 */
	public record Look(boolean twoHanded, float zoom) {
	}

	/**
	 * A scope's look ({@link GunScope}); its textures are the owner's, in textures/item/guns/optics/.
	 *
	 * @param zoom     aimed through it in first person, the field of view is multiplied by this, in place of the gun's own
	 * @param reticle  the reticle that fills the screen aimed through it, the gun put away; or null
	 * @param vignette the dark rim of the lens, drawn over the reticle; or null
	 * @param dot      a reflex sight's dot, on the middle of the screen over the gun; or null
	 */
	public record Optic(float zoom, @Nullable String reticle, @Nullable String vignette, @Nullable String dot) {
		/** Whether aiming through it fills the screen with the view through the scope (and puts the gun away). */
		public boolean fillsView() {
			return reticle != null;
		}
	}
}

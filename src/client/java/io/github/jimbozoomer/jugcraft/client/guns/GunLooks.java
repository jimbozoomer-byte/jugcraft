package io.github.jimbozoomer.jugcraft.client.guns;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * How each gun looks in use, on the client only (tools/guns.py: ZOOM, two_handed(), FLASH_SIZE and the attachments'
 * "hides_flash"; check_guns in tools/check_mod_data.py keeps them the same).
 */
public final class GunLooks {
	/** Each gun's look, by name. */
	static final Map<String, Look> LOOKS = new HashMap<>();
	/** How big a shot's muzzle flash is, across, in the gun model's pixels, by the round it fires. */
	static final Map<String, Float> FLASH_SIZES = Map.of("light_round", 5.0F, "rifle_round", 7.0F, "buckshot_shell", 8.0F,
			"paper_cartridge", 10.0F);
	/** The attachments that hide the flash: cans over the muzzle. */
	static final List<String> HIDE_FLASH = List.of("silencer", "baffled_silencer");
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
	}

	private GunLooks() {
	}

	/** The gun's look; a gun not listed is held in both hands and does not zoom. */
	public static Look of(String gun) {
		return LOOKS.getOrDefault(gun, DEFAULT);
	}

	/**
	 * @param twoHanded held in both hands: seen from outside, both arms come up to it ({@link GunPose})
	 * @param zoom      the field of view is multiplied by this aimed down the sights (GunFovMixin)
	 */
	public record Look(boolean twoHanded, float zoom) {
	}
}

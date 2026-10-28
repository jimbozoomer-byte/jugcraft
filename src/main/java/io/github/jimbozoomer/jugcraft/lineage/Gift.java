package io.github.jimbozoomer.jugcraft.lineage;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import org.jspecify.annotations.Nullable;

/**
 * The hidden gifts (docs/features/star-bred-lines.md): recessive, so one copy (a carrier) shows nothing and two show
 * the gift. Each belongs to the kinds named here, by entity id; tools/lineage.py GIFTS mirrors this and
 * tools/check_mod_data.py (check_lineage) holds the two together and every kind to #jugcraft:star_bred.
 */
public enum Gift {
	/** 1 laid egg in {@value Lineage#DOUBLE_YOLK} comes as two. */
	DOUBLE_YOLK("double_yolk", "minecraft:chicken", "jugcraft:turkey"),
	/** Sheared at night under the open sky, one more wool. */
	MOONFLEECE("moonfleece", "minecraft:sheep"),
	/** A goat with a coat to shear for white wool, which grows back over a game day. */
	ANGORA("angora", "minecraft:goat"),
	/** A pig with a coat to shear for brown wool, which grows back over a game day. */
	WOOLLY_COAT("woolly_coat", "minecraft:pig"),
	/** Always drops its foot. */
	LUCKY_FOOT("lucky_foot", "minecraft:rabbit"),
	/** One more leather. */
	HEAVY_HIDE("heavy_hide", "minecraft:cow", "minecraft:mooshroom");

	public final String id;
	public final Set<String> kinds;

	Gift(String id, String... kinds) {
		this.id = id;
		this.kinds = Set.of(kinds);
	}

	public String key() {
		return "lineage.jugcraft.gift." + id;
	}

	/** The gifts an animal of {@code type} can carry, in declaration order. */
	public static List<Gift> of(EntityType<?> type) {
		String kind = BuiltInRegistries.ENTITY_TYPE.getKey(type).toString();
		List<Gift> out = new ArrayList<>();
		for (Gift gift : values()) {
			if (gift.kinds.contains(kind)) {
				out.add(gift);
			}
		}
		return out;
	}

	public static @Nullable Gift byId(String id) {
		for (Gift gift : values()) {
			if (gift.id.equals(id)) {
				return gift;
			}
		}
		return null;
	}
}

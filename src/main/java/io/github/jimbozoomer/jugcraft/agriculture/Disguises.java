package io.github.jimbozoomer.jugcraft.agriculture;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import io.github.jimbozoomer.jugcraft.config.JugcraftConfig;
import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

/**
 * Costume disguises (docs/features/costume-disguises.md), all year: a player wearing a costume of a kind (in any armour
 * slot) is taken for one of their own by the monsters of that kind, which never pick them as a target, until they
 * strike one: a monster a player has just hurt fights back as ever. Which costumes fool which monsters is data, a pair of
 * tags for each kind in {@link #KINDS}: {@code #jugcraft:disguises/<kind>} for the costumes (items) and for the monsters
 * (entity types). Bosses are in none of them. Checked on the server whenever a monster picks a target
 * ({@code mixin/DisguiseTargetMixin}); the server's {@code costumes.disguises} ({@code on} or {@code off}) turns them off.
 */
public final class Disguises {
	/** The kinds of disguise: the skeleton, the walking dead, the witch, the werewolf, the ghost, the cat and the night. */
	public static final List<String> KINDS = List.of("skeleton", "walking_dead", "witch", "werewolf", "ghost", "cat", "night");
	public static final String OPTION = "costumes.disguises";
	private static final List<EquipmentSlot> WORN = List.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET);
	private static final List<TagKey<Item>> COSTUMES = KINDS.stream()
			.map(kind -> TagKey.create(Registries.ITEM, Jugcraft.id("disguises/" + kind))).toList();
	private static final List<TagKey<EntityType<?>>> FOOLED = KINDS.stream()
			.map(kind -> TagKey.create(Registries.ENTITY_TYPE, Jugcraft.id("disguises/" + kind))).toList();

	private Disguises() {
	}

	/** Whether disguises are on: {@code costumes.disguises}. */
	public static boolean enabled() {
		return !"off".equalsIgnoreCase(JugcraftConfig.textOption(OPTION));
	}

	/**
	 * Whether {@code mob} takes {@code player} for one of its own: the player wears a costume of a kind the mob is, and
	 * has not just hurt it.
	 */
	public static boolean fooled(Mob mob, Player player) {
		if (!enabled() || mob.getLastHurtByMob() == player) {
			return false;
		}
		for (int i = 0; i < KINDS.size(); i++) {
			if (mob.is(FOOLED.get(i)) && wears(player, COSTUMES.get(i))) {
				return true;
			}
		}
		return false;
	}

	/** The kind {@code player} is disguised as, if any (the first of {@link #KINDS} they wear a costume of), or null. */
	public static String disguise(Player player) {
		for (int i = 0; i < KINDS.size(); i++) {
			if (wears(player, COSTUMES.get(i))) {
				return KINDS.get(i);
			}
		}
		return null;
	}

	private static boolean wears(Player player, TagKey<Item> costumes) {
		for (EquipmentSlot slot : WORN) {
			if (player.getItemBySlot(slot).is(costumes)) {
				return true;
			}
		}
		return false;
	}
}

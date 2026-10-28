package io.github.jimbozoomer.jugcraft.client;

import io.github.jimbozoomer.jugcraft.Jugcraft;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import org.jspecify.annotations.Nullable;

/**
 * The magic interfaces' visual-only portraits (docs/features/magic-interfaces.md, spec 2.7): a creature of each type,
 * made once per world and never added to it, for the chooser's cards and the roster when no live one stands near. Unlike
 * the roster's first cache it advances its creatures' age each tick (a magic screen's tick calls {@link #tick}), and
 * MagicPortrait draws them at the frame's partial tick, so a GeckoLib spirit plays its idle pose rather than holding its
 * first frame; under reduced motion they hold still. At most sixteen are kept; a new world forgets them, and leaving a
 * world clears them (PactClient). Their ids are their own range, apart from the roster's.
 */
final class MagicPortraits {
	private static final int MAX = 16;
	private static final Map<String, Optional<LivingEntity>> MADE = new LinkedHashMap<>(16, 0.75F, true);
	private static @Nullable ClientLevel level;
	private static int nextId = -3_500_000;

	private MagicPortraits() {
	}

	/** A portrait of entity type {@code type} (an id), or null when it is none or cannot be made. */
	static @Nullable LivingEntity of(String type) {
		ClientLevel world = Minecraft.getInstance().level;
		if (world == null || type.isEmpty()) {
			return null;
		}
		if (world != level) {
			MADE.clear();
			level = world;
		}
		Optional<LivingEntity> known = MADE.get(type);
		if (known != null) {
			return known.orElse(null);
		}
		LivingEntity made = null;
		Identifier id = Identifier.tryParse(type);
		EntityType<?> kind = id == null ? null : BuiltInRegistries.ENTITY_TYPE.getOptional(id).orElse(null);
		try {
			if (kind != null && kind.create(world, EntitySpawnReason.LOAD) instanceof LivingEntity creature) {
				creature.setId(nextId--);
				if (creature instanceof Mob mob) {
					mob.setNoAi(true);
				}
				creature.setYRot(0.0F);
				creature.yBodyRot = creature.yBodyRotO = creature.yHeadRot = creature.yHeadRotO = 0.0F;
				made = creature;
			}
		} catch (RuntimeException error) {
			Jugcraft.LOGGER.warn("Could not make a magic interface portrait of {}", type, error);
		}
		if (MADE.size() >= MAX) {
			MADE.remove(MADE.keySet().iterator().next());
		}
		MADE.put(type, Optional.ofNullable(made));
		return made;
	}

	/** One tick of age for every kept portrait, so its animation plays; none under reduced motion. */
	static void tick() {
		if (ConcordanceClientOptions.reducedMotion()) {
			return;
		}
		for (Optional<LivingEntity> made : MADE.values()) {
			made.ifPresent(creature -> creature.tickCount++);
		}
	}

	/** Forgets every portrait and its world (PactClient, on leaving a world). */
	static void clear() {
		MADE.clear();
		level = null;
	}
}

package io.github.jimbozoomer.jugcraft.concordance.spirits;

import io.github.jimbozoomer.jugcraft.concordance.worker.Pacts;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import org.jspecify.annotations.Nullable;

/**
 * Concordance garden cohort, GC-01 (SG3): every pact spirit a perch can call, by its counterpart (its pact definition's id,
 * which is its entity id): its entity type and its sign (the item the pact screen draws by its name;
 * tools/concordance_pacts.py SPIRITS). Filled once in Workers.register. A counterpart not here is never offered, summoned,
 * seated or listed as a pact by the roster.
 */
public final class PactSpirits {
	/** One pact spirit: its counterpart, its entity type and its sign (an item id). */
	public record Spirit(String counterpart, EntityType<? extends PactSpiritEntity<?>> type, String sign) {
	}

	private static final Map<String, Spirit> SPIRITS = new LinkedHashMap<>();

	private PactSpirits() {
	}

	/** Registers {@code type} for {@code counterpart} once (Workers.register, after the entity type is registered): its counterpart is its entity id. */
	static void register(String counterpart, EntityType<? extends PactSpiritEntity<?>> type, String sign) {
		String id = Pacts.counterpart(counterpart);
		if (!id.equals(BuiltInRegistries.ENTITY_TYPE.getKey(type).toString())) {
			throw new IllegalStateException("A pact spirit's counterpart is its entity id: " + id);
		}
		if (SPIRITS.putIfAbsent(id, new Spirit(id, type, sign)) != null) {
			throw new IllegalStateException("Pact spirit " + id + " is registered twice");
		}
	}

	public static @Nullable Spirit get(@Nullable String counterpart) {
		return counterpart == null ? null : SPIRITS.get(Pacts.counterpart(counterpart));
	}

	public static boolean knows(@Nullable String counterpart) {
		return get(counterpart) != null;
	}

	/** {@code counterpart}'s sign, or "" for a counterpart that is no pact spirit. */
	public static String sign(@Nullable String counterpart) {
		Spirit spirit = get(counterpart);
		return spirit == null ? "" : spirit.sign();
	}

	public static Collection<Spirit> all() {
		return Collections.unmodifiableCollection(SPIRITS.values());
	}
}

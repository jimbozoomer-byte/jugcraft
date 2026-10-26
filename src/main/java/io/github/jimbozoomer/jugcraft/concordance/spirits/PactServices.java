package io.github.jimbozoomer.jugcraft.concordance.spirits;

import io.github.jimbozoomer.jugcraft.concordance.worker.PactParser;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import org.jspecify.annotations.Nullable;

/**
 * Concordance garden cohort, GC-01 (SG1): the pact services, one a work, registered once (Workers.register). Each is one
 * PactParser.WORK names, holder-only exactly when PactParser.HOLDER_ONLY says so; {@link #verify} refuses to start without
 * one for every work the parser accepts (check_pacts holds the same on the sources).
 */
public final class PactServices {
	private static final Map<String, PactService> SERVICES = new LinkedHashMap<>();

	private PactServices() {
	}

	static void register(PactService service) {
		String work = service.work();
		if (!PactParser.WORK.contains(work) || service.holderOnly() != PactParser.HOLDER_ONLY.contains(work)) {
			throw new IllegalStateException("Pact service " + work + " is not one PactParser.WORK names as it is");
		}
		if (SERVICES.putIfAbsent(work, service) != null) {
			throw new IllegalStateException("Pact service " + work + " is registered twice");
		}
	}

	/** Every work the parser accepts has its service (Workers.register, once every service is registered). */
	static void verify() {
		for (String work : PactParser.WORK) {
			if (!SERVICES.containsKey(work)) {
				throw new IllegalStateException("No pact service for the work " + work);
			}
		}
	}

	/** {@code work}'s service, or null for a work this version does not know. */
	public static @Nullable PactService get(@Nullable String work) {
		return work == null ? null : SERVICES.get(work);
	}

	public static Collection<PactService> all() {
		return Collections.unmodifiableCollection(SERVICES.values());
	}
}

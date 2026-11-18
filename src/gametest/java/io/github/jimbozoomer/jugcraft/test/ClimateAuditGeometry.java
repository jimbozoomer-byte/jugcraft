package io.github.jimbozoomer.jugcraft.test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The climate audit's pure band oracle (worker D), from CONTRACT.md section 1 and Amendment 4 only, never from the
 * climate core's code. No Minecraft or library imports, so it also compiles and runs with a bare JDK
 * ({@code tools/climate-audit/ClimateOracleCheck.java}).
 *
 * <p>North is -Z. With {@code s = blockZ - anchorZ} (south positive) and {@code n = -s}, in doubled integers so odd
 * transitions agree with the editor: the centred edge at threshold h blends iff {@code 2h - T <= 2s < 2h + T}; the north
 * frigid inner blend is {@code north_frigid - T <= n < north_frigid}; the south frigid inner blend is
 * {@code south_frigid - T <= s < south_frigid}; at and beyond each frigid limit the zone is frigid only.
 */
public final class ClimateAuditGeometry {
	public static final String FRIGID = "frigid";
	public static final String HOT = "hot";
	public static final String TEMPERATE = "temperate";
	public static final String CENTRAL = "central";

	private ClimateAuditGeometry() {
	}

	/** The contract's profile (section 3) as plain integers, with the audit's own validation and band geometry. */
	public record Geometry(int anchorX, int anchorZ, int north, int hotStart, int hotEnd, int southFrigid, int transition, int salt) {
		public static Geometry defaults(int anchorX, int anchorZ) {
			return new Geometry(anchorX, anchorZ, 2500, 2000, 4000, 5500, 256, 0);
		}

		public Geometry withTransition(int t) {
			return new Geometry(anchorX, anchorZ, north, hotStart, hotEnd, southFrigid, t, salt);
		}

		public Geometry withSalt(int value) {
			return new Geometry(anchorX, anchorZ, north, hotStart, hotEnd, southFrigid, transition, value);
		}

		public Geometry withAnchor(int x, int z) {
			return new Geometry(x, z, north, hotStart, hotEnd, southFrigid, transition, salt);
		}

		/** Amendment 4's eleven rules (rules 1, 2 and 6 hold by construction here: schema 1, mode fixed, an int salt). */
		public boolean valid() {
			return Math.abs((long) anchorX) <= 30_000_000 && Math.abs((long) anchorZ) <= 30_000_000
					&& threshold(north) && threshold(hotStart) && threshold(hotEnd) && threshold(southFrigid)
					&& transition >= 0 && transition <= 1024
					&& north > transition
					&& hotStart < hotEnd && hotEnd < southFrigid
					&& 2L * hotStart > transition
					&& (long) hotEnd - hotStart >= transition
					&& 2L * ((long) southFrigid - hotEnd) >= 3L * transition;
		}

		private static boolean threshold(int v) {
			return v >= 1 && v <= 1_000_000;
		}

		/** The zones the contract allows at block z (any x): exactly one outside a blend, the two adjacent zones inside one. */
		public Set<String> allowed(int blockZ) {
			long s = (long) blockZ - anchorZ;
			long n = -s;
			long t = transition;
			if (n >= north || s >= southFrigid) return Set.of(FRIGID);
			if (t > 0 && n >= north - t) return Set.of(CENTRAL, FRIGID);
			if (t > 0 && s >= southFrigid - t) return Set.of(TEMPERATE, FRIGID);
			if (t > 0 && 2L * hotStart - t <= 2 * s && 2 * s < 2L * hotStart + t) return Set.of(CENTRAL, HOT);
			if (t > 0 && 2L * hotEnd - t <= 2 * s && 2 * s < 2L * hotEnd + t) return Set.of(HOT, TEMPERATE);
			if (s < hotStart) return Set.of(CENTRAL);
			if (s < hotEnd) return Set.of(HOT);
			return Set.of(TEMPERATE);
		}

		/**
		 * The zones a stored biome quart may take: frigid only when any block that reads the quart (Minecraft's biome
		 * zoom fuzzes block z between quarts {@code (z-2)>>2} and the next, so quart q is read by blocks {@code 4q-2} to
		 * {@code 4q+5}) lies at or beyond a frigid limit, which keeps the limits strict at block level; otherwise
		 * {@link #allowed} at the quart's corner block.
		 */
		public Set<String> allowedForQuart(int quartZ) {
			long lowest = 4L * quartZ - 2, highest = 4L * quartZ + 5;
			if (anchorZ - lowest >= north || highest - anchorZ >= southFrigid) return Set.of(FRIGID);
			return allowed(Math.toIntExact(4L * quartZ));
		}

		/** The one zone outside the blends; null inside one. */
		public String certain(int blockZ) {
			Set<String> zones = allowed(blockZ);
			return zones.size() == 1 ? zones.iterator().next() : null;
		}

		/** Block z at south distance s (negative: north) from the anchor. */
		public int z(long s) {
			return Math.toIntExact(anchorZ + s);
		}

		/**
		 * The four blends as {first s inside, first s past, outer zone}: north frigid (s runs north, so it is listed by n),
		 * central to hot, hot to temperate, south frigid. Empty when T is 0.
		 */
		public List<Blend> blends() {
			if (transition == 0) return List.of();
			long t = transition;
			// North: north - T <= n < north, so s from -north + 1 to -(north - T) inclusive.
			// Centred: 2h - T <= 2s < 2h + T, so s from ceil((2h - T) / 2) to ceil((2h + T) / 2) exclusive.
			return List.of(new Blend("north frigid", -(long) north + 1, -(north - t) + 1, FRIGID, true),
					new Blend("central to hot", Math.ceilDiv(2L * hotStart - t, 2), Math.ceilDiv(2L * hotStart + t, 2), HOT, false),
					new Blend("hot to temperate", Math.ceilDiv(2L * hotEnd - t, 2), Math.ceilDiv(2L * hotEnd + t, 2), TEMPERATE, false),
					new Blend("south frigid", southFrigid - t, southFrigid, FRIGID, false));
		}
	}

	/**
	 * One blend: south distances {@code [from, to)}; {@code outer} is the zone it ramps towards (the frigid side of a frigid
	 * blend, the southern zone of a centred edge). {@code north} means the ramp runs towards smaller s.
	 */
	public record Blend(String name, long from, long to, String outer, boolean north) {
		/** 0 at the inner end of the ramp, 1 at the outer end. */
		public double progress(long s) {
			double p = (s - from + 0.5) / (double) (to - from);
			return north ? 1 - p : p;
		}
	}

	/**
	 * Static state in a class and its nested classes that two worlds could share: non-final statics, and final statics
	 * holding a visible or object array, a mutable collection or map, an atomic or a thread-local. Enum constants,
	 * synthetic fields, primitives, strings, private constant tables of primitives and immutable JDK or Guava
	 * collections are allowed (contract section 3: no global state). Returns findings.
	 */
	public static List<String> mutableStatics(Class<?> type) {
		List<String> out = new ArrayList<>();
		List<Class<?>> classes = new ArrayList<>(List.of(type));
		for (int i = 0; i < classes.size(); i++) {
			Class<?> c = classes.get(i);
			classes.addAll(List.of(c.getDeclaredClasses()));
			for (Field field : c.getDeclaredFields()) {
				int mods = field.getModifiers();
				if (!Modifier.isStatic(mods) || field.isSynthetic() || field.isEnumConstant()) continue;
				String where = c.getName() + "." + field.getName();
				if (!Modifier.isFinal(mods)) {
					out.add(where + " is a non-final static");
					continue;
				}
				Class<?> declared = field.getType();
				if (declared.isPrimitive() || declared == String.class) continue;
				Object value;
				try {
					field.setAccessible(true);
					value = field.get(null);
				} catch (ReflectiveOperationException | RuntimeException e) {
					out.add(where + " could not be inspected: " + e);
					continue;
				}
				if (value == null) continue;
				String runtime = value.getClass().getName();
				boolean immutable = runtime.startsWith("java.util.ImmutableCollections") || runtime.startsWith("java.util.Collections$Unmodifiable")
						|| runtime.startsWith("java.util.Collections$Empty") || runtime.startsWith("java.util.Collections$Singleton")
						|| runtime.startsWith("com.google.common.collect.Immutable") || runtime.startsWith("com.google.common.collect.Regular")
						|| runtime.startsWith("com.google.common.collect.Singleton");
				if (value.getClass().isArray()) {
					// A private constant table of primitives (for example vanilla's humidity band edges) is not state; a
					// visible or object array could be changed by anyone, so it is.
					if (!(Modifier.isPrivate(mods) && declared.getComponentType().isPrimitive())) out.add(where + " is a static array (mutable)");
				} else if ((value instanceof Collection<?> || value instanceof Map<?, ?>) && !immutable) {
					out.add(where + " is a static mutable " + runtime);
				} else if (runtime.startsWith("java.util.concurrent.atomic.") || value instanceof ThreadLocal<?>) {
					out.add(where + " is static mutable state (" + runtime + ")");
				}
			}
		}
		return out;
	}
}

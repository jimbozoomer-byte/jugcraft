package io.github.jimbozoomer.jugcraft.test;

import io.github.jimbozoomer.jugcraft.test.ClimateAuditGeometry.Blend;
import io.github.jimbozoomer.jugcraft.test.ClimateAuditGeometry.Geometry;
import io.github.jimbozoomer.jugcraft.world.climate.ClimateAnchor;
import io.github.jimbozoomer.jugcraft.world.climate.ClimateBands;
import io.github.jimbozoomer.jugcraft.world.climate.ClimateGeometry;
import io.github.jimbozoomer.jugcraft.world.climate.ClimateRules;
import io.github.jimbozoomer.jugcraft.world.climate.ClimateZone;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.SplittableRandom;

/**
 * Property checks of the climate bands' pure geometry (worker D's audit). Compiled and run with JDK 21 outside
 * Gradle, against the climate core's pure classes ({@code ClimateAnchor}, {@code ClimateBands}, {@code ClimateGeometry},
 * {@code ClimateNoise}, {@code ClimateRules}, {@code ClimateZone}) and the audit's own oracle
 * ({@code src/gametest/.../ClimateAuditGeometry.java}); {@code ClimateBands}' one profile overload is satisfied by a
 * one-line stub of {@code ClimateProfile} (see {@code run.sh}). Not a Gradle test, not a game test, not run in
 * Minecraft. Exits non-zero on any failed property and prints measurements for the review.
 *
 * <p>Usage: {@code java ... ClimateBandsPropertyCheck [vectors.json]}: also writes oracle vectors for the editor
 * cross-check ({@code js-crosscheck.cjs}).
 */
public final class ClimateBandsPropertyCheck {
	private static int failures;
	private static int checks;

	public static void main(String[] args) throws IOException {
		SplittableRandom random = new SplittableRandom(0x5EEDC11AL);
		Geometry defaults = Geometry.defaults(320, -192);
		check(defaults.valid(), "the defaults pass the audit oracle's eleven rules");
		check(rules(defaults).isEmpty(), "the defaults pass ClimateRules: " + rules(defaults));

		// 1. Validity: the core's eleven rules agree with the oracle on random and boundary profiles.
		List<Geometry> profiles = new ArrayList<>();
		for (int i = 0; i < 200_000; i++) profiles.add(randomGeometry(random));
		profiles.addAll(boundaries());
		int valid = 0, disagreements = 0;
		for (Geometry g : profiles) {
			boolean core = rules(g).isEmpty();
			if (core != g.valid()) {
				if (disagreements++ < 10) fail("ClimateRules and the oracle disagree on " + g + ": core " + rules(g));
			}
			if (core) valid++;
		}
		checks++;
		System.out.printf(Locale.ROOT, "validity: %d profiles, %d valid, %d disagreements%n", profiles.size(), valid, disagreements);

		// 2. The contract's rules are exactly the geometric guarantees: for profiles within the range rules (3-5) and rule 8,
		// rules 7 and 9-11 hold iff the four blends are disjoint and the anchor row is pure central.
		int tight = 0;
		for (Geometry g : profiles) {
			if (!rangesOk(g)) continue;
			boolean geometric = geometricallySound(g);
			if (geometric != g.valid()) fail("rules 7, 9-11 are not the geometric guarantees for " + g + ": sound " + geometric);
			tight++;
		}
		checks++;
		System.out.printf(Locale.ROOT, "tightness: %d in-range profiles; rules 7 and 9-11 match disjoint blends and a central anchor%n", tight);

		// 3. The core's block zone is the oracle's: exact outside blends, one of the two adjacent zones inside.
		long compared = 0;
		for (int i = 0; i < 400; i++) {
			Geometry g = i == 0 ? defaults : i == 1 ? defaults.withTransition(257) : i == 2 ? defaults.withTransition(0)
					: i == 3 ? defaults.withAnchor(-30_000_000, 30_000_000) : i == 4 ? defaults.withAnchor(30_000_000, -30_000_000) : null;
			while (g == null || !g.valid()) g = randomGeometry(random);
			ClimateGeometry core = core(g);
			long lo = -(long) g.north() - 700, hi = (long) g.southFrigid() + 700;
			int[] xs = {-30_000_000, -1_234_567, -1, 0, 77_777, 29_999_999};
			for (long s = lo; s <= hi; s += (i < 5 ? 1 : 7)) {
				long z = g.anchorZ() + s;
				if (z < Integer.MIN_VALUE || z > Integer.MAX_VALUE) continue;
				Set<String> allowed = g.allowed((int) z);
				for (int x : xs) {
					String zone = name(ClimateBands.zone(core, x, (int) z));
					compared++;
					if (!allowed.contains(zone)) {
						fail("zone " + zone + " not allowed " + allowed + " at s " + s + " x " + x + " for " + g);
						break;
					}
				}
			}
		}
		checks++;
		System.out.printf(Locale.ROOT, "zones: %d block columns compared with the oracle%n", compared);

		// 4. Strict at block level: a quart that any block at or beyond a frigid limit reads (BiomeManager fuzz: blocks
		// 4q-2 to 4q+5) is frigid; every other quart takes a zone the oracle allows at its corner block.
		long quarts = 0;
		for (Geometry g : List.of(defaults, defaults.withTransition(0), defaults.withTransition(5), defaults.withTransition(257))) {
			ClimateGeometry core = core(g);
			long first = Math.floorDiv((long) g.anchorZ() - g.north() - 600, 4), last = Math.floorDiv((long) g.anchorZ() + g.southFrigid() + 600, 4);
			for (long qz = first; qz <= last; qz++) {
				boolean readByStrict = g.anchorZ() - (4 * qz - 2) >= g.north() || 4 * qz + 5 - g.anchorZ() >= g.southFrigid();
				for (int qx : new int[]{-7_500_000, -3, 0, 12_345}) {
					String zone = name(ClimateBands.quartZone(core, qx, (int) qz));
					quarts++;
					if (readByStrict ? !zone.equals("frigid") : !g.allowed((int) (4 * qz)).contains(zone)) {
						fail("quart " + qx + " " + qz + " is " + zone + " for " + g + " (read by a strict block " + readByStrict + ")");
					}
				}
			}
		}
		checks++;
		System.out.printf(Locale.ROOT, "quarts: %d quart zones strict at block level%n", quarts);

		// 5. Blend shape (defaults): the outer zone's share per tenth of each blend, against the stated smoothstep, and
		// how often neighbouring quarts disagree (salt and pepper would be about one in two).
		ClimateGeometry core = core(defaults);
		for (Blend blend : defaults.blends()) {
			int tenths = 10;
			double[] outer = new double[tenths];
			int[] count = new int[tenths];
			for (long s = blend.from(); s < blend.to(); s++) {
				int z = defaults.z(s);
				int bucket = Math.min(tenths - 1, (int) (blend.progress(s) * tenths));
				for (int i = 0; i < 2000; i++) {
					int x = -1_000_000 + i * 997;
					if (name(ClimateBands.zone(core, x, z)).equals(blend.outer())) outer[bucket]++;
					count[bucket]++;
				}
			}
			StringBuilder line = new StringBuilder();
			double worst = 0;
			for (int b = 0; b < tenths; b++) {
				double share = outer[b] / count[b];
				double mid = (b + 0.5) / tenths, stated = mid * mid * (3 - 2 * mid);
				worst = Math.max(worst, Math.abs(share - stated));
				line.append(String.format(Locale.ROOT, " %.2f", share));
			}
			// Uniformity: the realized share per tenth follows the stated smoothstep ramp, so the blend is T wide (A2).
			check(worst <= 0.08, blend.name() + ": the outer share strays " + worst + " from the smoothstep ramp:" + line);
			double first = outer[0] / count[0], lastShare = outer[tenths - 1] / count[tenths - 1];
			check(first <= 0.25 && lastShare >= 0.75, blend.name() + ": the outer zone must be rare at the inner edge and common at the outer: " + line);
			boolean monotone = true;
			for (int b = 1; b < tenths; b++) monotone &= outer[b] / count[b] >= outer[b - 1] / count[b - 1] - 0.03;
			check(monotone, blend.name() + ": the outer share must rise across the blend:" + line);
			// Coherence along x at the blend's middle row, per quart.
			int z = defaults.z((blend.from() + blend.to()) / 2);
			int changes = 0, pairs = 0;
			String previous = null;
			for (int x = -131_072; x < 131_072; x += 4) {
				String zone = name(ClimateBands.zone(core, x, z));
				if (previous != null) {
					pairs++;
					if (!zone.equals(previous)) changes++;
				}
				previous = zone;
			}
			double rate = changes / (double) pairs;
			check(rate < 0.2, blend.name() + ": neighbouring quarts disagree too often (" + rate + "): salt and pepper");
			System.out.printf(Locale.ROOT, "blend %s: outer share by tenth%s; worst gap from smoothstep %.2f; quart neighbours differ %.3f%n",
					blend.name(), line, worst, rate);
		}
		// Salt changes the fringe, never the pure belts.
		ClimateGeometry salted = core(defaults.withSalt(12345));
		int fringe = 0, pure = 0;
		for (long s = -6000; s <= 6000; s += 3) {
			int z = defaults.z(s);
			for (int x = -50_000; x <= 50_000; x += 1009) {
				boolean same = ClimateBands.zone(core, x, z) == ClimateBands.zone(salted, x, z);
				if (defaults.certain(z) != null) {
					if (!same) pure++;
				} else if (!same) fringe++;
			}
		}
		check(pure == 0, "salt changed " + pure + " columns outside the blends");
		check(fringe > 0, "salt never changed a blend column");
		System.out.printf(Locale.ROOT, "salt: %d blend columns differ between salt 0 and 12345, %d pure columns%n", fringe, pure);

		// 6. Determinism: answers do not depend on the order of calls.
		List<long[]> points = new ArrayList<>();
		for (int i = 0; i < 50_000; i++) points.add(new long[]{random.nextInt(-200_000, 200_000), defaults.z(random.nextLong(-3000, 6000))});
		List<ClimateZone> forward = new ArrayList<>();
		for (long[] p : points) forward.add(ClimateBands.zone(core, (int) p[0], (int) p[1]));
		for (int i = points.size() - 1; i >= 0; i--) {
			if (ClimateBands.zone(salted, (int) points.get(i)[0], (int) points.get(i)[1]) == null) fail("null zone");
			if (ClimateBands.zone(core, (int) points.get(i)[0], (int) points.get(i)[1]) != forward.get(i)) {
				fail("zone depends on call order at " + points.get(i)[0] + " " + points.get(i)[1]);
				break;
			}
		}
		checks++;

		// 7. No shared mutable statics in the pure core (contract section 3; the game test scans the rest).
		for (String name : List.of("ClimateAnchor", "ClimateBands", "ClimateGeometry", "ClimateNoise", "ClimateRules", "ClimateSelection", "ClimateZone")) {
			try {
				List<String> statics = ClimateAuditGeometry.mutableStatics(Class.forName("io.github.jimbozoomer.jugcraft.world.climate." + name));
				check(statics.isEmpty(), name + " holds shared mutable statics: " + statics);
			} catch (ClassNotFoundException e) {
				fail("missing core class " + name);
			}
		}

		if (args.length > 0) writeVectors(Path.of(args[0]), random, profiles);
		System.out.printf(Locale.ROOT, "%d property groups, %d failures%n", checks, failures);
		System.exit(failures == 0 ? 0 : 1);
	}

	private static List<String> rules(Geometry g) {
		return ClimateRules.problems(1, "fixed", g.anchorX(), g.anchorZ(), g.north(), g.hotStart(), g.hotEnd(), g.southFrigid(), g.transition());
	}

	private static ClimateGeometry core(Geometry g) {
		return new ClimateGeometry(new ClimateAnchor(g.anchorX(), g.anchorZ()), g.north(), g.hotStart(), g.hotEnd(), g.southFrigid(),
				g.transition(), g.salt());
	}

	private static String name(ClimateZone zone) {
		return zone.name().toLowerCase(Locale.ROOT);
	}

	private static boolean rangesOk(Geometry g) {
		return Math.abs((long) g.anchorX()) <= 30_000_000 && Math.abs((long) g.anchorZ()) <= 30_000_000 && in(g.north()) && in(g.hotStart())
				&& in(g.hotEnd()) && in(g.southFrigid()) && g.transition() >= 0 && g.transition() <= 1024 && g.hotStart() < g.hotEnd()
				&& g.hotEnd() < g.southFrigid();
	}

	private static boolean in(int v) {
		return v >= 1 && v <= 1_000_000;
	}

	/** The four blends' raw intervals are pairwise disjoint, and the anchor row is pure central. */
	private static boolean geometricallySound(Geometry g) {
		long t = g.transition();
		if (t == 0) return true;
		long[][] spans = {{-(long) g.north() + 1, -(g.north() - t) + 1}, {Math.ceilDiv(2L * g.hotStart() - t, 2), Math.ceilDiv(2L * g.hotStart() + t, 2)},
				{Math.ceilDiv(2L * g.hotEnd() - t, 2), Math.ceilDiv(2L * g.hotEnd() + t, 2)}, {g.southFrigid() - t, g.southFrigid()}};
		for (int i = 0; i < spans.length; i++) {
			if (spans[i][0] <= 0 && 0 < spans[i][1]) return false;
			for (int j = i + 1; j < spans.length; j++) {
				if (spans[i][0] < spans[j][1] && spans[j][0] < spans[i][1]) return false;
			}
		}
		// The north blend must also lie north of the anchor (rule 7 keeps n >= 1 inside it).
		return spans[0][1] <= 0;
	}

	private static Geometry randomGeometry(SplittableRandom r) {
		int t = r.nextInt(4) == 0 ? r.nextInt(-2, 1030) : r.nextInt(0, 400);
		int scale = r.nextBoolean() ? 3000 : 1_000_010;
		int hs = r.nextInt(-2, scale), he = hs + r.nextInt(-3, Math.max(4, scale / 2)), sf = he + r.nextInt(-3, Math.max(4, scale / 2));
		return new Geometry(r.nextInt(-30_000_005, 30_000_006), r.nextInt(-30_000_005, 30_000_006), r.nextInt(-2, scale), hs, he, sf, t,
				r.nextInt());
	}

	/** Each rule's boundary, both sides, with even and odd T. */
	private static List<Geometry> boundaries() {
		List<Geometry> out = new ArrayList<>();
		for (int t : new int[]{0, 1, 255, 256, 257, 1023, 1024}) {
			int sf = 2 * 5500 + 3 * t;
			out.add(new Geometry(0, 0, t, 2000, 4000, sf, t, 0));
			out.add(new Geometry(0, 0, t + 1, 2000, 4000, sf, t, 0));
			out.add(new Geometry(0, 0, 2500, t / 2, 4000, sf, t, 0));
			out.add(new Geometry(0, 0, 2500, t / 2 + 1, 4000, sf, t, 0));
			out.add(new Geometry(0, 0, 2500, 2000, 2000 + t, sf, t, 0));
			out.add(new Geometry(0, 0, 2500, 2000, 2000 + t - 1, sf, t, 0));
			int d = (3 * t + 1) / 2;
			out.add(new Geometry(0, 0, 2500, 2000, 4000, 4000 + d, t, 0));
			out.add(new Geometry(0, 0, 2500, 2000, 4000, 4000 + d - 1, t, 0));
		}
		for (int a : new int[]{29_999_999, 30_000_000, 30_000_001}) {
			out.add(Geometry.defaults(a, 0));
			out.add(Geometry.defaults(0, -a));
		}
		out.add(new Geometry(0, 0, 1, 1, 2, 3, 0, 0));
		out.add(new Geometry(0, 0, 0, 1, 2, 3, 0, 0));
		out.add(new Geometry(0, 0, 1_000_000, 2000, 4000, 1_000_000, 256, 0));
		out.add(new Geometry(0, 0, 1_000_001, 2000, 4000, 1_000_000, 256, 0));
		out.add(new Geometry(0, 0, 2500, 2000, 4000, 1_000_001, 256, 0));
		out.add(new Geometry(0, 0, 2500, 2000, 4000, 6000, 1025, 0));
		out.add(new Geometry(0, 0, 2500, 2000, 4000, 6000, -1, 0));
		return out;
	}

	/** Oracle vectors for the editor cross-check: profiles with the oracle's verdict, and zone sets at chosen s. */
	private static void writeVectors(Path file, SplittableRandom r, List<Geometry> profiles) throws IOException {
		StringBuilder json = new StringBuilder("{\"profiles\":[");
		List<Geometry> chosen = new ArrayList<>(profiles.subList(profiles.size() - boundaries().size(), profiles.size()));
		for (int i = 0; i < 3000; i++) chosen.add(profiles.get(r.nextInt(profiles.size() - boundaries().size())));
		for (int i = 0; i < chosen.size(); i++) {
			Geometry g = chosen.get(i);
			if (i > 0) json.append(',');
			json.append(String.format(Locale.ROOT, "{\"anchor\":[%d,%d],\"north_frigid\":%d,\"south_hot_start\":%d,\"south_hot_end\":%d,"
					+ "\"south_frigid\":%d,\"transition\":%d,\"salt\":%d,\"valid\":%b", g.anchorX(), g.anchorZ(), g.north(), g.hotStart(),
					g.hotEnd(), g.southFrigid(), g.transition(), g.salt(), g.valid()));
			if (g.valid()) {
				json.append(",\"zones\":[");
				boolean firstZone = true;
				for (long s : sampleOffsets(g)) {
					long z = g.anchorZ() + s;
					if (z < Integer.MIN_VALUE || z > Integer.MAX_VALUE) continue;
					json.append(firstZone ? "" : ",").append('[').append(s);
					for (String zone : g.allowed((int) z).stream().sorted().toList()) json.append(",\"").append(zone).append('"');
					json.append(']');
					firstZone = false;
				}
				json.append(']');
			}
			json.append('}');
		}
		json.append("]}\n");
		Files.writeString(file, json, StandardCharsets.UTF_8);
	}

	private static List<Long> sampleOffsets(Geometry g) {
		List<Long> out = new ArrayList<>();
		long t = g.transition();
		for (long h : new long[]{-g.north(), -(g.north() - t), g.hotStart(), g.hotEnd(), g.southFrigid() - t, g.southFrigid(), 0}) {
			for (long d = -3; d <= 3; d++) out.add(h + d);
			out.add(h - t / 2 - 1);
			out.add(h + t / 2 + 1);
		}
		return out;
	}

	private static void check(boolean pass, String message) {
		checks++;
		if (!pass) fail(message);
	}

	private static void fail(String message) {
		failures++;
		System.out.println("FAIL: " + message);
	}
}

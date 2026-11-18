package io.github.jimbozoomer.jugcraft.world.climate;

import java.util.List;

/**
 * Deterministic executable tests of the pure climate band logic: the eleven profile rules at each boundary, every
 * zone threshold, strict frigid limits (block-precise and through Minecraft's fuzzy biome zoom), the inner-side and
 * centred blends, their monotonicity, coherence and determinism, and the in-pool choice. No game, no Minecraft types.
 */
public final class ClimateBandsTests {
	private static int checks;
	private static final ClimateAnchor ORIGIN = new ClimateAnchor(0, 0);
	private static final ClimateGeometry DEFAULTS = ClimateGeometry.defaults(ORIGIN);

	private static void check(boolean yes, String why) {
		checks++;
		if (!yes) {
			throw new AssertionError(why);
		}
	}

	public static void main(String[] args) {
		defaults();
		rules();
		thresholds();
		anchorAndX();
		strictFrigid();
		strictThroughBiomeZoom();
		blendPlacement();
		oddTransitions();
		monotoneBlends();
		uniformNoise();
		blendTracksSmoothstep();
		coherentBlends();
		determinism();
		selection();
		System.out.println("Climate bands: " + checks + " assertions passed");
	}

	private static ClimateGeometry geometry(int anchorZ, int north, int hotStart, int hotEnd, int southFrigid, int transition) {
		return new ClimateGeometry(new ClimateAnchor(0, anchorZ), north, hotStart, hotEnd, southFrigid, transition, 0);
	}

	private static List<String> problems(long schema, String mode, long x, long z, long nf, long hs, long he, long sf, long t) {
		return ClimateRules.problems(schema, mode, x, z, nf, hs, he, sf, t);
	}

	private static List<String> problems(long nf, long hs, long he, long sf, long t) {
		return problems(1, "fixed", 0, 0, nf, hs, he, sf, t);
	}

	/** Only the named rule (by its number) fails. */
	private static void only(List<String> problems, int rule, String why) {
		check(!problems.isEmpty(), why + ": expected rule " + rule + " to fail, but the profile passed");
		for (String p : problems) {
			check(p.startsWith("rule " + rule + ":"), why + ": expected only rule " + rule + ", got " + problems);
		}
	}

	private static void passes(List<String> problems, String why) {
		check(problems.isEmpty(), why + ": " + problems);
	}

	private static void defaults() {
		check(DEFAULTS.northFrigid() == 2500 && DEFAULTS.southHotStart() == 2000 && DEFAULTS.southHotEnd() == 4000
				&& DEFAULTS.southFrigid() == 5500 && DEFAULTS.transition() == 256 && DEFAULTS.salt() == 0, "The owner's defaults");
		passes(problems(2500, 2000, 4000, 5500, 256), "The defaults pass all eleven rules");
		check(ORIGIN.mode().equals("fixed"), "The only anchor mode is fixed");
	}

	private static void rules() {
		// 1: schema
		only(problems(0, "fixed", 0, 0, 2500, 2000, 4000, 5500, 256), 1, "schema 0");
		only(problems(2, "fixed", 0, 0, 2500, 2000, 4000, 5500, 256), 1, "schema 2");
		// 2: mode
		only(problems(1, "climate_spawn", 0, 0, 2500, 2000, 4000, 5500, 256), 2, "climate_spawn was removed");
		only(problems(1, "FIXED", 0, 0, 2500, 2000, 4000, 5500, 256), 2, "mode is case-sensitive");
		only(problems(1, null, 0, 0, 2500, 2000, 4000, 5500, 256), 2, "mode is required");
		// 3: anchor
		passes(problems(1, "fixed", 30_000_000, -30_000_000, 2500, 2000, 4000, 5500, 256), "anchor at +/-30,000,000");
		only(problems(1, "fixed", 30_000_001, 0, 2500, 2000, 4000, 5500, 256), 3, "anchor x beyond");
		only(problems(1, "fixed", 0, -30_000_001, 2500, 2000, 4000, 5500, 256), 3, "anchor z beyond");
		boolean refused = false;
		try {
			new ClimateAnchor(30_000_001, 0);
		} catch (IllegalArgumentException expected) {
			refused = true;
		}
		check(refused, "An anchor record beyond the limit is refused");
		// 4: distances in [1, 1,000,000]
		passes(problems(1, 1, 2, 3, 0), "the smallest thresholds, T = 0");
		passes(problems(1_000_000, 999_998, 999_999, 1_000_000, 0), "the largest thresholds");
		check(problems(0, 2000, 4000, 5500, 0).stream().allMatch(p -> p.startsWith("rule 4:") || p.startsWith("rule 7:"))
				&& problems(0, 2000, 4000, 5500, 0).stream().anyMatch(p -> p.startsWith("rule 4:")), "north_frigid 0 (also breaks rule 7)");
		only(problems(1_000_001, 2000, 4000, 5500, 256), 4, "north_frigid beyond");
		only(problems(2500, 1_000_001, 1_000_002, 1_000_003, 0), 4, "south thresholds beyond");
		check(problems(2500, 0, 4000, 5500, 0).stream().anyMatch(p -> p.startsWith("rule 4:")), "south_hot_start 0");
		// 5: T in [0, 1024]
		passes(problems(2500, 2000, 4000, 5500, 0), "T = 0");
		passes(problems(2500, 2000, 4000, 5600, 1024), "T = 1024");
		check(problems(2500, 2000, 4000, 5500, -1).stream().anyMatch(p -> p.startsWith("rule 5:")), "T = -1");
		only(problems(10_000, 2000, 4000, 7000, 1025), 5, "T = 1025");
		// 6: any 32-bit salt; default 0
		check(new ClimateGeometry(ORIGIN, 2500, 2000, 4000, 5500, 256, Integer.MIN_VALUE).salt() == Integer.MIN_VALUE
				&& DEFAULTS.withSalt(Integer.MAX_VALUE).salt() == Integer.MAX_VALUE && ClimateRules.DEFAULT_SALT == 0, "salt is any int");
		// 7: north_frigid > T
		passes(problems(257, 2000, 4000, 5500, 256), "north_frigid = T + 1");
		only(problems(256, 2000, 4000, 5500, 256), 7, "north_frigid = T");
		// 8: hot_start < hot_end < frigid
		only(problems(2500, 4000, 4000, 5500, 0), 8, "hot_start = hot_end");
		only(problems(2500, 2000, 5500, 5500, 0), 8, "hot_end = frigid");
		check(problems(2500, 4000, 2000, 5500, 0).stream().anyMatch(p -> p.startsWith("rule 8:")), "hot_start > hot_end");
		// 9: 2*hot_start > T
		passes(problems(2500, 129, 4000, 5500, 257), "2*hot_start = T + 1 (odd T)");
		only(problems(2500, 128, 4000, 5500, 256), 9, "2*hot_start = T");
		// 10: hot_end - hot_start >= T (touching blends allowed)
		passes(problems(2500, 2000, 2256, 5500, 256), "hot_end - hot_start = T");
		only(problems(2500, 2000, 2255, 5500, 256), 10, "hot_end - hot_start = T - 1");
		// 11: 2*(frigid - hot_end) >= 3*T
		passes(problems(2500, 2000, 4000, 4384, 256), "2*(frigid - hot_end) = 3*T");
		only(problems(2500, 2000, 4000, 4383, 256), 11, "2*(frigid - hot_end) = 3*T - 2");
		passes(problems(2500, 2000, 4000, 4002, 1), "odd T: 2*2 >= 3");
		only(problems(2500, 2000, 4000, 4001, 1), 11, "odd T: 2*1 < 3");
		// Several faults are all reported; huge values cannot overflow.
		check(problems(0, "x", 40_000_000, 0, 0, 0, 0, 0, 2000).size() >= 6, "Every broken rule is reported");
		check(!problems(Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE).isEmpty(),
				"Extreme ints are refused, not wrapped round");
		refused = false;
		try {
			geometry(0, 256, 2000, 4000, 5500, 256);
		} catch (IllegalArgumentException expected) {
			refused = expected.getMessage().contains("rule 7");
		}
		check(refused, "An invalid geometry cannot be constructed");
	}

	private static ClimateZone at(ClimateGeometry g, long s) {
		return ClimateBands.zone(g, 0, (int) (g.anchor().z() + s));
	}

	private static void thresholds() {
		ClimateGeometry hard = geometry(0, 2500, 2000, 4000, 5500, 0);
		check(at(hard, -2499) == ClimateZone.CENTRAL && at(hard, -2500) == ClimateZone.FRIGID, "T = 0: north limit at n = 2500");
		check(at(hard, 0) == ClimateZone.CENTRAL && at(hard, 1999) == ClimateZone.CENTRAL, "T = 0: central to s = 1999");
		check(at(hard, 2000) == ClimateZone.HOT && at(hard, 3999) == ClimateZone.HOT, "T = 0: hot from 2000 to 3999");
		check(at(hard, 4000) == ClimateZone.TEMPERATE && at(hard, 5499) == ClimateZone.TEMPERATE, "T = 0: temperate from 4000 to 5499");
		check(at(hard, 5500) == ClimateZone.FRIGID, "T = 0: south frigid from 5500");
		// With the default T: pure zones just outside each blend.
		check(at(DEFAULTS, -(2500 - 256) + 1) == ClimateZone.CENTRAL, "n = 2243 is central, before the north blend");
		check(at(DEFAULTS, 2000 - 128 - 1) == ClimateZone.CENTRAL, "s = 1871 is central, before the hot blend");
		check(at(DEFAULTS, 2000 + 128) == ClimateZone.HOT, "s = 2128 is hot, after the hot blend");
		check(at(DEFAULTS, 4000 - 128 - 1) == ClimateZone.HOT, "s = 3871 is hot, before the hot-end blend");
		check(at(DEFAULTS, 4000 + 128) == ClimateZone.TEMPERATE, "s = 4128 is temperate, after the hot-end blend");
		check(at(DEFAULTS, 5500 - 256 - 1) == ClimateZone.TEMPERATE, "s = 5243 is temperate, before the south blend");
		check(at(DEFAULTS, 5500) == ClimateZone.FRIGID && at(DEFAULTS, -2500) == ClimateZone.FRIGID, "Both limits are frigid");
		for (long s = -2243; s < 1872; s += 7) {
			check(at(DEFAULTS, s) == ClimateZone.CENTRAL, "Central between the north blend and the hot blend at s " + s);
		}
	}

	private static void anchorAndX() {
		ClimateGeometry moved = geometry(10_000, 2500, 2000, 4000, 5500, 0);
		check(ClimateBands.zone(moved, 0, 0) == ClimateZone.FRIGID, "Measured from the anchor: z 0 is 10,000 north of it");
		check(ClimateBands.zone(moved, 0, 10_000) == ClimateZone.CENTRAL, "The anchor itself is central");
		check(ClimateBands.zone(moved, 0, 12_000) == ClimateZone.HOT, "2,000 south of the anchor is hot");
		ClimateGeometry far = new ClimateGeometry(new ClimateAnchor(-30_000_000, 30_000_000), 2500, 2000, 4000, 5500, 256, 0);
		check(ClimateBands.zone(far, 29_999_999, 30_000_000 - 2600) == ClimateZone.FRIGID, "No overflow near the world border");
		for (int x : new int[]{Integer.MIN_VALUE / 8, -30_000_000, -1, 0, 1, 777, 30_000_000}) {
			check(ClimateBands.zone(DEFAULTS, x, 3000) == ClimateZone.HOT && ClimateBands.zone(DEFAULTS, x, 1000) == ClimateZone.CENTRAL
					&& ClimateBands.zone(DEFAULTS, x, 4800) == ClimateZone.TEMPERATE, "Pure belts run endlessly east and west, x " + x);
		}
	}

	private static void strictFrigid() {
		for (int t : new int[]{0, 1, 3, 64, 256, 1024}) {
			ClimateGeometry g = geometry(-3, 2500, 2000, 4000, 4001 + 2 * t, t);
			int north = g.anchor().z() - g.northFrigid(), south = g.anchor().z() + g.southFrigid();
			for (int x = -50_000; x <= 50_000; x += 997) {
				for (int d = 0; d < 1200; d += 3) {
					check(ClimateBands.zone(g, x, north - d) == ClimateZone.FRIGID, "North limit strict at x " + x + " d " + d + " T " + t);
					check(ClimateBands.zone(g, x, south + d) == ClimateZone.FRIGID, "South limit strict at x " + x + " d " + d + " T " + t);
				}
				check(ClimateBands.zone(g, x, north + t + 1) != ClimateZone.FRIGID, "No frigid before the north blend, T " + t);
				check(ClimateBands.zone(g, x, south - t - 1) != ClimateZone.FRIGID, "No frigid before the south blend, T " + t);
				for (int z = north + 1; z <= south - 1; z += 13) {
					ClimateZone zone = ClimateBands.zone(g, x, z);
					boolean inBlend = z > north && z <= north + t || z >= south - t && z < south;
					check(zone != ClimateZone.FRIGID || inBlend, "Frigid only inside the inner blends at z " + z + " T " + t);
				}
			}
		}
		for (long far : new long[]{1_000_000, 10_000_000, 30_000_000}) {
			check(at(DEFAULTS, far) == ClimateZone.FRIGID && at(DEFAULTS, -far) == ClimateZone.FRIGID, "Frigid all the way out, " + far);
		}
	}

	/** Block z reads quart (z-2)>>2 or the next (BiomeManager): every quart a block at or beyond a limit reads is frigid. */
	private static void strictThroughBiomeZoom() {
		for (int az = -9; az <= 9; az++) {
			for (int t : new int[]{0, 5, 256}) {
				ClimateGeometry g = geometry(az, 2500, 2000, 4000, 5500, t);
				int north = az - 2500, south = az + 5500;
				for (int x = -4000; x <= 4000; x += 401) {
					for (int z = south; z < south + 40; z++) {
						int q = (z - 2) >> 2;
						check(ClimateBands.quartZone(g, x >> 2, q) == ClimateZone.FRIGID && ClimateBands.quartZone(g, x >> 2, q + 1) == ClimateZone.FRIGID,
								"Every quart block z " + z + " can read is frigid (south, anchor " + az + ", T " + t + ")");
					}
					for (int z = north; z > north - 40; z--) {
						int q = (z - 2) >> 2;
						check(ClimateBands.quartZone(g, x >> 2, q) == ClimateZone.FRIGID && ClimateBands.quartZone(g, x >> 2, q + 1) == ClimateZone.FRIGID,
								"Every quart block z " + z + " can read is frigid (north, anchor " + az + ", T " + t + ")");
					}
					// Far from the limits a quart's zone is the zone of its corner block.
					for (int q = -400; q <= 1300; q += 37) {
						check(ClimateBands.quartZone(g, x >> 2, q) == ClimateBands.zone(g, (x >> 2) << 2, q << 2), "Quart zone is its corner's, q " + q);
					}
				}
			}
		}
	}

	private static void blendPlacement() {
		int t = 256;
		for (int x = -3000; x <= 3000; x += 61) {
			// Inner-side frigid blends: only central/frigid north, temperate/frigid south.
			for (long n = 2500 - t; n < 2500; n++) {
				ClimateZone zone = at(DEFAULTS, -n);
				check(zone == ClimateZone.CENTRAL || zone == ClimateZone.FRIGID, "North blend holds only central and frigid");
			}
			for (long s = 5500 - t; s < 5500; s++) {
				ClimateZone zone = ClimateBands.zone(DEFAULTS, x, (int) s);
				check(zone == ClimateZone.TEMPERATE || zone == ClimateZone.FRIGID, "South blend holds only temperate and frigid");
			}
			// Centred blends: [h - T/2, h + T/2), holding only the two neighbours.
			for (long s = 2000 - 128; s < 2000 + 128; s++) {
				ClimateZone zone = ClimateBands.zone(DEFAULTS, x, (int) s);
				check(zone == ClimateZone.CENTRAL || zone == ClimateZone.HOT, "Hot-start blend holds only central and hot");
			}
			for (long s = 4000 - 128; s < 4000 + 128; s++) {
				ClimateZone zone = ClimateBands.zone(DEFAULTS, x, (int) s);
				check(zone == ClimateZone.HOT || zone == ClimateZone.TEMPERATE, "Hot-end blend holds only hot and temperate");
			}
		}
		// The doubled-integer membership of contract amendment 4.
		check(ClimateBands.centred(2 * 1872L, 2000, 256) && !ClimateBands.centred(2 * 1871L, 2000, 256), "Centred blend starts at h - T/2");
		check(ClimateBands.centred(2 * 2127L, 2000, 256) && !ClimateBands.centred(2 * 2128L, 2000, 256), "Centred blend ends before h + T/2");
		check(!ClimateBands.centred(2 * 2000L, 2000, 0), "T = 0 has no centred blend");
		// Touching centred blends (hot_end - hot_start = T) never overlap.
		ClimateGeometry touching = geometry(0, 2500, 2000, 2256, 5500, 256);
		for (long s = 1872; s < 2384; s++) {
			boolean a = ClimateBands.centred(2 * s, 2000, 256), b = ClimateBands.centred(2 * s, 2256, 256);
			check(a != b, "Touching blends tile without overlap or gap at s " + s);
			ClimateZone zone = at(touching, s);
			check(a ? zone != ClimateZone.TEMPERATE : zone != ClimateZone.CENTRAL, "Each touching blend only mixes its own neighbours");
		}
		// The tightest valid hot-end to frigid spacing: the two blends are adjacent, not overlapping.
		ClimateGeometry tight = geometry(0, 2500, 2000, 4000, 4384, 256);
		check(!ClimateBands.centred(2 * (4384L - 256), 4000, 256) && ClimateBands.centred(2 * (4384L - 257), 4000, 256),
				"The hot-end blend ends exactly where the south frigid blend begins");
		check(at(tight, 4384) == ClimateZone.FRIGID, "Tight profile still strict at its limit");
	}

	private static void oddTransitions() {
		// T = 3, h = 10: 2s in [17, 23) means s = 9, 10, 11; three rows, centred on 10.
		ClimateGeometry g = geometry(0, 100, 10, 20, 100, 3);
		check(!ClimateBands.centred(16, 10, 3) && ClimateBands.centred(18, 10, 3) && ClimateBands.centred(22, 10, 3)
				&& !ClimateBands.centred(24, 10, 3), "Odd T: exactly s = 9, 10, 11");
		check(at(g, 8) == ClimateZone.CENTRAL && at(g, 12) == ClimateZone.HOT, "Odd T: pure on either side");
		check(at(g, -96) == ClimateZone.CENTRAL && at(g, -100) == ClimateZone.FRIGID, "Odd T: north blend n in [97, 100)");
		for (int x = -2000; x <= 2000; x++) {
			ClimateZone row = at(g, -97, x);
			check(row == ClimateZone.CENTRAL || row == ClimateZone.FRIGID, "Odd T: the north blend row mixes only central and frigid");
		}
		// T = 1, the smallest valid profile: blends are single rows (s = 1 for the hot start, s = 3 for the hot end).
		ClimateGeometry one = geometry(0, 2, 1, 3, 5, 1);
		check(at(one, 0) == ClimateZone.CENTRAL && at(one, 2) == ClimateZone.HOT && at(one, 5) == ClimateZone.FRIGID
				&& at(one, -2) == ClimateZone.FRIGID, "T = 1 smallest profile");
		check(at(one, 1) != ClimateZone.TEMPERATE && at(one, 3) != ClimateZone.CENTRAL && at(one, 4) != ClimateZone.HOT, "T = 1 single-row blends");
	}

	/** Share of the outer zone in a row of a blend, over many x. */
	private static double share(ClimateGeometry g, long s, ClimateZone outer) {
		int hits = 0, total = 0;
		for (int x = -40_000; x < 40_000; x += 19) {
			total++;
			if (at(g, s, x) == outer) {
				hits++;
			}
		}
		return hits / (double) total;
	}

	private static ClimateZone at(ClimateGeometry g, long s, int x) {
		return ClimateBands.zone(g, x, (int) (g.anchor().z() + s));
	}

	private static void monotoneBlends() {
		double last = -1;
		for (int i = 0; i <= 100; i++) {
			double p = ClimateBands.outerProbability(i / 100.0);
			check(p >= last && p >= 0 && p <= 1, "The outer chance never falls across a band");
			last = p;
		}
		check(ClimateBands.outerProbability(0) == 0 && ClimateBands.outerProbability(1) == 1, "0 at the inner edge, 1 at the outer");
		record Band(long from, long to, ClimateZone outer, int sign) {
		}
		for (Band band : List.of(new Band(1872, 2128, ClimateZone.HOT, 1), new Band(3872, 4128, ClimateZone.TEMPERATE, 1),
				new Band(5244, 5500, ClimateZone.FRIGID, 1), new Band(2244, 2500, ClimateZone.FRIGID, -1))) {
			double previous = -1;
			double first = -1, end = -1;
			for (long from = band.from(); from < band.to(); from += 32) {
				double sum = 0;
				for (long s = from; s < from + 32; s += 4) {
					sum += share(DEFAULTS, band.sign() * s, band.outer());
				}
				double slice = sum / 8;
				check(slice >= previous - 0.01, "The outer share rises across the band " + band + " at " + from + ": " + slice + " < " + previous);
				previous = slice;
				if (first < 0) {
					first = slice;
				}
				end = slice;
			}
			check(first < 0.05 && end > 0.95, "The band starts almost all inner and ends almost all outer: " + band + " " + first + " " + end);
			check(share(DEFAULTS, band.sign() * band.from(), band.outer()) < 0.01, "Its first row is nearly all inner: " + band);
		}
	}

	/** The blend noise is uniform on [0, 1): each tenth of its range is a tenth of the land (salts not used to build the table). */
	private static void uniformNoise() {
		int[] tenths = new int[10];
		int total = 0;
		for (int salt : new int[]{99, -31337}) {
			for (int edge = 0; edge < 4; edge++) {
				for (int x = -300_000; x < 300_000; x += 601) {
					for (int z = 5_000; z < 400_000; z += 997) {
						double v = ClimateNoise.blend(salt, edge, x, z);
						check(v >= 0 && v < 1, "Blend noise lies in [0, 1)");
						tenths[(int) (v * 10)]++;
						total++;
					}
				}
			}
		}
		for (int i = 0; i < 10; i++) {
			double share = tenths[i] / (double) total;
			check(Math.abs(share - 0.1) < 0.01, "Tenth " + i + " of the blend noise holds " + share + " of the land, not 0.1");
		}
		check(ClimateNoise.uniform(-1) == 0 && ClimateNoise.uniform(0) == 0 && ClimateNoise.uniform(2) < 1
				&& Math.abs(ClimateNoise.uniform(0.499316) - 0.5) < 1e-9, "The quantile map's ends and middle");
		double last = -1;
		for (double raw = 0; raw <= 1; raw += 0.0005) {
			double u = ClimateNoise.uniform(raw);
			check(u >= last, "The quantile map never falls");
			last = u;
		}
	}

	/** In each tenth of every blend, the outer zone's measured share is the smoothstep's mean there, within 0.05. */
	private static void blendTracksSmoothstep() {
		record Band(long from, int sign, ClimateZone outer) {
		}
		for (int t : new int[]{256, 100, 1024}) {
			ClimateGeometry g = geometry(0, 2500, 2000, 4000, 4000 + 2 * t + 100, t);
			List<Band> bands = List.of(new Band(2000 - t / 2, 1, ClimateZone.HOT), new Band(4000 - t / 2, 1, ClimateZone.TEMPERATE),
					new Band(g.southFrigid() - t, 1, ClimateZone.FRIGID), new Band(2500 - t, -1, ClimateZone.FRIGID));
			for (Band band : bands) {
				StringBuilder measured = new StringBuilder();
				for (int tenth = 0; tenth < 10; tenth++) {
					double expected = 0, got = 0;
					int rows = 0;
					for (long i = tenth * t / 10; i < (tenth + 1) * t / 10; i++) {
						long s = band.sign() * (band.from() + i);
						double fraction = band.outer() == ClimateZone.FRIGID ? (i + 0.5) / t : (2 * i + 1) / (2.0 * t);
						expected += ClimateBands.outerProbability(fraction);
						got += share(g, s, band.outer());
						rows++;
					}
					expected /= rows;
					got /= rows;
					measured.append(String.format("%.2f/%.2f ", got, expected));
					check(Math.abs(got - expected) <= 0.05, "T " + t + " band " + band + " tenth " + tenth + ": outer share " + got
							+ " should follow the smoothstep mean " + expected + " (" + measured + ")");
				}
				if (t == 256) {
					System.out.println("  T 256, " + band.outer() + " blend from " + band.sign() * band.from() + ", measured/expected by tenth: " + measured);
				}
			}
		}
	}

	private static void coherentBlends() {
		// In the middle of a blend, neighbouring blocks rarely differ: patches, not salt and pepper.
		long[] middles = {2000, 4000, 5372, -2372};
		for (long s : middles) {
			int changes = 0, steps = 0;
			ClimateZone previous = null;
			for (int x = -20_000; x < 20_000; x++) {
				ClimateZone zone = at(DEFAULTS, s, x);
				if (previous != null) {
					steps++;
					if (zone != previous) {
						changes++;
					}
				}
				previous = zone;
			}
			check(changes > 50, "The blend at s " + s + " mixes both zones (" + changes + " changes)");
			check(changes < steps / 12, "The blend at s " + s + " is coherent: " + changes + " changes in " + steps + " steps");
		}
	}

	private static void determinism() {
		ClimateGeometry salted = DEFAULTS.withSalt(12345);
		int differs = 0;
		for (int x = -5000; x <= 5000; x += 37) {
			for (long s = -2600; s <= 5600; s += 11) {
				ClimateZone a = at(DEFAULTS, s, x), b = at(DEFAULTS, s, x), c = at(ClimateGeometry.defaults(ORIGIN), s, x);
				check(a == b && b == c, "Same place, same answer");
				ClimateZone d = at(salted, s, x);
				if (d != a) {
					differs++;
					boolean blended = Math.abs(s - 2000) <= 128 || Math.abs(s - 4000) <= 128 || s >= 5244 && s < 5500 || s <= -2244 && s > -2500;
					check(blended, "Salt changes only blend patches, never pure belts (s " + s + ")");
				}
			}
		}
		check(differs > 0, "A different salt gives a different blend pattern");
		// Order independence: answering in reverse order gives the same answers.
		ClimateZone[] forward = new ClimateZone[2000];
		for (int i = 0; i < forward.length; i++) {
			forward[i] = at(DEFAULTS, 5244 + i % 256, i * 31);
		}
		for (int i = forward.length - 1; i >= 0; i--) {
			check(at(DEFAULTS, 5244 + i % 256, i * 31) == forward[i], "No dependence on the order of lookups");
		}
	}

	/** A humidity whose rank is the given fraction (inverse of {@link ClimateSelection#humidityRank}). */
	private static double inverseRank(double rank) {
		double lo = -1, hi = 1;
		for (int i = 0; i < 60; i++) {
			double mid = (lo + hi) / 2;
			if (ClimateSelection.humidityRank(mid) < rank) {
				lo = mid;
			} else {
				hi = mid;
			}
		}
		return (lo + hi) / 2;
	}

	private static void selection() {
		double last = -1;
		for (double h = -1.5; h <= 1.5; h += 0.001) {
			double rank = ClimateSelection.humidityRank(h);
			check(rank >= last && rank >= 0 && rank < 1, "Humidity rank is monotone in [0, 1)");
			last = rank;
		}
		check(ClimateSelection.humidityRank(Double.NaN) == ClimateSelection.humidityRank(0), "NaN humidity reads as neutral");
		check(Math.abs(ClimateSelection.humidityRank(-0.35) - 0.2) < 1e-9 && Math.abs(ClimateSelection.humidityRank(0.3) - 0.8) < 1e-9,
				"Vanilla's humidity bands each take a fifth");
		for (int size = 1; size <= 90; size++) {
			boolean[] reached = new boolean[size];
			int groups = ClimateSelection.groups(size);
			for (int g = 0; g < groups; g++) {
				double h = inverseRank((g + 0.5) / groups);
				check(ClimateSelection.group(size, h) == g, "A humidity in the middle of group " + g + " picks that group");
				for (int base = 0; base < 1000; base++) {
					reached[ClimateSelection.index(size, h, base * 0x9E3779B9, 3, 1, 4)] = true;
				}
			}
			for (int i = 0; i < size; i++) {
				check(reached[i], "Every member of a " + size + "-member pool can be chosen: " + i);
			}
			int lastGroup = -1;
			for (double h = -1; h < 1; h += 0.01) {
				int g = ClimateSelection.group(size, h);
				check(g >= lastGroup && g < ClimateSelection.groups(size), "Groups follow humidity, dry to wet");
				lastGroup = g;
				for (int base = 0; base < 64; base++) {
					int i = ClimateSelection.index(size, h, ("minecraft:biome_" + base).hashCode(), 7, 2, 0);
					check(i >= 0 && i < size, "The choice is a member");
					check(i >= ClimateSelection.start(size, g) && i < ClimateSelection.start(size, g + 1), "The choice is in its humidity group");
					check(i == ClimateSelection.index(size, h, ("minecraft:biome_" + base).hashCode(), 7, 2, 0), "The choice is deterministic");
				}
			}
			check(ClimateSelection.start(size, 0) == 0 && ClimateSelection.start(size, ClimateSelection.groups(size)) == size,
					"The groups cover the pool");
		}
		check(ClimateSelection.index(1, 0.9, 42, 0, 1, 1) == 0, "A one-member pool always gives that member");
		boolean refused = false;
		try {
			ClimateSelection.index(0, 0, 0, 0, 0, 0);
		} catch (IllegalArgumentException expected) {
			refused = true;
		}
		check(refused, "An empty pool is refused, never answered");
	}
}

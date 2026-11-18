package io.github.jimbozoomer.jugcraft.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * The plain arithmetic and output format of the opt-in benchmarks (docs/features/server-load-benchmark.md): the
 * server's workload benchmark ({@link WorkloadBenchmark}) and the client's frame benchmark
 * ({@link FrameBenchmarkClientGameTests}). It names no Minecraft type, so it can be compiled and checked on its own.
 * <p>
 * Percentiles are nearest-rank, the same as the Concordance load cards' (ConcordanceWorkshopLoadGameTests.rank): the
 * smallest sample with at least that share of all samples at or below it.
 */
public final class BenchmarkStats {
	/** A whole server tick, the budget a tick's work has before the server falls behind. */
	public static final long TICK_NANOS = 50_000_000L;

	private BenchmarkStats() {
	}

	/** One sampled span: how many samples, and their mean, median, 95th percentile and maximum in milliseconds. */
	public record Summary(int count, double meanMs, double p50Ms, double p95Ms, double maxMs, int over) {
	}

	/** The summary of {@code nanos}; {@code over} counts the samples longer than {@code overNanos}. */
	public static Summary of(long[] nanos, long overNanos) {
		if (nanos.length == 0) {
			return new Summary(0, 0, 0, 0, 0, 0);
		}
		long[] sorted = nanos.clone();
		Arrays.sort(sorted);
		double sum = 0;
		int over = 0;
		for (long sample : nanos) {
			sum += sample;
			if (sample > overNanos) {
				over++;
			}
		}
		return new Summary(nanos.length, ms(sum / nanos.length), ms(sorted[rank(sorted.length, 0.50)]),
				ms(sorted[rank(sorted.length, 0.95)]), ms(sorted[sorted.length - 1]), over);
	}

	/** Nearest rank: the index in a sorted run of {@code n} of the smallest value with at least {@code q} of all at or below it. */
	public static int rank(int n, double q) {
		if (n <= 0) {
			throw new IllegalArgumentException("No samples");
		}
		return Math.min(n - 1, Math.max(0, (int) Math.ceil(q * n) - 1));
	}

	/** How far apart two measurements are, as a share of the larger (0 when both are 0). */
	public static double apart(double a, double b) {
		double larger = Math.max(Math.abs(a), Math.abs(b));
		return larger == 0 ? 0 : Math.abs(a - b) / larger;
	}

	public static double ms(double nanos) {
		return nanos / 1_000_000.0;
	}

	public static double mib(long bytes) {
		return bytes / (1024.0 * 1024.0);
	}

	/** Three decimal places, the precision every figure is written with. */
	public static double round(double value) {
		return Math.round(value * 1000.0) / 1000.0;
	}

	/**
	 * One JSON object on one line, its fields in the map's order: numbers (a non-finite one is written null), strings,
	 * booleans, null, nested maps and collections of those.
	 */
	public static String json(Map<String, ?> fields) {
		StringBuilder out = new StringBuilder();
		write(out, fields);
		return out.toString();
	}

	private static void write(StringBuilder out, Object value) {
		if (value == null) {
			out.append("null");
		} else if (value instanceof Map<?, ?> map) {
			out.append('{');
			boolean first = true;
			for (Map.Entry<?, ?> entry : map.entrySet()) {
				if (!first) {
					out.append(',');
				}
				first = false;
				string(out, String.valueOf(entry.getKey()));
				out.append(':');
				write(out, entry.getValue());
			}
			out.append('}');
		} else if (value instanceof Collection<?> list) {
			out.append('[');
			boolean first = true;
			for (Object item : list) {
				if (!first) {
					out.append(',');
				}
				first = false;
				write(out, item);
			}
			out.append(']');
		} else if (value instanceof Double || value instanceof Float) {
			double number = ((Number) value).doubleValue();
			out.append(Double.isFinite(number) ? String.format(Locale.ROOT, "%.3f", number) : "null");
		} else if (value instanceof Number || value instanceof Boolean) {
			out.append(value);
		} else {
			string(out, value.toString());
		}
	}

	private static void string(StringBuilder out, String text) {
		out.append('"');
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			switch (c) {
				case '"' -> out.append("\\\"");
				case '\\' -> out.append("\\\\");
				case '\n' -> out.append("\\n");
				case '\r' -> out.append("\\r");
				case '\t' -> out.append("\\t");
				default -> {
					if (c < 0x20) {
						out.append(String.format(Locale.ROOT, "\\u%04x", (int) c));
					} else {
						out.append(c);
					}
				}
			}
		}
		out.append('"');
	}

	/**
	 * Checks the arithmetic on runs whose answers are known; returns what is wrong (empty when all is right). The
	 * benchmarks call it before measuring, and ServerLoadGameTests.benchmarkArithmetic asserts it in every run.
	 */
	public static List<String> check() {
		List<String> wrong = new ArrayList<>();
		long[] hundred = new long[100];
		for (int i = 0; i < hundred.length; i++) {
			hundred[i] = (100 - i) * 1_000_000L;
		}
		Summary s = of(hundred, 90_000_000L);
		expect(wrong, "count", s.count(), 100);
		expect(wrong, "mean", s.meanMs(), 50.5);
		expect(wrong, "median", s.p50Ms(), 50.0);
		expect(wrong, "p95", s.p95Ms(), 95.0);
		expect(wrong, "max", s.maxMs(), 100.0);
		expect(wrong, "over", s.over(), 10);
		expect(wrong, "rank of one", rank(1, 0.95), 0);
		expect(wrong, "rank of twenty", rank(20, 0.95), 18);
		Summary one = of(new long[] {7_000_000L}, TICK_NANOS);
		expect(wrong, "single p95", one.p95Ms(), 7.0);
		expect(wrong, "empty", of(new long[0], TICK_NANOS).count(), 0);
		expect(wrong, "apart", apart(1.0, 1.25), 0.2);
		expect(wrong, "apart of nothing", apart(0, 0), 0);
		String json = json(new java.util.LinkedHashMap<>(Map.of("a", 1)));
		if (!json.equals("{\"a\":1}")) {
			wrong.add("json " + json);
		}
		String nested = json(java.util.Collections.singletonMap("k", List.of("x\"y", 1.5, Double.NaN, true)));
		if (!nested.equals("{\"k\":[\"x\\\"y\",1.500,null,true]}")) {
			wrong.add("nested json " + nested);
		}
		return wrong;
	}

	private static void expect(List<String> wrong, String what, double got, double want) {
		if (Math.abs(got - want) > 1e-9) {
			wrong.add(what + ": " + got + ", not " + want);
		}
	}
}

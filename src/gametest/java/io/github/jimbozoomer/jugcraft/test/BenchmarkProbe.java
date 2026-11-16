package io.github.jimbozoomer.jugcraft.test;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * What the opt-in benchmarks read from the JVM (docs/features/server-load-benchmark.md): the heap in use from
 * MemoryMXBean, collections, a thread's allocated bytes from {@code com.sun.management.ThreadMXBean} where the JVM
 * has it, and the environment a figure was measured in. It names no Minecraft type.
 */
public final class BenchmarkProbe {
	private static final MemoryMXBean MEMORY = ManagementFactory.getMemoryMXBean();
	private static final String ALLOCATION;
	/** Null when the JVM cannot measure a thread's allocation. */
	private static final com.sun.management.ThreadMXBean THREADS;

	static {
		ThreadMXBean threads = ManagementFactory.getThreadMXBean();
		com.sun.management.ThreadMXBean found = null;
		String why;
		if (threads instanceof com.sun.management.ThreadMXBean sun) {
			if (!sun.isThreadAllocatedMemorySupported()) {
				why = "unavailable: this JVM does not measure per-thread allocation";
			} else {
				if (!sun.isThreadAllocatedMemoryEnabled()) {
					try {
						sun.setThreadAllocatedMemoryEnabled(true);
					} catch (UnsupportedOperationException | SecurityException e) {
						// Reported below as unavailable.
					}
				}
				if (sun.isThreadAllocatedMemoryEnabled()) {
					found = sun;
					why = "com.sun.management.ThreadMXBean.getThreadAllocatedBytes";
				} else {
					why = "unavailable: per-thread allocation measurement is switched off and could not be switched on";
				}
			}
		} else {
			why = "unavailable: this JVM has no com.sun.management.ThreadMXBean";
		}
		THREADS = found;
		ALLOCATION = why;
	}

	private BenchmarkProbe() {
	}

	/** Whether {@link #allocated} measures anything. */
	public static boolean allocationAvailable() {
		return THREADS != null;
	}

	/** How allocation is measured here, or why it is not. */
	public static String allocationSource() {
		return ALLOCATION;
	}

	/** The bytes the thread with this id has allocated so far, or -1 when the JVM cannot say. */
	public static long allocated(long threadId) {
		if (THREADS == null) {
			return -1L;
		}
		long bytes = THREADS.getThreadAllocatedBytes(threadId);
		return bytes < 0 ? -1L : bytes;
	}

	/** The bytes the current thread has allocated so far, or -1 when the JVM cannot say. */
	public static long allocatedHere() {
		return allocated(Thread.currentThread().threadId());
	}

	/** The heap in use now, as MemoryMXBean reports it (no collection first). */
	public static long heapUsed() {
		return MEMORY.getHeapMemoryUsage().getUsed();
	}

	/** The heap in use just after a full collection is asked for; -1 if the JVM ignored the request (-XX:+DisableExplicitGC). */
	public static long heapRetained() {
		long before = collections();
		System.gc();
		return collections() > before ? MEMORY.getHeapMemoryUsage().getUsed() : -1L;
	}

	public static long collections() {
		long n = 0;
		for (GarbageCollectorMXBean bean : ManagementFactory.getGarbageCollectorMXBeans()) {
			n += Math.max(0L, bean.getCollectionCount());
		}
		return n;
	}

	public static long collectionMillis() {
		long n = 0;
		for (GarbageCollectorMXBean bean : ManagementFactory.getGarbageCollectorMXBeans()) {
			n += Math.max(0L, bean.getCollectionTime());
		}
		return n;
	}

	/** The JVM, heap limit, processors, operating system and collectors this run had: the environment a figure belongs to. */
	public static Map<String, Object> environment() {
		Map<String, Object> out = new LinkedHashMap<>();
		Runtime runtime = Runtime.getRuntime();
		out.put("java", System.getProperty("java.runtime.version", System.getProperty("java.version", "?")));
		out.put("vm", System.getProperty("java.vm.name", "?") + " (" + System.getProperty("java.vm.vendor", "?") + ")");
		out.put("heapMaxMiB", runtime.maxMemory() == Long.MAX_VALUE ? -1L : runtime.maxMemory() >> 20);
		out.put("cores", runtime.availableProcessors());
		out.put("os", System.getProperty("os.name", "?") + " " + System.getProperty("os.arch", "?"));
		List<String> collectors = new ArrayList<>();
		for (GarbageCollectorMXBean bean : ManagementFactory.getGarbageCollectorMXBeans()) {
			collectors.add(bean.getName());
		}
		out.put("collectors", collectors);
		// Only the memory and collector flags: other arguments can carry local paths.
		List<String> flags = new ArrayList<>();
		for (String argument : ManagementFactory.getRuntimeMXBean().getInputArguments()) {
			String lower = argument.toLowerCase(Locale.ROOT);
			boolean local = lower.contains("path") || lower.contains("file") || lower.contains("/") || lower.contains("\\");
			if (!local && (lower.startsWith("-xm") || lower.startsWith("-xx:") && (lower.contains("gc") || lower.contains("heap")
					|| lower.contains("ram") || lower.contains("outofmemory")))) {
				flags.add(argument);
			}
		}
		out.put("memoryFlags", flags);
		out.put("allocation", ALLOCATION);
		return out;
	}
}

package io.github.jimbozoomer.jugcraft.test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.jimbozoomer.jugcraft.Jugcraft;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.loader.api.FabricLoader;

/**
 * Concordance garden cohort, GC-10 unit 7 (GC-P146): the second client JVM a two-client test starts, moved here from
 * ConcordancePactNetworkTests (Chunk D, D-09) so the pact and garden network tests share one copy. It is that class's own
 * code, with its names made parameters: a second, separate client JVM on the same classpath (the pattern of
 * OwnerCollectionsNetworkTests, #435, which keeps its own copy), its own temporary profile under owner-network-evidence/,
 * whose peer.log the Build workflow's client shards upload; the two JVMs coordinate through marker files in one directory.
 * Before the second JVM starts, the first runs a collection and logs its heap (CI's two-client timeouts came at the peer's
 * login with the first client's heap near its limit); a failure carries the heap too.
 */
final class SecondClient {
	private final String peerProperty;
	private final String coordinationProperty;
	private final String evidencePrefix;
	private final String peerName;
	private final Class<?> test;

	SecondClient(String peerProperty, String coordinationProperty, String evidencePrefix, String peerName, Class<?> test) {
		this.peerProperty = peerProperty;
		this.coordinationProperty = coordinationProperty;
		this.evidencePrefix = evidencePrefix;
		this.peerName = peerName;
		this.test = test;
	}

	/** Whether this JVM is the second client (its launch named the peer property). */
	boolean isPeer() {
		return System.getProperty(peerProperty) != null;
	}

	/** The server's port the second client joins (read in the second JVM). */
	int port() {
		return Integer.parseInt(System.getProperty(peerProperty));
	}

	/** The first JVM: a fresh coordination directory of its own under owner-network-evidence/. */
	void begin() throws IOException {
		System.setProperty(coordinationProperty, Path.of("owner-network-evidence", evidencePrefix + "-" + UUID.randomUUID()).toAbsolutePath().toString());
		Files.createDirectories(coordination());
	}

	Path coordination() {
		return Path.of(System.getProperty(coordinationProperty, "owner-network-evidence")).toAbsolutePath();
	}

	void mark(String name, String text) {
		try {
			Files.createDirectories(coordination());
			Files.writeString(coordination().resolve(name), text);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	boolean marked(String name) {
		return Files.exists(coordination().resolve(name));
	}

	String read(String name) {
		try {
			return Files.readString(coordination().resolve(name)).trim();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	/** {@code message}, the second client's last 120 log lines and this JVM's heap now. */
	AssertionError failure(String message) {
		ArrayDeque<String> tail = new ArrayDeque<>();
		try (Stream<String> lines = Files.lines(coordination().resolve("peer.log"))) {
			lines.forEach(line -> {
				if (tail.size() == 120) {
					tail.removeFirst();
				}
				tail.addLast(line);
			});
		} catch (IOException | UncheckedIOException e) {
			tail.addLast("The second client's log could not be read: " + e);
		}
		return new AssertionError(message + "\nThe first client's heap now: " + heap() + "\nThe second client's log, last lines:\n" + String.join("\n", tail));
	}

	/** Waits for the second client's mark {@code name}, failing at once if it has exited. */
	void awaitPeer(ClientGameTestContext context, Process peer, String name, int wait) {
		context.waitFor(client -> {
			if (marked(name)) {
				return true;
			}
			if (!peer.isAlive()) {
				throw failure("The second client exited (" + peer.exitValue() + ") before " + name);
			}
			return false;
		}, wait);
	}

	/** Waits for the first client's mark {@code name} (the second JVM's side). */
	void awaitMark(ClientGameTestContext context, String name, int wait) {
		context.waitFor(client -> marked(name), wait);
	}

	/** "used/max MiB" of this JVM's heap. */
	static String heap() {
		Runtime runtime = Runtime.getRuntime();
		long used = (runtime.totalMemory() - runtime.freeMemory()) >> 20;
		return used + "/" + (runtime.maxMemory() >> 20) + " MiB";
	}

	/**
	 * OwnerCollectionsNetworkTests.launchPeer (#435), with this test's names: a second client JVM on the same classpath,
	 * its manifest naming only {@code test} (and its PeerStartup as its client entrypoint), with {@code heap} as its -Xmx.
	 */
	Process launch(int port, String heap) throws IOException {
		System.gc();
		Jugcraft.LOGGER.info("[second-client] {} before the second client: heap {}", evidencePrefix, heap());
		Path profile = coordination().resolve("peer-profile");
		Files.createDirectories(profile);
		Files.writeString(profile.resolve("options.txt"), "preferredGraphicsBackend:\"opengl\"\nfullscreen:false\nlang:en_us\n");
		Path metadata = FabricLoader.getInstance().getModContainer("jugcraft-test").orElseThrow().findPath("fabric.mod.json").orElseThrow();
		Path resources = coordination().resolve("peer-test-resources");
		Files.createDirectories(resources);
		try (Stream<Path> paths = Files.walk(metadata.getParent())) {
			for (Path file : paths.filter(Files::isRegularFile).toList()) {
				Path target = resources.resolve(metadata.getParent().relativize(file));
				Files.createDirectories(target.getParent());
				Files.copy(file, target, StandardCopyOption.REPLACE_EXISTING);
			}
		}
		JsonObject json = JsonParser.parseString(Files.readString(metadata)).getAsJsonObject();
		JsonArray tests = new JsonArray();
		tests.add(test.getName());
		json.getAsJsonObject("entrypoints").add("fabric-client-gametest", tests);
		JsonArray client = new JsonArray();
		client.add(test.getName() + "$PeerStartup");
		json.getAsJsonObject("entrypoints").add("client", client);
		Files.writeString(resources.resolve("fabric.mod.json"), json.toString());
		String oldResources = metadata.getParent().toAbsolutePath().toString();
		Path config = Path.of(System.getProperty("user.dir")).toAbsolutePath();
		while (config != null && !Files.isRegularFile(config.resolve(".gradle/loom-cache/launch.cfg"))) {
			config = config.getParent();
		}
		if (config == null) {
			throw new IllegalStateException("Cannot locate this checkout's Loom launch.cfg");
		}
		List<String> launch = Files.readAllLines(config.resolve(".gradle/loom-cache/launch.cfg"));
		String assets = null;
		String assetIndex = null;
		for (int i = 0; i + 1 < launch.size(); i++) {
			if (launch.get(i).trim().equals("--assetsDir")) {
				assets = launch.get(i + 1).trim();
			}
			if (launch.get(i).trim().equals("--assetIndex")) {
				assetIndex = launch.get(i + 1).trim();
			}
		}
		if (assets == null || assetIndex == null) {
			throw new IllegalStateException("Loom's launch config lacks asset paths");
		}
		List<String> command = new ArrayList<>();
		command.add(Path.of(System.getProperty("java.home"), "bin", System.getProperty("os.name").startsWith("Windows") ? "java.exe" : "java").toString());
		command.add(heap);
		command.add("-XX:ActiveProcessorCount=4");
		command.add("-XX:ErrorFile=" + coordination().resolve("hs_err_peer.log"));
		for (Map.Entry<Object, Object> entry : System.getProperties().entrySet()) {
			String key = entry.getKey().toString();
			if (key.startsWith("fabric.") || key.startsWith("fabric-api.") || key.startsWith("org.lwjgl.") || key.equals("java.library.path")
					|| key.equals("log4j.configurationFile")) {
				command.add("-D" + key + "=" + entry.getValue().toString().replace(oldResources, resources.toString()));
			}
		}
		command.add("-D" + peerProperty + "=" + port);
		command.add("-D" + coordinationProperty + "=" + coordination());
		command.add("-cp");
		command.add(System.getProperty("java.class.path").replace(oldResources, resources.toString()));
		command.add("net.fabricmc.loader.impl.launch.knot.KnotClient");
		command.addAll(List.of("--username", peerName, "--version", "26.3", "--accessToken", "0", "--gameDir", profile.toString(),
				"--assetIndex", assetIndex, "--assetsDir", assets));
		Path arguments = coordination().resolve("peer-java.args");
		Files.write(arguments, command.subList(1, command.size()).stream().map(a -> "\"" + a.replace("\\", "\\\\").replace("\"", "\\\"") + "\"").toList());
		return new ProcessBuilder(command.getFirst(), "@" + arguments).directory(profile.toFile()).redirectErrorStream(true)
				.redirectOutput(coordination().resolve("peer.log").toFile()).start();
	}
}

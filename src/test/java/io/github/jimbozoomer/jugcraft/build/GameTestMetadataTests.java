package io.github.jimbozoomer.jugcraft.build;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import net.fabricmc.loader.impl.metadata.DependencyOverrides;
import net.fabricmc.loader.impl.metadata.EntrypointMetadata;
import net.fabricmc.loader.impl.metadata.LoaderModMetadata;
import net.fabricmc.loader.impl.metadata.ModMetadataParser;
import net.fabricmc.loader.impl.metadata.VersionOverrides;

/** Parse the full and selected test manifests with the actual pinned loader, before starting Minecraft. */
public final class GameTestMetadataTests {
	private GameTestMetadataTests() {
	}

	public static void main(String[] args) throws Exception {
		Path overrides = Files.createTempDirectory("jugcraft-metadata-test-");
		try {
			Path source = Path.of(args[0]);
			if (Files.size(source) <= 8192) {
				throw new AssertionError("Regression fixture must exercise the large entrypoint catalogue");
			}
			LoaderModMetadata full = parse(source, overrides);
			LoaderModMetadata selected = parse(Path.of(args[1]), overrides);
			if (!full.getEntrypointKeys().equals(selected.getEntrypointKeys())) {
				throw new AssertionError("Selection removed an entrypoint category");
			}
			for (String key : full.getEntrypointKeys()) {
				List<String> all = full.getEntrypoints(key).stream().map(EntrypointMetadata::getValue).toList();
				List<String> actual = selected.getEntrypoints(key).stream().map(EntrypointMetadata::getValue).toList();
				if (new HashSet<>(actual).size() != actual.size()) {
					throw new AssertionError("Duplicate selected entrypoints: " + key);
				}
				if (key.equals("fabric-client-gametest") ? !all.containsAll(actual) : !all.equals(actual)) {
					throw new AssertionError("Selection changed unrelated entrypoints: " + key);
				}
			}
			System.out.println("PASS: pinned Fabric loader parsed full and selected test manifests; server entrypoints preserved");
		} finally {
			Files.deleteIfExists(overrides);
		}
	}

	private static LoaderModMetadata parse(Path path, Path overrides) throws IOException, net.fabricmc.loader.impl.metadata.ParseMetadataException {
		try (var input = Files.newInputStream(path)) {
			LoaderModMetadata result = ModMetadataParser.parseMetadata(input, path.toString(), List.of(),
					new VersionOverrides(), new DependencyOverrides(overrides), false);
			if (result.getSchemaVersion() != 1 || !result.getId().equals("jugcraft-test")) {
				throw new AssertionError("Unexpected test mod metadata: " + path);
			}
			return result;
		}
	}
}

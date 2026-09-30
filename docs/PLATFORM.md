# Platform decision and bootstrap gate

## Selected platform

The owner has selected **Minecraft Java Edition 26.3 + Fabric** for Jugcraft. Build original systems from scratch; availability of third-party content mods does not determine this choice. Jugcraft remains one original mod with internal feature packages, using Fabric API where appropriate.

This is a recorded design decision, not a runnable build. Exact Fabric Loader/API versions, Java toolchain, Loom and Gradle wrapper/checksum must be verified together and pinned in the bootstrap PR. Do not use a floating `latest` version or assume Minecraft's version number is the Java/JDK version. Do not automatically upgrade the project when Minecraft releases a new version.

The owner authorizes this bootstrap now. A separate issue and second maintainer are not prerequisites. Create the missing build as part of this task.

## Required bootstrap PR

1. Generate a Minecraft 26.3 Fabric project from the official template, preserving required notices and provenance.
2. Pin and verify the full toolchain: Loader, Fabric API, JDK major, Loom, Gradle wrapper/version/checksum and any applicable naming/mapping configuration.
3. Add `jugcraft` metadata, separated client code and one minimal item/recipe. Keep original content independent of third-party gameplay mods.
4. Document tested Windows and Unix build, client launch, dedicated-server launch and game-test commands.
5. Replace the foundation-only source gate with actual compilation and relevant unit/game tests. Require those stable check names on main; do not weaken review or security requirements.
6. Run a dedicated server with two clients, test save/restart, and record exact versions and evidence.
7. Publish accurate installation requirements, preserve template notices, and record the pending license decision. Owner-directed original bootstrap may proceed; unrelated third-party contributions and general reusable releases need the license resolved.

After bootstrap, establish shared material, recipe, resource, progression and persistence interfaces before accepting disconnected large systems. Follow the owner-directed [design](DESIGN.md) and [specialties](CONTENT_BRANCHES.md).

Official references used for platform selection:
- https://www.minecraft.net/en-us/article/minecraft-java-edition-26-3
- https://www.fabricmc.net/2026/09/15/263.html
- https://docs.fabricmc.net/develop/getting-started/creating-a-project

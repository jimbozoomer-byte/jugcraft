# Platform decision and bootstrap gate

## Current pins (unverified)

The bootstrap scaffold targets **Minecraft Java Edition 26.3 + Fabric** with these pins in `gradle.properties`:

| Component | Pin | Source |
| --- | --- | --- |
| Minecraft | 26.3 | Owner decision (PR #1) |
| Java | 25 | Minecraft 26.1+ requirement |
| Gradle wrapper | 9.6.0, sha256 `bbaeb2fef8710818cf0e261201dab964c572f92b942812df0c3620d62a529a01` | Fabric 26.3 announcement; checksum computed from the downloaded distribution |
| Fabric Loom | 1.17 (`net.fabricmc.fabric-loom`, no mappings: 26.x is unobfuscated) | Fabric 26.3 announcement |
| Fabric API | 0.161.0+26.3 | Modrinth listing |
| Fabric Loader | 0.18.4 | Best guess; not confirmed |

These were gathered by web search on 30 September 2026 in an environment that could not reach Fabric or Mojang servers, so **nothing has been resolved or compiled yet**. The Build workflow is the first real check. Correct any pin it rejects before merging.

Status: pending dependency compatibility assessment. Minecraft Java Edition is the target. No game or loader versions are pinned yet; there is no Gradle build in this foundation.

The earlier Fabric suggestion was a starting option. The emphasis on tech/magic integrations means the first approved external mods must inform the loader and Minecraft version. Evaluate Fabric and NeoForge against those actual dependencies rather than promising compatibility in advance.

## Required bootstrap PR

1. List the initial required and optional external mods, official links, exact supported versions, and licenses. It is also valid to choose an original-content-only first milestone.
2. Record a decision here for one Minecraft version, loader and exact version, Java major, build plugin, mappings, and Gradle wrapper version/checksum.
3. Generate a project from that loader's official template; preserve its required notices and record provenance.
4. Add `jugcraft` mod metadata, separated client code, and one minimal registered item/recipe.
5. Document actual Windows and Unix build, client launch, server launch, and game-test commands after verifying them.
6. Add CI compilation and applicable unit/game tests; require their stable check names on main. Do not treat repository checks as a mod build.
7. Run a dedicated server with two clients, test save/restart, and record exact versions and evidence.
8. Select the project license before accepting implementation/assets. Publish client/server installation manifests when needed.

Only after this PR is reviewed and merged should feature implementation begin.

Official references:
- https://docs.fabricmc.net/develop/getting-started/creating-a-project
- https://docs.neoforged.net/docs/gettingstarted/

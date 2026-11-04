# Drill area mining follows the hit face

Owner-directed correction, November 3, 2026. Implemented with OpenAI Codex (GPT-6), extending [powered equipment](powered-equipment-rebalance.md) and its [Efficiency Module](tool-efficiency-module.md). Minecraft 26.3 and existing dependencies are retained.

## Player behavior

The Mining Drill and its Industrial, Diamond and Iridium variants choose the 3x3 or 5x5 mining plane from the face where the player starts breaking the centre block. Top/bottom faces give a horizontal layer; north/south faces an X/Y wall; east/west faces a Y/Z wall. The centre block is included in the nine/twenty-five total. Looking at a floor obliquely no longer produces a vertical cut. The old pitch threshold is removed from both execution and preview.

The starting face stays fixed for that block, including when the finishing packet reports another face. A new block receives its own starting face. The bounded cosmetic outline uses the same target geometry, the live hit face before mining, and the latched face during mining. Drill area previews refresh every client tick (at most 25 candidates), so switching faces does not wait for the other tools' ten-tick refresh.

This corrects an existing workshop mining tool; no recipe, research, resource, unlock or progression gate is added. Existing steel/electrical equipment acquisition, mine output consumers and module support remain applicable. Range still unlocks 5x5. Mode switching, sneaking for one block, vein mining, per-successful-block JE, Overclock and Efficiency remain as defined by the linked feature records. Protected blocks, harder neighbors, containers and unloaded terrain retain the existing exclusions.

## Input lifetime and failure behavior

Two small native game-mode mixins retain the starting direction because the tool's post-break callback runs after the centre block has disappeared. The server wraps the existing START/STOP/ABORT processing, tick and actual destroy call; it does not replace native reach, permissions, breaking time, drops or protection. Input belongs to each game-mode instance and matches the exact level, position, tool stack and mode. Actual break context is scoped with `try/finally`, consumed once, and cannot be inherited by recursive extra breaks or another player.

Abort, failed/inactive native mining, an observed tool/mode change, death, level change and disposal of the player/game-mode invalidate the input. The client rejects inactive or mismatched context and clears it on cancellation. Without a matching starting face, the server only breaks the original block. There are no new packets, permanent player data, item components, saved IDs, scans, chunk loads or migrations. Existing worlds and tools need no conversion.

The preview reuses the existing blueprint outline and its original texture. No art asset, UI framework, dependency or installation-pack change is introduced. No live server deployment/restart is part of this correction.

## Validation

Work is based on `235524e0c8` in the existing `codex/exosuit-tools-scanners` checkout, preserving the earlier owner-directed equipment changes. Tests use disposable generated worlds, the pinned Fabric platform and the local JDK 25 toolchain.

Verified November 3, 2026 on Minecraft 26.3, Fabric Loader 0.19.5, Fabric API 0.161.0+26.3, Loom 1.17.21 and JDK 25.0.4.1+1.

Six server regressions exercise all six faces for both sizes, changed look/STOP direction, actual native breaking time, absent/aborted/replaced input, tool-away/back and level reset, instant breaking, harder/container exclusions, exact discounted JE, insufficient charge and concurrent player input. Three previous area/JE tests now supply a native START face before their existing direct break assertions. Their counts/prices are retained.

The rendered client regression ray-picks all six faces at angles that defeat the old heuristic for both sizes, checks the preview plane, captures floor/wall screenshots, exercises repeated start and cancel, then mines a 5x5 wall with the real attack key and checks the server's 1,500 JE debit.

- Compilation: passed (`compileGametestJava`, 2m 48s; includes common/client Java).
- Server game tests: **3,334/3,334 passed**, `runGameTest`, 8m 12s, without a test/feature filter. The first run's four new fixture failures sent STOP before normal mining time; the corrected fixture advances native ticks before STOP, retaining the face, block-count and JE assertions. The complete rerun passed.
- Rendered client game tests: **passed**, `runClientGameTest -PclientTests=DrillFaceClientGameTests,PoweredEquipmentClientGameTests`, 5m 36s. All twelve face/size previews, native repeated-start/cancel, real mouse-driven 25-block mining, the synchronized 1,500 JE debit, and the existing scanner/large-area preview regression passed. Floor/wall screenshots were reviewed. The fixture waits for the updated preview plane and confirmed teleport position before acting, then awaits server charge synchronization before the final block/JE assertions; the initial two runs exposed missing synchronization in this new fixture.
- Repository structure/documentation links: passed (`python scripts/check_repository.py`).
- Final playable build: **passed**, 15m 1s, including 79 offline updater assertions and 393 scheduling assertions. The build used `build -x runGameTest -x sourcesJar` in addition to the prepared-resource flags below: the complete server suite had already passed separately, and the optional source archive was omitted.

The delivered `jugcraft-0.1.0-alpha.jar` is 344,301,866 bytes with 189,073 ZIP entries. Complete archive CRC verification passed, eleven relevant compiled classes and the two mixin manifests byte-matched the final outputs/source, the Efficiency recipe remains present, and embedded-library entries were verified. Test classes are excluded. The delivered copy's SHA-256 matches the finished build: `503a6a9f7e0c2313f547c0a0c3ab8fdf2bc4c406f450f650f508587b3649a9dd`.

The local Gradle commands use `-I ../ci600-compile.gradle`, which only removes prepared resource directories from compiler classpaths. Existing prepared resources are reused with `-x processResources -x processClientResources`; both edited mixin manifests are explicitly copied and byte-checked. The earlier unchanged item/recipe/art data audit remains recorded in the Efficiency feature; this correction changes Java and mixin registration only. Results here will distinguish actual runs from earlier evidence. Automated checks do not certify human multiplayer, a public-server load or live deployment.

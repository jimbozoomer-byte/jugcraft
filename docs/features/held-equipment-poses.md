# Equipment hand poses

Owner-directed visual correction, November 3, 2026. Implemented with OpenAI Codex (GPT-6) on the existing equipment working branch based on `235524e0c8`; earlier owner changes are preserved.

## Player experience

The mining drill, chainsaw and leaf blower sit at their actual rear grips, with their working ends visible in first person and facing forward in third person. Power and Ronin katanas are held by their hilts rather than their reversed blade ends. Industrial drills/chainsaw use their orange grips; the electric hoe, electric wrench/treetap and mining laser receive the quarter-turn needed by their opposite-diagonal sprites. The manual wrench and treetap have separate poses for their upright/horizontal artwork. The scanners, crop analyzer and obscurator are held upright at their lower casing/grip. The network tool, matter cannon and both color-applicator variants use the slanted handle in their imported meshes.

The bounded correction covers 23 model files, including both color-applicator states. Each receives explicit first/third-person poses for both hands. The bow, nano saber, charged staff, entropy manipulator and ordinary steel tools are included as visual references in the gallery; their existing poses are retained.

## Integration, assets and persistence

`tools/held_equipment.py` measures grips in the existing model coordinate system and places them at the native vanilla sword's hand contact. Katanas and readouts have a small first-person carry offset to keep their blade/display in view. Sprite orientation follows the artwork; centred 3D equipment mirrors its aiming angle for the opposite hand. The generator handles Minecraft's additional left-hand translation/rotation changes explicitly. It uses the native item display system; ArmsMotion, item-use animations and equipped armor rendering are unaffected.

The shared material generator runs this idempotent, display-only pass after its authored/imported model writers and final compatibility patches. It can also run directly with `python tools/held_equipment.py` for a bounded refresh. It updates existing files without passing imported meshes back through geometry finishing. No new model IDs or resource-manifest ownership changes are needed.

Only the four hand display contexts change. Mesh elements, textures and animation sidecars, item selectors, GUI icons, dropped-item poses and item-frame poses are preserved. Industrial and network runtime copies continue to use the supplied IC2/AE2 art from `art/owner-library/originals/Oct23/Jimtafacts/`; source originals and existing notices remain intact. This correction adds no textures, dependencies, renderers or platform changes.

This is cosmetic infrastructure for the existing electrical/tool progression. Recipes, JE capacities/costs, modules, mining/felling behavior and combat values are unchanged. No additional resource input, unlock, trade/solo route, conversion, automation loop or magical/ecological prerequisite applies. Item IDs and saved stacks remain compatible; the native client renders the same pose for other players. No server state, network protocol, ownership policy, chunk behavior, dimension/event rule or migration changes are introduced. Reverting the display edits restores the old poses without changing world data. No live server deployment or restart is included.

## Verification

The native rendered gallery checks 31 equipment/reference items in first and third person with both dominant hands, plus offhand equipment and a fully drawn power bow. It verifies the actual held item before capturing settled poses. Test worlds are disposable. Screenshots require visual review; a successful client run alone does not prove a grip looks correct. This is not a human multiplayer test.

- Rendered client: **passed**, `runClientGameTest -PclientTests=HeldEquipmentClientGameTests`, 7m 17s on the pinned Minecraft 26.3 platform. The final run produced 143 screenshots: 31 equipment/reference items in both views and both dominant hands, nine offhand examples per dominant hand, and the drawn power bow. Both-hand main views, the refined manual-tool/readout poses, offhand examples and drawn bow were visually reviewed.
- Native transform checks: **92 passed**, using a separate matrix composition against the serialized model data, including Minecraft's left-hand signs and the intended first-person carry offsets.
- Bounded regeneration: **passed**. All 23 original model snapshots regenerate to the final models; a second pass changes nothing. Every mesh/texture/non-hand display field matches its pre-change snapshot. Prepared runtime resources byte-match source. All 33 directly referenced model textures and existing animation sidecars byte-match the previous delivered chainsaw build.
- Repository structure/documentation links: **passed**, `python scripts/check_repository.py`.

Local Gradle runs use `-I ../ci600-compile.gradle` to remove prepared resource folders only from compiler classpaths, with `-x processResources -x processClientResources` to reuse prepared runtime output. The 23 changed model JSON files were copied into that output and byte-checked before client execution and packaging. The shared whole-data/recipe audit was not repeated for this display-only correction. The preceding chainsaw feature records the full 3,344-test server result; this correction does not claim a new server-suite run.

- Final build: **passed**, 15m 23s, including 79 offline updater and 393 scheduling assertions. It used `build -x runGameTest -x sourcesJar` with the prepared-resource flags above; the optional source archive was omitted.
- Delivered archive: **passed** complete CRC and duplicate-path checks, 344,310,142 bytes and 189,075 ZIP entries. Every ZIP payload was compared with the preceding tested chainsaw artifact: exactly the 23 equipment model files differ. All 6,985 runtime classes and every other archive payload byte-match that artifact. The 23 packaged models byte-match current source, test classes are excluded, and the delivered copy matches the finished build's SHA-256: `2a80aef46295045e080b4e6cfd32b615ffee363cbbdecea890471435ae560b65`.

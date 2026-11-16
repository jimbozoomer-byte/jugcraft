# Pharmacology improvement Batch E — Pattern Sight and Deep Reverie

Implementation tool: **OpenAI Codex (GPT-6 family)**. Owner-directed local continuation, 3 November 2026. Minecraft 26.3, Java 25, Loader 0.19.5, Fabric API 0.161.0+26.3, Loom 1.17.21, Gradle 9.6.0 and the existing framework pins remain unchanged.

## Selected contract

Extend the existing pharmacy and native world filter with LSD's geometric Pattern Sight, DMT's deeper geometry and cosmetic Clockwork glimpses, bounded directional afterimages, and explicit readable input reveals for authored Mirror/Reverie studies. Preserve shared controls, real paid study rewards, server authority and all manufacturing chains. Complete this improvement batch before original Batch 7's dimension travel, return anchors and elven trading.

Work units: (1) shared LSD/DMT channels and bounded afterimages; (2) peripheral Clockwork geometry/figures; (3) accessible transformed study clues; (4) real-item server/client integration, native captures and resource verification.

Reuse the private onset-complete flags and recorded-site payload from [Batch D](pharmacology-dream-visuals.md), the owner alchemy frame/native narrated buttons, shared magic particle budget and existing single native post chain. The owner library/catalog, framework guide and Concordance planning pack were consulted; no new dependency or saved registry is needed. Open PR search returned no open results for this repository at this checkpoint. Existing dirty pharmacy/owner work is preserved.

Afterimages will use three bounded offsets from the last six view ticks, resampling the currently visible world. This supplies directional trailing without storing past world frames, extra render targets, hidden information or input changes. Reduced Motion/zero motion, zero double vision, visual Off/zero intensity and native Distortion Effects zero remove them. Scene/player changes, menus and abrupt camera/body jumps reset their short history. Static geometric identities, useful site guidance and paid lesson clues remain available according to the existing controls.

The Clockwork glimpse is a cosmetic peripheral preview with slow fades and a static Reduced Motion version. It grants no travel, new target information, item, NPC interaction or trade access. Existing duration, onset, strain, tolerance, side effects and recipes are preserved. No injury condition or compulsory medical upkeep is added.

## Player outcome and progression

| Formulation | New presentation and useful study reveal | Existing duration / onset / strain |
| --- | --- | --- |
| LSD | Six-fold violet geometric lattice; up to three directional afterimages while turning. Memory remains available; Mirror cards show both their original symbol and its reversed input number. | 25 min / 20 s / 35 |
| DMT | Deeper eight-fold cyan geometry, short directional afterimages, copper Clockwork arches/gears and schematic pointed figures at the periphery. Reverie cards show each original symbol and its successor input. All three existing study layers remain available. | 5 min / 20 s / 50 |

These are fictional Minecraft profiles. LSD's existing supported aimed-gun hand-sway drawback remains +20%; this batch adds no gameplay drawback. Dose replacement, strain decay/soft thresholds, tolerance and combination accounting remain under [Batch A](pharmacology-strain.md).

The producer/consumer chain remains [botanical pharmacy](pharmacology-psychedelics.md): cultivated inputs → shared concentrates → paid Memory/Lucid Imprint → Perception Substrate → precision reactor LSD stock → verification/analysis/formula → press capsules. Paid Mirror produces the Pattern Imprint used by Oneiric Stock and precision DMT production, with its existing advanced capsule packaging. Paid Reverie's Oneiric Imprint retains its future consumer in original Batch 7. Existing reactor/Separator/Analyzer/Press tooling, power, times, input counts and yields are unchanged. No new ingredient, machine, crop, recipe or economic shortcut is introduced.

## Controls, frame ownership and bounds

The existing `drug_alcohol` native world chain now uses eight vec4s, **128 bytes**. The first six vectors retain their order; the formerly reserved Dream.z/w components carry LSD/DMT. Two appended vectors carry the three view offsets and glimpse amplitude. The canonical post-effect writer, GLSL block, extracted immutable frame and native mapped ring buffer agree. The existing 15-bit server presentation mask and private dream-site payload are unchanged. Only onset-complete server flags authorize the identities. Server/client versions must agree on the matching resources and code.

The fixed eight-slot angle buffer reads local view rotation each client tick, taking offsets from two/four/six ticks ago and capping each axis at 0.018 texture units. The world shader adds at most three current-world texture samples when turning. When still, the buffer settles within six view ticks. It clears across menus, visual disable, player/level replacement, remote cameras, disconnect, body jumps over eight blocks and single-tick view jumps over 45 degrees. It never retains an image of an unloaded area. No extra target, world scan, particle system, entity, input packet or C2S action is introduced. Rendering consumes extracted scalar values rather than mutable history.

Overall intensity/native Distortion Effects scale both advanced identities. Reduced Motion or motion zero makes geometry and Clockwork motifs static and removes trails/new moving wisps. Double vision zero independently removes the trails. Off/intensity zero/native accessibility zero removes these cosmetics while retaining authorized site guidance and clues. The slow glimpse modulation has a roughly 29-second cycle, never flashing/blackout behavior. Central targeting and all HUD/menu text remain clear; the shader never alters view rotation, camera FOV, aiming rays, time or movement. The short trail is a directional current-world afterimage, not a retained past-frame recording.

Clockwork imagery is procedural schematic line art in the same world shader. Existing owner alchemy UI art and shared magic wisps are reused; the catalog's mirror/rune assets were inspected, and no new bitmap/sound/model asset is needed for this screen-space motif. No library executable was run and the owner originals remain untouched.

## Readable, paid pattern reveals

The existing owner-framed study screen grows from 302×200 to **302×214** so its three cards can show a source row and a required-input row. Mirror highlights the source in reverse order; Reverie keeps source order and explicitly names the next symbol. Memory uses the same layout. Narrated native tooltips state both source and trace order. Clues and input hints stay hidden when the appropriate layer is locked, during onset, after cleansing, without a carried Folio or when the server invalidates reach/visibility. Completed lessons hide their hints.

`DreamStudyMenu.trace` derives hints only from synchronized, already-authorized clues. Server answers still use the server-owned authored pattern. The same native seven button IDs, owner/container validation, rate limit, layer gates, payment, output-space checks and one-completion guard remain. One successful study still spends one Study Canvas and produces one matching imprint; wrong answers retain their existing harmless reset. Turning cosmetics off never spends or grants anything.

## Trying it in the flat world

Restart the local development client and reopen **Pharmacy Flat Test**. Carry a Dream Folio and Study Canvases, place a Dream Study Plinth and interact nearby to record it. Consume LSD, wait 20 seconds, turn gently to see the short afterimages, then open Mirror and follow the input numbers on the cards. Close the study and consume DMT; after its own 20-second onset, inspect the peripheral Clockwork preview and the advanced Reverie card inputs. Test Reduced Motion, motion/double vision zero and Off in shared display settings. Actual travel/trading is the next original content batch. Existing saves are preserved.

## Validation

All **89 required pharmacy server tests passed** at **21:36:13 America/Chicago**, 3 November 2026. The previous 87 tests plus two new `PharmacologyReverieGameTests` cover actual LSD/DMT onsets, hidden/authorized transformed inputs, real paid advanced rewards, no completion replay, wrong-layer rejection, Folio loss, bounded indices and cleansing preserving strain. Earlier manufacturing, accounting and benefit regressions passed in the same run.

`PharmacologyReverieClientGameTests` passed at **21:40:42** with real formulations and native rendering. It covers both onset gates, distinct geometry/glimpse channels, actual bounded trails while turning, idle/sudden-jump cleanup, Reduced Motion, motion zero, double vision zero, Off and native Distortion Effects zero, paid Mirror/Reverie buttons and inventory rewards with cosmetics off, unchanged body/dimension, caffeine composition, shader reload and milk clearing the advanced channels/private cues without refunding strain. Seventeen native captures were produced. Reviewed images show violet lattice, deeper geometry/static Clockwork motifs, readable reversed/successor card inputs and the paid lessons. Canonical generation copied and byte-verified **468 runtime resources**. Repository structure/documentation links passed.

The combined run passed **all six selected rendered client classes**, using the pinned Sodium/Iris/Fusion stack with no shader pack selected:

- `PharmacologyReverieClientGameTests`: the complete new E flow above.
- `PharmacologyDreamClientGameTests`: actual Psilocybin/Mescaline, recorded-site privacy, wisps, guidance, paid Memory, controls, reload and milk with the expanded card layout.
- `PharmacologyCalmClientGameTests`: THC, opioids, four-heart Lidocaine, actual detached Echo View, 24-block server limit, body guidance, native accessibility and return/interaction guards.
- `PharmacologyFocusClientGameTests`: regular/extended-release stimulant/focus formulations, actual benefits, warning bands, composition, motion, controls, reload and cleanup.
- `PharmacologyVisualClientGameTests`: all five alcohol grades, unchanged aim, independent controls, lighter sustained phase, resource reload and milk.
- `PharmacologyPsychedelicClientGameTests`: sober clue hiding, all three real paid study layers, planted crops, recorded navigation, a world-ticked Separator job and THC use.

The rendered E/D/C/B/alcohol checkpoints passed at **21:40:42 / 21:42:38 / 21:45:20 / 21:48:58 / 21:52:03**, followed by the final original psychedelic suite and `BUILD SUCCESSFUL in 19m 23s` at **21:53:03**, all 3 November 2026 America/Chicago. The existing 120-second asset-reload allowance was retained; gameplay deadlines/assertions were unchanged.

Reproduction: `build-local.ps1 -Tasks` with `runGameTest`, `runClientGameTest`, `-PpharmacyTestFilter=jugcraft-test:pharmacology*`, the six `-PclientTests` names above, `-I ../early-pharmacy-focused.gradle`, `-x processResources`, `-x compileClientKotlin`, `--offline`, `--max-workers=2`. The unchanged Kotlin sources have no reference to the expanded frame. The skipped resource task was populated by the bounded canonical writer; all 468 source/runtime files were byte-verified, including the matching shader and eight-vector layout. Production client compilation also passed separately in 1m 52s.

Repository/documentation links and scoped/new-file whitespace checks passed. **133 native PNGs** (17 E, 15 D, 18 C, 35 B, 36 alcohol, 12 original psychedelic), successful logs, source/runtime manifests and evidence hashes are preserved outside Git in the parent workspace's `batch-e-2026-11-03/evidence-final/`. Reviewed captures include the geometric identities/afterimages, static Clockwork preview, Mirror/Reverie input cards with cosmetics off, paid rewards, post-reload world and cleanup. These are actual game captures.

Local dirty branch: `codex/pharmacology-caffeine`, base HEAD `f02bccb606c17a4305d5355a6ab6f75736da6c27`. Server checks used native test structures and rendered checks used new disposable Fabric client-test worlds with native player inventories. No saves, registry IDs, attachments, Folio format, network schema or configuration keys changed. No release JAR/pack was published, no live server was changed or restarted, and the existing ordinary flat test save was preserved.

The broad mod-data audit, a representative performance benchmark and human two-client dedicated-server playtest were not repeated. Graphics-mods-absent rendering remains unverified because of the existing pre-world development startup problem recorded in [alcohol validation](pharmacology-visuals.md#validation). Active external shader packs were not tested. This is local implementation/automated evidence; restart the local playtest client to load the compiled changes. Original Batch 7 remains planned.

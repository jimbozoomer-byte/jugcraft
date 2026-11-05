# Pharmacology improvement Batch D — dream sight and recorded-site guidance

[Batch E](pharmacology-reverie-visuals.md) subsequently adds advanced identities and input reveals; its 128-byte world-filter layout supersedes the 96-byte delivery size recorded below.

Implementation tool: **OpenAI Codex (GPT-6 family)**. Owner-directed continuation, 3 November 2026. Local implementation only. Minecraft 26.3, Java 25, Loader 0.19.5, Fabric API 0.161.0+26.3, Loom 1.17.21 and the existing framework pins remain unchanged.

## Player outcome

| Formulation | Presentation and useful cues |
| --- | --- |
| Psilocybin | Gentle violet/warm color, optional slow organic distortion around the edge of the world view, and bounded wisps at recorded, visible study plinths within 16 blocks. A persistent HUD explains missing Folios, empty searches and the direction/distance/height of the nearest eligible site. |
| Mescaline | Restrained green crystalline peripheral accents with an optional slow pulse. Persistent relative direction, distance and height guidance to the nearest recorded, loaded plinth within 96 blocks. Nearby unrecorded plinths are excluded. |
| Dream studies | Three numbered, readable source-clue cards; the next source card is highlighted, including reverse order for Mirror. Glyph buttons expose the lesson rule and clues through native narrated tooltips. Real server-checked answers still produce paid imprints. |

The existing Psilocybin 10-second onset/eight-minute duration/20 strain and Mescaline 15-second onset/15-minute duration/25 strain are unchanged. Shared family replacement, decaying strain, tolerance, recovery and combination accounting follow [Batch A](pharmacology-strain.md). There are no new injury conditions, compulsory treatments, drug drawbacks or real-world manufacturing instructions.

The existing LSD and DMT study layers keep their authorized recorded-site cues and readable cards. Their distinct advanced visual identities, trails and Clockwork glimpses are delivered in [improvement Batch E](pharmacology-reverie-visuals.md). Actual Clockwork dimension travel and trading remain original Batch 7.

## Accessibility and rendering

The native world filter from [alcohol](pharmacology-visuals.md), [focus](pharmacology-focus.md) and [calm](pharmacology-calm.md) gains two smoothly interpolated channels. Psilocybin and Mescaline target 65% and 60% before local intensity. Effects protect the center of the view and never rotate the camera, change aim or delay input. The existing stable post-effect ID is retained. The generator-owned uniform layout and native ring buffer now use six vec4s, **96 bytes**; all earlier channels remain in the same order. No temporal framebuffer history, renderer replacement, dependency or new bitmap asset is introduced. Existing owner alchemy art, native font and shared magic wisps are reused after consulting the owner library/catalog.

Shared intensity, motion, blur, double-vision, Off, Reduced Motion and Minecraft's Distortion Effects setting remain authoritative. Reduced Motion or zero motion makes the organic/crystal shader static and suppresses new moving wisps. Off, zero drug intensity and native Distortion Effects zero remove the filter and new wisps. Essential text guidance and paid study clues remain available. Existing particles finish their normal short lifetime after a control changes.

Wisps reuse `SignClient` and the shared `Presentation` budget/intensity/distance rules: at most four eligible sites and six particles per site per one-second update, further reduced by local drug intensity/motion/native scale. No custom particle backend, world entity or site scan is added. The HUD hides with F1 and open screens. Remote scrying, spectator mode, death, player/level replacement and disconnect cannot retain a prior player's dream guidance.

## Private authority and useful studies

The server reads only positions already stored in a carried Dream Folio, whose existing cap is 16. Physical recording, Overworld restriction and existing reach/line-of-sight checks remain. Sight first checks the loaded ray path, then native visibility, selects the nearest four eligible sites and applies the existing 16/24-block range. Mescaline chooses only the nearest recorded, loaded, still-existing plinth within 96 blocks; it can guide through ordinary terrain but discovers no new site and requests no chunk.

The new `pharmacy_dream_cues` payload is private S2C information sent only to its owner on the existing one-second perception clock. It carries a mode, carried-Folio flag and at most four immutable block positions (one for Mescaline). The decoder rejects oversized counts before reading positions. Inactive players receive no continuous stream; an active stream ends with an empty update. The client additionally requires matching onset-complete presentation authority, current player/level identity and a fresh heartbeat. There is no new C2S query or remote action. The existing strain presentation mask expands to 15 valid bits; client and server must update together.

The cards expose the existing authored puzzles: repeat Memory, reverse Mirror, or advance each Reverie symbol through Sun/Wave/Leaf/Star. Locked layers keep their clues hidden. The server still checks the owner/container, physical reach, line of sight, current active drug, valid answer, canvas payment, output space and one-completion limit. A wrong answer resets progress without spending a canvas. One completed lesson spends one Study Canvas and returns one matching imprint. UI hints grant no rewards themselves.

## Progression and saves

The [botanical pharmacy chain](pharmacology-psychedelics.md) remains unchanged: cultivated Dreamcap/Prism Cactus inputs, compact reactor concentrates and stock, Separator verification, Analyzer formula, Pharmaceutical Press capsules. Psilocybin's paid Memory lesson supplies Lucid Imprints for the later LSD chain. Mescaline remains a navigation branch. Existing machines, energy costs, packaging, yields, crop producers, recipe IDs and consumers are preserved. No item, machine, saved registry entry, Folio format or attachment field is added. The cue is transient network state. Tooltips, handbook, encyclopedia and post-effect layout are generated by the canonical pharmacy writers.

## Trying it in the flat test world

Restart the local development client, then reopen the existing **Pharmacy Flat Test** world. Find Dream Study Plinth, Dream Folio, Study Canvas, Psilocybin and Mescaline through Creative search. Command-enabled worlds can also give the items using `jugcraft:dream_study_plinth`, `jugcraft:dream_folio`, `jugcraft:study_canvas`, `jugcraft:psilocybin` and `jugcraft:mescaline`.

1. Place two plinths about 30 blocks apart. Carry the Folio and right-click each from nearby to record it. Sober studies keep their clues hidden.
2. Carry several Study Canvases, consume Psilocybin and wait ten seconds. Near a recorded plinth, inspect the optional organic effect/wisps and guidance; open the plinth and repeat the three numbered Memory symbols. Completion spends one canvas and gives one Lucid Imprint.
3. Turn drug visuals Off or enable Reduced Motion in the shared display settings. Text clues and actual study rewards remain available.
4. Close the study, consume Mescaline and wait fifteen seconds. It replaces the psychedelic family. Move away from the nearest recorded plinth and follow the relative arrow, distance and height toward it within 96 loaded blocks. Removing the Folio removes the target.

These steps use the existing local test world; this batch does not replace a save or restart the live server.

## Validation

All **87 required pharmacy server tests passed**: the previous 83 plus four new `PharmacologyDreamGameTests`. New coverage uses actual drug items and checks onset/Folio/recording/visibility gates, another sober player's copied Folio, nearest-site selection, the four-site bound, no chunk loads, Mescaline's range, existing LSD/DMT layers, milk retaining strain, immutable positions and malformed wire bounds. Canonical generation copied and byte-verified **468 runtime resources**.

`PharmacologyDreamClientGameTests` passed in the rendered client at **20:58:47 America/Chicago** on 3 November 2026. It uses actual capsules, private network updates, a normal native plinth screen and real inventory payments. Coverage includes onset, missing/removed Folios, unrecorded-site exclusion, actual budgeted wisps, unchanged camera aim, readable paid Memory clues, wrong-answer reset, one canvas/one imprint, family replacement, nearest recorded navigation, relative view direction, Reduced Motion, Off, native accessibility zero, resource reload and milk clearing cues/filter while retaining strain. Fifteen native captures were produced; reviewed images show the cards/highlight, organic identity, recorded-site direction and cleansing cleanup.

The first rendered attempt passed through all new in-world behavior but exceeded the existing 60-second resource-reload allowance while rebuilding the full owner asset collection. The successful rerun allows up to 120 seconds for that asset operation only; no gameplay deadline or assertion was relaxed. The same allowance is used by the selected older shared-filter regression tests.

The combined rendered run passed **all five selected client classes** with the pinned Sodium/Iris/Fusion stack and no shader pack selected:

- `PharmacologyDreamClientGameTests`: the complete new Batch D flow above.
- `PharmacologyCalmClientGameTests`: the complete THC/opioid/Lidocaine/Echo View regression, including actual detached rendering, body guidance, server bounds and return guards.
- `PharmacologyFocusClientGameTests`: regular/extended-release focus and stimulant profiles, actual benefits, all strain warning bands, composition and accessibility.
- `PharmacologyVisualClientGameTests`: all five alcohol grades, unchanged aim, controls, lighter sustained phase, reload and cleansing.
- `PharmacologyPsychedelicClientGameTests`: actual Memory, Mirror and Reverie lessons and payments, hidden sober clues, planted crops, Mescaline guidance, a world-ticked Separator job and THC consumption.

The server run passed at **20:39:53**; rendered D/C/B/alcohol checks passed at **20:58:47 / 21:01:51 / 21:05:42 / 21:09:40**, followed by the final psychedelic suite and `BUILD SUCCESSFUL in 19m 11s` at **21:10:50**, all 3 November 2026 America/Chicago. The exact local command used `build-local.ps1` with `runClientGameTest`, the five `-PclientTests` class names above, `-I ../early-pharmacy-focused.gradle`, `-x processResources`, `-x compileClientKotlin`, `--offline` and `--max-workers=2`. The matching server run used `runGameTest -PpharmacyTestFilter=jugcraft-test:pharmacology*` with the same focused build arguments. The bounded resource writer populated the skipped resource task's output and byte-verified all 468 files against source, including the final shader.

Repository/documentation-link checks and scoped whitespace checks passed. **116 final native PNGs** (15 D, 18 C, 35 B, 36 alcohol and 12 original psychedelic), successful logs, resource/source manifests and evidence hashes are preserved outside Git at the parent workspace's `batch-d-2026-11-03/evidence-final/`. The first attempt's 13 captures and timeout log remain separately in `evidence-first/`. Reviewed native captures include the readable cards and next-step highlight, organic sight, crystalline/resonance guidance, cosmetics off, later lesson cards and real manufacturing output. No synthetic illustrations substitute for game captures.

Local dirty branch: `codex/pharmacology-caffeine`, base HEAD `f02bccb606c17a4305d5355a6ab6f75736da6c27`. Test worlds were new disposable Fabric client-test worlds with actual native player inventories; server checks used the existing native test structures. The broad mod-data audit and human two-client dedicated-server playtest were not repeated. Graphics-mods-absent rendering remains unverified because of the existing pre-world development startup problem recorded in [alcohol validation](pharmacology-visuals.md#validation). No live server was stopped/restarted or updated, and the ordinary flat test world was preserved. Restart the local playtest client to load these compiled changes.

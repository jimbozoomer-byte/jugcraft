# Pharmacology improvement Batch B — focus, work and warning visuals

[Batch C](pharmacology-calm.md) subsequently extends this shared filter/protocol with calm and remote-view profiles. The layout and protocol details below record the original B delivery.

Implementation tool: **OpenAI Codex (GPT-6 family)**. Owner-directed continuation of the local pharmacy, 3 November 2026. Minecraft 26.3, Java 25, Loader 0.19.5, Fabric API 0.161.0+26.3 and the existing framework pins remain unchanged. Local implementation; no server deployment or restart.

## Player outcome

| Formulation / state | New presentation | Gameplay change |
| --- | --- | --- |
| Caffeine / Caffeine XR | Subtle warm contrast lift | Existing Haste I and walking bonus retained |
| Methylphenidate / XR | Cooler, clearer contrast | **25% tighter aimed firearm spread**, alongside existing hand steadiness and Haste II |
| Modafinil / XR | Restrained neutral clarity | **15% lower activity exhaustion** and native phantom rest counter held at zero while active; existing Haste I and hand steadiness retained |
| Amphetamine / XR | Warm peripheral tunnel | Existing tier-specific Haste/Speed and food cost retained |
| Stimulant | Warm peripheral tunnel | Existing short Haste/Speed and appetite retained |
| Adrenaline | Stronger initial peripheral tunnel, settling over the first sixteen seconds | Existing burst benefits and natural-expiry fatigue retained |
| Drug Jitters | Small peripheral image tremor | Existing spread drawback retained |
| Stimulant Fatigue | Gentle desaturation | Existing movement/mining drawback retained |
| Strained / Overloaded / Severe / Critical | Increasing amber-to-red peripheral rim; bounded edge blur | Batch A penalties retained |

All numbers describe fictional Minecraft balance, not real pharmacology. No new injury, combat, sleep-deprivation or treatment condition is added.

Methylphenidate reduces the actual server-side aimed shot error to 75% for both release forms, after selecting the strongest existing drug/strain drawback. Hip fire is unchanged. Critical strain therefore still produces 112.5% of normal aimed error with focus. The common firearm path covers bullets, darts, beams and aimed projectile launchers. Flame coverage and arc target-acquisition cones are deliberately unchanged: narrowing those useful cones would reduce coverage. Neither mouse aim nor weapon recoil is altered. Cosmetic settings cannot remove or grant this benefit.

Modafinil's activity discount uses the same shared best-benefit selection as Rehydration Salts; 15% and 20% discounts do not multiply. It does not discount native healing's food cost or restore food. The existing active pulse resets the native `TIME_SINCE_REST` statistic once per online second, preventing phantom buildup while the dose is active. Onset does not grant this benefit. Cleansing, replacement, natural expiry and death stop maintaining the counter; normal native counting resumes without adding a new negative effect. Resetting a native statistic is not forced sleep, changing the world's time, removing existing phantoms or granting night vision. XR keeps its longer duration and existing 30-second onset.

## Shared display and authority

The [existing alcohol filter and controls](pharmacology-visuals.md) are extended. No second post-processing backend, camera controller, gameplay ledger, texture overlay or dependency is added. The owner library/catalog were consulted; procedural sampling of the current world remains appropriate, with the existing original Jugcraft shader extended in place.

The private owner-only `pharmacy_strain` server-to-client payload gains a bounded one-byte active-presentation mask and a one-byte Adrenaline dose cue. The mask derives from the same ledger, onset completion and live marker checks as the real benefits. A marker installed during onset is insufficient to display an active stimulant effect. The transient cue advances only after a successful Adrenaline dose, so unrelated formulations and native timer corrections cannot replay a settled rush, while a tolerance-shortened replacement still restarts it. Its weak owner cache clears on disconnect/server stop; it is not persisted or used for gameplay. Only changes are sent, using the existing status cache. There is no client-to-server request or setting that can modify gameplay. Update client and server together; the expanded transient packet is not compatible with an older client, while persisted attachment/item/effect IDs and save data remain unchanged.

One shared native world chain composes alcohol, focus, energy, fatigue, warning and Jitters. The stable `drug_alcohol` resource ID is retained. Its existing native ring-buffer uniform expands from 32 to 64 bytes, with matching generator-owned JSON and shader layout. Channels ease in/out over at most two seconds, with immutable extracted frames for GPU work. Warning strength uses the authoritative fractional strain bands. No history targets, trails, scans, extra entities or chunk loading are introduced. Only the local player's private state is read.

The warning has a slow four-second peripheral breath. Jitters shift only peripheral world samples. Reduced Motion or a zero sway slider removes both animations and alcohol sway; warnings remain static. All new presentation obeys the existing On/Off, intensity and native Distortion Effects controls. Blur remains independently adjustable. The center stays geometrically clear; the interface and crosshair render afterward. Opening any screen suspends the world filter. Death, spectator mode, owner replacement and disconnect clear it; singleplayer pause freezes its envelopes. Milk ends dose-specific channels but retains warnings until strain falls. The text strain HUD works when cosmetics are off.

## Progression, acquisition and persistence

Existing tiers, farm inputs, machines, formulas, packaging, power, costs, strain, tolerance, duration and output consumers remain as recorded in [precision pharmacy](pharmacology-precision.md), [controlled release](pharmacology-controlled-release.md) and [decaying strain](pharmacology-strain.md). Existing saved items acquire the improved behavior. Canonical tooltips, handbook and encyclopedia explain the new benefits and visual controls. No recipe, machine, registration or save migration is introduced by B.

## Validation

All **80 required pharmacy server tests passed**, including the 77 existing checks and three new `PharmacologyFocusGameTests`. These exercise both release forms' onset, aimed/hip behavior, critical-strain interaction, milk cleanup, actual native activity exhaustion, the shared Salts discount and the native rest statistic. A real aimed Sprocket Gun shot misses a narrow target without focus and hits with focus using the same aim and random sample, spending one round each time. The pinned Minecraft 26.3 `PhantomSpawner` bytecode was also inspected and reads the same `TIME_SINCE_REST` statistic maintained by Modafinil.

Both rendered classes passed in disposable Survival worlds with the installed Sodium/Iris/Fusion stack and no shader pack selected:

- `PharmacologyFocusClientGameTests`: ten real formulations, onset gating, warm/cool identities, early tolerance-shortened Adrenaline replacement cues, settled rush, unrelated-dose continuity, natural fatigue, all warning bands, alcohol/focus/Jitters/critical-strain composition, unchanged camera aim, Reduced Motion, sharp inventory, visual Off preserving mechanics, native Distortion Effects zero, full resource reload, milk retaining warnings, and complete cleanup.
- `PharmacologyVisualClientGameTests`: all five real alcohol grades, increasing strength, peak/sustained phases, unchanged aim, accessibility/settings, native shader reload and cleansing. This complete existing regression passed with the expanded shared uniform.

The final combined run on 3 November 2026 passed at 19:21:30 (server), 19:27:58 (B client) and 19:31:31 (alcohol client), local America/Chicago time; `BUILD SUCCESSFUL in 14m 1s`. The branch remains local and dirty on `codex/pharmacology-caffeine`, base HEAD `f02bccb606c17a4305d5355a6ab6f75736da6c27`.

Canonical generation refreshed and byte-verified **468 runtime resources**, including the shader, matching post-effect layout and current language/handbook/encyclopedia. Repository/documentation-link checks and scoped whitespace checks passed. No recipe/item/machine additions required another full mod-data audit; that broad audit was not repeated.

**71 native PNGs** (35 B and 36 alcohol), the final log, resource manifest and source/evidence hashes are preserved outside Git under the parent workspace's `batch-b-2026-11-03/evidence/`. Reviewed captures include sober/focus contrast, fatigue, critical strain, combined effects, Off and milk. An initial fixed-delay cleanup assertion failed after resource reload; the test now waits for synchronized removal before checking the same fade/retained-warning conditions. That transition check and the final full run passed. Earlier diagnostic captures/logs remain in separate evidence folders.

No human two-client multiplayer playtest or graphics-mods-absent pass is claimed. The earlier development startup problem without the installed graphics stack remains recorded in [alcohol validation](pharmacology-visuals.md#validation). Restart the ordinary local playtest client to load the compiled changes; its existing flat world was not replaced, and the live server was not touched.

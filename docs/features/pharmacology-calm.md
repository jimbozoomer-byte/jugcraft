# Pharmacology improvement Batch C — calm visuals and wider scouting

[Batch D](pharmacology-dream-visuals.md) subsequently adds dream channels and private study guidance; its 96-byte filter layout and 15-bit presentation mask supersede the delivery sizes recorded below.

Implementation tool: **OpenAI Codex (GPT-6 family)**. Owner-directed continuation of the local pharmacy, 3 November 2026. Minecraft 26.3, Java 25, Loader 0.19.5, API 0.161.0+26.3 and the existing framework pins remain unchanged. Local implementation only.

## Player outcome

| Formulation | New presentation / benefit |
| --- | --- |
| THC | Gentle warm botanical color and light peripheral softness; existing Luck, stationary fishing benefit and mining tradeoff retained |
| Morphine | Muted cool color, softer edges and a restrained peripheral vignette; existing damage/knockback benefits and Drowsiness retained |
| Fentanyl | Stronger version of the opioid presentation; existing benefits and tradeoffs retained |
| Fentanyl Patch | Lighter sustained opioid presentation; existing controlled-release benefits retained |
| Ketamine | Cooler detached-view color, light peripheral color separation and optional slow peripheral breathing during the actual Echo View |
| Lidocaine | **Four absorption hearts (8 health points)** once per activation, replacing the previous two-heart grant; existing +25% knockback resistance retained |
| Echo View | **24-block radius**, replacing 12; unchanged maximum 30-second duration and 3-block/second input speed; essential body-direction HUD |

These are fictional Minecraft balance values. No injury, combat-generated ailment, compulsory treatment, new withdrawal meter or new drug-caused gameplay drawback is added.

Lidocaine keeps its existing 5-second onset, 10-minute benefit and 10 strain cost. Recurring pulses do not refill a spent shield. A new paid dose replaces its family, restarts onset and retains tolerance under [Batch A's rules](pharmacology-strain.md). Vanilla Absorption retains its existing priority and never adds another pharmacy capacity on top. Existing effect/item/attribute IDs and the player attachment schema are preserved.

## Display and accessibility

The [shared native filter](pharmacology-visuals.md), extended in [Batch B](pharmacology-focus.md), gains THC, opioid and Echo channels. Morphine/Fentanyl/Patch relative amplitudes are 50%/75%/30% before local intensity; THC is 55% and Echo 80%. Channels ease in/out over at most two seconds. All existing intensity, blur, motion, double-vision, Reduced Motion, Off and native Distortion Effects controls apply. Opioids do not close the player's eyes or add blackouts; no forced camera turns, input delay, time changes, flashing overlays or hidden-block information are introduced.

Echo's optional motion and color separation affect only peripheral world samples. Reduced Motion/zero motion makes cosmetic distortion static; zero double vision removes its separation. Essential body guidance and the actual controlled camera remain available with cosmetics disabled. Returning to the body removes the Echo tint rather than carrying it over to the normal camera. Opening a screen suspends world filters and ends Echo View through the existing return path.

The existing owner-only strain payload's presentation mask expands from a byte to a masked VarInt (11 valid bits); its Adrenaline cue is preserved. Only active, onset-complete profiles authorize their presentation. Server and client must be updated together. No new C2S action or gameplay ledger is added. The existing stable post-effect ID remains `drug_alcohol`; the matching shader, generator-owned layout and native ring-buffer uniform expand from 64 to **80 bytes**. Rendering consumes immutable frames. Bounded samples compose all profiles in one world chain. The owner library/catalog were consulted; the existing procedural shader and native font suit this work, so no bitmap/sound assets or dependencies are added.

Minecraft 26.3's local `GameRenderer` bytecode was inspected: it requests post effects from the local player even while the camera is detached. The shared request guard now permits the existing Echo camera, while other remote/spectator cameras retain their prior boundary. No second camera or renderer ownership is introduced.

## Body guidance and server boundaries

The native HUD shows a relative arrow/direction, straight-line distance back to the body's eye, and whether the body is above/below or at the same height. A directly overhead/underfoot body gets a vertical arrow and "directly" label instead of an arbitrary horizontal bearing. At the origin it says "At your body." A second line states that the body remains vulnerable, the Sneak return control and the current server radius. It uses only body/view coordinates already supplied by the private Echo state; there is no world search, target discovery or extra location network message. It obeys F1 and disappears on return, death, spectator mode, player/level replacement and disconnect.

The server retains the body, inventory, game mode and chunk center. Direction-only input is normalized and accepted at most once every two ticks. The new radius constant is shared with the HUD. Before collision/ray queries, every corner of the candidate viewport box must lie in loaded chunks, including boxes straddling a chunk boundary. Ordinary walls remain solid to the viewpoint. There is no chunk request/loader, teleport, inventory duplication or remote interaction permission.

Existing damage, menu, Sneak, expiry, death, disconnect, dimension and body-displacement return rules remain. Returning never refunds strain or clears tolerance. Client ownership/level checks clear old view data before it can place a newly loaded player at a previous body's coordinates. The real remote-view action remains useful even with cosmetic effects off.

## Progression, costs and saves

Existing crop producers, stocks, formulations, machines, power, packaging, recipe yields, tiers, onsets, durations, strain costs and consumers remain as recorded in [sterile pharmacy](pharmacology-sterile.md), [precision pharmacy](pharmacology-precision.md), [controlled release](pharmacology-controlled-release.md) and [botanical pharmacy](pharmacology-psychedelics.md). No machine, ingredient, recipe, saved registry ID or save field is added. Tooltips, handbook and encyclopedia are regenerated from their canonical writers. The earlier delivery records describe their original balance; this record owns the four-heart/24-block update.

## Validation

All **83 required pharmacy server tests passed** (the previous 80 plus three new `PharmacologyCalmGameTests`). Coverage includes actual damage spending the eight-point shield, no pulse refills, native milk/attribute cleanup, native save/logout/rejoin preserving a partly spent shield, stronger potion priority, controlled movement beyond the old radius to the 24-block bound, packet-rate limits, ordinary walls, body immobility and the unchanged 30-second native deadline. Existing manufacturing and drug-benefit regressions also passed.

The full rendered run passed all three selected classes with the pinned Sodium/Iris/Fusion stack and no shader pack selected:

- `PharmacologyCalmClientGameTests`: actual Lidocaine/THC/Morphine/Fentanyl/Patch items, onset gating, distinct opioid strengths, family composition, Naloxone preserving THC and accounting, visual Off preserving mechanics, native shader reload, actual detached Echo camera, scouting beyond 12 blocks, server-owned 24-block cap, relative direction/height, Reduced Motion and native accessibility zero, HUD with cosmetics off, inventory-drop rejection, Sneak/damage/menu returns and cleanup.
- `PharmacologyFocusClientGameTests`: complete Batch B presentation/benefit integration and shared-filter regression.
- `PharmacologyVisualClientGameTests`: complete five-grade alcohol, controls, aim, sustained-phase, reload and cleansing regression with the 80-byte layout.

The combined run passed on 3 November 2026 at 19:53:11 (server), 19:58:56 (C), 20:02:50 (B) and 20:06:31 (alcohol), local America/Chicago time; `BUILD SUCCESSFUL in 16m 28s`. A subsequent HUD review identified the purely vertical bearing edge case. After refining only the client HUD/label and adding its native capture, the complete C rendered class passed again at 20:17:20, `BUILD SUCCESSFUL in 7m 21s`. Common gameplay and the shader remained unchanged after the combined regression run.

Canonical generation refreshed and byte-verified **468 runtime resources**, including the matching shader/layout, localized HUD and current handbook/encyclopedia. Repository/documentation-link checks and scoped whitespace checks passed. **89 final native PNGs** (18 C, 35 B and 36 alcohol), both successful logs, resource/source manifests and evidence hashes are preserved outside Git under the parent workspace's `batch-c-2026-11-03/evidence-final/`; initial captures remain separately in `evidence-first/`. Reviewed images include four yellow absorption hearts, THC/opioid presentation, directly-below guidance, the 24-block boundary, cosmetics off and return cleanup.

Local dirty branch: `codex/pharmacology-caffeine`, base HEAD `f02bccb606c17a4305d5355a6ab6f75736da6c27`. The broad mod-data audit and human two-client playtest were not repeated. Graphics-mods-absent rendering remains subject to the existing pre-world development startup problem recorded in [alcohol validation](pharmacology-visuals.md#validation). The existing ordinary flat test world and live server were not replaced or restarted. Restart the local playtest client to load the compiled changes; Survival mode shows the absorption-heart HUD.

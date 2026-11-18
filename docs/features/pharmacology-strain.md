# Decaying strain — improvement Batch A

Implementation tool: **OpenAI Codex (GPT-6 family)**. Owner-approved continuation of the local pharmacy, implemented and verified locally. Not deployed. Minecraft 26.3, Java 25, Loader 0.19.5 and Fabric API 0.161.0+26.3 remain pinned. Base checkout: `f02bccb606c17a4305d5355a6ab6f75736da6c27`, with the existing uncommitted pharmacy batches preserved.

## Player outcome

100 is the safe threshold rather than a consumption cap. Every dose immediately adds its full formulation cost. Strain decreases by **6 points per online minute** while any dose is pending or active, and **12 per minute** otherwise. Recovery records do not reserve strain or block consumption. Benefits may outlast their strain. Every existing manufacturing chain supplies the same formulation with its original duration, cost and benefit; this batch changes shared accounting and administration rules rather than introducing a new progression gate.

| Strain | State | Movement | Mining | Attack speed | Activity food cost | Gun spread |
| --- | --- | --- | --- | --- | --- | --- |
| 0 through 100 | Stable | — | — | — | — | — |
| Above 100 through 125 | Strained | — | — | — | +15% | +10% |
| Above 125 through 150 | Overloaded | −10% | −10% | — | +25% | +20% |
| Above 150 through 200 | Severe | −20% | −25% | −15% | +40% | +35% |
| Above 200 | Critical | −30% | −40% | −25% | +60% | +50% |

Matching drug/strain penalties use the stronger value. Opioid Drowsiness, Intoxication, Stimulant/Adrenaline Fatigue and THC's mining penalty retain their effect IDs and markers, while one shared set of transient attribute modifiers owns their movement/mining penalties. Old modifier IDs are removed when reconciling players. Positive drug and normal potion benefits retain their existing priority. Natural regeneration's direct food cost remains separate from activity exhaustion. Gun spread remains server-authoritative. No injury, illness or combat-created debuff is introduced.

## Administration and persistence

- A new formulation replaces its family immediately, ends the old benefits and restarts its own onset. It adds full strain without refunding the earlier dose or banking unused duration. Short native benefit pulses are cleaned selectively; normal stronger/longer potions remain intact.
- Repeated family doses retain 75%, then 50% duration and the existing thirty-online-minute tolerance rule. Replacing an already active stimulant retains its Jitters consequence. The old `allowRedose` field remains authoring metadata; shared administration no longer uses it to reject replacements.
- Alcohol, opioids and Ketamine are available together. A new dose overlapping another family in this group adds **25 strain once per new dose**, including pending doses. Replacing the same family alone and ended recovery records add no surcharge. Normal self-use, spectator and Echo View activation/input guards remain.
- Milk, Charcoal and Naloxone do not erase accumulated strain or tolerance. Death ends benefits and preserves the pool/history. Online time on the death screen uses inactive decay. Respawn normalization remains ordered after Fabric's attachment transfer and is idempotent.
- Logout and singleplayer pause stop the game clock; there is no offline or wall-clock reduction.

The existing `jugcraft:pharmacology_v1` attachment adds optional `strain_tenths`. Integer tenths avoid floating-point drift: one tenth per active second or two per inactive second. Missing values decode as a legacy sentinel and seed the pool exactly once from known saved dose reservations. Explicit zero stays zero on reload. Recovery/tolerance data and all item/effect IDs are preserved. The dose-list codec expands from eight to 32 bounded records, supporting all twelve current families; replacing a family removes its earlier records. Arithmetic saturates only at the integer storage ceiling, without a gameplay rejection threshold.

Old saves upgrade automatically. Downgrading to the old eight-record/reservation implementation requires restoring a matching backup, particularly after using more than eight families; downgrade migration is not provided.

## Feedback and shared systems

The [Art Batch 6 update](pharmacology-item-art-6.md) replaces the original text bar with ten horizontal pills above the hunger bar. It shows layered strain, with exact strain/state above 100 or while holding a drug at positive strain. The empty row stays for three seconds after reaching zero, then hides even with a held drug or active benefit. Holding a drug at positive strain adds its next cost/total and a patterned preview; tooltips retain the forecast and combination-charge explanation even at zero. Item tooltips forecast the hovered formulation from the same private server snapshot. Held-item phase feedback now shows onset/active time or ready rather than an inert recovery lock.

`StrainStatus` is a bounded numeric server-to-client payload sent only to its owner when data changes. It has no client-to-server mutation route. A weak per-player cache handles disconnect, respawn and server shutdown. The persistent attachment remains server-owned. The original Batch A HUD used the existing font and fill/text rendering. Art Batch 6 adds a small native capsule atlas and dedicated status icons through the same Fabric HUD callback; it adds no UI framework, dependency or world scan. The existing native item-use validation still checks start and finish, and unavailable use spends nothing.

Cosmetic Off and Reduced Motion do not change strain, penalties or the HUD. Overstrain world distortion, other drug visual profiles and the proposed positive-effect upgrades remain later batches.

Canonical labels and the new handbook chapter live in `tools/pharmacology_strain.py`; the existing pharmacy writer includes them after formulation-specific labels. Existing drug guides now describe replacement, decay and combination charges rather than the retired cap/recovery locks.

## Validation

All **77 required pharmacy server checks passed**, including all existing manufacturing checks and twelve new strain checks: active/onset and inactive decay, zero strain with continuing benefits, real item consumption beyond 100, every tier boundary through actual attributes/exhaustion and the firearm spread hook, strongest-penalty selection, replacement/onset/native timer limits, all twelve families, combination charging, cleansing, real damage/death events, codec migration, native player save/logout/rejoin and preservation of independent potions. The prior Echo View regression now checks that returning preserves the exact remaining decayed balance.

The rendered `PharmacologyStrainClientGameTests` passed in an isolated survival test world. Actual item-use input replaces caffeine beyond 100, consumes exactly one tablet and shows synchronized forecasts. It also checks fractional decay with continuing Haste, a Fentanyl forecast including 25 combination strain, real critical penalties with cosmetics disabled, and milk preserving strain while switching to inactive decay. Test-only starting pools make thresholds quick to inspect; production item use performs the crossing. **Five final screenshots were reviewed** at 1280 x 800 / GUI scale 2. An initial screenshot review caught overlap with the native item-status text; the strain HUD was moved higher and the complete client check passed again.

The bounded canonical generation copied and byte-verified **467 runtime resources**, retaining existing generator ownership. New labels and the complete handbook/encyclopedia were independently compared against their canonical builders and runtime copies. Repository structure/documentation links and scoped whitespace checks passed. No manufacturing recipe or asset design change is intended. An additional broad recipe/machine/tag audit was stopped without a result and is not counted as a pass; the complete mod-data audit was not repeated. Final screenshots, logs and SHA-256 evidence are preserved outside Git under `../strain-a-2026-11-03/evidence-final/`. No live deployment, human multiplayer playtest or graphics-mods-off compatibility is claimed; the existing development renderer startup issue is recorded in [drug visuals](pharmacology-visuals.md).

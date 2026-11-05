# Sterile pharmacy — Batch 3

[Improvement Batch C](pharmacology-calm.md) raises Lidocaine to four absorption hearts and adds opioid presentation. Earlier values below record the original delivery.

Current shared strain, replacement and combination rules are recorded in [decaying strain — Batch A](pharmacology-strain.md); the original batch behavior below is historical.

Status: implemented and verified locally. Not deployed.

Owner-directed implementation using OpenAI Codex (GPT-6 family). Extends [pharmacy manufacturing](pharmacology-factory.md) and its shared formed machines. All manufacturing chains and effects are fictional Minecraft abstractions.

## Player outcome

Build an Autoclave and Sterile Filler, extend the existing Analyzer/Separator and use existing Industrial or Bulk Fermenters for a culture chain. Six new medicines provide useful bonuses, with drug-caused drawbacks and existing recovery/tolerance rules. No injury, infection, contamination, combat-triggered ailment or maintenance system is introduced.

| Equipment | Footprint (width × depth × height) | Job |
| --- | --- | --- |
| Autoclave | 2 × 3 × 2 | Eight bottles + 500 mB water → eight Sterile Vials; eight Pharmaceutical Starch + 500 mB water → eight Sterile Carrier. Each 200 ticks at 80 JE/t. |
| Sterile Filler | 2 × 2 × 3 | Two stock + one carrier + four sterile vials + 250 mB water → four medicine vials. Matching reusable formula, 200 ticks at 96 JE/t. |
| Existing Analyzer | unchanged | One prepared stock + one blank plate + 250 mB water → matching formula, 160 ticks at 32 JE/t. Six new recipes. |
| Existing Industrial / Bulk Fermenter | unchanged | One Culture Feed + 500 mB water → 500 mB Azure Antibiotic Broth, 400 ticks at 48 JE/t before bulk efficiency. Product outlet only, no exhaust. |
| Existing Pharmaceutical Separator | unchanged | 500 mB broth → four Antibiotic Extract, 200 ticks at 48 JE/t. The rear extraction inlet now accepts water or broth, without renaming the saved tank role. |

New construction uses existing stainless steel plates, borosilicate glass, electric motor, basic circuit, machine casing and copper cable. The Autoclave uses four plates; the Filler replaces one plate with a piston for a distinct construction recipe. It requires no new pharmaceutical output. The Autoclave has a front load drawer, front output and power, rear water. The Filler has upper front stock/carrier, right-side vial loading, lower front output/power and rear water. Both use the existing Chemical Reactor recipe family, controller, operating profile, upgrade slots, declared transport ports, output reservations, input escrow, cancellation and saved work.

## Manufacturing chains

Blue dye + brown mushroom + sugar crafts two fictional Azure Culture Starter. In the existing compact Chemical Reactor, every following recipe uses 250 mB water and 160 ticks, yielding four output:

| Inputs | Output |
| --- | --- |
| Two Sterile Carrier + one Azure Culture Starter | Culture Feed |
| Two Sterile Carrier + quartz | Lidocaine Game Stock |
| Two Pharmaceutical Base + redstone | Adrenaline Game Stock |
| Four kelp + lapis | Salbutamol Game Stock |
| Four beetroot + amethyst shard | Redleaf Medical Stock, the fictional Morphine input |
| Two Pharmaceutical Base + quartz | Naloxone Game Stock |

Culture Feed → fermenter broth → Separator extract supplies Penicillin. Analyze each stock (Antibiotic Extract for Penicillin; Redleaf for Morphine) to make the corresponding reusable formula. Then load that formula into the Sterile Filler. Formulas are tradeable and never require a finished medicine as their initial sample. The separate sterile-formula tag prevents using early Press plates in the Filler. Bottled outputs consume packaging one-for-one and return only an ordinary glass bottle on use. Sterile products remain stable in storage.

## Effects

| Medicine | Duration / onset | Strain | Benefit and tradeoff |
| --- | --- | --- | --- |
| Penicillin | 20 min / 5 s | 15 | Recovery II: 50% more vanilla natural healing, with proportional food cost. Same family as Paracetamol. |
| Lidocaine | 10 min / 5 s | 10 | Two absorption hearts once per dose; 25% knockback resistance. Shield does not refill on benefit pulses. Vanilla Absorption takes priority. |
| Adrenaline | 45 s / immediate | 35 | Speed II and Strength II. Natural expiry gives 60 s Fatigue: movement −10%, mining −15%. No active redose; 180 s recovery. |
| Salbutamol (Albuterol) | 15 min / immediate | 15 | Ground-sprint exhaustion −50%, water movement acceleration +15%. No water breathing. |
| Morphine | 12 min / 5 s | 35 | Resistance-equivalent 20% damage reduction and 50% knockback resistance. Drowsiness slows movement/mining 15%. |
| Naloxone | immediate utility | none | Ends active opioid benefit and Drowsiness; preserves recovery, strain, tolerance and other drug families. 30 s cooldown. |

Native stronger/longer Speed/Strength potions keep priority. Native Resistance suppresses drug damage reduction; bypass-resistance/effects damage remains unreduced. Aspirin and Morphine choose the better applicable defense. Knockback bonuses choose the best drug, rather than summing Lidocaine and Morphine. Salbutamol changes only the pinned native ground-sprint exhaustion branch; Salts use the better discount, with no multiplication. Water input is changed on both sides through the native movement path for client prediction. Healing costs and unrelated activity exhaustion retain their native paths.

Activated Charcoal additionally clears Adrenaline Fatigue, while preserving dose accounting. It does not clear opioid Drowsiness; Naloxone provides that utility. Milk ends benefits and attached opioid drawback. Death removes drug benefits/drawbacks while preserving per-profile recovery and tolerance. Offline time pauses saved dose accounting. The existing attachment ID and old item/recipe IDs remain stable; the recovery codec range is expanded backward-compatibly to support Adrenaline’s longer tail.

## Assets and integration

Both models are authored geometry composed from the established `starter_chemistry_models.py` / `steampunk_models.py` steel, olive, chrome, rubber, gauge and operating-light kit. They use closed vessels, skids and connected service pipes through the shared port/model slicing writer. Native lights show operation; no moving mechanisms are claimed. Existing item textures and effect icons are referenced or copied without modifying their originals. Broth animations/bucket use the existing deterministic `petro_textures.py` writer and a new blue palette. No new library, UI, transport or animation infrastructure is introduced.

## Validation

Verified locally on 3 November 2026:

- Main and game-test compilation passed using the pinned Minecraft 26.3/Fabric/Java 25 toolchain. No dependency changes.
- **All 29 required server tests passed in one focused run:** twelve original pharmacology cases, nine extended factory cases, and eight sterile cases. All 33 item-output recipes completed with exact inputs, fluid and energy costs and retained formulas. All five pharmacy forms were checked in all four orientations, including declared transport boundaries and full-output blocking.
- Both existing fermenters completed the culture recipe with exactly 500 mB broth and no exhaust; the Separator recovered four extract. Sterile tests also passed for saved Filler work and formula locking, distinct native construction recipes, reachable stocks without recipe interception, native food healing and sprint cost, one-time absorption/potion priority, extended recovery serialization, and targeted Naloxone reversal.
- **Rendered `PharmacologySterileClientGameTests` passed.** A native world-ticked Filler produced exactly four Penicillin vials and retained its formula. Wrong-formula status synchronized to the client. Six screenshots were inspected: front/back of both machines and the two Filler screen states. Local evidence is in the workspace artifact `sterile-pharmacy-2026-11-03/evidence/`.
- Complete resource generation passed, followed by a bounded final recipe/guide refresh. All 152 refreshed runtime resources were byte-verified against source before gameplay, including both machines’ final models.
- Complete `tools/check_mod_data.py` audit finished. Its sole finding was that the new fluid’s Java registration was earlier than its generator definition. The registration was appended to preserve earlier order, and the complete `check_petro()` component passed its targeted recheck. The other full-audit components passed: 15,342 registered material IDs; art validation covered 38,371 block/item models, 3,777 quad parts and 182 composed models. The full script was not repeated after this registration-only correction. Source recipes, textures and models were unchanged by that correction.
- Icon-map checks passed (285 maps, 2,018 icons, 28 existing warnings), repository structure/documentation links passed, and `git diff --check` passed.

The rendered-client run preceded the registration-only correction; all 29 server tests passed again after recompiling that final correction. No live server changes, human playtesting, two independent clients, separate-process restart, load measurement or release readiness is claimed.

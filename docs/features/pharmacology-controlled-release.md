# Controlled release and premium finishing — Batch 5

[Improvement Batch B](pharmacology-focus.md) adds real aimed accuracy to Methylphenidate XR and sustained-work benefits to Modafinil XR. Earlier effect descriptions below record the original delivery.

Current shared strain, replacement and combination rules are recorded in [decaying strain — Batch A](pharmacology-strain.md); the original batch behavior below is historical.

Status: implemented and verified locally. Not deployed.

Owner-directed implementation using OpenAI Codex (GPT-6 family). Extends [precision pharmacy and alcohol](pharmacology-precision.md), the shared formed-machine controller and the existing player attachment. All ingredients, effects and process parameters are fictional Minecraft abstractions.

## Products

| Formulation | Benefits | Duration / onset | Strain | Drug-caused cost |
| --- | --- | --- | --- | --- |
| Caffeine XR | Haste I; +10% walking | 30 min / 30 s | 10 | Longer commitment to the stimulant family |
| Methylphenidate XR | Haste II; 25% less supported aimed-gun hand sway | 40 min / 30 s | 30 | Activity food exhaustion ×1.2 |
| Amphetamine XR | Haste II; Speed I | 35 min / 30 s | 40 | Activity food exhaustion ×1.3; two minutes of existing Stimulant Fatigue after natural expiry |
| Modafinil XR | Haste I; 15% less supported aimed-gun hand sway | 60 min / 30 s | 25 | No sleep reset |
| Fentanyl Patch | 20% applicable damage reduction; +50% knockback resistance | 20 min / 30 s | 40 | Opioid Drowsiness: −10% movement/mining; 180 s recovery |
| Alcohol (Masterwork Reserve) | +30% melee damage; +60% knockback resistance | 25 min / 20 s | 60 | Intoxication: −15% movement and +35% supported gun spread; 180 s recovery |

Every new formulation prohibits active redosing, including its final quarter. XR products share the existing stimulant family and 120-second recovery with their immediate-release counterparts. All retain the common 75%/50% repeated-duration penalties and online timers. The patch shares opioid tolerance, exclusions and existing Naloxone reversal; Masterwork shares all alcohol grades' tolerance and incompatibility with opioids/Ketamine. Native potion protection and equivalent drug bonuses choose the best result rather than multiplying. Strain remains capped at 100. No new ledger, injury condition, armor enchantment, illness, spoilage or medical upkeep is added.

## Materials and shared tooling

| Inputs | Output | Producer |
| --- | --- | --- |
| Four starch + four paper + Plastic Pellets | Four Release Matrix Blend | Crafting table |
| Four blend + 500 mB water | Four Release-control Matrix | Existing Press, 200 ticks at 96 JE/t; release die |
| Four matrix + two Silicone Rubber + amethyst + 250 mB water | Four Advanced Release-control Matrix | Same Press, 200 ticks at 96 JE/t; release die |
| Two starch + two paper + 250 mB water | Eight Empty Pharmacy Capsules | Same Press, 200 ticks at 96 JE/t; release die |
| Two wool of any ordinary color + paper + rubber | Four Patch Backing | Crafting table |

A reusable **Release Control Die** costs four Stainless Steel Plates, amethyst, two Advanced Circuits, a Precision Process Module and Silicone Rubber. It fits the Coater release socket and an existing Analyzer/Press upgrade slot. It provides no speed or efficiency bonus. Existing machine slot counts, saved indices, formula sockets and normal upgrades are preserved. An ordinary precision module cannot substitute for the die. The process-tool slot locks during work, including across saving/loading.

## Release manufacturing

The new **3 × 3 × 3 Release Coater** uses the existing chemical-processing family with a dedicated capability, three solid inputs, one coated-stock output, a rear water input, a release socket and two normal upgrade slots. Construction uses two stainless plates, Borosilicate Glass, an Electric Motor, an Advanced Circuit, a Polymer Housing, two Precision Stainless Valves and the die. Its common electrical buffer can require replenishment during advanced jobs; continuous power uses the existing power network.

All coating jobs use **four matrices and 500 mB water**, take **300 ticks at 128 JE/t** before ordinary upgrades/warmup, retain the die, and produce four intermediates:

| Product | Active stock per four intermediates | Matrix / additional input |
| --- | --- | --- |
| Caffeine XR | 24 Caffeine Concentrate | Four ordinary matrix |
| Methylphenidate XR | Four Verified Methylphenidate Active | Four ordinary matrix |
| Amphetamine XR | Six Verified Amphetamine Active | Four ordinary matrix |
| Modafinil XR | Four Verified Modafinil Active | Four advanced matrix |
| Fentanyl Patch | Eight Verified Fentanyl Active | Four ordinary matrix and four patch backing |

Extra active pays for duration: six concentrates per Caffeine XR capsule, twice the active per Methylphenidate/Modafinil capsule, three times per Amphetamine capsule and four times per patch compared with the earlier four-dose tablet/vial batches. A single finished drug is never multiplied into long doses. Earlier coffee, starch, plastics, reactors and separators retain demand.

The **existing Analyzer** consumes one matching coated core/patch stock, a Blank Formula Plate and 250 mB water, keeping the die in an upgrade slot, to discover its formula: 200 ticks at 96 JE/t. No finished XR capsule or patch is needed for the first formula.

The **existing Press** uses four cores, four empty capsules and 250 mB water for four XR capsules, or four patch stock, one Sterile Carrier, four paper and 250 mB water for four patches. Both take 200 ticks at 96 JE/t and keep the matching formula in the ordinary formula socket. Capsules consume their shell. Patches are self-applied using a neutral native use animation and leather-equipment sound. Neither returns a bottle.

Missing/wrong tools and full outputs stop before input reservation. Power loss pauses escrowed work. Cancellation returns original solids and process fluids; breaking drops the original solids and tooling, with loose fluids following the existing machine-loss rules. Neither creates finished medicine. Common output reservations, physical ports, tool locks, saving/loading and automation remain in use. No psychedelic, dream research, elven material, dimension or cold-storage gate is introduced.

## Masterwork beverage branch

Masterwork Reserve uses advanced shared beverage conditioning, with **no medical release coating**:

1. Four High-Proof Spirits Concentrate + two honeycomb + gold nugget + sugar craft four Masterwork Blend.
2. The existing Separator consumes four blend and an extra 1,000 mB Refined Beverage Stock, keeping a Precision Process Module, for four Unconditioned Masterwork Concentrate: 300 ticks at 128 JE/t.
3. Craft a Masterwork Conditioning Insert from four stainless plates, amethyst, two Advanced Circuits, the earlier Oak Conditioning Insert and Silicone Rubber.
4. The existing Mash and Conditioning Barrel consumes four unconditioned concentrate and two sweet berries, keeping the new insert, for four Masterwork Reserve Concentrate: **4,800 loaded ticks**. Its earlier insert cannot authorize the new grade. No electricity or offline ageing is introduced; original cider behaviour remains unchanged.
5. Analyze one concentrate, a blank plate and 250 mB water, keeping the ordinary precision module, for its formula: 200 ticks at 96 JE/t.
6. The existing Press bottles four concentrate, sugar and four bottles into four Masterwork Reserve, retaining that formula: 160 ticks at 128 JE/t.

Intermediate analysis consumes a sample. No reverse conversion or repeated bottling increases potency for free. This completes the five-grade progression from Moonshine through High-Proof Spirits to Masterwork Reserve.

## Art and integration

Coater geometry is authored in `tools/controlled_release_models.py` using the existing `starter_chemistry_models.py`, `steampunk_models.py`, native part writer and port overlays. Muted silver-grey cladding, olive controls, dark steel, a supported closed drum/yokes, motor, doors and connected water plumbing follow the shared late-industrial palette. Existing runtime item textures provide the capsule, stock, tooling and effect icons; source library assets stay unchanged. No new dependency, screen/backend, fluid registration or player animation system is introduced.

## Verification

- Common Java and game-test sources compiled on the pinned Java 25 / Minecraft 26.3 toolchain, without dependency changes.
- **All 53 required pharmacy server checks passed:** the earlier 42 checks plus 11 Batch 5 checks. All **82 item-output pharmacy/beverage recipes** ran with exact inputs, outputs, tool retention and paid energy, supplying additional power where a job exceeds one electrical buffer. All eight formed pharmacy/beverage machines were checked in every horizontal orientation.
- New checks covered independent matrix/backing acquisition, exact coating costs, old-module rejection, die locking in unchanged Analyzer/Press layouts, saved work, power interruption, cancellation and far-part breaking; exact XR effects/onsets/food costs, no active redose, 60-minute codec roundtrip, death/tolerance, existing fatigue/Charcoal, patch damage/mining/movement, Naloxone, shared opioid tolerance, fifth-grade alcohol and compatibility through recovery, and loaded-only premium conditioning.
- **Rendered `PharmacologyReleaseClientGameTests` passed**, then passed again after the model alignment correction. A world-ticked Coater made exactly four cores and retained its die; its standard electrical store was replenished once during the powered fixture. The Press produced eight capsule shells, retained its die in the existing upgrade slot and synchronized its lock. Patch and fifth-grade Intoxication levels reached the client. Eight final screenshots cover front/back, wrong-tool/working Coater screens, the Press, premium conditioning and the new effects; they were inspected and copied with logs/checksums to the workspace artifact `batch5-pharmacy-2026-11-03/evidence/`.
- Canonical writers generated the bounded Batch 5 delta; **159 runtime resources were copied and byte-verified** over the existing complete test resources. Unchanged project assets were not regenerated and `processResources` was excluded from focused runs. The final registry contains **15,416 material IDs**. Fresh machine metadata, handbook and encyclopedia checks passed after the last model/guide refresh.
- Icon checks passed: 285 maps and 2,021 physical item icons, with the same 28 existing warnings. Repository structure/documentation links and `git diff --check` passed.
- **The complete data/art audit passed with zero errors**, including the final whole-tree JSON parse and **38,452 models, 3,777 quad parts and 182 composed models**. The final metadata/guide checks also ran against fresh imports after the model and wording corrections.

No live server changes, human playtesting, two independent clients, separate-process restart, representative load measurement or release readiness is claimed. Psychedelic exploration and Clockwork travel remain Batches 6–7.

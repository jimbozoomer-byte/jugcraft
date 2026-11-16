# Precision pharmacy and alcohol — Batch 4

[Improvement Batch C](pharmacology-calm.md) extends Echo View to 24 blocks, adds body guidance, and adds opioid/Ketamine visuals. Earlier scouting values below record the original delivery.

[Improvement Batch B](pharmacology-focus.md) adds real aimed accuracy to Methylphenidate and sustained-work benefits to Modafinil, including their XR variants. Earlier effect descriptions below record the original delivery.

Current shared strain, replacement and combination rules are recorded in [decaying strain — Batch A](pharmacology-strain.md); the original batch behavior below is historical.

Status: implemented and verified locally. Not deployed.

Alcohol's later world presentation and local accessibility controls are recorded in [drug visuals](pharmacology-visuals.md).

Owner-directed implementation using OpenAI Codex (GPT-6 family), extending [sterile pharmacy](pharmacology-sterile.md). All recipes, effects and numeric values are fictional Minecraft abstractions. No injury conditions or combat-triggered ailments are added.

## Alcohol progression

| Product | Melee damage / knockback resistance | Duration / onset | Strain | Movement / firearm spread | Recovery |
| --- | --- | --- | --- | --- | --- |
| Moonshine | +10% / +20% | 8 min / 10 s | 20 | −5% / +15% | 120 s |
| Refined Spirits | +15% / +30% | 12 min / 10 s | 30 | −7.5% / +20% | 120 s |
| Reserve Whiskey | +20% / +40% | 16 min / 10 s | 40 | −10% / +25% | 180 s |
| High-Proof Spirits | +25% / +50% | 20 min / 15 s | 50 | −12.5% / +30% | 180 s |

All four share the `intoxicant` family, its 75%/50% repeated-duration rule and online tolerance clock. Cross-grade switching cannot bypass onset, active use or recovery. T1/T2 permit the existing final-quarter same-product redose; T3/T4 prohibit active redosing. Alcohol melee bonuses choose the better result against native Strength rather than multiplying; drug knockback bonuses choose the best one before the normal equipment cap. Existing cider and food retain their behavior.

| Stage | Exact batch | Equipment and cost |
| --- | --- | --- |
| Empty packaging | Five bricks → two Empty Mash Jugs | Crafting table |
| Farm mash | Four wheat **or** corn + two sugar + one water bucket + empty jug → Mash Jug; returns bucket | Crafting table |
| Fermentation | Mash Jug → Fermented Mash Jug | New 2 × 2 × 2 Mash and Conditioning Barrel; 1,200 loaded ticks; no electricity |
| Early bottling | Fermented Mash Jug + four empty bottles → four Moonshine + Empty Mash Jug | New 2 × 2 × 2 Moonshine Still; 200 native furnace-fuel ticks; no power connection |
| Beverage culture | Sugar + brown mushroom + paper → two Beverage Culture Packs | Crafting table |
| Bulk wash | Culture Pack + 1,000 mB existing Grain Mash → 1,000 mB Beverage Wash | Existing Industrial / Bulk Fermenter; 400 ticks, base 48 JE/t; no exhaust |
| Refined stock | 1,000 mB Beverage Wash → 500 mB Refined Beverage Stock + 500 mB Stillage | Existing Chemical Distillation Station; 240 ticks, 64 JE/t |
| Refined concentrate | Two sugar + 1,000 mB Refined Beverage Stock → eight concentrate | Existing Pharmaceutical Separator; 200 ticks, 48 JE/t |
| Bulk Moonshine | One refined concentrate + sugar + four bottles → four Moonshine | Existing Formulation Press; reusable Refined Spirits Formula Plate; 160 ticks, 64 JE/t |
| Reserve concentrate | Four refined concentrate + honeycomb → four reserve concentrate | Mash and Conditioning Barrel; reusable Oak Conditioning Insert; 2,400 loaded ticks |
| High-proof concentrate | Four reserve concentrate + 500 mB Refined Beverage Stock → four high-proof concentrate | Pharmaceutical Separator; reusable Precision Process Module; 200 ticks, 80 JE/t |
| Formula discovery | One matching concentrate + Blank Formula Plate + 250 mB water → formula | Existing Analyzer; 160 ticks, 32 JE/t; high-proof also requires the precision module |
| Later bottling | Four concentrate + sugar + four bottles → four matching drinks | Existing Formulation Press; matching reusable formula; 160 ticks, 64 JE/t |

The rustic forms use the common formed-machine recipe, escrow, cancellation, persistence and port system. The barrel has no electrical store or upgrades. The still's last input is reserved for native furnace fuel, with no JE storage or conversion. Fuel containers return once in that slot. Banked fuel ticks survive saving and are spent only during actual processing; waiting, idle and paused machines spend none. Cancel returns original process inputs without refunding spent fuel or producing finished drinks. Loaded-time conditioning leaves the original Cider Barrel's ageing backend untouched.

The steel beverage line uses distinct fluids; industrial `bioethanol` is never bottled into a drink. Reserve conditioning requires stainless parts for its insert; high-proof processing additionally requires the precision module. Masterwork Reserve remains in Batch 5.

## Precision manufacturing

Build one Precision Process Module from four Stainless Steel Plates, quartz, two amethyst shards, an Advanced Circuit and a Precision Stainless Valve. It is a reusable process tool in an existing formed-machine upgrade slot and contributes no speed/efficiency bonus. Its slot locks during a job. Existing machine inventories and saved slot indices are retained.

1. Compact Chemical Reactor: four Pharmaceutical Base + oak log + 500 mB water → 500 mB Organic Process Stock, 200 ticks.
2. Craft one Pharmaceutical Base with the pair below into one matching Game Reagent Blend.
3. Existing Bulk Chemical Reactor: blend + 500 mB Organic Process Stock → four matching Game Stock, 240 ticks, 96 JE/t before the machine's normal efficiency; reusable precision module. Its existing single solid input and liquid inlet are sufficient; the gas tank stays empty.
4. Existing Pharmaceutical Separator: four stock + 250 mB water → four Verified Active, 200 ticks, 64 JE/t; reusable module.
5. Existing Laboratory Analyzer: one active + Blank Formula Plate + 250 mB water → matching formula, 160 ticks, 48 JE/t; reusable module. Discovery requires no finished drug.
6. Existing Formulation Press: two active + Pharmaceutical Starch + 250 mB water → four tablets; or existing Sterile Filler: two active + Sterile Carrier + four Sterile Vials + 250 mB water → four vials. Both retain the matching formula, take 200 ticks and use 96 JE/t.

| Drug | Fictional blend pair | Benefit | Duration / onset / strain | Drug-caused cost |
| --- | --- | --- | --- | --- |
| Methylphenidate | Lapis + redstone | Haste II; 25% less supported aimed-gun hand sway | 20 min / 20 s / 35 | Activity food exhaustion ×1.2 |
| Amphetamine | Blaze powder + sugar | Haste III; Speed II | 15 min / 20 s / 55 | Activity food exhaustion ×1.5; two minutes of Stimulant Fatigue after natural expiry |
| Modafinil | Quartz + glowstone | Haste I; 15% less supported aimed-gun hand sway | 40 min / 20 s / 25 | Existing stimulant family, tolerance and recovery; no sleep reset |
| Ketamine | Amethyst + ender pearl | Echo View scouting | Up to 30 s / immediate / 40 | Vulnerable body; no active redose; 120 s recovery |
| Fentanyl | Quartz + redstone | 40% applicable damage reduction; +75% knockback resistance | 8 min / 5 s / 55 | Opioid Drowsiness: −20% movement/mining; no active redose; 180 s recovery |

The first three share the existing stimulant family with caffeine and Stimulant. Native potion priority remains in effect. Aimed hand sway scales the two native look-lag rotations while using a supported Jugcraft gun; it does not tighten shot spread or remove recoil. Alcohol's drawback uses the existing server firearm spread hook.

Fentanyl shares Morphine's opioid family and Naloxone reversal. Damage reduction compares against native Resistance, including weaker/equal/stronger potions, and honors resistance/effect bypass damage tags. Naloxone removes opioid benefit and Drowsiness while preserving strain, family tolerance and the formulation's recovery. Saved Batch 3 Morphine effects keep their 15% movement/mining penalty after the shared Drowsiness amplifier change.

## Echo View and compatibility

Echo View starts while grounded (or already flying with creative abilities), with no mount, sleeping or open container. It reuses the existing detached scrying camera and input guards. The server accepts direction input at most every two ticks and decides position, collision and the 12-block radius. The camera travels at three blocks/second through loaded, ordinarily accessible space. No wall/ore reveal, spectator mode, teleportation, remote interaction, attacks, inventory movement or forced chunk loading is provided. The player's chunk center and body remain at the origin.

Sneak, opening a menu, taking damage, cleansing the effect, death, leaving the world, dimension changes or expiry end the view. The normal camera returns; no duplicate entity body or inventory is created. Rejected inventory actions resynchronize the client, including predicted item drops. Gun trigger, reload, bayonet and spin actions use the same remote-view guard as native interactions and Nouveau controls. Unused viewing time cannot be banked on reconnect. Recovery and tolerance remain on the existing online clock.

Alcohol, opioids and dissociatives are mutually incompatible with each other during onset, active use and recovery, checked before starting and before finishing consumption. Rejection spends no item or container. All items are self-use. The existing attachment ID and dose cap of eight remain; family-history decoding expands from eight to sixteen entries, accepting old saves. Total strain remains 100.

## Verification

- Common Java, client Java and game-test sources compiled on the pinned Java 25 / Minecraft 26.3 setup, without dependency changes.
- **All 42 required pharmacology server tests passed:** the earlier 29 checks plus 13 precision/beverage checks. All 60 item-output pharmacy/beverage jobs ran with exact input, output, reusable-tool and energy accounting. Additional checks covered both bulk fermenters, beverage distillation, five precision reactor jobs, every alcohol grade, shared tolerance/compatibility, native Resistance priority, existing Naloxone, old Morphine Drowsiness, saved fuel/work, pause, cancel, breaking, returned containers and ten saved family histories.
- Echo checks covered grounded activation, correct strafe direction, rate limiting, the 12-block bound, ordinary walls, a stationary vulnerable body, native drop packets, custom gun trigger/reload rejection, cleansing and retained recovery. No injury condition is introduced.
- **Rendered `PharmacologyPrecisionClientGameTests` passed on the final implementation.** The actual still finished four Moonshine and returned its jug. Fuel waiting/processing synchronized to the screen. Native forward/right/sneak/drop controls, predicted-inventory correction, first-frame camera position, normal-camera return and supported aimed-gun focus passed. Eight screenshots cover both forms front/back, two still screens, Echo View and focused aiming. They are preserved with logs and checksums in the workspace artifact `batch4-pharmacy-2026-11-03/evidence/`.
- Full resource generation completed. The focused runtime reused the complete existing resource tree with **233 changed/new resources copied and byte-verified**, excluding the expensive unchanged `processResources` step. The final data set contains **15,388 registered material IDs**.
- The **complete data audit ran through every phase and the final JSON parse**. Its sole reported error was capability ordering between the Bulk Chemical Reactor's Java and Python descriptors. That order was corrected, then all affected recipe, machine, fluid and acquisition-gate checks passed with zero errors, including the added bulk Moonshine job. The full script was not repeated after that descriptor-only correction. All other full-audit phases passed; art validation covered **38,397 models, 3,777 quad parts and 182 composed models**.
- Icon checks passed: 285 maps and 2,021 item-icon sizes, with the same 28 existing warnings. Repository structure/documentation links and `git diff --check` passed.

No live server changes, human playtesting, two independent clients, separate-process restart, load measurement or release readiness is claimed. Masterwork Reserve, Fentanyl Patch and release coating remain Batch 5; psychedelic exploration and Clockwork travel remain Batches 6–7.

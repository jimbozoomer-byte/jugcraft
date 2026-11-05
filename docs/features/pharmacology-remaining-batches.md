# Pharmacology — remaining batches

The owner's November 4 publication checkpoint and open texture/effects, Clockwork realm/trading, balance and validation work are tracked in the [central TODO list](../TODO.md). Current manufacturing, art and visual-improvement batches are implemented; original Clockwork Batch 7 remains future work. The historical design tables below are not the current dosing specification.

## November 3 improvement pass

The shared visual controls and alcohol presentation are implemented in [drug visuals](pharmacology-visuals.md). The owner selected the following next pass, separately from the original manufacturing batches:

| Batch | Selected scope | Status |
| --- | --- | --- |
| A | Decaying strain, soft threshold, real penalties, replacement, combination costs, HUD and save migration | [Implemented and verified locally](pharmacology-strain.md) |
| B | Overstrain warning visuals; caffeine/stimulants/Adrenaline presentation; useful Methylphenidate accuracy and Modafinil work benefits | [Implemented and verified locally](pharmacology-focus.md) |
| C | THC, opioids and Ketamine visuals; body-direction indicator; four-heart Lidocaine and 24-block scouting | [Implemented and verified locally](pharmacology-calm.md) |
| D | Psilocybin/Mescaline visual identities and working authored study/navigation cues | [Implemented and verified locally](pharmacology-dream-visuals.md) |
| E | LSD/DMT advanced geometry, trails, readable pattern reveals and Clockwork glimpses | [Implemented and verified locally](pharmacology-reverie-visuals.md) |

### Owner review and later visual polish

On 3 November 2026, after trying the local world, the owner judged the current visual effects acceptable and asked to improve them later. Keep that as a deferred polish pass: stronger individual identities where useful, smoother/more convincing afterimages, richer Clockwork imagery/depth, and renewed readability/accessibility/composition checks with native screenshots or clips. The current effects remain the baseline; this is not a request to change them during the item-art work.

The owner's next selected scope is [distinct pharmacy item art](../plans/pharmacology-item-art/README.md): unique recognizable finished drugs, coordinated manufacturing items, packaging, formulas, crops and HUD icons. The [six art batches](../plans/pharmacology-item-art/batches.md) give exact item assignments and completion criteria. [Art Batch 1](pharmacology-item-art-1.md) delivers 17 finished items; [Art Batches 2–4](pharmacology-item-art-234.md) add 97 finished/supply/material textures and LSD paper packaging; [Art Batch 5](pharmacology-item-art-5.md) adds all 32 formula plates. [Art Batch 6](pharmacology-item-art-6.md) adds all 39 effect icons, the ten-pill strain HUD above the hunger bar (including three-second zero hiding) and the completed nine-model/catalog review. All six art batches are implemented and verified locally.

The original Clockwork dimension and trading work remains Batch 7. Item art and the deferred visual-effects polish are separate scopes. The historical manufacturing/dose tables below describe their original delivery; current shared dosing rules are in [decaying strain](pharmacology-strain.md).

Status: Batches 4–6 are implemented locally; see [precision pharmacy and alcohol](pharmacology-precision.md), [controlled release](pharmacology-controlled-release.md), and [Overworld perception and botanical pharmacy](pharmacology-psychedelics.md) for final chains and verification. Batch 7 remains planned. Updated 3 November 2026 at the owner's request. Prepared with OpenAI Codex (GPT-6 family).

[Early pharmacy](pharmacology-early-pharmacy.md), [factory production](pharmacology-factory.md) and [sterile medicine](pharmacology-sterile.md) are implemented locally. This plan adds work to remaining delivery batches without reopening those completed batches. Technology tier describes when players can acquire something; delivery batch describes when we implement it. All numbers below are proposed Minecraft balance values, not real drug properties, doses or manufacturing instructions.

## Delivery order

| Batch | Scope | New additions |
| --- | --- | --- |
| **4A — early alcohol branch** | Low-tech fermentation, fueled Moonshine Still, bottled Alcohol and compatibility rules, then powered/refined beverage upgrades | T1 Moonshine, T2 Refined Spirits and T3 Reserve Whiskey. The first still is accessible at early iron/bronze technology despite delivery after Batch 3. |
| **4B — precision pharmacy** | Shared precision reaction, separation and stock analysis; Methylphenidate, Amphetamine, Modafinil and specialist formulations | Ketamine, Fentanyl and T4 High-Proof Spirits. Reuse the Analyzer, Pharmaceutical Separator and Sterile Filler; alcohol uses beverage finishing. |
| **5 — controlled release and premium finishing** | Release Coater, release-control materials, longer/lower-peak medicines and advanced beverage conditioning | Fentanyl Patch alongside the planned extended-release work/focus medicines; T5 Masterwork Reserve on the beverage branch. Alcohol does not use medical release coatings. |
| **6 — psychedelics (implemented locally)** | Overworld perception, authored dream puzzles and psychedelic manufacturing | THC, Psilocybin, Mescaline, LSD and DMT; three cultivated crops; shared processing jobs; a Dream Folio and paid Memory/Mirror/Reverie lessons. Ketamine's basic scouting remains available independently. |
| **7 — Clockwork Elves** | Return anchors, actual dimension travel, visit accounting and trading | Existing Clockwork visit/trade scope. No moonshine or first precision medicine requires elven materials. |

Batch 4A and 4B are two implementation sections of the same delivery batch. Finish and verify the low-tech production/accounting work before the precision section, then run shared regression checks once the complete batch is ready.

## Alcohol and the Moonshine Still — Batch 4A, early technology

**Role:** inexpensive preparation for melee expeditions and a useful farming trade product, scaling into stronger premium drinks as technology improves. All five alcohol grades share one `intoxicant` family. The first is **Alcohol (Moonshine)**. Existing food and cider items do not automatically become drugged items.

| Formulation | Positive effect | Duration / onset | Strain | Drug-caused drawback |
| --- | --- | --- | --- | --- |
| Alcohol (Moonshine) | +10% melee damage and +20% knockback resistance | 8 minutes / 10 seconds | 20 | Intoxication: 15% more supported weapon aim spread and 5% slower movement while active. Optional visual distortion can be disabled without changing mechanics. |

Knockback protection uses the better drug bonus, rather than adding to Lidocaine or Morphine. Repeat dosing follows the shared 75%/50% duration and online tolerance rules. Recovery lasts 120 seconds. No injury, fear, temperature or pain condition is required to gain the buff. No mandatory vomiting, random poisoning, blackouts, involuntary teleportation or forced camera sway.

### Machines and ingredients

- **Mash fermentation:** extend the existing Cider Barrel machinery with an explicit mash-processing mode or shared barrel variant. Reuse its container, fill/draw and progress presentation where appropriate; preserve its original cider recipes, saved contents and ageing behavior. New mash jobs count loaded processing time and do not inherit offline ageing or gain progress while carried as an item.
- **Moonshine Still:** a compact proposed **2 × 2 × 2** copper pot, closed coil condenser, iron/bronze supports and ceramic collection vessel. Built from ordinary copper, iron/bronze, ceramic, glass and wood-era materials. It burns ordinary furnace fuel directly, with no generator, motor, electricity, advanced circuit, stainless steel or pharmaceutical prerequisite.
- The still has feed, empty-bottle and fuel inputs, plus drink and returned-container outputs. The first job is designed for manual use; automation can use the shared storage/port rules later. Fuel should extend the common fueled-processing support for this fluid/container job, rather than creating a hidden electric generator or a second unrelated distillation backend.

### Fictional game chain

1. Renewable wheat **or** corn, sugar, water and a reusable clay jug make a **Mash Jug**. Return the water bucket once during preparation.
2. The barrel processes the jug into **Fermented Mash Jug**. Starting balance target: 60 loaded seconds per jug; no real fermentation settings.
3. One fermented jug, four empty glass bottles and a paid fuel job in the Moonshine Still produce **four Alcohol (Moonshine)** bottles and return the empty jug once. Starting job target: 200 ticks. Reserve both output stacks before spending mash, bottles or fuel.
4. Drinking returns one ordinary glass bottle. Cancellation/breaking returns only untransformed escrowed inputs; spent fuel is not refunded and no partial drinks appear. Saved work cannot duplicate the jug or bottles.

Jug servings and batch yields are game abstractions. There are no player-entered temperatures, strengths, collection cuts or real distillation procedures. Construction counts, actual existing material IDs and fuel arithmetic must be finalized against the shared backend during implementation.

At steel technology, extend existing Mashing Station / Industrial Fermenter / Chemical Distillation Station equipment with beverage jobs for bulk Moonshine **and distinct, stronger alcohol grades**. Bulk production of an existing grade improves throughput only; producing a higher grade requires its own upgraded process, inputs and cost. Existing `bioethanol` is industrial process/fuel stock and never becomes a drink by putting it into a bottle. No medicine or industrial alcohol is needed to build the first still, and the industrial route is not required to use it.

### Alcohol progression across all five technology tiers

All values are provisional game balance. Grade names describe fictional product quality/potency, not real alcohol concentrations or a guide to achieving them.

| Technology tier / delivery | Product | Positive buffs | Duration / onset | Strain | Intoxication drawback |
| --- | --- | --- | --- | --- | --- |
| **T1 / Batch 4A** | **Moonshine** | Melee damage +10%; knockback resistance +20% | 8 min / 10 s | 20 | Weapon spread +15%; movement −5% |
| **T2 / Batch 4A** | **Refined Spirits** | Melee damage +15%; knockback resistance +30% | 12 min / 10 s | 30 | Weapon spread +20%; movement −7.5% |
| **T3 / Batch 4A** | **Reserve Whiskey** | Melee damage +20%; knockback resistance +40% | 16 min / 10 s | 40 | Weapon spread +25%; movement −10% |
| **T4 / Batch 4B** | **High-Proof Spirits** | Melee damage +25%; knockback resistance +50% | 20 min / 15 s | 50 | Weapon spread +30%; movement −12.5% |
| **T5 / Batch 5** | **Masterwork Reserve** | Melee damage +30%; knockback resistance +60% | 25 min / 20 s | 60 | Weapon spread +35%; movement −15% |

This replaces the earlier throughput-only limit: later tiers improve both strength and duration. Moonshine remains cheaper, lower strain and easier to combine with other compatible medicines. Rum or other flavor variants can later share a grade's fixed profile instead of introducing another strength or tolerance system.

| Grade | Fictional manufacturing progression | Reused machinery / additional requirement |
| --- | --- | --- |
| Moonshine | Farm mash → barrel processing → fueled small-batch bottling | Low-tech Mash Barrel mode and Moonshine Still; no electricity |
| Refined Spirits | Better prepared beverage mash → controlled beverage fermentation → refined beverage stock → filtration and bottling | Existing Mashing Station, Industrial Fermenter, Chemical Distillation Station and suitable filtering capability; steel-era power |
| Reserve Whiskey | Refined beverage stock + oak-conditioning input + selected flavor stock → paid barrel-conditioning job → reserve bottling | Stainless-capable shared separation, plus an upgraded mode/insert for the existing barrel family; no new barrel backend |
| High-Proof Spirits | Dedicated beverage stock + prepared botanical/flavor concentrate → precision beverage finishing → verified bottling | Batch 4 precision tooling on existing separation/distillation machinery; no sterile injection or pharmaceutical release coating |
| Masterwork Reserve | Extra high-grade beverage stock + premium blending/conditioning input → advanced conditioning → masterwork bottling | Batch 5 advanced control and barrel-conditioning tooling, with shared analysis and bottling; no elven material required |

Exact ingredient IDs, input counts, yields, loaded processing times and energy costs are finalized during implementation. Higher grades consume additional farm stock and processing per serving, not just the same bottle passed around a loop. Conditioning uses loaded job progress, preserves paid work across saves, and cannot progress or duplicate contents while carried as an item. Original cider ageing remains unchanged. All grades consume packaging one-for-one and return one ordinary glass bottle when drunk.

**Shared dosing:** one active alcohol grade only; no adding their buffs together and no switching grade during onset, active use or recovery. Every grade shares alcohol tolerance, so downgrading or changing flavor does not reset 75%/50% repeat-duration penalties. T1/T2 retain same-profile final-quarter redosing and 120-second recovery. T3–T5 have **no active redose** and **180-second recovery**. The 100-point strain limit and Alcohol ↔ opioid/Ketamine exclusions apply to every grade, including recovery.

Equivalent damage and knockback sources choose the stronger applicable benefit instead of adding another multiplier. Use one visible Intoxication state with grade-dependent penalties, not five separate upkeep meters. Optional visual effects remain reducible/disableable. No grade adds injury conditions, random poisoning, blackouts or involuntary actions.

## Ketamine — Batch 4B, precision technology

Ketamine was already named in the earlier technology-tree proposal. This assigns that existing entry to Batch 4 and defines a standalone use before dream puzzles or Clockwork travel exist.

| Formulation | Positive effect | Duration / onset | Strain | Boundary / drawback |
| --- | --- | --- | --- | --- |
| Ketamine | **Echo View:** one controllable scouting viewpoint within 12 blocks of the player's body | Up to 30 seconds / immediate after use | 40 | Body stays at its origin and remains vulnerable; taking damage ends the view. No new injury debuff is applied. |

The view can scout around corners in ordinary reachable space. It cannot pass through normal solid blocks, reveal ores, attack, interact with inventories, move items, break blocks, teleport the body or load distant chunks. Later authored dream barriers may explicitly permit view passage; those barriers are optional content, not a prerequisite for the first useful dose. Ordinary blocks behind walls stay hidden. The server owns the origin, range, collision and end condition. Cancellation, milk, death, logout or dimension changes restore the player's normal camera and controls; no duplicate body or inventory is created.

No active redose. The unused remainder of a view is not banked or restored on reconnect. Completion/cancellation begins 120 seconds of recovery; offline time pauses recovery and tolerance. Shared repeat-duration reduction applies. Accessibility settings may reduce dream visuals without changing scouting information. This is a short exploration tool, not a passive damage buff or a Clockwork entry shortcut.

**Fictional chain:** reachable plant/resin/pharmaceutical inputs → **Organic Process Stock** → shared Precision Reactor capability with amethyst and an ender pearl → **Ketamine Stock** → existing Pharmaceutical Separator with precision tooling → **Verified Ketamine Active** → Analyzer sample plus Blank Formula Plate → reusable **Ketamine Formula Plate** → existing Sterile Filler with sterile carrier and four sterile vials → **four Ketamine**.

Implementation uses the Bulk Chemical Reactor's existing single solid input: craft the Minecraft ingredient pair with Pharmaceutical Base into one reagent blend, then process it with Organic Process Stock. A reusable precision module fits an existing upgrade slot without changing saved inventories. The feature record specifies exact counts and costs. Analyze the active stock, not finished Ketamine, so the first formula has no circular prerequisite. Echo View reuses the shared camera independently of the later dimension implementation.

## Fentanyl — Batch 4B, precision technology

**Role:** an expensive, stronger defensive option after Morphine, with a larger commitment to strain and slower movement/work. It requires precision production and sterile filling, not an injury condition.

| Formulation | Positive effect | Duration / onset | Strain | Drug-caused drawback |
| --- | --- | --- | --- | --- |
| Fentanyl | Resistance II equivalent: **40% less applicable damage**, plus **75% knockback resistance** | 8 minutes / 5 seconds | 55 | Opioid Drowsiness: movement and mining 20% slower while active. No active redose; 180-second recovery. |

Fentanyl and Morphine share the **existing opioid family**, including tolerance, active-use exclusion and recovery. A player cannot take Fentanyl to replace active or recovering Morphine, or vice versa. Native potion protection and drug protection choose the stronger result; they never multiply. A weaker Resistance I potion must not suppress Fentanyl's stronger protection, and stronger native Resistance still wins. The damage types that bypass Resistance remain excluded. Knockback protection uses the strongest drug bonus, plus normal equipment within the native cap.

The existing **Naloxone** cancels Fentanyl's active benefits and its opioid drawback immediately, while retaining strain, shared opioid tolerance and its 180-second recovery. Generalize the existing opioid lifecycle handling; do not create a second reversal item or erase dose accounting. Charcoal does not erase opioid Drowsiness. Normal supported use is protected by strain/compatibility checks; no random lethal batch or mandatory overdose-management system.

**Fictional chain:** independently reachable pharmaceutical/plant supplies → **Organic Process Stock** → shared Precision Reactor capability with quartz and redstone → **Fentanyl Stock** → existing Pharmaceutical Separator with precision tooling → **Verified Fentanyl Active** → Analyzer sample plus Blank Formula Plate → reusable **Fentanyl Formula Plate** → existing Sterile Filler with sterile carrier and four sterile vials → **four Fentanyl**.

This uses entirely fictional game stock and Minecraft co-ingredients. No real precursors or synthesis parameters are specified. Exact ingredient counts, construction gates and energy/time must fit the existing slot/tank capacities and be balanced during implementation. First production requires no Fentanyl sample, Ketamine, controlled-release equipment, psychedelic research or elven trade.

## Fentanyl Patch — Batch 5

Proposed lower-peak alternative: **20% damage reduction and 50% knockback resistance for 20 minutes**, 30-second onset, **40 strain**, with movement/mining 10% slower. It remains in the same opioid family, has no active redose, keeps a 180-second recovery, shares Morphine/Fentanyl tolerance and is reversible with existing Naloxone. It trades the vial's peak protection for duration; it is not 40% protection extended for free.

**Game chain:** extra Verified Fentanyl Active + release-control matrix + cloth backing → shared Release Coater → Analyzer verification/reusable patch tooling → four finished patches. Consumed active stock must pay for the longer benefit; coating a single vial must not create multiple long doses. Use a self-applied consumable and ordinary packaging rules. No permanent armor enchantment, skin/injury simulation, item transfer to another player or second duration ledger.

## Compatibility and shared implementation work

- **Alcohol ↔ opioids**, **Alcohol ↔ Ketamine**, and **Ketamine ↔ opioids** are mutually exclusive during onset, active use and recovery. This is a simple game compatibility rule. Explain the conflict before consumption, spend no item on rejection, and test both directions. No silent weakening of a currently active benefit or newly inflicted combat ailment.
- Introduce named `intoxicant` and `dissociative` families while retaining the existing `opioid` family. Extend the saved family-history capacity beyond the current eight before adding them, preserve old attachment IDs/data, and keep the total active strain limit at 100. Timers remain online counters.
- Keep one player-owned drug state. Extend reusable manufacturing tools, common fuel/recipe support, native buff priority and existing machine menus. Preserve current machine inventories and saved work if functional precision tooling changes their layout. No duplicate Analyzer, pharmaceutical separator, sterile filler, tolerance registry or per-drug factory.
- Shared precision stock and each formula must be reachable before its consumer. Formula discovery consumes the active sample and blank plate; manufacturing keeps the resulting formula. Maintain finite, deterministic stock variants with no hidden purity randomness.
- Self-use only for this batch. Preserve all existing food/cider IDs and behavior; do not implement covert dosing or automatic alcohol effects for legacy meals/drinks.
- New drawbacks originate only from taking drugs. Damage may end Echo View, but does not add an injury or medical-management condition. A broader withdrawal system remains optional future balancing, not an extra prerequisite for these products.

## Acceptance checks for implementation

Low-tech tests must build and run the still without electricity or pharmaceutical output; verify both crop alternatives, progress/unload persistence, fuel/output blocking, exact returned buckets/jugs/bottles, cancellation, breaking and saved work. Existing cider behavior must remain unchanged. Test every alcohol grade's acquisition gate, exact buffs/drawbacks, shared tolerance and compatibility, blocked cross-grade switching, the T3+ redose/recovery rule, packaging, and higher-grade stock costs with no upgrade loop.

Drug tests must cover positive effects, all compatibility pairs in both directions, shared opioid tolerance, per-profile recovery, strain rejection without item loss, cancellation/cleanses/death/save-load and existing Naloxone on both opioid products. Test weaker/equal/stronger native Resistance against Fentanyl, best-only knockback and any native aim implementation. Unsupported weapon sway must not be advertised as a working effect; use the supported spread hook for Alcohol's stated tradeoff.

Echo View needs server-authoritative range/collision, no ore/wall reveal, no body/item movement, reliable camera restoration on every exit path and useful scouting before dream content. Render the still and new precision products in game. Batch 5 must additionally verify patch costs, reduced peak, shared-family exclusion and Naloxone cancellation.

This turn changes planning records and the interactive tree only. No new drug, machine, recipe or effect is implemented by this document.

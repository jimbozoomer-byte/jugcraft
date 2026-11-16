# Pharmacy manufacturing — Batch 2

Status: implemented and verified locally. Not deployed.

Owner-directed implementation with OpenAI Codex (GPT-6 family). Builds on [early pharmacy](pharmacology-early-pharmacy.md) and the [industrial machine foundation](industrial-machine-foundation.md). These are fixed, fictional Minecraft formulations, not laboratory procedures.

## Player outcome

Build a steel-era pharmacy with three shared formed machines. Analyze an existing medicine once to obtain its reusable formula, separate farm ingredients at better yields, and feed the Formulation Press through ordinary factory transport. The finished products are the same six existing consumables, with their existing positive effects and drug accounting. No injury system, random purity grades, maintenance meters or new drug effects are introduced.

| Station | Width × depth × height | Slots / tanks | Process cost |
| --- | --- | --- | --- |
| Laboratory Analyzer | 2 × 2 × 2 | 2 inputs, 1 output, analysis water | 160 ticks at 32 JE/t |
| Pharmaceutical Separator | 2 × 2 × 3 | 1 input, 1 output, extraction water | 160 ticks at 48 JE/t |
| Formulation Press | 2 × 3 × 2 | 3 inputs, 1 output, formula socket; separate water/reagent tanks | 160 ticks at 64 JE/t |

All three use the existing Chemical Reactor recipe family with separate capabilities, the common controller, menu, recipe selection/locking, power, fluid/item ports and saved work lanes. Entry tanks hold 2,000 mB. Warmup is included in processing time, not an extra charge. Existing speed/efficiency cards work through the common upgrade slots. These are process-specific forms, not a new machine backend or a machine for each drug.

Construction uses four steel plates, an electric motor, a basic circuit, a machine casing and copper cable, plus glass for the Analyzer, a Tinplate Tank for the Separator, or a piston for the Press. There is no pharmaceutical prerequisite in the construction recipes.

## Formulas and reachable entry

A copper plate, paper and glass pane make two Blank Formula Plates. The Analyzer consumes **one finished medicine sample**, one blank plate and 250 mB water to write that medicine's Formula Plate. Samples, including their containers, are consumed as ingredients; analysis is not drinking and gives no player effects or consumption remainders.

Six formulas cover Caffeine Drink, Aspirin, Paracetamol, Rehydration Salts, Activated Charcoal and Stimulant. First samples come from the existing crafting and compact-reactor routes, which remain available. The formula plate is an ordinary tradeable item, reusable without wear. Install the matching plate once in the Press's tool socket; pipes and hoppers cannot reach it, and the native screen locks it during a batch. Owning equipment and a plate is sufficient: no personal research requirement.

## Ingredients and manufacturing chains

Every Separator recipe consumes 500 mB water as extraction/conditioning loss and runs for 160 ticks at 48 JE/t:

| Input | Output |
| --- | --- |
| 4 Coffee Beans | 8 Caffeine Concentrate |
| 8 cocoa beans | 4 Caffeine Concentrate |
| 4 willow logs | 4 Salicylic Acid |
| 4 birch logs | 2 Salicylic Acid |
| 4 potatoes | 8 Pharmaceutical Starch |
| 4 corn | 4 Pharmaceutical Starch |

Coffee remains the best caffeine source, at four times cocoa's concentrate yield per ingredient. Compared with manual preparation or the original bark recipes, the main extraction routes improve yields. The potato/cocoa/birch alternatives preserve access without custom biome supplies. Pharmaceutical Starch now serves several medicines instead of being a single-use precursor.

The **existing compact Chemical Reactor** makes four Prepared Pharmaceutical Base from two Pharmaceutical Starch, one sugar and 250 mB water in 160 ticks at its normal 96 JE/t. Sugar deliberately distinguishes this receipt from the earlier redstone-based Paracetamol receipt. This common fictional base supplies both Paracetamol and Stimulant manufacture.

Every Press recipe needs its matching formula, takes 160 ticks at 64 JE/t, and produces the existing stable medicine item:

| Output | Solid inputs, in slot order | Water | Second reagent |
| --- | --- | --- | --- |
| 8 Caffeine Drinks | 8 concentrate, 4 sugar, 8 bottles | 2,000 mB | none |
| 8 Aspirin | 4 salicylic acid, 2 cider vinegar, 2 starch | 250 mB | 50 mB sulfuric acid |
| 8 Paracetamol | 2 prepared base, 2 starch | 250 mB | none |
| 8 Rehydration Salts | 4 salt, 2 sugar, 8 bottles | 2,000 mB | none |
| 8 Activated Charcoal | 4 charcoal, 2 redstone, 2 starch | 250 mB | none |
| 4 Stimulant | 4 concentrate, 2 prepared base, 4 bottles | 500 mB | 500 mB bioethanol |

Bulk manufacturing rewards a complete factory through better extraction, ingredient efficiency and automated batching. It does not increase drug duration or potency, reduce strain, or reset tolerance. Every bottled result consumes a bottle; machine processing returns no free bottles or buckets. Drinking retains the normal bottle remainder. No product is a recipe for its own amplification, and no medicine or formula converts back into its raw inputs.

## Interfaces, automation and failure behavior

- Analyzer: upper front sample and blank-plate drawers; lower front-left power and front-right formula output; rear water inlet.
- Separator: top feed hatch; lower front-right extract output; lower front-left power; rear water inlet.
- Press: upper front active/binder drawers, side packaging/third-ingredient drawer, lower front-right output, lower front-left power, rear water and reagent inlets. Formula socket is accessed through the existing screen.

Only the declared faces expose transfer. Use the native recipe picker and lock when changing jobs; drain incompatible old fluids before switching. The reagent tank is unused by four of the six formulations and may be left empty. Native status messages identify missing inputs, wrong/missing formula, lack of power, blocked outputs and invalid structure.

The common controller reserves output room before consuming inputs, escrows inputs once and snapshots results. Full output or a wrong tool blocks before spending resources. Power loss or pause holds progress. Saved work resumes without re-consuming inputs. Cancellation returns raw inputs, never spent power or finished medicine. Breaking any part drops one machine, its tool and untransformed solid inputs once; no partial medicine is created. Existing machinery behavior and saved drug state remain in their established systems.

Acquisition requires machines and agriculture; salt formulations additionally require salt, the willow alternative additionally requires biomes, and industrial Aspirin additionally requires sulfur. Registrations always persist when acquisition is disabled. No existing item, effect, attachment, fluid, recipe or machine ID is renamed. There are no new fluids or dependencies. Work remains bounded by the shared foundation: one entry lane, at most 12 structural positions per new machine, existing periodic checks and bounded port pushes; no world searches or forced chunks.

## Assets and guide integration

`tools/pharmacy_factory.py` defines forms, receipts, formulas, construction/discovery and guide text. `tools/pharmacy_factory_models.py` builds the three native models with the existing authored steel/olive kit: skids, cabinets, drawers, closed vessels, guard frames, gauges and amber operating lights. It directly reuses `starter_chemistry_models.py` helpers and existing textures. Formula items reference the existing `item/general_chip_pattern.png`; prepared base references `item/salicylic_acid.png`. No new bitmap painting, downloaded assets, dependency art or modified library originals.

Models, part splitting, operating lights, marked connections and menus use the common industrial generators and renderer. There is no custom machine animation system; the initial models have static mechanisms with native operating lights. The Pharmacology chapter, encyclopedia, common machine pages, recipe catalog/JEI, tooltips and five recipe-discovery advancements describe the full line.

New stable block/item IDs: `laboratory_analyzer`, `pharmaceutical_separator`, `formulation_press`; ingredients `blank_formula_plate`, `pharmaceutical_base`; six `<medicine>_formula_plate` items. New capabilities: `pharmacy_analysis`, `botanical_separation`, `drug_formulation`. New tag: `pharmacy_formulas`. All eighteen form recipes and the base recipe use the `chemical_reaction/pharmacy_` prefix.

## Verification

Verified locally on 3 November 2026:

- Main and game-test compilation passed against the existing pinned Minecraft 26.3/Fabric/Java 25 toolchain.
- **21 required server checks passed in one focused run:** nine new factory tests and the twelve existing pharmacology regression cases. All eighteen production receipts actually completed with exact item/fluid/energy accounting and retained tools. The tests cover four orientations, declared port boundaries, missing/wrong formulas and tool locking, full outputs, power loss, saved work, cancellation, mid-batch breaking, reachable construction and a native world-ticked Press delivering eight drinks into an adjacent chest.
- **Rendered `PharmacologyFactoryClientGameTests` passed**, including the final fitting geometry. Eight screenshots were inspected: all three stations from front/back, the running Press menu, and synchronized wrong-formula feedback. A native world-ticked batch completed with exactly eight Caffeine Drinks and one retained formula. Native menus, fixed recipes and drug properties were used throughout.
- Complete material-resource generation passed. A scoped final model refresh uses the common industrial writers; all 67 final pharmacy model files were confirmed byte-identical to the rendered-test resources.
- Full `tools/check_mod_data.py` audit passed: 15,317 material IDs/data/recipes, 38,321 block/item models, 3,777 quad parts and 182 composed models.
- Icon-map checks passed (285 maps, 2,017 icons, 28 existing warnings), repository structure/documentation links passed, and `git diff --check` passed.

The runtime uses the existing complete local resource copy with an explicit factory refresh, avoiding repeated full asset-tree snapshots. Unchanged client Java/Kotlin outputs are reused. This is focused compilation/runtime evidence, not an unfiltered build or unrelated client gallery run. Human multiplayer, two separate clients, a separate-process server restart, performance/load measurements and live deployment remain unverified.

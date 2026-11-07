# Simplified mineral sand processing

Status: implemented and locally validated, 31 October 2026. The owner authorized all four short lines and independent rare-earth chances using existing machines. This supersedes the machinery and processing sequences in the [29 October proposal](mineral-sands-and-refining-plan.md#four-mineral-sand-processing-lines-proposed-29-october-2026). Prepared and implemented with OpenAI Codex, GPT-6 family. Source changes alone are not a gameplay test pass; exact verification follows below.

Use existing general-purpose processors. Combine washing, separation and preparation inside paid recipes rather than exposing each operation as another machine or intermediate item. No new concentrator, separator, extractor, rack, grinder, attachment or machine upgrade is required to make the sands useful. Keep existing deposits, harvesting and saved registrations.

## The four short lines

Counts below are actual processing operations from harvested sand, including finishing. Ordinary crafting of a tool or upgrade after recovering its material is shown separately. The receipts below are implemented local starting balances; owner survival/economy acceptance remains separate.

| Sand | Processing line | Operations to usable output |
| --- | --- | --- |
| Titanium | Mineral Conditioner -> Chemical Reactor -> Arc Furnace | 3 to titanium ingots |
| Zircon | Chemical Reactor -> Industrial Oven -> Precision Finisher | 3 to existing zirconia cutting inserts |
| Rare earth | Mineral Conditioner -> Chemical Reactor | 2 to independently rolled named oxides; 3 to magnets with the Alloy Smelter |
| Garnet | Pulverizer | 1 to usable abrasive grit; ordinary crafting makes tools |

These are deliberately simplified game recipes. They do not reproduce every real mineral separation, reduction or ceramic-manufacturing operation. Their power, reagents and time pay for the combined work. Earlier long routes stay available for existing factories; they are not prerequisites for these short routes.

## Titanium

**Titanium sand -> Raw Titanium -> Titanium Sponge -> titanium ingot.**

| Step | Existing machine | Receipt | Starting work budget |
| --- | --- | --- | --- |
| 1 | Mineral Conditioner | 2 titanium sand + 500 mB water -> 1 existing Raw Titanium | 200 ticks at 32 JE/t |
| 2 | Chemical Reactor | 1 Raw Titanium + 1 Coke + 250 mB chlorine -> 1 existing Titanium Sponge | Existing recipe: 160 ticks at the reactor's 96 JE/t |
| 3 | Arc Furnace | 1 Titanium Sponge -> 1 existing titanium ingot | Existing recipe: 160 ticks at the furnace's 64 JE/t, before boosts |

This uses the existing compact reactor route. Separate chlorination, chloride purification, magnesium reduction and salt recycling are not required by the new sand line. They remain compatible alternatives for already built factories.

Nominal processing energy is **32,000 JE per ingot**, before making coke/chlorine, supplying water, upgrades or optional furnace boosts. Ingots immediately feed current titanium tools, armor, plates and vehicle/component recipes. No new titanium concentrate, chloride or sponge variant is introduced.

**Optional pigment:** use the same first step, then a second Chemical Reactor recipe: 1 Raw Titanium + 250 mB sulfuric acid + 250 mB water -> 4 existing White Dye, proposed at 240 ticks. This combines pigment preparation into one game batch. Pigment and metal consume separate feed; neither is an extra coproduct of the other. No separate pigment cake or pigment-processing machine.

## Zircon

**Zircon sand -> Zirconia Powder -> Fired Zirconia Blank -> cutting inserts.**

| Step | Existing machine | Receipt | Starting work budget |
| --- | --- | --- | --- |
| 1 | Chemical Reactor | 4 zircon sand + 500 mB lye + 250 mB water -> 1 existing Zirconia Powder | 320 ticks at the reactor's 96 JE/t |
| 2 | Industrial Oven | 1 Zirconia Powder -> 1 existing Fired Zirconia Blank | 320 ticks at 80 JE/t |
| 3 | Precision Finisher | 1 fired blank + 125 mB cooling water; retained abrasive head -> 2 existing Zirconia Cutting Inserts + the same 125 mB water | Existing recipe: 200 ticks at 48 JE/t |

The reactor recipe combines mineral recovery and precursor treatment. The oven recipe treats standard blank preparation and firing as one paid operation, removing the separate precursor, prepared-charge and unfired-blank steps from this route. It is an explicit game abstraction; no hidden mold, extra station or manually repeated preparation is required.

Four sand yield two cutting inserts for **65,920 JE** of nominal processing, before reagent supply and upgrades. Inserts already make reusable polymer molds and extrusion dies. Preserve the present full ceramics chain, regional concentrate recipe and expensive legacy ordinary-sand route.

Existing Zircon Concentrate may enter an alternate first-step recipe: 2 concentrate + the same lye/water -> 1 Zirconia Powder. This spends the same four-sand-equivalent mineral allocation and cannot also award precursor or powder from the old recipe. Zircon flour and new foundry inserts are omitted from this delivery.

## Rare earths

**Rare-earth sand -> existing mixed Rare Earth Oxide -> independent chances for neodymium, cerium and yttrium oxides in the same batch.**

| Step | Existing machine | Receipt | Starting work budget |
| --- | --- | --- | --- |
| 1 | Mineral Conditioner | 2 rare-earth sand + 500 mB water -> 1 existing mixed Rare Earth Oxide | 200 ticks at 32 JE/t |
| 2 | Chemical Reactor | 1 mixed oxide + 250 mB sulfuric acid -> three independent rolls: **50% for 1 Neodymium Oxide; 50% for 2 Cerium Oxide; 50% for 1 Yttrium Oxide** | 240 ticks at the reactor's 96 JE/t |

Use one recovery recipe in the existing recipe picker/lock. Roll each material separately; getting one never prevents either of the others. A completed batch may produce none, one, two or all three material types. Inputs, acid and power are paid once for the whole batch even if all three rolls fail. Failed fractions are unrecovered process loss; there is no spent feed to run again. Preserve the previous quantities per successful roll: 1 neodymium, 2 cerium and 1 yttrium oxide.

| Successful material types in a batch | Probability |
| --- | --- |
| None | 12.5% |
| Exactly one | 37.5% |
| Exactly two | 37.5% |
| All three | 12.5% |

Each specific combination has a 12.5% chance. Long-run average output is 0.5 Neodymium Oxide, 1 Cerium Oxide and 0.5 Yttrium Oxide per recovery batch; these are averages, not guaranteed fractional drops. Nominal processing energy remains **29,440 JE per batch**, before water/acid supply and upgrades. The oxide counts are game units, not equal masses or natural ore composition. Audit the expected combined output, including all-success batches, rather than assuming only one product is awarded.

This still uses the existing Chemical Reactor and two processing steps. Its current single solid-output slot cannot hold three distinct oxides, so add shared support for three solid-output slots to that existing machine, preserving old inputs, the old output and saved jobs through an explicit inventory migration. Update its menu and automation to expose all three outputs. No new reactor, separator, solution tanks, salts, cartridges or extra processing operation is required.

Check room for the maximum possible output (1 neodymium, 2 cerium and 1 yttrium oxide) before committing completion. Roll once at irreversible completion and retain any pending rolled results durably; blocked output, pause, recipe changes, save/reload or break/place must neither reroll the batch nor refund already committed feed. Cancellation before that point follows the existing rules with no result rolled yet. All-failure completion still consumes its paid batch. The interface shows each independent 50% chance and successful-roll quantity.

| Material | Immediate use using existing equipment |
| --- | --- |
| Neodymium Oxide | Alloy Smelter: 1 named oxide + 1 existing Ferroboron -> 4 existing Neodymium Magnets, proposed at 200 ticks. The single batch abstracts final reduction/alloying. Magnets already feed motors, dynamos and engines. |
| Cerium Oxide | Ordinary crafting: 2 oxide + 1 existing Felt Polishing Head -> reusable Cerium Polishing Head. Use the same Precision Finisher socket for the existing optical-blank-to-lens recipe at 160 rather than 200 ticks, same one-lens yield and 48 JE/t. |
| Yttrium Oxide | An alternate ordinary crafting recipe for the existing Efficiency Upgrade, replacing its ceramic-insulation portion with 1 yttrium oxide and retaining its other bill. Upgrade behavior and caps stay unchanged; this gives the oxide a ceramic-related consumer without another zirconia-processing branch. |

The named magnet route's proposed higher yield trades acid and reactor work for less ferroboron per magnet. Compare the complete chain against the existing one-magnet iron recipe and two-magnet ferroboron recipe before balancing. Keep those old mixed-oxide recipes; neodymium metal, chloride and a new reduction stage are unnecessary here. Named cerium/yttrium stock must not match generic magnet-feed tags.

Existing monazite can keep feeding the new separation recipes through its current mixed-oxide producers. No conversion back from named oxides to mixed feed is added. Expanding the rare-earth roster is still possible later, using the same general reactor with actual consumers.

## Garnet

**Garnet sand -> Garnet Grit.**

Add one Pulverizer recipe: **1 garnet sand -> 1 Garnet Grit**, proposed at 120 ticks and the current 20 JE/t, or **2,400 JE per grit**. Crushing, cleaning and grading are one operation. No concentrate, washwater or grading attachment.

- Ordinary crafting: 1 grit + 1 wool + 1 plank -> a reusable Garnet Sanding Block. Existing Workshop Bench recipes use it to turn one Cut Sandstone into one Smooth Sandstone, or the equivalent red-sandstone pair, in 200 ticks. Each receipt preserves the block count.
- Ordinary crafting: 4 grit + 1 iron ingot + 1 copper wire -> a reusable Garnet Abrasive Head. In the existing Precision Finisher, it finishes one Steel Shaft Blank into one existing Precision Shaft at 300 ticks / 48 JE/t, compared with the diamond head's 200-tick baseline. Keep the existing cooling-water budget.

No separate wheel, kiln treatment or specialist grinder. Garnet does not replace the diamond head on hard fired ceramics or the polishing head on optics. The reusable tools have no routine durability/refill requirement. Their construction and compatible recipes must ship together.

## Keep the implementation small

- Add recipes to the existing general processors and use their current power, tanks, permissions and automation. The one required shared-machine extension is three solid-output slots on the existing Chemical Reactor, with compatible inventory/menu/automation handling for probabilistic coproducts. No new machine registration, footprint, power system or processing step.
- The Conditioner and Oven each produce one solid kind; the reactor retains its two solid inputs and current fluid inputs, with the three output slots described above; the Pulverizer needs one feed/result; the Alloy Smelter needs two inputs and one result. New Finisher heads use its current protected tool socket. Other machine layouts stay as they are.
- The new material items are the three named oxides and Garnet Grit; two reusable head variants and a sanding block provide the listed uses. Reuse current raw titanium, sponge, ingots, zirconia powder/blanks/inserts, White Dye, mixed oxide, magnets and upgrades.
- Remove the 29 October proposal's mandatory concentrate network, depleted-sand block, new washwater/slurry/chemical-waste fluids, separation salts and recycling circuit from this scope. Unrecovered host material and consumed preparation liquids are explicitly included in conversion loss; no player waste-processing step is needed. The Finisher returns only its reserved cooling water.
- Only the exact four rich-sand IDs get these new recovery receipts. Ordinary sand, red sand and generic ore/sand tags gain none. Keep the old ordinary-sand zircon exception compatible, without extending it to other minerals.
- Existing deposit blocks remain the finite reserve. No terrain changes, new extraction machine, retrogen or extra reserve multiplier is introduced.

## Delivery and checks

Implement all four short lines and their stated consumers as one focused recipe/material batch. Update the Encyclopedia and recipe viewer with the new direct paths as recommendations while preserving old production chains and running jobs. No separate machine-construction phase is needed.

Check actual registry recipes, first reagent availability, machine/tool compatibility, blocked outputs, recipe locks, hopper/pipe access, cancellation and save/reload. For rare earths, cover all eight independent-roll combinations, no-output completion, room for all three coproducts, old reactor inventory migration and durable results without rerolls or duplicated inputs. Audit expected conversion yields, alchemy values and trade prices across the old and new routes. New material-to-useful-product chains must not cycle back into more valuable feed. Verify tool behavior and readable recipes in a client, then review survival cost and multiplayer behavior. The implementation checks below distinguish source inspection, automated validation and owner playtesting.

## Local implementation and verification

The seven new registered items are Neodymium Oxide, Cerium Oxide, Yttrium Oxide, Garnet Grit, Cerium Polishing Head, Garnet Abrasive Head and Garnet Sanding Block. `tools/mineral_sand_processing.py` owns their recipes, guide pages, source feature gates and asset hooks. No new block, machine, fluid, world-generation or dependency registration is added.

The native Chemical Reactor retains its two input slots and original output at indices 0, 1 and 2; new outputs are appended at 3 and 4. `item_layout` upgrades old three-slot inventories without moving existing stacks. Old tank migration stays unchanged. A recipe's optional `result_chances` aligns with its solid outputs; omitted chances preserve all older guaranteed recipes. Network serialization, recipe previews and JEI show the same independent chance metadata.

Completion first checks room for every maximum possible product. A successful paid completion makes the three independent rolls and consumes/emits its batch within one server tick, leaving no exposed pending roll to reroll. All-failure batches still pay. Full outputs pause paid progress and spend no additional power or randomness. The saved `fluid_work_recipe` ties progress to its recipe so swapping fluid-only jobs cannot reuse another job's paid work. Formed machines with guaranteed-output work snapshots explicitly reject chanced recipes; the rare-earth recovery runs in the existing compact reactor.

Existing Finisher sockets retain the new heads; automation cannot extract them. Garnet supports the steel-shaft recipe and not hard ceramics or optical polishing. Cerium supports the faster lens recipe, with unchanged lens count and cooling-water return. The existing Efficiency Upgrade's yttrium alternative preserves its two copper wires, two steel plates and basic circuit, replacing the ceramic-material portion. Normal recipe/claim/party checks remain shared.

Owner-library catalog review found no exact named-oxide or garnet item art. The seven editable native 16 x 16 icon maps adapt the existing `tools/item_icons/zirconia_powder.txt` and `polishing_head.txt` language with shared amethyst, khaki, paper and garnet palettes; the whole sanding block is a new native text map. Sources remain intact and no library program was executed. All seven icon maps passed the icon lint with zero warnings and the contact sheet was visually inspected.

The alchemy catalog allocates each named oxide one quarter of the existing mixed-oxide value. Even the all-success 1 Nd + 2 Ce + 1 Y batch cannot award more essence than its mixed feed. This is conservative maximum-output accounting, not just average-drop pricing. Actual crafting still determines new tool/magnet/upgrade values.

Executed checks as of 31 October 2026:

- The eight `MineralSandProcessingGameTests` passed in a focused server run and in the broad server run. They exercise all eight chance combinations, exact inputs/power, all-failure completion, full output slots, blocked/reloaded paid progress, old three-slot inventory migration, fluid-only job switching, the four routes and retained-tool consumers.
- The broad server run completed 3,238 tests: 3,236 passed, with two stale assertions rejecting the newly approved content (the old nine-recipe manual catalog and a blanket ban on garnet pulverizing). Both tests were updated narrowly to retain their original checks while accepting only the approved additions. The corrected 11-test Workshop suite and the complete 14-test mineral-sand regression group both passed. This is a targeted rerun of the affected suites, not a claim that a second complete 3,238-test run was performed.
- 44 focused Python checks for alchemy conservation, manual recipes, recipe conflicts, JEI stations, the existing tech integration and client selection passed; the five survival-recipe tests also passed after exporting the actual in-game registry. The refreshed survival audit has zero invalid ingredient references and acquisition evidence for all seven new items.
- `tools/check_mod_data.py` passed for 8,193 material IDs and all recipe/data checks, including the artwork audit (25,201 block/item models, 3,777 quad parts and 182 assembled models). Icon lint passed for all seven new editable maps with zero warnings.
- The final full Python suite passed: **292 tests** in 323.055 seconds, using UTF-8 and the configured DejaVu font directory; Git Bash harness tests ran with local process access. Generator outputs were prepared before the repeatability check. Only generated line-ending differences were cleaned afterward. The earlier incomplete-generation/restricted-harness attempt is superseded by this executed pass.
- The local `build` checks passed, with the broad server task excluded on this final packaging invocation after its execution and the corrected focused reruns above. This included common/client/test compilation, native JAR packaging, 79 offline updater assertions and 393 scheduling assertions. The already-staged runtime tree was used as described below.
- `runClientGameTest -PclientTests=MineralSandProcessingClientGameTests` passed with rendering and the development optional integrations. It checked the synchronized three output slots, the cerium count of two, output-only insertion rules and non-overlapping slot geometry. Both screenshots were visually inspected: the reactor panel and all seven items in a chest. Local copies are in `build/mineral-sands-evidence/`. This is one automated rendered client, not two people or a public server.
- `scripts/check_repository.py` and whitespace checks passed. The final local JAR is `../.local-tools/mineral-sands-build/libs/jugcraft-0.1.0-alpha.jar`; no personal-profile installation, release upload or live-server action was performed.

For this local verification, generated runtime resources were staged and byte-checked before compiling, and Gradle used an isolated build/cache directory to avoid another active chat's output files. The local commands used `-x processResources` for that already-staged tree, with the updater helper generated explicitly. Native runtime resources remained complete; only irrelevant asset directories were removed from compilation classpath scanning. These local test settings are outside the versioned product build.

Owner survival-cost/balance acceptance and a two-human dedicated-server playtest remain manual checks. Automated client/server tests do not substitute for those. Nothing has been deployed to the owner's personal profile, live server or public update channel.

Related source records: [machine consolidation](machine-consolidation-playtest.md), [ceramics and precision](advanced-ceramics-and-precision.md), [existing deposit behavior](regional-mineral-sands.md), [machine recipe definitions](../../tools/machines.py), [chemistry recipes](../../tools/petro.py), and [current zircon recipes](../../tools/ceramics.py).

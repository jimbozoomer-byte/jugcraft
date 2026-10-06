# Waste recovery, equipment recycling and pollution

Status: planning step 6, recorded 6 October 2026. The owner choices below are endorsed directions. Equipment, pollution accounting, encounter rules and interface examples are proposals to discuss; no gameplay is implemented by this document. The remaining decisions are listed explicitly at the end.

Primary specialties: materials recovery, industrial chemistry, factory operations and base defence.
Related documentation: [machine roadmap](../MACHINE_ROADMAP.md), [chemistry](../branches/CHEMISTRY.md), [current technology](../TECH_TREE.md) and [testing guidance](../TESTING.md).

## Explicit owner choices

- **Optional recovery at first:** useful byproducts and extra processing can recover materials and reduce costs. Do not turn the first version into a compulsory waste-handling chain for every machine.
- **Equipment disassembly is in scope:** gradually include machines, tools and manufactured goods, alongside material scraps and spent fluids.
- **Pollution thresholds attract the existing marauders:** pollution must have thresholds that spawn that faction.
- **Pollution output is visible on a pollution map device.**
- **Pollution should not really affect how the world looks:** do not add terrain discoloration, visible smog, biome replacement or other landscape appearance changes as pollution consequences.

The owner's clarification was: "This shouldn't really effect how it looks or anything but it should have thresholds that spawn the marauders we made, the output should be visible on a pollution map device."

Earlier constraints still apply: a substantial mechanical workshop with sideways depth, a manageable first-electricity route, reusable machines after motor conversion, selected cross-industry dependencies and no new routine lubricant/replacement-part maintenance. Pollution-driven crop damage, tree death, player illness and terrain destruction are not part of the approved direction.

## Player experience and useful end products

A player can initially run a modest workshop without constructing a complete treatment plant. As production grows, recovering valuable leftovers saves mining, transport and reagent costs. Managing emissions can also keep marauder attention below encounter thresholds. A player can choose additional treatment, cleaner production or stronger defences; exact pressure and limits require balancing.

This distinguishes optional material recovery from pollution's encounter consequence. Optional recovery does not mean that heavy unchecked production is consequence-free, and the encounter system must not make a large treatment complex an unavoidable gate to the first electrical machines.

| Input family | Proposed recovered output or outlet | Useful consumers |
| --- | --- | --- |
| Metal offcuts and identifiable scrap | Known metal/alloy scrap, then an appropriate recovered metal form with defined losses | Plates, wire, construction fittings and replacement manufacturing |
| Retired machines and tools | Selected reusable parts plus damaged/unrecoverable fractions | Workshop equipment, motors, repair manufacture and further scrap recovery |
| Mineral-depleted sand | Graded construction/glass feed where suitable | Construction and selected glass recipes; no repeat recovery of the original heavy minerals |
| Suitable slag and mineral residues | Reviewed aggregate, filler or another explicitly usable product | Selected mortar, concrete and construction variants |
| Spent process fluids | A defined fraction of usable water, reagent or solvent | The original process or another compatible recipe |
| Filter cake and captured dust | Identified recoverable material, or a manageable residue outlet | Approved metal/mineral recipes or suitable construction uses |

These are product families, not permission to convert every residue into fertilizer, every alloy into all its constituent metals or every liquid into clean water. Introduce each outlet with a defined input, output, energy cost and downstream recipe.

## Proposed equipment and progression

| Equipment or shared role | What the player does | Progression and connections |
| --- | --- | --- |
| Disassembly bench | Break down selected registered machines, tools and manufactured goods | Reachable manual workshop route; visibly names what can be recovered |
| Scrap sorter and preparation mill | Separate/prepare approved scrap streams | Shaft-powered workshop processing; motors automate existing equipment |
| Scrap remelting recipes | Recover appropriate metals or alloys from known scrap | Reuse suitable furnace/smelter capability; larger recovery equipment only where throughput or process conditions justify it |
| Settling Plant / filter press | Separate solids and selected usable liquids from spent streams | Builds on the existing flowback-treatment role and shared refining equipment; selected agricultural filter cloth and ceramic parts can supply construction |
| Neutralization / treatment vessel | Process selected chemical residues with appropriate reagents | Chemistry equipment, with useful defined outputs; not mandatory on every unrelated machine |
| Reagent or solvent recovery | Recover a limited useful fraction of selected spent fluids | Shared separation/distillation capability, with energy and losses; independently reachable first-batch reagents remain required |
| Particle collector | Capture the selected particulate contribution of a connected industrial process | Durable workshop filters; captured outputs follow approved recovery recipes |
| Gas scrubber | Treat selected compatible process emissions | Gives suitable reagents, including lye where appropriate, an optional industrial consumer; cannot remove every emission with one universal recipe |
| Pollution map device | See polluted areas and encounter thresholds | Reachable before pollution creates a serious encounter threat; later display/range improvements can be optional sidegrades |

Use the earlier shared item, fluid, kinetic and electrical systems. Filters and linings are installed capabilities under the existing planning direction, rather than a new requirement to replace parts after every few operations. Treatment reagents may be recipe inputs, with their cost balanced against recovered products and emission reduction.

## Proposed pollution accounting

Start with a readable local industrial pollution value. Individual recipes/fuels can declare contributions so players can understand the sources. Air, water and solid-residue contributions can remain distinguishable in the data or device, but a mandatory simulation of three separate environmental systems is not yet chosen.

- Successful active operations and actual fuel consumption add their declared emissions. An idle machine, electric cable or stored finished product does not pollute merely because it exists.
- Account for fuel at its consuming generator/engine, and process emissions at the process producing them. Do not charge the same electricity-related emission again at every machine that uses that power.
- Approved treatment reduces the appropriate outgoing contribution or processes a defined spent stream. Recovery and pollution reduction need not be identical: a useful recycling operation can still consume fuel or produce a residual emission.
- Keep records for areas rather than attaching pollution solely to a player or machine. Removing a chimney or dismantling a machine must not erase pollution already recorded there.
- Spatial extent, spread, natural decline, units, thresholds and rates remain decisions to settle. Avoid an elaborate weather or fluid-world simulation unless separately requested.
- The device must explain the current level and threshold meaning. Proposed extra readings are recent emission rate, rising/falling trend, major nearby sources and whether an encounter is eligible or held by a cooldown.

Do not implicitly load chunks or scan the world to update pollution. A future implementation needs bounded active-source bookkeeping and persisted area records. Unloaded machines must not manufacture emissions or recovery products unless an independently approved simulation already performs those operations. A catch-up calculation for natural decline must be deterministic and bounded if that decline model is selected.

The system should support modest pre-electric activity without immediate large raids. Exact source assignments and threshold balance are not approved yet; neither a blanket exemption for all mechanical industry nor severe emissions on every early kiln is implied.

## Existing marauders and encounter integration

The owner's existing faction is called **Raiders** in the inspected source. Its [feature record at the inspected commit](https://github.com/jimbozoomer-byte/jugcraft/blob/b1929820b35c51e9cf49a57706ddfd2c8087e26c/docs/features/raiders.md) describes infantry, officers, grenade troops, walkers and blimps. That work is on `feature/raiders-57`; this plan does not claim it is merged or freshly playtested.

The inspected system already has directional horn warnings, a grace period, a world cooldown, capped encounter levels, an active-raid guard, and withdrawal rules. It respects its feature switches, peaceful difficulty and mob-spawning rules. Its explosives do not break, move or burn blocks; existing temporary siege ladders are an independent faction behavior rather than pollution changing the landscape.

Currently, automatic raids are timed/chance-based and raid difficulty rises with past victories. Pollution-based eligibility is a proposed integration, not existing behavior. Settle how pollution interacts with that trigger and escalation before implementation.

Proposed integration rules:

1. Evaluate pollution thresholds only through the existing bounded encounter scheduler. Crossing a threshold does not spawn a party every tick or add a new independent unlimited spawner.
2. Preserve clear warning, safety/configuration gates, encounter caps and a meaningful recovery window between encounters. Exact timings can be reviewed with the pollution balance.
3. Tie a pollution-driven encounter to the relevant producing area and nearby eligible players, rather than moving its threat to an unrelated village merely because the factory owner travels there.
4. Distinguish being above a threshold from a guaranteed immediate raid. The device can show eligibility and cooldown; exact scheduling after eligibility remains to be designed.
5. Decide whether higher bands change encounter strength, encounter frequency or both. Do not accidentally stack pollution escalation and the current victory-based escalation into uncapped difficulty.
6. Lower pollution must provide a visible benefit on the device and reduce future encounter pressure according to the chosen model. Cooling down does not automatically remove a raid already under way.
7. Existing camps and the voluntary war horn remain separate proposed behaviors; the pollution requirement should apply to automatic pollution-driven encounters. Any changes to those independent activities need their own decision.

Further balance work must address shared factories, overlapping polluted areas, nearby players who did not produce the emissions, migration from existing raid saves and exploit farming of raid loot. A multiplayer player-targeting policy is not finalized by this document.

## Proposed pollution map experience

The owner requires a map device. A candidate first version is a handheld device displaying the surrounding area's pollution as map colors, with the player/factory location and threshold legend. The world itself retains its ordinary appearance. A later wall display or control-room map could provide a larger view and selected monitoring information.

Useful proposed readings:

- Current pollution and the next encounter threshold.
- Whether the area is rising, stable or falling, based on actual stored readings.
- Which known nearby processes contribute most.
- Raid eligibility and any cooldown, without promising an exact arrival time unless the scheduler provides one.

Device form, map range, discovery rules, manufacturing ingredients, category views and exact interface are still proposals. Do not gate the first readable pollution measurement behind a material obtainable only by surviving pollution-driven marauders. Avoid revealing every remote factory/player location through an unrestricted global map.

## Recovery accounting and compatibility boundaries

- Use explicit disassembly recipes, not unrestricted inversion of every crafting recipe. Composite goods, alloys, coatings and consumable parts require reviewed recovery definitions.
- Empty or safely return an item's machine inventory and fluid contents before salvage. Batteries, charged items, tanks and installed upgrades need explicit handling; never duplicate their stored contents through disassembly.
- Used/damaged goods must not return the same pristine recoverable value as unused goods where doing so would create a positive-gain loop. Define durability/state handling alongside the first affected recipes.
- Do not let crafting, disassembly and remelting multiply material through alternative cheaper recipes, yield boosts or repeated recovery. Recovery cannot supply an industry's first reagent batch if that reagent is needed to produce the spent input.
- Provide manageable routes for unavoidable byproducts on any recipe that is expanded. Do not add new compulsory waste items to every existing machine under the label of optional recovery. Full-output and disposal behavior need explicit implementation design, not silent loss of inventory/fluid contents.
- Preserve stable registrations and existing reachable processing routes until replacement routes work. No new currencies, mandatory dependencies, chunk loaders or routine upkeep are authorized here.

The existing [petrochemistry data](../../tools/petro.py) has a flowback-treatment route, and the [chlor-alkali record](chlor-alkali.md) previously left emissions scrubbing for a future pollution system. These are source starting points for shared equipment and useful reagent consumers. The older roadmap idea that all acid waste and slag "must be neutralized" is superseded as a planning default by the owner's optional-recovery choice; individual later requirements need an explicit decision.

The separately published [industrial agriculture plan](https://github.com/jimbozoomer-byte/jugcraft/blob/docs/industrial-agriculture-plan/docs/features/industrial-agriculture-plan.md) and [mineral-sands/refining plan](https://github.com/jimbozoomer-byte/jugcraft/blob/docs/mineral-sands-refining-plan/docs/features/mineral-sands-and-refining-plan.md) supply proposed filters, coatings, construction outlets and recovery streams. This step adds no code or recipes to those branches.

## Remaining owner decisions and next planning step

Three immediate questions:

1. Should pollution replace the current random automatic-raid trigger, add another trigger, or modify the existing trigger while requiring a pollution threshold?
2. Should higher pollution thresholds produce stronger encounters, more frequent encounters, or both within explicit caps?
3. Should pollution stay local and naturally decline, spread modestly into nearby areas and naturally decline, or persist regionally until active cleanup?

Device form and exact numbers can be refined after these core choices. Recommended proposals are to connect the automatic trigger to pollution, use a small number of readable capped encounter bands, and allow local pollution to decline with limited nearby spread. These are recommendations, not recorded approvals.

After settling step 6, step 7 is progression and balance: build representative solo/trade production routes, identify bootstrap cycles, select the first implementation slice and plan relevant playtests. Do not interpret these planning steps as authorization to merge or implement gameplay automatically.

Future implementation validation includes accounting audits, threshold/cooldown behavior, bounded server work, restart/unload persistence, disassembly with inventory/fluid/energy contents, durability handling and a two-player server test. This documentation change requires repository/link and whitespace checks; it provides no new build or gameplay evidence.

AI-assisted planning documentation: OpenAI Codex, GPT-6 family. No gameplay feature implementation is included.

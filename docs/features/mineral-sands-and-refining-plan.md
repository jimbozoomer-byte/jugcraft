# Mineral sands and shared chemical refining

Status: owner-endorsed planning direction, recorded 6 October and updated 10 October 2026. This document records choices for planning steps 4 and 5 and the linked tenth and thirteenth chemistry batches. Proposed deposit profiles, machine names, recipe stages and consumer examples are designs to develop, not claims of implemented gameplay. Quantities, worldgen frequency, capacities, energy costs and individual recipe behavior are not finalized.

**Current direction, 31 October:** use the [simplified mineral sand processing plan](mineral-sand-processing-simple.md). The owner requested fewer operations and no new machines: the new plan uses only existing general-purpose processors. It supersedes this document's proposed specialist machines, intermediate chains and 29 October recipe budgets. Earlier planning remains below as history, not an implementation queue.

Primary specialties: regional mining, mineral processing, ceramics, chemical refining and precision materials.
Related documentation: [chemistry](../branches/CHEMISTRY.md), [machine roadmap](../MACHINE_ROADMAP.md), [current technology](../TECH_TREE.md), [resource deposits](resource-deposits.md), [industrial chemistry](industrial-chemistry.md).

## Explicit owner choices

- **Major progression route:** several important later materials primarily come through mineral-sand processing.
- **Dedicated mineral-bearing sand:** valuable material comes from particular sand types plentiful in suitable areas. Do not incentivize destroying every ordinary beach for trace industrial minerals.
- **Strong regional specialization:** different regions supply distinct primary minerals; exploration, transport and trade are central.
- **Moderate processing depth:** a short concentration route followed by specialized separation where a product needs it. Machines serve several recipes rather than requiring every possible fraction for every product.
- **Hybrid extraction:** exposed mineral sand can be collected for startup quantities; a dedicated extractor draws sustained production from a large, finite deposit with limited surface disturbance.
- **Shared chemical machines with distinct roles:** leaching/digestion, filtration, chemical separation, precipitation/crystallization and recovery equipment handle several materials. Dedicated equipment is appropriate for genuinely different process conditions.
- **Initial named rare-earth set, with expansion explicitly planned:** begin with neodymium, cerium and yttrium products. The owner said, "A but yeah definitely mark it to be expanded." These three are the first slice, not the final roster.

Earlier choices remain constraints: useful pre-electric industry, a manageable route into each next capability, many sideways products, first electricity before mod steel, optional motor-driven reuse of workshops, and selected cross-industry dependencies. This proposal does not introduce routine lubricant consumption or replacement-part maintenance.

## Regional deposits and ordinary sand

Use recognizable mineral-bearing feedstocks and explicit processing recipes. Ordinary sand, red sand and mineral-depleted outputs do not gain valuable heavy-mineral recovery recipes simply through a generic sand tag. Keep ordinary sand useful for construction and glass.

Deposits should be plentiful within their eligible regions and large enough to support a meaningful factory. Suitable settings include inland basins, ancient coastal formations and particular river/alluvial regions as well as selected coastal areas. The following profiles are game-design candidates; they are not final biome assignments or a claim that each real geology produces one pure mineral.

| Candidate deposit family | Primary supply | Proposed consumers |
| --- | --- | --- |
| Titanium-rich mineral sand | Ilmenite and rutile-bearing feed | Titanium refining, pigment products and suitable ceramic ingredients |
| Zircon-rich mineral sand | Zircon-bearing feed | Glazes, foundry materials, refractories and later zirconium chemistry |
| Rare-earth mineral sand | Monazite and potentially xenotime-bearing feed | Rare-earth separation and named material products |
| Garnet-rich mineral sand | Suitable garnet-bearing feed and associated minerals | Abrasives, grinding media and polishing products |

Secondary minerals can be useful, but should not make every deposit a sufficient source of every region's primary material. Regional identity must survive processing: mined and extracted feed retains its deposit family through its recipe path.

Existing underground titanium/monazite finds can remain supplemental sources while industrial quantities primarily come from sand deposits. Preserve stable existing registrations and a reachable route when rebalancing acquisition; this document does not remove ores or change current worldgen.

## Hybrid mining-site experience

1. Players recognize an exposed deposit and shovel startup batches of its mineral-bearing sand.
2. A simple ground sampler identifies the mineral family and roughly indicates the deposit's extent. It should have an entry route without advanced electronics. Later prospecting equipment can provide better readings.
3. A small shaft-powered sand extractor supplies unprocessed feed from the deeper reserve, using reachable early metals and mechanical parts.
4. A motor drives the same equipment after electrification. Larger extractors improve throughput and handling where useful.
5. The deposit is large but finite. Surface collection and powered extraction must account for material already removed; neither can collect the same reserve twice.

Exact reserve representation is undecided. Exposed blocks and a deeper stored reserve must have a defined resource budget, persist on restart/unload, and resist duplication through replacing blocks, repeated sampling or moving the extractor. These are implementation requirements, not a decision to copy the existing deposit capacity unchanged.

Depletion should have readable feedback. Larger equipment changes extraction rate/capability, not the quantity of material magically present. Mining automation uses bounded loaded-area work, existing ownership rules and shared item/power interfaces; do not add chunk loading as an implied requirement.

## Moderate physical separation

| Operation | Proposed equipment | Output and purpose |
| --- | --- | --- |
| Screen and wash | Hand/basic preparation and later screening/washing equipment | Remove oversized material and prepare suitable feed |
| Gravity concentration | Shaft-powered shaking table or spiral concentrator | Recover a heavy concentrate while separating most ordinary sand |
| Selected magnetic separation | Magnetic drum using reachable electromagnets | Recover appropriate magnetic fractions |
| Selected electrical separation | Later electrostatic separator | Separate fractions requiring that capability |
| Grinding and grading | Mill and grading screens | Prepare usable powders or approved abrasive grades |

Each feed/product recipe uses the needed subset of these operations. Use only intermediates with a clear processing, storage, trade or consumer role. Basic suitable abrasives and ceramic materials can have direct mechanical preparation routes; not every mineral must undergo chemical refining.

Starter magnetic equipment must not require rare-earth magnets produced by its own separation chain. Reachable copper/iron electromagnetic equipment can provide the initial capability; rare-earth components can support later approved upgrades. Electrostatic separation requires an electrical capability and appropriate equipment rather than pretending to be a purely mechanical operation.

Mineral-depleted sand should have useful construction outlets and remain depleted. Define concentrate recovery and material accounting before applying any existing ore-yield multipliers. Generic washing, compaction, grading or ore tags must not permit repeated mineral recovery from the same input.

## Shared chemical equipment

| Role | Proposed shared uses | Capability boundaries |
| --- | --- | --- |
| Leaching/digestion vessel | Appropriate acid or alkaline treatment of different mineral feeds | Material compatibility and heating; gas pipes automatically supply process pressure under the later owner choice |
| Filter press | Separate useful solutions from solid residues | Suitable durable filters; approved recovery/purity behavior |
| Precipitation/crystallization tank | Recover useful compounds from solution | Required reagents, temperature and product-specific recipes |
| Chemical separation unit | Separate selected dissolved materials, including rare-earth fractions | Appropriate separation chemistry; avoid one universal recipe for unrelated feeds |
| Calciner | Convert prepared compounds into useful oxide powders | Required temperature/atmosphere with ceramic equipment connections |
| Electrolytic cell | Selected metal and reagent production | Appropriate electrodes and bath conditions; molten-salt and aqueous operations need their real capability distinction |
| Sealed reduction retort | Controlled-atmosphere reduction and appropriate recovery operations | Dedicated process hardware where shared open vessels cannot perform the job |

A line uses the operations it actually needs. Small batches stay practical. Larger stations can offer throughput; control, heat, lining or electrode changes can unlock genuinely different processes. The later chemistry choice makes gas-pipe pressurization automatic, rather than a player-managed pressure tier or compressor prerequisite. Preserve shared kinetic/electrical/item/fluid systems rather than inventing parallel APIs or energy currencies.

Connections to earlier industries include ceramic linings/insulators, suitable agricultural filter cloth and seals, basic metal vessels and later compatible materials. Essential components need a reachable hand/solo/trade route. These connections must not require every farmer or engineer to complete every specialty.

The [twelfth chemistry batch](industrial-chemistry-and-fuels-plan.md#grid-storage-synthetic-fuels-and-cryogenics-decisions-twelfth-batch) selects mineral-derived vanadium electrolyte alongside refinery residue and independent cobalt stock for reusable synthetic-fuel tooling. The [thirteenth batch](industrial-chemistry-and-fuels-plan.md#resource-reagent-and-residue-decisions-thirteenth-batch) now selects galena lead, spodumene-bearing rock first with natural lithium brines later, **separate primary cobalt ore**, regional chromite concentrate and specifically vanadium-bearing iron concentrates. Cobalt mineral identity and new source regions/refining receipts remain open. Preserve current feeds and independently reachable earlier vanadium preparation for the first acid catalyst; ordinary iron does not universally yield vanadium.

The thirteenth batch also selects an optional **gypsum-rich phosphate-refining residue -> paid game purification/preparation -> suitable plaster/board ingredients** branch on compatible shared construction equipment. Define reagent/energy/yield/loss receipts and useful finished products before implementation. Raw residue is not the finished ingredient; this fictional route is distinct from stabilized coal-cleaning and bauxite filler stocks.

## Initial refining lines and finished products

### Alumina and aluminum

Candidate route: bauxite preparation, alkaline digestion, clarification/filtration, recovery of a precipitate and calcination into alumina. Alumina then branches into appropriate ceramic products or aluminum electrolysis.

The [7 October material planning follow-up](industrial-chemistry-and-fuels-plan.md#material-production-decisions-third-batch) selects aluminum's broad first product range: electrical parts, structural stock, building panels and vehicle components. Initial refining stations are compact functional installations, with optional larger bulk plants. Exact recipes/capacities remain to design; oxide/ceramic and metal consumers keep independently useful roles.

This gives the refinery a useful oxide output as well as a metal consumer. Preserve existing alumina/aluminum identities and keep prior reachable recipes until their replacement route works. The inspected electrolytic-cell recipe uses aluminum cable while producing aluminum. In the [7 October chemistry planning follow-up](industrial-chemistry-and-fuels-plan.md#machinery-and-operation-decisions-second-batch), the owner selected **earlier copper conductors for entry aluminum-processing equipment**, with aluminum improvements later. Exact construction recipes remain to design; verify this bootstrap before the current starting route is removed.

The [tenth chemistry batch](industrial-chemistry-and-fuels-plan.md#magnesium-aluminum-and-titanium-processing-decisions-tenth-batch) selects powered recovery/reuse of most digestion lye with makeup losses, consumed coke/carbon for electrolysis and optional CO2 capture. Stabilize the distinct bauxite residue into modest road/filler stock through shared construction equipment. Exact binder, yields, quantities and energy remain pending; this is not raw residue placement or routine electrode replacement.

### Magnesium feed and recovery

The owner selects **seawater pumped from beach or ocean biomes -> concentration -> magnesium-bearing brine -> magnesium hydroxide -> HCl-based preparation/conditioning -> dry prepared MgCl2 item -> internally heated molten-salt electrolysis -> magnesium/chlorine**. Beach and ocean are both eligible source categories. Exact source tags, intake validation, pumping/preparation quantities, reagent/water receipts, drying and losses remain to specify. A freshwater intake does not become mineral feed because the machine stands near a beach. This pumped route does not require stripping shorelines or introduce finite ocean depletion through the mineral-sand deposit rules.

Use earlier materials/ceramics for the first equipment and magnesium batch. Returning titanium-reduction salts can supplement that supply from the first titanium installation, with paid recovery and makeup losses; it cannot bootstrap the initial magnesium. Dry item feed melts inside the cell, without a separately transported molten-chloride requirement or player pressure management.

### Titanium materials

Separated titanium-bearing minerals can branch into pigment products or a metal-refining route. The owner selected **mineral treatment -> crude titanium chloride -> purification -> magnesium reduction -> titanium sponge -> melting**, using compatible shared equipment. The tenth batch selects shared chemical distillation for chloride purification and paid automatic sponge cleanup inside the reduction equipment, returning salts separately before melting. Exact quantities, conditioning and construction remain to design. Destinations include advanced process/precision equipment, vehicle components and a stronger titanium tool/armor tier above steel.

The current titanium recipe compresses chemical recovery into one conversion. The selected magnesium reduction needs an independently obtainable feedstock and equipment built before titanium. The owner selects recovery of magnesium/chlorine from spent MgCl2 salts from the first titanium installation, with explicit energy, conditioning and losses; it cannot supply the very first magnesium batch.

Maintain existing titanium/sponge consumers and define the purpose of any introduced intermediate. The retort, purifier and melting equipment cannot require their own titanium output to be constructed. Exact chemistry simplifications and coexistence with current recipes need a focused implementation design.

### Rare-earth products

Candidate route: prepared mineral concentrate, appropriate chemical treatment, filtration, chemical separation, then finished compounds and suitable metal/alloy recovery where needed. Do not imply that a mixed oxide automatically becomes a pure magnet metal merely by shaping it.

| Initial named family | Proposed material/output role | Proposed finished consumers |
| --- | --- | --- |
| Neodymium | Appropriate recovered compound followed by a defined metal/alloy route where required | Permanent magnets, improved motors/generators and selected equipment |
| Cerium | Appropriate oxide/polishing products | Precision glass, lenses, mirrors and surface-finishing supplies |
| Yttrium | Appropriate compounds such as oxide additions | Selected zirconia ceramic components and later reviewed precision-material applications |

Form, purity, grade and actual recipes remain to be designed. Not every named material needs ore, ingot, nugget and block forms; give it the forms its consumers need. Existing mixed rare-earth oxide and magnet recipes need a documented coexistence or staged transition, without stranding current saves or removing the only reachable route.

### Direct mineral uses

Zircon and garnet retain suitable direct preparation routes for ceramic materials and abrasives. New advanced zirconium or other metal products should be introduced with real consumers, rather than adding every possible element simply because a mineral contains it.

The later material batches select powder preparation -> shaping blanks -> firing -> shared grinding/polishing where needed, with clay foundation, porcelain-style insulators and alumina components, then specialist zirconia. Shared finishing also serves metal and optical consumers. Compact refining entry does not erase the substantial first digester or clay-based first steel casting. The selected initial alloys are chromium/nickel stainless steel and aluminum-magnesium structural alloy; titanium alloys come later and exact compositions remain to design. Regional chromium-bearing ore deposits now feed named chromite concentrate and shared refining; regions, producer recipes and stock forms remain open. The selected beach/ocean seawater and dry-chloride route supplies titanium reduction and aluminum alloying. Whole-ingot batches are selected for both first alloys; numerical ratios and certified grades remain unselected. Exact source validation, preparation receipts and compatible early hardware remain to design. This metal expansion follows the selected first gas/acid factory. Larger chemical/refining machines gain throughput and lower energy per unit, with useful upgraded small stations.

The [expanded chemical catalog](industrial-chemical-catalog-and-routes.md#fluorite-hydrofluoric-acid-and-phosphate-wet-processing) requests fluorite/concentrated-sulfuric HF production and phosphate-rock wet/hydrometallurgical processing into phosphoric acid. The owner selects substantial underground fluorite deposits in snow biomes with occasional surface outcrops; exact eligible biome IDs/tags, abundance/depth, extraction and outcrop placement remain to design. This is a game resource-distribution choice and implements no worldgen or changes to generated chunks. Shared dissolution/filtration/recovery and reagent purification use specific recipes, independently obtainable feeds and explicit residues; water conditioning is accounted within recipes. Named nitrogen/phosphate fertilizers use broad crop groups and optional mixed-farm blends. PTFE parts enter advanced-machine construction recipes rather than lining retrofits. The catalog links these minerals to advanced wafers, fertilizers and later LiPF6 storage; ordinary sands do not all become fluorite/phosphate sources.

## Rare-earth expansion is a planned follow-up

**The initial neodymium/cerium/yttrium set is not a permanent limit. Expansion is explicitly requested by the owner.** Keep it visible in the chemistry and machine roadmaps, and revisit it as precision manufacturing, optics, catalysts, advanced ceramics, improved magnets, alloys and expedition equipment gain concrete consumers.

For each additional named material:

- Name one independently useful application and preferably several connected products or sidegrades.
- Define its regional/mineral feed and a reachable separation/recovery route using shared equipment where suitable.
- Identify useful output forms, required purity and any real reason for dedicated process equipment.
- Explain its role beside existing materials, rather than adding an arbitrary compulsory ingredient to every later recipe.
- Record quantities, energy/reagent costs, losses and residue/recovery behavior when implemented.
- Add the material and its consumers together in a focused slice; preserve saved registrations and avoid ingredient cycles.

These criteria guide the required expansion; they do not cancel it. Exact additional elements and their sequence remain open. The intention is a growing roster with useful product depth, not a fixed three-material end state or a mandatory complete periodic table.

## Source observations and implementation boundaries

The inspected source includes surface resource deposits, a deposit drill, prospecting, ore washing/sifting, a Settling Plant, chemical reactors, electrolysis, simplified bauxite/titanium/rare-earth recovery and existing magnets. Use [deposits data](../../tools/deposits.py), [machine data](../../tools/machines.py), [material data](../../tools/materials.py) and [petrochemistry data](../../tools/petro.py) as current-source references. Recheck the source before implementation; these observations are not new game-test evidence.

The related [industrial agriculture plan](https://github.com/jimbozoomer-byte/jugcraft/blob/docs/industrial-agriculture-plan/docs/features/industrial-agriculture-plan.md) is published separately in [PR #208](https://github.com/jimbozoomer-byte/jugcraft/pull/208). It offers selected filter, belt, coating and finished-material connections without adding new routine upkeep. Use its merged main location once available.

This documentation change adds no gameplay, recipes, registries, runtime assets, worldgen or save changes. Future code must follow [testing guidance](../TESTING.md), prove entry paths, audit mineral/reagent/fuel loops, and test reserve persistence, full outputs, interrupted processing, ownership, unload/reload and relevant multiplayer behavior. Do not remove chemistry stand-ins until the replacement is reachable.

## Next planning step

Step 6 is waste recovery and recycling: useful outlets for depleted sand, process residues, scraps and spent fluids; optional recovery of materials/reagents; selected treatment where appropriate; and the owner's choices about mandatory handling versus additional efficiency. Pollution effects, maintenance and waste-blocking rules are not approved by this document. The existing flowback-treatment route is a starting point to inspect, not permission to extend compulsory waste to every machine.

## Four mineral sand processing lines proposed 29 October 2026

**Superseded on 31 October by the [simplified plan](mineral-sand-processing-simple.md).** The owner requested shorter lines using existing general-purpose processors. The concentrators, separators, attachments and long processing chains below are historical proposals, not requirements for the current delivery.

The player outcome is four complete industries: titanium metal and white pigment; zircon ceramics and tooling; named rare-earth products for magnets, optics and better zirconia; and garnet abrasives. Every family must reach at least one usable product in the first release. A concentrate alone does not finish a line.

This is a local design proposal prepared with OpenAI Codex, GPT-6 family. Source baseline: local `peepo-integration` at `8623f9dea`, checked against current upstream `main` during the preceding TODO review. Existing machinery and recipe observations below come from that checkout, not a new gameplay test. This planning change does not implement or deploy recipes.

### Current foundations and compatibility

| Foundation | Present behavior | Work this plan adds |
| --- | --- | --- |
| Four finite deposits | Each harvested block is one feed unit; shovel and existing powered Ore Drill recover that same unit | Family-specific consumers; preserve this reserve contract |
| Regional zircon | Mineral Conditioner: 2 zircon sand + 500 mB water -> 1 zircon concentrate, 200 ticks at 48 JE/t | A mechanical bulk producer and direct mineral products |
| Zircon ceramics | Concentrate -> prepared precursor -> zirconia powder -> conditioned charge -> pressed blank -> fired blank -> cutting inserts | Preserve the full route and existing mold/die consumers |
| Titanium | Existing raw titanium, chlorinator, shared distillation, magnesium retort, salt recycling and Arc Furnace | A sand-derived raw-titanium supply and a pigment branch |
| Rare earths | Monazite and mixed Rare Earth Oxide, existing magnet recipes and magnet motor/dynamo consumers | Named separation, neodymium metal, cerium polish and yttrium-enhanced zirconia |
| Garnet | Deposit feed, without a processing consumer found in the reviewed recipe source | Graded abrasive and immediately useful reusable tools |
| Shared processors | Conditioner, Filter Press, Industrial Oven, reactors, distillation, Metal Press and Precision Finisher | Explicit recipes/capabilities; no duplicate general-purpose chemistry stations |

Keep every existing saved item, machine, recipe and running job compatible. In particular, retain the expensive ordinary-sand zircon recipe as a legacy/existing-world route even though the original mineral-sands direction excludes adding generic sand recovery. New valuable-sand recipes must accept exact family IDs, never broad sand or ore tags. Keep existing monazite/mixed-oxide magnet routes and older titanium routes during the transition.

The model briefs describe proposed machines, not necessarily current implementations. Reuse the already consolidated Filter Press and Industrial Oven; do not rebuild their old proposed standalone replacements merely because those briefs still say unbuilt.

### Common preparation and extraction

Use the existing prospector and finite feed blocks. A dedicated shaft-powered extractor is a follow-up, not a prerequisite for processing: players can shovel startup feed or supply the line with the current Ore Drill. A later extractor must remove actual reserve blocks once, check loaded chunks/claims and leave depleted sediment; it must not attach a second hidden reserve to the same blocks.

Build one **Gravity Concentrator**, using the approved 4 x 6 x 3 shaking-table envelope. Its inlet screen, wash rail and riffled table combine washing and gravity concentration in one short operation. A separate Screening and Washing Plant is an optional larger preprocessing form later, not another compulsory starter machine.

First construction uses wood, iron, bronze gears, copper fittings and ordinary fired-clay components. It must require no steel, electricity, rare-earth magnet, titanium, zirconia or advanced chip. A hand crank supplies 16 KE/t in the current code; the proposed 8 KE/t demand leaves startup reachable. Shafts and water/wind power automate it; the existing electric motor can drive the same table later.

The shared form engine currently handles JE-paid batches. A kinetic input adapter is implementation work: use `KineticConsumer` and the existing KE network, the same durable work/escrow/output reservations, and a declared KE-only process profile. Do not silently treat KE as JE or accept both sources for one paid tick. The present copper motor is 75% efficient, so a steady 8 KE/t load draws 11 JE/t after rounding; its energy cost belongs in the line's economy.

**Starting concentration recipes.** Each row consumes 8 exact-family sand and 500 mB water, runs 200 ticks at 8 KE/t, and returns 500 mB new Cloudy Sand Washwater alongside its solids. The two solid outputs must both have reserved space.

| Feed | Family concentrate | Depleted Mineral Sand | Nominal concentration work |
| --- | --- | --- | --- |
| Titanium-rich sand x8 | Titanium Mineral Concentrate x2 | x6 | 1,600 KE |
| Zircon-rich sand x8 | Existing Zircon Concentrate x4 | x4 | 1,600 KE |
| Rare-earth-rich sand x8 | Rare-earth Mineral Concentrate x2 | x6 | 1,600 KE |
| Garnet-rich sand x8 | Garnet Concentrate x4 | x4 | 1,600 KE |

These are inventory accounting units, not claims about real mineral grades or equal item masses. The deliberate 2-sand-per-concentrate zircon yield matches the existing regional recipe. Mechanical preparation trades a large footprint, moving shaft and separate residue handling for much lower power cost; the compact Conditioner stays useful for small lines.

Cloudy Sand Washwater must have a new identity. Do not alias it to existing Spent Mineral Wash, whose current recovery makes salt: that would give every washed sand an unintended salt coproduct. Shared settling/filtration treats 1,000 mB cloudy washwater into 800 mB water and 200 mB mineral-settling slurry, proposed at 160 ticks / 16 JE/t. This returns water already supplied, never more. Fresh water can feed a simple open line; recycling is optional efficiency.

Depleted Mineral Sand is a final inert material usable in explicit mortar, fill, concrete or glass recipes. It has no valuable-mineral recovery recipe, including after placement, washing, grinding or alchemical conversion. This is a separate item/block identity from all four valuable sands. Mineral-settling slurry and leach residue can be stored; a paid clay/calcite recipe may turn them into existing stabilized construction residue. No fertilizer, metal or valuable mineral is awarded from unspecified waste composition. Treatment inputs, water return and construction yield must be explicit before that optional recipe ships.

The physical roles follow actual gravity, magnetic and electrical separation practice, while the number of stages and all amounts are game simplifications. [KMML's mineral-separation description](https://www.kmml.com/manufacturing-facility/1) distinguishes those operations; it does not supply these recipe yields.

### Titanium line

**Route:** Titanium-rich sand -> Gravity Concentrator -> Titanium Mineral Concentrate -> Magnetic Drum Separator -> existing Raw Titanium -> existing chloride purification/reduction chain -> titanium ingots and their current tools, armor and components.

The concentrate represents the deposit's ilmenite/rutile-bearing feed. The drum's native preparation recipe abstracts magnetic rejection and conditioning into a compatible raw mineral feed; it does not produce pure titanium metal. Do not introduce separate ilmenite, rutile and iron inventories unless a later recipe actually uses those distinctions.

| Stage | Proposed or existing receipt | Work |
| --- | --- | --- |
| Mineral cleanup, new | 2 titanium concentrate + 250 mB water -> 2 existing Raw Titanium + 250 mB cloudy washwater | 200 ticks x 32 JE/t |
| Chlorination, existing | 1 Raw Titanium + 1 Coke + 500 mB chlorine -> 1,000 mB crude titanium chloride + 250 mB CO2 | 240 x 64 JE/t |
| Purification, existing | 1,000 mB crude chloride -> 750 mB purified chloride + 250 mB existing Spent Mineral Wash | 240 x 48 JE/t |
| Reduction/cleanup, existing | 750 mB purified chloride + 2 magnesium ingots -> 1 Titanium Sponge + 2 Dry Magnesium Chloride | 400 x 96 JE/t |
| Melting, existing | 1 Titanium Sponge -> 1 titanium ingot | Preserve the existing Arc Furnace cost |

The new preparation stage costs 3,200 JE per raw titanium, before the shared sand concentration and motor overhead. Existing chlorination, distillation and reduction cost 65,280 JE per sponge. Reprocessing both dry salt items adds 51,200 JE and returns the same two magnesium ingots plus 500 mB chlorine. First magnesium still comes from the independently reachable coastal line; recovered salts cannot bootstrap it. Arc melting, reagent manufacture and the original water supply are additional costs, not included in that subtotal.

**Pigment payoff:** add an alternate Mineral Conditioner/Industrial Oven branch from Raw Titanium to Titanium White Pigment. Proposed receipt: 1 Raw Titanium + 250 mB sulfuric acid + 250 mB water -> 1 prepared pigment cake + 500 mB pigment-process liquor (240 ticks / 48 JE/t); calcine 1 cake -> 1 Titanium White Pigment (240 / 48). One pigment crafts 4 existing White Dye. The pigment liquor is a distinct chemical stream handled through explicit shared recovery/treatment, not cloudy washwater or a free iron source. This is a simplified game pigment route, not a real production recipe. It consumes the same titanium allocation as metal: one feed cannot award both.

Keep existing titanium material IDs and consumers. Do not force pigments into ordinary white dye recipes or require titanium to build the drum, chlorinator, distillation station or first magnesium cell.

### Zircon line

**Route A, direct mineral use:** Zircon-rich sand -> gravity concentration -> existing Zircon Concentrate -> grinding/grading -> Zircon Flour -> reusable foundry tooling.

**Route B, existing specialist ceramics:** Zircon Concentrate -> lye preparation -> zirconia precursor -> Industrial Oven -> zirconia powder -> Conditioner -> Metal Press -> Industrial Oven -> Precision Finisher -> existing Zirconia Cutting Inserts -> existing polymer molds and extrusion dies.

Preserve the existing receipts: 2 concentrate + 500 mB lye -> 1 precursor at 240 ticks / 64 JE/t; 1 precursor -> 1 powder at 320 / 80. Powder preparation uses 125 mB water; pressing, firing and finishing keep their existing costs. One fired blank still yields 2 inserts. Thus 8 regional sand supply 4 concentrate, enough for 2 powder and ultimately 4 normal inserts, before any yttrium branch.

For direct use, add a reusable **Mineral Grinding and Grading attachment** to shared workshop processing, rather than a zircon-only grinder. Its first construction and the grit/flour preparation recipes use an earlier iron grinding surface and ordinary screen; they must not require the garnet wheel, zircon foundry insert or flour/grit they produce. Proposed receipt: 1 zircon concentrate -> 1 Zircon Flour, 120 ticks / 8 KE/t. One flour plus one existing basic ceramic lining produces a reusable Zircon Foundry Insert, fired in the existing kiln. The insert gives the Small Crucible Furnace's existing metalworking recipes a 20% shorter processing time at unchanged recipe yield. Fuel cost stays at least the original paid heat/work budget; this is a throughput sidegrade, not a source of free metal or unaccounted fuel savings. It does not grant otherwise unavailable melting temperatures.

The workshop currently has a heat-insert path, not this proposed zircon tool behavior. Implement a distinct compatible tool position or saved modifier with explicit migration and batch-time locking. It must not displace an existing heat insert or become a required replacement part. A recipe that names a tool needs that actual tool behavior before being counted complete.

Do not use raw zircon flour as if it were zirconia or zirconium metal. More decorative glaze/refractory products and zirconium metal remain later branches with real consumers. The first release finishes both current cutting-tool production and the foundry-insert payoff.

### Rare-earth line

**Route:** Rare-earth-rich sand -> gravity concentration -> concentrate -> shared acid digestion -> Filter Press -> mixed rare-earth solution -> Chemical Separation Rack -> named salts -> Industrial Oven -> cerium oxide, neodymium oxide and yttrium oxide.

Use the approved 5 x 6 x 5 rack concept for a real chemical-separation capability. A magnetic drum can later reduce treatment costs, but do not require a redundant magnetic pass after the named concentrate has already been recovered. The rack's first construction uses steel, copper, basic circuits, existing polymer stock, ordinary ceramic parts and pumps. It needs no named rare-earth product, magnet motor or advanced chip.

**Proposed first batch ledger:**

| Stage | Inputs | Reserved outputs | Work |
| --- | --- | --- | --- |
| Digestion, existing compatible Dissolution Chamber/Bulk Reactor | 2 rare-earth concentrate + 500 mB sulfuric acid + 500 mB water | 1,000 mB Rare-earth Leach Slurry | 300 ticks x 64 JE/t |
| Filtration, existing Filter Press | 1,000 mB leach slurry | 750 mB Mixed Rare-earth Solution + 250 mB cloudy washwater + 1 Mineral Leach Residue | 200 x 32 |
| Separation, new rack | 750 mB mixed solution + 250 mB HCl; retained Separation Cartridge | 2 Cerium Salts + 1 Neodymium Salt + 1 Yttrium Salt + 1,000 mB Spent Rare-earth Separation Liquor | 400 x 96 |
| Calcination, existing Industrial Oven | 1 named salt | 1 corresponding named oxide | 240 x 48 per salt |

The two concentrate units allocate four abstract rare-earth product lots: two cerium, one neodymium and one yttrium. The four lots are split once, never duplicated across selectable recipes. Including all four calcinations, the proposed chemical work totals **110,080 JE** per two concentrate, excluding upstream concentration, reagent manufacture and later finished products. The fictional batch ratio is not a claim about natural monazite composition.

The Separation Cartridge is a reusable installed tool, proposed from ordinary filter cloth, polymer stock, ceramic plate and copper wire. It represents the rack's bounded separation internals; its paid reagent recipe abstracts repeated chemical stages. It is not a claim that ordinary filter cloth alone separates dissolved elements. Never consume the cartridge every batch or allow pipes to extract it mid-process. Spent separation liquor has a distinct identity, can be stored and must not use another waste fluid's recovery recipes. Optional paid treatment may recover some input water/reagent with explicit losses; no free metals, salt or useful acid are inferred. The first playable line can export it to shared tanks without waiting for the broader pollution system.

The rack needs two fluid inputs, one fluid output, three solid product slots and one locked tool slot, within the six-tank limit. All three salts and the spent liquor must fit before any input is consumed. Single-product focus modes may change which fraction is stockpiled, but cannot silently discard the other fractions or turn the entire feed into the selected element. Do not add focus modes to the first implementation.

**Named products must ship with these consumers:**

| Product | Finished route | Immediate payoff |
| --- | --- | --- |
| Neodymium oxide | Conditioner: 1 oxide + 500 mB HCl -> 500 mB prepared neodymium chloride + 250 mB recovered water (200 ticks / 48 JE/t); existing compatible retort: that chloride + 2 Mg -> 1 Neodymium Metal + 2 dry MgCl2 (400 / 96); Alloy Smelter: 2 Nd metal + 1 Ferroboron -> 4 existing Neodymium Magnets (200 / 64) | Existing magnet motors, dynamos and advanced-engine recipes; retain old mixed-oxide alternatives |
| Cerium oxide | 2 oxide + the existing Felt Polishing Head -> reusable Cerium Polishing Head; enable optical finishing at 160 ticks / 48 JE/t instead of 200 / 48, same one-lens output | Existing polished lenses, Spyglasses and Range Finders; no doubled lenses or removal of the old felt head |
| Yttrium oxide | 4 zirconia powder + 1 yttrium oxide -> 4 Yttria-stabilized Zirconia Blend; use shared conditioning/pressing/firing and finish each blank into 3 existing inserts rather than 2 | Better yield of the existing cutting inserts, molds and extrusion dies, paid for by the separate rare-earth line |

Neodymium metal is never made by pressing mixed oxide. Its reduction reuses the retort's existing one solid feed/two solid outputs and fluid input layout through a new explicit capability recipe. Keep titanium jobs, tanks and old batches intact. Magnesium/chlorine recovery is paid through the existing cell and must return no more than the batch supplied. The named chloride and separated oxide must never be accepted as titanium feeds through a broad reagent tag.

The cerium/yttrium applications have real material precedents: [Solvay describes cerium oxide optical polish](https://www.solvay.com/en/product/opaline-polissage), and [CeramTec describes yttrium-reinforced zirconia cutting ceramics](https://www.ceramtec-industrial.com/en/products-applications/textile-machinery/cutters). The heads, quantities and game bonuses above are proposed Jugcraft choices.

Allow existing monazite and mixed Rare Earth Oxide to feed the new line with explicit equivalent-lot recipes. Proposed equivalence: 2 monazite OR 4 mixed oxide can supply the same four-lot separated batch, paying appropriate digestion/dissolution costs. They are alternatives to the two-concentrate feed, never additional free inputs or reversible conversion pairs. Keep named Ce/Nd/Y stock out of generic mixed-oxide magnet tags, so cerium and yttrium cannot masquerade as neodymium.

The initial three products are not the final roster. Later named rare earths need independent consumers and an additive fraction ledger; do not reinterpret existing saved solution or erase a previous fraction to add another element.

### Garnet line

**Route:** Garnet-rich sand -> gravity concentration -> Garnet Concentrate -> mechanical grinding/grading -> usable Garnet Grit -> reusable workshop abrasives.

Use the same Mineral Grinding and Grading attachment as zircon. Proposed receipt: 1 garnet concentrate -> 1 Garnet Grit, 120 ticks / 8 KE/t. The first release needs one useful grit grade, not several nearly identical storage items. Therefore 8 sand supply 4 grit before tool construction. No acid, rare-earth magnet, high-purity line or electrostatic separator is necessary for that first payoff.

| Finished item | Proposed construction | Useful behavior |
| --- | --- | --- |
| Garnet Sanding Block | 1 grit + 1 wool + 1 plank | Reusable Workshop Bench tool: 1 Cut Sandstone -> 1 Smooth Sandstone, or 1 Cut Red Sandstone -> 1 Smooth Red Sandstone, at 200 ticks of workshop work; no duplicated blocks or universal scraping |
| Garnet Grinding Wheel | 4 grit + 1 iron ingot + 1 clay binder, fired in the existing kiln | Reusable tool on the shared kinetic attachment: 1 existing Steel Shaft Blank -> 1 existing Precision Shaft at 400 ticks / 16 KE/t; also an alternative Precision Finisher tool for that same recipe at 300 ticks / 48 JE/t |

The existing diamond head retains its full recipe set and faster 200-tick baseline. Garnet must not substitute for diamond on fired alumina/zirconia or serve as the final optical polish. Cerium supplies the later optical improvement. The wheel is one saved tool used by either compatible station, not two duplicated wheel/head products. Both sanding recipes and the steel-shaft finishing recipes ship with the tools. The sanding block is manually operated and reusable; its sandstone finish is an intentional building convenience alongside existing heat-based routes. Model the wheel and reserve its socket throughout a batch, using the existing tooling rules.

Do not introduce routine wheel replacement or abrasive-refill maintenance. Consumable blasting media and abrasive waterjet cutting are later optional applications, with spent-grit recovery strictly bounded if added. [GMA's waterjet application description](https://gmagarnet.com/en/americas/waterjet-applications) supports garnet's abrasive role; it does not establish these reusable-tool recipes or permit every hard ceramic job.

### Equipment, controls and construction boundaries

| Equipment | Role in this delivery | Construction and implementation constraint |
| --- | --- | --- |
| Gravity Concentrator, 4 x 6 x 3 | All four mechanical concentration recipes | New form/KE adapter; shaft plus one water input and washwater output; two solid outputs; reachable pre-electric bill |
| Mineral Grinding and Grading attachment | Zircon flour and garnet grit | Shared kinetic workshop capability, not four mineral-specific mills; retained tool/grade controls and conservative saved-work schema |
| Magnetic Drum Separator, 3 x 4 x 3 | Titanium feed preparation | New electrical form; copper/iron electromagnet, steel/bronze mechanics, basic circuit; never its own titanium/rare-earth output |
| Existing chemical and ceramic forms | Titanium, zirconia, rare-earth digestion, named oxides and metal reduction | Extend actual compatible capabilities and existing recommended machines, with saved tank/slot roles preserved |
| Chemical Separation Rack, 5 x 6 x 5 | Three named rare-earth salt outputs | New form, three reserved item outputs, spent-liquor outlet and a retained cartridge; steel/copper/polymer/ceramic construction |
| Electrostatic Separator, 3 x 5 x 4 | Later higher-throughput/cleaner mineral preparation | Follow the approved art brief when needed; not mandatory for every sand or every first product |

Reuse suitable owner-library art and record exact runtime source/transform provenance when implementing. Give each new form an intelligible mechanism, all-side surfaces, real ports and working/stopped animation. The two optional larger physical-separation forms are not completion requirements for the first useful four-line batch.

Every powered form uses the shared searchable recipe preview and saved recipe lock. Show actual solid and liquid quantities, tools retained, energy, time, coproduct outlets and the exact blocking reason. A stopped recipe retains its paid work and inputs through unload/restart; full waste or product storage pauses work without consuming more feed. Do not silently flush incompatible fluids on recipe changes. Sockets remain protected from automation, with player/party/claim checks on all controls and transfers.

### Delivery order and acceptance

1. **Shared preparation plus garnet and zircon.** Add family-specific concentration, KE integration, depleted sand and optional washwater recovery. Ship garnet tools and the foundry insert, preserve the existing zirconia route, and prove a hand-cranked sand-to-useful-tool path. These two lines are independently playable before rare-earth chemistry.
2. **Titanium.** Add electromagnetic preparation into the existing Raw Titanium chain, its explicit residue handling and pigment/White Dye branch. Demonstrate actual sand-derived titanium gear/components and pigment, with independent first magnesium and paid salt recovery.
3. **Rare earths.** Add digestion, filtration, separation rack, all three named products and all three consumer branches together. Demonstrate a working magnet motor, faster lens finishing and higher-yield zirconia inserts using only reachable inputs.
4. **Combined survival and release validation.** Audit the full recipes, capacities, costs, construction bills, tool slots and all old/new conversion paths. Inspect every form and its connections in a disposable showroom; then owner survival/visual review and real two-player acceptance before a coordinated release.

Required evidence includes:

- Exactly one reserve decrement per harvested/extracted block; no Fortune, generic ore processing or replacement-block reserve multiplication.
- Zero positive-return paths between valuable sand, depleted sand, concentrates, new products, alchemy values and NPC trades. Retain the legacy ordinary-sand zircon exception without broadening it.
- A complete per-family item/material-lot, fluid and energy ledger, including motor inefficiency, all initial reagents, recycling and alternate legacy routes. Proposed bonuses cannot be claimed balanced solely from a local recipe's cost.
- Full products and residues reserved atomically; insufficient power/KE, blocked waste, cancellation, break/place, unloaded sections and restart cannot create or delete a batch. Test simultaneous viewers, hoppers, pipe transfers and tools held during work.
- Old running zircon, titanium, magnesium and mixed-oxide jobs survive. Existing feature toggles retain saved registrations; disabling a consumer does not leave enabled recipes pointing at absent items. No new valuable output is approved without a reachable consumer.
- Existing sources and stock remain valid when optional integrations are absent; JEI/Encyclopedia list the actual machines and distinguish proposed equipment from implemented content.
- Rendered all-side/GUI-scale checks, mechanical motion/port visibility and bounded server-load measurements for representative connected lines. Human progression/economy/multiplayer checks remain distinct from automated tests.

The first release is complete when each sand can be harvested, processed and used for the stated payoff, with documented costs, storage and saved work. More extractor variants, electrostatic upgrades, larger plant profiles, additional rare earths and waterjet cutting remain visible follow-ups rather than prerequisites for making the four existing sands useful.

## Primary process references

Sources inform sensible roles and distinctions; game recipes remain deliberate simplifications and do not import industrial quantities as balance numbers.

- [USGS: heavy-mineral sand deposit model](https://www.usgs.gov/publications/deposit-model-heavy-mineral-sands-coastal-environments).
- [USGS: inland/coastal distribution and physical separation](https://www.usgs.gov/centers/gggsc/science/critical-mineral-resources-heavy-mineral-sands-us-atlantic-coastal-plain).
- [Iluka: inland zircon and rare-earth mineral sands](https://www.iluka.com/operations-resource-development/resource-development/wimmera).
- [International Aluminium Institute: alumina refining](https://alustory.international-aluminium.org/mining-refining/process-refining/).
- [Toho Titanium: chlorination, purification, reduction and recovery](https://www.toho-titanium.co.jp/pdf/company/companyprofile_en.pdf).
- [Iluka: rare-earth refinery process](https://www.iluka.com/media/xyfjf0fr/iluka-investor-briefing.pdf).
- [Solvay: cerium oxide polishing material](https://www.solvay.com/en/product/opaline-polissage).
- [CeramTec: yttrium-reinforced zirconia textile cutters](https://www.ceramtec-industrial.com/en/products-applications/textile-machinery/cutters).

AI-assisted planning documentation: OpenAI Codex, GPT-6 family. No gameplay feature implementation is included.

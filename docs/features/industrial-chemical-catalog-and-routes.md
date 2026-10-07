# Industrial chemical catalog and connected production routes

Status: owner-requested planning expansion, recorded 7 October 2026. This document preserves the owner's full chemical, machine, fuel and consumer request alongside scientifically corrected identities. Named branches and uses are requested scope; proposed machine assignments, simplified recipes, exact material quantities, capacities and implementation order remain to design. No gameplay is implemented here.

Owner: jimbozoomer-byte. Proposal: direct industrial-planning instructions; no separate issue.
Target entry: steel-built electrical chemistry, with later electronics, grid storage, nuclear/solar and space applications.
Related plans: [industrial chemistry and fuels](industrial-chemistry-and-fuels-plan.md), [mineral refining](mineral-sands-and-refining-plan.md), [agriculture](industrial-agriculture-plan.md), [Chemistry branch](../branches/CHEMISTRY.md), [Machine Roadmap](../MACHINE_ROADMAP.md), [TODO](../TODO.md#industrial-chemistry-gas-fuels-and-advanced-materials).

## Shared machines requested by the owner

These names describe Jugcraft machine roles. Reuse Jugcraft's shared gas/fluid, energy, recipes, inventories and side configuration; no external mod dependency, borrowed implementation/assets or separate incompatible chemical network is selected.

| Requested machine | Input and useful outputs | Capability and existing-system integration |
| --- | --- | --- |
| Electrolytic Separator | Water -> hydrogen/oxygen; aqueous brine -> chlorine/hydrogen/sodium hydroxide | Reuse/extend the existing Electrolytic Cell capability. Aqueous and molten-salt operations need appropriate recipes/electrodes; naming does not approve duplicate registrations or delete the current cell |
| Chemical Infuser | Two compatible gas feeds -> a defined compound/product stream | Shared gas reaction/synthesis role; H2/chlorine chemistry and appropriate sulfur-oxide reactions. Methanation also needs heat/catalyst capability and accounts for water output; two gases do not universally combine |
| Chemical Oxidizer | Suitable solid/dust feed + oxidant where required -> a named gas/product stream | Example: sulfur and oxygen supply sulfur dioxide. This is chemical conversion, not a universal dust-to-gas recipe or a way to erase oxygen/material requirements |
| Chemical Dissolution Chamber | Prepared mineral/solid feed + appropriate acid -> useful slurry/solution and residues | Shared acid digestion/leaching role. Heated fluorite conversion is a special compatible recipe with HF gas and calcium sulfate outputs, rather than an assertion that every dissolved block produces a slurry |
| Rotary Condensator | A compatible gas -> its liquid phase, or liquid -> gas | Phase conversion with defined units and energy; shared cooling/heating roles. Advanced cold capability is required for cryogenic fuels, with existing Cryogenic Liquefier integration reviewed first |

Gas pipes continue to handle pressurization automatically. Heat, catalyst and cooling capabilities can be explicit in recipes, but this request adds no pressure controls, compressor modules, pressure tiers, pressure-loss simulation or routine machine/catalyst replacement. Compact refining entry and optional larger plants remain selected; the first advanced digester remains substantial.

## Identities, phases and corrections

- Hydrochloric acid is aqueous **HCl**, not HCCl. Hydrogen chloride gas is HCl in a different delivery form; dissolution is not a free phase swap.
- Water electrolysis yields H2/O2. Aqueous sodium-chloride brine yields **NaOH + H2 + Cl2**, not metallic sodium. Metallic sodium needs a separate suitable molten-salt route if a consumer is later approved. [EPA chlor-alkali reference](https://www.epa.gov/sites/default/files/2020-09/documents/8.11_chlor-alkali.pdf).
- Sulfuric acid H2SO4, nitric acid HNO3, phosphoric acid H3PO4, hydrofluoric acid HF, acetic acid CH3COOH, citric acid C6H8O7 and acetylsalicylic acid C9H8O4 keep their distinct identities.
- Tetramethylammonium hydroxide is **TMAH, (CH3)4N+ OH-** (also written (CH3)4NOH); it is an alkaline developer, not another acid.
- Piranha solution is a **mixture of sulfuric acid and hydrogen peroxide**, not a new pure molecule or a developer interchangeable with TMAH.
- **VOSO4 is vanadyl sulfate**, a vanadium-electrolyte precursor. A functioning vanadium flow battery has two coupled electrolyte sides/oxidation-state roles, not one freshly filled fuel tank generating power.
- **LiPF6 is lithium hexafluorophosphate**, an electrolyte salt; it does not replace the battery's electrodes, separator, solvent or assembly.
- **PTFE** is the fluoropolymer commonly associated with the Teflon trademark. Fluorocarbon intermediates and PTFE are different products; HF alone does not polymerize directly into PTFE.
- Silane **SiH4** is a deposition precursor used by CVD equipment, rather than being the deposition method itself. Deposition quality is a process capability, not a promise of automatically flawless films.
- RP-1 is a highly refined, low-sulfur kerosene grade. Cryogenic liquid methane, hydrogen and oxygen retain their respective chemical identities while changing phase; normal gas-pipe pressurization does not supply free refrigeration.

## Complete requested chemical and consumer catalog

The consumer column records the owner's requested destinations. Proposed production details need source/recipe audits. Food, medicine and technical products use appropriate finished forms; industrial acid is not itself a consumable food or medicine item.

| Chemical/material | Supply or processing direction | Requested end products and planning distinctions |
| --- | --- | --- |
| Sulfuric acid, H2SO4 | Sulfur preparation/oxidation -> sulfur oxides -> conversion/absorption; retain the existing starter source during expansion | Central industrial reagent for phosphoric acid/advanced fertilizers, lead-acid batteries, mineral processing, petroleum refining and later electronics |
| Hydrochloric acid, HCl | Hydrogen/chlorine gas reaction, followed by controlled aqueous recovery | Corn-derived glucose syrup through starch hydrolysis and downstream finishing; also appropriate polymer/refining links. Fructose syrup requires additional conversion, rather than being assumed from acid hydrolysis |
| Nitric acid, HNO3 | Extend existing ammonia/oxygen chemistry through shared appropriate conversion/absorption roles | Ammonium-nitrate agricultural fertilizers; later finished game ammunition/weapon charges and rocketry chemistry. Record special aged pine/maple finishes as an owner-requested woodworking application to review, not a universal real-world aging method |
| Phosphoric acid, H3PO4 | Phosphate-rock wet processing, filtration and useful recovery | Triple-superphosphate fertilizer, advanced technology inputs and food-grade cola production |
| Hydrofluoric acid, HF | Prepared fluorite CaF2 + concentrated sulfuric acid + appropriate heat -> HF stream and calcium sulfate; recover a suitable aqueous/purified form | Glass frosting, selective oxide etching/wafer preparation, advanced semiconductor processing and fluorochemical intermediates leading to PTFE |
| Acetic acid, CH3COOH | Vinegar/ethanol-processing connection, with suitable recovery for industrial uses | Vinegar; vinyl-acetate monomer -> PVAc glues/adhesives; appropriate acetate/solvent connections for plastics and synthetic-fibre manufacture. It is not the monomer itself |
| Citric acid, C6H8O7 | Cultivatable lemons/limes/citrus -> extraction and suitable recovery; later fermentation is a candidate expansion | Food preservation, sour candy and soft drinks. Citrus farming remains independently useful; availability and food recipes need design |
| Acetylsalicylic acid, C9H8O4 | Dedicated later pharmacy preparation and finished-product assembly | Medicine/aspirin-style game item; precursor producers, quantities and gameplay effects remain to design. It is not acetic acid alone |
| TMAH, (CH3)4N+ OH- | Later independently reachable organic-reagent preparation | Positive-photoresist development after exposure; do not add several alternative developers with the same gameplay job |
| Piranha cleaning solution, H2SO4/H2O2 | Shared solution preparation using sulfuric acid and independently obtainable hydrogen peroxide | Organic-residue cleaning/stripping in advanced chip manufacture; no real mixture ratios or operating procedure are selected |
| Hydrogen peroxide, H2O2 | Independently reachable later chemical supply, with exact route pending | Cleaning-mixture precursor; not a mandatory reagent ladder before first electricity |
| Silane gas, SiH4 | Later reachable silicon/hydrogen precursor preparation and purification; exact source route pending | CVD silicon films on suitable wafers; advanced chips and reviewed solar/precision applications |
| Boric acid, H3BO3 | Existing borax-related supply -> appropriate acid recovery/purification is a candidate | Soluble-boron control for a suitable nuclear-reactor design; a boron-precursor route for high-tech solar panels. Boric acid is not automatically a universal controller for every reactor or direct finished semiconductor dopant |
| Lithium hexafluorophosphate, LiPF6 | Later lithium/fluorine/phosphorus chemical preparation and electrolyte formulation | High-tier lithium-ion cells/banks. Link fluorite and phosphate industries; supply suitable electrodes, separator and solvent independently |
| Vanadyl sulfate, VOSO4, and formulated vanadium electrolytes | Suitable vanadium feed -> sulfuric processing -> recovery/formulation | Major grid-scale redox-flow storage. Extend the existing vanadium electrolyte/flow battery instead of duplicating them |
| Sulfamic acid, NH2SO3H / H3NSO3 | Later process-specific acid and compatible plating-salt preparation | Advanced battery-terminal electroplating, especially a defined nickel-sulfamate bath; zinc uses are requested for review. Acid, metal salt, electrolyte and plated terminal are distinct |
| Ethylene gas, C2H4 | Named product from existing refinery/cracking feeds | Polyethylene monomer and selected PVC/vinyl-acetate intermediate connections |
| HDPE / LDPE polyethylene | Product-specific ethylene polymerization -> distinct appropriate pellets/resin -> forming | HDPE for rigid plastics; LDPE for flexible film products, with shared useful shaping equipment. Exact recipes/catalysts differ; pipe pressure is automatic |
| Vinyl acetate / PVAc | Appropriate acetate monomer preparation -> polymerization/formulation | Industrial glues/adhesives, with existing agricultural adhesive routes preserved |
| Fluorocarbon intermediates / PTFE | Fluorine supply -> named intermediate/monomer preparation -> polymerization/forming | Specialist chemical-compatible components/coatings with actual consumers; not a universal requirement for the first HF-producing machine |
| Gasoline / kerosene / RP-1 | Existing crude-oil fractionation and finishing, with reviewed product-grade roles | Typical engines, jets and the established RP-1 rocket path; preserve saved fuel identities/consumers |
| Syngas, CO/H2, and synthetic hydrocarbon feed | Coal/biomass gasification or compatible gas reforming -> cleanup/conditioning -> cobalt-catalyzed Fischer-Tropsch synthesis | Synthetic refinery feed, followed by shared fractionation and appropriate upgrading into usable gasoline/other fuel fractions; water and other outputs accounted once |
| Liquid methane CH4 / LOX O2 | Existing methane production -> cold conversion; oxygen -> liquefaction | Intermediate/later rocket family and optional Mars-like CO2/water-based local fuel production |
| LH2 / LOX | Electrolysis/suitable hydrogen supply -> advanced liquefaction; liquid oxygen supply | Higher-tier chemical rockets and deeper space exploration under the owner's selected progression |
| Hydrazine N2H4 / MMH CH3NHNH2 | Closed, abstract game propellant-production recipes with exact precursor route pending | Owner-requested early/atmospheric rocket family and satellite/station maneuvering; decide the first named fuel without forcing both near-equivalent options everywhere |
| Dinitrogen tetroxide, N2O4 | Appropriate nitrogen-oxide preparation and controlled phase/storage capability | Storable liquid oxidizer for the selected compatible hydrazine-family bipropellant modules |

Finished ammunition and propulsion items connect to their existing game systems. This catalog selects no real explosive/propellant formulations, charge ratios or operational manufacturing instructions. Canonical names are recorded so later game recipes can stay abstract and balanced.

## Sulfuric acid as the industrial backbone

The owner explicitly calls sulfuric acid the "King of Chemicals" and wants it central across the mod. It should support several profitable product lines, not become an arbitrary ingredient in every unrelated machine.

Proposed shared-machine arrangement: sulfur preparation -> Chemical Oxidizer with appropriate oxygen supply -> SO2 -> heated catalytic Chemical Infuser conversion -> SO3 -> controlled absorption/hydration capability -> sulfuric acid supply. The Electrolytic Separator provides useful gases; it does not split water directly into sulfuric acid.

Real contact-process manufacture separates oxidation and absorption; direct hydration/absorption may be combined into a readable game recipe, clearly labeled as a simplification. A Rotary Condensator changes phase; it does not by itself turn SO3 into H2SO4. See [EPA's sulfuric-acid process description](https://www.epa.gov/sites/production/files/2020-09/documents/8.10_sulfuric_acid.pdf).

Define an independently reachable first conversion capability before depending on advanced catalysts. In particular, a sulfuric-acid-dependent vanadium recovery route cannot supply its own first sulfuric-acid catalyst. Later catalyst improvements are proposals; existing acid recipes remain available until replacements are proven reachable.

Use an ordinary industrial acid supply and add concentration/purity distinctions only for demanding consumers such as HF production, wafers or food preparation. Exact grades are not selected yet. Concentration needs material/water accounting, rather than relabeling dilute stock for free.

## Fluorite, hydrofluoric acid and phosphate wet processing

The selected HF connection is **fluorite/calcium fluoride + concentrated sulfuric acid + heat**. The balanced relationship is CaF2 + H2SO4 -> CaSO4 + 2 HF. A compatible heated Chemical Dissolution Chamber can represent the chosen game conversion; real production uses purpose-built heated process equipment. Account for calcium-sulfate output and the initially gaseous HF stream, with subsequent phase/solution recovery as needed. [EPA HF process reference](https://archive.epa.gov/emergencies/docs/chem/web/pdf/hydro.pdf).

The first fluorite supply, concentrated acid capability and compatible chamber must be obtainable before HF/PTFE exists. Fluorite is a new feedstock candidate needing an existing-content/worldgen audit and a useful regional producer; do not promise all ordinary sand supplies it. Exact starter-compatible construction remains to review.

For phosphates: phosphate rock -> preparation -> sulfuric-acid dissolution -> slurry -> filtration/solid-liquid separation -> recovered phosphoric acid and accounted calcium-sulfate/residue streams. This is the owner's requested **hydrometallurgical/wet-processing route**. Mineral composition affects actual residues; do not reuse one universal slurry for every ore. [EPA describes phosphate-rock wet processing](https://www.epa.gov/radiation/tenorm-fertilizer-and-fertilizer-production-wastes).

Phosphoric acid plus suitable phosphate feed supports triple-superphosphate fertilizer. Preserve existing simple fertilizer until expanded alternatives are reachable; nutrient categories, application costs and agronomy benefits need the agriculture feature's design. [EPA identifies the triple-superphosphate connection](https://www.epa.gov/stationary-sources-air-pollution/phosphate-fertilizer-industry-new-source-performance-standards-40).

## A manageable advanced chip line

The owner requests chemical depth while limiting reagents that do substantially the same job. **Proposed core:** one sulfuric/peroxide cleaning solution, one TMAH developer, HF for selective oxide removal, and silane for later silicon deposition. These have different roles; this four-role selection is a proposal to confirm, not an owner answer already received.

A proposed sequence is wafer preparation -> coating with one suitable photoresist -> exposure through a reusable pattern -> TMAH development -> material-specific etching -> stripping/cleaning -> assembly. Later CVD adds a silicon film with silane. HF handles appropriate oxide/glass work, not universal silicon/metal etching. Cleaning is not development, and deposition is not etching.

Use selected shared finishing for wafer preparation. Existing lithography/assembly provides the foundation; ordinary basic circuits, first electricity and existing chip consumers stay reachable. Decide whether later steps are shared stations or readable stages inside a larger cleanroom, rather than adding a mandatory unique machine for every wash.

[Clemson's fabrication facility](https://www.clemson.edu/cecas/ece-clean-room/equipment.html) distinguishes coating/exposure, TMAH development, HF oxide etching and piranha stripping. [MicroChemicals' developer documentation](https://www.microchemicals.com/dokumente/application_notes/development_photoresist.pdf) supports resist-specific developer choices. The proposal deliberately avoids importing all alternative cleaners/developers from those catalogs.

Silane feedstock, photoresist, peroxide, purified wafer supplies and first equipment controls need independent earlier routes. Silane deposition supplies films on substrates, not a free replacement for bulk silicon/crystal/wafer production. [Air Liquide describes silane's silicon-deposition uses](https://encyclopedia.airliquide.com/silane).

## Polymers, food, woodworking and medicine

Existing refinery feeds retain the selected detailed PVC connection: named ethylene -> ethylene dichloride -> vinyl chloride -> PVC, with appropriate HCl recovery/reuse. The owner's polyethylene request adds HDPE/LDPE product families, pellets and film-sheet manufacture. Rigid products and flexible films have distinct consumers. Molding covers the selected housings, fittings, insulation, gaskets, hoses and vehicle panels; the appropriate stock determines properties.

HDPE/LDPE use distinct polymerization routes/grades, not a manual network-pressure setting. Film manufacture is a suitable shared forming/extrusion role to map alongside the Polymer Molding Press, rather than assuming every sheet is injection molded. [Dow's polyethylene product families](https://www.dow.com/en-us/product-technology/pt-polyethylene.html) support the different rigid/flexible applications.

Acetic acid connects vinegar production to industrial acetate intermediates and PVAc adhesives, without turning acetic acid directly into glue. [Celanese identifies vinyl-acetate monomer as a PVAc precursor](https://www.celanese.com/en/products/vinyl-acetate). Keep natural/earlier adhesives useful; the new request adds a focused branch, not a compulsory full finishes industry before refining.

Corn -> milling/starch preparation -> appropriate acid-assisted hydrolysis -> finishing/neutralization/purification -> glucose syrup supplies candy/drinks and other reviewed food recipes. [Cargill describes cereal sweeteners from acid and/or enzyme hydrolysis](https://www.cargill.com/food-beverage/emea/production-process). Record citrus extraction/preservation, sour candy, cola and vinegar as connected agricultural outputs. Exact crops, food-grade handling, drink effects and recipe stations remain to design.

Nitric-acid-treated aged pine/maple is an owner-requested decorative woodworking branch; exact color variants/process applicability need review. Aspirin-style medicine is a distinct later pharmacy product with independently obtainable precursors and game effects to design. These optional branches do not gate semiconductor or bulk-fuel progression.

## Refined and synthetic transport fuels

Crude oil -> existing fractional distillation -> refinery fractions -> appropriate finishing -> gasoline/kerosene products. The owner's approximately 350 C heating example describes a hot industrial feed, not a universal game temperature cutoff or a guarantee of finished gasoline from every crude fraction. Jets and suitable engines are actual consumers; the existing kerosene registration already displays RP-1.

Coal or farm-derived biomass -> appropriate gasification; gas feeds -> appropriate reforming/conversion -> **CO/H2 syngas** -> cleanup/conditioning -> heated **cobalt-catalyzed Fischer-Tropsch** capability -> mixed synthetic hydrocarbons/water -> shared refinery columns and appropriate cracking/reforming -> usable gasoline and other useful fractions.

The owner selected cobalt for this synthetic-fuel route; its obtainable feed and initial reusable catalyst capability need design. It is distinct from the already selected nickel methanation bed. Reuse gas cleaning, reaction, condensation and refining where compatible; exact station roles remain proposals. [NETL's coal-to-liquids project](https://netl.doe.gov/node/2251) produces an FT hydrocarbon/wax feed from gasifier syngas.

FT does not directly select one pure gasoline molecule or guarantee a suitable engine fuel by distillation alone; review product fractions and required upgrading with existing refinery equipment. Low sulfur requires appropriate feed cleanup. Synthetic provenance alone does not give identical finished gasoline more energy than the same grade made from oil.

Audit external feedstock energy, hydrogen production, heating, automatic-pressure abstraction, separation, distillation, upgrades and generator returns together. No coal/biogas double counting or positive water/CO2/electricity fuel loop. Use explicitly composed mixtures and bounded recipes rather than per-molecule simulation.

## Rocket families and phase conversion

| Owner-requested role | Fuel and oxidizer | Selected game direction and implementation boundary |
| --- | --- | --- |
| Early/atmospheric rockets; station/satellite maneuvering | Hydrazine or MMH; compatible bipropellant modules use N2O4 | Preserve this owner-requested tier assignment as game design, not a claim that these are confined to low-tier real rockets. Satellite/station propellant supply is part of setting up that infrastructure |
| Established refined liquid rocket option | RP-1 + LOX | Keep existing kerosene/liquid-oxygen IDs and working motors; further product-grade/space uses need review |
| Improved methane rockets | Liquid methane + LOX | Owner wants an efficient cleaner-burning next rocket family. CO2/water-fed methane production can support future Mars-like outposts when that destination is designed |
| Higher-tier chemical exploration rockets | LH2 + LOX | Owner wants a higher-efficiency advanced chemical-rocket tier; exact payload/range/fuel budget and cold-storage costs need balance |

[NASA documents MMH/N2O4 in satellite maneuvering](https://www.nasa.gov/technology/testing-continues-for-satellite-servicing-capabilities/). Hydrazine also has monopropellant uses; pairing and engine compatibility must be explicit. N2O4 is a storable oxidizer under controlled conditions, not a promise that every environment leaves it liquid without capability checks.

The owner's methane/hydrogen performance order is a game target. Compare propulsion efficiency, fuel density, storage/cooling and vehicle performance separately; hydrogen is not universally the best fuel in every mission. [NASA's propulsion fundamentals](https://ntrs.nasa.gov/api/citations/20140002716/downloads/20140002716.pdf) discusses efficiency/density tradeoffs.

Extend or reuse the existing Cryogenic Liquefier where appropriate; Rotary Condensator interfaces must account for reference gas units versus liquid quantities and powered cold capability. Liquefaction and vaporization cannot multiply molecules or bypass energy costs. No automatic boil-off/engine-damage upkeep mechanic is selected just because the real propellants are cryogenic.

Existing terrestrial solid and RP-1 routes remain until a reviewed transition provides reachable successors. This catalog plans future space connections; it does not implement a dimension, launch system or space station.

## Battery progression, major grid storage and other advanced uses

The latest ten-answer batch chooses **general storage progression first**, deferring portable packs and specialty high-output banks. The same message explicitly requests sulfuric-acid lead-acid storage, LiPF6-based later lithium-ion batteries and vanadium-based major grid storage. These named chemistries expand the general ladder; they do not automatically approve every portable/specialty variant.

- Lead-acid: appropriate lead/electrode components plus sulfuric electrolyte and assembled cells/banks. Entry construction remains practical.
- Lithium-ion: LiPF6 in a suitable formulated electrolyte plus independently reachable electrodes/separator/solvent and cell assembly. Keep current lithium-cell/bank IDs until transition details are designed. [DOE's lithium-ion electrolyte research](https://www.energy.gov/sites/prod/files/2014/03/f10/es024_jow_2012_o.pdf) identifies LiPF6-containing formulations.
- Vanadium redox flow: vanadyl-sulfate/formulated electrolyte supply, paired functional sides, cell stack, pumps and tanks provide major factory/grid storage. Build on current vanadium electrolyte and Flow Battery. Tank volume and stack throughput may support expansion; exact layout/capacities remain open. [PNNL describes coupled vanadium electrolyte sides](https://www.pnnl.gov/available-technologies/all-vanadium-redox-flow-battery-based-supporting-solutions-containing).
- Interpret the owner's "infinite recharge cycles" as **no routine cycle-degradation or electrolyte replacement in the game**, not infinite energy, perfect efficiency or an assertion of immortal real equipment. Charge/discharge losses, pumping, maximum capacity and stored-energy accounting still apply; electrolyte provides capacity, not free initial charge.
- Later sulfamic/sulfamate plating adds compatible advanced terminals. A nickel-sulfamate bath is a defined salt/electrolyte route; zinc compatibility still needs review. [A plating-material manufacturer distinguishes nickel sulfamate](https://www.nihonkagakusangyo.co.jp/en/chemicals/nickel-sulfamate/) from the acid itself.
- Boric acid can supply an appropriate reactor's soluble-boron control; solar manufacture needs a defined purified boron-precursor/doping route. [NRC documents boric-acid use in reactor systems](https://www.nrc.gov/documents-reports/generic-communications/generic-letters/1985/gl85016). Reactor design and panel benefits are future consumer work.

## Existing foundations and delivery boundaries

At main snapshot `8ca8aee59`, inspected sources already include water/brine electrolysis, sulfuric/nitric chemistry, simplified phosphate fertilizer/leaching, PVC/polymerization, lithography, lithium storage, vanadium electrolyte/Flow Battery, kerosene/RP-1 and liquid oxygen. These are source observations, not new gameplay tests:

- [Chemistry source data](../../tools/petro.py): current vanadium electrolyte comes from sulfuric-acid treatment of an asphalt-binder input; current lithography has sulfuric/nitric alternatives. Named VOSO4 processing and the new wafer stages are future expansions.
- [Flow Battery feature](flow-batteries.md): existing electrolyte determines capacity; it is not consumed as fuel.
- [Electronics](electronics.md), [chlor-alkali](chlor-alkali.md), [liquid fuels](liquid-fuels.md) and [rocketry](rocketry.md): preserve existing registrations and reachable consumers while adding depth.

Implement in focused slices: shared machine mappings/acid supply; reachable fluoride/phosphate processing; named polymer products; distinct advanced wafer operations; refined/synthetic fuel connections; later rocket and storage expansions. Exact sequence within that scope remains to plan. Cross-specialty food/medicine/wood finishes remain useful independent side branches.

Every new reagent needs a producer, consumer, saved identity, appropriate phase/unit mapping and a first construction path without its own output. Verify residues/recovery, atomic blocked-output behavior, energy/material loops, tank transport, restart/unload, multiplayer and bounded factory workloads when code is implemented. Nuclear, solar, vehicles and weapons need their own consumer designs.

Documentation-only contribution; no runtime recipes/assets, dependencies, saves, worldgen or platform pins changed. Primary sources inform process roles; original game-planning prose uses repository MIT. No new gameplay/build test evidence is supplied.

AI-assisted planning documentation: OpenAI Codex, GPT-6 family.

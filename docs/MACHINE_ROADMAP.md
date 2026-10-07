# Machine roadmap

Status: **proposals for discussion, not approved or implemented** unless marked ✅. How implemented things connect is documented in [TECH_TREE.md](TECH_TREE.md). Machines are grouped into branches: **Mechanical** (shape and mix of materials), **Fluids** (moving liquids) and the planned **Chemistry** branch ([branches/CHEMISTRY.md](branches/CHEMISTRY.md)).

## Progression and player guidance targets

On 6 October 2026 the owner selected approximately **2–4 active hours to first electricity** from a fresh world when following known recipes, excluding optional building detours. This is a playtest/balance target, not measured current behavior or a timed unlock. Keep the essential ceramics/mechanical-workshop/electrical-component route manageable, with substantial sideways industry and steel as a parallel capability.

The owner also requested a full [Jugcraft Encyclopedia UI](features/jugcraft-encyclopedia.md), independently tracked in the [TODO list with ten reference images](TODO.md#jugcraft-encyclopedia). It explains both technology and magic, then their detailed pathways and quests. It opens from the inventory or a configurable keybind without being an item. Exact quest and UI details remain to design; guidance does not impose completion of every specialty.

The owner-endorsed [industrial agriculture planning brief](features/industrial-agriculture-plan.md) records product-led textile, paper, coatings, panel, linoleum and rubber workshops, shared manufacturing stations and later biorefinery applications. It adds a substantial mechanical entry with optional electrification; proposed machines and exact recipes still need focused implementation. No new routine lubricant or replacement-part upkeep is included in that scope.

The owner-endorsed [mineral-sands and shared refining plan](features/mineral-sands-and-refining-plan.md) records regional mineral-bearing sands, hybrid extraction, reusable separation/refining equipment and an initial named rare-earth set. **Further rare-earth materials and applications are explicitly planned beyond neodymium, cerium and yttrium.** Exact recipes, equipment capabilities and additional elements remain to be developed.

The owner's [industrial starter workshop plan](features/industrial-starter-workshop-plan.md) selects a small fuel-fired furnace with ceramic crucibles and one manual workshop bench: hammer for plates, wire cutters for wires, and a fuel-heated soldering tool for basic circuits under the owner's delegated design choice. Separate shaft-powered presses, wire drawers and circuit assemblers provide mechanized production with equal material yields to equivalent manual recipes. Both a small steam engine and a proposed shaft-powered waterwheel supply ongoing mechanical power; the existing electric Water Wheel keeps its role. Both a dynamo and a small fuel generator are starter electricity choices; the 2–4 active-hour checkpoint is any useful working powered machine, including a motor driving working shaft equipment. Steel remains parallel. The owner accepted the initial recipes/four equipment subtotals as a provisional playtesting baseline and chose accessible Overworld quartz plus charcoal in a heat-upgraded crucible furnace for basic silicon, with manual bellows that later accept shaft automation. Optional reusable ceramic molds add bronze/brass gear casting at the same metal cost as plate-based gear crafting. Upgrade/machine construction quantities, throughput, early item handling, fuel costs and source performance still need development.

The owner's [steel and bulk-metallurgy plan](features/industrial-steel-and-bulk-metallurgy-plan.md) selects the simple Coke Oven -> Steel Foundry route and a charcoal starter alternative in the same foundry, with coke suited to efficient bulk production. Small steel plate batches use the hammer/shared bench at the same metal yield as machine forming. The first bulk expansion includes larger foundries and presses alongside upgrades for existing machines; small workshops remain useful. Steel remains parallel to electricity and extra refining stays an expansion. Charcoal steel, hammer/bench steel plates and larger stations are planned additions. Exact carbon/work costs, equipment specifications and broader forming/casting scope still need development.

## What exists now

| Role | Implemented |
| --- | --- |
| Generation | Coal Generator (32 JE/t), Steam Generator (64 JE/t, needs water, burns coal or bitumen), Solar Panel (8 JE/t in daylight) |
| Storage and transport | Battery Box (400k JE), Copper Cable (256 JE/t) |
| Processing | Electric Furnace, Crusher (ore doubling), Arc Furnace multiblock, Alloy Smelter |

## Design rules for new machines
1. **Each machine is a consumer for something that already exists.** Name the materials it uses, and what uses its output.
2. **Shared systems come before the machines that need them.** Fluids and item logistics each get one interface, like the energy API, instead of every machine inventing its own.
3. **No new free resources.** Any yield bonus is defined once and audited by `tools/check_mod_data.py`, the way ore doubling is today.
4. **Early machines stay useful.** Later tiers are faster or more efficient, but earlier routes stay valid and the IDs never change.
5. **Keep the server cheap.** No per-tick world scans, bounded networks, and idle machines do almost nothing.

## Shared systems needed next
These unlock whole groups of machines, so they should be designed first.

| System | Why | Unlocks |
| --- | --- | --- |
| **Fluid API and pipes/tanks** ✅ implemented (Fabric Transfer API) | Crude oil, steam, acids, water and lava need to move | Refinery, chemical reactor, pump, geothermal, fluid steam input |
| **Item pipes / conveyor** (optional; hoppers work today) | Factories larger than hopper chains | Automated multi-machine lines |
| **Machine upgrades** (speed, efficiency, energy) | Progression without a new block each time | A use for rare earths, silver and lithium |
| **Energy tiers** (low / medium / high voltage, transformers) | Stops one cable type carrying unlimited power; gives aluminum and silver cable a purpose | High-demand machines, the reactor |
| **Ownership and access** | Multiplayer servers | Every machine |

## Proposed machines by tier

### Tier 1: Workshops (current power levels)
| Machine | Does | Uses | Feeds |
| --- | --- | --- | --- |
| **Alloy Smelter** ✅ implemented (2×2 multi-block with a power socket) | Two-input alloying: bronze, brass (copper + zinc), solder (tin + lead), invar (iron + nickel) | Existing metals | Machine casings, circuits, magic instruments (bell bronze, brass astrolabes) |
| **Metal Press** ✅ implemented | Ingots → plates (4 plates → gear by crafting) | All metals | Circuit assembler, casings, rocket hulls, better machine recipes |
| **Wire Drawer** ✅ implemented | Ingots → wire (copper, silver, aluminum) | Copper, silver, aluminum | Circuits, cables, motors |
| **Water Pump** ✅ implemented as the Electric Pump (also pumps lava) | Moves water into tanks and the steam generator | Fluid API | Steam, agriculture (irrigation), chemistry |

### Tier 2: Specialization
| Machine | Does | Uses | Feeds |
| --- | --- | --- | --- |
| **Electrolytic Cell** (Chemistry branch) | Brine → lye + chlorine; alumina → aluminum (replaces the arc furnace shortcut) | Salt, bauxite, power | Aluminum, soap and glass chemistry, bleach, plastics |
| **Chemical Reactor** (Chemistry branch) | Sulfur + water → sulfuric acid; phosphate + acid → fertilizer; lithium carbonate → battery compounds | Sulfur, phosphate, lithium | Farming (fertilizer), batteries, ore leaching |
| **Circuit Assembler** ✅ implemented | Silicon + copper wire + solder → basic circuit; basic circuits + silver wire + invar plate → advanced circuit | Silicon, silver, solder | Every higher-tier machine, rockets, magic-tech bridges |
| **Lithium Battery** | Small high-density battery block and a portable battery item | Lithium, aluminum | Portable tools, rovers, space |
| **Geothermal Generator** ✅ implemented (placeable, 2 blocks wide) | Lava → power | Fluid API | Nether and cave bases |
| **Wind Turbine** ✅ implemented (placeable, 3 blocks tall) | Height- and weather-scaled power | Aluminum, bronze gears | Remote and cozy off-grid bases |

### Tier 3: Industry and expeditions
| Machine | Does | Uses | Feeds |
| --- | --- | --- | --- |
| **Oil Pump and Refinery** (multiblock) | Crude oil → fuel, lubricant, plastic, asphalt; adds liquid crude oil | Fluid API, oil deposits | Rocket fuel, plastics, roads; lubricant must stay compatible with the planned oilseed farming route |
| **Combustion Generator** | Burns refined fuel for high output | Refinery | High-demand factories |
| **Rare Earth Separator** | Rare earth oxide → neodymium and friends | Monazite, acids | Magnets (motor/generator upgrades), lasers, phosphors |
| **Precision Fabricator** | Rocket parts, navigation, habitat modules | Circuits, plates, aluminum, tungsten | The rocketry and space pillar |
| **Automated Farm Machines** (harvester, planter, fertilizer spreader) | Bounded-area crop work | Fertilizer, power | The agriculture pillar |

### Tier 4: Shared wonders
| Machine | Does | Uses | Feeds |
| --- | --- | --- | --- |
| **Fission Reactor** (multiblock) | Uranium → very high power | Uranium, lead shielding, tungsten, circuits. **Needs its own hazard design first** | Late-game factories, space launch |
| **Launch Pad** (multiblock) | Rocket launches | Fuel, fabricated parts | Moons and planets |

## Idea backlog saved by the owner (1 October 2026)

Ideas Claude suggested after batch 10 (advanced power and tanks). The owner asked to save them and to pursue the **chemistry ideas next**. They are proposals, not designs: each still needs a feature record, balance numbers and the owner's choices before it is built.

### Chemistry (next)
| Idea | What it adds | Builds on | Notes |
| --- | --- | --- | --- |
| **Haber–Bosch ammonia** ✅ batch 12 (synthesis converter) | Hydrogen + nitrogen → ammonia | Electrolytic cell (hydrogen), air separation (nitrogen) | Leads to better fertilizer and nitric acid |
| **Air separation unit** ✅ batch 12 (nitrogen and oxygen), argon and the oxygen/argon boosts ✅ batch 13 | A tall cryogenic column: air → oxygen, nitrogen, argon | Gas holders, pipes | Oxygen speeds up the steel foundry; argon is a shielding gas for titanium work |
| **Chlor-alkali uses** ✅ batch 15 (PVC and soap; scrubbing left for later) | Real uses for sodium hydroxide (soap, aluminum digestion, scrubbing); chlorine + ethylene → PVC, a second plastic | Electrolytic cell, cracker | Gives chlorine and lye steady consumers |
| **Explosives line** ✅ batch 18 as weapons only (guncotton, grenades, grenade launcher; no block damage, owner's choice) | Nitric acid → nitroglycerin → dynamite and mining charges for the ore drill | Ammonia, nitric acid | Can grief: needs a server config switch and a hazard design first |
| **Polymer tiers** ✅ batch 14 (butadiene, synthetic rubber, gaskets; hoses with the turbocharger) | Synthetic rubber from butadiene (off the cracker) → hoses, gaskets, tires | Catalytic cracker | Rubber could gate high-pressure pipes and an engine turbocharger |
| **Glass chemistry** ✅ batch 16 (tincal, borax, borosilicate glass, optical fibre, ferroboron; glass tanks with the tank gauges) | Borosilicate glass from borax → lab glassware and glass tanks; optical fibre for a data network | New mineral (borax) | Fits the owner's earlier glass stasis-tank reference |
| **Pharmaceuticals** | A chemistry bench making status potions industrially (antidote, haste, night-vision tonic) | Chemistry outputs, magic bridge | Needs hazard and balance limits |
| **Waste and pollution loop** | Acid waste and slag must be neutralized (lime, flowback treatment) | Flowback treatment unit | Gives a chemical plant a clean-up cost rather than free disposal |
| **Flow batteries** ✅ batch 17 (vanadium electrolyte from asphalt binder; 64,000,000 JE) | Two big tanks of vanadium electrolyte as grid storage | Tanks that keep their fluid (batch 10), vanadium | Ties fluid storage directly into the power grid |

### Power, tanks and engines
| Idea | What it adds | Notes |
| --- | --- | --- |
| **Fluid gauge and tank walls** ✅ batch 20 (tank gauge, joined tanks, glass tank) | A panel showing a tank's level; tinplate tanks side by side join into one bigger tank | |
| **Turbocharger / intercooler** ✅ batch 19 (fitted in the advanced engine, water coolant) | An add-on for the advanced engine: more output for more fuel, needs coolant water | Tuning instead of just more engines |
| **Solar tracker and concentrator** ✅ batch 21 (solar tracker, heliostats and a solar receiver) | The array follows the sun; a heliostat mirror field boils water for steam | A solar-thermal route |
| **Flywheel** ✅ batch 19 (2,000,000 KE, friction) | Stores kinetic energy for engines, smoothing bursty kinetic lines | |
| **Pressure tiers for pipes** | Heavy gas pipes and compressor stations | The gas holder becomes a real buffer for hydrogen and natural gas grids |
| **Grid control room** | A cyan monitor bank wired to battery banks, showing generation and use per source over time | |
| **Portable tanks / fluid canisters** | Handheld containers to refuel engines, rocket packs and tools in the field | |
| **Locomotive generator** | A long multi-block in the advanced engine's style burning heavy fuel oil | A late use for the bottom of the barrel |

### Resource deposits (after batch 11)
- A resource-rich biome: needs the owner's choice between a biome library dependency (such as TerraBlender), replacing the Overworld biome layout, or a rare "rich" variant of the stony hills with denser patches.
- Underground deposits (thicker veins in caves, a deeper drill tier) and more deposit kinds (gold, zinc, lead, nickel, salt).

## Connections to magic and other specialties
Each connection below works both ways, but no machine requires magic to work.
- **Magic → technology:**
  - Workshop mages could inscribe machine upgrades, for example an efficiency rune costing essence instead of rare earths.
  - A dark-magic path could supply the same parts through rituals: Blood rituals producing catalysts, Necromancy supplying bounded labor.
- **Technology → magic:**
  - The alloy smelter makes bell bronze and brass for magical instruments.
  - The crusher and chemical reactor prepare reagents: salt for wards, phosphorescent inks.
- **Power ↔ essence:** any converter between power and magical essence must follow the design's rules. It's one-directional or lossy, with defined units and caps, so neither economy can print the other.
- **Agriculture:** fertilizer (phosphate + sulfuric acid), irrigation pumps and harvester machines. Farmers supply bio-lubricant and plant oils that later compete with the refinery.
- **Space:** the precision fabricator, refinery, lithium batteries and solar panels all feed rocketry and off-world bases.

## Suggested next steps, in order
1. ~~**Alloy Smelter**~~ Done: bronze, brass, invar and solder.
2. ✅ **Fluid API, with Water Pump and tanks:** the foundation for steam, chemistry and oil.
3. ~~**Metal Press, Wire Drawer and Circuit Assembler**~~ Done: the parts economy for everything above tier 2.
4. **Chemistry branch** (planned; see [branches/CHEMISTRY.md](branches/CHEMISTRY.md)): Electrolytic Cell and Chemical Reactor replace the remaining stand-ins and give salt, sulfur and phosphate real uses.
5. **Energy tiers and upgrades:** once demand justifies them.
6. **Oil and refinery, and farm machines:** each tied to its pillar's own proposals.

Each step should be one issue and one PR, with the checker extended to audit the new recipes.

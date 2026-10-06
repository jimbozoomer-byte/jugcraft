# High tech and space: the tiers from steel to the stars

Status: **a plan, not approved content**, except where a batch is marked implemented. It was written on 6 October 2026 at the owner's request: map the technology into tiers that let players process ever more complex resources, reach rockets and space, build stations, and then harness what is out there for machines beyond anything on the ground; decide how each tier looks and what it is made of; say which base resources to add; take notes on how Mekanism, Immersive Engineering, GregTech, Thermal Expansion, Applied Energistics 2, ComputerCraft and Ad Astra do it; and keep it all runnable on a multiplayer server. Every machine below follows the rules in [MACHINE_ROADMAP.md](../MACHINE_ROADMAP.md) (a consumer for something that exists, shared systems first, no free resources, early machines stay useful, keep the server cheap), [DESIGN.md](../DESIGN.md) and [ARCHITECTURE.md](../ARCHITECTURE.md). Numbers are starting proposals for the usual audit in [BALANCE.md](../BALANCE.md).

**The first step is built.** Batch 57, the space-age materials (chromium, cobalt, graphite, stainless steel, nichrome and the nickel superalloy), is implemented: see [features/space-age-materials.md](../features/space-age-materials.md). Everything after it is proposed.

## Contents

1. [Where the mod is today](#where-the-mod-is-today)
2. [The tier ladder](#the-tier-ladder)
3. [How each tier looks](#how-each-tier-looks)
4. [Materials, alloys and processed goods](#materials-alloys-and-processed-goods)
5. [Machines by tier](#machines-by-tier)
6. [The space program](#the-space-program)
7. [The extremely advanced tiers](#the-extremely-advanced-tiers)
8. [What the big tech mods do, and what Jugcraft takes from each](#what-the-big-tech-mods-do-and-what-jugcraft-takes-from-each)
9. [Keeping it runnable on a server](#keeping-it-runnable-on-a-server)
10. [Batches, in order](#batches-in-order)
11. [Open questions for the owner](#open-questions-for-the-owner)

## Where the mod is today

Jugcraft already has four and a half tiers of technology. They were built batch by batch and documented in [TECH_TREE.md](../TECH_TREE.md); this is the same content sorted into the ladder it forms.

| Tier | Name | What the player can do | Look | Power ceiling | Moving things |
| --- | --- | --- | --- | --- | --- |
| 0 | Homestead | Mine tin, zinc, lead and copper; smelt bronze by hand; hand crank and water wheel turn shafts; conveyors on rotation | Vanilla, wood and stone | 16–24 KE/t | Hoppers, minecarts, belts |
| 1 | Workshop | Coal and steam power, electric furnace, crusher (×2 ore), alloy smelter, metal press, wire drawer, basic circuits, pulverizer and ore washer (×3), sieve, sawmill, prospector, deposit drill, auto-crafter, tree farm | Steampunk: brass, copper, riveted iron, gauges | 64 JE/t a generator; copper cable 256 JE/t; battery box 400k | Brass item pipes, extractors, sorters, conveyors |
| 2 | Steel | Coke oven and steel foundry (no power), steel tools and arms, machine upgrades, capacitor bank, ore drill, powered tools and the charging station, geothermal and wind power, heavy pump and steel pipes, valves and filters, item crates | Early dieselpunk: gunmetal, olive drab, hazard stripes | 72 JE/t wind; silver cable 1,024 JE/t; capacitor bank 4M | High-pressure extractor, steel fluid pipes |
| 3 | Oil and chemistry | Pumpjack, fracking, distillation, cracking, diesel and gas-turbine power, plastics, rubber, asphalt; electrolysis, sulfuric and nitric acid, ammonia, chlorine, titanium (Kroll), real aluminum (Bayer), lithium cells and bank, rare-earth magnets, borosilicate glass, flow battery, hydroponics, electroplating, gas cylinders, construction foam and concrete, field chemistry | Dieselpunk at full size: towers, rigs, engines | 512 JE/t turbine, 1,024 KE/t engine; aluminum cable 4,096 JE/t; lithium bank 32M, flow battery 64M | Gas holders, cylinders, drones (tiers 1–4) |
| 4 | Electronics | Silicon boules, wafers, lithography, microchips, processors, network terminal, control networks and the control room, advanced solar and solar thermal, magnet dynamos and motors, rocket workshop and terrestrial rocketry (survey, weather, flares, rocket post, ziplines, launcher, booster rails, kerosene and liquid oxygen), drone tower tiers 5–9, blueprints, exosuit | Retro-cyan: near-black casings, cyan glass, violet conduits, beige computers | 1,024 KE/t advanced engine, 64 JE/t a solar array; no new cable tier | Rocket post over 4,096 blocks, drone tower logistics |
| 4½ | Dieselpunk vehicles and arms | Zeppelin, diesel walker, landship, artillery and tower guns, 50 kinds of arms | Kaiserpunk and dieselpunk | – | Vehicles burn diesel and kerosene |

What is missing for the climb to continue:

- **Uranium has no use.** It is mined, ground and melted, and then sits in a chest. Nothing is radioactive and nothing makes power from it.
- **One cable carries everything.** Aluminum cable (4,096 JE/t) is the top; there are no transformers or voltage tiers, so a reactor making 8,000 JE/t has nowhere to go.
- **Nothing leaves the ground.** The rocket workshop makes rockets that survey, seed clouds and deliver cargo, but no launch pad, orbit or other world exists. The owner held space "til we have more of the terrestrial stuff"; the terrestrial stuff now exists.
- **Electronics has few consumers.** Processors go into control, drones, solar and the engine, but no tier needs them in quantity.
- **Storage is still chests.** Crates, sorters and pipes work, but a late-game factory has no way to see and pull from everything at once.
- **The drone tower runs ahead of the materials.** Its tiers 5–9 already name tungsten-steel frames, ceramic armour, depleted-uranium armour and a graphene lattice (made from coke on copper). The plan below gives those words real material lines and keeps the existing recipes working.

## The tier ladder

The ladder keeps the mod's rule that **tiers describe complexity, not a checklist**: a farmer or a mage never needs a reactor, and every tier opens several destinations rather than one final machine ([DESIGN.md](../DESIGN.md)). Each new tier is reached by processing something more complex than the last: ore → alloy → chemical → crystal → isotope → off-world material → exotic matter.

| Tier | Name | Era and look | The complex thing it processes | Signature materials | Power and cable | Logistics | Status |
| --- | --- | --- | --- | --- | --- | --- | --- |
| 0 | Homestead | Vanilla | Ore to ingot | iron, copper, tin, bronze | Hand crank, water wheel | Hoppers | Implemented |
| 1 | Workshop | Steampunk | Ingot to part | brass, invar, solder, silver, basic circuit | Coal and steam, 32–64 JE/t; copper cable | Pipes, extractors, conveyors | Implemented |
| 2 | Steel | Early dieselpunk | Iron to steel by carbon | steel, tungsten, nickel, advanced circuit | Geothermal, wind; silver cable | Steel pipes, drills | Implemented |
| 3 | Oil and chemistry | Dieselpunk | Crude to fractions; salt, sulfur and air to acids and gases | plastics, rubber, titanium, aluminum, lithium, magnets, borosilicate | Diesel, turbine, fuel cell; aluminum cable 4,096 | Gas holders, cylinders | Implemented |
| 4 | Electronics | Retro-cyan | Silicon to crystal to chip | wafers, microchips, processors, optical fibre | Advanced solar, solar thermal, advanced engine | Control networks, rocket post, drones | Implemented |
| **5** | **Atomic and aerospace** | **Atompunk** (space race): cream enamel, orange, chrome, trefoils | Uranium to isotopes; alloys to superalloys; fibre to composite | chromium, cobalt, graphite, fluorite, zirconium, enriched uranium, stainless steel, superalloy, zircaloy, carbon fibre, magnesium | Fission reactor and steam turbines, 2,048–8,192 JE/t; **transformers and HV lines 16,384** | Launch pad, cargo rockets, overhead power lines | **Materials implemented (batch 57); rest planned** |
| **6** | **Orbital** | Clean room: white panels, blue-cyan light, gold foil | Vacuum, cryogenics and superconductivity | niobium, helium, PTFE, aerogel, titanium aluminide, platinum-group catalysts | Space solar (no night), **superconducting cable 65,536**, hydrogen fuel cells | Station modules, docking, the cargo lattice (digital storage) | Planned |
| **7** | **Interplanetary** | Industrial space: dark grey, amber hazard, dust | Regolith and ice to metals, oxygen and fuel | lunar ilmenite and anorthite, helium-3, martian iron and perchlorate, asteroid nickel-iron and platinum, methane | Nuclear thermal rockets, beamed power (orbital transmitter and ground rectenna) | Interplanetary freighters, mass driver, rovers | Planned |
| **8** | **Deep space and exotic** | Black with violet and white energy; geometric, floating parts | Fusion, antimatter and exotic matter | deuterium, tritium, helium-3, metallic hydrogen, carbon nanotube, exotic matter | Fusion reactor 65,536 JE/t, antimatter cells, Dyson swarm receiver | Gates (the shared milestone with magic) | Planned, as vision |

**What stays useful.** Every tier keeps a job. Coal generators run small outposts and remote drills; steam and diesel power are what a new planet base starts with before a reactor is shipped; ore washing and acid leaching feed the reactor's uranium; the rocket workshop assembles every rocket part; drones build the launch pad. Later tiers are faster, denser and reach further; they do not delete earlier routes (rule 4 of the machine roadmap).

**What each tier unlocks for other specialties** (the collaboration milestones [DESIGN.md](../DESIGN.md) asks for): tier 5 gives farmers the atomic age's fertilizer plant (ammonia at scale) and preserved food by irradiation; tier 6 gives builders white-panel and glass modules and a cargo network for shared bases; tier 7 gives explorers new worlds and creature keepers off-world ecology; tier 8 opens the gate that mages attune, the first place where a machine and a ritual are both required.

## How each tier looks

[ART_DIRECTION.md](../ART_DIRECTION.md) sets the rule: the look grows with the tier, from brass and steam to dieselpunk to graphite and light, in detailed box models at real size and clean, flat-filled textures. The new tiers continue the line.

| Tier | Style | Materials and colours | Details | Light | Texture prefix (proposed) |
| --- | --- | --- | --- | --- | --- |
| 5 Atomic and aerospace | **Atompunk**: the 1950s–60s space race | Cream and white enamel on rounded stainless cabinets; chrome trim; international orange on anything that moves or burns; dark green reactor concrete; grey graphite blocks | Radiation trefoils and stencilled numbers, round glass dials with red needles, toggle switches, cooling fins, rivet-free seams, launch-gantry lattice in white and orange | Warm amber indicator lamps; reactor glow is a deep cyan (Cherenkov) through glass | `ap_` |
| 6 Orbital | **Clean room**: white and blue, every surface soft-cornered | White composite panels with hairline seams, blue-cyan light bars, gold foil insulation on anything exposed, dark glass | Docking rings, handrails, latches, EVA tethers, flat touch panels, small round windows | Cool white and blue; emissive strips the electric look already uses (`el_glow_cyan` is reserved for this) | `or_` |
| 7 Interplanetary | **Working space**: the dieselpunk spirit in vacuum | Dark grey and bare titanium, amber hazard stripes, regolith dust on the lower edges, exposed pipes and tanks | Big wheels and tracks, drill heads, radiators, dust-caked solar wings, beam emitters | Amber and red working lamps | `ip_` |
| 8 Deep space and exotic | **Energy made solid** | Black and near-black with violet, white and cyan energy; polished dark glass; parts that hover a few pixels off the body (box models with gaps) | Toroids, rings, magnetic yokes, containment fields drawn as emissive planes | Violet (`el_glow_violet` exists) and white; pulses while running | `ex_` |

Rules that carry over unchanged: box-built models with no coplanar faces; real sizes (a reactor is a building, a launch pad is a square of blocks, a station module is a room); flat palettes of four or five shades; placed wear, not noise; cables and pipes meet every machine near the middle of each side. Every texture stays original; the references guide colour and silhouette only.

## Materials, alloys and processed goods

[DESIGN.md](../DESIGN.md) asks each new material for a reason to exist where it is found, an acquisition tier, a technology use and a magic use (or a narrower-role rationale), and asks that existing materials be reused first. The plan reuses first: uranium, tungsten, borax, lithium, titanium, silver and the refinery gases all get new jobs before any new ore. Then it adds the fewest ores that unlock a whole tier each.

### Base resources

| Resource | Where and when | Tier | Processing route | Technology uses | Magic use or narrower role | Batch |
| --- | --- | --- | --- | --- | --- | --- |
| **Chromium** (chromite ore) | Overworld, Y −64 to 24, iron pickaxe | 5 | Blast or arc furnace; crusher, pulverizer (nickel byproduct), washer, acid leaching | Stainless steel, nichrome, chrome plating; later the reactor vessel and the centrifuge | Narrow role: a structural metal | **57, done** |
| **Cobalt** (cobaltite ore) | Overworld, Y −64 to 0, iron pickaxe; also 10% of ground nickel ore | 5 | Blast or arc furnace, as chromium | Superalloy; later lithium-cobalt cathodes and the nanotube catalyst | Blue pigment for a glass and candle colour (a cozy use) | **57, done** |
| **Graphite** (graphite ore) | Overworld, Y −40 to 40, any pickaxe; or coke baked in the arc furnace | 5 | Mined or synthetic | Anodes (aluminum, lithium cells), reactor moderator, electrodes, graphene, carbon fibre | Pencil lead: inks and drawing for the cosy branch | **57, done** |
| **Fluorite** (fluorspar ore) | Overworld, Y −32 to 48, hydrothermal veins beside lead and silver | 5 | Fluorite + sulfuric acid → hydrofluoric acid (chemical reactor) | Uranium hexafluoride for enrichment, PTFE (non-stick gaskets, cryogenic seals), cryolite that speeds aluminum smelting, glass etching | A pale violet glowing crystal lamp | 59 |
| **Zirconium** (zircon sand) | Beach and river sand, surface | 5 | Zircon + chlorine → zirconium sponge (the same Kroll step as titanium), arc furnace | Zircaloy fuel cladding, ceramics for the heat shield, zirconia crucibles | Narrow role | 59 |
| **Enriched and depleted uranium** | From uranium, no new ore | 5 | Uranium dust + HF → UF4 → UF6 gas → centrifuge cascade | Fuel rods; depleted uranium gives the drone tower's existing armour a real source | Narrow role | 60 |
| **Thorium** | A byproduct of monazite leaching, no new ore | 5 | Chemical reactor | Breeder fuel (a second reactor fuel cycle) | Narrow role | 61 |
| **Magnesium** | From brine, no new ore | 5 | Brine + lye → magnesium hydroxide → electrolysis (chlorine returns) | Light airframe alloys, flares, the Sabatier catalyst's support | Bright white flare for signals and festivals | 62 |
| **Carbon fibre** | From the refinery, no new ore | 5 | Ammonia + propylene → acrylonitrile → fibre, carbonized in the arc furnace; set in resin into composite | Rocket bodies, pressure vessels, the tower's carbon composite panel (a second recipe) | Narrow role | 62 |
| **Niobium** (columbite) | Overworld, rare, Y −64 to −16, with tin and tantalum | 6 | Arc furnace | Niobium-titanium superconducting wire, capacitors, rocket nozzle liners | Narrow role | 70 |
| **Helium** | From refinery gas (natural gas holds helium), later from lunar regolith (helium-3) | 6 | Cryogenic column (the air separation unit's cold box, a second recipe) | Superconductor coolant, balloons, breathing mixes, fusion (helium-3) | Lifting gas for festival balloons | 70 |
| **Platinum group** | Byproducts of nickel and copper refining; abundant in asteroids | 6–7 | Pulverizer byproduct, then asteroid ore | Catalysts (cracker, fuel cell, Sabatier), electrodes, thermocouples | Narrow role; silver's richer cousin for instruments | 69 |
| **Lunar regolith** | The Moon | 7 | Regolith processor: ilmenite (iron, titanium, **oxygen** by hydrogen reduction), anorthite (aluminum, silicon), KREEP (rare earths), helium-3 (trace) | Oxygen for habitats, building material (sintered regolith block), fusion fuel | Moon dust for lunar wards and ice rituals | 64 |
| **Martian iron, perchlorate and ice** | The Mars-like planet | 7 | Hematite → iron; perchlorate → oxygen and chlorine; ice → water; carbon dioxide + hydrogen → methane and water (Sabatier) | Local fuel (methalox) and air; iron without shipping | Red ochre pigment | 68 |
| **Asteroid nickel-iron, platinum and water** | The belt | 7 | Mined whole; orbital refinery | Platinum catalysts, bulk metal in orbit, water for fuel | Sky iron: meteoric metal for ritual blades | 69 |
| **Deuterium and tritium** | Water electrolysis (a heavy fraction), lithium in the reactor blanket | 8 | Isotope separation column; breeding | Fusion fuel | Narrow role | 72 |
| **Metallic hydrogen** | A gas giant's atmosphere | 8 | Atmospheric scoop station, pressed in the exotic press | The exotic-matter precursor; the densest fuel | Narrow role | 71 |
| **Carbon nanotube** | Methane over a cobalt catalyst (vapour deposition) | 8 | Nanotube furnace | Space elevator cable, armour, the strongest cable | Narrow role | 73 |
| **Exotic matter** | Antimatter and metallic hydrogen in the collider | 8 | Particle accelerator ring | Gates, the final power storage | The one material mages attune: the gate needs both | 73 |

Not added, on purpose: invented ores with no real basis (desh, ostrum, naquadah and their kind). Where a fantasy name is wanted the Realms branch has it; the technology branch stays on the periodic table.

### The alloy ladder

| Tier | Alloy or processed material | Made from | Where | Use |
| --- | --- | --- | --- | --- |
| 1 | Bronze, brass, invar, solder | copper, tin, zinc, nickel, lead | Alloy smelter | Casings, pipes, circuits (done) |
| 2 | Steel | iron + coke | Steel foundry | Everything heavy (done) |
| 3 | Aluminum, titanium, plastics, rubber, borosilicate, neodymium magnets | bauxite, rutile, oil, borax, rare earths | Chemistry | Light frames, seals, glass, motors (done) |
| 4 | Silicon boules, microchips, processors, optical fibre | quartz, phosphate, acid | Arc furnace, lithography | Control and electronics (done) |
| **5** | **Stainless steel** (3 steel + 1 chromium → 4), **nichrome** (4 nickel + 1 chromium → 5), **superalloy** (2 nichrome + 1 cobalt → 3) | chromium, nickel, cobalt | Alloy smelter | Vessels, heating elements, turbine blades and nozzles (**done, batch 57**) |
| 5 | Zircaloy (zirconium + tin), magnesium alloy (magnesium + aluminum), carbon composite, PTFE, reactor concrete (concrete + borax + lead), heat-shield tile (zirconia + silica) | zirconium, magnesium, carbon fibre, fluorite | Alloy smelter, vacuum furnace, polymerization reactor | Fuel cladding, airframes, rocket bodies, seals, shielding, re-entry |
| 6 | Niobium-titanium wire, titanium aluminide, aerogel, platinum catalysts, graphene (from graphite, a second recipe beside the tower's) | niobium, titanium, aluminum, silica, platinum | Vacuum furnace, wire drawer, chemical reactor | Superconducting cable, hot turbine parts, insulation, catalysts |
| 7 | Sintered regolith, lunar titanium and aluminum, methane, martian steel | regolith, ice, carbon dioxide | Regolith processor, Sabatier reactor | Building in place, local fuel and air |
| 8 | Carbon nanotube, metallic hydrogen, antimatter, exotic matter | methane, cobalt, gas giant atmosphere, power | Nanotube furnace, scoop, collider | Elevator cable, fusion and gates |

Every alloy conserves metal exactly, the audit's rule; the real alloys' trace additions (molybdenum, vanadium, tantalum) are left out rather than invented as ores.

## Machines by tier

Each entry names what the machine consumes (which must already exist), what consumes its output, its footprint, how it fails, and what it costs the server. Sizes follow the art direction: things that are big in life are big in the world.

### Tier 5: atomic and aerospace

| Machine | Does | Consumes | Feeds | Footprint and look | Failure behaviour | Server cost |
| --- | --- | --- | --- | --- | --- | --- |
| **Transformer** and **HV line** | Steps 4,096 JE/t aluminum networks up to a 16,384 JE/t high-voltage grid and back down; HV lines are connector-to-connector overhead wires (a graph, not blocks) | Steel-cored aluminum wire, ceramic insulators (the tower has one), superalloy | Every tier-5 generator and consumer | A two-tall cabinet with fins; lattice pylons up to 32 blocks apart | A line fed above its rating simply caps at it (no explosions, GregTech's lesson); a cut wire drops the line item | Wires are entities drawn client-side like belts and ziplines; the network is cached like cables |
| **Fluorine line** | Fluorite + sulfuric acid → hydrofluoric acid; HF electrolysis → fluorine gas; uranium dust + HF → UF4; UF4 + fluorine → uranium hexafluoride gas | Existing chemical reactor and electrolytic cell with new recipes; fluorite | Enrichment, PTFE, cryolite | No new machines | Data only | None beyond today's fluid recipes |
| **Centrifuge cascade** | UF6 → enriched uranium (1 in 8) and depleted uranium (7 in 8), slowly | UF6, 512 JE/t | Fuel fabrication; depleted uranium armour | 3×3×4 bank of stainless drums in a cream cabinet | Stops when its output is full; never leaks | One block entity, recipe-driven |
| **Fuel fabricator** (the precision fabricator) | Enriched uranium pellets in zircaloy tubes → fuel rods; also heat-shield tiles, turbopumps, gyroscopes, avionics from processors | Zircaloy, enriched uranium, superalloy, processors, carbon composite | Reactor, rockets | 3×2×2 glovebox line, white and orange | Standard processor | Standard processor |
| **Fission reactor** | Fuel rods make heat; coolant water becomes **steam** (a new gas) at up to 4,096 mB/t; graphite moderator and borax control rods set the rate; a redstone or control-network **scram** drops the rods | Fuel rods, water, graphite, borax (control rods), 0 JE (it makes heat, not power) | Steam turbines, the heat recovery unit (a second recipe), district heating for the hydroponic bay | 5×5×5 pressure vessel of stainless plate inside reactor concrete, a cream control face with a Cherenkov-blue window | **No block damage** (the owner's rule for explosives). Overheating (no coolant, rods fully out) first halves output, then **melts down**: the vessel becomes a dead "slag" block, the fuel is lost, and a bounded radiation field (a status effect, a 16-block radius, fading over an hour, stored as saved data) hurts the unprotected. Lead-lined gear and the exosuit shield from it. Spent rods are stored in a cask or reprocessed | One block entity; heat is a number, not a simulation; the radiation field is a list of centres checked against players every 20 ticks |
| **Steam turbine** | Steam → JE: 128 JE per 100 mB; 2,048 JE/t a turbine, up to four on one reactor | Steam | The HV grid | 3×3×5 turbine hall, white with orange rotor housings; a spinning rotor the client draws | Starves gracefully when steam is short | A fluid generator like the gas turbine |
| **Cooling tower** | Condenses spent steam back to 75% water (a loop with a loss, like fracking flowback) | Spent steam | The reactor's water | 5×5×7 hyperboloid of concrete | Nothing | A fluid processor |
| **Spent fuel cask and reprocessing** | Spent rods wait out their heat in a cask, then the chemical reactor recovers 3 of 8 uranium and the thorium cycle's fuel | Spent rods, nitric acid | Fuel again, with a loss | 2×2 lead-lined cask | A cask is a tank for one item | None |
| **Vacuum induction furnace** | Alloys that need no air: zircaloy, titanium aluminide, superalloy castings, graphene from graphite | Argon (the air separation unit) or a vacuum pump; 256 JE/t | Everything tier 5 and 6 | 3×3×3 chamber, cream and chrome | Standard processor | Standard processor |
| **Carbon fibre line** | Acrylonitrile → fibre (polymerization reactor), carbonized in the arc furnace; fibre + resin → carbon composite in the metal press | Ammonia, propylene (from naphtha), coke | Rocket bodies, pressure vessels | No new machines | Data only | None |
| **Launch pad** | A 7×7 pad with a gantry; assembles a rocket from stages placed in its cradle, fuels it from pipes, counts down and launches | Rocket stages (rocket workshop and fabricator), kerosene and liquid oxygen or methalox, power | The space program | 7×7 blast apron, a 9-tall white-and-orange gantry (the tower-gun code places large structures already) | Launch refused with a reason (no fuel, no sky, no destination, pad in use), as the rocket post does; a launch in rain or thunder is refused | One block entity; the rocket is one entity |
| **RTG** | Plutonium from reprocessing → 16 JE/t for years, anywhere, no sun | A plutonium pellet | Rovers, remote sensors, a stranded base's radio | One block, finned, black and orange | Never stops; cannot be recharged | A generator with no inputs, like the pumpjack |

### Tier 6: orbital

| Machine | Does | Consumes | Feeds | Footprint and look | Failure behaviour | Server cost |
| --- | --- | --- | --- | --- | --- | --- |
| **Station core and modules** | A core placed in orbit grows a station: hub, solar wing, habitat, lab, dock, greenhouse, tank farm. Modules are placed items that fill their footprint, as every multi-block does | Carbon composite, titanium, gold foil, borosilicate glass, processors | Everything in orbit | 5×5×5 to 7×7×9 modules in the clean-room look | Modules need a core within 32 blocks; a module without air is a pressure alarm, not a death | Modules are plain blocks; only the core ticks |
| **Oxygen system** | An oxygen distributor fills a sealed room (bounded flood fill, at most 4,096 blocks, cached per room and re-checked every 5 seconds or when a wall changes); airlocks; the existing scuba tank becomes the EVA suit's air | Oxygen (air separation unit, electrolysis, regolith), power | Every habitat | A one-block distributor with a cylinder bank; airlock doors two tall | A breached room drains over 30 seconds with an alarm (the control room's klaxon) before anyone suffocates | The flood fill runs once per change, never per tick; per-player oxygen ticks like the scuba tank |
| **Carbon dioxide scrubber** | Lithium hydroxide (lithium carbonate + lye) absorbs CO2 so the oxygen lasts; regenerable with heat | Lithium, lye, power | Habitats | One block, white | Full scrubber: oxygen use doubles | Trivial |
| **Cryogenic plant** | Liquid nitrogen and liquid helium from the air separation unit's gases and refinery helium; keeps superconductors cold | Nitrogen, helium, power | Superconducting cable, the fusion reactor, cryo-fuel storage | 3×3×4 cold box, white with frost | Warm cable falls back to aluminum's rate | A fluid processor |
| **Superconducting cable** | 65,536 JE/t with no loss while cooled (1 mB of liquid nitrogen a minute per 16 blocks) | Niobium-titanium wire, PTFE, liquid nitrogen | Space solar, fusion, the beamed-power receiver | A thick white sleeve with cyan light | Warms to aluminum's rate, never breaks | Networks cached as now; coolant counted per network, not per block |
| **Space solar array** | 256 JE/t with no night and no weather, orbit only | Advanced solar panels, gold foil, superconducting cable | The station and beamed power | 3×9 wings | None | A generator; the "sky" check is the dimension itself |
| **Orbital shipyard** | Assembles interplanetary vessels too large to launch from the ground | Stages, carbon composite, superalloy, fuel | Tier 7 travel | 9×9×9 open frame with a crane | Standard | One block entity |
| **Cargo lattice** (digital storage) | A lattice controller, storage cells (silicon memory: 1k, 4k, 16k, 64k items), an access terminal that shows and pulls everything, import and export buses, and crafting from patterns through the existing auto-crafter | Processors, silicon, optical fibre, steel | A late factory | Controller 2×2×2, cells in drives, thin lattice conduits (the data cable's look) | A full cell refuses; an unpowered lattice freezes; nothing is lost | Hard caps: 1,024 nodes a network, 64 cells a drive bank; the network is a cached graph, contents are counted on change, never scanned per tick; terminals send pages, not the whole inventory |
| **Programmable controller** (control electronics II) | Timers, counters, arithmetic and state machines added to the logic controller's rules; a drone task list; monitors show programs | Processors | Every automated base | The existing logic controller with a bigger screen | A rule set that loops too long is cut off at its budget | A fixed instruction budget per controller per tick (256 operations), no scripting language |

### Tier 7: interplanetary

| Machine | Does | Consumes | Feeds | Footprint and look | Failure behaviour | Server cost |
| --- | --- | --- | --- | --- | --- | --- |
| **Regolith processor** | Lunar regolith → ilmenite, anorthite, KREEP, trace helium-3; ilmenite + hydrogen → iron, titanium and **water** (so oxygen by electrolysis) | Regolith (mined like gravel), hydrogen, 384 JE/t | Lunar bases, fusion | 3×3×3 dusty dark-grey hopper and kiln | Standard processor | Standard processor |
| **Sabatier reactor** | Carbon dioxide (martian air, a pump) + hydrogen → methane + water | CO2, hydrogen, platinum catalyst | Methalox rockets, water | 2×2×3 | Standard | A fluid processor |
| **Mass driver** | Launches cargo pods from the Moon to orbit for power alone, no fuel | Superconducting cable, 8 MJ a pod | Orbital industry | A 32-block rail of coils | Refuses without a receiving pad in orbit | A rocket-post delivery with a different cost |
| **Asteroid claim and orbital refinery** | A claim stake marks an asteroid; the refinery cuts it into nickel-iron, platinum group and water | A ship, power | Platinum catalysts, bulk metal | Refinery 7×7×7 in the industrial look | A claim lapses after 7 days unworked, so the belt is not fenced off | Asteroids are structures in a bounded dimension; mining is normal block breaking |
| **Nuclear thermal rocket** | A fission core heats hydrogen: twice the range of chemical stages | Fuel rods, hydrogen | Tier 7 destinations | A stage item | Standard rocket rules | None extra |
| **Beamed power** | An orbital transmitter sends up to 16,384 JE/t to one ground rectenna in line of sight (the same dimension pair, a saved link) | Space solar or fusion | The ground base | Transmitter 5×5 dish; rectenna 9×9 mesh | A rectenna without its transmitter loaded gets nothing; nothing is force-loaded | One link each, a saved pair, no scanning |
| **Rover** and **freighter** | A pressurized rover (the landship's code in a new shell) and an interplanetary freighter (the zeppelin's code, in vacuum) | Methane or hydrogen | Exploration and hauling | Vehicles at real size | Out of fuel: it stops; it can be towed | Vehicle entities, as today's |

### Tier 8: deep space and exotic

| Machine | Does | Consumes | Feeds | Footprint and look | Failure behaviour | Server cost |
| --- | --- | --- | --- | --- | --- | --- |
| **Isotope column** | Deuterium from water (a heavy fraction, 1 in 6,400); tritium bred from lithium in the reactor's blanket | Water, lithium, power | Fusion | 2×2×7 column | Standard | A fluid processor |
| **Fusion reactor** | Deuterium + helium-3 (or deuterium + tritium with neutron damage to the vessel, a wear mechanic) → 65,536 JE/t | Fuel gases, superconducting magnets, liquid helium, a 1 GJ start-up charge | Everything | A 9×9×5 toroid, black with violet rings | Loses confinement without coolant: stops, 10-minute restart, no explosion | One block entity; the toroid is plain blocks |
| **Atmospheric scoop** | A station in a gas giant's upper atmosphere draws hydrogen and helium-3 | Power, carbon nanotube tether | Fusion, metallic hydrogen | A hanging station; the zeppelin's physics | A storm (the dimension's weather) halts it | A generator with no inputs |
| **Exotic press and collider** | Metallic hydrogen from hydrogen at extreme pressure; antimatter from 100 MJ a milligram in a 33-block accelerator ring; exotic matter from both | Power beyond anything before | Antimatter cells (portable power, 50 MJ each), gates | A ring in the exotic look | Containment fails without power: an antimatter cell empties harmlessly with a flash and a loud warning; it never breaks blocks | One block entity, a ring of plain blocks |
| **Gate** | A stable wormhole between two gate frames, in any two dimensions, including the magical realms | Exotic matter, a mage's attunement (the shared milestone), 4,096 JE/t while open | Everyone | A 7×7 ring, black and violet, white light inside | Closes when power stops; nobody is stranded mid-step; unloaded ends refuse to open | A portal pair, like the Nether's; no chunk loading |
| **Dyson swarm receiver** | Mirrors around the sun (a count, not entities) beam power to one receiver: the power ceiling, 262,144 JE/t | A swarm built mirror by mirror from aluminum foil (each mirror a launched pod) | The server's shared wonder | A 9×9 receiver | Nothing | A number |

## The space program

[CONTENT_BRANCHES.md](../CONTENT_BRANCHES.md#space-and-magical-realms) sets the order: a buildable launch system, one meaningful moon, then planets and varied expeditions, then broader exploration; one destination per reviewed increment; no collection of empty worlds; stable dimension IDs; safe return and stranded-player recovery. This is the shape.

### The player loop

1. **Build the pad.** A launch pad (7×7) with a gantry; drones can build it from a blueprint.
2. **Assemble the rocket.** Stages come from the rocket workshop and the fuel fabricator: a motor cluster (liquid rocket motors, which exist), a tank stage (carbon composite or stainless), a capsule or cargo bay, a heat shield, avionics (a guidance unit and a processor). Place them in the cradle; the pad shows the stack and what is missing.
3. **Fuel it** by pipe: kerosene and liquid oxygen (both exist) for tier 1 and 2 rockets, methalox from tier 7, nuclear thermal for the outer planets.
4. **Pick the destination** on the pad's screen: orbit, the Moon, and later worlds. Range depends on the stages. A survey satellite (an existing survey rocket in orbit) must have reported a destination before a crewed launch: the ore map of a new world is the first thing you get, and it is what makes the trip worth it.
5. **Launch.** A countdown, a rocket entity rising, then the player is moved to the destination dimension in a landing capsule that descends on a parachute (no fall deaths, Ad Astra's lander lesson) to a landing site the launch marked. Cargo pods land as rocket-post deliveries do: if the area is not loaded they wait.
6. **Survive.** The EVA suit is the exosuit helmet's successor: it holds the scuba tank's oxygen (8,000 mB, 400 seconds) and the suit's own 24,000 mB; the pneumatic grapple works in low gravity. Gravity, air and temperature are per-dimension data the server applies as attribute modifiers, never per-tick velocity edits.
7. **Build a base**: sealed rooms with an oxygen distributor, hydroponic bays (which already need no sun or soil), a scrubber, power from shipped solar or a reactor, and later local oxygen from regolith.
8. **Come home.** Every capsule is a return vehicle with one return's fuel. A base's pad launches new rockets built there. The **emergency beacon** (craftable from a processor and a signal flare, given free with every capsule) calls a rescue capsule after five minutes, which brings the player, not their cargo, back to the pad they launched from. Dying off-world respawns at the usual bed or spawn; nothing is lost in orbit except the trip.

### Destinations, one per increment

| Destination | Why go | Terrain and ecology | Hazards | Resources with downstream uses | Base rules |
| --- | --- | --- | --- | --- | --- |
| **Orbit** (tier 5–6) | The station: space solar, the shipyard, a waypoint to everything | A void dimension with the home world's horizon in the sky; no terrain | No air; nothing else | None, but the only place space solar and the shipyard work | Modules only; a station is saved as blocks like any build; unloaded stations do nothing |
| **The Moon** (tier 6) | Regolith industry, helium-3, the mass driver | Grey regolith plains, craters, highlands, rilles; ice in polar craters; a 20-minute day; black sky with stars; the home world hanging above | No air, low gravity (jump high, fall slow), micrometeorite showers as a rare weather event (damage to the unsheltered, never to blocks), solar storms that double radiation outside | Regolith, ilmenite, anorthite, KREEP, helium-3, polar ice | Full bases; rovers; the mass driver |
| **The red planet** (tier 7) | Methalox from local air, iron without shipping, a world worth settling | Red deserts and canyons, basalt caves, polar ice, dust storms as weather; **a thin biosphere** in the caves (lichen-like glowing growths and a cave creature for the creature branch) | Thin air (a suit, but half the oxygen use), 38% gravity, dust storms that stop solar | Hematite, perchlorate, sulfur, ice, carbon dioxide | Bases, rovers, greenhouses under glass |
| **The belt** (tier 7) | Platinum and bulk metal; water for fuel | Floating asteroids in a bounded dimension, from pebbles to 60-block bodies; carbonaceous, stony, metallic | No air, no gravity (free fall, the grapple is how you move), spin | Nickel-iron, platinum group, water ice, carbon | Claims; the orbital refinery |
| **The ice moon** (tier 7) | Water in quantity; the ocean beneath | An ice crust over a dark ocean with geysers; bioluminescent life for the creature branch | No air, cold that drains suit power | Water, salts, exotic deep-ocean reagents for magic | Drilled bases through the crust |
| **The gas giant** (tier 8) | Helium-3 and hydrogen in bulk; metallic hydrogen | No surface: a cloud dimension with layered winds and lightning; the scoop station hangs from a balloon | Crushing depth below a floor level, storms | Hydrogen, helium-3 | A hanging station only |
| **Deep space** (tier 8) | The gate's far ends: the magical realms and, later, a second star | Through the gate | The realm's own | The realm's own | The Realms branch decides |

**Dimensions are data.** Each destination is a datapack dimension with a Jugcraft dimension type carrying gravity, air, temperature, day length and radiation; Java reads those once. Worldgen is noise-based like the Overworld's (the biomes branch's region system reused), with structures for craters, ice caps and asteroid bodies. New destinations are added without touching the old ones, and IDs never change.

## The extremely advanced tiers

The owner asked for tiers beyond the station that harness what space offers to build machines beyond anything on the ground. The resources are real: helium-3 and hydrogen from the Moon and the gas giant, platinum from the belt, water from the ice moon, and energy from a sun with no night. What they make possible, in order:

1. **Fusion** (deuterium from water, helium-3 from the Moon) ends fuel scarcity on the ground: 65,536 JE/t from a machine the size of a house, with the superconducting grid to carry it.
2. **Beamed power** moves that energy between worlds without cables, one saved link at a time.
3. **Antimatter** is energy made portable: a cell powers a rover for a month or a gate for an hour. It costs more to make than it gives back (the audit's rule holds at every tier), so it is a battery, not a source.
4. **Exotic matter and the gate** are the branch's shared wonder with magic: a machine makes the matter, a mage attunes the frame, and both are needed to open a way to the realms and back ([DESIGN.md](../DESIGN.md): selected milestones for strong collaboration, never every recipe).
5. **The Dyson swarm** is the server's communal end project: a counter of mirrors that any player's launches add to, and a power ceiling that nothing passes.

Nothing in these tiers removes a cosy home's purpose: a fusion base still needs food, the gate still needs a mage, and the drones that build the swarm's mirrors still fly from a tower on a hill.

## What the big tech mods do, and what Jugcraft takes from each

Notes from how these mods are designed, written from their public behaviour as players see it. Nothing of their code or art is used; ideas are reshaped to fit Jugcraft's own systems (CLAUDE.md: original content first, no bundled mods).

| Mod | What it does well | What Jugcraft takes | What Jugcraft avoids |
| --- | --- | --- | --- |
| **Mekanism** | Ore processing in tiers (×2 to ×5 with chemical steps), universal cables in four tiers, "factories" (one machine with 3, 5 or 7 parallel slots), a full gas system, a fission reactor with a separate turbine hall and a fusion reactor, the induction matrix (a modular battery you enlarge), the digital miner, the QIO digital storage, jetpacks and the MekaSuit, side configuration and upgrade cards | The reactor-and-turbine split (heat is one number, steam a fluid, turbines make the power); the ×4 route is already here (acid leaching); parallel "factory" slots as a tier-5 machine upgrade card instead of new blocks; gas handling, side configuration and upgrade cards are already Jugcraft's | Energy from crops at a profit (the bio-generator), radiation that spreads through chunks and ticks everywhere, a digital miner that scans huge volumes (the ore drill's column and the deposit drill's finite patches stay), one mod doing everything |
| **Immersive Engineering** | Real-sized multi-blocks formed by hand, wires strung between connectors over distance, the excavator working finite mineral deposits, diesel generators and the refinery, conveyors, the arc furnace, capacitor banks, an in-game manual | Overhead HV lines as connector-to-connector wires (cheap for the server and beautiful), the look of big hand-built industry; deposits, conveyors, the arc furnace and the handbook are already here | Forming a multi-block by hammering an exact arrangement (Jugcraft places one item that fills its footprint); wire burn-out and shock damage |
| **GregTech** | Voltage tiers with transformers, machine hulls per tier, exact mass balance through long processing chains, byproducts that follow real ore associations, every material in every form, cables with loss | Three voltage steps (aluminum, HV, superconducting) with transformers; byproduct chains by real geology (done); the material form sets and the mass audit (done) | Machines exploding on over-voltage, dozens of cable kinds, "circuit configuration" items, chains so long that a part needs fifty steps, and the grind that makes a server's players quit |
| **Thermal Expansion** | Dynamos with augments, machine augments that change behaviour (not only speed), energy cells, the fluid transposer, readable one-block machines, satchels, ducts with round-robin | Behaviour augments as tier-5 upgrade cards (a fortune card for the sieve, a parallel card for the factory, a cryo card for the cracker); round-robin routing is already Jugcraft's | Nothing in particular; its clean "one block, one job" rule is the one Jugcraft keeps for mid tiers |
| **Applied Energistics 2** | The ME network: controller, cables, storage cells of tiered capacity, terminals, autocrafting from patterns, import and export buses, P2P tunnels, spatial storage, meteorites that drop the presses | The cargo lattice: tiered cells, a terminal, buses and pattern crafting through the existing auto-crafter and workshops; "presses" as asteroid finds (the belt is Jugcraft's meteorite) | Channels, unbounded networks, crafting CPUs scanning every tick, and spatial storage (which moves chunks around) |
| **ComputerCraft** | Lua computers and turtles with peripherals, monitors, modems, a sandbox with per-tick limits | The roles (monitoring, automation by rules, displays) through the control network: timers, counters, arithmetic and state machines in the logic controller, drone task lists, monitors; AND a fixed instruction budget per tick | A scripting runtime on a public server (CLAUDE.md: no remote execution; unbounded per-tick work; every sandbox escape is a server compromise). If the owner wants programs later, it is a tiny deterministic bytecode VM with instruction budgets and no I/O beyond peripherals, and needs maintainer design approval first |
| **Ad Astra** | The whole loop: a workbench builds tiered rockets, a launch pad, a planet-selection screen, oxygen distributors and suits with tanks, per-planet gravity, the lander on arrival, space stations, rovers, cryogenic fuel, planet-specific ores (desh, ostrum, calorite) | The loop end to end (pad → destination screen → capsule and parachute → suit and oxygen rooms → base → return), gravity and air as per-dimension data, the station as placed modules, the EVA suit on the scuba tank | Invented ores (Jugcraft uses lunar and martian geology), empty planets (every destination has a reason and, from the red planet on, life), and launches that teleport without logistics (cargo rides as rocket-post deliveries) |

Create is not on the list, but Jugcraft's kinetic branch already borrowed its best idea (rotation as a second, compatible power layer) in batch form; nothing more is needed from it.

## Keeping it runnable on a server

The rules Jugcraft already enforces stay: server authority, bounded message sizes, cached networks with node caps, only the master block of a multi-block ticks, idle machines do nothing, no chunk loaders, no per-tick world scans ([ARCHITECTURE.md](../ARCHITECTURE.md), [CONTENT_BRANCHES.md](../CONTENT_BRANCHES.md#factory-engineering)). The new tiers add these specific commitments.

| Risk | Rule | How it is measured |
| --- | --- | --- |
| Reactors simulating physics | Heat is one number updated once a tick from a handful of inputs; steam is a fluid moved by the existing push; no neutron maps, no per-block temperature | A reactor costs no more than a diesel generator in the tick profile |
| Radiation everywhere | Radiation is a short list of centres with a radius and a decay time in saved data, checked against players (not blocks, not mobs) every 20 ticks; it never spreads | At most 64 centres a dimension; older ones expire first |
| Digital storage scanning | The cargo lattice counts contents when they change (the Fabric storage events) and keeps the count; terminals ask for a page of 64 lines; networks are capped at 1,024 nodes like pipes | Game test: a 1,024-node lattice idles at zero work |
| Oxygen room checks | A room is flood-filled once (4,096-block cap), cached, and re-filled only when a block inside its bounds changes or every 5 seconds, whichever is later; players check their own air like the scuba tank | A 100-room base re-checks fewer than 20 rooms a second |
| Programs | No scripting language; rules run under a fixed budget of 256 operations a tick per controller | The budget is a constant the checker mirrors |
| Dimensions | New worlds are generated only when first visited; nothing is force-loaded; unloaded stations and bases freeze; rockets and deliveries to unloaded areas wait (the rocket post already does); at most one station core per 256-block cell | The dimension count is the destination count, no more |
| Entities | One entity per rocket in flight, per vehicle and per wire; asteroids are blocks; mirrors in the swarm are a number; no new mobs beyond each destination's few | Entity caps per dimension in the config |
| Gravity and air | Attribute modifiers set on arrival, never per-tick velocity edits; the suit's oxygen ticks on the player like the scuba tank | No per-tick player loop beyond vanilla's |
| Networks | HV lines, superconducting cable and the lattice reuse the cable network cache (invalidate on change, rebuild once); coolant for superconductors is counted per network, not per block | Network rebuilds per minute logged in the profiler |
| Benchmarks | Each batch's feature record states a workload (machines, players, hardware) and median and p95 tick time before and after, as [TESTING.md](../TESTING.md) asks; per-feature limits are agreed before merge | A benchmark world with 200 machines, 50 lattice nodes, a reactor and two stations is kept in the repository's test structures |
| Griefing and safety | Reactors and antimatter never break blocks; the walled town's protection and claims apply to launches and landings; a launch pad is owned like a drone depot (the Parties rule, `mayServe`); landings never target protected land | Game tests for each refusal |
| Switches | Every tier has its own feature switch (`atomic`, `space`, `exotic`, plus one per material), which disables acquisition only; dimensions already visited keep existing, and nothing saved is unregistered | The checker keeps `FEATURES` in sync with Java |

## Batches, in order

One batch is one PR with its own feature record, numbers in the balance audit, game tests, handbook pages and advancements, as every batch so far. The order follows dependencies; the owner can reorder.

| # | Batch | Depends on | Adds |
| --- | --- | --- | --- |
| 57 | **Space-age materials** (done) | – | Chromium, cobalt, graphite; stainless steel, nichrome, superalloy; chrome plating, superalloy nozzles, graphite anodes |
| 58 | Energy tiers | 57 | Transformer, HV line (overhead wires), the 16,384 JE/t grid, voltage in the network terminal and sensors |
| 59 | Fluorine and zirconium | 57 | Fluorite, hydrofluoric acid, fluorine, PTFE gaskets and seals, cryolite for faster aluminum, zircon, zirconium, zircaloy |
| 60 | Enrichment | 59 | UF4 and UF6, the centrifuge cascade, enriched and depleted uranium, fuel rods, a real source for depleted-uranium armour |
| 61 | The fission reactor | 58, 60 | Reactor, steam, steam turbines, cooling tower, control rods, scram, meltdown and radiation (**needs the owner's hazard decisions first**), spent fuel casks, reprocessing, thorium cycle, RTG |
| 62 | Aerospace fabrication | 57 | Fuel fabricator (precision fabricator), magnesium, carbon fibre and composite, heat shield, turbopump, gyroscope, avionics, the vacuum induction furnace |
| 63 | Launch pad and orbit | 62 | Launch pad and gantry, stages, destination screen, the orbit dimension, capsule and parachute, EVA suit on the scuba tank, station core and first modules, emergency beacon, return |
| 64 | The Moon | 63 | The lunar dimension and its terrain, regolith and its processor, low gravity, lunar day, polar ice, the lander and rover, sintered regolith |
| 65 | Life support | 63 | Oxygen rooms, airlocks, scrubbers, water recycling, radiation shelters, hydroponics and the cooking pot off-world, solar storms |
| 66 | The cargo lattice | 4 (electronics) | Controller, cells, terminal, buses, pattern crafting |
| 67 | Control electronics II | 4 | Timers, counters, arithmetic, state machines, drone task lists |
| 68 | The red planet | 64, 65 | The dimension, dust storms, caves with life, hematite, perchlorate, Sabatier methane, methalox rockets |
| 69 | The belt | 68 | The asteroid dimension, claims, free fall, platinum group, the orbital refinery, lattice "presses" as finds |
| 70 | Superconductors and beamed power | 58, 63 | Niobium, helium, the cryogenic plant, superconducting cable, space solar arrays, transmitter and rectenna, mass driver |
| 71 | Ice moon and gas giant | 69, 70 | Two dimensions, the drilled base, the hanging scoop station, bulk helium-3 and hydrogen |
| 72 | Fusion | 70, 71 | Isotope column, deuterium, tritium breeding, the fusion reactor |
| 73 | Exotic | 72 | Metallic hydrogen, carbon nanotube, antimatter, exotic matter, the gate (with the Magic branch), the Dyson swarm |

Batches 66 and 67 need nothing from space and can be built whenever a factory player wants them.

## Open questions for the owner

1. **Hazards.** Should the reactor's meltdown and radiation be in the game at all, or should the reactor simply stop (the way the owner chose no block damage for explosives)? The plan proposes a bounded, non-destructive radiation field; the choice gates batch 61.
2. **Voltage.** Is one extra grid tier (HV at 16,384 JE/t, then superconducting) enough, or should machines refuse power above their rating as GregTech's do? The plan proposes caps, never damage.
3. **Programs.** Rules with timers and state machines, or a real (sandboxed, budgeted) program language later? The plan proposes rules only until a maintainer design approves a VM.
4. **How many worlds.** The plan names six destinations beyond orbit. Each is a reviewed increment; the owner may stop at the Moon and the red planet for a long time without anything feeling unfinished.
5. **The magic bridge.** The gate asks the Magic branch (not yet started) for an attunement ritual. Until magic exists, the gate can open only between Jugcraft's own worlds.
6. **Which batch next.** Energy tiers (58) and fluorine (59) are small and unblock everything; the reactor (61) is the big one; the launch pad (63) is the one players will ask for first.

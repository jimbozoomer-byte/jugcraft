# Simplification proposals: plastic, the first powered tool, charges and overlap

Status: proposal only. Nothing here is implemented. Written 27 October 2026 for the owner's decision.

Proposal issue: none. The contributor asked for the most convoluted parts of the pack to be found (26 October 2026). Some were fixed on their own branches (blank runes, dead Nouveau effects, the first-join welcome, shared item names, boilerplate tooltips and codex text, and the encyclopedia). These four change balance or remove content, so they are written up for the owner to choose instead.

All figures come from the recipe files on `main` at e05b0091e. "Machine" counts the stations a player must build, not the times each is used.

## 6. Plastic sheets take too long

### Today

| Route | Steps | Cost per sheet | Notes |
| --- | --- | --- | --- |
| Crude oil | pump crude, Distillation Tower, Polymerization Vat, Metal Press | 2.5 buckets of crude | 1,000 mB crude gives 100 mB refinery gas; 1,000 mB refinery gas gives 4 pellets; the press makes 1 sheet per pellet |
| Ethylene (HDPE) | ethylene source, Polymerization Reactor with its insert, Polymer Extruder with its die | 0.125 bucket of ethylene | 1,000 mB ethylene gives 4 HDPE pellets; 4 pellets extrude to 8 sheets |
| PVC | vinyl chloride from the Synthesis Converter (needs titanium), polymerization, press or extruder | 0.06 bucket of vinyl chloride | 500 mB gives 4 resin; the press makes 2 sheets per resin, the extruder 8 per 4 |
| Bio | potatoes and salt through five machines | 4 potatoes and half a salt | the earlier audit route: 32 potatoes and 4 salt give 8 sheets |

The first route a player meets, crude oil, is the worst by far: 2.5 buckets per sheet, when the other 90% of the barrel goes to fuels. Most early plastic recipes take 2 to 8 sheets.

### Proposal

1. **Refinery gas polymerizes four times better:** 1,000 mB refinery gas gives 16 pellets instead of 4. Crude oil then costs 0.625 bucket per sheet, about the bio route's cost, and still more than ethylene. Ethylene and the extruder stay the better, later route.
2. **The Metal Press presses 2 sheets per pellet**, matching what it already does for PVC resin. With (1) a bucket of crude gives 3.2 sheets.
3. Optional: **naphtha also polymerizes**, at 1,000 mB to 8 pellets, so the 250 mB of naphtha in each bucket of crude has an early use.

(1) alone brings crude to 0.625 bucket per sheet, (2) alone to 1.25, and both together to 0.31. One of them is enough; (1) is the closer match to the other routes.

Checks to run when implementing: the recipe audit's no-gain loop check (pellets and sheets do not burn or reclaim into more crude), and the polymer game tests' per-batch counts.

## 9. The first powered tool is a long way off

### Today

The Mining Drill and the Charging Station each need an **advanced circuit**:
- an advanced circuit comes only from the Circuit Assembler: 2 basic circuits, 3 silver wire and 1 invar plate;
- invar comes only from the Alloy Smelter;
- the drill also needs a tungsten plate, steel plates, a steel gear and lead;
- the station also needs a battery box, copper cable and steel plates.

Built from nothing, the pair is 47 crafted parts from 13 stations: about 88 clay, 70 iron, 4 Nether quartz, plus tungsten and silver. Nothing smaller uses power from your inventory, so a player's first electric tool comes well after their first generator.

### Proposal

1. **A Hand Drill:** a weaker first electric tool from steel plates, a steel gear, a basic circuit and a battery (no tungsten, no advanced circuit). Iron-tier mining speed and drops and a small charge, charged like the other electric tools. The Mining Drill stays the upgrade.
2. **A cheap first charger:** a Charging Station recipe with a basic circuit (the advanced one becomes its faster upgrade), or let a battery box charge the tool in your hand, so charging is not the second advanced-circuit gate.
3. Or, smallest change: **the Mining Drill takes a basic circuit** and the advanced circuit moves to the drill's first upgrade.

Recommendation: (1) and (2). They add one item and one behavior, and leave the existing drill and station unchanged.

## 11. Charges that could be Ley Charge

### Today

Most of the magic resources are distinct on purpose: Focus is what you cast with; the ten Principle Essences (Radiance, Verdance and the rest), Vitae, Astral Resonance and Bound Will are research and ritual ingredients; and Klein stars only hold the alchemy tablet's essence. Those stay as they are.

Two things are separate charges that only exist to power one kind of item:

| Item | What it runs on today | How you refill it |
| --- | --- | --- |
| The four relics: Wardlight Lantern, Hearthstone, Stormglass, Owlsight Circlet | relic charge, 4 per Ley Charge | install or use it at a Reliquary Shrine, which draws from Ley Pylons within reach |
| Flying Broomstick | broom ointment | apply ointment |

Ley Charge itself only goes one way (JE, Radiance and Verdance turn into it; nothing turns it back), which is fine.

### Proposal

1. **Relics show and spend Ley Charge** instead of relic charge, and recharge by using them on any Ley store: a Ley Pylon, a Source Jar, or the shrine as now. A saved relic's charge converts at the existing rate (4 relic charge to 1 Ley Charge), so nothing is lost.
2. **The broom runs on Ley Charge,** refilled the same way; ointment stays as a portable refill worth a fixed amount of Ley Charge.

Not changed: JE and the machine power forms, fuels, Focus, the Principle Essences, Vitae, Astral Resonance, Bound Will, the tablet's essence and Klein stars.

## 14. Overlapping systems

### Today

- **Crusher and Pulverizer:** both are powered machines on a machine casing with similar recipes. They share 20 inputs, all ores: the Crusher turns one ore into 2 raw ore (for smelting), the Pulverizer into 2 dust. The Crusher has 26 other recipes and the Pulverizer 37.
- **Jimtafacts copies of machines** are already handled: the Industrial Generator, Macerator, Electric Furnace, Industrial Solar and Geothermal Generators, Industrial Ore Washer, the BatBox, CESU, MFE and MFSU, and the rest of `tools/jimtafacts_industry.py`'s `MACHINES` with a Jugcraft equivalent are registered as that Jugcraft machine (the Macerator is a Crusher, the Industrial Generator a Coal Generator), with the same recipes and numbers. One Jugcraft machine crafts into its Jimtafacts look and back, one for one. Only the five with no Jugcraft equivalent (Compressor, Extractor, Thermal Centrifuge, Recycler, Metal Cutter) are machines of their own.
- **Power cables:** Jugcraft's copper, aluminum and silver cables, plus 14 Jimtafacts ones (copper, tin, gold, iron and glass fiber, with insulated, double- and quad-insulated, detector and splitter versions). The 90-odd coloured covered, smart and dense cables are the ME network's data cables and are not part of this.
- **Storage:** four separate systems: the ME network (Jimtafacts), the Storage Lectern (Jug Nouveau), the alchemical chest and bags, and the Drone Depot.
- **Spells:** three systems share one Focus pool: Concordance (10 spells), Jug Nouveau (82 glyphs) and Jimtafacts spellbooks (113 spells), with 24 spell books between them.

### Proposal

Merge only where two things do the same job at the same tier; keep anything that does something the others cannot. Nothing is deleted: IDs stay registered and saved items and machines keep working, as the repository rules require.

1. **Crusher and Pulverizer:** the Pulverizer is made from a Crusher and does all of both machines' recipes; for an ore it gives the dust plus a byproduct. Placed Crushers keep working.
2. **Cables:** four power tiers by capacity (copper, aluminum, gold or high-voltage, glass fiber). Every other power cable becomes a look of one of those tiers with that tier's capacity.
3. **Storage:** the Storage Lectern, alchemical chest and Drone Depot get a connector the ME network can read and fill, so one terminal reaches all of them. Each keeps working on its own.
4. **Spells:** left as three systems; the shared Focus pool already ties them together. Only the spell books could be reduced to one book per system with tiers as upgrades.

Each of these is its own change and its own owner decision.

## AI attribution

Written with Claude Opus 5.5 (Claude Code) for the contributor; the contributor is responsible for the output.

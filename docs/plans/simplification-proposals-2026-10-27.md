# Simplification proposals: plastic, the first powered tool, currencies and overlap

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

## 11. About seventeen currencies

### Today

| Branch | Currencies |
| --- | --- |
| Technology | Jugcraft Energy (JE), kinetic power, heat, steam, the fuels, UU-Matter |
| Magic | Focus, Ley Charge, the ten Principle Essences, Vitae, Astral Resonance, Bound Will, alchemical essence (the Prima Materia ledger), Klein star charge, relic charge, Radiance, Verdance, broom ointment |

Conversions that exist:

| From | To | Rate |
| --- | --- | --- |
| Focus | Radiance | 6 : 2 |
| Radiance | Ley Charge | 3 : 2 |
| Verdance | Ley Charge | 3 : 2 |
| JE | Ley Charge | 1,000 : 1 |
| Ley Charge | relic charge | 1 : 4 |
| JE | heat | 5 : 4 |
| heat | JE or kinetic | 2 : 1 |
| JE | kinetic | 80% |

Ley Charge only goes one way: nothing turns it back into anything else. The Hearthstone, flight items and relics each want their own charge.

### Proposal

Keep three currencies a player has to think about, and make the rest storage forms of them:

1. **JE** for machines. Kinetic, heat and steam stay as physical forms with their existing conversions; fuels stay as items.
2. **Focus** for casting, as now.
3. **Ley Charge** for every placed magical device and every rechargeable magical item. Relic charge, Klein star charge, Radiance, Verdance and broom ointment become Ley Charge stored in the item, recharged from any Ley source (pylon, Source Jar, Sourcelink) or from JE at the existing 1,000 : 1.

The Hearthstone, relics and flight items would then recharge from the same pylon or jar. The ten Principle Essences, Vitae, Astral Resonance and Bound Will stay as research and ritual ingredients, but are shown as materials, not as balances.

This is the largest proposal; it changes item tooltips, saved charge components (with a migration that converts each item's charge at its current rate) and the codex. It should go in stages: first make relics and the Hearthstone accept Ley Charge, then fold the others in one at a time.

## 14. Overlapping systems

### Today

- **Spells:** three systems share one Focus pool: Concordance (10 spells, unlocked by research), Jug Nouveau (82 glyphs) and Jimtafacts spellbooks (113 spells), with 24 spell books between them.
- **Machines:** the Crusher (53 recipes) and Pulverizer (57) share 20 inputs. The Jimtafacts housings make a second Generator, Macerator and Electric Furnace beside Jugcraft's. There are 81 generator and engine names, 27 JE cable variants and 99 ME cables.
- **Storage:** four networks: ME, the Storage Lectern, the alchemical chest and bags, and the Drone Depot.
- **Flight:** nine personal flight sources using seven different fuels.

### Proposal

Merge only where two things do the same job at the same tier; keep a specialty where it does something the others cannot.

1. **Crusher and Pulverizer:** make the Pulverizer the Crusher's powered upgrade. It keeps all 57 recipes; the Crusher keeps the early, unpowered ones. Where both list the same input, the Pulverizer gives the Crusher's output plus its byproduct.
2. **Jimtafacts Generator, Macerator and Electric Furnace:** keep their models and IDs, but have their recipes make Jugcraft's machines (the Jimtafacts blocks become a cosmetic variant chosen at the workbench), so there is one generator, one grinder and one furnace line.
3. **Cables:** group the 27 JE cables into four tiers by capacity (copper, aluminum, gold or HV, superconducting), with the other variants as dyed or insulated looks of the same tier.
4. **Spell books:** one book item per system (Concordance wand, Nouveau spell book, Jimtafacts spell book) with tiers as upgrades, rather than 24 separate books.
5. **Storage:** keep ME as the automation network and the Storage Lectern as the early, no-power one; let the Drone Depot and alchemical chest connect to ME rather than run their own catalogs.
6. **Flight:** group flight sources into three: early (gliders), mid (rituals and brooms, Ley Charge) and late (jetpacks and wings, JE). Each item keeps its feel; the fuel follows its group.

Each of these is its own branch and its own owner decision. Nothing should be deleted: IDs stay registered and saved items keep working, as the repository rules require.

## AI attribution

Written with Claude Opus 5.5 (Claude Code) for the contributor; the contributor is responsible for the output.

# Industrial chemistry: electrochemistry and acids

Status: in progress (batch 5 of the Chemistry branch, PR to follow)
Proposal issue: owner request, 1 October 2026 ("merge it and start the next batch immediately"); plan in [CHEMISTRY.md](../branches/CHEMISTRY.md#industrial-chemistry-electrochemistry-and-acids)
Owner: jimbozoomer-byte
Target milestone and tier: steel tier, after the oil line
Primary specialty and supported player role: chemistry and industry; the player who builds factories

## Player experience
Salt, sulfur, phosphate and bauxite have been in the world since the first ores, with nothing to do. This line gives them real reactions in big dieselpunk machines, using the oil line's fluid machines, tanks and pipes.

### Brine and the electrolytic cell (commit 21)
- **Brine:** the chemical mixer dissolves **2 salt + 1,000 mB of water → 1,000 mB of brine** (60 ticks at 64 JE/t). A fluid with a bucket.
- **Electrolytic cell** (three wide, three tall, two deep): a rectifier cabinet (the master block) feeding three olive cells over copper bus bars, with gas headers on a rack at the back.
- **1,000 mB of brine → 250 mB of chlorine + 250 mB of hydrogen + 500 mB of lye**, every 200 ticks at 256 JE/t (51,200 JE a bucket: electrolysis is power hungry).
- Each product leaves at its own height: chlorine from the top row, hydrogen from the middle row, lye from the bottom row (`MachineKind.outputLayer`).
- **Chlorine** and **hydrogen** are gases (tanks and pipes only). **Lye** (sodium hydroxide solution) is a fluid with a bucket.
- The cell also has two item slots and an output slot, for alumina smelting (commit 23).
- Recipe: steel plates, two aluminum cables, two steel tanks, an advanced circuit and a machine casing.

### Sulfuric acid (commit 22)
- **Chemical reactor** (two by two by two): a lead-lined vessel with an acid sight glass, a firebrick sulfur burner with a hopper, a chrome duct, an absorption tower and the control panel.
- **2 sulfur dust + 1,000 mB of water → 1,000 mB of sulfuric acid**, every 100 ticks at 96 JE/t (the contact process, simplified). A fluid with a bucket.
- The reactor has one tank in, one tank out, two item slots and an output slot; bauxite digestion (commit 23) and fertilizer (commit 24) run in it too.
- Recipe: steel plates, glass, two tinplate tanks, a machine casing and a lead ingot.

### Alumina and real aluminum (commit 23)
- **Bayer process** (chemical reactor): **1 bauxite + 250 mB of lye → 2 alumina**, 120 ticks.
- **Hall–Héroult process** (electrolytic cell): **2 alumina + 1 coal coke → 2 aluminum ingots**, 160 ticks at 256 JE/t. The coke is the anode, which burns away.
- **Two ingots from each bauxite**, against one from the arc furnace and a nugget from the blast furnace. Those two stay as the simpler stand-ins. The data audit now counts a bauxite as two ingots of aluminum (it is about half alumina) and an alumina as one.
- Cost per pair of ingots: 40,960 JE in the cell, 3,840 in the reactor and half a bucket of brine's electrolysis for the lye (25,600 JE and a salt). That is about 35,000 JE an ingot, against 12,800 in the arc furnace: more power, twice the metal.
- `check_mod_data` now audits metal in fluid recipes too: no fluid machine gives out more metal than its items hold.

## Connections
- Existing input producer: rock salt ore and the flowback treatment unit (salt); crushed sulfur (sulfur dust); water pumps.
- Existing output consumer: tanks and pipes now; lye goes to bauxite digestion (commit 23), hydrogen to the fuel cell (commit 25), chlorine to later chemistry.
- Technology connection: the oil line's fluid machines and the chemical mixer.
- Magic connection: none.
- Reachable entry path: salt is mined from the start; the mixer and cell need only steel-tier parts. No circular unlock.
- Required vs optional: optional; aluminum keeps its blast-furnace and arc-furnace stand-ins, now the lossy routes.
- For infrastructure/cosmetics: not applicable.

## Balance and automation
- Volume is conserved: a bucket of brine gives 1,000 mB of products in all.
- Salt is renewable only through mining and fracking flowback; the cell costs 51,200 JE a bucket. Any fuel made from its hydrogen (commit 25) returns less than that, so electrolysis is never a power loop.

## Multiplayer and persistence
Server-side machines like the oil line's; tanks and inventories save with the block entity. No new persistent state.

## Dependencies and assets
No new dependencies. Textures and models are original (`tools/petro_textures.py`, `tools/dieselpunk_models.py`).

## Verification
- `tools/check_mod_data.py` audits the new fluids, gases and recipes like the oil line's.
- Game tests `mixerMakesBrine`, `cellSplitsBrine`, `reactorMakesSulfuricAcid` and `bayerRouteMakesAluminum` (PetroGameTests).

## World and event applicability
Not applicable: no worldgen, mobs or dimensions.

## Rollout and open questions
- Chlorine has no consumer yet; PVC, bleach and titanium refining are candidates for a later batch.

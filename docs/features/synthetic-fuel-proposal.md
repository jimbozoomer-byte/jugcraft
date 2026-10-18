# Synthetic fuel (cobalt Fischer-Tropsch): proposal for the owner

Status: **proposal only, nothing implemented.** It turns the owner's selected direction into concrete numbers and lists the choices that still need the owner before code is written.

TODO item: "Prepare independently supplied primary cobalt ore -> compatible cobalt stock -> reusable cobalt/ceramic Fischer-Tropsch tooling. Connect coal/biomass/gas -> cleaned/conditioned CO/H2 syngas -> catalytic synthesis -> one synthetic-crude mixture -> shared refining/upgrading into gasoline and other fuels; audit complete material/energy allocations."

**Already selected by the owner** ([fuels plan](industrial-chemistry-and-fuels-plan.md), 8 and 10 October):

- a separate primary cobalt ore, not a nickel coproduct;
- independently obtainable cobalt stock with an ordinary ceramic support as the reusable first catalyst, with no elaborate preparation chain;
- one synthetic-crude mixture, refined on the existing refinery equipment;
- finished fuels identical to oil-derived ones.

## What already exists to build on

- **Syngas:** the Coal Gasifier makes raw syngas, and the Gas Cleanup and Separation Rack divides 500 mB of it into 250 mB of carbon monoxide and 250 mB of hydrogen.
- **Extra hydrogen:** the Electrolytic Separator.
- **A catalyst-bed tool:** the Nickel Catalyst Bed (brick, nickel ingot, brick). It is a reusable tool in the Chemical Infuser's socket, and is never used up.
- **The refinery:** the Distillation Tower splits crude oil; the Catalytic Cracker cracks heavy oil and reforms naphtha into gasoline.

## Proposed route (numbers to review)

| Step | Machine | Recipe |
| --- | --- | --- |
| 1. Mine | — | **Cobaltite ore** (stone and deepslate) drops 1 raw cobalt (Fortune applies) |
| 2. Smelt | Blast furnace or Arc Furnace | 1 raw cobalt → 1 **cobalt ingot** (blasting only, like nickel and tungsten) |
| 3. Bed | Crafting table | brick, cobalt ingot, brick → 1 **Cobalt Catalyst Bed** (the nickel bed's pattern; reusable, never used up) |
| 4. Synthesis | Chemical Infuser with the cobalt bed (first slice) | 250 mB carbon monoxide + 500 mB hydrogen → 250 mB **synthetic crude** + 250 mB water, 200 ticks |
| 5. Refine | Distillation Tower | 1000 mB synthetic crude → 100 refinery gas, 300 naphtha, 450 diesel, 150 heavy fuel oil, 100 ticks |
| 6. Finish | existing Catalytic Cracker | naphtha → gasoline, heavy fuel oil → diesel and naphtha, unchanged |

Notes on the numbers:

- **Synthesis** follows the real CO + 2 H₂ → –CH₂– + H₂O ratio: one part carbon monoxide to two of hydrogen, with the water kept and accounted.
- **Refining:** synthetic crude splits like a lighter, cleaner crude oil, with more diesel and less heavy oil. It has no sulfur and no asphalt binder.
- **Hydrogen balance:** the Gasifier's cleanup gives CO and H₂ one to one. Each batch therefore needs a further 250 mB of hydrogen, from the Electrolytic Separator. That keeps electrolysis part of the chain, as the plan intends.

## Energy audit (no power loop)

With today's cheapest hydrogen (168 JE/mB, the audit's own figure), 1000 mB of synthetic crude costs at least 2000 mB of hydrogen. That is **336,000 JE** before counting the carbon monoxide, the synthesis power or the refinery.

The fuels from that 1000 mB are worth about **257,000 JE** at today's burn values, even after cracking:

| Fuel | Amount | Burn value |
| --- | --- | --- |
| refinery gas | 100 mB | 192 JE/mB |
| gasoline, from reformed naphtha | ≈ 270 mB | 384 JE/mB |
| diesel | 450 mB | 256 JE/mB |
| diesel, from cracked heavy oil | ≈ 75 mB | 256 JE/mB |

So even all-electrolytic hydrogen cannot make power from nothing. The coal (or biomass) carbon is what pays, as it should. `tools/check_mod_data.py`'s existing power-loop audit would check this automatically once the recipes exist.

## Choices for the owner

1. **Cobalt mineral name.**
   - A: **Cobaltite** (recommended: the classic cobalt ore).
   - B: Skutterudite.
   - C: A generic "Cobalt Ore".
2. **Where cobalt is found.**
   - A: Deepslate depth everywhere, as rare as tungsten.
   - B: Only in mountain and badlands biomes, like the regional ores (recommended: it fits the regional-ore direction of spodumene and vanadium iron).
   - C: Only in a new cobalt-vein structure.
3. **Where synthesis first runs.**
   - A: The existing Chemical Infuser, with the Cobalt Catalyst Bed in its tool socket (recommended: no new machine for the first slice).
   - B: Wait for the planned 6×6×6 Synthetic Fuel Reactor Train.
4. **The refinery split** in step 5.
   - A: As proposed (diesel-heavy, like real Fischer-Tropsch).
   - B: Gasoline-heavy.
   - C: Identical to crude oil.
5. **Cobalt's other uses later**, beyond the bed.
   - A: None for now.
   - B: Cobalt for lithium-ion cell cathodes (ties into the LiPF6 battery plan).
   - C: Cobalt-blue dye or glass.

Each answer keeps the approved direction; none changes existing items, fluids or recipes. Once answered, the implementation is about the size of the magnalium or methalox changes: one ore set, one bed, one fluid, two recipes, tests and a doc.

## AI attribution

Prepared with Claude Opus 5.5 (Claude Code) for the contributor; the contributor is responsible for the output.

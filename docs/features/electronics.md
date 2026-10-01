# Electronics: silicon, chips and the cyan look

Status: in progress (batch 7)
Proposal issue: owner request, 1 October 2026 ("merge it and start the next batch"), following suggestion 3 from batch 5 ("Electronics tier in the cyan look")
Owner: jimbozoomer-byte
Target milestone and tier: the high-tech tier after oil and chemistry
Primary specialty and supported player role: technology; the player who builds factories and automates them

## Plan

| # | Commit | What it adds |
| --- | --- | --- |
| 31 | The cyan look and the crystal grower | High-tech textures (near-black casings with cyan seams, cyan glass and screens, violet conduits); the crystal grower; silicon boules and wafers. |
| 32 | Lithography | A cleanroom lithography station: wafers etched with sulfuric acid into microchips. |
| 33 | Processors | Processors from microchips, gold wire and an advanced circuit in the circuit assembler. |
| 34 | Network terminal | A beige retro computer that reads out the power network it is cabled to. |
| 35 | Docs and PR | Advancements, handbook, docs, screenshots. |

## Player experience

### The cyan look (commit 31)
The owner's references for the tier after oil: dark sci-fi casings with cyan glass panels and screens, a dark multi-block with violet and cyan conduits and a monitor bank with a keyboard, and a beige retro computer. New original textures (`tools/electric_textures.py`): `el_dark` (near-black panels with a cyan trace in the groove), `el_glass` / `el_glass_on` (cyan glass, drawn opaque, glowing inside while the machine runs), `el_glow_violet` (emissive), `el_conduit` (a violet-lit conduit), `el_boule` (silicon crystal), `el_screen_cyan` / `_on`, and the beige `rt_` textures for the retro computer. Models: `tools/hightech_models.py`.

### Crystal grower (commit 31)
- **Two blocks tall:** a dark control cabinet with a cyan screen, a status lamp and power ports, under a glass growth chamber where a silicon boule hangs on its pull rod over a glowing crucible ring, with the pull head on top and violet conduits up the back.
- **4 silicon + 1 phosphate → 1 silicon boule**, 400 ticks at 128 JE/t (51,200 JE). The phosphate is the dopant (phosphorus makes n-type silicon).
- The **sawmill** cuts a boule into **8 silicon wafers** (200 ticks).
- Recipe: glass, titanium ingot, an arc furnace casing, aluminum plates and an advanced circuit.

## Connections
- Existing input producer: silicon (arc furnace from quartz), phosphate (mined), titanium (batch 6), the sawmill.
- Existing output consumer: wafers go to lithography (commit 32).
- Technology connection: power network, processing machines, recipe viewer.
- Magic connection: none.
- Reachable entry path: silicon, phosphate and titanium are all reachable before this tier; no circular unlock.
- Required vs optional: optional; existing circuits keep their recipes.

## Balance and automation
- A boule costs 4 silicon and 51,200 JE and gives 8 wafers: each wafer costs half a silicon and 6,400 JE before the sawmill.
- No item turns back into silicon, and no machine here makes power, so there is no loop.

## Multiplayer and persistence
Server-side machines like the others; the crystal grower is an ordinary powered processor that saves with its block entity. No new persistent state.

## Dependencies and assets
No new dependencies. Textures and models are original (`tools/electric_textures.py`, `tools/hightech_models.py`, `tools/petro_textures.py`).

## Verification
- `tools/check_mod_data.py` checks the new IDs, recipes and models.
- Game test `crystalGrowerPullsABoule` (JugcraftGameTests), which also checks the sawmill's boule recipe.

## World and event applicability
Not applicable: no worldgen, mobs or dimensions.

# Advanced power: solar arrays, engines and tanks

Status: in progress (batch 10)
Proposal issue: owner request, 1 October 2026, with three reference images: "Want bigger solar panels to look like this and be called Advanced Solar Panel, image 2 shows what I want tanks to look like which the player should be able to break and they should maintain how full they are of any liquid put inside it. Image 3 shows what more advanced engines should look like."
Owner: jimbozoomer-byte
Target milestone and tier: the high-tech tier (after advanced materials and electronics)
Primary specialty and supported player role: power and logistics

## Plan

| # | Commit | What it adds |
| --- | --- | --- |
| 50 | Advanced solar panel and engine | A 3×3 solar array on a white pedestal (reference 1); a four-cylinder advanced combustion engine (reference 3). |
| 51 | Tanks | Tanks in the look of reference 2, which keep their fluid when broken. |
| 52 | Docs and PR | Advancements, handbook, docs. |

## Player experience

### Advanced solar panel (commit 50)
- **The owner's first reference:** a white pedestal on a graphite foot with a green-lit ring and power ports, a dark stripe up the column, and a yoke carrying two large wings of deep blue cells, tilted to the sun.
- **Ten blocks:** the pedestal (where cables connect) and the 3×3 layer of cells above it, centred over it. The cells need open sky above them.
- **64 JE/t in full sun**, eight solar panels' worth; half in rain; none at night. 400,000 JE buffer, 512 JE/t out.
- Recipe: three solar panels, aluminum plates, a processor, aluminum cables and a titanium ingot.

### Advanced combustion engine (commit 50)
- **The owner's third reference:** two blocks long, graphite, with a sloping cylinder bank in light ribbed steel carrying four cylinder heads with white caps, tall side pylons with slit vents and ports, two front ports with orange and cyan indicators, and its output shaft out of the back of its right-hand (master) block.
- **Burns gasoline (448 KE a mB) or diesel (320) and turns a shaft at up to 1,024 KE/t**, twice the diesel engine, burning only for what the line takes. It does not take heavy fuel oil.
- Through a magnet dynamo (95%) that is 426 JE per mB of gasoline and 304 per mB of diesel: the best use of either fuel, ahead of the gas turbine (384) and the diesel generator (256).
- Recipe: titanium ingots, a processor, two neodymium magnets, a diesel engine and a machine casing.

## Connections
- Existing input producer: solar panels, processors (electronics), titanium and neodymium magnets (advanced materials), gasoline and diesel (the refinery).
- Existing output consumer: cables and batteries (solar); shafts, the magnet dynamo and any kinetic machine (engine).
- Technology connection: power and kinetic networks.
- Magic connection: none.
- Reachable entry path: all inputs come from earlier tiers; no circular unlock.
- Required vs optional: optional upgrades.

## Balance and automation
- The advanced solar panel is renewable but bound to daylight and open sky, and costs three solar panels and a processor for eight panels' output.
- The engine and magnet dynamo get more JE from a bucket of fuel than any generator, but fuel is finite (oil reservoirs run dry), so there is no loop; the KE → JE → KE round trip still loses power.

## Multiplayer and persistence
Server-side machines; nothing new is saved beyond the ordinary machine block entity.

## Dependencies and assets
No new dependencies. Textures and models are original (`tools/electric_textures.py`, `tools/electric_models.py`); the reference images guided shape and colour only.

## Verification
- `tools/check_mod_data.py` checks IDs, recipes, footprints, models and fuel values.
- Game tests `advancedEngineTurnsAMagnetDynamo` and `advancedSolarPanelFormsAndStores` (PetroGameTests).

## World and event applicability
Not applicable: no worldgen, mobs or dimensions.

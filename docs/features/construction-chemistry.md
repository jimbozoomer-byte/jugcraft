# Construction chemistry: spray foam, cement and concrete

Status: implemented on `feature/construction-32` (batch 32), awaiting review. Compiles and tests in CI only; **not yet played**.
Proposal issue: the owner, 3 October 2026, asked for more useful chemistry, built in the suggested order with weak ideas rethought. This is the fourth batch: construction chemistry.
Owner: jimbozoomer-byte
Target milestone and tier: industrial chemistry (ammonia, plastics) and the steel tier.
Primary specialty and supported player role: building, caving and base defence.

## What was rethought
- **No PVC pipes.** Jugcraft already has bronze and steel fluid pipes, and a third pipe would only be a reskin. The effort went into a building tool instead.
- **No colours of concrete.** Vanilla already has sixteen. Jugcraft's concrete is about strength: plain concrete for bulk building, reinforced concrete as a craftable, obsidian-strength bunker block.
- **The foam sprayer is the headline.** It does something vanilla can't: fill an awkward gap, cave mouth or lava flow from a distance in one action.

## Player experience
- **Foam Sprayer:** aim at a block up to 16 blocks away and use it.
  - Construction foam fills the open space in front of that face: up to 12 blocks per spray, within 2.5 blocks of where it lands, nearest first.
  - It fills air, water, lava and plants. It never replaces a solid block, never fills a space a mob or player stands in, and respects spawn protection and the walled town.
  - It sprays every 0.4 s and uses foam from **foam canisters** in your inventory, 32 blocks each. Canisters come from the chemical reactor: two plastic pellets and an iron nugget in 250 mB of ammonia.
  - Uses: bridge a gap, seal a cave mouth, stop a flood or a lava flow, or wall in a mob farm.
- **Construction Foam:** breaks in a moment and drops nothing. Use **cement** on it to set it into concrete, a block at a time, so a sprayed shape can be made permanent.
- **Cement:** calcite (or a bone block), clay and sand make four cement mix. Smelt the mix (or use a blast furnace) into cement.
- **Concrete:** four cement, four gravel and a water bucket make eight. It is as hard as stone, more blast-resistant (9), and needs a stone pickaxe. Slabs and stairs.
- **Reinforced Concrete:** eight concrete round a rebar make eight. Blast resistance is 1200, like obsidian; hardness 15; it needs a diamond or steel pickaxe. Slabs and stairs. Rebar: three steel ingots make six.
- Advancements: **Expanding Foam** (foam sprayer) and **Bunker Down** (reinforced concrete). Handbook pages: Foam Sprayer, Concrete.

## Connections
- Input producer: ammonia (synthesis converter), plastic pellets (polymerization), steel, calcite, clay, sand and gravel.
- Output consumer: the player's building; reinforced concrete protects bases from creepers and TNT.
- Technology connection: the chemical reactor; walled-town protection is respected.
- Magic connection: none. Required vs optional: optional.

## Balance and automation
- Foam is cheap (32 blocks per canister) but weak and drops nothing, so it can't be farmed or carried as a building block. Setting it costs one cement per block.
- Reinforced concrete costs steel (a rebar per eight) and cement. It is not wither-proof; the wither can still break it, as it can obsidian.
- No positive-gain loops: no recipe gives back more than it uses.

## Multiplayer and persistence
- Server-authoritative: only the server places foam and converts it.
- Each block placed is checked against `Level.mayInteract` (spawn protection), `Player.mayUseItemAt` (adventure mode) and `TownProtection.denies` (the walled town).
- Blocks are ordinary blocks and save with the world.
- Recipes follow the `machines` feature switch (the canister also needs `crude_oil`).

## Dependencies and assets
No new dependencies. Block and item textures, models, recipes and loot tables come from `tools/construction.py`. All original.

## Verification
- `tools/check_mod_data.py`: numbers and block strengths in `ConstructionChemistry` match `tools/construction.py`; blocks, items, textures, names and loot tables exist.
- Game test `foamFillsOpenSpaceAndCementSetsIt` (CI):
  - foam fills water but not stone or the pig's space, and stays within the radius;
  - a canister runs down and is used up;
  - cement turns foam into concrete and is consumed;
  - reinforced concrete's blast resistance is at least 1200.
- Not run: client play (aiming, the look of the blocks), two players, the walled town in play.

## World and event applicability
Not applicable: no worldgen, mobs or dimensions.

## Rollout and open questions
- Spray size, radius and canister size are first values for the owner to tune.
- Hydroponics, electroplating and hydrogen/ammonia storage come next. ANFO blasting is still waiting on the owner's decision, since it breaks blocks.

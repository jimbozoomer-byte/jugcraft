# Petrochemistry (the dieselpunk oil line)

Status: **in progress**, built in batches of five commits (plan: [branches/CHEMISTRY.md](../branches/CHEMISTRY.md#petrochemistry-the-dieselpunk-oil-line)). Compiles in CI; **not yet played**.
Proposal issue: none. The owner asked for this branch directly on 1 October 2026: "the Diesel Punk Chemistry branch of the science tree which should involve crude oil processing turning it into more advanced useable versions of fuel using big machines and oil fracking", planned as 15–20 commits done five at a time.
Owner: jimbozoomer-byte (implementation: Claude Opus 5.5).
Target milestone and tier: steel tier and later (dieselpunk; see [ART_DIRECTION.md](../ART_DIRECTION.md)).
Primary specialty and supported player role: industry and power engineering.

## Player experience

### Crude oil (batch 1, commit 1)
- **Crude oil** is a real fluid: black, slow and thick (it spreads every 20 ticks, three blocks at most from a source on flat ground). It has a bucket, can be placed and picked up, and goes into Jugcraft's tanks, pumps and pipes and into any other mod's fluid storage.
- It **never makes new source blocks**, unlike water, so oil can't be multiplied by placing buckets.
- It is tagged `c:crude_oil` for other mods.
- Where it comes from and what uses it arrive in the next commits (reservoirs, the pumpjack, the oil sand extractor and refining).

## Connections
- Existing input producer: none yet (crude oil comes from reservoirs and oil sand in commits 3–5).
- Existing output consumer: the fluid system (tanks, steel tank, pumps, pipes); refining comes in batch 2.
- Technology connection: steel tier; extends the fluid branch with the first fluids Jugcraft adds itself.
- Magic connection: none planned.
- Reachable entry path: oil sand (an existing surface rock) and oil reservoirs; neither needs anything from this line first.
- Required vs optional: optional for every earlier tier. Oil powers the dieselpunk tier; coal, steam, lava, sun, water and wind stay complete routes.
- For infrastructure: the fluid has no recipe of its own; balance is recorded with each machine.

## Balance and automation
- Crude oil is finite: it never forms new sources, and (from commit 3) reservoirs run dry.
- Units: millibuckets (1 bucket = 1,000 mB).

## Multiplayer and persistence
Fluids are registered whatever the config says, so saved oil and buckets are never lost. Placing and picking up uses vanilla bucket rules, so protections that stop bucket use also stop it.

## Dependencies and assets
Fabric API's fluid rendering registry draws the fluid. Textures are original, drawn by `tools/petro_textures.py`.

## Verification
- `tools/check_mod_data.py` checks that the Java fluids match `tools/petro.py` and that every fluid has its name, block model and animated textures.
- Game tests (`PetroGameTests`): `crudeOilFillsTanks` (a tinplate tank stores a bucket of crude oil) and `crudeOilMakesNoNewSources` (two sources with a gap leave flowing oil, not a new source).
- Not run: client play-testing of how the fluid looks and flows.

## World and event applicability
Not applicable for the fluid itself.

## Rollout and open questions
- IDs `jugcraft:crude_oil`, `jugcraft:flowing_crude_oil` and `jugcraft:crude_oil_bucket` are new and must stay stable once released.

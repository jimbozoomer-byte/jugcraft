# Hydrazine/N2O4 storable rocket fuels

Status: implemented on the contributor's fork, pending review. Delivered with [methane/LOX](methalox.md) and [hydrogen/LOX](hydrolox.md) in one change.

Proposal issue: none. TODO item: "Connect existing RP-1/LOX and requested methane/LOX, LH2/LOX and hydrazine/MMH/N2O4 families … Hydrazine precedes MMH; define engine/oxidizer compatibility …" This change delivers hydrazine and its oxidizer, dinitrogen tetroxide. MMH (monomethylhydrazine) stays open.

**This change's choices:** the chemistry routes, the quantities and the three-motor yield, listed below for the owner to review.

**Engine/oxidizer compatibility, as defined here:**

| Fuel | Oxidizer | Motors per pair |
| --- | --- | --- |
| kerosene | liquid oxygen | 3 |
| liquid methane | liquid oxygen | 4 |
| liquid hydrogen | liquid oxygen | 5 |
| hydrazine | dinitrogen tetroxide | 3 |

Hydrazine pairs only with dinitrogen tetroxide.

Owner: jimbozoomer-byte
Target milestone and tier: rocketry, alongside the cryogenic fuels
Primary specialty and supported player role: chemistry and rocketry; a player who wants liquid motors without running a Cryogenic Liquefier

## Player experience

All in the existing Chemical Reactor:

- **Sodium Hypochlorite** (bleach, a liquid with a bucket): **250 mB chlorine** (first tank) and **500 mB lye** (second tank) make **500 mB** (80 ticks).
- **Hydrazine** (a liquid with a bucket): **500 mB ammonia** (first tank) and **500 mB sodium hypochlorite** (second tank) make **250 mB hydrazine** and **one salt** (120 ticks). This is the Olin Raschig process.
- **Dinitrogen Tetroxide** (a liquid with a bucket): **250 mB ammonia** (first tank) and **1,000 mB oxygen** (second tank) make **250 mB** (120 ticks). Ammonia burnt in oxygen gives brown nitrogen dioxide, which condenses.
- **Tanks:** a rocket casing with **1,000 mB** of hydrazine or of dinitrogen tetroxide makes a **Hydrazine Tank** or a **Dinitrogen Tetroxide Tank** (60 ticks).
- **Rocket motors:** the rocket workshop pairs **one hydrazine tank, one dinitrogen tetroxide tank and two nozzles** into **three rocket motors**.

## Connections

- **Input producers:**
  - chlorine and lye (brine electrolysis);
  - ammonia (the ammonia synthesis);
  - oxygen (air separation or electrolysis).
- **Output consumers:**
  - rocket motors, which go into every rocket;
  - the salt returns to brine electrolysis.
- **Machines:** the existing Chemical Reactor and rocket workshop. No liquefier is needed.
- **Optional:** yes. Every other motor route stays.

## Balance

- One pair needs 4,000 mB of ammonia, 4,000 mB of bleach (2,000 mB chlorine and 4,000 mB lye), 4,000 mB of oxygen and two casings, and makes three motors.
- It is no stronger than kerosene, but needs neither crude oil nor a liquefier, and gives back some salt.
- **No loops:** motors never turn back into chemicals. The 4 salt returned from a hydrazine tank's 2,000 mB of chlorine is less salt than that chlorine took to make.

## Multiplayer and persistence

- **New registrations,** stable from now on:
  - the fluids `jugcraft:sodium_hypochlorite`, `jugcraft:hydrazine` and `jugcraft:dinitrogen_tetroxide`, with buckets;
  - the items `jugcraft:hydrazine_tank` and `jugcraft:n2o4_tank`;
  - the recipes `chemical_reaction/sodium_hypochlorite`, `chemical_reaction/hydrazine`, `chemical_reaction/dinitrogen_tetroxide`, `chemical_reaction/hydrazine_tank`, `chemical_reaction/n2o4_tank` and `rocket_assembly/hypergolic_rocket_motor`.
- The fluids are registered after liquid hydrogen, so earlier fluids' textures are unchanged.

## Dependencies and assets

There are no new dependencies.

- The liquids and buckets are drawn by `tools/petro_textures.py`.
- The two 16×16 tank icons share the cryogenic tanks' helper in `tools/rocketry.py`, without frost:
  - a violet band and red fuel valve for hydrazine;
  - a dark rust-brown band and yellow oxidizer valve for dinitrogen tetroxide.

No texture is copied.

## Verification

**Local checks** (no Gradle or game): `python tools/generate_material_data.py`, `python tools/check_mod_data.py`, `python tools/check_icon_maps.py` and `python scripts/check_repository.py` passed. The icons were inspected at 10x beside the others.

**Server game test for CI:** `MethaloxGameTests.hypergolicChemistryLoadsThreeMotors` checks each chemical step's inputs and outputs, both tank recipes, and the three-motor pairing.

### Results

Not yet run in CI.

## Rollout and open questions

- The routes, quantities and yield await the owner's review.
- Should hydrazine be more dangerous to handle (toxic spills), or stay a plain liquid?
- MMH, the Rotary Condensator role and insulated shared tanks remain open.

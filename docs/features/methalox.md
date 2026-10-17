# Methane/LOX rocket fuel

Status: implemented on the contributor's fork, pending review.

Proposal issue: none. TODO item: "Connect existing RP-1/LOX and requested methane/LOX, LH2/LOX and hydrazine/MMH/N2O4 families to compatible game rocket/station roles. … oxygen cooling -> methane -> more advanced hydrogen capability …" This change delivers the methane/LOX step. Liquid hydrogen and hydrazine stay open.

**This change's choices:** the quantities and the four-motor yield, listed below for the owner to review.

Owner: jimbozoomer-byte
Target milestone and tier: rocketry, alongside the existing kerosene/LOX motor
Primary specialty and supported player role: rocketry; a player without crude oil (a biogas or methanation methane supply) who wants liquid rocket motors

## Player experience

- **Liquid Methane** (a liquid, with a bucket). The existing Cryogenic Liquefier condenses **1,000 mB of methane** into **250 mB of liquid methane** (80 ticks), the same four-to-one as oxygen.
- **Liquid Methane Tank.** The Chemical Reactor fills one **rocket casing** with **1,000 mB of liquid methane** (60 ticks), as it fills the kerosene and liquid oxygen tanks.
- **Rocket motors.** The rocket workshop pairs **one liquid methane tank, one liquid oxygen tank and two rocket nozzles** into **four rocket motors** (200 ticks). Kerosene and oxygen make three.

## Connections

- **Input producers:**
  - methane (the anaerobic digester's cleaned biogas, or methanation from CO or CO2 and hydrogen);
  - oxygen for the liquid oxygen (air separation or electrolysis);
  - rocket casings and nozzles (the rocket workshop).
- **Output consumers:** rocket motors, which go into every rocket: survey, weather, delivery, line-throwing.
- **Machines:** the existing Cryogenic Liquefier, Chemical Reactor and rocket workshop, so no new machine.
- **Optional:** yes. Solid-fuel motors and kerosene/LOX motors stay. This route needs no crude oil, where kerosene does.

## Balance

- One methalox pair costs 4,000 mB of methane, 4,000 mB of oxygen, two casings (six steel plates) and two nozzles, and makes four motors.
- The kerosene pair needs a refinery and hydrogen and makes three. Methane's extra motor reflects its hotter, cleaner burn, and pays for the extra liquefaction of a second gas.
- **No loops:** motors never turn back into methane or oxygen. Liquid out (250 mB) is a quarter of gas in.

## Multiplayer and persistence

- **New registrations,** stable from now on:
  - the fluid `jugcraft:liquid_methane` and its bucket;
  - the item `jugcraft:methane_tank`;
  - the recipes `liquefaction/liquid_methane`, `chemical_reaction/methane_tank` and `rocket_assembly/methalox_rocket_motor`.
- The fluid is registered after every existing fluid, so the textures drawn for the others are unchanged.

## Dependencies and assets

There are no new dependencies.

- The liquid's still and flowing textures and its bucket are drawn by `tools/petro_textures.py` from its own pale grey-blue colours.
- The tank icon is drawn at 16×16 by `tools/rocketry.py`, as docs/ITEM_ICONS.md asks of new icons: the propellant tanks' upright steel casing with an olive band, a red fuel valve and frost.
- The older kerosene and liquid oxygen tanks are still larger legacy icons.

No texture is copied.

## Verification

**Local checks** (no Gradle or game): `python tools/generate_material_data.py`, `python tools/check_mod_data.py`, `python tools/check_icon_maps.py` and `python scripts/check_repository.py` passed. The icon was inspected at 10x.

**Server game tests for CI** (`MethaloxGameTests`):

| Test | What it checks |
| --- | --- |
| `liquefierCondensesMethane` | The liquefier turns 1,000 mB of methane into 250 mB of liquid methane. |
| `reactorFillsMethaneTank` | The reactor fills a rocket casing with 1,000 mB of liquid methane into a methane tank. |
| `workshopLoadsMethaloxMotors` | A methane tank, a liquid oxygen tank and two nozzles make four rocket motors. |

### Results

Fork Build run [38992402213](https://github.com/mmme123-spec/jugcraftfork/actions/runs/38992402213) on commit `4a159946` passed on its first attempt. That covers the mod job (compilation and every server game test, this change's included), the optional-integrations-absent job and all eight rendered-client shards. No human gameplay or multiplayer test was run.

## Rollout and open questions

- The quantities and the four-motor yield await the owner's review.
- Liquid hydrogen/LOX, hydrazine/MMH/N2O4, the Rotary Condensator role and insulated shared tanks remain open.

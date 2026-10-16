# Reagent Purification Station: electronic-grade sulfuric acid

Status: implemented on the contributor's fork; CI green; pending review.
Stacked on the [Bulk Electrolytic Separator](industrial-bulk-electrolytic-separator.md) and the branches below it, on main's factory integration (#358).

Main's integration added concentrated sulfuric acid and makes it in the Concentrator (`concentrate_sulfuric_acid`: a bucket of ordinary acid gives 750 mB and 250 mB of water). This station therefore no longer concentrates acid or registers its own concentrated acid: it takes main's concentrate and adds the electronic grade.

Proposal issue: none. TODO items:
- "Make sulfuric acid a central industrial supply … Implement selected ordinary, concentrated and electronic-grade supplies, distinguishing concentration from purity and accounting for water/impurities/losses";
- "Map the selected shared reagent purification station with specific electronic-grade sulfuric-acid and other demanding reagent recipes. Account for water conditioning inside those recipes".

**From the owner's plan:**
- the [chemistry plan](industrial-chemistry-and-fuels-plan.md)'s selections: ordinary, concentrated and separately purified electronic-grade supplies, with concentration and purity as distinct properties, and one shared purification station;
- the art brief's [Reagent Purification Station](industrial-machine-models-and-textures.md#reagent-purification-station), 4×4×4: two unequal columns and a conditioning chamber on a wet-services bed, ivory enamel, graphite frames and a cyan inspection panel;
- water conditioning inside the recipe, with no stored purified-water fluid.

**This change's choices:** the quantities, times, the bill and the microchip payoff, listed below for the owner to review.

Owner: jimbozoomer-byte
Target milestone and tier: steel era, the acid line, before advanced chips
Primary specialty and supported player role: chemical refining for electronics

## Player experience

The **Reagent Purification Station** is the factory's purification stand:
- a broad, low ivory wet-services bed with graphite rims;
- two unequal muted-steel purification columns, collared, lidded and on mounting shoes, the taller at the back right;
- a compact conditioning chamber at the front right, its cyan inspection panel lit while it works;
- at the front, the deep clean-product hatch with the product spout, a tall amber level strip and the instrument cabinet (the controller);
- at the back, the capped return loops, the residue drain and the two inlets.

It makes a new grade of sulfuric acid. A bucket of concentrated acid from the Concentrator (high inlet at the back), with **250 mB of water** in the low inlet at the back, becomes **900 mB of electronic-grade sulfuric acid** at the front spout in 15 seconds. The water is used up conditioning it, and a tenth of the acid is lost to the residue drain: the plan's water conditioning, accounted inside the recipe.

**The payoff.** The lithography station makes **six microchips** from a wafer and two copper wire with **50 mB of electronic-grade acid**. With ordinary acid it makes four from 100 mB, and with nitric acid four from 50.

**One new liquid** comes with its bucket: electronic-grade sulfuric acid, nearly clear.

**Guides.** Its Engineer's Handbook page describes it, and JEI lists its recipe and the new microchip recipe.

## Connections

- **Existing input producers:**
  - concentrated sulfuric acid from main's Concentrator (and its specialist Acid Concentrator), made from the Chemical Reactor's or the contact process's ordinary acid;
  - water from any pump.
- **Existing output consumers:** the lithography station's microchips, through the new recipe.
- **Technology connection:** the shared form foundation, in the chemical reactor family (96 JE/t), entry profile. One new capability, `jugcraft:reagent_purification`, which later demanding reagents can share.
- **Magic connection:** none.
- **Reachable entry path:** its bill contains a Chemical Reactor, and concentrated acid comes from earlier machines. Nothing it makes is needed to build it.
- **Required vs optional:** optional. Ordinary and nitric acid still make microchips.

## Balance and automation

- **Concentration is distinct from purity.** Main's Concentrator concentrates; this station purifies the concentrate.
- **Energy:** 28,800 JE a bucket to purify, after about 20,500 JE in the Concentrator for the 1,333 mB of ordinary acid behind it.
- **Per chip:** about 12.3 mB of ordinary acid's worth (1,333 mB of ordinary acid gives 1,000 mB of concentrate and 900 mB of electronic grade, 108 chips), against 25 mB with ordinary acid directly, plus about 49,300 JE for every 900 mB.
- **No loops:** every recipe makes less liquid than it uses, and nothing turns back into a fuel.
- **Upgrade cards** fit its terminal.

## Choices made here for review

- **Quantities and times:**
  - electronic grade: 1,000 + 250 water → 900, 300 ticks;
  - microchips: 50 mB → 6 chips.
- **The bill:** a Chemical Reactor, two Steel Tanks, a steel fluid pipe, four steel plates and a basic circuit (`PIP / TRT / PCP`).
- **Cells:** 47 blocks in the 4×4×4 envelope. The model leaves open the corners above the cabinet and the hatch.
- **Not modelled:** the brief's two turning service couplings are drawn standing still.
- **Not done:** other demanding reagents (the plan's later TMAH developer, peroxide and HF) and electronic-grade consumers beyond microchips.

## Multiplayer and persistence

- **Server authority.** The server decides everything.
- **Ports.** Its inlets take only what its recipes use, and nothing can be put in at the outlets.
- **New registrations,** stable from now on:
  - the block and item `jugcraft:reagent_purification_station`;
  - the fluid `jugcraft:electronic_grade_sulfuric_acid` with its bucket;
  - the capability `jugcraft:reagent_purification`;
  - the recipes `chemical_reaction/purification_electronic_grade_sulfuric_acid` and `lithography/microchip_electronic_grade`.
- **Not done:** two-client and dedicated-server play.

## Dependencies and assets

There are no new dependencies.

- **The model** is `reagent_purification_station()` in `tools/industrial_forms.py`. It uses existing textures (`el_white` enamel, gunmetal frames, `dw_steel_plate` vessels, the lit `el_screen_cyan` panel and the amber level strip).
- **The liquid's textures and bucket** are drawn from its colours by `tools/petro_textures.py`, as for every Jugcraft liquid.

## Verification

**Local checks** (no Gradle or game):
- `python tools/generate_material_data.py`, `python tools/check_mod_data.py`, `python tools/check_icon_maps.py` and `python scripts/check_repository.py`: passed.
- An offline render of the part models was inspected from six directions.

**Server game tests for CI** (`ReagentPurificationStationGameTests`):

| Test | What it checks |
| --- | --- |
| `itStandsWholeAndOnlyItsPortsAnswer` | It places as 47 blocks in every orientation, its open cells stay free, only its four ports answer, and the inlets take concentrated acid and water. |
| `theConcentrateIsPurifiedToElectronicGrade` | A bucket of concentrate and 250 mB of water give 900 mB of electronic grade at the spout for exactly 28,800 JE, the water used up. |
| `everyPartLightsWhileItWorks` | Every part lights while it works. |
| `itsRecipesBillAndMicrochips` | Exactly its one recipe, the bill builds it, and 50 mB of electronic-grade acid makes six chips. |

**Client game test** (`ReagentPurificationStationClientGameTests`): three views, a night view, its screen while working and its screen when a full product tank blocks it.

### Results

Fork CI run 38668067592 on `e13af882` (on main #358): the mod build, generated-data and material checks, server game tests (these five included), the server tests without optional integrations, and all five client shards passed.

**Not yet done:**
- a survival playtest;
- a two-client test;
- a performance measurement.

## Rollout and open questions

- The quantities, the bill and the microchip yield await the owner's review.
- Later demanding reagents join this station through the same capability.

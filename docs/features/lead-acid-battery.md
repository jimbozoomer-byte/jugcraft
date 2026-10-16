# Lead-acid battery

Status: implemented on the contributor's fork, pending CI and review.

Proposal issue: none. TODO item: "Develop general storage first, including requested lead-acid, later LiPF6 lithium-ion and major vanadium redox-flow chemistry; defer portable packs and specialty high-output banks." This change delivers the lead-acid step. The lithium bank and flow battery already exist.

**This change's choices:** the capacity, rate and recipe, listed below for the owner to review.

Owner: jimbozoomer-byte
Target milestone and tier: steel era, after the battery box and the sulfuric acid supply
Primary specialty and supported player role: electricity; any player storing power

## Player experience

The **Lead-Acid Battery** is a one-block battery bank: three black moulded cells side by side in a graphite rack, open at the top so their red and black terminal posts and lead straps show. It has a hazard-striped front rail with the output port and a status lamp.

It stores **1,600,000 JE**, four battery boxes' worth. It charges from any side and gives out **1,024 JE/t** from its front, like the other batteries.

Recipe: four lead plates, one bucket of sulfuric acid, two steel plates, a battery box and a copper cable. The bucket is given back.

| Battery | Capacity (JE) | Rate (JE/t) | Size |
| --- | --- | --- | --- |
| Battery Box | 400,000 | 256 | 1 block |
| **Lead-Acid Battery** | **1,600,000** | **1,024** | 1 block |
| Capacitor Bank | 4,000,000 | 4,096 | 2×2 |
| Lithium Battery Bank | 32,000,000 | 16,384 | 3×2 |
| Flow Battery | 64,000,000 | 8,192 | 3×3×2 |

## Connections

- **Input producers:**
  - lead (mined and pressed into plates);
  - sulfuric acid (the Chemical Reactor or the contact process);
  - steel plates;
  - the battery box, which it upgrades.
- **Output consumer:** any machine or cable network, through the shared energy system.
- **Machines:** none new beyond itself. It uses the shared battery behaviour (`MachineKind.isBattery`, `tickBattery`).
- **Optional:** yes. The capacitor bank is still reached the same way.

## Balance

- It is a one-block step between the battery box (400,000 JE) and the 2×2 capacitor bank (4,000,000 JE). It is denser than four battery boxes and costs a battery box, acid and lead.
- **No loops:** storage only; it creates no energy.

## Multiplayer and persistence

- **New registrations,** stable from now on:
  - the block and item `jugcraft:lead_acid_battery`, with its loot table;
  - the recipe `lead_acid_battery`;
  - the textures `el_battery_case`, `el_terminal_red`, `el_lead` and `lead_acid_battery_front`.
- Its energy is saved like every machine's. The server owns charging and discharging.

## Dependencies and assets

There are no new dependencies. The model is drawn in `tools/electric_models.py`, in the power gear's electric look: graphite frame, mint-green glow strips. The classic style uses `lead_acid_battery_front`. The textures are drawn in `tools/electric_textures.py`. Nothing is copied.

## Verification

**Local checks** (no Gradle or game): `python tools/generate_material_data.py`, `python tools/check_mod_data.py`, `python tools/check_icon_maps.py` and `python scripts/check_repository.py` passed. The model was reviewed from four angles in `tools/box_preview.py`, which draws flat faces, not the game's textures.

**Server game tests for CI** (`LeadAcidBatteryGameTests`):

| Test | What it checks |
| --- | --- |
| `leadAcidBatteryStoresBetweenTheBoxAndTheBank` | A placed battery holds 1,600,000 JE, is a battery, and sits between the box and the bank. |
| `leadAcidBatteryCraftsAndGivesTheBucketBack` | The recipe crafts the battery and gives back the acid's bucket. |

### Results

Not yet run in CI.

## Rollout and open questions

- The capacity, rate and recipe await the owner's review.
- An in-game look at the model and a rendered client test remain.

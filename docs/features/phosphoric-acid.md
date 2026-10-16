# Phosphoric acid, triple superphosphate and cola

Status: implemented on the contributor's fork, pending CI and review.

Proposal issue: none. TODO items:
- "Add phosphate-rock wet/hydrometallurgical dissolution, slurry filtration and phosphoric-acid recovery, with triple-superphosphate fertilizer and advanced technology consumers. Add the selected optional paid gypsum-rich residue preparation into plaster/board ingredients…"
- "…food-grade phosphoric acid to cola."

This change delivers the wet process, triple superphosphate and cola. It also gives the existing gypsum binder a second residue source. Advanced technology consumers and plaster/board ingredients remain open.

**This change's choices:** the quantities, the fertilizer's four doses and cola's effect, listed below for the owner to review.

Owner: jimbozoomer-byte
Target milestone and tier: steel era, after the Chemical Reactor and the sulfuric acid supply
Primary specialty and supported player role: chemistry and farming

## Player experience

One new fluid and two new items, all from the existing Chemical Reactor:

- **Phosphoric acid** (a fluid, with a bucket). Two phosphate rock in **250 mB of water (first tank)** and **250 mB of sulfuric acid (second tank)** give **250 mB of phosphoric acid** and **one calcium sulfate**. The acid goes in the second tank on purpose: the older superphosphate recipe takes acid in the first tank, so the two recipes never compete for the same fill.
- **Triple superphosphate.** One phosphate rock and 250 mB of phosphoric acid give four bags. A bag works like superphosphate on the crops in a 5×5 area, but gives each crop **four** bone-meal doses instead of two (ammonium nitrate gives three).
- **Cola.** Four glass bottles and four sugar, with 1,000 mB of water, 50 mB of phosphoric acid and 250 mB of carbon dioxide, give four bottles of cola. Drinking one, even on a full stomach, gives 3 hunger, **Haste I for 30 seconds** and the empty bottle back. Bottles stack to 16.

The calcium sulfate is the same residue the [fluorite dissolution](fluorochemistry-playtest.md) leaves. The Mineral Conditioning Station already turns two of it, with water, into Prepared Gypsum Binder for white concrete powder.

## Connections

- **Input producers:**
  - phosphate rock (mined, or crafted from bat guano);
  - sulfuric acid (the Chemical Reactor or the contact process);
  - water;
  - carbon dioxide (the fermenter, the digester and the other CO2 producers);
  - sugar.
- **Output consumers:**
  - crops (triple superphosphate);
  - the player (cola);
  - the Mineral Conditioning Station's gypsum binder (calcium sulfate).
- **Machines:** the existing Chemical Reactor, so no new machine.
- **Optional:** yes. Superphosphate and ammonium nitrate stay as they were, and nothing in core progression needs these products.

## Balance

- 2 rock and 250 mB of sulfuric acid make 250 mB of phosphoric acid; with 1 more rock that makes 4 bags at 4 doses each. Superphosphate gives 4 bags at 2 doses from the same 2 rock and 250 mB of acid. The wet process costs more rock and two steps, and gives twice the growth.
- 50 mB of acid makes 4 colas, so one batch of acid makes twenty.
- **No loops:** none of the products turn back into phosphate, acid or sugar. Fluid out (250 mB) never exceeds fluid in (500 mB).

## Multiplayer and persistence

- **New registrations,** stable from now on:
  - the fluid `jugcraft:phosphoric_acid`, with its block and the bucket `jugcraft:phosphoric_acid_bucket`;
  - the items `jugcraft:triple_superphosphate` and `jugcraft:cola`;
  - the recipes `chemical_reaction/phosphoric_acid`, `chemical_reaction/triple_superphosphate` and `chemical_reaction/cola`.
- The new fluid is registered after every existing fluid, so the textures drawn for the others are unchanged.
- The server applies the fertilizer and the drink's effect.

## Dependencies and assets

There are no new dependencies. The art is drawn by `tools/petro_textures.py`:

- the fluid's still and flowing textures and its bucket, from its own colours;
- triple superphosphate: superphosphate's sack in grey with three blue-grey granules;
- cola: a stoppered bottle of dark brown cola with rising bubbles.

No texture is copied.

## Verification

**Local checks** (no Gradle or game): `python tools/generate_material_data.py`, `python tools/check_mod_data.py`, `python tools/check_icon_maps.py` and `python scripts/check_repository.py` passed. The icons were inspected at 8x.

**Server game tests for CI** (`PhosphoricAcidGameTests`):

| Test | What it checks |
| --- | --- |
| `phosphateRockGivesPhosphoricAcid` | The wet-process recipe takes water first and acid second, and gives 250 mB of phosphoric acid and one calcium sulfate. |
| `phosphoricAcidMakesTripleSuperphosphateAndCola` | Rock and acid give four triple superphosphate, which out-doses superphosphate; the cola recipe gives four. |
| `colaGivesHasteAndTheBottle` | Drinking cola gives Haste and leaves a glass bottle. |

### Results

Not yet run in CI.

## Rollout and open questions

- The quantities, the four doses, cola's Haste and the order of the reactor tanks await the owner's review.
- Phosphoric acid's advanced technology consumers and the plaster/board ingredients remain open.

# Alumina ceramics

Status: implemented on the contributor's fork, pending CI and review. Builds on [porcelain insulators](porcelain-insulators.md) (`claude/porcelain-insulators`).

Proposal issue: none. TODO item: "Implement ceramic powders -> blanks -> firing -> shared grinding/polishing where needed: clay foundation, porcelain-style insulators and alumina, then specialist zirconia…". This change is the second slice, alumina components, after porcelain. The contributor chose it.

**This change's choices:** the press recipe, the firing rule and the consumer, listed below for the owner to review.

Owner: jimbozoomer-byte
Target milestone and tier: aluminum era (bauxite and alumina); consumed in electronics
Primary specialty and supported player role: ceramics and electronics

## Player experience

1. **Powder.** Alumina comes from the existing bauxite chain: bauxite and lye in the Chemical Reactor, or the bauxite digester and alumina calciner.
2. **Pressing.** Two alumina and a clay ball (the binder) craft into an **Unfired Alumina Substrate**, a matte grey pressed tile.
3. **Firing.** Only a **blast furnace** fires it into an **Alumina Substrate**, a dense ivory-white tile with four gold bond pads. An ordinary furnace is not hot enough, unlike porcelain, which either furnace fires.
4. **Use.** The **circuit assembler** gains a third way to make a processor: four microchips and an advanced circuit bonded on an alumina substrate, in place of the gold ingot or two optical fibres.

## Connections

- **Input producers:**
  - alumina (bauxite processing);
  - clay balls;
  - a blast furnace.
- **Output consumer:** processors, through the circuit assembler. The gold and fibre routes are unchanged.
- **Machines:** the existing blast furnace and circuit assembler, so no new machine.
- **Optional:** yes. It is an alternative route, so nothing requires alumina ceramics.

## Balance

- Two alumina and a clay ball make one substrate, and one substrate replaces one gold ingot in a processor. Gold-poor bases with an aluminum line gain a steady route.
- **No loops:** a substrate does not turn back into alumina.

## Multiplayer and persistence

- **New registrations,** stable from now on:
  - the items `jugcraft:unfired_alumina_substrate` and `jugcraft:alumina_substrate`;
  - the recipes `unfired_alumina_substrate`, `alumina_substrate_from_blasting` and `circuit_assembly/processor_on_alumina`.

## Dependencies and assets

There are no new dependencies. Both icons are drawn by `tools/petro_textures.py` from one 10×10 tile, symmetric on both axes: matte grey unfired, ivory-white fired with four gold pads. No texture is copied.

## Verification

**Local checks** (no Gradle or game): `python tools/generate_material_data.py`, `python tools/check_mod_data.py`, `python tools/check_icon_maps.py` and `python scripts/check_repository.py` passed.

**Server game tests for CI** (`AluminaCeramicsGameTests`):

| Test | What it checks |
| --- | --- |
| `aluminaIsPressedAndFiredOnlyInABlastFurnace` | Two alumina and a clay ball press a blank; a blast furnace fires it and an ordinary furnace does not. |
| `processorsCanBeBondedOnAlumina` | The circuit assembler's alumina processor recipe loads. |

### Results

Not yet run in CI.

## Rollout and open questions

- The press recipe, the blast-furnace-only rule and the processor route await the owner's review.
- Specialist zirconia and the shared grinding/polishing station remain.

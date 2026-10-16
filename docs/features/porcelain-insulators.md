# Porcelain insulators

Status: implemented on the contributor's fork, pending review.

Proposal issue: none. TODO item: "Implement ceramic powders -> blanks -> firing -> shared grinding/polishing where needed: clay foundation, porcelain-style insulators and alumina, then specialist zirconia…". This change is the first slice, porcelain-style insulators, following the selected chain from the [chemistry plan](industrial-chemistry-and-fuels-plan.md): powder preparation -> shaped blank -> kiln firing. No grinding or polishing is needed for this part.

**This change's choices:** the body's ingredients, the yields and the consumer, listed below for the owner to review.

Owner: jimbozoomer-byte
Target milestone and tier: steel era, early ceramics; consumed by the nuclear plant's Main Transformer
Primary specialty and supported player role: ceramics and electricity

## Player experience

1. **Preparation.** Two clay balls, a quartz and a bone meal craft into two **Porcelain Body**: off-white prepared clay.
2. **Shaping.** Three porcelain body stacked in a column craft into one **Unfired Porcelain Insulator**: the insulator's shape in matte buff clay, without caps.
3. **Firing.** A furnace or blast furnace fires the blank into a **Porcelain Insulator**: glossy brown glazed porcelain with four sheds and galvanized steel caps.

The nuclear plant's **Main Transformer** now takes two porcelain insulators as its bushings, where it took two white terracotta. Transformers already built are untouched.

The unfired blank is not accepted in place of the fired insulator, as the plan requires.

## Connections

- **Input producers:**
  - clay balls;
  - quartz;
  - bone meal;
  - a furnace or blast furnace.
- **Output consumer:** the Main Transformer.
- **Machines:** none new. The shared kiln role is the vanilla furnace and blast furnace for now; a dedicated ceramics kiln or grinder can come with later precision parts.
- **Reachability:** everything is available before steel. The transformer itself still needs steel and aluminum.

## Balance

- 2 clay, 1 quartz and 1 bone meal make 2 body; 3 body make 1 blank; 1 blank fires into 1 insulator. A transformer's two insulators cost 6 body (3 crafts).
- **No loops:** fired porcelain does not turn back into clay.

## Multiplayer and persistence

- **New registrations,** stable from now on:
  - the items `jugcraft:porcelain_body`, `jugcraft:unfired_porcelain_insulator` and `jugcraft:porcelain_insulator`;
  - the recipes `porcelain_body`, `unfired_porcelain_insulator`, `porcelain_insulator_from_smelting` and `porcelain_insulator_from_blasting`.
- The `main_transformer` recipe changes its bill. Its ID and every placed transformer stay.

## Dependencies and assets

There are no new dependencies. The icons are drawn by `tools/petro_textures.py`. The two insulators are drawn from one shape, symmetric about the middle columns: the unfired blank in matte buff, the fired one in brown glaze with steel caps. No texture is copied.

## Verification

**Local checks** (no Gradle or game): `python tools/generate_material_data.py`, `python tools/check_mod_data.py`, `python tools/check_icon_maps.py` and `python scripts/check_repository.py` passed.

**Server game tests for CI** (`PorcelainInsulatorGameTests`):

| Test | What it checks |
| --- | --- |
| `porcelainIsPreparedShapedAndFired` | The body recipe gives two, three body shape a blank, and both the furnace and blast furnace fire it into an insulator. |
| `porcelainInsulatorsGoIntoTheMainTransformer` | Porcelain insulators complete the Main Transformer; white terracotta no longer does. |

### Results

Fork Build run [38879961136](https://github.com/mmme123-spec/jugcraftfork/actions/runs/38879961136) on commit `9846790f` passed on its first attempt (an earlier run on 9f3d060b failed to compile the new test, fixed in 9846790f): the mod job (compilation and every server game test, this change's included), the optional-integrations-absent job and all five rendered-client shards. No human gameplay or multiplayer test was run.

## Rollout and open questions

- The body's ingredients, the yields and the consumer await the owner's review.
- Alumina components, specialist zirconia and the shared grinding/polishing station remain.

# Armor in Blockbench: the owner's sets as Blockbench projects, rebuilt from their renders

Status: implemented on `claude/festive-volta-4fa6fw` (stacked on `claude/epic-pascal-f8gpfg`, where the armor sets were
made). The data checks pass; **not compiled in CI on this branch yet, not game-tested and not played.**
Proposal issue: none. On 8 October 2026 the owner asked: "Can you use blockbench to make the armors I was making ...
better and more accurate to the images I had provided?" Earlier the same day they had written of the Sentinel's
pauldron "can you just 1:1 copy the pixel art from the source I gave you".
Owner: jimbozoomer-byte (designs: the owner; implementation: Claude Opus 5.5).
Target milestone and tier: tooling and art for the existing armor-only tiers ([armor-designs-8-october.md](armor-designs-8-october.md)); no new items or tiers.
Primary specialty and supported player role: combat (defense), cosmetic. Everyone who wears these sets.

## Player experience
The sets look like the owner's designs. The first one rebuilt is the **Sentinel**: its helm is the owner's 9 × 9 × 9
box with their art (the keyhole, the cheek strips, the meander behind and on the sides) copied texel for texel from
their renders; its right pauldron is the owner's bent plate, measured off their front and back views; its boots are
short gold boots whose tops rise in teeth; its coat, gorget and thigh plates follow the renders. Every texel the
renders show was lifted from them.

For the owner, each rebuilt set is a Blockbench project they can open, look at on a player and edit, and what they
save there is what the game draws.

## How it works
- **A set can be a Blockbench project.** `art/armor/<set>.bbmodel` holds its cubes and its texture.
  `tools/bbmodel.py` reads it (`load_set`) into the toolkit's parts, each with its own texels, so the quad budget, the
  flicker and skin checks and the generators work on it as on any set, and the atlas is the project's texture.
- **Any set can be written as one** (`python3 tools/bbmodel.py export --set <name>`), and every set survives the trip:
  `python3 tools/bbmodel.py check` exports each registered set, reads it back two ways and compares the quads and the
  atlas with the toolkit's own. Of the 20 sets, 15 come back byte for byte and 5 within the last decimal place.
- **The project format** is Blockbench 5's Generic Model ("free"), format version 5.0: cubes turned on any axis by any
  angle, a uv rectangle per face, the atlas embedded. Its outliner is a group per bone (`head`, `body`, `right_arm`,
  `left_arm`, `right_leg`, `left_leg`, pivots where the player's are), inside each a group per worn piece named for its
  `worn_models.json` key (`sentinel_chestplate_right_arm`), and a locked grey `player` to model round, not exported.
  The mapping between Blockbench's space and the toolkit's (a half turn about z and 24 pixels up) and the uv corner
  order were taken from Blockbench 5.2.1's source (`js/util/three_custom.js`, `js/outliner/types/cube.js`).
- **Rebuilding from renders** (`tools/armor_reference.py`): the owner's renders are kept under
  [art/armor/references/](../../art/armor/references/README.md). A camera and the figure's pose are fitted to each
  (its outline against the background, then one part's outline, here the helm's); a part's shape can be fitted so the
  art lifted from one view agrees with the others (`PhotoFit`); then each face of the model takes its texels from the
  view that sees it best, its projected corners nudged until the render's texel cells are flat colours (pixel art),
  each texel the median of its cell. Faces no render shows are filled from the face opposite. Weapons and effects in
  front of the armor are left out.
- **What the renders taught:** the Sentinel's texture is drawn two texels to a model pixel (its keyhole slit is two
  texels wide), so its atlas has density 2; its helm is a plain box with the detail painted on (the previous model had
  raised frames and ribs the renders do not have); the figure holds its sword with the right arm 10 degrees forward,
  not 21.

### Editing a set in Blockbench
1. Open `art/armor/<set>.bbmodel` in Blockbench (5.x). Hide the grey `player` with its eye icon to see the armor alone.
2. Edit cubes (move, resize, turn, add) inside the piece groups, and paint the texture. A new cube goes in the group of
   the piece and bone it belongs to; a cube straight in a bone's group belongs to that bone's usual piece (head: the
   helmet; body and arms: the chestplate; legs: the leggings).
3. Save the project (File > Save Project) with the texture embedded, which Blockbench does while its Embed Textures
   setting is on (the default).
4. Run `python3 tools/generate_material_data.py` and `python3 tools/generate_textures.py`, then
   `python3 tools/armor_models.py` (flicker and skin rules) and `python3 tools/check_mod_data.py`.
Keep a bone's own group at rest (not turned) and give cubes no stretch; `load_set` refuses both. Meshes are read by
`tools/bbmodel.py quads` but a set takes cubes only, since the checks need boxes.

### Commands
```
python3 tools/bbmodel.py export --set sentinel     write art/armor/sentinel.bbmodel from the registered set
python3 tools/bbmodel.py check                     every set through a project and back: quads and atlas compared
python3 tools/bbmodel.py quads FILE.bbmodel        what a project holds, piece by piece
python3 tools/armor_reference.py compare --set sentinel    each fitted render beside our model from its camera
```

## Connections
- Existing input producer: the owner's Blockbench designs and renders; the armor items of [armor-designs-8-october.md](armor-designs-8-october.md).
- Existing output consumer: `worn_models.json` and `textures/entity/equipment/3d/<set>.png`, drawn by the client's worn-model layer as before.
- Technology connection / magic connection: none new; the sets keep their tiers and stats.
- Reachable entry path: unchanged (the sets are creative-only until the owner places them).
- For infrastructure/cosmetics: this is the art pipeline of existing items; resource links do not apply.

## Balance and automation
Nothing changes: no recipe, stat or durability is touched.

## Multiplayer and persistence
Client-side art only. Item IDs, worn-model keys and texture names keep their names, except that the Sentinel's
leggings no longer draw a waist piece of their own (`sentinel_leggings_body`): the owner's coat covers the waist. No
saved data refers to worn-model keys.

## Dependencies and assets
- No new runtime dependency. Blockbench is the owner's editor; nothing of it is in the repository or the mod. The file
  format was read from Blockbench 5.2.1's source (GPL-3.0, read only, not copied).
- New art: the owner's renders (`art/armor/references/`) and the Sentinel's project (`art/armor/sentinel.bbmodel`),
  whose texels come from those renders. The owner's own work.
- This session could not run Blockbench itself: building it from source was refused by the sandbox as outside code,
  and web.blockbench.net is not reachable from it. So the projects are checked against Blockbench's source, not opened
  in it.

## Verification
Actually run on this branch, 8 October 2026:
- `python3 tools/bbmodel.py check`: 20 of 20 sets round trip (15 byte for byte, 5 within rounding), both through
  `entries` and through `load_set`.
- `python3 tools/armor_models.py`: no problems for any set; the Sentinel 42 parts and 237 quads (it was 96 and 518).
- `python3 tools/armor_smoke.py --no-render`: all checks passed.
- `python3 tools/generate_material_data.py` and `python3 tools/generate_textures.py`: only the Sentinel's entries and
  atlas change (the texture generator also rewrote some unrelated plant textures, which are left out of this change).
- `python3 tools/check_mod_data.py`: PASS, art check allow-list unchanged (A2 1, H1 3, O1 52).
- `python3 scripts/check_repository.py` and `python3 tools/check_icon_maps.py`: PASS.
- Compared with the owner's three renders from their fitted cameras (`armor_reference.py compare`).

Not run: opening the projects in Blockbench, the Gradle build, the game and client tests, and any play.

## World and event applicability
Not applicable: art only.

## Rollout and open questions
- The owner's own `.bbmodel` files would make every set exact. Committing them under `art/owner-library/` (or sending
  them) lets `tools/bbmodel.py` read them straight in; the rebuilds from renders are close but not their model.
- Rebuilt so far: the Sentinel. The other sets of the 7 and 8 October designs are still the toolkit's models.
- Faces no render shows are filled from the faces opposite, so the inside of an arm can repeat its outside.
- Reversible: the previous Sentinel module is in Git history; restoring it and regenerating brings the old model back.

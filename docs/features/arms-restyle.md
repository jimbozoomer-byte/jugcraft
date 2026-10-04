# Arms restyle: pixel-art icons and 3D models in the hand

Status: implemented on `claude/arms-restyle`, awaiting review. Compiles and tests in CI only; **not yet played**.
Proposal issue: the owner, 4 October 2026: "Refine all the weapons make them look better, study how other people have made weapons that actually look good and copy that currently they look AWFUL", with Simply Swords, Epic Knights and RPG Style More Weapons attached to study ("i like these too for more unique weapons").
Owner: jimbozoomer-byte
Target milestone and tier: art only, for every arm of batches 42 to 52 ([arms.md](arms.md) to [arms-vi.md](arms-vi.md)).
Primary specialty and supported player role: fighting (looks only).

## Player experience
Every arm (34 kinds in bronze and steel) and the Arms VI kit are redrawn:

- **Inventory icons:** crisp pixel art, 32×32 (48×48 for the great arms and polearms), drawn on the exact 45-degree pixel diagonal in flat tones, lit from the top left, with a one-pixel outline. Each part reads on its own: blade, guard, grip, pommel, haft, head. These icons show in inventories, item frames, on the ground and on shelves.
- **In the hand, a 3D model:** each arm is built of boxes with real thickness: a thin blade with a raised ridge or a sunk fuller, chunky guards and pommels, round grips and hafts, deep heads on hammers and maces. It is shaped and painted from the same design as its icon and lies exactly over it, so the arm is held as before.
- **The palettes:**
  - Bronze arms are warm copper-bronze with yellow brass fittings, leather grips and oak.
  - Steel arms are blued steel with gunmetal fittings, black rubber grips, dark wood and brass rivets.
  - The ornate arms carry a garnet (bronze) or a lit green phosphor stone (steel).
- **The longbows and arbalests** are redrawn the same way, as vanilla lays out its bow and crossbow. The shields' faces are flattened to clean tones.

## How it was studied
- **Simply Swords and Epic Knights (studied, not copied):**
  - 32 and 48-pixel sprites along the diagonal;
  - three to five interior tones a part, a bright edge facing the light and a dark one away from it;
  - a light outline on the lit side and a black one on the other;
  - about ten colours an item, no dithering or noise;
  - in the hand, scaled up 1.7 to 1.9 times.

  Our old sprites were 64×64, rendered with soft shading, dithering and glints, with no outline. That is why they read as muddy.
- **RPG Style More Weapons (studied, not copied):** weapons built of 40 to 85 boxes with painted textures, ornate guards and dark grips, real thickness.
- **None of their textures, models or code is used,** as the repository's rules require. Every design here is original, drawn by code in `tools/arms_art.py`.

## How it works
- **One design per kind:** `tools/arms_art.py`, along the weapon's own axis, in parts (strips with a profile, polygons, discs, rings, bars). Each part has a material and a thickness. The designs are about 28 units for each unit of the kind's in-hand size, so every arm in the hand has the same texel size.
- **The toolkit, `tools/arms_pixel.py`:**
  - draws the icon on the pixel diagonal: tones from each part's distance to its lit and shaded edges, then the outline;
  - draws the upright texture for the model;
  - builds the model's boxes: greedy rectangles per thickness, each turned 45 degrees about the hand so the model lies over the icon. Front and back faces are textured from the upright image, the sides from its edge texels.
  - At most 32 boxes a model.
- **Icon layout:** each icon is fitted whole, a pixel in from its edges. A wide head makes the rest smaller, and the hand pose makes up for it (`hand factor`), so every arm is as long in the hand as before.
- **Models:**
  - `models/item/arms_<kind>.json` is the 3D model, with vanilla's sword poses, or the spear's for the spear and lance, scaled as before.
  - `models/item/<item>_in_hand.json` textures it (`textures/item/<item>_model.png`).
  - `models/item/<item>.json` is the icon.
  - `items/<item>.json` picks the icon for the GUI, ground, frames and shelves, and the 3D model otherwise.
  - The brazier mace's flame flickers in both (its model is shaped to fit all four frames).
- **The data check** now allows 48×48 textures (`check_mod_data`), which the long arms' icons use. `docs/ART_DIRECTION.md` says so.

## Connections
None changed: no recipes, numbers, ids, tags, components or Java. Every arm, its tooltips, its motion and its weapon art are as before.

## Balance and automation
Not applicable (art only).

## Multiplayer and persistence
- **Saved state:** unchanged; ids are stable.
- **Removed:** the textures `textures/item/<bronze|steel>_<spear|lance>_in_hand.png`. The 3D models replace them, and no saved data refers to a texture.
- **Client-only:** the in-hand models are plain JSON item models, with no new rendering code.

## Dependencies and assets
- No new dependencies.
- The art is generated: `python3 tools/generate_textures.py` and `python3 tools/generate_material_data.py`.

## Verification
- **`python3 tools/check_mod_data.py`:** PASS (local).
- **`python3 scripts/check_repository.py`:** PASS (local).
- **Every arm model is checked to be within the JSON model limits:** coordinates −16 to 32, ±45° turns, UVs 0 to 16.
- **CI:** the client tests' racks, frames, held-arm, two-handed, weapon-art and Arms VI screenshots show the new art in game. Results are in the pull request.
  - **The run on fdae38d2** showed every kind's model in third person and ten in first person. The scythe's blade point floated apart from the blade, so the toolkit now joins pieces that per-texel sampling splits.
  - **The run on fb178b85** shows the scythe and kama blades whole in first and third person, and the flail, kusarigama, rapier and twinblade in one piece.
- **Not run:** play; how the models look in every pose of the arms motion.

## World and event applicability
Not applicable.

## Rollout and open questions
- **Each design is a first pass for the owner to judge:** proportions, palettes and details are easy to change in `tools/arms_art.py`.
- **Possible next steps,** each needing the owner's choice:
  - more ornate variants;
  - glowing parts (an emissive layer);
  - the same restyle for the power katana, power bow and other weapons outside the arms.

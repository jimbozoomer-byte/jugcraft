# Dieselworks building blocks (batch 45)

Status: implemented (pending CI and review)
Proposal issue: none; the owner chose it in chat ("start the building blocks batch") from the dieselpunk ideas list.
Owner: jimbozoomer-byte
Target milestone and tier: steel tier (needs iron, copper and steel plates)
Primary specialty and supported player role: building and decoration

## Player experience
Blocks in the look of the batch 44 dieselpunk giants, so a factory can be built to match its machines:

| Block | Variants | Recipe (makes) |
| --- | --- | --- |
| Weathered Steel Plate (`rust_plate`) | slab, stairs | 4 iron plates (4) |
| Riveted Steel Plate (`riveted_rust_plate`) | slab, stairs | 4 weathered steel plate + iron nugget (4) |
| Patina Plate | slab, stairs | 4 copper plates (4) |
| Perforated Patina Plate | | 4 patina plate + iron nugget (4) |
| Red Iron Plate | slab, stairs | 8 weathered steel plate + red dye (8) |
| Copper Dome Plate | slab, stairs | 4 patina plate + copper ingot (4) |
| Riveted Band | | 4 weathered steel plate + 4 iron nuggets (4) |
| Skid Iron | | 4 steel plates (4) |
| Ribbed Patina Pillar / Ribbed Steel Pillar (`ribbed_rust_pillar`) | placed along an axis | 2 patina / weathered steel plate (2) |
| Steel Grating (`rust_grating`, see-through) | slab | 4 iron bars (4) |
| Steel I-Beam (placed along an axis) | | 7 steel plates (6) |
| Porthole Window | | 4 weathered steel plate + glass (4) |
| Amber Cage Lamp (light 14) | | 7 iron nuggets + glowstone + weathered steel plate (2) |

Slabs make 6 from 3 and stairs 4 from 6, as in vanilla.

## Connections
- Existing input producer: the plates from the metal press, iron bars, glass, glowstone and dye.
- Existing output consumer: building only.
- Technology/magic connection: none; this is decoration. It reuses the giants' textures (`dr_*`). The grating and porthole textures (`dw_*`) are new and drawn by code.

## Balance and automation
One plate of metal makes one block. No recipe turns a block back into metal, so nothing gains material.

## Multiplayer and persistence
Ordinary blocks with no block entities and nothing that ticks. All are mined with a pickaxe and drop themselves; a double slab drops two.

## Dependencies and assets
No dependencies. Code: `building/Dieselworks.java`. Data and art: `tools/dieselworks.py`. All art is original.

## Verification
- Planned in CI: the game test `dieselworksBlocksPlace`. It places all 25 blocks and checks that the grating is see-through, the I-beam is not a full cube and the lamp gives light 14.
- Planned in CI: the client screenshot `jugcraft_dieselworks`, a riveted wall with portholes, a grating catwalk on I-beams and lamps.
- Done locally: `check_mod_data.py` and `check_repository.py` pass, and offline renders of the block models were reviewed.
- Not done: a survival play-test.

## World and event applicability
Not applicable: the blocks are crafted and placed by players only.

## Rollout and open questions
- More shapes could follow if the owner wants them: walls, panes, ladders and doors in these materials.

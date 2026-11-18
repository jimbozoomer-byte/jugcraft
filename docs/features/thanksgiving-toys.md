# Thanksgiving festival toys and lively props (Thanksgiving package, group D)

Status: integrated locally by OpenAI Codex. Compilation, all relevant Thanksgiving server cases in the 118-test combined cohort, six selected client suites and data/art checks pass. Personal Minecraft close-up review and the final HUD-isolation rerun are complete; see [combined acceptance](thanksgiving-craft-package-nov14.md). No publication or live-world change.
Proposal issue: none. The owner's Thanksgiving package request (group D, festival toys and lively props) is the scope approval.
Owner: the project owner (request).

Implemented with Claude Code.
Target milestone and tier: seasonal decoration, no tier; craftable from the first day.
Primary specialty and supported player role: building and decorating (feast rooms, porches, town squares and fairs), and a little play between friends.

The owner's candidate list for this group was first checked against what Jugcraft already has, so nothing is added twice.
This record keeps:
- that inventory;
- what was built for each candidate;
- how it behaves;
- how it was checked.

The pieces are decorations first. Four of them do something small when used. None of them stores items, pays out, trades, gives prizes or takes wagers.

## Player experience

Eight new placeable pieces, all crafted from things a farm grows or a starting player has. None needs research and none is locked to the calendar. Each single-block piece faces the player who places it, breaks quickly with an axe (or by hand) and drops itself once.

- **Corn Husk Dolls.** A pair of faceless dolls of dried corn husk, standing side by side on an oval coiled husk mat:
  - the taller doll in pale husk with an ochre-dyed shawl;
  - the shorter doll in toasted husk with a rust kerchief and a little sheaf of wheat;
  - long husk-leaf skirts flaring in tiers to the floor, dark twine ties at the waist and neck, and arms of rolled husk tied at the wrists;
  - no faces, as these dolls traditionally have none.
- **Turkey Pull Toy.** A carved and painted wooden turkey on a light toy cart (remodelled in Thanksgiving batch 2 after the owner rejected the first model in game):
  - a round bronze body with a breast stepped forward in rounded rings, russet folded wings, a pale blue head with a yellow beak, a red snood and wattle;
  - a broad tail fanned up in two rows of banded feathers (bronze tipped with cream behind, russet and orange in front), each feather turned from one point;
  - a pale maple cart on two through-axles with four round turned wheels (stepped discs) and walnut hub caps;
  - one unbroken pull cord runs from an iron eye on the cart's front edge, down to the floor, into a red bead.
- **Spinning Top.** A weighted top in a clean double cone: red, with one broad cream band round its widest part, an iron tip, a low dome and a walnut stem with a red knob (remodelled in batch 2).
  - At rest it stands upright on its tip (batch 2; it used to lie on its side).
  - Used, it spins on its tip (the frames are a sixteenth of a turn apart), slows, leans and circles on its tip, then comes to rest upright.
  - A spin lasts 86 ticks (about 4.3 seconds) and always ends.
  - Using it while it spins does nothing.
- **Mayflower Model Ship.** A model of a three-masted merchant ship of 1620 on a low stand, laid corner to corner in its block (remodelled in batch 2):
  - a long, low hull of oiled planking, filling out in layers from the keel and narrowing to the bow; its sheer rises a little to the forecastle and most to the stern castle and poop, with a gallery of windows and a rudder;
  - a restrained red band of upper works under a gold cap rail;
  - three evenly spaced masts; the fore and main carry two yards each, square across the keel, the mizzen one, and from each yard hangs a broad cream sail bellied in two steps;
  - a short bowsprit, three thin stays joined at both ends, and a red pennant.
  - Its outline is the block less half a pixel each side ({0.5, 0, 0.5, 15.5, 16, 15.5}); it was 16 by 7 pixels when the ship lay broadside.
- **Harvest Photo Frame Backdrop.** A rustic frame of heavy squared oak, four blocks across and three up (remodelled in batch 2; the same ten blocks, states and protection):
  - two posts on dark sole plates braced behind, a deep lintel running past them under a cap board, a knee brace in each top corner and a low sill; broad, quiet grain;
  - one harvest swag lashed across the middle third of the lintel (two ears of flint corn, pale husks and six broad rust, ochre and olive leaves), a pair of pumpkins at one foot and a hay bale with a white pumpkin and a gourd at the other;
  - its open middle (two blocks wide and two high, above a two-pixel sill) is where two people stand for a picture;
  - using any block of it switches its four bulbs on a cord under the lintel, grey glass when off and amber when lit (light 10 on the two middle blocks of the top rail).
- **Tabletop Corn Toss Set.** A small slanted board of pale maple on a walnut frame and legs, a red band across its front edge and a round hole cut right through it; four rust and four cream beanbags, flat and soft, lie shingled on the floor either side. It is for two players nearby, or one playing both colours. A use tosses the next bag, rust first, then by turns:
  - **Where to stand:** in front of the set, 2 to 5 blocks off.
  - **Where the bag lands:** where you look at the board's height, scattered a little more the further off you stand.
  - **Scoring:** in the hole is 3, on the board is 1. A bag on the board stays there to see.
  - **Who hears it:** everyone within 8 blocks is told each toss and the score.
  - **The round:** after eight tosses the round is over. Sneak and use the set to gather the bags and clear the score; this works at any time.
  - **Nothing at stake:** no wager, prize, reward or item ever changes hands. The score is only words.
- **Prize Pumpkin Display.** A big ribbed show pumpkin on a hessian-draped plinth, with a pumpkin-coloured rosette (rust and ochre) and its entry card on a wire stake.
- **Harvest Percussion Set.** A laced rust drum and a washboard on a bench, with a dried gourd shaker on the floor in front.
  - Use the drum, the washboard or the gourd to play it, aiming at the one you want.
  - Each plays a vanilla note-block sound (bass drum, hat or snare) with a note above it.
  - The instrument shows struck for 4 ticks, and the set cannot be played again until then.

### The inventory: the candidates against existing content

Each candidate was searched for in:
- `git grep` IDs and display names in Java, tools and `en_us.json`;
- the feature records;
- the other Thanksgiving workers' ID claims.

| # | Candidate | Decision | ID, and why |
|---|---|---|---|
| 1 | Corn-husk doll pair | New | `corn_husk_dolls`. `corn_dolly` is the woven wheat (straw) hanging on a wall, plaited in gold. These are standing figures of pale corn husk, with skirts, ties and a kerchief, on a husk mat for a table or shelf. `creepy_doll` is the Halloween doll, and nothing else was found. |
| 2 | Carved turkey pull-toy | New | `turkey_pull_toy`. `turkey_statuette` is a carved bronze figure on a log slice, with no wheels, board or cord. The other turkeys are the mob, `roast_turkey` and `giant_turkey_feast`. |
| 3 | Wooden spinning top | New | `spinning_top`. No top exists; `spinning_wheel` and `herbal_spinning_wheel` are for spinning yarn. |
| 4 | Tabletop Mayflower-style model ship | New | `mayflower_model_ship`. No model ship exists. The ship-like content is vehicles (airships, boats) and `beast_end_pirate_ship_wheel`. |
| 5 | Harvest photo backdrop | New | `harvest_photo_backdrop`. `haunted_archway` is a Halloween stone gateway. Group C's `timber_harvest_arch` is a timber gateway. This piece is a broad timber picture frame with a low sill, a harvest cluster and hanging bulbs, made to stand in for pictures. |
| 6 | Tabletop beanbag / corn toss set | New | `corn_toss_set`. `ring_toss` is the midway's bottle crate, which wins plushes from a loot table; this set gives nothing. `plinko_board`, `prize_wheel` and `bowling_scoreboard` are casino and fair games. |
| 7 | Pumpkin prize rosette / display | New | `prize_pumpkin_display`. The Prize Ribbon Board (`prize_ribbon_board`) hangs the earned blue, red and white ribbons, and is not duplicated. The Giant Vegetable Show has no pumpkin entry, and `giant_pumpkin` is a crop. This is a decorative show plinth whose rosette is pumpkin-coloured, so it never looks like an earned placing. |
| 8 | Harvest music / percussion display | New | `harvest_percussion_set`. `pumpkin_drummer` is a Halloween clockwork animatronic, `singing_pumpkin_*` are tuned voices, and `harvest_bell`, `music_box` and `bone_wind_chimes` are single sounds. No playable percussion stand exists. |

That is eight new blocks, one block entity type (`corn_toss_set`) and no new item beyond each block's own. Nothing was covered, refined or replaced.

## Connections

- **Input producer:** farm crops and simple materials:
  - corn, flax, warty gourds, pumpkins and wheat (hay);
  - sticks, slabs, planks and logs;
  - string, paper, leather, feathers, dyes, gold and iron nuggets, and a torch.
- **Output consumer:** none; these are decorations. The corn toss and percussion set are small social interactions with no reward.
- **Technology and magic connections:** none.
- **Reachable entry path (no circular unlock):** every recipe is a shapeless crafting-table recipe of reachable items. None needs research, and none needs another new piece.
- **Required and optional connections, trade and solo routes:** everything is solo-craftable and optional.
- **How this specialty stays useful without mastering every other branch:** it is decoration and light play for anyone.

## Balance and automation

All recipes are shapeless, follow the agriculture feature switch and are in the building category (`tools/thanksgiving_toys.py` `SHAPELESS`):

| Piece | Ingredients |
|---|---|
| Corn Husk Dolls | 2 corn, string, orange dye |
| Turkey Pull Toy | log, wooden slab, stick, string, feather, red dye |
| Spinning Top | wooden slab, stick, red and yellow dye |
| Mayflower Model Ship | planks, wooden slab, 2 sticks, paper, string |
| Harvest Photo Frame Backdrop | 3 planks, gold nugget, hay bale, pumpkin, corn, leaves, torch |
| Tabletop Corn Toss Set | 2 wooden slabs, stick, 2 corn, red dye, string |
| Prize Pumpkin Display | pumpkin, wooden slab, flax, orange dye, gold nugget, paper |
| Harvest Percussion Set | wooden slab, leather, 2 iron nuggets, warty gourd, stick |

- **No loops:** breaking a piece returns only the piece itself.
- **No overlaps:** a scan of all 14,168 base recipe files found no other shapeless recipe with an identical ingredient set.

**Bounded runtime work:**
- **Top:** one spin is 32 scheduled block ticks (86 game ticks), started only by a use while it lies still.
- **Percussion:** one scheduled tick per stroke, at most one stroke per set every 4 ticks.
- **Corn toss:** at most one toss per set every 10 ticks, and no ticking at all.

No automation: only a player's use does anything.

## Multiplayer, persistence and protection

- **Server authority.** Every change is decided on the server and sent as block state:
  - the top's spin step;
  - the percussion set's struck instrument;
  - the corn toss's bags thrown and bags on the board;
  - the backdrop's lights.

  The corn toss's score is in its block entity and is saved with the world, so a round survives a restart. The cooldown time is not saved, so it is free again after a restart. A toss is worked out from the server's own view of the thrower: their eye position and look, the distance and side, and the cooldown. The client's hit point is never trusted for scoring.
- **Protection.** Everything goes through the normal block-use and block-break paths, so land claims and town protection apply as to any block:
  - in claimed land, only the claim's members can use or break the pieces;
  - in a town, the top, the percussion set and the backdrop have no block entity, so spinning, playing and switching the lights are public interactions (TownProtection's `publicBlock`), like ringing a bell, while breaking them is still protected;
  - the corn toss's block entity keeps it private in a town, to those who may build there. Making it public would need `#jugcraft:town_usable`, which was deliberately left for the owner to decide.
- **The backdrop.** It is placed atomically, from the block aimed at to the placer's right and up:
  - it needs every one of its ten blocks free, and never replaces anything;
  - for a player, it also needs every block to be theirs to build on, so it never reaches into a neighbour's claim or a protected town;
  - breaking any block takes all ten down and drops one item (from its first block's loot table);
  - its two open middle blocks are not part of it at all, so no invisible collision stands in the opening.
- **IDs.** New IDs only; no existing ID or saved state is changed. Disabling the agriculture feature removes the recipes, never the registrations.

## Dependencies and assets

No new dependencies, and no new sounds: the percussion set uses vanilla note-block sounds and the toys use vanilla wood and wool sounds.

All textures are drawn by code in `tools/thanksgiving_toys_data.py`: one packed texture per piece (64 or 128 square), one texel per model pixel, three to five tones per material, lit from the top left. The pieces share palettes and painters with `tools/thanksgiving_decor_data.py`, the Harvest Festival Grounds and the Pumpkin Patch Market:
- the pumpkin, leaf, garland and willow painters;
- the husk, twine, hessian, cream, wood and iron palettes.

No Mojang texture is read.

The owner library catalog (`art/owner-library/catalog/files.csv`) was searched for:
- husk, doll, toy, turkey;
- top, ship, sail, galleon;
- photo and frame;
- beanbag and cornhole;
- rosette, ribbon and prize;
- drum, washboard, shaker and percussion.

It holds only a husk mob's loot data, gun ammunition drums, gun and magic frame textures, and a `toy_squeak.ogg` squeak. None fits these pieces, so nothing was copied from it.

The corn husk dolls follow a traditional North American folk craft. They are inspired game decor, not a reconstruction of any particular tradition's dolls, and carry no ceremonial meaning.

**Generated resources:** block and item models, blockstates (the corn toss's is multipart), items, loot tables, recipes, names, hints, messages, the `mineable/axe` tag and textures. They come from `tools/thanksgiving_toys_data.py`, hooked into the agriculture pipeline (`agriculture_data.py`, `crop_textures.py`) or run on its own (`python3 tools/thanksgiving_toys_data.py write`).

**JEI:** the recipes show through JEI's normal recipe registration. The interactive pieces' items carry a grey hint line (`item.jugcraft.<id>.tooltip`), which JEI shows with the item. As with the other decorations, there are no JEI information pages.

## Verification

### Run here (offline; no Minecraft or Fabric toolchain)

- `python3 tools/thanksgiving_toys_data.py write`: the generator writes every file.
- `python3 tools/thanksgiving_toys_check.py`: the generated files on disk are exactly what the generator makes now. It also compares the Java numbers with the table, and checks the drops, tags, names, recipes, model bounds and outlines, the backdrop's open middle, and the corn toss's hole and board against the model.
- `scripts/tests/test_thanksgiving_toys.py`, with Python unittest.
- The coordinator's shared renderer report: texel density, UVs inside the sprite, transparent faces, coplanar faces and missing textures, on every model of the eight blocks.
- Tree-sitter syntax parse of every new Java file. This is a syntax parse only, not a compile.

The delivery notes give the exact outputs.

### Previews

Generated previews of the exported model JSON, rendered with back faces culled and real textures (not in-game renders):
- a gallery for every piece, with every facing and a scale strip;
- the top's key poses;
- the corn toss's states;
- the percussion set's struck poses;
- the backdrop from every side, lit and unlit.

### Written, not run in Minecraft

`FestivalToyGameTests`, the server game tests, cover:
- placement, facing and one drop for every single-block piece;
- the top's spin: it steps down one frame at a time to resting still in its fixed time, ignores a use while it spins, and leaves nothing scheduled;
- the percussion set: it plays the instrument aimed at, rests for its cooldown, and its aim turns with its facing;
- the corn toss: tosses scored by turns, refused when too close, too far, behind, too soon or after the round, a use tossing one bag at a time, and a sneak-use gathering the bags with nothing ever given;
- the backdrop: assembly round an open middle with a two-pixel sill, light switching, teardown with one drop, refusal on blocked ground with no orphan parts, and refusal to reach into another player's claim;
- claimed land: outsiders cannot use or break the pieces, while the owner can;
- every recipe and loot table loads.

### Not run

- Java compilation;
- server and client game tests;
- in-game visual review;
- two-client play.

These must pass before release.

## World and event applicability

The pieces are seasonal in theme only:
- nothing is gated on the calendar;
- nothing spawns or changes in the world;
- nothing is placed in any town or template, and no owner placement is touched.

## Rollout and open questions

- Release needs a compile, `./gradlew build` (the server game tests) and a client look at the new models in the game.
- Implemented locally: the Corn Toss Set is a public non-container town amenity via one `#jugcraft:town_usable` entry. Private claims still restrict outsiders. Backdrop teardown, creative breaking, explosions and fluid replacement have focused regressions; results are in the combined November 14 record.
- The essence values for the new items are explicit recipe-derived records in `data/jugcraft/alchemy/values.json` (the economy audit of 14 November: each item's one recipe costed by `tools/build_alchemy_values.py`'s formula; `scripts/tests/test_thanksgiving_economy.py`). A full `tools/build_alchemy_values.py` run against the game JAR should reproduce them.

## November 14 remodel checkpoint

The five owner-rejected pieces have revised models and materials. Generated previews have been reviewed by Codex; this is approval to proceed to runtime inspection, not owner acceptance or a completed in-game visual check. Only the ship outline changes. Local interaction and fluid fixes remain intact. Checkpoint 2 also makes the resting top upright on its tip; combined validation remains pending.

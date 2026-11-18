# Thanksgiving harvest town details (group C)

Status: integrated locally by OpenAI Codex. Compilation, all relevant Thanksgiving server cases in the 118-test combined cohort, six selected client suites and data/art checks pass. Personal Minecraft close-up review and the final HUD-isolation rerun are complete; see [combined acceptance](thanksgiving-craft-package-nov14.md). No publication or live-world change.
Proposal issue: none. The owner's Thanksgiving batch request (group C, harvest town details) is the scope approval.
Owner: the project owner (request).

Implemented with Claude Code.
Target milestone and tier: seasonal decoration, no tier; craftable from the first day.
Primary specialty and supported player role: building and decorating (a town gateway, a farmhouse kitchen, the feast
table, the market).

The owner's Thanksgiving batch listed eight harvest-town candidates. Each was first checked against what Jugcraft already
has, by registry ID, display name, lang key, generator and Java class. The aim was to build nothing twice. This record
holds that inventory, what was built for the gaps, and how it was checked.

## The inventory

| # | Candidate | Decision | ID and reason |
|---|---|---|---|
| 1 | Timber harvest entrance arch | New | `timber_harvest_arch`. Existing gateways are graveyard or Halloween pieces: `haunted_archway` (mossy stone, iron, skulls), `lych_gate` and `cemetery_gateway` (graveyard buildings), and `corn_maze_gate` (a maze generator). None is a timber harvest gateway. It uses the existing multiblock helper (`MultiDecorationBlock`, as the Haunted Archway and the Giant Cornucopia do). |
| 2 | Stocked covered farm stall / canopy kit | Covered | `farm_stand` is already a covered roadside stall: a striped orange-and-cream awning on four posts and six crates that show their stock. It is a working shop: its owner stocks it and prices goods in Jugs. Stocked decorative displays exist as `pumpkin_wagon` (28 produce), `pumpkin_pyramid`, `pumpkin_wheelbarrow` and the produce crates. A canopy kit exists as `supps_awning`, in 16 colours, slanted or flat, and waterloggable. A decorative stall would duplicate the Farm Stand's look; a second shop would duplicate its economy. |
| 3 | Hanging earthenware and herb kitchen rack | New | `hanging_crock_rack`, which hangs from a ceiling. `drying_rack` is a freestanding floor station that dries herbs. Herb bundles (`basil_bundle` etc.) are single working bunches that dry under a block. `copper_pan_rack` is a wall rack of copper pans; `spice_rack`, `broom_rack` and `coat_rack` are other things. None is a ceiling rack of stoneware and earthenware with an onion string. Its dried herbs are fixed decoration; it dries nothing. |
| 4 | Folded linen stack | New | `folded_linen_stack`. `quilt_rail` (a quilt aired over a rail), `blanket_ladder` (carpets hung as throws) and the Feast Table cloths (laid on tables) are not folded linen set down. |
| 5 | Copper warming dish / chafing stand | New | `copper_chafing_dish`. No chafing dish, warmer or tureen exists. `copper_pan_rack` is pans on a wall, and the lightworks copper pieces are candle holders. Its burner uses the existing candle mechanism (`CandleCenterpieceBlock`, `CandleLighting`). |
| 6 | Pumpkin-carving workbench display | New | `pumpkin_carving_bench`. Carving exists as a mechanic on placed pumpkins (`CarvedPumpkinBlock`, `carving_knife`, the stencils), and `judging_stand` is the contest. There is no bench showing carving in progress. It is decoration: pumpkins are still carved where they stand. |
| 7 | Autumn window box | Refine | `window_box` (with `half_barrel_planter` and `galvanized_tub_planter`), refined in place. The planters already take the fall's mums (`yellow_mum`, `orange_mum`, `red_mum`, `purple_mum`), `aster`, `sedum` and `ornamental_kale` through `#minecraft:small_flowers`. They now also take a `warty_gourd`, through the new item tag `jugcraft:planter_harvest` (`PlanterBlock.HARVEST`), on the same mechanism (`PlanterBlockEntity`, `PlanterRenderer`). There is no new block. The window box's own model is unchanged, so window boxes already placed in towns look the same. The first plan was an `autumn_window_box` ID; it was withdrawn on review in favour of the existing mechanism. |
| 8 | Feast invitation / wayfinding signpost | Covered | `supps_way_sign`, the Direction Signpost: two boards on any fence or wall post, each turned separately, with text the player edits, dye colours, glow ink and wax. `chalkboard_sign` chalks any words from a renamed sign or Name Tag (for example "HARVEST FEAST TONIGHT"), and `harvest_market_sign` is the pictorial market sign. Building another sign system would duplicate them. |

Five new blocks and one refinement; two candidates are covered by existing content.

The planter harvest tag holds only the Warty Gourd, for these reasons:
- **The Warty Gourd fits.** Its own 8-pixel model, drawn at each planter's scale, fits inside every planter (a test checks this).
- **Squash and pumpkins don't.** Larger squash and whole pumpkins would stand out of the Window Box and through its wall.
- **Bittersweet draws nothing.** It is a wall vine whose default state is empty.
- **Wheat and corn have no block model to draw.** They are not blocks.

## Player experience

All five new pieces are player-placed decorations, crafted from early farm materials, with no research and no calendar
gate. Each breaks quickly with its tool, or by hand, and drops itself once.

### Timber Harvest Arch

A farm gateway of hewn oak, five blocks across and four up (3.6 m wide, 3.3 m to the top of its beam).
- **Its posts:** two hewn oak posts on fieldstone plinths, each with dried corn stalks tied against its outer side, two ears of ornamental corn hung on its front, and a pumpkin and a gourd at its foot.
- **Its beam:** a pegged oak beam on knee braces, with a board along its top.
- **On top:** a wheat sheaf bound in twine between an orange pumpkin and a white one.
- **Its garland:** autumn leaves in two swags along the front with red bows; a band of leaves along the back.
- **Placing it:**
  - It goes up from the block aimed at (its left post, as the player sees it) to the right, and faces the player.
  - Placing is all or nothing. It is refused, and nothing changes, if any of its eleven blocks:
    - is not free (only replaceable blocks such as air or short grass), or holds water or another fluid;
    - is unloaded, or has an entity in the way;
    - is somewhere the placer may not build (land claims, town protection).
  - It is also refused unless both posts stand on firm ground.
- **The opening:** three blocks across and three high, with no block in it at all. People and carts walk straight through; the beam over it starts above three blocks.
- **Breaking it:**
  - Breaking any one of its blocks brings the whole arch down, and it drops once.
  - A survival player who may not build at every one of its blocks makes no progress breaking it, so a claim holding any of its blocks protects the whole arch.
  - Pistons don't move it.
- **Using it:** it has nothing to use.

### Hanging Crock and Herb Rack

A kitchen rack hung under a ceiling: two oak rails and three turned rungs on four hemp ropes from iron eyes. From it hang:
- a salt-glazed stoneware jug (cream belly, brown-slipped shoulder) by its handle;
- a lidded red earthenware crock with a cream slip band, on its wire bail;
- a string of onions and garlic on a straw plait;
- a bunch of dried sage and one of thyme, each five sprigs fanned from a twine tie.

It needs a block above it (anything a lantern could hang from) and falls when that goes. Its outline turns with the way it
faces, and nothing bumps into it. It is decoration: it dries nothing (herb bundles and the Drying Rack do that).

### Folded Linen Stack

The feast's table linen, folded and stacked a little askew:
- the stack: cream linen, a mustard cloth, red gingham, rust napkins, cream linen again and a hessian runner;
- the tie: crosswise twine knotted on top over a wheat sprig and a maple leaf;
- beside it, a short pile of napkins.

### Copper Chafing Dish

An oval copper pan with a brass rim on a wrought-iron stand, its domed lid stepped up to a brass knob, with brass loop handles.
- **The burner:** a brass spirit burner stands under the pan on a shelf the legs pass through.
- **Lighting it:** flint and steel or a fire charge lights the burner. Its banded flame shows under the pan, and the dish gives light 7. An empty hand snuffs it, as on the Pumpkin Candle Centerpiece.
- **Protection:** in a protected town, the town's public-use rule lets visitors snuff it but not light it, the same as the centerpiece.

### Pumpkin Carving Bench

A low plank bench with a pumpkin half carved on it:
- **The pumpkin:** its lid is off and lies beside it with its stalk. Two eyes and half its mouth are cut through its front wall, with the dark hollow behind them, and the knife is stuck in at the end of the mouth.
- **On the bench:**
  - a scoop;
  - a candle stub waiting to go inside;
  - a wooden bowl of scooped guts and seeds;
  - curls of shaved rind.
- **Under it:** a pumpkin still waiting, and a shaving fallen on the floor.

### Autumn window boxes (refined Window Box)

Plant mums, asters, sedum or ornamental kale, and now a Warty Gourd, in a Window Box (or a Half-Barrel or Galvanized Tub),
one to a spot. An empty hand takes back the one planted last, as before.

## Connections
- **Inputs (existing):** logs, planks, slabs, sticks, string, clay, copper and iron, candles, a bowl, pumpkins, corn, onions, Dried Herbs, flax, wheat, dyes and leaves.
- **Output consumer:** none; they are decorations. The planter tag extends the existing planters.
- **Technology or magic connection:** none (copper ingots and iron nuggets only as materials).
- **Reachable entry path:**
  - every recipe is a shapeless crafting-table recipe of reachable items, under the agriculture switch;
  - none needs research, the calendar or another new piece.
- **Trade and solo routes:** all solo-craftable.

## Balance and automation
Recipes (`tools/thanksgiving_town.py` `SHAPELESS`):

| Piece | Ingredients |
|---|---|
| Timber Harvest Arch | four logs, two corn, a pumpkin, leaves and string |
| Hanging Crock and Herb Rack | a wooden slab, two sticks, string, two clay balls, an onion and Dried Herbs |
| Folded Linen Stack | three flax, red and orange dye, string and wheat |
| Copper Chafing Dish | three copper ingots, two iron nuggets and a candle |
| Pumpkin Carving Bench | two wooden slabs, two sticks, a pumpkin, an iron nugget and a bowl |

Nothing is returned on breaking but the block itself, so there is no conversion loop. Nothing ticks, and there are no
block entities beyond the planters' existing one. The dish's `lit` is a block state changed only by use.

## Multiplayer and persistence
- **Saved state:**
  - **The new pieces** have only plain block states: facing; lit on the dish; facing, lit and part on the arch. They are new IDs only.
  - **The planters** save what they hold by item, as before. Gourds are kept through a reload because they are plantable, and saved flowers are unchanged.
- **Protection:**
  - **Use:** placing, lighting, planting and breaking go through the normal block-use and break paths, so land and town protection apply.
  - **The arch's placement:** it checks claims and town protection at each of its eleven blocks.
  - **The arch's breaking:** a survival player must be able to build at all of them.
- **Disabling the feature:** turning agriculture off removes the recipes, never the registrations.

## Dependencies and assets
No new dependencies. All textures are drawn by code in `tools/thanksgiving_town_data.py`: one packed texture per piece, a
texel to a model pixel, three to five tones per material, lit from the top left, patterns rather than noise.

**Reused from existing generators:**
- the Thanksgiving decorations' palettes and painters: pumpkins, leaves, linen, napkin, gingham, copper, flame, wax and walnut;
- the Harvest Festival Grounds' corn, husk, wheat, kernels, ears, twine and brass;
- the Pumpkin Patch Market's planks, straps, turned wood and iron;
- the fall garden's planter mechanism.

**Palettes read from the owner's own drawings.** These are in `art/owner-library/originals/Blocks/farming and food
textures/` and were read as data; nothing was copied:

| Drawing | Palette |
|---|---|
| `onion.png` | the onions and their shoots |
| `rope.png` | the rack's ropes |
| `iron_knife.png` | the carving knife's blade and handle |
| `pumpkin_slice.png` | the cut pumpkin flesh |
| `cutting_board.png` | the bench's boards |

The [owner library catalog](../../art/owner-library/catalog/README.md) was searched for:
- arches and gates, stalls, canopies and awnings, hanging racks, crocks, jugs and pottery;
- linen, napkins, chafing dishes and warming trays;
- carving, window boxes, planters and signs.

It has canvas signs (Covered by the existing signs), a grey heating tray and cooking pots (not copper, not chafing dishes),
and the food textures above. It has no model of any of these pieces to adapt.

**Generated resources (these pieces only):**
- for each piece: its block and item models, blockstate, item, loot table, recipe and texture;
- the arch's eleven part models;
- the item tag `jugcraft:planter_harvest`.

The shared names and mining tags are merged by the agriculture pipeline hooks.

## Verification
Run here (offline; no Minecraft and no Fabric toolchain):

| Check | Result |
|---|---|
| `python3 tools/thanksgiving_town_data.py` | Regenerates every file these pieces own; a second run changes nothing. |
| `tools/thanksgiving_town_check.py` `problems()` | Pass. It checks registration text against the table, names, loot, mining tags, the planter tag, blockstate coverage, recipe switches, model bounds against outlines, and byte-for-byte regeneration. |
| `tools/thanksgiving_town_check.py` `art_problems()` | No finding. This runs `tools/art_check.py`'s I1, U1, Z1, N1 and O1 rules on every new model with none of its allow-lists, and X1 and X2 on the arch's eleven parts at their places. |
| `python3 -m unittest scripts.tests.test_thanksgiving_town` | 6 tests, OK. |
| The shared gallery renderer's `report` | Every face between 0.8 and 1.6 texels per pixel; no UVs outside the sprite; no transparent faces, no z-fighting, no missing or vanilla textures. |
| Tree-sitter Java parse of the new and changed Java files | 0 errors. This is a syntax parse only, not a compile. |
| The five patches applied in order to the base | They reproduce the branch's tree exactly. |

The renders are galleries of every piece from every side, in all facings and states, with a scale strip. The arch was
rendered assembled in all four facings. They are generated previews, not in-game renders.

The game tests `HarvestTownGameTests` are written, not run in Minecraft. They cover:
- placement, facing, outline and single drops of the floor pieces;
- the dish lit and snuffed through the normal use path;
- the rack hanging, refusing the floor, its turning outline and its fall;
- the arch:
  - assembly in all four facings, part positions, and the empty opening (a player-sized box fits through it, and the beam over it starts above three blocks);
  - refusals with a block, water or no ground in the way;
  - teardown from four different parts, each dropping one arch and leaving no orphan part;
  - a claim refusing placement and blocking a non-member's breaking;
- the planters taking a Warty Gourd beside their small flowers but not wheat, and keeping both through a save and load.

**Not runnable here:** `tools/check_mod_data.py` imports owner-library files this sparse checkout lacks. Its new
`check_thanksgiving_town()` only calls the module check above, which passes. `scripts/check_repository.py` reports only
links into folders the sparse checkout leaves out.

**Not run:** Java compilation, server and client game tests, in-game visual review, and two-client play. These must pass
before release.

## World and event applicability
These pieces are seasonal in theme only:
- nothing is gated on the calendar;
- nothing spawns or changes in the world;
- no city template, town decoration schedule or owner placement is touched;
- the existing window boxes in towns keep their look and contents.

## Rollout and open questions
- Release needs a compile, `./gradlew build` with the server game tests, and a client look at the models in the game.
- Alchemy essence values for the new items are explicit recipe-derived records in `data/jugcraft/alchemy/values.json` (the economy audit of 14 November: each item's one recipe costed by `tools/build_alchemy_values.py`'s formula; `scripts/tests/test_thanksgiving_economy.py`). A full `tools/build_alchemy_values.py` run against the game JAR should reproduce them.
- **Fixed locally:** the shared before-break guard checks the entire arch footprint for survival and non-OP creative users; explosions respect any protected part. Arch structural blocks resist water/lava replacement while the passage remains genuinely empty. Combined runtime validation is recorded in the November 14 package ledger.

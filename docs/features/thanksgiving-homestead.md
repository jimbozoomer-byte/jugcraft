# Thanksgiving homestead and feast room

Status: integrated locally by OpenAI Codex. Compilation, all relevant Thanksgiving server cases in the 118-test combined cohort, six selected client suites and data/art checks pass. Personal Minecraft close-up review and the final HUD-isolation rerun are complete; see [combined acceptance](thanksgiving-craft-package-nov14.md). No publication or live-world change.
Proposal issue: none; the owner's Thanksgiving package request (group A, the pilgrim homestead and feast room) is the
scope approval.
Owner: the project owner (request).

Implemented with Claude Code.
Target milestone and tier: seasonal decoration, no tier; craftable from the first day.
Primary specialty and supported player role: building and decorating (a colonial keeping room beside the feast table).

The owner listed eight homestead and feast-room pieces and asked that each first be checked against what Jugcraft
already has. This record is that inventory, what was built or refined, and how it was checked. The festival's feast
table and its table props are in [thanksgiving-decor.md](thanksgiving-decor.md); nothing in a city, town template or
owner placement is changed here.

## Player experience

Six new placeable pieces and two refined ones, all crafted from things a starting player has (planks, slabs, wool,
flax, tin, clay, string, dye, a candle), with no research and no calendar gate. Each faces the player who places it.

- **High-Backed Oak Settle** (two blocks across, two up): a panelled oak box settle with a hood and a carved crest, two
  russet squabs. Use either half (its seat or the back above it) with an empty hand to sit on that half's squab; two
  players can sit side by side, one a seat; sneak to get up, or to use it without sitting.
- **Carved Dish Dresser** (two across, two up): an oak pot-board dresser, its shelves holding modelled pewter chargers,
  plates on edge, tankards, a jug, a porringer, bowls and cups, its pot board crocks and a stoneware jug. Decoration
  only: it opens nothing and stores nothing.
- **Pilgrim Peg Rail** (on a wall): a Shaker peg rail with a linen coif, a free peg, and a russet cloak hung by its hood
  with a tall black felt hat over it (a plain band: the buckled pilgrim hat is a later myth).
- **Pewter Pitcher and Basin**: a footed pewter wash basin with its pitcher standing in it and a striped linen towel.
- **Pewter Chamberstick**: flint and steel or a fire charge lights its candle (light 10); an empty hand snuffs it. Its
  cone snuffer lies in the pan.
- **Embroidered Harvest Sampler** (on a wall): a cross-stitched linen sampler (border, A B C letter band, wheat
  sheaves) with a raised pumpkin, in a walnut frame with real depth.
- **Quilt Rail** (refined): the patchwork quilt over the rail now falls in pleats to an uneven, bound hem, with a
  restrained running stitch on its joins and binding.
- **Butter Churn** (refined): the same coopered body and working churn, with a rimmed lid, a centre hole and splash cup
  the dasher works through, and a turned dasher with ferrule, grip and knob.

Two-block pieces place only where all four blocks are free (they never replace a block), break as one from any block
and drop once. Wall pieces hang on the side of a solid block and fall when it goes.

### The inventory: the owner's list against existing content

Searched by ID, display name, lang key, generator and Java (`git grep` at base `8075c6d58b`), and in the owner library
catalog (`art/owner-library/catalog/files.csv`: settle, dresser, pewter, pitcher, ewer, candlestick, chamberstick,
sampler, embroider, quilt, churn, peg rail, bonnet, coif, pilgrim, cloak; nothing suitable at this scale).

| # | Candidate | Decision | ID, and why |
|---|---|---|---|
| 1 | High-backed oak settle | New | `oak_settle`. Benches exist (`crafted_*_bench`, `crafted_*_dining_bench`, `homeworks_*_bench`, `memorial_bench`, the sofas, `porch_swing`), but none is a high-backed settle: they are low-backed benches, a gravestone bench or a swing. Two blocks across and two up so the back stands about 1.8 m, as a settle's does; within one block it would be no higher than an ordinary bench. |
| 2 | Carved dish dresser | New | `carved_dish_dresser`. Cabinets, cupboards and shelves exist (`*_cabinet`, `crafted_*_cupboard`, `pantry_shelf` with its jars, `apothecary_shelf`), none a plate-rack dresser with dishes on it. Decoration: no storage block fits a rack of plates on show, so it holds nothing (no secret storage). |
| 3 | Wall peg rail with hat, coif, cloak | New | `pilgrim_peg_rail`. `coat_rack` is the power plant's office rack; `supps_hat_stand` is an armour-stand-like entity; no wall peg rail with clothes. Muted russet, linen and black felt; no buckle on the hat. |
| 4 | Pewter pitcher and basin | New | `pewter_pitcher_and_basin`. `household_*_basin` are kitchen sinks; nothing pewter. |
| 5 | Chamber candleholder with snuffer | New | `pewter_chamberstick`. The lightworks low/double/triple candle holders and `supps_candle_holder*` are owner-library holders without a pan handle or snuffer; uses `CandleCenterpieceBlock`/`CandleLighting` as the Pumpkin Candle Centerpiece does. |
| 6 | Embroidered harvest wall sampler | New | `harvest_sampler`. `framed_print` and `large_print` are pictures; nothing stitched. |
| 7 | Quilt over a wooden rail | Refine | `quilt_rail` (Fall Garden & Porch). Same ID, shape, `facing` states, recipe and loot; its model and texture are redrawn: pleats standing at different depths, a thicker fold over the rail, uneven hems with a thicker rust binding, post caps, a restrained running stitch. |
| 8 | Butter churn | Refine | `butter_churn` (Barn & Harvest Tools). Same ID, shape, states (`facing`, `full`, `runner`), recipe, loot and working (ButterChurnBlock and its block entity unchanged; it still churns milk into butter). Its coopered body is the original's box for box; the lid becomes a rimmed lid with a centre hole and splash cup, and the dasher a turned handle with ferrule, grip and knob, clear of the cup through its whole stroke. |

6 new blocks; 2 existing blocks refined in place; nothing covered or replaced.

## Connections
- Existing input producer: planks and slabs, wool, flax, tin ingots and nuggets, clay, string, dyes, a candle.
- Existing output consumer: none; they are decorations. The settle is a seat (Seat); the chamberstick is a light.
- Technology connection: tin (for pewter) is an early metal; no machine needed.
- Magic connection: none.
- Reachable entry path: every recipe is a shapeless crafting-table recipe of reachable items; none needs research or
  another new piece.
- Which connections are required vs optional; trade and solo routes: all solo-craftable.
- How this specialty stays useful without mastering every other branch: decoration only.

## Balance and automation
Recipes (shapeless, agriculture feature switch, category building; `tools/thanksgiving_homestead.py` `SHAPELESS`):
the settle four oak planks, two oak slabs and wool; the dresser three planks, two slabs, a tin ingot and two clay; the
peg rail a slab, a stick, black wool, wool and flax; the pitcher and basin two tin ingots, a tin nugget and flax; the
chamberstick a tin ingot, a tin nugget and a candle; the sampler four sticks, flax, string, orange and green dye.
Breaking returns only the piece itself, so there is no conversion loop. No ticking, no block entities (the two-block
pieces' parts, the chamberstick's `lit` and the furniture's unused `lit` are block states, changed only by placing,
use and breaking). The churn's working is unchanged.

## Multiplayer and persistence
Plain block states; no saved data beyond them. New IDs only; no existing ID or saved state is renamed or removed (the
refined pieces keep every state). The furniture keeps MultiDecorationBlock's `lit` state, which it never changes.
Placing, sitting, lighting and breaking go through the normal block-use and break paths, so land protection applies:
in someone else's claim a stranger can neither sit, light nor break (as with the Hay Bale Seat and the Porch Swing;
`LandProtection` has no per-block public-use list, and none was added). Disabling the agriculture feature removes the
recipes, never the registrations.

## Dependencies and assets
No new dependencies. Every texture is drawn by code in `tools/thanksgiving_homestead_data.py`, one packed texture a
piece, one texel to a model pixel, three to five tones a material lit from the top left, patterns and never noise; it
shares the Thanksgiving decorations' wax, flame, china, linen and walnut palettes and candle, the Pumpkin Patch
Market's plank, strap and turned-wood painters, and the Fall Garden quilt's fabrics. No Mojang texture is read and
nothing from the owner library is used (the catalog search above found nothing at this scale).

Generated resources: block and item models, blockstates, items, loot tables, recipes and textures through the
agriculture pipeline (`agriculture.py`, `agriculture_data.py`, `crop_textures.py`); the refined pieces' models and
textures through their own builders (`fall_garden_data.py`, `barn_tools_data.py`), which now take them from
`thanksgiving_homestead_data.py`. `thanksgiving_homestead_data.generate()` writes this group's files alone, exactly as
the pipeline does (checked against the pipeline's writer on the original builders: identical).

## Verification
Run in this environment (offline; no Minecraft, no Fabric toolchain):
- `scripts/tests/test_thanksgiving_homestead.py` (14 tests, pass): every generated file reproduced byte for byte (and
  textures pixel for pixel); blockstates cover every state; the furniture's 2 x 2 x 1 footprint and four part states,
  each part model inside its block; seat height equals the squab top; drops once (multi-block only from part 0);
  recipes from the table; outlines cover the models; art_check's Z1, N1, U1, O1 and I1 on every model and X1/X2 across
  the multi-block seams; opaque texels, one texel to a pixel (0.8 to 1.6); the Java registration matches the table;
  names, mining tags, the game-test entry and the resource manifest; no helper relies on an unlisted tag; the refined
  pieces keep their states, recipe and the churn's dasher clearance.
- The coordinator's gallery renderer (`report`): no missing textures, no UVs outside 0..16, no transparent faces, no
  same-plane overlaps, texel density 0.83 to 1.6.
- The Java files parse (tree-sitter syntax parse only; not compiled).
- Offline galleries of every piece from six sides, all four facings, every state, beside a 16-texel cube and the Pie
  Stand; the refined pieces beside their base versions; the churn assembled with its dasher at rest and at the bottom
  of its stroke. These are generated previews, not in-game renders.

Written, not run in Minecraft: `ThanksgivingHomesteadGameTests` (furniture placed whole in every facing and broken as one
from each part with one drop; never placed over a block; the settle seats two at the squab top, one a seat, not when
sneaking, and stands them up when broken; the dresser does nothing when used; floor pieces face their placer and drop
themselves; the chamberstick lights with flint and steel and a fire charge and snuffs; wall pieces hang only on a wall,
drop themselves and fall with it; in a claim a stranger can neither sit, light nor break).

Not run: Java compilation, server and client game tests, in-game visual review, two-client play. These must pass
before release.

## World and event applicability
Seasonal in theme only: nothing is gated on the calendar, nothing spawns or changes in the world, and earned pieces
keep working all year. No city block, town template or owner placement is changed.

## Rollout and open questions
- Release needs a compile, `./gradlew build` (server game tests) and a client look at the new models in the game.
- Alchemy essence values for the new items are explicit recipe-derived records in `data/jugcraft/alchemy/values.json` (the economy audit of 14 November: each item's one recipe costed by `tools/build_alchemy_values.py`'s formula; `scripts/tests/test_thanksgiving_economy.py`). A full `tools/build_alchemy_values.py` run against the game JAR should reproduce them.
- Whether sitting on a settle or lighting a chamberstick in someone's claim should be public is a protection-policy
  choice for the owner; today every block use in a claim needs build access.
- Resolved locally: each settle half has one canonical seat for players and companions. Breaking any part releases reservations and ejects sitters immediately. The shared multi-block guard protects the full footprint, including creative breaking and explosions. Local validation is tracked in [the combined record](thanksgiving-craft-package-nov14.md).

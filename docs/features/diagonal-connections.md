# Diagonal connections

Status: fences, panes and bars merged in #138; walls in review. Green in CI with screenshots; **not yet played** by a person. Results are under Verification.
Proposal issue: none. The owner asked for it on 3 October 2026 (see "Owner request" below).
Owner: @jimbozoomer-byte
Target milestone and tier: building, from the first day (no tier).
Primary specialty and supported player role: builders and decorators of every branch.

## Owner request
- "begin making the framework for fences and similar objects to connect diagonally similarly to how this mod does it but I want it to work with any blocks that connect like iron bars, graveyard fence etc etc", with a link to the Diagonal Fences mod and its screenshot of fences running at 45 degrees.
- Then, once the framework was merged: "merge it, then start on walls".
- Nothing is taken from that mod. The rule, the code, the models and the tests here are Jugcraft's own; the mod was only the idea.

## Player experience
- **Fences, glass panes, bars and walls join diagonally.** Place two of a kind a diagonal step apart and a rail (or a pane, bars or a wall) runs between them at 45 degrees, so fences and walls can follow a slope, turn a corner at an angle or draw a diamond.
- **Which blocks:**
  - all of vanilla's fences (13 wooden and the nether brick fence);
  - glass and the 16 stained glass panes;
  - iron bars and the 8 copper bars, waxed or not;
  - all 32 of vanilla's walls (cobblestone, the stone, brick, deepslate, blackstone, tuff, cinnabar and sulfur families, and the rest);
  - all 14 of Jugcraft's fences: the 13 wood-set fences and the wrought-iron cemetery fence (the graveyard fence).
- **The rule, the same from either end.** Two blocks a diagonal step apart join when they would join side by side, and neither joins straight into the two blocks between them:
  - a wooden fence joins any wooden fence; the nether brick fence and the cemetery fence join each other but not wooden fences, as they do straight; panes and bars all join each other; any wall joins any wall;
  - fences, panes and bars, and walls, never join each other's kind diagonally (a wall joins bars straight, but not on the diagonal);
  - a straight corner comes first. Fences that already meet round a corner, or a solid block in the corner, keep the diagonal from forming, so two diagonals never cross.
- **Walls: low arms, and posts as vanilla raises them.**
  - A wall's diagonal arm is a low wall side (14 pixels high), turned 45 degrees.
  - A wall with no straight sides and two opposite diagonals runs straight on, so it has no post, as a straight wall has none. Something above calls for a post as it does on a straight low wall: a torch or other post-raising block, a block over the middle, or a wall above with a post.
  - Any other wall with diagonals is an end, a corner or a junction, and keeps its post.
  - Diagonal arms never rise to the tall height. A straight side rises 2 pixels to meet a wall or block above it; a diagonal does not, so a diagonal wall two high shows a 2-pixel slot between its layers (see "Rollout and open questions").
- **Live updates.** Placing, breaking or changing a block updates its diagonal partners at once. Fill in the corner and the diagonal goes; clear it and the diagonal comes back.
- **Shapes.** The diagonal arm is part of the block's outline and collision. A fence's or wall's diagonal blocks players and mobs as its straight sides do, and is as high (one and a half blocks).
- **Structures.** Rotating or mirroring a structure (the structure block, templates) turns the diagonals with it.
- **Switch:** `diagonal_connections.enabled=false` stops new diagonal joins; existing ones go when their blocks next update. Vanilla shapes and models stay as they were for every block without a diagonal.

## Connections
- Existing input producer: none needed; the blocks are vanilla's and Jugcraft's own fences, panes, bars and walls.
- Existing output consumer: building. The walled town's fences and walls keep their exact layout: its builder places blocks without shape updates.
- Technology connection: none.
- Magic connection: none.
- Reachable entry path (prove no circular unlock): every block that joins is craftable without any Jugcraft progression.
- Which connections are required vs optional; trade and solo routes: optional and solo.
- How this specialty stays useful without mastering every other branch: it is a building aid for everyone.
- For infrastructure/cosmetics, supported systems and reason resource links do not apply: it changes no recipe, resource or progression; it is a block shape and model behaviour.

## How it works (the framework)
- **Properties.** Every `FenceBlock`, `IronBarsBlock` (glass panes are bars too) and `WallBlock`, including other mods', gets four more boolean properties: `north_east`, `south_east`, `south_west` and `north_west`, all false by default (`mixin/DiagonalStateMixin`, `mixin/DiagonalBlockMixin`).
- **Who joins.** Only blocks in the block tag `#jugcraft:connects_diagonally` ever set them. Every block in it has a diagonal arm model, so nothing joins without an arm to show. Today the tag has 86 blocks: vanilla's 72 (40 fences, panes and bars, 32 walls) and Jugcraft's 14 fences.
- **Adding a block.** A new Jugcraft fence, pane, bars block or wall joins diagonally once it is added to `tools/diagonal_connections.py`. The generator gives it an arm model and its four blockstate parts. The data checker fails if a Jugcraft blockstate shaped like a fence (a part for each straight side, none up or down) or like a wall (a low or tall part for each straight side) is missing from the tag. Another mod's block can join by adding itself to the tag and providing the models.
- **Updates.** `diagonal/DiagonalConnections` works out the diagonals wherever the block works out its straight connections: on placement and on each neighbour's shape update. Diagonal neighbours get no shape updates from the game, so a fence, bars block or wall also asks its four diagonal neighbours to look again whenever it is set. It does this through `updateIndirectNeighbourShapes`, the hook the game uses for redstone dust's diagonal neighbours. Blocks set with `UPDATE_KNOWN_SHAPE` (world generation, the town builder) are left as placed.
- **Wall posts.** A wall that has, or has just lost, diagonals has its post worked out again by vanilla's own rule (`WallBlock.shouldRaisePost`, reached through `mixin/WallBlockInvoker`). For a straight diagonal run, the rule is shown the wall as if it were a straight low wall running north to south. Every other wall with diagonals gets a post from the rule, as vanilla gives one to any wall that is not a straight run.
- **Shapes.** `mixin/DiagonalShapeMixin` adds each joined diagonal's arm to the outline and collision. It is a run of small overlapping boxes from the middle to the corner, as wide and high as the block's own straight arm: a fence's collision arm is 4 pixels wide and 24 high, its outline 2 wide; a wall's is 6 wide, 24 high in collision and 14 in outline. The same mixin turns the diagonals in `rotate` and `mirror`.
- **Shape memory.** Vanilla works out every fence's, bars block's and wall's shape for each of its states up front. `mixin/DiagonalBlockMixin` has it ignore the four diagonal properties, as it already ignores `waterlogged`, so it works out one shape for each set of straight sides instead of 16. The shapes with arms are kept in `DiagonalConnections.Arms`, by the shape without arms and the diagonals joined. Blocks whose shapes and arms are alike share them, for example every vanilla wall or every wooden fence.
- **Models (`tools/diagonal_connections.py`).**
  - **Arm model:** each block's arm, `jugcraft:block/diagonal/<block>`, is a child of its own side model, so it keeps that block's textures, glass translucency and lighting. Its elements are turned 45 degrees about the post with Minecraft's `rescale`, which stretches them across the block to reach the corner; the arm is drawn narrower by the same factor, so it ends as wide as the straight arm.
  - **Hand-drawn arms:** fences (two rails at 6–9 and 12–15), the bamboo fence (from its own texture sheet's regions), panes (glass not stretched along the arm), bars (a plane of bars with edge strips) and walls (a low side, 6 wide and 14 high, its stone not stretched).
  - **Turned arms:** a side model with its own elements, as the cemetery fence has, is turned element by element. Rails reach the corner; pickets and finials keep their size and are spread along the arm.
  - **Blockstates:** each blockstate gets four more parts, the arm turned 0, 90, 180 and 270 degrees. Vanilla's 72 blockstates are rebuilt in `assets/minecraft/blockstates`: vanilla's own parts, naming vanilla's models by ID, plus the four diagonals. The parts were checked against vanilla 26.3's own blockstates (40 fences, panes and bars in #138, the 32 walls here), all identical before the diagonals were added.

## Balance and automation
None: no recipes, items or resources change.

## Multiplayer and persistence
- The server decides every join, as it decides straight ones. Clients get the new states with the usual block updates, and a client's placement prediction runs the same rule.
- Saved fences, panes and bars load with no diagonals, since the new properties default to false. They join when they next update, for example when a neighbour changes.
- Removing the mod drops the unknown properties, leaving vanilla's own blocks with their straight joins.
- Turning the switch off keeps the properties, so saved states still load.
- **Block states.** The extra properties multiply each fence's, bars block's and wall's states by 16, and any other mod's too:
  - fences, panes and bars: about 26,000 more (54 blocks × 32 straight and waterlogged states × 15 more each);
  - walls: 155,520 more (32 walls × 324 states × 15 more each). A wall has 324 states, ten times a fence's 32, so walls add six times the states that fences, panes and bars do.
  - Vanilla 26.3 has 35,723 block states in all, so walls take the game to several times vanilla's count. What that costs in memory is measured under Verification.
- Another mod that adds properties with the same names (`north_east` and the rest) to fences, bars or walls would clash with this one. The Diagonal Fences mod itself is one such mod, so the two should not be installed together.

## Dependencies and assets
- No new dependencies.
- The arm models are written by `tools/diagonal_connections.py`. They name vanilla's side models and textures by ID and copy no Mojang file.
- Vanilla's blockstates for these 72 blocks are rebuilt from the same parts by the generator. A resource pack that replaces those blockstates wins over them and loses the diagonal arms; a pack that only retextures or reshapes the models keeps them.

## Verification
Checks:
- `python3 tools/check_mod_data.py` (`check_diagonal_connections`):
  - the tag matches the generator;
  - every tagged block has the four arm parts, turned correctly, and an arm model of turned elements with a parent;
  - vanilla's rebuilt blockstates keep vanilla's own parts;
  - every Jugcraft fence-shaped blockstate is tagged;
  - the Java names the four properties and the tag.
- Server game tests (`DiagonalConnectionsGameTests`):
  - fences placed by a player join diagonally, and their outline and collision reach that corner only;
  - a stone in the corner parts them and taking it away rejoins them;
  - a straight corner comes first;
  - breaking one lets the other go;
  - only kinds that join straight join diagonally (nine pairs, including the cemetery fence, the aspen fence, the bamboo fence, panes with copper bars, and bars with an oak fence);
  - rotation and mirroring;
  - every fence and bars block has the properties, starting false, and exactly 54 blocks are tagged.
- Client game test (`DiagonalClientGameTests`): photographs oak and bamboo fence zigzags, cemetery and aspen fence diamonds, and slanted rows of panes, stained glass, and iron and copper bars, from above and from the side.

### Results
- **Run [37137139804](https://github.com/jimbozoomer-byte/jugcraft/actions/runs/37137139804) on 6ad6205b: green**: `mod`, all three client shards and `client`; `repository` passed in run 37137139776.
  - **Server:** all 577 required game tests passed, including the seven diagonal tests. They logged:
    - placed oak fences as `oak_fence[north_east]` and `oak_fence[south_west]`;
    - every pair joining as expected: oak with spruce and with aspen, the cemetery fence with itself, glass with iron bars, red glass with copper bars, bamboo with bamboo; and oak not joining nether brick, the cemetery fence oak, or iron bars oak;
    - "54 blocks join diagonally; problems: []".
  - **Client:** the scene's joins were logged (each diamond's four corners and sides, each zigzag's steps, each slanted row), and the four screenshots were looked at:
    - from above, the zigzags and slanted rows read as unbroken 45-degree lines;
    - the oak and bamboo zigzags run post to post with diagonal rails;
    - the glass, light-blue glass, iron bars and copper bars rows run unbroken on the slant;
    - the cemetery fence and aspen fence diamonds close all the way round, the cemetery fence's pickets spread along its diagonal rails.
- **Failed, then fixed:** run 37137013971 on ca67de64 did not compile its tests: 26.3 has no `Blocks` field for each stained glass pane, and an `Identifier` does not concatenate with a list. The main code and mixins compiled in that run too.

Not run: play; two clients on a dedicated server; a save and reload of joined fences; other mods' fences.

## World and event applicability
- Not seasonal and not tied to any dimension.
- World generation is unchanged: villages and structures place their fences as before, without diagonals.

## Rollout and open questions
- **Walls** followed in the second stage, with low diagonal arms only.
- **Tall diagonal arms** (a diagonal that rises 2 pixels to meet a wall or block above, as a straight side does) would close the 2-pixel slot in diagonal walls two high. They need a fifth property for walls, which would double walls' states again (to about 320,000 more). Left out until the owner decides it is worth the memory.
- Diagonal joins are automatic. A way to stop one forming (for example, sneaking while placing) is a later choice.

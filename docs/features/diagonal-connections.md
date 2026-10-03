# Diagonal connections

Status: implemented, framework stage; **not yet played** by a person. Results are under Verification.
Proposal issue: none. The owner asked for it on 3 October 2026 (see "Owner request" below).
Owner: @jimbozoomer-byte
Target milestone and tier: building, from the first day (no tier).
Primary specialty and supported player role: builders and decorators of every branch.

## Owner request
- "begin making the framework for fences and similar objects to connect diagonally similarly to how this mod does it but I want it to work with any blocks that connect like iron bars, graveyard fence etc etc", with a link to the Diagonal Fences mod and its screenshot of fences running at 45 degrees.
- Nothing is taken from that mod. The rule, the code, the models and the tests here are Jugcraft's own; the mod was only the idea.

## Player experience
- **Fences, glass panes and bars join diagonally.** Place two of a kind a diagonal step apart and a rail (or a pane, or bars) runs between them at 45 degrees, so fences can follow a slope, turn a corner at an angle or draw a diamond.
- **Which blocks:**
  - all of vanilla's fences (13 wooden and the nether brick fence);
  - glass and the 16 stained glass panes;
  - iron bars and the 8 copper bars, waxed or not;
  - all 14 of Jugcraft's fences: the 13 wood-set fences and the wrought-iron cemetery fence (the graveyard fence).
- **The rule, the same from either end.** Two blocks a diagonal step apart join when they would join side by side, and neither joins straight into the two blocks between them:
  - a wooden fence joins any wooden fence; the nether brick fence and the cemetery fence join each other but not wooden fences, as they do straight; panes and bars all join each other;
  - fences never join panes or bars;
  - a straight corner comes first. Fences that already meet round a corner, or a solid block in the corner, keep the diagonal from forming, so two diagonals never cross.
- **Live updates.** Placing, breaking or changing a block updates its diagonal partners at once. Fill in the corner and the diagonal goes; clear it and the diagonal comes back.
- **Shapes.** The diagonal arm is part of the block's outline and collision. A fence's diagonal blocks players and mobs as its straight sides do, and is as high (one and a half blocks).
- **Structures.** Rotating or mirroring a structure (the structure block, templates) turns the diagonals with it.
- **Switch:** `diagonal_connections.enabled=false` stops new diagonal joins; existing ones go when their blocks next update. Vanilla shapes and models stay as they were for every block without a diagonal.

## Connections
- Existing input producer: none needed; the blocks are vanilla's and Jugcraft's own fences, panes and bars.
- Existing output consumer: building. The walled town's fences keep their exact layout: its builder places blocks without shape updates.
- Technology connection: none.
- Magic connection: none.
- Reachable entry path (prove no circular unlock): every block that joins is craftable without any Jugcraft progression.
- Which connections are required vs optional; trade and solo routes: optional and solo.
- How this specialty stays useful without mastering every other branch: it is a building aid for everyone.
- For infrastructure/cosmetics, supported systems and reason resource links do not apply: it changes no recipe, resource or progression; it is a block shape and model behaviour.

## How it works (the framework)
- **Properties.** Every `FenceBlock` and `IronBarsBlock` (glass panes are bars too), including other mods', gets four more boolean properties: `north_east`, `south_east`, `south_west` and `north_west`, all false by default (`mixin/DiagonalStateMixin`, `mixin/DiagonalDefaultStateMixin`).
- **Who joins.** Only blocks in the block tag `#jugcraft:connects_diagonally` ever set them. Every block in it has a diagonal arm model, so nothing joins without an arm to show. Today the tag has 54 blocks: vanilla's 40 and Jugcraft's 14.
- **Adding a block.** A new Jugcraft fence, pane or bars block joins diagonally once it is added to `tools/diagonal_connections.py`. The generator gives it an arm model and its four blockstate parts. The data checker fails if a Jugcraft blockstate shaped like a fence (a part for each straight side, none up or down) is missing from the tag. Another mod's block can join by adding itself to the tag and providing the models.
- **Updates.** `diagonal/DiagonalConnections` works out the diagonals wherever the block works out its straight connections: on placement and on each neighbour's shape update. Diagonal neighbours get no shape updates from the game, so a fence or bars block also asks its four diagonal neighbours to look again whenever it is set. It does this through `updateIndirectNeighbourShapes`, the hook the game uses for redstone dust's diagonal neighbours. Blocks set with `UPDATE_KNOWN_SHAPE` (world generation, the town builder) are left as placed.
- **Shapes.** `mixin/DiagonalShapeMixin` adds each joined diagonal's arm to the outline and collision. It is a run of small overlapping boxes from the middle to the corner, as wide and high as the block's own straight arm: a fence's collision arm is 4 pixels wide and 24 high, its outline 2 wide. Each state's shape is worked out once and kept. The same mixin turns the diagonals in `rotate` and `mirror`.
- **Models (`tools/diagonal_connections.py`).**
  - **Arm model:** each block's arm, `jugcraft:block/diagonal/<block>`, is a child of its own side model, so it keeps that block's textures, glass translucency and lighting. Its elements are turned 45 degrees about the post with Minecraft's `rescale`, which stretches them across the block to reach the corner; the arm is drawn narrower by the same factor, so it ends as wide as the straight arm.
  - **Hand-drawn arms:** fences (two rails at 6–9 and 12–15), the bamboo fence (from its own texture sheet's regions), panes (glass not stretched along the arm) and bars (a plane of bars with edge strips).
  - **Turned arms:** a side model with its own elements, as the cemetery fence has, is turned element by element. Rails reach the corner; pickets and finials keep their size and are spread along the arm.
  - **Blockstates:** each blockstate gets four more parts, the arm turned 0, 90, 180 and 270 degrees. Vanilla's 40 blockstates are rebuilt in `assets/minecraft/blockstates`: vanilla's own parts, naming vanilla's models by ID, plus the four diagonals. The parts were checked against vanilla 26.3's own blockstates, all 40 identical before the diagonals were added.

## Balance and automation
None: no recipes, items or resources change.

## Multiplayer and persistence
- The server decides every join, as it decides straight ones. Clients get the new states with the usual block updates, and a client's placement prediction runs the same rule.
- Saved fences, panes and bars load with no diagonals, since the new properties default to false. They join when they next update, for example when a neighbour changes.
- Removing the mod drops the unknown properties, leaving vanilla's own blocks with their straight joins.
- Turning the switch off keeps the properties, so saved states still load.
- The extra properties multiply each fence and bars block's states by 16. That is about 26,000 more block states for vanilla's 40 blocks and Jugcraft's 14 (54 blocks × 32 straight and waterlogged states × 15 more each), plus 16 times the states of any other mod's fences and bars.
- Another mod that adds properties with the same names (`north_east` and the rest) to fences or bars would clash with this one. The Diagonal Fences mod itself is one such mod, so the two should not be installed together.

## Dependencies and assets
- No new dependencies.
- The arm models are written by `tools/diagonal_connections.py`. They name vanilla's side models and textures by ID and copy no Mojang file.
- Vanilla's blockstates for these 40 blocks are rebuilt from the same parts by the generator. A resource pack that replaces those blockstates wins over them and loses the diagonal arms; a pack that only retextures or reshapes the models keeps them.

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
To be filled in from CI.

Not run: play; two clients on a dedicated server; a save and reload of joined fences; other mods' fences.

## World and event applicability
- Not seasonal and not tied to any dimension.
- World generation is unchanged: villages and structures place their fences as before, without diagonals.

## Rollout and open questions
- **Walls** (cobblestone walls and the rest) are not in this first stage. Their low and tall sides and posts make diagonals a bigger change, and each wall would gain many more states. They can follow on this framework.
- Diagonal joins are automatic. A way to stop one forming (for example, sneaking while placing) is a later choice.

# The Cursed Mirror

Status: implemented (pending CI and review).
Proposal issue: none. The owner asked, 26 October 2026, for new Halloween ideas before Halloween, and chose this one to start. It is a small, self-contained decoration with one consequence.

**This change's choices:** the recipe, how long the bad luck lasts, and that only breaking it (not placing or looking) curses you — listed below for the owner to review.

Owner: jimbozoomer-byte
Target milestone and tier: the Halloween hunt, any tier
Primary specialty and supported player role: decoration and spooky atmosphere

## Player experience

A tall gilt-framed standing mirror with a dark, silvered pane.

- **It hangs on a wall like a picture:** place it and it faces you. It is one block.
- **A face in the glass.** At night a pale face sometimes flickers in the pane for a moment, then is gone — a faint wisp in front of the glass. It is only a trick of the light (drawn on the client), nothing to fight.
- **Break it and you're cursed.** Shattering the mirror with a tool brings seven days of **Bad Luck** (vanilla's `minecraft:unluck`, so fishing and other loot rolls go against you for a while). The glass breaks with a crash and a scatter of shards. The mirror still drops, so you can hang it again — the curse is the price of having broken it.
- **No curse in creative,** and none from the mirror simply being mined by anything that is not a player (it drops as normal).

Seven superstitious days, and then it lifts.

## Connections

- **Input:** gold and glass at a crafting table.
- **Output:** a decoration, and — only if you break it — a Bad Luck effect. Nothing consumes the curse; it runs out on its own.
- **Optional:** yes. Nothing needs the mirror, and nothing needs the curse.

## Balance

- The mirror is a decoration: no inventory, no redstone, no power.
- Bad Luck is vanilla's own mild effect; it changes loot-table luck, not combat or movement. Seven in-game days (168000 ticks) is long enough to notice and short enough to shrug off.
- Only a player's break curses, and only once per break, so a mirror cannot be farmed into a stack of curses, and nothing is applied on a tick.
- The recipe (eight gold ingots around one glass pane) makes it a modest gilt ornament, not an early-game item.

## Multiplayer and persistence

- All of it is the server's except the flicker: the curse is applied on the break event, to the player who broke it. There is no saved mirror state and no per-tick scan.
- Bad Luck is an ordinary mob effect, saved and counted down by vanilla, so it survives a relog like any other.
- The face in the glass is drawn only on the client, from the block's own random animation tick; the server never knows about it.
- New registration, stable from now on: the block and item `jugcraft:cursed_mirror`.

## Dependencies and assets

- No new dependencies. All art is original.
  - Two 16×16 textures in `tools/cursed_mirror.py`: the gilt frame and the silvered pane with its faint face.
  - The break sound is vanilla's glass, and the shards are vanilla's block-break particles.
  - Bad Luck is vanilla's `minecraft:unluck`.
- `tools/cursed_mirror.py` holds the block, item, model, blockstate, recipe, loot, tag, lang and the curse constants; `tools/check_mod_data.py` checks the Java against it.
- Two generated tables gain the mirror by hand, since this machine has no Minecraft jar to rebuild them:
  - `data/jugcraft/alchemy/values.json`: every existing record is kept; the mirror takes the labelled one-essence fallback.
  - `docs/audits/survival-item-registry.json`: the new item ID. The snapshot game test's next export will confirm it.

## Verification

Server game tests (`CursedMirrorGameTests`):

| Test | What it checks |
| --- | --- |
| `breakingCursesThePlayer` | A survival player who breaks the mirror gets Bad Luck for seven days, and the mirror drops. |
| `creativeBreakDoesNotCurse` | A creative player who breaks it gets no effect. |
| `aNonPlayerBreakDoesNotCurse` | The mirror destroyed by other means curses nobody and still drops. |

### Results

Local, without Gradle or a game: the generator, the data checks, the Python unit tests and the Java syntax parse; see the PR. I reviewed the two textures at 8×.

CI: see the PR.

Not run: a player breaking the mirror in a real client.

## World and event applicability

Works everywhere and all year; the night-time flicker follows the world's own day/night, in any dimension with a sky.

## Rollout and open questions

- The recipe, the seven days and "only breaking curses you" await the owner's review.
- Still ideas, not built: a matched set of smaller wall mirrors, and a rare chance the face is a specific unmasked crook.

## History

New block; nothing like it existed before.

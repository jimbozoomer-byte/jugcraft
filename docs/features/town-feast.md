# Town square feast (Thanksgiving phase 6, scene half)

Status: implemented as an isolated service on the review branch `claude/nov10-a-feast-tables`. Written with Claude Code on 10 November 2026. **Not wired and not run.**
- The town's tick, command and protection do not call it yet. Codex wires those hooks; the proposed wiring is delivered separately, not committed.
- Nothing here has been compiled or run in a game: this environment cannot reach the Fabric or Mojang build hosts.

Proposal issue: none. This is the owner's phase 6 request in [the Thanksgiving festival record](thanksgiving-festival-nov1.md). Codex approved layout A on 10 November 2026.
Owner: jimbozoomer-byte
Target milestone and tier: the spawn town's seasonal scene (no progression tier).
Primary specialty and supported player role: a shared town event for every player.

## Player experience

The north plaza of the town square holds a feast from 4 to 30 November, while the town's theme is `harvest`. It sits between the Church of Notch and the main road.

| Site | What | Where (town coordinates, feet at y 1) | Food |
| --- | --- | --- | --- |
| W1 | Linen Feast Table, 6 lengths | x 85–90, z 89 | 12 dishes: mashed potatoes and gravy, harvest pea casserole, candied sweet potatoes, dinner rolls, cranberry sauce, cornbread stuffing, each twice |
| E2 | Linen Feast Table, 4 lengths | x 106–109, z 89 | 8 dishes: turkey slices ×2, dinner rolls, cranberry sauce, cornbread stuffing, candied sweet potatoes, pumpkin pie ×2 |
| T | Giant Turkey Feast | x 103–105, z 88–90; master (103, 1, 90), facing south | 24 portions |

- **Amount:** four servings on each dish, so 80 at the tables, plus the turkey's 24 portions. The feast is set out **once a season**.
- **Who may eat:** anyone with an empty hand.
- **What the town refuses:**
  - sneaking (taking a dish away, or packing the turkey);
  - using a held item (serving food onto a town table);
  - breaking anything.
- **After eating:** what is eaten stays eaten for the rest of the season. Bowl meals give the eater their bowl, as at any Feast Table.
- **Taking down:** when the season ends (or the theme leaves `harvest`), the feast is taken down. Anything a player changed stays: food or cloth they added, or a moved piece.

## Layout and clearances

Every cell is open blueprint ground, inside the wall, off every decor site and every townsperson's place.

Clearances are measured as clear blocks between footprints at feet level: the Chebyshev distance less one, over every pair of cells, from blueprint revision 4. `scripts/tests/test_town_feast_layout.py` checks them against `town.json.gz`.

| Clear blocks to | W1 | E2 | T |
| --- | --- | --- | --- |
| Church building (front wall z 84; furniture inside is at least one block further) | 4 | 4 | 3 |
| Church door path (x 95–98, z 85–92) | 4 | 7 | 4 |
| Nearest market stall | 5 (NW) | 3 (NE) | 7 (NE) |
| Main east–west gate road (z 93–97) | 3 | 3 | 2 |
| South gate road | 22 | 22 | 21 |
| Centrepiece site (legacy harvest and harvest_v2) | 11 | 11 | 10 |
| Nearest guard or guard walker | 3 | 4 | 4 |
| Flak-guard stations | > 40 | > 40 | > 40 |

- **Paths:** the church path and every lamp post and café set stay clear.
- **Sightlines:** guards' eyes clear the 13-pixel tables and the turkey, about 1.2 blocks tall.
- **The centrepiece:** it is untouched, whether the legacy harvest scene or harvest_v2.

## How it works (`town/TownFeast.java`)

**A saved ledger** (`jugcraft:town_feast`, keyed by season and site) records each site's state:
- `placed`, with what each dish was laid with;
- `removed`;
- `preserved`, with the reason;
- `skipped`, with the reason.

The season key is the calendar year of the date.

**Setting out.** A site is set out only if, at the time, all of these hold:
- the theme is `harvest` and the date is 4–30 November (the season calendar's date, or the operator's preview date);
- the site has no entry this season, or only a `skipped` one;
- the preflight passes for the whole site.

**The preflight**, all or nothing per site:
- **Loaded:** every cell of the site, and of the ground two blocks round it from the floor to one block above, is loaded. A chunk is never loaded to check.
- **Blueprint:** every such cell still equals the blueprint of the revision its chunk was built or upgraded to (`TownUpgrades.version` and `historical`). Decor sites change with the theme, so the blueprint doesn't own them and they are not compared.
- **Owner marks:** no cell has an owner override (`TownUpgrades.progress(level).preservedDecor`, read only).
- **The site's own cells:**
  - open blueprint air on a solid floor;
  - no block entity;
  - no casino furniture within two blocks (`TownUpgrades.fixtureNearby`);
  - nothing standing in them.

Failures are handled two ways:
- An unloaded chunk or someone standing in the way is waited out.
- Anything else records `skipped`. A skipped site is tried again while the season lasts: it was never set out, so setting it out later is not a refill.

**Placing.**
- Tables are set with `TownBuilder.set`, each length with its own joined part (start, middle, end), and their dishes are served once.
- The turkey is set part by part, master first, with 24 portions.

**Never refilled.** A site placed or removed in a season is never set out again that season. Nothing tops up a dish or the turkey. This holds through:
- reloads, restarts and chunk reloads;
- town revision upgrades;
- the theme leaving and coming back;
- re-runs.

**Taking down.** This happens when the date leaves the window or the theme leaves `harvest`, and also for an earlier season's leftovers.
- **The check:** a site is removed only while it is still what was set out, less what was eaten:
  - every table length is a linen Feast Table along x;
  - each dish is empty, or holds no more of the same food than was laid;
  - every turkey part is in place, facing as set, with one portion count (any value from 24 to 0).
  Cells already empty count as gone.
- **Removal:** a Feast Table spills its dishes when removed, so its dishes are emptied in code first. The turkey's master goes first: removing another part first makes the master drop the packed feast.
- **When the check fails** (more food, other food, another runner, a moved or missing turkey part), the site is left as it is and recorded as `preserved`.

**The legacy harvest scene and harvest_v2** are not touched. The feast never comes within ten blocks of the 7 × 7 × 13 centrepiece site. Neither `tools/town_decor.py` nor `town.json.gz` changes.

## Hooks for Codex to wire

Nothing calls these yet:

| Call | Where | What |
| --- | --- | --- |
| `TownFeast.tick(ServerLevel)` | the end of each server tick, for example in `TownDecor.register` | Acts every 100 ticks on the overworld. It reads `TownState.get(level).origin()` and `theme()` and the season calendar. No chunk hook is needed: unloaded sites wait for the next check. |
| `TownFeast.managed(ServerLevel, BlockPos)` | `TownProtection.useBlock`, before `publicBlock` | Whether a cell belongs to the town's set-out feast. |
| `TownFeast.mayPublicEat(ServerLevel, BlockPos, Player)` | `TownProtection.useBlock`, before `publicBlock` | Whether the town lets a player eat there now: a managed cell, an empty main hand, not sneaking, and on a table length only the town's own food. If it may, run the block's own `useWithoutItem`; otherwise refuse. |
| `TownFeast.report(ServerLevel, boolean dryRun)` | a `/jugcraft town feast` (and `feast dryrun`) subcommand | Lines to print: the ledger, and with `dryRun` what a run would do now, changing nothing. |

**Permissions.** No `#jugcraft:town_usable` entry and no `TownProtection` change is made here. Owner Feast Tables in town stay private, and `FeastTableBlock` is unchanged.

**Testing.** The core (`TownFeast.run`, `preflight`, `removable`, and the ledger-taking `managed` and `mayPublicEat`) takes the town origin, theme, date, a `Baseline` and a ledger, so it can be tested without a built town.

## Implementation choices (10 November)

- **Theme flips.** A theme change away from `harvest` during the window also takes the feast down. It is then not set out again that season, so a forced theme cannot refill it.
- **`preserved` is final.** It is not retried; the operator decides what to do with it.
- **The ledger belongs to one town origin.** A new origin starts it afresh, as `TownUpgrades.progress` does.
- **A shared margin is allowed.** E2 and T share part of their two-block margins, so a margin cell may hold the other feast site's own block.
- **Name differences from the description.** None: `TownBuilder.set`, `TownUpgrades.fixtureNearby`, `TownUpgrades.progress(level).preservedDecor`, `TownUpgrades.version` and `historical`, and `TownState.get(level).origin()` and `theme()` are as at the base. `preservedDecor` is package-private, so the service lives in the town package.

## Connections

- Existing input producer: the harvest festival dishes and the Giant Turkey Feast (agriculture).
- Existing output consumer: players' hunger and the Feast Table's shared feast blessing.
- No new item, recipe, currency or unlock. The food is the town's, once a season.

## Balance and automation

- Per season: 80 servings and 24 turkey portions in the one town, shared by everyone.
- No automation. Nothing refills it, and town protection keeps pipes and hoppers away.
- A player's companion restocking a town table makes that length private and keeps the site at take-down.

## Multiplayer and persistence

- **The server decides everything.**
- **Saved state:** the ledger is saved with the overworld; the tables keep their dishes in their block entities, and the turkey keeps its portions in its block state.
- **Disabling:** removing the hooks stops new feasts but leaves any set-out feast as it is.

## Dependencies and assets

None.

## Verification

`TownFeastGameTests` holds seven server game tests. Each runs in an arena standing for the north plaza and drives the service directly; none have been run.

| Test | What it checks |
| --- | --- |
| `setsOutTheWholeFeastOnceWithExactlyItsServings` | A dry run changes nothing. Every table length and dish, 80 servings, the turkey's 18 parts at 24 portions, and nothing dropped. |
| `eatenServingsAreNeverRefilledOnAnyReEntry` | After eating, nothing is refilled through a re-run, a dry run, a reload of the saved ledger, another blueprint revision, or a theme flip and back. |
| `consumedStaysConsumedAndIsStillTakenDown` | An empty dish and an eaten-out turkey stay so, are still taken down without spills, and the next November brings a new feast. |
| `anOwnerOrFixtureChangeSkipsTheWholeSite` | An owner block in the margin, casino furniture within two blocks, or an owner mark skips that whole site. It is set out once the ground is clear. |
| `takingDownSpillsNothingAndKeepsPlayersFoodAndCloth` | A player's bread and a player's runner keep their sites; the partly eaten turkey is taken down; nothing is spilled. |
| `theTurkeyGoesMasterFirst` | The service drops nothing. Removing a part first drops one packed feast, which is why the order matters. |
| `onlyManagedFeastCellsArePubliclyEdible` | Through `managed` and `mayPublicEat`: eating at the feast is allowed; sneaking, a held item, an owner's table and a length with added food are refused; nothing is managed after take-down. |

`scripts/tests/test_town_feast_layout.py` checks the layout against the blueprint: the cells, the decor sites and places, and every clearance above.

## World and event applicability

- The spawn town's square only, in November, while the theme is `harvest`.
- It sets out nothing outside the town and changes no chunk generation.

## Rollout and open questions

- **Wiring:** the hooks still have to be wired (Codex). The menu and servings are as approved and can be tuned in `TownFeast.LAYOUT` and `SERVINGS`.
- **Snow:** snow lying on the square before the 4th would read as a change from the blueprint, and the site would be skipped until it clears.

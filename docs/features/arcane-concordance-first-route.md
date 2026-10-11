# The Jugcraft Encyclopedia's first slice: the First Light route

Status: implemented for Concordance Chunk A (unit 4: ticket A-04, the encyclopedia entry and first route); the data
checks pass locally; nothing is compiled or run in a game yet (see Verification).
Proposal issue: none. The owner chose Chunk A on 10 October 2026; [the chunk's contract](concordance-chunk-a.md) holds
the scope, [the first-chunk tickets](../plans/concordance-expansion-2026-10-10/12-detailed-first-chunk-tickets.md) (A-04)
the ask, and [the encyclopedia's own record](jugcraft-encyclopedia.md) the owner's requirements for the whole feature.
Owner: @jimbozoomer-byte. AI-assisted implementation with Claude Code; the model is named in the commit trailers.
Target milestone and tier: a fresh survival world, from the first glowing thing found to a spell of the player's own
(First Light understood, the Practitioner stage), and on to a ward for a friend and, if they like, a lair.
Primary specialty and supported player role: a new caster, and anyone helping one.

## Player experience

**Two ways in, no item.** The Jugcraft Encyclopedia opens:

- with its key (Controls, *Jugcraft Encyclopedia*; **I** unless remapped), only from the game itself, so it never fires
  while typing in chat, a sign, an anvil or a book. Inside the encyclopedia the same key closes it, as Esc does;
- with the small scroll button in the survival inventory, right of the recipe-book button (it moves with it when the
  recipe book opens). In the creative inventory it is on the Survival Inventory tab only. Pressing it closes the
  inventory as Esc would (the carried stack and the 2×2 grid go back), then opens the encyclopedia; Back opens a fresh
  inventory.

**One plain screen**, vanilla widgets only, every control on the keyboard and read by the narrator. A tab for each page
that has something to show this player:

- **Discover**: the First Light route below, then the journal's research and stage reports, and a button to the
  Concordance Journal (J). Every step says, in words beside a mark of its own shape and colour, where it stands (Done,
  Next, Can do now, Later, Not available here) and how much it matters (Required, Recommended, Optional: after a boss).
  A step the player can act on lists its ways: Required, Recommended, Trade (bought or shared), Optional (after a boss).
  A recipe is shown as its crafting grid, read from the server's own recipe; a recipe the route needs but this server
  lacks says so instead, and one it merely recommends is left out. The numbers (how dark, how many, how long a study
  takes, what a spell costs) are read from the loaded rules and the compiler, so a data pack that changes them changes
  the page.
- **Spellcraft** (once First Light is begun, or an instrument carried): the instrument in hand and its limits, Focus,
  what is inscribed and its cost, the invocations known with what each costs this player, the words learned for
  composing (and how many are still to learn), and the way into the Spell Composer, whose Back returns here.
- **Expeditions**: a dossier for each lair the player has found (below).
- **Inspect**: opened while looking at a Circle Anchor or a Ley Pylon within 16 blocks. For an anchor, its own report
  (the same lines chat, `/jugcraft concordance circle` and Jade show, from the anchor's cached report) and the rituals
  the player knows there; for a pylon, its charge, how it fills and whose rituals it lends to.
- **Keys**: every Jugcraft key with its binding now, what it does, and any other key bound the same; the vanilla keys the
  route uses; and where to change them.

**Codex links** open an Arcane Concordance entry the player's research has opened, without the codex in hand.

### The route

Steps in order, with what each waits for, the recipes it shows (with the ingredients the recipe files register), the
sales it names when a town stands (each one a shop really sells), the codex entries it links, and the game tests that
exercise it. Written by `tools/encyclopedia.py` (`python3 tools/encyclopedia.py`) and checked against the Java.

<!-- first-route:start -->
| Step | Matters | Done when | After | Recipes (ingredients on disk) | Trade | Codex | Tests |
|---|---|---|---|---|---|---|---|
| find | Required | First Light encountered, or a luminous specimen carried | - | - | curios: `minecraft:glow_ink_sac` | `radiance/first_light` | `aNewcomerSeesOnlyTheFirstSteps`, `theRouteNamesOnlyRealSales` |
| examine | Required | First Light encountered | find | - | - | - | `theRouteFollowsTheResearch`, `examiningBySneakUse` |
| dark | Required | First Light observed | examine | - | - | - | `theRouteFollowsTheResearch`, `fieldEvidenceUnderstandsFirstLight` |
| understand | Required | First Light understood | dark | recommended: `jugcraft:lampwright_bench` (2 copper ingot, 1 #concordance/luminous matter, 3 #planks, 2 stick) | - | `radiance/lampwright_bench`<br>`foundations/research_notes` | `theRouteFollowsTheResearch`, `benchStudyUnderstandsFirstLight`, `anAbsentRecipeIsNeverAdvertised` |
| wand | Required | an instrument carried | - | required: `jugcraft:initiate_wand` (1 #concordance/luminous matter, 1 stick, 1 copper ingot) | - | `foundations/initiate_wand` | `everyRecipeShownIsOnTheServer`, `anAbsentRecipeIsNeverAdvertised` |
| cast | Required | the advancement Let There Be (Kindle cast) | understand, wand | - | - | `invocations/kindle` (first_light understood) | `theRouteFollowsTheResearch`, `kindleCastsThroughSpellEngine` |
| compose | Required | a spell inscribed on a carried instrument | understand, wand | - | - | `composition/composing` (first_light understood) | `theRouteFollowsTheResearch`, `spellcraftShowsWhatIsHeld` |
| support | Recommended | an inscription with ward and without here | compose | - | - | - | `theRouteFollowsTheResearch`, `aWardForAFriendIsComposedAndCast` |
| equip | Recommended | a piece of the first loadout carried or worn | understand | recommended: `jugcraft:pyromancers_hat` (4 #wool, 1 gold ingot)<br>recommended: `jugcraft:pyromancers_robes` (7 #wool, 1 gold ingot)<br>recommended: `jugcraft:pyromancers_leggings` (6 #wool, 1 gold ingot)<br>recommended: `jugcraft:pyromancers_boots` (2 #wool, 2 gold ingot)<br>recommended: `jugcraft:pyromaniacs_hood` (5 #wool)<br>recommended: `jugcraft:pyromaniacs_tunic` (8 #wool)<br>recommended: `jugcraft:pyromaniacs_pants` (7 #wool)<br>recommended: `jugcraft:pyromaniacs_shoes` (4 #wool)<br>recommended: `jugcraft:lesser_fire_focus` (2 string, 2 gold ingot, 2 #coals, 1 amethyst shard)<br>recommended: `jugcraft:fire_bangle` (4 gold ingot, 1 #coals, 1 amethyst shard) | general: `minecraft:lantern` | - | `theFirstLoadoutIsMadeInSurvival` |
| lair | Optional: after a boss | The Last Harvest or Unravelled | compose | - | - | - | `aBossIsHiddenUntilDiscovered` |
<!-- first-route:end -->

How a step is placed: done when the player's own state says so; otherwise waiting (Later) until the steps it waits for
are done; otherwise Not available here when this server has no way to it (the wand without its recipe); otherwise the
first required step that can be taken is Next (when no required step can be, the first recommended one) and the rest
Can do now. A step after a boss is never Next. The study at a bench is recommended, not an alternative to the dark: a
study made first is kept and completes understanding once one specimen has been examined in the dark (states are reached
in order).

What the player makes along the way, where everything the route touches comes from, and what stays creative-only:
[the first loadout's record](concordance-first-loadout.md).

### Nothing before it is met (proposed, A-D9)

- Words, invocations and spells are named only on steps that need First Light understood.
- A lair, and its rite, show once its token is carried (a Death Knell or a Mourning Wreath for the Hollow Acre, a
  Cursed Spindle for the Spindle Loft); its keeper once met (inside her lair, or owed her reward) or beaten.
- A boss's dossier then adds how support earns a share of her reward ([lair encounter credit](lair-encounter-credit.md)),
  what she gives (the acquisition text JEI shows, "with the default data"), the cheaper rite recipes her loot opens,
  how to leave, and, inside her lair, the player's own part in the current fight.

## Connections

- Existing input producer: the Concordance's research, evidence, invocations and composer (`Journal.research`,
  `StageProgress.describe`, `Composer.state`, `ComposedSpells.compile`), the server's recipes, the town's shops, the
  lairs' tokens, visits, owed rewards and first-win advancements, and a Circle Anchor's or Ley Pylon's own report.
- Existing output consumer: none; the encyclopedia only shows. Its links lead to the codex, the Spell Composer and the
  Concordance Journal.
- Technology connection: none in this slice (the technology route is a later chapter).
- Magic connection: the first route of the Arcane Concordance, from the first examination to a ward for a friend.
- Reachable entry path: the route's first step needs nothing; every required step's recipe uses Overworld vanilla
  things, and no required recipe needs a boss's loot (`check_encyclopedia`).
- Required vs optional; trade and solo routes: every way is labelled. A town's sales show only where a town stands, for
  offers on sale in today's theme (A-D11); Research Notes from a player who understands First Light are a Trade way.
- How this specialty stays useful without the others: every page is optional reading; nothing is gated on it.

## Balance and automation

None: no recipe, item, trade, reward or saved state is added. A recipe a data pack removes is not advertised.

## Multiplayer and persistence

- Server authority: the server writes every page from what it knows of that player. A request names no page and carries
  no text, only a sequence number and, when opened from the game, the block looked at (checked on the server: loaded and
  within 16 blocks, the command's own reach). A player may ask at most once every 10 ticks; one who asks sooner is
  answered at the first allowed tick (only their newest request waits). An answer goes only to a game that can receive
  it. Building a page reads the rules, the player's own records and inventory, the server's recipes and at most one
  block entity near the player; it writes nothing (`check_encyclopedia` forbids the writes).
- Bounds: 6 pages, 96 rows a page (the last says there is more), 12 grids a page, 9 icons a row, 4 alternatives a slot.
- Persistence: none. The page being read is remembered in memory only and forgotten on leaving a world (T11).
- Disable behaviour: with the Concordance off, its pages are left out and a notice says so; with the lairs off, no
  dossier; a server without Jugcraft's pages shows only the Keys page and a notice.
- New network payloads: `jugcraft:encyclopedia_request` (client to server) and `jugcraft:encyclopedia` (server to client).
- New key: `key.jugcraft.encyclopedia`.

## Dependencies and assets

No new dependency. Modonomicon (required, pinned) is called once, on the client, to open a codex entry
(`client/compat/CodexLinks`); a failure says so and is logged once. The screens are vanilla widgets (no GuiLib view in
this slice). The inventory button is added through Fabric's screen events, with no mixin.

The button's sprite is the owner's, copied byte for byte by `tools/owner_art.py` (it came in a set with waystone sprites,
which are not used):

| Runtime file | Library source | SHA-256 |
|---|---|---|
| `textures/gui/sprites/encyclopedia/inventory_button.png` | `originals/Magic/textures/gui/sprites/inventory/inventory_button.png` | `3dc52840afcc95022192e52bff317fe8e2cc6ce753db087f79a96c013cf8b4a4` |

## Verification

- `python3 tools/check_mod_data.py` (with the new `check_encyclopedia`) and `python3 scripts/check_repository.py` pass
  locally; `python3 tools/owner_art.py --check` matches.
- Game tests added (not yet run): `EncyclopediaGameTests` (a newcomer sees only the first steps; the route follows the
  research; every recipe shown is on the server; an absent recipe is never advertised; switched-off features have no
  pages; Spellcraft shows what is held; a boss is hidden until discovered; the route names only real sales; the pages
  survive the trip; requests are gated and the Inspect page reads only a near, loaded block; the page model keeps its
  place) and `ConcordanceComposeGameTests.aWardForAFriendIsComposedAndCast`.

Not yet run: any compilation or game test (this branch has not been through CI); the client screen, the key, the
inventory button and its placement (the client tests are unit 5's); a person following the route from a fresh world
without reading source (A-04's acceptance is a human judgement); readability at GUI scales; the narrator heard; two
clients.

Unverified, and where the tests will settle it: vanilla 26.3's I is free except for F3+I (Copy Recreate Command, a
debug combination that does not also press I); the recipe-book button being the inventory's only image button, and the
creative tab's free space right of the armour slots; `BookGuiManager.openEntry` in Modonomicon 2.16.0 (read in 2.15.0's
source); a placeable specimen being placed when aimed at a block.

## World and event applicability

Seasonal: the dossiers say whether a rite works on any night or only in the Halloween season, from the server's own
setting. Boss containment, loot and dimension travel are unchanged; the dossier only describes them.

## Rollout and open questions

Owner decisions, recorded as proposed in [the chunk's contract](concordance-chunk-a.md): the default key (A-D7), the
button and its place (A-D8), the discovery rule (A-D9), trade edges only from existing shops (A-D11), codex links without
the codex (A-D12) and the route's support spell (A-D13).

Known limits:

- Discovery is read, not saved: a player who entered a lair, left, and used up their token (a Cursed Spindle) reads as
  not having found it until they carry one again or win.
- The Inspect page is read when the encyclopedia opens; refreshing keeps the block it was opened on.
- The route's equip step is done by carrying or wearing any piece of the first loadout.
- Later (outside Chunk A): the GuiLib route canvas, search, history and bookmarks, a remembered last page, the
  technology route, quests, other schools' chapters, invocation cooldowns, bench and machine inspection.

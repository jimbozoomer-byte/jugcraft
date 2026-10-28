# Lair dossiers: every keeper, with lore, preparation and the way home

Status: implemented in source for Concordance Chunk E (E4, shared expedition polish, part 2 of 3), not merged. The
Python audits pass locally; the game compiles and its game tests run only in CI. See Verification for what has run.
Proposal issue: none. The owner chose "Finish Chunk E (E4)" on 28 October 2026; E4 is in
[the delivery chunks](../plans/concordance-expansion-2026-10-10/09-claude-delivery-chunks.md). Part 1 is
[lair-encounter-credit.md](lair-encounter-credit.md#concordance-chunk-e-every-keeper) (every keeper pays its support);
part 3 is replay convenience and a loadout measurement.
Owner: @jimbozoomer-byte. AI-assisted implementation with Claude Code; the model is named in the commit trailers.
Target milestone and tier: the lairs' own tiers (Discovery for the Witching Season lairs, the Moonlit Ring and the
Crowfield; Specialization for the rest), unchanged. The encyclopedia that shows the dossiers is reached from the start.
Primary specialty and supported player role: adventuring, and the party's planner: whoever reads up before a lair.

## Player experience

The encyclopedia's Expeditions page kept a dossier for four lairs: the Hollow Acre, the Spindle Loft, the Glacier Hall and
the Cinder Kiln. The other six lairs the game has were left out, so a player who found the Lantern Fen or the Crowfield
learned nothing there. Now **every lair has a dossier**, in the same shape:

- **Found** (its token carried): the lair's **lore**, the rite in words with the game's numbers, the recipes it needs,
  the town sales that help, when it works, the party it holds, and **what readies a player** for its keeper.
- **Met** (in the lair, or owed a share): the keeper by name, how support earns a share, the first win's trophy, each
  piece of loot with JEI's words, the cheaper token and what is made of the loot, and **the way home**.
- **Beaten:** the first win's advancement.

The six new dossiers:

| Lair | Keeper | Token and where it works | Recipes shown | Town sale | The way home |
|---|---|---|---|---|---|
| The Lantern Fen | the Mire Hag | the Hag Stone, looked through at night in a wetland's shallows or mud | the Hag Stone; after a win, the Hag Stone from Hagmoss | none | the Grey Mist between the dead willows at the jetty's end; the marsh's middle when she falls |
| The Archive of Last Names | the Crypt Lich | the Writ of Exhumation, served on a stone sarcophagus under a roof, any hour | the writ, and a tomb to serve it on (a stone brick sarcophagus; deepslate and blackstone ones serve too); after a win, the writ from Grave Dust | none | the Grey Mist in the stair's doorway; the dais when he falls |
| The Foundry Trench | the Iron Dreadnought | the Sealed Orders, used on a Field Telephone in a trench (4 trench-works blocks within 3), any hour | the orders and the telephone; after a win, the orders from Salvaged Coil | a bell (Curiosities) | the Grey Mist in the dugout's back doorway; the back of his gantry when he falls |
| The Moonlit Ring | the Alpha Werewolf | the Howling Totem, howled at night in the woods, no wolfsbane within 6 | the totem | none | the Grey Mist on the trail behind the camp; the Howling Stone's top when he falls |
| The Cydonian Sanctum | the Cydonian Sentinel | the Altar Keystone, set in the Ancient Altar of Adam-ondi-Ahman at night (stay within 8) | the keystone, and the keystone from Dreamglass; after a win, the keystone from Cydonian Shards | none | the Red Mist behind the gate chamber's arrival; the crater's middle when it falls |
| The Crowfield | the Pumpkin King | the Harvest Crown, set on a headed scarecrow among 8 harvest blocks within 6, at night, in the Halloween season unless `lairs.off_season` allows any night | the crown and the scarecrow; after a win, the King's Lantern made from his Ember | a hay bale (the Seasonal shop) | the Grey Mist at the end of the farmyard's lane; the foot of his mound when he falls |

**Lore** is a line the lair's own hunters, mourners or clerks would tell: a clue to the keeper's rule that never names
the keeper (who is named only once met). For example, the Lantern Fen's: "what waits in the fen walks in many shapes,
but the true one casts a shadow and rings the water round her feet". Every lair has one, the first four too.

**What readies a player** is text and icons, never a recipe grid (as B-P29 set for the Glacier Hall and the Cinder
Kiln, whose brews are unchanged). Each names the answer to the keeper's rule and, where the keeper's own loot holds one,
the piece that helps, held in the other hand:

| Lair | Preparation (numbers from the game) | Icons |
|---|---|---|
| The Hollow Acre | a Grief Bolt hit flies back at its skull; in Death's Harvest strike the souls down or light the wards, and be 10 blocks or more from the middle when she slams | none |
| The Spindle Loft | frayed lace knits back after 12 seconds; snap her Binding Thread; the Golden Thimble turns aside the first projectile, then rests 15 seconds | Golden Thimble |
| The Lantern Fen | within 8 blocks of a burning lamp her Likenesses fade and she glows; a relit lamp shows her 5 seconds; her poison never kills | none |
| The Archive of Last Names | break the lit seal, read a scraped name back, strike his Soul Bolt back; the Emptied Phylactery keeps Wither off | Emptied Phylactery |
| The Foundry Trench | throw the lit pylon's breaker; spread out (his arc jumps 4 blocks); the Grounding Strap lets only 50% of an electric blow through | Grounding Strap |
| The Moonlit Ring | silver does full damage; the Warden Bell flushes him for 8 seconds; wolfsbane keeps his Packmates off; the Packfinder Charm shows werewolves within 32 | Silver Dagger, Wolfsbane, Packfinder Charm |
| The Cydonian Sanctum | shelter is never more than 12 blocks away; a blow of 6 or more on its open core staggers it; the Lowfall Core spares every fall | Lowfall Core |
| The Crowfield | pick the called gourds before they ripen; pick a whole harvest to leave him bereft; fire does him 50% more | none |
| The Glacier Hall, the Cinder Kiln | unchanged: Winter Brine or the Kiln Draught while the Concordance brews it, and the Yeti Mitten or the Salamander Charm | as before |

**Credit** lines name each new keeper's own counted creatures and deeds, as part 1 made them: the Hag's Likenesses and
Root Knot and a relit lamp that shows her; the Lich's dead, seals, names read back and his bolt struck back; the
Dreadnought's Sparkers, the breaker that earths his discharge and a fouled breaker worked; the Alpha's Packmates and the
bell that flushes him; the Sentinel's Shard Sentries (no objective); the Pumpkin King's Jack-o'-Straws and a gourd of a
harvest picked clean.

**The route:** any keeper's first win does the First Light route's lair step, now all ten (it read "any lair's keeper"
before, but the dossiers knew only four).

## The page's room

With four keepers the page's twelve recipe grids (`PageWriter.MAX_GRIDS`) were spoken for. The page now draws grids
while they last, in the keepers' order, and shows any recipe past them **by its items**: the result first, then each
ingredient once, the very items of the grid it would have. So a player who has found only the Crowfield sees its grids;
one who has found and beaten every keeper sees the first four's grids and the rest as items. The page's row bound
(`EncyclopediaPayload.Page.MAX_ROWS`) rises from 160 to 384, enough for every keeper found and beaten with the
bestiary's 24 creatures after them (about 230 rows today), and room for the Storm Roc's and the Glass Abbot's dossiers
when their lairs land.

## Connections

- Existing input producer: each lair's own rite class, keeper, loot tables, recipes and the town's shops; the numbers
  are the game's constants (the rites', the keepers' and their loot's).
- Existing output consumer: the player reading the encyclopedia, and the First Light route's lair step.
- Technology connection: the Foundry Trench's Field Telephone and Sealed Orders, the Sentinel's keystone from Dreamglass.
- Magic connection: the Concordance's support credit, the Stillroot Poultice's fact and the Glacier Hall's and Cinder
  Kiln's brews, unchanged.
- Reachable entry path: unchanged. A dossier shows only once its token is carried, so nothing new is required.
- Required vs optional: all reading. Nothing is unlocked by a dossier.
- How this specialty stays useful: a party's planner can read every lair's counter and way home without beating it.
- For infrastructure: this is guidance; no resource links.

## Balance and automation

No recipe, loot, chance or cost changes. The dossiers only read.

## Multiplayer and persistence

The page is built on the server for the asking player, from their inventory, lair visit, kept shares and advancements,
as before; it writes nothing (the audit's "the pages only read" list covers `LairDossiers`). Discovery is still read,
not saved. No new saved data.

## Dependencies and assets

None new. The lore and preparation words are Jugcraft's own.

## Verification

- `python3 tools/check_mod_data.py` (full): see the pull request.
- `check_encyclopedia` now also holds: every lair a keeper is placed for (`Lairs.onPlaced`) has a dossier, in
  `DOSSIERS`' order; each dossier has its lore and preparation lang, and its own case in `rite` and `prepare`; no lore
  names a keeper; the Werewolf's trophies are read from `bosses/werewolf_alpha.json` (`trophy_table`); a recipe past
  the grid budget is shown by its items; and every keeper found and beaten with the bestiary full fits `MAX_ROWS`.
- New server game tests (CI) in `EncyclopediaGameTests`: `theLanternFensDossier`, `theArchiveOfLastNamesDossier`,
  `theFoundryTrenchsDossier`, `theMoonlitRingsDossier`, `theCydonianSanctumsDossier`, `theCrowfieldsDossier` (each from
  its token carried to a win), `everyLairGivesItsLoreAndPreparation` (the lore first, the rites' and preparations'
  numbers and icons the game's), and `eachRiteSaysWhenItWorks` and `theExpeditionsPageHoldsEveryKeeper` extended (every
  recipe in order, the first twelve as grids and the rest by the items of their grids; no row dropped).
- The client game test (`EncyclopediaClientGameTests`, CI) now also opens the Lantern Fen's dossier, checks it is shown
  and pictures it (`jugcraft_encyclopedia_expedition_lantern_fen`).
- Not run: play by hand, two clients.

## World and event applicability

The Crowfield's rite keeps the Halloween season as its rite does (`lairs.off_season`); the dossier says when it works,
as the Hollow Acre's does. The others work all year.

## Rollout and open questions

- When the Storm Roc's Crown of Thunder or the Glass Abbot's chapel reaches main, the audit asks for its dossier.
- The lore and preparation are English only, as the rest of the encyclopedia is.
- Discovery is still read, not saved (a used-up token hides the rite until carried again or a win).

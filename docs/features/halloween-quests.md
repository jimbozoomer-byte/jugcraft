# Halloween quests

Status: implemented on the contributor's fork, pending review.

Proposal issue: none. The contributor asked on 29 October 2026: "work on quests for the npcs and make them halloween themed and related to the other halloween mods", and chose to have them both from the town's residents and on the commission board.

Owner: jimbozoomer-byte
Target milestone and tier: the town, during the Halloween season; each errand needs the Halloween content it names
Primary specialty and supported player role: anyone who visits the town; boss hunters, trick-or-treaters and decorators

## Player experience

During the Halloween season (the same dates as trick-or-treating, `HalloweenSeason`), eight town residents each have an errand. Talk to one and they say what they want. Talk to them again carrying all of it and they take it, pay Jugs and give you a Halloween decoration. Each errand can be done once a Halloween per player. After that, or out of season, they chat as usual.

| Resident | Asks for | Ties into | Pays |
| --- | --- | --- | --- |
| Priest of Notch | 6 Reaper's Shade | Vesperine, the Hollow Acre | 8 Jugs and a Mourning Wreath (for the Last Rites again) |
| Market vendors | 4 Gossamer Silk | Madame Tatterlace, the Spindle Loft | 6 Jugs and a Witch Hat |
| The Mayor | the Horseman's Lantern | the Headless Horseman | 8 Jugs and a Haunted Portrait |
| Gardeners | a King's Ember | the Pumpkin King, the Crowfield | 6 Jugs and 2 Mini Pumpkin Stacks |
| The Baker | 16 Candy Corn | Halloween treats | 3 Jugs and a Candy Bag |
| Lamplighters | 3 Wisps in Jars | will-o'-wisps on Halloween nights | 3 Jugs and 4 Jack-o'-Lantern String Lights |
| Decorators | 8 Jack o'Lanterns | the carving season | 2 Jugs and 4 Bat Bunting |
| The city watch's guards | 4 Ectoplasm | the graveyard's ghosts | 3 Jugs and a Black Cat Figure |

Boss errands pay the most. Only plain items in the main inventory count: named or otherwise changed items, worn armour and anything in a container are never taken. If the Jugs would take the purse over the town's limit, the resident says so and takes nothing.

### On the commission board

In the season the board shows a **Halloween** plate in its header. It turns to two pages of the same eight errands, each with what is asked, how many you carry, the reward and whether it is done, and a button to hand it in. Handing in at the board or to the resident counts the same, once. The plate reads "More errands" on the first page and "City orders" on the second; out of season it is hidden, and the city orders are unchanged.

## Connections

- **Input producer:** the four Halloween bosses' loot, the candy recipes, wisp catching, carving and the graveyard's ghosts.
- **Output consumer:** Jugs (the town's currency) and Halloween decorations that are all craftable anyway.
- **Unlocks:** none. Nothing needs an errand done.
- **Failure behavior:** out of season the residents chat and the board shows only its city orders; saved records stay.

## Balance

All numbers are provisional, for the owner. Every reward is also obtainable another way, so the errands are a bonus and a pointer to the Halloween content, not a gate. The Mourning Wreath helps a player summon Vesperine again.

## Multiplayer and persistence

All checks and rewards run on the server; the board's screen only shows what the server sends and asks for an errand by number. Each errand's completion is kept per player in the town ledger (`Jugs.Data` "civic", key `<player>/halloween/<errand>`, with the Halloween's year), the same save as the city orders, so it survives restarts and resets the next Halloween. No new item, block or ID.

## Verification

- New server game test `HalloweenQuestsGameTests.errandsPayOnceAHalloween`: every errand names registered items; out of season nothing is taken; one short or named items do not count; carrying them all takes exactly the plain ones, pays the Jugs and gives the decoration; it cannot be done twice; the done mark survives a save; a full purse leaves the errand and the items alone; bad indexes are refused.
- `check_mod_data.py` `check_halloween_quests`: the Java table is `tools/halloween_quests.py`'s, every item is registered, every job is held by a town resident and every errand has its lines.
- Local: `python3 tools/generate_material_data.py`, then `python3 tools/check_mod_data.py` passed (8186 material IDs, data files and recipe audit, with `check_halloween_quests`), `python3 scripts/check_repository.py` passed, and the Foundation workflow's `python3 -m unittest discover -s scripts/tests` passed (290 tests). The Java files parse (tree-sitter); they were not compiled locally.
- CI on the fork: [Build run 40931195328](https://github.com/mmme123-spec/jugcraftfork/actions/runs/40931195328) passed on ec48b44a2 (compilation, server game tests including `HalloweenQuestsGameTests` and `BountyListGameTests`, and the rendered client tests).

Not run: a human talking to the residents or using the board's new pages in a game. No client test shoots the new board pages.

## AI attribution

Implemented with Claude Opus 5.5 (Claude Code) for the contributor; the contributor is responsible for the output.

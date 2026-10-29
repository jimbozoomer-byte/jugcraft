# The Bounty List

Status: implemented on the contributor's fork, pending review.

Proposal issue: none. On 29 October 2026 the contributor asked: "The first quest you complete should give you a list of the bosses", after finding that nothing told a new player the bosses or their summoning items existed.

Owner: jimbozoomer-byte
Target milestone and tier: the town; the first quest a player completes there
Primary specialty and supported player role: newcomers and boss hunters

## Player experience

The first time a player completes a quest, they also get a written book, the **Bounty List** (by "Town Hall"), and a chat line saying why. A quest counts as completed when they collect a city order's Jugs at the commission board or hand in a Halloween errand ([halloween-quests.md](halloween-quests.md)), whichever comes first. Each player gets one book, once.

The book has a "WANTED" first page, then one page for each lair boss: the boss's name in bold, the item that opens its lair, and how to use that item. These are the same instructions as the item's tooltip. There are eleven bosses: Vesperine, Madame Tatterlace, the Yeti King, the Cinder Tyrant, the Mire Hag, the Crypt Lich, the Iron Dreadnought, the Alpha Werewolf, the Cydonian Sentinel, the Pumpkin King and the Headless Horseman. The Storm Roc's Crown of Thunder has no boss yet. Add him to `tools/bounty_list.py` and `BountyList.BOSSES` when his fight is merged.

It is an ordinary written book: it can be copied, shelved on a lectern or given away. A lost book is not replaced.

## Connections

- **Input producer:** the commission board's city orders and the Halloween errands.
- **Output consumer:** the player. The book points to the summoning items, whose recipes JEI shows, and to the lairs.
- **Unlocks:** none. Every summoning item works without the book.
- **Failure behavior:** a full inventory drops the book at the player's feet. Nothing else depends on it.

## Multiplayer and persistence

It is given on the server when the quest pays. Whether a player has had it is kept in the town ledger (`Jugs.Data` "civic", key `<player>/bounty_list`), the same save as the quests, so it is given once ever per player, through restarts. No new item, block or ID: the book is a vanilla written book whose pages use language keys (`bounty_list.jugcraft.*`, from `tools/bounty_list.py`).

## Verification

- New server game test `BountyListGameTests.listNamesEveryBossOnce`: every boss is a registered creature and every lair item a registered item; the book has a first page and one per boss; the first completed quest gives it and a second does not.
- `HalloweenQuestsGameTests.errandsPayOnceAHalloween` now also checks that the first errand gives the book, that it is given once, and that this survives a save.
- `check_mod_data.py` `check_bounty_list`: the Java table is the tool's, every boss has a name, every item is registered, each page's words are in the language file and fit a page.
- Local: `python3 tools/generate_material_data.py`, then `python3 tools/check_mod_data.py` passed (8186 material IDs, data files and recipe audit, with `check_bounty_list`), `python3 scripts/check_repository.py` passed, and `python3 -m unittest discover -s scripts/tests` passed (290 tests). The Java files parse (tree-sitter); they were not compiled locally.
- CI on the fork: [Build run 40931195328](https://github.com/mmme123-spec/jugcraftfork/actions/runs/40931195328) passed on ec48b44a2 (compilation, server game tests including `HalloweenQuestsGameTests` and `BountyListGameTests`, and the rendered client tests).

Not run: a human reading the book in game. No client test shoots its pages.

## AI attribution

Implemented with Claude Opus 5.5 (Claude Code) for the contributor; the contributor is responsible for the output.

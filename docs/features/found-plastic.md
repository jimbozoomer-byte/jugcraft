# Found plastic

Status: implemented on the contributor's fork, pending review.

Proposal issue: none. The contributor asked for it on 27 October 2026, beside the cheaper plastic from naphtha: "There should me another way to get plastic also like finding items made out of plastic that you can deconstruct into pellets so it's not as easily farmable but you can skip ahead a couples steps".

Owner: jimbozoomer-byte
Target milestone and tier: exploration, before the oil refinery
Primary specialty and supported player role: explorers; any player who wants a first few plastic sheets early

## Player experience

Some chests in ruins, wrecks and camps hold old plastic things:

| Item | Breaks down into | Odds in a chest that has found plastic |
| --- | --- | --- |
| Old Plastic Bottle | 1 plastic pellet | 6 in 11 |
| Plastic Duck | 2 plastic pellets | 3 in 11 |
| Old Plastic Crate | 4 plastic pellets | 2 in 11 |

Put one in the crafting grid to break it down. The Metal Press then flattens each pellet into a plastic sheet, as it does for the refinery's pellets. Each item's tooltip says what it breaks down into.

A listed chest has a 30% chance of found plastic. When it does, it holds two finds, about 3.6 pellets. Across all listed chests that is about 1.1 pellets, so about one plastic sheet, per chest opened. That is enough for a player who explores to make their first plastic parts before building a refinery; a factory that needs a lot of plastic still needs oil.

The chests (`chemistry/FoundPlastic.java`, `CHESTS`):
- Minecraft's shipwreck supply chest, buried treasure, both ocean ruins, abandoned mineshafts, dungeons, ruined portals and pillager outposts.
- Jugcraft's raider camps, desert outposts, landscape camps, menagerie campsites and ruins, and the abyssal, forlorn, magnetic and toxic cavern ruins.

## Not farmable

Found plastic comes only from chests that generate once with their structure. No mob drops it, fishing never catches it, no villager sells it and no recipe makes it. A structure's chest is filled once, when it is first opened, so each structure can be looted once.

## Connections

- **Input producer:** exploration; the listed structure chests.
- **Output consumer:** plastic pellets, then plastic sheets in the Metal Press, for every recipe that already uses them.
- **Costs:** none beyond finding the chest. No power or machine is needed to break them down.
- **Unlocks:** nothing new; it gives early access to plastic that otherwise needs crude oil, the Distillation Tower and the Polymerization Reactor.
- **Failure behavior:** with the `machines` feature off, no chest gets found plastic and the break-down recipes do not load; found plastic already in a world stays as an item.

## Balance and automation

The items are made by nothing, so there is no loop. The numbers (30%, two finds, weights 6, 3 and 2, and 1, 2 and 4 pellets) are provisional and flagged for the owner.

## Art

Three new 16×16 icons, drawn as maps in `tools/item_icons/` (`old_plastic_bottle`, `plastic_duck`, `old_plastic_crate`) by the icon rules in `docs/ITEM_ICONS.md`. Two new colours in `tools/icon_materials.py`: bottle plastic (a pale blue, kept apart from the clear glass) and toy yellow (a lemon yellow, 10.5 from gold where at least 10 is required). The crate uses the existing red paint and the duck's beak the existing magma orange. `tools/check_icon_maps.py` passes them with no warnings. The contributor was shown the icons before they were added.

## Multiplayer and persistence

Three new item IDs: `jugcraft:old_plastic_bottle`, `jugcraft:plastic_duck` and `jugcraft:old_plastic_crate`, and three recipes, `jugcraft:plastic_pellets_from_<item>`. No existing ID or saved data changes. The loot is decided on the server when a chest is first opened.

## Verification

- New server game tests in `FoundPlasticGameTests`:
  - `foundPlasticBreaksDownIntoPellets`: each item crafts into its 1, 2 or 4 pellets.
  - `foundPlasticIsInTheListedChestsOnly`: every listed chest table loads; 200 rolls of the shipwreck supply chest and of the raider camp chest each give found plastic; 200 rolls of an end city chest, which is not listed, give none.
- Local: `python3 tools/generate_material_data.py`, the three icons written with `tools/generate_textures.py`'s `save`, then `python3 tools/check_mod_data.py` passed (8028 material IDs, data files and recipe audit, including the icon map checks) and `python3 scripts/check_repository.py` passed. The Java files parse (tree-sitter); they were not compiled locally.
- CI on the fork, run 40548021745 (27 October 2026): the mod compiled and every finished client shard passed; the server tests failed in `foundPlasticIsInTheListedChestsOnly`, which found that `jugcraft:chests/landscape_desert_outpost` does not exist. The desert outpost's table is `jugcraft:landscape_desert_outpost`, outside `chests/`; the list was corrected. A new run is pending.

Not run: a human opening chests in a world, or the icons seen in the game.

## AI attribution

Implemented with Claude Opus 5.5 (Claude Code) for the contributor; the contributor is responsible for the output.

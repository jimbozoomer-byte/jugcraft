# Blank runes craft again

Status: implemented on the contributor's fork, pending review.

Proposal issue: none. The contributor asked for the worst progression problems to be fixed (26 October 2026); runes stuck behind loot was first on the list.

Owner: jimbozoomer-byte
Target milestone and tier: Jimtafacts spellbooks, early magic
Primary specialty and supported player role: magic; any player starting scrolls or school runes

## Player experience

Stone and arcane essence craft two blank runes again, as the spellbook generator intended. Before this, a blank rune could only come from dungeon loot or from uninscribing a school rune with a shriving stone, and a school rune needs a blank rune to make. Every school rune, most scrolls, the school upgrade orbs and the school armor sets start from a blank rune, so a player without lucky loot could not begin any of them.

## Cause

`tools/jimtafacts_spellbook_recipes.py` imports the supplied source recipes and deletes the native starter recipe of each item a source recipe makes, so the source recipe replaces it. The source's only blank rune recipe is uninscribing, which spends an inscribed rune, so deleting the starter left no way in. The generator now keeps the starter recipe for blank runes; every other replacement is unchanged.

## Connections

- **Input producer:** stone, and arcane essence (amethyst, lapis and glowstone).
- **Output consumer:** the school runes, scrolls, upgrade orbs and school armor, through their existing source recipes.
- **Costs:** two blank runes per stone and arcane essence; uninscribing still returns a blank rune for a rune you no longer want.
- **Failure behavior:** unchanged. The recipe loads only while the Concordance feature is on, like the other spellbook recipes.

## Balance and automation

The recipe is the generator's original native one. A blank rune's loot value drops, which is intended: loot still finds them, ready-made.

## Multiplayer and persistence

One recipe file is added back. No IDs, saved data or items change.

## Verification

- New game test `JimtaSpellbookCurioGameTests.blankRunesCraftFromStoneWithoutSpendingARune`: stone and arcane essence find a crafting recipe that makes two blank runes.
- Local: `python tools/generate_material_data.py` restored `data/jugcraft/recipe/jimta_spellbooks_blank_rune.json`. `python tools/check_mod_data.py` passed (7,797 material IDs, data files and recipe audit) and `python scripts/check_repository.py` passed.
- CI: fork [run 40424498934](https://github.com/mmme123-spec/jugcraftfork/actions/runs/40424498934) on 23fa9dc18: green. The mod built, the generated-data and material checks passed, the server game tests passed (with and without optional integrations), and all sixteen client shards passed.
- After merging main (5b8d6520c): fork [run 40464336073](https://github.com/mmme123-spec/jugcraftfork/actions/runs/40464336073) on b1650fa32, green, every job and all sixteen client shards.

Not run: a human crafting runes in game.

## AI attribution

Implemented with Claude Opus 5.5 (Claude Code) for the contributor; the contributor is responsible for the output.

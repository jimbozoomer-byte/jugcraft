# No two items share a name

Status: implemented on the contributor's fork, pending review.

Proposal issue: none. The contributor asked for the worst problems in the pack to be fixed (26 October 2026); 327 items sharing 163 names between them was one.

Owner: jimbozoomer-byte
Target milestone and tier: all tiers (display names only)
Primary specialty and supported player role: every player reading an inventory, a recipe viewer or a chat link

## Player experience

Several supplied collections register their own copy of something Jugcraft already has, under the same English name. In a recipe viewer there were three "Maple Log"s, two "Steam Generator"s, two "Transmutation Table"s, two "Oak Chair"s and so on, with no way to tell which a recipe wanted.

Now every item's name is its own:

- **The original keeps its plain name;** each collection's copy gets the collection after it in brackets: "Maple Log (Landscapes)", "Maple Log (Ecology)", "Steam Generator (Jimtafacts)", "Cattail (Wildlife)", "Block of Uranium (Caverns)", "Candy Cane (Pocket Pet)".
- **Where both copies come from collections,** both get brackets: "Oak Chair (Crafted)" and "Oak Chair (Household)"; "Redstone Relay (Jimtafacts)" and "Redstone Relay (Nouveau)".
- **Pairs a bracket cannot separate get names of their own:**

  | ID | Was | Now |
  | --- | --- | --- |
  | `blueprint_sheet` | Blueprint | Blueprint Sheet |
  | `hv_duct` | HVAC Duct | High-Voltage Duct |
  | `printer` | Office Copier | Desk Printer |
  | `jimta_monitor` | Monitor | Bright Monitor (beside the Semi-Dark and Dark Monitors) |
  | `jimta_computer_wired_modem_full` | Wired Modem | Wired Modem (Full Block) |
  | `nouveau_glyph_rune` | Rune | Rune Glyph |
  | `cavern_fissure_primal_magma` | Primal Magma | Primal Magma Fissure |
  | `beast_red_sand_circle` | Anti-Sea Bear Circle | Red Anti-Sea Bear Circle |
  | `beast_transmutation_table` | Transmutation Table | Beast Transmutation Table |

207 names changed: 93 Landscapes, 41 Household, 40 Crafted, 11 Ecology, 6 Jimtafacts, 3 Wildlife, 2 Caverns, 1 Nouveau, 1 Pocket Pet, and the 9 above.

## How

`tools/display_names.py` runs last in `generate_material_data.py`, after every collection has written its names, and renames only items whose English name another registered item also has. New copies from these collections are named the same way automatically. `check_mod_data.py` now fails if two registered items share a name, naming them, so a pair the rules cannot separate gets a name in `NAMES`.

The collection words are provisional and flagged for the owner, who may prefer names describing the look (for example "Wild Maple Log") to collection names.

## Connections

Display names only. No recipes, tags, IDs, textures or behavior change. Guide text written earlier in the generator (handbook, codex) is unchanged.

## Multiplayer and persistence

No IDs change; saved items keep working. An item renamed with an anvil keeps its custom name.

## Verification

- `python tools/generate_material_data.py` twice: the second run left the language file byte-identical (CI checks generated files are up to date).
- `python tools/check_mod_data.py` with the new duplicate-name check passed (7,797 material IDs); on `main`'s language file the same check finds the 163 shared names. `python scripts/check_repository.py` and `python -m unittest discover -s scripts/tests` passed.
- CI: fork [run 40428945415](https://github.com/mmme123-spec/jugcraftfork/actions/runs/40428945415) on b4dca49b3: green. The mod built, the generated-data and material checks passed, the server game tests passed (with and without optional integrations), and all sixteen client shards passed.
- After merging main (5b8d6520c): fork [run 40465638863](https://github.com/mmme123-spec/jugcraftfork/actions/runs/40465638863) on e1c14b945, green, every job and all sixteen client shards.
- No game test reads these display names.

Not run: a human reading the names in game or in a recipe viewer.

## AI attribution

Implemented with Claude Opus 5.5 (Claude Code) for the contributor; the contributor is responsible for the output.

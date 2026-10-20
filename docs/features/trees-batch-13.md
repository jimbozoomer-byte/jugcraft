# Tree batch 13: walnut and bristlecone

Status: in progress on `codex/tree-generation-batch-13`, based on integrated
main `511b7ec0`. Owner requested implementation on 20 October 2026, with a
walnut timber approval checkpoint before tree or biome work.
Actual tool/model: OpenAI Codex (GPT-6).

The open upstream PRs and branch inventories of all five public forks were
checked before starting. No other open Batch 13 implementation was identified.
PR #421 concerns aged pine/maple and remains separate.

Runtime inspection found that integrated main also contains a recovered
`ecology_walnut` family from the 18 October owner collection. The initial plan
predates that integration. The gallery compares it with this candidate timber.
This is an approval-stage draft, not a finished second walnut acquisition path.
After approval, reconcile the recovered walnut's existing IDs, furniture, boats,
saplings and trees with the selected appearance and Batch 13 distribution,
preserving already placed blocks and inventories.

## First checkpoint: walnut wood

Add the established nine-form wood family: log, wood, stripped log/wood,
planks, stairs, slab, fence and fence gate. Use the existing wood registration,
recipes, tags, fuel and sawmill systems. Hand crafting yields four planks per
log; powered sawing yields six. This initial approval stage has creative
samples only; survival timber acquisition follows with walnut trees.

Use owner painting bank row 2 (`black_walnut`) as the palette reference.
The planned cool dark brown timber is `#5a4b41`; furrowed bark follows the
owner's charcoal-brown ramp. Extend the existing deterministic wood painter
and preserve all existing wood seeds and assets. The owner-library catalog
used for the initial plan predates the recovered ecology collection. Its
walnut art remains unchanged and is included as an in-game comparison.
No library originals change.

Prepare an inspection gallery with all nine forms, exposed end grain, tiled
walls and a small pavilion. Compare against spruce, dark oak, cedar, yew and
persimmon. Verify crafting, stripping axes, drops, fuel, sawmill costs and
save/reopen behavior. Record actual results below when available.

## Remaining authorized scope, after timber approval

- Walnut: 10% of Dense Forest tree selections, replacing fancy oak; 4% in
  Woodland, replacing ordinary oak. One fifth of walnut selections are large.
  Preserve current attempt budgets and other species' absolute shares.
- Normal walnut target: 8–10 blocks tall, 7–9 wide; large: 12–16 tall,
  10–14 wide. Straight lower trunks, connected spreading branches and
  irregular rounded crowns. Measure multiple seeds before finalizing.
- Seasonal green/ripe husks, renewable right-click walnut harvest, yellow-gold
  autumn and bare winter. Shared food routes: raw/roasted walnuts, squirrel
  food, walnut mooncake and consumptive husk-to-brown-dye processing.
- Basin bristlecone: one attempt per four chunks, twisted asymmetric
  4–7-block trees with sparse connected pine needles, 5–8-block spread.
  Existing pine wood, roughly 70% stripped and 30% bark. Existing pine saplings.
  Supported gravel, stone or andesite without dirt pedestals.

Tree generation changes apply only to new chunks. Existing registrations and
placed blocks persist with feature switches disabled. No new dependencies,
platform changes or Batch 14 implementation are included.

## Validation

Wood approval stage, 20 October 2026:

- Five opaque 16x16 textures match their deterministic painter outputs.
  Existing wood textures are unchanged.
- `python scripts/check_repository.py`: pass.
- Client selection unit tests: all six pass.
- `python tools/check_mod_data.py`: pass, 6,145 material IDs; art audit covers
  11,745 block/item models, 3,713 quad parts and 173 combined models.
- Material data regeneration changes only walnut resources, shared membership
  lists and their generated manifest. Essence regeneration adds the nine
  walnut forms using existing wood values.
- `build runClientGameTest -I build/focused.gradle
  -PgameTestFilter=jugcraft-test:walnut_wood_game_tests*
  -PclientTests=WalnutWoodClientGameTests`: pass. Exactly four server tests
  execute: real crafting recipes, axis-preserving paid axe stripping, drops and
  fuel/tags, and actual sawmill input/output/energy behavior.
- The final client-only gallery rerun passes in 1m33s after adding the recovered
  walnut comparison. All nine forms, inventory and 55 sampled block states
  survive save/reopen. Rendered samples and the pavilion were visually checked.
- The delivery copy scans 1,554 saved region records and all other save files
  without finding actual test content. All 83 source files remain unchanged.
  Only unused development metadata in `level.dat`, `level.dat_old` and
  `data/fabric/registry_entries.dat` is removed from the copy.

The export is `build/walnut-wood-preview-r2/Walnut Wood Gallery`. The flat
creative showroom has frozen time/weather/random ticks, comparison rows and a
walnut pavilion. It is not a Batch 13 world-generation demonstration.
Full tree/biome testing follows the timber approval and remaining implementation.
No multiplayer or external launcher import is claimed.

# Tree batch 14: orchard fruit

Status: implementation started on `codex/tree-generation-batch-14`, based on
upstream main `511b7ec0`. The owner requested Batch 14 on 20 October 2026 after
approving Batch 13. Actual tool/model: OpenAI Codex (GPT-6).

The open upstream PRs and branch inventories of all five public forks were
checked before starting; no competing Batch 14 tree implementation was found.
Existing kitchen orchard fruit, seeds, recipes, harvest states and saved IDs are
reused. This branch does not include unmerged Batch 13 changes.

## Scope

- Orchard keeps its 2-3 tree attempts per chunk. Replace the 20% oak-with-bees
  selection with the existing apple tree and a native 2% bee-nest decorator.
  Pear is 15%, peach 10%; retain the current azalea and plum shares, with chestnut
  taking the remainder. Express final shares through conditional probabilities.
- Pear: upright 7-8-block tree, existing fruitwood logs and existing pear foliage.
  Fruitwood recipes work under either agriculture or biomes.
- Peach: low 4-5-block forked/vase crown on vanilla cherry logs.
- Lemon and orange: dense round 4-5-block trees on vanilla oak logs. Subtropics
  orange becomes 10%, removing birch and retaining other species' absolute shares
  except the default azalea remainder. Mediterranean citrus stays 5% each.
  Existing rare vanilla-biome patches remain unchanged.
- Bring apple foliage onto the shared deterministic leaf painter; align pear,
  peach and citrus palettes/blossoms with the tree roster. Preserve fruit-stage
  states and the existing fruit/seed/item designs. No new timber set.
- Pears feed the existing Cider Press; all four orchard fruits are fermentable.
  Add orange as a Candy Kettle flavour (short Jump Boost) using the existing
  flavour system. Existing juices, pies, marmalade and preserves stay intact.

## Validation and delivery

Run the material/art/recipe audit, focused unit and server tests for shape sizes,
logs, saplings, fruit harvest, processing tags and biome probabilities. Reuse the
client inspection garden with three seeds of each shape, fruit-stage displays,
and natural Orchard/Subtropics/Mediterranean stops. Verify save/reopen, package a
matching preview and open it for owner inspection. Record actual results here.

No tests have run for this batch yet. No world save, build or library original is
committed. New tree shapes affect new chunks and newly grown saplings only;
existing registered blocks and items remain loadable. No platform/dependency
changes or later batch work.

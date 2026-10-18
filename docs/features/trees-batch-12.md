# Tree batch 12: rainforest

Status: implemented on `codex/tree-generation-batch-12`; awaiting owner inspection in [draft PR #420](https://github.com/jimbozoomer-byte/jugcraft/pull/420).
Requested by the owner on 18 October 2026; actual tool/model: OpenAI Codex (GPT-6).
Based on the owner-approved Batch 11 branch in PR #418. Batch 13 and later are excluded.

## Scope and coordination

The open upstream PR list and upstream/all five public forks' branch inventories
were checked on 18 October. No overlapping public implementation was identified.
Upstream main `ffc7f7ba` is already included in this branch.

- Add renewable tree ferns to Rainforest (9% of tree attempts), Eucalyptus Forest
  (5%) and Temperate Rainforest (3%), preserving each biome's attempt budget.
- Use the existing banana stem, orchard leaves, pup and harvest route. Place
  bananas in Lush Grassland and reconcile the roster's intended absolute shares
  in Rainforest and Tropics without adding duplicate banana blocks.
- Add the roster's taller mahogany fallback as `emergent_mahogany`: a soil-supported
  2x2 mega-jungle trunk, branching crowns and vines. The native mangrove-root
  prototype failed the first flat-soil seed, so it is not shipped. All four base
  columns are checked across twelve seeds. Existing giant-mahogany saplings stay unchanged.
- The owner explicitly included the missing rubber-tree/tapping/latex route.
  It uses the existing canonical `rubber` item and shared Cooking Pot.

## Distributions

All percentages are absolute shares of existing attempts, converted into native
sequential-selector probabilities. Successful growth also depends on clearance.

| Biome | Shares | Attempts per chunk |
|---|---|---|
| Rainforest | Mahogany 50%, giant mahogany 12%, taller emergent 7%, rubber tree 12%, tree fern 9%, banana 10% | 24, occasionally 28 |
| Tropics | Small palm 25%, flame tree 10%, banana 15%, jungle bush 7.65%, palm 42.35% | 4, occasionally 5 |
| Lush Grassland | Banana 45%, oak bush 25%, mango 20%, small palm 10% | 2, occasionally 3 |
| Eucalyptus Forest | Eucalyptus 42%, big eucalyptus 25%, wattle bush 20%, giant eucalyptus 8%, tree fern 5% | 8, occasionally 9 |
| Temperate Rainforest | Fir 38%, redwood 30%, tall fir 14%, mossy maple 11%, willow 4%, tree fern 3% | 11, occasionally 12 |

Bananas retain the established fruit-crop shape, leaves, ripe-leaf harvesting and
Banana Pup. The roster's older separate frond/bunch proposal is superseded by that
working orchard implementation. Its native cherry foliage requires height four;
it is not silently replaced with the invalid height-two proposal.

## Rubber and fern acquisition

Rubber trees have a 9–12-block straight trunk and compact oval crown. Generated
and sapling-grown logs carry a `natural` state, lost when collected and replaced.
A tap attaches to one of the lowest three trunk blocks, with one tap per trunk.
It requires connected natural vertical logs, soil underneath, and at least eight
nonpersistent nearby rubber leaves at the crown. Stockpiles do not produce latex.
Rubber logs cannot be pushed into new productive arrangements with pistons.

One iron nugget and three planks craft a Tree Tap. Its scheduled tick adds one
fill step every 1,200 loaded ticks, capped at four. A full tap fills one glass
bottle in four minutes. A heated Cooking Pot consumes two Latex Bottles and one
Cider Vinegar in 600 ticks (30 seconds), returning one existing Rubber and all
three Glass Bottles. This abstracts coagulation and drying; there is no new
rubber currency, sulfur recipe, or replacement for the industrial reactor.
The bottleneck is eight loaded tree-minutes per rubber per tap, plus vinegar and
heat. There is no unloaded catch-up, per-tick block entity, or free container.
Breaking a tap returns the tap and loses its unbottled contents.
The alchemy catalogue includes every new item and accounts for the latex bottle's
recoverable glass; rubber keeps its existing value. The processing loop cannot
mint essence through its three returned bottles. Its shared display name is now
Rubber, since both natural latex and the existing industrial route make it.

Rubber logs craft into four jungle planks, saw into six with the existing sawdust
chance, and strip into vanilla stripped jungle logs. They are not fuel logs.
Tree ferns have a 3–5-block fibrous stem and flat frond rosette. Their trunks can
be collected, burned or composted, but do not strip or turn into planks. Fronds
drop fern saplings; shears preserve the foliage. Ferns are excluded from the
timber-producing tree farm. Rubber saplings use that farm's existing timber-only
route; its collected logs are not productive living trees.

## Artwork and implementation

The owner library and tree/plant catalogs were searched. The matching `fern.png`
under the Weapon Smithing slime-foliage collection is a small ground-fern asset,
not a tree-fern trunk or tiled frond texture. The new 16px sprites extend the
established editable Canvas painter in `tools/rainforest_textures.py`, using the
roster's approved palettes. Rubber has three shallow furrows and small cream
latex marks; fern fibres have sparse pale scars and no timber rings. Existing
banana/mahogany textures are reused. No source-library file is altered.

All new blocks use native block models/placements and shared foliage, loot,
recipe, stripping and inspection systems. No new rendering framework is needed.

## Compatibility and validation

New chunks and future sapling growth use the new features; existing chunks are
not rewritten. Existing fruit, timber and recipes retain their IDs. No dependency
or platform change is included. Ferns are decorative renewable plants with no
plank conversion. Bananas continue feeding existing cooking and fermentation.

Validation on 18 October 2026, Minecraft 26.3, Java 25, Fabric Loader 0.19.5,
Fabric API 0.161.0+26.3:

- `build-local.ps1 -Tasks @('build', 'runClientGameTest', '-PclientTests=RainforestClientGameTests')`:
  the build and all **1,973 server GameTests passed**. The first client run found
  a harvest display inside a measured tree plot; it was moved to a separate pad
  before rerunning the client test alone.
- `python -m unittest discover -s scripts/tests -v`: **94 passed**.
- `python tools/check_mod_data.py`: **passed**, 3,031 material IDs plus the data,
  recipe and art audit. Agriculture checks include nonfood bottle remainders and
  registered industrial outputs, without introducing duplicate Rubber IDs.
- **707** generated tree, biome, loot, model and handbook documents matched their
  editable generators. Full material generation completed earlier in the run.
- Repository links, whitespace and the 18 pinned pack dependencies were checked.

- `build-local.ps1 -Tasks @('jar', 'runClientGameTest', '-PclientTests=RainforestClientGameTests')`:
  **passed** after the gallery correction; all 12 real-feature specimens, three
  tap displays and the ripe banana display survived save/reopen. Thirteen
  screenshots cover the trees, all five natural stops, the tap and both leaf modes.
- The final bracketed tap model passed the block/item model audit again: **8,433
  models, zero errors**. No server behavior changed after the full server pass.

The exported normal world uses seed `jugcraft` and has a separate authored garden
at `(1000..1120, 180, -1000..-1080)`. Natural surveys found bananas, rubber trees
and ferns in Rainforest; bananas in Tropics and Lush Grassland; and ferns in
Eucalyptus Forest and Temperate Rainforest. These surveys confirm presence in one
world; they do not measure global occurrence rates.

The source save is preserved. The delivery copy was checked across 7,721 saved
region records and every other save file: it contains no test blocks/entities.
Only unused development metadata changed in `level.dat`, `level.dat_old` and
Fabric's registry-presence snapshot; all other NBT and saved files are unchanged.
The cleaned runtime copy opened successfully through ordinary `runClient` (the test
mod absent), and its garden and supplied inventory were observed in the game.
The world ZIP, matching JAR, complete `.mrpack`, tour, screenshots and checksums are
provided locally in `tree-batch-12-2026-10-18`. The package reuses and verifies all
18 pinned dependency entries. Owner visual approval is pending.

External launcher import, a two-account dedicated-server session, live-server load
and an upgrade of a production save are not claimed.

## Measured shape sample

Twelve fixed seeds per shape, repeated to verify deterministic placement:

| Shape | Measured height | Measured width | Trunk/branch blocks |
|---|---|---|---|
| Tree fern | 4–6 | 5 | 3–5 |
| Rubber tree | 10–13 | 7 | 9–12 |
| Emergent mahogany | 20–31 | 14–16 | 81–126 |
| Existing banana | 5–8 | 5 | 4–6 |

These are observed samples, not promises that every possible seed has those bounds.
Seven focused server tests passed, including every base column, wrong soil,
obstacles, sapling growth, timed tap placement/filling/harvesting, duplicate-tap
and stockpile rejection, living-canopy checks, cap, propagation and shared timber
routes. The real Cooking Pot returned one Rubber and all three bottles.

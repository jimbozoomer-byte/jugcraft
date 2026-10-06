# New trees for Jugcraft's biomes

On 5 October 2026 the owner asked us to "look into each biome and determine what other new trees we need added". This
document answers that. It covers all 99 biomes in `tools/biomes.py`, plus Alpine Spawn and Pixel Hollows. For each
biome it records what grows there now and which vanilla trees are only standing in for the right ones. It then
proposes the trees that would fit, with how each should look and the order to build them in.

It builds on the biome roster ([BIOMES.md](BIOMES.md)) and follows the look in
[NATURAL_TEXTURES.md](../NATURAL_TEXTURES.md), from the wood repaint ([features/wood-repaint.md](../features/wood-repaint.md)).

**Batch 1 is built** on branch `claude/trees-batch-1` ([features/trees-batch-1.md](../features/trees-batch-1.md)), awaiting
review and CI, and not yet played; "Batch 1 as built" under Decisions says where it differs from this plan. The rest is
a plan. Every later tree still needs its own PR, its own feature record in `docs/features/`, in-game screenshots and the
owner's approval of its colours.

How it was made: survey groups proposed trees biome by biome, and their proposals were merged into one roster. Three
reviewers then checked the roster: one for whether it can be built and keeps the project's rules, one for art and fit
with vanilla, and one for biome coverage. Their findings were applied, and the last section, "Decisions", says what
changed and why. Leaf and bark colours were worked out by script from the ramps in `tools/wood_style.py`. Each was
checked by colour distance (CIE76 ΔE) against the trees it grows beside.

## How to read this

- **Shares.** A biome's trees are given as target shares of its tree tries, adding up to 100. Vanilla's
  `random_selector` tries its picks in order, so the build converts the shares into chances in that order:
  chance_i = share_i / (1 - the sum of the earlier shares). Whatever is left goes to the default.
- **(v)** marks a vanilla wood or a vanilla feature. The mod's `oak_bush` and `tall_vine_oak` are made of vanilla oak,
  so they carry it too.
- **Trunks.** "Straight trunk 6 (+3)" means `base_height` 6 and `height_rand_a` 3. "Forking trunk 3 (+1, +1)" also
  gives `height_rand_b`. Foliage is given by vanilla's placer name and fields: blob, spruce, pine, acacia, bush,
  fancy, cherry, random_spread, jungle, dark_oak.
- **Reuses X.** The tree's trunk is wood X's existing log. "Shape only" means it needs only a new entry in
  `trees.SHAPES` and its placed feature, and no new blocks or textures.
- **Colours** are six tones, dark to light, as `LEAVES` needs them. They are proposals. Rule 3 of
  `docs/NATURAL_TEXTURES.md` still applies: the owner approves each one beside the vanilla woods before it ships.
- **Names.** A new tree's key is its common name in snake case. Its leaves are `<key>_leaves`, or `<key>_needles` or
  `<key>_fronds` for those kinds, and its sapling is `<key>_sapling`. A fruit is named for itself only when no other
  tree could bear it. IDs are permanent after release, so this rule is fixed before the first new tree ships.

## Rules every new tree follows

These come from `docs/NATURAL_TEXTURES.md` and `CLAUDE.md`, and from the reviews.

1. **The look.** Each tree is drawn by code in vanilla's manner, never copied or recoloured from Mojang.
   - Everything is 16×16.
   - Each material is one colour stepped into a ramp.
   - Bark and grain run vertically, with no bands across the trunk.
   - Leaves are a speckle of small clumps with a few dark-backed gaps.
   - Each tree has one trait of its own.
   - Silhouettes come from vanilla's own trunk and foliage placers, so a new tree reads as more of the same world.
2. **Leaves.**
   - Six hand-set tones for every look, the darkest about 0.3 times the lightness of the lightest.
   - The same seed draws every seasonal look.
   - Gaps follow the kind's default: about 5% for leaves, 2.4% for needles, 3.5% for blossom and 6.4% for fronds. A
     new optional per-entry threshold covers the few trees that differ.
   - Fruit and flower clusters are placed from each tree's own seed, at least 4 pixels apart and wrapping at the
     edges, so species do not share a dot pattern.
   - Petals and blossom keep their main tones at lightness 0.86 or below. Only single-pixel highlights go above 0.9.
3. **New drawing options in `wood_style.py`** (batch 4):
   - blossom entries get an edge-green ramp, an under-gap colour and a flower coverage;
   - fronds get a midrib count, length and angle;
   - bare twigs can take a ramp of their own (`oak_twigs` #372d20 #4c3f2d #62503a #786246 #8d7453 and
     `dark_oak_twigs` #211912 #32251b #423224 #533e2d #634b36, drawn fresh) for trees on vanilla logs;
   - `LEAVES` takes a per-entry gap threshold.
4. **Feature switches.** A wood's planks, wood and sawmill recipes are on when *any* feature that grows its logs is
   on, and so are a tree's leaves and sapling. `WOOD_SWITCHES` becomes a list, and `check_mod_data` checks that every
   shape's log wood and leaves cover every feature that places the shape. This lands in batch 1, before any borrowed
   larch ships. Built: `jugcraft:feature_enabled` takes an `"or"` list; larch is alpine_spawn or biomes, and the
   chestnut agriculture or biomes (the Orchard places it). The check covers the biomes, Alpine Spawn and agriculture's
   wild patches, and each wood's hand and sawmill recipes.
5. **Placement.**
   - Picks are shares (above).
   - A pick that fails its survival check places nothing, so a tree is never picked where its sapling cannot stand.
   - Trees that grow on sand take `survives_as: minecraft:dead_bush`, as the palm does.
   - Rare lone trees are their own extras (a rarity filter, then the tree's survival check), not trees picks.
   - Cave trees use the cave extras' `environment_scan` placement.
   - Nether trees use `count_on_every_layer`.
   - Vanilla has no absolute-height filter, so there is no "above y 90".
6. **Fruit and ticking.**
   - Fruit ripens on random ticks whatever the season, so `seasons.mode=off` gates nothing.
   - Only tree-grown leaves with air below fruit, as the chestnut's do.
   - Evergreen fruiting leaves tick only while unripe.
   - Each record states its ticking budget.
7. **No gain loops.** Anything that bone meal grows returns less than one bone meal's worth in compost. Each record
   writes out the arithmetic.
8. **Records and tests.**
   - Each batch updates the biome records it changes (the files are named in "Biomes by biome").
   - Each new tree gets its own feature record: tier, producer, consumer, costs, unlocks, failure behaviour and test
     evidence.
   - `WoodClientGameTests` grows each new tree in CI beside vanilla's trees and its neighbours, and shoots it.

## The roster

57 trees. Priority is the value for the owner's world against the cost. The batch is its place in the build order.

| Tree | Species | Trunk | New blocks | Priority | Batch |
|---|---|---|---|---|---|
| stunted_fir (and bog_fir) | young balsam fir; black spruce | fir | none | high | 1 |
| subalpine_fir | subalpine fir | fir | none | medium | 1 |
| fir_bush | krummholz fir | fir | none | medium | 1 |
| tamarack | tamarack | larch | none | high | 1 |
| dead_snag | standing dead snag | dead | none | high | 1 |
| willow_bush | shrub willow | willow | none | medium | 1 |
| young_aspen | aspen suckers, bog birch | aspen | none | low | 1 |
| cedar (the swamp cedar) | Atlantic and northern white cedar | cedar (new wood) | cedar wood set, leaves, sapling | medium | 1 |
| mossy_maple | bigleaf maple | maple | none | high | 1 |
| cottonwood | plains cottonwood, white poplar | aspen, then willow | none | high | 2 |
| japanese_maple | Japanese maple | maple | none | medium | 2 |
| live_oak | southern live oak | oak (v) | none | high | 2 |
| windswept_pine | windswept and cliff pines | spruce (v) | none | high | 2 |
| big_jacaranda | mature jacaranda | jacaranda | none | medium | 2 |
| mallee | mallee eucalypts | eucalyptus | none | medium | 2 |
| giant_eucalyptus | mountain ash | eucalyptus | none | medium | 2 |
| yew | English yew | yew (new wood) | yew wood set, needles, sapling, arils | high | 3 |
| bald_cypress | bald cypress | cypress | needles, sapling | high | 4 |
| snowpetal_cherry | Yoshino cherry | cherry (v) | leaves, sapling | high | 4 |
| shimmer_birch | invented aurora birch | aspen | leaves, sapling | high | 4 |
| magnolia | southern magnolia | willow | leaves, sapling | medium | 4 |
| hawthorn | common hawthorn | oak (v) | leaves, sapling, haws | high | 5 |
| rowan | rowan | maple | leaves, sapling, berries | high | 5 |
| persimmon | American persimmon | dark oak (v) | leaves, sapling, persimmons | high | 5 |
| elder | elder | chestnut | leaves, sapling, elderberries | medium | 5 |
| juniper | junipers | cypress | needles, sapling, berries | high | 6 |
| gorse | gorse | hidden oak log (v) | leaves, sapling | high | 6 |
| sagebrush | big sagebrush | hidden dead log | leaves, sapling | medium | 6 |
| holly | European holly | pale oak (v) | leaves, sapling, sprigs, Holly Wreath | medium | 6 |
| olive | olive | olive (new wood) | olive wood set, leaves, sapling, olives | high | 7 |
| stone_pine | the nut pines | larch | needles, sapling, pine nuts | high | 8 |
| joshua_tree | Joshua tree | palm | fronds, sapling | high | 8 |
| wattle | golden wattle | acacia (v) | leaves, sapling | high | 8 |
| tamarisk | tamarisk | jacaranda | leaves, sapling | low | 8 |
| huge_glowcap | invented giant glowcap | mushroom stem (v) | cap block, light block | high | 9 |
| blight_fungus | invented dead huge fungus | dead | wart block, small fungus | high | 9 |
| marrow_fungus | invented bone fungus | bone block (v) | none | medium | 9 |
| bramble_bush | invented bramble thicket | hidden crimson stem (v) | leaves | high | 9 |
| dragonblood | after Socotra's dragon tree | palm | fronds, sapling, resin | medium | 9 |
| baobab | baobab and boab | palm | leaves, sapling, pod, fruit | high | 10 |
| date_palm | date palm | palm | date cluster, dates | medium | 10 |
| flame_tree | royal poinciana | jacaranda | leaves, sapling | medium | 11 |
| mango | mango | jungle (v) | leaves, sapling, mangoes | medium | 11 |
| sea_grape | sea grape | hidden oak log (v) | leaves, sapling, sea grapes | medium | 11 |
| banyan | banyan | jungle (v) | leaves, sapling | medium | 11 |
| pandanus | screw pine | palm | none | low | 11 |
| osmanthus | sweet osmanthus | oak (v) | leaves, sapling, flowers | low | 11 |
| driftwood | bleached driftwood | stripped dead log | none (one small feature) | low | 11 |
| rubber_tree | Pará rubber tree | rubber log | built by agriculture slice 5 | high | 12 |
| banana | banana | banana stem | stem, leaves, sapling, bunch, bananas | high | 12 |
| tree_fern | tree fern | tree fern trunk | trunk, fronds, sapling | medium | 12 |
| buttressed_mahogany | big-leaf mahogany on buttresses | mahogany | none | medium | 12 |
| walnut | walnut | dark oak (v) | leaves, sapling, walnuts | medium | 13 |
| bristlecone | Great Basin bristlecone pine | dead | none | medium | 13 |
| pear | pear | maple | leaves, sapling, pears | medium | 14 |
| peach | peach | cherry (v) | leaves, sapling, peaches | low | 14 |
| orange | sweet orange | oak (v) | leaves, sapling, oranges | medium | 14 |

### Batch 1: northern, wet and cool-forest shapes

Built on `claude/trees-batch-1` ([features/trees-batch-1.md](../features/trees-batch-1.md)), awaiting review and CI; not yet played.

#### stunted_fir and bog_fir
Young or suppressed balsam fir, and black spruce drawn on fir. **High.** It makes the Muskeg and the Fen match their
own records, and gives the conifer forests an understory. Two shapes cost nothing in textures.
- **Grows in:**
  - stunted_fir: Coniferous Forest 8%, replacing vanilla spruce rather than standing beside it; Snowy Coniferous
    Forest 10%.
  - bog_fir: Muskeg 30%, replacing the full-size fir; Fen 50%, as its default ("short firs"); Bog 15%.
- **Shape:**
  - stunted_fir: straight trunk 4 (+3) under spruce foliage (radius [1, 2], offset [0, 1], trunk_height [0, 1]). A
    squat cone 5-8 blocks tall with needles nearly to the ground.
  - bog_fir: the same trunk under spruce foliage (radius [0, 1], offset [0, 1], trunk_height [2, 3]). A pencil-thin,
    ragged spire with a tuft at the top, like black spruce's "club top".
- **Bark, wood and leaves:** fir's plated dark-brown bark, straw wood #ceb678 and needles (#0d261c-#457d5c).
  Evergreen.
- **Wood:** reuses fir, shape only.

#### subalpine_fir
Subalpine fir (*Abies lasiocarpa*), in its spire form. **Medium.** Narrow "snow ghost" spires are the silhouette of
snowy high forests, and a few dark spires among gold aspens are the Rocky Mountain view.
- **Grows in:** Snowy Coniferous Forest 20%, from the fir's share; Aspen Glade 5%, where it keeps the glade green in
  winter.
- **Shape:** straight trunk 10 (+4) under spruce foliage (radius [1, 2], offset [1, 2], trunk_height [3, 4]). A short
  bare bole shows, and the spire flares slightly at its foot, so it does not read as the cypress's column. 12-15
  blocks.
- **Bark, wood and leaves:** fir's. Snow settles on each tier.
- **Wood:** reuses fir, shape only.

#### fir_bush
Krummholz: wind-stunted fir. **Medium.** It keeps some green in the tundra in winter, when the maple and willow scrub
there is bare.
- **Grows in:** Tundra 20%.
- **Shape:** one log under bush foliage (radius 2, offset 1, height 2): a low, dark, wind-pressed mound.
- **Wood:** reuses fir, shape only.

#### tamarack
Tamarack (*Larix laricina*). **High.** The defining tree of muskegs and fens: gold spikes over a frozen bog in
autumn, at the cost of one shape.
- **Grows in:** Muskeg 35%, from the dead tree's share; Bog 30%, over its red-orange grass; Fen 35%, replacing
  vanilla dark oak.
- **Shape:** straight trunk 6 (+3) under pine foliage (radius 1, offset 1, height 3; built with 3, not 4, see
  Decisions). A thin pole 7-10 blocks tall
  with a narrow, sparse tuft in its top third, taller and thinner than Alpine Spawn's larch cone.
- **Bark, wood and leaves:** larch's red-brown plated bark and amber wood #c98634. The larch needles' three looks are
  already drawn: green; gold from about day 268; bare from about day 318.
- **Wood:** reuses larch, shape only.
  - Larch joins `FALLEN` (length 5-8), so tamaracks lie fallen too: fallen larch is 2% of the Muskeg's trees.
  - The larch sapling still grows the alpine cone.
  - Larch's switch list becomes alpine_spawn and biomes (rule 4).

#### dead_snag
A standing dead snag, killed by fire, beetles or flood. **High.** Real dead stands are mostly straight snags. A
second dead silhouette makes the dead biomes read as killed forests, not a scatter of identical props, and gives the
Ghost Forest its colonnade.
- **Grows in:**

  | Biome | Share | Note |
  |---|---|---|
  | Ghost Forest | 55% | the default; the dead tree stays at 30% |
  | Dead Forest | 35% | |
  | Dead Swamp | 50% | standing in its ponds |
  | Muskeg | 20% | |
  | Coniferous Forest | 3% | |
  | Burnt Forest | 45% | |
  | Wasteland | 40% | |
  | Cinder Barrens | 1-2 tries a chunk | on its coarse-dirt patches, about one chunk in eight |
  | Gloomweald | 10% | with its new canopy, in batch 3 |
  | Rotted Expanse | 30% | |

  The existing fallen dead log also joins the Dead Swamp, Burnt Forest and Rotted Expanse.
- **Shape:** two forms, both on the dead tree's no-foliage path (air foliage).
  - dead_snag: straight trunk 3 (+4, +3). A straight grey spar 3-10 blocks tall, broken off at the top.
  - dead_snag_bent, about a quarter of snags: bending trunk 4 (+3), bend_length 1, giving one crooked top. As built,
    `min_height_for_leaves` 8 (above the trunk), so only the bend carries the air crown and no trunk log's crown can
    open a hole in 2-deep swamp water.
  - The forking trunk is not used, because with no leaves it reads as a bare acacia.
- **Bark and wood:** dead wood's grey furrowed bark (#33302d-#908a81) and grey wood #7a7a7a.
- **Wood:** reuses dead wood, shape only. It survives as an oak sapling would, as the dead tree does, so it keeps to
  soil and coarse dirt.

#### willow_bush
Shrub willow (*Salix*): arctic, bog and riverbank scrub. **Medium.** It replaces vanilla-oak scrub with the shrub that
really grows where floods reach, and it changes with the seasons where the oak bush does not.
- **Grows in:**
  - Floodplain 70%, as its default, replacing the oak bush.
  - Lush River 70%, replacing the oak bush; the existing willow also joins at 20%.
  - Tundra 30%, turning yellow beside the red maple scrub.
  - Fen 15%, as carr scrub.
- **Shape:** straight trunk 1 (+1) under bush foliage (radius 2, offset 1, height 2). A low, round mound of willow
  leaves, with no hanging curtains.
- **Leaves:** willow's three looks: green; yellow from about day 283; bare from about day 328.
- **Wood:** reuses willow, shape only.

#### young_aspen
Quaking aspen suckers, and the bog birch form. **Low.** Trees of mixed ages make the glade read as one living clone,
and white trunks make the Hallowed Bog pale.
- **Grows in:** Aspen Glade 15%; Hallowed Bog 30%, replacing the vine-hung oak (v), in water up to 2 deep.
- **Shape:** bending trunk 4 (+2), bend_length 1, `min_height_for_leaves` 7, under a small blob crown (radius 2,
  offset 0, height 3). The leaf height sits above the tallest trunk, so only the bend carries a crown rather than a
  stack of blobs. A thin, slightly leaning white pole 6-7 blocks tall.
- **Leaves:** aspen's, with its seasons.
- **Wood:** reuses aspen, shape only.

#### swamp_cedar (built as `cedar`)
Atlantic and northern white cedar (*Chamaecyparis thyoides*, *Thuja occidentalis*). **Medium.** The real conifer of
cedar swamps, replacing vanilla spruce and dark oak and keeping the wetland green beside its bare winter willows.
- **Grows in:** Wetland 50%, as its default, replacing vanilla spruce; Ghost Forest 15%, replacing vanilla dark oak as
  the living fringe beside its own dead trunks.
- **Shape:** straight trunk 7 (+3) under spruce foliage (radius [1, 2], offset [0, 1], trunk_height [3, 5]). A clear
  stringy red-brown bole shows under a dense, narrow cone 7-10 blocks tall, so it reads as a cedar, not another small
  fir.
- **Bark, wood and leaves (as built):** its own wood, **cedar**, after the owner's 5 October update ("We are ok for
  making new wood and logs for a bunch as well ... Unless they are supposed to be the same wood"): the swamp cedar is
  not a cypress.
  - Bark: the owner's painted western red cedar (their second set, row 0, moved from `OWNER_BANK` into `WOOD` as
    "2:0" and `BARK`), #483229 #513a2f #563d31 #65493a #6b4f40, darkened by the wood's lightness ratio (0.866) to
    #3e2b24 #463229 #4a352a #573f32 #5d4437, so the log keeps the painting's contrast with its stripped side. Stringy.
  - Wood: the painting's #84654d, darkened to #725543 so it stands off vanilla spruce and the cypress (ΔE 10.0 and 10.6).
  - Leaves: `cedar_leaves`, sage-green scale-leaves #27301a #343f22 #43522c #536539 #657a46 #7d9657, ΔE 14.8 from the
    cypress's and 16.1 from the fir's. Evergreen.
- **Wood:** the cedar wood set, leaves and sapling. The shape is named `cedar`, which the cedar sapling grows.

#### mossy_maple
Bigleaf maple (*Acer macrophyllum*). **High.** The temperate rainforest's own moss-draped broadleaf giant in place of
a vanilla oak, so the "dripping" rainforest finally has moss on its trees.
- **Grows in:** Temperate Rainforest 11%, replacing the vine-hung oak (v); Redwood Forest 5%.
- **Shape:** fancy trunk 9 (+4) under fancy foliage (radius 2, offset 4, height 4), 10-14 blocks.
  - Vanilla's `attached_to_logs` decorator lays moss carpet on the upper faces of its limbs (probability 0.35,
    direction up).
  - Vine decorators: trunk vines, and leaf vines at 0.3.
- **Leaves:** maple's: green; red, orange and gold; bare.
- **Wood:** reuses maple, worldgen only.

### Batch 2: warm and mountain shapes, and the Lush Desert fix

#### cottonwood
Plains cottonwood and white poplar (*Populus deltoides*, *P. alba*), the same genus as the aspen. **High.** It
replaces vanilla oaks with the giant of floodplains and prairie creeks, and gives a golden crown over golden grass in
autumn.
- **Grows in:** Floodplain 30%, replacing vanilla oak; Prairie 10%, replacing most of the vanilla fancy oak (5% stays
  as the odd bur oak); Lush River 10%.
- **Shape:** fancy trunk 10 (+6) under fancy foliage (radius 2, offset 4, height 4). A tall trunk 10-16 blocks high,
  forking into heavy limbs under a broad, billowing crown.
- **Bark and wood:**
  - In batch 2 it takes aspen logs (white bark with dark marks, near-white wood), so it reads as the white poplar.
  - Once batch 4 lands, the shape moves to willow logs (grey-brown, furrowed), which is the plains cottonwood's real
    bark.
  - That move is a one-line worldgen change with no saved IDs.
- **Leaves:** aspen's: green; gold from about day 258; bare from about day 302.
- **Wood:** worldgen only, as big_maple is. Optionally an aspen sapling grows one a tenth of the time, as an oak
  sapling grows a fancy oak.

#### japanese_maple
Japanese maple (*Acer palmatum*). **Medium.** Red maples round the steaming pools. The Hot Springs' grass already
follows the seasons, but its trees never change; these do.
- **Grows in:** Hot Springs 30%, replacing vanilla spruce.
- **Shape:** forking trunk 2 (+1, +1) under blob foliage (radius 2, offset 0, height 2). Two low, overlapping domes
  4-5 blocks tall and wider than tall. Acacia pads are kept for the jacaranda family.
- **Leaves:** maple's, with its seasons.
- **Wood:** reuses maple, worldgen only.

#### live_oak
Southern live oak (*Quercus virginiana*). **High.** The signature silhouette of the humid subtropics and the bayou,
made entirely from vanilla parts.
- **Grows in:** Subtropics 30%, replacing vanilla oak; Bayou 15%, replacing the vine-hung oak (v).
- **Shape:** vanilla's cherry trunk placer throws out long, nearly level limbs:
  - base 4 (+1), branch_count [2, 3], branch_horizontal_length [4, 6];
  - branch_start_offset_from_top [-3, -2], branch_end_offset_from_top [-1, 0].

  Each limb ends in a flat pad of blob foliage (radius 3, offset 0, height 2). The tree is 6-8 blocks high and up to
  about 15 across. Vanilla's `attached_to_leaves` decorator hangs the mod's existing **shroud moss** under the limbs
  (direction down, about 0.3), so one hanging moss stands for Spanish moss in all the warm swamps. Pale hanging moss
  stays with the pale oak.
- **Wood and leaves:** vanilla oak logs and leaves, placed and not copied, as great_oak and tall_vine_oak are.

#### windswept_pine
Windswept white and jack pine of the Canadian Shield, Huangshan pine and Japanese black pine. **High.** The iconic
cliff pine replaces taiga pines in three mountain biomes, at no texture cost.
- **Grows in:**
  - Shield 25%, replacing vanilla pine. The Shield's vanilla oak also becomes aspen, and larch joins at 8%.
  - Karst Pinnacles 65%, as its default, replacing vanilla pine.
  - Hot Springs 55%, replacing most of the vanilla pine.
- **Shape:** vanilla's cherry trunk placer, with acacia foliage (radius 1) at every tip, which gives small flat pads
  at different heights: a leaning, layered cliff pine 6-9 blocks tall.
  - base 5 (+2), branch_count [2, 3], branch_horizontal_length [2, 3];
  - branch_start_offset_from_top [-4, -2], branch_end_offset_from_top [-1, 0].

  It does not use vanilla acacia's own forking and acacia placers, which would read as acacias in spruce colours.
- **Wood and leaves:** vanilla spruce logs and needles, as vanilla's own pine uses. The owner may later switch it to
  the stone pine's needles (batch 8).

#### big_jacaranda
A mature jacaranda (*Jacaranda mimosifolia*). **Medium.** Old giants among the small umbrellas: a mix of sizes and
ages makes a grove feel real.
- **Grows in:** Jacaranda Glade 15%; Glimmer Grove 15%.
- **Shape:** fancy trunk 7 (+4) under fancy foliage (radius 2, offset 4, height 4). An open, branching crown 10-14
  blocks tall, standing to the jacaranda as big_maple stands to the maple.
- **Bark, wood and leaves:** jacaranda's grey-brown furrowed bark, the owner's mauve wood #7a5a5e and violet blossom,
  in bloom all year.
- **Wood:** reuses jacaranda, shape only.

#### mallee
Mallee eucalypts: small, multi-stemmed *Eucalyptus*. **Medium.** Small gums are the commonest tree of dry Australia.
With the baobab, they make the Outback read as Australian rather than a generic acacia desert.
- **Grows in:** Outback 30%.
- **Shape:** forking trunk 3 (+1, +1) under random_spread foliage (radius 2, offset 0, foliage_height 2, 40
  attempts). An airy, leaning gum 5-6 blocks tall. It survives as a dead bush would, so it stands on red sand.
- **Bark, wood and leaves:** eucalyptus's green bark with rainbow streaks, tan wood #bda281 and blue-green leaves.
- **Wood:** reuses eucalyptus, shape only.

#### giant_eucalyptus
Mountain ash (*Eucalyptus regnans*). **Medium.** It gives the third big-tree wood the same four-sapling giant as the
redwood and mahogany have, and makes the biome live up to "tall eucalyptus".
- **Grows in:** Eucalyptus Forest 8%. Four eucalyptus saplings in a square also grow it.
- **Shape:** giant trunk 24 (+6, +8), two blocks wide, under a small, airy random_spread crown high up (radius 4,
  offset 0, foliage_height 4, 120 attempts).
- **Wood:** reuses eucalyptus. Needs a `"giant"` entry in `agriculture.TREES`, which makes the sapling a
  `GiantSaplingBlock`. No new blocks.

**Also in batch 2:**
- **The Lush Desert defect.** Its oak-bush pick (30%) checks an oak sapling, which can never stand on its red sand or
  sand, so the pick places nothing. The small palm replaces it now, since desert fan palms grow wild in the Mojave
  beside Joshua trees. Grass patches are added to its surface so that its `bushes_dense` and `meadow_wildflowers`
  extras have dirt to grow on.
- **`apple_tree_checked`** is added to `biomes.PLACED_TREES` (the apple tree on an apple-sapling check), for the
  Flower Isle now and the Orchard in batch 14.

### Batch 3: the yew

#### yew
English yew (*Taxus baccata*). **High.** The yew is the tree of churchyards and folklore, and the owner has already
painted a wood for it. It gives the Gloomweald its own ominous evergreen in place of vanilla spruce, and gives
builders a tree for the mod's Halloween graveyards and cemetery fences. Halloween opens on 20 October. The yew has its
own wood and leaves, the pattern every current tree uses, so it needs none of batch 4's shared change and can ship
first.
- **Grows in:**
  - Gloomweald, 20% of its own new canopy list.
  - Player churchyards, which is where its sapling is the point.
  - It is not placed in the Dense Forest, where it would stand beside the holly as a second dark evergreen with red
    berries.
- **Shape:**
  - yew: straight trunk 2 (+2) under blob foliage (radius 3, offset 0, height 4). A low, broad, very dark dome 5-7
    blocks tall, wider than tall, its crown almost to the ground.
  - ancient_yew, which four saplings in a square grow: vanilla's dark_oak trunk and foliage (two blocks wide,
    gnarled), with podzol beneath (`alter_ground`).
- **Bark:** the owner's painted yew bark (`OWNER_BANK` row 7: #473729 #564636 #614f3f #6e5d4a #766551), plated as
  painted, vertical only.
- **Wood:** the owner's yew, #654135, a dark red-brown.
  - It shares the cypress's and redwood's hue (about 15) but is much darker: lightness 0.30 against 0.46.
  - It is far less saturated than the mahogany.
  - Check it beside the cedar (batch 1, #725543: ΔE 8.8, the nearest wood), the cypress, mahogany and redwood, and
    vanilla's mangrove and dark oak, before it ships.
- **Leaves:** `yew_needles`, the darkest and densest needles in the mod (about 1.5% gaps):
  #111a12 #182419 #1f3021 #2b3d2d #354c38 #425f45. Its one trait is a few red arils (#901a29 #b22e3e #d04859, each
  with a dark #3a1418 centre pixel), which ripen on random ticks as the chestnut's burs do. Evergreen.
- **Extras:** yew arils, picked when ripe. They are poisonous to eat, give red dye and serve as a witch's-cauldron
  floater (decor17 `FLOATERS`). Yew wood could later be an optional material for the Arms branch's longbows, never a
  required one.
- **Also in batch 3:**
  - **The Gloomweald fix.** Its base keeps vanilla's whole dark-forest vegetation, and its own 2-3 trees mostly land
    on that canopy, where their sapling checks fail. So the Gloomweald sets `base_trees` to
    `minecraft:dark_forest_vegetation` and gets its own list of about 16 a chunk instead: dark oak (v) 45, yew 20,
    dead tree 10, dead snag 10, huge red mushroom (v) 8, huge brown mushroom (v) 7.
  - Check the yew's frequency in the client survey.

### Batch 4: own leaves on borrowed logs (the shared change), with four swamp and wonder trees

The shared change comes first, with a short design note posted before building. Until now a tree's name has been its
wood's name, and most later trees need that to change:
- `agriculture.TREES` gets a `wood` field (the log) and a `display` name;
- `trees.SHAPES` gets a `tree` field (whose leaves);
- Java's `registerTree` is split from `registerWoodSet`, so a tree on borrowed logs registers no phantom wood set;
- the switch, display names, tree-farm output and the giant check all key on the log wood;
- one-log shrubs give the tree farm 1-2 logs, not 6;
- `check_mod_data` refuses a wood-set block for any tree without its own `WOOD_SETS` entry;
- `trunk_placer` passes vanilla's `upwards_branching` probability and tag fields through, and vanilla's
  `mangrove_root_placer` is supported;
- the leaf drawing options under "Rules", item 3.

#### bald_cypress
Bald cypress (*Taxodium distichum*). **High.** The Bayou's name promises bald cypress, so this is the clearest stand-in
in the wetlands. One leaf set serves four swamps, and its russet autumn sits beside the willows' yellow.
- **Grows in:**
  - Bayou 60%, as its default; the willow stays at 25%.
  - Lush Swamp 25%; the existing maple also joins at 10%, as red swamp maple.
  - Swamp Woods 25%; the vanilla azalea also joins at 10%, as swamp azalea.
  - Webwood 15%, replacing vanilla birch and half the vine-hung oak (v).
- **Shape:**
  - Trunk: vanilla mangrove's `upwards_branching` trunk, base 9 (+3, +2), place_branch_per_log_probability 0.15,
    extra_branch_steps [1, 2], extra_branch_length [0, 1]. One tall, straight trunk 9-14 blocks with one or two short
    limbs.
  - Foot: vanilla's `mangrove_root_placer` with cypress logs as the roots (trunk_offset_y 1, max_root_width 2,
    max_root_length 3), so it stands in water on a flared foot.
  - Crown: a flat, airy random_spread crown (radius 3, offset 0, foliage_height 2, 50 attempts), with the existing
    shroud moss hanging beneath it (`attached_to_leaves`, about 0.12).
  - Fallback if the root placer is not ready: a straight trunk 10 (+4).
  - In the Webwood it stands on dry ground, which bald cypress also does.
- **Bark and wood:** cypress's stringy red-brown bark and wood #916558.
- **Leaves:** `bald_cypress_needles`, soft and open (about 5% gaps). All three looks come from one seed, on a schedule
  of about [98, 292, 330].

  | Look | Colours |
  |---|---|
  | green | #333d19 #485523 #5e702d #73883a #8da647 #a2b866. A greyed lime, lighter and yellower than any needles in the mod. |
  | autumn | #392014 #502d1b #683b24 #7f492f #9b5a39 #b56e4a. A russet cinnamon at hue 19 and saturation 0.46, about ΔE 24 or more from every maple look. |
  | winter | bare twigs in the cypress bark's colours |
- **Sapling:** a cross sprite in the needle tones on a cypress-bark stem.
- **Wood:** reuses cypress logs, with new needles and a sapling.
- **Extras:** shroud moss, taken with shears. Cypress becomes the swamps' building wood for stilt houses and docks.

#### snowpetal_cherry
Yoshino cherry (*Prunus × yedoensis*), the white-flowered cherry. **High.** White blossom over white petals in the
snow is the grove's whole picture, and vanilla's pink breaks it. It is the cheapest high-value swap on the roster.
- **Grows in:** Snowpetal Grove 75%, replacing vanilla cherry as its default. Its vanilla birch becomes the aspen
  (25%) in batch 1.
- **Shape:** exactly vanilla cherry's shape, with vanilla cherry logs.
- **Leaves:** `snowpetal_cherry_leaves`, blossom kind, among the densest (about 2% gaps).
  - Flowers: near-white clumps with a faint lilac-grey shadow, #8a7f94 #a79db0 #c2bbc8 #d6d1da, with single-pixel
    highlights of #e4e1e7.
  - Edge green: #2e5426 #3c6a2e.
  - Under the gaps: #2a3328, so fast graphics show dense foliage.
  - It sheds white petal particles, as vanilla's cherry sheds pink ones.
  - In bloom all year.
- **Wood:** reuses vanilla cherry logs, the same genus, with new leaves and a sapling.

#### shimmer_birch
An invented aurora birch, shaped after the silver birch (*Betula pendula*). **High.** One leaf block gives three
wonder biomes a magic tree of their own in place of vanilla birch and oak.
- **Grows in:**
  - Frostlight Garden 40%, replacing vanilla birch.
  - Starlit Wood 45% as its tall form, replacing vanilla's super birch as the default. Vanilla's stays at 30% for its
    bees.
  - Glimmer Grove 20%, replacing vanilla oak.
- **Shape:**
  - shimmer_birch: straight trunk 5 (+2) under blob foliage (radius 2, height 3), vanilla birch's silhouette.
  - tall_shimmer_birch: straight trunk 10 (+4) under blob foliage (radius 2, height 5), after vanilla's super birch.
- **Bark and wood:** aspen's white bark with dark marks, and near-white wood.
- **Leaves:** `shimmer_birch_leaves`, a light mint green at hue 155: #1b4433 #265f47 #327c5d #419773 #52b58b #7ac2a4.
  - That is ΔE 16 from the eucalyptus.
  - Its one trait is a few pink-lilac aurora clusters (#7a5288 #a070b0 #c49ad0), set as the chestnut's burs are and
    pinker than the jacaranda's violet.
  - Evergreen. Leaves follow the world's season, but this look has no seasonal states.
- **Wood:** reuses aspen logs, with new leaves and a sapling.

#### magnolia
Southern magnolia (*Magnolia grandiflora*). **Medium.** It gives the flower island the showy flowering tree its name
promises, without repeating vanilla's pink cherry or the violet jacaranda.
- **Grows in:** Flower Isle 55%, as its default. The oak bush stays at 30%, and the apple tree joins at 15% in
  batch 2. The wild apple patch already reaches the isle through `c:is_floral`.
- **Shape:** straight trunk 5 (+2) under a dense, broad oval of blob foliage (radius 3, offset 0, height 4), bigger and
  darker than an oak.
- **Bark and wood:** willow's grey-brown furrowed bark, and pale yellow-green wood #c7c785 that passes for magnolia's
  greenish cream.
- **Leaves:** `magnolia_leaves`, a glossy dark green in slightly larger two-pixel clumps, with about 3% gaps:
  #122c12 #1a3d19 #225020 #2b612a #357733 #438c40.
  - Its one trait is a few big cream flowers (2-3 px clumps of #c9b578 #ddd2b2 #e8e1cc), each with a darker #b8962e
    centre.
  - Evergreen, and in flower all year.
- **Wood:** reuses willow logs, with new leaves and a sapling.

### Batch 5: fruit on seasonal leaves

The shared change, with a short design note posted first:
- a `SeasonalFruitLeavesBlock` with both a season state and a fruit stage, the fruit drawn on every look from one
  seed;
- bare-twig ramps for trees on vanilla logs;
- the rare "lone tree" extra (a rarity filter, then the tree's survival check), first used in the Highland.

Fruit ripens on random ticks whatever the season, and only on tree-grown leaves with air below.

#### hawthorn
Common hawthorn (*Crataegus monogyna*). **High.** The real hedgerow and pasture shrub, with red haws in autumn, in
place of the vanilla oak bush the meadows default to.
- **Grows in:**
  - hawthorn_bush: Field 55%, as its default; Shrubland 60%, as its default; Woodland 4%, in place of the oak bush.
  - hawthorn: Field 10%; a rare lone tree in the Highland.
  - The oak bush is cut to 10% or less, or removed, wherever the hawthorn bush grows.
- **Shape:**
  - hawthorn: bending trunk 3 (+1), bend_length 1, `min_height_for_leaves` 3, under a flat, dense blob (radius 2,
    offset 0, height 2). A lopsided, wind-bent thorn 4-5 blocks tall.
  - hawthorn_bush: one log under bush foliage (radius 2, offset 1, height 2).
- **Wood:** vanilla oak logs, as the apple tree has.
- **Leaves:** `hawthorn_leaves`, in small, dense clumps (about 3% gaps). One seed on a schedule of about
  [85, 280, 325].

  | Look | Colours | Haws |
  |---|---|---|
  | green | #16301a #1f4022 #2a522c #366638 #447a44 #5c925a. A dark, faintly blue green at hue 122, ΔE 19 from the apple's leaves and 14 from the chestnut's. | a few unripe haws (#6a7a2a), so it reads in summer |
  | autumn | #3c2e1a #544124 #6e552f #86683c #a48049 #b69768. A dull ochre-brown, ΔE 30 or more from every maple look. | three or four 2-pixel clusters of deep red (#8e1c18 #b8302a #d04a36), lit on top, standing out against the ochre |
  | winter | bare twigs in the `oak_twigs` ramp | a few haws hanging on |
- **Extras:** haws drop from the autumn and bare leaves, and at a low rate from green ones, so `seasons.mode=off`
  still yields them.
  - Haw Jelly: a preserve cooked into a Mason Jar in the Cooking Pot, beside Sweet Berry Jam.
  - Fox food (`#minecraft:fox_food`).
  - Compost.

  Haws are not a progression item. Players can plant hawthorn as hedges.
- **Note:** our leaves are coloured in their texture, so replacing tinted oak bushes drops the biomes' foliage tint,
  such as the Field's #6fa880, from those bushes.

#### rowan
Rowan, or mountain ash (*Sorbus aucuparia*). **High.** A cold-hardy tree whose red berries on bare twigs make snow
look better, in place of the frozen forest's temperate vanilla oak. The rowan is the old charm against witches, so it
also gives the mod's woods their first magic tie.
- **Grows in:**
  - Snowy Forest 36%, as its default, replacing vanilla oak. The aspen also joins at 20% in batch 1.
  - Lake District 10%.
  - A rare lone tree in the Highland.
  - The Snowy Forest has no seasons of its own, but leaves follow the server's season, so the rowans still go bare
    with berries in winter.
- **Shape:** forking trunk 4 (+2, +1) under an airy random_spread crown (radius 2, offset 0, foliage_height 3, 40
  attempts). A slim tree 6-8 blocks tall, often with two leaders.
- **Bark and wood:** maple's grey furrowed bark and rosy wood #cc9d71, close to rowan's grey bark and pinkish wood.
- **Leaves:** `rowan_leaves`. One seed on a schedule of about [100, 255, 305].

  | Look | Colours | Berries |
  |---|---|---|
  | green | #384619 #4f6123 #67802e #7e9b3c #99ba4d #aec577. A lighter, feathery yellow-green at hue 78, ΔE 12.5 from the maple's green. | scarlet clusters of 3-5 px (#8a1a0a #c4300f #e8521c, lit #f47a3a), from late summer |
  | autumn | #4f3810 #6e4e17 #90661e #ae7d29 #d09836 #d5ad67. A muted apricot, ΔE 17 or more from each maple look, so the scarlet berries stand out. | berries |
  | winter | bare twigs in the maple bark's colours | the berry clusters stay |
- **Extras:** rowan berries, picked like chestnuts. With seasons off, they ripen on a timer, so nothing is gated.
  - A small bitter food.
  - Rowan jelly in a Mason Jar.
  - The existing `jugcraft:candy_flavours/berry` tag.
  - The Aura Candle's existing Warding scent, through `jugcraft:candle_scents/warding`.

#### persimmon
American persimmon (*Diospyros virginiana*). **High.** It fulfils the biome roster's written promise of "persimmons"
in the Seasonal Forest. Its orange fruit stays on the bare twigs until it is picked, a late-autumn sight it shares only
with the rowan and hawthorn.
- **Grows in:** Seasonal Forest 10%, from the vanilla oak's share. In batch 1 the fir also joins the Seasonal Forest
  at 4%, as a plain pick, for the roster's "firs in the hills", since vanilla has no absolute-height filter.
- **Shape:** straight trunk 5 (+3) under a tall oval blob (radius 2, offset 0, height 4): a slim, upright fruit tree,
  narrower and taller than an oak.
- **Wood:** vanilla dark oak logs.
- **Leaves:** `persimmon_leaves`. One seed on a schedule of about [110, 280, 315].

  | Look | Colours | Fruit |
  |---|---|---|
  | green | #133016 #1b431e #245828 #2e6b33 #38823e #47994e. A glossy deep green, a little bluer and darker than oak, leafing out late. | none |
  | autumn | #3a1018 #561a22 #74262c #8e3634 #a84a3e #c06450. A wine-maroon, ΔE 15 from the maple's red and 35 or more from its orange and gold. | orange persimmons as lit 2×2 clusters (#b8561a #de7a2a #f0a050), 4-6 to a face, clear against the maroon |
  | winter | bare twigs in the `dark_oak_twigs` ramp | the fruit stays until picked |
- **Extras:** persimmons, picked by right-click; the tree is never cut. With seasons off, they ripen on a timer, as
  apples do.
  - Food.
  - A persimmon pie filling for the Hearth Oven, appended to the end of `PIES["fillings"]` and the `PieFilling` enum
    so existing pies keep their filling.
  - A persimmon preserve in a Mason Jar.
  - `#jugcraft:fermentable`.

#### elder
Elder (*Sambucus nigra*). The tree key is `elder` and the fruit is elderberries. **Medium.** The shrub-tree of damp
woodland edges and fen carr, with flowers and then dark berries.
- **Grows in:** Woodland 4%, in its new understory; Wetland 10%. The Elder Vale is not changed: its name means the
  old world, and its point is plain vanilla oaks.
- **Shape:** straight trunk 2 (+1) under a broad, flat-topped bush crown (radius 2, offset 1, height 2), wider than it
  is tall.
- **Bark and wood:** chestnut's grey-brown furrowed bark, which suits elder's corky bark. Chestnut's switch list already
  has biomes (batch 1, for the Orchard; rule 4).
- **Leaves:** `elder_leaves`, on about the willow's schedule.

  | Look | Colours | Trait |
  |---|---|---|
  | green | #26351e #364a2a #466137 #577645 #6a9055 #82a66e. A soft mid green, ΔE 9 from the hawthorn and 17 from the willow beside it. | flat clusters that ripen on random ticks: cream flower umbels (#caba6a #e0d9b6), then purple-black berries (#2d1932 #39233f #482c4e) |
  | autumn | #3d3d1b #565626 #707032 #898940 #a7a74f #b7b770. A dull olive-yellow, ΔE 28 from the willow's gold. | |
  | winter | bare twigs in the chestnut bark's colours | |
- **Extras:** elderberries, picked when ripe.
  - A snack.
  - Purple dye.
  - The existing `jugcraft:candy_flavours/berry` tag.
  - A witch's-cauldron floater (decor17 `FLOATERS`).

### Batch 6: evergreen scrub

#### juniper
The junipers: common juniper (*Juniperus communis*), Utah juniper (*J. osteosperma*) and eastern red cedar
(*J. virginiana*). **High.** One leaf block and three shapes reach 13 biomes, more than any other tree on the roster.
It turns the canyon and dryland into pinyon-juniper country, and replaces the lush oak in the withered Dead Forest
with a drought-hardy survivor.
- **Grows in:**

  | Biome | Share |
  |---|---|
  | Canyon | juniper 35%, juniper_bush 25% |
  | Dryland | juniper 30%, juniper_bush 25% |
  | Xeric Shrubland | juniper 25% |
  | Dead Forest | juniper 10%, replacing vanilla oak |
  | Scrubland | juniper 15% |
  | Cold Desert | a rare lone juniper on its coarse-dirt patches, about one chunk in six |
  | Field | juniper_column 30%, replacing vanilla spruce as the record's "small spruces" |
  | Prairie | juniper_column 15%, as eastern red cedar |
  | Heathland | juniper_bush 15% |
  | Shrubland | juniper_bush 10% |
  | Steppe | juniper_bush 20% |
  | Tundra | juniper_bush 10% |
  | Alpine Spawn | juniper_bush 10% |
- **Shape:** all three forms survive as a dead bush would, so they stand on sand, red sand, coarse dirt and
  terracotta as well as grass.
  - juniper: forking trunk 2 (+2, +1) under random_spread foliage (radius 2, offset 0, foliage_height 3, 40
    attempts). Short, crooked and ragged, 4-6 blocks.
  - juniper_column: straight trunk 4 (+2) under spruce foliage (radius [1, 2], offset [0, 1], trunk_height [0, 1]). A
    shaggy flame, leafy from the ground up, 5-7 blocks.
  - juniper_bush: one log under bush foliage (radius 2, offset 0, height 1). A low mat 5 wide and 1-2 high.
- **Bark and wood:** cypress's stringy red-brown bark and wood #916558, since junipers belong to the cypress family.
- **Leaves:** `juniper_needles`, among the densest. A dusty grey-blue-green at hue 187 and saturation 0.17:
  #1e2a2e #27373b #324548 #3f5558 #4f676a #688487.
  - That is greyer and bluer than the fir, much lighter than the cypress and well clear of the eucalyptus.
  - Its one trait is frosted indigo berries as single and paired pixels (#2e3a66 #3a4a6e), each with a pale #9aaac4
    bloom pixel.
  - Evergreen.
- **Wood:** reuses cypress logs, with new needles and a sapling. Its sapling takes sand.
- **Extras:** juniper berries, picked like chestnuts or dropped from broken needles as apples drop from oak leaves.
  They are never required.
  - An alternative Mulling Spices recipe, with juniper berries in place of one ingredient.
  - Fox food.
  - Compost.

#### gorse
Common gorse, or furze (*Ulex europaeus*). **High.** The heath's signature shrub: yellow drifts over brown grass
beside the magenta heather the biome already has.
- **Grows in:** Heathland 60%, as its default; Shrubland 20%.
- **Shape:** tight, lumpy clumps round one hidden oak log: bush foliage (radius 2, offset 0, height 2), 3-5 wide and 2
  high.
- **Leaves:** `gorse_leaves`, blossom kind, evergreen and always in flower.
  - A dark spiny green field: #14200e #1c2c14 #253a1a #2e4721 #385629 #446633.
  - Lemon-yellow flower clumps on about 35% of the face: #927e1c #b09927 #d2b834, highlight #d7c465. Hue 50,
    saturation capped at 0.65, so the drifts do not read as yellow wool.
  - The wattle is egg-yolk gold at hue 42, so the two yellow shrubs differ.
- **Wood:** none. One vanilla oak log hidden inside is the stem, as in the oak bush. It has its own leaves and a
  sapling, so players can plant gorse hedges.
- **Extras:** its leaves join `#minecraft:flowers`, so bees work them, as vanilla's flowering azalea leaves do.

#### sagebrush
Big sagebrush (*Artemisia tridentata*). **Medium.** It turns the bare steppe into a believable shrub-steppe while
keeping it open and low.
- **Grows in:** Steppe, which gets a trees slot of 1-2 a chunk: sagebrush 80%, juniper_bush 20%, nothing over two
  blocks. The wild apple patch already reaches the Steppe through `c:is_plains`. Remove the Steppe from that patch if
  it must stay low, or accept the apples.
- **Shape:** one dead-wood log as the stem, grey like sage's shreddy stems, under bush foliage (radius 1, offset 0,
  height 2). 3 wide and 1-2 tall, smaller than every other bush in the mod.
- **Leaves:** `sagebrush_leaves`, in fine one-pixel clumps of silvery sage: #3e463a #535c4e #697262 #80897a #9aa392
  #b4bca8. Evergreen. It never shares a biome with the olive bush, its near-twin in colour.
- **Wood:** none new. Its sapling grows one dead-wood log, which makes dead wood renewable in the Overworld.
- **Extras:** compost. It could later become an Aura Candle scent.

#### holly
European holly (*Ilex aquifolium*). **Medium.** It keeps the oak woods green under winter snow and ties world
generation to the planned December content.
- **Grows in:** Woodland 4%; Dense Forest 5%, from the oak bush's share.
- **Shape:** straight trunk 3 (+2) under blob foliage (radius 2, offset 0, height 4). An upright broadleaf oval 5-7
  blocks tall. It is not a spruce-placer cone, which would read as a small fir.
- **Bark and wood:** vanilla pale oak logs: grey bark, and near-white wood like holly's ivory wood.
- **Leaves:** `holly_leaves`, dense and glossy: #17340f #204915 #29601c #347425 #408e2d #50a63b.
  - The hue is 109, a little yellower than the mahogany, with more light specks than other leaves.
  - Its one trait is small scarlet berry clusters (#8a1414 #c21f1f #e04a3a).
  - It is evergreen, but uses the season state for its berries, on a schedule of about [100, 300, 335]. The gold and
    bare states are the same leaves with berries.
  - The green state shows berries on about one block in three, picked by position, so a server with seasons off
    still sees them.
- **Extras:** holly sprigs, taken with shears. Holly grows all year, so nothing depends on the event window.
  - They make the **Holly Wreath**, a holly variant of the existing Autumn Wreath block, in the same PR, so the sprigs
    have a consumer from the start.
  - They are decoration only and never gate anything.

### Batch 7: the olive

#### olive
Olive (*Olea europaea*), wild and cultivated. **High.** The defining tree of the Mediterranean. It fills the biggest
stand-in gap in the warm, dry lands, where only the cypress reads as Mediterranean. It has its own wood and leaves,
the existing pattern, and so needs no shared change.
- **Grows in:**
  - Mediterranean Forest: olive 25% and olive_bush 15%, replacing vanilla dark oak, part of the vanilla oak, and the
    oak bush.
  - Lavender Field: olive_bees 30%, replacing most of vanilla's oak with bees, which keeps 10%. The cypress also
    joins at 10% in batch 2, and the biome's `winter_snow` goes off (Provence).
  - Scrubland: olive_bush 60%, as its default.
  - It is not placed in the snowy Orchard or the Mojave-like Xeric Shrubland.
- **Shape:**
  - olive: forking trunk 3 (+1, +2) under a wide, ragged, cloud-like random_spread crown (radius 3, offset 0,
    foliage_height 2, 45 attempts). A short, thick trunk that leans and splits low; 5-7 blocks, wider than tall.
  - olive_bees: the same, with vanilla's beehive decorator (0.05).
  - ancient_olive, which four saplings in a square grow: a squat giant trunk 3 (+1, +1) under random_spread (radius
    4, foliage_height 2, 90 attempts). It is not vanilla dark oak's placers, which would grow back the silhouette the
    olive replaces.
  - olive_bush: one log under bush foliage (radius 2, offset 1, height 2).
- **Bark:** silver-grey with a warm green cast: #474a3b #616451 #7b7f66 #93987e #aaad99 (hue 70, saturation 0.11).
  It is furrowed, with the standard one-pixel wander. That is ΔE 15 from the dead wood's bark and 10 from the palm's.
- **Wood:** the owner's first-set painting 3, #ada358 (painting 19 is the same colour, so only one is used).
  - A darker, greener yellow at hue 53 and lightness 0.51 than the willow (#c7c785) or the chestnut.
  - Its one trait is a figure of 2-3 straight, broken streaks drawn in the wood ramp's own two darkest tones,
    hue-shifted no more than 10 degrees toward brown, with a one-pixel jog.
  - **Check it beside vanilla's bamboo planks and stripped bamboo** (about hue 49, the nearest vanilla wood), as well
    as crimson and warped planks, birch, oak, the willow and the chestnut. Record the hue and lightness distances.
  - If it reads like bamboo, darken it or move its hue toward 58-62 with the owner's approval.
  - Fallback: the olive's leaves and fruit on willow logs.
  - This survey also proposes adding bamboo, crimson and warped to rule 3's list in `NATURAL_TEXTURES.md`.
- **Leaves:** `olive_leaves`, narrow and plainly silver: #424a37 #576249 #6d7b5b #83926f #9ba88b #b4bca9.
  - Middle tones at lightness 0.42-0.57 and saturation 0.14, the lightest tone silvery for the leaf undersides.
  - Its one trait is olive clusters drawn as the chestnut's burs are: green (#6a7a2a #8a9a3a), then ripe purple-black
    (#3a2a3a #5a3f55).
  - Evergreen.
- **Wood set:** NEW wood set "olive": log, wood, stripped log and wood, planks, stairs, slab, fence and fence gate.
- **Extras:** olives, picked by right-click from ripe leaves; the tree is never cut.
  - A small food.
  - Pickled olives in a Mason Jar, with cider vinegar, as the pickled beets are.
  - When agriculture slice 5 adds the shared plant oil, olives press into that same fluid, not an olive-only oil.
  - olive_bees keeps the Lavender Field's bees.
  - Wild olive bushes bear no fruit, which keeps the harvest on the tree.

### Batch 8: dry country

#### stone_pine
The nut pines: stone or umbrella pine (*Pinus pinea*), Swiss stone pine (*P. cembra*) and pinyon (*P. edulis*).
**High.** It merges four pine proposals into one needle block. It replaces vanilla's taiga pine in the dry and heath
biomes and vanilla oak in the Mediterranean, where flat parasols rise above dark cypress columns. It also gives the
start biome a second tree of its own, and a food.
- **Grows in:**
  - umbrella_pine: Mediterranean Forest 15%, replacing most of the vanilla oak; Heathland 25%, replacing vanilla pine
    as the flat-crowned Scots pine.
  - stone_pine: Alpine Spawn 20%. Its table becomes larch 50, stone pine 20, spruce (v) 20, juniper_bush 10, and
    `alpine.TREES` becomes a picks list, with `alpine_data.worldgen` changed to match.
  - pinyon: Dryland 45%, as its default; Canyon 40%, as its default, both replacing vanilla pine.
  - It is not placed on the all-sand Dune Beach, which keeps its "no trees".
- **Shape:**
  - umbrella_pine: forking trunk 7 (+2, +1) under acacia foliage (radius 3, offset 0). A tall, clean trunk forking at
    the top into a wide, flat parasol 9-11 blocks tall and 7-9 across.
  - stone_pine: straight trunk 4 (+4) under a blob (radius 2, offset 0, height 4) that comes low down the trunk. A
    dense, round-crowned nut pine 6-10 blocks tall, the Alps' Zirbe.
  - pinyon: straight trunk 3 (+2) under a blob (radius 2, offset 0, height 3), 4-6 blocks. It survives as a dead
    bush would, so it stands on canyon terracotta.
- **Bark and wood:** larch's red-brown plated bark, which reads as a pine's orange plates, and amber wood #c98634.
  Larch's switch list now covers alpine_spawn and biomes (rule 4).
- **Leaves:** `stone_pine_needles`, dense, evergreen, one look all year: #232f20 #31422d #40573b #4f6a4a #61825a
  #779970.
  - A dusty grey-green at hue 111 and saturation 0.18: ΔE 12 from the cypress beside it in the Mediterranean, and 17
    from the olive.
  - Its one trait is small 2-pixel cones lit on top (#5a3a20 #7a5230 #9a6e44).
  - The larch's own needles turn gold and fall, so they cannot be reused.
- **Wood:** reuses larch logs, with new needles and a sapling.
- **Extras:** pine nuts from ripe cones, picked by right-click like chestnuts. They give an early food at Alpine
  Spawn, where every player starts, and ripening is not seasonal.
  - They roast at a campfire into roasted pine nuts.
  - They join `#jugcraft:squirrel_food`.

#### joshua_tree
Joshua tree (*Yucca brevifolia*). **High.** The desert tree people picture, on the red dunes of the Lush Desert.
- **Grows in:** Lush Desert 35%, beside the desert acacia and the small palm from batch 2; Xeric Shrubland 30%, which
  becomes Mojave and Great Basin shrubland with juniper and acacia.
- **Shape:** vanilla's cherry trunk placer, with blob foliage (radius 1, offset 0, height 2) at each arm's tip. A short
  trunk throwing two or three upturned arms, each ending in a spiky rosette, 4-6 blocks.
  - base 3 (+1), branch_count [2, 3];
  - branch_horizontal_length 2: vanilla's codec does not allow less than 2, and `check_mod_data` gains a bounds check
    for every cherry-placer tree;
  - branch_start_offset_from_top [-2, -1], branch_end_offset_from_top [0, 1].

  It survives as a dead bush would, and its sapling takes sand, as the palm's does.
- **Bark and wood:** palm's grey-brown furrowed bark, which reads as a yucca's shaggy, fibrous trunk, and cream wood.
- **Leaves:** `joshua_tree_fronds`, fronds kind, drawn as short, steep 4-5 pixel spikes, among the most open:
  #33371f #484d2b #5e6439 #747a48 #8d9558 #a2aa74.
  - A fresher yucca green at hue 67 and saturation 0.26.
  - It is ΔE 31 from the juniper beside it.
  - Evergreen.
- **Wood:** reuses palm logs, since palms and yuccas are both fibrous-trunked monocots, with new fronds and a sapling.

#### wattle
Golden wattle (*Acacia pycnantha*). **High.** It replaces vanilla-oak scrub with the real Australian understory, and
makes the Gilded Shrubland's gold its own instead of a tint on vanilla oak leaves.
- **Grows in:**
  - Eucalyptus Forest: wattle_bush 15%, replacing the oak bush.
  - Gilded Shrubland: wattle_bush 70% and wattle 30%. The oak bush is removed there; it is the same shape in the
    biome's same gold tint, and the two could not be told apart.
- **Shape:**
  - wattle: forking trunk 2 (+1, +1) under a blob (radius 2, offset 0, height 3). A low, rounded tree 4-6 blocks
    tall.
  - wattle_bush: one log under bush foliage (radius 2, offset 1, height 2).
- **Wood:** vanilla acacia logs, the wattle's own genus.
- **Leaves:** `wattle_leaves`, blossom kind, dense. In bloom all year.
  - Egg-yolk golden puffs: #997520 #b98f2b #d3a844, highlight #dabc77; hue 42.
  - Grey-green phyllodes (#546142 #6e7c5a) show on at least 35% of the face, so it reads as blossom over leaves.
- **Extras:** none needed. The blossom could give yellow dye if the owner wants a use.

#### tamarisk
Tamarisk, or salt cedar (*Tamarix*). **Low.** The one living tree that belongs on a salt flat, and the archetypal
desert-river tree.
- **Grows in:**
  - Wasteland 15%. Its count rises from 0-1 to 1 try a chunk, so its trees are seen.
  - Dry River 40% (batch 10 adds the date palm). It gets 1 try a chunk on its dry banks.
  - Oasis 10%.
- **Shape:** forking trunk 2 (+1, +1) under random_spread foliage (radius 2, offset 0, foliage_height 3, 40 attempts).
  A low, many-stemmed, feathery tree 4-6 blocks tall, loose rather than vanilla cherry's compact ball. It survives as
  a dead bush would, which keeps it to coarse dirt in the Wasteland and to sand elsewhere.
- **Bark and wood:** jacaranda's grey-brown bark and the owner's mauve wood #7a5a5e, near tamarisk's pinkish wood.
- **Leaves:** `tamarisk_leaves`, blossom kind, mostly leaf. Evergreen.
  - A feathery grey-green field: #222d24 #303f32 #3f5342 #4e6652 #607c64 #76937a.
  - Sparse, dusty mauve-pink plumes (#7e5a6a #a07888 #c09aa8) on no more than 25% of the face, so it never reads as a
    misplaced cherry.
- **Wood:** reuses jacaranda logs, with new leaves and a sapling.

### Batch 9: giant fungi, the Nether and the End

Settle these together:
- `count_on_every_layer` placement under the Nether roof;
- one `huge_fungus` feature for each floor block;
- End saplings that grow without sky light, on end stone;
- the block the tree leaves under its trunk on end stone;
- the no-gain-loop arithmetic for bone meal and composting.

#### huge_glowcap
An invented giant of the mod's own glowcap, after vanilla's huge mushrooms. **High.** With one block, five biomes
across the Overworld, the Nether and the End get a giant of the mod's own fungus. Its cold light reads well against
the Nether's red haze.
- **Grows in:**

  | Biome | How it is placed |
  |---|---|
  | Fungal Thicket | about 2 a layer on nylium and mycelium, among the crimson fungi, which stay as the canopy |
  | Glowcap Grotto | 2-4 tries a chunk as a **cave extra**, with the same `environment_scan` placement as `grotto_glowcaps`; the surface trees field could never place in a cave biome. It fails where there is less than about 10 blocks of headroom. |
  | Toadstool Field | about 1 a chunk on mycelium, beside vanilla's huge mushrooms, which stay |
  | Mycelial Jungle | 7%, from the jungle bush's share |
  | Ender Wilds | 5%, on moss |
- **Shape:** vanilla's huge mushroom features in two forms, a domed one (`huge_red_mushroom`, cap radius 2-3 on a stem
  5-9 tall) and a parasol (`huge_brown_mushroom`, radius 3). The cap is a new `glowcap_block`; the stem is vanilla's
  mushroom stem, placed as it is.
- **Cap:** a huge-mushroom block with six face states, opaque, with no outlines, tiling seamlessly.
  - Its outer face is drawn in vanilla's mushroom-block manner, as soft 2-4 pixel blotches in three tones, not the
    leaves' speckle: #1d5a6e and #2a8aa0 for the field, #3fbfd0 for small spots.
  - About ten #79e6ee and #c4fbff glints.
  - Its inner face shows pale grey-green pores (#8a9a8c #b0bcb0 #d6e2d2).
  - Light 8, below the plant's 10, so a cap reads as a soft lamp.
- **Extras:**
  - Cap blocks drop a glowcap at about 15% each, which is about 7 per giant of about 45 cap blocks.
  - `glowcap_block` itself does not compost. The glowcap keeps its existing compost value.
  - Arithmetic: 7 glowcaps at a 0.65 compost chance make about 0.65 bone meal, from 1 bone meal and 1 glowcap spent,
    so bone meal never comes back with a gain. This goes into the record.
  - Glowcaps become renewable light.
  - Silk touch gives the cap as a soft cyan light block for builders.
  - A glowcap grows into a giant with bone meal, in low light, on mycelium, moss, mud or nylium.

#### blight_fungus
An invented dead, blighted huge fungus, after the dead trees standing in Deadvlei's dune pan. **High.** The Nether has
no Jugcraft growth at all. This gives the Withered Hollow and the Blighted Sands the dead trees their names refer to,
in vanilla's own Nether-tree form, and links the End's rot to the Nether through the soul soil both share.
- **Grows in:**
  - Blighted Sands: about 1 a layer on soul soil. It is the commonest biome in this group, at 337 of 16,641 Nether
    samples.
  - Withered Hollow: about 3 a layer on blackstone.
  - Rotted Expanse: its own extra on the soul-soil patches. It is not a trees pick, which would fail on the end stone
    five times in six and remove dead trees for nothing.
- **Shape:** vanilla's `huge_fungus` feature, one configured feature per floor because the feature takes one
  `valid_base_block` (soul soil or blackstone).
  - A straight stem 4-13 blocks tall, now and then twice that, under a rounded cap that hangs 2-3 blocks down round
    the stem, with ragged gaps.
  - `decor_state` is the wart itself, so there are no shroomlights and the Hollow stays dark.
- **Stem and cap:**
  - Stem: dead wood's existing grey log.
  - Cap: a new `blight_wart_block`, opaque like vanilla's wart blocks, drawn as mottled blotches in four tones of dry
    charcoal-mauve (#1e1a1e #2c262c #3c343a #4e444c), with about ten pale ash specks (#9a9296).
- **Extras:**
  - Blight wart blocks drop a small blight fungus at about 5%, so the fungus reproduces. That is about 2-3 per huge
    fungus, roughly 0.25 bone meal's worth of compost, below the one bone meal that grew it.
  - The small fungus grows wild on soul soil, and bone meal there grows a huge one, so dead wood is renewable from
    the first Nether trip. The sagebrush already makes it renewable in the Overworld.
  - Wart blocks do not compost. Nothing requires them. A Cursing or Necromancy school could use them later.

#### marrow_fungus
An invented huge fungus with a bone stalk and a nether-wart cap. **Medium.** It turns the heap's own two floor
materials into its growth, a silhouette no other biome has, for the cost of one configured feature.
- **Grows in:** Marrow Heap, about 1 a layer on its nether wart block patches. The bone spires stay as its smaller
  verticals.
- **Shape:** vanilla's `huge_fungus`, built only of vanilla blocks placed by reference: stem `minecraft:bone_block`
  (axis y), hat `minecraft:nether_wart_block`, decor `minecraft:shroomlight`, base `minecraft:nether_wart_block`. A
  pale bone stalk 4-13 blocks tall under a domed red wart cap with a few shroomlights.
- **Extras:** none. It stays worldgen-only, with no sapling and no bone-meal growth, because a planted one would turn
  one bone meal into a stalk of bone blocks.

#### bramble_bush
An invented thicket of the mod's existing Nether bramble. **High.** It fills the empty head-height layer between the
one-block brambles and the tall warped fungi, which makes the "brush" in Netherbrush real.
- **Grows in:** Netherbrush, about 4 a layer on nylium; Blighted Sands, about 2 a layer on soul sand and soul soil.
- **Shape:** vanilla's bush foliage (radius 2, offset 1, height 2) round one hidden crimson stem: a low, thorny dome
  2-3 tall and about 5 wide.
- **Leaves:** `bramble_bush_leaves`, dense. Not flammable, and no seasonal looks.
  - The bramble plant's dark-red cane colours, stepped into six tones: #2f130e #421a14 #56221a #682c23 #7f362a
    #954437.
  - Its one trait is thorns drawn as 2-pixel slanted marks, with a mid step (#a8705a) under a pale tip (#c8a888). Only
    4-5 pixels take the palest tone, on clump tops.
- **Extras:** its leaves drop sticks and the odd bramble. There is no sapling. Bone meal on a bramble on nylium or soul
  soil grows a bush, as bone meal on a fern grows a large fern. The drops stay below one bone meal's worth.

#### dragonblood
Invented, after the dragon's blood tree of Socotra (*Dracaena cinnabari*). **Medium.** The End gets a tree of its own
in place of two Overworld stand-ins. Its teal umbrellas sit well among violet jacarandas, purple chorus and pale end
stone.
- **Grows in:**
  - Ender Wilds 45%, as its default; the jacaranda stays at 30%. dragonblood_shrub takes 20%, replacing vanilla's
    azalea tree.
  - Outer Flats: a lone landmark on end stone only, about one chunk in ten, as a rare extra.
- **Shape:**
  - dragonblood: vanilla's cherry trunk placer, base 4 (+1), branch_count [2, 3], branch_horizontal_length [2, 3],
    with branches leaving 2-3 below the top. Acacia foliage (radius 2, offset 0) at each tip, so the flat pads meet in
    one canopy: a short, stout trunk splitting into upturned branches under one dense, flat umbrella.
  - dragonblood_shrub: one log under bush foliage (radius 2, offset 1, height 2).
- **Bark and wood:** palm's grey-brown bark, and pale cream wood that matches end stone. Dracaenas are monocots, like
  palms and yuccas.
- **Leaves:** `dragonblood_fronds`, fronds kind, drawn as dense, stiff 6-pixel blades: #102a2a #173836 #1f4744
  #295752 #346861 #457c74.
  - A deep pearl-teal at hue 173: darker and bluer than the eucalyptus, duller and greener than warped wart.
  - Evergreen.
- **Sapling:** survives on end stone and moss, and grows without sky light, as chorus does. The tree keeps end stone
  under its trunk, so check what `minecraft:soil_beneath_tree` does there.
- **Extras:** dragon's blood, a rare red resin dropped by its fronds, usable as red dye. It can only be reached after
  the dragon, so nothing in core progression may require it.

### Batch 10: hanging fruit

#### baobab
African baobab and Australian boab (*Adansonia digitata*, *A. gregorii*). **High.** The outback's icon, a silhouette no
vanilla tree has. Today the Outback's only tree is a generic acacia.
- **Grows in:** Outback: baobab 10%, giant_baobab 10%. A lone giant in the Lush Savanna is offered as the owner's
  call, under "Biomes by biome". It is not placed in the Dryland, which becomes American pinyon-juniper country.
- **Shape:**
  - baobab, the young tree a sapling grows: straight trunk 5 (+2) under acacia foliage (radius 2, offset 0).
  - giant_baobab, which four saplings in a square grow: a fat bottle trunk two blocks wide from vanilla's giant trunk
    placer, 6 (+2, +1), 6-9 blocks, under a sparse, stubby random_spread crown (radius 3, foliage_height 2, 40
    attempts).
  - Shoot both forms beside the desert acacia and the small palm.
- **Bark and wood:** palm's grey-brown bark and pale cream wood.
- **Leaves:** `baobab_leaves`, a fresh green, among the most open (about 6.5% gaps), dark beneath: #243e18 #335721
  #43722c #548a38 #66a845 #80ba63. Evergreen.
- **Sapling:** grows on sand and red sand (it survives as a dead bush would).
- **Pods:** a new hanging pod block in velvety olive-grey-green (#6e7056 #8a8c6c #a6a886), two pixels wide and 4-5
  tall on a one-pixel stalk, lit on top. Vanilla's `attached_to_leaves` decorator hangs them under the crown (about
  0.1, direction down), as the mangrove hangs propagules.
- **Wood:** reuses palm logs, with new leaves, a sapling and the pod block. The giant check keys on the tree's log wood
  (batch 4).
- **Extras:** baobab fruit from ripe pods, a small food. Its seeds plant the sapling, as a chestnut does.

#### date_palm
Date palm (*Phoenix dactylifera*). **Medium.** Date palms define an oasis, and they give players a renewable desert
food and a reason to visit.
- **Grows in:** Oasis 35%, beside the palm and the small palm (15%, added in batch 2); Dry River 60%, with the
  tamarisk.
- **Shape:** straight trunk 8 (+3), ramrod-straight, under cherry foliage (radius 2, offset 0, height 2,
  wide_bottom_layer_hole_chance 0.5, corner_hole_chance 0.5, hanging_leaves_chance 0.6,
  hanging_leaves_extension_chance 0.3). A full, flattened, drooping crown, unlike the palm's bend and flat spray.
  Compare it with the palm and the tree fern in game before settling it.
- **Wood and leaves:** palm's wood and fronds. Shape only, plus the date cluster block. It grows on sand.
- **Date clusters:** a new hanging block under the crown, placed by `attached_to_leaves` (about 0.2, direction down).
  Unripe clusters are golden (#c89a2a #e0bc48); ripe ones are amber-brown (#7a3e18 #a45a24 #c87a38) on a #d0a040
  stalk, drawn as a plant sprite lit from above.
- **Extras:** dates ripen on random ticks. A right-click picks 2-4 and leaves the cluster unripe again, as the
  chestnut's burs do. Dates are a sweet food and join `#jugcraft:fermentable` at the existing bioethanol rate, so no
  new conversion is added. The palm sapling could grow a date palm some of the time, through vanilla's secondary tree
  in `TreeGrower`.

### Batch 11: tropics, subtropics and coasts

#### flame_tree
Flame tree, or royal poinciana (*Delonix regia*). **Medium.** A flowering tree of the tropics. In the Jacaranda Glade
its scarlet against the jacaranda's violet is the classic subtropical pairing.
- **Grows in:** Tropics 10%, replacing vanilla's azalea tree, since azaleas are not lowland tropical trees; Jacaranda
  Glade 8%, from the vanilla cherry's share.
- **Shape:** forking trunk 4 (+1, +1) under acacia foliage (radius 3, offset 0). A low, very wide, flat umbrella 6-8
  blocks tall and about 9 across, lower and wider than the jacaranda.
- **Bark and wood:** jacaranda's grey-brown furrowed bark and the owner's mauve wood.
- **Leaves:** `flame_tree_leaves`, blossom kind, dense. In bloom all year.
  - Scarlet-orange flower clumps: #4a1c0e #682814 #89341a #a54224 #c9512c #d37255, at hue 14 and saturation 0.64.
  - Feathery fern green at the edges (#3a6a2a #4c8236).
- **Wood:** reuses jacaranda logs, with new leaves and a sapling.
- **Extras:** none. The blossom could give red or orange dye.

#### mango
Mango (*Mangifera indica*). **Medium.** Every tree in the Lush Grassland is a vanilla wood today. The mango gives it,
and its villages, a tree of their own and a warm-climate fruit.
- **Grows in:** Lush Grassland 20%, replacing vanilla's jungle tree. The small palm joins it at 10% in batch 2, and
  the banana becomes its default in batch 12.
- **Shape:** straight trunk 4 (+2) under a big, deep, dense dome (blob radius 3, offset 0, height 4). A stout fruit
  tree 7-9 blocks tall and 7 wide.
- **Wood:** vanilla jungle logs.
- **Leaves:** `mango_leaves`, dense (about 2% gaps), a dark glossy green: #0f2b11 #153d17 #1b4f1f #246028 #2b7530
  #388a3d.
  - It fruits in the apple's cycle (the `AppleLeavesBlock` pattern): plain leaves, then a few cream-pink flower sprays
    (#d8b8a8 #e4ccc0), then three or four 2×3-pixel mangoes.
  - The mangoes go from green-gold to orange-red: #5a8a2a, #e0a428, #d0582a.
  - Evergreen.
- **Wood:** reuses vanilla jungle logs, with new fruiting leaves and a sapling.
- **Extras:** mangoes, picked 1-2 at a time from ripe leaves.
  - Food.
  - Mango chutney in a Mason Jar, with cider vinegar.
  - `#minecraft:parrot_food`.
  - `#jugcraft:fermentable`.

#### sea_grape
Sea grape (*Coccoloba uvifera*). **Medium.** The real shore shrub of warm dunes, in place of vanilla-oak scrub on the
warmest beach, and it gives fruit.
- **Grows in:** Overgrown Beach 40%, as its default. The palm and the small palm join at 20% each in batch 2, and the
  pandanus at 20%.
- **Shape:**
  - sea_grape: a hidden log under bush foliage (radius 2, offset 1, height 2), a sprawling shore shrub 2-3 blocks
    tall.
  - sea_grape_tree, now and then: straight trunk 2 under a blob (radius 2, height 2).
  - Both survive as a dead bush would, so they stand on sand.
- **Leaves:** `sea_grape_leaves`, in larger two-pixel clumps for its round, leathery leaves, an olive green: #2a321a
  #3b4725 #4e5c30 #5f713d #748a4a #8ba35c.
  - Its one trait is hanging grape clusters, green (#6a8a3a #8aa34a) and then purple when ripe (#3e1e42 #5a2d5e, lit
    #7a4a7e).
  - Evergreen.
- **Wood:** none new; a hidden vanilla oak log. Its own fruiting leaves and a sapling.
- **Extras:** sea grapes, picked ripe while the bush stays.
  - Food.
  - Sea grape jelly in a Mason Jar.
  - `#jugcraft:fermentable`.

#### banyan
Banyan (*Ficus benghalensis*). **Medium.** A rooted, many-trunked sacred fig beside the great oaks and the warm
calcite pools makes the springs read as a shrine of the tropics.
- **Grows in:** Shrine Springs 14%, replacing vanilla oak. The great oak stays the default.
- **Shape:** vanilla mangrove's `upwards_branching` trunk, 6-9 blocks, branching as it climbs.
  - Vanilla's `mangrove_root_placer` flares its foot narrowly, with max_root_width 1-2, rather than arching like a
    mangrove.
  - A wide, low dome of blob foliage (radius 5, offset 0, height 3), with trunk vines.
  - Its defining look is vanilla's `hanging_roots`, hung under the crown by `attached_to_leaves` (direction down,
    about 0.15) as aerial roots.
- **Wood:** vanilla jungle logs.
- **Leaves:** `banyan_leaves`, a glossy dark green with more two-pixel clumps and about 3% gaps: #173113 #20441a
  #2a5922 #356c2c #408436 #509c45. Its one trait is a few small red figs drawn on the leaves (#7d2929 #9c3e3e
  #bb5555), as decoration only. There is no fig item, so the IDs stay free for a real fig tree. Evergreen.
- **Extras:** none.

#### pandanus
Screw pine (*Pandanus tectorius*). **Low.** It gives the warm shores a striking silhouette at no block cost.
- **Grows in:** Overgrown Beach 20%; Tropics 10%.
- **Shape:**
  - Trunk: vanilla's bending trunk 4 (+2), bend_length 1, with `min_height_for_leaves` above the trunk, so only the
    bend carries a tuft.
  - Roots: vanilla's `mangrove_root_placer` lifts it, with palm logs as the roots (trunk_offset_y 2, max_root_width 3,
    max_root_length 3), so stilt roots splay down to the sand.
  - Crown: a tuft of palm fronds (acacia foliage, radius 1).
  - A leaning shore tree 5-7 blocks tall that grows on sand.
- **Wood:** reuses palm, worldgen only, with the root placer from batch 4.

#### osmanthus
Sweet osmanthus (*Osmanthus fragrans*). **Low.** Guilin, the archetypal karst landscape, means "forest of sweet
osmanthus". It gives the pinnacles a broadleaf of their own and ties them to the mod's Mid-Autumn foods.
- **Grows in:** Karst Pinnacles 15%, from the spruce bush's share.
- **Shape:** forking trunk 2 (+1, +1) under random_spread foliage (radius 2, foliage_height 3, 50 attempts). A
  multi-stemmed shrub-tree 4-6 blocks tall, a silhouette of its own rather than the orange and apple trees' ball.
- **Wood:** vanilla oak logs.
- **Leaves:** `osmanthus_leaves`, a dark glossy green: #162a14 #1e3a1b #284c24 #325d2e #3e7138 #4c8646.
  - Its one trait is tiny pale cream-gold flowers as single pixels (#e8d08a #f2e2a8), tucked among the leaves. They
    are sparse enough that the tree reads green from afar and gold up close, never orange.
  - Evergreen, in flower all year.
- **Extras:** osmanthus flowers.
  - A Candy Kettle flavour with **luck**, an effect no flavour uses yet.
  - An osmanthus mooncake filling, beside the existing mooncakes and lantern festival.

#### driftwood
Bleached driftwood logs washed ashore. **Low.** It gives the bare shores, which stay treeless, a natural detail and a
little wood.
- **Grows in:** Dune Beach, on the strand, where the sand gives contrast; Gravel Beach, more rarely.
- **Shape:** a small Jugcraft feature, written in Java, that lays 3-6 *stripped* dead logs flat on sand or gravel just
  above the waterline, with no stump. Vanilla's `fallen_tree` always places a stump, and bleached driftwood has lost
  its bark.
- **Wood:** reuses stripped dead logs. No new blocks.

### Batch 12: rainforest

This batch follows agriculture slice 5, which already plans the rubber tree and its tapping.

#### rubber_tree
Pará rubber tree (*Hevea brasiliensis*). **High.** `docs/features/rubber.md` names natural rubber trees as a planned
addition, and `AGRICULTURE.md` slice 5 plans the tree with tapping. **Slice 5 builds it**: the log, the leaves, the
sapling and the tree tap. This roster only places it, as it does the slice-4 fruit trees.
- **Grows in:** Rainforest 12%, replacing vanilla's jungle tree.
- **Shape:** straight trunk 9 (+3) under blob foliage (radius 2, offset 0, height 4), with leaf vines at 0.1. A tall,
  clean trunk with a small oval crown high up.
- **Bark:** a new `rubber_log`, smooth grey-brown and furrowed with only three shallow furrows: #434137 #585548
  #6d695a #827e6b #95917e.
  - Its one trait is a few short cream latex drips (#e6dcc4, 2-3 px, vertical) on the ridges.
  - No bands. Its end rings are a pale pinkish tan.
- **Leaves:** `rubber_leaves`, a glossy mid green, dense (about 3% gaps): #1e3914 #29501b #366824 #447f2f #539b39
  #66b54a. Evergreen.
- **Wood:** no wood set.
  - `rubber_log` strips to vanilla's stripped jungle log and saws into jungle planks.
  - It is in `#minecraft:logs`, so its leaves do not decay, but not in `#minecraft:logs_that_burn`.
- **Extras (slice 5):** latex from a tree tap on a `rubber_log`.
  - The tap fills slowly on random or scheduled ticks, not a per-tick block entity, and is capped.
  - Latex set with cider vinegar makes `jugcraft:rubber`, the item the polymerization reactor already makes.
  - The route stays slower than the naphtha route, with its rate and losses written down.

#### banana
Banana (*Musa acuminata*). **High.** The iconic plant of tropical gardens and forest edges, in place of vanilla jungle
bushes, and it gives food.
- **Grows in:** Tropics 15%; Rainforest 10%; Lush Grassland 45%. It replaces vanilla's jungle bush, and is the Lush
  Grassland's default.
- **Shape:** straight trunk 2 (+1) of a new `banana_stem`, under a broad, drooping crown of cherry foliage (radius 2,
  offset 0, height 2, hanging_leaves_chance 0.5, hanging_leaves_extension_chance 0.2, corner_hole_chance 0.5). A
  soft-stemmed plant 4-5 blocks tall, clearly not the small palm's tuft.
- **Stem:** streaked, on a green-brown ramp (#474b25 #5e6331 #757b3d #8b9348 #a2ab54), with long vertical brown
  streaks (#5a4a2a) where old leaf bases dried.
- **Leaves:** `banana_fronds`, a broad fronds variant: two broad 2-pixel blades a tile, with tear gaps along the veins.
  A bright yellow-green: #334715 #47641d #5d8326 #739e32 #8cc13d #a3ca68. Evergreen.
- **Bunch:** a new `banana_bunch` hangs below on `attached_to_leaves` (about 0.3, direction down), green (#5a7a2a
  #7a9a34) and then yellow when ripe (#c8a428 #d8b832 #e6cc5a).
- **Wood:** none.
  - `banana_stem` is an axis pillar that breaks by hand, drops itself, composts and makes no planks.
  - It is in `#minecraft:logs`, so the leaves do not decay, but not in `#minecraft:logs_that_burn`.
  - A banana sapling, the sucker, drops from the fronds.
- **Extras:** bananas, picked from a ripe bunch without felling the plant. Food, and `#jugcraft:fermentable`.

#### tree_fern
Tree fern (*Cyathea*; *Dicksonia antarctica*). **Medium.** Tree ferns fill the understory of three wet forests with
one plant. Mountain ash over tree ferns is the classic wet eucalyptus forest.
- **Grows in:** Rainforest 9%, replacing the rest of vanilla's jungle bush; Eucalyptus Forest 5%; Temperate
  Rainforest 3%.
- **Shape:** straight trunk 3 (+2) of a new `tree_fern_trunk`, under acacia foliage (radius 2, offset 0). A fern on a
  trunk, 4-6 blocks, with a flat, lacy rosette that differs from the banana's droop and the date palm's tall ball.
- **Trunk:** matted dark-brown fibres, stringy kind (#271d16 #392b20 #4b382a #5d4634 #6f533e), with a few pale
  frond-scar flecks (#7a6a4a) and no bands.
- **Leaves:** `tree_fern_fronds`, fronds kind, drawn as many 3-pixel pinnae on a dark rachis. A cool fern green:
  #17331f #20472c #2a5d39 #357147 #418a57 #52a36a. It is ΔE 11 from the mahogany and further from the rainforest's other leaves.
  Evergreen.
- **Wood:** none.
  - `tree_fern_trunk` is an axis pillar with no planks and no stripping, which burns and composts.
  - It is in `#minecraft:logs` for leaf distance.
  - A sapling drops from the fronds.

#### buttressed_mahogany
Big-leaf mahogany (*Swietenia macrophylla*) as an emergent on buttress roots. **Medium.** Buttress roots are what make
a rainforest giant read as one, and they remove the last vanilla emergent from the canopy at no block cost.
- **Grows in:** Rainforest 7%, replacing vanilla's mega jungle tree.
- **Shape:** vanilla's mega_jungle trunk, 18 (+4, +10), lifted by vanilla's `mangrove_root_placer` with mahogany logs
  as the roots (trunk_offset_y 2-3, max_root_width 4, max_root_length 5). A wide, flat crown of jungle foliage
  (radius 3, height 2), with trunk vines and leaf vines at 0.25.
- **Prototype first.** Vanilla pairs the root placer only with one-block trunks, so under a 2×2 trunk the flare may
  sit on one corner and the other columns may float. Shoot it in `WoodClientGameTests` first. If it fails, fall back
  to a taller giant_mahogany, with no root placer.
- **Wood:** reuses mahogany, worldgen only. Four mahogany saplings still grow giant_mahogany.

### Batch 13: walnut and bristlecone

#### walnut
Walnut (*Juglans regia*; black walnut, *J. nigra*). **Medium.** It gives the dense oak forest a tree of its own and a
nut to forage. It is medium priority because the plain oak woods are already right.
- **Grows in:** Dense Forest 10%, from the vanilla fancy oak's share; Woodland 4%.
- **Shape:**
  - walnut: straight trunk 6 (+2) under a blob (radius 3, offset 0, height 4). A tall, open, round crown 8-10 blocks
    tall.
  - big_walnut: fancy trunk 9 (+5) under fancy foliage (radius 2, offset 4, height 4), as big_maple is.
- **Wood:** vanilla dark oak logs.
  - The owner's black walnut (`OWNER_BANK` row 2, #67533c) sits on vanilla spruce planks' hue (32) and lightness
    (0.32), so it fails rule 3 and stays banked.
  - Revisit a walnut wood set only if the owner nudges that colour at least 8 degrees away from spruce.
- **Leaves:** `walnut_leaves`, a light, slightly yellow mid green with about 5% gaps. One seed on a schedule of about
  [105, 255, 290].

  | Look | Colours | Trait |
  |---|---|---|
  | green | #2b3c1a #3c5424 #4f6e2f #61863c #77a449 #8fb668 | smooth, round 2×2 pale lime husks (#8aa040 #a8c050, lit #c4d870), darkening to black-brown when ripe (#2e2418 #4a3a28). No split shells, which are the chestnut's trait. |
  | autumn | #504412 #705f19 #937c21 #b2972c #cfb240 #d6c271. A short yellow-gold. | |
  | winter | bare early, in the `dark_oak_twigs` ramp | |
- **Extras:** walnuts, picked ripe as chestnuts are. The husks give brown dye.
  - Food, raw or roasted at a campfire.
  - `#jugcraft:squirrel_food`.
  - A mooncake filling.

#### bristlecone
Great Basin bristlecone pine (*Pinus longaeva*). **Medium.** The oldest living trees, standing alone on bare ridges,
deepen the barren basin's ancient mood without filling it.
- **Grows in:** Basin, as a rare lone tree on gravel and stone, about one chunk in four.
- **Shape:** dead wood's fancy trunk made shorter, 4 (+2), with small tufts at a few branch ends (blob radius 1,
  offset 0, height 2). An ancient, twisted, mostly silver tree 4-7 blocks tall.
- **Bark and wood:** dead wood's grey #7a7a7a and furrowed grey bark, which is how bristlecone deadwood looks.
- **Leaves:** the tufts are the stone pine's needles, with their cones (batch 8).
- **Wood:** reuses dead wood and the stone pine's needles. Worldgen only, with no new blocks and no sapling of its own.
  Its placement checks for gravel, stone or andesite below in place of a sapling check.

### Batch 14: orchard fruit (agriculture slice 4)

`AGRICULTURE.md` already plans pear, peach, lemon and orange for slice 4, so they are built there with the
`AppleLeavesBlock` pattern and placed wild by this roster. The same PR:
- places the existing apple tree in the Orchard (`apple_tree_checked`, with an `apple_tree_bees` variant), in place of
  vanilla's oak with bees;
- first redraws `apple_leaves`, `apple_leaves_blossom` and `apple_leaves_ripe` in `wood_style.py`'s manner, since
  they are still drawn by `cider_textures.py`.

Pear's maple logs add agriculture to maple's switch list (rule 4).

#### pear
European pear (*Pyrus communis*). **Medium.** Its upright crown adds shape to the Orchard beside the round chestnut
and apple.
- **Grows in:** Orchard 15%.
- **Shape:** straight trunk 5 (+2) under a blob (radius 2, offset 0, height 4). Upright, taller than wide, 7-8
  blocks.
- **Bark and wood:** maple's grey furrowed bark and pale pinkish-tan wood, close to real pear wood.
- **Leaves:** `pear_leaves`, a glossy deep green, bluer than the apple's: #18341e #22492a #2d5f37 #397445 #468e54
  #58a768.
  - Fruit stages as the apple's leaves have: blossom (#b8b0b4 #d8d2c8, with single-pixel highlights of #e4dfd6), then
    green-yellow pears two pixels tall (#8a9030 #a8b040 #cdd060).
  - No autumn look, as the apple has none.
- **Extras:** pears; the tree is never cut down.
  - Food.
  - `#jugcraft:cider_apples`, so the Cider Press makes perry-style cider.
  - `#jugcraft:fermentable`.

#### peach
Peach (*Prunus persica*). **Low.** Spring blossom and a low vase shape fill out the Orchard's understory.
- **Grows in:** Orchard 10%.
- **Shape:** forking trunk 3 (+1, +1) under a blob (radius 2, offset 0, height 2). Low, open and vase-shaped, 4-5
  blocks.
- **Wood:** vanilla cherry logs, the same genus.
- **Leaves:** `peach_leaves`, a lighter yellow-green: #2e411a #405c25 #547830 #68923e #7fb24c #99bf73.
  - Fruit stages: deep rose-magenta blossom (#b84a6a #d4708c #e896aa), clearly not cherry pink.
  - Then blushing peaches (#d0603a #e08a4a #f0b070).
- **Extras:** peaches.
  - Food.
  - A peach pie filling for the Hearth Oven, appended to the end of `PIES["fillings"]` and the `PieFilling` enum.
  - `#jugcraft:fermentable`.

#### orange
Sweet orange (*Citrus × sinensis*). The lemon is the same tree with yellow fruit. **Medium.** A warm-climate fruit
tree that feeds the existing kitchen machines, in place of a temperate vanilla birch.
- **Grows in:** Subtropics 10%, replacing vanilla birch.
- **Shape:** straight trunk 3 (+1) under a blob (radius 2, offset 0, height 3). A small, dense, round tree 4-5 blocks
  tall, like the apple tree, which never shares its biome.
- **Wood:** vanilla oak logs, as the apple tree has.
- **Leaves:** `orange_leaves`, a glossy dark green: #193010 #234317 #2e581e #396a27 #468230 #56983e.
  - Fruit stages as the apple's leaves have: white blossom specks (#d8d2c4 #e4dfd2), then small green fruit
    (#5a7a2a), then oranges (#c86a14 #e0861c, lit #f0a040) in two-pixel clusters.
  - Evergreen.
- **Extras:** oranges, picked while the tree stays.
  - Food.
  - Marmalade in a Mason Jar.
  - A Candy Kettle flavour with an effect no flavour uses yet (proposed: jump boost).
  - `#jugcraft:fermentable`.

## Build order

Each batch is one focused PR. They are listed highest value first, and each assumes the ones before it unless the
table says otherwise. Batches 1, 2, 3 and 7 need no shared change and can run side by side. Each PR also:
- updates the records of the biomes it changes;
- adds its trees to `WoodClientGameTests`, shot beside vanilla's trees and their neighbours;
- runs `python3 tools/check_mod_data.py`, `python3 scripts/check_repository.py` and `./gradlew build`;
- states that it has not been play-tested until someone plays it.

| # | Batch | Trees | Shared work in it | Depends on |
|---|---|---|---|---|
| 1 | Northern, wet and cool-forest shapes | stunted_fir and bog_fir, subalpine_fir, fir_bush, tamarack, dead_snag, willow_bush, young_aspen, swamp cedar (built as `cedar`), mossy_maple | the any-of feature switches and their check; larch in `FALLEN`; the free swaps below; the cedar wood set (owner's row 0) | none |
| 2 | Warm and mountain shapes | cottonwood, japanese_maple, live_oak, windswept_pine, big_jacaranda, mallee, giant_eucalyptus | the Lush Desert fix; `apple_tree_checked`; the eucalyptus `"giant"` entry; a cherry-placer bounds check | none |
| 3 | The yew | yew | the yew wood set (owner's row 7) and its check against the cedar's wood; the Gloomweald canopy fix | 1 (for the dead snag and the cedar) |
| 4 | Own leaves on borrowed logs | bald_cypress, snowpetal_cherry, shimmer_birch, magnolia | `TREES` `wood`/`display` and `SHAPES` `tree` fields; the Java split of `registerTree`; tree-farm and giant checks by log; the root placer; the new leaf drawing options; the naming rule. Cottonwood moves to willow logs. Design note first. | 1 |
| 5 | Fruit on seasonal leaves | hawthorn, rowan, persimmon, elder | `SeasonalFruitLeavesBlock`; twig ramps; the lone-tree extra. Design note first. | 4 |
| 6 | Evergreen scrub | juniper, gorse, sagebrush, holly | the Holly Wreath; the Steppe's trees slot | 4, 5 (holly's berries) |
| 7 | The olive | olive | the olive wood set (painting 3) and its check beside bamboo; Lavender Field `winter_snow` off | none |
| 8 | Dry country | stone_pine, joshua_tree, wattle, tamarisk | Alpine Spawn's picks list; sand saplings | 4, 6 (for the juniper beside them) |
| 9 | Giant fungi, the Nether and the End | huge_glowcap, blight_fungus, marrow_fungus, bramble_bush, dragonblood | the glowcap cap, blight wart, small blight fungus, bramble leaves, dragonblood fronds; Nether and cave placements; End saplings; the no-gain arithmetic | 4 |
| 10 | Hanging fruit | baobab, date_palm | the pod and date cluster blocks, hung by `attached_to_leaves`; the baobab giant on palm logs | 4, 8 (for the tamarisk in the Dry River) |
| 11 | Tropics, subtropics and coasts | flame_tree, mango, sea_grape, banyan, pandanus, osmanthus, driftwood | the driftwood feature; the osmanthus candy flavour | 4 |
| 12 | Rainforest | banana, tree_fern, buttressed_mahogany, and the rubber tree's placement | three trunk blocks in `#minecraft:logs`; the buttress prototype gate | 4; agriculture slice 5 (rubber tree) |
| 13 | Walnut and bristlecone | walnut, bristlecone | none | 5, 8 |
| 14 | Orchard fruit (agriculture slice 4) | pear, peach, orange | the apple leaves redrawn; the apple in the Orchard | 4 |

**Free swaps of existing trees.** These need no new tree, and go in batch 1, or batch 2 for the warm ones:

| Swap | Where | Batch |
|---|---|---|
| larch joins | Coniferous Forest 10%, Shield 8%, Lake District 10% | 1 |
| fir replaces vanilla spruce | Maple Woods, Redwood Forest, Lake District (this covers the rejected hemlock) | 1 |
| fir joins as a plain pick | Seasonal Forest 4% | 1 |
| aspen joins or replaces | Snowy Forest 20%; replaces the Shield's vanilla oak and the Snowpetal Grove's vanilla birch | 1 |
| fallen dead log joins | Dead Swamp, Burnt Forest and Rotted Expanse | 1 |
| willow joins | Lush River | 1 |
| maple joins | Lush Swamp | 1 |
| vanilla azalea joins | Swamp Woods | 1 |
| great oak joins as a rare veteran | Dense Forest | 1 |
| more tries a chunk | Wasteland's count rises to 1; Cinder Barrens gets 1-2 snag tries | 1 |
| cypress joins | Lavender Field, with `winter_snow` off | 2 |
| small palm joins | Lush Grassland, Lush Desert and Oasis | 2 |
| palm and small palm join | Overgrown Beach | 2 |
| mahogany replaces vanilla oak | Mycelial Jungle | 2 |
| apple tree joins | Flower Isle | 2 |

## Biomes by biome

Every biome in `tools/biomes.py`, plus Alpine Spawn and Pixel Hollows. **Now** shows the shares before batch 1, worked out from
the in-order chances in `biomes.py`. **After** shows the target shares once every batch has landed. "Stays" means no
change, with the reason. Expected frequencies of rare trees are estimates, to be tuned in the client survey.

### Seasonal forests (`docs/features/seasonal-forests.md`)

| Biome | Now | After | Batches |
|---|---|---|---|
| Coniferous Forest | fir 68, tall fir 25, spruce (v) 6, fallen fir 1 | fir 57, tall fir 20, larch 10, stunted fir 8 (for the spruce), dead snag 3, fallen fir 2 | 1 |
| Snowy Coniferous Forest | fir 78, tall fir 20, fallen fir 2 | fir 48, subalpine fir 20, tall fir 20, stunted fir 10, fallen fir 2 | 1 |
| Maple Woods | maple 76, big maple 12, spruce (v) 11, fallen maple 1 | maple 75, big maple 12, fir 12 (for the spruce), fallen maple 1 | 1 |
| Seasonal Forest | oak with bees and leaf litter (v) 49, maple 35, aspen 13, big maple 3, fallen maple 1 | maple 35, oak (v) 34, aspen 13, persimmon 10, fir 4, big maple 3, fallen maple 1 | 1, 5 |
| Aspen Glade | aspen 89, maple 10, fallen aspen 1 | aspen 69, young aspen 15, maple 10, subalpine fir 5, fallen aspen 1 | 1 |
| Dead Forest | dead tree 73, spruce (v) 15, oak (v) 8, fallen dead 4 | dead tree 40, dead snag 35, spruce (v) 10, juniper 10 (for the oak), fallen dead 5 | 1, 6 |
| Tundra | maple bush 100 | maple bush 40, willow bush 30, fir bush 20, juniper bush 10 | 1, 6 |
| Snowy Forest | oak (v) 56, fir 30, maple 14 | rowan 36 (for the oak), fir 30, aspen 20, maple 14 | 1, 5 |
| Muskeg | dead tree 71, fir 25, fallen dead 4 | tamarack 35, bog fir 30, dead snag 20, dead tree 10, fallen dead 3, fallen larch 2 | 1 |

### Fields and meadows (`docs/features/fields-and-meadows.md`)

| Biome | Now | After | Batches |
|---|---|---|---|
| Field | oak bush (v) 66, spruce (v) 30, oak with bees (v) 3 | hawthorn bush 55, juniper column 30 (for the spruce), hawthorn 10, oak with bees (v) 5, kept for its bees | 5, 6 |
| Flower Meadow | none | stays: an unbroken carpet of flowers is its point. The wild apple patch reaches it about one chunk in twelve; the owner may keep or remove it. | none |
| Grassland | none | stays: the open clover pasture that sets it apart. If hedgerows are ever wanted, use the hawthorn bush. | none |
| Heathland | oak bush (v) 70, pine (v) 30 | gorse 60, umbrella pine 25 (as Scots pine, for the pine), juniper bush 15 | 6, 8 |
| Lavender Field | jacaranda 60, oak with bees (v) 40 | jacaranda 50, olive with bees 30, oak with bees (v) 10, cypress 10; `winter_snow` off | 2, 7 |
| Lush Grassland | jungle bush (v) 63, oak bush (v) 30, jungle tree (v) 7 | banana 45, oak bush (v) 25, mango 20, small palm 10 | 2, 11, 12 |
| Prairie | oak bush (v) 85, fancy oak (v) 15 | oak bush (v) 70, juniper column 15 (eastern red cedar), cottonwood 10, fancy oak (v) 5 | 2, 6 |
| Shrubland | oak bush (v) 100 | hawthorn bush 60, gorse 20, juniper bush 10, oak bush (v) 10 | 5, 6 |
| Steppe | none | a new trees slot of 1-2 a chunk: sagebrush 80, juniper bush 20; nothing over two blocks | 6 |

### Wetlands (`docs/features/wetlands.md`)

| Biome | Now | After | Batches |
|---|---|---|---|
| Bog | maple bush 90, dead tree 10 | maple bush 45, tamarack 30, bog fir 15, dead tree 10 | 1 |
| Dead Swamp | dead tree 100 | dead snag 50, dead tree 45, fallen dead 5 | 1 |
| Lush Swamp | vine-hung oak (v) 70, willow 30 | vine-hung oak (v) 35, willow 30, bald cypress 25, maple 10 | 1, 4 |
| Swamp Woods | willow 70, vine-hung oak (v) 30 | willow 45, bald cypress 25, vine-hung oak (v) 20, azalea (v) 10 | 1, 4 |
| Bayou | willow 85, vine-hung oak (v) 15 | bald cypress 60, willow 25, live oak 15 | 2, 4 |
| Floodplain | oak bush (v) 80, oak (v) 20 | willow bush 70, cottonwood 30 | 1, 2 |
| Ghost Forest | dead tree 85, dark oak (v) 15 | dead snag 55, dead tree 30, swamp cedar 15 | 1 |
| Sludge Mire | dark oak (v) 56, fancy oak (v) 30, vine-hung oak (v) 14 | stays: its record is "a dense canopy of big oaks and dark oaks" | none |
| Lush River | oak bush (v) 100 | willow bush 70, willow 20, cottonwood 10 | 1, 2 |
| Fen | fir 70, dark oak (v) 30 | bog fir 50 ("short firs"), tamarack 35, willow bush 15 | 1 |
| Lake District | oak (v) 54, spruce (v) 40, fancy oak (v) 6 | oak (v) 44, fir 30 (for the spruce), larch 10, rowan 10, fancy oak (v) 6 | 1, 5 |
| Quagmire | none | stays: bare mud. Trees would blur it with the Dead Forest, which the client test already mistook for it at a region edge. | none |
| Marsh | none | stays: a marsh is grasses and reeds; a wooded one is a swamp | none |
| Wetland | spruce (v) 60, willow 40 | swamp cedar 50 (for the spruce), willow 40, elder 10 | 1, 5 |

### Warm and dry (`docs/features/warm-and-dry.md`)

| Biome | Now | After | Batches |
|---|---|---|---|
| Dryland | pine (v) 49, oak bush (v) 26, oak (v) 25 | pinyon-juniper country: pinyon 45, juniper 30, juniper bush 25 | 6, 8 |
| Xeric Shrubland | desert acacia (v) 100 | Mojave shrubland: desert acacia (v) 45, joshua tree 30, juniper 25 | 6, 8 |
| Jacaranda Glade | jacaranda 61, oak with bees (v) 17, cherry (v) 15, azalea (v) 7 | jacaranda 45, oak with bees (v) 17, big jacaranda 15, flame tree 8, cherry (v) 8, azalea (v) 7 | 2, 11 |
| Lush Desert | desert acacia (v) 70, oak bush (v) 30, which never places | desert acacia (v) 45, joshua tree 35, small palm 20; grass patches on its red sand | 2, 8 |
| Bone Flats | none | stays: its bone pillars stand in for trees | none |
| Dry River | none | 1 try a chunk on its dry banks: date palm 60, tamarisk 40 | 8, 10 |
| Cold Desert | none | a lone juniper on its coarse-dirt patches, about one chunk in six | 6 |
| Scrubland | oak bush (v) 100 | olive bush 60, oak bush (v) 25, juniper 15 | 6, 7 |
| Lush Savanna | none | stays: a treeless poppy field by its record. The owner's call: one lone giant baobab every 6-8 chunks would make it read as a savanna. | none |
| Outback | desert acacia (v) 100 | desert acacia (v) 50, mallee 30, baobab 10, giant baobab 10 | 2, 10 |
| Oasis | palm 100 | palm 40, date palm 35, small palm 15, tamarisk 10 | 2, 8, 10 |
| Wasteland | dead tree 100, at 0-1 tries a chunk | 1 try a chunk on its coarse dirt: dead tree 45, dead snag 40, tamarisk 15 | 1, 8 |
| Burnt Forest | dead tree 90, oak bush (v) 10 | dead snag 45, dead tree 40, oak bush (v) 10, fallen dead 5. Its record's "charred" trunks become grey snags unless the owner takes up the charred log (see Decisions). | 1 |
| Mediterranean Forest | cypress 48, oak (v) 30, oak bush (v) 12, dark oak (v) 10 | cypress 45, olive 25, umbrella pine 15, olive bush 15 | 7, 8 |
| Orchard | chestnut 60, oak with bees (v) 30, azalea (v) 10 | chestnut 40, apple (with bees) 25, pear 15, peach 10, azalea (v) 10 | 14 |

### Big trees and rainforests (`docs/features/big-trees-and-rainforests.md`)

| Biome | Now | After | Batches |
|---|---|---|---|
| Rainforest | mahogany 52, jungle bush (v) 17, giant mahogany 12, jungle tree (v) 12, mega jungle tree (v) 7 | mahogany 50, giant mahogany 12, rubber tree 12, banana 10, tree fern 9, buttressed mahogany 7 | 12 |
| Eucalyptus Forest | eucalyptus 52, big eucalyptus 30, oak bush (v) 18 | eucalyptus 47, big eucalyptus 25, wattle bush 15, giant eucalyptus 8, tree fern 5 | 2, 8, 12 |
| Tropics | palm 51, small palm 25, jungle bush (v) 15, azalea (v) 9 | palm 45, small palm 20, banana 15, flame tree 10, pandanus 10 | 11, 12 |
| Subtropics | azalea (v) 45, oak (v) 30, small palm 13, birch (v) 7, vine-hung oak (v) 5 | azalea (v) 40, live oak 30, small palm 15, orange 10, vine-hung oak (v) 5 | 2, 14 |
| Dense Forest | fancy oak (v) 57, oak (v) 30, dark oak (v) 7, oak bush (v) 6 | fancy oak (v) 44, oak (v) 30, walnut 10, dark oak (v) 7, holly 5, great oak 2, oak bush (v) 2 | 1, 6, 13 |
| Redwood Forest | redwood 57, giant redwood 35, spruce (v) 6, fallen redwood 1 | redwood 52, giant redwood 35, fir 6 (for the spruce), mossy maple 5, fallen redwood 2 | 1 |
| Temperate Rainforest | fir 40, redwood 30, tall fir 14, vine-hung oak (v) 11, willow 4 | fir 38, redwood 30, tall fir 14, mossy maple 11 (for the vine oak), willow 4, tree fern 3 | 1, 12 |
| Woodland | oak (v) 65, fancy oak (v) 25, oak bush (v) 8, fallen oak (v) 2 | oak (v) 57, fancy oak (v) 25, walnut 4, holly 4, elder 4, hawthorn bush 4 (for the oak bush), fallen oak (v) 2 | 5, 6, 13 |

### Mountains, coasts and volcanoes (`docs/features/mountains-coasts-and-volcanoes.md`)

| Biome | Now | After | Batches |
|---|---|---|---|
| Volcano | none | stays: a live cone is bare black rock and ash | none |
| Canyon | pine (v) 70, spruce bush (v) 30 | pinyon 40, juniper 35, juniper bush 25 | 6, 8 |
| Highland | none | almost treeless: a lone rowan or wind-bent hawthorn, about one chunk in four (rare extra) | 5 |
| Basin | none | a lone bristlecone on gravel and stone, about one chunk in four (rare extra) | 13 |
| Shield | fir 50, pine (v) 30, spruce (v) 14, oak (v) 6 | fir 45, windswept pine 25, spruce (v) 14, larch 8, aspen 8 (for the oak) | 1, 2 |
| Karst Pinnacles | pine (v) 65, spruce bush (v) 35 | windswept pine 65, spruce bush (v) 20, osmanthus 15. Bamboo for its pandas is a separate fix. | 2, 11 |
| Hot Springs | pine (v) 70, spruce (v) 30 | windswept pine 55, Japanese maple 30, pine (v) 15 | 2 |
| Ice Sheet | none | stays: sea ice has no trees | none |
| Ocean Trench | none | stays: the deep sea floor, kelp only | none |
| Gravel Beach | none | still no trees; a little driftwood | 11 |
| Dune Beach | none | still no trees; driftwood on the strand | 11 |
| Overgrown Beach | oak bush (v) 100 | sea grape 40, pandanus 20, palm 20, small palm 20 | 2, 11 |
| Flower Isle | oak bush (v) 100 | magnolia 55, oak bush (v) 30, apple 15 | 2, 4 |

### Wonders and caves (`docs/features/wonders-and-caves.md`)

| Biome | Now | After | Batches |
|---|---|---|---|
| Cinder Barrens | none | dead snags on its coarse-dirt patches, 1-2 tries a chunk, about one tree in eight chunks | 1 |
| Elder Vale | oak (v) 90, fancy oak (v) 10 | stays: "a vale of the old world" of plain vanilla oaks, on purpose | none |
| Frostlight Garden | fir 60, birch (v) 40 | fir 60, shimmer birch 40 | 4 |
| Gilded Shrubland | oak bush (v) 100, tinted gold | wattle bush 70, wattle 30 | 8 |
| Glimmer Grove | jacaranda 72, oak (v) 18, huge red mushroom (v) 10 | jacaranda 55, shimmer birch 20, big jacaranda 15, huge red mushroom (v) 10 | 2, 4 |
| Gloomweald | vanilla's dark-forest canopy, then dead tree 70 and spruce (v) 30 at 2-3 a chunk, mostly failing on the canopy | its own list in place of vanilla's, about 16 a chunk: dark oak (v) 45, yew 20, dead tree 10, dead snag 10, huge red mushroom (v) 8, huge brown mushroom (v) 7 | 3 |
| Glowcap Grotto | none (a cave) | huge glowcaps, 2-4 tries a chunk on its floors, as a cave extra | 9 |
| Hallowed Bog | willow 70, vine-hung oak (v) 30 | willow 70, young aspen 30 | 1 |
| Highsun Meadow | oak bush (v) 100, at 0-1 a chunk | stays: "a few small oaks" | none |
| Mycelial Jungle | jungle bush (v) 51, huge red mushroom (v) 30, huge brown mushroom (v) 10, oak (v) 9 | jungle bush (v) 45, huge red mushroom (v) 30, huge brown mushroom (v) 10, mahogany 8 (for the oak), huge glowcap 7 | 2, 9 |
| Shrine Springs | great oak 56, jungle bush (v) 30, oak (v) 14 | great oak 56, jungle bush (v) 30, banyan 14 | 11 |
| Snowpetal Grove | cherry (v) 75, birch (v) 25 | snowpetal cherry 75, aspen 25 | 1, 4 |
| Spider Nest | none (a cave) | stays: a dark cave whose identity is its webs | none |
| Starlit Wood | super birch with bees (v) 75, tall fir 25 | tall shimmer birch 45, super birch with bees (v) 30, tall fir 25 | 4 |
| Toadstool Field | none (vanilla's mushroom-island huge mushrooms) | plus about one huge glowcap a chunk on mycelium | 9 |
| Webwood | willow 58, dead tree 20, vine-hung oak (v) 16, birch (v) 6 | willow 57, dead tree 20, bald cypress 15, vine-hung oak (v) 8 | 4 |
| Wild Greens | none | stays: treeless by its record | none |

### The Nether (`docs/features/nether-biomes.md`)

| Biome | Now | After | Batches |
|---|---|---|---|
| Ashfall Wastes | none | stays: open by design. Its fire patches would burn a dead-wood stem and leave floating caps. | none |
| Blighted Sands | brambles | huge blight fungus on soul soil (about 1 a layer), bramble bushes on soul sand and soul soil (about 2 a layer) | 9 |
| Frost Rift | none | stays: glacier chasms; its ice is its look | none |
| Fungal Thicket | huge red and brown mushrooms (v), crimson fungi (v), glowcaps | plus huge glowcaps on nylium and mycelium (about 2 a layer) | 9 |
| Magma Fields | none | stays: fumarole fields kill plants; sulfur spikes and basalt give it height | none |
| Marrow Heap | bone spires | marrow fungus on its nether wart blocks (about 1 a layer) | 9 |
| Netherbrush | brambles, warped fungi (v) | bramble bushes on nylium (about 4 a layer) | 9 |
| Quartz Rift | none | stays: its quartz spires are its trees | none |
| Withered Hollow | none | huge blight fungus on blackstone (about 3 a layer), with no shroomlights | 9 |

### The End (`docs/features/end-biomes.md`)

| Biome | Now | After | Batches |
|---|---|---|---|
| Chorus Reef | chorus (v) | stays: the chorus stands where coral trees would | none |
| Ender Wilds | jacaranda 70, azalea (v) 30 | dragonblood 45, jacaranda 30, dragonblood shrub 20, huge glowcap 5 | 9 |
| Outer Flats | none | a lone dragonblood on end stone, about one chunk in ten (rare extra) | 9 |
| Phantom Garden | pale oak (v) 100 | stays: vanilla's pale oak, pale moss and eyeblossoms are one set, kept whole | none |
| Rotted Expanse | dead tree 100 | dead tree 60, dead snag 30, fallen dead 10; huge blight fungus on its soul-soil patches as its own extra | 1, 9 |

### Alpine Spawn and Pixel Hollows

| Biome | Now | After | Batches |
|---|---|---|---|
| Alpine Spawn (`docs/features/alpine-spawn.md`) | larch 60, spruce (v) 40 | larch 50, stone pine 20, spruce (v) 20, juniper bush 10 | 6, 8 |
| Pixel Hollows | none (a cave) | stays: a cave biome with no light or soil for trees | none |

## Rejected ideas

| Idea | Why not |
|---|---|
| Hemlock | The existing fir covers it: the same spruce placer at nearly the same size. Fir replaces vanilla spruce in the Maple Woods, Redwood Forest and Lake District instead. A hemlock wood's buff planks would sit on oak's and chestnut's hue. |
| A walnut wood set (owner's black walnut, row 2) | Its hue and lightness sit on vanilla spruce planks', so it fails rule 3. The walnut grows on vanilla dark oak logs, and black walnut stays banked until the owner nudges it. |
| A juniper wood set (painting 20, #86434e) | Cypress logs already give the juniper its family's stringy red-brown bark. Painting 20's hue (350) now sits on the jacaranda's new mauve wood (hue 352). |
| Glimmerwood | A new blue-violet wood for essentially one biome. The shimmer birch serves the Glimmer Grove. Painting 4 (#7078b1) stays reserved for when the magic branch needs a wood for wand hafts and workshop furniture. |
| A fire-proof Nether "blight" wood set | Dead-wood stems under a new wart cap serve three biomes, none of which spreads fire. A Nether set would need crimson-style registrations and IDs that become permanent. Revisit it with painting 18 (#383838) if the owner wants a third Nether building wood; the Ashfall Wastes would wait for that. |
| Dragonblood and baobab wood sets | No owner painting fits either: a lilac-grey End wood, or a smooth copper-grey baobab bark. Both read by silhouette and leaves, so palm logs serve them. Revisit either if the owner paints one. |
| Ebony or wenge | A near-black exotic for a rainforest that already gains four trees. The owner's wenge (row 3) stays banked; check it against vanilla dark oak first. |
| Elm, shagbark hickory, London plane (owner's banked rows 4, 5 and 1) | No surveyed biome needs them now. Rows 4 and 5 sit close to each other and near vanilla spruce; London plane's wood is close to oak planks. They stay banked. Row 0, western red cedar, became the swamp cedar's own wood in batch 1, at the owner's word. |
| Osage orange | Serves essentially one biome, and its golden wood needs a painting the owner has not given. |
| Persimmon on black wood (painting 18) | The persimmon reads by its fruit, so dark oak logs serve it. Painting 18 stays free for a possible charred or Nether wood. |
| Bay laurel | A dense, glossy oval evergreen on oak logs, too close to the osmanthus and the orange tree. |
| Lehua | The Volcano stays treeless, and its scarlet blossom duplicates the flame tree. |
| A pale oak shrub | The Phantom Garden is vanilla's pale set kept whole; a bush of the same blocks adds little. |
| A charred wood set | It would duplicate dead wood. A single charred log block is offered to the owner instead (see Decisions). |
| Cork oak, mesquite, palo verde, beech, Scots pine wood, alder, water tupelo, river birch, sycamore, linden, almond | Their wood would land on an existing colour: cork oak on vanilla oak, mesquite between cypress and redwood, beech on maple, alder on maple and jacaranda, tupelo on chestnut and willow, and birch, sycamore and linden on fir, palm and aspen. Palo verde's green bark is the eucalyptus's, and almond's cycle is the apple's. River birch's peeling curls would also break the no-bands rule. |
| Black spruce, paper or dwarf birch, sumac, hazel, saguaro, a separate lemon, full trees in the Tundra | Covered elsewhere or out of scope. The bog fir and tamarack fill black spruce's role; vanilla birch and the aspen give white bark; sumac's red autumn overlaps the maple bush; hazel belongs to a later woodland understory; saguaro is a cactus, a plant feature; the lemon is the orange with yellow fruit in slice 4; the Tundra stays treeless, with scrub only. |
| Merged names: bog_fir, big_aspen, crooked_aspen, charred_snag, olive_bush, ancient_olive, juniper_bush, golden_wattle, Scots pine, pinyon, umbrella pine, hawthorn_bush | Shapes of trees above, built with them: stunted_fir, cottonwood, young_aspen, dead_snag, olive, juniper, wattle, stone_pine and hawthorn. |
| Bald cypress in the Sludge Mire | Its record is a dense canopy of big oaks and dark oaks, and lime needles would be the brightest thing in its murk. |
| Elder in the Elder Vale and the Highsun Meadow | The Vale's point is plain vanilla oaks. The Meadow's 0-1 tries a chunk would hardly show it. |
| Baobab in the Dryland | The Dryland becomes one coherent American pinyon-juniper country. |
| Umbrella pine on the Dune Beach | An all-sand beach with no sand survival check, and its record says "no trees". |
| Olive in the Orchard and the Xeric Shrubland | The Orchard has snowy winters; the Xeric Shrubland becomes Mojave shrubland. |
| Yew in the Dense Forest | It would stand beside the holly as a second dark evergreen with red berries. |
| Several species in one low-density biome | The Field, Lake District, Muskeg, Outback and Heathland were trimmed to three to five kinds, so a species reads as the biome's own. The rowan left the Field, swamp cedar and willow left the Lake District, the willow bush left the Muskeg, the wattle left the Outback, and the juniper column and most of the oak bush left the Heathland. |
| Banyan figs, and banana, fig, walnut and candied-peel candies | No item without a consumer, and no Candy Kettle flavour without an effect of its own. |

## Decisions

The three reviews raised 98 points between them. Where two reviews raised the same point, it is answered once, in review order. "Accepted" means the roster above already
includes the change.

### Batch 1 as built

| Point | Decision |
|---|---|
| The swamp cedar was a cypress shape | **Changed by the owner.** It is its own wood, cedar, from their painted western red cedar, nudged off vanilla spruce and the cypress by the least change that separates it, with its bark darkened alike (see its record). The shape is `cedar`, which its sapling grows. |
| The tamarack's pine height 4 would crown half the tamaracks 7 wide | Built with height 3: 5 wide or less, by our reading of vanilla's pine placer. To check in the CI screenshots. |
| "Tamaracks lie fallen too", but no biome placed a fallen larch | The Muskeg's fallen 5 becomes fallen dead 3 and fallen larch 2. |
| The Orchard places chestnut trees under the biomes switch | The chestnut's switch list is agriculture or biomes, so rule 4's check passes for every placed tree, not only the shapes. |
| The Gloomweald's 10% snag | Left to batch 3, as its "Biomes by biome" row says: today's 2-3 tries mostly fail on vanilla's canopy. |
| A later batch's share before its batch | It stays with the tree it will replace: oak in the Seasonal Forest, Snowy Forest, Lake District, Dead Forest and Floodplain; the vine-hung oak in the Lush Swamp; the willow in the Swamp Woods; the oak bush in the Lush River and Dense Forest; the fancy oak in the Dense Forest; the maple bush in the Tundra; the spruce in the Wetland; the pine in the Shield; the fir in the Temperate Rainforest; the dead tree in the Wasteland; the cherry in the Snowpetal Grove. |

### Buildability and rules

| Point | Decision |
|---|---|
| Borrowed woods break the per-wood feature switch (tamarack, elder, stone pine, pear) | Accepted. Any-of switch lists with a `check_mod_data` rule, settled in batch 1 before any borrowed larch ships. |
| Batch 2's shared change covered only the tools side | Accepted. Batch 4 now covers the Java split of `registerTree`, switch, names, tree-farm output and giant checks keyed on the log, shrub log counts and a phantom-wood-set check. A design note comes first. |
| Joshua tree branch length below the codec minimum | Accepted. Constant 2, and a bounds check for every cherry-placer tree. |
| Huge glowcap in the Glowcap Grotto through the trees field | Accepted. Placed as a cave extra. |
| Glowcap bone-meal loop | Accepted. Caps drop about 7 glowcaps per giant, the cap block does not compost, and the glowcap keeps its value. The arithmetic goes in the record. |
| Banana and tree-fern leaves would decay | Accepted. Their stems, and `rubber_log`, join `#minecraft:logs` but not `#minecraft:logs_that_burn`. |
| Rubber tree duplicates agriculture slice 5 | Accepted. Slice 5 builds the tree and tap; this roster only places it. |
| Holly sprigs have no consumer | Accepted. The Holly Wreath ships in the same PR. |
| Walnut fails rule 3 | Accepted. Dark oak logs; black walnut stays banked. |
| Olive not checked against bamboo | Accepted. Bamboo, crimson and warped join its check and are proposed for rule 3. Fallback: willow logs. |
| The Field loses its bees | Accepted. Vanilla's oak with bees stays at 5%, and the rowan is no longer in the Field. |
| No filter for "firs above y 90" | Accepted. A plain 4% fir pick. |
| The Flower Isle apple tree depends on batch 9 | Accepted. `apple_tree_checked` is created in batch 2. The record notes that the wild patch already reaches the isle. |
| The blight fungus cannot reproduce | Accepted. Wart blocks drop the small fungus at about 5%. The sagebrush is credited with making dead wood renewable first. |
| Driftwood cannot skip vanilla's stump | Accepted. A small custom feature with stripped logs and no stump; `FALLEN` is not restructured. |
| Too many new Candy Kettle flavours | Accepted. Rowan and elder join the berry tag, osmanthus takes luck, orange takes an unused effect, and the rest are dropped. |
| Always-ticking fruit leaves | Accepted. Tree-grown leaves with air below only; evergreens tick only while unripe; a budget in each record. |
| Batches too large for one focused PR | Accepted. 14 batches, with design notes before batches 4 and 5. |
| The Lush Desert defect waits for batch 4 | Accepted. Fixed in batch 2 with the small palm and grass patches. |
| Juniper "Cooking Pot seasoning" does not exist | Accepted. An alternative Mulling Spices recipe instead. |
| Peach "Cooking Pot pies" | Accepted. Hearth Oven fillings, appended to the end of the list and enum. |
| Persimmon's uniqueness claim is false | Accepted. Reworded. |
| Leaf IDs that take other trees' names | Accepted. The naming rule; `elder`, `banyan_leaves`, `snowpetal_cherry_leaves`; no fig item. |
| Yew's Halloween case sat in batch 6 | Accepted. Moved to batch 3; it needs no shared change. |
| Dragonblood on sand and gravel in the Outer Flats | Accepted. End stone only. |
| Yew as a cauldron reagent | Accepted. Dropped. |
| Two different "Spanish mosses" in the Bayou | Accepted. The live oak hangs shroud moss. |
| Root placer under a 2×2 trunk | Accepted. A prototype gate, with a plain giant as the fallback. |
| Olive texture breaks rules 2 and 4 | Accepted. The figure is drawn from the wood's own ramp, with a one-pixel wander. |
| Holly berries never show with seasons off | Accepted. A schedule, plus berries by position in the green state. |
| Persimmon and holly give a cheap route to dark oak and pale oak logs | **Rejected.** The tree farm already turns one dark oak or pale oak sapling into six logs, and dark oak is common. The records will note it. |
| Biome records would contradict the new picks | Accepted. Each batch updates them; files are named in "Biomes by biome". |

### Art and fit with vanilla

| Point | Decision |
|---|---|
| Windswept pine copies vanilla acacia's placers | Accepted. Cherry trunk with radius-1 acacia pads. |
| Osmanthus is the orange and apple tree | Accepted. A multi-stemmed shape and cream-gold flowers. |
| Persimmon's autumn is maple orange and swallows its fruit | Accepted. Wine-maroon, ΔE 15 or more from every maple look. |
| The wattle bush is the Gilded Shrubland's tinted oak bush | Accepted. The oak bush is removed there, and phyllodes cover 35% or more. |
| The banana is the small palm's silhouette | Accepted. A drooping cherry crown. |
| Japanese maple is an acacia silhouette | Accepted. A low double dome. |
| One stunted fir cannot be both forms, and duplicates spruce | Accepted. Two shapes, and it replaces the spruce in the Coniferous Forest. |
| Subalpine fir is the cypress's column | Accepted. A visible bole and a flared foot. Proposed note for `NATURAL_TEXTURES.md`: fir and cypress never share a biome while their needles are this close. |
| Swamp cedar hides its bark | Accepted. trunk_height [3, 5]. |
| Holly is a small fir in summer | Accepted. An upright oval crown and a yellower glossy ramp; the yew is out of the Dense Forest. |
| Hawthorn's green is the apple's | Accepted. A shifted ramp (ΔE 19 from the apple), unripe haws, oak bush 10% or less. |
| Rowan reads as a small maple | Accepted. Hue 78, and an apricot autumn ΔE 17 or more from every maple look. |
| Juniper's hue is the eucalyptus's | Accepted. Hue 187, saturation 0.17. |
| Olive bark and wood break the manner rules | Accepted. |
| Olive wood is near bamboo | Accepted. See the olive's check. |
| Mediterranean greys are too alike | Accepted. A silver olive, stone pine ΔE 12 from the cypress, a fresher joshua green, and no olive bush with the sagebrush. |
| ancient_olive is vanilla dark oak's silhouette | Accepted. A squat giant trunk under random_spread. |
| Tamarisk reads as a vanilla cherry | Accepted. A random_spread crown, dustier plumes on 25% or less. |
| Gorse is over-saturated | Accepted. Saturation capped at 0.65, lemon versus the wattle's egg-yolk. |
| Glowcap cap too bright, drawn as leaves | Accepted. Darker blotches in the mushroom-block manner; the blight wart likewise. |
| Lime bald-cypress needles in murky biomes | Accepted. A greyed ramp and a browner russet; not placed in the Sludge Mire. |
| Walnut: wood, husks and bark | Accepted. Dark oak logs and smooth lime husks. |
| Live oak's pale moss | Accepted. Shroud moss. |
| The banyan reads as a mangrove | Accepted. Hanging roots, a wider dome and narrower root flare. |
| The forking dead snag reads as an acacia skeleton | Accepted. Straight, plus a bent variant. |
| Stumpless driftwood is impossible with vanilla's feature | Accepted. See driftwood. |
| One fronds routine for four leaf forms | Accepted. Midrib count, length and angle become parameters. |
| Blossom edges use one global green | Accepted. Per-entry edge green, under-gap colour and coverage. |
| Leaf entries without six tones | Accepted. Every new look above has six tones, checked by ΔE. |
| Per-tree gap rates the code cannot draw | Accepted in part. An optional threshold; per-tree figures kept only for the few that differ. |
| Fruit at fixed coordinates | Accepted. Each species places clusters from its own seed. |
| Near-white petals | Accepted. Main tones at lightness 0.86 or below. |
| Peach blossom reads as cherry | Accepted. Rose-magenta. |
| Bramble thorns jump in contrast | Accepted. A mid step under 4-5 pale pixels. |
| Young aspen and pandanus grow stacked crowns | Accepted. Leaf height set above the trunk. |
| Date palm is a lollipop | Accepted. A flattened crown, compared in game. |
| Shimmer birch near the eucalyptus and jacaranda | Accepted. Hue 155, pinker clusters. |
| The Field's juniper column beside vanilla spruce | Accepted. It replaces the spruce. |
| Cottonwood is a giant birch | Accepted. Aspen logs (as white poplar) in batch 2, willow logs after batch 4. |
| The baobab's crown is too dense | Accepted. A random_spread crown for the giant. |

### Biome coverage

| Point | Decision |
|---|---|
| Alpine Spawn's shares do not add up | Accepted. 50/20/20/10; `alpine.TREES` becomes a picks list. |
| Umbrella pine on the all-sand Dune Beach | Accepted. Dropped. |
| Chances in order are not shares | Accepted. This document gives shares, and the build converts them. |
| A failed pick places nothing | Accepted. The blight fungus is its own extra in the Rotted Expanse, and sand-capable survival checks are used where trees stand on sand. |
| Positions, rarities and a height filter the schema cannot express | Accepted. The wording is dropped, rare trees become extras, and the fir is a plain pick. |
| The Gloomweald's trees land on vanilla's canopy | Accepted. Its own canopy list (batch 3). |
| Elder works against the Elder Vale's theme | Accepted. The Vale stays; elder goes to the Woodland and Wetland. |
| The Dryland mixes continents and keeps vanilla oak | Accepted. Pinyon-juniper country; the baobab moves out. |
| The Xeric Shrubland mixes continents | Accepted. Mojave shrubland; the olive bush is dropped. |
| Olives in snowy winters | Accepted. No Orchard olive; the Lavender Field's `winter_snow` goes off. |
| The rubber tree belongs to slice 5 | Accepted. See the rubber tree. |
| The Burnt Forest's "charred" trunks | **Not built for now; offered to the owner.** One `charred_log` block, a log only, drawn from painting 18, could trunk the Burnt Forest's and Cinder Barrens' snags. Until the owner asks for it, the record says grey snags, to keep the roster lean. |
| The Lush Grassland is still mostly vanilla | Accepted. The banana becomes its default. |
| Too many kinds in low-density biomes | Accepted. Trimmed (see Rejected ideas). |
| Placements too rare to see | Accepted. Wasteland 1 a chunk, Dry River 1, Cinder Barrens 1-2, Cold Desert about one chunk in six; the Highsun elder is dropped. |
| Pixel Hollows missing | Accepted. Listed; it stays as it is. |
| Wrong claims (Hot Springs, Wild Greens, persimmon) | Accepted. Reworded: the Hot Springs' grass changes with the seasons but its trees do not; the Wild Greens are treeless by their record. |
| Record lines to update | Accepted. Each batch updates them. |
| The azalea is called a stand-in in one place and added in another | Accepted, keeping the azalea. It stays the Subtropics' flowering shrub and joins the Swamp Woods. The flame tree replaces it only in the Tropics, where azaleas do not grow. |
| The dead snag skips the Rotted Expanse and Gloomweald | Accepted. Added to both. |
| The Webwood has no water for the bald cypress | **Rejected.** Bald cypress also grows on dry ground. Setting a water depth would move the Webwood's willows and dead trees into water too. Its cypresses stand on dry ground. |

---

Prepared on 5 October 2026 with Claude Opus 5.5. Colour ramps and distances were computed by script from
`tools/wood_style.py`, and current shares from `tools/biomes.py`. Batch 1 has since been built (not played); nothing
else in this plan has been built, run in game or play-tested.

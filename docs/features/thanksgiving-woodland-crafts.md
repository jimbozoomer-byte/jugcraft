# Thanksgiving woodland harvest crafts (group B)

Status: integrated locally by OpenAI Codex. Compilation, all relevant Thanksgiving server cases in the 118-test combined cohort, six selected client suites and data/art checks pass. Personal Minecraft close-up review and the final HUD-isolation rerun are complete; see [combined acceptance](thanksgiving-craft-package-nov14.md). No publication or live-world change.
Proposal issue: none. The owner's Thanksgiving holiday batch (14 November 2026, group B, woodland harvest crafts) is the scope approval.
Owner: the project owner (request).

Implemented with Claude Code.
Target milestone and tier: seasonal decoration, no tier; craftable from the first day.
Primary specialty and supported player role: building and decorating (farm, hearth, harvest table, home floor).

Eight everyday harvest and craft decorations. Each is an **inspired game adaptation, not a reconstruction or a 1621
object**. They take their forms from Wampanoag and wider Northeastern Woodlands everyday craft (twined baskets, a bark
box, a rush mat, carved bowls and spoons, an earthenware cooking pot, a corn drying frame and a corn mortar).
- **No sacred items:** nothing ceremonial, sacred or regalia is made.
- **No invented iconography:** no motif is invented.
- **No borrowed designs:** no other nation's designs are used (the bark box is left plain, with no etched designs).
- **Sources:** the source notes below say what each form and motif follows and how much of that was actually read.

## Player experience

Eight new placeable decorations, all crafted from things a farm grows or a starting player has (wheat, string, flax,
corn, cranberries, logs, birch logs, bowls, clay, cobblestone and sticks).
- **Crafting:** no research and no calendar gate.
- **Floor pieces:** each faces the player who places it, stands on the centre of the block below and falls when that
  goes. It takes water like a lantern, breaks quickest with its tool and drops itself once.
- **Use:** using any of them changes nothing. They are decorations, and none stores, crafts, cooks or grinds.
- **The Rush Floor Mat:**
  - lies a pixel thick on any block, like a carpet, and is walked on like one;
  - its rushes run the way the player faced when laying it;
  - mats laid side by side join into one mat, bound only round the outer edge of the whole group;
  - taking one up re-binds its neighbours' edges.

### The inventory: each candidate against existing content

| # | Candidate | Decision | ID, and why |
|---|---|---|---|
| 1 | Lidded twined storage basket | New | `lidded_twined_basket` (Lidded Twined Basket). Existing baskets are different things: `wooden_basket` and `bamboo_basket` are working 9-slot pickup baskets; `wicker_apple_basket`, `pear_basket`, `bread_basket` and `root_vegetable_hamper` are wicker baskets of produce; `knitting_basket` is the hearth's working basket; `foraging_basket` is a carried item. None is a closed, lidded, twined storage basket. It holds nothing, and its JEI page says so. |
| 2 | Shoulder carrying basket | New | `twined_pack_basket` (Twined Pack Basket). Nothing existing is a carrying basket with shoulder straps. `foraging_basket` is an item and `supps_lunch_basket` is a lunch basket. |
| 3 | Birch-bark lidded box | New | `birch_bark_box` (Birch-Bark Box). No bark container exists. The only "birch bark" IDs are glass doors. |
| 4 | Connected rush-mat floor covering | New | `rush_floor_mat` (Rush Floor Mat). The existing mats are different: `half_tatami_mat`/`full_tatami_mat` (rice tatami, one or two blocks, no joining), `twigworks_bamboo_mat`, `household_door_mat`, `beware_doormat`, `carpet_tile` and the leaf carpets. None joins its neighbours with edges only on the outer sides. |
| 5 | Carved wooden serving bowls and spoons (table display) | New | `carved_bowls_and_spoons` (Carved Bowls and Spoons). `crafted_wood_bowl` and `crafted_wood_crockery_combo` are the owner's turned crockery, stacked plates, bowls and cups. They are not a carved bowl display with lug handles, spoons, samp and a twined food basket. |
| 6 | Incised earthenware cooking vessel, supported | New | `incised_earthenware_pot` (Incised Earthenware Pot). The existing pots do other jobs: `cooking_pot` and `simmer_pot` are working stations; `crafted_*_pot` are glazed planters and pots; `ecology_pot`, `twigworks_*_silt_pot` and `supps_urn` are flat-based pots and urns. None is a round-based cooking pot set among hearthstones. |
| 7 | Corn-drying frame | New, distinct from `drying_rack` | `corn_drying_frame` (Corn Drying Frame). See "Corn Drying Frame and the Drying Rack" below. `drying_rack` is unchanged. |
| 8 | Wooden corn mortar with stone-ended pestle | New, distinct from the alchemy mortar | `wooden_corn_mortar` (Wooden Corn Mortar). See "Wooden Corn Mortar and the other mortars" below. `corn_dolly`, a woven wheat hanging, is unrelated and unchanged. |

8 new blocks (with their items); no existing ID is changed; nothing is covered or replaced.

### Corn Drying Frame and the Drying Rack

| | `drying_rack` (Barn & Harvest Tools, part 3) | `corn_drying_frame` (this group) |
|---|---|---|
| What it is | A working station: herbs and peppers hung on it dry in 2,400 ticks into Dried Herbs and Dried Chili. | A decoration. It holds and dries nothing, and using it does nothing. |
| Size | One block across, two blocks tall (a multiblock). | One block, 16 px tall. |
| Build | Sawn timber frame, splayed feet, two rails, iron hooks. | Two forked sapling poles in their bark, a pole laid in the forks, a rail behind, all lashed with bark cord. |
| What hangs on it | Up to four bunches you add (herbs or a ristra). | Three fixed cords braided from corn husks, each with three ears (yellow, white, red) hung by their pulled-back husks. |
| Block entity, state | Block states per rail, drying timer. | None beyond facing and water. |

### Wooden Corn Mortar and the other mortars

| | `mortar` ("Mortar and Pestle", Concordance alchemy) | `forbidden_mortar` ("Mortar", arcane decor) | `wooden_corn_mortar` (this group) |
|---|---|---|---|
| Kind | A handheld item (not placeable); grinds alchemy ingredients. | An owner-supplied arcane block. | A floor decoration that grinds nothing. |
| Model | A small stone bowl in the hand. | The owner's arcane stone mortar. | A knee-high log stood on end, round and slightly waisted, with bark running up it and an end-grain rim round a deep hollow. Cracked corn is in the hollow, a long wooden pestle with a lashed stone end stands leaning in it (taller than the mortar), and an ear of corn and spilled kernels lie at its foot. |
| Name | Mortar and Pestle | Mortar | Wooden Corn Mortar |

### Lidded basket form: kept round

The owner's relayed reading of the Peabody "Listening" transcripts (below) mentions deep rectangular handled baskets and
shallow round food baskets. The lidded basket keeps its round-bellied lidded form, for these reasons:
- the brief's candidate is a lidded storage basket;
- the deep rectangular form is already represented by the Twined Pack Basket;
- a third rectangular basket would repeat that silhouette and come close to the existing lidded `root_vegetable_hamper`.

The shallow round food basket form was added to the Carved Bowls and Spoons, holding cranberries. The lid was tidied
after review into a flat round lid with one braided knob.

## Source notes

**Fetch status:** worker B could not open any of the three cited pages. All three hosts were blocked by this session's
network policy (DNS resolution refused for `plimoth.org`, `peabody.harvard.edu` and `collections.peabody.harvard.edu`).
No other route was attempted after that.

1. **Plimoth Patuxet, "Wampanoag Pottery Sherd"**, https://plimoth.org/yath/unit-1/wampanoag-pottery-sherd
   - **Read by the owner/Codex (Codex web tool), relayed by the coordinator:** rounded earthenware cooking pots supported
     by earth or stones; rims with two or four peaks; incised or impressed decoration.
   - **Search summary, page not opened (worker B's web search):**
     - the sherd is listed as Early–Middle Woodland earthenware;
     - pots were decorated by incising and impressing designs before firing;
     - a related Plimoth page ("Manomet Pot") lists incised lines, punctate and dentate designs among common decoration.
2. **Peabody Museum, "Listening to Wampanoag Voices: Beyond 1620" transcripts**,
   https://peabody.harvard.edu/listening-wampanoag-voices-beyond-1620-transcripts
   - **Read by the owner/Codex (Codex web tool), relayed by the coordinator:**
     - Alyssa Harris describes handwoven Wampanoag carrying baskets with sewn straps, used for gathering and storage;
     - Zoe Harris describes shallow round food baskets and deep rectangular handled baskets.
   - **Search summary, page not opened:**
     - a speaker describes twining as a traditional Wampanoag weaving method;
     - the basket discussed was open-twined rather than closed;
     - a basket strap was sewn.
3. **Peabody collections, Wampanoag results**, https://collections.peabody.harvard.edu/groups/wampanoag/results
   - **Unread:** blocked here, and the page returned 403 to the owner as well.
   - A search summary (page not opened) mentioned a catalogue record of a Wampanoag carved wooden bowl described as a
     samp bowl (a photograph record). Nothing more of that record is used.
4. **General knowledge, not verified against the cited pages:**
   - birch-bark boxes with root-lashed rims;
   - rush or cattail mats;
   - wooden log mortars with long pestles for corn;
   - drying corn braided by its husks.

   These are treated as broad Northeastern Woodlands everyday forms, and are plain on purpose.

**Which form and motif comes from where:**

| Piece | Form or motif | Basis |
|---|---|---|
| Both baskets, the mat, the food basket | Open twining: weft rows twisted round cord warps, warps showing between rows | Twining as a Wampanoag weaving method; open twining (search summary, page not opened) |
| Lidded basket | Darker band of bark-cord weft round the belly; braided lid edge and knob | Game choice for readability, not from a source |
| Pack basket | Shoulder straps sewn on, with sinew stitches at the foot and over the rim | Carrying baskets with sewn straps (read by the owner/Codex, relayed) |
| Food basket in the bowl display | Shallow round twined basket | Shallow round food baskets (read by the owner/Codex, relayed) |
| Earthenware pot | Rounded body and base, supported by stones on ash; four rim peaks | Read by the owner/Codex, relayed. The flattened support, the stepped voxel profile and the ash bed are a game adaptation. |
| Earthenware pot | Incised band under the rim: an incised line, a row of impressions, another line | Incised or impressed decoration (relayed); incised lines and dentate impressions (search summary, page not opened). Generic; no specific pot's design is copied. |
| Birch-bark box | Plain bark with its own lenticels; corners and lid edge sewn with root in even stitches | General knowledge; plain on purpose |
| Rush mat | Long rushes, twined rows every 8 texels, bound edge | General knowledge plus twining (as above) |
| Corn frame and mortar | Husk-braided corn; log mortar and stone-ended pestle | General knowledge; the stone end is the brief's |

**Owner/Codex follow-up:** verify the craft forms and motifs against the three cited pages, which were not fetchable
here.

## Connections
- **Input producer:** farm crops (wheat, flax, corn, cranberries), logs and birch logs, bowls, string, clay, cobblestone
  and sticks.
- **Output consumer:** none; these are decorations.
- **Technology and magic:** none.
- **Entry path (no circular unlock):** every recipe is a shapeless crafting-table recipe of reachable items. None needs
  research, and none needs another new decoration.
- **Routes:** all solo-craftable.
- **Infrastructure:** cosmetic blocks, built on the existing `FacingDecorationBlock` and the agriculture pipeline.

## Balance and automation
Recipes are shapeless, follow the agriculture feature switch and sit in the building category
(`tools/thanksgiving_woodland.py` `SHAPELESS`):

| Piece | Inputs | Makes |
|---|---|---|
| Lidded basket | 4 wheat, 2 string | 1 |
| Pack basket | 3 wheat, string, 2 flax, corn | 1 |
| Bark box | birch log, string, flax | 1 |
| Mat | 4 wheat, string, flax | 4 |
| Bowls and spoons | a log, 2 bowls, corn, cranberries | 1 |
| Pot | 3 clay balls, 2 cobblestone, a stick | 1 |
| Corn frame | 3 sticks, flax, 3 corn | 1 |
| Corn mortar | 2 logs, a stick, cobblestone, corn | 1 |

Breaking a piece returns only the block itself, so there is no conversion loop. There is no automation, no ticking and
no block entity.

## Multiplayer and persistence
- **State:** plain block states only: facing and water for the floor pieces; axis and four joins for the mat.
- **IDs:** new IDs only.
- **Protection:** placement and breaking go through the normal block path, so land and town protection applies. The
  pieces have no use interaction to protect.
- **Feature switch:** disabling the agriculture feature removes the recipes, never the registrations.
- **Data tags:** the only data tag these blocks rely on is their `minecraft:mineable/*` tag. The mat and baskets take
  `hoe`, the bark box, bowls, frame and mortar take `axe`, and the pot takes `pickaxe`. Waterlogging, the
  centre-support rule and the mat's joins use no tag.

## Dependencies and assets
- No new dependencies.
- All models and textures are drawn by code in `tools/thanksgiving_woodland_data.py`:
  - one packed texture per floor piece, at one texel per model pixel;
  - two 16×16 positional textures for the mat;
  - no Mojang texture is read.
- **Shared painters and palettes:**
  - the Harvest Festival's willow `braid` (lid edges, food-basket rim);
  - the Pumpkin Patch Market's `lengthwise`, `flat`, `wrapped` and `panel`;
  - the Harvest Festival's corn and husk palettes;
  - the Thanksgiving decorations' display transforms.
- **Owner library:** the catalog was searched for basket, mortar, pestle, pot, earthen, bark, birch, rush, mat, bowl,
  spoon and corn. It holds the forbidden-arcanus mortar (already integrated as `forbidden_mortar`), the handcrafted
  crockery bowls (already `crafted_*_bowl`) and unrelated stone, machine and birch-wood files. Nothing suited these
  forms, so nothing was copied.

## Verification

Run here (offline; no Minecraft, no Fabric toolchain):
- `tools/thanksgiving_woodland_data.py` regenerates every owned resource. A second run gives byte-identical output
  (`test_regeneration_is_deterministic`).
- `scripts/tests/test_thanksgiving_woodland.py`: all its tests pass at the branch head, after the shared hooks.
  - Before the hooks, the two hook tests fail as expected.
  - It covers: regeneration, outlines and states, art_check's Z1/N1/U1/O1/I1 rules on every model, drops, recipes and
    reachable inputs, the Java table, the mineable tags, one texel per model pixel, mat joins, the
    inspired-not-reconstruction wording, and decoration-only.
- `tools/thanksgiving_woodland_check.py`, which `check_mod_data.py` runs as `check_thanksgiving_woodland`: no finding at
  the branch head.
- The shared renderer's `report` on all eight pieces: 0 density outliers, 0 UVs outside the sprite, 0 transparent faces,
  0 z-fighting pairs, no missing textures.
- Textured galleries of every piece, every facing and every state (all 32 mat states), plus a joined-mat layout. These
  are generated previews, not in-game renders.
- Java: syntax parse only (tree-sitter), with no errors in the five new Java files.

Written, not run in Minecraft:
- `WoodlandHarvestCraftsGameTests`:
  - every floor piece faces its placer, is dry, and drops itself once;
  - placed in water, it holds the water; it cannot stand on air, and goes when its support goes;
  - an empty hand changes nothing;
  - mats join in an L, one pixel thick, split when the middle is taken up, and go with their support;
  - a laid mat takes the player's axis and drops itself.

Not run: Java compilation, server and client game tests, in-game visual review, two-client play.

## World and event applicability
Seasonal in theme only. Nothing is gated on the calendar, nothing spawns or changes in the world, and the pieces keep
working all year. No city block, town template or owner placement is changed.

## Rollout and open questions
- **Before release:** compile, run the server game tests, and look at the models in game.
- **Alchemy essence:** values are explicit recipe-derived records in `data/jugcraft/alchemy/values.json` (the economy audit of 14 November: each item's one recipe costed by `tools/build_alchemy_values.py`'s formula; `scripts/tests/test_thanksgiving_economy.py`). A full `tools/build_alchemy_values.py` run against the game JAR should reproduce them.
- **Source follow-up:** the owner/Codex follow-up above.

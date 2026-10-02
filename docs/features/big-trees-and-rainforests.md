# Big trees and rainforests (biomes batch 5)

Status: in progress on branch `claude/biomes`, with the region engine ([biome-regions.md](biome-regions.md)). Awaiting CI. **Not yet played.**
Proposal issue: none. The owner asked on 2 October 2026 to remake the Biomes O' Plenty catalog's biomes ([branches/BIOMES.md](../branches/BIOMES.md)) and to carry on through every batch. Everything here is original: the catalog guided the concepts only.
Owner: @jimbozoomer-byte
Target milestone and tier: world generation and building (Discovery).
Primary specialty and supported player role: exploration; building (redwood, eucalyptus and mahogany wood).

## Player experience
Eight forests, mostly in the **woodland** layout of Jugcraft regions. The two giant forests of the old-growth taigas also grow in the wild layout:

| Biome | Layout: replaces | What grows |
| --- | --- | --- |
| **Rainforest** | woodland: the wettest jungles | tall mahoganies and giant ones, jungle trees and bushes over ferns, orange cosmos and puddles; only parrots |
| **Eucalyptus Forest** | woodland: other jungles, savanna plateaus | tall eucalyptus with rainbow-streaked bark, oak scrub, melons and wildflowers; parrots among the farm animals |
| **Tropics** | woodland: sparse jungles | bright green land of palms, small palms, flowering azaleas and jungle bushes; hibiscus, hydrangeas and bamboo; turquoise water; parrots; jungle temples |
| **Subtropics** | woodland: savannas, warm plains | green, plains-like country with flowering azaleas, oaks, birches, small palms and vine-hung oaks; hydrangeas, sugar cane; villages |
| **Dense Forest** | woodland: dark forests | big spreading oaks packed close, dark oaks among them, leaf litter and ferns; woodland mansions |
| **Redwood Forest** | woodland and wild: old-growth pine taigas | giant redwoods two blocks wide and tall single ones on podzol broken by moss; ferns and tall ferns |
| **Temperate Rainforest** | woodland and wild: old-growth spruce taigas | firs and redwoods, vine-hung oaks and willows, thick with ferns and berry bushes |
| **Woodland** | woodland: flower forests, sunflower plains | plain oak woodland: oaks big and small, fallen logs, leaf litter, poppies, daisies, berry bushes; villages and woodland mansions |

- Biomes O' Plenty's Redwood Forest Edge merges into the Redwood Forest, the Temperate Rainforest Hills into the Temperate Rainforest, the old Rainforest into the Rainforest, the Tropic Beach into the Tropics, and the Deciduous Forest and Timber into the Woodland.
- Seasons: the Dense Forest, Redwood Forest, Temperate Rainforest and Woodland change colour with the seasons. Only the Dense Forest and Woodland take winter snow when `seasons.snow` is on; the redwood and temperate rainforests have mild, wet winters. The four tropical forests do not change.
- **New trees:**
  - Redwood: a tall, straight trunk under a narrow spire of needles. **Four redwood saplings in a square grow a giant redwood**: a trunk two blocks wide, its crown high up, podzol spread round its foot.
  - Eucalyptus: a tall, clean trunk with a loose, airy crown; its bark is streaked green, orange, purple and blue where it peels. Big ones branch high up.
  - Mahogany: a tall rainforest hardwood that forks into a broad, flat canopy hung with vines. **Four mahogany saplings in a square grow a giant mahogany**, two blocks wide.
  - All three have full wood sets and saplings and are evergreen. A small palm grows in the tropics and subtropics.
- **New flowers:** hibiscus (pink; dye and suspicious stew, water breathing) and hydrangea (a tall flower with blue mopheads; light blue dye).
- **Ground:** the Redwood Forest lays podzol with patches of moss and coarse dirt.

## Connections
- Input producer: world generation.
- Output consumer: building (redwood, eucalyptus and mahogany wood), dye (hibiscus, hydrangea), farming (melons in the Eucalyptus Forest, sugar cane in the Subtropics).
- Technology connection: the sawmill and tree farm take the three new woods like the others.
- Magic connection: none yet.
- Reachable entry path: jungles, savannas, dark forests and flower forests are common; old-growth taigas less so. The woodland layout is a quarter of Jugcraft regions; the giant forests also grow in the wild layout.
- Required vs optional: nothing here is required.

## Balance and automation
- The new woods are ordinary wood (4 planks by hand, 6 in the sawmill). A giant tree gives more logs from four saplings, as vanilla's giant spruce and jungle trees do. No new conversions, no loops.

## Multiplayer and persistence
- Server authority: world generation and tree growth are the server's.
- `biomes.enabled=false` stops the biomes in new chunks and the new woods' hand recipes. Every block, item and biome stays registered.
- Existing worlds: new chunks only.

## Dependencies and assets
- Giant trees use vanilla's giant and mega jungle trunk shapes and mega pine and jungle crowns. Four saplings in a square are found by `agriculture/GiantSaplingBlock`, which grows the giant from the square's north-west corner and leaves the saplings if there is no room. The other trees use vanilla's straight, fancy, forking and bending trunks.
- Leaf litter, melons, bamboo, fallen oak logs and vanilla's oaks with leaf litter come from vanilla's own features, by reference.
- Textures drawn by code in `tools/wild_textures.py`. No Mojang file is copied.

## Verification
Results are recorded under "Results" below after CI runs.
- `python3 tools/check_mod_data.py`: trees (each giant is a giant shape of its tree's wood, with its grower in Java), biomes and their features, and non-overlapping rules.
- Server game tests (`BiomeGameTests`):
  - `bigTreesGrow`: a redwood, a eucalyptus and a mahogany grow from saplings, and four redwood or mahogany saplings in a square grow a giant whose trunk is two blocks wide.
  - `tropicalPlantsWork`: hibiscus is a small flower with a potted form; a hydrangea takes two blocks and drops one.
  - `regionLayoutFollowsItsRules`: every batch 5 rule places its biome in each of its layouts.
- Client game test (`BiomeClientGameTests`): distances from the start of a real world (seed `jugcraft`), and screenshots of each biome found.
- Not run: play, a dedicated server, two clients.

### Results
Not yet run in CI.

## World and event applicability
- Biome fit: each biome takes the climate of the vanilla biome it replaces.
- Seasons: see above.

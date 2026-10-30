# Decorations from the transmutation tablet

Status: implemented on the contributor's fork, pending review.

Proposal issue: none. On 29 October 2026 the contributor asked for the decorative blocks the tablet could not make (vanilla walls and building sets, bamboo, crimson, warped and poplar wood, shelves, leaves, copper, lights, plants, furnishings, and Jugcraft's woods, stone, glass, columns, lamps, furniture, pots and decorations) to be made, "not any of the quartz or copper blocks that can be turned back into materials or be crafted into non decorative items", and for a note to the owner that the modded woods lack doors and other pieces.

Owner: jimbozoomer-byte
Target milestone and tier: the transmutation tablet's tier; nothing new unlocks
Primary specialty and supported player role: builders and decorators

## Player experience

The tablet's list of what it may make (`jugcraft:tablet_outputs`) grows from 394 items to 2,569. They are made the way the old ones are: dissolve one to learn it, then make more for its essence price. Nothing about dissolving changes.

**Vanilla:**
- every stone wall (26, with mud brick and resin brick), the nether brick fence;
- the missing cracked and chiseled stones, the stone and polished blackstone buttons and plates;
- the quartz family except the block and the slab (the pillar, bricks, chiseled block, smooth quartz and its slab and stairs, and the stairs);
- prismarine, purpur and resin bricks;
- bamboo, crimson, warped and poplar wood, every wood's shelf, every leaf;
- cut, chiseled and grated copper, copper doors, trapdoors, bars, chains, lanterns and torches, each with its exposed, weathered and oxidized look;
- sea lanterns, glowstone, froglights, shroomlights, end rods, redstone lamps, every candle, both campfires;
- every small flower, pink petals, wildflowers, ferns, grass, dead bushes, vines, leaf litter, moss and pale moss, azaleas, dripleaves, spore blossoms, lily pads, mushroom blocks, coral blocks and fans, hay, bone blocks, packed ice, magma, wart blocks and mycelium;
- the 16 colours' beds, banners, cushions, wool slabs and wool stairs, and flower pots, decorated pots, paintings, item frames, glow item frames, bookshelves, chiseled bookshelves, lecterns and bells.

**Kept out**, as asked: the quartz block (it makes the Craps Table) and quartz slab (the TSC Status Board), the copper block (it turns back into ingots), every copper bulb (one goes into the Thunderstorm scroll), the plain weathered copper blocks (they scrape back to a copper block), waxed copper, and Jugcraft's certus quartz bricks, pillar and glass (recipes use them). 

**Jugcraft:** every piece of all 56 woods (logs, wood, planks, slabs, stairs, fences, gates, doors, trapdoors, buttons, plates, signs and leaves where they exist), stone and brick families, glass, columns, lamps, pots and planters, decorations, and household and crafted furniture (chairs, tables, desks, sofas, stools, benches, beds, cabinets, baths, basins, sinks, toilets, hedges, lattice fences, trims, tableware and trophies). Machines, food, tools and boss, quest or ritual items stay out.

**Also on the tablet, as the contributor asked on 30 October 2026:** every sapling (vanilla and Jugcraft, 79), every boat, chest boat and raft (67), containers (chests, trapped chests, barrels, and the household and crafted crates, coolers, drawers, cupboards, nightstands, shelves, storage cabinets and jars, mail and post boxes and recycling bins: 174), household appliances (fridges, freezers, grills, stoves, microwaves, toasters, range hoods, the computer and television, doorbells, light switches, trampolines, the electricity generator, cutting boards, the oven and the coffee and microwave counters: 66), work tables (the vanilla crafting, cartography, fletching and smithing tables, loom, stonecutter and grindstone, and Jugcraft's artificer's, lampwright's and workshop benches, drafting, map, blueprint, spelunkery, fortune teller's, inscription, scribe's, alteration, lab, judges' and hologram tables, apothecary shelf, workbench and Beast transmutation table: 26) and game tables (baccarat, blackjack, craps, roulette and derby tables, the arcade cabinet and lottery kiosk: 7). A container is made empty, and a filled one is never dissolved, as before. Still out: the alchemy Transmutation Table (the owner excludes it from alchemy), the five Jimta industrial tables and the carbon and electric boats (made of steel, plastics, invar and silver wire, still unpriced: see Metals), and potted saplings (a pot placed with a sapling, not something to make).

**Only items with a real price are made.** Six are not yet, because they are made of steel or other metal parts whose prices are still the one-essence fallback (`access_floor_tile`, `black_iron_column`, `concrete_column`, `fluted_marble_column`, `imperial_gas_lamp`, `jimta_alloy_glass`): see Prices.

## Prices

New prices, in `tools/build_alchemy_values.py` and `data/jugcraft/alchemy/values.json` (marked "local decorative accounting"):
- weathered copper is worth the copper it grew from; concrete is its powder; pale moss is moss; mushroom blocks and stems are a mushroom; the warped wart block is a nether wart block; wildflowers are a yellow dye; rooted dirt is dirt; a stripped bamboo block is a bamboo block;
- mud is 1 (dirt and water), packed mud 25 (mud and wheat), mud bricks 25, a mud brick slab 12½; resin clumps 16 and resin bricks 16 a block; glow ink sacs 64, so a glow item frame is 96¼;
- **provisional, for the owner:** froglights 64, shroomlights 64, the bell 8,192, resin clumps 16, glow ink sacs 64.

The builder needs the game's jar, which was not available, so these records were written as it computes them; where a vanilla recipe's yield was uncertain (mud bricks, resin bricks) the lower reading was used, so a block can never be dissolved for more than it costs to craft. The owner's next run of the builder replaces them with its own exact figures.

### Jugcraft materials priced

Many Jugcraft materials had no price and fell to the one-essence fallback, so whatever was made of them was nearly free: a Landscapes plank was worth a quarter. They now have prices (`tools/data/alchemy_decorative_roots.json`, read by the builder), and everything made from them was recomputed from its recipe:
- the logs, stripped logs and wood of the Landscapes, Caverns and Ecology woods are 32 and their leaves 1, like vanilla's, so their planks are 8;
- flowers are 16 like vanilla's small flowers (tall lavender 32, like a tall flower), the curly fern and tiny cactus like a fern and a cactus;
- natural stone (abyssmarine, orpiment, sphalerite, limestone, circuitstone) is 1, like stone; sourcestone 4; snail shells 16;
- **provisional, for the owner:** gingerbread crumbs 8, wilden spikes 64, maple syrup bottles 33, guano 16, toxic paste 16, sawdust 1, fat 16, source gems 128, froglass 64, rainbow jelly 64, raw neodymium 512, sea glass shards 4, notor gizmos 64, candy canes 16, heavy bones 144, pixel shards 16, ornamental corn, turnips, flax and bittersweet 16-24, honeycomb 32, jack-o'-lanterns 153, totems of undying 4,096, wither skeleton skulls 8,192, and the plushes 64;
- Jugcraft saplings are 32 like vanilla's; **provisional:** Thornwood branches 4, a broken rubber boat 5, the scribe's table 128, magebloom 16, the Farseer arm 64;
- **sulfur dust is 96**: two and a gravel craft gunpowder (192), and at one essence that was 186 essence a craft.

Only Jugcraft prices that rested on an unpriced material changed (about 930); vanilla prices are untouched, because the game's own recipes, which the builder reads from the jar, may make them cheaper. Steel, zinc, brass and coke were left unpriced: machines make their plates, blocks and dust from forms that are themselves still at the fallback, so a price for the ingot alone opened loops through those machines (a molding press makes a gasket from 3/4 of an essence of silicone; at a steel plate of 384, a crafted gasket was worth 96). They need the whole metal chain priced at once.

### A loop closed

Eight tablet-made tuff (32 essence) and one Manipulation Essence (an uncommon loot item, priced at the one-essence fallback) crafted into eight calcite, which dissolved for 256: 223 essence a time. Deepslate to tuff did the same for 15. Manipulation Essence is now priced 224, the least that makes both recipes gain nothing. It only comes from loot, so the higher price cannot be farmed.

## Note for the owner

**Modded woods missing pieces.** Vanilla woods have a full set; these Jugcraft woods lack the pieces listed (found from the registered items):

| Wood | Missing |
| --- | --- |
| Aspen (`aspen`) | door, trapdoor, button, pressure plate, sign, hanging sign, shelf, boat |
| Bald Cypress (`bald_cypress`) | door, trapdoor, button, pressure plate, sign, hanging sign, shelf, boat |
| Baobab (`baobab`) | door, trapdoor, button, pressure plate, sign, hanging sign, shelf, boat |
| Pewen (`cavern_pewen`) | shelf |
| Thornwood (`cavern_thornwood`) | shelf |
| Cedar (`cedar`) | door, trapdoor, button, pressure plate, sign, hanging sign, shelf, boat |
| Chestnut (`chestnut`) | door, trapdoor, button, pressure plate, sign, hanging sign, shelf, boat |
| Cypress (`cypress`) | door, trapdoor, button, pressure plate, sign, hanging sign, shelf, boat |
| Dead (`dead`) | door, trapdoor, button, pressure plate, sign, hanging sign, shelf, boat |
| Dragonblood (`dragonblood`) | door, trapdoor, button, pressure plate, sign, hanging sign, shelf, boat |
| Flowering Azalea (`ecology_flowering_azalea`) | stripped log, stripped wood, button, pressure plate, shelf |
| Eucalyptus (`eucalyptus`) | door, trapdoor, button, pressure plate, sign, hanging sign, shelf, boat |
| Fig (`fig`) | door, trapdoor, button, pressure plate, sign, hanging sign, shelf, boat |
| Fir (`fir`) | door, trapdoor, button, pressure plate, sign, hanging sign, shelf, boat |
| Fruitwood (`fruitwood`) | door, trapdoor, button, pressure plate, sign, hanging sign, shelf, boat |
| Holly (`holly`) | door, trapdoor, button, pressure plate, sign, hanging sign, shelf, boat |
| Jacaranda (`jacaranda`) | door, trapdoor, button, pressure plate, sign, hanging sign, shelf, boat |
| Juniper (`juniper`) | door, trapdoor, button, pressure plate, sign, hanging sign, shelf, boat |
| Larch (`larch`) | door, trapdoor, button, pressure plate, sign, hanging sign, shelf, boat |
| Magnolia (`magnolia`) | door, trapdoor, button, pressure plate, sign, hanging sign, shelf, boat |
| Mahogany (`mahogany`) | door, trapdoor, button, pressure plate, sign, hanging sign, shelf, boat |
| Mango (`mango`) | door, trapdoor, button, pressure plate, sign, hanging sign, shelf, boat |
| Maple (`maple`) | door, trapdoor, button, pressure plate, sign, hanging sign, shelf, boat |
| Olive (`olive`) | door, trapdoor, button, pressure plate, sign, hanging sign, shelf, boat |
| Palm (`palm`) | door, trapdoor, button, pressure plate, sign, hanging sign, shelf, boat |
| Persimmon (`persimmon`) | door, trapdoor, button, pressure plate, sign, hanging sign, shelf, boat |
| Pine (`pine`) | door, trapdoor, button, pressure plate, sign, hanging sign, shelf, boat |
| Redwood (`redwood`) | door, trapdoor, button, pressure plate, sign, hanging sign, shelf, boat |
| Tamarisk (`tamarisk`) | door, trapdoor, button, pressure plate, sign, hanging sign, shelf, boat |
| Walnut (`walnut`) | door, trapdoor, button, pressure plate, sign, hanging sign, shelf, boat |
| Willow (`willow`) | door, trapdoor, button, pressure plate, sign, hanging sign, shelf, boat |
| Yew (`yew`) | door, trapdoor, button, pressure plate, sign, hanging sign, shelf, boat |
| Yucca (`yucca`) | door, trapdoor, button, pressure plate, sign, hanging sign, shelf, boat |

**Metals.** Steel, zinc, brass and coke are still at the one-essence fallback, with every machine-made form of them. Pricing them needs the whole chain at once (ore, raw, dust, ingot, nugget, plate, block, and every machine recipe between them); until then the six steel and metal decorations above stay off the tablet.

## Connections

- **Input producer:** essence from dissolving, as before.
- **Output consumer:** building and decorating.
- **Unlocks:** none.
- **Failure behavior:** the tag's 74 vanilla items new to the price list are optional entries, so a missing one cannot stop the tag loading. An item removed from the tag stays learned and saved but is not offered, as before.

## Multiplayer and persistence

The server's tag decides; the tablet checks it before taking essence, as before. No new item, block, ID or saved data.

## Verification

- New server game test `AlchemyPlayabilityGameTests.tabletMakesDecorationsButNotReversibleQuartzOrCopper`: a stone brick wall, quartz pillar, oxidized cut copper, oak shelf, sea lantern, white concrete, maple planks and a household oak chair are tablet outputs with real prices and are made for exactly their price; the quartz block and slab, copper block, copper bulb, plain exposed copper and waxed cut copper are not outputs.
- New `scripts/tests/test_alchemy_values.py` checks: every tablet output has a real (not fallback) price and the excluded quartz and copper are not outputs; the tuff and calcite recipes and the sulfur gunpowder recipe gain nothing; Landscapes and Caverns planks cost what oak planks do.
- Local: `python3 tools/generate_material_data.py`, then `python3 tools/check_mod_data.py` passed (8186 material IDs), `python3 scripts/check_repository.py` passed, and `python3 -m unittest discover -s scripts/tests` passed. The Java parses (tree-sitter); it was not compiled locally.
- CI on the fork: [Build run 40983577991](https://github.com/mmme123-spec/jugcraftfork/actions/runs/40983577991) passed on 4d7c7c995 (compilation, server game tests with and without the optional integrations, and all 16 rendered client shards, including the new tablet test). Its "optional integrations absent" job first failed only on `IronGunsIIGameTests` "comet pistol fires a flying microjet from one hand" (the microjet flew 2.566 blocks in a tick, not about 2.5), which this change does not touch and which passed in the same run's main server job; one re-run of that job passed. Earlier runs: 40964716611 and 40971852577 failed compiling the new game test (26.3 has no `Items` constants for some copper and concrete blocks; they are looked up by id now); 40978477394 was cancelled for a newer commit.
- A local audit of every Jugcraft crafting, smelting, stonecutting and machine recipe, before and after these prices, found no recipe made more profitable by them. Hydroponics now grows turnips, flax and ornamental corn worth more than their seeds, as farming does. The vanilla recipes could not be audited here (no game jar).

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

**Also on the tablet, as the contributor asked on 30 October 2026:** every sapling (vanilla and Jugcraft, 79), every boat, chest boat and raft (67), containers (chests, trapped chests, barrels, and the household and crafted crates, coolers, drawers, cupboards, nightstands, shelves, storage cabinets and jars, mail and post boxes and recycling bins: 174), work tables (the vanilla crafting, cartography, fletching and smithing tables, loom, stonecutter and grindstone, and Jugcraft's artificer's, lampwright's and workshop benches, workbench, drafting, map, blueprint, spelunkery, fortune teller's and inscription tables, apothecary shelf, lab bench and lab table: 20) and the game tables (baccarat, blackjack, craps, roulette and derby tables, the arcade cabinet and lottery kiosk: 7). A container is made empty, and a filled one is never dissolved, as before. Of the household appliances, the grills and trampolines (16 colours each), the cutting boards (11 woods), the light and dark fridges, freezers, stoves, microwaves, toasters, range hoods and light switches, the doorbell and the oven (59) are made; the electricity generators, computer, television, remote and the coffee and microwave counters, and the scribe's, alteration, judges' and hologram tables, lab sink and Beast transmutation table, stay off, as the contributor chose; so do potted saplings (a pot placed with a sapling, not something to make).

**Only items with a real price are made.** The six steel and metal decorations (`access_floor_tile`, `black_iron_column`, `concrete_column`, `fluted_marble_column`, `imperial_gas_lamp`, `jimta_alloy_glass`), the five Jimta industrial tables (advanced crafting, assembly, charging, integration and programming) and the carbon and electric boats joined once the metal chain was priced (see Metals).

## Prices

New prices, in `tools/build_alchemy_values.py` and `data/jugcraft/alchemy/values.json` (marked "local decorative accounting"):
- weathered copper is worth the copper it grew from; concrete is its powder; pale moss is moss; mushroom blocks and stems are a mushroom; the warped wart block is a nether wart block; wildflowers are a yellow dye; rooted dirt is dirt; a stripped bamboo block is a bamboo block;
- mud is 5, the contributor's price, packed mud 29 (mud and wheat), mud bricks 29, a mud brick slab 14½. Water treatment dries a mud into four clay balls (64): that stays a farm, like vanilla's dripstone, since a water bottle makes mud from dirt whatever mud is priced at; resin clumps 16 and resin bricks 16 a block; glow ink sacs 64, so a glow item frame is 96¼;
- **provisional, for the owner:** froglights 64, shroomlights 64, the bell 8,192, resin clumps 16, glow ink sacs 64.

The builder needs the game's jar, which was not available, so these records were written as it computes them; where a vanilla recipe's yield was uncertain (mud bricks, resin bricks) the lower reading was used, so a block can never be dissolved for more than it costs to craft. The owner's next run of the builder replaces them with its own exact figures.

### Jugcraft materials priced

Many Jugcraft materials had no price and fell to the one-essence fallback, so whatever was made of them was nearly free: a Landscapes plank was worth a quarter. They now have prices (`tools/data/alchemy_decorative_roots.json`, read by the builder), and everything made from them was recomputed from its recipe:
- the logs, stripped logs and wood of the Landscapes, Caverns and Ecology woods are 32 and their leaves 1, like vanilla's, so their planks are 8;
- flowers are 16 like vanilla's small flowers (tall lavender 32, like a tall flower), the curly fern and tiny cactus like a fern and a cactus;
- natural stone (abyssmarine, orpiment, sphalerite, limestone, circuitstone) is 1, like stone; sourcestone 4; snail shells 16;
- **provisional, for the owner:** gingerbread crumbs 8, wilden spikes 64, maple syrup bottles 33, guano 16, toxic paste 16, sawdust 1, fat 16, source gems 128, froglass 64, rainbow jelly 64, raw neodymium 512, sea glass shards 4, notor gizmos 64, candy canes 16, heavy bones 144, pixel shards 16, ornamental corn, turnips, flax and bittersweet 16-24, honeycomb 32, jack-o'-lanterns 153, totems of undying 4,096, wither skeleton skulls 8,192, and the plushes 64;
- Jugcraft saplings are 32 like vanilla's, except, as the contributor asked on 30 October 2026, Magic saplings (64) and the four Ars Nouveau archwood saplings (128), with their wood to match: Magic logs 64 (planks 16) and archwood logs and wood 128 (planks 32), set directly because the shared log and plank tags otherwise fix them at vanilla's 32 and 8. Everything made from them was recomputed: the woods' slabs, stairs, doors, signs, boats and the rest, the archwood chest, grate and sconce, the Ars Nouveau rituals and scrolls made with archwood (the Ritual of Awakening, made with all four saplings, is 1,152), the imbuement chamber, dowsing rod, spell prism and ghost train; **provisional:** Thornwood branches 4, a broken rubber boat 5 (and, priced while work tables were considered, the scribe's table 128, magebloom 16 and the Farseer arm 64);
- **sulfur dust is 96**: two and a gravel craft gunpowder (192), and at one essence that was 186 essence a craft.

Only Jugcraft prices that rested on an unpriced material changed (about 930); vanilla prices are untouched, because the game's own recipes, which the builder reads from the jar, may make them cheaper.

### Metals

Priced on 30 October 2026, as the contributor asked, the way the game already prices iron, copper and tin: by the ingot, with ores, raw ore and dust left as mined (so smelting what you mine still earns, as it does for iron). Every item whose price rested on an unpriced metal or part was recomputed from its cheapest real route, **machine recipes included** (a copy of the builder that also reads pressing, sawing, cutting, compressing, alloying, wire drawing, coking, steelmaking, chemical reactions and cooking, with fluids free and byproducts counted; growing machines are left out, since farms earn by design). About 1,110 prices changed; the 196 that only a machine recipe sets are written into `tools/data/alchemy_decorative_roots.json`, because the builder reads no machine recipe.
- coke 128 (a coal in the coke oven); **steel 384** (an iron ingot and a coke), its plate 384 (pressed), block 3,456, dense plate 3,456 (nine plates), casing 192;
- **zinc 128**, like copper (provisional), so **brass 128** (three copper and a zinc make four); invar plate 512; silver wire 170⅔ (three drawn from a silver ingot); precision shaft 384; porcelain insulator about 65;
- plastics as the owner's plastic sheet: LDPE pellets and PVC resin 1, their offcuts ¼ (four reclaim into a pellet), film and profile 1¼;
- **provisional:** bauxite 32, borax 16, aluminum hydroxide 16 (a bauxite makes two);
- the molding press still makes a gasket from ¾ of an essence of silicone, so the gasket stays ¾, not the 96 a steel plate would make it;
- titanium was left alone: its sponge is made from magnesium that is still unpriced, and a price for the sponge alone opened a loop through electrolysis.

A local audit of every crafting and machine recipe found no recipe made more profitable by these prices except smelting mined ore and dust into ingots, which the game already does for every metal. It also found that cinder block walls and stairs cost half an essence more than the block they are cut from (now the same).

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

**Metals.** Priced (see Metals above). Still unpriced: magnesium and the titanium sponge made from it (the brine and magnesium chloride chain), which only titanium needs.

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
- CI on the fork for the saplings, boats, containers, appliances and work and game tables: [Build run 41113970377](https://github.com/mmme123-spec/jugcraftfork/actions/runs/41113970377) passed on 937cc227a (compilation, server game tests with and without the optional integrations, all 16 rendered client shards, and the tablet test with a sapling, boat, chest, Landscapes boat, household drawer and fridge, smithing table, lampwright's bench and blackjack table); runs 41102757054, 41105949190 and 41107810760 were cancelled for newer commits.
- CI on the fork for the Magic and archwood prices: [Build run 41125523812](https://github.com/mmme123-spec/jugcraftfork/actions/runs/41125523812) passed on 1d3990d6b. Its main server job first failed only on two `JimtaLuaGameTests` ("Lua response deadline exceeded" in the isolated coroutine and ROM shell tests), which these prices do not touch and which passed in the same run's other server job and on 937cc227a; one re-run of that job passed.
- CI on the fork for the metal chain: [Build run 41152804231](https://github.com/mmme123-spec/jugcraftfork/actions/runs/41152804231) passed on 7ebc89a99 (compilation, server game tests with and without the optional integrations, including the catalogue balance and tablet tests, and all 16 rendered client shards).
- CI on the fork for mud at 5: [Build run 41167155836](https://github.com/mmme123-spec/jugcraftfork/actions/runs/41167155836) passed on ea55078b0.
- A local audit of every Jugcraft crafting, smelting, stonecutting and machine recipe, before and after these prices, found no recipe made more profitable by them. Hydroponics now grows turnips, flax and ornamental corn worth more than their seeds, as farming does. The vanilla recipes could not be audited here (no game jar).

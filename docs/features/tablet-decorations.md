# Decorations from the transmutation tablet

Status: implemented on the contributor's fork, pending review.

Proposal issue: none. On 29 October 2026 the contributor asked for the decorative blocks the tablet could not make (vanilla walls and building sets, bamboo, crimson, warped and poplar wood, shelves, leaves, copper, lights, plants, furnishings, and Jugcraft's woods, stone, glass, columns, lamps, furniture, pots and decorations) to be made, "not any of the quartz or copper blocks that can be turned back into materials or be crafted into non decorative items", and for a note to the owner that the modded woods lack doors and other pieces.

Owner: jimbozoomer-byte
Target milestone and tier: the transmutation tablet's tier; nothing new unlocks
Primary specialty and supported player role: builders and decorators

## Player experience

The tablet's list of what it may make (`jugcraft:tablet_outputs`) grows from 394 items to 1,609: 373 vanilla and 842 Jugcraft items. They are made the way the old ones are: dissolve one to learn it, then make more for its essence price. Nothing about dissolving changes.

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

**Kept out**, as asked: the quartz block (it makes the Craps Table) and quartz slab (the TSC Status Board), the copper block (it turns back into ingots), every copper bulb (one goes into the Thunderstorm scroll), the plain weathered copper blocks (they scrape back to a copper block), waxed copper, and Jugcraft's certus quartz bricks, pillar and glass (recipes use them). Saplings, boats and rafts are not decorations and stay out.

**Jugcraft:** every priced piece of 34 woods (logs, wood, planks, slabs, stairs, fences, gates, doors, trapdoors, buttons, plates, signs and leaves where they exist), stone and brick families, glass, columns, lamps, pots and planters, decorations, and household and crafted furniture (chairs, tables, desks, sofas, stools, benches, beds, cabinets, baths, basins, sinks, toilets, hedges, lattice fences, trims, tableware and trophies). Containers (crates, coolers, drawers, cupboards, nightstands, storage jars, mail boxes), appliances, machines, work tables, game tables, food, tools and boss, quest or ritual items are not decorations and stay out.

**Only items with a real price are made.** An item still on the one-essence fallback, or priced from one that is, is left out: see the owner note below.

## Prices

New prices, in `tools/build_alchemy_values.py` and `data/jugcraft/alchemy/values.json` (marked "local decorative accounting"):
- weathered copper is worth the copper it grew from; concrete is its powder; pale moss is moss; mushroom blocks and stems are a mushroom; the warped wart block is a nether wart block; wildflowers are a yellow dye; rooted dirt is dirt; a stripped bamboo block is a bamboo block;
- mud is 1 (dirt and water), packed mud 25 (mud and wheat), mud bricks 25, a mud brick slab 12½; resin clumps 16 and resin bricks 16 a block; glow ink sacs 64, so a glow item frame is 96¼;
- **provisional, for the owner:** froglights 64, shroomlights 64, the bell 8,192, resin clumps 16, glow ink sacs 64.

The builder needs the game's jar, which was not available, so these records were written as it computes them; where a vanilla recipe's yield was uncertain (mud bricks, resin bricks) the lower reading was used, so a block can never be dissolved for more than it costs to craft. The owner's next run of the builder replaces them with its own exact figures.

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

**Decorations left out for want of a price.** These are on the one-essence fallback, or priced from something that is, so the tablet would make them for almost nothing (the Landscapes, Caverns and Ecology logs are worth 1, so their planks are worth ¼ and a shelf 1). Give the roots real prices (a log is 32 like vanilla's) and run the builder, then add them to `tablet_outputs`:

- **Woods** (291 items, from these families): `cavern_pewen`, `cavern_thornwood`, `ecology_azalea`, `ecology_coconut`, `ecology_flowering_azalea`, `ecology_maple`, `ecology_walnut`, `jimta_spellbooks_wisewood`, `landscape_dead`, `landscape_empyreal`, `landscape_fir`, `landscape_hellbark`, `landscape_jacaranda`, `landscape_magic`, `landscape_mahogany`, `landscape_maple`, `landscape_origin_oak`, `landscape_palm`, `landscape_pine`, `landscape_redwood`, `landscape_umbran`, `landscape_willow`.
- **Stone and brick** (100): `cavern_abyssmarine`, `cavern_abyssmarine_brick_slab`, `cavern_abyssmarine_brick_stairs`, `cavern_abyssmarine_brick_wall`, `cavern_abyssmarine_bricks`, `cavern_abyssmarine_pillar`, `cavern_abyssmarine_slab`, `cavern_abyssmarine_stairs`, `cavern_abyssmarine_tiles`, `cavern_abyssmarine_wall`, `cavern_frosted_gingerbread_brick_slab`, `cavern_frosted_gingerbread_brick_stairs`, `cavern_frosted_gingerbread_brick_wall`, `cavern_frosted_gingerbread_bricks`, `cavern_frosted_gingerbread_slab`, `cavern_frosted_gingerbread_stairs`, `cavern_frosted_gingerbread_wall`, `cavern_gingerbread_brick_slab`, `cavern_gingerbread_brick_stairs`, `cavern_gingerbread_brick_wall`, `cavern_gingerbread_bricks`, `cavern_gingerbread_slab`, `cavern_gingerbread_stairs`, `cavern_gingerbread_wall`, `cavern_guanostone`, `cavern_guanostone_brick_slab`, `cavern_guanostone_brick_stairs`, `cavern_guanostone_brick_wall`, `cavern_guanostone_bricks`, `cavern_guanostone_slab`, `cavern_guanostone_stairs`, `cavern_guanostone_tiles`, `cavern_guanostone_wall`, `cavern_radrock`, `cavern_radrock_brick_slab`, `cavern_radrock_brick_stairs`, `cavern_radrock_brick_wall`, `cavern_radrock_bricks`, `cavern_radrock_slab`, `cavern_radrock_stairs`, `cavern_radrock_wall`, `circuitstone`, `circuitstone_bricks`, `ecology_seashell`, `ecology_seashell_tile_slab`, `ecology_seashell_tile_stairs`, `ecology_seashell_tile_wall`, `ecology_seashell_tiles`, `landscape_orpiment`, `landscape_orpiment_brick_slab`, `landscape_orpiment_brick_stairs`, `landscape_orpiment_brick_wall`, `landscape_orpiment_bricks`, `landscape_orpiment_slab`, `landscape_orpiment_stairs`, `landscape_orpiment_wall`, `landscape_sphalerite`, `landscape_sphalerite_brick_slab`, `landscape_sphalerite_brick_stairs`, `landscape_sphalerite_brick_wall`, `landscape_sphalerite_bricks`, `landscape_sphalerite_slab`, `landscape_sphalerite_stairs`, `landscape_sphalerite_wall`, `nouveau_gilded_sourcestone_large_bricks`, `nouveau_gilded_sourcestone_large_bricks_slab`, `nouveau_gilded_sourcestone_large_bricks_stairs`, `nouveau_gilded_sourcestone_small_bricks`, `nouveau_gilded_sourcestone_small_bricks_slab`, `nouveau_gilded_sourcestone_small_bricks_stairs`, `nouveau_smooth_gilded_sourcestone_large_bricks`, `nouveau_smooth_gilded_sourcestone_large_bricks_slab`, `nouveau_smooth_gilded_sourcestone_large_bricks_stairs`, `nouveau_smooth_gilded_sourcestone_small_bricks`, `nouveau_smooth_gilded_sourcestone_small_bricks_slab`, `nouveau_smooth_gilded_sourcestone_small_bricks_stairs`, `nouveau_smooth_sourcestone_large_bricks`, `nouveau_smooth_sourcestone_large_bricks_slab`, `nouveau_smooth_sourcestone_large_bricks_stairs`, `nouveau_smooth_sourcestone_small_bricks`, `nouveau_smooth_sourcestone_small_bricks_slab`, `nouveau_smooth_sourcestone_small_bricks_stairs`, `nouveau_sourcestone_large_bricks`, `nouveau_sourcestone_large_bricks_slab`, `nouveau_sourcestone_large_bricks_stairs`, `nouveau_sourcestone_small_bricks`, `nouveau_sourcestone_small_bricks_slab`, `nouveau_sourcestone_small_bricks_stairs`, `polished_circuitstone`, `station_tiles`, `station_tiles_slab`, `station_tiles_stairs`, `wildlife_shellstone`, `wildlife_shellstone_brick_slab`, `wildlife_shellstone_brick_stairs`, `wildlife_shellstone_brick_wall`, `wildlife_shellstone_bricks`, `wildlife_shellstone_slab`, `wildlife_shellstone_stairs`, `wildlife_shellstone_wall`.
- **Glass** (12): `beast_rainbow_glass`, `cavern_depth_glass`, `cavern_sugar_glass`, `jimta_alloy_glass`, `jimta_quartz_vibrant_glass`, `silhouette_window`, `wildlife_azure_froglass`, `wildlife_azure_froglass_pane`, `wildlife_crimson_froglass`, `wildlife_crimson_froglass_pane`, `wildlife_verdant_froglass`, `wildlife_verdant_froglass_pane`.
- **Columns, pillars and tiles** (20): `access_floor_tile`, `black_iron_column`, `cavern_azure_neodymium_pillar`, `cavern_limestone_pillar`, `cavern_scarlet_neodymium_pillar`, `concrete_column`, `crypt_stone_pillar`, `fluted_marble_column`, `nouveau_gilded_sourcestone_mosaic`, `nouveau_gilded_sourcestone_mosaic_slab`, `nouveau_gilded_sourcestone_mosaic_stairs`, `nouveau_smooth_gilded_sourcestone_mosaic`, `nouveau_smooth_gilded_sourcestone_mosaic_slab`, `nouveau_smooth_gilded_sourcestone_mosaic_stairs`, `nouveau_smooth_sourcestone_mosaic`, `nouveau_smooth_sourcestone_mosaic_slab`, `nouveau_smooth_sourcestone_mosaic_stairs`, `nouveau_sourcestone_mosaic`, `nouveau_sourcestone_mosaic_slab`, `nouveau_sourcestone_mosaic_stairs`.
- **Lamps, lanterns and candles** (39): `barn_lantern`, `black_pillar_candle`, `cafe_string_lights`, `candle_skull`, `cavern_magnetic_light`, `floating_candle`, `haunted_chandelier`, `household_acacia_dark_ceiling_fan`, `household_birch_dark_ceiling_fan`, `household_black_lamp`, `household_cherry_dark_ceiling_fan`, `household_crimson_dark_ceiling_fan`, `household_dark_ceiling_light`, `household_dark_oak_dark_ceiling_fan`, `household_green_lamp`, `household_jungle_dark_ceiling_fan`, `household_light_blue_lamp`, `household_lime_lamp`, `household_magenta_lamp`, `household_mangrove_dark_ceiling_fan`, `household_oak_dark_ceiling_fan`, `household_orange_lamp`, `household_pale_oak_dark_ceiling_fan`, `household_purple_lamp`, `household_red_lamp`, `household_spruce_dark_ceiling_fan`, `household_warped_dark_ceiling_fan`, `hurricane_lamp`, `imperial_gas_lamp`, `ivory_pillar_candle`, `jack_o_lantern_string_lights`, `mason_jar_candle`, `nouveau_archwood_sconce`, `nouveau_polished_sconce`, `nouveau_sourcestone_sconce`, `pixel_lamp`, `pumpkin_path_light`, `sky_lantern`, `turnip_lantern`.
- **Pots and planters** (2): `galvanized_tub_planter`, `half_barrel_planter`.
- **Furniture** (62): `crafted_evoker_trophy`, `crafted_wither_skeleton_trophy`, `ecology_azalea_cabinet`, `ecology_coconut_cabinet`, `ecology_flowering_azalea_cabinet`, `ecology_maple_cabinet`, `household_black_basin`, `household_black_bath`, `household_black_kitchen_cabinetry`, `household_black_kitchen_sink`, `household_black_sofa`, `household_black_stool`, `household_black_toilet`, `household_green_basin`, `household_green_bath`, `household_green_kitchen_cabinetry`, `household_green_kitchen_sink`, `household_green_sofa`, `household_green_stool`, `household_green_toilet`, `household_light_blue_basin`, `household_light_blue_bath`, `household_light_blue_kitchen_cabinetry`, `household_light_blue_kitchen_sink`, `household_light_blue_sofa`, `household_light_blue_stool`, `household_light_blue_toilet`, `household_lime_basin`, `household_lime_bath`, `household_lime_kitchen_cabinetry`, `household_lime_kitchen_sink`, `household_lime_sofa`, `household_lime_stool`, `household_lime_toilet`, `household_magenta_basin`, `household_magenta_bath`, `household_magenta_kitchen_cabinetry`, `household_magenta_kitchen_sink`, `household_magenta_sofa`, `household_magenta_stool`, `household_magenta_toilet`, `household_orange_basin`, `household_orange_bath`, `household_orange_kitchen_cabinetry`, `household_orange_kitchen_sink`, `household_orange_sofa`, `household_orange_stool`, `household_orange_toilet`, `household_purple_basin`, `household_purple_bath`, `household_purple_kitchen_cabinetry`, `household_purple_kitchen_sink`, `household_purple_sofa`, `household_purple_stool`, `household_purple_toilet`, `household_red_basin`, `household_red_bath`, `household_red_kitchen_cabinetry`, `household_red_kitchen_sink`, `household_red_sofa`, `household_red_stool`, `household_red_toilet`.
- **Decorations** (21): `acorn_plush`, `autumn_wreath`, `bat_bunting`, `bat_plush`, `bat_wreath`, `bittersweet_wreath`, `black_cat_plush`, `corn_plush`, `ghost_plush`, `harvest_bunting`, `hedgehog_plush`, `holly_wreath`, `jumbo_pumpkin_plush`, `mantel_garland`, `maple_leaf_plush`, `nouveau_starbuncle_plush`, `owl_plush`, `pumpkin_plush`, `squirrel_plush`, `sugar_skull`, `werewolf_plush`.

Left out under the quartz rule (recipes use them): `jimta_quartz_bricks`, `jimta_quartz_glass`, `jimta_quartz_pillar`.

**A loop outside the tablet.** Two Cavern sulfur dust (fallback, 1 each) and a gravel craft into gunpowder, which dissolves for 192. Sulfur dust is mined, not made by the tablet, so this was left alone; it needs a sulfur dust price of at least 94, or the gunpowder recipe changed.

## Connections

- **Input producer:** essence from dissolving, as before.
- **Output consumer:** building and decorating.
- **Unlocks:** none.
- **Failure behavior:** the tag's 74 vanilla items new to the price list are optional entries, so a missing one cannot stop the tag loading. An item removed from the tag stays learned and saved but is not offered, as before.

## Multiplayer and persistence

The server's tag decides; the tablet checks it before taking essence, as before. No new item, block, ID or saved data.

## Verification

- New server game test `AlchemyPlayabilityGameTests.tabletMakesDecorationsButNotReversibleQuartzOrCopper`: a stone brick wall, quartz pillar, oxidized cut copper, oak shelf, sea lantern, white concrete, maple planks and a household oak chair are tablet outputs with real prices and are made for exactly their price; the quartz block and slab, copper block, copper bulb, plain exposed copper and waxed cut copper are not outputs.
- New `scripts/tests/test_alchemy_values.py` checks: every tablet output has a real (not fallback) price and the excluded quartz and copper are not outputs; the tuff and calcite recipes gain nothing.
- Local: `python3 tools/generate_material_data.py`, then `python3 tools/check_mod_data.py` passed (8186 material IDs), `python3 scripts/check_repository.py` passed, and `python3 -m unittest discover -s scripts/tests` passed. The Java parses (tree-sitter); it was not compiled locally.
- A local audit of every Jugcraft recipe found no recipe that turns a tablet output into more essence than it costs, apart from the two loops above. The vanilla recipes could not be audited here (no game jar).

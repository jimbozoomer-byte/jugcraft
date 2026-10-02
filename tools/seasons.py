"""Seasons: which biomes change with the server's date, and the seasonal snow block.

#jugcraft:has_seasons: grass and foliage change colour (client/SeasonColors, season/SeasonPalette). Every
biome with four seasons: temperate grassland, forest, taiga, hills, swamp and river. Not the tropics (jungle,
mangrove swamp), the arid lands (desert, savanna, badlands), the always-frozen biomes (snowy plains, ice spikes,
the peaks, grove), oceans, beaches or caves. The pale garden keeps its fixed grey; birch, spruce, cherry and
poplar leaves keep their fixed colours, so in their biomes only grass and oak-type leaves change.

#jugcraft:has_winter_snow: with seasons.snow=on, winter rain falls as snow there and lies as seasonal snow
(season/SeasonalSnow) that melts in spring. The seasonal biomes and the pale garden, but not rivers, which
also run through deserts.
"""

BIOMES = [
    "minecraft:plains", "minecraft:sunflower_plains", "minecraft:meadow",
    "minecraft:forest", "minecraft:flower_forest", "minecraft:birch_forest", "minecraft:old_growth_birch_forest",
    "minecraft:dark_forest", "minecraft:dappled_forest", "minecraft:cherry_grove",
    "minecraft:taiga", "minecraft:old_growth_pine_taiga", "minecraft:old_growth_spruce_taiga",
    "minecraft:windswept_forest", "minecraft:windswept_hills", "minecraft:windswept_gravelly_hills",
    "minecraft:swamp", "minecraft:river",
]

TAG = "jugcraft:has_seasons"

WINTER_SNOW = [biome for biome in BIOMES if biome != "minecraft:river"] + ["minecraft:pale_garden"]

WINTER_SNOW_TAG = "jugcraft:has_winter_snow"

# The seasonal snow block: vanilla snow layers' looks (its models) and drops (a snowball per layer), its own block
# so that only snow the season laid melts in spring.
SNOW_BLOCK = "seasonal_snow"
SNOW_DISPLAY = "Seasonal Snow"

BLOCKS = [SNOW_BLOCK]

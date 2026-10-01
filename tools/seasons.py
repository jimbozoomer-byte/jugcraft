"""Seasonal colours: which biomes change colour with the server's date.

The tag #jugcraft:has_seasons lists them; client/SeasonColors tints grass and foliage there by
season/SeasonPalette. Only temperate biomes: deserts, jungles, savannas, swamps, snow, oceans and biomes
with fixed colours (cherry grove, pale garden, badlands) stay as they are. Birch and spruce leaves have fixed
colours in vanilla, so they do not change either; their biomes still change grass.
"""

BIOMES = [
    "minecraft:plains", "minecraft:sunflower_plains", "minecraft:meadow",
    "minecraft:forest", "minecraft:flower_forest", "minecraft:birch_forest", "minecraft:old_growth_birch_forest",
    "minecraft:dark_forest",
    "minecraft:taiga", "minecraft:old_growth_pine_taiga", "minecraft:old_growth_spruce_taiga",
    "minecraft:windswept_forest", "minecraft:windswept_hills", "minecraft:windswept_gravelly_hills",
    "minecraft:river",
]

TAG = "jugcraft:has_seasons"

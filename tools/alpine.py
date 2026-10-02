"""Alpine Spawn: a large, cool alpine meadow on mountain plateaus, where new worlds start.

The biome takes the place of vanilla's cool meadows in the Overworld climate table: every meadow entry whose
temperature band is cool (at most -0.15, vanilla's cool band is -0.45..-0.15) becomes Alpine Spawn
(world/AlpineSpawn, through mixin/OverworldBiomeBuilderMixin). Temperate meadows stay meadows.

New worlds start in it: on a new world's first start the server finds the nearest Alpine Spawn within
SPAWN["radius"] blocks of the origin and moves the world spawn there, onto a village when one stands in the biome
within SPAWN["village_chunks"] chunks. Villages are common: taiga-style villages (and their Retro Game Shop) on
their own grid, VILLAGES, much tighter than vanilla's 34 chunks.

Seasons: its grass and foliage follow the season colours, and winter snow covers it.
"""

BIOME = "alpine_spawn"
DISPLAY = "Alpine Spawn"
FEATURE = "alpine_spawn"

# The biome's own climate values (for colours, rain and snow): cooler and wetter than vanilla's meadow (0.5, 0.8
# temperature 0.5, downfall 0.8), so grass and leaves are a cool green and snow lies on the high ground.
TEMPERATURE = 0.3
DOWNFALL = 0.7
WATER_COLOR = "#3d6ee0"
# Fallen leaves (vanilla leaf litter) take this colour: larch gold.
DRY_FOLIAGE_COLOR = "#b8902a"
# Which vanilla climate entries become Alpine Spawn: meadows in the cool band (temperature at most -0.15).
REPLACES = "minecraft:meadow"
COOL_MAX = -0.15

SPAWN = {"radius": 6400, "step": 64, "village_chunks": 24}

# Its own villages: vanilla's taiga villages (spruce houses suit the mountains), on a tight grid.
VILLAGE = "village_alpine"
VILLAGE_SET = "alpine_villages"
VILLAGES = {"spacing": 16, "separation": 5, "salt": 20261002}
VILLAGE_TAG = "jugcraft:has_structure/village_alpine"

# Vanilla biome tags it joins, like the meadow (but its own villages instead of plains villages).
BIOME_TAGS = ["minecraft:is_overworld", "minecraft:is_mountain", "minecraft:stronghold_biased_to",
              "minecraft:has_structure/trial_chambers", "minecraft:has_structure/abandoned_camp_meadow"]

# Trees for now: vanilla spruces, scattered (larches come with the next part).
TREES = {"feature": "minecraft:spruce", "count": 1, "extra_chance": 0.5, "sapling": "minecraft:spruce_sapling"}

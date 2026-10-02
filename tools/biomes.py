"""The biomes branch (docs/branches/BIOMES.md): Jugcraft regions and the biomes in them.

Jugcraft regions (biome/JugcraftRegions, docs/features/biome-regions.md): the Overworld is divided into irregular
cells about REGIONS["size"] blocks across, and REGIONS["share"] of them use the Jugcraft layout: vanilla's climate
table with RULES applied (a vanilla biome, in the given climate bands, becomes a Jugcraft biome). The rest stay vanilla.
Every Jugcraft biome is also listed in vanilla's table at a climate no place has, so world generation knows it.

Biomes are built from the vanilla biome they replace (biome_bases.BASES): its ores, caves, lakes and springs, with
their own trees and plants (trees.SHAPES). Batch 1 is the seasonal forests.
"""

MOD = "jugcraft"
FEATURE = "biomes"

REGIONS = {"size": 1024, "share": 0.5}

# Vanilla's climate band edges (OverworldBiomeBuilder): an entry's band is where the middle of its range falls,
# 0 = coldest (driest) to 4 = hottest (wettest).
TEMPERATURE_BANDS = [-0.45, -0.15, 0.2, 0.55]
HUMIDITY_BANDS = [-0.35, -0.1, 0.1, 0.3]

# The Jugcraft layout: (vanilla biome, temperature bands [min, max], humidity bands [min, max], Jugcraft biome).
RULES = [
    ("minecraft:taiga", (1, 1), (0, 4), "coniferous_forest"),
    ("minecraft:snowy_taiga", (0, 0), (0, 4), "snowy_coniferous_forest"),
    ("minecraft:forest", (1, 1), (0, 4), "maple_woods"),
    ("minecraft:forest", (2, 2), (0, 4), "seasonal_forest"),
    ("minecraft:birch_forest", (0, 4), (0, 4), "aspen_glade"),
    ("minecraft:old_growth_birch_forest", (0, 4), (0, 4), "aspen_glade"),
    ("minecraft:plains", (1, 1), (0, 0), "dead_forest"),
    ("minecraft:plains", (1, 1), (1, 1), "tundra"),
    ("minecraft:snowy_plains", (0, 0), (2, 2), "snowy_forest"),
    ("minecraft:snowy_plains", (0, 0), (1, 1), "muskeg"),
]

# Features the biomes add, in one fixed order, after their base's own (keeps the Overworld's feature order acyclic).
EXTRAS = {
    "tundra_rocks": {"feature": "minecraft:forest_rock", "step": 2, "placement": [
        {"type": "minecraft:rarity_filter", "chance": 2}, {"type": "minecraft:in_square"},
        {"type": "minecraft:heightmap", "heightmap": "MOTION_BLOCKING"}, {"type": "minecraft:biome"}]},
    "patch_dead_bush": {"feature": "minecraft:dead_bush", "step": 9, "count": 4},
    "patch_pumpkin_dense": {"feature": "minecraft:pumpkin", "step": 9, "rarity": 24, "count": 96, "on": "minecraft:grass_block"},
}

# Each biome: its base, climate values, trees (count: [usual, sometimes]; default and weighted picks of placed
# features), base features it drops or swaps, extras it adds, mob changes, tags, and seasons.
# "seasons": in #jugcraft:has_seasons and #jugcraft:has_winter_snow (four-season biomes; not the frozen ones).
BIOMES = {
    "coniferous_forest": {
        "display": "Coniferous Forest", "base": "taiga", "temperature": 0.25, "downfall": 0.8, "seasons": True,
        "trees": {"count": [9, 10], "default": "jugcraft:fir_checked", "picks": [
            ["jugcraft:tall_fir_checked", 0.25], ["minecraft:spruce_checked", 0.08], ["jugcraft:fallen_fir_tree", 0.02]]},
        "tags": ["c:is_coniferous_tree"],
    },
    "snowy_coniferous_forest": {
        "display": "Snowy Coniferous Forest", "base": "snowy_taiga", "temperature": -0.5, "downfall": 0.4, "seasons": False,
        "trees": {"count": [7, 8], "default": "jugcraft:fir_checked", "picks": [
            ["jugcraft:tall_fir_checked", 0.2], ["jugcraft:fallen_fir_tree", 0.02]]},
        "tags": ["c:is_coniferous_tree", "c:is_snowy"],
    },
    "maple_woods": {
        "display": "Maple Woods", "base": "forest", "temperature": 0.45, "downfall": 0.8, "seasons": True,
        "trees": {"count": [10, 11], "default": "jugcraft:maple_checked", "picks": [
            ["jugcraft:big_maple_checked", 0.12], ["minecraft:spruce_checked", 0.12], ["jugcraft:fallen_maple_tree", 0.0125]]},
        "tags": ["minecraft:has_structure/village_taiga", "c:is_deciduous_tree"],
    },
    "seasonal_forest": {
        "display": "Seasonal Forest", "base": "forest", "temperature": 0.6, "downfall": 0.8, "seasons": True,
        "trees": {"count": [10, 11], "default": "minecraft:oak_bees_0002_leaf_litter", "picks": [
            ["jugcraft:maple_checked", 0.35], ["jugcraft:aspen_checked", 0.2], ["jugcraft:big_maple_checked", 0.05],
            ["jugcraft:fallen_maple_tree", 0.0125]]},
        "extras": ["patch_pumpkin_dense"],
        "tags": ["minecraft:has_structure/village_plains", "c:is_deciduous_tree"],
    },
    "aspen_glade": {
        "display": "Aspen Glade", "base": "birch_forest", "temperature": 0.5, "downfall": 0.6, "seasons": True,
        "trees": {"count": [10, 11], "default": "jugcraft:aspen_checked", "picks": [
            ["jugcraft:maple_checked", 0.1], ["jugcraft:fallen_aspen_tree", 0.0125]]},
        "tags": ["minecraft:has_structure/village_plains", "c:is_deciduous_tree", "c:is_birch_forest"],
    },
    "dead_forest": {
        "display": "Dead Forest", "base": "plains", "temperature": 0.4, "downfall": 0.25, "seasons": True,
        "effects": {"grass_color": "#a39a5e", "foliage_color": "#9c8a52"},
        "trees": {"count": [2, 3], "default": "jugcraft:dead_tree_checked", "picks": [
            ["minecraft:spruce_checked", 0.15], ["minecraft:oak_checked", 0.1], ["jugcraft:fallen_dead_tree", 0.05]]},
        "drop": ["minecraft:patch_tall_grass_2", "minecraft:flower_plains"],
        "swap": {"minecraft:patch_grass_plain": "minecraft:patch_grass_badlands"},
        "extras": ["patch_dead_bush"],
        "creatures": [["minecraft:rabbit", 4, 2, 3], ["minecraft:wolf", 2, 2, 4]],
        "untags": ["minecraft:has_structure/village_plains"],
        "tags": ["c:is_dead"],
    },
    # Tundra is as warm as vanilla taiga (0.25), so vanilla snow only lies above about y 160: lower down its snow is
    # Jugcraft's winter.
    "tundra": {
        "display": "Tundra", "base": "plains", "temperature": 0.25, "downfall": 0.5, "seasons": True,
        "effects": {"grass_color": "#9a9a5e", "foliage_color": "#a07a3c"},
        "trees": {"count": [3, 4], "default": "jugcraft:maple_bush_checked", "picks": []},
        "drop": ["minecraft:flower_plains", "minecraft:patch_tall_grass_2"],
        "swap": {"minecraft:patch_grass_plain": "minecraft:patch_grass_taiga_2"},
        "extras": ["tundra_rocks", "patch_dead_bush"],
        "creatures": [["minecraft:rabbit", 6, 2, 3], ["minecraft:fox", 4, 2, 4], ["minecraft:wolf", 2, 2, 4]],
        "untags": ["minecraft:has_structure/village_plains"],
        "tags": ["minecraft:has_structure/village_taiga"],
    },
    "snowy_forest": {
        "display": "Snowy Forest", "base": "snowy_plains", "temperature": -0.3, "downfall": 0.5, "seasons": False,
        "trees": {"count": [7, 8], "default": "minecraft:oak_checked", "picks": [
            ["jugcraft:fir_checked", 0.3], ["jugcraft:maple_checked", 0.2]]},
        "creatures": [["minecraft:sheep", 12, 4, 4], ["minecraft:pig", 10, 4, 4], ["minecraft:chicken", 10, 4, 4],
                      ["minecraft:cow", 8, 4, 4], ["minecraft:rabbit", 6, 2, 3], ["minecraft:wolf", 5, 4, 4]],
        "untags": ["minecraft:has_structure/village_snowy"],
        "tags": ["c:is_snowy", "c:is_deciduous_tree"],
    },
    "muskeg": {
        "display": "Muskeg", "base": "snowy_plains", "temperature": -0.2, "downfall": 0.9, "seasons": False,
        "effects": {"grass_color": "#8c9a6a", "foliage_color": "#7f8f5a"},
        "trees": {"count": [2, 3], "default": "jugcraft:dead_tree_checked", "picks": [
            ["jugcraft:fir_checked", 0.25], ["jugcraft:fallen_dead_tree", 0.05]]},
        "creatures": [["minecraft:rabbit", 6, 2, 3]],
        "untags": ["minecraft:has_structure/village_snowy"],
        "tags": ["c:is_snowy", "c:is_dead"],
    },
}

# The vanilla biome tags each base belongs to directly (vanilla 26.3's tag files); a biome joins its base's, less
# "untags", plus its own "tags". Tags nested inside these (mineshafts, for example) follow.
BASE_TAGS = {
    "taiga": ["minecraft:has_structure/pillager_outpost", "minecraft:has_structure/trail_ruins",
              "minecraft:has_structure/trial_chambers", "minecraft:has_structure/village_taiga", "minecraft:is_overworld",
              "minecraft:is_taiga", "minecraft:spawns_cold_variant_farm_animals", "minecraft:stronghold_biased_to"],
    "snowy_taiga": ["minecraft:has_structure/igloo", "minecraft:has_structure/trail_ruins",
                    "minecraft:has_structure/trial_chambers", "minecraft:is_overworld", "minecraft:is_taiga",
                    "minecraft:spawns_cold_variant_farm_animals", "minecraft:spawns_cold_variant_frogs",
                    "minecraft:spawns_snow_foxes", "minecraft:spawns_white_rabbits", "minecraft:stronghold_biased_to"],
    "forest": ["minecraft:has_structure/trial_chambers", "minecraft:is_forest", "minecraft:is_overworld",
               "minecraft:stronghold_biased_to"],
    "birch_forest": ["minecraft:has_structure/trial_chambers", "minecraft:is_forest", "minecraft:is_overworld",
                     "minecraft:stronghold_biased_to"],
    "plains": ["minecraft:has_structure/mineshaft", "minecraft:has_structure/pillager_outpost",
               "minecraft:has_structure/ruined_portal_standard", "minecraft:has_structure/trial_chambers",
               "minecraft:has_structure/village_plains", "minecraft:is_overworld", "minecraft:stronghold_biased_to"],
    "snowy_plains": ["minecraft:has_structure/igloo", "minecraft:has_structure/mineshaft",
                     "minecraft:has_structure/pillager_outpost", "minecraft:has_structure/ruined_portal_standard",
                     "minecraft:has_structure/trial_chambers", "minecraft:has_structure/village_snowy",
                     "minecraft:is_overworld", "minecraft:spawns_cold_variant_farm_animals",
                     "minecraft:spawns_cold_variant_frogs", "minecraft:spawns_snow_foxes", "minecraft:spawns_white_rabbits",
                     "minecraft:stronghold_biased_to"],
}


def biome_tags(name):
    info = BIOMES[name]
    tags = [t for t in BASE_TAGS[info["base"]] if t not in info.get("untags", [])]
    return tags + [t for t in info.get("tags", []) if t not in tags]

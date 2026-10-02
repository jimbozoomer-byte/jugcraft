"""The biomes branch (docs/branches/BIOMES.md): Jugcraft regions and the biomes in them.

Jugcraft regions (biome/JugcraftRegions, docs/features/biome-regions.md): the Overworld is divided into irregular
cells about REGIONS["size"] blocks across, and REGIONS["share"] of them are Jugcraft regions, each in one of LAYOUTS
layouts: vanilla's climate table with that layout's RULES applied (a vanilla biome, in the given climate bands, becomes
a Jugcraft biome). The rest stay vanilla. Every Jugcraft biome is also listed in vanilla's table at a climate no place
has, so world generation knows it. RULES are written to /jugcraft/region_rules.json, which Java reads.

Biomes are built from the vanilla biome they replace (biome_bases.BASES): its ores, caves, lakes and springs, with
their own trees and plants (trees.SHAPES). Batch 1 is the seasonal forests.
"""

from biome_bases import BASE_TAGS

MOD = "jugcraft"
FEATURE = "biomes"

REGIONS = {"size": 1024, "share": 0.5}

# Vanilla's climate band edges (OverworldBiomeBuilder): an entry's band is where the middle of its range falls,
# 0 = coldest (driest) to 4 = hottest (wettest).
TEMPERATURE_BANDS = [-0.45, -0.15, 0.2, 0.55]
HUMIDITY_BANDS = [-0.35, -0.1, 0.1, 0.3]

# Jugcraft regions come in LAYOUTS layouts, equally often, each with its own replacements, so that every vanilla climate
# slot can hold several Jugcraft biomes. Their characters: 0 woodland, 1 meadow, 2 wetland, 3 wild.
LAYOUTS = 4
LAYOUT_NAMES = ["woodland", "meadow", "wetland", "wild"]


def rule(layouts, replaces, temperature, humidity, biome, weirdness=0):
    """In each of `layouts`, entries of vanilla biome `replaces` whose temperature and humidity bands lie in the given
    [min, max] ranges, and whose weirdness half is `weirdness` (-1 negative, 1 positive, 0 either), become `biome`. The
    first rule that matches an entry decides it; check_mod_data refuses rules that overlap."""
    return {"layouts": list(layouts), "replaces": f"minecraft:{replaces}", "temperature": list(temperature),
            "humidity": list(humidity), "weirdness": weirdness, "biome": f"{MOD}:{biome}"}


RULES = [
    # Batch 1, the seasonal forests: everywhere but the meadow layout, where batch 2 takes cool forests and plains.
    rule([0, 1, 2, 3], "taiga", (1, 1), (0, 4), "coniferous_forest"),
    rule([0, 1, 2, 3], "snowy_taiga", (0, 0), (0, 4), "snowy_coniferous_forest"),
    rule([0, 2, 3], "forest", (1, 1), (0, 4), "maple_woods"),
    rule([0, 1, 2, 3], "forest", (2, 2), (0, 4), "seasonal_forest"),
    rule([0, 1, 2, 3], "birch_forest", (0, 4), (0, 4), "aspen_glade"),
    rule([0, 1, 2, 3], "old_growth_birch_forest", (0, 4), (0, 4), "aspen_glade"),
    rule([0, 2, 3], "plains", (1, 1), (0, 0), "dead_forest"),
    rule([0, 2, 3], "plains", (1, 1), (1, 1), "tundra"),
    rule([0, 1, 2, 3], "snowy_plains", (0, 0), (2, 2), "snowy_forest"),
    rule([0, 1, 2, 3], "snowy_plains", (0, 0), (1, 1), "muskeg"),
    # Batch 2, fields and meadows: the meadow layout.
    rule([1], "forest", (1, 1), (0, 4), "field"),
    rule([1], "plains", (1, 1), (0, 0), "steppe"),
    rule([1], "plains", (1, 1), (1, 1), "grassland"),
    rule([1], "plains", (2, 2), (0, 4), "prairie", weirdness=-1),
    rule([1], "plains", (2, 2), (0, 4), "shrubland", weirdness=1),
    rule([1], "sunflower_plains", (0, 4), (0, 4), "lavender_field"),
    rule([1], "flower_forest", (0, 4), (0, 4), "flower_meadow"),
    rule([1], "savanna", (3, 3), (1, 1), "heathland"),
    rule([1], "sparse_jungle", (3, 3), (0, 4), "lush_grassland"),
    rule([1], "plains", (3, 3), (0, 4), "lush_grassland"),
]


def rules_file():
    """The generated rules Java reads (/jugcraft/region_rules.json on the classpath)."""
    return RULES


# Features the biomes add, in one fixed order, after their base's own (keeps the Overworld's feature order acyclic).
EXTRAS = {
    "tundra_rocks": {"feature": "minecraft:forest_rock", "step": 2, "placement": [
        {"type": "minecraft:rarity_filter", "chance": 2}, {"type": "minecraft:in_square"},
        {"type": "minecraft:heightmap", "heightmap": "MOTION_BLOCKING"}, {"type": "minecraft:biome"}]},
    "patch_dead_bush": {"feature": "minecraft:dead_bush", "step": 9, "count": 4},
    "patch_pumpkin_dense": {"feature": "minecraft:pumpkin", "step": 9, "rarity": 24, "count": 96, "on": "minecraft:grass_block"},
    # Batch 2. "block": a vanilla or Jugcraft block placed alone (a feature of its own); "patches": patches per chunk.
    "field_sunflowers": {"feature": "minecraft:sunflower", "step": 9, "rarity": 3, "count": 48},
    "field_flowers": {"feature": "minecraft:flower_plain", "step": 9, "count": 24},
    "meadow_flowers": {"feature": "minecraft:flower_meadow", "step": 9, "patches": 2, "count": 96},
    "meadow_wildflowers": {"feature": "minecraft:wildflower", "step": 9, "count": 32},
    "lavender": {"feature": "jugcraft:lavender", "step": 9, "patches": 3, "count": 96},
    "tall_lavender": {"feature": "jugcraft:tall_lavender", "step": 9, "patches": 2, "count": 48},
    "clover": {"feature": "jugcraft:clover", "step": 9, "patches": 2, "count": 48},
    "goldenrod": {"feature": "jugcraft:goldenrod", "step": 9, "rarity": 2, "count": 48},
    "heather": {"feature": "jugcraft:heather", "step": 9, "patches": 2, "count": 64},
    "orange_cosmos": {"feature": "jugcraft:orange_cosmos", "step": 9, "rarity": 2, "count": 32},
    "oxeye_daisies": {"block": "minecraft:oxeye_daisy", "step": 9, "rarity": 2, "count": 32},
    "alliums": {"block": "minecraft:allium", "step": 9, "rarity": 3, "count": 24},
    "tall_grass_dense": {"feature": "minecraft:tall_grass", "step": 9, "count": 48},
    "dry_grass": {"feature": "minecraft:dry_grass", "step": 9, "patches": 2, "count": 32},
    "bushes_dense": {"feature": "minecraft:bush", "step": 9, "patches": 2, "count": 24},
}

# Each biome: its base, climate values, trees (count: [usual, sometimes]; default and weighted picks of placed
# features; None: no trees), base features it drops or swaps, extras it adds, mob changes, tags, and seasons.
# "seasons": in #jugcraft:has_seasons and #jugcraft:has_winter_snow (four-season biomes; not the frozen ones);
# "winter_snow": False keeps a seasonal biome out of #jugcraft:has_winter_snow (mild winters).
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
    # ---------------------------------------------------------------- batch 2: fields and meadows
    # Cool, flat land with teal grass, scattered small spruces and clumps of oak bush, and many flowers.
    "field": {
        "display": "Field", "base": "forest", "temperature": 0.35, "downfall": 0.6, "seasons": True,
        "effects": {"grass_color": "#7fb38c", "foliage_color": "#6fa880"},
        "trees": {"count": [2, 3], "default": "jugcraft:oak_bush_checked", "picks": [
            ["minecraft:spruce_checked", 0.3], ["minecraft:oak_bees_002", 0.05]]},
        "extras": ["field_sunflowers", "field_flowers"],
        "creatures": [["minecraft:sheep", 12, 4, 4], ["minecraft:pig", 10, 4, 4], ["minecraft:chicken", 10, 4, 4],
                      ["minecraft:cow", 8, 4, 4], ["minecraft:rabbit", 4, 2, 3], ["minecraft:fox", 2, 2, 4]],
        "untags": ["minecraft:is_forest"],
        "tags": ["minecraft:has_structure/village_taiga", "minecraft:has_structure/mineshaft", "c:is_plains"],
    },
    # Treeless and thick with vanilla flowers on almost every block.
    "flower_meadow": {
        "display": "Flower Meadow", "base": "flower_forest", "temperature": 0.5, "downfall": 0.7, "seasons": True,
        "effects": {"grass_color": "#83c25f"},
        "trees": None,
        "extras": ["meadow_flowers", "meadow_wildflowers"],
        "untags": ["minecraft:is_forest"],
        "tags": ["minecraft:has_structure/mineshaft", "c:is_plains", "c:is_flower_forest"],
    },
    # Treeless, blue-green grass carpeted with clover.
    "grassland": {
        "display": "Grassland", "base": "plains", "temperature": 0.6, "downfall": 0.6, "seasons": True,
        "effects": {"grass_color": "#6fbd84", "foliage_color": "#62ad73"},
        "trees": None,
        "drop": ["minecraft:patch_bush"],
        "extras": ["clover", "tall_grass_dense"],
        "creatures": [["minecraft:sheep", 12, 4, 4], ["minecraft:pig", 10, 4, 4], ["minecraft:chicken", 10, 4, 4],
                      ["minecraft:cow", 8, 4, 4]],
        "tags": ["c:is_plains"],
    },
    # Warm, dry heath: brown-tinged grass, heather and wildflowers, pines and oak bushes; wild horses.
    "heathland": {
        "display": "Heathland", "base": "plains", "temperature": 0.9, "downfall": 0.45, "seasons": True, "winter_snow": False,
        "effects": {"grass_color": "#a6a85c", "foliage_color": "#8f9a4c"},
        "trees": {"count": [2, 3], "default": "jugcraft:oak_bush_checked", "picks": [["minecraft:pine_checked", 0.3]]},
        "drop": ["minecraft:flower_plains"],
        "extras": ["heather", "meadow_wildflowers", "dry_grass"],
        "creatures": [["minecraft:horse", 8, 2, 6], ["minecraft:donkey", 2, 1, 3], ["minecraft:sheep", 10, 4, 4],
                      ["minecraft:rabbit", 4, 2, 3], ["minecraft:chicken", 6, 4, 4]],
        "tags": ["c:is_plains"],
    },
    # Rolling fields smothered in lavender, with blossoming jacarandas and oaks; bees.
    "lavender_field": {
        "display": "Lavender Field", "base": "sunflower_plains", "temperature": 0.7, "downfall": 0.6, "seasons": True,
        "effects": {"grass_color": "#8cbf64"},
        "trees": {"count": [1, 2], "default": "jugcraft:jacaranda_checked", "picks": [["minecraft:oak_bees_002", 0.4]]},
        "drop": ["minecraft:patch_sunflower", "minecraft:flower_plains", "minecraft:patch_bush"],
        "extras": ["lavender", "tall_lavender"],
        "tags": ["c:is_plains", "c:is_flower_forest"],
    },
    # Warm, wet grassland: small jungle trees and oak bushes among orange cosmos and oxeye daisies; villages.
    "lush_grassland": {
        "display": "Lush Grassland", "base": "sparse_jungle", "temperature": 0.85, "downfall": 0.85, "seasons": True,
        "winter_snow": False,
        "effects": {"grass_color": "#5fbd4a", "foliage_color": "#4fb03a"},
        "trees": {"count": [2, 3], "default": "minecraft:jungle_bush", "picks": [
            ["jugcraft:oak_bush_checked", 0.3], ["minecraft:jungle_tree", 0.1]]},
        "drop": ["minecraft:patch_melon_sparse"],
        "extras": ["orange_cosmos", "oxeye_daisies", "tall_grass_dense"],
        "creatures": [["minecraft:sheep", 12, 4, 4], ["minecraft:pig", 10, 4, 4], ["minecraft:chicken", 10, 4, 4],
                      ["minecraft:cow", 8, 4, 4], ["minecraft:parrot", 4, 1, 2]],
        "untags": ["minecraft:is_jungle"],
        "tags": ["minecraft:has_structure/village_plains", "c:is_plains"],
    },
    # Flat, golden prairie: tall grass and goldenrod, brushy oaks; villages and water holes.
    "prairie": {
        "display": "Prairie", "base": "plains", "temperature": 0.8, "downfall": 0.4, "seasons": True,
        "effects": {"grass_color": "#a3bd5c", "foliage_color": "#8fae4c"},
        "trees": {"count": [1, 2], "default": "jugcraft:oak_bush_checked", "picks": [["minecraft:fancy_oak_checked", 0.15]]},
        "extras": ["goldenrod", "tall_grass_dense"],
        "tags": ["c:is_plains"],
    },
    # Low hills of grass densely set with oak bushes and bushes, and alliums; no trees; wild horses.
    "shrubland": {
        "display": "Shrubland", "base": "plains", "temperature": 0.7, "downfall": 0.3, "seasons": True,
        "effects": {"grass_color": "#93ab5a", "foliage_color": "#7f9a49"},
        "trees": {"count": [4, 6], "default": "jugcraft:oak_bush_checked", "picks": []},
        "extras": ["bushes_dense", "alliums"],
        "creatures": [["minecraft:horse", 8, 2, 6], ["minecraft:donkey", 1, 1, 3], ["minecraft:sheep", 12, 4, 4],
                      ["minecraft:pig", 8, 4, 4], ["minecraft:chicken", 10, 4, 4], ["minecraft:cow", 8, 4, 4]],
        "tags": ["c:is_plains"],
    },
    # Cool, dry, tan grassland with almost nothing growing: dry grass and dead bushes; horses, donkeys and llamas.
    "steppe": {
        "display": "Steppe", "base": "plains", "temperature": 0.45, "downfall": 0.2, "seasons": True,
        "effects": {"grass_color": "#b8ab6c", "foliage_color": "#a89a5c"},
        "trees": None,
        "drop": ["minecraft:patch_bush", "minecraft:flower_plains", "minecraft:patch_tall_grass_2"],
        "swap": {"minecraft:patch_grass_plain": "minecraft:patch_grass_savanna"},
        "extras": ["dry_grass", "patch_dead_bush"],
        "creatures": [["minecraft:horse", 6, 2, 6], ["minecraft:donkey", 2, 1, 3], ["minecraft:llama", 4, 2, 4],
                      ["minecraft:rabbit", 4, 2, 3]],
        "tags": ["c:is_plains", "c:is_dry"],
    },
}

def biome_tags(name):
    info = BIOMES[name]
    tags = [t for t in BASE_TAGS[info["base"]] if t not in info.get("untags", [])]
    return tags + [t for t in info.get("tags", []) if t not in tags]

"""Single source of truth for the Agriculture branch: crops, seeds, foods, farm tools.

generate_material_data.py writes the JSON resources from these tables and
check_mod_data.py verifies them. Keep in sync with agriculture/Crop.java and
agriculture/JugcraftAgriculture.java; the checker compares the two.
See docs/branches/AGRICULTURE.md for the design.
"""

FEATURE = "agriculture"

# Tall crops grow through ages 0-7. heights[age] is how many blocks tall the plant is at
# that age; it only grows into air. At age 7 it is ripe: right-click (or a sickle) picks
# the produce and sets it back to pick_reset, which has the same height, so the plant
# keeps standing. That keeps cornfields and mazes intact through every harvest.
# textures[age] lists one texture per block, bottom first.
TALL_CROPS = {
    "corn": {
        "block": "corn_crop", "display": "Corn Crop", "seed": "corn_kernels",
        "heights": [1, 1, 1, 2, 2, 3, 3, 3],
        "pick": {"item": "corn", "min": 2, "max": 3}, "pick_reset": 5,
        # Grows this many times slower than wheat: a 3-block plant is a bigger investment.
        "growth_time": 1.5,
        "textures": [
            ["corn_sprout"], ["corn_seedling"], ["corn_young"],
            ["corn_stalk", "corn_young_top"],
            ["corn_stalk", "corn_leafy_top"],
            ["corn_stalk", "corn_stalk_middle", "corn_top"],
            ["corn_stalk", "corn_middle_silk", "corn_tassel"],
            ["corn_stalk_ripe", "corn_middle_ears", "corn_tassel_ripe"],
        ],
    },
    "sunflower": {
        "block": "sunflower_crop", "display": "Sunflower Crop", "seed": "sunflower_seeds",
        "heights": [1, 1, 1, 2, 2, 2, 2, 2],
        "pick": {"item": "sunflower_seeds", "min": 2, "max": 4}, "pick_reset": 5,
        "growth_time": 1.25,
        "textures": [
            ["sunflower_sprout"], ["sunflower_seedling"], ["sunflower_young"],
            ["sunflower_stem", "sunflower_young_top"],
            ["sunflower_stem", "sunflower_leafy_top"],
            ["sunflower_stem", "sunflower_bud"],
            ["sunflower_stem", "sunflower_opening"],
            ["sunflower_stem", "sunflower_bloom"],
        ],
    },
}
TALL_SECTIONS = 3  # The shared "section" block state property runs 0..2.

# One-block crops follow vanilla crop rules (farmland, light, bone meal) plus the legume bonus.
# stages[age] -> texture index <block>_stage<n>. loot: "root" = the planted item drops, more when
# ripe (vanilla carrots); "grain" = produce when ripe else seed, plus bonus seeds (vanilla wheat).
CROPS = {
    "beans": {"block": "bean_crop", "display": "Bean Crop", "seed": "beans", "produce": "beans",
              "legume": True, "stages": [0, 0, 1, 1, 2, 2, 2, 3], "loot": "root"},
    "sweet_potato": {"block": "sweet_potato_crop", "display": "Sweet Potato Crop", "seed": "sweet_potato",
                     "produce": "sweet_potato", "legume": False, "stages": [0, 0, 1, 1, 2, 2, 2, 3],
                     "loot": "root"},
    "flax": {"block": "flax_crop", "display": "Flax Crop", "seed": "flax_seeds", "produce": "flax",
             "legume": False, "stages": [0, 0, 1, 1, 2, 2, 2, 3], "loot": "grain"},
}

# Wild plants: the natural entry point. Found in patches on grass in fitting biomes (new chunks
# only); breaking one gives 1-2 of its crop's planting item. Shears take the plant itself.
# texture: which crop texture the plant reuses. biomes: Fabric ConventionalBiomeTags fields.
WILD_CROPS = {
    "wild_corn": {"display": "Wild Corn", "crop": "corn", "texture": "corn_wild",
                  "biomes": ["IS_PLAINS", "IS_SAVANNA"]},
    "wild_sunflower": {"display": "Wild Sunflower", "crop": "sunflower", "texture": "sunflower_wild",
                       "biomes": ["IS_PLAINS"]},
    "wild_beans": {"display": "Wild Beans", "crop": "beans", "texture": "bean_stage3",
                   "biomes": ["IS_FOREST", "IS_JUNGLE"]},
    "wild_sweet_potato": {"display": "Wild Sweet Potato", "crop": "sweet_potato", "texture": "sweet_potato_stage3",
                          "biomes": ["IS_SAVANNA", "IS_JUNGLE"]},
    "wild_flax": {"display": "Wild Flax", "crop": "flax", "texture": "flax_stage2",
                  "biomes": ["IS_PLAINS", "IS_FLORAL"]},
}
# One patch in about 1 of `rarity` chunks of a matching biome; `tries` placement attempts per patch.
WILD_PATCH = {"rarity": 24, "tries": 24, "spread_xz": 5, "spread_y": 2}
WILD_COMPOST = "medium"
# Breaking short grass has this chance per Jugcraft seed item to drop it (vanilla wheat seeds: 0.125),
# so every crop is reachable in any biome and in worlds generated before this feature.
GRASS_SEEDS = ["corn_kernels", "sunflower_seeds", "beans", "sweet_potato", "flax_seeds"]
GRASS_SEED_CHANCE = 0.02

# Plain and food items. food: [nutrition, saturation modifier] (vanilla carrot is [3, 0.6],
# baked potato [5, 0.6], rabbit stew [10, 0.6]). plants: the crop block a seed item places.
# compost: composter chance tier, as vanilla's (low 30 %, medium 65 %, medium_high 85 %).
# tags: extra item tags.
ITEMS = {
    "corn": {"display": "Corn", "food": [3, 0.6], "compost": "medium", "tags": ["c:crops/corn", "c:foods/vegetable", "minecraft:pig_food"]},
    "corn_kernels": {"display": "Corn Kernels", "plants": "corn_crop", "compost": "low",
                     "tags": ["c:seeds/corn", "minecraft:chicken_food", "minecraft:parrot_food"]},
    "roasted_corn": {"display": "Roasted Corn", "food": [5, 0.6], "compost": "medium_high", "tags": ["c:foods"]},
    "popcorn": {"display": "Popcorn", "food": [2, 0.3], "compost": "medium_high", "tags": ["c:foods"]},
    "sunflower_seeds": {"display": "Sunflower Seeds", "plants": "sunflower_crop", "compost": "low",
                        "tags": ["c:seeds/sunflower", "c:crops/sunflower", "minecraft:chicken_food",
                                 "minecraft:parrot_food"]},
    "roasted_sunflower_seeds": {"display": "Roasted Sunflower Seeds", "food": [2, 0.3], "compost": "medium_high", "tags": ["c:foods"]},
    "beans": {"display": "Beans", "plants": "bean_crop", "compost": "medium", "tags": ["c:seeds/beans", "c:crops/beans"]},
    "sweet_potato": {"display": "Sweet Potato", "plants": "sweet_potato_crop", "food": [2, 0.3], "compost": "medium",
                     "tags": ["c:crops/sweet_potato", "c:foods/vegetable", "minecraft:pig_food"]},
    "baked_sweet_potato": {"display": "Baked Sweet Potato", "food": [5, 0.6], "compost": "medium_high", "tags": ["c:foods"]},
    "flax": {"display": "Flax", "compost": "medium", "tags": ["c:crops/flax"]},
    "flax_seeds": {"display": "Flax Seeds", "plants": "flax_crop", "compost": "low",
                   "tags": ["c:seeds/flax", "minecraft:chicken_food", "minecraft:parrot_food"]},
    "three_sisters_stew": {"display": "Three Sisters Stew", "food": [10, 0.6], "stew": True,
                           "tags": ["c:foods/soup"]},
}

# Sickles: right-click harvests every ripe crop in a square of side 2 * radius + 1. Low crops are
# replanted from their own drops; tall crops are picked and stay standing. 1 durability per use.
SICKLES = {
    "flint_sickle": {"display": "Flint Sickle", "radius": 1, "durability": 131, "material": "minecraft:flint",
                     "features": []},
    "bronze_sickle": {"display": "Bronze Sickle", "radius": 2, "durability": 350, "material": "#c:ingots/bronze",
                      "features": ["tin"]},
}
SICKLE_PATTERN = [" M ", "  M", "SM "]

# Cooking: every cooked food works in the furnace, smoker and on a campfire (vanilla timings).
COOKING = {
    "roasted_corn": {"input": "corn", "xp": 0.35},
    "popcorn": {"input": "corn_kernels", "xp": 0.1},
    "baked_sweet_potato": {"input": "sweet_potato", "xp": 0.35},
    "roasted_sunflower_seeds": {"input": "sunflower_seeds", "xp": 0.1},
}
COOK_TIMES = {"smelting": 200, "smoking": 100, "campfire_cooking": 600}

# Crafting. result: an ID (jugcraft unless namespaced) and count.
SHAPELESS = [
    {"id": "corn_kernels", "inputs": ["jugcraft:corn"], "result": "corn_kernels", "count": 2},
    {"id": "three_sisters_stew", "inputs": ["minecraft:bowl", "jugcraft:corn", "jugcraft:beans", "minecraft:pumpkin"],
     "result": "three_sisters_stew", "count": 1},
    {"id": "string_from_flax", "inputs": ["jugcraft:flax", "jugcraft:flax"], "result": "minecraft:string", "count": 1},
]

# Growth rules shared with Java (agriculture/CropGrowth.java): non-legume crops next to a
# legume (block tag jugcraft:nitrogen_fixing_crops, 8 neighbours) grow this much faster.
LEGUME_BONUS = 1.5
LEGUME_TAG = "jugcraft:nitrogen_fixing_crops"


def tall_blocks():
    return [info["block"] for info in TALL_CROPS.values()]


def crop_blocks():
    return tall_blocks() + [info["block"] for info in CROPS.values()]


def all_blocks():
    """Every registered agriculture block. Crops have no block item (seeds place them); wild plants do."""
    return crop_blocks() + list(WILD_CROPS)


def all_items():
    return list(ITEMS) + list(SICKLES) + list(WILD_CROPS)


def owns(entry_id):
    return entry_id in all_blocks() or entry_id in all_items()


def textures():
    """Every crop block texture the models reference."""
    out = []
    for info in TALL_CROPS.values():
        for names in info["textures"]:
            out += [n for n in names if n not in out]
    for info in CROPS.values():
        out += [f"{info['block'].removesuffix('_crop')}_stage{n}" for n in sorted(set(info["stages"]))]
    out += [w["texture"] for w in WILD_CROPS.values() if w["texture"] not in out]
    return out

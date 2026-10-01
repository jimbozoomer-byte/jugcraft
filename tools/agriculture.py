"""Single source of truth for the Agriculture branch: crops, seeds, foods, farm tools.

generate_material_data.py writes the JSON resources from these tables and
check_mod_data.py verifies them. Keep in sync with agriculture/TallCrop.java and
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
# Kitchen Garden (slice 2). Tomatoes climb a trellis ("trellis": True): the plant only grows into
# trellis blocks, not air, and every block of it drops its trellis again when broken. Peppers
# are a one-block bush that is picked like the tall crops.
TALL_CROPS["tomato"] = {
    "block": "tomato_crop", "display": "Tomato Plant", "seed": "tomato_seeds",
    "heights": [1, 1, 1, 2, 2, 2, 2, 2],
    "pick": {"item": "tomato", "min": 2, "max": 4}, "pick_reset": 5,
    "growth_time": 1.25, "trellis": True,
    "textures": [
        ["tomato_sprout"], ["tomato_seedling"], ["tomato_young"],
        ["tomato_vine", "tomato_climbing_top"],
        ["tomato_vine", "tomato_leafy_top"],
        ["tomato_vine_flowers", "tomato_flowering_top"],
        ["tomato_vine_green", "tomato_green_top"],
        ["tomato_vine_ripe", "tomato_ripe_top"],
    ],
}
TALL_CROPS["pepper"] = {
    "block": "pepper_crop", "display": "Pepper Plant", "seed": "pepper_seeds",
    "heights": [1, 1, 1, 1, 1, 1, 1, 1],
    "pick": {"item": "pepper", "min": 1, "max": 3}, "pick_reset": 5,
    "growth_time": 1.25,
    "textures": [["pepper_stage0"], ["pepper_stage0"], ["pepper_stage1"], ["pepper_stage1"],
                 ["pepper_stage2"], ["pepper_stage3"], ["pepper_stage4"], ["pepper_stage5"]],
}
# Halloween harvest. Ornamental (flint) corn grows like corn but ripens into multicoloured ears for
# decoration. Its kernels come from grass and, now and then, from breaking Wild Corn (WILD_BONUS).
TALL_CROPS["ornamental_corn"] = {
    "block": "ornamental_corn_crop", "display": "Ornamental Corn Crop", "seed": "ornamental_corn_kernels",
    "heights": [1, 1, 1, 2, 2, 3, 3, 3],
    "pick": {"item": "ornamental_corn", "min": 1, "max": 2}, "pick_reset": 5,
    "growth_time": 1.5,
    "textures": [
        ["corn_sprout"], ["corn_seedling"], ["corn_young"],
        ["corn_stalk", "corn_young_top"],
        ["corn_stalk", "corn_leafy_top"],
        ["corn_stalk", "corn_stalk_middle", "corn_top"],
        ["corn_stalk", "corn_middle_silk", "corn_tassel"],
        ["corn_stalk_ripe", "ornamental_corn_middle_ears", "corn_tassel_ripe"],
    ],
}
# Breaking a corn plant three blocks tall (age 5 or more, so also after picking) drops dry stalks for
# Corn Shocks, from its bottom block only.
STALKS = {"item": "corn_stalks", "min": 1, "max": 2, "from_age": 5, "crops": ["corn", "ornamental_corn"]}
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
    # Kitchen Garden.
    "onion": {"block": "onion_crop", "display": "Onion Crop", "seed": "onion", "produce": "onion",
              "legume": False, "stages": [0, 0, 1, 1, 2, 2, 2, 3], "loot": "root"},
    "garlic": {"block": "garlic_crop", "display": "Garlic Crop", "seed": "garlic", "produce": "garlic",
               "legume": False, "stages": [0, 0, 1, 1, 2, 2, 2, 3], "loot": "root"},
    "cabbage": {"block": "cabbage_crop", "display": "Cabbage Crop", "seed": "cabbage_seeds", "produce": "cabbage",
                "legume": False, "stages": [0, 0, 1, 1, 2, 2, 2, 3], "loot": "grain"},
    "oats": {"block": "oat_crop", "display": "Oat Crop", "seed": "oat_seeds", "produce": "oats",
             "legume": False, "stages": [0, 0, 1, 1, 2, 2, 2, 3], "loot": "grain"},
    "barley": {"block": "barley_crop", "display": "Barley Crop", "seed": "barley_seeds", "produce": "barley",
               "legume": False, "stages": [0, 0, 1, 1, 2, 2, 2, 3], "loot": "grain"},
    # Festival crops.
    "turnip": {"block": "turnip_crop", "display": "Turnip Crop", "seed": "turnip", "produce": "turnip",
               "legume": False, "stages": [0, 0, 1, 1, 2, 2, 2, 3], "loot": "root"},
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
    "wild_tomato": {"display": "Wild Tomato", "crop": "tomato", "texture": "tomato_wild",
                    "biomes": ["IS_JUNGLE", "IS_SAVANNA"]},
    "wild_pepper": {"display": "Wild Pepper", "crop": "pepper", "texture": "pepper_stage5",
                    "biomes": ["IS_SAVANNA", "IS_BADLANDS"]},
    "wild_onion": {"display": "Wild Onion", "crop": "onion", "texture": "onion_stage3",
                   "biomes": ["IS_PLAINS", "IS_HILL"]},
    "wild_garlic": {"display": "Wild Garlic", "crop": "garlic", "texture": "garlic_stage3",
                    "biomes": ["IS_FOREST", "IS_TAIGA"]},
    "wild_cabbage": {"display": "Wild Cabbage", "crop": "cabbage", "texture": "cabbage_stage2",
                     "biomes": ["IS_WINDSWEPT", "IS_HILL"]},
    "wild_oats": {"display": "Wild Oats", "crop": "oats", "texture": "oat_stage2",
                  "biomes": ["IS_PLAINS", "IS_TAIGA"]},
    "wild_barley": {"display": "Wild Barley", "crop": "barley", "texture": "barley_stage2",
                    "biomes": ["IS_SAVANNA", "IS_HILL"]},
    "wild_turnip": {"display": "Wild Turnip", "crop": "turnip", "texture": "turnip_stage3",
                    "biomes": ["IS_TAIGA", "IS_BIRCH_FOREST"]},
}
# Extra drop when a wild plant is broken without shears: Wild Corn sometimes gives ornamental corn kernels.
WILD_BONUS = {"wild_corn": {"item": "ornamental_corn_kernels", "chance": 0.1}}
# One patch in about 1 of `rarity` chunks of a matching biome; `tries` placement attempts per patch.
WILD_PATCH = {"rarity": 24, "tries": 24, "spread_xz": 5, "spread_y": 2}
WILD_COMPOST = "medium"
# Breaking short grass has this chance to drop one Jugcraft seed, chosen evenly from GRASS_SEEDS: as
# often as vanilla wheat seeds (0.125), however many crops there are. Every crop is reachable in any
# biome and in worlds generated before this feature.
GRASS_SEEDS = ["corn_kernels", "sunflower_seeds", "beans", "sweet_potato", "flax_seeds",
               "tomato_seeds", "pepper_seeds", "onion", "garlic", "cabbage_seeds", "oat_seeds", "barley_seeds",
               "butternut_squash_seeds", "acorn_squash_seeds", "warty_gourd_seeds", "turnip", "cranberries", "chestnut",
               "giant_pumpkin_seeds", "white_pumpkin_seeds", "jarrahdale_pumpkin_seeds", "cinderella_pumpkin_seeds", "bottle_gourd_seeds",
               "ornamental_corn_kernels"]
GRASS_SEED_CHANCE = 0.125

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
    "roasted_pumpkin_seeds": {"display": "Roasted Pumpkin Seeds", "food": [2, 0.3], "compost": "medium_high", "tags": ["c:foods"]},
    "beans": {"display": "Beans", "plants": "bean_crop", "compost": "medium", "tags": ["c:seeds/beans", "c:crops/beans"]},
    "sweet_potato": {"display": "Sweet Potato", "plants": "sweet_potato_crop", "food": [2, 0.3], "compost": "medium",
                     "tags": ["c:crops/sweet_potato", "c:foods/vegetable", "minecraft:pig_food"]},
    "baked_sweet_potato": {"display": "Baked Sweet Potato", "food": [5, 0.6], "compost": "medium_high", "tags": ["c:foods"]},
    "flax": {"display": "Flax", "compost": "medium", "tags": ["c:crops/flax"]},
    "flax_seeds": {"display": "Flax Seeds", "plants": "flax_crop", "compost": "low",
                   "tags": ["c:seeds/flax", "minecraft:chicken_food", "minecraft:parrot_food"]},
    "three_sisters_stew": {"display": "Three Sisters Stew", "food": [10, 0.6], "stew": True,
                           "tags": ["c:foods/soup"]},
    # Kitchen Garden. "trellis_seed": planted on a trellis, not on bare farmland.
    "tomato": {"display": "Tomato", "food": [3, 0.3], "compost": "medium", "tags": ["c:crops/tomato", "c:foods/vegetable"]},
    "tomato_seeds": {"display": "Tomato Seeds", "plants": "tomato_crop", "trellis_seed": True, "compost": "low",
                     "tags": ["c:seeds/tomato", "minecraft:chicken_food", "minecraft:parrot_food"]},
    "pepper": {"display": "Pepper", "food": [2, 0.3], "compost": "medium", "tags": ["c:crops/pepper", "c:foods/vegetable"]},
    "pepper_seeds": {"display": "Pepper Seeds", "plants": "pepper_crop", "compost": "low",
                     "tags": ["c:seeds/pepper", "minecraft:chicken_food", "minecraft:parrot_food"]},
    "onion": {"display": "Onion", "plants": "onion_crop", "compost": "medium", "tags": ["c:crops/onion"]},
    "garlic": {"display": "Garlic", "plants": "garlic_crop", "compost": "medium", "tags": ["c:crops/garlic"]},
    "cabbage": {"display": "Cabbage", "food": [3, 0.6], "compost": "medium",
                "tags": ["c:crops/cabbage", "c:foods/vegetable", "minecraft:rabbit_food", "minecraft:pig_food"]},
    "cabbage_seeds": {"display": "Cabbage Seeds", "plants": "cabbage_crop", "compost": "low",
                      "tags": ["c:seeds/cabbage", "minecraft:chicken_food", "minecraft:parrot_food"]},
    "oats": {"display": "Oats", "compost": "medium",
             "tags": ["c:crops/oat", "minecraft:horse_food", "minecraft:cow_food", "minecraft:sheep_food", "minecraft:goat_food"]},
    "oat_seeds": {"display": "Oat Seeds", "plants": "oat_crop", "compost": "low",
                  "tags": ["c:seeds/oat", "minecraft:chicken_food", "minecraft:parrot_food"]},
    "barley": {"display": "Barley", "compost": "medium",
               "tags": ["c:crops/barley", "minecraft:cow_food", "minecraft:sheep_food", "minecraft:goat_food"]},
    "barley_seeds": {"display": "Barley Seeds", "plants": "barley_crop", "compost": "low",
                     "tags": ["c:seeds/barley", "minecraft:chicken_food", "minecraft:parrot_food"]},
    "barley_bread": {"display": "Barley Bread", "food": [5, 0.6], "compost": "medium_high", "tags": ["c:foods/bread"]},
    "sauerkraut": {"display": "Sauerkraut", "food": [4, 0.6], "compost": "medium_high", "tags": ["c:foods"]},
    "garden_salad": {"display": "Garden Salad", "food": [7, 0.6], "stew": True, "tags": ["c:foods"]},
    "tomato_soup": {"display": "Tomato Soup", "food": [8, 0.6], "stew": True, "tags": ["c:foods/soup"]},
    "onion_soup": {"display": "Onion Soup", "food": [8, 0.6], "stew": True, "tags": ["c:foods/soup"]},
    "vegetable_soup": {"display": "Vegetable Soup", "food": [10, 0.6], "stew": True, "tags": ["c:foods/soup"]},
    "mushroom_barley_soup": {"display": "Mushroom Barley Soup", "food": [8, 0.6], "stew": True, "tags": ["c:foods/soup"]},
    "oat_porridge": {"display": "Oat Porridge", "food": [6, 0.6], "stew": True, "tags": ["c:foods/soup"]},
    "chili": {"display": "Chili", "food": [10, 0.8], "stew": True, "tags": ["c:foods/soup"]},
    "cabbage_rolls": {"display": "Cabbage Rolls", "food": [6, 0.8], "tags": ["c:foods"]},
    # Festival crops. Gourd seeds plant stems; a chestnut plants a chestnut tree sapling; cranberries are
    # planted in shallow water ("bog_seed").
    "butternut_squash_seeds": {"display": "Butternut Squash Seeds", "plants": "butternut_squash_stem", "compost": "low",
                               "tags": ["c:seeds/butternut_squash", "minecraft:chicken_food", "minecraft:parrot_food"]},
    "acorn_squash_seeds": {"display": "Acorn Squash Seeds", "plants": "acorn_squash_stem", "compost": "low",
                           "tags": ["c:seeds/acorn_squash", "minecraft:chicken_food", "minecraft:parrot_food"]},
    "warty_gourd_seeds": {"display": "Warty Gourd Seeds", "plants": "warty_gourd_stem", "compost": "low",
                          "tags": ["c:seeds/warty_gourd", "minecraft:chicken_food", "minecraft:parrot_food"]},
    "turnip": {"display": "Turnip", "plants": "turnip_crop", "food": [3, 0.6], "compost": "medium",
               "tags": ["c:crops/turnip", "c:foods/vegetable", "minecraft:pig_food", "minecraft:rabbit_food"]},
    "cranberries": {"display": "Cranberries", "plants": "cranberry_bush", "bog_seed": True, "food": [2, 0.1], "compost": "low",
                    "tags": ["c:crops/cranberry", "c:foods/berry", "minecraft:fox_food"]},
    "chestnut": {"display": "Chestnut", "plants": "chestnut_sapling", "compost": "low",
                 "tags": ["c:crops/chestnut", "minecraft:pig_food"]},
    "roasted_chestnuts": {"display": "Roasted Chestnuts", "food": [4, 0.6], "compost": "medium_high", "tags": ["c:foods"]},
    "baked_acorn_squash": {"display": "Baked Acorn Squash", "food": [6, 0.6], "compost": "medium_high", "tags": ["c:foods"]},
    "squash_pie": {"display": "Squash Pie", "food": [8, 0.3], "compost": "medium_high", "tags": ["c:foods"]},
    "candy_corn": {"display": "Candy Corn", "food": [2, 0.1], "compost": "medium_high", "tags": ["c:foods/candy"]},
    "butternut_squash_soup": {"display": "Butternut Squash Soup", "food": [8, 0.6], "stew": True, "tags": ["c:foods/soup"]},
    "harvest_stew": {"display": "Harvest Stew", "food": [10, 0.6], "stew": True, "tags": ["c:foods/soup"]},
    "cranberry_sauce": {"display": "Cranberry Sauce", "food": [5, 0.6], "stew": True, "tags": ["c:foods"]},
    # Halloween harvest. "treat": eaten off a stick, which is left in the hand.
    "giant_pumpkin_seeds": {"display": "Giant Pumpkin Seeds", "plants": "giant_pumpkin_vine", "compost": "low",
                            "tags": ["c:seeds/giant_pumpkin", "minecraft:chicken_food", "minecraft:parrot_food"]},
    "pumpkin_guts": {"display": "Pumpkin Guts", "compost": "medium", "tags": []},
    "pumpkin_soup": {"display": "Pumpkin Soup", "food": [8, 0.6], "stew": True, "tags": ["c:foods/soup"]},
    "white_pumpkin_seeds": {"display": "White Pumpkin Seeds", "plants": "white_pumpkin_stem", "compost": "low",
                            "tags": ["c:seeds/white_pumpkin", "minecraft:chicken_food", "minecraft:parrot_food"]},
    "jarrahdale_pumpkin_seeds": {"display": "Jarrahdale Pumpkin Seeds", "plants": "jarrahdale_pumpkin_stem", "compost": "low",
                                 "tags": ["c:seeds/jarrahdale_pumpkin", "minecraft:chicken_food", "minecraft:parrot_food"]},
    "cinderella_pumpkin_seeds": {"display": "Cinderella Pumpkin Seeds", "plants": "cinderella_pumpkin_stem", "compost": "low",
                                 "tags": ["c:seeds/cinderella_pumpkin", "minecraft:chicken_food", "minecraft:parrot_food"]},
    "bottle_gourd_seeds": {"display": "Bottle Gourd Seeds", "plants": "bottle_gourd_stem", "compost": "low",
                           "tags": ["c:seeds/bottle_gourd", "minecraft:chicken_food", "minecraft:parrot_food"]},
    "dried_bottle_gourd": {"display": "Dried Bottle Gourd", "compost": "medium", "tags": []},
    "ornamental_corn": {"display": "Ornamental Corn", "compost": "medium", "tags": ["c:crops/ornamental_corn"]},
    "ornamental_corn_kernels": {"display": "Ornamental Corn Kernels", "plants": "ornamental_corn_crop", "compost": "low",
                                "tags": ["c:seeds/ornamental_corn", "minecraft:chicken_food", "minecraft:parrot_food"]},
    "corn_stalks": {"display": "Corn Stalks", "compost": "medium", "tags": []},
    "caramel": {"display": "Caramel", "food": [2, 0.1], "compost": "medium_high", "tags": ["c:foods/candy"]},
    "caramel_apple": {"display": "Caramel Apple", "food": [6, 0.6], "treat": True, "tags": ["c:foods/candy"]},
    "popcorn_ball": {"display": "Popcorn Ball", "food": [5, 0.6], "compost": "medium_high", "tags": ["c:foods/candy"]},
}

# ---------------------------------------------------------------- Festival crops (slice 3)

# Gourds grow from stems like vanilla pumpkins. A stem on farmland grows through ages 0-7, then puts its
# gourd on a free neighbouring block that supports vegetation (vanilla tag minecraft:supports_stem_fruit)
# and bends towards it; breaking the gourd lets the stem grow another. Stems grow by CropGrowth, so
# squash next to beans grows 1.5x as fast (the Three Sisters with corn). Gourds are blocks: decorations
# for fall displays, and food. growth_time: times wheat's time per stage.
GOURDS = {
    "butternut_squash": {"display": "Butternut Squash", "seed": "butternut_squash_seeds", "growth_time": 1.0,
                         "compost": "medium", "tags": ["c:crops/squash", "c:crops/butternut_squash", "c:foods/vegetable"]},
    "acorn_squash": {"display": "Acorn Squash", "seed": "acorn_squash_seeds", "growth_time": 1.0,
                     "compost": "medium", "tags": ["c:crops/squash", "c:crops/acorn_squash", "c:foods/vegetable"]},
    "warty_gourd": {"display": "Warty Gourd", "seed": "warty_gourd_seeds", "growth_time": 1.0,
                    "compost": "medium", "tags": ["c:crops/gourd", "c:crops/warty_gourd"]},
    # Halloween harvest: three heirloom pumpkins, whole blocks like vanilla's ("cube"), that the Carving Knife
    # carves like a pumpkin; and the bottle gourd, dried into canteens and birdhouses.
    "white_pumpkin": {"display": "White Pumpkin", "seed": "white_pumpkin_seeds", "growth_time": 1.0, "cube": True,
                      "compost": "medium", "tags": ["c:crops/pumpkin", "c:crops/white_pumpkin", "jugcraft:heirloom_pumpkins"]},
    "jarrahdale_pumpkin": {"display": "Jarrahdale Pumpkin", "seed": "jarrahdale_pumpkin_seeds", "growth_time": 1.0, "cube": True,
                           "compost": "medium", "tags": ["c:crops/pumpkin", "c:crops/jarrahdale_pumpkin", "jugcraft:heirloom_pumpkins"]},
    "cinderella_pumpkin": {"display": "Cinderella Pumpkin", "seed": "cinderella_pumpkin_seeds", "growth_time": 1.0, "cube": True,
                           "compost": "medium", "tags": ["c:crops/pumpkin", "c:crops/cinderella_pumpkin", "jugcraft:heirloom_pumpkins"]},
    "bottle_gourd": {"display": "Bottle Gourd", "seed": "bottle_gourd_seeds", "growth_time": 1.0,
                     "compost": "medium", "tags": ["c:crops/gourd", "c:crops/bottle_gourd"]},
}
STEM_TEXTURES = ["gourd_stem", "gourd_stem_attached", "gourd_stalk"]


def stem(gourd):
    return f"{gourd}_stem"


def attached_stem(gourd):
    return f"attached_{gourd}_stem"


# Cranberries are a bog crop. The bush stands in a still water source one block deep, rooted in bog soil
# (block tag jugcraft:bog_soil), and grows only with open air above the water. Ripe bushes are picked
# with a right-click or a sickle and flower again, like vanilla sweet berries. Breaking one leaves its
# water. growth_chance: one stage in this many random ticks with light 9 or more (sweet berries: 5).
CRANBERRY = {"block": "cranberry_bush", "display": "Cranberry Bush", "seed": "cranberries",
             "pick": {"item": "cranberries", "min": 2, "max": 3}, "pick_reset": 1, "growth_chance": 5,
             "stages": ["cranberry_stage0", "cranberry_stage1", "cranberry_stage2", "cranberry_stage3"]}
BOG_SOIL_TAG = "jugcraft:bog_soil"
BOG_SOIL = ["#minecraft:dirt", "#minecraft:mud", "#minecraft:grass_blocks", "#minecraft:sand", "minecraft:clay", "minecraft:gravel"]

# The chestnut tree, the branch's first fruit tree (orchards in slice 4 follow the same rules). A chestnut
# is its seed: it plants a chestnut sapling on dirt or grass, which grows into a tree like vanilla
# saplings (bone meal works). Leaves the tree grew (not ones a player placed) with air under them grow
# spiny burs that ripen, fruit 0 -> 1 -> 2, one step in fruit_chance random ticks (about a Minecraft day
# in all); a right-click picks the ripe ones. The tree is never cut down to harvest it.
CHESTNUT = {"sapling": "chestnut_sapling", "leaves": "chestnut_leaves", "seed": "chestnut", "fruit_chance": 10,
            "pick": {"item": "chestnut", "min": 1, "max": 2},
            # worldgen/feature/chestnut.json: a broad crown on a straight trunk.
            "trunk": {"base_height": 5, "height_rand_a": 2}, "foliage": {"radius": 3, "height": 3}}
# The chestnut wood set: display names. Logs and wood strip with an axe; logs saw into planks (sawmill).
WOOD = {
    "chestnut_log": "Chestnut Log", "chestnut_wood": "Chestnut Wood", "stripped_chestnut_log": "Stripped Chestnut Log",
    "stripped_chestnut_wood": "Stripped Chestnut Wood", "chestnut_planks": "Chestnut Planks", "chestnut_stairs": "Chestnut Stairs",
    "chestnut_slab": "Chestnut Slab", "chestnut_fence": "Chestnut Fence", "chestnut_fence_gate": "Chestnut Fence Gate",
}
WOOD_TAG = "jugcraft:chestnut_logs"
STRIPPED = {"chestnut_log": "stripped_chestnut_log", "chestnut_wood": "stripped_chestnut_wood"}
TREE_BLOCKS = {"chestnut_sapling": "Chestnut Sapling", "chestnut_leaves": "Chestnut Leaves"}
TREE_TEXTURES = ["chestnut_log", "chestnut_log_top", "stripped_chestnut_log", "stripped_chestnut_log_top", "chestnut_planks",
                 "chestnut_leaves", "chestnut_leaves_burs", "chestnut_leaves_ripe", "chestnut_sapling"]

# Decorations. The Turnip Lantern is the original jack-o'-lantern: a carved turnip with a candle inside.
DECOR = {"turnip_lantern": {"display": "Turnip Lantern", "light": 13}}
DECOR_TEXTURES = ["turnip_lantern_side", "turnip_lantern_face", "turnip_lantern_top"]

# Festival crops that also grow wild as themselves, in new chunks: gourds lie on grass like vanilla
# pumpkins, ripe cranberry bushes stand in swamp shallows, and chestnut trees grow in forests.
FOUND_WILD = {
    "butternut_squash": {"biomes": ["IS_PLAINS", "IS_SAVANNA"], "on": "grass"},
    "acorn_squash": {"biomes": ["IS_FOREST", "IS_TAIGA"], "on": "grass"},
    "warty_gourd": {"biomes": ["IS_SWAMP", "IS_SPOOKY"], "on": "grass"},
    "cranberry_bush": {"biomes": ["IS_SWAMP"], "on": "bog"},
    "white_pumpkin": {"biomes": ["IS_BIRCH_FOREST", "IS_SNOWY"], "on": "grass"},
    "jarrahdale_pumpkin": {"biomes": ["IS_SAVANNA", "IS_WINDSWEPT"], "on": "grass"},
    "cinderella_pumpkin": {"biomes": ["IS_PLAINS", "IS_FLORAL"], "on": "grass"},
    "bottle_gourd": {"biomes": ["IS_JUNGLE", "IS_SAVANNA"], "on": "grass"},
}
GOURD_PATCH = {"rarity": 32, "tries": 8, "spread_xz": 4, "spread_y": 2}
CRANBERRY_PATCH = {"rarity": 4, "tries": 32, "spread_xz": 6}
CHESTNUT_TREES = {"biomes": ["IS_FOREST"], "rarity": 3}

# Pumpkin carving: the Carving Knife opens a 16x16 carving screen for one side of a vanilla pumpkin;
# the server checks the finished face and turns the pumpkin into a hand-carved pumpkin (a block entity
# holds all four sides). A torch inside lights it: glow = 4 + holes / 3 + shaved / 12, up to 15.
# Java: agriculture/PumpkinCarving.java, PumpkinCarvings.java, CarvedPumpkinBlock.java.
CARVING = {
    "knife": "carving_knife", "knife_display": "Carving Knife", "durability": 238,
    "block": "hand_carved_pumpkin", "display": "Hand-Carved Pumpkin",
    "size": 16, "faces": 4, "session_ticks": 6000,
    "glow": {"base": 4, "per_holes": 3, "per_shaved": 12, "max": 15},
    "templates": ["classic", "cat", "ghost", "spooky"],
    "knife_pattern": ["I", "S"], "knife_key": {"I": "minecraft:iron_ingot", "S": "minecraft:stick"},
}
CARVING_TEXTURES = ["carving_knife", "hand_carved_pumpkin", "hand_carved_pumpkin_lit"]
# Heirloom pumpkins the knife carves: each becomes its own hand-carved block (same rules, its own skin),
# and its first cut drops 4 of its seeds (loot table carve/<pumpkin>), like vanilla's carve/pumpkin.
CARVED_VARIETIES = {"white_pumpkin": "hand_carved_white_pumpkin", "jarrahdale_pumpkin": "hand_carved_jarrahdale_pumpkin",
                    "cinderella_pumpkin": "hand_carved_cinderella_pumpkin"}
CARVE_SEEDS = 4
# Item tag of the heirloom pumpkins, which bake into vanilla pumpkin pie like a pumpkin does.
HEIRLOOM_TAG = "jugcraft:heirloom_pumpkins"

# ---------------------------------------------------------------- Halloween harvest (slice 4)

# The giant pumpkin. Giant Pumpkin Seeds (scooped out of pumpkins, or dropped by grass) plant a vine on
# farmland that grows like a pumpkin stem but vine_growth_time times slower, then sets one small fruit on a
# free side. While the vine holds it, each random tick of the fruit's master block gives 1 growth point, +1
# if the vine's farmland is moist, +1 if watered (Gourd Canteen) in the last watered_ticks; bone meal gives
# bone_meal_points. Points count up from planting: at grow_to_two it swells to 2x2x2, at grow_to_three to
# 3x3x3, if there is room (air, grass, flowers; never water) on ground fruit can lie on. Full grown it weighs
# start_weight (+0-20) kg and puts on weight_per_point kg a point up to max_weight, until carved. Each side
# carves as one face_size x face_size face; a torch lights it with glow = base + holes / per_holes +
# shaved / per_shaved. Breaking any block breaks it all and drops `drops[size]` pumpkins (and giant seeds
# when full grown).
GIANT_PUMPKIN = {
    "block": "giant_pumpkin", "vine": "giant_pumpkin_vine", "attached_vine": "attached_giant_pumpkin_vine",
    "seed": "giant_pumpkin_seeds", "display": "Giant Pumpkin", "vine_growth_time": 1.5,
    "grow_to_two": 16, "grow_to_three": 48, "bone_meal_points": 4,
    "start_weight": 100, "weight_per_point": 2, "max_weight": 1000, "watered_ticks": 24000,
    "max_size": 3, "face_size": 48, "reach": 3.5,
    "glow": {"base": 4, "per_holes": 27, "per_shaved": 108, "max": 15},
    "drops": {1: 1, 2: 4, 3: 9}, "seeds": [1, 3], "chop_table": "gameplay/chop_giant_pumpkin",
}
# The first cut into any plain pumpkin scoops it out: besides its seeds, 1-2 Pumpkin Guts and sometimes a
# giant pumpkin seed (loot table gameplay/scoop_pumpkin).
SCOOP = {"table": "gameplay/scoop_pumpkin", "guts": [1, 2], "giant_seed_chance": 0.1}

# The Harvest Scale weighs a full-grown giant pumpkin beside it and keeps the heaviest `board` it has
# weighed. The first time a pumpkin places, the player weighing it gets that place's ribbon (a trophy
# only); the scale remembers the last `remembered` pumpkins it gave ribbons to. Comparators read the last
# weight: weight * 15 / max_weight, at least 1.
HARVEST_SCALE = {"block": "harvest_scale", "display": "Harvest Scale", "board": 3, "remembered": 64,
                 "ribbons": {"first_prize_ribbon": "First Prize Ribbon", "second_prize_ribbon": "Second Prize Ribbon",
                             "third_prize_ribbon": "Third Prize Ribbon"}}

# Stencils: a Blank Stencil used on a carved side of a hand-carved pumpkin traces it into a Pumpkin
# Stencil (component jugcraft:stencil); held in the other hand while carving, the screen offers it.
STENCILS = {"blank": "blank_stencil", "blank_display": "Blank Stencil", "stencil": "pumpkin_stencil",
            "stencil_display": "Pumpkin Stencil"}
# The Gourd Canteen holds `capacity` sips of water (component jugcraft:canteen_water).
CANTEEN = {"item": "gourd_canteen", "display": "Gourd Canteen", "capacity": 3}

# Decorations of the Halloween harvest, with their display names. The scarecrow's shirt takes any of the 16
# dye colours (block state "shirt", red when placed).
HALLOWEEN_DECOR = {"scarecrow": "Scarecrow", "corn_shock": "Corn Shock", "ornamental_corn_bundle": "Ornamental Corn Bundle",
                   "gourd_birdhouse": "Gourd Birdhouse"}
SCARECROW_SHIRT = "red"
# Item tag of what a scarecrow wears for a head (drawn on its shoulders the way an armor stand wears a pumpkin).
SCARECROW_HEADS = "jugcraft:scarecrow_heads"
DYE_COLORS = ["white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray", "light_gray", "cyan",
              "purple", "blue", "brown", "green", "red", "black"]

# Mums (garden chrysanthemums): small flowers that go in flower pots, make dye and suspicious stew. They
# grow wild in flower forests, meadows and forests (one patch mixes all four colours).
MUMS = {
    "yellow_mum": {"display": "Yellow Mum", "dye": "yellow", "effect": "SATURATION", "seconds": 0.35},
    "orange_mum": {"display": "Orange Mum", "dye": "orange", "effect": "FIRE_RESISTANCE", "seconds": 4.0},
    "red_mum": {"display": "Red Mum", "dye": "red", "effect": "REGENERATION", "seconds": 8.0},
    "purple_mum": {"display": "Purple Mum", "dye": "purple", "effect": "NIGHT_VISION", "seconds": 5.0},
}
MUM_PATCH = {"biomes": ["IS_FLORAL", "IS_FOREST"], "rarity": 16, "tries": 32, "spread_xz": 5, "spread_y": 2}


def potted(mum):
    return f"potted_{mum}"


def giant_tile(kind, size, a, b):
    """A 16x16 tile of a giant pumpkin's side (column a from the left, row b from the top) or top (a = x, b = z)."""
    return f"giant_pumpkin_{kind}_{size}_{a}_{b}"


def halloween_textures():
    """Every block texture of the Halloween harvest that its models reference (the gourds' come with GOURDS)."""
    out = ["giant_pumpkin_bottom"]
    for size in range(2, GIANT_PUMPKIN["max_size"] + 1):
        for a in range(size):
            for b in range(size):
                out += [giant_tile("side", size, a, b), giant_tile("top", size, a, b)]
    out += ["harvest_scale_side", "harvest_scale_top", "harvest_scale_dial", "scarecrow_post", "scarecrow_trousers", "scarecrow_straw"]
    out += [f"scarecrow_shirt_{color}" for color in DYE_COLORS]
    out += ["corn_shock_lower", "corn_shock_upper", "ornamental_corn_bundle", "gourd_birdhouse_front", "gourd_birdhouse_side",
            "gourd_birdhouse_top", "gourd_birdhouse_string"]
    return out + list(MUMS)

# Kitchen Garden equipment: blocks with an item of their own.
# Trellis: a square wooden lattice. It stands on farmland, on any sturdy top face or on another
# trellis; climbing crops are planted on it and grow up it. Tomatoes need two stacked.
# Cooking Pot: cooks multi-ingredient meals (POT_RECIPES) while a heat source is under it.
EQUIPMENT = {
    "trellis": {"display": "Trellis"},
    "cooking_pot": {"display": "Cooking Pot"},
}
# Block tag of what heats a Cooking Pot from directly below. Blocks with a "lit" property (campfires)
# only count while lit. Data packs and other branches can add their own heat sources.
HEAT_TAG = "jugcraft:heat_sources"
HEAT_SOURCES = ["#minecraft:campfires", "minecraft:fire", "minecraft:soul_fire", "minecraft:lava", "minecraft:magma_block"]
# Cooking Pot numbers, shared with Java (agriculture/CookingPotBlockEntity.java).
POT_INPUTS = 6       # ingredient slots
POT_OUTPUTS = 4      # result slots after them: soups do not stack, so a pot cooks up to four bowls in a row
POT_COOLING = 2      # progress lost per tick without heat, like a furnace going out

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
    "roasted_chestnuts": {"input": "chestnut", "xp": 0.35},
    "baked_acorn_squash": {"input": "acorn_squash", "xp": 0.35},
    "roasted_pumpkin_seeds": {"input": "minecraft:pumpkin_seeds", "xp": 0.1},
    # Halloween harvest. Sugar melts into caramel; a bottle gourd dries hard enough to hollow out.
    "caramel": {"input": "minecraft:sugar", "xp": 0.1},
    "dried_bottle_gourd": {"input": "bottle_gourd", "xp": 0.1, "category": "misc"},
}
COOK_TIMES = {"smelting": 200, "smoking": 100, "campfire_cooking": 600}

# Cooking Pot recipes (recipe type jugcraft:pot_cooking): any slot order, one serving per batch, and
# ingredients may be stacked for batch cooking. Every filled slot must belong to the recipe. No two
# recipes may share the same set of ingredient items. time: ticks with heat.
POT_RECIPES = {
    "tomato_soup": {"inputs": {"minecraft:bowl": 1, "jugcraft:tomato": 2, "jugcraft:onion": 1}, "time": 200},
    "onion_soup": {"inputs": {"minecraft:bowl": 1, "jugcraft:onion": 2, "jugcraft:garlic": 1, "minecraft:bread": 1}, "time": 200},
    "vegetable_soup": {"inputs": {"minecraft:bowl": 1, "jugcraft:cabbage": 1, "minecraft:carrot": 1, "minecraft:potato": 1,
                                  "jugcraft:onion": 1}, "time": 200},
    "mushroom_barley_soup": {"inputs": {"minecraft:bowl": 1, "jugcraft:barley": 1, "minecraft:brown_mushroom": 1,
                                        "jugcraft:onion": 1}, "time": 200},
    "oat_porridge": {"inputs": {"minecraft:bowl": 1, "jugcraft:oats": 2, "minecraft:sugar": 1}, "time": 200},
    "chili": {"inputs": {"minecraft:bowl": 1, "jugcraft:beans": 1, "jugcraft:tomato": 1, "jugcraft:pepper": 1,
                         "jugcraft:onion": 1, "minecraft:beef": 1}, "time": 300},
    "cabbage_rolls": {"inputs": {"jugcraft:cabbage": 1, "minecraft:beef": 1, "jugcraft:onion": 1, "jugcraft:garlic": 1},
                      "count": 2, "time": 300},
    # Festival crops.
    "butternut_squash_soup": {"inputs": {"minecraft:bowl": 1, "jugcraft:butternut_squash": 1, "jugcraft:onion": 1,
                                         "jugcraft:garlic": 1}, "time": 200},
    "harvest_stew": {"inputs": {"minecraft:bowl": 1, "jugcraft:turnip": 1, "minecraft:carrot": 1, "jugcraft:onion": 1,
                                "minecraft:mutton": 1}, "time": 300},
    "cranberry_sauce": {"inputs": {"minecraft:bowl": 1, "jugcraft:cranberries": 2, "minecraft:sugar": 1}, "time": 200},
    # Halloween harvest: the guts scooped from carved pumpkins make soup.
    "pumpkin_soup": {"inputs": {"minecraft:bowl": 1, "jugcraft:pumpkin_guts": 2, "jugcraft:onion": 1}, "time": 200},
}

# Crafting. result: an ID (jugcraft unless namespaced) and count. features: switches besides agriculture.
SHAPELESS = [
    {"id": "corn_kernels", "inputs": ["jugcraft:corn"], "result": "corn_kernels", "count": 2},
    {"id": "three_sisters_stew", "inputs": ["minecraft:bowl", "jugcraft:corn", "jugcraft:beans", "minecraft:pumpkin"],
     "result": "three_sisters_stew", "count": 1},
    {"id": "string_from_flax", "inputs": ["jugcraft:flax", "jugcraft:flax"], "result": "minecraft:string", "count": 1},
    {"id": "tomato_seeds", "inputs": ["jugcraft:tomato"], "result": "tomato_seeds", "count": 2},
    {"id": "pepper_seeds", "inputs": ["jugcraft:pepper"], "result": "pepper_seeds", "count": 2},
    {"id": "garden_salad", "inputs": ["minecraft:bowl", "jugcraft:cabbage", "jugcraft:tomato", "jugcraft:pepper"],
     "result": "garden_salad", "count": 1},
    # Jugcraft's salt (the mining branch) pickles cabbage.
    {"id": "sauerkraut", "inputs": ["jugcraft:cabbage", "jugcraft:cabbage", "#c:dusts/salt"], "result": "sauerkraut", "count": 2,
     "features": ["salt"]},
    # Festival crops. A gourd gives four seeds, like a vanilla pumpkin.
    {"id": "butternut_squash_seeds", "inputs": ["jugcraft:butternut_squash"], "result": "butternut_squash_seeds", "count": 4},
    {"id": "acorn_squash_seeds", "inputs": ["jugcraft:acorn_squash"], "result": "acorn_squash_seeds", "count": 4},
    {"id": "warty_gourd_seeds", "inputs": ["jugcraft:warty_gourd"], "result": "warty_gourd_seeds", "count": 4},
    {"id": "squash_pie", "inputs": ["jugcraft:butternut_squash", "minecraft:sugar", "#minecraft:eggs"], "result": "squash_pie",
     "count": 1},
    {"id": "candy_corn", "inputs": ["jugcraft:corn", "minecraft:sugar", "minecraft:honey_bottle"], "result": "candy_corn",
     "count": 4},
    {"id": "chestnut_planks", "inputs": ["#jugcraft:chestnut_logs"], "result": "chestnut_planks", "count": 4,
     "category": "building", "group": "planks"},
    # Halloween harvest.
    {"id": "white_pumpkin_seeds", "inputs": ["jugcraft:white_pumpkin"], "result": "white_pumpkin_seeds", "count": 4},
    {"id": "jarrahdale_pumpkin_seeds", "inputs": ["jugcraft:jarrahdale_pumpkin"], "result": "jarrahdale_pumpkin_seeds", "count": 4},
    {"id": "cinderella_pumpkin_seeds", "inputs": ["jugcraft:cinderella_pumpkin"], "result": "cinderella_pumpkin_seeds", "count": 4},
    {"id": "bottle_gourd_seeds", "inputs": ["jugcraft:bottle_gourd"], "result": "bottle_gourd_seeds", "count": 4},
    {"id": "pumpkin_pie_from_heirloom_pumpkins", "inputs": ["#jugcraft:heirloom_pumpkins", "minecraft:sugar", "#minecraft:eggs"],
     "result": "minecraft:pumpkin_pie", "count": 1},
    {"id": "ornamental_corn_kernels", "inputs": ["jugcraft:ornamental_corn"], "result": "ornamental_corn_kernels", "count": 2},
    {"id": "ornamental_corn_bundle", "inputs": ["jugcraft:ornamental_corn", "jugcraft:ornamental_corn", "jugcraft:ornamental_corn",
                                                "minecraft:string"], "result": "ornamental_corn_bundle", "count": 1, "category": "building"},
    {"id": "caramel_apple", "inputs": ["minecraft:apple", "jugcraft:caramel", "minecraft:stick"], "result": "caramel_apple", "count": 1},
    {"id": "popcorn_ball", "inputs": ["jugcraft:popcorn", "jugcraft:popcorn", "jugcraft:caramel"], "result": "popcorn_ball", "count": 1},
    {"id": "gourd_canteen", "inputs": ["jugcraft:dried_bottle_gourd", "minecraft:leather"], "result": "gourd_canteen", "count": 1,
     "category": "equipment"},
    {"id": "blank_stencil", "inputs": ["minecraft:paper", "minecraft:paper"], "result": "blank_stencil", "count": 1},
    {"id": "yellow_dye_from_yellow_mum", "inputs": ["jugcraft:yellow_mum"], "result": "minecraft:yellow_dye", "count": 1, "group": "yellow_dye"},
    {"id": "orange_dye_from_orange_mum", "inputs": ["jugcraft:orange_mum"], "result": "minecraft:orange_dye", "count": 1, "group": "orange_dye"},
    {"id": "red_dye_from_red_mum", "inputs": ["jugcraft:red_mum"], "result": "minecraft:red_dye", "count": 1, "group": "red_dye"},
    {"id": "purple_dye_from_purple_mum", "inputs": ["jugcraft:purple_mum"], "result": "minecraft:purple_dye", "count": 1,
     "group": "purple_dye"},
]
SHAPED = [
    {"id": "barley_bread", "pattern": ["BBB"], "key": {"B": "jugcraft:barley"}, "result": "barley_bread", "count": 1,
     "category": "misc"},
    {"id": "trellis", "pattern": ["S S", " S ", "S S"], "key": {"S": "minecraft:stick"}, "result": "trellis", "count": 2,
     "category": "misc"},
    {"id": "cooking_pot", "pattern": ["S S", "I I", "III"], "key": {"S": "minecraft:stick", "I": "#c:ingots/iron"},
     "result": "cooking_pot", "count": 1, "category": "misc"},
    # Festival crops: the lantern (like vanilla's jack o'lantern recipe) and the chestnut wood set (like oak's).
    {"id": "turnip_lantern", "pattern": ["T", "B"], "key": {"T": "jugcraft:turnip", "B": "minecraft:torch"},
     "result": "turnip_lantern", "count": 1, "category": "building"},
    {"id": "chestnut_wood", "pattern": ["##", "##"], "key": {"#": "jugcraft:chestnut_log"}, "result": "chestnut_wood",
     "count": 3, "category": "building", "group": "bark"},
    {"id": "stripped_chestnut_wood", "pattern": ["##", "##"], "key": {"#": "jugcraft:stripped_chestnut_log"},
     "result": "stripped_chestnut_wood", "count": 3, "category": "building", "group": "bark"},
    {"id": "chestnut_stairs", "pattern": ["#  ", "## ", "###"], "key": {"#": "jugcraft:chestnut_planks"}, "result": "chestnut_stairs",
     "count": 4, "category": "building", "group": "wooden_stairs"},
    {"id": "chestnut_slab", "pattern": ["###"], "key": {"#": "jugcraft:chestnut_planks"}, "result": "chestnut_slab",
     "count": 6, "category": "building", "group": "wooden_slab"},
    {"id": "chestnut_fence", "pattern": ["W#W", "W#W"], "key": {"W": "jugcraft:chestnut_planks", "#": "minecraft:stick"},
     "result": "chestnut_fence", "count": 3, "category": "misc", "group": "wooden_fence"},
    {"id": "chestnut_fence_gate", "pattern": ["#W#", "#W#"], "key": {"W": "jugcraft:chestnut_planks", "#": "minecraft:stick"},
     "result": "chestnut_fence_gate", "count": 1, "category": "redstone", "group": "wooden_fence_gate"},
    # Halloween harvest: a platform scale with a clock for a dial, a straw man on a post, a stook of stalks
    # and a hollowed gourd on a string.
    {"id": "harvest_scale", "pattern": [" C ", "III", "PPP"], "key": {"C": "minecraft:clock", "I": "#c:ingots/iron",
                                                                      "P": "#minecraft:planks"},
     "result": "harvest_scale", "count": 1, "category": "misc"},
    {"id": "scarecrow", "pattern": [" W ", "SHS", " S "], "key": {"W": "#minecraft:wool", "S": "minecraft:stick", "H": "minecraft:hay_block"},
     "result": "scarecrow", "count": 1, "category": "building"},
    {"id": "corn_shock", "pattern": [" T ", "SSS", "S S"], "key": {"T": "minecraft:string", "S": "jugcraft:corn_stalks"},
     "result": "corn_shock", "count": 1, "category": "building"},
    {"id": "gourd_birdhouse", "pattern": ["T", "G"], "key": {"T": "minecraft:string", "G": "jugcraft:dried_bottle_gourd"},
     "result": "gourd_birdhouse", "count": 1, "category": "building"},
]

# Growth rules shared with Java (agriculture/CropGrowth.java): non-legume crops next to a
# legume (block tag jugcraft:nitrogen_fixing_crops, 8 neighbours) grow this much faster.
LEGUME_BONUS = 1.5
LEGUME_TAG = "jugcraft:nitrogen_fixing_crops"


def tall_blocks():
    return [info["block"] for info in TALL_CROPS.values()]


def crop_blocks():
    return tall_blocks() + [info["block"] for info in CROPS.values()]


def trellis_crops():
    return [info["block"] for info in TALL_CROPS.values() if info.get("trellis")]


def stem_blocks():
    return [block for gourd in GOURDS for block in (stem(gourd), attached_stem(gourd))]


def giant_blocks():
    return [GIANT_PUMPKIN["block"], GIANT_PUMPKIN["vine"], GIANT_PUMPKIN["attached_vine"]]


def planted_blocks():
    """Blocks that a seed item places: crops, gourd stems and vines, the cranberry bush and the chestnut sapling."""
    return crop_blocks() + [stem(gourd) for gourd in GOURDS] + [CRANBERRY["block"], CHESTNUT["sapling"], GIANT_PUMPKIN["vine"]]


def itemless_blocks():
    """Blocks without an item of their own: the item that plants them (or the pumpkins they drop) stands in for them."""
    return crop_blocks() + stem_blocks() + [CRANBERRY["block"], CHESTNUT["sapling"]] + giant_blocks() + [potted(m) for m in MUMS]


def all_blocks():
    """Every registered agriculture block. Crops have no block item (seeds place them); wild plants and equipment do."""
    return (crop_blocks() + stem_blocks() + [CRANBERRY["block"]] + list(WILD_CROPS) + list(EQUIPMENT) + list(GOURDS)
            + list(TREE_BLOCKS) + list(WOOD) + list(DECOR) + [CARVING["block"]] + list(CARVED_VARIETIES.values())
            + giant_blocks() + [HARVEST_SCALE["block"]] + list(HALLOWEEN_DECOR) + list(MUMS) + [potted(m) for m in MUMS])


def all_items():
    return (list(ITEMS) + list(SICKLES) + list(WILD_CROPS) + list(EQUIPMENT) + list(GOURDS) + [CHESTNUT["leaves"]]
            + list(WOOD) + list(DECOR) + [CARVING["block"], CARVING["knife"]] + list(CARVED_VARIETIES.values())
            + [HARVEST_SCALE["block"]] + list(HARVEST_SCALE["ribbons"]) + [STENCILS["blank"], STENCILS["stencil"], CANTEEN["item"]]
            + list(HALLOWEEN_DECOR) + list(MUMS))


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
    out += [f"{gourd}_{part}" for gourd in GOURDS for part in ("side", "top")] + STEM_TEXTURES + CRANBERRY["stages"]
    return out + EQUIPMENT_TEXTURES + TREE_TEXTURES + DECOR_TEXTURES + halloween_textures()


EQUIPMENT_TEXTURES = ["trellis", "trellis_post", "cooking_pot_side", "cooking_pot_rim", "cooking_pot_empty", "cooking_pot_soup"]

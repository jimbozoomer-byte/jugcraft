"""Thanksgiving side dishes (Thanksgiving batch 2, worker C; docs/features/thanksgiving-dishes.md): six placed feast
dishes, each a food that a sneaking player sets down on a table as its sculpted dish and takes back with an empty
hand, exactly as the menu's plated meals (tools/menu.py, PlacedDishBlock): it is only ever the one food it was, drops
that food when broken, and gives it back when taken.

The coordinator's six candidates, checked against what Jugcraft already has (the record keeps the inventory table):
- Glazed carrots: new, glazed with butter and sugar;
- Roasted root vegetables: new (Herb-Roasted Potatoes is potatoes alone; the Root Vegetable Hamper is a decoration);
- Maple baked squash: Baked Acorn Squash already exists (a baked half squash), so the seasonal stand-in is Scalloped
  Potatoes, sliced potatoes baked in cream with garlic and thyme in a ceramic baking dish (Succotash was weighed and
  dropped: Three Sisters Stew already makes corn and beans into a dish);
- Cranberry tart: the Hearth Oven already bakes a Cranberry Pie, so the stand-in is an Apple Cranberry Crisp, fruit
  under a crumble, baked in a ceramic baking dish;
- Stuffed mushrooms: new, on the glazed plate (brown caps would vanish on wood);
- Skillet corn cakes: new, fried cornmeal griddle cakes in the owner's Skillet (not the Cornbread loaf, the Cornbread
  Stuffing's cubes or the Maple Pancakes of wheat).

No recipe takes a filled container (a bucket or a bottle), so each dish's essence is its plain recipe cost
(tools/build_alchemy_values.py skips recipes that give a container back). Each dish's food follows docs/BALANCE.md
as the menu's do: at most COOK_BONUS more hunger than its ingredients, each counted as the most it would give
(tools/thanksgiving_dishes_check.py checks it). Each is eaten like the menu's plated
meals: a bowl food that gives the bowl back, one to a stack. Nothing asks for research or follows the calendar; every
recipe follows the agriculture feature switch.

Models and textures: tools/thanksgiving_dishes_data.py, through the placed-food pipeline of docs/FOOD_MODELS.md
(tools/menu_meal_models.py and tools/menu_meal_textures.py: the same dishware, piece helpers, top-and-side texture
method and writer). Java: io.github.jimbozoomer.jugcraft.agriculture.ThanksgivingDishes registers them.
"""
FEATURE = "agriculture"

COOK_BONUS = 3

# Each dish: its name, food [hunger, saturation modifier], how it sits (PlacedDishBlock.DishShape: PLATE for the plates,
# the skillet and the baking dish, all inside 1..15 and under 5.5 high) and its shapeless recipe (a bowl first, given
# back when it is eaten).
DISHES = {
    "glazed_carrots": {"display": "Glazed Carrots", "food": [9, 0.7], "shape": "PLATE",
                       "inputs": ["minecraft:bowl", "minecraft:carrot", "minecraft:carrot", "minecraft:carrot",
                                  "minecraft:sugar", "jugcraft:butter"]},
    "roasted_root_vegetables": {"display": "Roasted Root Vegetables", "food": [10, 0.7], "shape": "PLATE",
                                "inputs": ["minecraft:bowl", "minecraft:carrot", "minecraft:potato", "minecraft:beetroot",
                                           "jugcraft:turnip", "jugcraft:onion", "jugcraft:rosemary"]},
    "scalloped_potatoes": {"display": "Scalloped Potatoes", "food": [9, 0.7], "shape": "PLATE",
                           "inputs": ["minecraft:bowl", "minecraft:potato", "minecraft:potato", "jugcraft:butter", "jugcraft:garlic",
                                      "jugcraft:thyme"]},
    "apple_cranberry_crisp": {"display": "Apple Cranberry Crisp", "food": [8, 0.6], "shape": "PLATE",
                              "inputs": ["minecraft:bowl", "minecraft:apple", "jugcraft:cranberries", "jugcraft:cranberries",
                                         "jugcraft:flour", "minecraft:sugar", "jugcraft:butter"]},
    "stuffed_mushrooms": {"display": "Stuffed Mushrooms", "food": [7, 0.6], "shape": "PLATE",
                          "inputs": ["minecraft:bowl", "minecraft:brown_mushroom", "minecraft:brown_mushroom", "minecraft:bread",
                                     "jugcraft:garlic", "jugcraft:thyme"]},
    "skillet_corn_cakes": {"display": "Skillet Corn Cakes", "food": [9, 0.7], "shape": "PLATE",
                           "inputs": ["minecraft:bowl", "jugcraft:cornbread_batter", "jugcraft:butter", "minecraft:sugar"]},
}

# The most hunger each ingredient could give, for the balance check (docs/BALANCE.md): as itself, as what a furnace
# makes of it (a potato bakes), or, for one that is not food, as what went into it (the batter: two corn, an egg and
# milk). Vanilla's values for vanilla items; Jugcraft's from tools/agriculture.py ITEMS.
MOST_HUNGER = {
    "minecraft:bowl": 0, "minecraft:carrot": 3, "minecraft:potato": 5, "minecraft:beetroot": 1, "minecraft:apple": 4,
    "minecraft:sugar": 0, "minecraft:brown_mushroom": 0, "minecraft:bread": 5,
    "jugcraft:turnip": 3, "jugcraft:onion": 0, "jugcraft:rosemary": 0, "jugcraft:butter": 0, "jugcraft:cranberries": 2,
    "jugcraft:flour": 0, "jugcraft:garlic": 0, "jugcraft:thyme": 0, "jugcraft:cornbread_batter": 6,
}


def blocks():
    return list(DISHES)


def items():
    return list(DISHES)


def names():
    out = {}
    for dish, spec in DISHES.items():
        out[f"item.jugcraft.{dish}"] = spec["display"]
        out[f"block.jugcraft.{dish}"] = spec["display"]
    return out

"""Fall Garden & Porch, decor set 4 (docs/features/fall-garden-and-porch.md).

Part 1, the fall garden's plants, are wild plants of the biomes branch's kind (tools/plants.py PLANTS, sculpted by
tools/fall_garden_flora.py): New England Asters, Autumn Stonecrop, Ornamental Kale and Cabbage, Pampas Grass, Dried
Sunflowers and Bittersweet. Goldenrod and Cattails, which set 4's list also named, grow already (tools/plants.py).

The wild ones are found in vanilla's biomes (their "patch"). The garden ones are made here from what they come from:
- ornamental kale and cabbage are cabbages grown for their colour: a cabbage seed and purple dye make a kale (its
  violet heart), a cabbage seed and bone meal a cabbage (its pale one). Neither takes the dye it gives (magenta,
  white), so no recipe leads back to itself;
- a dried sunflower is a sunflower left standing to dry: one sunflower makes one.
They are the biomes branch's plants, so their recipes follow its switch, and the cabbages' the agriculture one too
(the seed is the kitchen garden's).
"""

# Shapeless recipes (tools/agriculture.py SHAPELESS).
SHAPELESS = [
    {"id": "ornamental_kale", "inputs": ["jugcraft:cabbage_seeds", "minecraft:purple_dye"], "result": "ornamental_kale", "count": 1,
     "category": "misc", "switch": "biomes", "features": ["agriculture"]},
    {"id": "ornamental_cabbage", "inputs": ["jugcraft:cabbage_seeds", "minecraft:bone_meal"], "result": "ornamental_cabbage", "count": 1,
     "category": "misc", "switch": "biomes", "features": ["agriculture"]},
    {"id": "dried_sunflower", "inputs": ["minecraft:sunflower"], "result": "dried_sunflower", "count": 1, "category": "misc",
     "switch": "biomes"},
]

# Part 1's plants, all in tools/plants.py.
PLANTS = ("aster", "sedum", "ornamental_kale", "ornamental_cabbage", "pampas_grass", "dried_sunflower", "bittersweet")

# ---------------------------------------------------------------- part 2: planters
# Planters (agriculture/PlanterBlock): each takes small flowers (#minecraft:small_flowers), one to a spot, and shows each
# at `scale` of its size standing on the soil. Drawn facing north, in pixels: `spots` are {x, z}, `soil` the soil's
# height; a wall planter's wall is behind it, to the south. Sizes are real ones: the window box 94 cm long, 25 cm deep
# and 25 cm high; the half-barrel 62 cm across and 45 cm high; the galvanized wash tub 75 by 50 cm and 28 cm high.
PLANTERS = {
    "window_box": {"display": "Window Box", "java": "WINDOW_BOX", "spots": [(3.0, 14.0), (8.0, 14.0), (13.0, 14.0)], "soil": 3.5,
                   "scale": 0.5, "wall": True},
    "half_barrel_planter": {"display": "Half-Barrel Planter", "java": "HALF_BARREL", "spots": [(8.0, 8.0)], "soil": 6.5, "scale": 0.9,
                            "wall": False},
    "galvanized_tub_planter": {"display": "Galvanized Tub Planter", "java": "GALVANIZED_TUB", "spots": [(5.5, 8.0), (10.5, 8.0)],
                               "soil": 3.75, "scale": 0.65, "wall": False},
}

RECIPES = [
    {"id": "window_box", "pattern": ["PDP", "PPP"], "key": {"P": "#minecraft:planks", "D": "minecraft:dirt"}, "result": "window_box",
     "count": 1, "category": "misc"},
    {"id": "galvanized_tub_planter", "pattern": ["IDI", "III"], "key": {"I": "#c:ingots/iron", "D": "minecraft:dirt"},
     "result": "galvanized_tub_planter", "count": 1, "category": "misc"},
]
# A barrel sawn in two makes two half-barrels, each filled with dirt; nothing turns them back into a barrel.
SHAPELESS += [
    {"id": "half_barrel_planter", "inputs": ["minecraft:barrel", "minecraft:dirt", "minecraft:dirt"], "result": "half_barrel_planter",
     "count": 2, "category": "misc"},
]


def blocks():
    """Every block set 4 adds beyond the plants (which are tools/plants.py's)."""
    return list(PLANTERS)

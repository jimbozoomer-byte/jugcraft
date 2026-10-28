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


# ---------------------------------------------------------------- part 3: the porch
# The Porch Swing (agriculture/PorchSwingBlock): a slatted bench swing 1.5 m wide, its seat 50 cm up, hung by chains from
# two hooks in a ceiling two blocks up; two sit on it. It sways `breeze` degrees each way when empty and `sitter` with
# someone on it, a sway taking `ticks`; it turns about its hooks, `pivot` (height, depth in pixels from its first block,
# facing north).
SWING = {"block": "porch_swing", "display": "Porch Swing", "size": (2, 2, 1), "seat": 0.5, "breeze": 1.5, "sitter": 8.0, "ticks": 70.0,
         "pivot": (31.5, 8.0)}
# The rest of the porch: decorations. The Wheat Stook: six sheaves of wheat stood together to dry, 95 cm high. The Corn
# Dolly: a plaited straw harvest knot with a red ribbon, hung on a wall or door. The Bittersweet Wreath: a ring of
# twisted bittersweet vine with its berries and yellow leaves, 69 cm across, on a wall. The Quilt Rail: a length of
# porch rail with a patchwork quilt hung over it to air.
PORCH = {
    "wheat_stook": {"display": "Wheat Stook"},
    "corn_dolly": {"display": "Corn Dolly"},
    "bittersweet_wreath": {"display": "Bittersweet Wreath"},
    "quilt_rail": {"display": "Quilt Rail"},
}

RECIPES += [
    {"id": "porch_swing", "pattern": ["C C", "PSP", "P P"], "key": {"C": "minecraft:iron_chain", "P": "#minecraft:planks",
                                                                   "S": "#minecraft:wooden_slabs"},
     "result": "porch_swing", "count": 1, "category": "misc"},
    {"id": "bittersweet_wreath", "pattern": ["BBB", "B B", "BBB"], "key": {"B": "jugcraft:bittersweet"}, "result": "bittersweet_wreath",
     "count": 1, "category": "misc"},
]
SHAPELESS += [
    {"id": "wheat_stook", "inputs": ["minecraft:wheat"] * 6 + ["minecraft:string"], "result": "wheat_stook", "count": 1, "category": "misc"},
    {"id": "corn_dolly", "inputs": ["minecraft:wheat", "minecraft:wheat", "minecraft:wheat", "minecraft:red_dye"], "result": "corn_dolly",
     "count": 1, "category": "misc"},
    {"id": "quilt_rail", "inputs": ["#minecraft:wooden_fences", "#minecraft:wool", "#minecraft:wool", "#minecraft:wool"],
     "result": "quilt_rail", "count": 1, "category": "misc"},
]


# ---------------------------------------------------------------- part 4: café lights
# Café String Lights (StringLightHookBlockEntity.Strand.CAFE): warm Edison bulbs on a black cord, strung between String
# Light Hooks like the other strands and lit the same way (redstone, or electricity). While lit they light the air along
# them: a lamp (a vanilla light block, light `light`) in empty air about every `spacing` blocks along the sag, put out
# again when they go dark, come down or lose a hook.
CAFE_LIGHTS = {"item": "cafe_string_lights", "display": "Café String Lights", "light": 12, "spacing": 2.0}

SHAPELESS += [
    {"id": "cafe_string_lights", "inputs": ["minecraft:string", "minecraft:string", "minecraft:glowstone_dust", "minecraft:glass_pane",
                                            "minecraft:black_dye"], "result": "cafe_string_lights", "count": 1, "category": "misc"},
]


def items():
    """Every item set 4 adds beyond its blocks' and the plants'."""
    return [CAFE_LIGHTS["item"]]


def blocks():
    """Every block set 4 adds beyond the plants (which are tools/plants.py's)."""
    return list(PLANTERS) + [SWING["block"]] + list(PORCH)

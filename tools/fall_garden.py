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

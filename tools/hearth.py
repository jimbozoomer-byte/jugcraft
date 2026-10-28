"""Cozy Fall Hearth, decor set 5 (docs/features/cozy-fall-hearth.md): the hearth and the room round it.

Part 1, the hearth itself:
- The Stone Fireplace (agriculture/StoneFireplaceBlock): a fieldstone chimney breast three blocks across and three up
  (3 m: floor to ceiling), its firebox opening 1.25 m across and 78 cm high on a flagstone hearth that runs out before
  it, an oak mantel shelf across it at 1.4 m, and the stack stepping in above. Lit with flint and steel or a fire
  charge, it burns in its firebox (its fire is its own full-bright model, an animated texture in smooth bands) and gives
  light `light` from its firebox block; a shovel puts it out. It is a decoration: it burns no fuel and makes nothing.
- The Mantel Garland (an item): autumn leaves, little pumpkins and pine cones. Use it on the fireplace to drape it
  along the mantel (`garland`, shown on the mantel's blocks); sneak-use it with an empty hand to take it back.

Part 2, the stove:
- The Cast-Iron Woodstove (agriculture/WoodstoveBlock): a parlour box stove 78 cm wide, 62 cm deep and 1 m high on four
  cast legs, its firebox door glazed with mica, a nickel towel rail across its front, two lids in its top plate, its
  stovepipe going out of its back into the wall. Lit as the fireplace is, its fire shows through the mica (the
  fireplace's fire texture), it gives light `light`, and it heats what stands on it (block tag jugcraft:heat_sources): a
  Cooking Pot brews on it as on a campfire. Its hot top burns what stands on it (`burn`, as a magma block). It burns no
  fuel and cooks nothing itself: the Kitchen Stove is the range, this is a heater.
- The Tea Kettle (TeaKettleBlock) and the Simmer Pot (SimmerPotBlock), set on the stove (StovetopBlock): each stands on
  any floor or heat source and is `heated` while that heats a pot. Heated, the kettle boils, steaming from its spout,
  and its lid rattles (client/TeaKettleRenderer: up to `lid_lift` pixels and `lid_tip` degrees about its back edge,
  `hinge`, height and depth); the simmer pot's spiced water simmers, its surface an animated texture in smooth bands
  (`frames` frames of `frametime` ticks). They make nothing.
Sizes in pixels, facing north (its wall behind, to the south).
"""

FIREPLACE = {"block": "stone_fireplace", "display": "Stone Fireplace", "size": (3, 3, 1), "light": 15, "frames": 8, "frametime": 3,
             "opening": (14.0, 34.0, 1.5, 14.0), "mantel": 22.0}
GARLAND = {"item": "mantel_garland", "display": "Mantel Garland"}
WOODSTOVE = {"block": "woodstove", "display": "Cast-Iron Woodstove", "light": 13, "burn": 1.0}
KETTLE = {"block": "tea_kettle", "display": "Tea Kettle", "lid_lift": 0.35, "lid_tip": 5.0, "hinge": (4.25, 9.25)}
SIMMER_POT = {"block": "simmer_pot", "display": "Simmer Pot", "frames": 8, "frametime": 4}

RECIPES = [
    {"id": "stone_fireplace", "pattern": ["SSS", "PCP", "SFS"], "key": {"S": "minecraft:cobblestone", "P": "#minecraft:planks",
                                                                       "C": "minecraft:campfire", "F": "minecraft:smooth_stone_slab"},
     "result": "stone_fireplace", "count": 1, "category": "misc"},
    {"id": "woodstove", "pattern": ["III", "IFI", "I I"], "key": {"I": "minecraft:iron_ingot", "F": "minecraft:furnace"},
     "result": "woodstove", "count": 1, "category": "misc"},
    {"id": "tea_kettle", "pattern": [" N ", "C C", "CCC"], "key": {"N": "minecraft:iron_nugget", "C": "minecraft:copper_ingot"},
     "result": "tea_kettle", "count": 1, "category": "misc"},
]
SHAPELESS = [
    {"id": "mantel_garland", "inputs": ["#minecraft:leaves", "#minecraft:leaves", "minecraft:pumpkin", "minecraft:string"],
     "result": "mantel_garland", "count": 1, "category": "misc"},
    {"id": "simmer_pot", "inputs": ["minecraft:iron_ingot", "minecraft:water_bucket", "minecraft:apple", "jugcraft:mulling_spices"],
     "result": "simmer_pot", "count": 1, "category": "misc"},
]


def blocks():
    return [FIREPLACE["block"], WOODSTOVE["block"], KETTLE["block"], SIMMER_POT["block"]]


def items():
    return blocks() + [GARLAND["item"]]

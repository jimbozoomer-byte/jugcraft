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

Part 3, the woodpile:
- The Firewood Rack (FirewoodRackBlock): a wrought-iron log rack 87 cm wide, 37 cm deep and 94 cm high. It takes logs of
  any kind, up to `capacity`, each shown as a split log, four to a layer; an empty hand takes back the last; broken, it
  gives them back as they went in.
- The Chopping Block (ChoppingBlockBlock): a sawn round of oak with a hatchet sunk in it. A log used on it is split into
  exactly what crafting it gives (its own crafting recipe), and the hatchet swings up `swing` degrees about its grip
  (`grip`, height and depth) and back down over `swing_ticks` ticks (client/ChoppingBlockRenderer).
Sizes in pixels, facing north (its wall behind, to the south).
"""

FIREPLACE = {"block": "stone_fireplace", "display": "Stone Fireplace", "size": (3, 3, 1), "light": 15, "frames": 8, "frametime": 3,
             "opening": (14.0, 34.0, 1.5, 14.0), "mantel": 22.0}
GARLAND = {"item": "mantel_garland", "display": "Mantel Garland"}
WOODSTOVE = {"block": "woodstove", "display": "Cast-Iron Woodstove", "light": 13, "burn": 1.0}
KETTLE = {"block": "tea_kettle", "display": "Tea Kettle", "lid_lift": 0.35, "lid_tip": 5.0, "hinge": (4.25, 9.25)}
SIMMER_POT = {"block": "simmer_pot", "display": "Simmer Pot", "frames": 8, "frametime": 4}
RACK = {"block": "firewood_rack", "display": "Firewood Rack", "capacity": 16}
CHOPPING_BLOCK = {"block": "chopping_block", "display": "Chopping Block", "swing_ticks": 8, "swing": 55.0, "grip": (10.6, 2.46)}

RECIPES = [
    {"id": "stone_fireplace", "pattern": ["SSS", "PCP", "SFS"], "key": {"S": "minecraft:cobblestone", "P": "#minecraft:planks",
                                                                       "C": "minecraft:campfire", "F": "minecraft:smooth_stone_slab"},
     "result": "stone_fireplace", "count": 1, "category": "misc"},
    {"id": "woodstove", "pattern": ["III", "IFI", "I I"], "key": {"I": "minecraft:iron_ingot", "F": "minecraft:furnace"},
     "result": "woodstove", "count": 1, "category": "misc"},
    {"id": "tea_kettle", "pattern": [" N ", "C C", "CCC"], "key": {"N": "minecraft:iron_nugget", "C": "minecraft:copper_ingot"},
     "result": "tea_kettle", "count": 1, "category": "misc"},
    {"id": "firewood_rack", "pattern": ["B B", "B B", "III"], "key": {"B": "minecraft:iron_bars", "I": "minecraft:iron_ingot"},
     "result": "firewood_rack", "count": 1, "category": "misc"},
]
SHAPELESS = [
    {"id": "mantel_garland", "inputs": ["#minecraft:leaves", "#minecraft:leaves", "minecraft:pumpkin", "minecraft:string"],
     "result": "mantel_garland", "count": 1, "category": "misc"},
    {"id": "simmer_pot", "inputs": ["minecraft:iron_ingot", "minecraft:water_bucket", "minecraft:apple", "jugcraft:mulling_spices"],
     "result": "simmer_pot", "count": 1, "category": "misc"},
    {"id": "chopping_block", "inputs": ["#minecraft:logs_that_burn", "minecraft:iron_axe"], "result": "chopping_block", "count": 1,
     "category": "misc"},
]


def blocks():
    return [FIREPLACE["block"], WOODSTOVE["block"], KETTLE["block"], SIMMER_POT["block"], RACK["block"], CHOPPING_BLOCK["block"]]


def items():
    return blocks() + [GARLAND["item"]]

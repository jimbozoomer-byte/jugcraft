"""Cozy Fall Hearth, decor set 5 (docs/features/cozy-fall-hearth.md): the hearth and the room round it.

Part 1, the hearth itself:
- The Stone Fireplace (agriculture/StoneFireplaceBlock): a fieldstone chimney breast three blocks across and three up
  (3 m: floor to ceiling), its firebox opening 1.25 m across and 78 cm high on a flagstone hearth that runs out before
  it, an oak mantel shelf across it at 1.4 m, and the stack stepping in above. Lit with flint and steel or a fire
  charge, it burns in its firebox (its fire is its own full-bright model, an animated texture in smooth bands) and gives
  light `light` from its firebox block; a shovel puts it out. It is a decoration: it burns no fuel and makes nothing.
- The Mantel Garland (an item): autumn leaves, little pumpkins and pine cones. Use it on the fireplace to drape it
  along the mantel (`garland`, shown on the mantel's blocks); sneak-use it with an empty hand to take it back.
Sizes in pixels, facing north (its wall behind, to the south).
"""

FIREPLACE = {"block": "stone_fireplace", "display": "Stone Fireplace", "size": (3, 3, 1), "light": 15, "frames": 8, "frametime": 3,
             "opening": (14.0, 34.0, 1.5, 14.0), "mantel": 22.0}
GARLAND = {"item": "mantel_garland", "display": "Mantel Garland"}

RECIPES = [
    {"id": "stone_fireplace", "pattern": ["SSS", "PCP", "SFS"], "key": {"S": "minecraft:cobblestone", "P": "#minecraft:planks",
                                                                       "C": "minecraft:campfire", "F": "minecraft:smooth_stone_slab"},
     "result": "stone_fireplace", "count": 1, "category": "misc"},
]
SHAPELESS = [
    {"id": "mantel_garland", "inputs": ["#minecraft:leaves", "#minecraft:leaves", "minecraft:pumpkin", "minecraft:string"],
     "result": "mantel_garland", "count": 1, "category": "misc"},
]


def blocks():
    return [FIREPLACE["block"]]


def items():
    return blocks() + [GARLAND["item"]]

"""Harvest Festival Grounds, decor set 10 (docs/features/harvest-festival-grounds.md): the grounds of a country harvest
festival, its displays and its games, at their real sizes.

Part 1, the Giant Cornucopia (agriculture/GiantCornucopiaBlock): a festival horn of plenty of woven willow 2.7 m long,
its mouth 1.25 m across behind a braided rim, lying on its side with its tail curling up and back over it, and the
harvest spilling out of its mouth at its own size: three pumpkins, a butternut squash, an acorn squash, ornamental
gourds, corn in its husks, a sheaf of wheat, a bunch of grapes and apples rolled out over the ground. Three blocks
across, two up and two deep, placed and broken as one (MultiDecorationBlock); it has no lanterns. Its mouth is to the
west of its front (to the right of whoever placed it), its tail to the east.
Sizes in pixels of its frame (48 across, 32 up, 32 deep), facing north.
"""

# Its mouth across its opening inside the rim (20 px, 1.25 m) and the horn's length from its rim to the back of its tail
# (43 px, 2.7 m), in pixels.
CORNUCOPIA = {"block": "giant_cornucopia", "display": "Giant Cornucopia", "size": (3, 2, 2), "mouth": 20.0, "length": 43.0}

RECIPES = [
    # The horn's willow (sticks woven round wheat) and the harvest in it, grown on the farm: a pumpkin, a butternut
    # squash, an ear of corn and an apple.
    {"id": CORNUCOPIA["block"], "pattern": ["SWS", "PBC", "SAS"],
     "key": {"S": "minecraft:stick", "W": "minecraft:wheat", "P": "minecraft:pumpkin", "B": "jugcraft:butternut_squash",
             "C": "jugcraft:corn", "A": "minecraft:apple"},
     "result": CORNUCOPIA["block"], "count": 1, "category": "building"},
]


def blocks():
    return [CORNUCOPIA["block"]]


def items():
    return blocks()

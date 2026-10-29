"""Harvest Festival Grounds, decor set 10 (docs/features/harvest-festival-grounds.md): the grounds of a country harvest
festival, its displays and its games, at their real sizes.

Part 1, the Giant Cornucopia (agriculture/GiantCornucopiaBlock): a festival horn of plenty of woven willow 2.7 m long,
its mouth 1.25 m across behind a braided rim, lying on its side with its tail curling up and back over it, and the
harvest spilling out of its mouth at its own size: three pumpkins, a butternut squash, an acorn squash, ornamental
gourds, corn in its husks, a sheaf of wheat, a bunch of grapes and apples rolled out over the ground. Three blocks
across, two up and two deep, placed and broken as one (MultiDecorationBlock); it has no lanterns. Its mouth is to the
west of its front (to the right of whoever placed it), its tail to the east.
Sizes in pixels of its frame (48 across, 32 up, 32 deep), facing north.

Part 2, the Giant Vegetable Show: four prize show vegetables at a show's sizes, each lying or standing on the ground with
its entry card on a wire stake beside it (agriculture/FacingDecorationBlock, one block each): a Giant Cabbage, its head
50 cm across in its splayed outer leaves 94 cm across; a Giant Marrow 94 cm long and 38 cm across, striped, on a bed of
straw; a Giant Turnip 44 cm across under its leaves; and a Giant Carrot, its root 25 cm across at the shoulder and 69 cm
long, lying under its fronds. Each is made from nine of its crop and drops only itself. And the Prize Ribbon Board
(agriculture/PrizeRibbonBoardBlock, two blocks tall): a show's results board, painted green on two posts 1.6 m tall,
SHOW lettered along its top and three brass cup hooks numbered 1, 2 and 3 under it; the Harvest Scale's (and the corn
maze's) First, Second and Third Prize Ribbons hang on it, each on its own hook, and come back off it, or out of it when
it is broken.
Sizes in pixels of a block (16 a metre), facing north.
"""

# Its mouth across its opening inside the rim (20 px, 1.25 m) and the horn's length from its rim to the back of its tail
# (43 px, 2.7 m), in pixels.
CORNUCOPIA = {"block": "giant_cornucopia", "display": "Giant Cornucopia", "size": (3, 2, 2), "mouth": 20.0, "length": 43.0}

# The show's vegetables: each `block`, `display`, the crop nine of which make it, and its sizes in pixels.
SHOW = {
    "cabbage": {"block": "giant_cabbage", "display": "Giant Cabbage", "crop": "jugcraft:cabbage", "head": 8.0, "spread": 15.0},
    "marrow": {"block": "giant_marrow", "display": "Giant Marrow", "crop": "jugcraft:zucchini", "length": 15.0, "across": 6.0},
    "turnip": {"block": "giant_turnip", "display": "Giant Turnip", "crop": "jugcraft:turnip", "across": 7.0},
    "carrot": {"block": "giant_carrot", "display": "Giant Carrot", "crop": "minecraft:carrot", "shoulder": 4.0, "length": 11.0},
}
# The Prize Ribbon Board: its hooks in order (1, 2, 3, left to right as you face it), the ribbon each takes, the block
# state that says it hangs there, and the board's sizes in pixels (its panel, its posts, its hooks' height and their
# rosettes across).
RIBBON_BOARD = {"block": "prize_ribbon_board", "display": "Prize Ribbon Board",
                "hooks": (("first_prize_ribbon", "first"), ("second_prize_ribbon", "second"), ("third_prize_ribbon", "third")),
                "panel": (0.5, 14.0, 15.5, 25.0), "post": 1.5, "height": 26.0, "hook_y": 18.5, "rosette": 4.0}

RECIPES = [
    # The horn's willow (sticks woven round wheat) and the harvest in it, grown on the farm: a pumpkin, a butternut
    # squash, an ear of corn and an apple.
    {"id": CORNUCOPIA["block"], "pattern": ["SWS", "PBC", "SAS"],
     "key": {"S": "minecraft:stick", "W": "minecraft:wheat", "P": "minecraft:pumpkin", "B": "jugcraft:butternut_squash",
             "C": "jugcraft:corn", "A": "minecraft:apple"},
     "result": CORNUCOPIA["block"], "count": 1, "category": "building"},
    # Nine of each crop grow one prize vegetable, as nine Warty Gourds make the Giant Warty Gourd.
    *({"id": spec["block"], "pattern": ["CCC", "CCC", "CCC"], "key": {"C": spec["crop"]}, "result": spec["block"], "count": 1,
       "category": "building"} for spec in SHOW.values()),
    # The board's planks, painted green, on two sticks, and a gold nugget for its brass hooks.
    {"id": RIBBON_BOARD["block"], "pattern": ["PGP", "PNP", "S S"],
     "key": {"P": "#minecraft:planks", "G": "minecraft:green_dye", "N": "minecraft:gold_nugget", "S": "minecraft:stick"},
     "result": RIBBON_BOARD["block"], "count": 1, "category": "building"},
]


def blocks():
    return [CORNUCOPIA["block"]] + [spec["block"] for spec in SHOW.values()] + [RIBBON_BOARD["block"]]


def items():
    return blocks()

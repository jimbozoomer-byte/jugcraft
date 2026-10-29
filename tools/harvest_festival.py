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

Part 3, the Steam Traction Engine and the Hay Wagon. The engine (agriculture/SteamTractionEngineBlock, three blocks
across, three up and five deep; MultiDecorationBlock): a farm traction engine of about 1910 at its real size, 5 m long,
2.2 m wide and 3 m to its chimney's top. Its boiler is lagged in green-painted strips under brass bands, its smokebox and
chimney black under a copper cap, its rear wheels 1.75 m across with strakes across their treads and its front wheels
1.1 m, all red; its single cylinder sits on the boiler's front, its crankshaft across the hornplates over the firebox,
its flywheel 1.3 m across on the left as you face it; a footplate and a coal tender behind. Fire it with coal or
charcoal, one at a time, each burning as long as in a furnace, up to `max_fuel`: while it is fired its flywheel turns
(once every `turn_ticks`), its crank turns, its connecting rod and crosshead drive it, its governor spins, its fire
glows through its fire door and smoke rises from its chimney (client/SteamTractionEngineRenderer). When the coal is
gone it runs down and stops. The Hay Wagon (agriculture/HayWagonBlock, three across, two up and four deep): a four-wheeled
farm wagon 4 m long under a load of hay 2 m high, roped down, with hay ladders at either end and a pitchfork in the
hay; placed behind the engine, its drawbar reaches the engine's hook.
Sizes in pixels of each one's frame, facing north.
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

# The Steam Traction Engine: its frame (48 across, 48 up, 80 deep); a coal's or charcoal's burn (a furnace's, ticks) and
# the most it holds; the flywheel's turn (ticks; 150 turns a minute); and, in pixels of its frame: the crankshaft's
# middle (across, height, depth) the flywheel, crank and connecting rod turn about, the crank's throw, the connecting
# rod's length (crank pin to crosshead pin), the flywheel's sides across and its radius, the governor's spindle (across,
# depth), and the chimney's mouth, where the smoke rises.
ENGINE = {"block": "steam_traction_engine", "display": "Steam Traction Engine", "size": (3, 3, 5), "burn": 1600, "max_fuel": 6400,
          "turn_ticks": 8, "crank": (28.0, 34.0, 45.0), "throw": 2.5, "rod": 15.0, "flywheel": (38.0, 40.0, 10.5),
          "governor": (20.0, 28.0), "chimney": (24.0, 47.0, 7.0)}
# The Hay Wagon: its frame (48 across, 32 up, 64 deep).
WAGON = {"block": "hay_wagon", "display": "Hay Wagon", "size": (3, 2, 4)}

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
    # The engine: an iron boiler (a blast furnace's) on iron wheels, a piston for its cylinder and a clock's gearing.
    {"id": ENGINE["block"], "pattern": ["I I", "BPC", "IGI"],
     "key": {"I": "minecraft:iron_block", "B": "minecraft:blast_furnace", "P": "minecraft:piston", "C": "minecraft:clock",
             "G": "minecraft:green_dye"},
     "result": ENGINE["block"], "count": 1, "category": "building"},
    # The wagon: planks and sticks on iron-shod wheels, under a load of hay.
    {"id": WAGON["block"], "pattern": ["HHH", "PSP", "I I"],
     "key": {"H": "minecraft:hay_block", "P": "#minecraft:planks", "S": "minecraft:stick", "I": "minecraft:iron_ingot"},
     "result": WAGON["block"], "count": 1, "category": "building"},
]


def blocks():
    return [CORNUCOPIA["block"]] + [spec["block"] for spec in SHOW.values()] + [RIBBON_BOARD["block"], ENGINE["block"], WAGON["block"]]


def items():
    return blocks()

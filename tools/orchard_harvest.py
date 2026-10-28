"""Orchard & Apple Harvest, decor set 2 (docs/features/orchard-apple-harvest.md): the gear of an apple harvest, to stand
beside the Cider Press (tools/agriculture.py CIDER). Part 1: the Orchard Ladder, the Apple Bin and the Picking Bag. Part
2: apple crates, windfall apples and the Orchard Handcart. The numbers here are what agriculture/OrchardLadderBlock,
AppleBinBlock, PickingBagBlock, FruitStoreBlockEntity, AppleCrateBlock, WindfallApplesBlock and OrchardHandcartBlock use;
tools/check_mod_data.py compares them. tools/orchard_harvest_data.py draws them and writes their files.

Each is real-life sized (docs/ART_DIRECTION.md). A prop of several blocks (agriculture/MultiDecorationBlock) is `size`
(across, up, deep) blocks, modelled whole in its frame: pixels from 0 to 16 x size, facing north, x across to the
placer's left from its far block, y up, z away from them.
"""

# The fruit the Apple Bin and the Picking Bag hold: the Cider Press's (item tag jugcraft:cider_apples: apples and pears),
# so a bin of picked fruit feeds the press by hand.
FRUIT_TAG = "jugcraft:cider_apples"

# The Orchard Ladder: a three-legged orchard ladder about nine feet tall (45 pixels, 2.8 m). Two rails tapering from
# `base_width` pixels apart at the foot to `top_width` at the top, flat steps every `step` pixels, a cap at the top, and
# a single pole hinged at the cap that leans back `pole_lean` degrees to the ground behind (block models turn boxes only
# by 22.5 or 45 degrees) from its `hinge` (y, z). The rails stand towards the back of the front blocks (`face_z`, pixels
# from the front), so a player against them is inside the ladder's blocks, which are climbable (tag minecraft:climbable),
# as a vanilla ladder's are. One block across, three up, two deep: the pole runs back into the blocks behind.
LADDER = {"block": "orchard_ladder", "display": "Orchard Ladder", "size": (1, 3, 2), "height": 45.0, "base_width": 14.0,
          "top_width": 6.0, "step": 5.0, "face_z": (9.5, 11.5), "pole_lean": 22.5, "hinge": (44.5, 12.75)}

# The Apple Bin: a bulk bin of the kind orchards fill in the field, 1.2 m square and 0.76 m deep (19 by 12 pixels) on a
# pallet base, its sides horizontal slats with gaps between. It holds up to `capacity` fruit (four stacks); the heap
# inside rises through `stages` as it fills, showing whichever fruit went in last (apples red, pears gold-green).
# Two blocks across and two deep, one up.
BIN = {"block": "apple_bin", "display": "Apple Bin", "size": (2, 1, 2), "capacity": 256, "stages": 4,
       "box": (6.5, 0.0, 6.5, 25.5, 14.0, 25.5)}

# The Picking Bag: a canvas picking bag hung by its harness on an iron wall hook, its stiff hoop mouth open at the top. It
# holds up to `capacity` fruit; it shows empty, some, or full (`stages`).
BAG = {"block": "picking_bag", "display": "Picking Bag", "capacity": 16, "stages": 2}

# Use one of the bin's or bag's fruit on it to put in all you hold that fits; use it with an empty hand to take back a
# stack (or what is left) of whichever fruit went in last. Breaking it spills what it held.
FRUITS = ("apple", "pear")

RECIPES = [
    {"id": "orchard_ladder", "pattern": ["S S", "SPS", "S S"], "key": {"S": "minecraft:stick", "P": "#minecraft:planks"},
     "result": "orchard_ladder", "count": 1, "category": "building"},
    {"id": "apple_bin", "pattern": ["S S", "S S", "PPP"], "key": {"S": "#minecraft:wooden_slabs", "P": "#minecraft:planks"},
     "result": "apple_bin", "count": 1, "category": "building"},
    {"id": "picking_bag", "pattern": ["SIS", "W W", " W "], "key": {"S": "minecraft:string", "I": "minecraft:iron_nugget",
                                                                    "W": "#minecraft:wool"},
     "result": "picking_bag", "count": 1, "category": "building"},
]


# Part 2. Apple crates: old wooden field crates of apples, 50 by 37 cm and 30 cm deep (8 by 6 by 4.75 pixels), up to
# `max` stacked in a block (use another crate of the same kind on it to stack it). Each kind holds a variety, its apples
# heaped flush with the rim and its name on a paper label on each end board; the dye in its recipe picks it.
CRATES = {"red": {"block": "red_apple_crate", "display": "Red Apple Crate", "dye": "minecraft:red_dye"},
          "green": {"block": "green_apple_crate", "display": "Green Apple Crate", "dye": "minecraft:lime_dye"},
          "golden": {"block": "golden_apple_crate", "display": "Golden Apple Crate", "dye": "minecraft:yellow_dye"}}
CRATE = {"max": 3, "box": (4.0, 5.0, 12.0, 11.0), "height": 4.75}
# Windfall apples: one to `max` apples fallen on the ground (use another to add one), at fixed places a block, the block
# placed at one of four turns at random. Two apples make two windfall apples; broken, they give back their apples, one
# for each (no recipe turns them back, so no recipe loop).
WINDFALL = {"block": "windfall_apples", "display": "Windfall Apples", "max": 4,
            "spots": [(5.0, 5.0), (10.0, 9.0), (5.5, 11.0), (11.0, 3.5)], "size": 2.0}
# The Orchard Handcart: a two-wheeled wooden handcart, its box 75 cm by 1 m on spoked wheels 62 cm across, two handles
# forward and a rest under them. One block across, one up, two deep (the handles towards the placer). It holds up to
# `capacity` fruit (two stacks) as the bin does, the load rising through `stages`.
CART = {"block": "orchard_handcart", "display": "Orchard Handcart", "size": (1, 1, 2), "capacity": 128, "stages": 4,
        "box": (2.0, 6.5, 14.0, 14.0, 11.5, 30.0), "wheel": {"r": 5.0, "axle": (5.25, 22.0)}}

RECIPES += [
    {"id": spec["block"], "pattern": ["ADA", "SPS"], "key": {"A": "minecraft:apple", "D": spec["dye"], "S": "minecraft:stick",
                                                              "P": "#minecraft:planks"},
     "result": spec["block"], "count": 1, "category": "building"} for spec in CRATES.values()
] + [
    {"id": "orchard_handcart", "pattern": ["S  ", "PPP", "W W"], "key": {"S": "minecraft:stick", "P": "#minecraft:planks",
                                                                        "W": "minecraft:iron_ingot"},
     "result": "orchard_handcart", "count": 1, "category": "building"},
]
SHAPELESS = [
    {"id": "windfall_apples", "inputs": ["minecraft:apple", "minecraft:apple"], "result": "windfall_apples", "count": 2,
     "category": "building"},
]


def blocks():
    return [LADDER["block"], BIN["block"], BAG["block"]] + part2_blocks()


def part2_blocks():
    return [spec["block"] for spec in CRATES.values()] + [WINDFALL["block"], CART["block"]]

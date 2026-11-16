"""Thanksgiving harvest town details (docs/features/thanksgiving-town-details.md): the pieces of the owner's Thanksgiving
batch for a town dressed for the harvest that Jugcraft did not already have, after a real inventory of what it has
(the feature record lists every candidate and what covers it). Models and textures: tools/thanksgiving_town_data.py,
built from boxes at one texel to a model pixel in the Thanksgiving decorations' manner (tools/thanksgiving_decor_data.py),
whose willow, produce, leaf, china, linen and copper painters and palettes they share.

Every piece is a player-placed decoration, craftable from the start and kept all year: nothing here asks for research,
follows the calendar, decorates a town by itself or changes a town template. Each is registered by
JugcraftAgriculture.registerThanksgivingTown() (the Java must match KINDS and the numbers below:
tools/thanksgiving_town_check.py, run by tools/check_mod_data.py):
- "floor": a FacingDecorationBlock standing on the floor or a table, facing whoever places it, its outline `shape`
  ({x0, y0, z0, x1, y1, z1} in pixels, facing north);
- "candles": a CandleCenterpieceBlock (the Pumpkin Candle Centerpiece's): flint and steel or a fire charge lights its
  burner and an empty hand snuffs it (CandleLighting), giving `light` while lit;
- "hanging": a HarvestTownHangingBlock, hung from the underside of a block as the Acorn and Leaf Mobile is
  (HangingGhostBlock) and dropping when that goes, its outline `shape` turned with the way it faces;
- "arch": the Timber Harvest Arch, a HarvestTownArchBlock (MultiDecorationBlock) of ARCH's cells.
Every recipe follows the agriculture feature switch.

The autumn window box is no new block: the existing Window Box (and the other planters, PlanterBlock) already takes the
fall's mums, asters, sedum and ornamental kale (#minecraft:small_flowers), and now also the small harvest pieces of
PLANTER_HARVEST (a Warty Gourd), through the same mechanism: planted one to a spot, taken back with an empty hand.
"""

FEATURE = "agriculture"

# Each piece: its kind, name, outline and how it sounds, breaks and shows on maps. Shapes are in pixels facing north
# (the piece's front to the north, towards whoever placed it); strength is the block's hardness.
PROPS = {
    "timber_harvest_arch": {"kind": "arch", "display": "Timber Harvest Arch", "color": "WOOD", "sound": "WOOD", "strength": 2.0},
    "hanging_crock_rack": {"kind": "hanging", "display": "Hanging Crock and Herb Rack",
                           "shape": [1.0, 0.5, 1.5, 15.0, 16.0, 14.5], "color": "TERRACOTTA_ORANGE", "sound": "WOOD", "strength": 0.5},
    "folded_linen_stack": {"kind": "floor", "display": "Folded Linen Stack",
                           "shape": [1.5, 0.0, 3.0, 15.0, 8.0, 11.5], "color": "TERRACOTTA_WHITE", "sound": "WOOL", "strength": 0.3},
    "copper_chafing_dish": {"kind": "candles", "display": "Copper Chafing Dish", "light": 7,
                            "shape": [1.5, 0.0, 4.0, 14.5, 13.0, 12.0], "color": "COLOR_ORANGE", "sound": "COPPER", "strength": 0.8},
    "pumpkin_carving_bench": {"kind": "floor", "display": "Pumpkin Carving Bench",
                              "shape": [0.5, 0.0, 1.0, 15.5, 13.5, 13.5], "color": "WOOD", "sound": "WOOD", "strength": 0.8},
}
# What breaks each quickest (block tag minecraft:mineable/...): the copper dish a pickaxe, the rest (wood, cloth,
# earthenware on a wooden rack) an axe. None needs the tool to drop.
PICKAXE = ("copper_chafing_dish",)
KINDS = {"floor": "FacingDecorationBlock", "candles": "CandleCenterpieceBlock", "hanging": "HarvestTownHangingBlock",
         "arch": "HarvestTownArchBlock"}

# The Timber Harvest Arch: `width` blocks across and `height` up, one deep. Its cells are {blocks to the right of the
# player who placed it, blocks up} (MultiDecorationBlock): the left post from the bottom up, the beam from left to right,
# then the right post from the top down; the opening between the posts (`opening` blocks across and `clear` up) has no
# block at all, so nothing in it can be bumped into. In pixels in its frame (80 by 64 by 16, facing north): `shapes` are
# its solid boxes, cut into each part's outline by the Java (MultiDecorationBlock.shapesOf).
ARCH = {
    "block": "timber_harvest_arch", "width": 5, "height": 4, "opening": 3, "clear": 3,
    "cells": [(0, 0), (0, 1), (0, 2), (0, 3), (1, 3), (2, 3), (3, 3), (4, 3), (4, 2), (4, 1), (4, 0)],
    "shapes": [
        [0.5, 0.0, 0.0, 12.0, 4.5, 12.0],     # the left plinth, its corn and pumpkin
        [0.5, 4.5, 3.0, 11.0, 50.0, 11.0],    # the left post and its corn
        [68.0, 0.0, 0.0, 79.5, 4.5, 12.0],    # the right plinth, its corn and pumpkin
        [69.0, 4.5, 3.0, 79.5, 50.0, 11.0],   # the right post and its corn
        [0.5, 50.0, 3.5, 79.5, 57.0, 12.0],   # the beam and its garland
        [31.0, 57.0, 5.0, 49.0, 64.0, 11.0],  # the sheaf and pumpkins on the beam
    ],
}

# Small harvest pieces a planter takes besides small flowers (item tag `tag`, PlanterBlock.HARVEST): block items whose
# own model, drawn at a planter's scale by client/PlanterRenderer, fits inside every planter (the Warty Gourd's 8-pixel
# body is 4 pixels in the Window Box, 7.2 in the Half-Barrel and 5.2 in the Galvanized Tub). Larger squash and pumpkins,
# whole blocks, would stand out of the Window Box and through its wall; bittersweet is a wall vine whose default state
# draws nothing; wheat and corn are no blocks. So they are left out.
PLANTER_HARVEST = {"tag": "jugcraft:planter_harvest", "items": ["jugcraft:warty_gourd"], "planters": ["window_box", "half_barrel_planter",
                                                                                                      "galvanized_tub_planter"]}

STICK, WHEAT = "minecraft:stick", "minecraft:wheat"
SHAPELESS = [
    # The arch: four logs for its posts and beam, the corn and pumpkin dressing it, leaves and string for its garland.
    {"id": "timber_harvest_arch", "inputs": ["#minecraft:logs", "#minecraft:logs", "#minecraft:logs", "#minecraft:logs", "jugcraft:corn",
                                             "jugcraft:corn", "minecraft:pumpkin", "#minecraft:leaves", "minecraft:string"]},
    # The rack: a slab and sticks for its frame, string to hang it, clay for its jug and crock, an onion and dried herbs.
    {"id": "hanging_crock_rack", "inputs": ["#minecraft:wooden_slabs", STICK, STICK, "minecraft:string", "minecraft:clay_ball",
                                            "minecraft:clay_ball", "jugcraft:onion", "jugcraft:dried_herbs"]},
    # Linen is flax: three of it, dyed cloths, twine and a wheat sprig.
    {"id": "folded_linen_stack", "inputs": ["jugcraft:flax", "jugcraft:flax", "jugcraft:flax", "minecraft:red_dye", "minecraft:orange_dye",
                                            "minecraft:string", WHEAT]},
    # Copper for the dish and lid, iron for its stand, a candle for its burner.
    {"id": "copper_chafing_dish", "inputs": ["minecraft:copper_ingot", "minecraft:copper_ingot", "minecraft:copper_ingot",
                                             "minecraft:iron_nugget", "minecraft:iron_nugget", "minecraft:candle"]},
    # Slabs and sticks for the bench, the pumpkin being carved, iron for the knife and scoop, a bowl for the seeds.
    {"id": "pumpkin_carving_bench", "inputs": ["#minecraft:wooden_slabs", "#minecraft:wooden_slabs", STICK, STICK, "minecraft:pumpkin",
                                               "minecraft:iron_nugget", "minecraft:bowl"]},
]
for _recipe in SHAPELESS:
    _recipe.setdefault("result", _recipe["id"])
    _recipe.setdefault("count", 1)
    _recipe.setdefault("category", "building")
SHAPED = []


def blocks():
    return list(PROPS)


def items():
    return blocks()


def names():
    return {f"block.jugcraft.{block}": spec["display"] for block, spec in PROPS.items()}


def java_box(values):
    """A shape as JugcraftAgriculture writes it: new double[] {x0, y0, z0, x1, y1, z1}."""
    return "new double[] {" + ", ".join(f"{float(v)}" for v in values) + "}"

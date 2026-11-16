"""Thanksgiving festival toys and lively props (group D of the Thanksgiving package; docs/features/thanksgiving-toys.md).

The owner's candidate list for this group, checked against what Jugcraft already has (the record keeps the inventory
table: every candidate, the existing IDs searched and why each is new). Eight new pieces, each a decoration first:
- Corn Husk Dolls: a pair of faceless dolls of dried husk on a coiled husk mat (the Corn Dolly is the woven wheat
  hanging; these stand on a table or a shelf);
- Turkey Pull Toy: a painted wooden turkey on a four-wheeled board with its pull string (the Turkey Statuette is a
  carved figure on a log slice, with no wheels);
- Spinning Top: a turned and painted top that rests on its tip, spins when used and winds down;
- Mayflower Model Ship: a model of a three-masted merchant ship of 1620 on its display stand, laid corner to corner;
- Harvest Photo Frame Backdrop: a broad timber picture frame dressed for the harvest, four blocks across and three up,
  whose open middle two people stand in for a picture;
- Tabletop Corn Toss Set: a small slanted board with a hole, eight beanbags in two colours on their racks, for two
  players to toss from a few blocks off (no wagers, prizes or rewards: only the score, and a reset);
- Prize Pumpkin Display: a show pumpkin on a draped plinth with a pumpkin-coloured rosette and its entry card (the
  Prize Ribbon Board shows the earned blue, red and white placings; the Giant Vegetable Show has no pumpkin entry);
- Harvest Percussion Set: a frame drum, a washboard and a gourd shaker on a bench, each played by using it (vanilla's
  note-block drum, hat and snare sounds, with a short cooldown).

Models and textures: tools/thanksgiving_toys_data.py, built from boxes at one texel to a model pixel in the Thanksgiving
decorations' manner (tools/thanksgiving_decor_data.py), sharing its pumpkin, leaf and willow painters and the Harvest
Festival Grounds' and Pumpkin Patch Market's palettes. Java: io.github.jimbozoomer.jugcraft.agriculture.FestivalToys
registers them (the classes are named FestivalToy...); tools/thanksgiving_toys_check.py checks that the Java, the
numbers here and the generated files agree.

Nothing here asks for research or follows the calendar; every recipe follows the agriculture feature switch.
"""

FEATURE = "agriculture"

# Each piece: its kind (what Java block it is), name, outline (pixels, facing north: its front to the north, towards
# whoever placed it), map colour, sound and hardness, and the tool that breaks it quickest. "class" is its Java block.
PROPS = {
    "corn_husk_dolls": {"kind": "floor", "display": "Corn Husk Dolls", "class": "FacingDecorationBlock",
                        "shape": [1.0, 0.0, 3.0, 15.0, 14.0, 13.5], "color": "SAND", "sound": "GRASS", "strength": 0.3},
    "turkey_pull_toy": {"kind": "floor", "display": "Turkey Pull Toy", "class": "FacingDecorationBlock",
                        "shape": [2.0, 0.0, 0.0, 14.0, 16.0, 14.5], "color": "TERRACOTTA_ORANGE", "sound": "WOOD", "strength": 0.6},
    "spinning_top": {"kind": "top", "display": "Spinning Top", "class": "FestivalToySpinningTopBlock",
                     "shape": [2.5, 0.0, 3.5, 13.0, 11.0, 12.5], "color": "COLOR_RED", "sound": "WOOD", "strength": 0.4},
    "mayflower_model_ship": {"kind": "floor", "display": "Mayflower Model Ship", "class": "FacingDecorationBlock",
                             "shape": [0.5, 0.0, 0.5, 15.5, 16.0, 15.5], "color": "WOOD", "sound": "WOOD", "strength": 0.6},
    "harvest_photo_backdrop": {"kind": "backdrop", "display": "Harvest Photo Frame Backdrop", "class": "FestivalToyBackdropBlock",
                               "color": "COLOR_YELLOW", "sound": "WOOD", "strength": 1.5},
    "corn_toss_set": {"kind": "corn_toss", "display": "Tabletop Corn Toss Set", "class": "FestivalToyCornTossBlock",
                      "shape": [0.5, 0.0, 2.5, 15.5, 7.5, 14.5], "color": "WOOD", "sound": "WOOD", "strength": 0.6},
    "prize_pumpkin_display": {"kind": "floor", "display": "Prize Pumpkin Display", "class": "FacingDecorationBlock",
                              "shape": [1.0, 0.0, 1.0, 15.0, 14.5, 14.5], "color": "COLOR_ORANGE", "sound": "WOOD", "strength": 0.8},
    "harvest_percussion_set": {"kind": "percussion", "display": "Harvest Percussion Set", "class": "FestivalToyPercussionBlock",
                               "shape": [0.5, 0.0, 0.5, 15.5, 14.0, 14.0], "color": "WOOD", "sound": "WOOD", "strength": 0.6},
}
# Everything is wood, husk, cloth or a pumpkin: an axe breaks each quickest; none needs a tool to drop.
AXE = tuple(PROPS)
# The pieces that do something when used, and the hint each item shows (item.jugcraft.<id>.tooltip).
HINTS = {
    "spinning_top": "Use it to give it a spin",
    "harvest_photo_backdrop": "Stand in the frame for a picture; use it to switch its lights",
    "corn_toss_set": "Toss a bag from 2 to 5 blocks in front; sneak-use to gather the bags",
    "harvest_percussion_set": "Use the drum, the washboard or the gourd to play it",
}

# ---------------------------------------------------------------- the Spinning Top
# A spin is SPIN steps, counted down by scheduled ticks: the top stands and turns a sixteenth of a turn (a 22.5 degree
# frame of its model) a step while SPIN >= WOBBLE + 1, then for the last WOBBLE steps leans and precesses a quarter turn a
# step, and at 0 it stands at rest on its tip again. DELAYS[n] is how many ticks step n lasts (n counts down from SPIN to 1), so it
# winds down; a spin always ends, SPIN steps after it began. Using it while it spins does nothing.
TOP = {"spin": 32, "wobble": 8, "frames": 4}


def top_delay(step):
    """The ticks the top waits at step `step` (SPIN down to 1) before the next: quick while it is fast, then slower."""
    if step > 20:
        return 1
    if step > 12:
        return 2
    if step > TOP["wobble"]:
        return 3
    return (8, 8, 6, 6, 5, 5, 4, 4)[step - 1]


TOP_TICKS = sum(top_delay(n) for n in range(1, TOP["spin"] + 1))

# ---------------------------------------------------------------- the Harvest Photo Frame Backdrop
# Four blocks across and three up, one deep, placed from the block aimed at to the right and up (MultiDecorationBlock).
# CELLS are {right, up} in part order: the left post from the bottom up, the top rail, the right post from the top down,
# then the low sill across the bottom of the opening. The two middle blocks of the middle row are not part of it: that
# is the open middle, two blocks wide and two high above the sill, where players stand. LIGHT is the string of bulbs'
# light, on the two middle blocks of the top rail (BULB_PARTS), while lit.
BACKDROP = {"across": 4, "up": 3, "cells": [[0, 0], [0, 1], [0, 2], [1, 2], [2, 2], [3, 2], [3, 1], [3, 0], [1, 0], [2, 0]],
            "open": [[1, 1], [2, 1]], "bulb_parts": [3, 4], "light": 10, "sill_height": 2.0,
            # Its outline and collision, boxes in its frame (pixels, facing north, x across to the placer's left from its
            # far block): the two posts with their feet, the top rail, the sill, and the low harvest at each foot.
            "boxes": [[8.5, 0.0, 2.0, 16.0, 48.0, 14.5], [48.0, 0.0, 2.0, 55.5, 48.0, 14.5], [8.5, 40.0, 4.0, 55.5, 48.0, 10.5],
                      [16.0, 0.0, 5.0, 48.0, 2.0, 9.5], [0.5, 0.0, 1.5, 8.5, 9.5, 13.0], [55.5, 0.0, 2.5, 63.5, 11.5, 13.5]]}

# ---------------------------------------------------------------- the Tabletop Corn Toss Set
# Two sides, rust and cream, four bags each, tossed in turn (rust first). A toss is a use from in front of the set
# (within FRONT_COS of its facing), at least MIN_DISTANCE and at most MAX_DISTANCE blocks off (measured flat from the
# player's eyes to the board's middle); the bag lands where the player's look crosses AIM_HEIGHT (pixels up: the hole's
# height on the slanted board), scattered by SCATTER blocks for each block of distance. HOLE and BOARD are the hole's
# middle and the board's extent seen from above (pixels, facing north: the slanted board's plan, as
# thanksgiving_toys_data.projected works it out). In the hole (within HOLE_R pixels of its middle) scores 3, on the
# board 1, off it 0. One toss each COOLDOWN ticks. After BAGS x 2 tosses the round is over until someone sneaks and
# uses the set, which gathers the bags and clears the score. No wager, prize, reward or item ever changes hands.
CORN_TOSS = {"bags": 4, "min_distance": 2.0, "max_distance": 5.0, "front_cos": 0.5, "scatter": 0.04, "cooldown": 10,
             "hole": [8.0, 10.5], "hole_r": 1.5, "board": [3.5, 2.75, 12.5, 13.25], "aim_height": 5.7,
             "points": {"hole": 3, "board": 1, "miss": 0}, "hear": 8.0}

# ---------------------------------------------------------------- the Harvest Percussion Set
# Which instrument a use plays is where it was aimed, in the set's own frame (pixels, facing north): the gourd shaker
# lying in front (below SHAKER_TOP and before SHAKER_Z), else the frame drum to the west (x below SPLIT_X), else the
# washboard. Each plays a vanilla note-block sound (no new sound file), shows that instrument struck for COOLDOWN ticks,
# and cannot be played again until then.
PERCUSSION = {"split_x": 8.0, "shaker_top": 4.0, "shaker_z": 4.5, "cooldown": 4,
              "instruments": {"drum": "NOTE_BLOCK_BASEDRUM", "washboard": "NOTE_BLOCK_HAT", "shaker": "NOTE_BLOCK_SNARE"}}

STICK = "minecraft:stick"
SHAPELESS = [
    {"id": "corn_husk_dolls", "inputs": ["jugcraft:corn", "jugcraft:corn", "minecraft:string", "minecraft:orange_dye"]},
    {"id": "turkey_pull_toy", "inputs": ["#minecraft:logs", "#minecraft:wooden_slabs", STICK, "minecraft:string", "minecraft:feather",
                                         "minecraft:red_dye"]},
    {"id": "spinning_top", "inputs": ["#minecraft:wooden_slabs", STICK, "minecraft:red_dye", "minecraft:yellow_dye"]},
    {"id": "mayflower_model_ship", "inputs": ["#minecraft:planks", "#minecraft:wooden_slabs", STICK, STICK, "minecraft:paper",
                                              "minecraft:string"]},
    {"id": "harvest_photo_backdrop", "inputs": ["#minecraft:planks", "#minecraft:planks", "#minecraft:planks", "minecraft:gold_nugget",
                                                "minecraft:hay_block", "minecraft:pumpkin", "jugcraft:corn", "#minecraft:leaves",
                                                "minecraft:torch"]},
    {"id": "corn_toss_set", "inputs": ["#minecraft:wooden_slabs", "#minecraft:wooden_slabs", STICK, "jugcraft:corn", "jugcraft:corn",
                                       "minecraft:red_dye", "minecraft:string"]},
    {"id": "prize_pumpkin_display", "inputs": ["minecraft:pumpkin", "#minecraft:wooden_slabs", "jugcraft:flax", "minecraft:orange_dye",
                                               "minecraft:gold_nugget", "minecraft:paper"]},
    {"id": "harvest_percussion_set", "inputs": ["#minecraft:wooden_slabs", "minecraft:leather", "minecraft:iron_nugget",
                                                "minecraft:iron_nugget", "jugcraft:warty_gourd", STICK]},
]
for _recipe in SHAPELESS:
    _recipe.setdefault("result", _recipe["id"])
    _recipe.setdefault("count", 1)
    _recipe.setdefault("category", "building")
SHAPED = []

# Words the interactive pieces say (message.jugcraft.<key>).
MESSAGES = {
    "corn_toss.rust": "Rust",
    "corn_toss.cream": "Cream",
    "corn_toss.hole": "%s: in the hole! +3 (Rust %s, Cream %s)",
    "corn_toss.board": "%s: on the board, +1 (Rust %s, Cream %s)",
    "corn_toss.miss": "%s: missed the board (Rust %s, Cream %s)",
    "corn_toss.too_close": "Step back: toss from at least 2 blocks away",
    "corn_toss.too_far": "Too far: toss from no more than 5 blocks away",
    "corn_toss.in_front": "Toss from in front of the board",
    "corn_toss.round_over": "Round over: Rust %s, Cream %s. Sneak and use the set to gather the bags",
    "corn_toss.final": "All bags tossed: Rust %s, Cream %s",
    "corn_toss.gathered": "The bags are gathered for a new round",
}


def blocks():
    return list(PROPS)


def items():
    return blocks()


def names():
    out = {f"block.jugcraft.{block}": spec["display"] for block, spec in PROPS.items()}
    out.update({f"item.jugcraft.{block}.tooltip": hint for block, hint in HINTS.items()})
    out.update({f"message.jugcraft.{key}": text for key, text in MESSAGES.items()})
    return out


def java_box(values):
    """A shape as FestivalToys writes it: new double[] {x0, y0, z0, x1, y1, z1}."""
    return "new double[] {" + ", ".join(f"{float(v)}" for v in values) + "}"

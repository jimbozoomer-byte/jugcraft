"""The graveyard pack (docs/features/graveyard.md): memorials carved from four stones, life-sized and finely modelled,
that weather with the years and carry an epitaph cut with the Stonemason's Chisel.

Pack 1, headstones (agriculture/HeadstoneBlock.java, HeadstoneBlockEntity.java, Epitaphs.java). Each headstone is one
to three blocks (`cells`: right, up, back from the block placed, part 0 holding the epitaph) facing whoever placed it.
`shapes`: per part, the boxes it can be hit and walked into by (pixels x0, y0, z0, x1, y1, z1, facing north, in that
part's block). `text`: where the epitaph is cut, facing north: FRONT on a face whose plane is z = `z` pixels, or TOP on a
top face at height `y`, centred at (`x`, `y`) or (`x`, `z`) (z may run into the next block back), `width` by `height`
pixels; each line is drawn as large as fits, up to `max_scale` blocks per font pixel.

Pack 3, buildings (agriculture/GraveyardBuildingBlock.java): BUILDINGS below, each many blocks, with the layout Java
reads generated into /jugcraft/graveyard_buildings.json.

Models: tools/graveyard_models.py and graveyard_buildings.py (geometry), graveyard_data.py (one model per part and
stage). Textures: tools/graveyard_textures.py. Everything follows the agriculture feature switch for its recipes.
"""

FEATURE = "agriculture"

# Weathering (HeadstoneBlock): each random tick a headstone that is not waxed takes the next stage with `age_chance`
# (twice that with open sky above it). A Brush scrubs one stage off; Honeycomb waxes it (no more weathering, as
# copper), an axe scrapes the wax off; Bone Meal ages it one stage at once (builders). The epitaph's letters fade
# into the stone with each stage by `ink_fade`. Neglect wakes the dead: at night a headstone stirs a restless spirit
# with the graves' chance (agriculture.GHOSTS stir_chance) times `stir` for its stage.
STAGES = ("clean", "worn", "mossy", "overgrown")
WEATHERING = {"age_chance": 0.02, "sky_factor": 2, "ink_fade": [0.0, 0.18, 0.36, 0.52], "stir": [0.25, 0.5, 1.0, 1.5]}

# Epitaphs (Epitaphs.java): the Stonemason's Chisel opens the epitaph screen on a headstone (needs build rights, like a
# sign); at most `lines` lines of `line_length` characters come back, checked on the server within `session_ticks` of
# opening, still holding the chisel and within reach. A named Name Tag cuts its name as the first line.
EPITAPH = {"lines": 4, "line_length": 24, "session_ticks": 6000, "chisel": "stonemasons_chisel", "chisel_display": "Stonemason's Chisel",
           "component": "epitaph"}

# Stone: ink colour of its letters (ARGB) and the colour they fade towards as it weathers.
STONES = {"marble": {"ink": 0xFF2C2B30, "fade_to": 0xFFB9B7B0, "vanilla": "minecraft:calcite"},
          "slate": {"ink": 0xFFD3D8DE, "fade_to": 0xFF4B525C, "vanilla": "minecraft:polished_deepslate"},
          "granite": {"ink": 0xFFE6DFD6, "fade_to": 0xFF7A7270, "vanilla": "minecraft:polished_granite"},
          "sandstone": {"ink": 0xFF3E2C1A, "fade_to": 0xFFB59C6E, "vanilla": "minecraft:smooth_sandstone"},
          # Pack 2: a mortsafe's cast-iron plate, its raised letters picked out in gilt.
          "iron": {"ink": 0xFFC9A961, "fade_to": 0xFF6E3A1E, "vanilla": "minecraft:iron_bars"}}

SINGLE = [(0, 0, 0)]
TALL2 = [(0, 0, 0), (0, 1, 0)]
TALL3 = [(0, 0, 0), (0, 1, 0), (0, 2, 0)]
LONG = [(0, 0, 0), (0, 0, 1)]
TALL4 = [(0, 0, 0), (0, 1, 0), (0, 2, 0), (0, 3, 0)]
# The Angel at the Tomb: the altar's two halves and, above the left one (the placer's right), her wings.
WIDE = [(0, 0, 0), (1, 0, 0), (1, 1, 0)]

HEADSTONES = {
    "gothic_headstone": {
        "display": "Gothic Headstone", "stone": "marble", "model": "gothic", "cells": SINGLE, "overgrowth": "small",
        "shapes": [[(1.5, 0, 4.5, 14.5, 2, 11.5), (2.5, 2, 6, 13.5, 16, 9.5), (4.5, 16, 6.5, 11.5, 20, 9.5)]],
        "text": {"face": "FRONT", "x": 8.0, "y": 8.0, "z": 6.5, "width": 6.8, "height": 7.6, "max_scale": 1 / 64},
        "recipe": {"stonecutting": "minecraft:calcite"}},
    "willow_urn_headstone": {
        "display": "Slate Headstone with Willow and Urn", "stone": "slate", "model": "willow_urn", "cells": SINGLE, "overgrowth": "small",
        "shapes": [[(1.5, 0, 7, 14.5, 14, 9), (4.5, 14, 7, 11.5, 17.5, 9)]],
        "text": {"face": "FRONT", "x": 8.0, "y": 6.6, "z": 7.25, "width": 9.2, "height": 9.4, "max_scale": 1 / 64},
        "recipe": {"stonecutting": "minecraft:polished_deepslate"}},
    "winged_skull_headstone": {
        "display": "Slate Headstone with Winged Skull", "stone": "slate", "model": "winged_skull", "cells": SINGLE, "overgrowth": "small",
        "shapes": [[(1.5, 0, 7, 14.5, 14, 9), (4.5, 14, 7, 11.5, 17.5, 9)]],
        "text": {"face": "FRONT", "x": 8.0, "y": 6.6, "z": 7.25, "width": 9.2, "height": 9.4, "max_scale": 1 / 64},
        "recipe": {"stonecutting": "minecraft:polished_deepslate"}},
    "lamb_headstone": {
        "display": "Lamb Headstone", "stone": "marble", "model": "lamb", "cells": SINGLE, "overgrowth": "small",
        "shapes": [[(3, 0, 5, 13, 2, 11), (3.7, 2, 6.2, 12.3, 9.5, 9.8), (4, 9.5, 6.3, 12.2, 13, 9.2)]],
        "text": {"face": "FRONT", "x": 8.0, "y": 5.6, "z": 6.5, "width": 5.8, "height": 5.0, "max_scale": 1 / 80},
        "recipe": {"stonecutting": "minecraft:calcite"}},
    "broken_column": {
        "display": "Broken Column", "stone": "marble", "model": "broken_column", "cells": TALL2, "overgrowth": "tall",
        "shapes": [[(2.5, 0, 2.5, 13.5, 3, 13.5), (3.5, 3, 3.5, 12.5, 12.4, 12.5), (4.6, 12.4, 4.6, 11.4, 16, 11.4)],
                   [(4.5, 0, 4.5, 11.5, 12.6, 11.5)]],
        "text": {"face": "FRONT", "x": 8.0, "y": 7.1, "z": 3.5, "width": 6.4, "height": 5.0, "max_scale": 1 / 80},
        "recipe": {"pattern": ["C", "C", "C"], "key": {"C": "minecraft:calcite"}}},
    "celtic_cross": {
        "display": "Celtic High Cross", "stone": "granite", "model": "celtic_cross", "cells": TALL3, "overgrowth": "tall",
        "shapes": [[(1, 0, 3, 15, 1.5, 13), (2, 1.5, 4, 14, 6.5, 12), (2.5, 6.5, 4.5, 13.5, 7.5, 11.5), (5, 7.5, 5.5, 11, 16, 10.5)],
                   [(5, 0, 5.5, 11, 16, 10.5), (0, 14, 5.5, 16, 16, 10.5)],
                   [(0, 0, 5.5, 16, 4, 10.5), (1.5, 0, 6, 14.5, 9, 10), (5, 0, 5.5, 11, 12, 10.5)]],
        "text": {"face": "FRONT", "x": 8.0, "y": 4.0, "z": 4.0, "width": 10.4, "height": 4.2, "max_scale": 1 / 96},
        "recipe": {"pattern": [" G ", "GGG", " G "], "key": {"G": "minecraft:polished_granite"}}},
    "rustic_scroll_headstone": {
        "display": "Rustic Scroll Headstone", "stone": "granite", "model": "scroll", "cells": SINGLE, "overgrowth": "small",
        "shapes": [[(1, 0, 4.2, 15, 10, 11.4), (3, 10, 5.4, 13, 15, 10.6)]],
        "text": {"face": "FRONT", "x": 8.0, "y": 7.0, "z": 4.6, "width": 8.0, "height": 6.4, "max_scale": 1 / 64},
        "recipe": {"stonecutting": "minecraft:granite"}},
    "table_tomb": {
        "display": "Table Tomb", "stone": "sandstone", "model": "table_tomb", "cells": LONG, "overgrowth": "slab",
        "shapes": [[(0.5, 0, 0.5, 15.5, 12.6, 16)], [(0.5, 0, 0, 15.5, 12.6, 15.5)]],
        "text": {"face": "TOP", "x": 8.0, "y": 12.6, "z": 16.0, "width": 11.0, "height": 24.0, "max_scale": 1 / 40},
        "recipe": {"pattern": ["SSS", "S S"], "key": {"S": "minecraft:smooth_sandstone"}}},
    "ledger_stone": {
        "display": "Ledger Stone", "stone": "sandstone", "model": "ledger", "cells": LONG, "overgrowth": "slab",
        "shapes": [[(1, 0, 1, 15, 2.4, 16)], [(1, 0, 0, 15, 2.4, 15)]],
        "text": {"face": "TOP", "x": 8.0, "y": 2.4, "z": 10.2, "width": 9.6, "height": 14.0, "max_scale": 1 / 48},
        "recipe": {"pattern": ["SS"], "key": {"S": "minecraft:smooth_sandstone_slab"}}},
    # ---------------------------------------------------------------- pack 2: monuments
    "grand_obelisk": {
        "display": "Grand Obelisk", "stone": "granite", "model": "obelisk", "cells": TALL4, "overgrowth": "tall", "pack": 2,
        "shapes": [[(0, 0, 0, 16, 2, 16), (1, 2, 1, 15, 4, 15), (2.5, 4, 2.5, 13.5, 16, 13.5)],
                   [(2.6, 0, 2.6, 13.4, 0.8, 13.4), (3.5, 0.8, 3.5, 12.5, 2.5, 12.5), (3.7, 2.5, 3.7, 12.3, 16, 12.3)],
                   [(4.4, 0, 4.4, 11.6, 16, 11.6)], [(5.2, 0, 5.2, 10.8, 12, 10.8)]],
        "text": {"face": "FRONT", "x": 8.0, "y": 10.0, "z": 2.2, "width": 8.2, "height": 7.2, "max_scale": 1 / 72},
        "recipe": {"pattern": [" G ", " G ", "GGG"], "key": {"G": "minecraft:polished_granite"}}},
    "draped_urn": {
        "display": "Draped Urn", "stone": "marble", "model": "draped_urn", "cells": TALL2, "overgrowth": "tall", "pack": 2,
        "shapes": [[(2, 0, 2, 14, 2.8, 14), (3, 2.8, 3, 13, 13, 13), (2.4, 13, 2.4, 13.6, 15, 13.6), (4, 15, 4, 12, 16, 12)],
                   [(4, 0, 4, 12, 12.2, 12)]],
        "text": {"face": "FRONT", "x": 8.0, "y": 8.0, "z": 3.0, "width": 7.6, "height": 6.6, "max_scale": 1 / 80},
        "recipe": {"pattern": ["C", "P", "C"], "key": {"C": "minecraft:calcite", "P": "minecraft:flower_pot"}}},
    "angel_at_the_tomb": {
        "display": "Angel at the Tomb", "stone": "marble", "model": "angel_of_grief", "cells": WIDE, "overgrowth": "small", "pack": 2,
        "shapes": [[(0, 0, 0, 16, 2.4, 16), (0, 2.4, 2.5, 14, 14, 14)],
                   [(0, 0, 0, 16, 2.4, 16), (10, 2.4, 2.5, 16, 14, 14), (1, 2.4, 4, 10, 16, 12)],
                   [(0, 0, 5, 9, 12, 11)]],
        "text": {"face": "FRONT", "x": 3.5, "y": 7.2, "z": 2.5, "width": 11.5, "height": 6.0, "max_scale": 1 / 64},
        "recipe": {"pattern": ["FCF", "CCC"], "key": {"F": "minecraft:feather", "C": "minecraft:calcite"}}},
    "trumpeting_angel": {
        "display": "Trumpeting Angel", "stone": "marble", "model": "trumpet_angel", "cells": TALL4, "overgrowth": "tall", "pack": 2,
        "shapes": [[(1, 0, 1, 15, 2.8, 15), (2, 2.8, 2, 14, 12, 14), (1.5, 12, 1.5, 14.5, 14.2, 14.5), (4, 14.2, 4, 12, 16, 12)],
                   [(5, 0, 5, 11, 16, 11)], [(5, 0, 5, 11, 8, 11), (3.6, 8, 3.6, 12.4, 11.6, 12.4)], [(5, 0, 4, 11, 15, 12)]],
        "text": {"face": "FRONT", "x": 8.0, "y": 7.4, "z": 2.0, "width": 8.8, "height": 6.0, "max_scale": 1 / 72},
        "recipe": {"pattern": ["FCF", " C ", " C "], "key": {"F": "minecraft:feather", "C": "minecraft:calcite"}}},
    "mortsafe": {
        "display": "Mortsafe", "stone": "iron", "model": "mortsafe", "cells": LONG, "overgrowth": "slab", "pack": 2,
        "shapes": [[(0.5, 0, 0.5, 15.5, 13.9, 16)], [(0.5, 0, 0, 15.5, 13.9, 15.5)]],
        "text": {"face": "FRONT", "x": 8.0, "y": 6.9, "z": 0.4, "width": 5.4, "height": 2.8, "max_scale": 1 / 96},
        "recipe": {"pattern": ["BBB", "B B", "BBB"], "key": {"B": "minecraft:iron_bars"}}},
    "faithful_hound": {
        "display": "Faithful Hound", "stone": "granite", "model": "hound", "cells": SINGLE, "overgrowth": "small", "pack": 2,
        "shapes": [[(1, 0, 2, 15, 6.2, 14), (1, 6.2, 4.5, 14.5, 12.5, 12)]],
        "text": {"face": "FRONT", "x": 8.0, "y": 3.3, "z": 2.6, "width": 11.0, "height": 3.6, "max_scale": 1 / 80},
        "recipe": {"pattern": ["BBB", "GGG"], "key": {"B": "#c:ingots/bronze", "G": "minecraft:polished_granite"}}},
}

# ---------------------------------------------------------------- pack 3: buildings
# Each building is one block (GraveyardBuildingBlock) of many parts, one for each block of its design grid that holds
# any of it, except those in `open` (a doorway, a room, a passage), which are left free to walk into and to hang a
# door or gates in. `size`: blocks wide, tall and deep, facing north (its front at z = 0); its roof may rise up to a
# block above its top row. `origin`: the grid cell of part 0, the block placed (on the ground in front). `texts`: where
# each inscription is cut, in design pixels (as a headstone's `text`; FACE EAST and WEST are upright faces whose plane
# is x = `x`), the first being its epitaph; `slots`: which inscription the chisel or a name tag cuts when used on the
# part at a grid cell (any other cuts the first). `light`: the light the part at a grid cell gives. `sound` and
# `tool`: what it sounds like and is mined with. Models: tools/graveyard_buildings.py.
MAUSOLEUM_OPEN = [(2, 0, 1), (2, 1, 1)] + [(x, y, z) for x in (1, 2, 3) for y in (0, 1) for z in (2, 3)]


def _mausoleum_texts():
    import graveyard_buildings as gb
    return [{"face": "FRONT", "x": 40.0, "y": 48.0, "z": 3.5, "width": 56.0, "height": 6.4, "max_scale": 1 / 28}] + gb.mausoleum_crypt_texts()


def _mausoleum_slots():
    """The crypt fronts, in the order of mausoleum_crypt_texts: the west wall's then the east's, each bay top down."""
    out = {}
    slot = 1
    for gx in (0, 4):
        for gz in (2, 3):
            for gy in (2, 1, 0):
                out[(gx, gy, gz)] = slot
                slot += 1
    return out


def _columbarium_texts():
    import graveyard_buildings as gb
    return gb.columbarium_texts()


BUILDINGS = {
    "family_mausoleum": {
        "display": "Family Mausoleum", "stone": "marble", "model": "mausoleum", "ivy": "mausoleum_ivy", "pack": 3,
        "size": (5, 4, 5), "origin": (2, 0, 0), "open": MAUSOLEUM_OPEN, "texts": _mausoleum_texts, "slots": _mausoleum_slots,
        "light": {(2, 2, 3): 10}, "sound": "stone", "tool": "pickaxe",
        "recipe": {"pattern": ["CCC", "CGC", "CBC"], "key": {"C": "minecraft:calcite", "G": "minecraft:glass_pane", "B": "#c:ingots/bronze"}}},
    "lych_gate": {
        "display": "Lych Gate", "stone": "granite", "model": "lych_gate", "ivy": "lych_gate_ivy", "pack": 3,
        "size": (4, 4, 2), "origin": (0, 0, 0), "open": [(x, y, z) for x in (1, 2) for y in (0, 1) for z in (0, 1)],
        "texts": lambda: [{"face": "FRONT", "x": 32.0, "y": 40.0, "z": 0.0, "width": 38.0, "height": 5.6, "max_scale": 1 / 28}],
        "slots": dict, "light": {}, "sound": "wood", "tool": "axe",
        "recipe": {"pattern": ["SSS", "LPL", "W W"], "key": {"S": "minecraft:deepslate_tile_slab", "L": "minecraft:oak_log",
                                                            "P": "minecraft:oak_planks", "W": "minecraft:cobblestone_wall"}}},
    "cemetery_gateway": {
        "display": "Cemetery Gateway", "stone": "granite", "model": "gateway", "ivy": "gateway_ivy", "pack": 3,
        "size": (5, 4, 1), "origin": (0, 0, 0), "open": [(x, y, 0) for x in (1, 2, 3) for y in (0, 1)],
        "texts": lambda: [{"face": "FRONT", "x": 40.0, "y": 46.4, "z": 7.2, "width": 25.0, "height": 5.2, "max_scale": 1 / 30}],
        "slots": dict, "light": {(0, 3, 0): 14, (4, 3, 0): 14}, "sound": "stone", "tool": "pickaxe",
        "recipe": {"pattern": ["L L", "GIG", "G G"], "key": {"L": "minecraft:lantern", "G": "minecraft:polished_granite", "I": "minecraft:iron_bars"}}},
    "columbarium": {
        "display": "Columbarium", "stone": "marble", "model": "columbarium", "ivy": "columbarium_ivy", "pack": 3,
        "size": (3, 3, 1), "origin": (1, 0, 0), "open": [], "texts": _columbarium_texts,
        "slots": lambda: {(2, 1, 0): 1, (1, 1, 0): 2, (0, 1, 0): 3, (2, 0, 0): 4, (1, 0, 0): 5, (0, 0, 0): 6},
        "light": {}, "sound": "stone", "tool": "pickaxe",
        "recipe": {"pattern": ["CCC", "CBC", "CCC"], "key": {"C": "minecraft:calcite", "B": "#c:ingots/bronze"}}},
}

# The Bronze Mausoleum Door: a door opened by hand (as a copper door) that fits the mausoleum's doorway.
MAUSOLEUM_DOOR = {"id": "bronze_mausoleum_door", "display": "Bronze Mausoleum Door",
                  "recipe": {"pattern": ["BB", "BG", "BB"], "key": {"B": "#c:ingots/bronze", "G": "minecraft:glass_pane"}, "count": 2}}

# Advancements granted from code (agriculture.HALLOWEEN_ADVANCEMENTS).
ADVANCEMENTS = {
    "here_lies": {"icon": "jugcraft:stonemasons_chisel", "title": "Here Lies…",
                  "description": "Cut an epitaph into a headstone with a Stonemason's Chisel", "frame": "task"},
    "groundskeeper": {"icon": "jugcraft:gothic_headstone", "title": "Groundskeeper",
                      "description": "Scrub an overgrown headstone back to clean stone with a brush", "frame": "task"},
}

# Every headstone's blockstate has parts 0 to PARTS - 1 (HeadstoneBlock.PART), however many it uses.
PARTS = 4

# The Stonemason's Chisel: an iron chisel struck with a mallet.
CHISEL_RECIPE = {"pattern": ["I", "S"], "key": {"I": "minecraft:iron_ingot", "S": "minecraft:stick"}}


def parts(headstone):
    return len(HEADSTONES[headstone]["cells"])


def blocks():
    return list(HEADSTONES) + list(BUILDINGS) + [MAUSOLEUM_DOOR["id"]]


def items():
    return list(HEADSTONES) + [EPITAPH["chisel"]] + list(BUILDINGS) + [MAUSOLEUM_DOOR["id"]]


def textures():
    """Every graveyard texture a model draws."""
    out = []
    for stone in (s for s in STONES if s != "iron"):
        for stage in STAGES:
            for suffix in (stage, f"{stage}_upper"):
                out += [f"gy_{stone}_{suffix}", f"gy_{stone}_relief_{suffix}"]
    out += [f"gy_granite_rough_{stage}" for stage in STAGES] + [f"gy_granite_knot_{stage}_upper" for stage in STAGES]
    out += [f"gy_{metal}_{stage}" for metal in ("iron", "bronze") for stage in STAGES]
    out += [f"gy_{wood}_{stage}" for wood in ("oak", "roof_slate") for stage in STAGES]
    out += ["gy_stained_glass", "gy_marble_floor", "gy_lamp_glass", "gy_lantern_glass", "gy_door_glass"]
    return out + ["gy_ivy"]

"""The graveyard pack (docs/features/graveyard.md): memorials carved from four stones, life-sized and finely modelled,
that weather with the years and carry an epitaph cut with the Stonemason's Chisel.

Pack 1, headstones (agriculture/HeadstoneBlock.java, HeadstoneBlockEntity.java, Epitaphs.java). Each headstone is one
to three blocks (`cells`: right, up, back from the block placed, part 0 holding the epitaph) facing whoever placed it.
`shapes`: per part, the boxes it can be hit and walked into by (pixels x0, y0, z0, x1, y1, z1, facing north, in that
part's block). `text`: where the epitaph is cut, facing north: FRONT on a face whose plane is z = `z` pixels, or TOP on a
top face at height `y`, centred at (`x`, `y`) or (`x`, `z`) (z may run into the next block back), `width` by `height`
pixels; each line is drawn as large as fits, up to `max_scale` blocks per font pixel.

Models: tools/graveyard_models.py (geometry), graveyard_data.py (one model per part and stage). Textures:
tools/graveyard_textures.py. Everything follows the agriculture feature switch for its recipes.
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
          "sandstone": {"ink": 0xFF3E2C1A, "fade_to": 0xFFB59C6E, "vanilla": "minecraft:smooth_sandstone"}}

SINGLE = [(0, 0, 0)]
TALL2 = [(0, 0, 0), (0, 1, 0)]
TALL3 = [(0, 0, 0), (0, 1, 0), (0, 2, 0)]
LONG = [(0, 0, 0), (0, 0, 1)]

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
}

# Advancements granted from code (agriculture.HALLOWEEN_ADVANCEMENTS).
ADVANCEMENTS = {
    "here_lies": {"icon": "jugcraft:stonemasons_chisel", "title": "Here Lies…",
                  "description": "Cut an epitaph into a headstone with a Stonemason's Chisel", "frame": "task"},
    "groundskeeper": {"icon": "jugcraft:gothic_headstone", "title": "Groundskeeper",
                      "description": "Scrub an overgrown headstone back to clean stone with a brush", "frame": "task"},
}

# The Stonemason's Chisel: an iron chisel struck with a mallet.
CHISEL_RECIPE = {"pattern": ["I", "S"], "key": {"I": "minecraft:iron_ingot", "S": "minecraft:stick"}}


def parts(headstone):
    return len(HEADSTONES[headstone]["cells"])


def blocks():
    return list(HEADSTONES)


def items():
    return list(HEADSTONES) + [EPITAPH["chisel"]]


def textures():
    """Every graveyard texture a model draws."""
    out = []
    for stone in STONES:
        for stage in STAGES:
            for suffix in (stage, f"{stage}_upper"):
                out += [f"gy_{stone}_{suffix}", f"gy_{stone}_relief_{suffix}"]
    out += [f"gy_granite_rough_{stage}" for stage in STAGES] + [f"gy_granite_knot_{stage}_upper" for stage in STAGES]
    return out + ["gy_ivy"]

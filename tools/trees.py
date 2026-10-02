"""Tree shapes: the configured tree features of Jugcraft's own trees (worldgen/feature/<shape>.json), and their
"<shape>_checked" placed features (only where the tree's sapling would survive), which biomes pick from.

Each shape grows one tree's wood (agriculture.WOOD_SETS, or a vanilla wood such as "minecraft:oak") and leaves
(agriculture.TREES, or the vanilla wood's leaves; the dead tree has none),
using vanilla 26.3's trunk and foliage placers. "sapling": the tree a sapling of that wood grows. Seasonal leaves take
today's look as the tree is placed: every shape with seasonal leaves lists the DECORATOR tree decorator
(agriculture/SeasonalLeavesDecorator), since world generation does not run the block's own placement hook.
Ranges are [min, max] (inclusive); a single number is fixed. "giant": a tree two blocks wide, which four saplings of its
wood grow when planted in a square (agriculture.TREES "giant"; agriculture/GiantSaplingBlock).
"""

DECORATOR = "seasonal_leaves"

SHAPES = {
    # Alpine Spawn's larch: a tall, narrow cone (vanilla's spruce foliage shape), its lowest trunk_height blocks bare.
    "larch": {"wood": "larch", "sapling": True,
              "trunk": {"type": "straight", "base_height": 7, "height_rand_a": 3},
              "foliage": {"type": "spruce", "radius": [1, 2], "offset": [0, 1], "trunk_height": [2, 3]}},
    # Maple: a round crown on a short trunk; big maples spread like vanilla's fancy oak.
    "maple": {"wood": "maple", "sapling": True,
              "trunk": {"type": "straight", "base_height": 5, "height_rand_a": 2},
              "foliage": {"type": "blob", "radius": 3, "offset": 0, "height": 3}},
    "big_maple": {"wood": "maple",
                  "trunk": {"type": "fancy", "base_height": 8, "height_rand_a": 6},
                  "foliage": {"type": "fancy", "radius": 2, "offset": 4, "height": 4}},
    # A maple bush: one log in a ball of leaves, the tundra's red scrub.
    "maple_bush": {"wood": "maple",
                   "trunk": {"type": "straight", "base_height": 1, "height_rand_a": 0},
                   "foliage": {"type": "bush", "radius": 2, "offset": 1, "height": 2}},
    # Aspen: a tall, slender trunk with a narrow, pear-shaped crown high up.
    "aspen": {"wood": "aspen", "sapling": True,
              "trunk": {"type": "straight", "base_height": 8, "height_rand_a": 3},
              "foliage": {"type": "blob", "radius": 2, "offset": 0, "height": 5}},
    # Fir: a tall, dense cone that keeps its needles; tall firs tower over it.
    "fir": {"wood": "fir", "sapling": True,
            "trunk": {"type": "straight", "base_height": 9, "height_rand_a": 3},
            "foliage": {"type": "spruce", "radius": [2, 3], "offset": [0, 1], "trunk_height": [1, 2]}},
    "tall_fir": {"wood": "fir",
                 "trunk": {"type": "straight", "base_height": 14, "height_rand_a": 5},
                 "foliage": {"type": "spruce", "radius": [2, 3], "offset": [0, 1], "trunk_height": [3, 5]}},
    # Jacaranda: a short trunk that forks into a wide, flat-topped umbrella of violet blossom (vanilla's acacia shapes).
    "jacaranda": {"wood": "jacaranda", "sapling": True,
                  "trunk": {"type": "forking", "base_height": 5, "height_rand_a": 2, "height_rand_b": 2},
                  "foliage": {"type": "acacia", "radius": 2, "offset": 0}},
    # Willow: a short trunk under a broad crown whose leaves hang in long curtains (vanilla's cherry foliage shape,
    # mostly hanging), with vines trailing like moss.
    "willow": {"wood": "willow", "sapling": True,
               "trunk": {"type": "straight", "base_height": 5, "height_rand_a": 2},
               "foliage": {"type": "cherry", "radius": 3, "offset": 0, "height": 5, "wide_bottom_layer_hole_chance": 0.2,
                           "corner_hole_chance": 0.3, "hanging_leaves_chance": 0.75, "hanging_leaves_extension_chance": 0.6},
               "decorators": [{"type": "minecraft:leave_vine", "probability": 0.2}]},
    # A tall swamp oak with vines (vanilla oak wood): the lush swamps' big trees.
    "tall_vine_oak": {"wood": "minecraft:oak",
                      "trunk": {"type": "straight", "base_height": 6, "height_rand_a": 3},
                      "foliage": {"type": "blob", "radius": 3, "offset": 0, "height": 3},
                      "decorators": [{"type": "minecraft:leave_vine", "probability": 0.25}]},
    # Palm: a tall trunk that bends as it rises (vanilla's bending trunk), crowned with a flat spray of fronds. It
    # grows on sand as well as grass (placed where a dead bush could stand).
    "palm": {"wood": "palm", "sapling": True, "survives_as": "minecraft:dead_bush",
             "trunk": {"type": "bending", "base_height": 6, "height_rand_a": 3, "min_height_for_leaves": 5,
                       "bend_length": [1, 2]},
             "foliage": {"type": "acacia", "radius": 2, "offset": 0}},
    # Cypress: a tall, narrow column of dark scale-leaves.
    "cypress": {"wood": "cypress", "sapling": True,
                "trunk": {"type": "straight", "base_height": 9, "height_rand_a": 4},
                "foliage": {"type": "spruce", "radius": [1, 1], "offset": [0, 1], "trunk_height": [1, 2]}},
    # A small acacia of dry country (vanilla acacia wood), growing on sand too.
    "desert_acacia": {"wood": "minecraft:acacia", "survives_as": "minecraft:dead_bush",
                      "trunk": {"type": "forking", "base_height": 3, "height_rand_a": 1, "height_rand_b": 1},
                      "foliage": {"type": "acacia", "radius": 1, "offset": 0}},
    # An oak bush: one oak log in a ball of oak leaves, the fields' scrub (vanilla oak wood; no sapling of its own).
    "oak_bush": {"wood": "minecraft:oak",
                 "trunk": {"type": "straight", "base_height": 1, "height_rand_a": 0},
                 "foliage": {"type": "bush", "radius": 2, "offset": 1, "height": 2}},
    # Big trees and rainforests. Redwood: a tall, straight trunk under a narrow spire of needles. Four saplings in a
    # square grow a giant redwood instead: a trunk two blocks wide, its crown high up, podzol spread round its foot
    # (vanilla's giant trunk and mega pine crown, as for the giant spruce).
    "redwood": {"wood": "redwood", "sapling": True,
                "trunk": {"type": "straight", "base_height": 12, "height_rand_a": 5},
                "foliage": {"type": "spruce", "radius": [2, 3], "offset": [0, 1], "trunk_height": [4, 6]}},
    "giant_redwood": {"wood": "redwood", "giant": True,
                      "trunk": {"type": "giant", "base_height": 22, "height_rand_a": 6, "height_rand_b": 10},
                      "foliage": {"type": "mega_pine", "radius": 0, "offset": 0, "crown_height": [11, 15]},
                      "decorators": [{"type": "minecraft:alter_ground", "provider": "minecraft:podzol_beneath_tree"}]},
    # Eucalyptus: a tall, clean trunk with a loose, airy crown (vanilla's random-spread foliage); big ones branch
    # high up.
    "eucalyptus": {"wood": "eucalyptus", "sapling": True,
                   "trunk": {"type": "straight", "base_height": 11, "height_rand_a": 4},
                   "foliage": {"type": "random_spread", "radius": 3, "offset": 0, "foliage_height": 3,
                               "leaf_placement_attempts": 60}},
    "big_eucalyptus": {"wood": "eucalyptus",
                       "trunk": {"type": "fancy", "base_height": 13, "height_rand_a": 7},
                       "foliage": {"type": "fancy", "radius": 2, "offset": 4, "height": 4}},
    # Mahogany: a tall rainforest hardwood that forks into a broad, flat canopy, hung with vines. Four saplings in a
    # square grow a giant mahogany: a trunk two blocks wide that branches as it rises (vanilla's mega jungle shapes).
    "mahogany": {"wood": "mahogany", "sapling": True,
                 "trunk": {"type": "forking", "base_height": 7, "height_rand_a": 3, "height_rand_b": 2},
                 "foliage": {"type": "acacia", "radius": 2, "offset": 0},
                 "decorators": [{"type": "minecraft:trunk_vine"}, {"type": "minecraft:leave_vine", "probability": 0.25}]},
    "giant_mahogany": {"wood": "mahogany", "giant": True,
                       "trunk": {"type": "mega_jungle", "base_height": 10, "height_rand_a": 2, "height_rand_b": 14},
                       "foliage": {"type": "jungle", "radius": 2, "offset": 0, "height": 2},
                       "decorators": [{"type": "minecraft:trunk_vine"}, {"type": "minecraft:leave_vine", "probability": 0.25}]},
    # A small palm of the tropics and subtropics, growing on sand as well as grass.
    "small_palm": {"wood": "palm", "survives_as": "minecraft:dead_bush",
                   "trunk": {"type": "bending", "base_height": 3, "height_rand_a": 1, "min_height_for_leaves": 2,
                             "bend_length": [1, 1]},
                   "foliage": {"type": "acacia", "radius": 1, "offset": 0}},
    # A spruce bush: one spruce log in a ball of needles, the scrub of rocky mountainsides (vanilla spruce wood).
    "spruce_bush": {"wood": "minecraft:spruce",
                    "trunk": {"type": "straight", "base_height": 1, "height_rand_a": 0},
                    "foliage": {"type": "bush", "radius": 2, "offset": 1, "height": 2}},
    # A dead tree: a bare, branching trunk of grey dead wood (vanilla's fancy trunk with no leaves).
    "dead_tree": {"wood": "dead", "survives_as": "minecraft:oak_sapling",
                  "trunk": {"type": "fancy", "base_height": 5, "height_rand_a": 4},
                  "foliage": None},
}

# Fallen logs lying on the forest floor (vanilla's fallen_tree feature), with mushrooms on some.
FALLEN = {"fir": [5, 8], "maple": [4, 6], "aspen": [5, 7], "dead": [4, 7], "redwood": [6, 9]}

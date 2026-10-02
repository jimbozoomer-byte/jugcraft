"""Tree shapes: the configured tree features of Jugcraft's own trees (worldgen/feature/<shape>.json), and their
"<shape>_checked" placed features (only where the tree's sapling would survive), which biomes pick from.

Each shape grows one tree's wood (agriculture.WOOD_SETS) and leaves (agriculture.TREES; the dead tree has none),
using vanilla 26.3's trunk and foliage placers. "sapling": the tree a sapling of that wood grows. Seasonal leaves take
today's look as the tree is placed: every shape with seasonal leaves lists the DECORATOR tree decorator
(agriculture/SeasonalLeavesDecorator), since world generation does not run the block's own placement hook.
Ranges are [min, max] (inclusive); a single number is fixed.
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
    # A dead tree: a bare, branching trunk of grey dead wood (vanilla's fancy trunk with no leaves).
    "dead_tree": {"wood": "dead", "survives_as": "minecraft:oak_sapling",
                  "trunk": {"type": "fancy", "base_height": 5, "height_rand_a": 4},
                  "foliage": None},
}

# Fallen logs lying on the forest floor (vanilla's fallen_tree feature), with mushrooms on some.
FALLEN = {"fir": [5, 8], "maple": [4, 6], "aspen": [5, 7], "dead": [4, 7]}

"""Barn & Harvest Tools, decor set 3 (docs/features/barn-and-harvest-tools.md): the farm equipment
docs/branches/AGRICULTURE.md planned but never built, and a barn's gear. Part 1: the planned hand tools, the Steel Sickle
(in tools/agriculture.py SICKLES, beside the flint and bronze ones), the Watering Can and the Seed Pouch. Part 2: the mill
and the dairy, the Quern and the Butter Churn, and the Flour, Butter and Pancakes they lead to. Part 3: drying and keeping
the harvest, the Drying Rack and the Corn Crib. Part 4: the hay loft's pulley and the farm bell, the Hay-Loft Pulley and
the Harvest Bell. The numbers here are what the Java (agriculture/WateringCanItem, SeedPouchItem, QuernBlock,
ButterChurnBlock, DryingRackBlock, CornCribBlock, HayPulleyBlock, HarvestBellBlock) uses; tools/check_mod_data.py
compares them.
tools/barn_tools_data.py writes their files; their icons are maps in tools/item_icons/.
"""

# The Watering Can (tinplate): holds `capacity` pours of water, filled at any water source. Each pour, on farmland or a
# crop standing on it, moistens the farmland in the square `radius` round it (3 by 3) and gives each crop on it one
# chance in `grow_odds` of a random tick: what an ordinary moment of growth does, never more than once a crop a pour.
# A pour waits `cooldown` ticks for the next, so a can cannot be emptied faster than a field can drink.
WATERING_CAN = {"item": "watering_can", "display": "Watering Can", "capacity": 16, "radius": 1, "grow_odds": 6, "cooldown": 10}

# The Seed Pouch: holds up to `kinds` kinds of seed (anything tagged c:seeds that plants a block), a stack of each. Use
# it in the air to gather the seeds you carry into it, sneak-use it in the air to empty it back; use it on farmland (or
# a crop on farmland) to sow every empty farmland in the square `radius` round it (3 by 3) from it, a seed a block, its
# first kind first.
SEED_POUCH = {"item": "seed_pouch", "display": "Seed Pouch", "kinds": 4, "radius": 1}

# ---------------------------------------------------------------- part 2: the mill and the dairy

# The Quern: a pair of hand millstones in a flour tray on a wooden stand, its upper stone (the runner) turned by a peg.
# It holds up to `capacity` of one grain in its eye and grinds one for every `turns` turns of the runner; a turn is a use
# with an empty hand, and the stone turns once round over `spin_ticks` ticks, a turn waiting for the last to finish. Wheat
# grinds into Flour, one for one; dried chili into paprika as the Mortar and Pestle grinds it (tools/spices.py GRIND:
# two). What it grinds falls out at its front. Sneak-use with an empty hand takes the grain back.
QUERN = {"block": "quern", "display": "Quern", "capacity": 16, "turns": 2, "spin_ticks": 10}
# The Butter Churn: a stave dash churn with iron hoops and a lid, the dasher's handle standing up through it. A bucket of
# milk goes in (the bucket comes back), and `strokes` strokes of the dasher (uses with an empty hand, each `stroke_ticks`
# ticks long, a stroke waiting for the last) churn it into `butter` Butter, which falls out at its front. Broken with milk
# in it, the milk is spilt: the bucket came back when it went in, so nothing else comes out.
CHURN = {"block": "butter_churn", "display": "Butter Churn", "strokes": 8, "butter": 2, "stroke_ticks": 10}

# ---------------------------------------------------------------- part 3: drying and keeping the harvest

# The Drying Rack: a freestanding frame, two uprights on splayed feet joined by two rails, one block across and two up.
# Each rail has two hooks, and a bunch of up to `bunch` herb sprigs (jugcraft:herbs) or peppers hangs from each. A bunch
# dries in `dry_ticks` ticks whether anyone is near or not: herbs into Dried Herbs and peppers into Dried Chili, one for
# one (the Herb Bundle's yield without its string, the furnace's without its fuel, and slower than either). An empty
# hand takes down a dried bunch, or the last fresh one if none has dried.
RACK = {"block": "drying_rack", "display": "Drying Rack", "size": (1, 2, 1), "bunch": 4, "dry_ticks": 2400}
# The Corn Crib: a slatted crib on posts under a tin gable roof, two blocks across, two up and one deep. It holds
# `capacity` ears of corn (anything tagged c:crops/corn), taken in and given back as the Apple Bin takes fruit, and the
# ears show through its slats, rising through `stages` stages as it fills.
CRIB = {"block": "corn_crib", "display": "Corn Crib", "size": (2, 2, 1), "capacity": 256, "stages": 4}

# ---------------------------------------------------------------- part 4: the hay loft and the bell

# The Hay-Loft Pulley: a beam out from a wall, braced, with an iron pulley block at its end and a rope over its wheel
# down to a hay hook, as a barn's hay hood has. The rope hangs from one to `drops` blocks below it. A use with an empty
# hand lets it down a block and sneaking hauls it up a block, the wheel turning and the rope running over `haul_ticks`
# ticks; a haul waits for the last. The hook sways a little as it hangs (drawn only).
PULLEY = {"block": "hay_pulley", "display": "Hay-Loft Pulley", "drops": 4, "haul_ticks": 20}
# The Harvest Bell: a farm bell hung in an iron yoke on a post, on a stone footing, one block across and two up. A use
# with an empty hand rings it: it swings and rings, the swing dying away over `swing_ticks` ticks, and the next ring waits
# for it to settle.
BELL = {"block": "harvest_bell", "display": "Harvest Bell", "size": (1, 2, 1), "swing_ticks": 40}

# What they make: plain ingredients, and a food that needs both (tools/agriculture.py ITEMS).
ITEMS = {
    "flour": {"display": "Flour", "compost": "low", "tags": []},
    "butter": {"display": "Butter", "tags": []},
    "pancakes": {"display": "Pancakes", "food": [5, 0.6], "compost": "medium_high", "tags": ["c:foods"]},
}

RECIPES = [
    {"id": "watering_can", "pattern": ["T  ", "TBT", " T "], "key": {"T": "#c:ingots/tin", "B": "minecraft:bucket"},
     "result": "watering_can", "count": 1, "category": "equipment", "features": ["tin"]},
    {"id": "seed_pouch", "pattern": [" S ", "C C", " C "], "key": {"S": "minecraft:string", "C": "#minecraft:wool"},
     "result": "seed_pouch", "count": 1, "category": "equipment"},
    # Part 2.
    {"id": "quern", "pattern": [" S ", "MMM", "P P"], "key": {"S": "minecraft:stick", "M": "minecraft:smooth_stone_slab", "P": "#minecraft:planks"},
     "result": "quern", "count": 1, "category": "misc"},
    {"id": "butter_churn", "pattern": [" S ", "PNP", "PPP"], "key": {"S": "minecraft:stick", "N": "minecraft:iron_nugget", "P": "#minecraft:planks"},
     "result": "butter_churn", "count": 1, "category": "misc"},
    # Part 3.
    {"id": "drying_rack", "pattern": ["STS", "S S", "S S"], "key": {"S": "minecraft:stick", "T": "minecraft:string"},
     "result": "drying_rack", "count": 1, "category": "misc"},
    {"id": "corn_crib", "pattern": ["LLL", "SCS", "P P"], "key": {"L": "#minecraft:wooden_slabs", "S": "minecraft:stick",
                                                                 "C": "minecraft:chest", "P": "#minecraft:planks"},
     "result": "corn_crib", "count": 1, "category": "misc"},
    # Part 4.
    {"id": "hay_pulley", "pattern": ["PPI", " SW", " S "], "key": {"P": "#minecraft:planks", "I": "#c:ingots/iron", "S": "minecraft:string",
                                                                  "W": "minecraft:tripwire_hook"},
     "result": "hay_pulley", "count": 1, "category": "misc"},
    {"id": "harvest_bell", "pattern": [" C ", "ICI", " L "], "key": {"C": "#c:ingots/copper", "I": "#c:ingots/iron", "L": "#minecraft:logs"},
     "result": "harvest_bell", "count": 1, "category": "misc"},
]

# Flour makes Wheat Dough as wheat does (three and a bucket of water make three), and Pancakes with butter and an egg.
SHAPELESS = [
    {"id": "wheat_dough_from_flour", "inputs": ["jugcraft:flour", "jugcraft:flour", "jugcraft:flour", "minecraft:water_bucket"],
     "result": "wheat_dough", "count": 3, "category": "misc"},
    {"id": "pancakes", "inputs": ["jugcraft:flour", "jugcraft:flour", "minecraft:egg", "jugcraft:butter"],
     "result": "pancakes", "count": 2, "category": "misc"},
]


def items():
    """The tools and blocks (the foods are in ITEMS)."""
    return [WATERING_CAN["item"], SEED_POUCH["item"]] + blocks()


def blocks():
    return [QUERN["block"], CHURN["block"], RACK["block"], CRIB["block"], PULLEY["block"], BELL["block"]]

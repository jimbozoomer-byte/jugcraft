"""Barn & Harvest Tools, decor set 3 (docs/features/barn-and-harvest-tools.md): the farm equipment
docs/branches/AGRICULTURE.md planned but never built, and a barn's gear. Part 1: the planned hand tools, the Steel Sickle
(in tools/agriculture.py SICKLES, beside the flint and bronze ones), the Watering Can and the Seed Pouch. The numbers here
are what agriculture/WateringCanItem and SeedPouchItem use; tools/check_mod_data.py compares them.
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

RECIPES = [
    {"id": "watering_can", "pattern": ["T  ", "TBT", " T "], "key": {"T": "#c:ingots/tin", "B": "minecraft:bucket"},
     "result": "watering_can", "count": 1, "category": "equipment", "features": ["tin"]},
    {"id": "seed_pouch", "pattern": [" S ", "C C", " C "], "key": {"S": "minecraft:string", "C": "#minecraft:wool"},
     "result": "seed_pouch", "count": 1, "category": "equipment"},
]


def items():
    return [WATERING_CAN["item"], SEED_POUCH["item"]]

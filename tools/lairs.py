"""The lairs (docs/features/witching-season.md, "The lairs: shared rules"; docs/features/hollow-acre.md): the numbers the
Java (src/main/java/.../lair) and the data (tools/lair_data.py) share. tools/check_mod_data.py checks the Java against
them.

Each lair is its own small pocket dimension, a void. A ritual opens an instance of it: a slot along the X axis,
SPACING blocks from the next, into which the lair's structure template is placed fresh. Players come in through the
ritual's gate (their own choice), cannot build or break anything there, and go home to exactly where they stood.
"""
FEATURE = "agriculture"

# Server options in config/jugcraft.properties (JugcraftConfig.TEXT_OPTIONS), with their defaults and (for numbers) the
# range the server keeps them in. lairs.off_season: "on" lets the rituals work all year at night; "off" only during the
# Halloween event (halloween.mode and its dates).
OPTIONS = {"lairs.instances": "8", "lairs.party_size": "4", "lairs.gate_seconds": "60", "lairs.off_season": "on"}
LIMITS = {"lairs.instances": (1, 32), "lairs.party_size": (1, 16), "lairs.gate_seconds": (10, 600)}

SPACING = 1024        # blocks between the instance slots of a lair, along X
BASE_Y = 64           # the template's lowest layer
CHECK_TICKS = 20      # the players in each lair are looked at once a second
EMPTY_SECONDS = 30    # an instance nobody has been inside for this long closes (its boss leaves, healed)
EDGE_DAMAGE = 4.0     # the mist's toll for flying or falling off, never below half a heart (1 health)

# The lair-only blocks: unbreakable fixtures, in the wither_immune and dragon_immune tags, with no item and no drops.
# The Hollow Acre's, the exit (every lair's), then the Spindle Loft's (docs/features/spindle-loft.md).
LAIR_BLOCKS = {
    "blighted_soil": "Blighted Soil",
    "black_wheat": "Black Wheat",
    "mown_stubble": "Mown Stubble",
    "lair_brazier": "Soul Brazier",
    "lair_moon": "Harvest Moon",
    "lair_exit": "Grey Mist",
    "doily_lace": "Doily Lace",
    "spool_wood": "Spool Wood",
    "spool_thread": "Spool Thread",
    "pincushion": "Pincushion",
    "pincushion_seam": "Pincushion Seam",
    "pincushion_leaf": "Pincushion Leaf",
    "needle_steel": "Needle Steel",
    "pin_shaft": "Pin",
    "measuring_tape": "Measuring Tape",
    "thimble_metal": "Thimble",
    "taut_thread": "Taut Thread",
    "grimy_skylight": "Grimy Skylight",
}
# The colours of the Spindle Loft's thread: its four spools', and the white of the threads between them.
THREAD_COLOURS = ("green", "blue", "beige", "red", "white")
# The doily's four lace patterns, in rings: a solid band, an open mesh, a flower and the scalloped edge.
LACE_PATTERNS = 4
# Fixtures a player in a lair may use (everything else is refused): the exits, and the braziers Vesperine's fight lights.
FIXTURES = ("lair_exit", "lair_brazier")

# The Last Rites (the Hollow Acre's ritual): a grave in the Overworld at night, lit candles round it, a Mourning Wreath
# laid on it and the Death Knell rung beside it.
RITE = {
    "grave_range": 4,          # how near the ringer the grave must be (blocks, a cube)
    "candle_range": 4,         # how near the grave the candles must be (blocks, a cube)
    "candles": 4,              # lit candles needed (a block of vanilla candles counts each candle)
    "wreath_range": 2,         # the wreath lies on the grave or beside it (blocks round the grave block found)
    "knell_cooldown": 40,      # ticks between rings of one bell
}
MOURNING_FLOWERS = ["jugcraft:black_rose", "jugcraft:funeral_lily", "jugcraft:spider_lily", "jugcraft:asphodel",
                    "jugcraft:snowdrop", "minecraft:lily_of_the_valley", "minecraft:wither_rose"]
# Any candle the séance counts, and the Chandlery's pillar candles.
RITE_CANDLES = ["#jugcraft:seance_candles", "jugcraft:black_pillar_candle", "jugcraft:ivory_pillar_candle"]

# A prick of the finger (the Spindle Loft's ritual): a Cursed Spindle used on a Spinning Wheel in the Overworld at
# night. The wheel spins wild and stays the gate for lairs.gate_seconds; whoever goes in wakes in the loft half-blind.
SPINDLE = {
    "waking_ticks": 40,        # Blindness on waking on the pincushion
}

ITEMS = {
    "mourning_wreath": "Mourning Wreath",
    "death_knell": "Death Knell",
    "cursed_spindle": "Cursed Spindle",
}
GATE = {"entity": "mist_gate", "display": "Mist Gate", "width": 1.6, "height": 2.6}

RECIPES = {
    # Four mourning flowers round a vine.
    "mourning_wreath": {"pattern": [" F ", "FVF", " F "], "key": {"F": "#jugcraft:mourning_flowers", "V": "minecraft:vine"}},
    # A handbell: a gold bell, an iron clapper, a bone handle. Not used up by ringing.
    "death_knell": {"pattern": ["G", "N", "B"], "key": {"G": "minecraft:gold_ingot", "N": "minecraft:iron_nugget",
                                                       "B": "minecraft:bone"}},
    # A spindle: thread and two spider eyes over a gold whorl set with amethyst, on a stick between two more threads.
    # Used up by its ritual.
    "cursed_spindle": {"pattern": ["ESE", "GAG", "STS"], "key": {
        "E": "minecraft:spider_eye", "S": "minecraft:string", "G": "minecraft:gold_ingot",
        "A": "minecraft:amethyst_shard", "T": "minecraft:stick"}},
}

# Each lair's dimension: a void with its own biome; fixed time, no weather, no beds or respawn anchors. Its sky, fog and
# light colours, its music, and the motes drifting in its air; and what a player is told on coming in.
LAIRS = {
    "hollow_acre": {
        "display": "The Hollow Acre",
        "enter": "You step through the mist into %s",
        "sky": "#0b0910", "fog": "#3a2a44", "water_fog": "#1d1622", "water": "#3f3150",
        "ambient_light_color": "#2a2030", "sky_light_color": "#3a3050",
        "music": "minecraft:music.nether.soul_sand_valley",
        "ambient_light": 0.15,
        "motes": ("minecraft:white_ash", 0.006),
    },
    "spindle_loft": {
        "display": "The Spindle Loft",
        "enter": "You prick your finger, fall asleep and wake in %s",
        "sky": "#0d0a08", "fog": "#3b3024", "water_fog": "#1e1913", "water": "#3d342a",
        "ambient_light_color": "#3a2e22", "sky_light_color": "#4a3c2c",
        "music": "minecraft:music.overworld.deep_dark",
        "ambient_light": 0.2,
        "motes": ("minecraft:white_ash", 0.004),
    },
}


def blocks():
    """Every block the lairs register: the lair-only blocks and the Mourning Wreath (the Cursed Spindle is an item)."""
    return list(LAIR_BLOCKS) + ["mourning_wreath"]


def itemless_blocks():
    """The lair-only blocks have no item: nobody can carry a piece of a lair home."""
    return list(LAIR_BLOCKS)


def items():
    return list(ITEMS)

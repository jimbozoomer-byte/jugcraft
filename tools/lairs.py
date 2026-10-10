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
LAIR_BLOCKS = {
    "blighted_soil": "Blighted Soil",
    "black_wheat": "Black Wheat",
    "mown_stubble": "Mown Stubble",
    "lair_brazier": "Soul Brazier",
    "lair_moon": "Harvest Moon",
    "lair_exit": "Grey Mist",
}
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

ITEMS = {
    "mourning_wreath": "Mourning Wreath",
    "death_knell": "Death Knell",
}
GATE = {"entity": "mist_gate", "display": "Mist Gate", "width": 1.6, "height": 2.6}

RECIPES = {
    # Four mourning flowers round a vine.
    "mourning_wreath": {"pattern": [" F ", "FVF", " F "], "key": {"F": "#jugcraft:mourning_flowers", "V": "minecraft:vine"}},
    # A handbell: a gold bell, an iron clapper, a bone handle. Not used up by ringing.
    "death_knell": {"pattern": ["G", "N", "B"], "key": {"G": "minecraft:gold_ingot", "N": "minecraft:iron_nugget",
                                                       "B": "minecraft:bone"}},
}

LAIRS = {
    "hollow_acre": {
        "display": "The Hollow Acre",
        # The dimension: a void with its own biome; fixed at midnight, no weather, no beds or respawn anchors.
        "sky": "#0b0910", "fog": "#3a2a44", "water_fog": "#1d1622",
        "music": "minecraft:music.nether.soul_sand_valley",
        "ambient_light": 0.15,
    },
}


def blocks():
    """Every block the lairs register: the lair-only blocks and the Mourning Wreath."""
    return list(LAIR_BLOCKS) + ["mourning_wreath"]


def itemless_blocks():
    """The lair-only blocks have no item: nobody can carry a piece of a lair home."""
    return list(LAIR_BLOCKS)


def items():
    return list(ITEMS)

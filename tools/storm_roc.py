"""The Storm Roc (docs/features/storm-roc.md; the plan in docs/branches/BOSSES.md, "The Storm Roc: the plan", part 2): the
numbers the Java (src/main/java/.../lair/roc) and the data (tools/storm_roc_data.py) share. tools/check_mod_data.py
checks the Java against them, and where his fight looks for the Crown's parts against tools/crown_of_thunder.py.

Units: health and damage in half hearts (vanilla's points), times in ticks (20 a second), distances in blocks, speeds in
blocks a tick. Damage and health are before the server's lairs.boss_damage and lairs.boss_health scaling (Vesperine's
settings, which every lair boss shares: 0.25 to 4, 1 by default).

His beak, talons and wings (the Storm Dive, the Wing Buffet, his fledglings' pecks) are a mob's attack; his Lightning
Marks are an electric arc (jugcraft:arc, the damage type tag jugcraft:electric), which the Iron Dreadnought's Grounding
Strap halves. The bolts that fall with them, and the rod's bolt that grounds him, are vanilla's light-only lightning.
"""

# StormRocEntity: him, his pacing and his rule.
ROC = {
    "HEALTH": 400.0, "ARMOR": 8.0,
    "LEASH": 30.0,               # farther than this from the summit's middle, he is set back
    "PARTY_STEP": 0.5, "PARTY_MAX": 2.5,
    "WAKE_TICKS": 50,            # waking: he rises screeching from his nest and glides down to the summit, unhurt
    "ABANDON_TICKS": 200,        # left alone this long, he flies back to his nest, healed
    "GLOBAL_COOLDOWN": 20,
    "SPEED": 0.16,               # stalking the summit on foot
    "KEEP_DISTANCE": 2.5,
    "STEP": 1.0,
    "FLIGHT_MAX": 120,           # no flight lasts longer: he lands to strike, so melee players get their openings
    "SQUALL_AT": 0.5,            # at this share of his health, the Squall Rises
    "EYE_AT": 0.2,               # below this share, the Eye of the Storm
    "SQUALL_TICKS": 60,          # the Squall Rises: he climbs high and circles screaming, unhurt
    "SQUALL_HEIGHT": 9.0,        # how high over the summit he circles then
    "SQUALL_CIRCLE": 8.0,        # and how wide
    # His rule, the Storm Rods: every DIVE_EVERY ticks (DIVE_FIRST after he wakes; DIVE_EVERY_SQUALL in the Squall and
    # DIVE_EVERY_EYE in the Eye of the Storm) he dives. For CHARGE_TICKS a rod charges (the bar names it) while he climbs
    # to where he dives from: over the summit's edge, PERCH_OUT from its middle and PERCH_UP over its floor, within
    # PERCH_SPREAD degrees either side of the charged rod's bearing: behind it, so that from anywhere on the summit a
    # sprinting player can put the rod between him and themselves in time (tools/check_mod_data.py walks it: ten degrees
    # either side already leaves a corner too far). For LANE_TICKS a line of sparks runs from him through
    # his marked foe and follows them; then it holds still, and he dives down it at DIVE_SPEED, through where they stood
    # and DIVE_OVERSHOOT beyond: DIVE_DAMAGE, and thrown DIVE_SHOVE aside, to everyone within DIVE_WIDTH of his path.
    # If his lane passes within GROUND_RADIUS of the charged rod, the rod grounds him as he passes it: he crashes there
    # and lies stunned for GROUNDED_TICKS, every blow doing GROUNDED_MULTIPLIER times as much, and he cannot fly.
    "DIVE_FIRST": 200, "DIVE_EVERY": 320, "DIVE_EVERY_SQUALL": 280, "DIVE_EVERY_EYE": 240,
    "CHARGE_TICKS": 40, "LANE_TICKS": 40,
    "PERCH_OUT": 20.0, "PERCH_UP": 8.0, "PERCH_SPREAD": 0.0,
    "DIVE_SPEED": 1.25, "DIVE_OVERSHOOT": 5.0, "DIVE_DAMAGE": 12.0, "DIVE_WIDTH": 1.5, "DIVE_SHOVE": 1.0,
    "GROUND_RADIUS": 2.5, "GROUNDED_TICKS": 120, "GROUNDED_MULTIPLIER": 1.5,
    # The sprint the rule is fair to: from anywhere on the summit a sprinting player (SPRINT blocks a second, less a
    # sixth for turning) reaches a place that lines the charged rod up between him and themselves before his lane holds.
    "SPRINT": 5.6,
    # Wing Buffet: he rears back and spreads his wings; then a gust in a cone before him, BUFFET_DEPTH deep and
    # BUFFET_ANGLE degrees either side of where he faces: BUFFET_DAMAGE, and thrown BUFFET_THROW back.
    "BUFFET_DAMAGE": 7.0, "BUFFET_DEPTH": 6.0, "BUFFET_ANGLE": 50.0, "BUFFET_THROW": 1.1,
    # Lightning Marks: MARKS crackling rings (EYE_MARKS in the Eye of the Storm), each MARK_RADIUS round, at least
    # MARK_APART from each other, the first under his foe and the rest under other players or on open summit; as the
    # wind-up ends a bolt strikes each: MARK_DAMAGE to whoever is within MARK_RADIUS of one (once).
    "MARKS": 3, "EYE_MARKS": 5, "MARK_RADIUS": 1.5, "MARK_APART": 6.0, "MARK_DAMAGE": 8.0,
    # Wind Strips (the Squall on): arrows on the summit floor in STRIPS strips STRIP_WIDTH wide, pointing the way the wind
    # will blow, through his foe and another place; through the active ticks the wind blows along each strip, pushing
    # everyone in it WIND_PUSH a tick the way its arrows point, unless rock stands within LEE of them upwind (a boulder).
    "STRIPS": 2, "STRIP_WIDTH": 3.0, "WIND_PUSH": 0.08, "LEE": 3.0,
    # His fledglings: at most FLEDGLING_CAP at once; they come at the Squall, and then he screams to the nest for more
    # (FLEDGLING_CALL, whose cooldown is the 30 seconds between) while he has fewer.
    "FLEDGLING_CAP": 3,
    "EXPERIENCE": 300,
}
# His attacks (StormRocEntity.Attack): wind-up, active and recovery ticks, cooldown, the range he uses it from (blocks),
# and in which phases ("gathering": the Gathering Storm; "squall": the Squall and the Eye of the Storm). The Storm Dive
# is his rule, not one of these: it comes on its own clock.
ATTACKS = {
    "WING_BUFFET": (20, 2, 14, 80, 0, 5.5, True, True),
    "LIGHTNING_MARKS": (30, 1, 16, 160, 0, 40, True, True),
    "WIND_STRIPS": (30, 80, 10, 300, 0, 40, False, True),
    "FLEDGLING_CALL": (20, 1, 10, 600, 0, 40, False, True),
}
# Where his fight stands on the Crown (template points and cells, checked against tools/crown_of_thunder.py):
# WOKE: where he comes down on the summit as he wakes, south of the north rod.
WOKE = (32.5, 24.5)
# FALL_EXIT: the Grey Mist he leaves when he falls, at the foot of his nest's crag: its columns, its row, three high from
# the summit's floor.
FALL_EXIT = {"x": (31, 33), "z": 15}
# His Gale Fledglings (GaleFledglingEntity): they leave the nest over RISE ticks, then dart after his foe at SPEED,
# HOVER over them, and stoop to peck (PECK_DAMAGE) every PECK_EVERY ticks once within REACH.
FLEDGLING = {"HEALTH": 20.0, "SPEED": 0.32, "HOVER": 2.5, "RISE": 20, "PECK_EVERY": 30, "PECK_DAMAGE": 3.0, "REACH": 1.6}

# His loot, rolled for each participant (RocLoot): Roc Feathers, a chance of one of his two trophies (the Arms VII
# variants his bosses/storm_roc table holds; one is certain on a player's first kill), and the chance of each of the
# rest.
FEATHERS = (2, 3)
TROPHIES = ("stormcaller", "galefeather")
TROPHY_CHANCE = 0.15
CHANCES = {"weather_vane_lens": 0.25, "rocs_crest": 0.2}
ADVANCEMENT = {"key": "eye_of_the_storm", "title": "Eye of the Storm", "icon": "jugcraft:stormcaller",
               "description": "Ground the Storm Roc with his own lightning and bring him down on the Crown of Thunder",
               "frame": "challenge"}
# The Weather-Vane Lens: held in the offhand, it tells its holder the coming weather every LENS_EVERY ticks, in the
# Overworld.
LENS_EVERY = 100

ENTITIES = {
    "storm_roc": "The Storm Roc",
    "gale_fledgling": "Gale Fledgling",
}
# The entities GeckoLib draws, and the model each is.
GECKO = {entity: entity for entity in ENTITIES}
# The bodies drawn with a glow layer, and so with a glowmask: his eyes and the lightning-white tips of his flight
# feathers; the fledglings' eyes.
GLOWING = ("storm_roc", "gale_fledgling")
ITEMS = {"roc_feather": "Roc Feather", "weather_vane_lens": "Weather-Vane Lens"}
COSTUMES = {"rocs_crest": "The Roc's Crest"}

RECIPES = {
    # For those who never fight him: a Roc Feather is four feathers.
    "feathers_from_roc_feather": {"shapeless": ["jugcraft:roc_feather"], "result": "minecraft:feather", "count": 4},
    # His part of what summons him again: two Roc Feathers make the next Thunder Vane without its lightning rod.
    "thunder_vane_from_roc_feathers": {"pattern": ["R R", "CKC", " C "], "key": {
        "R": "jugcraft:roc_feather", "C": "minecraft:copper_ingot", "K": "minecraft:compass"}, "result": "thunder_vane"},
}

MESSAGES = {
    "wakes": "The Storm Roc rises screeching from his nest, and lightning splits the sky",
    "charge_tell": "Lightning gathers on %s: put it between you and the Roc before he dives",
    "marked": "The Roc fixes on you: his lane follows you",
    "grounded": "The rod grounds him: the Storm Roc crashes down, stunned",
    "discharged": "The rod discharges harmlessly",
    "squall": "The Squall Rises: the Roc climbs high and circles screaming, and his fledglings leave the nest",
    "squall_bar": "The Storm Roc: the Squall",
    "eye": "The Eye of the Storm: the Roc dives oftener",
    "eye_bar": "The Storm Roc: the Eye of the Storm",
    "fledglings": "The Roc screams to his nest",
    "reset": "The Storm Roc flies back to his nest, and the storm quiets",
    "defeated": "The Storm Roc crashes down with a last thunderclap, and the storm breaks",
    "loot": "His feathers are yours",
    "lens.fair": "The lens shows fair weather for at least %s minutes",
    "lens.rain_in": "The lens shows rain in about %s minutes",
    "lens.rain_now": "The lens shows rain now, clearing in about %s minutes",
    "lens.thunder_in": "The lens shows rain now, and thunder in about %s minutes",
    "lens.thunder_now": "The lens shows thunder now, clearing in about %s minutes",
}
# The bar names the charged rod while it is (message.jugcraft.storm_roc.charged.<rod>).
CHARGED = {rod: f"The {rod} rod is charged" for rod in ("north", "east", "south", "west")}


def items():
    """Every item the Storm Roc's fight adds (the Stormcaller and the Galefeather are Arms VII variants, listed there)."""
    return list(ITEMS) + list(COSTUMES)

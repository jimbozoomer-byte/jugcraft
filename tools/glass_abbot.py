"""The Glass Abbot (docs/features/glass-abbot.md; the plan in docs/branches/BOSSES.md, "The Glass Abbot: the plan", part
2): the numbers the Java (src/main/java/.../lair/abbot) and the data (tools/glass_abbot_data.py) share.
tools/check_mod_data.py checks the Java against them, and where his fight looks for the chapel's parts against
tools/glass_chapel.py.

Units: health and damage in half hearts (vanilla's points), times in ticks (20 a second), distances in blocks, speeds in
blocks a tick. Damage and health are before the server's lairs.boss_damage and lairs.boss_health scaling (Vesperine's
settings, which every lair boss shares: 0.25 to 4, 1 by default).

His shards, his prism line and his beam are a mob's attack. His beam's geometry is the chapel's (tools/glass_chapel.py
trace): this file holds only its timing and what it does.
"""

# GlassAbbotEntity: him, his pacing, his mantle and his rule.
ABBOT = {
    "HEALTH": 340.0, "ARMOR": 6.0,
    "LEASH": 26.0,               # farther than this from the chapel's middle, he is set back at his place
    "PARTY_STEP": 0.5, "PARTY_MAX": 2.5,
    "WAKE_TICKS": 50,            # waking: he rises from his knees as the rose window floods with light, unhurt
    "ABANDON_TICKS": 200,        # left alone this long, he returns to his lectern and kneels, healed
    "GLOBAL_COOLDOWN": 20,
    "SPEED": 0.12,               # walking the nave
    "KEEP_DISTANCE": 6.0,        # he keeps this far from his foe: a caster, not a brawler
    "STEP": 1.0,
    "VEIL_AT": 0.5,              # at this share of his health, the Veil
    "BREAKING_AT": 0.2,          # below this share, the Breaking
    "VEIL_TICKS": 40,            # the Veil: he folds his hands and the windows dim, unhurt; his images stand up
    # His mantle: whole, blows and shots do MANTLE of their damage; cracked by his own beam sent home, for CRACK_TICKS he
    # kneels dazed at his place and they do CRACKED_MULTIPLIER of it.
    "MANTLE": 0.25, "CRACKED_MULTIPLIER": 1.25, "CRACK_TICKS": 160,
    # His rule, the Beam. BEAM_FIRST ticks after he wakes, and then BEAM_EVERY ticks from one marking to the next
    # (BEAM_EVERY_VEIL in the Veil, BEAM_EVERY_BREAKING in the Breaking; never less than BEAM_REST after a beam ends) he
    # marks two of the three mirrors (their frames burn gold, the bar names them). MARK_TICKS later he returns to his place
    # and gathers the sun for GATHER_TICKS; then the beam leaves his mantle, aimed at one of the two, and goes where the
    # mirrors send it (only a marked mirror turns it). Sent home, his mantle cracks. Lost, it burns along its whole path
    # for BEAM_SHOW ticks: BEAM_DAMAGE, once, to anyone within BEAM_WIDTH of it.
    "BEAM_FIRST": 160, "BEAM_EVERY": 400, "BEAM_EVERY_VEIL": 360, "BEAM_EVERY_BREAKING": 300, "BEAM_REST": 40,
    "MARK_TICKS": 200, "GATHER_TICKS": 60, "BEAM_SHOW": 20, "BEAM_DAMAGE": 6.0, "BEAM_WIDTH": 1.0,
    # From the Veil on: as he marks, one of the two is veiled for VEILED_TICKS (its glass clouded; its pointer and the
    # floor's lines still show it); and every TURN_EVERY ticks he turns one mirror one stop himself, after TURN_TELL ticks
    # of it shuddering, but never in the last TURN_GRACE ticks before he gathers the sun.
    "VEILED_TICKS": 80, "TURN_EVERY": 240, "TURN_TELL": 30, "TURN_GRACE": 100,
    # Shard Fan: three glass shards in a fan at his foe, SHARD_SPREAD degrees apart, flying SHARD_SPEED a tick for
    # SHARD_LIFE ticks unless a block stops them (a pillar, a mirror): SHARD_DAMAGE and a push of SHARD_PUSH to whoever one
    # strikes (within SHARD_HIT of its line). Strata's Brace halves the push.
    "SHARDS": 3, "SHARD_SPREAD": 15.0, "SHARD_SPEED": 0.9, "SHARD_LIFE": 30, "SHARD_DAMAGE": 6.0, "SHARD_PUSH": 0.6,
    "SHARD_HIT": 0.6,
    # Prism Line: a line of light drawn on the floor from him toward his foe, PRISM_LENGTH long; then a lance of light down
    # it: PRISM_DAMAGE to whoever is within PRISM_WIDTH of it.
    "PRISM_LENGTH": 18.0, "PRISM_WIDTH": 0.75, "PRISM_DAMAGE": 8.0,
    # His images (from the Veil): at most IMAGES_VEIL (IMAGES_BREAKING in the Breaking), standing IMAGE_APART from him; each
    # lasts IMAGE_TICKS or until struck.
    "IMAGES_VEIL": 2, "IMAGES_BREAKING": 3, "IMAGE_APART": 4.0, "IMAGE_TICKS": 200,
    # Radiance's preview: every PREVIEW_EVERY ticks while mirrors are marked, a player who understands First Light sees the
    # path the beam would take with the mirrors as they stand.
    "PREVIEW_EVERY": 10,
    "EXPERIENCE": 300,
}
# His attacks (GlassAbbotEntity.Attack): wind-up, active and recovery ticks, cooldown, the range he uses it from (blocks),
# and in which phases ("vespers": full to half health; "veil": the Veil and the Breaking). The Beam is his rule, not one of
# these: it comes on its own clock.
ATTACKS = {
    "SHARD_FAN": (16, 1, 10, 100, 0, 20, True, True),
    "PRISM_LINE": (20, 4, 12, 140, 0, 18, True, True),
    "FALSE_IMAGES": (30, 1, 10, 400, 0, 30, False, True),
}
# Where his fight stands in the chapel (template cells, checked against tools/glass_chapel.py):
# FALL_EXIT: the Grey Mist he leaves when he falls, in the apse behind his lectern: its columns, its row, three high.
FALL_EXIT = {"x": (16, 18), "z": 4}
# His Glass Images (GlassImageEntity): his likeness in glass; one blow shatters one.
IMAGE = {"HEALTH": 1.0}

# His loot, rolled for each participant (AbbotLoot): Prism Shards, a chance of the Glass Reliquary (certain on a player's
# first kill), and the chance of each of the rest.
SHARDS_DROPPED = (2, 3)
RELIQUARY_CHANCE = 0.15
CHANCES = {"abbots_cowl": 0.2}
ADVANCEMENT = {"key": "seen_clearly", "title": "Seen Clearly",
               "description": "Send the Glass Abbot's own light home and break his mantle for good", "icon": "jugcraft:glass_reliquary",
               "frame": "challenge"}
# The Lantern Lens: held in the offhand, every LENS_EVERY ticks it shows its holder (and only them) every invisible
# creature within LENS_RANGE, and the real Glass Abbot among his images.
LENS_EVERY = 20
LENS_RANGE = 12.0
# The Mirror Inscription: held in the offhand by someone a ward protects (who has absorption), it turns the next
# projectile that would strike them for INSCRIPTION_MAX or less back at its shooter, and rests INSCRIPTION_REST ticks.
INSCRIPTION_MAX = 8.0
INSCRIPTION_REST = 600
# Abbey Glass: stained glass that glows faintly.
ABBEY_GLASS_LIGHT = 4

ENTITIES = {
    "glass_abbot": "The Glass Abbot",
    "glass_image": "Glass Image",
}
# The entities GeckoLib draws, and the model each is: an image is his likeness.
GECKO = {"glass_abbot": "glass_abbot", "glass_image": "glass_abbot"}
# The bodies drawn with a glow layer, and so with a glowmask: his rose-window face and the light in his mantle's panes.
GLOWING = ("glass_abbot", "glass_image")
ITEMS = {"prism_shard": "Prism Shard", "glass_reliquary": "Glass Reliquary", "lantern_lens": "Lantern Lens",
         "mirror_inscription": "Mirror Inscription"}
COSTUMES = {"abbots_cowl": "The Abbot's Cowl"}
BLOCKS = {"abbey_glass": "Abbey Glass"}

RECIPES = {
    # For those who never fight him: a Prism Shard leads four glass into Abbey Glass.
    "abbey_glass": {"pattern": [" G ", "GPG", " G "], "key": {"G": "minecraft:glass", "P": "jugcraft:prism_shard"},
                    "result": "abbey_glass", "count": 4},
    # His part of what summons him again: two Prism Shards and a spyglass make the next Glass Accord.
    "glass_accord_from_prism_shards": {"shapeless": ["jugcraft:prism_shard", "jugcraft:prism_shard", "minecraft:spyglass"],
                                       "result": "glass_accord"},
    # The reliquary, his first-win choice: with a glass pane, the Lantern Lens; with an amethyst shard, the Mirror
    # Inscription. Neither turns back.
    "lantern_lens": {"shapeless": ["jugcraft:glass_reliquary", "minecraft:glass_pane"], "result": "lantern_lens"},
    "mirror_inscription": {"shapeless": ["jugcraft:glass_reliquary", "minecraft:amethyst_shard"], "result": "mirror_inscription"},
}

MESSAGES = {
    "wakes": "The Glass Abbot rises from his knees, and the rose window floods with light",
    "marked": "Light gathers in %s and %s: turn them to send his light home",
    "gather": "The Abbot gathers the sun",
    "home": "His light comes home: the Glass Abbot's mantle cracks",
    "lost": "The beam goes astray, and the mantle holds",
    "turned": "The Abbot turns %s",
    "veil": "The Veil: the windows dim, and three figures stand where he stood",
    "veil_bar": "The Glass Abbot: Compline",
    "breaking": "The Breaking: his light comes quicker",
    "breaking_bar": "The Glass Abbot: the Breaking",
    "reset": "The Glass Abbot kneels again before his lectern",
    "defeated": "The Glass Abbot's mantle shatters into light",
    "loot": "His light is yours",
    "mantle": "His glass mantle turns the blow aside",
    "inscription": "The Mirror Inscription turns the shot back",
}


def items():
    """Every item the Glass Abbot's fight adds."""
    return list(ITEMS) + list(COSTUMES) + list(BLOCKS)

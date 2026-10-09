"""Ember, the Hearthbinders' Principle (docs/features/arcane-concordance-ember.md), part 1: research and invocations.

The Hearthbinders keep fire and use it with control: heat for hearths, kilns and forges, never wildfire. Part 1 is
their research entry, Hearthbinding, and four invocations written in the shared grammar (roadmap step 10's rules):

- Hearthspark (utility): kindles an unlit campfire, candle or candle cake where you look, as flint and steel would;
- Hearthguard (defense): Fire Resistance;
- Cinderbolt (damage): fire damage that grows with fire Spell Power, and Smoulder;
- Hearthflare (damage, once mastered): a burst of fire round you, and Smoulder.

What Ember adds to the shared effects is small (Java: concordance/ember): an alteration in Ember's Spell Power school
kindles a hearth instead of putting out fire, and the Smoulder status keeps a creature alight while it lasts (vanilla
burning: water, rain, Fire Resistance and fire immunity all answer it). Nothing here places fire in the world.

Part 2 (the owner's art: the talismans, a bangle, three armour sets and a primer) follows in its own change.

tools/concordance.py merges these tables into its own. Numbers the Java repeats are checked by tools/check_mod_data.py.
"""

MOD = "jugcraft"


def rid(path):
    return f"{MOD}:{path}"


# ------------------------------------------------------------------------------------------------------- numbers

# What can be examined: fuels and things that hold fire. Coal, charcoal, torches and campfires are had in the Overworld
# from the start (charcoal from any log), so the first three are never out of reach; the rest come from the Nether or
# the sea floor.
EMBER_SPECIMEN_TAG = f"{MOD}:ember_specimens"
EMBER_SPECIMENS = {
    "minecraft:coal": "Coal", "minecraft:charcoal": "Charcoal", "minecraft:torch": "Torch",
    "minecraft:campfire": "Campfire", "minecraft:magma_block": "Magma Block", "minecraft:blaze_powder": "Blaze Powder",
    "minecraft:blaze_rod": "Blaze Rod", "minecraft:magma_cream": "Magma Cream", "minecraft:fire_charge": "Fire Charge",
}
# Different specimens examined to observe Hearthbinding.
OBSERVED_DISTINCT = 3

# The practice Hearthbinding is mastered by (Java: ember/Ember.ACTIVITY): each kind of hearth kindled with Hearthspark
# counts once (Java: Ember.hearth). Three of the four are had in the Overworld; the soul campfire needs the Nether.
HEARTHKEEPING = rid("hearthkeeping")
HEARTHS = {"campfire": "a campfire", "soul_campfire": "a soul campfire", "candle": "a candle",
           "candle_cake": "a candle cake"}
HEARTH_MASTERY = 3

# Smoulder (Java: ember/SmoulderEffect): whenever the creature's fire has burnt out, it is relit for FIRE_TICKS (a
# second), so it burns at vanilla's pace, a point of damage a second, while the status lasts.
SMOULDER = rid("smoulder")
SMOULDER_FIRE_TICKS = 20
SMOULDER_COLOUR = 0xF0743C
SMOULDER_TICKS = 60  # Cinderbolt's and Hearthflare's

# Hearthguard's Fire Resistance.
HEARTHGUARD_TICKS = 600
# Hearthspark's reach (its delivery's: the ray).
HEARTHSPARK_RANGE = 16

# ------------------------------------------------------------------------------------------------- research

_UNDERSTOOD = {"research": rid("hearthbinding"), "state": "understood"}
_MASTERED = {"research": rid("hearthbinding"), "state": "mastered"}

RESEARCH = {
    "hearthbinding": {
        "name": "Hearthbinding",
        "principle": "ember",
        "tradition": "hearthbinders",
        "stage": "practitioner",
        "icon": "minecraft:campfire",
        "requires": [{"research": rid("first_light"), "state": "understood"}],
        "states": {
            "encountered": [{"type": "examine", "specimens": f"#{EMBER_SPECIMEN_TAG}"}],
            "observed": [{"type": "examine", "specimens": f"#{EMBER_SPECIMEN_TAG}", "distinct": OBSERVED_DISTINCT}],
            "understood": [
                {"type": "study", "station": rid("lampwright_bench"), "specimens": f"#{EMBER_SPECIMEN_TAG}"},
                {"type": "notes"},
            ],
            # Mastery is practice: different kinds of hearth kindled.
            "mastered": [{"type": "practice", "activity": HEARTHKEEPING, "distinct": HEARTH_MASTERY}],
        },
        "unlocks": {"understood": {"invocations": [rid("hearthspark"), rid("hearthguard"), rid("cinderbolt")]},
                    "mastered": {"invocations": [rid("hearthflare")]}},
        "advancements": {
            "encountered": ("Banked Coals", "Examine something that holds fire", "task", "minecraft:coal"),
            "observed": ("Tinder and Flame", f"Examine {OBSERVED_DISTINCT} different things that hold fire", "task",
                         "minecraft:torch"),
            "understood": ("Hearthbinding",
                           "Understand Hearthbinding: study something that holds fire at a Lampwright's Bench",
                           "goal", "minecraft:campfire"),
            "mastered": ("Hearthbinder", f"Kindle {HEARTH_MASTERY} different kinds of hearth with Hearthspark",
                         "challenge", "minecraft:blaze_powder"),
        },
        "locked": {"encountered": "Examine something that holds fire first",
                   "observed": f"Examine {OBSERVED_DISTINCT} different things that hold fire first",
                   "understood": "Understand Hearthbinding first", "mastered": "Master Hearthbinding first"},
    },
}

# ------------------------------------------------------------------------------------- components and invocations

# Authored words: the invocations below are written with them, and players cannot compose with them (roadmap step 10).
COMPONENTS = {
    "kindling": {"slot": "operation", "requires": _UNDERSTOOD, "authored": True, "capacity": 1, "focus": 2,
                 "operation": {"effect": "alteration", "intent": "helpful", "principle": "ember",
                               "school": "spell_power:fire"},
                 "name": "Kindling", "text": "Hearthspark's spark: kindles an unlit campfire, candle or candle cake."},
    "hearthguard_ward": {"slot": "operation", "requires": _UNDERSTOOD, "authored": True, "capacity": 2, "focus": 4,
                         "operation": {"effect": "status", "intent": "helpful", "principle": "ember", "magnitude": 0,
                                       "duration": HEARTHGUARD_TICKS, "status": "minecraft:fire_resistance"},
                         "name": "Banked Fire", "text": "Hearthguard's warding: Fire Resistance."},
    "cinder": {"slot": "operation", "requires": _UNDERSTOOD, "authored": True, "capacity": 2, "focus": 3,
               "operation": {"effect": "damage", "intent": "harmful", "principle": "ember", "magnitude": 3,
                             "school": "spell_power:fire", "scaling": 0.5},
               "name": "Cinder", "text": "Cinderbolt's burning shard: fire damage that grows with fire Spell Power."},
    "smoulder": {"slot": "operation", "requires": _UNDERSTOOD, "authored": True, "capacity": 1, "focus": 1,
                 "operation": {"effect": "status", "intent": "harmful", "principle": "ember", "magnitude": 0,
                               "duration": SMOULDER_TICKS, "status": SMOULDER},
                 "name": "Smoulder", "text": "Leaves the creature burning for a few seconds (Smoulder)."},
    "flare": {"slot": "operation", "requires": _MASTERED, "authored": True, "capacity": 2, "focus": 4,
              "operation": {"effect": "damage", "intent": "harmful", "principle": "ember", "magnitude": 4,
                            "school": "spell_power:fire", "scaling": 0.5},
              "name": "Flare", "text": "Hearthflare's burst: fire damage that grows with fire Spell Power."},
}

INVOCATIONS = {
    "hearthspark": {
        "name": "Hearthspark", "role": "utility", "composition": "ray struck kindling", "stage": "understood",
        "focus": 3, "mastered_focus": 2, "tunings": ["extend"], "work": 4, "persists": 0,
        "cast": 0.5, "cooldown": 1.5,
        "animation": ("kindle_cast", "kindle_release"), "sounds": ("concordance.ember_gather", "concordance.hearthspark"),
        "particle": "minecraft:flame", "icon": "minecraft:flint_and_steel",
        "description": f"A spark that kindles the unlit campfire, candle or candle cake you look at, up to "
                       f"{HEARTHSPARK_RANGE} blocks away.",
        "counter": "It kindles only hearths you could light by hand there, never sets anything else alight, and a "
                   "waterlogged hearth will not catch.",
        "fizzle": "Nothing there to kindle: Hearthspark needs an unlit campfire, candle or candle cake within %s blocks",
    },
    "hearthguard": {
        "name": "Hearthguard", "role": "defense", "composition": "here struck hearthguard_ward", "stage": "understood",
        "focus": 5, "mastered_focus": 4, "tunings": ["prolong"], "work": 1, "persists": 2 * HEARTHGUARD_TICKS,
        "cast": 0.6, "cooldown": 20.0,
        "animation": ("aegis_cast", "aegis_release"), "sounds": ("concordance.ember_gather", "concordance.hearthguard"),
        "particle": "minecraft:small_flame", "icon": "minecraft:magma_cream",
        "description": f"Banks the fire round you: Fire Resistance for {HEARTHGUARD_TICKS // 20} seconds.",
        "counter": "Milk clears it, it guards against fire and lava only, and the cooldown is long.",
        "fizzle": "The fire finds nothing to guard",
    },
    "cinderbolt": {
        "name": "Cinderbolt", "role": "damage", "composition": "ray struck cinder smoulder", "stage": "understood",
        "focus": 5, "mastered_focus": 4, "tunings": ["extend", "intensify"], "work": 3, "persists": SMOULDER_TICKS,
        "cast": 0.5, "cooldown": 2.0,
        "animation": ("lance_cast", "lance_release"), "sounds": ("concordance.ember_gather", "concordance.cinderbolt"),
        "particle": "minecraft:flame", "icon": "minecraft:fire_charge",
        "description": "A burning shard: fire damage to the first creature it meets, which then smoulders. Stronger "
                       "with fire Spell Power.",
        "counter": "It needs a clear line and strikes one target; water, rain and Fire Resistance put out the "
                   "smoulder, and fire-born creatures shrug it off.",
        "fizzle": "The shard meets nothing to burn",
    },
    "hearthflare": {
        "name": "Hearthflare", "role": "damage", "composition": "here creatures flare smoulder", "stage": "mastered",
        "focus": 7, "mastered_focus": 6, "tunings": ["widen", "intensify"], "work": 8, "persists": SMOULDER_TICKS,
        "cast": 0.8, "cooldown": 10.0,
        "animation": ("ward_cast", "ward_release"), "sounds": ("concordance.ember_gather", "concordance.hearthflare"),
        "particle": "minecraft:lava", "icon": "minecraft:blaze_powder",
        "description": "A burst of fire round you: fire damage to the nearest creatures, which then smoulder. Never "
                       "you; stronger with fire Spell Power.",
        "counter": "It reaches only 3 blocks, the cast can be seen coming, and it spares no one but you: keep allies "
                   "clear (it never harms players you may not harm).",
        "fizzle": "No creature stands close enough to burn",
    },
}
for _invocation in INVOCATIONS.values():
    _invocation.update({"principle": "ember", "research": rid("hearthbinding")})

# Status names for the codex's words (vanilla's and Jugcraft's).
STATUS_NAMES = {"minecraft:fire_resistance": "Fire Resistance", SMOULDER: "Smoulder"}

# ------------------------------------------------------------------------------------------------ words and sounds

MESSAGES = {
    "examine.heat": "The %s holds fire. You turn it over, feeling for the heat it keeps.",
}

SOUND_EVENTS = {
    "concordance.ember_gather": "Embers gather",
    "concordance.hearthspark": "Spark kindles",
    "concordance.hearthguard": "Fire banks round you",
    "concordance.cinderbolt": "Cinder flies",
    "concordance.hearthflare": "Fire bursts",
}

ITEMS = {}
BLOCKS = {}


def lang_entries(lang):
    lang[f"effect.{MOD}.smoulder"] = "Smoulder"
    lang[f"tag.item.{MOD}.ember_specimens"] = "Things That Hold Fire"


def _hearth_list():
    return ", ".join(HEARTHS.values())


def codex():
    research = "hearthbinding"
    specimens = "\\\n".join(f"- {name}" for name in EMBER_SPECIMENS.values())
    return {
        ("hearth", "hearthbinding"): {
            "name": "Hearthbinding", "x": 0, "y": 0, "icon": "minecraft:campfire", "condition": None,
            "description": "Ember: research",
            "pages": [
                ("text", "Hearthbinding",
                 "Once you understand First Light, look at fire as the Hearthbinders do: something kept and used, "
                 "never let loose. **Sneak and use** one of these to examine it:\\\n" + specimens),
                ("text", "Observed",
                 f"You have examined {OBSERVED_DISTINCT} different things that hold fire. Study one at a "
                 f"**Lampwright's Bench**, or read someone's **Research Notes**.", (research, "observed")),
                ("text", "Understood",
                 "You can now cast **Hearthspark**, **Hearthguard** and **Cinderbolt** with any Concordance "
                 f"instrument. To **master** Hearthbinding, kindle {HEARTH_MASTERY} different kinds of hearth with "
                 f"Hearthspark: {_hearth_list()}.", (research, "understood")),
                ("text", "Mastered",
                 "You can now cast **Hearthflare**, and your Ember invocations cost a point less Focus.",
                 (research, "mastered")),
            ],
        },
        ("hearth", "smoulder"): {
            "name": "Smoulder", "x": 2, "y": 0, "icon": "minecraft:fire_charge", "condition": (research, "understood"),
            "description": "Fire that stays with its target",
            "pages": [
                ("text", "Smoulder",
                 "A smouldering creature burns as anything on fire does, a point of damage a second, while the status "
                 "lasts. Water and rain put it out; Fire Resistance and creatures born of fire ignore it; milk cures "
                 "it. It never sets blocks alight."),
                ("text", "Kindling",
                 "Hearthspark lights a hearth as flint and steel would, and only where you could light it by hand: "
                 "claims, protected towns and spawn protection refuse it. It never places fire."),
            ],
        },
    }


CATEGORY = {"hearth": {"name": "Hearth", "icon": "minecraft:campfire", "sort": 16,
                       "description": "The Hearthbinders' practice: fire kept and used"}}


def tags(tags):
    for ref in EMBER_SPECIMENS:
        tags.add("item", EMBER_SPECIMEN_TAG, ref)


def write_all(write, assets, data, lang, condition, self_drop):
    lang_entries(lang)


def write_data(write, data):
    pass

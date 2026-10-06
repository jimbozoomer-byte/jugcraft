"""The Arcane Concordance: Jugcraft's magic system (docs/ARCANE_CONCORDANCE.md). This module is the canonical source for
its vocabulary, numbers and first content (docs/features/arcane-concordance-first-light.md, milestone 1, "First Light").

Everything the game and the codex say about the Concordance is generated from the tables here, so a number cannot
differ between the research rules, the spell, the bench, the codex pages, the recipe viewer and the tests:

- data/jugcraft/concordance/{research,invocation,working}/*.json: the server-owned rules, read by
  concordance/ConcordanceData.java (a data pack can override them; the server validates them on every reload);
- data/jugcraft/spell/*.json: the Spell Engine spells the invocations cast (tools/check_mod_data.py lints them);
- data/jugcraft/modonomicon/...: the codex (a Modonomicon book) and the research bridge that shows its pages
  (Jugcraft awards an advancement on the server; Modonomicon turns it into a research fact; the fact unlocks pages);
- data/jugcraft/spell_assignments/*.json: Spell Engine opt-outs for Jugcraft arms that have their own weapon arts;
- assets/jugcraft/player_animations, dynamiclights, lang, models, items: presentation;
- assets/jugcraft/recipe_view.json "concordance": the bench's workings for JEI.

Java mirrors a few constants (Focus, motes, the lantern); tools/check_mod_data.py check_concordance() keeps them equal.

The ten Principles, seven resources, sixteen traditions and five stages below are NEW PROPOSALS made for this system on
5 October 2026. Nothing earlier in Jugcraft defined them; they extend (and do not rename) the eight school seeds in
docs/CONTENT_BRANCHES.md. Their ids become stable once released.
"""
MOD = "jugcraft"
FEATURE = "concordance"
BOOK = "arcane_concordance"

# ----------------------------------------------------------------------------------------------- vocabulary (proposed)

# The ten Principles: the aspects of the world a practice works with. A Principle is knowledge and affinity, never a
# currency: it names essences, research and invocations, and maps to a Spell Power school only where combat scaling fits.
PRINCIPLES = {
    "radiance": {"name": "Radiance", "domain": "light, revelation and sight", "school": "spell_power:arcane",
                 "seed": None, "color": 0xF4D27A},
    "ember": {"name": "Ember", "domain": "heat, combustion and change by fire", "school": "spell_power:fire",
              "seed": "Fire", "color": 0xF0743C},
    "rime": {"name": "Rime", "domain": "cold, stillness and preservation", "school": "spell_power:frost",
             "seed": "Ice", "color": 0xA9E4F2},
    "tempest": {"name": "Tempest", "domain": "storm, charge and sudden motion", "school": "spell_power:lightning",
                "seed": "Storm", "color": 0xF2EE8A},
    "strata": {"name": "Strata", "domain": "stone, structure and weight", "school": None, "seed": "Earth",
               "color": 0xA88A62},
    "verdance": {"name": "Verdance", "domain": "growth, life and renewal", "school": "spell_power:healing",
                 "seed": None, "color": 0x7CC46A},
    "tide": {"name": "Tide", "domain": "flow, solution and exchange", "school": None, "seed": None, "color": 0x4C8FD6},
    "tether": {"name": "Tether", "domain": "binding, sympathy and agreements", "school": None,
               "seed": "Cursing", "color": 0xB48AD8},
    "echo": {"name": "Echo", "domain": "memory, sound, dreams and knowledge", "school": "spell_power:arcane",
             "seed": None, "color": 0xC9B8E8},
    "hollow": {"name": "Hollow", "domain": "absence, decay and endings", "school": "spell_power:soul",
               "seed": "Necromancy, Blood", "color": 0x5E5A7A},
}

# The seven resource categories. None converts into another except through an explicit conversion recipe.
RESOURCES = {
    "focus": {"name": "Focus", "kind": "personal attention", "unit": "point",
              "summary": "A practitioner's own capacity to cast. It returns with time, lives with the player, and "
                         "can never be stored in an item, traded or carried by a device."},
    "ley_charge": {"name": "Ley Charge", "kind": "operational energy", "unit": "ley",
                   "summary": "Stored magical working energy in devices and conduits. It powers stations and "
                              "rituals; it is not light, heat or life."},
    "essence": {"name": "Principle Essence", "kind": "material value", "unit": "measure",
                "summary": "A measured quantity of one Principle drawn from a specimen. Essences of different "
                           "Principles never substitute for one another."},
    "vitae": {"name": "Vitae", "kind": "biological expenditure", "unit": "drop",
              "summary": "Life given deliberately in an offering. It is separate from health and from the "
                         "exhaustion that offering leaves behind."},
    "astral_resonance": {"name": "Astral Resonance", "kind": "celestial alignment", "unit": "resonance",
                         "summary": "Alignment gathered when the sky is right. It is recorded per event so the same "
                                    "alignment never pays twice."},
    "bound_will": {"name": "Bound Will", "kind": "supernatural identity", "unit": "record",
                   "summary": "A record of an agreement with a named spirit or construct: an obligation with an "
                              "identity, never an anonymous fluid."},
    "prima_materia": {"name": "Prima Materia", "kind": "equivalence value", "unit": "grain",
                      "summary": "The exact value assigned to an eligible mundane material for transmutation, rounded "
                                 "down and never created from nothing."},
}

# The five stages of the Concordance, against Jugcraft's own stages (docs/DESIGN.md).
STAGES = {
    "initiate": {"name": "Initiate", "jugcraft": "Discovery"},
    "practitioner": {"name": "Practitioner", "jugcraft": "Workshops"},
    "adept": {"name": "Adept", "jugcraft": "Specialization"},
    "master": {"name": "Master", "jugcraft": "Expeditions"},
    "architect": {"name": "Architect", "jugcraft": "Shared wonders"},
}

# The four states of a research entry for one player.
RESEARCH_STAGES = ["encountered", "observed", "understood", "mastered"]

# The sixteen traditions: specialties a player can follow without mastering the rest.
TRADITIONS = {
    "lampwrights": {"name": "Lampwrights", "principles": ["radiance"], "roadmap": 5,
                    "practice": "illumination, lanterns and revelation"},
    "hearthbinders": {"name": "Hearthbinders", "principles": ["ember"], "roadmap": 10,
                      "practice": "controlled heat for kilns, forges and cooking"},
    "rimekeepers": {"name": "Rimekeepers", "principles": ["rime"], "roadmap": 10,
                    "practice": "preservation, cooling and barriers"},
    "stormcallers": {"name": "Stormcallers", "principles": ["tempest"], "roadmap": 10,
                     "practice": "charging, weather instruments and swift movement"},
    "stratawrights": {"name": "Stratawrights", "principles": ["strata"], "roadmap": 10,
                      "practice": "soil care, shaping and mineral sensing"},
    "greenwardens": {"name": "Greenwardens", "principles": ["verdance"], "roadmap": 14,
                     "practice": "habitats, cultivation and living devices"},
    "alembists": {"name": "Alembists", "principles": ["tide", "ember", "verdance"], "roadmap": 13,
                  "practice": "experimental alchemy"},
    "circlewrights": {"name": "Circlewrights", "principles": ["tether", "strata"], "roadmap": 12,
                      "practice": "ritual structures, anchors and containment"},
    "spiritbinders": {"name": "Spiritbinders", "principles": ["tether", "hollow"], "roadmap": 17,
                      "practice": "agreements with spirits and familiars"},
    "clockhearts": {"name": "Clockhearts", "principles": ["strata", "tempest"], "roadmap": 17,
                    "practice": "constructs and accountable logistics"},
    "crimson_vigil": {"name": "Crimson Vigil", "principles": ["verdance", "hollow"], "roadmap": 16,
                      "practice": "offerings of Vitae and living equipment"},
    "starwatchers": {"name": "Starwatchers", "principles": ["radiance", "echo"], "roadmap": 15,
                     "practice": "celestial cycles and attunement"},
    "runesmiths": {"name": "Runesmiths", "principles": ["strata", "echo"], "roadmap": 19,
                   "practice": "equipment, runes, sockets and relics"},
    "hexweavers": {"name": "Hexweavers", "principles": ["tether", "hollow"], "roadmap": 22,
                   "practice": "sympathetic links, curses and wards"},
    "dreamwalkers": {"name": "Dreamwalkers", "principles": ["echo", "tide"], "roadmap": 22,
                     "practice": "dream expeditions and memory"},
    "balancewrights": {"name": "Balancewrights", "principles": ["strata", "tide"], "roadmap": 21,
                       "practice": "bounded material equivalence"},
}

# --------------------------------------------------------------------------------------------- milestone 1 numbers

# Focus (Java: concordance/FocusPool.java). One point returns every FOCUS_REGEN_TICKS game ticks, worked out from the
# game time when it is read, so nothing ticks per player.
FOCUS_MAX = 20
FOCUS_REGEN_TICKS = 40

# Examining a specimen counts as "in darkness" at or below this light (Java: Examination.DARK_LIGHT). Night in the open
# reads 4, so an Initiate can study by a dark cave or under the night sky.
DARK_LIGHT = 4
# Distinct specimens examined in darkness that understand Radiance without a bench (the field route).
FIELD_SPECIMENS = 3
# Distinct chunks a Kindle must light to master First Light.
MASTERY_CHUNKS = 8

# Kindle (the illumination invocation; Java: KindleInvocation.java and LumenMoteBlock.java).
KINDLE_FOCUS = 4
KINDLE_MASTERED_FOCUS = 3
KINDLE_RANGE = 16
KINDLE_COOLDOWN_SECONDS = 1.5
KINDLE_CAST_SECONDS = 0.6
MOTE_LIGHT = 14
MOTE_STEP_TICKS = 80
KINDLE_MOTE_STEPS = 15  # a Kindled mote lasts 15 steps of 80 ticks: 60 seconds
TRAIL_LIGHT = 12
TRAIL_CHECK_TICKS = 10

# The Kindled Lantern (Java: KindledLanternItem.java). Radiance is measured in "measures" of Radiance essence.
LANTERN_CAPACITY = 64
LANTERN_BURN_TICKS = 400  # a lit lantern burns one measure every 20 seconds, carried or not
LANTERN_START = 8  # the measures the amethyst shard leaves in a newly kindled lantern

# The Lampwright's Bench (Java: LampwrightBenchBlockEntity.java).
STUDY_TICKS = 100
CHANNEL_FOCUS = 6
CHANNEL_RADIANCE = 2

# Luminous specimens: what can be examined and studied, and the Radiance each yields when infused into a lantern.
# Amethyst is the first: geodes are common, early and already Jugcraft's magic crystal.
SPECIMENS = {
    "minecraft:amethyst_shard": 8,
    "minecraft:glowstone_dust": 6,
    f"{MOD}:glowcap": 4,
    f"{MOD}:glimmerbloom": 3,
    f"{MOD}:jack_o_lantern_mushroom": 3,
    "minecraft:glow_ink_sac": 3,
    "minecraft:glow_lichen": 2,
    "minecraft:glow_berries": 1,
}
SPECIMEN_TAG = f"{MOD}:luminous_specimens"
INSTRUMENT_TAG = f"{MOD}:concordance_instruments"
SPELL_TAG = f"{MOD}:concordance"

# --------------------------------------------------------------------------------------------- milestone 1 content

ITEMS = {
    "initiate_wand": {"name": "Initiate's Wand",
                      "tooltip": "A first instrument. Hold it to cast the invocations you have understood; a "
                                 "Concordance invocation needs a Concordance instrument in the main hand."},
    "kindled_lantern": {"name": "Kindled Lantern",
                        "tooltip": "Holds Radiance. Use it to light or put it out; lit and in either hand it lights "
                                   "the way round you, and it burns its Radiance whether carried or not. Recharge it "
                                   "at a Lampwright's Bench."},
}
BLOCKS = {
    "lampwright_bench": {"name": "Lampwright's Bench"},
}
# Items that cast Concordance invocations from the main hand (#jugcraft:concordance_instruments).
INSTRUMENTS = ["initiate_wand"]
# Blocks with no item: the Kindled mote is light, not a thing to hold.
ITEMLESS_BLOCKS = {"lumen_mote": {"name": "Kindled Light"}}

RESEARCH = {
    "first_light": {
        "name": "First Light",
        "principle": "radiance",
        "tradition": "lampwrights",
        "stage": "initiate",
        "icon": "minecraft:amethyst_shard",
        "requires": [],
        # Each state needs the one before it and one of its alternatives.
        "states": {
            "encountered": [{"type": "examine", "specimens": f"#{SPECIMEN_TAG}"}],
            "observed": [{"type": "examine", "specimens": f"#{SPECIMEN_TAG}", "max_light": DARK_LIGHT}],
            "understood": [
                {"type": "study", "station": f"{MOD}:lampwright_bench", "specimens": f"#{SPECIMEN_TAG}"},
                {"type": "examine", "specimens": f"#{SPECIMEN_TAG}", "max_light": DARK_LIGHT,
                 "distinct": FIELD_SPECIMENS},
            ],
            "mastered": [{"type": "invoke", "invocation": f"{MOD}:kindle", "distinct_chunks": MASTERY_CHUNKS}],
        },
        "unlocks": {"understood": {"invocations": [f"{MOD}:kindle"],
                                   "workings": [f"{MOD}:kindle_lantern", f"{MOD}:infuse_lantern",
                                                f"{MOD}:channel_lantern"]}},
    },
}

INVOCATIONS = {
    "kindle": {
        "name": "Kindle",
        "principle": "radiance",
        "research": f"{MOD}:first_light",
        "stage": "understood",
        "focus": KINDLE_FOCUS,
        "mastered_focus": KINDLE_MASTERED_FOCUS,
        "description": f"Sets a mote of steady light in the open block where you look, up to {KINDLE_RANGE} blocks "
                       f"away, for {KINDLE_MOTE_STEPS * MOTE_STEP_TICKS // 20} seconds. Costs {KINDLE_FOCUS} Focus "
                       f"({KINDLE_MASTERED_FOCUS} once First Light is mastered).",
    },
}

WORKINGS = {
    "kindle_lantern": {"type": "craft", "work": "minecraft:lantern", "specimen": "minecraft:amethyst_shard",
                       "result": f"{MOD}:kindled_lantern", "radiance": LANTERN_START},
    "infuse_lantern": {"type": "infuse", "work": f"{MOD}:kindled_lantern", "specimens": SPECIMENS},
    "channel_lantern": {"type": "channel", "work": f"{MOD}:kindled_lantern", "focus": CHANNEL_FOCUS,
                        "radiance": CHANNEL_RADIANCE},
}
for _working in WORKINGS.values():
    _working.update({"station": f"{MOD}:lampwright_bench", "research": f"{MOD}:first_light", "stage": "understood"})

SCHEMA = 1


def items():
    return list(ITEMS)


def blocks():
    return list(BLOCKS)


def itemless_blocks():
    return list(ITEMLESS_BLOCKS)


def research_advancement(research, state):
    """The advancement Jugcraft awards when a player's research reaches a state (Java: ConcordanceProgress.award)."""
    return f"concordance_{research}_{state}"


def advancement_ids():
    return ["concordance", "concordance_first_kindle"] + [research_advancement(r, s) for r in RESEARCH
                                                          for s in RESEARCH_STAGES]


# Jugcraft items with their own weapon arts or uses: Spell Engine's fallback would otherwise make them spell casters
# and give some of them Spell Engine weapon skills (docs/ARCANE_CONCORDANCE.md, "Equipment defaults").
def spell_opt_outs():
    import arms
    import arms_variants
    return sorted(set(arms.items()) | set(arms_variants.items())
                  | {"power_katana", "power_bow", "ronin_katana", "carnival_mallet", "pinata_stick"})


# ------------------------------------------------------------------------------------------------------- codex text

def seconds(ticks):
    value = ticks / 20
    return str(int(value)) if value == int(value) else f"{value:g}"


def specimen_name(ref):
    names = {"minecraft:amethyst_shard": "Amethyst Shard", "minecraft:glowstone_dust": "Glowstone Dust",
             f"{MOD}:glowcap": "Glowcap", f"{MOD}:glimmerbloom": "Glimmerbloom",
             f"{MOD}:jack_o_lantern_mushroom": "Jack-o'-Lantern Mushroom", "minecraft:glow_ink_sac": "Glow Ink Sac",
             "minecraft:glow_lichen": "Glow Lichen", "minecraft:glow_berries": "Glow Berries"}
    return names[ref]


def specimen_table():
    return "\\\n".join(f"- {specimen_name(ref)}: {value} Radiance" for ref, value in SPECIMENS.items())


# Codex entries: (category, entry) -> {name, description, x, y, icon, condition (research state or None), pages}.
# A page is (type, title, text[, extra]) with an optional condition. Texts are lang strings; Modonomicon renders them
# as markdown. Every number comes from the tables above.
def codex():
    principles = "\\\n".join(f"- **{p['name']}**: {p['domain']}" for p in PRINCIPLES.values())
    resources = "\\\n".join(f"- **{r['name']}** ({r['kind']})" for r in RESOURCES.values())
    stages = " > ".join(s["name"] for s in STAGES.values())
    return {
        ("foundations", "concordance"): {
            "name": "The Arcane Concordance", "x": 0, "y": 0, "icon": f"{MOD}:{BOOK}", "condition": None,
            "description": "What this codex records, and how to begin",
            "pages": [
                ("text", "The Arcane Concordance",
                 "The Concordance is the practice of working with the Principles that run through the world. It "
                 "begins with **looking closely**. Nothing in this codex teaches by being read: you learn by "
                 "examining, studying and doing, and the codex records what you have learned."),
                ("text", "Where to Begin",
                 "Find something that holds its own light. Amethyst grows in geodes underground; glow lichen and "
                 "glow berries hang in caves.\\\n\\\n**Sneak and use** a luminous specimen to examine it. Its glow "
                 "is easiest to judge **in darkness**: a dark cave, or the open sky at night."),
                ("text", "The Path", f"The Concordance has five stages: {stages}. Each opens new practices; no one "
                                     f"needs to follow every tradition."),
            ],
        },
        ("foundations", "principles"): {
            "name": "Principles and Resources", "x": 2, "y": 0, "icon": "minecraft:amethyst_cluster",
            "condition": None, "description": "The ten Principles and the seven resources",
            "pages": [
                ("text", "The Ten Principles", principles),
                ("text", "Resources Are Not Interchangeable",
                 f"{resources}\\\n\\\nNothing turns one into another except a recipe that names both, with its loss."),
                ("text", "Knowledge", "Knowledge is not a resource. A research entry moves through four states, "
                                      "**Encountered**, **Observed**, **Understood** and **Mastered**, from evidence "
                                      "you gather yourself. Repeating the same observation adds nothing."),
            ],
        },
        ("foundations", "focus"): {
            "name": "Focus", "x": 0, "y": 2, "icon": f"{MOD}:initiate_wand", "condition": None,
            "description": "Your own capacity to cast",
            "pages": [
                ("text", "Focus",
                 f"Every invocation spends **Focus**. You hold at most {FOCUS_MAX} points; one returns every "
                 f"{seconds(FOCUS_REGEN_TICKS)} seconds, whatever you are doing. Focus is yours alone: no item, "
                 f"device or trade can store it.\\\n\\\nWhile you hold an instrument, your Focus shows above the "
                 f"hotbar as a number and a bar."),
            ],
        },
        ("foundations", "initiate_wand"): {
            "name": "Initiate's Wand", "x": 2, "y": 2, "icon": f"{MOD}:initiate_wand", "condition": None,
            "description": "The first instrument",
            "pages": [
                ("crafting_recipe", "Initiate's Wand",
                 "An instrument channels invocations. Hold it in your **main hand**; the invocations you have "
                 "understood appear on the spell bar, and the first is cast with the use key.", f"{MOD}:initiate_wand"),
            ],
        },
        ("radiance", "first_light"): {
            "name": "First Light", "x": 0, "y": 0, "icon": "minecraft:amethyst_shard", "condition": None,
            "description": "Radiance: research",
            "pages": [
                ("text", "First Light",
                 "Some things hold light of their own. **Sneak and use** one to examine it.\\\n\\\nLuminous "
                 "specimens:\\\n" + "\\\n".join(f"- {specimen_name(ref)}" for ref in SPECIMENS)),
                ("text", "Encountered", "You have handled a luminous specimen and noticed its glow. Now judge it "
                                        "where there is no other light.", "encountered"),
                ("text", "Observed",
                 f"In darkness (light {DARK_LIGHT} or less) the glow is the specimen's own. To **understand** it, "
                 f"either study a specimen at a **Lampwright's Bench**, or examine {FIELD_SPECIMENS} different "
                 f"specimens in darkness.", "observed"),
                ("text", "Understood",
                 f"Radiance can be gathered and set loose. You can now cast **Kindle** with an instrument, and kindle, "
                 f"infuse and channel lanterns at a Lampwright's Bench.\\\n\\\nTo **master** First Light, Kindle "
                 f"light in {MASTERY_CHUNKS} different chunks.", "understood"),
                ("text", "Mastered", f"Kindle now costs {KINDLE_MASTERED_FOCUS} Focus instead of {KINDLE_FOCUS}.",
                 "mastered"),
            ],
        },
        ("radiance", "lampwright_bench"): {
            "name": "Lampwright's Bench", "x": 2, "y": 0, "icon": f"{MOD}:lampwright_bench", "condition": None,
            "description": "Study specimens and tend lanterns",
            "pages": [
                ("crafting_recipe", "Lampwright's Bench",
                 "The bench holds one specimen and one piece of work. Its buttons say what they need when they "
                 "cannot run.", f"{MOD}:lampwright_bench"),
                ("text", "Study",
                 f"**Study** reads a specimen for {seconds(STUDY_TICKS)} seconds. The specimen is held while it "
                 f"is read and used up only when the study finishes; cancel, break the bench or take the specimen "
                 f"and nothing is lost. If you are away when it finishes, the bench keeps your notes until you "
                 f"next open it."),
                ("text", "Lanterns",
                 f"Once First Light is understood: **Kindle** turns a lantern and an amethyst shard into a Kindled "
                 f"Lantern with {LANTERN_START} Radiance. **Infuse** adds a specimen's Radiance to a Kindled "
                 f"Lantern, up to {LANTERN_CAPACITY}; a specimen that would overfill it is refused. **Channel** "
                 f"spends {CHANNEL_FOCUS} Focus for {CHANNEL_RADIANCE} Radiance.\\\n\\\n" + specimen_table()),
            ],
        },
        ("radiance", "kindle"): {
            "name": "Kindle", "x": 0, "y": 2, "icon": "minecraft:glowstone_dust", "condition": "understood",
            "description": "The illumination invocation",
            "pages": [
                ("text", "Kindle",
                 f"{INVOCATIONS['kindle']['description']}\\\n\\\nThe mote lights only open air you may build in. "
                 f"It cannot be picked up and it goes out on its own; a block placed in its space replaces it."),
            ],
        },
        ("radiance", "kindled_lantern"): {
            "name": "Kindled Lantern", "x": 2, "y": 2, "icon": f"{MOD}:kindled_lantern", "condition": "understood",
            "description": "Light that travels with you",
            "pages": [
                ("text", "Kindled Lantern",
                 f"Use it to light it or put it out. Lit, it burns one Radiance every "
                 f"{seconds(LANTERN_BURN_TICKS)} seconds whether carried or not, and while it is in either hand the "
                 f"air round you is lit (light {TRAIL_LIGHT}). It holds up to {LANTERN_CAPACITY} Radiance.\\\n\\\n"
                 f"A lantern can be given or traded; anyone may carry its light, but only someone who understands "
                 f"First Light can recharge it."),
            ],
        },
    }


CATEGORIES = {
    "foundations": {"name": "Foundations", "icon": "minecraft:amethyst_shard", "sort": 0,
                    "description": "The Concordance, its Principles and its resources"},
    "radiance": {"name": "Radiance", "icon": "minecraft:glowstone_dust", "sort": 1,
                 "description": "The Lampwrights' practice"},
}

ENTRY_BACKGROUNDS = {None: "square_gray", "understood": "hexagon_purple"}

# ------------------------------------------------------------------------------------------------- data generation


def rid(path):
    return f"{MOD}:{path}"


def research_json(key, info):
    return {"schema": SCHEMA, "principle": info["principle"], "tradition": info["tradition"],
            "stage": info["stage"], "icon": info["icon"], "requires": info["requires"],
            "states": {state: {"any": rules} for state, rules in info["states"].items()},
            "unlocks": info["unlocks"]}


def invocation_json(key, info):
    return {"schema": SCHEMA, "spell": rid(key), "principle": info["principle"], "research": info["research"],
            "stage": info["stage"], "focus": info["focus"], "mastered_focus": info["mastered_focus"]}


def spell_json(key):
    """Kindle as a Spell Engine spell. Spell Engine owns the cast timeline, the cooldown and the animations; Jugcraft's
    CASTING_ATTEMPT listener gates it (instrument, research, Focus), its CUSTOM impact places the light, and Focus is
    spent once, in COST_CONSUME, after a successful cast (docs/ARCANE_CONCORDANCE.md, "One authority per cast")."""
    return {
        "school": "spell_power:arcane",
        "range": float(KINDLE_RANGE),
        "tier": 1,
        "group": "concordance",
        "type": "ACTIVE",
        "active": {"cast": {"duration": KINDLE_CAST_SECONDS, "animation": {"id": rid("kindle_cast")},
                            "start_sound": {"id": rid("concordance.kindle_gather")}}},
        "release": {"animation": {"id": rid("kindle_release")}, "sound": {"id": rid("concordance.kindle")}},
        "target": {"type": "CASTER"},
        "deliver": {"type": "DIRECT"},
        "impacts": [{"action": {"type": "CUSTOM", "custom": {"handler": rid("kindle_light"), "intent": "HELPFUL"}}}],
        "cost": {"exhaust": 0.0, "durability": 0,
                 "cooldown": {"duration": KINDLE_COOLDOWN_SECONDS, "hosting_item": False}},
    }


def player_animations():
    """Original Spell Engine/Player Animation Library clips in the PlayerAnimator "emote" format Spell Engine uses
    (angles in radians; a negative arm pitch raises the arm forward; parts and axes not listed keep their pose). No
    "torso" key: in this format PAL reads it as the whole-body bone, so it would tilt the player."""
    def clip(name, loop, begin, end, stop, keys):
        moves = []
        for tick, part, axis, value in keys:
            moves.append({"tick": tick, "easing": "EASEINOUTQUAD", "turn": 0, part: {axis: value}})
        return {"name": name, "author": "Jugcraft", "description": f"Arcane Concordance: {name}",
                "emote": {"isLoop": "true" if loop else "false", "returnTick": begin if loop else 0,
                          "beginTick": begin, "endTick": end, "stopTick": stop, "degrees": False, "moves": moves}}
    # Kindle, gathering: both hands cupped before the chest, rising and settling as the light gathers.
    cast = []
    for tick, lift in ((0, -1.10), (10, -1.28), (20, -1.10)):
        cast += [(tick, "rightArm", "pitch", lift), (tick, "rightArm", "yaw", -0.32), (tick, "rightArm", "roll", 0.12),
                 (tick, "leftArm", "pitch", lift), (tick, "leftArm", "yaw", 0.32), (tick, "leftArm", "roll", -0.12)]
    # Kindle, release: the casting hand opens upward and outward, then falls back.
    release = [(0, "rightArm", "pitch", -1.20), (3, "rightArm", "pitch", -2.05), (8, "rightArm", "pitch", -1.30),
               (0, "rightArm", "yaw", -0.30), (3, "rightArm", "yaw", 0.05), (8, "rightArm", "yaw", -0.10),
               (0, "rightArm", "roll", 0.10), (3, "rightArm", "roll", 0.28), (8, "rightArm", "roll", 0.10),
               (0, "leftArm", "pitch", -1.10), (3, "leftArm", "pitch", -0.55), (8, "leftArm", "pitch", -0.30),
               (0, "head", "pitch", 0.0), (3, "head", "pitch", -0.12), (8, "head", "pitch", 0.0)]
    return {"kindle_cast": clip("kindle_cast", True, 0, 20, 24, cast),
            "kindle_release": clip("kindle_release", False, 0, 8, 12, release)}


def book_json():
    return {"name": f"book.{MOD}.{BOOK}.name", "tooltip": f"book.{MOD}.{BOOK}.tooltip",
            "description": f"book.{MOD}.{BOOK}.description", "model": rid(BOOK), "generate_book_item": True,
            "creative_tab": "modonomicon:modonomicon", "display_mode": "node", "page_display_mode": "double_page",
            "show_recently_unlocked": True}


def node_id(state):
    return rid(f"concordance/first_light_{state}")


def condition_json(state):
    if state is None:
        return {"type": "modonomicon:none"}
    return {"type": "modonomicon:research_node_unlocked", "node_id": node_id(state),
            "tooltip": {"translate": f"book.{MOD}.{BOOK}.locked.{state}"}}


def codex_files(lang):
    """The book, its categories, entries and pages, and the research bridge, as {relative path: json}."""
    files = {}
    base = f"modonomicon/books/{BOOK}"
    files[f"{base}/book.json"] = book_json()
    lang[f"book.{MOD}.{BOOK}.name"] = "The Arcane Concordance"
    lang[f"book.{MOD}.{BOOK}.tooltip"] = "A record of what you have learned of the Principles"
    lang[f"book.{MOD}.{BOOK}.description"] = "The codex of the Arcane Concordance"
    for state, text in (("encountered", "Examine a luminous specimen first"),
                        ("observed", "Examine a luminous specimen in darkness first"),
                        ("understood", "Understand First Light first"), ("mastered", "Master First Light first")):
        lang[f"book.{MOD}.{BOOK}.locked.{state}"] = text
    for key, info in CATEGORIES.items():
        files[f"{base}/categories/{key}.json"] = {
            "name": f"book.{MOD}.{BOOK}.{key}.name", "description": f"book.{MOD}.{BOOK}.{key}.description",
            "icon": info["icon"], "sort_number": info["sort"], "display_mode": "node"}
        lang[f"book.{MOD}.{BOOK}.{key}.name"] = info["name"]
        lang[f"book.{MOD}.{BOOK}.{key}.description"] = info["description"]
    for (category, entry), info in codex().items():
        prefix = f"book.{MOD}.{BOOK}.{category}.{entry}"
        pages = []
        for index, page in enumerate(info["pages"]):
            kind, title, text = page[0], page[1], page[2]
            page_id = f"p{index}"
            page_json = {"type": f"modonomicon:{kind}", "id": page_id}
            lang[f"{prefix}.{page_id}.title"] = title
            lang[f"{prefix}.{page_id}.text"] = text
            if kind == "text":
                page_json.update({"title": f"{prefix}.{page_id}.title", "text": f"{prefix}.{page_id}.text",
                                  "show_title_separator": True})
                if len(page) > 3:
                    page_json["condition"] = condition_json(page[3])
            else:
                page_json.update({"title1": f"{prefix}.{page_id}.title", "recipe_id_1": page[3],
                                  "text": f"{prefix}.{page_id}.text"})
            pages.append(page_json)
        files[f"{base}/entries/{category}/{entry}.json"] = {
            "type": "modonomicon:content", "id": rid(f"{category}/{entry}"), "category": rid(category),
            "x": info["x"], "y": info["y"], "name": f"{prefix}.name", "description": f"{prefix}.description",
            "icon": info["icon"],
            "background": {"sprite": "modonomicon:modonomicon/themes/default/node/entry_backgrounds/"
                                     + ENTRY_BACKGROUNDS[info["condition"]], "width": 26, "height": 26},
            "condition": condition_json(info["condition"]), "pages": pages}
        lang[f"{prefix}.name"] = info["name"]
        lang[f"{prefix}.description"] = info["description"]
    research = "modonomicon/research/concordance"
    files[f"{research}/facts.json"] = [{"id": node_id(state)} for state in RESEARCH_STAGES]
    files[f"{research}/hooks.json"] = [
        {"id": rid(f"concordance/first_light_{state}_hook"), "trigger_type": "modonomicon:advancement",
         "trigger_target": rid(research_advancement("first_light", state)), "fact_id": node_id(state)}
        for state in RESEARCH_STAGES]
    files[f"{research}/nodes.json"] = [{"id": node_id(state), "required_facts": [node_id(state)]}
                                       for state in RESEARCH_STAGES]
    for state in RESEARCH_STAGES:
        lang[f"research_node.{MOD}.concordance.first_light_{state}"] = f"First Light: {state}"
    return files


ADVANCEMENT_TEXT = {
    "encountered": ("A Glow of Its Own", "Examine a luminous specimen", "task"),
    "observed": ("Against the Dark", "Examine a luminous specimen in darkness", "task"),
    "understood": ("First Light", "Understand Radiance: study at a bench, or examine three specimens in the dark",
                   "goal"),
    "mastered": ("Lamplighter", f"Master First Light: Kindle light in {MASTERY_CHUNKS} different chunks", "challenge"),
}


def advancements(data, write, lang):
    """The Concordance tab: its root appears with the first luminous specimen; the rest are awarded by the server when
    research advances (Java: ConcordanceProgress.award), and Modonomicon turns them into the codex's research facts."""
    folder = data / MOD / "advancement"
    write(folder / "concordance.json", {
        "display": {"icon": {"id": "minecraft:amethyst_shard"},
                    "title": {"translate": f"advancements.{MOD}.concordance.title"},
                    "description": {"translate": f"advancements.{MOD}.concordance.description"},
                    "frame": "task", "show_toast": True, "announce_to_chat": False,
                    "background": "minecraft:block/amethyst_block"},
        "criteria": {"has_specimen": {"trigger": "minecraft:inventory_changed",
                                      "conditions": {"items": [{"items": list(SPECIMENS)}]}}},
        "requirements": [["has_specimen"]]})
    lang[f"advancements.{MOD}.concordance.title"] = "The Arcane Concordance"
    lang[f"advancements.{MOD}.concordance.description"] = "Find something that holds its own light"
    parent = "concordance"
    for state in RESEARCH_STAGES:
        key = research_advancement("first_light", state)
        title, description, frame = ADVANCEMENT_TEXT[state]
        icon = {"encountered": "minecraft:amethyst_shard", "observed": "minecraft:glow_lichen",
                "understood": f"{MOD}:initiate_wand", "mastered": f"{MOD}:kindled_lantern"}[state]
        write(folder / f"{key}.json", {
            "parent": rid(parent),
            "display": {"icon": {"id": icon}, "title": {"translate": f"advancements.{MOD}.{key}.title"},
                        "description": {"translate": f"advancements.{MOD}.{key}.description"}, "frame": frame,
                        "show_toast": True, "announce_to_chat": frame != "task"},
            "criteria": {"done": {"trigger": "minecraft:impossible"}}})
        lang[f"advancements.{MOD}.{key}.title"] = title
        lang[f"advancements.{MOD}.{key}.description"] = description
        parent = key
    write(folder / "concordance_first_kindle.json", {
        "parent": rid(research_advancement("first_light", "understood")),
        "display": {"icon": {"id": "minecraft:glowstone_dust"},
                    "title": {"translate": f"advancements.{MOD}.concordance_first_kindle.title"},
                    "description": {"translate": f"advancements.{MOD}.concordance_first_kindle.description"},
                    "frame": "task", "show_toast": True, "announce_to_chat": False},
        "criteria": {"done": {"trigger": "minecraft:impossible"}}})
    lang[f"advancements.{MOD}.concordance_first_kindle.title"] = "Let There Be"
    lang[f"advancements.{MOD}.concordance_first_kindle.description"] = "Cast Kindle with an instrument"


# Sounds (tools/concordance_sounds.py draws the .ogg files): event -> subtitle.
SOUND_EVENTS = {
    "concordance.kindle_gather": "Light gathers",
    "concordance.kindle": "Light kindles",
    "concordance.examine": "Specimen examined",
    "concordance.study_complete": "Study completes",
    "concordance.lantern_ignite": "Lantern kindles",
    "concordance.lantern_snuff": "Lantern goes out",
}


def sounds():
    return {event: {"subtitle": f"subtitles.{MOD}.{event}",
                    "sounds": [{"name": f"{MOD}:concordance/{event.split('.', 1)[1]}", "attenuation_distance": 16}]}
            for event in SOUND_EVENTS}


MESSAGES = {
    "examine.bright": "The %s's glow is lost in this light. Examine it somewhere dark.",
    "examine.dark": "In the dark, the %s glows with its own light.",
    "examine.known": "You have already observed the %s in the dark.",
    "research.encountered": "Research: %s encountered",
    "research.observed": "Research: %s observed",
    "research.understood": "Research: %s understood",
    "research.mastered": "Research: %s mastered",
    "kindle.no_space": "Nothing there to light: Kindle needs open air within %s blocks",
    "kindle.not_allowed": "You may not change that block",
    "focus": "Focus %s / %s",
    "lantern.empty": "The lantern has no Radiance left",
    "lantern.lit": "Lantern lit: %s Radiance",
    "lantern.out": "Lantern put out: %s Radiance",
    "bench.busy": "The bench is already studying",
    "bench.no_specimen": "Put a luminous specimen in the bench",
    "bench.not_specimen": "That is not a luminous specimen",
    "bench.nothing_to_learn": "There is nothing more to learn from that specimen",
    "bench.unknown_research": "You have not learned this working yet (the codex says how)",
    "bench.no_lantern": "Put a lantern in the work slot",
    "bench.no_kindled_lantern": "Put a Kindled Lantern in the work slot",
    "bench.no_amethyst": "Kindling a lantern needs an amethyst shard",
    "bench.overfull": "That would overfill the lantern",
    "bench.no_focus": "You need %s Focus",
    "bench.study_done": "Study of the %s complete",
    "bench.notes_waiting": "Your notes from the bench: %s",
    "bench.cancelled": "Study cancelled; nothing was used",
    "bench.disabled": "The Concordance is switched off on this server",
    "instrument.needed": "Hold a Concordance instrument in your main hand",
    "no_invocations": "You understand no invocation yet: examine luminous specimens in the dark, or study one at a "
                      "Lampwright's Bench",
    "disabled": "The Concordance is switched off on this server",
}

SCREEN_TEXT = {
    "bench.study": "Study",
    "bench.cancel": "Cancel",
    "bench.kindle": "Kindle",
    "bench.infuse": "Infuse",
    "bench.channel": "Channel",
    "bench.specimen": "Specimen",
    "bench.work": "Work",
    "bench.progress": "Studying: %s%%",
    "bench.idle": "Idle",
    "bench.charge": "Radiance: %s / %s",
    "bench.help": "Hover a button to see what it needs",
    "focus.hud": "Focus %s/%s",
    "config.title": "Jugcraft: Arcane Concordance",
    "config.hud": "Show the Focus bar",
    "config.hud.tooltip": "Shows your Focus above the hotbar while you hold a Concordance instrument",
    "config.reduced_motion": "Reduced motion",
    "config.reduced_motion.tooltip": "Fewer and calmer particles from Kindled light and the bench",
}

TOOLTIPS = {
    "specimen": "Luminous specimen: sneak and use to examine it",
    "lantern.charge": "Radiance: %s / %s",
    "lantern.lit": "Lit",
    "lantern.unlit": "Put out",
    "jade.study": "Studying: %s%%",
    "jade.notes": "Notes waiting for their owner",
}


def lang_entries(lang):
    for item, info in ITEMS.items():
        lang[f"item.{MOD}.{item}"] = info["name"]
        lang[f"tooltip.{MOD}.{item}"] = info["tooltip"]
    for block, info in {**BLOCKS, **ITEMLESS_BLOCKS}.items():
        lang[f"block.{MOD}.{block}"] = info["name"]
    lang[f"container.{MOD}.lampwright_bench"] = BLOCKS["lampwright_bench"]["name"]
    for key, info in INVOCATIONS.items():
        lang[f"spell.{MOD}.{key}.name"] = info["name"]
        lang[f"spell.{MOD}.{key}.description"] = info["description"]
    for key, info in RESEARCH.items():
        lang[f"research.{MOD}.{key}"] = info["name"]
    for key, info in PRINCIPLES.items():
        lang[f"principle.{MOD}.{key}"] = info["name"]
    for key, info in RESOURCES.items():
        lang[f"resource.{MOD}.{key}"] = info["name"]
    for key, text in MESSAGES.items():
        lang[f"message.{MOD}.concordance.{key}"] = text
    for key, text in SCREEN_TEXT.items():
        lang[f"screen.{MOD}.concordance.{key}"] = text
    for key, text in TOOLTIPS.items():
        lang[f"tooltip.{MOD}.concordance.{key}"] = text
    for event, subtitle in SOUND_EVENTS.items():
        lang[f"subtitles.{MOD}.{event}"] = subtitle
    # Spell Engine names a missing "item" through its tag's translation key; Jugcraft's casting gate uses these two
    # pseudo-tags so its HUD says what is missing (Java: ConcordanceSpells.MISSING_*).
    lang[f"tag.item.{MOD}.concordance.focus"] = "Focus"
    lang[f"tag.item.{MOD}.concordance.instrument"] = "Concordance instrument"
    lang[f"config.jade.plugin_{MOD}.lampwright_bench"] = "Lampwright's Bench"
    lang[f"key.{MOD}.concordance_config"] = "Concordance settings"


def bench_model():
    """The Lampwright's Bench: a small oak desk with brass rails, a specimen dish and a hinged lens on a brass arm,
    real-life desk sized (one block, a little lower than a crafting table). Faces are drawn by
    tools/concordance_art.py. The lens is emissive while the bench works."""
    def box(f, t, tex, emissive=False):
        element = {"from": f, "to": t, "faces": {d: {"texture": f"#{tex}"} for d in
                                                ("north", "east", "south", "west", "up", "down")}}
        if emissive:
            element["light_emission"] = 15
        return element
    elements = [
        box([0, 12, 0], [16, 14, 16], "top"),            # desk top
        box([1, 0, 1], [3, 12, 3], "wood"),              # legs
        box([13, 0, 1], [15, 12, 3], "wood"),
        box([1, 0, 13], [3, 12, 15], "wood"),
        box([13, 0, 13], [15, 12, 15], "wood"),
        box([3, 9, 1.5], [13, 11, 2.5], "brass"),        # apron rails
        box([3, 9, 13.5], [13, 11, 14.5], "brass"),
        box([3, 14, 9], [8, 15, 14], "brass"),           # specimen dish
        box([11, 14, 3], [13, 22, 5], "brass"),          # lens arm post
        box([6.5, 21, 3.5], [12.5, 22, 4.5], "brass"),   # lens arm
    ]
    return elements


def write_all(write, assets, data, lang, condition, self_drop):
    lang_entries(lang)
    # Items.
    write(assets / "models" / "item" / "initiate_wand.json",
          {"parent": "minecraft:item/handheld", "textures": {"layer0": rid("item/initiate_wand")}})
    write(assets / "items" / "initiate_wand.json", {"model": {"type": "minecraft:model", "model": rid("item/initiate_wand")}})
    for model in ("kindled_lantern", "kindled_lantern_lit"):
        write(assets / "models" / "item" / f"{model}.json",
              {"parent": "minecraft:item/generated", "textures": {"layer0": rid(f"item/{model}")}})
    write(assets / "items" / "kindled_lantern.json", {"model": {
        "type": "minecraft:condition", "property": "minecraft:has_component", "component": rid("lantern_lit"),
        "on_true": {"type": "minecraft:model", "model": rid("item/kindled_lantern_lit")},
        "on_false": {"type": "minecraft:model", "model": rid("item/kindled_lantern")}}})
    # The codex book's model (Modonomicon reads book.json "model" as an item model definition).
    write(assets / "models" / "item" / f"{BOOK}.json",
          {"parent": "minecraft:item/generated", "textures": {"layer0": rid(f"item/{BOOK}")}})
    write(assets / "items" / f"{BOOK}.json", {"model": {"type": "minecraft:model", "model": rid(f"item/{BOOK}")}})
    # The bench.
    textures = {"particle": rid("block/lampwright_bench_wood"), "top": rid("block/lampwright_bench_top"),
                "wood": rid("block/lampwright_bench_wood"), "brass": rid("block/lampwright_bench_brass"),
                "lens": rid("block/lampwright_bench_lens")}
    elements = bench_model()
    lens = {"from": [6, 18, 2], "to": [10, 22, 3], "faces": {d: {"texture": "#lens"} for d in
                                                              ("north", "east", "south", "west", "up", "down")}}
    lit_lens = dict(lens, light_emission=15)
    write(assets / "models" / "block" / "lampwright_bench.json",
          {"parent": "minecraft:block/block", "textures": textures, "elements": elements + [lens]})
    write(assets / "models" / "block" / "lampwright_bench_working.json",
          {"parent": "minecraft:block/block", "textures": textures, "elements": elements + [lit_lens]})
    variants = {}
    for facing, y in (("north", 0), ("east", 90), ("south", 180), ("west", 270)):
        for working in ("false", "true"):
            model = rid("block/lampwright_bench_working" if working == "true" else "block/lampwright_bench")
            variants[f"facing={facing},working={working}"] = {"model": model, **({"y": y} if y else {})}
    write(assets / "blockstates" / "lampwright_bench.json", {"variants": variants})
    write(assets / "items" / "lampwright_bench.json",
          {"model": {"type": "minecraft:model", "model": rid("block/lampwright_bench")}})
    write(data / "loot_table" / "blocks" / "lampwright_bench.json", self_drop("lampwright_bench"))
    # The Kindled mote: no model is drawn (it renders nothing); particles show it.
    write(assets / "models" / "block" / "lumen_mote.json", {"textures": {"particle": rid("block/lampwright_bench_lens")}})
    write(assets / "blockstates" / "lumen_mote.json",
          {"variants": {"": {"model": rid("block/lumen_mote")}}})
    # Recipes (vanilla crafting; the bench's own workings are Concordance data below).
    write(data / "recipe" / "initiate_wand.json", {
        "fabric:load_conditions": condition(FEATURE), "type": "minecraft:crafting_shaped", "category": "equipment",
        "pattern": ["  A", " S ", "C  "],
        "key": {"A": "minecraft:amethyst_shard", "S": "minecraft:stick", "C": "minecraft:copper_ingot"},
        "result": {"id": rid("initiate_wand"), "count": 1}})
    write(data / "recipe" / "lampwright_bench.json", {
        "fabric:load_conditions": condition(FEATURE), "type": "minecraft:crafting_shaped", "category": "misc",
        "pattern": ["CAC", "PPP", "S S"],
        "key": {"C": "minecraft:copper_ingot", "A": "minecraft:amethyst_shard", "P": "#minecraft:planks",
                "S": "minecraft:stick"},
        "result": {"id": rid("lampwright_bench"), "count": 1}})
    write(data / "recipe" / f"{BOOK}.json", {
        "fabric:load_conditions": condition(FEATURE), "type": "minecraft:crafting_shapeless", "category": "misc",
        "ingredients": ["minecraft:book", "minecraft:amethyst_shard"],
        "result": {"id": "modonomicon:modonomicon", "count": 1, "components": {"modonomicon:book_id": rid(BOOK)}}})


def write_data(write, res):
    """Data outside the generator's usual folders, all under data/jugcraft/ (cleaned by generate_material_data.main
    through GENERATED_DIRS) and the client assets that only Spell Engine, PAL and LambDynamicLights read."""
    data = res / "data" / MOD
    assets = res / "assets" / MOD
    for key, info in RESEARCH.items():
        write(data / "concordance" / "research" / f"{key}.json", research_json(key, info))
    for key, info in INVOCATIONS.items():
        write(data / "concordance" / "invocation" / f"{key}.json", invocation_json(key, info))
        write(data / "spell" / f"{key}.json", spell_json(key))
    for key, info in WORKINGS.items():
        write(data / "concordance" / "working" / f"{key}.json", dict({"schema": SCHEMA}, **info))
    for item in spell_opt_outs():
        write(data / "spell_assignments" / f"{item}.json", {"access": "NONE", "access_param": ""})
    # Instruments resolve casts for the Concordance spell tag only; which of its spells a player has is the
    # Concordance's own container source (Java: ConcordanceSpells), so only understood invocations show.
    for item in INSTRUMENTS:
        write(data / "spell_assignments" / f"{item}.json", {"access": "TAG", "access_param": SPELL_TAG})
    lang = {}
    for path, content in codex_files(lang).items():
        write(data / path, content)
    for name, clip in player_animations().items():
        write(assets / "player_animations" / f"{name}.json", clip)
    # LambDynamicLights (optional, client): a lit Kindled Lantern glows in hand. Without the mod nothing reads this.
    write(assets / "dynamiclights" / "item" / "kindled_lantern.json",
          {"match": {"items": rid("kindled_lantern"), "components": {rid("lantern_lit"): {}}},
           "luminance": TRAIL_LIGHT})
    return lang


def tags(tags):
    for ref in SPECIMENS:
        tags.add("item", SPECIMEN_TAG, ref)
    for item in INSTRUMENTS:
        tags.add("item", INSTRUMENT_TAG, rid(item))
    tags.add("block", "minecraft:mineable/axe", rid("lampwright_bench"))
    for key in INVOCATIONS:
        tags.add("spell", SPELL_TAG, rid(key))


def recipe_view():
    """The bench's workings for JEI (client/compat/JugcraftJeiPlugin): public crafts, not secret research."""
    rows = [{"kind": "craft", "in": [[WORKINGS["kindle_lantern"]["work"], 1], [WORKINGS["kindle_lantern"]["specimen"], 1]],
             "out": [WORKINGS["kindle_lantern"]["result"], 1], "radiance": LANTERN_START}]
    for ref, value in SPECIMENS.items():
        rows.append({"kind": "infuse", "in": [[rid("kindled_lantern"), 1], [ref, 1]], "out": [rid("kindled_lantern"), 1],
                     "radiance": value})
    return {"block": rid("lampwright_bench"), "recipes": rows}

"""Star-bred lines, phase 1 (docs/features/star-bred-lines.md; the plan is docs/plans/star-bred-lines-2026-10-28.md): the
numbers of lineage/Lineage.java, Trait.java and Gift.java, a model of what they make, and the resources they need: the
Astrawand's item and hand models, its flourish, its recipe and its words; the kinds that carry a line
(#jugcraft:star_bred); what Yield gives more of (#jugcraft:line_produce); and the coats an Angora goat and a Woolly Coat
pig are shorn for (loot_table/lineage/).

Called from tools/agriculture_data.py (assets, loot, recipes, tags); the Astrawand's icon and model texture are
tools/lineage_art.py's. tools/check_mod_data.py (check_lineage) holds the Java to these numbers and the resources to
these functions. Run it to print the model's tables (python3 tools/lineage.py).

Units: grades 0 to 5, a trait's number 0 to 10, ticks (20 a second, 24000 a game day).
"""
from fractions import Fraction

MOD = "jugcraft"
TRAITS = ["yield", "vigour", "fertility", "hardiness"]  # Java: Trait, in this order
TRAIT_NAMES = {"yield": "Yield", "vigour": "Vigour", "fertility": "Fertility", "hardiness": "Hardiness"}
# The kinds that carry a line (Java: Lineage.STAR_BRED, the entity type tag written here).
KINDS = ["minecraft:cow", "minecraft:mooshroom", "minecraft:sheep", "minecraft:pig", "minecraft:chicken", "minecraft:goat",
         "minecraft:rabbit", "jugcraft:turkey"]
# Each gift: its name, its kinds (Java: Gift) and what it does, in words for the record.
GIFTS = {
    "double_yolk": ("Double Yolk", ["minecraft:chicken", "jugcraft:turkey"], "1 laid egg in 3 comes as two"),
    "moonfleece": ("Moonfleece", ["minecraft:sheep"], "sheared at night under the open sky, one more wool"),
    "angora": ("Angora", ["minecraft:goat"], "can be sheared for 1 to 2 white wool, then regrows its coat over a game day"),
    "woolly_coat": ("Woolly Coat", ["minecraft:pig"], "can be sheared for 1 to 2 brown wool once a game day"),
    "lucky_foot": ("Lucky Foot", ["minecraft:rabbit"], "always drops its foot"),
    "heavy_hide": ("Heavy Hide", ["minecraft:cow", "minecraft:mooshroom"], "one more leather"),
}
# The trait each pattern's sign raises (Java: Lineage.SIGNS); every pattern in data/jugcraft/concordance/pattern has one.
SIGNS = {"jugcraft:full_moon": "yield", "jugcraft:new_moon": "fertility", "jugcraft:lantern_star": "vigour",
         "jugcraft:echo_comet": "hardiness", "jugcraft:winter_crown": "hardiness", "jugcraft:harvest_moon": "yield"}
WAKING = "jugcraft:echo_comet"  # also wakes a copy of one of the kind's gifts

VANILLA_UP_TO = 4
MAX_GRADE = 5
WILD_WEIGHTS = [25, 45, 30]  # grades 0 to 2, in a hundred: every wild trait is at most 4, so exactly vanilla
WILD_CARRIER = 6
YIELD_ONE, YIELD_TWO = 6, 9
EGG_ONE, EGG_TWO = 4, 2
DOUBLE_YOLK = 3
HEALTH_PER_POINT = 0.10
SPEED_PER_POINT = 0.025
GROW_PER_POINT = 0.04
REST_PER_POINT = 0.05
GROWING = 24000
REST = 6000  # vanilla's rest after breeding
SPARE = {"thorns": 5, "freezing": 6, "falling": 7, "drowning": 8, "burning": 9, "lightning": 10}
SPARE_WORDS = {"thorns": "cactus and berry bushes", "freezing": "freezing", "falling": "falling", "drowning": "drowning",
               "burning": "burning (not lava or fire blocks)", "lightning": "lightning's damage"}
REGROW_TICKS = 24000
NIGHT_FROM, NIGHT_TO = 13000, 23000
READ_TICKS = 10

# What Yield gives more of at death: each kind's meat, raw or cooked (Java: Lineage.PRODUCE).
PRODUCE = ["minecraft:beef", "minecraft:cooked_beef", "minecraft:porkchop", "minecraft:cooked_porkchop", "minecraft:mutton",
           "minecraft:cooked_mutton", "minecraft:chicken", "minecraft:cooked_chicken", "minecraft:rabbit",
           "minecraft:cooked_rabbit", f"{MOD}:raw_turkey"]
# A shorn coat: its wool and how many (Java: Lineage.COATS).
COATS = {"angora": ("minecraft:white_wool", 1, 2), "woolly_coat": ("minecraft:brown_wool", 1, 2)}

ITEM = "astrawand"
DISPLAY = "Astrawand"
TOOLTIP = "Point it at a farm animal and use it to read its line"
# The diagonal, as the icon: the star at the top right, the shaft, the gold grip at the bottom left.
RECIPE = {"pattern": ["  A", " B ", "G  "],
          "key": {"A": "minecraft:amethyst_shard", "B": "minecraft:bone", "G": "minecraft:gold_ingot"}}
FLOURISH = "astrawand_flourish"

MESSAGES = {
    "wild": "%1$s: a wild line",
    "bred": "%1$s: generation %2$s · %3$s",
    "sign": "Sign: %s",
    "no_sign": "No birth sign",
    "trait": "%s %s",
    "trait_true": "%s %s ✦",
    "traits": "%s · %s · %s · %s",
    "gifts": "Gifts: %s",
    "no_gifts": "No gifts showing",
    "unseen": "Carries something unseen",
    "true_key": "✦ breeds true",
    "no_line": "%s carries no line the Astrawand can read",
}


def rid(path):
    return path if ":" in path else f"{MOD}:{path}"


# ---------------------------------------------------------------- the model

def wild_grade():
    """The chance of each wild grade, as fractions."""
    return {grade: Fraction(weight, 100) for grade, weight in enumerate(WILD_WEIGHTS)}


def wild_value():
    """The chance of each wild trait number (two wild copies added)."""
    grades = wild_grade()
    out = {}
    for a, pa in grades.items():
        for b, pb in grades.items():
            out[a + b] = out.get(a + b, 0) + pa * pb
    return out


def wild_past_vanilla():
    """The chance a wild animal's trait is past vanilla (5 or more)."""
    return sum(p for value, p in wild_value().items() if value > VANILLA_UP_TO)


def points(value):
    return max(0, value - VANILLA_UP_TO)


def extra(value):
    return 2 if value >= YIELD_TWO else 1 if value >= YIELD_ONE else 0


def eggs_expected(value, double_yolk):
    """The eggs one laying gives on average."""
    chance = Fraction(1, EGG_TWO) if value >= YIELD_TWO else Fraction(1, EGG_ONE) if value >= YIELD_ONE else 0
    return 1 + chance + (Fraction(1, DOUBLE_YOLK) if double_yolk else 0)


def growing(value):
    return round(GROWING * (1 - GROW_PER_POINT * points(value)))


def rest(value):
    return round(REST * (1 - REST_PER_POINT * points(value)))


def spared(value):
    return [harm for harm, at in SPARE.items() if value >= at]


def effects_table():
    """The record's table: what each number does."""
    rows = ["| Number | Yield | Vigour (health, speed, growing time) | Fertility (rest) | Hardiness (spares this, and each harm above) |",
            "|---|---|---|---|---|"]
    for value in range(VANILLA_UP_TO, 11):
        p = points(value)
        yield_words = f"+{extra(value)} produce; eggs ×{float(eggs_expected(value, False)):.2f}" if extra(value) else "vanilla"
        vigour = f"+{round(HEALTH_PER_POINT * p * 100)}%, +{SPEED_PER_POINT * p * 100:g}%, {growing(value)} ticks" if p else "vanilla"
        fertility = f"{rest(value)} ticks" if p else "vanilla (6000 ticks)"
        harms = [SPARE_WORDS[h] for h, at in SPARE.items() if at == value]
        hardiness = ", ".join(harms) if harms else "nothing"
        rows.append(f"| {value if value > VANILLA_UP_TO else '0 to 4'} | {yield_words} | {vigour} | {fertility} | {hardiness} |")
    return "\n".join(rows)


def wild_table():
    """The record's table: how often a wild trait has each number."""
    values = wild_value()
    numbers = range(2 * (len(WILD_WEIGHTS) - 1) + 1)
    rows = ["| Wild number | " + " | ".join(str(v) for v in numbers) + " |", "|---|" + "---|" * len(numbers)]
    rows.append("| Chance | " + " | ".join(f"{float(values.get(v, 0)) * 100:.1f}%" for v in numbers) + " |")
    return "\n".join(rows)


def risings_to_top(start=len(WILD_WEIGHTS) - 1):
    """How many risings of a trait's pattern, at the fewest, before a line of 10, by breeding alone, from wild parents
    whose best copy is graded `start` (2, the best wild grade). A sign raises the weaker copy by one, so a new grade comes
    only from a baby whose copies already match the best the line has: 2+2 under a rising gives 3+2; two 3+2s give 3+3
    one time in four, which under the next rising gives 4+3; and so on to 5+4, and two 5+4s, bred on any day, give 5+5
    one time in four. Each rising lifts the line's best grade by one."""
    return MAX_GRADE - start


# ---------------------------------------------------------------- resources

def flourish_clip():
    """The Astrawand's conducting flourish: the right arm lifts the baton, beats two small strokes across, and lowers.
    About a second; plays once."""
    def move(tick, part, axis, value):
        return {"tick": tick, "easing": "EASEINOUTQUAD", "turn": 0, part: {axis: value}}
    moves = []
    for tick, pitch, yaw in ((0, -0.15, 0.0), (5, -1.85, -0.25), (9, -1.55, 0.35), (13, -1.90, -0.30), (17, -1.60, 0.20),
                             (24, -0.15, 0.0)):
        moves += [move(tick, "rightArm", "pitch", pitch), move(tick, "rightArm", "yaw", yaw)]
    moves += [move(0, "head", "pitch", 0.0), move(6, "head", "pitch", -0.12), move(24, "head", "pitch", 0.0)]
    return {"name": FLOURISH, "author": "Jugcraft", "description": "Star-bred lines: the Astrawand's flourish",
            "emote": {"isLoop": "false", "returnTick": 0, "beginTick": 0, "endTick": 24, "stopTick": 28, "degrees": False,
                      "moves": moves}}


# The hand model: a baton built along the vertical through (8, 8), every part turned -45 degrees about z round the middle
# so it lies on the diagonal as the icon does (hand end bottom left). Each part names a band of its texture
# (tools/lineage_art.py MODEL_BANDS): stem, gold, bright gold, star.
HAND_PARTS = [
    ("pommel", "gold", (7.0, -3.0, 7.0), (9.0, -1.0, 9.0)),
    ("grip", "gold", (7.25, -1.0, 7.25), (8.75, 3.0, 8.75)),
    ("band", "bright", (7.0, 0.5, 7.0), (9.0, 1.5, 9.0)),
    ("collar", "bright", (7.1, 3.0, 7.1), (8.9, 3.75, 8.9)),
    ("shaft", "stem", (7.4, 3.75, 7.4), (8.6, 11.0, 8.6)),
    ("tip", "stem", (7.55, 11.0, 7.55), (8.45, 16.5, 8.45)),
    ("star_across", "star", (6.5, 17.25, 7.75), (9.5, 18.25, 8.25)),
    ("star_up", "star", (7.5, 16.25, 7.75), (8.5, 19.25, 8.25)),
    ("star_through", "star", (7.5, 17.25, 6.5), (8.5, 18.25, 9.5)),
]
# Each band is four columns of the model texture, lightest first; faces lit from the top left take the lighter.
BAND_START = {"stem": 0, "gold": 4, "bright": 8, "star": 12}
FACE_SHADE = {"up": 0, "west": 0, "north": 1, "south": 2, "east": 3, "down": 3}
# Vanilla's handheld transforms, for a model on the same diagonal.
HANDHELD = {
    "thirdperson_righthand": {"rotation": [0, -90, 55], "translation": [0, 4.0, 0.5], "scale": [0.85, 0.85, 0.85]},
    "thirdperson_lefthand": {"rotation": [0, 90, -55], "translation": [0, 4.0, 0.5], "scale": [0.85, 0.85, 0.85]},
    "firstperson_righthand": {"rotation": [0, -90, 25], "translation": [1.13, 3.2, 1.13], "scale": [0.68, 0.68, 0.68]},
    "firstperson_lefthand": {"rotation": [0, 90, -25], "translation": [1.13, 3.2, 1.13], "scale": [0.68, 0.68, 0.68]},
}


def hand_model():
    elements = []
    for name, band, low, high in HAND_PARTS:
        faces = {}
        for face, shade in FACE_SHADE.items():
            column = BAND_START[band] + shade
            faces[face] = {"uv": [column, 0, column + 1, 16], "texture": "#baton"}
        elements.append({"name": name, "from": list(low), "to": list(high),
                         "rotation": {"angle": -45, "axis": "z", "origin": [8, 8, 8]}, "faces": faces})
    return {"textures": {"particle": rid(f"item/{ITEM}"), "baton": rid(f"item/{ITEM}_model")},
            "display": HANDHELD, "elements": elements}


def items():
    """Every item the star-bred lines add (registered in Java by Lineage.register)."""
    return [ITEM]


def assets(root, write, lang):
    models = root / "models" / "item"
    write(models / f"{ITEM}.json", {"parent": "minecraft:item/handheld", "textures": {"layer0": rid(f"item/{ITEM}")}})
    write(models / f"{ITEM}_in_hand.json", hand_model())
    write(root / "items" / f"{ITEM}.json", {"model": {
        "type": "minecraft:select", "property": "minecraft:display_context",
        "cases": [{"when": ["gui", "ground", "fixed", "on_shelf"], "model": {"type": "minecraft:model", "model": rid(f"item/{ITEM}")}}],
        "fallback": {"type": "minecraft:model", "model": rid(f"item/{ITEM}_in_hand")}}})
    write(root / "player_animations" / f"{FLOURISH}.json", flourish_clip())
    lang[f"item.{MOD}.{ITEM}"] = DISPLAY
    lang[f"tooltip.{MOD}.{ITEM}"] = TOOLTIP
    for trait in TRAITS:
        lang[f"lineage.{MOD}.trait.{trait}"] = TRAIT_NAMES[trait]
    for gift, (name, _kinds, _words) in GIFTS.items():
        lang[f"lineage.{MOD}.gift.{gift}"] = name
    for key, text in MESSAGES.items():
        lang[f"message.{MOD}.lineage.{key}"] = text


def loot(out, write):
    """The coats (out = data/jugcraft/loot_table)."""
    for gift, (wool, low, high) in COATS.items():
        write(out / "lineage" / f"{gift}.json", {"pools": [{"rolls": 1, "entries": [{
            "type": "minecraft:item", "name": wool,
            "modifier": [{"type": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": low, "max": high}}]}]}],
            "random_sequence": rid(f"lineage/{gift}")})


def recipes(out, write, conditions):
    write(out / f"{ITEM}.json", {"fabric:load_conditions": conditions(), "type": "minecraft:crafting_shaped",
                                 "category": "equipment", "pattern": RECIPE["pattern"], "key": RECIPE["key"],
                                 "result": {"id": rid(ITEM), "count": 1}})


def tags(tags):
    for kind in KINDS:
        tags.add("entity_type", f"{MOD}:star_bred", kind)
    for item in PRODUCE:
        tags.add("item", f"{MOD}:line_produce", item)


if __name__ == "__main__":
    print(wild_table())
    print()
    print(f"A wild trait past vanilla (5 or more): {float(wild_past_vanilla()) * 100:.1f}%")
    print(f"Risings of its pattern before a line of 10, from the best wild copies (2): {risings_to_top()}")
    print()
    print(effects_table())

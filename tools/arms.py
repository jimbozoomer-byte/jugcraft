"""Arms, batch 42 (docs/features/arms.md): longswords, greatswords, rapiers, flanged maces, war hammers, glaives,
halberds, spears and lances in bronze and steel; Arms II, batch 45: daggers, sabres, estocs, battle axes, flails,
scythes, quarterstaves and pikes, each with a trait of its own (TRAITS); and Arms III, batch 46: two-handed swings
for the heavy arms (TWO_HANDED) and zweihanders, mauls, executioner's swords and bills.

After studying Epic Knights (all rights reserved) and Simply Swords (Timefall Development License) for how they draw,
animate and keep their weapons cheap; none of their code, models, numbers or art is used. What carried over is the
approach: one table of weapon kinds and their traits, one sprite and one model per weapon over a shared in-hand pose
per kind, and no per-tick code. Every trait is one of 26.3's own item components (swing animation, attack range,
blocking, piercing and kinetic attacks, shield disabling), so the game runs and checks them like its own weapons.

Java: weapons/JugcraftArms.java (keep KINDS and the numbers in sync; tools/check_mod_data.py checks them). Art:
tools/arms_art.py.
"""
import math

from PIL import Image

import arms_art
import gear

MOD = "jugcraft"

# Each kind of arm. Attack damage is added to the metal's bonus (bronze 2, steel 2.5) and the hand's 1, as a sword's
# 3 is; the iron sword makes 6 at 1.6 attacks a second. Speed is the attack-speed modifier (the sword's is -2.4: 4 -
# 2.4 = 1.6 a second). swing: the attack animation and how long it lasts in ticks (a plain swing is 6). reach: the
# nearest and farthest a hit lands, in blocks (a hand reaches 3; creative adds 2); margin widens the target's hitbox.
# disable: seconds a hit stops a shield blocking (the axe's 5). wear: durability a hit costs. knockback: extra attack
# knockback. parry: the share of damage from in front that blocking with it stops (a shield stops all).
# held: how much larger than a sword it is held. pierce: it thrusts through every target in line, as the spear's
# jab does. tags: the vanilla item tags it joins (the enchantments follow).
KINDS = {
    "longsword": {"display": "Longsword", "damage": 4.0, "speed": -2.7, "swing": ("whack", 8), "reach": (0.0, 3.25),
                  "margin": 0.0, "disable": 0.0, "wear": 1, "knockback": 0.0, "parry": 0.6, "held": 1.3,
                  "tags": ["swords"], "pattern": [" # ", " # ", "#L#"],
                  "tooltip": "Use to parry: blocks 60% of the damage from in front."},
    "greatsword": {"display": "Greatsword", "damage": 7.0, "speed": -3.2, "swing": ("whack", 20), "reach": (0.0, 3.75),
                   "margin": 0.0, "disable": 2.0, "wear": 1, "knockback": 0.5, "parry": 0.0, "held": 1.7,
                   "tags": ["swords"], "pattern": [" # ", "###", "#L#"],
                   "tooltip": "Two-handed: slow, heavy sweeps with a long reach. Staggers shields."},
    "rapier": {"display": "Rapier", "damage": 1.5, "speed": -2.0, "swing": ("stab", 5), "reach": (0.0, 3.5),
               "margin": 0.125, "disable": 0.0, "wear": 1, "knockback": 0.0, "parry": 0.35, "held": 1.2,
               "tags": ["swords"], "pattern": ["  #", " # ", "L  "],
               "tooltip": "Quick thrusts. Use to parry: blocks 35% of the damage from in front."},
    "flanged_mace": {"display": "Flanged Mace", "damage": 6.0, "speed": -3.1, "swing": ("whack", 9),
                     "reach": (0.0, 3.0), "margin": 0.0, "disable": 3.0, "wear": 1, "knockback": 0.0, "parry": 0.0,
                     "held": 1.15, "tags": ["enchantable/melee_weapon", "enchantable/durability"],
                     "pattern": [" ##", " ##", "S  "], "tooltip": "Breaks a shield's guard for 3 seconds."},
    "war_hammer": {"display": "War Hammer", "damage": 8.0, "speed": -3.3, "swing": ("whack", 22),
                   "reach": (0.0, 3.0), "margin": 0.0, "disable": 5.0, "wear": 2, "knockback": 1.0, "parry": 0.0,
                   "held": 1.35, "tags": ["enchantable/melee_weapon", "enchantable/durability"],
                   "pattern": ["###", "#S#", " S "], "tooltip": "Knocks foes back and breaks a shield's guard for 5 seconds."},
    "glaive": {"display": "Glaive", "damage": 6.0, "speed": -3.1, "swing": ("whack", 18), "reach": (0.0, 4.25),
               "margin": 0.0, "disable": 0.0, "wear": 1, "knockback": 0.0, "parry": 0.0, "held": 2.0,
               "tags": ["swords"], "pattern": [" ##", " S#", "S  "],
               "tooltip": "A blade on a pole: sweeps at a long reach."},
    "halberd": {"display": "Halberd", "damage": 7.0, "speed": -3.2, "swing": ("stab", 12), "reach": (1.0, 4.5),
                "margin": 0.125, "disable": 3.0, "wear": 1, "knockback": 0.0, "parry": 0.0, "held": 2.1, "pierce": True,
                "tags": ["enchantable/melee_weapon", "enchantable/durability"], "pattern": ["###", " S#", "S  "],
                "tooltip": "Thrusts through every foe in line, at a long reach. Breaks a shield's guard for 3 seconds."},
    # The spear and the lance charge like vanilla's spears: use and hold to charge, faster with a run or a horse.
    "spear": {"display": "Spear", "pattern": ["  #", " S ", "S  "], "tags": ["spears"], "held": 1.0,
              "tooltip": "Jab, or hold use to charge with it."},
    "lance": {"display": "Lance", "pattern": ["  #", "#S#", "S  "], "tags": ["spears"], "held": 1.3,
              "tooltip": "A horseman's charge: hits harder and unhorses riders. Long reach, slow jabs."},
    # Arms II (batch 45): each has a trait (TRAITS) besides its numbers.
    "dagger": {"display": "Dagger", "damage": 1.0, "speed": -1.7, "swing": ("stab", 4), "reach": (0.0, 2.5),
               "margin": 0.0, "disable": 0.0, "wear": 1, "knockback": 0.0, "parry": 0.0, "held": 0.85,
               "tags": ["enchantable/melee_weapon", "enchantable/durability"], "pattern": [" #", "L "],
               "trait": "backstab", "tooltip": "Quick stabs at a short reach. A blow from behind deals half again as much."},
    "sabre": {"display": "Sabre", "damage": 2.0, "speed": -2.2, "swing": ("whack", 6), "reach": (0.0, 3.0),
              "margin": 0.0, "disable": 0.0, "wear": 1, "knockback": 0.0, "parry": 0.0, "held": 1.2,
              "tags": ["swords"], "pattern": [" #", " #", "L "], "trait": "saddle",
              "tooltip": "A horseman's blade: quick sweeping cuts, 3 more damage from the saddle."},
    "estoc": {"display": "Estoc", "damage": 3.0, "speed": -2.6, "swing": ("stab", 7), "reach": (0.0, 3.5),
              "margin": 0.125, "disable": 0.0, "wear": 1, "knockback": 0.0, "parry": 0.0, "held": 1.35,
              "tags": ["enchantable/melee_weapon", "enchantable/durability"], "pattern": ["  #", " # ", "#L "],
              "trait": "armor_pierce", "tooltip": "Thrusts through mail: more damage the more armor the foe wears."},
    "battle_axe": {"display": "Battle Axe", "damage": 8.0, "speed": -3.3, "swing": ("whack", 22), "reach": (0.0, 3.25),
                   "margin": 0.0, "disable": 5.0, "wear": 2, "knockback": 0.5, "parry": 0.0, "held": 1.5,
                   "tags": ["enchantable/melee_weapon", "enchantable/durability"], "pattern": ["###", "#S ", " S "],
                   "trait": "chop", "tooltip": "Two-handed. Chops wood like an axe and breaks a shield's guard for 5 seconds."},
    "flail": {"display": "Flail", "damage": 5.0, "speed": -3.0, "swing": ("whack", 10), "reach": (0.0, 3.25),
              "margin": 0.0, "disable": 0.0, "wear": 1, "knockback": 0.0, "parry": 0.0, "held": 1.2,
              "tags": ["enchantable/melee_weapon", "enchantable/durability"], "pattern": ["  #", " # ", "S  "],
              "trait": "daze", "tooltip": "A hit dazes the foe, slowing it for 2 seconds."},
    "scythe": {"display": "Scythe", "damage": 5.0, "speed": -3.0, "swing": ("whack", 18), "reach": (0.0, 4.0),
               "margin": 0.0, "disable": 0.0, "wear": 1, "knockback": 0.0, "parry": 0.0, "held": 1.9,
               "tags": ["swords"], "pattern": ["###", "  S", " S "], "trait": "reap",
               "tooltip": "Wide sweeps at a long reach. Use on ripe crops to reap them, 3 by 3, and replant."},
    "quarterstaff": {"display": "Quarterstaff", "damage": 2.0, "speed": -2.4, "swing": ("whack", 12), "reach": (0.0, 3.5),
                     "margin": 0.0, "disable": 0.0, "wear": 1, "knockback": 1.0, "parry": 0.5, "held": 1.8,
                     "tags": ["enchantable/melee_weapon", "enchantable/durability"], "pattern": ["  #", " S ", "#  "],
                     "tooltip": "Two-handed. Knocks foes back. Use to parry: blocks 50% of the damage from in front."},
    "pike": {"display": "Pike", "damage": 5.0, "speed": -3.2, "swing": ("stab", 16), "reach": (2.0, 5.0),
             "margin": 0.125, "disable": 0.0, "wear": 1, "knockback": 0.0, "parry": 0.0, "held": 2.3,
             "tags": ["enchantable/melee_weapon", "enchantable/durability"], "pattern": ["  #", " S ", "SS "],
             "trait": "riders",
             "tooltip": "The longest reach, but nothing nearer than 2 blocks. Half again as much damage to riders and mounts."},
    # Arms III (batch 46): two-handed arms, swung as TWO_HANDED says.
    "zweihander": {"display": "Zweihander", "damage": 7.5, "speed": -3.2, "swing": ("whack", 22), "reach": (0.0, 4.0),
                   "margin": 0.0, "disable": 2.0, "wear": 1, "knockback": 0.5, "parry": 0.5, "held": 2.1,
                   "tags": ["swords"], "pattern": ["  #", "## ", "L# "],
                   "tooltip": "The widest cleave. Use to guard: blocks 50% of the damage from in front."},
    "maul": {"display": "Maul", "damage": 10.0, "speed": -3.45, "swing": ("whack", 24), "reach": (0.0, 3.25),
             "margin": 0.0, "disable": 5.0, "wear": 2, "knockback": 1.5, "parry": 0.0, "held": 1.7,
             "tags": ["enchantable/melee_weapon", "enchantable/durability"], "pattern": ["###", "###", " S "],
             "trait": "quake",
             "tooltip": "The heaviest blow. Its finishing blow shakes the ground, staggering every foe close by."},
    "executioner": {"display": "Executioner's Sword", "damage": 8.0, "speed": -3.3, "swing": ("whack", 22),
                    "reach": (0.0, 3.5), "margin": 0.0, "disable": 0.0, "wear": 1, "knockback": 0.5, "parry": 0.0,
                    "held": 1.9, "tags": ["swords"], "pattern": ["## ", "## ", " L#"], "trait": "execute",
                    "tooltip": "Half again as much damage to a foe at or below 30% of its health."},
    "bill": {"display": "Bill", "damage": 6.0, "speed": -3.1, "swing": ("whack", 18), "reach": (0.0, 4.5),
             "margin": 0.0, "disable": 0.0, "wear": 1, "knockback": 0.0, "parry": 0.0, "held": 2.2,
             "tags": ["enchantable/melee_weapon", "enchantable/durability"], "pattern": [" ##", " S ", "S  "],
             "trait": "hook", "tooltip": "A hooked polearm: pulls the foes it strikes towards you and drags riders from the saddle."},
}
# Arms II traits (weapons/ArmItem.java; JugcraftArms.TRAITS). backstab: a blow landing within BACKSTAB_ANGLE degrees of
# straight behind the target's body deals BACKSTAB more (a share of the blow). saddle: SADDLE more damage while riding.
# armor_pierce: ARMOR_PIERCE more for each point of the target's armor, at most ARMOR_PIERCE_MAX. chop: mines wood as an
# axe of its metal does. daze: a hit slows the target (DAZE: ticks, Slowness amplifier). reap: use on a ripe crop to
# harvest every ripe crop within REAP_RADIUS and replant it, at REAP_WEAR durability each. riders: RIDERS more (a share)
# against anything riding or ridden.
TRAITS = {kind: info["trait"] for kind, info in KINDS.items() if "trait" in info}
BACKSTAB = 0.5
BACKSTAB_ANGLE = 70.0
SADDLE = 3.0
ARMOR_PIERCE = 0.3
ARMOR_PIERCE_MAX = 6.0
DAZE = (40, 1)
REAP_RADIUS = 1
REAP_WEAR = 1
RIDERS = 0.5
# Arms III traits. quake: the maul's finishing blow also strikes every foe within QUAKE_RADIUS blocks (and a block of the
# wielder's footing) that the cleave missed, for QUAKE_SHARE of the blow, and slows every foe there (QUAKE_DAZE: ticks,
# Slowness amplifier). execute: EXECUTE
# more (a share of the blow) against a foe at or below EXECUTE_HEALTH of its most health. hook: a hit pulls the foe
# towards the wielder at HOOK blocks a tick (less its knockback resistance) and drags a rider from the saddle.
QUAKE_RADIUS = 2.5
QUAKE_SHARE = 0.5
QUAKE_DAZE = (40, 1)
EXECUTE = 0.5
EXECUTE_HEALTH = 0.3
HOOK = 0.6

# Arms III (batch 46): two-handed swings (weapons/TwoHanded.java; client/arms/TwoHandedInput.java), after studying the
# greatsword of Fiery Combat (a Bedrock add-on; nothing of it is used). A click with one of these in the main hand
# starts a committed swing instead of vanilla's instant hit:
#   strike: the tick the blow lands, counted from the click; it is when the kind's motion lands its blow
#     (tools/arms_moves.py: MOVES[kind]["blow"] times the swing's ticks, checked to half a tick);
#   arc: how wide the blow sweeps, in degrees across the wielder's view; every foe in it within the arm's reach and
#     in sight is struck, nearest first, up to `targets`;
#   combo: how many attacks the kind's combo has (as its motion); the last is the finishing blow, FINISHER times as
#     strong.
# While a swing is in the air the wielder moves at 1 - TWO_HANDED_SLOW of their speed and cannot sprint, and a shield
# (anything that blocks) in the off hand stops a swing from starting. A click within QUEUE_TICKS of a swing's end
# waits for it; COMBO_WINDOW ticks without a click start the combo again. The blow is as strong as the attack charge
# was at the click (vanilla's rule), and each foe is struck through vanilla's own thrust attack (Player.stabAttack):
# enchantments, knockback, wear and the kind's trait.
TWO_HANDED = {
    "greatsword": {"strike": 7, "arc": 120, "targets": 4, "combo": 2},
    "war_hammer": {"strike": 8, "arc": 70, "targets": 2, "combo": 2},
    "glaive": {"strike": 6, "arc": 120, "targets": 4, "combo": 2},
    "battle_axe": {"strike": 8, "arc": 90, "targets": 3, "combo": 2},
    "scythe": {"strike": 6, "arc": 150, "targets": 5, "combo": 2},
    "quarterstaff": {"strike": 4, "arc": 100, "targets": 3, "combo": 3},
    "pike": {"strike": 5, "arc": 20, "targets": 3, "combo": 2},
    "zweihander": {"strike": 7, "arc": 140, "targets": 5, "combo": 3},
    "maul": {"strike": 9, "arc": 90, "targets": 3, "combo": 2},
    "executioner": {"strike": 8, "arc": 90, "targets": 2, "combo": 2},
    "bill": {"strike": 6, "arc": 90, "targets": 3, "combo": 2},
}
TWO_HANDED_SLOW = 0.6
FINISHER = 1.25
QUEUE_TICKS = 4
COMBO_WINDOW = 30
METALS = list(gear.GEAR_TIERS)
# The charging kinds, as Item.Properties.spear takes them, by metal: jab duration (s), charge damage multiplier,
# charge delay (s), then for unhorsing, knockback and damage the longest a charge counts (s) and the speed it needs.
# Vanilla's iron spear is (0.95, 0.95, 0.6, 2.5, 11.0, 6.75, 5.1, 11.25, 4.6) and diamond (1.05, 1.075, 0.5, 3.0,
# 10.0, 6.5, 5.1, 10.0, 4.6). The lance trades a slow jab for a harder charge that unhorses at lower speeds.
CHARGE = {
    ("spear", "bronze"): (0.95, 1.0, 0.6, 2.5, 11.0, 6.75, 5.1, 11.25, 4.6),
    ("spear", "steel"): (1.0, 1.04, 0.55, 2.75, 10.5, 6.6, 5.1, 10.6, 4.6),
    ("lance", "bronze"): (1.25, 1.3, 0.9, 3.5, 9.0, 8.0, 4.5, 14.0, 4.0),
    ("lance", "steel"): (1.3, 1.4, 0.8, 4.0, 8.5, 8.5, 4.5, 15.0, 4.0),
}
# The lance's jab adds this to the spear's (the metal's bonus) and reaches farther.
LANCE_DAMAGE = 1.0
LANCE_REACH = (2.5, 5.5)
CHARGING = ("spear", "lance")
# What a parry's blocking is like: the angle either side of straight ahead it covers (degrees; a shield's is 90), the
# delay before it blocks (s; a shield's is 0.25), and its wear: a parried hit of 3 or more costs 1 + a share of it.
PARRY_ANGLE = 60.0
PARRY_DELAY = 0.1
PARRY_WEAR = (3.0, 1.0, 0.5)
# Crafting keys besides "#", the metal's ingot.
KEYS = {"S": "minecraft:stick", "L": "minecraft:leather"}

# Vanilla's item/handheld hand poses (rotation, translation, scale) and where a vanilla sword sprite is held, in
# pixels from its centre (x right, y up).
HANDHELD = {
    "thirdperson_righthand": ((0, -90, 55), (0, 4.0, 0.5), 0.85),
    "firstperson_righthand": ((0, -90, 25), (1.13, 3.2, 1.13), 0.68),
}
SWORD_GRIP = (-4.5, -4.5)
# Vanilla's item/spear_in_hand poses, for the lance (its sprite is drawn point to the top left), and where that sprite
# is held.
SPEAR_IN_HAND = {
    "thirdperson_righthand": ((5, 270, -40), (0, 2, 2), (1.7, 1.7, 0.85)),
    "firstperson_righthand": ((-20, 90, -35), (3.13, 2.0, 0.13), (1.36, 1.36, 0.68)),
}
SPEAR_GRIP = (3.0, -3.0)


def items():
    """Every arm, in registration order: each kind in bronze, then in steel."""
    return [f"{metal}_{kind}" for metal in METALS for kind in KINDS]


def split(item):
    metal, kind = item.split("_", 1)
    return metal, kind


def display(item):
    metal, kind = split(item)
    return f"{gear.GEAR_TIERS[metal]['display']} {KINDS[kind]['display']}"


def feature(item):
    return gear.GEAR_TIERS[split(item)[0]]["feature"]


def metal_content(item):
    """Nuggets of its metal an arm is made of (9 an ingot), for the recipe audit."""
    metal, kind = split(item)
    return {metal: 9 * "".join(KINDS[kind]["pattern"]).count("#")}


def textures():
    """Every arm's sprite (64x64, drawn by tools/arms_art.py): one each, and an in-hand one for a spear or lance."""
    names = []
    for item in items():
        names.append(item)
        if split(item)[1] in CHARGING:
            names.append(f"{item}_in_hand")
    return names


def item_tags():
    """Vanilla item tag -> arms, merged into tools/gear.py's tag files (they share swords)."""
    tags = {}
    for item in items():
        for tag in KINDS[split(item)[1]]["tags"]:
            tags.setdefault(tag, []).append(f"{MOD}:{item}")
    return tags


def _rotation(degrees):
    """The matrix of a display rotation: about x, then y, then z, as Minecraft's rotationXYZ."""
    x, y, z = (math.radians(d) for d in degrees)
    rx = ((1, 0, 0), (0, math.cos(x), -math.sin(x)), (0, math.sin(x), math.cos(x)))
    ry = ((math.cos(y), 0, math.sin(y)), (0, 1, 0), (-math.sin(y), 0, math.cos(y)))
    rz = ((math.cos(z), -math.sin(z), 0), (math.sin(z), math.cos(z), 0), (0, 0, 1))

    def mul(a, b):
        return tuple(tuple(sum(a[i][k] * b[k][j] for k in range(3)) for j in range(3)) for i in range(3))
    return mul(mul(rx, ry), rz)


def _pose(rotation, translation, scale, grip, base_scale, base_grip, factor):
    """A hand pose `factor` times the base one's size, moved so that `grip` (where this sprite is held) lands where the
    base pose holds `base_grip`. Minecraft scales a model, then rotates it, then moves it; so the grip's offset from
    the base one, scaled and rotated, is taken off the translation."""
    scales = base_scale if isinstance(base_scale, tuple) else (base_scale,) * 3
    new = tuple(round(s * factor, 3) for s in scales)
    delta = (scales[0] * base_grip[0] - new[0] * grip[0], scales[1] * base_grip[1] - new[1] * grip[1], 0.0)
    r = _rotation(rotation)
    shift = tuple(sum(r[i][k] * delta[k] for k in range(3)) for i in range(3))
    moved = tuple(round(t + d, 2) + 0.0 for t, d in zip(translation, shift))
    left_moved = (round(translation[0] - shift[0], 2) + 0.0, moved[1], moved[2])
    return ({"rotation": list(rotation), "translation": list(moved), "scale": list(new)},
            {"rotation": [rotation[0], -rotation[1], -rotation[2]], "translation": list(left_moved), "scale": list(new)})


def _grip(kind, mirrored=False):
    """Where a kind's sprite is held, in pixels from the centre of a 16-pixel model (x right, y up)."""
    x, y = arms_art.held_at(kind)
    x = 64 - x if mirrored else x
    return (x / 4 - 8, 8 - y / 4)


def held_model(kind):
    """The shared model a kind's arms are held with: vanilla's sword (or, for the lance, spear) poses, larger by the
    kind's `held` and moved so the hand stays on the grip."""
    charging = kind in CHARGING
    base, base_grip = (SPEAR_IN_HAND, SPEAR_GRIP) if charging else (HANDHELD, SWORD_GRIP)
    grip = _grip(kind, mirrored=charging)
    display = {}
    for context, (rotation, translation, scale) in base.items():
        right, left = _pose(rotation, translation, scale, grip, scale, base_grip, KINDS[kind]["held"])
        display[context] = right
        display[context.replace("righthand", "lefthand")] = left
    return {"parent": "minecraft:item/spear_in_hand" if charging else "minecraft:item/handheld", "display": display}


def write_all(write, assets, data, lang, condition):
    """Shared in-hand models, each arm's model and definition, names and tooltips, recipes and repair tags."""
    models = assets / "models" / "item"
    for kind in KINDS:
        if kind != "spear":
            write(models / f"arms_{kind}.json", held_model(kind))
        lang[f"tooltip.{MOD}.arms.{kind}"] = KINDS[kind]["tooltip"]
    lang[f"tooltip.{MOD}.arms.two_handed"] = "Two-handed: the blow lands as the swing comes round, on every foe in its arc."
    lang[f"message.{MOD}.two_handed.off_hand"] = "Two hands for this one: put away what is in your off hand."
    for item in items():
        metal, kind = split(item)
        lang[f"item.{MOD}.{item}"] = display(item)
        info = KINDS[kind]
        if kind in CHARGING:
            # As vanilla's spears: the plain sprite in inventories, frames and on the ground, and in the hand one
            # drawn point to the top left, which the spear's hand poses hold couched.
            write(models / f"{item}.json", {"parent": "minecraft:item/handheld",
                                            "textures": {"layer0": f"{MOD}:item/{item}"}})
            parent = "minecraft:item/spear_in_hand" if kind == "spear" else f"{MOD}:item/arms_{kind}"
            write(models / f"{item}_in_hand.json", {"parent": parent, "textures": {"layer0": f"{MOD}:item/{item}_in_hand"}})
            model = {"type": "minecraft:select", "property": "minecraft:display_context",
                     "cases": [{"when": ["gui", "ground", "fixed", "on_shelf"],
                                "model": {"type": "minecraft:model", "model": f"{MOD}:item/{item}"}}],
                     "fallback": {"type": "minecraft:model", "model": f"{MOD}:item/{item}_in_hand"}}
            swap = 1.95 * info["held"]
        else:
            write(models / f"{item}.json", {"parent": f"{MOD}:item/arms_{kind}",
                                            "textures": {"layer0": f"{MOD}:item/{item}"}})
            model = {"type": "minecraft:model", "model": f"{MOD}:item/{item}"}
            swap = info["held"]
        # A longer arm comes up into the hand faster, as vanilla's spear does, so it never hangs half-raised.
        write(assets / "items" / f"{item}.json", {"model": model, "swap_animation_scale": round(swap, 2)})

        key = {"#": gear.GEAR_TIERS[metal]["ingot"]}
        for row in info["pattern"]:
            for ch in row:
                if ch in KEYS:
                    key[ch] = KEYS[ch]
        write(data / "recipe" / f"{item}.json", {
            "fabric:load_conditions": condition(feature(item)), "type": "minecraft:crafting_shaped",
            "category": "equipment", "pattern": info["pattern"], "key": key, "result": {"id": f"{MOD}:{item}", "count": 1}})


def draw_all(save):
    """Each arm's 64x64 sprite; a spear's or lance's is also drawn mirrored, point to the top left, for the hand."""
    for item in items():
        metal, kind = split(item)
        img = arms_art.draw(kind, metal)
        save(img, "item", item)
        if kind in CHARGING:
            save(img.transpose(Image.Transpose.FLIP_LEFT_RIGHT), "item", f"{item}_in_hand")

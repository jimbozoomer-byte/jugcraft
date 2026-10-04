"""Tower guns (batch 54, docs/features/tower-guns.md): five heavy emplacements built to stand on top of a tower.

The owner asked for "more guns that look very similar to" a heavy mortar on a turntable mount (a picture of third-party
art used only for its mood: a concrete ring plinth, a railed turntable deck, a yellow armoured cradle with round
ports and pipework, and a fat black barrel), in Jugcraft's style: two that take a 3x3 top and three that take 5x5.

- Bastion Mortar (3x3): a compact high-arc mortar; Heavy Shells.
- Bastion Autocannon (3x3): twin quick-firing barrels in a yellow gunhouse; holds fire; Flak Shells.
- Grand Mortar (5x5): the full emplacement, with a stepped plinth, a tall cradle with an elevation gear and the
  biggest barrel; Great Shells, whose burst reaches further.
- Fortress Rifle (5x5): a long-barrelled gun in an armoured gunhouse that throws Heavy Shells fast and flat, twice as
  far as a mortar.
- Triple Battery (5x5): a low turret with three barrels that fire a salvo of three Heavy Shells.

Each is placed from its item onto the top of a tower (or anything else), centred on the block clicked, and needs a
solid top under its whole footprint. It works like the big guns (batch 51, tools/artillery.py): a gunner climbs
aboard, marks a target with a Range Finder or looks where to fire, and presses attack. Every burst is a Blast
(weapons/Blast): it hurts living things and never breaks a block.

Java: artillery/TowerGun and artillery/JugcraftTowerGuns; client/TowerGunRenderer draws the parts exported here
(assets/jugcraft/tower_gun_quads.json). tools/check_mod_data.py checks that the numbers match.
"""
import json
import math

from PIL import Image

import clean_metal
from steampunk_models import box, cyl
from zeppelin import tiled_quads

MOD = "jugcraft"

# The Great Shell: muzzle speed (blocks a tick), gravity, blast reach (blocks) and centre damage.
GREAT_SPEED = 3.2
GREAT_GRAVITY = 0.05
GREAT_RADIUS = 7.0
GREAT_DAMAGE = 48

# Each gun. size: its footprint (3 or 5 blocks a side). shell: "heavy", "flak" or "great". speed: the shell's muzzle
# speed. cooldown: ticks between shots. traverse: degrees a tick. pitch: elevation limits. high: fires on the high arc.
# auto: keeps firing while attack is held. barrels: each barrel's offset across the gun, in pixels (one shell from
# each). turntable, trunnion: pivots in pixels from the gun's feet (the turntable turns, the barrel elevates about the
# trunnion). muzzle: the barrel's length from the trunnion, in pixels. seats: (x, y, z) in pixels. height: the
# entity's height in blocks. recoil: how far the barrel kicks back, in pixels.
GUNS = {
    "bastion_mortar": dict(
        name="Bastion Mortar", size=3, shell="heavy", speed=3.0, cooldown=70, traverse=3.0, pitch=(45, 85), high=True,
        auto=False, barrels=[0], turntable=(0, 9, 0), trunnion=(0, 27, 2), muzzle=44, health=160,
        seats=[(0, 12, -15), (11, 12, -9)], height=2.4, recoil=5),
    "bastion_autocannon": dict(
        name="Bastion Autocannon", size=3, shell="flak", speed=4.0, cooldown=6, traverse=10.0, pitch=(-10, 85), high=False,
        auto=True, barrels=[-5, 5], turntable=(0, 9, 0), trunnion=(0, 25, 6), muzzle=50, health=120,
        seats=[(0, 12, -16)], height=2.2, recoil=2),
    "grand_mortar": dict(
        name="Grand Mortar", size=5, shell="great", speed=GREAT_SPEED, cooldown=200, traverse=1.0, pitch=(45, 85), high=True,
        auto=False, barrels=[0], turntable=(0, 12, 0), trunnion=(0, 50, 4), muzzle=86, health=320,
        seats=[(0, 15, -26), (20, 15, -16), (-20, 15, -16)], height=4.0, recoil=8),
    "fortress_rifle": dict(
        name="Fortress Rifle", size=5, shell="heavy", speed=4.5, cooldown=120, traverse=1.5, pitch=(-5, 45), high=False,
        auto=False, barrels=[0], turntable=(0, 12, 0), trunnion=(0, 34, 14), muzzle=104, health=300,
        seats=[(0, 15, -30), (18, 15, -26)], height=3.0, recoil=10),
    "triple_battery": dict(
        name="Triple Battery", size=5, shell="heavy", speed=3.0, cooldown=140, traverse=1.5, pitch=(-5, 60), high=False,
        auto=False, barrels=[-11, 0, 11], turntable=(0, 12, 0), trunnion=(0, 32, 14), muzzle=78, health=300,
        seats=[(0, 15, -30), (18, 15, -26)], height=2.8, recoil=7),
}

TOOLTIPS = {
    "bastion_mortar": "A compact heavy mortar for a 3x3 tower top. Climb aboard and press attack to lob a Heavy Shell at "
                      "your marked target, or where you look.",
    "bastion_autocannon": "Twin quick-firing cannon for a 3x3 tower top. Hold attack to fire Flak Shells that burst "
                          "beside anything flying.",
    "grand_mortar": "A colossal mortar for a 5x5 tower top. It lobs Great Shells, whose bursts reach 7 blocks, at your "
                    "marked target or where you look.",
    "fortress_rifle": "A long gun for a 5x5 tower top. It throws Heavy Shells fast and flat, out to about 200 blocks.",
    "triple_battery": "A three-barrelled turret for a 5x5 tower top. Each press fires a salvo of three Heavy Shells.",
    "great_shell": "Ammunition for the Grand Mortar. Its burst hurts creatures up to 7 blocks away but never breaks blocks.",
}

YELLOW, GUNMETAL, HAZARD, CONCRETE, DECK, OLIVE = "ar_yellow", "dp_gunmetal", "dp_hazard", "ar_concrete", "ar_deck", "dp_olive"
SKID, BAND, NUT, COPPER, SOOT, GRILLE = "dr_skid", "dr_band", "dr_nut", "dr_copper_pipe", "dr_soot", "dp_grille"
BRASS, ARMOR, AMBER = "ik_brass", "ar_armor", "dr_amber_on"
PORT, WARNING, SLOTS = "tg_port", "tg_warning", "tg_slots"


def items():
    """Every item id this batch adds: the five guns and the Great Shell."""
    return list(GUNS) + ["great_shell"]


def footprint_radius(gun):
    """Half the footprint's width, in pixels, less a sliver so a gun never pokes past its tower's edge."""
    return GUNS[gun]["size"] * 8 - 0.5


# ------------------------------------------------------------------ shared parts

def plinth(r, tall=False):
    """The fixed concrete ring the gun stands on: a steel rim, anchor feet at the corners and, for the big guns, a
    second stepped ring with vent slots and a yellow band."""
    m = cyl("y", 0, 0, r, 0, 5, CONCRETE, CONCRETE)
    m += cyl("y", 0, 0, r + 0.5, 4, 6, BAND)
    foot = r * 0.72
    for sx, sz in ((-1, -1), (1, -1), (-1, 1), (1, 1)):
        x, z = sx * foot, sz * foot
        m.append(box((x - 3, 0, z - 3), (x + 3, 3, z + 3), SKID))
    if tall:
        m += cyl("y", 0, 0, r - 5, 6, 11, SLOTS, CONCRETE)
        m += cyl("y", 0, 0, r - 4.5, 8, 9, YELLOW)
        m += cyl("y", 0, 0, r - 7, 11, 12, SKID, DECK)
    else:
        m += cyl("y", 0, 0, r - 4, 6, 9, SKID, DECK)
    return m


def railing(r, y, height, start, end, step=30):
    """Posts and a top rail round an arc of the deck (angles in degrees, 0 = forward, 180 = back)."""
    m = []
    for angle in range(start, end + 1, step):
        a = math.radians(angle)
        x, z = r * math.sin(a), r * math.cos(a)
        m.append(box((x - 0.5, y, z - 0.5), (x + 0.5, y + height, z + 0.5), SKID))
    for a0 in range(start, end, step):
        a, b = math.radians(a0), math.radians(a0 + step)
        x0, z0, x1, z1 = r * math.sin(a), r * math.cos(a), r * math.sin(b), r * math.cos(b)
        mx, mz = (x0 + x1) / 2, (z0 + z1) / 2
        half = math.hypot(x1 - x0, z1 - z0) / 2
        angle = math.degrees(math.atan2(x1 - x0, z1 - z0))
        snapped = max(-45, min(45, round(((angle + 90) % 180 - 90) / 22.5) * 22.5))
        m.append(box((mx - 0.5, y + height - 1, mz - half), (mx + 0.5, y + height, mz + half), SKID,
                     rotation=("y", snapped, (mx, y + height - 1, mz))))
    return m


def deck(r):
    """The turning deck: tread plate with a hazard-striped edge (the stripes only round its rim, the top is tread)."""
    return cyl("y", 0, 0, r, 0, 2, SKID, DECK) + cyl("y", 0, 0, r + 0.5, 2, 3, HAZARD, DECK)


def shell_rack(x, z, count):
    """A rack of shells standing on the deck."""
    m = []
    for i in range(count):
        m += cyl("y", x + i * 3.5, z, 1.4, 3, 10, BRASS, GUNMETAL)
    return m


def cradle_side(x0, x1, ty, tz, depth, port_r):
    """One yellow cradle cheek rising to the trunnion, with a round port hub (an X-braced cover) on its outer face."""
    m = [box((x0, 3, tz - depth), (x1, ty - 6, tz + depth * 0.7), YELLOW)]
    m.append(box((x0, ty - 6, tz - depth * 0.65), (x1, ty + port_r * 0.6, tz + depth * 0.5), YELLOW))
    m.append(box((x0 - 0.5, 3, tz - depth - 0.5), (x1 + 0.5, 4.5, tz + depth * 0.7 + 0.5), BAND))
    outer = (x1, x1 + 1.5) if x0 >= 0 else (x0 - 1.5, x0)
    m += cyl("x", ty, tz, port_r, outer[0], outer[1], YELLOW, PORT)
    return m


def pipe_run(points, r=1.2):
    """Copper pipework along straight runs between points (each run along one axis), with a nut at each bend."""
    m = []
    for (x0, y0, z0), (x1, y1, z1) in zip(points, points[1:]):
        if x0 != x1:
            m += cyl("x", y0, z0, r, min(x0, x1), max(x0, x1), COPPER)
        elif y0 != y1:
            m += cyl("y", x0, z0, r, min(y0, y1), max(y0, y1), COPPER)
        else:
            m += cyl("z", x0, y0, r, min(z0, z1), max(z0, z1), COPPER)
    for x, y, z in points[1:-1]:
        m.append(box((x - r - 0.3, y - r - 0.3, z - r - 0.3), (x + r + 0.3, y + r + 0.3, z + r + 0.3), NUT))
    return m


def fat_barrel(r, length, breech):
    """A fat black barrel along +z from its trunnion: a yellow breech housing, a sleeve over the first stretch,
    reinforcing bands and a heavy muzzle ring with a sooty bore."""
    m = [box((-breech, -breech, -breech * 1.5), (breech, breech, breech * 0.3), YELLOW)]
    m.append(box((-breech - 0.5, -breech - 0.5, -breech * 0.6), (breech + 0.5, breech + 0.5, -breech * 0.6 + 2), BAND))
    m += cyl("z", 0, 0, r, breech * 0.3, length, GUNMETAL, SOOT)
    m += cyl("z", 0, 0, r + 1.5, breech * 0.3, length * 0.35, GUNMETAL)
    m += cyl("z", 0, 0, r + 1, length * 0.68, length * 0.68 + 3, GUNMETAL)
    m += cyl("z", 0, 0, r + 1.5, length - 6, length, GUNMETAL, SOOT)
    return m


# ------------------------------------------------------------------ the Bastion Mortar (3x3)

def bastion_mortar_base():
    return plinth(footprint_radius("bastion_mortar") - 0.5)


def bastion_mortar_turntable():
    g = GUNS["bastion_mortar"]
    ty = g["trunnion"][1] - g["turntable"][1]
    tz = g["trunnion"][2]
    m = deck(18)
    m += railing(16.5, 3, 10, 120, 240)
    for x0, x1 in ((-13, -9), (9, 13)):
        m += cradle_side(x0, x1, ty, tz, 11, 5)
    m += pipe_run([(-10, 4, -12), (-10, 14, -12), (-10, 14, -6)])
    m += shell_rack(4, -14, 3)
    m.append(box((13, 3, -6), (16, 9, 0), {"*": YELLOW, "east": WARNING}))
    return m


def bastion_mortar_barrel():
    return fat_barrel(6.5, GUNS["bastion_mortar"]["muzzle"], 8)


# ------------------------------------------------------------------ the Bastion Autocannon (3x3)

def bastion_autocannon_base():
    return plinth(footprint_radius("bastion_autocannon") - 0.5)


def bastion_autocannon_turntable():
    g = GUNS["bastion_autocannon"]
    ty = g["trunnion"][1] - g["turntable"][1]
    tz = g["trunnion"][2]
    m = deck(18)
    m += railing(16.5, 3, 10, 135, 225)
    # The gunhouse: a yellow armoured box open at the back, with the barrels' mantlet slot at the front.
    m.append(box((-13, 3, -6), (-8, ty + 4, tz + 4), {"*": YELLOW, "west": PORT}))
    m.append(box((8, 3, -6), (13, ty + 4, tz + 4), {"*": YELLOW, "east": PORT}))
    m.append(box((-13, ty + 4, -6), (13, ty + 6, tz + 4), {"*": YELLOW, "up": GUNMETAL}))
    m.append(box((-13.5, 3, -6.5), (13.5, 4.5, tz + 4.5), BAND))
    # Ammunition drums on the sides and a sight on the roof.
    for x in (-15, 15):
        m += cyl("z", x, 9, 3, -4, 4, OLIVE, BRASS)
    m.append(box((-1, ty + 6, -2), (1, ty + 9, 0), NUT))
    return m


def bastion_autocannon_barrel():
    g = GUNS["bastion_autocannon"]
    m = [box((-8, -5, -6), (8, 5, 4), {"*": GUNMETAL, "south": YELLOW})]
    for x in g["barrels"]:
        m += cyl("z", x, 0, 1.5, 4, g["muzzle"], GUNMETAL, SOOT)
        m += cyl("z", x, 0, 2.4, 4, 12, GUNMETAL)
        m += cyl("z", x, 0, 2.2, g["muzzle"] - 5, g["muzzle"], GUNMETAL, SOOT)
    return m


# ------------------------------------------------------------------ the Grand Mortar (5x5)

def grand_mortar_base():
    return plinth(footprint_radius("grand_mortar") - 0.5, tall=True)


def grand_mortar_turntable():
    g = GUNS["grand_mortar"]
    ty = g["trunnion"][1] - g["turntable"][1]
    tz = g["trunnion"][2]
    m = deck(30)
    m += railing(28.5, 3, 12, 105, 255, step=25)
    for x0, x1 in ((-22, -15), (15, 22)):
        m += cradle_side(x0, x1, ty, tz, 20, 8)
    # The elevation gear beside the left cheek and its drive motor.
    m += cyl("x", ty - 4, tz - 2, 13, -25, -23, BAND, SKID)
    m.append(box((-27, 3, -18), (-22, 12, -8), {"*": GUNMETAL, "west": GRILLE}))
    # Pipework arching over the cheeks, as in the picture's looping pipes.
    m += pipe_run([(-18.5, 3, -24), (-18.5, 30, -24), (-18.5, 30, -14)], 1.6)
    m += pipe_run([(18.5, 3, -24), (18.5, 30, -24), (18.5, 30, -14)], 1.6)
    # A hazard sign, the control console with a lamp, the shell rack and a ladder down the back.
    m.append(box((22, 12, -6), (23.5, 22, 4), {"*": YELLOW, "east": WARNING}))
    m.append(box((8, 3, -27), (16, 12, -21), {"*": GUNMETAL, "north": GRILLE}))
    m.append(box((10, 12, -25), (14, 14, -23), AMBER))
    m += shell_rack(-14, -24, 4)
    for y in range(-8, 3, 3):
        m.append(box((-3, y, -31), (3, y + 1, -30), SKID))
    m.append(box((-3.5, -9, -31), (-2.5, 3, -30), SKID))
    m.append(box((2.5, -9, -31), (3.5, 3, -30), SKID))
    return m


def grand_mortar_barrel():
    g = GUNS["grand_mortar"]
    m = fat_barrel(11, g["muzzle"], 14)
    # Twin recuperator cylinders riding on top of the breech.
    for x in (-5, 5):
        m += cyl("z", x, 16, 3, -18, 22, GUNMETAL, NUT)
    return m


# ------------------------------------------------------------------ the Fortress Rifle (5x5)

def fortress_rifle_base():
    return plinth(footprint_radius("fortress_rifle") - 0.5, tall=True)


def fortress_rifle_turntable():
    g = GUNS["fortress_rifle"]
    ty = g["trunnion"][1] - g["turntable"][1]
    tz = g["trunnion"][2]
    m = deck(30)
    m += railing(28.5, 3, 12, 135, 225, step=15)
    # The armoured gunhouse: slab sides with ports, a sloped-looking roof in two steps and vents at the back.
    m.append(box((-24, 3, -24), (-14, ty + 8, tz), {"*": YELLOW, "west": PORT}))
    m.append(box((14, 3, -24), (24, ty + 8, tz), {"*": YELLOW, "east": PORT}))
    m.append(box((-24, ty + 8, -24), (24, ty + 11, tz - 4), {"*": YELLOW, "up": GUNMETAL}))
    m.append(box((-20, ty + 11, -20), (20, ty + 13, tz - 10), {"*": YELLOW, "up": GUNMETAL}))
    m.append(box((-14, 3, -24), (14, ty + 8, -21), {"*": YELLOW, "north": GRILLE}))
    m.append(box((-24.5, 3, -24.5), (24.5, 4.5, tz + 0.5), BAND))
    # The range-finder arms across the roof, with a lens at each end.
    m += cyl("x", ty + 15, -14, 2, -28, 28, GUNMETAL, AMBER)
    m.append(box((-2, ty + 13, -16), (2, ty + 15, -12), NUT))
    m.append(box((24, 10, -14), (25.5, 20, -4), {"*": YELLOW, "east": WARNING}))
    return m


def fortress_rifle_barrel():
    g = GUNS["fortress_rifle"]
    length = g["muzzle"]
    m = [box((-12, -10, -12), (12, 10, 4), {"*": YELLOW, "south": GUNMETAL})]
    m += cyl("z", 0, 0, 4.5, 4, length - 8, GUNMETAL, SOOT)
    m += cyl("z", 0, 0, 6, 4, 30, GUNMETAL)
    m += cyl("z", 0, 0, 5.5, 54, 58, GUNMETAL)
    # The muzzle brake: a block with vent slots.
    m.append(box((-6, -5, length - 8), (6, 5, length), GUNMETAL))
    for z in (length - 6, length - 3):
        m.append(box((-6.5, -2, z), (6.5, 2, z + 1.5), SOOT))
    return m


# ------------------------------------------------------------------ the Triple Battery (5x5)

def triple_battery_base():
    return plinth(footprint_radius("triple_battery") - 0.5, tall=True)


def triple_battery_turntable():
    g = GUNS["triple_battery"]
    ty = g["trunnion"][1] - g["turntable"][1]
    m = deck(30)
    m += railing(28.5, 3, 12, 150, 210, step=15)
    # A low round turret drum with a domed roof, ports on its flanks and a commander's cupola.
    m += cyl("y", 0, -2, 25, 3, ty + 6, YELLOW, GUNMETAL)
    m += cyl("y", 0, -2, 25.5, 3, 4.5, BAND)
    m += cyl("y", 0, -4, 18, ty + 6, ty + 8, YELLOW, GUNMETAL)
    m += cyl("y", -10, -12, 4, ty + 8, ty + 12, GUNMETAL, AMBER)
    for x in (-25.6, 24.1):
        m.append(box((x, 6, -10), (x + 1.5, 18, 2), {"*": YELLOW, "east" if x > 0 else "west": PORT}))
    m.append(box((-6, 6, -28), (6, 16, -26.5), {"*": YELLOW, "north": WARNING}))
    return m


def triple_battery_barrel():
    g = GUNS["triple_battery"]
    m = [box((-17, -7, -8), (17, 7, 4), {"*": YELLOW, "south": GUNMETAL})]
    for x in g["barrels"]:
        m += cyl("z", x, 0, 3, 4, g["muzzle"], GUNMETAL, SOOT)
        m += cyl("z", x, 0, 4, 4, 22, GUNMETAL)
        m += cyl("z", x, 0, 3.8, g["muzzle"] - 5, g["muzzle"], GUNMETAL, SOOT)
    return m


PARTS = {
    "bastion_mortar": (bastion_mortar_base, bastion_mortar_turntable, bastion_mortar_barrel),
    "bastion_autocannon": (bastion_autocannon_base, bastion_autocannon_turntable, bastion_autocannon_barrel),
    "grand_mortar": (grand_mortar_base, grand_mortar_turntable, grand_mortar_barrel),
    "fortress_rifle": (fortress_rifle_base, fortress_rifle_turntable, fortress_rifle_barrel),
    "triple_battery": (triple_battery_base, triple_battery_turntable, triple_battery_barrel),
}


def export():
    out = {}
    for gun, (base, turntable, barrel) in PARTS.items():
        out[f"{gun}_base"] = tiled_quads(base())
        out[f"{gun}_turntable"] = tiled_quads(turntable())
        out[f"{gun}_barrel"] = tiled_quads(barrel())
    return out


# ------------------------------------------------------------------ the Java the numbers must match

def _f(value):
    text = repr(float(value))
    return text + "F"


def java_spec(gun):
    """The line in artillery/JugcraftTowerGuns.java that defines a gun (tools/check_mod_data.py looks for it)."""
    g = GUNS[gun]
    seats = ", ".join(f"new Vec3({x / 16}, {y / 16}, {z / 16})" for x, y, z in g["seats"])
    barrels = ", ".join(str(b / 16) for b in g["barrels"])
    return (f'spec("{gun}", {g["size"]}, "{g["shell"]}", {g["speed"]}, {g["cooldown"]}, {_f(g["traverse"])}, '
            f'{_f(g["pitch"][0])}, {_f(g["pitch"][1])}, {str(g["high"]).lower()}, {str(g["auto"]).lower()}, '
            f'{g["trunnion"][1] / 16}, {g["trunnion"][2] / 16}, {g["muzzle"] / 16}, {g["health"]}, {_f(g["height"])}, '
            f'new double[] {{{barrels}}}, {seats})')


def java_pivots(gun):
    """The line in client/TowerGunRenderer.java with a gun's pivots and recoil."""
    g = GUNS[gun]
    pivots = ", ".join("new float[] {" + ", ".join(str(v) for v in p) + "}" for p in (g["turntable"], g["trunnion"]))
    return f'pivots("{gun}", {pivots}, {_f(g["recoil"])})'


# ------------------------------------------------------------------ data

RECIPES = {
    "bastion_mortar": (["PGP", "PMP", "CCC"], {"P": "#c:plates/steel", "G": "#c:gears/steel",
                                              "M": f"{MOD}:siege_mortar", "C": "minecraft:smooth_stone"}, 1),
    "bastion_autocannon": (["PDP", "GFG", "CCC"], {"P": "#c:plates/steel", "D": "minecraft:dispenser",
                                                  "G": "#c:gears/steel", "F": f"{MOD}:flak_gun",
                                                  "C": "minecraft:smooth_stone"}, 1),
    "grand_mortar": (["SBS", "EGE", "CCC"], {"S": "#c:storage_blocks/steel", "B": f"{MOD}:bastion_mortar",
                                            "E": f"{MOD}:diesel_engine", "G": "#c:gears/steel",
                                            "C": "minecraft:smooth_stone"}, 1),
    "fortress_rifle": (["SDS", "EGE", "CCC"], {"S": "#c:storage_blocks/steel", "D": "minecraft:dispenser",
                                              "E": f"{MOD}:diesel_engine", "G": "#c:gears/steel",
                                              "C": "minecraft:smooth_stone"}, 1),
    "triple_battery": (["DDD", "EGE", "SCS"], {"D": "minecraft:dispenser", "E": f"{MOD}:diesel_engine",
                                              "G": "#c:gears/steel", "S": "#c:storage_blocks/steel",
                                              "C": "minecraft:smooth_stone"}, 1),
    "great_shell": (["HTH"], {"H": f"{MOD}:heavy_shell", "T": "minecraft:tnt"}, 1),
}


def write_all(write, assets, data, lang, condition):
    names = {gun: g["name"] for gun, g in GUNS.items()}
    names["great_shell"] = "Great Shell"
    for item, name in names.items():
        lang[f"item.{MOD}.{item}"] = name
        write(assets / "models" / "item" / f"{item}.json",
              {"parent": "minecraft:item/generated", "textures": {"layer0": f"{MOD}:item/{item}"}})
        write(assets / "items" / f"{item}.json", {"model": {"type": "minecraft:model", "model": f"{MOD}:item/{item}"}})
        lang[f"entity.{MOD}.{item}"] = name
    for key, text in TOOLTIPS.items():
        lang[f"tooltip.{MOD}.{key}"] = text
    lang[f"message.{MOD}.artillery.needs_top"] = "Needs a solid %1$sx%1$s top to stand on"
    (assets / "tower_gun_quads.json").write_text(json.dumps(export(), separators=(",", ":")) + "\n", encoding="utf-8")
    for item, (pattern, key, count) in RECIPES.items():
        write(data / "recipe" / f"{item}.json", {
            "fabric:load_conditions": condition("machines"), "type": "minecraft:crafting_shaped",
            "category": "equipment" if item == "great_shell" else "misc",
            "pattern": pattern, "key": key, "result": {"id": f"{MOD}:{item}", "count": count}})


# ------------------------------------------------------------------ art (the clean style, tools/clean_metal.py)

PAINT = [(130, 96, 18), (176, 134, 28), (214, 170, 42), (236, 196, 72), (250, 220, 120)]
DARK = [(30, 30, 34), (44, 44, 48), (62, 62, 66), (86, 86, 90)]
STONE = [(100, 98, 92), (118, 116, 108), (134, 132, 124), (150, 148, 140)]


def port():
    """A round port cover on yellow paint: a dark steel ring, an X brace across it and a bolt at the centre."""
    img = clean_metal.canvas(PAINT[2])
    clean_metal.bevel(img, 0, 0, 15, 15, PAINT[3], PAINT[1], PAINT[2])
    for y in range(16):
        for x in range(16):
            d = math.hypot(x - 7.5, y - 7.5)
            if d < 6.6:
                c = DARK[2] if d > 5.4 else DARK[1]
                if d > 5.4 and x + y < 15:
                    c = DARK[3]
                if d <= 5.4 and (abs(x - y) <= 0 or abs(x + y - 15) <= 0):
                    c = DARK[3]
                clean_metal.put(img, x, y, c)
    clean_metal.bolt(img, 7, 7, DARK)
    return img


def warning():
    """A hazard sign on yellow paint: a black-edged triangle with an exclamation mark."""
    img = clean_metal.canvas(PAINT[2])
    clean_metal.bevel(img, 0, 0, 15, 15, PAINT[3], PAINT[1], PAINT[2])
    for y in range(3, 13):
        half = (y - 3) * 0.6 + 0.5
        for x in range(16):
            if abs(x - 7.5) <= half:
                edge = abs(x - 7.5) > half - 1 or y == 12
                clean_metal.put(img, x, y, DARK[0] if edge else PAINT[4])
    for y in (6, 7, 8):
        clean_metal.put(img, 7, y, DARK[0])
        clean_metal.put(img, 8, y, DARK[0])
    clean_metal.put(img, 7, 10, DARK[0])
    clean_metal.put(img, 8, 10, DARK[0])
    return img


def slots():
    """The plinth's upper ring: cast concrete with a row of dark vent slots, each lit on its lower lip."""
    img = clean_metal.canvas(STONE[2])
    for x in range(16):
        clean_metal.put(img, x, 0, STONE[3])
        clean_metal.put(img, x, 15, STONE[1])
    for x0 in (1, 9):
        clean_metal.rect(img, x0, 5, x0 + 5, 9, DARK[0])
        for x in range(x0, x0 + 6):
            clean_metal.put(img, x, 10, STONE[3])
        for x in range(x0 + 1, x0 + 5, 2):
            for y in range(5, 10):
                clean_metal.put(img, x, y, DARK[1])
    return img


def icon(kind):
    """The items: each gun side-on in miniature, and the Great Shell."""
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    black, yellow, stone, steel, brass = (34, 34, 38), PAINT[2], STONE[1], (120, 118, 112), (196, 160, 80)

    def rect(x0, y0, x1, y1, c):
        clean_metal.rect(img, x0, y0, x1, y1, c)

    if kind == "great_shell":
        rect(5, 6, 10, 14, brass)
        rect(5, 3, 10, 5, steel)
        rect(6, 1, 9, 2, steel)
        rect(5, 10, 10, 10, (170, 40, 40))
        rect(5, 12, 10, 12, (140, 110, 50))
        return img
    size = GUNS[kind]["size"]
    rect(1 if size == 5 else 3, 13, 14 if size == 5 else 12, 14, stone)
    rect(2 if size == 5 else 4, 11, 13 if size == 5 else 11, 12, steel)
    if kind in ("bastion_mortar", "grand_mortar"):
        rect(5, 7 if size == 5 else 8, 10, 10, yellow)
        width = 3 if size == 5 else 2
        for i in range(7):
            rect(7 + i, 6 - i, 7 + i + width, 7 - i + (width - 2), black)
    elif kind == "bastion_autocannon":
        rect(4, 7, 10, 10, yellow)
        for i in range(6):
            clean_metal.put(img, 9 + i, 7 - i, black)
            clean_metal.put(img, 10 + i, 8 - i, black)
    elif kind == "fortress_rifle":
        rect(2, 6, 9, 10, yellow)
        rect(9, 7, 15, 8, black)
    elif kind == "triple_battery":
        rect(3, 7, 11, 10, yellow)
        for y in (5, 7, 9):
            rect(10, y, 15, y, black)
    return img


def draw_all(save):
    for name, img in (("tg_port", port()), ("tg_warning", warning()), ("tg_slots", slots())):
        save(img, "block", name)
    for item in list(GUNS) + ["great_shell"]:
        save(icon(item), "item", item)

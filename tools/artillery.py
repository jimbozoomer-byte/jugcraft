"""Big guns (batch 51, docs/features/big-guns.md): heavy artillery for the dieselpunk front.

- The Siege Mortar: a fixed emplacement on a concrete ring, a railed turntable deck, a yellow cradle and a fat black
  barrel that lobs Heavy Shells in a high arc.
- The Self-Propelled Howitzer: a tracked gun carriage with an armoured cab and a long-barrelled gun with a muzzle brake
  that fires Heavy Shells flat or high.
- The Flak Gun: twin autocannon on a cross mount whose Flak Shells burst next to anything flying.
- The Observation Balloon: a tethered kite balloon whose basket rises high above its anchor while someone rides it, so
  a spotter with a Range Finder can mark targets far away for the guns.

Every shell bursts in a Blast (weapons/Blast): it hurts living things and never breaks a block. The guns aim
themselves at a target marked with a Range Finder (the gunner's own mark, else the nearest spotter's) and work out the
elevation that lands the shell on it; without a mark they fire where the gunner looks.

Java: artillery/ (the entities, shells, items and JugcraftArtillery); client/ArtilleryRenderers draws the parts
exported here (assets/jugcraft/artillery_quads.json). tools/check_mod_data.py keeps the numbers the same.
"""
import json
import math

from PIL import Image

from clean_metal import CHIP, CHIP_WIDE, bolt, corner_bolts, inset, patch, plate
from steampunk_models import box, cyl
from zeppelin import round_section, tiled_quads

MOD = "jugcraft"

# Shells: muzzle speed (blocks a tick), gravity (blocks a tick, a tick), blast reach and centre damage.
HEAVY_SPEED = 3.0
HEAVY_GRAVITY = 0.05
HEAVY_RADIUS = 5.0
HEAVY_DAMAGE = 32
FLAK_SPEED = 4.0
FLAK_GRAVITY = 0.01
FLAK_RADIUS = 3.0
FLAK_DAMAGE = 10
FLAK_FUSE = 30
# How close a flak shell must pass to something flying to burst, in blocks.
FLAK_PROXIMITY = 2.5
# Ticks between shots.
MORTAR_COOLDOWN = 100
HOWITZER_COOLDOWN = 80
FLAK_COOLDOWN = 8
# How fast a gun traverses and elevates, in degrees a tick.
MORTAR_TRAVERSE = 2.0
HOWITZER_TRAVERSE = 3.0
FLAK_TRAVERSE = 12.0
# The howitzer's gun turns this far either side of the hull.
HOWITZER_ARC = 30
# The howitzer drives like the landship, slower.
HOWITZER_SPEED = 0.1
HOWITZER_TURN = 2.0
HOWITZER_FUEL_TANK = 6000
FUEL_PER_BUCKET = 1000
HOWITZER_FUEL_PER_SECOND = 5
# The balloon: how high its basket rises above the anchor, and how fast it climbs and sinks.
BALLOON_HEIGHT = 32
BALLOON_CLIMB = 0.08
# The range finder's reach and how long a mark lasts (ticks).
MARK_RANGE = 256
MARK_TTL = 6000
HEALTH = {"siege_mortar": 150, "self_propelled_howitzer": 140, "flak_gun": 60, "observation_balloon": 30}

ITEMS = {
    "siege_mortar": "Siege Mortar",
    "self_propelled_howitzer": "Self-Propelled Howitzer",
    "flak_gun": "Flak Gun",
    "observation_balloon": "Observation Balloon",
    "heavy_shell": "Heavy Shell",
    "flak_shell": "Flak Shell",
    "range_finder": "Range Finder",
}
TOOLTIPS = {
    "siege_mortar": "A fixed heavy mortar. Climb aboard and press attack to lob a Heavy Shell at your marked target, or "
                    "where you look. Its bursts hurt creatures but never break blocks.",
    "self_propelled_howitzer": "A tracked gun. Drive with the movement keys; attack fires a Heavy Shell at your marked "
                               "target, or where you look. Refuel with a diesel or kerosene bucket.",
    "flak_gun": "Twin anti-aircraft cannon. Hold attack to fire Flak Shells that burst beside anything flying.",
    "observation_balloon": "A tethered balloon. Climb into its basket and it rises high above where you placed it; "
                           "climb out and it winches down.",
    "heavy_shell": "Ammunition for the Siege Mortar and the Self-Propelled Howitzer.",
    "flak_shell": "Ammunition for the Flak Gun.",
    "range_finder": "Use to mark the block you look at, up to 256 blocks away, as a target for your guns and any "
                    "gunner nearby. Sneak and use to clear your mark.",
}
ENTITIES = ["siege_mortar", "self_propelled_howitzer", "flak_gun", "observation_balloon", "heavy_shell", "flak_shell"]

YELLOW, GUNMETAL, OLIVE, HAZARD, CONCRETE, DECK = "ar_yellow", "dp_gunmetal", "dp_olive", "dp_hazard", "ar_concrete", "ar_deck"
SKID, BAND, NUT, BRASS, LACQUER, COPPER = "dr_skid", "dr_band", "dr_nut", "ik_brass", "ik_lacquer", "dr_copper_pipe"
EXHAUST, SOOT, CANVAS, STRIPE, WICKER, ARMOR = "dp_exhaust", "dr_soot", "dz_canvas", "dz_canvas_stripe", "ar_wicker", "ar_armor"

# Pivots, in pixels from the entity's feet (facing +z). Keep in sync with client/ArtilleryRenderers.
MORTAR_TURNTABLE = (0, 10, 0)
MORTAR_TRUNNION = (0, 30, 2)
HOWITZER_GUN = (6, 32, 4)
FLAK_HEAD = (0, 18, 0)
# The howitzer's track path round each track unit, (z, y) in pixels, and the units' x span.
HOWITZER_TRACK = [(-38, 1), (36, 1), (44, 8), (44, 16), (36, 23), (-38, 23), (-46, 16), (-46, 8)]
HOWITZER_TRACK_X = (17, 29)


# ------------------------------------------------------------------ the siege mortar

def mortar_base():
    """The fixed concrete ring with its steel rim and four anchor feet."""
    m = cyl("y", 0, 0, 26, 0, 6, CONCRETE, CONCRETE)
    m += cyl("y", 0, 0, 27, 5, 7, BAND)
    m += cyl("y", 0, 0, 21, 6, 9, SKID, DECK)
    for x, z in ((-26, -26), (22, -26), (-26, 22), (22, 22)):
        m.append(box((x, 0, z), (x + 4, 3, z + 4), SKID))
    return m


def mortar_turntable():
    """The turning deck, about its pivot: tread plate, hazard edge, railings on three sides, the cradle side plates
    and trunnion hubs, an elevation wheel and an ammunition rack."""
    m = cyl("y", 0, 0, 20, 0, 3, SKID, DECK)
    m += cyl("y", 0, 0, 20.5, 2, 3, HAZARD)
    # Railings round the back and sides.
    for angle in range(90, 271, 30):
        a = math.radians(angle)
        x, z = 18.5 * math.sin(a), 18.5 * math.cos(a)
        m.append(box((x - 0.5, 3, z - 0.5), (x + 0.5, 14, z + 0.5), SKID))
    for a0 in range(90, 270, 30):
        a, b = math.radians(a0), math.radians(a0 + 30)
        x0, z0, x1, z1 = 18.5 * math.sin(a), 18.5 * math.cos(a), 18.5 * math.sin(b), 18.5 * math.cos(b)
        mx, mz = (x0 + x1) / 2, (z0 + z1) / 2
        half = math.hypot(x1 - x0, z1 - z0) / 2
        angle = math.degrees(math.atan2(x1 - x0, z1 - z0))
        snapped = max(-45, min(45, round(((angle + 90) % 180 - 90) / 22.5) * 22.5))
        m.append(box((mx - 0.5, 13, mz - half), (mx + 0.5, 14, mz + half), SKID, rotation=("y", snapped, (mx, 13, mz))))
    # Cradle side plates rising to the trunnions, yellow with dark edging.
    tz = MORTAR_TRUNNION[2]
    for x0, x1 in ((-15, -10), (10, 15)):
        m.append(box((x0, 3, tz - 14), (x1, 14, tz + 10), {"*": YELLOW}))
        m.append(box((x0, 14, tz - 9), (x1, 26, tz + 7), {"*": YELLOW}))
        m.append(box((x0 - 0.5, 3, tz - 14.5), (x1 + 0.5, 4.5, tz + 10.5), BAND))
    ty = MORTAR_TRUNNION[1] - MORTAR_TURNTABLE[1]
    for x0, x1 in ((-17, -15), (15, 17)):
        m += cyl("x", ty, tz, 6, x0, x1, LACQUER, NUT)
    # The elevation wheel on the right and a rack of shells at the back.
    m += cyl("x", 10, tz + 4, 4, 17, 18, BRASS)
    for i in range(4):
        m += cyl("y", -6 + i * 4, -15, 1.6, 3, 10, BRASS, GUNMETAL)
    return m


def mortar_barrel():
    """The barrel, about the trunnions, along +z: a yellow breech ring, a fat black tube with reinforcing bands and a
    belled muzzle, and a recuperator under it."""
    m = [box((-9, -9, -16), (9, 9, 4), {"*": YELLOW})]
    m.append(box((-9.5, -9.5, -6), (9.5, 9.5, -4), BAND))
    m += cyl("z", 0, 0, 8, 4, 54, GUNMETAL, SOOT)
    m += cyl("z", 0, 0, 9.5, 4, 14, GUNMETAL)
    m += cyl("z", 0, 0, 9.5, 50, 56, GUNMETAL, SOOT)
    m += cyl("z", 0, -10.5, 2.5, -10, 22, COPPER)
    return m


# ------------------------------------------------------------------ the self-propelled howitzer

def howitzer_body():
    """The carriage: two track units, the hull between them, an armoured cab at the back left, the engine with its
    domed housing and exhausts, and the gun's mounting ring."""
    m = []
    x0, x1 = HOWITZER_TRACK_X
    for sign in (1, -1):
        a, b = (x0, x1) if sign > 0 else (-x1, -x0)
        m.append(box((a + 1, 4, -38), (b - 1, 20, 38), {"*": ARMOR, "up": SKID}))
        for z in (-28, -12, 4, 20):
            m += cyl("x", 6, z, 4.5, a + (11 if sign > 0 else -0.5), a + (12 if sign > 0 else 0.5), SKID, NUT)
    m.append(box((-17, 6, -36), (17, 22, 30), {"*": ARMOR, "up": DECK}))
    m.append(box((-17.5, 21, -36.5), (17.5, 23, 30.5), BAND))
    # The cab: tall, slab-sided, vision slits, a hatch and a rail.
    m.append(box((-17, 22, -36), (-1, 46, -12), {"*": ARMOR}))
    m.append(box((-15, 46, -32), (-3, 48, -16), {"*": OLIVE}))
    for z in (-34, -26, -18):
        m.append(box((-17.5, 38, z), (-16.5, 39.5, z + 4), NUT))
    m.append(box((-14, 38, -12), (-4, 39.5, -11.5), NUT))
    # The engine at the front right: a domed housing, cooling grille and two exhausts.
    m += cyl("z", 9, 32, 7, 14, 28, OLIVE, BRASS)
    m.append(box((2, 22, 14), (16, 30, 28), {"*": ARMOR, "south": "dp_grille"}))
    for x in (6, 12):
        m += cyl("y", x, 10, 1.6, 30, 40, EXHAUST, SOOT)
    # The gun's mounting ring and recoil buffers' housing.
    gx, gy, gz = HOWITZER_GUN
    m += cyl("y", gx, gz, 8, 22, 26, SKID, DECK)
    return m


def howitzer_gun():
    """The gun, about its pivot, along +z: a cradle with recoil cylinders, an armoured shield and a long barrel with
    a muzzle brake."""
    m = [box((-6, -5, -12), (6, 6, 12), {"*": ARMOR})]
    m.append(box((-11, -8, 8), (11, 10, 10), {"*": ARMOR}))
    for x in (-4, 4):
        m += cyl("z", x, 7, 2, -10, 20, COPPER)
    m += cyl("z", 0, 0, 3.5, 10, 88, GUNMETAL, SOOT)
    m += cyl("z", 0, 0, 4.5, 10, 24, GUNMETAL)
    m.append(box((-5, -4, 88), (5, 4, 96), {"*": GUNMETAL}))
    for z in (90, 93):
        m.append(box((-5.5, -2, z), (5.5, 2, z + 1.5), SOOT))
    return m


# ------------------------------------------------------------------ the flak gun

def flak_mount():
    """The fixed cross mount: four splayed legs with feet and a pedestal."""
    m = []
    for dx, dz in ((1, 0), (-1, 0), (0, 1), (0, -1)):
        x0, x1 = (min(0, dx * 22), max(0, dx * 22)) if dx else (-2, 2)
        z0, z1 = (min(0, dz * 22), max(0, dz * 22)) if dz else (-2, 2)
        m.append(box((x0, 0, z0), (x1, 3, z1), {"*": OLIVE}))
        fx, fz = dx * 21, dz * 21
        m.append(box((fx - 3, 0, fz - 3), (fx + 3, 1, fz + 3), SKID))
    m += cyl("y", 0, 0, 5, 3, 12, OLIVE, SKID)
    return m


def flak_head():
    """The turning head, about its pivot: the cradle, twin barrels with flash hiders, ammunition drums and the
    gunner's seat and sights."""
    m = [box((-7, -4, -10), (7, 6, 8), {"*": OLIVE})]
    for x in (-4, 4):
        m += cyl("z", x, 1, 1.4, 8, 44, GUNMETAL, SOOT)
        m += cyl("z", x, 1, 2.2, 40, 46, GUNMETAL, SOOT)
        m += cyl("x", 7, -4, 4, x - 1.5 if x < 0 else x - 1.5, x + 1.5, OLIVE, BRASS)
    m.append(box((-1, 6, 2), (1, 10, 4), SKID))
    m.append(box((-2, 9, 2), (2, 12, 3), NUT))
    return m


# ------------------------------------------------------------------ the observation balloon

BALLOON_Y = 104
BALLOON_STATIONS = [(-60, 6), (-48, 16), (-30, 22), (-6, 24), (20, 22), (38, 16), (50, 8)]


def balloon_envelope():
    """The kite balloon high above its basket: a sausage envelope with painted bands and three tail lobes."""
    m = []
    for (z0, r0), (z1, r1) in zip(BALLOON_STATIONS, BALLOON_STATIONS[1:]):
        m += round_section(BALLOON_Y, (r0 + r1) / 2, z0, z1, CANVAS, 4)
    for z in (-24, 12):
        m += round_section(BALLOON_Y, 23.5, z, z + 3, STRIPE, 4)
    for dx, dy in ((0, 1), (1, -1), (-1, -1)):
        cx, cy = dx * 12, BALLOON_Y + dy * 12
        m.append(box((cx - 6, cy - 6, -70), (cx + 6, cy + 6, -54), {"*": CANVAS}))
    return m


def balloon_basket():
    """The wicker basket with its rim, the rigging lines up to the envelope and the winch cable's shackle below."""
    m = [box((-10, 0, -10), (10, 14, 10), {"*": WICKER})]
    m.append(box((-10.5, 13, -10.5), (10.5, 15, 10.5), {"*": SKID}))
    for x, z in ((-9, -9), (9, -9), (-9, 9), (9, 9)):
        m.append(box((x - 0.5, 15, z - 0.5), (x + 0.5, BALLOON_Y - 22, z + 0.5), BAND))
    m += cyl("y", 0, 0, 1.5, -2, 0, NUT)
    return m


def balloon_cable():
    """One block of the winch cable, hanging down from the origin; the renderer stretches it to the anchor."""
    return [box((-0.5, -16, -0.5), (0.5, 0, 0.5), BAND)]


def export():
    return {"mortar_base": tiled_quads(mortar_base()), "mortar_turntable": tiled_quads(mortar_turntable()),
            "mortar_barrel": tiled_quads(mortar_barrel()), "howitzer_body": tiled_quads(howitzer_body()),
            "howitzer_gun": tiled_quads(howitzer_gun()), "flak_mount": tiled_quads(flak_mount()),
            "flak_head": tiled_quads(flak_head()), "balloon_envelope": tiled_quads(balloon_envelope()),
            "balloon_basket": tiled_quads(balloon_basket()), "balloon_cable": tiled_quads(balloon_cable())}


RECIPES = {
    "siege_mortar": (["BGB", "PEP", "CCC"], {"B": "#c:plates/steel", "G": "#c:gears/steel", "P": "minecraft:piston",
                                            "E": "minecraft:dispenser", "C": "minecraft:smooth_stone"}, 1),
    "self_propelled_howitzer": (["PBP", "EDE", "TGT"], {"P": "#c:plates/steel", "B": "minecraft:dispenser",
                                                       "E": f"{MOD}:diesel_engine", "D": "minecraft:piston",
                                                       "T": f"{MOD}:belt", "G": "#c:gears/steel"}, 1),
    "flak_gun": (["D D", "PGP", "I I"], {"D": "minecraft:dispenser", "P": "#c:plates/steel", "G": "#c:gears/steel",
                                         "I": "minecraft:iron_ingot"}, 1),
    "observation_balloon": (["LLL", "LCL", " W "], {"L": f"{MOD}:hydrogen_lift_cell", "C": "minecraft:lead",
                                                    "W": "minecraft:barrel"}, 1),
    "heavy_shell": (["P", "G", "B"], {"P": "#c:plates/steel", "G": "minecraft:gunpowder", "B": "#c:plates/brass"}, 2),
    "flak_shell": (["PGP"], {"P": "#c:nuggets/brass", "G": "minecraft:gunpowder"}, 4),
    "range_finder": (["G G", "BCB"], {"G": "minecraft:glass_pane", "B": "#c:plates/brass", "C": "minecraft:compass"}, 1),
}


def write_all(write, assets, data, lang, condition):
    for item, name in ITEMS.items():
        lang[f"item.{MOD}.{item}"] = name
        write(assets / "models" / "item" / f"{item}.json",
              {"parent": "minecraft:item/generated", "textures": {"layer0": f"{MOD}:item/{item}"}})
        write(assets / "items" / f"{item}.json", {"model": {"type": "minecraft:model", "model": f"{MOD}:item/{item}"}})
    for key, text in TOOLTIPS.items():
        lang[f"tooltip.{MOD}.{key}"] = text
    for entity in ENTITIES:
        lang[f"entity.{MOD}.{entity}"] = ITEMS[entity]
    lang[f"message.{MOD}.artillery.no_room"] = "Not enough room here"
    lang[f"message.{MOD}.artillery.no_shells"] = "No %s"
    lang[f"message.{MOD}.artillery.marked"] = "Target marked %s blocks away"
    lang[f"message.{MOD}.artillery.cleared"] = "Target mark cleared"
    lang[f"message.{MOD}.artillery.no_target"] = "Nothing to mark within %s blocks"
    lang[f"message.{MOD}.artillery.out_of_range"] = "The marked target is out of range"
    lang[f"message.{MOD}.artillery.fuel"] = "Fuel: %s%%"
    lang[f"message.{MOD}.artillery.refuelled"] = "Refuelled: %s%%"
    lang[f"message.{MOD}.artillery.full"] = "The fuel tank is full"
    (assets / "artillery_quads.json").write_text(json.dumps(export(), separators=(",", ":")) + "\n", encoding="utf-8")
    for item, (pattern, key, count) in RECIPES.items():
        category = "equipment" if item in ("heavy_shell", "flak_shell", "range_finder") else "misc"
        write(data / "recipe" / f"{item}.json", {
            "fabric:load_conditions": condition("machines"), "type": "minecraft:crafting_shaped", "category": category,
            "pattern": pattern, "key": key, "result": {"id": f"{MOD}:{item}", "count": count}})


# ------------------------------------------------------------------ art

def _img():
    return Image.new("RGBA", (16, 16), (0, 0, 0, 0))


def _put(img, x, y, c):
    if 0 <= x < 16 and 0 <= y < 16:
        img.putpixel((x, y), tuple(c) + (255,))




def yellow():
    """Warning-yellow paint on steel: a bevelled panel with a recessed inset, six bolts and two chipped corners."""
    img = _img()
    pal = [(130, 96, 18), (176, 134, 28), (214, 170, 42), (236, 196, 72), (250, 220, 120)]
    plate(img, pal)
    inset(img, 4, 4, 11, 11, pal[3], pal[1])
    for x, y in ((2, 2), (12, 2), (2, 12), (12, 12), (7, 2), (7, 12)):
        bolt(img, x, y, pal)
    patch(img, CHIP_WIDE, (84, 82, 78), 1, 13)
    patch(img, CHIP, (84, 82, 78), 13, 1)
    return img


def concrete():
    """Cast concrete like vanilla smooth stone: a flat grey, a formwork seam along the bottom and a few soft blotches."""
    pal = [(100, 98, 92), (118, 116, 108), (134, 132, 124), (150, 148, 140)]
    img = _img()
    _rect(img, 0, 0, 15, 15, pal[2])
    for x in range(16):
        _put(img, x, 15, pal[1])
        _put(img, x, 0, pal[3])
    for pixels, c in (([(3, 4), (4, 4), (4, 5)], pal[1]), ([(10, 7), (11, 7)], pal[3]),
                      ([(6, 11), (7, 11), (7, 12)], pal[1]), ([(12, 12), (13, 12)], pal[3])):
        for x, y in pixels:
            _put(img, x, y, c)
    return img


def deck():
    """Diamond tread plate: flat dark steel with raised lozenges, each lit on its upper left."""
    img = _img()
    _rect(img, 0, 0, 15, 15, (72, 72, 74))
    for cy in (2, 10):
        for cx in (2, 10):
            for ox, oy in ((0, 0), (4, 4)):
                x, y = (cx + ox) % 16, (cy + oy) % 16
                _put(img, x, y, (142, 142, 144))
                _put(img, (x + 1) % 16, (y + 1) % 16, (112, 112, 114))
                _put(img, (x + 2) % 16, (y + 2) % 16, (52, 52, 54))
    return img


def armor():
    """Khaki-bronze armour plate: a bevelled panel split by a welded seam, with bolts at the corners."""
    pal = [(78, 70, 48), (100, 90, 62), (122, 110, 76), (142, 128, 90), (172, 158, 114)]
    img = _img()
    plate(img, pal)
    for x in range(1, 15):
        _put(img, x, 8, pal[0])
        _put(img, x, 9, pal[3])
    corner_bolts(img, pal)
    return img


def wicker():
    """Woven wicker: alternating light and dark strands."""
    img = _img()
    for y in range(16):
        for x in range(16):
            over = ((x // 2) + (y // 2)) % 2 == 0
            c = (176, 140, 84) if over else (128, 96, 54)
            if (x % 2 == 0 and over) or (y % 2 == 0 and not over):
                c = tuple(v - 18 for v in c)
            _put(img, x, y, c)
    return img


def _rect(img, x0, y0, x1, y1, c):
    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1):
            _put(img, x, y, c)


def icon(kind):
    img = _img()
    black, yel, olive, steel, brass, canvas = (34, 34, 38), (222, 176, 44), (96, 104, 62), (120, 118, 112), (196, 160, 80), (214, 204, 180)
    if kind == "siege_mortar":
        _rect(img, 2, 13, 13, 14, (130, 128, 120))
        _rect(img, 4, 11, 11, 12, steel)
        _rect(img, 5, 8, 10, 10, yel)
        for i in range(7):
            _rect(img, 7 + i, 7 - i, 9 + i, 8 - i, black)
    elif kind == "self_propelled_howitzer":
        _rect(img, 1, 11, 14, 13, (60, 58, 54))
        _rect(img, 2, 8, 13, 10, (126, 112, 76))
        _rect(img, 2, 5, 6, 7, (126, 112, 76))
        for i in range(8):
            _put(img, 8 + i, 7 - i // 2, black)
            _put(img, 8 + i, 8 - i // 2, black)
    elif kind == "flak_gun":
        _rect(img, 2, 14, 13, 14, olive)
        _rect(img, 6, 10, 9, 13, olive)
        for i in range(8):
            _put(img, 6 + i, 9 - i, black)
            _put(img, 8 + i, 10 - i, black)
    elif kind == "observation_balloon":
        _rect(img, 2, 2, 13, 6, canvas)
        _rect(img, 1, 3, 14, 5, canvas)
        _rect(img, 2, 4, 13, 4, (170, 40, 40))
        _put(img, 6, 8, steel)
        _put(img, 9, 8, steel)
        _rect(img, 6, 10, 9, 12, (160, 124, 70))
    elif kind == "heavy_shell":
        _rect(img, 6, 6, 9, 14, brass)
        _rect(img, 6, 3, 9, 5, steel)
        _rect(img, 7, 1, 8, 2, steel)
        _rect(img, 6, 11, 9, 11, (140, 110, 50))
    elif kind == "flak_shell":
        _rect(img, 6, 8, 9, 13, brass)
        _rect(img, 6, 5, 9, 7, (170, 40, 40))
        _rect(img, 7, 3, 8, 4, steel)
    elif kind == "range_finder":
        _rect(img, 1, 6, 14, 9, black)
        _rect(img, 1, 6, 3, 9, brass)
        _rect(img, 12, 6, 14, 9, brass)
        _rect(img, 6, 4, 9, 5, steel)
        _put(img, 2, 7, (180, 220, 255))
        _put(img, 13, 7, (180, 220, 255))
    return img


def draw_all(save):
    for name, img in (("ar_yellow", yellow()), ("ar_concrete", concrete()), ("ar_deck", deck()), ("ar_armor", armor()),
                      ("ar_wicker", wicker())):
        save(img, "block", name)
    for item in ITEMS:
        save(icon(item), "item", item)

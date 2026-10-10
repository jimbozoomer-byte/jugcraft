"""The Monkey King: a boss three times the Monkey Monk's scale, 10 October 2026. A still model, no clips until the
owner asks.

- Head: a wide cream face with a heavy brow ridge, fierce amber eyes with black pupils, a broad muzzle with nostrils,
  a snarl with fangs, big round ears, a fur ruff of jagged tufts round the jaw and crown; the Phoenix-Feather Crown:
  a gold band with three upright plates, a red jewel, scrolled ends and two long pheasant feathers sweeping up and
  back, barred teal with an eye at each tip; a flame halo of gold and red tongues floating behind.
- Body: the Golden Chain Mail: a cuirass of overlapping gold lames with a round mirror plate at the chest set with
  jade, a riveted gold collar, great layered pauldrons; a red sash under a gold belt with a tiger-head buckle; the
  tiger-skin kilt of striped lappets; a red cape with gold cloud squares and dark trim hanging from the pauldrons in
  folded panels; a thick furred tail curling high.
- Arms: furred upper arms, gold vambraces with studs and a jade stud, big furred hands with long fingers; the right
  gripping the staff.
- Legs: furred thighs under the kilt, gold knee plates, black cloud-walking boots with gold trim, red laces and
  upturned toes.
- Staff (its own project, art/monkey_king/ruyi_staff_large.bbmodel): the great red iron staff with gold ends.

Units are pixels, the king facing +z with the ground at y = 0 (about 150 pixels to the feather tips).

    python tools/monkey_king.py --bbmodel --preview out.png
"""
import math
from pathlib import Path

import clean_metal
from steampunk_models import box, cyl

ROOT = Path(__file__).resolve().parents[1]
ART = ROOT / "art" / "monkey_king"

(FUR, FUR_DARK, CREAM, SCALE, SCALE_DARK, GOLD, GOLD_DARK, RED, RED_DARK, CAPE, TRIM, TIGER, TIGER_DARK, BOOT, FEATHER,
 FEATHER_DARK, JADE, BLACK, WHITE, IRON, FLAME, FLAME_DARK) = (
    "mkk_fur", "mkk_fur_dark", "mkk_cream", "mkk_scale", "mkk_scale_dark", "mkk_gold", "mkk_gold_dark", "mkk_red",
    "mkk_red_dark", "mkk_cape", "mkk_trim", "mkk_tiger", "mkk_tiger_dark", "mkk_boot", "mkk_feather", "mkk_feather_dark",
    "mkk_jade", "mkk_black", "mkk_white", "mkk_iron", "mkk_flame", "mkk_flame_dark")

# Joints, in pixels (three times the Monkey Monk's).
HIP = (0, 42, 0)
NECK = (0, 84, 0)
SHOULDERS = {"left_arm": (19, 79, 0), "right_arm": (-19, 79, 0)}
LEGS = {"left_leg": (7.2, 42, 0), "right_leg": (-7.2, 42, 0)}
TAIL = (0, 45, -8)
HAND = (0, -48, 0)
REST = {"left_arm": (-12, 0, 12), "right_arm": (-24, 0, -16), "staff": (24, 0, 16), "head": (8, -5, 0), "tail": (0, 0, 0)}


def _turned(elements, rotation):
    out = []
    for item in elements:
        frm, to, texture = item[:3]
        options = dict(item[3]) if len(item) > 3 else {}
        options["rotation"] = rotation
        out.append((frm, to, texture, options))
    return out


def tufts(points, size, tex, axis="z"):
    """Jagged fur tufts: a box at each point, turned 22.5 degrees by turns, so a ruff reads as hair not plates."""
    out = []
    for i, (x, y, z) in enumerate(points):
        a = 22.5 if i % 2 else -22.5
        out.append(box((x - size[0] / 2, y - size[1] / 2, z - size[2] / 2), (x + size[0] / 2, y + size[1] / 2, z + size[2] / 2),
                       tex, (axis, a, [x, y, z])))
    return out


# ------------------------------------------------------------------ parts (each from its joint)

def head():
    m = []
    m.append(box((-12, 0, -12), (12, 24, 12), FUR))
    # The face: a cream plate, brow ridge, deep sockets with fierce eyes, a broad muzzle, nostrils, a snarl with fangs.
    m.append(box((-10.5, 2.5, 12), (10.5, 18.5, 13.4), CREAM))
    m.append(box((-11.4, 16, 12.4), (11.4, 19.6, 15.2), FUR))
    m.append(box((-11, 17, 15.2), (11, 18.6, 15.9), FUR_DARK))
    m.append(box((-6.5, 1.5, 13.4), (6.5, 10.5, 18.6), CREAM))
    m.append(box((-7.4, 1.5, 13.4), (7.4, 5, 17.6), CREAM))
    for x in (-3.4, 1.4):
        m.append(box((x, 7.6, 18.6), (x + 2, 9.2, 19.2), FUR_DARK))
    m.append(box((-5, 4, 18.6), (5, 5, 19.0), FUR_DARK))
    for x in (-4.2, 2.8):
        m.append(box((x, 2.6, 18.6), (x + 1.4, 4.2, 19.4), WHITE))
    for x0, x1 in ((-9.4, -3.2), (3.2, 9.4)):
        m.append(box((x0, 11.5, 13.4), (x1, 15.8, 13.9), FUR_DARK))
        m.append(box((x0 + 1.2, 12.4, 13.9), (x1 - 1.2, 15, 14.3), GOLD))
        m.append(box((x0 + 2.4, 12.8, 14.3), (x1 - 2.4, 14.6, 14.6), BLACK))
        m.append(box((x0 + 1.6, 14.0, 14.6), (x0 + 2.6, 14.8, 14.75), WHITE))
    # Ears, big and round, cream inside.
    for x in (-19, 13):
        m += cyl("x", 13, -2.5, 6.6, x, x + 6, FUR)
        m += cyl("x", 13, -2.5, 4.4, x + (4 if x > 0 else 0), x + (6.3 if x > 0 else 2), CREAM)
    # The ruff: jagged tufts round the jaw and the sides, and the crown tufts.
    m += tufts([(x, 0.5, 9) for x in (-11, -7, -3, 1, 5, 9)], (4.4, 5, 4), FUR_DARK)
    m += tufts([(-14.5, y, 4) for y in (3, 8, 13)] + [(14.5, y, 4) for y in (3, 8, 13)], (4, 5, 5), FUR_DARK, "x")
    m += tufts([(x, 1.5, -8) for x in (-9, -3, 3, 9)], (4.4, 4, 5), FUR_DARK)
    m += tufts([(-4, 25.5, -3), (0, 27, -1), (4, 25.5, -4), (1.5, 29, -5)], (4, 5, 4), FUR)
    # The Phoenix-Feather Crown: a gold band, three upright plates with a jewel, scrolled ends, and two feathers.
    m.append(box((-12.6, 19.2, -12.6), (12.6, 22.8, 12.6), GOLD))
    m.append(box((-12.9, 20.4, -12.9), (12.9, 21.6, 12.9), GOLD_DARK))
    for x0, x1, top in ((-9, -3, 28), (-3.2, 3.2, 31), (3, 9, 28)):
        m.append(box((x0, 22.8, 11.2), (x1, top, 13.2), GOLD))
        m.append(box((x0 + 1.2, 23.6, 13.2), (x1 - 1.2, top - 1.8, 13.7), GOLD_DARK))
    m += cyl("z", 0, 26.5, 2.2, 13.2, 14.6, RED)
    m += cyl("z", 0, 26.5, 1, 14.6, 15.2, WHITE)
    for x in (-13.8, 11):
        m.append(box((x, 22.8, 8), (x + 2.8, 27, 12), GOLD))
        m.append(box((x, 27, 9), (x + 2.8, 29.2, 11), GOLD_DARK))
    for side in (-1, 1):
        x = side * 7
        # Each feather: a quill of overlapping boxes sweeping up and back along a curve, a barred vane widening
        # along the last third, and a gold-and-black eye at the tip.
        n = 16
        for i in range(n + 1):
            t = i / n
            y = 23 + 56 * t
            z = -1 - 34 * t * t
            vane = t > 0.55
            w = 1.1 + (2.4 * (t - 0.55) / 0.45 if vane else 0)
            d = 2.6 + (1.4 if vane else 0)
            tex = FEATHER if (vane and i % 2 == 0) else FEATHER_DARK
            m.append(box((x - w, y - 2.4, z - d), (x + w, y + 2.4, z + d), tex))
        y, z = 23 + 56, -1 - 34
        m.append(box((x - 3.6, y - 1, z - 4.6), (x + 3.6, y + 4.5, z + 4.6), FEATHER))
        m.append(box((x - 2, y + 0.4, z - 4.9), (x + 2, y + 3.2, z + 4.9), GOLD))
        m.append(box((x - 1, y + 1.2, z - 5.1), (x + 1, y + 2.4, z + 5.1), BLACK))
    # The flame halo behind: an octagonal gold ring with tongues of flame leaping outward from it, each a stack of
    # three shrinking boxes along the ring's radius.
    r = 24
    half = r * math.tan(math.radians(22.5))
    for angle in (0, 45):
        turn = ("z", angle, [0, 12, -16])
        for sign in (1, -1):
            m.append(box((-half, 12 + sign * r - 1.6, -17.2), (half, 12 + sign * r + 1.6, -14.8), GOLD, turn))
            m.append(box((sign * r - 1.6, 12 - half, -17.2), (sign * r + 1.6, 12 + half, -14.8), GOLD, turn))
    for i in range(12):
        a = math.pi / 2 + 2 * math.pi * i / 12
        if math.sin(a) < -0.6:
            continue
        ux, uy = math.cos(a), math.sin(a)
        tall = (1.0, 0.8, 0.6) if i % 2 == 0 else (0.85, 0.6, 0.4)
        for k, (dist, size) in enumerate(((r + 2.6, 3.4), (r + 6.2, 2.6), (r + 9.2, 1.8))):
            cx, cy = ux * dist * tall[k] / tall[k], 12 + uy * dist
            cx = ux * dist
            tex = FLAME_DARK if k == 0 else FLAME
            m.append(box((cx - size, cy - size, -16.6 - 0.1 * k), (cx + size, cy + size, -15.4 + 0.1 * k), tex,
                         ("z", 22.5 if (i + k) % 2 else -22.5, [cx, cy, -16])))
    return m


def body():
    m = []
    m.append(box((-12, 0, -6.6), (12, 39, 6.6), FUR))
    # The cuirass: a scale-mail body with overlapping lames stepping down the chest and back, a riveted collar.
    m.append(box((-12.6, 12, -7.2), (12.6, 36, 7.2), SCALE))
    for i, y in enumerate((33, 29, 25, 21, 17, 13)):
        d = 7.4 + 0.25 * i
        m.append(box((-12.9 - 0.1 * i, y - 4.4, -d), (12.9 + 0.1 * i, y - 0.8, d), SCALE if i % 2 else SCALE_DARK))
        m.append(box((-12.9 - 0.1 * i, y - 1.2, -d - 0.3), (12.9 + 0.1 * i, y - 0.4, d + 0.3), GOLD_DARK))
    m.append(box((-13.2, 36, -7.6), (13.2, 39.5, 7.6), GOLD))
    for x in (-10, -5, 0, 5, 10):
        m.append(box((x - 0.8, 37, 7.6), (x + 0.8, 38.5, 8.1), GOLD_DARK))
        m.append(box((x - 0.8, 37, -8.1), (x + 0.8, 38.5, -7.6), GOLD_DARK))
    # The mirror plate: a round gold boss on the chest set with jade, on a cross of straps.
    m += cyl("z", 0, 26, 6.5, 7.6, 9.2, GOLD)
    m += cyl("z", 0, 26, 4.6, 9.2, 10, GOLD_DARK)
    m += cyl("z", 0, 26, 2.6, 10, 10.9, JADE)
    for angle in (45, -45):
        m.append(box((-14, 25, 8.2), (14, 27, 8.9), GOLD_DARK, ("z", angle, [0, 26, 8.5])))
    # The pauldrons: three layered gold plates over each shoulder, flaring out, with a dark edge.
    for side in (-1, 1):
        for i, (w, y, d) in enumerate(((9, 36, 9), (10.5, 32.5, 10), (12, 29, 11))):
            x0 = side * 12.5 + (0 if side > 0 else -w)
            x1 = x0 + w
            m.append(box((x0, y, -d), (x1, y + 4.2, d), GOLD, ("z", side * -12, [side * 12.5, 40, 0])))
            m.append(box((x0 + (w - 0.4 if side < 0 else 0), y - 0.8, -d - 0.3), (x1 - (w - 0.4 if side > 0 else 0), y + 0.4, d + 0.3), GOLD_DARK,
                         ("z", side * -12, [side * 12.5, 40, 0])))
    # The sash and belt: a wide red sash, a gold belt with a tiger-head buckle, and the belt's hanging ends.
    m.append(box((-13, 7, -7.4), (13, 13.5, 7.4), RED))
    m.append(box((-13.3, 8.5, -7.7), (13.3, 12, 7.7), GOLD))
    m.append(box((-13.5, 10, -7.9), (13.5, 10.8, 7.9), GOLD_DARK))
    m.append(box((-5, 6.5, 7.7), (5, 14, 9.6), GOLD))
    m.append(box((-3.6, 8, 9.6), (3.6, 12.6, 10.4), TIGER_DARK))
    for x0, x1 in ((-3, -1.2), (1.2, 3)):
        m.append(box((x0, 10.4, 10.4), (x1, 11.6, 10.8), BLACK))
    m.append(box((-1, 8.2, 10.4), (1, 9.6, 10.8), WHITE))
    m.append(box((-16, -8, 2), (-13, 7, 5), RED, ("z", 22.5, [-14.5, 7, 3.5])))
    m.append(box((13, -6, -5), (16, 7, -2), RED, ("z", -22.5, [14.5, 7, -3.5])))
    # The cape: hung from the pauldrons down the back in three folded panels, dark trim, gold cloud squares.
    for x0, x1, dz, low in ((-16, -6, 0, -14), (-6.5, 6.5, 1.2, 9), (6, 16, 0, -14)):
        m.append(box((x0, low, -11 - dz), (x1, 38, -9.6 - dz), CAPE))
        m.append(box((x0, low - 2, -11.4 - dz), (x1, low, -9.4 - dz), TRIM))
        for y in (28, 14, 0):
            if y < low:
                continue
            m.append(box(((x0 + x1) / 2 - 2, y - 2, -11.3 - dz), ((x0 + x1) / 2 + 2, y + 2, -11 - dz), GOLD))
    m.append(box((-16.4, 36, -11.6), (16.4, 39.5, -9.2), TRIM))
    return m


def arm(side):
    m = []
    m.append(box((-6.5, -22, -6.5), (6.5, 2, 6.5), FUR))
    m.append(box((-7.2, -18, -7.2), (7.2, -8, 7.2), FUR))
    # The vambrace: a gold forearm guard with dark bands, studs and a jade stud at the back of the wrist.
    m.append(box((-7, -40, -7), (7, -22, 7), GOLD))
    for y in (-36, -29):
        m.append(box((-7.3, y, -7.3), (7.3, y + 1.4, 7.3), GOLD_DARK))
    for z in (-4, 0, 4):
        m.append(box((side * 7.0 - (0.9 if side > 0 else 0), -33, z - 0.9), (side * 7.0 + (0 if side > 0 else 0.9), -31.2, z + 0.9), GOLD_DARK))
    m += cyl("z", 0, -26, 2, -7.9, -7, JADE)
    m.append(box((-6.4, -42.5, -6.4), (6.4, -40, 6.4), RED_DARK))
    # The hand: furred, cream palm, four long fingers and a thumb.
    m.append(box((-6, -51, -6), (6, -42.5, 6), FUR))
    m.append(box((-5.4, -49, 6), (5.4, -43, 6.8), CREAM))
    for i, x in enumerate((-4.5, -1.5, 1.5, 4.5)):
        m.append(box((x - 1.3, -62 + 1.2 * abs(i - 1.5), -4.5), (x + 1.3, -51, -1), FUR_DARK))
        m.append(box((x - 1.0, -63.5 + 1.2 * abs(i - 1.5), -3.6), (x + 1.0, -61.5 + 1.2 * abs(i - 1.5), -1.4), FUR_DARK))
    thumb = 6 if side > 0 else -9
    m.append(box((thumb, -54, -1.5), (thumb + 3, -45, 3), FUR_DARK))
    return m


def tail():
    """A thick tail curling high behind, jointed furred segments stepping back and up, ending in a dark tuft."""
    m = []
    steps = [(0, -8), (0.3, -14), (1.2, -20), (2.8, -25.5), (5.2, -30.5), (8.4, -35), (12.2, -38.6), (16.5, -41.4),
             (21, -43.2), (25.6, -43.8), (30, -43), (34, -41), (37.4, -38), (40, -34.4)]
    for i, (y, z) in enumerate(steps):
        r = 4.4 - 0.17 * i
        m.append(box((-r, y - r, z - r - 1.2), (r, y + r, z + r + 1.2), FUR if i % 2 == 0 else FUR_DARK))
    y, z = steps[-1]
    m.append(box((-4, y - 2, z - 4), (4, y + 7, z + 6), FUR_DARK))
    m += tufts([(-2, y + 9, z + 1), (2, y + 9, z - 1)], (3.5, 4.5, 3.5), FUR_DARK)
    return m


def kilt():
    """The tiger-skin kilt: striped lappets round the hips, over the fur."""
    m = []
    m += cyl("y", 0, 0, 13.6, -2.5, 0.4, FUR_DARK)
    for i in range(14):
        a = 2 * math.pi * (i + 0.5) / 14
        cx, cz = 15.6 * math.sin(a), 9.6 * math.cos(a)
        tex = TIGER if i % 2 else TIGER_DARK
        if abs(math.sin(a)) > 0.7:
            lean = ("z", -10 if cx > 0 else 10, [cx, 0.4, cz])
        else:
            lean = ("x", 10 if cz > 0 else -10, [cx, 0.4, cz])
        length = 16 if i % 3 else 13
        m.append(box((cx - 3.2, -length, cz - 2.2), (cx + 3.2, 0.4, cz + 2.2), tex, lean))
        m.append(box((cx - 2, -length - 3, cz - 1.5), (cx + 2, -length, cz + 1.5), tex, lean))
    return m


def leg():
    m = []
    m.append(box((-6, -20, -6), (6, -0.3, 6), FUR))
    m.append(box((-6.8, -24, -6.8), (6.8, -19, 6.8), GOLD))
    m.append(box((-5, -25.5, 6.8), (5, -17.5, 8.2), GOLD))
    m += cyl("z", 0, -21.5, 2, 8.2, 9, GOLD_DARK)
    # The cloud boots: black with gold trim bands, red laces, a thick sole and upturned toes.
    m.append(box((-6.4, -36, -6.4), (6.4, -24, 6.4), BOOT))
    for y in (-26.5, -33):
        m.append(box((-6.7, y, -6.7), (6.7, y + 1.6, 6.7), GOLD))
    for y in (-31, -29):
        m.append(box((-2.2, y, 6.4), (2.2, y + 0.8, 6.9), RED))
    m.append(box((-6.8, -40, -7), (6.8, -36, 10), BOOT))
    m.append(box((-7.2, -42, -7.4), (7.2, -40, 10.6), BLACK))
    m.append(box((-5.4, -38, 10), (5.4, -34, 13), BOOT, ("x", -22.5, [0, -38, 10])))
    m.append(box((-4, -35, 12.4), (4, -33, 14), GOLD, ("x", -22.5, [0, -38, 10])))
    return m


def staff():
    """The great ruyi staff from the fist, upright: a thick red iron shaft, a gold grip band, heavy gold ends with
    dark bands and domed caps, a few gold rings along it."""
    m = []
    m += cyl("y", 0, 0, 3.2, -27, 95, RED)
    m += cyl("y", 0, 0, 3.8, -11, -8.5, GOLD_DARK)
    m += cyl("y", 0, 0, 3.8, 8.5, 11, GOLD_DARK)
    for y0, y1 in ((-36, -27), (95, 104)):
        m += cyl("y", 0, 0, 4.6, y0, y1, GOLD)
    for y0, y1 in ((-34, -32), (-30, -28.5), (96.5, 98), (101, 103)):
        m += cyl("y", 0, 0, 5, y0, y1, GOLD_DARK)
    m += cyl("y", 0, 0, 3.6, -39, -36, GOLD)
    m += cyl("y", 0, 0, 3.6, 104, 107.5, GOLD)
    m += cyl("y", 0, 0, 1.8, 107.5, 110, GOLD_DARK)
    for y in (18, 40, 62, 84):
        m += cyl("y", 0, 0, 3.45, y, y + 2.2, GOLD_DARK)
    return m


def shifted(item, offset):
    frm, to, texture = item[:3]
    options = dict(item[3]) if len(item) > 3 else {}
    if "rotation" in options:
        axis, angle, origin = options["rotation"][:3]
        options["rotation"] = (axis, angle, [origin[k] + offset[k] for k in range(3)])
    moved = ([frm[k] + offset[k] for k in range(3)], [to[k] + offset[k] for k in range(3)], texture)
    return moved + ((options,) if options else ())


def add(*points):
    return [sum(p[k] for p in points) for k in range(3)]


def groups(with_staff=False):
    out = [("body", HIP, None, [shifted(i, HIP) for i in body()]),
           ("kilt", HIP, None, [shifted(i, HIP) for i in kilt()], "body"),
           ("head", NECK, REST.get("head"), [shifted(i, NECK) for i in head()], "body"),
           ("tail", TAIL, REST.get("tail"), [shifted(i, TAIL) for i in tail()], "body")]
    for name, joint in SHOULDERS.items():
        side = 1 if "left" in name else -1
        out.append((name, joint, REST.get(name), [shifted(i, joint) for i in arm(side)], "body"))
    if with_staff:
        hand = add(SHOULDERS["right_arm"], HAND)
        out.append(("staff", hand, REST.get("staff"), [shifted(i, hand) for i in staff()], "right_arm"))
    for name, joint in LEGS.items():
        out.append((name, joint, None, [shifted(i, joint) for i in leg()]))
    return out


def posed(with_staff=True):
    parts = groups(with_staff)
    tree = {g[0]: g for g in parts}
    parents = {g[0]: (g[4] if len(g) > 4 else None) for g in parts}

    def chain(name):
        turns = []
        while name:
            g = tree[name]
            rx, ry, rz = g[2] or (0, 0, 0)
            for axis, angle in (("z", rz), ("y", ry), ("x", rx)):
                if angle:
                    turns.append((axis, angle, list(g[1])))
            name = parents[name]
        return turns

    out = []
    for g in parts:
        turns = chain(g[0])
        for item in g[3]:
            frm, to, texture = item[:3]
            options = dict(item[3]) if len(item) > 3 else {}
            options["rotations"] = turns
            out.append((frm, to, texture, options))
    return out


def write_bbmodel(folder=ART):
    import blockbench_export
    king = blockbench_export.write(folder / "monkey_king.bbmodel", "monkey_king", groups(False),
                                   draw=lambda name: TEXTURES[name]())
    blockbench_export.write(folder / "ruyi_staff_large.bbmodel", "ruyi_staff_large",
                            [("ruyi_staff_large", (0, 0, 0), None, staff())], draw=lambda name: TEXTURES[name]())
    return king


# ------------------------------------------------------------------ art (flat, clean, 16 x 16)

def plain(fill, light, shade):
    img = clean_metal.canvas(fill)
    clean_metal.bevel(img, 0, 0, 15, 15, light, shade, fill)
    return img


def fur(fill, light, shade):
    img = clean_metal.canvas(fill)
    for y in range(16):
        for x in range(16):
            if (x * 3 + y * 5) % 11 == 0:
                clean_metal.put(img, x, y, light)
            elif (x * 7 + y * 3) % 13 == 0:
                clean_metal.put(img, x, y, shade)
    return img


def scales(fill, light, shade):
    """Scale mail: rows of overlapping rounded scales four pixels wide, each lit along its top and edged below."""
    img = clean_metal.canvas(fill)
    for row in range(4):
        y0 = row * 4
        off = 2 if row % 2 else 0
        for col in range(5):
            x0 = col * 4 - off
            for dx in range(4):
                x = x0 + dx
                if 0 <= x < 16:
                    clean_metal.put(img, x, y0, light if dx in (1, 2) else fill)
                    clean_metal.put(img, x, y0 + 3, shade)
                    if dx in (0, 3):
                        clean_metal.put(img, x, y0 + 2, shade)
    return img


def tiger(fill, light, stripe):
    img = clean_metal.canvas(fill)
    for y in range(16):
        for x in range(16):
            if (x + y * 2) % 9 in (0, 1) or (x * 2 - y) % 11 == 0:
                clean_metal.put(img, x, y, stripe)
            elif (x + y) % 5 == 0:
                clean_metal.put(img, x, y, light)
    return img


def cape(fill, light, shade, gold):
    img = plain(fill, light, shade)
    for cx, cy in ((4, 4), (11, 11)):
        for dx, dy in ((0, 0), (1, 0), (0, 1), (1, 1), (-1, 1), (2, 1)):
            clean_metal.put(img, cx + dx, cy + dy, gold)
    return img


def feather(fill, bar, light):
    img = clean_metal.canvas(fill)
    for y in range(16):
        c = bar if y % 4 == 1 else light if y % 4 == 3 else fill
        clean_metal.rect(img, 0, y, 15, y, c)
    clean_metal.rect(img, 7, 0, 8, 15, bar)
    return img


TEXTURES = {FUR: lambda: fur((190, 136, 68), (218, 166, 94), (150, 102, 48)),
            FUR_DARK: lambda: fur((128, 86, 42), (156, 110, 58), (94, 60, 26)),
            CREAM: lambda: fur((236, 216, 180), (248, 236, 208), (204, 180, 140)),
            SCALE: lambda: scales((214, 170, 60), (244, 208, 112), (156, 114, 32)),
            SCALE_DARK: lambda: scales((190, 148, 48), (222, 184, 92), (136, 98, 26)),
            GOLD: lambda: plain((226, 182, 70), (248, 216, 124), (168, 124, 38)),
            GOLD_DARK: lambda: plain((168, 124, 38), (198, 152, 58), (118, 84, 26)),
            RED: lambda: plain((170, 42, 38), (202, 70, 62), (120, 26, 24)),
            RED_DARK: lambda: plain((124, 30, 28), (152, 48, 44), (84, 18, 18)),
            CAPE: lambda: cape((158, 36, 40), (186, 58, 60), (110, 22, 26), (226, 182, 70)),
            TRIM: lambda: plain((88, 24, 30), (112, 36, 44), (58, 14, 18)),
            TIGER: lambda: tiger((222, 150, 62), (240, 180, 100), (36, 28, 22)),
            TIGER_DARK: lambda: tiger((200, 132, 50), (222, 160, 84), (30, 24, 20)),
            BOOT: lambda: plain((40, 36, 42), (62, 56, 64), (24, 20, 26)),
            FEATHER: lambda: feather((46, 132, 128), (26, 84, 90), (92, 176, 168)),
            FEATHER_DARK: lambda: feather((34, 100, 104), (20, 64, 70), (66, 138, 136)),
            JADE: lambda: plain((92, 170, 128), (134, 204, 162), (56, 120, 86)),
            BLACK: lambda: plain((24, 20, 20), (44, 38, 38), (12, 10, 10)),
            WHITE: lambda: plain((242, 240, 232), (252, 252, 248), (208, 204, 196)),
            IRON: lambda: plain((110, 112, 118), (140, 142, 148), (76, 78, 84)),
            FLAME: lambda: plain((246, 196, 60), (254, 230, 120), (210, 140, 30)),
            FLAME_DARK: lambda: plain((226, 110, 40), (246, 150, 70), (170, 70, 24))}


def draw_all(save):
    for name, draw in TEXTURES.items():
        save(draw(), "block", name)


if __name__ == "__main__":
    import sys
    print(sum(len(g[3]) for g in groups(True)), "boxes in", len(groups(True)), "groups")
    if "--bbmodel" in sys.argv:
        print(write_bbmodel())
    if "--preview" in sys.argv:
        import box_preview
        draw = lambda n: TEXTURES[n]()
        out = sys.argv[-1]
        for yaw, suffix in ((30, "front_left"), (-30, "front_right"), (90, "side"), (-150, "back")):
            box_preview.render(posed(), scale=3, yaw=yaw, pitch=10, draw=draw).save(out.replace(".png", f"_{suffix}.png"))
        print(out)

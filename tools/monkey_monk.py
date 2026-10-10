"""The Monkey Monk: a wandering monkey sage of the old tales, 10 October 2026. With clips, once the owner approved the model (10 October 2026): idle, walk, jump and attack, all
slow, floaty and smooth, the tail and free hand trailing behind the body.

- Head: golden-brown fur with a cream muzzle and brow, a flat nose, narrow eyes under a heavy brow, big round ears,
  cheek ruffs, a tuft on the crown, and a golden circlet with a red jewel at the brow. Behind the head floats a thin
  golden halo ring.
- Body: a lean furred torso, the chest cream; a saffron robe worn off the right shoulder with dark red trim, a
  wrapped sash with a jade pendant, prayer beads across the chest, a gold belt plate.
- Arms: long monkey arms, the left in a loose saffron sleeve, the right bare with a gold armband; both ending in
  furred hands with long fingers and wrist wraps. The right hand is posed to hold the staff.
- Legs: bare furred legs in short saffron trousers wrapped at the knee, with long-toed monkey feet.
- Tail: long, curling up behind in jointed segments to a dark tuft.
- Staff (its own project, art/monkey_monk/ruyi_staff.bbmodel): the red iron staff with gold bands and caps.

Units are pixels, the monk facing +z with the ground at y = 0.

    python tools/monkey_monk.py --bbmodel --preview out.png
"""
import math
from pathlib import Path

import clean_metal
from steampunk_models import box, cyl

ROOT = Path(__file__).resolve().parents[1]
ART = ROOT / "art" / "monkey_monk"

FUR, FUR_DARK, CREAM, SAFFRON, SAFFRON_DARK, TRIM, GOLD, GOLD_DARK, JADE, RED, BLACK, BEAD, WRAP, IRON = (
    "mk_fur", "mk_fur_dark", "mk_cream", "mk_saffron", "mk_saffron_dark", "mk_trim", "mk_gold", "mk_gold_dark",
    "mk_jade", "mk_red", "mk_black", "mk_bead", "mk_wrap", "mk_iron")

# Joints, in pixels: a lean figure, legs 14, body 13, head 8.
HIP = (0, 14, 0)
NECK = (0, 28, 0)
SHOULDERS = {"left_arm": (5.5, 26.5, 0), "right_arm": (-5.5, 26.5, 0)}
LEGS = {"left_leg": (2, 14, 0), "right_leg": (-2, 14, 0)}
TAIL = (0, 15, -2.5)
HAND = (0, -15.5, 0)
# The rest pose: the staff planted upright at his right side, gripped at chest height; the free arm easy; the head
# carried a little forward.
REST = {"left_arm": (-12, 0, 10), "right_arm": (-22, 0, -22), "staff": (22, 0, 22), "head": (10, -6, 0),
        "tail": (0, 0, 0)}


def _turned(elements, rotation):
    out = []
    for item in elements:
        frm, to, texture = item[:3]
        options = dict(item[3]) if len(item) > 3 else {}
        options["rotation"] = rotation
        out.append((frm, to, texture, options))
    return out


# ------------------------------------------------------------------ parts (each from its joint)

def head():
    """A monkey's head: a wide cream face plate with a heavy brow ridge, deep-set eyes with pale rims, a broad muzzle
    with nostrils and a mouth line; big round ears with cream insides; a fur ruff round the head and a crown tuft;
    the jewelled circlet; and an octagonal gold halo floating behind."""
    m = []
    m.append(box((-4, 0, -4), (4, 8, 4), FUR))
    # The face plate and brow ridge.
    m.append(box((-3.6, 0.8, 4), (3.6, 6.2, 4.6), CREAM))
    m.append(box((-3.8, 5.4, 4.2), (3.8, 6.6, 5.1), FUR))
    m.append(box((-2.0, 0.4, 4.6), (2.0, 3.6, 6.4), CREAM))
    m.append(box((-2.4, 0.4, 4.6), (2.4, 1.6, 6.0), CREAM))
    for x in (-1.1, 0.5):
        m.append(box((x, 2.6, 6.4), (x + 0.6, 3.1, 6.6), FUR_DARK))
    m.append(box((-1.4, 1.3, 6.4), (1.4, 1.6, 6.55), FUR_DARK))
    # Eyes: dark sockets with amber eyes and a glint, under the brow.
    for x0, x1 in ((-3.1, -1.1), (1.1, 3.1)):
        m.append(box((x0, 3.9, 4.6), (x1, 5.3, 4.75), FUR_DARK))
        m.append(box((x0 + 0.4, 4.2, 4.75), (x1 - 0.4, 5.0, 4.9), GOLD_DARK))
        m.append(box((x0 + 0.5, 4.6, 4.9), (x0 + 0.9, 4.9, 5.0), CREAM))
    # Big round ears, cream inside, standing out from the head.
    for x in (-6.4, 4.4):
        m += cyl("x", 4.4, -0.8, 2.3, x, x + 2, FUR)
        m += cyl("x", 4.4, -0.8, 1.5, x + (1.3 if x > 0 else 0), x + (2.1 if x > 0 else 0.8), CREAM)
    # The fur ruff: tufts round the jaw and cheeks, and the crown tuft.
    for x0, x1, y0, y1, z0, z1 in ((-5.6, -4, 0.2, 4.6, 1, 4.4), (4, 5.6, 0.2, 4.6, 1, 4.4), (-4.6, 4.6, -1.6, 0.6, 1.5, 4.8),
                                   (-5.2, -3.4, -0.8, 1.2, -2, 1.6), (3.4, 5.2, -0.8, 1.2, -2, 1.6), (-4.4, 4.4, -1.2, 0.3, -4.4, -1.4)):
        m.append(box((x0, y0, z0), (x1, y1, z1), FUR_DARK))
    m.append(box((-2, 8, -3), (2, 10, 0.5), FUR))
    m.append(box((-1, 10, -2.5), (1, 11.8, -1), FUR_DARK))
    m.append(box((0.4, 11.4, -2.4), (1.4, 12.6, -1.4), FUR_DARK, ("z", -22.5, [0.9, 11.4, -1.9])))
    # The circlet: a gold band round the brow with a red jewel and two scrolled ends.
    m.append(box((-4.3, 6.4, -4.3), (4.3, 7.6, 4.3), GOLD))
    m.append(box((-4.5, 6.8, -4.5), (4.5, 7.2, 4.5), GOLD_DARK))
    m.append(box((-1.1, 6.0, 4.3), (1.1, 8.2, 5.0), GOLD))
    m.append(box((-0.55, 6.5, 5.0), (0.55, 7.7, 5.4), RED))
    for x in (-3.8, 2.8):
        m.append(box((x, 7.6, 3.2), (x + 1, 9, 4.4), GOLD))
    # The halo: an octagonal gold ring floating behind the head, eight short bars, with studs at the points.
    r = 8.4
    half = r * math.tan(math.radians(22.5))
    for angle in (0, 45):
        turn = ("z", angle, [0, 5, -6.7])
        m.append(box((-half, 5 + r - 0.6, -7.0), (half, 5 + r + 0.6, -6.4), GOLD, turn))
        m.append(box((-half, 5 - r - 0.6, -7.0), (half, 5 - r + 0.6, -6.4), GOLD, turn))
        m.append(box((-r - 0.6, 5 - half, -7.0), (-r + 0.6, 5 + half, -6.4), GOLD, turn))
        m.append(box((r - 0.6, 5 - half, -7.0), (r + 0.6, 5 + half, -6.4), GOLD, turn))
    for angle in (0, 45):
        turn = ("z", angle, [0, 5, -6.7])
        m.append(box((-0.8, 5 + r - 0.8, -7.15), (0.8, 5 + r + 0.8, -6.25), GOLD_DARK, turn))
        m.append(box((-0.8, 5 - r - 0.8, -7.15), (0.8, 5 - r + 0.8, -6.25), GOLD_DARK, turn))
    return m


def body():
    """The furred torso with a cream chest, the saffron robe off one shoulder with its trim, the sash, the jade
    pendant, the beads and the belt plate."""
    m = []
    m.append(box((-4, 0, -2.2), (4, 13, 2.2), FUR))
    m.append(box((-2.6, 6, 2.2), (2.6, 12.5, 2.5), CREAM))
    # The robe: over the left shoulder and across the chest, open at the right, with dark red trim along its edge.
    m.append(box((-4.3, 0.5, -2.5), (4.3, 9, 2.5), SAFFRON))
    m.append(box((-0.5, 9, -2.6), (4.5, 13.4, 2.6), SAFFRON))
    m.append(box((4.3, 9, -2.8), (4.9, 13.6, 2.8), SAFFRON_DARK))
    m.append(box((-0.5, 8.6, 2.5), (0.5, 13.6, 3.0), TRIM, ("z", 22.5, [0, 13.6, 2.7])))
    m.append(box((-4.4, 8.6, 2.5), (-0.4, 9.2, 2.9), TRIM))
    m.append(box((-4.5, 0.3, -2.7), (4.5, 1.2, 2.7), TRIM))
    # The sash: a wrapped band, its knot and tails at the left hip, a jade pendant hanging in front.
    m.append(box((-4.6, 3.2, -2.8), (4.6, 5.8, 2.8), SAFFRON_DARK))
    m.append(box((-4.8, 4.2, -3.0), (4.8, 4.8, 3.0), TRIM))
    m.append(box((4.4, 2.5, -1.5), (6, 5.5, 1.5), SAFFRON_DARK))
    m.append(box((5, -2, -0.5), (6, 2.5, 0.8), SAFFRON_DARK, ("x", -22.5, [5.5, 2.5, 0])))
    m.append(box((-1.4, 2, 2.8), (1.4, 4.4, 3.4), GOLD))
    m.append(box((-0.8, 0.2, 3.0), (0.8, 2, 3.5), JADE))
    # A tiger-skin kilt over the trousers, in striped panels round the hips, and gold anklets on the legs below.
    for i in range(10):
        a = 2 * math.pi * (i + 0.5) / 10
        cx, cz = 5.0 * math.sin(a), 3.2 * math.cos(a)
        tex = SAFFRON_DARK if i % 2 else BLACK
        lean = ("z", -12 if cx > 0.5 else 12 if cx < -0.5 else 0, [cx, 0.5, cz]) if abs(math.sin(a)) > 0.7 \
            else ("x", 12 if cz > 0 else -12, [cx, 0.5, cz])
        m.append(box((cx - 1.1, -5 + 0.8 * (i % 2), cz - 0.9), (cx + 1.1, 0.5, cz + 0.9), tex, lean))
    # Prayer beads across the chest from the left shoulder.
    for i in range(8):
        a = math.pi * (i + 0.5) / 8
        x, y = 4.0 * math.cos(a) - 0.5, 12.5 - 5 * math.sin(a)
        m.append(box((x - 0.5, y - 0.5, 2.6), (x + 0.5, y + 0.5, 3.6), BEAD))
    return m


def arm(side):
    """A long monkey arm: the left in a loose saffron sleeve with trim; the right bare fur with a gold armband; both
    with a wrist wrap and a furred hand with long fingers and a thumb."""
    m = []
    if side > 0:
        m.append(box((-2.6, -8, -2.6), (2.6, 1, 2.6), SAFFRON))
        m.append(box((-3.2, -9, -3.2), (3.2, -8, 3.2), TRIM))
        m.append(box((-2, -12, -2), (2, -8, 2), FUR))
    else:
        m.append(box((-2.2, -12, -2.2), (2.2, 1, 2.2), FUR))
        m.append(box((-2.5, -4, -2.5), (2.5, -2.2, 2.5), GOLD))
        m.append(box((-2.7, -3.4, -2.7), (2.7, -2.9, 2.7), GOLD_DARK))
    m.append(box((-2.2, -13.2, -2.2), (2.2, -12, 2.2), WRAP))
    m.append(box((-2, -16.5, -2), (2, -13.2, 2), FUR))
    m.append(box((-1.8, -15.5, 2), (1.8, -13.2, 2.4), CREAM))
    for i, x in enumerate((-1.5, -0.5, 0.5, 1.5)):
        m.append(box((x - 0.45, -20.4 + 0.4 * abs(i - 1.5), -1.4), (x + 0.45, -16.5, -0.2), FUR_DARK))
    thumb = 2 if side > 0 else -3
    m.append(box((thumb, -17.5, -0.3), (thumb + 1, -14.5, 1), FUR_DARK))
    return m


def tail():
    """A long tail curling up behind: a chain of furred segments stepping back and up into a hook, each overlapping
    the last, ending in a dark tuft."""
    m = []
    steps = ((0, 0, -3.5), (0.2, 0, -6.5), (0.8, 0, -9.5), (1.8, 0, -12), (3.4, 0, -14), (5.4, 0, -15.2), (7.6, 0, -15.6),
             (9.6, 0, -15.0), (11.2, 0, -13.6))
    size = 1.4
    for i, (y, x, z) in enumerate(steps):
        r = size - 0.08 * i
        m.append(box((x - r, y - r, z - 2.2), (x + r, y + r + 0.4 * (i > 3), z + 1.6), FUR if i % 2 == 0 else FUR_DARK))
    y, x, z = steps[-1]
    m.append(box((x - 1.5, y - 0.6, z - 1.2), (x + 1.5, y + 2.4, z + 2.4), FUR_DARK))
    return m


def leg():
    """A bare furred leg in short saffron trousers wrapped at the knee, with a long-toed monkey foot."""
    m = []
    m.append(box((-2.5, -7, -2.5), (2.5, 0.5, 2.5), SAFFRON))
    m.append(box((-2.7, -7.5, -2.7), (2.7, -6.2, 2.7), WRAP))
    m.append(box((-2.3, -11.8, -2.3), (2.3, -10.6, 2.3), GOLD))
    m.append(box((-2.1, -12.5, -2.1), (2.1, -7.5, 2.1), FUR))
    m.append(box((-2.4, -14, -2.6), (2.4, -12.5, 2.6), FUR))
    for i, x in enumerate((-1.6, -0.5, 0.6, 1.7)):
        m.append(box((x - 0.5, -14, 2.6), (x + 0.5, -12.8, 5 - 0.5 * abs(i - 1.5)), FUR_DARK))
    m.append(box((-2.9, -14, 0.5), (-2.2, -12.8, 2.8), FUR_DARK))
    return m


def staff():
    """The ruyi staff from the fist, standing upright: a thick red iron shaft with a gold grip band at the hand,
    heavy gold end sections with dark bands, and domed caps; its foot on the ground, its head above the halo."""
    m = []
    m += cyl("y", 0, 0, 1.0, -9, 30, RED)
    m += cyl("y", 0, 0, 1.25, -1.5, 1.5, GOLD_DARK)
    for y0, y1 in ((-12, -9), (30, 33)):
        m += cyl("y", 0, 0, 1.5, y0, y1, GOLD)
    for y0, y1 in ((-11.2, -10.4), (-9.6, -9.0), (30.4, 31.0), (32.2, 33.0)):
        m += cyl("y", 0, 0, 1.65, y0, y1, GOLD_DARK)
    m += cyl("y", 0, 0, 1.15, -13, -12, GOLD)
    m += cyl("y", 0, 0, 1.15, 33, 34.2, GOLD)
    m += cyl("y", 0, 0, 0.6, 34.2, 35, GOLD_DARK)
    for y in (6, 14, 22):
        m += cyl("y", 0, 0, 1.08, y, y + 0.7, GOLD_DARK)
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


def posed(pose=None, with_staff=True):
    """Every box in world pixels at the rest pose, or at `pose` ({group: (rx, ry, rz, dx, dy, dz)}, absolute)."""
    pose = pose or {}
    parts = groups(with_staff)
    tree = {g[0]: g for g in parts}
    parents = {g[0]: (g[4] if len(g) > 4 else None) for g in parts}

    def chain(name):
        turns = []
        while name:
            g = tree[name]
            rx, ry, rz, dx, dy, dz = pose.get(name, tuple(g[2] or (0, 0, 0)) + (0, 0, 0))
            for axis, angle in (("z", rz), ("y", ry), ("x", rx)):
                if angle:
                    turns.append((axis, angle, list(g[1])))
            if dx or dy or dz:
                turns.append(("move", (dx, dy, dz)))
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


# ------------------------------------------------------------------ animation curves (ticks): floaty and smooth

def with_rest(pose):
    out = {}
    for g in groups(True):
        base = tuple(g[2] or (0, 0, 0)) + (0, 0, 0)
        delta = pose.get(g[0], (0, 0, 0, 0, 0, 0))
        out[g[0]] = tuple(base[i] + delta[i] for i in range(6))
    return out


def idle_pose(t, period=60):
    """A slow hover: the body rising and settling, the head drifting, the tail curling and uncurling behind, the free
    hand floating, the staff hand still."""
    p = 2 * math.pi * t / period
    return with_rest({"body": (1.5 * math.sin(p), 0, 0, 0, 1.2 * math.sin(p), 0),
                      "head": (2 * math.sin(p + 0.6), 6 * math.sin(p / 2), 1.5 * math.sin(p + 1.2), 0, 0, 0),
                      "tail": (8 * math.sin(p - 0.9), 10 * math.sin(p / 2 - 0.5), 0, 0, 0, 0),
                      "left_arm": (4 * math.sin(p + 0.8), 0, 3 * math.sin(p), 0, 0, 0),
                      "right_arm": (1.5 * math.sin(p + 0.3), 0, 0, 0, 0, 0)}), (1, 1, 1)


def walk_pose(t, period=24):
    """A floating stride: long slow leg swings, a gentle rise and fall, the body leaning in, the free arm drifting,
    the tail trailing a beat behind, the head floating level."""
    p = 2 * math.pi * t / period
    rise = 1.6 * (0.5 - 0.5 * math.cos(2 * p))
    stretch = 0.03 * math.sin(2 * p - 0.6)
    pose = {"body": (4, 0, 2.5 * math.sin(p), 0, rise, 0),
            "head": (-3, 2 * math.sin(p), -2 * math.sin(p), 0, 0, 0),
            "tail": (6 * math.sin(2 * p - 1.2), 14 * math.sin(p - 1.0), 0, 0, 0, 0),
            "left_leg": (28 * math.sin(p), 0, 0, 0, 0, 0),
            "right_leg": (-28 * math.sin(p), 0, 0, 0, 0, 0),
            "left_arm": (-26 * math.sin(p - 0.3), 0, 3 * math.sin(2 * p), 0, 0, 0),
            "right_arm": (6 * math.sin(p - 0.3), 0, 0, 0, 0, 0)}
    return with_rest(pose), (1 - stretch / 2, 1 + stretch, 1 - stretch / 2)


JUMP_TICKS = 40


def jump_pose(t):
    """A slow floaty leap: a soft crouch, a long rise with the legs trailing and the free arm reaching up, a hang at
    the top with the tail curling, a gentle drop, a soft squash and a settle."""
    def smooth(u):
        u = max(0.0, min(1.0, u))
        return u * u * (3 - 2 * u)
    if t < 8:
        c = smooth(t / 8)
        rise, crouch, stretch, legs, arm, tail = 0, 3 * c, -0.05 * c, 12 * c, -10 * c, -10 * c
    elif t < 12:
        u = smooth((t - 8) / 4)
        rise, crouch, stretch, legs, arm, tail = 4 * u, 3 * (1 - u), -0.05 + 0.11 * u, 12 - 30 * u, -10 - 60 * u, -10 + 25 * u
    elif t < 28:
        u = (t - 12) / 16
        rise = 4 + 16 * math.sin(math.pi * u)
        tuck = smooth(u * 2) * (1 - smooth((u - 0.6) / 0.4))
        rise, crouch, stretch, legs, arm, tail = rise, 0, 0.06 * (1 - abs(2 * u - 1)), -18 - 22 * tuck, -70 - 30 * tuck, 15 + 25 * tuck
    elif t < 32:
        u = smooth((t - 28) / 4)
        rise, crouch, stretch, legs, arm, tail = 0, 4 * u, -0.08 * u, -8 + 20 * u, -40 + 25 * u, 20 - 30 * u
    else:
        u = (t - 32) / (JUMP_TICKS - 32)
        w = math.exp(-u * 2.5) * math.cos(u * 5)
        rise, crouch, stretch, legs, arm, tail = 0, 4 * w * (1 - u), -0.08 * w * (1 - u), 12 * w * (1 - u), -15 * w * (1 - u), -10 * w * (1 - u)
    pose = {"body": (crouch * 2, 0, 0, 0, rise - crouch, 0),
            "head": (-crouch * 1.5, 0, 0, 0, 0, 0),
            "tail": (tail, 0, 0, 0, 0, 0),
            "left_leg": (legs, 0, 0, 0, rise - crouch, 0),
            "right_leg": (legs * 0.8, 0, 0, 0, rise - crouch, 0),
            "left_arm": (arm, 0, 10 - arm / 6, 0, 0, 0),
            "right_arm": (arm / 4, 0, 0, 0, 0, 0)}
    return with_rest(pose), (1 - stretch / 2, 1 + stretch, 1 - stretch / 2)


ATTACK_TICKS = 36


def attack_pose(t):
    """A slow, sweeping staff blow: the staff hand lifts the staff up and back in a long arc as the body coils and the
    tail rises, a hang at the top, then the sweep down and across with the body turning into it and a soft dip, and
    a floating recovery."""
    def smooth(u):
        u = max(0.0, min(1.0, u))
        return u * u * (3 - 2 * u)
    if t < 14:
        u = smooth(t / 14)
        arm, twist, lean, tail, free, dip = -22 - 138 * u, 25 * u, -8 * u, 25 * u, -12 + 30 * u, 0
    elif t < 20:
        u = smooth((t - 14) / 6)
        arm, twist, lean, tail, free, dip = -160 + 190 * u, 25 - 50 * u, -8 + 22 * u, 25 - 40 * u, 18 - 46 * u, 2.5 * math.sin(math.pi * u)
    elif t < 26:
        u = (t - 20) / 6
        arm, twist, lean, tail, free, dip = 30 - 4 * u, -25 + 6 * u, 14 - 4 * u, -15 + 5 * u, -28 + 4 * u, 0
    else:
        u = smooth((t - 26) / (ATTACK_TICKS - 26))
        arm, twist, lean, tail, free, dip = 26 - 48 * u, -19 + 19 * u, 10 - 10 * u, -10 + 10 * u, -24 + 12 * u, 0
    pose = {"body": (lean, twist, 0, 0, -dip, 0), "head": (10 - lean / 2, -6 - twist / 2, 0, 0, 0, 0),
            "tail": (tail, -twist / 2, 0, 0, 0, 0), "right_arm": (arm, 0, -22, 0, 0, 0), "staff": (22, 0, 22, 0, 0, 0),
            "left_arm": (free, 0, 10, 0, 0, 0), "left_leg": (-lean / 2, 0, 0, 0, 0, 0), "right_leg": (lean / 2, 0, 0, 0, 0, 0)}
    return pose, (1, 1, 1)


def animations():
    import blockbench_export
    rests = {g[0]: tuple(g[2] or (0, 0, 0)) for g in groups(False)}
    names = list(rests)

    def clip(name, length, frames, poses, loop):
        tracks = {n: {"rotation": [], "position": [], "scale": []} for n in names}
        for i in range(frames + 1):
            time = round(length * i / frames, 4)
            pose, scale = poses(i, frames)
            for n in names:
                rx, ry, rz, dx, dy, dz = pose.get(n, rests[n] + (0, 0, 0))
                rest = rests[n]
                tracks[n]["rotation"].append((time, (rx - rest[0], ry - rest[1], rz - rest[2])))
                tracks[n]["position"].append((time, (dx, dy, dz)))
            tracks["body"]["scale"].append((time, scale))
        tracks = {g: {c: k for c, k in ch.items() if any(v != (0, 0, 0) and v != (1, 1, 1) for _, v in k)}
                  for g, ch in tracks.items()}
        return blockbench_export.animation(name, length, tracks, loop=loop)

    return [clip("idle", 3.0, 30, lambda i, n: idle_pose(60 * i / n), True),
            clip("walk", 1.2, 24, lambda i, n: walk_pose(24 * i / n), True),
            clip("jump", 2.0, 40, lambda i, n: jump_pose(JUMP_TICKS * i / n), False),
            clip("attack", 1.8, 36, lambda i, n: attack_pose(ATTACK_TICKS * i / n), False)]


def write_bbmodel(folder=ART):
    import blockbench_export
    monk = blockbench_export.write(folder / "monkey_monk.bbmodel", "monkey_monk", groups(False),
                                   draw=lambda name: TEXTURES[name](), animations=animations())
    blockbench_export.write(folder / "ruyi_staff.bbmodel", "ruyi_staff", [("ruyi_staff", (0, 0, 0), None, staff())],
                            draw=lambda name: TEXTURES[name]())
    return monk


# ------------------------------------------------------------------ art (flat, clean)

def plain(fill, light, shade):
    img = clean_metal.canvas(fill)
    clean_metal.bevel(img, 0, 0, 15, 15, light, shade, fill)
    return img


def fur(fill, light, shade):
    """Short fur: the fill with staggered strokes a shade lighter and darker."""
    img = clean_metal.canvas(fill)
    for y in range(16):
        for x in range(16):
            if (x * 3 + y * 5) % 11 == 0:
                clean_metal.put(img, x, y, light)
            elif (x * 7 + y * 3) % 13 == 0:
                clean_metal.put(img, x, y, shade)
    return img


def cloth(fill, light, shade):
    img = plain(fill, light, shade)
    for x0, y0, y1 in ((3, 2, 8), (8, 5, 13), (12, 1, 5)):
        clean_metal.rect(img, x0, y0, x0, y1, shade)
    return img


TEXTURES = {FUR: lambda: fur((176, 124, 62), (204, 152, 86), (138, 92, 44)),
            FUR_DARK: lambda: fur((118, 78, 38), (146, 100, 54), (86, 54, 24)),
            CREAM: lambda: fur((232, 212, 176), (246, 232, 204), (200, 176, 136)),
            SAFFRON: lambda: cloth((214, 134, 40), (236, 166, 72), (166, 98, 24)),
            SAFFRON_DARK: lambda: cloth((176, 104, 30), (200, 128, 50), (132, 74, 16)),
            TRIM: lambda: plain((150, 42, 36), (180, 64, 56), (104, 26, 22)),
            GOLD: lambda: plain((222, 180, 70), (246, 214, 120), (166, 122, 38)),
            GOLD_DARK: lambda: plain((166, 122, 38), (196, 150, 58), (118, 84, 26)),
            JADE: lambda: plain((92, 170, 128), (134, 204, 162), (56, 120, 86)),
            RED: lambda: plain((164, 44, 40), (196, 70, 62), (118, 28, 26)),
            BLACK: lambda: plain((24, 20, 20), (44, 38, 38), (12, 10, 10)),
            BEAD: lambda: plain((98, 56, 40), (128, 80, 60), (64, 36, 24)),
            WRAP: lambda: cloth((226, 220, 206), (242, 238, 226), (186, 178, 162)),
            IRON: lambda: plain((110, 112, 118), (140, 142, 148), (76, 78, 84))}


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
            box_preview.render(posed(), scale=6, yaw=yaw, pitch=12, draw=draw).save(out.replace(".png", f"_{suffix}.png"))
        from PIL import Image
        frames = []
        for poses in ([walk_pose(t)[0] for t in (0, 6, 12, 18)], [jump_pose(t)[0] for t in (6, 14, 20, 30)],
                      [attack_pose(t)[0] for t in (8, 14, 19, 30)]):
            for pose in poses:
                frames.append(box_preview.render(posed(pose), scale=4, yaw=-30, pitch=12, draw=draw))
        w = max(f.width for f in frames)
        h = max(f.height for f in frames)
        sheet = Image.new("RGB", (4 * (w + 8), 3 * (h + 8)), (236, 238, 242))
        for i, f in enumerate(frames):
            sheet.paste(f, ((i % 4) * (w + 8) + (w - f.width) // 2, (i // 4) * (h + 8) + h - f.height))
        sheet.save(out.replace(".png", "_frames.png"))
        print(out)

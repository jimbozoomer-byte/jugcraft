"""The Steampunk Monk: a tall, lean wandering monk in the manner of a certain cartoon samurai, made of brass and
cloth, 10 October 2026. A still model, no clips (by the owner's choice: "do not animate").

- Head: a shaved head with a stern face (brow ridge, narrow eyes, a straight mouth), ears, brass goggles pushed up on
  the brow with teal lenses and a leather strap; a conical woven straw kasa in stepped tiers with a brass rim, brass
  ribs and a brass finial, tilted a little forward.
- Body: a cream robe with dark lapels crossing at the chest, a prayer-bead necklace with a brass gear pendant, two
  leather straps over the chest with brass rivets, a black obi with a brass gauge for a buckle and a wrapped knot at
  the back, and a pleated dark hakama falling to the shins.
- Back: a small copper boiler on a brass frame with a chimney, a valve wheel, a gauge and two pipes running to the
  left arm.
- Arms: wide cream sleeves; the right hand bare, holding the staff; the left forearm a brass machine of plates,
  two pistons, a wrist ring and four articulated fingers, with the pipes from the boiler.
- Legs: hakama pleats over wrapped dark gaiters with brass greaves, and geta sandals of wood with two teeth and a
  strap.
- Staff: a long dark wooden shakujo with brass ferrules, a pressure gauge and a valve, and a brass head ring hung
  with six small rings.

Units are pixels, the monk facing +z with the ground at y = 0. The project (art/steampunk_monk/steampunk_monk.bbmodel)
has every box in groups at their joints, posed at rest (arms a little forward, the staff planted).

    python tools/steampunk_monk.py --bbmodel --preview out.png
"""
import math
from pathlib import Path

import clean_metal
from steampunk_models import box, cyl

ROOT = Path(__file__).resolve().parents[1]
ART = ROOT / "art" / "steampunk_monk"

CREAM, CREAM_DARK, BLACK, BRASS, BRASS_DARK, COPPER, LEATHER, SKIN, STRAW, STRAW_DARK, WOOD, GLASS, BEAD, GAUGE = (
    "sk_cream", "sk_cream_dark", "sk_black", "sk_brass", "sk_brass_dark", "sk_copper", "sk_leather", "sk_skin",
    "sk_straw", "sk_straw_dark", "sk_wood", "sk_glass", "sk_bead", "sp_gauge")

# Joints, in pixels: a tall figure, legs 16, body 14, head 8 and the hat above.
HIP = (0, 16, 0)
NECK = (0, 31, 0)
SHOULDERS = {"left_arm": (5.5, 29.5, 0), "right_arm": (-5.5, 29.5, 0)}
LEGS = {"left_leg": (2, 16, 0), "right_leg": (-2, 16, 0)}
HAND = (0, -14.5, 0)
REST = {"left_arm": (-18, 0, 8), "right_arm": (-25, 0, -35), "staff": (25, 0, 35), "head": (4, -8, 0)}


# ------------------------------------------------------------------ parts (each from its joint)

def head():
    """The shaved head and stern face, ears, the goggles on the brow and the kasa."""
    m = []
    m += [box((-4, 0, -4), (4, 8, 4), SKIN)]
    m.append(box((-3, 7.6, -3.2), (3, 8.3, 3.4), SKIN))
    # The face: a heavy brow, narrow dark eyes under it, a nose, a straight mouth, and shadow under the cheekbones.
    m.append(box((-3.4, 4.6, 4), (3.4, 5.5, 4.4), SKIN))
    for x0, x1 in ((-3, -1.2), (1.2, 3)):
        m.append(box((x0, 4, 4), (x1, 4.6, 4.3), BLACK))
    m.append(box((-0.7, 2.2, 4), (0.7, 4.6, 4.8), SKIN))
    m.append(box((-1.6, 1.3, 4), (1.6, 1.7, 4.25), BLACK))
    for x0, x1 in ((-3.6, -2.4), (2.4, 3.6)):
        m.append(box((x0, 2, 3.8), (x1, 3.6, 4.1), CREAM_DARK))
    for x in (-4.6, 4):
        m.append(box((x, 2.5, -0.5), (x + 0.6, 5.5, 1.5), SKIN))
    # The goggles pushed up on the brow: a leather strap round the head and two brass cups with teal lenses.
    m.append(box((-4.3, 6, -4.3), (4.3, 7.2, 4.3), LEATHER))
    for x in (-2.2, 2.2):
        m += cyl("z", x, 6.6, 1.6, 4.3, 5.6, BRASS, GLASS)
        m += cyl("z", x, 6.6, 1.1, 5.6, 5.8, GLASS)
    m.append(box((-0.6, 6.1, 4.4), (0.6, 7.1, 5.2), BRASS_DARK))
    return m


HAT = (0, 9.4, 0)   # the crown of the head, from NECK: the kasa hangs from here


def hat():
    """The kasa: woven straw tiers rising to a point, a brass rim and a finial, tilted a little forward. From HAT,
    its own joint, so it can bob behind the head."""
    m = []
    tilt = ("x", 4, [0, 0, 0])
    # A shallow cone: ten straw tiers each less than a pixel tall, from the brim to a point.
    tiers = []
    y = 0.2
    for i in range(10):
        r = 11.5 - 1.1 * i
        h = 0.7 + 0.08 * i
        tiers.append((r, y, y + h))
        y += h
    for i, (r, y0, y1) in enumerate(tiers):
        m += _turned(cyl("y", 0, 0, r, y0, y1, STRAW if i % 2 == 0 else STRAW_DARK), tilt)
    m += _turned(cyl("y", 0, 0, 12, -0.2, 0.5, BRASS), tilt)
    m += _turned(cyl("y", 0, 0, 11.2, 0.5, 0.8, BRASS_DARK), tilt)
    top = y
    m += _turned(cyl("y", 0, 0, 0.9, top, top + 1.4, BRASS), tilt)
    m += _turned(cyl("y", 0, 0, 1.3, top + 1.4, top + 2.4, BRASS), tilt)
    m += _turned(cyl("y", 0, 0, 0.45, top + 2.4, top + 4, BRASS_DARK), tilt)
    return m


def _turned(elements, rotation):
    out = []
    for item in elements:
        frm, to, texture = item[:3]
        options = dict(item[3]) if len(item) > 3 else {}
        options["rotation"] = rotation
        out.append((frm, to, texture, options))
    return out


def body():
    """The robe's torso with crossed lapels, the chest straps and rivets, the prayer beads, the obi with its gauge
    buckle and back knot, and the boiler pack on its brass frame."""
    m = []
    m.append(box((-4, -14, -2.2), (4, 0, 2.2), CREAM))
    m.append(box((-4.4, -8, -2.6), (4.4, -1, 2.6), CREAM))
    # The lapels: two dark bands crossing at the chest, the right over the left.
    m.append(box((-3.2, -9, 2.2), (0.3, -1, 2.5), CREAM_DARK, ("z", -22.5, [0, -1, 2.3])))
    m.append(box((-0.3, -9, 2.3), (3.2, -1, 2.6), CREAM_DARK, ("z", 22.5, [0, -1, 2.3])))
    m.append(box((-4.3, -1.5, -2.5), (4.3, 0.6, 2.5), CREAM_DARK))
    # Two leather straps over the shoulders to the obi, riveted in brass.
    for x in (-2.6, 1.8):
        m.append(box((x, -11, 2.5), (x + 0.8, -1.5, 2.9), LEATHER))
        m.append(box((x, -11, -2.9), (x + 0.8, -1.5, -2.5), LEATHER))
        for y in (-9.5, -6.5, -3.5):
            m.append(box((x + 0.2, y, 2.9), (x + 0.6, y + 0.4, 3.15), BRASS))
    # Prayer beads: a loop of dark beads round the neck with a brass gear pendant.
    for i in range(9):
        a = math.pi * (i + 0.5) / 9
        x, y = 4.2 * math.cos(a), -1 - 4 * math.sin(a)
        m.append(box((x - 0.55, y - 0.55, 2.7), (x + 0.55, y + 0.55, 3.8), BEAD))
    m += cyl("z", 0, -6.4, 1.4, 3.3, 4.0, BRASS)
    m += cyl("z", 0, -6.4, 0.5, 4.0, 4.3, BRASS_DARK)
    # The obi: a wide black band, a brass gauge for a buckle, and the wrapped knot behind.
    m.append(box((-4.6, -13.5, -2.8), (4.6, -9.5, 2.8), BLACK))
    m.append(box((-4.8, -11.8, -3.0), (4.8, -11.2, 3.0), BRASS_DARK))
    m.append(box((-1.9, -13, 2.8), (1.9, -10, 3.4), BRASS))
    m.append(box((-1.4, -12.5, 3.4), (1.4, -10.5, 3.6), {"*": BRASS, "south": f"{GAUGE}!"}))
    m.append(box((-2.4, -13.2, -3.8), (2.4, -9.8, -2.8), BLACK))
    m.append(box((-1.2, -13.8, -4.4), (1.2, -9.2, -3.8), BLACK))
    m.append(box((-3, -16, -4), (-1.6, -13.2, -3.2), BLACK, ("z", 22.5, [-2.3, -13.2, -3.6])))
    m.append(box((1.6, -16, -4), (3, -13.2, -3.2), BLACK, ("z", -22.5, [2.3, -13.2, -3.6])))
    # The boiler pack: a copper drum on a brass frame on the back, a chimney, a valve wheel, a gauge and pipes.
    m.append(box((-3.5, -9, -4.6), (3.5, -2, -2.2), BRASS_DARK))
    m += cyl("y", 0, -5.2, 2.6, -8.5, -1.5, COPPER)
    m += cyl("y", 0, -5.2, 2.9, -8.8, -8, BRASS)
    m += cyl("y", 0, -5.2, 2.9, -2, -1.2, BRASS)
    m += cyl("y", 1.4, -5.6, 0.8, -1.2, 3.5, BRASS_DARK)
    m += cyl("y", 1.4, -5.6, 1.1, 3.5, 4.3, BRASS)
    m += cyl("z", -1.6, -3, 1.2, -8.6, -8.0, BRASS, BRASS_DARK)
    m.append(box((-2.6, -3.6, -9.1), (-0.6, -2.4, -8.6), BRASS))
    m.append(box((-1.9, -4, -8.6), (-1.3, -2, -8.1), BRASS_DARK))
    m.append(box((-1.2, -6.9, -8.4), (1.2, -4.5, -8.0), {"*": BRASS, "north": f"{GAUGE}!"}))
    for z0 in (-7.3, -3.9):
        m += cyl("x", -6.6, z0 - 0.3, 0.55, 2.5, 6.5, COPPER)
    m += cyl("z", 2.5, -6.6, 0.55, -7.6, -3.6, COPPER)
    # Drawn from the obi upward: the torso stands on the hip joint.
    return [shifted(i, (0, 14, 0)) for i in m]


def arm(side):
    """A wide cream sleeve from the shoulder. The right (-x) ends in a bare hand; the left in the brass forearm: plates,
    two pistons, a wrist ring, four fingers and a thumb, with the pipes' couplings."""
    m = []
    m.append(box((-2.2, -9, -2.2), (2.2, 1, 2.2), CREAM))
    m.append(box((-2.8, -8, -2.8), (2.8, -2, 2.8), CREAM))
    m.append(box((-3.0, -9.2, -3.0), (3.0, -8, 3.0), CREAM_DARK))
    if side > 0:
        m.append(box((-1.9, -14.5, -1.9), (1.9, -9.2, 1.9), BRASS))
        m.append(box((-2.2, -13, -2.2), (2.2, -12, 2.2), BRASS_DARK))
        m.append(box((2, -14, -1), (2.6, -9.5, 1), BRASS_DARK))
        for x in (-1.2, 0.8):
            m += cyl("z", x, -11.5, 0.5, 1.9, 3.1, COPPER, BRASS_DARK)
            m += cyl("z", x, -11.5, 0.7, 2.6, 3.4, BRASS)
        m += cyl("y", 0, 0, 2.2, -15.3, -14.5, BRASS_DARK)
        m.append(box((-1.7, -18, -1.7), (1.7, -15.3, 1.7), BRASS))
        for i, x in enumerate((-1.4, -0.5, 0.4, 1.3)):
            m.append(box((x - 0.4, -19.8, -1.3), (x + 0.4, -18, 0.2), BRASS_DARK))
            m.append(box((x - 0.4, -21.2 + 0.3 * abs(i - 1.5), -1.3), (x + 0.4, -19.8, -0.1), BRASS))
        m.append(box((-2.6, -17.6, -0.6), (-1.7, -15.6, 0.6), BRASS_DARK))
        m.append(box((-3.3, -18.2, -0.5), (-2.6, -16.8, 0.5), BRASS))
        for z in (-0.9, 0.9):
            m += cyl("y", 0.2, z, 0.5, -9.2, -4, COPPER)
    else:
        m.append(box((-1.8, -13.5, -1.8), (1.8, -9.2, 1.8), SKIN))
        for x in (-1.3, -0.4, 0.5, 1.4):
            m.append(box((x - 0.4, -15.3, -1.5), (x + 0.4, -13.5, -0.2), SKIN))
        m.append(box((1.8, -13, -0.5), (2.6, -11, 0.5), SKIN))
    return m


def staff():
    """The shakujo from the right fist: a dark wooden shaft with brass ferrules, a pressure gauge and a valve wheel
    near the hand, a brass head ring on a collar hung with six small rings, and an iron shoe."""
    m = []
    m += cyl("y", 0, 0, 0.8, -15, 28, WOOD)
    for y0, y1 in ((-15.5, -13.5), (-4, -2), (4, 6), (26, 28.5)):
        m += cyl("y", 0, 0, 1.05, y0, y1, BRASS)
    m += cyl("y", 0, 0, 1.2, -16.5, -15.5, BRASS_DARK)
    m.append(box((-1.4, -1.5, 0.8), (1.4, 1.5, 1.6), {"*": BRASS, "south": f"{GAUGE}!"}))
    m += cyl("z", 0, 3, 0.4, -2.4, -0.8, BRASS_DARK)
    m += cyl("z", 0, 3, 1.3, -3.2, -2.4, COPPER)
    m += cyl("y", 0, 0, 1.6, 28.5, 30, BRASS_DARK)
    # The head: a brass hoop (two bars and their turned copies) round a dark boss, a spike, and six small rings hung
    # from the hoop's lower edge that would chime as he walks.
    for angle in (0, 45):
        m.append(box((-4.6, 33.4, -0.5), (4.6, 34.6, 0.5), BRASS, ("z", angle, [0, 34, 0])))
        m.append(box((-0.55, 29.4, -0.5), (0.55, 38.6, 0.5), BRASS, ("z", angle, [0, 34, 0])))
    m += cyl("z", 0, 34, 1.3, -0.6, 0.6, BRASS_DARK)
    m += cyl("y", 0, 0, 0.5, 38.6, 40.6, BRASS_DARK)
    for x in (-3.6, -2.2, 2.2, 3.6):
        y = 31.4 if abs(x) > 3 else 30.2
        m.append(box((x - 0.8, y - 2.6, -0.3), (x + 0.8, y, 0.3), BRASS_DARK))
        m.append(box((x - 0.4, y - 2.2, -0.35), (x + 0.4, y - 0.4, 0.35), BRASS))
    for x in (-1.0, 1.0):
        m.append(box((x - 0.7, 27.4, -0.3), (x + 0.7, 29.4, 0.3), BRASS_DARK))
        m.append(box((x - 0.35, 27.8, -0.35), (x + 0.35, 29.0, 0.35), BRASS))
    return m


def leg():
    """A hakama leg of pleats over a wrapped dark gaiter with a brass greave, and a geta sandal."""
    m = []
    m.append(box((-2.4, -8, -2.4), (2.4, 0.5, 2.4), BLACK))
    for z0, z1 in ((2.4, 2.9), (-2.9, -2.4)):
        for x in (-1.6, 0, 1.6):
            m.append(box((x - 0.5, -8.2, z0), (x + 0.5, -0.5, z1), CREAM_DARK))
    m.append(box((-2.9, -8, -2.2), (-2.4, -0.5, 2.2), CREAM_DARK))
    m.append(box((2.4, -8, -2.2), (2.9, -0.5, 2.2), CREAM_DARK))
    m.append(box((-2, -14, -2), (2, -8, 2), BLACK))
    for y in (-13, -11, -9):
        m.append(box((-2.15, y, -2.15), (2.15, y + 0.6, 2.15), LEATHER))
    m.append(box((-1.6, -14, 2), (1.6, -8.5, 2.7), BRASS))
    m.append(box((-1.2, -12.5, 2.7), (1.2, -10, 2.9), BRASS_DARK))
    m.append(box((-2.4, -15, -2.6), (2.4, -14, 3.2), WOOD))
    for z0, z1 in ((-2.2, -1.2), (1.6, 2.6)):
        m.append(box((-2.1, -16, z0), (2.1, -15, z1), WOOD))
    m.append(box((-0.4, -14.6, -0.5), (0.4, -13.2, 0.5), BLACK))
    m.append(box((-2.2, -13.6, -0.3), (2.2, -13.1, 0.3), BLACK))
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
    """The monk's groups at their joints with their rest rotations; the staff only when asked (it is its own
    project, art/steampunk_monk/shakujo.bbmodel, so it can be an item in the hand later)."""
    crown = add(NECK, HAT)
    out = [("body", HIP, None, [shifted(i, HIP) for i in body()]),
           ("head", NECK, REST.get("head"), [shifted(i, NECK) for i in head()], "body"),
           ("hat", crown, None, [shifted(i, crown) for i in hat()], "head")]
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
    """Every box in world pixels, at the rest pose or at `pose` ({group: (rx, ry, rz, dx, dy, dz)}, absolute), for
    a preview."""
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


# ------------------------------------------------------------------ animation curves (ticks)

def with_rest(pose):
    """A pose (changes per group) laid over the rest rotations: absolute angles."""
    out = {}
    for g in groups(True):
        base = tuple(g[2] or (0, 0, 0)) + (0, 0, 0)
        delta = pose.get(g[0], (0, 0, 0, 0, 0, 0))
        out[g[0]] = tuple(base[i] + delta[i] for i in range(6))
    return out


def idle_pose(t, period=48):
    """Breathing, a slow look about, the hat settling a beat behind the head, the free hand's fingers at rest."""
    p = 2 * math.pi * t / period
    return with_rest({"body": (0, 0, 0, 0, 0.6 * math.sin(p), 0),
                      "head": (1.5 * math.sin(p + 0.4), 5 * math.sin(p / 2), 0, 0, 0, 0),
                      "hat": (-2 * math.sin(p - 0.5), 0, 1.5 * math.sin(p / 2 - 0.6), 0, 0.4 * math.sin(2 * p - 0.9), 0),
                      "left_arm": (2 * math.sin(p + 0.8), 0, 1.5 * math.sin(p), 0, 0, 0),
                      "right_arm": (2 * math.sin(p + 0.2), 0, 0, 0, 0, 0)})


def walk_pose(t, period=16):
    """A cartoony bouncy stride: big leg swings with a hop at each step, the body squashing as it lands and
    stretching as it rises, swaying and nodding, the free arm swinging, the staff hand steadier, and the kasa
    bobbing and tilting a beat behind the head."""
    p = 2 * math.pi * t / period
    hop = max(0.0, math.sin(2 * p))
    stretch = 0.07 * math.sin(2 * p)
    pose = {"body": (3 * math.sin(2 * p + 0.3), 0, 3.5 * math.sin(p), 0, 2.6 * hop, 0),
            "head": (-4 * math.sin(2 * p), 3 * math.sin(p), -2.5 * math.sin(p), 0, 0, 0),
            "hat": (-7 * math.sin(2 * p - 0.9), 0, 3 * math.sin(p - 0.7), 0, 1.4 * math.sin(2 * p - 1.2), 0),
            "left_leg": (32 * math.sin(p), 0, 0, 0, 0, 0),
            "right_leg": (-32 * math.sin(p), 0, 0, 0, 0, 0),
            "left_arm": (-30 * math.sin(p), 0, 4 * math.sin(2 * p), 0, 0, 0),
            "right_arm": (8 * math.sin(p), 0, 0, 0, 0, 0)}
    return with_rest(pose), (1 - stretch / 2, 1 + stretch, 1 - stretch / 2)


ATTACK_TICKS = 24


def attack_pose(t):
    """The staff strike: the staff hand hauled up and back with the body coiling and the hat tipping back, then
    snapped down and forward in a stamping blow, a squash on the hit, and a springy recovery."""
    def smooth(u):
        u = max(0.0, min(1.0, u))
        return u * u * (3 - 2 * u)
    if t < 7:
        u = smooth(t / 7)
        arm, twist, lean, hat_tip, squash, free = -25 - 135 * u, 22 * u, -8 * u, 10 * u, 0.04 * u, -18 + 40 * u
    elif t < 10:
        u = smooth((t - 7) / 3)
        arm, twist, lean, hat_tip, squash, free = -160 + 175 * u, 22 - 44 * u, -8 + 24 * u, 10 - 24 * u, 0.04 - 0.16 * u, 22 - 50 * u
    elif t < 15:
        u = (t - 10) / 5
        arm, twist, lean, hat_tip, squash, free = 15 + 3 * math.sin(u * 7) * (1 - u), -22 + 6 * u, 16 - 4 * u, -14 + 6 * u, -0.12 * (1 - u), -28 + 4 * u
    else:
        u = smooth((t - 15) / (ATTACK_TICKS - 15))
        w = math.exp(-u * 3) * math.cos(u * 7)
        arm, twist, lean, hat_tip, squash, free = 15 - 40 * u, -16 + 16 * u, 12 - 12 * u, -8 * (1 - u) * w, 0.05 * w * (1 - u), -24 + 6 * u
    pose = {"body": (lean, twist, 0, 0, 0, 0), "head": (4 - lean / 2, -8 - twist / 2, 0, 0, 0, 0),
            "hat": (hat_tip, 0, 0, 0, 0, 0), "right_arm": (arm, 0, -35, 0, 0, 0), "staff": (25, 0, 35, 0, 0, 0),
            "left_arm": (free, 0, 8, 0, 0, 0), "left_leg": (-lean / 2, 0, 0, 0, 0, 0), "right_leg": (lean / 2, 0, 0, 0, 0, 0)}
    return pose, (1 - squash / 2, 1 + squash, 1 - squash / 2)


def animations():
    import blockbench_export
    names = [g[0] for g in groups(False)]

    def clip(name, length, frames, poses, loop):
        tracks = {n: {"rotation": [], "position": [], "scale": []} for n in names}
        for i in range(frames + 1):
            time = round(length * i / frames, 4)
            pose, scale = poses(i, frames)
            for n in names:
                rx, ry, rz, dx, dy, dz = pose.get(n, (0, 0, 0, 0, 0, 0))
                rest = tuple(dict((g[0], g[2] or (0, 0, 0)) for g in groups(False)).get(n, (0, 0, 0)))
                tracks[n]["rotation"].append((time, (rx - rest[0], ry - rest[1], rz - rest[2])))
                tracks[n]["position"].append((time, (dx, dy, dz)))
            tracks["body"]["scale"].append((time, scale))
        tracks = {g: {c: k for c, k in ch.items() if any(v != (0, 0, 0) and v != (1, 1, 1) for _, v in k)}
                  for g, ch in tracks.items()}
        return blockbench_export.animation(name, length, tracks, loop=loop)

    return [clip("idle", 2.4, 24, lambda i, n: (idle_pose(48 * i / n), (1, 1, 1)), True),
            clip("walk", 0.8, 16, lambda i, n: walk_pose(16 * i / n), True),
            clip("attack", 1.2, 24, lambda i, n: attack_pose(ATTACK_TICKS * i / n), False)]


def write_bbmodel(folder=ART):
    """The monk (no staff) with his clips, and the shakujo as its own project with its grip at the origin."""
    import blockbench_export
    monk = blockbench_export.write(folder / "steampunk_monk.bbmodel", "steampunk_monk", groups(False),
                                   draw=lambda name: TEXTURES[name](), animations=animations())
    blockbench_export.write(folder / "shakujo.bbmodel", "shakujo", [("shakujo", (0, 0, 0), None, staff())],
                            draw=lambda name: TEXTURES[name]())
    return monk


# ------------------------------------------------------------------ art (flat, clean)

def plain(fill, light, shade):
    img = clean_metal.canvas(fill)
    clean_metal.bevel(img, 0, 0, 15, 15, light, shade, fill)
    return img


def cloth(fill, light, shade):
    img = plain(fill, light, shade)
    for x0, y0, y1 in ((3, 2, 8), (8, 5, 13), (12, 1, 5)):
        clean_metal.rect(img, x0, y0, x0, y1, shade)
    return img


def straw(fill, light, shade):
    """Woven straw: diagonal weave lines."""
    img = clean_metal.canvas(fill)
    for y in range(16):
        for x in range(16):
            if (x + y) % 4 == 0:
                clean_metal.put(img, x, y, shade)
            elif (x + y) % 4 == 2:
                clean_metal.put(img, x, y, light)
    return img


def brass():
    img = plain((196, 150, 62), (228, 190, 104), (140, 100, 36))
    for x, y in ((3, 3), (12, 11)):
        clean_metal.put(img, x, y, (236, 204, 128))
    return img


def wood():
    img = plain((82, 56, 36), (104, 74, 50), (56, 36, 22))
    for y in (4, 9, 13):
        clean_metal.rect(img, 1, y, 14, y, (64, 42, 26))
    return img


TEXTURES = {CREAM: lambda: cloth((226, 216, 196), (242, 234, 218), (184, 172, 150)),
            CREAM_DARK: lambda: cloth((150, 138, 118), (176, 164, 142), (112, 102, 86)),
            BLACK: lambda: cloth((38, 36, 40), (58, 56, 60), (22, 20, 24)),
            BRASS: brass, BRASS_DARK: lambda: plain((140, 100, 36), (172, 128, 52), (96, 66, 22)),
            COPPER: lambda: plain((176, 104, 66), (206, 134, 92), (124, 70, 42)),
            LEATHER: lambda: plain((98, 66, 42), (124, 88, 58), (66, 42, 26)),
            SKIN: lambda: plain((226, 186, 150), (240, 204, 172), (192, 148, 112)),
            STRAW: lambda: straw((206, 170, 96), (226, 194, 124), (166, 130, 64)),
            STRAW_DARK: lambda: straw((186, 150, 80), (206, 172, 102), (146, 112, 54)),
            WOOD: wood, GLASS: lambda: plain((120, 196, 196), (176, 230, 230), (70, 140, 144)),
            BEAD: lambda: plain((66, 44, 40), (92, 66, 60), (42, 26, 24))}


def draw_all(save):
    for name, draw in TEXTURES.items():
        if name != GAUGE:
            save(draw(), "block", name)


if __name__ == "__main__":
    import sys
    print(sum(len(g[3]) for g in groups()), "boxes in", len(groups()), "groups")
    if "--bbmodel" in sys.argv:
        print(write_bbmodel())
    if "--preview" in sys.argv:
        import box_preview
        draw = lambda n: TEXTURES[n]() if n in TEXTURES else None
        out = sys.argv[-1]
        for yaw, suffix in ((30, "front_left"), (-30, "front_right"), (90, "side"), (-150, "back")):
            box_preview.render(posed(), scale=5, yaw=yaw, pitch=12, draw=draw).save(out.replace(".png", f"_{suffix}.png"))
        from PIL import Image
        frames = []
        for poses in ([walk_pose(t)[0] for t in (0, 4, 8, 12)], [attack_pose(t)[0] for t in (5, 9, 12, 20)]):
            for pose in poses:
                frames.append(box_preview.render(posed(pose), scale=4, yaw=-30, pitch=12, draw=draw))
        w = max(f.width for f in frames)
        h = max(f.height for f in frames)
        sheet = Image.new("RGB", (4 * (w + 8), 2 * (h + 8)), (236, 238, 242))
        for i, f in enumerate(frames):
            sheet.paste(f, ((i % 4) * (w + 8) + (w - f.width) // 2, (i // 4) * (h + 8) + h - f.height))
        sheet.save(out.replace(".png", "_frames.png"))
        print(out)

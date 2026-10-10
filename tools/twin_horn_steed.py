"""The Twin-Horned Steed model, after the owner's picture of 10 October 2026: a dark boss steed, a grey horse with
two splayed brown horns, a white blaze and withers patch, a mane of long straight slab strips hanging down one side,
a stiff branching tail, white bandages wrapped round every lower leg, glowing white eyes and pale green rune glyphs
floating about its head. A model only, by the owner's choice: no entity, renderer or data.

Units are pixels, the steed facing +z with the ground at y = 0. The project (art/twin_horn_steed/twin_horn_steed.bbmodel)
has every box in groups at their joints: body > neck > head (horns, ears, eyes, forelock, runes), body > tail, and four
legs of an upper and a lower part; with clips sampled from the curves below: "idle" (breathing, a nod, the tail and
mane swaying, the runes drifting), "walk" (a trot: diagonal legs together, a bob and a nod), "gallop" (fronts then
hinds, the body rocking) and "charge" (it rears, horns forward, runes flaring, and slams down).

    python tools/twin_horn_steed.py --bbmodel --preview out.png
"""
import math
from pathlib import Path

import clean_metal
from steampunk_models import box, cyl

ROOT = Path(__file__).resolve().parents[1]
ART = ROOT / "art" / "twin_horn_steed"

HIDE, SHADE, MANE, BLAZE, WRAP, HORN, EYE, RUNE, HOOF = ("ds_hide", "ds_hide_dark", "ds_mane", "ds_blaze", "ds_wrap",
                                                          "ds_horn", "ds_eye", "ds_rune", "ds_hoof")

# Joints, in pixels.
BODY = (0, 36, -4)
NECK = (0, 42, 12)
NECK_ANGLE = 45          # the neck leans forward this much from upright
NECK_LENGTH = 24
HEAD_ANGLE = 30          # the head hangs this much below horizontal
TAIL = (0, 43, -26)
SHOULDERS = {"fl": (6, 30, 10), "fr": (-6, 30, 10)}
HIPS = {"hl": (6, 32, -16), "hr": (-6, 32, -16)}
UPPER_FRONT, UPPER_HIND = 13, 16
LOWER = 13


def turned(point, angle):
    rad = math.radians(angle)
    x, y, z = point
    return [x, y * math.cos(rad) - z * math.sin(rad), y * math.sin(rad) + z * math.cos(rad)]


HEAD = turned((0, NECK_LENGTH, 0), NECK_ANGLE)   # the throat latch, from NECK


def rotated(elements, rotation):
    """The same boxes, each turned by one rotation (axis, angle, origin): a round part at an angle."""
    out = []
    for item in elements:
        frm, to, texture = item[:3]
        options = dict(item[3]) if len(item) > 3 else {}
        options["rotation"] = rotation
        out.append((frm, to, texture, options))
    return out


# ------------------------------------------------------------------ parts (each from its joint)

def body():
    """A round barrel of stepped boxes, a deeper chest and breast, a rounded rump under a sloping croup, the withers
    rising behind the neck, a lower rounder belly, shoulder and haunch muscle plates, and the white withers patch."""
    m = []
    m += cyl("z", 0, 0, 8.5, -14, 12, HIDE)
    m += cyl("z", 0, 1, 9, 12, 20, HIDE)
    m += cyl("z", 0, 0.5, 7.2, 20, 23.5, HIDE)
    m += cyl("z", 0, -1, 5, 23.5, 25.5, SHADE)
    # The rump: the barrel carried back, then rounds that shrink and drop toward the dock.
    m += cyl("z", 0, 0.5, 8, -20, -14, HIDE)
    m += cyl("z", 0, 0, 7, -23, -20, HIDE)
    m += cyl("z", 0, -1, 5.5, -25, -23, HIDE)
    m += cyl("z", 0, -2, 3.5, -26.5, -25, SHADE)
    m += cyl("z", 0, -3.2, 7, -10, 8, SHADE)
    m.append(box((-4.5, 8.5, 5), (4.5, 12, 13), HIDE, ("x", -22.5, [0, 12, 13])))
    # Round shoulder and haunch muscles standing proud of the barrel on each side.
    for x0, x1 in ((-9.6, -8.4), (8.4, 9.6)):
        m += cyl("x", 1, 13, 5.5, x0, x1, HIDE)
        m += cyl("x", 1.5, -16, 6, x0, x1, HIDE)
    m.append(box((-3.5, 11.9, 6), (3.5, 12.3, 12), BLAZE, ("x", -22.5, [0, 12, 13])))
    return m


def neck():
    """A round neck, thicker at the chest, tapering to the throat, with a crest for the mane to hang from and a
    muscle line down its side, every box leaned forward together."""
    m = []
    m += cyl("y", 0, 0, 5, -4, 11, HIDE)
    m += cyl("y", 0, 0.5, 4.2, 11, 20, HIDE)
    m += cyl("y", 0, 1, 3.4, 20, NECK_LENGTH + 1, HIDE)
    m.append(box((-2.5, 2, -6.5), (2.5, NECK_LENGTH, -4), MANE))
    m.append(box((-5.5, 4, 0), (-4.9, 18, 1.5), SHADE))
    m.append(box((4.9, 4, 0), (5.5, 18, 1.5), SHADE))
    return rotated(m, ("x", NECK_ANGLE, [0, 0, 0]))


def mane():
    """Long straight slab strips hanging plumb from the leaning crest, most down the left side, a few down the
    right, some with a hooked foot."""
    m = []
    strips = [(5, 1, 32), (4.6, 4, 28), (5.2, 7, 36), (4.8, 10, 24), (5, 13, 32), (4.6, 16, 20), (5.2, 19, 28), (4.8, 22, 16),
              (-4.8, 3, 14), (-5, 9, 18), (-4.6, 15, 12), (-5, 20, 10)]
    for i, (x, a, length) in enumerate(strips):
        top = turned((x, a, -5), NECK_ANGLE)
        w = 2.5 if i % 3 else 3.2
        m.append(box((top[0] - w / 2, top[1] - length, top[2] - 0.6), (top[0] + w / 2, top[1] + 1, top[2] + 0.6), MANE))
        if i % 2 == 0:
            hook = 3 if x > 0 else -3
            lo, hi = sorted((top[0], top[0] + hook))
            m.append(box((lo - w / 2, top[1] - length - 1.2, top[2] - 0.6), (hi + w / 2, top[1] - length, top[2] + 0.6), MANE))
    return m


def head():
    """A long tapered head from the throat latch: skull, forehead, cheeks, jaw, muzzle and nose with nostrils, the
    white blaze, glowing eyes, the face hung down together, and the ears and two long horns standing up in a wide V."""
    m = []
    m += cyl("z", 0, 1, 3.8, -4, 8, HIDE)
    m.append(box((-3, 2.5, 1), (3, 5.6, 9), HIDE))
    for x in (-4.2, 3.4):
        m.append(box((x, -2.5, -1), (x + 0.8, 3, 6), HIDE))
    m.append(box((-2.2, -5.5, 1), (2.2, -2, 11), HIDE))
    m += cyl("z", 0, 0.5, 2.8, 8, 16, HIDE)
    m += cyl("z", 0, 0.2, 2.4, 16, 18.5, SHADE)
    for x in (-1.9, 0.9):
        m.append(box((x, -0.5, 18.5), (x + 1, 0.8, 18.9), MANE))
    m.append(box((-1.6, 5.6, 2), (1.6, 6, 9), BLAZE))
    m.append(box((-1.2, 3.5, 9), (1.2, 3.9, 16), BLAZE))
    for x in (-4.4, 4.0):
        m.append(box((x, 1.5, 3.5), (x + 0.4, 3.5, 6.5), EYE))
    m = rotated(m, ("x", HEAD_ANGLE, [0, 0, 0]))
    # The ears and horns stand up from the poll, the horns in a wide V.
    for x, lean in ((-3.2, -20), (1.6, 20)):
        m.append(box((x, 3, -3), (x + 1.6, 9, -1), HIDE, ("z", lean, [x + 0.8, 3, -2])))
    for x, splay in ((-2, -28), (2, 28)):
        m.append(box((x - 1.2, 3, -1), (x + 1.2, 19, 1.4), HORN, ("z", splay, [x, 3, 0.2])))
        m.append(box((x - 0.7, 19, -0.5), (x + 0.7, 31, 0.9), HORN, ("z", splay, [x, 3, 0.2])))
    return m


def forelock():
    """Slab strips hanging plumb over the face from between the horns."""
    m = []
    for x, length in ((-2, 16), (1, 12), (3.5, 9)):
        m.append(box((x - 1.2, 5 - length, 0), (x + 1.2, 6, 1.2), MANE))
    return m


def runes():
    """Pale green glyphs floating about the head, drawn at full brightness: hollow squares with a hooked tail, like
    the picture's, each four bars."""
    m = []
    for cx, cy, cz, size in ((-10, 24, 0, 5), (11, 28, -4, 4), (-3, 33, -10, 6), (9, 16, 8, 3.5), (-13, 14, -6, 4), (3, 38, 2, 3)):
        s = size
        m.append(box((cx - s, cy + s - 1, cz), (cx + s, cy + s, cz + 1), RUNE))
        m.append(box((cx - s, cy - s, cz), (cx - s + 1, cy + s, cz + 1), RUNE))
        m.append(box((cx + s - 1, cy - s + 2, cz), (cx + s, cy + s, cz + 1), RUNE))
        m.append(box((cx - s + 3, cy - s, cz), (cx + s, cy - s + 1, cz + 1), RUNE))
    return m


def tail():
    """A stiff branching tail of slab strips, up and back, with zigzag bits and a hanging fork."""
    m = []
    r = ("x", -22.5, [0, 0, 0])
    m.append(box((-1, -1.5, -20), (1, 1.5, 0), MANE, r))
    m.append(box((-1, 1.5, -13), (1, 7, -11), MANE, r))
    m.append(box((-1, 5.5, -18), (1, 7, -11), MANE, r))
    m.append(box((-1, -9, -20), (1, -1.5, -18), MANE, r))
    m.append(box((-1, -9, -27), (1, -7, -18), MANE, r))
    m.append(box((-1, -16, -27), (1, -9, -25), MANE, r))
    m.append(box((-1, -16, -31), (1, -14, -25), MANE, r))
    m.append(box((-1, -3, -9), (1, 4, -7), MANE, r))
    return m


def upper_leg(hind):
    """A foreleg's upper part: shoulder muscle, forearm and elbow down to the knee; a hind leg's: the thigh angling
    forward to the stifle and the gaskin angling back to the hock."""
    m = []
    if hind:
        m += rotated(cyl("y", 0, 0, 3.6, -9, 2, HIDE), ("x", -22.5, [0, 0, 0]))
        stifle = turned((0, -8.6, 0), -22.5)
        m += rotated(cyl("y", stifle[0], stifle[2], 2.8, stifle[1] - 8.5, stifle[1] + 0.5, HIDE), ("x", 22.5, [0, stifle[1], stifle[2]]))
        m += cyl("y", 0, 0.3, 2.4, -UPPER_HIND - 1, -UPPER_HIND + 2.5, HIDE)
    else:
        m += cyl("y", 0, 0.3, 3.3, -5, 2, HIDE)
        m += cyl("y", 0, 0, 2.6, -UPPER_FRONT + 1, -4, HIDE)
        m.append(box((-2.2, -3, -4.2), (2.2, 1, -2.4), HIDE))
    return m


def lower_leg():
    """From the knee or hock: the joint, the cannon in its white bandages, the fetlock, the pastern angling forward
    and a dark hoof."""
    m = []
    m += cyl("y", 0, 0, 2.7, -1.5, 1.5, HIDE)
    m += cyl("y", 0, 0, 1.9, -9.5, -1.5, HIDE)
    m += cyl("y", 0, 0, 2.3, -9, -2, WRAP)
    for y in (-8.2, -6, -3.8):
        m += cyl("y", 0, 0, 2.6, y, y + 1.4, WRAP)
    m += cyl("y", 0, 0.3, 2.2, -11, -9.5, HIDE)
    m.append(box((-1.6, -14, -1.4), (1.6, -11, 1.6), HIDE, ("x", -22.5, [0, -11, 0])))
    m.append(box((-2.5, -LOWER - 4, -2), (2.5, -LOWER - 0.5, 3.2), HOOF))
    return m


def legs():
    out = {}
    for name, joint in SHOULDERS.items():
        out[name] = (joint, False)
    for name, joint in HIPS.items():
        out[name] = (joint, True)
    return out


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


def groups():
    """The Blockbench project's groups, each at its joint with its rest rotation, the children under their parents."""
    head_at = add(NECK, HEAD)
    out = [("body", BODY, None, [shifted(i, BODY) for i in body()]),
           ("neck", NECK, None, [shifted(i, NECK) for i in neck()], "body"),
           ("mane", NECK, None, [shifted(i, NECK) for i in mane()], "neck"),
           ("head", head_at, None, [shifted(i, head_at) for i in head()], "neck"),
           ("forelock", head_at, None, [shifted(i, head_at) for i in forelock()], "head"),
           ("runes", head_at, None, [shifted(i, head_at) for i in runes()], "head"),
           ("tail", TAIL, None, [shifted(i, TAIL) for i in tail()], "body")]
    for name, (joint, hind) in legs().items():
        knee = add(joint, (0, -(UPPER_HIND if hind else UPPER_FRONT), 0))
        out.append((f"{name}_upper", joint, None, [shifted(i, joint) for i in upper_leg(hind)], "body"))
        out.append((f"{name}_lower", knee, None, [shifted(i, knee) for i in lower_leg()], f"{name}_upper"))
    return out


# ------------------------------------------------------------------ previews

def posed(pose=None):
    """Every box in world pixels for a preview, with `pose` = {group: (rx, ry, rz, dx, dy, dz)} applied down the
    tree (box_preview takes a list of turns per element)."""
    import box_preview  # noqa: F401
    pose = pose or {}
    tree = {g[0]: g for g in groups()}
    parents = {g[0]: (g[4] if len(g) > 4 else None) for g in groups()}

    def chain(name):
        turns = []
        while name:
            g = tree[name]
            rx, ry, rz, dx, dy, dz = pose.get(name, (0, 0, 0, 0, 0, 0))
            rest = g[2] or (0, 0, 0)
            rx, ry, rz = rx + rest[0], ry + rest[1], rz + rest[2]
            origin = list(g[1])
            for axis, angle in (("z", rz), ("y", ry), ("x", rx)):
                if angle:
                    turns.append((axis, angle, origin))
            if dx or dy or dz:
                turns.append(("move", (dx, dy, dz)))
            name = parents[name]
        return turns

    out = []
    for g in groups():
        turns = chain(g[0])
        for item in g[3]:
            frm, to, texture = item[:3]
            options = dict(item[3]) if len(item) > 3 else {}
            options["rotations"] = turns
            out.append((frm, to, texture, options))
    return out


# ------------------------------------------------------------------ animation curves (ticks)

def idle_pose(t, period=40):
    """Breathing, a slow nod, the tail and mane swaying, the runes drifting."""
    p = 2 * math.pi * t / period
    pose = {"body": (0, 0, 0, 0, 0.6 * math.sin(p), 0),
            "neck": (3 * math.sin(p + 0.5), 0, 0, 0, 0, 0),
            "head": (4 * math.sin(p + 1.0), 2 * math.sin(p / 2), 0, 0, 0, 0),
            "tail": (0, 6 * math.sin(p * 1.5), 0, 0, 0, 0),
            "mane": (0, 0, 2 * math.sin(p + 0.8), 0, 0, 0),
            "forelock": (0, 0, 3 * math.sin(p + 0.3), 0, 0, 0),
            "runes": (0, 25 * math.sin(p / 2), 0, 0, 1.5 * math.sin(p * 1.3), 0)}
    return pose


def walk_pose(t, period=24):
    """A trot: diagonal legs together, the body bobbing, the head nodding against the step."""
    p = 2 * math.pi * t / period
    pose = {"body": (2 * math.sin(2 * p), 0, 0, 0, 1.2 * abs(math.sin(p)), 0),
            "neck": (-4 * math.sin(2 * p), 0, 0, 0, 0, 0),
            "head": (5 * math.sin(2 * p + 0.5), 0, 0, 0, 0, 0),
            "tail": (6 * math.sin(2 * p), 8 * math.sin(p), 0, 0, 0, 0),
            "mane": (0, 0, 4 * math.sin(p), 0, 0, 0),
            "runes": (0, 10 * math.sin(p / 2), 0, 0, 0, 0)}
    for name, phase in (("fl", 0), ("hr", 0), ("fr", math.pi), ("hl", math.pi)):
        q = p + phase
        pose[f"{name}_upper"] = (-22 * math.sin(q), 0, 0, 0, 0, 0)
        pose[f"{name}_lower"] = (28 * max(0.0, math.sin(q + 0.9)), 0, 0, 0, 0, 0)
    return pose


def gallop_pose(t, period=14):
    """A gallop: the fronts reach together, the hinds drive together, the body rocks and leaves the ground."""
    p = 2 * math.pi * t / period
    air = max(0.0, math.sin(p))
    pose = {"body": (-10 * math.sin(p), 0, 0, 0, 4 * air, 0),
            "neck": (8 * math.sin(p), 0, 0, 0, 0, 0),
            "head": (8 * math.sin(p + 0.6), 0, 0, 0, 0, 0),
            "tail": (14 * math.sin(p) - 10, 0, 0, 0, 0, 0),
            "mane": (0, 0, 6 * math.sin(p + 1), 0, 0, 0),
            "forelock": (-10 * air, 0, 0, 0, 0, 0),
            "runes": (0, 20 * math.sin(p / 2), 0, 0, 2 * air, 0)}
    for name, phase, swing in (("fl", 0, 40), ("fr", 0.5, 40), ("hl", math.pi, 35), ("hr", math.pi + 0.5, 35)):
        q = p + phase
        pose[f"{name}_upper"] = (-swing * math.sin(q), 0, 0, 0, 0, 0)
        pose[f"{name}_lower"] = (45 * max(0.0, math.sin(q + 1.2)), 0, 0, 0, 0, 0)
    return pose


CHARGE_TICKS = 40


def charge_pose(t):
    """It rears up on its hind legs, horns forward and runes flaring, hangs there, then slams down and settles."""
    def smooth(u):
        u = max(0.0, min(1.0, u))
        return u * u * (3 - 2 * u)
    if t < 8:
        r = smooth(t / 8)
    elif t < 22:
        r = 1 + 0.03 * math.sin((t - 8) * 1.2)
    elif t < 27:
        r = 1 - smooth((t - 22) / 5)
    else:
        u = (t - 27) / (CHARGE_TICKS - 27)
        r = -0.08 * math.exp(-u * 3) * math.cos(u * 7)
    rear = -40 * r
    flare = max(0.0, r)
    squash = -0.12 * math.sin(math.pi * (t - 27) / 6) if 27 <= t < 33 else 0.0
    pose = {"body": (rear, 0, 0, 0, 10 * max(0.0, r) + squash * 20, 0),
            "neck": (-10 * r, 0, 0, 0, 0, 0),
            "head": (18 * r, 0, 0, 0, 0, 0),
            "tail": (-10 * r, 0, 0, 0, 0, 0),
            "mane": (0, 0, 0, 0, 0, 0),
            "forelock": (-25 * r, 0, 0, 0, 0, 0),
            "runes": (0, 60 * r, 0, 0, 4 * flare, 0),
            "fl_upper": (-50 * r, 0, 0, 0, 0, 0), "fl_lower": (70 * max(0.0, r), 0, 0, 0, 0, 0),
            "fr_upper": (-35 * r, 0, 0, 0, 0, 0), "fr_lower": (60 * max(0.0, r), 0, 0, 0, 0, 0),
            "hl_upper": (30 * r, 0, 0, 0, 0, 0), "hl_lower": (-15 * r, 0, 0, 0, 0, 0),
            "hr_upper": (30 * r, 0, 0, 0, 0, 0), "hr_lower": (-15 * r, 0, 0, 0, 0, 0)}
    return pose, 1 + flare * 0.8, squash


def animations():
    import blockbench_export
    names = [g[0] for g in groups()]

    def clip(name, length, frames, poses, loop, extra=None):
        tracks = {n: {"rotation": [], "position": [], "scale": []} for n in names}
        for i in range(frames + 1):
            time = round(length * i / frames, 4)
            pose = poses(i, frames)
            for n in names:
                rx, ry, rz, dx, dy, dz = pose.get(n, (0, 0, 0, 0, 0, 0))
                tracks[n]["rotation"].append((time, (rx, ry, rz)))
                tracks[n]["position"].append((time, (dx, dy, dz)))
            if extra:
                for n, scale in extra(i, frames).items():
                    tracks[n]["scale"].append((time, scale))
        tracks = {g: {c: k for c, k in ch.items() if k and any(v != (0, 0, 0) for _, v in k) or c == "scale" and k}
                  for g, ch in tracks.items()}
        return blockbench_export.animation(name, length, tracks, loop=loop)

    idle = clip("idle", 2.0, 20, lambda i, n: idle_pose(40 * i / n), True)
    walk = clip("walk", 1.2, 24, lambda i, n: walk_pose(24 * i / n), True)
    gallop = clip("gallop", 0.7, 14, lambda i, n: gallop_pose(14 * i / n), True)

    def charge_frame(i, n):
        return charge_pose(CHARGE_TICKS * i / n)[0]

    def charge_scale(i, n):
        _, flare, squash = charge_pose(CHARGE_TICKS * i / n)
        return {"runes": (flare, flare, flare), "body": (1 - squash / 2, 1 + squash, 1 - squash / 2)}
    charge = clip("charge", 2.0, 40, charge_frame, False, charge_scale)
    return [idle, walk, gallop, charge]


def write_bbmodel(folder=ART):
    import blockbench_export
    return blockbench_export.write(folder / "twin_horn_steed.bbmodel", "twin_horn_steed", groups(),
                                   draw=lambda name: TEXTURES[name](), animations=animations())


# ------------------------------------------------------------------ art (flat, clean)

GREY = [(58, 62, 68), (86, 92, 100), (112, 118, 126), (128, 134, 142), (150, 156, 164)]
SLATE = [(34, 42, 48), (48, 58, 66), (60, 72, 80), (74, 86, 94)]
BROWN = [(70, 48, 36), (108, 74, 54), (132, 96, 70), (156, 118, 88)]


def plain(fill, light, shade):
    img = clean_metal.canvas(fill)
    clean_metal.bevel(img, 0, 0, 15, 15, light, shade, fill)
    return img


def hide():
    img = plain(GREY[2], GREY[3], GREY[1])
    for x0, x1, y in ((3, 7, 5), (9, 13, 9), (2, 5, 12)):
        clean_metal.rect(img, x0, y, x1, y, GREY[1])
    return img


def wrap():
    img = clean_metal.canvas((236, 236, 230))
    for y in (3, 8, 13):
        clean_metal.rect(img, 0, y, 15, y, (196, 196, 188))
    for x0, y0 in ((2, 1), (9, 5), (5, 10)):
        clean_metal.rect(img, x0, y0, x0 + 3, y0, (210, 210, 202))
    return img


def eye():
    img = clean_metal.canvas((250, 252, 255))
    clean_metal.rect(img, 0, 0, 15, 0, (210, 230, 240))
    return img


def rune():
    img = clean_metal.canvas((196, 240, 170))
    clean_metal.rect(img, 0, 0, 15, 0, (226, 250, 206))
    clean_metal.rect(img, 0, 15, 15, 15, (160, 206, 136))
    return img


TEXTURES = {HIDE: hide, SHADE: lambda: plain(GREY[1], GREY[2], GREY[0]), MANE: lambda: plain(SLATE[1], SLATE[2], SLATE[0]),
            BLAZE: lambda: plain((238, 240, 242), (250, 250, 252), (214, 218, 224)), WRAP: wrap,
            HORN: lambda: plain(BROWN[1], BROWN[2], BROWN[0]), EYE: eye, RUNE: rune,
            HOOF: lambda: plain(GREY[0], GREY[1], (40, 44, 48))}


def draw_all(save):
    for name, draw in TEXTURES.items():
        save(draw(), "block", name)


if __name__ == "__main__":
    import sys
    print(sum(len(g[3]) for g in groups()), "boxes in", len(groups()), "groups")
    if "--bbmodel" in sys.argv:
        print(write_bbmodel())
    if "--preview" in sys.argv:
        import box_preview
        draw = lambda n: TEXTURES[n]()
        out = sys.argv[-1]
        for yaw, suffix in ((35, "front_left"), (-35, "front_right"), (90, "side"), (-145, "back")):
            box_preview.render(posed(), scale=4, yaw=yaw, pitch=18, draw=draw).save(out.replace(".png", f"_{suffix}.png"))
        frames = []
        for name, poses in (("walk", [walk_pose(t) for t in (0, 6, 12, 18)]),
                            ("gallop", [gallop_pose(t) for t in (0, 3.5, 7, 10.5)]),
                            ("charge", [charge_pose(t)[0] for t in (4, 14, 28, 36)])):
            for pose in poses:
                frames.append(box_preview.render(posed(pose), scale=3, yaw=-35, pitch=18, draw=draw))
        from PIL import Image
        w = max(f.width for f in frames)
        h = max(f.height for f in frames)
        sheet = Image.new("RGB", (4 * (w + 8), 3 * (h + 8)), (236, 238, 242))
        for i, f in enumerate(frames):
            sheet.paste(f, ((i % 4) * (w + 8) + (w - f.width) // 2, (i // 4) * (h + 8) + h - f.height))
        sheet.save(out.replace(".png", "_frames.png"))
        print(out)

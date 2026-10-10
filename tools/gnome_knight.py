"""The Gnome Knight model, after the owner's picture of 10 October 2026: a short stocky gnome in a tall conical steel
helm whose brim hides his eyes, a big pink nose and a great white beard, puffed blue-and-yellow striped sleeves over
brown gauntlets, a riveted breastplate, a red sash under a buckled belt with a dagger at the left hip, a dagged
blue-and-yellow skirt hung with bells, grey boots, and a greatsword as tall as himself with a gilt hilt. A model only,
by the owner's choice: no entity or data.

Units are pixels, the knight facing +z with the ground at y = 0. The project (art/gnome_knight/gnome_knight.bbmodel)
has every box in groups at their joints, the Minecraft way: body > head (helm, nose, beard), body > left and right arms
(the sword in the right hand), body > skirt, and two legs; with clips sampled from the curves below: "idle" (breathing,
the sword resting on the shoulder, the bells and beard stirring), "walk" (a stumpy march, the skirt and bells swinging)
and "attack" (the sword hauled up over the head and swung down with the whole body).

    python tools/gnome_knight.py --bbmodel --preview out.png
"""
import math
from pathlib import Path

import clean_metal
from steampunk_models import box, cyl

ROOT = Path(__file__).resolve().parents[1]
ART = ROOT / "art" / "gnome_knight"

STEEL, STEEL_DARK, STRIPE, GLOVE, SKIN, BEARD, RED, BELT, BLUE, YELLOW, GOLD, BOOT, GRIP = (
    "gk_steel", "gk_steel_dark", "gk_stripe", "gk_glove", "gk_skin", "gk_beard", "gk_red", "gk_belt", "gk_blue",
    "gk_yellow", "gk_gold", "gk_boot", "gk_grip")

# Joints, in pixels.
HIP = (0, 10, 0)
NECK = (0, 24, 0)
SHOULDERS = {"left_arm": (8.5, 22, 0.5), "right_arm": (-8.5, 22, 0.5)}
LEGS = {"left_leg": (3.5, 10, 0), "right_leg": (-3.5, 10, 0)}
HAND = (0, -16, 0)        # from the shoulder
SKIRT = (0, 11, 0)


# ------------------------------------------------------------------ parts (each from its joint)

def body():
    """The breastplate with its belly and rivets, the red sash, the buckled belt and the dagger at the left hip."""
    m = []
    m.append(box((-6.5, 0, -4), (6.5, 14, 5), STEEL))
    m.append(box((-7.2, 2, -4.6), (7.2, 10, 6), STEEL))
    m.append(box((-6, 10, -4.3), (6, 13, 5.6), STEEL_DARK))
    for x in (-5.5, 4.5):
        for y in (4, 8):
            m.append(box((x, y, 6), (x + 1, y + 1, 6.4), STEEL_DARK))
    m.append(box((-7.5, 4.5, -4.9), (7.5, 7.5, 6.3), RED))
    m.append(box((7.4, -2, -3), (8.6, 5, 1), RED, ("x", -22.5, [8, 5, -1])))
    m.append(box((-7.7, 1.5, -5.1), (7.7, 4.2, 6.6), BELT))
    m.append(box((-2.2, 1.2, 6.6), (2.2, 4.5, 7.2), STEEL))
    m.append(box((-1.2, 2, 7.2), (1.2, 3.7, 7.5), BELT))
    for x in (-6, -4, 4, 6):
        m.append(box((x, 2.6, 6.6), (x + 0.6, 3.2, 6.9), STEEL_DARK))
    m.append(box((7.2, -6, -1), (8.6, 2, 1), BELT))
    m.append(box((7.4, 2, -0.8), (8.4, 3.5, 0.8), STEEL))
    m.append(box((7, 3.5, -1), (8.8, 4.3, 1), GOLD))
    return m


def head():
    """The face under the helm: the big nose, the beard in two tiers with a moustache, and the conical helm with its
    riveted brim pulled down over the eyes, its tiers set a little forward so it leans."""
    m = []
    m.append(box((-4, 0, -4), (4, 8, 4), SKIN))
    m += cyl("z", 0, 4.2, 1.7, 3.5, 7.8, SKIN)
    m.append(box((-3.6, 2.2, 4), (3.6, 4, 6), BEARD))
    m.append(box((-5, -6, 0.5), (5, 2.6, 5.4), BEARD))
    m.append(box((-3.2, -10.5, 1.5), (3.2, -6, 4.6), BEARD))
    m.append(box((-4.6, -1, -1), (-3.9, 4, 3), BEARD))
    m.append(box((3.9, -1, -1), (4.6, 4, 3), BEARD))
    m.append(box((-6, 6.5, -6), (6, 8.5, 6.5), STEEL))
    m.append(box((-5.6, 4.6, 4), (5.6, 6.5, 8), STEEL))
    for x in (-4.5, -1.5, 1.5, 4.5):
        m.append(box((x - 0.4, 5.2, 8), (x + 0.4, 6, 8.4), STEEL_DARK))
    for i, (half, y0, y1, lean) in enumerate(((5.2, 8.5, 14, 0.4), (4.3, 14, 20, 0.9), (3.4, 20, 26, 1.4),
                                                (2.5, 26, 31, 1.9), (1.6, 31, 36, 2.3), (0.8, 36, 40, 2.6))):
        m.append(box((-half, y0, -half + lean), (half, y1, half + lean), STEEL if i % 2 == 0 else STEEL_DARK))
    return m


def arm(side):
    """A puffed striped sleeve, a leather cuff and a brown gauntlet with a thumb."""
    m = []
    m.append(box((-4.2, -3.5, -4.2), (4.2, 1.5, 4.2), STRIPE))
    m.append(box((-3.4, -9, -3.4), (3.4, -3.5, 3.4), STRIPE))
    m.append(box((-2.8, -11.5, -2.8), (2.8, -9, 2.8), BELT))
    m.append(box((-2.6, -17, -2.6), (2.6, -11.5, 2.6), GLOVE))
    thumb = 2.6 if side > 0 else -3.6
    m.append(box((thumb, -14.5, -0.5), (thumb + 1, -12, 1.5), GLOVE))
    return m


def sword():
    """The greatsword from the fist: a wrapped grip, a gilt pommel and crossguard, and a long blade with a fuller
    and a tapered point, standing up through the hand."""
    m = []
    m.append(box((-0.8, -3.5, -0.8), (0.8, 3, 0.8), GRIP))
    m += cyl("y", 0, 0, 1.5, -5.5, -3.5, GOLD)
    m.append(box((-5.5, 3, -0.7), (5.5, 4.4, 0.7), GOLD))
    m.append(box((-0.7, 4.4, -0.7), (0.7, 6.5, 0.7), GOLD))
    m.append(box((-1.5, 4.4, -0.45), (1.5, 40, 0.45), STEEL))
    m.append(box((-0.4, 6, -0.55), (0.4, 34, 0.55), STEEL_DARK))
    m.append(box((-1, 40, -0.4), (1, 44, 0.4), STEEL))
    m.append(box((-0.5, 44, -0.3), (0.5, 46.5, 0.3), STEEL))
    return m


def skirt():
    """The dagged skirt: pointed lappets all round, blue and yellow by turns, a gilt bell at each point."""
    m = []
    n = 12
    for i in range(n):
        a = 2 * math.pi * i / n
        cx, cz = 7.6 * math.sin(a), 6.2 * math.cos(a)
        tex = BLUE if i % 2 else YELLOW
        along = ("z", -22.5, [cx, 0, cz]) if abs(math.sin(a)) > 0.7 else ("x", 22.5 if math.cos(a) > 0 else -22.5, [cx, 0, cz])
        if abs(math.sin(a)) > 0.7:
            along = ("z", -22.5 if cx > 0 else 22.5, [cx, 0, cz])
        m.append(box((cx - 1.6, -5, cz - 1.6), (cx + 1.6, 0.5, cz + 1.6), tex, along))
        m.append(box((cx - 0.9, -8, cz - 0.9), (cx + 0.9, -5, cz + 0.9), tex, along))
        m.append(box((cx - 0.6, -9.3, cz - 0.6), (cx + 0.6, -8, cz + 0.6), GOLD, along))
    return m


def leg():
    """A short grey boot with a toe cap and a strap."""
    m = []
    m.append(box((-3, -6, -3), (3, 0.5, 3), BOOT))
    m.append(box((-3.3, -10, -3.3), (3.3, -6, 4), BOOT))
    m.append(box((-3, -10, 4), (3, -7.5, 5), STEEL_DARK))
    m.append(box((-3.5, -5, -3.5), (3.5, -4, 3.5), BELT))
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


def groups():
    """The Blockbench project's groups, each at its joint, the children under their parents."""
    out = [("body", HIP, None, [shifted(i, HIP) for i in body()]),
           ("head", NECK, None, [shifted(i, NECK) for i in head()], "body"),
           ("skirt", SKIRT, None, [shifted(i, SKIRT) for i in skirt()], "body")]
    for name, joint in SHOULDERS.items():
        side = 1 if "left" in name else -1
        out.append((name, joint, None, [shifted(i, joint) for i in arm(side)], "body"))
    hand = add(SHOULDERS["right_arm"], HAND)
    out.append(("sword", hand, None, [shifted(i, hand) for i in sword()], "right_arm"))
    for name, joint in LEGS.items():
        out.append((name, joint, None, [shifted(i, joint) for i in leg()]))
    return out


# ------------------------------------------------------------------ previews

def posed(pose=None):
    pose = pose or {}
    tree = {g[0]: g for g in groups()}
    parents = {g[0]: (g[4] if len(g) > 4 else None) for g in groups()}

    def chain(name):
        turns = []
        while name:
            g = tree[name]
            rx, ry, rz, dx, dy, dz = pose.get(name, (0, 0, 0, 0, 0, 0))
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

# The rest pose: the sword hand up before the chest, the blade rising back over the shoulder, as in the picture.
REST = {"right_arm": (-60, 0, -15, 0, 0, 0), "sword": (20, 0, 0, 0, 0, 0), "left_arm": (0, 0, 8, 0, 0, 0)}


def with_rest(pose):
    out = dict(REST)
    for k, v in pose.items():
        base = out.get(k, (0, 0, 0, 0, 0, 0))
        out[k] = tuple(base[i] + v[i] for i in range(6))
    return out


def idle_pose(t, period=40):
    """Breathing, the sword resting over the shoulder, the beard and bells stirring."""
    p = 2 * math.pi * t / period
    return with_rest({"body": (0, 0, 0, 0, 0.5 * math.sin(p), 0),
                      "head": (2 * math.sin(p + 0.4), 3 * math.sin(p / 2), 0, 0, 0, 0),
                      "right_arm": (3 * math.sin(p), 0, 0, 0, 0, 0),
                      "left_arm": (0, 0, 2 * math.sin(p + 1), 0, 0, 0),
                      "skirt": (0, 0, 1.5 * math.sin(p + 0.8), 0, 0, 0)})


def walk_pose(t, period=16):
    """A stumpy march: short quick steps, the free arm swinging, the skirt and bells swinging, a waddle."""
    p = 2 * math.pi * t / period
    return with_rest({"body": (0, 0, 4 * math.sin(p), 0, 1.0 * abs(math.sin(p)), 0),
                      "head": (3 * math.sin(2 * p), 0, -3 * math.sin(p), 0, 0, 0),
                      "left_leg": (30 * math.sin(p), 0, 0, 0, 0, 0),
                      "right_leg": (-30 * math.sin(p), 0, 0, 0, 0, 0),
                      "left_arm": (-28 * math.sin(p), 0, 0, 0, 0, 0),
                      "right_arm": (6 * math.sin(p), 0, 0, 0, 0, 0),
                      "skirt": (6 * math.sin(p), 0, -4 * math.sin(p), 0, 0, 0)})


ATTACK_TICKS = 30


def attack_pose(t):
    """The sword hauled up over the head with a wind-up, swung down across the body, and brought back to the
    shoulder."""
    def smooth(u):
        u = max(0.0, min(1.0, u))
        return u * u * (3 - 2 * u)
    if t < 8:                      # wind up: the arm goes up and back, the body turns away
        u = smooth(t / 8)
        arm, twist, lean, sword = -60 - 110 * u, 25 * u, -8 * u, 20 - 30 * u
    elif t < 12:                   # the cut: fast, the body turning into it
        u = smooth((t - 8) / 4)
        arm, twist, lean, sword = -170 + 190 * u, 25 - 50 * u, -8 + 22 * u, -10 + 5 * u
    elif t < 18:                   # follow through and hold
        u = (t - 12) / 6
        arm, twist, lean, sword = 20 + 4 * math.sin(u * 6) * (1 - u), -25 + 5 * u, 14 - 4 * u, -5
    else:                          # back to the shoulder
        u = smooth((t - 18) / (ATTACK_TICKS - 18))
        arm, twist, lean, sword = 20 - 80 * u, -20 + 20 * u, 10 - 10 * u, -5 + 25 * u
    return {"body": (lean, twist, 0, 0, 0, 0), "head": (-lean / 2, -twist / 2, 0, 0, 0, 0),
            "right_arm": (arm, 0, -12, 0, 0, 0), "sword": (sword, 0, 0, 0, 0, 0),
            "left_arm": (-arm / 4, 0, 10, 0, 0, 0), "skirt": (lean / 2, 0, 0, 0, 0, 0),
            "left_leg": (-lean / 2, 0, 0, 0, 0, 0), "right_leg": (lean / 2, 0, 0, 0, 0, 0)}


def animations():
    import blockbench_export
    names = [g[0] for g in groups()]

    def clip(name, length, frames, poses, loop):
        tracks = {n: {"rotation": [], "position": []} for n in names}
        for i in range(frames + 1):
            time = round(length * i / frames, 4)
            pose = poses(i, frames)
            for n in names:
                rx, ry, rz, dx, dy, dz = pose.get(n, (0, 0, 0, 0, 0, 0))
                tracks[n]["rotation"].append((time, (rx, ry, rz)))
                tracks[n]["position"].append((time, (dx, dy, dz)))
        tracks = {g: {c: k for c, k in ch.items() if any(v != (0, 0, 0) for _, v in k)} for g, ch in tracks.items()}
        return blockbench_export.animation(name, length, tracks, loop=loop)

    return [clip("idle", 2.0, 20, lambda i, n: idle_pose(40 * i / n), True),
            clip("walk", 0.8, 16, lambda i, n: walk_pose(16 * i / n), True),
            clip("attack", 1.5, 30, lambda i, n: attack_pose(ATTACK_TICKS * i / n), False)]


def write_bbmodel(folder=ART):
    import blockbench_export
    return blockbench_export.write(folder / "gnome_knight.bbmodel", "gnome_knight", groups(),
                                   draw=lambda name: TEXTURES[name](), animations=animations())


# ------------------------------------------------------------------ art (flat, clean)

def plain(fill, light, shade):
    img = clean_metal.canvas(fill)
    clean_metal.bevel(img, 0, 0, 15, 15, light, shade, fill)
    return img


def steel():
    img = plain((150, 156, 164), (182, 188, 196), (104, 110, 118))
    for x, y in ((2, 2), (13, 2), (2, 13), (13, 13)):
        clean_metal.put(img, x, y, (196, 202, 210))
        clean_metal.put(img, x + 1, y + 1, (96, 102, 110))
    return img


def stripe():
    """Blue and yellow stripes four pixels wide, each lit along one edge, as the puffed sleeves."""
    img = clean_metal.canvas((56, 92, 190))
    for x in range(16):
        yellow = (x // 4) % 2 == 1
        for y in range(16):
            c = (224, 182, 60) if yellow else (56, 92, 190)
            if x % 4 == 0:
                c = (244, 206, 96) if yellow else (84, 120, 214)
            if x % 4 == 3:
                c = (190, 148, 40) if yellow else (38, 66, 150)
            clean_metal.put(img, x, y, c)
    return img


def beard():
    img = plain((232, 232, 230), (246, 246, 244), (196, 198, 200))
    for x0, y0, y1 in ((2, 2, 9), (5, 4, 13), (8, 1, 11), (11, 5, 14), (13, 2, 8)):
        clean_metal.rect(img, x0, y0, x0, y1, (206, 208, 210))
    return img


def belt():
    img = plain((88, 58, 38), (116, 80, 54), (56, 36, 24))
    for x in range(1, 15, 3):
        clean_metal.put(img, x, 7, (130, 94, 64))
    return img


TEXTURES = {STEEL: steel, STEEL_DARK: lambda: plain((110, 116, 124), (136, 142, 150), (76, 82, 90)),
            STRIPE: stripe, GLOVE: lambda: plain((112, 74, 48), (138, 96, 64), (78, 50, 32)),
            SKIN: lambda: plain((238, 176, 150), (250, 198, 176), (208, 140, 118)), BEARD: beard,
            RED: lambda: plain((178, 54, 50), (206, 78, 70), (130, 36, 34)), BELT: belt,
            BLUE: lambda: plain((56, 92, 190), (84, 120, 214), (38, 66, 150)),
            YELLOW: lambda: plain((224, 182, 60), (244, 206, 96), (190, 148, 40)),
            GOLD: lambda: plain((214, 170, 60), (240, 206, 110), (160, 118, 36)),
            BOOT: lambda: plain((90, 94, 102), (112, 116, 124), (62, 66, 72)),
            GRIP: lambda: plain((150, 112, 60), (178, 140, 86), (108, 78, 40))}


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
        from PIL import Image
        draw = lambda n: TEXTURES[n]()
        out = sys.argv[-1]
        rest = with_rest({})
        for yaw, suffix in ((30, "front_left"), (-30, "front_right"), (90, "side"), (-150, "back")):
            box_preview.render(posed(rest), scale=5, yaw=yaw, pitch=14, draw=draw).save(out.replace(".png", f"_{suffix}.png"))
        frames = []
        for poses in ([walk_pose(t) for t in (0, 4, 8, 12)], [attack_pose(t) for t in (6, 10, 14, 24)]):
            for pose in poses:
                frames.append(box_preview.render(posed(pose), scale=4, yaw=-30, pitch=14, draw=draw))
        w = max(f.width for f in frames)
        h = max(f.height for f in frames)
        sheet = Image.new("RGB", (4 * (w + 8), 2 * (h + 8)), (236, 238, 242))
        for i, f in enumerate(frames):
            sheet.paste(f, ((i % 4) * (w + 8) + (w - f.width) // 2, (i // 4) * (h + 8) + h - f.height))
        sheet.save(out.replace(".png", "_frames.png"))
        print(out)

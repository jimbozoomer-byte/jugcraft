"""The Sun Monk boss model, after the owner's picture of 10 October 2026, at Minecraft player proportions: a white hood
with a black face slot under a great square plank hat, a dark purple robe with a flared folded skirt, wide sleeves
with gold cuffs and tassels, a gold sash, white socks in red sandals; a dark staff with a golden sunburst head in the
right hand and a great golden eight-point throwing star in the left. A model only, by the owner's choice: no entity
or data.

Units are pixels, the monk facing +z with the ground at y = 0. The project (art/sun_monk/sun_monk.bbmodel) has
every box in groups at their joints, the Minecraft figure's way: body > head (hood, face, hat), body > arms (the staff
in the right hand, the star in the left), body > skirt, and two legs; with clips sampled from the curves below: "idle"
(a hover-bob, the sleeves and skirt swaying, the star turning slowly), "walk" (a gliding shuffle, the skirt and
tassels swinging) and "attack" (the staff swept overhead and down while the star spins up and is thrust forward).

    python tools/sun_monk.py --bbmodel --preview out.png
"""
import math
from pathlib import Path

import clean_metal
from steampunk_models import box, cyl

ROOT = Path(__file__).resolve().parents[1]
ART = ROOT / "art" / "sun_monk"

PLANKS, PLANK_DARK, WHITE, BLACK, ROBE, ROBE_LIGHT, GOLD, GOLD_DARK, SHAFT, SOCK, SANDAL, SKIN = (
    "sm_planks", "sm_plank_dark", "sm_white", "sm_black", "sm_robe", "sm_robe_light", "sm_gold", "sm_gold_dark",
    "sm_shaft", "sm_sock", "sm_sandal", "sm_skin")

# Joints, in pixels.
HIP = (0, 12, 0)
NECK = (0, 24, 0)
SHOULDERS = {"left_arm": (6, 22, 0), "right_arm": (-6, 22, 0)}
LEGS = {"left_leg": (2, 12, 0), "right_leg": (-2, 12, 0)}
HAND = (0, -14, 0)
SKIRT = (0, 13, 0)
# The rest pose: both arms a little forward, the left out to the side so the star stands clear of the body; the staff
# and star are modelled in front of the fists, so neither cuts into the robe.
REST = {"right_arm": (-60, 0, -6, 0, 0, 0), "left_arm": (-10, 0, 20, 0, 0, 0), "staff": (0, 0, 0, 0, 0, 0),
        "star": (0, 0, 0, 0, 0, 0)}


def rest_rotation(name):
    pose = REST.get(name)
    return tuple(pose[:3]) if pose else None


# ------------------------------------------------------------------ parts (each from its joint)

def body():
    """The robe's torso with the white hood's cape over the shoulders, the gold sash with its knot, and the robe's
    overlapping front panels."""
    m = []
    m.append(box((-4, -12, -2), (4, 0, 2), ROBE))
    m.append(box((-4.2, -4, -2.2), (4.2, -1, 2.2), ROBE_LIGHT))
    m.append(box((-0.3, -12, 2), (0.3, -1, 2.3), ROBE_LIGHT))
    m.append(box((-4.4, -7, -2.4), (4.4, -5, 2.4), GOLD))
    m.append(box((-1.5, -7.5, 2.4), (1.5, -4.5, 3.2), GOLD_DARK))
    m.append(box((-1, -11, 2.5), (0, -7.5, 3), GOLD))
    m.append(box((0.3, -10, 2.5), (1.3, -7.5, 3), GOLD))
    m.append(box((-5.2, -5, -3.2), (5.2, 0.6, 3.2), WHITE))
    m.append(box((-5.6, -7.5, -3.6), (5.6, -5, 3.6), WHITE))
    m.append(box((-3, -9, 2.8), (3, -7.5, 3.4), WHITE))
    return m


def head():
    """The white hood over the head with the black face slot, and the great square plank hat with its dark rim and
    bands, set a little askew."""
    m = []
    m.append(box((-4.4, -0.5, -4.4), (4.4, 8.4, 4.4), WHITE))
    m.append(box((-2.6, 1.5, 4.4), (2.6, 4.5, 4.7), BLACK))
    m.append(box((-1.6, 4.5, 4.4), (1.6, 6.5, 4.7), BLACK))
    m.append(box((-0.6, 6.5, 4.4), (0.6, 7.3, 4.7), BLACK))
    m.append(box((-12.5, 7.6, -12.5), (12.5, 8.6, 12.5), PLANK_DARK, ("y", 8, [0, 8, 0])))
    m.append(box((-12, 8.6, -12), (12, 12.4, 12), PLANKS, ("y", 8, [0, 8, 0])))
    for x0, x1 in ((-12.2, -11.4), (11.4, 12.2)):
        m.append(box((x0, 8.4, -12.2), (x1, 12.6, 12.2), PLANK_DARK, ("y", 8, [0, 8, 0])))
    for z0, z1 in ((-12.2, -11.4), (11.4, 12.2)):
        m.append(box((-12.2, 8.4, z0), (12.2, 12.6, z1), PLANK_DARK, ("y", 8, [0, 8, 0])))
    for z in (-4, 4):
        m.append(box((-11.6, 12.4, z - 0.4), (11.6, 12.7, z + 0.4), PLANK_DARK, ("y", 8, [0, 8, 0])))
    return m


def arm(side):
    """A wide robe sleeve, its gold cuff with a dark band and three hanging tassels, and the hand."""
    m = []
    m.append(box((-2, -12, -2), (2, 0.5, 2), ROBE))
    m.append(box((-3.6, -8, -3.6), (3.6, 0.5, 3.6), ROBE))
    m.append(box((-4.1, -11, -4.1), (4.1, -8, 4.1), GOLD))
    m.append(box((-4.3, -9.2, -4.3), (4.3, -8.4, 4.3), GOLD_DARK))
    for z in (-3, 0, 3):
        m.append(box((side * 4.1 - (0.5 if side > 0 else 0), -16, z - 0.5), (side * 4.1 + (0 if side > 0 else 0.5), -11, z + 0.5), GOLD))
        m.append(box((side * 4.1 - (0.6 if side > 0 else 0), -17, z - 0.6), (side * 4.1 + (0 if side > 0 else 0.6), -16, z + 0.6), GOLD_DARK))
    m.append(box((-2, -15, -2), (2, -11, 2), SKIN))
    return m


def staff():
    """From the right fist: a dark shaft with a gold ferrule, and a golden sunburst head: a disc, eight long rays and
    eight short ones between them."""
    m = []
    zc = 5.3  # the staff runs down in front of the fist, clear of the robe
    m.append(box((-0.7, -14, zc - 0.7), (0.7, 4, zc + 0.7), SHAFT))
    m += cyl("y", 0, zc, 1.2, -16, -14, GOLD)
    m += cyl("y", 0, zc, 1.0, 4, 6, GOLD_DARK)
    m += cyl("z", 0, 10, 3.2, zc - 0.8, zc + 0.8, GOLD_DARK)
    m += cyl("z", 0, 10, 2, zc - 1.0, zc + 1.0, GOLD)
    for angle in (0, 45):
        for w, h, d in ((9, 1.2, 0.6), (6, 2.2, 0.45)):
            cut = 0.05 if angle else 0
            turn = ("z", angle + 22.5 * (w == 6), [0, 10, zc])
            m.append(box((-w, 10 - h / 2, zc - d + cut), (w, 10 + h / 2, zc + d - cut), GOLD, turn))
            m.append(box((-h / 2, 10 - w, zc - d + cut), (h / 2, 10 + w, zc + d - cut), GOLD, turn))
    return m


def star():
    """From the left fist: a great golden eight-point throwing star, flat, each point a wide base and a thin tip, a
    dark hub with a bright boss."""
    m = []
    zc = 6  # the star stands in front of the fist, clear of the sleeve and robe
    for angle in (0, 45):
        cut = 0.08 if angle else 0
        turn = ("z", angle, [0, 0, zc])
        m.append(box((-12, -1.6, zc - 0.5 + cut), (12, 1.6, zc + 0.5 - cut), GOLD, turn))
        m.append(box((-1.6, -12, zc - 0.5 + cut), (1.6, 12, zc + 0.5 - cut), GOLD, turn))
        m.append(box((-8, -2.8, zc - 0.35 + cut), (8, 2.8, zc + 0.35 - cut), GOLD, turn))
        m.append(box((-2.8, -8, zc - 0.35 + cut), (2.8, 8, zc + 0.35 - cut), GOLD, turn))
    m += cyl("z", 0, 0, 3.4, zc - 0.7, zc + 0.7, GOLD_DARK)
    m += cyl("z", 0, 0, 1.6, zc - 0.9, zc + 0.9, GOLD)
    m.append(box((-0.8, -3, 1.6), (0.8, 3, zc - 0.6), SHAFT))
    return m


def skirt():
    """The robe's flared skirt in three tiers down to the sandals, lighter folds standing proud all round, and the
    hem's split at the front."""
    m = []
    m.append(box((-4.8, -6, -2.8), (4.8, 0.5, 2.8), ROBE))
    m.append(box((-5.8, -11, -3.8), (5.8, -6, 3.8), ROBE))
    m.append(box((-6.8, -12.5, -4.8), (6.8, -11, 4.8), ROBE_LIGHT))
    for x, z0, z1 in ((-5.3, -2, 2), (5.3, -2, 2)):
        m.append(box((x - 0.3, -11.5, z0), (x + 0.3, -1, z1), ROBE_LIGHT))
    for z in (-3.5, 3.5):
        for x in (-3.5, 0, 3.5):
            m.append(box((x - 0.7, -11.6, z - 0.3 - 0.3 * (z > 0)), (x + 0.7, -3, z + 0.3 + 0.3 * (z < 0)), ROBE_LIGHT))
    m.append(box((-0.4, -12.5, 3.6), (0.4, -6, 5.1), ROBE_LIGHT))
    return m


def leg():
    """A white sock in a red sandal with a strap and a dark sole."""
    m = []
    m.append(box((-2, -7, -2), (2, 0.5, 2), ROBE))
    m.append(box((-2.1, -11, -2.1), (2.1, -7, 2.1), SOCK))
    m.append(box((-2.5, -12, -2.7), (2.5, -10.8, 3.2), SANDAL))
    m.append(box((-2.3, -11.2, 0.5), (2.3, -10.5, 1.3), SANDAL))
    m.append(box((-2.6, -12.2, -2.8), (2.6, -11.6, 3.3), BLACK))
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
    out = [("body", HIP, None, [shifted(i, HIP) for i in body()]),
           ("head", NECK, None, [shifted(i, NECK) for i in head()], "body"),
           ("skirt", SKIRT, None, [shifted(i, SKIRT) for i in skirt()], "body")]
    for name, joint in SHOULDERS.items():
        side = 1 if "left" in name else -1
        out.append((name, joint, rest_rotation(name), [shifted(i, joint) for i in arm(side)], "body"))
    right = add(SHOULDERS["right_arm"], HAND)
    left = add(SHOULDERS["left_arm"], HAND)
    out.append(("staff", right, rest_rotation("staff"), [shifted(i, right) for i in staff()], "right_arm"))
    out.append(("star", left, rest_rotation("star"), [shifted(i, left) for i in star()], "left_arm"))
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
            rx, ry, rz, dx, dy, dz = pose.get(name, REST.get(name, (0, 0, 0, 0, 0, 0)))
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

def with_rest(pose):
    out = dict(REST)
    for k, v in pose.items():
        base = out.get(k, (0, 0, 0, 0, 0, 0))
        out[k] = tuple(base[i] + v[i] for i in range(6))
    return out


def idle_pose(t, period=40):
    """A hover-bob, the sleeves and skirt swaying, the head turning, the star turning slowly in the hand."""
    p = 2 * math.pi * t / period
    return with_rest({"body": (0, 0, 0, 0, 1.2 * math.sin(p), 0),
                      "head": (2 * math.sin(p + 0.5), 4 * math.sin(p / 2), 1.5 * math.sin(p), 0, 0, 0),
                      "left_arm": (2 * math.sin(p), 0, 3 * math.sin(p + 1), 0, 0, 0),
                      "right_arm": (3 * math.sin(p + 0.3), 0, -2 * math.sin(p), 0, 0, 0),
                      "star": (0, 0, 360 * t / period, 0, 0, 0),
                      "skirt": (0, 0, 2 * math.sin(p + 0.8), 0, 0, 0)})


def walk_pose(t, period=20):
    """A gliding shuffle: short steps, a slight lean, the skirt, sleeves and tassels swinging."""
    p = 2 * math.pi * t / period
    return with_rest({"body": (3, 0, 2 * math.sin(p), 0, 0.6 * abs(math.sin(p)), 0),
                      "head": (-2, 0, -2 * math.sin(p), 0, 0, 0),
                      "left_leg": (18 * math.sin(p), 0, 0, 0, 0, 0),
                      "right_leg": (-18 * math.sin(p), 0, 0, 0, 0, 0),
                      "left_arm": (-10 * math.sin(p), 0, 0, 0, 0, 0),
                      "right_arm": (10 * math.sin(p), 0, 0, 0, 0, 0),
                      "star": (0, 0, 360 * t / period, 0, 0, 0),
                      "skirt": (5 * math.sin(p), 0, -3 * math.sin(p), 0, 0, 0)})


ATTACK_TICKS = 30


def attack_pose(t):
    """The staff swept up overhead and down with the body turning into it, while the star spins up and is thrust
    out at the end; then back to rest."""
    def smooth(u):
        u = max(0.0, min(1.0, u))
        return u * u * (3 - 2 * u)
    if t < 8:
        u = smooth(t / 8)
        right, twist, lean, left, spin = -60 - 100 * u, 20 * u, -6 * u, -10 - 20 * u, 90 * u
    elif t < 12:
        u = smooth((t - 8) / 4)
        right, twist, lean, left, spin = -160 + 150 * u, 20 - 45 * u, -6 + 16 * u, -30 - 60 * u, 90 + 360 * u
    elif t < 18:
        u = (t - 12) / 6
        right, twist, lean, left, spin = -10 + 3 * math.sin(u * 6) * (1 - u), -25 + 5 * u, 10 - 3 * u, -90, 450 + 540 * u
    else:
        u = smooth((t - 18) / (ATTACK_TICKS - 18))
        right, twist, lean, left, spin = -10 - 50 * u, -20 + 20 * u, 7 - 7 * u, -90 + 80 * u, 990 + 90 * u
    return {"body": (lean, twist, 0, 0, 0, 0), "head": (-lean / 2, -twist / 2, 0, 0, 0, 0),
            "right_arm": (right, 0, -6, 0, 0, 0), "staff": (0, 0, 0, 0, 0, 0),
            "left_arm": (left, 0, 20, 0, 0, 0), "star": (0, 0, spin, 0, 0, 0),
            "skirt": (lean / 2, 0, 0, 0, 0, 0), "left_leg": (-lean / 2, 0, 0, 0, 0, 0), "right_leg": (lean / 2, 0, 0, 0, 0, 0)}


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
                rest = REST.get(n, (0, 0, 0, 0, 0, 0))
                tracks[n]["rotation"].append((time, (rx - rest[0], ry - rest[1], rz - rest[2])))
                tracks[n]["position"].append((time, (dx, dy, dz)))
        tracks = {g: {c: k for c, k in ch.items() if any(v != (0, 0, 0) for _, v in k)} for g, ch in tracks.items()}
        return blockbench_export.animation(name, length, tracks, loop=loop)

    return [clip("idle", 2.0, 20, lambda i, n: idle_pose(40 * i / n), True),
            clip("walk", 1.0, 20, lambda i, n: walk_pose(20 * i / n), True),
            clip("attack", 1.5, 30, lambda i, n: attack_pose(ATTACK_TICKS * i / n), False)]


def write_bbmodel(folder=ART):
    import blockbench_export
    return blockbench_export.write(folder / "sun_monk.bbmodel", "sun_monk", groups(),
                                   draw=lambda name: TEXTURES[name](), animations=animations())


# ------------------------------------------------------------------ art (flat, clean)

def plain(fill, light, shade):
    img = clean_metal.canvas(fill)
    clean_metal.bevel(img, 0, 0, 15, 15, light, shade, fill)
    return img


def planks():
    """Oak-like planks: four boards with seams and a few nail marks, lit along their tops."""
    img = clean_metal.canvas((168, 132, 80))
    for y in range(16):
        for x in range(16):
            c = (168, 132, 80)
            if y % 4 == 0:
                c = (104, 78, 44)
            elif y % 4 == 1:
                c = (190, 152, 96)
            if (x + y // 4 * 7) % 16 == 0 and y % 4 in (2, 3):
                c = (104, 78, 44)
            clean_metal.put(img, x, y, c)
    for x, y in ((3, 2), (12, 6), (6, 10), (13, 14)):
        clean_metal.put(img, x, y, (84, 60, 32))
    return img


def robe():
    img = plain((78, 58, 86), (96, 74, 106), (58, 42, 66))
    for x0, y0, y1 in ((3, 2, 9), (9, 5, 14), (13, 1, 6)):
        clean_metal.rect(img, x0, y0, x0, y1, (66, 48, 74))
    return img


def gold():
    img = plain((222, 176, 70), (246, 212, 120), (168, 122, 40))
    for x, y in ((4, 4), (11, 9)):
        clean_metal.put(img, x, y, (246, 212, 120))
    return img


TEXTURES = {PLANKS: planks, PLANK_DARK: lambda: plain((104, 78, 44), (124, 94, 56), (76, 56, 30)),
            WHITE: lambda: plain((240, 240, 236), (252, 252, 250), (210, 210, 206)),
            BLACK: lambda: plain((22, 22, 26), (40, 40, 46), (12, 12, 14)),
            ROBE: robe, ROBE_LIGHT: lambda: plain((108, 86, 118), (128, 104, 138), (84, 64, 94)),
            GOLD: gold, GOLD_DARK: lambda: plain((168, 122, 40), (200, 150, 58), (120, 86, 28)),
            SHAFT: lambda: plain((52, 42, 60), (70, 58, 80), (36, 28, 42)),
            SOCK: lambda: plain((228, 228, 226), (244, 244, 242), (196, 196, 194)),
            SANDAL: lambda: plain((164, 46, 44), (196, 68, 64), (118, 30, 30)),
            SKIN: lambda: plain((238, 196, 170), (250, 214, 190), (210, 164, 136))}


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
        for yaw, suffix in ((30, "front_left"), (-30, "front_right"), (90, "side"), (-150, "back")):
            box_preview.render(posed(), scale=5, yaw=yaw, pitch=14, draw=draw).save(out.replace(".png", f"_{suffix}.png"))
        frames = []
        for poses in ([walk_pose(t) for t in (0, 5, 10, 15)], [attack_pose(t) for t in (6, 10, 14, 24)]):
            for pose in poses:
                frames.append(box_preview.render(posed(pose), scale=4, yaw=-30, pitch=14, draw=draw))
        w = max(f.width for f in frames)
        h = max(f.height for f in frames)
        sheet = Image.new("RGB", (4 * (w + 8), 2 * (h + 8)), (236, 238, 242))
        for i, f in enumerate(frames):
            sheet.paste(f, ((i % 4) * (w + 8) + (w - f.width) // 2, (i // 4) * (h + 8) + h - f.height))
        sheet.save(out.replace(".png", "_frames.png"))
        print(out)

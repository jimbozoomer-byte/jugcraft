"""Stolas, the Great Prince of the Ars Goetia: a crowned owl on long legs, a boss at the Monkey King's scale,
10 October 2026. A still model, no clips until the owner asks.

- Head: a great round owl's head with a heart-shaped pale facial disc rimmed in dark feathers, huge amber eyes with
  black pupils, a hooked beak, two tall ear tufts leaning outward, and the prince's crown: a gold band with six
  points, purple jewels and a star on the front plate.
- Body: an egg-shaped torso of barred feathers, a cream breast of overlapping feather rows, a dark back, a ruff of
  tufts at the neck, a gold collar with a crescent-moon pendant (he teaches astronomy), and a fanned tail.
- Wings: folded along the sides, three tiers of coverts stepping down and back, long primaries sweeping down
  behind the tail.
- Legs: the long legs of the old engravings: fluffy feathered thighs, thin scaled shins, big four-toed feet with
  curved black talons.

Units are pixels, Stolas facing +z with the ground at y = 0 (about 142 pixels to the tufts' tips).

    python tools/stolas.py --bbmodel --preview out.png
"""
from pathlib import Path

import clean_metal
from steampunk_models import box, cyl

ROOT = Path(__file__).resolve().parents[1]
ART = ROOT / "art" / "stolas"

(FEATHER, FEATHER_DARK, BREAST, BREAST_DARK, DISC, RIM, EYE, EYE_DARK, BEAK, SCALE, SCALE_DARK, TALON, GOLD, GOLD_DARK,
 JEWEL, BLACK, WHITE, DOWN) = (
    "st_feather", "st_feather_dark", "st_breast", "st_breast_dark", "st_disc", "st_rim", "st_eye", "st_eye_dark",
    "st_beak", "st_scale", "st_scale_dark", "st_talon", "st_gold", "st_gold_dark", "st_jewel", "st_black", "st_white",
    "st_down")

# Joints, in pixels.
HIP = (0, 52, 0)
NECK = (0, 97.5, 0)
WINGS = {"left_wing": (15, 90, -3), "right_wing": (-15, 90, -3)}
LEGS = {"left_leg": (8, 52, 0), "right_leg": (-8, 52, 0)}
TAIL = (0, 57, -13.5)
REST = {"head": (3, -8, 0), "tail": (-42, 0, 0), "left_wing": (0, 0, 0), "right_wing": (0, 0, 0)}


def tufts(points, size, tex, axis="z"):
    out = []
    for i, (x, y, z) in enumerate(points):
        a = 22.5 if i % 2 else -22.5
        out.append(box((x - size[0] / 2, y - size[1] / 2, z - size[2] / 2), (x + size[0] / 2, y + size[1] / 2, z + size[2] / 2),
                       tex, (axis, a, [x, y, z])))
    return out


def mirrored(elements):
    """The same elements flipped across x = 0 (for the right wing): x extents swapped, y and z turns reversed."""
    out = []
    for item in elements:
        frm, to, texture = item[:3]
        options = dict(item[3]) if len(item) > 3 else {}
        if "rotation" in options:
            axis, angle, origin = options["rotation"][:3]
            options["rotation"] = (axis, angle if axis == "x" else -angle, [-origin[0], origin[1], origin[2]])
        flipped = ([-to[0], frm[1], frm[2]], [-frm[0], to[1], to[2]], texture)
        out.append(flipped + ((options,) if options else ()))
    return out


# ------------------------------------------------------------------ parts (each from its joint)

def head():
    m = []
    m.append(box((-15, 0, -10), (15, 27, 13), FEATHER))
    m.append(box((-13.4, 1.5, -12.6), (13.4, 25.5, -10), FEATHER))
    m.append(box((-11, 3.5, -14), (11, 23.5, -12.6), FEATHER_DARK))
    m.append(box((-13, 27, -9), (13, 29, 11), FEATHER))
    m.append(box((-10, 29, -7), (10, 30.4, 9), FEATHER_DARK))
    m.append(box((-13, -1.5, -8.5), (13, 0, 11), FEATHER_DARK))
    # The facial disc: a pale heart in a dark feathered rim, parted by a dark line from the brow to the beak.
    m.append(box((-14.6, 3, 13), (14.6, 23, 14.2), DISC))
    m.append(box((-15.3, 22.5, 12.8), (15.3, 24.6, 14.5), RIM))
    for x0, x1 in ((-15.3, -13.6), (13.6, 15.3)):
        m.append(box((x0, 2.5, 12.8), (x1, 23, 14.5), RIM))
    m.append(box((-14, 1.6, 12.8), (14, 3.4, 14.5), RIM))
    m += tufts([(x, 1.2, 14.6) for x in (-11, -6.5, -2, 2, 6.5, 11)], (3.6, 2.8, 1.6), RIM)
    m.append(box((-1.3, 13, 14.2), (1.3, 23.2, 14.9), RIM))
    # The eyes: dark rings, amber irises and black pupils, each with a glint.
    for x in (-7.6, 7.6):
        m += cyl("z", x, 15.5, 5.4, 14.2, 14.9, EYE_DARK)
        m += cyl("z", x, 15.5, 4.4, 14.9, 15.7, EYE)
        m += cyl("z", x, 15.5, 2.2, 15.7, 16.3, BLACK)
        m.append(box((x - 2.6, 17, 16.3), (x - 1.2, 18.2, 16.5), WHITE))
    # The beak: a dark hook under the disc's part.
    m.append(box((-2.2, 8.5, 14.2), (2.2, 13.5, 17.2), BEAK))
    m.append(box((-1.5, 5.6, 15.4), (1.5, 8.5, 17.6), BEAK, ("x", 22.5, [0, 8.5, 16.5])))
    m.append(box((-0.9, 4, 16.2), (0.9, 5.8, 17.4), BLACK, ("x", 22.5, [0, 8.5, 16.5])))
    # The ear tufts: two tall tufts at the crown's corners leaning outward, dark with lighter inner feathers.
    for side in (-1, 1):
        x = side * 12
        turn = ("z", -side * 22.5, [x, 26, -2])
        m.append(box((x - 2.6, 26, -4.5), (x + 2.6, 37, 0.5), FEATHER_DARK, turn))
        m.append(box((x - 1.6, 36.5, -3.6), (x + 1.6, 43.5, -0.4), FEATHER_DARK, turn))
        m.append(box((x - 1.0, 43, -3), (x + 1.0, 47, -1), FEATHER_DARK, turn))
        m.append(box((x - 1.8, 28, 0.5), (x + 1.8, 36, 1.3), FEATHER, turn))
    # The crown: a gold band, a dark groove, six points with jewels, and a front plate with a star.
    m.append(box((-15.6, 23.4, -11.2), (15.6, 27.4, 13.6), GOLD))
    m.append(box((-13.8, 23.4, -13.4), (13.8, 27.4, -11.2), GOLD))
    m.append(box((-15.9, 24.6, -11.5), (15.9, 25.6, 13.9), GOLD_DARK))
    m.append(box((-14.1, 24.6, -13.7), (14.1, 25.6, -11.5), GOLD_DARK))
    for x, z, top in ((0, 12.6, 36), (0, -12.2, 33), (13.6, 11.2, 33), (-13.6, 11.2, 33), (12.6, -9.6, 33), (-12.6, -9.6, 33)):
        m.append(box((x - 2, 27.4, z - 1.3), (x + 2, top, z + 1.3), GOLD))
        m.append(box((x - 1.2, top - 0.5, z - 1.8), (x + 1.2, top + 2.2, z + 1.8), JEWEL))
    m.append(box((-6, 27.4, 11.8), (6, 32, 13.4), GOLD))
    m.append(box((-4.8, 28.2, 13.4), (4.8, 31.2, 13.9), GOLD_DARK))
    m.append(box((-0.8, 28.8, 13.9), (0.8, 30.6, 14.3), WHITE))
    m.append(box((-2.2, 29.2, 13.9), (2.2, 30.2, 14.25), WHITE))
    m += cyl("z", 0, 34.5, 1.6, 11.3, 13.9, JEWEL)
    return m


def body():
    m = []
    # The torso: an egg, widest low; barred feathers on the sides and back, a cream breast in rows.
    m.append(box((-13, 3, -11), (13, 41, 11), FEATHER))
    m.append(box((-14.4, 8, -9.5), (14.4, 30, 9.5), FEATHER))
    m.append(box((-11, 0, -9), (11, 3, 9), BREAST))
    m.append(box((-11, 41, -8.5), (11, 43, 8.5), FEATHER))
    rows = ((2, 11.5, 11.4), (6, 13.2, 12.2), (10, 14, 12.6), (14, 14.2, 12.6), (18, 14, 12.4), (22, 13.4, 12.1),
            (26, 12.6, 11.8), (30, 11.6, 11.5), (34, 10.6, 11.3), (38, 9.6, 11.2))
    for i, (y, w, z) in enumerate(rows):
        m.append(box((-w, y - 1, z - 2), (w, y + 3.4, z + 1.2 + 0.1 * (i % 2)), BREAST if i % 2 else BREAST_DARK))
    for i, (y, w, z) in enumerate(rows[1:-1]):
        d = z - 1.6
        m.append(box((-w - 0.6, y - 1, -d - 1.2 - 0.1 * (i % 2)), (w + 0.6, y + 3.4, -d + 2), FEATHER_DARK if i % 2 else FEATHER))
    # The ruff at the neck.
    m += tufts([(x, 40.4, 10) for x in (-9, -4.5, 0, 4.5, 9)], (4.2, 4.2, 3.4), DOWN)
    m += tufts([(x, 40.4, -10) for x in (-8, -2.5, 2.5, 8)], (4.2, 3.8, 3.4), FEATHER_DARK)
    m += tufts([(-11.8, 40.4, y) for y in (-3, 3)] + [(11.8, 40.4, y) for y in (-3, 3)], (3.2, 3.8, 4.2), FEATHER_DARK, "x")
    # The collar: a gold chain round the shoulders and a crescent pendant on the breast.
    m.append(box((-13.8, 34.6, -11.8), (13.8, 36.8, 13.4), GOLD))
    m.append(box((-14.1, 35.2, -12.1), (14.1, 36.2, 13.7), GOLD_DARK))
    m.append(box((-1, 29, 12.8), (1, 34.6, 13.6), GOLD_DARK))
    m += cyl("z", 0, 25, 4.4, 13.2, 14.4, GOLD)
    m += cyl("z", 1.8, 25.8, 3.4, 14.4, 15, DISC)
    m.append(box((-1.4, 23.8, 14.4), (1.4, 26.2, 14.9), JEWEL))
    return m


def wing():
    """The left wing, folded: a scapular block, three tiers of coverts stepping back and down, and four long
    primaries sweeping down behind the tail. Mirrored for the right."""
    m = []
    m.append(box((-0.4, -8, -9), (5, 4, 7), FEATHER))
    m.append(box((0.5, 2, -7), (5.6, 6.5, 5), FEATHER_DARK))
    for r in range(3):
        top = -5 - 9 * r
        for j in range(4):
            z1 = 6 - 5 * j
            length = 10 + 3 * r + 2 * j
            x0 = 1.2 + 0.5 * r + 0.15 * j
            tex = FEATHER_DARK if (r + j) % 2 else FEATHER
            m.append(box((x0, top - length, z1 - 5.4), (x0 + 4, top, z1), tex))
            m.append(box((x0 + 0.1, top - length - 1.2, z1 - 4.4), (x0 + 3.9, top - length, z1 - 1), tex))
    for j in range(4):
        z1 = -12 - 3.5 * j
        length = 22 + 3 * j
        x0 = 2.2 + 0.4 * j
        m.append(box((x0, -30 - length, z1 - 4.6), (x0 + 3.2, -30, z1), FEATHER_DARK if j % 2 else FEATHER,
                     ("x", 22.5, [x0, -30, z1])))
    return m


def tail():
    """The tail: five fanned feathers, barred, hanging from the rump."""
    m = []
    m.append(box((-6, -1, -4), (6, 3, 0.4), FEATHER_DARK))
    for i, angle in enumerate((-45, -22.5, 0, 22.5, 45)):
        tex = FEATHER if i % 2 else FEATHER_DARK
        m.append(box((-2.4, -1.5 - 0.2 * abs(i - 2), -14), (2.4, 1 - 0.2 * abs(i - 2), 0), tex, ("y", angle, [0, 0, -1])))
        m.append(box((-1.8, -1.3 - 0.2 * abs(i - 2), -17.5), (1.8, 0.8 - 0.2 * abs(i - 2), -14), tex, ("y", angle, [0, 0, -1])))
    return m


def leg():
    m = []
    # The feathered thigh and knee, fluffy, then the long scaled shin and the big four-toed foot.
    m.append(box((-5.6, -14, -5.6), (5.6, -0.3, 5.6), BREAST))
    m += tufts([(x, -14.5, 3) for x in (-3.5, 0, 3.5)], (3.6, 4, 3.6), DOWN)
    m += tufts([(x, -14, -3.2) for x in (-3, 3)], (3.6, 4, 3.6), BREAST)
    m.append(box((-3.2, -19, -3.2), (3.2, -14, 3.2), SCALE_DARK))
    m.append(box((-2.5, -45, -2.5), (2.5, -19, 2.5), SCALE))
    for y in (-40, -34, -28, -22):
        m.append(box((-2.7, y, -2.7), (2.7, y + 1.4, 2.7), SCALE_DARK))
    m.append(box((-3.4, -49, -3.4), (3.4, -45, 3.4), SCALE))
    for x, angle in ((-4.6, 22.5), (0, 0), (4.6, -22.5)):
        turn = ("y", angle, [x * 0.4, -50, 0])
        m.append(box((x - 1.6, -52, -0.5), (x + 1.6, -48.4, 12), SCALE, turn))
        m.append(box((x - 1.2, -51.6, 2), (x + 1.2, -48, 8), SCALE_DARK, turn))
        m.append(box((x - 1.1, -52, 12), (x + 1.1, -48.8, 15), TALON, ("x", 22.5, [x, -50, 12])))
    m.append(box((-1.6, -52, -11), (1.6, -48.4, 0.5), SCALE))
    m.append(box((-1.1, -52, -14), (1.1, -48.8, -11), TALON, ("x", -22.5, [0, -50, -11])))
    return m


def shifted(item, offset):
    frm, to, texture = item[:3]
    options = dict(item[3]) if len(item) > 3 else {}
    if "rotation" in options:
        axis, angle, origin = options["rotation"][:3]
        options["rotation"] = (axis, angle, [origin[k] + offset[k] for k in range(3)])
    moved = ([frm[k] + offset[k] for k in range(3)], [to[k] + offset[k] for k in range(3)], texture)
    return moved + ((options,) if options else ())


def groups():
    out = [("body", HIP, None, [shifted(i, HIP) for i in body()]),
           ("head", NECK, REST.get("head"), [shifted(i, NECK) for i in head()], "body"),
           ("tail", TAIL, REST.get("tail"), [shifted(i, TAIL) for i in tail()], "body")]
    for name, joint in WINGS.items():
        items = wing() if "left" in name else mirrored(wing())
        out.append((name, joint, REST.get(name), [shifted(i, joint) for i in items], "body"))
    for name, joint in LEGS.items():
        out.append((name, joint, None, [shifted(i, joint) for i in leg()]))
    return out


def posed():
    parts = groups()
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
    return blockbench_export.write(folder / "stolas.bbmodel", "stolas", groups(), draw=lambda name: TEXTURES[name]())


# ------------------------------------------------------------------ art (flat, clean, 16 x 16)

def plain(fill, light, shade):
    img = clean_metal.canvas(fill)
    clean_metal.bevel(img, 0, 0, 15, 15, light, shade, fill)
    return img


def barred(fill, bar, light):
    """Owl feathers: broken dark bars every four rows with a light fleck between."""
    img = clean_metal.canvas(fill)
    for y in range(16):
        for x in range(16):
            if y % 4 == 1 and (x + y) % 7 not in (3, 4):
                clean_metal.put(img, x, y, bar)
            elif y % 4 == 3 and (x * 3 + y) % 5 == 0:
                clean_metal.put(img, x, y, light)
    return img


def fluff(fill, light, shade):
    img = clean_metal.canvas(fill)
    for y in range(16):
        for x in range(16):
            if (x * 5 + y * 3) % 11 == 0:
                clean_metal.put(img, x, y, light)
            elif (x * 3 + y * 7) % 13 == 0:
                clean_metal.put(img, x, y, shade)
    return img


def scaled(fill, light, shade):
    img = clean_metal.canvas(fill)
    for row in range(4):
        off = 2 if row % 2 else 0
        for col in range(5):
            x0 = col * 4 - off
            for dx in range(4):
                x = x0 + dx
                if 0 <= x < 16:
                    clean_metal.put(img, x, row * 4, light if dx in (1, 2) else fill)
                    clean_metal.put(img, x, row * 4 + 3, shade)
    return img


TEXTURES = {FEATHER: lambda: barred((112, 92, 74), (70, 54, 42), (146, 124, 100)),
            FEATHER_DARK: lambda: barred((82, 66, 52), (48, 36, 28), (108, 90, 72)),
            BREAST: lambda: barred((222, 206, 176), (150, 122, 90), (240, 230, 206)),
            BREAST_DARK: lambda: barred((204, 186, 154), (138, 110, 80), (226, 212, 184)),
            DISC: lambda: fluff((232, 220, 196), (246, 240, 222), (206, 190, 160)),
            RIM: lambda: fluff((58, 44, 36), (82, 66, 54), (36, 26, 22)),
            EYE: lambda: plain((236, 160, 34), (252, 206, 90), (190, 112, 20)),
            EYE_DARK: lambda: plain((46, 34, 30), (66, 52, 46), (26, 18, 16)),
            BEAK: lambda: plain((56, 50, 54), (84, 78, 84), (32, 28, 32)),
            SCALE: lambda: scaled((172, 158, 118), (204, 192, 150), (122, 108, 74)),
            SCALE_DARK: lambda: scaled((136, 122, 88), (166, 152, 114), (92, 80, 54)),
            TALON: lambda: plain((34, 30, 34), (58, 54, 58), (18, 16, 18)),
            GOLD: lambda: plain((226, 182, 70), (248, 216, 124), (168, 124, 38)),
            GOLD_DARK: lambda: plain((168, 124, 38), (198, 152, 58), (118, 84, 26)),
            JEWEL: lambda: plain((118, 52, 160), (164, 96, 204), (76, 28, 108)),
            BLACK: lambda: plain((20, 16, 18), (40, 34, 36), (10, 8, 10)),
            WHITE: lambda: plain((244, 242, 236), (254, 254, 250), (210, 206, 198)),
            DOWN: lambda: fluff((238, 228, 206), (250, 246, 232), (214, 200, 172))}


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
        for yaw, suffix in ((30, "front_left"), (-30, "front_right"), (90, "side"), (-150, "back")):
            box_preview.render(posed(), scale=3, yaw=yaw, pitch=10, draw=draw).save(out.replace(".png", f"_{suffix}.png"))
        print(out)

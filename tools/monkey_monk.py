"""The Monkey Monk: a wandering monkey sage of the old tales, 10 October 2026. A still model, no clips until the owner
asks ("wait to animate until i say").

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
REST = {"left_arm": (-12, 0, 10), "right_arm": (-30, 0, -32), "staff": (30, 0, 32), "head": (3, -6, 0),
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
    """The furred head, the cream muzzle and brow, eyes, nose, mouth, ears, cheek ruffs, crown tuft, the circlet and
    the halo."""
    m = []
    m.append(box((-4, 0, -4), (4, 8, 4), FUR))
    m.append(box((-3.2, 1.2, 4), (3.2, 5.8, 4.5), CREAM))
    m.append(box((-2.4, 0.4, 4.5), (2.4, 3.4, 6.2), CREAM))
    m.append(box((-1.2, 2.6, 6.2), (1.2, 3.4, 6.5), FUR_DARK))
    m.append(box((-1.4, 1.2, 6.2), (1.4, 1.5, 6.4), FUR_DARK))
    m.append(box((-3.4, 5.4, 4), (3.4, 6.2, 4.6), FUR_DARK))
    for x0, x1 in ((-2.9, -1.3), (1.3, 2.9)):
        m.append(box((x0, 4.3, 4.5), (x1, 5.1, 4.7), BLACK))
        m.append(box((x0 + 0.4, 4.6, 4.7), (x0 + 0.9, 5.0, 4.85), CREAM))
    for x in (-5.3, 4.3):
        m.append(box((x, 3, -1.8), (x + 1, 6.4, 1.2), FUR))
        m.append(box((x + (0.3 if x > 0 else 0.3), 3.6, -1.2), (x + 0.7, 5.8, 0.6), CREAM))
    for x0, x1 in ((-5.6, -4), (4, 5.6)):
        m.append(box((x0, 0.5, 1.5), (x1, 4, 4.2), FUR))
        m.append(box((x0 - (0.4 if x0 < 0 else 0), -0.5, 2), (x1 + (0.4 if x0 > 0 else 0), 1, 3.8), FUR_DARK))
    m.append(box((-1.5, 8, -2), (1.5, 10, 0), FUR))
    m.append(box((-0.8, 10, -1.5), (0.8, 11.5, -0.5), FUR_DARK))
    # The circlet: a gold band round the brow with a red jewel and two scrolled ends.
    m.append(box((-4.3, 5.6, -4.3), (4.3, 6.9, 4.3), GOLD))
    m.append(box((-4.5, 6.1, -4.5), (4.5, 6.5, 4.5), GOLD_DARK))
    m.append(box((-1, 5.3, 4.3), (1, 7.3, 4.9), GOLD))
    m.append(box((-0.5, 5.8, 4.9), (0.5, 6.8, 5.3), RED))
    for x in (-3.6, 2.6):
        m.append(box((x, 6.9, 3.2), (x + 1, 8.2, 4.4), GOLD))
    # The halo: a thin gold ring floating behind the head, eight bars turned about its centre, with four small studs.
    for angle in (0, 45):
        m.append(box((-7.6, 11.2, -7.0), (7.6, 12.4, -6.4), GOLD, ("z", angle, [0, 5, -6.7])))
        m.append(box((-7.6, -2.4, -7.0), (7.6, -1.2, -6.4), GOLD, ("z", angle, [0, 5, -6.7])))
        m.append(box((-7.6, -1.2, -7.0), (-6.4, 11.2, -6.4), GOLD, ("z", angle, [0, 5, -6.7])))
        m.append(box((6.4, -1.2, -7.0), (7.6, 11.2, -6.4), GOLD, ("z", angle, [0, 5, -6.7])))
    for x, y in ((0, 12.9), (0, -2.9), (-7.9, 5), (7.9, 5)):
        m.append(box((x - 0.6, y - 0.6, -7.1), (x + 0.6, y + 0.6, -6.3), GOLD_DARK))
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
    m.append(box((-2.1, -12.5, -2.1), (2.1, -7.5, 2.1), FUR))
    m.append(box((-2.4, -14, -2.6), (2.4, -12.5, 2.6), FUR))
    for i, x in enumerate((-1.6, -0.5, 0.6, 1.7)):
        m.append(box((x - 0.5, -14, 2.6), (x + 0.5, -12.8, 5 - 0.5 * abs(i - 1.5)), FUR_DARK))
    m.append(box((-2.9, -14, 0.5), (-2.2, -12.8, 2.8), FUR_DARK))
    return m


def staff():
    """The ruyi staff from the fist: a red iron shaft with gold bands and heavy gold caps."""
    m = []
    m += cyl("y", 0, 0, 0.9, -22, 22, RED)
    for y0, y1 in ((-22.5, -18.5), (18.5, 22.5)):
        m += cyl("y", 0, 0, 1.3, y0, y1, GOLD)
    for y in (-10, -3, 4, 11):
        m += cyl("y", 0, 0, 1.1, y, y + 1.2, GOLD_DARK)
    for y0, y1 in ((-23.5, -22.5), (22.5, 23.5)):
        m += cyl("y", 0, 0, 1.0, y0, y1, GOLD_DARK)
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
    monk = blockbench_export.write(folder / "monkey_monk.bbmodel", "monkey_monk", groups(False),
                                   draw=lambda name: TEXTURES[name]())
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
        print(out)

"""Sprites of the Arms VI kit (batch 55, docs/features/arms-vi.md): the longbows and arbalests, drawn as vanilla's bow
and crossbow are (at rest and drawn in three steps; a crossbow also loaded), and the shields' painted faces, backs and
metal trim, which tools/arms_kit.py maps onto their 3D models.

After the owner's reference sheets of an iron-and-wood war kit (studied for the look; nothing is copied). As the other
arms: bronze is warm and steampunk (oiled oak, brass, crimson paint), steel kaiserpunk (dark wood, blued steel,
gunmetal, olive paint, docs/ART_DIRECTION.md). All original.
"""
import math
import random

from PIL import Image

import arms
import arms_art
import arms_kit
import hd_art as hd
from hd_art import Material

CRIMSON = Material([(52, 8, 10), (88, 16, 18), (128, 26, 26), (164, 40, 36), (196, 64, 52), (226, 104, 86)], 0.1, 6)
NAVY = Material([(10, 14, 24), (18, 24, 40), (28, 38, 60), (40, 54, 82), (58, 76, 108), (86, 106, 138)], 0.1, 6)
OLIVE = hd.OLIVE
GOLD = hd.BRASS


def _tone(material, value):
    """A ramp colour for a brightness from 0 to 1."""
    ramp = material.ramp
    return ramp[max(0, min(len(ramp) - 1, int(value * len(ramp))))]


def _put(img, x, y, colour, alpha=255):
    if 0 <= x < img.width and 0 <= y < img.height:
        img.putpixel((x, y), tuple(colour[:3]) + (alpha,))


def _metal(style):
    """The trim's metal: bronze's blade bronze, steel's blued steel."""
    return style.blade


def _boards(img, material, rng, width, x0=0, y0=0, x1=None, y1=None, vertical=True):
    """Planks with a grain, a dark seam between each, lit a little from the top left."""
    x1 = img.width if x1 is None else x1
    y1 = img.height if y1 is None else y1
    shift = [rng.uniform(-0.08, 0.08) for _ in range(64)]
    for y in range(y0, y1):
        for x in range(x0, x1):
            along, across = (y, x - x0) if vertical else (x, y - y0)
            board = across // width
            edge = across % width
            grain = 0.06 * math.sin(along * 0.45 + board * 2.1 + math.sin(along * 0.11 + board) * 3.0)
            value = 0.55 + shift[board % 64] + grain - 0.12 * (along / max(1, (y1 if vertical else x1))) + rng.uniform(-0.03, 0.03)
            if edge == 0:
                value = 0.12
            elif edge == 1:
                value += 0.08
            _put(img, x, y, _tone(material, value))


def _rivet(img, x, y, material, big=False):
    """A domed rivet head: lit at its top left, shadowed below."""
    _put(img, x, y, material.ramp[-2])
    _put(img, x - 1, y - 1, material.ramp[-1]) if big else None
    _put(img, x + 1, y, material.ramp[2])
    _put(img, x, y + 1, material.ramp[1])
    _put(img, x + 1, y + 1, (12, 10, 10))
    if big:
        _put(img, x - 1, y, material.ramp[-2])
        _put(img, x, y - 1, material.ramp[-1])


def _wear(img, rng, count, bare, length=(2, 6)):
    """Scratches through the paint, to the wood or metal beneath."""
    for _ in range(count):
        x, y = rng.uniform(0, img.width), rng.uniform(0, img.height)
        angle = rng.uniform(0, math.pi)
        for step in range(rng.randint(*length)):
            px, py = int(x + math.cos(angle) * step), int(y + math.sin(angle) * step)
            if 0 <= px < img.width and 0 <= py < img.height and img.getpixel((px, py))[3]:
                _put(img, px, py, bare)


def _vignette(img, strength=0.35):
    """Darkens the face towards its edges, so the painted board reads as curved under the rim."""
    cx, cy = img.width / 2, img.height / 2
    pixels = img.load()
    for y in range(img.height):
        for x in range(img.width):
            r, g, b, a = pixels[x, y]
            d = max(abs(x + 0.5 - cx) / cx, abs(y + 0.5 - cy) / cy)
            k = 1.0 - strength * max(0.0, d - 0.6) / 0.4
            pixels[x, y] = (int(r * k), int(g * k), int(b * k), a)


def _face_size(kind):
    """The painted part of a shield's 64x64 face texture: the plate at arms_kit.face_scale, along the left side."""
    px0, py0, px1, py1 = arms_kit.PLATE[kind]
    k = arms_kit.face_scale(kind)
    return int(round((px1 - px0) * k)), int(round((py0 - py1) * k))


def _model(kind, x, y):
    """A face pixel's centre in model pixels."""
    px0, py0, _px1, _py1 = arms_kit.PLATE[kind]
    k = arms_kit.face_scale(kind)
    return px0 + (x + 0.5) / k, py0 - (y + 0.5) / k


def _on_face(kind, painted):
    """The painted plate on a 64x64 face texture, at its top left."""
    img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    img.paste(painted, (0, 0))
    return img


def heater_face(metal):
    """A heater shield's face. Bronze: crimson, a gold chevron and a gold jug in chief (Jugcraft's own charge). Steel:
    navy, a riveted steel bend and a pale-green gauge-light roundel."""
    style = arms_art.STYLES[metal]
    width, height = _face_size("heater_shield")
    img = Image.new("RGBA", (width, height), (0, 0, 0, 0))
    rng = random.Random(f"heater_{metal}")
    field = CRIMSON if metal == "bronze" else NAVY
    for y in range(height):
        for x in range(width):
            grain = 0.03 * math.sin(y * 0.5 + math.sin(x * 0.3) * 2)
            _put(img, x, y, _tone(field, 0.52 + grain - 0.1 * y / height + rng.uniform(-0.04, 0.04)))
    if metal == "bronze":
        # The chevron: a band rising from the flanks to a peak at the middle.
        for y in range(height):
            for x in range(width):
                mx, my = _model("heater_shield", x, y)
                peak = 11.5 - abs(mx - 8.0) * 1.15
                if peak - 2.2 <= my <= peak:
                    lit = 0.75 if my > peak - 0.6 else (0.35 if my < peak - 1.7 else 0.6)
                    _put(img, x, y, _tone(GOLD, lit + rng.uniform(-0.04, 0.04)))
        # A jug in chief: a round belly, a neck and a handle, in gold, in model pixels about (8, 15).
        for y in range(height):
            for x in range(width):
                mx, my = _model("heater_shield", x, y)
                dx, dy = mx - 8.0, 15.0 - my
                belly = (dx / 2.3) ** 2 + ((dy - 0.6) / 2.0) ** 2 <= 1.0
                neck = abs(dx) <= 0.8 and -2.6 <= dy <= -1.0
                lip = abs(dx) <= 1.15 and -3.1 <= dy <= -2.6
                handle = dx > 1.9 and 0.9 <= math.hypot(dx - 2.0, dy + 0.4) <= 1.5
                if belly or neck or lip or handle:
                    lit = 0.82 if dx < -0.5 and dy < 0.6 else (0.4 if dx > 1.0 or dy > 1.6 else 0.62)
                    _put(img, x, y, _tone(GOLD, lit))
    else:
        # The bend: a riveted steel band from the top left to the bottom right, through the middle. Along it is
        # (0.8, -0.6) in model pixels, across it (0.6, 0.8), from the plate's top-left corner.
        px0, py0 = arms_kit.PLATE["heater_shield"][:2]
        for y in range(height):
            for x in range(width):
                mx, my = _model("heater_shield", x, y)
                across = (mx - px0) * 0.6 + (my - py0) * 0.8
                if -5.2 <= across <= -2.0:
                    lit = 0.78 if across > -2.5 else (0.3 if across < -4.7 else 0.55)
                    _put(img, x, y, _tone(style.blade, lit))
        for step in range(12):
            along = 1.5 + step * 2.2
            mx, my = px0 + 0.6 * -3.6 + 0.8 * along, py0 + 0.8 * -3.6 - 0.6 * along
            k = arms_kit.face_scale("heater_shield")
            x, y = int((mx - px0) * k), int((py0 - my) * k)
            if 2 <= x < width - 2 and 2 <= y < height - 2:
                _rivet(img, x, y, hd.BRASS)
        # A roundel in the upper right, clear of the bend (model pixels about (11, 15.5)): a lit green gauge light in a
        # gunmetal bezel.
        for y in range(height):
            for x in range(width):
                mx, my = _model("heater_shield", x, y)
                d = math.hypot(mx - 11.0, my - 15.5)
                if d <= 1.9:
                    _put(img, x, y, _tone(hd.GUNMETAL, 0.7 if (mx - 11.0) - (my - 15.5) < 0 else 0.35) if d > 1.35
                         else _tone(arms_art.PHOSPHOR, 0.85 - d / 2.6))
    _wear(img, rng, 18, (style.haft.ramp[3] if metal == "bronze" else style.blade.ramp[3]))
    _vignette(img)
    return _on_face("heater_shield", img)


def tower_face(metal):
    """A tower shield's face. Bronze: oak boards bound with three bronze bands. Steel: olive-painted riveted plates with a
    vision slit near the top, the paint worn to the steel at the edges."""
    style = arms_art.STYLES[metal]
    width, height = _face_size("tower_shield")
    img = Image.new("RGBA", (width, height), (0, 0, 0, 0))
    rng = random.Random(f"tower_{metal}")
    if metal == "bronze":
        _boards(img, style.haft, rng, 9)
        for band in (0.16, 0.5, 0.84):
            y0 = int(height * band) - 3
            for y in range(y0, y0 + 6):
                for x in range(width):
                    lit = 0.8 if y == y0 else (0.28 if y == y0 + 5 else 0.55)
                    _put(img, x, y, _tone(_metal(style), lit + rng.uniform(-0.03, 0.03)))
            for x in range(4, width - 2, 9):
                _rivet(img, x, y0 + 2, GOLD, big=True)
    else:
        for y in range(height):
            for x in range(width):
                _put(img, x, y, _tone(OLIVE, 0.5 - 0.12 * y / height + rng.uniform(-0.03, 0.03)))
        seams = [int(height * f) for f in (0.3, 0.62)]
        for seam in seams:
            for x in range(width):
                _put(img, x, seam, OLIVE.ramp[0])
                _put(img, x, seam + 1, OLIVE.ramp[4])
            for x in range(3, width - 1, 6):
                _rivet(img, x, seam - 3, OLIVE)
                _rivet(img, x, seam + 4, OLIVE)
        # The vision slit: dark, with a lit lower lip.
        sy = int(height * 0.14)
        for y in range(sy, sy + 3):
            for x in range(12, width - 12):
                _put(img, x, y, (8, 8, 10))
        for x in range(11, width - 11):
            _put(img, x, sy - 1, OLIVE.ramp[1])
            _put(img, x, sy + 3, OLIVE.ramp[5])
        # A stencilled number in pale paint, low on the board.
        for i, (dx, dy) in enumerate([(0, 0), (1, 0), (2, 0), (2, 1), (2, 2), (1, 2), (0, 2), (0, 3), (0, 4), (1, 4), (2, 4),
                                      (5, 0), (5, 1), (5, 2), (5, 3), (5, 4)]):
            for sx in range(2):
                for sy2 in range(2):
                    _put(img, width // 2 - 6 + dx * 2 + sx, int(height * 0.78) + dy * 2 + sy2, hd.WHITE_PAINT.ramp[2])
        _wear(img, rng, 28, style.blade.ramp[3])
    _vignette(img, 0.3)
    return _on_face("tower_shield", img)


def back(metal):
    """A shield's back: bare boards (dark for steel), a leather strap across their middle (STRAP_UV)."""
    style = arms_art.STYLES[metal]
    img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    rng = random.Random(f"back_{metal}")
    _boards(img, style.haft, rng, 8)
    u0, v0, u1, v1 = arms_kit.STRAP_UV
    strap = arms_art.LEATHER if metal == "bronze" else hd.RUBBER
    for y in range(int(v0 * 4) - 2, int(v1 * 4) + 2):
        for x in range(64):
            edge = y in (int(v0 * 4) - 2, int(v1 * 4) + 1)
            _put(img, x, y, _tone(strap, 0.2 if edge else 0.5 + 0.08 * math.sin(x * 0.7) + rng.uniform(-0.04, 0.04)))
    for x in (10, 32, 54):
        _rivet(img, x, int(v0 * 4) + 3, style.fitting, big=True)
    return img


def trim(metal):
    """A shield's metal: the rim band along the top (RIM_UV), the boss's face at the bottom left (BOSS_UV), plain metal
    at the bottom right (PLAIN_UV)."""
    style = arms_art.STYLES[metal]
    metal_material = _metal(style)
    img = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    rng = random.Random(f"trim_{metal}")
    for y in range(16):
        for x in range(32):
            lit = (0.85, 0.65, 0.5, 0.3)[min(3, y)] if y < 4 else 0.5
            _put(img, x, y, _tone(metal_material, lit + rng.uniform(-0.04, 0.04)))
    for x in range(3, 32, 8):
        _rivet(img, x, 1, style.fitting)
    canvas = hd.Canvas(16)
    canvas.disc((8.0, 8.0), 7.6, metal_material, dome=0.85)
    canvas.ring((8.0, 8.0), 7.9, 6.0, style.fitting)
    canvas.disc((8.0, 8.0), 2.2, style.fitting, dome=1.0)
    img.paste(canvas.img, (0, 16))
    for y in range(16, 32):
        for x in range(16, 32):
            _put(img, x, y, _tone(metal_material, 0.5 + rng.uniform(-0.04, 0.04)))
    return img


# ---------------------------------------------------------------- bows and crossbows
#
# Drawn as vanilla 26.3 draws its bow and crossbow (read in game), so vanilla's hand poses hold them: a bow's limbs run
# from the top right to the bottom left with its grip at the centre and its arrow pointing to the top left; a crossbow's
# stock runs from its butt at the bottom right to its prod at the top left. Each in its own frame (arms_art.Axis): for
# the bow, s along the limbs and t forward, the way the arrow flies; for the crossbow, s along the stock towards the prod
# and t across it.
BOW_AXIS = {"origin": (32.0, 32.0), "angle": -45.0}
CROSSBOW_AXIS = {"origin": (55.0, 55.0), "angle": -135.0, "scale": 1.12}
# How far back the string is drawn in each of the three steps, and where a crossbow's nut holds it once loaded.
BOW_DRAW = (7.0, 12.0, 17.0)
CROSSBOW_PULL = (5.0, 10.0, 15.0)
STRING = (226, 222, 204)


def _limb(c, w, s0, s1, tip_t, grip_t, width, material, tint=0.0):
    """A bow limb from the grip (s0) to a tip (s1), curving back from grip_t to tip_t, tapering as it goes."""
    steps = 10
    for i in range(steps):
        f0, f1 = i / steps, (i + 1) / steps
        a = w(s0 + (s1 - s0) * f0, grip_t + (tip_t - grip_t) * f0 ** 1.6)
        b = w(s0 + (s1 - s0) * f1, grip_t + (tip_t - grip_t) * f1 ** 1.6)
        c.capsule(a, b, w.r(width * (1.0 - 0.45 * f0)), material, tint=tint)


def _arrow(c, w, t0, t1, style, s=0.0):
    """An arrow along t from its nock (t0) to its head (t1): a shaft, three vanes and a steel head."""
    c.capsule(w(s, t0), w(s, t1 - 3.0), w.r(0.75), arms_art.WOOD, tint=0.1)
    for side in (1.6, -1.6):
        arms_art.flat(c, [w(s, t0 + 0.5), w(s + side, t0 + 1.5), w(s + side, t0 + 6.0), w(s, t0 + 7.5)], (196, 40, 40))
    arms_art.flat(c, [w(s - 1.8, t1 - 4.0), w(s, t1), w(s + 1.8, t1 - 4.0), w(s, t1 - 3.0)], style.blade.ramp[4])


def longbow(metal, draw=None):
    """A longbow at rest (draw None) or drawn back `draw` pixels with an arrow nocked. Bronze: yew with brass nocks and a
    leather grip. Steel: dark wood backed with blued steel, a gunmetal riser and a rubber grip."""
    style = arms_art.STYLES[metal]
    c = hd.Canvas()
    w = arms_art.Axis(**BOW_AXIS)
    length = 29.0
    bend = 6.0 + (draw or 0.0) * 0.22
    limb = arms_art.WOOD if metal == "bronze" else arms_art.DARK_WOOD
    tips = []
    for side in (1, -1):
        _limb(c, w, 0.0, side * length, -bend, 0.0, 2.1, limb)
        if metal == "steel":
            _limb(c, w, 0.0, side * (length - 4.0), -bend + 1.0, 1.0, 0.8, style.blade, tint=0.1)
        tip = w(side * length, -bend)
        tips.append(tip)
        c.disc(tip, w.r(1.5), style.fitting)
    # The string: straight between the tips at rest; drawn back to the nock in a V.
    nock = w(0.0, -bend - (draw or 0.0) - 1.0)
    if draw is None:
        c.line(tips[0], tips[1], STRING, 1.0)
    else:
        c.line(tips[0], nock, STRING, 1.0)
        c.line(tips[1], nock, STRING, 1.0)
    # The grip and riser over the middle.
    if metal == "steel":
        c.box(w(0.0, 0.6), w.r(1.8), w.r(7.5), w.angle + 90, hd.GUNMETAL, bevel=1.0)
    c.capsule(w(-4.5, 0.0), w(4.5, 0.0), w.r(2.0), style.grip, bands=[(f, f + 0.08, style.fitting) for f in (0.15, 0.5, 0.85)])
    if draw is not None:
        _arrow(c, w, -bend - draw - 1.0, -bend - draw + 34.0, style)
    c.grip = w(0.0)
    c.finish()
    return c.img


def arbalest(metal, pull=None, load=None):
    """An arbalest: a wooden stock with a metal prod across its nose and a stirrup to hold it down, at rest (pull and load
    None), wound back `pull` pixels, or loaded with an arrow or a firework. Steel's carries a cranequin's geared rack."""
    style = arms_art.STYLES[metal]
    c = hd.Canvas()
    w = arms_art.Axis(**CROSSBOW_AXIS)
    stock = arms_art.WOOD if metal == "bronze" else arms_art.DARK_WOOD
    nose, nut = 46.0, 30.0
    # The stock: butt, then the tiller running up to the nose.
    c.box(w(5.0), w.r(4.6), w.r(3.2), w.angle, stock, bevel=1.5)
    c.capsule(w(6.0), w(nose + 2.0), w.r(2.3), stock)
    c.capsule(w(14.0, -2.2), w(19.0, -5.0), w.r(0.9), style.fitting)  # the trigger lever
    c.disc(w(nut), w.r(1.8), style.fitting)  # the nut
    if metal == "steel":
        c.capsule(w(12.0, 2.6), w(nut - 2.0, 2.6), w.r(1.1), hd.GUNMETAL, bands=[(i / 8, i / 8 + 0.04, style.fitting) for i in range(1, 8)])
    # The prod: a metal bow across the nose, its arms swept back towards the butt, bending further as it is wound.
    flex = (pull or (nose - nut if load else 0.0)) * 0.12
    tips = []
    for side in (1, -1):
        steps = 8
        for i in range(steps):
            f0, f1 = i / steps, (i + 1) / steps
            a = w(nose - (4.0 + flex) * f0 ** 1.5, side * 17.0 * f0)
            b = w(nose - (4.0 + flex) * f1 ** 1.5, side * 17.0 * f1)
            c.capsule(a, b, w.r(1.7 * (1.0 - 0.35 * f0)), style.blade)
        tip = w(nose - 4.0 - flex, side * 17.0)
        tips.append(tip)
        c.disc(tip, w.r(1.2), style.fitting)
    c.capsule(w(nose + 1.0, -2.6), w(nose + 4.5, -2.6), w.r(0.8), style.fitting)  # the stirrup
    c.capsule(w(nose + 1.0, 2.6), w(nose + 4.5, 2.6), w.r(0.8), style.fitting)
    c.capsule(w(nose + 4.5, -2.6), w(nose + 4.5, 2.6), w.r(0.8), style.fitting)
    # The string: across the prod at rest, drawn back towards the nut as it is wound, caught on the nut once loaded.
    back = nut if load else (nose - 4.0 - flex - (pull or 0.0))
    string = w(back)
    c.line(tips[0], string, STRING, 1.0)
    c.line(tips[1], string, STRING, 1.0)
    if load == "arrow":
        c.capsule(w(nut, 0.0), w(nose + 8.0, 0.0), w.r(0.75), arms_art.WOOD, tint=0.1)
        arms_art.flat(c, [w(nose + 6.5, -1.8), w(nose + 11.0, 0.0), w(nose + 6.5, 1.8), w(nose + 7.5, 0.0)], style.blade.ramp[4])
        for side in (1.6, -1.6):
            arms_art.flat(c, [w(nut + 0.5, 0.0), w(nut + 1.5, side), w(nut + 6.0, side), w(nut + 7.5, 0.0)], (196, 40, 40))
    elif load == "firework":
        c.capsule(w(nut + 1.0, 0.0), w(nose + 4.0, 0.0), w.r(2.2), hd.RED, bands=[(0.45, 0.6, hd.WHITE_PAINT)])
        arms_art.flat(c, [w(nose + 3.5, -2.2), w(nose + 8.5, 0.0), w(nose + 3.5, 2.2)], hd.WHITE_PAINT.ramp[3])
    c.grip = w(10.0)
    c.finish()
    return c.img


def bow_sprites(metal):
    return {"": longbow(metal), **{f"_pulling_{i}": longbow(metal, draw) for i, draw in enumerate(BOW_DRAW)}}


def crossbow_sprites(metal):
    return {"_standby": arbalest(metal), **{f"_pulling_{i}": arbalest(metal, pull) for i, pull in enumerate(CROSSBOW_PULL)},
            "_arrow": arbalest(metal, load="arrow"), "_firework": arbalest(metal, load="firework")}


RANGED_ART = {"bow": bow_sprites, "crossbow": crossbow_sprites}


SHIELD_FACES = {"heater_shield": heater_face, "tower_shield": tower_face}


def draw_all(save):
    for item in arms.kit():
        metal, kind = arms.split(item)
        if kind in arms.SHIELD_KINDS:
            save(SHIELD_FACES[kind](metal), "item", f"{item}_face")
            save(back(metal), "item", f"{item}_back")
            save(trim(metal), "item", f"{item}_trim")
        else:
            for suffix, img in RANGED_ART[arms.RANGED_KINDS[kind]["type"]](metal).items():
                save(img, "item", f"{item}{suffix}")

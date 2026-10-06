"""Ores, storage blocks and metal items in the manner of the vanilla ones (docs/ART_DIRECTION.md, "Each material looks
like its vanilla counterpart"; the owner's 6 October 2026 note: ore stone that matches vanilla stone and deepslate,
storage blocks like the vanilla metal blocks, ingots in the vanilla ingot's shape in each metal's own colours).

Every pixel comes from code here: no Mojang texture is read, traced or recoloured. What follows the vanilla textures
is the manner only:
- **Ore grounds** in plain, neutral stone grey (one ground tone with soft clumps a tone either side and a few short
  cracks) and in dark deepslate (a cooler grey in horizontal layers), so an ore sits in a stone or deepslate wall
  without a seam.
- **Ore** as five to seven small nodules, each lit on its upper left, shaded on its lower right, with a highlight on
  the bigger ones and darker stone under it.
- **Storage blocks** as one bright plate with a lit top and left edge, a shaded bottom and right edge and two faint
  seams, the same pattern for every metal and only the colours changed.
- **Items** on the diagonal, lit from the top left, outlined in each part's own dark tone (docs/ITEM_ICONS.md): the
  ingot a long bar with a lit top face over a darker side, the nugget three little lumps, raw ore a lumpy chunk.

Palettes run darkest first. Metal palettes have five tones; ore tones have four (dark, mid, light, highlight).
"""
import math
import random

from PIL import Image

import block_style as bs

# Neutral stone grey and darker, slightly cool deepslate, each with one ground tone (the middle one).
STONE = [(96, 96, 96), (110, 110, 110), (124, 124, 124), (137, 137, 137), (151, 151, 151)]
DEEPSLATE = [(44, 44, 50), (57, 57, 63), (71, 71, 77), (85, 85, 91), (100, 100, 106)]


def new():
    return Image.new("RGBA", (16, 16), (0, 0, 0, 0))


def _put(img, x, y, c):
    if 0 <= x < 16 and 0 <= y < 16:
        img.putpixel((x, y), tuple(c[:3]) + (255,))


# ---------------------------------------------------------------- ore grounds

def stone(seed, palette=STONE):
    """Plain stone: the ground tone with soft clumps one tone lighter and darker, a few lighter flecks and short dark
    cracks running roughly across."""
    img = new()
    g = bs.grain(16, 16, seed, 2.0, 5.0, 0.6)
    for y in range(16):
        for x in range(16):
            v = g(x, y)
            _put(img, x, y, palette[2 + (1 if v > 0.63 else -1 if v < 0.37 else 0)])
    rng = random.Random(seed + 11)
    for _ in range(3):
        x, y = rng.randrange(16), rng.randrange(16)
        for i in range(rng.randint(2, 3)):
            img.putpixel(((x + i) % 16, (y + (1 if i == 2 else 0)) % 16), palette[0] + (255,))
    for _ in range(4):
        img.putpixel((rng.randrange(16), rng.randrange(16)), palette[4] + (255,))
    return img


def deepslate(seed, palette=DEEPSLATE):
    """Deepslate: darker and cooler, in horizontal layers: long soft bands a tone apart, with short dark seams and a
    lighter edge above some of them."""
    img = new()
    bands = bs.Field(16, 16, seed, 8.0, 2.0)
    fine = bs.Field(16, 16, seed + 5, 2.0, 1.0)
    for y in range(16):
        for x in range(16):
            v = 0.7 * bands(x, y) + 0.3 * fine(x, y)
            _put(img, x, y, palette[2 + (1 if v > 0.62 else -1 if v < 0.38 else 0)])
    rng = random.Random(seed + 13)
    for _ in range(5):
        x, y, length = rng.randrange(16), rng.randrange(16), rng.randint(3, 5)
        for i in range(length):
            img.putpixel(((x + i) % 16, y), palette[0] + (255,))
            if rng.random() < 0.5:
                img.putpixel(((x + i) % 16, (y - 1) % 16), palette[3] + (255,))
    return img


# ---------------------------------------------------------------- ore

# Nodule shapes, as rows of '#': small, chunky and irregular, never round.
NODULES = [
    [".##.", "####", ".##."],
    ["##.", "###", ".##"],
    [".##", "###", "##."],
    ["###", "###"],
    ["##.", "###", ".#."],
    [".#.", "###", "##."],
    ["##..", "####", ".##."],
    [".##", "###", ".##"],
]


def _layout(seed, count):
    """`count` nodules on a jittered three-by-three grid of cells, so they spread evenly and never touch."""
    rng = random.Random(seed)
    cells = [(cx, cy) for cy in range(3) for cx in range(3)]
    rng.shuffle(cells)
    out = []
    for cx, cy in sorted(cells[:count]):
        shape = NODULES[rng.randrange(len(NODULES))]
        h, w = len(shape), len(shape[0])
        x0 = 1 + cx * 5 + rng.randint(0, max(0, 4 - w))
        y0 = 1 + cy * 5 + rng.randint(0, max(0, 4 - h))
        out.append((x0, y0, shape))
    return out


def nodules(img, tones, seed, count=6, ground=None):
    """Draws ore nodules over `img`: `tones` is (dark, mid, light, highlight); `ground` the stone palette, whose
    darkest tone shades the stone just below and to the right of each nodule."""
    dark, mid, light, high = tones
    for x0, y0, shape in _layout(seed, count):
        cells = {(x0 + i, y0 + j) for j, row in enumerate(shape) for i, ch in enumerate(row) if ch == "#"}
        if ground:
            for x, y in cells:
                for nx, ny in ((x + 1, y), (x, y + 1), (x + 1, y + 1)):
                    if (nx, ny) not in cells:
                        _put(img, nx, ny, ground[0])
        top_left = min(cells, key=lambda c: (c[0] + c[1], c[1]))
        for x, y in cells:
            lit = (x - 1, y) not in cells and (x, y - 1) not in cells
            shaded = (x + 1, y) not in cells and (x, y + 1) not in cells
            c = light if lit else dark if shaded else mid
            if (x, y) == top_left and len(cells) >= 4:
                c = high
            _put(img, x, y, c)
    return img


def ore(tones, seed, deep=False, count=6):
    """An ore block: nodules in `tones` over stone, or over deepslate when `deep`."""
    ground = DEEPSLATE if deep else STONE
    base = deepslate(seed) if deep else stone(seed)
    return nodules(base, tones, seed + 3, count, ground)


# ---------------------------------------------------------------- storage blocks

def storage_block(palette, seed=0):
    """A block of metal: one bright plate (the fourth tone) lit along its top and left edge and shaded along its
    bottom and right one, with soft clumps a tone down towards the lower right and short diagonal sheens.
    Every metal shares the pattern; `seed` only moves the clumps."""
    dark, low, mid, face, light = palette
    img = new()
    g = bs.grain(16, 16, seed, 3.0, 6.0, 0.5)
    for y in range(16):
        for x in range(16):
            _put(img, x, y, mid if g(x, y) < 0.34 and x + y > 12 else face)
    for i, j in ((2, 5), (3, 4), (4, 3), (5, 2), (2, 8), (3, 7), (4, 6)):
        _put(img, i, j, light)
    for i, j in ((13, 10), (12, 11), (11, 12), (10, 13), (13, 7), (12, 8)):
        _put(img, i, j, mid)
    for i in range(16):
        _put(img, i, 0, light)
        _put(img, 0, i, light)
        _put(img, i, 15, low)
        _put(img, 15, i, low)
        if 0 < i < 15:
            _put(img, i, 14, mid)
            _put(img, 14, i, mid)
    _put(img, 15, 0, mid)
    _put(img, 0, 15, mid)
    _put(img, 15, 15, dark)
    return img


def mineral_block(palette, seed):
    """A block of a mineral (salt, phosphate, lepidolite, monazite): a clumped crystalline surface of its tones with a
    few bright facets, as the vanilla mineral blocks."""
    img = new()
    s = bs.surface(palette, seed, spread=0.7)
    for y in range(16):
        for x in range(16):
            _put(img, x, y, s(x, y))
    light = max(palette, key=bs._luma)
    rng = random.Random(seed + 9)
    for _ in range(6):
        x, y = rng.randrange(15), rng.randrange(15)
        _put(img, x, y, light)
        _put(img, x + 1, y, light)
    return img


def raw_block(tones, seed):
    """A block of raw ore: big rounded lumps of the raw ore packed together, each lit on its upper left and shaded on
    its lower right, with dark joins between them."""
    dark, mid, light, high = tones
    joint = tuple(max(0, int(v * 0.7)) for v in dark)
    img = new()
    pts = bs._cells(16, 16, seed, 7)
    for y in range(16):
        for x in range(16):
            i, d1, d2 = bs._nearest(pts, x + 0.5, y + 0.5, 16, 16)
            if d2 - d1 < 0.9:
                _put(img, x, y, joint)
                continue
            px, py = pts[i]
            dx = ((x + 0.5 - px + 8) % 16) - 8
            dy = ((y + 0.5 - py + 8) % 16) - 8
            edge = d2 - d1 < 2.2
            c = mid
            if dx + dy < -1.5:
                c = light
            elif dx + dy > 1.5 and edge:
                c = dark
            if -2.6 < dx + dy < -1.6 and abs(dx - dy) < 1.2:
                c = high
            _put(img, x, y, c)
    return img


# ---------------------------------------------------------------- items

def lumps(circles, palette, outline=True):
    """Rounded lumps (cx, cy, r), later ones in front: each shaded by its own centre (lit at the upper left), the
    joins between lumps one tone down, the whole outlined in the darkest tone. `palette` has five tones."""
    owner = {}
    for index, (cx, cy, r) in enumerate(circles):
        for y in range(16):
            for x in range(16):
                if (x + 0.5 - cx) ** 2 + (y + 0.5 - cy) ** 2 <= r * r:
                    owner[(x, y)] = index
    img = new()
    for (x, y), index in owner.items():
        cx, cy, r = circles[index]
        d = (x + 0.5 - cx) + (y + 0.5 - cy)
        k = 3 if d < -r * 0.55 else 1 if d > r * 0.6 else 2
        if any(owner.get(n, index) > index for n in ((x + 1, y), (x, y + 1))):
            k = 1
        if outline and any(n not in owner for n in ((x - 1, y), (x + 1, y), (x, y - 1), (x, y + 1))):
            k = 0
        _put(img, x, y, palette[k])
    for cx, cy, r in circles:
        if r >= 1.9:
            hx, hy = int(cx - r * 0.45), int(cy - r * 0.45)
            if owner.get((hx, hy)) is not None and owner.get((hx - 1, hy)) is not None and owner.get((hx, hy - 1)) is not None:
                _put(img, hx, hy, palette[4])
    return img


RAW_LUMPS = [(5.8, 10.2, 3.3), (10.6, 6.6, 3.1), (6.6, 5.6, 2.5), (10.6, 11.0, 2.6), (3.4, 12.2, 1.6)]
NUGGET_LUMPS = [(6.2, 10.0, 2.6), (9.8, 9.8, 2.6), (8.0, 7.0, 2.6)]


def raw_item(tones, outline=None):
    """Raw ore, as the vanilla raw metals: a lumpy chunk of four rounded lumps."""
    dark, mid, light, high = tones
    edge = outline or tuple(max(0, int(v * 0.6)) for v in dark)
    return lumps(RAW_LUMPS, [edge, dark, mid, light, high])


def nugget(palette):
    """A nugget: three little lumps of the metal, outlined in its darkest tone."""
    return lumps(NUGGET_LUMPS, palette)


# The ingot, drawn for Jugcraft: a long bar lying on the diagonal from lower left to upper right, its top face (4 lit
# edge, 3) towards the upper left over a darker side (2, 1) towards the lower right, its ends cut square, outlined (0).
INGOT = [
    "................",
    "................",
    "................",
    "................",
    "..........00....",
    ".........0430...",
    "........043330..",
    ".......04333220.",
    "......043332210.",
    ".....043332210..",
    "....043332210...",
    "...043332210....",
    "....0332210.....",
    ".....02210......",
    "......000.......",
    "................",
]


def from_mask(mask, palette):
    img = new()
    for y, row in enumerate(mask):
        for x, ch in enumerate(row):
            if ch.isdigit():
                _put(img, x, y, palette[int(ch)])
    return img


def ingot(palette):
    """An ingot in the shape every metal shares, in this metal's five tones."""
    return from_mask(INGOT, palette)


PILE = [
    "................",
    "................",
    "................",
    "................",
    "................",
    "................",
    "......####......",
    "....########....",
    "...##########...",
    "..############..",
    "..############..",
    ".##############.",
    ".##############.",
    "..############..",
    "................",
    "................",
]
# Grains a tone lighter (+) or darker (-) on a heap, at fixed places so every dust shares one clean pattern.
GRAINS = {(7, 8): 1, (5, 9): 1, (10, 9): -1, (4, 11): 1, (8, 11): -1, (12, 11): -1, (6, 12): -1, (11, 12): -1,
          (9, 7): 1}


def dust(palette):
    """A heap of powder: lit on its upper left, shaded towards its lower right, a few grains a tone apart, outlined
    one tone down from the shade."""
    cells = {(x, y) for y, row in enumerate(PILE) for x, ch in enumerate(row) if ch == "#"}
    img = new()
    for x, y in cells:
        if any(n not in cells for n in ((x - 1, y), (x + 1, y), (x, y - 1), (x, y + 1))):
            _put(img, x, y, palette[1] if (x, y - 1) in cells or x > 7 else palette[2])
            continue
        k = 3 if x + y < 15 else 1 if x - y > 0 or y >= 12 else 2
        k = max(1, min(4, k + GRAINS.get((x, y), 0)))
        _put(img, x, y, palette[k])
    return img


def plate(palette):
    """A flat plate seen face on: outlined, lit along its top and left, shaded along its bottom and right, with two
    soft diagonal sheens."""
    img = new()
    for y in range(3, 13):
        for x in range(2, 14):
            if x in (2, 13) or y in (3, 12):
                c = palette[0]
            elif x == 3 or y == 4:
                c = palette[4]
            elif x == 12 or y == 11:
                c = palette[1]
            elif (x - y) in (2, 3) or (x - y) == 7:
                c = palette[3]
            else:
                c = palette[2]
            _put(img, x, y, c)
    return img


def _gear_cells():
    cells = set()
    for y in range(16):
        for x in range(16):
            dx, dy = x + 0.5 - 8, y + 0.5 - 8
            r = math.hypot(dx, dy)
            a = math.degrees(math.atan2(dy, dx)) % 45
            tooth = a < 13 or a > 32
            if 1.8 < r <= 5.4 or (r <= 7.4 and tooth and r > 1.8):
                cells.add((x, y))
    return cells


def gear(palette):
    """A gear of eight square teeth round an axle hole: outlined, lit on its upper left, shaded on its lower right."""
    cells = _gear_cells()
    img = new()
    for x, y in cells:
        d = (x + 0.5 - 8) + (y + 0.5 - 8)
        if any(n not in cells for n in ((x - 1, y), (x + 1, y), (x, y - 1), (x, y + 1))):
            c = palette[0]
        else:
            c = palette[4] if d < -4 else palette[1] if d > 3 else palette[3] if d < 0 else palette[2]
        _put(img, x, y, c)
    return img


SPOOL = [(150, 112, 70), (112, 80, 48), (78, 54, 32)]


def wire(palette):
    """A spool of wire: two wooden flanges with the wire wound neatly between them and its end trailing off."""
    img = new()
    for y in (2, 3, 12, 13):
        for x in range(3, 13):
            c = SPOOL[0] if y in (2, 12) else SPOOL[1]
            if x in (3, 12):
                c = SPOOL[2]
            _put(img, x, y, c)
    for y in range(4, 12):
        for x in range(4, 12):
            c = palette[3] if y % 2 == 0 else palette[2]
            if x == 4:
                c = palette[4] if y % 2 == 0 else palette[3]
            elif x == 11:
                c = palette[1]
            _put(img, x, y, c)
        _put(img, 3, y, palette[0])
        _put(img, 12, y, palette[0])
    for x, y in ((12, 10), (13, 11), (14, 11), (15, 12)):
        _put(img, x, y, palette[2])
    return img


WATER = [(96, 160, 214), (170, 218, 246)]


def washed(palette):
    """Washed ore: the clean chunk in the metal's own tones, still wet, with three drops of water on it."""
    img = raw_item(palette[1:5], palette[0])
    for x, y in ((6, 6), (10, 9), (5, 11)):
        _put(img, x, y, WATER[0])
        _put(img, x, y - 1, WATER[1])
    return img


def blend(copper, tin):
    """Bronze blend: a heap of copper powder with small clumps of tin powder through it."""
    img = dust(copper)
    for x, y, k in ((6, 8, 3), (7, 8, 2), (10, 10, 2), (4, 11, 3), (5, 11, 2), (9, 12, 1), (12, 11, 2)):
        _put(img, x, y, tin[k])
    return img

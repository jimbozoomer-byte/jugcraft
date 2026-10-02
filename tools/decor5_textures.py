"""Original textures for the fifth batch of Halloween decorations, the harvest party (requires Pillow): the Bobbing
for Apples Tub and its apples, the Pumpkin Crate and its label, the Hay Bale Seat, the Autumn Wreath (leaves, corn and
four colours of mums) and the red, orange and yellow Leaf Piles.

Called from crop_textures.crop_textures(). Every pixel is drawn here by code from fixed seeds; no Mojang texture is
read, traced or recoloured. Block textures are 16x16 and fully opaque.
"""
import math
import random

from crop_textures import Canvas, rgb
from decor_textures import noise
from halloween_textures import shade

STAVE = [rgb("6a4628"), rgb("7c5432"), rgb("8c623c"), rgb("9c7046")]
BAND = [rgb("2c2c32"), rgb("3a3a42"), rgb("4a4a54"), rgb("62626c")]
WATER = [rgb("2c5a9a"), rgb("3a6cb0"), rgb("4c80c4"), rgb("8ab8e8")]
APPLE = [rgb("7a1010"), rgb("a01818"), rgb("c42a20"), rgb("e05a3c")]
STEM = [rgb("3e2a14"), rgb("5a3e1e")]
CRATE = [rgb("a07a4a"), rgb("b48c58"), rgb("c49c66"), rgb("d2ac76")]
POST = [rgb("6a4a2a"), rgb("7a5634"), rgb("8a643e")]
PUMPKIN_PAINT = [rgb("c85a10"), rgb("e8761c")]
STRAW = [rgb("9a7a2a"), rgb("b8963a"), rgb("d0ae4e"), rgb("e4c66a"), rgb("f0d888")]
TWINE = [rgb("6a5030"), rgb("8a6a40"), rgb("a4845a")]
WREATH_LEAVES = [rgb("6a2a10"), rgb("9a3a14"), rgb("c4561a"), rgb("d8822a"), rgb("8a6a1c"), rgb("5a3a14")]
KERNELS = [rgb("7a1e1e"), rgb("c49a2a"), rgb("5a2a6a"), rgb("e0c870"), rgb("a0401a"), rgb("3a2a4a")]
HUSK = [rgb("b8a070"), rgb("cfb888"), rgb("e0cca0")]
MUMS = {"yellow": [rgb("a07a10"), rgb("d0a81c"), rgb("f0d040"), rgb("fff08a")],
        "orange": [rgb("a04a0a"), rgb("d06a14"), rgb("f08c2a"), rgb("ffbc6a")],
        "red": [rgb("6a1010"), rgb("9a1c1c"), rgb("c43030"), rgb("e86a5a")],
        "purple": [rgb("4a1a5a"), rgb("6a2a84"), rgb("8a44aa"), rgb("b884d4")]}
PILES = {"red": [rgb("4a1a10"), rgb("7a1e14"), rgb("a02a18"), rgb("c43c1e"), rgb("8a5a1c"), rgb("d0602a")],
         "orange": [rgb("5a2a10"), rgb("9a4a14"), rgb("c86a1a"), rgb("e08a26"), rgb("8a6a1c"), rgb("f0a840")],
         "yellow": [rgb("5a4a14"), rgb("9a7a18"), rgb("c8a020"), rgb("e4c03a"), rgb("8a6a1c"), rgb("f4dc6a")]}


# ---------------------------------------------------------------- the bobbing tub

def staves():
    """Upright plank staves, four pixels wide, with dark seams and grain."""
    c = Canvas()
    rng = random.Random(15101)
    for x in range(16):
        seam = x % 4 == 0
        base = rng.randrange(1, 3)
        for y in range(16):
            tone = 0 if seam else min(3, base + (1 if rng.random() < 0.25 else 0) - (1 if rng.random() < 0.15 else 0))
            c.px(x, y, STAVE[max(0, tone)])
    return c.img


def band():
    c = Canvas()
    noise(c, 0, 0, 15, 15, BAND[:3], 15111, [2, 3, 1])
    for x in range(0, 16, 5):
        c.px(x + 2, 7, BAND[3])
        c.px(x + 2, 8, BAND[0])
    return c.img


def water():
    c = Canvas()
    rng = random.Random(15121)
    for y in range(16):
        for x in range(16):
            ripple = (math.sin(x * 0.9 + y * 0.5) + math.sin(y * 1.1 - x * 0.3)) / 4 + 0.5
            c.px(x, y, WATER[min(2, int(ripple * 3))])
    for _ in range(6):
        x, y = rng.randrange(16), rng.randrange(16)
        c.px(x, y, WATER[3])
    return c.img


def apple():
    """Glossy red skin: darker low down, a bright highlight high on one side, a few pale freckles."""
    c = Canvas()
    rng = random.Random(15131)
    for y in range(16):
        for x in range(16):
            tone = 2 if y < 6 else 1 if y < 12 else 0
            if rng.random() < 0.12:
                tone = max(0, tone - 1)
            c.px(x, y, APPLE[tone])
    c.rect(3, 2, 6, 4, APPLE[3])
    for _ in range(5):
        c.px(rng.randrange(16), rng.randrange(16), APPLE[3])
    return c.img


def stem():
    c = Canvas()
    noise(c, 0, 0, 15, 15, STEM, 15141)
    return c.img


# ---------------------------------------------------------------- the pumpkin crate

def crate_wood():
    """Pale rough-sawn slats running across, with grain and a couple of nail heads."""
    c = Canvas()
    rng = random.Random(15201)
    for y in range(16):
        row = rng.randrange(1, 3)
        for x in range(16):
            tone = row + (1 if rng.random() < 0.2 else 0) - (1 if rng.random() < 0.2 else 0)
            c.px(x, y, CRATE[max(0, min(3, tone))])
    for x in (1, 14):
        for y in (4, 11):
            c.px(x, y, POST[0])
    return c.img


def crate_post():
    c = Canvas()
    noise(c, 0, 0, 15, 15, POST, 15211, [1, 3, 2])
    for y in range(0, 16, 5):
        c.px(4, y, POST[0])
        c.px(11, y + 2, POST[0])
    return c.img


def crate_label():
    """The front slat, stencilled with an orange pumpkin and its stalk."""
    c = Canvas()
    c.img.paste(crate_wood(), (0, 0))
    for y in range(16):
        for x in range(16):
            dx, dy = (x - 7.5) / 5.5, (y - 8.5) / 4.0
            if dx * dx + dy * dy <= 1.0:
                rib = abs(x - 7.5) in (1.5, 4.5) and dx * dx + dy * dy < 0.8
                c.px(x, y, PUMPKIN_PAINT[0] if rib else PUMPKIN_PAINT[1])
    c.rect(7, 3, 8, 4, rgb("4a6a1c"))
    return c.img


# ---------------------------------------------------------------- the hay bale seat

def straw(seed, horizontal):
    """Packed straw: streaks running one way, light and dark, with a few stray bright stalks."""
    c = Canvas()
    rng = random.Random(seed)
    for a in range(16):
        tone = rng.randrange(1, 4)
        length = rng.randrange(3, 8)
        b = 0
        while b < 16:
            for i in range(length):
                x, y = (b + i, a) if horizontal else (a, b + i)
                c.px(x, y, STRAW[tone])
            b += length
            tone = max(0, min(4, tone + rng.choice((-1, 0, 1))))
            length = rng.randrange(3, 8)
    for _ in range(8):
        x, y = rng.randrange(16), rng.randrange(16)
        c.px(x, y, STRAW[4])
    return c.img


def straw_ends():
    """The cut end of the bale: a jumble of stalk ends, dark between them."""
    c = Canvas()
    rng = random.Random(15311)
    for y in range(16):
        for x in range(16):
            c.px(x, y, STRAW[rng.choice((0, 1, 1, 2, 2, 3, 4))])
    for _ in range(14):
        c.px(rng.randrange(16), rng.randrange(16), STRAW[0])
    return c.img


def twine():
    c = Canvas()
    for y in range(16):
        for x in range(16):
            c.px(x, y, TWINE[(x + y) % 3])
    return c.img


# ---------------------------------------------------------------- the autumn wreath

def wreath_leaves():
    """Overlapping autumn leaves, rust to gold, with dark gaps and a few bright veins."""
    c = Canvas()
    rng = random.Random(15401)
    for y in range(16):
        for x in range(16):
            c.px(x, y, WREATH_LEAVES[5])
    for _ in range(22):
        cx, cy = rng.uniform(0, 16), rng.uniform(0, 16)
        tone = rng.randrange(0, 5)
        angle = rng.uniform(0, math.pi)
        for y in range(16):
            for x in range(16):
                dx, dy = x - cx, y - cy
                u = dx * math.cos(angle) + dy * math.sin(angle)
                v = -dx * math.sin(angle) + dy * math.cos(angle)
                if (u / 3.2) ** 2 + (v / 1.8) ** 2 <= 1.0:
                    c.px(x, y, shade(WREATH_LEAVES[tone], 1.15) if abs(v) < 0.4 else WREATH_LEAVES[tone])
    return c.img


def corn():
    """Ornamental corn: rows of kernels in red, gold, purple and cream."""
    c = Canvas()
    rng = random.Random(15411)
    for y in range(16):
        for x in range(16):
            c.px(x, y, KERNELS[rng.randrange(len(KERNELS))] if (x + y) % 2 == 0 or y % 2 == 0 else shade(KERNELS[rng.randrange(len(KERNELS))], 0.7))
    return c.img


def husk():
    c = Canvas()
    rng = random.Random(15421)
    for x in range(16):
        tone = rng.randrange(3)
        for y in range(16):
            c.px(x, y, HUSK[tone] if rng.random() > 0.1 else HUSK[0])
    return c.img


def mum(colour):
    """A tight mum head: rings of petals round a darker heart."""
    palette = MUMS[colour]
    c = Canvas()
    for y in range(16):
        for x in range(16):
            d = math.hypot(x - 7.5, y - 7.5)
            ring = int(d * 1.4) % 3
            c.px(x, y, palette[0] if d < 1.6 else palette[1 + ring] if ring < 2 else palette[2])
    return c.img


# ---------------------------------------------------------------- the leaf piles

def pile(colour, top):
    """Heaped fallen leaves of one colour (with a few browned ones), opaque; the side a little darker."""
    palette = PILES[colour]
    c = Canvas()
    rng = random.Random(15501 + list(PILES).index(colour) * 7 + (0 if top else 3))
    for y in range(16):
        for x in range(16):
            c.px(x, y, palette[0])
    for _ in range(40):
        cx, cy = rng.uniform(-1, 17), rng.uniform(-1, 17)
        tone = rng.choice((1, 2, 2, 3, 3, 4, 5))
        angle = rng.uniform(0, math.pi)
        length, width = rng.uniform(2.2, 3.4), rng.uniform(1.2, 1.8)
        for y in range(16):
            for x in range(16):
                dx, dy = x - cx, y - cy
                u = dx * math.cos(angle) + dy * math.sin(angle)
                v = -dx * math.sin(angle) + dy * math.cos(angle)
                if (u / length) ** 2 + (v / width) ** 2 <= 1.0:
                    color = palette[tone]
                    c.px(x, y, color if top else shade(color, 0.85))
    return c.img


def decor5_textures():
    """(kind, name) -> image for every texture of the fifth decorations batch."""
    out = {
        ("block", "bobbing_tub_wood"): staves(),
        ("block", "bobbing_tub_band"): band(),
        ("block", "bobbing_tub_water"): water(),
        ("block", "bobbing_tub_apple"): apple(),
        ("block", "bobbing_tub_stem"): stem(),
        ("block", "pumpkin_crate_wood"): crate_wood(),
        ("block", "pumpkin_crate_post"): crate_post(),
        ("block", "pumpkin_crate_label"): crate_label(),
        ("block", "hay_bale_seat_side"): straw(15301, True),
        ("block", "hay_bale_seat_top"): straw(15302, True),
        ("block", "hay_bale_seat_end"): straw_ends(),
        ("block", "hay_bale_seat_twine"): twine(),
        ("block", "autumn_wreath_leaves"): wreath_leaves(),
        ("block", "autumn_wreath_corn"): corn(),
        ("block", "autumn_wreath_husk"): husk(),
    }
    for colour in MUMS:
        out[("block", f"autumn_wreath_mum_{colour}")] = mum(colour)
    for colour in PILES:
        out[("block", f"{colour}_leaf_pile_top")] = pile(colour, True)
        out[("block", f"{colour}_leaf_pile_side")] = pile(colour, False)
    return out

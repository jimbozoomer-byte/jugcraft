"""Original 16x16 textures for Alpine Spawn's larch (requires Pillow).

Called from crop_textures.crop_textures(), next to the chestnut tree's. As with the rest of the branch's art,
every pixel is drawn here by code from fixed seeds; no Mojang texture is read, traced or recolored. The needles
come in three season states with the same layout: fresh green tufts, the same tufts turned gold, and the bare
twigs and knobbly short shoots left in winter.
"""
import math
import random

from crop_textures import Canvas, rgb

BARK = [rgb("2e1914"), rgb("44241a"), rgb("5c3222"), rgb("74422c"), rgb("8e5638")]
CRACK = rgb("1f100c")
HEARTWOOD = [rgb("7a3a22"), rgb("964a2c"), rgb("b05e38"), rgb("c87648")]
SAPWOOD = [rgb("b48a64"), rgb("c9a07a"), rgb("dcb690"), rgb("ebcaa6")]
PLANK = [rgb("6a3220"), rgb("86432a"), rgb("a05634"), rgb("b86a42"), rgb("cc8254")]
NEEDLES = {
    "green": [rgb("1e3a12"), rgb("2c5418"), rgb("3e6e1e"), rgb("568c28"), rgb("74aa38"), rgb("98c656")],
    "gold": [rgb("5a3a0c"), rgb("80560e"), rgb("a87414"), rgb("cc961e"), rgb("e6b832"), rgb("f6d866")],
}
TWIG = [rgb("2a201a"), rgb("3e3026"), rgb("564436"), rgb("6e5a48")]

# Tufts of needles on short shoots: (x, y) centres, the same in every season so the crown keeps its shape.
TUFTS = [(2, 2), (7, 1), (12, 3), (4, 6), (10, 7), (14, 9), (1, 10), (6, 11), (11, 13), (3, 14), (8, 5), (13, 15)]
# Twigs the tufts grow on, as wrapping line segments.
TWIGS = [((0, 4), (16, 1)), ((0, 9), (16, 12)), ((0, 15), (16, 13)), ((5, 0), (8, 16))]


def bark():
    """Thick, reddish larch bark broken into flaking plates by dark fissures; tiles vertically."""
    rng = random.Random(41)
    c = Canvas()
    for y in range(16):
        for x in range(16):
            c.px(x, y, BARK[2])
    # Plates in staggered columns, each with a lit top-left edge and a shaded lower edge.
    for column, x0 in enumerate((0, 4, 8, 12)):
        y = (column * 5) % 16
        while y < 32:
            height = rng.randint(3, 6)
            for dy in range(height):
                for dx in range(4):
                    px, py = x0 + dx, (y + dy) % 16
                    shade = 3 if dy == 0 or dx == 0 else 1 if dy == height - 1 or dx == 3 else 2
                    if rng.random() < 0.15:
                        shade = min(4, shade + 1)
                    c.px(px, py, BARK[shade])
            c.px(x0 + 3, (y + height - 1) % 16, CRACK)
            y += height
        for py in range(16):
            if rng.random() < 0.7:
                c.px(x0 + 3, py, BARK[0] if rng.random() < 0.5 else CRACK)
    for _ in range(6):
        c.px(rng.randrange(16), rng.randrange(16), BARK[4])
    return c.img


def log_top(stripped):
    """A sawn end: reddish heartwood rings inside pale sapwood, ringed by bark unless stripped."""
    c = Canvas()
    for y in range(16):
        for x in range(16):
            edge = max(abs(x - 7.5), abs(y - 7.5))
            ring = math.hypot(x - 7.5, y - 7.5)
            if edge > 6.9:
                c.px(x, y, SAPWOOD[1] if stripped else BARK[1 if (x * 3 + y) % 4 else 3])
            elif ring > 5.2:
                c.px(x, y, SAPWOOD[2 if int(ring * 2) % 2 else 1])
            else:
                shade = 2 if int(ring * 1.4) % 2 else 1
                c.px(x, y, HEARTWOOD[0] if ring < 1.2 else HEARTWOOD[shade + 1 if ring < 3 else shade])
    return c.img


def stripped_log():
    """Smooth stripped larch: pale sapwood streaked with long reddish grain."""
    rng = random.Random(43)
    c = Canvas()
    for y in range(16):
        for x in range(16):
            shade = 2 if x % 4 else 1
            if (x * 2 + y // 6) % 9 == 4:
                c.px(x, y, HEARTWOOD[3])
                continue
            if rng.random() < 0.05:
                shade = 0
            c.px(x, y, SAPWOOD[shade])
    return c.img


def planks():
    """Four boards of warm, reddish larch with dense grain and staggered seams, like a vanilla planks layout."""
    rng = random.Random(47)
    c = Canvas()
    for board in range(4):
        seam = (5, 12, 2, 9)[board]
        for y in range(board * 4, board * 4 + 4):
            for x in range(16):
                shade = 3 if y % 4 == 0 else 2
                if (x + board * 3) % 5 == 0:
                    shade -= 1
                if (x * 7 + y * 3 + board) % 11 == 0:
                    shade = 4
                if rng.random() < 0.05:
                    shade = max(0, shade - 1)
                c.px(x, y, PLANK[shade])
            c.px(seam, y, PLANK[0])
        for x in range(16):
            c.px(x, board * 4 + 3, PLANK[1])
    return c.img


def twigs(c):
    for (x0, y0), (x1, y1) in TWIGS:
        steps = 32
        for i in range(steps + 1):
            t = i / steps
            c.px((x0 + (x1 - x0) * t) % 16, (y0 + (y1 - y0) * t) % 16, TWIG[1])


def needles(season):
    """Larch needles: soft tufts on short shoots with gaps between; gold in autumn, bare twigs in winter."""
    rng = random.Random(53)
    c = Canvas()
    if season == "bare":
        # Gaps keep a dull twig colour, so fast graphics (drawn opaque) look like a dense mass of twigs.
        for y in range(16):
            for x in range(16):
                c.img.putpixel((x, y), TWIG[0] + (0,))
        twigs(c)
        for x, y in TUFTS:
            c.px(x, y, TWIG[3])
            c.px(x + 1, y, TWIG[2])
            c.px(x, y + 1, TWIG[2])
        return c.img
    palette = NEEDLES[season]
    for y in range(16):
        for x in range(16):
            c.img.putpixel((x, y), palette[0] + (0,))
    twigs(c)
    for x0, y0 in TUFTS:
        # Needles fan out from the short shoot in every direction, longest upward.
        for angle in range(0, 360, 40):
            a = math.radians(angle + rng.randint(-12, 12))
            length = rng.randint(2, 3)
            for i in range(1, length + 1):
                x = (x0 + math.sin(a) * i) % 16
                y = (y0 - math.cos(a) * i) % 16
                c.px(x, y, palette[4 if i == length else 3])
        c.px(x0, y0, palette[5])
    for y in range(16):
        for x in range(16):
            if c.empty(x, y) and rng.random() < 0.3:
                c.px(x, y, palette[rng.choice((1, 2))])
    return c.img


def sapling():
    """A young larch: a thin reddish stem with tiers of green tufts, narrowing to a point."""
    c = Canvas()
    c.line(8, 15, 8, 3, BARK[3])
    green = NEEDLES["green"]
    for y, half in ((4, 1), (6, 2), (8, 3), (10, 4), (12, 4)):
        for dx in range(-half, half + 1):
            c.px(8 + dx, y, green[4] if dx % 2 else green[3])
            if abs(dx) == half:
                c.px(8 + dx, y - 1, green[5])
    c.px(8, 2, green[5])
    return c.img


def larch_textures():
    """(kind, name) -> image for every larch texture."""
    out = {
        ("block", "larch_log"): bark(),
        ("block", "larch_log_top"): log_top(False),
        ("block", "stripped_larch_log"): stripped_log(),
        ("block", "stripped_larch_log_top"): log_top(True),
        ("block", "larch_planks"): planks(),
        ("block", "larch_needles"): needles("green"),
        ("block", "larch_needles_gold"): needles("gold"),
        ("block", "larch_needles_bare"): needles("bare"),
        ("block", "larch_sapling"): sapling(),
    }
    return out

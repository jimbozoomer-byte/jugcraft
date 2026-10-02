"""Original 16x16 textures for the biomes branch's seasonal-forest trees (requires Pillow): maple, aspen, fir and
dead wood (agriculture.WOOD_SETS, agriculture.TREES).

Called from crop_textures.crop_textures(), after the larch's. Every pixel is drawn here by code from fixed seeds; no
Mojang or other mod's texture is read, traced or recolored. Each wood has bark, a sawn end, stripped wood and planks;
each tree has its leaves in every look (green; autumn colours; bare twigs, see agriculture.leaf_looks) and a sapling.
"""
import math
import random

from crop_textures import Canvas, rgb


def pal(*hexes):
    return [rgb(h) for h in hexes]


WOODS = {
    "maple": {"bark": pal("3a3330", "4d4540", "615751", "776b63", "8c8077"),
              "plank": pal("8a5a3c", "a3704d", "bb8661", "cf9c76", "e0b38d"),
              "heart": pal("8a4f30", "a2623c", "b9774c", "cf8f60"),
              "sap": pal("c9a27c", "d8b48f", "e4c4a1", "eed3b3")},
    "aspen": {"bark": pal("4a443d", "a9a597", "c9c5b8", "dedace", "f0eee6"),
              "plank": pal("b69f74", "c9b48a", "d8c59c", "e5d4ae", "efe1c0"),
              "heart": pal("b19566", "c2a777", "d1b98a", "dec99d"),
              "sap": pal("dccfae", "e6dbbe", "eee5cc", "f5eedb")},
    "fir": {"bark": pal("2e2621", "40352d", "54473c", "685849", "7c6a57"),
            "plank": pal("9a7646", "b08a55", "c39d66", "d3af79", "e0c08c"),
            "heart": pal("9d6d3c", "b07f49", "c19158", "d0a269"),
            "sap": pal("d2b688", "dcc298", "e5cea8", "ecd9b7")},
    "dead": {"bark": pal("33302d", "4a4642", "615c56", "79736b", "908a81"),
             "plank": pal("6f695f", "857e72", "9a9285", "aea698", "c0b9ab"),
             "heart": pal("6a645b", "7b746a", "8b8479", "9b9488"),
             "sap": pal("a49d90", "b1aa9d", "bdb7aa", "c8c2b6")},
}

LEAVES = {
    "maple": pal("223f12", "2e5418", "3e6c20", "52872b", "6ba13a", "8cbc55"),
    "maple_red": pal("4e0d0a", "721510", "992018", "be3220", "dc512b", "ef7a3c"),
    "maple_orange": pal("6e2a07", "96400b", "ba5911", "d8761b", "ec9530", "f6b656"),
    "maple_gold": pal("6e4c07", "94690e", "b98c17", "d6ad27", "ebc943", "f6e07a"),
    "aspen": pal("27501a", "356a21", "47852b", "5ea137", "7dbc4e", "a2d571"),
    "aspen_gold": pal("7a5d0a", "a27d10", "c69d1a", "e2bd2e", "f2d752", "faea8c"),
    "fir": pal("0d261c", "143525", "1d4530", "27563c", "33684a", "457d5c"),
}
TWIG = pal("2a221c", "3e3228", "564637", "6e5a48")


def bark(wood, seed):
    """Bark that tiles vertically, in each wood's own pattern."""
    p = WOODS[wood]["bark"]
    rng = random.Random(seed)
    c = Canvas()
    for y in range(16):
        for x in range(16):
            if wood == "maple":
                # Long, interlacing furrows: a ridge every 3-4 columns that wanders a pixel left and right.
                phase = (x + (1 if (y // 4 + x // 4) % 2 else 0)) % 4
                shade = 3 if phase == 0 else 2 if phase in (1, 3) else 1
            elif wood == "aspen":
                # Smooth pale bark with faint horizontal bands.
                shade = 3 if (y % 5) else 2
            elif wood == "fir":
                # Smooth grey-brown bark broken into shallow, scaly patches.
                shade = 2 + (1 if (x * 3 + y * 5) % 7 < 3 else 0) - (1 if (x + y * 2) % 9 == 0 else 0)
            else:
                # Dead wood: grey, split by long vertical cracks.
                shade = 3 if x % 5 else 1
                if (x * 7 + y) % 13 == 0:
                    shade = 4
            if rng.random() < 0.1:
                shade = max(0, min(4, shade + rng.choice((-1, 1))))
            c.px(x, y, p[shade])
    if wood == "aspen":
        # Black "eyes" where branches fell, and short dark lenticels.
        for cx, cy in ((4, 3), (11, 10)):
            for dx, dy in ((-1, 0), (0, 0), (1, 0), (0, -1), (0, 1)):
                c.px(cx + dx, cy + dy, p[0])
            c.px(cx - 2, cy, p[1])
            c.px(cx + 2, cy, p[1])
        for x0, y0 in ((9, 2), (2, 9), (13, 14), (6, 13)):
            c.px(x0, y0, p[1])
            c.px(x0 + 1, y0, p[1])
    if wood == "fir":
        # Resin blisters.
        for x0, y0 in ((3, 5), (12, 2), (8, 12)):
            c.px(x0, y0, p[4])
            c.px(x0, y0 + 1, p[3])
    return c.img


def log_top(wood, stripped):
    w = WOODS[wood]
    c = Canvas()
    for y in range(16):
        for x in range(16):
            edge = max(abs(x - 7.5), abs(y - 7.5))
            ring = math.hypot(x - 7.5, y - 7.5)
            if edge > 6.9:
                c.px(x, y, w["sap"][1] if stripped else w["bark"][1 if (x * 3 + y) % 4 else 3])
            elif ring > 5.0:
                c.px(x, y, w["sap"][2 if int(ring * 2) % 2 else 1])
            else:
                shade = 2 if int(ring * 1.4) % 2 else 1
                c.px(x, y, w["heart"][0] if ring < 1.2 else w["heart"][shade + 1 if ring < 3 else shade])
    return c.img


def stripped_log(wood, seed):
    w = WOODS[wood]
    rng = random.Random(seed)
    c = Canvas()
    for y in range(16):
        for x in range(16):
            shade = 2 if x % 4 else 1
            if (x * 2 + y // 6) % 9 == 4:
                c.px(x, y, w["heart"][3])
                continue
            if rng.random() < 0.05:
                shade = 0
            c.px(x, y, w["sap"][shade])
    return c.img


def planks(wood, seed):
    """Four boards with staggered seams, like a vanilla planks layout, in the wood's colour and grain."""
    p = WOODS[wood]["plank"]
    rng = random.Random(seed)
    c = Canvas()
    for board in range(4):
        seam = (4, 11, 1, 8)[board]
        for y in range(board * 4, board * 4 + 4):
            for x in range(16):
                shade = 3 if y % 4 == 0 else 2
                if (x + board * 5) % 6 == 0:
                    shade -= 1
                if wood == "dead" and (x * 3 + y * 7 + board) % 10 == 0:
                    shade = 0
                if rng.random() < 0.05:
                    shade = max(0, shade - 1)
                c.px(x, y, p[shade])
            c.px(seam, y, p[0])
        for x in range(16):
            c.px(x, board * 4 + 3, p[1])
    return c.img


def twigs(c, seed):
    rng = random.Random(seed)
    for _ in range(4):
        x0, y0 = rng.randrange(16), rng.randrange(16)
        angle = rng.uniform(0, math.pi)
        for i in range(-8, 9):
            c.px((x0 + math.cos(angle) * i) % 16, (y0 + math.sin(angle) * i) % 16, TWIG[1])


def leaf_shape(kind, c, x0, y0, palette, rng):
    """One leaf (or spray) centred at x0, y0, wrapping round the tile."""
    def put(dx, dy, shade):
        c.px((x0 + dx) % 16, (y0 + dy) % 16, palette[shade])
    if kind == "maple":
        # A five-pointed maple leaf: a centre with three upper lobes and two side lobes.
        for dx, dy in ((0, 0), (-1, 0), (1, 0), (0, 1), (0, -1)):
            put(dx, dy, 3)
        for dx, dy in ((0, -2), (-2, -1), (2, -1), (-2, 1), (2, 1)):
            put(dx, dy, 4)
        put(0, 2, 1)
        put(-1, -1, 4 if rng.random() < 0.5 else 3)
        put(1, -1, 5 if rng.random() < 0.3 else 4)
    elif kind == "aspen":
        # Small round leaves that flutter: a lit side and a shaded side.
        for dx, dy in ((0, 0), (1, 0), (0, 1), (1, 1)):
            put(dx, dy, 4 if dx == 0 and dy == 0 else 3)
        put(1, 1, 2)
        put(-1, 0, 2 if rng.random() < 0.5 else 3)
    else:
        # A flat fir spray: a stem with short needles to both sides.
        for i in range(-2, 3):
            put(i, 0, 2)
            put(i, -1, 3 if i % 2 else 4)
            put(i, 1, 1 if i % 2 else 3)


def leaves(tree, look):
    """Leaves of one tree in one look; "bare" draws the twigs left in winter. Gaps keep a dark colour, so fast
    graphics (drawn opaque) still look like dense foliage."""
    rng = random.Random({"maple": 61, "aspen": 67, "fir": 71}[tree])
    c = Canvas()
    key = tree if look == "green" else f"{tree}_{look}"
    if look == "bare":
        for y in range(16):
            for x in range(16):
                c.img.putpixel((x, y), TWIG[0] + (0,))
        twigs(c, 73)
        for _ in range(6):
            c.px(rng.randrange(16), rng.randrange(16), TWIG[3])
        return c.img
    palette = LEAVES[key]
    for y in range(16):
        for x in range(16):
            c.img.putpixel((x, y), palette[0] + (0,))
    count = {"maple": 11, "aspen": 22, "fir": 14}[tree]
    for _ in range(count):
        leaf_shape(tree, c, rng.randrange(16), rng.randrange(16), palette, rng)
    fill = {"maple": 0.25, "aspen": 0.2, "fir": 0.55}[tree]
    for y in range(16):
        for x in range(16):
            if c.empty(x, y) and rng.random() < fill:
                c.px(x, y, palette[rng.choice((1, 2))])
    return c.img


def sapling(tree):
    c = Canvas()
    stem = WOODS[tree]["bark"][2 if tree != "aspen" else 3]
    c.line(8, 15, 8, 5, stem)
    if tree == "fir":
        green = LEAVES["fir"]
        for y, half in ((5, 1), (7, 2), (9, 3), (11, 4), (13, 4)):
            for dx in range(-half, half + 1):
                c.px(8 + dx, y, green[4] if dx % 2 else green[3])
                c.px(8 + dx, y + 1, green[2])
        c.px(8, 3, green[5])
        c.px(8, 4, green[4])
        return c.img
    green = LEAVES[tree]
    c.line(8, 10, 5, 7, stem)
    c.line(8, 8, 11, 5, stem)
    rng = random.Random(79)
    for x0, y0 in ((5, 5), (11, 4), (8, 3), (4, 9), (12, 8)):
        leaf_shape(tree, c, x0, y0, green, rng)
    return c.img


def forest_textures():
    """(kind, name) -> image for every seasonal-forest tree texture."""
    out = {}
    for index, wood in enumerate(WOODS):
        out[("block", f"{wood}_log")] = bark(wood, 81 + index)
        out[("block", f"{wood}_log_top")] = log_top(wood, False)
        out[("block", f"stripped_{wood}_log")] = stripped_log(wood, 91 + index)
        out[("block", f"stripped_{wood}_log_top")] = log_top(wood, True)
        out[("block", f"{wood}_planks")] = planks(wood, 101 + index)
    out[("block", "maple_leaves")] = leaves("maple", "green")
    for look in ("red", "orange", "gold", "bare"):
        out[("block", f"maple_leaves_{look}")] = leaves("maple", look)
    out[("block", "aspen_leaves")] = leaves("aspen", "green")
    for look in ("gold", "bare"):
        out[("block", f"aspen_leaves_{look}")] = leaves("aspen", look)
    out[("block", "fir_needles")] = leaves("fir", "green")
    for tree in ("maple", "aspen", "fir"):
        out[("block", f"{tree}_sapling")] = sapling(tree)
    return out

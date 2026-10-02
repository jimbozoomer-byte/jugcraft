"""Original 16x16 textures for the biomes branch's later batches (requires Pillow): their woods (agriculture.WOOD_SETS,
agriculture.TREES) and wild plants (tools/plants.py).

Called from crop_textures.crop_textures(), after the seasonal forests'. Every pixel is drawn here by code from fixed
seeds; no Mojang or other mod's texture is read, traced or recolored.
"""
import math
import random

from crop_textures import Canvas, rgb


def pal(*hexes):
    return [rgb(h) for h in hexes]


STEM = pal("2b4a1c", "3a6326", "4d7d31", "64983f")
WOODS = {
    # Jacaranda: smooth grey-brown bark with shallow fissures; rosy, fine-grained wood.
    "jacaranda": {"bark": pal("3b3330", "504641", "665a53", "7c6f66", "938479"),
                  "plank": pal("8c5a55", "a56d66", "bb8278", "cf978b", "e0ac9f"),
                  "heart": pal("8a4f4d", "9e605c", "b2736d", "c5877f"),
                  "sap": pal("d1aaa0", "dbb8ae", "e4c5bc", "ecd2ca"),
                  "style": "fissured"},
    # Willow: dark, deeply furrowed bark; pale, golden-green wood.
    "willow": {"bark": pal("2f2a22", "443b30", "5a4e3f", "71634f", "887960"),
               "plank": pal("9c8b5a", "b19f6b", "c4b27c", "d4c38f", "e2d3a3"),
               "heart": pal("8f7c48", "a08d56", "b19e66", "c1af78"),
               "sap": pal("d6caa0", "dfd4ae", "e7ddbb", "eee6c8"),
               "style": "furrowed"},
    # Palm: grey-brown bark in rings where old fronds fell; pale, sandy wood.
    "palm": {"bark": pal("4a4036", "5f5345", "766855", "8c7e68", "a2957c"),
             "plank": pal("a88a5c", "bc9e6c", "cdb07d", "dcc18f", "e8d2a3"),
             "heart": pal("9a7c4c", "ab8d5b", "bc9e6b", "cbaf7d"),
             "sap": pal("d9c7a0", "e2d1ae", "eadbbb", "f1e4c8"),
             "style": "ringed"},
    # Cypress: reddish, stringy bark; warm orange-brown wood.
    "cypress": {"bark": pal("3e2620", "55342a", "6c4436", "835543", "996752"),
                "plank": pal("8f5634", "a66641", "ba7750", "cc895f", "dc9c71"),
                "heart": pal("8a4a2a", "9c5833", "ae673e", "bf774b"),
                "sap": pal("d0a585", "dab293", "e2bea1", "eac9ae"),
                "style": "stringy"},
    # Redwood: thick, deep red-brown bark in long, shaggy ridges; rich red wood.
    "redwood": {"bark": pal("3a1a12", "552519", "6e3121", "88402b", "a35238"),
                "plank": pal("8a3a26", "a1482f", "b6583b", "c86b4a", "d8805e"),
                "heart": pal("7e2f1e", "923a26", "a54730", "b6563c"),
                "sap": pal("cf9a80", "d9a98f", "e2b79e", "eac4ad"),
                "style": "shaggy"},
    # Eucalyptus: smooth bark peeling in streaks of green, orange, purple and blue (the rainbow eucalyptus); pale,
    # honey-coloured wood.
    "eucalyptus": {"bark": pal("4d6b4a", "6f8f5a", "8fae6c", "b4c486", "d7d9a4"),
                   "streaks": pal("d07a2e", "9a4f9e", "4f7fb8", "c9a63a", "5ea06a"),
                   "plank": pal("b08a5a", "c39c69", "d3ad7a", "e1be8c", "ecce9f"),
                   "heart": pal("a3784a", "b38858", "c29867", "d0a877"),
                   "sap": pal("dccaa4", "e4d4b1", "ebdebe", "f2e7cb"),
                   "style": "streaked"},
    # Mahogany: grey-brown bark in small, flaking plates; deep reddish-brown wood.
    "mahogany": {"bark": pal("2e2420", "433530", "584741", "6e5a52", "856e64"),
                 "plank": pal("44201a", "54281f", "653126", "763b2d", "874636"),
                 "heart": pal("50201a", "622a20", "733428", "843f31"),
                 "sap": pal("b88a74", "c49883", "cfa692", "d9b4a1"),
                 "style": "scaly"},
}
NEEDLES = pal("102414", "16301b", "1d3d22", "264c2b", "315c35", "3f6f42")
EUCALYPTUS = pal("2c4a44", "3a5f56", "4a7468", "5e8a7c", "77a194", "96bcb0")
MAHOGANY = pal("0e2a12", "143719", "1b4520", "245529", "2f6634", "3d7a42")
FRONDS = pal("173d14", "1f5019", "2b671f", "3a8028", "4f9a34", "6db44c")
SCALES = pal("0f2a1a", "163522", "1e432b", "275235", "326240", "41754e")
WILLOW = {"green": pal("223f18", "2f5620", "3f6e29", "548834", "6fa244", "93bd62"),
          "gold": pal("6a5a0e", "8f7a12", "b39a1c", "cdb52e", "e2cd4a", "f0e27c")}
TWIG = pal("2a221c", "3e3228", "564637", "6e5a48")
BLOSSOM = {"jacaranda": pal("3b2a6b", "523b8e", "6b52b0", "8670cc", "a492e0", "c4b8f0")}
LEAF_GREEN = pal("1f3b16", "2b501d", "3a6826", "4c8231", "659c40", "86b85a")


def bark(wood, seed):
    w = WOODS[wood]
    p = w["bark"]
    rng = random.Random(seed)
    c = Canvas()
    for y in range(16):
        for x in range(16):
            if w["style"] == "fissured":
                # Smooth plates split by shallow, wandering vertical fissures.
                lane = (x + (y // 5) % 2) % 5
                shade = 1 if lane == 0 else 3 if lane == 2 else 2
            elif w["style"] == "ringed":
                # Rings where old fronds fell, every few rows.
                shade = 1 if y % 4 == 0 else 3 if y % 4 == 1 else 2
            elif w["style"] == "stringy":
                # Long, fibrous strands.
                shade = 3 if (x * 2 + y // 4) % 5 == 0 else 1 if x % 3 == 0 else 2
            elif w["style"] == "shaggy":
                # Long, thick ridges with deep, dark grooves, broken here and there.
                lane = (x + (1 if (y // 7) % 2 else 0)) % 4
                shade = 0 if lane == 0 else 4 if lane == 2 and y % 5 else 3 if lane == 2 else 2
            elif w["style"] == "streaked":
                # Smooth green bark; streaks of colour are laid over it below.
                shade = 2 if (x + y // 6) % 4 else 3
            elif w["style"] == "scaly":
                # Small plates, offset row by row, edged dark.
                plate_x = (x + (2 if (y // 3) % 2 else 0)) % 4
                shade = 1 if plate_x == 0 or y % 3 == 0 else 3 if plate_x == 2 else 2
            elif w["style"] == "furrowed":
                # Deep, interlacing furrows between narrow ridges.
                lane = (x + (1 if (y // 3) % 2 else 0)) % 3
                shade = 0 if lane == 0 else 3 if lane == 1 else 2
            else:
                shade = 2
            if rng.random() < 0.08:
                shade = max(0, min(4, shade + rng.choice((-1, 1))))
            c.px(x, y, p[shade])
    if w["style"] == "streaked":
        # Long vertical streaks where the bark has peeled, each in one of the wood's streak colours.
        for i in range(7):
            x = rng.randrange(16)
            top = rng.randrange(16)
            colour = w["streaks"][i % len(w["streaks"])]
            for k in range(rng.randrange(5, 12)):
                c.px(x, (top + k) % 16, colour)
                if k % 3 == 1:
                    c.px((x + 1) % 16, (top + k) % 16, colour)
    return c.img


def log_top(wood, stripped):
    w = WOODS[wood]
    c = Canvas()
    for y in range(16):
        for x in range(16):
            edge = max(abs(x - 7.5), abs(y - 7.5))
            ring = math.hypot(x - 7.5, y - 7.5)
            if edge > 6.9:
                c.px(x, y, w["sap"][1] if stripped else w["bark"][1 if (x + y * 3) % 4 else 3])
            elif ring > 5.0:
                c.px(x, y, w["sap"][2 if int(ring * 2) % 2 else 1])
            else:
                shade = 2 if int(ring * 1.5) % 2 else 1
                c.px(x, y, w["heart"][0] if ring < 1.2 else w["heart"][shade + 1 if ring < 3 else shade])
    return c.img


def stripped_log(wood, seed):
    w = WOODS[wood]
    rng = random.Random(seed)
    c = Canvas()
    for y in range(16):
        for x in range(16):
            shade = 2 if x % 3 else 1
            if (x * 3 + y // 5) % 11 == 6:
                c.px(x, y, w["heart"][3])
                continue
            if rng.random() < 0.05:
                shade = 0
            c.px(x, y, w["sap"][shade])
    return c.img


def planks(wood, seed):
    """Four boards with staggered seams in the wood's colour and grain."""
    p = WOODS[wood]["plank"]
    rng = random.Random(seed)
    c = Canvas()
    for board in range(4):
        seam = (6, 13, 2, 9)[board]
        for y in range(board * 4, board * 4 + 4):
            for x in range(16):
                shade = 3 if y % 4 == 0 else 2
                if (x * 2 + board * 3) % 7 == 0:
                    shade -= 1
                if rng.random() < 0.05:
                    shade = max(0, shade - 1)
                c.px(x, y, p[shade])
            c.px(seam, y, p[0])
        for x in range(16):
            c.px(x, board * 4 + 3, p[1])
    return c.img


def blossom_leaves(tree, seed):
    """Leaves in bloom: clusters of small flowers over a few leaves. Gaps keep a dark colour, so fast graphics (drawn
    opaque) still look dense."""
    rng = random.Random(seed)
    bloom = BLOSSOM[tree]
    c = Canvas()
    for y in range(16):
        for x in range(16):
            c.img.putpixel((x, y), bloom[0] + (0,))
    for _ in range(9):
        x0, y0 = rng.randrange(16), rng.randrange(16)
        for dx, dy in ((0, 0), (1, 0), (0, 1)):
            c.px((x0 + dx) % 16, (y0 + dy) % 16, LEAF_GREEN[3 if dx else 2])
    for _ in range(26):
        x0, y0 = rng.randrange(16), rng.randrange(16)
        for dx, dy, shade in ((0, 0, 4), (1, 0, 3), (0, 1, 3), (-1, 0, 2), (0, -1, 5)):
            c.px((x0 + dx) % 16, (y0 + dy) % 16, bloom[shade])
    for y in range(16):
        for x in range(16):
            if c.empty(x, y) and rng.random() < 0.35:
                c.px(x, y, bloom[rng.choice((1, 2))])
    return c.img


def blossom_sapling(tree):
    c = Canvas()
    stem = WOODS[tree]["bark"][3]
    c.line(8, 15, 8, 7, stem)
    c.line(8, 10, 5, 7, stem)
    c.line(8, 9, 11, 6, stem)
    bloom = BLOSSOM[tree]
    for x0, y0 in ((5, 6), (11, 5), (8, 5), (6, 4), (10, 3)):
        for dx, dy, shade in ((0, 0, 4), (1, 0, 3), (0, 1, 2), (-1, 0, 3), (0, -1, 5)):
            c.px(x0 + dx, y0 + dy, bloom[shade])
    for x0, y0 in ((4, 9), (12, 8)):
        c.px(x0, y0, LEAF_GREEN[3])
        c.px(x0 + 1, y0, LEAF_GREEN[4])
    return c.img


def willow_leaves(look, seed):
    """Long, narrow willow leaves hanging in strands; "bare" draws the winter twigs. Gaps keep a dark colour, so fast
    graphics (drawn opaque) still look dense."""
    rng = random.Random(seed)
    c = Canvas()
    if look == "bare":
        for y in range(16):
            for x in range(16):
                c.img.putpixel((x, y), TWIG[0] + (0,))
        for x in (1, 5, 9, 13):
            for y in range(16):
                if rng.random() < 0.8:
                    c.px(x + (y // 6) % 2, y, TWIG[1 + (y % 2)])
        return c.img
    palette = WILLOW[look]
    for y in range(16):
        for x in range(16):
            c.img.putpixel((x, y), palette[0] + (0,))
    for strand in range(7):
        x = strand * 2 + rng.randrange(0, 2)
        top = rng.randrange(-4, 4)
        for k in range(rng.randrange(8, 14)):
            y = (top + k) % 16
            shade = 4 if k % 3 == 0 else 3 if k % 3 == 1 else 2
            c.px(x, y, palette[shade])
            if k % 4 == 2:
                c.px((x + 1) % 16, y, palette[shade - 1])
    for y in range(16):
        for x in range(16):
            if c.empty(x, y) and rng.random() < 0.3:
                c.px(x, y, palette[1])
    return c.img


def willow_sapling():
    c = Canvas()
    stem = WOODS["willow"]["bark"][3]
    c.line(8, 15, 8, 5, stem)
    green = WILLOW["green"]
    for x0 in (5, 7, 9, 11):
        for k in range(5):
            c.px(x0 + (1 if k > 2 and x0 > 8 else 0) - (1 if k > 2 and x0 < 8 else 0), 4 + k, green[4 if k % 2 else 3])
    c.line(5, 4, 11, 4, green[2])
    return c.img


def fronds_leaves(seed):
    """Palm fronds: long feathery leaves with a midrib, crossing the tile."""
    rng = random.Random(seed)
    c = Canvas()
    for y in range(16):
        for x in range(16):
            c.img.putpixel((x, y), FRONDS[0] + (0,))
    for _ in range(5):
        x0, y0 = rng.randrange(16), rng.randrange(16)
        dx, dy = rng.choice(((1, 0), (0, 1), (1, 1), (1, -1)))
        for k in range(-7, 8):
            x, y = (x0 + dx * k) % 16, (y0 + dy * k) % 16
            c.px(x, y, FRONDS[2])
            for side in (-1, 1):
                if k % 2 == 0:
                    c.px((x + dy * side) % 16, (y - dx * side) % 16, FRONDS[4 if side > 0 else 3])
    for y in range(16):
        for x in range(16):
            if c.empty(x, y) and rng.random() < 0.3:
                c.px(x, y, FRONDS[1])
    return c.img


def scale_leaves(seed):
    """Dense cypress scale-leaves: small upright sprays, dark and nearly closed."""
    rng = random.Random(seed)
    c = Canvas()
    for y in range(16):
        for x in range(16):
            c.px(x, y, SCALES[rng.choice((0, 1, 1, 2))])
    for _ in range(20):
        x, y = rng.randrange(16), rng.randrange(16)
        c.px(x, y, SCALES[4])
        c.px(x, (y + 1) % 16, SCALES[3])
        c.px((x + 1) % 16, (y + 1) % 16, SCALES[5] if rng.random() < 0.3 else SCALES[3])
    return c.img


def needle_leaves(seed):
    """Redwood needles: short, flat sprays of dark needles along twigs, nearly closed."""
    rng = random.Random(seed)
    c = Canvas()
    for y in range(16):
        for x in range(16):
            c.px(x, y, NEEDLES[rng.choice((0, 1, 1))])
    for _ in range(9):
        x0, y0 = rng.randrange(16), rng.randrange(16)
        for k in range(-3, 4):
            x = (x0 + k) % 16
            c.px(x, y0, NEEDLES[2])
            c.px(x, (y0 - 1) % 16, NEEDLES[4 if k % 2 else 3])
            c.px(x, (y0 + 1) % 16, NEEDLES[3 if k % 2 else 5])
    return c.img


def eucalyptus_leaves(seed):
    """Eucalyptus: long, narrow, sickle-shaped leaves in a dusty blue-green, hanging loosely. Gaps keep a dark colour,
    so fast graphics (drawn opaque) still look dense."""
    rng = random.Random(seed)
    c = Canvas()
    for y in range(16):
        for x in range(16):
            c.img.putpixel((x, y), EUCALYPTUS[0] + (0,))
    for _ in range(11):
        x0, y0 = rng.randrange(16), rng.randrange(16)
        bend = rng.choice((-1, 1))
        for k in range(6):
            x = (x0 + (bend if k >= 3 else 0) + (bend if k >= 5 else 0)) % 16
            c.px(x, (y0 + k) % 16, EUCALYPTUS[3 + (k % 2) if k < 5 else 2])
        c.px(x0, y0, EUCALYPTUS[5])
    for y in range(16):
        for x in range(16):
            if c.empty(x, y) and rng.random() < 0.3:
                c.px(x, y, EUCALYPTUS[1])
    return c.img


def mahogany_leaves(seed):
    """Mahogany: glossy, broad, paired leaflets in a deep green, densely set."""
    rng = random.Random(seed)
    c = Canvas()
    for y in range(16):
        for x in range(16):
            c.px(x, y, MAHOGANY[rng.choice((0, 1))])
    for _ in range(14):
        x0, y0 = rng.randrange(16), rng.randrange(16)
        for dx, dy, shade in ((0, 0, 3), (1, 0, 4), (0, 1, 2), (1, 1, 3), (2, 0, 2), (-1, 1, 2)):
            c.px((x0 + dx) % 16, (y0 + dy) % 16, MAHOGANY[shade])
        c.px((x0 + 1) % 16, (y0 - 1) % 16, MAHOGANY[5])
    return c.img


def redwood_sapling():
    c = Canvas()
    c.line(8, 15, 8, 3, WOODS["redwood"]["bark"][3])
    for y in range(3, 13):
        half = 1 + (y - 3) // 4
        for dx in range(-half, half + 1):
            if dx and (y + dx) % 2 == 0:
                c.px(8 + dx, y, NEEDLES[3 + abs(dx) % 3])
    c.px(8, 2, NEEDLES[5])
    return c.img


def eucalyptus_sapling():
    c = Canvas()
    stem = WOODS["eucalyptus"]["bark"][3]
    c.line(8, 15, 8, 4, stem)
    c.px(8, 9, WOODS["eucalyptus"]["streaks"][0])
    for x0, y0, side in ((8, 5, -1), (8, 7, 1), (8, 9, -1), (8, 11, 1), (8, 4, 1)):
        for k in range(1, 4):
            c.px(x0 + side * k, y0 + k // 2, EUCALYPTUS[3 + k % 2])
    return c.img


def mahogany_sapling():
    c = Canvas()
    stem = WOODS["mahogany"]["bark"][3]
    c.line(8, 15, 8, 7, stem)
    c.line(8, 9, 5, 6, stem)
    c.line(8, 8, 11, 5, stem)
    for x0, y0 in ((4, 5), (11, 4), (8, 5), (6, 3), (10, 2)):
        for dx, dy, shade in ((0, 0, 3), (1, 0, 4), (0, 1, 2), (-1, 0, 2), (0, -1, 5)):
            c.px(x0 + dx, y0 + dy, MAHOGANY[shade])
    return c.img


def palm_sapling():
    c = Canvas()
    stem = WOODS["palm"]["bark"][3]
    c.line(8, 15, 9, 7, stem)
    for dx, dy in ((-4, 2), (4, 1), (-3, -2), (3, -3), (0, -4)):
        c.line(9, 6, 9 + dx, 6 + dy, FRONDS[3])
        c.px(9 + dx, 6 + dy, FRONDS[5])
    return c.img


def cypress_sapling():
    c = Canvas()
    c.line(8, 15, 8, 11, WOODS["cypress"]["bark"][3])
    for y in range(3, 12):
        half = 1 if y < 6 or y > 9 else 2
        for dx in range(-half, half + 1):
            c.px(8 + dx, y, SCALES[3 + (dx + y) % 3])
    return c.img


# ---------------------------------------------------------------- plants

LAVENDER = pal("3e2a6e", "583f94", "7558b6", "947ad0", "b8a6e6")
GOLD = pal("8a6407", "b88a0d", "dcb11c", "f2d03c", "fbe77a")
HEATHER = pal("5e1f4a", "84306a", "a8478c", "c66aa8", "dd94c6")
COSMOS = pal("8c3306", "b8500c", "dd7116", "f39431", "fbb862")
COSMOS_EYE = pal("8a6407", "dcb11c", "f2d03c")
CLOVER = pal("1e4a1a", "2a6222", "3a7c2c", "4f9638", "6db04c")
WHITE = pal("c9c4d6", "e6e2ee", "fbfaff")


def stems(c, rng, xs, top, palette=STEM, lean=0.0):
    for i, x in enumerate(xs):
        t = top + rng.randrange(0, 3)
        drift = lean * (i - len(xs) / 2)
        c.line(x, 15, x + drift, t, palette[1 + i % 2])


def spike(c, x, top, length, palette, rng):
    """A flower spike: florets stacked up a stem, lighter towards the tip."""
    for k in range(length):
        y = top + k
        shade = 4 if k == 0 else 3 if k < length // 2 else 2
        c.px(x, y, palette[shade])
        if k % 2 == 0 and k > 0:
            c.px(x + rng.choice((-1, 1)), y, palette[shade - 1])


def lavender(tall_part=None):
    """Lavender: grey-green stems, each topped by a violet spike. tall_part: "bottom" or "top" of tall lavender."""
    rng = random.Random({None: 401, "bottom": 403, "top": 405}[tall_part])
    c = Canvas()
    xs = [3, 5, 7, 8, 10, 12] if tall_part else [4, 6, 8, 10, 12]
    grey_green = pal("3c5236", "4f6a47", "66845c", "80a074")
    if tall_part == "bottom":
        for x in xs:
            c.line(x, 15, x + rng.choice((-1, 0, 1)), 0, grey_green[1 + x % 2])
        for _ in range(10):
            x, y = rng.randrange(2, 14), rng.randrange(6, 15)
            c.px(x, y, grey_green[3])
            c.px(x + 1, y - 1, grey_green[2])
        return c.img
    top = 2 if tall_part == "top" else 4
    for x in xs:
        tip = top + rng.randrange(0, 4)
        c.line(x, 15, x, tip + 5, grey_green[1 + x % 2])
        spike(c, x, tip, 6, LAVENDER, rng)
    if not tall_part:
        for x0 in (5, 9, 11):
            c.px(x0, 13, grey_green[3])
            c.px(x0 + 1, 12, grey_green[2])
    return c.img


def goldenrod():
    """Goldenrod: upright stems with arching, feathery plumes of tiny yellow flowers."""
    rng = random.Random(411)
    c = Canvas()
    for x, top in ((5, 3), (8, 2), (11, 4)):
        c.line(x, 15, x, top + 3, STEM[1 + x % 2])
        for k in range(7):
            y = top + k // 2
            dx = k - 3
            c.px(x + dx, y + abs(dx) // 2, GOLD[3 if k % 2 else 4])
            c.px(x + dx, y + abs(dx) // 2 + 1, GOLD[2])
        for _ in range(6):
            c.px(x + rng.randrange(-3, 4), top + rng.randrange(0, 4), GOLD[rng.choice((1, 3))])
    for x0, y0 in ((4, 11), (9, 10), (12, 12)):
        c.line(x0, y0, x0 + 2, y0 - 1, STEM[3])
    return c.img


def heather():
    """Heather: a low, wiry shrub thick with tiny pink-purple bells."""
    rng = random.Random(421)
    c = Canvas()
    for i in range(7):
        x = 2 + i * 2
        c.line(x, 15, x + rng.choice((-1, 0, 1)), 6 + rng.randrange(0, 3), STEM[i % 2])
    for _ in range(40):
        x, y = rng.randrange(1, 15), rng.randrange(4, 14)
        c.px(x, y, HEATHER[rng.choice((1, 2, 3, 3, 4))])
    for _ in range(8):
        c.px(rng.randrange(2, 14), rng.randrange(8, 15), STEM[3])
    return c.img


def cosmos_flower(c, cx, cy):
    for i in range(8):
        a = 2 * math.pi * i / 8
        for r in (1, 2):
            c.px(cx + math.cos(a) * r, cy + math.sin(a) * r, COSMOS[3 if r == 2 else 2])
    c.px(cx, cy, COSMOS_EYE[1])
    c.px(cx + 1, cy, COSMOS_EYE[2])
    c.px(cx, cy - 1, COSMOS[4])


def orange_cosmos():
    """Orange cosmos: slender stems with feathery leaves and open orange flowers."""
    c = Canvas()
    for x, top in ((5, 5), (10, 3)):
        c.line(x, 15, x, top + 2, STEM[1])
    c.line(8, 15, 7, 9, STEM[2])
    for x0, y0 in ((4, 11), (10, 10), (7, 13)):
        c.px(x0, y0, STEM[3])
        c.px(x0 + 1, y0 - 1, STEM[3])
        c.px(x0 - 1, y0 - 1, STEM[2])
    cosmos_flower(c, 5, 5)
    cosmos_flower(c, 10, 3)
    cosmos_flower(c, 7, 9)
    return c.img


def trefoil(c, cx, cy, rng):
    """A clover leaf from above: three round leaflets round a pale centre."""
    for a in (-90, 30, 150):
        r = math.radians(a + rng.randrange(-15, 16))
        lx, ly = cx + math.cos(r) * 1.5, cy + math.sin(r) * 1.5
        for dx, dy in ((0, 0), (1, 0), (0, 1), (1, 1)):
            c.px(lx + dx - 0.5, ly + dy - 0.5, CLOVER[3 if dx == dy else 2])
        c.px(lx, ly, CLOVER[4])
    c.px(cx, cy, CLOVER[1])


def clover():
    """Clover as a flowerbed sheet: four quarters, one clump each (vanilla's flowerbed layout), seen from above, with
    a few white flower heads."""
    rng = random.Random(431)
    c = Canvas()
    for qx, qy in ((0, 0), (0, 8), (8, 8), (8, 0)):
        for cx, cy in ((qx + 2, qy + 2), (qx + 5, qy + 3), (qx + 3, qy + 5), (qx + 6, qy + 6)):
            trefoil(c, cx, cy, rng)
        fx, fy = qx + rng.randrange(2, 6), qy + rng.randrange(2, 6)
        for dx, dy, shade in ((0, 0, 2), (1, 0, 1), (0, 1, 1), (-1, 0, 1), (0, -1, 2)):
            c.px(fx + dx, fy + dy, WHITE[shade])
    return c.img


def clover_stem():
    c = Canvas()
    for y in range(4, 8):
        c.px(0, y, CLOVER[2 if y % 2 else 1])
    return c.img


HIBISCUS = pal("7a1838", "a8244e", "d03a68", "ec6a8e", "f8a2bc")
HYDRANGEA = pal("2d3f86", "3c58ae", "5277cc", "6f95e0", "9ab6ee", "c4d4f6")


def hibiscus():
    """Hibiscus: glossy leaves and two big, open pink flowers, each with a long stamen."""
    c = Canvas()
    c.line(8, 15, 8, 8, STEM[1])
    c.line(8, 12, 5, 9, STEM[2])
    for x0, y0 in ((4, 11), (10, 11), (11, 8)):
        c.px(x0, y0, STEM[3])
        c.px(x0 + 1, y0, STEM[2])
        c.px(x0, y0 - 1, STEM[3])
    for cx, cy in ((5, 6), (10, 4)):
        for i in range(5):
            a = 2 * math.pi * i / 5 - math.pi / 2
            for r in (1, 2, 3):
                c.px(cx + math.cos(a) * r, cy + math.sin(a) * r, HIBISCUS[4 - r if r > 1 else 1])
        c.px(cx, cy, HIBISCUS[0])
        c.line(cx, cy, cx + 2, cy - 3, HIBISCUS[1])
        c.px(cx + 2, cy - 3, GOLD[3])
    return c.img


def hydrangea(part):
    """Hydrangea: a leafy shrub (bottom) under round mopheads of small blue florets (top)."""
    rng = random.Random({"bottom": 471, "top": 473}[part])
    c = Canvas()
    if part == "bottom":
        for x in (3, 6, 9, 12):
            c.line(x, 15, x + rng.choice((-1, 0, 1)), 0, STEM[1 + x % 2])
        for _ in range(14):
            x, y = rng.randrange(1, 15), rng.randrange(1, 15)
            for dx, dy, shade in ((0, 0, 2), (1, 0, 3), (0, 1, 1)):
                c.px(x + dx, y + dy, STEM[shade])
        return c.img
    for x in (4, 8, 12):
        c.line(x, 15, x, 9, STEM[1])
    for cx, cy, r in ((4, 8, 3), (11, 6, 3), (8, 4, 2)):
        for y in range(cy - r, cy + r + 1):
            for x in range(cx - r, cx + r + 1):
                if (x - cx) ** 2 + (y - cy) ** 2 <= r * r + 1:
                    shade = 4 if (x + y) % 2 else 3
                    if rng.random() < 0.2:
                        shade = rng.choice((2, 5))
                    c.px(x, y, HYDRANGEA[shade])
    for x0, y0 in ((2, 12), (13, 11)):
        c.px(x0, y0, STEM[3])
        c.px(x0 + 1, y0 - 1, STEM[2])
    return c.img


SEA_OATS = pal("6f7a3c", "8a9548", "a5ad5a", "c2c070")
OAT_HEADS = pal("8a6a2a", "a8843a", "c6a04e", "dcbc6a")


def sea_oats(part):
    """Sea oats: tall, pale green-gold grass blades (bottom), topped by stalks hung with flat, golden seed heads (top)."""
    rng = random.Random({"bottom": 481, "top": 483}[part])
    c = Canvas()
    if part == "bottom":
        for x in (2, 4, 6, 8, 10, 12, 14):
            lean = rng.choice((-1, 0, 1))
            c.line(x, 15, x + lean, rng.randrange(0, 4), SEA_OATS[x % 3])
        return c.img
    for x, top in ((4, 2), (8, 1), (12, 3)):
        c.line(x, 15, x, top, SEA_OATS[1])
        for k in range(5):
            y = top + 1 + k * 2
            side = -1 if k % 2 else 1
            c.px(x + side, y, OAT_HEADS[2 + k % 2])
            c.px(x + side * 2, y + 1, OAT_HEADS[1])
    for x in (2, 6, 10, 14):
        c.line(x, 15, x + rng.choice((-1, 1)), 9 + rng.randrange(0, 4), SEA_OATS[2])
    return c.img


CATTAIL_LEAF = pal("2f4a20", "3e6229", "507a33", "679541")
CATTAIL_HEAD = pal("3a2214", "53311c", "6b4226", "855533")
WATERGRASS = pal("1c4a2a", "245e35", "2f7543", "3d8c52", "52a566")
DUCKWEED = pal("2f5c1a", "3f7a22", "52962d", "6db03c", "8cc955")


def cattail(part):
    """Cattails: long, flat leaves and, at the top, brown, velvety heads on stiff stalks."""
    rng = random.Random({"bottom": 441, "top": 443}[part])
    c = Canvas()
    if part == "bottom":
        for x in (3, 5, 7, 9, 11, 13):
            lean = rng.choice((-1, 0, 1))
            c.line(x, 15, x + lean, 0, CATTAIL_LEAF[1 + x % 3])
        return c.img
    for x, top in ((4, 5), (8, 2), (11, 4)):
        c.line(x, 15, x, top, CATTAIL_LEAF[2])
        for k in range(5):
            c.px(x, top + 1 + k, CATTAIL_HEAD[2 if k % 2 else 3])
            c.px(x + 1, top + 1 + k, CATTAIL_HEAD[1])
        c.px(x, top - 1, CATTAIL_LEAF[3])
    for x in (2, 6, 13):
        c.line(x, 15, x + rng.choice((-1, 1)), 7 + rng.randrange(0, 4), CATTAIL_LEAF[1])
    return c.img


def watergrass():
    """Watergrass: soft, ribbon-like blades that sway under water."""
    rng = random.Random(451)
    c = Canvas()
    for i, x in enumerate((2, 4, 7, 9, 12, 14)):
        top = 2 + rng.randrange(0, 6)
        for y in range(15, top - 1, -1):
            sway = round(math.sin((y + i * 3) / 3.0))
            c.px(x + sway, y, WATERGRASS[1 + (y + i) % 4])
    return c.img


def duckweed():
    """Duckweed from above: a scatter of tiny round fronds, open water between them."""
    rng = random.Random(461)
    c = Canvas()
    for _ in range(46):
        x, y = rng.randrange(16), rng.randrange(16)
        c.px(x, y, DUCKWEED[rng.choice((2, 3, 3, 4))])
        if rng.random() < 0.5:
            c.px((x + 1) % 16, y, DUCKWEED[1])
    return c.img


def wild_textures():
    """(kind, name) -> image for every texture of the later batches' trees and plants."""
    out = {}
    for index, wood in enumerate(WOODS):
        out[("block", f"{wood}_log")] = bark(wood, 501 + index)
        out[("block", f"{wood}_log_top")] = log_top(wood, False)
        out[("block", f"stripped_{wood}_log")] = stripped_log(wood, 511 + index)
        out[("block", f"stripped_{wood}_log_top")] = log_top(wood, True)
        out[("block", f"{wood}_planks")] = planks(wood, 521 + index)
    out[("block", "jacaranda_leaves")] = blossom_leaves("jacaranda", 531)
    out[("block", "jacaranda_sapling")] = blossom_sapling("jacaranda")
    out[("block", "palm_fronds")] = fronds_leaves(551)
    out[("block", "palm_sapling")] = palm_sapling()
    out[("block", "cypress_leaves")] = scale_leaves(561)
    out[("block", "cypress_sapling")] = cypress_sapling()
    out[("block", "redwood_needles")] = needle_leaves(571)
    out[("block", "redwood_sapling")] = redwood_sapling()
    out[("block", "eucalyptus_leaves")] = eucalyptus_leaves(581)
    out[("block", "eucalyptus_sapling")] = eucalyptus_sapling()
    out[("block", "mahogany_leaves")] = mahogany_leaves(591)
    out[("block", "mahogany_sapling")] = mahogany_sapling()
    out[("block", "sea_oats_bottom")] = sea_oats("bottom")
    out[("block", "sea_oats_top")] = sea_oats("top")
    out[("block", "hibiscus")] = hibiscus()
    out[("block", "hydrangea_bottom")] = hydrangea("bottom")
    out[("block", "hydrangea_top")] = hydrangea("top")
    out[("block", "willow_leaves")] = willow_leaves("green", 541)
    out[("block", "willow_leaves_gold")] = willow_leaves("gold", 541)
    out[("block", "willow_leaves_bare")] = willow_leaves("bare", 543)
    out[("block", "willow_sapling")] = willow_sapling()
    out[("block", "cattail_bottom")] = cattail("bottom")
    out[("block", "cattail_top")] = cattail("top")
    out[("block", "watergrass")] = watergrass()
    out[("block", "duckweed")] = duckweed()
    out[("block", "lavender")] = lavender()
    out[("block", "tall_lavender_bottom")] = lavender("bottom")
    out[("block", "tall_lavender_top")] = lavender("top")
    out[("block", "goldenrod")] = goldenrod()
    out[("block", "heather")] = heather()
    out[("block", "orange_cosmos")] = orange_cosmos()
    out[("block", "clover")] = clover()
    out[("block", "clover_stem")] = clover_stem()
    return out

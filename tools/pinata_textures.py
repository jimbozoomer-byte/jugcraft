"""Original textures for the piñata party (fall addition 28), painted at 64 by 64 (docs/ART_DIRECTION.md, "High
resolution"): crepe-paper fringe in tiers, each tier's strips overlapping the one below, in orange, green, black, pink,
yellow and turquoise, and torn (the fringe ripped away in patches to the papier-mâché beneath); the pumpkin's black-paper
face and the bat's; a fringe skirt and tassel streamers, cut out between their strips; the bat's scalloped wings, whole
and torn; the rope; the items; the Blindfold's view (misc/blindfold, 256 by 128, as vanilla's pumpkin blur is drawn) and
its band as worn (four times the 64 by 32 armour layout).

Called from crop_textures.crop_textures(). Every pixel is drawn here by code from a fixed seed (tools/fur_paint.py's
painter); no other texture is read, traced or recoloured.
"""
import math

from PIL import Image

from fur_paint import Painter, mix, ramp
from crop_textures import rgb

N = 64

PAPER = {
    "orange": [rgb("6a2a04"), rgb("b04c0a"), rgb("e4741a"), rgb("fa9a38"), rgb("ffc480")],
    "green": [rgb("0e3010"), rgb("1e5a20"), rgb("348a34"), rgb("58b450"), rgb("9ad88a")],
    "black": [rgb("08080a"), rgb("16141a"), rgb("26222c"), rgb("3a3442"), rgb("5a5266")],
    "pink": [rgb("6a0c34"), rgb("b01c5a"), rgb("e83c84"), rgb("fa70a8"), rgb("ffb0d0")],
    "yellow": [rgb("6a5004"), rgb("b08a0a"), rgb("e8c01c"), rgb("fadc48"), rgb("fff0a0")],
    "turquoise": [rgb("04404a"), rgb("0a7a84"), rgb("1cb4c0"), rgb("48d8e0"), rgb("a0f0f4")],
    "purple": [rgb("1e0a30"), rgb("3c1660"), rgb("5e2a92"), rgb("8448c0"), rgb("b88ae0")],
}
NEWSPRINT = [rgb("6a5a42"), rgb("9a8a6c"), rgb("c4b494"), rgb("dccfae"), rgb("eee4c8")]
JUTE = [rgb("4a3418"), rgb("7a5a2c"), rgb("a68048"), rgb("c8a46a"), rgb("e4c894")]
CLOTH = [rgb("060608"), rgb("121216"), rgb("1e1e24"), rgb("2c2c34"), rgb("40404a")]
WOOD = [rgb("4a2e14"), rgb("7a5028"), rgb("a8743c"), rgb("c89a5c")]
TIERS = 8


def fringe(paper, seed, torn=False):
    """Crepe paper in tiers: each tier a row of strips cut every two or three texels, crinkled across, lit at the top
    and shaded under the tier above, their ends ragged over the next tier. Torn, patches are ripped off to the tan
    papier-mâché beneath, with newsprint lines and a few strips hanging loose."""
    p = Painter(N, N, seed)
    tier_h = N // TIERS
    for y in range(N):
        tier, row = divmod(y, tier_h)
        for x in range(N):
            strip = (x + tier * 3) // 3
            crinkle = 0.05 * math.sin(y * 2.3 + strip * 1.7) + 0.06 * (p.noise(x, y * 3, 3.0) - 0.5)
            # Lit at a tier's top, shaded at its foot where it tucks under, darker in the cut between strips.
            f = 0.72 - 0.32 * (row / tier_h) ** 1.4 + crinkle
            if (x + tier * 3) % 3 == 0:
                f -= 0.16
            p.put(x, y, ramp(paper, f))
        # The ragged ends of the tier above hang over this tier's top.
    for tier in range(1, TIERS):
        y0 = tier * tier_h
        for x in range(N):
            hang = 1 + int(2.5 * p.noise(x * 2, tier * 11, 2.0))
            for k in range(hang):
                p.put(x, y0 + k, ramp(paper, 0.5 - 0.1 * k + 0.05 * math.sin(x)), 0.85)
            p.put(x, y0 + hang, ramp(paper, 0.12), 0.6)
    if torn:
        rng = p.rng
        for _ in range(5):
            cx, cy = rng.randint(6, N - 7), rng.randint(6, N - 7)
            rx, ry = rng.uniform(5, 10), rng.uniform(4, 8)
            for y in range(int(cy - ry) - 1, int(cy + ry) + 2):
                for x in range(int(cx - rx) - 1, int(cx + rx) + 2):
                    d = ((x - cx) / rx) ** 2 + ((y - cy) / ry) ** 2 + 0.35 * (p.noise(x * 3, y * 3, 3.0) - 0.5)
                    if d <= 1.0:
                        line = (y % 4 == 0) and (x % 9 not in (0, 1))
                        p.put(x, y, ramp(NEWSPRINT, (0.35 if line else 0.62) + 0.08 * (p.noise(x, y, 4.0) - 0.5)))
                    elif d <= 1.25:
                        p.put(x, y, ramp(paper, 0.95))
            # A strip or two hanging loose from the tear.
            for s in range(rng.randint(1, 2)):
                sx = int(cx + rng.uniform(-rx, rx) * 0.6)
                for k in range(int(ry + 6)):
                    p.put(sx, int(cy - ry) + k, ramp(paper, 0.8 - 0.02 * k))
                    p.put(sx + 1, int(cy - ry) + k, ramp(paper, 0.55 - 0.02 * k))
    return p.img


def edge(paper, seed):
    """A fringe skirt: a band at the top, then strips hanging down to ragged ends, cut out between them."""
    p = Painter(N, N, seed)
    for x in range(N):
        strip = x // 4
        gap = x % 4 == 3
        length = 40 + int(18 * p.noise(strip * 7, 3, 1.5))
        for y in range(N):
            if y < 14:
                p.put(x, y, ramp(paper, 0.7 - 0.2 * y / 14 + 0.04 * math.sin(x * 1.3)))
            elif not gap and y < length:
                p.put(x, y, ramp(paper, 0.62 - 0.2 * (y - 14) / 50 + 0.05 * math.sin(y * 0.9 + strip)))
    return p.img


def tassel():
    """Tassel streamers: narrow strips of every colour, rippling down, cut out between them."""
    p = Painter(N, N, 28010)
    colours = ["pink", "yellow", "turquoise", "orange", "green", "purple"]
    for x in range(N):
        strip = x // 5
        if x % 5 == 4:
            continue
        paper = PAPER[colours[strip % len(colours)]]
        length = 50 + int(14 * p.noise(strip * 5, 1, 1.5))
        for y in range(length):
            wobble = 0.12 * math.sin(y * 0.45 + strip)
            p.put(x, y, ramp(paper, 0.6 + wobble - 0.15 * (x % 5) / 4))
    return p.img


def face_pumpkin():
    """The pumpkin's face cut from black paper: triangle eyes, a nose and a jagged grin, each with a lighter cut edge."""
    p = Painter(N, N, 28011)
    ink = PAPER["black"]

    def tri(ax, ay, bx, by, cx, cy):
        for y in range(N):
            for x in range(N):
                d1 = (x - bx) * (ay - by) - (ax - bx) * (y - by)
                d2 = (x - cx) * (by - cy) - (bx - cx) * (y - cy)
                d3 = (x - ax) * (cy - ay) - (cx - ax) * (y - ay)
                if (d1 >= 0 and d2 >= 0 and d3 >= 0) or (d1 <= 0 and d2 <= 0 and d3 <= 0):
                    p.put(x, y, ramp(ink, 0.3 + 0.1 * (p.noise(x, y, 3.0) - 0.5)))
    tri(8, 22, 24, 22, 16, 8)
    tri(40, 22, 56, 22, 48, 8)
    tri(28, 32, 36, 32, 32, 25)
    for y in range(40, 56):
        for x in range(8, 57):
            top = 40 + 4 * abs(math.sin(x * 0.4))
            bottom = 54 - 4 * abs(math.sin(x * 0.4 + 1.2)) - 3 * ((x - 32) / 24) ** 2 * -1
            if top <= y <= min(bottom, 56) and abs(x - 32) <= 24 - (y - 40) * 0.6:
                p.put(x, y, ramp(ink, 0.3))
    return p.img


def face_bat():
    """The bat's face on black fringe: round yellow eyes with black pupils, a pink nose and two white fangs."""
    p = Painter(N, N, 28012)
    img = fringe(PAPER["black"], 28013)
    p.img.paste(img)
    p.px = p.img.load()
    for cx in (20, 44):
        p.blob(cx, 26, 9, 9, rgb("c89a10"), rgb("fff080"))
        p.blob(cx + 1, 27, 4, 5, rgb("000000"), rgb("2a2a2a"))
        p.glint(cx - 2, 23)
    p.blob(32, 38, 4, 3, rgb("a0305a"), rgb("ff90b8"))
    for fx in (26, 38):
        for k in range(8):
            for w in range(-2 + k // 3, 3 - k // 3):
                p.put(fx + w, 44 + k, rgb("f4f0e8"))
    return p.img


def wing(torn=False):
    """A bat wing of black-purple paper: finger bones fanning from the shoulder (top inner corner), the membrane
    scalloped between them at its foot, cut out below. Torn, it has rips through it."""
    p = Painter(N, N, 28014 + (1 if torn else 0))
    fingers = [(64, 60), (40, 64), (18, 60), (2, 46)]
    for y in range(N):
        for x in range(N):
            # Scalloped bottom edge between the finger tips.
            u = x / N
            scallop = 46 + 14 * abs(math.sin(u * math.pi * 3.0))
            if y > scallop:
                continue
            f = 0.45 + 0.12 * (p.noise(x, y, 6.0) - 0.5) + 0.05 * math.sin(y * 1.5 + x * 0.2)
            p.put(x, y, ramp(PAPER["purple"] if (x + y) % 23 < 2 else PAPER["black"], f + 0.15))
    for fx, fy in fingers:
        p.line(N - 2, 2, fx - 1 if fx == 64 else fx, fy - 6, ramp(PAPER["black"], 0.05), width=2.5)
        p.line(N - 2, 2, fx - 1 if fx == 64 else fx, fy - 6, ramp(PAPER["purple"], 0.75), width=0.8)
    if torn:
        rng = p.rng
        for _ in range(4):
            cx, cy, r = rng.randint(10, 54), rng.randint(10, 40), rng.uniform(3, 6)
            for y in range(int(cy - r), int(cy + r) + 1):
                for x in range(int(cx - r), int(cx + r) + 1):
                    if (x - cx) ** 2 + (y - cy) ** 2 + 6 * (p.noise(x * 3, y * 3, 2.0) - 0.5) <= r * r and 0 <= x < N and 0 <= y < N:
                        p.px[x, y] = (0, 0, 0, 0)
    return p.img


def rope():
    p = Painter(N, N, 28016)
    for y in range(N):
        for x in range(N):
            twist = math.sin((x + y * 0.8) * 0.4)
            p.put(x, y, ramp(JUTE, 0.55 + 0.25 * twist + 0.08 * (p.noise(x, y, 3.0) - 0.5)))
    return p.img


# ---------------------------------------------------------------- items

def item_pumpkin():
    p = Painter(N, N, 28020)
    p.line(32, 0, 32, 10, ramp(JUTE, 0.6), width=2)
    for y in range(12, 58):
        for x in range(6, 58):
            d = ((x - 32) / 26) ** 2 + ((y - 36) / 22) ** 2
            if d <= 1:
                tier = (y // 6) % 2
                f = 0.65 - 0.2 * d + (0.06 if tier else -0.04) + 0.05 * math.sin(x * 1.1)
                if abs(x - 32) in (9, 18):
                    f -= 0.15
                p.put(x, y, ramp(PAPER["orange"], f))
    for (ax, ay, bx, by, cx, cy) in ((18, 32, 28, 32, 23, 24), (36, 32, 46, 32, 41, 24)):
        for y in range(N):
            for x in range(N):
                d1 = (x - bx) * (ay - by) - (ax - bx) * (y - by)
                d2 = (x - cx) * (by - cy) - (bx - cx) * (y - cy)
                d3 = (x - ax) * (cy - ay) - (cx - ax) * (y - ay)
                if (d1 >= 0 and d2 >= 0 and d3 >= 0) or (d1 <= 0 and d2 <= 0 and d3 <= 0):
                    p.put(x, y, PAPER["black"][1])
    for x in range(20, 45):
        for y in range(41, 46 - abs(x - 32) // 6):
            p.put(x, y, PAPER["black"][1])
    p.blob(32, 12, 3, 4, PAPER["green"][1], PAPER["green"][3])
    return p.img


def item_star():
    p = Painter(N, N, 28021)
    p.line(32, 0, 32, 8, ramp(JUTE, 0.6), width=2)
    colours = ["pink", "yellow", "turquoise", "orange", "pink", "yellow"]
    c = (32, 32)
    for i, angle in enumerate((90, 30, -30, -90, -150, 150)):
        rad = math.radians(angle)
        for t in range(9, 26):
            w = 6.5 * (1 - (t - 9) / 17)
            x, y = c[0] + t * math.cos(rad), c[1] - t * math.sin(rad)
            p.ellipse(x, y, max(0.8, w), max(0.8, w), ramp(PAPER[colours[i]], 0.75 - 0.2 * (t - 9) / 17))
    p.blob(32, 32, 10, 10, PAPER["pink"][1], PAPER["pink"][4])
    for tx in (14, 32, 50):
        for k in range(10):
            p.put(tx + (k % 3) - 1, 52 + k // 1 if 52 + k < N else N - 1, ramp(PAPER["turquoise" if tx != 32 else "yellow"], 0.7))
    return p.img


def item_bat():
    p = Painter(N, N, 28022)
    p.line(32, 0, 32, 12, ramp(JUTE, 0.6), width=2)
    for y in range(18, 44):
        for x in range(2, 62):
            u = abs(x - 32)
            if u < 9:
                continue
            top = 18 + (u - 9) * 0.25
            bottom = 40 - 6 * abs(math.sin(u * 0.33))
            if top <= y <= bottom:
                p.put(x, y, ramp(PAPER["black"], 0.55 + 0.1 * math.sin(x * 0.7)))
    p.blob(32, 36, 10, 14, PAPER["black"][1], PAPER["black"][4])
    p.blob(32, 22, 9, 8, PAPER["black"][1], PAPER["black"][4])
    for ex in (26, 38):
        p.blob(ex, 12, 3, 5, PAPER["black"][1], PAPER["black"][3])
        p.blob(ex, 21, 3, 3, rgb("c89a10"), rgb("fff080"))
    return p.img


def item_stick():
    """The Piñata Stick: a stick painted in red and white spirals, with a tassel at its grip."""
    p = Painter(N, N, 28023)
    for t in range(4, 60):
        x, y = t, N - t
        red = (t // 6) % 2 == 0
        for w in (-2, -1, 0, 1, 2):
            colour = rgb("c41e1e") if red else rgb("f4f0e8")
            p.put(x + w, y, mix(colour, (0, 0, 0), 0.25 if w == 2 else 0.0))
    for k in range(10):
        p.put(6 + k // 2, 52 + k, ramp(PAPER["yellow"], 0.7))
        p.put(9 + k // 2, 52 + k, ramp(PAPER["turquoise"], 0.7))
    return p.img


def item_blindfold():
    p = Painter(N, N, 28024)
    for y in range(24, 40):
        for x in range(4, 60):
            sag = 3 * math.sin((x - 4) / 56 * math.pi)
            if 24 + sag <= y <= 36 + sag:
                p.put(x, y, ramp(CLOTH, 0.55 + 0.15 * ((x + y) % 3 == 0) + 0.05 * math.sin(x)))
    for k in range(14):
        p.put(58 - k // 3, 34 + k, ramp(CLOTH, 0.6))
        p.put(60 - k // 2, 34 + k, ramp(CLOTH, 0.5))
    return p.img


# ---------------------------------------------------------------- the blindfold, seen and worn

def blindfold_view():
    """Looking out from under the Blindfold, 256x128: dark woven cloth over everything but a sliver at the bottom."""
    img = Image.new("RGBA", (256, 128), (0, 0, 0, 0))
    px = img.load()
    for y in range(128):
        for x in range(256):
            weave = ((x // 2 + y // 2) % 2) * 6
            alpha = 252 if y < 108 else int(252 - (y - 108) / 20 * 140)
            px[x, y] = (14 + weave, 14 + weave, 18 + weave, alpha)
    return img


def blindfold_worn():
    """The Blindfold as worn, at four times vanilla's 64 by 32 armour layout: a band of black cloth round the head at
    the eyes, knotted at the back with its two ends hanging."""
    s = 4
    img = Image.new("RGBA", (64 * s, 32 * s), (0, 0, 0, 0))
    px = img.load()
    for y in range(11 * s, 13 * s + s // 2):
        for x in range(0, 32 * s):
            shade = 0.5 + 0.2 * (((x // 2 + y // 2) % 3) == 0) - 0.15 * abs(y - 12 * s) / (2 * s)
            px[x, y] = ramp(CLOTH, shade) + (255,)
    # The knot on the back of the head and its ends.
    cx = 28 * s
    for y in range(10 * s, 16 * s):
        for x in range(cx - 2 * s, cx + 2 * s):
            if abs(x - cx) + abs(y - 12.5 * s) * 0.8 <= 2.2 * s:
                px[x, y] = ramp(CLOTH, 0.7 - 0.2 * abs(x - cx) / (2 * s)) + (255,)
            elif y > 13 * s and abs(x - cx - (y - 13 * s) * 0.3) < s * 0.6:
                px[x, y] = ramp(CLOTH, 0.55) + (255,)
    return img


def pinata_textures():
    blocks = {}
    for i, paper in enumerate(("orange", "green", "black", "pink", "yellow", "turquoise")):
        blocks[f"pinata_fringe_{paper}"] = fringe(PAPER[paper], 28100 + i)
    for i, paper in enumerate(("orange", "black", "pink", "yellow", "turquoise")):
        blocks[f"pinata_fringe_{paper}_torn"] = fringe(PAPER[paper], 28100 + i, torn=True)
    blocks.update({"pinata_edge_orange": edge(PAPER["orange"], 28030), "pinata_edge_purple": edge(PAPER["purple"], 28031),
                   "pinata_tassel": tassel(), "pinata_face_pumpkin": face_pumpkin(), "pinata_face_bat": face_bat(),
                   "pinata_wing": wing(), "pinata_wing_torn": wing(torn=True), "pinata_rope": rope()})
    out = {("block", name): img for name, img in blocks.items()}
    out.update({("item", "pumpkin_pinata"): item_pumpkin(), ("item", "star_pinata"): item_star(), ("item", "bat_pinata"): item_bat(),
                ("item", "pinata_stick"): item_stick(), ("item", "blindfold"): item_blindfold(),
                ("misc", "blindfold"): blindfold_view(), ("entity", "equipment/humanoid/blindfold"): blindfold_worn()})
    return out

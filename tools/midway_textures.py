"""Original textures for the fall fair midway (fall addition 26), painted at 64 by 64 (docs/ART_DIRECTION.md, "High
resolution"), four texels to each model pixel: the High Striker's painted wood, brass rail and bell, its lamps lit and
unlit, the puck and pad, the four bands of its scale and its signboard; Ring Toss's slatted crate and its green, amber and
milk-glass bottles and the ring; the first seven plushes' felt and embroidered faces (the harvest plushes are painted in
tools/decor16_data.py); and the striker, mallet and ring as items.

Called from crop_textures.crop_textures(). Every pixel is drawn here by code from a fixed seed (tools/fur_paint.py's
painter); no other texture is read, traced or recoloured.
"""
import math

from fur_paint import Painter, mix, ramp
from crop_textures import rgb
from midway import HIGH_STRIKER, RING_TOSS, PLUSHES

N = 64
S = 4  # texels to a model pixel

WOOD_WHITE = [rgb("8c8478"), rgb("bab2a2"), rgb("d8d0be"), rgb("ece6d6"), rgb("f8f4ea")]
LACQUER_RED = [rgb("4a0a0a"), rgb("7a1212"), rgb("a81c18"), rgb("cc2e26"), rgb("e85a48")]
GOLD = [rgb("5a3c0a"), rgb("8c6418"), rgb("bc9030"), rgb("e0bc5a"), rgb("fbe7a4")]
BRASS = [rgb("4a3010"), rgb("7a5420"), rgb("a8803a"), rgb("d0aa5c"), rgb("f0d898"), rgb("fff4d4")]
PLANK = [rgb("4a3020"), rgb("6c4a30"), rgb("8a6440"), rgb("a67e54"), rgb("c09a6c")]
RUBBER = [rgb("0c0c0e"), rgb("1c1c20"), rgb("2e2e34"), rgb("46464e")]
YELLOW = [rgb("7a5a04"), rgb("c8960a"), rgb("f0c428"), rgb("ffe680")]
GLASS = {"green": [rgb("0e2a14"), rgb("1c4a24"), rgb("2e6e36"), rgb("4c9a52"), rgb("9ad8a0")],
         "amber": [rgb("3a1c04"), rgb("6a3608"), rgb("9a5612"), rgb("c87e24"), rgb("f0c070")],
         "milk": [rgb("8a8e90"), rgb("b4b8ba"), rgb("d6d9da"), rgb("eceeee"), rgb("ffffff")]}
SCALE_BANDS = {1: [rgb("1e5a1e"), rgb("2e8a2e"), rgb("54b84a")], 2: [rgb("6a6a10"), rgb("b0b020"), rgb("e0dc40")],
               3: [rgb("8a4a08"), rgb("cc7212"), rgb("f09a2a")], 4: [rgb("7a0e0e"), rgb("c01c1c"), rgb("f04434")]}


def wood(paint, rect, colours, level=0.55, boards=4, vertical=True):
    """Painted boards: grain running along them, a dark seam between each, the paint worn pale at the edges."""
    x0, y0, w, h = rect
    across = w if vertical else h
    for y in range(h):
        for x in range(w):
            a, b = (x, y) if vertical else (y, x)
            board = int(a * boards / across)
            seam = (a * boards) % across < boards * 0.9 and a > 0
            grain = 0.06 * math.sin(b * 0.35 + board * 2.1 + 3.0 * paint.noise(x0 + x, y0 + y, 6.0))
            f = level + grain + 0.1 * (paint.noise(x0 + x, y0 + y, 10.0) - 0.5) - (0.35 if seam else 0.0)
            paint.put(x0 + x, y0 + y, ramp(colours, f))


def star(paint, cx, cy, r, colours, level=0.7):
    """A five-pointed star, lit from the top left."""
    for y in range(int(cy - r) - 1, int(cy + r) + 2):
        for x in range(int(cx - r) - 1, int(cx + r) + 2):
            dx, dy = x + 0.5 - cx, y + 0.5 - cy
            angle = math.atan2(dy, dx) + math.pi / 2
            reach = r * (0.5 + 0.5 * abs(math.cos(angle * 2.5))) ** 1.6
            if math.hypot(dx, dy) <= reach:
                paint.put(x, y, ramp(colours, level + 0.2 * (-dx - dy) / max(1.0, r)))


def stripe_border(paint, colours, width=3, level=0.65, inset=2):
    """A painted pinstripe round the texture, `inset` from its edge."""
    for i in range(N):
        for k in range(width):
            for x, y in ((i, inset + k), (i, N - 1 - inset - k), (inset + k, i), (N - 1 - inset - k, i)):
                paint.put(x, y, ramp(colours, level + 0.1 * math.sin(i * 0.4)))


def striker_textures():
    out = {}
    p = Painter(N, N, 26001)
    wood(p, (0, 0, N, N), WOOD_WHITE, boards=3)
    for x in (2, 3, N - 4, N - 3):
        for y in range(N):
            p.put(x, y, ramp(LACQUER_RED, 0.6))
    out["high_striker_post"] = p.img

    p = Painter(N, N, 26002)
    wood(p, (0, 0, N, N), LACQUER_RED, boards=4, vertical=False)
    stripe_border(p, GOLD, width=2, inset=3)
    out["high_striker_red"] = p.img

    p = Painter(N, N, 26003)
    wood(p, (0, 0, N, N), PLANK, boards=5, vertical=False)
    for r, level in ((22, 0.6), (16, 0.2), (10, 0.6)):
        p.ellipse(32, 32, r, r, ramp(LACQUER_RED if level < 0.5 else WOOD_WHITE, 0.6), 0.85)
    star(p, 32, 32, 8, GOLD)
    out["high_striker_deck"] = p.img

    p = Painter(N, N, 26004)
    for y in range(N):
        for x in range(N):
            f = 0.5 + 0.35 * math.cos((x / N) * math.pi * 2.2 + 0.6) + 0.06 * (p.noise(x, y, 8.0) - 0.5)
            p.put(x, y, ramp(BRASS, f))
    out["high_striker_brass"] = p.img

    for lit in (False, True):
        p = Painter(N, N, 26005)
        glass = [rgb("3a2a08"), rgb("6a4c10"), rgb("9a7020"), rgb("c09634")] if not lit else [rgb("e09a20"), rgb("ffc840"), rgb("fff090"), rgb("ffffff")]
        for y in range(N):
            for x in range(N):
                d = math.hypot(x + 0.5 - N / 2, y + 0.5 - N / 2) / (N / 2)
                p.put(x, y, ramp(glass, 0.85 - 0.6 * d + 0.08 * (p.noise(x, y, 6.0) - 0.5)))
        p.ellipse(22, 18, 7, 5, ramp(glass, 1.0), 0.7)
        out["high_striker_lamp_on" if lit else "high_striker_lamp_off"] = p.img

    p = Painter(N, N, 26006)
    for y in range(N):
        for x in range(N):
            band = 24 <= y < 40
            p.put(x, y, ramp(WOOD_WHITE if band else LACQUER_RED, 0.7 - 0.3 * y / N + 0.05 * (p.noise(x, y) - 0.5)))
    p.line(4, 6, N - 6, 6, ramp(LACQUER_RED, 0.95), width=2, alpha=0.7)
    out["high_striker_puck"] = p.img

    p = Painter(N, N, 26007)
    for y in range(N):
        for x in range(N):
            d = math.hypot(x + 0.5 - N / 2, y + 0.5 - N / 2)
            ring = int(d / 7) % 2 == 0
            p.put(x, y, ramp(RUBBER if ring else YELLOW, 0.5 + 0.1 * (p.noise(x, y, 5.0) - 0.5) - 0.004 * d))
    p.blob(32, 32, 6, 6, LACQUER_RED[1], LACQUER_RED[4])
    out["high_striker_pad"] = p.img

    for part, band in SCALE_BANDS.items():
        p = Painter(N, N, 26010 + part)
        wood(p, (0, 0, N, N), WOOD_WHITE, boards=1)
        # The gauge: a coloured band up the middle, ticks across it, a bright step at each lamp.
        for y in range(N):
            for x in range(14, 50):
                p.put(x, y, ramp(band, 0.55 + 0.25 * (1 - y / N) + 0.05 * (p.noise(x, y) - 0.5)))
        for y in range(2, N, 16):
            p.line(10, y, 54, y, ramp(RUBBER, 0.25), width=1.2, alpha=0.85)
        for y in range(10, N, 16):
            p.line(16, y, 48, y, ramp(RUBBER, 0.35), width=0.8, alpha=0.6)
        for y in range(N):
            for x in (12, 13, 50, 51):
                p.put(x, y, ramp(GOLD, 0.6))
        out[f"high_striker_scale_{part}"] = p.img

    for lit in (False, True):
        p = Painter(N, N, 26020)
        for y in range(N):
            for x in range(N):
                f = 0.55 + 0.3 * math.cos((x / N) * math.pi * 2.0 + 0.8) - 0.15 * y / N + 0.05 * (p.noise(x, y, 7.0) - 0.5)
                p.put(x, y, ramp(BRASS, f + (0.25 if lit else 0.0)))
        if lit:
            p.ellipse(20, 20, 10, 8, rgb("fffbe8"), 0.6)
        out["high_striker_bell_lit" if lit else "high_striker_bell"] = p.img

    p = Painter(N, N, 26030)
    wood(p, (0, 0, N, N), LACQUER_RED, boards=4, vertical=False)
    for k in range(5):
        star(p, 6 + k * 13, 8, 5, GOLD)
    for y in (17, 30):
        for x in range(N):
            p.put(x, y, ramp(GOLD, 0.7))
    star(p, 32, 24, 6, GOLD, 0.8)
    for cx in (12, 52):
        p.ellipse(cx, 24, 3, 3, ramp(GOLD, 0.5))
    out["high_striker_sign"] = p.img
    return out


def ring_toss_textures():
    out = {}
    p = Painter(N, N, 26101)
    wood(p, (0, 0, N, N), PLANK, boards=4, vertical=False)
    for y in range(18, 46):
        for x in range(N):
            if (x // 8) % 2 == 0:
                p.put(x, y, ramp(LACQUER_RED, 0.55 + 0.05 * (p.noise(x, y) - 0.5)), 0.9)
            else:
                p.put(x, y, ramp(WOOD_WHITE, 0.7 + 0.05 * (p.noise(x, y) - 0.5)), 0.9)
    star(p, 32, 32, 9, GOLD)
    out["ring_toss_crate"] = p.img

    p = Painter(N, N, 26102)
    wood(p, (0, 0, N, N), PLANK, boards=4, vertical=False)
    out["ring_toss_crate_top"] = p.img

    for name, glass in GLASS.items():
        p = Painter(N, N, 26110 + len(name))
        for y in range(N):
            for x in range(N):
                f = 0.45 + 0.3 * math.cos((x / N) * math.pi * 2.0 + 0.7) + 0.04 * (p.noise(x, y, 8.0) - 0.5)
                p.put(x, y, ramp(glass, f))
        for y in range(N):
            for x in (12, 13, 14):
                p.put(x, y, ramp(glass, 1.0), 0.7)
        out[f"ring_toss_bottle_{name}"] = p.img

    p = Painter(N, N, 26120)
    for y in range(N):
        for x in range(N):
            colour = [LACQUER_RED, YELLOW, [rgb("0a2a6a"), rgb("1a4aa8"), rgb("3a74dc"), rgb("80b0f8")]][((x + y) // 8) % 3]
            p.put(x, y, ramp(colour, 0.6 + 0.2 * math.sin(y * 0.5)))
    out["ring_toss_ring"] = p.img
    return out


# Felt colours: from shadow to highlight.
FELT = {
    "pumpkin": [rgb("6a2a04"), rgb("a84a0a"), rgb("dc7014"), rgb("f39434"), rgb("ffbc6c")],
    "ghost": [rgb("9aa0a8"), rgb("c4c8ce"), rgb("e2e4e8"), rgb("f4f5f7"), rgb("ffffff")],
    "bat": [rgb("0c0a10"), rgb("1a1622"), rgb("2a2434"), rgb("3c344a"), rgb("564c66")],
    "cat": [rgb("08080a"), rgb("141418"), rgb("222228"), rgb("32323a"), rgb("4a4a54")],
    "squirrel": [rgb("4a1a08"), rgb("7a2e10"), rgb("a8461c"), rgb("c8642c"), rgb("e08a4c")],
    "werewolf": [rgb("2a180c"), rgb("442814"), rgb("603a1e"), rgb("7a4e2a"), rgb("96683e")],
    "green": [rgb("1a3a10"), rgb("2c5a1c"), rgb("40782a"), rgb("5a963c"), rgb("80b85a")],
    "stem": [rgb("2a2410"), rgb("44401c"), rgb("5e5a28"), rgb("787434")],
    "tan": [rgb("8a6a4a"), rgb("a8865e"), rgb("c4a276"), rgb("dcbe92")],
    "cream": [rgb("b8a888"), rgb("d4c6a4"), rgb("ece0c0"), rgb("faf2dc")],
    "acorn": [rgb("3a2410"), rgb("5a3a1c"), rgb("7c5428"), rgb("9c703a")],
    "cap": [rgb("2a2414"), rgb("4a3e24"), rgb("6a5a36"), rgb("8a7848")],
    "orange": [rgb("7a3404"), rgb("c0580c"), rgb("ec7c1c"), rgb("ffa448")],
    "blue": [rgb("0a2050"), rgb("1a3a8a"), rgb("2e5ac4"), rgb("6a90f0")],
}
THREAD_DARK = rgb("1a1210")
BLACK_FELT = [rgb("040404"), rgb("101012"), rgb("202024")]
PINK = [rgb("a04a5a"), rgb("d8788a"), rgb("f4a8b6")]


def felt(paint, colours, level=0.55, seams=()):
    """Soft felt: a fine fuzz over gentle patches, and seams of stitches (each (x0, y0, x1, y1)) where pieces meet."""
    for y in range(N):
        for x in range(N):
            f = level + 0.1 * (paint.noise(x, y, 12.0) - 0.5) + paint.rng.uniform(-0.05, 0.05)
            paint.put(x, y, ramp(colours, f))
    for x0, y0, x1, y1 in seams:
        paint.line(x0, y0, x1, y1, ramp(colours, level - 0.35), width=2.2, alpha=0.7)
        length = max(abs(x1 - x0), abs(y1 - y0))
        for k in range(0, int(length), 4):
            t = k / max(1, length)
            paint.line(x0 + (x1 - x0) * t, y0 + (y1 - y0) * t, x0 + (x1 - x0) * (t + 1.5 / length), y0 + (y1 - y0) * (t + 1.5 / length),
                       ramp(colours, level + 0.3), width=1.2)


def button_eye(paint, cx, cy, r, colours=BLACK_FELT, shine=(255, 255, 255)):
    paint.blob(cx, cy, r, r, colours[0], colours[2])
    paint.ellipse(cx - r * 0.35, cy - r * 0.35, max(1.2, r * 0.3), max(1.2, r * 0.3), shine)


def stitches(paint, points, colour=THREAD_DARK, width=1.6):
    for (xa, ya), (xb, yb) in zip(points, points[1:]):
        paint.line(xa, ya, xb, yb, colour, width=width)


def plush_textures():
    out = {}
    # The pumpkin: orange felt, its ribs stitched; a jack-o'-lantern face of black felt sewn on.
    for name, grin in (("pumpkin_plush", 1.0), ("jumbo_pumpkin_plush", 1.25)):
        p = Painter(N, N, 26200 + len(name))
        felt(p, FELT["pumpkin"], seams=[(16, 0, 16, 64), (32, 0, 32, 64), (48, 0, 48, 64)])
        out[name] = p.img
        p = Painter(N, N, 26210 + len(name))
        felt(p, FELT["pumpkin"])
        for cx in (20, 44):
            for y in range(14, 28):
                half = (y - 14) * 0.55
                for x in range(int(cx - half), int(cx + half) + 1):
                    p.put(x, y, ramp(BLACK_FELT, 0.4))
        p.blob(32, 32, 3, 3, BLACK_FELT[0], BLACK_FELT[2])
        teeth = [(10, 40), (18, 48), (24, 42), (32, 50), (40, 42), (46, 48), (54, 40)]
        for (xa, ya), (xb, yb) in zip(teeth, teeth[1:]):
            p.line(xa, ya, xb, yb, ramp(BLACK_FELT, 0.4), width=4.0 * grin)
        stitches(p, [(8, 38), (56, 38)], ramp(FELT["pumpkin"], 0.2), width=1.0)
        out[f"{name}_face"] = p.img
    p = Painter(N, N, 26220)
    felt(p, FELT["stem"], seams=[(0, 32, 64, 32)])
    out["plush_stem"] = p.img
    p = Painter(N, N, 26221)
    felt(p, FELT["green"], seams=[(4, 32, 60, 32), (16, 32, 28, 16), (36, 32, 48, 16), (16, 32, 28, 48), (36, 32, 48, 48)])
    out["plush_leaf"] = p.img
    p = Painter(N, N, 26222)
    felt(p, FELT["blue"])
    for r, colours in ((28, FELT["blue"]), (18, GOLD), (10, LACQUER_RED)):
        for y in range(N):
            for x in range(N):
                d = math.hypot(x + 0.5 - 32, y + 0.5 - 32)
                if d <= r:
                    p.put(x, y, ramp(colours, 0.55 + 0.15 * math.sin(math.atan2(y - 32, x - 32) * 12) - 0.2 * d / r))
    out["plush_rosette"] = p.img

    # The ghost: white felt, button eyes, rosy cheeks, a little "o" of a mouth.
    p = Painter(N, N, 26230)
    felt(p, FELT["ghost"], level=0.65, seams=[(0, 58, 64, 58)])
    out["ghost_plush"] = p.img
    p = Painter(N, N, 26231)
    felt(p, FELT["ghost"], level=0.65)
    for cx in (22, 42):
        button_eye(p, cx, 22, 5.5)
        p.ellipse(cx + (-4 if cx < 32 else 4), 34, 5, 3, PINK[1], 0.6)
    p.ellipse(32, 40, 3.5, 4.5, BLACK_FELT[1])
    p.ellipse(32, 40, 2, 3, PINK[0])
    out["ghost_plush_face"] = p.img

    # The bat: near-black felt; big white felt eyes, two tiny fangs; wings of purple felt with scalloped edges.
    p = Painter(N, N, 26240)
    felt(p, FELT["bat"], seams=[(32, 0, 32, 64)])
    out["bat_plush"] = p.img
    p = Painter(N, N, 26241)
    felt(p, FELT["bat"])
    for cx in (20, 44):
        p.ellipse(cx, 28, 9, 10, rgb("f2f0ea"))
        button_eye(p, cx + (2 if cx < 32 else -2), 30, 5)
    stitches(p, [(24, 46), (32, 50), (40, 46)], rgb("e8d0d8"))
    for cx in (27, 37):
        p.tooth(cx, 49, 4, 6, [rgb("c8c0b0"), rgb("f4f0e6"), rgb("ffffff")])
    out["bat_plush_face"] = p.img
    p = Painter(N, N, 26242)
    felt(p, FELT["bat"][1:] + [rgb("6a4c7e")], level=0.5, seams=[(4, 6, 20, 50), (4, 6, 40, 52), (4, 6, 60, 46)])
    for k in range(4):
        cx = 8 + k * 16
        for y in range(N - 12, N):
            for x in range(cx - 8, cx + 8):
                if (x - cx) ** 2 + (y - N - 2) ** 2 < 100 and 0 <= x < N:
                    p.px[x, y] = (0, 0, 0, 0)
    out["bat_plush_wing"] = p.img

    # The black cat: black felt, big green-gold eyes, a pink nose, stitched whiskers; an orange bow.
    p = Painter(N, N, 26250)
    felt(p, FELT["cat"], seams=[(32, 0, 32, 64)])
    out["black_cat_plush"] = p.img
    p = Painter(N, N, 26251)
    felt(p, FELT["cat"])
    for cx in (20, 44):
        p.ellipse(cx, 26, 8, 7, rgb("c8d43a"))
        p.ellipse(cx, 26, 2.2, 6, BLACK_FELT[0])
        p.ellipse(cx - 3, 23, 1.6, 1.6, rgb("ffffff"))
    p.tooth(32, 38, 6, 5, PINK)
    stitches(p, [(32, 42), (28, 46)], PINK[0])
    stitches(p, [(32, 42), (36, 46)], PINK[0])
    for side in (-1, 1):
        for k in range(3):
            stitches(p, [(32 + side * 10, 40 + k * 3), (32 + side * 26, 36 + k * 5)], rgb("d8d8d8"), width=1.0)
    out["black_cat_plush_face"] = p.img
    p = Painter(N, N, 26252)
    felt(p, FELT["orange"])
    for y in range(4, N, 12):
        for x in range(4 + (y // 12) % 2 * 6, N, 12):
            p.ellipse(x, y, 2.5, 2.5, rgb("fff0d8"))
    out["plush_bow"] = p.img

    # The squirrel: rust felt with a cream muzzle, a bushy painted tail; an acorn held in front.
    p = Painter(N, N, 26260)
    felt(p, FELT["squirrel"], seams=[(32, 0, 32, 64)])
    out["squirrel_plush"] = p.img
    p = Painter(N, N, 26261)
    felt(p, FELT["squirrel"])
    p.ellipse(32, 46, 16, 12, ramp(FELT["cream"], 0.6))
    for cx in (20, 44):
        button_eye(p, cx, 26, 5)
    p.blob(32, 38, 4, 3, PINK[0], PINK[2])
    stitches(p, [(28, 46), (32, 44), (36, 46)])
    out["squirrel_plush_face"] = p.img
    p = Painter(N, N, 26262)
    p.shade((0, 0, N, N), FELT["squirrel"], 0.5, spread=0.3, light="centre")
    p.locks((0, 0, N, N), FELT["squirrel"], 0.6, flow=(0.0, -1.0), density=1.2, length=(10, 18), width=(4.0, 7.0))
    p.locks((0, 0, N, N), FELT["cream"], 0.7, flow=(0.0, -1.0), density=0.15, length=(6, 10), width=(2.0, 4.0))
    out["squirrel_plush_tail"] = p.img
    p = Painter(N, N, 26263)
    felt(p, FELT["acorn"], level=0.6)
    p.ellipse(20, 20, 8, 12, ramp(FELT["acorn"], 0.95), 0.5)
    out["plush_acorn"] = p.img
    p = Painter(N, N, 26264)
    felt(p, FELT["cap"])
    for y in range(0, N, 6):
        for x in range(0 + (y // 6) % 2 * 3, N, 6):
            p.ellipse(x, y, 2, 2, ramp(FELT["cap"], 0.15))
    out["plush_acorn_cap"] = p.img

    # The werewolf: brown felt, a tan muzzle with a black nose and two felt fangs, yellow eyes under cross brows.
    p = Painter(N, N, 26270)
    felt(p, FELT["werewolf"], seams=[(32, 0, 32, 64)])
    out["werewolf_plush"] = p.img
    p = Painter(N, N, 26271)
    felt(p, FELT["werewolf"])
    for cx, side in ((20, -1), (44, 1)):
        p.ellipse(cx, 30, 7, 6, rgb("f0c428"))
        p.ellipse(cx, 31, 2.5, 4, BLACK_FELT[0])
        p.ellipse(cx - 2, 28, 1.4, 1.4, rgb("ffffff"))
        stitches(p, [(cx - side * 9, 18), (cx + side * 7, 24)], BLACK_FELT[1], width=3.0)
    for cx in (12, 52):
        p.ellipse(cx, 50, 6, 4, ramp(FELT["tan"], 0.6))
    out["werewolf_plush_face"] = p.img
    p = Painter(N, N, 26272)
    felt(p, FELT["tan"])
    out["werewolf_plush_muzzle"] = p.img
    p = Painter(N, N, 26273)
    felt(p, FELT["tan"])
    p.blob(32, 18, 14, 10, BLACK_FELT[0], BLACK_FELT[2])
    p.ellipse(27, 15, 3, 2, rgb("6a6a70"))
    stitches(p, [(32, 28), (32, 40)])
    stitches(p, [(18, 42), (32, 40), (46, 42)])
    for cx in (22, 42):
        p.tooth(cx, 44, 6, 12, [rgb("c8c0b0"), rgb("f4f0e6"), rgb("ffffff")])
    out["werewolf_plush_snout"] = p.img
    return out


def items():
    out = {}
    # The High Striker: its tower in red and white, lamps up its front, the bell on top, on its base.
    p = Painter(N, N, 26300)
    for y in range(10, 54):
        for x in range(26, 38):
            p.put(x, y, ramp(WOOD_WHITE if (y // 6) % 2 else LACQUER_RED, 0.75 - 0.3 * abs(x - 31.5) / 6))
    for y in range(14, 52, 6):
        for cx in (22, 42):
            p.blob(cx, y, 2.2, 2.2, rgb("c08a14"), rgb("fff0a0"))
    p.blob(32, 7, 7, 6, BRASS[1], BRASS[4])
    for y in range(54, 62):
        for x in range(12, 52):
            p.put(x, y, ramp(LACQUER_RED, 0.7 - 0.3 * (y - 54) / 8))
    p.blob(32, 44, 4, 2.5, LACQUER_RED[1], LACQUER_RED[4])
    out["high_striker"] = p.img

    # The Carnival Mallet: a big wooden head banded in red, on a long handle, held aslant.
    p = Painter(N, N, 26301)
    for k in range(0, 40):
        x, y = 12 + k * 0.85, 56 - k * 0.85
        p.line(x - 2, y - 2, x + 2, y + 2, ramp(PLANK, 0.55 + 0.25 * math.sin(k * 0.3)), width=2.0)
    for k2 in range(-28, 29):
        for j2 in range(-16, 17):
            k, j = k2 / 2.0, j2 / 2.0
            x = 44 + k * 0.7 + j * 0.7
            y = 18 - k * 0.7 + j * 0.7
            band = abs(k) > 10 or abs(k) < 2
            p.put(x, y, ramp(LACQUER_RED if band else WOOD_WHITE, 0.75 - 0.025 * (j + k)))
    out["carnival_mallet"] = p.img

    # The Toss Ring: a striped ring, seen tilted.
    p = Painter(N, N, 26302)
    for a in range(360):
        t = math.radians(a)
        for r in (17, 18, 19, 20, 21):
            x, y = 32 + r * math.cos(t), 32 + r * 0.62 * math.sin(t)
            colour = [LACQUER_RED, YELLOW][(a // 30) % 2]
            p.put(x, y, ramp(colour, 0.7 - 0.25 * math.sin(t) + 0.04 * (r - 19)))
    out["toss_ring"] = p.img
    return out


def midway_textures():
    out = {}
    for name, img in {**striker_textures(), **ring_toss_textures(), **plush_textures()}.items():
        out[("block", name)] = img
    for name, img in items().items():
        out[("item", name)] = img
    return out

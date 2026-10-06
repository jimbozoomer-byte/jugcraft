"""Original textures for the Ferris wheel (fall addition 27), painted at 64 by 64 (docs/ART_DIRECTION.md, "High
resolution"): the A-frames' lattice steel (cut out between its bracing), the cream-enamelled steel of its braces, spokes
and bearings, the red rims with their gold pinstripe, the hub's brass sunburst, the axle, the footings and the bulbs; the
cars in pumpkin, cranberry, mustard and spruce, each with a striped canopy and a scalloped valance to match, the tufted
bench and plank floor; the booth's painted sides, its control panel, deck and speed gauge; and the item.

A texture is drawn whole onto each face of a part, so the long parts (legs, spokes, rim lengths) see it stretched
along their length; the patterns here run along or across for that.

Called from crop_textures.crop_textures(). Every pixel is drawn here by code from a fixed seed (tools/fur_paint.py's
painter); no other texture is read, traced or recoloured.
"""
import math

from fur_paint import mix, clean_painter as Painter, clean_ramp as ramp
from crop_textures import rgb
from ferris_wheel import CAR_COLOURS

N = 64

CREAM = [rgb("6e6656"), rgb("a49a84"), rgb("cfc6ae"), rgb("e9e2cc"), rgb("f9f5e8")]
RED = [rgb("3e0808"), rgb("6c1010"), rgb("9c1a16"), rgb("c42a22"), rgb("e45444")]
GOLD = [rgb("5a3c0a"), rgb("8c6418"), rgb("bc9030"), rgb("e0bc5a"), rgb("fbe7a4")]
BRASS = [rgb("4a3010"), rgb("7a5420"), rgb("a8803a"), rgb("d0aa5c"), rgb("f0d898"), rgb("fff4d4")]
RUST = [rgb("3a1a0a"), rgb("6a3014"), rgb("8e4a22")]
IRON = [rgb("16181c"), rgb("2a2e34"), rgb("444a52"), rgb("666e78"), rgb("9aa2ac")]
CONCRETE = [rgb("5a5852"), rgb("7a776e"), rgb("989488"), rgb("b4b0a2"), rgb("ccc8ba")]
PLANK = [rgb("3a2416"), rgb("5c3c24"), rgb("7e5634"), rgb("9c7048"), rgb("b88c60")]
LEATHER = [rgb("2a0a08"), rgb("4a1410"), rgb("6c2218"), rgb("8e3426"), rgb("b05040")]
GREEN = [rgb("0e2a1a"), rgb("1a4428"), rgb("2a5e38"), rgb("40804e"), rgb("6aa874")]
COLOURS = {
    "pumpkin": [rgb("5a2204"), rgb("923a08"), rgb("c85a10"), rgb("ec7c1e"), rgb("ffa850")],
    "cranberry": [rgb("3a0612"), rgb("640c20"), rgb("8e1630"), rgb("b42642"), rgb("d8506a")],
    "mustard": [rgb("5a4206"), rgb("8c6a0c"), rgb("bc9216"), rgb("dcb22c"), rgb("f4d468")],
    "spruce": [rgb("0a2418"), rgb("123c28"), rgb("1c5638"), rgb("2c744c"), rgb("54a070")],
}


def enamel(p, colours, level=0.6, streak=0.06, grime=0.1):
    """A painted metal face: even paint, faint streaks along it, a little grime gathered at its ends."""
    for y in range(N):
        for x in range(N):
            f = level + streak * (p.noise(x * 4, y * 0.5, 9.0) - 0.5) + 0.05 * (p.noise(x, y, 4.0) - 0.5)
            f -= grime * max(0.0, abs(y - N / 2) / (N / 2) - 0.75) * 4
            p.put(x, y, ramp(colours, f))


def rivet(p, x, y, colours, r=1.6):
    p.blob(x, y, r, r, ramp(colours, 0.25), ramp(colours, 0.95))


def lattice():
    """Lattice steel seen down a leg: two chords down its sides, battens across, and a zigzag of bracing between them;
    the gaps cut out."""
    p = Painter(N, N, 27001)
    chord = 9
    for y in range(N):
        for x in list(range(chord)) + list(range(N - chord, N)):
            edge = min(x, N - 1 - x)
            f = 0.62 + 0.15 * (1 - edge / chord) * (1 if x < chord else -1) + 0.05 * (p.noise(x, y, 5.0) - 0.5)
            p.put(x, y, ramp(CREAM, f))
    for yb in (0, 31, 63):
        for y in range(max(0, yb - 2), min(N, yb + 3)):
            for x in range(N):
                p.put(x, y, ramp(CREAM, 0.58 + 0.08 * (yb - y) / 3))
    for y0 in (0, 32):
        for start, end in (((chord - 1, y0 + 1), (N - chord, y0 + 30)), ((N - chord, y0 + 1), (chord - 1, y0 + 30))):
            p.line(start[0], start[1], end[0], end[1], ramp(CREAM, 0.55), width=5)
            p.line(start[0], start[1] - 1, end[0], end[1] - 1, ramp(CREAM, 0.8), width=1.5)
    for yb in (0, 31, 63):
        for x in (4, N - 5):
            rivet(p, x, min(N - 2, max(1, yb)), CREAM)
    # Rust weeping from the joints.
    for yb in (31,):
        for x in (6, N - 7):
            for k in range(6):
                p.put(x + p.rng.randint(-1, 1), yb + 2 + k, RUST[1], 0.35 - k * 0.05)
    return p.img


def steel():
    p = Painter(N, N, 27002)
    enamel(p, CREAM, 0.62)
    for y in (6, N - 7):
        for x in (8, N // 2, N - 9):
            rivet(p, x, y, CREAM, 1.8)
    return p.img


def rim():
    """A rim's length, seen along it: red paint, a gold pinstripe along its middle, rivets along it, its edges darker."""
    p = Painter(N, N, 27003)
    for y in range(N):
        for x in range(N):
            edge = abs(y - N / 2) / (N / 2)
            f = 0.6 - 0.25 * edge ** 3 + 0.05 * (p.noise(x, y * 0.3, 7.0) - 0.5)
            p.put(x, y, ramp(RED, f))
    for y in range(29, 35):
        for x in range(N):
            p.put(x, y, ramp(GOLD, 0.75 - 0.08 * abs(y - 31.5)))
    for x in range(4, N, 10):
        rivet(p, x + 2, 12, RED, 2.2)
        rivet(p, x + 2, N - 13, RED, 2.2)
    return p.img


def hub():
    """The hub's face: a brass sunburst of sixteen rays on red, round a domed brass boss with a bolt circle."""
    p = Painter(N, N, 27004)
    c = N / 2
    for y in range(N):
        for x in range(N):
            dx, dy = x + 0.5 - c, y + 0.5 - c
            d = math.hypot(dx, dy)
            a = math.atan2(dy, dx)
            ray = math.cos(a * 16) > 0.2
            if d < 30:
                base = ramp(GOLD, 0.55 + 0.25 * math.cos(a * 16) - 0.2 * d / 30) if ray else ramp(RED, 0.45 + 0.2 * (1 - d / 30))
            else:
                base = ramp(CREAM, 0.55)
            p.put(x, y, base)
    for y in range(N):
        for x in range(N):
            d = math.hypot(x + 0.5 - c, y + 0.5 - c)
            if 29 <= d < 31.5:
                p.put(x, y, ramp(BRASS, 0.55))
    p.blob(c, c, 11, 11, ramp(BRASS, 0.2), ramp(BRASS, 1.0))
    for k in range(8):
        a = k * math.pi / 4
        rivet(p, c + 15 * math.cos(a), c + 15 * math.sin(a), IRON, 1.8)
    p.glint(26, 26)
    return p.img


def axle():
    p = Painter(N, N, 27005)
    for y in range(N):
        for x in range(N):
            f = 0.45 + 0.3 * math.cos((x / N) * math.pi * 2) + 0.04 * (p.noise(x, y * 0.2, 6.0) - 0.5)
            p.put(x, y, ramp(IRON, f))
    for y in range(0, N, 16):
        for x in range(N):
            p.put(x, y, ramp(IRON, 0.2))
    return p.img


def footing():
    p = Painter(N, N, 27006)
    for y in range(N):
        for x in range(N):
            p.put(x, y, ramp(CONCRETE, 0.55 + 0.12 * (p.noise(x, y, 6.0) - 0.5) + 0.06 * (p.noise(x, y, 2.0) - 0.5)))
    for x, y in ((12, 12), (N - 13, 12), (12, N - 13), (N - 13, N - 13)):
        p.blob(x, y, 4, 4, ramp(IRON, 0.2), ramp(IRON, 0.85))
        p.put(x, y, IRON[0])
    return p.img


def brass():
    p = Painter(N, N, 27007)
    for y in range(N):
        for x in range(N):
            f = 0.5 + 0.35 * math.cos((x / N) * math.pi * 2.4 + 0.7) + 0.06 * (p.noise(x, y, 8.0) - 0.5)
            p.put(x, y, ramp(BRASS, f))
    return p.img


def bulb():
    """A lamp bulb: white-hot at its middle, warm amber at its rim, in a brass collar."""
    p = Painter(N, N, 27008)
    glass = [rgb("a05a10"), rgb("e09028"), rgb("ffc850"), rgb("fff0a0"), rgb("ffffff")]
    for y in range(N):
        for x in range(N):
            d = math.hypot(x + 0.5 - N / 2, y + 0.5 - N / 2) / (N / 2)
            if d > 0.86:
                p.put(x, y, ramp(BRASS, 0.5 + 0.3 * (1 - d)))
            else:
                p.put(x, y, ramp(glass, 1.0 - 0.75 * d))
    p.ellipse(24, 22, 6, 4, (255, 255, 255), 0.7)
    return p.img


def car(colour):
    """A car's painted panel: the colour, lighter to the top, with a cream pinstripe inset round it, a cream cartouche in
    the middle with a maple leaf in the colour, and a brass beading along the top."""
    paint = COLOURS[colour]
    p = Painter(N, N, 27010 + CAR_COLOURS.index(colour))
    for y in range(N):
        for x in range(N):
            p.put(x, y, ramp(paint, 0.68 - 0.3 * y / N + 0.05 * (p.noise(x, y, 7.0) - 0.5)))
    for i in range(N):
        for k in range(2):
            for x, y in ((i, 7 + k), (i, N - 9 - k), (6 + k, i), (N - 8 - k, i)):
                if 6 <= x <= N - 7 and 7 <= y <= N - 8:
                    p.put(x, y, ramp(CREAM, 0.8))
    for y in range(0, 4):
        for x in range(N):
            p.put(x, y, ramp(BRASS, 0.75 - 0.1 * y))
    c = N / 2
    p.ellipse(c, c, 9, 18, ramp(CREAM, 0.85))
    p.ellipse(c, c, 8, 16.5, ramp(CREAM, 0.95))
    # A maple leaf: five lobes, drawn tall as the panel is squeezed down onto the car.
    leaf = ramp(paint, 0.45)
    for k, (lx, ly, r) in enumerate(((0, -9, 3.0), (-4.5, -4, 2.6), (4.5, -4, 2.6), (-3, 3, 2.2), (3, 3, 2.2), (0, -2, 3.2))):
        p.ellipse(c + lx, c + ly, r, r * 1.9, leaf)
    p.line(c, c + 2, c, c + 12, leaf, width=1.2)
    return p.img


def canopy(colour):
    """The canopy: stripes of the colour and cream, shaded from the peak."""
    paint = COLOURS[colour]
    p = Painter(N, N, 27020 + CAR_COLOURS.index(colour))
    for y in range(N):
        for x in range(N):
            stripe = (x // 8) % 2 == 0
            f = 0.65 - 0.2 * abs(y - N / 2) / (N / 2) + 0.04 * (p.noise(x, y, 6.0) - 0.5)
            p.put(x, y, ramp(paint if stripe else CREAM, f))
            if x % 8 == 0:
                p.put(x, y, ramp(paint, 0.3), 0.5)
    return p.img


def valance(colour):
    """The valance hanging round the canopy: the stripes down to a scalloped edge, cut out below it, and a gold braid
    along its top."""
    paint = COLOURS[colour]
    p = Painter(N, N, 27030 + CAR_COLOURS.index(colour))
    for y in range(N):
        for x in range(N):
            stripe = (x // 8) % 2 == 0
            u = (x % 16) / 16.0
            bottom = 34 + 24 * math.sqrt(max(0.0, 1 - (2 * u - 1) ** 2))
            if y < bottom:
                p.put(x, y, ramp(paint if stripe else CREAM, 0.62 - 0.15 * y / N))
            if bottom - 4 <= y < bottom:
                p.put(x, y, ramp(GOLD, 0.7))
    for y in range(0, 8):
        for x in range(N):
            p.put(x, y, ramp(GOLD, 0.55 + 0.25 * math.sin(x * 0.8 + y)))
    return p.img


def seat():
    """Tufted oxblood leather: buttons in rows, the leather puckered round each."""
    p = Painter(N, N, 27040)
    for y in range(N):
        for x in range(N):
            bx, by = (x % 16) - 8, (y % 21) - 10
            d = math.hypot(bx, by * 0.75)
            p.put(x, y, ramp(LEATHER, 0.6 - 0.25 * math.exp(-d * d / 18) + 0.05 * (p.noise(x, y, 5.0) - 0.5)))
    for y in range(10, N, 21):
        for x in range(8, N, 16):
            p.blob(x, y, 1.8, 1.8, ramp(BRASS, 0.2), ramp(BRASS, 0.9))
    return p.img


def floor():
    p = Painter(N, N, 27041)
    for y in range(N):
        for x in range(N):
            board = x // 16
            seam = x % 16 == 0
            f = 0.55 + 0.06 * math.sin(y * 0.3 + board * 2.0 + 3 * p.noise(x, y, 6.0)) - (0.3 if seam else 0.0)
            p.put(x, y, ramp(PLANK, f))
    return p.img


def booth_side():
    """The booth's side: a cream panel in a red frame, a gold sunburst rising from its foot."""
    p = Painter(N, N, 27050)
    for y in range(N):
        for x in range(N):
            frame = x < 6 or x >= N - 6 or y < 6 or y >= N - 6
            p.put(x, y, ramp(RED if frame else CREAM, (0.55 if frame else 0.7) + 0.05 * (p.noise(x, y, 6.0) - 0.5)))
    c = N / 2
    for y in range(6, N - 6):
        for x in range(6, N - 6):
            dx, dy = x + 0.5 - c, (N - 6) - (y + 0.5)
            a = math.atan2(dy, dx)
            d = math.hypot(dx, dy)
            if d < 26 and math.cos(a * 14) > 0.35:
                p.put(x, y, ramp(GOLD, 0.7 - 0.2 * d / 26))
    p.blob(c, N - 7, 7, 5, ramp(GOLD, 0.35), ramp(GOLD, 1.0))
    for x, y in ((3, 3), (N - 4, 3), (3, N - 4), (N - 4, N - 4)):
        rivet(p, x, y, BRASS, 1.6)
    return p.img


def booth_front():
    """The booth's front: a riveted steel control panel in a red frame, a brass plate across its top."""
    p = Painter(N, N, 27051)
    for y in range(N):
        for x in range(N):
            frame = x < 5 or x >= N - 5 or y < 5 or y >= N - 5
            if frame:
                p.put(x, y, ramp(RED, 0.55 + 0.05 * (p.noise(x, y, 6.0) - 0.5)))
            else:
                p.put(x, y, ramp(GREEN, 0.55 - 0.15 * y / N + 0.05 * (p.noise(x, y, 5.0) - 0.5)))
    for x in range(10, N - 10):
        for y in range(9, 16):
            p.put(x, y, ramp(BRASS, 0.8 - 0.05 * (y - 9)))
    for x in range(8, N - 8, 8):
        rivet(p, x, N - 9, GREEN, 1.4)
    return p.img


def booth_deck():
    """The deck: planks with a yellow and black safety edge."""
    p = Painter(N, N, 27052)
    for y in range(N):
        for x in range(N):
            board = y // 13
            f = 0.55 + 0.06 * math.sin(x * 0.3 + board * 1.7 + 3 * p.noise(x, y, 6.0)) - (0.3 if y % 13 == 0 else 0.0)
            p.put(x, y, ramp(PLANK, f))
            edge = min(x, y, N - 1 - x, N - 1 - y)
            if edge < 5:
                p.put(x, y, (240, 196, 40) if ((x + y) // 6) % 2 == 0 else (30, 28, 26))
    return p.img


def gauge():
    """The speed gauge: a cream dial with its marks, a red zone at the top of its sweep and a black needle, in brass."""
    p = Painter(N, N, 27053)
    c = N / 2
    for y in range(N):
        for x in range(N):
            d = math.hypot(x + 0.5 - c, y + 0.5 - c)
            if d > 28:
                p.put(x, y, ramp(BRASS, 0.5 + 0.3 * (y < c)))
            elif d > 25:
                p.put(x, y, ramp(BRASS, 0.85))
            else:
                p.put(x, y, ramp(CREAM, 0.9))
    for k in range(11):
        a = math.radians(225 - k * 27)
        colour = RED[2] if k >= 8 else IRON[0]
        p.line(c + 17 * math.cos(a), c - 17 * math.sin(a), c + 23 * math.cos(a), c - 23 * math.sin(a), colour, width=2 if k % 5 == 0 else 1)
    a = math.radians(225 - 6 * 27)
    p.line(c, c, c + 20 * math.cos(a), c - 20 * math.sin(a), IRON[0], width=2)
    p.blob(c, c, 3, 3, ramp(BRASS, 0.2), ramp(BRASS, 0.9))
    return p.img


def item():
    """The item: a little Ferris wheel on its A-frame over its booth, its cars in their colours."""
    p = Painter(N, N, 27060)
    c, hub_y, r = N / 2, 26, 21
    for side in (-1, 1):
        p.line(c + side * 17, 60, c + side * 1, hub_y, ramp(CREAM, 0.45), width=3)
        p.line(c + side * 17, 60, c + side * 1, hub_y, ramp(CREAM, 0.8), width=1.2)
    for k in range(16):
        a = k * math.pi / 8
        p.line(c, hub_y, c + r * math.cos(a), hub_y + r * math.sin(a), ramp(CREAM, 0.7), width=1)
    for y in range(N):
        for x in range(N):
            d = math.hypot(x + 0.5 - c, y + 0.5 - hub_y)
            if r - 1.6 <= d <= r + 1.6:
                p.put(x, y, ramp(RED, 0.75 - 0.3 * (d - r + 1.6) / 3.2))
    for k in range(16):
        a = k * math.pi / 8
        p.put(c + r * math.cos(a), hub_y + r * math.sin(a), (255, 236, 150))
    p.blob(c, hub_y, 3.5, 3.5, ramp(GOLD, 0.3), ramp(GOLD, 1.0))
    for k in range(8):
        a = -math.pi / 2 + k * math.pi / 4
        x, y = c + r * math.cos(a), hub_y - r * math.sin(a)
        paint = COLOURS[CAR_COLOURS[k % 4]]
        p.line(x, y, x, y + 3, ramp(BRASS, 0.6), width=1)
        for yy in range(int(y + 3), int(y + 8)):
            for xx in range(int(x - 3), int(x + 4)):
                p.put(xx, yy, ramp(paint, 0.85 - 0.4 * (yy - y - 3) / 5))
    for y in range(56, 63):
        for x in range(int(c - 7), int(c + 7)):
            p.put(x, y, ramp(RED if y > 57 else PLANK, 0.6))
    return p.img


def ferris_wheel_textures():
    blocks = {"ferris_wheel_lattice": lattice(), "ferris_wheel_steel": steel(), "ferris_wheel_rim": rim(), "ferris_wheel_hub": hub(),
              "ferris_wheel_axle": axle(), "ferris_wheel_footing": footing(), "ferris_wheel_brass": brass(), "ferris_wheel_bulb": bulb(),
              "ferris_wheel_seat": seat(), "ferris_wheel_floor": floor(), "ferris_wheel_booth_side": booth_side(),
              "ferris_wheel_booth_front": booth_front(), "ferris_wheel_booth_deck": booth_deck(), "ferris_wheel_gauge": gauge()}
    for colour in CAR_COLOURS:
        blocks[f"ferris_wheel_car_{colour}"] = car(colour)
        blocks[f"ferris_wheel_canopy_{colour}"] = canopy(colour)
        blocks[f"ferris_wheel_valance_{colour}"] = valance(colour)
    out = {("block", name): img for name, img in blocks.items()}
    out[("item", "ferris_wheel")] = item()
    return out

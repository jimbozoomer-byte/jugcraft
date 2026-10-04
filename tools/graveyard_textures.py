"""Original textures for the graveyard pack (requires Pillow): the four memorial stones (white marble, dark slate,
speckled granite, warm sandstone) at each of their four stages of weathering (clean, worn, mossy, overgrown), their
carved and polished faces, rock-faced granite, Celtic knotwork, ivy, and the Stonemason's Chisel; for the buildings of
pack 3, weathering oak and roof slates, stained glass, a chequered marble floor, lamp and lantern glass, a bronze door's
smoked glass and the Bronze Mausoleum Door's item.

Weathering is drawn the way stone weathers: worn stone is duller, with grime run down from the top and chipped arrises;
mossy stone has moss creeping up from the ground and lichen rosettes; overgrown stone is green to a third of its height
and crusted with lichen. Block faces take their texture by position, so the bottom rows of a texture lie along the
ground: each stage has an `_upper` form, without the band of ground moss, for the parts of a monument above its first
block.

Called from crop_textures.crop_textures(). Every pixel is drawn here by code, from fixed seeds; no Mojang texture is
read, traced or recoloured.
"""
import random

from PIL import Image

from crop_textures import Canvas, rgb, outline
from halloween_textures import shade

STAGES = ("clean", "worn", "mossy", "overgrown")

# name: (base colours darkest to lightest, accent colours, seed)
STONES = {
    "marble": ([rgb("d6d4ce"), rgb("dddbd5"), rgb("e4e2dc"), rgb("eae8e2")], [rgb("c2c0bb"), rgb("cdcbc5")], 7101),
    "slate": ([rgb("3c424b"), rgb("444a54"), rgb("4b525c"), rgb("545b66")], [rgb("353a42"), rgb("5d6570")], 7102),
    "granite": ([rgb("8a8280"), rgb("99908d"), rgb("a69d99"), rgb("b2aaa5")], [rgb("3e3b3c"), rgb("c49d92"), rgb("d8d2cc")], 7103),
    "sandstone": ([rgb("b99e6c"), rgb("c7ad7a"), rgb("d3ba88"), rgb("dcc596")], [rgb("a88c5c"), rgb("e4d0a6")], 7104),
}
MOSS = [rgb("2f4a1c"), rgb("3e5e22"), rgb("4f7429"), rgb("63883a"), rgb("7a9c48")]
LICHEN = {"marble": [rgb("b8c08a"), rgb("a6b07a")], "slate": [rgb("a9b39a"), rgb("c6ccb4")],
          "granite": [rgb("d9a548"), rgb("c88a3a")], "sandstone": [rgb("cf8f3a"), rgb("b9c07e")]}
GRIME = rgb("3a3631")
IVY = [rgb("1f3a14"), rgb("2b4d1a"), rgb("3a6322"), rgb("4c7a2c"), rgb("67953a")]


def _base(stone, seed_extra=0, smooth=False):
    """The bare stone of `stone`: marble with grey veins, slate in fine laminations, granite speckled with black mica,
    pink feldspar and white quartz, sandstone in thin bedding planes."""
    base, accent, seed = STONES[stone]
    rng = random.Random(seed + seed_extra)
    c = Canvas()
    for y in range(16):
        for x in range(16):
            weights = (1, 3, 4, 2) if not smooth else (0, 2, 5, 3)
            colour = rng.choices(base, weights=weights)[0]
            if stone == "slate" and (y + (x // 6)) % 4 == 0:
                colour = shade(colour, 0.93)
            if stone == "sandstone" and y % 5 == 2:
                colour = shade(colour, 0.95 if not smooth else 0.97)
            c.px(x, y, colour)
    if stone == "marble":
        # One or two soft veins wandering across the face, broken here and there.
        for start in (rng.randint(-2, 8), rng.randint(9, 20))[: 1 if smooth else 2]:
            x = float(start)
            for y in range(16):
                x += rng.choice((-0.6, 0.3, 0.7, 1.0))
                if rng.random() < 0.75:
                    c.px(int(x) % 16, y, accent[1] if rng.random() < 0.8 else accent[0])
    elif stone == "granite":
        for _ in range(26 if not smooth else 18):
            c.px(rng.randrange(16), rng.randrange(16), rng.choice(accent))
    elif stone == "slate":
        for _ in range(8):
            c.px(rng.randrange(16), rng.randrange(16), accent[1])
    elif stone == "sandstone":
        for _ in range(10):
            c.px(rng.randrange(16), rng.randrange(16), rng.choice(accent))
    return c


def _weather(c, stone, stage, seed, upper=False):
    """Weathers the canvas to `stage`: grime from the top, chips, moss from the ground up and lichen."""
    if stage == 0:
        return c
    rng = random.Random(seed * 7 + stage * 131 + (1 if upper else 0))
    dull = (1.0, 0.95, 0.9, 0.84)[stage]
    for y in range(16):
        for x in range(16):
            pixel = c.get(x, y)
            if pixel:
                c.px(x, y, shade(pixel[:3], dull))
    # Grime washed down from the top in streaks.
    for _ in range(3 + 2 * stage):
        x = rng.randrange(16)
        length = rng.randint(3, 6 + 2 * stage)
        for y in range(length):
            if rng.random() < 0.85:
                pixel = c.get(x, y)
                if pixel:
                    c.px(x, y, tuple(int(p * 0.75 + g * 0.25) for p, g in zip(pixel[:3], GRIME)))
    # Chips: small pale scars.
    for _ in range(stage):
        x, y = rng.randrange(16), rng.randrange(16)
        pixel = c.get(x, y)
        if pixel:
            c.px(x, y, shade(pixel[:3], 1.12))
    if stage >= 2:
        # Lichen rosettes.
        lichen = LICHEN[stone]
        for _ in range(3 if stage == 2 else 6):
            cx, cy = rng.randrange(16), rng.randrange(16)
            for dx, dy in ((0, 0), (1, 0), (0, 1), (-1, 0), (0, -1)):
                if rng.random() < 0.8:
                    c.px(cx + dx, cy + dy, rng.choice(lichen))
        # Moss in cushions, and from the ground up unless this is an upper part.
        for _ in range(2 if stage == 2 else 4):
            cx, cy = rng.randrange(16), rng.randrange(4, 16)
            radius = rng.choice((1, 1, 2))
            for dy in range(-radius, radius + 1):
                for dx in range(-radius - 1, radius + 2):
                    if dx * dx / (radius + 1.2) ** 2 + dy * dy / (radius + 0.6) ** 2 <= 1 and rng.random() < 0.85:
                        c.px(cx + dx, cy + dy, MOSS[2 + (dy < 0) + (rng.random() < 0.3)])
        if not upper:
            height = 2 if stage == 2 else 5
            level = height
            for x in range(16):
                level = max(1, min(height + 2, level + rng.choice((-1, 0, 0, 1))))
                for y in range(16 - level, 16):
                    depth = 15 - y
                    c.px(x, y, MOSS[min(4, max(0, 1 + (depth < level - 1) + (rng.random() < 0.25) - (depth == 0)))])
    return c


def stone_face(stone, stage, upper=False):
    return _weather(_base(stone), stone, STAGES.index(stage), STONES[stone][2], upper).img


def relief_face(stone, stage, upper=False):
    """Carved and polished work: smoother than the dressed face. Marble is polished almost white, slate shows the
    pale grey of fresh-cut slate, granite polishes dark with fine specks, sandstone is rubbed smooth."""
    base, accent, seed = STONES[stone]
    rng = random.Random(seed + 50)
    c = Canvas()
    for y in range(16):
        for x in range(16):
            if stone == "marble":
                colour = shade(rng.choice(base[2:]), 1.03)
            elif stone == "slate":
                colour = shade(rng.choice(base[1:3]), 1.38)
            elif stone == "granite":
                colour = rng.choices([rgb("6e6766"), rgb("766e6c"), rgb("7d7573")], weights=(2, 5, 3))[0]
            else:
                colour = shade(rng.choice(base[1:]), 1.03)
            c.px(x, y, colour)
    if stone == "granite":
        for _ in range(14):
            c.px(rng.randrange(16), rng.randrange(16), rng.choice((rgb("2e2b2c"), rgb("a8857c"), rgb("b9b2ac"))))
    elif stone == "marble":
        x = float(rng.randint(2, 12))
        for y in range(16):
            x += rng.choice((-0.5, 0.3, 0.6))
            if rng.random() < 0.5:
                c.px(int(x) % 16, y, accent[1])
    return _weather(c, stone, STAGES.index(stage), STONES[stone][2] + 3, upper).img


def rough_face(stone, stage, upper=False):
    """Rock-faced stone, split rather than sawn: lumps lit from above with shadowed undersides, and pits."""
    c = _base(stone, 90)
    rng = random.Random(STONES[stone][2] + 91)
    for _ in range(9):
        cx, cy, r = rng.randrange(16), rng.randrange(16), rng.choice((1, 2, 2))
        for dy in range(-r, r + 1):
            for dx in range(-r, r + 1):
                if dx * dx + dy * dy <= r * r + 1:
                    pixel = c.get((cx + dx) % 16, (cy + dy) % 16)
                    factor = 1.12 if dy < 0 else (0.82 if dy == r else 1.0)
                    c.px((cx + dx) % 16, (cy + dy) % 16, shade(pixel[:3], factor))
    for _ in range(12):
        x, y = rng.randrange(16), rng.randrange(16)
        c.px(x, y, shade(c.get(x, y)[:3], 0.7))
    return _weather(c, stone, STAGES.index(stage), STONES[stone][2] + 9, upper).img


def knotwork(stone, stage, upper=False):
    """A plait of two strands crossing on the diagonal, carved as raised bands with a dark hollow either side; at the
    middle the strand falling to the right passes over, at the corners the other (the face of a Celtic cross)."""
    c = _base(stone, 120, smooth=True)
    base = STONES[stone][0]
    hollow = shade(base[0], 0.78)
    edge = shade(base[1], 0.88)
    band = [shade(base[2], 1.06), shade(base[3], 1.1)]

    def dist(x, y, sign):
        """Distance (pixels) from (x, y) to the nearest strand of one direction, tiling every 16 pixels."""
        u = (x - y) if sign > 0 else (x + y - 15)
        u = (u + 8) % 16 - 8
        return abs(u) / 1.414

    for y in range(16):
        for x in range(16):
            px, py = x + 0.5, y + 0.5
            a, b = dist(px, py, 1), dist(px, py, -1)
            centre = abs(px - 8) < 4 and abs(py - 8) < 4
            over = a if centre else b
            under = b if centre else a
            if over <= 1.3:
                c.px(x, y, band[(x + y) % 2])
            elif over <= 1.9:
                c.px(x, y, edge)
            elif under <= 1.3:
                c.px(x, y, band[0])
            elif under <= 1.9:
                c.px(x, y, edge)
            else:
                c.px(x, y, hollow)
    return _weather(c, stone, STAGES.index(stage), STONES[stone][2] + 21, upper).img


IRON = [rgb("1d1e21"), rgb("26282c"), rgb("303338"), rgb("3c4046")]
RUST = [rgb("5a2e17"), rgb("7a3e1c"), rgb("9a5224"), rgb("b4682e")]
BRONZE = [rgb("5e4220"), rgb("7a5a2c"), rgb("96723a"), rgb("b48c4c")]
VERDIGRIS = [rgb("3f7a66"), rgb("4f927a"), rgb("68a88e"), rgb("86bea4")]


def iron(stage, seed=7301):
    """Cast iron painted black: chipped at worn, rust breaking through the paint at mossy, rusty all over (with the
    moss) when overgrown."""
    rng = random.Random(seed + stage)
    c = Canvas()
    rust = (0.0, 0.08, 0.3, 0.6)[stage]
    for y in range(16):
        for x in range(16):
            colour = rng.choice(IRON) if rng.random() >= rust else rng.choice(RUST)
            if stage >= 1 and rng.random() < 0.05:
                colour = shade(colour, 1.4)
            c.px(x, y, colour)
    if stage >= 2:
        for _ in range(2 if stage == 2 else 5):
            x = rng.randrange(16)
            for y in range(rng.randrange(8), 16):
                if rng.random() < 0.7:
                    c.px(x, y, rng.choice(RUST[:2]))
    if stage == 3:
        for _ in range(10):
            c.px(rng.randrange(16), rng.randrange(10, 16), rng.choice(MOSS[1:]))
    return c.img


def bronze(stage, seed=7401):
    """Cast bronze: warm and polished new, darkening to a brown patina, then streaked and crusted with verdigris."""
    rng = random.Random(seed + stage)
    c = Canvas()
    for y in range(16):
        for x in range(16):
            colour = rng.choice(BRONZE)
            if stage >= 1:
                colour = shade(colour, (1.0, 0.78, 0.68, 0.6)[stage])
            c.px(x, y, colour)
    # Polished highlights where hands and weather wear it.
    for _ in range(6 if stage == 0 else 2):
        c.px(rng.randrange(16), rng.randrange(16), shade(BRONZE[3], 1.2))
    if stage >= 2:
        for _ in range(4 if stage == 2 else 9):
            x = rng.randrange(16)
            length = rng.randint(4, 12)
            start = rng.randrange(0, 8)
            for y in range(start, min(16, start + length)):
                if rng.random() < 0.8:
                    c.px(x + (1 if rng.random() < 0.2 else 0), y, rng.choice(VERDIGRIS))
    if stage == 3:
        for _ in range(30):
            c.px(rng.randrange(16), rng.randrange(16), rng.choice(VERDIGRIS[1:]))
    return c.img


def ivy():
    """Ivy leaves, dark and glossy, packed over their stems."""
    rng = random.Random(7201)
    c = Canvas()
    for y in range(16):
        for x in range(16):
            c.px(x, y, rng.choice(IVY[:3]))
    for _ in range(14):
        cx, cy = rng.randrange(16), rng.randrange(16)
        colour = rng.choice(IVY[2:])
        for dx, dy in ((0, 0), (1, 0), (-1, 0), (0, -1), (1, 1), (-1, 1)):
            c.px(cx + dx, cy + dy, colour)
        c.px(cx, cy + 1, shade(colour, 1.2))
    return c.img


def chisel_item():
    """A stonemason's chisel: a steel blade, square shank and a ringed head, laid on the slant with an ash mallet."""
    c = Canvas()
    steel = [rgb("5c636b"), rgb("7d858e"), rgb("a2aab2"), rgb("cfd5da")]
    ash = [rgb("8a6236"), rgb("a77a45"), rgb("c49a5e")]
    # The chisel, from its edge (lower left) to its struck head (upper right).
    for i in range(10):
        x, y = 3 + i, 12 - i
        c.px(x, y, steel[2])
        c.px(x + 1, y, steel[1])
        c.px(x, y - 1, steel[3] if i < 2 else steel[2])
    c.px(2, 13, steel[3])
    c.px(3, 13, steel[3])
    for x, y in ((12, 3), (13, 3), (13, 2), (12, 2), (11, 3)):
        c.px(x, y, steel[1])
    c.px(13, 1, steel[3])
    # The mallet, a dumpy round head on a short handle, below it.
    for x in range(8, 13):
        for y in range(11, 15):
            c.px(x, y, ash[(x + y) % 3])
    for i in range(4):
        c.px(6 - i, 12 + i // 2, ash[0])
    outline(c, rgb("26292d"))
    return c.img


# ---------------------------------------------------------------- pack 3: buildings

OAK = [rgb("6a4527"), rgb("7d5532"), rgb("8f643c"), rgb("a27448")]
SILVER = rgb("8c8a84")
SLATES = [rgb("2f343d"), rgb("373d47"), rgb("3f4651"), rgb("48505c")]


def oak(stage, seed=7501):
    """Oak timber with its grain running along it: warm brown when new, silvering grey as it weathers, then patched with
    moss and lichen."""
    rng = random.Random(seed + stage)
    c = Canvas()
    silver = (0.0, 0.35, 0.55, 0.65)[stage]
    for x in range(16):
        streak = rng.choice(OAK[1:])
        for y in range(16):
            colour = streak if rng.random() < 0.7 else rng.choice(OAK)
            if (x * 5 + y // 7) % 6 == 0:
                colour = shade(colour, 0.78)
            colour = tuple(int(p + (g - p) * silver) for p, g in zip(colour, SILVER))
            c.px(x, y, colour)
    # Checks (cracks) along the grain, and a knot.
    for _ in range(1 + stage):
        x = rng.randrange(16)
        start = rng.randrange(10)
        for y in range(start, min(16, start + rng.randint(3, 7))):
            c.px(x, y, shade(c.get(x, y)[:3], 0.62))
    kx, ky = rng.randrange(3, 13), rng.randrange(3, 13)
    for dx, dy in ((0, 0), (1, 0), (0, 1), (1, 1)):
        c.px(kx + dx, ky + dy, shade(OAK[0], 0.8))
    if stage >= 2:
        for _ in range(3 if stage == 2 else 7):
            cx, cy = rng.randrange(16), rng.randrange(16)
            for dx, dy in ((0, 0), (1, 0), (0, 1), (-1, 0), (0, -1), (1, 1)):
                if rng.random() < 0.75:
                    c.px(cx + dx, cy + dy, rng.choice(MOSS[1:4] + [rgb("a6b07a")]))
    return c.img


def roof_slate(stage, seed=7601):
    """Roofing slates in courses (running across the texture's u, as a roof's slope lies along a box's length), their
    lower edges dark; worn slates go dusty, then moss fills the joints and spreads, with yellow lichen."""
    rng = random.Random(seed + stage)
    c = Canvas()
    dust = (1.0, 1.08, 1.0, 0.95)[stage]
    for x in range(16):
        course = x // 4
        for y in range(16):
            joint = (y + course * 3) % 6 == 0
            colour = rng.choice(SLATES[1:]) if not joint else SLATES[0]
            if x % 4 == 0:
                colour = shade(SLATES[0], 0.8)
            c.px(x, y, shade(colour, dust))
    if stage >= 2:
        for _ in range(4 if stage == 2 else 10):
            x = rng.randrange(0, 16, 4)
            y = rng.randrange(16)
            for dy in range(rng.randint(1, 3)):
                c.px(x, y + dy, rng.choice(MOSS[1:4]))
                if stage == 3 and rng.random() < 0.6:
                    c.px(x + 1, y + dy, rng.choice(MOSS[2:]))
        for _ in range(2 if stage == 2 else 5):
            cx, cy = rng.randrange(16), rng.randrange(16)
            for dx, dy in ((0, 0), (1, 0), (0, 1)):
                c.px(cx + dx, cy + dy, rng.choice(LICHEN["granite"]))
    return c.img


def stained_glass():
    """Leaded stained glass, opaque: diamond quarries in a lattice of lead, deep blue and ruby by turns, with a gold
    rosette where every other pair of leads cross."""
    c = Canvas()
    lead = rgb("2a2a2e")
    blues = [rgb("1f3a8a"), rgb("2a4aa8"), rgb("3858bc")]
    reds = [rgb("8c1422"), rgb("a81c2c"), rgb("c02a38")]
    for y in range(16):
        for x in range(16):
            u, v = (x + y) % 8, (x - y) % 8
            if u == 0 or v == 0:
                c.px(x, y, lead)
                continue
            cell = ((x + y) // 8 + (x - y + 16) // 8) % 2
            palette = blues if cell == 0 else reds
            # Lighter towards each pane's middle, as light comes through thinner glass.
            depth = min(u, 8 - u, v, 8 - v)
            c.px(x, y, palette[min(2, depth - 1)])
    for cx, cy in ((0, 0), (8, 8)):
        for dx, dy in ((0, 0), (1, 0), (-1, 0), (0, 1), (0, -1)):
            c.px((cx + dx) % 16, (cy + dy) % 16, rgb("d8a83a") if (dx, dy) != (0, 0) else rgb("f4d470"))
    return c.img


def marble_floor():
    """A chequered floor of white marble and black slate in squares of four pixels (25 cm), polished."""
    rng = random.Random(7701)
    c = Canvas()
    white = STONES["marble"][0]
    dark = [rgb("26292e"), rgb("2e3238"), rgb("353a41")]
    for y in range(16):
        for x in range(16):
            if (x // 4 + y // 4) % 2 == 0:
                c.px(x, y, rng.choice(white[1:]))
            else:
                c.px(x, y, rng.choice(dark))
    for x in range(16):
        c.px(x, 0, shade(white[0], 0.85))
    return c.img


def lamp_glass():
    """A sanctuary lamp's ruby glass, lit from within."""
    c = Canvas()
    for y in range(16):
        for x in range(16):
            glow = max(0.0, 1 - (abs(x - 7.5) + abs(y - 9)) / 12)
            c.px(x, y, tuple(min(255, int(v + 90 * glow)) for v in (176, 24, 34)))
    return c.img


def lantern_glass():
    """A lantern's panes with the flame's light behind them: warm amber, brightest in the middle."""
    c = Canvas()
    for y in range(16):
        for x in range(16):
            glow = max(0.0, 1 - (abs(x - 7.5) + abs(y - 8.5)) / 11)
            c.px(x, y, (min(255, int(222 + 33 * glow)), min(255, int(150 + 80 * glow)), min(255, int(60 + 110 * glow))))
    return c.img


def door_glass():
    """Smoked glass behind a bronze grille: nearly black, with a faint sheen."""
    rng = random.Random(7801)
    c = Canvas()
    for y in range(16):
        for x in range(16):
            c.px(x, y, rng.choice((rgb("15181c"), rgb("1b1f24"), rgb("22272d"))))
    for i in range(5):
        c.px(10 + i // 2, 2 + i, rgb("3c444e"))
    return c.img


def mausoleum_door_item():
    """The Bronze Mausoleum Door as an item: a bronze frame with a meeting stile, a grille over smoked glass above and
    raised panels below."""
    c = Canvas()
    for y in range(1, 16):
        for x in range(3, 13):
            c.px(x, y, BRONZE[1] if (x + y) % 3 else BRONZE[2])
    for y in range(2, 8):
        for x in (4, 5, 6, 9, 10, 11):
            c.px(x, y, rgb("1b1f24"))
    for y in range(2, 8):
        c.px(5, y, BRONZE[2])
        c.px(10, y, BRONZE[2])
    for x in range(3, 13):
        c.px(x, 5, BRONZE[2])
    for y in range(10, 14):
        for x in (5, 10):
            c.px(x, y, BRONZE[3])
    for y in range(1, 16):
        c.px(7, y, BRONZE[0])
        c.px(8, y, BRONZE[0])
    c.px(6, 9, BRONZE[3])
    c.px(9, 9, BRONZE[3])
    outline(c, rgb("2a1d10"))
    return c.img


# ---------------------------------------------------------------- pack 4: the grounds

CHIPS = [rgb("d9d8d2"), rgb("e6e5df"), rgb("f0efe9"), rgb("c4c3bc"), rgb("b0afa8")]
SOIL = [rgb("3b2a1c"), rgb("46321f"), rgb("523b25")]
FLOWERS = [rgb("c8283a"), rgb("f2efe6"), rgb("f0c838"), rgb("8a52b8")]
GRASS = [rgb("3f6b24"), rgb("4e7f2c"), rgb("5f9234"), rgb("74a540")]


def chippings(stage, seed=7901):
    """White marble chippings: clean and bright, greying as dirt settles in, green with algae, then grassed over."""
    rng = random.Random(seed + stage)
    c = Canvas()
    for y in range(16):
        for x in range(16):
            colour = rng.choice(CHIPS[:3]) if rng.random() < 0.8 else rng.choice(CHIPS[3:])
            if stage >= 1 and rng.random() < 0.12 * stage:
                colour = shade(colour, 0.78)
            c.px(x, y, colour)
    if stage >= 2:
        for _ in range(5 if stage == 2 else 9):
            cx, cy = rng.randrange(16), rng.randrange(16)
            for dx, dy in ((0, 0), (1, 0), (0, 1), (-1, 0), (1, 1)):
                if rng.random() < 0.75:
                    c.px(cx + dx, cy + dy, rng.choice(MOSS[2:]))
    if stage == 3:
        for _ in range(14):
            x, y = rng.randrange(16), rng.randrange(16)
            for dy in range(rng.randint(1, 3)):
                c.px(x, y - dy, rng.choice(GRASS))
    return c.img


def flower_bed(stage, seed=8001):
    """A grave's planted bed seen from above and from the side alike: kept, neat flowers in rows on dark earth; let
    go, weeds and grass come through and the flowers thin; overgrown, grass and brambles."""
    rng = random.Random(seed + stage)
    c = Canvas()
    for y in range(16):
        for x in range(16):
            c.px(x, y, rng.choice(SOIL))
    flowers = (1.0, 0.6, 0.25, 0.08)[stage]
    weeds = (0.0, 0.25, 0.6, 0.9)[stage]
    for y in range(1, 16, 3):
        for x in range(1, 16, 3):
            ox, oy = x + rng.choice((0, 0, 1)), y + rng.choice((0, 0, 1))
            if rng.random() < flowers:
                c.px(ox, oy + 1, GRASS[1])
                c.px(ox + 1, oy + 1, GRASS[2])
                c.px(ox, oy, FLOWERS[(x // 3 + y // 3) % 4])
    for _ in range(int(60 * weeds)):
        x, y = rng.randrange(16), rng.randrange(16)
        c.px(x, y, rng.choice(GRASS))
        if rng.random() < 0.5:
            c.px(x, y - 1, rng.choice(GRASS))
    if stage == 3:
        for _ in range(6):
            x, y = rng.randrange(16), rng.randrange(16)
            c.px(x, y, rgb("3a1f2e"))
            c.px(x + 1, y, rgb("5b2a40"))
    return c.img


def pit():
    """An open grave's darkness, seen down into: nearly black earth, the same all over so a grave two blocks long
    shows no seam (its rim is the fresh earth round it)."""
    rng = random.Random(8301)
    c = Canvas()
    for y in range(16):
        for x in range(16):
            c.px(x, y, rng.choice((rgb("0e0a07"), rgb("130e0a"), rgb("18120c"))))
    return c.img


def straps():
    """Canvas lowering straps: webbing with a woven stripe."""
    c = Canvas()
    for y in range(16):
        for x in range(16):
            c.px(x, y, rgb("b8a27a") if (y % 4) else rgb("9c8660"))
    return c.img


PETALS = {"white": [rgb("e8e4d8"), rgb("f4f1e8"), rgb("fffdf6")], "red": [rgb("8c1422"), rgb("b01e2e"), rgb("d03644")],
          "yellow": [rgb("d8a020"), rgb("eec030"), rgb("f8dc60")], "purple": [rgb("5a2e8a"), rgb("7442a8"), rgb("9264c4")]}


def petals(colour, seed=8101):
    """Flower heads in `colour` (or `mixed`: all of them), or `wilted`: browned and dry."""
    rng = random.Random(seed + len(colour))
    c = Canvas()
    for y in range(16):
        for x in range(16):
            if colour == "wilted":
                col = rng.choice((rgb("6e5a3a"), rgb("85704a"), rgb("5a4a30")))
            elif colour == "mixed":
                col = rng.choice(PETALS[("white", "red", "yellow", "purple")[((x // 4) + (y // 4)) % 4]])
            else:
                col = rng.choice(PETALS[colour])
            c.px(x, y, col)
    return c.img


def leaves(seed=8201):
    c = Canvas()
    rng = random.Random(seed)
    for y in range(16):
        for x in range(16):
            c.px(x, y, rng.choice(GRASS))
    return c.img


def lantern_unlit():
    """A lantern's glass by day, unlit: dark amber with a gleam."""
    c = Canvas()
    for y in range(16):
        for x in range(16):
            c.px(x, y, rgb("4a3a22") if (x + y) % 5 else rgb("5c4a2c"))
    for i in range(4):
        c.px(11 + i // 2, 2 + i, rgb("8a7a5a"))
    return c.img


def graveyard_textures():
    out = {}
    for stone in STONES:
        for stage in STAGES:
            for upper in (False, True):
                suffix = f"{stage}_upper" if upper else stage
                out[("block", f"gy_{stone}_{suffix}")] = stone_face(stone, stage, upper)
                out[("block", f"gy_{stone}_relief_{suffix}")] = relief_face(stone, stage, upper)
        if stone == "granite":
            for stage in STAGES:
                out[("block", f"gy_granite_rough_{stage}")] = rough_face(stone, stage)
                for upper in (False, True):
                    suffix = f"{stage}_upper" if upper else stage
                    out[("block", f"gy_granite_knot_{suffix}")] = knotwork(stone, stage, upper)
    for k, stage in enumerate(STAGES):
        out[("block", f"gy_iron_{stage}")] = iron(k)
        out[("block", f"gy_bronze_{stage}")] = bronze(k)
    out[("block", "gy_ivy")] = ivy()
    out[("item", "stonemasons_chisel")] = chisel_item()
    for k, stage in enumerate(STAGES):
        out[("block", f"gy_oak_{stage}")] = oak(k)
        out[("block", f"gy_roof_slate_{stage}")] = roof_slate(k)
    out[("block", "gy_stained_glass")] = stained_glass()
    out[("block", "gy_marble_floor")] = marble_floor()
    out[("block", "gy_lamp_glass")] = lamp_glass()
    out[("block", "gy_lantern_glass")] = lantern_glass()
    out[("block", "gy_door_glass")] = door_glass()
    out[("item", "bronze_mausoleum_door")] = mausoleum_door_item()
    for k, stage in enumerate(STAGES):
        out[("block", f"gy_chippings_{stage}")] = chippings(k)
        out[("block", f"gy_flower_bed_{stage}")] = flower_bed(k)
    out[("block", "gy_pit")] = pit()
    out[("block", "gy_straps")] = straps()
    for colour in ("white", "red", "yellow", "purple", "mixed", "wilted"):
        out[("block", f"gy_petals_{colour}")] = petals(colour)
    out[("block", "gy_leaves")] = leaves()
    out[("block", "gy_lantern_unlit")] = lantern_unlit()
    return out

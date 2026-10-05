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

Everything is painted in the clean, cartoon style (docs/ART_DIRECTION.md, "Creatures and faces: cute and clean"): flat
tones, features in fixed places and regular patterns, no random speckle, and every pattern repeats every 16 pixels so
neighbouring blocks join up.

Called from crop_textures.crop_textures(). Every pixel is drawn here by code; no Mojang texture is read, traced or
recoloured.
"""
import math

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


# Fixed places (x, y) for the speckles and features a stone carries, so every block of a stone matches its neighbours.
FLECKS = [(2, 1), (9, 3), (13, 8), (5, 7), (11, 12), (1, 11), (7, 14), (14, 1), (4, 4), (10, 9)]


def _wave(x, shift, amplitude=1.0):
    """A gentle wave across the texture, repeating every 16 pixels so neighbouring blocks join up."""
    return amplitude * math.sin(2 * math.pi * (x + shift) / 16.0)


def _base(stone, seed_extra=0, smooth=False):
    """The bare stone of `stone`, flat in its middle tone: marble with one smooth grey vein, slate in even laminations,
    granite with neat flecks of black mica, pink feldspar and white quartz in a fixed pattern, sandstone in even
    bedding planes."""
    base, accent, seed = STONES[stone]
    shift = (seed + seed_extra) % 16
    c = Canvas()
    for y in range(16):
        for x in range(16):
            colour = base[2]
            if stone == "slate" and y % 4 == 0:
                colour = base[1]
            elif stone == "slate" and y % 4 == 1:
                colour = base[3]
            if stone == "sandstone" and y % 5 == 2:
                colour = base[1]
            elif stone == "sandstone" and y % 5 == 3:
                colour = base[3]
            c.px(x, y, colour)
    if stone == "marble":
        for y in range(16):
            x = int(round(8 + _wave(y, shift, 3.0)))
            c.px(x % 16, y, accent[1])
            if not smooth:
                c.px((x + 1) % 16, y, base[3])
    elif stone == "granite":
        for i, (fx, fy) in enumerate(FLECKS[: 10 if not smooth else 6]):
            x, y = (fx + shift) % 16, fy
            c.px(x, y, accent[i % 3])
            c.px((x + 1) % 16, y, accent[i % 3])
    return c


def _weather(c, stone, stage, seed, upper=False):
    """Weathers the canvas to `stage`, in neat shapes: duller stone; grime run down from the top in a few clean streaks;
    a pale chip; lichen in round rosettes and moss in soft cushions; and moss from the ground up in a gentle wave."""
    if stage == 0:
        return c
    shift = seed % 16
    dull = (1.0, 0.95, 0.9, 0.86)[stage]
    for y in range(16):
        for x in range(16):
            pixel = c.get(x, y)
            if pixel:
                c.px(x, y, shade(pixel[:3], dull))
    # Grime washed down from the top in straight streaks two tones deep.
    for i, gx in enumerate((3, 11, 7)[: 1 + stage]):
        x = (gx + shift) % 16
        length = 3 + 2 * stage + (i % 2) * 2
        for y in range(length):
            pixel = c.get(x, y)
            if pixel:
                c.px(x, y, tuple(int(p * 0.7 + g * 0.3) for p, g in zip(pixel[:3], GRIME)) if y < length - 1 else
                     tuple(int(p * 0.85 + g * 0.15) for p, g in zip(pixel[:3], GRIME)))
    # A chipped corner: two pale pixels.
    cx = (shift + 5) % 16
    for dx in (0, 1):
        pixel = c.get((cx + dx) % 16, 1)
        if pixel:
            c.px((cx + dx) % 16, 1, shade(pixel[:3], 1.12))
    if stage >= 2:
        lichen = LICHEN[stone]
        for lx, ly in ((4, 5), (12, 3), (8, 10), (1, 8), (14, 12), (6, 1))[: 2 if stage == 2 else 5]:
            x, y = (lx + shift) % 16, ly
            for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                c.px((x + dx) % 16, y + dy, lichen[1])
            c.px(x, y, lichen[0])
        for mx, my in ((10, 7), (3, 12), (13, 4), (6, 9))[: 2 if stage == 2 else 4]:
            x = (mx + shift) % 16
            for dy, dxs in ((0, (0, 1)), (1, (-1, 0, 1, 2))):
                for dx in dxs:
                    c.px((x + dx) % 16, my + dy, MOSS[3] if dy == 0 else MOSS[2])
        if not upper:
            height = 2 if stage == 2 else 5
            for x in range(16):
                level = height + int(round(_wave(x, shift, 1.0)))
                for y in range(16 - level, 16):
                    depth = y - (16 - level)
                    c.px(x, y, MOSS[4] if depth == 0 else MOSS[3] if depth == 1 else MOSS[1] if y == 15 else MOSS[2])
    return c


def stone_face(stone, stage, upper=False):
    return _weather(_base(stone), stone, STAGES.index(stage), STONES[stone][2], upper).img


def relief_face(stone, stage, upper=False):
    """Carved and polished work, flat and bright: marble polished almost white with one faint vein, slate the pale grey
    of fresh-cut slate, granite polished dark with a few neat specks, sandstone rubbed smooth."""
    base, accent, seed = STONES[stone]
    shift = (seed + 50) % 16
    c = Canvas()
    colour = {"marble": shade(base[3], 1.03), "slate": shade(base[2], 1.38), "granite": rgb("766e6c"),
              "sandstone": shade(base[2], 1.03)}[stone]
    for y in range(16):
        for x in range(16):
            c.px(x, y, colour)
    if stone == "granite":
        for i, (fx, fy) in enumerate(FLECKS[:6]):
            c.px((fx + shift) % 16, fy, (rgb("2e2b2c"), rgb("a8857c"), rgb("b9b2ac"))[i % 3])
    elif stone == "marble":
        for y in range(16):
            c.px(int(round(8 + _wave(y, shift, 3.0))) % 16, y, accent[1])
    return _weather(c, stone, STAGES.index(stage), STONES[stone][2] + 3, upper).img


def rough_face(stone, stage, upper=False):
    """Rock-faced stone, split rather than sawn: rounded lumps in staggered rows, each lit along its top and left and
    shadowed along its bottom and right."""
    c = _base(stone, 90)
    for y in range(16):
        for x in range(16):
            row = y // 4
            u = (x + (row % 2) * 2) % 4
            v = y % 4
            pixel = c.get(x, y)[:3]
            if v == 0 or u == 0:
                c.px(x, y, shade(pixel, 1.12))
            elif v == 3 or u == 3:
                c.px(x, y, shade(pixel, 0.8))
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
                c.px(x, y, band[1])
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
    """Cast iron painted black, flat with a sheen down every eighth column: chipped at worn, rust running down from the
    middle at mossy, rusty along the bottom with moss when overgrown."""
    c = Canvas()
    for y in range(16):
        for x in range(16):
            c.px(x, y, IRON[3] if x % 8 == 0 else IRON[2] if x % 8 == 1 else IRON[1])
    if stage >= 1:
        for x, y in ((5, 3), (12, 9), (2, 13))[:stage]:
            c.px(x, y, shade(IRON[3], 1.4))
    if stage >= 2:
        for x in (4, 11, 14, 7)[: 2 if stage == 2 else 4]:
            for y in range(6, 16):
                c.px(x, y, RUST[2] if y == 6 else RUST[1])
    if stage == 3:
        for x in range(16):
            for y in range(13 + (x % 3 == 0), 16):
                c.px(x, y, RUST[1] if y < 15 else RUST[0])
        for x in range(1, 16, 4):
            c.px(x, 15, MOSS[3])
            c.px(x + 1, 15, MOSS[2])
            c.px(x, 14, MOSS[3])
    return c.img


def bronze(stage, seed=7401):
    """Cast bronze, flat with a polished diagonal sheen: warm new, darkening to a brown patina, then with clean streaks
    of verdigris running down, and crusted with it from the bottom when overgrown."""
    factor = (1.0, 0.78, 0.68, 0.6)[stage]
    c = Canvas()
    for y in range(16):
        for x in range(16):
            sheen = (x + y) % 16 in (3, 4)
            c.px(x, y, shade(BRONZE[3], factor * (1.12 if stage == 0 else 1.0)) if sheen else shade(BRONZE[2], factor))
    if stage >= 2:
        for x, top, length in ((3, 0, 9), (10, 2, 7), (13, 0, 12), (6, 4, 6), (1, 3, 8))[: 2 if stage == 2 else 5]:
            for y in range(top, min(16, top + length)):
                c.px(x, y, VERDIGRIS[2] if y == top else VERDIGRIS[1])
    if stage == 3:
        for x in range(16):
            for y in range(12 + int(round(math.sin(2 * math.pi * x / 16.0))), 16):
                c.px(x, y, VERDIGRIS[2] if y % 3 == 0 else VERDIGRIS[1])
    return c.img


def ivy():
    """Ivy leaves, dark and glossy, in neat offset rows over their stems: each a small pointed leaf lit at its top."""
    c = Canvas()
    for y in range(16):
        for x in range(16):
            c.px(x, y, IVY[1])
    for row in range(4):
        for col in range(4):
            x0, y0 = col * 4 + (2 if row % 2 else 0), row * 4
            for dx, dy, k in ((1, 0, 4), (0, 1, 3), (1, 1, 3), (2, 1, 3), (1, 2, 2), (0, 2, 2), (2, 2, 2), (1, 3, 0)):
                c.px((x0 + dx) % 16, y0 + dy, IVY[k])
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
            c.px(x, y, ash[2] if y == 11 else ash[1])
    for i in range(4):
        c.px(6 - i, 12 + i // 2, ash[0])
    outline(c, rgb("26292d"))
    return c.img


# ---------------------------------------------------------------- pack 3: buildings

OAK = [rgb("6a4527"), rgb("7d5532"), rgb("8f643c"), rgb("a27448")]
SILVER = rgb("8c8a84")
SLATES = [rgb("2f343d"), rgb("373d47"), rgb("3f4651"), rgb("48505c")]


def oak(stage, seed=7501):
    """Oak timber with its grain running along it in even planks: warm brown when new, silvering grey as it weathers,
    with a check or two, then with neat tufts of moss and lichen."""
    silver = (0.0, 0.35, 0.55, 0.65)[stage]
    c = Canvas()
    for x in range(16):
        for y in range(16):
            k = x % 4
            colour = shade(OAK[0], 0.85) if k == 3 else OAK[3] if k == 0 else OAK[2] if (x // 4) % 2 else OAK[1]
            c.px(x, y, tuple(int(p + (g - p) * silver) for p, g in zip(colour, SILVER)))
    for x, start, length in ((5, 2, 5), (9, 8, 6), (1, 5, 4))[:stage]:
        for y in range(start, start + length):
            c.px(x, y, shade(c.get(x, y)[:3], 0.62))
    for dx, dy in ((0, 0), (1, 0), (0, 1), (1, 1)):
        c.px(10 + dx, 4 + dy, shade(OAK[0], 0.8))
    if stage >= 2:
        for cx, cy in ((3, 11), (12, 13), (7, 2), (14, 7))[: 2 if stage == 2 else 4]:
            for dx, dy in ((0, 0), (1, 0), (0, 1), (-1, 0), (0, -1)):
                c.px(cx + dx, cy + dy, MOSS[3] if (dx, dy) == (0, -1) else MOSS[2])
    return c.img


def roof_slate(stage, seed=7601):
    """Roofing slates in courses (running across the texture's u, as a roof's slope lies along a box's length): flat
    slates in two tones by turns, their lower edges dark; worn slates go dusty, then moss fills a few joints, with
    yellow lichen rosettes."""
    dust = (1.0, 1.08, 1.0, 0.95)[stage]
    c = Canvas()
    for x in range(16):
        course = x // 4
        for y in range(16):
            tile = (y + course * 3) // 6
            joint = (y + course * 3) % 6 == 0
            colour = SLATES[0] if joint else SLATES[2] if (tile + course) % 2 else SLATES[1]
            if x % 4 == 0:
                colour = shade(SLATES[0], 0.8)
            elif x % 4 == 1:
                colour = SLATES[3]
            c.px(x, y, shade(colour, dust))
    if stage >= 2:
        for x, y in ((4, 3), (8, 9), (12, 1), (0, 12), (4, 14))[: 2 if stage == 2 else 5]:
            for dy in range(3):
                c.px(x, y + dy, MOSS[3] if dy == 0 else MOSS[2])
                if stage == 3:
                    c.px(x + 1, y + dy, MOSS[3])
        for cx, cy in ((10, 6), (2, 9), (14, 13))[: 1 if stage == 2 else 3]:
            for dx, dy in ((0, 0), (1, 0), (0, 1)):
                c.px(cx + dx, cy + dy, LICHEN["granite"][0])
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
    """A chequered floor of white marble and black slate in squares of four pixels (25 cm), polished: each square flat,
    lit along its top and left edge."""
    c = Canvas()
    white = STONES["marble"][0]
    dark = [rgb("26292e"), rgb("2e3238"), rgb("353a41")]
    for y in range(16):
        for x in range(16):
            lit = x % 4 == 0 or y % 4 == 0
            if (x // 4 + y // 4) % 2 == 0:
                c.px(x, y, white[3] if lit else white[2])
            else:
                c.px(x, y, dark[2] if lit else dark[1])
    return c.img


def lamp_glass():
    """A sanctuary lamp's ruby glass, lit from within: three clean rings of red, brightest in the middle."""
    c = Canvas()
    for y in range(16):
        for x in range(16):
            glow = max(0.0, 1 - (abs(x - 7.5) + abs(y - 9)) / 12)
            glow = 0.0 if glow < 0.3 else 0.5 if glow < 0.65 else 1.0
            c.px(x, y, tuple(min(255, int(v + 90 * glow)) for v in (176, 24, 34)))
    return c.img


def lantern_glass():
    """A lantern's panes with the flame's light behind them: warm amber in three clean rings, brightest in the middle."""
    c = Canvas()
    for y in range(16):
        for x in range(16):
            glow = max(0.0, 1 - (abs(x - 7.5) + abs(y - 8.5)) / 11)
            glow = 0.0 if glow < 0.3 else 0.5 if glow < 0.65 else 1.0
            c.px(x, y, (min(255, int(222 + 33 * glow)), min(255, int(150 + 80 * glow)), min(255, int(60 + 110 * glow))))
    return c.img


def door_glass():
    """Smoked glass behind a bronze grille: flat near-black, with one clean diagonal sheen."""
    c = Canvas()
    for y in range(16):
        for x in range(16):
            c.px(x, y, rgb("1b1f24"))
    for i in range(5):
        c.px(10 + i // 2, 2 + i, rgb("3c444e"))
    return c.img


def mausoleum_door_item():
    """The Bronze Mausoleum Door as an item: a flat bronze frame with a meeting stile, a grille over smoked glass above
    and raised panels below."""
    c = Canvas()
    for y in range(1, 16):
        for x in range(3, 13):
            c.px(x, y, BRONZE[2] if x in (3, 4) or y == 1 else BRONZE[1])
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
    """White marble chippings: round pebbles in neat offset rows, each lit at its top, grey between; dirtier pebbles at
    worn, neat tufts of moss at mossy, and grass blades coming through when overgrown."""
    c = Canvas()
    for y in range(16):
        for x in range(16):
            row = y // 3
            u = (x + (row % 2) * 2) % 4
            v = y % 3
            pebble = (x + (row % 2) * 2) // 4 + row
            if u == 3 or v == 2:
                colour = CHIPS[4]
            elif v == 0 and u in (1, 2):
                colour = CHIPS[2]
            else:
                colour = CHIPS[1]
            if stage >= 1 and pebble % (5 - stage) == 0 and colour != CHIPS[4]:
                colour = shade(colour, 0.82)
            c.px(x, y, colour)
    if stage >= 2:
        for cx, cy in ((3, 4), (11, 9), (7, 13), (14, 2), (1, 10))[: 2 if stage == 2 else 5]:
            for dx, dy in ((0, 0), (1, 0), (0, 1), (-1, 0), (1, 1)):
                c.px((cx + dx) % 16, (cy + dy) % 16, MOSS[3] if dy == 0 else MOSS[2])
    if stage == 3:
        for x, y in ((5, 7), (9, 3), (13, 12), (2, 14), (8, 10), (15, 6)):
            for dy in range(3):
                c.px(x, y - dy, GRASS[3] if dy == 2 else GRASS[2])
    return c.img


def flower_bed(stage, seed=8001):
    """A grave's planted bed seen from above and from the side alike: dark earth in even furrows, neat little flowers
    in rows, each on two leaves; let go, the flowers thin and grass blades come through; overgrown, grass and brambles."""
    c = Canvas()
    for y in range(16):
        for x in range(16):
            c.px(x, y, SOIL[0] if y % 4 == 3 else SOIL[2] if y % 4 == 0 else SOIL[1])
    keep = (4, 3, 2, 1)[stage]
    for i, (x0, y0) in enumerate((x, y) for y in range(0, 16, 4) for x in range(0, 16, 4)):
        x = x0 + (2 if (y0 // 4) % 2 else 0)
        if i % 4 >= keep:
            continue
        colour = FLOWERS[(x0 // 4 + y0 // 4) % 4]
        c.px((x + 1) % 16, y0 + 2, GRASS[1])
        c.px((x - 1) % 16, y0 + 2, GRASS[2])
        for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            c.px((x + dx) % 16, y0 + 1 + dy, colour)
        c.px(x % 16, y0 + 1, rgb("f8e070"))
    blades = (0, 3, 6, 10)[stage]
    for x, y in ((2, 3), (9, 6), (14, 2), (5, 11), (12, 13), (0, 8), (7, 15), (11, 1), (3, 14), (15, 10))[:blades]:
        for dy in range(2):
            c.px(x, y - dy, GRASS[3] if dy == 1 else GRASS[2])
    if stage == 3:
        for x, y in ((4, 6), (10, 10), (13, 5)):
            c.px(x, y, rgb("3a1f2e"))
            c.px(x + 1, y, rgb("5b2a40"))
    return c.img


def pit():
    """An open grave's darkness, seen down into: flat near-black earth, the same all over so a grave two blocks long
    shows no seam (its rim is the fresh earth round it)."""
    c = Canvas()
    for y in range(16):
        for x in range(16):
            c.px(x, y, rgb("130e0a"))
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
    """Flower heads in `colour` (or `mixed`: all of them, a colour to each flower), or `wilted`: browned and dry; neat
    round flowers in offset rows, each lit at its middle, darker in the gaps."""
    c = Canvas()
    for y in range(16):
        for x in range(16):
            row = y // 4
            u = (x + (row % 2) * 2) % 4
            v = y % 4
            flower = (x + (row % 2) * 2) // 4 + row
            if colour == "wilted":
                palette = [rgb("5a4a30"), rgb("6e5a3a"), rgb("85704a")]
            elif colour == "mixed":
                palette = PETALS[("white", "red", "yellow", "purple")[flower % 4]]
            else:
                palette = PETALS[colour]
            gap = u == 3 or v == 3
            middle = u in (1, 2) and v in (1, 2)
            c.px(x, y, palette[0] if gap else palette[2] if middle and (u, v) == (1, 1) else palette[1])
    return c.img


def leaves(seed=8201):
    """Green leaves in neat offset rows, each lit at its top, darker between."""
    c = Canvas()
    for y in range(16):
        for x in range(16):
            row = y // 4
            u = (x + (row % 2) * 2) % 4
            v = y % 4
            c.px(x, y, GRASS[0] if v == 3 else GRASS[3] if v == 0 and u in (1, 2) else GRASS[2] if u in (1, 2) else GRASS[1])
    return c.img


def lantern_unlit():
    """A lantern's glass by day, unlit: flat dark amber with one clean gleam."""
    c = Canvas()
    for y in range(16):
        for x in range(16):
            c.px(x, y, rgb("4a3a22"))
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

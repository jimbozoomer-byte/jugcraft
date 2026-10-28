"""Generated assets for Haunted Midway, decor set 6 (tools/haunted_midway.py; docs/features/haunted-midway.md).

Built from boxes at real size, one texel to a model pixel, in the market displays' manner (tools/pumpkin_patch_data.py):
each material one colour stepped into tones, its marks placed the way it is made, never sprinkled.
- The livery: midnight-violet lacquer and pumpkin-orange paint, each panel a flat field inside a gold bead one texel in
  from its edge (lit along its top and left, shaded along its bottom and right), with bone-white boards for counters.
  Roofs are painted in stripes two texels wide, violet and orange by turns, each lit along its first column, each
  tier set in by one stripe so its stripes carry on the ones under it; the cart's canopy is an umbrella's panels seen
  from above, eight about its middle, tier on tier; a valance's stripes end, two by two, in a rounded tab (cut out at
  its corners).
- Signs are pictures, not lettering (at a texel a pixel a one-block cart has no room for a word): a paper cone of pink
  floss on the cotton-candy cart, a striped popcorn box heaped white on the wagon, a black bat on the wagon's crest.
- Steel (the spinning bowl, the kettle): flat bands, lit along one edge. The bowl and the kettle are eight-sided, four
  sides square to the block and four turned 45 degrees, their floors the same eight sides (two crossed pairs of boxes).
- Floss: pink strands wound on a slant, each row a step along, lit along its top row. Popcorn: white puffs two texels
  square, lit at their top left, set in a staggered grid with a golden hull between them.
- Glass, as the Caramel Apple Stand's jar: cut out inside a pale rim with a lit streak. Glows (the heater band, the case
  lamp and the jack-o'-lantern's face) are full-bright, unshaded and steady, a face cut out to its holes.
"""
import math

import decor17_data as d17d
import decor20_data as d20d
import flora_art as fa
import haunted_midway as hm
import pumpkin_patch_data as ppd
from flora_art import Sculpt, pal, rotation, shade
from pumpkin_patch_data import flat, lengthwise, panel, planks, walls, wrapped

MOD = "jugcraft"
FACINGS = ppd.FACINGS

VIOLET = pal("1e0f2a", "2e1840", "40225a", "553074", "6c428e")
ORANGE = pal("6a2a08", "9a420e", "c45e16", "e07c24", "f09c44")
BONE = pal("8a8270", "aaa290", "c8c0ac", "e0d8c4", "f0eadc")
GOLD = pal("5a3c0a", "8c6418", "bc9030", "e0bc5a", "fbe7a4")
BLACK = pal("16141a", "222028", "302e38", "403e4a")
STEEL = pal("4a4e54", "6a7078", "8c929a", "aeb4bc", "d0d6dc")
FLOSS = pal("b85a7e", "d47a9c", "e89ab6", "f6bcd0", "fcdce8")
POPCORN = pal("a88a4a", "d0b070", "ecd49a", "f8ecc4", "fffaea")
PAPER = pal("b8ab88", "d8ccaa", "ece2c4")
HEAT = pal("c8501a", "e8742a", "f8a040", "fcd080")
LAMP = pal("e8b450", "f8d888", "fff0c0")
PUMPKIN = pal("6a2a08", "9a420e", "c45e16", "e07c24")
STEM = pal("2e2216", "44321e", "5a4228")


def rid(path):
    return path if ":" in path else f"{MOD}:{path}"


def clear(p, x, y):
    p.img.putpixel((x, y), (0, 0, 0, 0))


# ---------------------------------------------------------------- painters

def lacquer(palette=VIOLET):
    """A lacquered panel: its flat field in one tone inside a gold bead one texel in from its edge (lit along its top and
    left, shaded along its bottom and right), the field's own edge a tone lit at the top and left, shaded at the others.
    A piece too small for the bead is the field alone."""
    def paint(p):
        bead_ok = p.w >= 5 and p.h >= 5
        for y in range(p.h):
            for x in range(p.w):
                bead = bead_ok and ((x in (1, p.w - 2) and 1 <= y <= p.h - 2) or (y in (1, p.h - 2) and 1 <= x <= p.w - 2))
                if bead:
                    p.put(x, y, shade(GOLD, 4 if (y == 1 or x == 1) else 2))
                elif x == 0 or y == 0:
                    p.put(x, y, shade(palette, 3))
                elif x == p.w - 1 or y == p.h - 1:
                    p.put(x, y, shade(palette, 1))
                else:
                    p.put(x, y, shade(palette, 2))
    paint.tag = repr(("lacquer", palette))
    return paint


def stripes(phase=0, width=2):
    """Stripes `width` texels wide across the piece's longer side (so they run along its shorter one, however the piece
    is laid in the texture), violet and orange by turns (`phase` 1 starting with orange), each lit along its first
    column."""
    def paint(p):
        along_x = p.w >= p.h
        for y in range(p.h):
            for x in range(p.w):
                band, at = divmod(x if along_x else y, width)
                palette = VIOLET if (band + phase) % 2 == 0 else ORANGE
                p.put(x, y, shade(palette, 3 if at == 0 else 2))
    paint.tag = repr(("stripes", phase, width))
    return paint


def sunburst(sectors=8):
    """A canopy's top seen from above, as an umbrella's: `sectors` flat panels about its middle, violet and orange by
    turns; the same however the piece is laid in the texture, and tier on tier
    about one middle, so a stepped canopy's panels carry on up it."""
    def paint(p):
        cx, cy = (p.w - 1) / 2, (p.h - 1) / 2
        for y in range(p.h):
            for x in range(p.w):
                angle = (math.degrees(math.atan2(y - cy, x - cx)) + 360.0 + 1e-6) % 360.0
                palette = VIOLET if int(angle // (360.0 / sectors)) % 2 == 0 else ORANGE
                p.put(x, y, shade(palette, 2))
    paint.tag = repr(("sunburst", sectors))
    return paint


def valance(width=2):
    """A canopy's valance: the stripes (`stripes`) under a gold binding along its top row, every two stripes ending at the
    bottom in a rounded tab, its two bottom corners cut out."""
    def paint(p):
        stripes(0, width)(p)
        for x in range(p.w):
            p.put(x, 0, shade(GOLD, 3))
            if x % (2 * width) in (0, 2 * width - 1) and p.h > 2:
                clear(p, x, p.h - 1)
    paint.tag = repr(("valance", width))
    return paint


def floss():
    """Spun floss: pink strands wound on a slant (every fourth texel along a row a strand, each row a step along from the
    last), lit along its top row."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                if y == 0:
                    k = 4
                else:
                    k = 3 if (x + 2 * y) % 4 == 0 else 1 if (x + 2 * y) % 4 == 2 else 2
                p.put(x, y, shade(FLOSS, k))
    paint.tag = repr(("floss",))
    return paint


def popcorn():
    """Popped corn: white puffs two texels square, each lit at its top left, in a grid three texels apart, each row of
    puffs a texel along from the last; the golden hulls show in the gaps between them."""
    def paint(p):
        for y in range(p.h):
            row, dy = divmod(y, 3)
            for x in range(p.w):
                dx = (x + row) % 3
                if dx == 2 or dy == 2:
                    k = 1 if (dx == 2 and dy == 2) else 2
                else:
                    k = 4 if (dx, dy) == (0, 0) else 3
                p.put(x, y, shade(POPCORN, k))
    paint.tag = repr(("popcorn",))
    return paint


def slotted():
    """A spinner head's drum: steel, its slots a dark texel every second column, its top row lit."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                p.put(x, y, shade(STEEL, 4 if y == 0 else 0 if x % 2 == 1 and 0 < y < p.h - 1 else 2))
    paint.tag = repr(("slotted",))
    return paint


def glow(palette):
    """A glow: its palette's middle tone, its brightest along its middle row, steady."""
    def paint(p):
        mid = (p.h - 1) // 2
        for y in range(p.h):
            for x in range(p.w):
                p.put(x, y, shade(palette, len(palette) - 1 if y == mid else len(palette) - 2))
    paint.tag = repr(("glow", palette))
    return paint


# The pictures painted on the stalls' fronts, a texel a pixel: each row a string, `.` the panel's own paint.
CONE_SIGN = (
    ".OO.OO.",
    "OHLOLMO",
    "OMMMMDO",
    ".OgYgO.",
    "..gFg..",
    "...g...",
)
POPCORN_SIGN = (
    ".HLHLHL.",
    "HLMHLMHL",
    "gVOVOVOg",
    ".VOVOVO.",
    ".VOVOVO.",
    ".gggggg.",
)
BAT_SIGN = (
    "B..B.B..B",
    "BBBBBBBBB",
    ".BB...BB.",
)
SIGN_COLOURS = {"O": shade(FLOSS, 0), "H": shade(FLOSS, 4), "L": shade(FLOSS, 3), "M": shade(FLOSS, 2), "D": shade(FLOSS, 1),
                "g": shade(PAPER, 0), "Y": shade(PAPER, 2), "F": shade(PAPER, 1), "f": shade(PAPER, 0),
                "V": shade(VIOLET, 4), "B": shade(BLACK, 0)}
POPCORN_COLOURS = dict(SIGN_COLOURS, H=shade(POPCORN, 4), L=shade(POPCORN, 3), M=shade(POPCORN, 2), O=shade(ORANGE, 3),
                       g=shade(GOLD, 2))


def signed(picture, palette=VIOLET, colours=SIGN_COLOURS):
    """A lacquered panel (`lacquer`) with `picture` painted in the middle of it."""
    def paint(p):
        lacquer(palette)(p)
        h, w = len(picture), len(picture[0])
        x0, y0 = (p.w - w) // 2, (p.h - h) // 2
        for j, line in enumerate(picture):
            for i, ch in enumerate(line):
                if ch != "." and 0 <= x0 + i < p.w and 0 <= y0 + j < p.h:
                    p.put(x0 + i, y0 + j, colours[ch])
    paint.tag = repr(("signed", picture, palette))
    return paint


def crest():
    """The wagon's crest board: pumpkin-orange paint lit along its top row, a gold band along its foot, and a black bat
    across its middle (`BAT_SIGN`)."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                p.put(x, y, shade(GOLD, 2) if y == p.h - 1 else shade(ORANGE, 3 if y == 0 else 2))
        h, w = len(BAT_SIGN), len(BAT_SIGN[0])
        x0, y0 = (p.w - w) // 2, max(0, (p.h - 1 - h) // 2)
        for j, line in enumerate(BAT_SIGN):
            for i, ch in enumerate(line):
                if ch == "B" and 0 <= x0 + i < p.w:
                    p.put(x0 + i, y0 + j, shade(BLACK, 0))
    paint.tag = repr(("crest",))
    return paint


# A jack-o'-lantern's carved face, three texels high and wide enough for two eyes and a grin: `#` a hole.
FACE = (
    "#.#",
    "...",
    "###",
)


def pumpkin_side(face=False):
    """A pumpkin's side: ribs two texels wide in two tones of orange, lit along its top row; with `face`, its carved face
    in the middle, the holes the pumpkin's darkest tone."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                p.put(x, y, shade(PUMPKIN, 3 if y == 0 else 2 if (x // 2) % 2 == 0 else 1))
        if face:
            x0, y0 = (p.w - 3) // 2, max(0, (p.h - 3) // 2)
            for j, line in enumerate(FACE):
                for i, ch in enumerate(line):
                    if ch == "#" and y0 + j < p.h:
                        p.put(x0 + i, y0 + j, shade(PUMPKIN, 0))
    paint.tag = repr(("pumpkin_side", face))
    return paint


def face_glow():
    """The carved face lit from inside: only its holes, warm yellow, the rest cut out."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                clear(p, x, y)
        x0, y0 = (p.w - 3) // 2, max(0, (p.h - 3) // 2)
        for j, line in enumerate(FACE):
            for i, ch in enumerate(line):
                if ch == "#" and y0 + j < p.h:
                    p.put(x0 + i, y0 + j, shade(LAMP, 1 if j == 2 else 2))
    paint.tag = repr(("face_glow",))
    return paint


def glowing(elements):
    """`elements` full-bright and unshaded, for a glow."""
    for e in elements:
        e["light_emission"] = 15
        e["shade"] = False
    return elements


# ---------------------------------------------------------------- shapes

def octagon(sc, name, painter, cx, cz, a, t, y0, y1, top=None):
    """An eight-sided ring about (cx, cz), `a` from its middle to its walls' outsides, `t` thick, from y0 to y1: four walls
    square to the block, each an eight-sided ring's side long, and four turned 45 degrees about the middle, set a tenth of
    a pixel lower at their tops so no two tops share a plane."""
    side = 2 * a * math.tan(math.radians(22.5))
    cap = {"up": top} if top else {}
    out = [panel(sc, name, painter, (cx - side / 2, y0, cz - a), (cx + side / 2, y1, cz - a + t), **cap),
           panel(sc, name, painter, (cx - side / 2, y0, cz + a - t), (cx + side / 2, y1, cz + a), **cap),
           panel(sc, name, painter, (cx - a, y0, cz - side / 2), (cx - a + t, y1, cz + side / 2), **cap),
           panel(sc, name, painter, (cx + a - t, y0, cz - side / 2), (cx + a, y1, cz + side / 2), **cap)]
    for angle in (45, -45):
        for z_lo, z_hi in ((cz - a, cz - a + t), (cz + a - t, cz + a)):
            out.append(panel(sc, name, painter, (cx - side / 2, y0, z_lo), (cx + side / 2, y1 - 0.1, z_hi),
                             rot=rotation((cx, (y0 + y1) / 2, cz), "y", angle), **cap))
    return out


def octagon_floor(sc, name, painter, cx, cz, a, y0, y1):
    """An eight-sided floor about (cx, cz), `a` from its middle to its sides: a cross of two boxes square to the block and
    the same cross turned 45 degrees (together exactly the eight sides), each a hundredth of a pixel apart in height."""
    s = a * math.tan(math.radians(22.5))
    out = [panel(sc, name, painter, (cx - a, y0, cz - s), (cx + a, y1, cz + s)),
           panel(sc, name, painter, (cx - s, y0, cz - a), (cx + s, y1 - 0.01, cz + a))]
    for k, angle in enumerate((45, -45)):
        out.append(panel(sc, name, painter, (cx - a, y0, cz - s), (cx + a, y1 - 0.02 - 0.01 * k, cz + s),
                         rot=rotation((cx, (y0 + y1) / 2, cz), "y", angle)))
    return out


def wheel(sc, key, cy, cz, r, x0, x1, rim_t=1.0):
    """A spoked wheel standing across x (x0..x1), as the market's (pumpkin_patch_data.wheel) but in the midway's livery:
    a black iron tyre of eight segments, eight orange-painted spokes and a gold hub standing proud on both sides."""
    side = 2 * r * math.tan(math.radians(22.5)) + rim_t
    mid = (x0 + x1) / 2
    els = []
    top = ((cy + r - rim_t, cz - side / 2), (cy + r, cz + side / 2))
    bottom = ((cy - r, cz - side / 2), (cy - r + rim_t, cz + side / 2))
    tyre = flat(BLACK)
    for (y0, z0), (y1, z1) in (top, bottom):
        els.append(panel(sc, f"{key}_tyre", tyre, (x0, y0, z0), (x1, y1, z1)))
    for z0, z1 in ((cz - r, cz - r + rim_t), (cz + r - rim_t, cz + r)):
        els.append(panel(sc, f"{key}_tyre", tyre, (x0, cy - side / 2, z0), (x1, cy + side / 2, z1)))
    for (y0, z0), (y1, z1) in (top, bottom):
        for angle in (45, -45):
            els.append(panel(sc, f"{key}_tyre", tyre, (x0 + 0.1, y0, z0), (x1 - 0.1, y1, z1), rot=rotation((mid, cy, cz), "x", angle)))
    inner = r - rim_t + 0.2
    spoke = lengthwise(ORANGE)
    els.append(panel(sc, f"{key}_spoke", spoke, (x0 + 0.3, cy - inner, cz - 0.5), (x1 - 0.3, cy + inner, cz + 0.5)))
    els.append(panel(sc, f"{key}_spoke", spoke, (x0 + 0.3, cy - 0.5, cz - inner), (x1 - 0.3, cy + 0.5, cz + inner)))
    for angle in (45, -45):
        els.append(panel(sc, f"{key}_spoke", spoke, (x0 + 0.4, cy - inner, cz - 0.45), (x1 - 0.4, cy + inner, cz + 0.45),
                         rot=rotation((mid, cy, cz), "x", angle)))
    els.append(panel(sc, f"{key}_hub", ppd.hub(GOLD), (x0 - 0.5, cy - 1.0, cz - 1.0), (x1 + 0.5, cy + 1.0, cz + 1.0)))
    return els


# ---------------------------------------------------------------- the Cotton-Candy Cart

def cotton_candy_cart():
    """The Cotton-Candy Cart, facing north (its handle behind, to the south), one block across and two up: a violet
    lacquered cabinet 69 cm wide on an iron frame, two orange-spoked wheels either side on an axle, two iron legs at its
    front and a push handle behind with a wrapped grip; on its bone-white counter the steel bowl, eight-sided, 53 cm
    across and 17 cm deep, with the motor housing on its floor; two posts up its back to a canopy at 1.7 m, its striped
    valance hanging round it, stepping up in tiers of umbrella panels to a gold cap and finial at 2 m. Models: `whole`, `head` (the spinner
    head, its runner, drawn by client/CottonCandyCartRenderer), `heater` (its band glowing, while it runs) and `floss1` to
    `floss3` (the floss wound on the bowl's wall, deeper and higher a serving at a time)."""
    spec = hm.COTTON_CANDY
    sc = Sculpt(spec["block"], 2601, 128)
    cx, cz = spec["bowl"]
    a = spec["apothem"]
    top = spec["counter"]
    iron = flat(BLACK)
    el = [panel(sc, "frame", iron, (2.25, 2.0, 3.75), (13.75, 2.5, 12.75)),
          panel(sc, "cabinet", lacquer(), (2.5, 2.5, 4.0), (13.5, 12.0, 12.5), north=signed(CONE_SIGN)),
          panel(sc, "counter", flat(GOLD), (2.0, 12.0, 3.5), (14.0, top, 12.5), up=planks(BONE, board=3, nails=False)),
          panel(sc, "axle", iron, (2.0, 2.5, cz - 0.5), (14.0, 3.5, cz + 0.5))]
    for x0 in (3.0, 12.0):
        el.append(panel(sc, "leg", iron, (x0, 0.5, 4.5), (x0 + 1.0, 2.0, 5.5)))
        el.append(panel(sc, "foot", iron, (x0 - 0.25, 0.0, 4.25), (x0 + 1.25, 0.5, 5.75)))
    el += wheel(sc, "wheel", 3.0, cz, 3.0, 1.0, 2.0)
    el += wheel(sc, "wheel", 3.0, cz, 3.0, 14.0, 15.0)
    for x0 in (3.5, 11.75):
        el.append(panel(sc, "handle", iron, (x0, 9.0, 12.5), (x0 + 0.75, 9.75, 14.5)))
    el.append(panel(sc, "grip", wrapped(BLACK), (3.25, 8.9, 14.5), (12.75, 9.85, 15.25)))
    # The bowl: steel walls round an eight-sided floor, the motor's housing in its middle.
    steel = flat(STEEL)
    el += octagon(sc, "bowl", steel, cx, cz, a, 0.5, top, 15.5)
    el += octagon_floor(sc, "bowl_floor", steel, cx, cz, a - 0.25, top, top + 0.25)
    el.append(panel(sc, "housing", steel, (cx - 1.5, top + 0.25, cz - 1.5), (cx + 1.5, 13.75, cz + 1.5)))
    el.append(panel(sc, "spindle", steel, (cx - 0.25, 13.75, cz - 0.25), (cx + 0.25, 14.0, cz + 0.25)))
    # The canopy: two posts up its back to a striped roof stepping up to a gold finial.
    for x0 in (2.5, 12.75):
        el.append(panel(sc, "post", iron, (x0, 2.5, 12.5), (x0 + 0.75, 27.0, 13.25)))
    el += walls(sc, "valance", valance(), 0.5, 0.5, 15.5, 15.5, 0.25, 25.5, 27.0)
    for k, (lo, hi) in enumerate(((0.5, 15.5), (2.5, 13.5), (4.5, 11.5))):
        y = 27.0 + 0.75 * k
        el.append(panel(sc, f"canopy{k}", stripes(k), (lo, y, lo), (hi, y + 0.75, hi), up=sunburst(), down=sunburst()))
    el.append(panel(sc, "canopy_cap", flat(GOLD), (6.5, 29.25, 6.5), (9.5, 30.0, 9.5)))
    el.append(panel(sc, "finial", flat(GOLD), (7.5, 30.0, 7.5), (8.5, 31.0, 8.5)))
    el.append(panel(sc, "finial", flat(GOLD), (7.75, 31.0, 7.75), (8.25, 32.0, 8.25)))
    sc.models["whole"] = el
    # The spinner head: a slotted steel drum and the same turned 45 degrees, eight-sided, capped.
    head = [panel(sc, "head", slotted(), (cx - 1.25, 14.0, cz - 1.25), (cx + 1.25, 15.0, cz + 1.25), up=flat(STEEL)),
            panel(sc, "head", slotted(), (cx - 1.15, 14.05, cz - 1.15), (cx + 1.15, 14.95, cz + 1.15), up=flat(STEEL),
                  rot=rotation((cx, 14.5, cz), "y", 45)),
            panel(sc, "cap", flat(STEEL), (cx - 0.5, 15.0, cz - 0.5), (cx + 0.5, 15.25, cz + 0.5))]
    sc.models["head"] = head
    sc.models["heater"] = glowing([panel(sc, "heater", glow(HEAT), (cx - 1.0, 13.75, cz - 1.0), (cx + 1.0, 14.0, cz + 1.0),
                                         sides=("north", "south", "east", "west"))])
    for level in range(1, spec["floss"] + 1):
        depth = 0.25 + 0.5 * level
        height = top + 0.25 + 0.75 * level
        sc.models[f"floss{level}"] = octagon(sc, f"floss{level}", floss(), cx, cz, a - 0.5, depth, top + 0.25, height)
    return sc


# ---------------------------------------------------------------- the Caramel-Corn Wagon

def popcorn_wagon():
    """The Caramel-Corn Wagon, facing north (its handle behind), one block across and two up: a violet lacquered cabinet
    75 cm wide on big orange-spoked wheels and two iron legs, its front painted with a striped popcorn box; on its counter
    a glass case 70 cm high in gold corner posts, a steel kettle hung in it from two rods on a pivot, the case's lamp
    under its ceiling; the roof in two striped tiers, a crest board on its front with a black bat, and a jack-o'-lantern
    on top. Models: `whole`, `kettle` (the kettle, its lid, pins and dump handle: its runner, drawn by
    client/PopcornWagonRenderer), `lit` (the lamp and the lantern's face glowing) and `heap1` to `heap8` (the popcorn in
    the case, a serving at a time)."""
    spec = hm.POPCORN_WAGON
    sc = Sculpt(spec["block"], 2602, 128)
    py, pz = spec["pivot"]
    floor = spec["counter"]
    iron = flat(BLACK)
    gold = flat(GOLD)
    el = [panel(sc, "cabinet", lacquer(), (2.0, 3.0, 3.75), (14.0, 13.0, 12.25), north=signed(POPCORN_SIGN, colours=POPCORN_COLOURS)),
          panel(sc, "counter", gold, (1.5, 13.0, 3.25), (14.5, floor, 12.75), up=planks(BONE, board=3, nails=False)),
          panel(sc, "axle", iron, (1.25, 3.5, 7.5), (14.75, 4.5, 8.5))]
    for x0 in (3.0, 12.0):
        el.append(panel(sc, "leg", iron, (x0, 0.5, 4.0), (x0 + 1.0, 3.0, 5.0)))
        el.append(panel(sc, "foot", iron, (x0 - 0.25, 0.0, 3.75), (x0 + 1.25, 0.5, 5.25)))
    el += wheel(sc, "wheel", 4.0, 8.0, 4.0, 0.25, 1.25)
    el += wheel(sc, "wheel", 4.0, 8.0, 4.0, 14.75, 15.75)
    for x0 in (3.5, 11.75):
        el.append(panel(sc, "handle", iron, (x0, 10.0, 12.25), (x0 + 0.75, 10.75, 14.5)))
    el.append(panel(sc, "grip", wrapped(BLACK), (3.25, 9.9, 14.5), (12.75, 10.85, 15.25)))
    # The glass case: gold corner posts, a pane each side.
    import orchard_harvest_data as ohd
    glass = ohd.glass()
    ceiling = 24.5
    for x0 in (1.75, 13.5):
        for z0 in (3.5, 11.75):
            el.append(panel(sc, "post", gold, (x0, floor, z0), (x0 + 0.75, ceiling, z0 + 0.75)))
    el.append(panel(sc, "pane", glass, (2.5, floor, 3.75), (13.5, ceiling, 4.0)))
    el.append(panel(sc, "pane", glass, (2.5, floor, 12.0), (13.5, ceiling, 12.25)))
    for x0 in (1.9, 13.85):
        el.append(panel(sc, "pane", glass, (x0, floor, 4.25), (x0 + 0.25, ceiling, 11.75)))
    # The kettle's hangers: two rods from the ceiling down to its pivot.
    for x0 in (4.4, 11.0):
        el.append(panel(sc, "rod", flat(STEEL), (x0, py - 0.5, pz - 0.3), (x0 + 0.6, ceiling, pz + 0.3)))
    el.append(panel(sc, "lamp", flat(LAMP), (5.0, ceiling - 0.4, 4.5), (11.0, ceiling, 5.5)))
    # The roof: a ceiling board, two striped tiers, the crest board, and the lantern on a gold plinth on the crest.
    el.append(panel(sc, "ceiling", gold, (1.25, ceiling, 3.0), (14.75, ceiling + 0.75, 13.0)))
    el.append(panel(sc, "roof", stripes(), (1.0, ceiling + 0.75, 2.75), (15.0, ceiling + 1.5, 13.25)))
    el.append(panel(sc, "roof", stripes(1), (3.0, ceiling + 1.5, 4.25), (13.0, ceiling + 2.25, 11.75)))
    el.append(panel(sc, "crest", crest(), (3.5, ceiling + 0.75, 2.75), (12.5, 29.25, 3.25), sides=("north", "south", "up")))
    for x0, x1 in ((2.0, 3.5), (12.5, 14.0)):
        el.append(panel(sc, "crest_end", lacquer(ORANGE), (x0, ceiling + 0.75, 2.75), (x1, 28.0, 3.25)))
    el.append(panel(sc, "plinth", gold, (7.0, 29.25, 2.25), (9.0, 29.5, 3.75)))
    el.append(panel(sc, "lantern", pumpkin_side(), (6.75, 29.5, 1.75), (9.25, 31.5, 4.25), north=pumpkin_side(face=True)))
    el.append(panel(sc, "stem", lengthwise(STEM), (7.75, 31.5, 2.75), (8.25, 32.0, 3.25)))
    sc.models["whole"] = el
    sc.models["lit"] = glowing([panel(sc, "lamp_lit", glow(LAMP), (4.95, ceiling - 0.45, 4.45), (11.05, ceiling - 0.05, 5.55),
                                      sides=("north", "south", "east", "west", "down")),
                                panel(sc, "face", face_glow(), (6.75, 29.5, 1.7), (9.25, 31.5, 1.7), sides=("north",))])
    # The kettle, about its pivot: eight-sided steel walls round its floor, its lid with the agitator's motor, the pins it
    # hangs by and the dump handle out behind.
    steel = flat(STEEL)
    kettle = octagon(sc, "kettle", steel, 8.0, pz, 2.5, 0.35, py - 2.0, py + 0.25)
    kettle += octagon_floor(sc, "kettle_floor", steel, 8.0, pz, 2.3, py - 2.0, py - 1.75)
    kettle += octagon_floor(sc, "kettle_lid", steel, 8.0, pz, 2.55, py + 0.25, py + 0.5)
    kettle.append(panel(sc, "motor", steel, (7.25, py + 0.5, pz - 0.75), (8.75, py + 1.5, pz + 0.75)))
    for x0, x1 in ((5.0, 5.7), (10.3, 11.0)):
        kettle.append(panel(sc, "pin", flat(GOLD), (x0, py - 0.35, pz - 0.35), (x1, py + 0.35, pz + 0.35)))
    kettle.append(panel(sc, "dump", flat(BLACK), (7.65, py - 0.25, pz + 2.5), (8.35, py + 0.25, pz + 3.5)))
    kettle.append(panel(sc, "knob", flat(GOLD), (7.5, py - 0.4, pz + 3.5), (8.5, py + 0.4, pz + 3.75)))
    sc.models["kettle"] = kettle
    for level in range(1, spec["capacity"] + 1):
        y = floor + 0.4 * level
        heap = [panel(sc, "popcorn", popcorn(), (2.5, floor, 4.25), (13.5, y, 11.75), sides=("north", "south", "east", "west", "up"))]
        if level >= 4:
            heap.append(panel(sc, "popcorn", popcorn(), (4.5, y, 5.75), (11.5, y + 0.15 * level - 0.3, 10.25),
                              sides=("north", "south", "east", "west", "up")))
        sc.models[f"heap{level}"] = heap
    return sc


# ---------------------------------------------------------------- building and files

_built = {}
BUILDERS = {hm.COTTON_CANDY["block"]: cotton_candy_cart, hm.POPCORN_WAGON["block"]: popcorn_wagon}


def build(block):
    if block not in _built:
        _built[block] = ppd.planned(BUILDERS[block], 128)
    return _built[block]


def _image_of(name):
    return build(name).atlas.img if name in BUILDERS else None


def write_cut(write, models, block, name, elements, size):
    """A model cut into its blocks' parts (`<block>_<part>` plus `_<name>`), returning which parts have any."""
    suffix = "" if name == "whole" else f"_{name}"
    have = []
    for part, pieces in enumerate(ppd.cut(elements, size)):
        if pieces or name == "whole":
            write(models / f"{block}_{part}{suffix}.json", fa.model(block, pieces))
        have.append(bool(pieces))
    return have


def assets(root, write, lang):
    """Each stall's two blocks' models, cut from the whole, its overlays cut the same way (the cart's heater and floss,
    the wagon's glow and popcorn), its runner whole in its first block's frame (the cart's spinner head, the wagon's
    kettle), a multipart blockstate adding each by its property (the runner alone when `runner`), and its item (the
    stall whole at half size)."""
    write = fa.closing_writer(write, _image_of)
    models = root / "models" / "block"
    states = root / "blockstates"
    item = hm.CANDY["item"]
    lang[f"item.{MOD}.{item}"] = hm.CANDY["display"]
    for spec, runner, overlays in ((hm.COTTON_CANDY, "head", [("heater", {"lit": "true"})]
                                    + [(f"floss{n}", {"floss": str(n)}) for n in range(1, hm.COTTON_CANDY["floss"] + 1)]),
                                   (hm.POPCORN_WAGON, "kettle", [("lit", {"lit": "true"})]
                                    + [(f"heap{n}", {"popcorn": str(n)}) for n in range(1, hm.POPCORN_WAGON["capacity"] + 1)])):
        block = spec["block"]
        lang[f"block.{MOD}.{block}"] = spec["display"]
        sc = build(block)
        size = spec["size"]
        n = len(d17d.cells(size))
        write_cut(write, models, block, "whole", sc.models["whole"], size)
        have = {name: write_cut(write, models, block, name, sc.models[name], size) for name, _ in overlays}
        write(models / f"{block}_{runner}.json", fa.model(block, d20d.separated(sc.models[runner])))
        multipart = []
        for f, y in FACINGS.items():
            turn = {"y": y} if y else {}
            for p in range(n):
                plain = {"facing": f, "part": str(p), "runner": "false"}
                multipart.append({"when": plain, "apply": {"model": rid(f"block/{block}_{p}"), **turn}})
                for name, when in overlays:
                    if have[name][p]:
                        multipart.append({"when": dict(plain, **when), "apply": {"model": rid(f"block/{block}_{p}_{name}"), **turn}})
            multipart.append({"when": {"facing": f, "runner": "true"}, "apply": {"model": rid(f"block/{block}_{runner}"), **turn}})
        write(states / f"{block}.json", {"multipart": multipart})
        whole = sc.models["whole"] + sc.models[runner]
        write(root / "models" / "item" / f"{block}.json",
              fa.model(block, fa.transformed(d17d.pinned(d20d.separated(whole)), 0.5, (4.0, 0.0, 4.0), (0, 0, 0))))
        write(root / "items" / f"{block}.json", {"model": {"type": "minecraft:model", "model": rid(f"item/{block}")}})
    for key, words in (("cotton_candy_cart.off", "Switch it on first"), ("cotton_candy_cart.full", "The bowl is full of floss"),
                       ("cotton_candy_cart.empty", "Spin some sugar first"),
                       ("popcorn_wagon.light", "Light it with flint and steel or a fire charge"),
                       ("popcorn_wagon.popping", "The kettle is popping"), ("popcorn_wagon.full", "The case is full"),
                       ("popcorn_wagon.two", "Caramel coats two popcorn at a time")):
        lang[f"message.{MOD}.{key}"] = words


def counted(block, prop, most, item):
    """A pool dropping one `item` for each `prop` the stall's first block holds, none when it holds none."""
    holds = {"type": "minecraft:inverted", "term": {"type": "minecraft:match_block", "blocks": rid(block), "state": {prop: "0"}}}
    return {"condition": {"type": "minecraft:all_of", "terms": [
                {"type": "minecraft:match_block", "blocks": rid(block), "state": {"part": "0"}}, holds]},
            "entries": [{"type": "minecraft:item", "name": item, "modifier": [
                {"type": "minecraft:set_count", "count": n, "add": False,
                 "condition": {"type": "minecraft:match_block", "blocks": rid(block), "state": {prop: str(n)}}}
                for n in range(2, most + 1)]}], "rolls": 1}


def loot(out, write):
    """Each stall drops once, from its first block, with what it holds: a sugar for each serving of floss in the cart's
    bowl; the popcorn in the wagon's case, and the kernel in its kettle if it was popping."""
    for spec, extra in ((hm.COTTON_CANDY, [counted(hm.COTTON_CANDY["block"], "floss", hm.COTTON_CANDY["floss"], "minecraft:sugar")]),
                        (hm.POPCORN_WAGON, [counted(hm.POPCORN_WAGON["block"], "popcorn", hm.POPCORN_WAGON["capacity"], rid("popcorn")),
                                            {"condition": {"type": "minecraft:match_block", "blocks": rid(hm.POPCORN_WAGON["block"]),
                                                           "state": {"part": "0", "popping": "true"}},
                                             "entries": [{"type": "minecraft:item", "name": rid("corn_kernels")}], "rolls": 1}])):
        block = spec["block"]
        write(out / f"{block}.json", {"type": "minecraft:block", "pools": [{
            "condition": {"type": "minecraft:all_of", "terms": [{"type": "minecraft:survives_explosion"},
                                                                {"type": "minecraft:match_block", "blocks": rid(block), "state": {"part": "0"}}]},
            "entries": [{"type": "minecraft:item", "name": rid(block)}], "rolls": 1}] + extra, "random_sequence": rid(f"blocks/{block}")})


def tags(tags):
    """Both stalls are wooden carts: they come off with an axe."""
    for block in hm.blocks():
        tags.add("block", "minecraft:mineable/axe", rid(block))


def textures():
    import item_icons
    out = {("block", block): build(block).atlas.img for block in hm.blocks()}
    out[("item", hm.CANDY["item"])] = item_icons.draw(hm.CANDY["item"])
    return out

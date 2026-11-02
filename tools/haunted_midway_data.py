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
- The Haunted Carousel (part 5) is drawn by its renderer from quads (assets/jugcraft/haunted_midway_quads.json), on its
  own 256 x 256 texture (textures/entity/haunted_carousel.png; its middle block's and item's models are on a block
  texture of their own): its deck and canopy are turned solids of sixteen flat sides (`lathe`), each side painted for its
  own size, a texel a pixel, its panels' seams following their slant; its mounts and their poles are boxes, written out
  as quads (decor6_data.quads).
"""
import hashlib
import math

import decor17_data as d17d
import decor20_data as d20d
import flora_art as fa
import haunted_midway as hm
import pumpkin_patch_data as ppd
from flora_art import Sculpt, pal, rotation, shade
from decor6_data import quads
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


# ---------------------------------------------------------------- part 2: the Ticket Booth

TICKET_SIGN = (
    "YYYYYYYYY",
    "YYfYYYYYY",
    ".YfYBBBY.",
    "YYfYYYYYY",
    "YYYYYYYYY",
)
TICKET_COLOURS = {"Y": shade(BONE, 4), "f": shade(BONE, 1), "B": shade(BLACK, 0)}
GREEN = pal("2a4420", "3a5c2a", "4e7838", "649448", "7eb05c")
ZOMBIE_SIGN = (
    "HHHHHHH",
    "ZZZZZZZ",
    "ZEEZEEZ",
    "ZZZNZZZ",
    "ZMMMMMZ",
    "ZZZZZZZ",
)
ZOMBIE_COLOURS = {"H": shade(GREEN, 0), "Z": shade(GREEN, 2), "E": shade(BLACK, 0), "N": shade(GREEN, 1), "M": shade(BLACK, 2)}


def board(picture, colours, palette=ORANGE):
    """A sign board: its paint lit along its top row, a gold band along its foot, `picture` in its middle."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                p.put(x, y, shade(GOLD, 2) if y == p.h - 1 else shade(palette, 3 if y == 0 else 2))
        h, w = len(picture), len(picture[0])
        x0, y0 = (p.w - w) // 2, max(0, (p.h - h) // 2)
        for j, line in enumerate(picture):
            for i, ch in enumerate(line):
                if ch != "." and 0 <= x0 + i < p.w and 0 <= y0 + j < p.h:
                    p.put(x0 + i, y0 + j, colours[ch])
    paint.tag = repr(("board", picture, palette))
    return paint


def roll_end():
    """A paper roll's end: rings of paper, light and mid by turns, about a dark core."""
    def paint(p):
        cx, cy = (p.w - 1) / 2, (p.h - 1) / 2
        for y in range(p.h):
            for x in range(p.w):
                r = max(abs(x - cx), abs(y - cy))
                p.put(x, y, shade(BLACK, 2) if r < 0.6 else shade(PAPER, 2 if round(r) % 2 else 1))
    paint.tag = repr(("roll_end",))
    return paint


def ticket_booth():
    """The Ticket Booth, facing north (its back to the south), one block across and two up: a violet lacquered kiosk 87 cm
    wide on an iron plinth, a black bat painted on its front, a bone-white counter at 87 cm running out before its ticket
    window (glass in gold posts) and the brass ticket dispenser on it, its slot to the front and its crank's axle at its
    east side; a striped roof in two tiers and an orange crest board across its front painted with a ticket. Models:
    `whole`, `crank` (the crank's arm and knob: its runner, drawn by client/TicketBoothRenderer) and `roll1` and `roll2`
    (the paper on the roll, half full and full)."""
    spec = hm.TICKET_BOOTH
    sc = Sculpt(spec["block"], 2603, 128)
    ay, az = spec["axle"]
    gold, iron, brass = flat(GOLD), flat(BLACK), flat(GOLD)
    el = [panel(sc, "plinth", iron, (0.75, 0.0, 2.75), (15.25, 1.0, 15.25)),
          panel(sc, "cabinet", lacquer(), (1.0, 1.0, 3.0), (15.0, 14.0, 15.0), north=signed(BAT_SIGN)),
          panel(sc, "counter", gold, (0.5, 14.0, 1.0), (15.5, 14.75, 15.25), up=planks(BONE, board=3, nails=False))]
    for x0 in (1.0, 14.0):
        el.append(panel(sc, "post", gold, (x0, 14.75, 3.0), (x0 + 1.0, 26.0, 4.0)))
        el.append(panel(sc, "side", lacquer(), (x0, 14.75, 4.0), (x0 + 1.0, 26.0, 14.0)))
    el.append(panel(sc, "back", lacquer(), (1.0, 14.75, 14.0), (15.0, 26.0, 15.0)))
    import orchard_harvest_data as ohd
    el.append(panel(sc, "window", ohd.glass(), (2.0, 14.75, 3.25), (14.0, 26.0, 3.5)))
    el.append(panel(sc, "roof", stripes(), (0.5, 26.0, 2.5), (15.5, 26.75, 15.5)))
    el.append(panel(sc, "roof", stripes(1), (2.5, 26.75, 4.5), (13.5, 27.5, 13.5)))
    el.append(panel(sc, "crest", board(TICKET_SIGN, TICKET_COLOURS), (2.0, 26.75, 2.25), (14.0, 31.75, 2.75)))
    # The dispenser: a brass box on the counter, its ticket slot dark in its front, the crank's axle out of its east side.
    el.append(panel(sc, "dispenser", brass, (5.0, 14.75, 1.25), (10.0, 17.75, 3.0)))
    el.append(panel(sc, "slot", flat(BLACK), (6.0, 15.5, 1.15), (9.0, 15.85, 1.25), sides=("north",)))
    el.append(panel(sc, "axle", flat(STEEL), (10.0, ay - 0.25, az - 0.25), (10.5, ay + 0.25, az + 0.25)))
    for x0 in (5.25, 9.25):
        el.append(panel(sc, "cheek", brass, (x0, 17.75, 1.5), (x0 + 0.5, 19.5, 2.75)))
    sc.models["whole"] = el
    sc.models["crank"] = [panel(sc, "arm", flat(BLACK), (10.5, ay - 0.25, az - 0.25), (11.0, ay + 1.75, az + 0.25)),
                          panel(sc, "knob", lengthwise(STEM), (11.0, ay + 1.25, az - 0.35), (12.25, ay + 1.95, az + 0.35))]
    for stage, (lo, hi) in enumerate(((17.95, 18.95), (17.75, 19.5)), start=1):
        sc.models[f"roll{stage}"] = [panel(sc, f"roll{stage}", lengthwise(PAPER), (5.75, lo, 2.125 - (hi - lo) / 2),
                                           (9.25, hi, 2.125 + (hi - lo) / 2), east=roll_end(), west=roll_end())]
    return sc


# ---------------------------------------------------------------- part 2: Whack-a-Zombie

HAIR = pal("1c1612", "2a211a", "3a2e24")


def zombie_face():
    """A zombie head's front, four texels square: dark hair along its top, two black eyes set wide, a frowning mouth,
    its skin a sickly green."""
    def paint(p):
        face = ("HHHH", "EZZE", "ZZZZ", "ZMMZ")
        for y in range(p.h):
            for x in range(p.w):
                ch = face[min(y, 3)][min(x, 3)] if (p.w, p.h) == (4, 4) else ("H" if y == 0 else "Z")
                p.put(x, y, shade(HAIR, 1) if ch == "H" else ZOMBIE_COLOURS[ch])
    paint.tag = repr(("zombie_face",))
    return paint


def zombie_skin(top=False):
    """A zombie head's side, green lit along its left, its dark hair along its top row (all hair on its top)."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                p.put(x, y, shade(HAIR, 1 if top and (x + y) % 3 else 2) if top or y == 0 else shade(GREEN, 3 if x == 0 else 2))
    paint.tag = repr(("zombie_skin", top))
    return paint


def whack_a_zombie():
    """Whack-a-Zombie, facing north (its back board to the south), one block across and two up: a violet lacquered
    cabinet on an iron plinth, a zombie's face painted on its front, an orange playfield at 94 cm with five holes (black
    rubber rims round dark wells), and a back board up from the playfield's back edge with eight score lamps along its
    top in gold sockets, a zombie's face painted under them and a striped crest. Models: `whole`, `head` (a zombie's
    square head on its pole over the first hole: its runner, drawn risen from whichever hole is up by
    client/WhackAZombieRenderer) and `lamp1` to `lamp8` (each lamp lit, full-bright)."""
    spec = hm.WHACK
    sc = Sculpt(spec["block"], 2604, 128)
    field = spec["field"]
    iron = flat(BLACK)
    el = [panel(sc, "plinth", iron, (0.75, 0.0, 0.75), (15.25, 1.0, 14.75)),
          panel(sc, "cabinet", lacquer(), (1.0, 1.0, 1.0), (15.0, field - 1.5, 14.5), north=signed(ZOMBIE_SIGN, colours=ZOMBIE_COLOURS)),
          panel(sc, "field", flat(GOLD), (0.5, field - 1.5, 0.5), (15.5, field, 14.5), up=lacquer(ORANGE))]
    for hx, hz in spec["holes"]:
        el += octagon(sc, "rim", iron, hx, hz, 2.15, 0.35, field, field + 0.15)
        el += octagon_floor(sc, "well", flat(BLACK[:2]), hx, hz, 1.9, field, field + 0.05)
    el.append(panel(sc, "board", signed(ZOMBIE_SIGN, colours=ZOMBIE_COLOURS), (1.0, field, 13.0), (15.0, 29.0, 14.5)))
    lamps = []
    for k in range(spec["lamps"]):
        x = 1.375 + 1.75 * k
        el.append(panel(sc, "socket", flat(GOLD), (x - 0.15, 26.35, 12.8), (x + 1.15, 27.65, 13.0)))
        el.append(panel(sc, "bulb", flat(BONE), (x, 26.5, 12.4), (x + 1.0, 27.5, 12.8)))
        lamps.append(glowing([panel(sc, "bulb_lit", glow(LAMP), (x - 0.05, 26.45, 12.35), (x + 1.05, 27.55, 12.8),
                                    sides=("north", "east", "west", "up", "down"))]))
    el.append(panel(sc, "crest", stripes(), (0.5, 29.0, 12.75), (15.5, 30.0, 14.75)))
    el.append(panel(sc, "crest", board(BAT_SIGN, SIGN_COLOURS), (3.5, 30.0, 13.25), (12.5, 32.0, 13.75)))
    sc.models["whole"] = el
    for k, lamp in enumerate(lamps, start=1):
        sc.models[f"lamp{k}"] = lamp
    hx, hz = spec["holes"][0]
    r = spec["head"] / 2
    sc.models["head"] = [panel(sc, "pole", flat(STEEL), (hx - 0.4, field - 4.5, hz - 0.4), (hx + 0.4, field, hz + 0.4)),
                         panel(sc, "head", zombie_skin(), (hx - r, field, hz - r), (hx + r, field + spec["head"], hz + r),
                               north=zombie_face(), up=zombie_skin(top=True))]
    return sc


# ---------------------------------------------------------------- part 3: the Shooting Gallery

# The signs' letters, three texels wide and five high (SHOOTING, GHOST TRAIN, IN and OUT need only these).
LETTERS = {"S": ("###", "#..", "###", "..#", "###"), "H": ("#.#", "#.#", "###", "#.#", "#.#"),
           "O": ("###", "#.#", "#.#", "#.#", "###"), "T": ("###", ".#.", ".#.", ".#.", ".#."),
           "I": ("###", ".#.", ".#.", ".#.", "###"), "N": ("##.", "#.#", "#.#", "#.#", "#.#"),
           "G": ("###", "#..", "#.#", "#.#", "###"), "R": ("##.", "#.#", "##.", "#.#", "#.#"),
           "A": (".#.", "#.#", "###", "#.#", "#.#"), "U": ("#.#", "#.#", "#.#", "#.#", "###"), " ": ("...",) * 5}
BAT_TARGET = ("#.##.#", "######", ".#..#.")
GHOST_TARGET = (".###.", "#####", "#E#E#", "#####", "#.#.#")
GHOST_SIGN = GHOST_TARGET
BOARD_WOOD = pal("2a2018", "3a2c20", "4a3a2a", "5a4834")


def lettered(word):
    """The header board: violet lacquer lit along its top row with a gold band along its foot, `word` across its middle
    in bone-white letters three texels wide and five high, each letter's left column lit a tone."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                p.put(x, y, shade(GOLD, 2) if y == p.h - 1 else shade(VIOLET, 3 if y == 0 else 2))
        width = len(word) * 4 - 1
        x0, y0 = (p.w - width) // 2, max(0, (p.h - 1 - 5) // 2)
        for k, letter in enumerate(word):
            for j, line in enumerate(LETTERS[letter]):
                for i, ch in enumerate(line):
                    if ch == "#":
                        p.put(x0 + k * 4 + i, y0 + j, shade(BONE, 4 if i == 0 else 3))
    paint.tag = repr(("lettered", word))
    return paint


def night_sky():
    """The gallery's back wall: a night sky in the deepest violet, a crescent moon to the right between the rails and
    stars set on a slanting lattice (one every so many texels, each row of them a step along), never at random."""
    def paint(p):
        mx, my, r = p.w - 7.5, 9.5, 3.5
        for y in range(p.h):
            for x in range(p.w):
                d = math.hypot(x - mx, y - my)
                bite = math.hypot(x - mx - 1.5, y - my + 1.0)
                if d <= r and bite > r - 0.5:
                    p.put(x, y, shade(BONE, 4 if d < r - 1 else 3))
                elif (x * 3 + y * 7) % 47 == 0 and y < p.h - 6:
                    p.put(x, y, shade(BONE, 3))
                else:
                    p.put(x, y, shade(VIOLET, 0 if y < p.h // 2 else 1))
    paint.tag = repr(("night_sky",))
    return paint


def cutout(shape, palette, colours=None):
    """A tin target's face: `shape` painted (its top row lit a tone), the rest cut out, so the card is its silhouette."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                ch = shape[y][x] if y < len(shape) and x < len(shape[0]) else "."
                if ch == ".":
                    clear(p, x, y)
                elif colours and ch in colours:
                    p.put(x, y, colours[ch])
                else:
                    p.put(x, y, shade(palette, 3 if y == 0 else 2))
    paint.tag = repr(("cutout", shape, palette))
    return paint


def gallery_front():
    """The counter's front: violet lacquer in its gold bead, a ghost painted at its middle and a bat at each quarter."""
    def paint(p):
        lacquer()(p)
        for picture, centre, colours in ((BAT_SIGN, p.w // 4, SIGN_COLOURS), (GHOST_SIGN, p.w // 2, {"#": shade(BONE, 4), "E": shade(BLACK, 0)}),
                                         (BAT_SIGN, 3 * p.w // 4, SIGN_COLOURS)):
            h, w = len(picture), len(picture[0])
            x0, y0 = centre - w // 2, (p.h - h) // 2
            for j, line in enumerate(picture):
                for i, ch in enumerate(line):
                    if ch != ".":
                        p.put(x0 + i, y0 + j, colours[ch] if ch in colours else shade(BLACK, 0))
    paint.tag = repr(("gallery_front",))
    return paint


def shooting_gallery():
    """The Shooting Gallery, facing north (its back wall to the south), three blocks across and two up: a violet counter
    at 88 cm across its front (a ghost and two bats painted on it, a bone-white top), side walls and gold front posts, a
    board floor, a night sky on its back wall, the two gold rails across it on brackets with orange covers hiding their
    ends, and its header board lettered SHOOTING with a row of bulbs under it, under a striped roof. Models: `whole`,
    `bat` and `ghost` (one tin target each, standing on its post at the frame's left edge, x 0, on its rail: the runners,
    drawn by client/ShootingGalleryRenderer at each target's place) and `bulbs_lit` (the bulbs glowing, while a round
    runs)."""
    spec = hm.GALLERY
    sc = Sculpt(spec["block"], 2605, 128)
    top = spec["counter"]
    gold, iron = flat(GOLD), flat(BLACK)
    el = [panel(sc, "counter", lacquer(), (0.0, 0.0, 0.0), (48.0, top - 0.75, 4.0), north=gallery_front()),
          panel(sc, "counter_top", gold, (0.0, top - 0.75, 0.0), (48.0, top, 4.5), up=planks(BONE, board=3, nails=False)),
          panel(sc, "floor", planks(BOARD_WOOD, board=3, nails=False), (1.0, 0.0, 4.0), (47.0, 0.5, 15.0)),
          panel(sc, "back", night_sky(), (1.0, 0.0, 15.0), (47.0, 31.5, 16.0)),
          panel(sc, "roof", stripes(), (0.0, 31.5, 0.5), (48.0, 32.0, 16.0)),
          panel(sc, "header", lettered("SHOOTING"), (0.0, 25.0, 0.0), (48.0, 32.0, 0.5))]
    for x0 in (0.0, 47.0):
        el.append(panel(sc, "wall", lacquer(), (x0, 0.0, 4.0), (x0 + 1.0, 31.5, 15.0)))
        el.append(panel(sc, "post", gold, (x0, top, 0.0), (x0 + 1.0, 25.0, 1.0)))
    lo, hi = spec["track"]
    for rail_y, rail_z, _, _ in spec["rails"]:
        el.append(panel(sc, "rail", gold, (lo, rail_y - 0.5, rail_z - 0.5), (hi, rail_y, rail_z + 0.5)))
        for bx in (lo + 1.0, (lo + hi) / 2, hi - 1.5):
            el.append(panel(sc, "bracket", iron, (bx, rail_y - 0.5, rail_z + 0.5), (bx + 0.5, rail_y, 15.0)))
    for x0, x1 in ((1.0, 5.0), (43.0, 47.0)):
        el.append(panel(sc, "cover", lacquer(ORANGE), (x0, top, 10.25), (x1, 25.0, 10.75)))
    bulbs = []
    for k in range(spec["bulbs"]):
        x = 2.0 + 4.4 * k
        el.append(panel(sc, "bulb", flat(BONE), (x - 0.3, 24.3, 0.1), (x + 0.3, 24.9, 0.7)))
        bulbs += glowing([panel(sc, "bulb_lit", glow(LAMP), (x - 0.35, 24.25, 0.05), (x + 0.35, 24.95, 0.75),
                                sides=("north", "east", "west", "down"))])
    sc.models["whole"] = el
    sc.models["bulbs_lit"] = bulbs
    # The targets, each at x 0 of the frame (the renderer moves them along their rails).
    ghost_colours = {"E": shade(BLACK, 0)}
    # The bats are tin painted pumpkin orange, so they show against the night sky; the ghosts bone-white.
    for (rail_y, rail_z, _, kind), shape, palette, colours in zip(spec["rails"], (BAT_TARGET, GHOST_TARGET), (ORANGE, BONE), (None, ghost_colours)):
        w, h = spec["targets"][kind]
        sc.models[kind] = [panel(sc, f"{kind}_post", flat(STEEL), (-0.25, rail_y, rail_z - 0.1), (0.25, rail_y + 0.5, rail_z + 0.1)),
                           panel(sc, kind, cutout(shape, palette, colours), (-w / 2, rail_y + 0.5, rail_z - 0.1), (w / 2, rail_y + 0.5 + h, rail_z + 0.1),
                                 sides=("north", "south"))]
    return sc


# ---------------------------------------------------------------- part 4: the Ghost Train

SIDING = pal("221c2a", "2e2638", "3a3046", "463a54", "544864")
STONE_FOOT = pal("2a2a2e", "3a3a40", "4a4a52", "5a5a64")


def clapboard():
    """Clapboard siding: boards laid along the wall three texels high, each lit along its top row and shadowed along its
    lower edge where the next board laps over it, in a haunted house's faded violet."""
    def paint(p):
        for y in range(p.h):
            within = y % 3
            for x in range(p.w):
                p.put(x, y, shade(SIDING, 3 if within == 0 else 0 if within == 2 else 2))
    paint.tag = repr(("clapboard",))
    return paint


def window(lit):
    """A window four panes square in a gold frame: unlit, dark glass with a lighter streak; lit, warm lamplight in steady
    bands, palest in its middle. Either way a ghost stands in it, black against the light, the light showing through its
    eyes."""
    ghost = (".###.", "#.#.#", "#####", "#####", "#.#.#")
    def paint(p):
        mid = (p.h - 1) / 2
        for y in range(p.h):
            for x in range(p.w):
                frame = x in (0, p.w - 1) or y in (0, p.h - 1) or x == p.w // 2 or y == p.h // 2
                if frame:
                    p.put(x, y, shade(GOLD, 2))
                elif lit:
                    p.put(x, y, shade(LAMP, 2 - min(2, int(abs(y - mid) / 2))))
                else:
                    p.put(x, y, shade(VIOLET, 1 if (x + y) % 5 else 2))
        x0, y0 = (p.w - 5) // 2, p.h - 6
        for j, line in enumerate(ghost):
            for i, ch in enumerate(line):
                if ch == "#":
                    p.put(x0 + i, y0 + j, shade(BLACK, 0))
    paint.tag = repr(("window", lit))
    return paint


def door_leaf():
    """A door leaf: boards up it in dark wood, two gold straps across it near its top and foot."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                strap = y in (3, 4, p.h - 5, p.h - 4)
                if strap:
                    p.put(x, y, shade(GOLD, 3 if y in (3, p.h - 5) else 2))
                else:
                    p.put(x, y, shade(BOARD_WOOD, 0 if x % 3 == 2 else 3 if x % 3 == 0 else 2))
    paint.tag = repr(("door_leaf",))
    return paint


def train_cells():
    """The Ghost Train's blocks in part order, {right, up} from its first block as GhostTrainBlock counts them: five
    across and four up, less the two doorways' lower two blocks."""
    w, h, _ = hm.GHOST_TRAIN["size"]
    return [(r, u) for u in range(h) for r in range(w) if not (r in hm.GHOST_TRAIN["doors"] and u < 2)]


def cut_cells(elements, size, cells):
    """A model drawn whole in its frame cut into the blocks `cells` ({right, up}) holds, in that order, each piece moved
    into its block (as pumpkin_patch_data.cut, for a prop with gaps in it); what lies in a gap is not drawn."""
    elements = d20d.separated(elements)
    parts = []
    for right, up in cells:
        cell = (size[0] - 1 - right, up, 0)
        parts.append(d17d.cell_part(d17d.pinned(elements), cell, size))
    return parts


def ghost_train():
    """The Ghost Train, facing north (the ride behind it, to the south), five blocks across and four up: a haunted
    house's clapboard front on a stone footing, two doorways at track level a block across and two high (OUT at the
    frame's x 16 to 32, IN at 48 to 64), each in orange jambs under a board lettered with its name, a ghost painted on the
    wall between them, three windows with ghosts in them above, a board lettered GHOST TRAIN under the striped eaves, a
    crest with a bat and pumpkins at the eaves' corners. Models: `whole`, `windows_lit` (the windows lit from inside),
    and its runners: `leaf_left` and `leaf_right` (the IN doorway's two door leaves, hinged at its sides; the renderer
    swings them, and draws the OUT doorway's from the same, moved) and `spook` (the ghost that springs up over the IN
    doors)."""
    spec = hm.GHOST_TRAIN
    sc = Sculpt(spec["block"], 2606, 128)
    z0, z1 = spec["wall"]
    siding = clapboard()
    gold = flat(GOLD)
    el = []
    for x0, x1 in ((0.0, 16.0), (32.0, 48.0), (64.0, 80.0)):
        el.append(panel(sc, "foot", flat(STONE_FOOT), (x0, 0.0, 0.0), (x1, 1.5, z1)))
        el.append(panel(sc, "wall", siding, (x0, 1.5, z0), (x1, 32.0, z1)))
    el.append(panel(sc, "wall", siding, (0.0, 32.0, z0), (80.0, 60.0, z1)))
    el.append(panel(sc, "ghost_sign", signed(GHOST_SIGN, colours={"#": shade(BONE, 4), "E": shade(BLACK, 0)}), (35.0, 8.0, z0 - 0.25),
                    (45.0, 24.0, z0)))
    for (x0, x1), word in (((16.0, 32.0), "OUT"), ((48.0, 64.0), "IN")):
        for jx in (x0 - 2.0, x1):
            el.append(panel(sc, "jamb", lacquer(ORANGE), (jx, 1.5, z0 - 1.5), (jx + 2.0, 34.0, z1)))
        el.append(panel(sc, "lintel", gold, (x0 - 2.0, 32.0, z0 - 1.5), (x1 + 2.0, 34.0, z1)))
        # Its sign stands off the wall on two brackets, so the IN doorway's ghost waits hidden behind it.
        el.append(panel(sc, "door_sign", lettered(word), (x0, 34.0, z0 - 1.5), (x1, 41.0, z0 - 1.0)))
        for bx in (x0 + 2.0, x1 - 2.5):
            el.append(panel(sc, "bracket", flat(BLACK), (bx, 36.0, z0 - 1.0), (bx + 0.5, 36.5, z0)))
    windows = []
    for x0 in (4.0, 36.0, 68.0):
        el.append(panel(sc, "window", window(False), (x0, 43.0, z0 - 0.5), (x0 + 8.0, 53.0, z0)))
        windows += glowing([panel(sc, "window_lit", window(True), (x0 - 0.05, 42.95, z0 - 0.55), (x0 + 8.05, 53.05, z0 - 0.5),
                                  sides=("north",))])
    el.append(panel(sc, "sign", lettered("GHOST TRAIN"), (16.0, 53.0, z0 - 0.5), (64.0, 60.0, z0)))
    el.append(panel(sc, "eaves", stripes(), (0.0, 60.0, 0.0), (80.0, 61.0, 6.0)))
    el.append(panel(sc, "crest", board(BAT_SIGN, SIGN_COLOURS), (34.0, 61.0, 1.0), (46.0, 64.0, 1.5)))
    for px in (1.5, 76.0):
        el.append(panel(sc, "pumpkin", pumpkin_side(), (px, 61.0, 1.5), (px + 2.5, 63.0, 4.0), north=pumpkin_side(face=True)))
        el.append(panel(sc, "stem", lengthwise(STEM), (px + 1.0, 63.0, 2.5), (px + 1.5, 63.75, 3.0)))
    sc.models["whole"] = el
    sc.models["windows_lit"] = windows
    # The IN doorway's leaves, in the first block's own model (the frame less 64 across), hinged at the doorway's sides.
    lw, lh = spec["leaf"]
    leaf_z = (z0 + z1) / 2
    sc.models["leaf_left"] = [panel(sc, "leaf", door_leaf(), (-16.0, 1.5, leaf_z - 0.25), (-16.0 + lw, 1.5 + lh, leaf_z + 0.25))]
    sc.models["leaf_right"] = [panel(sc, "leaf", door_leaf(), (-lw, 1.5, leaf_z - 0.25), (0.0, 1.5 + lh, leaf_z + 0.25))]
    # The ghost on its spring, a block lower than it springs (a block model stays within -16..32): the renderer lifts it
    # a block, over the IN sign, and wobbles it about its spring's top (y 25 here).
    ghost_colours = {"E": shade(BLACK, 0)}
    sc.models["spook"] = [panel(sc, "spring", flat(STEEL), (-8.25, 19.0, z0 - 0.75), (-7.75, 25.0, z0 - 0.25)),
                          panel(sc, "spook", cutout(GHOST_TARGET, BONE, ghost_colours), (-10.5, 25.0, z0 - 0.75), (-5.5, 30.0, z0 - 0.25),
                                sides=("north", "south"))]
    return sc

# ---------------------------------------------------------------- the Haunted Carousel (part 5)

# Its turning parts in pixels about its middle (x along slot 0, y up from its foot, z so that a slot's angle turns x
# toward -z, as the renderer's Axis.YP does): a deck of SIDES flat sides, the column's eight, the canopy's SIDES.
RIDE = f"entity/{hm.CAROUSEL['entity']}"
SIDES = 16
DECK_IN, DECK_OUT, DECK_TOP, TRIM = 9.0, 54.0, 3.0, 3.0
COLUMN, COLLAR = 7.0, 8.0
CEILING, BOARD_TOP, ROOF_TOP, DRUM, DRUM_TOP = 48.0, 58.0, 70.0, 6.0, 72.0
VALANCE = 3.5
SWEEP = (45.5, 48.0)
POLE = 1.25
BULB_ROWS = (49.5, 56.5)
BULB_ACROSS = (-5.5, 5.5)
WING = 12.0
PUMPKIN_SIGN = (
    "...s...",
    ".OOsOO.",
    "OYOOOYO",
    "OOOYOOO",
    "OYYYYYO",
    ".OOOOO.",
)
PUMPKIN_COLOURS = {"O": shade(ORANGE, 3), "s": shade(GREEN, 2), "Y": shade(LAMP, 1)}
BAT_COLOURS = {"B": shade(BLACK, 0)}


def _tag(painter):
    return hashlib.sha1(getattr(painter, "tag", repr(painter)).encode()).hexdigest()[:8]


def lathe(sc, key, painters, profile, sides=SIDES, phase=0.0, cutout=False):
    """The quads of a turned solid about the y axis, `sides` flat facets round, the first facing x (or turned `phase`
    radians on): `profile` the (apothem, y) points of its outline in order round it, counter-clockwise seen with the
    apothem to the right and y up, so each band's facets face out of it. `painters` gives each band (each pair of points)
    its painter, or a list for its facets by turns. A band's facets share their pieces, a texel to a pixel across and along
    them, each facet's narrower end in the middle of its piece; down a piece runs from the band's higher end (or, for a
    level band, its outer end). Quads in decor6_data.quads()'s format, on the carousel's own texture (`RIDE`)."""
    out = []
    half = math.tan(math.pi / sides)
    for band, (p0, p1) in enumerate(zip(profile, profile[1:])):
        (a0, y0), (a1, y1) = p0, p1
        length = math.hypot(a1 - a0, y1 - y0)
        na, ny = (y1 - y0) / length, -(a1 - a0) / length
        w = max(1, int(math.ceil(2 * max(a0, a1) * half - 1e-6)))
        h = max(1, int(math.ceil(length - 1e-6)))
        start, end = (p1, p0) if (y1 > y0 or (y1 == y0 and a1 > a0)) else (p0, p1)
        band_painters = painters[band] if isinstance(painters[band], (list, tuple)) else [painters[band]]
        for k in range(sides):
            painter = band_painters[k % len(band_painters)]
            u0, v0, u1, v1 = sc.piece(f"{key}{band}_{_tag(painter)}_{w}x{h}", w, h, painter)
            c = phase + k * 2 * math.pi / sides
            ux, uz, wx, wz = math.cos(c), -math.sin(c), -math.sin(c), -math.cos(c)
            corners = []
            for (a, y), t in ((start, 0.0), (end, length)):
                for side in (-1.0, 1.0):
                    s = side * a * half
                    corners.append(([a * ux + s * wx, y, a * uz + s * wz], s, t))
            quad = [corners[0], corners[1], corners[3], corners[2]]
            normal = [na * ux, ny, na * uz]
            p = [q[0] for q in quad]
            e1 = [p[1][i] - p[0][i] for i in range(3)]
            e2 = [p[2][i] - p[1][i] for i in range(3)]
            if abs(sum(abs(v) for v in e1)) < 1e-9:
                e1, e2 = e2, [p[3][i] - p[2][i] for i in range(3)]
            cross = [e1[1] * e2[2] - e1[2] * e2[1], e1[2] * e2[0] - e1[0] * e2[2], e1[0] * e2[1] - e1[1] * e2[0]]
            if sum(cross[i] * normal[i] for i in range(3)) < 0:
                quad.reverse()
            vertices = [[round(pt[0], 4), round(pt[1], 4), round(pt[2], 4), round((u0 + (w / 2 + s) / w * (u1 - u0)) / 16, 5),
                         round((v0 + t / h * (v1 - v0)) / 16, 5)] for pt, s, t in quad]
            quad_out = {"texture": RIDE, "normal": [round(n, 4) for n in normal], "vertices": vertices}
            if cutout:
                quad_out["cutout"] = True
            out.append(quad_out)
    return out


def turned_y(origin, degrees):
    """An element rotation about y by any angle, for the renderer's quads (decor6_data.quads turns them; block models
    could not)."""
    return {"origin": list(origin), "axis": "y", "angle": degrees} if degrees else None


def panel_seams(palette, narrow, narrow_top=True, seam=GOLD):
    """A canopy's or ceiling's panel, a facet narrowing to `narrow` of its width at its piece's top (or, not
    `narrow_top`, its foot): its field in the palette's middle tone with a lit line just inside its edges, and a gold seam
    along its slanting sides (painted out to the piece's corners, so all of it is opaque)."""
    def paint(p):
        for y in range(p.h):
            t = (y + 0.5) / p.h if narrow_top else 1.0 - (y + 0.5) / p.h
            edge = p.w / 2 * (narrow + (1.0 - narrow) * t)
            for x in range(p.w):
                d = abs(x + 0.5 - p.w / 2)
                if d > edge - 1.0:
                    p.put(x, y, shade(seam, 3 if d <= edge else 2))
                elif d > edge - 2.0:
                    p.put(x, y, shade(palette, 3))
                else:
                    p.put(x, y, shade(palette, 2))
    paint.tag = repr(("panel_seams", palette, round(narrow, 4), narrow_top, seam))
    return paint


def skirt():
    """The deck's edge: a gold binding along its top, an orange band and a black foot."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                p.put(x, y, shade(GOLD, 3) if y == 0 else shade(ORANGE, 2) if y < p.h - 1 else shade(BLACK, 1))
    paint.tag = repr(("skirt",))
    return paint


def mirror():
    """One of the column's mirrors: a gold frame round silvered glass, faintly violet, in three bands from bright at the
    top to dim at the foot, crossed by one slanting highlight."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                if x in (0, p.w - 1) or y in (0, p.h - 1):
                    p.put(x, y, shade(GOLD, 3 if (x == 0 or y == 0) else 1))
                elif (x + y) % (p.h // 2 or 1) == 2 and 0 < x < p.w - 1:
                    p.put(x, y, shade(STEEL, 4))
                else:
                    band = 3 * y // p.h
                    p.put(x, y, shade(STEEL, 3 - band) if band < 2 else shade(VIOLET, 3))
    paint.tag = repr(("mirror",))
    return paint


def rounding_board(picture, field, colours):
    """A rounding board: its field (`field`'s middle tone) between a gold rail along its top and foot, an orange pinstripe
    inside each, and `picture` in its middle."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                if y in (0, p.h - 1):
                    p.put(x, y, shade(GOLD, 3 if y == 0 else 2))
                elif y in (1, p.h - 2):
                    p.put(x, y, shade(ORANGE if field is BLACK else BLACK, 2))
                else:
                    p.put(x, y, shade(field, 2))
        h, w = len(picture), len(picture[0])
        x0, y0 = (p.w - w) // 2, (p.h - h) // 2
        for j, line in enumerate(picture):
            for i, ch in enumerate(line):
                if ch != "." and 0 <= x0 + i < p.w and 0 <= y0 + j < p.h:
                    p.put(x0 + i, y0 + j, colours[ch])
    paint.tag = repr(("rounding_board", picture, field))
    return paint


def coat(palette=BLACK):
    """A carousel horse's lacquered black coat: three smooth bands, lit along the top, darkest at the belly."""
    def paint(p):
        for y in range(p.h):
            k = 3 if y == 0 and p.h > 2 else (2, 1, 0)[min(2, 3 * y // p.h)]
            for x in range(p.w):
                p.put(x, y, shade(palette, k))
    paint.tag = repr(("coat", palette))
    return paint


def fur():
    """A bat's fur: short tufts in rows, each row's a texel on from the last, two tones of black."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                p.put(x, y, shade(BLACK, 2 if (x + y // 2) % 3 == 0 else 1))
    paint.tag = repr(("fur",))
    return paint


def eyed(base, eye, front_right):
    """A head's side, `base` painted, with a glinting orange eye near its front top: toward the piece's right when
    `front_right` (as an east side shows a north-facing head), else its left."""
    def paint(p):
        base(p)
        x = p.w - 2 if front_right else 1
        y = 1 if p.h > 2 else 0
        p.put(x, y, shade(ORANGE, 4))
        if p.h > 3:
            p.put(x, y + 1, shade(ORANGE, 2))
    paint.tag = repr(("eyed", base.tag, eye, front_right))
    return paint


def eyes_front():
    """A bat's face: its fur, two orange eyes, lit, above its snout."""
    def paint(p):
        fur()(p)
        for x in (p.w // 2 - 2, p.w // 2 + 1):
            p.put(x, 1, shade(ORANGE, 4))
            p.put(x, 2, shade(ORANGE, 2))
    paint.tag = repr(("eyes_front",))
    return paint


def membrane(left):
    """A bat's wing seen from above, `WING` texels out from its hinge (at the piece's left, or its right for the `left`
    wing) and ten from front (top) to back: its skin in three panels fanning out from the hinge between its finger bones,
    dark and darker by turns (the bones the panels' edges, not drawn lines: at a texel a pixel they would only step), the
    trailing edge scalloped between the fingers' tips (cut out), a lit bone along the front."""
    def paint(p):
        tips = (0.0, p.w / 3.0, 2 * p.w / 3.0, float(p.w))
        for y in range(p.h):
            for x in range(p.w):
                u = (p.w - 1 - x if left else x) + 0.5
                k = max(i for i in range(3) if tips[i] <= u)
                frac = (u - tips[k]) / (tips[k + 1] - tips[k])
                if y + 0.5 > p.h - 2.5 * math.sin(math.pi * frac):
                    clear(p, x, y)
                elif y == 0:
                    p.put(x, y, shade(BLACK, 3))
                else:
                    # Which panel: between the fingers fanning from the hinge to the tips along the trailing edge.
                    spread = u * (p.h - 0.5) / max(y + 0.5, 0.5)
                    panel_k = sum(1 for t in tips[1:3] if spread > t)
                    p.put(x, y, shade(VIOLET, 0) if panel_k % 2 == 0 else shade(BLACK, 1))
    paint.tag = repr(("membrane", left))
    return paint


def carousel_spin(sc):
    """The parts that turn as one: the deck (its boards running out from the middle, a gold trim round its edge and a
    banded skirt), the mirrored column, the canopy (ceiling, rounding boards with a bat or a jack-o'-lantern on each,
    roof panels orange and black, a gold drum on top and a jack-o'-lantern over it), its scalloped valance, the eight
    sweeps under the ceiling and the brass poles each horse and bat rides."""
    boards = planks(BOARD_WOOD)
    quads_out = lathe(sc, "deck", [flat(BLACK), skirt(), flat(GOLD), boards, flat(BLACK)],
                      [(DECK_IN, 0.0), (DECK_OUT, 0.0), (DECK_OUT, DECK_TOP), (DECK_OUT - TRIM, DECK_TOP), (DECK_IN, DECK_TOP),
                       (DECK_IN, 0.0)])
    # The column stands in its collars (each a closed ring), its foot half a pixel up inside the lower one.
    quads_out += lathe(sc, "column", [flat(BLACK), mirror()], [(0.0, 16.5), (COLUMN, 16.5), (COLUMN, CEILING)], sides=8,
                       phase=math.pi / 8)
    for y0, y1 in ((16.0, 18.0), (CEILING - 2.5, CEILING)):
        quads_out += lathe(sc, f"collar{int(y0)}_", [flat(GOLD)] * 4,
                           [(COLUMN - 0.5, y0), (COLLAR, y0), (COLLAR, y1), (COLUMN - 0.5, y1), (COLUMN - 0.5, y0)])
    narrow_roof = DRUM / DECK_OUT
    quads_out += lathe(sc, "canopy", [[panel_seams(VIOLET, 0.0, False), panel_seams(BLACK, 0.0, False)],
                                      [rounding_board(PUMPKIN_SIGN, BLACK, PUMPKIN_COLOURS), rounding_board(BAT_SIGN, ORANGE, BAT_COLOURS)],
                                      [panel_seams(ORANGE, narrow_roof), panel_seams(BLACK, narrow_roof)], flat(GOLD), flat(GOLD)],
                       [(0.0, CEILING), (DECK_OUT, CEILING), (DECK_OUT, BOARD_TOP), (DRUM, ROOF_TOP), (DRUM, DRUM_TOP), (0.0, DRUM_TOP)])
    quads_out += lathe(sc, "valance", [valance()], [(DECK_OUT, CEILING - VALANCE), (DECK_OUT, CEILING)], cutout=True)
    el = [panel(sc, "finial", pumpkin_side(), (-4.0, DRUM_TOP, -4.0), (4.0, DRUM_TOP + 6.0, 4.0), north=pumpkin_side(face=True),
                south=pumpkin_side(face=True)),
          panel(sc, "finial_stem", lengthwise(STEM), (-0.75, DRUM_TOP + 6.0, -0.75), (0.75, DRUM_TOP + 7.5, 0.75))]
    sweep = flat(GOLD)
    poles = wrapped(GOLD)
    for slot in range(len(hm.CAROUSEL["mounts"])):
        degrees = 45.0 * slot
        el.append(panel(sc, "sweep", sweep, (COLLAR + 0.5, SWEEP[0], -1.0), (DECK_OUT - 0.5, SWEEP[1], 1.0),
                        rot=turned_y((0.0, SWEEP[0], 0.0), degrees)))
        rings = [hm.CAROUSEL["outer"]] if hm.CAROUSEL["mounts"][slot] != "coach" else []
        rings += [hm.CAROUSEL["inner"]] if slot in hm.CAROUSEL["inner_slots"] else []
        for ring in rings:
            r = ring * 16
            # Each pole's top is buried in its crank, so no face of it shares the crank's or the sweep's plane.
            el.append(panel(sc, "pole", poles, (r - POLE / 2, DECK_TOP, -POLE / 2), (r + POLE / 2, SWEEP[0] - 0.75, POLE / 2),
                            rot=turned_y((0.0, DECK_TOP, 0.0), degrees)))
            el.append(panel(sc, "crank", flat(BLACK), (r - 1.25, SWEEP[0] - 1.5, -1.25), (r + 1.25, SWEEP[0], 1.25),
                            rot=turned_y((0.0, SWEEP[0], 0.0), degrees)))
    sc.models["spin_boxes"] = el
    return quads_out


def carousel_bulbs(sc):
    """Its bulbs, a row along the top and the foot of each rounding board, two to a board, standing out from it."""
    lamp = glow(LAMP)
    el = []
    for k in range(SIDES):
        degrees = k * 360.0 / SIDES
        for y in BULB_ROWS:
            for s in BULB_ACROSS:
                el.append(panel(sc, "bulb", lamp, (DECK_OUT, y - 0.5, s - 0.5), (DECK_OUT + 0.75, y + 0.5, s + 0.5),
                                rot=turned_y((0.0, y, 0.0), degrees)))
    return el


def carousel_horse(sc):
    """A black carousel horse, a jumper, about 1.5 m nose to tail, facing north on its pole (at x 0, z 0), its saddle's
    seat at y 0: its barrel and rump, chest and neck reaching up and forward, a violet mane, its head bowed with a gold
    bridle, glinting orange eyes and pricked ears, its front legs thrown forward and bent at the knee, its hind legs
    reaching back, gold hooves, an orange saddle with a gold pommel and cantle on a violet blanket, and a violet tail
    streaming behind."""
    hide = coat()
    gold = flat(GOLD)
    mane = lengthwise(VIOLET)
    el = [panel(sc, "horse_body", hide, (-2.5, -7.5, -7.0), (2.5, -1.5, 7.0)),
          panel(sc, "horse_belly", hide, (-2.0, -8.5, -5.5), (2.0, -7.5, 5.5)),
          panel(sc, "horse_rump", hide, (-2.25, -1.5, 4.5), (2.25, -0.75, 7.0)),
          panel(sc, "horse_chest", hide, (-2.25, -7.0, -8.5), (2.25, -2.0, -7.0)),
          panel(sc, "saddle", lacquer(ORANGE), (-2.75, -1.5, -1.0), (2.75, 0.0, 4.5)),
          panel(sc, "cantle", gold, (-2.25, 0.0, 3.5), (2.25, 1.25, 4.5)),
          panel(sc, "pommel", gold, (-1.25, 0.0, -1.0), (1.25, 0.75, 0.0))]
    for x0, x1 in ((-3.0, -2.5), (2.5, 3.0)):
        el.append(panel(sc, "blanket", lacquer(VIOLET), (x0, -5.0, -1.5), (x1, -1.5, 5.0)))
    neck = rotation((0.0, -3.0, -8.0), "x", -22.5)
    el += [panel(sc, "horse_neck", hide, (-1.75, -4.0, -10.0), (1.75, 6.0, -6.0), rot=neck),
           panel(sc, "mane", mane, (-0.5, -2.0, -6.0), (0.5, 6.5, -5.0), rot=neck)]
    head = rotation((0.0, 7.0, -11.5), "x", -45)
    el += [panel(sc, "horse_head", hide, (-1.5, 3.5, -17.0), (1.5, 7.0, -11.0), rot=head,
                 east=eyed(hide, "horse", True), west=eyed(hide, "horse", False)),
           panel(sc, "muzzle", coat(STEEL), (-1.1, 3.5, -18.5), (1.1, 5.75, -17.0), rot=head),
           panel(sc, "bridle", gold, (-1.7, 3.3, -15.0), (1.7, 7.2, -14.5), rot=head),
           panel(sc, "forelock", mane, (-0.6, 7.0, -12.5), (0.6, 7.75, -11.0), rot=head)]
    for x0, x1 in ((-1.3, -0.4), (0.4, 1.3)):
        el.append(panel(sc, "ear", hide, (x0, 7.0, -12.25), (x1, 8.75, -11.5), rot=head))
    shoulder = (0.0, -6.5, -6.75)
    upper = rotation(shoulder, "x", 45)
    knee_drop = 5.5
    knee = (0.0, shoulder[1] - knee_drop * math.cos(math.radians(45)), shoulder[2] - knee_drop * math.sin(math.radians(45)))
    lower = rotation(knee, "x", -22.5)
    for x0, x1 in ((-1.75, -0.5), (0.5, 1.75)):
        el.append(panel(sc, "foreleg", hide, (x0, shoulder[1] - knee_drop, shoulder[2] - 0.75), (x1, shoulder[1], shoulder[2] + 0.75), rot=upper))
        el.append(panel(sc, "shin", hide, (x0 + 0.1, knee[1] - 4.5, knee[2] - 0.7), (x1 - 0.1, knee[1], knee[2] + 0.7), rot=lower))
        el.append(panel(sc, "hoof", gold, (x0, knee[1] - 5.5, knee[2] - 0.8), (x1, knee[1] - 4.5, knee[2] + 0.8), rot=lower))
    hip = (0.0, -7.0, 5.5)
    hind = rotation(hip, "x", -22.5)
    for x0, x1 in ((-1.75, -0.5), (0.5, 1.75)):
        el.append(panel(sc, "hindleg", hide, (x0, hip[1] - 7.5, hip[2] - 0.8), (x1, hip[1] + 0.5, hip[2] + 0.8), rot=hind))
        el.append(panel(sc, "hoof", gold, (x0 - 0.1, hip[1] - 8.5, hip[2] - 0.9), (x1 + 0.1, hip[1] - 7.5, hip[2] + 0.9), rot=hind))
    el.append(panel(sc, "tail", mane, (-0.75, -3.0, 7.0), (0.75, -1.25, 12.0), rot=rotation((0.0, -2.0, 7.0), "x", 45)))
    return el


def carousel_bat(sc):
    """A giant black bat to ride, facing north on its pole, its saddle's seat at y 0: a furry body with a rounder belly,
    a violet saddle with a gold rim, its head with a snout, tall ears, orange eyes and two white fangs, and its feet tucked
    under. Its wings are its own models (`carousel_wing`), hinged at its sides 3 pixels under the seat."""
    hair = fur()
    el = [panel(sc, "bat_body", hair, (-3.0, -7.5, -5.0), (3.0, -1.5, 4.0)),
          panel(sc, "bat_chest", hair, (-2.25, -7.0, -6.0), (2.25, -2.0, -5.0)),
          panel(sc, "bat_belly", hair, (-2.25, -8.75, -3.5), (2.25, -7.5, 2.5)),
          panel(sc, "bat_rump", hair, (-2.0, -6.5, 4.0), (2.0, -2.0, 5.5)),
          panel(sc, "bat_saddle", lacquer(VIOLET), (-2.75, -1.5, -2.0), (2.75, 0.0, 4.0)),
          panel(sc, "bat_cantle", flat(GOLD), (-2.25, 0.0, 3.0), (2.25, 1.0, 4.0)),
          panel(sc, "bat_head", hair, (-2.25, -4.5, -9.5), (2.25, 0.5, -6.0), north=eyes_front()),
          panel(sc, "snout", hair, (-1.25, -3.75, -11.0), (1.25, -1.5, -9.5))]
    for x0, x1 in ((-2.25, -0.75), (0.75, 2.25)):
        el.append(panel(sc, "bat_ear", hair, (x0, 0.5, -8.0), (x1, 3.5, -7.0)))
        el.append(panel(sc, "bat_ear_tip", hair, (x0 + 0.4, 3.5, -7.85), (x1 - 0.4, 4.5, -7.15)))
    for x0, x1 in ((-0.9, -0.4), (0.4, 0.9)):
        el.append(panel(sc, "fang", flat(BONE), (x0, -4.75, -10.75), (x1, -3.75, -10.25)))
    for x0, x1 in ((-1.75, -1.0), (1.0, 1.75)):
        el.append(panel(sc, "bat_foot", flat(STEEL), (x0, -10.0, 1.0), (x1, -8.75, 2.5)))
    return el


def carousel_wing(sc, left):
    """A bat's wing, from its hinge (the origin) out `WING` pixels to the right (or the left, `left`) and from 5 in front
    to 5 behind: its skin a cut-out sheet (drawn from both sides) and a bone along its front edge."""
    x0, x1 = (-WING, 0.0) if left else (0.0, WING)
    sheet = panel(sc, "wing", membrane(left), (x0, 0.0, -5.0), (x1, 0.0, 5.0), sides=("up",))
    bone = panel(sc, "wing_bone", flat(BLACK), (x0, -0.4, -5.4), (x1, 0.4, -4.6))
    return sheet, [bone]


def carousel_coach(sc):
    """The Pumpkin Coach, facing north on the deck (its middle at x 0, z 0, the deck's top at y 0): an eight-sided
    pumpkin shell 1.25 m long, ribbed, gold-rimmed and carved with a face at the front, on four orange-spoked wheels; a
    violet bench for two inside with a back, and its curling green stem rising behind with a leaf."""
    shell = pumpkin_side()
    rim = flat(GOLD)
    a, t, y0, y1 = 10.0, 2.0, 3.0, 14.0
    side = 2 * a * math.tan(math.radians(22.5))
    el = [panel(sc, "coach_shell", shell, (-side / 2, y0, -a), (side / 2, y1, -a + t), up=rim),
          panel(sc, "coach_shell", shell, (-side / 2, y0, a - t), (side / 2, y1, a), up=rim),
          panel(sc, "coach_shell", shell, (-a, y0, -side / 2), (-a + t, y1, side / 2), up=rim),
          panel(sc, "coach_shell", shell, (a - t, y0, -side / 2), (a, y1, side / 2), up=rim)]
    for angle in (45, -45):
        for z_lo, z_hi in ((-a, -a + t), (a - t, a)):
            el.append(panel(sc, "coach_shell", shell, (-side / 2, y0, z_lo), (side / 2, y1 - 0.1, z_hi),
                            rot=rotation((0.0, (y0 + y1) / 2, 0.0), "y", angle), up=rim))
    # Its middle bulges, as a pumpkin does: a ring round the shell's middle three-quarters of a pixel proud, ribbed, the
    # carved face on its front.
    bulge, b0, b1 = a + 0.75, 6.0, 11.0
    bside = 2 * bulge * math.tan(math.radians(22.5))
    el += [panel(sc, "coach_bulge", shell, (-bside / 2, b0, -bulge), (bside / 2, b1, -bulge + 1.0), north=pumpkin_side(face=True)),
           panel(sc, "coach_bulge", shell, (-bside / 2, b0, bulge - 1.0), (bside / 2, b1, bulge)),
           panel(sc, "coach_bulge", shell, (-bulge, b0, -bside / 2), (-bulge + 1.0, b1, bside / 2)),
           panel(sc, "coach_bulge", shell, (bulge - 1.0, b0, -bside / 2), (bulge, b1, bside / 2))]
    for angle in (45, -45):
        for z_lo, z_hi in ((-bulge, -bulge + 1.0), (bulge - 1.0, bulge)):
            el.append(panel(sc, "coach_bulge", shell, (-bside / 2, b0 + 0.1, z_lo), (bside / 2, b1 - 0.1, z_hi),
                            rot=rotation((0.0, (b0 + b1) / 2, 0.0), "y", angle)))
    el += solid_octagon(sc, "coach_floor", flat(PUMPKIN), 0.0, 0.0, a - t, y0, y0 + 1.0)
    el += [panel(sc, "coach_bench", lacquer(VIOLET), (-7.5, y0 + 1.0, -1.0), (7.5, 7.25, 5.0)),
           panel(sc, "coach_back", lacquer(VIOLET), (-7.5, 7.25, 5.0), (7.5, 13.0, 7.0)),
           panel(sc, "coach_stem", lengthwise(GREEN), (-1.25, y1, 7.0), (1.25, 20.0, 9.5)),
           panel(sc, "coach_curl", lengthwise(GREEN), (-1.0, 18.0, 9.5), (1.0, 20.0, 12.0)),
           panel(sc, "coach_curl_tip", lengthwise(GREEN), (-1.0, 16.0, 10.5), (1.0, 18.0, 12.0)),
           panel(sc, "coach_leaf", flat(GREEN), (1.25, 15.5, 7.0), (5.0, 16.0, 10.0))]
    for z in (-6.5, 6.5):
        el.append(panel(sc, "coach_axle", flat(STEEL), (-10.0, 3.5, z - 0.5), (10.0, 4.5, z + 0.5)))
        for x0, x1 in ((-11.5, -10.5), (10.5, 11.5)):
            el += wheel(sc, "coach_wheel", 4.0, z, 4.0, x0, x1)
    return el


def solid_octagon(sc, name, painter, cx, cz, a, y0, y1, top=None):
    """A solid eight-sided block about (cx, cz), `a` from its middle to its sides, from y0 to y1: a cross of three boxes
    square to the block that meet without overlapping, and two boxes turned 45 degrees about the middle filling the
    corners, set a tenth and two tenths of a pixel lower and higher so no two of their tops or feet share a plane."""
    s = a * math.tan(math.radians(22.5))
    cap = {"up": top} if top else {}
    out = [panel(sc, name, painter, (cx - a, y0, cz - s), (cx + a, y1, cz + s), **cap),
           panel(sc, name, painter, (cx - s, y0, cz - a), (cx + s, y1, cz - s), **cap),
           panel(sc, name, painter, (cx - s, y0, cz + s), (cx + s, y1, cz + a), **cap)]
    for k, angle in enumerate((45, -45)):
        lift = 0.1 * (k + 1)
        out.append(panel(sc, name, painter, (cx - s, y0 + lift, cz - a), (cx + s, y1 - lift, cz + a),
                         rot=rotation((cx, (y0 + y1) / 2, cz), "y", angle), **cap))
    return out


def organ_pipes():
    """The band organ's front: a row of gold pipes a texel wide, a texel apart, tallest in the middle, each with a dark
    mouth near its foot, on a black panel with a gold foot rail."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                p.put(x, y, shade(BLACK, 1))
        for x in range(p.w):
            p.put(x, p.h - 1, shade(GOLD, 2))
        mid = (p.w - 1) / 2
        for x in range(1, p.w - 1, 2):
            top = 1 + int(round(abs(x - mid) * 0.8))
            for y in range(top, p.h - 1):
                p.put(x, y, shade(GOLD, 4 if y == top else 3))
            if p.h - 4 > top:
                p.put(x, p.h - 4, shade(BLACK, 0))
    paint.tag = repr(("organ_pipes",))
    return paint


def carousel_house(sc):
    """The carousel's middle block, which stays still, facing north: its drive house, an eight-sided violet house 88 cm
    across with a gold top, its band organ's front of gold pipes standing before its north side."""
    el = solid_octagon(sc, "house", lacquer(VIOLET), 8.0, 8.0, 7.0, 0.0, 16.0, top=flat(GOLD))
    el.append(panel(sc, "organ", flat(BLACK), (4.6, 2.0, 0.25), (11.4, 14.0, 1.0), north=organ_pipes()))
    return el


def mini_horse(cx, cz, fx, fz, sc, y):
    """A horse for the item, at (cx, cz) with its saddle at `y`, facing (fx, fz) (a whole quarter turn): its body, its
    neck and head reaching up at the front, and its legs, in black, all square to the block."""
    def box(along0, along1, y0, y1, half):
        if fx:
            xs = sorted((cx + fx * along0, cx + fx * along1))
            return (xs[0], y0, cz - half), (xs[1], y1, cz + half)
        zs = sorted((cz + fz * along0, cz + fz * along1))
        return (cx - half, y0, zs[0]), (cx + half, y1, zs[1])
    hide = coat()
    out = []
    for name, along, ys, half in (("mini_body", (-1.1, 1.0), (y - 0.9, y - 0.2), 0.35), ("mini_neck", (0.7, 1.3), (y - 0.2, y + 1.0), 0.25),
                                  ("mini_legs", (-0.9, 0.8), (y - 1.8, y - 0.9), 0.25), ("mini_saddle", (-0.3, 0.4), (y - 0.2, y), 0.4)):
        lo, hi = box(along[0], along[1], ys[0], ys[1], half)
        out.append(panel(sc, name, lacquer(ORANGE) if name == "mini_saddle" else hide, lo, hi))
    return out


def carousel_item(sc):
    """The carousel in miniature for its item, a seventh of its size in the block's middle: its deck, column and drive
    house, its canopy with its rounding boards, stepped roof and pumpkin, its poles and four horses on the outer ring."""
    k = 1.0 / 7.0
    c = 8.0
    el = solid_octagon(sc, "mini_deck", flat(BOARD_WOOD), c, c, DECK_OUT * k, 0.0, DECK_TOP * k)
    el += solid_octagon(sc, "mini_column", lacquer(VIOLET), c, c, COLUMN * k, DECK_TOP * k, CEILING * k)
    el += solid_octagon(sc, "mini_boards", flat(BLACK), c, c, DECK_OUT * k, CEILING * k, BOARD_TOP * k)
    tiers = (46.0, 34.0, 22.0, 10.0)
    step = (ROOF_TOP - BOARD_TOP) * k / len(tiers)
    for n, apothem in enumerate(tiers):
        el += solid_octagon(sc, "mini_roof", flat(ORANGE), c, c, apothem * k, BOARD_TOP * k + n * step,
                            BOARD_TOP * k + (n + 1) * step)
    el.append(panel(sc, "mini_pumpkin", pumpkin_side(), (c - 0.6, ROOF_TOP * k, c - 0.6), (c + 0.6, ROOF_TOP * k + 1.0, c + 0.6)))
    r = hm.CAROUSEL["outer"] * 16 * k
    saddle = (DECK_TOP + hm.CAROUSEL["seats"]["horse"] * 16) * k
    for q in range(4):
        angle = math.radians(90 * q)
        px, pz = c + r * math.cos(angle), c - r * math.sin(angle)
        el.append(panel(sc, "mini_pole", flat(GOLD), (px - 0.1, DECK_TOP * k, pz - 0.1), (px + 0.1, SWEEP[0] * k, pz + 0.1)))
        el += mini_horse(px, pz, round(-math.sin(angle)), round(-math.cos(angle)), sc, saddle)
    return el


def carousel():
    """The Haunted Carousel's block models, on its 128 x 128 block texture: `whole` (its middle block, the drive house)
    and `item` (the carousel in miniature)."""
    sc = Sculpt(hm.CAROUSEL["block"], 2607, 128)
    sc.models["whole"] = carousel_house(sc)
    sc.models["item"] = carousel_item(sc)
    return sc


def carousel_ride():
    """The renderer's parts, on the carousel's own 256 x 256 texture (`RIDE`): `spin_boxes` with the turned quads in
    `sc.spin`, `bulbs`, `horse`, `bat`, `wing_left` and `wing_right` (and their bones), and `coach`."""
    sc = Sculpt(RIDE, 2608, 256)
    sc.spin = carousel_spin(sc)
    sc.models["bulbs"] = carousel_bulbs(sc)
    sc.models["horse"] = carousel_horse(sc)
    sc.models["bat"] = carousel_bat(sc)
    for name, left in (("wing_left", True), ("wing_right", False)):
        sheet, bones = carousel_wing(sc, left)
        sc.models[name] = [sheet]
        sc.models[name + "_bones"] = bones
    sc.models["coach"] = carousel_coach(sc)
    return sc


_ride = []


def built_ride():
    if not _ride:
        _ride.append(ppd.planned(carousel_ride, 256))
    return _ride[0]


def carousel_quads():
    """The renderer's models (assets/jugcraft/haunted_midway_quads.json): the turning deck, column and canopy (with the
    sweeps, poles and finial), its bulbs, the horse, the bat and its two wings (cut out round their scallops), and the
    coach."""
    sc = built_ride()
    tex = {"p": RIDE}
    out = {"haunted_carousel_spin": sc.spin + quads(sc.models["spin_boxes"], tex),
           "haunted_carousel_bulbs": quads(sc.models["bulbs"], tex)}
    for name in ("horse", "bat", "coach"):
        out[f"haunted_carousel_{name}"] = quads(sc.models[name], tex)
    for name in ("wing_left", "wing_right"):
        out[f"haunted_carousel_bat_{name}"] = [dict(q, cutout=True) for q in quads(d17d.single_sheets(sc.models[name]), tex)] \
            + quads(sc.models[name + "_bones"], tex)
    return out



# ---------------------------------------------------------------- building and files

_built = {}
BUILDERS = {hm.COTTON_CANDY["block"]: cotton_candy_cart, hm.POPCORN_WAGON["block"]: popcorn_wagon,
            hm.TICKET_BOOTH["block"]: ticket_booth, hm.WHACK["block"]: whack_a_zombie, hm.GALLERY["block"]: shooting_gallery,
            hm.GHOST_TRAIN["block"]: ghost_train, hm.CAROUSEL["block"]: carousel}


def build(block):
    if block not in _built:
        _built[block] = ppd.planned(BUILDERS[block], 128)
    return _built[block]


def _image_of(name):
    return build(name).atlas.img if name in BUILDERS else None


def cut_parts(block, elements, size):
    """A model's pieces for each of the block's parts: the Ghost Train's by its own cells (it has gaps for its doorways),
    the rest's as a full box of blocks."""
    if block == hm.GHOST_TRAIN["block"]:
        return cut_cells(elements, size, train_cells())
    return ppd.cut(elements, size)


def write_cut(write, models, block, name, elements, size):
    """A model cut into its blocks' parts (`<block>_<part>` plus `_<name>`), returning which parts have any."""
    suffix = "" if name == "whole" else f"_{name}"
    have = []
    for part, pieces in enumerate(cut_parts(block, elements, size)):
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
    half = hm.TICKET_BOOTH["roll"] // 2
    lamps = hm.WHACK["lamps"]
    for spec, runner, overlays in ((hm.COTTON_CANDY, "head", [("heater", {"lit": "true"})]
                                    + [(f"floss{n}", {"floss": str(n)}) for n in range(1, hm.COTTON_CANDY["floss"] + 1)]),
                                   (hm.POPCORN_WAGON, "kettle", [("lit", {"lit": "true"})]
                                    + [(f"heap{n}", {"popcorn": str(n)}) for n in range(1, hm.POPCORN_WAGON["capacity"] + 1)]),
                                   # Part 2: the booth's roll half full and full; each score lamp lit from its score up.
                                   (hm.TICKET_BOOTH, "crank", [("roll1", {"roll": "|".join(str(n) for n in range(1, half + 1))}),
                                                              ("roll2", {"roll": "|".join(str(n) for n in range(half + 1, 2 * half + 1))})]),
                                   (hm.WHACK, "head", [(f"lamp{k}", {"score": "|".join(str(n) for n in range(k, lamps + 1))})
                                                       for k in range(1, lamps + 1)]),
                                   # Part 3: the gallery's two targets are its runners 1 and 2; its bulbs glow while a round runs.
                                   (hm.GALLERY, {"1": "bat", "2": "ghost"}, [("bulbs_lit", {"lit": "true"})]),
                                   # Part 4: the ghost train's door leaves and its ghost are its runners 1 to 3; its windows glow when lit.
                                   (hm.GHOST_TRAIN, {"1": "leaf_left", "2": "leaf_right", "3": "spook"}, [("windows_lit", {"lit": "true"})])):
        block = spec["block"]
        lang[f"block.{MOD}.{block}"] = spec["display"]
        sc = build(block)
        size = spec["size"]
        n = len(train_cells()) if block == hm.GHOST_TRAIN["block"] else len(d17d.cells(size))
        write_cut(write, models, block, "whole", sc.models["whole"], size)
        have = {name: write_cut(write, models, block, name, sc.models[name], size) for name, _ in overlays}
        # A runner is the model its `runner` value shows alone: "true" for most, "1" and "2" for the gallery's two targets.
        runners = {"true": runner} if isinstance(runner, str) else runner
        off = "false" if isinstance(runner, str) else "0"
        for name in runners.values():
            write(models / f"{block}_{name}.json", fa.model(block, d20d.separated(sc.models[name])))
        multipart = []
        for f, y in FACINGS.items():
            turn = {"y": y} if y else {}
            for p in range(n):
                plain = {"facing": f, "part": str(p), "runner": off}
                multipart.append({"when": plain, "apply": {"model": rid(f"block/{block}_{p}"), **turn}})
                for name, when in overlays:
                    if have[name][p]:
                        multipart.append({"when": dict(plain, **when), "apply": {"model": rid(f"block/{block}_{p}_{name}"), **turn}})
            for value, name in runners.items():
                multipart.append({"when": {"facing": f, "runner": value}, "apply": {"model": rid(f"block/{block}_{name}"), **turn}})
        write(states / f"{block}.json", {"multipart": multipart})
        if size[0] == 1:
            whole = sc.models["whole"] + [e for name in runners.values() for e in sc.models[name]]
            item = fa.transformed(d17d.pinned(d20d.separated(whole)), 0.5, (4.0, 0.0, 4.0), (0, 0, 0))
        elif block == hm.GHOST_TRAIN["block"]:
            # The ghost train's front at a fifth of its size, its doors shut.
            doors = [e for at in (64.0, 32.0) for e in d20d.shifted(sc.models["leaf_left"] + sc.models["leaf_right"], at, 0, 0)]
            item = fa.transformed(d17d.pinned(d20d.separated(sc.models["whole"] + doors)), 0.2, (0.0, 0.0, 8.0 - 1.6), (0, 0, 0))
        else:
            # The gallery whole at a third of its size, two targets on each rail.
            targets = [e for (_, _, _, kind) in spec["rails"] for at in (14.0, 34.0)
                       for e in d20d.shifted(sc.models[kind], at, 0, 0)]
            whole = sc.models["whole"] + targets
            item = fa.transformed(d17d.pinned(d20d.separated(whole)), 1.0 / 3.0, (0.0, 0.0, 8.0 - 8.0 / 3.0), (0, 0, 0))
        write(root / "models" / "item" / f"{block}.json", fa.model(block, item))
        write(root / "items" / f"{block}.json", {"model": {"type": "minecraft:model", "model": rid(f"item/{block}")}})
    for key, words in (("cotton_candy_cart.off", "Switch it on first"), ("cotton_candy_cart.full", "The bowl is full of floss"),
                       ("cotton_candy_cart.empty", "Spin some sugar first"),
                       ("popcorn_wagon.light", "Light it with flint and steel or a fire charge"),
                       ("popcorn_wagon.popping", "The kettle is popping"), ("popcorn_wagon.full", "The case is full"),
                       ("popcorn_wagon.two", "Caramel coats two popcorn at a time"),
                       ("ticket_booth.full", "The roll is full"), ("ticket_booth.empty", "Load the roll with paper"),
                       ("whack_a_zombie.ticket", "A Midway Ticket starts a round"), ("whack_a_zombie.playing", "A round is on"),
                       ("whack_a_zombie.not_yours", "It isn't your round"), ("whack_a_zombie.won", "All the lamps! Step right up"),
                       ("whack_a_zombie.lost", "Time's up"),
                       ("shooting_gallery.ticket", "A Midway Ticket starts a round"), ("shooting_gallery.playing", "A round is on"),
                       ("shooting_gallery.won", "All down! Step right up"), ("shooting_gallery.lost", "Time's up"),
                       ("shooting_gallery.too_close", "Stand further back to shoot")):
        lang[f"message.{MOD}.{key}"] = words
    item = hm.TICKET["item"]
    lang[f"item.{MOD}.{item}"] = hm.TICKET["display"]
    write(root / "models" / "item" / f"{item}.json", {"parent": "minecraft:item/generated", "textures": {"layer0": rid(f"item/{item}")}})
    write(root / "items" / f"{item}.json", {"model": {"type": "minecraft:model", "model": rid(f"item/{item}")}})
    carousel_assets(root, write, lang)


def carousel_assets(root, write, lang):
    """The Haunted Carousel's middle block's model and blockstate (turned to its facing, its lights and organ drawn
    alike), its item (the carousel in miniature), its names and messages, and the renderer's quads."""
    spec = hm.CAROUSEL
    block = spec["block"]
    sc = build(block)
    write(root / "models" / "block" / f"{block}.json", fa.model(block, d20d.separated(sc.models["whole"])))
    write(root / "blockstates" / f"{block}.json",
          {"variants": {f"facing={f}": {"model": rid(f"block/{block}"), **({"y": y} if y else {})} for f, y in FACINGS.items()}})
    write(root / "models" / "item" / f"{block}.json", fa.model(block, d20d.separated(sc.models["item"])))
    write(root / "items" / f"{block}.json", {"model": {"type": "minecraft:model", "model": rid(f"item/{block}")}})
    lang[f"block.{MOD}.{block}"] = spec["display"]
    lang[f"entity.{MOD}.{spec['entity']}"] = spec["entity_display"]
    for key, words in (("no_room", "The Haunted Carousel needs a clear space %s blocks across, %s through and %s high"),
                       ("full", "Every mount is taken: wait for the next ride"),
                       ("jammed", "The Haunted Carousel is jammed: something is in a mount's way"),
                       ("organ_on", "The band organ plays"), ("organ_off", "The band organ is quiet")):
        lang[f"message.{MOD}.{block}.{key}"] = words
    write(root / "haunted_midway_quads.json", carousel_quads())


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
    bowl; the popcorn in the wagon's case, and the kernel in its kettle if it was popping; a sheet of paper for each
    two tickets left on the booth's roll."""
    for spec, extra in ((hm.COTTON_CANDY, [counted(hm.COTTON_CANDY["block"], "floss", hm.COTTON_CANDY["floss"], "minecraft:sugar")]),
                        (hm.POPCORN_WAGON, [counted(hm.POPCORN_WAGON["block"], "popcorn", hm.POPCORN_WAGON["capacity"], rid("popcorn")),
                                            {"condition": {"type": "minecraft:match_block", "blocks": rid(hm.POPCORN_WAGON["block"]),
                                                           "state": {"part": "0", "popping": "true"}},
                                             "entries": [{"type": "minecraft:item", "name": rid("corn_kernels")}], "rolls": 1}])):
        drop_once(out, write, spec["block"], extra)
    # The booth gives back a sheet for each two tickets left on its roll; Whack-a-Zombie drops only itself.
    tb = hm.TICKET_BOOTH
    block = tb["block"]
    sheets = [{"type": "minecraft:set_count", "count": n // tb["per_sheet"], "add": False,
               "condition": {"type": "minecraft:match_block", "blocks": rid(block), "state": {"roll": str(n)}}}
              for n in range(2 * tb["per_sheet"], tb["roll"] + 1)]
    short = [{"type": "minecraft:inverted", "term": {"type": "minecraft:match_block", "blocks": rid(block), "state": {"roll": str(n)}}}
             for n in range(tb["per_sheet"])]
    paper = {"condition": {"type": "minecraft:all_of", "terms": [
                 {"type": "minecraft:match_block", "blocks": rid(block), "state": {"part": "0"}}] + short},
             "entries": [{"type": "minecraft:item", "name": "minecraft:paper", "modifier": sheets}], "rolls": 1}
    drop_once(out, write, block, [paper])
    drop_once(out, write, hm.WHACK["block"], [])
    drop_once(out, write, hm.GALLERY["block"], [])
    drop_once(out, write, hm.GHOST_TRAIN["block"], [])
    # The carousel is one block: its middle drops itself.
    block = hm.CAROUSEL["block"]
    write(out / f"{block}.json", {"type": "minecraft:block", "pools": [{
        "condition": {"type": "minecraft:survives_explosion"}, "entries": [{"type": "minecraft:item", "name": rid(block)}], "rolls": 1}],
        "random_sequence": rid(f"blocks/{block}")})


def drop_once(out, write, block, extra):
    """A two-block stall's loot: itself once, from its first block, and the `extra` pools."""
    write(out / f"{block}.json", {"type": "minecraft:block", "pools": [{
        "condition": {"type": "minecraft:all_of", "terms": [{"type": "minecraft:survives_explosion"},
                                                            {"type": "minecraft:match_block", "blocks": rid(block), "state": {"part": "0"}}]},
        "entries": [{"type": "minecraft:item", "name": rid(block)}], "rolls": 1}] + extra, "random_sequence": rid(f"blocks/{block}")})


def tags(tags):
    """The stalls are wooden: they come off with an axe."""
    for block in hm.blocks():
        tags.add("block", "minecraft:mineable/axe", rid(block))


def textures():
    import item_icons
    out = {("block", block): build(block).atlas.img for block in hm.blocks()}
    out[("item", hm.CANDY["item"])] = item_icons.draw(hm.CANDY["item"])
    out[("item", hm.TICKET["item"])] = item_icons.draw(hm.TICKET["item"])
    out[("entity", hm.CAROUSEL["entity"])] = built_ride().atlas.img
    return out

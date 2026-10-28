"""Generated assets for Cozy Fall Hearth, decor set 5 (tools/hearth.py; docs/features/cozy-fall-hearth.md).

Built from boxes at real size, one texel to a model pixel, in the market displays' manner (tools/pumpkin_patch_data.py):
each material one colour stepped into tones, its marks placed the way it is made, never sprinkled.
- Fieldstone: rounded stones in staggered courses, each stone one tone of the grey-brown (its own, by its place in the
  course), lit along its top row, set in a pale mortar joint.
- Firebrick at the back of the firebox: small bricks in a running bond, sooted darker towards the top.
- The hearth's flagstones: big slabs with a joint between them.
- The mantel: an oak beam, wood along its length. The logs: bark round them, end grain at their ends.
- The fire: its own model in an animated texture (`stone_fireplace_fire`), full-bright and unshaded, drawn only while
  the fireplace is lit: tongues of flame in smooth bands (a pale root, yellow, orange, a deep orange tip), their heights
  rising and falling by a slow wave from frame to frame, the frames blended, so it never flashes.
Part 2:
- Cast iron: a flat dark field inside a raised bead (lit top and left, shaded bottom and right), as a stove's panels
  are cast; its top plate with two round lids. Nickel trim in flat bands, lit along one edge. Mica: smoky amber with a
  lighter diagonal sheen every fourth texel. The woodstove's fire is the fireplace's, seen through its mica.
- Copper and brass: flat bands, each tier of the kettle lit along its top, so its body reads as a turned dome.
- Enamel: cream, with a cobalt rim and handle. The simmer pot's water: its colour in rings, still, or (simmering) an
  animated strip of rings spreading out from the middle, blended.
"""
import math

import decor17_data as d17d
import decor20_data as d20d
import flora_art as fa
import hearth as hh
import pumpkin_patch_data as ppd
from flora_art import Sculpt, pal, rotation, shade
from pumpkin_patch_data import IRON, WOOD, flat, lengthwise, panel, walls

MOD = "jugcraft"
FACINGS = ppd.FACINGS

STONE = pal("4a443c", "5e574d", "726a5e", "877e70", "9c9282")
MORTAR = pal("8a8478", "a09a8c", "b4ae9e")
FIREBRICK = pal("3a1e16", "52281c", "6a3424", "82422e")
FLAG = pal("5a5852", "6e6c64", "838078", "98958c")
BEAM = pal("2e2216", "44321e", "5a4228", "6e5232")
BARK = pal("2a2018", "3c2e22", "4e3c2c")
ENDGRAIN = pal("6a5638", "8a7048", "a88a5a")
FIRE = pal("b8401a", "dc6a22", "f0962c", "f8c048", "fce690")
LEAF_COLOURS = (pal("6a1a10", "8e2a16", "b0401e"), pal("7a3a0e", "a85414", "cc7420"), pal("7a5a10", "a07a16", "c49a24"))
PUMPKIN = pal("8a3c0a", "b25412", "d4701c", "e88a2c")
CONE = pal("3a2614", "52361c", "6a4626")


def rid(path):
    return path if ":" in path else f"{MOD}:{path}"


# Each course's stones' lengths, in turn (a course starts a step further along the run than the last).
STONE_LENGTHS = (5, 7, 4, 6, 8, 5, 6)


def fieldstone():
    """Fieldstone laid in courses four texels high, its stones of several lengths in turn (so no two courses line up),
    their corners rounded off into the mortar; each stone takes one of three tones by its place, so neighbours differ,
    and is lit along its top row. The joints between are a pale mortar."""
    def paint(p):
        for y in range(p.h):
            course, row = y // 4, y % 4
            # Where each stone of this course starts and ends.
            edges, at, k = [], -(course * 3) % 7, course
            while at < p.w + 8:
                length = STONE_LENGTHS[k % len(STONE_LENGTHS)]
                edges.append((at, at + length, k))
                at += length
                k += 1
            for x in range(p.w):
                for start, end, index in edges:
                    if start <= x < end:
                        break
                inner = x - start
                last = end - start - 1
                corner = row in (0, 2) and inner in (0, last) and row == 0
                if row == 3 or inner == last or corner:
                    p.put(x, y, shade(MORTAR, 1))
                    continue
                tone = 1 + (index * 2 + course) % 3
                p.put(x, y, shade(STONE, tone + (1 if row == 0 else 0)))
    paint.tag = "fieldstone"
    return paint


def firebrick():
    """Firebrick in a running bond, bricks four texels long and two high with a dark joint, sooted a tone darker in the
    upper half."""
    def paint(p):
        for y in range(p.h):
            row = y // 2
            for x in range(p.w):
                joint = y % 2 == 1 or (x + (row % 2) * 2) % 4 == 3
                k = 0 if joint else 2
                if y < p.h // 2 and not joint:
                    k = 1
                p.put(x, y, shade(FIREBRICK, k))
    paint.tag = "firebrick"
    return paint


def flagstone():
    """Flagstones eight texels square with a joint a tone darker between them, each slab lit along its top row."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                joint = x % 8 == 7 or y % 8 == 7
                k = 0 if joint else 3 if y % 8 == 0 else 2 if ((x // 8) + (y // 8)) % 2 == 0 else 1
                p.put(x, y, shade(FLAG, k))
    paint.tag = "flagstone"
    return paint


def end_grain():
    """A log's end: a ring of bark round rings of wood, light and mid by turns, about a dark pith, square as a log's end
    is drawn in pixels."""
    def paint(p):
        cx, cy = (p.w - 1) / 2, (p.h - 1) / 2
        outer = (min(p.w, p.h) - 1) / 2
        for y in range(p.h):
            for x in range(p.w):
                r = max(abs(x - cx), abs(y - cy))
                p.put(x, y, shade(BARK, 1) if r >= outer else shade(ENDGRAIN, 0 if r < 0.6 else 2 if round(r) % 2 else 1))
    paint.tag = "end_grain"
    return paint


def fire_strip():
    """The fire, a frame a 16 x 16 square, `frames` of them down the strip: five tongues of flame side by side, each a
    smooth rise from its root to its tip in the fire's bands (pale root, yellow, orange, deep orange), its height rising
    and falling on a slow wave (each tongue a step behind the last), clear above it. The frames are blended in game."""
    from PIL import Image
    n = hh.FIREPLACE["frames"]
    strip = Image.new("RGBA", (16, 16 * n), (0, 0, 0, 0))
    for f in range(n):
        for x in range(16):
            tongue = x // 3
            centre = tongue * 3 + 1
            phase = (f / n + tongue * 0.27) * math.tau
            height = 11.0 + 3.0 * math.sin(phase) - 1.6 * abs(x - centre)
            for y in range(16):
                up = 15 - y
                if up >= height:
                    continue
                band = up / max(1.0, height)
                k = 4 if band < 0.2 else 3 if band < 0.45 else 2 if band < 0.75 else 1
                strip.putpixel((x, f * 16 + y), tuple(shade(FIRE, k)) + (255,))
    return strip


def fireplace():
    """The Stone Fireplace, facing north (its wall behind, to the south), three blocks across and three up: a flagstone
    hearth running out before it, the breast's jambs and lintel round the firebox opening (1.25 m across, 78 cm high),
    firebrick at its back, an oak mantel shelf on two corbels at 1.4 m, and the stack stepping in above it to the
    ceiling; in the firebox two iron andirons with three logs across them. Its models: `whole`, `garland` (the mantel's
    dressing) and `fire` (drawn on its own texture)."""
    sc = Sculpt(hh.FIREPLACE["block"], 2501, 128)
    stone = fieldstone()
    x0, x1, y0, y1 = hh.FIREPLACE["opening"]
    mantel = hh.FIREPLACE["mantel"]
    el = [panel(sc, "hearth", flagstone(), (4.0, 0.0, 0.0), (44.0, 1.5, 6.0)),
          panel(sc, "plinth", flagstone(), (0.0, 0.0, 6.0), (48.0, y0, 16.0)),
          panel(sc, "jamb", stone, (0.0, y0, 6.0), (x0, mantel, 16.0)),
          panel(sc, "jamb", stone, (x1, y0, 6.0), (48.0, mantel, 16.0)),
          panel(sc, "lintel", stone, (x0, y1, 6.0), (x1, mantel, 16.0)),
          panel(sc, "back", firebrick(), (x0, y0, 14.0), (x1, y1, 16.0)),
          panel(sc, "mantel", lengthwise(BEAM), (2.0, mantel, 3.0), (46.0, mantel + 2.5, 16.0)),
          panel(sc, "stack", stone, (4.0, mantel + 2.5, 8.0), (44.0, 48.0, 16.0))]
    for cx in (4.0, 44.0):
        el.append(panel(sc, "corbel", lengthwise(BEAM), (cx - 1.0, mantel - 3.0, 4.0), (cx + 1.0, mantel, 6.0)))
    for ax in (17.0, 30.0):
        el.append(panel(sc, "andiron", flat(IRON), (ax, y0, 7.5), (ax + 1.0, y0 + 2.5, 12.5)))
        el.append(panel(sc, "finial", flat(IRON), (ax - 0.25, y0 + 2.5, 7.25), (ax + 1.25, y0 + 4.0, 8.25)))
    for ly, lz in ((y0 + 2.5, 9.0), (y0 + 2.5, 11.5), (y0 + 4.5, 10.25)):
        el.append(panel(sc, "log", ppd.lengthwise(BARK), (15.5, ly, lz - 1.0), (32.5, ly + 2.0, lz + 1.0),
                        east=end_grain(), west=end_grain()))
    sc.models["whole"] = el
    # The garland: leaves in three autumn colours along the mantel's top and front, little pumpkins and pine cones.
    gar = []
    for i, x in enumerate(range(3, 45, 3)):
        leaves = LEAF_COLOURS[i % 3]
        gar.append(panel(sc, f"leaf{i % 3}", flat(leaves), (x, mantel + 2.5, 3.25), (x + 2.5, mantel + 3.25, 5.75)))
    # Four swags hung along the mantel's front, each a run of leaves sagging between two points.
    for a in (3.0, 13.5, 24.0, 34.5):
        for k in range(7):
            t = k / 6.0
            x = a + t * 10.5
            y = mantel + 1.0 - 2.5 * math.sin(math.pi * t)
            leaves = LEAF_COLOURS[k % 3]
            gar.append(panel(sc, f"swag{k % 3}", flat(leaves), (x - 0.9, y - 0.6, 2.5), (x + 0.9, y + 0.6, 3.0)))
    for x in (8.0, 23.0, 38.0):
        gar.append(panel(sc, "pumpkin", flat(PUMPKIN), (x, mantel + 2.5, 3.75), (x + 2.5, mantel + 4.25, 6.25)))
        gar.append(panel(sc, "stem", flat(BEAM), (x + 1.0, mantel + 4.25, 4.75), (x + 1.5, mantel + 5.0, 5.25)))
    for x in (15.0, 31.0):
        gar.append(panel(sc, "cone", flat(CONE), (x, mantel + 2.5, 4.0), (x + 1.25, mantel + 4.0, 5.25)))
    sc.models["garland"] = gar
    # The fire: three sheets of flame across the firebox, one behind another, over the logs.
    fire = []
    for z in (10.0, 11.75, 13.0):
        fire.append({"from": [16.0, y0 + 2.5, z], "to": [32.0, y1 - 1.0, z], "shade": False, "light_emission": 15,
                     "faces": {"north": {"uv": [0, 0, 16, 16], "texture": "#f"}, "south": {"uv": [16, 0, 0, 16], "texture": "#f"}}})
    sc.models["fire"] = fire
    return sc


# ---------------------------------------------------------------- part 2: the woodstove, the kettle, the simmer pot

CAST = pal("1c1c20", "26262b", "303036", "3c3c44", "4a4a54")
NICKEL = pal("6a6e74", "8a9098", "aab0b8", "c8ced4", "e4e8ec")
MICA = pal("2a1a0c", "3a2612", "4e341a", "664626")
COPPER = pal("6a2c14", "8c3c1a", "aa5224", "c46a32", "dc8a4a")
BRASS = pal("6a5218", "8a6c22", "aa8a30", "c8a844", "e0c660")
ENAMEL = pal("b4ab96", "c8c0aa", "dcd4c0", "ece6d4")
COBALT = pal("1a2650", "24346a", "304684")
BREW = pal("5a2210", "74301a", "8e4024", "a85430")
APPLE = pal("e8dcb4", "f4ecc8")
PEEL = pal("8a1a14", "b0281c")


def cast_panel():
    """A cast-iron panel: its flat field in one tone, inside a raised bead one texel in from its edge (lit along its top
    and left, shaded along its bottom and right), as a stove's side is cast."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                bead = (x == 1 or x == p.w - 2) and 1 <= y <= p.h - 2 or (y == 1 or y == p.h - 2) and 1 <= x <= p.w - 2
                if p.w < 5 or p.h < 5:
                    bead = False
                if bead:
                    k = 4 if (y == 1 or x == 1) else 1
                elif x == 0 or y == 0:
                    k = 3
                elif x == p.w - 1 or y == p.h - 1:
                    k = 0
                else:
                    k = 2
                p.put(x, y, shade(CAST, k))
    paint.tag = "cast_panel"
    return paint


def cooktop():
    """The stove's top plate: cast iron with two round lids let into it side by side, each a ring a tone darker round a
    lit centre boss with its lifting notch, square as a circle is drawn in pixels."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                p.put(x, y, shade(CAST, 2 if 0 < x < p.w - 1 and 0 < y < p.h - 1 else 3))
        r = min(p.w / 4.0, p.h / 2.0) - 1.0
        for cx in (p.w / 4.0 - 0.5, 3 * p.w / 4.0 - 0.5):
            cy = p.h / 2.0 - 0.5
            for y in range(p.h):
                for x in range(p.w):
                    d = math.hypot(x - cx, y - cy)
                    if abs(d - r) < 0.5:
                        p.put(x, y, shade(CAST, 0))
                    elif d < 1.0:
                        p.put(x, y, shade(CAST, 4 if x <= cx else 3))
    paint.tag = "cooktop"
    return paint


def mica():
    """A mica window: smoky amber, its sheen a lighter diagonal every fourth texel (pressed sheets of mica, not noise)."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                p.put(x, y, shade(MICA, 3 if (x + y) % 4 == 0 else 1 if (x + y) % 4 == 2 else 2))
    paint.tag = "mica"
    return paint


def brew_strip():
    """The simmering surface, eight 16 x 16 frames: rings of the spiced water's tones spreading out from the middle, a
    ring a step further out each frame, so the frames blended in game ripple slowly (smooth bands, never flashing)."""
    from PIL import Image
    n = hh.SIMMER_POT["frames"]
    strip = Image.new("RGBA", (16, 16 * n), (0, 0, 0, 255))
    for f in range(n):
        for y in range(16):
            for x in range(16):
                r = math.hypot(x - 7.5, y - 7.5)
                k = (0, 1, 2, 3, 2, 1)[int(r - f * 0.75) % 6]
                strip.putpixel((x, f * 16 + y), tuple(shade(BREW, k)) + (255,))
    return strip


def brew_still():
    """The simmer pot's water at rest: its colour in one tone, a lighter ring of it round the middle where the light
    catches it."""
    from PIL import Image
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 255))
    for y in range(16):
        for x in range(16):
            ring = abs(math.hypot(x - 7.5, y - 7.5) - 1.5) < 0.6
            img.putpixel((x, y), tuple(shade(BREW, 2 if ring else 1)) + (255,))
    return img


def woodstove():
    """The Cast-Iron Woodstove, facing north (its wall behind, to the south): a parlour box stove 78 cm wide, 62 cm deep
    and 1 m high, on four cast legs, an ash-pan section with its door, the firebox with its door glazed with two mica
    windows (a nickel latch, iron hinges), cast side panels, a nickel towel rail across its front and its top plate with
    two lids; its stovepipe leaves the back into the wall through a collar and a thimble, with a damper handle. Models:
    `whole` and `fire` (the flames behind the mica, on the fireplace's fire texture)."""
    sc = Sculpt(hh.WOODSTOVE["block"], 2502, 128)
    iron = flat(CAST)
    el = []
    for x0 in (2.5, 12.0):
        for z0 in (3.5, 11.0):
            el.append(panel(sc, "leg", iron, (x0, 0.5, z0), (x0 + 1.5, 3.5, z0 + 1.5)))
            el.append(panel(sc, "foot", iron, (x0 - 0.25, 0.0, z0 - 0.25), (x0 + 1.75, 0.5, z0 + 1.75)))
    el.append(panel(sc, "ash", cast_panel(), (2.0, 3.5, 3.0), (14.0, 5.5, 13.0)))
    el.append(panel(sc, "ash_door", flat(CAST), (5.0, 3.85, 2.6), (11.0, 5.15, 3.0)))
    el.append(panel(sc, "draft", flat(NICKEL), (7.5, 4.15, 2.3), (8.5, 4.85, 2.6)))
    el.append(panel(sc, "body", cast_panel(), (2.5, 5.5, 3.5), (13.5, 14.5, 12.5)))
    el.append(panel(sc, "door", cast_panel(), (4.5, 7.0, 3.0), (11.5, 13.0, 3.5)))
    for x0 in (5.25, 8.25):
        el.append(panel(sc, "mica", mica(), (x0, 8.5, 2.95), (x0 + 2.5, 12.0, 3.0), sides=("north",)))
    el.append(panel(sc, "latch", flat(NICKEL), (10.75, 9.5, 2.5), (11.75, 10.5, 3.0)))
    for y0 in (7.75, 11.25):
        el.append(panel(sc, "hinge", flat(CAST), (4.0, y0, 2.75), (4.75, y0 + 1.0, 3.5)))
    el.append(panel(sc, "top", flat(CAST), (1.75, 14.5, 2.75), (14.25, 16.0, 13.25), up=cooktop()))
    el.append(panel(sc, "rail", flat(NICKEL), (2.5, 14.75, 2.0), (13.5, 15.25, 2.5)))
    for x0 in (2.75, 12.75):
        el.append(panel(sc, "bracket", flat(NICKEL), (x0, 14.75, 2.5), (x0 + 0.5, 15.25, 2.75)))
    el.append(panel(sc, "collar", flat(CAST), (6.0, 8.75, 12.5), (10.0, 12.75, 13.5)))
    el += [panel(sc, "pipe", lengthwise(CAST), (6.5, 9.25, 13.5), (9.5, 12.25, 15.75)),
           panel(sc, "pipe", lengthwise(CAST), (7.0, 8.75, 13.5), (9.0, 12.75, 15.75))]
    el.append(panel(sc, "damper", flat(NICKEL), (7.75, 12.75, 14.25), (8.25, 13.75, 14.75)))
    el.append(panel(sc, "thimble", flat(CAST), (5.5, 8.25, 15.5), (10.5, 13.25, 15.9), sides=("north", "east", "west", "up", "down")))
    sc.models["whole"] = el
    tex = f"{MOD}:block/{hh.FIREPLACE['block']}_fire"
    fire = []
    for x0, u in ((5.25, 4.0), (8.25, 9.0)):
        fire.append({"from": [x0, 8.5, 2.9], "to": [x0 + 2.5, 12.0, 2.9], "shade": False, "light_emission": 15,
                     "faces": {"north": {"uv": [u, 11.5, u + 2.5, 15.0], "texture": "#f"}}})
    sc.models["fire"] = fire
    return sc


def kettle():
    """The Tea Kettle, facing north, its spout to the west (the right of whoever set it down): a copper kettle 28 cm
    across and 44 cm to the top of its handle, on a foot ring, its body stepping in tier by tier to the lid like a turned
    dome, a spout rising from its side at 45 degrees, and a brass handle over it in line with the spout, on two posts,
    with a turned wooden grip. Models: `whole` (without its lid) and `lid` (the lid and its knob, drawn by
    client/TeaKettleRenderer)."""
    sc = Sculpt(hh.KETTLE["block"], 2503, 64)
    cu = flat(COPPER)
    el = [panel(sc, "foot", flat(COPPER), (6.0, 0.0, 6.0), (10.0, 0.5, 10.0))]
    for half, y0, y1 in ((2.25, 0.5, 2.5), (2.0, 2.5, 3.5), (1.5, 3.5, 4.25)):
        el.append(panel(sc, "body", cu, (8.0 - half, y0, 8.0 - half), (8.0 + half, y1, 8.0 + half)))
    el.append(panel(sc, "spout", cu, (5.4, 1.25, 7.6), (6.2, 4.25, 8.4), rot=rotation((5.8, 1.25, 8.0), "z", 45)))
    el.append(panel(sc, "spout_tip", flat(COPPER), (3.4, 3.25, 7.65), (4.1, 3.75, 8.35)))
    for x0 in (6.0, 9.5):
        el.append(panel(sc, "post", flat(BRASS), (x0, 4.25, 7.75), (x0 + 0.5, 6.5, 8.25)))
    el.append(panel(sc, "bail", flat(BRASS), (6.0, 6.5, 7.75), (10.0, 7.0, 8.25)))
    el.append(panel(sc, "grip", lengthwise(WOOD), (7.0, 6.4, 7.6), (9.0, 7.1, 8.4)))
    sc.models["whole"] = el
    sc.models["lid"] = [panel(sc, "lid", cu, (6.75, 4.25, 6.75), (9.25, 4.65, 9.25)),
                        panel(sc, "knob", flat(BRASS), (7.6, 4.65, 7.6), (8.4, 5.25, 8.4))]
    return sc


def simmer_pot():
    """The Simmer Pot, facing north: a cream enamel saucepan 25 cm across and 16 cm high with a cobalt rim, its handle
    out to the back, of spiced water with apple slices, cinnamon sticks and cloves floating in it. Models: `whole`, its
    surface (`#s`) the still brew or, simmering, the animated one (part2_assets gives each its texture)."""
    sc = Sculpt(hh.SIMMER_POT["block"], 2504, 64)
    enamel = flat(ENAMEL)
    el = walls(sc, "pot", enamel, 6.0, 6.0, 10.0, 10.0, 0.5, 0.0, 2.25)
    el.append(panel(sc, "base", enamel, (6.5, 0.0, 6.5), (9.5, 0.5, 9.5)))
    el += walls(sc, "rim", flat(COBALT), 5.9, 5.9, 10.1, 10.1, 0.6, 2.25, 2.6)
    el.append(panel(sc, "handle", flat(COBALT), (7.6, 1.9, 10.1), (8.4, 2.3, 13.25)))
    surface = {"from": [6.5, 0.5, 6.5], "to": [9.5, 2.0, 9.5], "faces": {"up": {"uv": [6.5, 6.5, 9.5, 9.5], "texture": "#s"}}}
    el.append(surface)
    for x0, z0 in ((6.75, 7.0), (8.25, 8.0)):
        el.append(panel(sc, "slice", flat(APPLE), (x0, 2.0, z0), (x0 + 1.0, 2.2, z0 + 1.0)))
        el.append(panel(sc, "peel", flat(PEEL), (x0 + 1.0, 2.0, z0), (x0 + 1.15, 2.2, z0 + 1.0)))
    el.append(panel(sc, "cinnamon", lengthwise(WOOD), (6.75, 2.0, 8.6), (8.75, 2.35, 8.95), rot=rotation((7.75, 2.1, 8.75), "y", 22.5)))
    for x0, z0 in ((8.6, 7.0), (7.0, 8.0)):
        el.append(panel(sc, "clove", flat(BARK), (x0, 2.0, z0), (x0 + 0.3, 2.25, z0 + 0.3)))
    sc.models["whole"] = el
    return sc


_built = {}
BUILDERS = {hh.FIREPLACE["block"]: fireplace, hh.WOODSTOVE["block"]: woodstove, hh.KETTLE["block"]: kettle, hh.SIMMER_POT["block"]: simmer_pot}
# Each one's texture's size: the fireplace's and the stove's 128 texels square, the kettle's and the pot's 64.
SIZES = {hh.FIREPLACE["block"]: 128, hh.WOODSTOVE["block"]: 128, hh.KETTLE["block"]: 64, hh.SIMMER_POT["block"]: 64}


def build(block):
    if block not in _built:
        _built[block] = ppd.planned(BUILDERS[block], SIZES[block])
    return _built[block]


def _image_of(name):
    return build(name).atlas.img if name in BUILDERS else None


ITEM_DISPLAY = {"gui": {"rotation": [25, 225, 0], "translation": [0, 0, 0], "scale": [0.625, 0.625, 0.625]},
                "ground": {"rotation": [0, 0, 0], "translation": [0, 2, 0], "scale": [0.3, 0.3, 0.3]},
                "fixed": {"rotation": [0, 0, 0], "translation": [0, 0, 0], "scale": [0.6, 0.6, 0.6]},
                "thirdperson_righthand": {"rotation": [75, 45, 0], "translation": [0, 2, 0], "scale": [0.35, 0.35, 0.35]},
                "firstperson_righthand": {"rotation": [0, 45, 0], "translation": [0, 0, 0], "scale": [0.35, 0.35, 0.35]}}


def fire_model(block, elements):
    """A model of the fire's sheets on its own animated texture."""
    return {"textures": {"particle": f"{MOD}:block/{block}_fire", "f": f"{MOD}:block/{block}_fire"}, "elements": elements}


def assets(root, write, lang):
    """The fireplace's nine blocks' models, cut from the whole, the garland's and the fire's cut the same way, a
    multipart blockstate adding the garland by `garland` and the fire by `lit`, the fire texture's animation, its item
    (the fireplace whole, dressed and lit) and the garland's item (its own icon)."""
    write = fa.closing_writer(write, _image_of)
    spec = hh.FIREPLACE
    block = spec["block"]
    lang[f"block.{MOD}.{block}"] = spec["display"]
    models = root / "models" / "block"
    sc = build(block)
    n = len(d17d.cells(spec["size"]))
    for part, elements in enumerate(ppd.cut(sc.models["whole"], spec["size"])):
        write(models / f"{block}_{part}.json", fa.model(block, elements))
    garland_parts = ppd.cut(sc.models["garland"], spec["size"])
    for part, elements in enumerate(garland_parts):
        if elements:
            write(models / f"{block}_{part}_garland.json", fa.model(block, elements))
    fire_parts = ppd.cut(sc.models["fire"], spec["size"])
    for part, elements in enumerate(fire_parts):
        if elements:
            write(models / f"{block}_{part}_fire.json", fire_model(block, elements))
    multipart = []
    for f, y in FACINGS.items():
        turn = {"y": y} if y else {}
        for p in range(n):
            multipart.append({"when": {"facing": f, "part": str(p)}, "apply": {"model": rid(f"block/{block}_{p}"), **turn}})
            if garland_parts[p]:
                multipart.append({"when": {"facing": f, "part": str(p), "garland": "true"},
                                  "apply": {"model": rid(f"block/{block}_{p}_garland"), **turn}})
            if fire_parts[p]:
                multipart.append({"when": {"facing": f, "part": str(p), "lit": "true"}, "apply": {"model": rid(f"block/{block}_{p}_fire"), **turn}})
    write(root / "blockstates" / f"{block}.json", {"multipart": multipart})
    write(root / "textures" / "block" / f"{block}_fire.png.mcmeta", {"animation": {"frametime": spec["frametime"], "interpolate": True}})
    whole = sc.models["whole"] + sc.models["garland"]
    write(root / "models" / "item" / f"{block}.json",
          fa.model(block, fa.transformed(d17d.pinned(d20d.separated(whole)), 1.0 / 3.0, (8 - 8, 0, 8 - 8 / 3.0), (0, 0, 0)), ITEM_DISPLAY))
    write(root / "items" / f"{block}.json", {"model": {"type": "minecraft:model", "model": rid(f"item/{block}")}})
    item = hh.GARLAND["item"]
    lang[f"item.{MOD}.{item}"] = hh.GARLAND["display"]
    write(root / "models" / "item" / f"{item}.json", {"parent": "minecraft:item/generated", "textures": {"layer0": rid(f"item/{item}")}})
    write(root / "items" / f"{item}.json", {"model": {"type": "minecraft:model", "model": rid(f"item/{item}")}})
    lang[f"message.{MOD}.{block}.light"] = "Light it with flint and steel or a fire charge"
    part2_assets(root, write, lang)


# The kettle and the pot are small: shown larger in a slot and a hand, lifted to the middle.
KETTLE_DISPLAY = {"gui": {"rotation": [25, 225, 0], "translation": [0, 4, 0], "scale": [1.5, 1.5, 1.5]},
                  "ground": {"rotation": [0, 0, 0], "translation": [0, 3, 0], "scale": [0.8, 0.8, 0.8]},
                  "fixed": {"rotation": [0, 0, 0], "translation": [0, 3, 0], "scale": [1.3, 1.3, 1.3]},
                  "thirdperson_righthand": {"rotation": [0, 45, 0], "translation": [0, 3, 1], "scale": [0.8, 0.8, 0.8]},
                  "firstperson_righthand": {"rotation": [0, 45, 0], "translation": [0, 3, 0], "scale": [0.8, 0.8, 0.8]}}
POT_DISPLAY = {"gui": {"rotation": [25, 225, 0], "translation": [0, 6, 0], "scale": [1.8, 1.8, 1.8]},
               "ground": {"rotation": [0, 0, 0], "translation": [0, 3, 0], "scale": [1.0, 1.0, 1.0]},
               "fixed": {"rotation": [0, 0, 0], "translation": [0, 4, 0], "scale": [1.5, 1.5, 1.5]},
               "thirdperson_righthand": {"rotation": [0, 45, 0], "translation": [0, 4, 1], "scale": [1.0, 1.0, 1.0]},
               "firstperson_righthand": {"rotation": [0, 45, 0], "translation": [0, 4, 0], "scale": [1.0, 1.0, 1.0]}}


def part2_assets(root, write, lang):
    """The woodstove's model and its fire's (a multipart: the fire added while lit), the kettle's (without its lid, and
    the lid as its `runner`, drawn by client/TeaKettleRenderer), the simmer pot's still and simmering, and their items,
    each shown whole."""
    models = root / "models" / "block"
    states = root / "blockstates"
    fire_tex = f"{MOD}:block/{hh.FIREPLACE['block']}_fire"
    block = hh.WOODSTOVE["block"]
    lang[f"block.{MOD}.{block}"] = hh.WOODSTOVE["display"]
    sc = build(block)
    whole = d20d.separated(sc.models["whole"])
    write(models / f"{block}.json", fa.model(block, whole))
    write(models / f"{block}_fire.json", {"textures": {"particle": fire_tex, "f": fire_tex}, "elements": sc.models["fire"]})
    multipart = []
    for f, y in FACINGS.items():
        turn = {"y": y} if y else {}
        multipart.append({"when": {"facing": f}, "apply": {"model": rid(f"block/{block}"), **turn}})
        multipart.append({"when": {"facing": f, "lit": "true"}, "apply": {"model": rid(f"block/{block}_fire"), **turn}})
    write(states / f"{block}.json", {"multipart": multipart})
    write(root / "models" / "item" / f"{block}.json", fa.model(block, whole))
    write(root / "items" / f"{block}.json", {"model": {"type": "minecraft:model", "model": rid(f"item/{block}")}})

    block = hh.KETTLE["block"]
    lang[f"block.{MOD}.{block}"] = hh.KETTLE["display"]
    sc = build(block)
    write(models / f"{block}.json", fa.model(block, d20d.separated(sc.models["whole"])))
    write(models / f"{block}_lid.json", fa.model(block, d20d.separated(sc.models["lid"])))
    variants = {}
    for f, y in FACINGS.items():
        turn = {"y": y} if y else {}
        for heated in ("false", "true"):
            variants[f"facing={f},heated={heated},runner=false"] = {"model": rid(f"block/{block}"), **turn}
            variants[f"facing={f},heated={heated},runner=true"] = {"model": rid(f"block/{block}_lid"), **turn}
    write(states / f"{block}.json", {"variants": variants})
    write(root / "models" / "item" / f"{block}.json", fa.model(block, d20d.separated(sc.models["whole"] + sc.models["lid"]), KETTLE_DISPLAY))
    write(root / "items" / f"{block}.json", {"model": {"type": "minecraft:model", "model": rid(f"item/{block}")}})

    block = hh.SIMMER_POT["block"]
    lang[f"block.{MOD}.{block}"] = hh.SIMMER_POT["display"]
    sc = build(block)
    variants = {}
    for heated, suffix in (("false", ""), ("true", "_simmering")):
        model = fa.model(block, d20d.separated(sc.models["whole"]))
        model["textures"]["s"] = rid(f"block/{block}_brew" + ("" if heated == "true" else "_still"))
        write(models / f"{block}{suffix}.json", model)
        for f, y in FACINGS.items():
            variants[f"facing={f},heated={heated}"] = {"model": rid(f"block/{block}{suffix}"), **({"y": y} if y else {})}
    write(states / f"{block}.json", {"variants": variants})
    item = fa.model(block, d20d.separated(sc.models["whole"]), POT_DISPLAY)
    item["textures"]["s"] = rid(f"block/{block}_brew_still")
    write(root / "models" / "item" / f"{block}.json", item)
    write(root / "items" / f"{block}.json", {"model": {"type": "minecraft:model", "model": rid(f"item/{block}")}})
    write(root / "textures" / "block" / f"{block}_brew.png.mcmeta",
          {"animation": {"frametime": hh.SIMMER_POT["frametime"], "interpolate": True}})


def loot(out, write):
    """The fireplace drops once, from its first block (a garland on it stays with the blocks, so it is dropped too)."""
    block = hh.FIREPLACE["block"]
    write(out / f"{block}.json", {"type": "minecraft:block", "pools": [
        {"condition": {"type": "minecraft:all_of", "terms": [{"type": "minecraft:survives_explosion"},
                                                             {"type": "minecraft:match_block", "blocks": rid(block), "state": {"part": "0"}}]},
         "entries": [{"type": "minecraft:item", "name": rid(block)}], "rolls": 1},
        {"condition": {"type": "minecraft:match_block", "blocks": rid(block), "state": {"part": "0", "garland": "true"}},
         "entries": [{"type": "minecraft:item", "name": rid(hh.GARLAND["item"])}], "rolls": 1}],
        "random_sequence": rid(f"blocks/{block}")})
    for block in (hh.WOODSTOVE["block"], hh.KETTLE["block"], hh.SIMMER_POT["block"]):
        write(out / f"{block}.json", {"type": "minecraft:block", "pools": [{
            "condition": {"type": "minecraft:survives_explosion"}, "entries": [{"type": "minecraft:item", "name": rid(block)}], "rolls": 1}],
            "random_sequence": rid(f"blocks/{block}")})


def tags(tags):
    """Stone and iron come off with a pickaxe; the woodstove heats what stands on it, as the Kitchen Stove does."""
    for block in (hh.FIREPLACE["block"], hh.WOODSTOVE["block"], hh.KETTLE["block"], hh.SIMMER_POT["block"]):
        tags.add("block", "minecraft:mineable/pickaxe", rid(block))
    tags.add("block", f"{MOD}:heat_sources", rid(hh.WOODSTOVE["block"]))


def textures():
    import item_icons
    block = hh.FIREPLACE["block"]
    pot = hh.SIMMER_POT["block"]
    out = {("block", block): build(block).atlas.img, ("block", f"{block}_fire"): fire_strip(),
           ("item", hh.GARLAND["item"]): item_icons.draw(hh.GARLAND["item"]),
           ("block", f"{pot}_brew"): brew_strip(), ("block", f"{pot}_brew_still"): brew_still()}
    for other in (hh.WOODSTOVE["block"], hh.KETTLE["block"], pot):
        out[("block", other)] = build(other).atlas.img
    return out

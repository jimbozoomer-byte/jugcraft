"""Files for Barn & Harvest Tools, decor set 3 (tools/barn_tools.py; docs/features/barn-and-harvest-tools.md).

Part 1's hand tools are flat items: the Watering Can's and the Seed Pouch's models and lang are written here, the Steel
Sickle's beside the other sickles (tools/agriculture_data.py, from SICKLES). Their icons, and part 2's Flour, Butter and
Pancakes (whose item models tools/agriculture_data.py writes from ITEMS), are maps drawn by tools/item_icons.py from
tools/item_icons/<name>.txt, checked by tools/check_icon_maps.py.

Part 2's Quern and Butter Churn are sculpted on the toolkit of tools/flora_art.py and tools/pumpkin_patch_data.py, each
block one texture painted here, at vanilla's density (one texel to a model pixel), and real-life sized: the quern's
stones 44 cm across on a stand 69 cm high, the churn 38 cm across and 78 cm to its lid. Each has a part that moves (the
quern's upper stone and its peg, the churn's dasher) drawn by its own model, which the block entity's renderer turns or
lifts (client/QuernRenderer, client/ButterChurnRenderer); the block's own model leaves it out.

Everything is drawn here by code; no Mojang texture is read, traced or copied.
"""

import barn_tools as bt
import decor17_data as d17d
import decor20_data as d20d
from decor18_data import shifted as d18d_shifted
import flora_art as fa
import item_icons
import pumpkin_patch_data as ppd
from flora_art import Sculpt, pal, shade
from flora_art import rotation
from pumpkin_patch_data import GREY_WOOD, IRON, WOOD, flat, lengthwise, panel, planks

MOD = "jugcraft"
FACINGS = ppd.FACINGS

# Palettes, darkest first: a grey-brown millstone grit, flour, wheat in the quern's eye.
STONE = pal("4a4640", "625d55", "7a746a", "928b80", "aaa396")
FLOUR = pal("c6bca4", "dcd3bd", "eee7d6")
GRAIN = pal("7e5e1e", "a8862e", "c8a644", "e0c464")
# Part 3: herbs fresh and dried, chilies fresh and dried, the string they hang by, and ear corn.
HERBS = {"herbs": pal("243e18", "345a22", "4a7a2e", "64983c"), "dried_herbs": pal("3e3e26", "5a5836", "767248", "908a5a")}
CHILI = {"chili": pal("520e08", "801a0e", "ac2814", "cc4424"), "dried_chili": pal("340c08", "52140c", "702014", "8a321c")}
STRING = pal("847456", "aa9a76", "cabc96")
CORN = pal("6a4a0e", "a07818", "d0a828", "e8c440", "f4dc70")
# Part 4: the bell's cast bronze, the pulley's rope, the bell post's stone footing.
BRONZE = pal("4a2a12", "74441e", "a0642c", "c0823e", "dca45a")
ROPE = pal("6e5c3a", "927c52", "b09a6a")
FOOTING = pal("4e4c48", "68655f", "807d76", "999690")


def rid(path):
    return f"{MOD}:{path}"


# ---------------------------------------------------------------- painters

def stone(face=False, eye=False):
    """Millstone grit, its middle tone. A side is lit along its upper row and shaded along its lower one; a face (`face`)
    is the one flat tone, so the bands a round stone is built of read as one face, and the runner's has its eye, the
    darkest tone, at its middle."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                k = 2 if face else 3 if y == 0 else 1 if (y == p.h - 1 and p.h > 1) else 2
                p.put(x, y, shade(STONE, k))
        if eye:
            p.put(p.w // 2, p.h // 2, shade(STONE, 0))
    paint.tag = repr(("stone", face, eye))
    return paint


def round_slab(sc, name, cx, cz, across, y0, y1, side, top, bottom=None):
    """A round slab `across` pixels wide (odd: 7 or 5) about (cx, cz), from y0 to y1, as a circle is drawn in pixels: three
    boxes, each a row band of the circle (for 7 across, 3 by 7, 5 by 5 and 7 by 3), centred on it. `separated` keeps
    their coincident tops and bottoms apart."""
    h = across / 2
    bands = ((h - 2, h), (h - 1, h - 1), (h, h - 2)) if across >= 7 else ((h - 1, h), (h, h - 1))
    over = {"up": top, "down": bottom or top}
    return [panel(sc, name, side, (cx - a, y0, cz - b), (cx + a, y1, cz + b), **over) for a, b in bands]


# ---------------------------------------------------------------- part 2: the Quern

def quern():
    """The Quern, facing north: a stand of four legs, rails and a top board; on it a flour tray (walls round a floor
    dusted with flour, a spout at the front) round the bed stone; and the runner, the upper stone with its eye and a
    turned peg near its rim, drawn by its own models (with wheat in its eye, or without) for the renderer to turn."""
    spec = bt.QUERN
    sc = Sculpt(spec["block"], 2301, 64)
    el = []
    # The stand: legs at the corners of its 10-pixel top, rails between them under it, the top board.
    for x in (3.0, 12.0):
        for z in (3.0, 12.0):
            el.append(panel(sc, "leg", lengthwise(WOOD), (x, 0.0, z), (x + 1.0, 10.0, z + 1.0)))
    for z in (3.0, 12.0):
        el.append(panel(sc, "rail", lengthwise(WOOD), (4.0, 7.5, z + 0.25), (12.0, 8.5, z + 0.75)))
    for x in (3.0, 12.0):
        el.append(panel(sc, "rail", lengthwise(WOOD), (x + 0.25, 7.5, 4.0), (x + 0.75, 8.5, 12.0)))
    el.append(panel(sc, "top", planks(WOOD, board=4, nails=False), (2.5, 10.0, 2.5), (13.5, 11.0, 13.5)))
    # The flour tray: low walls round the bed stone, flour dusting its floor, and the spout it leaves by at the front.
    el += ppd.walls(sc, "tray", flat(WOOD), 3.5, 3.5, 12.5, 12.5, 0.5, 11.0, 12.0)
    el.append(panel(sc, "flour", flat(FLOUR), (4.0, 11.0, 4.0), (12.0, 11.25, 12.0)))
    el.append(panel(sc, "spout", flat(WOOD), (7.0, 11.0, 2.0), (9.0, 11.5, 3.5)))
    el.append(panel(sc, "spout_flour", flat(FLOUR), (7.25, 11.5, 2.25), (8.75, 11.75, 3.5)))
    # The bed stone, fixed in the tray.
    el += round_slab(sc, "bed", 8.0, 8.0, 7, 11.25, 12.5, stone(), stone(face=True))
    sc.models["base"] = el
    # The runner: the upper stone, its eye, and its peg near the rim at the front.
    runner = round_slab(sc, "runner", 8.0, 8.0, 7, 12.5, 14.0, stone(), stone(face=True, eye=True), stone(face=True))
    runner.append(panel(sc, "peg", lengthwise(WOOD), (7.5, 14.0, 5.0), (8.5, 18.0, 6.0)))
    sc.models["runner"] = runner
    # Wheat waiting in the eye.
    sc.models["runner_loaded"] = runner + [panel(sc, "grain", flat(GRAIN), (7.25, 14.0, 7.25), (8.75, 14.5, 8.75))]
    return sc


# ---------------------------------------------------------------- part 2: the Butter Churn

def churn():
    """The Butter Churn, facing north: a round churn of upright staves (two row bands of a circle six pixels across),
    two iron hoops standing a little proud of them, a lid set into its top with a lifting cleat, and the dasher's handle,
    drawn by its own model for the renderer to work up and down through the lid."""
    spec = bt.CHURN
    sc = Sculpt(spec["block"], 2302, 64)
    staves = planks(WOOD, board=3, nails=False)
    el = []
    for a, b in ((2.0, 3.0), (3.0, 2.0)):
        el.append(panel(sc, "staves", staves, (8.0 - a, 0.0, 8.0 - b), (8.0 + a, 12.5, 8.0 + b), up=flat(WOOD), down=flat(WOOD)))
    for y in (1.5, 9.5):
        for a, b in ((2.0, 3.0), (3.0, 2.0)):
            el.append(panel(sc, "hoop", flat(IRON), (8.0 - a - 0.25, y, 8.0 - b - 0.25), (8.0 + a + 0.25, y + 1.0, 8.0 + b + 0.25)))
    # The lid, a board set into the top, and a cleat across it to lift it by (the handle passes beside it).
    el.append(panel(sc, "lid", planks(WOOD, board=2, nails=False), (6.0, 12.5, 6.0), (10.0, 13.0, 10.0)))
    el.append(panel(sc, "cleat", lengthwise(WOOD), (6.5, 13.0, 9.0), (9.5, 13.5, 9.75)))
    sc.models["base"] = el
    # The dasher: its handle up through the lid, a turned grip at the top.
    sc.models["runner"] = [panel(sc, "handle", lengthwise(WOOD), (7.5, 10.0, 7.5), (8.5, 21.0, 8.5)),
                           panel(sc, "grip", lengthwise(WOOD), (7.25, 19.0, 7.25), (8.75, 21.5, 8.75))]
    return sc


# ---------------------------------------------------------------- part 3: the Drying Rack

def sprigs(palette):
    """A bunch of sprigs hung head down: strands one texel wide along it in two tones by turns, the top row (under the
    tie) a tone darker."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                p.put(x, y, shade(palette, 1 if y == 0 and p.h > 2 else 3 if x % 2 == 0 else 2))
    paint.tag = repr(("sprigs", palette))
    return paint


# The hooks, as (slot, x, the rail's underside): slot 0 and 1 on the lower rail, 2 and 3 on the upper; 0 and 2 at the
# viewer's right as they face the rack (the frame's low x), 1 and 3 at the left.
HOOKS = ((0, 5.0, 15.0), (1, 11.0, 15.0), (2, 5.0, 29.0), (3, 11.0, 29.0))
BUNCHES = ("herbs", "dried_herbs", "chili", "dried_chili")


def bunch(sc, kind, cx, top):
    """A bunch hung from a hook at (cx, top), its middle at z 8: herbs (tied, stems up, the leaves spreading below), or a
    ristra of five chilies along a string, by turns either side of it."""
    if kind in HERBS:
        pal_ = HERBS[kind]
        return [panel(sc, "tie", flat(STRING), (cx - 0.75, top - 1.0, 7.25), (cx + 0.75, top, 8.75)),
                panel(sc, f"{kind}_stems", flat(pal_), (cx - 0.5, top - 2.5, 7.5), (cx + 0.5, top - 1.0, 8.5)),
                panel(sc, f"{kind}_leaves", sprigs(pal_), (cx - 1.25, top - 5.0, 6.75), (cx + 1.25, top - 2.5, 9.25)),
                panel(sc, f"{kind}_leaves", sprigs(pal_), (cx - 1.75, top - 6.5, 6.5), (cx + 1.75, top - 5.0, 9.5))]
    pal_ = CHILI[kind]
    out = [panel(sc, "string", flat(STRING), (cx - 0.25, top - 8.0, 7.75), (cx + 0.25, top, 8.25))]
    for k in range(5):
        x = cx + (0.75 if k % 2 == 0 else -0.75)
        y = top - 1.0 - 1.5 * k
        out.append(panel(sc, f"{kind}_pepper", flat(pal_), (x - 0.5, y - 2.0, 7.5), (x + 0.5, y, 8.5)))
    return out


def rack():
    """The Drying Rack, facing north: two uprights on splayed feet, a lower rail between them and a top rail across them,
    an iron hook under each rail at each slot; and each slot's bunch of each kind (`overlays`, keyed slot and kind)."""
    spec = bt.RACK
    sc = Sculpt(spec["block"], 2303, 64)
    el = []
    for x in (1.0, 13.0):
        el.append(panel(sc, "foot", lengthwise(WOOD), (x, 0.0, 3.0), (x + 2.0, 1.0, 13.0)))
        el.append(panel(sc, "upright", lengthwise(WOOD), (x + 0.5, 1.0, 7.5), (x + 1.5, 30.0, 8.5)))
    el.append(panel(sc, "rail", lengthwise(WOOD), (2.5, 15.0, 7.5), (13.5, 16.0, 8.5)))
    el.append(panel(sc, "rail", lengthwise(WOOD), (1.0, 30.0, 7.25), (15.0, 31.0, 8.75)))
    for _, cx, top in HOOKS:
        el.append(panel(sc, "hook", flat(IRON), (cx - 0.25, top - 0.5, 7.75), (cx + 0.25, top, 8.25)))
    sc.models["whole"] = el
    sc.overlays = {(slot, kind): bunch(sc, kind, cx, top) for slot, cx, top in HOOKS for kind in BUNCHES}
    return sc


# ---------------------------------------------------------------- part 3: the Corn Crib

def ear_corn():
    """Ear corn heaped lengthwise: rows of ears two texels high, each six long with a dark texel between ears, every row
    set half an ear along from the last; the kernels in a checker of two tones, lit along each ear's top row."""
    def paint(p):
        for y in range(p.h):
            row = y // 2
            for x in range(p.w):
                at = (x + 3 * row) % 7
                if at == 6:
                    k = 0
                else:
                    k = (4 if (x + y) % 2 == 0 else 3) if y % 2 == 0 else (3 if (x + y) % 2 == 0 else 2)
                p.put(x, y, shade(CORN, k))
    paint.tag = repr(("ear_corn",))
    return paint


# The crib roof's pitch, the north half's turn about x (the south half's is the opposite): 22.5 degrees, the most a model
# turns a box short of 45; negative lifts the north half's ridge end.
ROOF = -22.5


def crib():
    """The Corn Crib, facing north, two blocks across and two up: four short posts, a plank floor, corner posts, walls of
    horizontal slats with a gap between each (so the corn shows), a top plate, gable ends stepped up to the ridge, and a
    tin roof in two turned halves a side; and the ears inside at each stage (`heap_<stage>`)."""
    spec = bt.CRIB
    sc = Sculpt(spec["block"], 2304, 128)
    slat = planks(GREY_WOOD, board=2, nails=False)
    el = []
    for x in (1.0, 29.0):
        for z in (2.0, 12.0):
            el.append(panel(sc, "post", lengthwise(GREY_WOOD), (x, 0.0, z), (x + 2.0, 5.0, z + 2.0)))
            el.append(panel(sc, "corner", lengthwise(GREY_WOOD), (x + 0.25, 5.0, z + 0.25), (x + 1.75, 28.0, z + 1.75)))
    el.append(panel(sc, "floor", planks(GREY_WOOD, board=4, nails=False), (1.0, 4.0, 2.0), (31.0, 5.0, 14.0)))
    # The slats: 1.5 pixels high with a pixel's gap, from the floor to the plate, front and back, then the ends.
    y = 5.5
    while y + 1.5 <= 28.0:
        el.append(panel(sc, "slat", slat, (2.75, y, 2.25), (29.25, y + 1.5, 3.0)))
        el.append(panel(sc, "slat", slat, (2.75, y, 13.0), (29.25, y + 1.5, 13.75)))
        el.append(panel(sc, "slat", slat, (1.25, y, 3.75), (2.0, y + 1.5, 12.25)))
        el.append(panel(sc, "slat", slat, (30.0, y, 3.75), (30.75, y + 1.5, 12.25)))
        y += 2.5
    el.append(panel(sc, "plate", lengthwise(GREY_WOOD), (1.0, 28.0, 2.0), (31.0, 29.0, 14.0)))
    # The gable ends, stepped up under the roof to its ridge.
    for x0, x1 in ((1.25, 2.25), (29.75, 30.75)):
        for z0, z1, y0, y1 in ((3.0, 13.0, 29.0, 30.5), (5.0, 11.0, 30.5, 31.25), (7.0, 9.0, 31.25, 32.0)):
            el.append(panel(sc, "gable", slat, (x0, y0, z0), (x1, y1, z1)))
    # The roof: corrugated tin, each side in two halves (one a block), each turned 22.5 degrees up about its eave over the
    # plate (just inside the frame, so the cut gives it to its block) to meet the other at the ridge.
    tin = lengthwise(IRON)
    for x0, x1 in ((0.0, 16.0), (16.0, 32.0)):
        mid = (x0 + x1) / 2
        el.append(panel(sc, "roof", tin, (x0, 29.0, -0.75), (x1, 29.75, 8.35), rot=rotation((mid, 29.0, 0.25), "x", ROOF)))
        el.append(panel(sc, "roof", tin, (x0, 29.0, 7.65), (x1, 29.75, 16.75), rot=rotation((mid, 29.0, 15.75), "x", -ROOF)))
    # A tin cap along the ridge, over the halves' meeting.
    el.append(panel(sc, "ridge", flat(IRON), (0.0, 32.25, 7.0), (32.0, 33.0, 9.0)))
    sc.models["whole"] = el
    # The ears rise from the floor to just under the plate (a quarter pixel short, so the full heap's top doesn't share the
    # plate's plane).
    inside = 27.75 - 5.0
    for stage in range(1, spec["stages"] + 1):
        top = 5.0 + inside * stage / spec["stages"]
        sc.models[f"heap_{stage}"] = [panel(sc, "corn", ear_corn(), (3.0, 5.0, 3.25), (29.0, top, 12.75))]
    return sc


# ---------------------------------------------------------------- part 4: the Hay-Loft Pulley

# Where the pulley's wheel turns (its axle, along x) and where its rope leaves it, going down, in the block's pixels.
WHEEL_AXLE = (12.0, 3.0)
ROPE_AT = (8.0, 1.5)
ROPE_TOP = 12.0


def pulley():
    """The Hay-Loft Pulley, facing north (its wall behind it, to the south): an iron wall plate, a beam out from it under
    the block's top with a knee brace under it, and an iron pulley block (two cheeks and an axle bolt) under the beam's
    end. Its moving pieces are their own models: the wheel (`wheel`), a block's length of rope (`rope`) and the hay hook
    (`hook`: a ring, a shank and two tines curving in), each drawn where it hangs at the top of the block."""
    spec = bt.PULLEY
    sc = Sculpt(spec["block"], 2305, 64)
    el = [panel(sc, "plate", flat(IRON), (5.5, 9.0, 15.25), (10.5, 15.5, 16.0)),
          panel(sc, "beam", lengthwise(WOOD), (6.75, 13.0, 1.0), (9.25, 15.5, 15.25)),
          # The knee brace, from low on the wall plate up under the beam, turned 45 degrees about its middle.
          panel(sc, "brace", lengthwise(WOOD), (7.25, 9.9, 8.85), (8.75, 11.1, 16.65), rot=fa.rotation((8.0, 10.5, 12.75), "x", 45)),
          panel(sc, "cheek", flat(IRON), (6.25, 10.0, 1.25), (6.75, 13.0, 4.75)),
          panel(sc, "cheek", flat(IRON), (9.25, 10.0, 1.25), (9.75, 13.0, 4.75)),
          panel(sc, "bolt", flat(IRON), (5.75, 11.5, 2.5), (10.25, 12.5, 3.5))]
    sc.models["base"] = el
    cy, cz = WHEEL_AXLE
    # The wheel: a disc three pixels across in two row bands, standing across x between the cheeks.
    sc.models["wheel"] = [panel(sc, "wheel", flat(WOOD), (7.0, cy - 1.5, cz - 1.0), (9.0, cy + 1.5, cz + 1.0)),
                          panel(sc, "wheel", flat(WOOD), (7.0, cy - 1.0, cz - 1.5), (9.0, cy + 1.0, cz + 1.5)),
                          panel(sc, "hub", flat(IRON), (6.85, cy - 0.5, cz - 0.5), (9.15, cy + 0.5, cz + 0.5))]
    x, z = ROPE_AT
    sc.models["rope"] = [panel(sc, "rope", lengthwise(ROPE), (x - 0.25, 0.0, z - 0.25), (x + 0.25, 16.0, z + 0.25))]
    # The hook, its ring at the top of the block (where the rope ends): a ring, a shank, a crosspiece and two tines.
    sc.models["hook"] = [panel(sc, "ring", flat(IRON), (x - 0.75, 14.5, z - 0.25), (x + 0.75, 16.0, z + 0.25)),
                         panel(sc, "shank", flat(IRON), (x - 0.25, 10.0, z - 0.25), (x + 0.25, 14.5, z + 0.25)),
                         panel(sc, "cross", flat(IRON), (x - 2.5, 9.5, z - 0.25), (x + 2.5, 10.0, z + 0.25))]
    for side in (-1, 1):
        sc.models["hook"] += [panel(sc, "tine", flat(IRON), (x + side * 2.5 - 0.25, 7.0, z - 0.25), (x + side * 2.5 + 0.25, 9.5, z + 0.25)),
                              panel(sc, "tip", flat(IRON), (x + side * 1.75 - 0.5, 6.5, z - 0.25), (x + side * 1.75 + 0.5, 7.0, z + 0.25))]
    return sc


# ---------------------------------------------------------------- part 4: the Harvest Bell

def cast(palette):
    """Cast metal round a bell: its middle tone, lit down its left edge and shaded down its right, the same on every band,
    so the bands read as one cast surface rather than a stack."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                p.put(x, y, shade(palette, 3 if x == 0 and p.w > 2 else 1 if x == p.w - 1 and p.w > 2 else 2))
    paint.tag = repr(("cast", palette))
    return paint


# The bell's axle (along x), about which it swings, in the frame's pixels.
BELL_AXLE = (29.5, 8.0)


def bell():
    """The Harvest Bell, facing north, one block across and two up: a stone footing, a squared post, an iron yoke on
    its top (a crosspiece and two arms with bearings), and the bell, its own model (`bell`, drawn in the upper block): a
    cast bronze bell of stepped bands from crown to lip on an iron axle through the yoke's bearings."""
    spec = bt.BELL
    sc = Sculpt(spec["block"], 2306, 64)
    el = [panel(sc, "footing", flat(FOOTING), (4.0, 0.0, 4.0), (12.0, 2.0, 12.0)),
          panel(sc, "post", lengthwise(GREY_WOOD), (7.0, 2.0, 7.0), (9.0, 23.5, 9.0)),
          panel(sc, "cross", flat(IRON), (4.0, 23.5, 7.25), (12.0, 24.5, 8.75))]
    for x0 in (4.0, 11.25):
        el.append(panel(sc, "arm", flat(IRON), (x0, 24.5, 7.5), (x0 + 0.75, 30.5, 8.5)))
    sc.models["whole"] = el
    ay, az = BELL_AXLE
    # A bell's profile, crown to lip: a narrow crown, the shoulder, a nearly straight waist, then the flare and the lip.
    bands = ((1.25, 28.25, 29.0), (2.0, 27.25, 28.25), (2.25, 25.5, 27.25), (2.75, 24.75, 25.5), (3.0, 24.25, 24.75))
    bell_ = [panel(sc, "axle", flat(IRON), (4.75, ay - 0.5, az - 0.5), (11.25, ay + 0.5, az + 0.5))]
    for half, y0, y1 in bands:
        bell_.append(panel(sc, "bell", cast(BRONZE), (8.0 - half, y0, az - half), (8.0 + half, y1, az + half), up=flat(BRONZE),
                           down=flat(BRONZE)))
    sc.models["bell"] = bell_
    return sc


# ---------------------------------------------------------------- building and files

_built = {}
BUILDERS = {bt.QUERN["block"]: (quern, 64), bt.CHURN["block"]: (churn, 64), bt.RACK["block"]: (rack, 64),
            bt.CRIB["block"]: (crib, 128), bt.PULLEY["block"]: (pulley, 64), bt.BELL["block"]: (bell, 64)}


def build(block):
    if block not in _built:
        builder, size = BUILDERS[block]
        _built[block] = ppd.planned(builder, size)
    return _built[block]


def _image_of(name):
    return build(name).atlas.img if name in BUILDERS else None


DISPLAY = {"gui": {"rotation": [25, 225, 0], "translation": [0, -1.5, 0], "scale": [0.75, 0.75, 0.75]},
           "ground": {"rotation": [0, 0, 0], "translation": [0, 2, 0], "scale": [0.4, 0.4, 0.4]},
           "fixed": {"rotation": [0, 0, 0], "translation": [0, -1, 0], "scale": [0.7, 0.7, 0.7]},
           "thirdperson_righthand": {"rotation": [75, 45, 0], "translation": [0, 2, 0], "scale": [0.4, 0.4, 0.4]},
           "firstperson_righthand": {"rotation": [0, 45, 0], "translation": [0, 0, 0], "scale": [0.4, 0.4, 0.4]}}


def part2_assets(root, write, lang):
    """The quern's and churn's block models (the block without its moving part; the moving part alone, which the
    renderer draws), blockstates (`runner` picks the moving part's model, never set on a placed block) and items."""
    write = fa.closing_writer(write, _image_of)
    models = root / "models" / "block"
    for spec, extra in ((bt.QUERN, ("loaded", ("false", "true"))), (bt.CHURN, ("full", ("false", "true")))):
        block = spec["block"]
        lang[f"block.{MOD}.{block}"] = spec["display"]
        sc = build(block)
        write(models / f"{block}.json", fa.model(block, d20d.separated(sc.models["base"])))
        runners = [m for m in sc.models if m.startswith("runner")]
        for name in runners:
            write(models / f"{block}_{name}.json", fa.model(block, d20d.separated(sc.models[name])))
        prop, values = extra
        variants = {}
        for f, y in FACINGS.items():
            turn = {"y": y} if y else {}
            for v in values:
                variants[f"facing={f},{prop}={v},runner=false"] = {"model": rid(f"block/{block}"), **turn}
                runner = "runner_loaded" if prop == "loaded" and v == "true" else "runner"
                variants[f"facing={f},{prop}={v},runner=true"] = {"model": rid(f"block/{block}_{runner}"), **turn}
        write(root / "blockstates" / f"{block}.json", {"variants": variants})
        write(root / "models" / "item" / f"{block}.json", fa.model(block, d20d.separated(sc.models["base"] + sc.models["runner"]), DISPLAY))
        write(root / "items" / f"{block}.json", {"model": {"type": "minecraft:model", "model": rid(f"item/{block}")}})
    lang[f"message.{MOD}.quern.full"] = "The quern is full"
    lang[f"message.{MOD}.quern.other"] = "The quern holds other grain: take it out first"
    lang[f"message.{MOD}.butter_churn.full"] = "The churn is full"
    lang[f"message.{MOD}.drying_rack.full"] = "Every hook has a bunch on it"
    part3_assets(root, write, lang)


# The rack's slots: which block (part) each hangs in and which of its sides, as DryingRackBlock names them.
RACK_SLOTS = {0: (0, "right"), 1: (0, "left"), 2: (1, "right"), 3: (1, "left")}
ITEM_DISPLAY = {"gui": {"rotation": [25, 225, 0], "translation": [0, 0, 0], "scale": [0.625, 0.625, 0.625]},
                "ground": {"rotation": [0, 0, 0], "translation": [0, 2, 0], "scale": [0.3, 0.3, 0.3]},
                "fixed": {"rotation": [0, 0, 0], "translation": [0, 0, 0], "scale": [0.6, 0.6, 0.6]},
                "thirdperson_righthand": {"rotation": [75, 45, 0], "translation": [0, 2, 0], "scale": [0.35, 0.35, 0.35]},
                "firstperson_righthand": {"rotation": [0, 45, 0], "translation": [0, 0, 0], "scale": [0.35, 0.35, 0.35]}}


def item_model(elements, size, scale):
    """The whole prop scaled into one block about its middle."""
    return fa.transformed(d17d.pinned(elements), scale, (8 - 8 * size[0] * scale, 0, 8 - 8 * size[2] * scale), (0, 0, 0))


def part3_assets(root, write, lang):
    """The Drying Rack's two blocks' models, each bunch's model moved into the block it hangs in, and a multipart
    blockstate adding them by each block's `left` and `right`; and the Corn Crib's four blocks' models, cut from the whole,
    with the ears at each stage cut the same way and added by a multipart blockstate by `fill` (its `fruit` and `lit`, from
    the store and the prop it shares code with, draw nothing). Their items show them whole: the rack hung with one bunch
    of each, the crib half full."""
    models = root / "models" / "block"
    states = root / "blockstates"

    spec = bt.RACK
    block = spec["block"]
    lang[f"block.{MOD}.{block}"] = spec["display"]
    sc = build(block)
    n = len(d17d.cells(spec["size"]))
    for part, elements in enumerate(ppd.cut(sc.models["whole"], spec["size"])):
        write(models / f"{block}_{part}.json", fa.model(block, elements))
    for (slot, kind), elements in sc.overlays.items():
        part, side = RACK_SLOTS[slot]
        write(models / f"{block}_{part}_{side}_{kind}.json", fa.model(block, ppd.cut(elements, spec["size"])[part]))
    multipart = []
    for f, y in FACINGS.items():
        turn = {"y": y} if y else {}
        for p in range(n):
            multipart.append({"when": {"facing": f, "part": str(p)}, "apply": {"model": rid(f"block/{block}_{p}"), **turn}})
        for slot, (p, side) in RACK_SLOTS.items():
            for kind in BUNCHES:
                multipart.append({"when": {"facing": f, "part": str(p), side: kind},
                                  "apply": {"model": rid(f"block/{block}_{p}_{side}_{kind}"), **turn}})
    write(states / f"{block}.json", {"multipart": multipart})
    hung = sc.models["whole"] + [e for slot, kind in zip(range(4), BUNCHES) for e in sc.overlays[(slot, kind)]]
    write(root / "models" / "item" / f"{block}.json", fa.model(block, item_model(hung, spec["size"], 0.5), ITEM_DISPLAY))
    write(root / "items" / f"{block}.json", {"model": {"type": "minecraft:model", "model": rid(f"item/{block}")}})

    spec = bt.CRIB
    block = spec["block"]
    lang[f"block.{MOD}.{block}"] = spec["display"]
    sc = build(block)
    n = len(d17d.cells(spec["size"]))
    for part, elements in enumerate(ppd.cut(sc.models["whole"], spec["size"])):
        write(models / f"{block}_{part}.json", fa.model(block, elements))
    for stage in range(1, spec["stages"] + 1):
        for part, elements in enumerate(ppd.cut(sc.models[f"heap_{stage}"], spec["size"])):
            write(models / f"{block}_{part}_heap_{stage}.json", fa.model(block, elements))
    multipart = []
    for f, y in FACINGS.items():
        turn = {"y": y} if y else {}
        for p in range(n):
            multipart.append({"when": {"facing": f, "part": str(p)}, "apply": {"model": rid(f"block/{block}_{p}"), **turn}})
            for stage in range(1, spec["stages"] + 1):
                multipart.append({"when": {"facing": f, "part": str(p), "fill": str(stage)},
                                  "apply": {"model": rid(f"block/{block}_{p}_heap_{stage}"), **turn}})
    write(states / f"{block}.json", {"multipart": multipart})
    half = sc.models["whole"] + sc.models[f"heap_{spec['stages'] // 2}"]
    write(root / "models" / "item" / f"{block}.json", fa.model(block, item_model(half, spec["size"], 0.5), ITEM_DISPLAY))
    write(root / "items" / f"{block}.json", {"model": {"type": "minecraft:model", "model": rid(f"item/{block}")}})
    part4_assets(root, write, lang)


# The pulley's pieces, as HayPulleyBlock.PIECE names them, and the bell's moving part.
PULLEY_PIECES = ("block", "wheel", "rope", "hook")


def part4_assets(root, write, lang):
    """The Hay-Loft Pulley's model and its moving pieces' (the blockstate's `piece` picks one; a placed pulley is
    `piece=block`, the rest are drawn by client/HayPulleyRenderer), and the Harvest Bell's two blocks' models, cut from the
    whole, and its bell's (`runner=true`, drawn swinging by client/HandWorkedRenderer). Their items show them whole: the
    pulley with a block of rope and its hook, the bell on its post."""
    models = root / "models" / "block"
    states = root / "blockstates"

    spec = bt.PULLEY
    block = spec["block"]
    lang[f"block.{MOD}.{block}"] = spec["display"]
    sc = build(block)
    names = {"block": "base", "wheel": "wheel", "rope": "rope", "hook": "hook"}
    for piece in PULLEY_PIECES:
        name = block if piece == "block" else f"{block}_{piece}"
        write(models / f"{name}.json", fa.model(block, d20d.separated(sc.models[names[piece]])))
    variants = {}
    for f, y in FACINGS.items():
        turn = {"y": y} if y else {}
        for drop in range(1, spec["drops"] + 1):
            for piece in PULLEY_PIECES:
                name = block if piece == "block" else f"{block}_{piece}"
                variants[f"drop={drop},facing={f},piece={piece}"] = {"model": rid(f"block/{name}"), **turn}
    write(states / f"{block}.json", {"variants": variants})
    hung = (sc.models["base"] + sc.models["wheel"] + d18d_shifted(sc.models["rope"], 0, ROPE_TOP - 16.0, 0)
            + d18d_shifted(sc.models["hook"], 0, ROPE_TOP - 32.0, 0))
    write(root / "models" / "item" / f"{block}.json",
          fa.model(block, fa.transformed(d17d.pinned(d20d.separated(hung)), 0.5, (4, 8, 4), (0, 0, 0)), ITEM_DISPLAY))
    write(root / "items" / f"{block}.json", {"model": {"type": "minecraft:model", "model": rid(f"item/{block}")}})

    spec = bt.BELL
    block = spec["block"]
    lang[f"block.{MOD}.{block}"] = spec["display"]
    sc = build(block)
    n = len(d17d.cells(spec["size"]))
    for part, elements in enumerate(ppd.cut(sc.models["whole"], spec["size"])):
        write(models / f"{block}_{part}.json", fa.model(block, elements))
    write(models / f"{block}_bell.json", fa.model(block, d18d_shifted(d20d.separated(sc.models["bell"]), 0, -16.0, 0)))
    variants = {}
    for f, y in FACINGS.items():
        turn = {"y": y} if y else {}
        for p in range(n):
            for lit in ("false", "true"):
                variants[f"facing={f},lit={lit},part={p},runner=false"] = {"model": rid(f"block/{block}_{p}"), **turn}
                variants[f"facing={f},lit={lit},part={p},runner=true"] = {"model": rid(f"block/{block}_bell"), **turn}
    write(states / f"{block}.json", {"variants": variants})
    write(root / "models" / "item" / f"{block}.json",
          fa.model(block, item_model(sc.models["whole"] + sc.models["bell"], spec["size"], 0.5), ITEM_DISPLAY))
    write(root / "items" / f"{block}.json", {"model": {"type": "minecraft:model", "model": rid(f"item/{block}")}})
    lang[f"message.{MOD}.harvest_bell.ringing"] = "The bell is still ringing"


def loot(out, write):
    """The quern and the churn drop themselves; the quern spills its grain (QuernBlockEntity), the churn its milk. The
    rack, the crib and the bell drop once, from their first block; the rack gives back its bunches, the crib spills its
    corn. The pulley drops itself."""
    for block in (bt.QUERN["block"], bt.CHURN["block"], bt.PULLEY["block"]):
        write(out / f"{block}.json", {"type": "minecraft:block", "pools": [{
            "condition": {"type": "minecraft:survives_explosion"}, "entries": [{"type": "minecraft:item", "name": rid(block)}], "rolls": 1}],
            "random_sequence": rid(f"blocks/{block}")})
    for block in (bt.RACK["block"], bt.CRIB["block"], bt.BELL["block"]):
        write(out / f"{block}.json", {"type": "minecraft:block", "pools": [{
            "condition": {"type": "minecraft:all_of", "terms": [{"type": "minecraft:survives_explosion"},
                                                                {"type": "minecraft:match_block", "blocks": rid(block), "state": {"part": "0"}}]},
            "entries": [{"type": "minecraft:item", "name": rid(block)}], "rolls": 1}], "random_sequence": rid(f"blocks/{block}")})


def tags(tags):
    for block in bt.blocks():
        tags.add("block", "minecraft:mineable/axe", rid(block))


def assets(root, write, lang):
    for spec in (bt.WATERING_CAN, bt.SEED_POUCH):
        item = spec["item"]
        write(root / "models" / "item" / f"{item}.json", {"parent": "minecraft:item/generated", "textures": {"layer0": rid(f"item/{item}")}})
        write(root / "items" / f"{item}.json", {"model": {"type": "minecraft:model", "model": rid(f"item/{item}")}})
        lang[f"item.{MOD}.{item}"] = spec["display"]
    lang[f"item.{MOD}.watering_can.water"] = "Water: %s of %s pours"
    lang[f"item.{MOD}.seed_pouch.contents"] = "%s of %s kinds of seed, %s seeds"
    part2_assets(root, write, lang)


def textures():
    """The part 1 icons and part 2's foods', from their maps (the Steel Sickle's replaces nothing: the flint and bronze
    sickles keep tools/crop_textures.py's drawings), and the quern's and churn's block textures."""
    out = {("item", name): item_icons.draw(name) for name in ("steel_sickle", bt.WATERING_CAN["item"], bt.SEED_POUCH["item"], *bt.ITEMS)}
    out.update({("block", block): build(block).atlas.img for block in bt.blocks()})
    return out

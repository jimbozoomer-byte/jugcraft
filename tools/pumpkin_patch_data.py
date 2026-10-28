"""Generated data for the Pumpkin Patch Market's displays (decor set 1, part 2; tools/pumpkin_patch.py DISPLAYS): the
Pumpkin Wheelbarrow, Pumpkin Wagon and Pumpkin Pyramid, sculpted on the toolkit of tools/flora_art.py (each block one
texture painted here), their blockstates, items, names, loot and tags.

How they are drawn (docs/ART_DIRECTION.md):
- Real-life sized: the wheelbarrow is two blocks long, the wagon two wide, two high and four long on wheels a block
  high, the pyramid three blocks square. Each is modelled whole in its frame and cut into its blocks' models
  (`cut`), each piece of a cut box keeping its share of the picture; a turned part (a wheel's diagonal spokes and rim)
  lies wholly in one block.
- Built from boxes with shape: spoked wheels with iron-shod octagonal rims and hubs, slatted sides on stakes, a flared
  tray with a rolled rim, stepped tiers with lips. Hollows (the tray, the wagon bed) are walls round them with their
  insides drawn (flora_art.box_ring). No two differently drawn faces lie in one plane facing the same way (each wheel's
  parts are set a tenth of a pixel or more apart across it; the whole model is separated before it is cut).
- Clean textures at vanilla's density (one texel to a model pixel): boards in a few flat tones, each lit along its top
  and seamed along its bottom, a short grain line one shade off every seven texels, staggered board by board, and
  two-texel nail heads lit at their top left; iron in flat bands; hay in straight stripes. No random shade at any texel.

Everything is drawn here by code from fixed seeds; no Mojang texture is read, traced or copied.
"""
import hashlib
import math

import decor17_data as d17d
import decor18_data as d18d
import decor20_data as d20d
import flora_art as fa
import pumpkin_patch as pp
from flora_art import Sculpt, cube, pal, rotation, shade

MOD = "jugcraft"
FACINGS = {"north": 0, "east": 90, "south": 180, "west": 270}
ALL6 = ("north", "south", "east", "west", "up", "down")
faces = d18d.faces
shifted = d18d.shifted

# Palettes, darkest first. The woods are weathered greys and browns, the wagon's sides barn red; iron is a cool grey.
WOOD = pal("3a2c1e", "56422c", "6f5739", "896d48", "a2875c")
GREY_WOOD = pal("3c3832", "56514a", "6e6860", "88827a", "a39d94")
BARN_RED = pal("4a1913", "6c241b", "8a3223", "a4432f", "bb5a40")
IRON = pal("2a2a2e", "3e3e44", "56565e", "71717a", "8e8e98")
HAY = pal("7a5e1c", "9e7c26", "bea034", "d8bc4c", "ecd674")
GRIP = pal("3a2616", "54381f", "6e4c2c", "88623a")


def rid(path):
    return path if ":" in path else f"{MOD}:{path}"


# ---------------------------------------------------------------- painters
# Each painter fits itself to the piece it paints: boards, stripes and grain run along its longer side, so a face of any
# box, long one way or the other, shows its grain the right way (see `panel`).

def planks(palette=WOOD, board=4, nails=True):
    """Boards side by side running along the piece: each board one tone of the wood (by turns), lit along its upper edge
    and seamed along its lower one; a grain line one shade off every seven texels along it, staggered board by board;
    and a two-texel iron nail head, lit at its top left, near each end of each board."""
    def paint(p):
        along_x = p.w >= p.h
        span, across = (p.w, p.h) if along_x else (p.h, p.w)
        size = min(board, across)
        for a in range(across):
            b, w = divmod(a, size)
            base = (2, 3, 2, 1)[b % 4]
            for s in range(span):
                if size > 1 and w == size - 1:
                    k = 0
                elif w == 0:
                    k = base + 1
                elif (s + b * 3) % 7 == 0:
                    k = base - 1
                else:
                    k = base
                x, y = (s, a) if along_x else (a, s)
                p.put(x, y, shade(palette, k))
        if nails and size >= 4 and span >= 8:
            for b in range(across // size):
                for s in (1, span - 3):
                    for (ds, dw), k in (((0, 0), 4), ((1, 0), 3), ((0, 1), 3), ((1, 1), 1)):
                        x, y = (s + ds, b * size + 1 + dw) if along_x else (b * size + 1 + dw, s + ds)
                        p.put(x, y, shade(IRON, k))
    paint.tag = repr(("planks", palette, board, nails))
    return paint


def flat(palette):
    """A flat strap or band: its middle tone, lit along its upper (or left) edge and shaded along the other, across its
    shorter side."""
    def paint(p):
        along_x = p.w >= p.h
        across = p.h if along_x else p.w
        for y in range(p.h):
            for x in range(p.w):
                a = y if along_x else x
                k = 3 if (a == 0 and across > 1) else 1 if (a == across - 1 and across > 2) else 2
                p.put(x, y, shade(palette, k))
    paint.tag = repr(("flat", palette))
    return paint


def lengthwise(palette):
    """Sawn or turned wood (a spoke, a pole, a stake, a beam): stripes two texels wide along it in two tones, lit on the
    upper (or left) edge."""
    def paint(p):
        along_x = p.w >= p.h
        across = p.h if along_x else p.w
        for y in range(p.h):
            for x in range(p.w):
                a = y if along_x else x
                k = 3 if a == 0 else 1 if (a == across - 1 and across > 2) else (2, 2, 1, 1)[a % 4]
                p.put(x, y, shade(palette, k))
    paint.tag = repr(("lengthwise", palette))
    return paint


def hub(palette=IRON):
    """An iron hub's face: a flat cap with a lit rim at the top left and a two-texel bolt in the middle."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                k = 3 if (x == 0 or y == 0) else 1 if (x == p.w - 1 or y == p.h - 1) else 2
                p.put(x, y, shade(palette, k))
        if p.w >= 4 and p.h >= 4:
            cx, cy = p.w // 2 - 1, p.h // 2 - 1
            for (dx, dy), k in (((0, 0), 4), ((1, 0), 3), ((0, 1), 3), ((1, 1), 1)):
                p.put(cx + dx, cy + dy, shade(palette, k))
    paint.tag = repr(("hub", palette))
    return paint


def hay(palette=HAY):
    """Loose hay: straight stalks two texels wide in a fixed run of tones, along the piece's longer side."""
    def paint(p):
        along_x = p.w >= p.h
        for y in range(p.h):
            for x in range(p.w):
                a, s = (y, x) if along_x else (x, y)
                k = (3, 4, 3, 2, 3, 2)[(a // 2) % 6] - (1 if (s + a // 2) % 5 == 0 else 0)
                p.put(x, y, shade(palette, k))
    paint.tag = repr(("hay", palette))
    return paint


def slatted(palette=BARN_RED, slat=3, gap=1):
    """A painted slatted side: slats `slat` texels high with a dark gap between, each lit along its top and shaded along
    its bottom row."""
    def paint(p):
        for y in range(p.h):
            within = y % (slat + gap)
            k = 0 if within >= slat else 3 if within == 0 else 1 if within == slat - 1 else 2
            for x in range(p.w):
                p.put(x, y, shade(palette, k))
    paint.tag = repr(("slatted", palette, slat, gap))
    return paint


def wrapped(palette=GRIP):
    """A grip wrapped in cord: even diagonal ridges, three texels apart."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                p.put(x, y, shade(palette, 3 if (x + y) % 3 == 0 else 1 if (x + y) % 3 == 2 else 2))
    paint.tag = repr(("wrapped", palette))
    return paint


# ---------------------------------------------------------------- boxes

def face_size(side, lo, hi):
    """A side's width and height in model pixels, as a block model lays its picture: north and south show x by y, east
    and west z by y, up and down x by z."""
    x, y, z = (hi[k] - lo[k] for k in range(3))
    return {"north": (x, y), "south": (x, y), "east": (z, y), "west": (z, y), "up": (x, z), "down": (x, z)}[side]


def panel(sc, name, painter, lo, hi, sides=ALL6, rot=None, **over):
    """A box whose every side is painted for it: a piece of `painter` as many texels as the side is pixels (one texel
    to a model pixel, vanilla's density), so the grain runs along each side's longer edge and nothing is squeezed.
    `over` gives a side its own painter instead (e.g. an end grain)."""
    uvs = {}
    for side in sides:
        w, h = face_size(side, lo, hi)
        pw, ph = max(1, int(math.ceil(w - 1e-6))), max(1, int(math.ceil(h - 1e-6)))
        paint = over.get(side, painter)
        # Sides painted alike (the same painter with the same settings) and of one size share a piece.
        kind = hashlib.sha1(paint.tag.encode()).hexdigest()[:8]
        uvs[side] = sc.piece(f"{name}_{kind}_{pw}x{ph}", pw, ph, paint)
    return cube(lo, hi, uvs, rot)


def walls(sc, name, painter, x0, z0, x1, z1, t, y0, y1, top=None, inside=True):
    """A hollow wall round x0..x1 by z0..z1, `t` thick, from y0 to y1: the north and south walls run the full width and
    the east and west ones fit between them, each painted for its own sides (`panel`), the insides drawn too."""
    cap = {"up": top} if top else {}
    out = []
    for z_lo, z_hi in ((z0, z0 + t), (z1 - t, z1)):
        out.append(panel(sc, name, painter, (x0, y0, z_lo), (x1, y1, z_hi), **cap))
    for x_lo, x_hi in ((x0, x0 + t), (x1 - t, x1)):
        out.append(panel(sc, name, painter, (x_lo, y0, z0 + t), (x_hi, y1, z1 - t), **cap))
    return out


# ---------------------------------------------------------------- shared parts

def wheel(sc, key, cy, cz, r, x0, x1, rim_t=1.0):
    """A spoked wheel standing across x (x0..x1), its axle at (cy, cz), `r` from the axle to the rim's flat outside: an
    iron tyre of eight segments (four straight, four turned 45 degrees about the axle), eight wooden spokes (two straight
    through the hub, two turned 45 degrees) and an iron hub standing proud of the wheel on both sides. Each kind of part
    is set a little in from the last across x, so none shares a plane with another where they cross."""
    side = 2 * r * math.tan(math.radians(22.5)) + rim_t
    mid = (x0 + x1) / 2
    els = []
    top = ((cy + r - rim_t, cz - side / 2), (cy + r, cz + side / 2))
    bottom = ((cy - r, cz - side / 2), (cy - r + rim_t, cz + side / 2))
    for (y0, z0), (y1, z1) in (top, bottom):
        els.append(panel(sc, f"{key}_tyre", flat(IRON), (x0, y0, z0), (x1, y1, z1)))
    for z0, z1 in ((cz - r, cz - r + rim_t), (cz + r - rim_t, cz + r)):
        els.append(panel(sc, f"{key}_tyre", flat(IRON), (x0, cy - side / 2, z0), (x1, cy + side / 2, z1)))
    for (y0, z0), (y1, z1) in (top, bottom):
        for angle in (45, -45):
            els.append(panel(sc, f"{key}_tyre", flat(IRON), (x0 + 0.1, y0, z0), (x1 - 0.1, y1, z1),
                             rot=rotation((mid, cy, cz), "x", angle)))
    inner = r - rim_t + 0.2
    els.append(panel(sc, f"{key}_spoke", lengthwise(WOOD), (x0 + 0.3, cy - inner, cz - 0.5), (x1 - 0.3, cy + inner, cz + 0.5)))
    els.append(panel(sc, f"{key}_spoke", lengthwise(WOOD), (x0 + 0.3, cy - 0.5, cz - inner), (x1 - 0.3, cy + 0.5, cz + inner)))
    for angle in (45, -45):
        els.append(panel(sc, f"{key}_spoke", lengthwise(WOOD), (x0 + 0.4, cy - inner, cz - 0.45), (x1 - 0.4, cy + inner, cz + 0.45),
                         rot=rotation((mid, cy, cz), "x", angle)))
    els.append(panel(sc, f"{key}_hub", hub(), (x0 - 0.5, cy - 1.25, cz - 1.25), (x1 + 0.5, cy + 1.25, cz + 1.25)))
    return els


def cut(elements, size):
    """The prop's part models, in part order (MultiDecorationBlock's), from a model drawn whole in its frame: separated
    first (d20d.separated), then each plain box cut into the blocks it crosses (decor17_data.cell_part, each piece keeping
    its share of the picture) and each turned box given whole to the block its turn's middle lies in, moved into it."""
    elements = d20d.separated(elements)
    plain = [e for e in elements if "rotation" not in e]
    turned = [e for e in elements if "rotation" in e]
    parts = []
    for part in range(len(d17d.cells(size))):
        cell = d17d.frame_cell(size, part)
        mine = d17d.cell_part(d17d.pinned(plain), cell, size)
        for e in turned:
            centre = e["rotation"]["origin"]
            if all(cell[k] * 16 <= centre[k] < cell[k] * 16 + 16 for k in range(3)):
                lo = [e["from"][k] - cell[k] * 16 for k in range(3)]
                hi = [e["to"][k] - cell[k] * 16 for k in range(3)]
                assert min(lo) >= -16 and max(hi) <= 32, f"a turned box reaches out of its block: {e}"
                mine.append(shifted([e], -cell[0] * 16, -cell[1] * 16, -cell[2] * 16)[0])
        parts.append(mine)
    return parts


# ---------------------------------------------------------------- the Pumpkin Wheelbarrow

def wheelbarrow():
    """The Pumpkin Wheelbarrow, one block across and two long, facing north: an iron-shod wooden wheel at the front on an
    axle between two long handle poles, a flared wooden tray resting on the poles, two legs under them, and cord-wrapped
    grips at the back."""
    spec = pp.DISPLAYS["wheelbarrow"]
    sc = Sculpt(spec["block"], 3101, 128)
    el = wheel(sc, "wheel", 5.25, 6.5, 5.25, 7.0, 9.0)
    # The axle through the hub, its ends set into the poles.
    el.append(panel(sc, "axle", flat(IRON), (5.2, 4.75, 6.0), (10.8, 5.75, 7.0)))
    for x0, x1 in ((5.0, 6.5), (9.5, 11.0)):
        # The handle poles, front to back, and the grips over their back ends.
        el.append(panel(sc, "pole", lengthwise(WOOD), (x0, 4.5, 4.0), (x1, 6.0, 31.5)))
        el.append(panel(sc, "grip", wrapped(), (x0 - 0.2, 4.3, 27.5), (x1 + 0.2, 6.2, 31.7)))
        # A leg under each pole, ahead of the grips.
        el.append(panel(sc, "leg", lengthwise(WOOD), (x0 + 0.25, 0.0, 20.0), (x1 - 0.25, 4.5, 21.5)))
    # The tray: a floor on the poles and two courses of wall, the upper flared out, under a rolled iron rim.
    el.append(panel(sc, "floor", planks(WOOD, board=3), (3.5, 6.0, 12.5), (12.5, 7.5, 25.5)))
    el += walls(sc, "tray", planks(WOOD, board=2, nails=False), 2.5, 11.5, 13.5, 26.5, 1.0, 7.5, 9.5)
    el += walls(sc, "tray", planks(WOOD, board=2, nails=False), 2.0, 11.0, 14.0, 27.0, 1.0, 9.5, 11.5)
    el += walls(sc, "rim", flat(IRON), 1.75, 10.75, 14.25, 27.25, 1.25, 11.5, 12.25)
    sc.models["whole"] = el
    return sc


# ---------------------------------------------------------------- the Pumpkin Wagon

def wagon():
    """The Pumpkin Wagon, two blocks across, two high and four long, facing north: two axles under bolsters and a reach
    pole, four iron-shod spoked wheels (the back pair taller), a plank bed with barn-red slatted sides held by stakes, and
    a floor of loose hay for the produce."""
    spec = pp.DISPLAYS["wagon"]
    sc = Sculpt(spec["block"], 3102, 128)
    el = []
    for x0, x1 in ((0.5, 2.0), (30.0, 31.5)):
        el += wheel(sc, "front", 6.25, 8.0, 6.25, x0, x1)
        el += wheel(sc, "back", 7.25, 56.0, 7.25, x0, x1, rim_t=1.25)
    # The axles, their ends in the hubs; the bolsters across them; the reach pole joining them under the bed.
    el.append(panel(sc, "axle", flat(IRON), (1.0, 5.75, 7.4), (31.0, 6.75, 8.6)))
    el.append(panel(sc, "axle", flat(IRON), (1.0, 6.75, 55.4), (31.0, 7.75, 56.6)))
    el.append(panel(sc, "beam", lengthwise(GREY_WOOD), (3.0, 6.75, 6.5), (29.0, 10.0, 9.5)))
    el.append(panel(sc, "beam", lengthwise(GREY_WOOD), (3.0, 7.75, 54.5), (29.0, 10.0, 57.5)))
    # The long parts run in two lengths that meet at the middle (z 32), each painted alike, so the texture holds them.
    for z0, z1 in ((9.5, 32.0), (32.0, 54.5)):
        el.append(panel(sc, "reach", lengthwise(GREY_WOOD), (15.0, 8.75, z0), (17.0, 10.0, z1)))
    # The bed: the floor, its slatted barn-red sides and ends capped along their tops, the hay on the floor.
    for z0, z1 in ((4.0, 32.0), (32.0, 60.0)):
        el.append(panel(sc, "floor", planks(WOOD, board=4), (3.0, 10.0, z0), (29.0, 11.5, z1)))
    el += [panel(sc, "side", slatted(), (3.0, 11.5, 4.0), (29.0, 19.5, 5.25), up=flat(BARN_RED)),
           panel(sc, "side", slatted(), (3.0, 11.5, 58.75), (29.0, 19.5, 60.0), up=flat(BARN_RED))]
    for z0, z1 in ((5.25, 32.0), (32.0, 58.75)):
        for x0, x1 in ((3.0, 4.25), (27.75, 29.0)):
            el.append(panel(sc, "side", slatted(), (x0, 11.5, z0), (x1, 19.5, z1), up=flat(BARN_RED)))
        el.append(panel(sc, "hay", hay(), (4.25, 11.5, z0), (27.75, 12.5, z1)))
    # Stakes outside the sides, standing a little proud of them, clear of the wheels.
    for z in (4.0, 18.0, 32.0, 46.0, 58.5):
        for x0, x1 in ((2.1, 3.0), (29.0, 29.9)):
            el.append(panel(sc, "stake", lengthwise(GREY_WOOD), (x0, 9.5, z), (x1, 21.0, z + 1.5)))
    sc.models["whole"] = el
    return sc


# ---------------------------------------------------------------- the Pumpkin Pyramid

def pyramid():
    """The Pumpkin Pyramid, three blocks square, facing north: three stepped tiers of boards, each with a lip round its
    top edge to keep the produce on, and corner posts at the bottom tier."""
    spec = pp.DISPLAYS["pyramid"]
    sc = Sculpt(spec["block"], 3103, 128)
    el = []
    for tier, lo, hi in ((1, (0.5, 0.0, 0.5), (47.5, 4.0, 47.5)), (2, (8.0, 4.0, 8.0), (40.0, 10.0, 40.0)),
                         (3, (16.0, 10.0, 16.0), (32.0, 16.0, 32.0))):
        boards = planks(GREY_WOOD, board=4)
        el.append(panel(sc, f"tier{tier}", planks(WOOD, board=3, nails=False), lo, hi, up=boards, down=boards))
        # The lip: a frame a pixel wide and three-quarters high round the tier's top edge.
        el += walls(sc, "lip", flat(WOOD), lo[0] - 0.25, lo[2] - 0.25, hi[0] + 0.25, hi[2] + 0.25, 1.0, hi[1], hi[1] + 0.75)
    for x in (0.0, 46.0):
        for z in (0.0, 46.0):
            el.append(panel(sc, "post", lengthwise(GREY_WOOD), (x, 0.0, z), (x + 2.0, 6.0, z + 2.0)))
    sc.models["whole"] = el
    return sc


# ---------------------------------------------------------------- building and files

class PlannedAtlas(fa.Atlas):
    """An atlas whose pieces are placed by plan: a first build only records each piece's size (`plan` None), then the
    pieces are packed in rows tallest first (so a long thin strip never sets a tall row's height), and a second build
    paints each at its planned place. The props' long boards and thin lips fill a 128 x 128 texture this way, where
    placing them in the order they are made would not."""

    def __init__(self, size, plan=None):
        super().__init__(size)
        self.plan = plan
        self.sizes = {}

    def piece(self, key, w, h, painter, turn=0):
        if self.plan is None:
            self.sizes.setdefault(key, (w, h))
            return (0.0, 0.0, 0.0, 0.0)
        if key in self.uvs:
            return self.uvs[key]
        x, y = self.plan[key]
        img = fa.Image.new("RGBA", (w, h), (0, 0, 0, 0))
        painter(fa.Px(img))
        self.img.alpha_composite(img, (x, y))
        unit = 16 / self.size
        self.uvs[key] = (x * unit, y * unit, (x + w) * unit, (y + h) * unit)
        return self.uvs[key]


def pack(sizes, size):
    """Places pieces of `sizes` ({key: (w, h)}) in rows, tallest first, a texel apart: {key: (x, y)}."""
    out, x, y, row = {}, 0, 0, 0
    for key, (w, h) in sorted(sizes.items(), key=lambda item: (-item[1][1], -item[1][0], item[0])):
        if x + w > size:
            x, y, row = 0, y + row + 1, 0
        if y + h > size:
            raise ValueError(f"texture full placing {key}")
        out[key] = (x, y)
        x += w + 1
        row = max(row, h)
    return out


def planned(builder, size=128):
    """`builder`'s Sculpt, its pieces packed by plan (PlannedAtlas)."""
    original = fa.Sculpt.__init__

    def first(self, name, seed, sculpt_size=fa.SIZE):
        original(self, name, seed, sculpt_size)
        self.atlas = PlannedAtlas(size)
    fa.Sculpt.__init__ = first
    try:
        dry = builder()
    finally:
        fa.Sculpt.__init__ = original
    plan = pack(dry.atlas.sizes, size)

    def second(self, name, seed, sculpt_size=fa.SIZE):
        original(self, name, seed, sculpt_size)
        self.atlas = PlannedAtlas(size, plan)
    fa.Sculpt.__init__ = second
    try:
        return builder()
    finally:
        fa.Sculpt.__init__ = original


_built = {}
BUILDERS = {"pumpkin_wheelbarrow": wheelbarrow, "pumpkin_wagon": wagon, "pumpkin_pyramid": pyramid}


def build(block):
    if block not in _built:
        sc = planned(BUILDERS[block])
        size = next(spec["size"] for spec in pp.DISPLAYS.values() if spec["block"] == block)
        whole = sc.models["whole"]
        for part, elements in enumerate(cut(whole, size)):
            sc.models[f"{block}_{part}"] = elements
        # The item: the whole prop, made small enough that every box lies inside a block model's reach, about its middle.
        scale = 1.0 / max(1, (max(size) + 1) // 2)
        sc.models["item"] = fa.transformed(d17d.pinned(whole), scale, (8 - 8 * size[0] * scale, 0, 8 - 8 * size[2] * scale), (0, 0, 0))
        sc.item_scale = scale
        _built[block] = sc
    return _built[block]


def item_display(block):
    """The item's display, its prop shown about as big as a block's in the hand and on the slot."""
    s = build(block).item_scale
    size = next(spec["size"] for spec in pp.DISPLAYS.values() if spec["block"] == block)
    fit = s * max(size)
    gui = round(0.9 / fit, 3)
    return {"gui": {"rotation": [25, 225, 0], "translation": [0, -1, 0], "scale": [gui] * 3},
            "ground": {"rotation": [0, 0, 0], "translation": [0, 2, 0], "scale": [round(gui * 0.5, 3)] * 3},
            "fixed": {"rotation": [0, 0, 0], "translation": [0, -2, 0], "scale": [gui] * 3},
            "thirdperson_righthand": {"rotation": [0, 45, 0], "translation": [0, 2, 0], "scale": [round(gui * 0.6, 3)] * 3},
            "firstperson_righthand": {"rotation": [0, 45, 0], "translation": [0, 2, 0], "scale": [round(gui * 0.6, 3)] * 3}}


def _image_of(name):
    try:
        return build(name).atlas.img
    except KeyError:
        return None


def assets(root, write, lang):
    write = fa.closing_writer(write, _image_of)
    models = root / "models" / "block"
    states = root / "blockstates"
    items = root / "models" / "item"
    for spec in pp.DISPLAYS.values():
        block = spec["block"]
        lang[f"block.{MOD}.{block}"] = spec["display"]
        sc = build(block)
        n = len(d17d.cells(spec["size"]))
        for part in range(n):
            write(models / f"{block}_{part}.json", fa.model(block, sc.models[f"{block}_{part}"]))
        write(states / f"{block}.json", {"variants": {
            f"facing={f},lit={l},part={p}": {"model": rid(f"block/{block}_{p}"), **({"y": FACINGS[f]} if FACINGS[f] else {})}
            for f in FACINGS for l in ("false", "true") for p in range(n)}})
        write(items / f"{block}.json", fa.model(block, sc.models["item"], item_display(block)))
        write(root / "items" / f"{block}.json", {"model": {"type": "minecraft:model", "model": rid(f"item/{block}")}})
    lang["message.jugcraft.produce_display.full"] = "It's full"


def loot(out, write):
    """Each display drops once, from its first block."""
    for block in pp.display_blocks():
        write(out / f"{block}.json", {"type": "minecraft:block", "pools": [{
            "condition": {"type": "minecraft:all_of", "terms": [{"type": "minecraft:survives_explosion"},
                                                                {"type": "minecraft:match_block", "blocks": rid(block), "state": {"part": "0"}}]},
            "entries": [{"type": "minecraft:item", "name": rid(block)}], "rolls": 1}], "random_sequence": rid(f"blocks/{block}")})


def tags(tags):
    for block in pp.display_blocks():
        tags.add("block", "minecraft:mineable/axe", rid(block))


def textures():
    return {("block", block): build(block).atlas.img for block in pp.display_blocks()}

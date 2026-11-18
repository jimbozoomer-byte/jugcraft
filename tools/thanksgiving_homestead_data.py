"""Generated assets for the Thanksgiving homestead and feast room (tools/thanksgiving_homestead.py;
docs/features/thanksgiving-homestead.md).

Built from boxes at real size (a model pixel is 6.25 cm), one texel to a model pixel, in the manner of the Thanksgiving
decorations (tools/thanksgiving_decor_data.py) and the Pumpkin Patch Market (tools/pumpkin_patch_data.py), whose panel,
plank, strap and turned-wood painters, wax, flame, china and linen palettes and candle they share, so the homestead's
furniture stands beside the feast table in one hand. Each piece is one packed texture (block/<piece>), its faces each
painted for its own size (pumpkin_patch_data.panel), flat in three to five tones of a material, lit from the top left,
patterns and never noise.

How they are built (docs/ART_DIRECTION.md):
- Joinery is geometry: a settle's back and a dresser's carcass are frames of stiles and rails standing proud of fielded
  panels set back in them, drawers sit in their frame, carving is cut into the outline (the dresser's arcaded apron, its
  dentils, the settle's crest) as well as drawn on a rail (lunettes).
- What a shelf holds is modelled: plates are discs stood on edge and leant back on the boards, chargers, tankards, jugs
  and crocks are turned (two crossed boxes a ring), lidded or open and lined inside.
- Hollow vessels (the basin, the chamberstick's pan, the pitcher's mouth, a splash cup) are rings of walls with stepped
  corners round an open middle, lined inside, on a floor (`ring`); nothing is a solid box with its top left out.
- Two-block pieces are modelled whole in their frame and cut into their blocks (pumpkin_patch_data.cut), separated before
  the cut, so no two parts share a plane across a seam.

Muted seventeenth-century colours: oak and walnut, russet and dun wool, black felt, unbleached linen, redware and
salt-glazed stoneware, pewter. The pilgrim hat has no buckle (buckled hats on 1620s colonists are a later myth): a plain
leather band.

Everything is drawn here by code; no Mojang texture is read, traced or copied, and nothing from the owner library is
used (its catalog holds no settle, dresser, pewter, sampler, peg rail or clothing at this scale: the record says what was
searched).
"""
import math

import decor17_data as d17d
import decor20_data as d20d
import flora_art as fa
import pumpkin_patch_data as ppd
import thanksgiving_decor_data as tdd
import thanksgiving_homestead as th
from flora_art import pal, shade
from harvest_festival_data import APPLE, ASPEN, LEAF, MAPLE, TWINE, WHEAT, crossed_y, crossed_z
from haunted_midway_data import PUMPKIN, STEM
from pumpkin_patch_data import IRON, flat, lengthwise, panel, planks

MOD = "jugcraft"
FACINGS = {"north": 0, "east": 90, "south": 180, "west": 270}
ALL6 = ("north", "south", "east", "west", "up", "down")

# Palettes, darkest first.
OAK = pal("33200f", "4a3018", "634222", "7c552d", "966a3a")             # aged colonial oak
WALNUT = tdd.WALNUT
RUSSET = pal("4a1a0e", "662614", "82341c", "9c4626", "b45c34")          # madder-dyed wool
DUN = pal("5e4e36", "7a6848", "96825c", "ae9a72")                       # undyed sheep's grey-brown
FELT = pal("17151a", "232026", "302c33", "3e3a42")                      # black felt, not pure black
HATBAND = pal("3c2a18", "56402a", "6e553a")                             # plain leather band, no buckle
LINEN = tdd.LINEN
PEWTER = pal("4e5256", "686c70", "84888a", "a0a2a2", "bcbcb8", "d8d8d2")
REDWARE = pal("4e1c0c", "702a12", "90401c", "ac5a2a", "c47a40")        # lead-glazed earthenware
STONEWARE = pal("6e665a", "8c8474", "aaa290", "c4bca8")                 # salt-glazed grey
STONEWARE_DIP = pal("4a2e18", "6a4428")                                  # its brown-dipped neck
CHINA, CHINA_BAND = tdd.CHINA, tdd.CHINA_BAND
WAX, SHADOW = tdd.WAX, tdd.SHADOW
OLIVE = pal("3a4220", "4e5a2a", "667236")                                # sampler greens
GOLD = ASPEN


def rid(path):
    return path if ":" in path else f"{MOD}:{path}"


# ---------------------------------------------------------------- painters

def tagged(paint, *tag):
    paint.tag = repr(tag)
    return paint


def fielded(palette):
    """A raised (fielded) panel seen face on: a bevel a texel wide round it, lit along its top and left, shaded along its
    bottom and right, and the field inside it its own tone with a grain line one shade off every third column."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                if p.w > 2 and p.h > 2 and (y == 0 or x == 0):
                    k = 3
                elif p.w > 2 and p.h > 2 and (y == p.h - 1 or x == p.w - 1):
                    k = 1
                else:
                    k = 1 if x % 3 == 2 and 1 < y < p.h - 2 else 2
                p.put(x, y, shade(palette, k))
    return tagged(paint, "fielded", palette)


def lunettes(palette):
    """A carved rail: a row of half-round lunettes four texels apart cut into it, each an arch two texels high in
    shadow with its crown lit, between flat bands along its top and bottom."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                k = 2
                if p.h >= 3:
                    a = x % 4
                    if y == 0:
                        k = 3
                    elif y == p.h - 1:
                        k = 1
                    elif y == 1:
                        k = 0 if a in (1, 2) else 3 if a == 0 else 2
                    else:
                        k = 0 if a in (0, 3) else 1
                p.put(x, y, shade(palette, k))
    return tagged(paint, "lunettes", palette)


def boards_up(palette):
    """Upright boards (a back or a rack's backboards): boards four texels wide running up the piece, each a tone of the
    wood by turns, lit at its left column and seamed at its right, with a grain line one shade off every seventh row."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                b, w = divmod(x, 4)
                base = (2, 3, 2, 1)[b % 4]
                k = 1 if w == 3 else base + 1 if w == 0 else base - 1 if (y + b * 3) % 7 == 0 else base
                p.put(x, y, shade(palette, max(1, min(len(palette) - 1, k))))
    return tagged(paint, "boards_up", palette)


def wool(palette, piped=True):
    """Woven wool (a squab, a cushion): a flat fill of its own tone, lit along its top row, a piped edge along its bottom
    row a tone darker (`piped`), and button tufts a tone deeper on a lattice every four texels, each row of them two
    along from the last."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                if piped and y == p.h - 1 and p.h > 2:
                    k = 1
                elif y == 0 and p.h > 2:
                    k = 3
                elif p.w > 3 and p.h > 3 and y % 4 == 2 and (x + 2 * (y // 4)) % 4 == 1:
                    k = 1
                else:
                    k = 2
                p.put(x, y, shade(palette, k))
    return tagged(paint, "wool", palette, piped)


def folds(palette):
    """Hanging cloth seen face on: folds running down it, a lit ridge, two texels of its own tone and a crease in
    shadow, every four texels; its hem row a tone darker."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                k = 1 if y == p.h - 1 and p.h > 2 else (3, 2, 2, 1)[x % 4]
                p.put(x, y, shade(palette, k))
    return tagged(paint, "folds", palette)


def metal(palette, glint=True):
    """Pewter or tin: a flat fill in its middle tone, lit along its top row and left column, shaded along its bottom
    row, with a glint at its top left (`glint`)."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                if glint and (x, y) == (1, 0) and p.w > 2:
                    k = len(palette) - 1
                elif y == 0 or (x == 0 and p.w > 2):
                    k = 3
                elif y == p.h - 1 and p.h > 2:
                    k = 1
                else:
                    k = 2
                p.put(x, y, shade(palette, k))
    return tagged(paint, "metal", palette, glint)


def octagon(d, c):
    """Whether texel (x, y) of a d x d picture lies in the octagon two crossed boxes `d` across, their corners `c` in,
    cover (crossed_y, crossed_z)."""
    def inside(x, y):
        return 0 <= x < d and 0 <= y < d and (min(x, d - 1 - x) >= c or min(y, d - 1 - y) >= c)
    return inside


def plate_face(palette, band=None, c=1):
    """A plate seen face on, one picture across the whole disc (`disc`): its rim the outermost texel round the octagon,
    lit on its upper left and shaded on its lower right (in `band`, a coloured slip band, where given), its well a
    tone deeper inside it, and a glint on the rim at the upper left."""
    def paint(p):
        d = p.w
        inside = octagon(d, c)
        for y in range(p.h):
            for x in range(p.w):
                if not inside(x, y):
                    p.put(x, y, shade(palette, 1))
                    continue
                rim = not all(inside(x + dx, y + dy) for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)))
                if rim:
                    upper = x + y < d - 1
                    c_ = shade(band, 2 if upper else 1) if band is not None else shade(palette, 3 if upper else 1)
                else:
                    c_ = shade(palette, 2)
                p.put(x, y, c_)
        for x, y in ((c, 1), (1, c)):
            if inside(x, y) and d >= 5:
                p.put(x, y, shade(palette, len(palette) - 1))
    return tagged(paint, "plate_face", palette, band, c)


def glaze(palette, dip=None, rows=0):
    """A glazed pot: its own tone in two bands, lit along its top row, its foot a tone darker; `dip`, a darker dip glaze
    over its top `rows` rows (a stoneware jug's brown neck)."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                if dip is not None and y < rows:
                    c = shade(dip, 1 if y == 0 else 0)
                elif y == 0 and p.h > 1:
                    c = shade(palette, 3)
                elif y == p.h - 1 and p.h > 2:
                    c = shade(palette, 1)
                else:
                    c = shade(palette, 2 if (x + y // 2) % 5 else 3)
                p.put(x, y, c)
    return tagged(paint, "glaze", palette, dip, rows)


def striped_linen(hanging=False):
    """A linen towel: cream linen in two tones by rows, lit at its top; hanging (`hanging`), a rust stripe two rows above
    its hem and the hem row in shadow."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                if hanging and y == p.h - 3:
                    c = shade(CHINA_BAND, 1)
                elif hanging and y == p.h - 1:
                    c = shade(LINEN, 1)
                else:
                    c = shade(LINEN, 3 if y == 0 else 2)
                p.put(x, y, c)
    return tagged(paint, "striped_linen", hanging)


def staves(palette):
    """A coopered vessel's staves: upright staves three texels wide in its own tone, each joint a column one tone
    darker, its top row lit."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                k = 3 if y == 0 and p.h > 1 else 1 if x % 3 == 2 else 2
                p.put(x, y, shade(palette, k))
    return tagged(paint, "staves", palette)


def solid(palette, k):
    return tdd.solid(palette, k)


def sprite(rows, colours):
    """An opaque picture from a pixel map, a texel to a model pixel (the sampler's stitches)."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                row = rows[min(y, len(rows) - 1)]
                p.put(x, y, colours[row[min(x, len(row) - 1)]])
    return tagged(paint, "map", rows, tuple(sorted((k, tuple(v)) for k, v in colours.items())))


# ---------------------------------------------------------------- shared parts

def ring(sc, name, outside, inside, cx, cz, d, y0, y1, t=1.0, c=None, top=None):
    """A hollow round wall `d` across about (cx, cz) from y0 to y1, `t` thick, its corners stepped `c` in (a fifth of its
    width, as crossed_y): four straight walls round an open middle, each painted `outside` without and `inside` within,
    and where the step is deeper than the wall a corner block closing it. No two of its boxes overlap; the middle stays
    open for a floor or contents."""
    r = d / 2
    c = round(d / 5 * 2) / 2 if c is None else c
    x0, z0, x1, z1 = cx - r, cz - r, cx + r, cz + r
    cap = {"up": top} if top else {}
    side = max(c, t)
    out = [panel(sc, name, outside, (x0 + c, y0, z0), (x1 - c, y1, z0 + t), south=inside, **cap),
           panel(sc, name, outside, (x0 + c, y0, z1 - t), (x1 - c, y1, z1), north=inside, **cap),
           panel(sc, name, outside, (x0, y0, z0 + side), (x0 + t, y1, z1 - side), east=inside, **cap),
           panel(sc, name, outside, (x1 - t, y0, z0 + side), (x1, y1, z1 - side), west=inside, **cap)]
    if c > t:
        corners = (((x0 + t, x0 + c), (z0 + t, z0 + c), ("east", "south")), ((x1 - c, x1 - t), (z0 + t, z0 + c), ("west", "south")),
                   ((x0 + t, x0 + c), (z1 - c, z1 - t), ("east", "north")), ((x1 - c, x1 - t), (z1 - c, z1 - t), ("west", "north")))
        for (a0, a1), (b0, b1), inner in corners:
            out.append(panel(sc, name, outside, (a0, y0, b0), (a1, y1, b1), **{s: inside for s in inner}, **cap))
    return out


def floor(sc, name, painter, cx, cz, d, y0, y1, t=1.0, c=None, **kw):
    """The floor inside a `ring` of the same size: a square from wall to wall (its corners hidden in the corner blocks)."""
    r = d / 2
    return [panel(sc, name, painter, (cx - r + t, y0, cz - r + t), (cx + r - t, y1, cz + r - t), **kw)]


def disc(sc, name, face, back, cx, cy, cz, d, z0, z1, rot=None):
    """A round thing `d` across standing face to the north, about (cx, cy), from z0 to z1: two boxes crossed (as
    crossed_z), their north faces cut from ONE picture of the whole disc (`face`, painted d x d texels), so a plate's
    rim runs round it once; `back` paints every other face."""
    r, c = d / 2, round(d / 5 * 2) / 2
    n = int(math.ceil(d - 1e-6))
    u0, v0, u1, v1 = sc.piece(f"{name}_disc_{face.tag}_{n}", n, n, face)
    su, sv = (u1 - u0) / n, (v1 - v0) / n
    out = []
    for x0, x1, y0, y1 in ((cx - r + c, cx + r - c, cy - r, cy + r), (cx - r, cx + r, cy - r + c, cy + r - c)):
        e = panel(sc, name, back, (x0, y0, z0), (x1, y1, z1), rot=rot)
        # Seen from the north the picture's left is the disc's east (high x), its top the disc's top.
        e["faces"]["north"]["uv"] = [round(u0 + (cx + r - x1) * su, 4), round(v0 + (cy + r - y1) * sv, 4),
                                     round(u0 + (cx + r - x0) * su, 4), round(v0 + (cy + r - y0) * sv, 4)]
        out.append(e)
    return out


def plate(sc, name, cx, cy, cz, d, face, back, thick=0.5, lean=22.5):
    """A plate `d` across stood on edge, its face to the north, its middle at (cx, cy, cz), leant back `lean` degrees
    about its foot (so it rests on the boards behind it): a `disc` with rim and well painted across its face."""
    rot = fa.rotation((cx, cy - d / 2, cz), "x", lean)
    return disc(sc, name, face, back, cx, cy, cz, d, cz - thick / 2, cz + thick / 2, rot)


def mug(sc, name, cx, y0, cz, d, h, palette, handle="east", lid=False):
    """A turned mug or tankard on its foot at y0: a body two crossed boxes, its top a darker ring (open, its inside in
    shadow) or a domed lid with a thumbpiece (`lid`), and a strap handle on its `handle` side."""
    body = metal(palette) if palette is PEWTER else glaze(palette)
    if lid:
        el = crossed_y(sc, name, body, cx, cz, d, y0, y0 + h, up=metal(palette))
        el += crossed_y(sc, f"{name}_lid", metal(palette), cx, cz, max(1.0, d - 1.0), y0 + h, y0 + h + 0.5)
        el.append(panel(sc, f"{name}_thumb", flat(palette), (cx + d / 2 - 0.75, y0 + h + 0.25, cz - 0.25), (cx + d / 2, y0 + h + 0.75, cz + 0.25)))
    else:
        # Open: a solid body to under the rim, its top (the inside's floor) in shadow, and a thin ring of rim round it.
        el = crossed_y(sc, name, body, cx, cz, d, y0, y0 + h - 0.75, up=solid(palette, 0))
        el += ring(sc, f"{name}_rim", body, solid(palette, 1), cx, cz, d, y0 + h - 0.75, y0 + h, t=0.5)
    r = d / 2
    strap = flat(palette)
    if handle == "east":
        near, far = cx + r, cx + r + 0.5
        grip = (cx + r + 0.5, cx + r + 1.0)
    else:
        near, far = cx - r - 0.5, cx - r
        grip = (cx - r - 1.0, cx - r - 0.5)
    lo, hi = min(near, far), max(near, far)
    el += [panel(sc, f"{name}_handle", strap, (grip[0], y0 + 0.75, cz - 0.25), (grip[1], y0 + h - 0.25, cz + 0.25)),
           panel(sc, f"{name}_handle", strap, (lo, y0 + h - 0.75, cz - 0.25), (hi, y0 + h - 0.25, cz + 0.25)),
           panel(sc, f"{name}_handle", strap, (lo, y0 + 0.75, cz - 0.25), (hi, y0 + 1.25, cz + 0.25))]
    return el


# ---------------------------------------------------------------- the High-Backed Oak Settle

# The back's frame: its stiles' spans across (x), its rails' spans up (y), and the openings its fielded panels fill.
SETTLE_STILES = ((1.5, 3.0), (8.375, 9.875), (15.25, 16.75), (22.125, 23.625), (29.0, 30.5))
SETTLE_RAILS = ((7.5, 10.0), (16.5, 18.0), (23.5, 25.5))


def openings(stiles, rails):
    """The openings between a frame's stiles and rails, (x0, x1, y0, y1)."""
    return [(stiles[i][1], stiles[i + 1][0], rails[j][1], rails[j + 1][0]) for j in range(len(rails) - 1) for i in range(len(stiles) - 1)]


def frame_and_panels(sc, name, stiles, rails, z_frame, z_panel, z_back, face_side="north"):
    """A frame of stiles and rails `z_frame` (z0, z1) deep, its rails full width and its stiles between them, and a
    fielded panel set back in each opening from `z_panel` to `z_back`."""
    rail, field = lengthwise(OAK), fielded(OAK)
    x0, x1 = stiles[0][0], stiles[-1][1]
    el = []
    for k, (y0, y1) in enumerate(rails):
        over = {face_side: lunettes(OAK)} if k == len(rails) - 1 and name == "settle" else {}
        el.append(panel(sc, f"{name}_rail", rail, (x0, y0, z_frame[0]), (x1, y1, z_frame[1]), **over))
    for j in range(len(rails) - 1):
        for sx0, sx1 in stiles:
            el.append(panel(sc, f"{name}_stile", rail, (sx0, rails[j][1], z_frame[0]), (sx1, rails[j + 1][0], z_frame[1])))
    for ox0, ox1, oy0, oy1 in openings(stiles, rails):
        el.append(panel(sc, f"{name}_panel", planks(OAK, board=4, nails=False), (ox0, oy0, z_panel), (ox1, oy1, z_back),
                        **{face_side: field}))
    return el


def oak_settle():
    """The High-Backed Oak Settle, facing north, two blocks across and two up: a box settle of panelled oak, its seat
    for two (a russet wool squab on each half) over a panelled base, its back of eight fielded panels in a frame of
    stiles and rails 1.6 m high under a carved top rail, a hood and a crest, between two winged ends that step down from
    the hood to the arms. Behind, it is boarded upright; beneath, its ends and plinth stand on the floor under a boarded
    bottom."""
    sc = fa.Sculpt("oak_settle", 4401, 128)
    board, rail = planks(OAK, board=4, nails=False), lengthwise(OAK)
    face = fielded(OAK)
    el = []
    # The ends: a tall board stepped back from the arm at the front to the hood at the back, fielded outside.
    for x0, x1, outside in ((0.0, 1.5, "west"), (30.5, 32.0, "east")):
        el.append(panel(sc, "end", board, (x0, 0.0, 3.5), (x1, 12.0, 15.5), **{outside: face}))
        el.append(panel(sc, "end", board, (x0, 12.0, 8.0), (x1, 16.0, 15.5)))
        el.append(panel(sc, "end", board, (x0, 16.0, 9.5), (x1, 25.5, 15.5), **{outside: face}))
        ax0, ax1 = (x0, x1 + 0.5) if x0 == 0.0 else (x0 - 0.5, x1)
        el.append(panel(sc, "arm", rail, (ax0, 12.0, 3.0), (ax1, 13.0, 8.0)))
    # Behind: upright boards from the floor to the hood.
    el.append(panel(sc, "backboards", boards_up(OAK), (1.5, 0.0, 14.0), (30.5, 25.5, 15.5), north=board))
    # The back: a frame of stiles and rails with eight fielded panels, the top rail carved with lunettes.
    el += frame_and_panels(sc, "settle", SETTLE_STILES, SETTLE_RAILS, (12.5, 14.0), 13.25, 14.0)
    # The seat: a board on a box base panelled in front, a plinth at its foot and a boarded bottom.
    el.append(panel(sc, "seat", board, (1.5, 6.5, 3.5), (30.5, 7.5, 14.0)))
    el += frame_and_panels(sc, "base", SETTLE_STILES, ((1.0, 2.0), (5.5, 6.5)), (3.75, 4.75), 4.25, 4.75)
    el.append(panel(sc, "plinth", rail, (1.5, 0.0, 3.5), (30.5, 1.0, 4.75)))
    el.append(panel(sc, "bottom", board, (1.5, 0.5, 4.75), (30.5, 1.0, 14.0)))
    # Two squabs of russet wool, one a seat, plumped in the middle.
    for x0, x1 in ((1.75, 15.75), (16.25, 30.25)):
        el.append(panel(sc, "squab", wool(RUSSET), (x0, 7.5, 4.0), (x1, 8.25, 12.25)))
        el.append(panel(sc, "squab_top", wool(RUSSET, piped=False), (x0 + 0.75, 8.25, 4.75), (x1 - 0.75, 8.5, 11.5)))
    # The hood over the back, a crown moulding on it and a carved crest at its middle.
    el.append(panel(sc, "hood", board, (0.0, 25.5, 10.0), (32.0, 26.5, 15.75)))
    el.append(panel(sc, "crown", rail, (0.5, 26.5, 10.5), (31.5, 27.5, 15.5)))
    el.append(panel(sc, "crest", rail, (11.0, 27.5, 11.5), (21.0, 28.5, 14.5), north=lunettes(OAK), south=lunettes(OAK)))
    el.append(panel(sc, "crest_top", rail, (13.5, 28.5, 12.0), (18.5, 29.5, 14.0)))
    sc.models["whole"] = el
    return sc



# ---------------------------------------------------------------- the Carved Dish Dresser

def turned_leg(sc, name, x0, x1, z0, z1, y0, y1, bulb):
    """A turned leg from y0 to y1, its baluster bulb half a pixel wider all round from bulb[0] to bulb[1]."""
    rail = lengthwise(OAK)
    return [panel(sc, name, rail, (x0, y0, z0), (x1, bulb[0], z1)),
            panel(sc, f"{name}_bulb", rail, (x0 - 0.25, bulb[0], z0 - 0.25), (x1 + 0.25, bulb[1], z1 + 0.25)),
            panel(sc, name, rail, (x0, bulb[1], z0), (x1, y1, z1))]


def arcade(sc, x0, x1, y_top, z0, z1):
    """A carved apron between two legs, x0 to x1 under y_top: a board a pixel deep along its top and, hanging from it,
    a drop at each end and in the middle with a step either side of each, so the gaps between them read as arches."""
    rail = lengthwise(OAK)
    el = [panel(sc, "apron", rail, (x0, y_top - 1.0, z0), (x1, y_top, z1))]
    mid = (x0 + x1) / 2
    drops = ((x0, x0 + 1.0), (mid - 0.5, mid + 0.5), (x1 - 1.0, x1))
    for a, b in drops:
        el.append(panel(sc, "apron_drop", rail, (a, y_top - 2.25, z0), (b, y_top - 1.0, z1)))
        for sa, sb in ((a - 1.0, a), (b, b + 1.0)):
            if x0 <= sa and sb <= x1:
                el.append(panel(sc, "apron_step", rail, (sa, y_top - 1.5, z0), (sb, y_top - 1.0, z1)))
    return el


def charger_face(d=6):
    return plate_face(PEWTER, c=int(round(d / 5 * 2) / 2))


def creamware_face(d=4.5):
    return plate_face(CHINA, CHINA_BAND, c=int(round(d / 5 * 2) / 2))


def crock(sc, name, cx, cz, y0, d, h, lid=None, cloth=False):
    """A redware crock on its foot at y0: its body, a narrower shoulder and its rim; a wooden lid with a knob (`lid`) or
    a linen cloth tied over its mouth with twine (`cloth`)."""
    el = crossed_y(sc, name, glaze(REDWARE), cx, cz, d, y0, y0 + h - 0.5)
    el += crossed_y(sc, f"{name}_shoulder", glaze(REDWARE), cx, cz, d - 1.0, y0 + h - 0.5, y0 + h, up=solid(REDWARE, 1))
    if lid:
        el += crossed_y(sc, f"{name}_lid", planks(WALNUT, board=2, nails=False), cx, cz, d - 1.5, y0 + h, y0 + h + 0.5)
        el += crossed_y(sc, f"{name}_knob", lengthwise(WALNUT), cx, cz, 1.5, y0 + h + 0.5, y0 + h + 1.0)
    if cloth:
        el += crossed_y(sc, f"{name}_cloth", flat(LINEN), cx, cz, d, y0 + h - 0.75, y0 + h + 0.5)
        el += crossed_y(sc, f"{name}_tie", flat(TWINE), cx, cz, d + 0.25, y0 + h - 0.5, y0 + h - 0.125)
    return el


def jug(sc, name, cx, cz, y0, d, h, palette=STONEWARE, dip=STONEWARE_DIP):
    """A jug on its foot at y0: a round belly, a shoulder, a narrow neck dipped darker and an open rim round its mouth,
    its strap handle from the neck to the shoulder on the east."""
    body = glaze(palette)
    neck = glaze(palette, dip, 8)
    el = crossed_y(sc, name, body, cx, cz, d, y0, y0 + h * 0.55)
    el += crossed_y(sc, f"{name}_shoulder", body, cx, cz, d - 1.0, y0 + h * 0.55, y0 + h * 0.7)
    el += crossed_y(sc, f"{name}_neck", neck, cx, cz, 2.0, y0 + h * 0.7, y0 + h - 0.5, up=solid(SHADOW, 0))
    el += ring(sc, f"{name}_lip", neck, solid(dip, 0), cx, cz, 2.5, y0 + h - 0.5, y0 + h, t=0.5)
    strap = flat(dip)
    top, low = y0 + h - 1.0, y0 + h * 0.55
    el += [panel(sc, f"{name}_handle", strap, (cx + 1.0, top - 0.5, cz - 0.25), (cx + d / 2 + 0.5, top, cz + 0.25)),
           panel(sc, f"{name}_handle", strap, (cx + d / 2 + 0.5, low, cz - 0.25), (cx + d / 2 + 1.0, top, cz + 0.25))]
    return el


def porringer(sc, name, cx, cz, y0, d=3.0):
    """A pewter porringer: a shallow round bowl, lined, on a floor, with its flat pierced ear out to the east."""
    el = crossed_y(sc, name, metal(PEWTER), cx, cz, d, y0, y0 + 0.5)
    el += ring(sc, f"{name}_wall", metal(PEWTER), solid(PEWTER, 1), cx, cz, d, y0 + 0.5, y0 + 1.25, t=0.5)
    el.append(panel(sc, f"{name}_ear", metal(PEWTER, glint=False), (cx + d / 2, y0 + 0.75, cz - 0.75), (cx + d / 2 + 1.25, y0 + 1.25, cz + 0.75)))
    return el


def carved_dish_dresser():
    """The Carved Dish Dresser, facing north, two blocks across and two up: an oak pot-board dresser 2 m high. Its base:
    three drawers with turned knobs in a frame under the worktop, an arcaded apron carved below them between turned
    legs, and the pot board near the floor holding a lidded redware crock, a salt-glazed jug and a small crock tied
    over with linen. Its rack against the wall: upright backboards between shaped sides, two shelves with plate rails,
    under a dentilled cornice over a frieze carved with lunettes. On it: two pewter chargers stood on the worktop with a
    lidded tankard, a jug and a porringer before them; pewter and creamware plates stood on edge on the first shelf with
    a tankard between them; and creamware bowls and pewter cups along the top."""
    sc = fa.Sculpt("carved_dish_dresser", 4402, 128)
    board, rail = planks(OAK, board=4, nails=False), lengthwise(OAK)
    face = fielded(OAK)
    el = []
    # The base's sides and turned legs, and its back.
    for x0, x1, outside in ((0.5, 1.5, "west"), (30.5, 31.5, "east")):
        el.append(panel(sc, "side", board, (x0, 0.0, 8.5), (x1, 13.0, 16.0), **{outside: face}))
    for x0, x1 in ((0.5, 2.0), (30.0, 31.5)):
        el += turned_leg(sc, "leg", x0, x1, 7.0, 8.5, 0.0, 13.0, (3.0, 5.0))
    el += turned_leg(sc, "leg", 15.25, 16.75, 7.0, 8.5, 0.0, 9.0, (3.0, 5.0))
    el.append(panel(sc, "base_back", boards_up(OAK), (1.5, 1.5, 15.0), (30.5, 13.0, 16.0)))
    # The drawers in their frame: a top rail, a rail under them over a dust board, two dividers and three fronts,
    # each with a turned knob.
    el.append(panel(sc, "top_rail", rail, (2.0, 12.5, 7.5), (30.0, 13.0, 8.5)))
    el.append(panel(sc, "mid_rail", rail, (2.0, 9.0, 7.5), (15.25, 9.75, 8.5)))
    el.append(panel(sc, "mid_rail", rail, (16.75, 9.0, 7.5), (30.0, 9.75, 8.5)))
    el.append(panel(sc, "mid_rail", rail, (15.25, 9.0, 7.5), (16.75, 9.75, 8.5)))
    el.append(panel(sc, "dust_board", board, (1.5, 9.0, 8.5), (30.5, 9.75, 15.0)))
    for x0, x1 in ((10.5, 11.5), (20.5, 21.5)):
        el.append(panel(sc, "divider", rail, (x0, 9.75, 7.5), (x1, 12.5, 8.5)))
    for x0, x1 in ((2.0, 10.5), (11.5, 20.5), (21.5, 30.0)):
        el.append(panel(sc, "drawer", board, (x0, 9.75, 8.0), (x1, 12.5, 8.5), north=face))
        cx = (x0 + x1) / 2
        el.append(panel(sc, "knob_neck", lengthwise(WALNUT), (cx - 0.25, 10.875, 7.5), (cx + 0.25, 11.375, 8.0)))
        el.append(panel(sc, "knob", lengthwise(WALNUT), (cx - 0.5, 10.625, 7.0), (cx + 0.5, 11.625, 7.5)))
    # The arcaded apron under the drawers, between the legs.
    el += arcade(sc, 2.0, 15.25, 9.0, 7.5, 8.25)
    el += arcade(sc, 16.75, 30.0, 9.0, 7.5, 8.25)
    # The pot board and what stands on it.
    el.append(panel(sc, "pot_board", board, (1.5, 0.5, 8.5), (30.5, 1.5, 15.0)))
    el.append(panel(sc, "pot_board", board, (2.0, 0.5, 7.5), (15.25, 1.5, 8.5)))
    el.append(panel(sc, "pot_board", board, (16.75, 0.5, 7.5), (30.0, 1.5, 8.5)))
    el += crock(sc, "big_crock", 7.0, 11.75, 1.5, 6.0, 5.0, lid=True)
    el += jug(sc, "stone_jug", 19.5, 11.75, 1.5, 4.5, 6.5)
    el += crock(sc, "small_crock", 26.0, 12.0, 1.5, 3.5, 3.0, cloth=True)
    # The worktop.
    el.append(panel(sc, "worktop", board, (0.0, 13.0, 6.5), (32.0, 14.0, 16.0)))
    # The rack: shaped sides, upright backboards, two shelves with plate rails.
    for x0, x1, outside in ((0.5, 1.5, "west"), (30.5, 31.5, "east")):
        el.append(panel(sc, "rack_foot", board, (x0, 14.0, 10.5), (x1, 15.5, 16.0)))
        el.append(panel(sc, "rack_side", board, (x0, 15.5, 11.5), (x1, 30.0, 16.0)))
    el.append(panel(sc, "rack_back", boards_up(OAK), (1.5, 14.0, 15.0), (30.5, 30.0, 16.0)))
    for y in (19.75, 25.25):
        el.append(panel(sc, "shelf", board, (1.5, y, 11.5), (30.5, y + 0.75, 15.0), north=rail))
        el.append(panel(sc, "plate_rail", rail, (1.5, y + 1.25, 11.75), (30.5, y + 1.75, 12.25)))
    # The frieze, carved, its dentils and the cornice.
    el.append(panel(sc, "frieze", rail, (1.5, 28.75, 11.0), (30.5, 30.0, 11.75), north=lunettes(OAK)))
    for x in range(2, 30, 2):
        el.append(panel(sc, "dentil", rail, (x, 29.25, 10.5), (x + 1.0, 30.0, 11.0)))
    el.append(panel(sc, "cornice", board, (0.0, 30.0, 9.5), (32.0, 31.0, 16.0)))
    el.append(panel(sc, "crown", rail, (0.5, 31.0, 10.0), (31.5, 32.0, 16.0)))
    # On the worktop: two pewter chargers stood against the backboards, a lidded tankard, a jug and a porringer.
    pewter_back, china_back = flat(PEWTER), flat(CHINA)
    for cx in (5.5, 26.5):
        el += plate(sc, "charger", cx, 17.0, 12.35, 6.0, charger_face(), pewter_back)
    el += mug(sc, "tankard", 12.0, 14.0, 9.75, 2.5, 3.5, PEWTER, lid=True)
    el += jug(sc, "red_jug", 20.0, 9.75, 14.0, 3.0, 4.0, REDWARE, REDWARE)
    el += porringer(sc, "porringer", 16.0, 8.5, 14.0)
    # The first shelf: plates on edge, pewter and creamware by turns, and a tankard between the pairs.
    for cx, face_, back in ((4.75, charger_face(4.5), pewter_back), (10.25, creamware_face(), china_back),
                            (21.75, creamware_face(), china_back), (27.25, charger_face(4.5), pewter_back)):
        el += plate(sc, "plate", cx, 22.75, 12.85, 4.5, face_, back)
    el += mug(sc, "tankard", 16.0, 20.5, 13.0, 2.5, 3.0, PEWTER, lid=True)
    # The top shelf: creamware bowls and pewter cups.
    for cx in (6.0, 26.0):
        el += crossed_y(sc, "bowl", glaze(CHINA), cx, 13.25, 2.0, 26.0, 26.5)
        el += ring(sc, "bowl_rim", glaze(CHINA, CHINA_BAND, 1), solid(CHINA, 1), cx, 13.25, 3.0, 26.5, 27.5, t=0.5, top=flat(CHINA))
        el += floor(sc, "bowl_floor", solid(CHINA, 2), cx, 13.25, 3.0, 26.5, 26.75, t=0.5)
    for cx in (11.5, 20.5):
        el += mug(sc, "cup", cx, 26.0, 13.25, 2.0, 2.0, PEWTER, handle="east" if cx < 16 else "west")
    sc.models["whole"] = el
    return sc



def round_(sc, name, painter, cx, cz, d, y0, y1, **kw):
    """A round thing standing from y0 to y1 (crossed_y), or for one under 1.5 pixels across a single square box."""
    if d < 1.5:
        return [panel(sc, name, painter, (cx - d / 2, y0, cz - d / 2), (cx + d / 2, y1, cz + d / 2), **kw)]
    return crossed_y(sc, name, painter, cx, cz, d, y0, y1, **kw)


# ---------------------------------------------------------------- the Pilgrim Peg Rail

def pilgrim_peg_rail():
    """The Pilgrim Peg Rail, hung on a wall facing north: a moulded oak board the width of its block (rails side by side
    run on as one) with three turned Shaker pegs. On the left peg a linen coif hangs by its frill: a close cap seen from
    the front, open to show its lining, a turned-back frill round its face and two ties hanging. The middle peg is free.
    On the right peg a russet wool cloak hangs by its gathered hood: a long, wide fall from the shoulders to an uneven
    hem, in five folds standing at different depths, its linen ties in front; and on the same peg, over the hood, a
    tall-crowned black felt hat sits on its brim, a plain leather band round the crown (no buckle)."""
    sc = fa.Sculpt("pilgrim_peg_rail", 4403, 64)
    rail = lengthwise(OAK)
    el = [panel(sc, "board", planks(OAK, board=3, nails=False), (0.0, 9.0, 15.0), (16.0, 11.5, 16.0)),
          panel(sc, "cap", rail, (0.0, 11.5, 15.25), (16.0, 12.0, 16.0))]
    pegs = (3.5, 7.75, 12.0)
    for x in pegs:
        el.append(panel(sc, "peg", lengthwise(WALNUT), (x - 0.5, 9.75, 12.5), (x + 0.5, 10.75, 15.0)))
        el.append(panel(sc, "peg_head", lengthwise(WALNUT), (x - 0.75, 9.5, 11.75), (x + 0.75, 11.0, 12.5)))
    # The coif, hung by its frill under the left peg's head: the crown behind, its sides and top round an opening lined
    # in shadowed linen, the frill turned back round the face, two ties hanging from its lower corners.
    kx = pegs[0]
    linen, lining = folds(LINEN), solid(LINEN, 1)
    x0, x1 = kx - 1.5, kx + 1.5
    el += [panel(sc, "coif_crown", linen, (x0 + 0.25, 5.75, 13.75), (x1 - 0.25, 9.0, 14.5), north=lining),
           panel(sc, "coif_side", linen, (x0, 6.0, 12.25), (x0 + 0.5, 9.0, 13.75), east=lining),
           panel(sc, "coif_side", linen, (x1 - 0.5, 6.0, 12.25), (x1, 9.0, 13.75), west=lining),
           panel(sc, "coif_top", linen, (x0 + 0.5, 8.5, 12.25), (x1 - 0.5, 9.0, 13.75), down=lining),
           panel(sc, "coif_nape", linen, (x0 + 0.5, 5.75, 12.75), (x1 - 0.5, 6.25, 13.75), up=lining)]
    frill = flat(LINEN)
    el += [panel(sc, "coif_frill", frill, (x0 - 0.25, 9.0, 11.75), (x1 + 0.25, 9.5, 12.25)),
           panel(sc, "coif_frill", frill, (x0 - 0.25, 6.0, 11.75), (x0 + 0.25, 9.0, 12.25)),
           panel(sc, "coif_frill", frill, (x1 - 0.25, 6.0, 11.75), (x1 + 0.25, 9.0, 12.25))]
    for tx in (x0, x1 - 0.25):
        el.append(panel(sc, "coif_tie", flat(LINEN), (tx, 2.75, 12.0), (tx + 0.25, 6.0, 12.25)))
    # The cloak on the right peg: its hood gathered round the peg (either side of it and under it), its shoulders, then
    # five folds falling wide to an uneven hem, each a little in front of or behind its neighbours; its ties.
    cloak, edge = folds(RUSSET), flat(RUSSET)
    cx = pegs[2]
    el += [panel(sc, "hood", cloak, (cx - 1.75, 8.25, 12.5), (cx - 0.75, 10.75, 14.75), up=edge),
           panel(sc, "hood", cloak, (cx + 0.75, 8.25, 12.5), (cx + 1.75, 10.75, 14.75), up=edge),
           panel(sc, "hood", cloak, (cx - 2.25, 7.0, 12.5), (cx + 2.25, 8.25, 14.75), up=edge),
           panel(sc, "hood_back", cloak, (cx - 0.75, 9.0, 13.0), (cx + 0.75, 10.75, 15.0), up=edge)]
    fall = ((7.25, 9.0, 13.25, 1.0), (9.0, 10.5, 12.75, 0.5), (10.5, 12.25, 13.0, 0.75), (12.25, 13.75, 12.6, 0.25),
            (13.75, 15.75, 13.2, 1.25))
    for x0_, x1_, z0, hem in fall:
        el.append(panel(sc, "cloak_fold", cloak, (x0_, hem, z0), (x1_, 4.5, 15.5), up=edge, down=solid(RUSSET, 0)))
        a0, a1 = max(x0_, 9.0), min(x1_, 15.0)
        if a1 > a0:
            el.append(panel(sc, "cloak_fold", cloak, (a0, 4.5, z0 + 0.25), (a1, 7.0, 15.5), up=edge))
    for tx in (cx - 0.75, cx + 0.5):
        el.append(panel(sc, "cloak_tie", flat(LINEN), (tx, 4.0, 12.25), (tx + 0.25, 7.0, 12.5)))
    # The hat on the same peg, over the hood: a wide flat brim, a tall crown narrowing to a flat top, a plain band.
    hx, hz = cx, 11.6
    felt = flat(FELT)
    el += round_(sc, "brim", felt, hx, hz, 6.5, 11.0, 11.5, up=solid(FELT, 2), down=solid(FELT, 1))
    el += round_(sc, "crown", felt, hx, hz, 4.5, 11.5, 13.25)
    el += round_(sc, "band", flat(HATBAND), hx, hz, 4.75, 11.5, 12.25, up=solid(HATBAND, 1))
    el += round_(sc, "crown", felt, hx, hz, 4.0, 13.25, 14.5)
    el += round_(sc, "crown_top", felt, hx, hz, 3.5, 14.5, 15.0, up=solid(FELT, 3))
    sc.models["whole"] = el
    return sc


# ---------------------------------------------------------------- the Pewter Pitcher and Basin

def pewter_pitcher_and_basin():
    """The Pewter Pitcher and Basin, facing north: a wide footed pewter basin, its broad rim over a flaring bowl lined
    inside on its floor, and a pewter pitcher standing in it at the back: a round belly, a shoulder and a neck to an
    open mouth with a pinched spout to the front and a strap handle behind. A linen towel striped in rust hangs over
    the rim at the front right."""
    sc = fa.Sculpt("pewter_pitcher_and_basin", 4404, 64)
    pewter, inside = metal(PEWTER), solid(PEWTER, 1)
    bx, bz = 8.0, 8.5
    el = round_(sc, "foot", pewter, bx, bz, 7.0, 0.0, 0.75)
    el += ring(sc, "bowl", pewter, inside, bx, bz, 10.0, 0.75, 2.0, t=1.0)
    el += floor(sc, "bowl_floor", solid(PEWTER, 2), bx, bz, 10.0, 0.75, 1.25, t=1.0)
    el += ring(sc, "rim", pewter, inside, bx, bz, 12.0, 2.0, 3.0, t=1.5, top=metal(PEWTER, glint=False))
    # The pitcher.
    px, pz = 7.0, 9.75
    el += round_(sc, "pitcher_foot", pewter, px, pz, 3.0, 1.25, 1.75)
    el += round_(sc, "pitcher", pewter, px, pz, 4.5, 1.75, 5.25)
    el += round_(sc, "pitcher_shoulder", pewter, px, pz, 4.0, 5.25, 6.25)
    el += round_(sc, "pitcher_neck", pewter, px, pz, 3.0, 6.25, 7.75, up=solid(SHADOW, 1))
    el += ring(sc, "pitcher_lip", pewter, inside, px, pz, 3.5, 7.75, 8.5, t=0.5)
    el.append(panel(sc, "spout", pewter, (px - 0.75, 7.75, pz - 2.5), (px + 0.75, 8.25, pz - 1.75)))
    strap = flat(PEWTER)
    el += [panel(sc, "handle", strap, (px - 0.5, 7.0, pz + 1.5), (px + 0.5, 7.75, pz + 3.0)),
           panel(sc, "handle", strap, (px - 0.5, 3.75, pz + 3.0), (px + 0.5, 7.75, pz + 3.75)),
           panel(sc, "handle", strap, (px - 0.5, 3.5, pz + 2.25), (px + 0.5, 4.25, pz + 3.0))]
    # The towel over the rim.
    towel = striped_linen()
    el.append(panel(sc, "towel", towel, (9.0, 0.25, 1.9), (13.0, 3.35, 2.3), north=striped_linen(hanging=True)))
    el.append(panel(sc, "towel", towel, (9.0, 3.0, 2.3), (13.0, 3.35, 4.25)))
    sc.models["whole"] = el
    return sc


# ---------------------------------------------------------------- the Pewter Chamberstick

def pewter_chamberstick():
    """The Pewter Chamberstick, facing north: a round drip pan with a raised rim, lined, a socket stem with a drip collar
    holding a cream tallow candle with a run of wax down it, a finger ring to the east under a thumbpiece, and the
    snuffer, a plain cone on a thin straight handle, lying in the pan beside the socket with its handle over the rim.
    Lit (`lit`), the candle burns."""
    sc = fa.Sculpt("pewter_chamberstick", 4405, 64)
    pewter, inside = metal(PEWTER), solid(PEWTER, 1)
    el = round_(sc, "pan", pewter, 8.0, 8.0, 8.0, 0.0, 0.75, up=solid(PEWTER, 2))
    el += ring(sc, "pan_rim", pewter, inside, 8.0, 8.0, 8.0, 0.75, 1.75, t=0.75, top=metal(PEWTER, glint=False))
    el += round_(sc, "stem", pewter, 8.0, 8.0, 2.5, 0.75, 3.5)
    el += round_(sc, "collar", pewter, 8.0, 8.0, 4.0, 3.5, 4.0)
    el += round_(sc, "socket", pewter, 8.0, 8.0, 2.5, 4.0, 4.75)
    body, flame = tdd.candle(sc, 8.0, 8.0, 4.75, 4.5)
    el += body
    el.append(panel(sc, "drip", flat(WAX), (7.25, 6.25, 6.75), (7.75, 8.75, 7.0)))
    # The finger ring and the thumbpiece over it.
    for lo, hi in (((12.0, 0.75, 6.5), (15.0, 1.25, 7.25)), ((12.0, 0.75, 8.75), (15.0, 1.25, 9.5)), ((14.25, 0.75, 7.25), (15.0, 1.25, 8.75))):
        el.append(panel(sc, "ring", pewter, lo, hi))
    el.append(panel(sc, "bracket", pewter, (11.25, 1.75, 7.5), (12.0, 3.0, 8.5)))
    el.append(panel(sc, "thumbpiece", pewter, (11.25, 3.0, 7.25), (14.25, 3.5, 8.75)))
    # The snuffer lying in the pan beside the socket: a plain cone, its mouth to the back, on a thin straight handle
    # resting over the rim.
    sx, sy = 5.8, 1.625
    el += crossed_z(sc, "snuffer", pewter, sx, sy, 1.75, 8.5, 9.75, south=solid(SHADOW, 1))
    el.append(panel(sc, "snuffer", pewter, (sx - 0.5, sy - 0.5, 7.25), (sx + 0.5, sy + 0.5, 8.5)))
    el.append(panel(sc, "snuffer_handle", flat(PEWTER), (sx - 0.25, 1.75, 3.0), (sx + 0.25, 2.25, 7.25)))
    sc.models["whole"] = el
    sc.models["lit"] = el + flame
    return sc


# ---------------------------------------------------------------- the Embroidered Harvest Sampler

# The sampler's ground, a stitch to a texel (13 across, 13 down), worked in whole stitches on an even-weave linen: a
# stitched border all round in rust and ochre by turns; inside it, a band of letters (an alphabet's first three, A B C,
# as a sampler begins) in dark brown; a stitched rule of olive; and the harvest: two wheat sheaves tied in rust either
# side of the raised pumpkin. '.' is bare linen. Each stitched texel is one of two tones of its thread by turns, and the
# bare linen two tones by turns too (`stitches`), so the whole field reads as rows of stitches on a woven ground.
SAMPLER_FIELD = ("...........",
                 ".d..dd...dd",
                 "ddd.ddd.d..",
                 "d.d.dd...dd",
                 "...........",
                 ".v.v.v.v.v.",
                 "y.y.....y.y",
                 ".y.......y.",
                 ".r.......r.",
                 "b.b.....b.b",
                 "...........")


def sampler_map():
    """SAMPLER_FIELD inside its stitched border: 13 x 13 stitches."""
    rows = [("ro" * 7)[:13]]
    for i, row in enumerate(SAMPLER_FIELD):
        side = "or"[i % 2]
        rows.append(side + row[:11].ljust(11, ".") + ("ro"[i % 2]))
    rows.append(("or" * 7)[:13])
    return tuple(rows)


THREADS = {"r": MAPLE, "o": ASPEN, "d": pal("2a1a10", "3e2818"), "v": OLIVE, "y": WHEAT, "b": pal("5a3c18", "74502a")}


def stitches(rows):
    """Cross-stitch on even-weave linen, a stitch to a texel: each stitched texel one of its thread's two tones by turns
    in a checker (so each stitch stands apart from the next), the bare linen two close tones by turns (the weave)."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                ch = rows[min(y, len(rows) - 1)][min(x, len(rows[0]) - 1)]
                turn = (x + y) % 2
                if ch == ".":
                    c = shade(LINEN, 2 if turn else 3)
                else:
                    thread = THREADS[ch]
                    c = shade(thread, (1, 2)[turn] if len(thread) > 2 else turn)
                p.put(x, y, c)
    return tagged(paint, "stitches", rows)


def harvest_sampler():
    """The Embroidered Harvest Sampler, hung on a wall facing north: a ground of even-weave linen worked in cross-stitch
    (sampler_map): a stitched border, a band of letters, a stitched rule and two wheat sheaves; set back behind a moulded
    walnut frame with corner blocks, a backing board behind; and in raised stitchery standing off the linen, a pumpkin
    with its stem and leaf between the sheaves."""
    sc = fa.Sculpt("harvest_sampler", 4406, 64)
    rail = lengthwise(WALNUT)
    x0, y0, x1, y1 = 0.5, 0.5, 15.5, 15.5
    w = 1.0
    el = []
    # The frame: rails a pixel wide, each a base and a raised bead along its outer half; corner blocks over the joints.
    for lo, hi in (((x0, y0, 14.5), (x1, y0 + w, 16.0)), ((x0, y1 - w, 14.5), (x1, y1, 16.0)),
                   ((x0, y0 + w, 14.5), (x0 + w, y1 - w, 16.0)), ((x1 - w, y0 + w, 14.5), (x1, y1 - w, 16.0))):
        el.append(panel(sc, "frame", rail, lo, hi))
    for lo, hi in (((x0 + w, y0, 14.0), (x1 - w, y0 + 0.5, 14.5)), ((x0 + w, y1 - 0.5, 14.0), (x1 - w, y1, 14.5)),
                   ((x0, y0 + w, 14.0), (x0 + 0.5, y1 - w, 14.5)), ((x1 - 0.5, y0 + w, 14.0), (x1, y1 - w, 14.5))):
        el.append(panel(sc, "bead", rail, lo, hi))
    for cx, cy in ((x0, y0), (x1 - w, y0), (x0, y1 - w), (x1 - w, y1 - w)):
        el.append(panel(sc, "corner", rail, (cx, cy, 13.75), (cx + w, cy + w, 14.5), north=flat(WALNUT)))
    # The worked linen, 0.75 behind the frame's face, a walnut backing board behind it.
    el.append(panel(sc, "linen", flat(LINEN), (x0 + w, y0 + w, 15.25), (x1 - w, y1 - w, 16.0), north=stitches(sampler_map()),
                    south=planks(WALNUT, board=2, nails=False)))
    # Raised stitchery: the pumpkin (ribbed), its stem and a leaf, between the sheaves.
    el += crossed_z(sc, "pumpkin", tdd.ribbed(PUMPKIN), 8.0, 4.75, 4.0, 14.75, 15.25)
    el.append(panel(sc, "pumpkin_stem", flat(STEM), (7.75, 6.75, 14.75), (8.5, 7.75, 15.25)))
    el.append(panel(sc, "pumpkin_leaf", flat(LEAF), (8.5, 6.5, 14.875), (10.0, 7.25, 15.25)))
    sc.models["whole"] = el
    return sc


# ---------------------------------------------------------------- refined in place: the Quilt Rail

def quilt_rail():
    """The Quilt Rail, refined (tools/fall_garden.py PORCH; its ID, shape, states, recipe and loot unchanged), facing
    north: the same length of porch rail, a top rail and a bottom rail between two posts with three square balusters and
    a turned cap on each post, and the same patchwork quilt (tools/fall_garden_data.py patchwork) now with its body: a
    thick fold over the top rail, falling in three pleats down the front to a ragged hem and in two shorter ones down
    the back, every hem bound in rust; a running stitch in cream marks each patch join and runs along each binding."""
    import fall_garden_data as fgd
    sc = fa.Sculpt("quilt_rail", 2408, 64)
    wood = lengthwise(ppd.WOOD)
    quilt, binding = quilted(fgd.FABRICS), bound(fgd.FABRICS[0])
    el = [panel(sc, "top_rail", wood, (0.0, 13.0, 7.0), (16.0, 14.5, 9.0)),
          panel(sc, "bottom_rail", wood, (0.0, 1.5, 7.25), (16.0, 2.5, 8.75))]
    for x0 in (0.5, 14.0):
        el.append(panel(sc, "post", wood, (x0, 0.0, 7.0), (x0 + 1.5, 13.0, 9.0)))
        el.append(panel(sc, "post_cap", wood, (x0 + 0.25, 14.5, 7.25), (x0 + 1.25, 15.0, 8.75)))
    for x0 in (4.5, 7.5, 10.5):
        el.append(panel(sc, "baluster", wood, (x0, 2.5, 7.5), (x0 + 1.0, 13.0, 8.5)))
    # The fold over the rail.
    el.append(panel(sc, "quilt_top", quilt, (2.25, 14.5, 6.0), (13.75, 15.0, 10.0)))
    # Three pleats down the front, each standing a little off the last, their hems at different heights; two down the
    # back. Each ends in a binding a little thicker than the quilt.
    front = ((2.25, 6.0, 6.0, 4.5), (6.0, 9.75, 6.3, 3.75), (9.75, 13.75, 6.0, 5.0))
    back = ((2.25, 8.0, 9.5, 8.5), (8.0, 13.75, 9.2, 9.5))
    for x0, x1, z0, hem in front + back:
        z1 = z0 + 0.5
        el.append(panel(sc, "quilt", quilt, (x0, hem + 0.75, z0), (x1, 14.5, z1)))
        el.append(panel(sc, "binding", binding, (x0, hem, z0 - 0.1), (x1, hem + 0.75, z1 + 0.1)))
    sc.models["whole"] = el
    return sc


# ---------------------------------------------------------------- refined in place: the Butter Churn

def quilted(fabrics):
    """The Quilt Rail's patchwork (tools/fall_garden_data.py patchwork: squares four texels across in its four fabrics
    in a steady order, each square's lower and right edge a tone darker, its top left texel lit) with a restrained
    running stitch: one muted cream stitch a square, at the middle of its lower seam and of its right seam by turns."""
    stitch = shade(LINEN, 1)

    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                fabric = fabrics[((x // 4) + 3 * (y // 4)) % 4]
                if (x % 4, y % 4) == ((1, 3) if (x // 4 + y // 4) % 2 == 0 else (3, 1)):
                    c = stitch
                else:
                    k = 1 if x % 4 == 3 or y % 4 == 3 else 3 if (x % 4, y % 4) == (0, 0) else 2
                    c = shade(fabric, k)
                p.put(x, y, c)
    return tagged(paint, "quilted", fabrics)


def bound(palette):
    """A binding: a flat strap (pumpkin_patch_data.flat) with a muted cream running stitch every third texel along its
    middle."""
    stitch = shade(LINEN, 1)

    def paint(p):
        along_x = p.w >= p.h
        across = p.h if along_x else p.w
        for y in range(p.h):
            for x in range(p.w):
                a, s_ = (y, x) if along_x else (x, y)
                if a == across // 2 and s_ % 3 == 1 and across > 1:
                    c = stitch
                else:
                    c = shade(palette, 3 if (a == 0 and across > 1) else 1 if (a == across - 1 and across > 2) else 2)
                p.put(x, y, c)
    return tagged(paint, "bound", palette)


def churn():
    """The Butter Churn, refined (tools/barn_tools.py CHURN; its ID, shape, states, working and dasher's motion
    unchanged), facing north. Its coopered body is the original's, unchanged (tools/barn_tools_data.py churn, base
    8075c6d58b): upright staves, two crossed boxes six pixels across, two iron hoops standing a little proud. Refined
    are the lid and the dasher, and where one passes through the other: a round lid seated on the staves with a raised
    rim round its edge, a centre hole and a splash cup collar round it; the dasher (`runner`, drawn by its own model for
    the renderer to work up and down) a turned handle rising through the cup to a ferrule, a grip and a knob."""
    import barn_tools as bt
    sc = fa.Sculpt(bt.CHURN["block"], 2302, 64)
    wood = ppd.WOOD
    staves_ = planks(wood, board=3, nails=False)
    el = []
    # The original body, box for box.
    for a, b in ((2.0, 3.0), (3.0, 2.0)):
        el.append(panel(sc, "staves", staves_, (8.0 - a, 0.0, 8.0 - b), (8.0 + a, 12.5, 8.0 + b), up=flat(wood), down=flat(wood)))
    for y in (1.5, 9.5):
        for a, b in ((2.0, 3.0), (3.0, 2.0)):
            el.append(panel(sc, "hoop", flat(IRON), (8.0 - a - 0.25, y, 8.0 - b - 0.25), (8.0 + a + 0.25, y + 1.0, 8.0 + b + 0.25)))
    # The lid: a board ring seated on the staves round a centre hole, a raised rim round its edge, a splash cup round
    # the hole.
    lid = planks(wood, board=2, nails=False)
    el += ring(sc, "lid", flat(wood), solid(SHADOW, 1), 8.0, 8.0, 5.0, 12.5, 13.0, t=1.75, top=lid)
    el += ring(sc, "lid_rim", flat(wood), flat(wood), 8.0, 8.0, 5.0, 13.0, 13.25, t=0.5)
    el += ring(sc, "splash_cup", flat(wood), solid(SHADOW, 1), 8.0, 8.0, 2.5, 13.0, 13.75, t=0.5)
    sc.models["base"] = el
    turned_ = lengthwise(wood)
    # Everything wider than the handle stays above the splash cup at the bottom of the stroke (5 pixels down).
    sc.models["runner"] = [panel(sc, "handle", turned_, (7.5, 10.0, 7.5), (8.5, 19.0, 8.5)),
                           panel(sc, "ferrule", flat(IRON), (7.375, 19.0, 7.375), (8.625, 19.5, 8.625)),
                           panel(sc, "grip", turned_, (7.25, 19.5, 7.25), (8.75, 21.25, 8.75)),
                           panel(sc, "knob", turned_, (7.5, 21.25, 7.5), (8.5, 21.75, 8.5))]
    return sc


# ---------------------------------------------------------------- building and files

BUILDERS = {"oak_settle": oak_settle, "carved_dish_dresser": carved_dish_dresser, "pilgrim_peg_rail": pilgrim_peg_rail,
            "pewter_pitcher_and_basin": pewter_pitcher_and_basin, "pewter_chamberstick": pewter_chamberstick,
            "harvest_sampler": harvest_sampler}
_built = {}


def multi(block):
    return th.PROPS[block]["kind"] in ("settle", "furniture")


def build(block):
    """The piece's Sculpt, its pieces packed into the smallest texture of 64 or 128 texels square they fit; a
    multi-block piece's part models (`<block>_<part>`, pumpkin_patch_data.cut) and its item, the whole at half size."""
    if block not in _built:
        try:
            sc = ppd.planned(BUILDERS[block], 64)
        except ValueError:
            sc = ppd.planned(BUILDERS[block], 128)
        if multi(block):
            size = th.PROPS[block]["size"]
            whole = sc.models["whole"]
            for part, elements in enumerate(ppd.cut(whole, size)):
                sc.models[f"part_{part}"] = elements
            sc.models["item"] = fa.transformed(d17d.pinned(whole), 0.5, (0, 0, 8 - 4 * size[2]), (0, 0, 0))
        _built[block] = sc
    return _built[block]


def _image_of(name):
    return build(name).atlas.img if name in BUILDERS else None


DISPLAY = tdd.DISPLAY
WALL_DISPLAY = tdd.WALL_DISPLAY


def bounds(elements):
    return tdd.bounds(elements)


def turned(model, facing, **extra):
    return {"model": rid(f"block/{model}"), **({"y": FACINGS[facing]} if FACINGS[facing] else {}), **extra}


def assets(root, write, lang):
    """Each piece's block models (drawn whole, separated; a multi-block piece's part models), its blockstate by its
    kind's states, item model and item."""
    write = fa.closing_writer(write, _image_of)
    models = root / "models" / "block"
    lang.update(th.names())
    for block, spec in th.PROPS.items():
        if block not in BUILDERS:
            continue
        sc = build(block)
        kind = spec["kind"]
        states = root / "blockstates" / f"{block}.json"
        if multi(block):
            n = len(th.cells(spec["size"]))
            for part in range(n):
                write(models / f"{block}_{part}.json", fa.model(block, sc.models[f"part_{part}"]))
            write(states, {"variants": {f"facing={f},lit={l},part={p}": turned(f"{block}_{p}", f)
                                        for f in FACINGS for l in ("false", "true") for p in range(n)}})
            item = sc.models["item"]
        else:
            whole = d20d.separated(sc.models["whole"])
            write(models / f"{block}.json", fa.model(block, whole))
            if kind == "candles":
                write(models / f"{block}_lit.json", fa.model(block, d20d.separated(sc.models["lit"])))
                write(states, {"variants": {f"facing={f},lit={l}": turned(block + ("_lit" if l == "true" else ""), f)
                                            for f in FACINGS for l in ("false", "true")}})
            else:
                write(states, {"variants": {f"facing={f}": turned(block, f) for f in FACINGS}})
            item = whole
        display = WALL_DISPLAY if kind == "wall" else DISPLAY
        write(root / "models" / "item" / f"{block}.json", fa.model(block, item, display))
        write(root / "items" / f"{block}.json", {"model": {"type": "minecraft:model", "model": rid(f"item/{block}")}})


def loot(out, write):
    """Each piece drops itself once: a multi-block piece only from its first block (part 0)."""
    for block, spec in th.PROPS.items():
        pool = {"condition": {"type": "minecraft:survives_explosion"}, "entries": [{"type": "minecraft:item", "name": rid(block)}], "rolls": 1}
        if multi(block):
            pool["condition"] = {"type": "minecraft:all_of", "terms": [
                {"type": "minecraft:survives_explosion"}, {"type": "minecraft:match_block", "blocks": rid(block), "state": {"part": "0"}}]}
        write(out / f"{block}.json", {"type": "minecraft:block", "pools": [pool], "random_sequence": rid(f"blocks/{block}")})


def tags(tags):
    """Pewter comes off quickest with a pickaxe (thanksgiving_homestead.PICKAXE); wood and cloth with an axe."""
    for block in th.PROPS:
        tags.add("block", "minecraft:mineable/pickaxe" if block in th.PICKAXE else "minecraft:mineable/axe", rid(block))


def textures():
    return {("block", block): build(block).atlas.img for block in th.PROPS if block in BUILDERS}


# ---------------------------------------------------------------- writing this group's files alone

def recipes(out, write):
    """The recipes, as tools/agriculture_data.py recipes() writes thanksgiving_homestead.SHAPELESS (it is added to the
    agriculture SHAPELESS list), on the agriculture feature switch."""
    for recipe in th.SHAPELESS:
        write(out / f"{recipe['id']}.json", {"fabric:load_conditions": [{"condition": f"{MOD}:feature_enabled", "feature": th.FEATURE}],
                                             "type": "minecraft:crafting_shapeless", "category": recipe["category"],
                                             "ingredients": recipe["inputs"], "result": {"id": rid(recipe["result"]), "count": recipe["count"]}})


class _Tags:
    def __init__(self):
        self.values = {}

    def add(self, kind, tag, value):
        self.values.setdefault((kind, tag), []).append(value)


def generate(res=None):
    """Writes this group's own generated files the way tools/generate_material_data.py does (models finished by
    model_writer, JSON indented two spaces, textures saved optimized), and returns (paths written, lang entries, tag
    entries): the shared names file and mining tags are not written here but merged by the shared pipeline."""
    import json
    from pathlib import Path
    import model_writer
    res = Path(res) if res else Path(__file__).resolve().parents[1] / "src" / "main" / "resources"
    written = []

    def write(path, obj):
        if isinstance(obj, dict) and obj.get("elements") and "models" in path.parts:
            model_writer.finish_elements(obj["elements"])
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(json.dumps(obj, indent=2) + "\n", encoding="utf-8")
        written.append(path)

    lang = {}
    assets(res / "assets" / MOD, write, lang)
    refined_assets(res / "assets" / MOD, write, lang)
    loot(res / "data" / MOD / "loot_table" / "blocks", write)
    recipes(res / "data" / MOD / "recipe", write)
    for (kind, name), img in list(textures().items()) + list(refined_textures().items()):
        path = res / "assets" / MOD / "textures" / kind / f"{name}.png"
        path.parent.mkdir(parents=True, exist_ok=True)
        img.save(path, optimize=True)
        written.append(path)
    collected = _Tags()
    tags(collected)
    return written, lang, collected.values


REFINED_BUILDERS = {"quilt_rail": ("fall_garden_data", lambda: quilt_rail), "butter_churn": ("barn_tools_data", lambda: (churn, 64))}


def _refined_modules():
    """The modules that own the refined pieces, their builders set to this module's (as the shared hook sets them),
    and any build made before that forgotten."""
    import importlib
    out = []
    for block, (module, builder) in REFINED_BUILDERS.items():
        mod = importlib.import_module(module)
        mod.BUILDERS[block] = builder()
        mod._built.pop(block, None)
        out.append((block, mod))
    return out


def refined_assets(root, write, lang):
    """The refined pieces' models, blockstates and items, written by the builders that own them (the quilt rail's by
    tools/fall_garden_data.py, the churn's by tools/barn_tools_data.py) with this module's models in place; only their
    files are kept."""
    for block, mod in _refined_modules():
        def mine(path, obj, block=block):
            if path.stem == block or path.stem.startswith(block + "_"):
                write(path, obj)
        (mod.part2_assets if block == "butter_churn" else mod.assets)(root, mine, {})


def refined_textures():
    return {("block", block): mod.build(block).atlas.img for block, mod in _refined_modules()}


if __name__ == "__main__":
    paths, names_, tag_values = generate()
    for p in paths:
        print(p)

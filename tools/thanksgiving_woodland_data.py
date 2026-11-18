"""Generated assets for the woodland harvest crafts (tools/thanksgiving_woodland.py;
docs/features/thanksgiving-woodland-crafts.md).

Built from boxes at real size, one texel to a model pixel, in the Thanksgiving decorations' manner
(tools/thanksgiving_decor_data.py): each floor piece is one packed texture (block/<piece>) of pieces painted for the faces
they cover (pumpkin_patch_data.panel), flat in three to five tones a material, lit from the top left, patterned rather
than speckled. The Harvest Festival Grounds' willow braid binds the basket lids and rims; the Pumpkin Patch Market's
lengthwise wood, flat strap and cord-wrap painters draw the poles, straps and lashings; its corn and husk palettes are
the Harvest Festival's. The patterns follow the everyday forms the record's source notes cite: open twining (weft rows
twisted round cord warps, the warps showing between them) on the baskets and the mat, and incised lines with a row of
toothed (dentate) impressions on the pot's collar. Nothing ceremonial, sacred or regalia is drawn, and no motif from
another nation's tradition (no etched bark designs): the bark box is left plain.

The Rush Floor Mat is drawn from two 16x16 textures read by position (block/rush_floor_mat, block/rush_floor_mat_border),
so mats side by side read as one: a multipart of a middle, four edges and four corners, each edge and corner either
plain mat (joined) or bound edge (not), turned with the mat's axis.

Everything is drawn here by code; no Mojang texture is read, traced or copied, and nothing from the owner library is
used (the record says what was searched).

Writes, from agriculture_data.py (assets, loot, tags) and crop_textures.py (textures) once hooked, or standalone by
generate(): each piece's block and item models, blockstate, item, self-dropping loot and recipe; its tag; its texture.
"""
import json
import math
from pathlib import Path

import decor20_data as d20d
import flora_art as fa
import harvest_festival_data as hfd
import model_writer
import pumpkin_patch_data as ppd
import thanksgiving_decor_data as tdd
import thanksgiving_woodland as tw
from flora_art import Sculpt, pal, shade
from harvest_festival_data import CORN, HUSK, braid, crossed_x, crossed_y
from pumpkin_patch_data import flat, lengthwise, panel, wrapped

MOD = "jugcraft"
FACINGS = {"north": 0, "east": 90, "south": 180, "west": 270}
ROOT = Path(__file__).resolve().parents[1]
RES = ROOT / "src" / "main" / "resources"

# Palettes, darkest first; neighbouring tones about 15 to 25 apart in brightness.
GRASS = pal("6c5830", "8a7440", "a68e52", "c0a866", "d6c07c")      # sun-dried grass and rush, twined
BARK_CORD = pal("3a2616", "54381f", "6e4c2c", "88623a")           # inner-bark cord: warps, straps, lashings
CORD_BAND = pal("4a2a1a", "643a24", "7e4c30", "965e3c")           # a darker band of bark-cord weft
BIRCH = pal("4e4238", "8c8270", "a89e8c", "c4bca8", "dcd4c2")     # outer bark; its first tone the lenticels
BIRCH_INNER = pal("7a4e28", "9a6634", "b47e44", "c89658")         # the bark's inside
ROOT_LASH = pal("3e2c1a", "5a4028", "76563a", "8e6c4a")           # root lashing
CARVED = pal("4a2c16", "64401f", "7e542a", "966836", "ac7c44")     # carved hardwood
SAMP = pal("a08a4c", "bca460", "d4bc78", "e6d292")               # cracked-corn porridge
BERRY = pal("4a0e12", "6e1418", "942020", "b8302a")               # cranberries
CLAY = pal("6a3a26", "86492e", "a25c3a", "b8704a", "c8845c")       # fired earthenware, warm terracotta
SOOT = pal("2e2620", "40342a", "524436", "645442")                # fire-clouding at the pot's base
STEW = pal("3a2a14", "52401e", "6a5428")
BEAN = pal("4a1c14", "6a2a1c", "8a3c28")
STONE = pal("4a4844", "62605a", "7c7a72", "96948a")               # hearthstones and the pestle's stone
ASH = pal("5a5652", "767270", "908c88", "a8a49e")
CHAR = pal("1c1612", "2c241c", "3e3226")
POLE = pal("3a3026", "524436", "6a5a48", "827058")                # sapling poles in their bark
LOG_BARK = pal("3a2c1e", "4e3c28", "644e34", "7a6240")
LOG_WOOD = pal("6a4a2a", "86603a", "a07a4c", "b8925e")
LOG_INNER = pal("3e2a18", "523820", "664a2c")
WHITE_CORN = pal("8c826a", "a89e82", "c4ba9c", "dcd2b4")
RED_CORN = pal("4e120e", "6e1c14", "8c2a1e", "a83c2a")
SINEW = pal("8c7a5a", "b4a07a", "d4c49c")                         # the pale thread a strap is sewn on with


def rid(path):
    return path if ":" in path else f"{MOD}:{path}"


def tagged(paint, *tag):
    paint.tag = repr(tag)
    return paint


# ---------------------------------------------------------------- painters

def twined(palette=GRASS, warp=BARK_CORD):
    """Open twining: weft rows two texels high, each a run of slanted stitches two texels wide (lit at the top left, its
    own tone, its own tone, shaded at the bottom right), each row's stitches a texel along from the last's; between the
    rows a texel of the cord warps, a warp every two texels over a dark ground (open twining, the warps showing)."""
    def paint(p):
        for y in range(p.h):
            row, within = divmod(y, 3)
            for x in range(p.w):
                if within == 2:
                    c = shade(warp, 2 if x % 2 == 0 else 0)
                else:
                    s = (x + row) % 2
                    c = shade(palette, ((3, 2), (2, 1))[within][s])
                p.put(x, y, c)
    return tagged(paint, "twined", palette, warp)


def twined_rings(palette=GRASS, warp=BARK_CORD):
    """A twined lid seen from above: the same rows going round it, in square rings a texel wide (two of weft, one of
    warp) into its middle."""
    def paint(p):
        cx, cy = (p.w - 1) / 2, (p.h - 1) / 2
        for y in range(p.h):
            for x in range(p.w):
                ring = int(max(abs(x - cx), abs(y - cy)))
                if ring % 3 == 2:
                    c = shade(warp, 2 if (x + y) % 2 == 0 else 0)
                else:
                    c = shade(palette, 3 - ring % 3 - (x + y) % 2)
                p.put(x, y, c)
    return tagged(paint, "twined_rings", palette, warp)


def birch_bark():
    """Birch bark: cream, its top row lit, crossed by the bark's lenticels as short dark dashes three texels long, in
    rows every three texels, a dash every six texels, each row's three along from the last's (a regular pattern, not a
    scatter), each dash darkest in its middle, with a band a shade down every eight rows where the layers part."""
    def paint(p):
        for y in range(p.h):
            row = y // 3
            for x in range(p.w):
                k = 4 if (y == 0 and p.h > 1) else 2 if y % 8 == 6 else 3
                if y % 3 == 1 and (x + row * 3) % 6 in (0, 1, 2):
                    k = 0 if (x + row * 3) % 6 == 1 else 1
                p.put(x, y, shade(BIRCH, k))
    return tagged(paint, "birch_bark")


def faceted(palette=CARVED):
    """Carved wood: knife facets as upright bands four texels wide, each lit at its leading edge and shaded at its
    trailing one, its top row lit."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                k = 3 if (y == 0 and p.h > 1) else (3, 2, 2, 1)[x % 4]
                p.put(x, y, shade(palette, k))
    return tagged(paint, "faceted", palette)


def grains(palette, light=3):
    """Small grains heaped (cracked corn, samp): the grains on a slanting lattice, a lit one, the ground tone, a shaded
    one, by turns along each row, each row two along from the last."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                p.put(x, y, shade(palette, (light, light - 1, light - 2)[(x + 2 * y) % 3]))
    return tagged(paint, "grains", palette, light)


def berries():
    """Cranberries heaped: round berries two texels across, each lit at its top left and shaded at its bottom right,
    every other one a shade darker."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                dark = (x // 2 + y // 2) % 2
                corner = (x % 2, y % 2)
                k = {(0, 0): 3, (1, 1): 1}.get(corner, 2) - dark
                p.put(x, y, shade(BERRY, k))
    return tagged(paint, "berries")


def fired_clay(clouded=0):
    """Fired earthenware: one warm base tone on every face (no lit row, no seams, so stacked layers read as one smooth
    body), with soft fire clouding as large patches a shade darker: blocks five texels wide and three high on a staggered
    lattice, one in three of them clouded (`clouded` 0), or two in three (`clouded` 1, near the fire)."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                cell = (x + 2 * (y // 3)) // 5 + y // 3
                k = 1 if cell % 3 < 1 + clouded else 2
                p.put(x, y, shade(CLAY, k))
    return tagged(paint, "fired_clay", clouded)


def incised_collar():
    """The pot's collar, read from its rim down: the clay's base tone; then a shallow incised band a shade down, the one
    regular mark on the pot: an incised line, a row of impressions (one every three texels), another incised line; then
    the base tone again."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                if y in (1, 3):
                    k = 1
                elif y == 2:
                    k = 1 if x % 3 == 0 else 2
                else:
                    k = 2
                p.put(x, y, shade(CLAY, k))
    return tagged(paint, "incised_collar")


def stew():
    """Corn and beans in their broth, seen from above: the broth's tone with a lighter ripple every fourth row, kernels and
    beans on two even, sparse lattices."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                if (x + 3 * y) % 7 == 0:
                    c = shade(CORN, 1)
                elif (3 * x + y) % 9 == 4:
                    c = shade(BEAN, 1)
                else:
                    c = shade(STEW, 2 if y % 4 == 1 else 1)
                p.put(x, y, c)
    return tagged(paint, "stew")


def fieldstone(palette=STONE):
    """A rounded fieldstone: its top row lit, its bottom row shaded, small clumps a shade down on an even lattice (vanilla's
    manner of mottling, not noise)."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                if y == 0 and p.h > 1:
                    k = 3
                elif y == p.h - 1 and p.h > 2:
                    k = 1
                else:
                    k = 1 if (x // 2 + y // 2) % 3 == 0 else 2
                p.put(x, y, shade(palette, k))
    return tagged(paint, "fieldstone", palette)


def ash():
    """Cold ash: pale greys in short steps along each row, two texels to a step, each row a texel along."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                p.put(x, y, shade(ASH, (1, 2, 3, 2)[(x // 2 + y) % 4]))
    return tagged(paint, "ash")


def furrowed(palette=LOG_BARK):
    """A log's bark: upright furrows every four texels (a groove a shade down, a ridge lit, its own tone twice), each
    groove broken for a texel every six rows, each column's breaks three rows along from the last's, as vanilla's bark
    plates are (docs/NATURAL_TEXTURES.md: furrows run up a trunk, never round it)."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                if y == 0 and p.h > 1:
                    k = 3
                elif x % 4 == 0:
                    k = 2 if (y + 3 * (x // 4)) % 6 == 0 else 0
                else:
                    k = (3, 2, 2)[x % 4 - 1]
                p.put(x, y, shade(palette, k))
    return tagged(paint, "furrowed", palette)


def kernel_rows(palette):
    """An ear of corn in its own colour (harvest_festival_data.kernels' rows): a kernel lit, the next shaded, each row a
    texel along from the last."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                p.put(x, y, shade(palette, 3 if (x + y) % 2 == 0 else 1))
    return tagged(paint, "kernel_rows", palette)


def husk_braid():
    """Husks braided into a cord: plaited on the slant like the willow braid, in the husk's three tones."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                p.put(x, y, shade(HUSK, (2, 1, 0)[(x + y) % 3]))
    return tagged(paint, "husk_braid")


def solid(palette, k):
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                p.put(x, y, shade(palette, k))
    return tagged(paint, "solid", palette, k)


def heap(palette, light=3):
    """A soft heap seen from above (samp, cracked corn): lightest in its middle where it rises, its own tone round that,
    a shade down at its edge, the rings following the piece's shape; a lighter grain every five texels along the middle
    ring, each row's two along from the last's. No alternating squares."""
    def paint(p):
        cx, cy = (p.w - 1) / 2, (p.h - 1) / 2
        for y in range(p.h):
            for x in range(p.w):
                d = max(abs(x - cx) / max(cx, 0.5), abs(y - cy) / max(cy, 0.5))
                k = light if d < 0.45 else light - 1 if d < 0.85 else light - 2
                if 0.45 <= d < 0.85 and (x + 2 * y) % 5 == 0:
                    k = light
                p.put(x, y, shade(palette, k))
    return tagged(paint, "heap", palette, light)


def stitched(palette=ROOT_LASH):
    """Bark sewn with root: stitches two texels wide across the piece's length with a dark gap a texel wide between them,
    at an even spacing, each stitch lit along its first row (two tones and the gap)."""
    def paint(p):
        along_x = p.w >= p.h
        for y in range(p.h):
            for x in range(p.w):
                a, b = (x, y) if along_x else (y, x)
                k = 0 if a % 3 == 2 else 3 if b == 0 else 2
                p.put(x, y, shade(palette, k))
    return tagged(paint, "stitched", palette)


def sewn_strap():
    """A strap's sewn end: the strap's flat bark cord, a line of pale sinew stitches down its middle, a stitch every two
    texels (the stitches sewing it to the basket)."""
    def paint(p):
        along_x = p.w >= p.h
        across = p.h if along_x else p.w
        for y in range(p.h):
            for x in range(p.w):
                a, b = (x, y) if along_x else (y, x)
                if b == across // 2 and a % 2 == 0:
                    c = shade(SINEW, 2 if a % 4 == 0 else 1)
                else:
                    c = shade(BARK_CORD, 3 if b == 0 and across > 1 else 1 if b == across - 1 and across > 2 else 2)
                p.put(x, y, c)
    return tagged(paint, "sewn_strap")


def bark_lengthwise(palette=LOG_BARK):
    """A log's bark running up it, not round it (docs/NATURAL_TEXTURES.md): long plates a texel or two wide in three close
    tones in a fixed run across the piece, each broken a shade down every seven rows, a pair of columns three rows along
    from the last's. No dark grooves, so it reads as bark and not as staves."""
    tones = (2, 3, 2, 1, 2, 3, 3, 2)

    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                k = tones[x % 8]
                if (y + 3 * (x // 2)) % 7 == 0:
                    k = max(1, k - 1)
                if y == 0 and p.h > 1:
                    k = 3
                p.put(x, y, shade(palette, k))
    return tagged(paint, "bark_lengthwise", palette)


def end_grain(outer):
    """A log's end grain on a rim, seen from above: its bark along the piece's `outer` side ("top", "bottom", "left" or
    "right", whichever faces out of the log), then a pale sapwood ring, then the heartwood in rings a texel wide, a shade
    apart by turns."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                d = {"top": y, "bottom": p.h - 1 - y, "left": x, "right": p.w - 1 - x}[outer]
                c = shade(LOG_BARK, 2) if d == 0 else shade(LOG_WOOD, 3 if d == 1 else (2, 1)[d % 2])
                p.put(x, y, c)
    return tagged(paint, "end_grain", outer)


# ---------------------------------------------------------------- shapes

def octagon(sc, name, painter, cx, cz, rx, rz, c, y0, y1, **over):
    """A rounded body from y0 to y1, `rx` by `rz` from its middle to its sides, its corners cut `c` on the slant: two
    boxes crossed (harvest_festival_data.crossed_y, oval)."""
    return [panel(sc, name, painter, (cx - rx + c, y0, cz - rz), (cx + rx - c, y1, cz + rz), **over),
            panel(sc, name, painter, (cx - rx, y0, cz - rz + c), (cx + rx, y1, cz + rz - c), **over)]


def octagon_ring(sc, name, painter, inside, cx, cz, rx, rz, c, hx, hz, y0, y1, top=None):
    """The walls of a rounded vessel `rx` by `rz` (corners cut `c`), its hollow `hx` by `hz` from its middle (no more
    than rx - c, rz - c): four walls, the front and back as wide as the crossed body's long box, the sides as deep as
    its other, so together they are exactly the rounded body less its hollow. Each is drawn `painter` outside and
    `inside` towards the hollow; `top` paints the rim."""
    assert hx <= rx - c + 1e-6 and hz <= rz - c + 1e-6, "the hollow must lie within the walls"
    cap = {"up": top} if top else {}
    return [panel(sc, name, painter, (cx - rx + c, y0, cz - rz), (cx + rx - c, y1, cz - hz), south=inside, **cap),
            panel(sc, name, painter, (cx - rx + c, y0, cz + hz), (cx + rx - c, y1, cz + rz), north=inside, **cap),
            panel(sc, name, painter, (cx - rx, y0, cz - rz + c), (cx - hx, y1, cz + rz - c), east=inside, **cap),
            panel(sc, name, painter, (cx + hx, y0, cz - rz + c), (cx + rx, y1, cz + rz - c), west=inside, **cap)]


def box_walls(sc, name, painter, inside, x0, z0, x1, z1, y0, y1, t=1.0, top=None):
    """Square walls `t` thick round x0..x1 by z0..z1, drawn `painter` outside and `inside` within; the north and south
    walls run the full width."""
    cap = {"up": top} if top else {}
    return [panel(sc, name, painter, (x0, y0, z0), (x1, y1, z0 + t), south=inside, **cap),
            panel(sc, name, painter, (x0, y0, z1 - t), (x1, y1, z1), north=inside, **cap),
            panel(sc, name, painter, (x0, y0, z0 + t), (x0 + t, y1, z1 - t), east=inside, **cap),
            panel(sc, name, painter, (x1 - t, y0, z0 + t), (x1, y1, z1 - t), west=inside, **cap)]


def band_ring(sc, name, painter, x0, z0, x1, z1, y0, y1, t=0.5):
    """A band (a lashing, a braided rim) round x0..x1 by z0..z1, `t` thick, from y0 to y1."""
    return box_walls(sc, name, painter, painter, x0, z0, x1, z1, y0, y1, t)


def q(v):
    """To the quarter pixel."""
    return round(v * 4) / 4


def round_layer(sc, name, painter, cx, cz, r, y0, y1, rz=None, **over):
    """A round (or, with `rz`, oval) layer `r` from its middle across x and `rz` across z, y0 to y1: a staircase of three
    rectangles (r by rz/2, about 0.85 r by 0.85 rz, r/2 by rz), laid as five boxes that do not overlap (a middle box the
    full width, the steps before and behind it), so its outline steps round like a circle in pixels and no two of its
    faces share a plane."""
    rz = r if rz is None else rz
    hx, hz, mx, mz = q(r * 0.5), q(rz * 0.5), q(r * 0.85), q(rz * 0.85)
    out = [panel(sc, name, painter, (cx - r, y0, cz - hz), (cx + r, y1, cz + hz), **over)]
    for sign in (-1, 1):
        if mz - hz > 1e-6:
            out.append(panel(sc, name, painter, (cx - mx, y0, min(cz + sign * hz, cz + sign * mz)), (cx + mx, y1, max(cz + sign * hz, cz + sign * mz)), **over))
        if rz - mz > 1e-6:
            out.append(panel(sc, name, painter, (cx - hx, y0, min(cz + sign * mz, cz + sign * rz)), (cx + hx, y1, max(cz + sign * mz, cz + sign * rz)), **over))
    return out


def round_ring(sc, name, painter, inside, cx, cz, r, h, y0, y1, tops=None, rz=None, hz=None):
    """The walls of a round (or oval) vessel `r` by `rz` from its middle round a rectangular hollow `h` by `hz` (no more
    than 0.85 r by 0.85 rz): the sides of an r-by-hz rectangle, the front and back of a 0.85 r by 0.85 rz one and the
    front and back of an h-by-rz one, six boxes that do not overlap and together make the stepped round outline less
    the hollow. Outside `painter`, towards the hollow `inside`; `tops` paints the rim, one painter or one for each side
    it faces ({"front", "back", "west", "east"})."""
    rz = r if rz is None else rz
    hz = h if hz is None else hz
    mx, mz = max(h, q(r * 0.85)), max(hz, q(rz * 0.85))
    tops = tops if isinstance(tops, dict) or tops is None else {side: tops for side in ("front", "back", "west", "east")}

    def cap(side):
        return {"up": tops[side]} if tops else {}
    out = [panel(sc, name, painter, (cx - r, y0, cz - hz), (cx - h, y1, cz + hz), east=inside, **cap("west")),
           panel(sc, name, painter, (cx + h, y0, cz - hz), (cx + r, y1, cz + hz), west=inside, **cap("east")),
           panel(sc, name, painter, (cx - mx, y0, cz - mz), (cx + mx, y1, cz - hz), south=inside, **cap("front")),
           panel(sc, name, painter, (cx - mx, y0, cz + hz), (cx + mx, y1, cz + mz), north=inside, **cap("back"))]
    if rz - mz > 1e-6:
        out += [panel(sc, name, painter, (cx - h, y0, cz - rz), (cx + h, y1, cz - mz), **cap("front")),
                panel(sc, name, painter, (cx - h, y0, cz + mz), (cx + h, y1, cz + rz), **cap("back"))]
    return out


def stair(r, rz=None):
    """The three rectangles (half sizes across x and z) of a stepped round outline `r` (by `rz`)."""
    rz = r if rz is None else rz
    return ((r, q(rz * 0.5)), (q(r * 0.85), q(rz * 0.85)), (q(r * 0.5), rz))


def _inside(rects, x, z):
    return any(abs(x) <= a + 1e-9 and abs(z) <= b + 1e-9 for a, b in rects)


def round_rim(sc, name, painter, inside, cx, cz, r, ri, y0, y1, top=None):
    """A round wall between two stepped round outlines, `r` outside and `ri` inside (so the mouth is round too, not a
    square frame): the region between them on a quarter-pixel grid, merged into the fewest boxes that do not overlap,
    each side facing the hollow drawn `inside` and each facing out `painter`; `top` paints the rim."""
    outer, inner = stair(r), stair(ri)
    step = 0.25
    n = int(round(2 * r / step))
    cells = [[(lambda x, z: _inside(outer, x, z) and not _inside(inner, x, z))(-r + (i + 0.5) * step, -r + (j + 0.5) * step)
              for i in range(n)] for j in range(n)]
    # Runs along x in each row, then runs of equal rows merged along z.
    runs = []
    for j in range(n):
        i = 0
        while i < n:
            if cells[j][i]:
                k = i
                while k < n and cells[j][k]:
                    k += 1
                runs.append((j, i, k))
                i = k
            else:
                i += 1
    boxes, open_ = [], {}
    for j in range(n + 1):
        row = {(i, k) for jj, i, k in runs if jj == j}
        for key in list(open_):
            if key not in row:
                boxes.append((key, open_.pop(key), j))
        for key in row:
            open_.setdefault(key, j)
    out = []
    for (i, k), j0, j1 in sorted(boxes, key=lambda b: (b[1], b[0])):
        x0, x1, z0, z1 = -r + i * step, -r + k * step, -r + j0 * step, -r + j1 * step
        sides = {}
        for side, (px, pz) in (("north", ((x0 + x1) / 2, z0 - 0.01)), ("south", ((x0 + x1) / 2, z1 + 0.01)),
                               ("west", (x0 - 0.01, (z0 + z1) / 2)), ("east", (x1 + 0.01, (z0 + z1) / 2))):
            if _inside(inner, px, pz):
                sides[side] = inside
        if top:
            sides["up"] = top
        out.append(panel(sc, name, painter, (cx + x0, y0, cz + z0), (cx + x1, y1, cz + z1), **sides))
    return out


GRAIN_TOPS = {"front": end_grain("top"), "back": end_grain("bottom"), "west": end_grain("left"), "east": end_grain("right")}


def hanging_ear(sc, x, top, z, palette, tilt=0.0):
    """An ear of corn hung by its husks tied at (x, top, z): a husk neck, the ear below it four pixels long and point
    down, and two husk leaves pulled back up over the tie, leaning out either way."""
    kernels = kernel_rows(palette) if palette is not CORN else hfd.kernels()
    husk = lengthwise(HUSK)
    rot = fa.rotation((x, top, z), "z", tilt) if tilt else None
    el = [panel(sc, "ear", kernels, (x - 0.75, top - 5.0, z - 0.75), (x + 0.75, top - 1.0, z + 0.75), rot=rot),
          panel(sc, "ear_tip", kernels, (x - 0.5, top - 5.75, z - 0.5), (x + 0.5, top - 5.0, z + 0.5), rot=rot),
          panel(sc, "husk_neck", husk, (x - 0.375, top - 1.0, z - 0.375), (x + 0.375, top + 0.25, z + 0.375), rot=rot)]
    for lean in (-22.5, 22.5):
        el.append(panel(sc, "husk_leaf", husk, (x - 0.25, top - 0.75, z - 0.5), (x + 0.25, top + 1.75, z + 0.5),
                        rot=fa.rotation((x, top - 0.75, z), "z", lean)))
    return el


def lying_ear(sc, x0, x1, y, z, palette=CORN):
    """An ear of corn lying along x from x0 to x1 on y (its middle across at z): the ear, its point at x0, its husks
    pulled back at x1."""
    kernels = kernel_rows(palette) if palette is not CORN else hfd.kernels()
    husk = lengthwise(HUSK)
    return [panel(sc, "ear", kernels, (x0 + 0.75, y, z - 0.75), (x1 - 1.5, y + 1.5, z + 0.75)),
            panel(sc, "ear_tip", kernels, (x0, y, z - 0.5), (x0 + 0.75, y + 1.0, z + 0.5)),
            panel(sc, "husk_neck", husk, (x1 - 1.5, y + 0.25, z - 0.5), (x1, y + 1.25, z + 0.5)),
            panel(sc, "husk_leaf", husk, (x1 - 1.0, y, z - 1.25), (x1 + 0.5, y + 0.5, z - 0.5)),
            panel(sc, "husk_leaf", husk, (x1 - 1.0, y, z + 0.5), (x1 + 0.5, y + 0.5, z + 1.25))]


# ---------------------------------------------------------------- the pieces

def lidded_twined_basket():
    """The Lidded Twined Basket, facing north: a round-shouldered storage basket of open twining, a band of darker bark
    cord twined round its belly, its flat round lid edged with a braid and twined in rings, a single braided knob on it,
    and a cord tie at each side of its shoulder."""
    sc = Sculpt("lidded_twined_basket", 5101, 128)
    weave, band, rings, rim = twined(), twined(CORD_BAND), twined_rings(), braid()
    cx, cz = 8.0, 8.0
    el = []
    for name, paint, half, c, y0, y1 in (("foot", weave, 4.0, 1.5, 0.0, 1.0), ("lower", weave, 5.0, 2.0, 1.0, 3.0),
                                         ("belly", weave, 5.5, 2.0, 3.0, 4.5), ("band", band, 5.75, 2.0, 4.5, 6.5),
                                         ("belly", weave, 5.5, 2.0, 6.5, 8.0), ("shoulder", weave, 5.0, 2.0, 8.0, 9.5),
                                         ("neck", weave, 4.0, 1.5, 9.5, 10.5)):
        el += octagon(sc, name, paint, cx, cz, half, half, c, y0, y1)
    el += round_layer(sc, "lid", rim, cx, cz, 5.0, 10.5, 11.75, up=rings)
    # A single braided knob on the lid.
    el += round_layer(sc, "knob", rim, cx, cz, 1.0, 11.75, 13.0)
    # A cord tie at each side of the shoulder.
    cord = flat(BARK_CORD)
    el += [panel(sc, "tie", cord, (2.25, 7.0, 7.5), (3.0, 9.0, 8.5)), panel(sc, "tie", cord, (13.0, 7.0, 7.5), (13.75, 9.0, 8.5))]
    sc.models["whole"] = el
    return sc


def twined_pack_basket():
    """The Twined Pack Basket, facing north: a tall carrying basket of open twining, stepping wider in two steps from its
    foot to its rim as a pack basket flares, its rim bound round with bark cord; two bark-cord shoulder straps sewn on
    with sinew at its foot and over its rim, standing out from its back in loops; three ears of corn standing in it."""
    sc = Sculpt("twined_pack_basket", 5102, 128)
    weave, inside = twined(), solid(GRASS, 0)
    el = box_walls(sc, "lower", weave, inside, 4.5, 6.0, 11.5, 11.0, 0.0, 4.0)
    el.append(panel(sc, "floor", weave, (5.5, 0.0, 7.0), (10.5, 1.0, 10.0), up=inside))
    el += box_walls(sc, "middle", weave, inside, 4.0, 5.5, 12.0, 11.5, 4.0, 8.5)
    el += box_walls(sc, "upper", weave, inside, 3.25, 4.75, 12.75, 12.25, 8.5, 12.5)
    # The rim bound round with bark cord, standing a quarter pixel proud of the weave.
    el += band_ring(sc, "rim", wrapped(BARK_CORD), 3.0, 4.5, 13.0, 12.5, 12.5, 13.75, t=1.0)
    # Packed with husks below the corn, filling each step's hollow.
    el += [panel(sc, "fill", solid(HUSK, 0), (5.5, 1.0, 7.0), (10.5, 4.0, 10.0)),
           panel(sc, "fill", solid(HUSK, 0), (5.0, 4.0, 6.5), (11.0, 8.5, 10.5)),
           panel(sc, "fill", solid(HUSK, 0), (4.25, 8.5, 5.75), (11.75, 11.5, 11.25), up=lengthwise(HUSK))]
    # Three ears standing in it, husks pulled back, one leaning each way.
    for x, z, palette, lean in ((6.0, 8.0, CORN, 22.5), (8.25, 9.25, WHITE_CORN, 0.0), (10.25, 7.75, RED_CORN, -22.5)):
        kernels = kernel_rows(palette) if palette is not CORN else hfd.kernels()
        rot = fa.rotation((x, 11.5, z), "z", lean) if lean else None
        el += [panel(sc, "ear", kernels, (x - 0.75, 11.5, z - 0.75), (x + 0.75, 15.0, z + 0.75), rot=rot),
               panel(sc, "ear_tip", kernels, (x - 0.5, 15.0, z - 0.5), (x + 0.5, 15.75, z + 0.5), rot=rot)]
    el.append(panel(sc, "husk_leaf", lengthwise(HUSK), (6.75, 11.5, 9.5), (7.75, 13.0, 10.0)))
    # The straps: each sewn to the back of its foot, out to a loop standing off its back, up and over the rim, and sewn
    # to the inside of the rim.
    strap, sewn = flat(BARK_CORD), sewn_strap()
    for x in (4.75, 10.25):
        el += [panel(sc, "strap_foot", sewn, (x, 0.75, 11.0), (x + 1.0, 3.0, 11.5)),
               panel(sc, "strap", strap, (x, 2.25, 11.5), (x + 1.0, 3.0, 12.75)),
               panel(sc, "strap", strap, (x, 2.25, 12.75), (x + 1.0, 14.25, 13.5)),
               panel(sc, "strap_over", sewn, (x, 13.75, 11.0), (x + 1.0, 14.25, 12.75)),
               panel(sc, "strap_inside", sewn, (x, 12.0, 10.5), (x + 1.0, 14.25, 11.0))]
    sc.models["whole"] = el
    return sc


def birch_bark_box():
    """The Birch-Bark Box, facing north: a box of folded birch bark, its four corners sewn up with root in even stitches,
    and its fitted lid seated flat on it, half a pixel proud all round, its edge bound with a band of root stitching. The
    bark plain, with its own lenticels."""
    sc = Sculpt("birch_bark_box", 5103, 128)
    bark, inner, sewn = birch_bark(), flat(BIRCH_INNER), stitched()
    el = [panel(sc, "box", bark, (2.5, 0.0, 4.0), (13.5, 6.0, 12.0), down=inner)]
    # The sewn corners.
    for x, z in ((2.25, 3.75), (12.75, 3.75), (2.25, 11.25), (12.75, 11.25)):
        el.append(panel(sc, "seam", sewn, (x, 0.5, z), (x + 1.0, 5.5, z + 1.0)))
    el.append(panel(sc, "lid", bark, (2.0, 5.5, 3.5), (14.0, 8.0, 12.5), down=inner))
    el += band_ring(sc, "lid_band", sewn, 1.75, 3.25, 14.25, 12.75, 5.5, 6.75, t=0.25)
    sc.models["whole"] = el
    return sc


def carved_bowls_and_spoons():
    """Carved Bowls and Spoons, facing north: a broad carved bowl with a lug handle at each end, heaped with samp, a
    carved spoon resting in it with its handle over the rim; a small carved bowl at the front left with a second spoon
    laid across it; and a shallow round twined food basket of cranberries at the front right."""
    sc = Sculpt("carved_bowls_and_spoons", 5105, 128)
    wood, inside, rim = faceted(), solid(CARVED, 1), flat(CARVED)
    # The broad bowl, flaring from its foot: a lower ring and a wider upper one, lined, on a floor.
    cx, cz = 8.0, 10.5
    el = round_layer(sc, "foot", wood, cx, cz, 3.0, 0.0, 0.75, rz=2.25)
    el += round_ring(sc, "bowl", wood, inside, cx, cz, 4.75, 3.0, 0.75, 2.5, tops=rim, rz=3.5, hz=2.0)
    el += round_ring(sc, "bowl", wood, inside, cx, cz, 5.75, 4.0, 2.5, 4.5, tops=rim, rz=4.5, hz=2.75)
    el.append(panel(sc, "samp", solid(SAMP, 1), (cx - 3.0, 0.75, cz - 2.0), (cx + 3.0, 2.5, cz + 2.0), down=inside))
    el.append(panel(sc, "samp", solid(SAMP, 1), (cx - 4.0, 2.5, cz - 2.75), (cx + 4.0, 3.5, cz + 2.75), up=heap(SAMP)))
    el.append(panel(sc, "samp_heap", solid(SAMP, 2), (cx - 2.25, 3.5, cz - 1.25), (cx + 1.0, 4.0, cz + 1.5), up=heap(SAMP)))
    # The lug handles at its ends.
    el += [panel(sc, "lug", wood, (1.0, 3.25, cz - 1.0), (2.25, 4.5, cz + 1.0), up=rim),
           panel(sc, "lug", wood, (13.75, 3.25, cz - 1.0), (15.0, 4.5, cz + 1.0), up=rim)]
    # The spoon in it: its bowl on the samp, its neck rising to its handle laid over the front rim.
    handle = lengthwise(CARVED)
    el += [panel(sc, "spoon_bowl", flat(CARVED), (9.25, 3.5, 8.75), (11.25, 4.0, 11.25), up=solid(CARVED, 1)),
           panel(sc, "spoon_neck", handle, (9.875, 4.0, 7.75), (10.625, 4.75, 8.75)),
           panel(sc, "spoon_handle", handle, (9.875, 4.5, 4.5), (10.625, 5.25, 7.75)),
           panel(sc, "spoon_knob", handle, (9.75, 4.375, 3.75), (10.75, 5.375, 4.5))]
    # The small carved bowl at the front left, empty, a spoon laid across it from rim to rim.
    sx, sz = 4.5, 3.0
    el += round_layer(sc, "small_foot", wood, sx, sz, 2.0, 0.0, 0.5, rz=1.75)
    el.append(panel(sc, "small_floor", inside, (sx - 2.0, 0.25, sz - 1.75), (sx + 2.0, 0.5, sz + 1.75), down=wood))
    el += round_ring(sc, "small_bowl", wood, inside, sx, sz, 3.0, 2.0, 0.5, 2.5, tops=rim, rz=2.75, hz=1.75)
    el += [panel(sc, "spoon_handle", handle, (1.25, 2.5, sz - 0.375), (5.5, 3.25, sz + 0.375)),
           panel(sc, "spoon_bowl", flat(CARVED), (5.5, 2.5, sz - 1.0), (7.75, 3.0, sz + 1.0), up=solid(CARVED, 1))]
    # The shallow round twined food basket of cranberries at the front right.
    bx, bz = 12.0, 3.0
    el += round_ring(sc, "food_basket", twined(), solid(GRASS, 0), bx, bz, 2.75, 2.0, 0.0, 1.75, tops=braid())
    el.append(panel(sc, "berries", solid(BERRY, 0), (bx - 2.0, 0.0, bz - 2.0), (bx + 2.0, 1.5, bz + 2.0), up=berries()))
    el.append(panel(sc, "berry", berries(), (bx - 1.0, 1.5, bz - 1.0), (bx + 1.0, 2.25, bz + 1.0)))
    sc.models["whole"] = el
    return sc


def incised_earthenware_pot():
    """The Incised Earthenware Pot, facing north: a round-bodied cooking pot with a rounded base, set down into a bed of
    ash and held among four hearthstones, a charred stick and charcoal under it. Its round profile steps out from the
    base to its widest at the shoulder, in to a short neck and out again in a round collar flaring to its rim; the rim
    edge rises in four gentle points over the front, back and sides, and a shallow incised band (an incised line, a row
    of impressions, another line) runs round it under the rim. Warm terracotta clay, clouded by the fire in soft
    patches, more towards its base; corn and beans in it, under the round mouth. The support and the stepped voxel
    profile are a game adaptation."""
    sc = Sculpt("incised_earthenware_pot", 5106, 128)
    cx, cz = 8.0, 8.0
    el = round_layer(sc, "ash", solid(ASH, 1), cx, cz, 7.0, 0.0, 0.5, up=ash())
    # A charred stick and two lumps of charcoal on the ash.
    char = lengthwise(CHAR)
    el.append(panel(sc, "char", char, (3.5, 0.5, 7.25), (12.5, 1.5, 8.25), rot=fa.rotation((8.0, 1.0, 7.75), "y", 22.5)))
    el += [panel(sc, "charcoal", char, (4.5, 0.5, 4.25), (6.0, 1.25, 5.5)), panel(sc, "charcoal", char, (10.25, 0.5, 10.5), (11.5, 1.5, 12.0))]
    # The pot, layer by layer from its rounded base, in to a short neck and out again to its rim.
    near, body, ledge = fired_clay(1), fired_clay(0), solid(CLAY, 3)
    for paint, r, y0, y1 in ((near, 1.5, 1.0, 1.75), (near, 2.75, 1.75, 2.75), (near, 3.75, 2.75, 3.75), (near, 4.5, 3.75, 5.0),
                             (body, 5.0, 5.0, 7.0), (body, 5.5, 7.0, 9.0), (body, 5.0, 9.0, 10.0), (body, 4.5, 10.0, 10.75)):
        el += round_layer(sc, "body", paint, cx, cz, r, y0, y1, up=ledge, down=paint)
    # The collar: round outside and in, flaring a little to the rim, the incised band round its upper part.
    lining, rim = solid(CLAY, 1), solid(CLAY, 2)
    el += round_rim(sc, "collar", body, lining, cx, cz, 4.75, 3.25, 10.75, 12.25, top=ledge)
    el += round_rim(sc, "rim", incised_collar(), lining, cx, cz, 5.0, 3.25, 12.25, 14.25, top=rim)
    # The rim rising in four gentle points over its front, back and sides: a low rise the wall's thickness, a lower
    # crest on it.
    for axis, sign in (("z", -1), ("z", 1), ("x", -1), ("x", 1)):
        for half, y0, y1 in ((1.75, 14.25, 14.75), (0.75, 14.75, 15.25)):
            a0, a1 = sorted((sign * 3.25, sign * 5.0))
            x0, x1, z0, z1 = (-half, half, a0, a1) if axis == "z" else (a0, a1, -half, half)
            el.append(panel(sc, "peak", body, (cx + x0, y0, cz + z0), (cx + x1, y1, cz + z1), up=rim))
    el += round_layer(sc, "stew", solid(STEW, 0), cx, cz, 3.25, 10.75, 13.25, up=stew())
    # The hearthstones round its base, each a rounded stone with a smaller cap.
    stone = fieldstone()
    for x, z, w, h in ((8.0, 2.0, 4.5, 4.0), (2.25, 8.5, 4.0, 4.5), (13.75, 8.0, 4.0, 4.0), (8.5, 13.75, 4.5, 4.5)):
        el += octagon(sc, "stone", stone, x, z, w / 2, w / 2 - 0.25, 1.0, 0.5, 0.5 + h)
        el += octagon(sc, "stone_cap", stone, x, z, w / 2 - 0.75, w / 2 - 1.0, 0.5, 0.5 + h, 1.25 + h)
    sc.models["whole"] = el
    return sc


def corn_drying_frame():
    """The Corn Drying Frame, facing north: two forked sapling uprights, a pole laid in their forks and lashed there
    with bark cord, a rail lashed behind them low down, and from the pole three cords braided of husks, each hung with
    three ears of corn (yellow, white and red) by their pulled-back husks. A decoration: unlike the Drying Rack it dries
    nothing."""
    sc = Sculpt("corn_drying_frame", 5107, 128)
    pole, lash = lengthwise(POLE), wrapped(BARK_CORD)
    el = []
    for x in (1.0, 13.5):
        el.append(panel(sc, "post", pole, (x, 0.0, 7.25), (x + 1.5, 13.0, 8.75)))
        # The fork: two prongs leaning apart front and back, the pole lying between them.
        for lean in (-22.5, 22.5):
            el.append(panel(sc, "prong", pole, (x + 0.25, 12.5, 7.5), (x + 1.25, 15.75, 8.5), rot=fa.rotation((x + 0.75, 12.75, 8.0), "x", lean)))
        el.append(panel(sc, "lashing", lash, (x - 0.25, 12.5, 7.0), (x + 1.75, 13.5, 9.0)))
        el.append(panel(sc, "lashing", lash, (x - 0.25, 2.0, 8.75), (x + 1.75, 3.5, 9.25)))
    el += crossed_x(sc, "pole", pole, 0.5, 15.5, 14.25, 8.0, 1.75)
    el.append(panel(sc, "rail", pole, (0.75, 2.25, 9.25), (15.25, 3.25, 10.25)))
    # The braids and their ears.
    braid_ = husk_braid()
    for x, colours in ((4.0, (CORN, WHITE_CORN, RED_CORN)), (8.0, (WHITE_CORN, CORN, CORN)), (12.0, (RED_CORN, CORN, WHITE_CORN))):
        el.append(panel(sc, "braid", braid_, (x - 0.5, 6.5, 7.25), (x + 0.5, 13.5, 8.75)))
        el.append(panel(sc, "knot", braid_, (x - 0.75, 13.25, 7.125), (x + 0.75, 14.0, 8.875)))
        # One ear hung in front of the braid, one behind it lower down, and one from its end.
        el += hanging_ear(sc, x, 12.75, 6.75, colours[0])
        el += hanging_ear(sc, x, 10.5, 9.25, colours[1])
        el += hanging_ear(sc, x, 6.5, 8.0, colours[2])
    sc.models["whole"] = el
    return sc


def wooden_corn_mortar():
    """The Wooden Corn Mortar, facing north: a log stood on end and hollowed into a deep bowl, round in section, a little
    waisted, its bark running up it and its rim the bare end grain (bark, a pale sapwood ring, the heartwood) round the
    hollow; cracked corn in its bowl; a long wooden pestle standing in it and leaning, taller than the mortar, its
    lashed stone end down in the corn and showing over the rim; an ear of corn lying at its foot and kernels spilled.
    Not the alchemy Mortar and Pestle (a small stone bowl held in the hand): this stands on the floor and grinds
    nothing."""
    sc = Sculpt("wooden_corn_mortar", 5108, 128)
    bark = bark_lengthwise()
    cx, cz = 8.0, 9.0
    el = []
    for r, y0, y1 in ((5.5, 0.0, 1.0), (5.0, 1.0, 3.0), (4.75, 3.0, 5.5), (5.0, 5.5, 6.5)):
        el += round_layer(sc, "log", bark, cx, cz, r, y0, y1)
    el += round_ring(sc, "rim", bark, lengthwise(LOG_INNER), cx, cz, 5.0, 2.75, 6.5, 9.0, tops=GRAIN_TOPS)
    el.append(panel(sc, "corn", solid(CORN, 0), (cx - 2.75, 6.5, cz - 2.75), (cx + 2.75, 7.5, cz + 2.75), up=heap(CORN, 2)))
    # The pestle, standing in the corn and leaning to the east: its stone end rounded, lashed to the shaft.
    lean = fa.rotation((cx - 1.0, 7.25, cz), "z", -22.5)
    px = cx - 1.0
    stone = fieldstone()
    el += [panel(sc, "pestle_stone", stone, (px - 1.75, 7.25, cz - 1.0), (px + 1.75, 10.75, cz + 1.0), rot=lean),
           panel(sc, "pestle_stone", stone, (px - 1.0, 7.25, cz - 1.75), (px + 1.0, 10.75, cz + 1.75), rot=lean),
           panel(sc, "pestle_lash", wrapped(BARK_CORD), (px - 0.875, 10.75, cz - 0.875), (px + 0.875, 11.75, cz + 0.875), rot=lean),
           panel(sc, "pestle", lengthwise(LOG_WOOD), (px - 0.625, 11.75, cz - 0.625), (px + 0.625, 16.25, cz + 0.625), rot=lean)]
    # An ear of corn at the front left and kernels spilled by it.
    el += lying_ear(sc, 1.5, 7.0, 0.0, 1.75)
    for x, z in ((7.5, 0.75), (8.25, 2.25), (2.5, 3.0)):
        el.append(panel(sc, "kernel", grains(CORN), (x, 0.0, z), (x + 0.75, 0.5, z + 0.75)))
    sc.models["whole"] = el
    return sc


BUILDERS = {"lidded_twined_basket": lidded_twined_basket, "twined_pack_basket": twined_pack_basket,
            "birch_bark_box": birch_bark_box, "carved_bowls_and_spoons": carved_bowls_and_spoons,
            "incised_earthenware_pot": incised_earthenware_pot, "corn_drying_frame": corn_drying_frame,
            "wooden_corn_mortar": wooden_corn_mortar}
_built = {}


def build(block):
    """The piece's Sculpt, its pieces packed into the smallest texture of 64 or 128 texels square they fit."""
    if block not in _built:
        try:
            _built[block] = ppd.planned(BUILDERS[block], 64)
        except ValueError:
            _built[block] = ppd.planned(BUILDERS[block], 128)
    return _built[block]


def _image_of(name):
    return build(name).atlas.img if name in BUILDERS else None


# ---------------------------------------------------------------- the Rush Floor Mat

MAT = "rush_floor_mat"
MAT_TEXTURES = {"top": MAT, "border": f"{MAT}_border"}
BODY, EDGE = 0.75, 1.0      # the mat's height, and its bound edge's
BORDER = 2.0                # how far in from the block's side its bound edge reaches
SIDES = ("north", "east", "south", "west")
# Each mat part's box in the model drawn with its rushes along z (axis z): the middle, each side's edge and each corner
# (named by its two sides).
REGIONS = {"middle": (BORDER, BORDER, 16 - BORDER, 16 - BORDER),
           "north": (BORDER, 0.0, 16 - BORDER, BORDER), "south": (BORDER, 16 - BORDER, 16 - BORDER, 16.0),
           "west": (0.0, BORDER, BORDER, 16 - BORDER), "east": (16 - BORDER, BORDER, 16.0, 16 - BORDER),
           "north_west": (0.0, 0.0, BORDER, BORDER), "north_east": (16 - BORDER, 0.0, 16.0, BORDER),
           "south_west": (0.0, 16 - BORDER, BORDER, 16.0), "south_east": (16 - BORDER, 16 - BORDER, 16.0, 16.0)}
CORNERS = ("north_west", "north_east", "south_west", "south_east")
# A blockstate turn of 90 degrees (axis x) carries the model's north side to the world's east, and so on round.
TURN = {"north": "east", "east": "south", "south": "west", "west": "north"}


def mat_texture():
    """The mat's rushes, read by position, seamless: long rushes a texel wide running along the texture's height in a
    fixed run of two close tones (now and then a shade down), and across them every eight rows a row of twining, one
    tone darker than the rushes, a stitch every other texel; nothing else crosses them, so the rushes read first."""
    from PIL import Image
    img = Image.new("RGBA", (16, 16))
    rush = (3, 2, 3, 3, 2, 3, 2, 2)
    for y in range(16):
        for x in range(16):
            if y % 8 == 7:
                c = shade(GRASS, rush[x % 8] - 1 - (x % 2))
            else:
                c = shade(GRASS, rush[x % 8])
            img.putpixel((x, y), (*c, 255))
    return img


def mat_border_texture():
    """The mat's bound edge, read by position, seamless: the rushes turned over and wrapped with cord on the slant, a
    cord every four texels across the diagonal, the folded rushes between lit, their own tone and shaded."""
    from PIL import Image
    img = Image.new("RGBA", (16, 16))
    for y in range(16):
        for x in range(16):
            d = (x + y) % 4
            c = shade(BARK_CORD, 2) if d == 0 else shade(GRASS, (4, 3, 1)[d - 1])
            img.putpixel((x, y), (*c, 255))
    return img


def mat_part(region, bound):
    """A mat part's element (axis z): its box at the mat's height, or the bound edge's when `bound`; its top and every side
    read their texture by position, so neighbouring parts and mats meet without a seam."""
    x0, z0, x1, z1 = REGIONS[region]
    h = EDGE if bound else BODY
    tex = "#border" if bound else "#top"
    faces = {"up": {"uv": [x0, z0, x1, z1], "texture": tex},
             "down": {"uv": [x0, 16 - z1, x1, 16 - z0], "texture": "#top", "cullface": "down"},
             "north": {"uv": [16 - x1, 16 - h, 16 - x0, 16], "texture": tex},
             "south": {"uv": [x0, 16 - h, x1, 16], "texture": tex},
             "west": {"uv": [z0, 16 - h, z1, 16], "texture": tex},
             "east": {"uv": [16 - z1, 16 - h, 16 - z0, 16], "texture": tex}}
    return {"from": [x0, 0.0, z0], "to": [x1, h, z1], "faces": faces}


def mat_model(elements):
    return {"parent": "minecraft:block/block", "ambientocclusion": False,
            "textures": {"top": rid(f"block/{MAT_TEXTURES['top']}"), "border": rid(f"block/{MAT_TEXTURES['border']}"),
                         "particle": rid(f"block/{MAT_TEXTURES['top']}")}, "elements": elements}


def mat_models():
    """{model name: model}: the middle, each side and corner plain (`_joined`) and bound (`_bound`), and the whole
    single mat (every side bound) the item shows."""
    out = {f"{MAT}_middle": mat_model([mat_part("middle", False)])}
    for region in SIDES + CORNERS:
        out[f"{MAT}_{region}_joined"] = mat_model([mat_part(region, False)])
        out[f"{MAT}_{region}_bound"] = mat_model([mat_part(region, True)])
    out[MAT] = mat_model([mat_part("middle", False)] + [mat_part(r, True) for r in SIDES + CORNERS])
    return out


def mat_blockstate():
    """The mat's multipart: for each axis, its middle, and each side's edge plain where it joins the mat beside it and
    bound where it does not; each corner bound where either of its sides is. Axis x draws the axis-z parts turned a
    quarter, its parts' sides read through the turn."""
    parts = []
    for axis, y in (("z", 0), ("x", 90)):
        turn = (lambda side: TURN[side]) if y else (lambda side: side)
        apply = (lambda name: {"model": rid(f"block/{name}"), **({"y": y} if y else {})})
        parts.append({"when": {"axis": axis}, "apply": apply(f"{MAT}_middle")})
        for side in SIDES:
            world = turn(side)
            parts.append({"when": {"axis": axis, world: "true"}, "apply": apply(f"{MAT}_{side}_joined")})
            parts.append({"when": {"axis": axis, world: "false"}, "apply": apply(f"{MAT}_{side}_bound")})
        for corner in CORNERS:
            a, b = (turn(s) for s in corner.split("_"))
            parts.append({"when": {"axis": axis, a: "true", b: "true"}, "apply": apply(f"{MAT}_{corner}_joined")})
            parts.append({"when": {"OR": [{"axis": axis, a: "false"}, {"axis": axis, b: "false"}]}, "apply": apply(f"{MAT}_{corner}_bound")})
    return {"multipart": parts}


# ---------------------------------------------------------------- files

DISPLAY = tdd.DISPLAY
MAT_DISPLAY = {"gui": {"rotation": [30, 225, 0], "translation": [0, 0, 0], "scale": [0.625, 0.625, 0.625]},
               "ground": {"rotation": [0, 0, 0], "translation": [0, 3, 0], "scale": [0.25, 0.25, 0.25]},
               "fixed": {"rotation": [90, 0, 0], "translation": [0, 0, 0], "scale": [0.5, 0.5, 0.5]},
               "thirdperson_righthand": {"rotation": [75, 45, 0], "translation": [0, 2.5, 0], "scale": [0.375, 0.375, 0.375]},
               "firstperson_righthand": {"rotation": [0, 45, 0], "translation": [0, 0, 0], "scale": [0.4, 0.4, 0.4]}}


def bounds(elements):
    return tdd.bounds(elements)


def assets(root, write, lang):
    """Each piece's block models, blockstate (facing and water for a floor piece; the mat's multipart), item model and
    item; and the names and JEI pages."""
    closed = fa.closing_writer(write, _image_of)
    models = root / "models" / "block"
    lang.update(tw.names())
    for block, spec in tw.PROPS.items():
        if spec["kind"] == "mat":
            for name, model in mat_models().items():
                write(models / f"{name}.json", model)
            write(root / "blockstates" / f"{block}.json", mat_blockstate())
            item = dict(mat_models()[MAT], display=MAT_DISPLAY)
            write(root / "models" / "item" / f"{block}.json", item)
        else:
            whole = d20d.separated(build(block).models["whole"])
            closed(models / f"{block}.json", fa.model(block, whole))
            write(root / "blockstates" / f"{block}.json",
                  {"variants": {f"facing={f},waterlogged={w}": tdd.turned(block, f) for f in FACINGS for w in ("false", "true")}})
            closed(root / "models" / "item" / f"{block}.json", fa.model(block, whole, DISPLAY))
        write(root / "items" / f"{block}.json", {"model": {"type": "minecraft:model", "model": rid(f"item/{block}")}})


def loot(out, write):
    """Each piece drops itself, once."""
    from decor_data import self_drop
    for block in tw.PROPS:
        write(out / f"{block}.json", self_drop(block))


def recipes(out, write):
    """The shapeless recipes, as agriculture_data.recipes writes SHAPELESS (once hooked, it writes these)."""
    for recipe in tw.SHAPELESS:
        write(out / f"{recipe['id']}.json", {"fabric:load_conditions": [{"condition": f"{MOD}:feature_enabled", "feature": tw.FEATURE}],
                                             "type": "minecraft:crafting_shapeless", "category": recipe["category"],
                                             "ingredients": recipe["inputs"], "result": {"id": rid(recipe["result"]), "count": recipe["count"]}})


def tags(tags):
    """Each piece's tool (thanksgiving_woodland.PROPS "tool")."""
    for block, spec in tw.PROPS.items():
        tags.add("block", f"minecraft:mineable/{spec['tool']}", rid(block))


def textures():
    out = {("block", block): build(block).atlas.img for block in BUILDERS}
    out[("block", MAT_TEXTURES["top"])] = mat_texture()
    out[("block", MAT_TEXTURES["border"])] = mat_border_texture()
    return out


# ---------------------------------------------------------------- standalone

def write_json(path, obj):
    """As tools/generate_material_data.write: every model gets model_writer.finish_elements; two-space JSON."""
    if isinstance(obj, dict) and obj.get("elements") and "models" in path.parts:
        model_writer.finish_elements(obj["elements"])
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(obj, indent=2) + "\n", encoding="utf-8")


def outputs():
    """{path relative to src/main/resources: bytes} of every resource this group owns (not the shared names, tags or
    manifest), as the pipeline writes them."""
    import io
    files = {}

    def capture(path, obj):
        if isinstance(obj, dict) and obj.get("elements") and "models" in path.parts:
            model_writer.finish_elements(obj["elements"])
        files[path.relative_to(RES).as_posix()] = (json.dumps(obj, indent=2) + "\n").encode("utf-8")
    assets(RES / "assets" / MOD, capture, {})
    loot(RES / "data" / MOD / "loot_table" / "blocks", capture)
    recipes(RES / "data" / MOD / "recipe", capture)
    for (kind, name), img in textures().items():
        buffer = io.BytesIO()
        img.save(buffer, format="PNG", optimize=True)
        files[f"assets/{MOD}/textures/{kind}/{name}.png"] = buffer.getvalue()
    return files


def generate():
    """Writes this group's own resources (outputs()) into the tree."""
    for rel, data in outputs().items():
        path = RES / rel
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_bytes(data)
    return sorted(outputs())


if __name__ == "__main__":
    for rel in generate():
        print(rel)

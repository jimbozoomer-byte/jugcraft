"""Generated assets for the Thanksgiving festival toys and lively props (tools/thanksgiving_toys.py;
docs/features/thanksgiving-toys.md).

Built from boxes at real size, one texel to a model pixel, in the Thanksgiving decorations' manner
(tools/thanksgiving_decor_data.py): each piece is one packed texture (block/<piece>) of pieces painted for the faces
they cover (pumpkin_patch_data.panel), flat in three to five tones, lit from the top left, patterns rather than noise.
It shares the decorations' pumpkin, leaf, garland and pie-stand painters and the Harvest Festival Grounds' and Pumpkin
Patch Market's palettes (willow, husk, twine, hessian, cream, wood, iron), so the toys sit beside the feast props.

Everything is drawn here by code; no Mojang texture is read, traced or copied, and nothing from the owner library is
used (its catalog was searched; the record says for what).

Writes, from agriculture_data.py (assets, loot, recipes, tags) and crop_textures.py (textures), or on its own
(`python3 tools/thanksgiving_toys_data.py write`, the same functions): each piece's block models, blockstate, item model
and item, name, hint and messages, self-dropping loot (the backdrop's from its first block only), recipe, axe tag and
texture.
"""
import json
import math
import sys
from pathlib import Path

import decor17_data as d17d
import decor20_data as d20d
import flora_art as fa
import pumpkin_patch_data as ppd
import thanksgiving_decor_data as tdd
import thanksgiving_toys as tt
from flora_art import Sculpt, pal, shade
from harvest_festival_data import (CARD, CREAM, HESSIAN, INK, TWINE, WHEAT, crossed_x, crossed_y, ears, gourd, pleats)
from haunted_midway_data import PUMPKIN, STEM
from pumpkin_patch_data import IRON, WOOD, flat, hay, lengthwise, panel, planks

MOD = "jugcraft"
FACINGS = tdd.FACINGS
smooth, solid, tagged, pumpkin = tdd.smooth, tdd.solid, tdd.tagged, tdd.pumpkin

# Palettes, darkest first.
HUSK = d20d.CORN_HUSK                                                   # dried corn husk
HUSK_DARK = pal("6a5432", "86704a", "a28a5e", "bca474")                 # husk toasted a shade darker
RUST = pal("4e1a0c", "702612", "92361a", "ae4c26", "c66636")            # rust-red paint and cloth
OCHRE = pal("6a4810", "8e6418", "b2842a", "cea440", "e2c066")           # ochre paint
MAPLEWOOD = pal("6a4a26", "8c6634", "ac8448", "c8a262", "dcbc80")       # pale turned and carved wood
WALNUT = tdd.WALNUT
HIDE = pal("8a7652", "aa9670", "c6b48c", "dccca6")                      # a drum's skin
TIN = tdd.TIN
BULB_OFF = pal("4e4a42", "6a655a", "868074", "a29c8e")                       # an unlit bulb: clear glass, grey
BULB_ON = tdd.AMBER_GLASS
LEAF_RED = tdd.RED_LEAF
WHITE_PUMPKIN = tdd.WHITE_PUMPKIN
TIE = pal("3a2814", "54391c", "6e4c26")                                 # dark twine ties
OAK = pal("5e4426", "7e6038", "9e7d4a", "b8945e", "ceac74")              # warm sawn oak timber, as vanilla oak's
TOY_RED = pal("5a160e", "7e2214", "a0321c", "bc4a28", "d06636")          # a toy's glossy red paint
TYRE = pal("5a4026", "6e5030", "86643c")                                 # a toy wheel's worn tread, a tone under maple
BODY_BRONZE = pal("3e2412", "5a3618", "744a22", "8e5e2e", "a8743a")      # a carved turkey's warm bronze
RUSSET = pal("4a200e", "6a3016", "8a4420", "a65a2c", "c0763c")           # russet feathers
OAK_DARK = pal("44301a", "5e4426", "7a5a32", "94703e", "aa844c")         # the frame's darker feet and braces
OLIVE = pal("3a4016", "525a20", "6a722a", "848a3a")                       # a muted olive leaf
FLINT_CORN = pal("5a1c0c", "8a3414", "b45a1e", "d48a2e", "e8b450")       # dried flint corn, rust to gold
HULL = pal("34210f", "4c3018", "664222", "80562e", "98693a")             # a model hull's oiled planking
SAILCLOTH = pal("a8946a", "c4b088", "dccaa2", "ece0c0")                 # warm cream sailcloth
WING_BAR = pal("6a4422", "866034", "a07a44")                             # the paler bars across a bronze wing


def rid(path):
    return path if ":" in path else f"{MOD}:{path}"


# ---------------------------------------------------------------- painters

def husk_streaks(palette=HUSK):
    """Dried husk leaves hanging straight down: strands two texels wide in two clean tones by turns, the face's left
    column lit and its right column shaded (light from the top left); no row is broken, so each strand runs the whole
    length of the leaf."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                if x == 0 and p.w > 2:
                    k = 3
                elif x == p.w - 1 and p.w > 2:
                    k = 1
                else:
                    k = 2 if (x // 2) % 2 == 0 else 3
                p.put(x, y, shade(palette, k))
    return tagged(paint, "husk_leaves", palette)


def husk_ledge(palette=HUSK):
    """The folded-over top of a tier of husk, seen from above: one lit tone, its far (back) row a tone darker."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                p.put(x, y, shade(palette, 2 if y == p.h - 1 and p.h > 2 else 3))
    return tagged(paint, "husk_ledge", palette)


def coil(palette=HUSK):
    """A coiled husk mat seen from above: rings a texel wide round its middle, lit and shaded by turns."""
    def paint(p):
        cx, cy = (p.w - 1) / 2, (p.h - 1) / 2
        for y in range(p.h):
            for x in range(p.w):
                ring = int(max(abs(x - cx) * (p.h / max(1, p.w)), abs(y - cy)))
                p.put(x, y, shade(palette, (3, 2, 1, 2)[ring % 4]))
    return tagged(paint, "coil", palette)


def windows():
    """A stern's gallery: a row of small leaded windows, cream panes in ochre frames, between dark walnut."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                if 0 < y < p.h - 1 and x % 3 != 0:
                    c = shade(CREAM, 2 if y == 1 else 1)
                elif 0 < y < p.h - 1:
                    c = shade(OCHRE, 2)
                else:
                    c = shade(WALNUT, 2 if y == 0 else 1)
                p.put(x, y, c)
    return tagged(paint, "windows")


def corrugated():
    """A washboard's tin: ribs across it a texel high, lit and shaded by turns."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                p.put(x, y, shade(TIN, 3 if y % 2 == 0 else 1))
    return tagged(paint, "corrugated")


def drum_skin():
    """A frame drum's skin: pale hide, a ring a tone darker a texel in from its edge, a lit top left."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                ring = min(x, y, p.w - 1 - x, p.h - 1 - y)
                k = 3 if ring == 0 and (x == 0 or y == 0) else 1 if ring in (0, 1) else 2
                p.put(x, y, shade(HIDE, k))
    return tagged(paint, "drum_skin")


def hessian_drape():
    """A show plinth's hessian cloth: a basket weave of two-texel squares in two tones, its hem (the foot row) bound in
    rust."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                if y == p.h - 1 and p.h > 2:
                    c = shade(RUST, 2)
                else:
                    c = shade(HESSIAN, 3 if (x // 2 + y // 2) % 2 == 0 else 2)
                p.put(x, y, c)
    return tagged(paint, "hessian_drape")


def show_card():
    """A show's entry card: cream card, its top row lit, its entry number a short inked stroke in its middle."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                ink = p.h >= 3 and y == p.h // 2 and x == p.w // 2
                p.put(x, y, shade(INK, 1) if ink else shade(CARD, 3 if y == 0 else 2))
    return tagged(paint, "show_card")


def dried_gourd():
    """A dried shaker gourd: ochre, a darker stripe every third column, its top row lit."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                k = 1 if x % 3 == 2 else 3 if y == 0 and p.h > 1 else 2
                p.put(x, y, shade(OCHRE, k))
    return tagged(paint, "dried_gourd")


def laced(shell, rope):
    """A drum's shell: `shell` paint, laced with `rope` in a zigzag between its hoops, its top row lit."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                t = x % 6
                lace = round(abs(t - 3) * (p.h - 1) / 3)
                c = shade(rope, 2) if y == lace else shade(shell, 3 if y == 0 else 2)
                p.put(x, y, c)
    return tagged(paint, "laced", shell, rope)


def timber(palette=OAK, edges=True):
    """Sawn timber along the piece, in vanilla's manner: the face its middle tone, its upper (or left) edge lit and its
    other edge shaded (unless `edges` is False, for a surface made of several boxes that must read as one); long grain
    lines a tone darker running along it, three or four texels apart and broken into long dashes that step a texel
    across where they break, so the grain is broad and calm (never stripes by turns)."""
    def paint(p):
        along_x = p.w >= p.h
        span, across = (p.w, p.h) if along_x else (p.h, p.w)
        lines = set()
        a, gap = 2, 0
        while a < across - 1:
            lines.add(a)
            a += (4, 3, 5)[gap % 3]
            gap += 1
        for a in range(across):
            for s in range(span):
                if edges and across > 2 and a == 0:
                    k = 3
                elif edges and across > 2 and a == across - 1:
                    k = 1
                else:
                    k = 2
                    run = (s + 5 * a) % 11
                    # A grain line: dark along most of its run, stepping a texel across at its break.
                    if (a in lines and run < 8) or (a - 1 in lines and run >= 9):
                        k = 1
                x, y = (s, a) if along_x else (a, s)
                p.put(x, y, shade(palette, k))
    return tagged(paint, "timber", palette, edges)


def end_grain(palette=OAK):
    """A beam's sawn end: square growth rings round its heart in two tones, its rim (the outer row) a tone darker."""
    def paint(p):
        cx, cy = (p.w - 1) / 2, (p.h - 1) / 2
        for y in range(p.h):
            for x in range(p.w):
                if x in (0, p.w - 1) or y in (0, p.h - 1):
                    k = 1
                else:
                    ring = int(max(abs(x - cx), abs(y - cy)) + 0.5)
                    k = 3 if ring % 2 == 0 else 2
                p.put(x, y, shade(palette, k))
    return tagged(paint, "end_grain", palette)


def rim(palette, sides):
    """A wheel's face as one of its crossed boxes shows it: its own wood, a tread a texel deep along `sides` ("lr": its
    left and right columns, "tb": its top and bottom rows), lit at the top left."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                edge = (sides == "lr" and x in (0, p.w - 1)) or (sides == "tb" and y in (0, p.h - 1))
                if edge:
                    c = shade(TYRE, 2 if (x == 0 or y == 0) else 0)
                else:
                    c = shade(palette, 3 if (x + y) <= 2 else 2)
                p.put(x, y, c)
    return tagged(paint, "rim", palette, sides)


def folded_wing(palette, bars):
    """A folded wing seen from the side: its coverts plain `palette` above, lit along the top row; its primaries below
    in broad bars, two rows of `bars` and two of `palette`'s shade by turns (no checker)."""
    def paint(p):
        split = max(1, int(p.h * 0.55))
        for y in range(p.h):
            for x in range(p.w):
                if y < split:
                    c = shade(palette, 3 if y == 0 else 2)
                else:
                    c = shade(bars, 1) if ((y - split) // 2) % 2 == 0 else shade(palette, 1)
                p.put(x, y, c)
    return tagged(paint, "folded_wing", palette, bars)


def plumage(palette):
    """Close body feathers, calm: the feathers' own tone, every third row a row of feather tips a tone lit on two texels
    of four, staggered row by row; the top row lit."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                tip = y % 3 == 0 and (x + 2 * (y // 3)) % 4 < 2
                p.put(x, y, shade(palette, 3 if y == 0 or tip else 2))
    return tagged(paint, "plumage", palette)


# ---------------------------------------------------------------- shared parts

def crossed_z(sc, name, painter, cx, cy, d, z0, z1, rot=None, **kw):
    """A round thing `d` across lying along z from z0 to z1 about (cx, cy): two boxes crossed (as crossed_x and
    crossed_y), so it reads as an octagon end on."""
    r, c = d / 2, round(d / 5 * 2) / 2
    return [panel(sc, name, painter, (cx - r, cy - r + c, z0), (cx + r, cy + r - c, z1), rot=rot, **kw),
            panel(sc, name, painter, (cx - r + c, cy - r, z0), (cx + r - c, cy + r, z1), rot=rot, **kw)]


def toy_wheel(sc, x0, x1, cy, cz, d, palette=MAPLEWOOD):
    """A turned wooden toy wheel standing across x (x0..x1) about its axle at (cy, cz): a round disc in three crossed
    boxes (wide, square and tall, the corners stepped off twice), each a little thinner than the last so the face is
    gently domed and no two faces share a plane; its face pale polished wood, its tread a tone darker."""
    r = d / 2
    tread = flat(TYRE)
    face = smooth(palette, glint=True)
    el = []
    for (hw, hh), inset in (((r, r * 0.55), 0.0), ((r * 0.78, r * 0.78), 0.1), ((r * 0.55, r), 0.2)):
        el.append(panel(sc, "wheel", tread, (x0 + inset, cy - hh, cz - hw), (x1 - inset, cy + hh, cz + hw), east=face, west=face))
    return el


def rot_y(angle, origin=(8.0, 0.0, 8.0)):
    return fa.rotation(origin, "y", angle) if angle else None


def shifted(elements, dx=0.0, dy=0.0, dz=0.0):
    out = []
    for e in elements:
        c = dict(e, **{"from": [e["from"][0] + dx, e["from"][1] + dy, e["from"][2] + dz],
                       "to": [e["to"][0] + dx, e["to"][1] + dy, e["to"][2] + dz]})
        if "rotation" in e:
            o = e["rotation"]["origin"]
            c["rotation"] = dict(e["rotation"], origin=[o[0] + dx, o[1] + dy, o[2] + dz])
        out.append(c)
    return out


# ---------------------------------------------------------------- 1. Corn Husk Dolls

def husk_doll(sc, cx, cz, scale, husk_palette, shawl_palette=None, kerchief=False, sheaf=False):
    """A faceless corn husk doll standing at (cx, cz) (`scale` 1.0 is the taller): a long bell skirt of husk leaves
    hanging straight down, flaring in tiers to the floor, each tier's folded top a lit ledge; a dark twine tie round the
    waist and the neck; a slim husk bodice under a shawl of husk (dyed `shawl_palette`); arms of rolled husk straight
    out from the shoulders, each tied off with twine at the wrist and ending in a little tuft; a round husk head
    gathered to a tied knot on top; with a thin rust-dyed kerchief over the head (`kerchief`) and a sheaf of wheat held
    in one arm (`sheaf`). It has no face, as these dolls never do."""
    s = scale
    husk, tie, ledge = husk_streaks(husk_palette), flat(TIE), husk_ledge(husk_palette)
    el = []
    y = 0.5
    tiers = ((6.0, 1.5), (5.0, 1.5), (4.0, 1.5), (3.0, 1.0)) if s >= 1.0 else ((5.5, 1.5), (4.5, 1.5), (3.5, 1.5))
    for d, h in tiers:
        el += crossed_y(sc, "skirt", husk, cx, cz, d, y, y + h, up=ledge)
        y += h
    waist = y
    el += crossed_y(sc, "waist_tie", tie, cx, cz, 2.5, waist, waist + 0.5)
    bodice_top = waist + 0.5 + 1.75 * s
    el.append(panel(sc, "bodice", husk, (cx - 0.75, waist + 0.5, cz - 0.75), (cx + 0.75, bodice_top, cz + 0.75), up=ledge))
    shawl = husk_streaks(shawl_palette or husk_palette)
    el.append(panel(sc, "shawl", shawl, (cx - 1.5, bodice_top - 1.0, cz - 1.0), (cx + 1.5, bodice_top, cz + 1.0), up=husk_ledge(shawl_palette or husk_palette)))
    el.append(panel(sc, "shawl_point", shawl, (cx - 0.75, bodice_top - 2.0, cz - 1.0), (cx + 0.75, bodice_top - 1.0, cz - 0.75)))
    # The arms: one roll of husk through the shoulders, tied at each wrist, a tuft beyond each tie.
    arm_y = bodice_top - 0.875
    el.append(panel(sc, "arms", lengthwise(husk_palette), (cx - 3.0, arm_y, cz - 0.375), (cx + 3.0, arm_y + 0.75, cz + 0.375)))
    for side in (-1, 1):
        wx = cx + side * 2.25
        el.append(panel(sc, "wrist_tie", tie, (wx - 0.25, arm_y - 0.125, cz - 0.5), (wx + 0.25, arm_y + 0.875, cz + 0.5)))
        tx0, tx1 = sorted((cx + side * 3.0, cx + side * 3.5))
        el.append(panel(sc, "tuft", husk_ledge(husk_palette), (tx0, arm_y - 0.25, cz - 0.5), (tx1, arm_y + 1.0, cz + 0.5)))
    el += crossed_y(sc, "neck_tie", tie, cx, cz, 1.5, bodice_top, bodice_top + 0.5)
    head_d = 3.0
    h0, head_top = bodice_top + 0.5, bodice_top + 0.5 + head_d
    el += crossed_y(sc, "head", husk, cx, cz, head_d, h0, head_top, up=ledge)
    el.append(panel(sc, "knot", tie, (cx - 0.5, head_top, cz - 0.5), (cx + 0.5, head_top + 0.5, cz + 0.5)))
    el.append(panel(sc, "topknot", husk_ledge(husk_palette), (cx - 0.375, head_top + 0.5, cz - 0.375), (cx + 0.375, head_top + 1.25, cz + 0.375)))
    if kerchief:
        cloth, r = flat(RUST), head_d / 2 + 0.25
        el.append(panel(sc, "kerchief", cloth, (cx - r, head_top - 0.25, cz - r + 0.75), (cx + r, head_top + 0.25, cz + r)))
        el.append(panel(sc, "kerchief", cloth, (cx - r, h0 + 0.75, cz + r - 0.5), (cx + r, head_top - 0.25, cz + r)))
        for side in (-1, 1):
            x0 = cx + side * (r - 0.25)
            el.append(panel(sc, "kerchief", cloth, (min(x0, x0 + side * 0.5), h0 + 1.25, cz - r + 0.75), (max(x0, x0 + side * 0.5), head_top - 0.25, cz + r - 0.5)))
        el.append(panel(sc, "kerchief_knot", smooth(RUST), (cx - 0.5, h0 + 0.25, cz + r), (cx + 0.5, h0 + 1.0, cz + r + 0.5)))
        el.append(panel(sc, "kerchief_tail", cloth, (cx - 0.375, h0 - 1.25, cz + r + 0.125), (cx + 0.375, h0 + 0.25, cz + r + 0.375)))
    if sheaf:
        sx = cx + 1.5
        el.append(panel(sc, "sheaf_stalks", lengthwise(WHEAT), (sx - 0.375, arm_y - 3.0, cz - 1.25), (sx + 0.375, arm_y + 1.0, cz - 0.5)))
        el.append(panel(sc, "sheaf", ears(), (sx - 0.75, arm_y + 1.0, cz - 1.375), (sx + 0.75, arm_y + 3.0, cz - 0.375)))
        el.append(panel(sc, "sheaf_tie", tie, (sx - 0.5, arm_y - 0.25, cz - 1.375), (sx + 0.5, arm_y + 0.25, cz - 0.375)))
    return el


def corn_husk_dolls():
    """Corn Husk Dolls, facing north: a pair of faceless dolls of dried corn husk standing side by side on an oval
    coiled husk mat, the taller in pale husk with an ochre-dyed shawl, the shorter in toasted husk with a rust kerchief
    and a little sheaf of wheat."""
    sc = Sculpt("corn_husk_dolls", 4501, 128)
    # The mat: an oval of two boxes crossed, the narrower one's foot a hair above the floor so their bottoms never
    # share a plane.
    el = [panel(sc, "mat", flat(HUSK_DARK), (2.0, 0.0, 4.0), (14.0, 0.5, 13.0), up=coil(HUSK_DARK)),
          panel(sc, "mat", flat(HUSK_DARK), (1.25, 0.125, 5.0), (14.75, 0.5, 12.0), up=coil(HUSK_DARK))]
    el += husk_doll(sc, 10.25, 8.75, 1.0, HUSK, OCHRE)
    el += husk_doll(sc, 5.0, 8.0, 0.8, HUSK_DARK, kerchief=True, sheaf=True)
    sc.models["whole"] = el
    return sc


# ---------------------------------------------------------------- 2. Turkey Pull Toy

def fan_feather(sc, name, palette, pivot, length, width, z, angle):
    """One tail feather fanned out from `pivot` (x, y) at depth z..z + 0.5: `width` wide and `length` long, turned
    `angle` degrees about z (0 straight up), banded at its end and tipped with cream (tdd.tail_feather)."""
    px, py = pivot
    rot = fa.rotation((px, py, z + 0.25), "z", angle) if angle else None
    return panel(sc, name, tdd.tail_feather(palette), (px - width / 2, py, z), (px + width / 2, py + length, z + 0.5), rot=rot)


def turkey_pull_toy():
    """The Turkey Pull Toy, facing north: a carved and painted wooden turkey on a light toy cart. A round body of warm
    bronze with a breast stepped forward in rounded rings, russet barred wings folded at its sides, a pale blue head on
    a short neck with a dark eye each side, a yellow beak, a red snood over it and a red wattle down its throat; behind
    it a broad tail fanned up in two rows of banded feathers, bronze tipped with cream behind and russet and orange in
    front. It stands on a pale maple cart on a through-axle at each end with four round turned wheels and walnut hub
    caps; a twine pull cord drops from an iron eye at the front of the cart to the floor and runs along it to a red
    wooden bead."""
    sc = Sculpt("turkey_pull_toy", 4502, 128)
    oak = timber(MAPLEWOOD)
    el = [panel(sc, "cart", oak, (4.0, 2.75, 2.5), (12.0, 3.75, 13.0), north=end_grain(MAPLEWOOD), south=end_grain(MAPLEWOOD))]
    # The through-axles under the cart, a wheel near each end and a hub cap over each end.
    for z in (5.0, 10.75):
        el.append(panel(sc, "axle", flat(WALNUT), (1.75, 1.75, z - 0.5), (14.25, 2.75, z + 0.5)))
        el += toy_wheel(sc, 2.0, 3.0, 2.25, z, 4.5)
        el += toy_wheel(sc, 13.0, 14.0, 2.25, z, 4.5)
        el.append(panel(sc, "hub", smooth(WALNUT), (1.5, 1.5, z - 0.75), (2.0, 3.0, z + 0.75)))
        el.append(panel(sc, "hub", smooth(WALNUT), (14.0, 1.5, z - 0.75), (14.5, 3.0, z + 0.75)))
    # The turkey: a round body sat on the cart, its breast stepped forward in two rounded rings, its rump narrowing.
    body = plumage(BODY_BRONZE)
    el += crossed_z(sc, "body", body, 8.0, 7.25, 7.0, 5.0, 10.25)
    el += crossed_z(sc, "breast", body, 8.0, 7.75, 6.0, 4.0, 5.0)
    el += crossed_z(sc, "breast", body, 8.0, 8.0, 4.0, 3.25, 4.0)
    el += crossed_z(sc, "rump", body, 8.0, 7.75, 6.0, 10.25, 11.25)
    el += crossed_z(sc, "rump", body, 8.0, 8.0, 4.5, 11.25, 12.0)
    wing = folded_wing(RUSSET, WING_BAR)
    el += [panel(sc, "wing", wing, (3.75, 5.0, 6.0), (4.5, 9.5, 10.5)), panel(sc, "wing", wing, (11.5, 5.0, 6.0), (12.25, 9.5, 10.5))]
    # Its feet on the cart, before the breast.
    beak = flat(tdd.BEAK)
    el += [panel(sc, "foot", beak, (6.25, 3.75, 2.75), (7.25, 4.25, 4.25)), panel(sc, "foot", beak, (8.75, 3.75, 2.75), (9.75, 4.25, 4.25))]
    # The neck and head, forward over the breast; the beak, the snood hanging beside it and the wattle down the throat.
    skin = flat(tdd.TURKEY_HEAD)
    el.append(panel(sc, "neck", skin, (7.0, 9.5, 3.75), (9.0, 13.0, 5.75)))
    eye = tdd.face(tdd.TURKEY_HEAD, [(1, 1)], glint=True)
    el.append(panel(sc, "head", skin, (6.75, 12.5, 2.75), (9.25, 15.0, 5.5), east=eye, west=eye))
    el.append(panel(sc, "beak", beak, (7.5, 13.0, 1.75), (8.5, 14.0, 2.75)))
    red = flat(tdd.WATTLE)
    el += [panel(sc, "snood", red, (8.5, 11.75, 2.0), (9.0, 14.25, 2.5)),
           panel(sc, "wattle", red, (7.25, 10.5, 2.75), (8.75, 12.75, 3.75))]
    # The tail: five broad bronze feathers fanned from behind the rump, a sixteenth of a turn apart, and five shorter
    # ones in front of them in russet and orange, each feather at its own depth.
    pivot = (8.0, 8.0)
    for i, angle in enumerate((0.0, -22.5, 22.5, -45.0, 45.0)):
        el.append(fan_feather(sc, "fan_long", BODY_BRONZE, pivot, 7.5, 3.0, 12.75 + 0.6 * ((i + 1) // 2) / 2, angle))
    for i, (angle, palette) in enumerate(((0.0, tdd.ORANGE_LEAF), (-22.5, RUSSET), (22.5, RUSSET), (-45.0, tdd.ORANGE_LEAF), (45.0, tdd.ORANGE_LEAF))):
        el.append(fan_feather(sc, "fan_short", palette, pivot, 5.5, 2.25, 12.0 + 0.6 * ((i + 1) // 2) / 2 - 0.6, angle))
    # The pull cord: from an iron eye on the cart's front edge straight down to the floor, round a short bend, along
    # the floor and into the red bead that is its handle; each length overlaps the next.
    cord = flat(TWINE)
    el += [panel(sc, "eye", flat(IRON), (7.5, 2.5, 2.0), (8.5, 3.25, 2.5)),
           panel(sc, "cord", cord, (7.75, 0.75, 2.0), (8.25, 2.75, 2.5)),
           panel(sc, "cord", cord, (7.8, 0.45, 1.6), (8.2, 1.35, 2.4), rot=fa.rotation((8.0, 0.9, 2.0), "x", 45.0)),
           panel(sc, "cord", cord, (7.75, 0.0, 1.25), (8.25, 0.5, 2.0))]
    # The bead: a round red bead, a box with a narrower band round its middle (no shared faces with the floor).
    el += [panel(sc, "bead", smooth(TOY_RED, glint=True), (7.25, 0.0, -0.25), (8.75, 1.5, 1.25)),
           panel(sc, "bead", smooth(TOY_RED), (7.0, 0.375, 0.0), (9.0, 1.125, 1.0))]
    sc.models["whole"] = el
    return sc


# ---------------------------------------------------------------- 3. Spinning Top

# The top's rings from its tip up: (name, across, height, painter key). A weighted top in a clean double cone: a
# visible iron tip, a red cone widening half a pixel a side every half pixel up (so it tapers smoothly) to one broad
# cream band round its widest part, about a third of the way down from its top, then a low red dome and a short walnut
# stem with a red knob to spin it by. Every lower ring's outer corner lies on one line (it widens as fast as it
# rises), so lying_top's 45 degree tip lays that line on the floor: at rest it lies along its cone and its band's rim.
TOP_RINGS = (("tip", 1.0, 0.5, "iron"), ("ferrule", 1.5, 0.5, "iron"), ("cone", 3.0, 0.5, "red"), ("cone", 4.0, 0.5, "red"),
             ("cone", 5.0, 0.5, "red"), ("cone", 6.0, 0.5, "red"), ("band", 7.0, 0.5, "band"), ("shoulder", 8.0, 1.0, "band"),
             ("dome", 6.5, 0.5, "red"), ("crown", 4.5, 0.5, "crown"), ("stem", 1.5, 1.0, "wood"), ("knob", 2.5, 0.75, "red"))


def polished(palette):
    """Turned and polished wood or paint round a ring: its own tone, the top row lit and the foot a tone darker (no
    stripes, so the ring reads as one smooth band)."""
    return smooth(palette)


def quartered():
    """The crown's painted top seen from above: four quarters, red and cream by turns about its middle, so a spin
    frame shows the paint turning; a texel lit at the top left."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                red = (x < p.w / 2) != (y < p.h / 2)
                c = shade(TOY_RED, 3) if red else shade(CREAM, 3)
                if (x, y) == (0, 0):
                    c = shade(CREAM, 4) if not red else shade(TOY_RED, 4)
                p.put(x, y, c)
    return tagged(paint, "quartered")


def top_painters():
    return {"iron": flat(IRON), "wood": polished(WALNUT), "red": polished(TOY_RED), "band": polished(CREAM), "crown": polished(TOY_RED)}


def top_caps():
    """The tops of the rings seen from above (the up faces): each ring's own paint lit, the crown quartered so a spin
    frame shows the paint turning."""
    return {"iron": flat(IRON), "wood": solid(WALNUT, 3), "red": solid(TOY_RED, 3), "band": solid(CREAM, 3), "crown": quartered()}


def upright_top(sc, angle):
    """The top standing on its tip at the block's middle, turned `angle` degrees about its axis (a frame of its spin):
    each ring two boxes crossed, its crown's top painted in quarters so the turning shows."""
    paints, caps = top_painters(), top_caps()
    el, y = [], 0.0
    for name, d, h, key in TOP_RINGS:
        rot = rot_y(angle)
        if d <= 1.5:
            el.append(panel(sc, name, paints[key], (8.0 - d / 2, y, 8.0 - d / 2), (8.0 + d / 2, y + h, 8.0 + d / 2), rot=rot))
        else:
            el += crossed_y(sc, name, paints[key], 8.0, 8.0, d, y, y + h, rot=rot, up=caps[key])
        y += h
    return el


def lying_top(sc):
    """The top at rest, fallen on its side with its handle to the west: built along x, then tipped 45 degrees so its
    tip and the rim of its body both rest on the floor."""
    paints = top_painters()
    el, x = [], 0.0
    for name, d, h, key in TOP_RINGS:
        if d <= 1.5:
            el.append(panel(sc, name, paints[key], (x, -d / 2, 8.0 - d / 2), (x + h, d / 2, 8.0 + d / 2)))
        else:
            el += crossed_x(sc, name, paints[key], x, x + h, 0.0, 8.0, d, east=solid(MAPLEWOOD, 3) if key == "wood" else paints[key])
        x += h
    # Its axis runs from the tip (x 0, y 0) to the knob; tip it so the knob rises to the west: mirror x, then turn.
    mirrored = []
    for e in el:
        f, t = e["from"], e["to"]
        faces = dict(e["faces"])
        faces["east"], faces["west"] = e["faces"].get("west"), e["faces"].get("east")
        mirrored.append(dict(e, **{"from": [-t[0], f[1], f[2]], "to": [-f[0], t[1], t[2]], "faces": {k: v for k, v in faces.items() if v}}))
    turned = [dict(e, rotation=fa.rotation((0.0, 0.0, 8.0), "z", -45.0)) for e in mirrored]
    x0, y0, _, x1, _, _ = tdd.bounds(turned)
    # Lifted a tenth of a pixel more, the push that keeps its rings' faces apart (decor20_data.separated) dipping none
    # of them through the floor.
    # Quantise computed trigonometric offsets so Windows and Linux libm agree
    # in the exported JSON (well below a visible texel).
    return shifted(turned, round(8.0 - (x0 + x1) / 2, 6), round(-y0 + 0.1, 6), 0.0)


def wobbling_top(sc):
    """The top leaning a sixteenth of a turn to the west on its tip as it slows (the blockstate turns it round)."""
    el = upright_top(sc, 0.0)
    # Tipped about the middle of its tip's foot; lifted the fifth of a pixel the tip's corner would dip.
    return shifted([dict(e, rotation=fa.rotation((8.0, 0.0, 8.0), "z", 22.5)) for e in el], 0.0, 0.2, 0.0)


def spinning_top():
    """The Spinning Top: a weighted top in a double cone, red with one broad cream band round its widest part, an iron
    tip, a low red dome painted in quarters on top and a walnut stem with a red knob. At rest it stands upright on its
    tip, as the owner asked (a weighted toy resting or spinning on its tip); spinning, it turns on its tip in four frames
    a sixteenth of a turn apart, then leans and precesses on its tip as it slows, and comes to rest upright again.
    (lying_top, the old rest pose lying on its side, is no longer used; it is left as it was, unedited.)"""
    sc = Sculpt("spinning_top", 4503, 64)
    sc.models["whole"] = upright_top(sc, 0.0)
    for k in range(tt.TOP["frames"]):
        angle = (0.0, 22.5, 45.0, -22.5)[k]
        sc.models[f"spin{k}"] = upright_top(sc, angle)
    sc.models["wobble"] = wobbling_top(sc)
    return sc


def top_state_model(spin):
    """The model a spin step shows: lying at 0, a turning frame while it spins upright, leaning while it wobbles."""
    if spin == 0:
        return "spinning_top"
    if spin <= tt.TOP["wobble"]:
        return "spinning_top_wobble"
    return f"spinning_top_spin{spin % tt.TOP['frames']}"


def top_state_y(facing, spin):
    """The turn of a spin step's model: lying along its facing at rest; leaning a quarter turn further round at each
    wobble step, so it precesses, and toward its own facing's west (where it falls) at the last."""
    base = FACINGS[facing]
    if 0 < spin <= tt.TOP["wobble"]:
        return (base + 90 * (spin - 1)) % 360
    return base if spin == 0 else 0


# ---------------------------------------------------------------- 4. Mayflower Model Ship

def canvas_cloth():
    """Sailcloth: warm cream, a seam a tone darker every fourth column (the cloths it is sewn from), its head row lit."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                k = 3 if (y == 0 and p.h > 1) else 1 if x % 4 == 3 else 2
                p.put(x, y, shade(SAILCLOTH, k))
    return tagged(paint, "canvas_cloth")


def cloth_edge():
    """The narrow top and foot of a row of sailcloth: one quiet tone, so a sail's rows never draw lit ledges across it."""
    return solid(SAILCLOTH, 1)


# The ship is laid out in its own keel frame (x along the keel from the bow at x 0 to the stern, z across it about z 8,
# heights as in the block) and every box is then turned SHIP_TURN degrees about the block's middle, so the keel runs
# corner to corner: the longest hull a block holds, its yards square across the keel, and from the front of the block
# the hull and sails are both seen three-quarter on.
SHIP_TURN = -45.0
SHIP_MASTS = {"fore": (4.25, 5.0, 13.75), "main": (8.25, 5.0, 15.75), "mizzen": (12.25, 7.5, 12.5)}


def ship_rot():
    return fa.rotation((8.0, 0.0, 8.0), "y", SHIP_TURN)


def keel_box(sc, name, painter, lo, hi, **over):
    """A box of the ship, given in its keel frame, turned with the rest of it."""
    return panel(sc, name, painter, lo, hi, rot=ship_rot(), **over)


def ship_sail(sc, mast, head, foot, width):
    """A square sail hanging from its yard before mast `mast` (keel x), from `head` down to `foot`, `width` across the
    keel: a full panel of canvas, and in front of it towards the bow two narrower panels a step and two forward, so it
    is bellied by the wind in two steps of depth; on a walnut yard square across the keel, a pixel longer each side."""
    cloth, mid = canvas_cloth(), 8.0
    x = mast - 0.375
    el = [keel_box(sc, "sail", cloth, (x - 0.5, foot, mid - width / 2), (x, head, mid + width / 2), up=cloth_edge(), down=cloth_edge())]
    for step, inset_w, inset_h in ((1, 1.0, 0.5), (2, 2.25, 1.0)):
        if width - 2 * inset_w > 1.0 and head - foot - 2 * inset_h > 0.5:
            el.append(keel_box(sc, "sail_belly", cloth, (x - 0.5 - 0.5 * step, foot + inset_h, mid - width / 2 + inset_w),
                               (x - 0.5 - 0.5 * (step - 1), head - inset_h, mid + width / 2 - inset_w), up=cloth_edge(), down=cloth_edge()))
    el.append(keel_box(sc, "yard", lengthwise(WALNUT), (x - 0.5, head, mid - width / 2 - 1.0), (x + 0.25, head + 0.5, mid + width / 2 + 1.0)))
    return el


def ship_line(sc, a, b, step=0.5):
    """A thin stay from `a` to `b` (keel x, height) in the ship's middle plane, drawn as a pixel line: short lengths
    stepping from one end to the other, each overlapping the next and a fifth of a pixel thicker or thinner across
    the keel than it by turns, so the line is unbroken and no two of its sides share a plane."""
    (xa, ya), (xb, yb) = a, b
    n = max(1, int(math.ceil(max(abs(xb - xa), abs(yb - ya)) / step)))
    el = []
    for i in range(n):
        x0, y0 = xa + (xb - xa) * i / n, ya + (yb - ya) * i / n
        x1, y1 = xa + (xb - xa) * (i + 1) / n, ya + (yb - ya) * (i + 1) / n
        half = 0.2 if i % 2 == 0 else 0.3
        el.append(keel_box(sc, "stay", flat(TWINE), (min(x0, x1) - 0.125, min(y0, y1) - 0.125, 8.0 - half),
                           (max(x0, x1) + 0.125, max(y0, y1) + 0.125, 8.0 + half)))
    return el


def hull_layers():
    """The hull as a shipwright's half model lays it, in level layers from the keel up: (y0, y1, [(x0, x1, half
    beam)]) in the keel frame. Long and low: it fills out from a narrow keel to its full beam at the deck, narrowing in
    steps to the bow and barely at the square stern."""
    return ((1.0, 1.75, [(2.0, 15.5, 0.5)]),
            (1.75, 2.75, [(1.0, 2.0, 0.75), (2.0, 16.0, 1.5)]),
            (2.75, 4.0, [(0.5, 1.5, 1.0), (1.5, 2.75, 1.75), (2.75, 16.25, 2.25)]),
            (4.0, 5.0, [(0.0, 1.0, 1.0), (1.0, 2.25, 1.75), (2.25, 16.5, 2.5)]))


def mayflower_model_ship():
    """The Mayflower Model Ship on its stand, laid corner to corner in its block (SHIP_TURN): a merchant ship of 1620,
    long and low. Its hull of oiled planking fills out from the keel to the deck in level layers and narrows to the
    bow; its sheer rises a little to the forecastle at the bow and most to the stern castle, whose poop steps up again
    and whose square stern carries a gallery of windows and the rudder. The upper works are a restrained band of red
    under a gold cap rail. Three evenly spaced walnut masts: the fore and main each carry two yards square across the
    keel, shorter going up, the mizzen one; from each yard hangs a broad sail of cream canvas bellied towards the bow in
    two steps. A short bowsprit rises from the bow, and three thin stays in the ship's middle plane, each joined at both
    ends: the forestay from the fore top to the bowsprit, the main topmast stay to the fore top, and the backstay from the
    main top over the mizzen top to the stern. A low, quiet walnut plinth with two chocks holds the keel."""
    sc = Sculpt("mayflower_model_ship", 4504, 128)
    walnut = timber(WALNUT)
    hull, deck, works, rail = timber(HULL), timber(MAPLEWOOD), polished(TOY_RED), flat(OCHRE)
    # The stand.
    el = [keel_box(sc, "plinth", walnut, (4.0, 0.0, 6.75), (12.0, 0.5, 9.25), east=end_grain(WALNUT), west=end_grain(WALNUT))]
    for x in (5.5, 10.5):
        el.append(keel_box(sc, "chock", flat(WALNUT), (x - 0.5, 0.5, 6.75), (x + 0.5, 1.75, 9.25)))
    # The hull, layer by layer, its waist deck the top of the last.
    for y0, y1, plan in hull_layers():
        for x0, x1, hw in plan:
            el.append(keel_box(sc, "hull", hull, (x0, y0, 8.0 - hw), (x1, y1, 8.0 + hw), up=deck))
    el.append(keel_box(sc, "rudder", hull, (16.5, 1.0, 7.75), (17.0, 6.0, 8.25)))
    # The upper works: the waist's bulwarks, the forecastle and the stern castle with its poop, red under gold rails.
    for z0, z1 in ((5.5, 6.0), (10.0, 10.5)):
        el.append(keel_box(sc, "bulwark", works, (4.0, 5.0, z0), (11.5, 6.0, z1)))
        el.append(keel_box(sc, "rail", rail, (4.0, 6.0, z0 - 0.125), (11.5, 6.25, z1 + 0.125)))
    el += [keel_box(sc, "forecastle", works, (0.5, 5.0, 6.75), (1.5, 6.5, 9.25), up=deck),
           keel_box(sc, "forecastle", works, (1.5, 5.0, 5.75), (4.0, 6.75, 10.25), up=deck),
           keel_box(sc, "rail", rail, (1.5, 6.75, 5.625), (4.0, 7.0, 10.375))]
    el += [keel_box(sc, "stern_castle", works, (11.5, 5.0, 5.5), (16.5, 7.5, 10.5), up=deck, east=windows()),
           keel_box(sc, "rail", rail, (11.5, 7.5, 5.375), (14.0, 7.75, 10.625)),
           keel_box(sc, "poop", works, (14.0, 7.5, 5.75), (16.5, 8.75, 10.25), up=deck, east=windows()),
           keel_box(sc, "rail", rail, (14.0, 8.75, 5.625), (16.5, 9.0, 10.375))]
    # The bowsprit: two lengths stepping up and out from the forecastle.
    spar = lengthwise(WALNUT)
    el += [keel_box(sc, "bowsprit", spar, (-0.75, 6.5, 7.75), (1.5, 7.0, 8.25)),
           keel_box(sc, "bowsprit", spar, (-2.0, 6.875, 7.7), (-0.5, 7.375, 8.3))]
    # The masts, evenly spaced, and the tops.
    for name, (x, y0, y1) in SHIP_MASTS.items():
        el.append(keel_box(sc, "mast", spar, (x - 0.375, y0, 7.625), (x + 0.375, y1, 8.375)))
    el += [keel_box(sc, "top", flat(WALNUT), (3.5, 10.25, 7.25), (5.0, 10.75, 8.75)),
           keel_box(sc, "top", flat(WALNUT), (7.5, 11.0, 7.25), (9.0, 11.5, 8.75))]
    # The sails: two on the fore and main, the lower ones broadest, one on the mizzen.
    el += ship_sail(sc, SHIP_MASTS["fore"][0], 9.5, 6.5, 7.0) + ship_sail(sc, SHIP_MASTS["fore"][0], 13.0, 10.75, 5.0)
    el += ship_sail(sc, SHIP_MASTS["main"][0], 10.25, 6.5, 8.5) + ship_sail(sc, SHIP_MASTS["main"][0], 14.75, 11.5, 6.0)
    el += ship_sail(sc, SHIP_MASTS["mizzen"][0], 11.75, 8.25, 4.5)
    # The stays.
    el += ship_line(sc, (4.25, 13.75), (-1.75, 7.25))
    el += ship_line(sc, (8.25, 15.5), (4.5, 13.5))
    el += ship_line(sc, (8.25, 15.5), (16.25, 8.75))
    el.append(keel_box(sc, "pennant", flat(TOY_RED), (8.5, 15.0, 7.875), (10.25, 15.5, 8.125)))
    sc.models["whole"] = el
    return sc


# ---------------------------------------------------------------- 5. Harvest Photo Frame Backdrop
# Modelled whole in its frame (64 across, 48 up, 16 deep; facing north, x across to the placer's left from its far
# block) and cut into its ten blocks. The frame stands in the plane z 5..9.5; its harvest dressing is in front of and
# outside its posts, and nothing but the low sill lies in its open middle (x 16..48 below y 32).

FRAME_Z = (4.5, 10.5)
BULB_XS = (24.5, 29.0, 35.0, 39.5)


def flint_ear():
    """A dried ear of flint corn hanging: kernels in rows a texel square, rust, amber and gold by turns along each row
    and stepped row by row (a pattern, not a sprinkle), the top row lit."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                k = (1, 3, 2)[(x + y) % 3]
                p.put(x, y, shade(FLINT_CORN, k + (1 if y == 0 else 0)))
    return tagged(paint, "flint_ear")


def broad_leaf(palette):
    """A broad autumn leaf drawn on a square turned 45 degrees: its own tone, lit along one side of its midrib and a tone
    darker along the other, its midrib (the square's diagonal, the leaf's length once turned) a light vein."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                d = x - y
                if abs(d) < 1:
                    k = 3
                elif d > 0:
                    k = 2
                else:
                    k = 1
                p.put(x, y, shade(palette, k))
    return tagged(paint, "broad_leaf", palette)


def leaf_on_face(sc, palette, cx, cy, size, z):
    """A broad leaf pressed flat on the lintel's face, its back at depth z + 0.5: a square `size` across turned 45
    degrees about its middle (cx, cy), so it reads as a leaf point up, point down; a short stalk at its foot."""
    rot = fa.rotation((cx, cy, z + 0.25), "z", 45.0)
    return panel(sc, "leaf", broad_leaf(palette), (cx - size / 2, cy - size / 2, z), (cx + size / 2, cy + size / 2, z + 0.5), rot=rot)


def harvest_swag(sc, x, y, face):
    """The frame's one harvest arrangement, across the middle of the lintel's face (`face`, the face's depth): a
    twine-bound knot at (x, y) lashed to the lintel; two ears of dried flint corn laid out from it each way along the
    lintel; pale husks fanned up behind them; and five broad leaves, rust, ochre and olive, laid over them round the
    knot. Everything lies against the lintel's face or on the corn, and the knot binds it all."""
    ear, husk = flint_ear(), husk_streaks(HUSK)
    el = [panel(sc, "swag_knot", flat(TWINE), (x - 1.5, y - 1.75, face - 1.5), (x + 1.5, y + 1.75, face))]
    # The ears, laid along the lintel from the knot, and the husks fanned up behind them.
    el += [panel(sc, "swag_ear", ear, (x - 12.0, y - 1.25, face - 1.5), (x - 1.0, y + 1.25, face - 0.25)),
           panel(sc, "swag_ear", ear, (x + 1.0, y - 1.25, face - 1.5), (x + 12.0, y + 1.25, face - 0.25))]
    for side in (-1, 1):
        cx = x + side * 4.0
        el.append(panel(sc, "swag_husk", husk, (cx - 1.5, y - 0.5, face - 0.5), (cx + 1.5, y + 5.0, face),
                        rot=fa.rotation((cx, y - 0.5, face - 0.25), "z", -22.5 * side)))
    # The leaves, broad and few, laid over the corn round the knot.
    for dx, dy, size, palette, z in ((-8.0, 0.5, 4.5, RUST, 2.0), (8.0, 0.5, 4.5, OCHRE, 2.0), (-4.0, -1.0, 4.0, OLIVE, 2.5),
                                     (4.0, -1.0, 4.0, LEAF_RED, 2.5), (-2.25, 1.75, 3.5, OCHRE, 3.0), (2.25, 1.75, 3.5, RUST, 3.0)):
        el.append(leaf_on_face(sc, palette, x + dx, y + dy, size, face - z))
    return el


def photo_frame():
    """The whole Harvest Photo Frame Backdrop: a rustic timber frame to stand in for a picture. Two heavy squared oak
    posts on broad dark sole plates, braced behind; a deep oak lintel across them, its ends running past the posts with
    their sawn end grain showing, under a thinner cap board; a dark oak knee brace in each top corner of the opening and
    a low oak sill across its foot; all planed timber with a broad grain, no carving or stripes. Its harvest is kept
    small: a bundle of dried flint corn hung from the middle of the lintel, a pair of pumpkins at the west foot, and a
    hay bale with a white pumpkin and a gourd at the east foot; under the lintel, between the braces, a short string of
    four amber bulbs. Returns (unlit elements, bulbs unlit, bulbs lit)."""
    sc = photo_frame.sc
    z0, z1 = FRAME_Z
    oak, oak_up = timber(OAK), timber(OAK)
    dark, ends = timber(OAK_DARK), end_grain(OAK)
    el = []
    # The posts on their sole plates, each braced behind by a strut from its plate's back end.
    for x0, x1 in ((9.0, 16.0), (48.0, 55.0)):
        el.append(panel(sc, "post", oak_up, (x0, 2.0, z0), (x1, 40.0, z1), up=ends))
        el.append(panel(sc, "plate", dark, (x0 - 1.0, 0.0, 2.5), (x1 + 1.0, 2.0, 12.5), east=end_grain(OAK_DARK), west=end_grain(OAK_DARK)))
        el.append(panel(sc, "strut", dark, (x0 + 2.0, 1.0, z1), (x1 - 2.0, 13.0, z1 + 1.5),
                        rot=fa.rotation(((x0 + x1) / 2, 13.0, z1 + 0.75), "x", 22.5)))
    # The lintel, running past both posts, and its cap board.
    el.append(panel(sc, "lintel", oak, (5.0, 40.0, z0 - 0.5), (59.0, 47.25, z1 + 0.5), east=ends, west=ends))
    el.append(panel(sc, "cap", dark, (4.0, 47.25, z0 - 1.0), (60.0, 48.0, z1 + 1.0), east=end_grain(OAK_DARK), west=end_grain(OAK_DARK)))
    # A knee brace in each top corner of the opening, from the post's inner face up to the lintel's underside.
    brace = timber(OAK_DARK)
    for cx, angle in ((19.5, 45.0), (44.5, -45.0)):
        el.append(panel(sc, "knee", brace, (cx - 4.75, 35.25, z0 + 1.0), (cx + 4.75, 37.75, z1 - 1.0),
                        rot=fa.rotation((cx, 36.5, (z0 + z1) / 2), "z", angle)))
    # The low sill across the foot of the opening.
    el.append(panel(sc, "sill", oak, (16.0, 0.0, 5.0), (48.0, tt.BACKDROP["sill_height"], 9.5)))
    # The harvest swag across the middle of the lintel's face.
    el += harvest_swag(sc, 32.0, 43.5, z0 - 0.5)
    # The string of bulbs: a dark cord fixed along the lintel's underside between the braces, a short drop to each
    # socket, and four big bulbs.
    el.append(panel(sc, "wire", flat(TIE), (22.0, 39.25, 7.0), (42.0, 40.0, 8.0)))
    bulbs_off, bulbs_on = [], []
    for x in BULB_XS:
        el.append(panel(sc, "drop", flat(TIE), (x - 0.375, 38.5, 7.125), (x + 0.375, 39.25, 7.875)))
        el.append(panel(sc, "socket", flat(TIN), (x - 0.75, 37.5, 6.75), (x + 0.75, 38.5, 8.25)))
        for palette, out in ((BULB_OFF, bulbs_off), (BULB_ON, bulbs_on)):
            name = "bulb_off" if palette is BULB_OFF else "bulb_on"
            out += crossed_y(sc, name, smooth(palette, glint=palette is BULB_ON), x, 7.5, 2.5, 35.0, 37.5)
    tdd.lit(bulbs_on, 15)
    # The west foot: a big pumpkin and a small one before it.
    el += pumpkin(sc, "pumpkin", 4.5, 8.0, 7.0, 0.0, 5.5)
    el += pumpkin(sc, "pumpkin", 5.0, 3.5, 4.0, 0.0, 3.0)
    # The east foot: a hay bale with a white pumpkin on it and a gourd at its front.
    el.append(panel(sc, "bale", hay(), (56.5, 0.0, 3.5), (63.0, 6.0, 12.5)))
    for bx in (58.25, 61.0):
        el.append(panel(sc, "bale_band", flat(TWINE), (bx, 0.0, 3.25), (bx + 0.75, 6.25, 12.75)))
    el += pumpkin(sc, "white_pumpkin", 59.75, 8.5, 5.0, 6.0, 4.0, WHITE_PUMPKIN)
    el.append(panel(sc, "gourd", gourd(), (60.0, 0.0, 1.0), (62.0, 2.0, 3.0)))
    return el, bulbs_off, bulbs_on


def frame_cut(elements):
    """The backdrop's part models in part order (thanksgiving_toys.BACKDROP cells): the whole model separated, each
    plain box cut into the blocks it crosses (decor17_data.cell_part) and each turned box given whole to the block its
    turn's middle lies in. Nothing may lie in the open middle."""
    across, up = tt.BACKDROP["across"], tt.BACKDROP["up"]
    size = (across, up, 1)
    elements = d20d.separated(elements)
    plain = [e for e in elements if "rotation" not in e]
    turned = [e for e in elements if "rotation" in e]
    for right, high in tt.BACKDROP["open"]:
        cell = (across - 1 - right, high, 0)
        assert not d17d.cell_part(d17d.pinned(plain), cell, size), f"something lies in the open middle at {cell}"
    parts = []
    for right, high in tt.BACKDROP["cells"]:
        cell = (across - 1 - right, high, 0)
        mine = d17d.cell_part(d17d.pinned(plain), cell, size)
        for e in turned:
            centre = e["rotation"]["origin"]
            if all(cell[k] * 16 <= centre[k] < cell[k] * 16 + 16 for k in range(2)):
                mine += shifted([e], -cell[0] * 16, -cell[1] * 16, 0.0)
        parts.append(mine)
    for e in turned:
        centre = e["rotation"]["origin"]
        assert any(all(c[k] * 16 <= centre[k] < c[k] * 16 + 16 for k in range(2))
                   for c in [(across - 1 - r, h) for r, h in tt.BACKDROP["cells"]]), f"a turned box is in no block: {e}"
    return parts


def harvest_photo_backdrop():
    sc = Sculpt("harvest_photo_backdrop", 4505, 128)
    photo_frame.sc = sc
    whole, off, on = photo_frame()
    sc.models["frame_whole"] = whole + off
    unlit = frame_cut(whole + off)
    lit = frame_cut(whole + on)
    for part in range(len(tt.BACKDROP["cells"])):
        sc.models[f"part{part}"] = unlit[part]
        if part in tt.BACKDROP["bulb_parts"]:
            sc.models[f"part{part}_lit"] = lit[part]
    # The item: the whole frame a quarter of its size, about its middle.
    scale = 0.25
    sc.models["whole"] = fa.transformed(d17d.pinned(whole + on), scale, (8 - 32 * scale, 2.0, 8 - 8 * scale), (0, 0, 0))
    return sc


# ---------------------------------------------------------------- 6. Tabletop Corn Toss Set
# The board, before it is tilted: its deck from z 3 to 14.5, 1 thick; tilted back about its front foot (BOARD_PIVOT)
# by BOARD_TILT so it rises to the back. Its hole is round (an octagon of a 4-pixel square with its corners filled)
# about HOLE_MIDDLE, cut right through the deck: the deck is built round it, so the hole's inner rim is the deck's own
# closed sides and the floor shows through it. thanksgiving_toys.CORN_TOSS keeps the hole's middle and the board's plan.
BOARD_X = (3.5, 12.5)
BOARD_Z = (3.0, 14.5)
BOARD_Y = (1.5, 2.5)
HOLE_MIDDLE = (8.0, 11.5)
HOLE_HALF = 2.0
BOARD_HOLE = (HOLE_MIDDLE[0] - HOLE_HALF, HOLE_MIDDLE[1] - HOLE_HALF, HOLE_MIDDLE[0] + HOLE_HALF, HOLE_MIDDLE[1] + HOLE_HALF)
BOARD_PIVOT = (8.0, 1.5, 3.0)
BOARD_TILT = -22.5
# The bags' places on the board (their low corner, unturned), rust and cream by turns across two rows before the hole;
# and the trays either side, where each colour's bags lie stacked (x of the tray's middle; bag k from the bottom).
BAG = 3.0
BAG_H = 1.0
# Where each bag lies on the board (its low corner on the deck, unturned): rust and cream in loose rows before the
# hole, overlapping a little, the second row raised a fifth of a pixel so no two bags' tops share a plane.
BOARD_SPOTS = {"rust": ((6.5, 4.5), (3.5, 6.75), (9.5, 9.0), (6.5, 6.75)), "cream": ((9.5, 4.5), (3.5, 4.5), (9.5, 6.75), (3.5, 9.0))}
# Off the board each colour's bags lie loose on the floor beside it, shingled along it: the first flat, each next one
# tipped a sixteenth of a turn with its front edge resting on the last (x of the row's middle; bag k from the front).
RACKS = {"rust": 1.75, "cream": 14.25}
SHINGLE_Z = (3.0, 5.5, 8.0, 10.5)


def board_rot():
    return fa.rotation(BOARD_PIVOT, "x", BOARD_TILT)


def projected(z, y):
    """Where a point of the untilted board (its z and height) lies once tilted: its z and height."""
    a = math.radians(-BOARD_TILT)
    pz, py = BOARD_PIVOT[2], BOARD_PIVOT[1]
    dz, dy = z - pz, y - py
    return pz + dz * math.cos(a) - dy * math.sin(a), py + dz * math.sin(a) + dy * math.cos(a)


def cloth(palette):
    """A beanbag's cloth, calm: its own tone, lit along its top and left, its seam a tone darker along its bottom and
    right."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                if p.w > 1 and p.h > 1 and (x == p.w - 1 or y == p.h - 1):
                    k = 1
                elif x == 0 or y == 0:
                    k = 3
                else:
                    k = 2
                p.put(x, y, shade(palette, k))
    return tagged(paint, "bag_cloth", palette)


def bag(sc, colour, lo, rot=None, lift=0.0, flip=False):
    """A beanbag, BAG square at its low corner `lo`: flat and soft, its filled body pinched at the corners (two crossed
    boxes, the narrower a little taller), and a pillowed top set off its middle (towards the back unless `flip`), so its
    outline is irregular and it reads as a soft filled bag, never a cube. `lift` raises it all (to keep overlapping bags'
    tops apart)."""
    palette = {"rust": RUST, "cream": CREAM}[colour]
    x, y, z = lo
    y += lift
    c = cloth(palette)
    pinch = 0.5
    off = -0.25 if flip else 0.25
    return [panel(sc, f"bag_{colour}", c, (x, y, z + pinch), (x + BAG, y + BAG_H * 0.6, z + BAG - pinch), rot=rot),
            panel(sc, f"bag_{colour}", c, (x + pinch, y + 0.125, z), (x + BAG - pinch, y + BAG_H * 0.75, z + BAG), rot=rot),
            panel(sc, f"bag_{colour}", c, (x + 0.625, y + BAG_H * 0.75, z + 0.625 + off), (x + BAG - 0.625, y + BAG_H, z + BAG - 0.625 + off), rot=rot)]


def corn_toss_base(sc):
    """The board and its two trays, no bags: a deck of smooth pale maple on a walnut frame (side rails and a back rail
    under it, a front riser and two back legs on the floor), its round hole cut right through it with a dark inner
    rim; each tray a low walnut box."""
    rot = board_rot()
    deck, rim = timber(MAPLEWOOD, edges=False), end_grain(WALNUT)
    band = BOARD_Z[0] + 1.25
    bx0, bx1 = BOARD_X
    bz0, bz1 = BOARD_Z
    y0, y1 = BOARD_Y
    hx0, hz0, hx1, hz1 = BOARD_HOLE
    # The deck: one painted band across its front edge (the board's one accent), then plain maple round the hole.
    el = [panel(sc, "deck_band", polished(TOY_RED), (bx0, y0, bz0), (bx1, y1, band), rot=rot, up=solid(TOY_RED, 2)),
          panel(sc, "deck", deck, (bx0, y0, band), (bx1, y1, hz0), rot=rot, south=rim),
          panel(sc, "deck", deck, (bx0, y0, hz1), (bx1, y1, bz1), rot=rot, north=rim),
          panel(sc, "deck", deck, (bx0, y0, hz0), (hx0, y1, hz1), rot=rot, east=rim),
          panel(sc, "deck", deck, (hx1, y0, hz0), (bx1, y1, hz1), rot=rot, west=rim)]
    # The hole's corners filled in two steps (three quarters of a pixel along each edge, half a pixel in), so its
    # outline follows a circle 4 pixels across rather than a square or a cross.
    for sx, sz in ((1, 1), (-1, 1), (1, -1), (-1, -1)):
        cx, cz = (hx0 if sx > 0 else hx1), (hz0 if sz > 0 else hz1)
        for (ax, az) in ((0.75, 0.5), (0.5, 0.75)):
            dz0 = 0.0 if ax == 0.75 else 0.5
            xs = sorted((cx, cx + sx * ax))
            zs = sorted((cz + sz * dz0, cz + sz * az))
            el.append(panel(sc, "deck_corner", deck, (xs[0], y0, zs[0]), (xs[1], y1, zs[1]), rot=rot,
                            north=rim, south=rim, east=rim, west=rim))
    # The frame under the deck: side rails, a back rail, a front riser on the floor and two back legs.
    rail = timber(WALNUT)
    el += [panel(sc, "rail", rail, (bx0, 0.25, bz0 + 1.0), (bx0 + 1.0, y0, bz1), rot=rot),
           panel(sc, "rail", rail, (bx1 - 1.0, 0.25, bz0 + 1.0), (bx1, y0, bz1), rot=rot),
           panel(sc, "rail", rail, (bx0 + 1.0, 0.25, bz1 - 1.0), (bx1 - 1.0, y0, bz1), rot=rot),
           panel(sc, "riser", rail, (bx0, 0.0, bz0), (bx1, BOARD_Y[0], bz0 + 1.0))]
    back_z, back_y = projected(bz1 - 1.5, 0.25)
    for x in (bx0 + 0.25, bx1 - 1.25):
        el.append(panel(sc, "leg", rail, (x, 0.0, back_z - 0.5), (x + 1.0, back_y + 0.25, back_z + 0.5)))
    return el


def board_bag(sc, colour, k):
    x, z = BOARD_SPOTS[colour][k]
    lift = 0.2 if abs(z - 6.75) < 0.01 else 0.0
    return bag(sc, colour, (x, BOARD_Y[1], z), rot=board_rot(), lift=lift, flip=k % 2 == 1)


def rack_bag(sc, colour, k):
    """Bag k of `colour` lying loose beside the board, shingled from the front (k 0, flat on the floor) back, each
    next one tipped a sixteenth of a turn with its front edge on the last bag's back edge and its back on the floor."""
    x = RACKS[colour] - BAG / 2
    z = SHINGLE_Z[k]
    if k == 0:
        return bag(sc, colour, (x, 0.0, z), flip=colour == "cream")
    # Tipped about its back foot, its front edge rises over the bag before.
    rot = fa.rotation((x + BAG / 2, 0.0, z + BAG), "x", 22.5)
    return bag(sc, colour, (x, 0.0, z), rot=rot, flip=(k + (colour == "cream")) % 2 == 1)


def corn_toss_set():
    """The Tabletop Corn Toss Set, facing north: its slanted board in the middle and the rust and cream bags lying loose
    beside it, each colour on its own side. Its blockstate (multipart) adds each bag beside the board while it is still
    to be tossed and each bag lying on the board."""
    sc = Sculpt("corn_toss_set", 4506, 128)
    base = corn_toss_base(sc)
    sc.models["base"] = base
    whole = list(base)
    for colour in ("rust", "cream"):
        for k in range(tt.CORN_TOSS["bags"]):
            sc.models[f"rack_{colour}_{k}"] = rack_bag(sc, colour, k)
            sc.models[f"board_{colour}_{k}"] = board_bag(sc, colour, k)
            whole += sc.models[f"rack_{colour}_{k}"]
    sc.models["whole"] = whole
    return sc


def toss_rack_values(colour, k):
    """The tossed counts (0 to 8) at which bag k of `colour` is still in its rack: rust tosses first, so its k-th bag
    from the back goes at toss 2(3 - k) + 1."""
    n = tt.CORN_TOSS["bags"]
    last = 2 * (n - 1 - k) + (0 if colour == "rust" else 1)
    return list(range(0, last + 1))


# ---------------------------------------------------------------- 7. Prize Pumpkin Display

def big_rosette(sc, x, cy, z):
    """A show rosette pinned on the drape at (x, cy), its back at depth z: an outer ring of rust pleats, an inner ring of
    ochre, a cream and gold button, and two rust tails out in a V below it."""
    el = []
    for name, palette, d, depth in (("rosette_rust", RUST, 5.0, 0.5), ("rosette_ochre", OCHRE, 3.0, 0.75)):
        ring = pleats(palette)
        r = d / 2
        el += [panel(sc, name, ring, (x - r, cy - r + 1.0, z - depth), (x + r, cy + r - 1.0, z)),
               panel(sc, name, ring, (x - r + 1.0, cy - r, z - depth), (x + r - 1.0, cy + r, z))]
    el.append(panel(sc, "button", smooth(CREAM, glint=True), (x - 0.75, cy - 0.75, z - 1.0), (x + 0.75, cy + 0.75, z - 0.75)))
    tail = lengthwise(RUST)
    for side, angle in ((-1, -22.5), (1, 22.5)):
        tx = x + side * 0.75
        el.append(panel(sc, "rosette_tail", tail, (tx - 0.5, cy - 3.25, z - 0.375), (tx + 0.5, cy - 1.0, z - 0.125),
                        rot=fa.rotation((tx, cy - 1.0, z - 0.25), "z", angle)))
    return el


def show_pumpkin(sc, cx, cz, y0):
    """A big squat prize pumpkin at (cx, cz) on y0: ribbed rings widening to its middle and narrowing again (so it reads
    round, not square), its top ringed into a sunken middle, a thick stalk with a curling tendril."""
    rib, top = tdd.ribbed(PUMPKIN), tdd.crown(PUMPKIN)
    el, y = [], y0
    for d, h in ((7.0, 0.5), (9.0, 1.0), (11.0, 4.0), (9.0, 1.0), (7.0, 0.5)):
        el += crossed_y(sc, "pumpkin", rib, cx, cz, d, y, y + h, up=top)
        y += h
    el.append(panel(sc, "stalk", lengthwise(STEM), (cx - 0.75, y, cz - 0.75), (cx + 0.75, y + 2.0, cz + 0.75)))
    el.append(panel(sc, "stalk_cap", smooth(STEM), (cx - 1.0, y + 2.0, cz - 1.0), (cx + 1.0, y + 2.5, cz + 1.0)))
    el.append(panel(sc, "tendril", lengthwise(STEM), (cx + 0.75, y + 0.5, cz - 0.25), (cx + 2.75, y + 1.0, cz + 0.25)))
    el.append(panel(sc, "tendril", lengthwise(STEM), (cx + 2.25, y + 1.0, cz - 0.25), (cx + 2.75, y + 2.0, cz + 0.25)))
    return el


def prize_pumpkin_display():
    """The Prize Pumpkin Display, facing north: a big ribbed show pumpkin with a curling stalk on a low planked crate
    draped in hessian hemmed in rust, a rust and ochre rosette pinned on the drape's front and the entry card on its
    wire stake at the front corner."""
    sc = Sculpt("prize_pumpkin_display", 4507, 128)
    el = [panel(sc, "crate", planks(WOOD, board=2, nails=False), (2.0, 0.0, 2.5), (14.0, 1.75, 13.5)),
          panel(sc, "drape", hessian_drape(), (1.5, 1.5, 2.0), (14.5, 4.5, 14.0), up=tdd.solid(HESSIAN, 3))]
    el += show_pumpkin(sc, 8.0, 8.5, 4.5)
    el += big_rosette(sc, 8.0, 3.5, 2.0)
    # The entry card at the front east corner, on its stake.
    el.append(panel(sc, "stake", flat(IRON), (12.375, 4.5, 3.375), (12.625, 6.0, 3.625)))
    el.append(panel(sc, "card", show_card(), (11.0, 6.0, 3.25), (14.0, 9.0, 3.5)))
    sc.models["whole"] = el
    return sc


# ---------------------------------------------------------------- 8. Harvest Percussion Set

def percussion_bench(sc):
    bench = planks(WOOD, board=2, nails=False)
    el = [panel(sc, "bench", bench, (1.0, 3.0, 4.0), (15.0, 4.0, 13.0))]
    for x in (1.5, 13.5):
        for z in (4.5, 11.5):
            el.append(panel(sc, "bench_leg", lengthwise(WALNUT), (x, 0.0, z), (x + 1.0, 3.0, z + 1.0)))
    return el


DRUM = {"x": 4.75, "y": 4.0, "z": 8.5, "d": 7.0, "h": 5.0}
WASH = {"x": (9.5, 14.5), "y": 4.0, "z": (8.5, 9.5)}


def frame_drum(sc):
    """The drum standing on the bench to the west: a round rust shell laced in a cream zigzag between walnut hoops, its
    pale skin on top; round in two boxes crossed and two more turned a sixteenth of a turn."""
    x, y, z, d, h = DRUM["x"], DRUM["y"], DRUM["z"], DRUM["d"], DRUM["h"]
    shell, hoop, skin = laced(RUST, CREAM), flat(WALNUT), drum_skin()
    el = []
    # The turned set's ends sit a tenth of a pixel inside the straight set's, so their tops and bottoms never share a
    # plane (a shared plane would be pushed apart again and again, stretching the hoops).
    for angle, inset in ((0.0, 0.0), (22.5, 0.125)):
        rot = rot_y(angle, (x, y, z))
        el += crossed_y(sc, "drum_shell", shell, x, z, d, y + 0.75, y + h - 0.75, rot=rot)
        el += crossed_y(sc, "drum_hoop", hoop, x, z, d + 0.5, y + inset, y + 0.75, rot=rot)
        el += crossed_y(sc, "drum_hoop", hoop, x, z, d + 0.5, y + h - 0.75, y + h - inset, rot=rot, up=skin)
    return el


def washboard(sc):
    """The washboard leaning back against a peg: a maple frame, a top board and corrugated tin."""
    (x0, x1), y, (z0, z1) = WASH["x"], WASH["y"], WASH["z"]
    rot = fa.rotation(((x0 + x1) / 2, y, z1), "x", 22.5)
    wood = lengthwise(MAPLEWOOD)
    el = [panel(sc, "wash_leg", wood, (x0, y, z0), (x0 + 1.0, y + 9.0, z1), rot=rot),
          panel(sc, "wash_leg", wood, (x1 - 1.0, y, z0), (x1, y + 9.0, z1), rot=rot),
          panel(sc, "wash_top", planks(MAPLEWOOD, board=2, nails=False), (x0, y + 7.0, z0 - 0.25), (x1, y + 9.5, z1 + 0.25), rot=rot),
          panel(sc, "wash_foot", wood, (x0 + 1.0, y, z0), (x1 - 1.0, y + 1.0, z1), rot=rot),
          panel(sc, "tin", corrugated(), (x0 + 1.0, y + 1.0, z0 + 0.25), (x1 - 1.0, y + 7.0, z1 - 0.25), rot=rot)]
    el.append(panel(sc, "peg", lengthwise(WALNUT), (11.25, y, 11.25), (12.75, y + 4.0, 12.25)))
    return el


def beater(sc, struck):
    """The drum's beater, a maple stick with a felt knob: lying on the bench before the drum, or (`struck`) its knob on
    the skin, the stick slanting up away from it."""
    wood, knob = lengthwise(MAPLEWOOD), smooth(HIDE)
    if not struck:
        return [panel(sc, "beater", wood, (1.75, 4.125, 4.5), (6.75, 4.875, 5.25)),
                panel(sc, "beater_knob", knob, (6.75, 4.0, 4.25), (8.25, 5.5, 5.5))]
    rot = fa.rotation((4.75, 9.5, 7.0), "z", 45.0)
    return [panel(sc, "beater_knob", knob, (4.125, 9.0, 6.5), (5.375, 10.0, 7.5)),
            panel(sc, "beater", wood, (4.375, 10.0, 6.625), (5.125, 15.0, 7.375), rot=rot)]


def scraper(sc, struck):
    """The washboard's thimble scraper (a little tin cup on a handle): on the bench before it, or (`struck`) against
    its tin."""
    tin, wood = flat(TIN), lengthwise(MAPLEWOOD)
    if not struck:
        return [panel(sc, "scraper", wood, (10.0, 4.0, 5.0), (13.0, 4.5, 5.5)), panel(sc, "thimble", tin, (13.0, 4.0, 4.75), (14.0, 5.0, 5.75))]
    rot = fa.rotation((12.0, 8.0, 7.25), "x", 22.5)
    return [panel(sc, "scraper", wood, (11.75, 5.0, 6.75), (12.25, 8.0, 7.25), rot=rot),
            panel(sc, "thimble", tin, (11.5, 8.0, 6.5), (12.5, 9.0, 7.5), rot=rot)]


def shaker(sc, struck):
    """The gourd shaker lying on the floor before the bench: a dried bottle gourd, ochre striped darker, its round bulb
    rounded off at its ends, its long neck its handle with a walnut cap; shaken (`struck`), lifted and tipped."""
    # Shaken, it is lifted a pixel and a quarter off the floor and tipped a sixteenth of a turn, neck up.
    cy = 3.25 if struck else 2.0
    rot = fa.rotation((8.0, cy, 2.5), "z", 22.5) if struck else None
    paint = dried_gourd()
    el = crossed_x(sc, "shaker", paint, 5.0, 8.0, cy, 2.5, 4.0, rot=rot)
    el += crossed_x(sc, "shaker", paint, 4.5, 5.0, cy, 2.5, 2.5, rot=rot)
    el += crossed_x(sc, "shaker", paint, 8.0, 8.5, cy, 2.5, 3.0, rot=rot)
    el.append(panel(sc, "shaker_neck", paint, (8.5, cy - 0.625, 1.875), (12.0, cy + 0.625, 3.125), rot=rot))
    el.append(panel(sc, "shaker_cap", flat(WALNUT), (12.0, cy - 0.75, 1.75), (12.5, cy + 0.75, 3.25), rot=rot))
    return el


def harvest_percussion_set():
    """The Harvest Percussion Set, facing north: a low bench with a laced drum and its beater to the west, a washboard
    leaning back against a peg and its thimble scraper to the east, and a gourd shaker on the floor in front. Each
    struck pose (`struck_<instrument>`) shows that instrument's beater, scraper or gourd mid-play."""
    sc = Sculpt("harvest_percussion_set", 4508, 128)
    still = percussion_bench(sc) + frame_drum(sc) + washboard(sc)
    rest = {"drum": beater(sc, False), "washboard": scraper(sc, False), "shaker": shaker(sc, False)}
    play = {"drum": beater(sc, True), "washboard": scraper(sc, True), "shaker": shaker(sc, True)}
    sc.models["whole"] = still + rest["drum"] + rest["washboard"] + rest["shaker"]
    for name in tt.PERCUSSION["instruments"]:
        sc.models[f"struck_{name}"] = still + sum((play[n] if n == name else rest[n] for n in tt.PERCUSSION["instruments"]), [])
    return sc


# ---------------------------------------------------------------- building and files

BUILDERS = {"corn_husk_dolls": corn_husk_dolls, "turkey_pull_toy": turkey_pull_toy, "spinning_top": spinning_top,
            "mayflower_model_ship": mayflower_model_ship, "harvest_photo_backdrop": harvest_photo_backdrop,
            "corn_toss_set": corn_toss_set, "prize_pumpkin_display": prize_pumpkin_display,
            "harvest_percussion_set": harvest_percussion_set}
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


DISPLAY = tdd.DISPLAY
BACKDROP_DISPLAY = {"gui": {"rotation": [25, 210, 0], "translation": [0, 1, 0], "scale": [0.85, 0.85, 0.85]},
                    "ground": {"rotation": [0, 0, 0], "translation": [0, 2, 0], "scale": [0.4, 0.4, 0.4]},
                    "fixed": {"rotation": [0, 180, 0], "translation": [0, 1, 0], "scale": [0.9, 0.9, 0.9]},
                    "thirdperson_righthand": {"rotation": [0, 45, 0], "translation": [0, 2, 0], "scale": [0.45, 0.45, 0.45]},
                    "firstperson_righthand": {"rotation": [0, 45, 0], "translation": [0, 2, 0], "scale": [0.45, 0.45, 0.45]}}


def turned(model, facing, **extra):
    return {"model": rid(f"block/{model}"), **({"y": FACINGS[facing]} if FACINGS[facing] else {}), **extra}


def model_names(block):
    """Every block model file the piece writes, without its folder."""
    sc = build(block)
    kind = tt.PROPS[block]["kind"]
    if kind == "top":
        return [block, f"{block}_wobble"] + [f"{block}_spin{k}" for k in range(tt.TOP["frames"])]
    if kind == "backdrop":
        return [f"{block}_{name}" for name in sc.models if name.startswith("part")]
    if kind == "corn_toss":
        return [f"{block}_{name}" for name in sc.models if name == "base" or name.startswith(("rack_", "board_"))]
    if kind == "percussion":
        return [block] + [f"{block}_struck_{name}" for name in tt.PERCUSSION["instruments"]]
    return [block]


def blockstate(block):
    kind = tt.PROPS[block]["kind"]
    if kind == "top":
        return {"variants": {f"facing={f},spin={s}": {"model": rid(f"block/{top_state_model(s)}"),
                                                       **({"y": top_state_y(f, s)} if top_state_y(f, s) else {})}
                             for f in FACINGS for s in range(tt.TOP["spin"] + 1)}}
    if kind == "backdrop":
        n = len(tt.BACKDROP["cells"])
        out = {}
        for f in FACINGS:
            for lit in ("false", "true"):
                for p in range(n):
                    name = f"{block}_part{p}" + ("_lit" if lit == "true" and p in tt.BACKDROP["bulb_parts"] else "")
                    out[f"facing={f},lit={lit},part={p}"] = turned(name, f)
        return {"variants": out}
    if kind == "corn_toss":
        parts = []
        for f in FACINGS:
            parts.append({"when": {"facing": f}, "apply": turned(f"{block}_base", f)})
        for colour in ("rust", "cream"):
            for k in range(tt.CORN_TOSS["bags"]):
                tossed = "|".join(str(v) for v in toss_rack_values(colour, k))
                on = "|".join(str(v) for v in range(k + 1, tt.CORN_TOSS["bags"] + 1))
                for f in FACINGS:
                    parts.append({"when": {"facing": f, "tossed": tossed}, "apply": turned(f"{block}_rack_{colour}_{k}", f)})
                    parts.append({"when": {"facing": f, f"{colour}_board": on}, "apply": turned(f"{block}_board_{colour}_{k}", f)})
        return {"multipart": parts}
    if kind == "percussion":
        return {"variants": {f"facing={f},struck={s}": turned(block if s == "none" else f"{block}_struck_{s}", f)
                             for f in FACINGS for s in ("none", *tt.PERCUSSION["instruments"])}}
    return {"variants": {f"facing={f}": turned(block, f) for f in FACINGS}}


def block_models(block):
    """{model file name: elements} for the piece, each separated (decor20_data.separated) as the decorations' are."""
    sc = build(block)
    kind = tt.PROPS[block]["kind"]
    out = {}
    for name in model_names(block):
        key = "whole" if name == block else name[len(block) + 1:]
        elements = sc.models[key]
        out[name] = elements if kind == "backdrop" else d20d.separated(elements)
    return out


def item_elements(block):
    sc = build(block)
    if tt.PROPS[block]["kind"] == "top":
        return d20d.separated(sc.models["spin0"])
    return d20d.separated(sc.models["whole"])


def assets(root, write, lang):
    """Each piece's block models, blockstate, item model and item; and its names, hints and messages."""
    write = fa.closing_writer(write, _image_of)
    models = root / "models" / "block"
    lang.update(tt.names())
    for block in tt.PROPS:
        for name, elements in block_models(block).items():
            write(models / f"{name}.json", fa.model(block, elements))
        write(root / "blockstates" / f"{block}.json", blockstate(block))
        display = BACKDROP_DISPLAY if tt.PROPS[block]["kind"] == "backdrop" else DISPLAY
        write(root / "models" / "item" / f"{block}.json", fa.model(block, item_elements(block), display))
        write(root / "items" / f"{block}.json", {"model": {"type": "minecraft:model", "model": rid(f"item/{block}")}})


def loot(out, write):
    """Each piece drops itself once; the backdrop only from its first block (MultiDecorationBlock's master)."""
    from decor_data import self_drop
    for block, spec in tt.PROPS.items():
        if spec["kind"] == "backdrop":
            write(out / f"{block}.json", self_drop(block, {"type": "minecraft:match_block", "blocks": rid(block), "state": {"part": "0"}}))
        else:
            write(out / f"{block}.json", self_drop(block))


def recipes(out, write, conditions):
    """The shapeless recipes (thanksgiving_toys.SHAPELESS), as agriculture_data.recipes writes its own."""
    for recipe in tt.SHAPELESS:
        write(out / f"{recipe['id']}.json", {"fabric:load_conditions": conditions(), "type": "minecraft:crafting_shapeless",
                                             "category": recipe["category"], "ingredients": recipe["inputs"],
                                             "result": {"id": rid(recipe["result"]), "count": recipe["count"]}})


def tags(tags):
    for block in tt.AXE:
        tags.add("block", "minecraft:mineable/axe", rid(block))


def textures():
    return {("block", block): build(block).atlas.img for block in tt.PROPS}


# ---------------------------------------------------------------- on its own

def _conditions():
    return [{"condition": f"{MOD}:feature_enabled", "feature": tt.FEATURE}]


def write_all(repo):
    """Writes every file of these pieces into the repository at `repo`, as generate_material_data.py's write() would
    (models finished by model_writer.finish_elements), and returns the language entries (the coordinator merges them
    into en_us.json with the other groups')."""
    import model_writer
    res = Path(repo) / "src" / "main" / "resources"
    assets_root, data_root = res / "assets" / MOD, res / "data" / MOD
    written = []

    def write(path, obj):
        if isinstance(obj, dict) and obj.get("elements") and "models" in path.parts:
            model_writer.finish_elements(obj["elements"])
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(json.dumps(obj, indent=2) + "\n", encoding="utf-8")
        written.append(path)
    lang = {}
    assets(assets_root, write, lang)
    loot(data_root / "loot_table" / "blocks", write)
    recipes(data_root / "recipe", write, _conditions)
    for (kind, name), img in textures().items():
        path = assets_root / "textures" / kind / f"{name}.png"
        path.parent.mkdir(parents=True, exist_ok=True)
        img.save(path, optimize=True)
        written.append(path)
    return written, lang


if __name__ == "__main__":
    if sys.argv[1:] == ["write"]:
        repo = Path(__file__).resolve().parent.parent
        files, lang = write_all(repo)
        print(f"wrote {len(files)} files")
        print(json.dumps(lang, indent=2, sort_keys=True))
    else:
        print("usage: python3 tools/thanksgiving_toys_data.py write")

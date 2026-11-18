"""Generated assets for the Thanksgiving harvest town details (tools/thanksgiving_town.py;
docs/features/thanksgiving-town-details.md).

Built from boxes at real size, one texel to a model pixel, in the Thanksgiving decorations' manner
(tools/thanksgiving_decor_data.py), sharing its palettes and painters (pumpkins, leaves, linen, napkins, gingham, copper,
tin, wax and flame, walnut), the Harvest Festival Grounds' (corn, husk, wheat, kernels, ears, twine, brass) and the
Pumpkin Patch Market's planks, straps, turned wood and iron (tools/pumpkin_patch_data.py), so each piece sits beside the
existing feast props in one hand. Each piece is one packed texture (block/<piece>) of pieces painted for the faces they
cover (pumpkin_patch_data.panel), flat in three to five tones, lit from the top left; patterns, not noise.

Colours read off the owner's own drawings (art/owner-library/originals/Blocks/farming and food textures/, read as data,
nothing copied): the onions from onion.png, the rope from rope.png, the carving knife from iron_knife.png, the cut
pumpkin flesh from pumpkin_slice.png and the bench's boards from cutting_board.png. Everything else is drawn here by code;
no Mojang texture is read, traced or copied.

Writes, through agriculture_data.py (assets, loot, tags) and crop_textures.py (textures), or alone through main() for
these pieces only: each piece's block and item models, blockstate, self-dropping loot, recipe, axe or pickaxe tag, name
and texture.
"""
import copy
import json
import math
import sys
from pathlib import Path

import decor17_data as d17d
import decor20_data as d20d
import flora_art as fa
import harvest_festival_data as hfd
import pumpkin_patch_data as ppd
import thanksgiving_decor_data as tdd
import thanksgiving_town as tt
from flora_art import Sculpt, pal, shade
from harvest_festival_data import BRASS, CARD, CORN, HUSK, LEAF, TWINE, WHEAT, crossed_y, crossed_z, ears, gourd, kernels
from haunted_midway_data import PUMPKIN, STEM
from pumpkin_patch_data import BARN_RED, IRON, WOOD, flat, lengthwise, panel, planks
from thanksgiving_decor_data import (ACORN_CAP, ACORN_NUT, COPPER, FLAME, GINGHAM, GOLD_LEAF, LINEN, NAPKIN, ORANGE_LEAF, RED_LEAF,
                                     SHADOW, SILVER, WALNUT, WARTY, WHITE_PUMPKIN, check, crown, flat_leaf, lit, pumpkin, ribbed,
                                     smooth, solid, sprite, tagged, upright_leaf)

MOD = "jugcraft"
FACINGS = {"north": 0, "east": 90, "south": 180, "west": 270}

# Palettes, darkest first.
TIMBER = pal("3e2a16", "5a3f22", "74552f", "8e6c3e", "a8844e")           # hewn oak, warm
STONE = pal("4a4640", "625d55", "7a746a", "938c80")                      # fieldstone and its mortar
STRAW = pal("8a7448", "a89262", "c4ae7e", "dccb9c")                      # dried corn stalks and leaves
ROPE = pal("512d13", "613c11", "7b4f1e", "97652e", "b37b43")             # owner rope.png
ONION = pal("6d4a20", "8b6941", "ac732f", "c58b46", "dea766")            # owner onion.png, its skin
ONION_TOP = pal("536b38", "698747", "82a859")                            # owner onion.png, its dry shoot
GARLIC = pal("a89c8a", "c8bca8", "e2d8c6", "f2ecde")
STONEWARE = pal("8e826a", "b0a486", "cec2a2", "e4dabc")                  # salt-glazed stoneware, cream
BROWN_GLAZE = pal("2e1a0c", "4a2a12", "66391a", "844c24")                # its brown Albany slip
EARTHENWARE = pal("6a2c14", "8e3e1c", "b05428", "c86c38", "dc8a52")     # red earthenware crock
SAGE = pal("3e4a34", "56644a", "707e5e", "8c9874")                       # a dried sage bunch
THYME = pal("2c3a20", "3e4e2c", "54663a", "6e824c")                      # a dried thyme bunch
MUSTARD = pal("7a5a12", "9e7a1c", "be9a2e", "d8b84a")                    # a mustard cloth
HESSIAN = pal("6e5634", "8a6e44", "a68a58", "c0a672")                    # a hessian runner
FLESH = pal("7e3d0e", "a0560b", "c46f14", "e38a1d", "f7a43e", "f9bd64")  # owner pumpkin_slice.png, cut flesh
HOLLOW = pal("3e1c08", "5a2a0c", "7a3c12")                               # inside the hollowed pumpkin
BLADE = pal("444444", "8c8c8c", "b3b3b3", "d8d8d8")                      # owner iron_knife.png, its blade
HANDLE = pal("281e0b", "493615", "684e1e", "896727")                     # owner iron_knife.png, its handle
BENCH = pal("6c4b14", "8c682f", "a0793a", "b99248", "c89b52")            # owner cutting_board.png
SEED = pal("b8a67e", "d6c69c", "eee2bc")


def rid(path):
    return path if ":" in path else f"{MOD}:{path}"


# ---------------------------------------------------------------- painters

def stones():
    """Fieldstone in courses three texels high, each stone five long, every other course set half a stone along: each
    stone lit along its top and left edge, the mortar a shade under and to its right; stones alternate two tones."""
    def paint(p):
        for y in range(p.h):
            course, row = divmod(y, 3)
            for x in range(p.w):
                along = x + (2 if course % 2 else 0)
                col = along % 5
                if row == 2 or col == 4:
                    k = 0
                elif row == 0 or col == 0:
                    k = 3
                else:
                    k = 2 if (along // 5 + course) % 2 == 0 else 1
                p.put(x, y, shade(STONE, k))
    return tagged(paint, "stones")


def stalk():
    """A dried corn stalk: straw in stripes two texels wide along it, lit on its upper (or left) edge, with a darker node
    band every seventh texel along."""
    def paint(p):
        along_x = p.w >= p.h
        span, across = (p.w, p.h) if along_x else (p.h, p.w)
        for y in range(p.h):
            for x in range(p.w):
                s, a = (x, y) if along_x else (y, x)
                k = 0 if s % 7 == 6 else 3 if a == 0 and across > 1 else (2, 2, 1, 1)[a % 4]
                p.put(x, y, shade(STRAW, k))
    return tagged(paint, "stalk")


def folded(palette):
    """The edge of a folded cloth: its top row lit (the fold), its last row a shade (the gap to the next cloth), its own
    tone between, with a hem stitched every third texel along its second row where it is three texels or more."""
    def paint(p):
        along_x = p.w >= p.h
        span, across = (p.w, p.h) if along_x else (p.h, p.w)
        for y in range(p.h):
            for x in range(p.w):
                s, a = (x, y) if along_x else (y, x)
                if a == 0:
                    k = 3
                elif a == across - 1 and across > 1:
                    k = 1
                elif a == 1 and across >= 3 and s % 3 == 1:
                    k = 3
                else:
                    k = 2
                p.put(x, y, shade(palette, k))
    return tagged(paint, "folded", palette)


def cloth_top(palette):
    """A folded cloth seen from above: its own tone, lit along its north and west edges, a crease a tone darker down its
    middle across its shorter side."""
    def paint(p):
        crease = p.w // 2 if p.w >= p.h else None
        crease_y = p.h // 2 if crease is None else None
        for y in range(p.h):
            for x in range(p.w):
                if x == 0 or y == 0:
                    k = 3
                elif x == crease or y == crease_y:
                    k = 1
                else:
                    k = 2
                p.put(x, y, shade(palette, k))
    return tagged(paint, "cloth_top", palette)


def polished(palette):
    """Polished copper or brass: its own tone, its top row lit, its last row a shade, and one bright streak a texel or two
    in from its left edge where the light catches the curve (no banding, no speckle)."""
    top = len(palette) - 1

    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                if y == 0 and p.h > 1:
                    k = top - 1
                elif y == p.h - 1 and p.h > 2:
                    k = top - 3
                elif x == 1 and p.w > 3:
                    k = top
                elif x == 2 and p.w > 5:
                    k = top - 1
                else:
                    k = top - 2
                p.put(x, y, shade(palette, k))
    return tagged(paint, "polished", palette)


def herbs(palette):
    """A dried herb bunch: small leaves on a diagonal lattice, a leaf lit, its own tone, then its shadow side."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                n = (x + 2 * y) % 4
                p.put(x, y, shade(palette, 3 if n == 0 else 1 if n == 2 else 2))
    return tagged(paint, "herbs", palette)


def guts():
    """Scooped pumpkin guts: stringy flesh in wavy strands (a strand lit and one in shadow by turns, each row a texel
    along from the last), a cream seed on every third texel of every other row."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                if y % 2 == 0 and (x + y) % 3 == 0:
                    c = shade(SEED, 2)
                else:
                    c = shade(FLESH, (3, 2, 1, 2)[(x + y // 2) % 4])
                p.put(x, y, c)
    return tagged(paint, "guts")


# ---------------------------------------------------------------- the Timber Harvest Arch

# Its frame (pixels): 80 across, 64 up, 16 deep, facing north. Each post stands at POSTS[i] (its middle across).
POSTS = (8.0, 72.0)
BEAM_Y0, BEAM_Y1 = 50.0, 56.0


def corn_shock(sc, x0, x1, outward):
    """Dried corn stalks stood against a post's outer side (x0..x1), tied to it with twine: three stalks of different
    heights, their leaves bending out and down from them, their tassels at the top."""
    el = []
    w = (x1 - x0) / 3
    for i, (h, dz) in enumerate(((40.0, 6.0), (44.0, 8.0), (37.0, 7.0))):
        sx = x0 + i * w
        el.append(panel(sc, "stalk", stalk(), (sx, 0.0, dz), (sx + w + 0.25, h, dz + 1.5)))
        # The tassel: a short spray of three stems at the top.
        for turn in (-22.5, 0.0, 22.5):
            el.append(panel(sc, "tassel", lengthwise(STRAW), (sx + w / 2 - 0.25, h, dz + 0.5), (sx + w / 2 + 0.25, h + 3.0, dz + 1.0),
                            rot=fa.rotation((sx + w / 2, h, dz + 0.75), "z", turn) if turn else None))
    # Leaves: long blades out from the stalks, bending outward and down, at three heights.
    for y, dz in ((14.0, 5.5), (24.0, 9.5), (31.0, 6.0)):
        cx = x0 + (x1 - x0) / 2
        el.append(panel(sc, "corn_leaf", flat(STRAW), (cx - 0.25, y, dz), (cx + 0.25, y + 7.0, dz + 1.0),
                        rot=fa.rotation((cx, y, dz + 0.5), "z", 22.5 * -outward)))
    return el


def hung_corn(sc, px, z_front):
    """Two ears of ornamental corn hung on a post's front by their husks, tied with twine: the ears' kernels hanging
    down, the husks pulled up and splayed over the tie."""
    el = [panel(sc, "corn_tie", lengthwise(TWINE), (px - 1.5, 33.0, z_front - 0.5), (px + 1.5, 34.0, z_front))]
    for i, ex in enumerate((px - 1.25, px + 0.25)):
        top = 33.0 - i * 0.75
        el.append(panel(sc, "ear", kernels(), (ex, top - 6.0, z_front - 1.25), (ex + 1.25, top, z_front)))
        for turn in (-22.5, 22.5):
            el.append(panel(sc, "husk", flat(HUSK), (ex + 0.375, top, z_front - 1.0), (ex + 0.875, top + 3.5, z_front - 0.5),
                            rot=fa.rotation((ex + 0.625, top, z_front - 0.75), "z", turn)))
    return el


def swag(sc, xa, xb, y, z0, z1):
    """A garland of autumn leaves pinned at (xa, y) and (xb, y) on the beam's front, sagging between them: a length falling
    from each pin at 22.5 degrees and a level length between, leaves laid over its front."""
    body = tdd.leafy()
    core = 2.0
    slant = 6.5
    drop = slant * math.sin(math.radians(22.5))
    low = y - drop
    el = [panel(sc, "garland", body, (xa, y - core / 2, z0), (xa + slant, y + core / 2, z1), rot=fa.rotation((xa, y, (z0 + z1) / 2), "z", -22.5)),
          panel(sc, "garland", body, (xb - slant, y - core / 2, z0), (xb, y + core / 2, z1), rot=fa.rotation((xb, y, (z0 + z1) / 2), "z", 22.5)),
          panel(sc, "garland", body, (xa + slant * math.cos(math.radians(22.5)) - 0.5, low - core / 2, z0),
                (xb - slant * math.cos(math.radians(22.5)) + 0.5, low + core / 2, z1))]
    front = z0 - 0.1
    span = xb - xa
    leaves = (("small", RED_LEAF, 0.18, 0.0), ("oak", GOLD_LEAF, 0.36, -0.15), ("small", ORANGE_LEAF, 0.5, -0.3),
              ("oak", RED_LEAF, 0.64, -0.15), ("small", GOLD_LEAF, 0.82, 0.0))
    for kind, palette, t, dz in leaves:
        x = xa + span * t
        sag = drop if 0.25 < t < 0.75 else drop * 0.6
        # Each leaf kept above the beam's block row: below it is the opening, where there is no block to draw it.
        low_edge = 48.0 + tdd.leaf_size(kind)[1] / 2 + 0.25
        el.append(upright_leaf(sc, kind, palette, x, max(y - sag, low_edge), front + dz))
    return el


def bow(sc, x, y, z):
    """A red ribbon bow at a garland's pin: a knot and two loops turned 22.5 degrees each way."""
    ribbon = flat(GINGHAM)
    el = [panel(sc, "knot", ribbon, (x - 0.75, y - 0.75, z - 0.75), (x + 0.75, y + 0.75, z + 0.25))]
    for side in (-1.0, 1.0):
        lo = x + (side * 0.75 - 1.5 if side < 0 else side * 0.75)
        el.append(panel(sc, "loop", ribbon, (lo, y - 0.5, z - 0.5), (lo + 1.5, y + 0.5, z),
                        rot=fa.rotation((x + side * 0.75, y, z - 0.25), "z", 22.5 * side)))
    for side in (-1.0, 1.0):
        el.append(panel(sc, "tail", ribbon, (x + side * 0.5 - 0.25, y - 3.0, z - 0.4), (x + side * 0.5 + 0.25, y - 0.75, z - 0.15)))
    return el


def timber_harvest_arch():
    """The Timber Harvest Arch, facing north, in its frame (80 across, 64 up, 16 deep): a farm gateway 3.6 m wide and
    3.3 m to the top of its beam, its opening three blocks across and three high with nothing in it.
    - Two hewn oak posts on fieldstone plinths, knee braces into the beam at 45 degrees, a heavy oak beam pegged over them
      and a board along its top.
    - On the beam: a sheaf of wheat bound with twine between an orange pumpkin and a white one, maple leaves fallen by them.
    - Along its front: autumn leaves pinned in two swags with red bows; along its back, a band of leaves.
    - At each post: dried corn stalks tied against its outer side, two ears of ornamental corn hung on its front, and a
      pumpkin and a gourd at its foot."""
    sc = Sculpt(tt.ARCH["block"], 4601, 128)
    timber, ends = lengthwise(TIMBER), tdd.rings()
    el = []
    for i, px in enumerate(POSTS):
        outward = -1.0 if i == 0 else 1.0
        el.append(panel(sc, "plinth", stones(), (px - 4.0, 0.0, 4.0), (px + 4.0, 3.5, 12.0), up=solid(STONE, 3)))
        el.append(panel(sc, "post", timber, (px - 3.0, 3.5, 5.0), (px + 3.0, BEAM_Y0, 11.0)))
        # The knee brace, from the post's inner face up into the beam, its ends buried in both.
        inner = px + 3.0 * -outward
        cx, cy = inner - outward * 4.5, 45.5
        el.append(panel(sc, "brace", timber, (cx - 7.75, cy - 1.0, 6.5), (cx + 7.75, cy + 1.0, 9.5),
                        rot=fa.rotation((cx, cy, 8.0), "z", 45.0 * -outward)))
        # Dried corn tied against its outer side, bound to the post twice.
        x0, x1 = (0.5, px - 3.0) if i == 0 else (px + 3.0, 79.5)
        el += corn_shock(sc, x0, x1, outward)
        for y in (15.0, 36.0):
            el.append(panel(sc, "binding", lengthwise(TWINE), (min(x0, px - 3.0) - 0.25, y, 4.75), (max(x1, px + 3.0) + 0.25, y + 0.75, 11.25)))
        el += hung_corn(sc, px, 5.0)
        # Its pegs, through the beam over the post.
        for dx in (-1.5, 0.5):
            el.append(panel(sc, "peg", solid(TIMBER, 0), (px + dx, 52.5, 4.25), (px + dx + 1.0, 53.5, 4.5)))
        # A pumpkin and a gourd at its foot.
        if i == 0:
            el += pumpkin(sc, "foot_pumpkin", px - 2.0, 2.0, 4.0, 0.0, 3.5)
            el.append(panel(sc, "gourd", gourd(), (px + 1.0, 0.0, 1.0), (px + 2.5, 1.5, 2.5)))
        else:
            el += pumpkin(sc, "foot_white_pumpkin", px + 2.0, 2.0, 4.0, 0.0, 3.0, WHITE_PUMPKIN)
            el.append(panel(sc, "warty_gourd", tdd.warty(), (px - 3.0, 0.0, 1.0), (px - 1.0, 2.0, 3.0)))
    # The beam, its sawn ends showing their rings, and a board along its top.
    el.append(panel(sc, "beam", timber, (0.5, BEAM_Y0, 4.5), (79.5, BEAM_Y1, 11.5), east=ends, west=ends))
    el.append(panel(sc, "cap", planks(TIMBER, board=3, nails=False), (2.0, BEAM_Y1, 5.0), (78.0, BEAM_Y1 + 1.0, 11.0)))
    top = BEAM_Y1 + 1.0
    # On it: the sheaf of wheat between two pumpkins.
    el += [panel(sc, "sheaf", lengthwise(WHEAT), (38.5, top, 6.5), (41.5, top + 4.5, 9.5)),
           panel(sc, "sheaf_band", lengthwise(TWINE), (38.25, top + 1.5, 6.25), (41.75, top + 2.5, 9.75)),
           panel(sc, "sheaf_ears", ears(), (37.5, top + 4.5, 5.5), (42.5, 64.0, 10.5))]
    el += pumpkin(sc, "crest_pumpkin", 33.5, 8.0, 5.0, top, 4.0)
    el += pumpkin(sc, "crest_white_pumpkin", 46.5, 8.0, 4.0, top, 3.0, WHITE_PUMPKIN)
    el.append(flat_leaf(sc, "small", RED_LEAF, 28.5, 8.0, top + 0.1, 22.5))
    el.append(flat_leaf(sc, "oak", GOLD_LEAF, 51.0, 8.0, top + 0.1, -45.0))
    # The garland: two swags along the front, a bow at each pin; a band of leaves along the back.
    z0, z1 = 3.0, 4.5
    el += swag(sc, 11.0, 40.0, 53.0, z0, z1) + swag(sc, 40.0, 69.0, 53.0, z0, z1)
    for x in (11.0, 40.0, 69.0):
        el += bow(sc, x, 53.0, z0)
    el.append(panel(sc, "back_garland", tdd.leafy(), (3.0, 51.0, 11.5), (77.0, 53.0, 12.5)))
    sc.models["whole"] = el
    return sc


def arch_cells():
    """The arch's parts' cells in its frame (blocks across, up, deep), in part order: MultiDecorationBlock's {right, up}
    turned to the frame, which runs across to the placer's left."""
    w = tt.ARCH["width"]
    return [(w - 1 - right, up, 0) for right, up in tt.ARCH["cells"]]


def arch_parts(elements):
    """The arch's part models, in part order, from its model drawn whole in its frame: separated first, then each plain box
    cut into the blocks of its cells it crosses (each piece keeping its share of the picture), and each turned box given
    whole to the cell its turn's middle lies in, moved into it. Nothing may lie in the opening, where there is no block."""
    w, h = tt.ARCH["width"], tt.ARCH["height"]
    size = (w, h, 1)
    elements = d20d.separated(elements)
    plain = [e for e in elements if "rotation" not in e]
    turned = [e for e in elements if "rotation" in e]
    cells = arch_cells()
    pinned = d17d.pinned(plain)
    for e in pinned:
        for cx in range(w):
            for cy in range(h):
                if (cx, cy, 0) in cells:
                    continue
                lo = [max(e["from"][0], cx * 16), max(e["from"][1], cy * 16)]
                hi = [min(e["to"][0], cx * 16 + 16), min(e["to"][1], cy * 16 + 16)]
                assert hi[0] - lo[0] <= 1e-6 or hi[1] - lo[1] <= 1e-6, f"a box reaches into the opening at {(cx, cy)}: {e['from']}..{e['to']}"
    parts = []
    for cell in cells:
        mine = d17d.cell_part(pinned, cell, size)
        for e in turned:
            centre = e["rotation"]["origin"]
            if all(cell[k] * 16 <= centre[k] < cell[k] * 16 + 16 for k in range(2)):
                lo = [e["from"][k] - cell[k] * 16 for k in range(3)]
                hi = [e["to"][k] - cell[k] * 16 for k in range(3)]
                assert min(lo) >= -16 and max(hi) <= 32, f"a turned box reaches out of its block: {e}"
                mine.append(moved_into(e, cell))
        parts.append(mine)
    placed = sum(1 for e in turned if any(all(c[k] * 16 <= e["rotation"]["origin"][k] < c[k] * 16 + 16 for k in range(2)) for c in cells))
    assert placed == len(turned), "every turned box belongs to one of the arch's blocks"
    return parts


def moved_into(e, cell):
    """`e` moved into its cell's block."""
    out = copy.deepcopy(e)
    for key in ("from", "to"):
        out[key] = [round(out[key][k] - cell[k] * 16, 4) for k in range(3)]
    if "rotation" in out:
        out["rotation"] = dict(out["rotation"], origin=[round(out["rotation"]["origin"][k] - cell[k] * 16, 4) for k in range(3)])
    return out


# ---------------------------------------------------------------- the Hanging Crock and Herb Rack

def jug(sc, cx, cz, y0):
    """A salt-glazed stoneware jug hung by its handle: a cream belly, its shoulder, neck and lip dipped in brown slip, its
    handle a loop out to the east."""
    el = crossed_y(sc, "jug_belly", smooth(STONEWARE), cx, cz, 3.5, y0, y0 + 2.5, down=solid(STONEWARE, 1))
    el += crossed_y(sc, "jug_dip", smooth(BROWN_GLAZE), cx, cz, 3.5, y0 + 2.5, y0 + 3.5)
    el += crossed_y(sc, "jug_shoulder", smooth(BROWN_GLAZE), cx, cz, 2.5, y0 + 3.5, y0 + 4.5)
    el.append(panel(sc, "jug_neck", smooth(BROWN_GLAZE), (cx - 0.75, y0 + 4.5, cz - 0.75), (cx + 0.75, y0 + 5.5, cz + 0.75)))
    el.append(panel(sc, "jug_lip", smooth(BROWN_GLAZE), (cx - 1.0, y0 + 5.5, cz - 1.0), (cx + 1.0, y0 + 6.0, cz + 1.0),
                    up=solid(SHADOW, 0)))
    # The handle: out from the shoulder, up and back into the neck.
    el.append(panel(sc, "jug_handle", smooth(BROWN_GLAZE), (cx + 1.25, y0 + 3.0, cz - 0.25), (cx + 1.75, y0 + 5.75, cz + 0.25)))
    el.append(panel(sc, "jug_handle", smooth(BROWN_GLAZE), (cx + 0.75, y0 + 5.25, cz - 0.25), (cx + 1.25, y0 + 5.75, cz + 0.25)))
    return el


def crock(sc, cx, cz, y0):
    """A red earthenware crock with a cream slip band, its lid on, hung from a wire bail."""
    el = crossed_y(sc, "crock", smooth(EARTHENWARE), cx, cz, 4.0, y0, y0 + 3.5, down=solid(EARTHENWARE, 1))
    el += crossed_y(sc, "crock_band", flat(STONEWARE), cx, cz, 4.5, y0 + 2.0, y0 + 2.5)
    el += crossed_y(sc, "crock_rim", flat(EARTHENWARE), cx, cz, 4.5, y0 + 3.5, y0 + 4.0)
    el += crossed_y(sc, "crock_lid", smooth(EARTHENWARE), cx, cz, 3.5, y0 + 4.0, y0 + 4.5)
    el.append(panel(sc, "crock_knob", smooth(EARTHENWARE), (cx - 0.5, y0 + 4.5, cz - 0.5), (cx + 0.5, y0 + 5.0, cz + 0.5)))
    wire = flat(IRON)
    el += [panel(sc, "bail", wire, (cx - 2.5, y0 + 3.0, cz - 0.25), (cx - 2.25, y0 + 6.5, cz + 0.25)),
           panel(sc, "bail", wire, (cx + 2.25, y0 + 3.0, cz - 0.25), (cx + 2.5, y0 + 6.5, cz + 0.25)),
           panel(sc, "bail", wire, (cx - 2.5, y0 + 6.5, cz - 0.25), (cx + 2.5, y0 + 6.75, cz + 0.25))]
    return el


def hook(sc, x, z, y0, y1):
    """An iron S-hook from y0 up over a rail at y1."""
    return [panel(sc, "hook", flat(IRON), (x - 0.25, y0, z - 0.25), (x + 0.25, y1, z + 0.25))]


def herb_bunch(sc, name, palette, cx, cz, top):
    """A bunch of dried herbs hung upside down from a rail: its stems bound in twine under the rail, five sprigs hanging
    from the binding, one straight down and four fanned out 22.5 degrees each way, each ending in its leafy head."""
    leaves = herbs(palette)
    tie = top - 1.25
    el = [panel(sc, f"{name}_tie", lengthwise(TWINE), (cx - 0.5, tie, cz - 0.5), (cx + 0.5, top, cz + 0.5)),
          panel(sc, f"{name}_stems", lengthwise(STRAW), (cx - 0.25, tie - 1.25, cz - 0.25), (cx + 0.25, tie, cz + 0.25))]
    hang = tie - 1.0
    for axis, angle in ((None, 0.0), ("z", 22.5), ("z", -22.5), ("x", 22.5), ("x", -22.5)):
        rot = fa.rotation((cx, hang, cz), axis, angle) if axis else None
        el.append(panel(sc, f"{name}_sprig", leaves, (cx - 0.375, hang - 4.0, cz - 0.375), (cx + 0.375, hang, cz + 0.375), rot=rot))
        el.append(panel(sc, f"{name}_head", leaves, (cx - 0.625, hang - 5.5, cz - 0.625), (cx + 0.625, hang - 3.5, cz + 0.625), rot=rot))
    return el


def onion_string(sc, cx, cz, top):
    """A string of onions hung from the middle rung: a straw plait down its middle, onions round it in turn, two garlic
    bulbs among them, each with its dry shoot into the plait."""
    el = [panel(sc, "plait", lengthwise(STRAW), (cx - 0.5, top - 8.0, cz - 0.5), (cx + 0.5, top, cz + 0.5))]
    for x, z, y, palette in ((cx - 1.5, cz + 0.25, top - 3.0, ONION), (cx + 1.25, cz - 0.75, top - 4.25, ONION),
                             (cx - 1.0, cz - 1.0, top - 5.5, GARLIC), (cx + 1.0, cz + 0.75, top - 6.5, ONION),
                             (cx - 0.25, cz + 0.25, top - 8.5, ONION), (cx + 0.25, cz + 1.5, top - 2.25, GARLIC)):
        d = 2.5 if palette is ONION else 2.0
        el += crossed_y(sc, "onion" if palette is ONION else "garlic", smooth(palette, glint=palette is ONION), x, z, d, y - 1.0, y + 0.75)
        el.append(panel(sc, "shoot", lengthwise(ONION_TOP if palette is ONION else STRAW), (x - 0.25, y + 0.75, z - 0.25),
                        (x + 0.25, y + 1.5, z + 0.25)))
    return el


def hanging_crock_rack():
    """The Hanging Crock and Herb Rack, hung under a ceiling facing north: a frame of two oak rails and three turned rungs
    on four hemp ropes from iron eyes in the ceiling. Hung from it: a salt-glazed stoneware jug by its handle, a lidded red
    earthenware crock on its bail, a string of onions and garlic from the middle rung, and a bunch of dried sage and one of
    thyme. Decoration: it dries nothing (herb bundles and the Drying Rack do that)."""
    sc = Sculpt("hanging_crock_rack", 4602, 64)
    rail = lengthwise(TIMBER)
    el = []
    for x in (2.25, 13.25):
        for z in (4.5, 11.5):
            el.append(panel(sc, "eye", flat(IRON), (x - 0.5, 15.5, z - 0.75), (x + 1.0, 16.0, z + 0.75)))
            el.append(panel(sc, "rope", lengthwise(ROPE), (x, 11.75, z - 0.25), (x + 0.5, 15.5, z + 0.25)))
            el.append(panel(sc, "lashing", flat(ROPE), (x - 0.25, 9.75, z - 1.0), (x + 0.75, 11.75, z + 1.0)))
    for z0 in (3.75, 10.75):
        el.append(panel(sc, "rail", rail, (1.0, 10.0, z0), (15.0, 11.5, z0 + 1.5), east=tdd.rings(), west=tdd.rings()))
    for x in (4.0, 8.0, 12.0):
        el.append(panel(sc, "rung", lengthwise(TIMBER), (x - 0.5, 10.25, 5.25), (x + 0.5, 11.25, 10.75)))
    # The jug from the front rail, the crock from the back one.
    el += hook(sc, 6.0, 4.5, 8.0, 10.0) + jug(sc, 4.5, 4.5, 2.5)
    el += hook(sc, 10.5, 11.5, 9.5, 10.0) + crock(sc, 10.5, 11.5, 2.75)
    # The onion string from the middle rung, the herbs from the rails between.
    el += onion_string(sc, 8.0, 8.0, 10.25)
    el += herb_bunch(sc, "sage", SAGE, 11.25, 4.5, 10.0)
    el += herb_bunch(sc, "thyme", THYME, 4.75, 11.5, 10.0)
    sc.models["whole"] = el
    return sc


# ---------------------------------------------------------------- the Folded Linen Stack

def linen_layer(sc, name, palette, lo, hi, top=None):
    """A folded cloth: its body, and where it is a pixel or more thick, its fold along the front, a quarter pixel in at
    top and bottom so the fold reads round."""
    el = [panel(sc, name, folded(palette), lo, hi, up=top or cloth_top(palette))]
    if hi[1] - lo[1] >= 1.0:
        el.append(panel(sc, f"{name}_fold", folded(palette), (lo[0] + 0.25, lo[1] + 0.25, lo[2] - 0.25), (hi[0] - 0.25, hi[1] - 0.25, lo[2])))
    return el


def folded_linen_stack():
    """The Folded Linen Stack, facing north: the feast's table linen folded and stacked, none quite square on the one
    under it: a cream linen cloth, a mustard one, a red gingham, rust napkins, cream linen again and a folded hessian
    runner on top, tied crosswise with twine knotted on top over a wheat sprig and a maple leaf; beside it, a short pile of
    napkins, rust, gingham and linen."""
    sc = Sculpt("folded_linen_stack", 4603, 64)
    el = (linen_layer(sc, "linen", LINEN, (2.5, 0.0, 4.0), (10.5, 1.5, 10.5))
          + linen_layer(sc, "mustard", MUSTARD, (2.75, 1.5, 4.25), (10.5, 2.5, 10.25))
          + linen_layer(sc, "gingham", GINGHAM, (2.5, 2.5, 4.0), (10.25, 3.75, 10.5), top=check(GINGHAM, LINEN))
          + linen_layer(sc, "napkins", NAPKIN, (2.5, 3.75, 4.25), (10.5, 4.75, 10.25))
          + linen_layer(sc, "linen", LINEN, (2.75, 4.75, 4.0), (10.5, 6.0, 10.5))
          + linen_layer(sc, "hessian", HESSIAN, (2.5, 6.0, 4.0), (10.5, 6.75, 10.5)))
    twine = lengthwise(TWINE)
    # Crosswise twine: over the top along x and along z, down every side to the bottom.
    el += [panel(sc, "twine", twine, (2.25, 6.75, 7.0), (10.75, 7.0, 7.5)),
           panel(sc, "twine", twine, (2.25, 0.0, 7.0), (2.5, 6.75, 7.5)),
           panel(sc, "twine", twine, (10.5, 0.0, 7.0), (10.75, 6.75, 7.5)),
           panel(sc, "twine", twine, (6.25, 6.75, 3.5), (6.75, 7.0, 10.75)),
           panel(sc, "twine", twine, (6.25, 0.0, 3.5), (6.75, 6.75, 3.75)),
           panel(sc, "twine", twine, (6.25, 0.0, 10.5), (6.75, 6.75, 10.75))]
    el.append(panel(sc, "knot", twine, (5.75, 7.0, 6.5), (7.25, 7.75, 8.0)))
    # A wheat sprig tucked under the knot, its ear to the north-east; a maple leaf by it.
    el.append(panel(sc, "wheat_stalk", lengthwise(WHEAT), (3.5, 6.75, 7.75), (9.0, 7.25, 8.25), rot=fa.rotation((6.5, 7.0, 8.0), "y", 22.5)))
    el.append(panel(sc, "wheat_ear", ears(), (8.75, 6.75, 6.375), (10.25, 7.5, 7.125), rot=fa.rotation((9.5, 7.1, 6.75), "y", 22.5)))
    el.append(flat_leaf(sc, "small", RED_LEAF, 5.0, 6.75, 6.85, -22.5))
    # The napkins beside it.
    el += (linen_layer(sc, "napkins", NAPKIN, (11.25, 0.0, 7.25), (14.75, 0.75, 11.0))
           + linen_layer(sc, "gingham", GINGHAM, (11.4, 0.75, 7.4), (14.6, 1.5, 10.85), top=check(GINGHAM, LINEN))
           + linen_layer(sc, "linen", LINEN, (11.25, 1.5, 7.25), (14.75, 2.25, 11.0)))
    sc.models["whole"] = el
    return sc


# ---------------------------------------------------------------- the Copper Chafing Dish

def bevel(palette, fill, ridge=False):
    """Clean metal: one flat fill (`fill`), a one-texel bevel lit along its top row and left column and shaded along its
    bottom row and right column; with `ridge`, its top row the brightest tone, a highlight running along the edge of a
    lid's step."""
    top = len(palette) - 1

    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                if y == 0 and p.h > 1:
                    k = top if ridge else fill + 1
                elif y == p.h - 1 and p.h > 2 or x == p.w - 1 and p.w > 2:
                    k = fill - 1
                elif x == 0 and p.w > 2:
                    k = fill + 1
                else:
                    k = fill
                p.put(x, y, shade(palette, k))
    return tagged(paint, "bevel", palette, fill, ridge)


# A spirit burner's flame, banded from its deep orange edge in to its pale core at the wick (`.` clear).
FLAME_MAP = (".d.",
             "dod",
             "oyo",
             "yWy")


def flame(sc, x, z, y):
    """The burner's flame, cut out, 3 pixels across and 4 high at a texel a pixel: two planes crossed (seen from the front
    and the side), drawn full-bright."""
    colours = {"d": shade(FLAME, 0), "o": shade(FLAME, 1), "y": shade(FLAME, 2), "W": shade(FLAME, 3)}
    uv = sc.piece("burner_flame", 3, 4, sprite(FLAME_MAP, colours))
    return lit([fa.plane_xy(x - 1.5, x + 1.5, y, y + 4.0, z, uv), fa.plane_zy(z - 1.5, z + 1.5, y, y + 4.0, x, uv)])


def copper_chafing_dish():
    """The Copper Chafing Dish, facing north: an oval copper food pan with a brass rim on a wrought-iron stand of four
    legs on pad feet, its domed copper lid stepped up to a brass knob, brass loop handles at its ends, and under it, on a
    shelf the legs pass through, a brass spirit burner whose wick flint and steel lights (`lit`: its flame, deep orange to
    a pale core, under the pan). Clean copper: flat fills, each face bevelled lit at its top left, each lid step's edge a
    highlight."""
    sc = Sculpt("copper_chafing_dish", 4604, 64)
    iron = flat(IRON)
    body, lid, top = bevel(COPPER, 3), bevel(COPPER, 3, ridge=True), bevel(COPPER, 4)
    el = []
    legs_top, pan0 = 6.5, 7.25
    for x in (3.0, 12.25):
        for z in (5.0, 10.25):
            el.append(panel(sc, "foot", iron, (x - 0.5, 0.0, z - 0.5), (x + 1.25, 0.5, z + 1.25)))
            el.append(panel(sc, "leg", iron, (x, 0.5, z), (x + 0.75, legs_top, z + 0.75)))
    el += [panel(sc, "frame", iron, (3.0, legs_top, 5.0), (13.0, pan0, 5.75)), panel(sc, "frame", iron, (3.0, legs_top, 10.25), (13.0, pan0, 11.0)),
           panel(sc, "frame", iron, (3.0, legs_top, 5.75), (3.75, pan0, 10.25)), panel(sc, "frame", iron, (12.25, legs_top, 5.75), (13.0, pan0, 10.25))]
    # The burner on a shelf the legs pass through, on their feet.
    el.append(panel(sc, "shelf", iron, (3.0, 0.5, 5.75), (13.0, 1.0, 10.25)))
    el += crossed_y(sc, "burner", bevel(BRASS, 2), 8.0, 8.0, 3.0, 1.0, 2.25)
    el += crossed_y(sc, "burner_cap", bevel(BRASS, 3), 8.0, 8.0, 2.0, 2.25, 2.5)
    el.append(panel(sc, "wick", solid(SHADOW, 0), (7.75, 2.5, 7.75), (8.25, 3.0, 8.25)))
    # The pan, its brass rim, and the lid domed in three steps to its knob.
    rim0 = pan0 + 2.0
    el += [panel(sc, "pan", body, (2.5, pan0, 5.25), (13.5, rim0, 10.75), down=solid(COPPER, 2)),
           panel(sc, "pan", body, (3.5, pan0, 4.5), (12.5, rim0, 11.5), down=solid(COPPER, 2))]
    el += [panel(sc, "rim", flat(BRASS), (2.25, rim0, 5.0), (13.75, rim0 + 0.5, 11.0)),
           panel(sc, "rim", flat(BRASS), (3.25, rim0, 4.25), (12.75, rim0 + 0.5, 11.75))]
    l0 = rim0 + 0.5
    el += [panel(sc, "lid", lid, (3.0, l0, 5.5), (13.0, l0 + 0.75, 10.5), up=top),
           panel(sc, "lid", lid, (4.0, l0, 4.75), (12.0, l0 + 0.75, 11.25), up=top),
           panel(sc, "lid", lid, (4.5, l0 + 0.75, 6.25), (11.5, l0 + 1.5, 9.75), up=top),
           panel(sc, "lid", lid, (5.5, l0 + 0.75, 5.5), (10.5, l0 + 1.5, 10.5), up=top),
           panel(sc, "lid", lid, (6.5, l0 + 1.5, 6.75), (9.5, l0 + 2.0, 9.25), up=top)]
    k0 = l0 + 2.0
    el += [panel(sc, "knob", bevel(BRASS, 3), (7.25, k0, 7.25), (8.75, k0 + 0.75, 8.75)),
           panel(sc, "knob", flat(BRASS), (7.5, k0 + 0.75, 7.5), (8.5, k0 + 1.0, 8.5))]
    # Brass loop handles at its ends: a grip out from the pan on two arms.
    for grip, arm in (((1.5, 2.0), (2.0, 2.5)), ((14.0, 14.5), (13.5, 14.0))):
        el.append(panel(sc, "handle", flat(BRASS), (grip[0], pan0 + 0.75, 6.75), (grip[1], pan0 + 2.0, 9.25)))
        for z in (6.75, 8.75):
            el.append(panel(sc, "handle_arm", flat(BRASS), (arm[0], pan0 + 1.25, z), (arm[1], pan0 + 1.75, z + 0.5)))
    sc.models["whole"] = el
    sc.models["lit"] = el + flame(sc, 8.0, 8.0, 2.75)
    return sc


# ---------------------------------------------------------------- the Pumpkin Carving Bench

def minus(rect, holes):
    """The rectangle `rect` (x0, y0, x1, y1) less the rectangles `holes`, as rectangles: cut into bands at every hole's
    top and bottom, each band split round the holes it crosses."""
    x0, y0, x1, y1 = rect
    ys = sorted({y0, y1, *[v for h in holes for v in (h[1], h[3]) if y0 < v < y1]})
    out = []
    for a, b in zip(ys, ys[1:]):
        cuts = sorted((h[0], h[2]) for h in holes if h[1] <= a and h[3] >= b)
        x = x0
        for c0, c1 in cuts:
            if c0 > x:
                out.append((x, a, c0, b))
            x = max(x, c1)
        if x < x1:
            out.append((x, a, x1, b))
    return out


# The carved pumpkin's face, cut through its front wall (x0, y0, x1, y1 in pixels): two eyes and the half-cut mouth, the
# knife stuck in at the mouth's end.
EYES = ((4.75, 11.0, 6.0, 12.25), (8.0, 11.0, 9.25, 12.25))
MOUTH = ((5.0, 9.75, 7.75, 10.5),)


def carved_pumpkin(sc):
    """The pumpkin being carved, standing on the bench, as round as the market's pumpkins (a narrower foot and shoulder
    under and over two crossed boxes): its lid cut off, the cut flesh showing down inside the opening and the hollow dark
    below it; two eyes and half its mouth cut through its front wall, the hollow showing behind them and the cut flesh
    round their edges; the carving knife stuck in at the end of the mouth, its handle out to the front."""
    rind = ribbed(PUMPKIN)
    top_rind = solid(PUMPKIN, 3)
    cut = solid(FLESH, 4)
    hollow = solid(HOLLOW, 1)
    y_body, y_cut, y_top, y_shoulder = 9.5, 12.5, 13.0, 13.5
    el = [panel(sc, "pk_foot", rind, (4.0, 9.0, 6.0), (10.0, y_body, 11.0))]
    # The front wall with its face cut through, one piece to each rectangle left.
    for x0, y0, x1, y1 in minus((4.0, y_body, 10.0, y_cut), EYES + MOUTH):
        over = {"north": rind}
        if x0 <= 4.0:
            over["west"] = rind
        if x1 >= 10.0:
            over["east"] = rind
        over["down"] = rind if y0 <= y_body else cut
        el.append(panel(sc, "pk_front", cut, (x0, y0, 5.0), (x1, y1, 6.0), **over))
    # The hollow behind it, the sides and the back.
    el += [panel(sc, "pk_hollow", rind, (4.0, y_body, 6.0), (10.0, y_cut, 11.0), north=hollow, up=hollow),
           panel(sc, "pk_side", rind, (3.0, y_body, 6.0), (4.0, y_cut, 11.0)), panel(sc, "pk_side", rind, (10.0, y_body, 6.0), (11.0, y_cut, 11.0)),
           panel(sc, "pk_back", rind, (4.0, y_body, 11.0), (10.0, y_cut, 12.0))]
    # The top of its body and its shoulder, round its opening (x 5..9, z 7..10): rind on top, cut flesh down inside.
    hole = (5.0, 7.0, 9.0, 10.0)
    layers = ((y_cut, y_top, [((3.0, 6.0), (5.0, 11.0), "east"), ((9.0, 6.0), (11.0, 11.0), "west"), ((5.0, 6.0), (9.0, 7.0), "south"),
                               ((5.0, 10.0), (9.0, 11.0), "north"), ((4.0, 5.0), (10.0, 6.0), None), ((4.0, 11.0), (10.0, 12.0), None)]),
              (y_top, y_shoulder, [((4.0, 6.0), (5.0, 11.0), "east"), ((9.0, 6.0), (10.0, 11.0), "west"), ((5.0, 6.0), (9.0, 7.0), "south"),
                                   ((5.0, 10.0), (9.0, 11.0), "north")]))
    for lo_y, hi_y, pieces in layers:
        for lo, hi, inner in pieces:
            over = {"up": top_rind}
            if inner:
                over[inner] = cut
            el.append(panel(sc, "pk_top", rind, (lo[0], lo_y, lo[1]), (hi[0], hi_y, hi[1]), **over))
    assert hole == (5.0, 7.0, 9.0, 10.0)
    # The knife, stuck in at the mouth's east end, its handle out to the front and up.
    knife = fa.rotation((7.75, 10.125, 5.5), "x", 22.5)
    el.append(panel(sc, "blade", flat(BLADE), (7.625, 9.625, 3.5), (7.875, 10.625, 6.5), rot=knife))
    el.append(panel(sc, "knife_handle", lengthwise(HANDLE), (7.375, 9.75, 1.0), (8.125, 10.5, 3.5), rot=knife))
    return el


def curl(sc, x, z, y, turn, rise=False):
    """A shaving of rind curled off the pumpkin: a strip lying turned `turn` about the vertical, its end curling up."""
    strip = solid(FLESH, 3)
    el = [panel(sc, "shaving", strip, (x, y, z), (x + 2.0, y + 0.25, z + 0.5), up=ribbed(PUMPKIN),
                rot=fa.rotation((x + 1.0, y, z + 0.25), "y", turn))]
    if rise:
        el.append(panel(sc, "shaving_curl", strip, (x + 2.0, y, z), (x + 3.0, y + 0.25, z + 0.5), up=ribbed(PUMPKIN),
                        rot=fa.rotation((x + 2.0, y, z + 0.25), "z", 45.0)))
    return el


def pumpkin_carving_bench():
    """The Pumpkin Carving Bench, facing north: a low plank bench on four legs with stretchers, and on it a pumpkin half
    carved (lid off, two eyes cut, the knife stuck in for its mouth), its lid with its stalk beside it, a scoop, the
    candle stub that will go inside it, a wooden bowl of scooped guts and seeds and curls of rind shaved off; under it a
    pumpkin still waiting, and shavings fallen on the floor. Decoration: pumpkins are carved where they stand with the Carving Knife."""
    sc = Sculpt("pumpkin_carving_bench", 4605, 128)
    el = [panel(sc, "bench_top", planks(BENCH, board=3, nails=True), (0.5, 7.5, 3.0), (15.5, 9.0, 13.0))]
    legs = lengthwise(WOOD)
    for x in (1.5, 13.0):
        for z in (4.0, 10.5):
            el.append(panel(sc, "leg", legs, (x, 0.0, z), (x + 1.5, 7.5, z + 1.5)))
        el.append(panel(sc, "stretcher", legs, (x + 0.25, 2.0, 5.5), (x + 1.25, 3.0, 10.5)))
    el.append(panel(sc, "stretcher", legs, (2.75, 2.0, 7.5), (13.25, 3.0, 8.5)))
    el += carved_pumpkin(sc)
    # The lid, cut flesh down, its stalk up.
    el += crossed_y(sc, "lid", ribbed(PUMPKIN), 13.25, 5.25, 3.0, 9.0, 10.25, up=crown(PUMPKIN), down=solid(FLESH, 4))
    el.append(panel(sc, "lid_stalk", lengthwise(STEM), (12.75, 10.25, 4.75), (13.75, 11.5, 5.75)))
    # The scoop and the stencil.
    el += [panel(sc, "scoop_handle", flat(SILVER), (0.75, 9.0, 3.5), (3.5, 9.25, 4.0)),
           panel(sc, "scoop_bowl", flat(SILVER), (3.5, 9.0, 3.25), (4.75, 9.5, 4.5), up=solid(SILVER, 1))]
    # The candle stub that will go inside it, waiting at the back.
    el += [panel(sc, "candle", smooth(tdd.WAX, foot=False), (1.0, 9.0, 10.0), (2.25, 10.75, 11.25), up=solid(tdd.WAX, 3)),
           panel(sc, "wick", solid(SHADOW, 0), (1.5, 10.75, 10.5), (1.75, 11.25, 10.75))]
    # The bowl of guts and seeds.
    bowl = lengthwise(TIMBER)
    el += ppd.walls(sc, "bowl", bowl, 11.5, 8.5, 15.0, 12.5, 0.75, 9.0, 11.0, top=solid(TIMBER, 4))
    el.append(panel(sc, "bowl_floor", bowl, (12.25, 9.0, 9.25), (14.25, 9.5, 11.75)))
    el.append(panel(sc, "guts", guts(), (12.25, 9.5, 9.25), (14.25, 10.5, 11.75)))
    for sx, sz in ((12.5, 9.75), (13.25, 10.75), (12.75, 11.0), (13.5, 9.5)):
        el.append(panel(sc, "seed", smooth(SEED), (sx, 10.5, sz), (sx + 0.75, 10.75, sz + 0.5)))
    # Shavings: curls of rind on the bench, and two fallen on the floor.
    el += curl(sc, 8.5, 3.5, 9.0, 22.5, rise=True) + curl(sc, 0.75, 6.0, 9.0, -45.0) + curl(sc, 11.0, 7.25, 9.0, 45.0)
    el += curl(sc, 9.0, 6.0, 0.0, -22.5)
    for sx, sz in ((1.5, 8.0), (6.0, 3.75)):
        el.append(panel(sc, "seed", smooth(SEED), (sx, 9.0, sz), (sx + 0.75, 9.25, sz + 0.5)))
    # A pumpkin still waiting under the bench.
    el += pumpkin(sc, "spare_pumpkin", 6.5, 10.75, 4.0, 0.0, 3.5)
    sc.models["whole"] = el
    return sc


# ---------------------------------------------------------------- building and files

BUILDERS = {"timber_harvest_arch": timber_harvest_arch, "hanging_crock_rack": hanging_crock_rack,
            "folded_linen_stack": folded_linen_stack, "copper_chafing_dish": copper_chafing_dish,
            "pumpkin_carving_bench": pumpkin_carving_bench}
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
bounds = tdd.bounds


def turned(model, facing, **extra):
    return {"model": rid(f"block/{model}"), **({"y": FACINGS[facing]} if FACINGS[facing] else {}), **extra}


def assets(root, write, lang):
    """Each piece's block models (drawn whole, separated; the arch cut into its parts), its blockstate by its kind's
    states, item model and item."""
    write = fa.closing_writer(write, _image_of)
    models = root / "models" / "block"
    lang.update(tt.names())
    for block, spec in tt.PROPS.items():
        sc = build(block)
        kind = spec["kind"]
        states = root / "blockstates" / f"{block}.json"
        if kind == "arch":
            parts = arch_parts(sc.models["whole"])
            for part, elements in enumerate(parts):
                write(models / f"{block}_{part}.json", fa.model(block, elements))
            write(states, {"variants": {f"facing={f},lit={l},part={p}": turned(f"{block}_{p}", f)
                                        for f in FACINGS for l in ("false", "true") for p in range(len(parts))}})
            write(root / "models" / "item" / f"{block}.json",
                  fa.model(block, hfd.item_model(sc.models["whole"], (tt.ARCH["width"], tt.ARCH["height"], 1), 1 / tt.ARCH["width"]), hfd.DISPLAY))
            write(root / "items" / f"{block}.json", {"model": {"type": "minecraft:model", "model": rid(f"item/{block}")}})
            continue
        whole = d20d.separated(sc.models["whole"])
        if kind == "candles":
            write(models / f"{block}.json", fa.model(block, whole))
            write(models / f"{block}_lit.json", fa.model(block, d20d.separated(sc.models["lit"])))
            write(states, {"variants": {f"facing={f},lit={l}": turned(block + ("_lit" if l == "true" else ""), f)
                                        for f in FACINGS for l in ("false", "true")}})
        else:
            write(models / f"{block}.json", fa.model(block, whole))
            write(states, {"variants": {f"facing={f}": turned(block, f) for f in FACINGS}})
        write(root / "models" / "item" / f"{block}.json", fa.model(block, whole, DISPLAY))
        write(root / "items" / f"{block}.json", {"model": {"type": "minecraft:model", "model": rid(f"item/{block}")}})


def loot(out, write):
    """Each piece drops itself once; the arch from its first block only (its other blocks break with it, dropping
    nothing), as the Giant Cornucopia does."""
    from decor_data import self_drop
    for block, spec in tt.PROPS.items():
        if spec["kind"] == "arch":
            write(out / f"{block}.json", {"type": "minecraft:block", "pools": [{"condition": {"type": "minecraft:all_of", "terms": [
                {"type": "minecraft:survives_explosion"},
                {"type": "minecraft:match_block", "blocks": rid(block), "state": {"part": "0"}}]},
                "entries": [{"type": "minecraft:item", "name": rid(block)}], "rolls": 1}], "random_sequence": rid(f"blocks/{block}")})
        else:
            write(out / f"{block}.json", self_drop(block))


def tags(tags):
    """The copper dish comes off quickest with a pickaxe (thanksgiving_town.PICKAXE); wood, cloth and the rest with an
    axe."""
    for block in tt.PROPS:
        tags.add("block", "minecraft:mineable/pickaxe" if block in tt.PICKAXE else "minecraft:mineable/axe", rid(block))
    # What a planter takes besides small flowers (PlanterBlock.HARVEST).
    for item in tt.PLANTER_HARVEST["items"]:
        tags.add("item", tt.PLANTER_HARVEST["tag"], item)


def textures():
    return {("block", block): build(block).atlas.img for block in tt.PROPS}


# ---------------------------------------------------------------- running it alone

ROOT = Path(__file__).resolve().parents[1]
RES = ROOT / "src" / "main" / "resources"


def condition(switch):
    """A feature switch's load condition, as agriculture_data.condition writes it (that module is not imported here: its
    imports reach every generator, some needing pinned fonts and vanilla files)."""
    return {"condition": f"{MOD}:feature_enabled", "feature": switch}


def recipe_files():
    """These pieces' recipe files, as agriculture_data.recipes writes tools/agriculture.py SHAPELESS."""
    out = {}
    for recipe in tt.SHAPELESS:
        switches = (recipe.get("switch", tt.FEATURE), *recipe.get("features", []))
        data = {"fabric:load_conditions": [condition(f) for f in switches],
                "type": "minecraft:crafting_shapeless", "category": recipe.get("category", "misc")}
        data.update({"ingredients": recipe["inputs"], "result": {"id": rid(recipe["result"]), "count": recipe["count"]}})
        out[f"{recipe['id']}.json"] = data
    return out


def files():
    """Every file these pieces own, as the pipeline would write it: {path under src/main/resources: bytes}, and the shared
    entries they add (names, mining tags) beside them."""
    import model_writer
    out = {}

    def write(path, obj):
        obj = copy.deepcopy(obj)
        if isinstance(obj, dict) and obj.get("elements") and "models" in path.parts:
            model_writer.finish_elements(obj["elements"])
        out[path.relative_to(RES).as_posix()] = (json.dumps(obj, indent=2) + "\n").encode("utf-8")
    lang = {}
    assets(RES / "assets" / MOD, write, lang)
    loot(RES / "data" / MOD / "loot_table" / "blocks", write)
    for name, data in recipe_files().items():
        write(RES / "data" / MOD / "recipe" / name, data)
    import io
    for (kind, name), img in textures().items():
        buf = io.BytesIO()
        img.save(buf, format="PNG", optimize=True)
        out[f"assets/{MOD}/textures/{kind}/{name}.png"] = buf.getvalue()

    class Tags:
        def __init__(self):
            self.entries = {}

        def add(self, registry, tag, value):
            self.entries.setdefault((registry, tag), [])
            if value not in self.entries[(registry, tag)]:
                self.entries[(registry, tag)].append(value)
    collected = Tags()
    tags(collected)
    shared = {}
    for (registry, tag), values in collected.entries.items():
        namespace, path_ = tag.split(":")
        if namespace == "minecraft":
            shared[(registry, tag)] = values
        else:
            # A tag of these pieces' own: its own file, as the pipeline's Tags.write writes it.
            out[f"data/{namespace}/tags/{registry}/{path_}.json"] = (json.dumps({"replace": False, "values": values}, indent=2) + "\n").encode("utf-8")
    return out, lang, shared


def insert_sorted_lines(path, entries, kind):
    """Adds `entries` to a shared JSON file without rewriting any line of it: a "values" list (`kind` "list", entries are
    strings) or an object (`kind` "object", entries are (key, value) pairs). Each goes in, as its own line, where it sorts
    within the file's leading sorted run (the pipeline's own part), after the entry sorting just before it; an entry
    already present is left alone."""
    import bisect
    lines = path.read_text(encoding="utf-8").split("\n")
    if kind == "list":
        rows = [(i, json.loads(line.strip().rstrip(","))) for i, line in enumerate(lines) if line.startswith("    \"")]
        new = [(value, f"    {json.dumps(value, ensure_ascii=False)}") for value in entries]
    else:
        rows = [(i, line.strip().split('": ', 1)[0][1:]) for i, line in enumerate(lines) if line.startswith("  \"")]
        new = [(key, f"  {json.dumps(key, ensure_ascii=False)}: {json.dumps(value, ensure_ascii=False)}") for key, value in entries]
    present = {key for _, key in rows}
    run = 1
    while run < len(rows) and rows[run - 1][1] <= rows[run][1]:
        run += 1
    head = rows[:run]
    keys = [key for _, key in head]
    for key, text in sorted(new, reverse=True):
        if key in present:
            continue
        at = bisect.bisect_left(keys, key)
        line_index = head[at - 1][0] + 1 if at > 0 else head[0][0]
        lines.insert(line_index, text + ",")
        for j in range(at, len(head)):
            head[j] = (head[j][0] + 1, head[j][1])
    path.write_text("\n".join(lines), encoding="utf-8")


def main(argv):
    """Writes these pieces' own files; with --shared, also adds their names to en_us.json and their mining tags to
    minecraft:mineable/axe and /pickaxe, each a new line where it sorts, nothing else rewritten."""
    out, lang, tag_entries = files()
    for rel, data in sorted(out.items()):
        path = RES / rel
        path.parent.mkdir(parents=True, exist_ok=True)
        if not path.exists() or path.read_bytes() != data:
            path.write_bytes(data)
            print("wrote", rel)
    if "--shared" in argv:
        insert_sorted_lines(RES / "assets" / MOD / "lang" / "en_us.json", sorted(lang.items()), "object")
        for (registry, tag), values in tag_entries.items():
            namespace, path_ = tag.split(":")
            insert_sorted_lines(RES / "data" / namespace / "tags" / registry / f"{path_}.json", values, "list")
            print("added to", tag)
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))

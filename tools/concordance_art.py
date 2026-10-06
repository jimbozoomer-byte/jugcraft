"""Art for the Arcane Concordance's first content (tools/concordance.py; docs/features/arcane-concordance-first-light.md).

Every pixel is drawn here by code, with no randomness at all, so the same run always writes the same files. Nothing is
traced or copied from another game or mod. The style follows docs/ART_DIRECTION.md:

- the Initiate's Wand and the Kindled Lantern (unlit and lit) are 64x64 items drawn with tools/hd_art.py, like the
  other high-detail items. The wand lies on the held-item diagonal (grip bottom left, crystal top right) and is early
  tier: turned dark wood, copper and brass, not dieselpunk. The two lanterns share every shape, so they read as one
  item; only the glass, the crystal wick and the light on the cage change;
- the codex (the Arcane Concordance book) and the Kindle spell icon are 32x32 pixel art: flat fills from four or five
  shades a material, lit from the top left, a dark outline;
- the Lampwright's Bench faces are opaque block textures: an oak desk top in planks with a brass inlay (32x32), darker
  oak for its legs and apron (32x32), polished brass with a bevel (16x16) and the lens, violet glass in a brass rim
  (16x16), which is also the Kindled mote's particle.

Called from tools/generate_textures.py: draw_all(save).
"""
import math

from PIL import Image

import clean_metal as cm
import hd_art as hd
from construction_art import Tool

# ------------------------------------------------------------------------------------------------------- palettes

# 64x64 materials (hd_art colour ramps, darkest first).
DARK_WOOD = hd.Material([(30, 18, 12), (48, 30, 19), (68, 43, 27), (90, 59, 37), (114, 78, 50), (140, 100, 66)],
                        0.25, 10)
COPPER = hd.Material([(74, 32, 18), (116, 54, 30), (160, 84, 48), (200, 118, 72), (230, 158, 110), (250, 206, 168)],
                     0.6, 20)
AMETHYST = hd.Material([(84, 58, 128), (118, 90, 166), (154, 126, 204), (188, 164, 232), (218, 202, 248),
                        (246, 240, 255)], 0.7, 30, 0.35)

# Flat colours (painted directly, not shaded) for glass, light and glints.
GLINT_GOLD = [(214, 164, 70), (246, 210, 120), (255, 240, 190)]
WHITE = (255, 255, 250)
# Lantern glass, unlit: dim and cool, a faint violet.
PANE_DIM = [(34, 30, 54), (46, 42, 72), (60, 56, 92), (96, 92, 136)]
# Lantern glass, lit: warm gold-white, brightest round the wick.
PANE_LIT = [(196, 120, 44), (232, 166, 70), (250, 206, 118), (255, 234, 172), (255, 250, 226)]
# Rim light on the cage's inner edges when lit.
CAGE_GLOW = [(236, 168, 86), (255, 214, 140)]

# 32x32 and 16x16 palettes, darkest first.
VIOLET = [(18, 10, 30), (40, 22, 64), (60, 36, 94), (82, 52, 124), (108, 74, 152)]
BRASS = [(76, 50, 18), (120, 84, 32), (166, 124, 50), (206, 166, 76), (236, 206, 122), (250, 236, 180)]
GEM = [(70, 40, 112), (124, 86, 182), (178, 144, 232), (232, 218, 255)]
PAGE = [(150, 130, 96), (206, 190, 150), (232, 220, 186)]
OAK = [(84, 54, 28), (112, 76, 40), (142, 102, 58), (170, 128, 78), (196, 154, 100)]
OAK_DARK = [(38, 22, 12), (56, 35, 19), (76, 50, 28), (98, 66, 38), (122, 86, 52)]
SPELL_BG = [(20, 10, 34), (38, 20, 62), (54, 32, 88), (74, 48, 116), (98, 70, 146)]
HAND = [(118, 98, 160), (168, 150, 210), (206, 192, 240), (236, 228, 255)]
MOTE = [(226, 156, 54), (246, 196, 96), (255, 226, 150), (255, 246, 210), (255, 255, 248)]
LENS = [(46, 30, 82), (72, 50, 120), (104, 78, 160), (140, 116, 196), (188, 172, 232)]


# ------------------------------------------------------------------------------------------------- small helpers

def _flat_rect(c, x0, y0, x1, y1, colour):
    for y in range(int(y0), int(y1) + 1):
        for x in range(int(x0), int(x1) + 1):
            c.put(x, y, colour)


def _flat_disc(c, cx, cy, r, colour):
    for y in range(int(cy - r) - 1, int(cy + r) + 2):
        for x in range(int(cx - r) - 1, int(cx + r) + 2):
            if math.hypot(x + 0.5 - cx, y + 0.5 - cy) <= r:
                c.put(x, y, colour)


def _disc(img, cx, cy, r, colour):
    """A flat round fill on a PIL image (pixel centres within r of the centre)."""
    for y in range(img.height):
        for x in range(img.width):
            if math.hypot(x + 0.5 - cx, y + 0.5 - cy) <= r:
                cm.put(img, x, y, colour)


def _mask(img, rows, colours, ox=0, oy=0):
    """Paint a character mask: each character a key in colours, '.' left alone."""
    for y, row in enumerate(rows):
        for x, ch in enumerate(row):
            if ch in colours:
                cm.put(img, ox + x, oy + y, colours[ch])


def _outline(img, colour=(14, 8, 22)):
    """A one-pixel dark outline round every opaque pixel of a see-through image."""
    src = img.copy()
    sp = src.load()
    for y in range(img.height):
        for x in range(img.width):
            if sp[x, y][3]:
                continue
            for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                nx, ny = x + dx, y + dy
                if 0 <= nx < img.width and 0 <= ny < img.height and sp[nx, ny][3] == 255:
                    img.putpixel((x, y), tuple(colour) + (255,))
                    break
    return img


# ------------------------------------------------------------------------------------------------- the wand (64)

def initiate_wand():
    """The Initiate's Wand: a turned dark-wood shaft with a copper ferrule at the butt, a copper band at the top of the
    grip and another under the head, and a brass claw holding a pale amethyst point with a soft gold glint inside."""
    c = hd.Canvas()
    tool = Tool((9, 55), -45)
    deg = tool.angle

    # The shaft, from the butt up: the grip, a turned bead, then a long taper towards the head.
    c.capsule(tool(2, 0), tool(17, 0), 2.7, DARK_WOOD)
    for s in (6.5, 11.5):  # lathe-turned rings round the grip
        c.capsule(tool(s - 0.6, 0), tool(s + 0.6, 0), 3.0, DARK_WOOD)
    c.capsule(tool(17, 0), tool(31, 0), 2.2, DARK_WOOD)
    c.capsule(tool(31, 0), tool(46, 0), 1.8, DARK_WOOD)
    c.capsule(tool(21.5, 0), tool(22.5, 0), 2.7, DARK_WOOD)  # the bead above the first band

    # Copper: the ferrule capping the butt, the band at the top of the grip, the band under the head.
    c.capsule(tool(0.6, 0), tool(4.2, 0), 2.9, COPPER, flat_ends=True)
    c.disc(tool(0.4, 0), 1.6, COPPER, 0.8)
    c.capsule(tool(17.2, 0), tool(19.6, 0), 3.0, COPPER, flat_ends=True)
    c.capsule(tool(43.4, 0), tool(45.6, 0), 2.4, COPPER, flat_ends=True)

    # The brass claw: a cup at the top of the shaft and two prongs curling round the crystal (drawn behind it).
    c.capsule(tool(45.6, 0), tool(48.6, 0), 2.6, hd.BRASS, flat_ends=True)
    for side in (1, -1):
        pts = [tool(47.5, 2.0 * side), tool(50.5, 3.7 * side), tool(54.0, 3.4 * side)]
        for a, b in zip(pts, pts[1:]):
            c.capsule(a, b, 0.9, hd.BRASS)

    # The amethyst point: a hexagonal prism seen side on, four flat facets lit from the top left.
    ux, uy = tool.ux, tool.uy  # across the shaft, towards the top left
    ax, ay = tool.ax, tool.ay  # along the shaft, towards the tip
    up = (ux * 0.55, uy * 0.55, 0.83)
    down = (-ux * 0.55, -uy * 0.55, 0.83)
    tip_up = (ux * 0.45 + ax * 0.45, uy * 0.45 + ay * 0.45, 0.77)
    tip_down = (-ux * 0.35 + ax * 0.5, -uy * 0.35 + ay * 0.5, 0.79)
    base, shoulder, point = 48.0, 58.5, 64.5
    c.polygon([tool(base, 0), tool(base, 2.9), tool(shoulder, 3.3), tool(shoulder, 0)], AMETHYST, up)
    c.polygon([tool(base, 0), tool(shoulder, 0), tool(shoulder, -3.3), tool(base, -2.9)], AMETHYST, down)
    c.polygon([tool(shoulder, 0), tool(shoulder, 3.3), tool(point, 0)], AMETHYST, tip_up)
    c.polygon([tool(shoulder, 0), tool(point, 0), tool(shoulder, -3.3)], AMETHYST, tip_down)
    # The ridge between the facets, and a bright edge along the lit side.
    c.line(tool(base + 1, 0.2), tool(point - 1.5, 0.2), AMETHYST.ramp[4])
    c.line(tool(base + 2, 2.4), tool(shoulder - 1, 2.7), AMETHYST.ramp[5])

    # The soft gold glint deep inside: a small warm core with a pale heart, and a white catchlight on the tip.
    g = tool(53.5, 0.6)
    _flat_disc(c, g[0], g[1], 1.8, GLINT_GOLD[1])
    _flat_disc(c, g[0], g[1], 0.9, GLINT_GOLD[2])
    t = tool(60.5, 1.2)
    c.put(int(t[0]), int(t[1]), WHITE)

    # The claw's front prong, over the crystal's base.
    c.capsule(tool(47.8, -0.6), tool(51.0, -1.2), 0.8, hd.BRASS)
    return c.finish()


# ----------------------------------------------------------------------------------------------- the lantern (64)

def kindled_lantern(lit):
    """A small hand lantern: a brass ring handle and roof, copper corner posts and rims, a front pane between them
    (the lantern has four; one faces the viewer) and a crystal wick standing in a brass cup. Unlit, the glass is dim
    and cool and the crystal pale; lit, warm gold-white light fills the glass, brightest round the crystal, and the
    cage's inner edges catch it. Both states share every outline."""
    c = hd.Canvas()
    cx = 32

    # The ring handle on top (its lower half hides behind the roof) and the post it hangs from.
    c.ring((cx, 9.5), 6.4, 4.0, hd.BRASS)
    c.capsule((cx, 14), (cx, 18), 2.2, hd.BRASS, flat_ends=True)

    # The roof: a low pyramid with a row of vent slots, on a copper rim.
    roof = [(cx - 6, 17), (cx + 6, 17), (cx + 15, 23), (cx - 15, 23)]
    c.polygon(roof, hd.BRASS, (0, -0.55, 0.83))
    c.polygon([(cx - 6, 17), (cx + 6, 17), (cx + 5, 18), (cx - 5, 18)], hd.BRASS, (0, -0.2, 0.98))
    for vx in (cx - 8, cx - 3, cx + 2, cx + 7):
        _flat_rect(c, vx, 20, vx + 1, 21, hd.BRASS.ramp[0])
        c.put(vx, 22, hd.BRASS.ramp[2])
        c.put(vx + 1, 22, hd.BRASS.ramp[2])
    c.box((cx, 24.5), 16, 1.9, 0, COPPER, bevel=1.2)

    # The glass, and what shows through it.
    x0, x1, y0, y1 = cx - 11, cx + 10, 27, 48
    if lit:
        _flat_rect(c, x0, y0, x1, y1, PANE_LIT[1])
        for r, colour in ((11.5, PANE_LIT[2]), (8.0, PANE_LIT[3]), (5.0, PANE_LIT[4])):
            for y in range(y0, y1 + 1):
                for x in range(x0, x1 + 1):
                    if math.hypot((x + 0.5 - cx) * 1.1, (y + 0.5 - 38) * 0.8) <= r:
                        c.put(x, y, colour)
        # The panes' corners stay a shade deeper, so the light reads as filling them from the middle.
        for x, y in ((x0, y0), (x1, y0), (x0, y1), (x1, y1)):
            c.put(x, y, PANE_LIT[0])
    else:
        _flat_rect(c, x0, y0, x1, y1, PANE_DIM[1])
        _flat_rect(c, x0, y1 - 3, x1, y1, PANE_DIM[0])  # the bottom of the glass, in shadow
    # The crystal wick, standing in its cup.
    if lit:
        facets = [(255, 252, 236), (255, 240, 196), (255, 255, 255)]
    else:
        facets = [AMETHYST.ramp[3], AMETHYST.ramp[1], AMETHYST.ramp[4]]
    left = [(cx - 3, 44), (cx - 3, 35), (cx, 31), (cx, 44)]
    right = [(cx, 44), (cx, 31), (cx + 3, 35), (cx + 3, 44)]
    c.polygon(left, AMETHYST)
    c.polygon(right, AMETHYST)
    for pts, colour in ((left, facets[0]), (right, facets[1])):
        xs, ys = [p[0] for p in pts], [p[1] for p in pts]
        for y in range(int(min(ys)), int(max(ys)) + 1):
            for x in range(int(min(xs)), int(max(xs)) + 1):
                if hd._inside(pts, x + 0.5, y + 0.5):
                    c.put(x, y, colour)
    for y in range(34, 43):  # the bright edge down the lit facet
        c.put(cx - 2, y, facets[2])
    c.box((cx, 45.5), 4.5, 1.6, 0, hd.BRASS, bevel=1.0)  # the cup
    # Highlights on the glass: two short diagonal streaks, upper left, over whatever is behind.
    streak = PANE_LIT[4] if lit else PANE_DIM[3]
    for i in range(5):
        c.put(x0 + 2 + i, y0 + 6 - i, streak)
    for i in range(3):
        c.put(x0 + 2 + i, y0 + 9 - i, streak)

    # Guard wires across the glass, top and bottom, standing off the pane.
    for wy in (30, 41):
        for x in range(x0, x1 + 1):
            if not (lit and cx - 3 <= x <= cx + 3 and wy == 41):
                c.put(x, wy, COPPER.ramp[3] if not lit else CAGE_GLOW[0])
            c.put(x, wy + 1, COPPER.ramp[1] if not lit else PANE_LIT[0])

    # The cage: copper corner posts with brass caps, the base rim and the foot.
    for px in (cx - 14, cx + 13):
        c.box((px + 0.5, 37.5), 2.4, 11.6, 0, COPPER, bevel=1.2)
        c.disc((px + 0.5, 27.6), 1.4, hd.BRASS, 0.9)
        c.disc((px + 0.5, 47.4), 1.4, hd.BRASS, 0.9)
    c.box((cx, 50.5), 16, 2.0, 0, COPPER, bevel=1.2)
    c.box((cx, 54.0), 12, 1.6, 0, hd.BRASS, bevel=1.0)

    if lit:
        # The cage lit from inside: warm light along every edge that faces the glass.
        for y in range(y0, y1 + 1):
            c.put(x0 - 1, y, CAGE_GLOW[1])
            c.put(x1 + 1, y, CAGE_GLOW[0])
        for x in range(x0, x1 + 1):
            c.put(x, y0 - 1, CAGE_GLOW[0])
            c.put(x, y1 + 1, CAGE_GLOW[1])
        for x in range(cx - 4, cx + 5):  # the cup's rim catches the crystal's light
            c.put(x, 44, CAGE_GLOW[1])
    return c.finish()


# ------------------------------------------------------------------------------------------------- the codex (32)

def arcane_concordance():
    """The codex: a closed book in deep violet leather, its spine to the left with raised bands, a tooled border, brass
    caps on the corners, a brass clasp across the fore-edge set with an amethyst and an eight-rayed sunburst."""
    img = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    # The back board and the page block peeking out along the right and the bottom.
    cm.rect(img, 5, 3, 29, 29, VIOLET[1])
    cm.rect(img, 6, 4, 28, 28, PAGE[1])
    for y in range(5, 28, 2):  # page edges as even lines
        cm.put(img, 27, y, PAGE[0])
        cm.put(img, 28, y, PAGE[2])
    for x in range(7, 28, 2):
        cm.put(img, x, 27, PAGE[0])
    # The front board, bevelled: lit top and left, shaded bottom and right.
    cm.bevel(img, 2, 1, 26, 26, VIOLET[3], VIOLET[1], VIOLET[2])
    # The spine: a darker strip with three raised bands and the hinge groove beside it.
    cm.rect(img, 3, 2, 5, 25, VIOLET[1])
    cm.rect(img, 2, 1, 2, 26, VIOLET[3])
    for y in (5, 13, 21):
        cm.put(img, 3, y, VIOLET[4])
        cm.put(img, 4, y, VIOLET[3])
        cm.put(img, 5, y, VIOLET[3])
        cm.put(img, 3, y + 1, VIOLET[2])
        cm.put(img, 4, y + 1, VIOLET[0])
        cm.put(img, 5, y + 1, VIOLET[0])
    for y in range(2, 26):
        cm.put(img, 6, y, VIOLET[0])
        cm.put(img, 7, y, VIOLET[3])
    # The tooled border: a fine lighter line set in from the board's edge.
    cm.inset(img, 9, 3, 24, 24, VIOLET[3], VIOLET[1])
    # Brass corner caps on the fore-edge corners, and at the head and tail of the spine.
    caps = {
        (26, 1): [(0, 0), (-1, 0), (-2, 0), (-3, 0), (0, 1), (-1, 1), (-2, 1), (0, 2), (-1, 2), (0, 3)],
        (26, 26): [(0, 0), (-1, 0), (-2, 0), (-3, 0), (0, -1), (-1, -1), (-2, -1), (0, -2), (-1, -2), (0, -3)],
        (2, 1): [(0, 0), (1, 0), (2, 0), (3, 0), (0, 1), (1, 1), (2, 1), (0, 2), (1, 2), (0, 3)],
        (2, 26): [(0, 0), (1, 0), (2, 0), (3, 0), (0, -1), (1, -1), (2, -1), (0, -2), (1, -2), (0, -3)],
    }
    for (x, y), pixels in caps.items():
        for dx, dy in pixels:
            # Lit along the top and left, shaded along the bottom and right, mid tone between.
            edge_top = dy == 0 and y < 10
            edge_left = dx == 0 and x < 10
            if edge_top or edge_left:
                colour = BRASS[4]
            elif dy == 0 or dx == 0:
                colour = BRASS[1]
            else:
                colour = BRASS[3]
            cm.put(img, x + dx, y + dy, colour)
    # The sunburst: a round boss with eight rays, four long and four short, in brass, lit from the top left.
    sx, sy = 16, 13
    for i in range(8):
        a = math.radians(i * 45)
        length = 6 if i % 2 == 0 else 4
        for k in range(2, length + 1):
            x, y = round(sx + math.cos(a) * k), round(sy + math.sin(a) * k)
            lit_side = math.cos(a) + math.sin(a) < -0.1
            colour = BRASS[4] if lit_side else (BRASS[2] if math.cos(a) + math.sin(a) > 0.1 else BRASS[3])
            if k == length:
                colour = BRASS[2] if not lit_side else BRASS[3]
            cm.put(img, x, y, colour)
    _mask(img, [
        ".bab.",
        "bcccb",
        "accca",
        "bccda",
        ".bdd.",
    ], {"a": BRASS[2], "b": BRASS[3], "c": BRASS[4], "d": BRASS[1]}, sx - 2, sy - 2)
    cm.put(img, sx - 1, sy - 1, BRASS[5])
    # The clasp: a brass strap from the back board over the fore-edge, its plate set with an amethyst.
    cm.bevel(img, 21, 11, 29, 15, BRASS[4], BRASS[1], BRASS[3])
    cm.rect(img, 27, 12, 29, 14, BRASS[2])
    cm.put(img, 29, 12, BRASS[1])
    _mask(img, [
        ".aa.",
        "acba",
        "abba",
        ".aa.",
    ], {"a": GEM[0], "b": GEM[1], "c": GEM[3]}, 22, 11)
    cm.put(img, 24, 12, GEM[2])
    return _outline(img)


# --------------------------------------------------------------------------------------------- the spell icon (32)

HAND_MASK = [
    "......t...................",
    ".....tht.............tf...",
    ".....hhh............tffj..",
    ".....hhhh..........tfffj..",
    "......hhh........ttfffj...",
    "......hhhh....tthhhhffj...",
    ".......hhhhhhhhhhhhhhj....",
    "........hhhhhhhhhhhhj.....",
    ".........hhhhhhhhhhj......",
    "..........hhhhhhhjj.......",
    "...........hhhhhhj........",
    "...........hhhhhhj........",
    "...........hhhhhhj........",
]


def kindle_icon():
    """The Kindle invocation: an open cupped hand, palm up, with a bright mote of light above it, on a round dark
    violet ground. The hand's rim catches the mote's gold light. Flat bands only, so it reads at 16 pixels."""
    img = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    # The ground: a dark rim, a violet disc, a lighter halo round the mote.
    _disc(img, 16, 16, 15.6, SPELL_BG[0])
    _disc(img, 16, 16, 14.6, SPELL_BG[1])
    _disc(img, 15.4, 15.4, 13.4, SPELL_BG[2])
    _disc(img, 16, 11, 9.0, SPELL_BG[3])
    _disc(img, 16, 11, 6.2, SPELL_BG[4])
    # The hand, palm up, its wrist going down off the ground.
    _mask(img, HAND_MASK, {"h": HAND[2], "j": HAND[1], "t": MOTE[2], "f": HAND[3]}, 3, 17)
    # Finger divisions: short dark grooves between the curled fingertips on the right.
    for x, y in ((22, 21), (23, 20), (21, 22)):
        cm.put(img, x, y, HAND[0])
    # The palm's hollow, in shadow under the light.
    for x in range(12, 21):
        cm.put(img, x, 23, HAND[1])
    # The mote: smooth bands, gold to a white core, with four short rays.
    mx, my = 16, 11
    for r, colour in ((4.6, MOTE[0]), (3.4, MOTE[1]), (2.3, MOTE[2]), (1.3, MOTE[4])):
        _disc(img, mx, my, r, colour)
    for dx, dy in ((0, -1), (0, 1), (-1, 0), (1, 0)):
        for k in (5, 6):
            cm.put(img, mx + dx * k - (1 if dx < 0 else 0), my + dy * k - (1 if dy < 0 else 0),
                   MOTE[2] if k == 5 else MOTE[1])
    # A small glint on the ground's rim, top left.
    for x, y in ((7, 6), (6, 7), (8, 5)):
        cm.put(img, x, y, SPELL_BG[3])
    return img


def composed_icon():
    """An inscribed spell: three gold nodes joined by a line (delivery, selection, operation) with a short branch off
    the last, the way a composition reads, on the same round violet ground as Kindle. Flat bands only."""
    img = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    _disc(img, 16, 16, 15.6, SPELL_BG[0])
    _disc(img, 16, 16, 14.6, SPELL_BG[1])
    _disc(img, 15.4, 15.4, 13.4, SPELL_BG[2])
    nodes = [(8, 22), (14, 15), (21, 10)]
    branch = (24, 20)
    sizes = (2.4, 2.4, 3.0, 1.8)

    def line(a, b, colour):
        steps = max(abs(b[0] - a[0]), abs(b[1] - a[1]))
        for i in range(steps + 1):
            x = round(a[0] + (b[0] - a[0]) * i / steps)
            y = round(a[1] + (b[1] - a[1]) * i / steps)
            cm.put(img, x, y, colour)
            cm.put(img, x + 1, y, colour)
    for (x, y), r in zip(nodes + [branch], sizes):
        _disc(img, x, y, r + 1.4, SPELL_BG[3])
    line(nodes[0], nodes[1], MOTE[0])
    line(nodes[1], nodes[2], MOTE[0])
    line(nodes[2], branch, MOTE[0])
    for (x, y), r in zip(nodes + [branch], sizes):
        _disc(img, x, y, r, MOTE[1])
        _disc(img, x, y, r - 1.0, MOTE[3])
    for x, y in ((7, 6), (6, 7), (8, 5)):
        cm.put(img, x, y, SPELL_BG[3])
    return img


# ------------------------------------------------------------------------------------------------- the bench

def bench_top():
    """The desk top: oak planks running across, each lit along its top edge, with a staggered butt joint and regular
    grain lines, inside a thin brass inlay that frames the top a pixel in from the edge."""
    img = Image.new("RGBA", (32, 32), OAK[2] + (255,))
    joints = (11, 23, 5, 17)
    for plank in range(4):
        y0 = 2 + plank * 7
        joint = joints[plank]
        for y in range(y0, y0 + 7):
            for x in range(2, 30):
                if y == y0:
                    colour = OAK[3]
                elif y == y0 + 6 or x == joint:
                    colour = OAK[0] if y == y0 + 6 else OAK[1]
                else:
                    colour = OAK[2]
                cm.put(img, x, y, colour)
        # Grain: two rows of even dashes, offset plank to plank.
        for row, offset in ((2, 0), (4, 5)):
            for x in range(2, 30):
                if (x + offset + plank * 3) % 10 in (0, 1, 2, 3) and x not in (joint, joint + 1):
                    cm.put(img, x, y0 + row, OAK[1])
        cm.put(img, joint + 1, y0 + 1, OAK[4])  # the joint's lit lip
    # The frame round the planks and the brass inlay line set into it.
    cm.bevel(img, 0, 0, 31, 31, OAK[4], OAK[0])
    for x in range(1, 31):
        cm.put(img, x, 1, BRASS[4])
        cm.put(img, x, 30, BRASS[2])
    for y in range(1, 31):
        cm.put(img, 1, y, BRASS[4])
        cm.put(img, 30, y, BRASS[2])
    for x, y in ((1, 1), (30, 1), (1, 30), (30, 30)):  # brass corner squares
        cm.put(img, x, y, BRASS[5])
    return img


def bench_wood():
    """Darker oak for the legs and apron: upright planks eight pixels wide, each lit down its left edge with a dark
    joint at its right, regular grain lines and one staggered butt joint."""
    img = Image.new("RGBA", (32, 32), OAK_DARK[2] + (255,))
    butts = (9, 25, 3, 17)
    for plank in range(4):
        x0 = plank * 8
        for x in range(x0, x0 + 8):
            for y in range(32):
                if x == x0:
                    colour = OAK_DARK[3]
                elif x == x0 + 7:
                    colour = OAK_DARK[0]
                else:
                    colour = OAK_DARK[2]
                cm.put(img, x, y, colour)
        # Grain: two columns of even dashes per plank, offset so neighbours never line up.
        for col, offset in ((2, 0), (5, 4)):
            for y in range(32):
                if (y + offset + plank * 5) % 9 in (0, 1, 2, 3, 4):
                    cm.put(img, x0 + col, y, OAK_DARK[1])
        butt = butts[plank]
        for x in range(x0 + 1, x0 + 7):
            cm.put(img, x, butt, OAK_DARK[0])
            cm.put(img, x, butt + 1, OAK_DARK[4] if x == x0 + 1 else OAK_DARK[3])
    return img


def bench_brass():
    """Polished brass: a soft horizontal sheen (bright near the top, deeper towards the bottom), a one-pixel bevel lit
    on the top and left, and two short bright scratches."""
    img = cm.canvas()
    bands = [4, 4, 3, 4, 5, 4, 3, 3, 3, 3, 2, 3, 2, 2, 2, 1]
    for y in range(16):
        for x in range(16):
            cm.put(img, x, y, BRASS[bands[y]])
    cm.bevel(img, 0, 0, 15, 15, BRASS[5], BRASS[1])
    cm.put(img, 15, 0, BRASS[3])
    cm.put(img, 0, 15, BRASS[2])
    cm.scuffs(img, BRASS[4], [(3, 8, 3), (9, 11, 2)])
    return img


def bench_lens():
    """The lens: a disc of violet glass in a bevelled brass rim on a darker brass mount, lit from the top left, with a
    gold glint. Opaque, like every block texture."""
    img = cm.canvas(BRASS[1])
    cm.bevel(img, 0, 0, 15, 15, BRASS[3], BRASS[0])
    for y in range(16):
        for x in range(16):
            dx, dy = x + 0.5 - 8, y + 0.5 - 8
            d = math.hypot(dx, dy)
            if d <= 6.9:
                # The rim: lit on its upper left, shaded on its lower right.
                lightness = -(dx + dy) / max(d, 0.01)
                colour = BRASS[4] if lightness > 0.35 else BRASS[2] if lightness < -0.35 else BRASS[3]
                cm.put(img, x, y, colour)
            if d <= 5.4:
                # The glass: bands of violet, lighter towards the top left, deepest at the bottom right.
                k = (dx + dy) / 2 + 0.6 * d
                colour = LENS[3] if k < -2.5 else LENS[2] if k < 0.8 else LENS[1] if k < 3.2 else LENS[0]
                cm.put(img, x, y, colour)
            if 5.4 < d <= 6.1 and dx + dy > 0:
                cm.put(img, x, y, BRASS[1])  # the rim's inner shadow
    # The glint: a soft gold catch of light upper left, with a white heart, and a faint pale ring opposite.
    cm.patch(img, [(0, 0), (1, 0), (0, 1)], GLINT_GOLD[1], 5, 5)
    cm.put(img, 5, 5, GLINT_GOLD[2])
    cm.put(img, 6, 6, GLINT_GOLD[0])
    cm.patch(img, [(0, 0), (1, -1)], LENS[4], 9, 11)
    return img


# --------------------------------------------------------------------------------------------------------- all

# ------------------------------------------------------------------------------------------- research notes (32)

def research_notes(written):
    """A sheet of notes: cream paper with faint rules and a folded top corner. Written, its lines are filled in violet
    ink, a small sketch of a rayed light sits at the head of the page, and a violet wax seal closes the foot."""
    img = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    cm.rect(img, 6, 3, 25, 28, PAGE[2])
    cm.rect(img, 25, 4, 25, 28, PAGE[1])  # shaded right edge and foot
    cm.rect(img, 7, 28, 25, 28, PAGE[1])
    # The folded corner: cut away the top right and lay the fold over it.
    for i in range(5):
        for j in range(5 - i):
            img.putpixel((25 - j, 3 + i), (0, 0, 0, 0))
    for i in range(5):
        for j in range(i + 1):
            cm.put(img, 21 + j, 3 + i, PAGE[1] if j < i else PAGE[0])
    # Faint rules.
    for y in range(9, 27, 3):
        for x in range(9, 23):
            cm.put(img, x, y, PAGE[1])
    if written:
        # Lines of writing: strokes of fixed lengths with word gaps, so every sheet looks the same.
        lengths = [(9, 13), (15, 22), (9, 11), (13, 20), (9, 16), (18, 22)]
        for row, (x0, x1) in enumerate(lengths):
            y = 11 + row * 3 - 1
            if y > 25:
                break
            for x in range(x0, x1 + 1):
                if (x - x0) % 5 != 4:
                    cm.put(img, x, y, VIOLET[2])
        # A sketch of a rayed light at the head of the page.
        for dx, dy in ((0, 0), (-2, 0), (2, 0), (0, -2), (0, 2), (-1, -1), (1, 1), (1, -1), (-1, 1)):
            cm.put(img, 11 + dx, 6 + dy, VIOLET[3] if (dx, dy) == (0, 0) else VIOLET[2])
        # The seal.
        _disc(img, 21.5, 25.5, 2.6, GEM[1])
        cm.put(img, 21, 24, GEM[2])
        cm.put(img, 20, 25, GEM[2])
    return _outline(img)


ITEMS = {
    "research_notes": lambda: research_notes(False),
    "research_notes_written": lambda: research_notes(True),
    "initiate_wand": initiate_wand,
    "kindled_lantern": lambda: kindled_lantern(False),
    "kindled_lantern_lit": lambda: kindled_lantern(True),
    "arcane_concordance": arcane_concordance,
}
BLOCKS = {
    "lampwright_bench_top": bench_top,
    "lampwright_bench_wood": bench_wood,
    "lampwright_bench_brass": bench_brass,
    "lampwright_bench_lens": bench_lens,
}
SPELLS = {"kindle": kindle_icon, "composed": composed_icon}


def textures():
    """Every texture as {(kind, name): image}, in a fixed order."""
    out = {}
    for kind, table in (("item", ITEMS), ("block", BLOCKS), ("spell", SPELLS)):
        for name, draw in table.items():
            out[(kind, name)] = draw()
    return out


def draw_all(save):
    for (kind, name), image in textures().items():
        save(image, kind, name)

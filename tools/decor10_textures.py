"""Original textures for the tenth batch of Halloween decorations, lighting and glow (requires Pillow): the Black
Light's fixture and tube, Glow Paint's six designs (faint, and glowing under a black light) and its jar, the Witch Fire
Brazier's iron, coals and flame, the Shadow Puppet Lamp's wood, brass, candle, paper panels and the shadows they throw,
the Mini Pumpkin Stack's rind, faces and stems, and the Floating Witch Hat's felt, band, candle and flame.

Called from crop_textures.crop_textures(). Every pixel is drawn here by code or from the small pixel-art grids below
(some shared with tools/decor9_textures.py), from fixed seeds; no Mojang texture is read, traced or recoloured. Block
textures are 16x16; the glow paint designs are faint and see-through round their shapes, the lamp's paper panels are
opaque; the glowing designs, flame and shadows (entity textures) are see-through round their shapes.

Surfaces are painted in the manner of the vanilla blocks with tools/block_style.py: a short palette in small clumps,
never a random colour at every pixel; wood as planks, and straw, bark and hair as streaks.
"""
import math

from crop_textures import Canvas, rgb
import block_style as bs
from decor9_textures import BAT, WITCH, IRON, put, grid

GLOW = (190, 255, 170)
FAINT_ALPHA = 70

SKULL_SHAPE = [
    "................",
    ".....XXXXXX.....",
    "....XXXXXXXX....",
    "...XXXXXXXXXX...",
    "...XX..XX..XX...",
    "...X....X...X...",
    "...XX..XXX.XX...",
    "...XXXXX.XXXX...",
    "....XXX...XX....",
    ".....XXXXXX.....",
    ".....X.X.X.X....",
    ".....XXXXXX.....",
    "................",
    "................",
    "................",
    "................"]
SPIDER_SHAPE = [
    "................",
    ".X............X.",
    "..X....XX....X..",
    "...X..XXXX..X...",
    "X...XXXXXXXX...X",
    ".XXX.XXXXXX.XXX.",
    ".....XXXXXX.....",
    "..XXXXXXXXXXXX..",
    ".X...XXXXXX...X.",
    "X...X.XXXX.X...X",
    "...X...XX...X...",
    "..X..........X..",
    ".X............X.",
    "................",
    "................",
    "................"]
HAND_SHAPE = [
    "................",
    "....X..X..X.....",
    "....X..X..X.....",
    "....XX.XX.X..X..",
    "....XX.XX.XX.X..",
    "....XXXXXXXX.X..",
    ".X..XXXXXXXXXX..",
    ".XX.XXXXXXXXXX..",
    "..XXXXXXXXXXXX..",
    "...XXXXXXXXXX...",
    "....XXXXXXXXX...",
    ".....XXXXXXX....",
    ".....XXXXXX.....",
    ".....XXXXXX.....",
    "................",
    "................"]
EYE_SHAPE = [
    "................",
    "................",
    "................",
    "......XXXX......",
    "...XXX....XXX...",
    "..X....XX....X..",
    ".X....XXXX....X.",
    "X....XXXXXX....X",
    ".X....XXXX....X.",
    "..X....XX....X..",
    "...XXX....XXX...",
    "......XXXX......",
    "................",
    "................",
    "................",
    "................"]
CAT_SHAPE = [
    "................",
    "................",
    "...X...X........",
    "...XX.XX........",
    "...XXXXX........",
    "...XXXXX........",
    "....XXX.........",
    "....XXXX......X.",
    "...XXXXXX....X..",
    "...XXXXXXX...X..",
    "..XXXXXXXXX..X..",
    "..XXXXXXXXXXX...",
    "..XXXXXXXXXX....",
    "...X.X..X.X.....",
    "................",
    "................"]


def web_shape():
    """A spider's web: threads out from the middle and rings across them, as a grid of X."""
    cells = [["." for _ in range(16)] for _ in range(16)]
    for angle in range(0, 360, 45):
        for r in range(8):
            x = round(7.5 + r * math.cos(math.radians(angle)))
            y = round(7.5 + r * math.sin(math.radians(angle)))
            if 0 <= x < 16 and 0 <= y < 16:
                cells[y][x] = "X"
    for radius in (2.5, 4.5, 6.5):
        for t in range(0, 360, 8):
            x = round(7.5 + radius * math.cos(math.radians(t)))
            y = round(7.5 + radius * math.sin(math.radians(t)))
            if 0 <= x < 16 and 0 <= y < 16:
                cells[y][x] = "X"
    return ["".join(row) for row in cells]


DESIGNS = {"skull": SKULL_SHAPE, "bat": BAT, "spider": SPIDER_SHAPE, "web": web_shape(), "hand": HAND_SHAPE, "eye": EYE_SHAPE}


def shape(rows, color, alpha=255):
    """A see-through texture with the shape's X in one colour."""
    c = Canvas()
    for y, row in enumerate(rows):
        for x, ch in enumerate(row):
            if ch == "X":
                put(c.img, x, y, color, alpha)
    return c.img


# ---------------------------------------------------------------- the black light

def fixture():
    c = Canvas()
    bs.fill(c, 0, 0, 15, 15, [rgb("121214"), rgb("18181c"), rgb("202026")], 20101, [2, 3, 1])
    return c.img


def tube(lit):
    """A fluorescent tube: violet, white-hot down the middle while it is on."""
    c = Canvas()
    for y in range(16):
        for x in range(16):
            d = abs(y - 7.5) / 7.5
            if lit:
                c.px(x, y, (235, 200, 255) if d < 0.3 else (170, 90, 255) if d < 0.7 else (110, 40, 200))
            else:
                c.px(x, y, (120, 110, 130) if d < 0.5 else (90, 80, 100))
    return c.img


def paint_jar():
    """The Glow Paint item: a little glass jar of glowing green paint with a cork."""
    c = Canvas()
    for y in range(5, 15):
        for x in range(4, 12):
            edge = x in (4, 11) or y == 14
            c.px(x, y, (150, 180, 170) if edge else GLOW if y > 7 else (210, 235, 225))
    c.rect(5, 3, 10, 4, rgb("8a6a44"))
    c.rect(6, 2, 9, 2, rgb("a07a50"))
    c.px(6, 9, (255, 255, 255))
    return c.img


# ---------------------------------------------------------------- the witch fire brazier

def brazier_iron():
    c = Canvas()
    bs.fill(c, 0, 0, 15, 15, IRON[:3], 20201, [2, 3, 1])
    for x in range(0, 16, 5):
        c.px(x, 2, IRON[3])
    return c.img


def coals(lit):
    """Coals heaped like vanilla gravel: rounded lumps lit at their upper left; glowing, or burnt out."""
    c = Canvas()
    if lit:
        lumps = [[rgb("c03a10"), rgb("ff7a1a"), rgb("ffb040")], [rgb("8a2a0c"), rgb("c03a10"), rgb("ff7a1a")]]
        bs.heap(lumps, 20211, count=12, joint=rgb("2a1a14"), weights=[3, 2])(c)
    else:
        lumps = [[rgb("1a1614"), rgb("26201c"), rgb("3a302a")], [rgb("121010"), rgb("1a1614"), rgb("26201c")]]
        bs.heap(lumps, 20211, count=12, joint=rgb("0a0808"), weights=[3, 2])(c)
    return c.img


def flame():
    """A white-hot flame, see-through round its edges (tinted by the client: orange, green, purple or blue)."""
    c = Canvas()
    for y in range(16):
        for x in range(16):
            height = 1 - y / 16
            width = 7 * (1 - height) ** 0.6 * (1 - 0.5 * height)
            d = abs(x - 7.5)
            if d < width:
                core = d < width * 0.45 and y > 6
                put(c.img, x, y, (255, 255, 235) if core else (255, 230, 190), 255 if core else 200)
    return c.img


# ---------------------------------------------------------------- the shadow puppet lamp

def lamp_wood():
    c = Canvas()
    for y in range(16):
        for x in range(16):
            c.px(x, y, [rgb("4a2e18"), rgb("5a3a20"), rgb("6a4628")][(x + y // 4) % 3])
    return c.img


def brass():
    c = Canvas()
    bs.fill(c, 0, 0, 15, 15, [rgb("8a6a20"), rgb("b08a30"), rgb("d0aa48")], 20311, [2, 3, 1])
    return c.img


def candle():
    c = Canvas()
    bs.fill(c, 0, 0, 15, 15, [rgb("e8e0c8"), rgb("f0eadc"), rgb("dcd2b8")], 20321, [3, 2, 1])
    return c.img


def paper(design):
    """A panel of warm, lamp-lit paper with a silhouette in it."""
    c = Canvas()
    rows = {"bat": BAT, "cat": CAT_SHAPE, "witch": WITCH}[design]
    paper = bs.surface([rgb("f0d8a0"), rgb("f4e0b0"), rgb("e8cc90")], 20331, spread=0.6)
    for y in range(16):
        for x in range(16):
            ch = rows[y][x]
            c.px(x, y, rgb("1a1210") if ch == "X" else paper(x, y))
    for y in range(16):
        c.px(0, y, rgb("5a3a20"))
        c.px(15, y, rgb("5a3a20"))
    return c.img


# ---------------------------------------------------------------- the mini pumpkin stack

PUMPKIN = [rgb("b85a10"), rgb("d06c18"), rgb("e88424"), rgb("f8a040")]

MINI_FACE = [
    "................",
    "................",
    "................",
    "...YY......YY...",
    "..YYYY....YYYY..",
    "................",
    ".......YY.......",
    "................",
    "..Y.YYYYYYYY.Y..",
    "..YYYYYYYYYYYY..",
    "...YY.YYYY.YY...",
    "................",
    "................",
    "................",
    "................",
    "................"]


def pumpkin_side():
    c = Canvas()
    for y in range(16):
        for x in range(16):
            c.px(x, y, PUMPKIN[[0, 1, 2, 3, 2, 1][x % 6]])
    return c.img


def pumpkin_top():
    c = Canvas()
    for y in range(16):
        for x in range(16):
            d = ((x - 7.5) ** 2 + (y - 7.5) ** 2) ** 0.5
            c.px(x, y, PUMPKIN[max(0, 3 - int(d / 2.5))])
    return c.img


def pumpkin_face(lit):
    glow = rgb("ffd84a") if lit else rgb("3a2208")
    return grid(MINI_FACE, {"Y": glow}, [PUMPKIN[1], PUMPKIN[2]], 20411 if lit else 20412)


def stem():
    c = Canvas()
    bs.fill(c, 0, 0, 15, 15, [rgb("3a5a1a"), rgb("4a6a22"), rgb("2e4814")], 20421)
    return c.img


# ---------------------------------------------------------------- the floating witch hat

def felt():
    c = Canvas()
    bs.fill(c, 0, 0, 15, 15, [rgb("16101c"), rgb("1e1626"), rgb("281e32")], 20501, [2, 3, 1])
    return c.img


def band():
    c = Canvas()
    bs.fill(c, 0, 0, 15, 15, [rgb("16101c"), rgb("1e1626")], 20511)
    c.rect(0, 9, 15, 13, rgb("e07818"))
    c.rect(6, 8, 9, 14, rgb("d8b048"))
    c.rect(7, 10, 8, 12, rgb("1e1626"))
    return c.img


def hat_flame():
    c = Canvas()
    for y in range(16):
        for x in range(16):
            c.px(x, y, (255, 240, 160) if 4 <= x <= 11 and y >= 4 else (255, 170, 60))
    return c.img


def decor10_textures():
    """(kind, name) -> image for every texture of the tenth decorations batch."""
    out = {
        ("block", "black_light_fixture"): fixture(),
        ("block", "black_light_tube"): tube(True),
        ("block", "black_light_tube_off"): tube(False),
        ("item", "glow_paint"): paint_jar(),
        ("block", "witch_fire_brazier_iron"): brazier_iron(),
        ("block", "witch_fire_brazier_coals"): coals(True),
        ("block", "witch_fire_brazier_coals_dark"): coals(False),
        ("entity", "witch_fire_flame"): flame(),
        ("block", "shadow_puppet_lamp_wood"): lamp_wood(),
        ("block", "shadow_puppet_lamp_brass"): brass(),
        ("block", "shadow_puppet_lamp_candle"): candle(),
        ("block", "mini_pumpkin_side"): pumpkin_side(),
        ("block", "mini_pumpkin_top"): pumpkin_top(),
        ("block", "mini_pumpkin_face"): pumpkin_face(True),
        ("block", "mini_pumpkin_face_off"): pumpkin_face(False),
        ("block", "mini_pumpkin_stem"): stem(),
        ("block", "floating_witch_hat_felt"): felt(),
        ("block", "floating_witch_hat_band"): band(),
        ("block", "floating_witch_hat_candle"): candle(),
        ("block", "floating_witch_hat_flame"): hat_flame(),
    }
    for design, rows in DESIGNS.items():
        out[("block", f"glow_paint_{design}")] = shape(rows, GLOW, FAINT_ALPHA)
        out[("entity", f"glow_paint_{design}_glow")] = shape(rows, GLOW)
    for design in ("bat", "cat", "witch"):
        out[("block", f"shadow_puppet_lamp_paper_{design}")] = paper(design)
        out[("entity", f"shadow_puppet_lamp_{design}_shadow")] = shape({"bat": BAT, "cat": CAT_SHAPE, "witch": WITCH}[design], (0, 0, 0))
    return out

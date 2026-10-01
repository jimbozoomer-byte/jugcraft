"""Original textures for the first batch of Halloween decorations (requires Pillow).

Called from crop_textures.crop_textures(). Every pixel is drawn here by code from fixed seeds; no Mojang texture is
read, traced or recoloured. Block and item textures are 16x16. The Haunted Portrait's eye whites sit exactly where
HAUNTED_PORTRAIT in tools/agriculture.py says, because the client draws the moving pupils there.
"""
import math
import random

from PIL import Image

from agriculture import HAUNTED_PORTRAIT
from crop_textures import Canvas, rgb, outline
from halloween_textures import PLANK, IRON, shade, wood_grain

TERRACOTTA = [rgb("8a3f12"), rgb("a24c18"), rgb("b85a1e"), rgb("c86a28")]
FACE = rgb("2a1206")
CANDY = [rgb("f4f0e0"), rgb("f7b21e"), rgb("e8601a"), rgb("8a3cc0"), rgb("4cb84a"), rgb("d8344a"), rgb("2a2a2a")]
COFFIN_WOOD = [rgb("2a1810"), rgb("3a2216"), rgb("4a2e1e"), rgb("5a3a26")]
VELVET = [rgb("5a0c14"), rgb("74121c"), rgb("8e1a26"), rgb("a8283a")]
BRASS = [rgb("6a4a14"), rgb("94701e"), rgb("bc9632"), rgb("e0c060")]
GOLD = [rgb("7a5a10"), rgb("a8801c"), rgb("d0aa36"), rgb("f0d470")]
CANVAS_DARK = [rgb("1a1612"), rgb("241e18"), rgb("2e261e")]
EYE_WHITE = rgb("e8e0c8")
OLIVE = [rgb("2c3020"), rgb("3a3f2a"), rgb("4a5036"), rgb("5c6444")]
STEEL = [rgb("2a2c30"), rgb("3c3f44"), rgb("52565c"), rgb("6c7178")]
BULB_OFF = [rgb("7a3a0c"), rgb("9a4e14"), rgb("b4621e")]
BULB_ON = [rgb("ffb21e"), rgb("ffd23c"), rgb("fff0a0")]


def noise(c, x0, y0, x1, y1, palette, seed, weights=None):
    rng = random.Random(seed)
    picks = [i for i, n in enumerate(weights or [1] * len(palette)) for _ in range(n)]
    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1):
            c.px(x, y, palette[rng.choice(picks)])


# ---------------------------------------------------------------- string lights

def hook_iron():
    c = Canvas()
    noise(c, 0, 0, 15, 15, IRON[:3], 8101, [2, 3, 1])
    return c.img


def bulb(lit):
    """A tiny pumpkin: ribs, a stalk on top and, lit, a glowing grin."""
    c = Canvas()
    palette = BULB_ON if lit else BULB_OFF
    for y in range(16):
        for x in range(16):
            c.px(x, y, palette[0] if x % 5 == 0 else palette[1 + (y // 5 + x) % 2])
    face = rgb("fff6c8") if lit else rgb("3a1a04")
    for x, y in ((5, 6), (6, 6), (9, 6), (10, 6), (5, 10), (6, 11), (7, 11), (8, 11), (9, 11), (10, 10)):
        c.px(x, y, face)
    return c.img


def string_lights_entity():
    """The strand's texture: the left half is the wire, the right half a bulb (the renderer picks the halves)."""
    c = Canvas()
    noise(c, 0, 0, 7, 15, [rgb("16200e"), rgb("1e2a14"), rgb("26341a")], 8111)
    for y in range(16):
        for x in range(8, 16):
            c.px(x, y, BULB_ON[0] if x % 3 == 0 else BULB_ON[1 + (x + y) % 2])
    for x, y in ((10, 6), (13, 6), (10, 10), (11, 11), (12, 11), (13, 10)):
        c.px(x, y, rgb("fff6c8"))
    return c.img


def string_lights_item():
    c = Canvas()
    points = [(1 + i, 4 + round(6 * math.sin(i / 14 * math.pi))) for i in range(15)]
    for x, y in points:
        c.px(x, y, rgb("1e2a14"))
    for x, y in points[1::4]:
        c.rect(x - 1, y + 1, x + 1, y + 3, BULB_ON[0])
        c.px(x, y + 2, rgb("fff6c8"))
        c.px(x, y, rgb("2e5a1c"))
    outline(c, rgb("141008"))
    return c.img


def hook_item():
    c = Canvas()
    for y in range(9, 15):
        c.px(7, y, IRON[2])
        c.px(8, y, IRON[1])
    c.rect(5, 14, 10, 15, IRON[1])
    c.rect(5, 4, 10, 9, BULB_ON[1])
    for x in (5, 8):
        for y in range(4, 10):
            c.px(x, y, BULB_ON[0])
    c.px(7, 3, rgb("2e5a1c"))
    outline(c, rgb("141008"))
    return c.img


# ---------------------------------------------------------------- the candy bowl

def bowl():
    c = Canvas()
    noise(c, 0, 0, 15, 15, TERRACOTTA, 8201, [1, 3, 3, 1])
    for x in range(16):
        c.px(x, 0, TERRACOTTA[3])
    return c.img


def bowl_face():
    """The front of the bowl: a jack-o'-lantern grin glazed in near-black, in the part the front shows (v 11-15)."""
    c = Canvas()
    noise(c, 0, 0, 15, 15, TERRACOTTA, 8202, [1, 3, 3, 1])
    for x, y in ((5, 11), (6, 11), (9, 11), (10, 11), (6, 12), (9, 12), (4, 13), (11, 13), (5, 14), (6, 14), (7, 14), (8, 14), (9, 14),
                 (10, 14)):
        c.px(x, y, FACE)
    return c.img


def candy():
    """A heap of wrapped sweets and candy corn."""
    c = Canvas()
    rng = random.Random(8203)
    noise(c, 0, 0, 15, 15, [rgb("6a3a14"), rgb("7a4a1c")], 8204)
    for _ in range(26):
        x, y = rng.randrange(16), rng.randrange(16)
        color = CANDY[rng.randrange(len(CANDY))]
        c.px(x, y, color)
        c.px(x + 1, y, shade(color, 0.8))
        if rng.random() < 0.4:
            c.px(x, y + 1, CANDY[1])
    return c.img


def bowl_item():
    c = Canvas()
    for y in range(7, 14):
        half = 7 - (y - 7) // 2
        for x in range(8 - half, 8 + half):
            c.px(x, y, TERRACOTTA[1 + (x + y) % 2])
    for x, y in ((5, 9), (6, 9), (9, 9), (10, 9), (5, 11), (6, 12), (7, 12), (8, 12), (9, 12), (10, 11)):
        c.px(x, y, FACE)
    rng = random.Random(8205)
    for x in range(2, 14):
        for y in range(4, 7):
            if abs(x - 7.5) + (6 - y) * 2 < 7:
                c.px(x, y, CANDY[rng.randrange(1, 6)])
    outline(c, rgb("2a1206"))
    return c.img


# ---------------------------------------------------------------- the coffin

def coffin_wood():
    c = Canvas()
    wood_grain(c, COFFIN_WOOD, 8301, vertical=False)
    return c.img


def coffin_lid():
    """The lid: dark planks along the length with a brass cross on the head half (u 6-9, v 2-12)."""
    c = Canvas()
    wood_grain(c, COFFIN_WOOD, 8302, vertical=True)
    for y in range(2, 13):
        c.px(7, y, BRASS[2])
        c.px(8, y, BRASS[1])
    for x in range(5, 11):
        c.px(x, 5, BRASS[2])
        c.px(x, 6, BRASS[1])
    return c.img


def velvet():
    c = Canvas()
    noise(c, 0, 0, 15, 15, VELVET, 8303, [1, 3, 3, 1])
    for y in range(0, 16, 4):
        for x in range(16):
            c.px(x, y, VELVET[0])
    return c.img


def brass():
    c = Canvas()
    noise(c, 0, 0, 15, 15, BRASS, 8304, [1, 2, 3, 1])
    return c.img


def coffin_item():
    c = Canvas()
    shape = [(6, 9), (5, 10), (4, 11), (4, 11), (4, 11), (5, 10), (5, 10), (5, 10), (5, 10), (5, 10), (5, 10), (6, 9), (6, 9)]
    for i, (x0, x1) in enumerate(shape):
        for x in range(x0, x1 + 1):
            c.px(x, 1 + i, COFFIN_WOOD[1 + (x + i) % 2])
    for y in range(3, 10):
        c.px(7, y, BRASS[2])
    for x in range(6, 10):
        c.px(x, 5, BRASS[2])
    outline(c, rgb("0e0806"))
    return c.img


# ---------------------------------------------------------------- the haunted portrait

def frame():
    c = Canvas()
    noise(c, 0, 0, 15, 15, GOLD, 8401, [1, 2, 3, 1])
    return c.img


SITTERS = {
    # (background, figure palette, skin, hair) per portrait
    "lady": (CANVAS_DARK, [rgb("1c1028"), rgb("2a1838"), rgb("3a2448")], [rgb("c8a890"), rgb("dcc0a8")], [rgb("2a1810"), rgb("3a2416")]),
    "captain": (CANVAS_DARK, [rgb("101a2c"), rgb("18243a"), rgb("223048")], [rgb("b89478"), rgb("ccaa8c")], [rgb("d0d0cc"), rgb("e8e8e4")]),
    "cat": ([rgb("2a1a10"), rgb("34221a")], [rgb("0e0e10"), rgb("18181c"), rgb("222226")], [rgb("18181c"), rgb("222226")], []),
    "owl": ([rgb("101a14"), rgb("16221a")], [rgb("4a3420"), rgb("5e4430"), rgb("72563e")], [rgb("8a6a48"), rgb("a08060")], []),
}


def portrait(name):
    """A gilt frame (the outer pixel) round a dark canvas with the sitter; the eye whites are left for the renderer's pupils."""
    background, figure, skin, hair = SITTERS[name]
    c = Canvas()
    noise(c, 0, 0, 15, 15, GOLD, 8410, [1, 2, 3, 1])
    noise(c, 1, 1, 14, 14, background, 8411)
    rng = random.Random(8412 + len(name))
    if name in ("lady", "captain"):
        # Shoulders and a dark coat or gown below, an oval face above.
        for y in range(10, 15):
            for x in range(3, 13):
                if abs(x - 7.5) < 3 + (y - 10):
                    c.px(x, y, figure[rng.randrange(len(figure))])
        for y in range(3, 11):
            for x in range(4, 12):
                if ((x - 7.5) / 3.6) ** 2 + ((y - 6.8) / 3.9) ** 2 <= 1:
                    c.px(x, y, skin[rng.randrange(len(skin))])
        for x in range(4, 12):
            c.px(x, 3, hair[0])
            c.px(x, 4 if name == "lady" else 3, hair[1])
        if name == "lady":
            for y in range(4, 10):
                c.px(4, y, hair[0])
                c.px(11, y, hair[0])
            c.px(7, 11, rgb("d8d0b0"))
            c.px(8, 11, rgb("d8d0b0"))
        else:
            c.rect(6, 9, 9, 9, hair[1])
            c.px(4, 11, GOLD[3])
            c.px(11, 11, GOLD[3])
        c.px(7, 9, rgb("6a2a2a"))
        c.px(8, 9, rgb("6a2a2a"))
    elif name == "cat":
        for y in range(4, 15):
            for x in range(3, 13):
                if ((x - 7.5) / 4.6) ** 2 + ((y - 8.5) / 4.2) ** 2 <= 1 or y >= 12:
                    c.px(x, y, figure[rng.randrange(len(figure))])
        for x, y in ((4, 3), (4, 4), (5, 4), (11, 3), (11, 4), (10, 4)):
            c.px(x, y, figure[1])
        c.px(7, 10, rgb("b05a6a"))
        c.px(8, 10, rgb("b05a6a"))
    else:
        for y in range(3, 15):
            for x in range(3, 13):
                if ((x - 7.5) / 4.8) ** 2 + ((y - 8.5) / 5.6) ** 2 <= 1:
                    c.px(x, y, figure[rng.randrange(len(figure))])
        for y in range(4, 9):
            for x in range(3, 13):
                if ((x - 7.5) / 4.6) ** 2 + ((y - 6.5) / 2.6) ** 2 <= 1:
                    c.px(x, y, skin[rng.randrange(len(skin))])
        c.px(7, 8, rgb("d0a030"))
        c.px(8, 8, rgb("d0a030"))
        c.px(4, 3, figure[2])
        c.px(11, 3, figure[2])
    whites = rgb("d8e060") if name == "cat" else rgb("f0b030") if name == "owl" else EYE_WHITE
    for x, y, w, h in HAUNTED_PORTRAIT["portraits"][name]:
        c.rect(x, y, x + w - 1, y + h - 1, whites)
    return c.img


def portrait_pupil():
    c = Canvas()
    c.rect(0, 0, 15, 15, rgb("ffffff"))
    return c.img


def portrait_item():
    img = portrait("lady").copy()
    for x, y, w, h in HAUNTED_PORTRAIT["portraits"]["lady"]:
        img.putpixel((x + w // 2, y), (16, 12, 10, 255))
    return img


# ---------------------------------------------------------------- the fog machine

def riveted(palette, seed, rivets=True):
    """Dieselpunk plate: steel or olive noise, a darker seam round the edge and rivets in the corners."""
    c = Canvas()
    noise(c, 0, 0, 15, 15, palette, seed, [1, 3, 3, 1])
    for i in range(16):
        c.px(i, 0, palette[0])
        c.px(i, 15, palette[0])
        c.px(0, i, palette[0])
        c.px(15, i, palette[0])
    if rivets:
        for x, y in ((2, 2), (13, 2), (2, 13), (13, 13), (7, 2), (7, 13)):
            c.px(x, y, shade(palette[3], 1.3))
            c.px(x + 1, y + 1, palette[0])
    return c.img


def fog_body():
    img = riveted(OLIVE, 8501)
    c = Canvas()
    c.img = img
    # A stencilled hazard band low on the panels.
    for x in range(1, 15):
        c.px(x, 11, rgb("c8a020") if (x // 2) % 2 else rgb("1c1c18"))
    return c.img


def fog_tank():
    c = Canvas()
    noise(c, 0, 0, 15, 15, BRASS, 8502, [1, 2, 3, 1])
    for y in (3, 12):
        for x in range(16):
            c.px(x, y, BRASS[0])
            c.px(x, y + 1, shade(BRASS[3], 1.05))
    return c.img


def fog_grille():
    c = Canvas()
    noise(c, 0, 0, 15, 15, STEEL, 8503)
    for y in range(1, 15, 2):
        for x in range(1, 15):
            c.px(x, y, rgb("101214"))
    return c.img


def fog_gauge():
    """A round dial on a steel plate (u 4-11, v 4-11): white face, red sector, a needle pointing up-right."""
    c = Canvas()
    noise(c, 0, 0, 15, 15, STEEL, 8504)
    for y in range(16):
        for x in range(16):
            d = math.hypot(x - 7.5, y - 7.5)
            if d < 3.6:
                c.px(x, y, rgb("e8e4d8"))
            elif d < 4.4:
                c.px(x, y, BRASS[2])
    c.px(10, 6, rgb("c02020"))
    c.px(10, 7, rgb("c02020"))
    for t in range(4):
        c.px(8 + t * 0.6, 8 - t * 0.6, rgb("1a1a1a"))
    return c.img


def fog_lamp(lit):
    c = Canvas()
    palette = [rgb("ffb020"), rgb("ffd040"), rgb("fff0a0")] if lit else [rgb("4a2a0a"), rgb("5c360e"), rgb("6c4214")]
    noise(c, 0, 0, 15, 15, palette, 8505)
    return c.img


def fog_steel():
    return riveted(STEEL, 8506, rivets=False)


def fog_item():
    c = Canvas()
    c.rect(3, 6, 13, 14, OLIVE[2])
    c.rect(3, 13, 13, 14, OLIVE[0])
    for x in range(4, 13):
        c.px(x, 12, rgb("c8a020") if (x // 2) % 2 else rgb("1c1c18"))
    c.rect(5, 2, 11, 5, BRASS[2])
    c.px(5, 3, BRASS[0])
    c.px(11, 3, BRASS[0])
    c.rect(1, 8, 3, 10, STEEL[2])
    c.px(12, 1, rgb("ffd040"))
    outline(c, rgb("101010"))
    return c.img


def fog_particle():
    """A soft round puff: white, fading out to the edge."""
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for y in range(16):
        for x in range(16):
            d = math.hypot(x - 7.5, y - 7.5) / 7.5
            if d < 1:
                img.putpixel((x, y), (235, 240, 245, int(170 * (1 - d) ** 1.5)))
    return img


def decor_textures():
    """(kind, name) -> image for every texture of the first decorations batch."""
    out = {
        ("block", "string_light_hook_iron"): hook_iron(),
        ("block", "string_light_bulb"): bulb(False),
        ("block", "string_light_bulb_lit"): bulb(True),
        ("entity", "string_lights"): string_lights_entity(),
        ("item", "jack_o_lantern_string_lights"): string_lights_item(),
        ("item", "string_light_hook"): hook_item(),
        ("block", "candy_bowl"): bowl(),
        ("block", "candy_bowl_face"): bowl_face(),
        ("block", "candy_bowl_candy"): candy(),
        ("item", "candy_bowl"): bowl_item(),
        ("block", "coffin_wood"): coffin_wood(),
        ("block", "coffin_lid"): coffin_lid(),
        ("block", "coffin_velvet"): velvet(),
        ("block", "coffin_brass"): brass(),
        ("item", "coffin"): coffin_item(),
        ("block", "haunted_portrait_frame"): frame(),
        ("entity", "portrait_pupil"): portrait_pupil(),
        ("item", "haunted_portrait"): portrait_item(),
        ("block", "fog_machine_body"): fog_body(),
        ("block", "fog_machine_tank"): fog_tank(),
        ("block", "fog_machine_grille"): fog_grille(),
        ("block", "fog_machine_gauge"): fog_gauge(),
        ("block", "fog_machine_lamp"): fog_lamp(False),
        ("block", "fog_machine_lamp_lit"): fog_lamp(True),
        ("block", "fog_machine_steel"): fog_steel(),
        ("item", "fog_machine"): fog_item(),
        ("particle", "fog"): fog_particle(),
    }
    for name in HAUNTED_PORTRAIT["portraits"]:
        out[("block", f"haunted_portrait_{name}")] = portrait(name)
    return out


if __name__ == "__main__":
    from pathlib import Path
    root = Path(__file__).resolve().parents[1] / "src" / "main" / "resources" / "assets" / "jugcraft" / "textures"
    for (kind, name), image in decor_textures().items():
        path = root / kind / f"{name}.png"
        path.parent.mkdir(parents=True, exist_ok=True)
        image.save(path, optimize=True)

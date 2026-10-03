"""Original textures for pie baking (fall additions 16) (requires Pillow): the Hearth Oven's red bricks with pale mortar,
its sooty inside, glowing embers and stone hearth; the pies' lattice tops (golden pastry strips over each filling), the
fillings where a pie is cut, the fluted crust edge and the tin; the near-white crust the oven's renderer tints as a pie
bakes; and the raw pies, slices and pastry dough as items.

Called from crop_textures.crop_textures(). Every pixel is drawn here by code, from a fixed seed; no Mojang texture is
read, traced or recoloured.
"""
import random

from PIL import Image

from agriculture import PIES
from crop_textures import Canvas, rgb, outline
from halloween_textures import shade

BRICK = [rgb("8a3a26"), rgb("9c4430"), rgb("ae5038"), rgb("7a3220")]
MORTAR = rgb("b8ad9c")
SOOT = [rgb("1a1614"), rgb("241e1a"), rgb("2e2620"), rgb("3a2e26")]
EMBER = [rgb("c83a0e"), rgb("e8601a"), rgb("f8902a"), rgb("ffc04a")]
STONE = [rgb("6e6c68"), rgb("7e7c78"), rgb("8e8c86"), rgb("9c9a94")]
CRUST = [rgb("c8862e"), rgb("d89a40"), rgb("e8b058"), rgb("f0c470")]
RAW = [rgb("e6d0a0"), rgb("eedcb0"), rgb("f4e6c0"), rgb("faeed0")]
TIN = [rgb("8a8c90"), rgb("a0a2a6"), rgb("b4b6ba")]
BURNT = [rgb("1c120c"), rgb("2a1a10"), rgb("3a2616"), rgb("4a3020")]


def noise(palette, seed, weights=None):
    img = Image.new("RGBA", (16, 16))
    rng = random.Random(seed)
    for x in range(16):
        for y in range(16):
            img.putpixel((x, y), rng.choices(palette, weights or [1] * len(palette))[0] + (255,))
    return img


def bricks():
    """Red bricks in courses of four pixels, staggered, with pale mortar."""
    img = noise(BRICK, 16101, [3, 4, 2, 1])
    for y in range(16):
        for x in range(16):
            offset = 4 if (y // 4) % 2 else 0
            if y % 4 == 3 or (x + offset) % 8 == 7:
                img.putpixel((x, y), MORTAR + (255,))
    return img


def filling_palette(color):
    base = ((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF)
    return [shade(base, 0.75), shade(base, 0.9), base, shade(base, 1.12)]


def lattice_top(filling, crust=CRUST, seed=16200):
    """A pie's top: the filling, with a lattice of pastry strips over it (two pixels wide, every five) and a crust rim."""
    img = noise(filling, seed, [1, 3, 3, 1])
    rng = random.Random(seed + 1)
    for y in range(16):
        for x in range(16):
            strip = x % 5 in (1, 2) or y % 5 in (1, 2)
            rim = x in (0, 15) or y in (0, 15)
            if strip or rim:
                tone = crust[rng.choice((1, 2, 2, 3))] if not rim else crust[1 + (x + y) % 2]
                img.putpixel((x, y), tone + (255,))
    return img


def inside(filling, seed):
    """The filling where a pie is cut, under a line of top crust and over the bottom crust."""
    img = noise(filling, seed, [1, 3, 3, 1])
    for x in range(16):
        for y in (0, 1, 2, 3, 12, 13, 14, 15):
            img.putpixel((x, y), CRUST[1 + (x + y) % 3] + (255,))
    return img


def side():
    """The pie's crust edge: fluted pastry in rounded scallops."""
    img = Image.new("RGBA", (16, 16))
    for x in range(16):
        for y in range(16):
            tone = CRUST[3] if (x % 3 == 1 and y < 14) else CRUST[2] if y < 14 else CRUST[0]
            img.putpixel((x, y), tone + (255,))
    return img


def crust_white():
    """Near-white crust for the oven's renderer to tint as the pie bakes."""
    img = Image.new("RGBA", (16, 16))
    rng = random.Random(16300)
    for x in range(16):
        for y in range(16):
            v = rng.choice((214, 228, 240, 252)) if (x % 5) not in (1, 2) and (y % 5) not in (1, 2) else 255
            img.putpixel((x, y), (v, v, v, 255))
    return img


def raw_pie_item(filling):
    """A raw pie seen from above: pale pastry lattice over the filling, in a tin."""
    c = Canvas()
    for y in range(2, 14):
        for x in range(2, 14):
            if (x - 7.5) ** 2 + (y - 7.5) ** 2 <= 36:
                strip = x % 4 == 1 or y % 4 == 1
                c.px(x, y, RAW[2] if strip else filling[(x + y) % 3 + 1])
    for y in range(2, 14):
        for x in range(2, 14):
            if 30 < (x - 7.5) ** 2 + (y - 7.5) ** 2 <= 42:
                c.px(x, y, RAW[3])
    outline(c, TIN[0])
    return c.img


def slice_item(filling):
    """A wedge of pie: golden lattice on top, the filling and the bottom crust on its cut side."""
    c = Canvas()
    for y in range(3, 13):
        width = int((y - 3) * 1.1) + 2
        for x in range(8 - width // 2, 8 + width // 2 + 1):
            if y < 6:
                c.px(x, y, CRUST[2] if x % 3 else CRUST[3])
            elif y < 11:
                c.px(x, y, filling[(x + y) % 3 + 1])
            else:
                c.px(x, y, CRUST[1])
    outline(c, shade(CRUST[0], 0.6))
    return c.img


def dough_item():
    """A ball of pastry dough, floured."""
    c = Canvas()
    for y in range(4, 14):
        for x in range(3, 14):
            if (x - 8) ** 2 / 25 + (y - 9) ** 2 / 20 <= 1:
                c.px(x, y, RAW[(x * 3 + y) % 4])
    for x, y in ((6, 6), (9, 7), (7, 9), (10, 10)):
        c.px(x, y, (250, 248, 240))
    outline(c, shade(RAW[0], 0.7))
    return c.img


def pie_textures():
    out = {("block", "oven_brick"): bricks(), ("block", "oven_soot"): noise(SOOT, 16110, [2, 3, 2, 1]),
           ("block", "oven_embers"): noise(EMBER, 16120, [2, 3, 2, 1]), ("block", "oven_stone"): noise(STONE, 16130, [1, 3, 3, 1]),
           ("block", "pie_side"): side(), ("block", "pie_tin"): noise(TIN, 16140), ("block", "pie_crust"): crust_white(),
           ("block", "burnt_pie_top"): lattice_top(BURNT, BURNT, 16250), ("block", "burnt_pie_inside"): inside(BURNT, 16251),
           ("item", PIES["dough"]): dough_item()}
    for i, (filling, info) in enumerate(PIES["fillings"].items()):
        palette = filling_palette(info["color"])
        out[("block", f"{filling}_pie_top")] = lattice_top(palette, seed=16200 + 10 * i)
        out[("block", f"{filling}_pie_inside")] = inside(palette, 16201 + 10 * i)
        out[("item", f"raw_{filling}_pie")] = raw_pie_item(palette)
        out[("item", f"{filling}_pie_slice")] = slice_item(palette)
    return out

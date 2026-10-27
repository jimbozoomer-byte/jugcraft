"""Textures for the Glass Chapel (tools/glass_chapel.py, docs/features/glass-chapel.md): its four lair-only blocks and the
Glass Accord's icon (its map, tools/item_icons/glass_accord.txt). Painted here by code, 16 x 16, in the clean style the
other lairs use: a few flat tones a material, crisp edges, light from the top left, the patterns laid out whole within
the block rather than as noise. Called from tools/lair_textures.py. No Mojang texture is read, traced or copied.
- the Chapel Mirror: its silvered glass, pale and cool with a bright sheen running down it from the top left; veiled, the
  same glass clouded over milk-white; its brass frame and turntable, and the frame burning gold when the Abbot marks it;
- the Abbot's Lectern: pale stone, worn, and on its desk an open book whose leaves are leaded glass;
- the lancet glass: tall panes of stained glass, amber, rose, blue and green, held in dark lead;
- the rose window: petals of rose and gold round a blue heart, in lead.

The models' faces (tools/lair_data.py) take the textures at a texel a pixel.
"""
from PIL import Image

import item_icons
from lair_layout import hash01


def rgb(h):
    h = h.lstrip("#")
    return tuple(int(h[i:i + 2], 16) for i in (0, 2, 4))


def pal(*hexes, alpha=255):
    return [rgb(h) + (alpha,) for h in hexes]


SILVER = pal("5e6c78", "7c8c9a", "9aaab8", "bccad6", "e2ecf4")      # the mirror's glass, dark to light
SHEEN = rgb("f6fbff") + (255,)
VEIL = pal("a8acb2", "c2c6cc", "d8dce0", "eceef0")
BRASS = pal("4a3416", "7a5a24", "a8803a", "d0a852", "ecd082")      # dark to light
BURNING = pal("8a5a10", "d0941e", "f4c440", "ffe680", "fff8d0")    # a marked frame
STONE = pal("8a8478", "a8a296", "c4beb0", "dcd6c8")                # the lectern's pale stone
LEAD = rgb("2a2c32") + (255,)
# Stained glass, each a dark and a light, see-through.
STAINED = {"amber": pal("b06a18", "e8a840", alpha=190), "rose": pal("a03850", "e07890", alpha=190),
           "blue": pal("2c4c9a", "6a90e0", alpha=190), "green": pal("2e7040", "70b070", alpha=190),
           "gold": pal("b08a20", "f0d060", alpha=190)}


def new(fill=(0, 0, 0, 0)):
    return Image.new("RGBA", (16, 16), fill)


def mirror_glass(veiled):
    """The silvered glass: a cool gradient, lighter toward the top left, a bright sheen in two diagonal streaks. Veiled,
    milk-white cloud swirled over it."""
    img = new()
    for y in range(16):
        for x in range(16):
            if veiled:
                v = (x * 0.4 + y * 0.9 + hash01(x // 2, y // 3, 211) * 3.0) % 4.0
                c = VEIL[int(v)]
            else:
                shade = min(4, max(0, 4 - (x + y) // 7 - (1 if hash01(x, y, 213) < 0.08 else 0)))
                c = SILVER[shade]
                if (x + y) in (5, 6, 11):
                    c = SHEEN if (x + y) != 11 else SILVER[4]
            img.putpixel((x, y), c)
    return img


def frame(lit):
    """The frame's brass: bands lit on their top-left edges, a dark groove down the middle; lit, burning gold."""
    ramp = BURNING if lit else BRASS
    img = new()
    for y in range(16):
        for x in range(16):
            if x in (0, 15) or y in (0, 15):
                c = ramp[0]
            elif x in (1, 2) or y in (1, 2):
                c = ramp[3] if not (x == 1 and y == 1) else ramp[4]
            elif x in (13, 14) or y in (13, 14):
                c = ramp[1]
            elif x == 7 or y == 7:
                c = ramp[1]
            else:
                c = ramp[2]
            img.putpixel((x, y), c)
    return img


def lectern_stone():
    """The lectern's pale stone: worn blocks, lit at the top, a seam between courses."""
    img = new()
    for y in range(16):
        for x in range(16):
            if y % 5 == 4:
                c = STONE[0]
            elif (x + (3 if (y // 5) % 2 else 0)) % 8 == 7:
                c = STONE[0]
            else:
                c = STONE[3] if y % 5 == 0 else STONE[2] if hash01(x, y, 217) < 0.7 else STONE[1]
            img.putpixel((x, y), c)
    return img


def lectern_top():
    """The desk from above: a stone rim round an open book whose two leaves are leaded glass, amber and rose."""
    img = lectern_stone()
    for y in range(2, 14):
        for x in range(1, 15):
            if x in (1, 14) or y in (2, 13):
                c = BRASS[1]
            elif x == 7 or x == 8:
                c = BRASS[2] if x == 7 else BRASS[1]
            elif y % 4 == 1 or (x in (4, 11)):
                c = LEAD
            else:
                c = STAINED["amber" if x < 7 else "rose"][1 if (x + y) % 5 else 0][:3] + (255,)
            img.putpixel((x, y), c)
    return img


def lancet():
    """Stained glass in a tall lancet: diamond panes in amber, rose, blue and green, each lit on its top left, in lead."""
    img = new()
    colours = ("amber", "rose", "blue", "green")
    for y in range(16):
        for x in range(16):
            u, v = (x + y) % 8, (x - y) % 8
            if u == 0 or v == 0:
                img.putpixel((x, y), LEAD)
                continue
            pane = ((x + y) // 8 + (x - y) // 8) % len(colours)
            dark, light = STAINED[colours[pane]]
            img.putpixel((x, y), light if u < 3 or v < 3 else dark)
    return img


def rose():
    """The rose window's glass: petals of rose and gold radiating from a blue heart, in lead (the window is many blocks
    of it; each block shows a petal's worth)."""
    img = new()
    for y in range(16):
        for x in range(16):
            dx, dy = x - 7.5, y - 7.5
            ring = (dx * dx + dy * dy) ** 0.5
            if abs(ring - 7.0) < 0.6 or abs(dx) < 0.6 or abs(dy) < 0.6 or abs(abs(dx) - abs(dy)) < 0.7:
                img.putpixel((x, y), LEAD)
                continue
            if ring < 3.2:
                dark, light = STAINED["blue"]
            elif (abs(dx) > abs(dy)) == (dx * dy > 0):
                dark, light = STAINED["rose"]
            else:
                dark, light = STAINED["gold"]
            img.putpixel((x, y), light if dx + dy < 0 else dark)
    return img


def glass_chapel_textures():
    return {
        ("block", "chapel_mirror_glass"): mirror_glass(False),
        ("block", "chapel_mirror_veiled"): mirror_glass(True),
        ("block", "chapel_mirror_frame"): frame(False),
        ("block", "chapel_mirror_frame_lit"): frame(True),
        ("block", "abbot_lectern"): lectern_stone(),
        ("block", "abbot_lectern_top"): lectern_top(),
        ("block", "lancet_glass"): lancet(),
        ("block", "rose_glass"): rose(),
        ("item", "glass_accord"): item_icons.draw("glass_accord"),
    }


if __name__ == "__main__":
    import sys
    sheet = glass_chapel_textures()
    keys = sorted(sheet)
    scale = 8
    image = Image.new("RGBA", (len(keys) * (16 * scale + 8), 16 * scale), (40, 44, 52, 255))
    for i, key in enumerate(keys):
        tile = sheet[key].resize((16 * scale, 16 * scale), Image.NEAREST)
        image.paste(tile, (i * (16 * scale + 8), 0), tile)
    image.save(sys.argv[1] if len(sys.argv) > 1 else "glass_chapel_textures.png")
    print(len(keys), "textures")

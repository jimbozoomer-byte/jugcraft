"""Tool and armor textures (batch 25, docs/features/tools-and-armor.md): bronze and steel swords, pickaxes, axes,
shovels and hoes, paxels for every tier, and bronze and steel armor (inventory icons and the worn 64x32 layers).

All original: each icon is a hand-drawn mask below, coloured from a five-shade palette (0 darkest .. 4 lightest);
handles are oak brown. The worn armor is drawn plate by plate onto the humanoid UV layout.
"""
from PIL import Image

# Mask characters: digits are the head's palette shade; h/H the handle (dark/light); . is empty.
HANDLE = [(58, 40, 22), (98, 70, 40)]

SWORD = [
    "................",
    "............344.",
    "...........3442.",
    "..........3442..",
    ".........3442...",
    "........3442....",
    ".......3442.....",
    "..1...3442......",
    "..21.3442.......",
    "...2342.........",
    "...1H2..........",
    "..hH.21.........",
    ".hH....1........",
    "1h..............",
    "11..............",
    "................",
]

PICKAXE = [
    "................",
    "....233332......",
    "...3444444431...",
    "..341....hH443..",
    "..21....hH..342.",
    "..1....hH....42.",
    "......hH.....32.",
    ".....hH......31.",
    "....hH........1.",
    "...hH...........",
    "..hH............",
    ".hH.............",
    "hH..............",
    "h...............",
    "................",
    "................",
]

AXE = [
    "................",
    ".......2333.....",
    "......244443....",
    ".....24444432...",
    ".....2443hH31...",
    "......23hH31....",
    ".......hH1......",
    "......hH........",
    ".....hH.........",
    "....hH..........",
    "...hH...........",
    "..hH............",
    ".hH.............",
    "hH..............",
    "h...............",
    "................",
]

SHOVEL = [
    "................",
    "...........233..",
    "..........24443.",
    ".........244443.",
    "..........44432.",
    ".........hH332..",
    "........hH.21...",
    ".......hH.......",
    "......hH........",
    ".....hH.........",
    "....hH..........",
    "...hH...........",
    "..hH............",
    ".hH.............",
    "hH..............",
    "................",
]

HOE = [
    "................",
    "......23333.....",
    ".....2444443....",
    ".....21..hH43...",
    "........hH..1...",
    ".......hH.......",
    "......hH........",
    ".....hH.........",
    "....hH..........",
    "...hH...........",
    "..hH............",
    ".hH.............",
    "hH..............",
    "h...............",
    "................",
    "................",
]

# A paxel: a pick head on one side, an axe blade on the other and a shovel tip on top, on one long handle.
PAXEL = [
    "..........2333..",
    "...23332.244443.",
    "..34444443444432",
    "..341...hH44431.",
    "..21...hH.3442..",
    "..1...hH...321..",
    "......hH........",
    ".....hH.........",
    "....hH..........",
    "...hH...........",
    "..hH............",
    ".hH.............",
    "hH..............",
    "h...............",
    "................",
    "................",
]

HELMET = [
    "................",
    "................",
    "....23333332....",
    "...2444444443...",
    "..244433334442..",
    "..243......342..",
    "..242......242..",
    "..231......132..",
    "..11........11..",
    "................",
    "................",
    "................",
    "................",
    "................",
    "................",
    "................",
]

CHESTPLATE = [
    "................",
    "..2333....3332..",
    ".2444431134444..",
    ".2444444444442..",
    ".1344444444431..",
    "..1.2444442.1...",
    "....2444442.....",
    "....2433342.....",
    "....2444442.....",
    "....2444442.....",
    "....1333331.....",
    "................",
    "................",
    "................",
    "................",
    "................",
]

LEGGINGS = [
    "................",
    "................",
    "....23333332....",
    "....24444442....",
    "....24433442....",
    "....2442.442....",
    "....2442.442....",
    "....2441.442....",
    "....2441.442....",
    "....2431.342....",
    "....1331.331....",
    "................",
    "................",
    "................",
    "................",
    "................",
]

BOOTS = [
    "................",
    "................",
    "................",
    "................",
    "................",
    "...233....332...",
    "...244....442...",
    "...244....442...",
    "...243....342...",
    "..2443....3442..",
    ".24442....24442.",
    ".13331....13331.",
    "................",
    "................",
    "................",
    "................",
]

TOOLS = {"sword": SWORD, "pickaxe": PICKAXE, "axe": AXE, "shovel": SHOVEL, "hoe": HOE}
ARMOR = {"helmet": HELMET, "chestplate": CHESTPLATE, "leggings": LEGGINGS, "boots": BOOTS}

# Head palettes for the paxel tiers that are not Jugcraft metals (darkest .. lightest), our own picks.
VANILLA_TIERS = {
    "wood": [(64, 44, 22), (96, 68, 36), (128, 94, 52), (160, 122, 72), (190, 152, 98)],
    "stone": [(58, 58, 58), (88, 88, 88), (116, 116, 116), (142, 142, 142), (170, 170, 170)],
    "iron": [(96, 96, 96), (150, 150, 150), (196, 196, 196), (224, 224, 224), (248, 248, 248)],
    "gold": [(140, 96, 20), (196, 150, 30), (234, 196, 52), (250, 226, 96), (255, 246, 170)],
    "diamond": [(18, 92, 86), (30, 150, 140), (60, 206, 196), (130, 236, 226), (206, 252, 248)],
    "netherite": [(30, 26, 30), (50, 44, 48), (72, 64, 68), (96, 88, 92), (122, 114, 116)],
}


def icon(mask, palette):
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for y, row in enumerate(mask):
        for x, ch in enumerate(row):
            if ch.isdigit():
                img.putpixel((x, y), tuple(palette[int(ch)]) + (255,))
            elif ch in "hH":
                img.putpixel((x, y), HANDLE[ch == "H"] + (255,))
    return img


# Humanoid armor UV boxes (x, y, w, h, d) on the 64x32 sheet: each box's six faces sit in the usual net.
HEAD, BODY, RIGHT_ARM, RIGHT_LEG = (0, 0, 8, 8, 8), (16, 16, 8, 12, 4), (40, 16, 4, 12, 4), (0, 16, 4, 12, 4)


def _faces(box):
    """The six faces of a box on the sheet as (left, top, width, height) rectangles."""
    u, v, w, h, d = box
    return [(u + d, v, w, d), (u + d + w, v, w, d),  # top, bottom
            (u, v + d, d, h), (u + d, v + d, w, h), (u + d + w, v + d, d, h), (u + 2 * d + w, v + d, w, h)]


def _plate(img, rect, palette, seam_every=4, rows=None):
    """Fills one face with plate: a light top edge, a dark bottom edge, a seam every few rows and rivets."""
    left, top, width, height = rect
    for y in range(top, top + height):
        if rows is not None and not rows(y - top, height):
            continue
        for x in range(left, left + width):
            row = y - top
            if row == 0:
                c = palette[4]
            elif row == height - 1:
                c = palette[0]
            elif row % seam_every == 0:
                c = palette[1]
            else:
                c = palette[3 if (x + y) % 7 else 2]
            img.putpixel((x, y), tuple(c) + (255,))
    # Rivets at the top corners of wide faces, where that part of the face is plated.
    if width >= 6 and height >= 6 and (rows is None or rows(1, height)):
        for x, y in ((left + 1, top + 1), (left + width - 2, top + 1)):
            img.putpixel((x, y), tuple(palette[4]) + (255,))


def armor_layer(palette, leggings):
    """The worn armor on the 64x32 humanoid sheet. Layer 1 (helmet, chestplate, boots): head, body, arms and the
    boot part of the legs. Layer 2 (leggings): the waist of the body and the upper legs."""
    img = Image.new("RGBA", (64, 32), (0, 0, 0, 0))
    if leggings:
        for rect in _faces(BODY)[2:]:
            _plate(img, rect, palette, rows=lambda r, h: r >= h - 4)
        for rect in _faces(RIGHT_LEG):
            _plate(img, rect, palette, rows=lambda r, h: r < 9)
        return img
    for rect in _faces(HEAD):
        _plate(img, rect, palette, seam_every=3)
    # The visor: a dark slot across the helmet's front face.
    for x in range(9, 15):
        img.putpixel((x, 12), tuple(palette[0]) + (255,))
    for rect in _faces(BODY):
        _plate(img, rect, palette)
    for rect in _faces(RIGHT_ARM):
        _plate(img, rect, palette, rows=lambda r, h: r < 6)
    for rect in _faces(RIGHT_LEG):
        _plate(img, rect, palette, rows=lambda r, h: r >= h - 4)
    return img


def draw_all(save, save_armor, part_palette):
    """save(img, kind, name) as in generate_textures; save_armor(img, layer, name) for the worn layers."""
    for metal in ("bronze", "steel"):
        palette = part_palette(metal)
        for tool, mask in TOOLS.items():
            save(icon(mask, palette), "item", f"{metal}_{tool}")
        for piece, mask in ARMOR.items():
            save(icon(mask, palette), "item", f"{metal}_{piece}")
        save_armor(armor_layer(palette, False), "humanoid", metal)
        save_armor(armor_layer(palette, True), "humanoid_leggings", metal)
    for tier, palette in list(VANILLA_TIERS.items()) + [("bronze", part_palette("bronze")),
                                                         ("steel", part_palette("steel"))]:
        save(icon(PAXEL, palette), "item", f"{tier}_paxel")

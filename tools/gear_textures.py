"""Tool and armor textures (batch 25, docs/features/tools-and-armor.md): bronze and steel swords, pickaxes, axes,
shovels and hoes, and paxels for every tier. The bronze and steel armor (icons and worn layers) is in armor_styles.py.

All original: each icon is a hand-drawn mask below, coloured from a five-shade palette (0 darkest .. 4 lightest);
handles are oak brown.
"""
from PIL import Image

import armor_styles

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


TOOLS = {"sword": SWORD, "pickaxe": PICKAXE, "axe": AXE, "shovel": SHOVEL, "hoe": HOE}

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


def draw_all(save, save_armor, part_palette):
    """save(img, kind, name) as in generate_textures; save_armor(img, layer, name) for the worn layers."""
    for metal in ("bronze", "steel"):
        palette = part_palette(metal)
        for tool, mask in TOOLS.items():
            save(icon(mask, palette), "item", f"{metal}_{tool}")
        # Armor: steampunk bronze and kaiserpunk steel, drawn in tools/armor_styles.py.
        armor = armor_styles.palette(metal, palette)
        for piece in ("helmet", "chestplate", "leggings", "boots"):
            save(armor_styles.icon(metal, piece, armor), "item", f"{metal}_{piece}")
        save_armor(armor_styles.layer(metal, armor, False), "humanoid", metal)
        save_armor(armor_styles.layer(metal, armor, True), "humanoid_leggings", metal)
    for tier, palette in list(VANILLA_TIERS.items()) + [("bronze", part_palette("bronze")),
                                                         ("steel", part_palette("steel"))]:
        save(icon(PAXEL, palette), "item", f"{tier}_paxel")

"""Original textures for the third batch of Halloween decorations, the graveyard (requires Pillow): wrought iron, the
crypt set, the Grave Mound and its zombie hand, the Mourning Angel and the Pop-Up Skeleton.

Painted in the clean, cartoon style (docs/ART_DIRECTION.md, "Creatures and faces: cute and clean"): flat tones, lit
and shaded edges, features in fixed places, no random speckle.

Called from crop_textures.crop_textures(). Every pixel is drawn here by code; no Mojang texture is read, traced or
recoloured. Block and item textures are 16x16 and opaque.
"""
from crop_textures import Canvas, rgb, outline
from halloween_textures import shade

IRON = [rgb("1c1c20"), rgb("26262c"), rgb("303038"), rgb("44444e")]
RUST = rgb("5a3a22")
CRYPT = [rgb("3a3c3e"), rgb("46484a"), rgb("525456"), rgb("5e6062")]
MORTAR = rgb("2a2a2c")
LICHEN = [rgb("4e5a34"), rgb("5f6c3c")]
SOIL = [rgb("2e2014"), rgb("3a2a1a"), rgb("4a3622"), rgb("5a4430")]
GRASS = [rgb("3a5a24"), rgb("4c6e2c"), rgb("5e7e36")]
LEAF = [rgb("8a4a1a"), rgb("a8642a"), rgb("6a3a14")]
ZOMBIE = [rgb("2e5a32"), rgb("3c7040"), rgb("4c844e"), rgb("5c9a5c")]
CLOTH = [rgb("2a2a40"), rgb("34344e"), rgb("1e1e30")]
MARBLE = [rgb("a8a8a2"), rgb("bebeb8"), rgb("d0d0ca"), rgb("e0e0da")]
VEIN = rgb("8e8e8a")
PLINTH = [rgb("5a5c58"), rgb("686a64"), rgb("767872")]
PLANK = [rgb("4a3a2c"), rgb("5a4834"), rgb("6a563e"), rgb("7a644a")]
BONE = [rgb("b8ae90"), rgb("ccc2a6"), rgb("ddd4ba"), rgb("ece4cc")]
SOCKET = rgb("1a1612")


def flat(c, x0, y0, x1, y1, palette, seed=0, weights=None):
    """Fills x0..x1, y0..y1 flat in the palette's commonest tone (by `weights`; the middle one by default)."""
    w = weights or [1] * len(palette)
    k = max(range(len(palette)), key=lambda i: (w[i], -abs(i - (len(palette) - 1) / 2)))
    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1):
            c.px(x, y, palette[k])


def wrought_iron():
    """Wrought iron painted black: flat, a sheen down every fourth column, two neat spots of rust."""
    c = Canvas()
    for y in range(16):
        for x in range(16):
            c.px(x, y, IRON[3] if x % 4 == 0 else IRON[1])
    c.px(6, 4, RUST)
    c.px(13, 11, RUST)
    return c.img


def crypt_bricks(seed, lichen=True):
    """Dark stone bricks: rows four pixels tall, offset every other row, each brick flat in one of two tones and lit
    along its top, with a neat crack and a few tufts of lichen."""
    c = Canvas()
    for y in range(16):
        row = y // 4
        for x in range(16):
            u = x + (4 if row % 2 else 0)
            joint = y % 4 == 3 or u % 8 == 7
            brick = u // 8 + row
            c.px(x, y, MORTAR if joint else CRYPT[3] if y % 4 == 0 else CRYPT[2] if brick % 2 else CRYPT[1])
    for i, (x, y) in enumerate(((9, 5), (10, 6), (11, 6))):
        c.px(x, y, MORTAR)
    if lichen:
        for x, y in ((3, 9), (12, 2), (6, 14)):
            c.px(x, y, LICHEN[1])
            c.px(x + 1, y, LICHEN[0])
    return c.img


def chiseled_crypt():
    """A sunken panel framed in stone with a skull carved in it."""
    c = Canvas()
    flat(c, 0, 0, 15, 15, CRYPT[1:], 9611, [2, 3, 1])
    c.rect(1, 1, 14, 14, CRYPT[0])
    flat(c, 2, 2, 13, 13, CRYPT[1:3], 9612)
    skull = shade(CRYPT[3], 1.15)
    c.rect(5, 4, 10, 9, skull)
    c.rect(6, 10, 9, 11, skull)
    for x, y in ((6, 6), (6, 7), (9, 6), (9, 7), (7, 8), (8, 8), (6, 11), (8, 11)):
        c.px(x, y, MORTAR)
    return c.img


def pillar_side():
    """A fluted column: light ridges between dark grooves, a band at top and bottom."""
    c = Canvas()
    for y in range(16):
        for x in range(16):
            color = CRYPT[0] if x % 4 == 0 else CRYPT[3] if x % 4 == 2 else CRYPT[2]
            if y in (0, 1, 14, 15):
                color = CRYPT[1]
            c.px(x, y, color)
    for x, y in ((5, 6), (10, 11)):
        c.px(x, y, LICHEN[1])
    return c.img


def pillar_top():
    c = Canvas()
    flat(c, 0, 0, 15, 15, CRYPT[1:3], 9631)
    for ring, color in ((0, CRYPT[0]), (3, CRYPT[3]), (6, CRYPT[0])):
        for i in range(ring, 16 - ring):
            for x, y in ((i, ring), (i, 15 - ring), (ring, i), (15 - ring, i)):
                c.px(x, y, color)
    return c.img


def crypt_door(top):
    """A heavy stone slab with iron bands and rivets; the top half has a carved cross above a barred grille."""
    c = Canvas()
    flat(c, 0, 0, 15, 15, CRYPT[1:], 9641 + top, [2, 3, 1])
    c.rect(0, 0, 0, 15, MORTAR)
    c.rect(15, 0, 15, 15, MORTAR)
    for y in ((3, 12) if not top else (4,)):
        c.rect(1, y, 14, y + 1, IRON[2])
        for x in (2, 13):
            c.px(x, y, IRON[3])
    if top:
        c.rect(6, 7, 9, 14, MORTAR)
        for x in (7, 8):
            for y in range(7, 15):
                c.px(x, y, IRON[1] if y % 2 else IRON[2])
        c.rect(7, 0, 8, 3, shade(CRYPT[3], 1.15))
        c.rect(6, 1, 9, 1, shade(CRYPT[3], 1.15))
    else:
        c.px(12, 7, IRON[3])
        c.rect(11, 6, 13, 8, IRON[2])
        c.px(12, 7, IRON[0])
    return c.img


def crypt_door_item():
    c = Canvas()
    c.rect(4, 1, 11, 14, CRYPT[2])
    for y in (4, 11):
        c.rect(4, y, 11, y, IRON[2])
    c.rect(7, 2, 8, 3, MORTAR)
    c.px(10, 8, IRON[3])
    outline(c, rgb("141416"))
    return c.img


# ---------------------------------------------------------------- the grave mound

def mound_top():
    """Freshly turned earth in flat rows of clods, a few neat blades of grass and fallen leaves."""
    c = Canvas()
    for y in range(16):
        for x in range(16):
            c.px(x, y, SOIL[3] if y % 4 == 0 and x % 4 != 3 else SOIL[0] if y % 4 == 3 else SOIL[2] if (x // 4 + y // 4) % 2 else SOIL[1])
    for x, y in ((2, 5), (9, 2), (13, 10), (5, 13), (11, 6)):
        c.px(x, y, GRASS[2])
        c.px(x, y - 1, GRASS[1])
    for x, y, k in ((4, 9, 0), (12, 3, 1), (7, 6, 2)):
        c.px(x, y, LEAF[k])
        c.px(x + 1, y, LEAF[k])
    return c.img


def mound_side():
    """The mound's side: flat earth in even layers, with two pebbles."""
    c = Canvas()
    for y in range(16):
        for x in range(16):
            c.px(x, y, SOIL[0] if y % 5 == 4 else SOIL[2] if y % 5 == 0 else SOIL[1])
    c.px(4, 7, rgb("6a6058"))
    c.px(11, 12, rgb("6a6058"))
    return c.img


def zombie_hand():
    """Mottled green skin, darker at the knuckles and between the fingers."""
    c = Canvas()
    flat(c, 0, 0, 15, 15, ZOMBIE[1:], 9721, [2, 3, 1])
    for y in (4, 9, 13):
        for x in range(0, 16, 3):
            c.px(x, y, ZOMBIE[0])
    return c.img


def sleeve():
    c = Canvas()
    flat(c, 0, 0, 15, 15, CLOTH, 9731, [3, 2, 1])
    for x in range(0, 16, 2):
        c.px(x, 15, CLOTH[2])
        c.px(x + 1, 14, CLOTH[2])
    return c.img


# ---------------------------------------------------------------- the mourning angel

def marble():
    """Weathered marble: pale with grey veins and a little lichen."""
    c = Canvas()
    flat(c, 0, 0, 15, 15, MARBLE, 9801, [1, 2, 3, 2])
    # One smooth vein wandering down, and two tufts of lichen.
    for y in range(16):
        c.px((2 + (y // 3) % 3) % 16, y, VEIN)
    for x, y in ((11, 5), (6, 12)):
        c.px(x, y, LICHEN[1])
        c.px(x + 1, y, LICHEN[0])
    return c.img


def wing():
    """Rows of overlapping marble feathers, each with a shaded lower edge."""
    c = Canvas()
    for y in range(16):
        for x in range(16):
            edge = (y + (x // 4) % 2 * 2) % 4 == 3
            top = (y + (x // 4) % 2 * 2) % 4 == 0
            c.px(x, y, MARBLE[0] if edge else MARBLE[3] if top else MARBLE[2])
    return c.img


def plinth():
    c = Canvas()
    flat(c, 0, 0, 15, 15, PLINTH, 9821, [2, 3, 2])
    for x in range(16):
        c.px(x, 0, PLINTH[2])
        c.px(x, 15, PLINTH[0])
    for x, y in ((3, 12), (9, 10), (13, 13)):
        c.px(x, y, LICHEN[1])
        c.px(x + 1, y, LICHEN[0])
    return c.img


def angel_item():
    """A bowed white figure with folded wings on a grey plinth."""
    c = Canvas()
    c.rect(4, 13, 11, 15, PLINTH[1])
    c.rect(6, 5, 9, 12, MARBLE[2])
    c.rect(5, 9, 10, 12, MARBLE[1])
    c.rect(6, 2, 9, 4, MARBLE[3])
    c.rect(7, 4, 8, 5, MARBLE[3])
    for y in range(2, 11):
        c.px(3, y + 1, MARBLE[1])
        c.px(4, y, MARBLE[2])
        c.px(11, y, MARBLE[2])
        c.px(12, y + 1, MARBLE[1])
    outline(c, rgb("3a3a38"))
    return c.img


# ---------------------------------------------------------------- the pop-up skeleton

def crate():
    """Weathered planks with dark gaps between them and a nail at each end."""
    c = Canvas()
    for y in range(16):
        for x in range(16):
            c.px(x, y, PLANK[0] if y % 5 == 4 else PLANK[3] if y % 5 == 0 else PLANK[2] if (y // 5) % 2 else PLANK[1])
    for y in (1, 6, 11):
        for x in (1, 14):
            c.px(x, y, IRON[3])
    return c.img


def crate_lid():
    """Planks with a diagonal brace."""
    img = crate()
    c = Canvas()
    c.img = img
    for i in range(16):
        c.px(i, i, PLANK[3])
        c.px(i, min(15, i + 1), PLANK[0])
    return c.img


def crate_inside():
    c = Canvas()
    flat(c, 0, 0, 15, 15, [rgb("1e1810"), rgb("2a2016")], 9911)
    return c.img


def spring():
    c = Canvas()
    for y in range(16):
        for x in range(16):
            c.px(x, y, IRON[3] if y % 3 == 0 else IRON[1])
    return c.img


def bone():
    c = Canvas()
    flat(c, 0, 0, 15, 15, BONE, 9921, [1, 2, 3, 2])
    return c.img


def ribs():
    """Ribs: bone curves with dark gaps between them, a spine down the middle."""
    c = Canvas()
    flat(c, 0, 0, 15, 15, BONE[1:], 9931)
    for x in range(16):
        c.px(x, 0, BONE[3])
    for y in range(1, 16, 3):
        for x in range(16):
            if x not in (7, 8):
                c.px(x, y, SOCKET)
    return c.img


def skull():
    """A cute grinning skull: two big round eye sockets with a white glint, no nose hole, and a neat row of teeth."""
    c = Canvas()
    flat(c, 0, 0, 15, 15, BONE[1:], 9941, [2, 3, 2])
    for x in range(16):
        c.px(x, 0, BONE[3])
    for x0 in (3, 9):
        c.rect(x0, 4, x0 + 3, 7, SOCKET)
        for x, y in ((x0, 4), (x0 + 3, 4), (x0, 7), (x0 + 3, 7)):
            c.px(x, y, BONE[2])
        c.px(x0 + 1, 5, rgb("ffffff"))
    c.rect(3, 11, 12, 13, BONE[3])
    for x in range(4, 13, 2):
        c.rect(x, 11, x, 13, SOCKET)
    return c.img


def skeleton_item():
    """A crate with a skull and a hand springing out of it."""
    c = Canvas()
    c.rect(2, 9, 13, 15, PLANK[2])
    c.rect(2, 12, 13, 12, PLANK[0])
    c.rect(5, 1, 10, 5, BONE[3])
    c.px(6, 3, SOCKET)
    c.px(9, 3, SOCKET)
    c.rect(7, 6, 8, 8, IRON[3])
    c.px(3, 4, BONE[2])
    c.px(12, 4, BONE[2])
    c.px(4, 5, BONE[2])
    c.px(11, 5, BONE[2])
    outline(c, rgb("1a1612"))
    return c.img


def decor3_textures():
    """(kind, name) -> image for every texture of the third decorations batch."""
    return {
        ("block", "cemetery_iron"): wrought_iron(),
        ("block", "crypt_stone"): crypt_bricks(9651),
        ("block", "chiseled_crypt_stone"): chiseled_crypt(),
        ("block", "crypt_stone_pillar"): pillar_side(),
        ("block", "crypt_stone_pillar_top"): pillar_top(),
        ("block", "crypt_door_bottom"): crypt_door(False),
        ("block", "crypt_door_top"): crypt_door(True),
        ("item", "crypt_door"): crypt_door_item(),
        ("block", "grave_mound_top"): mound_top(),
        ("block", "grave_mound_side"): mound_side(),
        ("block", "grave_mound_hand"): zombie_hand(),
        ("block", "grave_mound_sleeve"): sleeve(),
        ("block", "mourning_angel_marble"): marble(),
        ("block", "mourning_angel_wing"): wing(),
        ("block", "mourning_angel_plinth"): plinth(),
        ("item", "mourning_angel"): angel_item(),
        ("block", "pop_up_skeleton_crate"): crate(),
        ("block", "pop_up_skeleton_lid"): crate_lid(),
        ("block", "pop_up_skeleton_inside"): crate_inside(),
        ("block", "pop_up_skeleton_spring"): spring(),
        ("block", "pop_up_skeleton_bone"): bone(),
        ("block", "pop_up_skeleton_ribs"): ribs(),
        ("block", "pop_up_skeleton_skull"): skull(),
        ("item", "pop_up_skeleton"): skeleton_item(),
    }

"""Beardshy's look: his whole 64x64 skin on the villager model's UV layout.

Run from the repository root:
    python3 tools/beardshy_look.py

Beardshy is the shy side of Beardson, the bearded millennial who runs the Retro Game Shop (retro_trader_look.py): the
same villager body, his own look. Curly dark-brown hair, a ginger beard, freckles and rosy cheeks (he blushes a lot),
round tortoiseshell glasses with his eyes cast down, his pink hoodie with its drawstrings, kangaroo pocket and the hood
bunched behind his neck, black headphones round his neck, "uwu" inked on the backs of his hands, dark jeans and grey
sneakers. Everything is original pixel art drawn here; nothing is read, traced or recoloured from Mojang's textures.

Unlike the Retro Trader's overlay, this is a whole skin (BeardshyRenderer draws it alone on the villager model), so
every face of every part he shows is painted. The villager's nose is not raised (no mixin): it covers columns 3 and 4
of the face's rows 7-9, so his mouth is hidden in his beard behind it, as a villager's is.
"""
from pathlib import Path

from PIL import Image

from retro_trader_look import box_faces, fill, shade, strand

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "src" / "main" / "resources" / "assets" / "jugcraft" / "textures" / "entity" / "beardshy" / "beardshy.png"

CLEAR = (0, 0, 0, 0)
SKIN = [(234, 190, 164), (216, 168, 142), (194, 144, 120)]
FRECKLE = (198, 124, 92)
BLUSH = [(236, 140, 142), (222, 116, 124)]
HAIR = [(58, 38, 28), (76, 50, 36), (40, 26, 20)]
BEARD = [(182, 88, 42), (156, 70, 32), (124, 54, 26)]
BROW = (70, 40, 26)
FRAME = [(118, 70, 38), (166, 108, 58)]  # tortoiseshell: dark, and its amber flecks
LENS = (226, 230, 232)
IRIS = (92, 64, 44)
HOODIE = [(234, 168, 178), (216, 148, 160), (194, 126, 140), (246, 192, 200)]  # base, shade, deep shade, highlight
STRING = (248, 238, 238)
AGLET = (196, 196, 204)
TEE = (238, 238, 232)
PHONES = [(30, 30, 34), (52, 52, 58)]
INK = (44, 36, 42)
JEANS = [(54, 66, 98), (44, 54, 82), (68, 82, 118), (88, 104, 140)]
SHOE = [(76, 76, 84), (58, 58, 66)]
SOLE = (236, 232, 224)


def curls(x, y):
    """Tight dark curls: a lighter loop every few pixels on a staggered grid."""
    if (x + 2 * (y // 2)) % 3 == 0 and y % 2 == 0:
        return HAIR[1]
    return HAIR[2] if (x * 3 + y) % 5 == 0 else HAIR[0]


def beard(x, y):
    return strand(BEARD[:2], x, y)


def knit(x, y):
    """Soft fleece: the base pink with a faint diagonal."""
    return HOODIE[1] if (x + y) % 6 == 0 else HOODIE[0]


def rib(x, y):
    """The ribbed cuffs and hem: alternate raised and sunken columns."""
    return HOODIE[1] if x % 2 else HOODIE[2]


def denim(x, y):
    return JEANS[2] if (x + y) % 5 == 0 else JEANS[0] if (x + 2 * y) % 7 else JEANS[1]


# ---------------------------------------------------------------- the face (8 wide, 10 tall: the head's front)

def face(x, y):
    """Curls over his forehead, worried brows, round glasses with his eyes cast down, rosy freckled cheeks, beard."""
    if y == 0:
        return curls(x, y)
    if y == 1:
        return curls(x, y) if x in (0, 1, 3, 6, 7) else SKIN[0]  # a curl falling over his forehead
    if y == 2:
        # Short brows near the middle, lifted at the inner ends: worried.
        return BROW if x in (2, 5) else shade(BROW, 30) if x in (1, 6) else SKIN[0]
    left = x < 4
    lx = x if left else 7 - x  # 0 at the temple, 3 at the bridge
    if y in (3, 6):
        # The round frames' top and bottom; at the bottom corners his cheeks show, blushing.
        if lx in (1, 2):
            return FRAME[1] if (y == 3 and lx == 2) else FRAME[0]
        return BLUSH[0] if y == 6 and lx == 0 else SKIN[0]
    if y in (4, 5):
        if lx in (0, 3):
            return FRAME[0]
        if y == 4:
            return SKIN[1]  # his eyelids, lowered
        # Looking down and in: the irises low in the lenses, towards his nose.
        return IRIS if lx == 2 else LENS
    if y == 7:
        if lx == 0:
            return beard(x, y)
        if lx == 1:
            return BLUSH[1]
        return FRECKLE if lx == 2 and (x % 2 == 0) else SKIN[1]
    return beard(x, y) if not (y == 8 and lx == 3) else BEARD[2]  # the moustache under his nose


def side(front_at_high_u):
    def paint(x, y):
        toward_front = x if front_at_high_u else 7 - x
        if y >= 7 or (y >= 5 and toward_front >= 5):
            return beard(x, y) if toward_front >= 2 else curls(x, y)  # the beard round his jaw
        if y == 4 and toward_front >= 4:
            return FRAME[0]  # the arm of his glasses
        if toward_front >= 6 and y >= 2:
            return BLUSH[0] if (y == 5 and toward_front == 7) else SKIN[0]  # cheek
        if toward_front == 4 and 3 <= y <= 5:
            return BLUSH[1] if y == 4 else SKIN[1]  # his ear, red too
        return curls(x, y)
    return paint


def skin():
    img = Image.new("RGBA", (64, 64), CLEAR)

    # Head (0,0; 8x10x8).
    head = box_faces(0, 0, 8, 10, 8)
    fill(img, head["front"], face)
    fill(img, head["top"], curls)
    fill(img, head["bottom"], beard)
    fill(img, head["back"], lambda x, y: curls(x, y) if y < 8 else SKIN[1])
    fill(img, head["right"], side(True))
    fill(img, head["left"], side(False))

    # Hat layer (32,0; the head grown a half pixel): his curls standing up off his head, open at the face.
    hat = box_faces(32, 0, 8, 10, 8)
    fill(img, hat["top"], curls)
    fill(img, hat["front"], lambda x, y: curls(x, y) if y == 0 and x != 4 else None)
    fill(img, hat["back"], lambda x, y: curls(x, y) if y < 7 or (y == 7 and x % 3) else None)

    def hat_side(front_at_high_u):
        def paint(x, y):
            toward_front = x if front_at_high_u else 7 - x
            if y <= 1 or (y <= 4 and toward_front <= 3) or (y <= 6 and toward_front <= 1):
                return curls(x, y)
            return None
        return paint
    fill(img, hat["right"], hat_side(True))
    fill(img, hat["left"], hat_side(False))

    # Nose (24,0; 2x4x2): red with blushing, the moustache along its underside.
    nose = box_faces(24, 0, 2, 4, 2)
    for name in ("front", "right", "left", "back"):
        fill(img, nose[name], lambda x, y: BEARD[2] if y == 3 else BLUSH[0] if y == 2 else SKIN[0])
    fill(img, nose["top"], lambda x, y: SKIN[0])
    fill(img, nose["bottom"], lambda x, y: BEARD[2])

    def front(x, y):
        """The hoodie: headphones round his neck, drawstrings, the cable, the kangaroo pocket and the hem; jeans."""
        if y >= 12:
            return denim(x, y)
        if y == 11:
            return rib(x, y)
        if y == 0:
            if x in (1, 6):
                return PHONES[0]  # the cups
            return TEE if x in (3, 4) else HOODIE[2]  # his tee at the neck, the hood's edge
        if x in (1, 6) and y == 1:
            return PHONES[1]
        if x in (2, 5) and 1 <= y <= 4:
            return AGLET if y == 4 else STRING
        if x == 4 and 1 <= y <= 9:
            return PHONES[0]  # the cable down his front
        if 7 <= y <= 10 and 1 <= x <= 6:
            if y == 7 or x in (1, 6):
                return HOODIE[2]  # the pocket's seam
            return HOODIE[1]
        return knit(x, y)

    def back(x, y):
        if y >= 12:
            return denim(x, y)
        if y == 11:
            return rib(x, y)
        if y <= 3:
            # The hood bunched behind his neck, its opening dark.
            if y <= 1 and 2 <= x <= 5:
                return HOODIE[2]
            return HOODIE[3] if y == 3 or x in (1, 6) else HOODIE[1]
        return knit(x, y)

    def flank(x, y):
        if y >= 12:
            return denim(x, y)
        return rib(x, y) if y == 11 else PHONES[0] if y == 0 and x in (1, 2) else knit(x, y)

    # Body (16,20; 8x12x6) and the robe layer over it (0,38; 8x20x6): hoodie above, jeans below.
    for faces in (box_faces(16, 20, 8, 12, 6), box_faces(0, 38, 8, 20, 6)):
        fill(img, faces["front"], front)
        fill(img, faces["back"], back)
        fill(img, faces["right"], flank)
        fill(img, faces["left"], flank)
        fill(img, faces["top"], lambda x, y: SKIN[1] if 3 <= x <= 4 and 2 <= y <= 3 else HOODIE[2] if y >= 4 else HOODIE[1])
        fill(img, faces["bottom"], denim)

    # Folded arms (44,22; 4x8x4): hoodie sleeves, a ribbed cuff at the elbow end.
    arm = box_faces(44, 22, 4, 8, 4)
    for name in ("top", "front", "back", "right", "left"):
        fill(img, arm[name], lambda x, y: rib(x, y) if y == 7 else knit(x, y))
    fill(img, arm["bottom"], rib)

    # The forearms across his chest (40,38; 8x4x4): sleeves to the wrist, his hands in the middle with "uwu" on them.
    forearms = box_faces(40, 38, 8, 4, 4)

    def forearm(x, y):
        if x in (2, 5):
            return rib(x, y)  # the cuffs
        if x in (3, 4):
            # The backs of his hands: a little ink on each.
            return INK if (y == 1 and x == 3) or (y == 2 and x == 4) else SKIN[0]
        return knit(x, y)
    for name in ("top", "bottom", "back", "front"):
        fill(img, forearms[name], forearm)
    fill(img, forearms["right"], knit)
    fill(img, forearms["left"], knit)

    # Legs (0,22; 4x12x4): only the bottom shows below the robe: dark jeans, then grey sneakers with white soles.
    leg = box_faces(0, 22, 4, 12, 4)

    def leg_side(toe):
        def paint(x, y):
            if y < 9:
                return denim(x, y)
            if y == 11:
                return SOLE
            if toe and y == 10:
                return SOLE  # the toe cap
            if toe and y == 9 and x in (1, 2):
                return STRING  # the laces
            return SHOE[1] if y == 9 else SHOE[0]
        return paint
    fill(img, leg["front"], leg_side(True))
    for name in ("right", "left", "back"):
        fill(img, leg[name], leg_side(False))
    fill(img, leg["top"], denim)
    fill(img, leg["bottom"], lambda x, y: SOLE)
    return img


EGG_OUT = ROOT / "src" / "main" / "resources" / "assets" / "jugcraft" / "textures" / "item" / "beardshy_spawn_egg.png"


def spawn_egg():
    """His spawn egg (docs/ITEM_ICONS.md): vanilla's egg form drawn fresh, his hoodie's pink with ginger spots for his
    beard, lit from the top left, outlined in the pink's own darkest tone."""
    rows = [
        "................",
        "......dddd......",
        ".....dHPPPd.....",
        "....dHPoPPPd....",
        "....dPooPPPd....",
        "...dPPPPPoPPd...",
        "...dPPPPooPPd...",
        "...dPoPPPPPPPd..",
        "...dooPPPPoPPd..",
        "...dPPPPPoopPd..",
        "...dPPPPPPPppd..",
        "....dPoPPPPpd...",
        "....dPooPPppd...",
        ".....dPPPppd....",
        "......ddddd.....",
        "................",
    ]
    tones = {"d": (128, 70, 86), "P": HOODIE[0], "p": HOODIE[2], "H": HOODIE[3], "o": BEARD[0]}
    img = Image.new("RGBA", (16, 16), CLEAR)
    for y, row in enumerate(rows):
        for x, ch in enumerate(row):
            if ch in tones:
                img.putpixel((x, y), tones[ch][:3] + (255,))
    return img


def main():
    OUT.parent.mkdir(parents=True, exist_ok=True)
    skin().save(OUT)
    spawn_egg().save(EGG_OUT)
    print(f"wrote {OUT.relative_to(ROOT)} and {EGG_OUT.relative_to(ROOT)}")


if __name__ == "__main__":
    main()

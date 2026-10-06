"""The bronze and steel armor's 16x16 inventory icons: the knight armor (tools/knight_armor.py) drawn small, in the
owner's style for item icons (vanilla's own size, a one-pixel outline in the material's darkest tone, light from the
top left, a few flat tones, chunky parts that read at a glance).

Each piece is one hand-drawn map, tools/armor_icons/<piece>.txt: 16 lines of 16 symbols, which the owner can edit
directly (lines starting with # are comments). A map names no colours, only what each pixel is made of, so one map
serves both metals: steel_<piece> is coloured from armor_paint.STEEL (the owner's sampled steel, gold and leather) and
bronze_<piece> from armor_paint.BRONZE, the same palettes the 3D armor is painted in.

What each icon carries of the knight (front view, as vanilla draws armor; boots in vanilla's stance):
    helmet      the big diamond crest overhanging the helm, with its nested chevrons and the finial, the horn fins at
                its corners, the dark seam under it, the eye slits beside the nasal bar, and the gold collar
    chestplate  the two layered pauldrons with nested L's toward the armpit, the light chevron plate over the chest
                with its bold dark V and the keel at its point, the hem lame, and the dark under-layer at the waist
    leggings    the brown leather belt (the under-layer's light top edge, two plates, the strap across, the
                under-layer showing at the centre) over the skirt of four plate lames, light over dark, widening to
                the hem, with the dark seam down the middle
    boots       greaves with a rolled top, a shadow under it and the knee cop's small chevron, and sabatons of banded
                instep lames, light over dark, with pointed toes
The bronze icons add what the bronze variant adds in brass: the finial and horn tips, rivets on the pauldron lames, the
hem lame and the skirt lames' ends, and the belt buckle.

Symbols:
    .           transparent
    O D M L H   the plate: outline, dark, mid, mid-light, light (the ramp's "dark", "mid", "mid_light", "light"; the
                outline is its "void" taken down to an item icon's outline depth, OUTLINE_LUMA)
    F Y g       the gold trim (gold on steel, brass on bronze): dark, light, outline
    R r         fittings (finial, horn tips, rivets): brass on bronze (light, dark); on steel the plate's own light
                and mid, since the steel has no brass there (knight_armor.py, the bronze variant): put R where the
                steel plate is lit and r where it is mid
    Q           the belt's centre: a brass buckle on bronze, the strap's lighter middle on steel (as the owner drew it)
    w k b B     leather: outline, strap, plate, top edge ("leather_darkest", "leather_dark", "leather_mid",
                "leather_light")
    u U         the dark under-layer: fill, top edge ("under_dark", "under_light")
"""
import os

from PIL import Image

import armor_paint

FOLDER = os.path.join(os.path.dirname(os.path.abspath(__file__)), "armor_icons")
SIZE = 16
PIECES = ("helmet", "chestplate", "leggings", "boots")
METALS = {"steel": armor_paint.STEEL, "bronze": armor_paint.BRONZE}
# An item icon's outline sits at about this luma (the owner's outlines measure about 35 to 42; the item-icon rules in
# review on the arms-icons branch, docs/ITEM_ICONS.md, keep it at 45 or less). The knight's void (steel 68, bronze 56)
# is lighter, so the outline is the void carried down to it, keeping its hue.
OUTLINE_LUMA = 40

# symbol -> palette tone name, the same for both metals
TONES = {"D": "dark", "M": "mid", "L": "mid_light", "H": "light", "F": "gold_dark", "Y": "gold_light",
         "k": "leather_dark", "b": "leather_mid", "B": "leather_light", "w": "leather_darkest",
         "u": "under_dark", "U": "under_light"}
# symbol -> tone, per metal: the bronze's brass fittings are its own plate on the steel
FITTINGS = {"steel": {"R": "light", "r": "mid", "Q": "leather_mid"},
            "bronze": {"R": "gold_light", "r": "gold_dark", "Q": "gold_light"}}


def luma(colour):
    r, g, b = colour[:3]
    return 0.299 * r + 0.587 * g + 0.114 * b


def deepen(colour, target=OUTLINE_LUMA):
    """`colour` scaled down to luma `target` (its hue kept)."""
    k = min(1.0, target / max(1.0, luma(colour)))
    return tuple(int(round(c * k)) for c in colour[:3])


def palette(metal):
    """Symbol -> RGBA for `metal` ("steel" or "bronze")."""
    tones = METALS[metal]
    colours = {symbol: tones[name] for symbol, name in {**TONES, **FITTINGS[metal]}.items()}
    colours["O"] = deepen(tones["void"])
    colours["g"] = deepen(tones["gold_dark"])
    return {symbol: tuple(colour) + (255,) for symbol, colour in colours.items()}


def path(piece):
    return os.path.join(FOLDER, piece + ".txt")


def load(piece):
    """The map's 16 rows (comment lines, starting with #, are skipped)."""
    with open(path(piece), encoding="utf-8") as f:
        rows = [line.rstrip("\n") for line in f if line.strip() and not line.startswith("#")]
    if len(rows) != SIZE or any(len(row) != SIZE for row in rows):
        raise ValueError(f"{path(piece)}: a map is {SIZE} rows of {SIZE} symbols, not {[len(r) for r in rows]}")
    return rows


def icon(metal, piece):
    """The 16x16 icon of `metal`_`piece` (an RGBA image)."""
    colours = palette(metal)
    img = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))
    for y, row in enumerate(load(piece)):
        for x, symbol in enumerate(row):
            if symbol == ".":
                continue
            if symbol not in colours:
                raise ValueError(f"{path(piece)}: unknown symbol {symbol!r} at ({x}, {y})")
            img.putpixel((x, y), colours[symbol])
    return img

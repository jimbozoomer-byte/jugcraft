"""Generated assets for the Thanksgiving side dishes (tools/thanksgiving_dishes.py; docs/features/thanksgiving-dishes.md).

Every dish is sculpted as docs/FOOD_MODELS.md asks, through the very pipeline the menu's remodelled meals use
(tools/menu_meal_models.py for the dishware, the piece helpers and the writer that closes the model, drops buried faces
and keeps faces off each other's planes; tools/menu_meal_textures.py for the two textures each dish wears, <dish>_top
drawn from the model's own pieces and <dish>_side with each material's rows). The dishes are listed here, in the same
two forms as those modules' MEALS, and lent to them only while one of these dishes is being built (`lent`), so the
menu's own MEALS never see them.

Colours: from the owner's own drawings in art/owner-library/originals/Blocks/farming and food textures (read only, never
changed): carrot_crate_top.png (carrot), potato_crate_top.png (potato), beetroot_crate_top.png (beetroot),
onion_crate_top.png (onion), brown_mushroom_colony_stage3.png (mushroom), apple_pie_inner.png (apple filling and
cranberry), cornbread.png (corn cakes, crumble and stuffing) and bowl.png (the woods, as tools/menu_meal_textures.py).
The rest (the glaze, butter, cream, browned cheese, herbs and turnip) are written out here beside the menu's own
(tools/menu_meal_textures.py steak_and_potatoes butter and parsley). All written out as hex.

Writes, from agriculture_data.py (assets, loot, recipes) and crop_textures.py (textures), or on its own
(`python3 tools/thanksgiving_dishes_data.py write`, the same functions): each dish's block model, blockstate, item model
and item, self-dropping loot, recipe and two textures.
"""
import contextlib
import json
import sys
from pathlib import Path

import menu_meal_models as mm
import menu_meal_textures as mt
import thanksgiving_dishes as td
from menu_meal_models import P, strand

MOD = "jugcraft"
FACINGS = {"north": 0, "east": 90, "south": 180, "west": 270}
OPPOSITE = {"north": "south", "south": "north", "east": "west", "west": "east"}
# A dish held, shown and framed as the menu's sandwiches are (a dish model, not a flat icon).
DISPLAY = {"gui": {"rotation": [30, 225, 0], "translation": [0, 2, 0], "scale": [0.8, 0.8, 0.8]},
           "ground": {"rotation": [0, 0, 0], "translation": [0, 3, 0], "scale": [0.5, 0.5, 0.5]},
           "fixed": {"rotation": [90, 0, 0], "translation": [0, 0, -2], "scale": [0.8, 0.8, 0.8]},
           "thirdperson_righthand": {"rotation": [75, 45, 0], "translation": [0, 2.5, 0], "scale": [0.4, 0.4, 0.4]},
           "firstperson_righthand": {"rotation": [0, 45, 0], "translation": [0, 3, 0], "scale": [0.45, 0.45, 0.45]},
           "firstperson_lefthand": {"rotation": [0, 225, 0], "translation": [0, 3, 0], "scale": [0.45, 0.45, 0.45]}}


def rid(path):
    return path if ":" in path else f"{MOD}:{path}"


def turned_y(lo, hi, mat, angle, **options):
    """A piece turned `angle` degrees about the middle of its foot (y), so it lies askew on whatever it rests on."""
    c = ((lo[0] + hi[0]) / 2, lo[1], (lo[2] + hi[2]) / 2)
    return P(lo, hi, mat, rot=("y", angle, c), **options)


def crossed(x, z, y, d, h, mat, angle=None):
    """A round piece `d` across and `h` high at (x, z) (its low corner) on y: two boxes crossed, each a quarter of its
    width shorter, so it reads round from above; `angle` turns both about its middle."""
    c = d / 4
    a = P((x, y, z + c), (x + d, y + h, z + d - c), mat)
    b = P((x + c, y, z), (x + d - c, y + h, z + d), mat)
    if angle:
        mid = (x + d / 2, y, z + d / 2)
        a, b = dict(a, rot=("y", angle, mid)), dict(b, rot=("y", angle, mid))
    return [a, b]


def baking_dish(side="#plate_side", top="#plate"):
    """A ceramic baking dish in the glazed plate's cream and blue (tools/menu_meal_textures.py meal_glazed_*), built as
    menu_meal_models.plate is: a foot, a floor 12 by 10 whose top is at y 1, and walls three pixels high round it (their
    tops at y 3.5) lined inside, its four corners stepped in so it reads as a rounded oblong. Its contents start on the
    floor (a dish's `bed`)."""
    box = mm.dish_box
    out = [box((3, 0, 4), (13, 0.5, 12), side, mm.SIDES + ("down",), row=mm.FOOT),
           box((2, 0.5, 3), (14, 1.0, 13), side, mm.FACES, top=top, row=mm.LIP + 1)]
    for lo, hi, inner in (((2, 0.5, 2), (14, 3.5, 3), "south"), ((2, 0.5, 13), (14, 3.5, 14), "north"),
                          ((1, 0.5, 3), (2, 3.5, 13), "east"), ((14, 0.5, 3), (15, 3.5, 13), "west")):
        out.append(box(lo, hi, side, mm.FACES, inner=(inner,)))
    return out


# ---------------------------------------------------------------- 1. Glazed Carrots

def glazed_carrots():
    """Carrot batons heaped crosswise on the wooden plate in three layers, the top one standing proud, glistening in a
    pool of butter-and-sugar glaze; round carrot coins in front; thyme leaves on the top baton and a coin."""
    def baton(x0, x1, z, y, angle):
        return turned_y((x0, y, z), (x1, y + 1.0, z + 1.0), "carrot", angle)
    return [
        P((4, 1.5, 4.5), (12, 1.7, 11), "glaze"),
        baton(4.5, 10.5, 5.0, 1.7, 22.5), baton(5.5, 11.5, 6.75, 1.7, -22.5), baton(4.5, 10.0, 8.5, 1.7, 45),
        baton(6.0, 11.0, 9.25, 1.7, -45),
        baton(5.5, 10.5, 6.0, 2.7, -45), baton(6.0, 11.0, 7.75, 2.7, 22.5),
        baton(6.5, 10.0, 6.75, 3.7, 45),
        *crossed(3.75, 11.0, 1.5, 2.0, 0.6, "coin"), *crossed(6.5, 11.25, 1.5, 2.0, 0.6, "coin"),
        *crossed(9.25, 10.75, 1.5, 2.0, 0.6, "coin"),
        P((7.75, 4.7, 7.0), (8.75, 4.9, 7.5), "thyme"), P((8.0, 4.7, 7.75), (8.5, 4.9, 8.5), "thyme"),
        P((7.0, 2.1, 12.0), (8.0, 2.3, 12.5), "thyme"),
    ]


# ---------------------------------------------------------------- 2. Roasted Root Vegetables

def roasted_root_vegetables():
    """Chunks of roast potato, carrot, beetroot, turnip and onion of every size heaped on the wooden plate, a few turned
    askew, a second layer resting on the first in the middle (the tallest a beet on the biggest potato), and sprigs of
    rosemary on the plate and on the onion."""
    def chunk(x, z, size, h, mat, y=1.5, angle=None):
        lo, hi = (x, y, z), (x + size, y + h, z + size)
        return turned_y(lo, hi, mat, angle) if angle else P(lo, hi, mat)
    return [
        chunk(3.5, 4.0, 2.5, 1.5, "potato"), chunk(6.25, 3.75, 2.0, 1.25, "carrot", angle=22.5), chunk(8.75, 4.0, 2.25, 1.5, "beet"),
        chunk(11.0, 4.5, 1.75, 1.25, "onion", angle=45),
        chunk(3.75, 6.75, 2.0, 1.25, "turnip"), chunk(6.0, 6.5, 2.5, 1.75, "potato"), chunk(9.0, 6.75, 2.0, 1.25, "carrot", angle=-22.5),
        chunk(11.0, 7.0, 1.75, 1.5, "beet"),
        chunk(4.0, 9.25, 2.25, 1.5, "beet", angle=22.5), chunk(6.75, 9.25, 2.0, 1.25, "onion"), chunk(9.0, 9.0, 2.5, 1.5, "potato"),
        chunk(11.25, 9.5, 1.5, 1.0, "turnip"),
        chunk(5.25, 11.25, 1.75, 1.0, "carrot"), chunk(8.0, 11.25, 2.0, 1.25, "turnip", angle=45),
        # The second layer, each on the chunk below its middle.
        chunk(6.5, 7.0, 1.75, 1.25, "beet", y=3.25), chunk(9.25, 4.25, 1.5, 1.0, "potato", y=3.0),
        chunk(4.0, 4.5, 1.5, 1.0, "carrot", y=3.0, angle=45), chunk(9.25, 9.25, 1.75, 1.0, "onion", y=3.0),
        # Rosemary.
        strand(3.75, 7.25, 12.5, 1.5, 22.5, "rosemary", width=0.6, height=0.4),
        strand(9.5, 10.75, 9.85, 4.0, 45, "rosemary", width=0.5, height=0.3),
    ]


# ---------------------------------------------------------------- 3. Scalloped Potatoes

def scalloped_potatoes():
    """Scalloped potatoes baked in the ceramic baking dish: a bed of cream filling it, and over it three rows of round
    potato slices shingled along the dish, each slice tipped a sixteenth of a turn so its edge rests on the next, their
    rims browned; patches of browned, bubbling cheese on some of them (another material), thyme on the cream."""
    def round_slice(x, z, y, mat):
        mid = (x + 1.5, y, z + 1.5)
        return [dict(p, rot=("z", -22.5, mid)) for p in crossed(x, z, y, 3.0, 0.5, mat)]
    out = []
    for row, z in enumerate((3.25, 6.25, 9.25)):
        for i, x in enumerate((2.5, 5.0, 7.5, 10.0)):
            out += round_slice(x, z, 2.25 + 0.25 * (i % 2), "cheesy" if (row + i) % 3 == 0 else "slice")
    out += [P((12.75, 2.25, 4.0), (13.75, 2.5, 4.5), "thyme"), P((12.75, 2.25, 12.0), (13.75, 2.5, 12.5), "thyme")]
    return out


# ---------------------------------------------------------------- 4. Apple Cranberry Crisp

def apple_cranberry_crisp():
    """An apple and cranberry crisp baked in the ceramic baking dish: the fruit filling the dish, a golden-brown crumble
    over it in deliberate clumps two or three pixels across at three heights, the fruit left showing along the front
    edge with slices of apple and red cranberries in it, and a round scoop of cream on the highest clump."""
    def clump(x, z, w, d, top, mat="crumb"):
        return P((x, 2.5, z), (x + w, top, z + d), mat)
    return [
        clump(2.0, 3.0, 2.75, 2.5, 3.25), clump(4.75, 3.0, 2.5, 2.75, 3.5, "crumb_dark"), clump(7.25, 3.0, 3.0, 2.5, 3.25),
        clump(10.25, 3.0, 2.25, 2.75, 3.5, "crumb_dark"), clump(12.5, 3.0, 1.5, 2.5, 3.25),
        clump(2.0, 5.5, 2.5, 2.75, 3.5, "crumb_dark"), clump(4.5, 5.75, 3.0, 2.5, 3.25), clump(7.5, 5.5, 2.5, 3.0, 3.75, "crumb_light"),
        clump(10.0, 5.75, 2.75, 2.5, 3.25), clump(12.75, 5.5, 1.25, 2.75, 3.5, "crumb_dark"),
        clump(2.0, 8.25, 3.0, 2.5, 3.25), clump(5.0, 8.25, 2.5, 2.5, 3.5, "crumb_dark"), clump(7.5, 8.5, 3.0, 2.25, 3.25),
        clump(10.5, 8.25, 2.5, 2.5, 3.5, "crumb_dark"), clump(13.0, 8.25, 1.0, 2.5, 3.25),
        # The second layer of crumble, on the first.
        clump(5.0, 4.25, 2.0, 2.0, 4.0, "crumb_light"), clump(8.0, 6.0, 2.0, 2.0, 4.25, "crumb_light"), clump(10.5, 8.75, 2.0, 1.75, 4.0, "crumb_light"),
        # Along the front edge the fruit shows: apple slices and cranberries in the filling.
        P((2.5, 2.5, 11.0), (4.75, 3.0, 12.0), "apple"), P((7.0, 2.5, 11.25), (9.25, 3.0, 12.25), "apple"),
        P((11.0, 2.5, 11.0), (13.0, 3.0, 12.0), "apple"),
        P((5.25, 2.5, 11.25), (6.25, 3.25, 12.25), "berry"), P((9.75, 2.5, 11.5), (10.75, 3.25, 12.5), "berry"),
        P((3.25, 2.5, 12.0), (4.25, 3.25, 12.75), "berry"), P((12.75, 2.5, 10.75), (13.75, 3.25, 11.75), "berry"),
        # A scoop of cream: a round of it with a smaller round on top.
        *crossed(7.75, 6.25, 4.25, 2.5, 0.75, "cream"), *crossed(8.25, 6.75, 5.0, 1.5, 0.4, "cream"),
    ]


# ---------------------------------------------------------------- 5. Stuffed Mushrooms

def stuffed_mushrooms():
    """Five brown mushroom caps on the glazed plate, three at the back and two in front, the middle one at the back
    bigger and standing proud; each cap filled with a mound of golden crumb stuffing flecked with herbs; two sprigs of
    parsley in front."""
    def cap(x, z, d, h, stuffing_h):
        out = crossed(x, z, 1.5, d, h, "cap")
        s = d / 4 + 0.25
        out.append(P((x + s, 1.5 + h, z + s), (x + d - s, 1.5 + h + stuffing_h, z + d - s), "stuffing"))
        return out
    return [
        *cap(3.25, 3.5, 3.0, 1.0, 0.6), *cap(6.25, 3.25, 3.5, 1.25, 0.8), *cap(9.75, 3.5, 3.0, 1.0, 0.6),
        *cap(4.75, 7.0, 3.0, 1.0, 0.6), *cap(8.25, 7.0, 3.0, 1.0, 0.6),
        strand(4.0, 8.0, 11.25, 1.5, 22.5, "parsley", width=0.75, height=0.4),
        strand(8.25, 11.75, 11.5, 1.5, -22.5, "parsley", width=0.75, height=0.4),
    ]


# ---------------------------------------------------------------- 6. Skillet Corn Cakes

def skillet_corn_cakes():
    """Three corn cakes fried golden in the owner's Skillet: two side by side in a little melted butter, round, four
    and a half pixels across and a pixel thick, their rims browned, and the third stacked on the first, turned a
    sixteenth of a turn; a pat of butter melting on top."""
    def cake(x, z, y, mat, angle=None):
        return crossed(x, z, y, 4.5, 1.0, mat, angle)
    # Each cake its own material, so the drawing of the top gives each its own browned rim where two touch.
    return [
        *cake(4.0, 5.0, 1.25, "cake"), *cake(7.5, 8.5, 1.25, "cake_b"),
        *cake(4.5, 5.5, 2.25, "cake", angle=22.5),
        P((6.0, 3.25, 7.0), (7.5, 3.75, 8.5), "butter"),
    ]


# The dishes in tools/menu_meal_models.py MEALS's form.
MODELS = {
    "glazed_carrots": {"dish": "wood_plate", "food": glazed_carrots},
    "roasted_root_vegetables": {"dish": "wood_plate", "food": roasted_root_vegetables},
    "scalloped_potatoes": {"dish": "baking_dish", "food": scalloped_potatoes, "bed": ((2, 1.0, 3), (14, 2.25, 13), "cream")},
    "apple_cranberry_crisp": {"dish": "baking_dish", "food": apple_cranberry_crisp, "bed": ((2, 1.0, 3), (14, 2.5, 13), "filling")},
    "stuffed_mushrooms": {"dish": "glazed_plate", "food": stuffed_mushrooms},
    "skillet_corn_cakes": {"dish": "skillet", "food": skillet_corn_cakes, "bed": ((4, 1, 5), (12, 1.25, 13), "fat")},
}


# ---------------------------------------------------------------- textures, in tools/menu_meal_textures.py MEALS's form

def checks(a, b, period=2):
    """Two tones by turns, a pattern of square cells `period` texels wide (crumb, kernels)."""
    return lambda x, z: a if ((x // period) + (z // period)) % 2 == 0 else b


TEXTURES = {
    # carrot_crate_top.png's carrot; the amber glaze and thyme written out.
    "glazed_carrots": {
        "palette": {"_": "714f0f", "d": "752802", "c": "ac3900", "C": "d36a0d", "L": "e8800c", "k": "9a5a08", "h": "c07a10",
                    "H": "e0a024", "w": "f2c650", "t": "3c5a20", "g": "557a2c", "G": "74983a"},
        "materials": {
            "glaze": {"base": "h", "lit": "H", "shade": "k", "side": ["khkkhkkkhkkhkkkh"]},
            "carrot": {"tile": lambda x, z: "C" if (x + 2 * z) % 5 else "c", "lit": "L", "shade": "d",
                       "side": ["LCLLCLLLCLLCLLLC"], "swatch": ["LC", "Cc"]},
            "coin": {"tile": lambda x, z: "C" if (x + z) % 2 else "L", "lit": "L", "shade": "c", "side": ["cdccdcccdccdcccd"]},
            "thyme": {"base": "g", "lit": "G", "shade": "t", "side": ["tgttgttgttgttgtt"]},
        },
        "details": ["", "", "", "", "", "    w", "", "          w", "", "   w", "", "", ""],
    },
    # potato, carrot, beetroot and onion from the owner's crate tops; turnip, rosemary and the roast's char written out.
    "roasted_root_vegetables": {
        "palette": {"_": "714f0f", "s": "9a5500", "S": "9d772e", "i": "c8973a", "Y": "e9ba62", "x": "5a3410",
                    "d": "752802", "c": "ac3900", "C": "d36a0d", "L": "e8800c",
                    "b": "5b1d17", "B": "8f2d2f", "p": "b6484c",
                    "u": "b4a68a", "U": "d8ccb0", "W": "ece2c8", "v": "7a3a6a",
                    "o": "6d4a20", "O": "ac732f", "q": "c58b46",
                    "r": "2e5228", "R": "3e6a30", "e": "5a8a3e"},
        "materials": {
            "potato": {"tile": lambda x, z: "x" if (x + z) % 5 == 0 else "i", "lit": "Y", "shade": "s",
                       "side": ["YiYiiYiiYiiYiiYi", "SsSSxSSsSSxSSsSS"]},
            "carrot": {"base": "C", "lit": "L", "shade": "d", "side": ["LCLLCLLLCLLCLLLC", "dcddcdddcddcdddc"], "swatch": ["LC", "Cc"]},
            "beet": {"base": "B", "lit": "p", "shade": "b", "side": ["pBppBpppBppBpppB", "bBbbBbbbBbbBbbbB"], "swatch": ["pB", "Bb"]},
            "turnip": {"base": "U", "lit": "W", "shade": "u", "side": ["vUvvUvvvUvvUvvvU", "uUuuUuuuUuuUuuuU"], "swatch": ["WU", "Uu"]},
            "onion": {"tile": lambda x, z: "O" if (x + z) % 2 else "q", "lit": "q", "shade": "o", "side": ["qOqqOqqqOqqOqqqO"],
                      "swatch": ["qO", "Oo"]},
            "rosemary": {"base": "R", "lit": "e", "shade": "r", "side": ["rRrrRrrRrrRrrrRr"], "swatch": ["eR", "Rr"]},
        },
        "details": [],
    },
    # potato_crate_top.png's potato for the slices, browned at the rim; the cream, the browned cheese and thyme written out.
    "scalloped_potatoes": {
        "palette": {"_": "e8dcbc", "w": "d8caa4", "W": "eadfc2", "v": "c4b48a", "s": "9a5500", "S": "9d772e", "i": "c8973a",
                    "I": "dcae52", "p": "f0d488", "P": "e6c26a", "b": "a8641c", "B": "c8862e", "t": "3c5a20", "g": "557a2c",
                    "G": "74983a"},
        "materials": {
            "cream": {"base": "w", "lit": "W", "shade": "v", "side": ["wWwwvwwwWwwvwwwW"]},
            "slice": {"side": ["SIIIIIIIIIIIIIIS"], "swatch": ["SIIS", "IpPI", "IPpI", "SIIS"]},
            "cheesy": {"side": ["SIBIIIIBIIIIBIIS"], "swatch": ["SBBS", "BbBI", "IBpI", "SIIS"]},
            "thyme": {"base": "g", "lit": "G", "shade": "t", "side": ["tgttgttgttgttgtt"]},
        },
        "details": [],
    },
    # apple_pie_inner.png's apple, crust and cranberry; cornbread.png's golds and toasted browns for the crumble, in
    # three tones of clump; the cream written out.
    "apple_cranberry_crisp": {
        "palette": {"_": "c5853e", "a": "b96913", "A": "d59f4b", "E": "eab563", "R": "941e33", "P": "c14836", "d": "8a5418",
                    "c": "a8702a", "C": "c88a34", "g": "d8a246", "G": "eab563", "w": "e2d6be", "W": "eae0cc", "v": "c8b898"},
        "materials": {
            "filling": {"base": "a", "lit": "A", "shade": "R", "side": ["aRaaAaRaaAaaRaAa", "RaaRaaaRaaRaaaRa"]},
            "crumb": {"base": "C", "lit": "g", "shade": "c", "side": ["gCCgCCgCCgCCgCCg"]},
            "crumb_dark": {"base": "c", "lit": "C", "shade": "d", "side": ["CccCccCccCccCccC"]},
            "crumb_light": {"base": "g", "lit": "G", "shade": "C", "side": ["GggGggGggGggGggG"]},
            "apple": {"base": "E", "lit": "E", "shade": "A", "side": ["AEEEEEEEEEEEEEEA"]},
            "berry": {"base": "P", "lit": "P", "shade": "R", "side": ["RPPRRPPRRPPRRPPR"]},
            "cream": {"base": "w", "lit": "W", "shade": "v", "side": ["WwwwWwwwWwwwWwww"]},
        },
        "details": [],
    },
    # brown_mushroom_colony_stage3.png's caps and stems; the stuffing in cornbread.png's golds with the menu's parsley.
    "stuffed_mushrooms": {
        "palette": {"_": "ebe5d7", "m": "6a4e3b", "M": "916d55", "n": "bb8967", "N": "cc9978", "s": "ccae9b",
                    "c": "a25c00", "C": "d47900", "g": "e0a83a", "G": "f0c460", "h": "3c713e", "H": "4d8c3b", "J": "7ab244"},
        "materials": {
            "cap": {"base": "M", "lit": "N", "shade": "m", "side": ["NnNNnNNNnNNnNNNn", "mMmmMmmmMmmMmmmM"]},
            "stuffing": {"tile": lambda x, z: "H" if (x * 2 + z) % 5 == 0 else "g" if (x + z) % 2 else "C",
                         "lit": "G", "shade": "c", "side": ["gCggCgggCggCgggC"]},
            "parsley": {"base": "H", "lit": "J", "shade": "h", "side": ["hHhhHhhHhhHhhhHh"], "swatch": ["JH", "Hh"]},
        },
        "details": [],
    },
    # cornbread.png's golds and toasted browns for the cakes; butter as the menu's.
    "skillet_corn_cakes": {
        "palette": {"_": "a25c00", "f": "6a4208", "F": "8a5a10", "a": "8a4e08", "b": "a86410", "d": "d48a1e", "e": "e8a630",
                    "g": "f2c050", "y": "cca844", "Y": "e4c662", "Z": "f2e092"},
        "materials": {
            "fat": {"base": "F", "lit": "F", "shade": "f", "side": ["fFffFfffFffFfffF"]},
            "cake": {"base": "e", "lit": "g", "shade": "b", "side": ["bdddddddddddddda", "abbbbbbbbbbbbbba"], "swatch": ["bddb", "dggd", "dged", "bddb"]},
            "cake_b": {"base": "d", "lit": "e", "shade": "a", "side": ["adddddddddddddda", "abbbbbbbbbbbbbba"]},
            "butter": {"base": "Y", "lit": "Z", "shade": "y", "side": ["yYyyYyyyYyyYyyyY"]},
        },
        "details": [],
    },
}


@contextlib.contextmanager
def lent():
    """The six dishes lent to the menu's meal modules while one is built, and taken back after, so those modules'
    own MEALS (and everything that walks them) never see them."""
    clash = (set(MODELS) & set(mm.MEALS)) | (set(TEXTURES) & set(mt.MEALS))
    assert not clash, f"already in the menu's meals: {sorted(clash)}"
    mm.MEALS.update(MODELS)
    mt.MEALS.update(TEXTURES)
    try:
        yield
    finally:
        for name in MODELS:
            mm.MEALS.pop(name, None)
        for name in TEXTURES:
            mt.MEALS.pop(name, None)


def pieces(dish):
    with lent():
        return mm.pieces(dish)


def block_model(dish):
    """The dish's block model: its dishware and food, closed, buried faces dropped, faces apart (menu_meal_models; the
    baking dish's dishware is built here, the rest exactly as menu_meal_models.textures_and_elements does)."""
    import model_writer
    with lent():
        if MODELS[dish]["dish"] == "baking_dish":
            elements = baking_dish() + mm.food_elements(dish, mm.pieces(dish))
            model_writer.finish_closed(elements)
            mm.drop_buried(elements)
            model_writer.finish_elements(elements)
            textures = {"food": f"{dish}_top", "food_side": f"{dish}_side", **mm.DISH_TEXTURES["glazed_plate"]}
        else:
            textures, elements = mm.textures_and_elements(dish)
    return {"parent": "minecraft:block/block", "ambientocclusion": False,
            "textures": {"particle": rid(f"block/{dish}_top"), **{key: rid(f"block/{value}") for key, value in textures.items()}},
            "elements": elements}


def blockstate(dish):
    """Faces whoever set it down: the model faces south, so each facing turns it from its opposite."""
    out = {}
    for f in FACINGS:
        y = FACINGS[OPPOSITE[f]]
        out[f"facing={f}"] = {"model": rid(f"block/{dish}"), **({"y": y} if y else {})}
    return {"variants": out}


def assets(root, write, lang):
    """Each dish's block model, blockstate, item model (the dish itself, as the menu's sandwiches show theirs) and item;
    and its names."""
    lang.update(td.names())
    for dish in td.DISHES:
        write(root / "models" / "block" / f"{dish}.json", block_model(dish))
        write(root / "blockstates" / f"{dish}.json", blockstate(dish))
        write(root / "models" / "item" / f"{dish}.json", {"parent": rid(f"block/{dish}"), "display": DISPLAY})
        write(root / "items" / f"{dish}.json", {"model": {"type": "minecraft:model", "model": rid(f"item/{dish}")}})


def loot(out, write):
    """A placed dish drops its food, once (tools/menu_data.py loot)."""
    for dish in td.DISHES:
        write(out / f"{dish}.json", {"type": "minecraft:block", "random_sequence": rid(f"blocks/{dish}"), "pools": [{
            "rolls": 1, "entries": [{"type": "minecraft:item", "name": rid(dish)}],
            "condition": {"type": "minecraft:survives_explosion"}}]})


def recipes(out, write, conditions):
    for dish, spec in td.DISHES.items():
        write(out / f"{dish}.json", {"fabric:load_conditions": conditions(), "type": "minecraft:crafting_shapeless",
                                     "category": "misc", "ingredients": spec["inputs"],
                                     "result": {"id": rid(dish), "count": 1}})


def tags(tags):
    for dish in td.DISHES:
        tags.add("item", "c:foods", rid(dish))


def textures():
    out = {}
    with lent():
        for dish in td.DISHES:
            out[("block", f"{dish}_top")] = mt.top_image(dish)
            out[("block", f"{dish}_side")] = mt.side_image(dish)
    return out


# ---------------------------------------------------------------- on its own

def _conditions():
    return [{"condition": f"{MOD}:feature_enabled", "feature": td.FEATURE}]


def write_all(repo):
    """Writes every file of these dishes into the repository at `repo` (as generate_material_data.py's write() would)
    and returns the language entries (the coordinator merges them into en_us.json)."""
    import model_writer
    res = Path(repo) / "src" / "main" / "resources"
    assets_root, data_root = res / "assets" / MOD, res / "data" / MOD
    written = []

    def write(path, obj):
        if isinstance(obj, dict) and obj.get("elements") and "models" in path.parts:
            model_writer.finish_elements(obj["elements"])
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(json.dumps(obj, indent=2) + "\n", encoding="utf-8")
        written.append(path)
    lang = {}
    assets(assets_root, write, lang)
    loot(data_root / "loot_table" / "blocks", write)
    recipes(data_root / "recipe", write, _conditions)
    for (kind, name), img in textures().items():
        path = assets_root / "textures" / kind / f"{name}.png"
        path.parent.mkdir(parents=True, exist_ok=True)
        img.save(path, optimize=True)
        written.append(path)
    return written, lang


if __name__ == "__main__":
    if sys.argv[1:] == ["write"]:
        repo = Path(__file__).resolve().parent.parent
        files, lang = write_all(repo)
        print(f"wrote {len(files)} files")
        print(json.dumps(lang, indent=2, sort_keys=True))
    else:
        print("usage: python3 tools/thanksgiving_dishes_data.py write")

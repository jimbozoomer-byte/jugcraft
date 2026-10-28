"""The Pumpkin Patch Market, decor set 1 (docs/features/pumpkin-patch-market.md), part 1: six new squashes and gourds for
the fall's displays and kitchens. Each grows from a stem like every gourd (tools/agriculture.py GOURDS, FOUND_WILD; the
Java in agriculture/JugcraftAgriculture.java), and is drawn here: its model's boxes, and the colours its skins are
painted in (tools/pumpkin_patch_textures.py, in the vanilla manner of docs/NATURAL_TEXTURES.md).

- Turban Squash: a squat red-orange squash wearing a striped green and cream "turban" on top.
- Blue Hubbard Squash: a big slate-blue squash lying on its side, tapered at both ends, its skin set with warts.
- Sweet Dumpling Squash: a small cream squash, deeply ribbed, striped dark green along its ribs.
- Long Island Cheese Pumpkin: a flat, buff tan wheel of a pumpkin with a thick corky stalk, the old pie pumpkin.
- Spoon Gourd: a round yellow bowl on a straight green handle, lying on its side.
- Swan Gourd: a speckled dark green bulb whose long neck arches over and down, like a swan's.

Models are drawn facing north (a lying gourd's stalk end points north), in pixels. Each box names the part it is: the
skin (the variety's side texture round it, its top texture over and under it), the cap (the turban: its own side
texture round it and its own top over and under it) or the stalk (the shared gourd stalk texture).
"""

# The varieties, in the order they are listed everywhere.
VARIETIES = ("turban_squash", "blue_hubbard_squash", "sweet_dumpling_squash", "long_island_cheese_pumpkin", "spoon_gourd",
             "swan_gourd")

# Each variety's boxes: (low corner, high corner, part). Every box is written low corner first; no two faces of different
# boxes lie in one plane facing the same way where they overlap (docs/ART_DIRECTION.md).
SHAPES = {
    # A wide red-orange base, its shoulder, the striped turban and its crown, and the dried blossom at its top.
    "turban_squash": [((2, 0, 2), (14, 6, 14), "skin"), ((3, 6, 3), (13, 7, 13), "skin"),
                      ((4.5, 7, 4.5), (11.5, 10, 11.5), "cap"), ((6, 10, 6), (10, 11, 10), "cap"),
                      ((7.5, 11, 7.5), (8.5, 11.5, 8.5), "stalk")],
    # Lying north to south: the body, a taper at each end, the hump of its back, and the stalk out of the north end.
    "blue_hubbard_squash": [((3, 0, 2.5), (13, 8, 13.5), "skin"), ((4.5, 1, 1), (11.5, 7, 2.5), "skin"),
                            ((4.5, 1, 13.5), (11.5, 7, 15), "skin"), ((5, 8, 5), (11, 9, 11), "skin"),
                            ((7.25, 3.5, 0), (8.75, 5, 1), "stalk")],
    # A small ribbed body, its rounded shoulder and the stalk.
    "sweet_dumpling_squash": [((4, 0, 4), (12, 5, 12), "skin"), ((4.75, 5, 4.75), (11.25, 6, 11.25), "skin"),
                              ((7.5, 6, 7.5), (8.5, 7.5, 8.5), "stalk")],
    # A flat wheel, its shoulder and a thick corky stalk.
    "long_island_cheese_pumpkin": [((1, 0, 1), (15, 6, 15), "skin"), ((2.5, 6, 2.5), (13.5, 7, 13.5), "skin"),
                                   ((7, 7, 7), (9, 9, 9), "stalk")],
    # Lying north to south: the bowl and its top, the handle and its tip, and the stalk at the handle's end.
    "spoon_gourd": [((5, 0, 8.5), (11, 6, 14.5), "skin"), ((6, 6, 9.5), (10, 7, 13.5), "skin"),
                    ((7, 0.5, 3), (9, 3, 8.5), "skin"), ((7.25, 1, 1.5), (8.75, 2.5, 3), "skin"),
                    ((7.6, 1.3, 0.5), (8.4, 2.1, 1.5), "stalk")],
    # The bulb and its top, the neck rising from it, the arch, the head bowed to the north, and the stalk at its tip.
    "swan_gourd": [((4, 0, 7), (12, 7, 15), "skin"), ((5, 7, 8), (11, 8, 14), "skin"),
                   ((7, 8, 9.5), (9, 12, 11.5), "skin"), ((7, 12, 6.5), (9, 14, 11), "skin"),
                   ((7.25, 10, 5), (8.75, 13, 7), "skin"), ((7.6, 9, 4.75), (8.4, 10, 5.25), "stalk")],
}

# Each variety's skin, for tools/pumpkin_patch_textures.py: its colour (one colour stepped into six tones by lightness,
# as the woods are), the colour of its second marks, and how it is marked.
#   ribs: a darker groove every this many texels across a side (the grooves run up and down, as a pumpkin's do);
#   stripes: the second colour along each groove (Sweet Dumpling) or round the turban;
#   warts: Blue Hubbard's raised warts, set on an even, staggered lattice;
#   specks: Swan Gourd's pale specks, on a staggered lattice;
#   split: the row of the side texture below which the second colour takes over (Spoon Gourd's green underside).
SKINS = {
    "turban_squash": {"base": "c8502a", "second": "5f8a3a", "ribs": 4},
    "blue_hubbard_squash": {"base": "6f8796", "second": "8ea6b1", "ribs": 0, "warts": True},
    "sweet_dumpling_squash": {"base": "e3d6ae", "second": "3f6a34", "ribs": 4, "stripes": True},
    "long_island_cheese_pumpkin": {"base": "d9b37e", "second": "b98e58", "ribs": 4},
    "spoon_gourd": {"base": "e6c23c", "second": "5d8f36", "ribs": 0, "split": 13},
    "swan_gourd": {"base": "3d6a33", "second": "b9cf8a", "ribs": 0, "specks": True},
}
# The turban's cap: cream ground, broad green stripes and a band of the squash's own red-orange round its foot.
TURBAN_CAP = {"base": "e2d8b0", "stripe": "5f8a3a", "band": "c8502a"}

# Each variety's seeds icon (tools/item_icons/<variety>_seeds.txt) and the material its seeds are drawn in
# (tools/icon_materials.py), told apart as vanilla's pumpkin and melon seeds are: by their colour.
SEED_MATERIALS = {"turban_squash": "stem", "blue_hubbard_squash": "old_bone", "sweet_dumpling_squash": "paper",
                  "long_island_cheese_pumpkin": "canvas", "spoon_gourd": "straw", "swan_gourd": "horn"}


def textures(variety):
    """The textures `variety`'s model uses: side, top and, for the turban, the cap's sides and top."""
    out = {"side": f"block/{variety}_side", "top": f"block/{variety}_top"}
    if any(part == "cap" for *_, part in SHAPES[variety]):
        out["cap"], out["cap_top"] = f"block/{variety}_cap", f"block/{variety}_cap_top"
    return out

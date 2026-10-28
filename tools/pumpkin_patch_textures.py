"""The Pumpkin Patch Market's squashes and gourds (tools/pumpkin_patch.py): their skins and their seeds icons.

Called from crop_textures.crop_textures(). Every skin is drawn here by code from fixed seeds in vanilla's manner
(docs/NATURAL_TEXTURES.md): 16x16; one colour stepped into six tones by lightness (wood_style.ramp), most of a face in the
middle tones, as small clumps (block_style.surface) rather than a random shade at every texel; marks that follow how the
fruit grows (grooves running up and down every four texels, so a side tiles round its model), and the raised warts and
specks on an even, staggered lattice instead of scattered; no outline and no gradient. A model samples its textures by
position (tools/festival_data.py patch_gourd_model), so each is drawn where its model shows it. The seeds icons are
16x16 maps (tools/item_icons/, docs/ITEM_ICONS.md). No Mojang texture is read, traced or recoloured.
"""
import math

from crop_textures import Canvas, rgb
import block_style as bs
import item_icons
import pumpkin_patch
from wood_style import ramp


def tones(hex_colour):
    """Six tones of one colour, darkest first."""
    return ramp(rgb(hex_colour))


def ground(c, palette, seed, rows=range(16)):
    """The skin's ground: its middle tones in small clumps (the second and third of the four used most)."""
    surface = bs.surface(palette[1:5], seed, weights=[1, 4, 4, 1], spread=0.7)
    for y in rows:
        for x in range(16):
            c.px(x, y, surface(x, y))


def grooves(c, palette, every, deep=True, rows=range(16)):
    """A groove every `every` texels, running up and down, with a lit ridge on its right (light from the top left)."""
    for y in rows:
        for x in range(0, 16, every):
            c.px(x, y, palette[1 if deep else 2])
            c.px(x + 1, y, palette[4])


def radial(c, palette, seed, groove, ridge=None, scar=None):
    """A top: the ground, eight grooves running out from the middle to the edges (as a pumpkin's ribs meet at its stalk),
    and the stalk's scar in the middle."""
    ground(c, palette, seed)
    for k in range(8):
        angle = k * math.pi / 4
        for r in range(2, 11):
            x, y = 7.5 + math.cos(angle) * r, 7.5 + math.sin(angle) * r
            c.px(x, y, groove)
            if ridge is not None and r > 3:
                c.px(x + (1 if abs(math.sin(angle)) > 0.5 else 0), y + (1 if abs(math.cos(angle)) > 0.5 else 0), ridge)
    if scar:
        for y in range(6, 10):
            for x in range(6, 10):
                c.px(x, y, scar[1] if (x in (6, 9) or y in (6, 9)) else scar[2])
        c.px(7, 7, scar[3])


def lattice(c, every, mark):
    """`mark(x, y)` at every point of an even lattice `every` texels apart, each row of it set half a step over from the
    one above (staggered), so the pattern tiles across a face's edges."""
    for row, y in enumerate(range(1, 16, every)):
        for x in range(1 + (every // 2 if row % 2 else 0), 16, every):
            mark(x, y)


STALK = tones("8a7444")


def turban_side():
    """The turban squash's red-orange base: ribbed, with short green streaks running down from its shoulder along every
    other groove (the turban's colours bleeding into the base, as they do)."""
    skin = pumpkin_patch.SKINS["turban_squash"]
    p, green = tones(skin["base"]), tones(skin["second"])
    c = Canvas()
    ground(c, p, 31)
    grooves(c, p, skin["ribs"])
    for x in range(2, 16, 8):
        for y in range(10, 13):
            c.px(x, y, green[2] if y < 12 else green[3])
    return c.img


def turban_top():
    p = tones(pumpkin_patch.SKINS["turban_squash"]["base"])
    c = Canvas()
    radial(c, p, 32, p[1], p[4], STALK)
    return c.img


def turban_cap():
    """The turban's sides: cream, with broad green stripes every four texels, and a band of the base's red-orange round
    its foot (the knob's lowest row, y 7 to 8 of the model: row 8 of the texture)."""
    cap = pumpkin_patch.TURBAN_CAP
    cream, green, band = tones(cap["base"]), tones(cap["stripe"]), tones(cap["band"])
    c = Canvas()
    ground(c, cream, 33)
    for y in range(16):
        for x in range(0, 16, 4):
            c.px(x + 1, y, green[2])
            c.px(x + 2, y, green[3])
    for x in range(16):
        c.px(x, 8, band[3] if x % 4 else band[2])
        c.px(x, 9, band[2] if x % 4 else band[1])
    return c.img


def turban_cap_top():
    """The turban's crown from above: cream, the green stripes running out from the blossom in the middle."""
    cap = pumpkin_patch.TURBAN_CAP
    cream, green = tones(cap["base"]), tones(cap["stripe"])
    c = Canvas()
    radial(c, cream, 34, green[2], green[3], STALK)
    return c.img


def hubbard(seed):
    """Blue Hubbard: slate blue, set with raised warts on a staggered lattice eight texels apart (sparse, as a hubbard's
    are), each lit at its top left with its shadow one tone under the ground."""
    skin = pumpkin_patch.SKINS["blue_hubbard_squash"]
    p, pale = tones(skin["base"]), tones(skin["second"])
    c = Canvas()
    ground(c, p, seed)

    def wart(x, y):
        c.px(x, y, pale[5])
        c.px(x + 1, y, pale[4])
        c.px(x, y + 1, pale[4])
        c.px(x + 1, y + 1, p[1])
    lattice(c, 8, wart)
    return c.img


def dumpling_side():
    """Sweet Dumpling: cream, each groove a dark green stripe two texels wide."""
    skin = pumpkin_patch.SKINS["sweet_dumpling_squash"]
    p, green = tones(skin["base"]), tones(skin["second"])
    c = Canvas()
    ground(c, p, 41)
    for y in range(16):
        for x in range(0, 16, skin["ribs"]):
            c.px(x, y, green[1])
            c.px(x + 1, y, green[3])
            c.px(x + 2, y, p[4])
    return c.img


def dumpling_top():
    skin = pumpkin_patch.SKINS["sweet_dumpling_squash"]
    p, green = tones(skin["base"]), tones(skin["second"])
    c = Canvas()
    radial(c, p, 42, green[1], green[3], STALK)
    return c.img


def cheese_side():
    """Long Island Cheese: buff, ribbed like a wheel of cheese."""
    skin = pumpkin_patch.SKINS["long_island_cheese_pumpkin"]
    p = tones(skin["base"])
    c = Canvas()
    ground(c, p, 51)
    grooves(c, p, skin["ribs"])
    return c.img


def cheese_top():
    p = tones(pumpkin_patch.SKINS["long_island_cheese_pumpkin"]["base"])
    c = Canvas()
    radial(c, p, 52, p[2], p[4], STALK)
    return c.img


def spoon_side():
    """Spoon Gourd: a yellow bowl over a green underside. Below row `split` the green takes over, the line between them
    scalloped every two texels (the handle, lower on the model, is all green)."""
    skin = pumpkin_patch.SKINS["spoon_gourd"]
    yellow, green = tones(skin["base"]), tones(skin["second"])
    split = skin["split"]
    c = Canvas()
    ground(c, yellow, 61, rows=range(0, split))
    ground(c, green, 62, rows=range(split, 16))
    for x in range(0, 16, 2):
        c.px(x, split - 1, green[3])
    return c.img


def spoon_top():
    yellow = tones(pumpkin_patch.SKINS["spoon_gourd"]["base"])
    c = Canvas()
    ground(c, yellow, 63)
    return c.img


def swan(seed):
    """Swan Gourd: dark green, with pale specks on a staggered lattice."""
    skin = pumpkin_patch.SKINS["swan_gourd"]
    p, pale = tones(skin["base"]), tones(skin["second"])
    c = Canvas()
    ground(c, p, seed)
    lattice(c, 4, lambda x, y: c.px(x, y, pale[3]))
    return c.img


def pumpkin_patch_textures():
    out = {
        ("block", "turban_squash_side"): turban_side(),
        ("block", "turban_squash_top"): turban_top(),
        ("block", "turban_squash_cap"): turban_cap(),
        ("block", "turban_squash_cap_top"): turban_cap_top(),
        ("block", "blue_hubbard_squash_side"): hubbard(21),
        ("block", "blue_hubbard_squash_top"): hubbard(22),
        ("block", "sweet_dumpling_squash_side"): dumpling_side(),
        ("block", "sweet_dumpling_squash_top"): dumpling_top(),
        ("block", "long_island_cheese_pumpkin_side"): cheese_side(),
        ("block", "long_island_cheese_pumpkin_top"): cheese_top(),
        ("block", "spoon_gourd_side"): spoon_side(),
        ("block", "spoon_gourd_top"): spoon_top(),
        ("block", "swan_gourd_side"): swan(71),
        ("block", "swan_gourd_top"): swan(72),
    }
    for variety in pumpkin_patch.VARIETIES:
        out[("item", f"{variety}_seeds")] = item_icons.draw(f"{variety}_seeds")
    return out

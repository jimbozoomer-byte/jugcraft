"""Original textures for the Bat House (fall additions 13) (requires Pillow): weathered dark planks, the slatted front
with a bat painted on it, the guano piled on its tray, and the Bat Guano item.

Called from crop_textures.crop_textures(). Every pixel is drawn here by code, from fixed seeds; no Mojang texture is
read, traced or recoloured.
"""
import random

from crop_textures import Canvas, rgb, outline
from decor_textures import noise

WOOD = [rgb("2e2218"), rgb("3e2e20"), rgb("4e3a28"), rgb("5e4632")]
GUANO = [rgb("2a2420"), rgb("4a4038"), rgb("8a8478"), rgb("d8d4c8")]


def wood():
    """Weathered dark planks, grain running across, a nail or two."""
    c = Canvas()
    noise(c, 0, 0, 15, 15, WOOD[1:], 28101, [2, 3, 1])
    for y in (4, 9, 14):
        for x in range(16):
            c.px(x, y, WOOD[0])
    c.px(2, 2, rgb("8a8a8a"))
    c.px(13, 7, rgb("8a8a8a"))
    return c.img


def slats():
    """The front: upright slats with dark gaps, and a small bat painted in pale paint."""
    c = Canvas()
    noise(c, 0, 0, 15, 15, WOOD[1:], 28102, [2, 3, 1])
    for x in (3, 7, 11, 15):
        for y in range(16):
            c.px(x, y, WOOD[0])
    pale = rgb("d8ccb0")
    for x, y in ((5, 6), (6, 5), (7, 6), (8, 6), (9, 5), (10, 6), (7, 7), (8, 7), (4, 7), (11, 7)):
        c.px(x, y, pale)
    return c.img


def pile():
    """Guano: dark crumbs flecked with white."""
    c = Canvas()
    noise(c, 0, 0, 15, 15, GUANO[:3], 28103, [3, 2, 1])
    rng = random.Random(28104)
    for _ in range(10):
        c.px(rng.randrange(16), rng.randrange(16), GUANO[3])
    return c.img


def guano_item():
    """A small heap of guano with white flecks."""
    c = Canvas()
    rng = random.Random(28105)
    for y in range(7, 14):
        half = (y - 6) * 0.9
        for x in range(int(8 - half), int(8 + half) + 1):
            c.px(x, y, GUANO[rng.choice((0, 1, 1, 2))])
    for _ in range(6):
        c.px(rng.randrange(5, 12), rng.randrange(9, 14), GUANO[3])
    outline(c, rgb("15110e"))
    return c.img


def bat_textures():
    return {("block", "bat_house"): wood(), ("block", "bat_house_slats"): slats(), ("block", "bat_guano_pile"): pile(),
            ("item", "bat_guano"): guano_item()}

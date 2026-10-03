"""The Hay Golem's texture (fall additions 14) (requires Pillow): golden straw running along each bundle, bound with
twine (two bands round the body, one round each wrist and ankle), a sackcloth patch stitched on its chest, and loose straw
splaying out at the collar, wrists and ankles; laid out for the client's HayGolemModel (64 by 64; each box's faces where
a vanilla model box puts them).

Called from crop_textures.crop_textures(). Every pixel is drawn here by code, from a fixed seed; no Mojang texture is
read, traced or recoloured.
"""
import random

from PIL import Image

from crop_textures import rgb

STRAW = [rgb("8a6a1e"), rgb("a8862a"), rgb("c4a23a"), rgb("d8b84e"), rgb("e8cc6a")]
TWINE = [rgb("5a3e22"), rgb("704e2c"), rgb("86603a")]
SACK = [rgb("8c7a5c"), rgb("9c8a6a"), rgb("ac9a78")]
STITCH = rgb("3a2a1a")

# (u, v, width, height, depth) of each box, as HayGolemModel lays them out.
BOXES = {"body": (0, 0, 12, 14, 8), "collar": (0, 22, 10, 2, 6), "arm": (40, 0, 4, 13, 4), "wrist": (40, 18, 5, 3, 5),
         "leg": (0, 30, 5, 10, 5), "ankle": (20, 30, 6, 2, 6)}


def box_area(u, v, w, h, d):
    """Every texel a model box with its texture at (u, v) reads, with whether it is on the top or bottom (else a side)."""
    for x in range(u + d, u + d + 2 * w):
        for y in range(v, v + d):
            yield x, y, True
    for x in range(u, u + 2 * (w + d)):
        for y in range(v + d, v + d + h):
            yield x, y, False


def straw(rng, x, y, along_y=True):
    """A straw texel: streaks run down the bundle (or across, on its ends), lighter and darker straws side by side."""
    stripe = x if along_y else y
    base = (stripe * 7 + (stripe // 2) * 3) % len(STRAW)
    return STRAW[max(0, min(len(STRAW) - 1, base + rng.choice((-1, 0, 0, 1))))]


def hay_golem():
    img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    rng = random.Random(14014)
    for name, (u, v, w, h, d) in BOXES.items():
        for x, y, end in box_area(u, v, w, h, d):
            colour = straw(rng, x, y, along_y=not end)
            side_y = y - (v + d)
            if not end:
                # Twine: two bands round the body, one near the end of each limb's bundle.
                if name == "body" and side_y in (3, 10):
                    colour = TWINE[(x + side_y) % 3]
                elif name in ("arm", "leg") and side_y == h - 2:
                    colour = TWINE[x % 3]
                # Loose straw at the collar, wrists and ankles: ragged ends with gaps.
                elif name in ("collar", "wrist", "ankle") and side_y == h - 1 and rng.random() < 0.35:
                    colour = None
            if colour is None:
                continue
            img.putpixel((x, y), colour + (255,))
    # A sackcloth patch stitched on the chest (the body's front face: u 8-19, v 8-21), a little left of centre.
    for x in range(10, 15):
        for y in range(12, 16):
            img.putpixel((x, y), SACK[rng.randrange(3)] + (255,))
    for x in range(10, 15):
        if x % 2 == 0:
            img.putpixel((x, 12), STITCH + (255,))
            img.putpixel((x, 15), STITCH + (255,))
    return img


def hay_golem_textures():
    return {("entity", "hay_golem"): hay_golem()}

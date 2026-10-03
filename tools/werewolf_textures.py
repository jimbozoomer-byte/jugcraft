"""Original textures for full-moon werewolves (fall addition 23) (requires Pillow): the werewolf (128 by 64, laid out as
client/WerewolfModel.java's boxes: shaggy grey-brown fur, a paler chest, yellow eyes, a black nose, a red mouth with
ivory teeth, pink inner ears, ivory claws); wolfsbane (a hooded violet-blue raceme over palmate leaves); the Werewolf
Rug (the pelt and its head); the silver dagger, the silver arrow and the pelt as items.

Called from crop_textures.crop_textures(). Every pixel is drawn here by code, from a fixed seed; no Mojang texture is
read, traced or recoloured.
"""
import math
import random

from PIL import Image

from agriculture import WEREWOLF, WOLFSBANE
from crop_textures import rgb

FUR = [rgb("2a2522"), rgb("3a332e"), rgb("4a423b"), rgb("5c5249"), rgb("6e6357")]
BELLY = [rgb("6a6054"), rgb("7c7264"), rgb("8c8272")]
EYE = rgb("f2c83a")
PUPIL = rgb("1a1208")
NOSE = rgb("141010")
MOUTH = rgb("7a1e1e")
TOOTH = rgb("ece4cc")
EAR = rgb("a8686a")
CLAW = rgb("d8ceb4")
SILVER = [rgb("6e7480"), rgb("9aa2ae"), rgb("c4ccd6"), rgb("eef2f6")]
LEATHER = [rgb("3e2414"), rgb("5a361e")]
SHAFT = [rgb("7a5a32"), rgb("9a7444")]
FLETCH = [rgb("d8d8d8"), rgb("b0b0b0")]
STEM = [rgb("2f5a24"), rgb("3e7230"), rgb("50883c")]
HOOD = [rgb("2c2470"), rgb("3e34a0"), rgb("5a4cc8"), rgb("7e70e0")]


def fill(img, x0, y0, w, h, colour):
    for y in range(y0, y0 + h):
        for x in range(x0, x0 + w):
            img.putpixel((x, y), colour + (255,))


def fur(width, height, seed, palette=FUR, lightest=3):
    """Shaggy fur: streaks of shade running down, a few lighter guard hairs."""
    img = Image.new("RGBA", (width, height))
    rng = random.Random(seed)
    for x in range(width):
        base = rng.choice((1, 2, 2, 3))
        for y in range(height):
            if rng.random() < 0.18:
                base = max(0, min(lightest, base + rng.choice((-1, 1))))
            tone = base if rng.random() > 0.08 else min(len(palette) - 1, base + 1)
            img.putpixel((x, y), palette[tone] + (255,))
    return img


def werewolf():
    img = fur(128, 64, 23001)
    # The chest's front (13 x 17 at 9, 9): a paler, rougher belly.
    rng = random.Random(23002)
    for y in range(9, 26):
        for x in range(11, 20):
            img.putpixel((x, y), BELLY[rng.randrange(3)] + (255,))
    # The skull's front (9 x 8 at 54, 8): brows, yellow eyes with black pupils.
    for x in range(54, 63):
        img.putpixel((x, 10), FUR[0] + (255,))
    for ex in (55, 60):
        fill(img, ex, 11, 2, 1, EYE)
        img.putpixel((ex + (1 if ex == 55 else 0), 11), PUPIL + (255,))
    # The snout's front (5 x 4 at 88, 6): a black nose on top, the lips below; its top (5 x 6 at 88, 0) paler.
    fill(img, 89, 6, 3, 2, NOSE)
    fill(img, 88, 9, 5, 1, FUR[0])
    for y in range(0, 6):
        for x in range(88, 93):
            img.putpixel((x, y), BELLY[(x + y) % 2] + (255,))
    # The jaw's top (5 x 5 at 111, 0) is the inside of the mouth, teeth round its edge; its front (5 x 2 at 111, 5).
    fill(img, 111, 0, 5, 5, MOUTH)
    for x in range(111, 116):
        img.putpixel((x, 0), TOOTH + (255,))
    for y in range(0, 5, 2):
        img.putpixel((111, y), TOOTH + (255,))
        img.putpixel((115, y), TOOTH + (255,))
    fill(img, 111, 5, 5, 1, TOOTH)
    # The snout's underside (5 x 6 at 93, 0) shows when the jaw drops: the roof of the mouth, with teeth.
    fill(img, 93, 0, 5, 6, MOUTH)
    for y in range(0, 6, 2):
        img.putpixel((93, y), TOOTH + (255,))
        img.putpixel((97, y), TOOTH + (255,))
    # The ears' fronts (3 x 4 at 108, 10): pink inside.
    fill(img, 109, 11, 1, 3, EAR)
    # The claws (5 x 3 x 5 at 86, 12): their sides ivory at the tips, the underside dark pads.
    for x in range(86, 106):
        for y in range(17, 20):
            img.putpixel((x, y), (CLAW if y == 19 or (x + y) % 3 == 0 else FUR[1]) + (255,))
    fill(img, 96, 12, 5, 5, FUR[0])
    # The feet's soles (6 x 7 at 107, 36): dark pads.
    fill(img, 107, 36, 6, 7, FUR[0])
    return img


def wolfsbane():
    """A tall stem, palmate leaves low down, and a spike of hooded violet-blue flowers above."""
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for y in range(4, 16):
        img.putpixel((8, y), STEM[1] + (255,))
    # Palmate leaves: fingers splayed either side, low on the stem.
    for side in (-1, 1):
        for finger in range(3):
            for k in range(1, 5 - finger):
                x = 8 + side * k
                y = 13 - finger - (k // 2) * (1 if finger else 0)
                if 0 <= x < 16 and 0 <= y < 16:
                    img.putpixel((x, y), STEM[(finger + k) % 3] + (255,))
    # The raceme: hooded flowers alternating up the spike, smaller towards the top.
    flowers = [(6, 10, 2), (10, 8, 2), (6, 6, 2), (10, 4, 1), (7, 2, 1), (9, 1, 1)]
    for fx, fy, size in flowers:
        for dy in range(-size, size + 1):
            for dx in range(-size, size + 1):
                if abs(dx) + abs(dy) <= size + 1:
                    x, y = fx + dx, fy + dy
                    if 0 <= x < 16 and 0 <= y < 16:
                        tone = 3 if dy < 0 else 2 if dy == 0 else 1
                        img.putpixel((x, y), HOOD[tone - (1 if abs(dx) == size else 0)] + (255,))
        # The hood's dark opening.
        if 0 <= fy + 1 < 16:
            img.putpixel((fx, fy + 1), HOOD[0] + (255,))
    img.putpixel((8, 0), HOOD[2] + (255,))
    return img


def rug():
    """The pelt seen from above: paler fur, a darker stripe down the spine."""
    img = fur(16, 16, 23003, palette=FUR[1:] + [BELLY[1]], lightest=4)
    for y in range(16):
        for x in (7, 8):
            img.putpixel((x, y), FUR[1] + (255,))
    return img


def rug_head():
    """The head's fur, darker, with a hint of eyes and teeth where the face is."""
    img = fur(16, 16, 23004)
    for x in (6, 9):
        img.putpixel((x, 13), EYE + (255,))
    for x in range(6, 10):
        img.putpixel((x, 15), TOOTH + (255,))
    return img


def dagger():
    """A silver dagger held point up to the right: a leaf blade, a cross-guard, a leather grip and a round pommel."""
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for i in range(7, 15):
        x, y = i, 15 - i
        img.putpixel((x, y), SILVER[2] + (255,))
        img.putpixel((x - 1, y), SILVER[1] + (255,))
        if i < 13:
            img.putpixel((x, y + 1), SILVER[0] + (255,))
    img.putpixel((15, 0), SILVER[3] + (255,))
    img.putpixel((14, 1), SILVER[3] + (255,))
    # The cross-guard, across the blade's foot.
    for x, y in ((4, 9), (5, 8), (6, 9), (7, 10), (8, 11), (5, 10), (6, 11)):
        img.putpixel((x, y), SILVER[1] + (255,))
    # The grip and pommel.
    for x, y in ((4, 11), (3, 12), (5, 12), (4, 13)):
        img.putpixel((x, y), LEATHER[(x + y) % 2] + (255,))
    for x, y in ((2, 13), (1, 14), (2, 14), (1, 13)):
        img.putpixel((x, y), SILVER[2] + (255,))
    return img


def arrow():
    """An arrow, fletching at the lower left and a bright silver head at the upper right."""
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for i in range(3, 13):
        img.putpixel((i, 15 - i), SHAFT[i % 2] + (255,))
    for x, y in ((12, 1), (13, 2), (14, 1), (13, 0), (12, 2), (14, 2), (13, 1), (11, 2)):
        img.putpixel((x, y), SILVER[2 + (x + y) % 2] + (255,))
    for x, y in ((2, 11), (3, 11), (1, 12), (2, 13), (4, 13), (4, 14), (3, 14), (1, 13)):
        img.putpixel((x, y), FLETCH[(x + y) % 2] + (255,))
    return img


def pelt():
    """A folded pelt, shaggy, a paler edge showing."""
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    fur_img = fur(16, 16, 23005)
    for y in range(3, 14):
        for x in range(2 + (y % 2), 14 - (y % 3 == 0)):
            img.putpixel((x, y), fur_img.getpixel((x, y)))
    for x in range(3, 13):
        img.putpixel((x, 13), BELLY[1] + (255,))
    return img


def werewolf_textures():
    return {("entity", WEREWOLF["entity"]): werewolf(), ("block", WOLFSBANE["block"]): wolfsbane(),
            ("block", WEREWOLF["rug"]): rug(), ("block", f"{WEREWOLF['rug']}_head"): rug_head(),
            ("item", WEREWOLF["dagger"]): dagger(), ("item", WEREWOLF["arrow"]): arrow(), ("item", WEREWOLF["pelt"]): pelt()}

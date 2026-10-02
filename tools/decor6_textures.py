"""Original textures for the sixth batch of Halloween decorations, the haunted house and yard (requires Pillow): the
Rocking Chair's wood and cushion, the Lurking Eyes, the Silhouette Window's frame and its three cut-outs (dim and lit),
the Spooky Music Box and the Giant Fake Spider.

Called from crop_textures.crop_textures(). Every pixel is drawn here by code or from the small pixel-art grids below,
from fixed seeds; no Mojang texture is read, traced or recoloured. Block textures are 16x16 and opaque; the eye sprite
and the item icons have see-through backgrounds.
"""
import math
import random

from crop_textures import Canvas, rgb
from decor_textures import noise

WALNUT = [rgb("3a2414"), rgb("4a2e1a"), rgb("5a3a22"), rgb("6a462a")]
VELVET = [rgb("4a0e16"), rgb("5e1420"), rgb("741c2a"), rgb("8a2a36")]
LEAVES = [rgb("1a2a12"), rgb("223818"), rgb("2c4620")]
EYE = {"y": rgb("b8d828"), "Y": rgb("f0ff8a"), "P": rgb("0c0c08")}
FRAME = [rgb("24160c"), rgb("2e1c10"), rgb("382414")]
PAPER_DIM = [rgb("8a4410"), rgb("9c5014"), rgb("b05e1a")]
PAPER_LIT = [rgb("e8781a"), rgb("ff9a2a"), rgb("ffbe4a"), rgb("ffd878")]
INK = rgb("140c08")
LACQUER = [rgb("140a14"), rgb("1e101e"), rgb("2a162a"), rgb("4a2a4a")]
BRASS = [rgb("7a5a1a"), rgb("a8822a"), rgb("d0aa44"), rgb("f0d278")]
GHOST = [rgb("d8dce4"), rgb("eef0f6"), rgb("ffffff")]
HAIR = [rgb("0e0a08"), rgb("1a1410"), rgb("261c14"), rgb("3a2a1c")]
SPIDER_EYE = [rgb("6a0808"), rgb("b01414"), rgb("ff4a3a")]
SILK = [rgb("c8c8c0"), rgb("dcdcd4"), rgb("eeeee8")]

# The cut-outs, 16 by 16 ('#' black; the outer ring sits under the frame).
DESIGNS = {
    "bat": [
        "................",
        "................",
        "................",
        "................",
        ".#............#.",
        ".##...#..#...##.",
        ".###..####..###.",
        ".##############.",
        "..############..",
        "..###.####.###..",
        "..##..####..##..",
        "..#....##....#..",
        "................",
        "................",
        "................",
        "................"],
    "cat": [
        "................",
        "................",
        "................",
        "..........#..#..",
        "..........####..",
        "..........####..",
        "...........##...",
        "..........####..",
        ".........#####..",
        "........######..",
        "...#....######..",
        "..#.....######..",
        "..#....#######..",
        "...##.########..",
        ".....#########..",
        "................"],
    "witch": [
        "................",
        "................",
        "................",
        ".........#......",
        "........##......",
        ".......####.....",
        ".....########...",
        "........###.....",
        ".......####.....",
        "......######....",
        ".###############",
        "#####..##.......",
        ".###....#.......",
        "................",
        "................",
        "................"],
}
SPIDER_ICON = [
    "................",
    ".#............#.",
    "..#..........#..",
    "...#...##...#...",
    "#...#.#rr#.#...#",
    ".#...######...#.",
    "..##.######.##..",
    "....########....",
    "..##.######.##..",
    ".#...######...#.",
    "#...#.####.#...#",
    "...#..####..#...",
    "..#....##....#..",
    ".#............#.",
    "................",
    "................"]


def walnut():
    """Dark stained planks running across, with grain streaks."""
    c = Canvas()
    rng = random.Random(16101)
    for y in range(16):
        base = 1 if y % 8 else 0
        for x in range(16):
            tone = base + (1 if rng.random() < 0.3 else 0) + (1 if (x * 3 + y * 7) % 11 == 0 else 0)
            c.px(x, y, WALNUT[min(3, tone)])
    return c.img


def velvet():
    """Faded velvet with a tufting button every eight pixels."""
    c = Canvas()
    noise(c, 0, 0, 15, 15, VELVET[1:], 16111, [3, 2, 1])
    for x, y in ((3, 3), (11, 3), (7, 11), (15, 11)):
        c.px(x, y, VELVET[0])
        c.px(x + 1, y, VELVET[0])
    return c.img


def leafy():
    c = Canvas()
    noise(c, 0, 0, 15, 15, LEAVES, 16201, [2, 3, 2])
    return c.img


def eye_sprite():
    """One glowing eye (6 by 4, top left); the renderer draws two."""
    c = Canvas()
    for y, row in enumerate([".yyyy.", "yYYPYy", "yYYPYy", ".yyyy."]):
        for x, ch in enumerate(row):
            if ch in EYE:
                c.px(x, y, EYE[ch])
    return c.img


def eyes_item():
    """A dark bush with two glowing eyes."""
    c = Canvas()
    rng = random.Random(16211)
    for y in range(16):
        for x in range(16):
            if ((x - 7.5) / 7.5) ** 2 + ((y - 8.5) / 5.5) ** 2 <= 1.0:
                c.px(x, y, LEAVES[rng.randrange(3)])
    for x0 in (3, 9):
        for dx, dy, ch in ((0, 0, "y"), (1, 0, "Y"), (2, 0, "P"), (3, 0, "y"), (0, 1, "y"), (1, 1, "Y"), (2, 1, "P"), (3, 1, "y")):
            c.px(x0 + dx, 7 + dy, EYE[ch])
    return c.img


def frame():
    c = Canvas()
    noise(c, 0, 0, 15, 15, FRAME, 16301, [2, 3, 1])
    return c.img


def paper(design, lit):
    """Orange paper, brighter towards the middle (lit, much brighter), with the black cut-out."""
    c = Canvas()
    rng = random.Random(16311 + list(DESIGNS).index(design) * 3 + (1 if lit else 0))
    palette = PAPER_LIT if lit else PAPER_DIM
    for y in range(16):
        for x in range(16):
            d = math.hypot(x - 7.5, y - 7.5) / 10.6
            tone = int((1 - d) * (len(palette) - 1) + rng.uniform(-0.4, 0.4))
            c.px(x, y, palette[max(0, min(len(palette) - 1, tone))])
    for y, row in enumerate(DESIGNS[design]):
        for x, ch in enumerate(row):
            if ch == "#":
                c.px(x, y, INK)
    return c.img


def lacquer():
    """Glossy black lacquer with a faint purple sheen and a highlight streak."""
    c = Canvas()
    noise(c, 0, 0, 15, 15, LACQUER[:3], 16401, [3, 2, 1])
    for i in range(16):
        c.px(i, (i // 2 + 3) % 16, LACQUER[3])
    return c.img


def brass():
    c = Canvas()
    noise(c, 0, 0, 15, 15, BRASS[:3], 16411, [1, 3, 2])
    for i in range(0, 16, 3):
        c.px(i, i, BRASS[3])
    return c.img


def velvet_lining():
    c = Canvas()
    noise(c, 0, 0, 15, 15, VELVET, 16421, [1, 2, 3, 1])
    return c.img


def ghost():
    """A little sheet ghost: white with two dark eyes in the middle of each face."""
    c = Canvas()
    noise(c, 0, 0, 15, 15, GHOST, 16431, [1, 2, 3])
    for x in (5, 10):
        c.rect(x, 5, x, 7, INK)
    c.rect(6, 10, 9, 11, rgb("3a3a44"))
    return c.img


def hair():
    """Coarse black-brown bristles, with a few lighter tips."""
    c = Canvas()
    rng = random.Random(16501)
    for y in range(16):
        for x in range(16):
            c.px(x, y, HAIR[rng.choice((0, 0, 1, 1, 2))])
    for _ in range(18):
        x, y = rng.randrange(16), rng.randrange(16)
        c.px(x, y, HAIR[3])
    return c.img


def spider_eye():
    c = Canvas()
    noise(c, 0, 0, 15, 15, SPIDER_EYE[:2], 16511, [1, 2])
    c.rect(3, 3, 6, 6, SPIDER_EYE[2])
    return c.img


def silk():
    c = Canvas()
    noise(c, 0, 0, 15, 15, SILK, 16521, [1, 2, 2])
    return c.img


def spider_icon():
    c = Canvas()
    for y, row in enumerate(SPIDER_ICON):
        for x, ch in enumerate(row):
            if ch == "#":
                c.px(x, y, HAIR[1] if (x + y) % 3 else HAIR[3])
            elif ch == "r":
                c.px(x, y, SPIDER_EYE[2])
    return c.img


def decor6_textures():
    """(kind, name) -> image for every texture of the sixth decorations batch."""
    out = {
        ("block", "rocking_chair_wood"): walnut(),
        ("block", "rocking_chair_cushion"): velvet(),
        ("block", "lurking_eyes_particle"): leafy(),
        ("entity", "lurking_eyes"): eye_sprite(),
        ("item", "lurking_eyes"): eyes_item(),
        ("block", "silhouette_window_frame"): frame(),
        ("block", "music_box_lacquer"): lacquer(),
        ("block", "music_box_brass"): brass(),
        ("block", "music_box_velvet"): velvet_lining(),
        ("block", "music_box_ghost"): ghost(),
        ("block", "giant_fake_spider_hair"): hair(),
        ("block", "giant_fake_spider_eye"): spider_eye(),
        ("block", "giant_fake_spider_silk"): silk(),
        ("item", "giant_fake_spider"): spider_icon(),
    }
    for design in DESIGNS:
        out[("block", f"silhouette_window_{design}")] = paper(design, False)
        out[("entity", f"silhouette_window_{design}_lit")] = paper(design, True)
    return out

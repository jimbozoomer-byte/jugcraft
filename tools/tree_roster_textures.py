"""Small authored tree details, using the same Canvas and palettes as the established wood generator."""
import random

from crop_textures import Canvas, rgb


def cluster_centres(seed, count=3, spacing=4):
    """Distinct seeded clusters, separated even across a repeating texture's edges."""
    rng, points = random.Random(seed), []
    for _ in range(256):
        point = rng.randrange(16), rng.randrange(16)
        if all(sum(min(abs(a - b), 16 - abs(a - b)) ** 2 for a, b in zip(point, other)) >= spacing ** 2 for other in points):
            points.append(point)
            if len(points) == count:
                return points
    raise ValueError("Cannot fit fruit clusters at the requested spacing")


def yew_fruit(base, ripe):
    canvas = Canvas()
    canvas.img = base.copy()
    colours = [rgb(c) for c in ("901a29", "b22e3e", "d04859", "3a1418")] if ripe else [rgb(c) for c in ("39472a", "53633a", "71854d", "25301b")]
    for x, y in cluster_centres(9303):
        for dx, dy, shade in ((-1, 0, 0), (0, -1, 2), (1, 0, 1), (0, 1, 0), (0, 0, 3)):
            canvas.px((x + dx) % 16, (y + dy) % 16, colours[shade])
    return canvas.img


def yew_sapling():
    from wood_style import BARK, LEAVES
    canvas = Canvas()
    bark, leaves = BARK["yew"][0], LEAVES["yew_needles"][0]
    canvas.line(8, 15, 8, 5, bark[2])
    for y, half in ((4, 2), (7, 4), (10, 5)):
        canvas.line(8 - half, y, 8 + half, y, bark[2])
        for dx in range(-half, half + 1):
            canvas.px(8 + dx, y - 1, leaves[4 if dx % 2 else 3])
            canvas.px(8 + dx, y + 1, leaves[2])
    canvas.px(8, 2, leaves[5])
    return canvas.img


def yew_arils():
    canvas = Canvas()
    palette = [rgb(c) for c in ("3a1418", "681421", "901a29", "b22e3e", "d04859", "e3747e")]
    for x, y in ((5, 5), (11, 7), (7, 11)):
        for dx in range(-2, 3):
            for dy in range(-2, 3):
                if abs(dx) + abs(dy) <= 3:
                    canvas.px(x + dx, y + dy, palette[1 if dy == 2 else 2 if dx == 2 else 3])
        canvas.px(x - 1, y - 1, palette[4])
        canvas.px(x, y - 2, palette[5])
        canvas.px(x, y, palette[0])
    return canvas.img


def textures(base):
    needles = base[("block", "yew_needles")]
    return {("block", "yew_needles_unripe"): yew_fruit(needles, False),
            ("block", "yew_needles_ripe"): yew_fruit(needles, True),
            ("block", "yew_sapling"): yew_sapling(), ("item", "yew_arils"): yew_arils()}

"""The fall garden's plants (decor set 4, part 1; docs/features/fall-garden-and-porch.md), sculpted as the graveyard
flora is (tools/flora_models.py; docs/features/graveyard-flora.md): a few stems of small boxes carrying flat, cut-out
leaves and petals at angles, and the flower heads, seed heads, plumes and berries as little boxes. Each builder returns
a Sculpt whose models are named as tools/flora_data.py writes them (the plant's own, or `<plant>_bottom`, `_top` and
`_item` for a two-block plant); flora_models.BUILDERS takes them in, and flora_models.build() paints them in the clean
style (no per-pixel random tones).

Sizes are real ones, at a block a metre: a New England aster's clump 65 cm high with heads 12 cm across (a head of
many daisies drawn as one); stonecrop 50 cm with flat flower heads 19 cm across; ornamental kale and cabbage rosettes
50 cm across and 20 cm high; pampas grass 2 m to its plumes; a dried sunflower 1.9 m with a 37 cm head.
"""
import math

from flora_art import (SIDES4, TEXELS, Sculpt, cube, leaf, leaf_flat, leaf_out, pal, plane_xy, plane_xz, plane_zy, rotation,
                       segment, shade, solid, star, strip, upright)

# One colour each, stepped into tones (docs/NATURAL_TEXTURES.md, "One colour, stepped into tones").
ASTER = pal("3e2056", "56307a", "6e449c", "8a5cba", "a678d2")
ASTER_EYE = pal("8a6418", "b08a20", "d0aa34")
ASTER_LEAF = pal("223a1e", "30502a", "426836", "568046")
SEDUM_HEAD = pal("4a1a16", "6a2a20", "8a3c2a", "a8523a", "c06a4c")
SEDUM_LEAF = pal("2a4232", "3a5a44", "4e7458", "668e6e", "80a886")
KALE_HEART = pal("3a1236", "561c4e", "742a68", "923c84", "ac56a0")
KALE_LEAF = pal("1e3a3a", "2a4e4c", "3a6460", "4e7a74", "66928a")
CABBAGE_HEART = pal("8a7a7e", "a8969a", "c4b2b4", "dccccc", "ece2e0")
CABBAGE_LEAF = pal("26402e", "34563c", "46704c", "5c8a60", "76a476")
PAMPAS = pal("8a7a5a", "a8966e", "c4b286", "d8c89e", "e8dab4")
PAMPAS_BLADE = pal("3a4a2a", "4e6236", "667a44", "7e9256", "98aa6c")
SUN_STALK = pal("3a2a16", "52401e", "6a5228", "826836")
SUN_LEAF = pal("2e2214", "44321c", "5a4426", "725a34", "8a7044")
SUN_SEED = pal("1e140c", "2c1e12", "3c2a18", "4e3820")
SUN_PETAL = pal("6a4a14", "8a621c", "a87c28", "c49638")
VINE_STEM = pal("3a2a1e", "4e3a28", "644c34")
VINE_LEAF = pal("6a5212", "8a6c1a", "a88a26", "c4a636", "dcc04e")
HUSK = pal("8a5a10", "b07818", "d09a26")
BITTERSWEET = pal("6a120a", "901c10", "b42a18", "d04424")


def seed_head(palette, rim):
    """A sunflower's dried face, seen straight on: rings of seeds in the middle tones, a darker ring every third, inside a
    rim of shrivelled petal bases (`rim`), lit along its upper left."""
    def paint(p):
        cx, cy = (p.w - 1) / 2, (p.h - 1) / 2
        r_max = min(p.w, p.h) / 2
        for y in range(p.h):
            for x in range(p.w):
                r = math.hypot(x - cx, y - cy) / r_max
                if r > 1.0:
                    continue
                if r > 0.82:
                    c = shade(rim, 2 if (x + y) % 2 else 1)
                else:
                    ring = int(r * 6)
                    k = 1 if ring % 3 == 2 else 2
                    if x + y < cx + cy - r_max * 0.6:
                        k += 1
                    c = shade(palette, k)
                p.put(x, y, c)
    return paint


def plume(palette):
    """A pampas plume's side: soft rows of florets, the middle tone with a lit row every other one and a shaded edge
    down its right, paler towards its tip."""
    def paint(p):
        for y in range(p.h):
            t = 1 - y / max(1, p.h - 1)
            for x in range(p.w):
                k = 3 if y % 2 == 0 else 2
                if x == p.w - 1 and p.w > 2:
                    k -= 1
                if t > 0.75:
                    k += 1
                p.put(x, y, shade(palette, k))
    return paint


def floret_cap(palette):
    """A flat head of tiny florets seen from above: the middle tone, with a lit florets' row every other texel row
    offset by one, round to its edge."""
    def paint(p):
        cx, cy = (p.w - 1) / 2, (p.h - 1) / 2
        r_max = min(p.w, p.h) / 2
        for y in range(p.h):
            for x in range(p.w):
                if math.hypot(x - cx, y - cy) / r_max > 1.0:
                    continue
                lit = (x + (y % 2)) % 2 == 0 and y % 2 == 0
                p.put(x, y, shade(palette, 3 if lit else 2))
    return paint


def along(base, axis, angle, length):
    """The point `length` up a stem from `base` leaning by `angle` about `axis` (as flora_art.segment leans it)."""
    bx, by, bz = base
    a = math.radians(angle)
    if axis == "x":
        return bx, by + length * math.cos(a), bz + length * math.sin(a)
    if axis == "z":
        return bx - length * math.sin(a), by + length * math.cos(a), bz
    return bx, by + length, bz


def aster():
    """A New England aster: a bushy clump of seven stems, leafy all the way up with lance leaves, each crowned by a
    spray of three violet daisy heads with golden eyes (one level on top, two turned up and out beside it)."""
    sc = Sculpt("aster", 61)
    stem = sc.piece("stem", 2, 20, strip(pal("2a3e1e", "3a5428", "4c6a34", "608044"), 3))
    head = sc.piece("head", 7, 7, star(14, ASTER, centre=ASTER_EYE, inner=0.12, width=0.55, seed=3, centre_r=0.3))
    lance = leaf(ASTER_LEAF, "lance", seed=5)
    els = []
    stems = ((8.0, 8.0, None, 0, 10.5), (6.6, 7.4, "z", 22.5, 9.4), (9.4, 7.6, "z", -22.5, 9.8), (7.6, 9.4, "x", 22.5, 9.0),
             (8.6, 6.6, "x", -22.5, 8.8), (6.8, 9.0, "z", 22.5, 8.2), (9.2, 9.2, "x", 22.5, 8.4))
    sides = ("north", "east", "south", "west")
    for i, (x, z, axis, angle, length) in enumerate(stems):
        e, (tx, ty, tz) = segment((x, 0, z), length, axis, angle, width=0.6, uv=stem)
        els.append(e)
        for k, up in enumerate((0.3, 0.5, 0.7)):
            lx, ly, lz = along((x, 0, z), axis, angle, length * up)
            els.append(leaf_out(sc, "leaf", lance, (lx, ly, lz), sides[(i + k) % 4], 2.6, 0.9, 45))
        h = 1.2
        els.append(plane_xz(tx - h, tx + h, tz - h, tz + h, ty, head))
        for k, (dx, dz, rot) in enumerate(((0.0, 1.1, ("x", 45)), (1.1, 0.0, ("z", -45)), (0.0, -1.1, ("x", -45)), (-1.1, 0.0, ("z", 45)))):
            if (i + k) % 2:
                continue
            cx, cz = tx + dx, tz + dz
            els.append(plane_xz(cx - 1.0, cx + 1.0, cz - 1.0, cz + 1.0, ty - 1.0, head, rotation((cx, ty - 1.0, cz), *rot)))
    sc.models["aster"] = els
    return sc


def sedum():
    """'Autumn Joy' stonecrop: thick upright stems set with round, fleshy grey-green leaves (little boxes), each stem
    topped by a broad flat head of florets turned brick-rose by the cold."""
    sc = Sculpt("sedum", 62)
    stem = sc.piece("stem", 2, 16, strip(pal("3a5244", "4c6a56", "60806a", "76987e"), 5))
    fleshy = sc.piece("fleshy", 3, 3, solid(SEDUM_LEAF, 6))
    cap = sc.piece("cap", 8, 8, floret_cap(SEDUM_HEAD))
    rim = sc.piece("rim", 6, 2, solid(SEDUM_HEAD, 7))
    els = []
    for x, z, height in ((6.0, 6.5, 7.0), (10.0, 7.0, 7.6), (7.5, 10.0, 6.6), (9.6, 10.4, 7.2)):
        e, _ = segment((x, 0, z), height, width=0.9, uv=stem)
        els.append(e)
        for k, (dx, dz) in enumerate(((0.9, 0.0), (-0.9, 0.0), (0.0, 0.9), (0.0, -0.9))):
            y = 1.5 + k * 1.3
            lx, lz = x + dx, z + dz
            els.append(cube((lx - 0.6, y, lz - 0.6), (lx + 0.6, y + 1.0, lz + 0.6), {s: fleshy for s in SIDES4 + ("up", "down")}))
        # The flat head: a low dome of florets, its rim a little below its top.
        els.append(cube((x - 1.5, height, z - 1.5), (x + 1.5, height + 0.6, z + 1.5),
                        {**{s: rim for s in SIDES4}, "up": cap, "down": rim}))
    sc.models["sedum"] = els
    return sc


def rosette(name, seed, heart, outer, frilled):
    """An ornamental kale or cabbage, a rosette like a great flower 50 cm across: a ring of broad outer leaves lying
    out and rising at their tips, eight of them, their tips taking the heart's colour; a ring of the heart's colour
    standing up within it; and the heart's own tight leaves, cupped, round a small bud. Kale's leaves are frilled at
    their edges (`frilled`), cabbage's smooth."""
    sc = Sculpt(name, seed)
    wavy = 1.0 if frilled else 0.25
    big = leaf(outer, "spoon", seed=seed, wavy=wavy, tip_colour=shade(heart, 3))
    mid = leaf(heart, "spoon", seed=seed + 1, wavy=wavy)
    els = []
    for d in ("north", "south", "east", "west"):
        els.append(leaf_out(sc, "outer", big, (8, 0.8, 8), d, 4.2, 4.0, 22.5))
    for d in ("ne", "se", "sw", "nw"):
        els.append(leaf_flat(sc, "low", leaf(outer, "spoon", seed=seed + 2, wavy=wavy), (8, 0.5, 8), d, 3.8, 3.4))
    for d in ("north", "south", "east", "west"):
        els.append(leaf_out(sc, "inner", mid, (8, 1.2, 8), d, 3.0, 3.0, 45))
    small = leaf(heart, "round", seed=seed + 3, wavy=wavy * 0.5)
    for d, (x, z) in (("north", (8, 8.4)), ("south", (8, 7.6)), ("east", (7.6, 8)), ("west", (8.4, 8))):
        els.append(leaf_out(sc, "heart", small, (x, 1.6, z), d, 1.8, 2.0, 45))
    bud = sc.piece("bud", 3, 3, solid(heart, seed + 4))
    els.append(cube((7.3, 0.8, 7.3), (8.7, 2.4, 8.7), {s: bud for s in SIDES4 + ("up", "down")}))
    sc.models[name] = els
    return sc


def ornamental_kale():
    return rosette("ornamental_kale", 63, KALE_HEART, KALE_LEAF, True)


def ornamental_cabbage():
    return rosette("ornamental_cabbage", 64, CABBAGE_HEART, CABBAGE_LEAF, False)


def pampas_grass():
    """Pampas grass, two blocks: a fountain of long arching blades from its crown, and five stems rising above them to
    feathery cream plumes, each a box tapering in two steps."""
    from flora_models import strap_arch, tall_models
    sc = Sculpt("pampas_grass", 65)
    stalk = sc.piece("stalk", 2, 28, strip(pal("6a6a44", "84845a", "a09e70"), 8))
    els = []
    for i, (yaw, lean) in enumerate(((0, 22.5), (0, -22.5), (90, 22.5), (90, -22.5), (45, 0), (135, 0), (22.5, 0), (112.5, 0))):
        els.append(upright(sc, f"blade{i % 2}", strap_arch(PAMPAS_BLADE, 90 + i % 2), (8, 8), 4, 0, 14.0 - (i % 3) * 1.5, yaw=yaw, lean=lean))
    side = sc.piece("plume", 4, 14, plume(PAMPAS))
    tip = sc.piece("plume_tip", 2, 6, plume(PAMPAS))
    for x, z, height, angle in ((8.0, 8.0, 22.0, 0), (6.5, 7.5, 20.0, -22.5), (9.6, 8.6, 20.6, 22.5), (7.6, 9.8, 19.2, 0),
                                (8.8, 6.4, 18.6, 0)):
        axis = "z" if angle else None
        e, top = segment((x, 0, z), height, axis, angle, width=0.5, uv=stalk)
        els.append(e)
        tx, ty, tz = top
        rot = rotation((tx, ty, tz), "z", angle) if angle else None
        els.append(cube((tx - 1.0, ty, tz - 1.0), (tx + 1.0, ty + 7.0, tz + 1.0), {**{s: side for s in SIDES4}, "up": tip, "down": tip}, rot))
        els.append(cube((tx - 0.5, ty + 7.0, tz - 0.5), (tx + 0.5, ty + 9.0, tz + 0.5), {**{s: tip for s in SIDES4}, "up": tip}, rot))
    tall_models(sc, els)
    return sc


def dried_sunflower():
    """A sunflower left standing to dry, two blocks: a stout brown stalk with its leaves hanging limp and withered, and
    its head, heavy with seed, bowed over to the south: a seeded face in a rim of shrivelled petals, a domed green-brown
    back, 37 cm across and 12 cm deep."""
    from flora_models import tall_models
    sc = Sculpt("dried_sunflower", 66)
    stalk = sc.piece("stalk", 3, 30, strip(SUN_STALK, 9))
    face = sc.piece("face", 12, 12, seed_head(SUN_SEED, SUN_PETAL))
    back = sc.piece("back", 12, 12, solid(SUN_LEAF, 12))
    dome = sc.piece("dome", 8, 8, solid(SUN_LEAF, 19))
    edge = sc.piece("edge", 12, 4, solid(SUN_PETAL, 13))
    limp = leaf(SUN_LEAF, "oval", seed=14, wavy=0.5, curl=0.35)
    els = []
    e, top = segment((8, 0, 8), 16, width=1.4, uv=stalk)
    els.append(e)
    e, top = segment(top, 11, width=1.2, uv=stalk)
    els.append(e)
    for i, (y, d) in enumerate(((4.0, "north"), (7.5, "east"), (11.0, "west"), (15.0, "south"), (19.0, "north"), (22.5, "east"))):
        els.append(leaf_out(sc, "limp", limp, (8, y, 8), d, 5.0 - i * 0.3, 3.4 - i * 0.2, -45, tex=(7, 10)))
    # The neck bends over to the south, and the head hangs from it, its face turned down and out.
    e, neck = segment((8, 27, 8), 2.6, "x", 45, width=1.0, uv=stalk)
    els.append(e)
    nx, ny, nz = neck
    z = nz + 1.0
    rot = rotation((8, ny, z), "x", 45)
    els.append(cube((5.0, ny - 6.0, z), (11.0, ny, z + 2.0),
                    {"south": face, "north": back, "east": edge, "west": edge, "up": edge, "down": edge}, rot))
    els.append(cube((6.0, ny - 5.0, z - 0.75), (10.0, ny - 1.0, z), {**{s: dome for s in SIDES4}, "up": dome, "down": dome}, rot))
    tall_models(sc, els)
    return sc


def twining(seed=1):
    """Bittersweet's stem across a face, as a sheet: a woody stem two texels wide wandering up it on a slow sine, with a
    shorter one branching off, in two tones (lit on its upper side); the rest open."""
    def paint(p):
        def stem_line(x0, y0, x1, y1, wave, phase):
            steps = max(abs(x1 - x0), abs(y1 - y0)) * 2
            for k in range(steps + 1):
                t = k / steps
                x = x0 + (x1 - x0) * t + wave * math.sin(t * math.tau * 1.5 + phase)
                y = y0 + (y1 - y0) * t
                p.put(x, y, shade(VINE_STEM, 2))
                p.put(x + 1, y, shade(VINE_STEM, 1))
        stem_line(4, p.h - 1, p.w - 6, 0, 5.0, seed)
        stem_line(p.w // 2, p.h // 2, p.w - 2, p.h - 6, 2.5, seed + 2)
    return paint


def bittersweet():
    """American bittersweet climbing a face: its woody stem twining up across it (a sheet), butter-yellow oval leaves
    standing out from it, and clusters of berries along it, each a scarlet berry in its split orange husk."""
    sc = Sculpt("bittersweet", 67)
    sheet = sc.piece("sheet", 32, 32, twining(1))
    berry_side = sc.piece("berry", 2, 2, solid(BITTERSWEET, 16, glossy=shade(BITTERSWEET, 3)))
    husk = sc.piece("husk", 4, 4, star(3, HUSK, inner=0.0, width=0.7, seed=17))
    els = [plane_xy(0, 16, 0, 16, 0.15, sheet)]
    # Points on the stem (the sheet's sine, in model pixels: x across, y up from the bottom).
    def on_stem(t):
        return 2.0 + 9.0 * t + 2.5 * math.sin(t * math.tau * 1.5 + 1), 16.0 * t

    for i, t in enumerate((0.15, 0.35, 0.55, 0.75, 0.92)):
        x, y = on_stem(t)
        uv = sc.atlas.piece(f"leaf{i % 2}", 5, 8, leaf(VINE_LEAF, "oval", seed=15 + i % 2))
        side = 1.6 if i % 2 else -1.6
        lx = min(14.0, max(2.0, x + side))
        els.append(plane_xy(lx - 1.25, lx + 1.25, y - 1.5, y + 2.5, 0.6, uv, rotation((lx, y - 1.5, 0.6), "x", -22.5 if i % 2 else -45)))
    for t in (0.25, 0.45, 0.65, 0.85):
        cx, cy = on_stem(t)
        for dx, dy in ((0.0, 0.0), (1.1, -0.7), (-1.0, -0.9)):
            bx, by = cx + dx + 0.6, cy + dy
            els.append(cube((bx - 0.5, by - 0.5, 0.4), (bx + 0.5, by + 0.5, 1.4), {s: berry_side for s in SIDES4 + ("up", "down")}))
            els.append(plane_xy(bx - 1.0, bx + 1.0, by - 0.1, by + 1.1, 1.45, husk))
    sc.models["bittersweet"] = els
    return sc


BUILDERS = {"aster": aster, "sedum": sedum, "ornamental_kale": ornamental_kale, "ornamental_cabbage": ornamental_cabbage,
            "pampas_grass": pampas_grass, "dried_sunflower": dried_sunflower, "bittersweet": bittersweet}

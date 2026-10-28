"""Generated assets for Fall Garden & Porch, decor set 4 (tools/fall_garden.py; docs/features/fall-garden-and-porch.md),
from part 2 on (part 1's plants are the wild plants', tools/flora_data.py).

Part 2, the planters, are built from boxes at real size, one texel to a model pixel, in the market displays' manner
(tools/pumpkin_patch_data.py): each material one colour stepped into tones, its marks running the way it does.
- The Window Box: painted boards, sage green, nailed, with two cleats behind it on the wall and soil to its rim.
- The Half-Barrel: oak staves round in two row bands of a circle, two iron hoops a quarter pixel proud of them, the
  staves' ends a rim round the soil.
- The Galvanized Tub: an oval wash tub of spangled zinc, two pressed ribs round it, a rolled rim and a drop handle at
  each end.
Round and oval things are two crossed boxes, as the churn and the quern are; their soil is the top of the body, and the
rim is four thin boxes along the outline above it, so no box is left open. What grows in them is drawn by
client/PlanterRenderer.
"""
import decor20_data as d20d
import fall_garden as fg
import flora_art as fa
import pumpkin_patch_data as ppd
from flora_art import Sculpt, pal, shade
from pumpkin_patch_data import IRON, WOOD, flat, lengthwise, panel, planks

MOD = "jugcraft"
FACINGS = ppd.FACINGS

PAINT = pal("2e3a2c", "3e4e3a", "52664c", "68805e", "809a74")
SOIL = pal("2a1e14", "3a2a1c", "4c3826", "5e4630")
ZINC = pal("4e5458", "666c70", "7e8488", "989ea2", "b2b8bc")


def rid(path):
    return path if ":" in path else f"{MOD}:{path}"


def soil():
    """Turned soil seen from above: its middle tone with lit clods and their shadows in a steady diagonal run, every fifth
    texel along each row, one row's run a step on from the last."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                n = (x + 3 * y) % 5
                p.put(x, y, shade(SOIL, 3 if n == 0 else 1 if n == 2 else 2))
    paint.tag = "soil"
    return paint


def staves(palette=WOOD):
    """A barrel's staves: upright boards three texels wide, each one tone of the wood by turns, with a seam a tone darker
    down its right edge; its top row lit (the stave ends' edge)."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                k = 2 if (x // 3) % 2 == 0 else 3
                if x % 3 == 2:
                    k = 1
                if y == 0 and p.h > 2:
                    k = 4
                p.put(x, y, shade(palette, k))
    paint.tag = repr(("staves", tuple(palette)))
    return paint


def zinc(ribs=False):
    """Galvanized zinc: its middle tone, crossed by the spangle of the coating in a steady run of lighter pairs (every
    fourth texel along every third row, each run two on from the last); on a side (`ribs`) two pressed ribs round it,
    lit along their tops."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                k = 2
                if y % 3 == 1 and (x + 2 * (y // 3)) % 4 in (0, 1):
                    k = 3
                if ribs and p.h >= 4 and y in (p.h // 3, 2 * p.h // 3):
                    k = 4
                elif ribs and p.h >= 4 and y in (p.h // 3 + 1, 2 * p.h // 3 + 1):
                    k = 1
                p.put(x, y, shade(ZINC, k))
    paint.tag = repr(("zinc", ribs))
    return paint


def rim(sc, name, painter, body, y0, y1, t):
    """A rim `t` thick from y0 to y1 along the outline of two crossed boxes (`body`: (x0, z0, x1, z1) each): a thin box
    along each long side of each, the shorter ones running a little past so their corners meet."""
    (ax0, az0, ax1, az1), (bx0, bz0, bx1, bz1) = body
    return [panel(sc, name, painter, (ax0, y0, bz0 + t), (ax0 + t, y1, bz1 - t)), panel(sc, name, painter, (ax1 - t, y0, bz0 + t), (ax1, y1, bz1 - t)),
            panel(sc, name, painter, (bx0, y0, bz0), (bx1, y1, bz0 + t)), panel(sc, name, painter, (bx0, y0, bz1 - t), (bx1, y1, bz1))]


def window_box():
    """The Window Box, facing north (its wall behind, to the south): a box of nailed boards painted sage green, 94 cm
    long, 25 cm deep and 25 cm high, two cleats behind it against the wall, and soil inside to a little under its rim
    (four boards round the top)."""
    spec = fg.PLANTERS["window_box"]
    sc = Sculpt("window_box", 2401, 64)
    boards = planks(PAINT, board=4, nails=True)
    soil_top = spec["soil"]
    el = [panel(sc, "box", boards, (0.5, 0.0, 12.0), (15.5, soil_top, 15.25), up=soil())]
    el += rim(sc, "rim", boards, ((0.5, 12.0, 15.5, 15.25), (0.5, 12.0, 15.5, 15.25)), soil_top, 4.0, 0.5)
    for x in (2.0, 13.0):
        el.append(panel(sc, "cleat", lengthwise(PAINT), (x, 0.5, 15.25), (x + 1.0, 3.5, 16.0)))
    sc.models["whole"] = el
    return sc


def half_barrel():
    """The Half-Barrel, 62 cm across and 45 cm high: oak staves round in two row bands of a circle, two iron hoops a
    quarter pixel proud of them, the soil inside and the staves' ends a rim round it."""
    spec = fg.PLANTERS["half_barrel_planter"]
    sc = Sculpt("half_barrel_planter", 2402, 64)
    side = staves()
    ends = lengthwise(WOOD)
    body = ((3.0, 4.0, 13.0, 12.0), (4.0, 3.0, 12.0, 13.0))
    top = spec["soil"]
    el = [panel(sc, "staves", side, (x0, 0.0, z0), (x1, top, z1), up=soil(), down=flat(WOOD)) for x0, z0, x1, z1 in body]
    el += rim(sc, "rim", side, body, top, 7.25, 0.75)
    for y in (1.0, 5.0):
        for x0, z0, x1, z1 in body:
            el.append(panel(sc, "hoop", flat(IRON), (x0 - 0.25, y, z0 - 0.25), (x1 + 0.25, y + 0.75, z1 + 0.25)))
    sc.models["whole"] = el
    return sc


def galvanized_tub():
    """The Galvanized Tub, an oval wash tub 75 by 50 cm and 28 cm high: spangled zinc with two pressed ribs round its
    sides, the soil inside, a rolled rim and a drop handle at each end."""
    spec = fg.PLANTERS["galvanized_tub_planter"]
    sc = Sculpt("galvanized_tub_planter", 2403, 64)
    body = ((2.0, 5.0, 14.0, 11.0), (3.0, 4.0, 13.0, 12.0))
    top = spec["soil"]
    el = [panel(sc, "tub", zinc(ribs=True), (x0, 0.0, z0), (x1, top, z1), up=soil(), down=zinc()) for x0, z0, x1, z1 in body]
    el += rim(sc, "rim", zinc(), body, top, 4.5, 0.5)
    for x0 in (1.25, 14.25):
        el.append(panel(sc, "handle", flat(IRON), (x0, 2.5, 6.5), (x0 + 0.5, 3.25, 9.5)))
    sc.models["whole"] = el
    return sc


_built = {}
BUILDERS = {"window_box": window_box, "half_barrel_planter": half_barrel, "galvanized_tub_planter": galvanized_tub}


def build(block):
    if block not in _built:
        _built[block] = ppd.planned(BUILDERS[block], 64)
    return _built[block]


def _image_of(name):
    return build(name).atlas.img if name in BUILDERS else None


DISPLAY = {"gui": {"rotation": [25, 225, 0], "translation": [0, 0, 0], "scale": [0.8, 0.8, 0.8]},
           "ground": {"rotation": [0, 0, 0], "translation": [0, 2, 0], "scale": [0.4, 0.4, 0.4]},
           "fixed": {"rotation": [0, 0, 0], "translation": [0, 0, 0], "scale": [0.7, 0.7, 0.7]},
           "thirdperson_righthand": {"rotation": [75, 45, 0], "translation": [0, 2, 0], "scale": [0.4, 0.4, 0.4]},
           "firstperson_righthand": {"rotation": [0, 45, 0], "translation": [0, 0, 0], "scale": [0.4, 0.4, 0.4]}}


def assets(root, write, lang):
    """Each planter's model, a blockstate turning it to its facing, and its item (the planter, empty)."""
    write = fa.closing_writer(write, _image_of)
    for block, spec in fg.PLANTERS.items():
        lang[f"block.{MOD}.{block}"] = spec["display"]
        sc = build(block)
        write(root / "models" / "block" / f"{block}.json", fa.model(block, d20d.separated(sc.models["whole"])))
        write(root / "blockstates" / f"{block}.json", {"variants": {
            f"facing={f}": {"model": rid(f"block/{block}"), **({"y": y} if y else {})} for f, y in FACINGS.items()}})
        write(root / "models" / "item" / f"{block}.json", fa.model(block, d20d.separated(sc.models["whole"]), DISPLAY))
        write(root / "items" / f"{block}.json", {"model": {"type": "minecraft:model", "model": rid(f"item/{block}")}})
    lang[f"message.{MOD}.planter.full"] = "Every spot in it is planted"


def loot(out, write):
    """Each planter drops itself (its flowers come back from its block entity)."""
    for block in fg.PLANTERS:
        write(out / f"{block}.json", {"type": "minecraft:block", "pools": [{
            "condition": {"type": "minecraft:survives_explosion"}, "entries": [{"type": "minecraft:item", "name": rid(block)}], "rolls": 1}],
            "random_sequence": rid(f"blocks/{block}")})


def tags(tags):
    """Wooden planters come off with an axe, the zinc tub with a pickaxe."""
    for block in fg.PLANTERS:
        tags.add("block", "minecraft:mineable/pickaxe" if "galvanized" in block else "minecraft:mineable/axe", rid(block))


def textures():
    return {("block", block): build(block).atlas.img for block in fg.PLANTERS}

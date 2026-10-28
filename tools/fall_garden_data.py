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
import decor17_data as d17d
import decor20_data as d20d
import fall_garden as fg
import flora_art as fa
import pumpkin_patch_data as ppd
from decor18_data import shifted as d18d_shifted
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


# ---------------------------------------------------------------- part 3: the porch

STRAW = pal("6a5420", "8a7030", "a88c40", "c4a854", "dcc46c")
EARS = pal("7a5a1a", "9a7624", "ba9432", "d4b044")
RIBBON = pal("5a1410", "801e16", "a42c1e", "c4422c")
VINE = pal("3a2a1e", "4e3a28", "644c34", "7a5e42")
BERRY_RED = pal("6a120a", "901c10", "b42a18", "d04424")
HUSK = pal("8a5a10", "b07818", "d09a26")
LEAF_GOLD = pal("6a5212", "8a6c1a", "a88a26", "c4a636")
# The quilt's four fabrics, each one colour stepped into tones: rust, mustard, cream and pine.
FABRICS = (pal("5a2214", "7a3020", "9a422c", "b45838"), pal("6a5014", "8a6a1e", "aa862a", "c4a03a"),
           pal("8a8270", "a89e88", "c4baa2", "dad2bc"), pal("1e3424", "2a4632", "3a5c42", "4e7456"))


def links():
    """A chain: its links seen edge-on and flat by turns along it, a lit texel and a dark one."""
    def paint(p):
        along_x = p.w >= p.h
        for y in range(p.h):
            for x in range(p.w):
                a = x if along_x else y
                p.put(x, y, shade(IRON, 3 if a % 2 == 0 else 1))
    paint.tag = "links"
    return paint


def ears():
    """A sheaf's heads of grain: kernels in a two-tone checker, lit along the top row."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                p.put(x, y, shade(EARS, 3 if y == 0 and p.h > 2 else 2 if (x + y) % 2 == 0 else 1))
    paint.tag = "ears"
    return paint


def plait():
    """Plaited straw: a herringbone of stalks, each row's run leaning the other way from the last, in two tones."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                lean = x + y if (y // 2) % 2 == 0 else x - y
                p.put(x, y, shade(STRAW, 3 if lean % 3 == 0 else 2))
    paint.tag = "plait"
    return paint


def patchwork():
    """A patchwork quilt: squares four texels across in its four fabrics in a steady order, each square's lower and
    right edge a tone darker (the seam), its top left texel lit."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                fabric = FABRICS[((x // 4) + 3 * (y // 4)) % 4]
                k = 1 if x % 4 == 3 or y % 4 == 3 else 3 if (x % 4, y % 4) == (0, 0) else 2
                p.put(x, y, shade(fabric, k))
    paint.tag = "patchwork"
    return paint


def swing():
    """The Porch Swing, facing north, two blocks across and two up: a slatted seat 1.5 m wide on a front apron, a back of
    three slats between two posts under a top rail, arms on front posts, and at each end two chains rising from the arm's
    front and the back post to a ring, and one chain on up to a hook in the ceiling (`hooks`, the block's own model; the
    rest, `whole`, is the swing's, drawn swinging by the renderer)."""
    spec = fg.SWING
    sc = Sculpt(spec["block"], 2404, 64)
    seat = planks(WOOD, board=3, nails=True)
    turned_ = lengthwise(WOOD)
    chain = links()
    el = [panel(sc, "seat", seat, (4.0, 7.0, 4.5), (28.0, 8.0, 11.25)),
          panel(sc, "apron", turned_, (4.0, 5.5, 4.0), (28.0, 7.0, 4.75)),
          panel(sc, "rail", turned_, (4.0, 16.5, 11.0), (28.0, 17.5, 12.5))]
    for x0 in (4.0, 27.0):
        el.append(panel(sc, "post", turned_, (x0, 5.5, 11.25), (x0 + 1.0, 16.5, 12.25)))
    for y in (9.0, 11.5, 14.0):
        el.append(panel(sc, "slat", turned_, (5.0, y, 11.5), (27.0, y + 1.5, 12.0)))
    for x0 in (2.75, 27.25):
        el.append(panel(sc, "arm", turned_, (x0, 11.0, 4.25), (x0 + 2.0, 11.75, 11.25)))
        el.append(panel(sc, "arm_post", turned_, (x0 + 0.5, 8.0, 4.75), (x0 + 1.5, 11.0, 5.75)))
    pz, py = spec["pivot"][1], spec["pivot"][0]
    tan = 0.41421356
    for x in (2.25, 29.75):
        # The V of each end: from the arm's front (z 5) and the back (z 11) up at 22.5 degrees to the ring.
        rise = 3.0 / tan
        length = (3.0 ** 2 + rise ** 2) ** 0.5
        for z, angle in ((5.0, 22.5), (11.0, -22.5)):
            el.append(panel(sc, "chain", chain, (x - 0.25, 12.0, z - 0.25), (x + 0.25, 12.0 + length, z + 0.25),
                            rot=fa.rotation((x, 12.0, z), "x", angle)))
        ring = 12.0 + rise
        el.append(panel(sc, "ring", flat(IRON), (x - 0.4, ring - 0.4, pz - 0.4), (x + 0.4, ring + 0.4, pz + 0.4)))
        el.append(panel(sc, "chain", chain, (x - 0.25, ring + 0.4, pz - 0.25), (x + 0.25, py, pz + 0.25)))
    sc.models["whole"] = el
    hooks = []
    for x in (2.25, 29.75):
        hooks.append(panel(sc, "plate", flat(IRON), (x - 0.75, 31.5, pz - 0.75), (x + 0.75, 32.0, pz + 0.75)))
        hooks.append(panel(sc, "eye", flat(IRON), (x - 0.3, 30.5, pz - 0.3), (x + 0.3, 31.5, pz + 0.3)))
    sc.models["hooks"] = hooks
    return sc


def stook():
    """The Wheat Stook: six sheaves stood together to dry, leaning in on one another, 95 cm high: four round the outside
    leaning in, two upright in the middle; each a bundle of stalks tied a third of the way up, under its heads of grain."""
    sc = Sculpt("wheat_stook", 2405, 64)
    stalks = ppd.hay(STRAW)
    head = ears()
    band = flat(STRAW)
    el = []
    sheaves = (((8.0, 4.5), "x", 22.5), ((8.0, 11.5), "x", -22.5), ((4.5, 8.0), "z", -22.5), ((11.5, 8.0), "z", 22.5),
               ((7.0, 8.0), None, 0), ((9.0, 8.0), None, 0))
    for (cx, cz), axis, angle in sheaves:
        rot = fa.rotation((cx, 0.0, cz), axis, angle) if axis else None
        el.append(panel(sc, "sheaf", stalks, (cx - 1.25, 0.0, cz - 1.25), (cx + 1.25, 11.0, cz + 1.25), rot=rot))
        el.append(panel(sc, "band", band, (cx - 1.5, 4.0, cz - 1.5), (cx + 1.5, 4.75, cz + 1.5), rot=rot))
        el.append(panel(sc, "heads", head, (cx - 1.5, 11.0, cz - 1.5), (cx + 1.5, 15.0, cz + 1.5), rot=rot))
    sc.models["whole"] = el
    return sc


def corn_dolly():
    """The Corn Dolly, facing north (its wall behind, to the south): a plaited straw harvest knot, its bell stepping in
    to a neck, tied with a red ribbon bow under its hanging loop, the ears of grain fanned under it, 50 cm long."""
    sc = Sculpt("corn_dolly", 2406, 64)
    straw = plait()
    el = []
    for k, (half, y0, y1) in enumerate(((2.5, 4.0, 6.0), (2.0, 6.0, 8.0), (1.5, 8.0, 9.5), (1.0, 9.5, 11.0))):
        el.append(panel(sc, "bell", straw, (8.0 - half, y0, 15.0 - 0.5 * half), (8.0 + half, y1, 16.0)))
    # The ears of grain fanned out under the bell, as a harvest knot's are.
    for x0, w in ((6.0, 1.25), (7.375, 1.25), (8.75, 1.25)):
        el.append(panel(sc, "ears", ears(), (x0, 1.0, 15.0), (x0 + w, 4.0, 16.0)))
    el.append(panel(sc, "ribbon", flat(RIBBON), (6.75, 11.0, 14.75), (9.25, 11.75, 16.0)))
    for x0 in (5.75, 9.25):
        el.append(panel(sc, "bow", flat(RIBBON), (x0, 10.75, 14.75), (x0 + 1.0, 12.0, 15.25)))
    el.append(panel(sc, "loop", flat(STRAW), (7.5, 11.75, 15.25), (8.5, 13.0, 16.0)))
    sc.models["whole"] = el
    return sc


def wreath():
    """The Bittersweet Wreath, facing north (its wall behind, to the south): a thick ring of twisted vine 69 cm across
    (four straight lengths and four corners, as an octagon is drawn in pixels), thick with yellow leaves and clusters of
    scarlet berries in their split orange husks all round it."""
    sc = Sculpt("bittersweet_wreath", 2407, 64)
    vine = lengthwise(VINE)
    el = []
    for lo, hi in (((5.0, 2.0, 14.25), (11.0, 4.25, 16.0)), ((5.0, 11.75, 14.25), (11.0, 14.0, 16.0)),
                   ((2.0, 5.0, 14.25), (4.25, 11.0, 16.0)), ((11.75, 5.0, 14.25), (14.0, 11.0, 16.0))):
        el.append(panel(sc, "vine", vine, lo, hi))
    for x0, y0 in ((3.25, 3.25), (10.25, 3.25), (3.25, 10.25), (10.25, 10.25)):
        el.append(panel(sc, "vine", vine, (x0, y0, 14.25), (x0 + 2.5, y0 + 2.5, 16.0)))
    leaf_top = flat(LEAF_GOLD)
    for x0, y0 in ((6.0, 12.5), (2.25, 7.5), (12.25, 6.0), (8.5, 2.25), (3.5, 11.5), (11.0, 2.75), (12.0, 9.75), (2.5, 4.25)):
        el.append(panel(sc, "leaf", leaf_top, (x0, y0, 13.75), (x0 + 1.5, y0 + 1.0, 14.25)))
    berry = flat(BERRY_RED)
    clusters = ((4.25, 5.0), (11.75, 5.0), (4.25, 11.0), (11.75, 11.0), (8.0, 3.0), (8.0, 13.0), (3.0, 8.0), (13.0, 8.0),
                (6.0, 3.5), (10.0, 12.5))
    for cx, cy in clusters:
        for dx, dy in ((0.0, 0.0), (0.9, -0.7), (-0.8, -0.6)):
            bx, by = cx + dx - 0.4, cy + dy - 0.4
            el.append(panel(sc, "berry", berry, (bx, by, 13.5), (bx + 0.8, by + 0.8, 14.25)))
        el.append(panel(sc, "husk", flat(HUSK), (cx - 0.75, cy + 0.4, 13.75), (cx + 0.75, cy + 0.75, 14.25)))
    sc.models["whole"] = el
    return sc


def quilt_rail():
    """The Quilt Rail, facing north: a length of porch rail (a top rail and a bottom rail between two posts, three
    square balusters) with a patchwork quilt hung over the top rail, down both sides to a little above the bottom one."""
    sc = Sculpt("quilt_rail", 2408, 64)
    wood = lengthwise(WOOD)
    quilt = patchwork()
    el = [panel(sc, "top_rail", wood, (0.0, 13.0, 7.0), (16.0, 14.5, 9.0)),
          panel(sc, "bottom_rail", wood, (0.0, 1.5, 7.25), (16.0, 2.5, 8.75))]
    for x0 in (0.5, 14.0):
        el.append(panel(sc, "post", wood, (x0, 0.0, 7.0), (x0 + 1.5, 13.0, 9.0)))
    for x0 in (4.5, 7.5, 10.5):
        el.append(panel(sc, "baluster", wood, (x0, 2.5, 7.5), (x0 + 1.0, 13.0, 8.5)))
    # The quilt: over the rail and down both faces, a quarter pixel off them.
    el.append(panel(sc, "quilt_top", quilt, (2.5, 14.5, 6.75), (13.5, 15.0, 9.25)))
    for z0, z1 in ((6.25, 6.75), (9.25, 9.75)):
        el.append(panel(sc, "quilt", quilt, (2.5, 5.0, z0), (13.5, 15.0, z1)))
    sc.models["whole"] = el
    return sc


_built = {}
BUILDERS = {"window_box": window_box, "half_barrel_planter": half_barrel, "galvanized_tub_planter": galvanized_tub,
            fg.SWING["block"]: swing, "wheat_stook": stook, "corn_dolly": corn_dolly, "bittersweet_wreath": wreath, "quilt_rail": quilt_rail}


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
    part3_assets(root, write, lang)


ITEM_DISPLAY = {"gui": {"rotation": [25, 225, 0], "translation": [0, 0, 0], "scale": [0.625, 0.625, 0.625]},
                "ground": {"rotation": [0, 0, 0], "translation": [0, 2, 0], "scale": [0.3, 0.3, 0.3]},
                "fixed": {"rotation": [0, 0, 0], "translation": [0, 0, 0], "scale": [0.6, 0.6, 0.6]},
                "thirdperson_righthand": {"rotation": [75, 45, 0], "translation": [0, 2, 0], "scale": [0.35, 0.35, 0.35]},
                "firstperson_righthand": {"rotation": [0, 45, 0], "translation": [0, 0, 0], "scale": [0.35, 0.35, 0.35]}}


def part3_assets(root, write, lang):
    """The Porch Swing's four blocks' models (its hooks, cut from the whole: nothing in its lower blocks) and its swing's
    (the blockstate's `runner`, drawn by client/PorchSwingRenderer from its first block, so moved half the frame along);
    and the rest of the porch, each one model turned to its facing. Their items show them whole."""
    models = root / "models" / "block"
    states = root / "blockstates"
    spec = fg.SWING
    block = spec["block"]
    lang[f"block.{MOD}.{block}"] = spec["display"]
    sc = build(block)
    n = len(d17d.cells(spec["size"]))
    for part, elements in enumerate(ppd.cut(sc.models["hooks"], spec["size"])):
        write(models / f"{block}_{part}.json", fa.model(block, elements))
    write(models / f"{block}_swing.json", fa.model(block, d18d_shifted(d20d.separated(sc.models["whole"]), -16.0, 0, 0)))
    variants = {}
    for f, y in FACINGS.items():
        turn = {"y": y} if y else {}
        for p in range(n):
            for lit in ("false", "true"):
                variants[f"facing={f},lit={lit},part={p},runner=false"] = {"model": rid(f"block/{block}_{p}"), **turn}
                variants[f"facing={f},lit={lit},part={p},runner=true"] = {"model": rid(f"block/{block}_swing"), **turn}
    write(states / f"{block}.json", {"variants": variants})
    whole = sc.models["whole"] + sc.models["hooks"]
    write(root / "models" / "item" / f"{block}.json",
          fa.model(block, fa.transformed(d17d.pinned(whole), 0.5, (8 - 8 * 2 * 0.5, 0, 8 - 8 * 0.5), (0, 0, 0)), ITEM_DISPLAY))
    write(root / "items" / f"{block}.json", {"model": {"type": "minecraft:model", "model": rid(f"item/{block}")}})
    for block, info in fg.PORCH.items():
        lang[f"block.{MOD}.{block}"] = info["display"]
        sc = build(block)
        write(models / f"{block}.json", fa.model(block, d20d.separated(sc.models["whole"])))
        write(states / f"{block}.json", {"variants": {
            f"facing={f}": {"model": rid(f"block/{block}"), **({"y": y} if y else {})} for f, y in FACINGS.items()}})
        write(root / "models" / "item" / f"{block}.json", fa.model(block, d20d.separated(sc.models["whole"]), DISPLAY))
        write(root / "items" / f"{block}.json", {"model": {"type": "minecraft:model", "model": rid(f"item/{block}")}})


def loot(out, write):
    """Each planter drops itself (its flowers come back from its block entity), and so does each piece of the porch; the
    swing drops once, from its first block."""
    block = fg.SWING["block"]
    write(out / f"{block}.json", {"type": "minecraft:block", "pools": [{
        "condition": {"type": "minecraft:all_of", "terms": [{"type": "minecraft:survives_explosion"},
                                                            {"type": "minecraft:match_block", "blocks": rid(block), "state": {"part": "0"}}]},
        "entries": [{"type": "minecraft:item", "name": rid(block)}], "rolls": 1}], "random_sequence": rid(f"blocks/{block}")})
    for block in list(fg.PLANTERS) + list(fg.PORCH):
        write(out / f"{block}.json", {"type": "minecraft:block", "pools": [{
            "condition": {"type": "minecraft:survives_explosion"}, "entries": [{"type": "minecraft:item", "name": rid(block)}], "rolls": 1}],
            "random_sequence": rid(f"blocks/{block}")})


def tags(tags):
    """Wood comes off with an axe, the zinc tub with a pickaxe, and straw and vine with a hoe."""
    for block in list(fg.PLANTERS) + [fg.SWING["block"], "quilt_rail"]:
        tags.add("block", "minecraft:mineable/pickaxe" if "galvanized" in block else "minecraft:mineable/axe", rid(block))
    for block in ("wheat_stook", "corn_dolly", "bittersweet_wreath"):
        tags.add("block", "minecraft:mineable/hoe", rid(block))


def textures():
    return {("block", block): build(block).atlas.img for block in BUILDERS}

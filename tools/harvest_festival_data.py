"""Generated assets for the Harvest Festival Grounds, decor set 10 (tools/harvest_festival.py;
docs/features/harvest-festival-grounds.md).

Built from boxes at real size, one texel to a model pixel, in the Pumpkin Patch Market's manner, whose wood, iron and
pumpkins it shares (tools/pumpkin_patch_data.py, tools/haunted_midway_data.py).
- Willow (the cornucopia's horn): basket weave, its weavers two texels deep passing over two stakes and under two, each
  row of them a step along from the last, lit where they pass over and in shadow where the stakes show; its rim a braid
  on the slant. Inside the horn the same weave in shadow.
- The harvest, each at its own size and in its own colours, flat as fruit is and lit along its top: a pumpkin's rind
  ribbed up it (a rib lit, a groove in shadow); an acorn squash's deep green ribs; ornamental gourds striped yellow and
  green; corn in rows of kernels, its husks pale straw; wheat in golden stalks under bearded ears; grapes in a bunch, lit
  and shaded by turns; apples red, a glint at the top.
A prop of several blocks is modelled whole in its frame (16 pixels a block) and cut into its blocks' models
(pumpkin_patch_data.cut), so its picture runs on unbroken across them.
"""
import decor17_data as d17d
import flora_art as fa
import harvest_festival as hf
import pumpkin_patch_data as ppd
from flora_art import Sculpt, pal, shade
from haunted_midway_data import PUMPKIN, STEM
from pumpkin_patch_data import lengthwise, panel

MOD = "jugcraft"
FACINGS = ppd.FACINGS
WILLOW = pal("4a3418", "6a4c24", "8c6a34", "ae8a48", "cca862")
BUTTERNUT = pal("9c6c34", "bc8c50", "d8aa6c", "ecc48c")
ACORN = pal("142212", "20341a", "2e4826", "3e5c32")
GOURD_YELLOW = pal("b08a1c", "d4ac30", "ecc84c")
GOURD_GREEN = pal("2c4a16", "3e6420", "52802c")
CORN = pal("b8860e", "d8a420", "f0c444", "f8dc78")
HUSK = pal("a89466", "c4b282", "dccca0")
WHEAT = pal("9a7426", "bc9438", "d8b04e", "ecca6c")
GRAPE = pal("2a1430", "3e1e48", "56306a", "70468a")
APPLE = pal("6e1010", "981a16", "be2a20", "dc4a34")
LEAF = pal("2a4a18", "3c6422", "52802e")


def rid(path):
    return path if ":" in path else f"{MOD}:{path}"


# ---------------------------------------------------------------- painters

def wicker(shadowed=False):
    """Woven willow: its weavers run round the horn in bands two texels wide (across the piece), each passing over three
    stakes and under one, where the stake shows in shadow, each band's stakes two along from the last's; a band's first
    column lit, its second its own tone. `shadowed`, two tones darker (the inside of the horn)."""
    def paint(p):
        drop = 2 if shadowed else 0
        for y in range(p.h):
            for x in range(p.w):
                stake = (y + 2 * (x // 2)) % 4 == 3
                p.put(x, y, shade(WILLOW, max(0, (1 if stake else (3, 2)[x % 2]) - drop)))
    paint.tag = repr(("wicker", shadowed))
    return paint


def crossed_x(sc, name, painter, x0, x1, cy, cz, d, rot=None, **kw):
    """A round length of horn `d` across lying along x from x0 to x1 about (cy, cz): two boxes crossed, each a fifth of
    its width shorter at its corners (to the half pixel), so it reads as an octagon end on; `rot` turns both alike."""
    r, c = d / 2, round(d / 5 * 2) / 2
    return [panel(sc, name, painter, (x0, cy - r, cz - r + c), (x1, cy + r, cz + r - c), rot=rot, **kw),
            panel(sc, name, painter, (x0, cy - r + c, cz - r), (x1, cy + r - c, cz + r), rot=rot, **kw)]


def crossed_y(sc, name, painter, cx, cz, d, y0, y1, **kw):
    """A round thing `d` across standing from y0 to y1 about (cx, cz): two boxes crossed, each a fifth of its width
    shorter at its corners (to the half pixel)."""
    r, c = d / 2, round(d / 5 * 2) / 2
    return [panel(sc, name, painter, (cx - r + c, y0, cz - r), (cx + r - c, y1, cz + r), **kw),
            panel(sc, name, painter, (cx - r, y0, cz - r + c), (cx + r, y1, cz + r - c), **kw)]


def braid():
    """The horn's braided rim: willow plaited on the slant, a strand lit, its own tone, then shadow, over and over."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                p.put(x, y, shade(WILLOW, (4, 3, 1)[(x + y) % 3]))
    paint.tag = repr(("braid",))
    return paint


def rind():
    """A pumpkin's rind: ribs running up it, a column lit, the next its own tone, a groove in shadow, its own tone
    again, over and over; its top row lit."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                p.put(x, y, shade(PUMPKIN, 3 if y == 0 and p.h > 1 else (2, 3, 2, 1)[x % 4]))
    paint.tag = repr(("rind",))
    return paint


def produce(palette, glint=False):
    """Smooth produce (a squash's skin, an apple): flat in its own tone, lit along its top row; `glint`, a glint in its
    top left texel."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                if glint and (x, y) == (0, 0):
                    p.put(x, y, shade(palette, len(palette) - 1))
                else:
                    p.put(x, y, shade(palette, 2 if y == 0 and p.h > 1 else 1))
    paint.tag = repr(("produce", palette, glint))
    return paint


def acorn_squash():
    """An acorn squash: deep green, a rib a tone lighter every third column, its top row lit, a patch of orange at the
    middle of its foot."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                if y == p.h - 1 and x == p.w // 2 and p.h > 2:
                    p.put(x, y, shade(PUMPKIN, 2))
                else:
                    p.put(x, y, shade(ACORN, 3 if y == 0 and p.h > 1 else 2 if x % 3 == 0 else 1))
    paint.tag = repr(("acorn_squash",))
    return paint


def gourd():
    """An ornamental gourd: striped yellow and green, a stripe a texel wide, its top row lit."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                palette = GOURD_YELLOW if x % 2 == 0 else GOURD_GREEN
                p.put(x, y, shade(palette, 2 if y == 0 and p.h > 1 else 1))
    paint.tag = repr(("gourd",))
    return paint


def kernels():
    """An ear of corn: its kernels in rows, each a texel, lit and shaded by turns, each row a texel along from the last."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                p.put(x, y, shade(CORN, 3 if (x + y) % 2 == 0 else 1))
    paint.tag = repr(("kernels",))
    return paint


def ears():
    """A sheaf's ears of wheat: gold, bearded, a beard texel lit on every other column of its top row."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                p.put(x, y, shade(WHEAT, 3 if y == 0 and x % 2 == 0 else 2 if (x + y) % 2 == 0 else 1))
    paint.tag = repr(("ears",))
    return paint


def grapes():
    """A bunch of grapes: a grape lit, the next in shadow, by turns across and down."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                p.put(x, y, shade(GRAPE, 3 if (x + y) % 2 == 0 else 1))
    paint.tag = repr(("grapes",))
    return paint


# ---------------------------------------------------------------- part 1: the Giant Cornucopia

# The horn behind its mouth, as it narrows to the east: (from x, to x, across, for each length of it); it lies on the
# ground, so each is centred half its width up. Its depth's middle.
HORN = ((9.0, 15.0, 18.0), (15.0, 21.0, 16.0), (21.0, 26.0, 13.0), (26.0, 31.0, 11.0), (31.0, 35.0, 9.0))
HORN_Z = 17.0
# The rim's west face: the horn's length is measured from here to the back of its tail.
RIM_X = 2.0


def giant_cornucopia():
    """The Giant Cornucopia, facing north, in its frame (48 across, 32 up, 32 deep; its mouth to the west).
    - The horn: its mouth 1.25 m across, four willow walls round its opening behind a braided rim; behind it, lengths of
      willow narrowing to the east; then its tail curling up off the ground, rising, standing up and turning back west
      over it to a tip (each length turned as a block model's turns allow, 22.5 or 45 degrees).
    - The harvest spilling from its mouth: a big pumpkin (50 cm) on the ground before it with a butternut squash lying on
      it, a smaller pumpkin in the mouth with an acorn squash on it, a sheaf of wheat standing in the mouth, three ears
      of corn in their husks leaning out over the rim, a bunch of grapes hanging over it, a third pumpkin, two gourds and
      four apples rolled out over the ground."""
    spec = hf.CORNUCOPIA
    sc = Sculpt(spec["block"], 3301, 128)
    weave, inside, rim = wicker(), wicker(shadowed=True), braid()
    z = HORN_Z
    el = []
    # The mouth: an octagon of willow walls 20 pixels across round its opening, its four corners turned 45 degrees, and
    # its braided rim round its front the same way.
    cy, r, cut, t = 11.0, spec["mouth"] / 2, 4.0, 1.5
    for x0, x1, paint, wall, name in ((RIM_X + 1, HORN[0][0], weave, t, "mouth"), (RIM_X, RIM_X + 2, rim, 2.0, "rim")):
        grow = 1.0 if name == "rim" else 0.0
        rr = r + grow
        el += [panel(sc, name, paint, (x0, cy + rr - wall, z - rr + cut), (x1, cy + rr, z + rr - cut), down=inside if name == "mouth" else paint),
               panel(sc, name, paint, (x0, cy - rr, z - rr + cut), (x1, cy - rr + wall, z + rr - cut), up=inside if name == "mouth" else paint),
               panel(sc, name, paint, (x0, cy - rr + cut, z - rr), (x1, cy + rr - cut, z - rr + wall), south=inside if name == "mouth" else paint),
               panel(sc, name, paint, (x0, cy - rr + cut, z + rr - wall), (x1, cy + rr - cut, z + rr), north=inside if name == "mouth" else paint)]
        diagonal = cut * 1.41421356
        for sy, sz in ((1, -1), (1, 1), (-1, -1), (-1, 1)):
            # Each corner's outer face on the octagon's edge, its ends meeting the flat walls' outer faces.
            inset = rr - cut / 2 - wall / (2 * 1.41421356)
            my, mz = cy + sy * inset, z + sz * inset
            el.append(panel(sc, name, paint, (x0, my - wall / 2, mz - diagonal / 2), (x1, my + wall / 2, mz + diagonal / 2),
                            rot=fa.rotation((x0, my, mz), "x", 45.0 * sy * sz)))
    # The horn narrowing behind it, round, its first length's west face the dark inside seen through the mouth.
    for i, (x0, x1, d) in enumerate(HORN):
        over = {"west": inside} if i == 0 else {}
        el += crossed_x(sc, "horn", weave, x0, x1, 0.5 + d / 2, z, d, **over)
    # The tail curling up: rising off the ground, then steeply, standing up, and turning back west over the horn to its tip.
    el += crossed_x(sc, "tail", weave, 35.0, 39.0, 4.5, z, 7.0, rot=fa.rotation((35.0, 4.5, z), "z", 22.5))
    el += crossed_x(sc, "tail", weave, 38.0, 42.0, 7.0, z, 6.0, rot=fa.rotation((38.0, 7.0, z), "z", 45.0))
    el += crossed_y(sc, "tail", weave, 42.5, z, 5.0, 9.0, 18.0)
    el.append(panel(sc, "tail", weave, (38.0, 15.5, z - 2.0), (42.0, 19.5, z + 2.0), rot=fa.rotation((42.0, 17.5, z), "z", -45.0)))
    el.append(panel(sc, "tail", weave, (35.0, 20.0, z - 1.5), (38.0, 23.0, z + 1.5), rot=fa.rotation((38.0, 21.5, z), "z", -22.5)))
    # The harvest.
    pumpkin, stem = rind(), lengthwise(STEM)
    el += crossed_y(sc, "pumpkin", pumpkin, 4.5, 12.0, 8.0, 0.5, 7.5)
    el += crossed_y(sc, "pumpkin", pumpkin, 7.0, 21.0, 6.0, 2.5, 7.5)
    el += crossed_y(sc, "pumpkin", pumpkin, 14.5, 3.5, 5.0, 0.5, 5.0)
    el += [panel(sc, "stem", stem, (4.0, 7.5, 13.75), (5.0, 9.0, 14.75)),
           panel(sc, "stem", stem, (14.0, 5.0, 3.0), (15.0, 6.5, 4.0))]
    butternut = produce(BUTTERNUT)
    el += [panel(sc, "butternut", butternut, (0.0, 7.5, 10.5), (3.0, 10.5, 13.5)),
           panel(sc, "butternut", butternut, (3.0, 8.0, 11.0), (6.0, 10.0, 13.0))]
    el.append(panel(sc, "acorn_squash", acorn_squash(), (5.0, 7.5, 19.0), (9.0, 10.5, 23.0)))
    # The sheaf of wheat standing in the mouth, its stalks bound, its ears above.
    el += [panel(sc, "sheaf", lengthwise(WHEAT), (4.0, 9.0, 13.0), (7.0, 16.0, 16.0)),
           panel(sc, "sheaf_band", lengthwise(STEM), (3.75, 11.0, 12.75), (7.25, 12.0, 16.25)),
           panel(sc, "ears", ears(), (3.5, 16.0, 12.5), (7.5, 18.5, 16.5))]
    # Three ears of corn leaning out over the rim, each over its husk.
    corn, husk = kernels(), lengthwise(HUSK)
    for cy, cz in ((10.0, 17.5), (11.5, 20.5), (10.5, 23.0)):
        turn = fa.rotation((4.0, cy, cz), "z", -22.5)
        el.append(panel(sc, "corn", corn, (-1.0, cy, cz - 0.5), (4.0, cy + 1.0, cz + 0.5), rot=turn))
        el.append(panel(sc, "husk", husk, (0.0, cy - 0.5, cz - 1.0), (4.0, cy, cz + 1.0), rot=turn))
    # The grapes hanging over the rim, their stalk up to it.
    el += [panel(sc, "grapes", grapes(), (0.5, 3.0, 22.5), (3.0, 6.0, 25.0)),
           panel(sc, "grape_stalk", lengthwise(STEM), (1.5, 6.0, 23.5), (2.0, 8.0, 24.0))]
    # Gourds and apples rolled out over the ground.
    el += [panel(sc, "gourd", gourd(), (8.0, 0.5, 3.0), (10.0, 2.5, 5.0)),
           panel(sc, "gourd", gourd(), (10.5, 0.5, 7.0), (12.5, 2.5, 9.0))]
    apple = produce(APPLE, glint=True)
    for ax, az in ((1.0, 5.0), (3.0, 2.5), (6.5, 1.5), (10.5, 3.5)):
        el.append(panel(sc, "apple", apple, (ax, 0.5, az), (ax + 1.0, 1.5, az + 1.0)))
    sc.models["whole"] = el
    return sc


# ---------------------------------------------------------------- building and files

_built = {}
BUILDERS = {hf.CORNUCOPIA["block"]: giant_cornucopia}


def build(block):
    if block not in _built:
        _built[block] = ppd.planned(BUILDERS[block], 128)
    return _built[block]


def _image_of(name):
    return build(name).atlas.img if name in BUILDERS else None


DISPLAY = {"gui": {"rotation": [25, 225, 0], "translation": [0, 0, 0], "scale": [0.625, 0.625, 0.625]},
           "ground": {"rotation": [0, 0, 0], "translation": [0, 2, 0], "scale": [0.3, 0.3, 0.3]},
           "fixed": {"rotation": [0, 0, 0], "translation": [0, 0, 0], "scale": [0.6, 0.6, 0.6]},
           "thirdperson_righthand": {"rotation": [0, 45, 0], "translation": [0, 2, 0], "scale": [0.4, 0.4, 0.4]},
           "firstperson_righthand": {"rotation": [0, 45, 0], "translation": [0, 2, 0], "scale": [0.4, 0.4, 0.4]}}


def item_model(elements, size, scale):
    """The item: the whole prop scaled into one block about its middle."""
    return fa.transformed(d17d.pinned(elements), scale, (8 - 8 * size[0] * scale, 0, 8 - 8 * size[2] * scale), (0, 0, 0))


def assets(root, write, lang):
    """The cornucopia's blocks' models, cut from the whole; its blockstate by facing, part and `lit` (it has no lanterns,
    so `lit` shows nothing); and its item, the whole at a third of its size."""
    write = fa.closing_writer(write, _image_of)
    models = root / "models" / "block"
    spec = hf.CORNUCOPIA
    block = spec["block"]
    lang[f"block.{MOD}.{block}"] = spec["display"]
    sc = build(block)
    n = len(d17d.cells(spec["size"]))
    for part, elements in enumerate(ppd.cut(sc.models["whole"], spec["size"])):
        write(models / f"{block}_{part}.json", fa.model(block, elements))
    write(root / "blockstates" / f"{block}.json", {"variants": {
        f"facing={f},lit={lit},part={p}": {"model": rid(f"block/{block}_{p}"), **({"y": FACINGS[f]} if FACINGS[f] else {})}
        for f in FACINGS for lit in ("false", "true") for p in range(n)}})
    write(root / "models" / "item" / f"{block}.json", fa.model(block, item_model(sc.models["whole"], spec["size"], 1 / 3), DISPLAY))
    write(root / "items" / f"{block}.json", {"model": {"type": "minecraft:model", "model": rid(f"item/{block}")}})


def loot(out, write):
    """The cornucopia drops itself, from its first block only (its other blocks break with it, dropping nothing)."""
    block = hf.CORNUCOPIA["block"]
    write(out / f"{block}.json", {"type": "minecraft:block", "pools": [{"condition": {"type": "minecraft:all_of", "terms": [
        {"type": "minecraft:survives_explosion"},
        {"type": "minecraft:match_block", "blocks": rid(block), "state": {"part": "0"}}]},
        "entries": [{"type": "minecraft:item", "name": rid(block)}], "rolls": 1}], "random_sequence": rid(f"blocks/{block}")})


def tags(tags):
    tags.add("block", "minecraft:mineable/axe", rid(hf.CORNUCOPIA["block"]))


def textures():
    return {("block", block): build(block).atlas.img for block in hf.blocks()}

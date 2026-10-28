"""Generated data for Orchard & Apple Harvest (decor set 2; tools/orchard_harvest.py): the Orchard Ladder, the Apple Bin
and the Picking Bag, sculpted on the toolkit of tools/flora_art.py and tools/pumpkin_patch_data.py (each block one
texture painted here), with their blockstates, items, names, loot and tags.

How they are drawn (docs/ART_DIRECTION.md):
- Real-life sized: the ladder nine feet tall, the bin 1.2 m square on its pallet, the bag a picker's bag on a wall hook.
  A prop of several blocks is modelled whole in its frame and cut into its blocks' models (pumpkin_patch_data.cut);
  the ladder's leaning pole is built of one turned box a block, each turned about its own foot so it lies wholly in
  that block.
- Built from boxes with shape: tapering rails stepped in a pixel at a time, flat steps, a cap and an iron hinge; a
  pallet of skids and deck boards under slatted sides on corner posts; a canvas bag with a reinforced foot and an iron
  hoop mouth, hung by its harness. Hollows (the bin, the bag) are walls round them with their insides drawn.
- Clean textures at vanilla's density (one texel to a model pixel): the displays' wood and iron
  (pumpkin_patch_data), canvas in flat tones lit along its top, and fruit heaped as a staggered lattice of round
  fruit, each lit at its top left with its stalk, the gaps between in shadow. No random shade at any texel.

Everything is drawn here by code; no Mojang texture is read, traced or copied.
"""
import math

import decor17_data as d17d
import flora_art as fa
import orchard_harvest as oh
import pumpkin_patch_data as ppd
from flora_art import Sculpt, cube, pal, rotation, shade
from pumpkin_patch_data import IRON, WOOD, flat, lengthwise, panel, planks

MOD = "jugcraft"
FACINGS = ppd.FACINGS
rid = ppd.rid

CANVAS = pal("8a7a58", "a2916a", "b8a67c", "cbb98e", "dccaa0")
# The fruit in a heap, darkest first: red apples, gold-green pears; the stalk a dark brown.
FRUIT = {"apple": pal("4a1210", "741d17", "9c2a1e", "bf3d26", "d9623a"),
         "pear": pal("3e4214", "6b7a24", "9aa636", "bcc24e", "dcd978")}
STALK = pal("2e2014", "4a3420")


# ---------------------------------------------------------------- painters

def canvas():
    """Canvas: one flat tone, lit along its top row and shaded along its bottom one."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                p.put(x, y, shade(CANVAS, 3 if y == 0 and p.h > 2 else 1 if y == p.h - 1 and p.h > 2 else 2))
    paint.tag = repr(("canvas",))
    return paint


def heap(fruit):
    """Fruit heaped: round fruit four texels across on a staggered lattice (each row half a fruit along from the last),
    the gaps between them in shadow. Each fruit is one flat tone with a single lit texel at its top left and its bottom
    right edge a tone darker; every other one shows its stalk, a dark texel at its top. The lattice repeats every 16
    texels, so a heap of any size carries on round its corners."""
    tones = FRUIT[fruit]
    disc = (".##.", "####", "####", ".##.")

    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                p.put(x, y, shade(tones, 0))
        for row in range(-1, p.h // 4 + 2):
            for col in range(-1, p.w // 4 + 2):
                ox, oy = col * 4 + (2 if row % 2 else 0), row * 4
                for j, line in enumerate(disc):
                    for i, ch in enumerate(line):
                        x, y = ox + i, oy + j
                        if ch != "#" or not (0 <= x < p.w and 0 <= y < p.h):
                            continue
                        k = 4 if (i, j) == (1, 1) else 2 if (i, j) in ((3, 2), (2, 3), (3, 1), (1, 3)) else 3
                        p.put(x, y, shade(tones, k))
                if 0 <= ox + 2 < p.w and 0 <= oy < p.h and (row + col) % 2 == 0:
                    p.put(ox + 2, oy, shade(STALK, 1))
    paint.tag = repr(("heap", fruit))
    return paint


# ---------------------------------------------------------------- the Orchard Ladder

def pole_segments(sc, pivot, length, lean, splits):
    """A leaning pole 1.5 pixels square from `pivot` (y, z) down and back `lean` degrees, `length` long, as one turned
    box between each pair of `splits` (heights where it crosses into the next block down), each box turned about its
    own foot so it lies wholly in the block its foot is in."""
    py, pz = pivot
    c, s = math.cos(math.radians(lean)), math.sin(math.radians(lean))
    heights = [py] + list(splits) + [None]
    out = []
    for top_y, foot_y in zip(heights, heights[1:]):
        s0 = (py - top_y) / c
        s1 = length if foot_y is None else (py - foot_y) / c
        foot = (8.0, py - s1 * c, pz + s1 * s)
        el = panel(sc, "pole", lengthwise(WOOD), (7.25, foot[1], foot[2] - 0.75), (8.75, foot[1] + (s1 - s0), foot[2] + 0.75),
                   rot=rotation(foot, "x", -lean))
        out.append(el)
    return out


def ladder():
    """The Orchard Ladder, facing north: two rails tapering a pixel in every few pixels up (stepped, as a line is drawn
    in pixels), flat steps between them, iron shoes on their feet, a cap over their tops with an iron hinge strap, and
    the pole hinged there leaning back to the ground, an iron shoe on its foot."""
    spec = oh.LADDER
    sc = Sculpt(spec["block"], 2201, 64)
    z0, z1 = spec["face_z"]
    h, base, top = spec["height"], spec["base_width"], spec["top_width"]
    inset_total = (base - top) / 2
    segments = int(round(inset_total)) + 1
    seg_h = h / segments
    el = []

    def rails_at(y):
        k = min(segments - 1, int(y // seg_h))
        return 1.0 + k, 15.0 - k
    for k in range(segments):
        y0, y1 = k * seg_h, min(h, (k + 1) * seg_h + 0.5)
        left = 1.0 + k
        right = 15.0 - k
        el.append(panel(sc, "rail", lengthwise(WOOD), (left, y0, z0), (left + 1.5, y1, z1)))
        el.append(panel(sc, "rail", lengthwise(WOOD), (right - 1.5, y0, z0), (right, y1, z1)))
    step = spec["step"]
    y = step
    while y < h - 3:
        left, right = rails_at(y)
        el.append(panel(sc, "step", planks(WOOD, board=3, nails=False), (left + 1.5, y, z0 - 0.5), (right - 1.5, y + 1.0, z1 + 0.5)))
        y += step
    for x in (0.75, 13.25):
        el.append(panel(sc, "shoe", flat(IRON), (x, 0.0, z0 - 0.25), (x + 2.0, 1.25, z1 + 0.25)))
    hy, hz = spec["hinge"]
    el.append(panel(sc, "cap", planks(WOOD, board=2, nails=False), (4.0, h - 1.5, z0 - 0.5), (12.0, h + 1.0, z1 + 1.0)))
    el.append(panel(sc, "hinge", flat(IRON), (6.5, hy - 1.25, z1 + 1.0), (9.5, hy + 1.25, z1 + 1.4)))
    length = hy / math.cos(math.radians(spec["pole_lean"]))
    el += pole_segments(sc, (hy, hz + 0.75), length, spec["pole_lean"], (32.25, 16.25))
    sc.models["whole"] = el
    return sc


# ---------------------------------------------------------------- the Apple Bin

SLATS = ((2.75, 4.75), (5.75, 7.75), (8.75, 10.75), (11.75, 13.75))


def heap_top(stage):
    """The top of the heap at `stage` (1 to BIN["stages"]), in pixels."""
    return 2.75 + stage * (13.75 - 2.75) / oh.BIN["stages"]


def apple_bin():
    """The Apple Bin, facing north: a pallet of three skids and five deck boards, four corner posts, four slats a side
    with a pixel's gap between, and a board floor; its fruit (`heaps`, by stage and fruit) drawn separately."""
    spec = oh.BIN
    sc = Sculpt(spec["block"], 2202, 128)
    x0, _, z0, x1, _, z1 = spec["box"]
    el = []
    for x in (7.0, 15.0, 23.0):
        el.append(panel(sc, "skid", lengthwise(WOOD), (x, 0.0, z0), (x + 2.0, 1.5, z1)))
    for k in range(5):
        za = z0 + k * 4.0
        el.append(panel(sc, "deck", planks(WOOD, board=3, nails=True), (x0, 1.5, za), (x1, 2.25, za + 3.0)))
    for x, z in ((x0, z0), (x1 - 1.5, z0), (x0, z1 - 1.5), (x1 - 1.5, z1 - 1.5)):
        el.append(panel(sc, "post", lengthwise(WOOD), (x, 2.25, z), (x + 1.5, 14.0, z + 1.5)))
    t = 0.75
    for ya, yb in SLATS:
        el.append(panel(sc, "slat", planks(WOOD, board=2, nails=True), (x0 + 1.5, ya, z0), (x1 - 1.5, yb, z0 + t)))
        el.append(panel(sc, "slat", planks(WOOD, board=2, nails=True), (x0 + 1.5, ya, z1 - t), (x1 - 1.5, yb, z1)))
        el.append(panel(sc, "slat", planks(WOOD, board=2, nails=True), (x0, ya, z0 + 1.5), (x0 + t, yb, z1 - 1.5)))
        el.append(panel(sc, "slat", planks(WOOD, board=2, nails=True), (x1 - t, ya, z0 + 1.5), (x1, yb, z1 - 1.5)))
    el.append(panel(sc, "floor", planks(WOOD, board=3, nails=False), (x0 + t, 2.25, z0 + t), (x1 - t, 2.75, z1 - t)))
    sc.models["whole"] = el
    for fruit in oh.FRUITS:
        for stage in range(1, spec["stages"] + 1):
            top_y = heap_top(stage)
            boxes = [panel(sc, f"heap_{fruit}", heap(fruit), (x0 + t + 0.05, 2.75, z0 + t + 0.05), (x1 - t - 0.05, top_y, z1 - t - 0.05),
                           sides=("north", "south", "east", "west", "up"))]
            if stage == spec["stages"]:
                boxes.append(panel(sc, f"heap_{fruit}", heap(fruit), (x0 + 4.0, top_y, z0 + 4.0), (x1 - 4.0, top_y + 1.75, z1 - 4.0),
                                   sides=("north", "south", "east", "west", "up")))
            sc.models[f"heap_{fruit}_{stage}"] = boxes
    return sc


# ---------------------------------------------------------------- the Picking Bag

def bag_top(stage):
    return {1: 6.0, 2: 9.5}[stage]


def picking_bag():
    """The Picking Bag, facing north on the wall to its south: an iron hook (a plate on the wall, an arm and its upturned
    tip), the harness strap looped over it and down to the hoop, the canvas bag (a reinforced foot, walls standing on it)
    and the iron hoop round its mouth; its fruit (`heaps`, by stage and fruit) drawn separately."""
    sc = Sculpt(oh.BAG["block"], 2203, 64)
    el = [panel(sc, "plate", flat(IRON), (7.0, 12.5, 15.5), (9.0, 15.0, 16.0)),
          panel(sc, "arm", flat(IRON), (7.5, 13.25, 13.25), (8.5, 14.25, 15.5)),
          panel(sc, "tip", flat(IRON), (7.5, 14.25, 13.25), (8.5, 15.25, 14.0)),
          panel(sc, "strap", canvas(), (5.5, 9.5, 13.6), (6.5, 14.25, 14.4)),
          panel(sc, "strap", canvas(), (9.5, 9.5, 13.6), (10.5, 14.25, 14.4)),
          panel(sc, "strap", canvas(), (6.5, 14.25, 13.5), (9.5, 14.85, 14.5))]
    x0, z0, x1, z1 = 4.5, 9.0, 11.5, 15.0
    el.append(panel(sc, "band", canvas(), (x0 - 0.25, 2.0, z0 - 0.25), (x1 + 0.25, 3.25, z1 + 0.25)))
    el += ppd.walls(sc, "bag", canvas(), x0, z0, x1, z1, 0.5, 3.25, 9.0)
    el += ppd.walls(sc, "hoop", flat(IRON), x0 - 0.25, z0 - 0.25, x1 + 0.25, z1 + 0.25, 0.75, 9.0, 9.75)
    sc.models["whole"] = el
    for fruit in oh.FRUITS:
        for stage in (1, 2):
            top_y = bag_top(stage)
            boxes = [panel(sc, f"heap_{fruit}", heap(fruit), (x0 + 0.55, 3.25, z0 + 0.55), (x1 - 0.55, top_y, z1 - 0.55),
                           sides=("north", "south", "east", "west", "up"))]
            if stage == 2:
                boxes.append(panel(sc, f"heap_{fruit}", heap(fruit), (x0 + 1.5, top_y, z0 + 1.5), (x1 - 1.5, top_y + 1.25, z1 - 1.5),
                                   sides=("north", "south", "east", "west", "up")))
            sc.models[f"heap_{fruit}_{stage}"] = boxes
    return sc


# ---------------------------------------------------------------- building and files

_built = {}
BUILDERS = {"orchard_ladder": (ladder, 64), "apple_bin": (apple_bin, 128), "picking_bag": (picking_bag, 64)}


def build(block):
    if block not in _built:
        builder, size = BUILDERS[block]
        _built[block] = ppd.planned(builder, size)
    return _built[block]


def _image_of(name):
    return build(name).atlas.img if name in BUILDERS else None


def item_model(block, elements, size, scale):
    """The item: the whole prop scaled into one block about its middle."""
    return fa.transformed(d17d.pinned(elements), scale, (8 - 8 * size[0] * scale, 0, 8 - 8 * size[2] * scale), (0, 0, 0))


DISPLAY = {"gui": {"rotation": [25, 225, 0], "translation": [0, 0, 0], "scale": [0.625, 0.625, 0.625]},
           "ground": {"rotation": [0, 0, 0], "translation": [0, 2, 0], "scale": [0.3, 0.3, 0.3]},
           "fixed": {"rotation": [0, 0, 0], "translation": [0, 0, 0], "scale": [0.6, 0.6, 0.6]},
           "thirdperson_righthand": {"rotation": [0, 45, 0], "translation": [0, 2, 0], "scale": [0.4, 0.4, 0.4]},
           "firstperson_righthand": {"rotation": [0, 45, 0], "translation": [0, 2, 0], "scale": [0.4, 0.4, 0.4]}}


def assets(root, write, lang):
    write = fa.closing_writer(write, _image_of)
    models = root / "models" / "block"
    states = root / "blockstates"
    items = root / "models" / "item"

    spec = oh.LADDER
    block = spec["block"]
    lang[f"block.{MOD}.{block}"] = spec["display"]
    sc = build(block)
    n = len(d17d.cells(spec["size"]))
    for part, elements in enumerate(ppd.cut(sc.models["whole"], spec["size"])):
        write(models / f"{block}_{part}.json", fa.model(block, elements))
    write(states / f"{block}.json", {"variants": {
        f"facing={f},lit={l},part={p}": {"model": rid(f"block/{block}_{p}"), **({"y": FACINGS[f]} if FACINGS[f] else {})}
        for f in FACINGS for l in ("false", "true") for p in range(n)}})
    write(items / f"{block}.json", fa.model(block, item_model(block, sc.models["whole"], spec["size"], 1 / 3), DISPLAY))
    write(root / "items" / f"{block}.json", {"model": {"type": "minecraft:model", "model": rid(f"item/{block}")}})

    spec = oh.BIN
    block = spec["block"]
    lang[f"block.{MOD}.{block}"] = spec["display"]
    sc = build(block)
    n = len(d17d.cells(spec["size"]))
    empty = ppd.cut(sc.models["whole"], spec["size"])
    for part, elements in enumerate(empty):
        write(models / f"{block}_{part}.json", fa.model(block, elements))
    for fruit in oh.FRUITS:
        for stage in range(1, spec["stages"] + 1):
            full = ppd.cut(sc.models["whole"] + sc.models[f"heap_{fruit}_{stage}"], spec["size"])
            for part, elements in enumerate(full):
                write(models / f"{block}_{part}_{fruit}_{stage}.json", fa.model(block, elements))
    variants = {}
    for f in FACINGS:
        turn = {"y": FACINGS[f]} if FACINGS[f] else {}
        for fruit in oh.FRUITS:
            for stage in range(spec["stages"] + 1):
                for p in range(n):
                    model = f"{block}_{p}" if stage == 0 else f"{block}_{p}_{fruit}_{stage}"
                    for lit in ("false", "true"):
                        variants[f"facing={f},fill={stage},fruit={fruit},lit={lit},part={p}"] = {"model": rid(f"block/{model}"), **turn}
    write(states / f"{block}.json", {"variants": variants})
    # The item shows it three quarters full of apples, so it reads as what it is.
    write(items / f"{block}.json", fa.model(block, item_model(block, sc.models["whole"] + sc.models[f"heap_apple_{spec['stages'] - 1}"],
                                                             spec["size"], 0.5), DISPLAY))
    write(root / "items" / f"{block}.json", {"model": {"type": "minecraft:model", "model": rid(f"item/{block}")}})

    spec = oh.BAG
    block = spec["block"]
    lang[f"block.{MOD}.{block}"] = spec["display"]
    sc = build(block)
    write(models / f"{block}.json", fa.model(block, sc.models["whole"]))
    for fruit in oh.FRUITS:
        for stage in (1, 2):
            write(models / f"{block}_{fruit}_{stage}.json", fa.model(block, sc.models["whole"] + sc.models[f"heap_{fruit}_{stage}"]))
    variants = {}
    for f in FACINGS:
        turn = {"y": FACINGS[f]} if FACINGS[f] else {}
        for fruit in oh.FRUITS:
            for stage in range(spec["stages"] + 1):
                model = block if stage == 0 else f"{block}_{fruit}_{stage}"
                variants[f"facing={f},fill={stage},fruit={fruit}"] = {"model": rid(f"block/{model}"), **turn}
    write(states / f"{block}.json", {"variants": variants})
    write(items / f"{block}.json", fa.model(block, sc.models["whole"], DISPLAY))
    write(root / "items" / f"{block}.json", {"model": {"type": "minecraft:model", "model": rid(f"item/{block}")}})
    lang[f"message.{MOD}.fruit_store.full"] = "It's full"


def loot(out, write):
    """The ladder and the bin drop once, from their first block; the bag drops itself."""
    for block in (oh.LADDER["block"], oh.BIN["block"]):
        write(out / f"{block}.json", {"type": "minecraft:block", "pools": [{
            "condition": {"type": "minecraft:all_of", "terms": [{"type": "minecraft:survives_explosion"},
                                                                {"type": "minecraft:match_block", "blocks": rid(block), "state": {"part": "0"}}]},
            "entries": [{"type": "minecraft:item", "name": rid(block)}], "rolls": 1}], "random_sequence": rid(f"blocks/{block}")})
    block = oh.BAG["block"]
    write(out / f"{block}.json", {"type": "minecraft:block", "pools": [{
        "condition": {"type": "minecraft:survives_explosion"}, "entries": [{"type": "minecraft:item", "name": rid(block)}], "rolls": 1}],
        "random_sequence": rid(f"blocks/{block}")})


def tags(tags):
    for block in oh.blocks():
        tags.add("block", "minecraft:mineable/axe", rid(block))
    tags.add("block", "minecraft:climbable", rid(oh.LADDER["block"]))


def textures():
    return {("block", block): build(block).atlas.img for block in oh.blocks()}

"""Files for Barn & Harvest Tools, decor set 3 (tools/barn_tools.py; docs/features/barn-and-harvest-tools.md).

Part 1's hand tools are flat items: the Watering Can's and the Seed Pouch's models and lang are written here, the Steel
Sickle's beside the other sickles (tools/agriculture_data.py, from SICKLES). Their icons, and part 2's Flour, Butter and
Pancakes (whose item models tools/agriculture_data.py writes from ITEMS), are maps drawn by tools/item_icons.py from
tools/item_icons/<name>.txt, checked by tools/check_icon_maps.py.

Part 2's Quern and Butter Churn are sculpted on the toolkit of tools/flora_art.py and tools/pumpkin_patch_data.py, each
block one texture painted here, at vanilla's density (one texel to a model pixel), and real-life sized: the quern's
stones 44 cm across on a stand 69 cm high, the churn 38 cm across and 78 cm to its lid. Each has a part that moves (the
quern's upper stone and its peg, the churn's dasher) drawn by its own model, which the block entity's renderer turns or
lifts (client/QuernRenderer, client/ButterChurnRenderer); the block's own model leaves it out.

Everything is drawn here by code; no Mojang texture is read, traced or copied.
"""

import barn_tools as bt
import decor20_data as d20d
import flora_art as fa
import item_icons
import pumpkin_patch_data as ppd
from flora_art import Sculpt, pal, shade
from pumpkin_patch_data import IRON, WOOD, flat, lengthwise, panel, planks

MOD = "jugcraft"
FACINGS = ppd.FACINGS

# Palettes, darkest first: a grey-brown millstone grit, flour, wheat in the quern's eye.
STONE = pal("4a4640", "625d55", "7a746a", "928b80", "aaa396")
FLOUR = pal("c6bca4", "dcd3bd", "eee7d6")
GRAIN = pal("7e5e1e", "a8862e", "c8a644", "e0c464")


def rid(path):
    return f"{MOD}:{path}"


# ---------------------------------------------------------------- painters

def stone(face=False, eye=False):
    """Millstone grit, its middle tone. A side is lit along its upper row and shaded along its lower one; a face (`face`)
    is the one flat tone, so the bands a round stone is built of read as one face, and the runner's has its eye, the
    darkest tone, at its middle."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                k = 2 if face else 3 if y == 0 else 1 if (y == p.h - 1 and p.h > 1) else 2
                p.put(x, y, shade(STONE, k))
        if eye:
            p.put(p.w // 2, p.h // 2, shade(STONE, 0))
    paint.tag = repr(("stone", face, eye))
    return paint


def round_slab(sc, name, cx, cz, across, y0, y1, side, top, bottom=None):
    """A round slab `across` pixels wide (odd: 7 or 5) about (cx, cz), from y0 to y1, as a circle is drawn in pixels: three
    boxes, each a row band of the circle (for 7 across, 3 by 7, 5 by 5 and 7 by 3), centred on it. `separated` keeps
    their coincident tops and bottoms apart."""
    h = across / 2
    bands = ((h - 2, h), (h - 1, h - 1), (h, h - 2)) if across >= 7 else ((h - 1, h), (h, h - 1))
    over = {"up": top, "down": bottom or top}
    return [panel(sc, name, side, (cx - a, y0, cz - b), (cx + a, y1, cz + b), **over) for a, b in bands]


# ---------------------------------------------------------------- part 2: the Quern

def quern():
    """The Quern, facing north: a stand of four legs, rails and a top board; on it a flour tray (walls round a floor
    dusted with flour, a spout at the front) round the bed stone; and the runner, the upper stone with its eye and a
    turned peg near its rim, drawn by its own models (with wheat in its eye, or without) for the renderer to turn."""
    spec = bt.QUERN
    sc = Sculpt(spec["block"], 2301, 64)
    el = []
    # The stand: legs at the corners of its 10-pixel top, rails between them under it, the top board.
    for x in (3.0, 12.0):
        for z in (3.0, 12.0):
            el.append(panel(sc, "leg", lengthwise(WOOD), (x, 0.0, z), (x + 1.0, 10.0, z + 1.0)))
    for z in (3.0, 12.0):
        el.append(panel(sc, "rail", lengthwise(WOOD), (4.0, 7.5, z + 0.25), (12.0, 8.5, z + 0.75)))
    for x in (3.0, 12.0):
        el.append(panel(sc, "rail", lengthwise(WOOD), (x + 0.25, 7.5, 4.0), (x + 0.75, 8.5, 12.0)))
    el.append(panel(sc, "top", planks(WOOD, board=4, nails=False), (2.5, 10.0, 2.5), (13.5, 11.0, 13.5)))
    # The flour tray: low walls round the bed stone, flour dusting its floor, and the spout it leaves by at the front.
    el += ppd.walls(sc, "tray", flat(WOOD), 3.5, 3.5, 12.5, 12.5, 0.5, 11.0, 12.0)
    el.append(panel(sc, "flour", flat(FLOUR), (4.0, 11.0, 4.0), (12.0, 11.25, 12.0)))
    el.append(panel(sc, "spout", flat(WOOD), (7.0, 11.0, 2.0), (9.0, 11.5, 3.5)))
    el.append(panel(sc, "spout_flour", flat(FLOUR), (7.25, 11.5, 2.25), (8.75, 11.75, 3.5)))
    # The bed stone, fixed in the tray.
    el += round_slab(sc, "bed", 8.0, 8.0, 7, 11.25, 12.5, stone(), stone(face=True))
    sc.models["base"] = el
    # The runner: the upper stone, its eye, and its peg near the rim at the front.
    runner = round_slab(sc, "runner", 8.0, 8.0, 7, 12.5, 14.0, stone(), stone(face=True, eye=True), stone(face=True))
    runner.append(panel(sc, "peg", lengthwise(WOOD), (7.5, 14.0, 5.0), (8.5, 18.0, 6.0)))
    sc.models["runner"] = runner
    # Wheat waiting in the eye.
    sc.models["runner_loaded"] = runner + [panel(sc, "grain", flat(GRAIN), (7.25, 14.0, 7.25), (8.75, 14.5, 8.75))]
    return sc


# ---------------------------------------------------------------- part 2: the Butter Churn

def churn():
    """The Butter Churn, facing north: a round churn of upright staves (two row bands of a circle six pixels across),
    two iron hoops standing a little proud of them, a lid set into its top with a lifting cleat, and the dasher's handle,
    drawn by its own model for the renderer to work up and down through the lid."""
    spec = bt.CHURN
    sc = Sculpt(spec["block"], 2302, 64)
    staves = planks(WOOD, board=3, nails=False)
    el = []
    for a, b in ((2.0, 3.0), (3.0, 2.0)):
        el.append(panel(sc, "staves", staves, (8.0 - a, 0.0, 8.0 - b), (8.0 + a, 12.5, 8.0 + b), up=flat(WOOD), down=flat(WOOD)))
    for y in (1.5, 9.5):
        for a, b in ((2.0, 3.0), (3.0, 2.0)):
            el.append(panel(sc, "hoop", flat(IRON), (8.0 - a - 0.25, y, 8.0 - b - 0.25), (8.0 + a + 0.25, y + 1.0, 8.0 + b + 0.25)))
    # The lid, a board set into the top, and a cleat across it to lift it by (the handle passes beside it).
    el.append(panel(sc, "lid", planks(WOOD, board=2, nails=False), (6.0, 12.5, 6.0), (10.0, 13.0, 10.0)))
    el.append(panel(sc, "cleat", lengthwise(WOOD), (6.5, 13.0, 9.0), (9.5, 13.5, 9.75)))
    sc.models["base"] = el
    # The dasher: its handle up through the lid, a turned grip at the top.
    sc.models["runner"] = [panel(sc, "handle", lengthwise(WOOD), (7.5, 10.0, 7.5), (8.5, 21.0, 8.5)),
                           panel(sc, "grip", lengthwise(WOOD), (7.25, 19.0, 7.25), (8.75, 21.5, 8.75))]
    return sc


# ---------------------------------------------------------------- building and files

_built = {}
BUILDERS = {bt.QUERN["block"]: quern, bt.CHURN["block"]: churn}


def build(block):
    if block not in _built:
        _built[block] = ppd.planned(BUILDERS[block], 64)
    return _built[block]


def _image_of(name):
    return build(name).atlas.img if name in BUILDERS else None


DISPLAY = {"gui": {"rotation": [25, 225, 0], "translation": [0, -1.5, 0], "scale": [0.75, 0.75, 0.75]},
           "ground": {"rotation": [0, 0, 0], "translation": [0, 2, 0], "scale": [0.4, 0.4, 0.4]},
           "fixed": {"rotation": [0, 0, 0], "translation": [0, -1, 0], "scale": [0.7, 0.7, 0.7]},
           "thirdperson_righthand": {"rotation": [75, 45, 0], "translation": [0, 2, 0], "scale": [0.4, 0.4, 0.4]},
           "firstperson_righthand": {"rotation": [0, 45, 0], "translation": [0, 0, 0], "scale": [0.4, 0.4, 0.4]}}


def part2_assets(root, write, lang):
    """The quern's and churn's block models (the block without its moving part; the moving part alone, which the
    renderer draws), blockstates (`runner` picks the moving part's model, never set on a placed block) and items."""
    write = fa.closing_writer(write, _image_of)
    models = root / "models" / "block"
    for spec, extra in ((bt.QUERN, ("loaded", ("false", "true"))), (bt.CHURN, ("full", ("false", "true")))):
        block = spec["block"]
        lang[f"block.{MOD}.{block}"] = spec["display"]
        sc = build(block)
        write(models / f"{block}.json", fa.model(block, d20d.separated(sc.models["base"])))
        runners = [m for m in sc.models if m.startswith("runner")]
        for name in runners:
            write(models / f"{block}_{name}.json", fa.model(block, d20d.separated(sc.models[name])))
        prop, values = extra
        variants = {}
        for f, y in FACINGS.items():
            turn = {"y": y} if y else {}
            for v in values:
                variants[f"facing={f},{prop}={v},runner=false"] = {"model": rid(f"block/{block}"), **turn}
                runner = "runner_loaded" if prop == "loaded" and v == "true" else "runner"
                variants[f"facing={f},{prop}={v},runner=true"] = {"model": rid(f"block/{block}_{runner}"), **turn}
        write(root / "blockstates" / f"{block}.json", {"variants": variants})
        write(root / "models" / "item" / f"{block}.json", fa.model(block, d20d.separated(sc.models["base"] + sc.models["runner"]), DISPLAY))
        write(root / "items" / f"{block}.json", {"model": {"type": "minecraft:model", "model": rid(f"item/{block}")}})
    lang[f"message.{MOD}.quern.full"] = "The quern is full"
    lang[f"message.{MOD}.quern.other"] = "The quern holds other grain: take it out first"
    lang[f"message.{MOD}.butter_churn.full"] = "The churn is full"


def loot(out, write):
    """The quern and the churn drop themselves; the quern spills its grain (QuernBlockEntity), the churn its milk."""
    for block in bt.blocks():
        write(out / f"{block}.json", {"type": "minecraft:block", "pools": [{
            "condition": {"type": "minecraft:survives_explosion"}, "entries": [{"type": "minecraft:item", "name": rid(block)}], "rolls": 1}],
            "random_sequence": rid(f"blocks/{block}")})


def tags(tags):
    for block in bt.blocks():
        tags.add("block", "minecraft:mineable/axe", rid(block))


def assets(root, write, lang):
    for spec in (bt.WATERING_CAN, bt.SEED_POUCH):
        item = spec["item"]
        write(root / "models" / "item" / f"{item}.json", {"parent": "minecraft:item/generated", "textures": {"layer0": rid(f"item/{item}")}})
        write(root / "items" / f"{item}.json", {"model": {"type": "minecraft:model", "model": rid(f"item/{item}")}})
        lang[f"item.{MOD}.{item}"] = spec["display"]
    lang[f"item.{MOD}.watering_can.water"] = "Water: %s of %s pours"
    lang[f"item.{MOD}.seed_pouch.contents"] = "%s of %s kinds of seed, %s seeds"
    part2_assets(root, write, lang)


def textures():
    """The part 1 icons and part 2's foods', from their maps (the Steel Sickle's replaces nothing: the flint and bronze
    sickles keep tools/crop_textures.py's drawings), and the quern's and churn's block textures."""
    out = {("item", name): item_icons.draw(name) for name in ("steel_sickle", bt.WATERING_CAN["item"], bt.SEED_POUCH["item"], *bt.ITEMS)}
    out.update({("block", block): build(block).atlas.img for block in bt.blocks()})
    return out

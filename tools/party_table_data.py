"""Generated assets for the Halloween Party Table, decor set 8 (tools/party_table.py; docs/features/halloween-party-table.md).

Built from boxes at real size, one texel to a model pixel, in the Haunted Midway's manner (tools/haunted_midway_data.py,
whose palettes it shares): each material one colour stepped into tones, its marks placed the way it is made.
- Cast iron (the dispenser's foot, pedestal, collar and lid): black, lit along its top row and shaded along its foot; the
  pedestal banded in orange enamel.
- Brass (the faceplate, crank, flap and knob): lit like a gold block, its top row bright and its foot dark.
- Glass (the globe): clear and faint, a streak of reflection down its upper left, never a rim round each step.
- Candy corn (what fills the globe): kernels two texels wide and three tall, white tip, orange middle and yellow foot,
  each column of them a texel lower than the last, lit on their left; counted down from the candy's surface, so they
  run on from one ring of the globe to the next.
"""
import decor20_data as d20d
import flora_art as fa
import party_table as pt
import pumpkin_patch_data as ppd
from flora_art import Sculpt, cube, pal, shade
from haunted_midway_data import BLACK, GOLD, ORANGE
from pumpkin_patch_data import panel

MOD = "jugcraft"
FACINGS = ppd.FACINGS
GLASS = (214, 232, 236)
CANDY = {"tip": (246, 238, 220), "middle": (236, 124, 28), "foot": (250, 196, 54)}


def rid(path):
    return path if ":" in path else f"{MOD}:{path}"


# ---------------------------------------------------------------- painters

def cast(band=None):
    """Black cast iron: the black's middle tone, lit along its top row, shaded along its foot; with `band`, that row
    (from the top) orange enamel, lit."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                k = 3 if y == 0 and p.h > 1 else 1 if y == p.h - 1 and p.h > 2 else 2
                p.put(x, y, shade(ORANGE, 3) if y == band else shade(BLACK, k))
    paint.tag = repr(("cast", band))
    return paint


def brass():
    """Polished brass, lit like a gold block: its top row bright, its foot dark, a tone between."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                k = 4 if y == 0 and p.h > 1 else 1 if y == p.h - 1 and p.h > 2 else 3 if x == 0 else 2
                p.put(x, y, shade(GOLD, k))
    paint.tag = repr(("brass",))
    return paint


def dark():
    """The dark inside of the chute: the black's darkest tone."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                p.put(x, y, shade(BLACK, 0))
    paint.tag = repr(("dark",))
    return paint


def globe_glass(streak=False):
    """The globe's glass: faint and clear; with `streak`, a streak of reflection a texel wide down its second column,
    brighter at its top."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                p.put(x, y, GLASS, 55)
        if streak and p.w > 2:
            for y in range(p.h):
                p.put(1, y, (250, 254, 255), 190 if y == 0 else 150)
    paint.tag = repr(("globe_glass", streak))
    return paint


def candy_corn(phase=0):
    """Candy corn packed in a jar: kernels two texels wide and three tall, each column of them a texel lower than the
    last, as they settle: a white tip, an orange middle and a yellow foot, each kernel lit on its left and a tone deeper
    on its right. `phase` is how many texels below the candy's surface the piece's top row lies, so the kernels run on
    from one ring of the globe to the next."""
    order = ("tip", "middle", "foot")

    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                tone = CANDY[order[(y + phase + x // 2) % 3]]
                p.put(x, y, tone if x % 2 == 0 else tuple(int(c * 0.84) for c in tone))
    paint.tag = repr(("candy_corn", phase % 3))
    return paint


# ---------------------------------------------------------------- the Candy Corn Dispenser

# The globe's rings: (bottom, top, half-width), from the collar up.
GLOBE = ((5.75, 6.5, 2.25), (6.5, 7.5, 3.0), (7.5, 10.5, 3.25), (10.5, 11.5, 3.0), (11.5, 12.25, 2.25))
# How high the candy stands at each fill level, 1 to `levels`.
FILL_TOPS = (6.75, 8.25, 9.75, 11.25)


def ring_strips(sc, name, painter, y, outer, inner, side):
    """The flat glass between a ring `outer` half-wide and the next `inner` half-wide at height `y`: four strips round
    the step, seen from `side` (up or down) alone, so no glass lies inside the globe."""
    lo, hi = 8.0 - outer, 8.0 + outer
    ilo, ihi = 8.0 - inner, 8.0 + inner
    out = []
    for (x0, z0), (x1, z1) in (((lo, lo), (hi, ilo)), ((lo, ihi), (hi, hi)), ((lo, ilo), (ilo, ihi)), ((ihi, ilo), (hi, ihi))):
        out.append(panel(sc, name, painter, (x0, y, z0), (x1, y, z1), sides=(side,)))
    return out


def candy_corn_dispenser():
    """The Candy Corn Dispenser, facing north (its crank and chute to the north): a cast-iron foot and pedestal banded in
    orange, a brass faceplate on the pedestal's front with the chute's dark mouth low on it, a collar, the glass globe in
    five rings, the lid with its knob. Models: `whole`; `fill_1` to `fill_4` (the candy in the globe at each level); and
    its runners `crank` (the hub, arm and knob, which the renderer turns about its axle) and `flap` (over the chute,
    which the renderer swings out about its top)."""
    spec = pt.DISPENSER
    sc = Sculpt(spec["block"], 2801, 128)
    iron = cast()
    shine = brass()
    el = [panel(sc, "foot", iron, (4.5, 0.0, 4.5), (11.5, 1.0, 11.5)),
          panel(sc, "pedestal", cast(band=1), (5.5, 1.0, 5.5), (10.5, 5.0, 10.5), up=iron, down=iron),
          panel(sc, "faceplate", shine, (6.0, 1.5, 5.0), (10.0, 4.5, 5.5)),
          panel(sc, "chute", dark(), (6.5, 1.5, 4.9), (9.5, 3.0, 5.0), sides=("north", "east", "west", "up", "down")),
          panel(sc, "collar", iron, (5.0, 5.0, 5.0), (11.0, 5.75, 11.0)),
          panel(sc, "lid", iron, (6.0, 12.25, 6.0), (10.0, 13.0, 10.0)),
          panel(sc, "knob", shine, (7.5, 13.0, 7.5), (8.5, 13.75, 8.5))]
    # The globe: each ring's sides, the streak down its north and west faces; the flat glass at each step between rings.
    glass, streaked = globe_glass(), globe_glass(streak=True)
    for y0, y1, a in GLOBE:
        el.append(panel(sc, "globe", glass, (8.0 - a, y0, 8.0 - a), (8.0 + a, y1, 8.0 + a), sides=fa.SIDES4,
                        north=streaked, west=streaked))
    for (y0, y1, a), (_, _, b) in zip(GLOBE, GLOBE[1:]):
        if b > a:
            el += ring_strips(sc, "globe_step", glass, y1, b, a, "down")
        else:
            el += ring_strips(sc, "globe_step", glass, y1, a, b, "up")
    sc.models["whole"] = el
    for level, top in enumerate(FILL_TOPS, start=1):
        fill = []
        for y0, y1, a in GLOBE:
            lo, hi = max(y0, GLOBE[0][0] + 0.05), min(y1, top)
            if hi > lo:
                candy = candy_corn(int(round(top - hi)))
                fill.append(panel(sc, "candy", candy, (8.25 - a, lo, 8.25 - a), (7.75 + a, hi, 7.75 + a), up=candy_corn()))
        sc.models[f"fill_{level}"] = fill
    sc.models["crank"] = [panel(sc, "hub", shine, (7.5, 3.25, 4.5), (8.5, 4.25, 5.0)),
                          panel(sc, "crank_arm", shine, (8.0, 3.5, 4.0), (9.75, 4.0, 4.5)),
                          panel(sc, "crank_knob", cast(), (9.25, 3.375, 3.0), (10.0, 4.125, 4.0))]
    sc.models["flap"] = [panel(sc, "flap", shine, (6.5, 1.5, 4.5), (9.5, 3.0, 4.75))]
    return sc


# ---------------------------------------------------------------- building and files

_built = {}
BUILDERS = {pt.DISPENSER["block"]: candy_corn_dispenser}


def build(block):
    if block not in _built:
        _built[block] = ppd.planned(BUILDERS[block], 128)
    return _built[block]


def _image_of(name):
    return build(name).atlas.img if name in BUILDERS else None


def assets(root, write, lang):
    """The dispenser's model, its candy at each fill level and its runners' models (each shown alone at its `runner`), a
    multipart blockstate adding the candy by `fill`, and its item (the dispenser whole, its crank and flap, half full)."""
    write = fa.closing_writer(write, _image_of)
    models = root / "models" / "block"
    spec = pt.DISPENSER
    block = spec["block"]
    lang[f"block.{MOD}.{block}"] = spec["display"]
    sc = build(block)
    write(models / f"{block}.json", fa.model(block, d20d.separated(sc.models["whole"])))
    for name in ["crank", "flap"] + [f"fill_{n}" for n in range(1, spec["levels"] + 1)]:
        write(models / f"{block}_{name}.json", fa.model(block, d20d.separated(sc.models[name])))
    multipart = []
    for f, y in FACINGS.items():
        turn = {"y": y} if y else {}
        multipart.append({"when": {"facing": f, "runner": "0"}, "apply": {"model": rid(f"block/{block}"), **turn}})
        for n in range(1, spec["levels"] + 1):
            multipart.append({"when": {"facing": f, "runner": "0", "fill": str(n)}, "apply": {"model": rid(f"block/{block}_fill_{n}"), **turn}})
        for value, name in (("1", "crank"), ("2", "flap")):
            multipart.append({"when": {"facing": f, "runner": value}, "apply": {"model": rid(f"block/{block}_{name}"), **turn}})
    write(root / "blockstates" / f"{block}.json", {"multipart": multipart})
    shown = sc.models["whole"] + sc.models["fill_2"] + sc.models["crank"] + sc.models["flap"]
    write(root / "models" / "item" / f"{block}.json", fa.model(block, d20d.separated(shown)))
    write(root / "items" / f"{block}.json", {"model": {"type": "minecraft:model", "model": rid(f"item/{block}")}})


def loot(out, write):
    """The dispenser drops itself (what it holds drops from its block entity)."""
    for block in pt.blocks():
        write(out / f"{block}.json", {"type": "minecraft:block", "pools": [{
            "condition": {"type": "minecraft:survives_explosion"}, "entries": [{"type": "minecraft:item", "name": rid(block)}],
            "rolls": 1}], "random_sequence": rid(f"blocks/{block}")})


def tags(tags):
    tags.add("block", "minecraft:mineable/pickaxe", rid(pt.DISPENSER["block"]))


def textures():
    return {("block", block): build(block).atlas.img for block in pt.blocks()}

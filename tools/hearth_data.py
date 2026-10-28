"""Generated assets for Cozy Fall Hearth, decor set 5 (tools/hearth.py; docs/features/cozy-fall-hearth.md).

Built from boxes at real size, one texel to a model pixel, in the market displays' manner (tools/pumpkin_patch_data.py):
each material one colour stepped into tones, its marks placed the way it is made, never sprinkled.
- Fieldstone: rounded stones in staggered courses, each stone one tone of the grey-brown (its own, by its place in the
  course), lit along its top row, set in a pale mortar joint.
- Firebrick at the back of the firebox: small bricks in a running bond, sooted darker towards the top.
- The hearth's flagstones: big slabs with a joint between them.
- The mantel: an oak beam, wood along its length. The logs: bark round them, end grain at their ends.
- The fire: its own model in an animated texture (`stone_fireplace_fire`), full-bright and unshaded, drawn only while
  the fireplace is lit: tongues of flame in smooth bands (a pale root, yellow, orange, a deep orange tip), their heights
  rising and falling by a slow wave from frame to frame, the frames blended, so it never flashes.
"""
import math

import decor17_data as d17d
import decor20_data as d20d
import flora_art as fa
import hearth as hh
import pumpkin_patch_data as ppd
from flora_art import Sculpt, pal, shade
from pumpkin_patch_data import IRON, WOOD, flat, lengthwise, panel

MOD = "jugcraft"
FACINGS = ppd.FACINGS

STONE = pal("4a443c", "5e574d", "726a5e", "877e70", "9c9282")
MORTAR = pal("8a8478", "a09a8c", "b4ae9e")
FIREBRICK = pal("3a1e16", "52281c", "6a3424", "82422e")
FLAG = pal("5a5852", "6e6c64", "838078", "98958c")
BEAM = pal("2e2216", "44321e", "5a4228", "6e5232")
BARK = pal("2a2018", "3c2e22", "4e3c2c")
ENDGRAIN = pal("6a5638", "8a7048", "a88a5a")
FIRE = pal("b8401a", "dc6a22", "f0962c", "f8c048", "fce690")
LEAF_COLOURS = (pal("6a1a10", "8e2a16", "b0401e"), pal("7a3a0e", "a85414", "cc7420"), pal("7a5a10", "a07a16", "c49a24"))
PUMPKIN = pal("8a3c0a", "b25412", "d4701c", "e88a2c")
CONE = pal("3a2614", "52361c", "6a4626")


def rid(path):
    return path if ":" in path else f"{MOD}:{path}"


# Each course's stones' lengths, in turn (a course starts a step further along the run than the last).
STONE_LENGTHS = (5, 7, 4, 6, 8, 5, 6)


def fieldstone():
    """Fieldstone laid in courses four texels high, its stones of several lengths in turn (so no two courses line up),
    their corners rounded off into the mortar; each stone takes one of three tones by its place, so neighbours differ,
    and is lit along its top row. The joints between are a pale mortar."""
    def paint(p):
        for y in range(p.h):
            course, row = y // 4, y % 4
            # Where each stone of this course starts and ends.
            edges, at, k = [], -(course * 3) % 7, course
            while at < p.w + 8:
                length = STONE_LENGTHS[k % len(STONE_LENGTHS)]
                edges.append((at, at + length, k))
                at += length
                k += 1
            for x in range(p.w):
                for start, end, index in edges:
                    if start <= x < end:
                        break
                inner = x - start
                last = end - start - 1
                corner = row in (0, 2) and inner in (0, last) and row == 0
                if row == 3 or inner == last or corner:
                    p.put(x, y, shade(MORTAR, 1))
                    continue
                tone = 1 + (index * 2 + course) % 3
                p.put(x, y, shade(STONE, tone + (1 if row == 0 else 0)))
    paint.tag = "fieldstone"
    return paint


def firebrick():
    """Firebrick in a running bond, bricks four texels long and two high with a dark joint, sooted a tone darker in the
    upper half."""
    def paint(p):
        for y in range(p.h):
            row = y // 2
            for x in range(p.w):
                joint = y % 2 == 1 or (x + (row % 2) * 2) % 4 == 3
                k = 0 if joint else 2
                if y < p.h // 2 and not joint:
                    k = 1
                p.put(x, y, shade(FIREBRICK, k))
    paint.tag = "firebrick"
    return paint


def flagstone():
    """Flagstones eight texels square with a joint a tone darker between them, each slab lit along its top row."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                joint = x % 8 == 7 or y % 8 == 7
                k = 0 if joint else 3 if y % 8 == 0 else 2 if ((x // 8) + (y // 8)) % 2 == 0 else 1
                p.put(x, y, shade(FLAG, k))
    paint.tag = "flagstone"
    return paint


def end_grain():
    """A log's end: a ring of bark round rings of wood, light and mid by turns, about a dark pith, square as a log's end
    is drawn in pixels."""
    def paint(p):
        cx, cy = (p.w - 1) / 2, (p.h - 1) / 2
        outer = (min(p.w, p.h) - 1) / 2
        for y in range(p.h):
            for x in range(p.w):
                r = max(abs(x - cx), abs(y - cy))
                p.put(x, y, shade(BARK, 1) if r >= outer else shade(ENDGRAIN, 0 if r < 0.6 else 2 if round(r) % 2 else 1))
    paint.tag = "end_grain"
    return paint


def fire_strip():
    """The fire, a frame a 16 x 16 square, `frames` of them down the strip: five tongues of flame side by side, each a
    smooth rise from its root to its tip in the fire's bands (pale root, yellow, orange, deep orange), its height rising
    and falling on a slow wave (each tongue a step behind the last), clear above it. The frames are blended in game."""
    from PIL import Image
    n = hh.FIREPLACE["frames"]
    strip = Image.new("RGBA", (16, 16 * n), (0, 0, 0, 0))
    for f in range(n):
        for x in range(16):
            tongue = x // 3
            centre = tongue * 3 + 1
            phase = (f / n + tongue * 0.27) * math.tau
            height = 11.0 + 3.0 * math.sin(phase) - 1.6 * abs(x - centre)
            for y in range(16):
                up = 15 - y
                if up >= height:
                    continue
                band = up / max(1.0, height)
                k = 4 if band < 0.2 else 3 if band < 0.45 else 2 if band < 0.75 else 1
                strip.putpixel((x, f * 16 + y), tuple(shade(FIRE, k)) + (255,))
    return strip


def fireplace():
    """The Stone Fireplace, facing north (its wall behind, to the south), three blocks across and three up: a flagstone
    hearth running out before it, the breast's jambs and lintel round the firebox opening (1.25 m across, 78 cm high),
    firebrick at its back, an oak mantel shelf on two corbels at 1.4 m, and the stack stepping in above it to the
    ceiling; in the firebox two iron andirons with three logs across them. Its models: `whole`, `garland` (the mantel's
    dressing) and `fire` (drawn on its own texture)."""
    sc = Sculpt(hh.FIREPLACE["block"], 2501, 128)
    stone = fieldstone()
    x0, x1, y0, y1 = hh.FIREPLACE["opening"]
    mantel = hh.FIREPLACE["mantel"]
    el = [panel(sc, "hearth", flagstone(), (4.0, 0.0, 0.0), (44.0, 1.5, 6.0)),
          panel(sc, "plinth", flagstone(), (0.0, 0.0, 6.0), (48.0, y0, 16.0)),
          panel(sc, "jamb", stone, (0.0, y0, 6.0), (x0, mantel, 16.0)),
          panel(sc, "jamb", stone, (x1, y0, 6.0), (48.0, mantel, 16.0)),
          panel(sc, "lintel", stone, (x0, y1, 6.0), (x1, mantel, 16.0)),
          panel(sc, "back", firebrick(), (x0, y0, 14.0), (x1, y1, 16.0)),
          panel(sc, "mantel", lengthwise(BEAM), (2.0, mantel, 3.0), (46.0, mantel + 2.5, 16.0)),
          panel(sc, "stack", stone, (4.0, mantel + 2.5, 8.0), (44.0, 48.0, 16.0))]
    for cx in (4.0, 44.0):
        el.append(panel(sc, "corbel", lengthwise(BEAM), (cx - 1.0, mantel - 3.0, 4.0), (cx + 1.0, mantel, 6.0)))
    for ax in (17.0, 30.0):
        el.append(panel(sc, "andiron", flat(IRON), (ax, y0, 7.5), (ax + 1.0, y0 + 2.5, 12.5)))
        el.append(panel(sc, "finial", flat(IRON), (ax - 0.25, y0 + 2.5, 7.25), (ax + 1.25, y0 + 4.0, 8.25)))
    for ly, lz in ((y0 + 2.5, 9.0), (y0 + 2.5, 11.5), (y0 + 4.5, 10.25)):
        el.append(panel(sc, "log", ppd.lengthwise(BARK), (15.5, ly, lz - 1.0), (32.5, ly + 2.0, lz + 1.0),
                        east=end_grain(), west=end_grain()))
    sc.models["whole"] = el
    # The garland: leaves in three autumn colours along the mantel's top and front, little pumpkins and pine cones.
    gar = []
    for i, x in enumerate(range(3, 45, 3)):
        leaves = LEAF_COLOURS[i % 3]
        gar.append(panel(sc, f"leaf{i % 3}", flat(leaves), (x, mantel + 2.5, 3.25), (x + 2.5, mantel + 3.25, 5.75)))
    # Four swags hung along the mantel's front, each a run of leaves sagging between two points.
    for a in (3.0, 13.5, 24.0, 34.5):
        for k in range(7):
            t = k / 6.0
            x = a + t * 10.5
            y = mantel + 1.0 - 2.5 * math.sin(math.pi * t)
            leaves = LEAF_COLOURS[k % 3]
            gar.append(panel(sc, f"swag{k % 3}", flat(leaves), (x - 0.9, y - 0.6, 2.5), (x + 0.9, y + 0.6, 3.0)))
    for x in (8.0, 23.0, 38.0):
        gar.append(panel(sc, "pumpkin", flat(PUMPKIN), (x, mantel + 2.5, 3.75), (x + 2.5, mantel + 4.25, 6.25)))
        gar.append(panel(sc, "stem", flat(BEAM), (x + 1.0, mantel + 4.25, 4.75), (x + 1.5, mantel + 5.0, 5.25)))
    for x in (15.0, 31.0):
        gar.append(panel(sc, "cone", flat(CONE), (x, mantel + 2.5, 4.0), (x + 1.25, mantel + 4.0, 5.25)))
    sc.models["garland"] = gar
    # The fire: three sheets of flame across the firebox, one behind another, over the logs.
    fire = []
    for z in (10.0, 11.75, 13.0):
        fire.append({"from": [16.0, y0 + 2.5, z], "to": [32.0, y1 - 1.0, z], "shade": False, "light_emission": 15,
                     "faces": {"north": {"uv": [0, 0, 16, 16], "texture": "#f"}, "south": {"uv": [16, 0, 0, 16], "texture": "#f"}}})
    sc.models["fire"] = fire
    return sc


_built = {}
BUILDERS = {hh.FIREPLACE["block"]: fireplace}


def build(block):
    if block not in _built:
        _built[block] = ppd.planned(BUILDERS[block], 128)
    return _built[block]


def _image_of(name):
    return build(name).atlas.img if name in BUILDERS else None


ITEM_DISPLAY = {"gui": {"rotation": [25, 225, 0], "translation": [0, 0, 0], "scale": [0.625, 0.625, 0.625]},
                "ground": {"rotation": [0, 0, 0], "translation": [0, 2, 0], "scale": [0.3, 0.3, 0.3]},
                "fixed": {"rotation": [0, 0, 0], "translation": [0, 0, 0], "scale": [0.6, 0.6, 0.6]},
                "thirdperson_righthand": {"rotation": [75, 45, 0], "translation": [0, 2, 0], "scale": [0.35, 0.35, 0.35]},
                "firstperson_righthand": {"rotation": [0, 45, 0], "translation": [0, 0, 0], "scale": [0.35, 0.35, 0.35]}}


def fire_model(block, elements):
    """A model of the fire's sheets on its own animated texture."""
    return {"textures": {"particle": f"{MOD}:block/{block}_fire", "f": f"{MOD}:block/{block}_fire"}, "elements": elements}


def assets(root, write, lang):
    """The fireplace's nine blocks' models, cut from the whole, the garland's and the fire's cut the same way, a
    multipart blockstate adding the garland by `garland` and the fire by `lit`, the fire texture's animation, its item
    (the fireplace whole, dressed and lit) and the garland's item (its own icon)."""
    write = fa.closing_writer(write, _image_of)
    spec = hh.FIREPLACE
    block = spec["block"]
    lang[f"block.{MOD}.{block}"] = spec["display"]
    models = root / "models" / "block"
    sc = build(block)
    n = len(d17d.cells(spec["size"]))
    for part, elements in enumerate(ppd.cut(sc.models["whole"], spec["size"])):
        write(models / f"{block}_{part}.json", fa.model(block, elements))
    garland_parts = ppd.cut(sc.models["garland"], spec["size"])
    for part, elements in enumerate(garland_parts):
        if elements:
            write(models / f"{block}_{part}_garland.json", fa.model(block, elements))
    fire_parts = ppd.cut(sc.models["fire"], spec["size"])
    for part, elements in enumerate(fire_parts):
        if elements:
            write(models / f"{block}_{part}_fire.json", fire_model(block, elements))
    multipart = []
    for f, y in FACINGS.items():
        turn = {"y": y} if y else {}
        for p in range(n):
            multipart.append({"when": {"facing": f, "part": str(p)}, "apply": {"model": rid(f"block/{block}_{p}"), **turn}})
            if garland_parts[p]:
                multipart.append({"when": {"facing": f, "part": str(p), "garland": "true"},
                                  "apply": {"model": rid(f"block/{block}_{p}_garland"), **turn}})
            if fire_parts[p]:
                multipart.append({"when": {"facing": f, "part": str(p), "lit": "true"}, "apply": {"model": rid(f"block/{block}_{p}_fire"), **turn}})
    write(root / "blockstates" / f"{block}.json", {"multipart": multipart})
    write(root / "textures" / "block" / f"{block}_fire.png.mcmeta", {"animation": {"frametime": spec["frametime"], "interpolate": True}})
    whole = sc.models["whole"] + sc.models["garland"]
    write(root / "models" / "item" / f"{block}.json",
          fa.model(block, fa.transformed(d17d.pinned(d20d.separated(whole)), 1.0 / 3.0, (8 - 8, 0, 8 - 8 / 3.0), (0, 0, 0)), ITEM_DISPLAY))
    write(root / "items" / f"{block}.json", {"model": {"type": "minecraft:model", "model": rid(f"item/{block}")}})
    item = hh.GARLAND["item"]
    lang[f"item.{MOD}.{item}"] = hh.GARLAND["display"]
    write(root / "models" / "item" / f"{item}.json", {"parent": "minecraft:item/generated", "textures": {"layer0": rid(f"item/{item}")}})
    write(root / "items" / f"{item}.json", {"model": {"type": "minecraft:model", "model": rid(f"item/{item}")}})
    lang[f"message.{MOD}.{block}.light"] = "Light it with flint and steel or a fire charge"


def loot(out, write):
    """The fireplace drops once, from its first block (a garland on it stays with the blocks, so it is dropped too)."""
    block = hh.FIREPLACE["block"]
    write(out / f"{block}.json", {"type": "minecraft:block", "pools": [
        {"condition": {"type": "minecraft:all_of", "terms": [{"type": "minecraft:survives_explosion"},
                                                             {"type": "minecraft:match_block", "blocks": rid(block), "state": {"part": "0"}}]},
         "entries": [{"type": "minecraft:item", "name": rid(block)}], "rolls": 1},
        {"condition": {"type": "minecraft:match_block", "blocks": rid(block), "state": {"part": "0", "garland": "true"}},
         "entries": [{"type": "minecraft:item", "name": rid(hh.GARLAND["item"])}], "rolls": 1}],
        "random_sequence": rid(f"blocks/{block}")})


def tags(tags):
    tags.add("block", "minecraft:mineable/pickaxe", rid(hh.FIREPLACE["block"]))


def textures():
    import item_icons
    block = hh.FIREPLACE["block"]
    return {("block", block): build(block).atlas.img, ("block", f"{block}_fire"): fire_strip(),
            ("item", hh.GARLAND["item"]): item_icons.draw(hh.GARLAND["item"])}

"""Generated assets for Front-Yard Haunt Scenes, decor set 7 (tools/front_yard.py; docs/features/front-yard-haunt.md).

Built from boxes at real size, one texel to a model pixel, as the Haunted Midway's are (tools/haunted_midway_data.py,
whose painters it shares): each material one colour stepped into tones, its marks placed the way it is made.
- Sheet steel (the mailbox): black, lit along its top row and shaded along its foot, a row of rivets along its seam,
  an orange reflector strip along each flank with an amber reflector at its front end. Its inside is dark.
- Weathered wood (the mailbox's post, arm and brace): grey-brown stripes along the grain.
- Wrought iron (the path light's hook): black, in stripes along it.
- The jack-o'-lantern: the midway's ribbed pumpkin, its carved face lit from inside as its own full-bright model, cut out
  to its holes, steady.
- The cobweb: thin strands fanning from the corner between the post and the arm, with two strands across them, cut out.
"""
import math

import decor17_data as d17d
import decor20_data as d20d
import flora_art as fa
import front_yard as fy
import pumpkin_patch_data as ppd
from flora_art import Sculpt, pal, rotation, shade
from haunted_midway_data import (BLACK, BONE, LAMP, ORANGE, STEEL, STEM, clear, face_glow, glow, glowing, pumpkin_side)
from pumpkin_patch_data import flat, lengthwise, panel

MOD = "jugcraft"
FACINGS = ppd.FACINGS
WEATHERED = pal("2e2a26", "3e3832", "4e4740", "5e564d")
RED = pal("4a0e0e", "781a16", "a42a20", "c84432")


def rid(path):
    return path if ":" in path else f"{MOD}:{path}"


# ---------------------------------------------------------------- painters

def sheet(rivets=True):
    """Black sheet steel: its field in the black's middle tone, lit along its top row, shaded along its foot, with a
    steel rivet every third texel along the row above its foot."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                k = 3 if y == 0 and p.h > 2 else 1 if y == p.h - 1 and p.h > 2 else 2
                p.put(x, y, shade(BLACK, k))
        if rivets and p.h >= 4 and p.w >= 4:
            for x in range(1, p.w - 1, 3):
                p.put(x, p.h - 2, shade(STEEL, 2))
    paint.tag = repr(("sheet", rivets))
    return paint


def reflector():
    """The box's flank: the sheet with an orange reflector strip along its middle row and an amber reflector two texels
    square at its front end, lit at its top left."""
    def paint(p):
        sheet()(p)
        mid = p.h // 2
        for x in range(p.w):
            p.put(x, mid, shade(ORANGE, 2))
        for (dx, dy), k in (((0, 0), 4), ((1, 0), 3), ((0, 1), 3), ((1, 1), 2)):
            p.put(p.w - 3 + dx, mid - 1 + dy, shade(ORANGE, k))
    paint.tag = repr(("reflector",))
    return paint


def dark():
    """The dark inside of the box: the black's darkest tone."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                p.put(x, y, shade(BLACK, 0))
    paint.tag = repr(("dark",))
    return paint


def cobweb(right=False):
    """A cobweb in a corner (the piece's top left, or its top right for `right`): five strands fanning from the corner
    and two strands across them, pale and cut out everywhere else."""
    def paint(p):
        put = (lambda x, y, c: p.put(p.w - 1 - x, y, c)) if right else p.put
        for y in range(p.h):
            for x in range(p.w):
                clear(p, x, y)
        size = max(p.w, p.h)
        for k in range(5):
            a = (k + 0.5) * (math.pi / 2) / 5
            for t in range(size * 2):
                x, y = t * 0.5 * math.cos(a), t * 0.5 * math.sin(a)
                if x < p.w and y < p.h:
                    put(int(x), int(y), shade(BONE, 3))
        for r in (0.45 * size, 0.85 * size):
            for t in range(int(r * 3)):
                a = t / (r * 3) * math.pi / 2
                x, y = r * math.cos(a), r * math.sin(a)
                if x < p.w and y < p.h:
                    put(int(x), int(y), shade(BONE, 2))
    paint.tag = repr(("cobweb", right))
    return paint


# ---------------------------------------------------------------- the Haunted Mailbox

def haunted_mailbox():
    """The Haunted Mailbox, facing north (its door to the street), one block across and two up: a weathered post with a
    brace up to the arm that carries the box, the box black sheet steel under an arched top (two steps), its door at the
    front, its flag on its east side, a reflector strip along each flank, a cobweb between the post and the arm.
    Models: `whole`, `flag_down` and `flag_up` (by its `flag`), and its runners: `lid` (the door, hinged at its foot;
    the renderer swings it) and `eyes` (inside, drawn full-bright while the door is open)."""
    spec = fy.MAILBOX
    sc = Sculpt(spec["block"], 2701, 128)
    wood = lengthwise(WEATHERED)
    box_paint = sheet()
    el = [panel(sc, "post", wood, (7.25, 0.0, 8.0), (8.75, 15.5, 9.5)),
          panel(sc, "arm", wood, (7.0, 15.5, 4.0), (9.0, 17.0, 12.5)),
          panel(sc, "brace", wood, (7.5, 11.0, 7.5), (8.5, 16.8, 8.5), rot=rotation((8.0, 11.0, 8.0), "x", -45))]
    for lo, hi in (((5.5, 17.0, 3.0), (10.5, 21.0, 13.0)), ((6.0, 21.0, 3.0), (10.0, 21.75, 13.0)), ((7.0, 21.75, 3.0), (9.0, 22.25, 13.0))):
        flank = reflector() if lo[1] == 17.0 else box_paint
        el.append(panel(sc, "box", box_paint, lo, hi, north=dark(), east=flank, west=flank))
    # The web fans from the corner between the post and the arm: at its top, by the post (its west side's left, its east
    # side's right, as each is seen).
    el.append(panel(sc, "web", cobweb(), (8.0, 12.0, 9.5), (8.0, 15.5, 12.5), sides=("east", "west"), east=cobweb(right=True)))
    sc.models["whole"] = el
    red = flat(RED)
    sc.models["flag_down"] = [panel(sc, "flag_arm", red, (10.5, 18.25, 7.0), (11.0, 18.75, 12.0)),
                              panel(sc, "flag", red, (10.5, 17.75, 5.0), (11.0, 19.25, 7.0))]
    sc.models["flag_up"] = [panel(sc, "flag_arm", red, (10.5, 18.25, 11.5), (11.0, 23.25, 12.0)),
                            panel(sc, "flag", red, (10.5, 21.25, 12.0), (11.0, 23.25, 14.0))]
    sc.models["lid"] = [panel(sc, "door", box_paint, (5.5, 17.0, 2.5), (10.5, 21.0, 3.0)),
                        panel(sc, "door", box_paint, (6.0, 21.0, 2.5), (10.0, 21.75, 3.0)),
                        panel(sc, "door", box_paint, (7.0, 21.75, 2.5), (9.0, 22.25, 3.0)),
                        panel(sc, "latch", flat(STEEL), (7.6, 20.25, 2.0), (8.4, 20.75, 2.5))]
    eye = glow(LAMP)
    sc.models["eyes"] = glowing([panel(sc, "eye", eye, (6.75, 19.0, 2.85), (7.75, 20.0, 3.0)),
                                 panel(sc, "eye", eye, (8.25, 19.0, 2.85), (9.25, 20.0, 3.0))])
    return sc


# ---------------------------------------------------------------- the Pumpkin Path Light

def pumpkin_path_light():
    """The Pumpkin Path Light, facing north (its crook out over the path to the north), one block across and two up: a
    shepherd's hook of black wrought iron with a foot step low on it, its top bent out into the crook and down at its
    tip. Models: `whole`, and its runners: `lantern` (the chain, the bail and the jack-o'-lantern hanging from the
    crook's tip, which the renderer sways) and `glow` (its carved face lit, drawn full-bright while it is lit)."""
    spec = fy.PATH_LIGHT
    sc = Sculpt(spec["block"], 2702, 128)
    iron = lengthwise(BLACK)
    # The crook: the rod bends out over the path through a bevel at each corner, and its tip turns down.
    sc.models["whole"] = [panel(sc, "rod", iron, (7.5, 0.0, 7.5), (8.5, 19.75, 8.5)),
                          panel(sc, "step", iron, (5.5, 2.0, 7.5), (10.5, 2.75, 8.5)),
                          panel(sc, "step_end", iron, (5.5, 1.0, 7.5), (6.25, 2.0, 8.5)),
                          panel(sc, "step_end", iron, (9.75, 1.0, 7.5), (10.5, 2.0, 8.5)),
                          panel(sc, "crook_bend", iron, (7.5, 19.25, 7.0), (8.5, 21.5, 8.0), rot=rotation((8.0, 20.4, 7.5), "x", -45)),
                          panel(sc, "crook", iron, (7.5, 20.5, 4.75), (8.5, 21.5, 7.25)),
                          panel(sc, "crook_bend", iron, (7.5, 19.75, 3.5), (8.5, 22.0, 4.5), rot=rotation((8.0, 20.9, 4.0), "x", 45)),
                          panel(sc, "crook_tip", iron, (7.5, 19.0, 3.0), (8.5, 20.25, 4.0))]
    py, pz = spec["pivot"]
    wire = flat(STEEL)
    pumpkin = pumpkin_side()
    face = pumpkin_side(face=True)
    sc.models["lantern"] = [panel(sc, "link", wire, (7.75, py - 1.25, pz - 0.25), (8.25, py, pz + 0.25)),
                            panel(sc, "link", wire, (7.6, py - 2.25, pz - 0.1), (8.4, py - 1.25, pz + 0.1)),
                            panel(sc, "link", wire, (7.75, py - 3.25, pz - 0.25), (8.25, py - 2.25, pz + 0.25)),
                            panel(sc, "bail", wire, (5.75, py - 3.75, pz - 0.1), (10.25, py - 3.25, pz + 0.1)),
                            panel(sc, "bail", wire, (5.75, 11.5, pz - 0.1), (6.0, py - 3.75, pz + 0.1)),
                            panel(sc, "bail", wire, (10.0, 11.5, pz - 0.1), (10.25, py - 3.75, pz + 0.1)),
                            panel(sc, "lantern_pumpkin", pumpkin, (6.0, 10.0, pz - 2.0), (10.0, 13.5, pz + 2.0), north=face, south=face),
                            panel(sc, "lantern_bulge", pumpkin, (5.75, 10.5, pz - 1.75), (10.25, 13.0, pz + 1.75)),
                            panel(sc, "lantern_stem", lengthwise(STEM), (7.6, 13.5, pz - 0.4), (8.4, 14.25, pz + 0.4))]
    lit = face_glow()
    sc.models["glow"] = glowing([panel(sc, "face_glow", lit, (6.0, 10.0, pz - 2.05), (10.0, 13.5, pz - 2.0), sides=("north",)),
                                 panel(sc, "face_glow", lit, (6.0, 10.0, pz + 2.0), (10.0, 13.5, pz + 2.05), sides=("south",))])
    return sc


# ---------------------------------------------------------------- building and files

_built = {}
BUILDERS = {fy.MAILBOX["block"]: haunted_mailbox, fy.PATH_LIGHT["block"]: pumpkin_path_light}


def build(block):
    if block not in _built:
        _built[block] = ppd.planned(BUILDERS[block], 128)
    return _built[block]


def _image_of(name):
    return build(name).atlas.img if name in BUILDERS else None


def assets(root, write, lang):
    """Each piece's two blocks' models, cut from the whole, its overlays cut the same way (the mailbox's flag down and
    up), its runners whole in its first block's frame, a multipart blockstate adding each by its property (the runner
    alone when `runner`), and its item (the piece whole at half size)."""
    write = fa.closing_writer(write, _image_of)
    models = root / "models" / "block"
    states = root / "blockstates"
    for spec, runners, overlays in ((fy.MAILBOX, {"1": "lid", "2": "eyes"}, [("flag_down", {"flag": "false"}), ("flag_up", {"flag": "true"})]),
                                    (fy.PATH_LIGHT, {"1": "lantern", "2": "glow"}, [])):
        block = spec["block"]
        lang[f"block.{MOD}.{block}"] = spec["display"]
        sc = build(block)
        size = spec["size"]
        n = len(d17d.cells(size))
        for part, pieces in enumerate(ppd.cut(sc.models["whole"], size)):
            write(models / f"{block}_{part}.json", fa.model(block, pieces))
        have = {}
        for name, _ in overlays:
            have[name] = []
            for part, pieces in enumerate(ppd.cut(sc.models[name], size)):
                if pieces:
                    write(models / f"{block}_{part}_{name}.json", fa.model(block, pieces))
                have[name].append(bool(pieces))
        for name in runners.values():
            write(models / f"{block}_{name}.json", fa.model(block, d20d.separated(sc.models[name])))
        multipart = []
        for f, y in FACINGS.items():
            turn = {"y": y} if y else {}
            for p in range(n):
                plain = {"facing": f, "part": str(p), "runner": "0"}
                multipart.append({"when": plain, "apply": {"model": rid(f"block/{block}_{p}"), **turn}})
                for name, when in overlays:
                    if have[name][p]:
                        multipart.append({"when": dict(plain, **when), "apply": {"model": rid(f"block/{block}_{p}_{name}"), **turn}})
            for value, name in runners.items():
                multipart.append({"when": {"facing": f, "runner": value}, "apply": {"model": rid(f"block/{block}_{name}"), **turn}})
        write(states / f"{block}.json", {"multipart": multipart})
        shown = sc.models["whole"] + [e for name, _ in overlays[:1] for e in sc.models[name]] + sc.models[list(runners.values())[0]]
        item = fa.transformed(d17d.pinned(d20d.separated(shown)), 0.5, (4.0, 0.0, 4.0), (0, 0, 0))
        write(root / "models" / "item" / f"{block}.json", fa.model(block, item))
        write(root / "items" / f"{block}.json", {"model": {"type": "minecraft:model", "model": rid(f"item/{block}")}})


def loot(out, write):
    """Each piece drops once, from its first block."""
    for block in fy.blocks():
        write(out / f"{block}.json", {"type": "minecraft:block", "pools": [{
            "condition": {"type": "minecraft:all_of", "terms": [{"type": "minecraft:survives_explosion"},
                                                                {"type": "minecraft:match_block", "blocks": rid(block), "state": {"part": "0"}}]},
            "entries": [{"type": "minecraft:item", "name": rid(block)}], "rolls": 1}], "random_sequence": rid(f"blocks/{block}")})


def tags(tags):
    """The mailbox comes off with an axe (its post is wood), the path light with a pickaxe (it is iron)."""
    tags.add("block", "minecraft:mineable/axe", rid(fy.MAILBOX["block"]))
    tags.add("block", "minecraft:mineable/pickaxe", rid(fy.PATH_LIGHT["block"]))


def textures():
    return {("block", block): build(block).atlas.img for block in fy.blocks()}

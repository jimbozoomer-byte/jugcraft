"""Generated assets for the Clockwork Animatronics, decor set 9 (tools/clockwork.py; docs/features/clockwork-animatronics.md).

Built from boxes at real size, one texel to a model pixel, in the Haunted Midway's and the Front-Yard Haunt's manner,
whose palettes and bones it shares (tools/haunted_midway_data.py, tools/front_yard_data.py).
- Mahogany (the barrel organ's case): deep red-brown, a grain line a tone darker every third texel, its top row lit;
  bound in brass at its corners.
- Brass (the organ's trim, its pipes and its crank): lit like a gold block, its top row bright and its foot dark.
- Bone: its middle tone, lit along its upper or left edge and shaded along the other, its knuckles a tone lighter.
- Felt and cloth (the top hat, its band and the waistcoat): flat, as cloth is; the waistcoat's moth holes in a fixed
  pattern, never at random.
Each figure's moving parts are drawn by its renderer from quads (assets/jugcraft/clockwork_quads.json), cut from the
same boxes as its block and item models; its block model is the rest of it, the whole figure in its lower block's
model (two blocks tall), its upper block's model empty.
"""
import decor20_data as d20d
import flora_art as fa
import clockwork as cw
import pumpkin_patch_data as ppd
from decor6_data import quads
from flora_art import Sculpt, pal, shade
from front_yard_data import RED, bone, skull, vertebrae
from haunted_midway_data import BLACK, BONE, GOLD, STEEL
from pumpkin_patch_data import IRON, WOOD, flat, lengthwise, panel, walls

MOD = "jugcraft"
FACINGS = ppd.FACINGS
MAHOGANY = pal("3a1610", "52201a", "6c2e22", "86402e")


def rid(path):
    return path if ":" in path else f"{MOD}:{path}"


# ---------------------------------------------------------------- painters

def mahogany(lit=True):
    """Mahogany: its middle tone, a grain line a tone darker every third texel across, its top row lit."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                p.put(x, y, shade(MAHOGANY, 3 if lit and y == 0 and p.h > 1 else 1 if x % 3 == 2 else 2))
    paint.tag = repr(("mahogany", lit))
    return paint


def case_side():
    """A side of the organ's case: mahogany, bound in brass down both its edges, its top row lit."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                if x in (0, p.w - 1):
                    p.put(x, y, shade(GOLD, 4 if y == 0 else 1 if y == p.h - 1 else 3 if x == 0 else 2))
                else:
                    p.put(x, y, shade(MAHOGANY, 3 if y == 0 else 1 if x % 3 == 2 else 2))
    paint.tag = repr(("case_side",))
    return paint


def organ_front():
    """The organ's front: a mahogany frame; under its top rail a brass moulding, and above its foot rail another, on
    which the gilded pipes stand in a row a texel each, rising to the middle against red cloth, each pipe lit on
    alternate columns and its top catching the light."""
    def paint(p):
        iw, ih = p.w - 2, p.h - 4
        middle = (iw - 1) / 2
        for y in range(p.h):
            for x in range(p.w):
                if x in (0, p.w - 1) or y in (0, p.h - 1):
                    p.put(x, y, shade(MAHOGANY, 3 if y == 0 else 2))
                elif y in (1, p.h - 2):
                    p.put(x, y, shade(GOLD, 4 if y == 1 else 2))
                else:
                    i, j = x - 1, y - 2
                    rise = 1.0 - abs(i - middle) / (middle or 1)
                    tall = int(ih * (0.5 + 0.5 * rise) + 0.5)
                    if j == ih - tall:
                        p.put(x, y, shade(GOLD, 4))
                    elif j > ih - tall:
                        p.put(x, y, shade(GOLD, 3 if i % 2 == 0 else 2))
                    else:
                        p.put(x, y, shade(RED, 1))
    paint.tag = repr(("organ_front",))
    return paint


def ribs():
    """A ribcage's wall: a rib a texel high on every other row, lit, the gaps between them dark."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                p.put(x, y, shade(BONE, 3) if y % 2 == 0 else shade(BLACK, 0))
    paint.tag = repr(("ribs",))
    return paint


def chest():
    """The ribcage's front: the ribs, and the breastbone down its middle column."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                p.put(x, y, shade(BONE, 2 if y % 2 else 3) if x == p.w // 2 else shade(BONE, 3) if y % 2 == 0 else shade(BLACK, 0))
    paint.tag = repr(("chest",))
    return paint


# Where the moth has been at the waistcoat's back: (column, row), on a piece four texels across or more.
MOTH = ((1, 2), (3, 5), (4, 1))


def waistcoat():
    """A moth-eaten red waistcoat: red cloth, flat, its top row lit; on its back, holes eaten at fixed places (`MOTH`),
    dark."""
    def paint(p):
        holes = {(x % p.w, y % p.h) for x, y in MOTH} if p.w >= 4 and p.h > 2 else set()
        for y in range(p.h):
            for x in range(p.w):
                p.put(x, y, shade(BLACK, 0) if (x, y) in holes else shade(RED, 3 if y == 0 else 2))
    paint.tag = repr(("waistcoat",))
    return paint


def felt(band=False):
    """Black felt (the top hat), lit along its top row; with `band`, its foot row a dark red ribbon."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                p.put(x, y, shade(RED, 1) if band and y == p.h - 1 and p.h > 2 else shade(BLACK, 3 if y == 0 and p.h > 1 else 1))
    paint.tag = repr(("felt", band))
    return paint


def brass():
    """Polished brass: lit like a gold block, its top row bright, its left edge catching the light, its foot dark."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                k = 4 if y == 0 and p.h > 1 else 1 if y == p.h - 1 and p.h > 2 else 3 if x == 0 else 2
                p.put(x, y, shade(GOLD, k))
    paint.tag = repr(("brass",))
    return paint


def tin():
    """A tin cup: dull steel, lit along its rim."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                p.put(x, y, shade(STEEL, 3 if y == 0 else 1))
    paint.tag = repr(("tin",))
    return paint


# ---------------------------------------------------------------- part 1: the Organ-Grinder Skeleton

SKULL = ("BBB", "SBS", "BNB", "TTT")


def organ_grinder_skeleton():
    """The Organ-Grinder Skeleton, facing north, in its lower block's pixels (two blocks tall).
    - The cart: two spoked wheels 37 cm across on an iron axle, a plank bed, a prop under its front and two handles back.
    - The barrel organ on it: a mahogany case 47 cm wide, 37 deep and 55 high, west of the cart's middle so its crank is
      at the skeleton's right hand; its front a brass-bound frame of gilded pipes on red cloth; a mahogany lid; the
      crank's brass boss on its east side.
    - The skeleton, 1.75 m tall, standing behind it: feet, shins, knees, thighs, pelvis and spine; its ribcage in an open
      red waistcoat, moth-eaten, with brass buttons; its collarbones and its left arm hanging at its side, a tin cup on a
      finger.
    Models: `whole` (all but its moving parts); `crank` (the crank's arm and handle, its handle at the top); `upper_arm`
    and `forearm` (the right arm, hanging straight down from its shoulder); `head` (the skull and the top hat); `jaw`."""
    spec = cw.ORGAN_GRINDER
    sc = Sculpt(spec["block"], 2901, 128)
    b, wood, iron = bone(), lengthwise(WOOD), flat(IRON)
    # The cart.
    el = ppd.wheel(sc, "wheel", 3.0, 6.0, 3.0, 1.75, 2.75, rim_t=0.5) + ppd.wheel(sc, "wheel", 3.0, 6.0, 3.0, 10.75, 11.75, rim_t=0.5)
    el.append(panel(sc, "axle", iron, (2.75, 2.75, 5.75), (10.75, 3.25, 6.25)))
    el.append(panel(sc, "bed", wood, (2.75, 5.0, 2.5), (10.75, 5.75, 9.0)))
    el.append(panel(sc, "prop", wood, (6.25, 0.25, 2.75), (7.25, 5.0, 3.5)))
    el.append(panel(sc, "prop_shoe", iron, (6.0, 0.0, 2.5), (7.5, 0.25, 3.75)))
    for x0 in (3.0, 10.25):
        el.append(panel(sc, "handle", wood, (x0, 5.25, 9.0), (x0 + 0.5, 5.75, 12.0)))
    # The organ.
    el.append(panel(sc, "organ", mahogany(), (3.0, 5.75, 2.5), (10.5, 14.5, 8.5),
                    north=organ_front(), east=case_side(), west=case_side()))
    el.append(panel(sc, "organ_lid", mahogany(), (2.75, 14.5, 2.25), (10.75, 15.0, 8.75)))
    cx, cy, cz = spec["crank"]
    el.append(panel(sc, "crank_boss", brass(), (cx, cy - 0.5, cz - 0.5), (cx + 0.25, cy + 0.5, cz + 0.5)))
    # The skeleton's legs and hips.
    for x0 in (5.75, 8.75):
        el.append(panel(sc, "foot", b, (x0, 0.0, 9.75), (x0 + 1.25, 0.5, 12.25)))
        el.append(panel(sc, "shin", b, (x0 + 0.25, 0.5, 11.0), (x0 + 1.0, 7.25, 11.75)))
        el.append(panel(sc, "knee", b, (x0 + 0.2, 7.0, 10.75), (x0 + 1.05, 7.75, 11.5)))
        el.append(panel(sc, "thigh", b, (x0 + 0.25, 7.5, 11.0), (x0 + 1.0, 14.25, 11.75)))
    # The pelvis: its two wings, the sacrum behind between them, the pubic arch in front.
    for x0 in (5.75, 8.5):
        el.append(panel(sc, "pelvis", b, (x0, 14.0, 10.5), (x0 + 1.75, 15.25, 12.0)))
    el.append(panel(sc, "sacrum", b, (7.5, 13.75, 11.25), (8.5, 15.25, 12.25)))
    el.append(panel(sc, "pubis", b, (6.5, 13.75, 10.25), (9.5, 14.25, 10.75)))
    el.append(panel(sc, "spine", vertebrae(), (7.6, 15.25, 11.75), (8.4, 24.0, 12.5)))
    # The ribcage, in its open waistcoat.
    el += walls(sc, "ribs", ribs(), 5.5, 9.75, 10.5, 12.75, 0.25, 17.25, 23.25)
    el[-4] = panel(sc, "chest", chest(), (5.5, 17.25, 9.75), (10.5, 23.25, 10.0))
    coat = waistcoat()
    for x0, x1 in ((5.25, 7.25), (8.75, 10.75)):
        el.append(panel(sc, "waistcoat", coat, (x0, 16.75, 9.5), (x1, 23.25, 9.75)))
    el.append(panel(sc, "waistcoat", coat, (5.25, 16.75, 12.75), (10.75, 23.25, 13.0)))
    for x0 in (5.25, 10.5):
        el.append(panel(sc, "waistcoat", coat, (x0, 16.75, 9.75), (x0 + 0.25, 23.25, 12.75)))
    for y0 in (17.5, 19.0, 20.5, 22.0):
        el.append(panel(sc, "button", brass(), (8.25, y0, 9.4), (8.6, y0 + 0.35, 9.5), sides=("north", "up", "down", "east", "west")))
    el.append(panel(sc, "collarbones", b, (4.75, 23.25, 10.5), (11.25, 23.75, 11.25)))
    el.append(panel(sc, "neck", vertebrae(), (7.65, 23.75, 11.0), (8.35, 25.0, 11.75)))
    # The left arm, hanging at its side, a tin cup hooked on a finger.
    el.append(panel(sc, "arm", b, (4.5, 18.25, 10.25), (5.0, 23.5, 10.75)))
    el.append(panel(sc, "arm", b, (4.5, 11.25, 10.25), (5.0, 18.25, 10.75)))
    el.append(panel(sc, "hand", b, (4.35, 10.5, 10.1), (5.15, 11.75, 10.9)))
    el.append(panel(sc, "cup", tin(), (3.75, 9.25, 10.0), (4.75, 10.25, 11.0), sides=("north", "south", "east", "west", "down")))
    el.append(panel(sc, "cup", tin(), (3.9, 10.0, 10.15), (4.6, 10.1, 10.85), sides=("up",)))
    sc.models["whole"] = el
    # The crank, its handle at the top.
    r = spec["handle"]
    sc.models["crank"] = [panel(sc, "crank_arm", brass(), (cx + 0.1, cy - 0.25, cz - 0.25), (cx + 0.35, cy + r + 0.25, cz + 0.25)),
                          panel(sc, "crank_handle", felt(), (cx + 0.35, cy + r - 0.25, cz - 0.25), (cx + 1.35, cy + r + 0.25, cz + 0.25))]
    # The right arm, hanging from its shoulder: the upper arm, then the forearm and the hand that grips the handle.
    sx, sy, sz = spec["shoulder"]
    upper, lower = spec["arm"]
    sc.models["upper_arm"] = [panel(sc, "arm", b, (sx - 0.25, sy - upper, sz - 0.25), (sx + 0.25, sy, sz + 0.25))]
    ey = sy - upper
    sc.models["forearm"] = [panel(sc, "arm", b, (sx - 0.25, ey - lower + 0.75, sz - 0.25), (sx + 0.25, ey, sz + 0.25)),
                            panel(sc, "hand", b, (sx - 0.4, ey - lower - 0.75, sz - 0.4), (sx + 0.4, ey - lower + 0.75, sz + 0.4))]
    # The skull, its sockets, nose and upper teeth on its front, under a battered top hat; and its jaw.
    face = skull(SKULL)
    sc.models["head"] = [panel(sc, "skull", b, (6.75, 25.0, 9.5), (9.25, 28.25, 12.5), north=face),
                         panel(sc, "hat_brim", felt(), (6.25, 28.25, 9.0), (9.75, 28.5, 13.0)),
                         panel(sc, "hat", felt(band=True), (6.85, 28.5, 9.6), (9.15, 30.75, 12.4))]
    sc.models["jaw"] = [panel(sc, "jaw", b, (7.0, 24.25, 9.5), (9.0, 25.0, 11.5), north=skull(("TT",)))]
    return sc


MOVING = ("crank", "upper_arm", "forearm", "head", "jaw")
# The pose the item shows the figure in, its hand on the crank as near as a block model's turns allow (22.5 degrees at a
# time): the crank turned 112.5 degrees, the upper arm back 22.5, the forearm forward 45.
ITEM_POSE = {"crank": 112.5, "upper_arm": -22.5, "forearm": 45.0}


def posed(sc, spec):
    """The moving parts in ITEM_POSE, as block model boxes."""
    import math
    cx, cy, cz = spec["crank"]
    sx, sy, sz = spec["shoulder"]
    upper, _ = spec["arm"]
    out = []
    # The crank: a quarter turn drawn as its arm laid back along z, then turned the rest of the way.
    turn = ITEM_POSE["crank"] - 90.0
    for e in sc.models["crank"]:
        (x0, y0, z0), (x1, y1, z1) = e["from"], e["to"]
        quarter = dict(e, **{"from": [x0, cy - (z1 - cz), cz + (y0 - cy)], "to": [x1, cy - (z0 - cz), cz + (y1 - cy)]})
        out.append(dict(quarter, rotation=fa.rotation((cx, cy, cz), "x", turn)))
    out += [dict(e, rotation=fa.rotation((sx, sy, sz), "x", ITEM_POSE["upper_arm"])) for e in sc.models["upper_arm"]]
    a = math.radians(ITEM_POSE["upper_arm"])
    ey, ez = sy - upper * math.cos(a), sz - upper * math.sin(a)
    for e in fa.transformed(sc.models["forearm"], 1.0, (0.0, ey - (sy - upper), ez - sz)):
        out.append(dict(e, rotation=fa.rotation((sx, ey, ez), "x", ITEM_POSE["forearm"])))
    return out + sc.models["head"] + sc.models["jaw"]

# ---------------------------------------------------------------- building and files

_built = {}
BUILDERS = {cw.ORGAN_GRINDER["block"]: organ_grinder_skeleton}


def build(block):
    if block not in _built:
        _built[block] = ppd.planned(BUILDERS[block], 128)
    return _built[block]


def _image_of(name):
    return build(name).atlas.img if name in BUILDERS else None


ITEM_DISPLAY = {
    "gui": {"rotation": [25, 200, 0], "translation": [0, -3.25, 0], "scale": [0.42, 0.42, 0.42]},
    "ground": {"rotation": [0, 0, 0], "translation": [0, 1, 0], "scale": [0.25, 0.25, 0.25]},
    "fixed": {"rotation": [0, 180, 0], "translation": [0, -4, 0], "scale": [0.45, 0.45, 0.45]},
    "thirdperson_righthand": {"rotation": [75, 45, 0], "translation": [0, 1.5, 1], "scale": [0.28, 0.28, 0.28]},
    "firstperson_righthand": {"rotation": [0, 45, 0], "translation": [0, 0, 0], "scale": [0.3, 0.3, 0.3]},
}


def assets(root, write, lang):
    """Each figure's lower model (the whole figure but its moving parts) and empty upper model, its blockstate by facing
    and half, its item (the figure whole, at rest), and the moving parts' quads (clockwork_quads.json)."""
    write = fa.closing_writer(write, _image_of)
    models = root / "models" / "block"
    out_quads = {}
    for block in cw.blocks():
        lang[f"block.{MOD}.{block}"] = cw.ORGAN_GRINDER["display"]
        sc = build(block)
        tex = f"{MOD}:block/{block}"
        write(models / f"{block}.json", fa.model(block, d20d.separated(sc.models["whole"])))
        write(models / f"{block}_upper.json", {"parent": "minecraft:block/block", "textures": {"particle": tex}, "elements": []})
        variants = {}
        for f, y in FACINGS.items():
            for half, model in (("lower", block), ("upper", f"{block}_upper")):
                for running in ("false", "true"):
                    variants[f"facing={f},half={half},running={running}"] = {"model": rid(f"block/{model}"), **({"y": y} if y else {})}
        write(root / "blockstates" / f"{block}.json", {"variants": variants})
        shown = sc.models["whole"] + posed(sc, cw.ORGAN_GRINDER)
        write(root / "models" / "item" / f"{block}.json", fa.model(block, d20d.separated(shown), display=ITEM_DISPLAY))
        write(root / "items" / f"{block}.json", {"model": {"type": "minecraft:model", "model": rid(f"item/{block}")}})
        for name in MOVING:
            out_quads[f"{block}_{name}"] = quads(sc.models[name], {"p": block})
    write(root / "clockwork_quads.json", out_quads)


def loot(out, write):
    """Each figure drops itself, from its lower block only."""
    for block in cw.blocks():
        write(out / f"{block}.json", {"type": "minecraft:block", "pools": [{"condition": {"type": "minecraft:all_of", "terms": [
            {"type": "minecraft:survives_explosion"},
            {"type": "minecraft:match_block", "blocks": rid(block), "state": {"half": "lower"}}]},
            "entries": [{"type": "minecraft:item", "name": rid(block)}], "rolls": 1}], "random_sequence": rid(f"blocks/{block}")})


def tags(tags):
    for block in cw.blocks():
        tags.add("block", "minecraft:mineable/axe", rid(block))


def textures():
    return {("block", block): build(block).atlas.img for block in cw.blocks()}

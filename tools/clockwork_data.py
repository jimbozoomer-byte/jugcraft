"""Generated assets for the Clockwork Animatronics, decor set 9 (tools/clockwork.py; docs/features/clockwork-animatronics.md).

Built from boxes at real size, one texel to a model pixel, in the Haunted Midway's and the Front-Yard Haunt's manner,
whose palettes and bones it shares (tools/haunted_midway_data.py, tools/front_yard_data.py).
- Mahogany (the barrel organ's case): deep red-brown, a grain line a tone darker every third texel, its top row lit;
  bound in brass at its corners.
- Brass (the organ's trim, its pipes and its crank): lit like a gold block, its top row bright and its foot dark.
- Bone: its middle tone, lit along its upper or left edge and shaded along the other, its knuckles a tone lighter.
- Felt and cloth (the top hat, its band and the waistcoat): flat, as cloth is; the waistcoat's moth holes in a fixed
  pattern, never at random.
- Cheesecloth (the crank ghost): pale, hanging in folds, torn and worn through in fixed patterns (never at random),
  cut out.
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
from haunted_midway_data import BLACK, BONE, GOLD, STEEL, clear
from pumpkin_patch_data import ALL6, IRON, WOOD, flat, lengthwise, panel, walls

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


# ---------------------------------------------------------------- part 2: the Animatronic Werewolf

WOLF = pal("231c17", "33291f", "45382b", "584838", "6e5c47")
CHEST = pal("3e3429", "55483a", "6c5d4b", "82725d")
DENIM = pal("1a2030", "262f46", "34405e", "455476")
IVORY = pal("8c8168", "b0a688", "d2c9ab", "ebe4cc")
AMBER = (236, 168, 44)


def shaggy(palette=WOLF):
    """Shaggy fur: tufts a texel wide in rows two texels deep, each row's tufts two texels on from the last's, a tuft
    lit, the next dark, so it hangs in locks; its top row lit."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                k = (x + 2 * (y // 2)) % 4
                p.put(x, y, shade(palette, 3 if y == 0 and p.h > 1 else 3 if k == 0 else 1 if k == 2 else 2))
    paint.tag = repr(("shaggy", palette))
    return paint


def ragged():
    """Torn trousers: faded denim, a seam a tone darker every fourth column, the hem along its foot torn in a zigzag
    that shows the fur beneath on every other texel."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                if y == p.h - 1 and p.h > 2 and x % 2 == 0:
                    p.put(x, y, shade(WOLF, 2))
                else:
                    p.put(x, y, shade(DENIM, 3 if y == 0 and p.h > 1 else 1 if x % 4 == 3 else 2))
    paint.tag = repr(("ragged",))
    return paint


def clawed(palette=WOLF):
    """A paw's or a hand's front: fur, and its foot row ivory claws on every other texel."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                if y == p.h - 1 and x % 2 == 0:
                    p.put(x, y, shade(IVORY, 2))
                else:
                    p.put(x, y, shade(palette, 2 if y else 3))
    paint.tag = repr(("clawed", palette))
    return paint


def wolf_face():
    """The werewolf's brow and eyes: fur, its top row lit, two amber eyes at its outer columns on its second row."""
    def paint(p):
        shaggy()(p)
        if p.h > 1 and p.w > 2:
            for x in (0, p.w - 1):
                p.put(x, 1, AMBER)
    paint.tag = repr(("wolf_face",))
    return paint


def muzzle(gum):
    """The front of the werewolf's muzzle, fur above and two white fangs hanging at the outer columns of its foot row;
    with `gum` (its jaw's front, seen when it drops), the gum red between them."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                if y == p.h - 1 and x in (0, p.w - 1):
                    p.put(x, y, (242, 238, 226))
                elif y == p.h - 1 and gum:
                    p.put(x, y, shade(RED, 1))
                else:
                    p.put(x, y, shade(WOLF, 3 if y == 0 and p.h > 1 else 2))
    paint.tag = repr(("muzzle", gum))
    return paint


def riveted():
    """The turntable's brass deck: plain brass, a rivet a tone darker every third texel round the rim of its top; its
    edge (a row high) lit."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                if p.h == 1:
                    p.put(x, y, shade(GOLD, 1 if x % 3 == 0 else 3))
                else:
                    rim = y in (0, p.h - 1) or x in (0, p.w - 1)
                    p.put(x, y, shade(GOLD, 1 if rim and (x + y) % 3 == 0 else 2))
    paint.tag = repr(("riveted",))
    return paint


def iron():
    """Black cast iron: the black's middle tone, lit along its top row."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                p.put(x, y, shade(BLACK, 3 if y == 0 and p.h > 1 else 2))
    paint.tag = repr(("iron",))
    return paint


def round_box(sc, name, painter, w, y0, y1, cx=8.0, cz=8.0, **kw):
    """A round slab `w` across, from y0 to y1: two boxes crossed, each a sixth shorter at its ends."""
    c = w / 6.0
    return [panel(sc, name, painter, (cx - w / 2 + c, y0, cz - w / 2), (cx + w / 2 - c, y1, cz + w / 2), **kw),
            panel(sc, name, painter, (cx - w / 2, y0, cz - w / 2 + c), (cx + w / 2, y1, cz + w / 2 - c), **kw)]


def animatronic_werewolf():
    """The Animatronic Werewolf, facing north, in its lower block's pixels (two blocks tall).
    - A black iron plinth 75 cm across, round (two boxes crossed), with a brass gear housing at its front: the static
      block model (`whole`).
    - On it the brass turntable deck, 69 cm across, riveted round its rim, and the werewolf standing on it, 1.75 m tall
      and hunched: digitigrade legs (long paws clawed at their fronts, hocks high behind, shins forward, thighs back to
      the hips, each a 22.5-degree lean), torn denim trousers to the knee, a shaggy torso leaning forward, its chest a
      lighter fur, and a ruff round its neck. All of it turns with the deck (`spin`).
    - Its arms (`left_arm`, `right_arm`), long and clawed, hanging straight down from its shoulders; its head (`head`:
      the brow with amber eyes, the muzzle with its upper fangs, a black nose and two pointed ears) and its jaw
      (`jaw`), its lower fangs at its front."""
    spec = cw.WEREWOLF
    sc = Sculpt(spec["block"], 2902, 128)
    fur, chest, coat = shaggy(), shaggy(CHEST), ragged()
    sc.models["whole"] = round_box(sc, "plinth", iron(), 12.0, 0.0, 0.75) + [
        panel(sc, "gear_housing", brass(), (7.0, 0.25, 1.25), (9.0, 1.25, 2.25))]
    el = round_box(sc, "deck", riveted(), 11.0, 0.75, 1.5)
    for x0, x1 in ((5.25, 7.0), (9.0, 10.75)):
        mid = (x0 + x1) / 2
        el.append(panel(sc, "paw", fur, (x0, 1.5, 5.0), (x1, 2.25, 8.0), north=clawed()))
        el.append(panel(sc, "foot", fur, (mid - 0.625, 2.25, 7.25), (mid + 0.625, 6.25, 8.25), rot=fa.rotation((mid, 2.25, 7.75), "x", 22.5)))
        el.append(panel(sc, "shin", fur, (mid - 0.625, 5.95, 8.78), (mid + 0.625, 10.95, 9.78), rot=fa.rotation((mid, 5.95, 9.28), "x", -22.5)))
        el.append(panel(sc, "thigh", coat, (x0, 10.57, 6.62), (x1, 15.07, 8.12), rot=fa.rotation((mid, 10.57, 7.37), "x", 22.5)))
    el.append(panel(sc, "hips", coat, (4.75, 13.75, 8.0), (11.25, 16.25, 11.0)))
    el.append(panel(sc, "torso", fur, (4.5, 15.5, 6.5), (11.5, 24.5, 11.5), north=chest, rot=fa.rotation((8.0, 15.5, 10.0), "x", -22.5)))
    el.append(panel(sc, "ruff", fur, (5.5, 22.25, 5.25), (10.5, 24.25, 9.25)))
    sc.models["spin"] = el
    sy, sz = spec["shoulder"]
    for side, (x0, x1) in (("left_arm", (2.5, 4.25)), ("right_arm", (11.75, 13.5))):
        sc.models[side] = [panel(sc, "upper_arm", fur, (x0, sy - 6.0, sz - 0.85), (x1, sy, sz + 0.85)),
                           panel(sc, "forearm", fur, (x0 + 0.1, sy - 11.5, sz - 0.75), (x1 - 0.1, sy - 6.0, sz + 0.75)),
                           panel(sc, "hand", fur, (x0, sy - 13.5, sz - 0.85), (x1, sy - 11.5, sz + 0.85), north=clawed())]
    sc.models["head"] = [panel(sc, "brow", fur, (6.25, 23.5, 3.5), (9.75, 27.0, 7.5), north=wolf_face()),
                         panel(sc, "muzzle", fur, (6.75, 23.75, 1.0), (9.25, 25.75, 3.5), north=muzzle(False)),
                         panel(sc, "nose", flat(BLACK), (7.5, 25.0, 0.75), (8.5, 25.75, 1.0), sides=("north", "up", "down", "east", "west"))]
    for x0, angle in ((6.5, 22.5), (8.5, -22.5)):
        sc.models["head"].append(panel(sc, "ear", fur, (x0, 27.0, 5.5), (x0 + 1.0, 29.0, 6.5), rot=fa.rotation((x0 + 0.5, 27.0, 6.0), "z", angle)))
    sc.models["jaw"] = [panel(sc, "jaw", fur, (7.0, 23.0, 1.25), (9.0, 23.75, 4.0), north=muzzle(True))]
    return sc


WEREWOLF_MOVING = ("spin", "left_arm", "right_arm", "head", "jaw")


def werewolf_posed(sc, spec):
    """The werewolf's item pose: on its deck, its arms forward at rest (`arms_rest`, a block model's turn), its head and
    jaw closed."""
    sy, sz = spec["shoulder"]
    arms = [dict(e, rotation=fa.rotation((8.0, sy, sz), "x", spec["arms_rest"])) for side in ("left_arm", "right_arm") for e in sc.models[side]]
    return sc.models["spin"] + arms + sc.models["head"] + sc.models["jaw"]

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

# ---------------------------------------------------------------- part 3: the Flying Crank Ghost

GAUZE = pal("6c747a", "959ca2", "b9c0c4", "d7dcde", "eef0ee")
HOLLOW = (12, 12, 18)
GLOW = (196, 255, 212)
# How far up each strip of cloth is torn from its foot (texels), column by column round a piece, over and over: the
# hood's and the cape's hems a little, the robe's into long strips, the cuffs between.
HOOD_TEAR = (1, 0, 1, 2)
CAPE_TEAR = (1, 0, 2, 1, 0, 2, 1)
ROBE_TEAR = (2, 6, 1, 4, 7, 3, 5, 0, 6, 2)
CUFF_TEAR = (3, 1, 2, 0)


def cheesecloth(tears=(), offset=0, holes=False):
    """Loose cheesecloth gone pale: folds hanging down it, a column lit, the next lit, one in between, one in shadow, over
    and over; its top row lit. Its foot torn into strips, column by column (`tears`, counted from `offset` so the pieces
    round a part go on where the last left off), cut out; with `holes`, worn through in a staggered grid (a texel in
    every fourth column of every fourth row, each band of rows two columns along from the last), cut out, never in the
    torn rows or the row above them (so no scrap of it hangs loose)."""
    def paint(p):
        torn = bool(tears) and p.h > max(tears)
        sound = p.h - 1 - (max(tears) if torn else 0)
        for y in range(p.h):
            for x in range(p.w):
                c = x + offset
                if torn and p.h - 1 - y < tears[c % len(tears)]:
                    clear(p, x, y)
                elif holes and y < sound and y % 4 == 2 and c % 4 == (1 if (y // 4) % 2 == 0 else 3):
                    clear(p, x, y)
                else:
                    p.put(x, y, shade(GAUZE, 4 if y == 0 and p.h > 1 else (3, 3, 2, 1)[c % 4]))
    paint.tag = repr(("cheesecloth", tuple(tears), offset, holes))
    return paint


def ghost_face():
    """The ghost's face, three texels across and four high, the cheesecloth drawn tight over it: its brow lit; under it two
    hollow eye sockets at its outer columns and between them the bridge of its nose; below, its mouth agape down the
    middle, its chin in shadow either side."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                hollow = (y == 1 and x in (0, p.w - 1)) or (y >= 2 and x == p.w // 2)
                p.put(x, y, HOLLOW if hollow else shade(GAUZE, 3 if y == 0 else 1 if y == p.h - 1 else 2))
    paint.tag = repr(("ghost_face",))
    return paint


def rag_tube(sc, name, x0, x1, z0, z1, t, y0, y1, tears, holes=False):
    """Four cloth walls `t` thick round x0..x1 by z0..z1, from y0 to y1, open at the foot, where each is torn
    (`cheesecloth`): the north and south walls the full width, the east and west between them. Their tears are counted on
    round the tube, so the strips go on from wall to wall."""
    walls = (((x0, z0), (x1, z0 + t)), ((x1 - t, z0 + t), (x1, z1 - t)), ((x0, z1 - t), (x1, z1)), ((x0, z0 + t), (x0 + t, z1 - t)))
    out, offset = [], 0
    for (ax, az), (bx, bz) in walls:
        rag = cheesecloth(tears, offset, holes)
        out.append(panel(sc, name, rag, (ax, y0, az), (bx, y1, bz), sides=("north", "south", "east", "west", "up"),
                         up=cheesecloth(holes=False)))
        offset += int(round(max(bx - ax, bz - az)))
    return out


def glow():
    """An eye's lamp: pale green, flat; the renderer draws it at full light while the ghost runs."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                p.put(x, y, GLOW)
    paint.tag = repr(("glow",))
    return paint


def wire():
    """Steel wire: dull and flat (the renderer stretches it to length, so it has no pattern along it to stretch)."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                p.put(x, y, shade(STEEL, 0))
    paint.tag = repr(("wire",))
    return paint


def round_plate(sc, name, painter, w, z0, z1, cx, cy, cut=0.5, **kw):
    """A round plate `w` across standing across z, from z0 to z1, centred on (cx, cy): two boxes crossed, each `cut`
    shorter at its ends."""
    return [panel(sc, name, painter, (cx - w / 2 + cut, cy - w / 2, z0), (cx + w / 2 - cut, cy + w / 2, z1), **kw),
            panel(sc, name, painter, (cx - w / 2, cy - w / 2 + cut, z0), (cx + w / 2, cy + w / 2 - cut, z1), **kw)]


WIRE = 0.25
SIDES4 = ("north", "south", "east", "west")


def flying_crank_ghost():
    """The Flying Crank Ghost, facing north, in its lower block's pixels (two blocks tall).
    - The frame (`whole`): a round black iron base plate 50 cm across, a riveted brass gear housing on it at the back,
      where its drive comes in; an iron post 2 m tall from it, a brass coupling at its foot, another half way up and a
      cap on its top; an iron boom forward from its top, braced at 45 degrees; across the boom's front, an iron
      crossbar 94 cm long, a brass pulley hung at each end in front of it on an iron strap; and on it, where the boom
      meets it, a riveted brass gearbox.
    - The crank (`crank`): a brass disc 19 cm across on the gearbox's front, its iron pin at the top.
    - The ghost (`ghost`), hanging from the pin by its hood's top: a cheesecloth hood round its pale face, hollow-eyed
      and open-mouthed; over its shoulders a hollow cape, its hem torn; and from under that a hollow robe falling to
      1.3 m below its hood's top, worn through and torn at its foot into strips. Its eyes' lamps (`eyes`).
    - Its arms (`left_arm`, `right_arm`): thin cheesecloth sleeves hanging straight down from its shoulders, their cuffs
      worn through and torn.
    - A wire (`wire`), 16 pixels long up from its foot, in the plane of the wires, which the renderer stretches and
      turns into each of the five."""
    spec = cw.FLYING_GHOST
    sc = Sculpt(spec["block"], 2903, 128)
    cast, br, studded = iron(), brass(), riveted()
    cx, cy = spec["crank"]
    wz = spec["wire_z"]
    el = [panel(sc, "base", cast, (5.5, 0.0, 8.0), (10.5, 0.75, 16.0)), panel(sc, "base", cast, (4.0, 0.0, 9.5), (12.0, 0.75, 14.5))]
    el.append(panel(sc, "housing", studded, (6.5, 0.75, 10.5), (9.5, 2.75, 14.5)))
    for y0, y1 in ((2.75, 3.5), (15.5, 16.25), (31.0, 31.5)):
        el.append(panel(sc, "coupling", br, (7.0, y0, 11.0), (9.0, y1, 13.0)))
    el.append(panel(sc, "post", cast, (7.5, 3.0, 11.5), (8.5, 31.0, 12.5)))
    el.append(panel(sc, "boom", cast, (7.5, 30.0, 8.5), (8.5, 31.0, 11.5)))
    el.append(panel(sc, "brace", cast, (7.75, 28.5, 8.48), (8.25, 29.0, 12.02), rot=fa.rotation((8.0, 28.75, 10.25), "x", 45.0)))
    el.append(panel(sc, "crossbar", cast, (0.5, 29.5, 7.25), (15.5, 30.5, 8.25)))
    for px in spec["pulleys"]:
        py = spec["pulley_y"]
        el += round_plate(sc, "pulley", br, 2.0, wz - 0.25, wz + 0.25, px, py)
        el.append(panel(sc, "strap", cast, (px - 0.25, py + 0.25, wz), (px + 0.25, py + 1.25, 7.5)))
    el.append(panel(sc, "gearbox", studded, (6.0, 28.0, 7.0), (10.0, 32.0, 9.0)))
    sc.models["whole"] = el
    # The crank, its pin at the top.
    r = spec["pin"]
    sc.models["crank"] = round_plate(sc, "disc", br, 3.0, wz + 0.25, 7.0, cx, cy) + [
        panel(sc, "pin", cast, (cx - 0.25, cy + r - 0.25, wz - 0.25), (cx + 0.25, cy + r + 0.25, wz + 0.25))]
    # The ghost: its hood and its face, the cape over its shoulders and the robe falling from under it.
    hx, hy = spec["head_top"]
    cloth, hood = cheesecloth(), cheesecloth(HOOD_TEAR)
    g = [panel(sc, "crown", cloth, (hx - 1.5, hy - 0.5, wz - 1.5), (hx + 1.5, hy, wz + 1.5)),
         panel(sc, "hood", cloth, (hx - 2.0, hy - 1.5, wz - 2.0), (hx + 2.0, hy - 0.5, wz + 2.0)),
         panel(sc, "face", cloth, (hx - 1.5, hy - 5.5, wz - 1.75), (hx + 1.5, hy - 1.5, wz + 1.5), north=ghost_face())]
    for x0 in (hx - 2.0, hx + 1.5):
        g.append(panel(sc, "hood", hood, (x0, hy - 5.5, wz - 2.0), (x0 + 0.5, hy - 1.5, wz + 2.0), sides=SIDES4 + ("up",)))
    g.append(panel(sc, "hood", hood, (hx - 1.5, hy - 5.5, wz + 1.5), (hx + 1.5, hy - 1.5, wz + 2.0), sides=SIDES4 + ("up",)))
    sy = spec["shoulder_y"]
    g += rag_tube(sc, "cape", hx - 3.0, hx + 3.0, wz - 1.5, wz + 1.5, 0.5, sy - 6.0, sy + 1.0, CAPE_TEAR)
    g.append(panel(sc, "cape", cloth, (hx - 2.5, sy + 0.5, wz - 1.0), (hx + 2.5, sy + 1.0, wz + 1.0)))
    g += rag_tube(sc, "robe", hx - 2.0, hx + 2.0, wz - 0.75, wz + 0.75, 0.5, sy - 15.5, sy + 0.5, ROBE_TEAR, holes=True)
    sc.models["ghost"] = g
    sc.models["eyes"] = [panel(sc, "eye", glow(), (x0, hy - 3.5, wz - 1.85), (x0 + 1.0, hy - 2.5, wz - 1.77))
                         for x0 in (hx - 1.5, hx + 0.5)]
    # The arms, hanging straight down from the shoulders: a sleeve to the wrist, and its cuff.
    for side, px in zip(("left_arm", "right_arm"), spec["shoulders"]):
        wrist = sy - spec["wrist"]
        sc.models[side] = [panel(sc, "sleeve", cloth, (px - 0.5, wrist + 0.5, wz - 0.25), (px + 0.5, sy + 0.5, wz + 0.25)),
                           panel(sc, "cuff", cheesecloth(CUFF_TEAR, holes=True), (px - 1.0, wrist - 4.5, wz - 0.5), (px + 1.0, wrist + 0.5, wz + 0.5),
                                 sides=SIDES4 + ("up",), up=cloth)]
    sc.models["wire"] = [panel(sc, "wire", wire(), (-WIRE / 2, 0.0, wz - WIRE / 2), (WIRE / 2, 16.0, wz + WIRE / 2))]
    sc.models["posed_wires"] = posed_wires(sc, spec)
    return sc


GHOST_MOVING = ("crank", "ghost", "eyes", "left_arm", "right_arm", "wire")
# The ghost's parts drawn cut out (its cheesecloth is torn and worn through).
GHOST_CUTOUT = ("ghost", "left_arm", "right_arm")


def posed_wires(sc, spec):
    """The ghost's wires as its item shows them, as near as a block model's turns allow (22.5 degrees at a time): with
    the crank's pin at the top and both arms half raised (`ghost_posed`), straight up from its hood to the pin, from the
    pin out to each pulley 22.5 degrees down, and from each pulley straight down to the sleeve."""
    import math
    cx, cy = spec["crank"]
    wz, hw = spec["wire_z"], WIRE / 2
    hx, hy = spec["head_top"]
    sy, py = spec["shoulder_y"], spec["pulley_y"]
    lift = math.radians(ITEM_LIFT)
    pin = cy + spec["pin"]
    out = [panel(sc, "wire", wire(), (hx - hw, hy, wz - hw), (hx + hw, pin, wz + hw))]
    for px, shoulder, sign in zip(spec["pulleys"], spec["shoulders"], (-1, 1)):
        reach = math.hypot(px - cx, pin - py)
        lo, hi = (cx - reach, cx) if sign < 0 else (cx, cx + reach)
        out.append(panel(sc, "wire", wire(), (lo, pin - hw, wz - hw), (hi, pin + hw, wz + hw),
                         rot=fa.rotation((cx, pin, wz), "z", -sign * 22.5)))
        hand = sy - spec["wrist"] * math.cos(lift)
        wx = (px + shoulder + sign * spec["wrist"] * math.sin(lift)) / 2
        out.append(panel(sc, "wire", wire(), (wx - hw, hand, wz - hw), (wx + hw, py, wz + hw)))
    return out


# The ghost's arms as its item shows them: half raised (`arms_rest` + `flap` / 2).
ITEM_LIFT = cw.FLYING_GHOST["arms_rest"] + cw.FLYING_GHOST["flap"] / 2


def ghost_posed(sc, spec):
    """The ghost's item pose: the crank's pin at the top, the ghost hanging straight under it, both arms half raised
    (45 degrees), and its wires (`posed_wires`)."""
    sy, wz = spec["shoulder_y"], spec["wire_z"]
    out = sc.models["crank"] + sc.models["ghost"] + sc.models["eyes"] + sc.models["posed_wires"]
    for side, shoulder, sign in zip(("left_arm", "right_arm"), spec["shoulders"], (-1, 1)):
        out += [dict(e, rotation=fa.rotation((shoulder, sy, wz), "z", sign * ITEM_LIFT)) for e in sc.models[side]]
    return out


# ---------------------------------------------------------------- building and files

_built = {}
BUILDERS = {cw.ORGAN_GRINDER["block"]: organ_grinder_skeleton, cw.WEREWOLF["block"]: animatronic_werewolf,
            cw.FLYING_GHOST["block"]: flying_crank_ghost}
# Each figure's moving parts (clockwork_quads.json, `<block>_<part>`), and the parts its item shows, posed.
PARTS = {cw.ORGAN_GRINDER["block"]: MOVING, cw.WEREWOLF["block"]: WEREWOLF_MOVING, cw.FLYING_GHOST["block"]: GHOST_MOVING}
POSES = {cw.ORGAN_GRINDER["block"]: posed, cw.WEREWOLF["block"]: werewolf_posed, cw.FLYING_GHOST["block"]: ghost_posed}
# The moving parts drawn cut out, by figure.
CUTOUT = {cw.FLYING_GHOST["block"]: GHOST_CUTOUT}


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
    for spec in cw.figures():
        block = spec["block"]
        lang[f"block.{MOD}.{block}"] = spec["display"]
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
        shown = sc.models["whole"] + POSES[block](sc, spec)
        write(root / "models" / "item" / f"{block}.json", fa.model(block, d20d.separated(shown), display=ITEM_DISPLAY))
        write(root / "items" / f"{block}.json", {"model": {"type": "minecraft:model", "model": rid(f"item/{block}")}})
        for name in PARTS[block]:
            drawn = quads(sc.models[name], {"p": block})
            if name in CUTOUT.get(block, ()):
                for quad in drawn:
                    quad["cutout"] = True
            out_quads[f"{block}_{name}"] = drawn
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

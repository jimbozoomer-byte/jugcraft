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
from haunted_midway_data import (BLACK, BONE, GREEN, HAIR, LAMP, ORANGE, STEEL, STEM, clear, face_glow, glow, glowing, pumpkin_side,
                                 zombie_skin)
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


# ---------------------------------------------------------------- part 2: the bones and the risen

DIRT = pal("2a1c12", "3a2818", "4c3622", "5e462e")
LINEN = pal("5e5a4c", "7c7766", "9c9682", "b8b29c")


def bone(palette=BONE):
    """A bone: its middle tone, lit along its upper (or left) edge and shaded along the other, its two ends (the
    knuckles) a tone lighter."""
    def paint(p):
        along_x = p.w >= p.h
        length, across = (p.w, p.h) if along_x else (p.h, p.w)
        for y in range(p.h):
            for x in range(p.w):
                a, l = (y, x) if along_x else (x, y)
                k = 3 if (a == 0 and across > 1) else 1 if (a == across - 1 and across > 2) else 2
                if length >= 4 and l in (0, length - 1):
                    k = min(4, k + 1)
                p.put(x, y, shade(palette, k))
    paint.tag = repr(("bone", palette))
    return paint


def vertebrae():
    """A spine or a tail: a knob of bone every other texel along it, the joints between them shaded, lit along its
    upper (or left) edge."""
    def paint(p):
        along_x = p.w >= p.h
        across = p.h if along_x else p.w
        for y in range(p.h):
            for x in range(p.w):
                a, l = (y, x) if along_x else (x, y)
                k = 3 if l % 2 == 0 else 1
                if a == 0 and across > 1:
                    k += 1
                p.put(x, y, shade(BONE, k))
    paint.tag = repr(("vertebrae",))
    return paint


SKULL_COLOURS = {"S": BLACK[0], "N": BLACK[1], "T": BONE[4]}


def skull(rows):
    """A skull's face, row by row from its top: `B` bone (lit along the top row), `S` an eye socket (square, dark), `N`
    a nasal hole, `T` a tooth."""
    def paint(p):
        for y in range(p.h):
            row = rows[min(y, len(rows) - 1)]
            for x in range(p.w):
                ch = row[min(x, len(row) - 1)]
                p.put(x, y, SKULL_COLOURS.get(ch) or shade(BONE, 3 if y == 0 else 2))
    paint.tag = repr(("skull", tuple(rows)))
    return paint


def bird_side(front_right):
    """A bird's skull seen from the side: bone, its eye socket (on the side, as a bird's is) the top texel at its front:
    at the piece's right when `front_right` (as an east side shows a north-facing head), else its left."""
    def paint(p):
        bone()(p)
        p.put(p.w - 1 if front_right else 0, 0, SKULL_COLOURS["S"])
    paint.tag = repr(("bird_side", front_right))
    return paint


def earth():
    """Fresh-dug earth: its middle tone, a lit clod and its dark shadow under it in a staggered pattern, a texel apart."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                k = (x + 3 * y) % 7
                p.put(x, y, shade(DIRT, 3 if k == 0 else 0 if k == 3 else 2 if k % 2 else 1))
    paint.tag = repr(("earth",))
    return paint


def shirt(rip=False):
    """A burial shirt of coarse linen hanging in folds: its middle tone, a fold shaded every third texel across, lit
    along its top. Ripped (`rip`, its front), a tear runs down it from its left shoulder: green flesh, the zombie's ribs
    showing through it in every other row, the linen frayed pale along its edges."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                p.put(x, y, shade(LINEN, 3 if y == 0 else 1 if x % 3 == 2 else 2))
        if rip:
            for y in range(2, p.h - 2):
                for x in range(p.w):
                    d = x - y + 1
                    if d in (0, 1):
                        p.put(x, y, shade(BONE, 3) if y % 2 == 0 else shade(GREEN, 0))
                    elif d in (-1, 2):
                        p.put(x, y, shade(LINEN, 3))
    paint.tag = repr(("shirt", rip))
    return paint


def sleeve():
    """A shirt sleeve torn off at the elbow: the linen, its folds down it, its top row ragged, linen and flesh by turns."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                if y == 0:
                    p.put(x, y, shade(GREEN, 2) if x % 2 else shade(LINEN, 3))
                else:
                    p.put(x, y, shade(LINEN, 1 if x % 3 == 2 else 2))
    paint.tag = repr(("sleeve",))
    return paint


def finger():
    """A zombie's finger: grey-green flesh, lit along its left, its top texel a black, broken nail."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                p.put(x, y, shade(BLACK, 0) if y == 0 else shade(GREEN, 3 if x == 0 and p.w > 1 else 2))
    paint.tag = repr(("finger",))
    return paint


ZOMBIE_FACE = ("HHH", "SZS", "ZNZ", "ZMZ")


def zombie_face():
    """The zombie's face, three texels across and four high: dark hair along its top, two sunken black eyes, a rotted
    nose and a gaping mouth."""
    def paint(p):
        colours = {"H": shade(HAIR, 1), "S": shade(BLACK, 0), "Z": shade(GREEN, 2), "N": shade(GREEN, 0), "M": shade(BLACK, 1)}
        for y in range(p.h):
            row = ZOMBIE_FACE[min(y, 3)]
            for x in range(p.w):
                p.put(x, y, colours[row[min(x, 2)]])
    paint.tag = repr(("zombie_face_3x4",))
    return paint


def tilted(elements, origin, angle, axis="x"):
    """A group of boxes turned together as one piece, `angle` degrees about `axis` through `origin`."""
    turn = rotation(origin, axis, angle)
    for e in elements:
        assert "rotation" not in e, e
        e["rotation"] = dict(turn)
    return elements


def pair(sc, name, painter, lo, hi, **kw):
    """A box and its mirror image across the piece's middle (x = 8): a left and a right leg, rib or wing."""
    mirror = (16.0 - hi[0], lo[1], lo[2]), (16.0 - lo[0], hi[1], hi[2])
    return [panel(sc, name, painter, lo, hi, **kw), panel(sc, name, painter, *mirror, **kw)]


def rib_hoops(sc, b, hoops, top, thick):
    """Rib hoops, each `(z, bottom, half-width)` round the spine at x = 8, from `bottom` up to `top`: two sides the
    full height and a bar across their foot, open above for the spine."""
    out = []
    for zc, bottom, hw in hoops:
        z0, z1 = zc - thick / 2, zc + thick / 2
        out += pair(sc, "rib", b, (8.0 - hw, bottom, z0), (8.0 - hw + thick, top, z1))
        out.append(panel(sc, "rib", b, (8.0 - hw + thick, bottom, z0), (8.0 + hw - thick, bottom + thick, z1)))
    return out


def skeleton_dog():
    """The Skeleton Dog, facing north: a big dog's bones sitting up, its front legs straight under its chest, its hind
    legs folded flat beside it, its ribcage hanging from a spine tipped up 45 degrees from its hips, its neck rising to a
    skull with square sockets over its muzzle and its jaw agape. Models: `whole`, and its runner `tail` (lying along the
    ground behind it; the renderer sweeps it about its root)."""
    spec = fy.SKELETON_DOG
    sc = Sculpt(spec["block"], 2703, 128)
    b = bone()
    # The trunk, drawn level (its spine along the depth, its ribs hanging under it) and tipped up about the hips.
    trunk = [panel(sc, "spine", vertebrae(), (7.6, 2.0, 4.5), (8.4, 2.8, 12.0))]
    trunk += rib_hoops(sc, b, ((5.75, -2.0, 1.75), (7.0, -1.75, 1.75), (8.25, -1.25, 1.5), (9.5, -0.5, 1.25)), 2.25, 0.5)
    trunk += pair(sc, "scapula", b, (6.0, 0.25, 4.75), (6.25, 2.5, 6.5))
    trunk += pair(sc, "pelvis", b, (6.9, 0.75, 10.25), (7.4, 2.5, 13.0))
    el = tilted(trunk, (8.0, 2.25, 12.0), 45)
    # Each front leg: the upper arm from the shoulder back to the elbow, the forearm straight down, the paw flat.
    el += pair(sc, "humerus", b, (6.4, 3.25, 5.75), (6.9, 6.25, 6.25), rot=rotation((6.65, 4.75, 6.0), "x", -22.5))
    el += pair(sc, "radius", b, (6.45, 0.5, 6.1), (6.85, 3.6, 6.6))
    el += pair(sc, "paw", b, (6.15, 0.0, 5.25), (7.15, 0.5, 6.75))
    # Each hind leg folded: the thigh forward from the hip to the knee, the shin back to the hock, the foot flat forward.
    el += pair(sc, "femur", b, (6.1, 1.5, 9.0), (6.7, 2.1, 12.3), rot=rotation((6.4, 1.8, 10.65), "x", -22.5))
    el += pair(sc, "tibia", b, (5.9, 0.7, 9.0), (6.4, 1.2, 12.75), rot=rotation((6.15, 0.95, 10.875), "x", 22.5))
    el += pair(sc, "foot", b, (6.0, 0.0, 9.5), (6.6, 0.5, 12.5))
    el += pair(sc, "paw", b, (5.85, 0.0, 8.5), (6.85, 0.4, 9.5))
    # The neck up from the shoulders, leaning forward, and the skull on it.
    el.append(panel(sc, "neck", vertebrae(), (7.65, 7.0, 6.0), (8.35, 9.5, 6.8), rot=rotation((8.0, 8.25, 6.4), "x", -22.5)))
    el.append(panel(sc, "cranium", b, (6.5, 9.0, 4.0), (9.5, 11.0, 6.5), north=skull(("BBB", "SBS"))))
    el.append(panel(sc, "muzzle", b, (7.5, 8.75, 2.0), (8.5, 9.75, 4.0), north=skull(("N",))))
    el.append(panel(sc, "teeth", skull(("T",)), (7.5, 8.5, 2.0), (8.5, 8.75, 2.5)))
    el.append(panel(sc, "jaw", b, (7.4, 8.25, 2.25), (8.6, 8.75, 5.75), north=skull(("T",)), rot=rotation((8.0, 8.5, 5.5), "x", -22.5)))
    sc.models["whole"] = el
    tail = vertebrae()
    sc.models["tail"] = [panel(sc, "tail", tail, (7.7, 0.25, 12.0), (8.3, 0.85, 13.5)),
                         panel(sc, "tail", tail, (7.7, 0.0, 13.5), (8.3, 0.5, 14.5)),
                         panel(sc, "tail", tail, (8.3, 0.0, 14.0), (9.5, 0.5, 14.5))]
    return sc


def skeleton_cat():
    """The Skeleton Cat, facing north: a cat's bones arched like a Halloween cat's, stiff-legged, its back bowed up, its
    skull's sockets wide and square, hissing, its fangs bared. Models: `whole`, and its runner `tail` (held up and
    curling over at its tip; the renderer sways it from side to side about its root)."""
    spec = fy.SKELETON_CAT
    sc = Sculpt(spec["block"], 2704, 128)
    b = bone()
    spine = vertebrae()
    el = []
    for z0 in (4.75, 9.75):
        el += pair(sc, "leg", b, (7.0, 0.4, z0), (7.5, 4.0, z0 + 0.5))
        el += pair(sc, "paw", b, (6.85, 0.0, z0 - 0.5), (7.65, 0.4, z0 + 0.5))
    # The arched back: up from the shoulders, over, and down to the hips.
    el.append(panel(sc, "spine", spine, (7.7, 4.5, 4.6), (8.3, 5.1, 7.0), rot=rotation((8.0, 4.8, 5.8), "x", -45)))
    el.append(panel(sc, "spine", spine, (7.65, 5.3, 6.4), (8.35, 5.9, 8.6)))
    el.append(panel(sc, "spine", spine, (7.7, 4.5, 8.0), (8.3, 5.1, 10.4), rot=rotation((8.0, 4.8, 9.2), "x", 45)))
    for zc, bottom, top in ((5.75, 2.75, 4.75), (6.75, 3.0, 5.4), (7.75, 3.25, 5.4)):
        el += pair(sc, "rib", b, (7.15, bottom, zc - 0.2), (7.55, top, zc + 0.2))
        el.append(panel(sc, "rib", b, (7.55, bottom, zc - 0.2), (8.45, bottom + 0.4, zc + 0.2)))
    el += pair(sc, "pelvis", b, (7.2, 3.6, 9.0), (7.55, 4.6, 10.5))
    el.append(panel(sc, "neck", spine, (7.75, 4.0, 4.1), (8.25, 5.5, 4.7), rot=rotation((8.0, 4.75, 4.4), "x", -22.5)))
    # The skull: its sockets open between the brow, the bridge of its nose and its cheekbones, dark inside.
    el.append(panel(sc, "cranium", b, (7.0, 5.0, 3.25), (9.0, 7.0, 4.75), north=skull(("S",))))
    el.append(panel(sc, "brow", b, (7.0, 6.25, 2.75), (9.0, 7.0, 3.25)))
    el.append(panel(sc, "bridge", b, (7.75, 5.25, 2.75), (8.25, 6.25, 3.25)))
    el.append(panel(sc, "cheek", b, (7.0, 5.0, 2.75), (9.0, 5.25, 3.25)))
    el.append(panel(sc, "muzzle", b, (7.5, 4.25, 2.25), (8.5, 5.0, 3.25), north=skull(("N",))))
    el += pair(sc, "fang", skull(("T",)), (7.55, 3.75, 2.3), (7.8, 4.25, 2.55))
    el.append(panel(sc, "jaw", b, (7.6, 3.9, 2.5), (8.4, 4.25, 4.25), north=skull(("T",)), rot=rotation((8.0, 4.1, 4.0), "x", -22.5)))
    sc.models["whole"] = el
    sc.models["tail"] = [panel(sc, "tail", spine, (7.75, 3.75, 10.6), (8.25, 7.0, 11.1), rot=rotation((8.0, 3.75, 10.85), "x", 22.5)),
                         panel(sc, "tail", spine, (7.7, 6.5, 11.6), (8.3, 8.5, 12.1)),
                         panel(sc, "tail", spine, (7.75, 8.25, 10.75), (8.25, 8.75, 11.6))]
    return sc


def skeleton_crow():
    """The Skeleton Crow, facing north: a crow's bones perched, its body tipped up, its keel under its ribs, its wings
    folded along its sides, standing on its two legs and its toes. Models: `whole`, and its runner `head` (the skull
    with its eye sockets on its sides as a bird's are, and its beak; the renderer turns it about its neck)."""
    spec = fy.SKELETON_CROW
    sc = Sculpt(spec["block"], 2705, 128)
    b = bone()
    body = [panel(sc, "spine", vertebrae(), (7.7, 4.0, 5.5), (8.3, 4.5, 9.5)),
            panel(sc, "keel", b, (7.85, 2.0, 5.25), (8.15, 2.8, 8.0)),
            panel(sc, "pygostyle", vertebrae(), (7.8, 3.75, 9.5), (8.2, 4.25, 10.75))]
    body += rib_hoops(sc, b, ((6.25, 2.75, 0.9), (7.0, 2.75, 0.9), (7.75, 2.9, 0.85)), 4.25, 0.4)
    body += pair(sc, "pelvis", b, (7.3, 3.5, 8.25), (7.65, 4.4, 9.75))
    # Each folded wing: the humerus back along the side, the forearm under it, the hand back over the tail.
    body += pair(sc, "humerus", b, (6.75, 3.75, 5.75), (7.05, 4.15, 8.0))
    body += pair(sc, "ulna", b, (6.7, 3.35, 6.0), (7.0, 3.75, 9.25))
    body += pair(sc, "hand", b, (6.8, 3.75, 8.75), (7.1, 4.05, 10.25))
    el = tilted(body, (8.0, 3.5, 7.5), 22.5)
    el += pair(sc, "tibiotarsus", b, (7.3, 1.5, 7.6), (7.6, 3.25, 7.95), rot=rotation((7.45, 2.4, 7.8), "x", 22.5))
    el += pair(sc, "tarsus", b, (7.35, 0.25, 7.35), (7.55, 1.85, 7.6), rot=rotation((7.45, 1.0, 7.5), "x", -22.5))
    el += pair(sc, "toe", b, (7.35, 0.0, 6.6), (7.55, 0.25, 7.6))
    el += pair(sc, "toe", b, (7.05, 0.0, 6.8), (7.25, 0.25, 7.6))
    el += pair(sc, "toe", b, (7.65, 0.0, 6.8), (7.85, 0.25, 7.6))
    el += pair(sc, "toe", b, (7.35, 0.0, 7.6), (7.55, 0.25, 8.3))
    el.append(panel(sc, "neck", vertebrae(), (7.75, 4.4, 5.1), (8.25, 5.75, 5.6), rot=rotation((8.0, 5.0, 5.35), "x", -22.5)))
    sc.models["whole"] = el
    sc.models["head"] = [panel(sc, "cranium", b, (7.25, 5.5, 4.25), (8.75, 7.0, 5.75), east=bird_side(True), west=bird_side(False)),
                         panel(sc, "beak", b, (7.75, 5.9, 3.0), (8.25, 6.4, 4.25)),
                         panel(sc, "beak", b, (7.85, 5.75, 2.5), (8.15, 6.15, 3.0)),
                         panel(sc, "beak", b, (7.8, 5.6, 3.25), (8.2, 5.9, 4.25))]
    return sc


def zombie_arm(sc, right):
    """One of the Rising Zombie's arms, reaching up from its shoulder: the torn sleeve, the bare forearm, the hand with
    four clawed fingers curling over and a thumb, all leaning out from the shoulder (the left further, so the right
    reaches higher)."""
    def box(lo, hi):
        return (lo, hi) if right else ((16.0 - hi[0], lo[1], lo[2]), (16.0 - lo[0], hi[1], hi[2]))
    skin = flat(GREEN)
    el = [panel(sc, "sleeve", sleeve(), *box((11.5, 9.75, 7.25), (13.0, 14.5, 8.75))),
          panel(sc, "forearm", skin, *box((11.6, 14.5, 7.35), (12.9, 18.5, 8.65))),
          panel(sc, "palm", skin, *box((11.5, 18.5, 7.5), (13.0, 20.0, 8.5))),
          panel(sc, "thumb", finger(), *box((11.2, 19.0, 7.4), (11.5, 20.5, 7.8)))]
    for x0 in (11.5, 11.9, 12.3, 12.7):
        el.append(panel(sc, "finger", finger(), *box((x0, 20.0, 7.6), (x0 + 0.3, 22.0, 8.1))))
        el.append(panel(sc, "fingertip", finger(), *box((x0, 21.75, 7.1), (x0 + 0.3, 22.25, 7.6))))
    sx, sy, sz = fy.RISING_ZOMBIE["shoulder"]
    lean = fy.RISING_ZOMBIE["lean"]
    return tilted(el, (sx if right else 16.0 - sx, sy, sz), -lean[0] if right else lean[1], axis="z")


def rising_zombie():
    """The Rising Zombie, facing north, one block across and two up: a zombie out to its waist from a mound of fresh-dug
    earth heaped in three layers with clods thrown round it, its torso in a torn linen burial shirt (its ribs showing
    through the tear), its head lolling to one side. Models: `whole`, and its runners `left_arm` and `right_arm`
    (reaching up from its shoulders; the renderer claws them at the air)."""
    spec = fy.RISING_ZOMBIE
    sc = Sculpt(spec["block"], 2706, 128)
    dirt = earth()
    el = [panel(sc, "mound", dirt, (1.0, 0.0, 2.0), (15.0, 1.25, 14.0)),
          panel(sc, "mound", dirt, (2.5, 1.25, 3.0), (13.5, 2.25, 13.0)),
          panel(sc, "mound", dirt, (4.0, 2.25, 4.5), (12.0, 3.0, 11.5)),
          panel(sc, "clod", dirt, (1.25, 1.25, 11.75), (2.5, 2.0, 13.0)),
          panel(sc, "clod", dirt, (13.5, 1.25, 4.0), (14.5, 1.75, 5.25)),
          panel(sc, "clod", dirt, (5.0, 0.0, 0.75), (6.5, 0.75, 2.0)),
          panel(sc, "clod", dirt, (10.25, 0.0, 14.0), (11.5, 0.75, 15.0)),
          panel(sc, "torso", shirt(), (4.5, 2.5, 6.5), (11.5, 11.0, 9.5), north=shirt(rip=True)),
          panel(sc, "neck", flat(GREEN), (7.0, 11.0, 7.0), (9.0, 11.75, 9.0)),
          panel(sc, "head", zombie_skin(), (6.5, 11.5, 6.5), (9.5, 15.5, 9.5), north=zombie_face(), up=zombie_skin(top=True),
                rot=rotation((8.0, 11.5, 8.0), "z", 22.5))]
    sc.models["whole"] = el
    sc.models["left_arm"] = zombie_arm(sc, False)
    sc.models["right_arm"] = zombie_arm(sc, True)
    return sc


# ---------------------------------------------------------------- building and files

_built = {}
BUILDERS = {fy.MAILBOX["block"]: haunted_mailbox, fy.PATH_LIGHT["block"]: pumpkin_path_light, fy.SKELETON_DOG["block"]: skeleton_dog,
            fy.SKELETON_CAT["block"]: skeleton_cat, fy.SKELETON_CROW["block"]: skeleton_crow, fy.RISING_ZOMBIE["block"]: rising_zombie}


def build(block):
    if block not in _built:
        _built[block] = ppd.planned(BUILDERS[block], 128)
    return _built[block]


def _image_of(name):
    return build(name).atlas.img if name in BUILDERS else None


# Each multi-block piece: its runners' models (by `runner` value), its overlays (by property), and what its item shows.
MULTI = ((fy.MAILBOX, {"1": "lid", "2": "eyes"}, [("flag_down", {"flag": "false"}), ("flag_up", {"flag": "true"})], ("flag_down", "lid")),
         (fy.PATH_LIGHT, {"1": "lantern", "2": "glow"}, [], ("lantern",)),
         (fy.RISING_ZOMBIE, {"1": "left_arm", "2": "right_arm"}, [], ("left_arm", "right_arm")))
# Each skeleton animal's one moving part, its `runner` 1.
RUNNER = {fy.SKELETON_DOG["block"]: "tail", fy.SKELETON_CAT["block"]: "tail", fy.SKELETON_CROW["block"]: "head"}


def assets(root, write, lang):
    """Each multi-block piece's blocks' models, cut from the whole, its overlays cut the same way (the mailbox's flag down
    and up), its runners whole in its first block's frame, a multipart blockstate adding each by its property (the runner
    alone when `runner`), and its item (the piece whole at half size). Each skeleton animal's model, its moving part's
    model (shown alone when `runner` is 1), and its item (the animal whole)."""
    write = fa.closing_writer(write, _image_of)
    models = root / "models" / "block"
    states = root / "blockstates"
    for spec, runners, overlays, item_shows in MULTI:
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
        shown = sc.models["whole"] + [e for name in item_shows for e in sc.models[name]]
        item = fa.transformed(d17d.pinned(d20d.separated(shown)), 0.5, (4.0, 0.0, 4.0), (0, 0, 0))
        write(root / "models" / "item" / f"{block}.json", fa.model(block, item))
        write(root / "items" / f"{block}.json", {"model": {"type": "minecraft:model", "model": rid(f"item/{block}")}})
    for spec in fy.ANIMALS:
        block = spec["block"]
        lang[f"block.{MOD}.{block}"] = spec["display"]
        sc = build(block)
        runner = RUNNER[block]
        write(models / f"{block}.json", fa.model(block, d20d.separated(sc.models["whole"])))
        write(models / f"{block}_{runner}.json", fa.model(block, d20d.separated(sc.models[runner])))
        variants = {}
        for f, y in FACINGS.items():
            turn = {"y": y} if y else {}
            variants[f"facing={f},runner=0"] = {"model": rid(f"block/{block}"), **turn}
            variants[f"facing={f},runner=1"] = {"model": rid(f"block/{block}_{runner}"), **turn}
        write(states / f"{block}.json", {"variants": variants})
        write(root / "models" / "item" / f"{block}.json", fa.model(block, d20d.separated(sc.models["whole"] + sc.models[runner])))
        write(root / "items" / f"{block}.json", {"model": {"type": "minecraft:model", "model": rid(f"item/{block}")}})


def loot(out, write):
    """Each multi-block piece drops once, from its first block; each skeleton animal drops itself."""
    multi = {spec["block"] for spec, *_ in MULTI}
    for block in fy.blocks():
        survives = {"type": "minecraft:survives_explosion"}
        condition = {"type": "minecraft:all_of", "terms": [survives, {"type": "minecraft:match_block", "blocks": rid(block), "state": {"part": "0"}}]} \
            if block in multi else survives
        write(out / f"{block}.json", {"type": "minecraft:block", "pools": [{
            "condition": condition, "entries": [{"type": "minecraft:item", "name": rid(block)}], "rolls": 1}],
            "random_sequence": rid(f"blocks/{block}")})


def tags(tags):
    """The mailbox comes off with an axe (its post is wood), the path light with a pickaxe (it is iron), the skeleton
    animals with a pickaxe (they are bone, as a bone block is) and the Rising Zombie with a shovel (it is a mound of earth)."""
    tags.add("block", "minecraft:mineable/axe", rid(fy.MAILBOX["block"]))
    tags.add("block", "minecraft:mineable/pickaxe", rid(fy.PATH_LIGHT["block"]))
    for spec in fy.ANIMALS:
        tags.add("block", "minecraft:mineable/pickaxe", rid(spec["block"]))
    tags.add("block", "minecraft:mineable/shovel", rid(fy.RISING_ZOMBIE["block"]))


def textures():
    return {("block", block): build(block).atlas.img for block in fy.blocks()}

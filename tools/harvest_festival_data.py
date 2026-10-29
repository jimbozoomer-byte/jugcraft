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
import decor20_data as d20d
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
CABBAGE = pal("27402a", "3a5a3a", "50764c", "6c9264", "8cae80", "b4cca4")
CABBAGE_HEART = pal("5c7a46", "789658", "94b26c", "b2ca86", "d0e0a6")
MARROW = pal("16280f", "223a18", "2f4c20", "3e602a")
MARROW_STRIPE = pal("6c8a3e", "8ca850", "acc468")
TURNIP_PURPLE = pal("4a1f4e", "6c2f70", "8e4592", "b067b0")
TURNIP_WHITE = pal("b9ad9a", "d8cfbd", "ece6d8", "faf7ef")
TURNIP_BLUSH = pal("c8a4bc", "a8749e")
TURNIP_LEAF = pal("24481c", "2f5d22", "3d7429", "4f8c33", "6aa645")
CARROT = pal("7e320a", "a64814", "cc6422", "e8823a", "f6a45a")
FROND = pal("1e4214", "2c5a1c", "3e7428", "548e38")
CARD = pal("a8a498", "ccc8bc", "e8e4d8", "f8f6ee")
INK = pal("1e2028", "3a3c48")
BOARD = pal("16301c", "214428", "2c5834", "3a6e44", "4c8456")
CREAM = pal("b4a882", "d4c8a0", "ece2c0")
BRASS = pal("5a3c0a", "8c6418", "bc9030", "e0bc5a", "fbe7a4")
# The Harvest Scale's ribbons' colours (tools/halloween_textures.py ribbon_item), darkest first, and their buttons.
RIBBONS = {"first": pal("14265e", "1e3890", "2a4ec0", "4c6ed8", "7a94e4"),
           "second": pal("5e1414", "901e1e", "c02a2a", "d84c4c", "e47a7a"),
           "third": pal("8c8c86", "b0b0aa", "d0d0ca", "e8e8e4", "f8f8f4")}
BUTTON = pal("8c6418", "d8b84a", "f4e8b0")


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


# ---------------------------------------------------------------- part 2: the Giant Vegetable Show

def cabbage_leaf():
    """An outer cabbage leaf, blue-green: its midrib pale down the middle of its length, veins a tone lighter running
    out from it on the slant every third texel, the blade between them by turns its own tone and a tone darker, its
    edge a tone lighter."""
    def paint(p):
        along_x = p.w >= p.h
        span, across = (p.w, p.h) if along_x else (p.h, p.w)
        mid = across // 2
        for y in range(p.h):
            for x in range(p.w):
                s, a = (x, y) if along_x else (y, x)
                if a == mid:
                    k = 5
                elif a in (0, across - 1):
                    k = 3
                elif (s + abs(a - mid)) % 3 == 0:
                    k = 4
                else:
                    k = (2, 1)[(s // 2 + a) % 2]
                p.put(x, y, shade(CABBAGE, k))
    paint.tag = repr(("cabbage_leaf",))
    return paint


def cabbage_heart():
    """The tight head: its leaves wrapped round it in bands three texels high, each band's edge lit along its top, a
    shadow under it, and a vein a tone darker every fourth column, each band's two along from the last's."""
    def paint(p):
        for y in range(p.h):
            band, row = divmod(y, 3)
            for x in range(p.w):
                if row == 0:
                    k = 4
                elif row == 2:
                    k = 1
                else:
                    k = 2 if (x + 2 * band) % 4 == 0 else 3
                p.put(x, y, shade(CABBAGE_HEART, k))
    paint.tag = repr(("cabbage_heart",))
    return paint


def cabbage_crown():
    """The top of the head: its leaves folded over it, in rings a texel wide round its middle, lit and shaded by turns."""
    def paint(p):
        cx, cy = (p.w - 1) / 2, (p.h - 1) / 2
        for y in range(p.h):
            for x in range(p.w):
                ring = int(max(abs(x - cx), abs(y - cy)))
                p.put(x, y, shade(CABBAGE_HEART, (4, 2, 3, 1)[ring % 4]))
    paint.tag = repr(("cabbage_crown",))
    return paint


def marrow_skin():
    """A marrow's skin: dark green with pale stripes running its length, a stripe every third texel round it, each broken
    for a texel every fifth along (each stripe's breaks two along from the last's); its upper row lit."""
    def paint(p):
        along_x = p.w >= p.h
        for y in range(p.h):
            for x in range(p.w):
                s, a = (x, y) if along_x else (y, x)
                if a % 3 == 1 and (s + 2 * (a // 3)) % 5 != 0:
                    p.put(x, y, shade(MARROW_STRIPE, 1 if a else 2))
                else:
                    p.put(x, y, shade(MARROW, 3 if a == 0 else 2 if a % 3 == 0 else 1))
    paint.tag = repr(("marrow_skin",))
    return paint


def marrow_end():
    """A marrow's end, seen end on: dark green, its top row lit, a dark scar in its middle."""
    def paint(p):
        cx, cy = (p.w - 1) / 2, (p.h - 1) / 2
        for y in range(p.h):
            for x in range(p.w):
                if abs(x - cx) < 1 and abs(y - cy) < 1:
                    p.put(x, y, shade(MARROW, 0))
                else:
                    p.put(x, y, shade(MARROW, 2 if y == 0 else 1))
    paint.tag = repr(("marrow_end",))
    return paint


def turnip_skin():
    """A turnip's root: purple shoulders over white, the purple's top row lit, blushing down into the white in streaks
    of a fixed run of lengths (0 to 2 texels, column by column), then white with a faint ring a tone darker every third
    row, its foot in shadow."""
    def paint(p):
        meet = max(1, round(p.h * 0.45))
        for y in range(p.h):
            for x in range(p.w):
                streak = (1, 2, 0, 1, 0, 2, 1)[x % 7]
                if y < meet:
                    c = shade(TURNIP_PURPLE, 3 if y == 0 else 2)
                elif y < meet + streak:
                    c = shade(TURNIP_BLUSH, 1 if y == meet else 0)
                elif y == meet:
                    c = shade(TURNIP_BLUSH, 0)
                elif y == p.h - 1 and p.h > 2:
                    c = shade(TURNIP_WHITE, 0)
                else:
                    c = shade(TURNIP_WHITE, 1 if (y - meet) % 3 == 2 else 2)
                p.put(x, y, c)
    paint.tag = repr(("turnip_skin",))
    return paint


def turnip_leaf():
    """A turnip leaf standing up: its pale stalk at the foot, then its midrib lit up the middle, the blade a tone lighter
    and darker by turns out from it, and its lobes notched dark into its edges every third row."""
    def paint(p):
        mid = p.w // 2
        for y in range(p.h):
            for x in range(p.w):
                if y >= p.h - 2:
                    k = 3 if x == mid else 1
                elif x == mid:
                    k = 4
                elif x in (0, p.w - 1) and y % 3 == 2:
                    k = 0
                else:
                    k = (2, 3)[(x + y) % 2]
                p.put(x, y, shade(TURNIP_LEAF, k))
    paint.tag = repr(("turnip_leaf",))
    return paint


def carrot_skin():
    """A carrot's root: orange, its upper row lit, ringed by short dark dashes two texels long every third texel along
    it, each ring's dash two round from the last's."""
    def paint(p):
        along_x = p.w >= p.h
        for y in range(p.h):
            for x in range(p.w):
                s, a = (x, y) if along_x else (y, x)
                if s % 3 == 0 and (a + s // 3) % 3 != 2:
                    k = 1
                else:
                    k = 4 if a == 0 else 3 if a % 2 == 0 else 2
                p.put(x, y, shade(CARROT, k))
    paint.tag = repr(("carrot_skin",))
    return paint


def carrot_shoulder():
    """The carrot's shoulder, end on: a ring of orange round the green crown its fronds grow from."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                edge = x in (0, p.w - 1) or y in (0, p.h - 1)
                p.put(x, y, shade(CARROT, 2) if edge else shade(FROND, 2 if (x + y) % 2 else 1))
    paint.tag = repr(("carrot_shoulder",))
    return paint


def frond():
    """A carrot frond: feathery, its stalk lit up the middle and its leaflets by turns lit and dark either side of it,
    a fishbone."""
    def paint(p):
        along_x = p.w >= p.h
        across = p.h if along_x else p.w
        mid = across // 2
        for y in range(p.h):
            for x in range(p.w):
                s, a = (x, y) if along_x else (y, x)
                p.put(x, y, shade(FROND, 3 if a == mid else 2 if (s + (a > mid)) % 2 == 0 else 0))
    paint.tag = repr(("frond",))
    return paint


def entry_card():
    """A show's entry card: white card, its top row lit, its entry number inked at the left of its foot (two texels)."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                if y == p.h - 1 and p.h > 1 and x < min(2, p.w - 1):
                    p.put(x, y, shade(INK, 0))
                else:
                    p.put(x, y, shade(CARD, 3 if y == 0 else 2))
    paint.tag = repr(("entry_card",))
    return paint


def crossed_z(sc, name, painter, x, cy, d, z0, z1, rot=None, cut=None, **kw):
    """A round thing `d` across lying along z from z0 to z1 about (x, cy): two boxes crossed, each a fifth of its width
    (or `cut`) shorter at its corners (to the half pixel)."""
    r, c = d / 2, (round(d / 5 * 2) / 2 if cut is None else cut)
    return [panel(sc, name, painter, (x - r + c, cy - r, z0), (x + r - c, cy + r, z1), rot=rot, **kw),
            panel(sc, name, painter, (x - r, cy - r + c, z0), (x + r, cy + r - c, z1), rot=rot, **kw)]


def card(sc, x, z):
    """The entry card on its wire stake, the stake's foot at (x, z), the card's face to the north."""
    return [panel(sc, "stake", ppd.flat(ppd.IRON), (x - 0.125, 0.0, z - 0.125), (x + 0.125, 3.0, z + 0.125)),
            panel(sc, "card", ppd.flat(CARD), (x - 1.5, 2.5, z - 0.375), (x + 1.5, 4.5, z - 0.125), north=entry_card())]


def giant_cabbage():
    """The Giant Cabbage, facing north: its head 50 cm across and 47 cm high, domed, sitting in its outer leaves, which
    spread 94 cm across: four cupped up at 22.5 degrees to the north, east, south and west, and four lying flat between
    them (two leaves crossed at 45 degrees); its entry card at its front left corner."""
    spec = hf.SHOW["cabbage"]
    sc = Sculpt(spec["block"], 3311, 64)
    leaf, heart, crown = cabbage_leaf(), cabbage_heart(), cabbage_crown()
    head, reach = spec["head"], spec["spread"] / 2
    el = crossed_y(sc, "head", heart, 8.0, 8.0, head, 0.5, 6.5, up=crown)
    el += crossed_y(sc, "crown", heart, 8.0, 8.0, head - 2.5, 6.5, 7.5, up=crown)
    # The flat leaves, crossed, under the cupped ones.
    for angle in (45.0, -45.0):
        el.append(panel(sc, "leaf", leaf, (8.0 - reach + 1.5, 0.0, 5.5), (8.0 + reach - 1.5, 0.5, 10.5),
                        rot=fa.rotation((8.0, 0.0, 8.0), "y", angle)))
    # The cupped leaves, each from under the head out to the leaves' reach, turned up about its inner end.
    inner = 8.0 - head / 2 + 2.0
    el.append(panel(sc, "leaf", leaf, (5.0, 0.5, 8.0 - reach), (11.0, 1.0, inner), rot=fa.rotation((8.0, 0.5, inner), "x", 22.5)))
    el.append(panel(sc, "leaf", leaf, (5.0, 0.5, 16.0 - inner), (11.0, 1.0, 8.0 + reach), rot=fa.rotation((8.0, 0.5, 16.0 - inner), "x", -22.5)))
    el.append(panel(sc, "leaf", leaf, (16.0 - inner, 0.5, 5.0), (8.0 + reach, 1.0, 11.0), rot=fa.rotation((16.0 - inner, 0.5, 8.0), "z", 22.5)))
    el.append(panel(sc, "leaf", leaf, (8.0 - reach, 0.5, 5.0), (inner, 1.0, 11.0), rot=fa.rotation((inner, 0.5, 8.0), "z", -22.5)))
    el += card(sc, 2.5, 1.5)
    sc.models["whole"] = el
    return sc


def giant_marrow():
    """The Giant Marrow, facing north: 94 cm long and 38 cm across, lying along its block on a bed of straw, round in
    two crossed boxes, its ends narrower, its stalk at the back; its entry card at its front left corner."""
    spec = hf.SHOW["marrow"]
    sc = Sculpt(spec["block"], 3312, 64)
    skin, end, d = marrow_skin(), marrow_end(), spec["across"]
    z0, z1 = 0.5, 0.5 + spec["length"]
    el = [panel(sc, "straw", ppd.hay(), (4.0, 0.0, 0.25), (12.0, 0.5, 15.75))]
    # Round as an octagon is, its corners cut 1.5 pixels.
    el += crossed_z(sc, "marrow", skin, 8.0, 0.5 + d / 2, d, z0 + 1.5, z1 - 1.5, cut=1.5)
    el += crossed_z(sc, "marrow", skin, 8.0, 0.5 + d / 2, d - 1.5, z0, z0 + 1.5, cut=1.0, north=end)
    el += crossed_z(sc, "marrow", skin, 8.0, 0.5 + d / 2, d - 1.5, z1 - 1.5, z1, cut=1.0, south=end)
    el.append(panel(sc, "stalk", lengthwise(STEM), (7.5, 0.5 + d / 2 - 0.5, z1), (8.5, 0.5 + d / 2 + 0.5, 16.0)))
    el += card(sc, 2.0, 1.5)
    sc.models["whole"] = el
    return sc


def giant_turnip():
    """The Giant Turnip, facing north: its root 44 cm across, a globe a little wider than high on its taproot, purple
    over white, under five leaves 38 cm high, four leaning out at 22.5 degrees and one standing up; its entry card at
    its front left corner."""
    spec = hf.SHOW["turnip"]
    sc = Sculpt(spec["block"], 3313, 64)
    skin, leaf, d = turnip_skin(), turnip_leaf(), spec["across"]
    top = ppd.flat(TURNIP_PURPLE)
    el = [panel(sc, "taproot", lengthwise(TURNIP_WHITE), (7.5, 0.0, 7.5), (8.5, 1.0, 8.5))]
    el += crossed_y(sc, "root", ppd.flat(TURNIP_WHITE), 8.0, 8.0, d - 2.0, 0.5, 1.5)
    el += crossed_y(sc, "root", skin, 8.0, 8.0, d, 1.5, 6.0, up=top)
    el += crossed_y(sc, "crown", ppd.flat(TURNIP_PURPLE), 8.0, 8.0, d - 3.0, 6.0, 6.5)
    for lo, hi, axis, angle, origin in (((6.75, 6.5, 6.5), (9.25, 12.5, 7.0), "x", 22.5, (8.0, 6.5, 7.0)),
                                        ((6.75, 6.5, 9.0), (9.25, 12.5, 9.5), "x", -22.5, (8.0, 6.5, 9.0)),
                                        ((9.0, 6.5, 6.75), (9.5, 12.0, 9.25), "z", -22.5, (9.0, 6.5, 8.0)),
                                        ((6.5, 6.5, 6.75), (7.0, 12.0, 9.25), "z", 22.5, (7.0, 6.5, 8.0))):
        el.append(panel(sc, "leaf", leaf, lo, hi, rot=fa.rotation(origin, axis, angle)))
    el.append(panel(sc, "leaf", leaf, (7.0, 6.5, 7.75), (9.0, 12.5, 8.25)))
    el += card(sc, 2.5, 1.5)
    sc.models["whole"] = el
    return sc


def giant_carrot():
    """The Giant Carrot, facing north: its root 25 cm across at its shoulder and 69 cm long, lying along its block from
    its tip at the front, tapering in five lengths, its fronds rising from its shoulder at the back, three of them
    leaning back at 22.5 degrees, two of those fanned out; its entry card at its front left corner."""
    spec = hf.SHOW["carrot"]
    sc = Sculpt(spec["block"], 3314, 64)
    skin, fr = carrot_skin(), frond()
    shoulder = spec["shoulder"]
    back = 1.0 + spec["length"]
    lengths = ((1.0, 2.0, 1.0), (2.0, 4.5, 2.0), (4.5, 7.0, 2.5), (7.0, 9.5, 3.5), (9.5, back, shoulder))
    el = []
    for z0, z1, d in lengths:
        if d <= 1.0:
            el.append(panel(sc, "root", skin, (8.0 - d / 2, 0.0, z0), (8.0 + d / 2, d, z1)))
        else:
            el += crossed_z(sc, "root", skin, 8.0, d / 2, d, z0, z1, **({"south": carrot_shoulder()} if z1 == back else {}))
    top = shoulder / 2
    el.append(panel(sc, "crown", lengthwise(FROND), (7.25, top - 0.75, back), (8.75, top + 0.75, back + 1.0)))
    for lo, hi, rot in (((7.25, top, back + 0.25), (8.75, top + 6.0, back + 0.75), fa.rotation((8.0, top, back + 0.5), "x", -22.5)),
                        ((6.75, top, back + 0.25), (8.25, top + 5.0, back + 0.75), fa.rotation((7.5, top, back + 0.5), "z", 22.5)),
                        ((7.75, top, back + 0.25), (9.25, top + 5.0, back + 0.75), fa.rotation((8.5, top, back + 0.5), "z", -22.5))):
        el.append(panel(sc, "frond", fr, lo, hi, rot=rot))
    el += card(sc, 3.0, 1.5)
    sc.models["whole"] = el
    return sc


DIGITS = {"1": (".#.", "##.", ".#.", ".#.", "###"), "2": ("###", "..#", "###", "#..", "###"), "3": ("###", "..#", ".##", "..#", "###")}


def painted_boards():
    """Boards painted green, running across: each board three texels high, lit along its top and seamed dark along its
    foot, no grain showing through the paint."""
    def paint(p):
        along_x = p.w >= p.h
        for y in range(p.h):
            for x in range(p.w):
                a = y if along_x else x
                p.put(x, y, shade(BOARD, (3, 2, 1)[a % 3]))
    paint.tag = repr(("painted_boards",))
    return paint


def ribbon_board_face():
    """The board's painted front: its green boards, a dull gold line round its edge, and over each of its three hooks
    its number, 1, 2 and 3 from the left, in cream, three texels wide and five high, each digit's left column lit a
    tone."""
    boards = painted_boards()

    def paint(p):
        boards(p)
        for y in range(p.h):
            for x in range(p.w):
                if x in (0, p.w - 1) or y in (0, p.h - 1):
                    p.put(x, y, shade(BRASS, 2))
        column = p.w // 3
        for k, digit in enumerate("123"):
            x0 = k * column + (column - 3) // 2
            for j, line in enumerate(DIGITS[digit]):
                for i, ch in enumerate(line):
                    if ch == "#" and 1 + j < p.h - 1:
                        p.put(x0 + i, 1 + j, shade(CREAM, 2 if i == 0 else 1))
    paint.tag = repr(("ribbon_board_face",))
    return paint


def pleats(palette):
    """A rosette's pleated ring: its pleats a texel wide, lit and shaded by turns."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                p.put(x, y, shade(palette, 3 if (x + y) % 2 == 0 else 1))
    paint.tag = repr(("pleats", palette[2]))
    return paint


def hook_x(i):
    """Hook `i`'s middle across the board, facing north: hook 1 at the left as you face it, which is the east (+x)."""
    x0, _, x1, _ = hf.RIBBON_BOARD["panel"]
    column = (x1 - x0) / 3
    return x1 - column * (i + 0.5)


def rosette(sc, kind, x, y, z):
    """A prize rosette hung by its top from a hook at (x, y), its back on the board's face at depth z: its pleated ring
    4 pixels across, round in two crossed boxes, a button in its middle, and its two tails hanging in a V below it."""
    r = hf.RIBBON_BOARD["rosette"] / 2
    palette = RIBBONS[kind]
    ring = pleats(palette)
    tail = lengthwise(palette)
    cy = y - r
    el = [panel(sc, f"{kind}_ring", ring, (x - r, cy - r + 1.0, z - 0.5), (x + r, cy + r - 1.0, z)),
          panel(sc, f"{kind}_ring", ring, (x - r + 1.0, cy - r, z - 0.5), (x + r - 1.0, cy + r, z)),
          panel(sc, "button", ppd.flat(BUTTON), (x - 0.75, cy - 0.75, z - 0.75), (x + 0.75, cy + 0.75, z - 0.5))]
    for side, angle in ((-1, -22.5), (1, 22.5)):
        tx = x + side * 0.75
        el.append(panel(sc, f"{kind}_tail", tail, (tx - 0.5, cy - r - 4.0, z - 0.375), (tx + 0.5, cy - r + 0.5, z - 0.125),
                        rot=fa.rotation((tx, cy - r + 0.5, z - 0.25), "z", angle)))
    return el


def ribbon_board():
    """The Prize Ribbon Board, facing north: a board of green-painted planks 94 cm across and 69 cm high on two square
    posts 1.6 m tall, its face 1 m off the ground, three brass cup hooks across it under their numbers; and each prize
    rosette, hung on its hook (its own model, drawn when it hangs there)."""
    spec = hf.RIBBON_BOARD
    sc = Sculpt(spec["block"], 3315, 64)
    x0, y0, x1, y1 = spec["panel"]
    post, z = spec["post"], 7.5
    wood = lengthwise(BOARD)
    el = [panel(sc, "panel", painted_boards(), (x0, y0, z), (x1, y1, z + 1.0), north=ribbon_board_face())]
    for px in (x0 + 1.0, x1 - 1.0 - post):
        el.append(panel(sc, "post", wood, (px, 0.0, z + 1.0), (px + post, spec["height"], z + 1.0 + post)))
        el.append(panel(sc, "cap", ppd.flat(BOARD), (px - 0.25, spec["height"], z + 0.75), (px + post + 0.25, spec["height"] + 0.5, z + 1.25 + post)))
    for i in range(3):
        hx = hook_x(i)
        el.append(panel(sc, "hook", ppd.flat(BRASS), (hx - 0.25, spec["hook_y"] - 0.5, z - 0.75), (hx + 0.25, spec["hook_y"], z)))
    sc.models["whole"] = el
    for i, (_, kind) in enumerate(spec["hooks"]):
        sc.models[kind] = rosette(sc, kind, hook_x(i), spec["hook_y"] - 0.25, z - 0.25)
    return sc


# ---------------------------------------------------------------- building and files

_built = {}
BUILDERS = {hf.CORNUCOPIA["block"]: giant_cornucopia, hf.SHOW["cabbage"]["block"]: giant_cabbage, hf.SHOW["marrow"]["block"]: giant_marrow,
            hf.SHOW["turnip"]["block"]: giant_turnip, hf.SHOW["carrot"]["block"]: giant_carrot, hf.RIBBON_BOARD["block"]: ribbon_board}
# The atlas each is packed into.
ATLAS = {hf.CORNUCOPIA["block"]: 128}


def build(block):
    if block not in _built:
        _built[block] = ppd.planned(BUILDERS[block], ATLAS.get(block, 64))
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
    # Part 2: each show vegetable one model by facing, its item the same at full size.
    for veg in hf.SHOW.values():
        block = veg["block"]
        lang[f"block.{MOD}.{block}"] = veg["display"]
        whole = d20d.separated(build(block).models["whole"])
        write(models / f"{block}.json", fa.model(block, whole))
        write(root / "blockstates" / f"{block}.json", {"variants": {
            f"facing={f}": {"model": rid(f"block/{block}"), **({"y": FACINGS[f]} if FACINGS[f] else {})} for f in FACINGS}})
        write(root / "models" / "item" / f"{block}.json", fa.model(block, whole, DISPLAY))
        write(root / "items" / f"{block}.json", {"model": {"type": "minecraft:model", "model": rid(f"item/{block}")}})
    # The Prize Ribbon Board: the whole board in its lower block's model, its upper's empty, and each ribbon's own model
    # added by a multipart blockstate while it hangs; its item the bare board at 60 percent.
    spec = hf.RIBBON_BOARD
    block = spec["block"]
    lang[f"block.{MOD}.{block}"] = spec["display"]
    lang[f"message.{MOD}.{block}.taken"] = "A ribbon already hangs on that hook"
    sc = build(block)
    write(models / f"{block}.json", fa.model(block, d20d.separated(sc.models["whole"])))
    write(models / f"{block}_upper.json", {"parent": "minecraft:block/block", "textures": {"particle": rid(f"block/{block}")}, "elements": []})
    multipart = []
    for f, y in FACINGS.items():
        turn = {"y": y} if y else {}
        multipart.append({"when": {"facing": f, "half": "lower"}, "apply": {"model": rid(f"block/{block}"), **turn}})
        multipart.append({"when": {"facing": f, "half": "upper"}, "apply": {"model": rid(f"block/{block}_upper"), **turn}})
        for _, kind in spec["hooks"]:
            multipart.append({"when": {"facing": f, "half": "lower", kind: "true"}, "apply": {"model": rid(f"block/{block}_{kind}"), **turn}})
    for _, kind in spec["hooks"]:
        write(models / f"{block}_{kind}.json", fa.model(block, d20d.separated(sc.models[kind])))
    write(root / "blockstates" / f"{block}.json", {"multipart": multipart})
    write(root / "models" / "item" / f"{block}.json", fa.model(block, item_model(sc.models["whole"], (1, 2, 1), 0.6), DISPLAY))
    write(root / "items" / f"{block}.json", {"model": {"type": "minecraft:model", "model": rid(f"item/{block}")}})


def loot(out, write):
    """The cornucopia drops itself, from its first block only (its other blocks break with it, dropping nothing); each
    show vegetable drops itself; and the Prize Ribbon Board drops itself and every ribbon hanging on it, from its lower
    half (its upper breaks with it), its ribbons even in an explosion."""
    block = hf.CORNUCOPIA["block"]
    write(out / f"{block}.json", {"type": "minecraft:block", "pools": [{"condition": {"type": "minecraft:all_of", "terms": [
        {"type": "minecraft:survives_explosion"},
        {"type": "minecraft:match_block", "blocks": rid(block), "state": {"part": "0"}}]},
        "entries": [{"type": "minecraft:item", "name": rid(block)}], "rolls": 1}], "random_sequence": rid(f"blocks/{block}")})
    for veg in hf.SHOW.values():
        block = veg["block"]
        write(out / f"{block}.json", {"type": "minecraft:block", "pools": [{"condition": {"type": "minecraft:survives_explosion"},
            "entries": [{"type": "minecraft:item", "name": rid(block)}], "rolls": 1}], "random_sequence": rid(f"blocks/{block}")})
    spec = hf.RIBBON_BOARD
    block = spec["block"]
    pools = [{"condition": {"type": "minecraft:all_of", "terms": [
        {"type": "minecraft:survives_explosion"},
        {"type": "minecraft:match_block", "blocks": rid(block), "state": {"half": "lower"}}]},
        "entries": [{"type": "minecraft:item", "name": rid(block)}], "rolls": 1}]
    for ribbon, kind in spec["hooks"]:
        pools.append({"condition": {"type": "minecraft:match_block", "blocks": rid(block), "state": {"half": "lower", kind: "true"}},
                      "entries": [{"type": "minecraft:item", "name": rid(ribbon)}], "rolls": 1})
    write(out / f"{block}.json", {"type": "minecraft:block", "pools": pools, "random_sequence": rid(f"blocks/{block}")})


def tags(tags):
    for block in hf.blocks():
        tags.add("block", "minecraft:mineable/axe", rid(block))


def textures():
    return {("block", block): build(block).atlas.img for block in hf.blocks()}

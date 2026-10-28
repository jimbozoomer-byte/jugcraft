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
from pumpkin_patch_data import panel, walls

MOD = "jugcraft"
FACINGS = ppd.FACINGS
GLASS = (214, 232, 236)
CHOCOLATE = pal("2a160c", "3e2214", "56321e", "6e4428")
ICING = (244, 244, 240)
MERINGUE = pal("b0a898", "d0c8b8", "e4ddd0", "f2eee4", "fbf9f3")
COCOA = (74, 44, 24)
PASTRY = pal("8a5a22", "b07a34", "d09c4c", "e6bc6c", "f4d898")
SAUSAGE = pal("5a2418", "7e3424", "a04a34", "bc6448")
MUSTARD = (232, 184, 40)
DOUGH = pal("3e5a1c", "547626", "6c9232", "86ac44")
ALMOND = (206, 160, 104)
CRUMB = pal("6a4422", "8a5c30", "a87840")
PORCELAIN = pal("b8b4ac", "d8d4cc", "ecebe6", "f8f7f4")
JELLY = pal("5a0a12", "8a121e", "b41c2a", "d8343c")
IRIS = (52, 104, 168)
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


def liner():
    """A cupcake's paper case: pleats a texel wide, orange and black by turns."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                p.put(x, y, shade(ORANGE, 3) if x % 2 == 0 else shade(BLACK, 2))
    paint.tag = repr(("liner",))
    return paint


def frosting():
    """Chocolate frosting, lit along its top row."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                p.put(x, y, shade(CHOCOLATE, 3 if y == 0 and p.h > 1 else 2))
    paint.tag = repr(("frosting",))
    return paint


def web_top():
    """The frosting's top under its white web: chocolate, a strand of white across it from one corner (and from both on
    a top three texels across or more)."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                strand = x == y and (p.w >= 3 or x == 0) or (p.w >= 3 and x == p.w - 1 - y)
                p.put(x, y, ICING if strand else shade(CHOCOLATE, 2 if (x + y) % 2 else 3))
    paint.tag = repr(("web_top",))
    return paint


def licorice():
    """Black licorice, for a spider: the black's middle tone, lit along its top row."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                p.put(x, y, shade(BLACK, 3 if y == 0 and p.h > 1 else 1))
    paint.tag = repr(("licorice",))
    return paint


def enamel():
    """Black enamelled steel, lit along its top row."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                p.put(x, y, shade(BLACK, 3 if y == 0 and p.h > 1 else 2))
    paint.tag = repr(("enamel",))
    return paint


def glaze():
    """Black glaze on earthenware: deep black, a glint of light along its top row."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                p.put(x, y, shade(BLACK, 3) if y == 0 and x % 3 == 1 else shade(BLACK, 1))
    paint.tag = repr(("glaze",))
    return paint


def piped():
    """Meringue piped from a star nozzle: ridges a texel wide down it, cream and white by turns, its top row the
    brightest."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                k = 4 if y == 0 and p.h > 1 else 3 if x % 2 == 0 else 2
                p.put(x, y, shade(MERINGUE, k))
    paint.tag = repr(("piped",))
    return paint


def cocoa():
    """A cocoa eye: one dark brown texel."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                p.put(x, y, COCOA)
    paint.tag = repr(("cocoa",))
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


# ---------------------------------------------------------------- part 2: the cake stands

def round_plate(sc, name, painter, w, y0, y1, cx=8.0, cz=8.0):
    """A round plate `w` across, from y0 to y1: two boxes crossed, each a sixth shorter at its ends, so its corners are
    cut off."""
    c = w / 6.0
    return [panel(sc, name, painter, (cx - w / 2 + c, y0, cz - w / 2), (cx + w / 2 - c, y1, cz + w / 2)),
            panel(sc, name, painter, (cx - w / 2, y0, cz - w / 2 + c), (cx + w / 2, y1, cz + w / 2 - c))]


def tower_slots():
    """Where each cupcake sits on the tower, in the order they go on: (x, z, the tier's top): the bottom tier's four
    sides and four corners, the middle tier's four corners, and the top's middle."""
    (w1, y1, _), (w2, y2, _), (w3, y3, _) = pt.CUPCAKE_TOWER["tiers"]
    out = [(8.0 + dx, 8.0 + dz, y1 + 0.25) for dx, dz in ((0, -2.25), (2.25, 0), (0, 2.25), (-2.25, 0),
                                                         (-1.75, -1.75), (1.75, -1.75), (1.75, 1.75), (-1.75, 1.75))]
    out += [(8.0 + dx, 8.0 + dz, y2 + 0.25) for dx, dz in ((-1.1, -1.1), (1.1, -1.1), (1.1, 1.1), (-1.1, 1.1))]
    out.append((8.0, 8.0, y3 + 0.25))
    return out


def cupcake(sc, cx, cz, y, spider):
    """A jumbo spiderweb cupcake 9 cm across: its pleated case, its chocolate frosting under a white web and a swirl on
    top; with `spider`, a licorice spider sitting on it, its legs crossed in an X."""
    choc, web = frosting(), web_top()
    el = [panel(sc, "liner", liner(), (cx - 0.75, y, cz - 0.75), (cx + 0.75, y + 0.75, cz + 0.75)),
          panel(sc, "frosting", choc, (cx - 0.625, y + 0.75, cz - 0.625), (cx + 0.625, y + 1.25, cz + 0.625), up=web),
          panel(sc, "swirl", choc, (cx - 0.375, y + 1.25, cz - 0.375), (cx + 0.375, y + 1.5, cz + 0.375), up=web)]
    if spider:
        black = licorice()
        el.append(panel(sc, "spider", black, (cx - 0.25, y + 1.5, cz - 0.375), (cx + 0.25, y + 1.875, cz + 0.375)))
        for angle in (45, -45):
            el.append(panel(sc, "spider_legs", black, (cx - 0.75, y + 1.5, cz - 0.125), (cx + 0.75, y + 1.625, cz + 0.125),
                            rot=fa.rotation((cx, y + 1.5, cz), "y", angle)))
    return el


def spider_cupcake_tower():
    """The Spider Cupcake Tower, facing north: a round foot, a centre rod, three round tiers of black enamelled steel
    (`tiers`: across, height), their edges lit. Models: `whole` (the empty stand) and `slot_0` up to
    the last (each cupcake, shown while the tower holds more than that many)."""
    spec = pt.CUPCAKE_TOWER
    sc = Sculpt(spec["block"], 2802, 128)
    steel = enamel()
    el = round_plate(sc, "foot", steel, 3.0, 0.0, 0.5)
    top = spec["tiers"][-1][1]
    el.append(panel(sc, "rod", steel, (7.75, 0.5, 7.75), (8.25, top, 8.25)))
    for w, y, _ in spec["tiers"]:
        el += round_plate(sc, "tier", steel, w, y, y + 0.25)
    sc.models["whole"] = el
    for k, (x, z, y) in enumerate(tower_slots()):
        sc.models[f"slot_{k}"] = cupcake(sc, x, z, y, k % 3 == 0)
    return sc


def platter_slots():
    """Where each meringue sits on the platter, in the order they go on: three rows of three, front to back."""
    return [(x, z) for z in (6.75, 8.0, 9.25) for x in (6.5, 8.0, 9.5)]


def ghost(sc, cx, cz, y):
    """A meringue ghost 9 cm high, piped in a cone of four steps: a broad foot, its body, its shoulders and its peak,
    curled over to its left; two cocoa eyes on its front."""
    cream = piped()
    eye = cocoa()
    return [panel(sc, "ghost_foot", cream, (cx - 0.6, y, cz - 0.6), (cx + 0.6, y + 0.4, cz + 0.6)),
            panel(sc, "ghost_body", cream, (cx - 0.45, y + 0.4, cz - 0.45), (cx + 0.45, y + 0.85, cz + 0.45)),
            panel(sc, "ghost_shoulders", cream, (cx - 0.3, y + 0.85, cz - 0.3), (cx + 0.3, y + 1.2, cz + 0.3)),
            panel(sc, "ghost_peak", cream, (cx - 0.05, y + 1.2, cz - 0.15), (cx + 0.3, y + 1.5, cz + 0.15)),
            panel(sc, "ghost_eye", eye, (cx - 0.3, y + 0.55, cz - 0.5), (cx - 0.1, y + 0.75, cz - 0.45)),
            panel(sc, "ghost_eye", eye, (cx + 0.1, y + 0.55, cz - 0.5), (cx + 0.3, y + 0.75, cz - 0.45))]


def ghost_meringue_platter():
    """The Ghost Meringue Platter, facing north: an oval platter of black-glazed earthenware 35 cm long (two crossed
    boxes, their ends cut), a low raised rim, on a low foot. Models: `whole` (the empty platter) and `slot_0` up to the
    last (each meringue, shown while the platter holds more than that many)."""
    spec = pt.MERINGUE_PLATTER
    sc = Sculpt(spec["block"], 2803, 128)
    black = glaze()
    el = [panel(sc, "platter_foot", black, (6.5, 0.0, 6.75), (9.5, 0.5, 9.25)),
          panel(sc, "platter", black, (5.25, 0.5, 6.5), (10.75, 0.75, 9.5)),
          panel(sc, "platter", black, (5.75, 0.5, 6.0), (10.25, 0.75, 10.0))]
    el += walls(sc, "platter_rim", black, 5.75, 6.0, 10.25, 10.0, 0.125, 0.75, 1.0)
    sc.models["whole"] = el
    for k, (x, z) in enumerate(platter_slots()):
        sc.models[f"slot_{k}"] = ghost(sc, x, z, 0.75)
    return sc


# ---------------------------------------------------------------- part 3: the trays

def flat_colour(colour, alpha=255):
    """One colour all over (an eye, a dot of mustard)."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                p.put(x, y, colour, alpha)
    paint.tag = repr(("flat_colour", colour, alpha))
    return paint


def toned(palette, lit=True):
    """A palette's middle tone, its top row lit (and its foot shaded, on a side three texels high or more)."""
    def paint(p):
        top = len(palette) - 1
        for y in range(p.h):
            for x in range(p.w):
                k = top if lit and y == 0 and p.h > 1 else 1 if y == p.h - 1 and p.h > 2 else top - 1
                p.put(x, y, shade(palette, k))
    paint.tag = repr(("toned", palette, lit))
    return paint


def wound():
    """Pastry wound round a sausage: bands a texel wide across it, pastry, sausage and pastry, the pastry lit at its
    left; the sausage shows between."""
    def paint(p):
        long_x = p.w >= p.h
        for y in range(p.h):
            for x in range(p.w):
                a = x if long_x else y
                p.put(x, y, shade(SAUSAGE, 2) if a % 3 == 1 else shade(PASTRY, 3 if a % 3 == 0 else 2))
    paint.tag = repr(("wound",))
    return paint


def finger():
    """A witch's finger of green shortbread, from its tip down: an almond nail, a knuckle crease a tone deeper, then the
    finger, lit along its left."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                if y == 0:
                    p.put(x, y, ALMOND)
                else:
                    p.put(x, y, shade(DOUGH, 1 if y == 1 else 3 if x == 0 else 2))
    paint.tag = repr(("finger",))
    return paint


def crumbs():
    """Cookie crumbs: two tones by turns, a lit crumb every third texel."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                p.put(x, y, shade(CRUMB, 2 if (x + 2 * y) % 3 == 0 else 1 if (x + y) % 2 else 0))
    paint.tag = repr(("crumbs",))
    return paint


def jelly():
    """Red jelly, clear: its middle tone, see-through, its top row lit."""
    def paint(p):
        for y in range(p.h):
            for x in range(p.w):
                p.put(x, y, shade(JELLY, 3 if y == 0 and p.h > 1 else 2), 190)
    paint.tag = repr(("jelly",))
    return paint


def sausage_slots():
    """Where each sausage lies on the tray, front to back along it, in the order they go on: two rows of four, either
    side of the ramekin of mustard."""
    return [(x, z) for z in (7.0, 9.0) for x in (5.5, 6.5, 9.5, 10.5)]


def mummy_sausage(sc, cx, cz, y):
    """A mummy sausage 12 cm long and 5 cm thick, lying front to back: wound in pastry, two dots of mustard for eyes near
    its front end."""
    eye = flat_colour(MUSTARD)
    return [panel(sc, "mummy", wound(), (cx - 0.375, y, cz - 1.0), (cx + 0.375, y + 0.75, cz + 1.0), north=toned(SAUSAGE), south=toned(SAUSAGE)),
            panel(sc, "mummy_eye", eye, (cx - 0.3, y + 0.75, cz - 0.8), (cx - 0.1, y + 0.8, cz - 0.6), sides=("up", "north", "east", "west")),
            panel(sc, "mummy_eye", eye, (cx + 0.1, y + 0.75, cz - 0.8), (cx + 0.3, y + 0.8, cz - 0.6), sides=("up", "north", "east", "west"))]


def mummy_sausage_tray():
    """The Mummy Sausage Tray, facing north: a black baking tray 40 cm by 28 cm, its low sides round it, a white ramekin
    of mustard in its middle. Models: `whole` (the tray and ramekin) and `slot_0` up to the last (each sausage)."""
    spec = pt.MUMMY_TRAY
    sc = Sculpt(spec["block"], 2804, 128)
    steel, white = enamel(), toned(PORCELAIN)
    el = [panel(sc, "tray", steel, (4.75, 0.0, 5.75), (11.25, 0.25, 10.25))]
    el += walls(sc, "tray_side", steel, 4.75, 5.75, 11.25, 10.25, 0.25, 0.25, 0.75)
    el += walls(sc, "ramekin", white, 7.25, 7.25, 8.75, 8.75, 0.25, 0.25, 1.0)
    el.append(panel(sc, "mustard", flat_colour(MUSTARD), (7.5, 0.25, 7.5), (8.5, 0.75, 8.5), sides=("up",)))
    sc.models["whole"] = el
    for k, (x, z) in enumerate(sausage_slots()):
        sc.models[f"slot_{k}"] = mummy_sausage(sc, x, z, 0.25)
    return sc


# Each finger: where it stands in the crumbs (x, z), and how it leans out (the axis, degrees).
FINGERS = ((7.0, 7.0, "x", -22.5), (8.0, 6.75, "x", -22.5), (9.0, 7.0, "z", -22.5), (6.75, 8.0, "z", 22.5), (9.25, 8.0, "z", -22.5),
           (7.0, 9.0, "z", 22.5), (8.0, 9.25, "x", 22.5), (9.0, 9.0, "x", 22.5), (7.75, 8.0, "x", 0.0), (8.5, 8.25, "z", 0.0))


def witch_finger_cauldron():
    """The Witch-Finger Cauldron, facing north: a cast-iron pot 25 cm across, round (two boxes crossed), its lip round its
    top, on three stubby legs, filled with cookie crumbs. Models: `whole` (the pot and crumbs) and `slot_0` up to the
    last (each finger, standing in the crumbs and leaning out, 14 cm long and 3 cm thick)."""
    spec = pt.FINGER_CAULDRON
    sc = Sculpt(spec["block"], 2805, 128)
    iron = cast()
    el = []
    for x, z in ((6.75, 6.75), (9.25, 6.75), (8.0, 9.5)):
        el.append(panel(sc, "leg", iron, (x - 0.25, 0.0, z - 0.25), (x + 0.25, 0.5, z + 0.25)))
    el += round_plate(sc, "pot_foot", iron, 3.0, 0.5, 1.0)
    el += round_plate(sc, "pot", iron, 4.0, 1.0, 2.5)
    el += walls(sc, "pot_lip", iron, 6.25, 6.25, 9.75, 9.75, 0.25, 2.5, 3.0)
    el.append(panel(sc, "crumbs", crumbs(), (6.5, 2.5, 6.5), (9.5, 2.625, 9.5), sides=("up",)))
    sc.models["whole"] = el
    nail = finger()
    for k, (x, z, axis, angle) in enumerate(FINGERS):
        rot = fa.rotation((x, 2.5, z), axis, angle) if angle else None
        sc.models[f"slot_{k}"] = [panel(sc, "finger", nail, (x - 0.25, 1.75, z - 0.25), (x + 0.25, 4.0, z + 0.25), rot=rot,
                                        up=flat_colour(ALMOND))]
    return sc


def jelly_slots():
    """The jelly's segments round its hole, in the order they set: from the front, round to the right."""
    step = 1.125
    return [(8.0 + dx * step, 8.0 + dz * step) for dx, dz in ((0, -1), (1, -1), (1, 0), (1, 1), (0, 1), (-1, 1), (-1, 0), (-1, -1))]


def eyeball_jelly_mould():
    """The Eyeball Jelly Mould, facing north: a round white plate 28 cm across, on which the ring of jelly stands, 22 cm
    across and 9 cm high. Models: `whole` (the plate) and `slot_0` up to the last (each segment of clear red jelly, an
    eyeball set in it looking out, its blue iris toward the ring's outside; the corner segments drawn in from the outside,
    so the ring reads round)."""
    spec = pt.JELLY_MOULD
    sc = Sculpt(spec["block"], 2806, 128)
    sc.models["whole"] = round_plate(sc, "plate", toned(PORCELAIN), 4.5, 0.0, 0.25)
    half = 1.125 / 2
    white, iris = toned(PORCELAIN, lit=False), flat_colour(IRIS)
    for k, (x, z) in enumerate(jelly_slots()):
        dx, dz = x - 8.0, z - 8.0
        # A corner segment is drawn in from the ring's outside, so the ring reads round, not square.
        x0, x1 = x - half + (0.25 if dx < 0 and dz else 0.0), x + half - (0.25 if dx > 0 and dz else 0.0)
        z0, z1 = z - half + (0.25 if dz < 0 and dx else 0.0), z + half - (0.25 if dz > 0 and dx else 0.0)
        el = [panel(sc, "jelly", jelly(), (x0, 0.25, z0), (x1, 1.75, z1)),
              panel(sc, "eyeball", white, (x - 0.3, 0.7, z - 0.3), (x + 0.3, 1.3, z + 0.3))]
        if abs(dz) >= abs(dx):
            zo = z - 0.35 if dz < 0 else z + 0.3
            el.append(panel(sc, "iris", iris, (x - 0.15, 0.85, zo), (x + 0.15, 1.15, zo + 0.05)))
        else:
            xo = x - 0.35 if dx < 0 else x + 0.3
            el.append(panel(sc, "iris", iris, (xo, 0.85, z - 0.15), (xo + 0.05, 1.15, z + 0.15)))
        sc.models[f"slot_{k}"] = el
    return sc


def lowered(elements):
    """`elements` set down onto a Feast Table: `table_drop` pixels lower."""
    return fa.transformed(elements, 1.0, (0.0, -pt.TABLE["table_drop"], 0.0))


# ---------------------------------------------------------------- building and files

_built = {}
BUILDERS = {pt.DISPENSER["block"]: candy_corn_dispenser, pt.CUPCAKE_TOWER["block"]: spider_cupcake_tower,
            pt.MERINGUE_PLATTER["block"]: ghost_meringue_platter, pt.MUMMY_TRAY["block"]: mummy_sausage_tray,
            pt.FINGER_CAULDRON["block"]: witch_finger_cauldron, pt.JELLY_MOULD["block"]: eyeball_jelly_mould}


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
    for spec in pt.serving():
        serving_assets(root, write, lang, spec)


def serving_assets(root, write, lang, spec):
    """A table piece that holds a food: the empty stand and each serving's model, each again set down onto a Feast
    Table (`_table`); a multipart blockstate adding each serving while it holds more than that many (`servings`), on the
    table or off it (`on_table`); and its item (the stand full)."""
    models = root / "models" / "block"
    block = spec["block"]
    lang[f"block.{MOD}.{block}"] = spec["display"]
    sc = build(block)
    names = ["whole"] + [f"slot_{k}" for k in range(spec["capacity"])]
    for name in names:
        model = block if name == "whole" else f"{block}_{name}"
        write(models / f"{model}.json", fa.model(block, d20d.separated(sc.models[name])))
        write(models / f"{model}_table.json", fa.model(block, d20d.separated(lowered(sc.models[name]))))
    multipart = []
    for f, y in FACINGS.items():
        turn = {"y": y} if y else {}
        for on, suffix in (("false", ""), ("true", "_table")):
            multipart.append({"when": {"facing": f, "on_table": on}, "apply": {"model": rid(f"block/{block}{suffix}"), **turn}})
            for k in range(spec["capacity"]):
                more = "|".join(str(n) for n in range(k + 1, spec["capacity"] + 1))
                multipart.append({"when": {"facing": f, "on_table": on, "servings": more},
                                  "apply": {"model": rid(f"block/{block}_slot_{k}{suffix}"), **turn}})
    write(root / "blockstates" / f"{block}.json", {"multipart": multipart})
    shown = [e for name in names for e in sc.models[name]]
    write(root / "models" / "item" / f"{block}.json", fa.model(block, d20d.separated(shown)))
    write(root / "items" / f"{block}.json", {"model": {"type": "minecraft:model", "model": rid(f"item/{block}")}})


def loot(out, write):
    """Each piece drops itself. The dispenser's candy drops from its block entity; a table piece's servings drop with
    it, as many as its `servings` says (a pool for each count)."""
    for block in pt.blocks():
        pools = [{"condition": {"type": "minecraft:survives_explosion"}, "entries": [{"type": "minecraft:item", "name": rid(block)}],
                  "rolls": 1}]
        for spec in pt.serving():
            if spec["block"] == block:
                for n in range(1, spec["capacity"] + 1):
                    pools.append({"condition": {"type": "minecraft:all_of", "terms": [
                        {"type": "minecraft:survives_explosion"},
                        {"type": "minecraft:match_block", "blocks": rid(block), "state": {"servings": str(n)}}]},
                        "entries": [{"type": "minecraft:item", "name": rid(spec["serves"]),
                                     "modifier": [{"type": "minecraft:set_count", "count": n}]}], "rolls": 1})
        write(out / f"{block}.json", {"type": "minecraft:block", "pools": pools, "random_sequence": rid(f"blocks/{block}")})


def tags(tags):
    for block in pt.blocks():
        tags.add("block", "minecraft:mineable/pickaxe", rid(block))


def textures():
    """Each piece's block texture, and each food's icon, drawn from its map (tools/item_icons/<food>.txt)."""
    import item_icons
    out = {("block", block): build(block).atlas.img for block in pt.blocks()}
    for item in pt.ITEMS:
        out[("item", item)] = item_icons.draw(item)
    return out

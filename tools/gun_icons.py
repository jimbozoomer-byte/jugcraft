"""The big guns' and war machines' item icons (5 October 2026: the owner asked for every weapon's icon to "look way
cooler, maybe not even accurate but cooler"): 32x32 pixel art in the arms icons' rules (docs/ART_DIRECTION.md, "Weapons
are pixel art"). Each icon is a few flat shapes, each lit along its top and left edge and shaded along its bottom and
right one, with a dark one-pixel outline round the whole silhouette; no dithering, noise or glints. Every shape stays
inside a one-pixel margin, so the outline closes all round (Icon.image refuses a shape on the edge).

Guns sit in a three-quarter view with the barrel raised to the upper right. Each has one cue of its own, so they tell
apart in a hotbar: the Grand Mortar stands on a tall concrete tower, the Bastion Mortar and Autocannon on a compact 3x3
block, the Fortress Rifle has its long, low barrel and the range finder across its roof, the Triple Battery its round
drum and three barrels, and the Siege Mortar its round railed deck with a hazard rim. The Landship, the Diesel Walker
and the Zeppelin follow the same rules.

draw(kind) returns the icon for any of KINDS; tools/tower_guns.py, tools/artillery.py, tools/landship.py, tools/mech.py
and tools/zeppelin.py save them.
"""
import math

from PIL import Image, ImageDraw

SIZE = 32
OUTLINE = (24, 22, 26)

# Each material: shaded edge, fill, lit edge (dark to light). "_top" and "_side" variants light a box's top and darken
# its side, so a box reads in three-quarter view.
MATERIALS = {
    "yellow": [(168, 120, 22), (220, 172, 40), (246, 208, 88)],
    "yellow_top": [(206, 160, 36), (244, 204, 82), (255, 232, 140)],
    "yellow_side": [(132, 92, 16), (170, 126, 26), (196, 150, 34)],
    "steel": [(46, 48, 54), (74, 77, 84), (112, 116, 124)],
    "steel_top": [(84, 87, 94), (110, 114, 122), (146, 150, 158)],
    "steel_side": [(34, 36, 40), (54, 56, 62), (70, 73, 80)],
    "wicker_top": [(140, 106, 62), (178, 140, 86), (204, 168, 110)],
    "wicker_side": [(86, 62, 34), (116, 88, 50), (136, 104, 60)],
    "dark": [(30, 31, 35), (44, 46, 51), (64, 66, 72)],
    "concrete": [(108, 106, 100), (138, 136, 128), (164, 162, 154)],
    "concrete_side": [(82, 80, 76), (104, 102, 96), (120, 118, 112)],
    "deck": [(52, 52, 56), (70, 70, 74), (96, 96, 100)],
    "olive": [(62, 70, 40), (92, 102, 60), (120, 132, 80)],
    "olive_top": [(92, 102, 60), (120, 132, 80), (148, 160, 104)],
    "olive_side": [(48, 54, 30), (68, 76, 44), (84, 94, 54)],
    "khaki": [(96, 86, 58), (128, 116, 80), (156, 142, 102)],
    "khaki_top": [(128, 116, 80), (156, 142, 102), (182, 168, 126)],
    "khaki_side": [(76, 68, 46), (98, 88, 60), (114, 104, 72)],
    "brass": [(140, 102, 40), (200, 160, 72), (236, 202, 112)],
    "copper": [(120, 60, 34), (170, 92, 54), (210, 128, 84)],
    "red": [(120, 30, 24), (168, 44, 34), (204, 72, 56)],
    "canvas": [(156, 144, 108), (198, 186, 148), (226, 218, 186)],
    "cream": [(204, 196, 168), (230, 222, 196), (244, 238, 218)],
    "wicker": [(112, 82, 46), (152, 116, 68), (182, 144, 88)],
    "hazard": [(36, 36, 40), (36, 36, 40), (36, 36, 40)],
    "lens": [(60, 110, 150), (110, 170, 214), (190, 230, 250)],
    "bore": [(14, 14, 16), (14, 14, 16), (14, 14, 16)],
    "amber": [(196, 120, 20), (240, 170, 40), (255, 220, 120)],
    "lacquer": [(20, 20, 26), (42, 42, 52), (80, 80, 96)],
    "tread": [(36, 34, 32), (56, 53, 50), (84, 80, 76)],
    "gold": [(150, 108, 34), (212, 168, 64), (246, 214, 120)],
    "teal": [(34, 88, 80), (56, 128, 116), (94, 172, 156)],
    "rust": [(70, 64, 60), (100, 95, 91), (130, 124, 118)],
    "silver": [(112, 114, 120), (164, 166, 172), (212, 214, 220)],
}


def palette(material):
    """A material's three tones; a missing "_top" or "_side" variant is its base one step lighter or darker."""
    if material in MATERIALS:
        return MATERIALS[material]
    base, variant = material.rsplit("_", 1)
    shade, fill, lit = MATERIALS[base]
    if variant == "top":
        return [fill, lit, tuple(min(255, c + 30) for c in lit)]
    return [tuple(int(c * 0.75) for c in shade), shade, fill]


class Icon:
    """Shapes painted in order onto a 32x32 grid; each shape keeps its own region, so it is lit as one piece."""

    def __init__(self):
        self.region = [[0] * SIZE for _ in range(SIZE)]
        self.material = {0: None}
        self.flat = set()
        self.next = 1

    def _shape(self, material, flat=False):
        rid = self.next
        self.next += 1
        self.material[rid] = material
        if flat:
            self.flat.add(rid)
        mask = Image.new("L", (SIZE, SIZE), 0)
        return rid, mask, ImageDraw.Draw(mask)

    def _commit(self, rid, mask):
        px = mask.load()
        for y in range(SIZE):
            for x in range(SIZE):
                if px[x, y]:
                    self.region[y][x] = rid

    def poly(self, points, material, flat=False):
        rid, mask, d = self._shape(material, flat)
        d.polygon([(round(x), round(y)) for x, y in points], fill=255)
        self._commit(rid, mask)

    def rect(self, x0, y0, x1, y1, material, flat=False):
        rid, mask, d = self._shape(material, flat)
        d.rectangle((x0, y0, x1, y1), fill=255)
        self._commit(rid, mask)

    def ellipse(self, x0, y0, x1, y1, material, flat=False):
        rid, mask, d = self._shape(material, flat)
        d.ellipse((x0, y0, x1, y1), fill=255)
        self._commit(rid, mask)

    def bar(self, p0, p1, r, material, flat=False, square=True):
        """A straight bar of half-width r from p0 to p1 (square ends), drawn as one region."""
        (x0, y0), (x1, y1) = p0, p1
        dx, dy = x1 - x0, y1 - y0
        n = math.hypot(dx, dy) or 1.0
        nx, ny = -dy / n * r, dx / n * r
        rid, mask, d = self._shape(material, flat)
        d.polygon([(x0 + nx, y0 + ny), (x1 + nx, y1 + ny), (x1 - nx, y1 - ny), (x0 - nx, y0 - ny)], fill=255)
        if not square:
            d.ellipse((x0 - r, y0 - r, x0 + r, y0 + r), fill=255)
            d.ellipse((x1 - r, y1 - r, x1 + r, y1 + r), fill=255)
        self._commit(rid, mask)

    def box3(self, x, y, w, h, d, material):
        """A box in three-quarter view: its front face from (x, y), w wide and h tall, its top and its right side
        receding d pixels up and to the right; the top is lit, the side shaded."""
        self.poly([(x, y), (x + d, y - d), (x + w + d, y - d), (x + w, y)], material + "_top")
        self.poly([(x + w, y), (x + w + d, y - d), (x + w + d, y + h - d), (x + w, y + h)], material + "_side")
        self.rect(x, y, x + w - 1, y + h - 1, material)

    def oval(self, centre, along, major, minor, material, flat=False):
        """An ellipse whose minor axis lies along the unit vector `along` (a muzzle face, a drum seen at an angle)."""
        (cx, cy), (ux, uy) = centre, along
        vx, vy = -uy, ux
        points = [(cx + ux * minor * math.cos(t) + vx * major * math.sin(t), cy + uy * minor * math.cos(t) + vy * major * math.sin(t))
                  for t in (2 * math.pi * k / 24 for k in range(24))]
        self.poly(points, material, flat)

    def stripe(self, x0, x1, material, over):
        """Repaints columns x0..x1 of every region of material `over` as one new region (a painted band)."""
        rid = self.next
        self.next += 1
        self.material[rid] = material
        for y in range(SIZE):
            for x in range(x0, x1 + 1):
                if self.material[self.region[y][x]] == over:
                    self.region[y][x] = rid

    def image(self):
        reg = self.region
        edge = [(x, y) for y in range(SIZE) for x in range(SIZE) if reg[y][x] and (x in (0, SIZE - 1) or y in (0, SIZE - 1))]
        if edge:
            raise ValueError(f"an icon shape touches the edge at {edge[:4]}: keep it inside the one-pixel margin, so the "
                             f"outline closes")
        img = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))
        px = img.load()

        def at(x, y):
            return reg[y][x] if 0 <= x < SIZE and 0 <= y < SIZE else 0

        for y in range(SIZE):
            for x in range(SIZE):
                r = reg[y][x]
                if not r:
                    if any(at(x + dx, y + dy) for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1))):
                        px[x, y] = OUTLINE + (255,)
                    continue
                shade, fill, lit = palette(self.material[r])
                c = fill
                if r not in self.flat:
                    if at(x, y - 1) != r or at(x - 1, y) != r:
                        c = lit
                    elif at(x, y + 1) != r or at(x + 1, y) != r:
                        c = shade
                px[x, y] = c + (255,)
        return img


def _along(p0, p1, t):
    return (p0[0] + (p1[0] - p0[0]) * t, p0[1] + (p1[1] - p0[1]) * t)


def barrel(icon, p0, p1, r, ring=1.4, bands=(), brake=False, material="steel"):
    """A barrel from breech p0 to muzzle p1: the tube, raised bands, a heavier muzzle ring (or a longer, boxier brake)
    and, on its face, a dark round bore."""
    length = math.dist(p0, p1)
    u = ((p1[0] - p0[0]) / length, (p1[1] - p0[1]) / length)
    icon.bar(p0, p1, r, material)
    for t in bands:
        icon.bar(_along(p0, p1, t - 0.8 / length), _along(p0, p1, t + 0.8 / length), r + 0.7, material)
    end = (3.0 + r + (2.0 if brake else 0.0)) / length
    icon.bar(_along(p0, p1, 1 - end), p1, r + ring, "dark" if brake else material)
    face = r + ring
    if face >= 2.0:
        icon.oval(p1, u, face, face * 0.55, material + "_top")
        icon.oval(p1, u, face * 0.6, face * 0.33, "bore", flat=True)
    else:
        icon.oval(p1, u, face, face * 0.6, "bore", flat=True)


def plinth(icon, x0, x1, top, depth=3, height=5, hazard=True):
    """The concrete plinth in three-quarter view with a tread deck on top and a hazard-striped edge along its front."""
    w = x1 - x0 - depth
    icon.poly([(x0, top), (x0 + depth, top - depth), (x1, top - depth), (x1 - depth, top)], "deck")
    icon.poly([(x1 - depth, top), (x1, top - depth), (x1, top - depth + height), (x1 - depth, top + height)], "concrete_side")
    icon.rect(x0, top, x0 + w - 1, top + height - 1, "concrete")
    if hazard:
        for x in range(x0, x0 + w, 4):
            icon.poly([(x, top), (x + 2, top), (x + 1, top + 1), (x - 1, top + 1)], "hazard", flat=True)
    return top - depth


def port(icon, cx, cy, r):
    """A round port cover: a dark ring with a steel boss."""
    icon.ellipse(cx - r, cy - r, cx + r, cy + r, "dark")
    icon.ellipse(cx - r + 1.5, cy - r + 1.5, cx + r - 1.5, cy + r - 1.5, "steel")


def side_centre(x, y, w, h, d):
    """The middle of a box3's right side face: where a gun pointing right comes out of its housing."""
    return (x + w + d / 2, y + h / 2 - d / 2)


def hazard_ticks(icon, x0, x1, y, step=4):
    """Black hazard chevrons along a yellow lip at rows y and y + 1."""
    for x in range(x0, x1 + 1, step):
        icon.poly([(x, y), (x + 1, y), (x, y + 1), (x - 1, y + 1)], "hazard", flat=True)


def slots(icon, x0, x1, y0, y1, step=3):
    """A row of dark vertical vent slots (a plinth's slotted ring)."""
    for x in range(x0, x1 + 1, step):
        icon.rect(x, y0, x, y1, "dark", flat=True)


def block(icon, x, y, w, h, d, lip=True):
    """A concrete plinth or tower in three-quarter view: a tread deck on top and, along its front, a yellow lip with
    hazard chevrons."""
    icon.box3(x, y, w, h, d, "concrete")
    icon.poly([(x + 1, y - 1), (x + d, y - d + 1), (x + w + d - 2, y - d + 1), (x + w - 1, y - 1)], "deck", flat=True)
    if lip:
        icon.rect(x, y, x + w - 1, y + 1, "yellow", flat=True)
        hazard_ticks(icon, x + 1, x + w - 2, y)


def recuperators(icon, p0, p1, r, upto=0.55, material="steel"):
    """Twin cylinders riding on top of a barrel from p0 towards p1, up to `upto` of its length."""
    length = math.dist(p0, p1)
    u = ((p1[0] - p0[0]) / length, (p1[1] - p0[1]) / length)
    v = (u[1], -u[0])
    for off in (r + 0.9,):
        a = (p0[0] + v[0] * off, p0[1] + v[1] * off)
        b = _along(a, (a[0] + u[0] * length, a[1] + u[1] * length), upto)
        icon.bar(a, b, 1.3, material)


def grand_mortar():
    """The biggest gun, on its own concrete tower: a fat barrel raised steeply out of yellow cheeks, with its
    recuperators riding on top."""
    i = Icon()
    block(i, 2, 19, 17, 11, 5)
    slots(i, 4, 17, 23, 26)
    i.box3(4, 12, 12, 6, 3, "yellow")
    port(i, 8, 15, 2)
    p0, p1 = (13, 16), (23.5, 7)
    recuperators(i, p0, p1, 4.4)
    barrel(i, p0, p1, 4.4, ring=1.4, bands=(0.5,))
    return i.image()


def bastion_mortar():
    """A 3x3 mortar: a compact concrete block, a short yellow breech and a stubby, fat barrel."""
    i = Icon()
    block(i, 6, 23, 14, 6, 4)
    i.box3(8, 17, 10, 5, 3, "yellow")
    port(i, 11, 19, 1.5)
    barrel(i, (15, 18), (22, 10), 3.4, ring=1.2)
    return i.image()


def fortress_rifle():
    """The long gun: a low gunhouse with the range finder across its roof and a long barrel held low, ending in a
    muzzle brake."""
    i = Icon()
    block(i, 1, 23, 20, 6, 5)
    box = (2, 16, 14, 6, 4)
    barrel(i, side_centre(*box), (28.5, 8.5), 1.3, ring=0.8, brake=True)
    i.box3(*box, "yellow")
    port(i, 6, 19, 1.8)
    # The range finder: a dark tube across the roof with a lens at either end, standing out past both sides.
    i.rect(1, 13, 21, 14, "dark")
    i.rect(1, 13, 1, 14, "lens", flat=True)
    i.rect(21, 13, 21, 14, "lens", flat=True)
    i.rect(9, 11, 12, 12, "dark")
    return i.image()


def bastion_autocannon():
    """A 3x3 quick-firing gun: a compact block, a small gunhouse with an ammunition drum and twin long barrels with
    slotted flash hiders."""
    i = Icon()
    block(i, 6, 23, 14, 6, 4)
    box = (8, 17, 10, 5, 3)
    sx, sy = side_centre(*box)
    for off in (-2.5, 0.5):
        barrel(i, (sx - 3 + off, sy + 1 + off), (26.5 + off, 4.5 + off), 0.8, ring=0.6, bands=(0.82,))
    i.box3(*box, "yellow")
    i.ellipse(4, 17, 10, 24, "steel")
    i.ellipse(6, 19, 8, 22, "brass", flat=True)
    i.rect(12, 13, 13, 15, "dark")
    return i.image()


def triple_battery():
    """Three barrels out of a round yellow turret drum on a 5x5 plinth."""
    i = Icon()
    block(i, 1, 24, 22, 5, 5)
    for off in (-4, 0, 4):
        barrel(i, (15 + off, 17 + off), (25 + off, 7 + off), 1.0, ring=0.6)
    i.poly([(3, 15), (20, 15), (20, 21), (3, 21)], "yellow")
    i.ellipse(3, 17, 20, 24, "yellow")
    i.ellipse(3, 10, 20, 19, "yellow_top")
    i.ellipse(7, 11, 13, 15, "steel_top")
    port(i, 6, 19, 1.5)
    return i.image()


def siege_mortar():
    """The field mortar on its round turntable deck: a hazard rim, railing posts, yellow trunnion cheeks either side
    of a fat barrel and the copper recuperator under it."""
    i = Icon()
    i.ellipse(2, 21, 29, 30, "steel_side")
    i.ellipse(2, 19, 29, 28, "yellow", flat=True)
    i.ellipse(4, 20, 27, 27, "deck")
    for x, y in ((3, 23), (7, 20), (13, 19), (19, 19), (25, 20), (28, 23), (25, 26), (19, 27), (12, 27), (6, 26)):
        i.rect(x, y, x, y, "hazard", flat=True)
    # Railing posts round the back of the deck, with a rail along their tops.
    for x in (4, 27):
        i.rect(x, 15, x + 1, 22, "steel")
    i.rect(4, 15, 27, 16, "steel")
    i.box3(7, 17, 4, 6, 2, "yellow")
    i.bar((16, 24), (21, 18), 1.0, "copper")
    barrel(i, (14, 20), (22.5, 9), 3.9, ring=1.3, bands=(0.5,))
    i.box3(17, 18, 4, 6, 2, "yellow")
    return i.image()


def self_propelled_howitzer():
    """The tracked gun carriage with its armoured cab and a long barrel ending in a muzzle brake."""
    i = Icon()
    i.poly([(1, 24), (4, 21), (27, 21), (30, 24), (27, 29), (4, 29)], "dark")
    for x in (6, 11, 16, 21, 25):
        i.ellipse(x - 2, 24, x + 2, 28, "steel")
    i.box3(2, 18, 22, 5, 3, "khaki")
    box = (5, 11, 9, 7, 3)
    barrel(i, side_centre(*box), (28, 6.5), 1.2, ring=0.9, brake=True)
    i.box3(*box, "khaki")
    i.rect(6, 13, 10, 13, "dark", flat=True)
    i.rect(7, 7, 10, 8, "olive")
    return i.image()


def flak_gun():
    """The anti-aircraft gun: a cross mount, an olive head with its ammunition drum and twin barrels pointing high."""
    i = Icon()
    i.bar((2, 25), (28, 29), 1.0, "olive")
    i.bar((4, 29), (27, 24), 1.0, "olive")
    i.box3(12, 22, 6, 5, 2, "olive")
    box = (7, 15, 11, 7, 3)
    sx, sy = side_centre(*box)
    for off in (-2, 1):
        barrel(i, (sx - 3 + off, sy + 1 + off), (25.5 + off, 4 + off), 0.7, ring=0.7, bands=(0.85,))
    i.box3(*box, "olive")
    i.ellipse(8, 16, 13, 21, "brass")
    i.rect(13, 10, 14, 13, "dark")
    i.rect(11, 9, 16, 10, "steel")
    return i.image()


def observation_balloon():
    """The kite balloon: a long canvas envelope with its tail lobes and red bands, the basket on its rigging below."""
    i = Icon()
    for x0, x1 in ((14, 15), (21, 20)):
        i.bar((x0, 14), (x1, 23), 0.4, "dark", flat=True)
    i.box3(13, 24, 8, 4, 2, "wicker")
    i.ellipse(1, 3, 8, 7, "canvas_side")
    i.ellipse(1, 14, 8, 18, "canvas_side")
    i.ellipse(1, 8, 7, 12, "canvas_side")
    i.ellipse(5, 3, 30, 17, "canvas")
    i.stripe(13, 14, "red", "canvas")
    i.stripe(15, 15, "cream", "canvas")
    i.stripe(24, 24, "red", "canvas")
    return i.image()


def range_finder():
    """The range finder: a long tube with a lens at each end, on a tripod."""
    i = Icon()
    i.bar((14, 17), (10, 29), 0.6, "dark")
    i.bar((17, 17), (21, 29), 0.6, "dark")
    i.bar((15, 17), (15, 28), 0.6, "dark")
    i.bar((4, 15), (27, 9), 2.3, "dark")
    i.bar((4, 15), (7, 14), 2.9, "brass")
    i.bar((24, 10), (27, 9), 2.9, "brass")
    i.oval((3.5, 15.2), (-0.96, 0.27), 2.6, 1.2, "lens")
    i.oval((27.5, 8.9), (0.96, -0.27), 2.6, 1.2, "lens")
    i.box3(12, 10, 5, 3, 2, "brass")
    return i.image()


def shell(length, width, band, tip="steel"):
    """A shell on the diagonal, nose up and right: a brass case with a rim, a driving band and a steel ogive."""
    i = Icon()
    half = length / 2
    p0 = (16 - half * 0.707, 16 + half * 0.707)
    p1 = (16 + half * 0.707, 16 - half * 0.707)
    body = _along(p0, p1, 0.6)
    i.bar(p0, body, width, "brass")
    i.bar(p0, _along(p0, p1, 0.06), width + 0.7, "brass")
    nx, ny = 0.707, 0.707
    mid = _along(p0, p1, 0.84)
    tip_point = _along(p0, p1, 1.04)
    i.poly([(body[0] + nx * width, body[1] + ny * width), (mid[0] + nx * width * 0.62, mid[1] + ny * width * 0.62), tip_point,
            (mid[0] - nx * width * 0.62, mid[1] - ny * width * 0.62), (body[0] - nx * width, body[1] - ny * width)], tip)
    i.bar(_along(p0, p1, 0.52), _along(p0, p1, 0.6), width + 0.4, band)
    return i.image()


def landship():
    """The Landship from the front left: the track loop round its side frame with the gilt crest, the black lacquered
    hull, the turret with its copper hatch and the cannon, the sponson gun and the short stacks at the back."""
    i = Icon()
    # The far track's top run, showing over the hull.
    i.poly([(9, 14), (12, 11), (27, 11), (29, 13), (27, 15), (11, 15)], "tread")
    # The hull between the tracks, with a gilt rail round its deck.
    i.box3(7, 15, 18, 7, 4, "lacquer")
    i.poly([(7, 15), (11, 11), (12, 11), (8, 15)], "gold", flat=True)
    i.rect(7, 15, 24, 15, "gold", flat=True)
    # The stacks at the back and the turret with its hatch.
    for x in (9, 12):
        i.rect(x, 7, x + 1, 13, "dark")
        i.rect(x, 9, x + 1, 9, "gold", flat=True)
    i.ellipse(12, 7, 22, 15, "lacquer")
    i.rect(12, 12, 22, 12, "gold", flat=True)
    i.ellipse(12, 5, 22, 11, "lacquer_top")
    i.ellipse(15, 6, 19, 9, "copper")
    barrel(i, (19, 10), (29, 5.5), 1.1, ring=0.6, bands=(0.6,), material="silver")
    # The near track loop round the side frame, the frame with its crest, and the sponson gun.
    i.poly([(1, 20), (5, 15), (22, 15), (28, 20), (25, 29), (4, 29)], "tread")
    for x in range(5, 25, 3):
        i.rect(x, 28, x + 1, 28, "dark", flat=True)
    i.poly([(4, 20), (7, 17), (21, 17), (25, 20), (22, 26), (6, 26)], "lacquer")
    i.rect(5, 20, 23, 20, "gold", flat=True)
    i.ellipse(8, 19, 14, 25, "gold")
    i.ellipse(10, 21, 12, 23, "red", flat=True)
    i.box3(16, 21, 5, 4, 2, "lacquer")
    barrel(i, (21, 22), (28, 21), 0.6, ring=0.5)
    return i.image()


def diesel_walker():
    """The Diesel Walker from the front: barrel shoulders, the red chest round its amber core, the twin stacks, the
    big fist on one arm and the drill on the other, and the legs on their red feet."""
    i = Icon()
    for x in (13, 17):
        i.rect(x, 1, x + 2, 7, "dark")
    # Legs first, so the pelvis and the chest sit over their tops.
    for x in (10, 18):
        i.rect(x, 19, x + 4, 26, "rust")
        i.rect(x, 22, x + 4, 22, "dark", flat=True)
        i.rect(x - 1, 27, x + 5, 29, "red")
    i.rect(10, 17, 22, 19, "dark")
    i.box3(9, 7, 13, 10, 2, "red")
    i.rect(14, 10, 16, 13, "amber")
    # The shoulders: a fat drum either side.
    i.rect(2, 6, 10, 12, "rust")
    i.rect(22, 6, 30 - 1, 12, "rust")
    for x in (4, 7, 24, 27):
        i.rect(x, 6, x, 12, "dark", flat=True)
    # The drill arm on the left, its bit pointing down.
    i.rect(3, 13, 7, 16, "steel")
    i.poly([(2, 17), (8, 17), (5, 26)], "silver")
    for y in (19, 22):
        i.rect(3, y, 7, y, "steel", flat=True)
    # The fist arm on the right.
    i.rect(24, 13, 27, 15, "steel")
    i.box3(22, 16, 7, 6, 1, "red")
    for y in (18, 20):
        i.rect(22, y, 28, y, "red_side", flat=True)
    return i.image()


def zeppelin():
    """The Zeppelin in profile, nose to the right: the long rigid envelope with its red bands, the cross of tail fins
    with red edges, and the gondola slung close under it with its engine grille, window and propeller."""
    i = Icon()
    i.poly([(1, 2), (4, 2), (10, 9), (3, 9)], "canvas_side")
    i.poly([(1, 21), (4, 21), (10, 14), (3, 14)], "canvas_side")
    i.rect(1, 2, 2, 8, "red", flat=True)
    i.rect(1, 15, 2, 21, "red", flat=True)
    i.ellipse(2, 5, 30, 18, "canvas")
    i.poly([(2, 11), (8, 10), (8, 13), (2, 12)], "canvas_side")
    i.stripe(10, 11, "red", "canvas")
    i.stripe(21, 21, "red", "canvas")
    i.stripe(27, 30, "cream", "canvas")
    for x in (13, 21):
        i.rect(x, 18, x, 20, "dark", flat=True)
    i.box3(11, 21, 13, 5, 2, "lacquer")
    i.rect(13, 22, 17, 24, "teal", flat=True)
    i.rect(19, 22, 21, 23, "amber", flat=True)
    i.rect(8, 22, 10, 23, "steel")
    i.rect(7, 19, 7, 26, "silver")
    return i.image()


KINDS = {
    "grand_mortar": grand_mortar,
    "bastion_mortar": bastion_mortar,
    "fortress_rifle": fortress_rifle,
    "bastion_autocannon": bastion_autocannon,
    "triple_battery": triple_battery,
    "siege_mortar": siege_mortar,
    "self_propelled_howitzer": self_propelled_howitzer,
    "flak_gun": flak_gun,
    "observation_balloon": observation_balloon,
    "range_finder": range_finder,
    "heavy_shell": lambda: shell(24, 3.0, "copper"),
    "flak_shell": lambda: shell(18, 2.2, "copper", "red"),
    "great_shell": lambda: shell(29, 4.2, "red"),
    "cannon_shell": lambda: shell(20, 2.4, "lacquer"),
    "landship": landship,
    "diesel_walker": diesel_walker,
    "zeppelin": zeppelin,
}


def draw(kind):
    return KINDS[kind]()

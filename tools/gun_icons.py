"""The big guns' item icons (5 October 2026: the owner asked for every weapon's icon to "look way cooler, maybe not even
accurate but cooler"): 32x32 pixel art in the arms icons' rules (docs/ART_DIRECTION.md, "Weapons are pixel art"). Each
icon is a few flat shapes, each lit along its top and left edge and shaded along its bottom and right one, with a dark
one-pixel outline round the whole silhouette; no dithering, noise or glints. Guns sit in a three-quarter view with the
barrel on the 45-degree diagonal; the 5x5 tower guns fill the frame and the 3x3 ones stand smaller.

draw(kind) returns the icon for any of KINDS; tools/tower_guns.py and tools/artillery.py save them.
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
        img = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))
        px = img.load()
        reg = self.region

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


def grand_mortar():
    i = Icon()
    plinth(i, 1, 30, 25, depth=4, height=5)
    i.box3(4, 15, 16, 7, 4, "yellow")
    port(i, 8, 18, 2)
    barrel(i, (15, 14), (25, 4), 4.6, ring=1.4, bands=(0.45,))
    i.box3(5, 19, 4, 3, 1, "dark")
    return i.image()


def bastion_mortar():
    i = Icon()
    plinth(i, 4, 27, 26, depth=3, height=4)
    i.box3(7, 17, 13, 6, 3, "yellow")
    port(i, 10, 20, 1.5)
    barrel(i, (16, 16), (24, 8), 3.6, ring=1.2)
    return i.image()


def fortress_rifle():
    i = Icon()
    plinth(i, 1, 30, 25, depth=4, height=5)
    box = (2, 15, 13, 7, 5)
    i.box3(*box, "yellow")
    i.poly([(4, 14), (7, 11), (16, 11), (13, 14)], "steel_top")
    port(i, 6, 18, 2)
    barrel(i, side_centre(*box), (30, 2), 1.3, ring=0.8, brake=True)
    return i.image()


def bastion_autocannon():
    i = Icon()
    plinth(i, 4, 27, 26, depth=3, height=4)
    box = (6, 16, 11, 7, 4)
    i.box3(*box, "yellow")
    port(i, 9, 19, 1.5)
    sx, sy = side_centre(*box)
    for off in (-3, 0):
        barrel(i, (sx - 2 + off, sy + 1 + off), (28 + off, 3 + off), 0.7, ring=0.6)
    i.rect(11, 10, 12, 12, "dark")
    return i.image()


def triple_battery():
    i = Icon()
    plinth(i, 1, 30, 25, depth=4, height=5)
    for off in (-4, 0, 4):
        barrel(i, (16 + off, 16 + off), (27 + off, 5 + off), 1.0, ring=0.6)
    i.poly([(2, 15), (20, 15), (20, 21), (2, 21)], "yellow")
    i.ellipse(2, 17, 20, 24, "yellow")
    i.ellipse(2, 10, 20, 19, "yellow_top")
    i.ellipse(6, 10, 13, 14, "steel_top")
    port(i, 5, 19, 1.5)
    return i.image()


def siege_mortar():
    i = Icon()
    i.ellipse(1, 21, 29, 30, "concrete_side")
    i.ellipse(1, 19, 29, 27, "concrete")
    i.ellipse(4, 19, 26, 25, "deck")
    i.box3(6, 16, 14, 6, 3, "yellow")
    port(i, 9, 19, 1.5)
    barrel(i, (15, 15), (24, 5), 4.2, ring=1.3, bands=(0.45,))
    i.bar((19, 21), (23, 16), 0.8, "copper")
    return i.image()


def self_propelled_howitzer():
    i = Icon()
    i.poly([(1, 23), (4, 20), (26, 20), (29, 23), (26, 29), (4, 29)], "dark")
    for x in (6, 11, 16, 21):
        i.ellipse(x - 2, 23, x + 2, 27, "steel")
    i.box3(2, 17, 22, 5, 3, "khaki")
    box = (5, 10, 9, 7, 3)
    barrel(i, side_centre(*box), (30, 4), 1.2, ring=0.9, brake=True)
    i.box3(*box, "khaki")
    i.rect(6, 12, 10, 12, "dark", flat=True)
    i.rect(7, 6, 10, 7, "olive")
    return i.image()


def flak_gun():
    i = Icon()
    i.bar((2, 25), (28, 30), 1.0, "olive")
    i.bar((4, 30), (27, 24), 1.0, "olive")
    i.box3(12, 22, 6, 5, 2, "olive")
    box = (7, 15, 11, 7, 3)
    sx, sy = side_centre(*box)
    for off in (-2, 1):
        barrel(i, (sx - 3 + off, sy + 1 + off), (28 + off, 2 + off), 0.7, ring=0.7, bands=(0.85,))
    i.box3(*box, "olive")
    i.ellipse(8, 16, 13, 21, "brass")
    i.rect(13, 10, 14, 13, "dark")
    i.rect(11, 9, 16, 10, "steel")
    return i.image()


def observation_balloon():
    i = Icon()
    for x0, x1 in ((14, 15), (22, 21)):
        i.bar((x0, 14), (x1, 23), 0.4, "dark", flat=True)
    i.box3(13, 24, 9, 4, 2, "wicker")
    i.ellipse(1, 2, 9, 6, "canvas_side")
    i.ellipse(1, 14, 9, 18, "canvas_side")
    i.ellipse(1, 8, 8, 12, "canvas_side")
    i.ellipse(5, 3, 31, 17, "canvas")
    i.stripe(13, 14, "red", "canvas")
    i.stripe(15, 15, "cream", "canvas")
    i.stripe(24, 24, "red", "canvas")
    return i.image()


def range_finder():
    i = Icon()
    i.bar((14, 17), (10, 29), 0.6, "dark")
    i.bar((17, 17), (21, 29), 0.6, "dark")
    i.bar((15, 17), (15, 28), 0.6, "dark")
    i.bar((3, 15), (28, 8), 2.3, "dark")
    i.bar((3, 15), (6, 14), 2.9, "brass")
    i.bar((25, 9), (28, 8), 2.9, "brass")
    i.oval((2.5, 15.2), (-0.96, 0.27), 2.6, 1.2, "lens")
    i.oval((28.5, 7.9), (0.96, -0.27), 2.6, 1.2, "lens")
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
}


def draw(kind):
    return KINDS[kind]()

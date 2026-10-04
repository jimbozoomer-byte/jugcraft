"""High-detail (64x64) art for the arms of batch 42, drawn with tools/hd_art.py (docs/features/arms.md).

Each weapon is laid along the canvas diagonal, pommel or butt at the bottom left and point at the top right, the way
vanilla draws its swords, so one sprite serves the inventory and the hand: the item models scale it up in the hand.
Bronze arms are steampunk (brass fittings, leather wraps, a warm bronze blade); steel arms follow the steel armor's
kaiserpunk (blued steel, gunmetal fittings, black rubber grips, a brass rivet here and there). All original.
"""
import math

import hd_art as hd
from hd_art import Canvas, Material

BRONZE = Material([(58, 30, 12), (100, 56, 22), (146, 88, 38), (186, 122, 58), (220, 160, 92), (246, 206, 146)], 0.55, 18)
LEATHER = Material([(36, 20, 12), (58, 34, 20), (84, 52, 30), (110, 72, 44), (136, 94, 60)], 0.12, 6, 0.32)
WOOD = Material([(44, 28, 16), (70, 46, 26), (98, 66, 38), (126, 88, 52), (152, 112, 70)], 0.1, 6, 0.32)
DARK_WOOD = Material([(26, 18, 14), (42, 30, 22), (60, 44, 32), (80, 60, 44), (102, 78, 58)], 0.1, 6, 0.32)
BLUED = Material([(26, 30, 40), (46, 54, 70), (72, 84, 104), (108, 122, 144), (156, 170, 190), (214, 224, 236)], 0.65, 22)


class Style:
    """The materials a metal's arms are made of."""

    def __init__(self, blade, fitting, grip, haft, accent):
        self.blade = blade
        self.fitting = fitting
        self.grip = grip
        self.haft = haft
        self.accent = accent


STYLES = {
    "bronze": Style(BRONZE, hd.BRASS, LEATHER, WOOD, hd.BRASS),
    "steel": Style(BLUED, hd.GUNMETAL, hd.RUBBER, DARK_WOOD, hd.BRASS),
}


class Axis:
    """Weapon space: s along the weapon from the grip end towards the point, t across it (towards the top left)."""

    def __init__(self, origin=(6.0, 58.0), angle=-45.0, scale=1.0):
        self.ox, self.oy = origin
        a = math.radians(angle)
        self.ax, self.ay = math.cos(a), math.sin(a)
        self.ux, self.uy = self.ay, -self.ax
        self.k = scale
        self.angle = angle

    def __call__(self, s, t=0.0):
        s, t = s * self.k, t * self.k
        return (self.ox + self.ax * s + self.ux * t, self.oy + self.ay * s + self.uy * t)

    def r(self, size):
        return size * self.k


def blade(c, w, s0, s1, half, material, ridge=True, fuller=None, edge=None):
    """A blade from s0 to s1, `half(f)` wide either side of its spine at f (0 at s0, 1 at s1). Its two halves face
    a little apart, so a bright ridge runs along the middle; `fuller` is a groove (f0, f1) down the middle."""
    steps = 24
    left, right = [], []
    for i in range(steps + 1):
        f = i / steps
        s = s0 + (s1 - s0) * f
        h = half(f)
        left.append(w(s, h))
        right.append(w(s, -h))
    spine = [w(s0 + (s1 - s0) * i / steps, 0) for i in range(steps + 1)]
    tilt = 0.45 if ridge else 0.0
    # Normals tilted across the blade, in canvas space.
    ux, uy = w.ux, w.uy
    c.polygon(spine + left[::-1], material, normal=_unit(ux * tilt, uy * tilt, 1.0))
    c.polygon(spine + right[::-1], material, normal=_unit(-ux * tilt, -uy * tilt, 1.0))
    if fuller:
        f0, f1 = fuller
        a = w(s0 + (s1 - s0) * f0, 0)
        b = w(s0 + (s1 - s0) * f1, 0)
        c.capsule(a, b, w.r(0.9), material, tint=-0.28)
    if edge:
        for side in (left, right):
            for p, q in zip(side, side[1:]):
                c.line(p, q, edge, 1.0)


def _unit(x, y, z):
    n = math.sqrt(x * x + y * y + z * z)
    return (x / n, y / n, z / n)


def grip(c, w, s0, s1, radius, style, wraps=True):
    """A wrapped grip: the wrap's turns are bands of the fitting metal."""
    bands = [(i / 7, i / 7 + 0.05, style.fitting) for i in range(1, 7)] if wraps else None
    c.capsule(w(s0), w(s1), w.r(radius), style.grip, bands=bands)


def haft(c, w, s0, s1, radius, style, rings=()):
    c.capsule(w(s0), w(s1), w.r(radius), style.haft,
              bands=[(f, f + 0.03, style.fitting) for f in rings])


def longsword(style):
    c = Canvas()
    w = Axis()
    c.capsule(w(1.5), w(4.0), w.r(2.4), style.fitting)  # pommel
    grip(c, w, 4.0, 15.0, 1.6, style)
    c.grip = w(9.5)
    c.capsule(w(15.6, -7.0), w(15.6, 7.0), w.r(1.4), style.fitting)  # crossguard
    c.disc(w(15.6, -7.4), w.r(1.6), style.fitting)
    c.disc(w(15.6, 7.4), w.r(1.6), style.fitting)
    blade(c, w, 16.6, 82.0, lambda f: 3.1 - 0.5 * f if f < 0.82 else (2.6 * (1 - f) / 0.18), style.blade, fuller=(0.04, 0.6))
    c.disc(w(15.6), w.r(1.5), style.accent)
    return c


def greatsword(style):
    c = Canvas()
    w = Axis(origin=(4.0, 60.0), scale=0.92)
    c.capsule(w(0.5), w(4.0), w.r(2.8), style.fitting)
    grip(c, w, 4.0, 22.0, 1.9, style)
    c.grip = w(10.0)
    c.capsule(w(22.5, -10.0), w(22.5, 10.0), w.r(1.6), style.fitting)
    for t in (-10.6, 10.6):
        c.disc(w(22.5, t), w.r(2.0), style.fitting)
    # The ricasso and its parrying lugs, then the long blade.
    c.box(w(26.5), w.r(2.4), w.r(3.6), w.angle + 90, style.blade, bevel=1.0)
    for t in (-4.2, 4.2):
        c.capsule(w(29.0, t), w(30.0, t * 1.3), w.r(1.0), style.fitting)
    blade(c, w, 30.0, 92.0, lambda f: 4.2 - 0.8 * f if f < 0.85 else (3.4 * (1 - f) / 0.15), style.blade, fuller=(0.02, 0.55))
    return c


def rapier(style):
    c = Canvas()
    w = Axis(origin=(7.0, 57.0))
    c.disc(w(2.2), w.r(2.4), style.fitting)
    grip(c, w, 3.5, 13.0, 1.4, style)
    c.grip = w(8.0)
    # Swept hilt: a knuckle bow and curling quillons.
    c.capsule(w(13.5, 5.5), w(3.5, 5.0), w.r(0.8), style.fitting)
    c.capsule(w(13.5, -6.5), w(13.5, 6.5), w.r(1.0), style.fitting)
    c.capsule(w(13.5, -6.5), w(10.5, -8.0), w.r(0.8), style.fitting)
    c.ring(w(15.2), w.r(4.2), w.r(2.6), style.fitting)
    blade(c, w, 15.0, 84.0, lambda f: 1.4 * (1 - f) + 0.25, style.blade)
    return c


def flanged_mace(style):
    c = Canvas()
    w = Axis(origin=(9.0, 55.0))
    haft(c, w, 0.0, 44.0, 1.8, style, rings=(0.3, 0.92))
    grip(c, w, 1.0, 13.0, 2.1, style)
    c.grip = w(7.0)
    c.capsule(w(-1.0), w(1.2), w.r(2.6), style.fitting)
    # The head: a core with seven flanges, drawn as plates fanning round it.
    core = w(52.0)
    for i in range(7):
        a = math.radians(i * 360 / 7 + 10)
        tip = (core[0] + math.cos(a) * w.r(10.5), core[1] + math.sin(a) * w.r(10.5))
        side = (-math.sin(a) * w.r(2.6), math.cos(a) * w.r(2.6))
        base1 = (core[0] + side[0], core[1] + side[1])
        base2 = (core[0] - side[0], core[1] - side[1])
        c.polygon([base1, tip, base2], style.blade, normal=_unit(math.cos(a) * 0.5, math.sin(a) * 0.5, 1.0))
    c.disc(core, w.r(5.2), style.blade)
    c.disc(core, w.r(2.0), style.accent)
    c.capsule(w(58.5), w(61.5), w.r(1.6), style.blade)  # the cap spike
    return c


def war_hammer(style):
    c = Canvas()
    w = Axis(origin=(8.0, 56.0))
    haft(c, w, 0.0, 58.0, 1.8, style, rings=(0.28, 0.78))
    grip(c, w, 1.0, 14.0, 2.1, style)
    c.grip = w(7.5)
    c.capsule(w(-1.0), w(1.2), w.r(2.6), style.fitting)
    # Langets up the haft, then the head: a square hammer face on one side and a back spike on the other.
    c.capsule(w(42.0, 1.4), w(54.0, 1.4), w.r(0.7), style.fitting)
    c.capsule(w(42.0, -1.4), w(54.0, -1.4), w.r(0.7), style.fitting)
    c.box(w(52.0), w.r(4.4), w.r(5.2), w.angle, style.blade, bevel=1.6)
    c.box(w(52.0, 8.5), w.r(5.0), w.r(6.4), w.angle, style.blade, bevel=1.8)  # the face
    for t in (6.0, 11.0):
        c.disc(w(52.0, t), w.r(0.9), style.accent)
    c.polygon([w(48.0, -4.0), w(52.0, -17.0), w(56.0, -4.0)], style.blade, normal=_unit(-w.ux * 0.4, -w.uy * 0.4, 1.0))
    c.capsule(w(57.0), w(62.0), w.r(1.5), style.blade)  # top spike
    return c


def glaive(style):
    c = Canvas()
    w = Axis(origin=(3.5, 60.5), scale=0.78)
    haft(c, w, 0.0, 68.0, 1.7, style, rings=(0.12, 0.5, 0.95))
    c.capsule(w(-1.5), w(1.0), w.r(2.2), style.fitting)
    c.capsule(w(66.0), w(70.0), w.r(2.4), style.fitting)  # socket
    # A long single-edged blade, its cutting edge swelling to one side and its back with a small hook.
    blade(c, w, 69.0, 102.0, lambda f: (2.4 + 2.6 * math.sin(f * math.pi) * (1 - f * 0.4)) if f < 0.92 else 2.0 * (1 - f) / 0.08,
          style.blade, fuller=(0.05, 0.5))
    c.polygon([w(74.0, -2.2), w(77.0, -6.0), w(78.5, -2.2)], style.blade, normal=_unit(-w.ux * 0.4, -w.uy * 0.4, 1.0))
    c.grip = w(16.0)
    return c


def halberd(style):
    c = Canvas()
    w = Axis(origin=(3.5, 60.5), scale=0.78)
    haft(c, w, 0.0, 74.0, 1.7, style, rings=(0.1, 0.55))
    c.capsule(w(-1.5), w(1.0), w.r(2.2), style.fitting)
    c.capsule(w(56.0, 1.3), w(70.0, 1.3), w.r(0.6), style.fitting)
    c.capsule(w(56.0, -1.3), w(70.0, -1.3), w.r(0.6), style.fitting)
    # The axe blade on one side, a back fluke on the other and the long spike on top.
    c.polygon([w(60.0, 2.0), w(56.0, 15.0), w(68.0, 17.0), w(76.0, 13.0), w(72.0, 2.0)], style.blade,
              normal=_unit(-w.ux * 0.15, -w.uy * 0.15, 1.0), tint=-0.12)
    c.line(w(56.0, 15.0), w(68.0, 17.0), (240, 244, 248), 1.0)
    c.polygon([w(63.0, -2.0), w(67.0, -11.0), w(70.0, -2.0)], style.blade, normal=_unit(-w.ux * 0.4, -w.uy * 0.4, 1.0))
    blade(c, w, 72.0, 102.0, lambda f: 2.0 * (1 - f) + 0.3, style.blade)
    for s in (60.0, 66.0):
        c.disc(w(s), w.r(0.9), style.accent)
    c.grip = w(16.0)
    return c


def spear(style):
    c = Canvas()
    w = Axis(origin=(3.5, 60.5), scale=0.78)
    haft(c, w, 0.0, 78.0, 1.5, style, rings=(0.08, 0.45))
    c.capsule(w(-1.5), w(1.0), w.r(1.9), style.fitting)
    c.capsule(w(74.0), w(80.0), w.r(2.1), style.fitting)  # socket
    blade(c, w, 79.0, 103.0, lambda f: 3.4 * math.sin(min(1.0, f * 1.6) * math.pi / 2) * (1 - f) + 0.2, style.blade, fuller=(0.05, 0.6))
    c.grip = w(22.0)
    return c


def lance(style):
    c = Canvas()
    w = Axis(origin=(3.5, 60.5), scale=0.78)
    # A tapering wooden shaft painted in the metal's colours, a grip and a broad vamplate guarding the hand.
    c.capsule(w(0.0), w(14.0), w.r(2.0), style.haft)
    grip(c, w, 14.0, 22.0, 2.0, style)
    shaft = [(f, f + 0.08, style.fitting if i % 2 else style.haft) for i, f in enumerate([k * 0.12 for k in range(8)])]
    c.capsule(w(28.0), w(92.0), w.r(2.6), style.haft, bands=shaft)
    c.polygon([w(22.0, 0.0), w(30.0, 8.5), w(32.0, 0.0), w(30.0, -8.5)], style.fitting)
    c.disc(w(29.0), w.r(3.2), style.fitting)
    blade(c, w, 91.0, 104.0, lambda f: 2.6 * (1 - f) + 0.2, style.blade)
    c.grip = w(18.0)
    return c


WEAPONS = {"longsword": longsword, "greatsword": greatsword, "rapier": rapier, "flanged_mace": flanged_mace,
           "war_hammer": war_hammer, "glaive": glaive, "halberd": halberd, "spear": spear, "lance": lance}


def draw(kind, metal):
    canvas = WEAPONS[kind](STYLES[metal])
    canvas.finish()
    return canvas.img


def held_at(kind):
    """Where the weapon is held on its 64x64 sprite: the middle of its grip, in pixels from the top left."""
    return WEAPONS[kind](STYLES["bronze"]).grip

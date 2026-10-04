"""High-detail (64x64) art for the arms of batches 42, 45 and 46, drawn with tools/hd_art.py (docs/features/arms.md).

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
# Arms IV's set stones (batch 47): a deep garnet in the bronze arms, a lit green phosphor cabochon in the steel ones (the
# dieselpunk green of the gauges, docs/ART_DIRECTION.md).
GARNET = Material([(48, 4, 12), (92, 10, 24), (146, 20, 40), (196, 40, 60), (234, 96, 110), (255, 196, 200)], 0.85, 26, 0.35)
PHOSPHOR = Material([(8, 44, 22), (18, 90, 42), (36, 146, 68), (76, 204, 108), (148, 240, 168), (222, 255, 230)], 0.6, 20, 0.6)


class Style:
    """The materials a metal's arms are made of."""

    def __init__(self, blade, fitting, grip, haft, accent, gem, glint):
        self.blade = blade
        self.fitting = fitting
        self.grip = grip
        self.haft = haft
        self.accent = accent
        self.gem = gem
        self.glint = glint


STYLES = {
    "bronze": Style(BRONZE, hd.BRASS, LEATHER, WOOD, hd.BRASS, GARNET, (255, 214, 168)),
    "steel": Style(BLUED, hd.GUNMETAL, hd.RUBBER, DARK_WOOD, hd.BRASS, PHOSPHOR, (190, 255, 210)),
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


def curved_blade(c, w, s0, s1, half, bend, material, edge=None):
    """A single-edged blade from s0 to s1 whose spine bends towards -t by bend * f squared (f from 0 to 1), its back
    straight along the spine and its edge `half(f)` out on the -t side."""
    steps = 28
    back, front = [], []
    for i in range(steps + 1):
        f = i / steps
        s = s0 + (s1 - s0) * f
        t = -bend * f * f
        back.append(w(s, t + half(f) * 0.25))
        front.append(w(s, t - half(f)))
    c.polygon(back + front[::-1], material, normal=_unit(-w.ux * 0.35, -w.uy * 0.35, 1.0))
    c.polygon(back[:-1] + [w(s0 + (s1 - s0) * (steps - 1) / steps, -bend)], material, normal=_unit(w.ux * 0.3, w.uy * 0.3, 1.0),
              tint=0.08)
    if edge:
        for p, q in zip(front, front[1:]):
            c.line(p, q, edge, 1.0)


# ---------------------------------------------------------------- Arms II (batch 45)


def dagger(style):
    c = Canvas()
    w = Axis(origin=(10.0, 54.0), scale=1.2)
    c.disc(w(1.6), w.r(2.2), style.fitting)  # pommel
    grip(c, w, 2.8, 11.0, 1.5, style)
    c.grip = w(7.0)
    c.capsule(w(11.6, -5.0), w(11.6, 5.0), w.r(1.2), style.fitting)  # short crossguard
    for t in (-5.3, 5.3):
        c.disc(w(11.6, t), w.r(1.3), style.fitting)
    # A diamond-section blade: a broad base tapering straight to a needle point.
    blade(c, w, 12.4, 50.0, lambda f: 2.7 * (1 - f) + 0.2, style.blade, fuller=(0.04, 0.35))
    c.disc(w(11.6), w.r(1.1), style.accent)
    return c


def sabre(style):
    c = Canvas()
    w = Axis(origin=(7.0, 57.0))
    c.capsule(w(1.5), w(3.5), w.r(2.0), style.fitting)
    grip(c, w, 3.5, 12.5, 1.5, style)
    c.grip = w(8.0)
    # A stirrup hilt: the knuckle bow from the guard round to the pommel, and a short back quillon.
    c.capsule(w(13.0, 6.0), w(13.0, -3.5), w.r(1.2), style.fitting)
    c.capsule(w(13.0, -3.5), w(2.5, -4.2), w.r(0.9), style.fitting)
    c.capsule(w(2.5, -4.2), w(1.8, -1.2), w.r(0.9), style.fitting)
    c.disc(w(13.0, 6.2), w.r(1.3), style.fitting)
    # A curved single-edged blade, its edge on the knuckle-bow side.
    curved_blade(c, w, 13.5, 80.0, lambda f: 3.0 - 0.4 * f if f < 0.86 else 2.7 * (1 - f) / 0.14, 10.0, style.blade,
                 edge=(236, 240, 246))
    return c


def estoc(style):
    c = Canvas()
    w = Axis(origin=(5.0, 59.0), scale=0.94)
    c.capsule(w(0.8), w(3.6), w.r(2.6), style.fitting)  # pear pommel
    grip(c, w, 3.6, 19.0, 1.6, style)
    c.grip = w(11.0)
    c.capsule(w(19.6, -8.5), w(19.6, 8.5), w.r(1.3), style.fitting)  # a long straight crossguard
    for t in (-8.9, 8.9):
        c.disc(w(19.6, t), w.r(1.5), style.fitting)
    c.ring(w(21.2, 2.6), w.r(2.6), w.r(1.6), style.fitting)  # a side ring
    # A stiff, edgeless, triangular blade for thrusting through mail.
    blade(c, w, 20.6, 92.0, lambda f: 2.1 * (1 - f) ** 0.8 + 0.25, style.blade)
    c.disc(w(19.6), w.r(1.2), style.accent)
    return c


def battle_axe(style):
    c = Canvas()
    w = Axis(origin=(5.0, 59.0), scale=0.84)
    haft(c, w, 0.0, 70.0, 1.8, style, rings=(0.3, 0.62))
    grip(c, w, 1.0, 14.0, 2.1, style)
    c.grip = w(8.0)
    c.capsule(w(-1.2), w(1.2), w.r(2.5), style.fitting)
    c.capsule(w(56.0, 1.5), w(70.0, 1.5), w.r(0.7), style.fitting)
    c.capsule(w(56.0, -1.5), w(70.0, -1.5), w.r(0.7), style.fitting)
    # A broad bearded head: the cheek, then a crescent edge sweeping down past the haft.
    head = [w(60.0, 2.0), w(56.0, 5.0), w(49.0, 9.0), w(42.0, 15.5), w(45.0, 19.0), w(56.0, 20.5), w(67.0, 20.0),
            w(76.0, 16.5), w(75.0, 9.0), w(72.0, 4.0), w(70.0, 2.0)]
    c.polygon(head, style.blade, normal=_unit(-w.ux * 0.12, -w.uy * 0.12, 1.0), tint=-0.08)
    for p, q in zip(head[3:8], head[4:9]):
        c.line(p, q, (236, 240, 246), 1.0)
    c.polygon([w(61.0, -2.0), w(64.0, -12.0), w(68.0, -2.0)], style.blade, normal=_unit(-w.ux * 0.4, -w.uy * 0.4, 1.0))
    for s in (62.0, 67.0):
        c.disc(w(s, 5.0), w.r(0.9), style.accent)
    c.capsule(w(70.0), w(75.0), w.r(1.6), style.blade)  # top spike
    return c


def flail(style):
    c = Canvas()
    w = Axis(origin=(8.0, 56.0))
    haft(c, w, 0.0, 26.0, 1.9, style, rings=(0.85,))
    grip(c, w, 1.0, 13.0, 2.1, style)
    c.grip = w(7.0)
    c.capsule(w(-1.0), w(1.2), w.r(2.5), style.fitting)
    c.capsule(w(25.0), w(28.5), w.r(2.2), style.fitting)  # the swivel cap
    # A chain of links curving out to the ball.
    links = 7
    for i in range(links):
        f = (i + 0.5) / links
        s = 29.0 + f * 24.0
        t = 5.5 * math.sin(f * math.pi)
        if i % 2:
            c.ring(w(s, t), w.r(2.0), w.r(1.1), style.fitting)
        else:
            c.capsule(w(s - 1.6, t), w(s + 1.6, t), w.r(0.9), style.fitting)
    # The spiked ball.
    ball = w(60.0, 1.0)
    for i in range(10):
        a = math.radians(i * 36 + 8)
        tip = (ball[0] + math.cos(a) * w.r(10.0), ball[1] + math.sin(a) * w.r(10.0))
        side = (-math.sin(a) * w.r(1.6), math.cos(a) * w.r(1.6))
        c.polygon([(ball[0] + side[0], ball[1] + side[1]), tip, (ball[0] - side[0], ball[1] - side[1])], style.blade,
                  normal=_unit(math.cos(a) * 0.5, math.sin(a) * 0.5, 1.0))
    c.disc(ball, w.r(6.4), style.blade)
    c.disc(ball, w.r(1.8), style.accent)
    return c


def scythe(style):
    c = Canvas()
    w = Axis(origin=(5.0, 59.0), scale=0.8)
    haft(c, w, 0.0, 76.0, 1.6, style, rings=(0.05,))
    c.capsule(w(-1.0), w(1.0), w.r(2.0), style.fitting)
    # The snath's two hand nibs, then the long curved blade swept back along -t from the top.
    for s in (6.0, 34.0):
        c.capsule(w(s, 0.0), w(s, 7.0), w.r(1.2), style.grip)
        c.disc(w(s, 7.2), w.r(1.3), style.fitting)
    c.grip = w(6.0)
    c.capsule(w(72.0), w(77.0), w.r(2.2), style.fitting)  # the tang collar
    tip_w = Axis(origin=w(75.0, 0.0), angle=w.angle - 98.0, scale=0.8)
    curved_blade(c, tip_w, 0.0, 44.0, lambda f: 3.2 - 1.2 * f if f < 0.85 else 2.2 * (1 - f) / 0.15, -9.0, style.blade,
                 edge=(236, 240, 246))
    return c


def quarterstaff(style):
    c = Canvas()
    w = Axis(origin=(3.5, 60.5), scale=0.76)
    haft(c, w, 0.0, 104.0, 1.9, style)
    # Iron-shod ends, studded collars and a wrapped middle for the two hands.
    for s0, s1 in ((-1.0, 9.0), (95.0, 105.0)):
        c.capsule(w(s0), w(s1), w.r(2.4), style.fitting)
        for s in (s0 + 2.5, s1 - 2.5):
            c.disc(w(s, 1.6), w.r(0.8), style.accent)
    grip(c, w, 38.0, 66.0, 2.15, style)
    c.grip = w(44.0)
    return c


def pike(style):
    c = Canvas()
    w = Axis(origin=(3.0, 61.0), scale=0.75)
    haft(c, w, 0.0, 96.0, 1.4, style, rings=(0.06, 0.25))
    c.capsule(w(-1.0), w(1.0), w.r(1.8), style.fitting)
    c.capsule(w(84.0, 1.1), w(96.0, 1.1), w.r(0.5), style.fitting)  # langets
    c.capsule(w(84.0, -1.1), w(96.0, -1.1), w.r(0.5), style.fitting)
    c.capsule(w(94.0), w(98.0), w.r(1.8), style.fitting)
    blade(c, w, 97.0, 108.0, lambda f: 2.4 * math.sin(min(1.0, f * 1.8) * math.pi / 2) * (1 - f) + 0.2, style.blade)
    c.grip = w(26.0)
    return c


# ---------------------------------------------------------------- Arms III (batch 46)


def zweihander(style):
    c = Canvas()
    w = Axis(origin=(3.0, 61.0), scale=0.82)
    c.capsule(w(0.0), w(3.6), w.r(3.0), style.fitting)  # pommel
    grip(c, w, 3.6, 25.0, 2.0, style)
    c.capsule(w(14.0), w(15.0), w.r(2.4), style.fitting)  # the ring between the hands
    c.grip = w(12.0)
    # A wide crossguard with drooping, curled ends, then the leather-wrapped ricasso and its parrying lugs.
    c.capsule(w(26.0, -12.0), w(26.0, 12.0), w.r(1.7), style.fitting)
    for t in (-1, 1):
        c.capsule(w(26.0, 12.0 * t), w(22.5, 15.0 * t), w.r(1.4), style.fitting)
        c.disc(w(22.0, 15.4 * t), w.r(1.9), style.fitting)
    c.box(w(31.0), w.r(4.2), w.r(3.4), w.angle, style.blade, bevel=1.0)
    grip(c, w, 28.0, 33.0, 2.6, style, wraps=False)
    for t in (-1, 1):
        c.capsule(w(35.0, 4.0 * t), w(37.0, 7.0 * t), w.r(1.1), style.fitting)
    blade(c, w, 35.5, 100.0, lambda f: 4.6 - 0.9 * f if f < 0.86 else (3.8 * (1 - f) / 0.14), style.blade, fuller=(0.02, 0.5))
    c.disc(w(26.0), w.r(1.8), style.accent)
    return c


def maul(style):
    c = Canvas()
    w = Axis(origin=(7.0, 57.0), scale=0.94)
    haft(c, w, 0.0, 56.0, 2.0, style, rings=(0.32, 0.72))
    grip(c, w, 1.0, 15.0, 2.3, style)
    c.grip = w(8.0)
    c.capsule(w(-1.2), w(1.2), w.r(2.8), style.fitting)
    c.capsule(w(44.0, 1.6), w(58.0, 1.6), w.r(0.8), style.fitting)  # langets
    c.capsule(w(44.0, -1.6), w(58.0, -1.6), w.r(0.8), style.fitting)
    # A great block of a head across the haft, banded at both ends, with broad striking faces.
    c.box(w(57.0), w.r(6.5), w.r(14.0), w.angle, style.blade, bevel=2.4)
    for t in (-10.5, 10.5):
        c.box(w(57.0, t), w.r(7.2), w.r(1.6), w.angle, style.fitting, bevel=0.8)
    for t in (-14.4, 14.4):
        c.box(w(57.0, t), w.r(6.0), w.r(0.9), w.angle, style.blade, bevel=0.6, tint=0.12)
    for s, t in ((54.0, -6.0), (60.0, -6.0), (54.0, 6.0), (60.0, 6.0)):
        c.disc(w(s, t), w.r(0.9), style.accent)
    return c


def executioner(style):
    c = Canvas()
    w = Axis(origin=(5.0, 59.0), scale=0.9)
    c.disc(w(1.8), w.r(3.2), style.fitting)  # a disc pommel
    c.disc(w(1.8), w.r(1.3), style.accent)
    grip(c, w, 4.0, 22.0, 1.9, style)
    c.grip = w(12.0)
    c.capsule(w(22.6, -8.0), w(22.6, 8.0), w.r(1.6), style.fitting)  # a short straight guard
    for t in (-8.4, 8.4):
        c.box(w(22.6, t), w.r(1.6), w.r(1.2), w.angle, style.fitting, bevel=0.6)
    # A broad blade with parallel edges and a blunt, rounded end: made for one stroke, not for the point.
    blade(c, w, 23.4, 88.0, lambda f: 5.6 if f < 0.93 else 5.6 * math.sqrt(max(0.0, 1.0 - ((f - 0.93) / 0.07) ** 2)),
          style.blade, fuller=(0.02, 0.45))
    for s in (74.0, 79.0):
        c.disc(w(s), w.r(0.9), style.accent)
    return c


def bill(style):
    c = Canvas()
    w = Axis(origin=(3.5, 60.5), scale=0.78)
    haft(c, w, 0.0, 80.0, 1.7, style, rings=(0.1, 0.5))
    c.capsule(w(-1.5), w(1.0), w.r(2.2), style.fitting)
    c.capsule(w(64.0, 1.3), w(80.0, 1.3), w.r(0.6), style.fitting)  # langets
    c.capsule(w(64.0, -1.3), w(80.0, -1.3), w.r(0.6), style.fitting)
    c.capsule(w(78.0), w(82.0), w.r(2.2), style.fitting)  # socket
    # The billhook: a broad blade rising from the socket and curling forward into a hook, a back spike, a top spike.
    hook = [w(80.0, 1.6), w(82.0, 7.0), w(88.0, 11.5), w(96.0, 13.0), w(101.0, 11.0), w(101.5, 7.5), w(98.0, 9.0),
            w(93.0, 8.0), w(90.0, 4.0), w(91.0, 1.6)]
    c.polygon(hook, style.blade, normal=_unit(-w.ux * 0.15, -w.uy * 0.15, 1.0), tint=-0.08)
    for p, q in zip(hook[1:6], hook[2:7]):
        c.line(p, q, (236, 240, 246), 1.0)
    c.polygon([w(84.0, -2.0), w(88.0, -10.0), w(90.0, -2.0)], style.blade, normal=_unit(-w.ux * 0.4, -w.uy * 0.4, 1.0))
    blade(c, w, 90.0, 106.0, lambda f: 1.8 * (1 - f) + 0.3, style.blade)
    c.disc(w(85.0, 3.0), w.r(0.9), style.accent)
    c.grip = w(18.0)
    return c


# ---------------------------------------------------------------- Arms IV (batch 47): the ornate style
#
# After the owner's reference sheets of fantasy weapon sets (studied for their look only; nothing is traced or copied):
# a faceted set stone as the focal point where head meets haft, guards that sweep into winged points, a lit rim along
# the edges that face the light, and a few glints of light about the head. Drawn with the same renderer and metals.


def gem(c, center, radius, style):
    """A faceted set stone in a ring of the fitting metal: a lozenge cut into four lit facets, with a glint."""
    cx, cy = center
    c.disc(center, radius + 1.3, style.fitting)
    top, right, bottom, left = (cx, cy - radius), (cx + radius, cy), (cx, cy + radius), (cx - radius, cy)
    for a, b, n in ((top, left, (-0.5, -0.5, 0.7)), (top, right, (0.4, -0.5, 0.7)), (bottom, left, (-0.4, 0.5, 0.7)),
                    (bottom, right, (0.5, 0.5, 0.7))):
        c.polygon([center, a, b], style.gem, normal=_unit(*n))
    c.disc(center, radius * 0.38, style.gem, dome=0.2, tint=0.15)
    c.pixel(cx - radius * 0.35, cy - radius * 0.45, (255, 255, 255))


def wing(c, w, s, side, reach, rise, material):
    """One wing of a guard: from the hilt at s out to `side` * reach across, sweeping up the weapon by rise to a point."""
    t0 = 1.5 * side
    pts = [w(s - 1.6, t0), w(s - 1.0, side * reach * 0.55), w(s + rise * 0.35, side * reach * 0.85),
           w(s + rise, side * reach), w(s + rise * 0.55, side * reach * 0.62), w(s + 1.2, side * reach * 0.38),
           w(s + 1.8, t0)]
    c.polygon(pts, material, normal=_unit(-w.ux * 0.3 * side, -w.uy * 0.3 * side, 1.0))


def rim(canvas, strength=0.3):
    """A lit rim: each painted pixel with open air above it or to its left (towards the light) is brightened, unless it
    is bright already."""
    src = canvas.img.copy()
    sp = src.load()
    n = canvas.size
    for y in range(n):
        for x in range(n):
            r, g, b, a = sp[x, y]
            if a < 255 or r + g + b > 600:
                continue
            open_up = y == 0 or sp[x, y - 1][3] < 255
            open_left = x == 0 or sp[x - 1, y][3] < 255
            if open_up or open_left:
                canvas.px[x, y] = (int(r + (255 - r) * strength), int(g + (255 - g) * strength), int(b + (255 - b) * strength), 255)


def flat(c, points, colour):
    """Fills a polygon with one colour from a ramp: the flat tones of hand-placed pixel art, with no dithering."""
    xs, ys = [p[0] for p in points], [p[1] for p in points]
    for y in range(max(0, int(min(ys))), min(c.size, int(max(ys)) + 1)):
        for x in range(max(0, int(min(xs))), min(c.size, int(max(xs)) + 1)):
            if hd._inside(points, x + 0.5, y + 0.5):
                c.put(x, y, colour)


def cut(c, points):
    """Clears every pixel inside a polygon (a notch, a hole)."""
    xs, ys = [p[0] for p in points], [p[1] for p in points]
    for y in range(max(0, int(min(ys))), min(c.size, int(max(ys)) + 1)):
        for x in range(max(0, int(min(xs))), min(c.size, int(max(xs)) + 1)):
            if hd._inside(points, x + 0.5, y + 0.5):
                c.px[x, y] = (0, 0, 0, 0)


def arc_blade(c, centre, radius, a0, a1, width, material, edge=None, steps=32):
    """A crescent blade along a circle: from angle a0 to a1 (degrees, canvas space), `width(f)` deep inwards from the
    circle at f (0 to 1), its cutting edge on the inside."""
    cx, cy = centre
    outer, inner = [], []
    for i in range(steps + 1):
        f = i / steps
        a = math.radians(a0 + (a1 - a0) * f)
        d = width(f)
        outer.append((cx + math.cos(a) * radius, cy + math.sin(a) * radius))
        inner.append((cx + math.cos(a) * (radius - d), cy + math.sin(a) * (radius - d)))
    # Flat tones: a dark back, a lighter middle band, a bright cutting edge on the inside.
    middle = [(o[0] + (i[0] - o[0]) * 0.4, o[1] + (i[1] - o[1]) * 0.4) for o, i in zip(outer, inner)]
    flat(c, outer + inner[::-1], material.ramp[2])
    flat(c, middle + inner[::-1], material.ramp[3])
    for p, q in zip(outer, outer[1:]):
        c.line(p, q, material.ramp[1], 1.0)
    if edge:
        for p, q in zip(inner, inner[1:]):
            c.line(p, q, edge, 1.0)
    return outer, inner


def glints(points, colour):
    """Small four-pointed glints of light in open air about the head, drawn after the outline (a step for canvas.after)."""
    def step(canvas):
        for x, y in points:
            x, y = int(round(x)), int(round(y))
            for dx, dy, k in ((0, 0, 1.0), (1, 0, 0.55), (-1, 0, 0.55), (0, 1, 0.55), (0, -1, 0.55)):
                px, py = x + dx, y + dy
                if 0 <= px < canvas.size and 0 <= py < canvas.size and canvas.px[px, py][3] == 0:
                    canvas.px[px, py] = tuple(int(v) for v in colour) + (int(255 * k),)
    return step


def ornate(c, glint_points, style):
    """Finish an Arms IV weapon: the lit rim now, the glints after the outline."""
    rim(c)
    c.after = [glints(glint_points, style.glint)]


def pommel(c, w, s, style):
    """A lozenge pommel with a short spike, as the reference sets end their grips."""
    c.polygon([w(s - 3.6), w(s, 3.0), w(s + 2.4), w(s, -3.0)], style.fitting, normal=_unit(-0.3, -0.4, 0.8))
    c.capsule(w(s - 5.6), w(s - 3.0), w.r(0.8), style.fitting)


def labrys(style):
    c = Canvas()
    w = Axis(origin=(5.0, 59.0), scale=0.82)
    haft(c, w, 0.0, 72.0, 1.9, style, rings=(0.32,))
    grip(c, w, 2.0, 18.0, 2.2, style)
    pommel(c, w, 1.6, style)
    c.grip = w(9.0)
    # Twin crescent bits either side of the haft, horns flaring back and forward, with bright edges.
    for side in (1, -1):
        bit = [w(54.0, 2.4 * side), w(50.0, 6.5 * side), w(44.0, 15.0 * side), w(46.5, 20.0 * side), w(52.0, 17.5 * side),
               w(60.0, 18.6 * side), w(68.0, 17.5 * side), w(73.5, 20.0 * side), w(76.0, 15.0 * side), w(70.0, 6.5 * side),
               w(66.0, 2.4 * side)]
        # Dark by the haft, a lighter cheek, a bright edge along the crescent: flat tones.
        dark = 1 if side > 0 else 1
        flat(c, bit, style.blade.ramp[dark])
        inner = [w(55.0, 3.6 * side), w(51.0, 7.5 * side), w(47.5, 15.0 * side), w(53.0, 15.6 * side), w(60.0, 16.6 * side),
                 w(67.0, 15.6 * side), w(72.5, 15.0 * side), w(69.0, 7.5 * side), w(65.0, 3.6 * side)]
        flat(c, inner, style.blade.ramp[3 if side > 0 else 2])
        cheek = [w(53.5, 9.0 * side), w(50.0, 14.0 * side), w(60.0, 15.0 * side), w(70.0, 14.0 * side), w(66.5, 9.0 * side)]
        flat(c, cheek, style.blade.ramp[4 if side > 0 else 3])
        for p, q in zip(bit[2:9], bit[3:10]):
            c.line(p, q, style.blade.ramp[-1], 1.0)
    c.box(w(60.0), w.r(7.0), w.r(3.2), w.angle, style.fitting, bevel=1.2)
    c.capsule(w(66.0), w(76.0), w.r(1.5), style.blade)  # the top spike
    gem(c, w(60.0), w.r(2.6), style)
    ornate(c, [w(44.0, 22.0), w(78.0, -18.0), w(80.0, 3.0)], style)
    return c


def battleblade(style):
    c = Canvas()
    w = Axis(origin=(4.0, 60.0), scale=0.86)
    pommel(c, w, 2.0, style)
    grip(c, w, 3.0, 21.0, 2.0, style)
    c.grip = w(11.0)
    # A broad cleaver of a blade with its point clipped back, saw notches cut from its back and a fuller down the middle.
    def half(f):
        return 6.4 + 0.6 * f if f < 0.86 else 7.0 * (1 - f) / 0.14 + 1.2
    steps = 24
    spine = [w(24.0 + 72.0 * i / steps, 0.0) for i in range(steps + 1)]
    back = [w(24.0 + 72.0 * i / steps, half(i / steps)) for i in range(steps + 1)]
    edge_side = [w(24.0 + 72.0 * i / steps, -half(i / steps)) for i in range(steps + 1)]
    flat(c, spine + back[::-1], style.blade.ramp[3])  # the lit half, towards the light
    flat(c, spine + edge_side[::-1], style.blade.ramp[2])
    c.line(w(28.0, 0.8), w(66.0, 0.8), style.blade.ramp[1], 1.0)  # the fuller
    c.line(w(28.0, 1.8), w(66.0, 1.8), style.blade.ramp[4], 1.0)
    for s0 in range(40, 86, 9):
        cut(c, [w(s0, 7.6), w(s0 + 3.0, 4.2), w(s0 + 4.5, 7.6)])
    for p, q in zip([w(f, -6.4 - 0.6 * (f - 24) / 72) for f in range(24, 86, 6)], [w(f + 6, -6.4 - 0.6 * (f - 18) / 72) for f in range(24, 86, 6)]):
        c.line(p, q, style.blade.ramp[-1], 1.0)
    # A winged guard sweeping up the blade, a collar and the set stone.
    for side in (1, -1):
        wing(c, w, 22.0, side, 11.0, 7.0, style.fitting)
    c.box(w(23.0), w.r(2.6), w.r(6.4), w.angle, style.fitting, bevel=1.0)
    gem(c, w(23.0), w.r(2.8), style)
    ornate(c, [w(98.0, 8.0), w(70.0, 12.0), w(30.0, -14.0)], style)
    return c


def war_fork(style):
    c = Canvas()
    w = Axis(origin=(3.5, 60.5), scale=0.76)
    haft(c, w, 0.0, 82.0, 1.7, style, rings=(0.1, 0.5))
    pommel(c, w, 1.0, style)
    c.grip = w(20.0)
    c.capsule(w(78.0), w(83.0), w.r(2.3), style.fitting)  # socket
    # A crossbar with the set stone, two side tines curling up from its ends and the long middle tine, all barbed.
    c.capsule(w(84.0, -7.5), w(84.0, 7.5), w.r(1.5), style.fitting)
    for side in (1, -1):
        c.capsule(w(84.0, 7.5 * side), w(98.0, 6.0 * side), w.r(1.7), style.blade)
        c.polygon([w(96.0, 7.2 * side), w(104.0, 5.4 * side), w(97.0, 4.6 * side)], style.blade,
                  normal=_unit(-w.ux * 0.3 * side, -w.uy * 0.3 * side, 1.0))
        c.polygon([w(90.0, 6.6 * side), w(87.5, 9.6 * side), w(91.5, 7.9 * side)], style.blade)  # barb
    blade(c, w, 84.0, 110.0, lambda f: 2.6 * (1 - f) + 0.4, style.blade)
    for side in (1, -1):
        c.polygon([w(100.0, 1.6 * side), w(97.5, 4.0 * side), w(102.0, 2.0 * side)], style.blade)  # barbs on the middle tine
    gem(c, w(84.0), w.r(2.4), style)
    ornate(c, [w(108.0, 9.0), w(96.0, -12.0)], style)
    return c


def kama(style):
    c = Canvas()
    w = Axis(origin=(14.0, 50.0), scale=1.1)
    pommel(c, w, 1.4, style)
    grip(c, w, 2.0, 17.0, 1.8, style)
    c.grip = w(9.0)
    c.capsule(w(16.6), w(19.4), w.r(2.2), style.fitting)  # ferrule
    # A crescent blade rising from the ferrule and curling right round over the hand, its edge on the inside.
    top = w(19.0)
    centre = (top[0] + 8.0, top[1] - 15.0)
    arc_blade(c, centre, 17.5, 128.0, -12.0, lambda f: 5.0 - 1.6 * f if f < 0.85 else 3.4 * (1 - f) / 0.15 + 0.2, style.blade,
              edge=style.blade.ramp[-1])
    gem(c, w(18.0), w.r(1.9), style)
    ornate(c, [(centre[0] + 12.0, centre[1] - 15.0), (centre[0] - 9.0, centre[1] - 6.0)], style)
    return c


def war_pick(style):
    c = Canvas()
    w = Axis(origin=(8.0, 56.0), scale=0.98)
    haft(c, w, 0.0, 50.0, 1.8, style, rings=(0.35,))
    grip(c, w, 1.5, 15.0, 2.1, style)
    pommel(c, w, 1.2, style)
    c.grip = w(8.0)
    # The head: a long beak curving down to one side, a square hammer face to the other, a top spike and the stone.
    beak = [w(44.5, 2.0), w(43.5, 9.0), w(40.5, 14.0), w(35.5, 19.0), w(29.0, 23.0), w(34.0, 16.0), w(39.5, 12.5),
            w(46.5, 13.0), w(51.0, 8.5), w(53.0, 2.0)]
    flat(c, beak, style.blade.ramp[2])
    flat(c, [w(44.5, 2.0), w(43.5, 9.0), w(40.5, 14.0), w(35.5, 19.0), w(29.0, 23.0), w(39.0, 12.0), w(45.0, 8.0),
             w(48.0, 2.0)], style.blade.ramp[3])
    for p, q in zip(beak[:5], beak[1:5]):
        c.line(p, q, style.blade.ramp[-1], 1.0)
    c.box(w(49.0, -7.0), w.r(4.6), w.r(4.4), w.angle, style.blade, bevel=1.6)
    c.box(w(49.0, -11.2), w.r(4.8), w.r(0.9), w.angle, style.blade, bevel=0.5, tint=0.15)
    c.box(w(49.0), w.r(5.6), w.r(3.2), w.angle, style.fitting, bevel=1.0)
    c.capsule(w(53.0), w(61.0), w.r(1.5), style.blade)
    for side in (1, -1):
        wing(c, w, 44.0, side, 6.0, -5.0, style.fitting)
    gem(c, w(49.5), w.r(2.2), style)
    ornate(c, [w(36.0, 24.0), w(62.0, 4.0)], style)
    return c


# ---------------------------------------------------------------- Arms V (batch 48): the arms with weapon arts


def flat_blade(c, w, s0, s1, half, style, fuller=None):
    """A straight two-edged blade in flat tones (as the ornate heads): the half towards the light lit, the other in
    shadow, a dark fuller line and bright edges."""
    steps = 24
    spine = [w(s0 + (s1 - s0) * i / steps, 0.0) for i in range(steps + 1)]
    lit = [w(s0 + (s1 - s0) * i / steps, half(i / steps)) for i in range(steps + 1)]
    shade = [w(s0 + (s1 - s0) * i / steps, -half(i / steps)) for i in range(steps + 1)]
    flat(c, spine + lit[::-1], style.blade.ramp[4])
    flat(c, spine + shade[::-1], style.blade.ramp[2])
    if fuller:
        f0, f1 = fuller
        c.line(w(s0 + (s1 - s0) * f0, 0.0), w(s0 + (s1 - s0) * f1, 0.0), style.blade.ramp[1], 1.0)
    for side in (lit, shade):
        for p, q in zip(side, side[1:]):
            c.line(p, q, style.blade.ramp[-1] if side is lit else style.blade.ramp[3], 1.0)


def twinblade(style):
    c = Canvas()
    w = Axis(origin=(4.0, 60.0), scale=0.8)
    # A grip in the middle, a winged guard and a stone at each end of it, and a blade out from each guard.
    grip(c, w, 40.0, 60.0, 2.0, style)
    c.grip = w(50.0)
    for s0, s1, toward in ((61.5, 99.0, 1), (38.5, 1.0, -1)):
        flat_blade(c, w, s0, s1, lambda f: 3.6 * (1 - f) ** 0.8 + 0.25, style, fuller=(0.05, 0.5))
        for side in (1, -1):
            wing(c, w, s0 - toward * 0.5, side * toward, 6.0, 4.5 * toward, style.fitting)
        c.box(w(s0 - toward * 0.5), w.r(1.6), w.r(5.0), w.angle, style.fitting, bevel=0.8)
        gem(c, w(s0 - toward * 0.5), w.r(2.0), style)
    ornate(c, [w(100.0, 6.0), w(-2.0, -6.0), w(70.0, -8.0)], style)
    return c


def nodachi(style):
    c = Canvas()
    w = Axis(origin=(4.0, 60.0), scale=0.78)
    c.capsule(w(0.5), w(2.5), w.r(1.9), style.fitting)  # the cap
    grip(c, w, 2.5, 26.0, 1.7, style)
    c.grip = w(13.0)
    # A round guard with the stone set in it, a collar, and a long blade curving gently back, its edge bright.
    c.disc(w(27.0), w.r(4.8), style.fitting, dome=0.4)
    c.disc(w(27.0), w.r(3.4), style.fitting, dome=0.2, tint=-0.15)
    c.capsule(w(28.0), w(31.0), w.r(2.0), style.accent)
    curved_blade(c, w, 29.5, 110.0, lambda f: 3.0 - 0.6 * f if f < 0.9 else 2.46 * (1 - f) / 0.1 + 0.1, 9.0, style.blade,
                 edge=style.blade.ramp[-1])
    gem(c, w(27.0), w.r(1.9), style)
    ornate(c, [w(104.0, 6.0), w(70.0, -12.0)], style)
    return c


def earthbreaker(style):
    c = Canvas()
    w = Axis(origin=(6.0, 58.0), scale=0.86)
    haft(c, w, 0.0, 62.0, 2.0, style, rings=(0.36, 0.7))
    grip(c, w, 2.0, 20.0, 2.3, style)
    pommel(c, w, 1.6, style)
    c.grip = w(11.0)
    # A great block of a head, wide across the haft, bound with straps, capped at both faces, a spike on top.
    c.box(w(56.0), w.r(7.5), w.r(13.0), w.angle, style.blade, bevel=1.8)
    for side in (1, -1):
        c.box(w(56.0, 13.6 * side), w.r(6.4), w.r(1.6), w.angle, style.blade, bevel=0.8, tint=0.15)
        c.box(w(56.0, 9.0 * side), w.r(8.1), w.r(1.3), w.angle, style.fitting, bevel=0.6)
        for s in (51.5, 60.5):
            c.disc(w(s, 5.0 * side), w.r(0.9), style.accent)
    c.capsule(w(63.5), w(68.5), w.r(1.8), style.blade)
    c.polygon([w(68.0, 1.8), w(73.0, 0.0), w(68.0, -1.8)], style.blade, normal=_unit(w.ux * 0.3, w.uy * 0.3, 1.0))
    gem(c, w(56.0), w.r(2.8), style)
    ornate(c, [w(56.0, 18.0), w(74.0, 6.0), w(42.0, -12.0)], style)
    return c


def katar(style):
    c = Canvas()
    w = Axis(origin=(12.0, 52.0), scale=1.1)
    # The frame: two side bars along the blade's line, joined by two cross grips the fist closes on.
    for t in (5.5, -5.5):
        c.capsule(w(0.0, t), w(14.0, t), w.r(1.3), style.fitting)
    for s in (3.5, 8.5):
        c.capsule(w(s, -5.5), w(s, 5.5), w.r(1.5), style.grip)
    c.grip = w(6.0)
    c.box(w(15.0), w.r(1.6), w.r(6.6), w.angle, style.fitting, bevel=0.8)
    # A broad triangular blade, thick at its base, with a fuller.
    flat_blade(c, w, 16.0, 46.0, lambda f: 5.0 * (1 - f) ** 1.2 + 0.2, style, fuller=(0.05, 0.55))
    gem(c, w(15.0), w.r(1.9), style)
    ornate(c, [w(46.0, 6.0), w(30.0, -9.0)], style)
    return c


def moonblade(style):
    c = Canvas()
    w = Axis(origin=(4.0, 60.0), scale=0.78)
    pommel(c, w, 2.0, style)
    grip(c, w, 3.0, 21.0, 1.9, style)
    c.grip = w(12.0)
    # A straight back and a crescent belly swelling out on the edge side, in flat tones, its edge bright.
    def belly(f):
        return 2.4 + 7.6 * math.sin(math.pi * min(f, 0.92) ** 0.8) if f < 0.92 else (2.4 + 7.6 * math.sin(math.pi * 0.92 ** 0.8)) * (1 - f) / 0.08
    steps = 26
    back = [w(26.0 + 74.0 * i / steps, 1.6) for i in range(steps + 1)]
    spine = [w(26.0 + 74.0 * i / steps, -0.6) for i in range(steps + 1)]
    edge = [w(26.0 + 74.0 * i / steps, -belly(i / steps)) for i in range(steps + 1)]
    flat(c, back + spine[::-1], style.blade.ramp[4])
    flat(c, spine + edge[::-1], style.blade.ramp[2])
    flat(c, [w(30.0 + 62.0 * i / steps, -0.6 - 0.45 * belly(4.0 / 74.0 + 62.0 / 74.0 * i / steps)) for i in range(steps + 1)]
         + spine[::-1][2:-2], style.blade.ramp[3])
    for p, q in zip(edge, edge[1:]):
        c.line(p, q, style.blade.ramp[-1], 1.0)
    cut(c, [w(34.0, -3.0), w(38.0, -5.6), w(41.0, -3.0), w(38.0, -4.2)])  # a small crescent pierced near the hilt
    for side in (1, -1):
        wing(c, w, 23.5, side, 9.0, 6.0, style.fitting)
    c.box(w(24.0), w.r(2.4), w.r(6.0), w.angle, style.fitting, bevel=1.0)
    gem(c, w(24.0), w.r(2.6), style)
    ornate(c, [w(100.0, -4.0), w(64.0, -14.0), w(40.0, 8.0)], style)
    return c


def kusarigama(style):
    c = kama(style)
    w = Axis(origin=(14.0, 50.0), scale=1.1)
    # A weighted chain from the butt, slung round under the blade: links turning in and out of the light.
    path = [w(0.0), (17.0, 56.0), (24.0, 60.0), (33.0, 61.0), (42.0, 59.0), (49.0, 55.5)]
    points = []
    for a, b in zip(path, path[1:]):
        for i in range(4):
            f = i / 4
            points.append((a[0] + (b[0] - a[0]) * f, a[1] + (b[1] - a[1]) * f))
    points.append(path[-1])
    for i, (p, q) in enumerate(zip(points, points[1:])):
        c.capsule(p, q, 0.9 if i % 2 else 0.7, style.fitting, tint=0.12 if i % 2 else -0.1)
    c.disc((53.0, 53.5), 3.8, style.blade)
    c.disc((52.2, 52.6), 1.2, style.blade, tint=0.3)
    return c


# ---------------------------------------------------------------- Arms VI (batch 53)


def katana(style):
    c = Canvas()
    w = Axis(origin=(6.0, 58.0), scale=0.86)
    c.capsule(w(0.5), w(2.5), w.r(1.8), style.fitting)  # the cap
    # A long cord-wrapped grip: diamonds of the wrap over the grip leather.
    grip(c, w, 2.5, 20.0, 1.7, style)
    for i in range(6):
        s = 4.0 + i * 2.7
        c.polygon([w(s, 0.0), w(s + 1.35, 1.6), w(s + 2.7, 0.0), w(s + 1.35, -1.6)], style.grip, tint=0.25)
    c.grip = w(11.0)
    # A small round guard with the stone in it, a collar, and a gently curved blade: a bright tempered edge and a
    # wavy temper line along it.
    c.disc(w(21.0), w.r(4.0), style.fitting, dome=0.4)
    c.disc(w(21.0), w.r(2.7), style.fitting, dome=0.2, tint=-0.15)
    c.capsule(w(22.0), w(24.5), w.r(1.9), style.accent)
    curved_blade(c, w, 23.5, 86.0, lambda f: 2.7 - 0.4 * f if f < 0.9 else 2.34 * (1 - f) / 0.1 + 0.1, 6.0, style.blade,
                 edge=style.blade.ramp[-1])
    for i in range(14):
        f0, f1 = i / 14, (i + 1) / 14
        s0, s1 = 25.0 + 56.0 * f0, 25.0 + 56.0 * f1
        t0 = -6.0 * f0 * f0 - (1.4 + 0.4 * (i % 2))
        t1 = -6.0 * f1 * f1 - (1.4 + 0.4 * ((i + 1) % 2))
        c.line(w(s0, t0), w(s1, t1), style.blade.ramp[4], 1.0)
    gem(c, w(21.0), w.r(1.7), style)
    ornate(c, [w(84.0, 4.0), w(52.0, -10.0)], style)
    return c


EMBER = Material([(80, 16, 4), (150, 40, 8), (214, 86, 16), (248, 146, 34), (255, 206, 90), (255, 246, 196)], 0.2, 8, 1.0)


def brazier_mace(style, frame=0):
    """A mace whose head is a brazier: a cage of bars round a bed of coals, with a flame licking up through it (frame
    0 to 3: the flame flickers)."""
    c = Canvas()
    w = Axis(origin=(7.0, 57.0), scale=0.9)
    haft(c, w, 0.0, 44.0, 1.9, style, rings=(0.4,))
    grip(c, w, 2.0, 17.0, 2.2, style)
    pommel(c, w, 1.6, style)
    c.grip = w(9.5)
    # The flame behind the bars: tongues from the coals up past the cage's crown, swaying with the frame.
    sway = (0.0, 1.2, -0.6, 0.8)[frame % 4]
    height = (0.0, 2.0, 1.0, -1.0)[frame % 4]
    for width, top, tone in ((8.5, 70.0 + height, 1), (6.4, 66.5 + height, 2), (4.4, 63.0 + height, 3), (2.4, 59.0 + height, 4)):
        flat(c, [w(46.0, -width), w(53.0, -width * 0.95 + sway * 0.5), w(top - 6.0, sway - width * 0.35), w(top, sway),
                 w(top - 7.0, sway + width * 0.45), w(53.0, width * 0.95 + sway * 0.5), w(46.0, width)], EMBER.ramp[tone])
    flat(c, [w(48.5, -1.2), w(56.0 + height * 0.5, sway * 0.6), w(48.5, 1.2)], EMBER.ramp[5])
    # The coals and the cage: a bowl, four bars curving up to a crown ring, and a spike above.
    c.box(w(45.5), w.r(2.2), w.r(8.0), w.angle, style.fitting, bevel=1.0, tint=-0.2)
    for t in (-7.5, -2.5, 2.5, 7.5):
        bow = 1.6 if abs(t) > 5 else 0.6
        c.capsule(w(46.0, t), w(53.0, t + bow * (1 if t > 0 else -1)), w.r(0.85), style.fitting, tint=-0.3)
        c.capsule(w(53.0, t + bow * (1 if t > 0 else -1)), w(60.0, t * 0.6), w.r(0.85), style.fitting, tint=-0.3)
    c.capsule(w(60.0, -5.2), w(60.0, 5.2), w.r(1.1), style.fitting, tint=-0.15)
    c.capsule(w(61.0), w(67.0), w.r(1.2), style.blade, tint=-0.15)
    gem(c, w(45.5), w.r(2.2), style)
    return c


# Kinds whose sprite flickers (an animated item texture, its frames top to bottom in one strip): frames, ticks each.
ANIMATED = {"brazier_mace": (4, 3)}


WEAPONS = {"longsword": longsword, "greatsword": greatsword, "rapier": rapier, "flanged_mace": flanged_mace,
           "war_hammer": war_hammer, "glaive": glaive, "halberd": halberd, "spear": spear, "lance": lance,
           "dagger": dagger, "sabre": sabre, "estoc": estoc, "battle_axe": battle_axe, "flail": flail, "scythe": scythe,
           "quarterstaff": quarterstaff, "pike": pike, "zweihander": zweihander, "maul": maul, "executioner": executioner,
           "bill": bill, "labrys": labrys, "battleblade": battleblade, "war_fork": war_fork, "kama": kama,
           "war_pick": war_pick, "twinblade": twinblade, "nodachi": nodachi, "earthbreaker": earthbreaker, "katar": katar,
           "moonblade": moonblade, "kusarigama": kusarigama, "katana": katana, "brazier_mace": brazier_mace}


def draw(kind, metal, frame=0):
    canvas = WEAPONS[kind](STYLES[metal], frame) if kind in ANIMATED else WEAPONS[kind](STYLES[metal])
    canvas.finish()
    for step in getattr(canvas, "after", ()):
        step(canvas)
    return canvas.img


def held_at(kind):
    """Where the weapon is held on its 64x64 sprite: the middle of its grip, in pixels from the top left."""
    return WEAPONS[kind](STYLES["bronze"]).grip

"""Original textures for the hot-air balloon fiesta (fall addition 29), painted by code (docs/ART_DIRECTION.md, "High
resolution"):
  - the basket and rigging at 64 by 64: close-woven wicker, padded oxblood leather, the floor's planks, gunmetal steel,
    brass, the burner's copper coils, a fuel tank in red enamel with a stencilled serial, the instrument panel's three
    green-lit dials, the load cables and the suede sleeves on the uprights;
  - each envelope as one wrap 768 by 384 (entity/hot_air_balloon/envelope_<kind>), 32 pixels to each of its 24 gores,
    its crown at the top and its throat at the bottom, with the load tapes down its seams:
    - Harvest Stripes: gores of pumpkin, gold, cranberry and cream, a band of maple leaves round its widest, a cream
      crown;
    - the Jack-o'-Lantern: a ribbed orange pumpkin with a carved face on its front and back, over a green skirt of
      leaves; glow_pumpkin is that face alone, lit, for the night glow;
    - Harvest Moon: a night sky deepening to the crown, stars, a great harvest moon on its front with a witch on her
      broom across it, bats round it, and hills with pumpkins along its foot;
  - the burner's flame (blue at its root, gold at its tongues), the pumpkin's stem, the pibal's red latex;
  - the Mooring Post at 64 by 64: black cast iron, a granite plinth, tarred rope and brass;
  - the items: the three balloons, the burner and a pibal.

Called from crop_textures.crop_textures(). Every pixel is drawn here from a fixed seed (tools/fur_paint.py's painter);
nothing is read, traced or recoloured.
"""
import math

from fur_paint import mix, clean_painter as Painter, clean_ramp as ramp
from crop_textures import rgb

N = 64
WRAP_W, WRAP_H = 768, 384
GORE = WRAP_W // 24

WICKER = [rgb("3e2810"), rgb("6a4820"), rgb("94703a"), rgb("bc9a5c"), rgb("dcc28a")]
LEATHER = [rgb("220806"), rgb("40120c"), rgb("62201a"), rgb("843228"), rgb("a8503e")]
PLANK = [rgb("3a2416"), rgb("5c3c24"), rgb("7e5634"), rgb("9c7048"), rgb("b88c60")]
GUNMETAL = [rgb("121416"), rgb("24282c"), rgb("3a4046"), rgb("596068"), rgb("8a929a")]
BRASS = [rgb("4a3010"), rgb("7a5420"), rgb("a8803a"), rgb("d0aa5c"), rgb("f0d898"), rgb("fff4d4")]
COPPER = [rgb("3a160a"), rgb("6a2c14"), rgb("9c4a24"), rgb("cc7040"), rgb("eea070")]
RED = [rgb("3a0606"), rgb("680e0c"), rgb("981a14"), rgb("c42c22"), rgb("e65a46")]
CREAM = [rgb("6e6656"), rgb("a49a84"), rgb("cfc6ae"), rgb("e9e2cc"), rgb("f9f5e8")]
SUEDE = [rgb("2a1c10"), rgb("44301c"), rgb("624a2e"), rgb("806446"), rgb("9e8262")]
ROPE = [rgb("2c2418"), rgb("4c4030"), rgb("70624a"), rgb("968868"), rgb("b8ac8c")]
PUMPKIN = [rgb("4a1a02"), rgb("8a3406"), rgb("c4560e"), rgb("ec7c1c"), rgb("ffaa4c"), rgb("ffd08a")]
GOLD = [rgb("5a3c08"), rgb("8e6412"), rgb("c0901e"), rgb("e6b838"), rgb("fbde7c")]
CRANBERRY = [rgb("380612"), rgb("620c22"), rgb("8c1632"), rgb("b42646"), rgb("d8506c")]
LEAF_GREEN = [rgb("0c2210"), rgb("183a1a"), rgb("285828"), rgb("3e7a36"), rgb("68a050")]
NIGHT = [rgb("05060f"), rgb("0c1028"), rgb("162048"), rgb("223268"), rgb("34508c")]
MOON = [rgb("6a3a10"), rgb("b06a1c"), rgb("e09a34"), rgb("f6c060"), rgb("fff0b0")]
GLOW = [rgb("a04008"), rgb("e07a10"), rgb("ffb02a"), rgb("ffe070"), rgb("fff8d0")]
IRON = [rgb("0c0c0e"), rgb("1c1d20"), rgb("2e3034"), rgb("464a50"), rgb("6a7078")]
GRANITE = [rgb("38363a"), rgb("58565a"), rgb("78747a"), rgb("9a969c"), rgb("bcb8be")]
SOOT = [rgb("101010"), rgb("1e1c1a"), rgb("2e2a26"), rgb("403a34")]


# ---------------------------------------------------------------- basket and rigging

def wicker():
    """Close basket weave: stakes upright, the weavers passing in front of one and behind the next, each lit along its
    top, darker in the gaps."""
    p = Painter(N, N, 29001)
    for y in range(N):
        for x in range(N):
            row, col = y // 4, x // 8
            over = (row + col) % 2 == 0
            v = (y % 4) / 3.0
            f = 0.62 - 0.32 * abs(v - 0.35) + (0.08 if over else -0.12) + 0.06 * (p.noise(x, y, 6.0) - 0.5)
            if x % 8 in (0, 7):
                f -= 0.18
            p.put(x, y, ramp(WICKER, f))
    return p.img


def leather():
    """Padded oxblood leather: soft folds across, a row of stitching along each edge."""
    p = Painter(N, N, 29002)
    for y in range(N):
        for x in range(N):
            f = 0.55 + 0.12 * math.sin(y / N * math.pi * 3) + 0.08 * (p.noise(x, y, 9.0) - 0.5)
            p.put(x, y, ramp(LEATHER, f))
    for x in range(2, N, 4):
        for y in (4, N - 5):
            p.put(x, y, ramp(CREAM, 0.6))
            p.put(x + 1, y, ramp(CREAM, 0.45))
    return p.img


def floor():
    p = Painter(N, N, 29003)
    for y in range(N):
        for x in range(N):
            board = x // 16
            f = 0.55 + 0.1 * math.sin(board * 2.1) + 0.12 * (p.noise(x * 0.3, y * 2.5, 5.0) - 0.5)
            if x % 16 == 0:
                f -= 0.3
            p.put(x, y, ramp(PLANK, f))
    return p.img


def steel():
    """Gunmetal, worn bright at its edges, with heavy bolts."""
    p = Painter(N, N, 29004)
    for y in range(N):
        for x in range(N):
            edge = min(x, y, N - 1 - x, N - 1 - y)
            f = 0.48 + 0.06 * (p.noise(x, y, 7.0) - 0.5) + (0.25 if edge < 2 else 0.0)
            p.put(x, y, ramp(GUNMETAL, f))
    for x, y in ((7, 7), (N - 8, 7), (7, N - 8), (N - 8, N - 8)):
        p.blob(x, y, 2.6, 2.6, ramp(GUNMETAL, 0.15), ramp(GUNMETAL, 0.9))
    return p.img


def brass():
    p = Painter(N, N, 29005)
    for y in range(N):
        for x in range(N):
            p.put(x, y, ramp(BRASS, 0.5 + 0.35 * math.cos((x / N) * math.pi * 2.4 + 0.7) + 0.06 * (p.noise(x, y, 8.0) - 0.5)))
    return p.img


def coil():
    """The burner's coil seen from the side: copper tube wound in tight turns, blued and sooted by the heat towards
    the top."""
    p = Painter(N, N, 29006)
    for y in range(N):
        turn = (y % 8) / 7.0
        heat = 1.0 - y / N
        for x in range(N):
            f = 0.3 + 0.55 * math.sin(turn * math.pi) + 0.05 * (p.noise(x, y, 5.0) - 0.5)
            colour = ramp(COPPER, f)
            colour = mix(colour, (70, 60, 110), 0.35 * heat * (0.5 + 0.5 * math.sin(x * 0.2)))
            colour = mix(colour, SOOT[1], 0.25 * heat ** 2)
            p.put(x, y, colour)
    return p.img


def tank():
    """A fuel tank in red enamel, a white band round it with a stencilled serial, the paint chipped to steel."""
    p = Painter(N, N, 29007)
    for y in range(N):
        for x in range(N):
            f = 0.55 + 0.3 * math.cos((x / N - 0.35) * math.pi * 2) * 0.5 + 0.05 * (p.noise(x, y, 6.0) - 0.5)
            p.put(x, y, ramp(RED, f))
            if 26 <= y < 38:
                p.put(x, y, ramp(CREAM, f + 0.15))
            if p.noise(x * 3, y * 3, 3.0) > 0.86:
                p.put(x, y, ramp(GUNMETAL, 0.6))
    # The serial: blocky stencil marks.
    for k, x in enumerate(range(10, 54, 7)):
        for y in range(29, 35):
            if (k + y) % 3 != 0:
                p.put(x, y, ramp(GUNMETAL, 0.15))
                p.put(x + 2, y, ramp(GUNMETAL, 0.15))
        for xx in range(x, x + 3):
            p.put(xx, 29 + (k % 2) * 5, ramp(GUNMETAL, 0.15))
    return p.img


def gauge():
    """The instrument panel: three round dials with green-lit faces (altimeter, variometer, envelope heat) in a
    gunmetal panel, their needles, and a caged amber lamp."""
    p = Painter(N, N, 29008)
    for y in range(N):
        for x in range(N):
            p.put(x, y, ramp(GUNMETAL, 0.4 + 0.05 * (p.noise(x, y, 6.0) - 0.5)))
    for cx, angle in ((12, 2.3), (32, 0.6), (52, 1.6)):
        cy = 30
        for y in range(N):
            for x in range(N):
                d = math.hypot(x + 0.5 - cx, y + 0.5 - cy)
                if d < 9:
                    p.put(x, y, mix(rgb("0c3a18"), rgb("5aff8a"), 0.35 * (1 - d / 9)) if d < 7.5 else ramp(BRASS, 0.7))
        for k in range(9):
            a = math.pi * (0.8 + 1.4 * k / 8)
            p.put(cx + 6 * math.cos(a), cy + 6 * math.sin(a), rgb("b8ffcc"))
        p.line(cx, cy, cx + 6 * math.cos(math.pi + angle), cy - 6 * math.sin(angle), rgb("f4f4e0"), width=1.0)
    p.blob(32, 52, 4, 4, rgb("8a4a00"), rgb("ffd060"))
    p.line(28, 52, 36, 52, ramp(GUNMETAL, 0.2), width=0.8)
    return p.img


def rope():
    p = Painter(N, N, 29009)
    for y in range(N):
        for x in range(N):
            twist = math.sin((x * 0.6 + y * 0.35))
            p.put(x, y, ramp(ROPE, 0.5 + 0.25 * twist + 0.05 * (p.noise(x, y, 4.0) - 0.5)))
    return p.img


def suede():
    p = Painter(N, N, 29010)
    for y in range(N):
        for x in range(N):
            p.put(x, y, ramp(SUEDE, 0.55 + 0.12 * (p.noise(x, y, 4.0) - 0.5) + 0.05 * (p.noise(x, y, 1.5) - 0.5)))
    return p.img


# ---------------------------------------------------------------- envelopes

def seams(p, colours, dark=0.12):
    """The load tapes down every seam between gores, and the horizontal seams between panels, sewn a shade darker."""
    for y in range(WRAP_H):
        for x in range(WRAP_W):
            if x % GORE in (0, 1):
                old = p.px[x, y]
                p.put(x, y, mix(old[:3], (20, 14, 10), 0.35 if x % GORE == 0 else 0.15))
            elif y % 48 == 0 and y > 24:
                old = p.px[x, y]
                p.put(x, y, mix(old[:3], (20, 14, 10), dark))


def crown_and_throat(p, crown, throat_colours):
    """The crown's parachute valve (a ring of a darker shade and its edge tape) and the throat's scorch-proof scoop."""
    for y in range(WRAP_H):
        for x in range(WRAP_W):
            if y < 10:
                p.put(x, y, ramp(crown, 0.5 + 0.1 * math.sin(x * 0.05)))
            elif y < 13:
                p.put(x, y, ramp(BRASS, 0.55))
            if y >= WRAP_H - 16:
                f = 0.4 + 0.15 * (p.noise(x, y, 6.0) - 0.5) - 0.2 * (y - (WRAP_H - 16)) / 16
                p.put(x, y, ramp(throat_colours, f))


def leaf(p, cx, cy, size, colours, angle=0.0):
    """A maple leaf: five pointed lobes on short stalks, veined."""
    lobes = ((0.0, 1.0), (-1.1, 0.75), (1.1, 0.75), (-2.0, 0.45), (2.0, 0.45))
    for y in range(int(cy - size * 1.3), int(cy + size * 1.3) + 1):
        for x in range(int(cx - size * 1.3), int(cx + size * 1.3) + 1):
            dx, dy = (x + 0.5 - cx) / size, (y + 0.5 - cy) / size
            rx = dx * math.cos(angle) + dy * math.sin(angle)
            ry = -dx * math.sin(angle) + dy * math.cos(angle)
            a = math.atan2(rx, -ry)
            r = math.hypot(rx, ry)
            reach = 0.25
            for la, ll in lobes:
                d = abs((a - la * 0.62 + math.pi) % (2 * math.pi) - math.pi)
                reach = max(reach, ll * max(0.0, 1 - d / 0.42) ** 0.8)
            if r <= reach or (abs(rx) < 0.06 and 0 < ry < 0.9):
                f = 0.55 + 0.25 * (1 - r) - (0.25 if abs(rx) < 0.04 else 0)
                p.put(x, y, ramp(colours, f))


def harvest():
    """Harvest Stripes: gores of pumpkin, gold, cranberry and cream down from a cream crown, with a band of maple leaves
    round its widest in the colours of fall, edged in gold."""
    p = Painter(WRAP_W, WRAP_H, 29101)
    order = (PUMPKIN, GOLD, CRANBERRY, CREAM)
    band = (int(WRAP_H * (1 - 0.66)), int(WRAP_H * (1 - 0.54)))
    for y in range(WRAP_H):
        for x in range(WRAP_W):
            gore = x // GORE
            colours = order[gore % 4]
            across = (x % GORE) / GORE
            f = 0.6 - 0.12 * abs(across - 0.5) * 2 - 0.12 * (y / WRAP_H) + 0.04 * (p.noise(x, y, 14.0) - 0.5)
            if y < 46:
                colours, f = CREAM, 0.7 - 0.1 * (1 - y / 46)
            if band[0] <= y < band[1]:
                colours, f = SOOT, 0.55 + 0.1 * (p.noise(x, y, 10.0) - 0.5)
            if y in (band[0], band[0] + 1, band[1] - 2, band[1] - 1):
                colours, f = GOLD, 0.8
            p.put(x, y, ramp(colours, f))
    middle = (band[0] + band[1]) / 2
    for k in range(24):
        x = k * GORE + GORE / 2
        leaf(p, x, middle, 12, (PUMPKIN, GOLD, CRANBERRY)[k % 3], angle=0.4 * math.sin(k * 1.7))
    crown_and_throat(p, CREAM, SOOT)
    seams(p, PUMPKIN)
    return p.img


def pumpkin_face(p, cx, colours, lit):
    """A carved jack-o'-lantern face centred at `cx`: two triangle eyes, a triangle nose and a wide grin with teeth.
    Lit, it is the glow of the candle inside; carved, the dark of the cut with a rind of paler flesh round it."""
    eye_y0, eye_y1 = int(WRAP_H * (1 - 0.74)), int(WRAP_H * (1 - 0.62))
    nose_y0, nose_y1 = eye_y1 + 6, eye_y1 + 22
    mouth_y0, mouth_y1 = int(WRAP_H * (1 - 0.52)), int(WRAP_H * (1 - 0.38))

    def inside(x, y):
        for side in (-1, 1):
            ex = cx + side * 38
            t = (y - eye_y0) / (eye_y1 - eye_y0)
            if 0 <= t <= 1 and abs(x - ex) <= 22 * t:
                return True
        t = (y - nose_y0) / (nose_y1 - nose_y0)
        if 0 <= t <= 1 and abs(x - cx) <= 10 * t:
            return True
        u = (x - cx) / 74.0
        if abs(u) <= 1:
            top = mouth_y0 + 18 * (1 - u * u) * 0.4 - 8 * u * u
            bottom = mouth_y1 - 6 + 10 * (1 - u * u) - 18 * u * u
            tooth = (abs(u) < 0.6 and (int((u + 1) * 5) % 2 == 0))
            if top <= y <= bottom:
                if tooth and y < top + 12:
                    return False
                if tooth and y > bottom - 10 and abs(u) < 0.4:
                    return False
                return True
        return False
    for y in range(int(eye_y0 - 4), int(mouth_y1 + 10)):
        for x in range(int(cx - 90), int(cx + 90)):
            xx = x % WRAP_W
            if inside(x, y):
                if lit:
                    d = abs(x - cx) / 90
                    p.put(xx, y, ramp(GLOW, 0.95 - 0.35 * d - 0.15 * (p.noise(x, y, 6.0) - 0.5)))
                else:
                    p.put(xx, y, ramp(SOOT, 0.25 + 0.1 * (p.noise(x, y, 5.0) - 0.5)))
            elif not lit and any(inside(x + dx, y + dy) for dx, dy in ((2, 0), (-2, 0), (0, 2), (0, -2))):
                p.put(xx, y, ramp(PUMPKIN, 0.95))


def pumpkin():
    """The Jack-o'-Lantern: an orange pumpkin, each rib (two gores) lit across its swell and shadowed into its creases,
    faint streaks along it; a face carved on its front and back; a green skirt of leaves below; the stem's foot."""
    p = Painter(WRAP_W, WRAP_H, 29102)
    skirt_y = int(WRAP_H * (1 - 0.18))
    for y in range(WRAP_H):
        for x in range(WRAP_W):
            rib = (x % (2 * GORE)) / (2 * GORE)
            f = 0.62 + 0.22 * math.sin(rib * math.pi) - 0.28 * (1 - math.sin(rib * math.pi)) ** 3
            f += 0.05 * (p.noise(x * 0.5, y * 3, 9.0) - 0.5) - 0.1 * abs(y / WRAP_H - 0.45)
            colours = PUMPKIN
            if y < 18:
                colours, f = LEAF_GREEN, 0.4 + 0.2 * (p.noise(x, y, 5.0) - 0.5)
            if y >= skirt_y:
                colours, f = LEAF_GREEN, 0.45 + 0.2 * math.sin(x * 0.3) * 0.5 + 0.15 * (p.noise(x, y, 6.0) - 0.5)
            p.put(x, y, ramp(colours, f))
    # Scalloped leaf points over the skirt's top.
    for x in range(WRAP_W):
        u = (x % 24) / 24
        for y in range(skirt_y - 10, skirt_y):
            if y >= skirt_y - 10 * math.sin(u * math.pi):
                p.put(x, y, ramp(LEAF_GREEN, 0.55))
    for cx in (WRAP_W * 0.25, WRAP_W * 0.75):
        pumpkin_face(p, cx, PUMPKIN, lit=False)
    crown_and_throat(p, LEAF_GREEN, SOOT)
    return p.img


def pumpkin_glow():
    """The carved faces alone, lit: drawn over the envelope at full brightness while the burner fires."""
    p = Painter(WRAP_W, WRAP_H, 29103)
    for cx in (WRAP_W * 0.25, WRAP_W * 0.75):
        pumpkin_face(p, cx, GLOW, lit=True)
    return p.img


def moon():
    """Harvest Moon: night blue deepening to the crown, sprinkled with stars; a great harvest moon on its front with a
    witch on her broom across it; bats round about; and a gold-edged band of dark hills with lit pumpkins at its foot."""
    p = Painter(WRAP_W, WRAP_H, 29104)
    hills_y = int(WRAP_H * (1 - 0.2))
    for y in range(WRAP_H):
        for x in range(WRAP_W):
            f = 0.25 + 0.5 * (y / WRAP_H) + 0.06 * (p.noise(x, y, 30.0) - 0.5)
            p.put(x, y, ramp(NIGHT, f))
    for _ in range(420):
        x, y = p.rng.randrange(WRAP_W), p.rng.randrange(14, hills_y - 4)
        bright = p.rng.random()
        p.put(x, y, mix(NIGHT[3], (255, 250, 220), 0.5 + 0.5 * bright))
        if bright > 0.85:
            p.glint(x, y, (255, 248, 210))
    # The moon, on the front gores.
    mx, my, mr = WRAP_W * 0.25, WRAP_H * 0.42, 70
    for y in range(int(my - mr - 6), int(my + mr + 6)):
        for x in range(int(mx - mr - 6), int(mx + mr + 6)):
            d = math.hypot(x + 0.5 - mx, (y + 0.5 - my) * 1.15)
            if d <= mr:
                f = 0.75 - 0.25 * (d / mr) ** 2 + 0.12 * (p.noise(x, y, 18.0) - 0.5)
                f -= 0.18 * max(0.0, p.noise(x + 300, y, 9.0) - 0.62) * 4
                p.put(x, y, ramp(MOON, f))
            elif d <= mr + 6:
                p.put(x, y, ramp(MOON, 0.6), 0.25 * (1 - (d - mr) / 6))
    # The witch: a silhouette on her broom across the moon, her hat's brim and point, her cloak streaming back.
    black = (8, 8, 14)
    p.line(mx - 62, my + 22, mx + 40, my + 2, black, width=3.2)
    for k in range(10):
        p.line(mx + 38, my + 2, mx + 58 + k * 0.6, my - 6 + k * 2.0, black, width=1.2)
    p.ellipse(mx - 12, my + 6, 12, 15, black)
    p.ellipse(mx - 14, my - 14, 8, 8, black)
    p.line(mx - 30, my - 18, mx + 2, my - 22, black, width=2.4)
    for k in range(18):
        t = k / 17
        p.line(mx - 18 + 6 * t, my - 22 - 20 * t, mx - 8 + 2 * t, my - 22 - 20 * t, black, width=1.2)
    for k in range(6):
        p.line(mx - 22, my + 4 + k * 3, mx - 46 - k * 2, my - 2 + k * 5, black, width=2.0)
    # Bats round the sky.
    for k in range(18):
        bx, by = p.rng.randrange(WRAP_W), p.rng.randrange(60, hills_y - 40)
        if abs(bx - mx) < mr + 20 and abs(by - my) < mr + 20:
            continue
        s = p.rng.uniform(5, 9)
        p.ellipse(bx, by, s * 0.35, s * 0.5, black)
        for side in (-1, 1):
            for j in range(3):
                p.line(bx, by, bx + side * s * (1.0 + 0.5 * j), by - s * 0.5 + j * s * 0.35, black, width=1.4)
    # The hills and their pumpkins, edged in gold.
    for x in range(WRAP_W):
        crest = hills_y + 10 * math.sin(x * 0.021) + 6 * math.sin(x * 0.057 + 1.3)
        for y in range(int(crest), WRAP_H):
            p.put(x, y, ramp(SOOT, 0.3 + 0.1 * (p.noise(x, y, 8.0) - 0.5)))
        p.put(x, int(crest) - 1, ramp(GOLD, 0.7))
    for k in range(14):
        x = 20 + k * 54 + p.rng.randrange(20)
        crest = hills_y + 10 * math.sin(x * 0.021) + 6 * math.sin(x * 0.057 + 1.3)
        p.blob(x, crest + 7, 5, 4, ramp(PUMPKIN, 0.45), ramp(GLOW, 0.9))
    crown_and_throat(p, NIGHT, SOOT)
    seams(p, NIGHT, dark=0.06)
    return p.img


def flame():
    """The burner's flame, 64 by 128: a hard blue root over the jets, flaring gold and orange, licking into tongues,
    cut out round them."""
    w, h = 64, 128
    p = Painter(w, h, 29201)
    for y in range(h):
        t = 1 - y / h
        for x in range(w):
            u = (x + 0.5) / w - 0.5
            width = 0.18 + 0.28 * math.sin(min(1.0, t * 1.6) * math.pi * 0.85)
            wobble = 0.08 * math.sin(y * 0.21 + x * 0.05) + 0.12 * (p.noise(x, y, 9.0) - 0.5) * t
            if abs(u + wobble * t) < width * (1 - t ** 3):
                core = 1 - abs(u) / max(0.01, width)
                if t < 0.14:
                    colour = mix(rgb("2040ff"), rgb("a0c8ff"), core)
                else:
                    colour = ramp(GLOW, 0.35 + 0.6 * core - 0.35 * t)
                p.put(x, y, colour)
    return p.img


def stem():
    p = Painter(N, N, 29202)
    for y in range(N):
        for x in range(N):
            p.put(x, y, ramp(LEAF_GREEN, 0.45 + 0.2 * math.sin(x * 0.4) + 0.1 * (p.noise(x, y * 0.3, 6.0) - 0.5)))
    return p.img


def pibal_latex():
    """Red latex, glossy where the light catches it."""
    p = Painter(N, N, 29203)
    for y in range(N):
        for x in range(N):
            f = 0.6 + 0.2 * math.cos((x / N) * math.pi * 2 - 0.8) * math.sin(y / N * math.pi) + 0.03 * (p.noise(x, y, 8.0) - 0.5)
            p.put(x, y, ramp(RED, f))
    p.ellipse(20, 18, 4, 7, (255, 220, 210), 0.6)
    return p.img


# ---------------------------------------------------------------- the mooring post

def iron():
    """Black cast iron, its casting a little rough, rubbed bright where hands and ropes go."""
    p = Painter(N, N, 29301)
    for y in range(N):
        for x in range(N):
            f = 0.45 + 0.1 * (p.noise(x, y, 3.0) - 0.5) + 0.05 * (p.noise(x, y, 11.0) - 0.5)
            if 40 <= y < 46:
                f += 0.25
            p.put(x, y, ramp(IRON, f))
    return p.img


def granite():
    p = Painter(N, N, 29302)
    for y in range(N):
        for x in range(N):
            f = 0.5 + 0.2 * (p.noise(x, y, 2.0) - 0.5) + 0.1 * (p.noise(x, y, 7.0) - 0.5)
            p.put(x, y, ramp(GRANITE, f))
    for x in range(N):
        p.put(x, 0, ramp(GRANITE, 0.85))
    return p.img


def tarred_rope():
    p = Painter(N, N, 29303)
    for y in range(N):
        for x in range(N):
            twist = math.sin(x * 0.55 - y * 0.55)
            p.put(x, y, ramp([rgb("1a140c"), rgb("3a2e1c"), rgb("5e4c30"), rgb("84704c")], 0.5 + 0.3 * twist))
    return p.img


# ---------------------------------------------------------------- items

def balloon_item(kind):
    """A balloon aloft: its envelope in its design, the basket below on its cables."""
    p = Painter(N, N, 29400 + ("harvest", "pumpkin", "moon").index(kind))
    cx, cy = 32, 25
    for y in range(N):
        for x in range(N):
            dx, dy = (x + 0.5 - cx) / 21.0, (y + 0.5 - cy) / 22.0
            if dy < 0.7:
                inside = dx * dx + dy * dy <= 1.0 if dy <= 0 else abs(dx) <= math.sqrt(max(0.0, 1 - dy * dy)) * (1 - 0.55 * dy / 0.7)
            else:
                inside = False
            if not inside:
                continue
            light = 0.62 + 0.25 * (-dx - dy) * 0.5
            if kind == "harvest":
                band = int((math.asin(max(-1, min(1, dx / max(0.2, math.sqrt(max(0.04, 1 - min(dy, 0.99) ** 2)))))) + 1.6) * 2.4)
                colours = (PUMPKIN, GOLD, CRANBERRY, CREAM)[band % 4]
                if -0.1 < dy < 0.05:
                    colours = SOOT
            elif kind == "pumpkin":
                colours = PUMPKIN
                light += 0.15 * math.cos(dx * 9)
            else:
                colours = NIGHT
                light = 0.35 + 0.3 * (dy + 1) / 2
            p.put(x, y, ramp(colours, light))
    if kind == "pumpkin":
        for ex in (26, 38):
            p.line(ex - 3, 22, ex + 3, 22, ramp(GLOW, 0.9), width=1.2)
            p.line(ex - 3, 22, ex, 18, ramp(GLOW, 0.9), width=1.2)
            p.line(ex + 3, 22, ex, 18, ramp(GLOW, 0.9), width=1.2)
        p.line(24, 30, 40, 30, ramp(GLOW, 0.9), width=2.0)
        p.line(24, 30, 32, 34, ramp(GLOW, 0.8), width=2.0)
        p.line(40, 30, 32, 34, ramp(GLOW, 0.8), width=2.0)
        p.line(32, 4, 34, 0, ramp(LEAF_GREEN, 0.5), width=2.5)
    if kind == "moon":
        p.blob(26, 22, 8, 8, ramp(MOON, 0.45), ramp(MOON, 0.95))
        for k in range(6):
            p.put(36 + k * 2, 10 + (k % 3) * 9, (255, 250, 220))
    for side in (-1, 1):
        p.line(cx + side * 9, 41, cx + side * 4, 52, ramp(ROPE, 0.4), width=0.8)
    for y in range(52, 60):
        for x in range(26, 38):
            p.put(x, y, ramp(WICKER, 0.65 - 0.25 * (y - 52) / 8 + (0.08 if (x // 2 + y // 2) % 2 else -0.05)))
    for x in range(25, 39):
        p.put(x, 52, ramp(LEATHER, 0.6))
    return p.img


def burner_item():
    """The burner: two copper coils on a gunmetal frame, a brass valve lever, a blue pilot flame."""
    p = Painter(N, N, 29410)
    for x0 in (14, 34):
        for y in range(22, 46):
            for x in range(x0, x0 + 16):
                turn = ((y - 22) % 4) / 3.0
                p.put(x, y, ramp(COPPER, 0.35 + 0.5 * math.sin(turn * math.pi) + 0.15 * math.cos((x - x0) / 16 * math.pi * 2)))
        for x in range(x0 + 3, x0 + 13):
            for y in range(17, 22):
                p.put(x, y, ramp(BRASS, 0.6 + 0.2 * math.cos((x - x0) / 10 * 6)))
    for y in range(46, 52):
        for x in range(8, 56):
            p.put(x, y, ramp(GUNMETAL, 0.5))
    p.line(30, 50, 22, 60, ramp(BRASS, 0.8), width=2.5)
    p.ellipse(32, 12, 3, 6, rgb("6a9cff"))
    p.ellipse(32, 13, 1.5, 3.5, rgb("d8e8ff"))
    return p.img


def pibal_item():
    p = Painter(N, N, 29411)
    p.blob(32, 24, 15, 18, ramp(RED, 0.3), ramp(RED, 0.95))
    p.ellipse(26, 16, 3, 6, (255, 225, 215), 0.7)
    p.line(32, 42, 32, 46, ramp(RED, 0.4), width=2.5)
    for y in range(46, 62):
        p.put(32 + int(2 * math.sin(y * 0.5)), y, ramp(CREAM, 0.7))
    return p.img


def hot_air_balloon_textures():
    blocks = {"balloon_wicker": wicker(), "balloon_leather": leather(), "balloon_floor": floor(), "balloon_steel": steel(),
              "balloon_brass": brass(), "balloon_coil": coil(), "balloon_tank": tank(), "balloon_gauge": gauge(),
              "balloon_rope": rope(), "balloon_suede": suede(),
              "mooring_post_iron": iron(), "mooring_post_stone": granite(), "mooring_post_rope": tarred_rope(), "mooring_post_brass": brass()}
    out = {("block", name): img for name, img in blocks.items()}
    entity = {"envelope_harvest": harvest(), "envelope_pumpkin": pumpkin(), "envelope_moon": moon(), "glow_pumpkin": pumpkin_glow(),
              "flame": flame(), "stem": stem(), "pibal": pibal_latex()}
    out.update({("entity", f"hot_air_balloon/{name}"): img for name, img in entity.items()})
    out[("item", "harvest_balloon")] = balloon_item("harvest")
    out[("item", "pumpkin_balloon")] = balloon_item("pumpkin")
    out[("item", "harvest_moon_balloon")] = balloon_item("moon")
    out[("item", "balloon_burner")] = burner_item()
    out[("item", "pibal")] = pibal_item()
    return out

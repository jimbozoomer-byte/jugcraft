"""Original 16x16 textures for the weathered dieselpunk giants of batch 44 (tools/giant_models.py).

The owner's reference look: heavy rust and weathering, green verdigris patina on copper housings, riveted
perforated covers with round holes, copper and rusty pipes with hex-nut fittings, banded ribbed domes and coil
stacks, chipped red paint, skid feet and glowing amber conduits. Deterministic like the other texture sets:
each texture draws from its own seed. Names start with "dr_". Called from generate_textures.py.
"""
import math
import random

from steampunk_textures import new, put, save

RUST = [(46, 26, 18), (70, 38, 22), (98, 52, 28), (128, 68, 34), (156, 88, 44), (184, 112, 58), (206, 140, 80)]
PATINA = [(30, 62, 54), (46, 88, 74), (66, 116, 96), (90, 146, 120), (122, 176, 146), (160, 204, 176)]
COPPER = [(84, 40, 24), (122, 62, 36), (162, 88, 50), (196, 116, 70), (224, 152, 100), (244, 196, 150)]
IRON = [(22, 20, 20), (36, 33, 32), (52, 48, 46), (70, 65, 62), (92, 86, 82), (120, 112, 106)]
RED = [(70, 18, 14), (104, 26, 20), (138, 36, 26), (168, 52, 36), (192, 78, 54)]
AMBER = [(70, 34, 6), (130, 70, 10), (196, 118, 20), (240, 168, 40), (255, 210, 96), (255, 240, 180)]
BLUE = [(18, 40, 70), (28, 62, 104), (44, 88, 140), (78, 124, 176)]
STEEL = [(64, 66, 68), (92, 94, 96), (124, 126, 126), (160, 160, 156), (196, 194, 188)]


def mottle(rng, x, y, palette, low=1, high=4):
    """A weathered pick from `palette`: mostly the middle shades, darker in low patches."""
    n = math.sin(x * 1.7 + y * 0.6 + rng.random() * 0.9) + math.cos(y * 1.3 - x * 0.4) + rng.random() * 1.6
    index = low + int((n + 2) / 5.6 * (high - low + 1))
    return palette[max(low, min(high, index))]


def rust_plate(seed, rivets=True):
    """Heavily rusted steel: mottled browns and oranges, dark pits, rust runs streaking down, riveted edges."""
    rng = random.Random(seed)
    img = new()
    for y in range(16):
        for x in range(16):
            put(img, x, y, mottle(rng, x, y, RUST, 2, 4) if rng.random() < 0.9 else RUST[5])
    for _ in range(3):  # Rust runs down from a seam or a bolt.
        x, y = rng.randrange(16), rng.randrange(0, 6)
        for i in range(rng.randrange(5, 11)):
            put(img, x, y + i, RUST[5] if i % 3 else RUST[6])
    for _ in range(6):  # Pitting.
        put(img, rng.randrange(16), rng.randrange(16), RUST[0])
    if rivets:
        for x in range(1, 16, 4):
            for y in (1, 14):
                put(img, x, y, IRON[1])
                put(img, x + 1, y, RUST[6])
        for x in range(16):
            put(img, x, 0, RUST[2])
            put(img, x, 15, RUST[1])
    return img


def patina_plate(seed, holes=False):
    """Copper gone green: verdigris mottle, rust bleeding at the seams, rivets round the edge; optionally a
    perforated cover with round holes (the reference engine's riveted cover)."""
    rng = random.Random(seed)
    img = new()
    for y in range(16):
        for x in range(16):
            c = mottle(rng, x, y, PATINA, 1, 4)
            if rng.random() < 0.06:
                c = RUST[4]
            put(img, x, y, c)
    for x in range(16):
        put(img, x, 0, PATINA[0])
        put(img, x, 15, RUST[2] if rng.random() < 0.5 else PATINA[0])
    for y in range(16):
        put(img, 0, y, PATINA[0])
        put(img, 15, y, PATINA[1])
    for i in range(2, 15, 3):
        for x, y in ((i, 1), (i, 14), (1, i), (14, i)):
            put(img, x, y, PATINA[5])
    if holes:
        for cy in (4, 8, 12):
            for cx in (4, 8, 12):
                for dx, dy in ((0, 0), (1, 0), (0, 1), (1, 1)):
                    put(img, cx - 1 + dx, cy - 1 + dy, IRON[0])
                put(img, cx - 1, cy - 2, PATINA[0])
                put(img, cx + 1, cy + 1, PATINA[4])
    return img


def ribbed(seed, palette, accent=None):
    """Horizontal ribs every other row (coil stacks, dome bands), each rib lit on top and shaded below."""
    rng = random.Random(seed)
    img = new()
    for y in range(16):
        for x in range(16):
            if y % 4 == 0:
                c = palette[4]
            elif y % 4 == 1:
                c = palette[3]
            elif y % 4 == 2:
                c = palette[2]
            else:
                c = palette[0]
            if accent and rng.random() < 0.08:
                c = accent
            put(img, x, y, c)
    return img


def banded_dome(seed):
    """The reference hall's dome: rusty copper panels in bands, riveted seams and green streaks."""
    rng = random.Random(seed)
    img = new()
    for y in range(16):
        for x in range(16):
            c = mottle(rng, x, y, COPPER, 1, 4)
            if rng.random() < 0.15:
                c = RUST[3]
            if rng.random() < 0.05:
                c = PATINA[2]
            put(img, x, y, c)
    for y in (0, 7, 8):
        for x in range(16):
            put(img, x, y, RUST[1] if y != 8 else COPPER[5])
    for x in (0, 8):
        for y in range(16):
            put(img, x, y, RUST[2])
    for x in range(2, 16, 4):
        put(img, x, 7, COPPER[5])
    return img


def pipe_metal(seed, palette, spots):
    """Pipe stock: a bright highlight row and a dark underside, with weathering spots of another colour."""
    rng = random.Random(seed)
    img = new()
    for y in range(16):
        for x in range(16):
            band = y % 8
            c = palette[[1, 2, 3, 4, 5, 3, 2, 1][band]]
            if rng.random() < 0.1:
                c = rng.choice(spots)
            put(img, x, y, c)
    return img


def hex_nut():
    """A hex-nut fitting seen face on: a six-sided steel nut round a dark bore."""
    img = new(IRON[2])
    for y in range(16):
        for x in range(16):
            dx, dy = x - 7.5, y - 7.5
            # Hexagon with flats top and bottom.
            inside = abs(dy) <= 6.5 and abs(dx) * 0.866 + abs(dy) * 0.5 <= 6.5
            if inside:
                c = STEEL[3] if dy < -2 else STEEL[2] if dy < 3 else STEEL[1]
                if math.hypot(dx, dy) < 2.6:
                    c = IRON[0]
                elif math.hypot(dx, dy) < 3.6:
                    c = STEEL[4]
                put(img, x, y, c)
    return img


def red_paint(seed):
    """Chipped red paint over rust (the reference engine's block)."""
    rng = random.Random(seed)
    img = new()
    for y in range(16):
        for x in range(16):
            c = RED[2] if rng.random() < 0.75 else RED[3]
            edge = min(x, y, 15 - x, 15 - y)
            if edge == 0:
                c = RED[1]
            if (edge <= 1 and rng.random() < 0.3) or rng.random() < 0.05:
                c = RUST[rng.randrange(2, 5)]
            put(img, x, y, c)
    for x, y in ((2, 2), (13, 2), (2, 13), (13, 13)):
        put(img, x, y, IRON[1])
        put(img, x + 1, y, RED[4])
    return img


def rivet_band(seed):
    """A dark iron band with a row of domed rivets: flanges and hoops."""
    rng = random.Random(seed)
    img = new()
    for y in range(16):
        for x in range(16):
            put(img, x, y, IRON[3] if rng.random() < 0.8 else RUST[2])
    for x in range(16):
        put(img, x, 0, IRON[5])
        put(img, x, 15, IRON[1])
    for x in range(1, 16, 4):
        for y in range(1, 16, 4):
            put(img, x, y, IRON[5])
            put(img, x + 1, y, IRON[4])
            put(img, x, y + 1, IRON[2])
            put(img, x + 1, y + 1, IRON[1])
    return img


def skid(seed):
    """Dark skid iron with weld beads and grease."""
    rng = random.Random(seed)
    img = new()
    for y in range(16):
        for x in range(16):
            c = IRON[2] if rng.random() < 0.8 else IRON[1]
            if y in (3, 12):
                c = IRON[4] if x % 2 else IRON[3]
            put(img, x, y, c)
    for _ in range(5):
        put(img, rng.randrange(16), rng.randrange(16), RUST[2])
    return img


def grate(seed):
    """Rusty floor grating: a diamond-ish grid with dark gaps."""
    rng = random.Random(seed)
    img = new(IRON[0])
    for y in range(16):
        for x in range(16):
            if x % 4 == 0 or y % 4 == 0:
                put(img, x, y, mottle(rng, x, y, RUST, 2, 5))
    return img


def amber(on):
    """A glowing amber conduit window behind cage bars; dim when the machine is idle."""
    img = new()
    base = 3 if on else 1
    for y in range(16):
        for x in range(16):
            glow = 1 - min(1.0, abs(y - 7.5) / 8)
            c = AMBER[min(5, base + int(glow * (3 if on else 1.5)))]
            if x % 5 == 0:
                c = IRON[1]
            if y in (0, 15):
                c = IRON[2]
            put(img, x, y, c)
    return img


def fire_door(on):
    """Riveted firebox door with a slotted draft and a glowing slit."""
    img = new()
    for y in range(16):
        for x in range(16):
            c = RUST[3] if (x + y) % 7 else RUST[2]
            if x in (0, 15) or y in (0, 15):
                c = IRON[1]
            put(img, x, y, c)
    for y in (5, 8, 11):
        for x in range(3, 13):
            put(img, x, y, AMBER[4 if on else 1] if x % 3 else AMBER[5 if on else 2])
            put(img, x, y - 1, IRON[0])
    for x, y in ((2, 2), (13, 2), (2, 13), (13, 13)):
        put(img, x, y, IRON[5])
    put(img, 13, 8, STEEL[4])
    return img


def fan():
    """A turbine fan in its rusty housing, seen face on (the reference's big top-down fan)."""
    img = new(RUST[1])
    for y in range(16):
        for x in range(16):
            dx, dy = x - 7.5, y - 7.5
            r = math.hypot(dx, dy)
            if r > 7.6:
                continue
            if r > 6.5:
                put(img, x, y, RUST[4])
                continue
            angle = math.atan2(dy, dx)
            blade = (angle * 7 / (2 * math.pi) + r * 0.08) % 1.0
            c = STEEL[3] if blade < 0.45 else IRON[1]
            if r < 1.8:
                c = BLUE[3]
            elif r < 2.6:
                c = BLUE[1]
            put(img, x, y, c)
    return img


def coil(seed):
    """Copper windings: tight horizontal turns with a green bloom here and there."""
    rng = random.Random(seed)
    img = new()
    for y in range(16):
        for x in range(16):
            c = COPPER[3] if y % 2 == 0 else COPPER[1]
            if (x + y * 3) % 11 == 0:
                c = COPPER[5]
            if rng.random() < 0.05:
                c = PATINA[3]
            put(img, x, y, c)
    return img


def blue_cap(seed):
    """Blue-painted end caps (the reference engine's pipe ends), worn at the rim."""
    rng = random.Random(seed)
    img = new()
    for y in range(16):
        for x in range(16):
            c = BLUE[2] if rng.random() < 0.8 else BLUE[3]
            if min(x, y, 15 - x, 15 - y) == 0:
                c = BLUE[0]
            if rng.random() < 0.04:
                c = STEEL[2]
            put(img, x, y, c)
    return img


def blade(seed):
    """Brushed saw steel in rings, with a bright edge where the teeth run."""
    rng = random.Random(seed)
    img = new()
    for y in range(16):
        for x in range(16):
            r = math.hypot(x - 7.5, y - 7.5)
            c = STEEL[2 + int(r) % 2]
            if r > 6.6:
                c = STEEL[4] if (x + y) % 2 else IRON[2]
            if r < 1.5:
                c = IRON[1]
            if rng.random() < 0.04:
                c = RUST[3]
            put(img, x, y, c)
    return img


def crt(on):
    """A round-cornered amber phosphor screen with scan lines and a trace."""
    img = new(IRON[1])
    for y in range(2, 14):
        for x in range(2, 14):
            if (x in (2, 13)) and (y in (2, 13)):
                continue
            c = AMBER[1 if on else 0] if y % 2 else AMBER[2 if on else 0]
            put(img, x, y, c)
    if on:
        for x in range(3, 13):
            y = 8 + round(2.5 * math.sin(x * 0.9))
            put(img, x, y, AMBER[5])
        for x in range(3, 8):
            put(img, x, 4, AMBER[4])
    return img


def leaves(seed):
    """Lush hydroponic greens: overlapping leaves with bright veins."""
    rng = random.Random(seed)
    greens = [(26, 64, 22), (38, 92, 30), (56, 124, 40), (86, 160, 56), (130, 196, 84)]
    img = new()
    for y in range(16):
        for x in range(16):
            put(img, x, y, greens[rng.choice([0, 1, 1, 2, 2, 2, 3, 3, 4])])
    return img


def solution(seed, palette):
    """A tank's liquid surface: ripples on a deep colour."""
    rng = random.Random(seed)
    img = new()
    for y in range(16):
        for x in range(16):
            c = palette[1]
            if (x + 2 * y + rng.randrange(3)) % 9 == 0:
                c = palette[3]
            elif rng.random() < 0.2:
                c = palette[2]
            put(img, x, y, c)
    return img


def frost(seed):
    """Frosted pipework: pale ice crusted over patina."""
    rng = random.Random(seed)
    img = new()
    ice = [(150, 186, 196), (186, 214, 222), (220, 236, 240), (246, 250, 252)]
    for y in range(16):
        for x in range(16):
            c = ice[rng.choice([0, 1, 1, 2, 2, 3])]
            if rng.random() < 0.12:
                c = PATINA[3]
            put(img, x, y, c)
    return img


def draw_all():
    save(rust_plate(4400), "dr_rust")
    save(rust_plate(4401, rivets=False), "dr_rust_bare")
    save(patina_plate(4402), "dr_patina")
    save(patina_plate(4403, holes=True), "dr_perforated")
    save(ribbed(4404, PATINA, RUST[4]), "dr_ribbed_patina")
    save(ribbed(4405, RUST, PATINA[2]), "dr_ribbed_rust")
    save(banded_dome(4406), "dr_dome")
    save(pipe_metal(4407, COPPER, [PATINA[3], RUST[3]]), "dr_copper_pipe")
    save(pipe_metal(4408, RUST, [RUST[0], IRON[2]]), "dr_rust_pipe")
    save(hex_nut(), "dr_nut")
    save(red_paint(4409), "dr_red")
    save(rivet_band(4410), "dr_band")
    save(skid(4411), "dr_skid")
    save(grate(4412), "dr_grate")
    save(amber(False), "dr_amber")
    save(amber(True), "dr_amber_on")
    save(fire_door(False), "dr_fire_door")
    save(fire_door(True), "dr_fire_door_on")
    save(fan(), "dr_fan")
    save(coil(4413), "dr_coil")
    save(blue_cap(4414), "dr_blue")
    save(blade(4415), "dr_blade")
    save(crt(False), "dr_crt")
    save(crt(True), "dr_crt_on")
    save(leaves(4416), "dr_leaves")
    save(solution(4417, [(10, 40, 46), (20, 70, 80), (40, 110, 120), (120, 190, 196)]), "dr_wash_water")
    save(solution(4418, [(20, 60, 30), (40, 110, 60), (70, 150, 90), (160, 220, 150)]), "dr_plating_bath")
    save(frost(4419), "dr_frost")

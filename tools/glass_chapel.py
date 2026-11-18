"""The Glass Chapel (docs/features/glass-chapel.md): the Glass Abbot's lair, written as the structure template
data/jugcraft/structure/lair/glass_chapel.nbt, which lair/Lairs places into every instance of the jugcraft:glass_chapel
dimension. Called from tools/lair_data.py (so tools/generate_material_data.py writes it and CI checks it is current).

An abandoned chapel of pale stone, lit through tall windows. North is low z.
- the narthex (NARTHEX): a porch at the chapel's south end, the arrival (ARRIVAL), with the Grey Mist (EXIT) in the south
  door's arch, the way home; a low rood screen (ROOD) with a gap in its middle parts it from the nave;
- the nave (NAVE), the arena: 31 blocks wide and 29 long, its floor worn stone, and north of it the apse (APSE), where
  the Abbot's Lectern (LECTERN) stands under the rose window (ROSE) and the Abbot kneels before it (ABBOT);
- the three Chapel Mirrors (MIRRORS): tall mirrors on turntables east, south and west of the nave's middle, making a
  diamond with the Abbot's place; the floor round each is inlaid with copper lines (LINES) pointing to the Abbot's place
  and the other two mirrors;
- the four pillars (PILLARS) in the nave's corners, clear of every line between the Abbot and the mirrors;
- the lancet windows (LANCETS) down both walls, candles on their sills.
The light is hidden light blocks: the lairs' sky gives none.

The Abbot's beam (part 2) is worked out in the plane of the floor, on the server, from the mirrors' angles alone, by
trace() below; lair/GlassChapel.java does the same and the audit holds the two to each other. A mirror's angle is the
bearing of the way its glass faces (degrees clockwise from north), modulo 180 since a mirror has two faces, in steps of
ANGLE_STEP. Each mirror turns through its STOPS: one angle for each of its two pairs (the angle that sends the beam
from the Abbot to it on to the other mirror, or back) and two that send it into a wall. Only the two mirrors the Abbot
marks turn his beam; the third is dark, and stops it.

Format: tools/lair_layout.py's (26.3's). Deterministic: the same file every run.
"""
import math

import lair_layout
from lair_layout import block, hash01 as _hash

TEMPLATE = "lair/glass_chapel"

SIZE = (35, 24, 46)
PAVE = 2                           # the floor's top layer
STAND = PAVE + 1                   # where anyone in the chapel stands
CENTRE = (17.5, 22.5)              # the chapel's middle (x and z, block corners): the leash and the bounds' middle
# The walls: the nave between the west and east walls (cells WALLS[0] + 1 to WALLS[1] - 1), from its north wall (NAVE_Z[0]
# - 1) to the rood screen; the apse north of it, narrower (APSE_X), back to the north wall (APSE_Z[0] - 1).
WALLS = (1, 33)
NAVE_Z = (9, 37)                   # the nave's rows, inclusive
APSE_X = (9, 25)                   # the apse's columns, inclusive
APSE_Z = (2, 8)                    # the apse's rows, inclusive
APSE_CHAMFER = 3                   # the apse's back corners are cut off this far
WALL_TOP = PAVE + 11               # the walls' top layer; the vault springs from it
VAULT_RISE = 5                     # how far the vault's crown rises over the walls' top
RIB_STEP = 6                       # a rib of chiseled stone across the vault every this many rows
# The rood screen across the nave's south end, ROOD_HEIGHT high, with a gap (ROOD_GAP, its columns) to walk through.
ROOD_Z = 38
ROOD_GAP = (15, 19)
ROOD_HEIGHT = 2
# The narthex south of it, the arrival, and the south door with the Grey Mist: three columns, three high.
NARTHEX_Z = (39, 43)
SOUTH_WALL = 44
ARRIVAL = (17.5, STAND, 41.5, 180.0)  # x, y, z and yaw: in the narthex, facing north up the nave
EXIT = {"x": (16, 18), "z": SOUTH_WALL}
# Where the mist throws back whoever strays: farther than BOUNDS (horizontally) from CENTRE, or below FLOOR.
BOUNDS = 30
FLOOR = 0
# The Abbot's place (x and z, block corners): where he kneels, and where his beam leaves and must come home to. The
# lectern stands north of it, facing him; he kneels facing it (yaw 180), his back to the nave.
ABBOT = (17.5, 8.5)
LECTERN = (17, 7)
WAIT = (17.5, STAND, 8.5, 180.0)
# The rose window in the apse's north wall: its middle (x, y) and radius.
ROSE = (17.5, PAVE + 8.5, 4.2)
# The three mirrors: each one's cell; it stands two blocks high (lower and upper) on the floor.
MIRRORS = {"east": (29, 20), "south": (17, 32), "west": (5, 20)}
MIRROR_PARTS = ("lower", "upper")
ANGLE_STEP = 22.5
# Each mirror's four stops, as steps of ANGLE_STEP (0 to 7): STOPS[name][i], each turning the same way round. A mirror
# stands at REST (its first, a decoy) when an instance is placed and after the Abbot resets; a use turns it to the
# next, round. pair_steps() works out the two that serve its pairs; the audit checks these are them, and that no other
# setting sends the beam home.
STOPS = {"east": (2, 4, 5, 7), "south": (3, 5, 7, 1), "west": (0, 3, 4, 6)}
REST = 0
# The four pillars: each PILLAR cells square from its north-west cell, from the floor to the vault.
PILLARS = ((8, 10), (25, 10), (8, 29), (25, 29))
PILLAR = 2
# The lancets: windows WINDOW_WIDTH wide in both side walls, from the rows in LANCETS, between WINDOW_LOW and
# WINDOW_HIGH over the floor (their tops pointed); under each, a sill standing in from the wall over everyone's head, a
# lit candle on it.
LANCETS = (12, 17, 23, 28, 33)
WINDOW_WIDTH = 2
WINDOW_LOW = 3
WINDOW_HIGH = 9
# The copper lines in the floor round each mirror: from LINE_NEAR to LINE_FAR blocks from it, toward the Abbot's place and
# each other mirror.
LINE_NEAR = 2
LINE_FAR = 4
# The beam (part 2), in the floor's plane: it strikes a mirror (or the Abbot) whose middle it passes within HIT of; a
# pillar stops it; it fades at the rood screen and at the walls (REACH: the box it is worked out in, x0, z0, x1, z1);
# it turns at most BOUNCES times. CLEARANCE: how far every line between the Abbot's place and a mirror keeps from a
# pillar's sides and from the other mirror (the audit's).
HIT = 0.75
REACH = (2.0, 2.0, 33.0, 38.0)
BOUNCES = 4
CLEARANCE = 2.0
# Fair to a player alone (the audit walks it): from every open cell of the nave a sprinting player (FAIR_SPRINT blocks a
# second) comes within FAIR_REACH of one marked mirror, turns it, and comes within FAIR_REACH of the other in FAIR_SECONDS:
# the plan's 10 seconds from the Abbot's marking to his gathering the sun, and the 3 seconds he gathers it.
FAIR_SECONDS = 13
FAIR_SPRINT = 5.6
FAIR_REACH = 3.0
# The hidden lights: a lattice LIGHT_STEP apart LIGHT_HEIGHT over the floor, staggered row to row; NAVE_LIGHT is the least
# block light they leave on any cell where anyone stands in the nave, the apse or the narthex (the audit floods it).
LIGHT = 15
LIGHT_STEP = 5
LIGHT_HEIGHT = 3
NAVE_LIGHT = 8


# ---------------------------------------------------------------- states

FLOOR_BLOCKS = ((0.40, block("minecraft:polished_andesite")), (0.62, block("minecraft:stone_bricks")),
                (0.80, block("minecraft:andesite")), (0.92, block("minecraft:cracked_stone_bricks")),
                (1.0, block("minecraft:mossy_stone_bricks")))
WALL_BLOCKS = ((0.62, block("minecraft:stone_bricks")), (0.82, block("minecraft:cracked_stone_bricks")),
               (1.0, block("minecraft:mossy_stone_bricks")))
BRICKS = block("minecraft:stone_bricks")
CHISELED = block("minecraft:chiseled_stone_bricks")
POLISHED = block("minecraft:polished_andesite")
CALCITE = block("minecraft:calcite")
FOUNDATION = block("minecraft:stone")
LINE = block("minecraft:waxed_cut_copper")
COBWEB = block("minecraft:cobweb")
EXIT_MIST = block("jugcraft:lair_exit", axis="x")
LANCET_GLASS = block("jugcraft:lancet_glass")
ROSE_GLASS = block("jugcraft:rose_glass")


def mirror(name, part):
    return block("jugcraft:chapel_mirror", half=part, mirror=name, stop=REST, marked=False, veiled=False)


def lectern():
    return block("jugcraft:abbot_lectern", facing="south")


def candle():
    return block("minecraft:candle", candles=1, lit=True, waterlogged=False)


def slab(kind):
    return block("minecraft:stone_brick_slab", type=kind, waterlogged=False)


def light(level):
    return block("minecraft:light", level=level, waterlogged=False)


# ---------------------------------------------------------------- the beam's geometry

def pair_name(a, b):
    """A pair of mirrors, named the same either way round."""
    return "+".join(sorted((a, b)))


PAIRS = tuple(pair_name(a, b) for a, b in (("east", "south"), ("east", "west"), ("south", "west")))


def middle(name):
    """A mirror's middle (x and z)."""
    x, z = MIRRORS[name]
    return (x + 0.5, z + 0.5)


def bearing_of(dx, dz):
    """The bearing of (dx, dz), degrees clockwise from north (low z)."""
    return math.degrees(math.atan2(dx, -dz)) % 360.0


def facing(step):
    """The unit vector (x, z) a mirror at angle step faces: its glass's normal."""
    a = math.radians(step * ANGLE_STEP)
    return (math.sin(a), -math.cos(a))


def pair_step(name, other):
    """The angle step (0 to 7) at which mirror name sends the beam from the Abbot's place on to mirror other (or back):
    its glass faces half way between the two."""
    x, z = middle(name)
    ox, oz = middle(other)
    to_abbot = bearing_of(ABBOT[0] - x, ABBOT[1] - z)
    to_other = bearing_of(ox - x, oz - z)
    half = (to_abbot + to_other) / 2.0
    if abs(to_abbot - to_other) > 180.0:
        half += 180.0
    step = (half % 180.0) / ANGLE_STEP
    if abs(step - round(step)) > 1e-6:
        raise ValueError(f"{name} to {other}: {half % 180.0} degrees is no step of {ANGLE_STEP}")
    return int(round(step)) % 8


def pair_steps():
    """{mirror: {pair: step}}: each mirror's angle for each of its two pairs."""
    return {name: {pair_name(name, other): pair_step(name, other) for other in MIRRORS if other != name} for name in MIRRORS}


def pillar_box(corner):
    x, z = corner
    return (x, z, x + PILLAR, z + PILLAR)


def _box_hit(px, pz, dx, dz, box):
    """How far along the ray (px, pz) + t (dx, dz) it enters the box (x0, z0, x1, z1), or None."""
    x0, z0, x1, z1 = box
    lo, hi = 0.0, math.inf
    for p, d, a, b in ((px, dx, x0, x1), (pz, dz, z0, z1)):
        if abs(d) < 1e-12:
            if p < a or p > b:
                return None
        else:
            t0, t1 = (a - p) / d, (b - p) / d
            lo, hi = max(lo, min(t0, t1)), min(hi, max(t0, t1))
    return lo if lo <= hi and lo > 1e-9 else None


def _exit(px, pz, dx, dz):
    """How far along the ray it leaves the beam's reach."""
    x0, z0, x1, z1 = REACH
    out = math.inf
    for p, d, a, b in ((px, dx, x0, x1), (pz, dz, z0, z1)):
        if d > 1e-12:
            out = min(out, (b - p) / d)
        elif d < -1e-12:
            out = min(out, (a - p) / d)
    return out


def _passes(px, pz, dx, dz, cx, cz):
    """How far along the ray it passes within HIT of (cx, cz), ahead of its start, or None."""
    t = (cx - px) * dx + (cz - pz) * dz
    if t <= 1e-9:
        return None
    if math.hypot(px + t * dx - cx, pz + t * dz - cz) > HIT:
        return None
    return t


def trace(stops, marked, first):
    """The beam with the mirrors at stops ({mirror: stop index}) and the mirrors in marked lit, aimed from the Abbot's
    place at the middle of mirror first: (its points, the mirrors it struck in order, whether it came home). It turns at
    each marked mirror's middle, once; an unmarked mirror is dark and stops it, as a mirror it has struck before does."""
    px, pz = ABBOT
    fx, fz = middle(first)
    length = math.hypot(fx - px, fz - pz)
    dx, dz = (fx - px) / length, (fz - pz) / length
    points, struck, leaving = [(px, pz)], [], None
    for _ in range(BOUNCES + 1):
        best, what = _exit(px, pz, dx, dz), None
        for name in MIRRORS:
            if name == leaving:
                continue
            t = _passes(px, pz, dx, dz, *middle(name))
            if t is not None and t < best:
                best, what = t, name
        if struck:
            t = _passes(px, pz, dx, dz, *ABBOT)
            if t is not None and t < best:
                best, what = t, "abbot"
        for corner in PILLARS:
            t = _box_hit(px, pz, dx, dz, pillar_box(corner))
            if t is not None and t < best:
                best, what = t, "pillar"
        if what in MIRRORS and what in marked and what not in struck:
            px, pz = middle(what)
            points.append((px, pz))
            struck.append(what)
            nx, nz = facing(STOPS[what][stops[what]])
            dot = dx * nx + dz * nz
            dx, dz = dx - 2 * dot * nx, dz - 2 * dot * nz
            leaving = what
            continue
        if what in MIRRORS:
            struck.append(what)
            points.append(middle(what))
            return points, struck, False
        points.append((px + best * dx, pz + best * dz) if what != "abbot" else ABBOT)
        return points, struck, what == "abbot"
    return points, struck, False


def comes_home(stops, pair):
    """Whether the beam, with the mirrors of pair marked and aimed at either of them, strikes both and comes home with
    the mirrors at stops."""
    a, b = pair.split("+")
    for first in (a, b):
        _, struck, home = trace(stops, (a, b), first)
        if home and a in struck and b in struck:
            return True
    return False


def segment_distance(p, a, b):
    """How far point p is from the segment a-b."""
    dx, dz = b[0] - a[0], b[1] - a[1]
    t = max(0.0, min(1.0, ((p[0] - a[0]) * dx + (p[1] - a[1]) * dz) / (dx * dx + dz * dz)))
    return math.hypot(p[0] - a[0] - t * dx, p[1] - a[1] - t * dz)


def routes():
    """Every line a beam sent home runs along: the Abbot's place to each mirror, and each mirror to each other."""
    points = {"abbot": ABBOT, **{name: middle(name) for name in MIRRORS}}
    names = list(points)
    return [(names[i], names[j], points[names[i]], points[names[j]]) for i in range(len(names)) for j in range(i + 1, len(names))]


# ---------------------------------------------------------------- shapes

def in_nave(x, z):
    return WALLS[0] < x < WALLS[1] and NAVE_Z[0] <= z <= NAVE_Z[1]


def in_apse(x, z):
    if not (APSE_X[0] <= x <= APSE_X[1] and APSE_Z[0] <= z <= APSE_Z[1]):
        return False
    back = APSE_CHAMFER - (z - APSE_Z[0])
    return not (back > 0 and (x - APSE_X[0] < back or APSE_X[1] - x < back))


def in_narthex(x, z):
    return WALLS[0] < x < WALLS[1] and NARTHEX_Z[0] <= z <= NARTHEX_Z[1]


def inside(x, z):
    """Whether (x, z) is inside the chapel: the nave, the apse, the rood screen's row or the narthex."""
    return in_nave(x, z) or in_apse(x, z) or in_narthex(x, z) or (WALLS[0] < x < WALLS[1] and z == ROOD_Z)


def in_pillar(x, z):
    return any(cx <= x < cx + PILLAR and cz <= z < cz + PILLAR for cx, cz in PILLARS)


def vault_top(x):
    """The vault's underside over column x: the walls' top at the walls, rising to WALL_TOP + VAULT_RISE at the middle."""
    half = (WALLS[1] - WALLS[0]) / 2.0
    across = (x + 0.5 - CENTRE[0]) / half
    return WALL_TOP + round(VAULT_RISE * math.sqrt(max(0.0, 1.0 - across * across)))


def line_cells():
    """The copper lines in the floor: {(x, z)} along each mirror's ways to the Abbot's place and the other mirrors."""
    out = set()
    for name in MIRRORS:
        x, z = middle(name)
        targets = [ABBOT] + [middle(other) for other in MIRRORS if other != name]
        for tx, tz in targets:
            length = math.hypot(tx - x, tz - z)
            dx, dz = (tx - x) / length, (tz - z) / length
            step = max(abs(dx), abs(dz))
            for i in range(LINE_NEAR, LINE_FAR + 1):
                cx, cz = math.floor(x + dx / step * i), math.floor(z + dz / step * i)
                out.add((cx, cz))
    return out


def exit_cells():
    """Where the Grey Mist is (for the audit and the tests)."""
    (x0, x1), z = EXIT["x"], EXIT["z"]
    return [(x, y, z) for y in range(STAND, STAND + 3) for x in range(x0, x1 + 1)]


def lancet_cells():
    """The lancets' glass: {(x, y, z)} in both side walls, each pointed at its top."""
    out = set()
    for z0 in LANCETS:
        for x in WALLS:
            for z in range(z0, z0 + WINDOW_WIDTH):
                for y in range(PAVE + WINDOW_LOW, PAVE + WINDOW_HIGH + 1):
                    out.add((x, y, z))
            out.add((x, PAVE + WINDOW_HIGH + 1, z0 + (WINDOW_WIDTH - 1) // 2))
    return out


def rose_cells():
    """The rose window's glass in the apse's north wall: {(x, y)}."""
    cx, cy, r = ROSE
    return {(x, y) for x in range(APSE_X[0], APSE_X[1] + 1) for y in range(PAVE + 1, WALL_TOP + 1)
            if math.hypot(x + 0.5 - cx, y + 0.5 - cy) <= r}


def pick(table, x, y, z, salt):
    h = _hash(x, y, z, salt)
    for limit, state in table:
        if h < limit:
            return state
    return table[-1][1]


# ---------------------------------------------------------------- the build

def build():
    blocks = {}

    def put(x, y, z, state):
        if 0 <= x < SIZE[0] and 0 <= y < SIZE[1] and 0 <= z < SIZE[2]:
            blocks[(x, y, z)] = state

    foundation_and_floor(put)
    walls(put)
    vault(put)
    windows(put)
    rood_screen(put)
    pillars(put)
    mirrors_and_lines(put)
    apse(put)
    exit_mist(put)
    cobwebs(put, blocks)
    lights(put, blocks)
    return hollow(blocks)


def foundation_and_floor(put):
    """The foundation under the whole chapel and its worn floor, in patches of polished stone, bricks and moss."""
    for x in range(SIZE[0]):
        for z in range(SIZE[2]):
            if not (WALLS[0] <= x <= WALLS[1] and APSE_Z[0] - 1 <= z <= SOUTH_WALL):
                continue
            for y in range(PAVE):
                put(x, y, z, FOUNDATION)
            put(x, PAVE, z, pick(FLOOR_BLOCKS, x // 2, 0, z // 2, 7) if inside(x, z) else BRICKS)


def walls(put):
    """The walls round the nave, the apse and the narthex, up to the vault's spring."""
    for x in range(WALLS[0], WALLS[1] + 1):
        for z in range(APSE_Z[0] - 1, SOUTH_WALL + 1):
            if inside(x, z):
                continue
            near = any(inside(x + dx, z + dz) for dx in (-1, 0, 1) for dz in (-1, 0, 1))
            if not near:
                continue
            for y in range(STAND, WALL_TOP + 1):
                put(x, y, z, pick(WALL_BLOCKS, x, y, z, 11))


def vault(put):
    """The barrel vault over the nave and the narthex, ribbed every RIB_STEP rows; the apse's lower, flat."""
    for x in range(WALLS[0], WALLS[1] + 1):
        for z in range(NAVE_Z[0] - 1, SOUTH_WALL + 1):
            top = vault_top(x)
            rib = (z - NAVE_Z[0]) % RIB_STEP == 0
            for y in range(WALL_TOP, top + 1):
                if y == top or x in WALLS:
                    put(x, y, z, CHISELED if rib else pick(WALL_BLOCKS, x, y, z, 13))
    for x in range(APSE_X[0] - 1, APSE_X[1] + 2):
        for z in range(APSE_Z[0] - 1, NAVE_Z[0]):
            put(x, WALL_TOP, z, pick(WALL_BLOCKS, x, WALL_TOP, z, 17))


def sill_cells():
    """The lancets' sills, standing in from the walls: {(x, y, z)}, and the candles on them."""
    sills, candles = set(), set()
    for z0 in LANCETS:
        for x in (WALLS[0] + 1, WALLS[1] - 1):
            for z in range(z0, z0 + WINDOW_WIDTH):
                sills.add((x, PAVE + WINDOW_LOW, z))
            candles.add((x, PAVE + WINDOW_LOW + 1, z0))
    return sills, candles


def windows(put):
    """The lancets down both walls, their sills and candles, and the rose window in the apse's north wall."""
    for x, y, z in lancet_cells():
        put(x, y, z, LANCET_GLASS)
    sills, candles = sill_cells()
    for pos in sills:
        put(*pos, CHISELED)
    for pos in candles:
        put(*pos, candle())
    for x, y in rose_cells():
        put(x, y, APSE_Z[0] - 1, ROSE_GLASS)


def rood_screen(put):
    """The rood screen across the nave's south end: a low wall of bricks, capped, with a gap in its middle."""
    for x in range(WALLS[0] + 1, WALLS[1]):
        if ROOD_GAP[0] <= x <= ROOD_GAP[1]:
            continue
        for y in range(STAND, STAND + ROOD_HEIGHT):
            put(x, y, ROOD_Z, CHISELED if x % 4 == 0 else BRICKS)
        put(x, STAND + ROOD_HEIGHT, ROOD_Z, slab("bottom"))


def pillars(put):
    """The four pillars, polished from floor to vault, a calcite band at head height."""
    for cx, cz in PILLARS:
        for x in range(cx, cx + PILLAR):
            for z in range(cz, cz + PILLAR):
                for y in range(STAND, vault_top(x) + 1):
                    put(x, y, z, CALCITE if y == STAND + 2 else CHISELED if y == STAND else POLISHED)


def mirrors_and_lines(put):
    """The three mirrors at rest, and the copper lines in the floor round them."""
    for x, z in line_cells():
        put(x, PAVE, z, LINE)
    for name, (x, z) in MIRRORS.items():
        for i, part in enumerate(MIRROR_PARTS):
            put(x, STAND + i, z, mirror(name, part))


def apse(put):
    """The Abbot's Lectern before the rose window, a step of polished stone under it."""
    x, z = LECTERN
    put(x, PAVE, z, POLISHED)
    put(x, STAND, z, lectern())


def exit_mist(put):
    """The Grey Mist in the south door's arch: the way home."""
    for x, y, z in exit_cells():
        put(x, y, z, EXIT_MIST)
    (x0, x1), z = EXIT["x"], EXIT["z"]
    for x in range(x0 - 1, x1 + 2):
        put(x, STAND + 3, z, CHISELED)


def cobwebs(put, blocks):
    """Cobwebs in the vault's corners, out of reach."""
    for x in (WALLS[0] + 1, WALLS[1] - 1):
        for z in range(NAVE_Z[0], NARTHEX_Z[1] + 1):
            y = vault_top(x) - 1
            if _hash(x, z, 23) < 0.3 and (x, y, z) not in blocks:
                put(x, y, z, COBWEB)


def lights(put, blocks):
    """The hidden lights: a staggered lattice over the nave, the apse and the narthex."""
    y = STAND + LIGHT_HEIGHT
    for row, z in enumerate(range(APSE_Z[0] + 1, NARTHEX_Z[1] + 1, LIGHT_STEP // 2)):
        for x in range(WALLS[0] + 2 + (LIGHT_STEP // 2 if row % 2 else 0), WALLS[1] - 1, LIGHT_STEP):
            if inside(x, z) and (x, y, z) not in blocks:
                put(x, y, z, light(LIGHT))


def hollow(blocks):
    """Leaves out blocks nobody can see: stone whose six neighbours are all stone."""
    opaque = {FOUNDATION, BRICKS, CHISELED, POLISHED, CALCITE, *(state for _, state in FLOOR_BLOCKS),
              *(state for _, state in WALL_BLOCKS)}
    out = {}
    for (x, y, z), state in blocks.items():
        if state in opaque and all(blocks.get(n) in opaque for n in (
                (x + 1, y, z), (x - 1, y, z), (x, y + 1, z), (x, y - 1, z), (x, y, z + 1), (x, y, z - 1))):
            continue
        out[(x, y, z)] = state
    return out


def template():
    return lair_layout.template(build(), SIZE)


def write(path):
    lair_layout.write(path, template())


if __name__ == "__main__":
    print(pair_steps())
    for pair in PAIRS:
        print(pair, comes_home({name: REST for name in MIRRORS}, pair))

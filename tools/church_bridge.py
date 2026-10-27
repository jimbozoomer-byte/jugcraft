"""The Church Bridge (docs/features/church-bridge.md): the Headless Horseman's lair out of the Halloween season, written as
the structure template data/jugcraft/structure/lair/church_bridge.nbt, which lair/Lairs places into every instance of the
jugcraft:church_bridge dimension. Called from tools/lair_data.py (so tools/generate_material_data.py writes it and CI checks
it is current).

A wooded hollow at night under a full moon. North is low z.
- the hollow, the arena (HOLLOW): a clearing about 41 blocks across round its middle (CENTRE), its floor podzol, moss,
  coarse and rooted earth in patches; the wood road (ROAD) runs north through it between split-rail fences (RAILS) with
  gaps in them; two fallen trunks (FALLEN) give low cover;
- the braziers (BRAZIERS): eight of the lairs' Soul Braziers (lair_brazier, a fixture) in a ring round the middle, lit,
  none on the road;
- the brook (BROOK): from west to east across the hollow's north, water flush with the ground and two deep, walled in at
  both ends by the wood; the covered bridge (BRIDGE) carries the road over it: a deck of spruce, plank walls on stripped
  spruce posts with windows, and a shingled roof of dark oak;
- the church (CHURCH): across the brook, fieldstone walls, a steep dark oak roof and a white wooden belfry (BELFRY) over
  its door with a bell in it; its churchyard (YARD) of old stone markers (MARKERS) runs down to the brook inside a low
  cobblestone wall, the road through a gap in it to the belfry's door;
- the wood: everything else; old dark oak trunks with a thicket of Moonwood Leaves (the Moonlit Ring's lair-only
  moonwood_leaves) between them up to THICKET_TOP, too thick to pass, and a canopy over it from CANOPY_LOW to CANOPY_HIGH;
- the wood road's south end (ARRIVAL), and at its end Grey Mist (EXIT): the way home;
- where the Horseman waits (WAIT, part 2): mounted at the bridge's foot, facing south over the hollow.
The moon is not in the template: Lairs places it, MOON_RADIUS across, north beyond the church (MOON). The light is hidden
light blocks over the hollow, the road, the bridge and the yard, and the braziers.

Format: tools/lair_layout.py's (26.3's). Jugcraft blocks' states are checked against their blockstate files as the
template is written; vanilla blocks use only properties they have had for years. Nothing in it changes by itself once
placed: no vanilla leaves (they would decay), no grass blocks, no farmland; the brook's water is all still sources,
walled in. Deterministic: the same file every run.
"""
import math

import lair_layout
from lair_layout import block, hash01 as _hash

TEMPLATE = "lair/church_bridge"

SIZE = (72, 26, 86)
SOIL = 4                           # the floor's top layer
STAND = SOIL + 1                   # where anyone on the floor stands
CENTRE = (36.5, 50.5)              # the hollow's middle: the middle of cell (36, 50)
HOLLOW = 20.5                      # the hollow's radius (41 across), ragged by up to HOLLOW_RAG into the wood
HOLLOW_RAG = 1.5
# The wood road: its cells' columns (x, inclusive), three wide, from the bridge's south end (ROAD_NORTH) to the Grey Mist
# (ROAD_SOUTH); the open verges either side of it south of the hollow (VERGE, inclusive).
ROAD = (35, 37)
ROAD_NORTH = 29
ROAD_SOUTH = 82                    # the Grey Mist's row: the road's end
VERGE = (33, 39)
# The split-rail fences beside the road through the hollow: on the columns RAIL_X, from RAIL_NORTH to RAIL_SOUTH, a gap of
# RAIL_GAP every RAIL_EVERY cells.
RAIL_X = (34, 38)
RAIL_NORTH = 33
RAIL_SOUTH = 68
RAIL_EVERY = 7
RAIL_GAP = 2
# The braziers: on a ring BRAZIER_RING from the middle, one every 45 degrees from BRAZIER_FIRST (clockwise from north), so
# none stands on the road. In ring order (tools/lairs.py BRAZIER_NAMES).
BRAZIER_RING = 13.0
BRAZIER_FIRST = 22.5
BRAZIER_NAMES = ("north_east", "east_north", "east_south", "south_east", "south_west", "west_south", "west_north",
                 "north_west")
# The fallen trunks: (x0, x1, z), lying east and west.
FALLEN = ((27, 31, 41), (42, 46, 66))
# The brook: its rows (z, inclusive), its columns (x, inclusive; the wood walls it in beyond them), BROOK_DEPTH deep from
# the floor's level down, on a bed of gravel and mud.
BROOK = {"z": (24, 26), "x": (3, 68)}
BROOK_DEPTH = 2
# The covered bridge: its rows (z, inclusive), its deck's columns (the road's) and its walls' (BRIDGE_WALLS), posts every
# BRIDGE_POST rows from its south end, windows WINDOW_ROWS; walls BRIDGE_WALL_HEIGHT high, the roof's eaves on top.
BRIDGE = {"z": (22, 28)}
BRIDGE_WALLS = (34, 38)
BRIDGE_POSTS = (22, 25, 28)
WINDOW_ROWS = (23, 24, 26, 27)
BRIDGE_WALL_HEIGHT = 4
# The bank meadow between the hollow and the brook (open): (x0, x1, z0, z1), inclusive.
BANK = (24, 48, 27, 30)
# The churchyard: its cells (inclusive), walled round its west, east and north sides and along the brook (YARD_WALL_Z),
# with a gap for the road; the church inside it, and its belfry over the door at its south end.
YARD = {"x": (19, 53), "z": (3, 21)}
YARD_WALL_Z = 21
CHURCH = {"x": (29, 43), "z": (5, 15)}
CHURCH_WALL = 5                    # the walls' height above the floor
BELFRY = {"x": (33, 39), "z": (15, 19)}
BELFRY_TOP = STAND + 14            # the belfry's roof's peak
BELL_Y = STAND + 9                 # where the bell hangs, in the open chamber
DOOR = {"x": (35, 37), "z": 19}    # the belfry's door, open, three wide and three high, facing south; an arch
# the same through its north wall (the church's south wall) into the church
# The stone markers in the yard: every MARKER_STEP cells across and MARKER_ROW_STEP rows, in the yard's west and east
# parts and the strip south of the church, never on the road.
MARKER_STEP = 2
MARKER_ROW_STEP = 3
# The wood: trunks where the hash says so (TRUNK_CHANCE), the thicket up to THICKET_TOP, the canopy from CANOPY_LOW to
# CANOPY_HIGH over the wood and over the hollow's rim (within CANOPY_REACH of its edge).
TRUNK_CHANCE = 0.11
THICKET_TOP = STAND + 3
CANOPY_LOW = STAND + 9
CANOPY_HIGH = STAND + 12
CANOPY_REACH = 3.0
# The arrival and the way home: the road's south end. ARRIVAL: x, y, z and yaw (facing north up the road).
ARRIVAL = (36.5, STAND, 77.5, 180.0)
EXIT = {"x": (35, 37), "z": 82}    # the Grey Mist's columns and row, three blocks high from the road
# Where the Horseman waits (part 2): mounted at the bridge's south foot, facing south over the hollow. x, y, z and yaw.
WAIT = (36.5, STAND, 31.5, 0.0)
# The moon's centre and radius, beyond the template's north edge (Lairs places it; it faces south).
MOON = (36, 30, -28)
MOON_RADIUS = 13
# Where the mist throws back whoever strays: farther than BOUNDS (horizontally) from CENTRE, or below FLOOR.
BOUNDS = 50
FLOOR = 1
# The hidden lights: a lattice LIGHT_STEP apart LIGHT_HEIGHT over the hollow, the road, the bridge and the yard.
LIGHT = 11
LIGHT_STEP = 6
LIGHT_HEIGHT = 5


# ---------------------------------------------------------------- states

FLOOR_BLOCKS = ((0.38, block("minecraft:podzol", snowy=False)), (0.62, block("minecraft:coarse_dirt")),
                (0.82, block("minecraft:moss_block")), (1.0, block("minecraft:rooted_dirt")))
DIRT = block("minecraft:dirt")
PATH = block("minecraft:dirt_path")
GRAVEL = block("minecraft:gravel")
MUD = block("minecraft:mud")
WATER = block("minecraft:water", level=0)
LEAVES = block("jugcraft:moonwood_leaves")
PLANKS = block("minecraft:spruce_planks")
ROOF_PLANKS = block("minecraft:dark_oak_planks")
ROOF_SLAB = block("minecraft:dark_oak_slab", type="bottom", waterlogged=False)
FIELDSTONE = (block("minecraft:cobblestone"), block("minecraft:mossy_cobblestone"), block("minecraft:stone"),
              block("minecraft:andesite"))
WHITE = block("minecraft:birch_planks")
WHITE_TRIM = block("minecraft:calcite")
CHURCH_FLOOR = block("minecraft:spruce_planks")
EXIT_MIST = block("jugcraft:lair_exit", axis="x")
BRAZIER = block("jugcraft:lair_brazier", lit=True)
BELL = block("minecraft:bell", attachment="ceiling", facing="south", powered=False)
MARKERS = (block("minecraft:mossy_stone_brick_wall", east="none", north="none", south="none", west="none", up=True,
                 waterlogged=False),
           block("minecraft:stone_brick_wall", east="none", north="none", south="none", west="none", up=True,
                 waterlogged=False),
           block("minecraft:andesite_wall", east="none", north="none", south="none", west="none", up=True,
                 waterlogged=False))


def log(kind, axis):
    return block(f"minecraft:{kind}", axis=axis)


def fence(**sides):
    props = {side: sides.get(side, False) for side in ("east", "north", "south", "west")}
    return block("minecraft:spruce_fence", waterlogged=False, **props)


def wall(**sides):
    """A cobblestone wall: low toward the sides given, a post where it turns or ends."""
    props = {side: "low" if sides.get(side) else "none" for side in ("east", "north", "south", "west")}
    straight = (sides.get("east") and sides.get("west") and not sides.get("north") and not sides.get("south")) or \
               (sides.get("north") and sides.get("south") and not sides.get("east") and not sides.get("west"))
    return block("minecraft:cobblestone_wall", up=not straight, waterlogged=False, **props)


def stair(kind, facing, half="bottom"):
    return block(f"minecraft:{kind}_stairs", facing=facing, half=half, shape="straight", waterlogged=False)


def light(level):
    return block("minecraft:light", level=level, waterlogged=False)


# ---------------------------------------------------------------- shapes

def distance(x, z):
    """How far the middle of cell (x, z) is from the hollow's middle."""
    return math.hypot(x + 0.5 - CENTRE[0], z + 0.5 - CENTRE[1])


def in_box(box, x, z):
    return box["x"][0] <= x <= box["x"][1] and box["z"][0] <= z <= box["z"][1]


def in_hollow(x, z):
    """Whether cell (x, z) is in the hollow: within HOLLOW of its middle, ragged at the edge, never north of the bank."""
    return z > BANK[3] - 1 and distance(x, z) <= HOLLOW + HOLLOW_RAG * (_hash(x, z, 3) - 0.5)


def on_road(x, z):
    return ROAD[0] <= x <= ROAD[1] and ROAD_NORTH <= z <= ROAD_SOUTH


def in_verge(x, z):
    """The open verges beside the road south of the hollow, up to the mist (the wood closes in round it)."""
    return VERGE[0] <= x <= VERGE[1] and ROAD_NORTH <= z < ROAD_SOUTH


def in_bank(x, z):
    return BANK[0] <= x <= BANK[1] and BANK[2] <= z <= BANK[3]


def in_brook(x, z):
    return BROOK["z"][0] <= z <= BROOK["z"][1] and BROOK["x"][0] <= x <= BROOK["x"][1]


def on_bridge(x, z):
    return BRIDGE_WALLS[0] <= x <= BRIDGE_WALLS[1] and BRIDGE["z"][0] <= z <= BRIDGE["z"][1]


def in_yard(x, z):
    return in_box(YARD, x, z)


def is_open(x, z):
    """Whether cell (x, z) is open ground (not the wood): the hollow, the road and its verges, the bank, the bridge and
    the yard."""
    return in_hollow(x, z) or on_road(x, z) or in_verge(x, z) or in_bank(x, z) or on_bridge(x, z) or in_yard(x, z)


def braziers():
    """The braziers' cells, in ring order: {name: (x, z)}."""
    out = {}
    for i, name in enumerate(BRAZIER_NAMES):
        angle = math.radians(BRAZIER_FIRST + 45.0 * i)
        out[name] = (math.floor(CENTRE[0] + math.sin(angle) * BRAZIER_RING),
                     math.floor(CENTRE[1] - math.cos(angle) * BRAZIER_RING))
    return out


def rail_cells():
    """The split-rail fences' cells beside the road through the hollow, with their gaps left out."""
    cells = set()
    for x in RAIL_X:
        for z in range(RAIL_NORTH, RAIL_SOUTH + 1):
            if (z - RAIL_NORTH) % RAIL_EVERY < RAIL_EVERY - RAIL_GAP:
                cells.add((x, z))
    return cells


def church_cells():
    """The church's footprint, walls and all."""
    return {(x, z) for x in range(CHURCH["x"][0], CHURCH["x"][1] + 1) for z in range(CHURCH["z"][0], CHURCH["z"][1] + 1)}


def belfry_cells():
    return {(x, z) for x in range(BELFRY["x"][0], BELFRY["x"][1] + 1) for z in range(BELFRY["z"][0], BELFRY["z"][1] + 1)}


def yard_wall_cells():
    """The churchyard's low wall: its west, east and north sides and along the brook, a gap for the road."""
    (x0, x1), (z0, z1) = YARD["x"], YARD["z"]
    cells = set()
    for x in range(x0, x1 + 1):
        cells.add((x, z0))
        if not ROAD[0] <= x <= ROAD[1]:
            cells.add((x, YARD_WALL_Z))
    for z in range(z0, YARD_WALL_Z + 1):
        cells.add((x0, z))
        cells.add((x1, z))
    return cells


def marker_cells():
    """The stone markers in the yard, between its wall and the church, off the road and the belfry's approach."""
    cells = []
    (x0, x1), (z0, z1) = YARD["x"], YARD["z"]
    taken = church_cells() | belfry_cells() | yard_wall_cells()
    for z in range(z0 + 2, YARD_WALL_Z - 1, MARKER_ROW_STEP):
        for x in range(x0 + 2, x1 - 1, MARKER_STEP):
            near = any((x + dx, z + dz) in taken for dx in (-1, 0, 1) for dz in (-1, 0, 1))
            if near or VERGE[0] - 1 <= x <= VERGE[1] + 1:
                continue
            cells.append((x, z))
    return cells


def exit_cells():
    """Where the Grey Mist is (for the audit and the tests)."""
    (x0, x1), z = EXIT["x"], EXIT["z"]
    return [(x, y, z) for y in range(STAND, STAND + 3) for x in range(x0, x1 + 1)]


def patches(x, z, scale, salt):
    """Smooth 0..1 noise: hashed corners SCALE cells apart, blended, so the floor lies in patches, not speckles."""
    gx, gz = x / scale, z / scale
    x0, z0 = math.floor(gx), math.floor(gz)
    fx, fz = gx - x0, gz - z0
    fx, fz = fx * fx * (3 - 2 * fx), fz * fz * (3 - 2 * fz)
    a, b = _hash(x0, z0, salt), _hash(x0 + 1, z0, salt)
    c, d = _hash(x0, z0 + 1, salt), _hash(x0 + 1, z0 + 1, salt)
    return (a * (1 - fx) + b * fx) * (1 - fz) + (c * (1 - fx) + d * fx) * fz


def floor_block(x, z):
    h = 0.7 * patches(x, z, 5, 41) + 0.3 * _hash(x, z, 42)
    for limit, state in FLOOR_BLOCKS:
        if h < limit:
            return state
    return FLOOR_BLOCKS[-1][1]


def fieldstone(x, y, z):
    return FIELDSTONE[int(_hash(x, y, z, 47) * len(FIELDSTONE))]


# ---------------------------------------------------------------- the build

def build():
    blocks = {}

    def put(x, y, z, state):
        if 0 <= x < SIZE[0] and 0 <= y < SIZE[1] and 0 <= z < SIZE[2]:
            blocks[(x, y, z)] = state

    ground(put)
    brook(put)
    road(put)
    wood(put, blocks)
    rails(put)
    fallen(put)
    bridge(put)
    yard(put)
    church(put)
    belfry(put)
    braziers_and_mist(put)
    lights(put, blocks)
    return hollow(blocks)


def ground(put):
    """The floor everywhere: earth under a top layer of podzol, moss and coarse and rooted earth in patches."""
    for x in range(SIZE[0]):
        for z in range(SIZE[2]):
            for y in range(SOIL - 3, SOIL):
                put(x, y, z, DIRT)
            put(x, SOIL, z, floor_block(x, z))


def brook(put):
    """The brook: still water flush with the ground, BROOK_DEPTH deep, on gravel and mud."""
    for x in range(BROOK["x"][0], BROOK["x"][1] + 1):
        for z in range(BROOK["z"][0], BROOK["z"][1] + 1):
            put(x, SOIL - BROOK_DEPTH, z, MUD if _hash(x, z, 53) < 0.4 else GRAVEL)
            for y in range(SOIL - BROOK_DEPTH + 1, SOIL + 1):
                put(x, y, z, WATER)


def road(put):
    """The wood road: trodden earth from the bridge to the mist."""
    for x in range(ROAD[0], ROAD[1] + 1):
        for z in range(ROAD_NORTH, ROAD_SOUTH + 1):
            put(x, SOIL, z, PATH)


def wood(put, blocks):
    """The wood round everything open: old trunks, the thicket between them, and the canopy over it and the hollow's rim."""
    for x in range(SIZE[0]):
        for z in range(SIZE[2]):
            if is_open(x, z):
                rim = in_hollow(x, z) and distance(x, z) >= HOLLOW - CANOPY_REACH
                if rim and not on_road(x, z):
                    canopy(put, x, z)
                continue
            wet = blocks.get((x, SOIL, z)) == WATER
            trunk = _hash(x, z, 59) < TRUNK_CHANCE and not wet
            for y in range(STAND, CANOPY_HIGH + 1):
                if trunk and y < CANOPY_LOW + 1:
                    put(x, y, z, log("dark_oak_log", "y"))
                elif y <= THICKET_TOP and not wet:
                    put(x, y, z, LEAVES)        # (the brook runs on under the wood, open over its water)
            if not trunk:
                canopy(put, x, z)
            else:
                for y in range(CANOPY_LOW + 1, CANOPY_HIGH + 1):
                    put(x, y, z, LEAVES)


def canopy(put, x, z):
    low = CANOPY_LOW + (1 if _hash(x, z, 61) < 0.3 else 0)
    high = CANOPY_HIGH - (1 if _hash(x, z, 67) < 0.35 else 0)
    for y in range(low, high + 1):
        put(x, y, z, LEAVES)


def rails(put):
    """The split-rail fences beside the road, joined along it, with gaps."""
    cells = rail_cells()
    for x, z in cells:
        put(x, STAND, z, fence(north=(x, z - 1) in cells, south=(x, z + 1) in cells))


def fallen(put):
    """The fallen trunks: dark oak lying east and west on the floor."""
    for x0, x1, z in FALLEN:
        for x in range(x0, x1 + 1):
            put(x, STAND, z, log("dark_oak_log", "x"))


def bridge(put):
    """The covered bridge: a spruce deck over the brook, plank walls on stripped spruce posts with windows, and a
    shingled dark oak roof."""
    (z0, z1), (wx0, wx1) = BRIDGE["z"], BRIDGE_WALLS
    for z in range(z0, z1 + 1):
        for x in range(wx0, wx1 + 1):
            put(x, SOIL, z, PLANKS)
        for x in (wx0, wx1):
            for y in range(STAND, STAND + BRIDGE_WALL_HEIGHT):
                if z in BRIDGE_POSTS:
                    put(x, y, z, log("stripped_spruce_log", "y"))
                elif z in WINDOW_ROWS and y in (STAND + 1, STAND + 2):
                    continue
                else:
                    put(x, y, z, PLANKS)
        # The roof: three tiers of stairs either side of the ridge, a slab along it, eaves overhanging the walls.
        top = STAND + BRIDGE_WALL_HEIGHT
        put(wx0 - 1, top, z, stair("dark_oak", "east"))
        put(wx1 + 1, top, z, stair("dark_oak", "west"))
        for x in range(wx0, wx1 + 1):
            put(x, top, z, ROOF_PLANKS)
        put(wx0, top + 1, z, stair("dark_oak", "east"))
        put(wx1, top + 1, z, stair("dark_oak", "west"))
        for x in range(wx0 + 1, wx1):
            put(x, top + 1, z, ROOF_PLANKS)
        put(wx0 + 1, top + 2, z, stair("dark_oak", "east"))
        put(wx1 - 1, top + 2, z, stair("dark_oak", "west"))
        put(wx0 + 2, top + 2, z, ROOF_PLANKS)
        put(wx0 + 2, top + 3, z, ROOF_SLAB)


def yard(put):
    """The churchyard: its low wall, the road through it to the belfry's door, and its old stone markers."""
    cells = yard_wall_cells()
    for x, z in cells:
        put(x, STAND, z, wall(east=(x + 1, z) in cells, west=(x - 1, z) in cells, north=(x, z - 1) in cells,
                              south=(x, z + 1) in cells))
    for x in range(ROAD[0], ROAD[1] + 1):
        for z in range(DOOR["z"] + 1, BRIDGE["z"][0]):
            put(x, SOIL, z, PATH)
    for x, z in marker_cells():
        put(x, STAND, z, MARKERS[int(_hash(x, z, 71) * len(MARKERS))])


def church(put):
    """The church: fieldstone walls CHURCH_WALL high, window gaps along its sides, a spruce floor, and a steep dark oak roof
    running north and south over it."""
    (x0, x1), (z0, z1) = CHURCH["x"], CHURCH["z"]
    for x in range(x0, x1 + 1):
        for z in range(z0, z1 + 1):
            edge = x in (x0, x1) or z in (z0, z1)
            put(x, SOIL, z, CHURCH_FLOOR if not edge else fieldstone(x, SOIL, z))
            if not edge:
                continue
            for y in range(STAND, STAND + CHURCH_WALL):
                window = x in (x0, x1) and z % 3 == 0 and y in (STAND + 2, STAND + 3) and z0 < z < z1
                arch = z == z1 and DOOR["x"][0] <= x <= DOOR["x"][1] and y < STAND + 3
                if not window and not arch:
                    put(x, y, z, fieldstone(x, y, z))
    # The roof: stairs stepping up from both long walls to a ridge over the middle column.
    middle = (x0 + x1) // 2
    for z in range(z0 - 1, z1 + 2):
        for step in range(middle - x0 + 2):
            y = STAND + CHURCH_WALL + step
            west, east = x0 - 1 + step, x1 + 1 - step
            if west >= east:
                put(middle, y, z, ROOF_SLAB)
                break
            put(west, y, z, stair("dark_oak", "east"))
            put(east, y, z, stair("dark_oak", "west"))
            if z in (z0 - 1, z1 + 1):
                continue
            # The gables: fieldstone filling the roof's ends.
            if z in (z0, z1):
                for x in range(west + 1, east):
                    put(x, y, z, fieldstone(x, y, z))


def belfry(put):
    """The belfry over the church's door: white boards, an open chamber with the bell in it, a pointed roof, and the door
    open three wide and three high at its foot."""
    (x0, x1), (z0, z1) = BELFRY["x"], BELFRY["z"]
    chamber = (BELL_Y - 1, BELL_Y + 1)
    roof_base = BELL_Y + 2
    for x in range(x0, x1 + 1):
        for z in range(z0, z1 + 1):
            edge = x in (x0, x1) or z in (z0, z1)
            corner = x in (x0, x1) and z in (z0, z1)
            put(x, SOIL, z, CHURCH_FLOOR)
            for y in range(STAND, roof_base):
                if not edge:
                    if y == chamber[0] - 1:
                        put(x, y, z, WHITE)           # the chamber's floor
                    continue
                if chamber[0] <= y <= chamber[1] and not corner:
                    continue                          # the chamber's openings
                if z in (z0, z1) and DOOR["x"][0] <= x <= DOOR["x"][1] and y < STAND + 3:
                    continue                          # the door, and the arch into the church
                put(x, y, z, WHITE_TRIM if corner else WHITE)
    put((x0 + x1) // 2, BELL_Y + 1, (z0 + z1) // 2, WHITE)
    put((x0 + x1) // 2, BELL_Y, (z0 + z1) // 2, BELL)
    # The roof: rings of dark oak stairs stepping in to a point.
    for step in range(0, (x1 - x0) // 2 + 1):
        y = roof_base + step
        if y > BELFRY_TOP:
            break
        lx, hx, lz, hz = x0 + step, x1 - step, z0 + step, z1 - step
        if lx >= hx or lz >= hz:
            put((x0 + x1) // 2, y, (z0 + z1) // 2, ROOF_SLAB)
            break
        for x in range(lx, hx + 1):
            put(x, y, lz, stair("dark_oak", "south"))
            put(x, y, hz, stair("dark_oak", "north"))
        for z in range(lz + 1, hz):
            put(lx, y, z, stair("dark_oak", "east"))
            put(hx, y, z, stair("dark_oak", "west"))
        for x in range(lx + 1, hx):
            for z in range(lz + 1, hz):
                put(x, y, z, ROOF_PLANKS)


def braziers_and_mist(put):
    """The Soul Braziers on their ring, lit, and the Grey Mist across the road's end, between the trees."""
    for x, z in braziers().values():
        put(x, STAND, z, BRAZIER)
    for x, y, z in exit_cells():
        put(x, y, z, EXIT_MIST)


def lights(put, blocks):
    """The hidden lights: a lattice over everything open, LIGHT_HEIGHT up, staggered row to row."""
    for row, z in enumerate(range(2, SIZE[2] - 1, LIGHT_STEP // 2)):
        for x in range(2 + (LIGHT_STEP // 2 if row % 2 else 0), SIZE[0] - 1, LIGHT_STEP):
            if not is_open(x, z):
                continue
            y = STAND + LIGHT_HEIGHT
            if (x, y, z) not in blocks:
                put(x, y, z, light(LIGHT))


def hollow(blocks):
    """Leaves out blocks nobody can see: earth whose six neighbours are all solid."""
    solid = {DIRT, GRAVEL, MUD, *(state for _, state in FLOOR_BLOCKS)}
    out = {}
    for (x, y, z), state in blocks.items():
        if state in solid and all(blocks.get(n) in solid for n in (
                (x + 1, y, z), (x - 1, y, z), (x, y + 1, z), (x, y - 1, z), (x, y, z + 1), (x, y, z - 1))):
            continue
        out[(x, y, z)] = state
    return out


def template():
    return lair_layout.template(build(), SIZE)


def write(path):
    lair_layout.write(path, template())


if __name__ == "__main__":
    t = template()
    print(f"{len(t['blocks'])} blocks, {len(t['palette'])} states")

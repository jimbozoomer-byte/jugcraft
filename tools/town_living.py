"""The living spawn city (blueprint revision 4): where each resident works by day and gathers in the evening, and the
street furniture added round the square, the market and the streets (town/TownRoutine.java and town/Townsfolk.java read
the routines; TownUpgrades applies the new cells to an existing town).

Both passes run last in Town.build(), after the stair shapes and connections are worked out, and draw from their own
random stream (SEED + 4). No earlier layout draw, resident index, name, skin, bed, sign or block changes; the frozen
revision 3 (data/jugcraft/town/revisions/3.json.gz) differs from this blueprint only in the cells added here.

Decorations are strictly additive. Each goes into a cell the town left as air, outdoors, on solid paving or ground. None
goes near a door, a decor site, a sign, an ATM, a resident's place, a gathering, or anywhere on the Church of Notch's
whole plot (where the owner's slot machines stand). They are plain blocks with no block entity and no inventory. None
joins a neighbour, and none stands beside a block that would join it (a fence, a pane, a wall), so placing one never
changes another block. The mod applies a cell only where the live world still shows the old blueprint's air, so whatever
an owner built there stays.
"""
import math
import random
import sys

from town_voxels import S, block_name

AIR = S("air")
#: x0, z0, x1, z1 of the Church of Notch's whole plot (the building, its steps and the owner's slot machines inside).
CHURCH = (78, 35, 114, 87)
#: South edge of the square, facing north. Outside stalls, the south road and the church plot.
BOARD = (104, 1, 112)
BOARD_STATE = "jugcraft:town_commission_board[facing=north,part={part}]"
#: What the ten street townsfolk do by day, in turn by their place's order.
JOBS = ["gardener", "porter", "sweeper", "lamplighter", "busker"]
#: Roles that join an evening gathering. Guards stay on watch, the priest keeps vespers and innkeepers keep their counters.
GATHERERS = {"townsfolk", "decorator", "shopkeeper", "vendor", "baker", "banker", "mayor"}
WOODS = ["oak", "spruce", "dark_oak"]
CRATES = ["cabbage", "carrot", "beetroot", "corn", "potato", "onion", "tomato"]
POTS = ["red_tulip", "pink_tulip", "white_tulip", "orange_tulip", "allium", "azure_bluet", "cornflower", "oxeye_daisy",
        "lily_of_the_valley", "fern", "azalea_bush", "flowering_azalea_bush"]
#: Ground a decoration may stand on: the town's paving. Grass only under blocks that let light through.
PAVING = {"cobblestone", "mossy_cobblestone", "stone_bricks", "cracked_stone_bricks", "chiseled_stone_bricks", "andesite",
          "polished_andesite", "stone", "smooth_stone", "bricks", "gravel"}
#: Blocks that would join a full block placed beside them (and so change their own state).
JOINS = ("fence", "pane", "bars", "_wall", "glass", "chain")
#: Blocks a bench may not lean against.
NOT_WALL = ("door", "trapdoor", "fence", "pane", "glass", "slab", "stairs", "sign", "banner", "bed", "chest", "barrel",
            "torch", "lantern", "leaves", "water", "lava", "carpet", "ladder", "_wall", "bars", "pot", "button", "lever",
            "rail", "flower", "grass", "fern", "bush", "sapling", "chain", "candle", "skull", "head", "atm", "path")


def _layout(town):
    """tools/town.py's module (its layout constants), whether it runs as a script or was imported."""
    return sys.modules[type(town).__module__]


def church(x, z):
    x0, z0, x1, z1 = CHURCH
    return x0 <= x <= x1 and z0 <= z <= z1


def _empty(state):
    return state is None or state == AIR


def _name(state):
    return block_name(state) if state else ""


class Plan:
    """What the passes need to know about the town: free cells, reserved columns and the gatherings."""

    def __init__(self, town):
        L = _layout(town)
        self.town, self.L = town, L
        self.top = L.HEIGHT + L.Y_MIN
        self.site_cells = {(b[0], b[1], b[2]) for site in town.sites for b in site["blocks"]}
        self.site_columns = {(x, z) for x, _, z in self.site_cells}
        self.low_sites = {(x, z) for x, y, z in self.site_cells if y <= 1}
        self.spots = [(s["pos"][0], s["pos"][2]) for s in town.spots if s["pos"][1] == 1]
        self.signs = [(s["pos"][0], s["pos"][2]) for s in town.signs]
        self.boards = [(b["pos"][0], b["pos"][2]) for b in town.map_boards]
        self.atms = [(a[0], a[2]) for a in town.atms]
        self.lamps = [(site["blocks"][0][0], site["blocks"][0][2]) for site in town.sites if site["kind"] == "lamp"]
        self.planters = [(site["blocks"][0][0], site["blocks"][0][2]) for site in town.sites if site["kind"] == "planter"]
        self.gatherings = gatherings(town, self)
        self.decor = {}

    # ------------------------------------------------ cells

    def inside(self, x, z, margin=1):
        L = self.L
        return 0 <= x < L.SIZE and 0 <= z < L.SIZE and self.town.sd[z, x] < -(L.RING_LANE[1] + margin)

    def ground(self, x, z):
        return self.town.v.get(x, 0, z)

    def walkable(self, x, z):
        """A place a resident can stand: ground under it, two blocks of air, no decor site in the way."""
        g = self.ground(x, z)
        if not self.inside(x, z, 0) or _empty(g) or any(w in _name(g) for w in ("water", "lava", "fence", "_wall")):
            return False
        return all(_empty(self.town.v.get(x, y, z)) and (x, y, z) not in self.site_cells and (x, y, z) not in self.decor
                   for y in (1, 2))

    def open_sky(self, x, z):
        """Nothing of the town's above the cell (decor sites such as awnings and lamps aside)."""
        v = self.town.v
        return all(_empty(v.get(x, y, z)) for y in range(1, self.top))

    def outdoors(self, x, z):
        """Under open sky, or under a roof's eaves at least three high with open sky beside it (never indoors)."""
        if self.open_sky(x, z):
            return True
        v = self.town.v
        return all(_empty(v.get(x, y, z)) for y in (1, 2)) and any(
            self.open_sky(x + dx, z + dz) for dx, dz in ((1, 0), (-1, 0), (0, 1), (0, -1)))

    def near(self, x, z, points, r):
        return any(max(abs(x - px), abs(z - pz)) <= r for px, pz in points)

    def near_door(self, x, z):
        v = self.town.v
        for dx in (-1, 0, 1):
            for dz in (-1, 0, 1):
                for y in (1, 2):
                    n = _name(v.get(x + dx, y, z + dz))
                    if "_door" in n or "fence_gate" in n or "stairs" in n or "ladder" in n:
                        return True
        return False

    def joins_beside(self, x, y, z):
        v = self.town.v
        return any(any(w in _name(v.get(x + dx, y, z + dz)) for w in JOINS)
                   for dx, dz in ((1, 0), (-1, 0), (0, 1), (0, -1)))

    def reserved(self, x, z):
        """Columns no decoration may take: the church plot, the Styx district's road, and room round everything the
        town or its people use."""
        L = self.L
        if church(x, z):
            return True
        if x >= 175 and 94 <= z <= 98:   # StyxTownDistrict's road out of the east gate
            return True
        fx, fz = L.FOUNTAIN_CENTRE
        if math.hypot(x - fx, z - fz) < 9.5:
            return True
        cx0, cz0, cx1, cz1 = L.CENTERPIECE
        if cx0 - 2 <= x <= cx1 + 2 and cz0 - 2 <= z <= cz1 + 2:
            return True
        bx, _, bz = BOARD
        if bx - 1 <= x <= bx + 3 and bz - 3 <= z <= bz + 1:
            return True
        if (x, z) in self.site_columns or self.near(x, z, self.low_sites, 1):
            return True
        if self.near(x, z, self.spots, 1) or self.near(x, z, self.atms, 2) or self.near(x, z, self.signs, 1) \
                or self.near(x, z, self.boards, 1) or self.near(x, z, self.lamps, 1):
            return True
        for g in self.gatherings.values():
            if math.hypot(x - g["centre"][0], z - g["centre"][1]) < g["keep"]:
                return True
        return self.near_door(x, z)

    def free(self, x, z, grass=False, height=1):
        """A cell a decoration may take, `height` blocks high."""
        if not self.inside(x, z) or self.reserved(x, z) or not self.outdoors(x, z):
            return False
        g = _name(self.ground(x, z))
        if g not in PAVING and not (grass and g == "grass_block"):
            return False
        for y in range(1, height + 1):
            if (x, y, z) in self.decor or self.joins_beside(x, y, z):
                return False
        # Never crowd another decoration from the side (furniture of one kind would join).
        return not any((x + dx, 1, z + dz) in self.decor for dx, dz in ((1, 0), (-1, 0), (0, 1), (0, -1)))

    def put(self, x, y, z, state):
        if not _empty(self.town.v.get(x, y, z)) or (x, y, z) in self.site_cells or (x, y, z) in self.decor:
            raise ValueError(f"town decoration over an authored cell at {x} {y} {z}")
        self.decor[(x, y, z)] = state


# ---------------------------------------------------------------- gatherings

def _door(town, house):
    """A house's front door (lower half) and the way out of it."""
    x0, z0, x1, z1 = house[:4]
    for x in range(x0, x1 + 1):
        for z in range(z0, z1 + 1):
            state = town.v.get(x, 1, z) or ""
            if "_door" in state and "half=lower" in state:
                for dx, dz in ((0, 1), (0, -1), (1, 0), (-1, 0)):
                    if not (x0 <= x + dx <= x1 and z0 <= z + dz <= z1):
                        return (x, z), (dx, dz)
    raise ValueError(f"no front door on {house}")


def gatherings(town, plan):
    """Where the town gathers in the evening: before each tavern's door, and round the fountain."""
    L = _layout(town)
    out = {}
    taverns = sorted((h for h in town.houses if h[4] == "tavern"), key=lambda h: (h[1], h[0]))
    for i, house in enumerate(taverns):
        (dx, dz), (sx, sz) = _door(town, house)
        out[f"tavern_{i}"] = {"centre": (dx + 3 * sx, dz + 3 * sz), "door": (dx + sx, dz + sz), "radius": (1.4, 2.9),
                              "keep": 3.6, "share": 7}
    out["fountain"] = {"centre": L.FOUNTAIN_CENTRE, "door": None, "radius": (5.6, 6.7), "keep": 0, "share": 10}
    return out


def ring(plan, name, taken):
    """Standing places round a gathering, in turn round it."""
    g = plan.gatherings[name]
    cx, cz = g["centre"]
    lo, hi = g["radius"]
    cells = []
    for x in range(int(cx - hi) - 1, int(cx + hi) + 2):
        for z in range(int(cz - hi) - 1, int(cz + hi) + 2):
            r = math.hypot(x - cx, z - cz)
            if lo <= r <= hi and (x, z) not in taken and plan.walkable(x, z) \
                    and not (g["door"] and max(abs(x - g["door"][0]), abs(z - g["door"][1])) <= 1):
                cells.append((math.atan2(z - cz, x - cx), x, z))
    return [(x, z) for _, x, z in sorted(cells)]


# ---------------------------------------------------------------- decorations

def decorate(town):
    """Adds the street furniture and the commission board to the town's blocks. Returns the plan (for routines)."""
    L = _layout(town)
    rng = random.Random(L.SEED + 4)
    plan = Plan(town)
    bx, by, bz = BOARD
    for row in range(3):
        for col in range(3):
            x, y, z = bx + col, by + row, bz
            if not _empty(town.v.get(x, y, z)) or not plan.walkable(x, z - 1) or _name(plan.ground(x, z)) not in PAVING:
                raise ValueError(f"the square noticeboard footprint is not clear at {x} {y} {z}")
            plan.put(x, y, z, BOARD_STATE.format(part=row * 3 + col))
    plan.crates = []
    market(plan, rng)
    cafes(plan, rng)
    square_edges(plan, rng)
    frontages(plan, rng)
    for (x, y, z), state in sorted(plan.decor.items()):
        town.v.set(x, y, z, state)
    return plan


def market(plan, rng):
    """Produce crates stacked beside and behind each market stall, a lantern on some."""
    for box in plan.town.stalls:
        x0, z0, x1, z1 = box
        keeper = next((p for p in plan.spots if x0 <= p[0] <= x1 and z0 <= p[1] <= z1), None)
        if keeper is None:
            continue
        # The front is the edge nearest the keeper: customers stand there.
        edges = {"n": keeper[1] - z0, "s": z1 - keeper[1], "w": keeper[0] - x0, "e": x1 - keeper[0]}
        front = min(edges, key=edges.get)
        cells = []
        for x in range(x0 - 1, x1 + 2):
            for z in range(z0 - 1, z1 + 2):
                if x0 <= x <= x1 and z0 <= z <= z1:
                    continue
                if front == "n" and z <= z0 or front == "s" and z >= z1 or front == "w" and x <= x0 or front == "e" and x >= x1:
                    continue
                cells.append((x, z))
        rng.shuffle(cells)
        put = 0
        for x, z in cells:
            if put >= 2:
                break
            if not plan.free(x, z, height=2):
                continue
            crop = rng.choice(CRATES)
            plan.put(x, 1, z, f"jugcraft:{crop}_crate")
            top = rng.random()
            if top < 0.35:
                plan.put(x, 2, z, f"jugcraft:{rng.choice(CRATES)}_crate")
            elif top < 0.6:
                plan.put(x, 2, z, S("lantern", hanging="false", waterlogged="false"))
            plan.crates.append((x, z))
            put += 1


def cafes(plan, rng):
    """A few café tables on the square's open paving, each with a chair either side, off the walking lines."""
    L = plan.L
    x0, x1, z0, z1 = L.SQUARE
    placed = []
    cells = [(x, z) for x in range(x0 + 3, x1 - 2) for z in range(z0 + 3, z1 - 2)]
    rng.shuffle(cells)
    for x, z in cells:
        if len(placed) >= 4:
            break
        # Keep the main street's line across the square and the way north to the church doors.
        if L.MAIN_STREET[2] - 1 <= z <= L.MAIN_STREET[3] + 1 or L.SOUTH_STREET[0] - 1 <= x <= L.SOUTH_STREET[1] + 1:
            continue
        if any(math.hypot(x - px, z - pz) < 7 for px, pz in placed):
            continue
        if any(b[0] - 2 <= x <= b[2] + 2 and b[1] - 2 <= z <= b[3] + 2 for b in plan.town.stalls):
            continue
        along_x = rng.random() < 0.5
        dx, dz = (1, 0) if along_x else (0, 1)
        row = [(x - dx, z - dz), (x, z), (x + dx, z + dz)]
        if not all(plan.free(cx, cz) for cx, cz in row):
            continue
        # Room to walk round it.
        if not all(plan.walkable(cx, cz) for ax, az in row for cx in (ax - 1, ax, ax + 1) for cz in (az - 1, az, az + 1)
                   if (cx, cz) not in row):
            continue
        wood = rng.choice(WOODS)
        plan.put(x, 1, z, S(f"jugcraft:crafted_{wood}_table", color="none", shape="single", waterlogged="false"))
        first, second = ("east", "west") if along_x else ("south", "north")
        plan.put(x - dx, 1, z - dz, S(f"jugcraft:crafted_{wood}_chair", color="none", facing=first, waterlogged="false"))
        plan.put(x + dx, 1, z + dz, S(f"jugcraft:crafted_{wood}_chair", color="none", facing=second, waterlogged="false"))
        placed.append((x, z))


def square_edges(plan, rng):
    """Round the square's edge, inside its kerb: hedges, flower stands and benches facing in, in turn, every few
    blocks, clear of the streets that cross it."""
    L = plan.L
    x0, x1, z0, z1 = L.SQUARE
    ring = [(x, z0 + 1, "south") for x in range(x0 + 2, x1 - 1)] + [(x1 - 1, z, "west") for z in range(z0 + 2, z1 - 1)] \
        + [(x, z1 - 1, "north") for x in range(x1 - 2, x0 + 1, -1)] + [(x0 + 1, z, "east") for z in range(z1 - 2, z0 + 1, -1)]
    kinds = ["hedge", "stand", "hedge", "bench"]
    k = 0
    last = None
    for x, z, facing in ring:
        if L.MAIN_STREET[2] - 1 <= z <= L.MAIN_STREET[3] + 1 or L.SOUTH_STREET[0] - 1 <= x <= L.SOUTH_STREET[1] + 1:
            continue
        if last is not None and math.hypot(x - last[0], z - last[1]) < 4:
            continue
        kind = kinds[k % len(kinds)]
        if not plan.free(x, z, height=2 if kind == "stand" else 1):
            continue
        if kind == "hedge":
            plan.put(x, 1, z, S("flowering_azalea_leaves" if rng.random() < 0.7 else "azalea_leaves", distance="7",
                                persistent="true", waterlogged="false"))
        elif kind == "stand":
            plan.put(x, 1, z, S(f"jugcraft:crafted_{rng.choice(WOODS)}_side_table", color="none", facing=facing,
                                waterlogged="false"))
            plan.put(x, 2, z, S(f"potted_{rng.choice(POTS)}"))
        else:
            plan.put(x, 1, z, S(f"jugcraft:crafted_{rng.choice(WOODS)}_bench", color="none", facing=facing, shape="single",
                                waterlogged="false"))
        k += 1
        last = (x, z)


def frontages(plan, rng):
    """Along the streets, lanes and the square's edges: benches against house walls facing the street, hedges and potted
    flowers between them, crates and hay outside the General Store and the bakery."""
    L, town = plan.L, plan.town
    shops = {h[4]: h for h in town.houses if h[4] in ("general", "bakery", "tailor", "tavern")}
    used_benches = []
    used_any = []
    cells = []
    for z in range(L.SIZE):
        for x in range(L.SIZE):
            if town.used[z, x] == 1:
                continue
            for fx, fz, facing in ((0, 1, "south"), (0, -1, "north"), (1, 0, "east"), (-1, 0, "west")):
                sx, sz = x + fx, z + fz
                if not (0 <= sx < L.SIZE and 0 <= sz < L.SIZE) or town.used[sz, sx] != 1:
                    continue
                wall = town.v.get(x - fx, 1, z - fz)
                if _empty(wall) or any(w in _name(wall) for w in NOT_WALL) or _empty(town.v.get(x - fx, 2, z - fz)):
                    continue
                cells.append((x, z, facing, (x - fx, z - fz)))
                break
    rng.shuffle(cells)
    for x, z, facing, behind in cells:
        if any(math.hypot(x - px, z - pz) < 3 for px, pz in used_any):
            continue
        owner = next((kind for kind, h in shops.items() if h[0] <= behind[0] <= h[2] and h[1] <= behind[1] <= h[3]), None)
        roll = rng.random()
        if owner in ("general", "bakery") and roll < 0.7:
            if not plan.free(x, z, height=2):
                continue
            if owner == "bakery":
                plan.put(x, 1, z, S("hay_block", axis="y"))
            else:
                plan.put(x, 1, z, f"jugcraft:{rng.choice(CRATES)}_crate")
            if rng.random() < 0.5:
                plan.put(x, 2, z, f"jugcraft:{rng.choice(CRATES)}_crate")
            plan.crates.append((x, z))
        elif roll < 0.42 and not any(math.hypot(x - px, z - pz) < 8 for px, pz in used_benches):
            if not plan.free(x, z, grass=True):
                continue
            wood = rng.choice(WOODS)
            plan.put(x, 1, z, S(f"jugcraft:crafted_{wood}_bench", color="none", facing=facing, shape="single",
                                waterlogged="false"))
            used_benches.append((x, z))
        elif roll < 0.58:
            # A flower stand: a little side table with a potted flower on it.
            if not plan.free(x, z, grass=True, height=2):
                continue
            plan.put(x, 1, z, S(f"jugcraft:crafted_{rng.choice(WOODS)}_side_table", color="none", facing=facing,
                                waterlogged="false"))
            plan.put(x, 2, z, S(f"potted_{rng.choice(POTS)}"))
        elif roll < 0.88:
            if not plan.free(x, z, grass=True):
                continue
            if rng.random() < 0.55:
                plan.put(x, 1, z, S("flowering_azalea_leaves" if rng.random() < 0.6 else "azalea_leaves", distance="7",
                                    persistent="true", waterlogged="false"))
            else:
                plan.put(x, 1, z, S(f"potted_{rng.choice(POTS)}"))
        else:
            continue
        used_any.append((x, z))


# ---------------------------------------------------------------- routines

def routines(town, plan):
    """Gives each resident a job, a place to work by day and a place in an evening gathering (in their spot's data)."""
    spots = town.spots
    taken = set()

    def claim(cells, near):
        free = [c for c in cells if c not in taken]
        if not free:
            return None
        best = min(free, key=lambda c: (c[0] - near[0]) ** 2 + (c[1] - near[1]) ** 2)
        taken.add(best)
        return best

    def beside(points, near, used):
        """A free standing place next to the nearest of some points (planters, crates, lamp posts) not yet used."""
        for px, pz in sorted((p for p in points if p not in used), key=lambda p: (p[0] - near[0]) ** 2 + (p[1] - near[1]) ** 2):
            cells = [(px + dx, pz + dz) for dx, dz in ((0, 1), (0, -1), (1, 0), (-1, 0))
                     if plan.walkable(px + dx, pz + dz) and not church(px + dx, pz + dz)]
            cell = claim(cells, near)
            if cell:
                used.add((px, pz))
                return cell, (px, pz)
        return None, None

    k = 0
    used_points = set()
    workers = {}
    for spot in spots:
        role = spot["role"]
        pos = (spot["pos"][0], spot["pos"][2])
        if role == "townsfolk":
            job = JOBS[k % len(JOBS)]
            k += 1
        else:
            job = {"guard": "watch", "flak_guard": "flak", "mecha_guard": "walker"}.get(role, role)
            if role == "shopkeeper" and spot.get("shop") == "florist":
                job = "florist"
        spot["job"] = job
        workers[id(spot)] = job
        if job == "gardener":
            cell, look = beside(plan.planters, pos, used_points)
            if cell:
                spot["work"], spot["work_look"] = [cell[0], 1, cell[1]], [look[0], 1, look[1]]
        elif job == "porter":
            cell, look = beside(plan.crates, pos, used_points)
            if cell:
                spot["work"], spot["work_look"] = [cell[0], 1, cell[1]], [look[0], 1, look[1]]
        elif job == "lamplighter":
            cell, look = beside(plan.lamps, pos, used_points)
            if cell:
                spot["work"], spot["work_look"] = [cell[0], 1, cell[1]], [look[0], 4, look[1]]
            x0, x1, z0, z1 = plan.L.SQUARE
            square = [p for p in plan.lamps if x0 <= p[0] <= x1 and z0 <= p[1] <= z1]
            cell, look = beside(square, ((x0 + x1) // 2, (z0 + z1) // 2), used_points)
            if cell:
                spot["evening"], spot["evening_look"] = [cell[0], 1, cell[1]], [look[0], 4, look[1]]
                spot["gathering"] = "lamps"
        elif job == "sweeper":
            line = [(x, (plan.L.MAIN_STREET[2] + plan.L.MAIN_STREET[3]) // 2)
                    for x in range(plan.L.MAIN_STREET[0] + 6, plan.L.MAIN_STREET[1] - 5)] \
                + [((plan.L.SOUTH_STREET[0] + plan.L.SOUTH_STREET[1]) // 2, z)
                   for z in range(plan.L.SOUTH_STREET[2] + 8, plan.L.SOUTH_STREET[3] - 5)]
            line = [c for c in line if plan.walkable(*c) and not church(*c) and not any(
                math.hypot(c[0] - w[0], c[1] - w[2]) < 24 for w in (s.get("work") for s in spots if s.get("job") == "sweeper") if w)]
            cell = claim(line, pos)
            if cell:
                spot["work"] = [cell[0], 1, cell[1]]
        elif job == "busker":
            cell = claim(ring(plan, "fountain", taken), pos)
            if cell:
                fx, fz = plan.L.FOUNTAIN_CENTRE
                spot["work"] = [cell[0], 1, cell[1]]
                spot["work_look"] = [2 * cell[0] - fx, 1, 2 * cell[1] - fz]   # facing out, to the crowd
    # Evenings: buskers play at a tavern each; the mayor holds court by the fountain; the rest go to the gathering
    # nearest their bed, a fair share to each.
    places = list(plan.gatherings)
    taverns = [p for p in places if p.startswith("tavern")]
    joining = [s for s in spots if s["role"] in GATHERERS and s["job"] != "lamplighter"]
    # The fountain's ring holds more than a tavern's doorstep.
    share = {p: plan.gatherings[p]["share"] for p in places}
    count = {p: 0 for p in places}

    def seat(spot, place):
        g = plan.gatherings[place]
        cell = claim(ring(plan, place, taken), (spot["pos"][0], spot["pos"][2]) if place != "fountain"
                     else tuple(spot.get("bed", spot["pos"])[::2]))
        if cell is None:
            return False
        spot["evening"] = [cell[0], 1, cell[1]]
        spot["evening_look"] = [g["centre"][0], 1, g["centre"][1]]
        spot["gathering"] = "tavern" if place.startswith("tavern") else place
        count[place] += 1
        return True

    buskers = [s for s in joining if s["job"] == "busker"]
    for i, spot in enumerate(buskers):
        seat(spot, taverns[i % len(taverns)])
    for spot in joining:
        if spot["job"] == "mayor":
            seat(spot, "fountain")
    for spot in joining:
        if "evening" in spot:
            continue
        home = spot.get("bed", spot["pos"])
        order = sorted(places, key=lambda p: (plan.gatherings[p]["centre"][0] - home[0]) ** 2
                       + (plan.gatherings[p]["centre"][1] - home[2]) ** 2)
        for place in order:
            if count[place] < share[place] and seat(spot, place):
                break


def check(old, new):
    """Problems with revision 4 against the frozen revision 3 (both decoded export dicts): every changed cell was
    authored air inside the wall, outside the church plot, and now holds a plain block; residents and beds kept."""
    import base64
    import numpy as np
    errors = []
    size, height, y_min = new["size"], new["height"], new["y_min"]
    if (old["size"], old["height"], old["y_min"]) != (size, height, y_min):
        return ["the town's box changed"]
    a = np.frombuffer(base64.b64decode(old["blocks"]), dtype="<u2").reshape(height, size, size)
    b = np.frombuffer(base64.b64decode(new["blocks"]), dtype="<u2").reshape(height, size, size)
    old_pal, new_pal = old["palette"], new["palette"]
    ids = {}
    old_ids = np.array([ids.setdefault(s, len(ids)) for s in old_pal])
    new_ids = np.array([ids.setdefault(s, len(ids)) for s in new_pal])
    changed = np.argwhere(old_ids[a] != new_ids[b])
    x0, z0, x1, z1 = CHURCH
    for y, z, x in changed:
        was, now = old_pal[a[y, z, x]], new_pal[b[y, z, x]]
        if was != "minecraft:air":
            errors.append(f"revision 4 changes an authored {was} at {x} {y + y_min} {z} into {now}")
        if x0 <= x <= x1 and z0 <= z <= z1:
            errors.append(f"revision 4 adds {now} on the church plot at {x} {y + y_min} {z}")
        if now == "keep" or any(w in now for w in ("chest", "barrel", "shulker", "hopper", "furnace", "sign", "banner")):
            errors.append(f"revision 4 adds a block entity or container {now} at {x} {y + y_min} {z}")
    if len(new["spots"]) < len(old["spots"]):
        errors.append("revision 4 drops residents")
    for before, after in zip(old["spots"], new["spots"]):
        for key in ("role", "name", "skin", "pos", "bed", "shop"):
            if before.get(key) != after.get(key):
                errors.append(f"resident {before.get('name')} changed {key}: {before.get(key)} -> {after.get(key)}")
    if old["sites"] != new["sites"] or old["atms"] != new["atms"] or old["map_boards"] != new["map_boards"]:
        errors.append("revision 4 moves decor sites, ATMs or map boards")
    if [s["pos"] for s in old["signs"]] != [s["pos"] for s in new["signs"]]:
        errors.append("revision 4 moves signs")
    return errors

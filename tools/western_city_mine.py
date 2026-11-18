"""Deepstead, the lamp-lit mine under Jugcraft's original spawn city.

Coordinates are relative to the saved city floor. The only surface opening is
in the *new* bridge approach outside the old gate. Call build_mine(v) after
building that bridge and before town_voxels.connections / stair_shapes.
"""
import math

from town_voxels import S, Voxels, turn_facing


CX, CZ = 87, 86
UPPER, MARKET, BASIN = -54, -68, -82
TIERS = (BASIN, MARKET, UPPER)
AIR = S("air")
BRICK = S("deepslate_bricks")
TILE = S("deepslate_tiles")
STONE = S("polished_deepslate")
TRIM = S("polished_andesite")
COPPER = S("waxed_cut_copper")
PATINA = S("waxed_weathered_cut_copper")
PLANK = S("spruce_planks")
POST = S("stripped_spruce_log", axis="y")
RAIL = S("dark_oak_fence")
LAMP = S("lantern")
WATER = S("water", level=0)
SIDES = ((1, 0), (0, 1), (-1, 0), (0, -1))
NEIGHBORS = ((1,0,0),(-1,0,0),(0,1,0),(0,-1,0),(0,0,1),(0,0,-1))


def rock(x, y, z):
    """Folded strata rather than random per-block speckle."""
    fold = math.floor(2.1 * math.sin(x / 8) + 1.7 * math.cos(z / 10))
    band = (y + fold) % 15
    if band in (0, 1):
        return S("tuff")
    if band == 8 and y < MARKET:
        return S("basalt", axis="y")
    return S("deepslate", axis="y") if band < 10 else S("cobbled_deepslate")


def chamber(v):
    """Scalloped fault walls and a dome with a watertight, one-block skin."""
    for dx in range(-44, 45):
        for dz in range(-44, 45):
            angle = math.atan2(dz, dx)
            r = math.hypot(dx, dz)
            rim = 40.5 + 1.1 * math.sin(7 * angle) + 0.8 * math.cos(13 * angle)
            if r > rim:
                continue
            x, z = CX + dx, CZ + dz
            vault = math.sqrt(max(0, 1 - (r / rim)**2))
            ceiling = math.floor(-50 + 24 * vault + 1.4 * math.sin(dx / 6) * math.cos(dz / 5))
            floor = math.floor(-97 + 8 * (r / rim)**2 + 1.8 * math.cos(dx / 8) * math.cos(dz / 9))
            v.box(x, floor + 1, z, x, ceiling - 1, z, AIR)
            v.set(x, ceiling, z, rock(x, ceiling, z))
            v.set(x, floor, z, rock(x, floor, z))
            # A dark lake in the bottom of the excavation. Its level is below
            # every promenade. A solid island houses the tower footings.
            if floor < -94 and r > 7:
                v.box(x, floor + 1, z, x, -94, z, WATER)
            if r <= 11:
                top = -84 - max(0, int(r) - 6)
                v.box(x, floor, z, x, top, z, rock)


def gallery(v, y, inner, bridge_axis):
    outer = 40
    for dx in range(-outer, outer + 1):
        for dz in range(-outer, outer + 1):
            r = math.hypot(dx, dz)
            if not inner <= r < outer:
                continue
            x, z = CX + dx, CZ + dz
            edge = r < inner + 1.4
            v.set(x, y - 1, z, BRICK)
            v.set(x, y, z, COPPER if edge else STONE if int(r) % 8 == 0 else S("stone_bricks"))
            if edge:
                # Rails break only where a full bridge deck catches the opening.
                crossing = abs(dz if bridge_axis == "x" else dx) <= 2
                if not crossing:
                    v.set(x, y + 1, z, RAIL)
            # Wall sconces radiate around the ring. Their niche lighting is
            # inlaid in the deck so displays and room doors remain usable.
            octant = round(math.atan2(dz, dx) * 8 / math.pi)
            a = octant * math.pi / 8
            if (dx, dz) == (round((inner+3)*math.cos(a)), round((inner+3)*math.sin(a))):
                v.set(x, y, z, S("ochre_froglight"))


def cavern_details(v):
    # A change in seam density leaves long natural teeth beside the cut
    # capitals, and exposed amber seams light the dark vault above the tower.
    for index in range(23):
        a = (index + 0.32) * 2 * math.pi / 23
        r = 20 + (7 * index) % 15
        xx, zz = CX + round(math.cos(a)*r), CZ + round(math.sin(a)*r)
        for dx in range(-2, 3):
            for dz in range(-2, 3):
                distance = abs(dx) + abs(dz)
                if distance > 3:
                    continue
                x, z = xx + dx, zz + dz
                roof = max(y for y in range(-55,-24) if v.get(x,y,z) not in (None,AIR))
                if index % 5 == 2:
                    v.set(x,roof,z, S("glowstone") if distance <= 1 else S("calcite"))
                else:
                    length = max(1, 4 + index%6 - distance*2)
                    v.box(x,roof-length+1,z,x,roof,z,S("dripstone_block"))
                    if distance == 0:
                        v.set(x,roof-length,z,S("pointed_dripstone",vertical_direction="down",thickness="tip"))

    # Massive gallery piers tie all three balustraded terraces to the rock
    # floor. Corbels branch toward the underside of each successive tier.
    for index in range(12):
        a = (index+0.5) * math.pi / 6
        x, z = CX + round(37*math.cos(a)), CZ + round(37*math.sin(a))
        for floor in TIERS:
            v.box(x-2,floor-2,z-2,x+2,floor-1,z+2,TILE)
            v.box(x-1,floor-13,z-1,x+1,floor-3,z+1,BRICK)
        v.box(x-1,-92,z-1,x+1,BASIN-3,z+1,STONE)
        # The stonemasons left the openings between the ring floors intact.
        # Running ribs finish short of the pedestrian promenade above.
        for level in (MARKET, UPPER):
            v.box(x,level-12,z,x,level-2,z,STONE)


def waterworks(v):
    # The south-east pump house feeds a closed viewing flume. The entire
    # transparent face looks over the basin below the lowest promenade.
    x0,z0 = CX+15,CZ+17
    for y in range(-94, BASIN):
        v.box(x0,y,z0,x0+3,y,z0+3,BRICK)
        v.box(x0+1,y,z0+1,x0+2,y,z0+2,WATER)
        v.box(x0+1,y,z0,x0+2,y,z0,S("glass"))
    v.box(x0,BASIN,z0,x0+3,BASIN,z0+3,PATINA)
    for z in (z0,z0+3):
        for x in (x0,x0+3):
            v.set(x,BASIN+1,z,STONE)
    v.box(x0,BASIN+2,z0,x0+3,BASIN+2,z0+3,COPPER)
    v.box(x0+1,-95,z0+1,x0+2,-95,z0+2,S("sea_lantern"))


def terrace_stairs(v, x, start, floor):
    # A fourteen-step flight rises south along the gallery. The opening flares
    # at the upper end, allowing someone on the landing to turn onto the ring.
    top = floor+14
    for step in range(1, 15):
        z, y = start+step, floor+step
        for xx in range(x-2,x+3):
            side = abs(xx-x)==2
            v.box(xx,y+1,z,xx,y+3,z,AIR)
            v.set(xx,y-1,z,BRICK)
            v.set(xx,y,z,TRIM if side else S("stone_brick_stairs",facing="south"))
            if side and step < 14:
                v.set(xx,y+1,z,RAIL)
            if side and step%5==0:
                v.box(xx,floor+1,z,xx,y-1,z,STONE)
                v.set(xx,y+2,z,LAMP)
            # Where the flight breaches the upper deck, rim the shaft too.
            if side and top-y<=3 and step<14:
                v.set(xx,top,z,TRIM)
                v.set(xx,top+1,z,RAIL)
    v.box(x-1,top,start+15,x+1,top,start+16,TRIM)
    v.box(x-1,top+1,start+15,x+1,top+3,start+16,AIR)


def market_pitch(v, x, y, z, color):
    """An open trading pitch tucked between the main road and the outer fronts."""
    for xx in range(x,x+5):
        for zz in range(z,z+4):
            v.set(xx,y,zz,PLANK)
            v.set(xx,y+5-(zz==z+3),zz,S(color+"_wool") if (xx-x)%2==0 else S("white_wool"))
    for xx in (x,x+4):
        for zz in (z,z+3):
            v.box(xx,y+1,zz,xx,y+3+(zz==z),zz,S("spruce_fence"))
    v.box(x+1,y+1,z+3,x+3,y+1,z+3,S("stripped_oak_log",axis="x"))
    v.set(x+1,y+2,z+3,S("flower_pot"))
    v.set(x+2,y+2,z+3,S("lantern"))
    v.set(x+3,y+1,z,S("spruce_stairs",facing="east"))


def hoist(v):
    # The overhead ore hoist spans two anchored trestles. A brake drum,
    # twin sheaves and a suspended ore skip leave its operation legible.
    z = CZ-17
    for x in (CX-31, CX-25):
        v.box(x-1,UPPER,z-1,x+1,UPPER,z+1,STONE)
        v.box(x,UPPER+1,z,x,UPPER+7,z,POST)
        v.set(x,UPPER+6,z-1,COPPER)
    for h in range(4):
        v.set(CX-25+h,UPPER+4+h,z,POST)
    v.box(CX-32,UPPER+8,z,CX-16,UPPER+8,z,S("stripped_dark_oak_log",axis="x"))
    for x in (CX-30,CX-17):
        v.box(x,UPPER+7,z-1,x,UPPER+7,z+1,PATINA)
        v.set(x,UPPER+7,z+2,STONE)
    v.box(CX-17,MARKET+6,z,CX-17,UPPER+6,z,S("iron_chain",axis="y"))
    v.box(CX-18,MARKET+4,z-1,CX-16,MARKET+5,z+1,PLANK)
    for x in (CX-18,CX-16):
        v.box(x,MARKET+4,z-1,x,MARKET+4,z+1,COPPER)
    v.set(CX-17,MARKET+6,z-1,S("raw_copper_block"))
    v.set(CX-17,MARKET+3,z,S("lantern",hanging=True))


def bridge(v, y, axis, reach):
    for direction in (-1, 1):
        for t in range(7, reach + 3):
            # A shallow arch spans the open chasm; taller springers meet the
            # central bastion and outer piers. The center stays visibly open.
            span = max(0, min(t - 8, reach - t))
            depth = max(1, 7 - int(2.6 * math.sqrt(span)))
            for side in range(-3, 4):
                along, across = direction * t, side
                dx, dz = (along, across) if axis == "x" else (across, along)
                x, z = CX + dx, CZ + dz
                rim = abs(side) == 3
                v.set(x, y, z, TRIM if rim else PLANK)
                v.box(x, y - (depth if rim else 1), z, x, y - 1, z, BRICK)
                if rim and t < reach:
                    v.set(x, y + 1, z, RAIL)
                    if t in (11, reach - 4):
                        v.set(x, y + 1, z, S("polished_deepslate_wall"))
                        v.set(x, y + 2, z, LAMP)


def watch_house(v):
    # The tower has a solid rock pedestal, fluted corner piers, copper aprons
    # separating each storey, glazed oriel windows and an open lantern crown.
    v.box(CX-6, -91, CZ-6, CX+6, BASIN-1, CZ+6, BRICK)
    for y in (BASIN, MARKET, UPPER, -40):
        half = 7
        v.box(CX-half-1, y, CZ-half-1, CX+half+1, y, CZ+half+1, COPPER)
        v.box(CX-half, y, CZ-half, CX+half, y, CZ+half, PLANK)
        top = y + (13 if y < -40 else 7)
        v.walls(CX-half, y+1, CZ-half, CX+half, top, CZ+half, BRICK if y < UPPER else POST)
        for sx, sz in SIDES:
            for offset in range(-5, 6):
                x, z = CX + sx*half - sz*offset, CZ + sz*half + sx*offset
                # Wide door looking down each actual bridge; other sides keep
                # a sill and tall amber slit windows.
                connects = ((y == UPPER and sz) or (y in (MARKET, BASIN) and sx))
                if connects and abs(offset) <= 2:
                    v.box(x, y+1, z, x, y+4+(abs(offset)<2), z, AIR)
                if abs(offset) in (3, 4) or (not connects and abs(offset)<=1):
                    v.box(x, y+3, z, x, min(y+7, top-1), z, S("orange_stained_glass_pane"))
                if abs(offset) == 5:
                    v.box(x+sx, y+1, z+sz, x+sx, top-1, z+sz, POST if y>=MARKET else STONE)
            for corner in (-1, 1):
                x, z = CX + sx*half - sz*corner*half, CZ + sz*half + sx*corner*half
                v.box(x, y+1, z, x, top, z, STONE if y <= MARKET else POST)
            if y in (MARKET, UPPER) and ((y == MARKET and sz) or (y == UPPER and sx)):
                # Projecting miners' balconies on the sides without bridges.
                for offset in range(-4,5):
                    for reach in range(7,12):
                        x = CX+sx*reach-sz*offset
                        z = CZ+sz*reach+sx*offset
                        v.set(x,y,z,COPPER if reach==11 else PLANK)
                        if reach==7 and abs(offset)<=1:
                            v.box(x,y+1,z,x,y+3,z,AIR)
                        if reach==11 or abs(offset)==4 and reach>7:
                            v.set(x,y+1,z,RAIL)
                        if abs(offset) in (2,4) and reach in (8,9,10):
                            v.box(x,y-1-(10-reach),z,x,y-1,z,STONE)
                        if reach==11 and abs(offset)==4:
                            v.set(x,y+2,z,LAMP)
            if y != -40:
                for offset in range(-7,8):
                    x,z=CX+sx*8-sz*offset,CZ+sz*8+sx*offset
                    v.set(x,top,z,COPPER if abs(offset)<5 else STONE)
        if y == -40:
            v.box(CX-8, top+1, CZ-8, CX+8, top+1, CZ+8, PATINA)
            for step in range(1, 5):
                w = 8-step
                v.box(CX-w, top+1+step, CZ-w, CX+w, top+1+step, CZ+w, PATINA)
            v.box(CX-2, top+1, CZ-2, CX+2, top+1, CZ+2, S("ochre_froglight"))
    # Three paired stair flights inside the bastion connect every bridge level
    # and the lantern room. Clear each head opening through the upper floor.
    for y in TIERS:
        for step in range(1, 8):
            for x, z, floor, direction in ((CX-5+step,CZ-4,y+step,"east"),
                                           (CX+3-step,CZ+3,y+7+step,"west")):
                for zz in (z,z+1):
                    v.box(x, floor+1, zz, x, floor+3, zz, AIR)
                    v.set(x, floor, zz, S("spruce_stairs", facing=direction))
        v.box(CX+3,y+7,CZ-4,CX+4,y+7,CZ+4,PLANK)
        v.set(CX+5,y+8,CZ,S("wall_torch", facing="west"))
        v.set(CX+6,y+8,CZ,BRICK)
        for x in (CX-5,CX+5):
            v.set(x,y+9,CZ-5,S("ochre_froglight"))
            v.set(x,y+10,CZ-5,S("spruce_slab",type="bottom"))
            v.box(x,y+1,CZ-5,x,y+8,CZ-5,POST)


def workshop(v, direction, y, offset, station, craft, roof):
    """A terrace-facing shop with stepped gable, stained-glass transom and eaves."""
    # Template grows south starting at its front door. The signed side offset
    # spreads different-width gables around the cavern's four cardinal walls.
    dx, dz = SIDES[direction]
    reach = round(math.sqrt(35**2 - offset**2))
    origin_x, origin_z = CX + dx*reach - dz*offset, CZ + dz*reach + dx*offset

    def pos(a, h, depth):
        return origin_x - dz*a + dx*depth, y + h, origin_z + dx*a + dz*depth

    def put(a, h, depth, material):
        v.set(*pos(a,h,depth), material)

    half = 4 if offset else 5
    toward = turn_facing("west",direction)
    for a in range(-half,half+1):
        for depth in range(0,7):
            put(a,0,depth,TRIM if depth==0 else PLANK)
            for h in range(1,6):
                wall = abs(a)==half or depth in (0,6)
                state = S("mud_bricks") if y == UPPER else BRICK
                if abs(a) == half and depth % 3 == 0:
                    state = POST
                if depth==0 and abs(a)<=1 and h <= 3+(a==0):
                    state = AIR
                elif depth==0 and 2<=abs(a)<half and 2<=h<=4:
                    state = S("yellow_stained_glass_pane")
                put(a,h,depth,state if wall else AIR)
            put(a,6,depth,PLANK)
    for a in range(-half-1,half+2):
        high = 7+(half+1-abs(a))//2
        for depth in range(-1,8):
            for h in range(7,high):
                put(a,h,depth,S("stripped_dark_oak_log",axis="y") if depth in (0,6) else PLANK)
            put(a,high,depth,roof)
    for a in (-half, half):
        for h in range(1,6):
            put(a,h,-1,POST)
        put(a,6,-1,COPPER)
        put(a,5,-2,S("spruce_fence"))
        put(a,6,-2,PLANK)
        put(a,4,-2,S("lantern",hanging=True))
    # A player can stand at each usable block. All stations are vanilla public
    # workstation menus with per-player inputs and no persisted town inventory.
    put(-2,1,3,S(station))
    put(2,1,4,S(craft))
    put(-2,2,5,S("ochre_froglight"))
    put(2,1,5,S("spruce_stairs",facing=toward))
    return {"block":station,"position":list(pos(-2,1,3)),"approach":list(pos(-1,1,3))}


def deep_stair(v):
    # A small covered landing on the north parapet of the NEW stone bridge.
    # No main gate blocks, existing city streets or resident buildings touched.
    v.box(-13,-2,85,-8,0,93,BRICK)
    v.box(-13,0,85,-8,0,93,TRIM)
    v.box(-12,1,86,-9,4,93,AIR)
    for x in (-13,-8):
        for z in (85,92):
            v.box(x,1,z,x,4,z,POST)
            v.set(x,5,z,COPPER)
    v.box(-14,5,84,-7,5,93,TILE)
    v.box(-13,6,85,-8,6,92,COPPER)
    v.set(-12,4,87,S("lantern",hanging=True))
    # Descend east, leaving the old gate foundations overhead. The masonry roof
    # only follows the stair until it breaks through the cavern at the west rim.
    for x in range(-8,CX-29):
        floor = max(UPPER,-(x+8))
        for z in range(85,92):
            border = z in (85,91)
            if border:
                if x <= CX-40:
                    v.box(x,floor-1,z,x,floor+5,z,BRICK if x%6 else POST)
            else:
                v.set(x,floor-1,z,BRICK)
                v.set(x,floor,z,S("stone_brick_stairs",facing="west") if -8<x<=46 else STONE)
                v.box(x,floor+1,z,x,floor+4,z,AIR)
                if x <= CX-40:
                    v.set(x,floor+5,z,TRIM if x%6==0 else TILE)
                    if z==86 and x%6==0:
                        v.set(x,floor+4,z,S("lantern",hanging=True))


def seal(v):
    # Close each exposed face only, including vertical jumps in the irregular
    # rock roof and the rooms hewn out beyond the cave. The bridge entrance
    # adjoins the outdoor air; all other authored air/water is sealed.
    skin = set()
    for (x,y,z),state in v.b.items():
        if state not in (AIR, WATER) or y>=0:
            continue
        for dx,dy,dz in NEIGHBORS:
            q = (x+dx,y+dy,z+dz)
            if q not in v.b:
                skin.add(q)
    for x,y,z in skin:
        v.set(x,y,z,rock(x,y,z))


def build_mine(v):
    """Append mine cells and return local landmarks for integration and testing."""
    mine = Voxels()
    chamber(mine)
    cavern_details(mine)
    for y,inner,axis in ((BASIN,19,"x"),(MARKET,23,"x"),(UPPER,28,"z")):
        gallery(mine,y,inner,axis)
        bridge(mine,y,axis,inner)
    watch_house(mine)
    terrace_stairs(mine,CX+28,CZ-9,MARKET)
    terrace_stairs(mine,CX-26,CZ+4,BASIN)
    waterworks(mine)
    hoist(mine)
    utility_rooms = []
    for direction, y, offset, station, craft, roof in (
        (0, UPPER, -13, "loom","crafting_table",PATINA),
        (1, UPPER, 9, "cartography_table","loom",S("dark_oak_planks")),
        (3, UPPER, 12, "crafting_table","cartography_table",COPPER),
        (0, MARKET, 0, "smithing_table","grindstone",S("polished_blackstone_bricks")),
        (1, MARKET, -11, "stonecutter","crafting_table",COPPER),
        (2, MARKET, 11, "loom","crafting_table",S("dark_oak_planks")),
        (3, MARKET, -11, "cartography_table","smithing_table",PATINA),
        (0, BASIN, 12, "stonecutter","grindstone",COPPER),
        (2, BASIN, -9, "smithing_table","stonecutter",TILE),
    ):
        utility_rooms.append(workshop(mine,direction,y,offset,station,craft,roof))
    # Sheltered stalls occupy the broad intermediate landings, in the gaps
    # between the workshop doors and clear of bridgeheads and stair landings.
    for x,z,color in ((-25,-25,"green"),(17,-25,"red"),(20,18,"yellow"),(-25,19,"cyan")):
        market_pitch(mine,CX+x,MARKET,CZ+z,color)
    deep_stair(mine)
    seal(mine)
    # Overlap with the new bridge is intentional solely at its north parapet.
    # Merging individual cells leaves all un-authored terrain alone.
    v.b.update(mine.b)
    return {"id":"deepstead", "bounds":list(mine.bounds()), "entrance":[-10,1,93],
            "tiers":[BASIN,MARKET,UPPER], "utilities":utility_rooms,
            "landmarks":{"upper_gallery":[CX-31,UPPER+1,CZ+2],
                         "watch_lantern":[CX,-39,CZ], "exchange":[CX,MARKET+1,CZ],
                         "waterworks_quay":[CX+23,BASIN+1,CZ], "south_bridge":[CX,UPPER+1,CZ+16]}}

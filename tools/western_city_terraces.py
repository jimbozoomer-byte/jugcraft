"""Terrain-informed architectural alternative, exported to build/ and a test-only fixture for review.

This is not an installation plan. The live basin contains water, builds and
archaeology; the production installer deliberately refuses this site's flat
prototype. Reuse the authored buildings without quietly relaxing that refusal.
"""
from pathlib import Path
import gzip
import json
import random
from western_city import apartment, shop, undercity, stair_portal, lamp, payload, AIR
from town_voxels import Voxels, S, parse, connections, stair_shapes
import town_buildings
from town import Town, signed_distance, tree, WALL_TOP

ROOT=Path(__file__).resolve().parents[1]
# Reference-copy block entities plus a generous local design exclusion. These
# are not proof that all owner buildings, entities or protected land were found.
PRESERVE=[
    {'reason':'existing surface chest and surrounding ground','bounds':[-169,-56,108,-154,-40,125]},
    {'reason':'archaeology pocket','bounds':[-55,-46,75,-43,-34,87]},
    {'reason':'southern underground spawner','bounds':[-70,-60,170,-58,-48,182]},
]


# Pier bottoms below the default Y56, derived from all nine footing columns
# in the November 16 read-only reference. These are design elevations, not
# permission to overwrite terrain; fresh survey/ownership checks remain required.
FOOTINGS = {
    (-190,20): -59,
    (-190,36): -66,
    (-190,52): -92,
    (-190,68): -90,
    (-190,100): -69,
    (-190,116): -67,
    (-190,132): -74,
    (-190,148): -68,
    (-190,164): -63,
    (-174,20): -79,
    (-174,36): -94,
    (-174,84): -62,
    (-174,148): -67,
    (-174,164): -62,
    (-158,148): -69,
    (-158,164): -62,
    (-142,116): -59,
    (-142,132): -64,
    (-142,148): -61,
    (-142,164): -64,
    (-126,116): -65,
    (-126,132): -63,
    (-126,164): -68,
    (-110,20): -59,
    (-110,100): -61,
    (-110,116): -72,
    (-110,132): -61,
    (-94,20): -64,
    (-94,36): -66,
    (-94,52): -68,
    (-94,68): -68,
    (-94,84): -73,
    (-94,100): -68,
    (-94,116): -70,
    (-94,132): -61,
    (-78,68): -69,
    (-78,84): -71,
    (-78,100): -74,
    (-78,116): -70,
    (-78,164): -65,
    (-62,84): -64,
    (-62,100): -69,
    (-62,116): -66,
    (-62,148): -63,
    (-30,132): -82,
}


def street_height(x):
    if x<=-151:return -48
    if x<=-133:return x+103
    if x<=-94:return -30
    if x<=-76:return x+64
    if x<=-28:return -12
    if x<=-16:return x+16
    return 0


# Sixteen changes of direction echo the old city's irregular curtain. The long
# north and south arcs leave room for the previously surveyed apartment courts.
RING_X_OFFSET = -219
RING_VERTICES = (
    (-33,96), (-39,133), (-47,158), (-70,179),
    (-120,188), (-157,182), (-181,170), (-201,147),
    (-213,96), (-206,58), (-196,36), (-182,20),
    (-124,18), (-78,20), (-54,25), (-38,44),
)


def outer_ring():
    ring = Town(seed=1908)
    ring.points = [(x - RING_X_OFFSET, z) for x, z in RING_VERTICES]
    ring.sd = signed_distance(ring.points)
    ring.wall(gates={0: "east", 8: "west"})
    return ring


def inside_curtain(ring, x, z):
    return ring.sdist(x - RING_X_OFFSET, z) <= 0


def place_curtain(v, ring):
    # Every tower stands on a level court. Only the masonry between towers
    # changes elevation, with east-facing stairs along its uphill wall walk.
    for (gx, height, z), state in ring.v.b.items():
        x = gx + RING_X_OFFSET
        floor = street_height(x)
        if height == 0:
            # Stop each curtain footing at its surveyed strip elevation.
            bottom = min(floor, curtain_footing(x, z))
            v.box(x, bottom, z, x, floor, z, S("stone_bricks"))
        if height == WALL_TOP and street_height(x - 1) < floor:
            state = S("stone_brick_stairs", facing="east")
        v.set(x, floor + height, z, state)


# Lowest bearing elevation at authored wall feet in each 8x8 survey square.
# Coordinates are town-local. Authored elevations keep layout generation offline.
CURTAIN_BEARING = {
    (-27, 6): -69, (-27, 7): -68, (-27, 8): -70, (-27, 9): -70, (-27, 10): -70,
    (-27, 11): -74, (-27, 12): -70, (-27, 13): -71, (-27, 14): -70, (-26, 5): -69,
    (-26, 6): -72, (-26, 7): -94, (-26, 8): -96, (-26, 9): -70, (-26, 10): -70,
    (-26, 11): -72, (-26, 12): -71, (-26, 13): -73, (-26, 14): -72, (-26, 15): -69,
    (-26, 16): -71, (-26, 17): -67, (-26, 18): -68, (-25, 3): -58, (-25, 4): -63,
    (-25, 5): -68, (-25, 6): -71, (-25, 17): -68, (-25, 18): -69, (-25, 19): -69,
    (-24, 2): -67, (-24, 3): -64, (-24, 4): -61, (-24, 19): -69, (-24, 20): -66,
    (-24, 21): -63, (-23, 2): -66, (-23, 3): -65, (-23, 20): -63, (-23, 21): -63,
    (-22, 2): -85, (-22, 21): -61, (-22, 22): -50, (-21, 2): -87, (-21, 21): -55,
    (-21, 22): -50, (-21, 23): -51, (-20, 2): -50, (-20, 22): -51, (-20, 23): -51,
    (-19, 2): -49, (-19, 22): -49, (-19, 23): -46, (-18, 2): -53, (-18, 22): -47,
    (-18, 23): -46, (-17, 2): -55, (-17, 22): -46, (-17, 23): -46, (-16, 1): -61,
    (-16, 2): -61, (-16, 23): -44, (-15, 2): -60, (-15, 23): -42, (-14, 2): -61,
    (-14, 22): -40, (-14, 23): -40, (-13, 2): -68, (-13, 22): -39, (-13, 23): -39,
    (-12, 2): -68, (-12, 22): -39, (-12, 23): -38, (-11, 2): -54, (-11, 22): -37,
    (-10, 2): -48, (-10, 21): -35, (-10, 22): -36, (-9, 2): -45, (-9, 3): -45,
    (-9, 21): -34, (-9, 22): -34, (-8, 2): -45, (-8, 3): -45, (-8, 20): -34,
    (-8, 21): -33, (-7, 2): -40, (-7, 3): -40, (-7, 4): -38, (-7, 19): -35,
    (-7, 20): -34, (-6, 3): -37, (-6, 4): -37, (-6, 5): -37, (-6, 6): -36,
    (-6, 15): -39, (-6, 16): -41, (-6, 17): -40, (-6, 18): -36, (-6, 19): -35,
    (-6, 20): -34, (-5, 5): -37, (-5, 6): -36, (-5, 7): -35, (-5, 8): -32,
    (-5, 9): -32, (-5, 10): -32, (-5, 11): -35, (-5, 12): -38, (-5, 13): -39,
    (-5, 14): -39, (-5, 15): -38, (-5, 16): -39, (-5, 17): -38, (-4, 11): -31,
    (-4, 12): -35, (-4, 13): -36,
}


def curtain_footing(x, z):
    return CURTAIN_BEARING[(x // 8, z // 8)]


def gate_bridge(v):
    # The old gate floor starts at X17. Leave a five-block lane clear throughout
    # and carry it across the original ravine on three open masonry arches.
    for x in range(-32, 17):
        floor = street_height(x)
        v.box(x, floor, 93, x, floor, 99, S("stone_bricks"))
        if street_height(x - 1) < floor:
            v.box(x, floor, 94, x, floor, 98, S("stone_brick_stairs", facing="east"))
        for z in (93, 99):
            v.set(x, floor + 1, z, S("stone_brick_wall"))
        # The two longitudinal arch ribs join the deck at their crowns.
        distance = min(abs(x - pier) for pier in (-30, -15, 0, 15))
        depth = (12, 9, 6, 5, 4, 3, 2, 2)[distance]
        for z in (93, 99):
            v.box(x, floor - depth, z, x, floor - 1, z, S("stone_bricks"))
        v.box(x, floor - 1, 94, x, floor - 1, 98, S("polished_andesite"))
        if distance <= 1:
            bottom = -36 if x < 3 else -12
            v.box(x, min(floor - depth, bottom), 93, x, floor - depth, 99, S("stone_bricks"))


def passages(v,segments):
    # Build all shells before carving any junction, so a later tunnel's end wall
    # cannot close an earlier tunnel at a T junction or turning landing.
    for x0,z0,x1,z1 in segments:v.box(x0-1,-76,z0-1,x1+1,-69,z1+1,S('deepslate_bricks'))
    for x0,z0,x1,z1 in segments:
        v.box(x0,-74,z0,x1,-74,z1,S('stone_bricks'))
        v.box(x0,-73,z0,x1,-70,z1,AIR)


def courtyard_detail(v):
    rng=random.Random(16112026)
    # Reuse the town's tested contained fountain and original planted trees.
    p=town_buildings.fountain()
    v.box(-121,-30,70,-105,-30,86,S('polished_andesite'))
    v.paste(p.v,-117,-30,74,origin_size=(p.w,p.d))
    for x,y,z,kind in [(-183,-48,78,'oak'),(-125,-30,76,'birch'),(-101,-30,82,'oak'),
                        (-70,-12,87,'oak'),(-44,-12,86,'birch'),(-135,-30,157,'spruce')]:
        # The last tree stands on the actual sloped terrace surface.
        y=street_height(x)
        t=Voxels();tree(t,rng,0,0,kind)
        v.paste(t,x,y,z,origin_size=(1,1))
        v.set(x,y,z,S('rooted_dirt'))
        for dx,dz in ((-1,-1),(-1,1),(1,-1),(1,1)):
            v.set(x+dx,y,z+dz,S('dirt'))
            v.set(x+dx,y+1,z+dz,S('fern'))
    for x,y,z,facing in [(-120,-29,78,'east'),(-106,-29,78,'west'),
                          (-178,-47,78,'west'),(-58,-11,85,'east')]:
        for dz in (-2,0,2):v.set(x,y,z+dz,S('jugcraft:crafted_oak_chair',facing=facing))
    # A sheltered three-pitch market beside the middle court's lower lane.
    for x in (-128,-117,-106):
        p=town_buildings.stall(rng,w=6,d=5)
        for at,s in list(p.v.b.items()):
            if parse(s)[0]=='minecraft:barrel':p.v.b[at]=S('spruce_planks')
        for site in p.sites:
            for xx,yy,zz,slot in site['blocks']:p.v.set(xx,yy,zz,S('orange_wool' if slot==0 else 'white_wool'))
        v.box(x-1,-30,135,x+6,-30,143,S('stone_bricks'))
        v.paste(p.v,x,-30,137,origin_size=(p.w,p.d))
    # Low flower beds occupy the sloping garden strips without blocking stairs.
    for x in (-148,-142,-136,-91,-85,-79):
        y=street_height(x)
        for z in (31,48,119,161):
            v.box(x,y,z,x+1,y,z+3,S('dirt'))
            for zz in range(z,z+4):
                v.set(x,y+1,zz,S('poppy' if zz%2 else 'dandelion'))
            v.box(x-1,y,z-1,x-1,y,z+4,S('mossy_stone_bricks'))


def build_terraced(*, expanded=False):
    v=Voxels();rooms=[];plots=[]
    # The quay is four blocks above the observed waterline. Higher courts step
    # back into the eastern hillside; they no longer float at the old gate level.
    ring = outer_ring()
    for x in range(-216, -28):
        y = street_height(x)
        for z in range(6, 191):
            if not inside_curtain(ring, x, z):
                continue
            v.box(x,y-3,z,x,y-1,z,S('stone'))
            v.set(x,y,z,S('grass_block'))
            v.box(x,y+1,z,x,y+37,z,AIR)
            if 65<=z<=69 or 92<=z<=100 or 144<=z<=148:
                state=S('stone_brick_stairs',facing='east') if street_height(x-1)<y else S('stone_bricks')
                v.set(x,y,z,state)
            if z in (90,102):
                v.set(x,y,z,S('stone_bricks'))
                v.set(x,y+1,z,S('stone_brick_wall'))
    # Widely spaced masonry piers support the raised courts while leaving
    # existing below-ground tunnels open between them. The read-only impact
    # check still has to reject any pier that intersects an existing build.
    for x in range(-190,-15,16):
        for z in range(20,170,16):
            # Avoid the reference mine's torch and cobblestone at these piers.
            px=-42 if (x,z)==(-46,52) else x
            pz=23 if (x,z)==(-30,20) else z
            if not inside_curtain(ring, px, pz):continue
            v.box(px-1,FOOTINGS.get((px,pz),-58),pz-1,px+1,street_height(px)-4,pz+1,S('stone_bricks'))
    for x in range(-188,-43,12):
        for z in (90,102):lamp(v,x,street_height(x)+1,z)
    # Three independent courts and normal stairs, with stable room identities.
    for num,(x,y,z0,tone,roof) in enumerate(((-182,-48,28,'bricks','dark_oak'),(-129,-30,25,'white_terracotta','spruce'),(-74,-12,45,'mud_bricks','dark_oak')),1):
        house,local=apartment(tone,roof,num,storeys=(8,7,6)[num-1] if expanded else 5)
        v.paste(house,x,y,z0,turns=2,origin_size=(31,37))
        for r in local:
            a,b,c,d,e,f=r['bounds'];dx,dy,dz=r['door']
            rooms.append({'id':r['id'],'bounds':[x+30-d,y+b,z0+36-f,x+30-a,y+e,z0+36-c],
                          'door':[x+30-dx,y+dy,z0+36-dz]})
        # Unobstructed paths between apartment courts and the main stair avenue.
        for xx in (x+7,x+23):
            v.box(xx-2,y,z0+37,xx+2,y,144,S('stone_bricks'))
            for z in (90,102):v.box(xx-2,y+1,z,xx+2,y+3,z,AIR)
    # Lower chest site becomes an unbuilt courtyard, not a demolished shop plot.
    for id,x,y,z,w,d,roof in (
        ('furniture',-188,-48,111,15,23,'spruce'),('portraits',-172,-48,146,19,19,'dark_oak'),
        ('exchange',-129,-30,111,31,23,'dark_oak'),('post',-129,-30,151,31,19,'spruce'),
        ('tailor',-72,-12,111,27,23,'spruce'),('stables',-75,-12,145,23,15,'dark_oak')):
        b=Voxels();shop(b,0,0,w,d,roof,'stone_bricks' if id=='stables' else 'bricks')
        if id=='stables':
            b.box(w//2-2,1,-3,w//2+2,5,3,AIR)
            b.box(2,1,5,w-3,4,d-2,AIR)
            for xx in (2,9,16,w-3):b.box(xx,1,6,xx,2,d-2,S('spruce_fence'))
            for xx in (5,12,18):b.box(xx,1,d-4,xx+1,1,d-2,S('hay_block'))
        v.paste(b,x,y,z,origin_size=(1,1))
        plots.append({'id':id,'bounds':[x,y+1,z,x+w-1,y+11,z+d-1]})
    # Paved landing between the lower court and the waterfront gatehouse.
    v.box(-213,-48,94,-185,-48,98,S('cobblestone'))
    # Ground the undercity beneath the waterline instead of leaving it suspended
    # under the old elevation. Its roof stays below the quay floor.
    u=Voxels();undercity(u)
    # The initial west/east halls intersected recorded mine stairs and a door.
    # Move their entire rooms north; retain those old tunnel volumes intact.
    wings=[]
    for x0,x1 in ((-182,-143),(-61,-22)):
        wing=Voxels()
        for p in list(u.b):
            x,y,z=p
            if x0<=x<=x1 and 88<=z<=124 and -28<=y<=-16:
                wing.b[(x,y,z-66)]=u.b.pop(p)
        wing.walls(x0,-25,22,x1,-18,58,S('deepslate_bricks'))
        wings.append(wing)
    # Remove the abandoned hall connectors outside the rotunda's actual shell.
    for p in list(u.b):
        x,y,z=p
        if (-174<=x<=-158 and 64<=z<=95) or (-44<=x<=-30 and 64<=z<=95 and y< -16):
            del u.b[p]
    for wing in wings:u.paste(wing,origin_size=(1,1))

    # The prototype's high eastern entry is replaced with a second quay entry.
    for p in list(u.b):
        x,y,z=p
        if -44<=x<=-30 and 64<=z<=87 and y>=-25:del u.b[p]
    v.paste(u,0,-48,0,origin_size=(1,1))
    original_entry=Voxels();stair_portal(original_entry,-161,69)
    v.paste(original_entry,0,-48,0,origin_size=(1,1))
    # Western stair is shifted to (-161,69) clear of the recorded mine; second entrance faces north
    # from the lower southern court, connected directly to the west service hall.
    lower=Voxels();stair_portal(lower,-189,149);v.paste(lower,0,-48,0,origin_size=(1,1))
    passages(v,[(-184,54,-182,148),(-184,54,-177,56),(-156,56,-154,68),
                (-145,52,-129,54),(-131,52,-129,86),(-75,52,-60,54),(-75,52,-73,86)])
    v.box(-184,-73,148,-182,-70,149,AIR)
    v.box(-180,-73,54,-176,-70,56,AIR)
    v.box(-156,-73,55,-154,-70,69,AIR)
    for x0,x1 in ((-131,-129),(-75,-73)):
        v.box(x0,-73,84,x1,-70,87,AIR)
    for x0,x1 in ((-146,-143),(-61,-59)):
        v.box(x0,-73,52,x1,-70,54,AIR)
    # Walkways to both lower entries and the western avenue.
    for x0,z0,x1,z1 in ((-163,63,-155,68),(-181,145,-177,148),(-188,145,-178,148),(-181,103,-177,145)):
        v.box(x0,-48,z0,x1,-48,z1,S('stone_bricks'))
    courtyard_detail(v)
    place_curtain(v, ring)
    gate_bridge(v)
    # Every dry underground edge needs its own closed shell. The original flat
    # review relied on surrounding terrain at a few vaulted-roof/passage ends;
    # the actual site's aquifer exposed those openings. Keep the quay entrances
    # open above water and close only unauthored neighbors below that elevation.
    shell=set()
    for (x,y,z),state in list(v.b.items()):
        if state!=AIR or y>=-49:continue
        for dx,dy,dz in ((1,0,0),(-1,0,0),(0,1,0),(0,-1,0),(0,0,1),(0,0,-1)):
            q=x+dx,y+dy,z+dz
            if q not in v.b:shell.add(q)
    for q in shell:v.set(*q,S('deepslate_bricks'))
    if expanded:
        from western_city_mine import build_mine
        from western_city_port import build_port
        mine = build_mine(v)
        plots.append({'id':mine['id'], 'bounds':mine['bounds']})
        plots.extend(build_port(v))
    # Avoid putting pavement over the recorded chest. This preserves the entire
    # supplied pocket, not merely the container's one block or its item contents.
    for zone in PRESERVE:
        x0,y0,z0,x1,y1,z1=zone['bounds']
        for x in range(x0,x1+1):
            for y in range(y0,y1+1):
                for z in range(z0,z1+1):v.remove(x,y,z)
    for (x,y,z),s in list(v.b.items()):
        if parse(s)[0]=='minecraft:grass_block' and v.get(x,y+1,z,AIR)!=AIR:v.set(x,y,z,S('stone_bricks'))
    connections(v);stair_shapes(v)
    return v,rooms,plots


def main():
    v,rooms,plots=build_terraced();data=payload(v,rooms,plots)
    # Close the structure before opening the dry volume in the disposable review.
    # A production migration still needs before-images and transient-fluid checks.
    def shell_first(cell):
        name=data['palette'][cell[3]].split('[',1)[0]
        phase=1 if name=='minecraft:air' else 3 if name in ('minecraft:water','minecraft:lava') else 2 if name.endswith(('_door','_bed')) else 0
        return phase,cell[1],cell[0],cell[2]
    data['cells'].sort(key=shell_first)
    data.update(installation='not_installable_site_review',siteReferenceOrigin=[1296,114,400],
                proposedFloorOffsets=[-48,-30,-12],preserve=PRESERVE,
                limitations=['Water/seabed and retaining structures need a fresh stopped-world survey.',
                             'Entity/ownership and all non-terrain structures must be reviewed.',
                             'No live apply command accepts this architectural alternative.'])
    folder=ROOT/'build/western-city-plan';folder.mkdir(parents=True,exist_ok=True)
    raw=json.dumps(data,separators=(',',':')).encode('utf-8')
    packed=bytearray(gzip.compress(raw,mtime=0));packed[9]=255
    (folder/'terraced-review.json.gz').write_bytes(packed)
    fixture=ROOT/'src/gametest/resources/data/jugcraft/test/western_terraced_review.json.gz'
    fixture.parent.mkdir(parents=True,exist_ok=True);fixture.write_bytes(packed)
    print(f'Terraced review only: {len(v.b)} cells, {len(rooms)} rooms, floors 66/84/102 at the saved live origin. No runtime asset or world edited.')


if __name__=='__main__':main()

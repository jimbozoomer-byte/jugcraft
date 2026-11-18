"""Offline western district blueprint. Never edits a save or the old town asset.

Uses the town's original voxel/state helpers. Explicit air is excavation, not
permission to overwrite land: runtime installation needs a reviewed survey.
"""
from pathlib import Path
import gzip
import json
import math
from town_voxels import Voxels, S, parse, stair_shapes, connections

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / 'src/main/resources/data/jugcraft/town/western_district.json.gz'
AIR = S('air')


def door(v, x, y, z, facing):
    for half, dy in [('lower', 0), ('upper', 1)]:
        v.set(x, y + dy, z, S('spruce_door', facing=facing, half=half, hinge='left', open=False))


def lamp(v, x, y, z):
    v.set(x, y, z, S('stone_bricks'))
    for h in range(1, 4):
        v.set(x, y+h, z, S('spruce_fence'))
    v.set(x, y+4, z, S('lantern'))


def apartment(tone, roof, number, storeys=5):
    v = Voxels()
    roof_y = storeys * 5
    v.box(0, 0, 0, 30, roof_y + 7, 36, AIR)
    v.box(0, -2, 0, 30, 0, 36, S('stone_bricks'))
    rooms = []
    for floor in range(storeys):
        y = floor*5
        v.box(0, y, 0, 30, y, 36, S('spruce_planks'))
        v.walls(0, y+1, 0, 30, y+4, 36, S(tone))
        # Stone sill/string courses, buttressed corners and distinct window bays.
        v.walls(0, y+1, 0, 30, y+1, 36, S('stone_bricks'))
        for x in (0, 10, 20, 30):
            for z in (0, 36):
                v.box(x, y+1, z, x, y+4, z, S('polished_andesite'))
        for x in (4, 14, 24):
            for z in (0, 36):
                v.box(x, y+2, z, x+2, y+3, z, S('glass'))
                v.box(x-1, y+4, z, x+3, y+4, z, S('spruce_slab', type='top'))
        for z in (5, 12, 22, 29):
            for x in (0, 30):
                v.box(x, y+2, z, x, y+3, z+2, S('glass'))
        for x in (13, 19):
            v.box(x, y+1, 1, x, y+4, 35, S('white_terracotta'))
        v.box(14, y+1, 1, 18, y+1, 27, S('red_carpet'))
        for z in (4, 17, 30):
            v.set(16, y+4, z, S('lantern', hanging=True))
        for side, x0, x1, wall, facing in [('L', 1, 12, 13, 'east'), ('R', 20, 29, 19, 'west')]:
            if floor:
                v.box(x0, y+1, 16, x1, y+4, 16, S('white_terracotta'))
            for idx, z0, z1, entrance in [(1, 1, 15, 8), (2, 17, 35, 23)]:
                if not floor and idx == 2:
                    continue
                door(v, wall, y+1, entrance, facing)
                if floor:
                    bx=x0+2
                    v.set(bx, y+1, z0+3, S('red_bed', facing='south', part='foot'))
                    v.set(bx, y+1, z0+4, S('red_bed', facing='south', part='head'))
                    v.set(x1-2, y+1, z0+3, S('jugcraft:crafted_oak_table'))
                    v.set(x1-2, y+2, z0+3, S('lantern'))
                    v.set(x1-2, y+1, z0+5, S('jugcraft:crafted_oak_chair', facing='north'))
                    v.box(x0+2, y+1, z1-2, x0+4, y+1, z1-2, S('bookshelf'))
                    # Readable inhabited rooms: real light sources, bedside trim and
                    # a woven runner, using the existing game's materials/furniture.
                    v.set((x0+x1)//2,y+4,(z0+z1)//2,S('lantern',hanging=True))
                    v.box(x0+1,y+1,z0+1,x0+4,y+1,z0+1,S('dark_oak_slab',type='top'))
                    v.set(x0+1,y+2,z0+1,S('potted_fern'))
                    v.box(x0+4,y+1,z0+5,x0+5,y+1,z0+8,S('red_carpet'))
                    v.box(x1-2,y+1,z1-2,x1-1,y+3,z1-2,S('stripped_spruce_log',axis='y'))
                    v.box(x1-3,y+4,z1-3,x1,y+4,z1-1,S('spruce_slab',type='top'))
                    rooms.append({'id':f'west_{number}_{floor}{side}{idx}', 'bounds':[x0,y+1,z0,x1,y+4,z1],
                                  'door':[wall,y+1,entrance]})
                else:
                    v.box(x0+2, y+1, 11, x1-2, y+1, 11, S('spruce_planks'))
                    for z in (3, 7, 18, 25):
                        v.set(x0+2, y+1, z, S('potted_fern'))
        door(v, 16, y+1, 0, 'north') if floor == 0 else None
    # Projected masonry piers, balconies and a sheltered entrance give the facade depth.
    for x in (0,10,20,30):
        v.box(x,1,-1,x,roof_y-1,-1,S('polished_andesite'))
        v.set(x,roof_y-1,-2,S('stone_brick_stairs',facing='south',half='top'))
    for floor in range(1, storeys, 2):
        y=floor*5
        for x in (5,25):
            v.box(x-2,y,-3,x+2,y,-1,S('stone_brick_slab',type='top'))
            v.box(x-2,y+1,-3,x+2,y+1,-3,S('iron_bars'))
            for xx in (x-2,x+2):v.box(xx,y+1,-2,xx,y+1,-1,S('iron_bars'))
            door(v,x,y+1,0,'north')
    v.box(13,3,-3,19,3,-1,S('dark_oak_slab',type='top'))
    for x in (13,19):v.box(x,1,-3,x,2,-3,S('spruce_fence'))
    for x in (3,8,23,28):
        v.set(x,1,-1,S('mossy_stone_bricks'))
        v.set(x,2,-1,S('potted_fern'))
    # Cut after all the floors are in. The tall variants continue out onto the roof.
    if storeys != 5:
        from western_city_roofs import finish_tenement
        finish_tenement(v, number, storeys)
    for floor in range(storeys if storeys != 5 else storeys-1):
        y = floor*5
        v.box(15,y+1,28,17,y+8,32,AIR)
        for i in range(5):
            v.box(15,y+i+1,28+i,17,y+i+1,28+i,S('spruce_stairs',facing='south'))
        v.box(14,y+5,33,18,y+5,35,S('spruce_planks'))
    if storeys != 5:
        return v, rooms
    v.box(-1,25,-1,31,25,37,S('stone_brick_slab',type='bottom'))
    # Three pitched frontage gables; closed stepped roof and a narrower rear roof.
    for start in (0,10,20):
        for step in range(6):
            for x in {start+step,start+10-step}:
                v.box(x,26+step,-1,x,26+step,37,S(roof+'_stairs',facing='east' if x==start+step else 'west'))
            v.box(start+step,25+step,0,start+10-step,25+step,0,S(tone))
            v.box(start+step,25+step,36,start+10-step,25+step,36,S(tone))
        v.set(start+5,28,0,S('glass'))
    for x in (3,27):
        v.box(x,27,29,x+1,34,30,S('bricks'))
        v.box(x-1,35,28,x+2,35,31,S('stone_brick_slab'))
    return v, rooms


def shop(v, x, z, w, d, roof='spruce', stone='bricks'):
    v.box(x,1,z,x+w-1,12,z+d-1,AIR)
    v.box(x,0,z,x+w-1,0,z+d-1,S('stone_bricks'))
    v.walls(x,1,z,x+w-1,5,z+d-1,S(stone))
    v.box(x,6,z,x+w-1,6,z+d-1,S('spruce_planks'))
    for dx in range(3,w-3,6):
        v.box(x+dx,2,z,x+dx+2,4,z,S('glass'))
    door(v,x+w//2,1,z,'north')
    for step in range(5):
        for zz, facing in [(z-1+step,'south'),(z+d-step,'north')]:
            v.box(x-1,7+step,zz,x+w,7+step,zz,S(roof+'_stairs',facing=facing))
    v.box(x-1,11,z+4,x+w,11,z+d-5,S(roof+'_slab',type='top'))
    for dx in (3,w-4):
        v.set(x+dx,5,z+3,S('iron_chain'))
        v.set(x+dx,4,z+3,S('lantern',hanging=True))
        v.box(x+dx,1,z+d-3,x+dx+1,2,z+d-3,S('bookshelf'))
    for dx in (0,w-1):v.box(x+dx,1,z-1,x+dx,5,z-1,S('stripped_spruce_log',axis='y'))
    v.box(x+w//2-2,3,z-3,x+w//2+2,3,z-1,S('spruce_slab',type='top'))
    for dx in (w//2-2,w//2+2):v.box(x+dx,1,z-3,x+dx,2,z-3,S('spruce_fence'))
    for dx in (3,w-4):
        v.set(x+dx,1,z-1,S('mossy_stone_bricks'))
        v.set(x+dx,2,z-1,S('potted_azalea_bush'))


def stair_portal(v, x, z):
    # Two parallel flights of thirteen, with a three-block turning landing.
    v.box(x-1,-27,z-1,x+9,6,z+17,S('stone_bricks'))
    v.box(x,1,z,x+8,5,z+16,AIR)
    for i in range(13):
        v.box(x+1,-i,z+i,x+3,-i+4,z+i,AIR)
        v.box(x+1,-i-1,z+i,x+3,-i-1,z+i,S('stone_brick_stairs',facing='north'))
        v.box(x+5,-14-i,z+12-i,x+7,-10-i,z+12-i,AIR)
        v.box(x+5,-14-i,z+12-i,x+7,-14-i,z+12-i,S('stone_brick_stairs',facing='south'))
    v.box(x+1,-13,z+13,x+7,-13,z+15,S('stone_bricks'))
    v.box(x+1,-12,z+13,x+7,-9,z+15,AIR)
    v.box(x+5,-25,z-1,x+7,-22,z,AIR)
    v.box(x+1,1,z-1,x+3,4,z,AIR)
    for xx in (x,x+8):
        v.box(xx,1,z,xx,5,z,S('polished_deepslate'))
    v.box(x-1,6,z-1,x+9,6,z+17,S('deepslate_tile_slab'))
    for zz in (z+3,z+9,z+15):
        v.set(x+4,1,zz,S('polished_deepslate'))
        v.set(x+4,2,zz,S('sea_lantern'))
        for xx in (x-1,x+9):
            v.box(xx,2,zz-1,xx,3,zz+1,S('cyan_stained_glass'))
            v.box(xx,4,zz-2,xx,4,zz+2,S('spruce_slab',type='top'))
    for i in range(4):
        v.box(x-2+i,7+i,z-2,x+10-i,7+i,z+18,S('deepslate_tiles'))
        for xx,facing in ((x-2+i,'east'),(x+10-i,'west')):
            v.box(xx,7+i,z-2,xx,7+i,z+18,S('deepslate_tile_stairs',facing=facing))
    for xx in (x-1,x+9):
        v.set(xx,1,z-2,S('mossy_stone_bricks'))
        v.set(xx,2,z-2,S('potted_fern'))


def underground_arcade():
    """A complete recessed public alcove: masonry arch, lit glazing, counters and seating."""
    v=Voxels()
    v.box(0,-26,0,12,-17,8,S('stone_bricks'))
    v.box(1,-25,0,11,-18,7,AIR)
    for x in (0,12):
        v.box(x,-25,0,x,-19,0,S('polished_deepslate'))
        v.set(x,-18,0,S('chiseled_stone_bricks'))
    for x,top in ((1,-19),(2,-18),(3,-17),(9,-17),(10,-18),(11,-19)):
        v.box(x,top,0,x,-17,0,S('stone_bricks'))
        v.set(x,top-1,0,S('stone_brick_stairs',facing='east' if x<6 else 'west',half='top'))
    v.box(3,-23,8,9,-20,8,S('cyan_stained_glass'))
    for x in (3,6,9):v.set(x,-22,9,S('sea_lantern'))
    v.box(2,-25,3,4,-25,3,S('spruce_planks'))
    v.box(8,-25,3,10,-25,3,S('spruce_planks'))
    for x in (2,10):
        v.set(x,-24,3,S('potted_fern'))
        v.box(x,-25,6,x,-23,6,S('bookshelf'))
    v.set(6,-18,4,S('lantern',hanging=True))
    for x in (5,7):v.set(x,-25,6,S('jugcraft:crafted_oak_chair',facing='north'))
    return v


def undercity(v):
    cx,cz=-102,105
    for x in range(cx-39,cx+40):
        for z in range(cz-39,cz+40):
            r=math.hypot(x-cx,z-cz)
            if r>39: continue
            roof=-12+round(9*math.sqrt(max(0,1-(r/39)**2)))
            v.box(x,-28,z,x,roof,z,S('deepslate_bricks'))
            if r<37:
                v.box(x,-25,z,x,roof-2,z,AIR)
                v.set(x,-26,z,S('polished_deepslate' if int(r)%9==0 else 'stone_bricks'))
            if 29<=r<36:
                v.set(x,-12,z,S('stone_bricks'))
                if 29<=r<30: v.set(x,-11,z,S('stone_brick_wall'))
            if r<9:
                v.set(x,-27,z,S('sea_lantern' if (x+z)%4==0 else 'dark_prismarine'))
                v.set(x,-26,z,S('water'))
            elif r<10: v.set(x,-26,z,S('polished_deepslate'))
    for a in range(0,360,45):
        x=cx+round(math.cos(math.radians(a))*33);z=cz+round(math.sin(math.radians(a))*33)
        v.box(x-1,-25,z-1,x+1,-13,z+1,S('polished_deepslate'))
        v.box(x-2,-13,z-2,x+2,-13,z+2,S('chiseled_stone_bricks'))
        v.set(x,-11,z,S('stone_brick_wall'))
        v.set(x,-10,z,S('sea_lantern'))
    for x in range(cx-36,cx+37):
        v.box(x,-12,cz-2,x,-12,cz+2,S('stone_bricks'))
        v.box(x,-11,cz-2,x,-8,cz+2,AIR)
        for z in (cz-3,cz+3):
            v.set(x,-11,z,S('stone_brick_wall') if abs(x-cx)<30 else AIR)
    # Walkable main-floor/gallery connections, with fourteen steps and full headroom.
    for i in range(14):
        v.box(-126,-24+i,115+i,-124,-21+i,115+i,AIR)
        v.box(-126,-25+i,115+i,-124,-25+i,115+i,S('stone_brick_stairs',facing='south'))
        v.set(-127,-24+i,115+i,S('stone_brick_wall'))
    v.box(-126,-11,129,-124,-8,130,AIR)
    v.box(-126,-12,129,-124,-12,130,S('stone_bricks'))
    # Ground-level arcades and lighting; empty bays reserve space for later services.
    for a in range(0,360,30):
        xx=cx+round(math.cos(math.radians(a))*22);zz=cz+round(math.sin(math.radians(a))*22)
        v.set(xx,-26,zz,S('sea_lantern'))
        gx=cx+round(math.cos(math.radians(a))*36);gz=cz+round(math.sin(math.radians(a))*36)
        v.set(gx,-11,gz,S('stone_brick_wall'))
        v.set(gx,-10,gz,S('sea_lantern'))
    for x in (cx-6,cx+6):
        v.box(x,-7,cz,x,-4,cz,S('iron_chain'))
        v.set(x,-8,cz,S('lantern',hanging=True))
    # Connected halls for future service rooms, not functioning factories yet.
    for x0,x1 in [(-182,-143),(-61,-22)]:
        v.box(x0,-28,88,x1,-16,124,S('deepslate_bricks'))
        v.box(x0+2,-25,90,x1-2,-18,122,AIR)
        for z in range(92,122,7):
            for x in (x0+2,x1-2):
                v.box(x,-19,z,x,-18,z,S('iron_chain'))
                v.set(x,-20,z,S('lantern',hanging=True))
        # A readable central promenade and hung lights, not an unlit empty vault.
        v.box(x0+16,-26,90,x0+22,-26,122,S('polished_andesite'))
        for z in (94,103,112,121):
            v.box(x0+2,-17,z,x1-2,-17,z,S('stripped_dark_oak_log',axis='x'))
            for x in (x0+9,x0+19,x0+29):v.set(x,-18,z,S('lantern',hanging=True))
        for x in (x0+4,x1-4):
            for z in (95,109):
                v.box(x,-25,z,x,-20,z,S('polished_deepslate'))
                v.box(x,-25,z+8,x,-20,z+8,S('polished_deepslate'))
                v.box(x,-20,z,x,-20,z+8,S('dark_oak_slab',type='top'))
                v.box(x,-25,z+2,x,-25,z+6,S('spruce_planks'))
                v.set(x,-24,z+4,S('potted_azalea_bush'))
    for x0,x1 in [(-145,-136),(-68,-59)]:
        v.box(x0,-25,102,x1,-21,108,AIR)
        v.box(x0,-26,102,x1,-26,108,S('stone_bricks'))
    for x in (-171,-43):
        stair_portal(v,x,69)
        # Tunnel from lower stair exit to each service hall.
        # Route around the stair rather than cutting its lower six steps away.
        for x0,z0,x1,z1 in [(x+5,65,x+7,68),(x+5,65,x+12,67),(x+10,65,x+12,92)]:
            v.box(x0-1,-27,z0-1,x1+1,-21,z1+1,S('deepslate_bricks'))
        for x0,z0,x1,z1 in [(x+5,65,x+7,68),(x+5,65,x+12,67),(x+10,65,x+12,92)]:
            v.box(x0,-26,z0,x1,-26,z1,S('stone_bricks'))
            v.box(x0,-25,z0,x1,-22,z1,AIR)
        stair_portal(v,x,69)
        v.box(x+10,-25,92,x+12,-22,94,AIR)
    for x in (cx-15,cx+15):
        v.paste(underground_arcade(),x-6,0,cz+29,origin_size=(13,10))
        v.paste(underground_arcade(),x-6,0,cz-38,turns=2,origin_size=(13,10))
    # Lantern-bearing braces light the gallery edge and emphasize its supports.
    for a in range(0,360,45):
        xx=cx+round(math.cos(math.radians(a))*28);zz=cz+round(math.sin(math.radians(a))*28)
        bx=cx+round(math.cos(math.radians(a))*32);bz=cz+round(math.sin(math.radians(a))*32)
        v.box(min(xx,bx),-13,min(zz,bz),max(xx,bx),-13,max(zz,bz),S('dark_oak_planks'))
        v.set(xx,-13,zz,S('chiseled_stone_bricks'))
        v.set(xx,-14,zz,S('lantern',hanging=True))


def build():
    v=Voxels();rooms=[];plots=[]
    # Foundation and a stepped apron; actual terrain adaptation is a surveyed task.
    v.box(-192,-3,16,-16,0,176,S('stone'))
    v.box(-189,0,19,-19,0,173,S('grass_block'))
    v.box(-192,1,16,-16,42,176,AIR)
    for x0,z0,x1,z1 in [(-192,92,-16,100),(-182,65,-27,69),(-182,144,-27,148),(-136,22,-132,166),(-86,22,-82,166)]:
        v.box(x0,0,z0,x1,0,z1,S('stone_bricks'))
    # Old-city connector is surface only. It never clears the old gate or houses.
    for x in range(-15,17):
        v.box(x,0,94,x,0,98,S('stone_bricks'))
    for num,(x,z,tone,roof) in enumerate([(-177,25,'bricks','dark_oak'),(-126,25,'white_terracotta','spruce'),(-75,25,'mud_bricks','dark_oak')],1):
        house, local=apartment(tone,roof,num)
        v.paste(house,x,0,z,turns=2,origin_size=(31,37))
        for r in local:
            a,b,c,d,e,f=r['bounds'];dx,dy,dz=r['door']
            rooms.append({'id':r['id'],'bounds':[x+30-d,b,z+36-f,x+30-a,e,z+36-c],'door':[x+30-dx,dy,z+36-dz]})
    for id,x,z,w,d,roof in [('furniture',-178,111,31,23,'spruce'),('exchange',-126,111,31,23,'dark_oak'),('tailor',-75,111,27,23,'spruce'),('portraits',-178,151,31,19,'dark_oak'),('post',-126,151,31,19,'spruce')]:
        shop(v,x,z,w,d,roof)
        plots.append({'id':id,'bounds':[x,1,z,x+w-1,11,z+d-1]})
    # Stable paddock with wide access; no horses spawned until service behavior exists.
    shop(v,-75,151,27,19,'dark_oak','stone_bricks')
    v.box(-64,1,148,-60,5,154,AIR)
    v.box(-73,1,156,-50,4,168,AIR)
    for x in (-73,-65,-57,-50):v.box(x,1,157,x,2,168,S('spruce_fence'))
    for x in (-70,-62,-54):
        v.box(x,1,166,x+2,1,168,S('hay_block'))
        v.set(x+3,1,165,S('water_cauldron',level=3))
    plots.append({'id':'stables','bounds':[-75,1,151,-49,11,169]})
    for x in (-185,-141,-90,-34):
        for z in (86,105,141):lamp(v,x,1,z)
    for x in range(-182,-25,16):
        for z in (90,102):lamp(v,x,1,z)
    # Small sheltered market pitches: surfaces and circulation now, leasing later.
    for x in (-178,-166,-154):
        v.box(x,1,138,x+5,1,138,S('spruce_planks'))
        for xx in (x,x+5):
            for zz in (138,142):v.box(xx,1,zz,xx,3,zz,S('spruce_fence'))
        v.box(x-1,4,137,x+6,4,143,S('red_wool'))
        for xx in range(x,x+6,2):v.box(xx,4,137,xx,4,143,S('white_wool'))
        v.set(x+2,3,140,S('lantern',hanging=True))
    # New perimeter stays away from the existing gate; east edge becomes an internal frontage.
    for x0,z0,x1,z1 in [(-192,16,-192,176),(-192,16,-16,16),(-192,176,-16,176)]:
        v.box(x0,1,z0,x1,5,z1,S('stone_bricks'))
        for x in range(x0,x1+1):
            for z in range(z0,z1+1):
                if (x+z)%2==0:v.set(x,6,z,S('stone_brick_wall'))
    v.box(-193,1,92,-190,5,100,AIR)
    undercity(v)
    # Covered grass naturally becomes dirt, which would otherwise look like a later
    # owner edit to the conservative rollback. Author stable paving beneath fixtures.
    for (x,y,z),state in list(v.b.items()):
        if y==0 and parse(state)[0]=='minecraft:grass_block' and v.get(x,1,z,AIR)!=AIR:
            v.set(x,0,z,S('stone_bricks'))
    connections(v)
    stair_shapes(v)
    return v,rooms,plots


def payload(v,rooms,plots,*,shell_first=False):
    palette=sorted(set(v.b.values()));lookup={s:i for i,s in enumerate(palette)}
    phases = {}
    for state in palette:
        name=parse(state)[0]
        if name in ('minecraft:water','minecraft:lava'):
            phases[state]=3
        elif name.endswith(('_door','_bed')):
            phases[state]=2
        elif name == 'minecraft:air':
            phases[state]=1 if shell_first else 0
        else:
            phases[state]=0 if shell_first else 1
    def placement_order(entry):
        (x,y,z),s=entry
        return phases[s],y,x,z
    return {'version':1,'anchor':'saved_town_origin','bounds':list(v.bounds()),'palette':palette,
            'cells':[[x,y,z,lookup[s]] for (x,y,z),s in sorted(v.b.items(),key=placement_order)],
            'rooms':rooms,'plots':plots,'installation':'survey_required','automatic':False}


def main():
    v,rooms,plots=build();data=payload(v,rooms,plots)
    raw=json.dumps(data,separators=(',',':')).encode('utf-8')
    OUT.parent.mkdir(parents=True,exist_ok=True)
    packed=bytearray(gzip.compress(raw,mtime=0));packed[9]=255;OUT.write_bytes(packed)
    folder=ROOT/'build/western-city';folder.mkdir(parents=True,exist_ok=True)
    from town_preview import iso
    # Debug geometry previews, not texture/render acceptance.
    for name,vox in [('apartment',apartment('bricks','dark_oak',1)[0])]:
        iso(vox,scale=3).save(folder/f'{name}.png')
    surface=Voxels();surface.b={p:s for p,s in v.b.items() if p[1]>=0}
    iso(surface,scale=2).save(folder/'surface.png')
    cut=Voxels();cut.b={p:s for p,s in v.b.items() if p[1]<-12 and p[2]>=105}
    iso(cut,scale=2).save(folder/'undercity-cutaway.png')
    print(f'{len(v.b)} planned cells; {len(palette) if (palette:=data["palette"]) else 0} states; {len(rooms)} rooms; no world edited')


if __name__=='__main__':main()
